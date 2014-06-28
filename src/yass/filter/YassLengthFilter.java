package yass.filter;

import yass.YassSong;
import yass.YassSongList;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassLengthFilter extends YassFilter {

    private int start = -1, end = -1;


    /**
     * Gets the iD attribute of the YassLengthFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "length";
    }


    /**
     * Sets the rule attribute of the YassLengthFilter object
     *
     * @param s The new rule value
     */
    public void setRule(String s) {
        super.setRule(s);

        int i = s.indexOf('-');
        if (i >= 0) {
            try {
                String startLength = s.substring(0, i).trim();
                if (startLength.length() > 0) {
                    int j = startLength.indexOf(':');
                    if (j >= 0) {
                        int min = Integer.parseInt(startLength.substring(0, j));
                        int sec = Integer.parseInt(startLength.substring(j + 1));
                        start = min * 60 + sec;
                    } else {
                        start = Integer.parseInt(startLength);
                    }
                } else {
                    start = -1;
                }
            } catch (Exception e) {
                start = -1;
            }
            try {
                String endLength = s.substring(i + 1).trim();
                if (endLength.length() > 0) {
                    int j = endLength.indexOf(':');
                    if (j >= 0) {
                        int min = Integer.parseInt(endLength.substring(0, j));
                        int sec = Integer.parseInt(endLength.substring(j + 1));
                        end = min * 60 + sec;
                    } else {
                        end = Integer.parseInt(endLength);
                    }
                } else {
                    end = -1;
                }

            } catch (Exception e) {
                end = -1;
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
     * Gets the extraInfo attribute of the YassLengthFilter object
     *
     * @return The extraInfo value
     */
    public int getExtraInfo() {
        return YassSongList.LENGTH_COLUMN;
    }


    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean accept(YassSong s) {
        String length = s.getLength();

        int len;
        try {
            len = Integer.parseInt(length);
        } catch (Exception e) {
            len = -1;
        }

        boolean hit = false;

        if (rule.equals("all")) {
            hit = true;
        } else if (rule.equals("unspecified")) {
            if (len < 0) {
                hit = true;
            }
        } else {
            hit = (start < 0 || start <= len) && (end < 0 || len <= end);
        }

        return hit;
    }

}

