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

