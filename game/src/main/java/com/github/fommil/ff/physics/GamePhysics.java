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

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.logging.Logger;

import org.ode4j.math.DVector3;
import org.ode4j.ode.DGeom.DNearCallback;
import com.github.fommil.ff.Direction;
import com.github.fommil.ff.Pitch;
import com.github.fommil.ff.PlayerStats;
import com.github.fommil.ff.Tactics;
import com.github.fommil.ff.Tactics.BallZone;
import com.github.fommil.ff.Tactics.PlayerZone;
import com.github.fommil.ff.Team;
import com.github.fommil.ff.physics.Player.PlayerState;
import com.github.fommil.ff.swos.SoundParser;
import com.github.fommil.ff.swos.SoundParser.Fx;

/**
 * The model (M) and controller (C) for game play.
 * The coordinate system is a left-handed system with X = East, Y = North, Z = Sky.
 *
 * @author Samuel Halliday
 * @author Doga Can Yanikoglu
 */
public class GamePhysics extends Physics {

	private static final Logger log = Logger.getLogger(GamePhysics.class.getName());

	static final double MIN_SPEED = 0.1;

	static final double MAX_SPEED = 50;

	private final OpponentController opponentController;

	@Deprecated // DEBUGGING
	private void debugNaNs() {
		ball.getPosition();
		ball.getVelocity();
		for (Player player : getPlayers()) {
			player.getPosition();
			player.getVelocity();
		}
	}

	/**
	 * @param vector
	 * @return the angle relative to NORTH {@code (- PI, + PI]}.
	 */
	static double toAngle(DVector3 vector) {
		return dePhase(Math.PI / 2 - Math.atan2(vector.get1(), vector.get0()));
	}

	/**
	 * @return the angle relative to NORTH {@code (- PI, + PI]}.
	 */
	static double toAngle(DVector3 vector, double fallback) {
		Preconditions.checkNotNull(vector);
		if (vector.length() == 0)
			return fallback;
		return toAngle(vector);
	}

	private static double dePhase(double d) {
		if (d > Math.PI)
			return dePhase(d - 2 * Math.PI);
		if (d <= -Math.PI)
			return dePhase(d + 2 * Math.PI);
		return d;
	}

	private final Team a, b;

	private final Ball ball;

	private final List<Player> as = Lists.newArrayListWithCapacity(11);

	private final List<Opponent> bs = Lists.newArrayListWithCapacity(11);

	private final Pitch pitch;

	private volatile Player selected;

	private volatile Collection<Action> actions = Collections.emptyList();

	private volatile Collection<Aftertouch> aftertouches = Collections.emptyList();

	private final Map<Player, Double> grounded = Maps.newHashMap();

	private final Collection<Goalpost> goals = Lists.newArrayList();

	private volatile boolean pauseGame;

	/**
	 * @param a
	 * @param b
	 * @param pitch
	 */
	public GamePhysics(Team a, Team b, Pitch pitch) {
		super(9.81);
		this.a = a;
		this.b = b;
		this.pitch = pitch;
		pauseGame = false;

		opponentController = new OpponentController();

		ball = new Ball(world, space);
		Position centre = pitch.getCentre();
		ball.setPosition(centre);

		goals.add(new Goalpost(world, space, pitch, Direction.NORTH));
		goals.add(new Goalpost(world, space, pitch, Direction.SOUTH));

		BallZone bz = ball.getZone(pitch);

        List<PlayerStats> aPlayers = a.getPlayers();
        Tactics tactics = a.getCurrentTactics();
        for (int i = 2; i <= 11; i++) {
            Position p = tactics.getZone(bz, i, Direction.NORTH).getCentre(pitch);
            Player pma = new Player(i, a, aPlayers.get(i - 1), world, space, this);
            pma.setPosition(p);
            pma.setOpponent(Direction.NORTH);
            as.add(pma);
        }
        selected = as.get(9);

		List<PlayerStats> bPlayers = b.getPlayers();
		tactics = b.getCurrentTactics();

		for (int i = 2; i <= 11; i++) {
			Position p = tactics.getZone(bz, i, Direction.SOUTH).getCentre(pitch);
			Opponent pma = new Opponent(i, b, bPlayers.get(i - 1), world, space, this);
			pma.setPosition(p);
		    pma.setOpponent(Direction.SOUTH);
			bs.add(pma);
		}

        GeneralAgent gameAgent = new GeneralAgent(this);
	}

	@Override
	protected DNearCallback getCollisionCallback() {
		GameCollisionHandler handler = new GameCollisionHandler();
		return new CollisionCallback(world, joints, handler, this);
	}

	/**
	 * Controller.
	 *
	 * @param actions
	 * @param aftertouches
	 */
	public void setUserActions(Collection<Action> actions, Collection<Aftertouch> aftertouches) {
		Preconditions.checkNotNull(actions);
		Preconditions.checkNotNull(aftertouches);
		this.actions = Sets.newHashSet(actions);
		this.aftertouches = Sets.newHashSet(aftertouches);
	}

	@Override
	protected void beforeStep() {
		debugNaNs();
        for (Goalpost goal : goals) {
			if (goal.isInside(ball)) {
                pauseGame = true;
				log.info("GOAL TO " + goal.getFacing());
				SoundParser.play(Fx.CROWD_CHEER);
			}
		}

		if (actions.contains(Action.CHANGE))
			updateSelected();

		BallZone bz = ball.getZone(pitch);
		for (Player p : getPlayers()) {
			transition(p);
			if (p == selected)
				continue;

			if (p instanceof Opponent && ((Opponent) p).isSelected()) {
				opponentController.autoPilot((Opponent) p);
				continue;
			}

			if(getBall().isKickedRecently()) { // If ball is kicked recently, stop all of players for a short time
			    continue;
            }

			Team team = p.getTeam();
			Tactics tactics = team.getCurrentTactics();
			PlayerZone pz;
			if (team == a) {
				pz = tactics.getZone(bz, p.getShirt(), Direction.NORTH);
			} else {
				pz = tactics.getZone(bz, p.getShirt(), Direction.SOUTH);
			}
			Position target = pz.getCentre(pitch);

			p.autoPilot(target);
		}

		selected.setActions(actions);
		ball.setAftertouch(aftertouches);
		ball.setDamping(0);
	}

	@Override
	protected void afterStep() {
		double ballSpeed = ball.getVelocity().speed();
		if (Double.isNaN(ballSpeed)) {
			log.warning("ball had NaN speed");
			ball.setVelocity(new DVector3());
		}
		if (ballSpeed < MIN_SPEED)// stops small movements
			ball.setVelocity(new DVector3());
		if (ballSpeed > MAX_SPEED) { // stops really weird rounding errors
			log.warning("ball was going " + ballSpeed);
			DVector3 ballVelocity = ball.getVelocity().toDVector();
			ballVelocity.normalize();
			ballVelocity.scale(MAX_SPEED);
		}

		switch (selected.getState()) {
			case KICK:
				selected.kick(ball);
				break;
			case PASS:
				selected.pass(null, ball);
				break;
			case THROWING:
				selected.throwIn(ball);
            case STEAL:
                selected.steal(ball);
        }
	}

	public void updateSelected() {
		assert selected != null;
		Player closest = selected;
		double distance = Double.MAX_VALUE;
		for (Player model : as) {
			switch (model.getState()) {
				case GROUND:
				case INJURED:
				case HEAD_START:
				case HEAD_MID:
				case HEAD_END:
				case TACKLE:
					continue;
			}
			double ds2 = model.getPosition().distance(ball.getPosition());
			if (ds2 < distance) {
				distance = ds2;
				closest = model;
			}
		}
		selected = closest;
	}

	@SuppressWarnings("fallthrough")
	private void transition(Player p) {
		// TODO: should be in the Player class
		switch (p.getState()) {
			case TACKLE:
				if (p.getVelocity().speed() > MIN_SPEED)
					break;
			case GROUND:
				if (!grounded.containsKey(p)) {
					grounded.put(p, time);
				} else if ((time - grounded.get(p)) > 2) {
					if (p.getState() == PlayerState.GROUND && new Random().nextBoolean()) {
						p.setState(PlayerState.INJURED);
						return;
					}
					p.setState(PlayerState.RUN);
					grounded.remove(p);
					break;
				}
			case INJURED:
				if (!grounded.containsKey(p)) {
					log.warning("WASN'T LISTED IN GROUNDED");
					grounded.put(p, time);
				} else if ((time - grounded.get(p)) > 5) {
					p.setState(PlayerState.RUN);
					grounded.remove(p);
				}
		}
	}

	// <editor-fold defaultstate="collapsed" desc="BOILERPLATE GETTERS/SETTERS">
	public Ball getBall() {
		return ball;
	}

	public Iterable<Player> getPlayers() {
		return Iterables.concat(as, bs);
	}

	public List<Opponent> getOpponentPlayers() {
		return bs;
	}

	public List<Player> getOwningPlayers() {
		return as;
	}

	public Player getSelected() {
		return selected;
	}

	public Opponent getControlledOpponent() {
	    for(Opponent o: bs) {
	        if(o.isSelected()) {
	            return o;
            }
        }
        return null;
    }

    public boolean getPauseState() {
	   return pauseGame;
    }

	public double getTimestamp() {
		return time;
	}

	public Team getTeamA() {
		return a;
	}

	public Team getTeamB() {
		return b;
	}

	public Pitch getPitch() {
		return pitch;
	}

	public ArrayList<Opponent> getOpponentsIn(Pitch.Area area) {
        ArrayList<Opponent> temp = new ArrayList<Opponent>();
	    for(Opponent o: bs) {
	        if(o.getArea() == area)
	            temp.add(o);
        }
        return temp;
    }
	// </editor-fold>
}