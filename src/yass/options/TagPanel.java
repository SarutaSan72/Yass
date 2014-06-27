package yass.options;

import yass.I18;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    22. August 2007
 */
public class TagPanel extends OptionsPanel {

	private static final long serialVersionUID = -677330599560709247L;

	/**
	 *  Gets the body attribute of the DirPanel object
	 */
	public void addRows() {
		addText(I18.get("options_tags_lines"), "valid-lines");
		addComment(I18.get("options_tags_lines_comment"));
		addTextArea(I18.get("options_tags_comments"), "valid-tags", 4);
		addComment(I18.get("options_tags_comments_comment"));
		addSeparator();
	}
}

