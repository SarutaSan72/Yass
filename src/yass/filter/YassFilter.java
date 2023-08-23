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

import yass.I18;
import yass.YassSong;
import yass.YassSongList;
import yass.YassUtils;

import java.io.File;
import java.util.*;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassFilter implements Cloneable {
    /**
     * Description of the Field
     */
    public static boolean isInterrupted = false;
    private static Hashtable<String, YassFilter> hash = null;
    private static Vector<YassFilter> plugins = null;
    /**
     * Description of the Field
     */
    private static Properties prop = null;
    /**
     * Description of the Field
     */
    protected String rule = null;
    private String label = null;


    /**
     * Constructor for the YassFilterPlugin object
     */
    public YassFilter() {
    }


    /**
     * Description of the Method
     */
    public static void init() {
        hash = new Hashtable<>();
        plugins = new Vector<>();
        StringTokenizer st = new StringTokenizer(prop.getProperty("filter-plugins"), "|");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            addPlugin(s);
        }
    }


    /**
     * Adds a feature to the Plugin attribute of the YassFilter class
     *
     * @param filtername The feature to be added to the Plugin attribute
     * @return Description of the Return Value
     */
    private static YassFilter addPlugin(String filtername) {
        YassFilter f;
        try {
            Class<?> c = YassUtils.forName(filtername);
            f = (YassFilter) c.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        hash.put(f.getID(), f);
        plugins.addElement(f);
        return f;
    }

    /**
     * Gets the groupsLabel attribute of the YassGroups class
     *
     * @return The groupsLabel value
     */
    public static String[] getAllLabels() {
        String[] s = new String[plugins.size()];
        int i = 0;
        for (Enumeration<YassFilter> en = plugins.elements(); en.hasMoreElements(); ) {
            YassFilter f = en.nextElement();
            s[i++] = f.getLabel();
        }
        return s;
    }


    /**
     * Gets the iDAt attribute of the YassFilter class
     *
     * @param i Description of the Parameter
     * @return The iDAt value
     */
    public static String getIDAt(int i) {
        YassFilter f = plugins.elementAt(i);
        return f.getID();
    }

    /**
     * Gets the properties attribute of the YassFilter class
     *
     * @return The properties value
     */
    public static Properties getProperties() {
        return prop;
    }

    /**
     * Sets the properties attribute of the YassFilter class
     *
     * @param p The new properties value
     */
    public static void setProperties(Properties p) {
        prop = p;
    }

    /**
     * Description of the Method
     *
     * @param group Description of the Parameter
     * @return Description of the Return Value
     */
    public static YassFilter createFilter(String group) {
        if (hash == null) {
            init();
        }
        try {
            YassFilter f = hash.get(group);
            return (YassFilter) f.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Description of the Method
     *
     * @param f   Description of the Parameter
     * @param str Description of the Parameter
     * @return Description of the Return Value
     */
    public static boolean containsIgnoreCase(String f, String str) {
        if (f == null) {
            return false;
        }
        f = f.toLowerCase();
        if (str.contains("'")) {
            f = f.replace("’", "'");
        }
        return f.contains(str);
    }

    /**
     * Gets the iD attribute of the YassFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "unspecified";
    }

    /**
     * Gets the label attribute of the YassFilter object
     *
     * @return The label value
     */
    public String getLabel() {
        if (label == null) {
            label = I18.get("group_" + getID() + "_title");
        }
        return label;
    }

    /**
     * Sets the label attribute of the YassFilter object
     *
     * @param s The new label value
     */
    public void setLabel(String s) {
        label = s;
    }

    /**
     * Gets the genericRules attribute of the YassFilter object
     *
     * @param songs Description of the Parameter
     * @return The genericRules value
     */
    public String[] getGenericRules(Vector<YassSong> songs) {
        return null;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean confirm() {
        return false;
    }

    /**
     * Gets the confirmString attribute of the YassFilter object
     *
     * @param rule Description of the Parameter
     * @return The confirmString value
     */
    public String getConfirmString(String rule) {
        return null;
    }

    /**
     * Description of the Method
     *
     * @param songs Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean start(Vector<?> songs) {
        return true;
    }

    /**
     * Description of the Method
     */
    public void stop() {
    }

    /**
     * Gets the sorting attribute of the YassFilter object
     *
     * @return The sorting value
     */
    public int getSorting() {
        return YassSongList.TITLE_COLUMN;
    }

    /**
     * Gets the extraInfo attribute of the YassFilter object
     *
     * @return The extraInfo value
     */
    public int getExtraInfo() {
        return YassSongList.TITLE_COLUMN;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean count() {
        return true;
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean allowDrop(String s) {
        return false;
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean allowCoverDrop(String s) {
        return false;
    }

    /**
     * Description of the Method
     *
     * @param rule Description of the Parameter
     * @param s    Description of the Parameter
     */
    public void drop(String rule, YassSong s) {
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean refreshCounters() {
        return false;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean renderTitle() {
        return rule.equals("all");
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean showTitle() {
        if (rule.equals("all")) {
            return false;
        }
        return !rule.equals("unspecified");
    }

    /**
     * Gets the rule attribute of the YassFilter object
     *
     * @return The rule value
     */
    public final String getRule() {
        return rule;
    }

    /**
     * Sets the rule attribute of the YassFilter object
     *
     * @param s The new rule value
     */
    public void setRule(String s) {
        rule = s;
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean accept(YassSong s) {
        String t = s.getDirectory() + File.separator + s.getFilename();
        return containsIgnoreCase(t, rule) ||
                containsIgnoreCase(s.getTitle(), rule) ||
                containsIgnoreCase(s.getArtist(), rule) ||
                containsIgnoreCase(s.getLanguage(), rule) ||
                containsIgnoreCase(s.getEdition(), rule) ||
                containsIgnoreCase(s.getGenre(), rule) ||
                containsIgnoreCase(s.getDuetSingerNames(), rule);
    }
}

