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

import java.util.EnumSet;
import java.util.logging.Logger;
import org.ode4j.ode.DContact.DSurfaceParameters;
import org.ode4j.ode.OdeConstants;
import com.github.fommil.ff.physics.CollisionCallback.CollisionHandler;
import com.github.fommil.ff.physics.Player.PlayerState;
import com.github.fommil.ff.swos.SoundParser;

/**
 * Handles collisions using objects specific to this package.
 *
 * @author Samuel Halliday
 * @author Doga Can Yanikoglu
 * @see CollisionCallback
 */
class GameCollisionHandler implements CollisionHandler {
	private static final Logger log = Logger.getLogger(GameCollisionHandler.class.getName());

	@Override
	public boolean collide(Ball ball, Player player, DSurfaceParameters surface, GamePhysics game) {
		if(!ball.isOwned() && !ball.isKickedRecently()) {
			ball.setOwner(player);
			player.setBallOwner(true);
			if(!(player instanceof Opponent)) {
				game.updateSelected();
			}
		}

		enableSoftBounce(surface);
		ball.setDamping(0.1); // ?? can be overridden
		surface.bounce = player.getBounce();
		return false;
		// FIXME: consider ignoring many player/ball interactions altogether for gameplay!
	}

	@Override
	public boolean collide(Player player1, Player player2, DSurfaceParameters surface, GamePhysics game) {
		if (player1 instanceof Goalkeeper || player2 instanceof Goalkeeper)
			return false; // classic graphics can't handle goalkeepers on the ground
		if (player1.getTeam() == player2.getTeam())
			return false; // team mates do not collide
		EnumSet<PlayerState> nonBlocking = EnumSet.of(PlayerState.CELEBRATE, PlayerState.KICK, PlayerState.RUN);
		if (nonBlocking.contains(player1.getState()) && nonBlocking.contains(player2.getState()))
			// let opposing players run through each other in most cases
			return false;

		enableSoftBounce(surface);
		surface.bounce = 0.75; // affects tackling
		return true;
	}

	@Override
	public boolean collide(Ball ball, DSurfaceParameters surface, GamePhysics game) {
		enableSoftBounce(surface);
		surface.bounce = 0.5;
		ball.setDamping(0.1); // ?? can be overridden

		if (ball.getVelocity().z < -1) {
			ball.setAftertouch(false);

			// TODO: different bounce sound for ground/player/post
			try {
				SoundParser.play(SoundParser.Fx.BALL_BOUNCE);
			} catch (Exception ex) {
				log.warning(ex.getMessage());
			}
		}

		return true;
	}

	@Override
	public boolean collide(Player player, DSurfaceParameters surface, GamePhysics game) {
		enableSoftBounce(surface);
		if (player.getTilt() > Math.PI / 8) // ?? exposing more than is needed?
			surface.mu = 1000;
		return true;
	}

	@Override
	public boolean collide(Goalpost post, DSurfaceParameters surface, GamePhysics game) {
		enableSoftBounce(surface);
		surface.bounce = 0;
		surface.mu = Double.POSITIVE_INFINITY;
		return true;
	}

	private void enableSoftBounce(DSurfaceParameters surface) {
		surface.mode = OdeConstants.dContactBounce | OdeConstants.dContactSoftERP;
		surface.bounce_vel = 0.1;
	}
}
