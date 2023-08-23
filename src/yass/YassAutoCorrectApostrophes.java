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

import java.util.Arrays;
import java.util.List;

public class YassAutoCorrectApostrophes extends YassAutoCorrector {

    public static final List<String> BORING_APOSTROPHES = Arrays.asList("'", "′", "´", "`", "‵", "ʹ", "＇");

    public YassAutoCorrectApostrophes(YassProperties properties) {
        super(properties);
    }

    @Override
    public boolean autoCorrect(YassTable table, int currentRowIndex, int rowCount) {
        if (!this.properties.getBooleanProperty("typographic-apostrophes")) {
            return false;
        }
        YassRow currentRow = table.getRowAt(currentRowIndex);
        String text = currentRow.isComment() ? currentRow.getComment() : currentRow.getText();
        String newText = text;
        for (String apostrophe : BORING_APOSTROPHES) {
            newText = newText.replace(apostrophe, "’");
        }
        boolean changed = !text.equals(newText);
        if (currentRow.isComment()) {
            currentRow.setComment(newText);
        } else {
            currentRow.setText(newText);
        }
        return changed;
    }
}
