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
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.*;

import yass.filter.YassFilter;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassGroups extends JTable implements DropTargetListener {
    private static final long serialVersionUID = 7459513036812933052L;
    /**
     * Description of the Field
     */
    public static ImageIcon noCover = null;
    private final int WIDTH = 100;
    Font groupfont = new Font("SansSerif", Font.BOLD, 12);
    Color bgColor = new Color(1f, 1f, 1f, 0.8f), fgColor = new Color(.2f, .2f, .2f, .6f);
    Color titleColor = new Color(153, 153, 153);
    boolean ignoreChange = false;
    MessageFormat msgtip = new MessageFormat(I18.get("groups_tip"));
    private YassProperties prop;
    private YassActions actions;
    private YassGroupsModel gm = null;
    private Hashtable<String, ImageIcon> covers = new Hashtable<>();
    private Hashtable<String, YassFilter> filters = new Hashtable<>();
    private String group = null, rule = null;
    private YassSongList songList = null;
    private Vector<Integer> counters = new Vector<>();
    private RefresherThread refresher = null;
    private Hashtable<String, JToolBar> toolbars = null;
    private JToolBar defTool = null;
    private int dragOverCell = -1;
    private boolean dragOK = true;
    private JComponent dropTarget = null;


    /**
     * Constructor for the YassGroups object
     *
     * @param songList Description of the Parameter
     */
    public YassGroups(YassSongList songList) {
        actions = songList.getActions();
        prop = actions.getProperties();
        this.songList = songList;

        YassFilter.setProperties(prop);
        YassFilter.init();

        getInputMap().put(KeyStroke.getKeyStroke("HOME"), "selectFirstRow");
        getInputMap().put(KeyStroke.getKeyStroke("END"), "selectLastRow");

        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setShowGrid(false);

        setModel(gm = new YassGroupsModel());
        getTableHeader().setReorderingAllowed(false);
        createDefaultColumnsFromModel();
        setRowHeight(WIDTH);
        setTableHeader(null);
        TableColumn col = getColumnModel().getColumn(0);
        col.setMinWidth(WIDTH);
        col.setMaxWidth(WIDTH);
        col.setPreferredWidth(WIDTH);
        col.setCellRenderer(new GroupRenderer());

        Dimension size = getPreferredScrollableViewportSize();
        setPreferredScrollableViewportSize(new Dimension(Math.min(getPreferredSize().width, size.width), size.height));

        setBackground(songList.getBackground());

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setDragEnabled(false);
        setDropTarget(this);
        //setTransferHandler(new YassGroupsTransferHandler(songList));

        // setFillsViewportHeight(true);

        setGroups(0);

        getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if (e.getValueIsAdjusting()) {
                            return;
                        }

                        int i = getSelectedRow();
                        if (i < 0 || i >= getRowCount()) {
                            return;
                        }
                        setFilter(i);
                    }
                });

        try {
            noCover = new ImageIcon(I18.getImage("group_image.jpg"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the groups attribute of the YassGroups object
     *
     * @param i The new groups value
     */
    public void setGroups(int i) {
        String oldgroup = group;
        group = YassFilter.getIDAt(i);
        refresh();

        rule = gm.getRowAt(i);
        firePropertyChange("group", oldgroup, group);
    }

    /**
     * Description of the Method
     */
    public void refresh() {
        gm.getData().clear();

        String str = prop.getProperty("group-" + group);
        if (str == null) {
            str = "";
        }

        StringTokenizer st = new StringTokenizer(str, "|");
        while (st.hasMoreTokens()) {
            String t = st.nextToken();

            if (t.equals("generic")) {
                YassFilter gf = YassFilter.createFilter(group);
                if (gf != null) {
                    String rules[] = gf.getGenericRules(songList.getUnfilteredData());
                    if (rules != null) {
                        for (String rule1 : rules) {
                            t = rule1;
                            gm.addRow(t);

                            YassFilter f = filters.get(group + "-" + t);
                            if (f == null) {
                                f = YassFilter.createFilter(group);
                                if (f != null) {
                                    f.setRule(t);
                                    filters.put(group + "-" + t, f);
                                }
                            }
                        }
                    }
                }
            } else {
                gm.addRow(t);

                YassFilter f = filters.get(group + "-" + t);
                if (f == null) {
                    f = YassFilter.createFilter(group);
                    if (f != null) {
                        f.setRule(t);
                        filters.put(group + "-" + t, f);
                    }
                }
            }
        }
        gm.fireTableDataChanged();

        if (getRowCount() > 0) {
            setRowSelectionInterval(0, 0);
        }
        refreshCounters();
    }

    /**
     * Description of the Method
     */
    public void refreshCounters() {
        if (refresher != null) {
            refresher.notInterrupted = false;
            int n = 10;
            while (!refresher.finished && --n > 0) {
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (Exception ignored) {
                }
            }
        }

        refresher = new RefresherThread();
        refresher.start();
    }

    /**
     * Gets the filter attribute of the YassGroups object
     *
     * @param rule Description of the Parameter
     * @return The filter value
     */
    public YassFilter getFilter(String rule) {
        return filters.get(group + "-" + rule);
    }

    /**
     * Sets the ignoreChange attribute of the YassGroups object
     *
     * @param onoff The new ignoreChange value
     */
    public void setIgnoreChange(boolean onoff) {
        ignoreChange = onoff;
    }

    /**
     * Sets the filter attribute of the YassGroups object
     *
     * @param i The new filter value
     */
    public void setFilter(int i) {
        if (ignoreChange) {
            return;
        }

        String oldrule = rule;
        rule = gm.getRowAt(i);

        YassSong sel = songList.getFirstSelectedSong();
        YassFilter f = getFilter(rule);
        songList.setPreFilter(f);
        songList.interruptFilter();
        songList.filter(null);
        songList.sortBy(f != null ? f.getSorting() : YassSongList.TITLE_COLUMN);
        songList.setExtraInfo(f != null ? f.getExtraInfo() : YassSongList.TITLE_COLUMN);

        if (songList.getFirstSelectedSong() == null) {
            songList.selectSong(0);
            //songList.focusFirstVisible();
        } else {
            //songList.requestFocus();
            songList.selectSong(sel);
        }
        firePropertyChange("rule", oldrule, rule);
    }

    /**
     * Sets the toolBar attribute of the YassGroups object
     *
     * @param rule The new toolBar value
     * @param t    The new toolBar value
     */
    public void setToolBar(String rule, JToolBar t) {
        if (toolbars == null) {
            toolbars = new Hashtable<>();
        }
        toolbars.put(rule, t);
    }

    /**
     * Gets the defaultToolBar attribute of the YassGroups object
     *
     * @return The defaultToolBar value
     */
    public JToolBar getDefaultToolBar() {
        if (defTool == null) {
            defTool = new JToolBar();
            defTool.setFloatable(false);
        }
        return defTool;
    }

    /**
     * Gets the toolBar attribute of the YassGroups object
     *
     * @param i Description of the Parameter
     * @return The toolBar value
     */
    public JToolBar getToolBar(int i) {
        if (toolbars == null) {
            return getDefaultToolBar();
        }
        String rule = group = YassFilter.getIDAt(i);
        JToolBar t = toolbars.get(rule);
        if (t == null) {
            return getDefaultToolBar();
        }
        return t;
    }

    /**
     * Gets the icon attribute of the YassGroups object
     *
     * @param s Description of the Parameter
     * @return The icon value
     */
    public ImageIcon getCover(String s) {
        //System.out.println("getcover " + s);
        ImageIcon ii = covers.get(s);
        if (ii != null) {
            return ii;
        }

        try {
            s = s.replace(':', '_');
            s = s.replace('?', '_');
            s = s.replace('/', '_');
            s = s.replace('\\', '_');
            s = s.replace('*', '_');
            s = s.replace('\"', '_');
            s = s.replace('<', '_');
            s = s.replace('>', '_');
            s = s.replace('|', '_');

            String coverDir = prop.getProperty("cover-directory");

            File file = new File(coverDir + File.separator + s + ".jpg");
            BufferedImage img;
            if (file.exists()) {
                img = javax.imageio.ImageIO.read(file);
            } else {
                java.net.URL is = I18.getResource(s + ".jpg");
                // throws exception when not found
                img = YassUtils.readImage(is);
            }

            //attention: scale down only
            BufferedImage bufferedImage = YassUtils.getScaledInstance(img, WIDTH, WIDTH);

            //BufferedImage bufferedImage = new BufferedImage(WIDTH, WIDTH, BufferedImage.TYPE_INT_RGB);
            //Graphics2D g2d = bufferedImage.createGraphics();
            //g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            //g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            //g2d.drawImage(img, 0, 0, WIDTH, WIDTH, null);
            //g2d.dispose();
            img.flush();

            ii = new ImageIcon(bufferedImage);
            covers.put(s, ii);
            return ii;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }

    /**
     * Sets the dropTarget attribute of the YassActions object
     *
     * @param c The new dropTarget value
     */
    public void setDropTarget(JComponent c) {
        if (dropTarget == null) {
            dropTarget = c;
        }
        new DropTarget(c, this);
    }

    /**
     * Description of the Method
     *
     * @param dropTargetDragEvent Description of the Parameter
     */
    public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
        dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    }

    /**
     * Description of the Method
     *
     * @param dropTargetEvent Description of the Parameter
     */
    public void dragExit(DropTargetEvent dropTargetEvent) {
        dragOverCell = -1;
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        repaint();
                    }
                });
    }

    /**
     * Description of the Method
     *
     * @param dropTargetDragEvent Description of the Parameter
     */
    public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
        Point p = dropTargetDragEvent.getLocation();
        int row = rowAtPoint(p);
        dragOverCell = -1;
        if (row < 0 || row >= getRowCount()) {
            return;
        }

        String rule = gm.getRowAt(row);
        YassFilter f = getFilter(rule);

        Transferable tr = dropTargetDragEvent.getTransferable();
        if (tr.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            dragOK = f.allowCoverDrop(rule);
        } else if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dragOK = f.allowCoverDrop(rule);
        } else if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dragOK = f.allowDrop(rule);
        }

        dragOverCell = row;
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        repaint();
                    }
                });
    }

    /**
     * Description of the Method
     *
     * @param dropTargetDragEvent Description of the Parameter
     */
    public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
    }

    /**
     * Description of the Method
     *
     * @param dropTargetDropEvent Description of the Parameter
     */
    public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
        YassGroupsModel gm = (YassGroupsModel) getModel();

        Point p = dropTargetDropEvent.getLocation();
        int row = rowAtPoint(p);
        if (row < 0 || row >= getRowCount()) {
            dropTargetDropEvent.rejectDrop();
            return;
        }

        String rule = gm.getRowAt(row);
        YassFilter f = getFilter(rule);

        try {
            Transferable tr = dropTargetDropEvent.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                String str = (String) tr.getTransferData(DataFlavor.stringFlavor);

                if (f.allowDrop(rule)) {
                    boolean doit = true;
                    if (f.confirm()) {
                        int ok = JOptionPane.showConfirmDialog(actions.getTab(), f.getConfirmString(rule), I18.get("groups_drop_title"), JOptionPane.OK_CANCEL_OPTION);
                        if (ok != JOptionPane.OK_OPTION) {
                            doit = false;
                        }
                    }
                    if (doit) {
                        try {
                            StringReader sr = new StringReader(str);
                            BufferedReader br = new BufferedReader(sr);
                            String l;
                            while ((l = br.readLine()) != null) {
                                StringTokenizer st = new StringTokenizer(l, ":");
                                String artist = st.hasMoreTokens() ? st.nextToken().trim() : null;
                                String title = st.hasMoreTokens() ? st.nextToken().trim() : null;
                                if (artist != null && title != null) {
                                    YassSong s = songList.getSong(artist, title);
                                    if (s != null) {
                                        f.drop(rule, s);
                                        if (!s.isSaved()) {
                                            songList.setSaved(false);
                                        }
                                    }
                                }
                            }
                            br.close();
                            SwingUtilities.invokeLater(
                                    new Runnable() {
                                        public void run() {
                                            songList.repaint();
                                        }
                                    });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (f.refreshCounters()) {
                        refreshCounters();
                    }
                }
            } else if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

                if (f.allowCoverDrop(rule)) {
                    java.util.List fileList = (java.util.List) tr.getTransferData(DataFlavor.javaFileListFlavor);
                    Iterator<?> iterator = fileList.iterator();
                    File file = null;
                    if (iterator.hasNext()) {
                        file = (File) iterator.next();
                    }
                    if (file != null && !file.isDirectory()) {
                        String fn = file.getName().toLowerCase();
                        if (fn.endsWith(".gif") || fn.endsWith(".bmp") || fn.endsWith(".png") || fn.endsWith(".jpg") || fn.endsWith(".jpeg")) {
                            String coDir = prop.getProperty("cover-directory");
                            boolean doit = true;
                            File newfile = new File(coDir + File.separator + rule + ".jpg");
                            if (newfile.exists()) {
                                int ok = JOptionPane.showConfirmDialog(actions.getTab(), MessageFormat.format(I18.get("groups_drop_cover_msg"), rule), I18.get("groups_drop_title"), JOptionPane.OK_CANCEL_OPTION);
                                if (ok != JOptionPane.OK_OPTION) {
                                    doit = false;
                                } else {
                                    newfile.delete();
                                }
                            }
                            if (doit) {
                                BufferedImage buf = YassUtils.readImage(file);
                                if (buf != null) {
                                    javax.imageio.ImageIO.write(buf, "jpg", newfile);
                                    covers.remove(rule);
                                }
                            }
                        }
                    }
                }
            } else if (tr.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                Image img = (Image) tr.getTransferData(DataFlavor.imageFlavor);

                if (f.allowCoverDrop(rule)) {
                    String coDir = prop.getProperty("cover-directory");
                    boolean doit = true;
                    File newfile = new File(coDir + File.separator + rule + ".jpg");
                    if (newfile.exists()) {
                        int ok = JOptionPane.showConfirmDialog(actions.getTab(), MessageFormat.format(I18.get("groups_drop_cover_msg"), rule), I18.get("groups_drop_title"), JOptionPane.OK_CANCEL_OPTION);
                        if (ok != JOptionPane.OK_OPTION) {
                            doit = false;
                        } else {
                            newfile.delete();
                        }
                    }
                    if (doit) {
                        BufferedImage buf = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2d = buf.createGraphics();
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g2d.drawImage(img, 0, 0, null);
                        g2d.dispose();
                        javax.imageio.ImageIO.write(buf, "jpg", newfile);
                        covers.remove(rule);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        dragOverCell = -1;
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        repaint();
                    }
                });
        dropTargetDropEvent.getDropTargetContext().dropComplete(true);
    }

    class RefresherThread extends Thread {
        public boolean finished = false;
        public boolean notInterrupted = true;

        public void run() {
            counters.removeAllElements();

            Vector<?> rules = gm.getData();
            //System.out.println("refreshing counters...");
            for (Enumeration<?> en = rules.elements(); en.hasMoreElements() && notInterrupted; ) {
                String t = (String) en.nextElement();
                YassFilter f = filters.get(group + "-" + t);
                if (!f.count()) {
                    counters.addElement(new Integer(-1));
                    continue;
                }

                int n = 0;

                Vector<?> all = songList.getUnfilteredData();
                f.start(all);
                for (Enumeration<?> e = all.elements(); e.hasMoreElements() && notInterrupted; ) {
                    YassSong s = (YassSong) e.nextElement();
                    if (f.accept(s)) {
                        n++;
                    }
                }
                f.stop();

                counters.addElement(new Integer(n));
            }
            //System.out.println("refreshing counters done.");
            finished = true;
        }
    }

    /**
     * Description of the Class
     *
     * @author Saruta
     */
    class GroupRenderer extends JLabel implements TableCellRenderer {
        private static final long serialVersionUID = 8499609874569104902L;
        String title = "";
        boolean selected = false;
        boolean renderTitle = false;
        String counterString = null;

        Color b1col = UIManager.getColor("Table.gridColor");
        Color selcol = UIManager.getColor("Table.selectionBackground");
        Border b1 = BorderFactory.createLineBorder(b1col, 1);
        Border b2 = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(selcol, 2), BorderFactory.createLoweredBevelBorder());
        Border b5 = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black, 2), BorderFactory.createLoweredBevelBorder());
        Border b3 = BorderFactory.createLineBorder(Color.green, 2);
        Border b4 = BorderFactory.createLineBorder(Color.red, 2);

        Hashtable<String, String> i18Labels = new Hashtable<>();


        /**
         * Gets the tableCellRendererComponent attribute of the GroupRenderer object
         *
         * @param table      Description of the Parameter
         * @param value      Description of the Parameter
         * @param isSelected Description of the Parameter
         * @param hasFocus   Description of the Parameter
         * @param rowIndex   Description of the Parameter
         * @param vColIndex  Description of the Parameter
         * @return The tableCellRendererComponent value
         */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
            if (value == null) {
                return this;
            }
            String s = (String) value;

            YassFilter f = getFilter(s);
            String groupkey = "group_" + f.getID();
            String key = "group_" + f.getID() + "_" + s;
            try {
                String is = i18Labels.get(key);
                //System.out.println(key + " " + is);
                if (is == null) {
                    is = I18.get(key);
                    if (is == null) {
                        is = "_none_";
                    }
                    i18Labels.put(key, is);
                }
                if (!is.equals("_none_")) {
                    s = is;
                }
            } catch (java.util.MissingResourceException ex) {
                s = (String) value;
            }

            setHorizontalAlignment(CENTER);

            setText("");
            title = "";

            renderTitle = f.renderTitle();
            if (renderTitle || f.showTitle()) {
                title = s;
            }

            Icon i = getCover(s);
            if (i == null) {
                i = getCover(key);
                //System.out.println("key="+key);
            }
            if (i == null) {
                i = getCover(groupkey);
            }
            if (i == null) {
                i = noCover;
            } else {
                renderTitle = false;
                if (!f.showTitle()) {
                    title = "";
                }
            }
            setIcon(i);

            if (rowIndex < counters.size()) {
                int n = counters.elementAt(rowIndex).intValue();
                if (n >= 0) {
                    counterString = n + "";
                } else {
                    counterString = null;
                }
            }
            setBorder(isSelected ? (YassGroups.this.isFocusOwner() ? b2 : b5) : b1);
            if (rowIndex == dragOverCell) {
                setBorder(dragOK ? b3 : b4);
            }
            selected = isSelected && YassGroups.this.isFocusOwner();

            setOpaque(true);
            return this;
        }


        /**
         * Description of the Method
         *
         * @param g2 Description of the Parameter
         */
        public void paintComponent(Graphics g2) {
            int w = YassGroups.this.WIDTH;
            int yoff = 20;
            int xoff = 20;

            Graphics2D g2d = (Graphics2D) g2;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            super.paintComponent(g2);
            if (renderTitle) {
                int fontsize = title.length() > 2 ? 24 : 48;
                Font font = new Font("Verdana", Font.BOLD, fontsize);
                g2d.setFont(font);
                g2d.setColor(titleColor);
                FontMetrics metrics = g2d.getFontMetrics();
                Rectangle2D box = metrics.getStringBounds(title, g2d);
                int xx = (int) (w / 2 - box.getWidth() / 2);
                int yy = (int) (w / 2 - box.getHeight() / 2 + metrics.getAscent());
                g2d.drawString(title, xx, yy);
            } else if (title != null && title.length() > 0) {
                g2.setColor(fgColor);
                g2.fillRoundRect(xoff + 1, w - 20 - yoff + 1, w, 20, 5, 5);

                g2.setColor(selected ? selcol : bgColor);
                g2.fillRoundRect(xoff, w - 20 - yoff, w, 20, 5, 5);

                g2.setColor(fgColor);
                g2.setFont(groupfont);
                FontMetrics metrics = g2.getFontMetrics();
                String s = trim(g2, title, w - xoff - 6);
                int strw = metrics.stringWidth(s);
                g2.drawString(s, xoff + 3 + (w - xoff - 3) / 2 - strw / 2, w - 20 + 14 - yoff);
            }
            if (counterString != null) {
                g2.setColor(fgColor);
                g2.setFont(groupfont);
                FontMetrics metrics = g2.getFontMetrics();
                int strw = metrics.stringWidth(counterString);
                g2.drawString(counterString, w - strw - 4, w - 5);
                g2.setColor(bgColor);
                g2.drawString(counterString, w - strw - 5, w - 6);
            }
        }


        public String trim(Graphics g2, String s, int w) {
            if (g2 == null) {
                return s;
            }
            FontMetrics fm = g2.getFontMetrics();

            if (fm.stringWidth(s) < w) {
                return s;
            }

            String in = s.substring(0, 3);
            String trim = s.substring(3);
            while (trim.length() > 1 && fm.stringWidth(in + "~" + trim) > w) {
                trim = trim.substring(1);
            }
            return in + "~" + trim;
        }
    }
}

