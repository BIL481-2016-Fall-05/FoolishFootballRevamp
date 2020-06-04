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

import com.google.common.base.Preconditions;

/**
 *
 * @author Samuel Halliday
 */
public enum Direction {

	NORTH(0), SOUTH(Math.PI), WEST(-Math.PI / 2), EAST(Math.PI / 2),
	NORTH_EAST(Math.PI / 4), NORTH_WEST(-Math.PI / 4), SOUTH_EAST(3 * Math.PI / 4), SOUTH_WEST(-3 * Math.PI / 4);

	private double angle;

	Direction(double angle) {
		this.angle = angle;
	}

	public double getAngle() {
		return angle;
	}

	/**
	 * @param angle {@code [-PI, PI]} with 0 being NORTH.
	 * @return
	 */
	public static Direction valueOf(double angle) {
		if (Double.isNaN(angle)) {
			return null;
		}
		Preconditions.checkArgument(angle <= Math.PI && angle >= -Math.PI, angle);
		angle = angle - Math.PI / 8; // tolerance
		if (angle <= -Math.PI) {
			return SOUTH;
		} else if (angle <= -3 * Math.PI / 4) {
			return SOUTH_WEST;
		} else if (angle <= -Math.PI / 2) {
			return WEST;
		} else if (angle <= -Math.PI / 4) {
			return NORTH_WEST;
		} else if (angle <= 0) {
			return NORTH;
		} else if (angle <= Math.PI / 4) {
			return NORTH_EAST;
		} else if (angle <= Math.PI / 2) {
			return EAST;
		} else if (angle <= 3 * Math.PI / 4) {
			return SOUTH_EAST;
		} else {
			return SOUTH;
		}
	}
}
