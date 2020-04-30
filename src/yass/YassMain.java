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

import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassMain extends JApplet {
    private static final long serialVersionUID = 777825370133814283L;

    public static boolean NO_GAME = true;
    private static boolean PRE_LOAD_FOBS = false;

    private static boolean convert = false;
    private static boolean edit = false;
    private static boolean play = false;
    private static String midiFile = null;
    private static String txtFile = null;

    private YassProperties prop;
    private YassSheet sheet = null;
    private YassSheetInfo sheetInfo = null;
    private YassActions actions = null;
    private YassPlayer mp3 = null;
    private YassLyrics lyrics = null;
    private YassSongList songList = null;
    private YassSongInfo songInfo = null;
    private YassPlayList playList = null;
    private YassGroups groups = null;
    private JPanel toolPanel = null;
    private JPanel groupsPanel, songPanel, playlistPanel;

    public static void main(String argv[]) {
        checkAudio();
        loadFobs();
        initLater(argv);
    }

    private static void initLater(final String[] argv) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        final YassMain y = new YassMain();
                        y.parseCommandLine(argv);

                        System.out.println("Init...");
                        y.init();
                        System.out.println("Inited.");

                        y.initConvert();

                        y.onShow();
                        y.setDefaultSize(y.getDefaultSize());

                        System.out.println("Starting...");
                        y.start();
                        System.out.println("Loading...");
                        y.load();

                        y.initFrame();
                        System.out.println("Ready. Let's go.");
                    }
                });
    }

    private static void loadFobs() {
        if (PRE_LOAD_FOBS) {
            System.out.println("Library path: " + System.getProperty("java.library.path"));
            System.loadLibrary("fobs4jmf");
        }
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
            //e.printStackTrace();
        }
    }

    private void initFrame() {
        final JFrame frame = new JFrame(I18.get("yass_title"));
        URL icon16 = YassMain.this.getClass().getResource("/yass/resources/img/yass-icon-16.png");
        URL icon32 = YassMain.this.getClass().getResource("/yass/resources/img/yass-icon-32.png");
        URL icon48 = YassMain.this.getClass().getResource("/yass/resources/img/yass-icon-48.png");
        ArrayList<Image> icons = new ArrayList<Image>();
        icons.add(new ImageIcon(icon48).getImage());
        icons.add(new ImageIcon(icon32).getImage());
        icons.add(new ImageIcon(icon16).getImage());
        frame.setIconImages(icons);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(
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
        frame.add("Center", this);
        frame.pack();
        frame.setSize(getDefaultSize());

        Point p = getDefaultLocation();
        if (p != null)
            frame.setLocation(p);
        else
            frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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

    public Point getDefaultLocation() {
        String x = prop.getProperty("frame-x");
        String y = prop.getProperty("frame-y");
        if (x == null || y == null)
            return null;
        return new Point(new Integer(x), new Integer(y));
    }

    public void setDefaultLocation(Point p) {
        prop.setProperty("frame-x", p.x + "");
        prop.setProperty("frame-y", p.y + "");
    }

    public Dimension getDefaultSize() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        String w = prop.getProperty("frame-width");
        if (w == null)
            w = dim.width >= 1000 ? "1000" : dim.width + "";
        else if (Integer.parseInt(w) > dim.width)
            w = dim.width + "";

        String h = prop.getProperty("frame-height");
        if (h == null)
            h = dim.height >= 600 ? "600" : dim.height + "";
        else if (Integer.parseInt(h) > dim.height)
            w = dim.height + "";

        return new Dimension(new Integer(w), new Integer(h));
    }

    public void setDefaultSize(Dimension d) {
        prop.setProperty("frame-width", d.width + "");
        prop.setProperty("frame-height", d.height + "");

        actions.interruptPlay();

        sheet.updateHeight();
        actions.revalidateLyricsArea();
        sheet.update();
        sheet.repaint();
    }

    public void init() {
        prop = new YassProperties();
        YassVideoUtils.TRY_TO_USE_FOBS = prop.getBooleanProperty("use-fobs");

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

        sheetInfo = new YassSheetInfo(sheet);

        if (!NO_GAME) {
            yass.screen.YassScreen.loadPlugins(prop);
            yass.screen.YassScreen.addScreenChangeListener(
                    new yass.screen.ScreenChangeListener() {
                        public void screenChanged(yass.screen.ScreenChangeEvent e) {
                            actions.gotoScreen(e.getNewScreen());
                        }
                    });
        }

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

        actions.setPanels(this, mainPanel, songListPanel, songInfo, groupsPanel, songPanel, playlistPanel, sheetPanel);

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
            int ok = JOptionPane.showConfirmDialog(null, "<html>" + I18.get("incompatible_version") + "<br>" + dir + "<br><br>" + I18.get("remove_version"), I18.get("incompatible_version") + " - Yass", JOptionPane.OK_CANCEL_OPTION);
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

    public void parseCommandLine(String argv[]) {
        if (argv == null)
            return;

        for (String arg : argv) {
            String low = arg.toLowerCase();
            if (low.equals("-convert"))
                convert = true;
            else if (low.equals("-play"))
                play = true;
            else if (low.equals("-edit"))
                edit = true;
            else if (low.endsWith(".mid") || low.endsWith(".midi") || low.endsWith(".kar"))
                midiFile = arg;
            else if (low.endsWith(".txt")) {
                edit = true;
                txtFile = arg;
            } else if (new File(arg).isDirectory()) {
                edit = true;
                txtFile = arg;
            }
        }
    }

    public void load() {
        String s = prop.getProperty("welcome");
        if (s != null && s.equals("true")) {
            new YassLibOptions(prop, actions, songList, mp3);
            prop.setProperty("welcome", "false");
            prop.store();
        }

        if (edit) {
            if (txtFile != null) {
                actions.openFiles(txtFile);
            } else {
                actions.closeAllTables();
            }
            actions.setView(YassActions.VIEW_EDIT);
            sheet.requestFocusInWindow();
            return;
        }

        if (play && !NO_GAME) {
            songList.addSongListListener(
                    new YassSongListListener() {
                        public void stateChanged(YassSongListEvent e) {
                            int state = e.getState();
                            if (state == YassSongListEvent.STARTING) {
                                yass.screen.YassScreen.setLoading(true);
                                yass.screen.YassScreen.getCurrentScreen().stopJukebox();
                            }
                            if (state == YassSongListEvent.LOADED) {
                                groups.refreshCounters();
                                yass.screen.YassScreen.setSongData(songList.getSongData());
                                yass.screen.YassScreen.setGroupData(groups.toScreenGroups());
                                yass.screen.YassScreen.setLoading(false);
                                yass.screen.YassScreen.getCurrentScreen().startJukebox();
                            }
                        }
                    });
            yass.screen.YassScreen.setCurrentScreen("start");
            actions.setView(YassActions.VIEW_SCREEN);
            yass.screen.YassScreen.getCurrentScreen().stopJukebox();
            yass.screen.YassScreen.getCurrentScreen().requestFocusInWindow();
            yass.screen.YassScreen.setLoading(true);
            SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            String songdir = prop.getProperty("song-directory");
                            if (songdir == null || !new File(songdir).exists()) {
                                yass.screen.YassScreen.getCurrentScreen().loadSongs();
                            } else {
                                songList.load();
                                songList.filter(null);
                            }
                        }
                    });
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

    public boolean askStop() {
        if (actions.cancelOpen()) {
            return false;
        }

        mp3.closeMP3();
        YassTable t = actions.firstTable();
        if (t != null) {
            prop.setProperty("recent-file", t.getDir() + File.separator + t.getFilename());
        } else {
            prop.setProperty("recent-file", "");
        }
        songList.setDefaults();
        prop.store();

        songList.interrupt();
        return true;
    }

    public JPanel createSheetPanel() {
        JPanel sheetPanel = new JPanel(new BorderLayout());
        JScrollPane sheetPane = new JScrollPane(sheet);

        sheetPane.getHorizontalScrollBar().addMouseListener(
                new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        YassTable t = sheet.getActiveTable();
                        if (t != null && t.getMultiSize() == 1) {
                            YassTable.setZoomMode(YassTable.ZOOM_MULTI);
                            sheet.enablePan(false);
                            actions.revalidateLyricsArea();
                            sheet.repaint();
                        }
                    }
                });
        sheetPane.getViewport().addChangeListener(
                new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
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
                    }
                });


        sheetPane.setWheelScrollingEnabled(false);
        sheetPane.addMouseWheelListener(
                new MouseWheelListener() {
                    public void mouseWheelMoved(MouseWheelEvent e) {
                        if (sheet.isPlaying() || sheet.isTemporaryStop()) {
                            return;
                        }
                        int notches = e.getWheelRotation();
                        if (notches == 0) {
                            return;
                        }
                        actions.getTable().gotoPage(notches < 0 ? -1 : 1);
                    }
                });

        JComponent t;
        sheetPanel.add("North", t = actions.createFileEditToolbar());
        sheetPanel.add("Center", sheetPane);
        sheetPanel.add("South", sheetInfo);

        sheet.addYassSheetListener(new YassSheetListener() {
            @Override
            public void posChanged(YassSheet source, double posMs) { }
            @Override
            public void rangeChanged(YassSheet source, int minHeight, int maxHeight, int minBeat, int maxBeat) { }
            @Override
            public void propsChanged(YassSheet source) {
                t.setBackground(sheet.darkMode ? sheet.hiGray2DarkMode : sheet.hiGray2);
                t.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
                /* todo dark mode buttons
                Border emptyBorder = BorderFactory.createCompoundBorder(
                        BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                        BorderFactory.createEmptyBorder(4,4,4,4));
                Border rolloverBorder = BorderFactory.createCompoundBorder(
                        BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                        BorderFactory.createEmptyBorder(4,4,4,4));
                for (Component c: t.getComponents()) {
                    c.setBackground(sheet.darkMode ? sheet.hiGray : sheet.hiGray2);
                    if (c instanceof JButton)
                        ((JButton)c).getModel().addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                ButtonModel model = (ButtonModel) e.getSource();
                                c.setBackground(model.isRollover()
                                        ? (sheet.darkMode ? sheet.blue : sheet.blue)
                                        : (sheet.darkMode ? sheet.hiGray2DarkMode : sheet.hiGray2));
                                ((JButton)c).setBorder(model.isRollover() ? rolloverBorder : emptyBorder);
                            }
                        });
                }*/
            }
        });
        t.setBackground(sheet.darkMode ? sheet.hiGray2DarkMode : sheet.hiGray2);
        t.setBorder(BorderFactory.createLineBorder(sheet.darkMode ? sheet.hiGray2DarkMode : sheet.hiGray2, 3));

        sheetPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sheetPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        sheetPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        sheetPane.setBorder(null);

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

        groupsBox.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        SwingUtilities.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        int i = groupsBox.getSelectedIndex();
                                        groups.setGroups(i);
                                        toolPanel.removeAll();
                                        toolPanel.add("Center", groups.getToolBar(i));
                                        toolPanel.validate();
                                        toolPanel.repaint();
                                    }
                                });
                    }
                });
        groups.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent p) {
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
        actions.setLibTools(toolPanel3);

        songList.addKeyListener(
                new KeyAdapter() {
                    public void keyReleased(KeyEvent e) {
                        int c = e.getKeyCode();
                        if (c == KeyEvent.VK_RIGHT && e.getModifiers() == 0 && playList.getList().getRowCount() > 0) {
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
                        if (c == KeyEvent.VK_LEFT && e.getModifiers() == 0) {
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
                        if (c == KeyEvent.VK_LEFT && e.getModifiers() == 0) {
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
                        if (c == KeyEvent.VK_RIGHT && e.getModifiers() == 0) {
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
                        if (c == KeyEvent.VK_RIGHT && e.getModifiers() == 0) {
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
                        if (c == KeyEvent.VK_LEFT && e.getModifiers() == 0) {
                            groupsBox.requestFocus();
                        }
                    }
                });
        groupsBox.addKeyListener(
                new KeyAdapter() {
                    public void keyReleased(KeyEvent e) {
                        int c = e.getKeyCode();
                        if (c == KeyEvent.VK_RIGHT && e.getModifiers() == 0) {
                            groups.requestFocus();
                            groups.repaint();
                        }
                        if (c == KeyEvent.VK_LEFT && e.getModifiers() == 0) {
                            groups.requestFocus();
                            groups.repaint();
                        }
                    }
                });
        actions.getPlayListBox().addKeyListener(
                new KeyAdapter() {
                    public void keyReleased(KeyEvent e) {
                        int c = e.getKeyCode();
                        if ((c == KeyEvent.VK_RIGHT || c == KeyEvent.VK_LEFT) && e.getModifiers() == 0) {
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

        songList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
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
            }
        });
        playList.getList().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
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
}

