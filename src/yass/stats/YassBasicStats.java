package yass.stats;

import yass.YassRow;
import yass.YassSong;
import yass.YassTable;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 4. M�rz 2008
 */
public class YassBasicStats extends YassStats {

    private String[] ids = new String[]{"pages", "words", "notes", "golden", "freestyle", "notesperpage"};

    private int pagesIndex = -1;
    private int wordsIndex = -1;
    private int notesIndex = -1;
    private int goldenIndex = -1;
    private int freestyleIndex = -1;
    private int nppIndex = -1;


    /**
     * Gets the iD attribute of the YassSpeedStats object
     *
     * @return The iD value
     */
    public String[] getIDs() {
        return ids;
    }


    /**
     * Description of the Method
     *
     * @param t Description of the Parameter
     * @param s Description of the Parameter
     */
    public void calcStats(YassSong s, YassTable t) {
        int pages = 0;
        int words = 0;
        int notes = 0;
        int golden = 0;
        int freestyle = 0;

        int notelen = 0;
        int goldenlen = 0;
        int freelen = 0;

        int i = 0;
        int n = t.getRowCount() - 1;
        boolean lastspace = true;
        while (i <= n) {
            YassRow r = t.getRowAt(i);
            if (r.isNote()) {
                String txt = r.getText();
                if (txt.startsWith(YassRow.SPACE + "") || lastspace) {
                    words++;
                    lastspace = false;
                }
                notelen += r.getLengthInt();
                notes++;
            }
            if (r.isPageBreak() || r.isEnd()) {
                pages++;
                lastspace = true;
            }
            if (r.isGolden()) {
                goldenlen += r.getLengthInt();
                golden++;
            }
            if (r.isFreeStyle()) {
                freelen += r.getLengthInt();
                freestyle++;
            }

            i++;
        }

        float npp = notes / (float) pages;

        float goldenp = notes > 0 ? 100 * goldenlen / (float) notelen : 0;
        float freestylep = notes > 0 ? 100 * freelen / (float) notelen : 0;

        if (pagesIndex < 0) {
            pagesIndex = indexOf("pages");
            wordsIndex = indexOf("words");
            notesIndex = indexOf("notes");
            goldenIndex = indexOf("golden");
            freestyleIndex = indexOf("freestyle");
            nppIndex = indexOf("notesperpage");
        }
        s.setStatsAt(pagesIndex, pages);
        s.setStatsAt(wordsIndex, words);
        s.setStatsAt(notesIndex, notes);
        s.setStatsAt(goldenIndex, goldenp);
        s.setStatsAt(freestyleIndex, freestylep);
        s.setStatsAt(nppIndex, npp);
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
        boolean hit = false;

        if (pagesIndex < 0) {
            return false;
        }

        if (rule.equals("common")) {
            float val = s.getStatsAt(pagesIndex);
            boolean pageshit = val >= 31 && (val <= 80);
            val = s.getStatsAt(goldenIndex);
            boolean goldenhit = val >= 3 && (val <= 20);
            val = s.getStatsAt(freestyleIndex);
            boolean freehit = val <= 30;
            hit = pageshit && goldenhit && freehit;
        }
        if (rule.equals("pages")) {
            float val = s.getStatsAt(pagesIndex);
            hit = val >= start && (val <= end || end < 0);
        }
        if (rule.equals("words")) {
            float val = s.getStatsAt(wordsIndex);
            hit = val >= start && (val <= end || end < 0);
        }
        if (rule.equals("notes")) {
            float val = s.getStatsAt(notesIndex);
            hit = val >= start && (val <= end || end < 0);
        }
        if (rule.equals("golden")) {
            float val = s.getStatsAt(goldenIndex);
            hit = val >= start && (val <= end || end < 0);
        }
        if (rule.equals("freestyle")) {
            float val = s.getStatsAt(freestyleIndex);
            hit = val >= start && (val <= end || end < 0);
        }
        if (rule.equals("notesperpage")) {
            float val = s.getStatsAt(nppIndex);
            hit = val >= start && (val <= end || end < 0);
        }
        return hit;
    }
}

