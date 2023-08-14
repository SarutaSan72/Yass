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
public class ErrorPanel extends OptionsPanel {

    private static final long serialVersionUID = -7477393618838320424L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        setLabelWidth(110);

        addBoolean(I18.get("options_errors_touching"), "touching-syllables", I18.get("options_errors_touching_syllables"));
        addRadio(I18.get("options_errors_pages"), "correct-uncommon-pagebreaks", "true|false|unknown", I18.get("options_errors_pages_true") + "|" + I18.get("options_errors_pages_false") + "|" + I18.get("options_errors_pages_unknown"));
        addComment(I18.get("options_errors_pages_comment"));
        addText(I18.get("options_errors_pages_fix"), "correct-uncommon-pagebreaks-fix");
        addComment(I18.get("options_errors_pages_fix_comment"));
        addRadio(I18.get("options_errors_spacing"), "correct-uncommon-spacing", "after|before", I18.get("options_errors_uncommon_spacing_after") + "|" + I18.get("options_errors_uncommon_spacing_before"));
        addComment(I18.get("options_errors_spacing_comment"));
        addBoolean(I18.get("options_errors_apostrophes"), "typographic-apostrophes", I18.get("options_errors_apostrophes_typographic"));
        addBoolean(I18.get("options_errors_captilization"), "capitalize-rows", I18.get("options_errors_captilization_all_rows"));
    }
}


