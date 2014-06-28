package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 22. August 2007
 */
public class DictionaryPanel extends OptionsPanel {

    private static final long serialVersionUID = 8446040398070422620L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        addText(I18.get("options_dict_hyphenation"), "hyphenations");
        addComment(I18.get("options_dict_hyphenation_comment"));
        addText(I18.get("options_dict_spelling"), "dicts");
        addComment(I18.get("options_dict_spelling_comment"));
        addTextArea(I18.get("options_dict_languages"), "dict-map", 2);
        addComment(I18.get("options_dict_languages_comment"));
        addDirectory(I18.get("options_dict_user"), "user-dicts");
        addComment(I18.get("options_dict_user_comment"));
        addComment(I18.get("options_dict_comment"));
    }
}

