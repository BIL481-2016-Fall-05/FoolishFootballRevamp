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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import org.ode4j.math.DMatrix3;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.internal.Rotation;
import com.github.fommil.ff.Direction;
import com.github.fommil.ff.PlayerStats;
import com.github.fommil.ff.Team;

/**
 * The model (M) for a goalkeeper.
 *
 * @author Samuel Halliday
 */
public class Goalkeeper extends Player {

	public enum GoalkeeperState {

		DIVE_START, DIVE_MID, DIVE_PEAK, FALL_START, FALL_MID, FALL_END
		// TODO FALL_END_BALL, HOLDING

	}

	/**
	 * @param i
	 * @param team
	 * @param stats
	 * @param world
	 * @param space
	 */
	public Goalkeeper(int i, Team team, PlayerStats stats, DWorld world, DSpace space, GamePhysics game) {
		// TODO: consider rolling Goalkeeper functionality into Player
		super(i, team, stats, world, space, game);
	}

	@Override
	public void setActions(Collection<Action> actions) {
		if (getGkState() != null)
			return;

		Collection<Action> sanitised = Lists.newArrayList(actions);
		sanitised.remove(Action.TACKLE);
		sanitised.remove(Action.HEAD);
		if (actions.contains(Action.DIVE))
			sanitised.remove(Action.KICK);

		if (!sanitised.contains(Action.DIVE)) {
			super.setActions(sanitised);
			return;
		}
		sanitised.remove(Action.UP);
		sanitised.remove(Action.DOWN);
		DVector3 move = Action.asVector(sanitised);

		DMatrix3 rotation = new DMatrix3();
		DMatrix3 tilt = new DMatrix3();

		double direction = 0;
		if (getOpponent() == Direction.SOUTH) {
			direction = Math.PI;
		}
		Rotation.dRFromAxisAndAngle(rotation, 0, 0, -1, direction);
		double tiltAngle = 0;
		if (sanitised.contains(Action.RIGHT))
			tiltAngle = Math.PI / 4;
		else if (sanitised.contains(Action.LEFT))
			tiltAngle = -Math.PI / 4;
		Rotation.dRFromAxisAndAngle(tilt, 0, 1, 0, tiltAngle);
		rotation.eqMul(rotation.clone(), tilt);

		move.scale(10); // TODO: goalkeeper stats
		move.set2(5);

		body.setLinearVel(move);
		body.setRotation(rotation);
	}

	public GoalkeeperState getGkState() {
		DVector3C position = body.getPosition();
		DVector3C velocity = body.getLinearVel();
		double z = position.get2() - HEIGHT / 2;
		double vz = velocity.get2();
		double tilt = getTilt();
		if (tilt < Math.PI / 8 && z < 0.1 && Math.abs(vz) < 0.1)
			return null;

		if (vz > 0.5) {
			if (z < 0.1)
				return GoalkeeperState.DIVE_START;
			if (z < 0.25)
				return GoalkeeperState.DIVE_MID;
			return GoalkeeperState.DIVE_PEAK;
		}
		if (z < 0.1)
			return GoalkeeperState.FALL_END;
		if (z < 0.25)
			return GoalkeeperState.FALL_MID;
		return GoalkeeperState.FALL_START;
	}

	public void dive(@Nullable Direction direction) {
		Preconditions.checkArgument(direction == null || direction == Direction.EAST || direction == Direction.WEST, direction);

		List<Action> auto = Lists.newArrayList();
		if (direction != null)
			switch (direction) {
				case EAST:
					auto.add(Action.RIGHT);
					break;
				case WEST:
					auto.add(Action.LEFT);
			}
		auto.add(Action.DIVE);
		setActions(auto);
	}

	@Override
	public double getAutoPilotTolerance() {
		return 0.1;
	}
}
