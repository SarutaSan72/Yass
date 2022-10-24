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

import java.awt.*;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassUndoElement {
    public Vector<YassRow> data = null;
    public int[] selectedRows = null;
    public Point sheetViewPosition = new Point(0, 0);
    public double sheetBeatSize = 0;
    public double bpm, gap, start, end, vgap;
    public boolean isRelative;
    public boolean isSaved;

    public YassUndoElement(Vector<YassRow> d, int[] r, Point p, double w, double b, double g, double s, double e, double vg, boolean rel, boolean saved) {
        data = d;
        selectedRows = r;
        sheetViewPosition.setLocation(p.x, p.y);
        sheetBeatSize = w;
        bpm = b;
        gap = g;
        start = s;
        end = e;
        vgap = vg;
        isRelative = rel;
        isSaved = saved;
    }


    /**
     * Description of the Method
     *
     * @param r Description of the Parameter
     * @param p Description of the Parameter
     * @param w Description of the Parameter
     */
    public void set(int[] r, Point p, double w, double b, double g, double s, double e, double vg, boolean rel, boolean saved) {
        selectedRows = r;
        sheetViewPosition.setLocation(p.x, p.y);
        sheetBeatSize = w;
        bpm = b;
        gap = g;
        start = s;
        end = e;
        vgap = vg;
        isRelative = rel;
        isSaved = saved;
    }
}

