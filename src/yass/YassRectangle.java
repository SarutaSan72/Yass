package yass;

import java.awt.geom.RoundRectangle2D;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    21. Februar 2009
 */
public class YassRectangle extends RoundRectangle2D.Double implements Cloneable {
	private static final long serialVersionUID = -4749465399293425641L;

	private int type = DEFAULT;

	/**
	 *  Description of the Field
	 */
	public final static int INVALID = 0;

	/**
	 *  Description of the Field
	 */
	public final static int DEFAULT = 1;
	/**
	 *  Description of the Field
	 */
	public final static int GOLDEN = 2;
	/**
	 *  Description of the Field
	 */
	public final static int FREESTYLE = 4;
	/**
	 *  Description of the Field
	 */
	public final static int WRONG = 8;
	/**
	 *  Description of the Field
	 */
	public final static int GAP = 16;
	/**
	 *  Description of the Field
	 */
	public final static int FIRST = 32;
	/**
	 *  Description of the Field
	 */
	public final static int START = 64;
	/**
	 *  Description of the Field
	 */
	public final static int END = 128;
	/**
	 *  Description of the Field
	 */
	public final static int HEADER = 256;
	/**
	 *  Description of the Field
	 */
	public final static int UNDEFINED = 512;

	private int pageMin = 0;


	/**
	 *  Sets the pageMin attribute of the YassRectangle object
	 *
	 * @param  m  The new pageMin value
	 */
	public void setPageMin(int m) {
		pageMin = m;
	}


	/**
	 *  Gets the pageMin attribute of the YassRectangle object
	 *
	 * @return    The pageMin value
	 */
	public int getPageMin() {
		return pageMin;
	}


	/**
	 *  Gets the type attribute of the YassRectangle object
	 *
	 * @param  t  Description of the Parameter
	 * @return    The type value
	 */
	public boolean isType(int t) {
		return (type & t) == t;
	}


	/**
	 *  Gets the type attribute of the YassRectangle object
	 *
	 * @return    The type value
	 */
	public int getType() {
		return type;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  t  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public boolean hasType(int t) {
		return (type & t) != 0;
	}


	/**
	 *  Sets the type attribute of the YassRectangle object
	 *
	 * @param  t  The new type value
	 */
	public void setType(int t) {
		type = t;
	}


	/**
	 *  Adds a feature to the Type attribute of the YassRectangle object
	 *
	 * @param  t  The feature to be added to the Type attribute
	 */
	public void addType(int t) {
		type |= t;
	}


	/**
	 *  Description of the Method
	 */
	public void resetType() {
		type = DEFAULT;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  t  Description of the Parameter
	 */
	public void removeType(int t) {
		type &= ~t;
	}


	int pn = -1;
	// int nn=-1;

	/**
	 *  Gets the pageBreak attribute of the YassRectangle object
	 *
	 * @return    The pageBreak value
	 */
	public boolean isPageBreak() {
		return pn >= 0;
	}


	/**
	 *  Gets the pageNumber attribute of the YassRectangle object
	 *
	 * @return    The pageNumber value
	 */
	public int getPageNumber() {
		return pn;
	}


	/**
	 *  Sets the pageNumber attribute of the YassRectangle object
	 *
	 * @param  p  The new pageNumber value
	 */
	public void setPageNumber(int p) {
		pn = p;
	}
	//public void getNoteNumber() { return nn; }
	//public void setNoteNumber(int n) { nn=n; }

	/**
	 *  Constructor for the YassRectangle object
	 */
	public YassRectangle() {
		super(0, 0, 0, 0, 10, 10);
		type = DEFAULT;
	}


	/**
	 *  Constructor for the YassRectangle object
	 *
	 * @param  r  Description of the Parameter
	 */
	public YassRectangle(YassRectangle r) {
		super(r.x, r.y, r.width, r.height, 10, 10);
		type = r.type;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public Object clone() {
		return new YassRectangle(this);
	}
}

