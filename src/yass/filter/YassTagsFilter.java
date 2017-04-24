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

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassTagsFilter extends YassFilter {

    /**
     * Gets the iD attribute of the YassMultiPlayerFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "tags";
    }


    /**
     * Gets the extraInfo attribute of the YassMultiPlayerFilter object
     *
     * @return The extraInfo value
     */
    public int getExtraInfo() {
        return YassSongList.FOLDER_COLUMN;
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
        if (rule.equals("medleystartbeat")) {
            String tag = s.getMedleyStartBeat();
            hit = tag != null && tag.trim().length() > 1;
        } else if (rule.equals("previewstart")) {
            String tag = s.getPreviewStart();
            hit = tag != null && tag.trim().length() > 1;
        } else if (rule.equals("start")) {
            String tag = s.getStart();
            hit = tag != null && tag.trim().length() > 1;
        } else if (rule.equals("end")) {
            String tag = s.getEnd();
            hit = tag != null && tag.trim().length() > 1;
        } else if (rule.equals("relative")) {
            String r = s.getRelative();
            hit = r != null && (r.equalsIgnoreCase("yes") || r.equalsIgnoreCase("true"));
        } else if (rule.equals("gap>60")) {
            String g = s.getGap();
            if (g != null) {
                g = g.replace(',', '.');
            }
            try {
                double d = Double.parseDouble(g);
                hit = d >= 60000;
            } catch (Exception ignored) {
            }
        } else if (rule.equals("gap>30")) {
            String g = s.getGap();
            if (g != null) {
                g = g.replace(',', '.');
            }
            try {
                double d = Double.parseDouble(g);
                hit = d >= 30000;
            } catch (Exception ignored) {
            }
        } else if (rule.equals("gap<10")) {
            String g = s.getGap();
            if (g != null) {
                g = g.replace(',', '.');
            }
            try {
                double d = Double.parseDouble(g);
                hit = d < 10000;
            } catch (Exception ignored) {
            }
        } else if (rule.equals("gap<5")) {
            String g = s.getGap();
            if (g != null) {
                g = g.replace(',', '.');
            }
            try {
                double d = Double.parseDouble(g);
                hit = d < 5000;
            } catch (Exception ignored) {
            }
        } else if (rule.equals("bpm<200")) {
            String g = s.getBPM();
            if (g != null) {
                g = g.replace(',', '.');
            }
            try {
                double d = Double.parseDouble(g);
                hit = d < 200;
            } catch (Exception ignored) {
            }
        } else if (rule.equals("bpm>400")) {
            String g = s.getBPM();
            if (g != null) {
                g = g.replace(',', '.');
            }
            try {
                double d = Double.parseDouble(g);
                hit = d > 400;
            } catch (Exception ignored) {
            }
        }

        return hit;
    }
}

