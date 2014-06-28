package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 22. August 2007
 */
public class LabelPanel extends OptionsPanel {

    private static final long serialVersionUID = 4053700583240795557L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        addText(I18.get("options_labels_cover"), "cover-id");
        addText(I18.get("options_labels_background"), "background-id");
        addText(I18.get("options_labels_video"), "video-id");
        addComment(I18.get("options_labels_comment"));
        addText(I18.get("options_labels_folder"), "videodir-id");
        addComment(I18.get("options_labels_folder_comment"));
    }
}

