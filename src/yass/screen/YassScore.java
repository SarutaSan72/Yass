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

package yass.screen;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassScore implements Comparable<Object> {
    private String artist = null;
    private String title = null;
    private String user = null;
    private int score = 0;
    private int difficulty = 0;


    /**
     * Constructor for the YassScore object
     *
     * @param artist     Description of the Parameter
     * @param title      Description of the Parameter
     * @param user       Description of the Parameter
     * @param score      Description of the Parameter
     * @param difficulty Description of the Parameter
     */
    public YassScore(String artist, String title, String user, int score, int difficulty) {
        this.artist = artist;
        this.title = title;
        this.user = user;
        this.score = score;
        this.difficulty = difficulty;
    }


    /**
     * Gets the artist attribute of the YassScore object
     *
     * @return The artist value
     */
    public String getArtist() {
        return artist;
    }


    /**
     * Gets the title attribute of the YassScore object
     *
     * @return The title value
     */
    public String getTitle() {
        return title;
    }


    /**
     * Gets the player attribute of the YassScore object
     *
     * @return The player value
     */
    public String getPlayer() {
        return user;
    }


    /**
     * Gets the score attribute of the YassScore object
     *
     * @return The score value
     */
    public int getScore() {
        return score;
    }


    /**
     * Gets the difficulty attribute of the YassScore object
     *
     * @return The difficulty value
     */
    public int getDifficulty() {
        return difficulty;
    }


    /**
     * Description of the Method
     *
     * @param o Description of the Parameter
     * @return Description of the Return Value
     */
    public int compareTo(Object o) {
        YassScore s = (YassScore) o;
        if (score < s.score) {
            return 1;
        }
        if (score > s.score) {
            return -1;
        }
        return 0;
    }
}

