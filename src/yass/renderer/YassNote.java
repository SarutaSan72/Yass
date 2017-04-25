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
public class YassNote implements Cloneable {
    /**
     * Description of the Field
     */
    public final static int NORMAL = 0;
    private int type = NORMAL;
    /**
     * Description of the Field
     */
    public final static int GOLDEN = 1;
    /**
     * Description of the Field
     */
    public final static int FREESTYLE = 2;
    private int beat = 0;
    private int length = 1;
    private int height = 0;
    private String txt = "";

    private long start = 0;
    private long end = 0;

    private double noteScore = 0;
    private double goldenScore = 0;
    private int maxNoteScore = 0;
    private int maxGoldenScore = 0;


    /**
     * Constructor for the YassNote object
     *
     * @param type        Description of the Parameter
     * @param beat        Description of the Parameter
     * @param length      Description of the Parameter
     * @param height      Description of the Parameter
     * @param txt         Description of the Parameter
     * @param startMillis Description of the Parameter
     * @param endMillis   Description of the Parameter
     */
    public YassNote(int type, int beat, int length, int height, String txt, long startMillis, long endMillis) {
        this.type = type;
        this.beat = beat;
        this.length = length;
        this.height = height;
        this.txt = txt;
        start = startMillis;
        end = endMillis;
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public Object clone() {
        YassNote n = new YassNote(type, beat, length, height, txt, start, end);
        n.maxNoteScore = maxNoteScore;
        n.maxGoldenScore = maxGoldenScore;
        return n;
    }


    /**
     * Gets the beat attribute of the YassNote object
     *
     * @return The beat value
     */
    public int getBeat() {
        return beat;
    }


    /**
     * Gets the length attribute of the YassNote object
     *
     * @return The length value
     */
    public int getLength() {
        return length;
    }


    /**
     * Gets the startMillis attribute of the YassNote object
     *
     * @return The startMillis value
     */
    public long getStartMillis() {
        return start;
    }


    /**
     * Gets the endMillis attribute of the YassNote object
     *
     * @return The endMillis value
     */
    public long getEndMillis() {
        return end;
    }


    /**
     * Gets the height attribute of the YassNote object
     *
     * @return The height value
     */
    public int getHeight() {
        return height;
    }


    /**
     * Gets the type attribute of the YassNote object
     *
     * @return The type value
     */
    public int getType() {
        return type;
    }


    /**
     * Gets the text attribute of the YassNote object
     *
     * @return The text value
     */
    public String getText() {
        return txt;
    }


    /**
     * Gets the playerNoteScore attribute of the YassNote object
     *
     * @return The playerNoteScore value
     */
    public double getPlayerNoteScore() {
        return noteScore;
    }


    /**
     * Sets the playerLineScore attribute of the YassLine object
     *
     * @param val The new playerLineScore value
     */
    public void setPlayerNoteScore(double val) {
        noteScore = Math.min(maxNoteScore, val);
    }


    /**
     * Gets the playerGoldenScore attribute of the YassNote object
     *
     * @return The playerGoldenScore value
     */
    public double getPlayerGoldenScore() {
        return goldenScore;
    }


    /**
     * Sets the playerLineScore attribute of the YassLine object
     *
     * @param val The new playerLineScore value
     */
    public void setPlayerGoldenScore(double val) {
        goldenScore = Math.min(maxGoldenScore, val);
    }


    /**
     * Gets the maxScore attribute of the YassLine object
     *
     * @return The maxScore value
     */
    public int getMaxNoteScore() {
        return maxNoteScore;
    }


    /**
     * Sets the maxLineScore attribute of the YassLine object
     *
     * @param val The new maxLineScore value
     */
    public void setMaxNoteScore(int val) {
        maxNoteScore = val;
    }


    /**
     * Gets the maxScore attribute of the YassLine object
     *
     * @return The maxScore value
     */
    public int getMaxGoldenScore() {
        return maxGoldenScore;
    }


    /**
     * Sets the maxLineScore attribute of the YassLine object
     *
     * @param val The new maxLineScore value
     */
    public void setMaxGoldenScore(int val) {
        maxGoldenScore = val;
    }
}

