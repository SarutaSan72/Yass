package yass.filter;

import yass.YassSong;
import yass.stats.YassStats;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 4. M�rz 2008
 */
public class YassStatsFilter extends YassFilter {
    private YassStats stats = null;

    private float start = -1, end = -1;


    /**
     * Gets the iD attribute of the YassErrorsFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "stats";
    }


    /**
     * Sets the rule attribute of the YassStatsFilter object
     *
     * @param s The new rule value
     */
    public void setRule(String s) {
        super.setRule(s);

        //System.out.println("###" + s);
        int i = s.indexOf('_');
        if (i >= 0) {
            String id = s.substring(0, i).trim();
            stats = YassStats.getStatsAt(YassStats.indexOf(id));
            rule = s.substring(i + 1).trim();
        }

        start = end = -1;
        i = rule.indexOf('_');
        if (i >= 0) {
            s = rule.substring(i + 1).trim();
            rule = rule.substring(0, i).trim();

            i = s.indexOf('-');
            try {
                String startLength = s.substring(0, i).trim();
                if (startLength.length() > 0) {
                    start = (float) Double.parseDouble(startLength);
                } else {
                    start = -1;
                }
            } catch (Exception e) {
                start = -1;
            }
            try {
                String endLength = s.substring(i + 1).trim();
                if (endLength.length() > 0) {
                    end = (float) Double.parseDouble(endLength);
                } else {
                    end = -1;
                }

            } catch (Exception e) {
                end = -1;
            }
            //System.out.println(rule + " " + start + " " + end + " ");
        }
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
        } else {
            hit = stats.accept(s, rule, start, end);
        }
        return hit;
    }
}

