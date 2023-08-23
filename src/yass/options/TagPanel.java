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
public class TagPanel extends OptionsPanel {

    private static final long serialVersionUID = -677330599560709247L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        addText(I18.get("options_tags_lines"), "valid-lines");
        addComment(I18.get("options_tags_lines_comment"));
        addTextArea(I18.get("options_tags_comments"), "valid-tags", 4);
        addComment(I18.get("options_tags_comments_comment"));
        addRadio(I18.get("options_tags_duetsinger"), "duetsinger-tag", "P|DUETSINGERP", I18.get("options_tags_duetsinger_p") + "|" + I18.get("options_tags_duetsinger_duetsingerp"));
        addComment(I18.get("options_tags_duetsinger_comment"));
        addSeparator();
    }
}

