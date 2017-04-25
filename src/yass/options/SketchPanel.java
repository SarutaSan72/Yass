/*
 * Yass - Karaoke Editor
 * Copyright (C) 2009 Saruta
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
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

