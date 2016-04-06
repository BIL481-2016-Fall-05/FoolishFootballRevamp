package com.github.fommil.ff.swos;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.fommil.ff.swos.SwosUtils;

public class SwosUtilsTest {

	@BeforeClass
	public static void setUp() throws IOException {
		SwosUtils.main(null);
	}

	@Test
	public void verifyGamePalImage() {

		File folder = new File("data/sprites");

		// this is the filter
		FilenameFilter pitchFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().contains("gamepal");
			}
		};

		// list the files in the data folder
		String[] filenames = folder.list(pitchFilter);

		assertEquals(filenames.length, 1);

	}

	@Test
	public void verifyPalImage() {

		File folder = new File("data/sprites");

		// this is the filter
		FilenameFilter pitchFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().contentEquals("pal.png");
			}
		};

		// list the files in the data folder
		String[] filenames = folder.list(pitchFilter);

		assertEquals(filenames.length, 1);

	}

	@Test
	public void fileToByte() throws IOException {

		byte[] bytes = SwosUtils.getBytes(new File("data/sprites/gamepal.png"));

		assertEquals(bytes.length, 623);

	}

}
