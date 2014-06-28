package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class AudioPanel extends OptionsPanel {

    private static final long serialVersionUID = 6219009590405620627L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        addSeparator(I18.get("options_audio"));
        addText(I18.get("options_seek_in"), "seek-in-offset");
        addText(I18.get("options_seek_out"), "seek-out-offset");
        addComment(I18.get("options_seek_comment"));
        addSeparator(I18.get("options_video"));
        addBoolean("", "use-fobs", I18.get("options_use_fobs"));
        addComment(I18.get("options_use_fobs_comment"));
    }
}

