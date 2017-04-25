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

package yass;

import java.util.EventObject;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassSongListEvent extends EventObject {
    /**
     * Description of the Field
     */
    public final static int STARTING = 0;
    /**
     * Description of the Field
     */
    public final static int SEARCHING = 1;
    /**
     * Description of the Field
     */
    public final static int UPDATING = 2;
    /**
     * Description of the Field
     */
    public final static int LOADING = 3;
    /**
     * Description of the Field
     */
    public final static int LOADED = 4;
    /**
     * Description of the Field
     */
    public final static int THUMBNAILING = 5;
    /**
     * Description of the Field
     */
    public final static int FINISHED = 6;
    private static final long serialVersionUID = -7232684562486258377L;
    int state = 0;


    /**
     * Constructor for the YassSongListEvent object
     *
     * @param src   Description of the Parameter
     * @param state Description of the Parameter
     */
    public YassSongListEvent(Object src, int state) {
        super(src);
        this.state = state;
    }


    /**
     * Gets the state attribute of the YassSongListEvent object
     *
     * @return The state value
     */
    public int getState() {
        return state;
    }
}

