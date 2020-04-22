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

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassRectangle extends RoundRectangle2D.Double implements Cloneable {
    /**
     * Description of the Field
     */
    public final static int INVALID = 0;
    /**
     * Description of the Field
     */
    public final static int DEFAULT = 1;
    private int type = DEFAULT;
    /**
     * Description of the Field
     */
    public final static int GOLDEN = 2;
    /**
     * Description of the Field
     */
    public final static int FREESTYLE = 4;
    /**
     * Description of the Field
     */
    public final static int WRONG = 8;
    /**
     * Description of the Field
     */
    public final static int GAP = 16;
    /**
     * Description of the Field
     */
    public final static int FIRST = 32;
    /**
     * Description of the Field
     */
    public final static int START = 64;
    /**
     * Description of the Field
     */
    public final static int END = 128;
    /**
     * Description of the Field
     */
    public final static int HEADER = 256;
    /**
     * Description of the Field
     */
    public final static int UNDEFINED = 512;

    public final static int RAP = 1024;
    public final static int RAPGOLDEN = 2048;

    private static final long serialVersionUID = -4749465399293425641L;
    int pn = -1;
    private int pageMin = 0;


    /**
     * Constructor for the YassRectangle object
     */
    public YassRectangle() {
        super(0, 0, 0, 0, 10, 10);
        type = DEFAULT;
    }


    /**
     * Constructor for the YassRectangle object
     *
     * @param r Description of the Parameter
     */
    public YassRectangle(YassRectangle r) {
        super(r.x, r.y, r.width, r.height, 10, 10);
        type = r.type;
    }

    /**
     * Gets the pageMin attribute of the YassRectangle object
     *
     * @return The pageMin value
     */
    public int getPageMin() {
        return pageMin;
    }

    /**
     * Sets the pageMin attribute of the YassRectangle object
     *
     * @param m The new pageMin value
     */
    public void setPageMin(int m) {
        pageMin = m;
    }

    /**
     * Gets the type attribute of the YassRectangle object
     *
     * @param t Description of the Parameter
     * @return The type value
     */
    public boolean isType(int t) {
        return (type & t) == t;
    }

    /**
     * Gets the type attribute of the YassRectangle object
     *
     * @return The type value
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the type attribute of the YassRectangle object
     *
     * @param t The new type value
     */
    public void setType(int t) {
        type = t;
    }

    /**
     * Description of the Method
     *
     * @param t Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean hasType(int t) {
        return (type & t) != 0;
    }

    /**
     * Adds a feature to the Type attribute of the YassRectangle object
     *
     * @param t The feature to be added to the Type attribute
     */
    public void addType(int t) {
        type |= t;
    }
    // int nn=-1;

    /**
     * Description of the Method
     */
    public void resetType() {
        type = DEFAULT;
    }

    /**
     * Description of the Method
     *
     * @param t Description of the Parameter
     */
    public void removeType(int t) {
        type &= ~t;
    }

    /**
     * Gets the pageBreak attribute of the YassRectangle object
     *
     * @return The pageBreak value
     */
    public boolean isPageBreak() {
        return pn >= 0;
    }
    //public void getNoteNumber() { return nn; }
    //public void setNoteNumber(int n) { nn=n; }

    /**
     * Gets the pageNumber attribute of the YassRectangle object
     *
     * @return The pageNumber value
     */
    public int getPageNumber() {
        return pn;
    }

    /**
     * Sets the pageNumber attribute of the YassRectangle object
     *
     * @param p The new pageNumber value
     */
    public void setPageNumber(int p) {
        pn = p;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public Object clone() {
        return new YassRectangle(this);
    }
}

