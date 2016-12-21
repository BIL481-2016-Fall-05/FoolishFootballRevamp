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

import java.awt.Point;
import java.awt.Rectangle;
import com.github.fommil.ff.physics.Position;

/**
 * This is a container that is tied to the pixel values of features from the SWOS pitch graphics. A
 * ratio is used to convert pixels into meters.
 * <p>
 * The pitches are (672, 880).
 * <p>
 * The location of the corner flags are pixels (81, 129), (590, 129), (590, 769),
 * (81, 769).
 * <p>
 * The location of the goal nets are (300, 769), (300, 751), (371, 751),
 * (371, 769) and (371, 129), (371, 111), (300, 111), (300, 129).
 * (Note that the posts are two pixels wide, and these correspond to the leftmost pixels).
 * <p>
 * The locations of the penalty boxes are (193, 129), (478, 129), (193, 216),
 * (478, 216) and (193, 769), (193, 682), (478, 682), (478, 769).
 * <p>
 * The locations of the goal boxes are (273, 769), (273, 740), (398, 740), (398, 769)
 * and (273, 129), (273, 158), (398, 129), (398, 158).
 * <p>
 * The penalty spots are (336, 711) and (336, 187).
 * <p>
 * The centre spot is (336, 449).

 * @author Samuel Halliday
 * @author Doga Can Yanikoglu
 */
public class Pitch {
    private static final double SCALE = 0.1;

    private static final Point bounds = new Point(672, 880);

    private static final Rectangle pitch = new Rectangle(81, 129, 509, 640);

    private static final Point centreSpot = new Point(336, 449);

    public final Area leftBack = new Area(8,19,75,54);
    public final Area rightBack = new Area(48,59,75,54);
    public final Area centralBack = new Area(19,48,75,54);

    public final Area leftWingBack = new Area(8,19,54,43);
    public final Area rightWingBack = new Area(48,59,54,43);
    public final Area defensiveMid = new Area(19,48,54,43);

    public final Area leftWing = new Area(8,19,43,11);
    public final Area rightWing = new Area(48,59,43,11);

    public final Area attackerMid = new Area(19,48,43,31);
    public final Area forward = new Area(19,48,31,11);

    public final Area centre = new Area(27,40,48,38);

    private final Point penaltySpotTop = new Point(336, 187);

    private final Point penaltySpotBottom = new Point(336, 711);

	public class Area {
		private double leftBound, rightBound, upperBound, lowerBound;

        /**
         * Constructor for Area Class
         * @param leftBound Leftmost coordinate of area
         * @param rightBound Rightmost coordinate of area
         * @param upperBound Top coordinate of area
         * @param lowerBound Bottom coordinate of area
         */
        Area(double leftBound, double rightBound, double upperBound, double lowerBound) {
			this.leftBound = leftBound;
			this.rightBound = rightBound;
			this.upperBound = upperBound;
			this.lowerBound = lowerBound;
		}

        /**
         * Checks if given position is inside of this area
         * @param p Position to be checked
         */
		public boolean isInside(Position p) {
			if(p.x <= rightBound && p.x >= leftBound && p.y <= upperBound && p.y >= lowerBound)
				return true;
			else
				return false;
		}

		/**
		 * Returns specified position in this area
		 */
        public Position topPos() {
            return new Position((leftBound + rightBound) / 2,upperBound - 3,0);
        }
		public Position bottomPos() {
            return new Position((leftBound + rightBound) / 2,lowerBound + 3,0);
        }
        public Position leftPos() {
            return new Position(leftBound + 2,(lowerBound + upperBound) / 2,0);
        }
        public Position rightPos() {
            return new Position(rightBound - 2,(lowerBound + upperBound) / 2,0);
        }
        public Position leftBottomPos() {
            return new Position(leftBound + 2,lowerBound + 2,0);
        }
        public Position rightBottomPos() {
            return new Position(rightBound - 2,lowerBound + 2,0);
        }
        public Position leftTopPos() {
            return new Position(leftBound + 2,upperBound - 2,0);
        }
        public Position rightTopPos() {
            return new Position(rightBound - 2,upperBound - 2,0);
        }
        public Position centerPos() {
            return new Position((rightBound+leftBound)/2,(upperBound+lowerBound)/2,0);
        }
	}

	public Position getPitchLowerLeft() {
		return new Position(pitch.x * SCALE, (bounds.y - pitch.y - pitch.height) * SCALE, 0);
	}

	public Position getPitchUpperRight() {
		return new Position((pitch.x + pitch.width) * SCALE, (bounds.y - pitch.y) * SCALE, 0);
	}

	public Position getCentre() {
		return new Position(centreSpot.x * SCALE, (bounds.y - centreSpot.y) * SCALE, 0);
	}

	public double getGoalWidth() {
		return 71 * SCALE;
	}

	public double getGoalHeight() {
		return 3;
	}

	public double getGoalDepth() {
		return 12 * SCALE;
	}

	public double getGoalThickness() {
		return 2 * SCALE;
	}

	public Position getGoalTop() {
		return new Position(SCALE * (300 + 71 / 2.0), SCALE * (bounds.y - 117 - 12 / 2.0), 0);
	}

	public Position getGoalBottom() {
		return new Position(SCALE * (300 + 71 / 2.0), SCALE * (bounds.y - 769 - 12 / 2.0), 0);
	}

	public Position getPenaltySpotTop() {
		return pointToPosition(penaltySpotTop);
	}

	public Position getPenaltySpotBottom() {
		return pointToPosition(penaltySpotBottom);
	}

	private Position pointToPosition(Point point) {
		return new Position(SCALE * point.x, SCALE * (bounds.y - point.y), 0);
	}

	@Deprecated // implementation detail
	public double getScale() {
		return SCALE;
	}
}
