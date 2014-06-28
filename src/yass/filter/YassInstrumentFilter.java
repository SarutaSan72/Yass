package yass.filter;

import yass.YassRow;
import yass.YassSong;
import yass.YassTable;

import java.io.File;
import java.util.Enumeration;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 4. M�rz 2008
 */
public class YassInstrumentFilter extends YassFilter {

    private String noteTable[] = new String[]{
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "B", "H"
    };
    private YassTable t = null;

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean count() {
        return false;
    }

    /**
     * Gets the noteName attribute of the YassPitchStats object
     *
     * @param n Description of the Parameter
     * @return The noteName value
     */
    public String getNoteName(int n) {
        int norm = n;
        int app = 0;
        while (norm < 0) {
            norm += 12;
            app--;
        }
        while (norm > 12) {
            norm -= 12;
            app++;
        }
        String name = noteTable[norm % 12];
        if (app != 0) {
            name = name + "" + app;
        }
        return name;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean showTitle() {
        return true;
    }

    /**
     * Gets the whiteNote attribute of the YassInstrumentFilter object
     *
     * @param n Description of the Parameter
     * @return The whiteNote value
     */
    public boolean isWhiteNote(int n) {
        while (n < 0) {
            n += 12;
        }
        n = n % 12;
        if (n == 0 || n == 2 || n == 4 || n == 5 || n == 7 || n == 9 || n == 11) {
            return true;
        }
        return false;
    }

    /**
     * Gets the iD attribute of the YassTitleFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "instrument";
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
            return hit;
        }

        if (t == null) {
            t = new YassTable();
        }
        String filename = s.getDirectory() + File.separator + s.getFilename();
        t.removeAllRows();
        if (!t.loadFile(filename)) {
            return false;
        }

        if (rule.equals("white")) {
            for (Enumeration<YassRow> en = t.getRows(); en.hasMoreElements(); ) {
                YassRow r = (YassRow) en.nextElement();
                if (!r.isNote()) {
                    continue;
                }
                int height = r.getHeightInt();
                boolean isWhite = isWhiteNote(height);
                if (!isWhite) {
                    return false;
                }
            }
            hit = true;
        } else if (rule.equals("black")) {
            for (Enumeration<YassRow> en = t.getRows(); en.hasMoreElements(); ) {
                YassRow r = (YassRow) en.nextElement();
                if (!r.isNote()) {
                    continue;
                }
                int height = r.getHeightInt();
                boolean isWhite = isWhiteNote(height);
                if (isWhite) {
                    return false;
                }
            }
            hit = true;
        } else if (rule.equals("12-white")) {
            int minHeight = 1000;
            int maxHeight = -1000;
            for (Enumeration<YassRow> en = t.getRows(); en.hasMoreElements(); ) {
                YassRow r = (YassRow) en.nextElement();
                if (!r.isNote()) {
                    continue;
                }
                int height = r.getHeightInt();
                minHeight = Math.min(height, minHeight);
                maxHeight = Math.max(height, maxHeight);
                boolean isWhite = isWhiteNote(height);
                if (!isWhite) {
                    return false;
                }
            }
            int range = maxHeight - minHeight;
            int start = minHeight % 12;
            hit = start + range <= 20;
        } else if (rule.equals("8-white")) {
            int minHeight = 1000;
            int maxHeight = -1000;
            for (Enumeration<?> en = t.getRows(); en.hasMoreElements(); ) {
                YassRow r = (YassRow) en.nextElement();
                if (!r.isNote()) {
                    continue;
                }
                int height = r.getHeightInt();
                minHeight = Math.min(height, minHeight);
                maxHeight = Math.max(height, maxHeight);
                boolean isWhite = isWhiteNote(height);
                if (!isWhite) {
                    return false;
                }
            }
            int range = maxHeight - minHeight;
            int start = minHeight % 12;
            hit = start + range <= 13;
        } else if (rule.equals("25-keys")) {
            int minHeight = 1000;
            int maxHeight = -1000;
            for (Enumeration<?> en = t.getRows(); en.hasMoreElements(); ) {
                YassRow r = (YassRow) en.nextElement();
                if (!r.isNote()) {
                    continue;
                }
                int height = r.getHeightInt();
                minHeight = Math.min(height, minHeight);
                maxHeight = Math.max(height, maxHeight);
            }
            int range = maxHeight - minHeight;
            int start = minHeight % 12;
            hit = start + range < 25;
        } else if (rule.equals("13-keys")) {
            int minHeight = 1000;
            int maxHeight = -1000;
            for (Enumeration<?> en = t.getRows(); en.hasMoreElements(); ) {
                YassRow r = (YassRow) en.nextElement();
                if (!r.isNote()) {
                    continue;
                }
                int height = r.getHeightInt();
                minHeight = Math.min(height, minHeight);
                maxHeight = Math.max(height, maxHeight);
            }
            int range = maxHeight - minHeight;
            int start = minHeight % 12;
            hit = start + range < 13;
        }

        return hit;
    }
}

