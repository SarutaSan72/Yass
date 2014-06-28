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


    /**
     * Constructor for the YassUndoElement object
     *
     * @param d Description of the Parameter
     * @param r Description of the Parameter
     * @param p Description of the Parameter
     * @param w Description of the Parameter
     */
    public YassUndoElement(Vector<YassRow> d, int[] r, Point p, double w) {
        data = d;
        selectedRows = r;
        sheetViewPosition.setLocation(p.x, p.y);
        sheetBeatSize = w;
    }


    /**
     * Description of the Method
     *
     * @param r Description of the Parameter
     * @param p Description of the Parameter
     * @param w Description of the Parameter
     */
    public void set(int[] r, Point p, double w) {
        selectedRows = r;
        sheetViewPosition.setLocation(p.x, p.y);
        sheetBeatSize = w;
    }
}

