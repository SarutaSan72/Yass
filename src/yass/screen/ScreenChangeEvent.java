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
 * Description of the Interface
 *
 * @author Saruta
 */
public class ScreenChangeEvent {
    private String oldScreen = null;
    private String newScreen = null;
    private YassScreen screen = null;


    /**
     * Constructor for the ScreenChangeEvent object
     *
     * @param source    Description of the Parameter
     * @param oldScreen Description of the Parameter
     * @param newScreen Description of the Parameter
     */
    public ScreenChangeEvent(YassScreen source, String oldScreen, String newScreen) {
        screen = source;
        this.oldScreen = oldScreen;
        this.newScreen = newScreen;
    }


    /**
     * Gets the source attribute of the ScreenChangeEvent object
     *
     * @return The source value
     */
    public YassScreen getSource() {
        return screen;
    }


    /**
     * Gets the oldScreen attribute of the ScreenChangeEvent object
     *
     * @return The oldScreen value
     */
    public String getOldScreen() {
        return oldScreen;
    }


    /**
     * Gets the newScreen attribute of the ScreenChangeEvent object
     *
     * @return The newScreen value
     */
    public String getNewScreen() {
        return newScreen;
    }
}

