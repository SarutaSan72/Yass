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
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.*;

public class YassPlayList extends JPanel implements TabChangeListener {
    String listTitle = null;
    private final YassActions actions;
    private final YassProperties prop;
    private final YassSongList list;
    private final YassSongList lib;
    private Vector<YassPlayListModel> playlists = null;
    private String listFilename = null;
    private boolean mustRefresh = false;
    private Action saveAction = null, saveAsAction = null, moveUpAction = null, moveDownAction = null;
    private String txtFile = null;


    /**
     * Constructor for the YassPlayList object
     *
     * @param p       Description of the Parameter
     * @param toolbar Description of the Parameter
     * @param actions Description of the Parameter
     * @param lib     Description of the Parameter
     */
    public YassPlayList(YassActions actions, YassProperties p, JComponent toolbar, YassSongList lib) {
        this.actions = actions;
        this.lib = lib;

        prop = p;

        actions.setPlayList(this);

        setOpaque(false);

        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);

        list = new YassSongList(actions);
        list.setOpaque(false);
        list.setEmptyMessage(I18.get("playlist_msg_empty"));
        list.setTableHeader(null);
        list.renderLocked(false);

        list.getTable().getModel().addTableModelListener(e -> {
                    int i = e.getFirstRow();
                    if (i == TableModelEvent.HEADER_ROW) {
                        return;
                    }
                    if (i < 0) {
                        return;
                    }
                    setChanged(true);
                });

        KeyListener[] key = list.getKeyListeners();
        for (KeyListener aKey : key) {
            list.removeKeyListener(aKey);
        }
        list.addKeyListener(
                new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyChar() == KeyEvent.VK_DELETE) {
                            removeSongs();
                            e.consume();
                        }
                    }
                });
        for (KeyListener aKey : key) {
            list.addKeyListener(aKey);
        }

        if (lib != null) {
            list.setDragEnabled(true);
            //list.setDropMode(DropMode.ON_OR_INSERT);
            list.setTransferHandler(new YassSongListTransferHandler(lib, false));
            list.setStoreAction(lib.getStoreAction());

            lib.setDragEnabled(true);
            //lib.setDropMode(DropMode.ON_OR_INSERT);
            lib.setTransferHandler(new YassSongListTransferHandler(lib, true));
            //list.setFilterList(lib);

            lib.addSongListListener(e -> {
                        int state = e.getState();
                        switch (state) {
                            case YassSongListEvent.LOADED:
                                loadPlayLists();
                                if (mustRefresh) {
                                    refresh();
                                }
                                break;
                            case YassSongListEvent.THUMBNAILING:
                                break;
                            case YassSongListEvent.FINISHED:
                                list.repaint();
                                break;
                        }
                    });
        }

        JScrollPane scroll = new JScrollPane(list);

        // JDK 1.6 specific
        // list.setFillsViewportHeight(true);

        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        panel.add("Center", scroll);
        add("Center", panel);

        if (toolbar != null) {
            panel.add("South", toolbar);
        }
    }

    /**
     * Gets the playList attribute of the YassPlayList class
     *
     * @param f Description of the Parameter
     * @return The playList value
     */
    public static boolean isPlayList(File f) {
        try {
            BufferedReader inputStream = new BufferedReader(new FileReader(f));
            String l;
            while ((l = inputStream.readLine()) != null) {
                if (l.length() < 1) {
                    continue;
                }
                if (l.startsWith("#Playlist")) {
                    inputStream.close();
                    return true;
                }
                if (l.startsWith("#Songs")) {
                    inputStream.close();
                    return true;
                }
                if (l.startsWith("#")) {
                    continue;
                }
                inputStream.close();
                return false;
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets the playlists attribute of the WorkerThread object
     *
     * @return The playlists value
     */
    public Vector<YassPlayListModel> getPlayLists() {
        return playlists;
    }

    /**
     * Description of the Method
     *
     * @param v Description of the Parameter
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean containsFile(Vector<YassPlayListModel> v, String s) {
        for (Enumeration<YassPlayListModel> en = v.elements(); en.hasMoreElements(); ) {
            YassPlayListModel pl = en.nextElement();
            if (s.equalsIgnoreCase(pl.getFileName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Description of the Method
     */
    public void clear() {
        if (playlists != null) {
            playlists.clear();
        }
        prop.remove("recent-playlist");
        list.clear();
    }

    /**
     * Description of the Method
     */
    public void loadPlayLists() {
        if (playlists == null) {
            playlists = new Vector<>();
        } else {
            playlists.clear();
        }

        String rec = prop.getProperty("recent-playlist");
        if (rec == null) {
            rec = "";
        }

        Vector<?> plv = lib.getPlayLists();
        if (plv != null) {
            for (Enumeration<?> en = plv.elements(); en.hasMoreElements(); ) {
                String filename = (String) en.nextElement();
                File pfile = new File(filename);
                if (pfile.exists() && isPlayList(pfile)) {
                    YassPlayListModel pl = getPlayListFile(pfile.getAbsolutePath());
                    pl.setFileName(filename);
                    if (!containsFile(playlists, pl.getFileName())) {
                        playlists.addElement(pl);
                    }
                    if (pl.getName().equals(rec)) {
                        setPlayList(pl);
                    }
                }
            }
        }

        String dir = prop.getProperty("song-directory");
        String pdir = prop.getProperty("playlist-directory");
        String playlistFileType = prop.getProperty("playlist-filetype");

        if (pdir != null) {
            File pf = new File(pdir);
            if (pf.exists() && pf.isDirectory()) {
                File sd = new File(dir);
                try {
                    String filename = pf.getCanonicalPath();
                    if (!filename.startsWith(sd.getCanonicalPath())) {
                        File[] pfiles = pf.listFiles();
                        for (File pfile : pfiles) {
                            if (pfile.getName().toLowerCase().endsWith(playlistFileType) && isPlayList(pfile)) {
                                YassPlayListModel pl = getPlayListFile(pfile.getAbsolutePath());
                                if (!containsFile(playlists, pl.getFileName())) {
                                    playlists.addElement(pl);
                                }
                                if (pl.getName().equals(rec)) {
                                    setPlayList(pl);
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        Collections.sort(playlists);

        actions.updatePlayListBox();
    }

    /**
     * Gets the list attribute of the YassPlayList object
     *
     * @return The list value
     */
    public YassSongList getList() {
        return list;
    }

    /**
     * Description of the Method
     *
     * @param onoff Description of the Parameter
     */
    public void tabChanged(boolean onoff) {
    }

    /**
     * Description of the Method
     */
    public void openPlayList() {
        FileDialog fd = new FileDialog((JFrame) SwingUtilities.getWindowAncestor(this), I18.get("playlist_open"), FileDialog.LOAD);

        String defDir = prop.getProperty("song-directory");
        if (defDir != null) {
            fd.setDirectory(defDir);
        }
        fd.setVisible(true);
        if (fd.getFile() != null) {
            YassPlayListModel pl = getPlayListFile(fd.getDirectory() + File.separator + fd.getFile());
            setPlayList(pl);
        }
        fd.dispose();
    }

    /**
     * Description of the Method
     */
    public void reload() {
        if (txtFile != null) {
            setPlayList(getPlayListFile(txtFile));
        }
    }

    public void addSongs(Vector<?> v) {
        YassSongListModel m = (YassSongListModel) list.getTable().getModel();
        boolean changed = false;
        for (Enumeration<?> en = v.elements(); en.hasMoreElements(); ) {
            YassSong s = (YassSong) en.nextElement();
            if (m.getData().contains(s)) {
                continue;
            }
            m.addRow(s);
            s.setLocked(true);
            changed = true;
        }
        if (changed) {
            setChanged(true);
            m.fireTableDataChanged();
            actions.updatePlayListCursor();
            //if (list.getRowCount() > 0 && list.getSelectedRow() < 0) {
            //	list.addRowSelectionInterval(0, 0);
            //}
        }
    }

    public void refreshWhenLoaded() {
        mustRefresh = true;
    }

    public void refresh() {
        YassSongListModel m = (YassSongListModel) list.getTable().getModel();
        Vector<YassSong> data = m.getData();
        Vector<YassSong> olddata = (Vector<YassSong>) data.clone();
        data.removeAllElements();

        for (YassSong s: olddata) {
            YassSong s2 = lib.getSong(s.getArtist(), s.getTitle());
            if (s2 != null) {
                data.addElement(s2);
            } else {
                data.addElement(s2 = new YassSong("", "", "", s.getArtist(), s.getTitle()));
                s2.setIcon(YassSongList.brokenSong);
            }
            s2.setSaved(s.isSaved());
            s2.setLocked(true);
        }
        m.fireTableDataChanged();
    }

    public void removeAllSongs() {
        JTable t = list.getTable();
        YassSongListModel m = (YassSongListModel) t.getModel();
        Vector<?> data = m.getData();

        for (Enumeration<?> en = data.elements(); en.hasMoreElements(); ) {
            YassSong s = (YassSong) en.nextElement();
            s.setLocked(false);
        }
        list.removeAllSongs();
        actions.updatePlayListCursor();
        listTitle = null;
        listFilename = null;

        setChanged(false);
        mustRefresh = false;
    }

    public void removeSongs() {
        JTable t = list.getTable();
        int[] rows = t.getSelectedRows();
        if (rows == null || rows.length == 0) {
            return;
        }
        removeSongs(rows);
    }

    public void removeSongs(int[] rows) {
        JTable t = list.getTable();
        YassSongListModel m = (YassSongListModel) t.getModel();
        Vector<?> data = m.getData();

        Arrays.sort(rows);
        boolean changed = false;
        for (int i = rows.length - 1; i >= 0; i--) {
            YassSong s = (YassSong) data.remove(rows[i]);
            s.setLocked(false);
            changed = true;
        }

        m.fireTableDataChanged();
        int n = data.size();
        if (n > 0) {
            int i = Math.min(rows[0], n - 1);
            t.clearSelection();
            t.addRowSelectionInterval(i, i);
        }
        actions.updatePlayListCursor();
        setChanged(changed);
    }

    public void up() {
        JTable t = list.getTable();
        YassSongListModel m = (YassSongListModel) t.getModel();
        Vector<YassSong> data = m.getData();

        int[] rows = t.getSelectedRows();
        if (rows == null || rows.length == 0) {
            return;
        }

        Arrays.sort(rows);
        if (rows[0] == 0) {
            return;
        }

        int i;
        for (i = 0; i < rows.length; i++) {
            YassSong o = data.remove(rows[i]);
            data.insertElementAt(o, rows[i] - 1);
        }

        m.fireTableDataChanged();
        t.clearSelection();
        Rectangle rr = null;
        for (i = rows.length - 1; i >= 0; i--) {
            int n = Math.max(0, rows[i] - 1);
            t.addRowSelectionInterval(n, n);
            if (rr == null) {
                rr = t.getCellRect(n, 0, true);
            } else {
                rr.add(t.getCellRect(n, 0, true));
            }
        }
        t.scrollRectToVisible(rr);
    }

    public void down() {
        JTable t = list.getTable();
        YassSongListModel m = (YassSongListModel) t.getModel();
        Vector<YassSong> data = m.getData();

        int[] rows = t.getSelectedRows();
        if (rows == null || rows.length == 0) {
            return;
        }

        Arrays.sort(rows);
        if (rows[rows.length - 1] == data.size() - 1) {
            return;
        }

        int i;
        for (i = rows.length - 1; i >= 0; i--) {
            YassSong o = data.remove(rows[i]);
            data.insertElementAt(o, rows[i] + 1);
        }

        m.fireTableDataChanged();
        t.clearSelection();
        Rectangle rr = null;
        for (i = rows.length - 1; i >= 0; i--) {
            int n = Math.min(data.size(), rows[i] + 1);
            t.addRowSelectionInterval(n, n);
            if (rr == null) {
                rr = t.getCellRect(n, 0, true);
            } else {
                rr.add(t.getCellRect(n, 0, true));
            }
        }
        t.scrollRectToVisible(rr);
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean storePlayListAs() {
        if (listTitle == null) {
            listTitle = "";
        }

        String input = JOptionPane.showInputDialog(SwingUtilities.getWindowAncestor(this), I18.get("playlist_store"), listTitle);
        if (input == null || input.trim().length() < 1) {
            return true;
        }
        boolean same = input.equals(listTitle);
        if (same) {
            return storePlayList();
        }

        String oldTitle = listTitle;
        listTitle = input;

        String root = prop.getProperty("songlist-directory");
        String defDir = prop.getProperty("playlist-directory");
        String filename = defDir + File.separator + listTitle + ".upl";

        String oldFilename = listFilename;
        if (listFilename != null) {
            File f = new File(listFilename);
            String fn = f.getAbsolutePath();
            String fpath = fn.substring(0, fn.lastIndexOf(File.separator) + 1);
            String fext = fn.substring(fn.lastIndexOf("."));
            listFilename = fpath + listTitle + fext;
            filename = listFilename;

            for (Enumeration<YassPlayListModel> en = playlists.elements(); en.hasMoreElements(); ) {
                YassPlayListModel pl = en.nextElement();
                if (pl.getName().equals(oldTitle)) {
                    pl.setFileName(listFilename);
                }
            }
        }

        if (new File(filename).exists()) {
            int ok = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this), I18.get("playlist_store_error"), I18.get("playlist_store_title"), JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) {
                return false;
            }
        }

        prop.setProperty("recent-playlist", listTitle);
        boolean success = storePlayList();
        if (!success) {
            return false;
        }

        if (oldTitle.length() < 1) {
            YassPlayListModel pl = new YassPlayListModel();
            pl.setName(listTitle);
            YassSongListModel m = (YassSongListModel) list.getTable().getModel();
            pl.addAll(m.getData());
            playlists.addElement(pl);
        } else {
            String old = root + File.separator + defDir + File.separator + oldTitle + ".upl";

            if (oldFilename != null) {
                old = oldFilename;
            }
            new File(old).delete();
            for (Enumeration<YassPlayListModel> en = playlists.elements(); en.hasMoreElements(); ) {
                YassPlayListModel pl = en.nextElement();
                if (pl.getName().equals(oldTitle)) {
                    pl.setName(listTitle);
                }
            }
        }
        actions.updatePlayListBox();
        saveAction.setEnabled(false);
        return true;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean storePlayList() {
        if (listTitle == null || listTitle.length() < 1) {
            return storePlayListAs();
        }

        String defDir = prop.getProperty("playlist-directory");
        String filename = defDir + File.separator + listTitle + ".upl";

        if (listFilename != null) {
            filename = listFilename;
        }

        YassSongListModel m = (YassSongListModel) list.getTable().getModel();
        Vector<?> data = m.getData();
        int n = data.size();

        PrintWriter outputStream;
        try {
            outputStream = new PrintWriter(new FileWriter(filename));

            outputStream.println("######################################");
            outputStream.println("#Ultrastar Deluxe Playlist Format v1.0");
            outputStream.println("#Playlist \"" + listTitle + "\" with " + n + " Songs.");
            outputStream.println("#Created with Yass " + YassActions.VERSION + ".");
            outputStream.println("######################################");
            outputStream.println("#Name: " + listTitle);
            outputStream.println("#Songs:");

            for (Enumeration<?> en = data.elements(); en.hasMoreElements(); ) {
                YassSong s = (YassSong) en.nextElement();

                String at = s.getArtist() + " : " + s.getTitle();
                outputStream.println(at);
            }
            outputStream.close();

            saveAction.setEnabled(false);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Sets the saveAction attribute of the YassPlayList object
     *
     * @param a The new saveAction value
     * @param b The new storeAction value
     */
    public void setStoreAction(Action a, Action b) {
        saveAction = a;
        //saveAction.setEnabled(false);

        saveAsAction = b;
        //saveAsAction.setEnabled(false);
    }

    /**
     * Sets the moveAction attribute of the YassPlayList object
     *
     * @param a The new moveAction value
     * @param b The new moveAction value
     */
    public void setMoveAction(Action a, Action b) {
        moveUpAction = a;
        moveDownAction = b;
        moveUpAction.setEnabled(false);
        moveDownAction.setEnabled(false);
    }

    /**
     * Sets the playList attribute of the YassPlayList object
     *
     * @param pl The new playList value
     */
    public void setPlayList(YassPlayListModel pl) {
        setName(pl.getName());
        removeAllSongs();
        addSongs(pl);
        listTitle = pl.getName();
        listFilename = pl.getFileName();
        prop.setProperty("recent-playlist", listTitle);
        setChanged(false);
    }

    /**
     * Sets the playList attribute of the YassPlayList object
     *
     * @param n The new playList value
     */
    public void setPlayList(int n) {
        YassPlayListModel pl = getPlayLists().elementAt(n);
        setPlayList(pl);
    }

    /**
     * Description of the Method
     *
     * @param n Description of the Parameter
     */
    public void removePlayList(int n) {
        YassPlayListModel pl = getPlayLists().elementAt(n);
        String defDir = prop.getProperty("playlist-directory");
        String filename = defDir + File.separator + pl.getName() + ".upl";
        File f = new File(filename);

        int ok = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this), I18.get("playlist_remove_msg"), I18.get("playlist_remove_title"), JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        f.delete();

        removeAllSongs();
        getPlayLists().remove(pl);
        prop.remove("recent-playlist");
        actions.updatePlayListBox();
    }

    /**
     * Gets the name attribute of the YassPlayList object
     *
     * @return The name value
     */
    public String getName() {
        return listTitle;
    }

    /**
     * Sets the name attribute of the YassPlayList object
     *
     * @param s The new name value
     */
    public void setName(String s) {
        listTitle = s;
    }

    /**
     * Gets the playListFile attribute of the YassPlayList object
     *
     * @param txt Description of the Parameter
     * @return The playListFile value
     */
    public YassPlayListModel getPlayListFile(String txt) {
        try {
            File pFile = new File(txt);
            FileReader fr = new FileReader(pFile);

            StringWriter sw = new StringWriter();
            char[] buffer = new char[1024];
            int readCount;
            while ((readCount = fr.read(buffer)) > 0) {
                sw.write(buffer, 0, readCount);
            }
            fr.close();

            YassPlayListModel plm = getPlayList(sw.toString());
            plm.setFileName(txt);
            return plm;
        } catch (Exception e) {
            list.removeAllSongs();
            txtFile = null;
        }
        return null;
    }


    /**
     * Sets the changed attribute of the YassPlayList object
     *
     * @param changed The new changed value
     */
    public void setChanged(boolean changed) {
        YassSongListModel m = (YassSongListModel) list.getTable().getModel();
        int n = m.getData().size();

        //saveAction.setEnabled(changed && !empty);
        // double action with setPlayListCursor

        saveAction.setEnabled(changed);
        saveAsAction.setEnabled(n >= 1);
        moveUpAction.setEnabled(n > 1);
        moveDownAction.setEnabled(n > 1);
    }


    /**
     * Gets the playList attribute of the YassPlayList object
     *
     * @param txt Description of the Parameter
     * @return The playList value
     */
    public YassPlayListModel getPlayList(String txt) {
        YassPlayListModel pl = new YassPlayListModel();

        try {
            BufferedReader inputStream = new BufferedReader(new StringReader(txt));
            String l;

            while ((l = inputStream.readLine()) != null) {
                StringTokenizer ll = new StringTokenizer(l, ":");
                String artist = null;
                String title;

                if (ll.hasMoreTokens()) {
                    artist = ll.nextToken().trim();
                    if (artist.startsWith("#")) {
                        if (artist.startsWith("#Playlist")) {
                            int k = artist.indexOf('\"');
                            if (k > 0) {
                                int kk = artist.indexOf('\"', k + 1);
                                if (kk > 0) {
                                    pl.setName(artist.substring(k + 1, kk));
                                }
                            }
                        }
                        if (artist.startsWith("#Name:")) {
                            int k = artist.indexOf(':');
                            pl.setName(artist.substring(k + 1).trim());
                        }
                        continue;
                    }
                }
                if (ll.hasMoreTokens()) {
                    title = ll.nextToken().trim();
                    YassSong s = lib.getSong(artist, title);
                    if (s == null) {
                        s = new YassSong("", "", "", artist, title);
                        s.setIcon(YassSongList.brokenSong);
                    }
                    if (!pl.contains(s)) {
                        pl.addElement(s);
                    }
                }
            }
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return pl;
    }
}

