package com.github.fommil.ff.physics;

import com.github.fommil.ff.PlayerStats;
import com.github.fommil.ff.Team;
import org.ode4j.math.DVector3;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * Threaded AI module for a opponent player.
 *
 * @author Doga Can Yanikoglu
 */
public class Opponent extends Player {

	private final JobAssignerAgent assignAgent;
	private final ConditionCheckerAgent checkAgent;
	private final StatusUpdaterAgent statusAgent;
	private final AssignmentHandlerAgent handlerAgent;
    private ArrayList<Position> assignments;
    private Position targetPosition;
	private GamePhysics game;
    private Semaphore jobMonitor;

    private boolean assignmentInProgress;
    private boolean isSelected;
    private boolean clearShoot;
    private boolean isGoingForScore;
    private boolean isStoleTheBallRecently;
    private boolean inIdleState;
    private boolean isPursuingPlayer;
    private boolean isDefending;
    private boolean ballIsStolenRecently;
    private boolean isKickedRecently;
	private boolean atBehindOfPlayer;
    private boolean atFrontOfPlayer;
    private boolean atLeftsideOfPlayer;
    private boolean atRightsideOfPlayer;

	public Opponent(int i, Team team, PlayerStats stats, DWorld world, DSpace space, GamePhysics game) {
		super(i, team, stats, world, space);
		this.game = game;
		assignments = new ArrayList<Position>();
        targetPosition = null;
        isSelected = false;
        clearShoot = false;
        jobMonitor = new Semaphore(0);
        checkAgent = new ConditionCheckerAgent();
        checkAgent.start();
        statusAgent = new StatusUpdaterAgent();
        statusAgent.start();
        handlerAgent = new AssignmentHandlerAgent();
        handlerAgent.start();
		assignAgent = new JobAssignerAgent();
		assignAgent.start();
	}

	public void select() {
	    for(Opponent o: game.getOpponentPlayers()) {
            o.isSelected = false;
        }
        this.isSelected = true;
    }

    public boolean isSelected() {
	    return isSelected;
    }

    public Position getTargetPosition() {
	    return targetPosition;
    }

	private class JobAssignerAgent extends Thread {
		public void run() {
			while (true) {
			    if(game.getPauseState()) {
			        break;
                }
                try {
                    sleep(25);
                } catch (InterruptedException e) {

                }

                if(isSelected) {
                    if(clearShoot) {
                        clearShoot = false;
                        Opponent.this.kick(game.getBall());
                    }
                    if(isBallOwner()) {
                        isGoingForScore = true;
                        assignments.add(new Position(Opponent.this.getPosition().toDVector().add(-2,-2,0)));
                        assignments.add(new Position(Opponent.this.getPosition().toDVector().add(-4,-2,0)));
                        assignments.add(new Position(Opponent.this.getPosition().toDVector().add(-6,0,0)));
                        assignments.add(new Position(Opponent.this.getPosition().toDVector().add(-6,2,0)));
                        assignments.add(game.getPitch().getGoalTop());
                        try {
                            jobMonitor.acquire();
                        } catch (InterruptedException e) {
                        }
                        isGoingForScore = false;
                    } else {
                        isPursuingPlayer = true;
                        if(game.getBall().getPosition().distance(Opponent.this.getPosition()) < 1) {
                            Opponent.this.steal(game.getBall());
                        }
                        assignments.add(game.getBall().getPosition());
                        try {
                            jobMonitor.acquire();
                        } catch (InterruptedException e) {
                        }
                        isPursuingPlayer = false;

                    }
                }
            }
        }
    }

	private class ConditionCheckerAgent extends Thread {
		public void run() {
			while (true) {
			    try {
			        sleep(25);
                } catch (InterruptedException e) {

                }
                if(game.getPauseState()) {
                    break;
                }

                if(isSelected) {
                    DVector3 distVector = Opponent.this.getPosition().toDVector().sub(game.getSelected().getPosition().toDVector());
                    DVector3 relativeDirVector = distVector.scale(game.getSelected().getFacing().scale(-1, 1, 1));
                    if (relativeDirVector.get0() < 0) {
                        atLeftsideOfPlayer = true;
                        atRightsideOfPlayer = false;
                    } else {
                        atLeftsideOfPlayer = false;
                        atRightsideOfPlayer = true;
                    }

                    if (relativeDirVector.get1() < 0) {
                        atBehindOfPlayer = true;
                        atFrontOfPlayer = false;
                    } else {
                        atBehindOfPlayer = false;
                        atFrontOfPlayer = true;
                    }

                    if(isBallOwner() && Opponent.this.getPosition().distance(game.getPitch().getGoalTop()) < 13) { // Close to goal, just kick the ball
                        clearShoot = true;
                        jobMonitor.release();
                    }

                    if (isPursuingPlayer && assignments.size() > 0 && game.getBall().getPosition().distance(assignments.get(0)) > 1) { // Still pursuing player, release job assigner
                        jobMonitor.release();
                    }

                    if(isGoingForScore && !isBallOwner()) {
                        jobMonitor.release();
                    }
                }
            }
		}
	}

    private class AssignmentHandlerAgent extends Thread {
        public void run() {
            while (true) {
                if(game.getPauseState()) {
                    break;
                }

                try {
                    sleep(25);
                } catch (InterruptedException e) {

                }
                if(assignments.size() != 0 && !assignmentInProgress) {
                    targetPosition = assignments.remove(0);
                    assignmentInProgress = true;
                }
            }
        }
    }

    private class StatusUpdaterAgent extends Thread {
        public void run() {
            while (true) {
                if(game.getPauseState()) {
                    break;
                }

                try {
                    sleep(25);
                } catch (InterruptedException e) {

                }
                if(assignmentInProgress && Opponent.this.getPosition().distance(targetPosition) < 1) {
                    assignmentInProgress = false;
                    if(assignments.size() == 0) {
                        targetPosition = null;
                        jobMonitor.release();
                    }
                }
            }
        }
    }
}