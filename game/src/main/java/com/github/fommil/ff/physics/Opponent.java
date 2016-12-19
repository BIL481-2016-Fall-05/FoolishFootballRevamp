package com.github.fommil.ff.physics;

import com.github.fommil.ff.PlayerStats;
import com.github.fommil.ff.Team;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Threaded AI module for a opponent player.
 *
 * @author Doga Can Yanikoglu
 */
public class Opponent extends Player {

	private final JobAssignerAgent assignAgent;
	private final ConditionCheckerAgent checkAgent;
    public Queue<Assignment> assignments = new PriorityQueue<Assignment>();
	private final GamePhysics game;

    private volatile boolean assignmentInProgress;
    private volatile boolean isSelected;
    private volatile boolean clearShoot;
    private volatile boolean isGoingForScore;
    private volatile boolean isStoleTheBallRecently;
    private volatile boolean inIdleState;
    private volatile boolean isPursuingPlayer;
    private volatile boolean isDefending;
    private volatile boolean ballIsStolenRecently;
    private volatile boolean isKickedRecently;
    public volatile boolean atFrontOfPlayer;

	public Opponent(int i, Team team, PlayerStats stats, DWorld world, DSpace space, GamePhysics game) {
		super(i, team, stats, world, space);
		this.game = game;

        isSelected = false;
        clearShoot = false;
        checkAgent = new ConditionCheckerAgent();
        //checkAgent.start();
		assignAgent = new JobAssignerAgent();
		assignAgent.start();
	}

	public void select() {
	    for(Opponent o: game.getOpponentPlayers()) {
	        if(this != o) {
                o.flush();
            }
        }
        this.isSelected = true;
    }

    public void flush() {
	    assignmentInProgress = false;
	    isSelected = false;
	    clearShoot = false;
	    isGoingForScore = false;
	    isStoleTheBallRecently = false;
	    inIdleState = false;
	    isPursuingPlayer = false;
	    isDefending = false;
	    ballIsStolenRecently = false;
	    isKickedRecently = false;
	    atFrontOfPlayer = false;
        assignments.clear();
    }

    public boolean isSelected() {
	    return isSelected;
    }

	private class JobAssignerAgent extends Thread {
		public void run() {
            try {
                while (true) {
                    if (game.getPauseState()) {
                        break;
                    }
                    sleep(25);

                    if (isSelected) {
                        if (isBallOwner()) {
                            Assignment tempAssignment = new Assignment(Opponent.this, Assignment.DO.GO_TO_OPPONENT_GOAL, game);
                            assignments.add(tempAssignment);
                            tempAssignment.jobMonitor.acquire();
                        }
                        else {
                            if (game.getSelected().isBallOwner()) {
                                Assignment tempAssignment = new Assignment(Opponent.this, Assignment.DO.PURSUIT_PLAYER, game);
                                assignments.add(tempAssignment);
                                tempAssignment.jobMonitor.acquire();
                            } else {
                                Assignment tempAssignment = new Assignment(Opponent.this, Assignment.DO.GET_BALL, game);
                                assignments.add(tempAssignment);
                                tempAssignment.jobMonitor.acquire();
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

	private class ConditionCheckerAgent extends Thread {
		public void run() {
			while (true) {
                if (game.getPauseState()) {
                    break;
                }

            }
		}
	}
}