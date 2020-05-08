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

package yass;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassTableRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = -8540061131858129154L;
    private static Color red = new Color(1f, .8f, .8f);
    private static Color darkRed = new Color(.8f, .5f, .5f);


    public static void setColors(Color[] colorSet) {
        red = colorSet[YassSheet.COLOR_ERROR];
        darkRed = colorSet[YassSheet.COLOR_WARNING];
    }
    /**
     * Gets the tableCellRendererComponent attribute of the YassTableRenderer
     * object
     *
     * @param table      Description of the Parameter
     * @param value      Description of the Parameter
     * @param isSelected Description of the Parameter
     * @param hasFocus   Description of the Parameter
     * @param row        Description of the Parameter
     * @param column     Description of the Parameter
     * @return The tableCellRendererComponent value
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if ((column == 4) || table.getValueAt(row, 0).equals("#")) {
            setHorizontalAlignment(LEFT);
        } else {
            setHorizontalAlignment(RIGHT);
        }

        YassTable t = ((YassTable) table);
        boolean showMessages = t.showMessages();

        YassRow r = t.getRowAt(row);
        boolean hidden = r.isHidden();
        if (hidden) {
            setText("");
        }

        if ((!(r.isNote())) && !isSelected) {
            if (r.hasMessage() && showMessages && !hidden) {
                cell.setBackground(darkRed);
            } else {
                cell.setBackground(Color.lightGray);
            }
        } else if (!isSelected) {
            if (r.hasMessage() && showMessages && !hidden) {
                cell.setBackground(red);
            } else {
                cell.setBackground(Color.white);
            }

        }
        return this;
    }
}

