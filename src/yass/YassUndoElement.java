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
import java.util.Arrays;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassUndoElement {
    public final Vector<YassRow> data;
    public final int[] selectedRows;
    public final Point sheetViewPosition;
    public final double sheetBeatSize, bpm, gap, start, end, vgap;
    public final boolean isRelative, isSaved;
    public final int duetTrack, duetTrackCount;
    public final String duetTrackName;
    public final String[] duetSingerNames;

    public YassUndoElement(Vector<YassRow> d, int[] r, Point p, double w, double b, double g, double s, double e, double vg, boolean rel, boolean saved, int duetTrack, String duetTrackName, int duetTrackCount, String[] duetSingerNames) {
        data = d;
        selectedRows = r;
        sheetViewPosition = new Point(p.x, p.y);
        sheetBeatSize = w;
        bpm = b;
        gap = g;
        start = s;
        end = e;
        vgap = vg;
        isRelative = rel;
        isSaved = saved;
        this.duetTrack = duetTrack;
        this.duetTrackName = duetTrackName;
        this.duetTrackCount = duetTrackCount;
        this.duetSingerNames = Arrays.copyOf(duetSingerNames, duetSingerNames.length);
    }
}

