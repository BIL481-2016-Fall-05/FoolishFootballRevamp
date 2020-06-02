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
package com.github.fommil.ff;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;

import com.github.fommil.ff.physics.Action;
import com.github.fommil.ff.physics.Aftertouch;
import com.github.fommil.ff.physics.GamePhysics;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 *
 * @author Samuel Halliday
 */
public class KeyboardController extends KeyAdapter {

	private final GamePhysics game;

	public KeyboardController(GamePhysics game) {
		this.game = game;
	}

	private final Collection<Action> actions = Sets.newHashSet();

	private final Collection<Aftertouch> aftertouches = Sets.newHashSet();

	@Override
	public synchronized void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				actions.add(Action.LEFT);
				aftertouches.add(Aftertouch.LEFT);
				break;
			case KeyEvent.VK_RIGHT:
				actions.add(Action.RIGHT);
				aftertouches.add(Aftertouch.RIGHT);
				break;
			case KeyEvent.VK_UP:
				actions.add(Action.UP);
				aftertouches.add(Aftertouch.UP);
				break;
			case KeyEvent.VK_DOWN:
				actions.add(Action.DOWN);
				aftertouches.add(Aftertouch.DOWN);
				break;
			case KeyEvent.VK_CONTROL:
				actions.add(Action.STEAL);
				break;
			case KeyEvent.VK_SPACE:
				actions.add(Action.CHANGE);
				break;
			case KeyEvent.VK_D:
				actions.add(Action.KICK);
				break;
			case KeyEvent.VK_S:
				actions.add(Action.PASS);
				break;
			case KeyEvent.VK_ENTER:
				actions.add(Action.TACKLE);
				break;
			case KeyEvent.VK_A:
				actions.add(Action.HEAD);
				break;
			default:
				return;
		}
		updateActions();
	}

	@Override
	public synchronized void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
                actions.remove(Action.LEFT);
				aftertouches.remove(Aftertouch.LEFT);
				break;
			case KeyEvent.VK_RIGHT:
				actions.remove(Action.RIGHT);
				aftertouches.remove(Aftertouch.RIGHT);
				break;
			case KeyEvent.VK_UP:
				actions.remove(Action.UP);
				aftertouches.remove(Aftertouch.UP);
				break;
			case KeyEvent.VK_DOWN:
				actions.remove(Action.DOWN);
				aftertouches.remove(Aftertouch.DOWN);
				break;
			case KeyEvent.VK_CONTROL:
				actions.remove(Action.STEAL);
				break;
			case KeyEvent.VK_SPACE:
				actions.remove(Action.CHANGE);
				break;
			case KeyEvent.VK_D:
				actions.remove(Action.KICK);
				break;
			case KeyEvent.VK_S:
				actions.remove(Action.PASS);
				break;
			case KeyEvent.VK_ENTER:
				actions.remove(Action.TACKLE);
				break;
			case KeyEvent.VK_A:
				actions.remove(Action.HEAD);
				break;
			case KeyEvent.VK_ESCAPE:
				System.exit(0);
				break;
			default:
				return;
		}
		updateActions();
	}

	private void updateActions() {
        game.setUserActions(actions, aftertouches);
	}

	private boolean directionSelected() {
		return Iterables.any(actions, new Predicate<Action>() {

			@Override
			public boolean apply(Action input) {
				switch (input) {
					case UP:
					case DOWN:
					case LEFT:
					case RIGHT:
						return true;
					default:
						return false;
				}
			}
		});
	}
}
