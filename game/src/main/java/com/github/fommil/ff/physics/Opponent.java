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

    private final GamePhysics game;
	private final JobAssignerAgent assignAgent;
	private final ConditionCheckerAgent checkAgent;
    public Queue<Assignment> assignments = new PriorityQueue<Assignment>();

    private volatile boolean assignmentInProgress;
    private volatile boolean isSelected;
    private volatile int feintMass;

	public Opponent(int i, Team team, PlayerStats stats, DWorld world, DSpace space, GamePhysics game) {
		super(i, team, stats, world, space);

		this.game = game;
        feintMass = 0;
        isSelected = false;
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

                    if (isSelected && assignments.isEmpty()) {
                        if (isBallOwner()) {
                            assignJob(Assignment.DO.GO_TO_OPPONENT_GOAL)
                                    .jobMonitor.acquire();
                            assignmentInProgress = false;
                        }
                        else {
                            if (game.getSelected().isBallOwner()) {
                                assignJob(Assignment.DO.PURSUIT_PLAYER)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else {
                                assignJob(Assignment.DO.GET_BALL)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Assignment assignJob(Assignment.DO action) {
        Assignment tempAssignment;
	    if(action.equals(Assignment.DO.FEINT) && ++feintMass > 3) { // Feint limit is reached, pass to another teammate
            feintMass = 0;
            tempAssignment = new Assignment(Opponent.this, Assignment.DO.PASS, game);
        } else {
            tempAssignment = new Assignment(Opponent.this, action, game);
        }
        assignmentInProgress = true;
        assignments.add(tempAssignment);
        return tempAssignment;
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