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

public class YassAutoCorrectUncommonSpacing extends YassAutoCorrector {
    public YassAutoCorrectUncommonSpacing(YassProperties properties) {
        super(properties);
    }

    public boolean autoCorrect(YassTable table, int currentRowIndex, int rowCount) {
        YassRow currentRow = table.getRowAt(currentRowIndex);
        YassRow nextRow = table.getRowAt(currentRowIndex + 1);
        boolean changed;
        if (!currentRow.isNote()) {
            return false;
        }
        if (properties.isUncommonSpacingAfter()) {
            changed = autoCorrectSpacing(currentRow, nextRow, currentRowIndex, rowCount);
        } else {
            changed = autoCorrectLegacySpacing(currentRow, nextRow, currentRowIndex, rowCount);
        }
        return changed;
    }

    private boolean autoCorrectSpacing(YassRow currentRow, YassRow nextRow, int currentRowIndex, int rowCount) {
        boolean changed = false;
        if (currentRow.startsWithSpace() || !currentRow.endsWithSpace()) {
            String txt = currentRow.getText();
            int textStart = currentRow.startsWithSpace() ? 1 : 0;
            boolean changeSpace;
            if (currentRowIndex + 1 < rowCount) {
                changeSpace = nextRow.startsWithSpace() || !nextRow.isNote();
            } else {
                changeSpace = true;
            }
            changed = true;
            currentRow.setText(txt.substring(textStart) + (changeSpace ? YassRow.SPACE : ""));
        }
        return changed;
    }

    private boolean autoCorrectLegacySpacing(YassRow currentRow, YassRow nextRow, int currentRowIndex, int rowCount) {
        boolean changed = false;
        if (currentRow.endsWithSpace()) {
            String txt = currentRow.getText();
            currentRow.setText(txt.substring(0, txt.length() - 1));
            if (currentRowIndex + 1 < rowCount) {
                if (nextRow.isNote()) {
                    nextRow.setText(YassRow.SPACE + nextRow.getText());
                }
            }
            changed = true;
        }
        return changed;
    }
}