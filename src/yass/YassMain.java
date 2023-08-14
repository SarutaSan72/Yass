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

import org.apache.commons.lang3.StringUtils;
import yass.stats.YassStats;

import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassMain extends JFrame {
    private static boolean convert = false;
    private static boolean edit = false;
    private static String midiFile = null;
    private static final Vector<String> txtFiles = new Vector<>();
    private static String dirFile = null;
    private YassProperties prop;
    private YassSheet sheet = null;
    private YassActions actions = null;
    private YassPlayer mp3 = null;
    private YassLyrics lyrics = null;
    private YassSongList songList = null;
    private YassSongInfo songInfo = null;
    private YassPlayList playList = null;
    private YassGroups groups = null;
    private JPanel toolPanel = null;
    private JPanel groupsPanel, songPanel, playlistPanel, sheetInfoPanel;
    private JComponent editToolbar;

    public static void main(String[] argv) {
        checkAudio();
        initLater(argv);
    }

    private static void initLater(final String[] argv) {
        SwingUtilities.invokeLater(() -> {
            final YassMain y = new YassMain();
            y.parseCommandLine(argv);

            System.out.println("Init...");
            y.init();
            System.out.println("Initialized.");

            y.initConvert();

            y.onShow();

            System.out.println("Starting...");
            y.load();

            y.initFrame();
            if (y.refreshLibrary()) {
                System.out.println("Song Library was refreshed...");
            }
            System.out.println("Ready. Let's go.");
        });
    }

    private static void checkAudio() {
        try {
            if (javax.sound.midi.MidiSystem.getSequencer() == null)
                System.out.println("MidiSystem sequencer unavailable.");
            else if (javax.sound.sampled.AudioSystem.getMixer(null) == null)
                System.out.println("AudioSystem unavailable.");
            System.out.println("AudioSystem and MidiSystem Sequencer found.");
        } catch (MidiUnavailableException e) {
            System.err.println("Midi system sequencer not available.");
        }
    }

    private void initFrame() {
        this.setTitle(I18.get("yass_title"));
        URL icon16 = YassMain.this.getClass().getResource("/yass/resources/img/yass-icon-16.png");
        URL icon32 = YassMain.this.getClass().getResource("/yass/resources/img/yass-icon-32.png");
        URL icon48 = YassMain.this.getClass().getResource("/yass/resources/img/yass-icon-48.png");
        ArrayList<Image> icons = new ArrayList<>();
        addIconToIconList(icon48, icons);
        addIconToIconList(icon32, icons);
        addIconToIconList(icon16, icons);
        this.setIconImages(icons);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        if (!askStop())
                            return;

                        setDefaultSize(((Component) e.getSource()).getSize());
                        setDefaultLocation(((Component) e.getSource()).getLocation());

                        e.getWindow().setVisible(false);
                        e.getWindow().dispose();
                        System.exit(0);
                    }
                });
        this.pack();
        this.setSize(getDefaultSize());

        Point p = getDefaultLocation();
        if (p != null)
            this.setLocation(p);
        else
            this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private static void addIconToIconList(URL icon, ArrayList<Image> icons) {
        if (icon == null) {
            return;
        }
        icons.add(new ImageIcon(icon).getImage());
    }

    private void initConvert() {
        if (convert || midiFile != null) {
            String newSong = actions.createNewSong(midiFile, true);
            if (newSong == null)
                System.exit(0);
            parseCommandLine(new String[]{"-edit", newSong});
        }
    }

    private void onShow() {
        JViewport v = (JViewport) songList.getTableHeader().getParent();
        if (v != null) {
            v.setVisible(false);
        }
    }

    private Point getDefaultLocation() {
        String x = prop.getProperty("frame-x");
        String y = prop.getProperty("frame-y");
        if (x == null || y == null)
            return null;
        return new Point(Integer.parseInt(x), Integer.parseInt(y));
    }

    private void setDefaultLocation(Point p) {
        prop.setProperty("frame-x", Integer.toString(p.x));
        prop.setProperty("frame-y", Integer.toString(p.y));
    }

    private Dimension getDefaultSize() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        String w = prop.getProperty("frame-width");
        if (w == null)
            w = dim.width >= 1200 ? "1200" : Integer.toString(dim.width);
        else if (Integer.parseInt(w) > dim.width)
            w = Integer.toString(dim.width);

        String h = prop.getProperty("frame-height");
        if (h == null)
            h = dim.height >= 800 ? "800" : Integer.toString(dim.height);
        else if (Integer.parseInt(h) > dim.height)
            w = Integer.toString(dim.height);

        return new Dimension(Integer.parseInt(w), Integer.parseInt(h));
    }

    private void setDefaultSize(Dimension d) {
        prop.setProperty("frame-width", Integer.toString(d.width));
        prop.setProperty("frame-height", Integer.toString(d.height));

        actions.interruptPlay();

        sheet.updateHeight();
        actions.revalidateLyricsArea();
        sheet.update();
        sheet.repaint();
    }

    public void init() {
        prop = new YassProperties();

        initLanguage();
        checkVersion();
        initTempDir();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        sheet = new YassSheet();
        mp3 = new YassPlayer(sheet);

        lyrics = new YassLyrics(prop);
        lyrics.setSheet(sheet);

        actions = new YassActions(sheet, mp3, lyrics);
        actions.init(prop);
        actions.setTab(mainPanel);
        sheet.setActions(actions);

        ToolTipManager.sharedInstance().setInitialDelay(200);
        ToolTipManager.sharedInstance().setReshowDelay(0);

        YassStats.setProperties(prop);
        YassStats.init();

        JPanel songListPanel = createSongListPanel();
        JPanel sheetPanel = createSheetPanel();

        YassVideo video = new YassVideo(prop, sheet);
        actions.setVideo(video);
        mp3.setVideo(video);

        initLyricsLayout();

        sheet.setLayout(null);
        sheet.add(lyrics);

        YassErrors errors = new YassErrors(actions, prop, actions.createErrorToolbar());
        actions.setErrors(errors);

        actions.setPanels(this, mainPanel, songListPanel, songInfo, songPanel, playlistPanel, sheetPanel, sheetInfoPanel);

        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add("Center", mainPanel);
    }

    private void initLyricsLayout() {
        String layout = prop.getProperty("editor-layout");
        if (layout == null)
            layout = "East";

        String lyricsWidthString = prop.getProperty("lyrics-width");
        String lyricsHeightString = prop.getProperty("lyrics-min-height");
        int lyricsWidth = Integer.parseInt(lyricsWidthString);
        int lyricsMinHeight = Integer.parseInt(lyricsHeightString);

        if (layout.equals("East")) {
            lyrics.setBounds(500, 30, lyricsWidth, lyricsMinHeight);
        } else if (layout.equals("West")) {
            lyrics.setBounds(0, 30, lyricsWidth, lyricsMinHeight);
        }
    }

    private void initTempDir() {
        File td = new File(prop.getProperty("temp-dir"));
        if (td.exists()) {
            YassUtils.deleteDir(td);
        }
        if (! td.mkdirs()) {
            System.out.println("Warning: Cannot create temp-dir: "+td.getAbsolutePath());
        }
    }

    private void checkVersion() {
        if (prop.checkVersion()) {
            String dir = prop.getUserDir();
            int ok = JOptionPane.showConfirmDialog(null, "<html>" + I18.get("incompatible_version") + "<br>" + dir + "<br><br>" + I18.get("remove_version"), I18.get("incompatible_version") + " - Yass", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.OK_OPTION) {
                boolean verify = !dir.contains(".yass");
                if (verify || !(new File(dir).exists()) || !(new File(dir).isDirectory())) {
                    JOptionPane.showMessageDialog(null, I18.get("remove_version_error"), I18.get("incompatible_version"), JOptionPane.WARNING_MESSAGE);
                } else {
                    YassUtils.deleteDir(new File(dir));
                }
                prop.load();
            }
        }
    }

    private void initLanguage() {
        String lang = prop.getProperty("yass-language");
        if (lang == null || lang.equals("default")) {
            lang = Locale.getDefault().getLanguage();
        }
        System.out.println("Setting Language: " + lang);
        I18.setLanguage(lang);
    }

    private void parseCommandLine(String[] argv) {
        if (argv == null)
            return;

        for (String arg : argv) {
            String low = arg.toLowerCase();
            if (low.equals("-convert"))
                convert = true;
            else if (low.equals("-edit"))
                edit = true;
            else if (low.endsWith(".mid") || low.endsWith(".midi") || low.endsWith(".kar"))
                midiFile = arg;
            else if (low.endsWith(".txt")) {
                edit = true;
                txtFiles.add(arg);
            } else if (new File(arg).isDirectory()) {
                edit = true;
                dirFile = arg;
            }
        }
    }

    public void load() {
        String s = prop.getProperty("welcome");
        boolean store = false;
        if (s != null && s.equals("true")) {
            new YassLibOptions(prop, actions, songList, mp3);
            prop.setProperty("welcome", "false");
            store = true;
        }
        String spacing = prop.getProperty("correct-uncommon-spacing");
        if (StringUtils.isEmpty(spacing)) {
            int ok = JOptionPane.showConfirmDialog(this, "<html>"
                            + I18.get("tool_prefs_spacing") + "</html>", I18.get("tool_prefs_spacing_title"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) {
                prop.setProperty("correct-uncommon-spacing", "before");
            } else {
                prop.setProperty("correct-uncommon-spacing", "after");
            }
            store = true;
        }
        if (StringUtils.isEmpty(prop.getProperty("options_dir_refresh"))) {
            int option = JOptionPane.showConfirmDialog(null, I18.get("options_dir_refresh_confirm"), I18.get(
                    "options_dir_refresh"), JOptionPane.OK_CANCEL_OPTION);
            prop.setProperty("options_dir_refresh", option == JOptionPane.OK_OPTION ? "true" : "false");
            store = true;
        }
        if (store) {
            prop.store();
        }
        if (edit) {
            if (txtFiles.size() > 0) {
                actions.openFiles(txtFiles, false);
            } else if (dirFile != null) {
                actions.openFiles(dirFile, false);
            } else {
                actions.closeAllTables();
            }
            actions.setView(YassActions.VIEW_EDIT);
            sheet.requestFocusInWindow();
            return;
        }
        actions.setView(YassActions.VIEW_LIBRARY);

        songList.load();
        songList.filter(null);
        songList.focusFirstVisible();
    }

    public void stop() {
        askStop();
    }

    private boolean askStop() {
        if (actions.cancelOpen()) {
            return false;
        }
        actions.storeRecentFiles();
        songList.setDefaults();
        prop.store();

        songList.interrupt();
        return true;
    }

    private JPanel createSheetPanel() {
        JPanel sheetPanel = new JPanel(new BorderLayout());
        JScrollPane sheetPane = new JScrollPane(sheet);

        sheetPane.getViewport().addChangeListener(e -> {
                    JViewport v = (JViewport) e.getSource();
                    Point p = v.getViewPosition();
                    Dimension r = v.getExtentSize();

                    // LYRICS POSITION
                    String layout = prop.getProperty("editor-layout");
                    if (layout == null) {
                        layout = "East";
                    }

                    String lyricsWidthString = prop.getProperty("lyrics-width");
                    int lyricsWidth = Integer.parseInt(lyricsWidthString);

                    int newx = (int) p.getX() + r.width - lyricsWidth;
                    if (layout.equals("East")) {
                        newx = (int) p.getX() + r.width - lyricsWidth;
                    } else if (layout.equals("West")) {
                        newx = (int) p.getX();
                    }
                    int newy = (int) p.getY() + 20;
                    Point p2 = lyrics.getLocation();
                    if (p2.x != newx || p2.y != newy) {
                        lyrics.setLocation(newx, newy);
                        sheet.revalidate();
                        sheet.update();
                    }
                });
        sheetPane.getViewport().addComponentListener(
                new ComponentAdapter() {
                    public void componentResized(ComponentEvent e) {
                        actions.stopPlaying();

                        JViewport v = (JViewport) e.getSource();
                        Point p = v.getViewPosition();
                        Dimension r = v.getExtentSize();

                        // LYRICS POSITION
                        String layout = prop.getProperty("editor-layout");
                        if (layout == null) {
                            layout = "East";
                        }

                        String lyricsWidthString = prop.getProperty("lyrics-width");
                        int lyricsWidth = Integer.parseInt(lyricsWidthString);

                        int newx = (int) p.getX() + r.width - lyricsWidth;
                        if (layout.equals("East")) {
                            newx = (int) p.getX() + r.width - lyricsWidth;
                        } else if (layout.equals("West")) {
                            newx = (int) p.getX();
                        }
                        int newy = (int) p.getY() + 20;
                        Point p2 = lyrics.getLocation();
                        if (p2.x != newx || p2.y != newy) {
                            lyrics.setLocation(newx, newy);
                            sheet.revalidate();
                            sheet.update();
                        } else {
                            sheet.updateHeight();
                            actions.revalidateLyricsArea();
                        }
                        YassTable t = actions.getTable();
                        if (t != null) t.zoomPage();
                    }
                });


        sheetPane.setWheelScrollingEnabled(false);
        sheetPane.addMouseWheelListener(e -> {
            if (sheet.isPlaying() || sheet.isTemporaryStop()) {
                return;
            }
            int notches = e.getWheelRotation();
            if (notches == 0) {
                return;
            }
            actions.getTable().gotoPage(notches < 0 ? -1 : 1);
        });

        sheetPanel.add("North", editToolbar = actions.createFileEditToolbar());
        sheetPanel.add("Center", sheetPane);

        YassSheetInfo sheetInfo = new YassSheetInfo(sheet, 0);
        sheetInfoPanel = new JPanel(new GridLayout(1,1));
        sheetInfoPanel.add(sheetInfo);
        sheetPanel.add("South", sheetInfoPanel);

        // dark mode buttons
        Border emptyBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                BorderFactory.createEmptyBorder(4,4,4,4));
        Border rolloverBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                BorderFactory.createEmptyBorder(4,4,4,4));
        for (Component c: editToolbar.getComponents()) {
            if (c instanceof JButton) {
                ((JButton) c).getModel().addChangeListener(e -> {
                    ButtonModel model = (ButtonModel) e.getSource();
                    c.setBackground(model.isRollover()
                            ? (YassSheet.BLUE)
                            : (sheet.darkMode ? YassSheet.HI_GRAY_2_DARK_MODE : YassSheet.HI_GRAY_2));
                    ((JButton) c).setBorder(model.isRollover() ? rolloverBorder : emptyBorder);
                });
            }
            if (c instanceof JToggleButton) {
                ((JToggleButton) c).getModel().addChangeListener(e -> {
                    ButtonModel model = (ButtonModel) e.getSource();
                    c.setBackground(model.isRollover()
                            ? (YassSheet.BLUE)
                            : (sheet.darkMode ? YassSheet.HI_GRAY_2_DARK_MODE : YassSheet.HI_GRAY_2));
                    ((JToggleButton) c).setBorder(model.isRollover() ? rolloverBorder : emptyBorder);
                });
            }
        }

        sheet.addYassSheetListener(new YassSheetListener() {
            @Override
            public void posChanged(YassSheet source, double posMs) { }
            @Override
            public void rangeChanged(YassSheet source, int minHeight, int maxHeight, int minBeat, int maxBeat) { }
            @Override
            public void propsChanged(YassSheet source) {
                editToolbar.setBackground(sheet.darkMode ? YassSheet.HI_GRAY_2_DARK_MODE : YassSheet.HI_GRAY_2);
                editToolbar.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
                for (Component c: editToolbar.getComponents()) {
                    if (c instanceof JButton || c instanceof JToggleButton) {
                        c.setBackground(sheet.darkMode ? YassSheet.HI_GRAY_2_DARK_MODE : YassSheet.HI_GRAY_2);
                        ((JComponent) c).setBorder(emptyBorder);
                    }
                }
            }
        });
        editToolbar.setBackground(sheet.darkMode ? YassSheet.HI_GRAY_2_DARK_MODE : YassSheet.HI_GRAY_2);
        editToolbar.setBorder(BorderFactory.createLineBorder(sheet.darkMode ? YassSheet.HI_GRAY_2_DARK_MODE :
                YassSheet.HI_GRAY_2, 3));

        sheetPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sheetPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        sheetPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        sheetPane.setBorder(null);
        sheetPane.getActionMap().clear();

        return sheetPanel;
    }

    public JPanel createSongListPanel() {
        JScrollPane songScroll = new JScrollPane(songList = new YassSongList(actions));
        songScroll.setOpaque(false);
        songScroll.getViewport().setOpaque(false);
        songScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JScrollPane groupsScroll = new JScrollPane(groups = new YassGroups(songList));
        groupsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        final JComboBox<String> groupsBox = actions.createGroupsBox();
        groupsBox.setPreferredSize(new Dimension(120, 40));

        actions.createFilterBox();

        actions.setGroups(groups);
        actions.setSongList(songList);

        JPanel toolPanel3 = new JPanel(new BorderLayout());
        JPanel toolPanel2 = new JPanel(new BorderLayout());
        toolPanel3.add("West", actions.createSongListToolbar());
        toolPanel3.add("East", actions.createFilterToolbar());
        toolPanel3.add("Center", toolPanel2);
        toolPanel = new JPanel(new BorderLayout());

        toolPanel2.add("Center", toolPanel);
        groups.setToolBar("title", (JToolBar) actions.createTitleToolbar());
        groups.setToolBar("artist", (JToolBar) actions.createArtistToolbar());
        groups.setToolBar("genre", (JToolBar) actions.createGenreToolbar());
        groups.setToolBar("edition", (JToolBar) actions.createEditionToolbar());
        groups.setToolBar("language", (JToolBar) actions.createLanguageToolbar());
        groups.setToolBar("year", (JToolBar) actions.createYearToolbar());
        groups.setToolBar("length", (JToolBar) actions.createLengthToolbar());
        groups.setToolBar("album", (JToolBar) actions.createAlbumToolbar());
        groups.setToolBar("playlist", (JToolBar) actions.createPlaylistToolbar());
        groups.setToolBar("duets", (JToolBar) actions.createDuetsToolbar());
        groups.setToolBar("files", (JToolBar) actions.createFilesToolbar());
        groups.setToolBar("folder", (JToolBar) actions.createFolderToolbar());
        groups.setToolBar("errors", (JToolBar) actions.createErrorsToolbar());
        toolPanel.add("Center", groups.getToolBar(0));

        groupsPanel = new JPanel(new BorderLayout());
        groupsPanel.add("Center", groupsScroll);

        groupsBox.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            int i = groupsBox.getSelectedIndex();
            groups.setGroups(i);
            toolPanel.removeAll();
            toolPanel.add("Center", groups.getToolBar(i));
            toolPanel.validate();
            toolPanel.repaint();
        }));
        groups.addPropertyChangeListener(p -> {
            if (!p.getPropertyName().equals("group")) {
                return;
            }
            String o = (String) p.getOldValue();
            String n = (String) p.getNewValue();
            if (o.equals("errors")) {
                songList.showErrors(false);
            }
            if (o.equals("stats")) {
                songList.showStats(false);
            }
            if (n.equals("errors")) {
                songList.showStats(false);
                songList.showErrors(true);
            }
            if (n.equals("stats")) {
                songList.showErrors(false);
                songList.showStats(true);
            }
        });
        groupsPanel.add("North", groupsBox);

        songInfo = new YassSongInfo(prop, actions);
        actions.setSongInfo(songInfo);

        playList = new YassPlayList(actions, prop, null, songList);
        playList.setOpaque(false);

        playlistPanel = new JPanel(new BorderLayout());
        playlistPanel.add("Center", playList);
        playlistPanel.setOpaque(false);
        //playlistPanel.add("West", actions.createListTransferToolbar());
        playlistPanel.add("North", actions.createPlayListToolbar());

        songPanel = new JPanel(new BorderLayout());
        songPanel.add("Center", songScroll);
        songPanel.setOpaque(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", toolPanel3);
        panel.add("Center", songInfo);

        songList.addKeyListener(
                new KeyAdapter() {
                    public void keyReleased(KeyEvent e) {
                        int c = e.getKeyCode();
                        if (c == KeyEvent.VK_RIGHT && e.getModifiersEx() == 0 && playList.getList().getRowCount() > 0) {
                            playList.getList().requestFocus();
                            YassSong s = playList.getList().getFirstSelectedSong();
                            if (s == null) {
                                playList.getList().selectSong(0);
                            } else {
                                songInfo.setSong(s);
                                songInfo.repaint();
                            }
                            songList.repaint();
                            playList.repaint();
                        }
                        if (c == KeyEvent.VK_LEFT && e.getModifiersEx() == 0) {
                            groups.requestFocus();
                            songList.repaint();
                            groups.repaint();
                        }
                    }
                });
        playList.getList().addKeyListener(
                new KeyAdapter() {
                    public void keyReleased(KeyEvent e) {
                        int c = e.getKeyCode();
                        if (c == KeyEvent.VK_LEFT && e.getModifiersEx() == 0) {
                            if (songList.getRowCount() > 0) {
                                songList.requestFocus();
                                YassSong s = songList.getFirstSelectedSong();
                                if (s == null) {
                                    songList.selectSong(0);
                                } else {
                                    songInfo.setSong(s);
                                    songInfo.repaint();
                                }
                                songList.repaint();
                                playList.repaint();
                            } else {
                                groups.requestFocus();
                                playList.repaint();
                                groups.repaint();
                            }
                        }
                        if (c == KeyEvent.VK_RIGHT && e.getModifiersEx() == 0) {
                            actions.getPlayListBox().requestFocus();
                            playList.repaint();
                            actions.getPlayListBox().repaint();
                        }
                    }
                });

        groups.addKeyListener(
                new KeyAdapter() {
                    public void keyReleased(KeyEvent e) {
                        int c = e.getKeyCode();
                        if (c == KeyEvent.VK_RIGHT && e.getModifiersEx() == 0) {
                            if (songList.getRowCount() > 0) {
                                songList.requestFocus();
                                YassSong s = songList.getFirstSelectedSong();
                                if (s == null) {
                                    songList.selectSong(0);
                                } else {
                                    songInfo.setSong(s);
                                    songInfo.repaint();
                                }
                            } else if (playList.getList().getRowCount() > 0) {
                                playList.getList().requestFocus();
                                YassSong s = playList.getList().getFirstSelectedSong();
                                if (s == null) {
                                    playList.getList().selectSong(0);
                                } else {
                                    songInfo.setSong(s);
                                    songInfo.repaint();
                                }
                            }
                            songList.repaint();
                            playList.repaint();
                        }
                        if (c == KeyEvent.VK_LEFT && e.getModifiersEx() == 0) {
                            groupsBox.requestFocus();
                        }
                    }
                });
        groupsBox.addKeyListener(
                new KeyAdapter() {
                    public void keyReleased(KeyEvent e) {
                        int c = e.getKeyCode();
                        if (c == KeyEvent.VK_RIGHT && e.getModifiersEx() == 0) {
                            groups.requestFocus();
                            groups.repaint();
                        }
                        if (c == KeyEvent.VK_LEFT && e.getModifiersEx() == 0) {
                            groups.requestFocus();
                            groups.repaint();
                        }
                    }
                });
        actions.getPlayListBox().addKeyListener(
                new KeyAdapter() {
                    public void keyReleased(KeyEvent e) {
                        int c = e.getKeyCode();
                        if ((c == KeyEvent.VK_RIGHT || c == KeyEvent.VK_LEFT) && e.getModifiersEx() == 0) {
                            if (playList.getList().getRowCount() > 0) {
                                playList.getList().requestFocus();
                                YassSong s = playList.getList().getFirstSelectedSong();
                                if (s == null) {
                                    playList.getList().selectSong(0);
                                } else {
                                    songInfo.setSong(s);
                                    songInfo.repaint();
                                }
                                playList.repaint();
                            }
                        }
                    }
                });

        songList.getSelectionModel().addListSelectionListener(e -> {
            YassSong s = songList.getFirstSelectedSong();
            boolean isEmpty = songList.getRowCount() < 1;

            if (s != null) {
                playList.repaint();
            }
            if (s != null && songList.getShowLyrics() && s.getLyrics() == null) {
                songList.loadSongDetails(s, new YassTable());
            }

            if (s != null || isEmpty) {
                songInfo.setSong(s);
                songInfo.repaint();
            }
        });
        playList.getList().getSelectionModel().addListSelectionListener(e -> {
            YassSong s = playList.getList().getFirstSelectedSong();
            boolean isEmpty = playList.getList().getRowCount() < 1;

            if (s == null) {
                return;
            }

            songList.repaint();
            if (songList.getShowLyrics() && s.getLyrics() == null) {
                songList.loadSongDetails(s, new YassTable());
            }
            if (isEmpty) {
                songInfo.setSong(s);
                songInfo.repaint();
            }
        });

        songInfo.add(playlistPanel, 0);
        songInfo.add(songPanel, 0);
        songInfo.add(groupsPanel, 0);
        songInfo.validate();

        songInfo.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int ww = songInfo.getWidth();
                int hh = songInfo.getHeight();

                int bb = 10;
                int w2 = groupsPanel.getPreferredSize().width;

                groupsPanel.setBounds(bb, bb, w2, hh - 20);

                int min = 200;
                int max = 500;
                int sw = ww / 4;
                sw = Math.max(min, Math.min(sw, max));
                songPanel.setBounds(bb + w2 + bb, bb, sw, hh - 20);

                int w3 = ww - bb - 250;
                songInfo.updateTextBounds(w3, bb + 250 + bb + 32, 250, hh - 250 - 3 * bb - 32);

                int w4 = bb + w2 + bb + sw + bb;
                playlistPanel.setBounds(w4, bb, 200, hh - 20);

                actions.updateDetails();
            }
        });

        actions.registerLibraryActions(panel);
        return panel;
    }
    private boolean refreshLibrary() {
        if (prop.getBooleanProperty("options_dir_refresh") && actions != null) {
            actions.refreshLibrary();
            return true;
        }
        return false;
    }
}

