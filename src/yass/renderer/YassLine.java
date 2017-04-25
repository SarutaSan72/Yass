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
 * Description of the Interface
 *
 * @author Saruta
 */
public class YassLine implements Cloneable {
    private long start = 0;
    private long end = 0;
    private int firstNote = 0;
    private int lastNote = 0;
    private int minh = 0;
    private int maxh = 0;
    private double lineScore = 0;
    private int maxLineScore = 0;
    private long lineMillis = 0;
    private boolean isFreestyle = false;


    /**
     * Constructor for the YassPage object
     *
     * @param firstNoteIndex Description of the Parameter
     * @param lastNoteIndex  Description of the Parameter
     * @param minHeight      Description of the Parameter
     * @param maxHeight      Description of the Parameter
     * @param startMillis    Description of the Parameter
     * @param endMillis      Description of the Parameter
     * @param freestyle      Description of the Parameter
     * @param millis         Description of the Parameter
     */
    public YassLine(int firstNoteIndex, int lastNoteIndex, int minHeight, int maxHeight, long startMillis, long endMillis, long millis, boolean freestyle) {
        firstNote = firstNoteIndex;
        lastNote = lastNoteIndex;
        minh = minHeight;
        maxh = maxHeight;
        start = startMillis;
        end = endMillis;
        lineMillis = millis;
        isFreestyle = freestyle;
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public Object clone() {
        YassLine l = new YassLine(firstNote, lastNote, minh, maxh, start, end, lineMillis, isFreestyle);
        l.maxLineScore = maxLineScore;
        return l;
    }


    /**
     * Gets the firstNote attribute of the YassPage object
     *
     * @return The firstNote value
     */
    public int getFirstNote() {
        return firstNote;
    }


    /**
     * Gets the lastNote attribute of the YassPage object
     *
     * @return The lastNote value
     */
    public int getLastNote() {
        return lastNote;
    }


    /**
     * Gets the minHeight attribute of the YassLine object
     *
     * @return The minHeight value
     */
    public int getMinHeight() {
        return minh;
    }


    /**
     * Gets the maxHeight attribute of the YassLine object
     *
     * @return The maxHeight value
     */
    public int getMaxHeight() {
        return maxh;
    }


    /**
     * Gets the start attribute of the YassPage object
     *
     * @return The start value
     */
    public long getStartMillis() {
        return start;
    }


    /**
     * Gets the end attribute of the YassPage object
     *
     * @return The end value
     */
    public long getEndMillis() {
        return end;
    }


    /**
     * Gets the lineMillis attribute of the YassLine object
     *
     * @return The lineMillis value
     */
    public long getLineMillis() {
        return lineMillis;
    }


    /**
     * Gets the lineScore attribute of the YassLine object
     *
     * @return The lineScore value
     */
    public double getPlayerLineScore() {
        return lineScore;
    }


    /**
     * Sets the playerLineScore attribute of the YassLine object
     *
     * @param val The new playerLineScore value
     */
    public void setPlayerLineScore(double val) {
        lineScore = Math.min(maxLineScore, val);
    }


    /**
     * Gets the lineScore attribute of the YassLine object
     *
     * @return The lineScore value
     */
    public int getMaxLineScore() {
        return maxLineScore;
    }


    /**
     * Sets the playerLineScore attribute of the YassLine object
     *
     * @param val The new playerLineScore value
     */
    public void setMaxLineScore(int val) {
        maxLineScore = val;
    }


    /**
     * Gets the freestyle attribute of the YassLine object
     *
     * @return The freestyle value
     */
    public boolean isFreestyle() {
        return isFreestyle;
    }
}

