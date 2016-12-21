package com.github.fommil.ff.physics;

import com.github.fommil.ff.Pitch;

import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Threaded Assignment module for a opponent players.
 *
 * @author Doga Can Yanikoglu
 */
public class Assignment extends Thread implements Comparable {
    /**
     * Enum declaration for specifying action type to do.
     */
    public enum DO { PURSUIT_PLAYER, GET_BALL, GO_TO_AM, GO_TO_F, GO_TO_LOWER_LW, GO_TO_LOWER_RW, GO_TO_DM, GO_TO_LWB,
        GO_TO_RWB, GO_FOR_SCORE, FEINT}
    private volatile boolean flag = true; // Ends thread if set to false
    private volatile DO action; // Action to be done
    private volatile int priority; // Priority of the assignment
    private volatile boolean canFeint; // Decides if player can feint or not
    private volatile boolean canShoot; // Decides if player can shoot to goal or not
    private volatile boolean canPass; // Decides if player can pass or not
    private final Stack<Position> targets = new Stack<Position>(); // Stack for storing target positions
    private volatile Opponent assignee; // Related player with this assignment
    private volatile GamePhysics game;
    private volatile Semaphore jobMonitor; // Semaphore for organizing player's thread
    private volatile ThreadLocalRandom probabilityGenerator; // Thread-safe random value generator

    public Assignment(Opponent assignee, DO action, GamePhysics game) {
        this.game = game;
        this.assignee = assignee;
        this.action = action;
        this.jobMonitor = new Semaphore(0);
        probabilityGenerator = ThreadLocalRandom.current();
        switch (action) {
            case PURSUIT_PLAYER:
                targets.push(game.getSelected().getPosition());
                canShoot = false;
                canPass = false;
                canFeint = false;
                break;
            case GET_BALL:
                targets.push(game.getBall().getPosition());
                canShoot = false;
                canPass = false;
                canFeint = false;
                break;
            case GO_FOR_SCORE:
                targets.push(new Position(game.getPitch().getGoalBottom().toDVector().add(-5,0,0)));
                canShoot = true;
                canPass = false;
                canFeint = true;
                break;
            case GO_TO_DM: // DefensiveMid
                if(probabilityGenerator.nextBoolean()) {
                    targets.push(new Position(game.getPitch().defensiveMid.leftPos().toDVector()));
                }
                else {
                    targets.push(new Position(game.getPitch().defensiveMid.rightTopPos().toDVector()));
                }
                canShoot = false;
                canPass = true;
                canFeint = true;
                break;
            case GO_TO_LWB: // Left Wing Back
                targets.push(new Position(game.getPitch().leftWingBack.topPos().toDVector()));
                canShoot = false;
                canPass = false;
                canFeint = true;
                break;
            case GO_TO_RWB: // Right Wing Back
                targets.push(new Position(game.getPitch().rightWingBack.topPos().toDVector()));
                canShoot = false;
                canPass = false;
                canFeint = true;
                break;
            case GO_TO_AM: // Attacker Mid
                if(assignee.getArea() == game.getPitch().leftWingBack) {
                    targets.push(new Position(game.getPitch().attackerMid.leftTopPos().toDVector()));
                } else if(assignee.getArea() == game.getPitch().rightWingBack) {
                    targets.push(new Position(game.getPitch().attackerMid.rightTopPos().toDVector()));
                } else { // Defenders Mid
                    targets.push(new Position(game.getPitch().attackerMid.centerPos().toDVector()));
                }
                canShoot = false;
                canPass = true;
                canFeint = true;
                break;
            case GO_TO_F: // Forward
                int probability = probabilityGenerator.nextInt(100);
                if(probability < 33) {
                    targets.push(new Position(game.getPitch().forward.leftTopPos().toDVector()));
                } else if(probability < 66) {
                    targets.push(new Position(game.getPitch().forward.topPos().toDVector()));
                } else {
                    targets.push(new Position(game.getPitch().forward.rightTopPos().toDVector()));
                }
                canPass = true;
                canShoot = false;
                canFeint = true;
                break;
            case GO_TO_LOWER_LW: // Lower-side of Left Wing
                targets.push(new Position(game.getPitch().leftWing.bottomPos().toDVector()));
                canShoot = false;
                canFeint = true;
                break;
            case GO_TO_LOWER_RW: // Lower-side of Right Wing
                targets.push(new Position(game.getPitch().rightWing.bottomPos().toDVector()));
                System.out.println(targets.peek());
                canShoot = false;
                canFeint = true;
                break;
            case FEINT:
                Player p = checkPlayersToFeint();
                if(probabilityGenerator.nextBoolean()) {
                    targets.push(new Position(p.getPosition().toDVector().add(2.2,0,0)));
                }
                else {
                    targets.push(new Position(p.getPosition().toDVector().add(-2.2,0,0)));
                }
                canFeint = false;
                canShoot = true;
                break;
        }
        this.start();
    }

    public void run() {
        try {
            while (flag) {
                sleep(25);
                switch (action) {
                    /**
                     * Try getting ball from pitch until the ball has an owner
                     */
                    case GET_BALL:
                        if(assignee.isBallOwner() || game.getSelected().isBallOwner()) {
                            dismissAssignment(true);
                            break;
                        }
                        targets.pop();
                        targets.push(game.getBall().getPosition());
                        break;

                    /**
                     * Keep updated target with player's position until ball is stolen or player lost the ball.
                     * If opponent gets close to ball he will try to steal it.
                     */
                    case PURSUIT_PLAYER:
                        if (game.getSelected().getPosition().distance(assignee.getPosition()) < 1) {
                            assignee.steal(game.getBall());
                        }
                        if (!game.getSelected().isBallOwner()) {
                            dismissAssignment(true);
                            break;
                        }
                        targets.pop();
                        targets.push(game.getSelected().getPosition());
                        break;

                    /**
                     * Go for score until ball is lost
                     */
                    case GO_FOR_SCORE:
                        if (game.getBall().getOwner() == null) {
                            dismissAssignment(true);
                        }
                        break;

                    /**
                     * Go to lower-side of left wing. After arriving to target, cross-ball to penalty area.
                     */
                    case GO_TO_LOWER_LW:
                        if(assignee.getPosition().distance(targets.peek()) < 1) {
                            targets.pop();
                            assignee.setFacing(new Position(game.getPitch().getGoalBottom().toDVector().add(0,4,0)));
                            assignee.kick(game.getBall());
                            dismissAssignment(true);
                        }
                        break;

                    /**
                     * Go to lower-side of right wing. After arriving to target, cross-ball to penalty area.
                     */
                    case GO_TO_LOWER_RW:
                        if(assignee.getPosition().distance(targets.peek()) < 1) {
                            targets.pop();
                            assignee.setFacing(new Position(game.getPitch().getGoalBottom().toDVector().add(0,4,0)));
                            assignee.kick(game.getBall());
                            dismissAssignment(true);
                        }
                        break;

                    /**
                     * Go to forward area.
                     */
                    case GO_TO_F:
                        if(assignee.getPosition().distance(targets.peek()) < 1) {
                            targets.pop();
                            dismissAssignment(true);
                        }
                        break;

                    /**
                     * Go to left wing back area.
                     */
                    case GO_TO_LWB:
                        if(assignee.getPosition().distance(targets.peek()) < 1) {
                            targets.pop();
                            dismissAssignment(true);
                        }
                        break;

                    /**
                     * Go to right wing back area.
                     */
                    case GO_TO_RWB:
                        if(assignee.getPosition().distance(targets.peek()) < 1) {
                            targets.pop();
                            dismissAssignment(true);
                        }
                        break;

                    /**
                     * Go to defensive mid area.
                     */
                    case GO_TO_DM:
                        if(assignee.getPosition().distance(targets.peek()) < 1) {
                            targets.pop();
                            dismissAssignment(true);
                        }
                        break;

                    /**
                     * Control feint action's status. If player arrives to feint target locations, pop it from stack.
                     * If feint action is complete, dismiss this feint assignment.
                     */
                    case FEINT:
                        if(assignee.getPosition().distance(targets.peek()) < 0.5) {
                            targets.pop();
                        }
                        if(targets.isEmpty()) {
                            dismissAssignment(true);
                        }
                        break;
                }

                if(!flag)
                    break;

                if(canPass) { // If passing is allowed, try to pass. (Priority: High)
                    Opponent o = findTeammateToPass();
                    if(o != null) {
                        assignee.pass(o,game.getBall());
                        dismissAssignment(true);
                        break;
                    }
                }

                // If shooting is allowed, try to shoot to goal. (Priority: Medium)
                if(canShoot && assignee.getPosition().distance(game.getPitch().getGoalBottom()) < 13) {
                    assignee.setFacing(game.getPitch().getGoalBottom());
                    assignee.kick(game.getBall());
                    dismissAssignment(true);
                    break;
                }

                //If feinting is allowed, try to feint. (Priority: Low)
                if(canFeint) {
                    Player p = checkPlayersToFeint();
                    if(p != null) {
                        if(assignee.incrementFeintMass() > 3) { // If feint limit is reached, player can pass for escaping current situation
                            canPass = true;
                        }
                        else {
                            assignee.assignJob(DO.FEINT);
                            dismissAssignment(true);
                            break;
                        }
                    }
                }
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Dismiss this assignment from assignee's assignments, stop thread, and release the semaphore.
     * @param isSemaphoreUsed Specifies that if semaphore is used with this action or not
     */
    private void dismissAssignment(Boolean isSemaphoreUsed) {
        assignee.getAssignments().remove(this);
        if(isSemaphoreUsed)
            jobMonitor.release();
        flag = false;
    }

    /**
     * Checks if there is a player need to be feinted
     * @return Appropriate player to feint, null if no player is found
     */
    private Player checkPlayersToFeint() {
        for(Player p: game.getOwningPlayers()) {
            if(Math.abs(p.getPosition().x - assignee.getPosition().x) < 2
                    && assignee.getPosition().y - p.getPosition().y  < 2
                    && assignee.getPosition().y - p.getPosition().y  > 0) {
                return p;
            }
        }
        return null; // Don't feint
    }

    /**
     * Checks specified conditions and decides an teammate to pass
     * @return An appropriate teammate to pass, null if no teammates are found
     */
    private Opponent findTeammateToPass() {
        if(assignee.getArea() == game.getPitch().attackerMid) {
            double LWDist, RWDist, FDist;
            Opponent leftWingPassable = getPassableTeammateInArea(game.getPitch().leftWing);
            Opponent rightWingPassable = getPassableTeammateInArea(game.getPitch().rightWing);
            Opponent forwardPassable = getPassableTeammateInArea(game.getPitch().forward);
            if(leftWingPassable != null) {
                LWDist = assignee.getPosition().distance(leftWingPassable.getPosition());
            } else { LWDist = 9999;}

            if(rightWingPassable != null) {
                RWDist = assignee.getPosition().distance(rightWingPassable.getPosition());
            } else { RWDist = 9999;}

            if(forwardPassable != null) {
                FDist = assignee.getPosition().distance(forwardPassable.getPosition());
            } else { FDist = 9999;}

            if(LWDist < RWDist && LWDist < FDist) {
                return leftWingPassable;
            } else if(RWDist < LWDist && RWDist < FDist) {
                return rightWingPassable;
            } else {
                return forwardPassable;
            }
        }
        else if(assignee.getArea() == game.getPitch().leftWing || assignee.getArea() == game.getPitch().rightWing) {
            Opponent forwardPassable = getPassableTeammateInArea(game.getPitch().forward);
            return forwardPassable;
        }
        else if(assignee.getArea() == game.getPitch().leftWingBack) {
            Opponent leftWingPassable = getPassableTeammateInArea(game.getPitch().leftWing);
            return leftWingPassable;
        }
        else if(assignee.getArea() == game.getPitch().rightWingBack) {
            Opponent rightWingPassable = getPassableTeammateInArea(game.getPitch().rightWing);
            return rightWingPassable;
        }
        else if(assignee.getArea() == game.getPitch().defensiveMid) {
            double LWDist, RWDist;
            Opponent leftWingPassable = getPassableTeammateInArea(game.getPitch().leftWing);
            Opponent rightWingPassable = getPassableTeammateInArea(game.getPitch().rightWing);
            if(leftWingPassable != null) {
                LWDist = assignee.getPosition().distance(leftWingPassable.getPosition());
            } else { LWDist = 9999;}
            if(rightWingPassable != null) {
                RWDist = assignee.getPosition().distance(rightWingPassable.getPosition());
            } else { RWDist = 9999;}

            if(LWDist < RWDist) {
                return leftWingPassable;
            } else {
                return rightWingPassable;
            }
        }
        else if(assignee.getArea() == game.getPitch().centralBack) {
            double LWBDist, RWBDist;
            Opponent leftWingBackPassable = getPassableTeammateInArea(game.getPitch().leftWing);
            Opponent rightWingBackPassable = getPassableTeammateInArea(game.getPitch().rightWing);
            if(leftWingBackPassable != null) {
                LWBDist = assignee.getPosition().distance(leftWingBackPassable.getPosition());
            } else { LWBDist = 9999;}
            if(rightWingBackPassable != null) {
                RWBDist = assignee.getPosition().distance(rightWingBackPassable.getPosition());
            } else { RWBDist = 9999;}

            if(LWBDist < RWBDist) {
                return leftWingBackPassable;
            } else {
                return rightWingBackPassable;
            }
        }
        else {
            return null; // Don't pass
        }
    }

    /**
     * Returns closest, passable teammate
     * @param areaToPass Checks players just in this area
     * @return A passable teammate in specified area
     */
    private Opponent getPassableTeammateInArea(Pitch.Area areaToPass) {
        Opponent closest = null;
        double closestDist = 10;
        for(Opponent o: game.getOpponentsIn(areaToPass)) {
            double tempDist = assignee.getPosition().distance(o.getPosition());
            if(assignee.getPosition().y - o.getPosition().y > 2 && tempDist < closestDist) {
                closest = o;
                closestDist = tempDist;
            }
        }
        return closest;
    }

    Stack<Position> getTargets() {
        return targets;
    }

    public Semaphore getJobMonitor() {
        return jobMonitor;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof Assignment) {
            if(this.priority == ((Assignment) o).priority) {
                return 0;
            } else if(this.priority < ((Assignment) o).priority) {
                return -1;
            } else {
                return 1;
            }
        }
        return 0;
    }
}