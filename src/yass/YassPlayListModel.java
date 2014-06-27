package yass;

import java.util.Vector;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    21. August 2007
 */
public class YassPlayListModel extends Vector<Object> implements Comparable<Object> {
	private static final long serialVersionUID = 3473486648850642487L;
	String name = null;
	String filename = null;


	/**
	 *Constructor for the PlayListModel object
	 */
	public YassPlayListModel() {
		super();
		name = "";
	}


	/**
	 *  Sets the name attribute of the PlayListModel object
	 *
	 * @param  s  The new name value
	 */
	public void setName(String s) {
		name = s;
	}


	/**
	 *  Sets the fileName attribute of the YassPlayListModel object
	 *
	 * @param  s  The new fileName value
	 */
	public void setFileName(String s) {
		filename = s;
	}


	/**
	 *  Gets the name attribute of the PlayListModel object
	 *
	 * @return    The name value
	 */
	public String getName() {
		return name;
	}


	/**
	 *  Gets the fileName attribute of the YassPlayListModel object
	 *
	 * @return    The fileName value
	 */
	public String getFileName() {
		return filename;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  o  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public int compareTo(Object o) {
		return getName().compareTo(((YassPlayListModel) o).getName());
	}
}


