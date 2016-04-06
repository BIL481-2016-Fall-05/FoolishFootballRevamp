package com.github.fommil.ff.physics;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Ignore;
import org.junit.Test;

import com.github.fommil.ff.Direction;
import com.github.fommil.ff.PlayerStats;

public class GoalkeeperTest {

	@Test
	public void createGoalKeeper() {
		Goalkeeper gk = new DummyPhysics().createGoalkeeper(1, new PlayerStats());
		assertEquals(gk.getShirt(), 1);
	}

	@Test
	public void goalKeeperState_DIVE_START() {

		Goalkeeper gk = new DummyPhysics().createGoalkeeper(1, new PlayerStats());

		gk.body.setPosition(0.5, 0.5, 0.5);
		gk.body.setLinearVel(5, 5, 5);

		assertEquals(gk.getGkState(), Goalkeeper.GoalkeeperState.DIVE_START);
	}

	@Test
	public void goalKeeperState_DIVE_MID() {
		Goalkeeper gk = new DummyPhysics().createGoalkeeper(1, new PlayerStats());

		gk.body.setPosition(1.0, 1.0, 1.0);
		gk.body.setLinearVel(2, 2, 2);

		assertEquals(gk.getGkState(), Goalkeeper.GoalkeeperState.DIVE_MID);
	}

	@Test
	public void goalKeeperState_DIVE_PEAK() {
		Goalkeeper gk = new DummyPhysics().createGoalkeeper(1, new PlayerStats());

		gk.body.setPosition(3.0, 3.0, 3.0);
		gk.body.setLinearVel(2, 2, 2);

		assertEquals(gk.getGkState(), Goalkeeper.GoalkeeperState.DIVE_PEAK);
	}

	@Test
	public void goalKeeperState_FALL_END() {
		Goalkeeper gk = new DummyPhysics().createGoalkeeper(1, new PlayerStats());

		gk.body.setPosition(0.5, 0.5, 0.5);
		gk.body.setLinearVel(0.5, 0.3, 0.2);

		assertEquals(gk.getGkState(), Goalkeeper.GoalkeeperState.FALL_END);
	}

	@Test
	public void goalKeeperState_FALL_MID() {
		Goalkeeper gk = new DummyPhysics().createGoalkeeper(1, new PlayerStats());

		gk.body.setPosition(1.0, 1.0, 1.0);
		gk.body.setLinearVel(0.5, 0.3, 0.2);

		assertEquals(gk.getGkState(), Goalkeeper.GoalkeeperState.FALL_MID);
	}

	@Test
	public void goalKeeperState_FALL_START() {
		Goalkeeper gk = new DummyPhysics().createGoalkeeper(1, new PlayerStats());

		gk.body.setPosition(3.0, 3.0, 3.0);
		gk.body.setLinearVel(0.5, 0.3, 0.2);

		assertEquals(gk.getGkState(), Goalkeeper.GoalkeeperState.FALL_START);
	}

	@Test
	public void diveGoalKeeper() {
		Goalkeeper gk = new DummyPhysics().createGoalkeeper(1, new PlayerStats());

		gk.dive(Direction.EAST);
		assertEquals(gk.getVelocity(), new Velocity(10.0, 0.0, 5.0));

		gk.dive(Direction.WEST);
		assertEquals(gk.getVelocity(), new Velocity(10.0, 0.0, 5.0));

	}

	@Test
	public void getTolerance() {
		Goalkeeper gk = new DummyPhysics().createGoalkeeper(1, new PlayerStats());
		assertTrue(gk.getAutoPilotTolerance() - 0.1 == 0);
	}

}
