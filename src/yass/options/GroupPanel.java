package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 22. August 2007
 */
public class GroupPanel extends OptionsPanel {

    private static final long serialVersionUID = -4577091923631897645L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        addTextArea(I18.get("options_group1_title"), "group-title", 2);
        addTextArea(I18.get("options_group1_artist"), "group-artist", 2);
        addTextArea(I18.get("options_group1_genre"), "group-genre", 2);
        addTextArea(I18.get("options_group1_language"), "group-language", 2);
        addTextArea(I18.get("options_group1_edition"), "group-edition", 2);
        addTextArea(I18.get("options_group1_year"), "group-year", 2);
        addSeparator();
        addText(I18.get("options_group1_minsize"), "group-min");
        addComment(I18.get("options_group1_minsize_comment"));

    }
}

