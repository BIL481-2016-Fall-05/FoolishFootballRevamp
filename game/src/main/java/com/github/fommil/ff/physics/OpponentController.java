package com.github.fommil.ff.physics;

import com.github.fommil.ff.Pitch;

/**
 * The logic for automatic controlling a {@link Opponent} - users do not get to directly
 * control the opponent.
 *
 * @author Doga Can Yanikoglu
 */
class OpponentController {

	private final Pitch pitch;

	private final Position topDefault;

	private final Position bottomDefault;

	private final Position topGoal;

	private final Position bottomGoal;

	public OpponentController(Pitch pitch) {
		this.pitch = pitch;
		topGoal = pitch.getGoalTop();
		topDefault = new Position(topGoal.x, topGoal.y - 5, topGoal.z);
		bottomGoal = pitch.getGoalBottom();
		bottomDefault = new Position(bottomGoal.x, bottomGoal.y + 5, bottomGoal.z);
	}

	public void autoPilot(Opponent p) {
		if(p.isSelected && p.targetPosition != null) {
			p.autoPilot(p.targetPosition);
		}
	}
}
