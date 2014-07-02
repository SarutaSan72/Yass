package yass;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassPage implements Comparable<Object> {
    private Vector<YassRow> rows = null;
    private YassTable table = null;
    private int player = 0;


    /**
     * Constructor for the YassPage object
     *
     * @param t Description of the Parameter
     */
    public YassPage(YassTable t) {
        table = t;
        rows = new Vector<>(16);
    }

    /**
     * Gets the rows attribute of the YassPage object
     *
     * @return The rows value
     */
    public Vector<YassRow> getRows() {
        return rows;
    }

    /**
     * Sets the rows attribute of the YassPage object
     *
     * @param r The new rows value
     */
    public void setRows(Vector<?> r) {
        for (Enumeration<?> en = r.elements(); en.hasMoreElements(); ) {
            addRow((YassRow) en.nextElement());
        }
    }

    /**
     * Adds a feature to the Row attribute of the YassPage object
     *
     * @param r The feature to be added to the Row attribute
     */
    public void addRow(YassRow r) {
        rows.addElement(r);
    }


    /**
     * Description of the Method
     */
    public void clear() {
        rows.removeAllElements();
    }


    /**
     * Gets the table attribute of the YassPage object
     *
     * @return The table value
     */
    public YassTable getTable() {
        return table;
    }


    /**
     * Gets the minBeat attribute of the YassPage object
     *
     * @return The minBeat value
     */
    public int getMinBeat() {
        int min = Integer.MAX_VALUE;
        for (Enumeration<YassRow> en = rows.elements(); en.hasMoreElements(); ) {
            YassRow r = en.nextElement();
            if (r.isNote()) {
                min = Math.min(min, r.getBeatInt());
            }
        }
        return min;
    }


    /**
     * Gets the maxBeat attribute of the YassPage object
     *
     * @return The maxBeat value
     */
    public int getMaxBeat() {
        int max = -1;
        for (Enumeration<YassRow> en = rows.elements(); en.hasMoreElements(); ) {
            YassRow r = en.nextElement();
            if (r.isNote()) {
                max = Math.max(max, r.getBeatInt() + r.getLengthInt());
            }
        }
        return max;
    }


    /**
     * Description of the Method
     *
     * @param p Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean intersects(YassPage p) {
        int min = getMinBeat();
        int min2 = p.getMinBeat();

        int max = getMaxBeat();
        int max2 = p.getMaxBeat();

        return min <= min2 && max > min2 || min2 <= min && max2 > min;
    }


    /**
     * Description of the Method
     *
     * @param o Description of the Parameter
     * @return Description of the Return Value
     */
    public int compareTo(Object o) {
        YassPage p = (YassPage) o;

        int min = getMinBeat();
        int min2 = p.getMinBeat();
        if (min < min2) {
            return -1;
        }
        if (min > min2) {
            return 1;
        }
        return 0;
    }


    /**
     * Description of the Method
     *
     * @param o Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean matches(Object o) {
        YassPage p = (YassPage) o;

        Enumeration<YassRow> en = rows.elements();
        Enumeration<YassRow> en2 = p.getRows().elements();

        while (en.hasMoreElements() && en2.hasMoreElements()) {
            YassRow r = en.nextElement();
            if (!r.isNote()) {
                break;
            }

            YassRow r2 = en2.nextElement();
            if (!r2.isNote()) {
                break;
            }

            if (!r.equals(r2)) {
                return false;
            }
        }

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

