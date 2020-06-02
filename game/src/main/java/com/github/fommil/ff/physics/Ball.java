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
import java.util.logging.Logger;

import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DFixedJoint;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DSphere;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

import com.github.fommil.ff.Pitch;
import com.github.fommil.ff.Tactics.BallZone;
import com.google.common.base.Preconditions;

/**
 * The model (M) and controller (C) for the ball during game play.
 *
 * @author Samuel Halliday
 * @author Doga Can Yanikoglu
 * @see <a href="http://en.wikipedia.org/wiki/Football_(ball)#Dimensions">Dimension information</a>
 */
public class Ball {

	private static final Logger log = Logger.getLogger(Ball.class.getName());

	private static final double MASS_KG = 0.45;

	private static final double RADIUS = 0.2; // official size = 0.7 / (2 * Math.PI);

	private static final double LIFT = 6;

	private static final double POWER = 2;

	private static final double BEND = 5;

	private static final double MAX_HEIGHT = 4;

	private final DSphere sphere;

	private final DWorld world;

	private volatile boolean aftertouch;

	private volatile Player owner = null;

	private volatile boolean isKickedRecently;

	Ball(DWorld world, DSpace space) {
		Preconditions.checkNotNull(world);
		Preconditions.checkNotNull(space);

		DBody body = OdeHelper.createBody(world);
		sphere = OdeHelper.createSphere(RADIUS);
		sphere.setBody(body);
		DMass mass = OdeHelper.createMass();
		mass.setSphereTotal(MASS_KG, RADIUS);
		body.setMass(mass);
		space.add(sphere);
		body.setData(this);

		this.isKickedRecently = false;

		this.world = world;
	}

	/**
	 * Controller.
	 *
	 * @param aftertouches
	 */
	public void setAftertouch(Collection<Aftertouch> aftertouches) {
		Preconditions.checkNotNull(aftertouches);
		if (!aftertouch || aftertouches.isEmpty()) {
			return;
		}
		DVector3C velocity = sphere.getBody().getLinearVel();
		if (velocity.length() < GamePhysics.MIN_SPEED) {
			return;
		}
		DVector3 forward = new DVector3(velocity);
		forward.set2(0);
		if (forward.length() < GamePhysics.MIN_SPEED) {
			return;
		}
		forward.normalize();
		double vz = velocity.get2();
		if (vz < 0) {
			return;
		}

		DVector3 touch = Aftertouch.asVector(aftertouches);
		DVector3 sideways = new DVector3(-forward.get1(), forward.get0(), 0);
		double bend = sideways.dot(touch);

		if (Math.abs(bend) > 0.1) {
			DVector3 bendy = new DVector3(sideways);
			bendy.scale(bend * BEND);
			addForce(bendy);
		}

		double z = sphere.getBody().getPosition().get2() - RADIUS;
		if (z > MAX_HEIGHT) {
			return;
		}

		double power = forward.dot(touch);
		if (Math.abs(power) > 0.1) {
			if (power < 0) {
				DVector3 lift = new DVector3(0, 0, 1);
				lift.scale(LIFT);
				addForce(lift);
			} else {
				DVector3 powery = new DVector3(forward);
				powery.scale(POWER);
				// power diminishes with height
				powery.scale(Math.abs(MAX_HEIGHT - z));
				addForce(powery);
			}
		}
	}

	/**
	 * @param pitch
	 * @return
	 */
	public BallZone getZone(Pitch pitch) {
		Preconditions.checkNotNull(pitch);
		return new BallZone(new Position(sphere.getPosition()), pitch);
	}

	public Velocity getVelocity() {
		return new Velocity(sphere.getBody().getLinearVel());
	}

	/**
	 * Places the ball, at rest, at the given position.
	 *
	 * @param p
	 */
	public void setPosition(Position p) {
		Preconditions.checkNotNull(p);
		Preconditions.checkArgument(!Double.isNaN(p.x));
		Preconditions.checkArgument(!Double.isNaN(p.y));
		Preconditions.checkArgument(!Double.isNaN(p.z));

		DVector3 vector = p.toDVector();
		vector.add(0, 0, sphere.getRadius());
		sphere.setPosition(vector);
		setVelocity(new DVector3());
	}

	public Position getPosition() {
		DVector3C p = sphere.getPosition();
		return new Position(p.get0(), p.get1(), p.get2() - sphere.getRadius());
	}

	public void setVelocity(DVector3 v) {
		sphere.getBody().setLinearVel(v);
	}

	public void setVelocity(Velocity v) {
		setVelocity(v.toDVector());
	}

	private void addForce(DVector3C force) {
		assert force != null;
		assert !Double.isNaN(force.get0());
		assert !Double.isNaN(force.get1());
		assert !Double.isNaN(force.get2());

		DBody body = sphere.getBody();
		body.addForce(force);
	}

	public void setDamping(double damping) {
		sphere.getBody().setLinearDamping(damping);
	}

	public void setAftertouch(boolean enabled) {
		this.aftertouch = enabled;
	}

	public DGeom getGeom() {
		return sphere;
	}

	public synchronized Player getOwner() {
		return owner;
	}

	public synchronized boolean isOwned() {
		return owner != null;
	}

	public synchronized void setOwner(Player p) {
		owner = p;
		if(p != null) {
			addFixedJoint(p.body);
		}
	}

	public synchronized boolean isKickedRecently() {
		return isKickedRecently;
	}

	public synchronized void setKickStatus(Boolean b) {
		isKickedRecently = b;
	}

	private synchronized void addFixedJoint(DBody body) {
        DFixedJoint joint = OdeHelper.createFixedJoint(world);
        joint.attach(body, this.getGeom().getBody());
    }
}
