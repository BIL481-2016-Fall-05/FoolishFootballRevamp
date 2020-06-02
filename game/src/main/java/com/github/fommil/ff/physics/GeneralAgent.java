package com.github.fommil.ff.physics;

import com.pigdroid.fommil.util.StepTick;

/**
 * A General Agent Model for Game
 * It selects closest opponent player to ball, and executes a counter to set ball's kicked recently state.
 * @author Doga Can Yanikoglu
 */
public class GeneralAgent extends StepTick {
    private GamePhysics game;
	private BallCounter counter;

    public GeneralAgent(GamePhysics game) {
    	super(250L);
        this.game = game;
        this.counter = new BallCounter();
    }

	@Override
	protected void doStep(long elapsedTimeInMillis) {
		counter.doTick(elapsedTimeInMillis);
        if (game.getSelected().isBallOwner()) { // Ball is on player
            double minDistance = Double.MAX_VALUE;
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

    private class BallCounter extends StepTick {

        protected BallCounter() {
			super(375L);
		}


		@Override
		protected void doStep(long elapsedTimeInMillis) {
            if (game.getBall().isKickedRecently()) {
                game.getBall().setKickStatus(false);
            }
        }

    }

}