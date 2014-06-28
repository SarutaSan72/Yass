package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class LanguagePanel extends OptionsPanel {

    private static final long serialVersionUID = -4230333605266102703L;

    /**
     * Gets the body attribute of the LanguagePanel object
     */
    public void addRows() {
        addTextArea(I18.get("options_language_common"), "language-tag", 2);
        addTextArea(I18.get("options_language_others"), "language-more-tag", 5);
        addComment(I18.get("options_language_comment"));
        addSeparator();
        addTextArea(I18.get("options_tags_notenaming"), "note-naming-h", 1);
        addComment(I18.get("options_tags_notenaming_comment"));
    }
}

