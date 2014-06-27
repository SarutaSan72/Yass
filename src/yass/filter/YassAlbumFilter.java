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
public class YassAlbumFilter extends YassFilter {

	/**
	 *  Gets the iD attribute of the YassAlbumFilter object
	 *
	 * @return    The iD value
	 */
	public String getID() {
		return "album";
	}


	/**
	 *  Gets the genericRules attribute of the YassAlbumFilter object
	 *
	 * @param  data  Description of the Parameter
	 * @return       The genericRules value
	 */
	public String[] getGenericRules(Vector<YassSong> data) {
		Vector<String> albums = new Vector<String>();
		for (Enumeration<?> e = data.elements(); e.hasMoreElements(); ) {
			YassSong s = (YassSong) e.nextElement();
			String album = s.getAlbum();
			if (album == null || album.length() < 1) {
				continue;
			}
			if (!albums.contains(album)) {
				albums.addElement(album);

			}
		}
		Collections.sort(albums);

		return (String[]) albums.toArray(new String[]{});
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
		String old = s.getAlbum();
		if (old == null || old.equals(rule)) {
			return;
		}

		s.setAlbum(rule);
		s.setSaved(false);
	}


	/**
	 *  Gets the extraInfo attribute of the YassAlbumFilter object
	 *
	 * @return    The extraInfo value
	 */
	public int getExtraInfo() {
		return YassSongList.ALBUM_COLUMN;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public boolean accept(YassSong s) {
		String t = s.getAlbum();
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

