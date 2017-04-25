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

package yass.stats;

import yass.I18;
import yass.YassSong;
import yass.YassTable;
import yass.YassUtils;

import java.util.*;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassStats implements Cloneable {
    /**
     * Description of the Field
     */
    public static int length = -1;
    /**
     * Description of the Field
     */
    public static String allids[] = null;
    /**
     * Description of the Field
     */
    public static String alllabels[] = null;
    /**
     * Description of the Field
     */
    public static String alldesc[] = null;
    /**
     * Description of the Field
     */
    public static YassStats allstats[] = null;
    private static Hashtable<String, Integer> hash = null;
    private static Vector<YassStats> plugins = null;

    /**
     * Description of the Field
     */
    private static Properties prop = null;


    /**
     * Constructor for the YassFilterPlugin object
     */
    public YassStats() {
    }

    /**
     * Gets the iDAt attribute of the YassStats class
     *
     * @param i Description of the Parameter
     * @return The iDAt value
     */
    public static String getIDAt(int i) {
        return allids[i];
    }

    /**
     * Gets the labelAt attribute of the YassStats class
     *
     * @param i Description of the Parameter
     * @return The labelAt value
     */
    public static String getLabelAt(int i) {
        return alllabels[i];
    }

    /**
     * Gets the descriptionAt attribute of the YassStats class
     *
     * @param i Description of the Parameter
     * @return The descriptionAt value
     */
    public static String getDescriptionAt(int i) {
        return alldesc[i];
    }

    /**
     * Description of the Method
     */
    public static void init() {
        hash = new Hashtable<>();
        plugins = new Vector<>();
        StringTokenizer st = new StringTokenizer(prop.getProperty("stats-plugins"), "|");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            addPlugin(s);
        }

        length = 0;
        for (Enumeration<YassStats> en = plugins.elements(); en.hasMoreElements(); ) {
            YassStats stats = en.nextElement();
            length += stats.getIDCount();
        }

        allids = new String[length];
        alllabels = new String[length];
        alldesc = new String[length];
        allstats = new YassStats[length];

        int k = 0;
        for (Enumeration<YassStats> en = plugins.elements(); en.hasMoreElements(); ) {
            YassStats stats = en.nextElement();
            String s[] = stats.getIDs();
            for (String value : s) {
                allstats[k] = stats;
                allids[k] = value;
                alllabels[k] = I18.get("stats_" + value + "_title");
                alldesc[k] = I18.get("stats_" + value + "_msg");
                hash.put(value, new Integer(k));
                k++;
            }
        }
    }

    private static void addPlugin(String classname) {
        YassStats stats;
        try {
            Class<?> c = YassUtils.forName(classname);
            stats = (YassStats) c.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        plugins.addElement(stats);
    }

    /**
     * Gets the allStats attribute of the YassStats class
     *
     * @param i Description of the Parameter
     * @return The allStats value
     */
    public static YassStats getStatsAt(int i) {
        return allstats[i];
    }

    /**
     * Description of the Method
     *
     * @param id Description of the Parameter
     * @return Description of the Return Value
     */
    public static int indexOf(String id) {
        return hash.get(id).intValue();
    }

    /**
     * Gets the allStats attribute of the YassStats class
     *
     * @return The allStats value
     */
    public static Vector<YassStats> getAllStats() {
        return plugins;
    }

    /**
     * Gets the properties attribute of the YassStats class
     *
     * @return The properties value
     */
    public static Properties getProperties() {
        return prop;
    }

    /**
     * Sets the properties attribute of the YassStats class
     *
     * @param p The new properties value
     */
    public static void setProperties(Properties p) {
        prop = p;
    }

    /**
     * Gets the iD attribute of the YassStats object
     *
     * @return The iD value
     */
    public String[] getIDs() {
        return new String[]{"unspecified"};
    }

    /**
     * Gets the count attribute of the YassStats class
     *
     * @return The count value
     */
    public int getIDCount() {
        return getIDs().length;
    }

    /**
     * Gets the stats attribute of the YassStats object
     *
     * @param t Description of the Parameter
     * @param s Description of the Parameter
     */
    public void calcStats(YassSong s, YassTable t) {
    }


    /**
     * Constructor for the accept object
     *
     * @param s     Description of the Parameter
     * @param rule  Description of the Parameter
     * @param start Description of the Parameter
     * @param end   Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean accept(YassSong s, String rule, float start, float end) {
        return false;
    }
}


