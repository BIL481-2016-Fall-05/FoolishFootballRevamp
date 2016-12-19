package com.github.fommil.ff.physics;

import java.util.concurrent.Semaphore;

/**
 * Created by dyani on 12/19/2016.
 */
public class Assignment extends Thread implements Comparable{
    public enum DO { BLOCK_PLAYER, PURSUIT_PLAYER, GET_BALL, GO_TO_MID, GO_TO_OPPONENT_GOAL, GO_TO_HOME_GOAL};
    volatile boolean flag = true;
    volatile DO action;
    volatile int priority;
    volatile boolean canFeint;
    volatile boolean canShoot;
    volatile Position target;
    volatile Opponent assignee;
    volatile GamePhysics game;
    public volatile Semaphore jobMonitor;

    public Assignment(Opponent assignee, DO action, GamePhysics game) {
        this.game = game;
        this.assignee = assignee;
        this.action = action;
        this.jobMonitor = new Semaphore(0);
        switch (action) {
            case BLOCK_PLAYER:
                break;
            case PURSUIT_PLAYER:
                target = game.getSelected().getPosition();
                canShoot = false;
                canFeint = false;
                break;
            case GET_BALL:
                target = game.getBall().getPosition();
                canShoot = false;
                canFeint = false;
                break;
            case GO_TO_MID:
                break;
            case GO_TO_OPPONENT_GOAL:
                target = new Position(game.getPitch().getGoalBottom().toDVector().add(5,0,0));
                canShoot = true;
                canFeint = true;
                break;
            case GO_TO_HOME_GOAL:
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
                        if(assignee.isBallOwner()) {
                            assignee.assignments.remove(this);
                            jobMonitor.release();
                            flag = false;
                            break;
                        }
                        if(game.getSelected().isBallOwner()) {
                            assignee.assignments.remove(this);
                            jobMonitor.release();
                            flag = false;
                            break;
                        }
                        target = game.getBall().getPosition();
                        break;
                    case PURSUIT_PLAYER:
                        if (!game.getSelected().isBallOwner()) {
                            assignee.assignments.remove(this);
                            jobMonitor.release();
                            flag = false;
                            break;
                        }
                        if (game.getSelected().getPosition().distance(assignee.getPosition()) < 1) {
                            assignee.steal(game.getBall());
                        }
                        target = game.getSelected().getPosition();
                        break;
                    case GO_TO_OPPONENT_GOAL:
                        if(canShoot && assignee.getPosition().distance(game.getPitch().getGoalBottom()) < 13) {
                            assignee.body.setRotation(assignee.createRotationMatrix(game.getPitch().getGoalBottom()));
                            assignee.kick(game.getBall());
                        }
                        if (!game.getSelected().isBallOwner()) {
                            assignee.assignments.remove(this);
                            jobMonitor.release();
                            flag = false;
                            break;
                        }
                        break;
                }
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
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
