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

package yass;

import java.util.Vector;

public class YassTapNotes {
    public static void evaluateTaps(YassTable table, Vector<Long> taps) {
        if (taps == null) return;
        int n = taps.size();
        if (n < 2) {
            taps.clear();
            return;
        }
        if (n % 2 == 1) {
            taps.removeElementAt(n - 1);
            n--;
        }

        int tn = table.getRowCount();
        YassTableModel tm = (YassTableModel) table.getModel();

        double gap = table.getGap();
        double bpm = table.getBPM();

        // get first note that follows selection
        int t = table.getSelectionModel().getMinSelectionIndex();
        if (t < 0) t = 0;
        while (t < tn) {
            YassRow r = table.getRowAt(t);
            if (r.isNote()) break;
            t++;
        }

        int k = 0;
        while (k < n && t < tn) {
            YassRow r = table.getRowAt(t++);
            if (r.isNote()) {
                long tapBeat = taps.elementAt(k++).longValue();
                long tapBeat2 = taps.elementAt(k++).longValue();

                double ms = tapBeat / 1000.0 - gap;
                double ms2 = tapBeat2 / 1000.0 - gap;
                int beat = (int) Math.round((4 * bpm * ms / (60 * 1000)));
                int beat2 = (int) Math.round((4 * bpm * ms2 / (60 * 1000)));

                int length = beat2 - beat;

                if (length < 1) length = 1;
                r.setBeat(beat);
                r.setLength(length);
            }
        }

        tm.fireTableDataChanged();
        table.addUndo();
        table.repaint();
        taps.clear();
    }
}