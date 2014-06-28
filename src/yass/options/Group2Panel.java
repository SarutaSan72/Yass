package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class Group2Panel extends OptionsPanel {

    private static final long serialVersionUID = -4872178900558007241L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        addTextArea(I18.get("options_group2_album"), "group-album", 2);
        addTextArea(I18.get("options_group2_length"), "group-length", 2);
        addTextArea(I18.get("options_group2_folder"), "group-folder", 2);
        addTextArea(I18.get("options_group2_files"), "group-files", 2);
        addTextArea(I18.get("options_group2_errors"), "group-errors", 2);
        addSeparator();
        addTextArea(I18.get("options_group2_plugins"), "filter-plugins", 7);
    }
}

