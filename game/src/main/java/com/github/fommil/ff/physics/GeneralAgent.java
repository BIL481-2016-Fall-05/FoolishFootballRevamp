package com.github.fommil.ff.physics;

/**
 *
 *
 * @author Doga Can Yanikoglu
 */
public class GeneralAgent extends Thread{

    GamePhysics game;

    public GeneralAgent(GamePhysics game) {
        this.game = game;
        this.start();
    }

    public void run() {
        while(true) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {

            }
            if(game.getSelected().isBallOwner()) { // Ball is on player
                double minDistance = 999999999;
                Opponent closest = null;
                for(Opponent o: game.getOpponentPlayers()) {
                    double tempDistance = o.getPosition().distance(game.getBall().getPosition());
                    if(tempDistance < minDistance) {
                        minDistance = tempDistance;
                        closest = o;
                    }
                }
                if(closest != null) {
                    closest.select();
                } else {
                    System.out.println("Error on finding closest opponent");
                }
            } else if(game.getBall().getOwner() == null) { // Ball is on pitch
                double minDistance = 999999999;
                Opponent closest = null;
                for(Opponent o: game.getOpponentPlayers()) {
                    double tempDistance = o.getPosition().distance(game.getBall().getPosition());
                    if(tempDistance < minDistance) {
                        minDistance = tempDistance;
                        closest = o;
                    }
                }
                if(closest != null) {
                    closest.select();
                } else {
                    System.out.println("Error on finding closest opponent");
                }
            } else if(game.getBall().getOwner() != null) { // Ball is on opponent team
                //
            } else { // TODO Unhandled situations

            }
        }
    }
}