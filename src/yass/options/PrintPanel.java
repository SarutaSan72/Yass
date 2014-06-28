package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 22. August 2007
 */
public class PrintPanel extends OptionsPanel {

    private static final long serialVersionUID = 1802331579466957409L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        addFile(I18.get("options_print_pdf"), "songlist-pdf");
        addComment(I18.get("options_print_pdf_comment"));
        addTextArea(I18.get("options_print_plugins"), "print-plugins", 3);
        addComment(I18.get("options_print_plugins_comment"));
    }
}

