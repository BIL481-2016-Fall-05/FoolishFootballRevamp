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

import com.github.fommil.ff.*;
import com.github.fommil.ff.swos.PitchParser;
import com.github.fommil.ff.swos.SpriteParser;
import com.github.fommil.ff.swos.TacticsParser;
import org.ode4j.drawstuff.DrawStuff;
import org.ode4j.drawstuff.DrawStuff.dsFunctions;
import org.ode4j.ode.DBox;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DSphere;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A debugging version of the game using a 3D rendered view.
 * <p>
 * Don't forget to extract the native libs and pass -Djava.library.path=native
 *
 * @author Samuel Halliday
 */
public class GamePhysicsGL extends dsFunctions {

	private static final Logger log = Logger.getLogger(GamePhysicsGL.class.getName());

	/** @param args */
	public static final void main(String[] args) throws Exception {
		final int width = 600;
		final int height = 400;
		Team a = new Team("Player");
		a.setCurrentTactics(TacticsParser.getSwosTactics(Main.SWOS).get("442"));
		Pitch pitch = new Pitch();
		Team b = new Team("Opponent");
		b.setCurrentTactics(TacticsParser.getSwosTactics(Main.SWOS).get("433"));
		b.setHomeKit(a.getAwayKit());
		b.setAwayKit(a.getHomeKit());

		GamePhysics game = new GamePhysics(a, b, pitch);
		Position ballStart = pitch.getPenaltySpotBottom();
		game.getBall().setPosition(ballStart);

		GamePhysicsGL demo = new GamePhysicsGL(game);
		DrawStuff.dsSimulationLoop(args, width, height, demo);
	}

	private final GamePhysics game;

	private final LwjglKeyboardController controller;

	private ClassicView gv;

	private GamePhysicsGL(GamePhysics game) {
		this.game = game;
		this.controller = new LwjglKeyboardController(game);
	}

	@Override
	public void start() {
		BufferedImage pitchImage;
		try {
			pitchImage = PitchParser.getPitch(Main.SWOS, 6);
			Map<Integer, Sprite> sprites = SpriteParser.getSprites(Main.SWOS);
			gv = new ClassicView(game, pitchImage, sprites);
			JFrame frame = new JFrame();
			frame.add(gv);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(400, 600);
			frame.setLocation(800, 200);
			frame.setTitle("Foolish Football");
			frame.setVisible(true);
		} catch (IOException ex) {
			log.log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void step(boolean pause) {
		controller.poll();

		game.step(0.017); // ?? about 60Hz

		Position c = game.getBall().getPosition();
		float[] xyz = {(float) c.x, (float) c.y - 5, 15f};
		float[] hpr = {90, -70, 0}; // ?? heading definition wrong in drawStuff API
		DrawStuff.dsSetViewpoint(xyz, hpr);

		for (DGeom geom : game.getGeoms()) {
			// TODO: draw different colours for the teams/ball/goalies, etc
			draw(geom, Color.RED);
		}
		gv.repaint();
	}

	@Override
	public void command(char cmd) {
	}

	@Override
	public void stop() {
	}

	private void draw(DGeom geometry, Color c) {
		float[] color = c.getColorComponents(new float[3]);
		DrawStuff.dsSetColor(color[0], color[1], color[2]);
		if (geometry instanceof DBox) {
			DBox box = (DBox) geometry;
			DrawStuff.dsDrawBox(box.getPosition(), box.getRotation(), box.getLengths());
		} else if (geometry instanceof DSphere) {
			DSphere sphere = (DSphere) geometry;
			DrawStuff.dsDrawSphere(sphere.getPosition(), sphere.getRotation(), (float) sphere.getRadius());
		}
	}

	@Override
	public void dsPrintHelp() {
	}
}
