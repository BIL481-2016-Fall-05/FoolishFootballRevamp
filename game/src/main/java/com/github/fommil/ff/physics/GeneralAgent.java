package com.github.fommil.ff.physics;

/**
 * A General Agent Model for Game
 * It selects closest opponent player to ball, and executes a counter to set ball's kicked recently state.
 * @author Doga Can Yanikoglu
 */
public class GeneralAgent extends Thread {
    GamePhysics game;

    public GeneralAgent(GamePhysics game) {
        this.game = game;
        this.start();
        BallCounter counter = new BallCounter();
        counter.start();
    }

    public void run() {
        try {
            while (true) {
                sleep(250);
                if (game.getSelected().isBallOwner()) { // Ball is on player
                    double minDistance = 999999999;
                    Opponent closest = null;
                    for (Opponent o : game.getOpponentPlayers()) {
                        double tempDistance = o.getPosition().distance(game.getBall().getPosition());
                        if (tempDistance < minDistance) {
                            minDistance = tempDistance;
                            closest = o;
                        }
                    }
                    closest.select();
                } else if (game.getBall().getOwner() == null) { // Ball is on pitch
                    double minDistance = 999999999;
                    Opponent closest = null;
                    for (Opponent o : game.getOpponentPlayers()) {
                        double tempDistance = o.getPosition().distance(game.getBall().getPosition());
                        if (tempDistance < minDistance) {
                            minDistance = tempDistance;
                            closest = o;
                        }
                    }
                    closest.select();
                } else if (game.getBall().getOwner() != null) { // Ball is on opponent team
                    for(Opponent o: game.getOpponentPlayers()){
                        if(o.isBallOwner()) {
                            o.select();
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class BallCounter extends Thread {
        public void run() {
            try {
                while (true) {
                    if (game.getBall().isKickedRecently()) {
                        sleep(350);
                        game.getBall().setKickStatus(false);
                    }
                    sleep(25);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}