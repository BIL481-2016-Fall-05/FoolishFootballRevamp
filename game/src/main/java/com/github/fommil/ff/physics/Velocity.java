/*
 * Copyright Samuel Halliday 2010
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
package com.github.fommil.ff.physics;

import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Container class describing the (x, y, z) velocity of an object. Used so that the physics engine
 * does not need to expose implementation details.
 *
 * @author Samuel Halliday
 */
public final class Velocity {

	public final double x, y, z;

	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public Velocity(double x, double y, double z) {
		Preconditions.checkArgument(!Double.isNaN(x), "x cannot be NaN");
		Preconditions.checkArgument(!Double.isNaN(y), "y cannot be NaN");
		Preconditions.checkArgument(!Double.isNaN(z), "z cannot be NaN");
		this.x = x;
		this.y = y;
		this.z = z;
	}

	// implementation specific constructor
	Velocity(DVector3C vector) {
		this(vector.get0(), vector.get1(), vector.get2());
	}

	// implementation specific convertor
	DVector3 toDVector() {
		return new DVector3(x, y, z);
	}

	/**
	 * @return
	 */
	public double speed() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public boolean equals(Object obj) {
		// <editor-fold defaultstate="collapsed" desc="boilerplate identity, instanceof and cast">
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Velocity)) {
			return false;
		}
		final Velocity other = (Velocity) obj;// </editor-fold>
		return x == other.x && y == other.y && z == other.z;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(x, y, z);
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}
}
