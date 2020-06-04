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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import com.google.common.base.Preconditions;

/**
 * Convenience methods for dealing with SWOS data files.
 *
 * @author Samuel Halliday
 */
public final class SwosUtils {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static final void main(String[] args) throws IOException {
		BufferedImage gamePal = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
		BufferedImage pal = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				Color c1 = SwosUtils.getGamePalette().get(j * 16 + i);
				Color c2 = SwosUtils.getPalette().get(j * 16 + i);
				gamePal.setRGB(i, j, c1.getRGB());
				pal.setRGB(i, j, c2.getRGB());
			}
		}
		ImageIO.write(gamePal, "png", new File("data/sprites/gamepal.png"));
		ImageIO.write(pal, "png", new File("data/sprites/pal.png"));
	}

	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static byte[] getBytes(File file) throws IOException {
		Preconditions.checkNotNull(file);
		Preconditions.checkArgument(file.exists());

		int size = (int) file.length();
		byte[] bytes = new byte[size];
		InputStream in = new FileInputStream(file);

		try {
			int response = in.read(bytes);
			Preconditions.checkState(response == size);
			return bytes;
		} finally {
			in.close();
		}
	}

	/**
	 * Treat a {@code byte} as unsigned and convert to an {@code int}.
	 *
	 * @param b
	 * @return
	 */
	public static int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
	}

	/**
	 * Treat an array of {@code byte}s as unsigned and convert to an {@code int} array.
	 *
	 * @param bs
	 * @return
	 */
	public static int[] unsignedBytesToInts(byte[] bs) {
		Preconditions.checkNotNull(bs);
		int[] ints = new int[bs.length];
		for (int i = 0; i < ints.length; i++) {
			ints[i] = unsignedByteToInt(bs[i]);
		}
		return ints;
	}

	/**
	 * Convert an array of {@code ints}s to an array of {@code byte}s which are treated as unsigned.
	 *
	 * @param sprite
	 * @return
	 */
	public static byte[] intsToUnsignedBytes(int[] sprite) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	/* lazy initialisation */

	private static volatile List<Color> PAL_GAME;

	private static volatile List<Color> PAL;

	// TODO: do not depend on hard-coded palette - read from the SWOS data files
	private static final int[] PAL_RAW = new int[]{
		0, 0, 36, 180, 180, 180, 252, 252, 252, 0, 0, 0, 108, 36, 0, 180, 72, 0,
		252, 108, 0, 108, 108, 108, 36, 36, 36, 72, 72, 72, 252, 0, 0, 0, 0, 252,
		108, 0, 36, 144, 144, 252, 36, 144, 0, 252, 252, 0, 144, 120, 84, 156, 132,
		92, 168, 140, 104, 180, 152, 116, 192, 164, 128, 204, 176, 140, 216, 192,
		152, 228, 204, 168, 228, 208, 176, 232, 212, 188, 236, 220, 196, 240, 224,
		208, 240, 232, 216, 244, 236, 228, 248, 244, 240, 252, 252, 252, 0, 0, 0,
		0, 0, 8, 0, 0, 16, 0, 0, 24, 0, 0, 32, 0, 0, 40, 0, 0, 48, 0, 0, 56, 0, 0,
		64, 0, 0, 72, 0, 0, 80, 0, 0, 88, 0, 0, 96, 0, 0, 104, 0, 0, 112, 0, 0,
		120, 0, 0, 132, 0, 0, 140, 0, 0, 148, 0, 0, 156, 0, 0, 164, 0, 0, 172, 0,
		0, 180, 0, 0, 188, 0, 0, 196, 0, 0, 204, 0, 0, 212, 0, 0, 220, 0, 0, 228,
		0, 0, 236, 0, 0, 244, 0, 0, 252, 108, 36, 0, 112, 36, 0, 116, 40, 0, 120,
		44, 0, 124, 48, 0, 128, 52, 0, 136, 56, 0, 140, 56, 0, 144, 60, 0, 148, 64,
		0, 152, 68, 0, 156, 72, 0, 164, 76, 0, 168, 80, 0, 172, 88, 0, 176, 92, 0,
		180, 96, 0, 184, 100, 0, 192, 104, 0, 196, 112, 0, 200, 116, 0, 204, 120,
		0, 208, 124, 0, 212, 132, 0, 216, 136, 0, 224, 144, 0, 228, 148, 0, 232,
		152, 0, 236, 160, 0, 240, 168, 0, 244, 172, 0, 252, 180, 0, 36, 36, 36, 40,
		40, 40, 48, 48, 48, 56, 52, 52, 60, 60, 60, 68, 64, 64, 76, 72, 72, 80, 76,
		76, 88, 80, 80, 96, 88, 88, 104, 92, 92, 108, 96, 96, 116, 104, 104, 124,
		108, 108, 128, 112, 112, 136, 116, 116, 144, 124, 124, 148, 128, 128, 156,
		132, 132, 164, 136, 136, 168, 140, 140, 176, 144, 144, 184, 148, 148, 188,
		152, 152, 196, 156, 156, 204, 160, 160, 212, 164, 164, 216, 168, 168, 224,
		168, 168, 232, 172, 172, 236, 176, 176, 244, 180, 180, 252, 0, 0, 252, 0,
		0, 252, 4, 4, 252, 8, 8, 252, 8, 8, 252, 12, 12, 252, 16, 16, 252, 20, 20,
		252, 24, 24, 252, 28, 28, 252, 32, 32, 252, 36, 36, 252, 40, 40, 252, 44,
		44, 252, 48, 48, 252, 52, 52, 252, 56, 56, 252, 60, 60, 252, 64, 64, 252,
		68, 68, 252, 72, 72, 252, 76, 76, 252, 80, 80, 252, 84, 84, 252, 88, 88,
		252, 92, 92, 252, 96, 96, 252, 100, 100, 252, 104, 104, 252, 108, 108, 252,
		112, 112, 252, 116, 116, 108, 0, 36, 112, 0, 44, 116, 0, 52, 120, 4, 64,
		124, 4, 72, 128, 8, 84, 136, 12, 92, 140, 12, 104, 144, 16, 116, 148, 20,
		128, 152, 24, 136, 156, 28, 148, 164, 32, 160, 160, 36, 168, 160, 40, 172,
		156, 44, 176, 152, 48, 180, 152, 56, 184, 152, 60, 192, 148, 64, 196, 144,
		72, 200, 144, 76, 204, 144, 84, 208, 140, 88, 212, 140, 96, 216, 140, 100,
		224, 140, 108, 228, 140, 112, 232, 140, 120, 236, 140, 128, 240, 140, 136,
		244, 144, 144, 252, 0, 0, 252, 0, 4, 248, 4, 16, 248, 4, 28, 248, 8, 36,
		244, 12, 48, 244, 12, 60, 244, 16, 68, 244, 20, 76, 240, 20, 84, 240, 24,
		96, 240, 28, 104, 236, 32, 112, 236, 32, 116, 236, 36, 124, 236, 40, 132,
		232, 40, 140, 232, 44, 148, 232, 48, 152, 228, 48, 160, 228, 52, 164, 228,
		56, 172, 228, 56, 176, 224, 60, 180, 224, 64, 188, 224, 64, 192, 220, 68,
		196, 220, 68, 200, 220, 72, 204, 216, 76, 208, 216, 76, 212, 216, 80, 216,
		216, 16, 32, 0, 16, 44, 0, 20, 56, 0, 24, 68, 0, 24, 80, 0, 28, 88, 0, 28,
		100, 0, 32, 112, 0, 32, 124, 0, 36, 136, 0, 36, 144, 0, 48, 152, 0, 60,
		156, 0, 68, 160, 0, 80, 168, 0, 88, 172, 0, 100, 176, 0, 108, 180, 0, 120,
		188, 0, 132, 192, 0, 140, 196, 0, 152, 204, 0, 160, 208, 0, 172, 212, 0,
		180, 216, 0, 192, 224, 0, 204, 228, 0, 212, 232, 0, 224, 240, 0, 232, 244,
		0, 244, 248, 0, 252, 252, 0
	};

	private static final int[] PAL_GAME_RAW = {
		112, 80, 0, 152, 152, 152, 252, 252, 252, 0, 0, 0, 100, 32, 0, 184, 68, 0,
		252, 100, 0, 96, 80, 0, 0, 32, 0, 80, 80, 0, 252, 0, 0, 0, 0, 252, 100, 0,
		32, 152, 152, 252, 0, 220, 0, 252, 252, 0, 156, 200, 0, 172, 172, 172, 252,
		252, 252, 24, 24, 24, 124, 36, 0, 208, 72, 0, 252, 128, 52, 132, 200, 0, 0,
		56, 0, 200, 196, 0, 252, 108, 108, 120, 124, 252, 124, 0, 40, 188, 188,
		252, 0, 240, 0, 252, 252, 188, 108, 136, 0, 116, 116, 116, 208, 208, 208,
		0, 0, 0, 72, 20, 0, 120, 36, 0, 184, 64, 0, 88, 132, 0, 0, 4, 0, 132, 128,
		0, 204, 0, 0, 0, 0, 204, 60, 0, 20, 120, 124, 204, 0, 172, 0, 204, 200, 0,
		0, 0, 0, 20, 20, 20, 44, 44, 44, 68, 68, 68, 92, 92, 92, 116, 116, 116,
		136, 136, 136, 160, 160, 160, 184, 184, 184, 208, 208, 208, 228, 228, 228,
		252, 252, 252, 252, 216, 188, 252, 176, 128, 252, 140, 64, 252, 100, 0,
		228, 88, 0, 200, 76, 0, 176, 64, 0, 152, 52, 0, 128, 44, 0, 100, 32, 0, 0,
		0, 0, 0, 0, 112, 0, 0, 128, 0, 0, 148, 0, 0, 168, 44, 44, 196, 96, 104,
		224, 148, 152, 228, 200, 184, 164, 176, 120, 0, 68, 44, 0, 92, 56, 0, 232,
		232, 232, 224, 212, 212, 220, 196, 196, 216, 184, 184, 212, 168, 168, 208,
		156, 156, 204, 140, 140, 176, 120, 120, 144, 100, 100, 116, 80, 80, 88, 60,
		60, 56, 40, 40, 28, 20, 20, 0, 0, 0, 252, 232, 208, 232, 208, 176, 212,
		188, 152, 196, 168, 128, 176, 148, 108, 148, 124, 92, 124, 100, 76, 96, 80,
		56, 84, 68, 52, 36, 28, 20, 84, 60, 8, 184, 156, 120, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 152, 252, 80, 80, 80, 112, 112, 112, 32, 32, 32, 240, 0,
		0, 240, 112, 112, 128, 128, 128, 144, 144, 144, 160, 160, 160, 176, 176,
		176, 240, 176, 176, 192, 192, 192, 208, 208, 208, 224, 224, 224, 240, 240,
		240, 252, 76, 76, 52, 36, 0, 92, 92, 92, 192, 192, 192, 0, 0, 0, 40, 12, 0,
		124, 44, 0, 192, 72, 0, 36, 28, 0, 0, 0, 0, 20, 20, 0, 192, 0, 0, 0, 0,
		192, 0, 0, 0, 116, 116, 192, 0, 160, 0, 192, 192, 0, 108, 140, 0, 112, 112,
		112, 192, 192, 192, 0, 0, 0, 64, 16, 0, 148, 48, 0, 192, 96, 40, 92, 140,
		0, 0, 0, 0, 140, 136, 0, 192, 80, 80, 92, 92, 192, 0, 0, 0, 144, 144, 192,
		0, 180, 0, 192, 192, 144, 60, 76, 0, 56, 56, 56, 148, 148, 148, 0, 0, 0,
		12, 0, 0, 60, 16, 0, 124, 40, 0, 44, 72, 0, 0, 0, 0, 72, 68, 0, 144, 0, 0,
		0, 0, 144, 0, 0, 0, 84, 84, 144, 0, 112, 0, 144, 140, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 8, 8, 8, 32, 32, 32, 56, 56, 56, 76, 76, 76, 100, 100, 100, 124,
		124, 124, 148, 148, 148, 168, 168, 168, 192, 192, 192, 192, 164, 144, 192,
		132, 96, 192, 104, 48, 192, 72, 0, 168, 60, 0, 140, 52, 0, 116, 40, 0, 92,
		28, 0, 68, 20, 0, 40, 12, 0, 0, 0, 0, 0, 0, 52, 0, 0, 68, 0, 0, 88, 0, 0,
		108, 28, 28, 136, 68, 76, 164, 108, 112, 168, 140, 128, 112, 116, 76, 0, 8,
		4, 0, 32, 16, 0, 172, 172, 172, 164, 152, 152, 160, 144, 144, 156, 132,
		132, 152, 120, 120, 148, 108, 108, 144, 96, 96, 116, 80, 80, 84, 56, 56,
		56, 36, 36, 28, 16, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 192, 176, 156, 172, 152,
		128, 152, 132, 108, 136, 116, 88, 116, 96, 68, 88, 72, 52, 64, 48, 36, 36,
		28, 20, 24, 16, 12, 0, 0, 0, 24, 16, 0, 124, 104, 80, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 112, 192, 20, 20, 20, 52, 52, 52, 0, 0, 0, 180, 0, 0,
		180, 84, 84, 68, 68, 68, 84, 84, 84, 100, 100, 100, 116, 116, 116, 180,
		132, 132, 132, 132, 132, 148, 148, 148, 164, 164, 164, 180, 180, 180, 192,
		56, 56
	};

	/**
	 * @return the "game" palette - used by the pitch files and rotating R.
	 */
	public static List<Color> getGamePalette() {
		if (PAL_GAME != null)
			return PAL_GAME;
		return PAL_GAME = buildPalette(PAL_GAME_RAW);
	}

	/**
	 * @return the standard palette.
	 */
	public static List<Color> getPalette() {
		if (PAL != null)
			return PAL;
		return PAL = buildPalette(PAL_RAW);
	}

	private static List<Color> buildPalette(int[] raw) {
		Preconditions.checkArgument(raw.length == 256 * 3);
		Color[] pal = new Color[256];
		for (int i = 0; i < pal.length; i++) {
			pal[i] = new Color(raw[i * 3], raw[i * 3 + 1], raw[i * 3 + 2]);
		}
		return Collections.unmodifiableList(Arrays.asList(pal));
	}
}
