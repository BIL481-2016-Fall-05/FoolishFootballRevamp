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
package com.github.fommil.ff.physics;

import java.util.Collection;

import org.ode4j.math.DVector3;

import com.github.fommil.ff.Direction;
import com.google.common.base.Preconditions;

/**
 * The aftertouch that a ball may exhibit. Aftertouch depends on the direction of motion.
 */
public enum Aftertouch {

	UP, DOWN, LEFT, RIGHT;

	static DVector3 asVector(Collection<Aftertouch> touches) {
		Preconditions.checkNotNull(touches);
		DVector3 aftertouch = new DVector3();
		for (Aftertouch touch : touches) {
			assert touch != null;
			switch (touch) {
				case UP:
					aftertouch.add(0, 1, 0);
					break;
				case DOWN:
					aftertouch.sub(0, 1, 0);
					break;
				case LEFT:
					aftertouch.sub(1, 0, 0);
					break;
				case RIGHT:
					aftertouch.add(1, 0, 0);
					break;
			}
		}
		if (aftertouch.length() > 0) {
			aftertouch.normalize();
		}
		return aftertouch;
	}

	static Direction toDirection(Collection<Aftertouch> touches) {
		DVector3 vector = asVector(touches);
		double angle = GamePhysics.toAngle(vector, 0);
		return Direction.valueOf(angle);
	}
}
