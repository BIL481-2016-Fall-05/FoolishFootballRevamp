package com.github.fommil.ff.swos;

import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.Test;

import com.github.fommil.ff.Main;
import com.github.fommil.ff.swos.TacticsParser;;

public class TacticsParserTest {

	@Test
	public void getSwosTacticsReturnCount() throws IOException {
		assertEquals(TacticsParser.getSwosTactics(Main.SWOS).size(), 12);
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseTacs() throws IOException {
		TacticsParser tp = new TacticsParser();
		byte[] b = new byte[369];
		assertEquals(tp.parseTacs(b).getName().length(), 0);
	}
}
