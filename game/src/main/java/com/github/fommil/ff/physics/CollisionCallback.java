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
import org.ode4j.ode.DBody;
import org.ode4j.ode.DContact;
import org.ode4j.ode.DContact.DSurfaceParameters;
import org.ode4j.ode.DContactBuffer;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DGeom.DNearCallback;
import org.ode4j.ode.DJoint;
import org.ode4j.ode.DJointGroup;
import org.ode4j.ode.DPlane;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

/**
 * Wrapper class that greatly simplifies the collision detection behaviour for this package.
 * <p>
 * TODO: could be made much more general by using reflection instead of a hard-coded interface.
 *
 * @author Samuel Halliday
 */
class CollisionCallback implements DNearCallback {

	interface CollisionHandler {

		boolean collide(Ball ball, Player player, DSurfaceParameters surface, GamePhysics game);

		boolean collide(Player player1, Player player2, DSurfaceParameters surface, GamePhysics game);

		boolean collide(Ball ball, DSurfaceParameters surface, GamePhysics game);

		boolean collide(Player player, DSurfaceParameters surface, GamePhysics game);

		boolean collide(Goalpost post, DSurfaceParameters surface, GamePhysics game);
	}

	private static final int MAX_CONTACTS = 8;

	private final DWorld world;

	private final DJointGroup joints;

	private final CollisionHandler handler;

	private final GamePhysics game;

	public CollisionCallback(DWorld world, DJointGroup joints, CollisionHandler handler, GamePhysics game) {
		Preconditions.checkNotNull(world);
		Preconditions.checkNotNull(joints);
		Preconditions.checkNotNull(handler);
		this.world = world;
		this.joints = joints;
		this.handler = handler;
		this.game = game;
	}

	@Override
	public void call(Object data, DGeom o1, DGeom o2) {
		Preconditions.checkNotNull(o1, "o1");
		Preconditions.checkNotNull(o2, "o2");

		DBody b1 = o1.getBody();
		DBody b2 = o2.getBody();

		Object obj1 = b1 != null ? b1.getData() : null;
		Object obj2 = b2 != null ? b2.getData() : null;

		boolean ballInvolved = obj1 instanceof Ball || obj2 instanceof Ball;
		boolean playerInvolved = obj1 instanceof Player || obj2 instanceof Player;
		boolean groundInvolved = o1 instanceof DPlane || o2 instanceof DPlane;
		boolean goalPostInvolved = obj1 instanceof Goalpost || obj2 instanceof Goalpost;

		DContactBuffer contacts = new DContactBuffer(MAX_CONTACTS);
		int numc = OdeHelper.collide(o1, o2, MAX_CONTACTS, contacts.getGeomBuffer());

		for (int i = 0; i < numc; i++) {
			DContact contact = contacts.get(i);
			DSurfaceParameters surface = contact.surface;

			// TODO: clean
			if (ballInvolved) {
				Ball ball = (Ball) (obj1 instanceof Ball ? obj1 : obj2);
				if (playerInvolved) {
					Player player = (Player) (obj1 instanceof Player ? obj1 : obj2);
					if (!handler.collide(ball, player, surface, game))
						continue;
				} else if (groundInvolved || goalPostInvolved) {
					// TODO: treat ground and goalposts differently
					if (!handler.collide(ball, surface, game))
						continue;
				} else {
					throw new UnsupportedOperationException(o1 + " " + o2);
				}
			} else if (playerInvolved) {
				Player player = (Player) (obj1 instanceof Player ? obj1 : obj2);
				if (obj1 instanceof Player && obj2 instanceof Player) {
					Player player2 = (Player) obj2;
					if (!handler.collide(player, player2, surface, game))
						continue;
				} else if (groundInvolved || goalPostInvolved) {
					if (!handler.collide(player, surface, game))
						continue;
				} else {
					throw new UnsupportedOperationException(o1 + " " + o2);
				}
			} else if (goalPostInvolved) {
				assert groundInvolved : obj1 + " " + obj2;
				Goalpost post = (Goalpost) (obj1 instanceof Goalpost ? obj1 : obj2);
				if (!handler.collide(post, surface, game))
					continue;
			}

			DJoint c = OdeHelper.createContactJoint(world, joints, contact);
			c.attach(b1, b2);
		}
	}
}
