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

import java.util.Enumeration;
import java.util.Vector;

public class YassPage implements Comparable<Object> {
    private final Vector<YassRow> rows;
    private final YassTable table;
    private int player = 0;

    public YassPage(YassTable t) {
        table = t;
        rows = new Vector<>(16);
    }

    public Vector<YassRow> getRows() {
        return rows;
    }

    public void setRows(Vector<?> r) {
        for (Enumeration<?> en = r.elements(); en.hasMoreElements(); ) {
            addRow((YassRow) en.nextElement());
        }
    }

    public void addRow(YassRow r) {
        rows.addElement(r);
    }

    public void clear() {
        rows.removeAllElements();
    }

    public YassTable getTable() {
        return table;
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int p) {
        player = p;
    }

    public int getMinBeat() {
        int min = Integer.MAX_VALUE;
        for (YassRow r: rows) {
            if (r.isNote())
                min = Math.min(min, r.getBeatInt());
        }
        return min;
    }

    public int getMaxBeat() {
        int max = -1;
        for (YassRow r: rows) {
            if (r.isNote())
                max = Math.max(max, r.getBeatInt() + r.getLengthInt());
        }
        return max;
    }

    /**
     * Compares two pages for overlaps.
     * @param p another page
     * @return true if overlapping.
     */
    public boolean intersects(YassPage p) {
        int min = getMinBeat();
        int min2 = p.getMinBeat();
        int max = getMaxBeat();
        int max2 = p.getMaxBeat();
        return min <= min2 && max > min2 || min2 <= min && max2 > min;
    }

    /**
     * Compares first beat of both pages.
     * @return -1 if this page starts before the argument page,
     *          1 if the argument page starts before this page,
     *          0 if they start at the same beat
     * @param o the page to be compared
     */
    public int compareTo(Object o) {
        YassPage p = (YassPage) o;
        int min = getMinBeat();
        int min2 = p.getMinBeat();
        if (min < min2)
            return -1;
        if (min > min2)
            return 1;
        return 0;
    }

    /**
     * Check if two pages have the same notes, ignoring all non-note elements afterwards.
     * @param o other page
     * @return true if same notes
     */
    public boolean matches(Object o) {
        YassPage p = (YassPage) o;
        Enumeration<YassRow> en = rows.elements();
        Enumeration<YassRow> en2 = p.getRows().elements();
        // check if all notes match, stop at first not-note element
        while (en.hasMoreElements() && en2.hasMoreElements()) {
            YassRow r = en.nextElement();
            if (!r.isNote())
                break;
            YassRow r2 = en2.nextElement();
            if (!r2.isNote())
                break;
            if (!r.equals(r2))
                return false;
        }
        // after first not-note element, there is no other note
        while (en.hasMoreElements()) {
            YassRow r = en.nextElement();
            if (r.isNote()) {
                return false;
            }
        }
        while (en2.hasMoreElements()) {
            YassRow r2 = en2.nextElement();
            if (r2.isNote()) {
                return false;
            }
        }
        return true;
    }
}

