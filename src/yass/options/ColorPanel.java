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
public class ColorPanel extends OptionsPanel {
    private static final long serialVersionUID = -3441746602050223699L;

    /**
     * Gets the body attribute of the ColorPanel object
     */
    public void addRows() {
        setLabelWidth(140);
        addSeparator(I18.get("options_design_versions"));
        addColorSet(I18.get("options_design_versions_colors"), "color", 8);
        addSeparator(I18.get("options_design_notes"));
        addColorSet(I18.get("options_design_notes_colors"), "note-color", 7);
        addBoolean("", "shade-notes", I18.get("options_design_notes_shade"));
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

        //addSeparator(I18.get("options_design_tools"));
        //addBoolean("", "expert", I18.get("options_design_tools_expert"));
        //addBoolean("", "floatable", I18.get("options_design_tools_floatable"));
        //addComment(I18.get("options_design_tools_comment"));

		/*
         *  addColor("Standard:", "color-0");
		 *  addColor("Version 1:", "color-1");
		 *  addColor("Version 2:", "color-2");
		 *  addColor("Version 3:", "color-3");
		 *  addColor("Version 4:", "color-4");
		 *  addColor("Version 5:", "color-5");
		 *  addColor("Version 6:", "color-6");
		 *  addColor("Version 7:", "color-7");
		 *  addColor("Version 8:", "color-8");
		 *  addColor("Version 9:", "color-9");
		 */
    }

}

