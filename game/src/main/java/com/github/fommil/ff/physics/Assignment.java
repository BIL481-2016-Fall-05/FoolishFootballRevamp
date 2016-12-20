package com.github.fommil.ff.physics;

import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Threaded Assignment module for a opponent player.
 *
 * @author Doga Can Yanikoglu
 */
public class Assignment extends Thread implements Comparable {
    public enum DO { BLOCK_PLAYER, PURSUIT_PLAYER, GET_BALL, GO_TO_MID, GO_TO_OPPONENT_GOAL, GO_TO_HOME_GOAL, FEINT, PASS}
    private volatile boolean flag = true;
    private volatile DO action;
    private volatile int priority;
    private volatile boolean canFeint;
    private volatile boolean canShoot;
    private final Stack<Position> targets = new Stack<Position>();
    private volatile Opponent assignee;
    private volatile GamePhysics game;
    public volatile Semaphore jobMonitor;
    public ThreadLocalRandom probabilityGenerator;

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
                System.out.println("PURSUIT PLAYER");
                targets.push(game.getSelected().getPosition());
                canShoot = false;
                canFeint = false;
                break;
            case GET_BALL:
                System.out.println("GET BALL");
                targets.push(game.getBall().getPosition());
                canShoot = false;
                canFeint = false;
                break;
            case GO_TO_MID:
                break;
            case GO_TO_OPPONENT_GOAL:
                System.out.println("GO TO OPPONENT GOAL");
                targets.push(new Position(game.getPitch().getGoalBottom().toDVector().add(-5,0,0)));
                canShoot = true;
                canFeint = true;
                break;
            case GO_TO_HOME_GOAL:
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
            case PASS:
                System.out.println("PASS");
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
                    case GO_TO_OPPONENT_GOAL:
                        if (game.getSelected().isBallOwner()) {
                            dismissAssignment(true);
                            break;
                        }
                        break;
                    case FEINT:
                        if(assignee.getPosition().distance(targets.peek()) < 0.5) {
                            targets.pop();
                        }
                        if(targets.isEmpty()) {
                            dismissAssignment(false);
                        }
                        break;
                }

                if(canShoot && assignee.getPosition().distance(game.getPitch().getGoalBottom()) < 13) {
                    assignee.body.setRotation(assignee.createRotationMatrix(game.getPitch().getGoalBottom()));
                    assignee.kick(game.getBall());
                    dismissAssignment(true);
                }

                if(canFeint) {
                    Player p = checkPlayersToFeint();
                    if(p != null) {
                        assignee.assignJob(DO.FEINT);
                        dismissAssignment(true);
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

    private Player checkPlayersToFeint() {
        for(Player p: game.getOwningPlayers()) {
            double rangeTangent = GamePhysics.toAngle(p.getPosition().toDVector().sub(assignee.getPosition().toDVector()));
            if(p.getPosition().distance(assignee.getPosition()) < 3 && ( (rangeTangent < 3 && rangeTangent > 2) || (rangeTangent > -3 && rangeTangent < -2) )) {
                return p;
            }
        }
        return null;
    }

    public Stack<Position> getTargets() {
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