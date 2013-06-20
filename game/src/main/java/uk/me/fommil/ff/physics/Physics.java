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

import com.google.common.collect.Lists;
import java.util.Collection;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DGeom.DNearCallback;
import org.ode4j.ode.DJointGroup;
import org.ode4j.ode.DSimpleSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;
import org.ode4j.ode.internal.OdeInit;

/**
 * Reduces the boilerplate when constructing an Open Dynamics Engine Physics World.
 * Only one instance can be alive at a time, due to the single-threaded nature of ODE construction.
 * ODE requires manual cleanup.
 *
 * @author Samuel Halliday
 */
public abstract class Physics {

	static {
		try {
			OdeInit.dInitODE(); // TODO: better way to initialise world
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	final DWorld world;

	final DSimpleSpace space;

	final DJointGroup joints;

	final DNearCallback collision;

	volatile double time;

	Physics(double gravity) {
		world = OdeHelper.createWorld();
		world.setGravity(0, 0, -gravity);

		space = createSpace();
		joints = createJointGroup();

		OdeHelper.createPlane(space, 0, 0, 1, 0);

		collision = getCollisionCallback();
	}

	protected void clean() {
		joints.destroy();
		space.destroy();
		world.destroy();
	}

	protected DSimpleSpace createSpace() {
		return OdeHelper.createSimpleSpace();
	}

	protected DJointGroup createJointGroup() {
		return OdeHelper.createJointGroup();
	}

	protected Collection<DGeom> getGeoms() {
		Collection<DGeom> geoms = Lists.newArrayList();
		int num = space.getNumGeoms();
		for (int i = 0; i
				< num; i++) {
			geoms.add(space.getGeom(i));
		}
		return geoms;
	}

	/**
	 * @param dt in seconds
	 */
	public void step(double dt) {
		time += dt;

		beforeStep();

		space.collide(null, collision);

		world.step(dt);
		joints.empty();

		afterStep();
	}

	protected abstract DNearCallback getCollisionCallback();

	protected void beforeStep() {
	}

	protected void afterStep() {
	}
}
