package yass.filter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import yass.YassSong;
import yass.YassSongList;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    4. März 2008
 */
public class YassEditionFilter extends YassFilter {

	/**
	 *  Gets the iD attribute of the YassEditionFilter object
	 *
	 * @return    The iD value
	 */
	public String getID() {
		return "edition";
	}



	/**
	 *  Gets the genericRules attribute of the YassEditionFilter object
	 *
	 * @param  data  Description of the Parameter
	 * @return       The genericRules value
	 */
	public String[] getGenericRules(Vector<YassSong> data) {
		Vector<String> editions = new Vector<String>();
		for (Enumeration<YassSong> e = data.elements(); e.hasMoreElements(); ) {
			YassSong s = (YassSong) e.nextElement();
			String edition = s.getEdition();
			if (edition == null || edition.length() < 1) {
				continue;
			}
			if (!editions.contains(edition)) {
				editions.addElement(edition);
			}
		}
		Collections.sort(editions);

		return (String[]) editions.toArray(new String[]{});
	}


	/**
	 *  Gets the extraInfo attribute of the YassEditionFilter object
	 *
	 * @return    The extraInfo value
	 */
	public int getExtraInfo() {
		return YassSongList.EDITION_COLUMN;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  rule  Description of the Parameter
	 * @return       Description of the Return Value
	 */
	public boolean allowDrop(String rule) {
		if (rule.equals("all")) {
			return false;
		}
		if (rule.equals("unspecified")) {
			return false;
		}
		return true;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  rule  Description of the Parameter
	 * @return       Description of the Return Value
	 */
	public boolean allowCoverDrop(String rule) {
		if (rule.equals("all")) {
			return false;
		}
		if (rule.equals("unspecified")) {
			return false;
		}
		return true;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  rule  Description of the Parameter
	 * @param  s     Description of the Parameter
	 */
	public void drop(String rule, YassSong s) {
		String old = s.getEdition();
		if (old == null || old.equals(rule)) {
			return;
		}

		s.setEdition(rule);
		s.setSaved(false);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public boolean accept(YassSong s) {
		String t = s.getEdition();
		boolean hit = false;

		if (rule.equals("all")) {
			hit = true;
		} else if (rule.equals("unspecified")) {
			if (t == null || t.length() < 1) {
				hit = true;
			}
		} else {
			hit = t.equals(rule);
		}

		return hit;
	}
}

