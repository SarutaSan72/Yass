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

import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassYearFilter extends YassFilter {

    private int start = -1, end = -1, exact = -1;


    /**
     * Gets the iD attribute of the YassYearFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "year";
    }


    /**
     * Gets the genericRules attribute of the YassYearFilter object
     *
     * @param data Description of the Parameter
     * @return The genericRules value
     */
    public String[] getGenericRules(Vector<YassSong> data) {
        Vector<String> years = new Vector<>();
        Calendar rightNow = Calendar.getInstance();
        //int year = rightNow.get(Calendar.YEAR);
        //for (int i=1900; i<=year; i++) {
        for (Enumeration<?> e = data.elements(); e.hasMoreElements(); ) {
            YassSong s = (YassSong) e.nextElement();
            String y = s.getYear();
            if (y == null || y.length() < 1) {
                continue;
            }
            if (!years.contains(y)) {
                years.addElement(y);
            }
        }
        Collections.sort(years);

        return years.toArray(new String[years.size()]);
    }


    /**
     * Sets the rule attribute of the YassYearFilter object
     *
     * @param s The new rule value
     */
    public void setRule(String s) {
        super.setRule(s);

        int i = s.indexOf('-');
        if (i >= 0) {

            try {
                String startyear = s.substring(0, i);
                start = Integer.parseInt(startyear);
            } catch (Exception e) {
                start = -1;
            }
            try {
                String endyear = s.substring(i + 1);
                end = Integer.parseInt(endyear);
            } catch (Exception e) {
                end = -1;
            }

        } else {
            try {
                exact = Integer.parseInt(s);
            } catch (Exception e) {
                exact = -1;
            }
        }
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
        String old = s.getYear();
        if (old == null || old.equals(rule)) {
            return;
        }

        s.setYear(rule);
        s.setSaved(false);
    }


    /**
     * Gets the extraInfo attribute of the YassYearFilter object
     *
     * @return The extraInfo value
     */
    public int getExtraInfo() {
        return YassSongList.YEAR_COLUMN;
    }


    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean accept(YassSong s) {
        String year = s.getYear();

        int y;
        try {
            y = Integer.parseInt(year);
        } catch (Exception e) {
            y = -1;
        }

        boolean hit = false;

        if (rule.equals("all")) {
            hit = true;
        } else if (rule.equals("unspecified")) {
            if (y < 0) {
                hit = true;
            }
        } else if (exact > 0) {
            hit = exact == y;
        } else {
            hit = (start < 0 || start <= y) && (end < 0 || y <= end);
        }

        return hit;
    }

}

