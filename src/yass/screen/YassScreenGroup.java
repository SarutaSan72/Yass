package yass.screen;

import java.util.Vector;

/**
 *  Description of the Interface
 *
 * @author     Saruta
 * @created    22. M�rz 2010
 */
public class YassScreenGroup {
	private String group = null;
	private String rule = null;
	private Vector<?> matches = null;


	/**
	 *  Constructor for the YassScreenGroup object
	 *
	 * @param  rule     Description of the Parameter
	 * @param  matches  Description of the Parameter
	 * @param  group    Description of the Parameter
	 */
	public YassScreenGroup(String group, String rule, Vector<?> matches) {
		this.group = group;
		this.rule = rule;
		this.matches = matches;
	}


	/**
	 *  Gets the artist attribute of the YassSongData object
	 *
	 * @return    The artist value
	 */
	public String getTitle() {
		return group;
	}


	/**
	 *  Gets the filter attribute of the YassScreenGroup object
	 *
	 * @return    The filter value
	 */
	public String getFilter() {
		return rule;
	}


	/**
	 *  Gets the title attribute of the YassSongData object
	 *
	 * @return    The title value
	 */
	public Vector<?> getSongs() {
		return matches;
	}


	/**
	 *  Gets the songAt attribute of the YassScreenGroup object
	 *
	 * @param  i  Description of the Parameter
	 * @return    The songAt value
	 */
	public int getSongAt(int i) {
		return ((Integer) matches.elementAt(i)).intValue();
	}
}

