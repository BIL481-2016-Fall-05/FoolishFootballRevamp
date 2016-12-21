package com.github.fommil.ff.physics;

import com.github.fommil.ff.Pitch;

import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Threaded Assignment module for a opponent player.
 *
 * @author Doga Can Yanikoglu
 */
public class Assignment extends Thread implements Comparable {
    public enum DO { BLOCK_PLAYER, PURSUIT_PLAYER, GET_BALL, GO_TO_AM, GO_TO_F, GO_TO_LOWER_LW, GO_TO_LOWER_RW, GO_TO_DM, GO_TO_LWB,
        GO_TO_RWB, GO_TO_LB, GO_TO_RB, GO_TO_CB, GO_FOR_GOAL, FEINT}
    private volatile boolean flag = true;
    private volatile DO action;
    private volatile int priority;
    private volatile boolean canFeint;
    private volatile boolean canShoot;
    private volatile boolean canPass;
    private final Stack<Position> targets = new Stack<Position>();
    private volatile Opponent assignee;
    private volatile GamePhysics game;
    public volatile Semaphore jobMonitor;
    private volatile ThreadLocalRandom probabilityGenerator;

    public Assignment(Opponent assignee, DO action, GamePhysics game) {
        this.game = game;
        this.assignee = assignee;
        this.action = action;
        this.jobMonitor = new Semaphore(0);
        probabilityGenerator = ThreadLocalRandom.current();
        switch (action) {
            case BLOCK_PLAYER:
                break;
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
            case GO_FOR_GOAL:
                targets.push(new Position(game.getPitch().getGoalBottom().toDVector().add(-5,0,0)));
                canShoot = true;
                canPass = false;
                canFeint = true;
                break;
            case GO_TO_DM:
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
            case GO_TO_LWB:
                targets.push(new Position(game.getPitch().leftWingBack.topPos().toDVector()));
                canShoot = false;
                canPass = false;
                canFeint = true;
                break;
            case GO_TO_RWB:
                targets.push(new Position(game.getPitch().rightWingBack.topPos().toDVector()));
                canShoot = false;
                canPass = false;
                canFeint = true;
                break;
            case GO_TO_AM:
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
            case GO_TO_F:
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
            case GO_TO_LOWER_LW:
                targets.push(new Position(game.getPitch().leftWing.bottomPos().toDVector()));
                canShoot = false;
                canFeint = true;
                break;
            case GO_TO_LOWER_RW:
                targets.push(new Position(game.getPitch().rightWing.bottomPos().toDVector()));
                System.out.println(targets.peek());
                canShoot = false;
                canFeint = true;
                break;
            case FEINT:
                System.out.println("FEINT");
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
                    case GET_BALL:
                        if(assignee.isBallOwner() || game.getSelected().isBallOwner()) {
                            dismissAssignment(true);
                            break;
                        }
                        targets.pop();
                        targets.push(game.getBall().getPosition());
                        break;
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
                    case GO_FOR_GOAL:
                        if (game.getBall().getOwner() == null) {
                            dismissAssignment(true);
                        }
                        break;
                    case GO_TO_LOWER_LW:
                        if(assignee.getPosition().distance(targets.peek()) < 1) {
                            targets.pop();
                            assignee.setFacing(new Position(game.getPitch().getGoalBottom().toDVector().add(0,4,0)));
                            assignee.kick(game.getBall());
                            dismissAssignment(true);
                        }
                        break;
                    case GO_TO_LOWER_RW:
                        if(assignee.getPosition().distance(targets.peek()) < 1) {
                            targets.pop();
                            assignee.setFacing(new Position(game.getPitch().getGoalBottom().toDVector().add(0,4,0)));
                            assignee.kick(game.getBall());
                            dismissAssignment(true);
                        }
                        break;
                    case GO_TO_F:
                        if(assignee.getPosition().distance(targets.peek()) < 1) {
                            targets.pop();
                            dismissAssignment(true);
                        }
                        break;
                    case GO_TO_LWB:
                        if(assignee.getPosition().distance(targets.peek()) < 1) {
                            targets.pop();
                            dismissAssignment(true);
                        }
                        break;
                    case GO_TO_RWB:
                        if(assignee.getPosition().distance(targets.peek()) < 1) {
                            targets.pop();
                            dismissAssignment(true);
                        }
                        break;
                    case GO_TO_DM:
                        if(assignee.getPosition().distance(targets.peek()) < 1) {
                            targets.pop();
                            dismissAssignment(true);
                        }
                        break;
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

                if(canPass) {
                    Opponent o = checkTeammatesToPass();
                    if(o != null) {
                        assignee.pass(o,game.getBall());
                        dismissAssignment(true);
                    }
                }

                if(canShoot && assignee.getPosition().distance(game.getPitch().getGoalBottom()) < 13) {
                    assignee.setFacing(game.getPitch().getGoalBottom());
                    assignee.kick(game.getBall());
                    dismissAssignment(true);
                    break;
                }

                if(canFeint) {
                    Player p = checkPlayersToFeint();
                    if(p != null) {
                        if(++assignee.feintMass > 3) {
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

    private void dismissAssignment(Boolean isSemaphoreUsed) {
        assignee.assignments.remove(this);
        if(isSemaphoreUsed)
            jobMonitor.release();
        flag = false;
    }

    private Player checkPlayersToFeint() { // TODO Fix range values
        for(Player p: game.getOwningPlayers()) {
            double rangeTangent = GamePhysics.toAngle(p.getPosition().toDVector().sub(assignee.getPosition().toDVector()));
            if(p.getPosition().distance(assignee.getPosition()) < 3 && ( (rangeTangent < 3 && rangeTangent > 2) || (rangeTangent > -3 && rangeTangent < -2) )) {
                return p;
            }
        }
        return null;
    }

    private Opponent checkTeammatesToPass() { // TODO Check if player is blocking the passing line
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
            return null;
        }
    }

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