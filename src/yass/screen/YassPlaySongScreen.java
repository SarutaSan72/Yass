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
public class YassPlaySongScreen extends YassScreen {
    private static final long serialVersionUID = 4243370261445350556L;


    /**
     * Gets the iD attribute of the YassScoreScreen object
     *
     * @return The iD value
     */
    public String getID() {
        return "playsong";
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public String nextScreen() {
        return "enterscore";
    }


    /**
     * Description of the Method
     */
    public void init() {
    }


    /**
     * Description of the Method
     */
    public void show() {
        unloadBackgroundImage("start_background.jpg");
        unloadBackgroundImage("plain_background.jpg");
    }


    /**
     * Description of the Method
     */
    public void hide() {
    }
}

