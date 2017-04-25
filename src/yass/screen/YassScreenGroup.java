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

import java.util.Vector;

/**
 * Description of the Interface
 *
 * @author Saruta
 */
public class YassScreenGroup {
    private String group = null;
    private String rule = null;
    private Vector<?> matches = null;


    /**
     * Constructor for the YassScreenGroup object
     *
     * @param rule    Description of the Parameter
     * @param matches Description of the Parameter
     * @param group   Description of the Parameter
     */
    public YassScreenGroup(String group, String rule, Vector<?> matches) {
        this.group = group;
        this.rule = rule;
        this.matches = matches;
    }


    /**
     * Gets the artist attribute of the YassSongData object
     *
     * @return The artist value
     */
    public String getTitle() {
        return group;
    }


    /**
     * Gets the filter attribute of the YassScreenGroup object
     *
     * @return The filter value
     */
    public String getFilter() {
        return rule;
    }


    /**
     * Gets the title attribute of the YassSongData object
     *
     * @return The title value
     */
    public Vector<?> getSongs() {
        return matches;
    }


    /**
     * Gets the songAt attribute of the YassScreenGroup object
     *
     * @param i Description of the Parameter
     * @return The songAt value
     */
    public int getSongAt(int i) {
        return ((Integer) matches.elementAt(i)).intValue();
    }
}

