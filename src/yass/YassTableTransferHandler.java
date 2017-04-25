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
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassTableTransferHandler extends YassStringTransferHandler {
    private static final long serialVersionUID = -8972312557589267911L;
    private int[] rows = null;
    private int addIndex = -1;
    //Location where items were added
    private int addCount = 0;


    //Number of items added.

    /**
     * Description of the Method
     *
     * @param c Description of the Parameter
     * @return Description of the Return Value
     */
    protected String exportString(JComponent c) {
        YassTable table = (YassTable) c;
        rows = table.getSelectedRows();
        if (rows != null && rows.length == 0) {
            rows = null;
            return "";
        }
        return table.getSelectedRowsAsString();
    }


    /**
     * Description of the Method
     *
     * @param c   Description of the Parameter
     * @param str Description of the Parameter
     */
    protected void importString(JComponent c, String str) {
        YassTable target = (YassTable) c;
        addIndex = target.getSelectedRow();
        if (addIndex < 0) {
            return;
        }

        addCount = target.insertRowsAt(str, addIndex, true);
    }


    /**
     * Description of the Method
     *
     * @param c      Description of the Parameter
     * @param remove Description of the Parameter
     */
    protected void cleanup(JComponent c, boolean remove) {
        YassTable source = (YassTable) c;
        if (remove && rows != null) {
            YassTableModel model = (YassTableModel) source.getModel();
            Vector<YassRow> data = model.getData();

            //If we are moving items around in the same table, we
            //need to adjust the rows accordingly, since those
            //after the insertion point have moved.
            if (addCount > 0) {
                for (int i = 0; i < rows.length; i++) {
                    if (rows[i] > addIndex) {
                        rows[i] += addCount;
                    }
                }
            }
            for (int i = rows.length - 1; i >= 0; i--) {
                data.removeElementAt(rows[i]);
            }
            model.fireTableDataChanged();
        }
        rows = null;
        addCount = 0;
        addIndex = -1;
    }
}


