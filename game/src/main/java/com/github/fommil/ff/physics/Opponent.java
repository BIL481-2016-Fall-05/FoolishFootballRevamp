package com.github.fommil.ff.physics;

import com.github.fommil.ff.Pitch;
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
class Opponent extends Player {

	private final JobAssignerAgent assignAgent;
	private final AreaDetectAgent areaAgent;
    public Queue<Assignment> assignments = new PriorityQueue<Assignment>(); // Queue of all jobs assigned to this player

    private volatile Pitch.Area currentArea;
    private volatile boolean assignmentInProgress;
    private volatile boolean isSelected; // Controlled-State of this player
    public volatile int feintMass; // Count of total feints made before pass

    /**
     * Constructor for Opponent Class
     */
    public Opponent(int i, Team team, PlayerStats stats, DWorld world, DSpace space, GamePhysics game) {
		super(i, team, stats, world, space, game);
        currentArea = null;
        feintMass = 0;
        isSelected = false;
        areaAgent = new AreaDetectAgent();
        areaAgent.start();
		assignAgent = new JobAssignerAgent();
		assignAgent.start();
	}

    /**
     * Selects this opponent to be controlled. Other opponents are flushed to return them initial state.
     */
    public synchronized void select() {
	    for(Opponent o: game.getOpponentPlayers()) {
	        if(this != o) {
                o.flush();
            }
        }
        this.isSelected = true;
    }

    /**
     * Sets all of critical variables to their initial states.
     */
    private synchronized void flush() {
	    assignmentInProgress = false;
	    isSelected = false;
	    currentArea = null;
	    feintMass = 0;
        assignments.clear();
    }

    /**
     * Returns if this opponent is controlled right now or not
     */
    public synchronized boolean isSelected() {
	    return isSelected;
    }

    /**
     * Returns current area of this player
     */
    public synchronized Pitch.Area getArea() {
        return currentArea;
    }

    /**
     * Assigns a job to this player, and returns it
     */
    public synchronized Assignment assignJob(Assignment.DO action) {
        Assignment tempAssignment;
        tempAssignment = new Assignment(Opponent.this, action, game);
        assignmentInProgress = true;
        assignments.add(tempAssignment);
        return tempAssignment;
    }

    /**
     * Thread for assigning jobs to this player
     */
	private class JobAssignerAgent extends Thread {
		public void run() {
            try {
                while (true) {
                    if (game.getPauseState()) {
                        break;
                    }
                    sleep(25);

                    // If this player is selected, and there are no more assignments to handle, start assigning new jobs
                    if (isSelected && assignments.isEmpty()) {
                        if (isBallOwner()) { // Ball is on this player
                            if(game.getPitch().leftBack == currentArea) {
                                assignJob(Assignment.DO.GO_TO_LWB)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().rightBack == currentArea) {
                                assignJob(Assignment.DO.GO_TO_RWB)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().leftWingBack == currentArea) {
                                assignJob(Assignment.DO.GO_TO_AM)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().rightWingBack == currentArea) {
                                assignJob(Assignment.DO.GO_TO_AM)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().defensiveMid == currentArea) {
                                assignJob(Assignment.DO.GO_TO_AM)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().centralBack == currentArea) {
                                assignJob(Assignment.DO.GO_TO_DM)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().attackerMid == currentArea) {
                                assignJob(Assignment.DO.GO_TO_F)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().leftWing == currentArea) {
                                assignJob(Assignment.DO.GO_TO_LOWER_LW)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().rightWing == currentArea) {
                                assignJob(Assignment.DO.GO_TO_LOWER_RW)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().forward == currentArea) {
                                assignJob(Assignment.DO.GO_FOR_SCORE)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else {
                                assignmentInProgress = false;
                            }
                        }
                        else {
                            if (game.getSelected().isBallOwner()) { // Ball is on opponent player
                                assignJob(Assignment.DO.PURSUIT_PLAYER)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else { // Ball is on pitch
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

    /**
     * Thread for setting current are of this player
     */
    private class AreaDetectAgent extends Thread {
        public void run() {
            try {
                while (true) {
                    sleep(25);
                    if (game.getPitch().leftBack.isInside(getPosition())) {
                        currentArea = game.getPitch().leftBack;
                    } else if (game.getPitch().rightBack.isInside(getPosition())) {
                        currentArea = game.getPitch().rightBack;
                    } else if (game.getPitch().leftWingBack.isInside(getPosition())) {
                        currentArea = game.getPitch().leftWingBack;
                    } else if (game.getPitch().rightWingBack.isInside(getPosition())) {
                        currentArea = game.getPitch().rightWingBack;
                    } else if (game.getPitch().defensiveMid.isInside(getPosition())) {
                        currentArea = game.getPitch().defensiveMid;
                    } else if (game.getPitch().centralBack.isInside(getPosition())) {
                        currentArea = game.getPitch().centralBack;
                    } else if (game.getPitch().attackerMid.isInside(getPosition())) {
                        currentArea = game.getPitch().attackerMid;
                    } else if (game.getPitch().leftWing.isInside(getPosition())) {
                        currentArea = game.getPitch().leftWing;
                    } else if (game.getPitch().rightWing.isInside(getPosition())) {
                        currentArea = game.getPitch().rightWing;
                    } else if (game.getPitch().forward.isInside(getPosition())) {
                        currentArea = game.getPitch().forward;
                    } else {
                        currentArea = null;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}