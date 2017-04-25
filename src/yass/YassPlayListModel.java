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

import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassPlayListModel extends Vector<Object> implements Comparable<Object> {
    private static final long serialVersionUID = 3473486648850642487L;
    String name = null;
    String filename = null;


    /**
     * Constructor for the PlayListModel object
     */
    public YassPlayListModel() {
        super();
        name = "";
    }

    /**
     * Gets the name attribute of the PlayListModel object
     *
     * @return The name value
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name attribute of the PlayListModel object
     *
     * @param s The new name value
     */
    public void setName(String s) {
        name = s;
    }

    /**
     * Gets the fileName attribute of the YassPlayListModel object
     *
     * @return The fileName value
     */
    public String getFileName() {
        return filename;
    }

    /**
     * Sets the fileName attribute of the YassPlayListModel object
     *
     * @param s The new fileName value
     */
    public void setFileName(String s) {
        filename = s;
    }

    /**
     * Description of the Method
     *
     * @param o Description of the Parameter
     * @return Description of the Return Value
     */
    public int compareTo(Object o) {
        return getName().compareTo(((YassPlayListModel) o).getName());
    }
}


