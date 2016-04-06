package com.github.fommil.ff.ui;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.fommil.ff.Pitch;
import com.github.fommil.ff.physics.Position;

public class PitchTest {

	private final double EPSILON = 0.0001;
	Pitch pitch;

	@Test
	public void getPitchLowerLeft() {
		pitch = new Pitch();
		assertEquals(pitch.getPitchLowerLeft(), new Position(8.1, 11.100000000000001, 0.0));
	}

	@Test
	public void getPitchUpperRight() {
		pitch = new Pitch();
		assertEquals(pitch.getPitchUpperRight(), new Position(59.0, 75.10000000000001, 0.0));
	}

	@Test
	public void getCentre() {
		pitch = new Pitch();
		assertEquals(pitch.getCentre(), new Position(33.6, 43.1, 0.0));
	}

	@Test
	public void getGoalWidth() {
		pitch = new Pitch();
		assertEquals(pitch.getGoalWidth(), 7.1, EPSILON);
	}

	@Test
	public void getGoalHeight() {
		pitch = new Pitch();
		assertEquals(pitch.getGoalHeight(), 3, EPSILON);
	}

	@Test
	public void getGoalDepthTest() {
		pitch = new Pitch();
		assertEquals(pitch.getGoalDepth(), 1.2, EPSILON);
	}

	@Test
	public void getGoalThickness() {
		pitch = new Pitch();
		assertEquals(pitch.getGoalThickness(), .2, EPSILON);
	}

	@Test
	public void getGoalTop() {
		pitch = new Pitch();
		assertEquals(pitch.getGoalTop(), new Position(33.550000000000004, 75.7, 0.0));
	}

	@Test
	public void getGoalBottom() {
		pitch = new Pitch();
		assertEquals(pitch.getGoalBottom(), new Position(33.550000000000004, 10.5, 0.0));

	}

	@Test
	public void getPenaltySpotTop() {
		pitch = new Pitch();
		assertEquals(pitch.getPenaltySpotTop(), new Position(33.6, 69.3, 0.0));

	}

	@Test
	public void getPenaltySpotBottom() {
		pitch = new Pitch();
		assertEquals(pitch.getPenaltySpotBottom(), new Position(33.6, 16.900000000000002, 0.0));
	}
}
