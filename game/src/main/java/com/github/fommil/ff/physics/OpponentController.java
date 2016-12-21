package com.github.fommil.ff.physics;

/**
 * The logic for automatic controlling a {@link Opponent} - users do not get to directly
 * control the opponent.
 *
 * @author Doga Can Yanikoglu
 */
class OpponentController {
	public void autoPilot(Opponent p) {
		if(p.isSelected() && !p.getAssignments().isEmpty() && !p.getAssignments().peek().getTargets().isEmpty()) {
			p.autoPilot(p.getAssignments().peek().getTargets().peek());
		}
	}
}