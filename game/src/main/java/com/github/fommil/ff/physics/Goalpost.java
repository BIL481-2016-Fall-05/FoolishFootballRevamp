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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.ode4j.math.DVector3;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DBox;
import org.ode4j.ode.DFixedJoint;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DGeom.DNearCallback;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

import com.github.fommil.ff.Direction;
import com.github.fommil.ff.Pitch;
import com.google.common.base.Preconditions;

/**
 * The model (M) and controller (C) for a goal during game play.
 *
 * @author Samuel Halliday
 */
public class Goalpost {

	private static final Logger log = Logger.getLogger(Goalpost.class.getName());

	private final DBody body;

	private final DBox goal;

	private final Direction facing;

	/**
	 *
	 * @param world
	 * @param space
	 * @param pitch
	 * @param facing
	 */
	Goalpost(DWorld world, DSpace space, Pitch pitch, Direction facing) {
		Preconditions.checkNotNull(world);
		Preconditions.checkNotNull(space);
		Preconditions.checkArgument(facing == Direction.NORTH || facing == Direction.SOUTH);
		this.facing = facing;

		this.body = OdeHelper.createBody(world);
		body.setData(this);
		DVector3 centre;
		if (facing == Direction.NORTH) {
			centre = pitch.getGoalBottom().toDVector();
		} else {
			centre = pitch.getGoalTop().toDVector();
		}
		body.setPosition(centre);
		body.setKinematic();
		DFixedJoint fixed = OdeHelper.createFixedJoint(world, null);
		fixed.attach(null, body);
		fixed.setFixed();

		double width = pitch.getGoalWidth();
		double height = pitch.getGoalHeight();
		double depth = pitch.getGoalDepth();
		double thickness = pitch.getGoalThickness();

		DBox left = OdeHelper.createBox(space, thickness, depth, height);
		left.setBody(body);
		left.setOffsetPosition(thickness / 2 - width / 2, 0, height / 2);

		DBox right = OdeHelper.createBox(space, thickness, depth, height);
		right.setBody(body);
		right.setOffsetPosition(-thickness / 2 + width / 2, 0, height / 2);

		DBox back = OdeHelper.createBox(space, width, thickness, height);
		back.setBody(body);
		double backUp = 1;
		if (facing == Direction.NORTH) {
			backUp = -1;
		}
		back.setOffsetPosition(0, backUp * (depth / 2 - thickness / 2), height / 2);

		DBox roof = OdeHelper.createBox(space, width, depth, thickness);
		roof.setBody(body);
		roof.setOffsetPosition(0, 0, height - thickness / 2);

		// goal is not registered with the world
		goal = OdeHelper.createBox(width - thickness, depth - thickness / 2, height - thickness / 2);
		DVector3 pos = new DVector3(body.getPosition());
		pos.add(1, backUp * thickness / 4);
		pos.add(2, height / 2 + thickness / 4);
		goal.setPosition(pos);
	}

	boolean isInside(Ball ball) {
		Preconditions.checkNotNull(ball);

		final AtomicBoolean inside = new AtomicBoolean();
		goal.collide2(ball.getGeom(), null, new DNearCallback() {

			@Override
			public void call(Object data, DGeom o1, DGeom o2) {
				inside.set(true);
			}
		});
		return inside.get();
	}

	public Direction getFacing() {
		return facing;
	}
}
