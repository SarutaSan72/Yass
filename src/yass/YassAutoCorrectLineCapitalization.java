/*
 * Yass Reloaded - Karaoke Editor
 * Copyright (C) 2009-2023 Saruta
 * Copyright (C) 2023 DoubleDee
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

package yass;

import org.apache.commons.lang3.StringUtils;

public class YassAutoCorrectLineCapitalization extends YassAutoCorrector {
    public YassAutoCorrectLineCapitalization(YassProperties properties) {
        super(properties);
    }

    @Override
    public boolean autoCorrect(YassTable table, int currentRowIndex, int rowCount) {
        if (!this.properties.getBooleanProperty("capitalize-rows") || currentRowIndex < 2) {
            return false;
        }
        YassRow currentRow = table.getRowAt(currentRowIndex);
        YassRow previousRow = table.getRowAt(currentRowIndex - 1);
        boolean changed;
        if (!previousRow.isPageBreak()) {
            return false;
        }
        String text = currentRow.getText();
        if (YassUtils.isPunctuation(text.substring(0, 1)) && text.length() > 1) {
            text = text.substring(0, 1) + StringUtils.capitalize(text.substring(1));
        } else {
            text = StringUtils.capitalize(text);
        }
        changed = !text.equals(currentRow.getText());
        currentRow.setText(text);
        return changed;
    }
}
