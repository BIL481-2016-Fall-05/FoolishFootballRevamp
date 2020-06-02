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
package com.github.fommil.ff.swos;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

import com.github.fommil.ff.Main;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Reads the SWOS commentary and SFX files into a Java compatible format.
 *
 * @author Samuel Halliday
 * @see <a href="http://swos.hajas.org/sounds.htm">Hajas SWOS Brasil</a>
 */
public class SoundParser {

	private static final Logger log = Logger.getLogger(SoundParser.class.getName());

	private static final Map<Fx, byte[]> fxCache = Maps.newEnumMap(Fx.class);

	private static final Map<Fx, Date> playing = Maps.newEnumMap(Fx.class);

	/**
	 * @param fx
	 * @throws IOException
	 * @throws LineUnavailableException
	 *
	 * @deprecated because this is a hack
	 */
	@Deprecated
	public static void play(final Fx fx) {
		Preconditions.checkNotNull(fx);

		if (playing.containsKey(fx))
			return;

		playing.put(fx, new Date());
		Thread player = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					byte[] bytes;
					if (!fxCache.containsKey(fx)) {
						String filename = fx.getFilename();
						File file = new File(Main.SWOS + filename);
						Preconditions.checkArgument(file.exists());
						bytes = SwosUtils.getBytes(file);
						fxCache.put(fx, bytes);
					} else {
						bytes = fxCache.get(fx);
					}
					ByteArrayInputStream in = new ByteArrayInputStream(bytes);
					AudioInputStream audio = new AudioInputStream(in, SWOS_RAW_FORMAT, bytes.length);
					Clip clip = AudioSystem.getClip();
					clip.open(audio);
					clip.addLineListener(new LineListener() {

						@Override
						public void update(LineEvent event) {
							if (event.getType() == LineEvent.Type.STOP) {
								playing.remove(fx);
							}
						}
					});
					clip.start();
					clip.drain();
				} catch (Exception e) {
					log.warning(fx.getFilename());
				}
			}
		});
		player.start();
	}

	private static final AudioFormat SWOS_RAW_FORMAT = new AudioFormat(22000, 8, 1, false, true);

	/**
	 * Index for various types of commentary sound files.
	 */
	public enum Commentary {

		GOALS("/SFX/PIERCE/", "M158_1_", "M158_2_", "M158_3_", "M158_4_", "M158_5_", "M158_7_"),
		ON_THE_POST("/SFX/PIERCE/", "M10_f_", "M10_g_", "M10_i_", "M10_s_", "M10_t_", "M10_u_"),
		NEAR_MISSES("/SFX/PIERCE/", "M10_d_", "M10_e_", "M10_j_", "M10_k_", "M10_r_"),
		OWN_GOALS("/SFX/PIERCE/", "M158_p_", "M158_q_", "M158_s_", "M158_t_", "M158_x_", "M158_y_"),
		CLAIMED_BY_THE_KEEPER("/SFX/PIERCE/", "M313_6_", "M313_8_", "M313_9_", "M313_c_", "M313_d_", "M313_g_"),
		SAVES_BY_THE_KEEPER("/SFX/PIERCE/", "M196_w_", "M196_x_", "M196_z_", "M233_1_", "M313_7_", "M313_a_", "M313_b_", "M313_h_"), // "M313_e_" bad sampling in original)
		PENALTIES("/SFX/PIERCE/", "M196_k_", "M196_l_", "M196_m_", "M196_n_"),
		SCORED_PENALTIES("/SFX/PIERCE/", "M233_7_", "M233_8_", "M233_9_", "M233_c_"),
		MISSED_PENALTIES("/SFX/PIERCE/", "M233_2_", "M233_4_", "M233_5_", "M233_6_"),
		SAVED_PENALTIES("/SFX/PIERCE/", "M196_t_", "M196_u_", "M196_v_"),
		GOOD_PASSES("/SFX/PIERCE/", "M313_i_", "M313_j_", "M313_o_"),
		GOOD_PLAY("/SFX/PIERCE/", "M349_4_", "M349_7_", "M349_8_", "M349_e_", "M365_1_"),
		FOULS("/SFX/PIERCE/", "M158_z_", "M195", "M196_1_", "M196_2_", "M196_4_", "M196_6_", "M196_7_"),
		GOOD_JUMPING_HEADERS("/SFX/PIERCE/", "M33_2_", "M33_3_", "M33_4_", "M33_5_", "M33_6_", "M33_8_", "M33_a_", "M33_b_"),
		DISPUTED_REFEREE("/SFX/PIERCE/", "M158__"),
		TACTICS("/HARD/", "M10_5_", "M10_7_", "M10_8_", "M10_9_", "M10_a_", "M10_b_"),
		SUBSTITUTE("/HARD/", "M233_j_", "M233_k_", "M233_l_", "M233_m_", "M10_3_", "M10_4_"),
		THROW_IN("/HARD/", "M406_8_", "M406_9_", "M406_3_", "M406_7_"),
		CORNERS("/HARD/", "M10_v_", "M10_w_", "M10_x_", "M10_y_", "M10_z_", "M313_1_", "M313_2_", "M313_3_"),
		YELLOW_CARD("/HARD/", "M443_7_", "M443_8_", "M443_9_", "M443_a_", "M443_b_", "M443_c_", "M443_d_", "M443_e_", "M443_f_", "M443_g_", "M443_h_", "M443_i_", "M443_j_"),
		RED_CARD("/HARD/", "M196_8_", "M196_9_", "M196_a_", "M196_b_", "M196_c_", "M196_d_", "M196_e_", "M196_f_", "M196_g_", "M196_h_", "M196_i_", "M196_j_"),
		END_GAME("/HARD/", "M406_f_", "M406_g_", "M406_h_", "M406_i_", "M406_j_");

		private final String directory;

		private final String[] filenames;

		private Commentary(String directory, String... filenames) {
			this.directory = directory;
			this.filenames = filenames;
		}

		/**
		 * @return the filename of the sounds files associated to this commentary, relative to the
		 * SWOS installation.
		 */
		List<String> getFilenames() {
			List<String> names = Arrays.asList(filenames);
			Iterable<String> fullNames = Iterables.transform(names, new Function<String, String>() {

				@Override
				public String apply(String from) {
					return directory + from + ".RAW";
				}
			});
			return Lists.newArrayList(fullNames);
		}
	}

	enum ManagerVoiceover {
		// TODO: document the SFX/ENGLISH files, used for voice overs in the management game
	}

	enum Flc {
		// TODO: document the SFX/FLC files, used for ambient sounds tied to results
	}

	public enum Fx {
		// TODO: document the FX files, used for sound effects during play

		WHISTLE_FOUL("FOUL"),
		WHISTLE_1("REF_WH"),
		WHISTLE_2("WHISTLE"),
		WHISTLE_ENDGAME("ENDGAMEW"),
		BALL_BOUNCE("BOUNCEX"),
		BALL_KICK("KICKX"),
		CROWD_AWAYSUBL("AWAYSUBL"),
		CROWD_BGCRD3L("BGCRD3L"),
		CROWD_BOOWHISL("BOOWHISL"),
		CROWD_CHANT10L("CHANT10L"),
		CROWD_CHANT4L("CHANT4L"),
		CROWD_CHANT8L("CHANT8L"),
		CROWD_CHEER("CHEER"),
		CROWD_COMBLUE("COMBLUE"),
		CROWD_COMBROWN("COMBROWN"),
		CROWD_COMGREEN("COMGREEN"),
		CROWD_COMRED("COMRED"),
		CROWD_COMWHITE("COMWHITE"),
		CROWD_COMYELO("COMYELO"),
		CROWD_EASY("EASY"),
		CROWD_EREWEGO("EREWEGO"),
		CROWD_GOALKICK("GOALKICK"),
		CROWD_HOMEGOAL("HOMEGOAL"),
		CROWD_HOMESUBL("HOMESUBL"),
		CROWD_HOMEWINL("HOMEWINL"),
		CROWD_MISSGOAL("MISSGOAL"),
		CROWD_NOTSING("NOTSING"),
		CROWD_ONENIL("ONENIL"),
		CROWD_TWONIL("TWONIL"),
		CROWD_THREENIL("THREENIL"),
		CROWD_FOURNIL("FOURNIL"),
		CROWD_FIVENIL("FIVENIL");

		private final String filename;

		Fx(String filename) {
			this.filename = filename;
		}

		String getFilename() {
			return "/SFX/FX/" + this.filename + ".RAW";
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static final void main(String[] args) throws Exception {

		//for (String filename : Commentary.END_GAME.getFilenames()) {
		for (Fx fx : Fx.values()) {
			System.out.println(fx.name());
			play(fx);
		}
	}
}
