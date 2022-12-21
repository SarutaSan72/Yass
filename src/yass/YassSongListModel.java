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

import yass.stats.YassStats;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * Descriptinoutn of the Class
 *
 * @author Saruta
 */
public class YassSongListModel extends AbstractTableModel {
    // private static String[] columnNames = {"", "Directory", "Folder", "Filename", "Cover", "Background", "Video", "Artist", "Title", "Edition", "Genre", "Language"};
    private static String[] columnNames = {"", I18.get("songlist_col_1"), I18.get("songlist_col_2"), I18.get("songlist_col_3"), I18.get("songlist_col_4"), I18.get("songlist_col_5"), I18.get("songlist_col_6"), I18.get("songlist_col_7"), I18.get("songlist_col_8"), I18.get("songlist_col_9"), I18.get("songlist_col_10"), I18.get("songlist_col_11"), I18.get("songlist_col_12")};
    private Vector<YassSong> data = new Vector<>();


    /**
     * Constructor for the YassSongListModel object
     */
    public YassSongListModel() {
        super();
    }


    /**
     * Gets the data attribute of the YassSongListModel object
     *
     * @return The data value
     */
    public Vector<YassSong> getData() {
        return data;
    }


    /**
     * Sets the data attribute of the YassSongListModel object
     *
     * @param d The new data value
     */
    public void setData(Vector<YassSong> d) {
        data = d;
    }


    /**
     * Gets the columnCount attribute of the MyTableModel object
     *
     * @return The columnCount value
     */
    public int getColumnCount() {
        return columnNames.length + YassRow.ALL_MESSAGES.length + YassStats.length;
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
     * @param col Descriptinoutn of the Parameter
     * @return The columnName value
     */
    public String getColumnName(int col) {
        if (col < columnNames.length) {
            return columnNames[col];
        }
        if (col < columnNames.length + YassRow.ALL_MESSAGES.length) {
            return YassRow.ALL_MESSAGES[col - columnNames.length];
        }
        return YassStats.getIDAt(col - columnNames.length - YassRow.ALL_MESSAGES.length);
    }


    /**
     * Gets the valueAt attribute of the MyTableModel object
     *
     * @param row Descriptinoutn of the Parameter
     * @param col Descriptinoutn of the Parameter
     * @return The valueAt value
     */
    public Object getValueAt(int row, int col) {
        if (row >= data.size()) {
            return null;
        }
        YassSong r = data.elementAt(row);
        if (col < columnNames.length) {
            switch (col) {
                case 1:
                    return r.getComplete();
                case 2:
                    return r.getArtist();
                case 3:
                    return r.getTitle();
                case 4:
                    return r.getDuetSingerNames();
                case 5:
                    return r.getGenre();
                case 6:
                    return r.getEdition();
                case 7:
                    return r.getLanguage();
                case 8:
                    return r.getYear();
                case 9:
                    return r.getFolder();
                case 10:
                    return r.getAlbum();
                case 11:
                    return r.getLength();
                case 12:
                    return r.getID();
            }
        }
        if (col < columnNames.length + YassRow.ALL_MESSAGES.length) {
            if (!r.hasMessages()) {
                return "";
            }
            String msg = r.getMessage(col - columnNames.length);
            if (msg == null) {
                return "";
            }
            return msg;
        }

        float st = r.getStatsAt(col - columnNames.length - YassRow.ALL_MESSAGES.length);
        if (st < 0) {
            return "";
        }
        st = ((int) (st * 10)) / 10f;
        String str = st + "";
        if (str.endsWith(".0")) {
            str = str.substring(0, str.length() - 2);
        }
        return str;
    }


    /**
     * Gets the columnClass attribute of the MyTableModel object
     *
     * @param c Descriptinoutn of the Parameter
     * @return The columnClass value
     */
    public Class<?> getColumnClass(int c) {
        Object o = getValueAt(0, c);
        if (o == null) {
            return String.class.getClass();
        }
        return o.getClass();
    }


    /**
     * Gets the cellEditable attribute of the MyTableModel object
     *
     * @param row Descriptinoutn of the Parameter
     * @param col Descriptinoutn of the Parameter
     * @return The cellEditable value
     */
    public boolean isCellEditable(int row, int col) {
        return false;// col == 2 || col == 3 || col == 8;  // artist, title, year
    }


    /**
     * Sets the valueAt attribute of the MyTableModel object
     *
     * @param value The new valueAt value
     * @param row   The new valueAt value
     * @param col   The new valueAt value
     */
    public void setValueAt(Object value, int row, int col) {
        if (col < columnNames.length) {
            YassSong r = data.elementAt(row);

            switch (col) {
                case 1: {
                    r.setComplete((String) value);
                    break;
                }
                case 2: {
                    r.setArtist((String) value);
                    break;
                }
                case 3: {
                    r.setTitle((String) value);
                    break;
                }
                case 4: {
                    r.setDuetSingerNames((String) value);
                    break;
                }
                case 5: {
                    r.setGenre((String) value);
                    break;
                }
                case 6: {
                    r.setEdition((String) value);
                    break;
                }
                case 7: {
                    r.setLanguage((String) value);
                    break;
                }
                case 8: {
                    r.setYear((String) value);
                    break;
                }
                case 9: {
                    r.setFolder((String) value);
                    break;
                }
                case 10: {
                    r.setAlbum((String) value);
                    break;
                }
                case 11: {
                    r.setLength((String) value);
                    break;
                }
                case 12: {
                    r.setID((String) value);
                    break;
                }
            }
            fireTableCellUpdated(row, col);
        }
    }


    /**
     * Adds a feature to the Row attribute of the MyTableModel object
     *
     * @param dir      The feature to be added to the Row attribute
     * @param folder   The feature to be added to the Row attribute
     * @param filename The feature to be added to the Row attribute
     * @param artist   The feature to be added to the Row attribute
     * @param title    The feature to be added to the Row attribute
     */
    public void addRow(String dir, String folder, String filename, String artist, String title) {
        data.addElement(new YassSong(dir, folder, filename, artist, title));
    }


    /**
     * Adds a feature to the Row attribute of the YassSongListModel object
     *
     * @param s The feature to be added to the Row attribute
     */
    public void addRow(YassSong s) {
        data.addElement(s);
    }


    /**
     * Gets the rowAt attribute of the YassSongListModel object
     *
     * @param i Description of the Parameter
     * @return The rowAt value
     */
    public YassSong getRowAt(int i) {
        if (i >= data.size()) {
            return null;
        }
        return data.elementAt(i);
    }
}

