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
package com.github.fommil.ff;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.awt.Color;
import java.util.Collections;
import java.util.List;

/**
 * Represents a football team, including the list of available {@link Player}s and
 * {@link Tactics}.
 *
 * @author Samuel Halliday
 */
public class Team {
	private final String  name;

	public static class Colours {

		private final Color primary;

		private final Color secondary;

		private final Color shorts;

		private final Color socks;

		/**
		 * @param primary
		 * @param secondary
		 * @param shorts
		 * @param socks
		 */
		public Colours(Color primary, Color secondary, Color shorts, Color socks) {
			this.primary = primary;
			this.secondary = secondary;
			this.shorts = shorts;
			this.socks = socks;
		}

		// <editor-fold defaultstate="collapsed" desc="BOILERPLATE GETTERS/SETTERS">
		public Color getPrimary() {
			return primary;
		}

		public Color getSecondary() {
			return secondary;
		}

		public Color getShorts() {
			return shorts;
		}

		public Color getSocks() {
			return socks;
		}
		// </editor-fold>
	}

	private Tactics current;

	private List<PlayerStats> players = Lists.newArrayList();

	private Colours homeKit = new Colours(Color.RED, Color.RED, Color.WHITE, Color.RED);
	private Colours awayKit = new Colours(Color.BLUE, Color.BLUE, Color.WHITE, Color.BLUE);

	/** */
	public Team(String name) {
		for (int i = 0; i < 20; i++) {
			players.add(new PlayerStats());
		}
		this.name = name;
	}

	/**
	 * @return the currently chosen tactics.
	 */
	public Tactics getCurrentTactics() {
		return current;
	}

	public void setCurrentTactics(Tactics tactics) {
		this.current = tactics;
	}

	/**
	 * @return
	 */
	public List<PlayerStats> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	public String getName() {
		return name;
	}

	// <editor-fold defaultstate="collapsed" desc="BOILERPLATE GETTERS/SETTERS">
	public Colours getAwayKit() {
		return awayKit;
	}

	public void setAwayKit(Colours awayKit) {
		this.awayKit = awayKit;
	}

	public Colours getHomeKit() {
		return homeKit;
	}

	public void setHomeKit(Colours homeKit) {
		this.homeKit = homeKit;
	}
	// </editor-fold>
}
