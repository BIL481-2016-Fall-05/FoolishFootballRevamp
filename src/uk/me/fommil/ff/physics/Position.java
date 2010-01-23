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
package uk.me.fommil.ff.physics;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import javax.vecmath.Vector3d;
import org.ode4j.math.DVector3;
import static java.lang.Math.*;
import org.ode4j.math.DVector3C;

/**
 * Container class describing the (x, y, z) position of an object. Used so that the physics engine
 * does not need to expose implementation details.
 *
 * @author Samuel Halliday
 */
public class Position {

	public final double x, y, z;

	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public Position(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	// implementation specific constructor
	Position(DVector3C vector) {
		this(vector.get0(), vector.get1(), vector.get2());
	}

	// implementation specific convertor
	DVector3 toDVector() {
		return new DVector3(x, y, z);
	}

	// implementation specific constructor
	@Deprecated
	Position(Vector3d vector) {
		this(vector.x, vector.y, vector.z);
	}

	// implementation specific convertor
	@Deprecated
	Vector3d toVector3d() {
		return new Vector3d(x, y, z);
	}

	/**
	 * @param other
	 * @return
	 */
	public double distance(Position other) {
		Preconditions.checkNotNull(other);
		return sqrt(pow(x - other.x, 2) + pow(y - other.y, 2) + pow(z - other.z, 2));
	}

	@Override
	public boolean equals(Object obj) {
		// <editor-fold defaultstate="collapsed" desc="boilerplate identity, instanceof and cast">
		if (this == obj)
			return true;
		if (!(obj instanceof Position))
			return false;
		final Position other = (Position) obj;// </editor-fold>
		return x == other.x && y == other.y && z == other.z;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(x, y, z);
	}
}
