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
public class LanguagePanel extends OptionsPanel {

    private static final long serialVersionUID = -4230333605266102703L;

    /**
     * Gets the body attribute of the LanguagePanel object
     */
    public void addRows() {
        addTextArea(I18.get("options_language_common"), "language-tag", 2);
        addTextArea(I18.get("options_language_others"), "language-more-tag", 5);
        addComment(I18.get("options_language_comment"));
        addSeparator();
        addTextArea(I18.get("options_tags_notenaming"), "note-naming-h", 1);
        addComment(I18.get("options_tags_notenaming_comment"));
    }
}

