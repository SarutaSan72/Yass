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
import yass.YassSongList;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassMultiPlayerFilter extends YassFilter {

    /**
     * Gets the iD attribute of the YassMultiPlayerFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "duets";
    }


    /**
     * Gets the extraInfo attribute of the YassMultiPlayerFilter object
     *
     * @return The extraInfo value
     */
    public int getExtraInfo() {
        return YassSongList.DUETSINGER_COLUMN;
    }


    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean accept(YassSong s) {
        boolean hit = false;

        if (rule.equals("all")) {
            return true;
        }

        int pn;
        String p = s.getMultiplayer();
        try {
            pn = Integer.parseInt(p);
        } catch (Exception e) {
            pn = 1;
        }

        if (rule.equals("solos")) {
            hit = pn == 1;
        } else if (rule.equals("duets")) {
            hit = pn == 2;
        } else if (rule.equals("trios")) {
            hit = pn == 3;

        } else if (rule.equals("quartets")) {
            hit = pn == 4;
        } else if (rule.equals("choirs")) {
            hit = pn > 4;
        }

        return hit;
    }
}

