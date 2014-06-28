package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class GenrePanel extends OptionsPanel {

    private static final long serialVersionUID = 6200747739200627531L;

    /**
     * Gets the body attribute of the EditionPanel object
     */
    public void addRows() {
        addTextArea(I18.get("options_genre_common"), "genre-tag", 2);
        addTextArea(I18.get("options_genre_others"), "genre-more-tag", 5);
        addComment(I18.get("options_genre_comment"));
    }
}

