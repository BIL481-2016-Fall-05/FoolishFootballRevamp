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
package uk.me.fommil.ff.physics;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.ode4j.math.DMatrix3;
import org.ode4j.math.DMatrix3C;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DBox;
import org.ode4j.ode.DMass;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;
import org.ode4j.ode.internal.Rotation;
import uk.me.fommil.ff.PlayerStats;

/**
 * The model (M) and controller (C) for a {@link Player} during game play.
 *
 * @author Samuel Halliday
 */
public class Player {

	private static final Logger log = Logger.getLogger(Player.class.getName());

	protected static final double HEIGHT = 1.7; // http://en.wikipedia.org/wiki/Human_height

	private static final double WIDTH = 1.0;

	private static final double DEPTH = 0.75;

	private static final double SPEED = 6.5; // about 15 MPH

	private static final double MASS = 60;

	private static final double AUTOPILOT_TOLERANCE = 1;

	private static final double HEADER_BOOST = 2;

	private static final double TACKLE_BOOST = 2;

	public enum PlayerState {

		RUN, KICK, TACKLE, HEAD_START, HEAD_MID, HEAD_END, GROUND, INJURED,
		THROW, THROWING, PENALTY, CELEBRATE, PUNISH

	}

	private final PlayerStats stats;

	private final int shirt;

	private final DBox box;

	protected final DBody body;

	private volatile Collection<Action> actions = Collections.emptySet();

	private volatile PlayerState forcedState;

	Player(int i, PlayerStats stats, DWorld world, DSpace space) {
		Preconditions.checkArgument(i >= 1 && i <= 11, i);
		Preconditions.checkNotNull(stats);
		this.shirt = i;
		this.stats = stats;
		this.body = OdeHelper.createBody(world);
		box = OdeHelper.createBox(space, WIDTH, DEPTH, HEIGHT);
		box.setBody(body);

		DMass mass = OdeHelper.createMass();
		mass.setBoxTotal(MASS, WIDTH, DEPTH, HEIGHT);
		body.setMass(mass);
		body.setData(this);
		body.setAngularDamping(0.1);
	}

	void control(Ball ball) {
		Preconditions.checkNotNull(ball);
		if (distanceTo(ball) > 1)
			return;
//		// TODO: come up with a solution that avoids the oscillation
//		DVector3 control = getPosition().toDVector().sub(ball.getPosition().toDVector());
//		control.set(2, 0);
//		control.scale(25);
//		ball.addForce(control);
	}

	void kick(Ball ball) {
		assert actions.contains(Action.KICK);
		if (distanceTo(ball) > 1.5)
			return;
		hit(ball, 10, 2);
	}

	void throwIn(Ball ball) {
		assert getState() == PlayerState.THROWING;
		assert actions.contains(Action.KICK);
		if (distanceTo(ball) > 1)
			return;
		hit(ball, 5, 0);
		setState(PlayerState.RUN);
	}

	private void hit(Ball ball, double power, double lift) {
		double direction = getDirection();
		DVector3 kick = new DVector3(power * Math.sin(direction), power * Math.cos(direction), lift);
		ball.setVelocity(kick);
		ball.setAftertouchEnabled(true);
	}

	private double distanceTo(Ball ball) {
		// TODO: better distance measure considering feet location and direction
		return getPosition().distance(ball.getPosition());
	}

	/**
	 * Controller, must be called for each time step.
	 * 
	 * @param actions
	 */
	void setActions(Collection<Action> actions) {
		Preconditions.checkNotNull(actions);
		PlayerState state = getState();
		switch (state) {
			case THROW:
			case RUN:
			case KICK:
				break;
			default:
				return;
		}
		this.actions = actions;
		DVector3 move = Action.asVector(actions);
		move.scale(SPEED);

		DMatrix3 rotation = new DMatrix3();
		DMatrix3 tilt = new DMatrix3();
		double direction = GamePhysics.toAngle(move, getDirection());
		Rotation.dRFromAxisAndAngle(rotation, 0, 0, -1, direction);
		Rotation.dRFromAxisAndAngle(tilt, -1, 0, 0, getTilt());
		rotation.eqMul(rotation.clone(), tilt);

		move.add(2, body.getLinearVel().get(2));
		if (state == PlayerState.RUN) {
			if (actions.contains(Action.HEAD)) {
				move.scale(HEADER_BOOST);
				move.add(0, 0, 3);
				// TODO: trajectory that doesn't make player land with feet on ground after heading
			} else if (actions.contains(Action.TACKLE)) {
				move.scale(TACKLE_BOOST);
				DMatrix3 horizontal = new DMatrix3();
				Rotation.dRFromAxisAndAngle(horizontal, -1, 0, 0, Math.PI / 2);
				rotation.eqMul(rotation.clone(), horizontal);
			}
		}

		if (state != PlayerState.THROW)
			body.setLinearVel(move);
		body.setRotation(rotation);
	}

	/**
	 * Controller. Ignore user input and go to the zone indicated.
	 *
	 * @param attractor
	 */
	void autoPilot(Position attractor) {
		Preconditions.checkNotNull(attractor);
		List<Action> auto = Lists.newArrayList();
		double dx = body.getPosition().get0() - attractor.x;
		if (dx < -AUTOPILOT_TOLERANCE) {
			auto.add(Action.RIGHT);
		} else if (dx > AUTOPILOT_TOLERANCE) {
			auto.add(Action.LEFT);
		}
		double dy = body.getPosition().get1() - attractor.y;
		if (dy < -AUTOPILOT_TOLERANCE) {
			auto.add(Action.UP);
		} else if (dy > AUTOPILOT_TOLERANCE) {
			auto.add(Action.DOWN);
		}
		setActions(auto);
	}

	// only works for some states
	@SuppressWarnings("fallthrough")
	void setState(PlayerState state) {
		switch (state) {
			case RUN:
			case THROW:
			case CELEBRATE:
			case PUNISH:
				setUpright();
				this.forcedState = state;
				break;
			case INJURED:
				this.forcedState = state;
				break;
			default:
				throw new UnsupportedOperationException();
		}
	}

	private void setUpright() {
		setPosition(getPosition());
		DMatrix3 rotation = new DMatrix3();
		Rotation.dRFromAxisAndAngle(rotation, 0, 0, -1, getDirection());
		body.setRotation(rotation);
	}

	/**
	 * @return
	 */
	@SuppressWarnings("fallthrough")
	public PlayerState getState() {
		if (forcedState != null)
			switch (forcedState) {
				case THROW:
					if (actions.contains(Action.KICK))
						return PlayerState.THROWING;
				case CELEBRATE:
				case PUNISH:
				case INJURED:
					return forcedState;
			}

		DVector3C position = body.getPosition();
		DVector3C velocity = body.getLinearVel();

		double tilt = getTilt();
		double z = position.get2() - HEIGHT / 2;
		double vz = velocity.get2();

		if (tilt > Math.PI / 8) {
			if (actions.contains(Action.TACKLE))
				return PlayerState.TACKLE;
			return PlayerState.GROUND;
		}
		if (vz > 0) {
			if (z < 0.2)
				return PlayerState.HEAD_START;
			if (z < 0.4)
				return PlayerState.HEAD_MID;
			return PlayerState.HEAD_END;
		}
		if (z > 0.1)
			return PlayerState.HEAD_END;
		if (actions.contains(Action.KICK))
			return PlayerState.KICK;
		return PlayerState.RUN;
	}

	/**
	 * @return the angle relative to NORTH {@code (- PI, + PI]}.
	 */
	public double getDirection() {
		// TODO: is there a cleaner way to calculate direction?
		DMatrix3C rotation = body.getRotation();
		if (getTilt() > Math.PI / 4) {
			DVector3 rotated = new DVector3(rotation.get02(), rotation.get12(), rotation.get22());
			rotated.normalize();
			return Math.signum(rotated.get0()) * Math.acos(rotated.dot(new DVector3(0, 1, 0)));
		}
		DVector3 rotated = new DVector3(rotation.get01(), rotation.get11(), rotation.get21());
		rotated.normalize();
		return Math.signum(rotated.get0()) * Math.acos(rotated.dot(new DVector3(0, 1, 0)));
	}

	// returns the angle (radians) off the vertical [0, PI]
	double getTilt() {
		// TODO: a [-PI, PI] version for head over feet
		DMatrix3C rotation = body.getRotation();
		DVector3 rotated = new DVector3(rotation.get02(), rotation.get12(), rotation.get22());
		rotated.normalize();
		return Math.acos(rotated.dot(new DVector3(0, 0, 1)));
	}

	public Velocity getVelocity() {
		return new Velocity(body.getLinearVel());
	}

	public Position getPosition() {
		return new Position(body.getPosition());
	}

	void setPosition(Position p) {
		setPosition(p.toDVector());
	}

	void setPosition(DVector3C p) {
		DVector3 position = new DVector3(p);
		position.set(2, HEIGHT / 2);
		body.setPosition(position);
	}

	// [0, 1] bounciness when contacting the ball
	double getBounce() {
		switch (getState()) {
			case HEAD_START:
			case HEAD_MID:
			case HEAD_END:
				return 1.0;
			default:
				return 0.1;
		}
	}

	public int getShirt() {
		return shirt;
	}
}