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
public class DebugPanel extends OptionsPanel {

    private static final long serialVersionUID = 2888327974296685430L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        addSeparator(I18.get("options_debug_editor"));
        addBoolean(I18.get("options_debug_memory_title"), "debug-memory", I18.get("options_debug_memory"));
        addBoolean(I18.get("options_debug_accuracy_title"), "debug-waveform", I18.get("options_debug_waveform"));
        addSeparator(I18.get("options_debug_player"));
        addBoolean(I18.get("options_debug_score_title"), "debug-score", I18.get("options_debug_score"));
    }
}

