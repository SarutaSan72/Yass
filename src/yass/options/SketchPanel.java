package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 22. August 2007
 */
public class SketchPanel extends OptionsPanel {

    private static final long serialVersionUID = -8910897734777932450L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        setLabelWidth(100);
        addBoolean(I18.get("options_control_hover"), "mouseover", I18.get("options_control_hover_enable"));
        addComment(I18.get("options_control_hover_comment"));
        addBoolean(I18.get("options_control_trim"), "auto-trim", I18.get("options_control_trim_enable"));
        addComment(I18.get("options_control_trim_comment"));
        addBoolean(I18.get("options_control_gestures"), "sketching", I18.get("options_control_gestures_enable"));
        addComment(I18.get("options_control_gestures_comment"));
        addBoolean(I18.get("options_control_playback"), "sketching-playback", I18.get("options_control_playback_enable"));
        addComment(I18.get("options_control_playback_comment"));
        addBoolean("", "playback-buttons", I18.get("options_control_buttons_enable"));
        addComment(I18.get("options_control_buttons_comment"));

        addSeparator();
        addChoice(I18.get("options_control_mic_title"), getProperties().getProperty("control-mics"), "control-mics", "control-mic");
        addComment(I18.get("options_control_mic_comment"));
    }
}

