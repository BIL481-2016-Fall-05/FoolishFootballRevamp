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
	public ArrayList<Position> assignments = new ArrayList<Position>();
	Position targetPosition = null;
	private GamePhysics game;
	Semaphore s1;

	boolean assignmentInProgress;

	boolean isSelected = false;
	boolean clearShoot = false;
	boolean isGoingForScore;
	boolean isDribbling;
	boolean isStoleTheBallRecently;
	boolean inIdleState;
	boolean isPursuingPlayer;
	boolean isDefending;
	boolean pause = false;

    boolean ballIsStolenRecently;
    boolean isKickedRecently;

	boolean atBehindOfPlayer;
	boolean atFrontOfPlayer;
	boolean atLeftsideOfPlayer;
	boolean atRightsideOfPlayer;

	public Opponent(int i, Team team, PlayerStats stats, DWorld world, DSpace space, GamePhysics game) {
		super(i, team, stats, world, space);
		this.game = game;
        s1 = new Semaphore(0);
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

	private class JobAssignerAgent extends Thread {
		public void run() {
			while (true) {
			    if(pause) {
			        break;
                }
                try {
                    sleep(25);
                } catch (InterruptedException e) {

                }

                if(isSelected) {
                    if(clearShoot) {
                        Opponent.this.kick(game.getBall());
                        clearShoot = false;
                    }
                    if(isBallOwner()) {
                        assignments.add(new Position(Opponent.this.getPosition().toDVector().add(-2,-2,0)));
                        assignments.add(new Position(Opponent.this.getPosition().toDVector().add(-4,-2,0)));
                        assignments.add(new Position(Opponent.this.getPosition().toDVector().add(-6,0,0)));
                        assignments.add(new Position(Opponent.this.getPosition().toDVector().add(-6,2,0)));
                        assignments.add(game.getPitch().getGoalTop());
                        try {
                            s1.acquire();
                        } catch (InterruptedException e) {
                        }
                    } else {
                        if(game.getBall().getPosition().distance(Opponent.this.getPosition()) < 1.5) {
                            Opponent.this.steal(game.getBall());
                        }
                        assignments.add(game.getBall().getPosition());
                        isPursuingPlayer = true;
                        try {
                            s1.acquire();
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
                if(pause) {
                    break;
                }

                if(game.getScore() != 0) {
                    pause = true;
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
                        s1.release();
                    }

                    if(assignments.size() > 0) { // There are still some assignments
                        if (isPursuingPlayer && game.getBall().getPosition().distance(assignments.get(0)) > 2) { // Still pursuing player, release job assigner
                            s1.release();
                        }
                    }
                }
            }
		}
	}

    private class AssignmentHandlerAgent extends Thread {
        public void run() {
            while (true) {
                if(pause) {
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
                if(pause) {
                    break;
                }

                try {
                    sleep(25);
                } catch (InterruptedException e) {

                }
                if(assignmentInProgress && Opponent.this.getPosition().distance(targetPosition) < 1) {
                    //System.out.println("Job finished");
                    assignmentInProgress = false;
                    if(assignments.size() == 0) {
                        targetPosition = null;
                        s1.release();
                    }
                }
            }
        }
    }
}