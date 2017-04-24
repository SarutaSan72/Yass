/*
 * Yass - Karaoke Editor
 * Copyright (C) 2014 Saruta
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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassGenreFilter extends YassFilter {

    /**
     * Gets the iD attribute of the YassGenreFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "genre";
    }


    /**
     * Gets the genericRules attribute of the YassGenreFilter object
     *
     * @param data Description of the Parameter
     * @return The genericRules value
     */
    public String[] getGenericRules(Vector<YassSong> data) {
        Vector<String> genres = new Vector<>();
        for (Enumeration<YassSong> e = data.elements(); e.hasMoreElements(); ) {
            YassSong s = e.nextElement();
            String genre = s.getGenre();
            if (genre == null || genre.length() < 1) {
                continue;
            }
            if (!genres.contains(genre)) {
                genres.addElement(genre);

            }
        }
        Collections.sort(genres);

        return genres.toArray(new String[genres.size()]);
    }


    /**
     * Description of the Method
     *
     * @param rule Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean allowDrop(String rule) {
        if (rule.equals("all")) {
            return false;
        }
        if (rule.equals("unspecified")) {
            return false;
        }
        return true;
    }


    /**
     * Description of the Method
     *
     * @param rule Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean allowCoverDrop(String rule) {
        if (rule.equals("all")) {
            return false;
        }
        if (rule.equals("unspecified")) {
            return false;
        }
        return true;
    }


    /**
     * Description of the Method
     *
     * @param rule Description of the Parameter
     * @param s    Description of the Parameter
     */
    public void drop(String rule, YassSong s) {
        String old = s.getGenre();
        if (old == null || old.equals(rule)) {
            return;
        }

        s.setGenre(rule);
        s.setSaved(false);
    }


    /**
     * Gets the extraInfo attribute of the YassGenreFilter object
     *
     * @return The extraInfo value
     */
    public int getExtraInfo() {
        return YassSongList.GENRE_COLUMN;
    }


    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean accept(YassSong s) {
        String t = s.getGenre();
        boolean hit = false;

        if (rule.equals("all")) {
            hit = true;
        } else if (rule.equals("unspecified")) {
            if (t == null || t.length() < 1) {
                hit = true;
            }
        } else {
            hit = t.equals(rule);
        }

        return hit;
    }
}

