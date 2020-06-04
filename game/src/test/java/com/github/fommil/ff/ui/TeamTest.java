package com.github.fommil.ff.ui;

import static org.junit.Assert.assertEquals;

import java.awt.Color;

import org.junit.Test;

import com.github.fommil.ff.Tactics;
import com.github.fommil.ff.Team;
import com.github.fommil.ff.Team.Colours;

public class TeamTest {

	@Test
	public void verifyColors() {
		Colours colors = new Colours(Color.black, Color.blue, Color.cyan, Color.GREEN);
		assertEquals(colors.getPrimary(), (Color.black));
		assertEquals(colors.getSecondary(), (Color.blue));
		assertEquals(colors.getShorts(), (Color.cyan));
		assertEquals(colors.getSocks(), (Color.GREEN));
	}

	@Test
	public void verifyModifiedHomeKit() {
		Team team = new Team("Test");
		team.setHomeKit(new Colours(Color.RED, Color.RED, Color.BLUE, Color.RED));
		assertEquals(team.getHomeKit().getPrimary(),
				((new Colours(Color.RED, Color.RED, Color.BLUE, Color.RED).getPrimary())));
	}

	@Test
	public void verifyModifiedAwayKit() {
		Team team = new Team("Test");
		team.setAwayKit(new Colours(Color.RED, Color.RED, Color.BLUE, Color.RED));
		assertEquals(team.getAwayKit().getPrimary(),
				((new Colours(Color.RED, Color.RED, Color.BLUE, Color.RED)).getPrimary()));
	}

	@Test
	public void verifyTactics() {
		Team team = new Team("Test");
		Tactics tactices = new Tactics("One");
		team.setCurrentTactics(tactices);
		assertEquals(team.getCurrentTactics(), (tactices));
	}

}
