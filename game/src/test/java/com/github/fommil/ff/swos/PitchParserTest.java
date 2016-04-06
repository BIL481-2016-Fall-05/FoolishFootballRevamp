package com.github.fommil.ff.swos;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;

import org.junit.Test;

import com.github.fommil.ff.swos.PitchParser;

public class PitchParserTest {

	@Test
	public void verifyNumberOfPitchesCreated() throws Exception {

		PitchParser.main(null);

		File folder = new File("data/sprites");

		FilenameFilter pitchFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().contains("pitch");
			}
		};

		String[] filenames = folder.list(pitchFilter);

		assertEquals(filenames.length, 6);
	}

}
