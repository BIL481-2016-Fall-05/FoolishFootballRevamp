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

import org.junit.Test;
import org.ode4j.math.DVector3;
import org.ode4j.ode.DBody;

import com.github.fommil.ff.Pitch;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Samuel Halliday
 */
public class BallTest {
	
	private static final double dt = 0.01;

	private static final double EPSILON = 0.0001;

	private final Pitch pitch = new Pitch();

	private final Position centre = pitch.getCentre();

	interface Tester {

		void test(Position s, Velocity v);
	}

	void testHelper(Position position, Velocity velocity, Tester stepTest) {
		DummyPhysics physics = new DummyPhysics();
		Ball ball = physics.createBall();
		ball.setPosition(position);
		ball.setVelocity(velocity);
		for (int i = 0; i < 1000; i++) {
			physics.step(dt);
			stepTest.test(ball.getPosition(), ball.getVelocity());
		}
		physics.clean();
	}
	
	@Test
	public void testNoKick() {
		final Velocity velocity = new Velocity(0, 0, 0);
		testHelper(centre, velocity, new Tester() {

			@Override
			public void test(Position s, Velocity v) {
				assertEquals(centre.x, s.x, EPSILON);
				assertEquals(centre.y, s.y, EPSILON);
				assertEquals(centre.z, s.z, EPSILON);
			}
		});
	}

	@Test
	public void testGroundKicks() {
		{
			final Velocity velocity = new Velocity(10, 0, 0); // right
			testHelper(centre, velocity, new Tester() {

				@Override
				public void test(Position s, Velocity v) {
					assertTrue(centre.x < s.x);
					assertEquals(centre.y,s.y,EPSILON);
				}
			});
		}
		{
			final Velocity velocity = new Velocity(0, -10, 0); // down
			testHelper(centre, velocity, new Tester() {

				@Override
				public void test(Position s, Velocity v) {
					assertTrue(centre.y > s.y);
					assertEquals(centre.x, s.x,EPSILON);
				}
			});
		}
		{
			final Velocity velocity = new Velocity(-10, 0, 0); // left
			testHelper(centre, velocity, new Tester() {

				@Override
				public void test(Position s, Velocity v) {
					assertTrue(centre.x > s.x);
					assertEquals(centre.y, s.y,EPSILON);
				}
			});
		}
		{
			final Velocity velocity = new Velocity(0, 10, 0); // up
			testHelper(centre, velocity, new Tester() {

				@Override
				public void test(Position s, Velocity v) {
					assertTrue(centre.y < s.y);
					assertEquals(centre.x, s.x,EPSILON);
				}
			});
		}
		{
			final Velocity velocity = new Velocity(10, -10, 0); // down right
			testHelper(centre, velocity, new Tester() {

				@Override
				public void test(Position s, Velocity v) {
					assertTrue(centre.x < s.x);
					assertTrue(centre.y > s.y);
				}
			});
		}
		{
			final Velocity velocity = new Velocity(-10, 10, 0); // up left
			testHelper(centre, velocity, new Tester() {

				@Override
				public void test(Position s, Velocity v) {
					assertTrue(centre.x > s.x);
					assertTrue(centre.y < s.y);
				}
			});
		}
		{
			final Velocity velocity = new Velocity(10, 10, 0); // up right
			testHelper(centre, velocity, new Tester() {

				@Override
				public void test(Position s, Velocity v) {
					assertTrue(centre.x < s.x);
					assertTrue(centre.y < s.y);
				}
			});
		}
		{
			final Velocity velocity = new Velocity(-10, -10, 0); // down left
			testHelper(centre, velocity, new Tester() {

				@Override
				public void test(Position s, Velocity v) {
					assertTrue(centre.x > s.x);
					assertTrue(centre.y > s.y);
				}
			});
		}
		{
			final Velocity velocity = new Velocity(50, 0, 0); // right, fast
			testHelper(centre, velocity, new Tester() {

				@Override
				public void test(Position s, Velocity v) {
					assertTrue(centre.x < s.x);
					assertEquals(centre.y, s.y,EPSILON);
				}
			});
		}
		{
			final Velocity velocity = new Velocity(0, -50, 0); // down, fast
			testHelper(centre, velocity, new Tester() {

				@Override
				public void test(Position s, Velocity v) {
					assertTrue(centre.y > s.y);
					assertEquals(centre.x, s.x,EPSILON);
				}
			});
		}
	}

	
	@Test
	public void testAfertouch() {
		DummyPhysics physics = new DummyPhysics();
		Ball ball = physics.createBall();
		Collection<Aftertouch> aftertouches = new HashSet<Aftertouch>();
		ball.setAftertouch(true);

		ball.setVelocity(new Velocity(150,150,150));
		
		aftertouches.add(Aftertouch.UP);
		aftertouches.add(Aftertouch.DOWN);
	
		aftertouches.add(Aftertouch.LEFT);
		aftertouches.add(Aftertouch.LEFT);
		

		ball.setAftertouch(aftertouches);
		DBody body =  ball.getGeom().getBody();
				
		assertEquals(body.getForce(), new DVector3(-2.4999999999999996, 2.4999999999999996, 6.0));
		
	}

	
}
