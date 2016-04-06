package com.github.fommil.ff.ui;

import static org.junit.Assert.*;

import org.junit.Test;

import com.github.fommil.ff.Direction;

//class to verify angles return exact direction
public class DirectionsTest {

	@Test
	public void South() {
		assertEquals(Direction.valueOf(Math.PI), Direction.SOUTH);
	}

	@Test
	public void ForMinusAngleSouth() {
		assertEquals(Direction.valueOf(-Math.PI), Direction.SOUTH);
	}

	@Test
	public void NORTH() {
		assertEquals(Direction.valueOf(0), Direction.NORTH);
	}

	@Test
	public void WEST() {
		assertEquals(Direction.valueOf(-Math.PI / 2), Direction.WEST);
	}

	@Test
	public void EAST() {
		assertEquals(Direction.valueOf(Math.PI / 2), Direction.EAST);
	}

	@Test
	public void NORTH_EAST() {
		assertEquals(Direction.valueOf(Math.PI / 4), Direction.NORTH_EAST);
	}

	@Test
	public void NORTH_WEST() {
		assertEquals(Direction.valueOf(-Math.PI / 4), Direction.NORTH_WEST);
	}

	@Test
	public void SOUTH_EAST() {
		assertEquals(Direction.valueOf(3 * Math.PI / 4), Direction.SOUTH_EAST);
	}

	@Test
	public void SOUTH_WEST() {
		assertEquals(Direction.valueOf(-3 * Math.PI / 4), Direction.SOUTH_WEST);
	}

}
