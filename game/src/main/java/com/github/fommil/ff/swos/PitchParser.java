/*
 * Copyright Samuel Halliday 2009
 * 
 * This file is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this file.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.fommil.ff.swos;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.github.fommil.ff.Main;
import com.google.common.base.Preconditions;

/**
 * The SWOS 96/97 directory contains {@code PITCH} files containing the graphics of the
 * game pitches, this file parses them into {@code Image} files. Each pitch is stored
 * across two files:
 * <ul>
 * <li>BLK - contains pattern blocks: 16x16 pixels, 256 bit indexed.</li>
 * <li>DAT - contains the ordering of the patterns for a pitch.</li>
 * </ul>
 * <p>
 * The palette was extracted by Zlatko Karakas (author of SWOS Picture Editor).
 * Every colour has its darkened counterpart (colour + 128). Note that although
 * the {@code PALA.DAT} file contains a palette that is apparently 11 blocks of 48
 * ASCII hex codes, it is not clear if it encodes the palette used for the pitch.
 * <p>
 * Colour 11 is converted into the primary colour of the team. Colour 10 is
 * converted to the secondary colour or shorts. Only 42 patterns have this colour
 * converting property, and those are mostly crowd patterns.
 * <p>
 * Pitch types are achieved by palette modification of entries
 * 0, 7, 9, 78, 79, 80, 81, 106 and 107
 * denoting visual changes according to conditions.
 * TODO: the exact process of weather-modification is unclear.
 * <p>
 * Patterns 1 to 24 are animated. Patterns 1..12 are upper crowd, and patterns 13..24
 * are lower crowd. Odd indices are pictures when not moving, and even indices are
 * pictures when animated (jumping, cheering).
 * <p>
 * Pattern 0 is usually empty pattern (except in training pitch).
 * <p>
 * SWOS can only read a maximum of 296 unique patterns and pitches are
 * comprised of 42 x 53 patterns.
 *
 * @author Samuel Halliday
 */
public class PitchParser {

	private static final Logger log = Logger.getLogger(PitchParser.class.getName());

	/**
	 * @param swos
	 * @param i
	 * @return
	 * @throws IOException
	 */
	public static final BufferedImage getPitch(File swos, int i) throws IOException {
		File blkFile = new File(swos.getPath() + "/PITCH" + i + ".BLK");
		File datFile = new File(swos.getPath() + "/PITCH" + i + ".DAT");
		FileInputStream blk = new FileInputStream(blkFile);
		FileInputStream dat = new FileInputStream(datFile);
		PitchParser parser = new PitchParser();
		return parser.extractPitch(blk, dat);
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static final void main(String[] args) throws Exception {
		for (int i = 1; i <= 6; i++) {
			ImageIO.write(getPitch(Main.SWOS, i), "png", new File("data/sprites/pitch" + i + ".png"));
		}
	}

	/**
	 * @param blk
	 * @param dat
	 * @return
	 * @throws IOException
	 */
	public BufferedImage extractPitch(InputStream blk, InputStream dat) throws IOException {
		Preconditions.checkNotNull(blk);
		Preconditions.checkNotNull(dat);
		List<Color> palette = SwosUtils.getGamePalette();

		// TODO: perhaps we should use an indexed BufferedImage instead of RGB
		try {
			int HEIGHT = 55;
			int WIDTH = 42;
			// pattern index, [0][0] is top left.
			int[][] pattern = new int[WIDTH][HEIGHT];
			for (int j = HEIGHT - 1; j >= 0; j--) {
				for (int i = WIDTH - 1; i >= 0; i--) {
					int a = dat.read(); // ??
					pattern[WIDTH - i - 1][HEIGHT - j - 1] = dat.read();
					int b = dat.read(); // ??
					int c = dat.read(); // ??
					Preconditions.checkArgument(a != -1 && b != -1 && c != -1 && pattern[i][j] != -1, "bad DAT file");
					if (a != 0 || b != 0 || c != 0)
						log.info("Strange bytes in pitch DAT file: " + a + " " + b + " " + c);
				}
			}
			Preconditions.checkArgument(dat.read() == -1, "bad DAT file");
			BufferedImage[] patterns = new BufferedImage[296];
			outer:
			for (int i = 0; i < 296; i++) {
				patterns[i] = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
				for (int y = 0; y < 16; y++) {
					for (int x = 0; x < 16; x++) {
						int read = blk.read();
						if (read == -1) {
							Preconditions.checkArgument(x == 0 && y == 0, "bad BLK file");
							break outer;
						}
						Color c = palette.get(read);
						patterns[i].setRGB(x, y, c.getRGB());
					}
				}
			}

			BufferedImage image = new BufferedImage(16 * WIDTH, 16 * HEIGHT, BufferedImage.TYPE_INT_RGB);
			Graphics g = image.getGraphics();
			for (int i = 0; i < WIDTH; i++) {
				for (int j = 0; j < HEIGHT; j++) {
					int p = pattern[i][j];
					g.drawImage(patterns[p], i * 16, j * 16, null);
				}
			}
			return image;
		} finally {
			blk.close();
			dat.close();
		}
	}
}
