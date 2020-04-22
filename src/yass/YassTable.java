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

import unicode.UnicodeReader;
import org.mozilla.universalchardet.Constants;
import org.mozilla.universalchardet.UniversalDetector;

import yass.renderer.YassLine;
import yass.renderer.YassNote;
import yass.renderer.YassSession;
import yass.renderer.YassTrack;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.*;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassTable extends JTable {
    /**
     * Description of the Field
     */
    public final static int ZOOM_TIME = 0;
    /**
     * Description of the Field
     */
    public final static int ZOOM_ONE = 1;
    /**
     * Description of the Field
     */
    public static int zoomMode = ZOOM_ONE;
    /**
     * Description of the Field
     */
    public final static int ZOOM_MULTI = 2;
    /**
     * Description of the Field
     */
    public final static int ZOOM_ALL = 3;
    private static final long serialVersionUID = 9021366386206950968L;
    /**
     * Description of the Field
     */
    public int multiSize = 1;
    private YassTableModel tm;
    private YassActions actions = null;
    private YassSheet sheet = null;
    private YassAutoCorrect auto = null;
    private YassProperties prop = null;
    private String mp3 = null, dir = null, txtFilename = null;
    private double bpm = 120, gap = 0, vgap = 0, start = 0, end = -1;
    private int MAX_UNDO = 2048;
    private Vector<YassUndoElement> undos = new Vector<>(
            MAX_UNDO);
    private int undoPos = -1, redoMax = 0;
    private boolean isRelative = false;
    private int multiplayer = 0;
    private Color myColor = null;
    private boolean saved = true;
    private boolean isLoading = false;
    private String encoding = null;
    private boolean showMessages = true;
    private boolean preventUndo = false;
    private boolean preventAutoCheck = false;
    private boolean lyricsChanged = true;
    private Hashtable<String, Boolean> messages = null;
    private int relativePageBreak = 0;
    private boolean preventZoom = false;
    private int goldenPoints = 0;
    private int idealGoldenPoints = 0;
    private int goldenVariance = 0;
    private int durationGolden;
    private int idealGoldenBeats;
    private String goldenDiff;

    private static UniversalDetector detector = null;

    /**
     * Constructor for the YassTable object
     */
    public YassTable() {
        getTableHeader().setReorderingAllowed(false);
        createDefaultColumnsFromModel();
        ToolTipManager.sharedInstance().unregisterComponent(this);
        ToolTipManager.sharedInstance().unregisterComponent(getTableHeader());

        setDragEnabled(true);
        setTransferHandler(new YassTableTransferHandler());

        getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if (sheet != null) {
                            sheet.repaint();
                        }
                    }
                });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                zoomPage();
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                zoomPage();
                updatePlayerPosition();
            }

            public void mouseClicked(MouseEvent e) {
                zoomPage();
                if (e.getClickCount() > 1) {
                    int i = getSelectedRow();
                    if (i < 0) {
                        return;
                    }
                    int col = columnAtPoint(e.getPoint());

                    YassRow r = tm.getRowAt(i);
                    if (r.isGap() || r.isEnd() || r.isComment()) {
                        firePropertyChange("edit", null, "start");
                    }

                    if (r.isNote() && col == 4) {
                        firePropertyChange("edit", null, "start");
                    }

                    if (r.isNote() && col == 0) {
                        if (r.isGolden()) {
                            r.setType("F");
                        } else if (r.isFreeStyle()) {
                            r.setType(":");
                        } else if (r.isRap()) {
                            r.setType("R");
                        } else if (r.isRapGolden()) {
                            r.setType("G");
                        } else {
                            r.setType("*");
                        }
                        tm.fireTableRowsUpdated(i, i);
                    }
                }
            }
        });
        setDefaultRenderer(Object.class, new YassTableRenderer());
        setAutoscrolls(true);

        boolean oldUndo = preventUndo;
        preventUndo = true;
        removeAllRows();
        preventUndo = oldUndo;
    }

    /**
     * Gets the zoomMode attribute of the YassTable class
     *
     * @return The zoomMode value
     */
    public static int getZoomMode() {
        return zoomMode;
    }

    /**
     * Sets the zoomMode attribute of the YassTable class
     *
     * @param i The new zoomMode value
     */
    public static void setZoomMode(int i) {
        zoomMode = i;
    }

    /**
     * Gets the uTF8 attribute of the YassTable object
     *
     * @return The uTF8 value
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the uTF8 attribute of the YassTable object
     *
     * @param s The new encoding value
     */
    public void setEncoding(String s) {
        encoding = s;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean showMessages() {
        return showMessages;
    }

    /**
     * Description of the Method
     *
     * @param onoff Description of the Parameter
     */
    public void showMessages(boolean onoff) {
        showMessages = onoff;
    }

    /**
     * Gets the saved attribute of the YassTable object
     *
     * @return The saved value
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * Sets the saved attribute of the YassTable object
     *
     * @param onoff The new saved value
     */
    public void setSaved(boolean onoff) {
        saved = onoff;
    }

    /**
     * Gets the tableColor attribute of the YassTable object
     *
     * @return The tableColor value
     */
    public Color getTableColor() {
        return myColor;
    }

    /**
     * Sets the tableColor attribute of the YassTable object
     *
     * @param c The new tableColor value
     */
    public void setTableColor(Color c) {
        myColor = c;
    }

    /**
     * Description of the Method
     */
    public void updatePlayerPosition() {
        if (actions != null) {
            actions.updatePlayerPosition();
        }
    }

    /**
     * Gets the preventUndo attribute of the YassTable object
     *
     * @return The preventUndo value
     */
    public boolean getPreventUndo() {
        return preventUndo;
    }

    /**
     * Sets the preventUndo attribute of the YassTable object
     *
     * @param onoff The new preventUndo value
     */
    public void setPreventUndo(boolean onoff) {
        preventUndo = onoff;
    }

    /**
     * Gets the preventAutoCheck attribute of the YassTable object
     *
     * @return The preventAutoCheck value
     */
    public boolean getPreventAutoCheck() {
        return preventAutoCheck;
    }

    /**
     * Sets the preventAutoCheck attribute of the YassTable object
     *
     * @param onoff The new preventAutoCheck value
     */
    public void setPreventAutoCheck(boolean onoff) {
        preventAutoCheck = onoff;
    }

    /**
     * Description of the Method
     *
     * @param onoff Description of the Parameter
     */
    public void preventLyricsUpdate(boolean onoff) {
        lyricsChanged = !onoff;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean lyricsChanged() {
        return lyricsChanged;
    }

    /**
     * Gets the actions attribute of the YassTable object
     *
     * @return The actions value
     */
    public YassActions getActions() {
        return actions;
    }

    /**
     * Sets the actions attribute of the YassTable object
     *
     * @param a The new actions value
     */
    public void setActions(YassActions a) {
        actions = a;
    }

    /**
     * Sets the sheet attribute of the YassTable object
     *
     * @param s The new sheet value
     */
    public void setSheet(YassSheet s) {
        sheet = s;
    }

    /**
     * Description of the Method
     *
     * @param p Description of the Parameter
     */
    public void init(YassProperties p) {
        prop = p;
    }

    /**
     * Gets the properties attribute of the YassTable object
     *
     * @return The properties value
     */
    public YassProperties getProperties() {
        return prop;
    }

    /**
     * Gets the autoCorrect attribute of the YassTable object
     *
     * @return The autoCorrect value
     */
    public YassAutoCorrect getAutoCorrect() {
        return auto;
    }

    /**
     * Sets the autoCorrect attribute of the YassTable object
     *
     * @param a The new autoCorrect value
     */
    public void setAutoCorrect(YassAutoCorrect a) {
        auto = a;
    }

    /**
     * Description of the Method
     */
    public void removeAllRows() {
        if (tm == null) {
            setModel(tm = new YassTableModel());

            // TableColumn col = getColumnModel().getColumn(0);
            // col.setCellEditor(new YassTableCellEditor());

            tm.addTableModelListener(new TableModelListener() {
                public void tableChanged(TableModelEvent e) {
                    int i = e.getFirstRow();
                    int j = e.getLastRow();
                    int t = e.getType();
                    if (i == TableModelEvent.HEADER_ROW) {
                        return;
                    }
                    if (i < 0) {
                        return;
                    }
                    // DEBUG: gets triggered for mouse drags
                    // so we cannot just addUndo() for txt input

                    if (!isLoading) {
                        setSaved(false);
                        if (actions != null) {
                            actions.setSaved(false);
                        }
                    }

                    // don't check comment header
                    // (done only after loading && focusGained)
                    boolean checkAll = t != TableModelEvent.UPDATE;
                    if (actions != null && !preventAutoCheck) {
                        actions.checkData(YassTable.this, checkAll, checkAll);
                    }

                    repaint();
                    if (sheet != null) {
                        int n = getRowCount();
                        if (i >= n || j >= n || t != TableModelEvent.UPDATE) {
                            sheet.init();
                        }
                        // errors may affect unchanged notes
                        // so always update all rows
                        if (t == TableModelEvent.UPDATE) {
                            sheet.updateActiveTable();
                            // zoomPage();
                            // updatePlayerPosition();
                            sheet.repaint();
                            sheet.firePropsChanged();
                        }
                    }
                }
            });
        } else {
            tm.getData().clear();
        }

        resetUndo();
        addUndo();
        mp3 = dir = txtFilename = null;
        gap = 0;
        bpm = 100;
        vgap = 0;
        start = 0;
        end = -1;
        isRelative = false;
        multiplayer = 0;
        encoding = null;
    }

    /**
     * Gets the mP3 attribute of the YassTable object
     *
     * @return The mP3 value
     */
    public String getMP3() {
        return mp3;
    }

    /**
     * Sets the mP3 attribute of the YassTable object
     *
     * @param s The new mP3 value
     */
    public void setMP3(String s) {
        mp3 = s;
        YassRow r = tm.getCommentRow("MP3:");
        if (r == null) {
            return;
        }
        r.setComment(s);
    }

    /**
     * Gets the dir attribute of the YassTable object
     *
     * @return The dir value
     */
    public String getDir() {
        return dir;
    }

    /**
     * Sets the dir attribute of the YassTable object
     *
     * @param d The new dir value
     */
    public void setDir(String d) {
        dir = d;
    }

    /**
     * Gets the filename attribute of the YassTable object
     *
     * @return The filename value
     */
    public String getFilename() {
        return txtFilename;
    }

    /**
     * Sets the filename attribute of the YassTable object
     *
     * @param s The new filename value
     */
    public void setFilename(String s) {
        txtFilename = s;
    }

    /**
     * Gets the gap attribute of the YassTable object
     *
     * @return The gap value
     */
    public double getGap() {
        return gap;
    }

    /**
     * Sets the gap attribute of the YassTable object
     *
     * @param g The new gap value
     */
    public void setGap(double g) {
        gap = g;
        String s = new Integer((int) gap).toString();
        YassRow r = tm.getCommentRow("GAP:");
        if (r == null) {
            r = new YassRow("#", "GAP:", s, "", "");
            YassRow v = tm.getCommentRow("BPM:");
            int i = v != null ? tm.getData().indexOf(v) : 0;
            tm.getData().insertElementAt(r, i + 1);

            addUndo();
            tm.fireTableDataChanged();
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
            }
        }
    }

    /**
     * Adds a feature to the Gap attribute of the YassTable object
     *
     * @param g The feature to be added to the Gap attribute
     */
    public void addGap(double g) {
        setGap(getGap() + g);
        if (sheet != null) {
            sheet.update();
            sheet.repaint();
        }
    }

    /**
     * Gets the bPM attribute of the YassTable object
     *
     * @return The bPM value
     */
    public double getBPM() {
        return bpm;
    }

    /**
     * Sets the BPM attribute of the YassTable object
     *
     * @param b The new bPM value
     */
    public void setBPM(double b) {
        bpm = b;

        String s = new Double(bpm).toString();
        s = s.replace('.', ',');
        if (s.endsWith(",00")) {
            s = s.substring(0, s.length() - 3);
        }
        if (s.endsWith(",0")) {
            s = s.substring(0, s.length() - 2);
        }
        YassRow r = tm.getCommentRow("BPM:");
        if (r == null) {
            r = new YassRow("#", "BPM:", s, "", "");
            YassRow v = tm.getCommentRow("GAP:");
            int i = v != null ? tm.getData().indexOf(v) : 0;
            tm.getData().insertElementAt(r, i);

            addUndo();
            tm.fireTableDataChanged();
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
            }
        }

        repaint();
    }

    /**
     * Sets the bPM attribute of the YassTable object
     *
     * @param s The new bPM value
     */
    public void setBPM(String s) {
        if (s == null || s.length() < 1) {
            return;
        }
        s.replace(',', '.');
        double d = Double.parseDouble(s);
        if (d > 0) {
            setBPM(d);
        }
    }

    /**
     * Gets the start attribute of the YassTable object
     *
     * @return The start value
     */
    public double getStart() {
        return start;
    }

    /**
     * Sets the start attribute of the YassTable object
     *
     * @param b The new start value
     */
    public void setStart(double b) {
        start = b;

        String s = new Double(start).toString();
        YassRow r = tm.getCommentRow("START:");
        if (r == null && start > 0) {
            r = new YassRow("#", "START:", s, "", "");
            YassRow v = tm.getCommentRow("GAP:");
            int i = v != null ? tm.getData().indexOf(v) + 1 : 0;
            tm.getData().insertElementAt(r, i);

            addUndo();
            tm.fireTableDataChanged();
        } else if (start <= 0) {
            if (r != null) {
                tm.getData().removeElement(r);
                addUndo();
                tm.fireTableDataChanged();
            }
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
            }
        }
    }

    /**
     * Sets the start attribute of the YassTable object
     *
     * @param s The new start value
     */
    public void setStart(String s) {
        if (s == null || s.length() < 1) {
            return;
        }
        s.replace(',', '.');
        double d = Double.parseDouble(s);
        if (d > 0) {
            setStart(d);
        }
    }

    /**
     * Gets the end attribute of the YassTable object
     *
     * @return The end value
     */
    public double getEnd() {
        return end;
    }

    /**
     * Sets the end attribute of the YassTable object
     *
     * @param b The new end value
     */
    public void setEnd(double b) {
        end = b;

        String s = new Double(end).toString();
        YassRow r = tm.getCommentRow("END:");
        if (r == null && end >= 0) {
            r = new YassRow("#", "END:", s, "", "");
            YassRow v = tm.getCommentRow("GAP:");
            int i = v != null ? tm.getData().indexOf(v) + 1 : 0;
            tm.getData().insertElementAt(r, i);

            addUndo();
            tm.fireTableDataChanged();
        } else if (end < 0) {
            if (r != null) {
                tm.getData().removeElement(r);
                addUndo();
                tm.fireTableDataChanged();
            }
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
            }
        }
    }

    /**
     * Sets the end attribute of the YassTable object
     *
     * @param s The new end value
     */
    public void setEnd(String s) {
        if (s == null || s.length() < 1) {
            return;
        }
        s.replace(',', '.');
        double d = Double.parseDouble(s);
        if (d > 0) {
            setEnd(d);
        }
    }

    /**
     * Gets the videoGap attribute of the YassTable object
     *
     * @return The videoGap value
     */
    public double getVideoGap() {
        return vgap;
    }

    /**
     * Sets the videoGap attribute of the YassTable object
     *
     * @param g The new videoGap value
     */
    public void setVideoGap(double g) {
        vgap = g;
        String s = new Double(vgap).toString().replace('.', ',');
        if (s.endsWith(",0")) {
            s = s.substring(0, s.length() - 2);
        }
        YassRow r = tm.getCommentRow("VIDEOGAP:");
        if (r == null) {
            r = new YassRow("#", "VIDEOGAP:", s, "", "");
            YassRow v = tm.getCommentRow("VIDEO:");
            int i = v != null ? tm.getData().indexOf(v) : 0;
            tm.getData().insertElementAt(r, i + 1);

            addUndo();
            tm.fireTableDataChanged();
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
            }
        }
    }

    /**
     * Sets the videoGap attribute of the YassTable object
     *
     * @param vg The new videoGap value
     */
    public void setVideoGap(String vg) {
        vg = vg.replace(',', '.');
        double vgap = Double.parseDouble(vg);
        setVideoGap(vgap);
    }

    /**
     * Gets the previewStart attribute of the YassTable object
     *
     * @return The previewStart value
     */
    public double getPreviewStart() {
        YassRow r = tm.getCommentRow("PREVIEWSTART:");
        if (r == null) {
            return -1;
        }

        String p = r.getComment();
        p = p.replace(',', '.');
        return Double.parseDouble(p);
    }

    /**
     * Sets the videoGap attribute of the YassTable object
     *
     * @param g The new videoGap value
     */
    public void setPreviewStart(double g) {
        String s = new Double(g).toString().replace('.', ',');
        if (s.endsWith(",0")) {
            s = s.substring(0, s.length() - 2);
        }
        YassRow r = tm.getCommentRow("PREVIEWSTART:");
        if (r == null) {
            r = new YassRow("#", "PREVIEWSTART:", s, "", "");
            YassRow v = tm.getCommentRow("GAP:");
            int i = v != null ? tm.getData().indexOf(v) : 0;
            tm.getData().insertElementAt(r, i + 1);

            addUndo();
            tm.fireTableDataChanged();
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
            }
        }
    }

    /**
     * Sets the previewStart attribute of the YassTable object
     *
     * @param p The new previewStart value
     */
    public void setPreviewStart(String p) {
        p = p.replace(',', '.');
        double pp = Double.parseDouble(p);
        setPreviewStart(pp);
    }

    /**
     * Gets the medleyStartBeat attribute of the YassTable object
     *
     * @return The medleyStartBeat value
     */
    public int getMedleyStartBeat() {
        YassRow r = tm.getCommentRow("MEDLEYSTARTBEAT:");
        if (r == null) {
            return -1;
        }

        String p = r.getComment();
        return Integer.parseInt(p);
    }

    /**
     * Sets the medleyStartBeat attribute of the YassTable object
     *
     * @param g The new medleyStartBeat value
     */
    public void setMedleyStartBeat(int g) {
        String s = g + "";
        YassRow r = tm.getCommentRow("MEDLEYSTARTBEAT:");
        if (r == null) {
            r = new YassRow("#", "MEDLEYSTARTBEAT:", s, "", "");
            YassRow v = tm.getCommentRow("PREVIEWSTART:");
            if (v == null) {
                v = tm.getCommentRow("GAP:");
            }
            int i = v != null ? tm.getData().indexOf(v) : 0;
            tm.getData().insertElementAt(r, i + 1);

            addUndo();
            tm.fireTableDataChanged();
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
            }
        }
    }

    /**
     * Gets the medleyEndBeat attribute of the YassTable object
     *
     * @return The medleyEndBeat value
     */
    public int getMedleyEndBeat() {
        YassRow r = tm.getCommentRow("MEDLEYENDBEAT:");
        if (r == null) {
            return -1;
        }

        String p = r.getComment();
        return Integer.parseInt(p);
    }

    /**
     * Sets the medleyEndBeat attribute of the YassTable object
     *
     * @param g The new medleyEndBeat value
     */
    public void setMedleyEndBeat(int g) {
        String s = g + "";
        YassRow r = tm.getCommentRow("MEDLEYENDBEAT:");
        if (r == null) {
            r = new YassRow("#", "MEDLEYENDBEAT:", s, "", "");
            YassRow v = tm.getCommentRow("MEDLEYSTARTBEAT:");
            if (v == null) {
                v = tm.getCommentRow("PREVIEWSTART:");
            }
            if (v == null) {
                v = tm.getCommentRow("GAP:");
            }
            int i = v != null ? tm.getData().indexOf(v) : 0;
            tm.getData().insertElementAt(r, i + 1);

            addUndo();
            tm.fireTableDataChanged();
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
            }
        }
    }

    /**
     * Sets the medleyStartBeat attribute of the YassTable object
     *
     * @param p The new medleyStartBeat value
     */
    public void setMedleyStartBeat(String p) {
        int pp = Integer.parseInt(p);
        setMedleyStartBeat(pp);
    }

    /**
     * Sets the medleyEndBeat attribute of the YassTable object
     *
     * @param p The new medleyEndBeat value
     */
    public void setMedleyEndBeat(String p) {
        int pp = Integer.parseInt(p);
        setMedleyEndBeat(pp);
    }

    /**
     * Gets the relative attribute of the YassTable object
     *
     * @return The relative value
     */
    public boolean isRelative() {
        YassRow r = tm.getCommentRow("RELATIVE:");
        if (r == null) {
            return false;
        }
        String s = r.getComment();
        if (s == null) {
            return false;
        }
        s = s.toLowerCase();
        return s.equals("yes") || s.equals("true");
    }

    /**
     * Gets the multiplayer attribute of the YassTable object
     *
     * @return The multiplayer value
     */
    public int getMultiplayer() {
        return multiplayer;
    }

    /**
     * Gets the artist attribute of the YassTable object
     *
     * @return The artist value
     */
    public String getArtist() {
        YassRow r = tm.getCommentRow("ARTIST:");
        if (r == null) {
            return null;
        }
        return r.getComment();
    }

    /**
     * Gets the title attribute of the YassTable object
     *
     * @return The title value
     */
    public String getTitle() {
        YassRow r = tm.getCommentRow("TITLE:");
        if (r == null) {
            return null;
        }
        return r.getComment();
    }

    /**
     * Sets the title attribute of the YassTable object
     *
     * @param s The new title value
     * @return Description of the Return Value
     */
    public boolean setTitle(String s) {
        YassRow r = tm.getCommentRow("TITLE:");
        if (r == null) {
            return false;
        }
        String old = r.getComment();
        if (!s.equals(old)) {
            r.setComment(s);
            addUndo();
            tm.fireTableDataChanged();
            return true;
        }
        return false;
    }

    /**
     * Sets the artist attribute of the YassTable object
     *
     * @param s The new artist value
     * @return Description of the Return Value
     */
    public boolean setArtist(String s) {
        YassRow r = tm.getCommentRow("ARTIST:");
        if (r == null) {
            return false;
        }
        String old = r.getComment();
        if (!s.equals(old)) {
            r.setComment(s);
            addUndo();
            tm.fireTableDataChanged();
            return true;
        }
        return false;
    }

    /**
     * Gets the version attribute of the YassTable object
     *
     * @return The version value
     */
    public String getVersion() {
        YassRow r = tm.getCommentRow("TITLE:");
        if (r == null) {
            return null;
        }
        return r.getVersion();
    }

    /**
     * Sets the version attribute of the YassTable object
     *
     * @param s The new version value
     * @return Description of the Return Value
     */
    public boolean setVersion(String s) {
        YassRow r = tm.getCommentRow("TITLE:");
        if (r == null) {
            return false;
        }
        String old = r.getComment();
        if (!s.equals(old)) {
            r.setVersion(s);
            addUndo();
            tm.fireTableDataChanged();
            return true;
        }
        return false;
    }

    /**
     * Gets the genre attribute of the YassTable object
     *
     * @return The genre value
     */
    public String getGenre() {
        YassRow r = tm.getCommentRow("GENRE:");
        if (r == null) {
            return null;
        }
        return r.getComment();
    }

    /**
     * Gets the edition attribute of the YassTable object
     *
     * @return The edition value
     */
    public String getEdition() {
        YassRow r = tm.getCommentRow("EDITION:");
        if (r == null) {
            return null;
        }
        return r.getComment();
    }

    /**
     * Gets the album attribute of the YassTable object
     *
     * @return The album value
     */
    public String getAlbum() {
        YassRow r = tm.getCommentRow("ALBUM:");
        if (r == null) {
            return null;
        }
        return r.getComment();
    }

    /**
     * Gets the iD attribute of the YassTable object
     *
     * @return The iD value
     */
    public String getID() {
        YassRow r = tm.getCommentRow("ID:");
        if (r == null) {
            return null;
        }
        return r.getComment();
    }

    /**
     * Gets the length attribute of the YassTable object
     *
     * @return The length value
     */
    public String getLength() {
        YassRow r = tm.getCommentRow("LENGTH:");
        if (r == null) {
            return null;
        }
        return r.getComment();
    }

    /**
     * Gets the cover attribute of the YassTable object
     *
     * @return The cover value
     */
    public String getCover() {
        YassRow r = tm.getCommentRow("COVER:");
        if (r == null) {
            return null;
        }
        return r.getComment();
    }

    /**
     * Gets the background attribute of the YassTable object
     *
     * @return The background value
     */
    public String getBackgroundTag() {
        YassRow r = tm.getCommentRow("BACKGROUND:");
        if (r == null) {
            return null;
        }
        return r.getComment();
    }

    /**
     * Gets the video attribute of the YassTable object
     *
     * @return The video value
     */
    public String getVideo() {
        YassRow r = tm.getCommentRow("VIDEO:");
        if (r == null) {
            return null;
        }
        return r.getComment();
    }

    /**
     * Gets the language attribute of the YassTable object
     *
     * @return The language value
     */
    public String getLanguage() {
        YassRow r = tm.getCommentRow("LANGUAGE:");
        if (r == null) {
            return null;
        }
        return r.getComment();
    }

    /**
     * Gets the year attribute of the YassTable object
     *
     * @return The year value
     */
    public String getYear() {
        YassRow r = tm.getCommentRow("YEAR:");
        if (r == null) {
            return null;
        }
        return r.getComment();
    }

    /**
     * Sets the genre attribute of the YassTable object
     *
     * @param s The new genre value
     * @return Description of the Return Value
     */
    public boolean setGenre(String s) {
        YassRow r = tm.getCommentRow("GENRE:");
        if (r == null) {
            r = new YassRow("#", "GENRE:", s, "", "");
            YassRow v = tm.getCommentRow("EDITION:");
            if (v == null) {
                v = tm.getCommentRow("LANGUAGE:");
            }
            if (v == null) {
                v = tm.getCommentRow("ARTIST:");
            }
            int i = v != null ? tm.getData().indexOf(v) + 1 : 0;
            tm.getData().insertElementAt(r, i);

            addUndo();
            tm.fireTableDataChanged();
            return true;
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the edition attribute of the YassTable object
     *
     * @param s The new edition value
     * @return Description of the Return Value
     */
    public boolean setEdition(String s) {
        YassRow r = tm.getCommentRow("EDITION:");
        if (r == null) {
            r = new YassRow("#", "EDITION:", s, "", "");
            YassRow v = tm.getCommentRow("LANGUAGE:");
            if (v == null) {
                v = tm.getCommentRow("ARTIST:");
            }
            int i = v != null ? tm.getData().indexOf(v) + 1 : 0;
            tm.getData().insertElementAt(r, i);

            addUndo();
            tm.fireTableDataChanged();
            return true;
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the language attribute of the YassTable object
     *
     * @param s The new language value
     * @return Description of the Return Value
     */
    public boolean setLanguage(String s) {
        YassRow r = tm.getCommentRow("LANGUAGE:");
        if (r == null) {
            r = new YassRow("#", "LANGUAGE:", s, "", "");
            YassRow v = tm.getCommentRow("ARTIST:");
            int i = v != null ? tm.getData().indexOf(v) + 1 : 0;
            tm.getData().insertElementAt(r, i);

            addUndo();
            tm.fireTableDataChanged();
            return true;
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the year attribute of the YassTable object
     *
     * @param s The new year value
     * @return Description of the Return Value
     */
    public boolean setYear(String s) {
        if (s != null && (s.trim().equals("0") || s.trim().length() < 1)) {
            s = null;
        }

        YassRow r = tm.getCommentRow("YEAR:");
        if (r == null && s != null) {
            r = new YassRow("#", "YEAR:", s, "", "");
            YassRow v = tm.getCommentRow("LANGUAGE:");
            if (v == null) {
                v = tm.getCommentRow("GENRE:");
            }
            if (v == null) {
                v = tm.getCommentRow("EDITION:");
            }
            if (v == null) {
                v = tm.getCommentRow("ARTIST:");
            }
            int i = v != null ? tm.getData().indexOf(v) + 1 : 0;
            tm.getData().insertElementAt(r, i);

            addUndo();
            tm.fireTableDataChanged();
            return true;
        } else if (s == null) {
            if (r != null) {
                tm.getData().removeElement(r);
                addUndo();
                tm.fireTableDataChanged();
            }
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the album attribute of the YassTable object
     *
     * @param s The new album value
     * @return Description of the Return Value
     */
    public boolean setAlbum(String s) {
        YassRow r = tm.getCommentRow("ALBUM:");
        if (r == null) {
            r = new YassRow("#", "ALBUM:", s, "", "");
            YassRow v = tm.getCommentRow("GENRE:");
            if (v == null) {
                v = tm.getCommentRow("ARTIST:");
            }
            int i = v != null ? tm.getData().indexOf(v) + 1 : 0;
            tm.getData().insertElementAt(r, i);

            addUndo();
            tm.fireTableDataChanged();
            return true;
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the iD attribute of the YassTable object
     *
     * @param s The new iD value
     * @return Description of the Return Value
     */
    public boolean setID(String s) {
        YassRow r = tm.getCommentRow("ID:");
        if (r == null) {
            r = new YassRow("#", "ID:", s, "", "");
            YassRow v = tm.getCommentRow("YEAR:");
            if (v == null) {
                v = tm.getCommentRow("ARTIST:");
            }
            int i = v != null ? tm.getData().indexOf(v) + 1 : 0;
            tm.getData().insertElementAt(r, i);

            addUndo();
            tm.fireTableDataChanged();
            return true;
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the length attribute of the YassTable object
     *
     * @param s The new length value
     * @return Description of the Return Value
     */
    public boolean setLength(String s) {
        YassRow r = tm.getCommentRow("LENGTH:");
        if (r == null) {
            r = new YassRow("#", "LENGTH:", s, "", "");
            YassRow v = tm.getCommentRow("GAP:");
            if (v == null) {
                v = tm.getCommentRow("MP3:");
            }
            int i = v != null ? tm.getData().indexOf(v) + 1 : 0;
            tm.getData().insertElementAt(r, i);

            addUndo();
            tm.fireTableDataChanged();
            return true;
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the cover attribute of the YassTable object
     *
     * @param s The new cover value
     * @return Description of the Return Value
     */
    public boolean setCover(String s) {
        YassRow r = tm.getCommentRow("COVER:");
        if (r == null) {
            r = new YassRow("#", "COVER:", s, "", "");
            YassRow v = tm.getCommentRow("MP3:");
            if (v == null) {
                v = tm.getCommentRow("TITLE:");
            }
            int i = v != null ? tm.getData().indexOf(v) + 1 : 0;
            tm.getData().insertElementAt(r, i);

            addUndo();
            tm.fireTableDataChanged();
            return true;
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the background attribute of the YassTable object
     *
     * @param s The new background value
     * @return Description of the Return Value
     */
    public boolean setBackground(String s) {
        YassRow r = tm.getCommentRow("BACKGROUND:");
        if (r == null) {
            r = new YassRow("#", "BACKGROUND:", s, "", "");
            YassRow v = tm.getCommentRow("COVER:");
            if (v == null) {
                v = tm.getCommentRow("MP3:");
            }
            if (v == null) {
                v = tm.getCommentRow("TITLE:");
            }
            int i = v != null ? tm.getData().indexOf(v) + 1 : 0;
            tm.getData().insertElementAt(r, i);

            addUndo();
            tm.fireTableDataChanged();
            return true;
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the video attribute of the YassTable object
     *
     * @param s The new video value
     * @return Description of the Return Value
     */
    public boolean setVideo(String s) {
        YassRow r = tm.getCommentRow("VIDEO:");
        if (r == null) {
            r = new YassRow("#", "VIDEO:", s, "", "");
            YassRow v = tm.getCommentRow("BACKGROUND:");
            if (v == null) {
                v = tm.getCommentRow("COVER:");
            }
            if (v == null) {
                v = tm.getCommentRow("MP3:");
            }
            if (v == null) {
                v = tm.getCommentRow("TITLE:");
            }
            int i = v != null ? tm.getData().indexOf(v) + 1 : 0;
            tm.getData().insertElementAt(r, i);

            addUndo();
            tm.fireTableDataChanged();
            return true;
        } else {
            String old = r.getComment();
            if (!s.equals(old)) {
                r.setComment(s);
                addUndo();
                int k = tm.getData().indexOf(r);
                tm.fireTableRowsUpdated(k, k);
                return true;
            }
        }
        return false;
    }

    /**
     * Description of the Method
     *
     * @param key Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean hasMessage(String key) {
        if (messages == null) {
            return false;
        }
        return messages.contains(key);
    }

    /**
     * Sets the hasMessage attribute of the YassTable object
     */
    public void resetMessages() {
        if (messages == null) {
            messages = new Hashtable<>();
        }
        messages.clear();
    }

    /**
     * Sets the message attribute of the YassTable object
     *
     * @param key The feature to be added to the Message attribute
     */
    public void addMessage(String key) {
        if (messages == null) {
            messages = new Hashtable<>();
        }
        messages.put(key, new Boolean(true));
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean hasMinorPageBreakMessages() {
        if (messages == null || auto == null) {
            return false;
        }
        Enumeration<String> en = messages.keys();
        while (en.hasMoreElements()) {
            String key = en.nextElement();
            if (YassAutoCorrect.isAutoCorrectionMinorPageBreak(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean hasPageBreakMessages() {
        if (messages == null || auto == null) {
            return false;
        }
        Enumeration<String> en = messages.keys();
        while (en.hasMoreElements()) {
            String key = en.nextElement();
            if (YassAutoCorrect.isAutoCorrectionPageBreak(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean hasTransposedMessages() {
        if (messages == null || auto == null) {
            return false;
        }
        Enumeration<String> en = messages.keys();
        while (en.hasMoreElements()) {
            String key = en.nextElement();
            if (key.equals(YassRow.TRANSPOSED_NOTES)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean hasFileNameMessages() {
        if (messages == null || auto == null) {
            return false;
        }
        Enumeration<String> en = messages.keys();
        while (en.hasMoreElements()) {
            String key = en.nextElement();
            if (YassAutoCorrect.isAutoCorrectionFileName(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean hasSpacingMessages() {
        if (messages == null || auto == null) {
            return false;
        }
        Enumeration<String> en = messages.keys();
        while (en.hasMoreElements()) {
            String key = en.nextElement();
            if (YassAutoCorrect.isAutoCorrectionSpacing(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean hasTagsMessages() {
        if (messages == null || auto == null) {
            return false;
        }
        Enumeration<String> en = messages.keys();
        while (en.hasMoreElements()) {
            String key = en.nextElement();
            if (YassAutoCorrect.isAutoCorrectionTags(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the commentRow attribute of the YassTable object
     *
     * @param s Description of the Parameter
     * @return The commentRow value
     */
    public YassRow getCommentRow(String s) {
        return tm.getCommentRow(s);
    }

    /**
     * Description of the Method
     *
     * @param filename Description of the Parameter
     * @return Description of the Return Value
     */
    public synchronized boolean loadFile(String filename) {
        // System.out.println("Loading " + filename);

        File f = new File(filename);
        if (!f.exists()) {
            return false;
        }
        if (f.length() > 1024 * 1024) {
            return false;
        }

        dir = f.getAbsolutePath();
        int isep = dir.lastIndexOf(File.separator);
        if (isep <= 0) {
            return false;
        }
        txtFilename = dir.substring(isep + 1);
        dir = dir.substring(0, isep);

        // saruta, Jan 2019: better UTF-8 detection method
        // String detectedEncoding = detectUTF8(new File(filename)) ? "UTF-8" : null;
        String detectedEncoding = detectEncoding(new File(filename));

        isRelative = false;
        // saruta, Jan 2019: better UTF-8 detection method
        // encoding = null;
        encoding = detectedEncoding == Constants.CHARSET_UTF_8 ? "UTF-8" : null;
        relativePageBreak = 0;
        UnicodeReader r = null;
        BufferedReader inputStream = null;
        FileInputStream fis = null;
        boolean success = false;
        try {
            r = new UnicodeReader(fis = new FileInputStream(filename),
                    detectedEncoding);
            inputStream = new BufferedReader(r);

            // BufferedReader inputStream = new BufferedReader(new
            // FileReader(filename));
            String l;
            while ((l = inputStream.readLine()) != null) {
                if (!addRow(l)) {
                    throw new IOException("Invalid data");
                }
            }
            // saruta, Jan 2019: better UTF-8 detection method
            // encoding = r.getEncoding();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
            if (r != null) {
                try {
                    r.close();
                } catch (Exception e) {
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                }
            }
        }

        if (tm.getCommentRow("TITLE:") == null) {
            // System.out.println("ERROR: No title tag in - "+filename);
            success = false;
        }
        if (tm.getCommentRow("ARTIST:") == null) {
            // System.out.println("ERROR: No artist tag - "+filename);
            success = false;
        }

        isLoading = true;
        tm.fireTableDataChanged();
        isLoading = false;

        return success;
    }

    private boolean detectNonAscii(String filename) {
        File file = new File(filename);
        DataInputStream dis = null;
        try {
            byte[] fileData = new byte[(int) file.length()];
            dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(fileData);
            dis.close();
            for (byte aFileData : fileData) {
                // System.out.print((char) fileData[i]);
                if (aFileData > 127 || aFileData < 0) {
                    // System.out.print((int)fileData[i] + "  " + (char)
                    // fileData[i] + "  ");
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (dis != null)
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return false;
    }

    public boolean detectLatin1(File file) {
        Charset charset = Charset.forName("ISO-8859-1");
        CharsetDecoder decoder = charset.newDecoder();
        decoder.reset();

        BufferedInputStream input = null;
        try {
            input = new BufferedInputStream(new FileInputStream(file));

            byte[] buffer = new byte[512];
            while (input.read(buffer) != -1) {
                try {
                    decoder.decode(ByteBuffer.wrap(buffer));
                } catch (CharacterCodingException e) {
                    return false;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return true;
    }

    // saruta, Jan 2019: deprecated; replaced with detectedEncoding
    /*public boolean detectUTF8(File file) {

        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        decoder.reset();

        BufferedInputStream input = null;
        try {
            input = new BufferedInputStream(new FileInputStream(file));

            byte[] buffer = new byte[512];
            while (input.read(buffer) != -1) {
                try {
                    decoder.decode(ByteBuffer.wrap(buffer));
                } catch (CharacterCodingException e) {
                    return false;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return true;
    }
    */
    public String detectEncoding(File file) {
        String enc = null;
        if (detector == null) detector = new UniversalDetector(null);

        BufferedInputStream input = null;
        try {
            input = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[512];
            int nRead;
            while ((nRead = input.read(buffer)) > 0 && !detector.isDone()) {
                detector.handleData(buffer, 0, nRead);
            }
            detector.dataEnd();
            enc = detector.getDetectedCharset();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        detector.reset(); // reuse
        return enc;
    }

    /**
     * Sets the text attribute of the YassTable object
     *
     * @param s The new text value
     * @return Description of the Return Value
     */
    public synchronized boolean setText(String s) {
        if (s == null) {
            return false;
        }
        if (s.length() > 1024 * 1024) {
            return false;
        }

        isRelative = false;
        encoding = null;
        relativePageBreak = 0;
        try {
            BufferedReader inputStream = new BufferedReader(new StringReader(s));
            String l;
            while ((l = inputStream.readLine()) != null) {
                if (!addRow(l)) {
                    throw new IOException("Invalid data");
                }
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (tm.getCommentRow("TITLE:") == null) {
            // System.out.println("ERROR: No title tag in - "+filename);
            return false;
        }
        if (tm.getCommentRow("ARTIST:") == null) {
            // System.out.println("ERROR: No artist tag - "+filename);
            return false;
        }

        tm.fireTableDataChanged();
        if (actions != null) {
            actions.checkData(this, true, true);
        }
        return true;
    }

    /**
     * Description of the Method
     *
     * @param filename Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean storeFile(String filename) {
        // System.out.println("Storing "+tm.getCommentRow("ARTIST:").getComment()+" - "+tm.getCommentRow("TITLE:").getComment());

        int relPageBreak = 0;
        boolean isRel = isRelative();

        BufferedWriter bw = null;
        OutputStreamWriter osw = null;
        FileOutputStream fos = null;
        PrintWriter outputStream = null;

        if (prop == null) {
            StringWriter errors = new StringWriter();
            new Exception().printStackTrace(new PrintWriter(errors));
            String msg = "Cannot store file: properties not initialized. Please send me the following stacktrace:\n"
                    + errors.toString();
            System.out.println(msg);
            JTextArea text = new JTextArea(msg.toString());
            text.setOpaque(false);
            JOptionPane.showMessageDialog(null, text);
            return false;
        }
        String p = prop.getProperty("utf8-without-bom");
        boolean utf8WithoutBom = p != null && p.equals("true");

        p = prop.getProperty("utf8-always");
        boolean utf8Always = p != null && p.equals("true");

        boolean success = false;
        try {
            File f = new File(filename);
            if (f.exists()) {
                // todo: backup
                f.delete();
            }

            // saruta, Jan 2019: utf8-->UTF-8
            //if ((encoding != null && encoding.equals("UTF8")) || utf8Always) {
            if ((encoding != null && encoding.equals("UTF-8")) || utf8Always) {
                fos = new FileOutputStream(f, false);
                if (f.length() < 1 && !utf8WithoutBom) {
                    final byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB,
                            (byte) 0xBF};
                    fos.write(bom);
                }
                osw = new OutputStreamWriter(fos, "UTF-8");
                bw = new BufferedWriter(osw);
                outputStream = new PrintWriter(bw);
                // encoding = "UTF8";
                encoding = "UTF-8";
            } else {
                outputStream = new PrintWriter(new FileWriter(filename));
            }

            int rows = tm.getRowCount();
            String s;
            for (int i = 0; i < rows; i++) {
                YassRow r = tm.getRowAt(i);
                if (isRel) {
                    int revert = -1;
                    if (r.isPageBreak() && i + 1 < rows) {
                        // set 2nd beat to next note
                        YassRow r2 = tm.getRowAt(i + 1);
                        if (r2.isNote()) {
                            revert = r.getSecondBeatInt();
                            r.setSecondBeat(r2.getBeatInt());
                        }
                    }
                    if (r.isNote() || r.isPageBreak()) {
                        s = r.toString(relPageBreak);
                        if (r.isPageBreak()) {
                            relPageBreak = r.getSecondBeatInt();
                        }
                    } else {
                        s = r.toString();
                    }
                    if (revert >= 0) {
                        r.setSecondBeat(revert);
                    }
                } else {
                    s = r.toString();
                }

                s = s.replace(YassRow.SPACE, ' ');
                outputStream.println(s);
                // System.out.println(tm.getRowAt(i).toString());
            }
            success = true;
        } catch (Exception e) {
            if (sheet != null) {
                sheet.setMessage(I18.get("sheet_msg_write_error"));
            }
            System.out.println("Write Error: " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (bw != null) {
                    bw.close();
                }
                if (osw != null) {
                    osw.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        YassTable verify = new YassTable();
        verify.loadFile(filename);
        if (!equalsData(verify)) {
            System.out
                    .println("###############################################");
            System.out
                    .println("Write Error: Written data could not be verfied.");
            System.out.println("File: " + filename);
            System.out.println("Encoding: " + encoding);
            System.out
                    .println("###############################################");
            JOptionPane.showMessageDialog(null,
                    "<html>Write Error: Written data could not be verfied.<br>File: "
                            + filename, "Error", JOptionPane.ERROR_MESSAGE);
        }

        return success;
    }

    /**
     * Description of the Method
     */
    public void resetUndo() {
        undos.removeAllElements();
        undoPos = -1;
        redoMax = 0;
        if (actions != null) {
            actions.getUndoAction().setEnabled(false);
        }
        if (actions != null) {
            actions.getRedoAction().setEnabled(false);
        }
    }

    /**
     * Adds a feature to the Undo attribute of the YassTable object
     *
     * @return Description of the Return Value
     */
    public YassUndoElement addUndo() {
        if (preventUndo) {
            return null;
        }

        if (undoPos == MAX_UNDO - 1) {
            undos.remove(0);
        } else {
            undoPos++;
        }

        Vector<YassRow> c = (Vector<YassRow>) tm.getData().clone();
        int n = c.size();
        for (int i = 0; i < n; i++) {
            c.setElementAt(c.elementAt(i).clone(), i);
        }

        n = undos.size();
        while (n > undoPos) {
            undos.remove(n - 1);
            n--;
        }
        YassUndoElement ue = null;
        if (sheet != null) {
            ue = new YassUndoElement(c, getSelectedRows(),
                    sheet.getViewPosition(), sheet.getBeatSize());
        } else {
            ue = new YassUndoElement(c, getSelectedRows(), new Point(), 0);
        }
        undos.addElement(ue);
        // System.out.println("add " + ue.sheetViewPosition);

        redoMax = 0;
        if (actions != null && undoPos > 0) {
            actions.getUndoAction().setEnabled(true);
        }
        if (actions != null) {
            actions.getRedoAction().setEnabled(false);
        }
        return ue;
    }

    /**
     * Description of the Method
     */
    public void updateUndo() {
        if (preventUndo) {
            return;
        }

        YassUndoElement ue = lastUndo();
        if (ue == null) {
            return;
        }

        if (sheet != null) {
            ue.set(getSelectedRows(), sheet.getViewPosition(),
                    sheet.getBeatSize());
        } else {
            ue.set(getSelectedRows(), new Point(), 0);
        }
    }

    /**
     * Description of the Method
     */
    public void removeLastUndo() {
        if (undoPos < 1) {
            return;
        }
        undos.remove(undoPos - 1);
        undoPos--;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public YassUndoElement currentUndo() {
        return undos.elementAt(undoPos);
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public YassUndoElement lastUndo() {
        if (undoPos < 1) {
            return null;
        }
        return undos.elementAt(undoPos - 1);
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public YassUndoElement nextUndo() {
        int n = undos.size();
        if (undoPos + 1 >= n) {
            return null;
        }
        return undos.elementAt(undoPos + 1);
    }

    /**
     * Description of the Method
     */
    public void redoRows() {
        if (redoMax < 1) {
            return;
        }
        redoMax--;
        undoPos++;

        YassUndoElement undoElem = undos.elementAt(undoPos);

        Vector<YassRow> c = (Vector<YassRow>) undoElem.data.clone();
        int n = c.size();
        for (int i = 0; i < n; i++) {
            c.setElementAt(c.elementAt(i).clone(), i);
        }

        boolean oldUndo = preventUndo;
        preventUndo = true;
        tm.setData(c);
        tm.fireTableDataChanged();
        preventUndo = oldUndo;

        ListSelectionModel sel = getSelectionModel();
        sel.setValueIsAdjusting(true);
        sel.clearSelection();
        Rectangle rr = new Rectangle();
        for (int i = 0; i < undoElem.selectedRows.length; i++) {
            int k = undoElem.selectedRows[i];
            rr.add(getCellRect(k, 0, true));
            sel.addSelectionInterval(k, k);
        }
        sel.setValueIsAdjusting(false);
        // scrollRectToVisible(rr);
        // zoomPage();

        if (sheet != null) {
            sheet.init();
            sheet.update();
            sheet.setBeatSize(undoElem.sheetBeatSize);
            sheet.setViewPosition(undoElem.sheetViewPosition);
            sheet.repaint();
        }
        // System.out.println("redo " + undoElem.sheetViewPosition);

        if (actions != null && redoMax == 0) {
            actions.getRedoAction().setEnabled(false);
        }
        actions.getUndoAction().setEnabled(true);
    }

    /**
     * Description of the Method
     */
    public void undoRows() {
        if (undoPos <= 0) {
            return;
        }
        int n = undos.size();
        undoPos--;
        redoMax++;
        actions.getRedoAction().setEnabled(true);
        if (undoPos == 0) {
            actions.getUndoAction().setEnabled(false);
        }
        if (undoPos >= n) {
            return;
        }

        if (n < 1) {
            return;
        }
        YassUndoElement undoElem = undos.elementAt(undoPos);
        YassUndoElement nextUndoElem = undos
                .elementAt(undoPos + 1);

        Vector<YassRow> c = (Vector<YassRow>) undoElem.data.clone();
        n = c.size();
        for (int i = 0; i < n; i++) {
            c.setElementAt(c.elementAt(i).clone(), i);
        }

        boolean oldUndo = preventUndo;
        preventUndo = true;
        tm.setData(c);
        tm.fireTableDataChanged();
        preventUndo = oldUndo;

        ListSelectionModel sel = getSelectionModel();
        sel.setValueIsAdjusting(true);
        sel.clearSelection();
        Rectangle rr = new Rectangle();
        for (int i = 0; i < nextUndoElem.selectedRows.length; i++) {
            int k = nextUndoElem.selectedRows[i];
            rr.add(getCellRect(k, 0, true));
            sel.addSelectionInterval(k, k);
        }
        sel.setValueIsAdjusting(false);
        // scrollRectToVisible(rr);
        // zoomPage();

        if (sheet != null) {
            sheet.init();
            sheet.update();
            sheet.setBeatSize(nextUndoElem.sheetBeatSize);
            sheet.setViewPosition(nextUndoElem.sheetViewPosition);
            sheet.repaint();
        }
        // System.out.println("undo " + undoElem.sheetViewPosition);
    }

    /**
     * Adds a feature to the Row attribute of the YassTable object
     *
     * @param s The feature to be added to the Row attribute
     * @return Description of the Return Value
     */
    public synchronized boolean addRow(String s) {
        // trim empty lines
        if (s == null || s.trim().length() < 1 || s.trim().equals("#")) {
            tm.addRow("#", "", "", "", "", YassRow.EMPTY_LINE);
            return true;
        }
        int n = s.length();

        // trim text after "E"
        if (s.charAt(0) == 'E') {
            s = s.substring(1).trim();
            if (s.length() > 0) {
                tm.addRow("E", " " + s, "", "", "", YassRow.COMMENT_AFTER_END,
                        s);
            } else {
                tm.addRow("E", "", "", "", "");
            }
            return true;
        }
        if (s.charAt(0) == '#') {
            int i = s.indexOf(':');
            if (i >= 0 && i + 1 < s.length()) {
                String version = "";
                String tag = s.substring(1, i + 1);
                s = s.substring(i + 1).trim();
                if (tag.equals("TITLE:")) {
                    int ti = s.indexOf("[");
                    if (ti > 0) {
                        int tii = s.indexOf("]", ti);
                        if (tii < 0) {
                            version = " " + s.substring(ti);
                        } else {
                            version = " " + s.substring(ti, tii + 1);
                        }
                    }
                    if (ti > 0 && s.charAt(ti - 1) == ' ') {
                        ti--;
                    }
                    if (ti > 0) {
                        s = s.substring(0, ti);
                    }
                } else if (tag.equals("MP3:")) {
                    mp3 = s;
                } // bpm or gap are set to 0 for invalid input
                else if (tag.equals("BPM:")) {
                    bpm = Double.parseDouble(s.replace(',', '.'));
                } else if (tag.equals("GAP:")) {
                    gap = Double.parseDouble(s.replace(',', '.'));
                } else if (tag.equals("START:")) {
                    start = Double.parseDouble(s.replace(',', '.'));
                } else if (tag.equals("END:")) {
                    end = Double.parseDouble(s.replace(',', '.'));
                } else if (tag.equals("VIDEOGAP:")) {
                    vgap = Double.parseDouble(s.replace(',', '.'));
                } else if (tag.equals("PLAYERS:")) {
                    // s="all player names", version=""
                } else if (tag.equals("RELATIVE:")) {
                    isRelative = s.toLowerCase().equals("yes")
                            || s.toLowerCase().equals("true");
                }
                tm.addRow("#", tag, s, version, "");
                return true;
            }
            // non-tag comment
            tm.addRow("#", s.substring(1).trim(), "", "", "",
                    YassRow.INVALID_LINE);
            return true;
        }

        // insert space when missing
        char c0 = s.charAt(0);
        if (n > 1 && c0 == '-' || c0 == ':' || c0 == '*' || c0 == 'F'
                || c0 == 'f') {
            if (s.charAt(1) != ' ') {
                s = c0 + " " + s.substring(1);
                n++;
            }
        }

        if (s.charAt(0) == '-' && n > 1) {
            // skip spaces
            String time = s.substring(1).trim();
            int i = time.indexOf(' ');
            String time2 = "";
            if (i > 0) {
                time2 = time.substring(i + 1).trim();
                time = time.substring(0, i).trim();
                if (isRelative) {
                    int ti = Integer.parseInt(time);
                    int ti2 = (time2.length() > 0) ? Integer.parseInt(time2) : ti;
                    time = (ti + relativePageBreak) + "";
                    time2 = "";
                    relativePageBreak += ti2;
                }
            } else if (isRelative) {
                relativePageBreak += Integer.parseInt(time);
            }

            tm.addRow("-", time, time2, "", "");

            return true;
        }

        if (s.charAt(0) == 'P' && n > 2) {
            String pnum = s.substring(2).trim();
            try {
                int pn = Integer.parseInt(pnum);
                tm.addRow(s.charAt(0) + "", pnum, "", "", "");
                if (pn > 0) {
                    multiplayer = Math.max(multiplayer, pn);
                }
                return true;
            } catch (Exception e) {
                tm.addRow("#", s, "", "", "", YassRow.LINE_CUT);
                return true;
            }
        }

        // convert invalid lines into comments
        if (s.charAt(0) != ':' && s.charAt(0) != '*' && s.charAt(0) != 'F') {
            tm.addRow("#", s, "", "", "", YassRow.LINE_CUT);
            return true;
        }

        int i = s.indexOf(' ');
        if (i < 0) {
            tm.addRow(s.charAt(0) + "", "", "", "", "", YassRow.LINE_CUT);
            return true;
        }

        // skip spaces
        while (i < n && s.charAt(i) == ' ') {
            i++;
        }
        if (i == n) {
            tm.addRow(s.charAt(0) + "", "", "", "", "", YassRow.LINE_CUT);
            return true;
        }
        while (i < n && s.charAt(i) != ' ') {
            i++;
        }
        if (i == n) {
            tm.addRow(s.charAt(0) + "", "", "", "", "", YassRow.LINE_CUT);
            return true;
        }
        String time = s.substring(1, i).trim();
        try {
            Integer.parseInt(time);
        } catch (Exception e) {
            tm.addRow(s.charAt(0) + "", "", "", "", "", YassRow.LINE_CUT);
            return true;
        }

        while (i < n && s.charAt(i) == ' ') {
            i++;
        }
        int j = s.indexOf(' ', i);
        if (i == n || j < 0) {
            tm.addRow(s.charAt(0) + "", time, "", "", "", YassRow.LINE_CUT);
            return true;
        }
        String length = s.substring(i, j).trim();
        try {
            Integer.parseInt(length);
        } catch (Exception e) {
            tm.addRow(s.charAt(0) + "", time, "", "", "", YassRow.LINE_CUT);
            return true;
        }

        while (j < n && s.charAt(j) == ' ') {
            j++;
        }
        int k = s.indexOf(' ', j);
        if (j == n || k < 0) {
            tm.addRow(s.charAt(0) + "", time, length, "", "", YassRow.LINE_CUT);
            return true;
        }
        String height = s.substring(j, k).trim();
        try {
            Integer.parseInt(height);
        } catch (Exception e) {
            tm.addRow(s.charAt(0) + "", time, length, "", "", YassRow.LINE_CUT);
            return true;
        }

        if (k + 1 > n - 1) {
            tm.addRow(s.charAt(0) + "", time, length, height, "",
                    YassRow.LINE_CUT);
            return true;
        }
        String txt = s.substring(k + 1);

        if (isRelative) {
            int timeInt = Integer.parseInt(time);
            timeInt += relativePageBreak;
            time = timeInt + "";
        }

        txt = txt.replace(' ', YassRow.SPACE);
        tm.addRow(s.charAt(0) + "", time, length, height, txt);

        return true;
    }

    /**
     * Gets the rowAt attribute of the YassTable object
     *
     * @param row Description of the Parameter
     * @return The rowAt value
     */
    public YassRow getRowAt(int row) {
        return tm.getRowAt(row);
    }

    /**
     * Description of the Method
     *
     * @param b Description of the Parameter
     */
    public void shiftBeat(int b) {
        int row;
        int rows[] = getSelectedRows();
        if (rows == null || rows.length < 1) {
            return;
        }
        int minb = Integer.MAX_VALUE;
        int maxb = -1;
        for (int row1 : rows) {
            row = row1;
            YassRow r = getRowAt(row);
            if (r.isNote()) {
                int db = r.getBeatInt() + b;
                r.setBeat(db);
                minb = Math.min(db, minb);
                maxb = Math.max(db + r.getLengthInt(), maxb);
            } else if (r.isPageBreak()) {
                int db = r.getBeatInt() + b;
                r.setBeat(db);
                if (r.hasSecondBeat()) {
                    db = r.getSecondBeatInt() + b;
                    r.setSecondBeat(db);
                }
                minb = Math.min(db, minb);
                maxb = Math.max(db, maxb);
            }
        }

        if (sheet != null) {
            boolean scrolled = false;
            if (minb < Integer.MAX_VALUE) {
                int minx = sheet.beatToTimeline(minb);
                if (minx < sheet.getLeftX()) {
                    sheet.setLeftX(minx);
                    scrolled = true;
                }
            }
            if (!scrolled) {
                // int minx = sheet.beatToTimeline(minb);
                int maxx = sheet.beatToTimeline(maxb);
                int w = sheet.getClipBounds().width;
                if (maxx > sheet.getViewPosition().x + w) {
                    sheet.setViewPosition(new Point(maxx - w, 0));
                }
            }
        }
        if (zoomMode == ZOOM_ONE) {
            zoomPage();
        }
        updatePlayerPosition();

        addUndo();
        preventLyricsUpdate(true);
        tm.fireTableRowsUpdated(getSelectionModel().getMinSelectionIndex(),
                getSelectionModel().getMaxSelectionIndex());
        preventLyricsUpdate(false);
    }

    /**
     * Description of the Method
     *
     * @param b Description of the Parameter
     */
    public void shiftRemainder(int b) {
        int row = getSelectionModel().getMinSelectionIndex();
        if (row < 0 && sheet != null) {
            row = sheet.nextElement();
        }
        if (row < 0) {
            return;
        }

        int n = getRowCount();
        int minb = Integer.MAX_VALUE;
        for (int i = row; i < n; i++) {
            YassRow r = getRowAt(i);
            if (r.isNote()) {
                int db = r.getBeatInt() + b;
                r.setBeat(db);
                minb = Math.min(db, minb);
            } else if (r.isPageBreak()) {
                int db = r.getBeatInt() + b;
                r.setBeat(db);
                if (r.hasSecondBeat()) {
                    db = r.getSecondBeatInt() + b;
                    r.setSecondBeat(db);
                }
                minb = Math.min(db, minb);
            }
        }

        if (sheet != null && minb < Integer.MAX_VALUE) {
            int minx = sheet.beatToTimeline(minb);
            if (minx < sheet.getLeftX()) {
                sheet.setLeftX(minx);
            }
        }
        updatePlayerPosition();

        addUndo();
        preventLyricsUpdate(true);
        tm.fireTableRowsUpdated(row, n);
        preventLyricsUpdate(false);
    }

    /**
     * Description of the Method
     *
     * @param b Description of the Parameter
     */
    public void shiftLine(int b) {
        int row = getSelectionModel().getMinSelectionIndex();
        if (row < 0 && sheet != null) {
            row = sheet.nextElement();
        }
        if (row < 0) {
            return;
        }

        int pb = -1;
        int n = getRowCount();
        int i = row;
        YassRow rpb = null;
        YassRow r = getRowAt(i);
        while (r.isNote()) {
            r = getRowAt(--i);
        }
        if (r.isPageBreak()) {
            rpb = r;
            pb = i;
        }
        i++;

        int minb = Integer.MAX_VALUE;

        int maxb = -1;
        while (i < n) {
            r = getRowAt(i);
            if (r.isNote()) {
                int db = r.getBeatInt() + b;
                r.setBeat(db);
                minb = Math.min(db, minb);
                maxb = Math.max(db + r.getLengthInt(), maxb);

            } else if (r.isPageBreak()) {
                break;
            }
            i++;
        }

        if (rpb != null && pb > 0 && pb < n - 1) {
            int comm[] = new int[2];
            YassRow prev = getRowAt(pb - 1);
            YassRow next = getRowAt(pb + 1);
            if (prev.isNote() && next.isNote()) {
                comm[0] = prev.getBeatInt() + prev.getLengthInt();
                comm[1] = getRowAt(pb + 1).getBeatInt();
                YassAutoCorrect.getCommonPageBreak(comm, getBPM(), null);
                rpb.setBeat(comm[0]);
                rpb.setSecondBeat(comm[1]);
            }
        }

        if (sheet != null) {
            boolean scrolled = false;
            if (minb < Integer.MAX_VALUE) {
                int minx = sheet.beatToTimeline(minb);
                if (minx < sheet.getLeftX()) {
                    sheet.setLeftX(minx);
                    scrolled = true;
                }
            }
            if (!scrolled) {
                int maxx = sheet.beatToTimeline(maxb);
                int w = sheet.getClipBounds().width;
                if (maxx > sheet.getViewPosition().x + w) {
                    sheet.setViewPosition(new Point(maxx - w, 0));
                }
            }
        }
        updatePlayerPosition();

        addUndo();
        preventLyricsUpdate(true);
        tm.fireTableRowsUpdated(row, n);
        preventLyricsUpdate(false);
    }

    /**
     * Description of the Method
     */
    public void selectLine() {
        int row = getSelectionModel().getMinSelectionIndex();
        if (row < 0 && sheet != null) {
            row = sheet.nextElement();
            if (row < 0) {
                return;
            }
            YassRow r = getRowAt(row);
            if (r == null) {
                return;
            }
            if (!r.isNote()) {
                row--;
            }
        }
        if (row < 0) {
            return;
        }

        int n = getRowCount();
        int i = row;
        YassRow r = getRowAt(i);
        if (!r.isNote()) {
            return;
        }

        while (r.isNote() && i > 0) {
            r = getRowAt(--i);
        }

        if (!r.isNote()) {
            i++;
        }

        int j = row;
        ;
        while (j < n) {
            r = getRowAt(j);
            if (r.isPageBreak()) {
                break;
            }
            j++;
        }
        clearSelection();
        addRowSelectionInterval(i, j - 1);
        updatePlayerPosition();
    }

    /**
     * Description of the Method
     */
    public void selectAll() {
        int n = getRowCount();
        int i = 0;
        YassRow r = getRowAt(i);
        while (!r.isNoteOrPageBreak() && i < n) {
            r = getRowAt(++i);
        }

        if (!r.isNoteOrPageBreak()) {
            return;
        }

        int j = n - 1;
        r = getRowAt(j);
        while (!r.isNoteOrPageBreak() && j > 0) {
            r = getRowAt(--j);
        }

        clearSelection();
        addRowSelectionInterval(i, j);

        updatePlayerPosition();

        if (zoomMode != ZOOM_TIME) {
            sheet.setZoom(i, j, true);
            sheet.scrollRectToVisible(i, j);
        }
        actions.setRelative(false);
        setZoomMode(ZOOM_MULTI);
        addMultiSize(getRowCount());

        if (sheet != null) {
            sheet.repaint();
        }
        preventLyricsUpdate(true);
        tm.fireTableRowsUpdated(getSelectionModel().getMinSelectionIndex(),
                getSelectionModel().getMaxSelectionIndex());
        preventLyricsUpdate(false);
        return;
    }

    /**
     * Description of the Method
     *
     * @param h Description of the Parameter
     */
    public void shiftHeight(int h) {
        int row;
        int rows[] = getSelectedRows();
        if (rows == null || rows.length < 1) {
            return;
        }
        /*
		 * if (rows.length==1) { row = rows[0]; YassRow r = getRowAt(row); if
		 * (r.isNote()) { int n = r.getHeightInt() + h; midi.play(n); } }
		 */
        for (int row1 : rows) {
            row = row1;
            YassRow r = getRowAt(row);
            if (r.isNote()) {
                r.setHeight(r.getHeightInt() + h);
            }
        }
        addUndo();

        if (sheet != null) {
            sheet.init();
            sheet.update();
            // sheet.repaint();
        }
        preventLyricsUpdate(true);
        tm.fireTableRowsUpdated(getSelectionModel().getMinSelectionIndex(),
                getSelectionModel().getMaxSelectionIndex());
        preventLyricsUpdate(false);
    }

    /**
     * Description of the Method
     *
     * @param d Description of the Parameter
     */
    public void shiftLeftEndian(int d) {
        int row = -1;
        int row2 = -1;
        int rows[] = getSelectedRows();
        if (rows == null || rows.length < 1) {
            return;
        }
        if (rows.length == 1) {
            row = rows[0];
        } else {
            int minb = Integer.MAX_VALUE;
            int dd = d;
            for (int row1 : rows) {
                YassRow r = getRowAt(row1);
                if (!r.isNote()) {
                    continue;
                }
                int k = row1 - 1;
                YassRow r2 = getRowAt(k);
                while (r2.isPageBreak() && (k > 0)) {
                    r2 = getRowAt(--k);
                }
                if (!r2.isNoteOrPageBreak()) {
                    r2 = null;
                }

                d = dd;
                int beat = r.getBeatInt() + d;
                int len = r.getLengthInt();
                int dur = len - d;
                if (dur < 1) {
                    beat -= 1 - dur;
                    dur = 1;
                    d = r.getLengthInt() - 1;
                }
                if (r2 != null) {
                    // prevent dragging beyond left note
                    int beat2 = r2.getBeatInt() + r2.getLengthInt();
                    if (beat < beat2) {
                        beat = beat2;
                        dur = r.getBeatInt() - beat + len;
                    }
                }
                r.setBeat(beat);
                minb = Math.min(beat, minb);
                r.setLength(dur);
            }
            if (sheet != null && minb < Integer.MAX_VALUE) {
                int minx = sheet.beatToTimeline(minb);
                if (minx < sheet.getLeftX()) {
                    sheet.setLeftX(minx);
                }
            }
            if (zoomMode == ZOOM_ONE) {
                zoomPage();
            }
            updatePlayerPosition();

            addUndo();
            preventLyricsUpdate(true);
            tm.fireTableRowsUpdated(getSelectionModel().getMinSelectionIndex(),
                    getSelectionModel().getMaxSelectionIndex());
            preventLyricsUpdate(false);
            return;
        }

        YassRow r = getRowAt(row);

        YassRow r2 = null;
        if (row2 >= 0) {
            r2 = getRowAt(row2);
            if (!r2.isNoteOrPageBreak()) {
                r2 = null;
            }
        }

        int minb = Integer.MAX_VALUE;

        int beat = 0;
        if (r.isNote()) {
            beat = r.getBeatInt() + d;
            int len = r.getLengthInt();
            int dur = len - d;
            if (dur < 1) {
                beat -= 1 - dur;
                dur = 1;
                d = r.getLengthInt() - 1;
            }
            if (r2 != null) {
                int beat2 = r2.getBeatInt();
                if (beat < beat2 + 1) {
                    beat = beat2 + 1;
                    dur = r.getBeatInt() - beat + len;
                }
            }
            r.setBeat(beat);
            minb = Math.min(beat, minb);
            r.setLength(dur);
        } else if (r.isPageBreak()) {
            beat = r.getBeatInt() + d;
            int beat2 = r.getSecondBeatInt() + d;
            if (beat > beat2 - 1) {
                beat = beat2 - 1;
            }
            r.setBeat(beat);
            minb = Math.min(beat, minb);
        }

        if (r2 != null) {
            r = r2;
            if (r.isNote()) {
                r.setLength(Math.max(1, r.getLengthInt() + d));
            } else if (r.isPageBreak()) {
                int beat1 = r.getBeatInt() + d;
                int beat2 = r.getSecondBeatInt() + d;
                if (beat2 < beat1 + 1) {
                    beat2 = beat1 + 1;
                }
                r.setSecondBeat(beat2);
            }
        }

        if (sheet != null && minb < Integer.MAX_VALUE) {
            int minx = sheet.beatToTimeline(minb);
            if (minx < sheet.getLeftX()) {
                sheet.setLeftX(minx);
            }
        }
        if (zoomMode == ZOOM_ONE) {
            zoomPage();
        }
        updatePlayerPosition();

        addUndo();
        preventLyricsUpdate(true);
        tm.fireTableRowsUpdated(getSelectionModel().getMinSelectionIndex(),
                getSelectionModel().getMaxSelectionIndex());
        preventLyricsUpdate(false);
    }

    /**
     * Description of the Method
     *
     * @param d Description of the Parameter
     */
    public void shiftRightEndian(int d) {
        int row = -1;
        int rows[] = getSelectedRows();
        if (rows == null || rows.length < 1) {
            return;
        }
        int n = getRowCount();
        if (rows.length == 1) {
            row = rows[0];
        } else {
            int maxb = -1;
            for (int row1 : rows) {
                YassRow r = getRowAt(row1);
                if (!r.isNote()) {
                    continue;
                }

                int k = row1 + 1;
                YassRow r2 = getRowAt(k);
                while (r2.isPageBreak() && (k < n - 1)) {
                    r2 = getRowAt(++k);
                }
                if (!r2.isNoteOrPageBreak()) {
                    r2 = null;
                }

                int beat1 = r.getBeatInt();
                int len = r.getLengthInt();
                len = Math.max(1, len + d);
                int beat2 = 0;

                if (r2 != null) {
                    // prevent dragging beyond right note
                    beat2 = r2.getBeatInt();
                    if (beat1 + len > beat2) {
                        len = beat2 - beat1;
                    }
                }
                r.setLength(len);
                beat2 = beat1 + len;
                maxb = Math.max(beat2, maxb);
            }
            if (sheet != null) {
                int maxx = sheet.beatToTimeline(maxb);
                int w = sheet.getClipBounds().width;
                if (maxx > sheet.getViewPosition().x + w) {
                    sheet.setViewPosition(new Point(maxx - w, 0));
                }
            }
            if (zoomMode == ZOOM_ONE) {
                zoomPage();
            }
            updatePlayerPosition();

            addUndo();
            preventLyricsUpdate(true);
            tm.fireTableRowsUpdated(getSelectionModel().getMinSelectionIndex(),
                    getSelectionModel().getMaxSelectionIndex());
            preventLyricsUpdate(false);
            return;
        }

        int maxb = -1;
        int beat2 = 0;
        YassRow r = getRowAt(row);
        if (r.isNote()) {
            int beat1 = r.getBeatInt() + d;
            int len = r.getLengthInt();
            len = Math.max(1, len + d);
            r.setLength(len);
            beat2 = beat1 + len;
            maxb = Math.max(beat2, maxb);
        } else if (r.isPageBreak()) {
            int beat1 = r.getBeatInt() + d;
            beat2 = r.getSecondBeatInt() + d;
            if (beat2 < beat1 + 1) {
                beat2 = beat1 + 1;
            }
            r.setSecondBeat(beat2);
            maxb = Math.max(beat2, maxb);
        }

        if (sheet != null) {
            int maxx = sheet.beatToTimeline(maxb);
            int w = sheet.getClipBounds().width;
            if (maxx > sheet.getViewPosition().x + w) {
                sheet.setViewPosition(new Point(maxx - w, 0));
            }
        }
        if (zoomMode == ZOOM_ONE) {
            zoomPage();
        }

        addUndo();
        preventLyricsUpdate(true);
        tm.fireTableRowsUpdated(getSelectionModel().getMinSelectionIndex(),
                getSelectionModel().getMaxSelectionIndex());
        preventLyricsUpdate(false);
    }

    /**
     * Sets the type attribute of the YassTable object
     *
     * @param s The new type value
     */
    public void setType(String s) {
        int row;
        int rows[] = getSelectedRows();
        if (rows == null || rows.length < 1) {
            return;
        }

        boolean changed = false;
        for (int row2 : rows) {
            row = row2;
            YassRow r = getRowAt(row);
            if (r.isNote()) {
                if (!r.getType().equals(s)) {
                    r.setType(s);
                    changed = true;
                }
            }
        }
        if (!changed) {
            for (int row1 : rows) {
                row = row1;
                YassRow r = getRowAt(row);
                if (r.isNote()) {
                    r.setType(":");
                }
            }
        }

        addUndo();
        tm.fireTableRowsUpdated(getSelectionModel().getMinSelectionIndex(),
                getSelectionModel().getMaxSelectionIndex());
    }

    /**
     * Description of the Method
     *
     * @param n Description of the Parameter
     */
    public void gotoPageNumber(int n) {
        int pn = 1;
        int i = 0;
        Enumeration<?> en = tm.getData().elements();
        while (en.hasMoreElements()) {
            YassRow r = (YassRow) en.nextElement();
            if (n == 0 || (r.isNote() && n == pn)) {
                setRowSelectionInterval(i, i);
                updatePlayerPosition();
                zoomPage();
                return;
            }
            if (r.isPageBreak()) {
                pn++;
            }
            i++;
        }

        i = getRowCount();
        if (i < 1) {
            return;
        }
        setRowSelectionInterval(i - 1, i - 1);
        updatePlayerPosition();
        zoomPage();
    }

    /**
     * Gets the pageNumber attribute of the YassTable object
     *
     * @return The pageNumber value
     */
    public int getPageNumber() {
        int row = getSelectionModel().getMinSelectionIndex();
        if (row < 0) {
            return -1;
        }
        return getPageNumber(row);
    }

    /**
     * Gets the rows attribute of the YassTable object
     *
     * @return The rows value
     */
    public Enumeration<YassRow> getRows() {
        return tm.getData().elements();
    }

    /**
     * Gets the firstVisiblePageNumber attribute of the YassTable object
     *
     * @return The firstVisiblePageNumber value
     */
    public int getFirstVisiblePageNumber() {
        if (sheet == null) {
            return -1;
        }
        int row = sheet.firstVisibleElement() + 1;
        // +1: quick hack
        if (row < 0) {
            return -1;
        }
        return getPageNumber(row);
    }

    /**
     * Gets the pageNumber attribute of the YassTable object
     *
     * @param row Description of the Parameter
     * @return The pageNumber value
     */
    public int getPageNumber(int row) {
        YassRow r = getRowAt(row);
        if (r.isComment()) {
            return 0;
        }
        if (r.isEnd()) {
            return getPageCount() + 1;
        }

        int pn = 1;
        int i = 0;
        Enumeration<?> en = tm.getData().elements();
        while (en.hasMoreElements()) {
            r = (YassRow) en.nextElement();
            if (i == row) {
                return pn;
            }
            if (r.isPageBreak()) {
                pn++;
            }
            i++;
        }
        return -1;
    }

    /**
     * Gets the pageCount attribute of the YassTable object
     *
     * @return The pageCount value
     */
    public int getPageCount() {
        int pn = 1;
        Enumeration<?> en = tm.getData().elements();
        while (en.hasMoreElements()) {
            YassRow r = (YassRow) en.nextElement();
            if (r.isPageBreak()) {
                pn++;
            }
        }
        return pn;
    }

    /**
     * Gets the pages attribute of the YassTable object
     *
     * @return The pages value
     */
    public Vector<YassPage> getPages() {
        Vector<YassPage> pages = new Vector<>();
        YassPage p = null;

        boolean first = true;
        for (Enumeration<?> en = tm.getData().elements(); en.hasMoreElements(); ) {
            YassRow r = (YassRow) en.nextElement();

            if (r.isNote()) {
                if (first) {
                    p = new YassPage(this);
                    pages.addElement(p);
                    first = false;
                }
                p.addRow(r);
            }
            if (p != null && (r.isPageBreak() || r.isEnd())) {
                p.addRow(r);
                first = true;
            }
        }
        return pages;
    }

    /**
     * Gets the multiSize attribute of the YassTable object
     *
     * @return The multiSize value
     */
    public int getMultiSize() {
        return multiSize;
    }

    /**
     * Sets the multiSize attribute of the YassTable object
     *
     * @param i The new multiSize value
     */
    public void setMultiSize(int i) {
        multiSize = i;
    }

    /**
     * Adds a feature to the MultiSize attribute of the YassTable object
     *
     * @param i The feature to be added to the MultiSize attribute
     */
    public void addMultiSize(int i) {
        multiSize += i;
        if (multiSize < 1) {
            multiSize = 1;
        }
        int n = getPageCount() + 2;
        int k = getPageNumber();
        if (multiSize > n - k + 1) {
            multiSize = n - k + 1;
        }
    }

    /**
     * Gets the preventZoom attribute of the YassTable object
     *
     * @return The preventZoom value
     */
    public boolean getPreventZoom() {
        return preventZoom;
    }

    /**
     * Sets the preventZoom attribute of the YassTable object
     *
     * @param onoff The new preventZoom value
     */
    public void setPreventZoom(boolean onoff) {
        preventZoom = onoff;
    }

    /**
     * Description of the Method
     */
    public void zoomPage() {
        if (preventZoom) {
            return;
        }

        if (sheet == null) {
            return;
        }
        double pos = sheet.fromTimeline(sheet.getPlayerPosition());

        int i = getSelectionModel().getMinSelectionIndex();
        int j = getSelectionModel().getMaxSelectionIndex();
        if (i < 0) {
            i = sheet.nextElement();
        }
        if (i < 0) {
            return;
        }
        if (j < 0) {
            j = i;
        }

        if (zoomMode == ZOOM_TIME) {
            sheet.setZoom(80 * 60 / bpm);
        }

        int n = getRowCount();
        int ij[] = null;
        int lastij[] = null;
        if (zoomMode == ZOOM_ONE) {
            ij = enlargeToPages(i, j);
        }
        if (zoomMode == ZOOM_MULTI) {
            ij = enlargeToPages(i, j);
            if (ij == null) {
                return;
            }

            int k = multiSize;
            int span = countSelectedPages();
            if (span > 1) {
                k -= span;
            }

            boolean addEnd = false;

            while (k > 1 && ij != null) {
                lastij = ij;
                ij[1] = enlargeToPageBreak(Math.min(ij[1] + 1, n - 1));
                boolean endReached = getRowAt(ij[1]).isEnd();
                if (endReached && !addEnd) {
                    while (ij[1] > 0 && !getRowAt(ij[1]).isNote()) {
                        ij[1]--;
                    }
                    addEnd = true;
                }
                k--;
            }
        }
        if (zoomMode == ZOOM_TIME) {
            ij = new int[]{i, j};
        }
        if (ij == null) {
            return;
        }

        Rectangle rr = getCellRect(ij[0], 0, true);
        rr.add(getCellRect(ij[1], 4, true));
        scrollRectToVisible(rr);

        if (zoomMode != ZOOM_TIME) {
            sheet.setZoom(ij[0], ij[1], true);
        }
        sheet.setPlayerPosition(sheet.toTimeline(pos));
        if (zoomMode == ZOOM_TIME) {
            sheet.scrollRectToVisible(ij[0], ij[1]);
        }

        actions.revalidateLyricsArea();
        sheet.repaint();
    }

    /**
     * Description of the Method
     *
     * @param i Description of the Parameter
     * @param j Description of the Parameter
     * @return Description of the Return Value
     */
    public int[] enlargeToPages(int i, int j) {
        if (i < 0) {
            return null;
        }

        int n = getRowCount();
        YassRow r = getRowAt(i);

        if (r.isEnd()) {
            return new int[]{i, i};
        }

        while (!(r.isNote()) && i > 0) {
            r = getRowAt(--i);
        }
        // move back until i-->note
        boolean inHeader = !r.isNote();
        YassRow r2 = getRowAt(j);
        while (!(r2.isNote()) && j < n - 1) {
            r2 = getRowAt(++j);
        }
        // // move forward until j-->note
        if (inHeader) {
            return new int[]{i, j - 1};
        }

        r = getRowAt(i);
        while (r.isNote() && i > 0) {
            r = getRowAt(--i);
        }

        r2 = getRowAt(j);
        while (r2.isNote() && j < n - 1) {
            r2 = getRowAt(++j);
        }

        return new int[]{i + 1, j - 1};
    }

    /**
     * Description of the Method
     *
     * @param j Description of the Parameter
     * @return Description of the Return Value
     */
    public int enlargeToPageBreak(int j) {
        int n = getRowCount();
        YassRow r2 = getRowAt(j);
        while (j < n - 1 && !r2.isPageBreak() && !r2.isEnd()) {
            j++;
            r2 = getRowAt(j);
        }
        return j;
    }

    /**
     * Description of the Method
     */
    public void home() {
        int row = getSelectionModel().getMinSelectionIndex();
        if (row < 0 && sheet != null) {
            row = sheet.nextElement();
        }
        if (row < 0) {
            return;
        }

        int n = getRowCount();
        YassRow r = getRowAt(row);
        while (!r.isNote() && row > 0) {
            r = getRowAt(--row);
        }

        while (r.isNote() && row > 0) {
            r = getRowAt(--row);
        }
        row++;

        if (row < 0 || row > n - 1) {
            return;
        }
        setRowSelectionInterval(row, row);
        Rectangle rr = getCellRect(row, 0, true);
        scrollRectToVisible(rr);
        updatePlayerPosition();
    }

    /**
     * Description of the Method
     *
     * @param k Description of the Parameter
     */
    public void note(int k) {
        int notes = 0;
        int n = getRowCount();
        int row = 0;
        YassRow r = getRowAt(0);
        if (r.isNoteOrPageBreak()) {
            notes++;
        }
        while (notes < k && row < n - 1) {
            r = getRowAt(++row);
            if (r.isNoteOrPageBreak()) {
                notes++;
            }
        }
        setRowSelectionInterval(row, row);
        Rectangle rr = getCellRect(row, 0, true);
        if (row < n - 1) {
            rr.add(getCellRect(row + 1, 0, true));
        }
        if (row > 0) {
            rr.add(getCellRect(row - 1, 0, true));
        }
        scrollRectToVisible(rr);
        updatePlayerPosition();
    }

    /**
     * Description of the Method
     */
    public void end() {
        int row = getSelectionModel().getMaxSelectionIndex();
        int n = getRowCount();
        if (row < 0 && sheet != null) {
            row = sheet.nextElement();
        }
        if (row < 0) {
            return;
        }

        YassRow r = getRowAt(row);
        while (!r.isNote() && row > 0) {
            r = getRowAt(--row);
        }

        while (r.isNote() && row < n - 1) {
            r = getRowAt(++row);
        }
        row--;
        if (row < 0 || row > n - 1) {
            return;
        }

        setRowSelectionInterval(row, row);
        updatePlayerPosition();
    }

    /**
     * Gets the firstNote attribute of the YassTable object
     *
     * @return The firstNote value
     */
    public YassRow getFirstNote() {
        int n = getRowCount();
        if (n < 0) {
            return null;
        }

        int i = 0;
        YassRow r = getRowAt(i);
        while (!r.isNote() && i < n - 1) {
            r = getRowAt(++i);
        }
        if (!r.isNote()) {
            return null;
        }
        return r;
    }

    /**
     * Description of the Method
     */
    public void firstNote() {
        int n = getRowCount();
        if (n <= 0) {
            return;
        }

        int i = 0;
        YassRow r = getRowAt(i);
        while (!r.isNote() && i < n - 1) {
            r = getRowAt(++i);
        }
        if (!r.isNote()) {
            return;
        }

        setRowSelectionInterval(i, i);
        updatePlayerPosition();

        Rectangle rr = getCellRect(i, 0, true);
        scrollRectToVisible(rr);
        zoomPage();
    }

    /**
     * Description of the Method
     */
    public void lastNote() {
        int n = getRowCount();
        if (n < 0) {
            return;
        }

        int i = n - 1;
        YassRow r = getRowAt(i);
        while (!r.isNote() && i > 0) {
            r = getRowAt(--i);
        }
        if (!r.isNote()) {
            return;
        }

        setRowSelectionInterval(i, i);
        updatePlayerPosition();

        Rectangle rr = getCellRect(i, 0, true);
        scrollRectToVisible(rr);
        zoomPage();

    }

    public YassRow getNoteAtBeat(int beat) {
        YassRow r;
        int i = 0;
        int n = getRowCount();
        while (i < n - 1) {
            r = getRowAt(i++);
            if (r.isNote() && r.getBeatInt() == beat) return r;
        }
        return null;
    }

    public YassRow getNoteEndingAtBeat(int beat) {
        YassRow r;
        int i = 0;
        int n = getRowCount();
        while (i < n - 1) {
            r = getRowAt(i++);
            if (r.isNote() && r.getBeatInt() + r.getLengthInt() == beat) return r;
        }
        return null;
    }

    /**
     * Returns closest note before given beat.
     * If a note exactly hits that beat, it is returned.
     * Stops search at any note that lays after the given beat.
     */
    public int getIndexOfNoteBeforeBeat(int beat) {
        YassRow r;
        int i = 0;
        int iMin = -1;
        int n = getRowCount();
        int min = Integer.MAX_VALUE;
        while (i < n - 1) {
            r = getRowAt(i);
            if (r.isNote()) {
                int b = r.getBeatInt();
                if (b <= beat) {
                    if (beat - b < min) {
                        min = beat - b;
                        iMin = i;
                    }
                }
                else break;
            }
            i++;
        }
        return iMin;
    }

    /**
     * Returns closest note before given beat.
     * If a note exactly hits that beat, it is returned.
     * Stops search at any note that lays after the given beat.
     */
    public YassRow getNoteBeforeBeat(int beat) {
        YassRow r;
        YassRow rMin = null;
        int i = 0;
        int n = getRowCount();
        int min = Integer.MAX_VALUE;
        while (i < n - 1) {
            r = getRowAt(i);
            if (r.isNote()) {
                int b = r.getBeatInt();
                if (b <= beat) {
                    if (beat - b < min) {
                        min = beat - b;
                        rMin = r;
                    }
                }
                else break;
            }
            i++;
        }
        return rMin;
    }

    /**
     * Returns closest note end before given beat.
     * If a note end exactly hits that beat, it is returned.
     * Stops search at any note end that lays after the given beat.
     */
    public YassRow getNoteEndingBeforeBeat(int beat) {
        YassRow r;
        YassRow rMin = null;
        int i = 0;
        int n = getRowCount();
        int min = Integer.MAX_VALUE;
        while (i < n - 1) {
            r = getRowAt(i++);
            if (r.isNote()) {
                int b = r.getBeatInt() + r.getLengthInt();
                if (b <= beat) {
                    if (beat - b < min) {
                        min = beat - b;
                        rMin = r;
                    }
                }
                else break;
            }
        }
        return rMin;
    }

    /**
     * Description of the Method
     */
    public void prevBeat() {
        prevBeat(false);
    }

    /**
     * Description of the Method
     *
     * @param add Description of the Parameter
     */
    public void prevBeat(boolean add) {
        int rows[] = getSelectedRows();
        int i = -1;
        int n = getRowCount();
        if (rows == null || rows.length < 1) {
            // if nothing selected
            i = sheet != null ? sheet.nextElement() : -1;
            // take next possible note
            if (i < 0) {
                i = n - 1;
            } else if (!getRowAt(i).isNoteOrPageBreak()) {
                i = 1;
            }
            // or goto start
        } else {
            i = rows[0];
        }

        if (i > 0) {
            if (!add && !getRowAt(i - 1).isNote()) {
                end();
            } else {
                if (add) {
                    if (rows.length > 1) {
                        int j = rows[rows.length - 2];
                        while (!getRowAt(j).isNote()) {
                            j--;
                            if (j < i) {
                                return;
                            }
                        }
                        setRowSelectionInterval(i, j);
                    }
                } else {
                    setRowSelectionInterval(i - 1, i - 1);
                }

                Rectangle rr = getCellRect(i - 1, 0, true);
                rr.add(getCellRect(i - 1, 4, true));
                scrollRectToVisible(rr);

                updatePlayerPosition();
                if (add) {
                    adjustMultiSize();
                }
            }
        }
    }

    /**
     * Description of the Method
     */
    public void nextBeat() {
        nextBeat(false);
    }

    /**
     * Description of the Method
     *
     * @param add Description of the Parameter
     */
    public void nextBeat(boolean add) {
        int rows[] = getSelectedRows();
        int i = -1;
        int n = getRowCount();
        if (n < 1) {
            return;
        }

        if (rows == null || rows.length < 1) {
            // if nothing selected
            int k = sheet != null ? sheet.nextElement() : 0;
            // take next possible note
            if (k >= 0) {
                while (k < n && !getRowAt(k).isNoteOrPageBreak()) {
                    k++;
                }
                if (k < n) {
                    i = k - 1;
                }
            } else {
                i = n - 2;
            }
        } else {
            i = rows[rows.length - 1];
        }
        if (i < n - 1) {
            if (!add && !getRowAt(i + 1).isNote()) {
                home();
            } else {
                if (add) {
                    int j = i + 1;
                    while (!getRowAt(j).isNote()) {
                        j++;
                        if (j >= n) {
                            return;
                        }
                    }
                    setRowSelectionInterval(rows[0], j);
                } else {
                    setRowSelectionInterval(i + 1, i + 1);
                }

                Rectangle rr = getCellRect(i + 1, 0, true);
                rr.add(getCellRect(i + 1, 4, true));
                scrollRectToVisible(rr);

                updatePlayerPosition();
                if (add) {
                    adjustMultiSize();
                }
            }
        }
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public int countSelectedPages() {
        int i = getSelectionModel().getMinSelectionIndex();
        int j = getSelectionModel().getMaxSelectionIndex();
        if (i < 0) {
            return 0;
        }

        int span = 1;
        for (int k = i; k <= j; k++) {
            if (getRowAt(k).isPageBreak()) {
                span++;
            }
        }
        return span;
    }

    /**
     * Description of the Method
     */
    public void adjustMultiSize() {
        int s = getSelectionModel().getMinSelectionIndex();
        int t = getSelectionModel().getMaxSelectionIndex();
        if (sheet == null || s < 0 || t < 0
                || (sheet.isVisible(s) && sheet.isVisible(t))) {
            return;
        }

        int span = countSelectedPages();
        if (span > 1) {
            if (sheet != null) {
                sheet.enablePan(false);
            }
            zoomMode = ZOOM_MULTI;
            multiSize = span + 1;
        } else {
            if (sheet != null) {
                sheet.enablePan(true);
            }
            zoomMode = ZOOM_ONE;
            multiSize = 1;
        }
        zoomPage();
    }

    /**
     * Description of the Method
     */
    public void selectNextBeat() {
        nextBeat(true);
    }

    /**
     * Description of the Method
     */
    public void selectPrevBeat() {
        prevBeat(true);
    }

    /**
     * Description of the Method
     */
    public void gotoGap() {
        YassRow r = tm.getCommentRow("GAP:");
        if (r == null) {
            return;
        }
        int i = tm.getData().indexOf(r);
        setRowSelectionInterval(i, i);
        Rectangle rr = getCellRect(i, 0, true);
        rr.add(getCellRect(i, 4, true));
        scrollRectToVisible(rr);
        updatePlayerPosition();
    }

    /**
     * Description of the Method
     */
    public void gotoStart() {
        YassRow r = tm.getCommentRow("START:");
        if (r == null) {
            return;
        }
        int i = tm.getData().indexOf(r);
        setRowSelectionInterval(i, i);
        Rectangle rr = getCellRect(i, 0, true);
        rr.add(getCellRect(i, 4, true));
        scrollRectToVisible(rr);
        updatePlayerPosition();
    }

    /**
     * Description of the Method
     */
    public void gotoEnd() {
        YassRow r = tm.getCommentRow("END:");
        if (r == null) {
            return;
        }
        int i = tm.getData().indexOf(r);
        setRowSelectionInterval(i, i);
        Rectangle rr = getCellRect(i, 0, true);
        rr.add(getCellRect(i, 4, true));
        scrollRectToVisible(rr);
        updatePlayerPosition();
    }

    /**
     * Gets the page attribute of the YassTable object
     *
     * @param b Description of the Parameter
     * @return The page value
     */
    public int getPage(int b) {
        int pn = 1;
        int i = 0;
        Enumeration<?> en = tm.getData().elements();
        while (en.hasMoreElements()) {
            YassRow r = (YassRow) en.nextElement();
            if (r.isNote() && pn == b) {
                return i;
            }
            if (r.isPageBreak()) {
                pn++;
            }
            i++;
        }
        return -1;
    }

    /**
     * Description of the Method
     *
     * @param b Description of the Parameter
     */
    public void gotoPage(int b) {
        int n = getRowCount();
        if (n < 1) {
            return;
        }

        int row = 0;
        int rows[] = getSelectedRows();
        if (rows == null || rows.length < 1) {
            row = sheet != null ? sheet.nextElement() : 0;
            if (row < 0) {
                row = 0;
            }
        } else {
            if (b < 0) {
                row = rows[0];
            } else {
                row = rows[rows.length - 1];
            }
        }

        if (b < 0) {
            YassRow r = getRowAt(row);
            while (r.isNote() && row > 0) {
                r = getRowAt(--row);
            }
            while ((!r.isNote()) && row > 0) {
                r = getRowAt(--row);
            }
            while (r.isNote() && row > 0) {
                r = getRowAt(--row);
            }
            if (row > 0) {
                row++;
            }
            // if (row==0) --> select START
        } else if (b > 0) {
            YassRow r = getRowAt(row);
            while (r.isNote() && row < n - 1) {
                r = getRowAt(++row);
            }
            while ((!r.isNote()) && row < n - 1) {
                r = getRowAt(++row);
            }
            // if (row==n-1) --> select END
        }

        setRowSelectionInterval(row, row);
        Rectangle rr = getCellRect(row, 0, true);
        rr.add(getCellRect(row, 4, true));
        scrollRectToVisible(rr);

        updatePlayerPosition();
        if (sheet != null && !sheet.isVisible(row)) {
            zoomPage();
        }
    }

    /**
     * Description of the Method
     *
     * @param b Description of the Parameter
     */
    public void gotoPageBreak(int b) {
        int row = 0;
        int rows[] = getSelectedRows();
        if (rows == null || rows.length < 1) {
            row = sheet != null ? sheet.nextElement() : 0;
            if (row < 0) {
                row = 0;
            }
        } else {
            if (b < 0) {
                row = rows[0];
            } else {
                row = rows[rows.length - 1];
            }
        }

        int n = getRowCount();
        if (b < 0 && row > 1) {
            row -= 2;
            YassRow r = getRowAt(row);
            while (!r.isPageBreak() && row > 0) {
                r = getRowAt(--row);
            }
            if (row == 0) {
                return;
            }
        } else if (b > 0 && row < n - 1) {
            YassRow r = getRowAt(++row);
            while (!r.isPageBreak() && row < n - 1) {
                r = getRowAt(++row);
            }
            if (row == n - 1) {
                return;
            }
        }
        if (row < n - 1) {
            setRowSelectionInterval(row + 1, row + 1);
        }

        int ij[] = enlargeToPages(row, row);
        if (ij == null) {
            return;
        }
        Rectangle rr = getCellRect(ij[0], 0, true);
        rr.add(getCellRect(ij[1], 4, true));
        scrollRectToVisible(rr);

        if (sheet != null) {
            sheet.setZoom(ij[0], ij[1], true);
            sheet.repaint();
        }
    }

    /**
     * Description of the Method
     *
     * @param force Description of the Parameter
     */
    public void zoomSelection(boolean force) {
        if (sheet == null) {
            return;
        }

        int i = getSelectionModel().getMinSelectionIndex();
        int j = getSelectionModel().getMaxSelectionIndex();
        if (i < 0) {
            return;
        }
        sheet.setZoom(i, j, force);
        sheet.repaint();
    }

    /**
     * Description of the Method
     */
    public void zoomAll() {
        if (sheet != null) {
            sheet.setZoom(0, getRowCount() - 1, true);
        }
    }

    /**
     * Description of the Method
     */
    public void zoom() {
        if (sheet == null) {
            return;
        }

        sheet.enablePan(false);
        if (zoomMode == ZOOM_TIME) {
            zoomPage();
        } else if (zoomMode == ZOOM_ONE) {
            sheet.enablePan(true);
            zoomPage();
        } else if (zoomMode == ZOOM_MULTI) {
            zoomPage();
        } else if (zoomMode == ZOOM_ALL) {
            zoomAll();
        }
    }

    /**
     * Description of the Method
     */
    public void multiply() {
        int sel = getSelectionModel().getMinSelectionIndex();
        if (sel < 0 && sheet != null) {
            sel = sheet.nextElement();
        }

        int n = getRowCount();
        Enumeration<?> en = tm.getData().elements();
        while (en.hasMoreElements()) {
            YassRow r = (YassRow) en.nextElement();

            if (r.isNote()) {
                r.setBeat(r.getBeatInt() * 2);
                r.setLength(r.getLengthInt() * 2);
            }
            if (r.isPageBreak()) {
                r.setBeat(r.getBeatInt() * 2);
                if (r.hasSecondBeat()) {
                    r.setSecondBeat(r.getSecondBeatInt() * 2);
                }
            }
        }
        setBPM(getBPM() * 2);
        tm.fireTableDataChanged();
        if (sel >= 0) {
            setRowSelectionInterval(sel, sel);
            zoomPage();
        }
    }

    /**
     * Description of the Method
     */
    public void divide() {
        int sel = getSelectionModel().getMinSelectionIndex();
        if (sel < 0 && sheet != null) {
            sel = sheet.nextElement();
        }

        if (getBPM() / 2.0 < 20)
            return;

        Enumeration<YassRow> en = tm.getData().elements();
        while (en.hasMoreElements()) {
            YassRow r = en.nextElement();

            if (r.isNote()) {
                r.setBeat(r.getBeatInt() / 2);
                r.setLength(Math.max(1, r.getLengthInt() / 2));
            }
            if (r.isPageBreak()) {
                r.setBeat(r.getBeatInt() / 2);
                if (r.hasSecondBeat()) {
                    r.setSecondBeat(r.getSecondBeatInt() / 2);
                }
            }
        }
        setBPM(getBPM() / 2.0);
        tm.fireTableDataChanged();
        if (sel >= 0) {
            setRowSelectionInterval(sel, sel);
            zoomPage();
        }
    }

    /**
     * Description of the Method
     */
    public void pasteRows() {
        int startRow = getSelectionModel().getMinSelectionIndex();
        if (startRow < 0) {
            return;
        }

        YassRow r = getRowAt(startRow);
        if (!r.isNote()) {
            return;
        }
        int startBeat = r.getBeatInt();
        try {
            Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
            String trstring = (String) (system.getContents(this)
                    .getTransferData(DataFlavor.stringFlavor));
            StringTokenizer st1 = new StringTokenizer(trstring, "\n");
            int i;
            int n = getRowCount();
            int pasteBeat = -1;
            for (i = 0; st1.hasMoreTokens() && (startRow + i < n); i++) {
                StringTokenizer st2 = new StringTokenizer(st1.nextToken(), "\t");
                String type = st2.hasMoreTokens() ? st2.nextToken() : "";
                String beat = st2.hasMoreTokens() ? st2.nextToken() : "";
                if (pasteBeat == -1) {
                    pasteBeat = Integer.parseInt(beat);
                }
                String length = st2.hasMoreTokens() ? st2.nextToken() : "";
                String height = st2.hasMoreTokens() ? st2.nextToken() : "";

                boolean isSep = type.equals("-");
                if (isSep) {
                    continue;
                }

                int beatInt = Integer.parseInt(beat);
                YassRow r2 = getRowAt(startRow + i);
                if (r2.isNote()) {
                    r2.setBeat(startBeat + beatInt - pasteBeat);
                    r2.setLength(length);
                    r2.setHeight(height);
                }
            }
            addUndo();
            tm.fireTableRowsUpdated(startRow, Math.min(startRow + i, n - 1));
        } catch (Exception ex) {
        }
    }

    /**
     * Description of the Method
     */
    public void insertNote() {
        int n = getRowCount();
        int row = getSelectionModel().getMinSelectionIndex();
        if (row < 0) {
            row = sheet != null ? sheet.nextElement() : 0;
            if (row < 0) {
                row = n - 1;
            }
            if (row > 0) {
                row--;
            }
        }
        if (getRowAt(row).isComment()) {
            return;
        }

        int pnote = row;
        YassRow prevNote = getRowAt(pnote);
        while (!prevNote.isNote() && pnote > 0) {
            prevNote = getRowAt(--pnote);
        }
        if (!prevNote.isNote()) {
            prevNote = null;
        }

        int nnote = Math.min(row + 1, n - 1);
        YassRow nextNote = getRowAt(nnote);
        while (!nextNote.isNote() && nnote < n - 1) {
            nextNote = getRowAt(++nnote);
        }
        if (!nextNote.isNote()) {
            nextNote = null;
        }

        int pbreak = row;
        YassRow prevBreak = getRowAt(pbreak);
        while (!prevBreak.isPageBreak() && pbreak > 0) {
            prevBreak = getRowAt(--pbreak);
        }
        if (!prevBreak.isPageBreak()) {
            prevBreak = null;
        }

        int nbreak = row;
        YassRow nextBreak = getRowAt(nbreak);
        while (!nextBreak.isPageBreak() && nbreak < n - 1) {
            nextBreak = getRowAt(++nbreak);
        }
        if (!nextBreak.isPageBreak()) {
            nextBreak = null;
        }

        int beat = 0;
        if (prevNote != null) {
            beat = prevNote.getBeatInt() + prevNote.getLengthInt();
        }
        if (prevBreak != null) {
            beat = Math.max(beat, prevBreak.getSecondBeatInt());
        }

        int length = 4;
        if (nextNote != null) {
            length = Math.min(length, nextNote.getBeatInt() - beat);
        }
        if (nextBreak != null) {
            length = Math.min(length, nextBreak.getBeatInt() - beat);
        }
        if (length == 0) {
            return;
        }

        // preserve spacing
		/*
		 * if (length > 1) { beat++; length--; }
		 */
        if (length > 1) {
            length--;
        }

        int height = 0;
        if (prevNote != null) {
            height = prevNote.getHeightInt();
        } else if (nextNote != null) {
            height = nextNote.getHeightInt();
        }

        tm.insertRowAt(":", beat + "", length + "", height + "", "~", row + 1);
        addUndo();
        tm.fireTableRowsInserted(row + 1, row + 1);
        setRowSelectionInterval(row + 1, row + 1);
        updatePlayerPosition();
        zoomPage();
    }

    /**
     * Description of the Method
     */
    public void togglePageBreak() {
        int row = getSelectionModel().getMinSelectionIndex() - 1;
        if (row < 0) {
            return;
        }
        YassRow r = getRowAt(row);
        if (r.isNote()) {
            insertPageBreak(true);
            setRowSelectionInterval(row + 2, row + 2);
        } else if (r.isPageBreak()) {
            removePageBreak(true);
            setRowSelectionInterval(row, row);
        }
        if (zoomMode == ZOOM_ONE) {
            zoomPage();
        } else {
            // why update??
            sheet.update();
            sheet.repaint();
        }
    }

    /**
     * Description of the Method
     *
     * @param before Description of the Parameter
     */
    public void insertPageBreak(boolean before) {
        int row = before ? getSelectionModel().getMinSelectionIndex() - 1
                : getSelectionModel().getMaxSelectionIndex();
        if (row < 0) {
            return;
        }
        if (row == 0) {
            before = false;
        }

        insertPageBreakAt(row);
    }

    /**
     * Description of the Method
     *
     * @param row Description of the Parameter
     */
    public void insertPageBreakAt(int row) {
        YassRow r = getRowAt(row);

        if (r.isEnd()) {
            return;
        }
        if (r.isPageBreak()) {
            return;
        }

        int n = getRowCount();

        YassRow next = row < n - 1 ? getRowAt(row + 1) : null;

        if (r.isComment() && next != null && next.isComment()) {
            return;
        }

        if (next != null) {
            if (next.isPageBreak()) {
                return;
            }
            String txt = next.getText();
            if (txt.startsWith(YassRow.SPACE + "")) {
                next.setText(txt.substring(1));
            } else {
                r.setText(r.getText() + "-");
            }
        }

        String beat = "" + (r.getBeatInt() + r.getLengthInt());

        int ij[] = null;
        if (row > 0 && next != null) {
            if (r.isNote() && next.isNote()) {
                ij = new int[2];
                ij[0] = r.getBeatInt() + r.getLengthInt();
                ij[1] = next.getBeatInt();
                YassAutoCorrect.getCommonPageBreak(ij, bpm, null);
            }
        }
        if (ij != null) {
            tm.insertRowAt("-", ij[0] + "", (ij[1] == ij[0]) ? "" : ij[1] + "",
                    "", "", row + 1);
        } else {
            tm.insertRowAt("-", beat, "", "", "", row + 1);
        }

        addUndo();
        tm.fireTableRowsInserted(row + 1, row + 1);
    }

    /**
     * Description of the Method
     *
     * @param before Description of the Parameter
     */
    public void removePageBreak(boolean before) {
        int row = before ? getSelectionModel().getMinSelectionIndex() - 1
                : getSelectionModel().getMaxSelectionIndex() + 1;
        if (row < 0) {
            return;
        }
        if (row == 0) {
            before = false;
        }

        int n = getRowCount();
        if (row >= n - 1) {
            return;
        }

        YassRow r = getRowAt(row);
        if (!r.isPageBreak()) {
            return;
        }

        YassRow next = row < n - 1 ? getRowAt(row + 1) : null;
        YassRow prev = row > 0 ? getRowAt(row - 1) : null;

        if (next != null && next.isNote()) {
            String txt = next.getText();
            if (prev != null && prev.isNote() && prev.getText().endsWith("-")) {
                prev.setText(prev.getText().substring(0,
                        prev.getText().length() - 1));
            } else if (!txt.startsWith(YassRow.SPACE + "")
                    && !txt.startsWith("~")) {
                next.setText(YassRow.SPACE + txt);
            }
        }

        tm.getData().removeElementAt(row);
        addUndo();
        tm.fireTableDataChanged();
    }

    /**
     * Description of the Method
     *
     * @param trstring Description of the Parameter
     * @param startRow Description of the Parameter
     * @param before   Description of the Parameter
     * @return Description of the Return Value
     */
    public int insertRowsAt(String trstring, int startRow, boolean before) {

        YassRow r = getRowAt(startRow);
        boolean isSep = r.isPageBreak();
        if (!(r.isNote() || isSep)) {
            return 0;
        }
        int startBeat = r.getBeatInt();
        int num = 0;
        try {
            StringTokenizer st1 = new StringTokenizer(trstring, "\n");
            int i = before ? 0 : 1;
            int pasteBeat = -1;
            for (; st1.hasMoreTokens(); i++) {
                StringTokenizer st2 = new StringTokenizer(st1.nextToken(), "\t");
                String type = st2.hasMoreTokens() ? st2.nextToken() : "";
                String beat = st2.hasMoreTokens() ? st2.nextToken() : "";
                if (pasteBeat == -1) {
                    pasteBeat = Integer.parseInt(beat);
                }
                String length = st2.hasMoreTokens() ? st2.nextToken() : "";
                String height = st2.hasMoreTokens() ? st2.nextToken() : "";
                String txt = st2.hasMoreTokens() ? st2.nextToken() : "";
                txt = txt.replace(' ', YassRow.SPACE);
                int beatInt = Integer.parseInt(beat);
                isSep = type.equals("-");
                if (isSep && length.length() > 0) {
                    int lengthInt = Integer.parseInt(length);
                    length = (lengthInt + startBeat) + "";
                }
                tm.insertRowAt(type, (startBeat + beatInt - pasteBeat) + "",
                        length + "", height + "", txt, startRow + i);
                num++;
            }
            addUndo();
            tm.fireTableRowsInserted(before ? startRow : startRow + 1, startRow
                    + i - 1);
        } catch (Exception ex) {
        }
        return num;
    }

    /**
     * Description of the Method
     *
     * @param before Description of the Parameter
     * @return Description of the Return Value
     */
    public int insertRows(boolean before) {
        int startRow = before ? getSelectionModel().getMinSelectionIndex()
                : getSelectionModel().getMaxSelectionIndex();
        if (startRow < 0) {
            return 0;
        }
        if (startRow == 0) {
            before = false;
        }

        String trstring = null;
        try {
            Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
            trstring = (String) (system.getContents(this)
                    .getTransferData(DataFlavor.stringFlavor));
        } catch (Exception e) {
            return 0;
        }
        ;

        return insertRowsAt(trstring, startRow, before);
    }

    /**
     * Description of the Method
     */
    public void joinRows() {
        int rows[] = getSelectedRows();
        if (rows == null || rows.length < 1) {
            return;
        }

        int sel = -1;
        Arrays.sort(rows);
        int n = getRowCount();
        int end = rows.length > 1 ? rows.length - 2 : rows.length - 1;
        for (int i = end; i >= 0; i--) {
            int row = rows[i];

            YassRow r = getRowAt(row);
            if (!r.isNote()) {
                continue;
            }

            String txt = r.getText();

            String ntxt = null;

            YassRow nr = null;
            if (row < n - 1) {
                nr = getRowAt(row + 1);
                if (nr.isNote()) {
                    ntxt = nr.getText();
                }
            }

			/*
			 * if (txt.equals("~") && ptxt!=null) {
			 * pr.setLength(r.getBeatInt()-pr.getBeatInt()+r.getLengthInt());
			 * tm.removeRowAt(row); sel = row-1; }
			 */
            if (ntxt != null && ntxt.equals("~")) {
                r.setLength(nr.getBeatInt() - r.getBeatInt()
                        + nr.getLengthInt());
                tm.removeRowAt(row + 1);
                sel = row;
            } else if (ntxt != null && !txt.endsWith(YassRow.SPACE + "")
                    && !ntxt.startsWith(YassRow.SPACE + "")) {
                if (txt.equals("~")) {
                    txt = "";
                }
                if (txt.endsWith("~")) {
                    txt = txt.substring(0, txt.length() - 1);
                }
                if (ntxt.startsWith("~")) {
                    ntxt = ntxt.substring(1);
                }
                r.setText(txt + ntxt);
                r.setLength(nr.getBeatInt() - r.getBeatInt()
                        + nr.getLengthInt());
                tm.removeRowAt(row + 1);
                sel = row;
            } else if (ntxt != null) {
                if (txt.equals("~")) {
                    txt = "";
                }
                r.setText(txt + ntxt);
                r.setLength(nr.getBeatInt() - r.getBeatInt()
                        + nr.getLengthInt());
                tm.removeRowAt(row + 1);
                sel = row;
            }
        }
        addUndo();

        tm.fireTableDataChanged();
        if (sel >= 0) {
            setRowSelectionInterval(sel, sel);
            updatePlayerPosition();
        }
    }

    /**
     * Description of the Method
     */
    public void splitRows() {
        int row = getSelectionModel().getMinSelectionIndex();
        if (row < 0) {
            return;
        }
        YassRow r = getRowAt(row);
        int words = 0;
        if (r.isNote()) {
            StringTokenizer st = new StringTokenizer(r.getText(), YassRow.SPACE
                    + "");
            words = st.countTokens();
        }
        double f = 0.5;
        if (words > 1) {
            f = (words - 1) / (double) words;
        }
        split(f);
    }

    /**
     * Description of the Method
     */
    public void removeRows() {
        int n = getRowCount();
        int rows[] = getSelectedRows();
        if (rows == null || rows.length < 1) {
            return;
        }

        int sel = -1;
        Arrays.sort(rows);
        for (int i = rows.length - 1; i >= 0; i--) {
            int row = rows[i];

            YassRow r = getRowAt(row);
            if (!r.isNote()) {
                if (row < n - 1) {
                    YassRow nr = getRowAt(row + 1);
                    if (nr.isNote()) {
                        String ntxt = nr.getText();
                        if (!ntxt.startsWith(YassRow.SPACE + "")) {
                            nr.setText(YassRow.SPACE + ntxt);
                        }
                    }
                }
                tm.removeRowAt(row);
                continue;
            }
            String txt = r.getText();

            String ntxt = null;

            String ptxt = null;
            YassRow nr = null;
            YassRow pr = null;
            if (row < n - 1) {
                nr = getRowAt(row + 1);
                if (nr.isNote()) {
                    ntxt = nr.getText();
                }
            }
            if (row > 0) {
                pr = getRowAt(row - 1);
                if (pr.isNote()) {
                    ptxt = pr.getText();
                }
            }

            if (txt.equals("~")) {
                tm.removeRowAt(row);
                sel = row - 1;
            } else if (ntxt != null && ntxt.equals("~")) {
                nr.setText(txt);
                tm.removeRowAt(row);
                sel = row;
            } else if (ntxt != null && !txt.endsWith(YassRow.SPACE + "")
                    && !ntxt.startsWith(YassRow.SPACE + "")) {
                nr.setText(txt + ntxt);
                tm.removeRowAt(row);
                sel = row;
            } else if (ptxt != null && !ptxt.endsWith(YassRow.SPACE + "")
                    && !txt.startsWith(YassRow.SPACE + "")) {
                pr.setText(ptxt + txt);
                tm.removeRowAt(row);
                sel = row - 1;
            } else if (ntxt != null) {
                nr.setText(txt + ntxt);
                tm.removeRowAt(row);
                sel = row;
            } else if (ptxt != null) {
                pr.setText(ptxt + txt);
                tm.removeRowAt(row);
                sel = row - 1;
            }
        }
        addUndo();

        tm.fireTableDataChanged();
        if (sel >= 0) {
            setRowSelectionInterval(sel, sel);
            updatePlayerPosition();
        }
    }

    /**
     * Description of the Method
     */
    public void rollRight() {
        rollRight('$', 0);
        // shift from start
    }

    /**
     * Description of the Method
     *
     * @param splitCode Description of the Parameter
     * @param pos       Description of the Parameter
     */
    public void rollRight(char splitCode, int pos) {
        int n = getRowCount();
        if (n < 1) {
            return;
        }
        int row = getSelectionModel().getMinSelectionIndex();
        if (row < 0) {
            return;
        }

        YassRow r = getRowAt(row);
        String txt = r.getText();
        String init = "~";
        if (pos > 0 && pos < txt.length()) {
            if (splitCode == ' ') {
                splitCode = YassRow.SPACE;
            }
            init = txt.substring(0, pos) + splitCode + txt.substring(pos);
        } else {
            init = txt;
        }

        int i = n - 1;
        r = getRowAt(i);
        while (i > 0 && !r.isNote()) {
            r = getRowAt(--i);
        }
        if (i < 1) {
            return;
        }
        txt = r.getText().trim();
        String lastTXT = null;
        if (txt.length() >= 1 && !txt.equals("~") && !txt.equals("-")) {
            lastTXT = txt;
        }

        YassRow next = null;

        YassRow prev = null;
        while (i > row) {
            prev = getRowAt(--i);
            boolean isFirst = prev.isPageBreak();
            while (i > row && !prev.isNote()) {
                prev = getRowAt(--i);
            }
            if (i >= row) {
                YassRow pprev = i > 0 ? getRowAt(i - 1) : null;
                boolean prevIsFirst = pprev != null && pprev.isPageBreak();

                txt = prev.getText();
                if (i == row) {
                    if (init != null) {
                        char c[] = init.toCharArray();
                        int k = c.length - 1;
                        while (k > 0 && c[k] == YassRow.SPACE) {
                            k--;
                        }

                        while (k > 0 && c[k] != YassRow.SPACE && c[k] != '-') {
                            k--;
                        }
                        if (k > 0) {
                            YassRow prevnext = row < n - 1 ? getRowAt(row + 1)
                                    : null;
                            if (prevnext != null && prevnext.isPageBreak()) {
                                if (c[k] == YassRow.SPACE) {
                                    prev.setText(init.substring(0, k));
                                    r.setText(init.substring(k + 1));
                                } else {
                                    prev.setText(init.substring(0, k) + "-");
                                    r.setText(init.substring(k + 1));
                                }
                            } else {
                                prev.setText(init.substring(0, k));
                                if (c[k] == YassRow.SPACE) {
                                    r.setText(init.substring(k));
                                } else {
                                    r.setText(init.substring(k + 1));
                                }
                            }
                        } else {
                            prev.setText("~");
                            r.setText(txt);
                        }
                    } else {
                        prev.setText("~");
                        r.setText(txt);
                    }
                    break;
                }

                if (prevIsFirst && !txt.startsWith(YassRow.SPACE + "")
                        && !txt.startsWith("~") && !txt.equals("-")) {
                    txt = YassRow.SPACE + txt;
                }

                if (isFirst) {
                    if (txt.startsWith(YassRow.SPACE + "")) {
                        txt = txt.substring(1);
                    } else {
                        if (pprev != null && pprev.isNote()) {
                            String ppText = pprev.getText();
                            // in v1.0.1:
                            // if (!pprev.getText().equals("~") &&
                            // !pprev.getText().equals("-")) {
                            if (!pprev.getText().equals("-")) {
                                pprev.setText(ppText + "-");
                            }
                        }
                    }
                    if (txt.endsWith("-")) {
                        txt = txt.substring(0, txt.length() - 1);
                        if (next != null) {
                            String ntxt = next.getText();
                            if (ntxt.startsWith(YassRow.SPACE + "")) {
                                next.setText(ntxt.substring(1));
                            }
                        }
                    }
                }

                if (lastTXT != null) {
                    if (!lastTXT.startsWith(YassRow.SPACE + "")) {
                        lastTXT = "-" + lastTXT;
                    }
                    r.setText(txt + lastTXT);
                    lastTXT = null;
                } else {
                    r.setText(txt);
                }

                next = r;
                r = prev;
            }
        }
        addUndo();

        tm.fireTableDataChanged();
        setRowSelectionInterval(row, row);
        updatePlayerPosition();
    }

    /**
     * Description of the Method
     */
    public void rollLeft() {
        int n = getRowCount();
        if (n < 1) {
            return;
        }
        int row = getSelectionModel().getMinSelectionIndex();
        if (row < 0) {
            return;
        }

        int i = n - 1;
        YassRow r = getRowAt(i);
        while (i > 0 && !r.isNote()) {
            r = getRowAt(--i);
        }
        if (i < 1) {
            return;
        }
        // index of last note
        int lastIndex = i;

        // start at current row
        i = row;
        r = getRowAt(i);
        if (!r.isNote()) {
            return;
        }
        String txt = r.getText();
        String init = null;
        if (txt.length() >= 1 && !txt.equals("~") && !txt.equals("-")) {
            init = txt;
        }

        YassRow prev = r;

        YassRow next = null;
        while (i < lastIndex) {
            boolean isFirst = i == 0 || getRowAt(i - 1).isPageBreak();

            next = getRowAt(++i);
            boolean isLast = next.isPageBreak();
            while (i < n - 1 && !next.isNote()) {
                next = getRowAt(++i);
            }
            txt = next.getText();

            if (i == lastIndex) {
                char c[] = txt.toCharArray();
                int k = 0;
                while (k < c.length && c[k] == YassRow.SPACE) {
                    k++;
                }
                while (k < c.length && c[k] != YassRow.SPACE && c[k] != '-') {
                    k++;
                }
                if (k < c.length) {
                    r.setText(txt.substring(0, k));
                    if (c[k] == YassRow.SPACE) {
                        next.setText(txt.substring(k));
                    } else {
                        next.setText(txt.substring(k + 1));
                    }
                } else {
                    r.setText(txt);
                    next.setText("~");
                }
            } else {
                if (isLast) {
                    if (prev != null) {
                        String ptxt = prev.getText();
                        if (ptxt.endsWith("-")
                                || ptxt.endsWith(YassRow.HYPHEN + "")) {
                            prev.setText(ptxt.substring(0, ptxt.length() - 1));
                        } else if (!txt.startsWith("~")) {
                            txt = YassRow.SPACE + txt;
                        }
                    } else if (!txt.startsWith("~")) {
                        txt = YassRow.SPACE + txt;
                    }
                }

                if (init != null) {
                    if (!txt.startsWith(YassRow.SPACE + "")
                            && !init.endsWith("-")) {
                        init = init + "-";
                    }
                    if (init.endsWith("-")
                            || init.endsWith(YassRow.HYPHEN + "")) {
                        init = init.substring(0, init.length() - 1);
                    }
                    r.setText(init + txt);
                    init = null;
                } else {
                    if (isFirst) {
                        if (txt.startsWith(YassRow.SPACE + "")) {
                            txt = txt.substring(1);
                        } else {
                            if (prev != null) {
                                String ptxt = prev.getText();
                                if (!ptxt.endsWith("~") && !ptxt.endsWith("-")) {
                                    prev.setText(ptxt + "-");
                                }
                            }
                        }
                    }
                    r.setText(txt);
                }

                prev = r;
                r = next;
            }
        }
        addUndo();

        tm.fireTableDataChanged();
        setRowSelectionInterval(row, row);
        updatePlayerPosition();
    }

    /**
     * Description of the Method
     */
    public void copyRows() {
        String s = getSelectedRowsAsString();
        StringSelection stsel = new StringSelection(s);
        Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
        system.setContents(stsel, stsel);
    }

    /**
     * Gets the rowCopy attribute of the YassTable object
     *
     * @return The rowCopy value
     */
    public String getSelectedRowsAsString() {
        StringBuffer sbf = new StringBuffer();
        int rows[] = getSelectedRows();
        if (rows == null || rows.length < 1) {
            return "";
        }
        Arrays.sort(rows);
        String txt = null;
        for (int row : rows) {
            sbf.append((String) getValueAt(row, 0));
            sbf.append("\t");
            sbf.append((String) getValueAt(row, 1));
            sbf.append("\t");
            sbf.append((String) getValueAt(row, 2));
            sbf.append("\t");
            sbf.append((String) getValueAt(row, 3));
            sbf.append("\t");

            txt = (String) getValueAt(row, 4);
            txt = txt.replace(YassRow.SPACE, ' ');
            sbf.append(txt);
            sbf.append("\n");
        }
        return sbf.toString();
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public String toString() {
        StringBuffer sbf = new StringBuffer();

        Enumeration<?> en = tm.getData().elements();
        while (en.hasMoreElements()) {
            YassRow r = (YassRow) en.nextElement();
            sbf.append(r.toString());
            sbf.append("\n");
        }
        return sbf.toString();
    }

    /**
     * Description of the Method
     */
    public void cutRows() {
        copyRows();
        removeRows();
    }

    /**
     * Description of the Method
     *
     * @param percent Description of the Parameter
     */
    public void split(double percent) {
        int row = getSelectionModel().getMinSelectionIndex();
        if (row < 0) {
            return;
        }
        YassRow r = getRowAt(row);
        if (!r.isNote()) {
            return;
        }

        int w = r.getLengthInt();
        if (w < 2) {
            return;
        }

        int w1 = (int) Math.round(w * percent);
        int w2 = w - w1;

        r.setLength(w1);
        YassRow r2 = r.clone();
        r2.setBeat(r.getBeatInt() + w1);
        r2.setLength(w2);
        String txt = r.getText();
        char c[] = txt.toCharArray();
        int i = c.length - 1;
        while (i > 0 && c[i] == YassRow.SPACE) {
            i--;
        }
        while (i > 0 && !isSpaceOrPunctuation(c[i])) {
            i--;
        }
        if (i > 0) {
            r.setText(txt.substring(0, i));

            String remainder = txt.substring(i);
            if (remainder.length() == 1
                    && isSpaceOrPunctuation(remainder.charAt(0)))
                remainder = "~" + remainder;
            r2.setText(remainder);
        } else {
            r2.setText("~");
        }

        tm.getData().insertElementAt(r2, row + 1);
        addUndo();

        tm.fireTableDataChanged();
        setRowSelectionInterval(row, row + 1);
        updatePlayerPosition();
    }

    public boolean isSpaceOrPunctuation(char c) {
        return c == YassRow.SPACE || c == ',' || c == '!' || c == '?'
                || c == ':' || c == ';' || c == '.';
    }

    /**
     * Description of the Method
     */
    public void joinLeft() {
        int row = getSelectionModel().getMinSelectionIndex();

        if (row < 1) {
            return;
        }
        YassRow r2 = getRowAt(row);
        if (!r2.isNote()) {
            return;
        }

        YassRow r = getRowAt(row - 1);
        if (!r.isNote()) {
            return;
        }

        int w2 = r2.getBeatInt() - r.getBeatInt() + r2.getLengthInt();
        r.setLength(w2);

        r.setHeight(r2.getHeightInt());

        String txt = r2.getText();
        if (txt.equals("~")) {
            txt = "";
        }
        r.setText(r.getText() + txt);

        tm.getData().removeElementAt(row);
        addUndo();
        tm.fireTableDataChanged();
    }

    /**
     * Description of the Method
     */
    public void joinRight() {
        int row = getSelectionModel().getMinSelectionIndex();
        int n = getRowCount();

        if (row < 0 || row > n - 2) {
            return;
        }
        YassRow r = getRowAt(row);
        if (!r.isNote()) {
            return;
        }

        YassRow r2 = getRowAt(row + 1);
        if (!r2.isNote()) {
            return;
        }

        int w2 = r2.getBeatInt() - r.getBeatInt() + r2.getLengthInt();
        r.setLength(w2);

        String txt = r2.getText();
        if (txt.equals("~")) {
            txt = "";
        }
        r.setText(r.getText() + txt);

        tm.getData().removeElementAt(row + 1);
        addUndo();
        tm.fireTableDataChanged();
        setRowSelectionInterval(row, row);
    }

    /**
     * Gets the text attribute of the YassTable object
     *
     * @return The text value
     */
    public String getText() {
        int n = getRowCount();
        StringBuffer sbf = new StringBuffer();
        for (int i = 0; i < n; i++) {
            YassRow r = getRowAt(i);
            if (r.isNote()) {
                String txt = r.getText();
                txt = txt.replace('-', YassRow.HYPHEN);

                boolean spacy = YassRow.trim(txt).length() == 0;
                if (spacy) {
                    // text is all-space --> author meant a space
                    txt = " " + YassRow.SPACE;
                    sbf.append(txt);
                } else {
                    if (!txt.startsWith(YassRow.SPACE + "")) {
                        if (i > 0) {
                            YassRow r2 = getRowAt(i - 1);
                            if ((r2.isNote())) {
                                String txt2 = r2.getText();
                                if (txt2.length() == 0
                                        || !txt2.endsWith(YassRow.SPACE + "")) {
                                    txt = "-" + txt;
                                } else {
                                    txt = YassRow.SPACE + txt;
                                }
                            }
                        }
                    }
                    int tn = txt.length();
                    if (tn > 0) {
                        if (tn > 1 && txt.charAt(0) == YassRow.SPACE) {
                            txt = " " + txt.substring(1);
                        }
                        if (tn > 1 && txt.charAt(tn - 1) == YassRow.SPACE) {
                            txt = txt.substring(0, tn - 1);
                        }
                        sbf.append(txt);
                    }
                }
            }
            if (r.isPageBreak()) {
                sbf.append("\n");
            }
        }
        return sbf.toString();
    }

    // copied from storeFile

    /**
     * Gets the plainText attribute of the YassTable object
     *
     * @return The plainText value
     */
    public String getPlainText() {
        int relPageBreak = 0;
        boolean isRel = isRelative();

        StringWriter buffer = new StringWriter();
        PrintWriter outputStream = new PrintWriter(buffer);

        int rows = tm.getRowCount();
        String s;
        for (int i = 0; i < rows; i++) {
            YassRow r = tm.getRowAt(i);
            if (isRel) {
                int revert = -1;
                if (r.isPageBreak() && i + 1 < rows) {
                    // set 2nd beat to next note
                    YassRow r2 = tm.getRowAt(i + 1);
                    if (r2.isNote()) {
                        revert = r.getSecondBeatInt();
                        r.setSecondBeat(r2.getBeatInt());
                    }
                }
                if (r.isNote() || r.isPageBreak()) {
                    s = r.toString(relPageBreak);
                    if (r.isPageBreak()) {
                        relPageBreak = r.getSecondBeatInt();
                    }
                } else {
                    s = r.toString();
                }
                if (revert >= 0) {
                    r.setSecondBeat(revert);
                }
            } else {
                s = r.toString();
            }

            s = s.replace(YassRow.SPACE, ' ');
            outputStream.println(s);
            // System.out.println(tm.getRowAt(i).toString());
        }
        return buffer.toString();
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean hasLyrics() {
        String txt = getText();
        txt = txt.replace('-', ' ');
        txt = txt.replace('~', ' ');
        txt = txt.replace('\n', ' ');
        txt = txt.replace('\r', ' ');
        txt = txt.replace(YassRow.HYPHEN, ' ');
        txt = txt.trim();
        // System.out.println("###hasLyrics: \n" + txt + "\n###");
        return txt.length() > 0;
    }

    /**
     * Gets the syllables attribute of the YassTable object
     *
     * @param txt Description of the Parameter
     * @return The syllables value
     */
    public Vector<String> getSyllables(String txt) {
        Vector<String> h = new Vector<>();
        StringTokenizer st = new StringTokenizer(txt, "\n");
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            StringTokenizer st2 = new StringTokenizer(line, " ");
            boolean first = true;
            while (st2.hasMoreTokens()) {
                String word = st2.nextToken();
                if (first) {
                    first = false;
                } else {
                    word = YassRow.SPACE + word;
                }

                StringTokenizer st3 = new StringTokenizer(word, "-", true);
                boolean last = false;
                boolean delim = false;
                while (st3.hasMoreTokens()) {
                    String syll = st3.nextToken();
                    last = delim;
                    delim = syll.equals("-");
                    if (delim && last) {
                        h.addElement("");
                    } else if (!delim) {
                        h.addElement(syll);
                    }
                }
            }
            if (st.hasMoreTokens()) {
                h.addElement("\n");
            }
        }
        return h;
    }

    /**
     * Gets the noteCount attribute of the YassTable object
     *
     * @return The noteCount value
     */
    public int getNoteOrPageBreakCount() {
        int i = 0;
        int notes = 0;
        int n = getRowCount() - 1;
        while (i <= n) {
            YassRow r = getRowAt(i);
            if (r.isNote() || r.isPageBreak()) {
                notes++;
            }
            i++;
        }
        return notes;
    }

    /**
     * Gets the noteCount attribute of the YassTable object
     *
     * @return The noteCount value
     */
    public int getNoteCount() {
        int i = 0;
        int notes = 0;
        int n = getRowCount() - 1;
        while (i <= n) {
            YassRow r = getRowAt(i);
            if (r.isNote()) {
                notes++;
            }
            i++;
        }
        return notes;
    }

    public String getPageMessage(int line) {
        int pageIndex = getPage(line);
        if (pageIndex >= 0) {
            int[] ij = enlargeToPages(pageIndex, pageIndex);
            for (int k = ij[0]; k <= ij[1] /* include page break */+ 1; k++) {
                YassRow row = getRowAt(k);
                if (row.hasMessage()) return row.getMessage();
            }
        }
        return null;
    }

    /**
     * Description of the Method
     *
     * @param txt Description of the Parameter
     * @return Description of the Return Value
     */
    public int applyLyrics(String txt) {
        int i = 0;
        int notes = getNoteOrPageBreakCount();

        Vector<String> hyph = getSyllables(txt);
        int syllables = hyph.size();

        int mismatch = syllables - notes;

        // spread syllables
        txt = "";
        int k = 0;
        boolean changed = false;
        Vector<?> data = tm.getData();
        int n = data.size();
        for (i = 0; i < n; i++) {
            YassRow r = getRowAt(i);
            if (r.isNoteOrPageBreak()) {
                if (k < syllables) {
                    txt = hyph.elementAt(k++);
                } else {
                    txt = " -";
                }
                if (txt.equals("\n")) {
                    if (k < syllables) {
                        txt = hyph.elementAt(k);
                        if (txt.equals("\n")) {
                            while (k < syllables && txt.equals("\n")) {
                                txt = hyph.elementAt(k++);
                            }
                        }
                    }
                    if (!r.isPageBreak()) {
                        insertPageBreakAt(i - 1);
                        // i++;
                        changed = true;
                        continue;
                    } else {
                        continue;
                    }
                }
                txt = txt.replace(YassRow.HYPHEN, '-');

                if (r.isNote() && !r.getText().equals(txt)) {
                    r.setText(txt);
                    changed = true;
                } else if (r.isPageBreak()) {
                    tm.getData().removeElementAt(i);
                    i--;
                    k--;
                    changed = true;
                }
            }
        }
        if (changed) {
            tm.fireTableDataChanged();
        }

        return mismatch;
    }

    /**
     * Gets the selection attribute of the YassTable object
     *
     * @param i                 Description of the Parameter
     * @param j                 Description of the Parameter
     * @param inout             Description of the Parameter
     * @param clicks            Description of the Parameter
     * @param includePageBreaks Description of the Parameter
     * @return The selection value
     */
    public long[][] getSelection(int i, int j, long inout[], long clicks[][],
                                 boolean includePageBreaks) {
        int n = getRowCount();
        boolean all = i < 0 && j < 0;
        if (i < 0) {
            inout[0] = 0;
            i = 0;
        }
        if (j < 0) {
            inout[1] = -1;
            j = n - 1;
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int beat;
        int end;
        int clickCount = 0;
        if (clicks == null) {
            if (includePageBreaks) {
                clicks = new long[j - i + 1][3];
            } else {
                int nn = 0;
                for (int k = i; k <= j; k++) {
                    YassRow r = getRowAt(k);
                    if (r.isNote()) {
                        nn++;
                    }
                }
                clicks = new long[nn][3];
            }
        }

        for (int k = i; k <= j; k++) {
            YassRow r = getRowAt(k);
            if (r.isComment() || r.isEnd()) {
                continue;
            }
            if (r.isPageBreak()) {
                if (includePageBreaks) {
                    beat = r.getBeatInt();
                    end = r.hasSecondBeat() ? r.getSecondBeatInt() : beat;
                    clicks[clickCount][0] = beat;
                    clicks[clickCount][1] = 100;
                    clicks[clickCount][2] = end;
                    clickCount++;
                    min = Math.min(min, beat);
                    max = Math.max(max, end);
                }
            } else if (r.isNote()) {
                beat = r.getBeatInt();
                end = beat + r.getLengthInt();
                clicks[clickCount][0] = beat;
                clicks[clickCount][1] = r.getHeightInt();
                clicks[clickCount][2] = end;
                clickCount++;
                min = Math.min(min, beat);
                max = Math.max(max, end);
            }
        }
        if (!all && min == Integer.MAX_VALUE) {
            return null;
        }

        if (!all) {
            inout[0] = (long) ((60 * min / (4.0 * bpm) + gap / 1000.0) * 1000000L);
            inout[1] = (long) ((60 * max / (4.0 * bpm) + gap / 1000.0) * 1000000L);
        }
        for (i = 0; i < clickCount; i++) {
            clicks[i][0] = (long) ((60 * clicks[i][0] / (4.0 * bpm) + gap / 1000.0) * 1000000L);
            clicks[i][2] = (long) ((60 * clicks[i][2] / (4.0 * bpm) + gap / 1000.0) * 1000000L);
        }
        return clicks;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public YassSession createSession() {
        Vector<YassTrack> tracks = new Vector<>();
        Vector<YassNote> notes = new Vector<>();
        Vector<YassLine> lines = new Vector<>();
        YassLine line = null;

        int firstNoteIndex = -1;
        int lastNoteIndex = -1;
        int lineStartBeat = 0;
        int noteIndex = 0;
        boolean first = true;
        boolean lineFreestyle = true;
        int lineMin = 1000;
        int lineMax = -1000;

        double trackBPM = getBPM();
        int gapMillis = (int) getGap();
        long startMillis = (long) (getStart() * 1000);
        long endMillis = (long) (getEnd() * 1000);
        long lineMillis = 0;

        for (Enumeration<?> en = tm.getData().elements(); en.hasMoreElements(); ) {
            YassRow r = (YassRow) en.nextElement();

            if (r.isNote()) {
                int type = YassNote.NORMAL;
                if (r.isGolden()) {
                    type = YassNote.GOLDEN;
                    lineFreestyle = false;
                } else if (r.isFreeStyle()) {
                    type = YassNote.FREESTYLE;
                } else {
                    type = YassNote.NORMAL;
                    lineFreestyle = false;
                }
                int beat = r.getBeatInt();
                int length = r.getLengthInt();
                int height = r.getHeightInt();
                lineMin = Math.min(lineMin, height);
                lineMax = Math.max(lineMax, height);
                String txt = r.getText();
                txt = txt.replace(YassRow.SPACE, ' ');

                long noteStartMillis = (long) ((60 * beat / (4.0 * bpm) + gap / 1000.0) * 1000);
                long noteEndMillis = (long) ((60 * (beat + length)
                        / (4.0 * bpm) + gap / 1000.0) * 1000);
                if (!r.isFreeStyle()) {
                    lineMillis += noteEndMillis - noteStartMillis;
                }

                YassNote note = new YassNote(type, beat, length, height, txt,
                        noteStartMillis, noteEndMillis);
                notes.addElement(note);

                if (first) {
                    firstNoteIndex = noteIndex;
                    first = false;
                }
                lastNoteIndex = noteIndex;
                noteIndex++;
            }
            if (r.isPageBreak() || r.isEnd()) {
                int lineEndBeat = r.getBeatInt();
                long lineStartMillis = (int) ((60 * lineStartBeat / (4.0 * bpm) + gap / 1000.0) * 1000);
                long lineEndMillis = (int) ((60 * lineEndBeat / (4.0 * bpm) + gap / 1000.0) * 1000);

                line = new YassLine(firstNoteIndex, lastNoteIndex, lineMin,
                        lineMax, lineStartMillis, lineEndMillis, lineMillis,
                        lineFreestyle);
                lines.addElement(line);
                lineStartBeat = r.getSecondBeatInt();
                lineMin = 1000;
                lineMax = -1000;
                lineMillis = 0;
                lineFreestyle = true;
                first = true;
            }
        }

        YassNote[] notesArray = notes.toArray(new YassNote[]{});
        YassLine[] linesArray = lines.toArray(new YassLine[]{});
        YassTrack track = new YassTrack(notesArray, linesArray, trackBPM,
                gapMillis);
        tracks.addElement(track);
        YassTrack[] tracksArray = tracks
                .toArray(new YassTrack[]{});
        tracksArray[0].setDifficulty(Integer.parseInt(prop
                .getProperty("player1_difficulty")));

        String[] ratings = new String[9];
        for (int i = 0; i < 9; i++) {
            ratings[i] = I18.get("session_rating_" + i);
        }

        YassSession session = new YassSession(getArtist(), getTitle(),
                tracksArray, startMillis, endMillis, ratings);
        int noteScore = Integer.parseInt(prop.getProperty("max-points"));
        int goldenScore = Integer.parseInt(prop.getProperty("max-golden"));
        int lineScore = Integer.parseInt(prop.getProperty("max-linebonus"));
        session.initScore(noteScore, goldenScore, lineScore);
        return session;
    }

    /**
     * Description of the Method
     */
    public void sortRows() {
        java.util.Collections.sort(tm.getData());
    }

    /**
     * Gets the scrollableTracksViewportHeight attribute of the YassTable object
     *
     * @return The scrollableTracksViewportHeight value
     */
    public boolean getScrollableTracksViewportHeight() {
        return getPreferredSize().height <= getParent().getHeight();
    }

    // "equals" is used by dnd & awt, can't overwrite it here

    /**
     * Description of the Method
     *
     * @param t Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean equalsData(YassTable t) {
        return tm.equalsData(t.tm);
    }

    // EDITORS

    public void setGoldenPoints(int goldenPoints, int idealGoldenPoints,
                                int goldenVariance, int durationGolden, int idealGoldenBeats,
                                String diff) {
        this.goldenPoints = goldenPoints;
        this.idealGoldenPoints = idealGoldenPoints;
        this.goldenVariance = goldenVariance;
        this.durationGolden = durationGolden;
        this.idealGoldenBeats = idealGoldenBeats;
        this.goldenDiff = diff;

    }

    public int getGoldenPoints() {
        return goldenPoints;
    }

    public int getIdealGoldenPoints() {
        return idealGoldenPoints;
    }

    public int getGoldenVariance() {
        return goldenVariance;
    }

    public int getDurationGolden() {
        return durationGolden;
    }

    public int getIdealGoldenBeats() {
        return idealGoldenBeats;
    }

    public String getGoldenDiff() {
        return goldenDiff;
    }

    public void setCurrentLineTo(int sourceLine) {
        int rows[] = getSelectedRows();
        if (rows == null || rows.length != 1) {
            return;
        }

        int i = rows[0];
        if (i <= 0)
            return;

        if (!getRowAt(i).isNote())
            return;

        int firstTargetIndex = i;
        YassRow firstTargetNote = getRowAt(firstTargetIndex);
        while (firstTargetNote.isNote())
            firstTargetNote = getRowAt(--firstTargetIndex);
        firstTargetIndex++;

        int lastTargetIndex = i;
        YassRow lastTargetNote = getRowAt(lastTargetIndex);
        while (lastTargetNote.isNote())
            lastTargetNote = getRowAt(++lastTargetIndex);
        lastTargetIndex--;

        // target
        int firstSourceIndex = getPage(sourceLine);
        if (firstSourceIndex <= 0)
            return;

        if (!getRowAt(firstSourceIndex).isNote())
            return;

        int lastSourceIndex = firstSourceIndex;
        YassRow lastSourceNote = getRowAt(lastSourceIndex);
        while (lastSourceNote.isNote())
            lastSourceNote = getRowAt(++lastSourceIndex);
        lastSourceIndex--;

        int firstSourceBeat = getRowAt(firstSourceIndex).getBeatInt();
        int firstTargetBeat = getRowAt(firstTargetIndex).getBeatInt();
        for (i = firstSourceIndex; i <= lastSourceIndex; i++) {
            YassRow sourceRow = getRowAt(i);
            YassRow targetRow = getRowAt(i - firstSourceIndex
                    + firstTargetIndex);
            targetRow.setBeat(sourceRow.getBeatInt() - firstSourceBeat
                    + firstTargetBeat);
            targetRow.setLength(sourceRow.getLength());
            targetRow.setHeight(sourceRow.getHeight());
        }

        addUndo();

        tm.fireTableDataChanged();
        setRowSelectionInterval(rows[0], rows[0] + 1);
        zoomPage();
        updatePlayerPosition();
    }

    public double beatToMs(int beat) {
        return 1000 * 60 * beat / (4 * bpm) + gap;
    }

    /**
     * Description of the Class
     *
     * @author Saruta
     */
    public class YassTableCellEditor extends AbstractCellEditor implements
            TableCellEditor {
        private static final long serialVersionUID = -5422573906886420055L;
        Dimension d = new Dimension(100, 100);
        JComboBox<?> ed = new JComboBox<Object>(new String[]{":", "*", "F", "R", "G"}) {
            private static final long serialVersionUID = 1L;

            public Dimension getPopupSize() {
                return d;
            }
        };
        JLabel c = new JLabel("");

        /**
         * Gets the tableCellEditorComponent attribute of the
         * YassTableCellEditor object
         *
         * @param table      Description of the Parameter
         * @param value      Description of the Parameter
         * @param isSelected Description of the Parameter
         * @param rowIndex   Description of the Parameter
         * @param vColIndex  Description of the Parameter
         * @return The tableCellEditorComponent value
         */
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value, boolean isSelected, int rowIndex, int vColIndex) {
            String v = (String) value;

            if (v.equals("*")) {
                ed.setSelectedIndex(1);
            } else if (v.equals("F")) {
                ed.setSelectedIndex(2);
            } else if (v.equals("R")) {
                ed.setSelectedIndex(3);
            } else if (v.equals("G")) {
                ed.setSelectedIndex(4);
            } else {
                ed.setSelectedIndex(0);
            }
            return ed;
        }

        /**
         * Gets the cellEditorValue attribute of the YassTableCellEditor object
         *
         * @return The cellEditorValue value
         */
        public Object getCellEditorValue() {
            return ed.getSelectedItem();
        }
    }
}
