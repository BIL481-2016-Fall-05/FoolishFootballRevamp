package com.github.fommil.ff.ui;

import java.awt.Color;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
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
		Team team = new Team();
		team.setHomeKit(new Colours(Color.RED, Color.RED, Color.BLUE, Color.RED));
		assertEquals(team.getHomeKit().getPrimary(),
				((new Colours(Color.RED, Color.RED, Color.BLUE, Color.RED).getPrimary())));
	}

	@Test
	public void verifyModifiedAwayKit() {
		Team team = new Team();
		team.setAwayKit(new Colours(Color.RED, Color.RED, Color.BLUE, Color.RED));
		assertEquals(team.getAwayKit().getPrimary(),
				((new Colours(Color.RED, Color.RED, Color.BLUE, Color.RED)).getPrimary()));
	}

	@Test
	public void verifyTactics() {
		Team team = new Team();
		Tactics tactices = new Tactics("One");
		team.setCurrentTactics(tactices);
		assertEquals(team.getCurrentTactics(), (tactices));
	}

}
