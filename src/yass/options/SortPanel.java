package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class SortPanel extends OptionsPanel {

    private static final long serialVersionUID = -3511309756748343798L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        addTextArea(I18.get("options_sorting_articles"), "articles", 5);
        addBoolean("", "use-articles", I18.get("options_sorting_articles_enable"));
        //addComment("Orders artists in library and print.");
    }
}

