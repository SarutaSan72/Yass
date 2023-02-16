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
import yass.YassSheet;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class ColorPanel extends OptionsPanel {
    private static final long serialVersionUID = -3441746602050223699L;

    /**
     * Gets the body attribute of the ColorPanel object
     */
    public void addRows() {
        setLabelWidth(100);
        addSeparator(I18.get("options_design_dark_mode"));
        addBoolean("", "dark-mode", I18.get("options_design_dark_mode_enabled"));
        addSeparator(I18.get("options_design_versions"));
        addColorSet(I18.get("options_design_versions_colors"), "color", 8, null);
        addSeparator(I18.get("options_design_notes"));
        String s = I18.get("options_design_notes_colors_names");
        String[] names = s != null ? s.split("\\|") : null;
        addColorSet(I18.get("options_design_notes_colors"), "note-color", YassSheet.COLORSET_COUNT, names);
        addBoolean("", "shade-notes", I18.get("options_design_notes_shade"));
        addBoolean("", "show-note-beat", I18.get("options_design_notes_beat"));
        addBoolean("", "show-note-heightnum", I18.get("options_design_notes_heightnum"));
        addBoolean("", "show-note-height", I18.get("options_design_notes_height"));
        addBoolean("", "show-note-scale", I18.get("options_design_notes_scale"));
        addBoolean("", "show-note-length", I18.get("options_design_notes_length"));

        addSeparator(I18.get("options_design_lyrics"));
        addChoice(I18.get("options_design_lyrics_layout_title"), I18.get("options_design_lyrics_layout"), "editor-layouts", "editor-layout");
        addText(I18.get("options_design_lyrics_width"), "lyrics-width");
        addText(I18.get("options_design_lyrics_fontsize"), "lyrics-font-size");
        addComment(I18.get("options_design_lyrics_fontsize_comment"));

        addSeparator(I18.get("options_design_feedback"));
        addBoolean("", "use-sample", I18.get("options_design_sampled_clicks"));
    }

}

