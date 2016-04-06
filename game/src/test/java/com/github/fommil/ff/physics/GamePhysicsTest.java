package com.github.fommil.ff.physics;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import com.github.fommil.ff.Main;
import com.github.fommil.ff.Pitch;
import com.github.fommil.ff.Tactics;
import com.github.fommil.ff.Team;
import com.github.fommil.ff.swos.TacticsParser;

public class GamePhysicsTest {

	@Test
	public void testTeamSizeEqual() throws IOException {
		Map<String, Tactics> swosTactics = TacticsParser.getSwosTactics(Main.SWOS);
		Team a = new Team();

		a.setCurrentTactics(swosTactics.get("442"));
		Team b = new Team();

		b.setCurrentTactics(swosTactics.get("433"));
		GamePhysics gp = new GamePhysics(a, b, new Pitch());

		a = gp.getTeamA();
		b = gp.getTeamB();

		assertEquals(b.getPlayers().size(), a.getPlayers().size());
	}

}
