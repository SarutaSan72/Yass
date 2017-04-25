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
public class GroupPanel extends OptionsPanel {

    private static final long serialVersionUID = -4577091923631897645L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        addTextArea(I18.get("options_group1_title"), "group-title", 2);
        addTextArea(I18.get("options_group1_artist"), "group-artist", 2);
        addTextArea(I18.get("options_group1_genre"), "group-genre", 2);
        addTextArea(I18.get("options_group1_language"), "group-language", 2);
        addTextArea(I18.get("options_group1_edition"), "group-edition", 2);
        addTextArea(I18.get("options_group1_year"), "group-year", 2);
        addSeparator();
        addText(I18.get("options_group1_minsize"), "group-min");
        addComment(I18.get("options_group1_minsize_comment"));

    }
}

