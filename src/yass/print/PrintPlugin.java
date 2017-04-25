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

package yass.print;

import yass.YassSong;

import javax.swing.*;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Description of the Interface
 *
 * @author Saruta
 */
public interface PrintPlugin {
    /**
     * Gets the title attribute of the PrintPlugin object
     *
     * @return The title value
     */
    public String getTitle();


    /**
     * Gets the description attribute of the PrintPlugin object
     *
     * @return The description value
     */
    public String getDescription();


    /**
     * Gets the control attribute of the PrintPlugin object
     *
     * @return The control value
     */
    public JPanel getControl();


    /**
     * Description of the Method
     *
     * @param songList Description of the Parameter
     * @param filename Description of the Parameter
     * @param options  Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean print(Vector<YassSong> songList, String filename, Hashtable<String, Object> options);


    /**
     * Description of the Method
     *
     * @param options Description of the Parameter
     */
    public void commit(Hashtable<String, Object> options);
}

