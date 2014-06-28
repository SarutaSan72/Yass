package yass.filter;

import yass.YassSong;
import yass.YassSongList;

import java.util.Collections;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassLanguageFilter extends YassFilter {
    private Vector<String> multiple = null;


    /**
     * Gets the iD attribute of the YassLanguageFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "language";
    }


    /**
     * Gets the genericRules attribute of the YassLanguageFilter object
     *
     * @param data Description of the Parameter
     * @return The genericRules value
     */
    public String[] getGenericRules(Vector<YassSong> data) {
        Vector<String> langs = new Vector<>();
        for (Enumeration<?> e = data.elements(); e.hasMoreElements(); ) {
            YassSong s = (YassSong) e.nextElement();
            String lang = s.getLanguage();
            if (lang == null || lang.length() < 1) {
                continue;
            }
            if (!langs.contains(lang)) {
                langs.addElement(lang);

            }
        }
        Collections.sort(langs);

        return langs.toArray(new String[langs.size()]);
    }


    /**
     * Sets the rule attribute of the YassLanguageFilter object
     *
     * @param s The new rule value
     */
    public void setRule(String s) {
        super.setRule(s);
        if (s.indexOf(";") > 0) {
            StringTokenizer st = new StringTokenizer(s, ";");
            multiple = new Vector<>(4);
            while (st.hasMoreTokens()) {
                String s2 = st.nextToken();
                multiple.addElement(s2);
            }
        } else {
            multiple = null;
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
        if (multiple != null) {
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
        if (multiple != null) {
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
        String old = s.getLanguage();
        if (old == null || old.equals(rule)) {
            return;
        }

        s.setLanguage(rule);
        s.setSaved(false);
    }


    /**
     * Gets the extraInfo attribute of the YassLanguageFilter object
     *
     * @return The extraInfo value
     */
    public int getExtraInfo() {
        return YassSongList.LANGUAGE_COLUMN;
    }


    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean accept(YassSong s) {
        String t = s.getLanguage();
        boolean hit;

        if (rule.equals("all")) {
            hit = true;
        } else if (multiple != null) {
            hit = multiple.indexOf(t) >= 0;
        } else {
            hit = t.equals(rule);
        }

        return hit;
    }
}

