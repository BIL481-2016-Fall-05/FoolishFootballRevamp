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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.logging.Logger;

import org.junit.Test;

import com.github.fommil.ff.Direction;
import com.github.fommil.ff.Pitch;

public class GoalpostTest {

	private static final Logger log = Logger.getLogger(GoalpostTest.class.getName());

	private final Pitch pitch = new Pitch();

	private static final long DT = 100L;

	interface Tester {

		void test(Position s, Velocity v);
	}

	void testHelper(Position position, Velocity velocity, Tester stepTest) {
		DummyPhysics physics = new DummyPhysics();
		physics.createGoalpost(Direction.NORTH);
		physics.createGoalpost(Direction.SOUTH);
		Ball ball = physics.createBall();
		ball.setPosition(position);
		ball.setVelocity(velocity);
		for (int i = 0; i < 1000; i++) {
			physics.doTick(DT);
			stepTest.test(ball.getPosition(), ball.getVelocity());
		}
		physics.clean();
	}

	@Test
	public void createGoalpost_North() {
		Goalpost gp = new DummyPhysics().createGoalpost(Direction.NORTH);
		assertEquals(gp.getFacing(), Direction.NORTH);
	}

	@Test
	public void createGoalpost_South() {
		Goalpost gp = new DummyPhysics().createGoalpost(Direction.SOUTH);
		assertEquals(gp.getFacing(), Direction.SOUTH);
	}

	@Test
	public void newBallIsNotInside() {
		Ball ball = new DummyPhysics().createBall();
		Goalpost gp = new DummyPhysics().createGoalpost(Direction.NORTH);
		assertFalse(gp.isInside(ball));
	}
}
