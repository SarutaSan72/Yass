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

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassGroupsModel extends AbstractTableModel {
    private static final long serialVersionUID = 5944503797621574207L;
    private static String[] columnNames = {"Group"};
    private Vector<String> data = new Vector<>();


    /**
     * Constructor for the YassSongListModel object
     */
    public YassGroupsModel() {
        super();
    }


    /**
     * Gets the data attribute of the YassSongListModel object
     *
     * @return The data value
     */
    public Vector<String> getData() {
        return data;
    }


    /**
     * Sets the data attribute of the YassSongListModel object
     *
     * @param d The new data value
     */
    public void setData(Vector<String> d) {
        data = d;
    }


    /**
     * Gets the columnCount attribute of the MyTableModel object
     *
     * @return The columnCount value
     */
    public int getColumnCount() {
        return columnNames.length;
    }


    /**
     * Gets the rowCount attribute of the MyTableModel object
     *
     * @return The rowCount value
     */
    public int getRowCount() {
        return data.size();
    }


    /**
     * Gets the columnName attribute of the MyTableModel object
     *
     * @param col Description of the Parameter
     * @return The columnName value
     */
    public String getColumnName(int col) {
        return columnNames[col];
    }


    /**
     * Gets the valueAt attribute of the YassGroupsModel object
     *
     * @param row Description of the Parameter
     * @param col Description of the Parameter
     * @return The valueAt value
     */
    public Object getValueAt(int row, int col) {
        if (row >= data.size()) {
            return null;
        }
        return data.elementAt(row);
    }


    /**
     * Gets the columnClass attribute of the MyTableModel object
     *
     * @param c Description of the Parameter
     * @return The columnClass value
     */
    public Class<?> getColumnClass(int c) {
        return String.class.getClass();
    }


    /**
     * Gets the cellEditable attribute of the MyTableModel object
     *
     * @param row Description of the Parameter
     * @param col Description of the Parameter
     * @return The cellEditable value
     */
    public boolean isCellEditable(int row, int col) {
        return false;
    }


    /**
     * Sets the valueAt attribute of the MyTableModel object
     *
     * @param value The new valueAt value
     * @param row   The new valueAt value
     * @param col   The new valueAt value
     */
    public void setValueAt(Object value, int row, int col) {
    }


    /**
     * Gets the rowAt attribute of the YassSongListModel object
     *
     * @param i Description of the Parameter
     * @return The rowAt value
     */
    public String getRowAt(int i) {
        if (i >= data.size()) {
            return null;
        }
        return data.elementAt(i);
    }


    /**
     * Adds a feature to the Row attribute of the YassGroupsModel object
     *
     * @param s The feature to be added to the Row attribute
     */
    public void addRow(String s) {
        data.addElement(s);
    }
}

