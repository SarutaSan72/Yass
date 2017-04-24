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

import yass.YassRow;
import yass.YassSong;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassErrorsFilter extends YassFilter {

    /**
     * Gets the iD attribute of the YassErrorsFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "errors";
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
            hit = true;
        } else if (rule.equals("all_errors")) {
            hit = s.hasMessage(YassRow.getMajorMessages());
            hit = hit || s.hasMessage(YassRow.getMinorPageBreakMessages());
            hit = hit || s.hasMessage(YassRow.getTagsMessages());
            hit = hit || s.hasMessage(YassRow.getTextMessages());
            hit = hit || s.hasMessage(YassRow.getFileMessages());
        } else if (rule.equals("major_errors")) {
            hit = s.hasMessage(YassRow.getMajorMessages());
        } else if (rule.equals("page_errors")) {
            hit = s.hasMessage(YassRow.getMinorPageBreakMessages());
        } else if (rule.equals("tag_errors")) {
            hit = s.hasMessage(YassRow.getTagsMessages());
        } else if (rule.equals("text_errors")) {
            hit = s.hasMessage(YassRow.getTextMessages());
        } else if (rule.equals("file_errors")) {
            hit = s.hasMessage(YassRow.getFileMessages());
        } else if (rule.equals("critical_errors")) {
            hit = s.hasMessage(YassRow.getCriticalMessages());
        }
        return hit;
    }
}

