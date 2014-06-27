package yass.filter;

import yass.YassRow;
import yass.YassSong;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    4. März 2008
 */
public class YassErrorsFilter extends YassFilter {

	/**
	 *  Gets the iD attribute of the YassErrorsFilter object
	 *
	 * @return    The iD value
	 */
	public String getID() {
		return "errors";
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public boolean accept(YassSong s) {
		boolean hit = false;

		if (rule.equals("all")) {
			hit = true;
		} else if (rule.equals("all_errors")) {
			hit = s.hasMessage(YassRow.getMajorMessages());
			hit = hit || s.hasMessage(YassRow.getMinorPageBreakMessages());
			hit = hit || s.hasMessage(YassRow.getTagsMessages());
			hit = hit || s.hasMessage(YassRow.getTextMessages());
			hit = hit || s.hasMessage(YassRow.getFileMessages());
		} else if (rule.equals("major_errors")) {
			hit = s.hasMessage(YassRow.getMajorMessages());
		} else if (rule.equals("page_errors")) {
			hit = s.hasMessage(YassRow.getMinorPageBreakMessages());
		} else if (rule.equals("tag_errors")) {
			hit = s.hasMessage(YassRow.getTagsMessages());
		} else if (rule.equals("text_errors")) {
			hit = s.hasMessage(YassRow.getTextMessages());
		} else if (rule.equals("file_errors")) {
			hit = s.hasMessage(YassRow.getFileMessages());
		} else if (rule.equals("critical_errors")) {
			hit = s.hasMessage(YassRow.getCriticalMessages());
		}
		return hit;
	}
}

