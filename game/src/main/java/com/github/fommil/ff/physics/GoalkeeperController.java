/*
 * Copyright Samuel Halliday 2010
 * 
 * This file is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this file.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.fommil.ff.physics;

import org.ode4j.math.DVector3;

import com.github.fommil.ff.Direction;
import com.github.fommil.ff.Pitch;
import com.google.common.base.Preconditions;

/**
 * The logic for automatic controlling a {@link Goalkeeper} - users do not get to directly
 * control the goalkeeper.
 *
 * @author Samuel Halliday
 */
class GoalkeeperController {

	private final Pitch pitch;

	private final Position topDefault;

	private final Position bottomDefault;

	private final Position topGoal;

	private final Position bottomGoal;

	public GoalkeeperController(Pitch pitch) {
		this.pitch = pitch;
		topGoal = pitch.getGoalTop();
		topDefault = new Position(topGoal.x, topGoal.y - 5, topGoal.z);
		bottomGoal = pitch.getGoalBottom();
		bottomDefault = new Position(bottomGoal.x, bottomGoal.y + 5, bottomGoal.z);
	}

	public void autoPilot(Goalkeeper p, Ball ball) {
		Preconditions.checkNotNull(p);
		Preconditions.checkNotNull(ball);
		Position target;

		Position position = p.getPosition();
		Position ballPosition = ball.getPosition();
		double ballSpeed = ball.getVelocity().speed();
		double distance = ballPosition.distance(position);
		if (p.getOpponent() == Direction.SOUTH) {
			target = topDefault;
		} else {
			target = bottomDefault;
		}
		if (distance > 15 || Math.abs(target.y - ballPosition.y) > 10) {
			// step 1: stand in a default position
			p.autoPilot(target);
			return;
		} else if (distance > 5) {
			// step 2: stand between the ball and the goal
			DVector3 b = ballPosition.toDVector();
			double xOffset = (b.get0() - target.x) / 2.0;
			double yOffset = Math.abs(b.get1() - topGoal.y) / 3.0;
			if (p.getOpponent() == Direction.SOUTH) {
				target = new Position(topGoal.x + xOffset, topGoal.y - yOffset, 0);
			} else {
				target = new Position(bottomGoal.x + xOffset, bottomGoal.y + yOffset, 0);
			}
			p.autoPilot(target);
		} else if (distance > 1 && ballSpeed < 10) {
			// step 3: go for the ball
			p.autoPilot(ballPosition);
		} else if (ballSpeed > 8) {
			// step 4: dive!
			DVector3 s = position.toDVector();

			Velocity ballVelocity = ball.getVelocity();
			DVector3 goingTo = ballPosition.toDVector().add(ballVelocity.toDVector());

			// TODO: smarter dive logic: go to where the ball will be at goalie's y position

			DVector3 diff = goingTo.sub(s);
			if (diff.get0() > 0.5)
				p.dive(Direction.EAST);
			else if (diff.get0() < -0.5)
				p.dive(Direction.WEST);
			else
				p.dive(null);
		}
	}
}
