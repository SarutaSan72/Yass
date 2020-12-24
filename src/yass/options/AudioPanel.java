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
public class AudioPanel extends OptionsPanel {

    private static final long serialVersionUID = 6219009590405620627L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        setLabelWidth(155);
        addSeparator(I18.get("options_audio"));
        addText(I18.get("options_before_next_ms"), "before_next_ms");
        addComment(I18.get("options_before_next_comment"));
        addText(I18.get("options_seek_in"), "seek-in-offset");
        addText(I18.get("options_seek_out"), "seek-out-offset");
        addComment(I18.get("options_seek_comment"));
        addText(I18.get("options_seek_in_ms"), "seek-in-offset-ms");
        addText(I18.get("options_seek_out_ms"), "seek-out-offset-ms");
        addComment(I18.get("options_seek_comment_ms"));
        addSeparator(I18.get("options_piano"));
        addRadio(I18.get("options_piano_volume"), "piano-volume", "127|100|70", I18.get("options_piano_volume_max") + "|" + I18.get("options_piano_volume_med") + "|" + I18.get("options_piano_volume_min"));
        addSeparator(I18.get("options_video"));
        addBoolean("", "use-fobs", I18.get("options_use_fobs"));
        addComment(I18.get("options_use_fobs_comment"));
    }
}

