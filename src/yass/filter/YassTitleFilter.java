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

package yass.filter;

import yass.YassSong;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassTitleFilter extends YassFilter {

    /**
     * Gets the iD attribute of the YassTitleFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "title";
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean showTitle() {
        return false;
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean renderTitle() {
        return true;
    }


    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean accept(YassSong s) {
        String t = s.getTitle();
        if (t == null) {
            t = "";
        }

        boolean hit;

        if (rule.equals("all")) {
            hit = true;
        } else if (rule.equals("#")) {
            hit = t.length() > 0 && Character.toLowerCase(t.charAt(0)) < 'a';
        } else {
            hit = t.startsWith(rule);
        }

        return hit;
    }
}

