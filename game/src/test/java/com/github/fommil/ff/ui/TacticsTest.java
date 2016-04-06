package com.github.fommil.ff.ui;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.fommil.ff.Direction;
import com.github.fommil.ff.Pitch;
import com.github.fommil.ff.Tactics;
import com.github.fommil.ff.physics.Position;

public class TacticsTest {

	Position position;
	Pitch pitch;
	Tactics.BallZone ball;
	Tactics.PlayerZone player;
	Tactics tactices;

	@Test
	public void verifyBallPosition() {
		Tactics.BallZone ball = new Tactics.BallZone(2, 2);
		assertEquals(ball.toString(), ("(2, 2)"));
	}

	@Test
	public void verifyBallPositionInPitchTest() {
		pitch = new Pitch();
		position = new Position(2, 2, 2);
		ball = new Tactics.BallZone(position, pitch);
		assertEquals(ball.toString(), ("(4, 0)"));
	}

	@Test
	public void verifyPlayerPositionTest() {
		player = new Tactics.PlayerZone(2, 2);
		assertEquals(player.toString(), ("(2, 2)"));
	}

	@Test
	public void verifyPlayerPositionInPitchTest() {
		player = new Tactics.PlayerZone(2, 2);
		pitch = new Pitch();
		assertEquals(player.getCentre(pitch), (new Position(50.516666666666666, 21.1, 0.0)));
	}

	@Test
	public void verifyPlayerZoneTest() {
		player = new Tactics.PlayerZone(2, 2);
		pitch = new Pitch();
		position = new Position(2, 2, 2);
		ball = new Tactics.BallZone(position, pitch);
		tactices = new Tactics("One");
		tactices.set(ball, 2, player);
		assertEquals(tactices.getZone(ball, 2, Direction.NORTH), (player));
	}

}
