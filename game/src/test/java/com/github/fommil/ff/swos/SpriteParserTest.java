package com.github.fommil.ff.swos;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.junit.Test;

public class SpriteParserTest {

	@Test
	public void verifyNumberOfSprites() throws IOException {
		SpriteParser.main(null);

		File folder = new File("data/sprites");

		// this is the filter
		FilenameFilter pitchFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().contains("charset");
			}
		};

		// list the files in the data folder
		String[] filenames = folder.list(pitchFilter);

		assertEquals(filenames.length, 227);
	}

}
