/*
 * Yass - Karaoke Editor
 * Copyright (C) 2009 Saruta
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package yass.renderer;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassPlayerNote {
    /**
     * Description of the Field
     */
    public final static int NOISE = -1000;
    /**
     * Description of the Field
     */
    private int height = 0;
    /**
     * Description of the Field
     */
    private double level = 0;
    /**
     * Description of the Field
     */
    private long startMillis = 0;
    /**
     * Description of the Field
     */
    private long endMillis = 0;
    /**
     * Constructor for the Note object
     *
     * @param l Description of the Parameter
     * @param m Description of the Parameter
     * @param h Description of the Parameter
     */
    public YassPlayerNote(int h, double l, long m) {
        height = h;
        level = l;
        startMillis = m;
        endMillis = m;
    }

    /**
     * Constructor for the YassPlayerNote object
     *
     * @param note Description of the Parameter
     */
    public YassPlayerNote(YassPlayerNote note) {
        height = note.height;
        level = note.level;
        startMillis = note.startMillis;
        endMillis = note.endMillis;
    }

    /**
     * Gets the none attribute of the YassPlayerNote object
     *
     * @return The none value
     */
    public boolean isNoise() {
        return height == NOISE;
    }


    /**
     * Gets the pitch attribute of the YassPlayerNote object
     *
     * @return The pitch value
     */
    public int getHeight() {
        return height;
    }


    /**
     * Sets the height attribute of the YassPlayerNote object
     *
     * @param h The new height value
     */
    public void setHeight(int h) {
        height = h;
    }


    /**
     * Gets the level attribute of the YassPlayerNote object
     *
     * @return The level value
     */
    public double getLevel() {
        return level;
    }


    /**
     * Sets the level attribute of the YassPlayerNote object
     *
     * @param l The new level value
     */
    public void setLevel(double l) {
        level = l;
    }


    /**
     * Gets the millis attribute of the YassPlayerNote object
     *
     * @return The millis value
     */
    public long getStartMillis() {
        return startMillis;
    }

    /**
     * Sets the time attribute of the YassPlayerNote object
     *
     * @param ms The new time value
     */
    public void setStartMillis(long ms) {
        startMillis = endMillis = ms;
    }

    /**
     * Gets the endMillis attribute of the YassPlayerNote object
     *
     * @return The endMillis value
     */
    public long getEndMillis() {
        return endMillis;
    }

    /**
     * Sets the endMillis attribute of the YassPlayerNote object
     *
     * @param ms The new endMillis value
     */
    public void setEndMillis(long ms) {
        endMillis = ms;
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public String toString() {
        return startMillis + ", " + endMillis + ", " + height + ", " + level;
    }
}


