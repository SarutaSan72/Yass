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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

public class YassSongListTransferHandler extends TransferHandler {
    private static final long serialVersionUID = 100465417509492151L;
    YassSongList lib;
    boolean isLib;
    private int[] rows = null;
    private int addIndex = -1;

    public YassSongListTransferHandler(YassSongList l, boolean isLib) {
        lib = l;
        this.isLib = isLib;
    }

    protected Transferable createTransferable(JComponent c) {
        return new StringSelection(exportString(c));
    }

    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    public boolean importData(JComponent c, Transferable t) {
        /*
		    if (canImportFileList(c, t.getTransferDataFlavors())) {
		    try {
		    Vector fileVector = new Vector();
		    java.util.List fileList = (java.util.List) t.getTransferData(DataFlavor.javaFileListFlavor);
		    Iterator iterator = fileList.iterator();
		    while (iterator.hasNext()) {
		    File file = (File) iterator.next();
		    fileVector.addElement(file);
		    }
		    File[] ff = new File[fileVector.size()];
		    fileVector.copyInto(ff);
		    YassImport.importFiles(lib, lib.getActions().getProperties(), ff);
		    return true;
		    }
		    catch (UnsupportedFlavorException ufe) {
		    }
		    catch (IOException ioe) {
		    }
		    }
		    else
		  */
        if (canImport(c, t.getTransferDataFlavors())) {
            try {
                String str = (String) t.getTransferData(DataFlavor.stringFlavor);
                importString(c, str);
                return true;
            } catch (UnsupportedFlavorException | IOException ignored) {
            }
        }
        return false;
    }

    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        for (DataFlavor flavor : flavors) {
            if (DataFlavor.stringFlavor.equals(flavor)) {
                return true;
            }
            //if (DataFlavor.javaFileListFlavor.equals(flavors[i])) {
            //	return true;
            //}
        }
        return false;
    }

    protected String exportString(JComponent c) {
        YassSongList table = (YassSongList) c;
        YassSongListModel sm = (YassSongListModel) table.getModel();
        rows = table.getSelectedRows();
        if (rows == null || rows.length == 0) {
            rows = null;
            return null;
        }
        Arrays.sort(rows);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        for (int row : rows) {
            YassSong s = sm.getRowAt(row);
            String a = s.getArtist();
            String t = s.getTitle();
            pw.println(a + " : " + t);
        }
        pw.close();
        return sw.toString();
    }

    protected void importString(JComponent c, String str) {
        if (isLib) {
            return;
        }
        if (str == null) {
            return;
        }

        YassSongList target = (YassSongList) c;
        YassSongListModel sm = (YassSongListModel) target.getModel();
        Vector<YassSong> data = sm.getData();

        addIndex = target.getSelectedRow();
        if (addIndex < 0) {
            addIndex = target.getRowCount();
        }

        if (rows != null) {
            Vector<YassSong> d = new Vector<>();
            for (int i = rows.length - 1; i >= 0; i--) {
                d.addElement(data.elementAt(rows[i]));
            }
            for (int i = rows.length - 1; i >= 0; i--) {
                data.remove(rows[i]);
            }
            int addIndex2 = addIndex;
            if (addIndex2 > data.size())
                addIndex2 = data.size();
            for (int i = 0; i < rows.length; ++i) {
                data.insertElementAt(d.elementAt(i), addIndex2);
            }
            sm.fireTableDataChanged();
            target.clearSelection();
            target.addRowSelectionInterval(addIndex2, addIndex2 + rows.length - 1);
            target.refreshCursor();
            lib.repaint();
            rows = null;
            return;
        }

        int lc = 0;
        int addIndex2 = addIndex;
        try {
            StringReader sr = new StringReader(str);
            BufferedReader br = new BufferedReader(sr);
            String l;
            while ((l = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(l, ":");
                String artist = st.hasMoreTokens() ? st.nextToken().trim() : null;
                String title = st.hasMoreTokens() ? st.nextToken().trim() : null;
                if (artist != null && title != null) {
                    YassSong s2 = target.getSong(artist, title);
                    if (s2 != null) {
                        continue;
                    }

                    YassSong s = lib.getSong(artist, title);
                    if (s != null) {
                        data.insertElementAt(s, addIndex2++);
                        s.setLocked(true);
                        lc++;
                    } else {
                        s = new YassSong("", "", "", artist, title);
                        s.setIcon(YassSongList.brokenSong);
                        data.insertElementAt(s, addIndex2++);
                        s.setLocked(true);
                        lc++;
                    }
                }
            }
            br.close();
        } catch (Exception ignored) {
        }

        if (lc > 0) {
            sm.fireTableDataChanged();
            target.addRowSelectionInterval(addIndex, addIndex + lc - 1);
            target.refreshCursor();
            lib.repaint();
        }
    }

    protected void exportDone(JComponent c, Transferable td, int action) {
        if (isLib)
            return;

        boolean remove = action == MOVE;

        YassSongList source = (YassSongList) c;
        if (remove && rows != null) {
            YassSongListModel model = (YassSongListModel) source.getModel();
            Vector<?> data = model.getData();

            for (int i = rows.length - 1; i >= 0; i--) {
                YassSong s = (YassSong) data.remove(rows[i]);
                s.setLocked(false);
            }
            model.fireTableDataChanged();
            lib.repaint();

            int i = rows[0];

            int n = data.size();
            if (i >= n)
                i = n - 1;
            if (i >= 0) {
                source.clearSelection();
                source.addRowSelectionInterval(i, i);
            }
            source.refreshCursor();

        }

        rows = null;
        addIndex = -1;
    }
}

