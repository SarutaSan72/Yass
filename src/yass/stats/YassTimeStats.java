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

import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassTimeStats extends YassStats {

    private String[] ids = new String[]{
            //  "pagelengthmax",
            //"notelength",
            "pagelengthsec",
            "notelengthms",
            "notesgapms", "shorts50ms", "shorts100ms", "holds1sec", "holds3sec", "speedlen", "speeddist"};

    private int pagelenmaxIndex = -1;
    private int notelenIndex = -1;
    private int pagelensecIndex = -1;
    private int notelenmsIndex = -1;

    private int shorts100msIndex = -1;
    private int shorts50msIndex = -1;
    private int holds1secIndex = -1;
    private int holds3secIndex = -1;

    private int notesgapmsIndex = -1;
    private int speedlenIndex = -1;
    private int speeddistIndex = -1;


    /**
     * Gets the iD attribute of the YassSpeedStats object
     *
     * @return The iD value
     */
    public String[] getIDs() {
        return ids;
    }



	/*
     *  Try to give a useful answer to the hardness of a song. For this, several parts
	 *  are analysed and the fastest one will be returned.
	 *
	 *  This is a little bit better than an average value of the whole song because some
	 *  tricky ones (e.g. Nightwish) got a long slow end but a very fast start or middle
	 *  part.
	 *
	 *  I hope it will be useful. (saiya_mg)
	 *
	 *  @return the number of singable syllables per second (no freestyle or pauses)
	 */

    /**
     * Description of the Method
     *
     * @param t Description of the Parameter
     * @return Description of the Return Value
     */
    public float lengthPerSecond(YassTable t) {
        float bpm = (float) t.getBPM();
        if (bpm <= 0) {
            return 0;
        }

        Vector<Integer> lengths = new Vector<>();

        int i = 0;
        int n = t.getRowCount() - 1;
        while (i <= n) {
            YassRow r = t.getRowAt(i);
            if (r.isFreeStyle()) {
                i++;
                continue;
            }
            if (r.isNote()) {
                int length = r.getLengthInt();
                lengths.addElement(new Integer(length));
            }
            i++;
        }

        if (lengths.isEmpty()) {
            return 0;
        }

        int beatCount1 = 0;
        int beatCount2 = 0;
        int beatCount3 = 0;
        int dlen = lengths.size();
        int dlen2 = dlen / 2;
        for (int d = 0; d < dlen2; d++) {
            beatCount1 += lengths.elementAt(d).intValue();
            beatCount2 += lengths.elementAt(d + dlen / 4).intValue();
            beatCount3 += lengths.elementAt(Math.min(d + dlen2, dlen)).intValue();
        }

        float beat_to_sec = (4 * bpm) / 60f;
        float firsthalf = (dlen2 / (float) beatCount1) * beat_to_sec;
        float middle = (dlen2 / (float) beatCount2) * beat_to_sec;
        float secondhalf = (dlen2 / (float) beatCount3) * beat_to_sec;

        return Math.max(firsthalf, Math.max(middle, secondhalf));
    }


    /**
     * Notes per second, based on average notes per page.
     *
     * @param t Description of the Parameter
     * @return Description of the Return Value
     */
    public float notesPerSecond(YassTable t) {
        float bpm = (float) t.getBPM();
        if (bpm <= 0) {
            return 0;
        }

        Vector<Float> ratios = new Vector<>();

        int i = 0;
        int n = t.getRowCount() - 1;
        int notes = 0;
        int startbeat = 0;
        YassRow lastr = null;
        while (i <= n) {
            YassRow r = t.getRowAt(i);
            if (r.isFreeStyle()) {
                i++;
                continue;
            }
            if (r.isNote()) {
                if (notes == 0) {
                    startbeat = r.getBeatInt();
                }
                notes++;
                lastr = r;
            }
            if (r.isPageBreak() || r.isEnd()) {
                int pagelen = lastr != null ? lastr.getBeatInt() + lastr.getLengthInt() - startbeat : 0;
                if (pagelen > 0) {
                    ratios.addElement(new Float(notes / (float) pagelen));
                }
                notes = 0;
            }
            i++;
        }

        if (ratios.isEmpty()) {
            return 0;
        }

        float ratio1 = 0;
        float ratio2 = 0;
        float ratio3 = 0;
        int dlen = ratios.size();
        int dlen2 = dlen / 2;
        for (int d = 0; d < dlen2; d++) {
            ratio1 += ratios.elementAt(d).floatValue();
            ratio2 += ratios.elementAt(d + dlen / 4).floatValue();
            ratio3 += ratios.elementAt(Math.min(d + dlen2, dlen)).floatValue();
        }

        float beat_to_sec = (4 * bpm) / 60f;
        float firsthalf = ratio1 / dlen2 * beat_to_sec;
        float middle = ratio2 / dlen2 * beat_to_sec;
        float secondhalf = ratio3 / dlen2 * beat_to_sec;

        return Math.max(firsthalf, Math.max(middle, secondhalf));
    }


    /**
     * Description of the Method
     *
     * @param t Description of the Parameter
     * @param s Description of the Parameter
     */
    public void calcStats(YassSong s, YassTable t) {
        if (s == null || t == null)
            return;
        int pagelenmax = 0;
        float notelen = 0;
        float pagelensec;
        float notelenms;

        float holds1sec = 0;
        float holds3sec = 0;
        float shorts100ms = 0;
        float shorts50ms = 0;

        float notesgap = 0;
        float notesgapms;
        float speedlen;
        float speeddist;

        int i = 0;
        int n = t.getRowCount() - 1;
        boolean newpage = true;
        int notes = 0;
        int pages = 0;
        int pagestart = 0;
        int pageend = 0;

        // beat = (int) ((ms - gap) * 4 * bpm / (60 * 1000))
        float bpm = (float) t.getBPM();
        float gap = (float) t.getGap();
        float beat_to_ms = 60 * 1000 / (4 * bpm);
        //double ms = gap + beat * beat_to_ms;

        while (i <= n) {
            YassRow r = t.getRowAt(i);
            if (r.isFreeStyle()) {
                i++;
                continue;
            }

            if (r.isNote()) {
                notes++;
                int beat = r.getBeatInt();
                int len = r.getLengthInt();

                if (newpage) {
                    pagestart = beat;
                    newpage = false;
                } else {
                    int dist = Math.abs(beat - pageend);
                    if (dist > 1000) {
                        //System.out.print("high notes distance (" + dist + "): ");
                        //System.out.println(t.getArtist() + " - " + t.getTitle());
                    }
                    notesgap += dist;
                }
                pageend = beat + len;
                notelen += len;
                if (len * beat_to_ms > 1000) {
                    holds1sec++;
                }
                if (len * beat_to_ms > 3000) {
                    holds3sec++;
                }
                if (len * beat_to_ms < 50) {
                    shorts50ms++;
                }
                if (len * beat_to_ms < 100) {
                    shorts100ms++;
                }
            }
            if (r.isPageBreak() || r.isEnd()) {
                pages++;
                int pagelen = pageend - pagestart;
                pagelenmax = Math.max(pagelen, pagelenmax);
                newpage = true;
            }
            i++;
        }
        notelen = notes > 0 ? (notelen / (float) notes) : 0;
        pagelensec = (pagelenmax * beat_to_ms) / 1000f;
        notelenms = (int) (notelen * beat_to_ms);

        if (notes > 0) {
            shorts50ms = 100 * shorts50ms / (float) notes;
            shorts100ms = 100 * shorts100ms / (float) notes;
            holds1sec = 100 * holds1sec / (float) notes;
            holds3sec = 100 * holds3sec / (float) notes;
        }

        notesgap = notes - pages > 0 ? notesgap / (float) (notes - pages) : 0;
        notesgapms = (int) (notesgap * beat_to_ms);

        speedlen = lengthPerSecond(t);
        speeddist = notesPerSecond(t);

        if (pagelenmaxIndex < 0) {
            //pagelenmaxIndex = indexOf("pagelengthmax");
            //notelenmsIndex = indexOf("notelength");
            pagelensecIndex = indexOf("pagelengthsec");
            notelenmsIndex = indexOf("notelengthms");
            shorts50msIndex = indexOf("shorts50ms");
            shorts100msIndex = indexOf("shorts100ms");
            holds1secIndex = indexOf("holds1sec");
            holds3secIndex = indexOf("holds3sec");
            notesgapmsIndex = indexOf("notesgapms");
            speedlenIndex = indexOf("speedlen");
            speeddistIndex = indexOf("speeddist");
        }

        //s.setStatsAt(pagelenmaxIndex, pagelenmax);
        //s.setStatsAt(notelenIndex, notelenms);
        s.setStatsAt(pagelensecIndex, pagelensec);
        s.setStatsAt(notelenmsIndex, notelenms);
        s.setStatsAt(holds1secIndex, holds1sec);
        s.setStatsAt(holds3secIndex, holds3sec);
        s.setStatsAt(shorts50msIndex, shorts50ms);
        s.setStatsAt(shorts100msIndex, shorts100ms);
        s.setStatsAt(notesgapmsIndex, notesgapms);
        s.setStatsAt(speedlenIndex, speedlen);
        s.setStatsAt(speeddistIndex, speeddist);
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

        if (speedlenIndex < 0) {
            return false;
        }

        if (rule.equals("slow")) {
            float val = s.getStatsAt(speedlenIndex);
            boolean lenhit = val <= 3;

            val = s.getStatsAt(speeddistIndex);
            boolean disthit = val <= 2.5;

            hit = lenhit || disthit;
        }
        if (rule.equals("averagespeed")) {
            float val = s.getStatsAt(speedlenIndex);
            boolean lenhit = val > 3 && val < 5;

            val = s.getStatsAt(speeddistIndex);
            boolean disthit = val > 2.5 && val < 4;

            hit = lenhit && disthit;
        }
        if (rule.equals("fast")) {
            float val = s.getStatsAt(speedlenIndex);
            boolean lenhit = val >= 5;

            val = s.getStatsAt(speeddistIndex);
            boolean disthit = val >= 4;

            hit = lenhit || disthit;
        }
        if (rule.equals("longbreath")) {
            float val = s.getStatsAt(pagelensecIndex);
            boolean pagehit = val >= 8;

            val = s.getStatsAt(holds3secIndex);
            boolean holdhit = val >= 2;

            hit = pagehit || holdhit;
        }

        return hit;
    }
}

