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
package com.github.fommil.ff;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Map;

import com.google.common.base.Preconditions;

/**
 * Wrapper class for a {@link BufferedImage} with metadata, including convenience methods.
 *
 * @author Samuel Halliday
 */
public class Sprite {

	private final BufferedImage image;

	private final Point centre;

	/**
	 * @param image
	 * @param centre
	 */
	public Sprite(BufferedImage image, Point centre) {
		this.image = image;
		this.centre = centre;
	}

	/**
	 * Creates a copy of this, with the given colours replaced.
	 *
	 * @param replace
	 * @return
	 */
	public Sprite copyWithReplace(Map<Color, Color> replace) {
		Preconditions.checkNotNull(replace);
		BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int rgb = image.getRGB(x, y);
				if (rgb == 0)
					continue;
				Color c = new Color(rgb);
				if (replace.containsKey(c)) {
					c = replace.get(c);
				}
				newImage.setRGB(x, y, c.getRGB());
			}
		}
		return new Sprite(newImage, new Point(centre));
	}

	@Override
	public String toString() {
		return centre.toString();
	}

	// <editor-fold defaultstate="collapsed" desc="BOILERPLATE GETTERS/SETTERS">
	public Point getCentre() {
		return centre;
	}

	public BufferedImage getImage() {
		return image;
	}
	// </editor-fold>
}
