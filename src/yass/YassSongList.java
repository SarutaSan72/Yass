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

import org.tritonus.share.sampled.file.TAudioFileFormat;
import yass.filter.YassFilter;
import yass.stats.YassStats;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class YassSongList extends JTable {
    public final static int TILE = 0;
    public final static int DETAILS = 1;

    public final static int ERRORS = 2;

    public final static int SYMBOL = 3;
    private int options = SYMBOL;
    public static ImageIcon brokenSong = null, noCover = null;

    public static Image openedIcon = null, videoIcon = null, nobackgroundIcon = null, perfectIcon = null, lockedIcon = null;

    public static String[] langArray, langID;

    public static Image[] langIcons = null;

    public static Image err_minorpage_icon = null, err_major_icon = null, err_file_icon = null, err_tags_icon = null, err_text_icon = null;

    public static int COMPLETE_COLUMN = 1;

    public static int ARTIST_COLUMN = 2;

    public static int TITLE_COLUMN = 3;

    public static int DUETSINGER_COLUMN = 4;

    public static int GENRE_COLUMN = 5;

    public static int EDITION_COLUMN = 6;

    public static int LANGUAGE_COLUMN = 7;

    public static int YEAR_COLUMN = 8;

    public static int FOLDER_COLUMN = 9;

    public static int ALBUM_COLUMN = 10;

    public static int LENGTH_COLUMN = 11;

    public static int ID_COLUMN = 12;

    public int FIRST_MSG_COLUMN = 13;

    public int FIRST_STATS_COLUMN = -1;
    private JTableHeader header = null;
    private String imageCacheName = null;
    private final Action editTitle = new AbstractAction(I18.get("lib_title")) {
                public void actionPerformed(ActionEvent e) {
                    setTitle();
                }
            };
    private final Action editArtist = new AbstractAction(I18.get("lib_artist")) {
                public void actionPerformed(ActionEvent e) {
                    setArtist();
                }
            };
    private final Action editYear = new AbstractAction(I18.get("lib_year")) {
                public void actionPerformed(ActionEvent e) {
                    setYear();
                }
            };
    Action editAlbum = new AbstractAction(I18.get("lib_album")) {
                public void actionPerformed(ActionEvent e) {
                    setAlbum();
                }
            };
    Action editLength = new AbstractAction(I18.get("lib_length")) {
                public void actionPerformed(ActionEvent e) {
                    setLength();
                }
            };
    Action editID = new AbstractAction(I18.get("lib_id")) {
                public void actionPerformed(ActionEvent e) {
                    setID();
                }
            };
    Action createNewEdition = new AbstractAction(I18.get("lib_edition_set")) {
                public void actionPerformed(ActionEvent e) {
                    newEdition();
                }
            };
    Action setEditionToFolder = new AbstractAction(I18.get("lib_edition_folder")) {
                public void actionPerformed(ActionEvent e) {
                    setEditionToFolder();
                }
            };
    Color b1col = UIManager.getColor("Table.gridColor");
    Border b1 = BorderFactory.createLineBorder(b1col, 1);
    Color selcol = UIManager.getColor("Table.selectionBackground");
    Border b2 = BorderFactory.createLineBorder(selcol, 2);
    Border b5 = BorderFactory.createLineBorder(Color.black, 1);

    Border focusLostBorder = BorderFactory.createLineBorder(UIManager.getColor("Label.background"), 5);
    Border focusGainedBorder = BorderFactory.createLineBorder(selcol, 5);

    Action openAction = null, printAction = null, dirAction = null, autoAction = null;
    int extraCol = -1;
    Hashtable<String, Vector<String>> lang_articles = null;
    Vector<YassSongListListener> listeners = null;
    Action storeAction = null, undoallAction = null;
    boolean preventInteraction = false;
    YassFilter prefilter = null;
    FilterThread filterThread = null;
    private YassSongListModel sm;
    private final YassAutoCorrect auto;
    private final YassProperties prop;
    private WorkerThread worker = null;
    private CorrectorThread corrector = null;
    private final YassActions actions;
    private long lastTime = -1;
    private String lastTimeString = "";
    private final Font big = new Font("SansSerif", Font.BOLD, 22);
    private final int ICON_WIDTH = 56;
    private final int ICON_HEIGHT = 56;
    private boolean dirChanged = false;
    private final Color hi = new Color(245, 245, 245);
    private final Color red = new Color(1f, .8f, .8f);
    private final Color redsel = new Color(1f, .8f, .9f);
    private final Color hired = new Color(1f, .9f, .9f);
    private final String playlistFileType, songFileType;
    private Vector<String> pldata = null;
    private int sortbyCol = -1;
    private final JPopupMenu combinedPopup;
    private final Vector<String> editions;
    private JMenu moreEditions, cmoreEditions;
    private final ActionListener actionEdition;
    private final JMenu languagePopup, genrePopup, editionPopup, sortbyPopup;
    private String emptyString = I18.get("songlist_msg_empty");
    private boolean showErrors = false;
    private boolean showStats = false;
    private boolean showLyrics = false;
    private boolean imgShown = true;
    private boolean loaded = false;
    private StoreThread storeThread = null;
    private boolean moveArticles = true;
    private boolean renderLocked = true;
    private Vector<YassSong> allData = null;
    private boolean filterAll = false;
    private Hashtable<String, String> lyricsCache = null;
    private YassFileUtils fileUtils;
    /**
     * Constructor for the YassSongList object
     *
     * @param a Description of the Parameter
     */
    public YassSongList(YassActions a) {
        actions = a;
        auto = a.getAutoCorrect();
        prop = a.getProperties();

        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setShowGrid(false);
        // JDK 1.6 specific
        // setFillsViewportHeight(true);

        songFileType = prop.getProperty("song-filetype");
        playlistFileType = prop.getProperty("playlist-filetype");

        imageCacheName = prop.getProperty("songlist-imagecache");

        moveArticles = prop.get("use-articles").equals("true");

        YassSong.ordering = YassSong.SORT_BY_TITLE;
        sortbyCol = TITLE_COLUMN;

        combinedPopup = new JPopupMenu();
        JMenu clang;
        JMenu cgenre;
        JMenu cedition;

        combinedPopup.add(new JMenuItem(editArtist));
        getInputMap().put(KeyStroke.getKeyStroke("F2"), "editArtist");
        getActionMap().put("editArtist", editArtist);

        combinedPopup.add(new JMenuItem(editTitle));
        getInputMap().put(KeyStroke.getKeyStroke("F4"), "editTitle");
        getActionMap().put("editTitle", editTitle);

        combinedPopup.add(cgenre = new JMenu(I18.get("lib_genre")));
        combinedPopup.add(cedition = new JMenu(I18.get("lib_edition")));
        combinedPopup.add(clang = new JMenu(I18.get("lib_language")));

        combinedPopup.add(new JMenuItem(editYear));
        getInputMap().put(KeyStroke.getKeyStroke("F6"), "editYear");
        getActionMap().put("editYear", editYear);

        combinedPopup.add(new JMenuItem(editAlbum));
        getInputMap().put(KeyStroke.getKeyStroke("F3"), "editAlbum");
        getActionMap().put("editAlbum", editAlbum);

        getInputMap().put(KeyStroke.getKeyStroke("HOME"), "selectFirstRow");
        getInputMap().put(KeyStroke.getKeyStroke("END"), "selectLastRow");

        combinedPopup.add(new JMenuItem(editLength));

        combinedPopup.add(new JMenuItem(editID));
        getInputMap().put(KeyStroke.getKeyStroke("F7"), "editID");
        getActionMap().put("editID", editID);

        JMenuItem menuItem;
        combinedPopup.addSeparator();
        combinedPopup.add(menuItem = new JMenuItem(I18.get("lib_undo_selected")));
        menuItem.addActionListener(e -> undoSelection());
        combinedPopup.add(menuItem = new JMenuItem(I18.get("lib_save_selected")));
        menuItem.addActionListener(e -> storeSelection());

		/*
         *  combinedPopup.addSeparator();
		 *  combinedPopup.add(menuItem = new JMenuItem(I18.get("lib_open_folder")));
		 *  menuItem.addActionListener(
		 *  new ActionListener() {
		 *  public void actionPerformed(ActionEvent e) {
		 *  openSongFolder();
		 *  }
		 *  });
		 *  combinedPopup.add(menuItem = new JMenuItem(I18.get("lib_remove")));
		 *  menuItem.addActionListener(
		 *  new ActionListener() {
		 *  public void actionPerformed(ActionEvent e) {
		 *  removeSelectedSongs();
		 *  }
		 *  });
		 */
        languagePopup = new JMenu(I18.get("lib_language"));

        Vector<String> langVector = new Vector<>();
        Vector<String> langIDVector = new Vector<>();
        StringTokenizer languages = new StringTokenizer(prop.getProperty("language-tag"), "|");
        ActionListener al = e -> setSelectionLanguage(e.getActionCommand());

        while (languages.hasMoreTokens()) {
            String s = languages.nextToken();
            String sid = languages.nextToken();

            menuItem = new JMenuItem(s);
            languagePopup.add(menuItem);
            menuItem.addActionListener(al);

            menuItem = new JMenuItem(s);
            clang.add(menuItem);
            menuItem.addActionListener(al);
            langVector.add(s);
            langIDVector.add(sid);
        }

        JMenu more = new JMenu(I18.get("lib_language_more"));
        languagePopup.addSeparator();
        languagePopup.add(more);
        JMenu cmore = new JMenu(I18.get("lib_language_more"));
        clang.addSeparator();
        clang.add(cmore);

        languages = new StringTokenizer(prop.getProperty("language-more-tag"), "|");
        while (languages.hasMoreTokens()) {
            String s = languages.nextToken();
            String sid = languages.nextToken();

            menuItem = new JMenuItem(s);
            more.add(menuItem);
            menuItem.addActionListener(al);

            menuItem = new JMenuItem(s);
            cmore.add(menuItem);
            menuItem.addActionListener(al);
            langVector.add(s);
            langIDVector.add(sid);
        }
        langArray = new String[langVector.size()];
        langID = new String[langVector.size()];
        langIcons = new Image[langVector.size()];
        langVector.toArray(langArray);
        langIDVector.toArray(langID);

        genrePopup = new JMenu(I18.get("lib_genre"));

        StringTokenizer genres = new StringTokenizer(prop.getProperty("genre-tag"), "|");
        ActionListener ag = e -> setSelectionGenre(e.getActionCommand());
        while (genres.hasMoreTokens()) {
            String s = genres.nextToken();

            menuItem = new JMenuItem(s);
            genrePopup.add(menuItem);
            menuItem.addActionListener(ag);

            menuItem = new JMenuItem(s);
            cgenre.add(menuItem);
            menuItem.addActionListener(ag);
        }
        more = new JMenu(I18.get("lib_genre_more"));
        genrePopup.addSeparator();
        genrePopup.add(more);
        cmore = new JMenu(I18.get("lib_genre_more"));
        cgenre.addSeparator();
        cgenre.add(cmore);
        genres = new StringTokenizer(prop.getProperty("genre-more-tag"), "|");
        while (genres.hasMoreTokens()) {
            String s = genres.nextToken();

            if (s.equals("MORE")) {
                JMenu evenmore = new JMenu(I18.get("lib_genre_more"));
                more.addSeparator();
                more.add(evenmore);
                more = evenmore;

                evenmore = new JMenu(I18.get("lib_genre_more"));
                cmore.addSeparator();
                cmore.add(evenmore);
                cmore = evenmore;
            } else {
                menuItem = new JMenuItem(s);
                more.add(menuItem);
                menuItem.addActionListener(ag);

                menuItem = new JMenuItem(s);
                cmore.add(menuItem);
                menuItem.addActionListener(ag);
            }
        }

        editionPopup = new JMenu(I18.get("lib_edition"));

        menuItem = new JMenuItem(I18.get("lib_edition_set"));
        menuItem.addActionListener(e -> newEdition());
        editionPopup.add(menuItem);
        editionPopup.add(setEditionToFolder);
        editionPopup.addSeparator();

        menuItem = new JMenuItem(I18.get("lib_edition_set"));
        menuItem.addActionListener(e -> newEdition());
        cedition.add(menuItem);
        cedition.add(setEditionToFolder);
        cedition.addSeparator();

        actionEdition =e -> setSelectionEdition(e.getActionCommand());

        StringTokenizer edis = new StringTokenizer(prop.getProperty("edition-tag"), "|");
        //editionPopup.addItemListener(itemEdition);
        while (edis.hasMoreTokens()) {
            String s = edis.nextToken();

            menuItem = new JMenuItem(s);
            editionPopup.add(menuItem);
            menuItem.addActionListener(actionEdition);

            menuItem = new JMenuItem(s);
            cedition.add(menuItem);
            menuItem.addActionListener(actionEdition);
        }

        moreEditions = new JMenu(I18.get("lib_edition_more"));
        editionPopup.addSeparator();
        editionPopup.add(moreEditions);
        cmoreEditions = new JMenu(I18.get("lib_edition_more"));
        cedition.addSeparator();
        cedition.add(cmoreEditions);

        editions = new Vector<>();

        sortbyPopup = new JMenu(I18.get("songlist_col_sortby"));
        String[] sortString = new String[]{I18.get("songlist_col_1"), I18.get("songlist_col_2"), I18.get("songlist_col_3"), I18.get("songlist_col_4"),
                I18.get("songlist_col_5"), I18.get("songlist_col_6"), I18.get("songlist_col_7"), I18.get("songlist_col_8"), I18.get("songlist_col_9"),
                I18.get("songlist_col_10"), I18.get("songlist_col_11"), I18.get("songlist_col_12")};
        for (int k = 1; k <= sortString.length; k++) {
            if (k == 4) {
                continue;
            }
            menuItem = new JMenuItem(sortString[k - 1]);
            sortbyPopup.add(menuItem);
            menuItem.addActionListener(new SortByAction(k));
        }

        setModel(sm = new YassSongListModel());
        getTableHeader().setReorderingAllowed(false);
        createDefaultColumnsFromModel();
        setRowHeight(ICON_HEIGHT);

        TableColumn col = null;
        //int cw[] = getDefaultColumnWidth();
        for (int i = 0; i < FIRST_MSG_COLUMN; i++) {
            col = getColumnModel().getColumn(i);
            col.setHeaderRenderer(new DefaultSongHeaderRenderer());
            col.setCellRenderer(new SongRenderer());
            col.setMinWidth(0);
            //col.setPreferredWidth(cw[i]);
        }
        col = getColumnModel().getColumn(ARTIST_COLUMN);
        col.setCellRenderer(new SongArtistRenderer());
        col = getColumnModel().getColumn(TITLE_COLUMN);
        col.setCellRenderer(new SongTitleRenderer());

        for (int i = 0; i < YassRow.ALL_MESSAGES.length; i++) {
            col = getColumnModel().getColumn(FIRST_MSG_COLUMN + i);
            col.setHeaderRenderer(new SongHeaderRenderer());
            col.setCellRenderer(new SongMessageRenderer());
            getColumnModel().getColumn(FIRST_MSG_COLUMN + i).setMinWidth(12);
            getColumnModel().getColumn(FIRST_MSG_COLUMN + i).setMaxWidth(12);
            getColumnModel().getColumn(FIRST_MSG_COLUMN + i).setPreferredWidth(12);
        }

        FIRST_STATS_COLUMN = FIRST_MSG_COLUMN + YassRow.ALL_MESSAGES.length;
        int statsCount = YassStats.length;
        for (int i = 0; i < statsCount; i++) {
            col = getColumnModel().getColumn(FIRST_STATS_COLUMN + i);
            col.setHeaderRenderer(new SongHeaderRenderer());
            col.setCellRenderer(new SongMessageRenderer());
            getColumnModel().getColumn(FIRST_STATS_COLUMN + i).setMinWidth(12);
            getColumnModel().getColumn(FIRST_STATS_COLUMN + i).setMaxWidth(12);
            getColumnModel().getColumn(FIRST_STATS_COLUMN + i).setPreferredWidth(12);
        }

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        addMouseListener(
                new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            actions.playSong();
                            e.consume();
                        }
                    }


                    public void mouseReleased(MouseEvent e) {
                        if (preventInteraction) {
                            return;
                        }

                        boolean isPopup = e.isPopupTrigger() || (e.getModifiers() & InputEvent.BUTTON1_MASK) == 0;
                        if (isPopup && actions.isLibraryLoaded()) {
                            combinedPopup.show(e.getComponent(), e.getX(), e.getY());
                            return;
                        }
                    }

                });
        sm.addTableModelListener(e -> {
            int col1 = e.getColumn();
            if (col1 != ARTIST_COLUMN && col1 != TITLE_COLUMN) {
                return;
            }

            int row = e.getFirstRow();
            YassSong s = sm.getRowAt(row);
            String filename = s.getDirectory() + File.separator + s.getFilename();
            YassTable t = new YassTable();
            t.init(prop);
            t.removeAllRows();
            t.loadFile(filename);
            YassTableModel tm = (YassTableModel) t.getModel();
            if (col1 == ARTIST_COLUMN) {
                YassRow r = tm.getCommentRow("ARTIST:");
                if (r != null) {
                    String a1 = s.getArtist();
                    String olda = r.getComment();
                    if (!a1.equals(olda)) {
                        r.setComment(a1);
                        t.storeFile(filename);
                    }
                }
            } else if (col1 == TITLE_COLUMN) {
                YassRow r = tm.getCommentRow("TITLE:");
                if (r != null) {
                    String a1 = s.getTitle();
                    String olda = r.getComment();
                    if (!a1.equals(olda)) {
                        r.setComment(a1);
                        t.storeFile(filename);
                    }
                }
            }
        });

        // putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        ((DefaultCellEditor) getDefaultEditor(String.class)).setClickCountToStart(10);
        addKeyListener(
                new KeyAdapter() {
                    public void keyTyped(KeyEvent e) {
                        //System.out.println("typed " + e.getKeyChar());
                    }


                    public void keyReleased(KeyEvent e) {
                        //System.out.println("released " + e.getKeyChar());
                    }


                    public void keyPressed(KeyEvent e) {
                        //System.out.println("pressed " + e.getKeyChar());

                        if (e.isConsumed()) {
                            return;
                        }

                        if (isEditing()) {
                            return;
                        }

                        if (e.isControlDown() || e.isAltDown()) {
                            if ((int) e.getKeyChar() == 6) {
                                actions.filterLibrary();
                                e.consume();
                            }
                            return;
                        }

                        if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
                            return;
                        }

                        e.consume();

					/*
					 *  if (e.getKeyChar() == KeyEvent.VK_HOME) {
					 *  setRowSelectionInterval(0,0);
					 *  scrollRectToVisible(getCellRect(0, 0, true));
					 *  return;
					 *  }
					 *  if (e.getKeyChar() == KeyEvent.VK_END) {
					 *  int i = sm.getRowCount();
					 *  setRowSelectionInterval(i,i);
					 *  scrollRectToVisible(getCellRect(i, 0, true));
					 *  return;
					 *  }
					 */
                        if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                            actions.stopPlaySong();
                            return;
                        }
                        if (e.getKeyChar() == KeyEvent.VK_SPACE) {
                            actions.playSong();
                            return;
                        }

                        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                            actions.playSong();
                            return;
                        }

                        if (e.getKeyChar() == KeyEvent.VK_DELETE) {
                            return;
                        }

                        int n = sm.getRowCount();
                        char c = Character.toLowerCase(e.getKeyChar());
                        String cstr = c + "";

                        long currentTime = System.currentTimeMillis();
                        if (currentTime < lastTime + 700) {
                            if (lastTimeString.length() < 3) {
                                cstr = lastTimeString + cstr;
                            }
                            lastTimeString = cstr;

                        } else {
                            lastTimeString = cstr;
                        }
                        lastTime = currentTime;
                        repaint();

                        // select next
                        int k = getSelectionModel().getMinSelectionIndex();
                        if (k >= 0 && k < n - 1) {
                            String a = null;
                            if ((YassSong.ordering & YassSong.SORT_BY_ARTIST) != 0) {
                                a = sm.getRowAt(k).getArtist();
                            } else if ((YassSong.ordering & YassSong.SORT_BY_TITLE) != 0) {
                                a = sm.getRowAt(k).getTitle();
                            } else if ((YassSong.ordering & YassSong.SORT_BY_GENRE) != 0) {
                                a = sm.getRowAt(k).getGenre();
                            } else if ((YassSong.ordering & YassSong.SORT_BY_EDITION) != 0) {
                                a = sm.getRowAt(k).getEdition();
                            } else if ((YassSong.ordering & YassSong.SORT_BY_LANGUAGE) != 0) {
                                a = sm.getRowAt(k).getLanguage();
                            } else if ((YassSong.ordering & YassSong.SORT_BY_FOLDER) != 0) {
                                a = sm.getRowAt(k).getFolder();
                            } else if ((YassSong.ordering & YassSong.SORT_BY_COMPLETE) != 0) {
                                a = sm.getRowAt(k).getComplete();
                            }

                            if (a != null && a.toLowerCase().startsWith(cstr)) {
                                k++;

                                if ((YassSong.ordering & YassSong.SORT_BY_ARTIST) != 0) {
                                    a = sm.getRowAt(k).getArtist();
                                } else if ((YassSong.ordering & YassSong.SORT_BY_TITLE) != 0) {
                                    a = sm.getRowAt(k).getTitle();
                                } else if ((YassSong.ordering & YassSong.SORT_BY_GENRE) != 0) {
                                    a = sm.getRowAt(k).getGenre();
                                } else if ((YassSong.ordering & YassSong.SORT_BY_EDITION) != 0) {
                                    a = sm.getRowAt(k).getEdition();
                                } else if ((YassSong.ordering & YassSong.SORT_BY_LANGUAGE) != 0) {
                                    a = sm.getRowAt(k).getLanguage();
                                } else if ((YassSong.ordering & YassSong.SORT_BY_FOLDER) != 0) {
                                    a = sm.getRowAt(k).getFolder();
                                } else if ((YassSong.ordering & YassSong.SORT_BY_COMPLETE) != 0) {
                                    a = sm.getRowAt(k).getComplete();
                                }
                                if (a != null && a.toLowerCase().startsWith(cstr)) {
                                    setRowSelectionInterval(k, k);
                                    scrollRectToVisible(getCellRect(k, 0, true));
                                    return;
                                }
                            }
                        }

                        // select first
                        int i = 0;
                        while (i < n) {
                            String a = null;
                            if ((YassSong.ordering & YassSong.SORT_BY_ARTIST) != 0) {
                                a = sm.getRowAt(i).getArtist();
                            } else if ((YassSong.ordering & YassSong.SORT_BY_TITLE) != 0) {
                                a = sm.getRowAt(i).getTitle();
                            } else if ((YassSong.ordering & YassSong.SORT_BY_GENRE) != 0) {
                                a = sm.getRowAt(i).getGenre();
                            } else if ((YassSong.ordering & YassSong.SORT_BY_EDITION) != 0) {
                                a = sm.getRowAt(i).getEdition();
                            } else if ((YassSong.ordering & YassSong.SORT_BY_LANGUAGE) != 0) {
                                a = sm.getRowAt(i).getLanguage();
                            } else if ((YassSong.ordering & YassSong.SORT_BY_FOLDER) != 0) {
                                a = sm.getRowAt(i).getFolder();
                            } else if ((YassSong.ordering & YassSong.SORT_BY_COMPLETE) != 0) {
                                a = sm.getRowAt(i).getComplete();
                            }

                            if (a != null && a.toLowerCase().startsWith(cstr)) {
                                break;
                            }
                            //if (a != null && a.toLowerCase().startsWith(cstr.charAt(0) + "")) {
                            //	break;
                            //}
                            if (a != null && a.compareToIgnoreCase(cstr) > 0) {
                                break;
                            }
                            i++;
                        }
                        if (i == n) {
                            return;
                        }
                        setRowSelectionInterval(i, i);
                        scrollRectToVisible(getCellRect(i, 0, true));
                    }
                });
        header = getTableHeader();
        header.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        boolean isPopup = e.isPopupTrigger() || (e.getModifiers() & InputEvent.BUTTON1_MASK) == 0;
                        //if (isPopup)
                            // sortbyPopup.show(e.getComponent(), e.getX(), e.getY());
                    }


                    public void mouseReleased(MouseEvent e) {
                        boolean isPopup = e.isPopupTrigger() || (e.getModifiers() & InputEvent.BUTTON1_MASK) == 0;
                        if (isPopup) {
                            // sortbyPopup.show(e.getComponent(), e.getX(), e.getY());
                            return;
                        }
                        if (preventInteraction)
                            return;

                        int x = e.getX();
                        int y = e.getY();
                        TableColumnModel colModel = getColumnModel();
                        int col = colModel.getColumnIndexAtX(x);
                        Object o = colModel.getColumn(col).getHeaderRenderer();
                        if (o instanceof SongHeaderRenderer) {
                            SongHeaderRenderer hr = (SongHeaderRenderer) o;
                            hr.setSorted(true);
                            getTableHeader().resizeAndRepaint();
                        }

                        if (col < FIRST_MSG_COLUMN) {
                            sortBy(col);
                            getTableHeader().resizeAndRepaint();
                            return;
                        } else if (col < FIRST_STATS_COLUMN) {
                            if (y > 12) {
                                sortBy(col);
                                getTableHeader().resizeAndRepaint();
                            }
                            if (y <= 12 && auto.isAutoCorrectionSafe(YassRow.ALL_MESSAGES[col - FIRST_MSG_COLUMN])) {
                                SongHeaderRenderer hr = (SongHeaderRenderer) colModel.getColumn(col).getHeaderRenderer();
                                boolean onoff = hr.isSelected();
                                hr.setSelected(!onoff);
                                getTableHeader().resizeAndRepaint();
                            }
                        } else {
                            sortBy(col);
                            getTableHeader().resizeAndRepaint();
                        }
                    }
                });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                ((JComponent)getParent().getParent()).setBorder(focusGainedBorder);
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                ((JComponent)getParent().getParent()).setBorder(focusLostBorder);
            }
        });
		/*
		 *  addComponentListener(
		 *  new ComponentAdapter() {
		 *  public void componentResized(ComponentEvent e) {
		 *  setDefaultSize(((Component) e.getSource()).getSize());
		 *  setDefaultLocation(((Component) e.getSource()).getLocation());
		 *  int ww[] = new int[FIRST_MSG_COLUMN + YassRow.ALL_MESSAGES.length];
		 *  for (int i = 0; i < ww.length; i++) {
		 *  ww[i] = getColumnModel().getColumn(i).getWidth();
		 *  }
		 *  setDefaultColumnWidth(ww);
		 *  }
		 *  public void componentMoved(ComponentEvent e) {
		 *  setDefaultSize(((Component) e.getSource()).getSize());
		 *  setDefaultLocation(((Component) e.getSource()).getLocation());
		 *  int ww[] = new int[FIRST_MSG_COLUMN + YassRow.ALL_MESSAGES.length];
		 *  for (int i = 0; i < ww.length; i++) {
		 *  ww[i] = getColumnModel().getColumn(i).getWidth();
		 *  }
		 *  setDefaultColumnWidth(ww);
		 *  }
		 *  });
		 */
        setOptions(TILE);
        showErrors = false;

        try {
            noCover = new ImageIcon(getClass().getResource("/yass/resources/img/NoCover.jpg"));
            brokenSong = new ImageIcon(getClass().getResource("/yass/resources/img/Broken.jpg"));
            openedIcon = new ImageIcon(getClass().getResource("/yass/resources/img/GrayOpen16.gif")).getImage();
            videoIcon = new ImageIcon(getClass().getResource("/yass/resources/img/Video.gif")).getImage();
            nobackgroundIcon = new ImageIcon(getClass().getResource("/yass/resources/img/NoBackground.gif")).getImage();
            perfectIcon = new ImageIcon(getClass().getResource("/yass/resources/img/Perfect.gif")).getImage();
            lockedIcon = new ImageIcon(getClass().getResource("/yass/resources/img/Locked.gif")).getImage();

            for (int i = 0; i < langIcons.length; i++) {
                // System.out.println("language: " + langID[i]);
                langIcons[i] = new ImageIcon(getClass().getResource("/yass/resources/img/"+langID[i].toLowerCase() + ".gif")).getImage();
            }
            err_minorpage_icon = new ImageIcon(getClass().getResource("/yass/resources/img/MinorPageError.gif")).getImage();
            err_major_icon = new ImageIcon(getClass().getResource("/yass/resources/img/MajorError2.gif")).getImage();
            err_file_icon = new ImageIcon(getClass().getResource("/yass/resources/img/FileError.gif")).getImage();
            err_tags_icon = new ImageIcon(getClass().getResource("/yass/resources/img/TagError.gif")).getImage();
            err_text_icon = new ImageIcon(getClass().getResource("/yass/resources/img/TextError.gif")).getImage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the actions attribute of the YassSongList object
     *
     * @return The actions value
     */
    public YassActions getActions() {
        return actions;
    }

    /**
     * Gets the scrollableTracksViewportHeight attribute of the YassSongList
     * object
     *
     * @return The scrollableTracksViewportHeight value
     */
    public boolean getScrollableTracksViewportHeight() {
        return getParent() instanceof JViewport && getPreferredSize().height < getParent().getHeight();
    }

    /**
     * Sets the title attribute of the YassSongList object
     */
    public void setTitle() {
        int i = getSelectedRow();
        if (i < 0) {
            return;
        }

        YassSong s = sm.getRowAt(i);

        String input = s.getTitle();
        String msg = I18.get("mpop_title_msg");
        input = JOptionPane.showInputDialog(actions.getTab(), msg, input);
        if (input == null) {
            return;
        }

        s.setTitle(input);
        s.setSaved(false);
        setSaved(false);
        repaint();
    }

    /**
     * Sets the artist attribute of the YassSongList object
     */
    public void setArtist() {
        int i = getSelectedRow();
        if (i < 0) {
            return;
        }

        Vector<YassSong> sel = getSelectedSongs();
        String input = sm.getRowAt(i).getArtist();
        int n = sel.size();
        String msg = n > 1 ? MessageFormat.format(I18.get("mpop_artist_msg_2"), n) : I18.get("mpop_artist_msg_1");
        input = JOptionPane.showInputDialog(actions.getTab(), msg, input);
        if (input == null) {
            return;
        }
        for (Enumeration<YassSong> en = sel.elements(); en.hasMoreElements(); ) {
            YassSong s = en.nextElement();
            s.setArtist(input);
            s.setSaved(false);

        }
        setSaved(false);
        repaint();

        return;
    }

    /**
     * Sets the album attribute of the YassSongList object
     */
    public void setAlbum() {
        int i = getSelectedRow();
        if (i < 0) {
            return;
        }

        Vector<YassSong> sel = getSelectedSongs();
        String input = sm.getRowAt(i).getAlbum();
        int n = sel.size();
        String msg = n > 1 ? MessageFormat.format(I18.get("mpop_album_msg_2"), n) : I18.get("mpop_album_msg_1");
        input = JOptionPane.showInputDialog(actions.getTab(), msg, input);
        if (input == null) {
            return;
        }
        for (Enumeration<YassSong> en = sel.elements(); en.hasMoreElements(); ) {
            YassSong s = en.nextElement();
            s.setAlbum(input);
            s.setSaved(false);

        }
        setSaved(false);
        repaint();

        return;
    }

    /**
     * Sets the iD attribute of the YassSongList object
     */
    public void setLength() {
        int i = getSelectedRow();
        if (i < 0) {
            return;
        }

        Vector<YassSong> sel = getSelectedSongs();
        String input = sm.getRowAt(i).getLength();
        int n = sel.size();
        String msg = n > 1 ? MessageFormat.format(I18.get("mpop_length_msg_2"), n) : I18.get("mpop_length_msg_1");
        input = JOptionPane.showInputDialog(actions.getTab(), msg, input);
        if (input == null)
            return;
        for (YassSong s: sel) {
            s.setLength(input);
            s.setSaved(false);

        }
        setSaved(false);
        repaint();
    }

    /**
     * Sets the iD attribute of the YassSongList object
     */
    public void setID() {
        int i = getSelectedRow();
        if (i < 0) {
            return;
        }

        Vector<YassSong> sel = getSelectedSongs();
        String input = sm.getRowAt(i).getID();
        int n = sel.size();
        String msg = n > 1 ? MessageFormat.format(I18.get("mpop_id_msg_2"), n) : I18.get("mpop_id_msg_1");
        input = JOptionPane.showInputDialog(actions.getTab(), msg, input);
        if (input == null) {
            return;
        }
        for (Enumeration<YassSong> en = sel.elements(); en.hasMoreElements(); ) {
            YassSong s = en.nextElement();
            s.setID(input);
            s.setSaved(false);

        }
        setSaved(false);
        repaint();
    }

    /**
     * Sets the encoding attribute of the YassSongList object
     *
     * @param enc The new encoding value
     */
    public void setEncoding(String enc) {
        int i = getSelectedRow();
        if (i < 0) {
            return;
        }

        Vector<YassSong> sel = getSelectedSongs();
        for (Enumeration<YassSong> en = sel.elements(); en.hasMoreElements(); ) {
            YassSong s = en.nextElement();
            s.setEncoding(enc);
            s.setSaved(false);

        }
        setSaved(false);
        repaint();
    }

    /**
     * Description of the Method
     */
    public void updateLength() {
        int i = getSelectedRow();
        if (i < 0) {
            return;
        }

        boolean changed = false;
        Vector<YassSong> sel = getSelectedSongs();
        for (Enumeration<YassSong> en = sel.elements(); en.hasMoreElements(); ) {
            YassSong s = en.nextElement();

            String dir = s.getDirectory();
            String mp3 = s.getMP3();

            File file = new File(dir + File.separator + mp3);
            if (file.exists()) {
                try {
                    AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(file);
                    if (baseFileFormat instanceof TAudioFileFormat) {
                        Map<?, ?> properties = baseFileFormat.properties();
                        Long dur = (Long) properties.get("duration");
                        long sec = Math.round(dur.longValue() / 1000000.0);
                        s.setLength(sec + "");
                        s.setSaved(false);
                        changed = true;
                    }
                } catch (Exception ignored) {}
            }
        }
        if (changed) {
            setSaved(false);
        }
        repaint();
    }

    /**
     * Description of the Method
     */
    public void updateAlbum() {
        int i = getSelectedRow();
        if (i < 0) {
            return;
        }

        boolean changed = false;
        Vector<YassSong> sel = getSelectedSongs();
        for (Enumeration<YassSong> en = sel.elements(); en.hasMoreElements(); ) {
            YassSong s = en.nextElement();

            String dir = s.getDirectory();
            String mp3 = s.getMP3();

            File file = new File(dir + File.separator + mp3);
            if (file.exists()) {
                try {
                    AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(file);
                    if (baseFileFormat instanceof TAudioFileFormat) {
                        Map<?, ?> properties = baseFileFormat.properties();
                        String a = (String) properties.get("album");
                        if (a != null && a.trim().length() > 0) {
                            s.setAlbum(a);
                            s.setSaved(false);
                            changed = true;
                        }
                    }
                } catch (Exception ignored) { }
            }
        }
        if (changed) {
            setSaved(false);
        }
        repaint();
    }

    /**
     * Description of the Method
     */
    public void updateYear() {
        int i = getSelectedRow();
        if (i < 0) {
            return;
        }

        boolean changed = false;
        Vector<YassSong> sel = getSelectedSongs();
        for (Enumeration<YassSong> en = sel.elements(); en.hasMoreElements(); ) {
            YassSong s = en.nextElement();

            String dir = s.getDirectory();
            String mp3 = s.getMP3();

            File file = new File(dir + File.separator + mp3);
            if (file.exists()) {
                try {
                    AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(file);
                    if (baseFileFormat instanceof TAudioFileFormat) {
                        Map<?, ?> properties = baseFileFormat.properties();
                        String y = (String) properties.get("year");
                        if (y != null && y.trim().length() > 0) {
                            s.setYear(y);
                            s.setSaved(false);
                            changed = true;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        if (changed) {
            setSaved(false);
        }
        repaint();
    }

    /**
     * Sets the artist attribute of the YassSongList object
     */
    public void newEdition() {
        int i = getSelectedRow();
        if (i < 0) {
            return;
        }

        Vector<YassSong> sel = getSelectedSongs();
        String input = "";
        int n = sel.size();
        String msg = n > 1 ? MessageFormat.format(I18.get("mpop_edition_msg_2"), n) : I18.get("mpop_edition_msg_1");
        input = JOptionPane.showInputDialog(actions.getTab(), msg, input);
        if (input == null) {
            return;
        }
        for (Enumeration<YassSong> en = sel.elements(); en.hasMoreElements(); ) {
            YassSong s = en.nextElement();
            s.setEdition(input);
            s.setSaved(false);

        }
        setSaved(false);

        actions.refreshGroups();
        updateSelectionEdition();
        repaint();
    }

    /**
     * Sets the editionToFolder attribute of the YassSongList object
     */
    public void setEditionToFolder() {
        int i = getSelectedRow();
        if (i < 0) {
            return;
        }

        Vector<YassSong> sel = getSelectedSongs();
        int n = sel.size();
        String msg = n > 1 ? MessageFormat.format(I18.get("mpop_edition_folder_msg_2"), n) : I18.get("mpop_edition_folder_msg_1");
        int ok = JOptionPane.showConfirmDialog(actions.getTab(), msg, I18.get("mpop_edition_folder_title"), JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }

        boolean saved = true;
        for (Enumeration<YassSong> en = sel.elements(); en.hasMoreElements(); ) {
            YassSong s = en.nextElement();
            String folder = s.getFolder();
            String edition = s.getEdition();
            if (folder == null) {
                folder = "";
            }
            if (edition == null) {
                edition = "";
            }
            if (!folder.equals(edition)) {
                s.setEdition(folder);
                s.setSaved(false);
                saved = false;
            }
        }
        setSaved(saved);

        actions.refreshGroups();
        updateSelectionEdition();
        repaint();
    }

    /**
     * Sets the artist attribute of the YassSongList object
     */
    public void newFolder() {
        int i = getSelectedRow();
        if (i < 0) {
            return;
        }

        String dir = prop.getProperty("song-directory");
        File sd = new File(dir);
        if (!sd.exists()) {
            return;
        }

        Vector<YassSong> sel = getSelectedSongs();
        String input = "";
        int n = sel.size();
        String msg = n > 1 ? MessageFormat.format(I18.get("mpop_folder_msg_2"), n) : I18.get("mpop_folder_msg_1");
        input = JOptionPane.showInputDialog(actions.getTab(), msg, input);
        if (input == null) {
            return;
        }

        File newdir = new File(sd, input);
        if (!newdir.exists()) {
            if (!newdir.mkdir()) {
                return;
            }
        }

        for (Enumeration<YassSong> en = sel.elements(); en.hasMoreElements(); ) {
            YassSong s = en.nextElement();

            String sdir = s.getDirectory();
            File f = new File(sdir);
            File p = f.getParentFile();
            String name = f.getName();

            File f2 = new File(newdir, name);
            String newname = f2.getAbsolutePath();

            boolean ok2 = f.renameTo(f2);
            if (ok2) {
                s.setDirectory(newname);
                s.setFolder(input);
                if (p.listFiles() == null || p.listFiles().length == 0) {
                    p.delete();
                }
            }
        }
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean renameFolder() {
        int i = getSelectedRow();
        if (i < 0) {
            return false;
        }

        String dir = prop.getProperty("song-directory");
        File sd = new File(dir);
        if (!sd.exists()) {
            return false;
        }

        YassSong s = sm.getRowAt(i);

        String input = s.getFolder();
        if (input == null || input.length() < 1) {
            return false;
        }

        File folder = new File(sd, input);
        if (!folder.exists()) {
            return false;
        }

        String msg = I18.get("mpop_folder_name");
        String newinput = JOptionPane.showInputDialog(actions.getTab(), msg, input);
        if (newinput == null || newinput.equals(input)) {
            return false;
        }

        File newfolder = new File(sd, newinput);
        if (newfolder.exists()) {
            JOptionPane.showMessageDialog(actions.getTab(), I18.get("mpop_folder_name_error_msg"), I18.get("mpop_folder_name_error_title"), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!folder.renameTo(newfolder)) {
            return false;
        }

        String coverDir = prop.getProperty("cover-directory");
        File file = new File(coverDir + File.separator + input + ".jpg");
        File newfile = new File(coverDir + File.separator + newinput + ".jpg");
        if (file.exists() && !newfile.exists()) {
            file.renameTo(newfile);
        }

        String newfoldername = newfolder.getAbsolutePath();
        for (Enumeration<YassSong> en = allData.elements(); en.hasMoreElements(); ) {
            YassSong s2 = en.nextElement();
            String sf = s2.getFolder();
            if (sf != null && sf.equals(input)) {
                s2.setFolder(newinput);

                String sdir = s2.getDirectory();
                int k = sdir.lastIndexOf(File.separator);
                s2.setDirectory(newfoldername + sdir.substring(k));
            }
        }
        storeCache();
        repaint();

        return true;
    }

    /**
     * Sets the year attribute of the YassSongList object
     */
    public void setYear() {
        int i = getSelectedRow();
        if (i < 0) {
            return;
        }

        Vector<YassSong> sel = getSelectedSongs();
        String input = sm.getRowAt(i).getYear();
        int n = sel.size();
        String msg = n > 1 ? MessageFormat.format(I18.get("mpop_year_msg_2"), n) : I18.get("mpop_year_msg_1");
        input = JOptionPane.showInputDialog(actions.getTab(), msg, input);
        if (input == null) {
            return;
        }
        if (input.trim().equals("0")) {
            input = "";
        }
        for (Enumeration<YassSong> en = sel.elements(); en.hasMoreElements(); ) {
            YassSong s = en.nextElement();
            s.setYear(input);
            s.setSaved(false);

        }
        setSaved(false);
        repaint();
    }

    /**
     * Gets the languageMenu attribute of the YassSongList object
     *
     * @return The languageMenu value
     */
    public JMenu getLanguageMenu() {
        return languagePopup;
    }

    /**
     * Gets the editionMenu attribute of the YassSongList object
     *
     * @return The editionMenu value
     */
    public JMenu getEditionMenu() {
        return editionPopup;
    }

    /**
     * Gets the genreMenu attribute of the YassSongList object
     *
     * @return The genreMenu value
     */
    public JMenu getGenreMenu() {
        return genrePopup;
    }

    /**
     * Gets the sortByMenu attribute of the YassSongList object
     *
     * @return The sortByMenu value
     */
    public JMenu getSortByMenu() {
        return sortbyPopup;
    }

    /**
     * Description of the Method
     */
    public void selectAll() {
        int start = 0;
        int end = getRowCount() - 1;
        if (end >= 0) {
            setRowSelectionInterval(start, end);
        }
    }

    /**
     * Description of the Method
     *
     * @param ks        Description of the Parameter
     * @param e         Description of the Parameter
     * @param condition Description of the Parameter
     * @param pressed   Description of the Parameter
     * @return Description of the Return Value
     */
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (e.getKeyCode() == KeyEvent.VK_F5) {
            actions.refreshLibrary();
            return false;
        }
        if ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK) {
            return false;
        }
        if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
            if (e.getKeyCode() == KeyEvent.VK_A) {
                return super.processKeyBinding(ks, e, condition, pressed);
            }
            if (e.getKeyCode() == KeyEvent.VK_S) {
                //store();
                return false;
            }
            if (e.getKeyCode() == KeyEvent.VK_F) {
                actions.startFilterLibrary();
                return true;
            }
            int n = getRowCount();
            if (e.getKeyCode() == KeyEvent.VK_PAGE_UP && n > 0) {
                setRowSelectionInterval(0, 0);
                Rectangle r = getCellRect(0, 0, true);
                scrollRectToVisible(r);
            } else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN && n > 0) {
                setRowSelectionInterval(n - 1, n - 1);
                Rectangle r = getCellRect(n - 1, n - 1, true);
                scrollRectToVisible(r);
            }

            return false;
        }
        return super.processKeyBinding(ks, e, condition, pressed);
    }

    /**
     * Description of the Method
     *
     * @param renderer Description of the Parameter
     * @param column   Description of the Parameter
     * @param row      Description of the Parameter
     * @return Description of the Return Value
     */
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        ((JComponent) c).setOpaque(isCellSelected(row, column));
        ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        if (isCellSelected(row, column) == false) {
            YassSong r = sm.getRowAt(row);
            if (r == null) {
                return c;
            }
            boolean saved = r.isSaved();
            if (row % 2 == 0) {
                c.setBackground(saved ? hi : red);
            } else {
                c.setBackground(saved ? getBackground() : hired);
            }
            ((JComponent) c).setBorder(null);
        } else {
            YassSong r = sm.getRowAt(row);
            if (r == null) {
                return c;
            }
            boolean saved = r.isSaved();
            boolean foc = isFocusOwner();
            //if (foc) {
            c.setBackground(saved ? UIManager.getColor("Table.selectionBackground") : redsel);
            c.setForeground(UIManager.getColor("Table.selectionForeground"));
            //}

            if (options == TILE) {
                ((JComponent) c).setBorder(foc ? b2 : b5);
            }
        }

        return c;
    }

    /**
     * Gets the emptyMessage attribute of the YassSongList object
     *
     * @return The emptyMessage value
     */
    public String getEmptyMessage() {
        return emptyString;
    }

    /**
     * Sets the emptyMessage attribute of the YassSongList object
     *
     * @param s The new emptyMessage value
     */
    public void setEmptyMessage(String s) {
        emptyString = s;
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public void paintComponent(Graphics g) {
        Dimension d = getSize();

        //if (!isOpaque() && getFillsViewportHeight()) {
        if (!isOpaque()) {
            Color color1 = new Color(255, 255, 255, 150);
            Color color2 = new Color(255, 255, 255, 200);
            if (options == DETAILS) {
                color1 = new Color(200, 200, 200);
                color2 = new Color(255, 255, 255);
            }
            GradientPaint gp = new GradientPaint(0, 0, color1, d.width, d.height, color2);
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(gp);
            g2.fillRect(0, 0, d.width, d.height);
        }
        if (getRowCount() < 1) {
            g.setColor(Color.black);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(emptyString, d.width / 2 - fm.stringWidth(emptyString) / 2, d.height / 2);
            return;
        }

        super.paintComponent(g);

        long currentTime = System.currentTimeMillis();
        if (currentTime > lastTime + 700) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        String str = lastTimeString.toUpperCase();
        FontMetrics metrics = g2.getFontMetrics();
        int strh = metrics.getHeight();

        Point p = ((JViewport) getParent()).getViewPosition();
        int x = d.width - 60;
        int y = p.y + strh + 6;
        g2.setFont(big);
        g.setColor(Color.blue);
        g.drawString(str, x, y);
    }

    /**
     * Description of the Method
     */
    public void openSongFolder() {
        Vector<String> fn = getSelectedFiles();
        if (fn == null) {
            return;
        }

        Vector<String> pars = new Vector<>();
        for (Enumeration<String> en = fn.elements(); en.hasMoreElements(); ) {
            String filename = en.nextElement();
            File f = new File(filename);
            File p = f.getParentFile();
            filename = p.getAbsolutePath();
            pars.addElement(filename);
        }
        YassActions.openURLFiles(pars);
    }

    /**
     * Sets the openAction attribute of the YassSongList object
     *
     * @param a The new openAction value
     */
    public void setOpenAction(Action a) {
        openAction = a;
    }

    /**
     * Sets the dirAction attribute of the YassSongList object
     *
     * @param a The new dirAction value
     */
    public void setDirAction(Action a) {
        dirAction = a;
    }

    /**
     * Sets the openAction attribute of the YassSongList object
     *
     * @param a The new openAction value
     */
    public void setPrintAction(Action a) {
        printAction = a;
    }

    /**
     * Sets the openAction attribute of the YassSongList object
     *
     * @param a The new openAction value
     */
    public void setAutoAction(Action a) {
        autoAction = a;
    }

    /**
     * Gets the selectedSongs attribute of the YassSongList object
     *
     * @return The selectedSongs value
     */
    public Vector<YassSong> getSelectedSongs() {
        Vector<YassSong> v = new Vector<>();
        int[] rows = getSelectedRows();
        if (rows == null) {
            return null;
        }
        for (int row : rows) {
            v.add(sm.getRowAt(row));
        }
        return v;
    }

    /**
     * Gets the firstSelectedSong attribute of the YassSongList object
     *
     * @return The firstSelectedSong value
     */
    public YassSong getFirstSelectedSong() {
        int row = getSelectedRow();
        if (row < 0) {
            return null;
        }
        return sm.getRowAt(row);
    }

    /**
     * Gets the songAt attribute of the YassSongList object
     *
     * @param row Description of the Parameter
     * @return The songAt value
     */
    public YassSong getSongAt(int row) {
        return sm.getRowAt(row);
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     */
    public void selectSong(YassSong s) {
        if (s == null) {
            return;
        }
        int i = sm.getData().indexOf(s);
        if (i < 0) {
            return;
        }

        setRowSelectionInterval(i, i);
        Rectangle r = getCellRect(i, 0, true);
        scrollRectToVisible(r);
        repaint();
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public int indexOf(YassSong s) {
        if (s == null) {
            return -1;
        }
        return sm.getData().indexOf(s);
    }

    /**
     * Description of the Method
     *
     * @param i Description of the Parameter
     */
    public void selectSong(int i) {
        if (i < 0 || i >= getRowCount()) {
            return;
        }
        setRowSelectionInterval(i, i);
        Rectangle r = getCellRect(i, 0, true);
        scrollRectToVisible(r);
        repaint();
    }

    /**
     * Description of the Method
     */
    public void printSongs() {
        String filename = prop.getProperty("songlist-pdf");
        if (filename == null) {
            return;
        }

        YassSongListPrinter p = new YassSongListPrinter(prop);
        Hashtable<String, Object> hash = p.showDialog();
        if (hash != null) {
            Vector<YassSong> d = sm.getData();
            Vector<YassSong> data = (Vector<YassSong>) d.clone();
            p.print(data, filename, hash);

            try {
                // Java 1.6
                Class<?> c = Class.forName("java.awt.Desktop");
                Method m = c.getMethod("getDesktop", (Class[]) null);
                Object desktop = m.invoke(null, (Object[]) null);

                Class<?>[] par = new Class[1];
                par[0] = File.class;
                m = c.getMethod("open", par);
                m.invoke(desktop, new File(filename));
            } catch (Throwable t) {
                try {
                    String os = System.getProperty("os.name");
                    if (os.startsWith("Windows")) {
                        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + filename);
                    } else if (os.startsWith("MacOS")) {
                        Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                        java.lang.reflect.Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
                        openURL.invoke(null, new Object[]{filename});
                    } else if (os.startsWith("Linux")) {
                        String[] apps = {"gpdf", "evince", "kpdf", "firefox", "opera", "gnome-open", "kfmclient", "xdg-open", "gvfs-open"};
                        String app = null;
                        for (int i = 0; i < apps.length && app == null; i++) {
                            if (Runtime.getRuntime().exec(new String[]{"which", apps[i]}).waitFor() == 0) {
                                app = apps[i];
                            }
                        }
                        if (app != null) {
                            Runtime.getRuntime().exec(new String[]{app, filename});
                        } else {

                        }
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     * Description of the Method
     */
    public void refresh() {
        dirChanged = true;
        load();
    }

    /**
     * Gets the songIcon attribute of the WorkerThread object
     *
     * @param s        Description of the Parameter
     * @param rowIndex Description of the Parameter
     * @return The songIcon value
     */
    public ImageIcon getSongIcon(YassSong s, int rowIndex) {
        ImageIcon ii = s.getIcon();
        if (ii != null) {
            return ii;
        }

        String at = YassSong.toFilename(s.getArtist() + " - " + s.getTitle() + " @ " + s.getFolder());
        File cacheFile = new File(imageCacheName + File.separator + at + ".jpg");
        if (cacheFile.exists()) {
            try {
                Image img = javax.imageio.ImageIO.read(cacheFile);
                s.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                s.setIcon(null);
            }
        } else {
            SwingUtilities.invokeLater(new CacheCover(s, rowIndex));
        }
        return s.getIcon();
    }

    /**
     * Sets the library attribute of the YassSongList object
     */
    public void setLibrary() {
        String dir = prop.getProperty("song-directory");

        String newdir = YassUtils.getSongDir(this, prop, true);
        if (newdir == null || (dir != null && newdir.equals(dir))) {
            return;
        }
        dirChanged = true;
        load();
    }

    /**
     * Gets the table attribute of the YassSongList object
     *
     * @return The table value
     */
    public JTable getTable() {
        return this;
    }

    /**
     * Description of the Method
     *
     * @return The selectedFile value
     */
    public Vector<String> getSelectedFiles() {
        int[] rows = getSelectedRows();
        if (rows == null || rows.length < 1) {
            return null;
        }
        Vector<String> filenames = new Vector<>();
        for (int row : rows) {
            YassSong s = sm.getRowAt(row);
            if (s == null) {
                return null;
            }
            String d = s.getDirectory();
            if (d == null || d.trim().length() < 1) {
                return null;
            }
            String filename = d + File.separator + s.getFilename();
            filenames.addElement(filename);
        }
        return filenames;
    }

    /**
     * Description of the Method
     */
    public void closeOpened() {
        Vector<YassSong> data = sm.getData();
        int i = 0;
        for (Enumeration<YassSong> e = data.elements(); e.hasMoreElements(); i++) {
            YassSong s = e.nextElement();
            if (s.isOpened()) {
                s.setOpened(false);
                repaintIfVisible(i);
            }
        }
    }

    /**
     * Sets the opened attribute of the YassSongList object
     *
     * @param t Description of the Parameter
     * @return Description of the Return Value
     */
    public YassSong gotoSong(YassTable t) {
        if (t == null) {
            return null;
        }
        String dir = t.getDir();
        String txt = t.getFilename();
        if (txt == null || dir == null) {
            return null;
        }

        Vector<YassSong> data = sm.getData();
        int i = 0;
        for (Enumeration<YassSong> e = data.elements(); e.hasMoreElements(); i++) {
            YassSong s = e.nextElement();
            String dir2 = s.getDirectory();
            String txt2 = s.getFilename();
            if (txt.equals(txt2) && dir.equals(dir2)) {
                setRowSelectionInterval(i, i);
                Rectangle r = getCellRect(i, 0, true);
                scrollRectToVisible(r);
                repaint();
                return s;
            }
        }
        return null;
    }

    /**
     * Sets the messagesShown attribute of the YassSongList object
     *
     * @param onoff The new messagesShown value
     */
    public void showMessageColumns(boolean onoff) {
        for (int i = 0; i < YassRow.ALL_MESSAGES.length; i++) {
            TableColumn col = getColumnModel().getColumn(FIRST_MSG_COLUMN + i);
            SongHeaderRenderer r = (SongHeaderRenderer) col.getHeaderRenderer();
            if (onoff) {
                r.maximize();
            } else {
                r.minimize();
            }
            fixWidth(FIRST_MSG_COLUMN + i, onoff ? 20 : 0);
        }
    }

    /**
     * Description of the Method
     *
     * @param onoff Description of the Parameter
     */
    public void showStatsColumns(boolean onoff) {
        int statsCount = YassStats.length;
        for (int i = 0; i < statsCount; i++) {
            TableColumn col = getColumnModel().getColumn(FIRST_STATS_COLUMN + i);
            SongHeaderRenderer r = (SongHeaderRenderer) col.getHeaderRenderer();
            if (onoff) {
                r.maximize();
            } else {
                r.minimize();
            }
            fixWidth(FIRST_STATS_COLUMN + i, onoff ? 20 : 0);
        }
    }

    /**
     * Gets the messagesShown attribute of the YassSongList object
     *
     * @return The messagesShown value
     */
    public boolean getMessagesShown() {
        TableColumn col = getColumnModel().getColumn(FIRST_MSG_COLUMN);
        return col.getWidth() != 0;
    }

    /**
     * Gets the statsShown attribute of the YassSongList object
     *
     * @return The statsShown value
     */
    public boolean getStatsShown() {
        TableColumn col = getColumnModel().getColumn(FIRST_STATS_COLUMN);
        return col.getWidth() != 0;
    }

    /**
     * Gets the imagesShown attribute of the YassSongList object
     *
     * @return The imagesShown value
     */
    public boolean getImagesShown() {
        return imgShown;
    }

    /**
     * Sets the imagesShown attribute of the YassSongList object
     *
     * @param onoff The new imagesShown value
     */
    public void setImagesShown(boolean onoff) {
        imgShown = onoff;
        setRowHeight(onoff ? ICON_HEIGHT : 15);
    }

    /**
     * Gets the options attribute of the YassSongList object
     *
     * @return The options value
     */
    public int getOptions() {
        return options;
    }

    /**
     * Sets the options attribute of the YassSongList object
     *
     * @param op The new options value
     */
    public void setOptions(int op) {
        options = op;

        if (op == TILE) {
            if (getTableHeader() != null && getTableHeader().getParent() != null) {
                getTableHeader().getParent().setVisible(false);
            }

            fixWidth(0, 0);
            fixWidth(COMPLETE_COLUMN, 0);
            fixWidth(ARTIST_COLUMN, 0);
            fixWidth(DUETSINGER_COLUMN, 0);
            preferWidth(TITLE_COLUMN, 160);
            for (int i = TITLE_COLUMN + 1; i < FIRST_MSG_COLUMN; i++) {
                fixWidth(i, 0);
            }
            setImagesShown(true);
            showMessageColumns(false);
            showStatsColumns(false);
        } else if (op == DETAILS) {
            if (getTableHeader() != null && getTableHeader().getParent() != null) {
                getTableHeader().getParent().setVisible(true);
            }

            if (showErrors || showStats) {
                fixWidth(0, 0);
                fixWidth(COMPLETE_COLUMN, 0);
                preferWidth(ARTIST_COLUMN, 50);
                preferWidth(TITLE_COLUMN, 80);
                fixWidth(DUETSINGER_COLUMN, 0);
                fixWidth(GENRE_COLUMN, 0);
                fixWidth(EDITION_COLUMN, 0);
                fixWidth(LANGUAGE_COLUMN, 0);
                fixWidth(YEAR_COLUMN, 0);
                fixWidth(FOLDER_COLUMN, 0);
                fixWidth(ALBUM_COLUMN, 0);
                fixWidth(LENGTH_COLUMN, 0);
                fixWidth(ID_COLUMN, 0);
            } else {
                fixWidth(0, 0);
                fixWidth(COMPLETE_COLUMN, 25);
                preferWidth(ARTIST_COLUMN, 180);
                preferWidth(TITLE_COLUMN, 200);
                fixWidth(DUETSINGER_COLUMN, 0);
                preferWidth(GENRE_COLUMN, 80);
                preferWidth(EDITION_COLUMN, 100);
                preferWidth(LANGUAGE_COLUMN, 100);
                preferWidth(YEAR_COLUMN, 50);
                preferWidth(FOLDER_COLUMN, 100);
                fixWidth(ALBUM_COLUMN, 100);
                fixWidth(LENGTH_COLUMN, 50);
                fixWidth(ID_COLUMN, 25);
            }
            for (int i = ID_COLUMN + 1; i < FIRST_MSG_COLUMN; i++) {
                fixWidth(i, 0);
            }
            setImagesShown(false);
            showMessageColumns(showErrors);
            showStatsColumns(showStats);
        }
    }

    /**
     * Gets the showErrors attribute of the YassSongList object
     *
     * @return The showErrors value
     */
    public boolean getShowErrors() {
        return showErrors;
    }

    /**
     * Gets the showLyrics attribute of the YassSongList object
     *
     * @return The showLyrics value
     */
    public boolean getShowLyrics() {
        return showLyrics;
    }

    /**
     * Description of the Method
     *
     * @param onoff Description of the Parameter
     */
    public void showLyrics(boolean onoff) {
        showLyrics = onoff;
    }

    /**
     * Description of the Method
     *
     * @param onoff Description of the Parameter
     */
    public void showErrors(boolean onoff) {
        showErrors = onoff;
        setOptions(options);
        if (onoff) {
            startWorker(false, null);
        } else {
            interruptWorker();
            Enumeration<YassSong> en = sm.getData().elements();
            while (en.hasMoreElements()) {
                YassSong s = en.nextElement();
                s.clearMessages();
            }
            sm.fireTableDataChanged();
        }
    }

    /**
     * Description of the Method
     *
     * @param onoff Description of the Parameter
     */
    public void showStats(boolean onoff) {
        showStats = onoff;
        setOptions(options);
        if (onoff) {
            startWorker(false, null);
        } else {
            interruptWorker();
            Enumeration<YassSong> en = sm.getData().elements();
            while (en.hasMoreElements()) {
                YassSong s = en.nextElement();
                s.clearStats();
            }
            sm.fireTableDataChanged();
        }
    }

    /**
     * Description of the Method
     *
     * @param i Description of the Parameter
     * @param w Description of the Parameter
     */
    private void fixWidth(int i, int w) {
        TableColumn col = getColumnModel().getColumn(i);
        col.setMinWidth(0);
        col.setMaxWidth(w);
        col.setPreferredWidth(w);
    }

    /**
     * Description of the Method
     *
     * @param i Description of the Parameter
     * @param w Description of the Parameter
     */
    private void preferWidth(int i, int w) {
        TableColumn col = getColumnModel().getColumn(i);
        col.setMinWidth(0);
        col.setMaxWidth(2000);
        col.setPreferredWidth(w);
    }

    /**
     * Gets the autoWidth attribute of the YassSongList object
     *
     * @param col Description of the Parameter
     * @return The autoWidth value
     */
    public int getAutoWidth(int col) {
        Graphics g = getGraphics();
        if (g == null) {
            return 0;
        }
        FontMetrics metrics = g.getFontMetrics();
        if (col == 0) {
            String str = Integer.toString(sm.getData().size());
            return metrics.stringWidth(str + "88");
        }
        int max = 0;
        Enumeration<YassSong> en = sm.getData().elements();
        while (en.hasMoreElements()) {
            YassSong s = en.nextElement();
            max = Math.max(max, metrics.stringWidth(s.elementAt(col)));
        }
        return max + 2;
    }

    /**
     * Description of the Method
     *
     * @param col Description of the Parameter
     */
    public void autoWidth(int col) {
        fixWidth(col, getAutoWidth(col) + 4);
    }

    /**
     * Sets the extraColInfo attribute of the YassSongList object
     *
     * @param col The new extraColInfo value
     */
    public void setExtraInfo(int col) {
        extraCol = col;
    }

    /**
     * Description of the Method
     *
     * @param col Description of the Parameter
     */
    public void sortBy(int col) {
        sortbyCol = col;
        if (col == COMPLETE_COLUMN) {
            YassSong.ordering = YassSong.SORT_BY_COMPLETE;
        }
        if (col == ARTIST_COLUMN) {
            YassSong.ordering = YassSong.SORT_BY_ARTIST;
        }
        if (col == TITLE_COLUMN) {
            YassSong.ordering = YassSong.SORT_BY_TITLE;
        }
        if (col == DUETSINGER_COLUMN) {
            YassSong.ordering = YassSong.SORT_BY_DUETSINGER;
        }
        if (col == EDITION_COLUMN) {
            YassSong.ordering = YassSong.SORT_BY_EDITION;
        }
        if (col == GENRE_COLUMN) {
            YassSong.ordering = YassSong.SORT_BY_GENRE;
        }
        if (col == FOLDER_COLUMN) {
            YassSong.ordering = YassSong.SORT_BY_FOLDER;
        }
        if (col == LANGUAGE_COLUMN) {
            YassSong.ordering = YassSong.SORT_BY_LANGUAGE;
        }
        if (col == YEAR_COLUMN) {
            YassSong.ordering = YassSong.SORT_BY_YEAR;
        }
        if (col == ALBUM_COLUMN) {
            YassSong.ordering = YassSong.SORT_BY_ALBUM;
        }
        if (col == LENGTH_COLUMN) {
            YassSong.ordering = YassSong.SORT_BY_LENGTH;
        }
        if (col == ID_COLUMN) {
            YassSong.ordering = YassSong.SORT_BY_ID;
        }
        if (col >= FIRST_MSG_COLUMN && col < FIRST_STATS_COLUMN) {
            YassSong.ordering = YassSong.SORT_BY_MESSAGE;
            YassSong.msgindex = col - FIRST_MSG_COLUMN;
        }
        if (col >= FIRST_STATS_COLUMN) {
            YassSong.ordering = YassSong.SORT_BY_STATS;
            YassSong.statsindex = col - FIRST_STATS_COLUMN;
        }

        TableCellEditor ed = getCellEditor();
        if (ed != null) {
            ed.cancelCellEditing();
        }
        Collections.sort(sm.getData());
        int i = 1;
        Enumeration<YassSong> en = sm.getData().elements();
        while (en.hasMoreElements()) {
            en.nextElement().setState((i++) + "");
        }
        sm.fireTableRowsUpdated(0, sm.getRowCount());
    }

    /**
     * Sets the selectionLanguage attribute of the YassSongList object
     *
     * @param language The new selectionLanguage value
     */
    public void setSelectionLanguage(String language) {
        int[] rows = getSelectedRows();
        boolean changed = false;
        for (int row : rows) {
            YassSong s = sm.getRowAt(row);
            if (s.isOpened()) {
                if (s.getTable().setLanguage(language)) {
                    changed = true;
                    s.setSaved(false);
                }
            }
            if (!language.equals(s.getLanguage())) {
                s.setLanguage(language);
                changed = true;
                s.setSaved(false);
            }
            sm.fireTableRowsUpdated(row, row);
        }
        if (changed) {
            setSaved(false);
            actions.refreshGroups();
        }
    }

    /**
     * Sets the saved attribute of the YassSongList object
     *
     * @param onoff The new saved value
     */
    public void setSaved(boolean onoff) {
        if (storeAction != null) {
            storeAction.setEnabled(!onoff);
        }
        if (undoallAction != null) {
            undoallAction.setEnabled(!onoff);
        }
    }

    /**
     * Sets the selectionGenre attribute of the YassSongList object
     *
     * @param genre The new selectionGenre value
     */
    public void setSelectionGenre(String genre) {
        int[] rows = getSelectedRows();
        boolean changed = false;
        for (int row : rows) {
            YassSong s = sm.getRowAt(row);
            if (s.isOpened()) {
                if (s.getTable().setGenre(genre)) {
                    changed = true;
                    s.setSaved(false);
                }
            }
            if (!genre.equals(s.getGenre())) {
                s.setGenre(genre);
                changed = true;
                s.setSaved(false);
            }
            sm.fireTableRowsUpdated(row, row);
        }
        if (changed) {
            setSaved(false);
            actions.refreshGroups();

        }
    }

    /**
     * Sets the selectionEdition attribute of the YassSongList object
     *
     * @param edition The new selectionEdition value
     */
    public void setSelectionEdition(String edition) {
        int[] rows = getSelectedRows();
        boolean changed = false;
        for (int row : rows) {
            YassSong s = sm.getRowAt(row);
            if (s.isOpened()) {
                if (s.getTable().setEdition(edition)) {
                    changed = true;
                    s.setSaved(false);
                }
            }
            if (!edition.equals(s.getEdition())) {
                s.setEdition(edition);
                changed = true;
                s.setSaved(false);
            }
            sm.fireTableRowsUpdated(row, row);
        }
        if (changed) {
            setSaved(false);
            actions.refreshGroups();

        }
    }

    /**
     * Description of the Method
     */
    public void updateSelectionEdition() {
        editions.clear();
        Vector<YassSong> data = sm.getData();
        for (Enumeration<YassSong> e = data.elements(); e.hasMoreElements(); ) {
            YassSong s = e.nextElement();
            String edition = s.getEdition();
            if (edition == null || edition.length() < 1) {
                continue;
            }
            if (!editions.contains(edition)) {
                editions.addElement(edition);
            }
        }
        Collections.sort(editions);

        moreEditions.removeAll();
        cmoreEditions.removeAll();

        int i = 0;
        for (Enumeration<String> en = editions.elements(); en.hasMoreElements(); ) {
            String ed = en.nextElement();

            if (++i > 20) {
                i = 0;
                JMenu evenmore = new JMenu(I18.get("lib_edition_more"));
                moreEditions.addSeparator();
                moreEditions.add(evenmore);
                moreEditions = evenmore;

                evenmore = new JMenu(I18.get("lib_edition_more"));
                cmoreEditions.addSeparator();
                cmoreEditions.add(evenmore);
                cmoreEditions = evenmore;
            }

            JMenuItem menuItem = new JMenuItem(ed);
            moreEditions.add(menuItem);
            menuItem.addActionListener(actionEdition);

            menuItem = new JMenuItem(ed);
            cmoreEditions.add(menuItem);
            menuItem.addActionListener(actionEdition);
        }
    }

    /**
     * Description of the Method
     */
    public void removeSelectedSongs() {
        int[] rows = getSelectedRows();
        if (rows == null || rows.length < 1) {
            return;
        }
        java.util.Arrays.sort(rows);

        stopPlaying();

        int n = rows.length;
        String nmsg = n > 1 ? MessageFormat.format(I18.get("mpop_remove_msg_2"), n) : I18.get("mpop_remove_msg_1");
        int ok = JOptionPane.showConfirmDialog(actions.getTab(), nmsg, I18.get("mpop_remove_title"), JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }

        actions.stopPlayers();
        for (int i = rows.length - 1; i >= 0; i--) {
            YassSong s = sm.getRowAt(rows[i]);
            String dir = s.getDirectory();
            File fdir = new File(dir);
            File[] files = fdir.listFiles();
            int k = 0;
            for (File file : files) {
                if (YassUtils.isKaraokeFile(file)) {
                    k++;
                }
            }
            if (k > 1) {
                String txt = s.getFilename();
                File f = new File(dir + File.separator + txt);
                if (!f.delete()) {
                    JOptionPane.showMessageDialog(actions.getTab(), I18.get("mpop_remove_error"), I18.get("mpop_remove_title"), JOptionPane.ERROR_MESSAGE);
                    break;
                }
            } else {
                if (!YassUtils.deleteDir(fdir)) {
                    String msg = YassUtils.getMessage();
                    JOptionPane.showMessageDialog(actions.getTab(), msg, I18.get("mpop_remove_title"), JOptionPane.ERROR_MESSAGE);
                    break;
                }
            }
            sm.getData().removeElementAt(rows[i]);
            allData.removeElement(s);
        }
        sm.fireTableDataChanged();
        storeCache();

        actions.setProgress(MessageFormat.format(I18.get("lib_msg"), sm.getData().size()), "");

        n = sm.getData().size();
        if (n > 0) {
            clearSelection();
            int i = Math.min(rows[0], n - 1);
            addRowSelectionInterval(i, i);
        }
    }

    /**
     * Gets the loaded attribute of the YassSongList object
     *
     * @return The loaded value
     */
    boolean isLoaded() {
        return loaded;
    }

    /**
     * Description of the Method
     */
    public void loadArticles() {
        if (lang_articles == null) {
            lang_articles = new Hashtable<>();
        }

        String s = prop.getProperty("articles");
        StringTokenizer st = new StringTokenizer(s, ":");
        while (st.hasMoreTokens()) {
            String lang = st.nextToken();

            Vector<String> v = lang_articles.get(lang);
            if (v == null) {
                v = new Vector<>();
                lang_articles.put(lang, v);
            } else {
                v.clear();
            }
            String articles = st.nextToken();
            StringTokenizer sta = new StringTokenizer(articles, "|");
            while (sta.hasMoreTokens()) {
                String article = sta.nextToken();
                article = article.toLowerCase();
                v.addElement(article);
            }
        }
    }

    /**
     * Gets the sortedArtist attribute of the YassSongList object
     *
     * @param a    Description of the Parameter
     * @param lang Description of the Parameter
     * @return The sortedArtist value
     */
    public String getSortedArtist(String a, String lang) {
        if (lang == null) {
            return null;
        }

        for (int i = 0; i < langArray.length; i++) {
            if (langArray[i].equals(lang)) {
                lang = langID[i];
                break;
            }
        }

        Vector<?> v = lang_articles.get(lang);
        if (v == null) {
            return null;
        }

        String artist = a.toLowerCase();
        for (Enumeration<?> en = v.elements(); en.hasMoreElements(); ) {
            String article = (String) en.nextElement();
            if (artist.startsWith(article)) {
                int len = article.length();
                a = a.substring(len) + ", " + a.substring(0, len);
                return a;
            }
        }
        return null;
    }

    /**
     * Description of the Method
     */
    public void load() {
        interrupt();
        if (fileUtils == null) {
            fileUtils = new YassFileUtils();
        }
        actions.setLibraryLoaded(false);
        preventInteraction = true;

        String dir = prop.getProperty("song-directory");

        if (dir == null || dir.length() < 1 || !new File(dir).exists()) {
            clear();
            actions.setProgress(I18.get("lib_msg_unset"), 0);
            return;
        }

        loadArticles();

        if (dirChanged) {
            String cacheName = prop.getProperty("lyrics-cache");
            File lcache = new File(cacheName);
            if (lcache.exists()) {
                lcache.delete();
            }
        }

        if (pldata == null) {
            pldata = new Vector<>();
        } else {
            pldata.clear();
        }
        boolean plok = true;
        String plcacheName = prop.getProperty("playlist-cache");
        if (plcacheName == null) {
            plcacheName = "";
        }
        File plcache = new File(plcacheName);
        if (plcache.exists() && !dirChanged) {
            YassFile playlist = fileUtils.loadTextFile(plcacheName);
            if (playlist == null) {
                plok = false;
            } else {
                for (String plElement : playlist.getContentLines())
                    pldata.addElement(plElement);
            }
        }

        boolean ok = true;
        String cacheName = prop.getProperty("songlist-cache");
        File cache = new File(cacheName);
        if (cache.exists() && !dirChanged) {
            boolean forceUtf8 = prop.getBooleanProperty("utf8-always");
            dirChanged = false;
            sm.getData().clear();
            try {
                YassFile songListFile = fileUtils.loadTextFile(cacheName);
                if (songListFile == null) {
                    ok = false;
                } else {
                    if (!songListFile.isUtf8() && forceUtf8) {
                        fileUtils.writeFile(songListFile.getContentLines(), Paths.get(cacheName));
                    }
                    YassSong s;
                    for (String listEntry : songListFile.getContentLines()) {
                        if (!ok) {
                            break;
                        }
                        s = new YassSong(listEntry);
                        String sorted_artist = null;
                        if (moveArticles) {
                            sorted_artist = getSortedArtist(s.getArtist(), s.getLanguage());
                        }
                        s.setSortedArtist(sorted_artist);

                        s.setIcon(null);
                        s.clearMessages();
                        sm.addRow(s);
                    }
                }
            } catch (Exception e) {
                ok = false;
            }
        } else {
            ok = false;
            sm.getData().clear();
            sm.fireTableDataChanged();
        }

        if (!ok || !plok) {
            startWorker(true, dir);
        } else {
            fireSongListChanged(YassSongListEvent.LOADED);
            fireSongListChanged(YassSongListEvent.FINISHED);
            actions.setLibraryLoaded(true);
            preventInteraction = false;
            loaded = true;
            if (allData == null) {
                allData = (Vector<YassSong>) sm.getData().clone();
            }
            updateSelectionEdition();
        }
    }

    /**
     * Description of the Method
     *
     * @param dir Description of the Parameter
     * @param all Description of the Parameter
     */
    private void startWorker(boolean all, String dir) {
        interruptWorker();
        worker = new WorkerThread(all, dir);
        worker.start();
    }

    /**
     * Description of the Method
     */
    public void interrupt() {
        stopPlaying();
        interruptWorker();
    }

    /**
     * Description of the Method
     */
    public void interruptWorker() {
        if (worker != null) {
            worker.notInterrupted = false;
            while (worker.state != YassSongListEvent.FINISHED) {
                Thread.yield();
            }
        }
        if (corrector != null) {
            corrector.notInterrupted = false;
            while (!corrector.finished) {
                Thread.yield();
            }
        }
    }

    /**
     * Description of the Method
     */
    public void clear() {
        interrupt();

        String plcacheName = prop.getProperty("playlist-cache");
        File plcache = new File(plcacheName);
        if (plcache.exists()) {
            plcache.delete();
        }

        String cacheName = prop.getProperty("songlist-cache");
        File cache = new File(cacheName);
        if (cache.exists()) {
            cache.delete();
        }

        String imageCacheName = prop.getProperty("songlist-imagecache");
        File imageCache = new File(imageCacheName);
        if (imageCache.exists()) {
            removeDir(imageCache);
        }

        sm.getData().clear();
        sm.fireTableDataChanged();
    }

    /**
     * Description of the Method
     *
     * @param dir Description of the Parameter
     * @return Description of the Return Value
     */
    private boolean removeDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = YassUtils.deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * Adds a feature to the Opened attribute of the YassSongList object
     *
     * @param t The feature to be added to the Opened attribute
     */
    public void addOpened(YassTable t) {
        String dir = t.getDir();
        String txt = t.getFilename();

        if (dir == null || txt == null) {
            return;
        }

        Vector<YassSong> data = sm.getData();
        int i = 0;
        for (Enumeration<YassSong> e = data.elements(); e.hasMoreElements(); i++) {
            YassSong s = e.nextElement();
            String dir2 = s.getDirectory();
            String txt2 = s.getFilename();
            if (dir2 == null || txt2 == null) {
                continue;
            }

            if (txt.equals(txt2) && dir.equals(dir2)) {
                s.setOpened(true);
                s.setTable(t);
                repaintIfVisible(i);
            }
        }
    }

    /**
     * Adds a feature to the Song attribute of the YassSongList object
     *
     * @param filedir The feature to be added to the Song attribute
     * @param folder  The feature to be added to the Song attribute
     * @param name    The feature to be added to the Song attribute
     */
    public void addSong(String filedir, String folder, String name) {
        String dir = prop.getProperty("song-directory");
        if (dir == null) {
            return;
        }
        if (!filedir.startsWith(dir)) {
            return;
        }
        // outside song dir --> skip
        sm.addRow(filedir, folder, name, "", "");
        YassSong s = sm.getRowAt(sm.getData().size() - 1);

        YassTable t = new YassTable();
        if (loadSongDetails(s, t)) {
            Collections.sort(sm.getData());
            int i = 0;
            Enumeration<YassSong> en = sm.getData().elements();
            while (en.hasMoreElements()) {
                en.nextElement().setState((i++) + "");
            }
            sm.fireTableRowsUpdated(0, sm.getRowCount());

            storeCache();
            updateSelectionEdition();
            repaint();
        }
    }

    /**
     * Description of the Method
     *
     * @param state Description of the Parameter
     */
    public void fireSongListChanged(int state) {
        if (listeners == null) {
            return;
        }

        YassSongListEvent e = new YassSongListEvent(this, state);
        for (Enumeration<YassSongListListener> en = listeners.elements(); en.hasMoreElements(); ) {
            YassSongListListener l = en.nextElement();
            l.stateChanged(e);
        }
    }

    /**
     * Adds a feature to the SongListListener attribute of the YassSongList object
     *
     * @param l The feature to be added to the SongListListener attribute
     */
    public void addSongListListener(YassSongListListener l) {
        if (listeners == null) {
            listeners = new Vector<>();
        }
        listeners.addElement(l);
    }

    /**
     * Description of the Method
     *
     * @param l Description of the Parameter
     */
    public void removeSongListListener(YassSongListListener l) {
        listeners.removeElement(l);
    }

    /**
     * Description of the Method
     */
    public void startThumbnailer() {
        String dir = prop.getProperty("song-directory");

        if (dir == null || dir.length() < 1 || !new File(dir).exists()) {
            return;
        }
        ThumbnailerThread thumbnailer = new ThumbnailerThread(dir);
        thumbnailer.start();
    }

    /**
     * Description of the Method
     */
    public void refreshCursor() {
        actions.updatePlayListCursor();
        // hack, should be in transferhandler
    }

    /**
     * Description of the Method
     *
     * @param s              Description of the Parameter
     * @param imageCacheName Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean cacheSongCover(String imageCacheName, YassSong s) {
        String d = s.getDirectory();
        if (d == null || d.trim().length() < 1) {
            s.setIcon(brokenSong);
            return true;
        }
        String co = s.getCover();
        if (co == null) {
            s.setIcon(noCover);
            return true;
        }
        co = d + File.separator + co;
        File coverFile = new File(co);
        String at = YassSong.toFilename(s.getArtist() + " - " + s.getTitle() + " @ " + s.getFolder());
        File cacheFile = new File(imageCacheName + File.separator + at + ".jpg");

        boolean useCache = cacheFile.exists();
        if (useCache) {
            long cacheTimestamp = cacheFile.lastModified();
            long coverTimestamp = coverFile.lastModified();
            useCache = cacheTimestamp == coverTimestamp;

            if (useCache) {
                try {
                    Image img = javax.imageio.ImageIO.read(cacheFile);
                    s.setIcon(new ImageIcon(img));
                } catch (Exception e) {
                    s.setIcon(noCover);
                }
                return true;
            }
        }
        if (!useCache) {
            try {
                BufferedImage img = YassUtils.readImage(coverFile);

                //attention: scale down only
                BufferedImage bufferedImage = YassUtils.getScaledInstance(img, ICON_WIDTH, ICON_HEIGHT);

                // BufferedImage bufferedImage = new BufferedImage(ICON_WIDTH, ICON_HEIGHT, BufferedImage.TYPE_INT_RGB);
                //Graphics2D g2d = bufferedImage.createGraphics();
                //g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                //g2d.drawImage(img, 0, desty, ICON_WIDTH, desth, null);
                //g2d.dispose();
                img.flush();

                if (cacheFile.exists()) {
                    cacheFile.delete();
                }
                javax.imageio.ImageIO.write(bufferedImage, "jpg", cacheFile);
                cacheFile.setLastModified(coverFile.lastModified());
                s.setIcon(new ImageIcon(bufferedImage));
                return true;
            } catch (Exception e) {
                //System.out.println("error at " + s.getFilename());
                s.setIcon(noCover);
                return true;
                // may happen for multipe txts in one directory, one with video, others not
                // --> dir is autocorrected to [VIDEO]; alternating for the others
            }
        }
        return false;
    }

    /**
     * Description of the Method
     *
     * @param i Description of the Parameter
     */
    public void repaintIfVisible(int i) {
        JViewport viewport = (JViewport) getParent();
        int y = getCellRect(i, 0, true).y;
        Point or = viewport.getViewPosition();
        Dimension dim = viewport.getExtentSize();
        if (y >= or.y && y <= or.y + dim.height) {
            SwingUtilities.invokeLater(() -> repaint());
        }
    }

    /**
     * Description of the Method
     */
    public void store() {
        if (storeThread == null) {
            storeThread = new StoreThread();
            storeThread.start();
            return;
        }

        int n = 10;
        while (n-- > 0 && storeThread.started && !storeThread.finished) {
            storeThread.notInterrupted = false;
            try {
                Thread.sleep(200);
            } catch (Exception ignored) { }
        }
        if (n > 0) {
            storeThread = new StoreThread();
            storeThread.start();
        }
    }

    /**
     * Description of the Method
     */
    public void storeSelection() {
        YassTable t = new YassTable();
        t.init(prop);
        int[] rows = getSelectedRows();
        Vector<YassSong> sel = getSelectedSongs();
        for (Enumeration<YassSong> en = sel.elements(); en.hasMoreElements(); ) {
            YassSong s = en.nextElement();
            if (!s.isSaved()) {
                storeSongDetails(s, t);
            }
        }
        sm.fireTableDataChanged();
        storeCache();

        clearSelection();
        for (int row : rows) {
            addRowSelectionInterval(row, row);
        }

        Vector<YassSong> data = sm.getData();
        for (Enumeration<YassSong> en = data.elements(); en.hasMoreElements(); ) {
            YassSong s = en.nextElement();
            if (!s.isSaved()) {
                return;
            }
        }
        setSaved(true);
    }

    /**
     * Description of the Method
     */
    public void undoSelection() {
        YassTable t = new YassTable();
        Vector<YassSong> sel = getSelectedSongs();
        int[] rows = getSelectedRows();
        for (Enumeration<YassSong> en = sel.elements(); en.hasMoreElements(); ) {
            YassSong s = en.nextElement();
            if (!s.isSaved()) {
                loadSongDetails(s, t);
                s.setSaved(true);
            }
        }
        sm.fireTableDataChanged();

        actions.refreshGroups();
        updateSelectionEdition();

        clearSelection();
        for (int row : rows) {
            addRowSelectionInterval(row, row);
        }

        Vector<YassSong> data = sm.getData();
        for (Enumeration<YassSong> en = data.elements(); en.hasMoreElements(); ) {
            YassSong s = en.nextElement();
            if (!s.isSaved()) {
                return;
            }
        }
        setSaved(true);
    }

    /**
     * Description of the Method
     */
    public void undoAll() {
        int[] rows = getSelectedRows();

        YassTable t = new YassTable();
        Vector<YassSong> data = sm.getData();
        for (Enumeration<YassSong> e = data.elements(); e.hasMoreElements(); ) {
            YassSong s = e.nextElement();
            if (!s.isSaved()) {
                loadSongDetails(s, t);
                s.setSaved(true);
            }
        }
        sm.fireTableDataChanged();

        actions.refreshGroups();
        updateSelectionEdition();

        clearSelection();
        for (int row : rows) {
            addRowSelectionInterval(row, row);
        }
        setSaved(true);
    }

    /**
     * Sets the undoAllAction attribute of the YassSongList object
     *
     * @param a The new undoAllAction value
     */
    public void setUndoAllAction(Action a) {
        undoallAction = a;
    }

    /**
     * Gets the storeAction attribute of the YassSongList object
     *
     * @return The storeAction value
     */
    public Action getStoreAction() {
        return storeAction;
    }

    /**
     * Sets the storeAction attribute of the YassSongList object
     *
     * @param a The new storeAction value
     */
    public void setStoreAction(Action a) {
        storeAction = a;
    }

    /**
     * Gets the undoallAction attribute of the YassSongList object
     *
     * @return The undoallAction value
     */
    public Action getUndoallAction() {
        return undoallAction;
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @param t Description of the Parameter
     */
    public synchronized void storeSongDetails(YassSong s, YassTable t) {
        String filename = s.getDirectory() + File.separator + s.getFilename();
        t.removeAllRows();
        if (t.loadFile(filename)) {
            t.setTitle(s.getTitle());
            t.setArtist(s.getArtist());
            String g = s.getGenre();
            if (g != null && g.length() > 0) {
                t.setGenre(g);
            }
            g = s.getEdition();
            if (g != null && g.length() > 0) {
                t.setEdition(g);
            }
            g = s.getLanguage();
            if (g != null && g.length() > 0) {
                t.setLanguage(g);
            }
            g = s.getYear();
            if (g != null && g.length() > 0) {
                t.setYear(g);
            }
            g = s.getAlbum();
            if (g != null && g.length() > 0) {
                t.setAlbum(g);
            }
            g = s.getID();
            if (g != null && g.length() > 0) {
                t.setID(g);
            }
            g = s.getLength();
            if (g != null && g.length() > 0) {
                t.setLength(g);
            }
            g = s.getPreviewStart();
            if (g != null && g.length() > 0) {
                t.setPreviewStart(g);
            }
            g = s.getMedleyStartBeat();
            if (g != null && g.length() > 0) {
                t.setMedleyStartBeat(g);
            }
            g = s.getMedleyEndBeat();
            if (g != null && g.length() > 0) {
                t.setMedleyEndBeat(g);
            }

            t.setEncoding(s.getEncoding());

            String tmp = prop.getProperty("temp-dir");
            String title = YassSong.toFilename(s.getTitle());
            String artist = YassSong.toFilename(s.getArtist());
            String folder = s.getFolder();
            String fname = tmp + File.separator + artist + " - " + title + " [CO] @ " + folder + ".jpg";
            File file = new File(fname);
            if (file.exists()) {
                String dir = t.getDir();
                String co = t.getCover();
                boolean ok = false;
                if (co != null) {
                    String oldfilename = dir + File.separator + co;
                    File oldfile = new File(oldfilename);
                    if (oldfile.exists()) {
                        oldfile.delete();
                        file.renameTo(oldfile);
                        ok = true;
                    }
                }
                if (!ok) {
                    co = dir + File.separator + artist + " - " + title + " [CO].jpg";
                    File newfile = new File(co);
                    if (newfile.exists()) {
                        newfile.delete();
                    }
                    file.renameTo(newfile);
                    YassAutoCorrect.insertCover(t, newfile);
                }
            }
            fname = tmp + File.separator + artist + " - " + title + " [BG] @ " + folder + ".jpg";
            file = new File(fname);
            if (file.exists()) {
                String dir = t.getDir();
                String bg = t.getBackgroundTag();
                boolean ok = false;
                if (bg != null) {
                    String oldfilename = dir + File.separator + bg;
                    File oldfile = new File(oldfilename);
                    if (oldfile.exists()) {
                        oldfile.delete();
                        file.renameTo(oldfile);
                        ok = true;
                    }
                }
                if (!ok) {
                    bg = dir + File.separator + artist + " - " + title + " [BG].jpg";
                    File newfile = new File(bg);
                    if (newfile.exists()) {
                        newfile.delete();
                    }
                    file.renameTo(newfile);
                    YassAutoCorrect.insertBackground(t, newfile);
                }
            }
            file = null;
            String at = artist + " - " + title + " [VD#";
            File tmpfile = new File(tmp);
            File files[] = tmpfile.listFiles();
            for (File file2 : files) {
                String name = file2.getName();
                if (name.startsWith(at)) {
                    if (name.indexOf("@ " + folder + ".") > 0) {
                        file = file2;
                    }
                }
            }
            if (file != null && file.exists()) {
                String dir = t.getDir();
                String vd = t.getVideo();

                String vdname = file.getName();
                String videoID = prop.getProperty("video-id");
                String vgap = YassUtils.getWildcard(vdname, videoID);
                if (vgap == null) {
                    vgap = "0";
                }

                boolean ok = false;
                if (vd != null) {
                    String oldfilename = dir + File.separator + vd;
                    File oldfile = new File(oldfilename);
                    if (oldfile.exists()) {
                        oldfile.delete();
                        file.renameTo(oldfile);
                        t.setVideoGap(vgap);
                        ok = true;
                    }
                }
                if (!ok) {
                    int i = vdname.lastIndexOf(".");
                    String ext = vdname.substring(i);
                    vd = dir + File.separator + artist + " - " + title + " [VD#" + vgap + "]" + ext;
                    File newfile = new File(vd);
                    if (newfile.exists()) {
                        newfile.delete();
                    }
                    file.renameTo(newfile);
                    YassAutoCorrect.insertVideo(t, newfile);
                    t.setVideoGap(vgap);
                }
            }

            t.storeFile(filename);
            s.setSaved(true);
            loadSongDetails(s, t);

            String imageCacheName = prop.getProperty("songlist-imagecache");
            if (cacheSongCover(imageCacheName, s)) {
                repaint();
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param onoff Description of the Parameter
     */
    public void moveArticles(boolean onoff) {
        moveArticles = onoff;
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @param t Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean loadSongDetails(YassSong s, YassTable t) {
        String filename = s.getDirectory() + File.separator + s.getFilename();
        String title = "";
        String artist = "";
        String mp3 = "";
        String cover = "";
        String background = "";
        String video = "";
        String edition = "";
        String genre = "";
        String language = "";
        String year = "";
        String vgap = "";
        String start = "";
        String end = "";
        String rel = "";
        String bpm = "";
        String gap = "";
        int multiplayer = 0;
        String duetSingerNames = "";
        String album = "";
        String id = "";
        String versionid = "";
        String length = "";
        String previewstart = "";
        String medleystartbeat = "";
        String medleyendbeat = "";
        String encoding = "";
        boolean changed = false;

        boolean oldUndo = t.getPreventUndo();
        t.setPreventUndo(true);
        t.removeAllRows();
        t.setPreventUndo(oldUndo);
        if (t.loadFile(filename)) {
            long timestamp = new File(filename).lastModified();
            if (timestamp != s.getTimestamp()) {
                changed = true;
                s.setTimestamp(timestamp);
            }

            YassTableModel tm = (YassTableModel) t.getModel();
            YassRow r = tm.getCommentRow("TITLE:");
            title = r != null ? r.getComment() : "";
            r = tm.getCommentRow("ARTIST:");
            artist = r != null ? r.getComment() : "";
            r = tm.getCommentRow("MP3:");
            mp3 = r != null ? r.getComment() : "";
            r = tm.getCommentRow("COVER:");
            cover = r != null ? r.getComment() : "";
            r = tm.getCommentRow("BACKGROUND:");
            background = r != null ? r.getComment() : "";
            r = tm.getCommentRow("VIDEO:");
            video = r != null ? r.getComment() : "";
            r = tm.getCommentRow("EDITION:");
            edition = r != null ? r.getComment() : "";
            r = tm.getCommentRow("GENRE:");
            genre = r != null ? r.getComment() : "";
            r = tm.getCommentRow("LANGUAGE:");
            language = r != null ? r.getComment() : "";
            r = tm.getCommentRow("YEAR:");
            year = r != null ? r.getComment() : "";

            r = tm.getCommentRow("VIDEOGAP:");
            vgap = r != null ? r.getComment() : "";
            r = tm.getCommentRow("START:");
            start = r != null ? r.getComment() : "";
            r = tm.getCommentRow("END:");
            end = r != null ? r.getComment() : "";
            r = tm.getCommentRow("RELATIVE:");
            rel = r != null ? r.getComment() : "";
            r = tm.getCommentRow("BPM:");
            bpm = r != null ? r.getComment() : "";
            r = tm.getCommentRow("GAP:");
            gap = r != null ? r.getComment() : "";

            r = tm.getCommentRow("ALBUM:");
            album = r != null ? r.getComment() : "";
            r = tm.getCommentRow("ID:");
            id = r != null ? r.getComment() : "";
            r = tm.getCommentRow("VERSION:");
            versionid = r != null ? r.getComment() : "";
            r = tm.getCommentRow("LENGTH:");
            length = r != null ? r.getComment() : "";
            r = tm.getCommentRow("PREVIEWSTART:");
            previewstart = r != null ? r.getComment() : "";
            r = tm.getCommentRow("MEDLEYSTARTBEAT:");
            medleystartbeat = r != null ? r.getComment() : "";
            r = tm.getCommentRow("MEDLEYENDBEAT:");
            medleyendbeat = r != null ? r.getComment() : "";

            multiplayer = t.getPlayerCount();
            String multiplayerString = "";
            if (multiplayer > 1) {
                duetSingerNames = t.getDuetSingerNamesAsString();
                multiplayerString = multiplayer+"";
            }

            encoding = t.getEncoding();
            if (encoding==null) encoding="";

            if (!title.equals(s.getTitle())) {
                changed = true;
                s.setTitle(title);
            }
            if (!artist.equals(s.getArtist())) {
                changed = true;
                s.setArtist(artist);

                String sorted_artist = null;
                if (moveArticles) {
                    sorted_artist = getSortedArtist(artist, language);
                }
                s.setSortedArtist(sorted_artist);
            }
            if (!duetSingerNames.equals(s.getDuetSingerNames())) {
                changed = true;
                s.setDuetSingerNames(duetSingerNames);
            }
            if (!mp3.equals(s.getMP3())) {
                changed = true;
                s.setMP3(mp3);
            }
            if (!cover.equals(s.getCover())) {
                changed = true;
                s.setCover(cover);
            }
            if (!background.equals(s.getBackground())) {
                changed = true;
                s.setBackground(background);
            }
            if (!video.equals(s.getVideo())) {
                changed = true;
                s.setVideo(video);
            }
            if (!edition.equals(s.getEdition())) {
                changed = true;
                s.setEdition(edition);
            }
            if (!genre.equals(s.getGenre())) {
                changed = true;
                s.setGenre(genre);
            }
            if (!language.equals(s.getLanguage())) {
                changed = true;
                s.setLanguage(language);
            }
            if (!year.equals(s.getYear())) {
                changed = true;
                s.setYear(year);
            }

            if (!vgap.equals(s.getVideoGap())) {
                changed = true;
                s.setVideoGap(vgap);
            }
            if (!start.equals(s.getStart())) {
                changed = true;
                s.setStart(start);
            }
            if (!end.equals(s.getEnd())) {
                changed = true;
                s.setEnd(end);
            }
            if (!rel.equals(s.getRelative())) {
                changed = true;
                s.setRelative(rel);
            }
            if (!bpm.equals(s.getBPM())) {
                changed = true;
                s.setBPM(bpm);
            }
            if (!gap.equals(s.getGap())) {
                changed = true;
                s.setGap(gap);
            }
            if (!album.equals(s.getAlbum())) {
                changed = true;
                s.setAlbum(album);
            }
            if (!id.equals(s.getID())) {
                changed = true;
                s.setID(id);
            }
            if (!versionid.equals(s.getVersionID())) {
                changed = true;
                s.setVersionID(versionid);
            }
            if (!length.equals(s.getLength())) {
                changed = true;
                s.setLength(length);
            }
            if (!previewstart.equals(s.getPreviewStart())) {
                changed = true;
                s.setPreviewStart(previewstart);
            }
            if (!medleystartbeat.equals(s.getMedleyStartBeat())) {
                changed = true;
                s.setMedleyStartBeat(medleystartbeat);
            }
            if (!medleyendbeat.equals(s.getMedleyEndBeat())) {
                changed = true;
                s.setMedleyEndBeat(medleyendbeat);
            }

            if (!multiplayerString.equals(s.getMultiplayer())) {
                changed = true;
                s.setMultiplayer(multiplayerString);
            }
            if (!duetSingerNames.equals(s.getDuetSingerNames())) {
                changed = true;
                s.setDuetSingerNames(duetSingerNames);
            }
            if (!encoding.equals(s.getEncoding())) {
                changed = true;
                s.setEncoding(encoding);
            }

            if (showLyrics) {
                String txt = t.getText().trim();
                txt = txt.replaceAll("[-|~|\r|" + YassRow.HYPHEN + "]", "");
                //txt = txt.replaceAll("[\n]", "<br>");
                s.setLyrics(txt);
                changed = true;
            }

            s.clearMessages();
            if (showErrors) {
                auto.checkData(t, true, true);
                Hashtable<?, ?> hash = tm.collectMessages();
                for (int j = 0; j < YassRow.ALL_MESSAGES.length; j++) {
                    Vector<?> msg = (Vector<?>) hash.get(YassRow.ALL_MESSAGES[j]);
                    if (msg != null) {
                        s.setMessageAt(j, msg.size() + "");
                    } else {
                        s.setMessageAt(j, null);
                    }
                    changed = true;
                }
            }
            s.clearStats();
            if (showStats) {
                for (YassStats st: YassStats.getAllStats()) {
                    st.calcStats(s, t);
                    changed = true;
                }
            }
        }
        return changed;
    }

    /**
     * Description of the Method
     */
    public void focusFirstVisible() {
        SwingUtilities.invokeLater(
                () -> {
                    if (getRowCount() < 1) {
                        return;
                    }
                    requestFocusInWindow();
                    changeSelection(0, 0, false, false);
                });

    }

    /**
     * Gets the playLists attribute of the YassSongList object
     *
     * @return The playLists value
     */
    public Vector<String> getPlayLists() {
        return pldata;
    }

    /**
     * Description of the Method
     */
    public void setDefaults() {
        StringBuffer sb = new StringBuffer();
        int ww[] = new int[FIRST_MSG_COLUMN + YassRow.ALL_MESSAGES.length];
        for (int i = 0; i < ww.length; i++) {
            ww[i] = getColumnModel().getColumn(i).getWidth();
            sb.append(ww[i] + "");
            if (i < ww.length - 1) {
                sb.append(":");
            }
        }
        prop.setProperty("songlist-column-width", sb.toString());
    }

    /**
     * Description of the Method
     */
    public void storeCache() {
        String cacheName = prop.getProperty("songlist-cache");
        File cache = new File(cacheName);
        File p = cache.getParentFile();
        if (p != null) {
            p.mkdirs();
        }

        Vector<YassSong> data = getUnfilteredData();
        List<String> songcacheList = data.stream().map(song -> song.toString()).collect(Collectors.toList());
        Path songCache = Paths.get(cache.getAbsolutePath());
        fileUtils.writeFile(songcacheList, songCache);

        if (pldata != null) {
            String plcacheName = prop.getProperty("playlist-cache");
            File plcache = new File(plcacheName);
            if (plcache.exists()) {
                if (!plcache.delete()) {
                    System.out.println("Error: Cannot delete playlist cache.");
                }
            }
            File pp = plcache.getParentFile();
            if (pp != null) {
                pp.mkdirs();
            }

            List<String> plCacheList = pldata.stream().collect(Collectors.toList());
            Path plCachePath = Paths.get(plcache.getAbsolutePath());
            fileUtils.writeFile(plCacheList, plCachePath);
        } else {
            String plcacheName = prop.getProperty("playlist-cache");
            File plcache = new File(plcacheName);
            if (plcache.exists()) {
                plcache.delete();
            }
        }
    }

    /**
     * Description of the Method
     */
    public void batchCorrect() {
        String dir = prop.getProperty("song-directory");
        if (dir == null) {
            return;
        }

        TableColumnModel colModel = getColumnModel();
        SongHeaderRenderer hr = null;
        Vector<String> msg = new Vector<>();
        for (int i = 0; i < YassRow.ALL_MESSAGES.length; i++) {
            hr = (SongHeaderRenderer) colModel.getColumn(FIRST_MSG_COLUMN + i).getHeaderRenderer();
            if (hr.isSelected()) {
                msg.addElement(YassRow.ALL_MESSAGES[i]);
            }
        }

        JLabel label = new JLabel(I18.get("mpop_correct_label"));

        JPanel songPanel = new JPanel(new BorderLayout());
        JRadioButton song1Button = new JRadioButton(I18.get("mpop_correct_selected"));
        song1Button.setMnemonic(KeyEvent.VK_S);
        song1Button.setSelected(true);
        JRadioButton song2Button = new JRadioButton(I18.get("mpop_correct_all"));
        song2Button.setMnemonic(KeyEvent.VK_A);
        ButtonGroup group = new ButtonGroup();
        group.add(song1Button);
        group.add(song2Button);

        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.add("West", new JLabel(I18.get("mpop_correct_errors"), JLabel.RIGHT));
        JRadioButton msg1Button = new JRadioButton(I18.get("mpop_correct_selected"));
        msg1Button.setMnemonic(KeyEvent.VK_E);
        JRadioButton msg2Button = new JRadioButton(I18.get("mpop_correct_all"));
        msg2Button.setMnemonic(KeyEvent.VK_L);
        if (msg.size() > 0) {
            msg1Button.setSelected(true);
        } else {
            msg2Button.setSelected(true);
        }
        ButtonGroup group2 = new ButtonGroup();
        group2.add(msg1Button);
        group2.add(msg2Button);

        JPanel sPanel = new JPanel(new GridLayout(2, 3));
        sPanel.add(new JLabel(I18.get("mpop_correct_songs"), JLabel.LEFT));
        sPanel.add(song1Button);
        sPanel.add(song2Button);
        sPanel.add(new JLabel(I18.get("mpop_correct_errors"), JLabel.LEFT));
        sPanel.add(msg1Button);
        sPanel.add(msg2Button);
        sPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        songPanel.add("Center", sPanel);

        JPanel all = new JPanel(new BorderLayout());
        all.add("North", label);
        all.add("Center", sPanel);

        all.add("South", new JLabel(I18.get("mpop_correct_ok")));
		/*
		 *  JCheckBox check = new JCheckBox("Create AUTO version");
		 *  check.setToolTipText("<html>If selected, the corrected file will be stored as version<br><b>Artist - Title [AUTO].txt</b><br>and the TITLE tag will be extended with [AUTO].<br>Existing versions will be extended with [..._AUTO]. <br><br><b>Deselecting this option will remove any AUTO version.</html>");
		 *  check.setSelected(true);
		 *  all.add("South", check);
		 */
        int ok = JOptionPane.showConfirmDialog(actions.createOwnerFrame(), all, I18.get("mpop_correct_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }

        if (autoAction != null) {
            autoAction.setEnabled(false);
        }

        batchProcess(song2Button.isSelected(), (msg1Button.isSelected() ? msg : null));
    }

    /**
     * Description of the Method
     */
    public void exportMetadata() {
        Vector<YassSong> v = getSelectedSongs();
        int n = v.size();

        if (v == null || n < 1) {
            return;
        }

        JLabel label = new JLabel(MessageFormat.format(I18.get("mpop_export_label"), n));

        JCheckBox g;

        JCheckBox e;

        JCheckBox l;

        JCheckBox y;

        JCheckBox f;
        JCheckBox a;
        JCheckBox ii;
        JPanel sPanel = new JPanel(new GridLayout(2, 4));
        sPanel.add(g = new JCheckBox(I18.get("mpop_export_genre")));
        sPanel.add(e = new JCheckBox(I18.get("mpop_export_edition")));
        sPanel.add(l = new JCheckBox(I18.get("mpop_export_language")));
        sPanel.add(y = new JCheckBox(I18.get("mpop_export_year")));
        sPanel.add(f = new JCheckBox(I18.get("mpop_export_folder")));
        sPanel.add(a = new JCheckBox(I18.get("mpop_export_album")));
        sPanel.add(ii = new JCheckBox(I18.get("mpop_export_id")));
        g.setSelected(true);
        e.setSelected(true);
        l.setSelected(true);
        y.setSelected(true);
        f.setSelected(true);
        a.setSelected(true);
        ii.setSelected(true);

        sPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel all = new JPanel(new BorderLayout());
        all.add("North", label);
        all.add("Center", sPanel);

        int ok = JOptionPane.showConfirmDialog(actions.getTab(), all, I18.get("mpop_export_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }

        boolean useGenre = g.isSelected();

        boolean useEdition = e.isSelected();

        boolean useLanguage = l.isSelected();

        boolean useYear = y.isSelected();

        boolean useFolder = f.isSelected();
        boolean useAlbum = a.isSelected();
        boolean useID = ii.isSelected();

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(I18.get("mpop_export_filename")));
        if (chooser.showSaveDialog(actions.getTab()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();

        PrintWriter outputStream = null;
        try {
            outputStream = new PrintWriter(new FileWriter(file));
            outputStream.print("# ARTIST");
            outputStream.print("\t");
            outputStream.print("TITLE");
            if (useGenre) {
                outputStream.print("\t");
                outputStream.print("GENRE");
            }
            if (useEdition) {
                outputStream.print("\t");
                outputStream.print("EDITION");
            }
            if (useLanguage) {
                outputStream.print("\t");
                outputStream.print("LANGUAGE");
            }
            if (useYear) {
                outputStream.print("\t");
                outputStream.print("YEAR");
            }
            if (useFolder) {
                outputStream.print("\t");
                outputStream.print("FOLDER");
            }
            if (useAlbum) {
                outputStream.print("\t");
                outputStream.print("ALBUM");
            }
            if (useID) {
                outputStream.print("\t");
                outputStream.print("ID");
            }
            outputStream.println();

            for (Enumeration<YassSong> en = v.elements(); en.hasMoreElements(); ) {
                YassSong s = en.nextElement();
                String artist = s.getArtist();
                String title = s.getTitle();
                String genre = s.getGenre();
                if (genre == null) {
                    genre = "";
                }
                String edition = s.getEdition();
                if (edition == null) {
                    edition = "";
                }
                String language = s.getLanguage();
                if (language == null) {
                    language = "";
                }
                String year = s.getYear();
                if (year == null) {
                    year = "";
                }
                String folder = s.getFolder();
                if (folder == null) {
                    folder = "";
                }
                String album = s.getAlbum();
                if (album == null) {
                    album = "";
                }
                String id = s.getID();
                if (id == null) {
                    id = "";
                }

                outputStream.print(artist);
                outputStream.print("\t");
                outputStream.print(title);

                if (useGenre) {
                    outputStream.print("\t");
                    outputStream.print(genre);
                }
                if (useEdition) {
                    outputStream.print("\t");
                    outputStream.print(edition);
                }
                if (useLanguage) {
                    outputStream.print("\t");
                    outputStream.print(language);
                }
                if (useYear) {
                    outputStream.print("\t");
                    outputStream.print(year);
                }
                if (useFolder) {
                    outputStream.print("\t");
                    outputStream.print(folder);
                }
                if (useAlbum) {
                    outputStream.print("\t");
                    outputStream.print(album);
                }
                if (useID) {
                    outputStream.print("\t");
                    outputStream.print(id);
                }
                outputStream.println();
            }
            outputStream.close();
        } catch (Exception ex) {
            System.out.println("Metadata Write Error:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Description of the Method
     */
    public void importMetadata() {
        JLabel label = new JLabel(I18.get("mpop_import_label"));

        JCheckBox g;

        JCheckBox e;

        JCheckBox l;

        JCheckBox y;
        JCheckBox a;
        JCheckBox ii;
        JPanel sPanel = new JPanel(new GridLayout(2, 3));
        sPanel.add(g = new JCheckBox(I18.get("mpop_import_genre")));
        sPanel.add(e = new JCheckBox(I18.get("mpop_import_edition")));
        sPanel.add(l = new JCheckBox(I18.get("mpop_import_language")));
        sPanel.add(y = new JCheckBox(I18.get("mpop_import_year")));
        sPanel.add(a = new JCheckBox(I18.get("mpop_import_album")));
        sPanel.add(ii = new JCheckBox(I18.get("mpop_import_id")));

        sPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel wlabel = new JLabel(I18.get("mpop_import_ok"));

        JPanel all = new JPanel(new BorderLayout());
        all.add("North", label);
        all.add("Center", sPanel);
        all.add("South", wlabel);

        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(actions.getTab()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();

        int ok = JOptionPane.showConfirmDialog(actions.getTab(), all, I18.get("mpop_import_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        boolean useGenre = g.isSelected();
        boolean useEdition = e.isSelected();
        boolean useLanguage = l.isSelected();
        boolean useYear = y.isSelected();
        boolean useAlbum = a.isSelected();
        boolean useID = ii.isSelected();

        try {
            BufferedReader inputStream = new BufferedReader(new FileReader(file));
            String line;
            String[] col = new String[10];
            int ci = 0;
            String artist = null;
            String title = null;
            String genre = null;
            String edition = null;
            String language = null;
            String year = null;
            String album = null;
            String id = null;
            YassTable t = new YassTable();
            t.init(prop);
            while ((line = inputStream.readLine()) != null) {
                //System.out.println("line: "+line);
                ci = 0;
                if (line.startsWith("#")) {
                    line = line.substring(1);
                    line = line.trim();
                    StringTokenizer st = new StringTokenizer(line, "\t");
                    while (st.hasMoreTokens() && ci < 10) {
                        col[ci] = st.nextToken().toUpperCase();
                        ci++;
                    }
                    continue;
                }

                String tokens[] = line.split("\t");
                artist = null;
                title = null;
                genre = "";
                edition = "";
                language = "";
                year = "";
                album = "";
                id = "";
                for (int k = 0; k < tokens.length && ci < 10; k++) {
                    if (col[ci] == null) {
                        break;
                    }

                    String token = tokens[k];
                    if (token == null) {
                        token = "";
                    }
                    if (col[ci].equals("ARTIST")) {
                        artist = token;
                    } else if (col[ci].equals("TITLE")) {
                        title = token;
                    } else if (useGenre && col[ci].equals("GENRE")) {
                        genre = token;
                    } else if (useEdition && col[ci].equals("EDITION")) {
                        edition = token;
                    } else if (useLanguage && col[ci].equals("LANGUAGE")) {
                        language = token;
                    } else if (useYear && col[ci].equals("YEAR")) {
                        year = token;
                    } else if (useYear && col[ci].equals("ALBUM")) {
                        album = token;
                    } else if (useYear && col[ci].equals("ID")) {
                        id = token;
                    }
                    ci++;
                }
                if (artist == null || title == null) {
                    continue;
                }
				/*
				 *  System.out.println("extract genre: "+genre);
				 *  System.out.println("extract edition: "+edition);
				 *  System.out.println("extract language: "+language);
				 *  System.out.println("extract year: "+year);
				 *  System.out.println();
				 */
                Vector<YassSong> v = findSong(artist, title);
                for (Enumeration<YassSong> en = v.elements(); en.hasMoreElements(); ) {
                    YassSong s = en.nextElement();

                    boolean changed = false;
                    if (useGenre) {
                        String old = s.getGenre();
                        if (old == null) {
                            old = "";
                        }
                        if (!genre.equals(old)) {
                            changed = true;
                            s.setGenre(genre);
                            //System.out.println("new genre: "+s.getArtist()+" - "+s.getTitle()+":" + s.getGenre());
                        }
                    }
                    if (useEdition) {
                        String old = s.getEdition();
                        if (old == null) {
                            old = "";
                        }
                        if (!edition.equals(old)) {
                            changed = true;
                            s.setEdition(edition);
                            //System.out.println("new edition: "+s.getArtist()+" - "+s.getTitle()+":" + s.getEdition());
                        }
                    }
                    if (useLanguage) {
                        String old = s.getLanguage();
                        if (old == null) {
                            old = "";
                        }
                        if (!language.equals(old)) {
                            changed = true;
                            s.setLanguage(language);
                            //System.out.println("new language: "+s.getArtist()+" - "+s.getTitle()+":" + s.getLanguage());
                        }
                    }
                    if (useYear) {
                        String old = s.getYear();
                        if (old == null) {
                            old = "";
                        }
                        if (!year.equals(old)) {
                            changed = true;
                            s.setYear(year);
                            //System.out.println("new year: "+s.getArtist()+" - "+s.getTitle()+":" + s.getYear());
                        }
                    }
                    if (useAlbum) {
                        String old = s.getAlbum();
                        if (old == null) {
                            old = "";
                        }
                        if (!album.equals(old)) {
                            changed = true;
                            s.setAlbum(album);
                        }
                    }
                    if (useID) {
                        String old = s.getID();
                        if (old == null) {
                            old = "";
                        }
                        if (!id.equals(old)) {
                            changed = true;
                            s.setID(id);
                        }
                    }
                    if (changed) {
                        storeSongDetails(s, t);
                    }
                }
            }
            inputStream.close();

            sm.fireTableDataChanged();
            storeCache();
        } catch (Exception ex) {
            System.out.println("Metadata Read Error:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Description of the Method
     *
     * @param all Description of the Parameter
     * @param msg Description of the Parameter
     */
    public void batchProcess(boolean all, Vector<?> msg) {
        String dir = prop.getProperty("song-directory");
        if (dir == null) {
            return;
        }

        corrector = new CorrectorThread();
        corrector.allsongs = all;
        corrector.messages = msg;
        corrector.backup = false;
        corrector.dir = dir;
        corrector.renameFiles = false;
        corrector.start();
    }

    /**
     * Description of the Method
     *
     * @param all Description of the Parameter
     */
    public void batchRename(boolean all) {
        String dir = prop.getProperty("song-directory");
        if (dir == null) {
            return;
        }

        corrector = new CorrectorThread();
        corrector.allsongs = all;
        corrector.messages = null;
        corrector.backup = false;
        corrector.dir = dir;
        corrector.renameFiles = true;
        corrector.coverID = prop.getProperty("cover-id");
        corrector.backgroundID = prop.getProperty("background-id");
        corrector.videoID = prop.getProperty("video-id");
        corrector.videodirID = prop.getProperty("videodir-id");
        corrector.start();
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean renderLocked() {
        return renderLocked;
    }

    /**
     * Description of the Method
     *
     * @param onoff Description of the Parameter
     */
    public void renderLocked(boolean onoff) {
        renderLocked = onoff;
    }

    /**
     * Description of the Method
     *
     * @param line The new previewStart value
     */

    public void setPreviewStart(int line) {
        if (line < 1) {
            line = 1;
        }
        int[] rows = getSelectedRows();
        if (rows == null || rows.length != 1) {
            return;
        }

        YassSong s = sm.getRowAt(rows[0]);

        String txt = s.getDirectory() + File.separator + s.getFilename();
        YassTable t = new YassTable();
        t.removeAllRows();
        t.loadFile(txt);
        double gap = t.getGap();
        double bpm = t.getBPM();

        int row = t.getPage(line);
        YassRow r = t.getRowAt(row);
        int beat = r.getBeatInt();
        double in = 60 * beat / (4.0 * bpm) + gap / 1000.0;
        if (in < 0) {
            in = 0;
        }
        in = ((int) (in * 100)) / 100.0;
        s.setPreviewStart(in + "");
        s.setSaved(false);
        setSaved(false);
        repaint();
    }

    /**
     * Sets the medleyStartEnd attribute of the YassSongList object
     *
     * @param lineIn  The new medleyStartEnd value
     * @param lineOut The new medleyStartEnd value
     */
    public void setMedleyStartEnd(int lineIn, int lineOut) {
        if (lineIn < 1) {
            lineIn = 1;
        }
        if (lineOut < lineIn) {
            lineOut = lineIn;
        }
        int[] rows = getSelectedRows();
        if (rows == null || rows.length != 1) {
            return;
        }

        YassSong s = sm.getRowAt(rows[0]);

        String txt = s.getDirectory() + File.separator + s.getFilename();
        YassTable t = new YassTable();
        t.removeAllRows();
        t.loadFile(txt);

        int row = t.getPage(lineIn);
        YassRow r = t.getRowAt(row);
        int beat = r.getBeatInt();
        s.setMedleyStartBeat(beat + "");

        int row2 = t.getPage(lineOut);
        YassRow r2 = t.getRowAt(row2);
        while (r2.isNote()) {
            r2 = t.getRowAt(++row2);
        }
        r2 = t.getRowAt(--row2);

        int beat2 = r2.getBeatInt() + r2.getLengthInt();
        s.setMedleyEndBeat(beat2 + "");

        s.setSaved(false);
        setSaved(false);
        repaint();
    }

    /**
     * Description of the Method
     */
    public void startPlaying() {
        startPlaying(0, -1);
    }

    /**
     * Description of the Method
     *
     * @param firstLine Description of the Parameter
     * @param lastLine  Description of the Parameter
     */
    public void startPlaying(int firstLine, int lastLine) {
        int i = getSelectedRow();
        if (i < 0 || i >= getRowCount()) {
            return;
        }

        YassSong s = sm.getRowAt(i);
        String dir = s.getDirectory();
        String audio = s.getMP3();
        if (dir == null || audio == null) {
            return;
        }
        String filename = dir + File.separator + audio;

        YassPlayer mp3 = actions.getMP3();
        mp3.setHasPlaybackRenderer(false);
        mp3.openMP3(filename);

        String txt = s.getDirectory() + File.separator + s.getFilename();
        YassTable t = new YassTable();
        t.removeAllRows();
        t.loadFile(txt);
        double gap = t.getGap();
        double bpm = t.getBPM();

        double previewStart = t.getPreviewStart();

        long in = 0;
        long out = -1;

        if (firstLine > lastLine || firstLine < 0) {
            if (previewStart >= 0) {
                in = (long) (previewStart * 1000000L);
            } else {
                YassRow r = t.getFirstNote();
                int beat = r.getBeatInt();
                in = (long) ((60 * beat / (4.0 * bpm) + gap / 1000.0) * 1000000L);
                in -= 1000;
                if (in < 0) {
                    in = 0;
                }
            }
        } else {
            int row = t.getPage(firstLine);
            YassRow r = t.getRowAt(row);
            int beat = r.getBeatInt();
            in = (long) ((60 * beat / (4.0 * bpm) + gap / 1000.0) * 1000000L);
            if (in < 0) {
                in = 0;
            }
            int row2 = t.getPage(lastLine);
            YassRow r2 = t.getRowAt(row2);
            while (r2.isNote()) {
                r2 = t.getRowAt(++row2);
            }
            r2 = t.getRowAt(row2 - 1);
            int beat2 = r2.getBeatInt() + r2.getLengthInt() + 1;
            out = (long) ((60 * beat2 / (4.0 * bpm) + gap / 1000.0) * 1000000L);
            out += 1000;
        }

        mp3.playSelection(in, out, null);
    }

    /**
     * Description of the Method
     */
    public void stopPlaying() {
        YassPlayer mp3 = actions.getMP3();
        mp3.setHasPlaybackRenderer(false);
        mp3.interruptMP3();
        mp3.setHasPlaybackRenderer(true);

		/*
		 *  if (player != null) {
		 *  player.close();
		 *  }
		 *  player = null;
		 */
    }

    /**
     * Gets the unfilteredData attribute of the YassSongList object
     *
     * @return The unfilteredData value
     */
    public Vector<YassSong> getUnfilteredData() {
        if (allData == null) {
            return sm.getData();
        }
        return allData;
    }

    /**
     * Gets the preFilter attribute of the YassSongList object
     *
     * @return The preFilter value
     */
    public YassFilter getPreFilter() {
        return prefilter;
    }

    /**
     * Sets the preFilter attribute of the YassSongList object
     *
     * @param f The new preFilter value
     */
    public void setPreFilter(YassFilter f) {
        prefilter = f;
    }

    /**
     * Description of the Method
     *
     * @param onoff Description of the Parameter
     */
    public void filterLyrics(boolean onoff) {
        filterAll = onoff;
    }

    /**
     * Description of the Method
     *
     * @param str Description of the Parameter
     */
    public void filter(String str) {
        interruptFilter();
        filterThread = new FilterThread(str);
        filterThread.start();
    }

    /**
     * Description of the Method
     */
    public void interruptFilter() {
        if (filterThread != null) {
            YassFilter.isInterrupted = true;
            filterThread.isInterrupted = true;
            int n = 10;
            while (!filterThread.isFinished && --n > 0) {
                try {
                    Thread.currentThread().wait(100);
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean cacheLyrics() {
        String cacheName = prop.getProperty("lyrics-cache");

        BufferedWriter bw = null;
        OutputStreamWriter osw = null;
        FileOutputStream fos = null;

        PrintWriter outputStream = null;
        try {
            File f = new File(cacheName);
            if (f.exists()) {
                f.delete();
            }

            fos = new FileOutputStream(f, false);
            osw = new OutputStreamWriter(fos, "UTF-8");
            bw = new BufferedWriter(osw);
            outputStream = new PrintWriter(bw);

            for (Enumeration<YassSong> e = allData.elements(); e.hasMoreElements(); ) {
                YassSong s = e.nextElement();
                String txt = s.getLyrics();
                if (txt == null) {
                    continue;
                }
                outputStream.println("#SONG:" + s.getDirectory() + File.separator + s.getFilename());
                outputStream.println(txt);
            }
            outputStream.close();
            return true;
        } catch (Exception ignored) {}
        finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (osw != null) {
                    osw.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean loadLyricsCache() {
        String cacheName = prop.getProperty("lyrics-cache");

        if (!new File(cacheName).exists()) {
            return false;
        }

        lyricsCache = new Hashtable<>(2000);
        unicode.UnicodeReader r = null;
        BufferedReader inputStream = null;
        FileInputStream fis = null;
        try {
            r = new unicode.UnicodeReader(fis = new FileInputStream(cacheName), null);
            inputStream = new BufferedReader(r);

            String l;
            String key = null;
            StringBuffer sb = new StringBuffer(4000);
            while ((l = inputStream.readLine()) != null) {
                if (l.startsWith("#SONG:")) {
                    if (key != null) {
                        lyricsCache.put(key, sb.toString());
                        sb = new StringBuffer(4000);
                    }
                    key = l.substring(6);
                } else {
                    sb.append(l + "\n");
                }
            }
            inputStream.close();
            r.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Description of the Method
     */
    public void removeAllSongs() {
        sm.getData().clear();
        sm.fireTableDataChanged();
    }

    public YassSong getSong(String artist, String title) {
        if (artist == null || title == null)
            return null;
        for (YassSong s: sm.getData()) {
            String artist2 = s.getArtist();
            String title2 = s.getTitle();
            if (artist2 == null || title2 == null)
                continue;
            if (artist.equals(artist2) && title.equals(title2))
                return s;
        }
        return null;
    }

    /**
     * Description of the Method
     *
     * @param title   Description of the Parameter
     * @param artist  Description of the Parameter
     * @return Description of the Return Value
     */
    public Vector<YassSong> findSong(String artist, String title) {
        Vector<YassSong> hits = new Vector<>();
        if (artist == null || title == null) {
            return null;
        }
        artist = artist.toLowerCase().trim();
        title = title.toLowerCase().trim();

        YassSong s = null;
        Vector<YassSong> data = sm.getData();
        for (Enumeration<YassSong> en = data.elements(); en.hasMoreElements(); ) {
            s = en.nextElement();
            String artist2 = s.getArtist().toLowerCase().trim();
            String title2 = s.getTitle().toLowerCase().trim();
            if (artist2.equals(artist) && (title2.startsWith(title) || title.startsWith(title2))) {
                hits.addElement(s);
            }
        }
        if (hits.size() < 1) {
            return null;
        }
        return hits;
    }

    /**
     * Description of the Method
     *
     */
    class CacheCover extends Thread {
        YassSong s;
        int rowIndex;


        /**
         * Constructor for the CacheCover object
         *
         * @param s        Description of the Parameter
         * @param rowIndex Description of the Parameter
         */
        public CacheCover(YassSong s, int rowIndex) {
            this.s = s;
            this.rowIndex = rowIndex;
            setPriority(NORM_PRIORITY - 1);
        }


        /**
         * Main processing method for the CacheCover object
         */
        public void run() {
            imageCacheName = prop.getProperty("songlist-imagecache");
            File f = new File(imageCacheName);
            if (!f.exists()) {
                f.mkdirs();
            }

            String at = YassSong.toFilename(s.getArtist() + " - " + s.getTitle() + " @ " + s.getFolder());
            File cacheFile = new File(imageCacheName + File.separator + at + ".jpg");

            if (cacheSongCover(imageCacheName, s)) {
                try {
                    Image img = javax.imageio.ImageIO.read(cacheFile);
                    s.setIcon(new ImageIcon(img));
                } catch (Exception e) {
                    s.setIcon(null);
                }
            }
            repaintIfVisible(rowIndex);
        }
    }

    /**
     * Description of the Class
     *
     * @author Saruta
     */
    class SortByAction implements ActionListener {
        private final int col;


        /**
         * Constructor for the SortByAction object
         *
         * @param c Description of the Parameter
         */
        public SortByAction(int c) {
            col = c;
        }


        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            sortBy(col);
        }
    }

    /**
     * Description of the Class
     *
     * @author Saruta
     */
    class WorkerThread extends Thread {
        /**
         * Description of the Field
         */
        public boolean notInterrupted = true, all = true;

        /**
         * Description of the Field
         */
        public int state = 0;
        String dir = null;
        String libmsgsearch = null;


        /**
         * Constructor for the WorkerThread object
         *
         * @param d   Description of the Parameter
         * @param all Description of the Parameter
         */
        public WorkerThread(boolean all, String d) {
            dir = d;
            this.all = all;
        }

        /**
         * Main processing method for the WorkerThread object
         */
        public void run() {
            preventInteraction = true;
            state = YassSongListEvent.STARTING;
            fireSongListChanged(state);

            Vector<YassSong> newdata = null;
            if (all) {
                state = YassSongListEvent.UPDATING;
                fireSongListChanged(state);

                actions.setProgress(I18.get("lib_msg_search"));
                newdata = new Vector<>(3000, 1000);
                libmsgsearch = I18.get("lib_msg_search_n");
                collect(newdata, new File(dir), "");
            }

            Vector<YassSong> data = all ? newdata : sm.getData();

            state = YassSongListEvent.LOADING;
            fireSongListChanged(state);
            actions.setProgress(I18.get("lib_msg_load"), data.size());

            int i = 0;
            YassTable t = new YassTable();
            t.init(prop);
            String libmsgload = I18.get("lib_msg_load_n");
            for (Enumeration<?> e = data.elements(); e.hasMoreElements() && notInterrupted; ) {
                YassSong s = (YassSong) e.nextElement();
                if (loadSongDetails(s, t)) {
                    //repaintIfVisible(i);
                    actions.setProgress(++i);
                    actions.setProgress(MessageFormat.format(libmsgload, i), s.getDirectory());
                }
            }

            Collections.sort(data);

            if (all) {
                sm.getData().clear();
                sm.getData().addAll(newdata);
                allData = (Vector<YassSong>) sm.getData().clone();
                sm.fireTableDataChanged();
            }
            updateSelectionEdition();
            storeCache();

            state = YassSongListEvent.LOADED;
            loaded = true;
            fireSongListChanged(state);
            actions.setLibraryLoaded(true);
            preventInteraction = false;

            // keep prefilter and sortby
            filter(null);

            try {
                if (data.size() >= 0) {
                    setRowSelectionInterval(0, 0);
                }
            } catch (Exception ignored) {}

            actions.setProgress(0);
            actions.setProgress(MessageFormat.format(I18.get("lib_msg"), data.size()), dir);

            state = YassSongListEvent.FINISHED;
            fireSongListChanged(state);
        }

        /**
         * Description of the Method
         *
         * @param t      Description of the Parameter
         * @param d      Description of the Parameter
         * @param folder Description of the Parameter
         * @return Description of the Return Value
         */
        public boolean hasNewSongs(Hashtable<?, ?> t, File d, String folder) {
            File dirs[] = d.listFiles();
            if (dirs == null) {
                return false;
            }
            for (File dir1 : dirs) {
                if (!notInterrupted) {
                    return true;
                }
                String name = dir1.getName();
                String namelow = name.toLowerCase();
                if (dir1.isDirectory()) {
                    if (hasNewSongs(t, dir1, d.getName())) {
                        return true;
                    }
                } else {
                    if (namelow.endsWith(".txt") && quickCheck(dir1)) {
                        if (t.get(dir1.getAbsolutePath()) == null) {
                            //System.out.println("new file: " + dirs[i].getAbsolutePath());
                            return true;
                        }
                        actions.setProgress(I18.get("lib_msg_search"), d.getPath());
                    }
                }
                Thread.yield();
            }
            return false;
        }

        /**
         * Description of the Method
         *
         * @param d      Description of the Parameter
         * @param folder Description of the Parameter
         * @param data   Description of the Parameter
         */
        public void collect(Vector<YassSong> data, File d, String folder) {
            File dirs[] = d.listFiles();
            if (dirs != null) {
                for (int i = 0; i < dirs.length && notInterrupted; i++) {
                    String name = dirs[i].getName();
                    String namelow = name.toLowerCase();
                    if (dirs[i].isDirectory()) {
                        collect(data, dirs[i], d.getName());
                    } else {
                        if (namelow.endsWith(songFileType) && quickCheck(dirs[i])) {
                            data.addElement(new YassSong(d.getAbsolutePath(), folder, name, "", ""));
                            actions.setProgress(MessageFormat.format(libmsgsearch, data.size()), d.getPath());
                        }
                        if (namelow.endsWith(playlistFileType) && YassPlayList.isPlayList(dirs[i])) {
                            pldata.addElement(dirs[i].getAbsolutePath());
                        }
                    }
                }
            }
        }


        /**
         * Description of the Method
         *
         * @param f Description of the Parameter
         * @return Description of the Return Value
         */
        public boolean quickCheck(File f) {
            if (f.length() > 1024 * 1024) {
                return false;
            }
            try {
                char cstr[] = new char[8];

                //BufferedReader inputStream = new BufferedReader(new FileReader(f));
                unicode.UnicodeReader r = new unicode.UnicodeReader(new FileInputStream(f), null);
                BufferedReader inputStream = new BufferedReader(r);

                int len;
                int c = inputStream.read();
                while (c == '#') {
                    len = inputStream.read(cstr, 0, 8);
                    if (len < 8) {
                        inputStream.close();
                        return false;
                    }
                    String headerTag = new String(cstr).trim().toUpperCase();
                    if (HeaderEnum.isValidHeader(headerTag)) {
                        inputStream.close();
                        return true;
                    } else {
                        System.out.println("Invalid Header Tag for " + f.getName());
                    }
                    c = inputStream.read();
                }
                inputStream.close();
            } catch (Exception e) {
            }
            return false;
        }
    }

    /**
     * Constructor for the ThumbnailerThread object
     *
     */
    class ThumbnailerThread extends Thread {
        String dir;
        boolean notInterrupted = true;


        /**
         * Constructor for the ThumnailerThread object
         *
         * @param d Description of the Parameter
         */
        public ThumbnailerThread(String d) {
            dir = d;
        }


        /**
         * Main processing method for the WorkerThread object
         */
        public void run() {
            int state = YassSongListEvent.STARTING;
            fireSongListChanged(state);

            Vector<YassSong> data = sm.getData();
            state = YassSongListEvent.LOADING;
            fireSongListChanged(state);
            actions.setProgress(I18.get("lib_msg_load"), data.size());

            imageCacheName = prop.getProperty("songlist-imagecache");
            File f = new File(imageCacheName);
            if (!f.exists()) {
                f.mkdirs();
            }

            state = YassSongListEvent.THUMBNAILING;
            fireSongListChanged(state);
            actions.setProgress(I18.get("lib_msg_thumb"), data.size());
            int i = 0;
            for (Enumeration<YassSong> en = data.elements(); en.hasMoreElements() && notInterrupted; ) {
                YassSong s = en.nextElement();
                if (cacheSongCover(imageCacheName, s)) {
                    repaintIfVisible(i);
                    sm.fireTableRowsUpdated(i, i);
                }
                actions.setProgress(++i);
                actions.setProgress(MessageFormat.format(I18.get("lib_msg_load"), i), s.getDirectory());
            }

            actions.setProgress(0);
            actions.setProgress(MessageFormat.format(I18.get("lib_msg"), data.size()), dir);

            state = YassSongListEvent.FINISHED;
            fireSongListChanged(state);
        }
    }

    class StoreThread extends Thread {
        public boolean started = false, finished = false, notInterrupted = true;


        public void run() {
            started = true;
            finished = false;
            notInterrupted = true;

            int[] rows = getSelectedRows();

            actions.setLibraryLoaded(false);

            YassTable t = new YassTable();
            t.init(prop);
            Vector<YassSong> data = getUnfilteredData();
            actions.setProgress(I18.get("lib_msg_store"), data.size());
            int i = 0;
            for (Enumeration<YassSong> en = data.elements(); en.hasMoreElements() && notInterrupted; ) {
                YassSong s = en.nextElement();
                if (!s.isSaved()) {
                    storeSongDetails(s, t);
                }
                actions.setProgress(++i);
                actions.setProgress(MessageFormat.format(I18.get("lib_msg_store_n"), i), s.getDirectory());
                Thread.yield();
            }

            if (notInterrupted) {
                sm.fireTableDataChanged();
                setSaved(true);
                storeCache();
            }

            actions.setProgress(0);
            actions.setProgress(MessageFormat.format(I18.get("lib_msg"), data.size()), "");
            actions.setLibraryLoaded(true);

            if (rows != null) {
                clearSelection();
                for (i = 0; i < rows.length; i++) {
                    addRowSelectionInterval(rows[i], rows[i]);
                }
            }
            finished = true;
        }
    }

    /**
     * Description of the Class
     *
     */
    class CorrectorThread extends Thread {
        /**
         * Description of the Field
         */
        public Vector<?> messages = null;
        /**
         * Description of the Field
         */
        public boolean notInterrupted = true, finished = false, backup = true, allsongs = false;
        /**
         * Description of the Field
         */
        public String dir = null;
        /**
         * Description of the Field
         */
        public boolean renameFiles = false;
        String coverID, backgroundID, videoID, videodirID;

        /**
         * Main processing method for the CorrectorThread object
         */
        public void run() {
            Vector<YassSong> data = sm.getData();
            String filename;
            int i = 0;

            int n = allsongs ? data.size() : getSelectedRows().length;
            actions.setProgress(I18.get("lib_msg_correct"), n);

            YassTable t = new YassTable();
            YassTable t2 = new YassTable();
            for (Enumeration<YassSong> e = data.elements(); e.hasMoreElements() && notInterrupted; ) {
                YassSong s = e.nextElement();

                if (!allsongs && !isRowSelected(i)) {
                    i++;
                    continue;
                }

                System.out.println("Process: " + s.getArtist() + " - " + s.getFilename());
                filename = s.getDirectory() + File.separator + s.getFilename();
                t.removeAllRows();
                t.init(prop);
                try {
                    if (t.loadFile(filename)) {

                        if (renameFiles) {
                            YassUtils.renameFiles(t, coverID, backgroundID, videoID, videodirID);
                        } else {
                            if (messages != null) {
                                auto.autoCorrectAllSafe(t, messages);
                            } else {
                                auto.autoCorrectAllSafe(t);
                            }
                        }
						filename = t.getDir() + File.separator + t.getFilename();
                        t.storeFile(filename);
                        s.setDirectory(t.getDir());
                        s.setFilename(t.getFilename());

                        loadSongDetails(s, t2);
                        sm.fireTableRowsUpdated(i, i);
                        repaintIfVisible(i);

                        actions.setProgress(++i);
                    }
                } catch (Throwable th) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    th.printStackTrace(pw);
                    JOptionPane.showMessageDialog(actions.getTab(), "<html>" + MessageFormat.format(I18.get("lib_msg_correct_error_msg"), s.getDirectory(), th.getMessage(), sw.toString()), I18.get("lib_msg_correct_error_title"), JOptionPane.ERROR_MESSAGE);
                    notInterrupted = false;
                }
            }

            finished = true;
            storeCache();

            actions.setProgress(0);
            actions.setProgress(MessageFormat.format(I18.get("lib_msg"), data.size()), dir);

            SwingUtilities.invokeLater(() -> {
                if (autoAction != null) autoAction.setEnabled(true);
            });
        }
    }

    /**
     * Description of the Class
     *
     */
    class DefaultSongHeaderRenderer extends JLabel implements TableCellRenderer {
        private static final long serialVersionUID = -1846744957956407987L;
        Color bg, fg;
        Border b;
        Font f;

        boolean isSorted = false;


        /**
         * Constructor for the DefaultSongHeaderRenderer object
         */
        public DefaultSongHeaderRenderer() {
            bg = UIManager.getColor("TableHeader.background");
            fg = UIManager.getColor("TableHeader.foreground");
            b = UIManager.getBorder("TableHeader.cellBorder");
            f = UIManager.getFont("TableHeader.font");
        }

        public boolean isSorted() {
            return isSorted;
        }

        public void setSorted(boolean onoff) {
            isSorted = onoff;
        }

        /**
         * Gets the tableCellRendererComponent attribute of the
         * DefaultSongHeaderRenderer object
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
            setBackground(bg);
            setForeground(fg);
            setHorizontalAlignment(SwingConstants.CENTER);
            setHorizontalTextPosition(SwingConstants.LEFT);
            setBorder(b);
            setFont(f);

            if (options == TILE && vColIndex == TITLE_COLUMN) {
                String str = "Title";
				/*
				 *  String dir = "";
				 *  /if ((YassSong.ordering & YassSong.SORT_INVERSE) != 0) dir = "(Z-A)";
				 *  /if ((YassSong.ordering & YassSong.SORT_BY_TITLE) != 0) str=str+" ("+dir+")";;
				 *  if ((YassSong.ordering & YassSong.SORT_BY_ARTIST) != 0) {
				 *  str = str + " (by Artist)";
				 *  }
				 *  if ((YassSong.ordering & YassSong.SORT_BY_COMPLETE) != 0) {
				 *  str = str + " (by Video)";
				 *  }
				 *  if ((YassSong.ordering & YassSong.SORT_BY_VERSION) != 0) {
				 *  str = str + " (by Version)";
				 *  }
				 *  if ((YassSong.ordering & YassSong.SORT_BY_GENRE) != 0) {
				 *  str = str + " (by Genre)";
				 *  }
				 *  if ((YassSong.ordering & YassSong.SORT_BY_EDITION) != 0) {
				 *  str = str + " (by Edition)";
				 *  }
				 *  if ((YassSong.ordering & YassSong.SORT_BY_LANGUAGE) != 0) {
				 *  str = str + " (by Language)";
				 *  }
				 *  if ((YassSong.ordering & YassSong.SORT_BY_YEAR) != 0) {
				 *  str = str + " (by Year)";
				 *  }
				 *  if ((YassSong.ordering & YassSong.SORT_BY_FOLDER) != 0) {
				 *  str = str + " (by Folder)";
				 *  }
				 */
                setText(str);
            } else {
                String str = value.toString();
                if (isSorted()) {
                    //str = str + "v";
                }
                setText(str);
            }
            return this;
        }

    }

    /**
     * Description of the Class
     *
     */
    class SongHeaderRenderer extends JLabel implements TableCellRenderer {
        boolean isSelected = false;
        boolean isSelectable = false;
        boolean isSorted = false;
        Color gray = new Color(180, 180, 180);
        boolean err_minorpage, err_major, err_file, err_text, err_tags;

        private final Font font = new Font("SansSerif", Font.PLAIN, 12);
        private Dimension dim = new Dimension(6, 180);
        private final Dimension minDim = new Dimension(6, 15);

        /**
         * Gets the selected attribute of the SongHeaderRenderer object
         *
         * @return The selected value
         */
        public boolean isSelected() {
            return isSelected;
        }

        /**
         * Sets the selected attribute of the SongHeaderRenderer object
         *
         * @param onoff The new selected value
         */
        public void setSelected(boolean onoff) {
            isSelected = onoff;
        }

        public boolean isSelectable() {
            return isSelectable;
        }

        public void setSelectable(boolean onoff) {
            isSelectable = onoff;
        }

        public boolean isSorted() {
            return isSorted;
        }


		/*
		 *  public void updateUI() {
		 *  super.updateUI();
		 *  cell = UIManager.getBorder("TableHeader.cellBorder");
		 *  }
		 */

        public void setSorted(boolean onoff) {
            isSorted = onoff;
        }

        /**
         * Gets the tableCellRendererComponent attribute of the SongHeaderRenderer
         * object
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
                value = "";
            }
            String title = "";
            String description = "";

            boolean isMessage = vColIndex < FIRST_STATS_COLUMN;
            boolean isStats = vColIndex >= FIRST_STATS_COLUMN;

            setSelectable(false);
            if (isMessage) {
                title = I18.get(value.toString());
                description = I18.get(value + "_msg");

                int msgi = vColIndex - FIRST_MSG_COLUMN;
                String msg = YassRow.ALL_MESSAGES[msgi];
                if (auto != null && auto.isAutoCorrectionSafe(msg)) {
                    setForeground(getForeground());
                    setSelectable(true);
                } else {
                    setForeground(gray);
                }
                err_minorpage = YassRow.isMessage(msgi, YassRow.getMinorPageBreakMessages());
                err_major = YassRow.isMessage(msgi, YassRow.getMajorMessages());
                err_file = YassRow.isMessage(msgi, YassRow.getFileMessages());
                err_text = YassRow.isMessage(msgi, YassRow.getTextMessages());
                err_tags = YassRow.isMessage(msgi, YassRow.getTagsMessages());
            } else {
                setForeground(getForeground());
            }

            if (isStats) {
                int idx = YassStats.indexOf(value.toString());
                title = YassStats.getLabelAt(idx);
                description = YassStats.getDescriptionAt(idx);
            }

            setSorted(vColIndex == sortbyCol);

            setText(title);
            setToolTipText("<html><b>" + title + "</b><br>" + description);
            return this;
        }

        /**
         * Description of the Method
         *
         * @param g Description of the Parameter
         */
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            int hw = getSize().width;
            int hh = getSize().height;
            if (hw < 3) {
                return;
            }

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            g2d.setStroke(new BasicStroke(1));
            if (isSelectable()) {
                g2d.setColor(Color.white);
                g2d.fillRect(2, 2, 10, 10);
                g2d.setColor(Color.black);
                g2d.drawRect(2, 2, 10, 10);
                g2d.setColor(UIManager.getColor("Table.selectionBackground"));
                if (isSelected()) {
                    g2d.fillRect(3, 3, 9, 9);
                    g2d.setColor(Color.black);
                    g2d.drawLine(3, 3, 11, 11);
                    g2d.drawLine(3, 11, 11, 3);
                }
            }

            hw = Math.min(16, hw - 2);
            if (err_major && err_major_icon != null) {
                g2d.drawImage(err_major_icon, 0, 16, hw, 16, null);
            }
            if (err_file && err_file_icon != null) {
                g2d.drawImage(err_file_icon, 0, 16, hw, 16, null);
            }
            if (err_minorpage && err_minorpage_icon != null) {
                g2d.drawImage(err_minorpage_icon, 0, 16, hw, 16, null);
            }
            if (err_text && err_text_icon != null) {
                g2d.drawImage(err_text_icon, 0, 16, hw, 16, null);
            }
            if (err_tags && err_tags_icon != null) {
                g2d.drawImage(err_tags_icon, 0, 16, hw, 16, null);
            }

            if (isSorted()) {
                g2d.setColor(UIManager.getColor("Table.selectionForeground"));
                int mid = hw / 2;
                int j = hh - 8;
                for (int i = 4; i >= 0; i--) {
                    g.drawLine(mid - i, j, mid + i, j);
                    j++;
                }
                g2d.setColor(getForeground());
            }

            g2d.setFont(font);
            g2d.setColor(getForeground());
            g2d.translate(12, 165.0);
            g2d.rotate(270 * Math.PI / 180.0);
            g2d.drawString(getText(), 0, 0);
        }

        /**
         * Gets the preferredSize attribute of the SongHeaderRenderer object
         *
         * @return The preferredSize value
         */
        public Dimension getPreferredSize() {
            return dim;
        }


        /**
         * Description of the Method
         */
        public void minimize() {
            dim = new Dimension(6, 15);
        }


        /**
         * Description of the Method
         */
        public void maximize() {
            dim = new Dimension(6, 180);
        }


        /**
         * Gets the minimumSize attribute of the SongHeaderRenderer object
         *
         * @return The minimumSize value
         */
        public Dimension getMinimumSize() {
            return minDim;
        }


        // override defaults for performance reasons

        /**
         * Description of the Method
         */
        public void validate() {
        }


        /**
         * Description of the Method
         */
        public void revalidate() {
        }


        /**
         * Description of the Method
         *
         * @param propertyName Description of the Parameter
         * @param oldValue     Description of the Parameter
         * @param newValue     Description of the Parameter
         */
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        }


        /**
         * Description of the Method
         *
         * @param propertyName Description of the Parameter
         * @param oldValue     Description of the Parameter
         * @param newValue     Description of the Parameter
         */
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        }
    }

    /**
     * Description of the Class
     *
     */
    class SongRenderer extends JLabel implements TableCellRenderer {
        private static final long serialVersionUID = -3155315980103953013L;
        Font bigFont = null;


        /**
         * Gets the tableCellRendererComponent attribute of the SongRenderer object
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
                value = "";
            }
            if (vColIndex == 0) {
                setHorizontalAlignment(RIGHT);
            }
            if (vColIndex == 1) {
                setHorizontalAlignment(CENTER);
                if (options == SYMBOL) {
                    value = "";
                }
            }
            if (vColIndex >= FIRST_MSG_COLUMN) {
                setHorizontalAlignment(RIGHT);
            }

            String t = value.toString();
            if (t == null) {
                t = "";
            }
            Font f = table.getFont();
            boolean hilight = false;
            if (!hilight) {
                hilight = (YassSong.ordering & YassSong.SORT_BY_COMPLETE) != 0 && vColIndex == COMPLETE_COLUMN;
            }
            if (!hilight) {
                hilight = (YassSong.ordering & YassSong.SORT_BY_DUETSINGER) != 0 && vColIndex == DUETSINGER_COLUMN;
            }
            if (!hilight) {
                hilight = (YassSong.ordering & YassSong.SORT_BY_GENRE) != 0 && vColIndex == GENRE_COLUMN;
            }
            if (!hilight) {
                hilight = (YassSong.ordering & YassSong.SORT_BY_EDITION) != 0 && vColIndex == EDITION_COLUMN;
            }
            if (!hilight) {
                hilight = (YassSong.ordering & YassSong.SORT_BY_LANGUAGE) != 0 && vColIndex == LANGUAGE_COLUMN;
            }
            if (!hilight) {
                hilight = (YassSong.ordering & YassSong.SORT_BY_YEAR) != 0 && vColIndex == YEAR_COLUMN;
            }
            if (!hilight) {
                hilight = (YassSong.ordering & YassSong.SORT_BY_FOLDER) != 0 && vColIndex == FOLDER_COLUMN;
            }
            if (hilight) {
                if (bigFont == null) {
                    bigFont = table.getFont().deriveFont(Font.BOLD);
                }
                if (rowIndex > 0) {
                    String tt = (String) table.getValueAt(rowIndex - 1, vColIndex);
                    if (tt == null) {
                        tt = "";
                    }
                    if (!t.equals(tt)) {
                        f = bigFont;
                    }
                } else {
                    f = bigFont;
                }
            }
            setText(t);
            setFont(f);

            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }


        public void paintComponent(Graphics g2) {
            Graphics2D g2d = (Graphics2D) g2;

            if (!isOpaque()) {
                Dimension d = YassSongList.this.getSize();
                Color bg = getBackground();
                Color color1 = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 230);
                Color color2 = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 250);
                GradientPaint gp = new GradientPaint(0, 0, color1, d.width, d.height, color2);

                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            g2d.setColor(Color.black);

            super.paintComponent(g2);
        }


        /**
         * Description of the Method
         */
        public void validate() {
        }


        /**
         * Description of the Method
         */
        public void revalidate() {
        }


        /**
         * Description of the Method
         *
         * @param propertyName Description of the Parameter
         * @param oldValue     Description of the Parameter
         * @param newValue     Description of the Parameter
         */
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        }


        /**
         * Description of the Method
         *
         * @param propertyName Description of the Parameter
         * @param oldValue     Description of the Parameter
         * @param newValue     Description of the Parameter
         */
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        }
    }

    /**
     * Description of the Class
     *
     */
    class SongArtistRenderer extends JLabel implements TableCellRenderer {
        private static final long serialVersionUID = 3421629804624478692L;
        Font bigFont = null;


        /**
         * Gets the tableCellRendererComponent attribute of the SongArtistRenderer
         * object
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
            YassSong s = sm.getRowAt(rowIndex);
            if (s == null) {
                return this;
            }

            String dir = s.getDirectory();
            if (dir == null) {
                return this;
            }
            String a = s.getSortedArtist();
            if (a == null) {
                a = s.getArtist();
            }
            if (a == null || a.length() < 1) {
                a = " ";
            }
            Font f = table.getFont();
            if ((YassSong.ordering & YassSong.SORT_BY_ARTIST) != 0) {
                if (bigFont == null) {
                    bigFont = table.getFont().deriveFont(Font.BOLD);
                }
                if (rowIndex > 0) {
                    YassSong ss = sm.getRowAt(rowIndex - 1);
                    if (ss != null) {
                        String aa = ss.getSortedArtist();
                        if (aa == null) {
                            aa = ss.getArtist();
                        }
                        if (aa == null || aa.length() < 1) {
                            aa = " ";
                        }
                        if (!a.equals(aa)) {
                            f = bigFont;
                        }
                    }
                } else {
                    f = bigFont;
                }
            }
            setText(a);
            setFont(f);

            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }


        public void paintComponent(Graphics g2) {
            Graphics2D g2d = (Graphics2D) g2;

            Shape shape = g2d.getClip();

            if (!isOpaque()) {
                Dimension d = YassSongList.this.getSize();
                Color bg = getBackground();
                Color color1 = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 230);
                Color color2 = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 250);
                GradientPaint gp = new GradientPaint(0, 0, color1, d.width, d.height, color2);

                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            g2d.setColor(Color.black);

            super.paintComponent(g2);

            g2d.setClip(shape);
        }


        /**
         * Description of the Method
         */
        public void revalidate() {
        }


        /**
         * Description of the Method
         *
         * @param propertyName Description of the Parameter
         * @param oldValue     Description of the Parameter
         * @param newValue     Description of the Parameter
         */
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        }


        /**
         * Description of the Method
         *
         * @param propertyName Description of the Parameter
         * @param oldValue     Description of the Parameter
         * @param newValue     Description of the Parameter
         */
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        }
    }

    /**
     * Description of the Class
     *
     */
    class SongTitleRenderer extends JLabel implements TableCellRenderer {
        private static final long serialVersionUID = -1054349571120736221L;
        Font bigFont = null;
        String t, a, g, l, e, y, f, ds, multi, al, len;
        boolean bold, underlined, opened, locked, video, nobackground, perfect;
        int langIndex = -1;
        boolean err_minorpage, err_major, err_file, err_text, err_tags;

        private final Font font = new Font("SansSerif", Font.PLAIN, 12);

        private final Hashtable<String, String> i18Labels = new Hashtable<>();


        /**
         * Gets the tableCellRendererComponent attribute of the SongTitleRenderer
         * object
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

            YassSong s = sm.getRowAt(rowIndex);
            if (s == null) {
                return this;
            }

            String dir = s.getDirectory();
            if (dir == null) {
                return this;
            }
            t = s.getTitle();
            String key;
            if (t == null || t.length() < 1) {
                key = "songlist_no_3";
                t = i18Labels.get(key);
                if (t == null) {
                    i18Labels.put(key, t = I18.get(key));
                }
            }
            a = s.getSortedArtist();
            if (a == null) {
                a = s.getArtist();
            }
            if (a == null || a.length() < 1) {
                key = "songlist_no_2";
                a = i18Labels.get(key);
                if (a == null) {
                    i18Labels.put(key, a = I18.get(key));
                }
            }
            l = s.getLanguage();
            if (l == null || l.length() < 1) {
                key = "songlist_no_7";
                l = i18Labels.get(key);
                if (l == null) {
                    i18Labels.put(key, l = I18.get(key));
                }
            }
            e = s.getEdition();
            if (e == null || e.length() < 1) {
                key = "songlist_no_6";
                e = i18Labels.get(key);
                if (e == null) {
                    i18Labels.put(key, e = I18.get(key));
                }
            }
            g = s.getGenre();
            if (g == null || g.length() < 1) {
                key = "songlist_no_5";
                g = i18Labels.get(key);
                if (g == null) {
                    i18Labels.put(key, g = I18.get(key));
                }
            }
            y = s.getYear();
            if (y == null || y.length() < 1) {
                key = "songlist_no_8";
                y = i18Labels.get(key);
                if (y == null) {
                    i18Labels.put(key, y = I18.get(key));
                }
            }
            f = s.getFolder();
            if (f == null || f.length() < 1) {
                key = "songlist_no_9";
                f = i18Labels.get(key);
                if (f == null) {
                    i18Labels.put(key, f = I18.get(key));
                }
            }
            ds = s.getDuetSingerNames();
            if (ds == null || ds.length() < 1) {
                ds = "";
            }
            al = s.getAlbum();
            if (al == null || al.length() < 1) {
                key = "songlist_no_10";
                al = i18Labels.get(key);
                if (al == null) {
                    i18Labels.put(key, al = I18.get(key));
                }
            }
            len = s.getLength();
            if (len == null || len.length() < 1) {
                key = "songlist_no_11";
                len = i18Labels.get(key);
                if (len == null) {
                    i18Labels.put(key, len = I18.get(key));
                }
            }

            multi = s.getMultiplayer();
            if (multi == null || multi.length() < 1 || multi.equals("0")) {
                multi = "";
            }

            Font font = table.getFont();
            boolean useBigFont = false;
            if ((YassSong.ordering & YassSong.SORT_BY_TITLE) != 0) {
                if (bigFont == null) {
                    bigFont = table.getFont().deriveFont(Font.BOLD);
                }
                if (rowIndex > 0) {
                    YassSong ss = sm.getRowAt(rowIndex - 1);
                    if (ss != null) {
                        String tt = ss.getTitle();
                        if (tt == null || tt.length() < 1) {
                            tt = " ";
                        }
                        if (t.charAt(0) != tt.charAt(0)) {
                            useBigFont = true;
                        }
                    }
                } else {
                    useBigFont = true;
                }
            }
            setFont(font);

            int options = getOptions();
            if (options == DETAILS || options == ERRORS) {
                if (s.isOpened()) {
                    t = "<u>" + t + "</u>";
                }
                if (useBigFont) {
                    t = "<b>" + t + "</b>";
                }
                setText("<html>" + t);
            } else if (options == SYMBOL) {
                if (s.isOpened()) {
                    t = "<u>" + t + "</u>";
                }
                if (useBigFont) {
                    t = "<b>" + t + "</b>";
                }

                setText("<html>" + t);
            } else if (options == TILE) {
                bold = false;
                // useBigFont;
                underlined = opened = s.isOpened();
                locked = s.isLocked();
                video = s.getComplete().equals("V");
                nobackground = s.getComplete().equals("b");
                perfect = e.contains("[SC]");

                langIndex = -1;
                for (int i = 0; i < langArray.length; i++) {
                    if (l.equals(langArray[i])) {
                        langIndex = i;
                        break;
                    }
                }

                setText("");
            }

            err_minorpage = s.hasMessage(YassRow.getMinorPageBreakMessages());
            err_major = s.hasMessage(YassRow.getMajorMessages());
            err_file = s.hasMessage(YassRow.getFileMessages());
            err_text = s.hasMessage(YassRow.getTextMessages());
            err_tags = s.hasMessage(YassRow.getTagsMessages());

            if (imgShown) {
                ImageIcon icon = getSongIcon(s, rowIndex);
                if (icon == null) {
                    icon = noCover;
                }
                setIcon(icon);
            } else {
                setIcon(null);
            }
            return this;
        }


        /**
         * Description of the Method
         *
         * @param g2 Description of the Parameter
         */
        public void paintComponent(Graphics g2) {
            Graphics2D g2d = (Graphics2D) g2.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape shape = g2d.getClip();

            if (!isOpaque()) {
                Dimension d = YassSongList.this.getSize();
                Color bg = getBackground();
                Color color1 = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 230);
                Color color2 = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 250);
                GradientPaint gp = new GradientPaint(0, 0, color1, d.width, d.height, color2);

                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            g2d.setColor(Color.black);

            super.paintComponent(g2);

            if (options != TILE) {
                return;
            }


//			   t = "TitleTitleTitleTitleTitleTitleTitleTitleTitleTitleTitleTitle";
//			   a = "ArtistArtistArtistArtistArtistArtistArtistArtistArtistArtist";
//			   v = "VERSIONVERSIONVERSIONVERSIONVERSIONVERSIONVERSIONVERSION";
//			   g = "genregenregenregenregenregenregenregenregenregenregenre";
//			   e = "editioneditioneditioneditioneditioneditioneditionedition";
//			   l = "languagelanguagelanguagelanguagelanguagelanguagelanguage";
//			   f = "folderfolderfolderfolderfolderfolderfolderfolderfolder";
//			   locked = opened = video = perfect = err_major = err_file = err_text = err_minorpage = err_tags = true;

            FontMetrics fm = g2.getFontMetrics();
            int sh = fm.getHeight() - 1;
            int width = getSize().width;
            int height = getSize().height;

            int width_icon = ICON_HEIGHT;
            int x_text = width_icon + 3;
            int max_width = width - x_text - 20;
            boolean ch = false;
            while (t.length() > 0 && fm.stringWidth(t) > max_width) {
                t = t.substring(0, t.length() - 1);
                ch = true;
            }
            if (ch) {
                t += "...";
            }
            ch = false;

            while (a.length() > 0 && fm.stringWidth(a) > max_width) {
                a = a.substring(0, a.length() - 1);
                ch = true;
            }
            if (ch) {
                a += "...";
            }
            ch = false;

            while (ds.length() > 0 && fm.stringWidth(ds) > max_width) {
                ds = ds.substring(0, ds.length() - 1);
                ch = true;
            }
            if (ch) {
                ds += "...";
            }
            ch = false;

            String extra = "";
            if (extraCol == YassSongList.LANGUAGE_COLUMN) {
                extra = l;
            } else if (extraCol == YassSongList.GENRE_COLUMN) {
                extra = g;
            } else if (extraCol == YassSongList.EDITION_COLUMN) {
                extra = e;
            } else if (extraCol == YassSongList.FOLDER_COLUMN) {
                extra = f;
            } else if (extraCol == YassSongList.YEAR_COLUMN) {
                extra = y;
            } else if (extraCol == YassSongList.ALBUM_COLUMN) {
                extra = al;
            } else if (extraCol == YassSongList.DUETSINGER_COLUMN) {
                extra = ds;
            }
            else if (extraCol == YassSongList.LENGTH_COLUMN) {
                int sec = 0;
                try {
                    sec = Integer.parseInt(len);
                } catch (Exception ignored) {
                }

                int min = sec / 60;
                sec = sec - min * 60;
                extra = (sec < 10) ? min + ":0" + sec : min + ":" + sec;
            }

            while (extra.length() > 0 && fm.stringWidth(extra) > max_width - 40) {
                extra = extra.substring(0, extra.length() - 1);
                ch = true;
            }
            if (ch) {
                extra += "...";
            }

            g2d.setColor(Color.black);
            g2.drawString(t, x_text, sh);

            g2.setColor(Color.gray);
            g2.setFont(font);
            fm = g2.getFontMetrics();
            sh = fm.getHeight() - 1;

            g2.drawString(a, x_text, 2 * sh);

            boolean extraCol_has_icon = false;
            if (extraCol == YassSongList.LANGUAGE_COLUMN && langIndex >= 0 && langIcons[langIndex] != null) {
                g2.drawImage(langIcons[langIndex], x_text, height - 16, null);
                extraCol_has_icon = true;
            }

            if (extra.length() > 0) {
                int extraCol_x = x_text;
                if (extraCol_has_icon) {
                    extraCol_x += 20;
                }
                g2.drawString(extra, extraCol_x, height - 6);
            }
            if (video && videoIcon != null) {
                g2.drawImage(videoIcon, ICON_WIDTH - 16, height - 16, null);
            }
            if (nobackground && nobackgroundIcon != null) {
                g2.drawImage(nobackgroundIcon, 34, height - 16, null);
            }

            if (err_major && err_major_icon != null) {
                g2.drawImage(err_major_icon, x_text, height - 16, null);
            }
            if (err_tags && err_tags_icon != null) {
                g2.drawImage(err_tags_icon, x_text + 16, height - 16, null);
            }
            if (err_file && err_file_icon != null) {
                g2.drawImage(err_file_icon, x_text + 2 * 16, height - 16, null);
            }
            if (err_minorpage && err_minorpage_icon != null) {
                g2.drawImage(err_minorpage_icon, x_text + 3 * 16, height - 16, null);
            }
            if (err_text && err_text_icon != null) {
                g2.drawImage(err_text_icon, x_text + 4 * 16, height - 16, null);
            }
            if (locked && lockedIcon != null && renderLocked) {
                g2.drawImage(lockedIcon, -2, height - 16, null);
            }

            if (multi.length() > 0) {
                g2d.setStroke(new BasicStroke(0.5f));
                g2d.drawOval(width - 26, height - 16,10,10);
                g2d.drawOval(width - 32, height - 16,10,10);
                g2d.drawString(multi, width - 30 + 18, height - 6);
            }
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            g2d.setClip(shape);
            g2d.dispose();
        }

        //public void validate() { if (getOptions()==TILE) super.validate(); }
        //public void revalidate() {  if (getOptions()==TILE) super.revalidate(); }
        //protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {  if (getOptions()==TILE) super.firePropertyChange(propertyName, oldValue, newValue); }
        //public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {  if (getOptions()==TILE) super.firePropertyChange(propertyName, oldValue, newValue); }
    }

    /**
     * Description of the Class
     *
     */
    class SongMessageRenderer extends SongRenderer {
        private final Font stdFont = new Font("SansSerif", Font.PLAIN, 11);
        private final Font smallFont = new Font("SansSerif", Font.PLAIN, 9);


        /**
         * Description of the Method
         *
         * @param g Description of the Parameter
         */
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            int height = this.getHeight();
            int width = this.getWidth();
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, width - 1, height - 1);
            g2d.setColor(Color.black);
            //if (getSize().width < 3) return;
            //g2d.setColor(Color.black);
            //g2d.setFont(font);
            //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean big = getText().length() > 1;
            g2d.setFont(big ? smallFont : stdFont);

            FontMetrics fm = g2d.getFontMetrics();
            int sw = fm.stringWidth(getText());

            if (big) {
                g2d.drawString(getText(), width / 2 - sw / 2, 10);
            } else {
                g2d.drawString(getText(), width / 2 - sw / 2, 11);
            }
        }
    }

    /**
     * Description of the Class
     *
     */
    class FilterThread extends Thread {
        boolean isInterrupted = false;
        boolean isFinished = false;
        private String str;


        /**
         * Constructor for the FilterThread object
         *
         * @param s Description of the Parameter
         */
        public FilterThread(String s) {
            str = s;
        }


        /**
         * Main processing method for the FilterThread object
         */
        public void run() {
            boolean all = false;
            boolean not = false;
            if (str == null) {
                all = true;
            } else {
                str = str.toLowerCase();
                str = str.trim();
                if (str.startsWith("!")) {
                    not = true;
                    str = str.substring(1);
                    str = str.trim();
                }
                if (str.startsWith("-")) {
                    not = true;
                    str = str.substring(1);
                    str = str.trim();
                }
            }

            if (allData == null) {
                return;
            }
            Vector<YassSong> fData = new Vector<>(allData.size());

            if (prefilter != null) {
                YassFilter.isInterrupted = false;
                boolean ok = prefilter.start(allData);
                if (!ok) {
                    return;
                }
            }

            if (filterAll && lyricsCache == null) {
                loadLyricsCache();
            }

            actions.setProgress(I18.get("lib_msg_filter"), allData.size());
            long startTime = System.currentTimeMillis();

            YassTable ft = new YassTable();
            boolean notInterrupted = true;
            int k = 0;
            for (Enumeration<YassSong> e = allData.elements(); e.hasMoreElements() && notInterrupted; ) {
                YassSong s = e.nextElement();
                boolean add = false;

                if (isInterrupted) {
                    isFinished = true;
                    return;
                }

                ++k;
                long currentTime = System.currentTimeMillis();
                if (currentTime > startTime + 300) {
                    actions.setProgress(k);
                    actions.setProgress(MessageFormat.format(I18.get("lib_msg_filter_n"), k), s.getDirectory());
                    Thread.yield();
                }

                if (prefilter != null && !prefilter.accept(s)) {
                    continue;
                }

                String t = s.getDirectory() + File.separator + s.getFilename();
                add = all || /* s.isLocked() || */ YassFilter.containsIgnoreCase(t, str) ||
                        YassFilter.containsIgnoreCase(s.getTitle(), str) ||
                        YassFilter.containsIgnoreCase(s.getArtist(), str) ||
                        YassFilter.containsIgnoreCase(s.getLanguage(), str) ||
                        YassFilter.containsIgnoreCase(s.getEdition(), str) ||
                        YassFilter.containsIgnoreCase(s.getGenre(), str) ||
                        YassFilter.containsIgnoreCase(s.getDuetSingerNames(), str);

                if (filterAll) {
                    String txt = s.getLyrics();
                    if (txt == null) {
                        if (lyricsCache != null) {
                            String key = s.getDirectory() + File.separator + s.getFilename();
                            txt = lyricsCache.get(key);
                        }
                        if (txt == null) {
                            loadSongDetails(s, ft);
                        }
                        txt = s.getLyrics();
                    }
                    if (txt != null) {
                        add = add || YassFilter.containsIgnoreCase(txt, str);
                    }
                }
                if ((add && !not) || (!add && not)) {
                    fData.addElement(s);
                }
            }
            if (prefilter != null) {
                prefilter.stop();
            }

            if (isInterrupted) {
                isFinished = true;
                return;
            }

            sm.setData(fData);
            //sm.fireTableDataChanged();

            Collections.sort(sm.getData());
            int i = 1;
            Enumeration<YassSong> en = sm.getData().elements();
            while (en.hasMoreElements()) {
                en.nextElement().setState((i++) + "");

                if (isInterrupted) {
                    isFinished = true;
                    return;
                }
            }
            sm.fireTableDataChanged();

            if (filterAll && lyricsCache == null) {
                cacheLyrics();
            }

            actions.setProgress(0);

            String dir = prop.getProperty("song-directory");

            if (all) {
                actions.setProgress(MessageFormat.format(I18.get("lib_msg"), sm.getData().size()), dir);
            } else {
                actions.setProgress(MessageFormat.format(I18.get("lib_msg_matches"), sm.getData().size()), dir);
            }

            SwingUtilities.invokeLater(() -> {
                changeSelection(0, 0, false, false);
                repaint();
            });
            isFinished = true;
        }
    }

    enum HeaderEnum {
        TITLE,
        ARTIST,
        MEDLEY,
        MP3,
        AUTHOR,
        LANGUAGE,
        GENRE,
        YEAR,
        ENCODING;

        static boolean isValidHeader(String header) {
            for (HeaderEnum headerEnum : HeaderEnum.values()) {
                if (header.startsWith(headerEnum.toString())) {
                    return true;
                }
            }
            return false;
        }
    }
}

