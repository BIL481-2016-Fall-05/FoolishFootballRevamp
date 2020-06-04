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
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.github.fommil.ff.Main;
import com.github.fommil.ff.Sprite;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * There are 1,334 sprites in SWOS contained across
 * CHARSET.DAT, SCORE.DAT, TEAM1.DAT, TEAM2.DAT, TEAM3.DAT, GOAL1.DAT, BENCH.DAT. TEAM2.DAT and
 * TEAM3.DAT are mutually exclusive in the original game.
 * <p>
 * Special thanks to Zlatko Karakas for providing the source code to the SWOS Picture Editor
 * which cracked the file format and a high level description of the NASM code, and to
 * {@code Jester01} on {@code ##asm@irc.freenode.net} for helping with initial interpretation of
 * the NASM code.
 * <p>
 * The sprite files consist of a sequence of individual sprites, each one has the following
 * binary structure (in Little Endian, all bytes are unsigned)
 * <ul>
 * <li>{@code int} pointer to start of graphics in file</li>
 * <li>{@code short} - unused</li>
 * <li>{@code short} - unused</li>
 * <li>{@code byte} - unused</li>
 * <li>{@code byte} - unknown</li>
 * <li>{@code short} - width, for visual cropping</li>
 * <li>{@code short} - nlines (height)</li>
 * <li>{@code short} - wquads (number of pixels / 8) in one line</li>
 * <li>{@code short} - the x centre</li>
 * <li>{@code short} - the y centre</li>
 * <li>{@code byte} - unknown</li>
 * <li>{@code byte} - nlines/4</li>
 * <li>{@code short} - sprite id</li>
 * <li>{@code byte[]} - encoded pixel data, {@code 8 * nlines * wquads} length.</li>
 * </ul>
 * The {@code byte[]} part is encoded as follows, with zero padding between each byte in the stream:-
 * <ul>
 * <li>line is divided in 4 "planes", 0, 1, 2 and 3</li>
 * <li>every pixel consists of four bits</li>
 * <li>every pixel consists of 1 bit out of each plane</li>
 * <li>first pixel consists of highest bits from plane 3, 2, 1, 0 respectively</li>
 * <li>second pixel consists of following bits, and so on</li>
 * </ul>
 * <p>
 * Colour number 17 in palette is transparent (??). Also note that colours will be converted according
 * to skin colour of player and dress colour of teams that are playing:
 * <ul>
 * <li>0 - nochange</li>
 * <li>1 - nochange</li>
 * <li>2 - nochange</li>
 * <li>3 - nochange</li>
 * <li>4 - skin color (light shade)</li>
 * <li>5 - skin color (normal shade)</li>
 * <li>6 - skin color (dark shade)</li>
 * <li>7 - turned to zero</li>
 * <li>8 - nochange</li>
 * <li>9 - hair color (normal shade)</li>
 * <li>10 - shirt basic color</li>
 * <li>11 - shirt stripes color (swapped with 10 when vertical stripes)</li>
 * <li>12 - hair color (dark shade)</li>
 * <li>13 - hair color (light shade)</li>
 * <li>14 - shorts color</li>
 * <li>15 - socks color</li>
 * </ul>
 * @author Samuel Halliday
 */
public class SpriteParser {

	private static final Logger log = Logger.getLogger(SpriteParser.class.getName());
	// order to read the sprite files
	// note that sprites 644 – 946 are different in TEAM2.DAT and TEAM3.DAT
	private static final List<String> ORDER = Lists.newArrayList("CHARSET.DAT", "SCORE.DAT", "TEAM1.DAT", "TEAM2.DAT", "TEAM3.DAT", "GOAL1.DAT", "BENCH.DAT");

	/**
	 * Return a list of all the SWOS sprites, indexed by their sprite number.
	 * Note that there is ambiguity in the choice of sprites in the
	 * {@code TEAM1.DAT, TEAM2.DAT, TEAM3.DAT} sprites in the range 
	 *
	 * @param SWOS
	 * @return
	 * @throws IOException
	 * @deprecated due to ambiguity in the index number, and lack of centering info
	 */
	@Deprecated
	public static Map<Integer, Sprite> getSprites(File SWOS) throws IOException {
		Map<Integer, Sprite> sprites = Maps.newHashMap();
		SpriteParser parser = new SpriteParser();
		for (String name : ORDER) {
			File datFile = new File(Main.SWOS.getPath() + File.separator + name);
			InputStream datS = new FileInputStream(datFile);
			sprites.putAll(parser.parseDat(datS));
		}
		return sprites;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static final void main(String[] args) throws IOException {
		SpriteParser parser = new SpriteParser();
		for (String name : ORDER) {
			File datFile = new File(Main.SWOS.getPath() + File.separator + name);
			InputStream datS = new FileInputStream(datFile);
			Map<Integer, Sprite> sprites = parser.parseDat(datS);
			for (Entry<Integer, Sprite> e : sprites.entrySet()) {
				ImageIO.write(e.getValue().getImage(), "png", new File("data/sprites/" + name + "-" + e.getKey() + ".png"));
			}
		}
	}
	private final List<Color> pal = SwosUtils.getPalette();

	/**
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public Map<Integer, Sprite> parseDat(InputStream stream) throws IOException {
		Preconditions.checkNotNull(stream);
		Map<Integer, Sprite> sprites = Maps.newHashMap();
		DataInputStream dat = new DataInputStream(stream);
		try {
			while (true) {
				try {
					dat.readInt();
				} catch (EOFException e) {
					break;
				}
				dat.skipBytes(6);
				int width = Short.reverseBytes(dat.readShort());
				int nlines = Short.reverseBytes(dat.readShort());
				int wquads = Short.reverseBytes(dat.readShort());
				int xc = Short.reverseBytes(dat.readShort());
				int yc = Short.reverseBytes(dat.readShort());
				dat.skipBytes(2);
				int id = Short.reverseBytes(dat.readShort());

				Preconditions.checkArgument(id >= 0 && id < 1335, id);
				Preconditions.checkArgument(!sprites.containsKey(id), id);

				byte[] data = new byte[8 * wquads * nlines];
				dat.readFully(data);
				int[][] pixels = decodeSprite(data, wquads, nlines);

				// List<Color> pal = id >= 1209 && id <= 1272 ? SwosUtils.getGamePalette() : SwosUtils.getPalette();

				BufferedImage image = new BufferedImage(width, nlines, BufferedImage.TYPE_INT_ARGB);
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < nlines; y++) {
						int c = pixels[x][y];
						if (c == 0)
							continue;
						Color col = pal.get(c);
						int rgb = col.getRGB();
						image.setRGB(x, y, rgb);
					}
				}
				Sprite sprite = new Sprite(image, new Point(xc, yc));

				sprites.put(id, sprite);
			}
			return sprites;
		} finally {
			dat.close();
		}
	}

	private int[][] decodeSprite(byte[] data, int wquads, int nlines) {
		int[] sprite = SwosUtils.unsignedBytesToInts(data);
		int[][] output_pixels = new int[16 * wquads][nlines];
		int half = 8 * wquads;
		for (int line = 0; line < nlines; line++) {
			int output_pix_index = 0;
			for (int byte_index = 0; byte_index < half / 4; byte_index++) {
				int offset = wquads * 8 * line;
				int byte1 = sprite[offset + byte_index];
				int byte2 = sprite[offset + byte_index + half / 4];
				int byte3 = sprite[offset + byte_index + half / 2];
				int byte4 = sprite[offset + byte_index + 3 * half / 4];
				for (int i = 0; i < 8; i++) {
					int p1 = (byte1 & 0x80) >> 7;
					p1 |= (byte2 & 0x80) >> 6;
					p1 |= (byte3 & 0x80) >> 5;
					p1 |= (byte4 & 0x80) >> 4;
					output_pixels[output_pix_index + i][line] = p1;
					byte1 = (byte1 << 1) & 0xff;
					byte2 = (byte2 << 1) & 0xff;
					byte3 = (byte3 << 1) & 0xff;
					byte4 = (byte4 << 1) & 0xff;
				}
				output_pix_index += 8;
			}
		}
		return output_pixels;
	}
}
