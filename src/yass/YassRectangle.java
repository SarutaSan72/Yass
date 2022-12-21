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

import java.awt.geom.RoundRectangle2D;

public class YassRectangle extends RoundRectangle2D.Double implements Cloneable {
    public final static int DEFAULT = 1;
    private int type;
    public final static int GOLDEN = 2;
    public final static int FREESTYLE = 4;
    public final static int WRONG = 8;
    public final static int GAP = 16;
    public final static int FIRST = 32;
    public final static int START = 64;
    public final static int END = 128;
    public final static int HEADER = 256;
    public final static int UNDEFINED = 512;

    public final static int RAP = 1024;
    public final static int RAPGOLDEN = 2048;

    private static final long serialVersionUID = -4749465399293425641L;
    int pn = -1;
    private int pageMin = 0;

    public YassRectangle() {
        super(0, 0, 0, 0, 10, 10);
        type = DEFAULT;
    }
    public YassRectangle(YassRectangle r) {
        super(r.x, r.y, r.width, r.height, 10, 10);
        type = r.type;
    }
    public int getPageMin() {
        return pageMin;
    }
    public void setPageMin(int m) {
        pageMin = m;
    }
    public boolean isType(int t) {
        return (type & t) == t;
    }
    public int getType() {
        return type;
    }
    public void setType(int t) {
        type = t;
    }
    public boolean hasType(int t) {
        return (type & t) != 0;
    }
    public void addType(int t) {
        type |= t;
    }
    public void resetType() {
        type = DEFAULT;
    }
    public void removeType(int t) {
        type &= ~t;
    }
    public boolean isPageBreak() {
        return pn >= 0;
    }
    public int getPageNumber() {
        return pn;
    }
    public void setPageNumber(int p) {
        pn = p;
    }
    public Object clone() {
        return new YassRectangle(this);
    }
}

