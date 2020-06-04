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

/**
 * The actions that a user can perform.
 */
public enum Action {

	UP, DOWN, LEFT, RIGHT, KICK, PASS, TACKLE, HEAD, DIVE, CHANGE, STEAL;

	static DVector3 asVector(Collection<Action> actions) {
		DVector3 move = new DVector3();
		for (Action action : actions) {
			switch (action) {
				case UP:
					move.add(0, 1, 0);
					break;
				case DOWN:
					move.sub(0, 1, 0);
					break;
				case LEFT:
					move.sub(1, 0, 0);
					break;
				case RIGHT:
					move.add(1, 0, 0);
					break;
			}
		}
		if (move.length() > 0) {
			move.normalize();
		}
		return move;
	}
}
