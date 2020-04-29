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
public class FontErrorPanel extends OptionsPanel {

    private static final long serialVersionUID = -7477393618838320424L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        setLabelWidth(190);

        addChoice(I18.get("options_errors_font_file"), getProperties().getProperty("font-files"), "font-files", "font-file");
        addFile("", "font-file-custom");
        addComment(I18.get("options_errors_font_file_comment"));

        addText(I18.get("options_errors_font_size"), "font-size");

        addText(I18.get("options_errors_font_spacing"), "char-spacing");
        addComment(I18.get("options_errors_font_spacing_comment"));

        addText(I18.get("options_errors_font_max_width"), "text-max-width");
        addComment(I18.get("options_errors_font_max_width_comment"));
    }
}


