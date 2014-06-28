package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 22. August 2007
 */
public class EditionPanel extends OptionsPanel {

    private static final long serialVersionUID = -7922378987451949759L;

    /**
     * Gets the body attribute of the EditionPanel object
     */
    public void addRows() {
        addTextArea(I18.get("options_editions_common"), "edition-tag", 5);
        addComment(I18.get("options_editions_common_comment"));
    }
}

