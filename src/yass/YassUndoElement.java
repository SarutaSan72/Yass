package yass;

import java.awt.Point;
import java.util.Vector;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    11. September 2007
 */
public class YassUndoElement {
	/**
	 *  Description of the Field
	 */
	public Vector<YassRow> data = null;
	/**
	 *  Description of the Field
	 */
	public int[] selectedRows = null;
	/**
	 *  Description of the Field
	 */
	public Point sheetViewPosition = new Point(0, 0);
	/**
	 *  Description of the Field
	 */
	public double sheetBeatSize = 0;


	/**
	 *  Constructor for the YassUndoElement object
	 *
	 * @param  d  Description of the Parameter
	 * @param  r  Description of the Parameter
	 * @param  p  Description of the Parameter
	 * @param  w  Description of the Parameter
	 */
	public YassUndoElement(Vector<YassRow> d, int[] r, Point p, double w) {
		data = d;
		selectedRows = r;
		sheetViewPosition.setLocation(p.x, p.y);
		sheetBeatSize = w;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  r  Description of the Parameter
	 * @param  p  Description of the Parameter
	 * @param  w  Description of the Parameter
	 */
	public void set(int[] r, Point p, double w) {
		selectedRows = r;
		sheetViewPosition.setLocation(p.x, p.y);
		sheetBeatSize = w;
	}
}

