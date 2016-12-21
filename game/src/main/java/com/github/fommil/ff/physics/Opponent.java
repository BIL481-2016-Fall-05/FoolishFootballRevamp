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
    public Queue<Assignment> assignments = new PriorityQueue<Assignment>();

    private volatile Pitch.Area currentArea;
    private volatile boolean assignmentInProgress;
    private volatile boolean isSelected;
    public volatile int feintMass;

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

    public void select() {
	    for(Opponent o: game.getOpponentPlayers()) {
	        if(this != o) {
                o.flush();
            }
        }
        this.isSelected = true;
    }

    private void flush() {
	    assignmentInProgress = false;
	    isSelected = false;
	    currentArea = null;
	    feintMass = 0;
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
                            if(game.getPitch().leftBack == currentArea) {
                                System.out.println("leftback");
                                assignJob(Assignment.DO.GO_TO_LWB)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().rightBack == currentArea) {
                                System.out.println("rightBack");
                                assignJob(Assignment.DO.GO_TO_RWB)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().leftWingBack == currentArea) {
                                System.out.println("leftWingBack");
                                assignJob(Assignment.DO.GO_TO_AM)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().rightWingBack == currentArea) {
                                System.out.println("rightWingBack");
                                assignJob(Assignment.DO.GO_TO_AM)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().defensiveMid == currentArea) {
                                System.out.println("defensiveMid");
                                assignJob(Assignment.DO.GO_TO_AM)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().centralBack == currentArea) {
                                System.out.println("centralBack");
                                assignJob(Assignment.DO.GO_TO_DM)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().attackerMid == currentArea) {
                                System.out.println("attackerMid");
                                assignJob(Assignment.DO.GO_TO_F)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().leftWing == currentArea) {
                                System.out.println("leftWing");
                                assignJob(Assignment.DO.GO_TO_LOWER_LW)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().rightWing == currentArea) {
                                System.out.println("rightWing");
                                assignJob(Assignment.DO.GO_TO_LOWER_RW)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else if(game.getPitch().forward == currentArea) {
                                System.out.println("forward");
                                assignJob(Assignment.DO.GO_FOR_GOAL)
                                        .jobMonitor.acquire();
                                assignmentInProgress = false;
                            }
                            else {
                                System.out.println("PLAYER IS IN OUT OF BOUNDS");
                                assignmentInProgress = false;
                            }
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

    public Pitch.Area getArea() {
        return currentArea;
    }

    public Assignment assignJob(Assignment.DO action) {
        Assignment tempAssignment;
        tempAssignment = new Assignment(Opponent.this, action, game);
        assignmentInProgress = true;
        assignments.add(tempAssignment);
        return tempAssignment;
    }
}