package yass.filter;

import yass.YassSong;
import yass.YassSongList;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 4. M�rz 2008
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
            } catch (Exception e) {
            }
        } else if (rule.equals("gap>30")) {
            String g = s.getGap();
            if (g != null) {
                g = g.replace(',', '.');
            }
            try {
                double d = Double.parseDouble(g);
                hit = d >= 30000;
            } catch (Exception e) {
            }
        } else if (rule.equals("gap<10")) {
            String g = s.getGap();
            if (g != null) {
                g = g.replace(',', '.');
            }
            try {
                double d = Double.parseDouble(g);
                hit = d < 10000;
            } catch (Exception e) {
            }
        } else if (rule.equals("gap<5")) {
            String g = s.getGap();
            if (g != null) {
                g = g.replace(',', '.');
            }
            try {
                double d = Double.parseDouble(g);
                hit = d < 5000;
            } catch (Exception e) {
            }
        } else if (rule.equals("bpm<200")) {
            String g = s.getBPM();
            if (g != null) {
                g = g.replace(',', '.');
            }
            try {
                double d = Double.parseDouble(g);
                hit = d < 200;
            } catch (Exception e) {
            }
        } else if (rule.equals("bpm>400")) {
            String g = s.getBPM();
            if (g != null) {
                g = g.replace(',', '.');
            }
            try {
                double d = Double.parseDouble(g);
                hit = d > 400;
            } catch (Exception e) {
            }
        }

        return hit;
    }
}

