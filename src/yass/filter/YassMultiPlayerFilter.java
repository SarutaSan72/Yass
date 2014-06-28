package yass.filter;

import yass.YassSong;
import yass.YassSongList;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 4. M�rz 2008
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

        int pn = 1;
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

