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

import com.github.fommil.ff.physics.GamePhysics;
import com.github.fommil.ff.swos.PitchParser;
import com.github.fommil.ff.swos.SpriteParser;
import com.github.fommil.ff.swos.TacticsParser;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Samuel Halliday
 * @see <a href="http://hotud.org/component/content/article/42-sport/22386">SWOS 96/97 Download</a>
 */
@SuppressWarnings("serial")
public class Main {

	public static final File SWOS = new File("../data/Sensible World of Soccer 96-97");

	/**
	 * @param args
	 * @throws IOException
	 */
	public static final void main(String[] args) throws IOException {


		Map<String, Tactics> swosTactics = TacticsParser.getSwosTactics(SWOS);
		BufferedImage pitchImage = PitchParser.getPitch(SWOS, 6);

		Map<Integer, Sprite> sprites = SpriteParser.getSprites(SWOS);

		Team a = new Team("Player");
		a.setCurrentTactics(swosTactics.get("442"));
		Team b = new Team("Opponent");
		b.setCurrentTactics(swosTactics.get("433"));
		b.setHomeKit(a.getAwayKit());
		b.setAwayKit(a.getHomeKit());

		Pitch pitch = new Pitch();
		final GamePhysics game = new GamePhysics(a, b, pitch);
		final ClassicView gv = new ClassicView(game, pitchImage, sprites);

		JFrame frame = new JFrame();
		frame.add(gv);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setTitle("Foolish Football");
		frame.setUndecorated(true);
		frame.setVisible(true);

		final long period = 10L;
		final int redraw = 5;
		TimerTask ticker = new TimerTask() {
			private final AtomicLong counter = new AtomicLong();

			@Override
			public synchronized void run() {
				game.step(period / 1000.0);
				long count = counter.incrementAndGet();
				if (count % redraw == 0)
					gv.repaint();
			}
		};
		new Timer().schedule(ticker, 0L, period);

		assert gv.getKeyListeners().length > 0;

//		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//		GraphicsDevice[] gs = ge.getScreenDevices();
//		gs[0].setFullScreenWindow(frame);
	}
}
