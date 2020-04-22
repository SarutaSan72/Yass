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

import yass.YassRow;
import yass.YassSong;
import yass.YassTable;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassPitchStats extends YassStats {

    private String[] ids = new String[]{"pitchrange", "pitchrangepage", "pitchdistance", "pitchleaps3", "pitchleaps6"};

    private int pitchrangeIndex = -1;
    private int pitchrangepageIndex = -1;
    private int pitchdistanceIndex = -1;
    private int pitchleaps3Index = -1;
    private int pitchleaps6Index = -1;


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
        int pitchrange;
        int pitchrangepage = 0;
        float pitchdistance = 0;
        float pitchleaps3 = 0;
        float pitchleaps6 = 0;

        int pitchmin = 256;
        int pitchmax = -256;
        int pitchminpage = 256;
        int pitchmaxpage = -256;

        int i = 0;
        int n = t.getRowCount() - 1;
        int notes = 0;
        int pages = 0;
        boolean newpage = true;
        int lastheight = 0;

        while (i <= n) {
            YassRow r = t.getRowAt(i);
            if (r.isFreeStyle() || r.isRap() || r.isRapGolden()) {
                i++;
                continue;
            }

            if (r.isNote()) {
                notes++;
                int height = r.getHeightInt();
                int dist = Math.abs(height - lastheight);
                if (!newpage) {
                    if (dist > 3) {
                        pitchleaps3++;
                    }
                    if (dist > 6) {
                        pitchleaps6++;
                    }
                    pitchdistance += dist;
                }
                pitchmin = Math.min(pitchmin, height);
                pitchmax = Math.max(pitchmax, height);
                pitchminpage = Math.min(pitchminpage, height);
                pitchmaxpage = Math.max(pitchmaxpage, height);
                lastheight = height;
                newpage = false;
            }
            if (r.isPageBreak() || r.isEnd()) {
                if (!newpage) {
                    pages++;
                    pitchrangepage += Math.abs(pitchmaxpage - pitchminpage);
                }
                pitchminpage = 256;
                pitchmaxpage = -256;
                newpage = true;
            }
            i++;
        }

        pitchrange = notes > 0 ? pitchmax - pitchmin : 0;
        pitchrangepage = pages > 0 ? pitchrangepage / pages : 0;
        pitchdistance = notes - pages > 0 ? pitchdistance / (notes - pages) : 0;

        if (notes > 0) {
            pitchleaps3 = 100 * pitchleaps3 / (float) notes;
            pitchleaps6 = 100 * pitchleaps6 / (float) notes;
        }

        if (pitchrangeIndex < 0) {
            pitchrangeIndex = indexOf("pitchrange");
            pitchrangepageIndex = indexOf("pitchrangepage");
            pitchdistanceIndex = indexOf("pitchdistance");
            pitchleaps3Index = indexOf("pitchleaps3");
            pitchleaps6Index = indexOf("pitchleaps6");
        }
        s.setStatsAt(pitchrangeIndex, pitchrange);
        s.setStatsAt(pitchrangepageIndex, pitchrangepage);
        s.setStatsAt(pitchdistanceIndex, pitchdistance);
        s.setStatsAt(pitchleaps3Index, pitchleaps3);
        s.setStatsAt(pitchleaps6Index, pitchleaps6);
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

        if (pitchrangeIndex < 0) {
            return false;
        }

        if (rule.equals("monoton")) {
            float val = s.getStatsAt(pitchrangeIndex);
            boolean phit = val <= 12;

            val = s.getStatsAt(pitchrangepageIndex);
            boolean pphit = val < 8;

            hit = phit && pphit;
        }
        if (rule.equals("melodic")) {
            float val = s.getStatsAt(pitchrangeIndex);
            boolean phit = val > 12;

            val = s.getStatsAt(pitchrangepageIndex);
            boolean pphit = val >= 8;

            hit = phit || pphit;
        }
        if (rule.equals("smooth")) {
            float val = s.getStatsAt(pitchdistanceIndex);
            hit = val <= 1.5;
        }
        if (rule.equals("bumpy")) {
            float val = s.getStatsAt(pitchdistanceIndex);
            hit = val > 1.5;
        }

        return hit;
    }
}

