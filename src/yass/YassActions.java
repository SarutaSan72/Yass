/*
 * Yass - Karaoke Editor
 * Copyright (C) 2009 Saruta
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package yass;

import com.nexes.wizard.Wizard;
import yass.renderer.YassSession;
import yass.wizard.CreateSongWizard;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public class YassActions implements DropTargetListener {

    private final YassSheet sheet;
    public final static String VERSION = "2023.11";
    public final static String DATE = "11/2023";

    static int VIEW_LIBRARY = 1;
    static int VIEW_EDIT = 2;
    private int currentView = 0;
    private final Vector<YassTable> openTables = new Vector<>();
    private JPanel main = null;
    private JComponent libComponent = null, songComponent = null, songInfoComponent = null, playlistComponent = null, editComponent = null, trackComponent = null;
    private JFrame menuHolder = null;
    private Rectangle songBounds = null;
    private final JProgressBar progressBar;
    private JComponent tab = null;
    private JMenu encMenu = null;
    private Vector<String> plBoxTips = null;
    private PlayListBoxListener plBoxListener = null;
    private final Font groupsFont = new Font("SansSerif", Font.BOLD, 14);
    private JDialog errDialog = null;
    private JDialog gapDialog = null;
    private JTextField bpmField = null;
    private TimeSpinner gapSpinner = null, startSpinner = null, endSpinner = null, vgapSpinner = null;
    private JDialog seDialog = null, vgapDialog = null, srcDialog = null;
    private JTextPane helpPane = null;
    private int vmark = 0;
    private int recordLength = -1;
    private int playTimebase = 1;
    private long playAllStart = -1;
    private long[][] playAllClicks = null;
    private int beforeNextMs = 300;
    private boolean preventTrim = false;
    long lastTogglePressed = -1;

    private YassTable table = null;
    private YassLyrics lyrics;
    private final YassPlayer mp3;
    private YassPlayList playList = null;
    private YassSongInfo songInfo = null;
    private YassVideo video = null;
    private YassErrors errors = null;
    private YassProperties prop = null;
    private YassAutoCorrect auto = null;
    private YassSongList songList = null;
    private YassGroups groups = null;
    private JComboBox<String> groupsBox = null;
    private final Hashtable<String, ImageIcon> icons = new Hashtable<>();
    private JToggleButton mp3Button, midiButton, vmarkButton, detailToggle, songInfoToggle, bgToggle, playlistToggle, snapshotButton, videoButton, videoAudioButton, speedButton;
    private final JCheckBoxMenuItem playlistCBI = null;
    private JCheckBoxMenuItem audioCBI = null;

    private JCheckBoxMenuItem playAllVideoCBI = null;
    private JCheckBoxMenuItem alignCBI = null;
    private JCheckBoxMenuItem showCopyCBI = null;
    private JToggleButton filterAll = null;
    private JButton playToggle;
    private boolean isLibraryPlaying = false;
    private JRadioButtonMenuItem showNothingToggle, showBackgroundToggle, showVideoToggle = null;
    private boolean isUpdating = false;
    private JButton correctPageBreakButton, correctTransposedButton, correctSpacingButton;
    private final Color stdBackground = new JButton().getBackground();
    private Color errBackground = new Color(.95f, .5f, .5f);
    private Color minorerrBackground = new Color(.95f, .8f, .8f);
    private final Color[] colors = new Color[]{
            new Color(.3f, .3f, 0.3f, .7f), new Color(.3f, .6f, 0.3f, .7f), new Color(.3f, .3f, 0.6f, .7f), new Color(.3f, .6f, 0.6f, .7f), new Color(.6f, .6f, 0.3f, .7f), new Color(.6f, .3f, 0.6f, .7f), new Color(.4f, .4f, 0.6f, .7f), new Color(.4f, .6f, 0.4f, .7f)};

    private boolean autoTrim = false;
    private JComboBox<String> plBox = null, filter = null;
    private JTextField filterEditor = null;
    private JComponent editTools = null;
    private boolean soonStarting = false;
    private JMenuBar editMenu = null, libMenu = null;
    private JToolBar videoToolbar = null;
    private final RecordEventListener awt = new RecordEventListener();
    private final AWTEventListener awt2 = e -> {
        if (e instanceof MouseWheelEvent) {
            // att: freezes when not consumed
            stopPlaying();
            ((MouseWheelEvent) e).consume();
        }
        if (e instanceof KeyEvent k) {
            if (k.getID() == KeyEvent.KEY_RELEASED) {
                return;
            }

            if (k.getID() == KeyEvent.KEY_TYPED) {
                char ch = k.getKeyChar();
                if (ch == 'p' || ch == 'P' || ch == ' ') {
                    return;
                }
            }
            if ((k.isControlDown() || k.isAltDown() || k.isShiftDown())
                    && k.getKeyCode() == 0) {
                return;
            }
            stopPlaying();

            int c = k.getKeyCode();
            if (c == KeyEvent.VK_P || c == KeyEvent.VK_ESCAPE
                    || c == KeyEvent.VK_SPACE) {
                k.consume();
            }
        }
    };

    private final PropertyChangeListener propertyListener = e -> {
        String p = e.getPropertyName();
        if (p.equals("edit")) {
            String s = (String) e.getNewValue();
            if (s.equals("start")) {
                editLyrics();
            }
            if (s.equals("stop")) {
                lyrics.finishEditing();
            }
        }
    };

    private final Action showAbout = new AbstractAction(I18.get("lib_about")) {
        public void actionPerformed(ActionEvent e) {
            URL url = getClass().getResource("/yass/resources/img/about.jpg");
            ImageIcon icon = url == null ? null : new ImageIcon(url);
            try {
                JEditorPane label = new JEditorPane("text/html", I18.getCopyright(VERSION, DATE));
                label.setEditable(false);
                label.setBackground(new JLabel().getBackground());
                label.addHyperlinkListener(e1 -> {
                    if (e1.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        openURL(e1.getURL().toExternalForm());
                    }
                });
                JOptionPane.showMessageDialog(null, label, I18.get("lib_about_title"), JOptionPane.PLAIN_MESSAGE, icon);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    private final Action spellLyrics = new AbstractAction(I18.get("edit_spellcheck")) {
        public void actionPerformed(ActionEvent e) {
            lyrics.spellLyrics();
        }
    };
    private final Action selectAllSongs = new AbstractAction(I18.get("lib_select_all")) {
        public void actionPerformed(ActionEvent e) {
            if (currentView == VIEW_LIBRARY)
                songList.selectAll();
        }
    };
    private final Action setTitle = new AbstractAction(I18.get("lib_title")) {
        public void actionPerformed(ActionEvent e) {
            if (currentView == VIEW_LIBRARY)
                songList.setTitle();
        }
    };
    private final Action setArtist = new AbstractAction(I18.get("lib_artist")) {
        public void actionPerformed(ActionEvent e) {
            if (currentView == VIEW_LIBRARY)
                songList.setArtist();
        }
    };
    private final Action setYear = new AbstractAction(I18.get("lib_year")) {
        public void actionPerformed(ActionEvent e) {
            if (currentView == VIEW_LIBRARY)
                songList.setYear();
        }
    };
    private final Action setAlbum = new AbstractAction(I18.get("lib_album")) {
        public void actionPerformed(ActionEvent e) {
            if (currentView == VIEW_LIBRARY)
                songList.setAlbum();
        }
    };
    private final Action setLength = new AbstractAction(I18.get("lib_length")) {
        public void actionPerformed(ActionEvent e) {
            if (currentView == VIEW_LIBRARY)
                songList.setLength();
        }
    };
    private final Action setID = new AbstractAction(I18.get("lib_id")) {
        public void actionPerformed(ActionEvent e) {
            if (currentView == VIEW_LIBRARY)
                songList.setID();
        }
    };
    private final Action setEncodingUTF8 = new AbstractAction(I18.get("lib_encoding_utf8")) {
        public void actionPerformed(ActionEvent e) {
            if (currentView == VIEW_LIBRARY)
                songList.setEncoding("UTF-8"); // saruta, Jan 2019: utf8-->UTF-8
        }
    };
    private final Action setEncodingANSI = new AbstractAction(I18.get("lib_encoding_ansi")) {
        public void actionPerformed(ActionEvent e) {
            if (currentView == VIEW_LIBRARY)
                songList.setEncoding(null);
        }
    };
    private final Action editRecent = new AbstractAction(I18.get("lib_edit_recent")) {
        public void actionPerformed(ActionEvent e) {
            if (currentView == VIEW_EDIT) {
                if (cancelOpen()) {
                    return;
                }
            }
            String recent = prop.getProperty("recent-files");
            if (recent != null)
                openFiles(recent, false);
        }
    };
    private final Action clearFilter = new AbstractAction(I18.get("lib_find_clear")) {
        public void actionPerformed(ActionEvent e) {
            if (currentView == VIEW_LIBRARY) {
                clearFilter();
                filterLibrary();
            }
        }
    };
    private final Action reHyphenate = new AbstractAction(I18.get("edit_rehyphenate")) {
      public void actionPerformed(ActionEvent e) {
          table.rehyphenate();
      }
    };
    private final Action findLyrics = new AbstractAction(I18.get("edit_lyrics_find")) {
        public void actionPerformed(ActionEvent e) {
            lyrics.find();
        }
    };
    private final Action correctLibrary = new AbstractAction(I18.get("lib_correct")) {
        public void actionPerformed(ActionEvent e) {
            if (songList.getOptions() != YassSongList.DETAILS
                    || !songList.getShowErrors()) {
                JOptionPane.showMessageDialog(getFrame(tab), I18.get("lib_correct_error"), I18.get("lib_correct"), JOptionPane.PLAIN_MESSAGE);
                return;
            }
            songList.batchCorrect();
        }
    };
    private final Action correctPageBreakLibrary = new AbstractAction(I18.get("tool_correct_breaks")) {
        public void actionPerformed(ActionEvent e) {
            int[] rows = songList.getSelectedRows();
            if (rows == null) {
                return;
            }
            int n = rows.length;
            if (n < 1) {
                return;
            }
            int ok = JOptionPane.showConfirmDialog(
                    tab, "<html>"
                            + MessageFormat.format(
                            I18.get("tool_correct_breaks_msg"), n), I18.get("tool_correct_breaks_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) {
                return;
            }

            Vector<String> msg = new Vector<>();
            // msg.addElement(YassRow.UNCOMMON_PAGE_BREAK);
            msg.addElement(YassRow.EARLY_PAGE_BREAK);
            msg.addElement(YassRow.LATE_PAGE_BREAK);
            msg.addElement(YassRow.PAGE_OVERLAP);
            songList.batchProcess(false, msg);
        }
    };
    private final Action correctTextLibrary = new AbstractAction(I18.get("tool_correct_text")) {
        public void actionPerformed(ActionEvent e) {
            int[] rows = songList.getSelectedRows();
            if (rows == null) {
                return;
            }
            int n = rows.length;
            if (n < 1) {
                return;
            }
            String spacingMsg = prop.isUncommonSpacingAfter() ? I18.get("options_errors_uncommon_spacing_after") :
                    I18.get("options_errors_uncommon_spacing_before");
            int ok = JOptionPane.showConfirmDialog(
                    tab, "<html>"
                            + MessageFormat.format(
                            I18.get("tool_correct_text_msg"), n, spacingMsg), I18.get("tool_correct_text_title"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) {
                return;
            }

            Vector<String> msg = new Vector<>();
            msg.addElement(YassRow.TOO_MUCH_SPACES);
            msg.addElement(YassRow.UNCOMMON_SPACING);
            songList.batchProcess(false, msg);
        }
    };
    private final Action renameFilesLibrary = new AbstractAction(I18.get("tool_correct_files")) {
        public void actionPerformed(ActionEvent e) {
            int[] rows = songList.getSelectedRows();
            if (rows == null) {
                return;
            }
            int n = rows.length;
            if (n < 1) {
                return;
            }

            String coverID = prop.getProperty("cover-id");
            String backgroundID = prop.getProperty("background-id");
            String videoID = prop.getProperty("video-id");
            String videoDirID = prop.getProperty("videodir-id");

            songInfo.preventLoad(true);

            int ok = JOptionPane
                    .showConfirmDialog(
                            tab, "<html>"
                                    + MessageFormat.format(I18.get("tool_correct_files_msg"), n, coverID, backgroundID, videoID, videoDirID), I18
                                    .get("tool_correct_files_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) {
                return;
            }

            songList.batchRename(false);

            songInfo.preventLoad(false);
            songInfo.resetSong();
        }
    };
    private final Action correctFilesLibrary = new AbstractAction(I18.get("tool_correct_filetags")) {
        public void actionPerformed(ActionEvent e) {
            int[] rows = songList.getSelectedRows();
            if (rows == null) {
                return;
            }
            int n = rows.length;
            if (n < 1) {
                return;
            }
            int ok = JOptionPane.showConfirmDialog(
                    tab, "<html>"
                            + MessageFormat.format(
                            I18.get("tool_correct_filetags_msg"), n), I18.get("tool_correct_filetags_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) {
                return;
            }

            Vector<String> msg = new Vector<>();
            msg.addElement(YassRow.FILE_FOUND);
            // msg.addElement(YassRow.NO_COVER_LABEL);
            // msg.addElement(YassRow.NO_BACKGROUND_LABEL);
            // msg.addElement(YassRow.NO_VIDEO_LABEL);
            // msg.addElement(YassRow.DIRECTORY_WITHOUT_VIDEO);
            msg.addElement(YassRow.WRONG_VIDEOGAP);
            // msg.addElement(YassRow.WRONG_FILENAME);
            songList.batchProcess(false, msg);
        }
    };
    private final Action correctTagsLibrary = new AbstractAction(I18.get("tool_correct_tags")) {
        public void actionPerformed(ActionEvent e) {
            int[] rows = songList.getSelectedRows();
            if (rows == null) {
                return;
            }
            int n = rows.length;
            if (n < 1) {
                return;
            }
            int ok = JOptionPane.showConfirmDialog(
                    tab, "<html>"
                            + MessageFormat.format(
                            I18.get("tool_correct_tags_msg"), n), I18.get("tool_correct_tags_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) {
                return;
            }
            Vector<String> msg = new Vector<>();
            msg.addElement(YassRow.MISSING_TAG);
            msg.addElement(YassRow.UNSORTED_COMMENTS);
            msg.addElement(YassRow.WRONG_MEDLEY_START_BEAT);
            msg.addElement(YassRow.WRONG_MEDLEY_END_BEAT);
            msg.addElement(YassRow.BORING_APOSTROPHE);
            songList.batchProcess(false, msg);
        }
    };
    private final Action activateTrack1 = new AbstractAction(I18.get("mpop_tracks_1")) {
        public void actionPerformed(ActionEvent e) {
            activateTrack(0);
        }
    };
    private final Action activateTrack2 = new AbstractAction(I18.get("mpop_tracks_2")) {
        public void actionPerformed(ActionEvent e) {
            activateTrack(1);
        }
    };
    private final Action activateTrack3 = new AbstractAction(I18.get("mpop_tracks_3")) {
        public void actionPerformed(ActionEvent e) {
            activateTrack(2);
        }
    };
    private final Action activateTrack4 = new AbstractAction(I18.get("mpop_tracks_4")) {
        public void actionPerformed(ActionEvent e) {
            activateTrack(3);
        }
    };
    private final Action activateTrack5 = new AbstractAction(I18.get("mpop_tracks_5")) {
        public void actionPerformed(ActionEvent e) {
            activateTrack(4);
        }
    };
    private final Action activateTrack6 = new AbstractAction(I18.get("mpop_tracks_6")) {
        public void actionPerformed(ActionEvent e) {
            activateTrack(5);
        }
    };
    private final Action activateTrack7 = new AbstractAction(I18.get("mpop_tracks_7")) {
        public void actionPerformed(ActionEvent e) {
            activateTrack(6);
        }
    };
    private final Action activateTrack8 = new AbstractAction(I18.get("mpop_tracks_8")) {
        public void actionPerformed(ActionEvent e) {
            activateTrack(7);
        }
    };
    private final Action openSongFromLibrary = new AbstractAction(I18.get("lib_edit_songs")) {
        public void actionPerformed(ActionEvent e) {
            Vector<String> fn = songList.getSelectedFiles();
            if (fn != null)
                openFiles(fn, false);
        }
    };
    private final Action testMic = new AbstractAction(I18.get("lib_test_mic")) {
        public void actionPerformed(ActionEvent e) {
            YassCaptureAudio cap = new YassCaptureAudio();
            cap.createGUI();
            cap.startCapture(getProperties().getProperty("control-mic"));
        }
    };
    Action refreshLibrary = new AbstractAction(I18.get("lib_refresh")) {
        public void actionPerformed(ActionEvent e) {
            int savePlaylist = unsavedPlaylist();
            if (savePlaylist == JOptionPane.YES_OPTION) {
                playList.storePlayList();
            } else if (savePlaylist == JOptionPane.CANCEL_OPTION) {
                return;
            }

            refreshLibrary();
        }
    };
    private final Action printLibrary = new AbstractAction(I18.get("lib_print")) {
        public void actionPerformed(ActionEvent e) {
            songList.printSongs();
        }
    };
    Action setPreviewStart = new AbstractAction(I18.get("lib_previewstart")) {
        public void actionPerformed(ActionEvent e) {
            int[] inout = songInfo.getInOut();
            songList.setPreviewStart(inout[0]);
        }
    };
    Action setMedleyStartEnd = new AbstractAction(
            I18.get("lib_medleystartend")) {
        public void actionPerformed(ActionEvent e) {
            int[] inout = songInfo.getInOut();
            songList.setMedleyStartEnd(inout[0], inout[1]);
        }
    };
    private final Action togglePlaySong = new AbstractAction(I18.get("lib_play_song")) {
        public void actionPerformed(ActionEvent e) {
            togglePlaySong();
        }
    };
    Action playSong = new AbstractAction(I18.get("lib_play_song")) {
        public void actionPerformed(ActionEvent e) {
            if (isFilterEditing()) {
                return;
            }
            playSong();
        }
    };
    Action stopPlaySong = new AbstractAction(I18.get("lib_stop_song")) {
        public void actionPerformed(ActionEvent e) {
            if (isFilterEditing()) {
                return;
            }
            stopPlaySong();
        }
    };
    private final Action thumbnailLibrary = new AbstractAction(I18.get("lib_thumbs")) {
        public void actionPerformed(ActionEvent e) {
            songList.startThumbnailer();
        }
    };
    private final Action correctLength = new AbstractAction(I18.get("tool_correct_length")) {
        public void actionPerformed(ActionEvent e) {
            songList.updateLength();
        }
    };
    private final Action correctAlbum = new AbstractAction(I18.get("tool_correct_album")) {
        public void actionPerformed(ActionEvent e) {
            songList.updateAlbum();
        }
    };
    private final Action correctYear = new AbstractAction(I18.get("tool_correct_year")) {
        public void actionPerformed(ActionEvent e) {
            songList.updateYear();
        }
    };

    private final Action refreshPlayList = new AbstractAction(
            I18.get("lib_playlist_refresh")) {
        public void actionPerformed(ActionEvent e) {
            int n = plBox.getSelectedIndex();
            if (n > 0) {
                playList.setPlayList(n - 1);
            }
        }
    };
    private final Action newEdition = new AbstractAction(I18.get("lib_edition_new")) {
        public void actionPerformed(ActionEvent e) {
            songList.newEdition();
            refreshGroups();
        }
    };
    private final Action newFolder = new AbstractAction(I18.get("lib_folder_new")) {
        public void actionPerformed(ActionEvent e) {
            songList.newFolder();
            refreshGroups();
        }
    };
    private final Action renameFolder = new AbstractAction(I18.get("lib_folder_rename")) {
        public void actionPerformed(ActionEvent e) {
            YassSong s = songList.getFirstSelectedSong();
            if (s == null) {
                return;
            }

            if (!songList.renameFolder()) {
                return;
            }

            groups.setIgnoreChange(true);
            groups.refresh();

            String rule = s.getFolder();
            int n = groups.getRowCount();
            for (int i = 0; i < n; i++) {
                String ir = (String) groups.getValueAt(i, 0);
                if (ir.equals(rule)) {
                    groups.setRowSelectionInterval(i, i);
                    break;
                }
            }
            groups.setIgnoreChange(false);
        }
    };
    private final Action setGapHere = new AbstractAction(I18.get("tool_lyrics_start_here")) {
        public void actionPerformed(ActionEvent e) {
            int n = sheet.getPlayerPosition();
            long t = sheet.fromTimeline(n);
            setGap((int) t);
        }
    };
    private final Action setVideoMarkHere = new AbstractAction(I18.get("tool_video_mark")) {
        public void actionPerformed(ActionEvent e) {
            if (!vmarkButton.isSelected()) {
                setVideoMark(0);
                return;
            }

            setVideoMark(video.getTime());
        }
    };
    private final Action setStartHere = new AbstractAction(I18.get("tool_audio_start_here")) {
        public void actionPerformed(ActionEvent e) {
            int n = sheet.getPlayerPosition();
            long t = sheet.fromTimeline(n);
            setStart((int) t);
        }
    };
    private final Action setEndHere = new AbstractAction(I18.get("tool_audio_end_here")) {
        public void actionPerformed(ActionEvent e) {
            int n = sheet.getPlayerPosition();
            long t = sheet.fromTimeline(n);
            setEnd((int) t);
        }
    };
    private final Action removeStart = new AbstractAction(I18.get("tool_audio_start_reset")) {
        public void actionPerformed(ActionEvent e) {
            setStart(0);
        }
    };
    private final Action removeEnd = new AbstractAction(I18.get("tool_audio_end_reset")) {
        public void actionPerformed(ActionEvent e) {
            setEnd((int) (mp3.getDuration() / 1000));
        }
    };
    private final Action removeVideoGap = new AbstractAction(I18.get("tool_video_gap_reset")) {
        public void actionPerformed(ActionEvent e) {
            setVideoGap(0);
        }
    };
    private final Action undoLibraryChanges = new AbstractAction("Undo Changes") {
        public void actionPerformed(ActionEvent e) {
            undoLibraryChanges();
            refreshGroups();
        }
    };
    private final Action undoAllLibraryChanges = new AbstractAction(I18.get("lib_undo_all")) {
        public void actionPerformed(ActionEvent e) {
            undoAllLibraryChanges();
            refreshGroups();
        }
    };
    Action openSongFolder = new AbstractAction(I18.get("lib_open_folder")) {
        public void actionPerformed(ActionEvent e) {
            songList.openSongFolder();
        }
    };
    private final Action copySongInfo = new AbstractAction(I18.get("lib_copy_data")) {
        public void actionPerformed(ActionEvent e) {
            songInfo.copy(songInfo.NONE);
        }
    };
    private final Action copyCoverSongInfo = new AbstractAction(I18.get("lib_copy_cover")) {
        public void actionPerformed(ActionEvent e) {
            songInfo.copyCover();
        }
    };
    private final Action copyLyricsSongInfo = new AbstractAction(I18.get("lib_copy_lyrics")) {
        public void actionPerformed(ActionEvent e) {
            songInfo.copyLyrics();
        }
    };
    private final Action copyBackgroundSongInfo = new AbstractAction(
            I18.get("lib_copy_background")) {
        public void actionPerformed(ActionEvent e) {
            songInfo.copyBackground();
        }
    };
    private final Action copyVideoSongInfo = new AbstractAction(I18.get("lib_copy_video")) {
        public void actionPerformed(ActionEvent e) {
            songInfo.copyVideo();
        }
    };
    private final Action pasteSongInfo = new AbstractAction(I18.get("lib_paste_data")) {
        public void actionPerformed(ActionEvent e) {
            songInfo.paste();
        }
    };
    private final Action addToPlayList = new AbstractAction(I18.get("lib_playlist_add")) {
        public void actionPerformed(ActionEvent e) {
            if (!playlistToggle.isSelected())
                showPlaylistMenu.actionPerformed(null);

            Vector<YassSong> v = songList.getSelectedSongs();
            playList.addSongs(v);
            songList.repaint();
            YassSong s = v.firstElement();
            if (s == null) {
                playList.getList().selectSong(0);
            } else {
                playList.getList().selectSong(s);
            }
        }
    };
    private final Action removeFromPlayList = new AbstractAction(I18.get("lib_playlist_remove")) {
        public void actionPerformed(ActionEvent e) {
            if (!playlistToggle.isSelected())
                showPlaylistMenu.actionPerformed(null);
            playList.removeSongs();
            songList.repaint();
        }
    };
    private final Action movePlayListDown = new AbstractAction(I18.get("lib_playlist_down")) {
        public void actionPerformed(ActionEvent e) {
            playList.down();
        }
    };
    private final Action removePlayList = new AbstractAction(
            I18.get("lib_playlist_remove_disk")) {
        public void actionPerformed(ActionEvent e) {
            int n = plBox.getSelectedIndex();
            if (n > 0) {
                playList.removePlayList(n - 1);
            }
        }
    };
    private final Action movePlayListUp = new AbstractAction(I18.get("lib_playlist_up")) {
        public void actionPerformed(ActionEvent e) {
            playList.up();
        }
    };
    private final Action savePlayList = new AbstractAction(I18.get("lib_playlist_save")) {
        public void actionPerformed(ActionEvent e) {
            playList.storePlayList();

        }
    };
    private final Action savePlayListAs = new AbstractAction(I18.get("lib_playlist_save_as")) {
        public void actionPerformed(ActionEvent e) {
            playList.storePlayListAs();
        }
    };
    private final Action saveLibrary = new AbstractAction(I18.get("lib_save_all")) {
        public void actionPerformed(ActionEvent e) {
            songList.store();
            saveLibrary.setEnabled(false);
            undoAllLibraryChanges.setEnabled(false);
            refreshLibrary.setEnabled(false);

            if (savePlayList.isEnabled()) {
                savePlayList.actionPerformed(null);
            }
        }
    };
    private final Action showStartEnd = new AbstractAction(I18.get("edit_audio_offsets")) {
        public void actionPerformed(ActionEvent e) {
            if (seDialog != null) {
                if (seDialog.isShowing()) {
                    seDialog.setVisible(false);
                    return;
                }
                seDialog.pack();
                seDialog.setVisible(true);
                return;
            }

            JDialog dia = seDialog = new JDialog(new OwnerFrame());
            dia.setTitle(I18.get("edit_audio_offsets_title"));
            dia.setAlwaysOnTop(true);
            dia.setResizable(false);
            dia.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    e.getWindow().dispose();
                }
            });

            AbstractButton b;
            JToolBar panel = new JToolBar();
            panel.setFloatable(false);
            panel.add(startSpinner);

            panel.add(b = new JButton());
            b.setAction(setStartHere);
            b.setToolTipText(b.getText());
            b.setText("");
            b.setIcon(getIcon("bookmarks24Icon"));
            b.setFocusable(false);

            panel.add(b = new JButton());
            b.setAction(removeStart);
            b.setToolTipText(b.getText());
            b.setText("");
            b.setIcon(getIcon("delete24Icon"));
            b.setFocusable(false);

            panel.addSeparator();
            panel.add(endSpinner);

            panel.add(b = new JButton());
            b.setAction(setEndHere);
            b.setToolTipText(b.getText());
            b.setText("");
            b.setIcon(getIcon("bookmarks24Icon"));
            b.setFocusable(false);

            panel.add(b = new JButton());
            b.setAction(removeEnd);
            b.setToolTipText(b.getText());
            b.setText("");
            b.setIcon(getIcon("delete24Icon"));
            b.setFocusable(false);

            dia.add("Center", panel);
            dia.pack();
            // dia.setIconImage(new ImageIcon(YassActions.this.getClass().getResource("/yass/yass-icon-16.png")).getImage());
            dia.setVisible(true);
        }
    };
    private final Action multiply = new AbstractAction(I18.get("edit_bpm_double")) {
        public void actionPerformed(ActionEvent e) {
            for (YassTable t: getOpenTables(table))
                t.multiply();
            if (bpmField != null)
                bpmField.setText(String.valueOf(table.getBPM()));
        }
    };
    private final Action divide = new AbstractAction(I18.get("edit_bpm_half")) {
        public void actionPerformed(ActionEvent e) {
            for (YassTable t: getOpenTables(table))
                t.divide();
            if (bpmField != null)
                bpmField.setText(String.valueOf(table.getBPM()));
        }
    };
    private final Action recalcBpm = new AbstractAction(I18.get("edit_bpm_recalc")) {
        public void actionPerformed(ActionEvent e) {
            double newBpm = 0;
            String strBpm = bpmField.getText().replace(",", ".");
            if (bpmField != null && Double.parseDouble(strBpm) > 0) {
                newBpm = Double.parseDouble(bpmField.getText());
            }
            for (YassTable t: getOpenTables(table)) {
                t.recalcBPM(newBpm);
            }
            if (bpmField != null)
                bpmField.setText(String.valueOf(table.getBPM()));
        }
    };
    private final Action showLyricsStart = new AbstractAction(I18.get("edit_gap")) {
        public void actionPerformed(ActionEvent e) {
            updateGap();

            if (gapDialog != null) {
                if (gapDialog.isShowing()) {
                    gapDialog.setVisible(false);
                    return;
                }
                gapDialog.pack();
                gapDialog.setVisible(true);
                return;
            }

            JDialog dia = gapDialog = new JDialog(new OwnerFrame());
            dia.setTitle(I18.get("edit_gap_title"));
            dia.setAlwaysOnTop(true);
            dia.setResizable(false);
            dia.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    e.getWindow().dispose();
                }
            });

            AbstractButton b;
            JToolBar panel = new JToolBar();
            panel.setFloatable(false);
            panel.add(gapSpinner);

            panel.add(b = new JButton());
            b.setAction(setGapHere);
            b.setToolTipText(b.getText());
            b.setText("");
            b.setIcon(getIcon("gap24Icon"));
            b.setFocusable(false);

            panel.addSeparator();
            panel.add(new JLabel(I18.get("edit_bpm_title")));

            double bpm = table.getBPM();
            bpmField = new JTextField(String.valueOf(bpm), 5);
            bpmField.addActionListener(e1 -> {
                String s = bpmField.getText();
                double bpm1 = table.getBPM();
                try {
                    bpm1 = Double.parseDouble(s);
                } catch (Exception ex) {
                    bpmField.setText(String.valueOf(bpm1));
                }
                for (YassTable t: getOpenTables(table))
                    t.setBPM(bpm1);
            });
            panel.add(bpmField);

            panel.add(b = new JButton());
            b.setAction(multiply);
            b.setToolTipText(b.getText());
            b.setText("");
            b.setIcon(getIcon("fastforward24Icon"));
            b.setFocusable(false);
            panel.add(b = new JButton());
            b.setAction(divide);
            b.setToolTipText(b.getText());
            b.setText("");
            b.setIcon(getIcon("rewind24Icon"));
            b.setFocusable(false);
            panel.add(b = new JButton());
            b.setAction(recalcBpm);
            b.setToolTipText(b.getText());
            b.setText("");
            b.setIcon(getIcon("refresh24Icon"));
            b.setFocusable(false);
            panel.add(b = new JButton());
            b.setAction(showOnlineHelpBeat);
            b.setToolTipText(b.getText());
            b.setText("");
            b.setIcon(getIcon("help24Icon"));
            b.setFocusable(false);

            dia.add("Center", panel);
            dia.pack();
            // dia.setIconImage(new
            // ImageIcon(YassActions.this.getClass().getResource("/yass/yass-icon-16.png")).getImage());
            dia.setLocationRelativeTo(main);
            dia.setVisible(true);
        }
    };
    private final Action showVideoGap = new AbstractAction(I18.get("edit_videogap")) {
        public void actionPerformed(ActionEvent e) {
            sheet.showVideo(true);

            if (vgapDialog != null) {
                if (vgapDialog.isShowing()) {
                    sheet.showVideo(false);
                    vgapDialog.setVisible(false);
                    return;
                }
                vgapDialog.pack();
                vgapDialog.setVisible(true);
                return;
            }

            updateGap();

            JDialog fh = vgapDialog = new JDialog(new OwnerFrame());
            fh.setTitle(I18.get("edit_videogap_title"));
            fh.setAlwaysOnTop(true);
            fh.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    sheet.showVideo(false);
                    // video.closeVideo();
                    e.getWindow().dispose();
                }
            });
            updateVideo();

            fh.add("Center", createVideoToolbar());
            fh.pack();
            fh.setVisible(true);

            long time = sheet.fromTimeline(sheet.getPlayerPosition());
            video.setTime((int) time);
        }
    };
    private final Action showLibTable = new AbstractAction(I18.get("lib_source")) {
        public void actionPerformed(ActionEvent e) {
            Vector<?> v = songList.getSelectedFiles();
            if (v.size() < 1 || v.size() > 10) {
                return;
            }

            for (Enumeration<?> en = v.elements(); en.hasMoreElements(); ) {
                String fn = (String) en.nextElement();

                JDialog fh = new JDialog(new OwnerFrame());
                fh.setTitle(I18.get("lib_source_title"));
                fh.setAlwaysOnTop(true);
                fh.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        e.getWindow().dispose();
                    }
                });

                YassTable t = new YassTable();
                t.loadFile(fn);
                fh.add("Center", new JScrollPane(t));
                fh.pack();
                int w = 240;
                int h = 400;
                fh.setSize(w, h);
                fh.setVisible(true);
            }
        }
    };
    private final Action showNothing = new AbstractAction(I18.get("edit_view_1")) {
        public void actionPerformed(ActionEvent e) {
            showNothing();
        }
    };
    Action showBackground = new AbstractAction(I18.get("edit_view_2")) {
        public void actionPerformed(ActionEvent e) {
            sheet.showVideo(false);
            sheet.showBackground(true);
            showBackgroundToggle.setSelected(true);
            videoButton.setSelected(false);
            sheet.repaint();
        }
    };
    Action showVideo = new AbstractAction(I18.get("edit_view_3")) {
        public void actionPerformed(ActionEvent e) {
            if (!YassVideoUtils.useFOBS) {
                return;
            }
            sheet.showVideo(true);
            sheet.showBackground(false);
            showVideoToggle.setSelected(true);
            videoButton.setSelected(true);

            long time = sheet.fromTimeline(sheet.getPlayerPosition());
            video.setTime((int) time);
            video.refreshPlayer();
            sheet.repaint();
        }
    };
    private final Action toggleVideo = new AbstractAction(I18.get("edit_video_toggle")) {
        public void actionPerformed(ActionEvent e) {
            boolean onoff = sheet.showVideo() || sheet.showBackground();
            onoff = !onoff;

            if (onoff) {
                video.setTime(0);
                if (video.getFrame() != null) {
                    sheet.showVideo(true);
                    sheet.showBackground(false);
                    showVideoToggle.setSelected(true);
                } else {
                    sheet.showVideo(false);
                    sheet.showBackground(true);
                    showBackgroundToggle.setSelected(true);
                }
            } else {
                sheet.showVideo(false);
                sheet.showBackground(false);
                showNothingToggle.setSelected(true);
            }

            videoButton.setSelected(onoff);

            long time = sheet.fromTimeline(sheet.getPlayerPosition());
            video.setTime((int) time);
            video.refreshPlayer();
            sheet.repaint();
        }
    };

    private final Action showHelp = new AbstractAction(I18.get("lib_help_offline")) {
        public void actionPerformed(ActionEvent e) {
            helpPane = new JTextPane();
            HTMLDocument doc = (HTMLDocument) helpPane.getEditorKitForContentType("text/html").createDefaultDocument();
            doc.setAsynchronousLoadPriority(-1);
            helpPane.setDocument(doc);
            URL url = I18.getResource("help.html");
            try { helpPane.setPage(url); } catch (Exception ignored) { }

            helpPane.addHyperlinkListener(event -> {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    URL url1 = I18.getResource(event.getDescription());
                    try { helpPane.setPage(url1); } catch (IOException ignored) {}
                }
            });
            helpPane.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        SwingUtilities.getWindowAncestor(helpPane).dispose();
                    }
                    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        URL url = I18.getResource("help5.html");
                        try { helpPane.setPage(url); } catch (Exception ignored) {}
                    }
                }
            });
            helpPane.setEditable(false);

            JFrame fh = new JFrame(I18.get("lib_help_offline_title"));
            fh.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    e.getWindow().dispose();
                }
            });
            fh.add("Center", new JScrollPane(helpPane));
            fh.pack();
            fh.setSize(500, 600);
            fh.setIconImage(new ImageIcon(YassActions.this.getClass().getResource("/yass/resources/img/yass-icon-16.png")).getImage());
            fh.setVisible(true);
        }
    };
    private final Action showOnlineHelp = new AbstractAction(I18.get("lib_help_online")) {
        public void actionPerformed(ActionEvent e) {
            openURL("http://www.yass-along.com");
        }
    };
    private final Action showOnlineHelpErrors = new AbstractAction(I18.get("lib_help_online")) {
        public void actionPerformed(ActionEvent e) {
            openURL("http://www.yass-along.com/errors");
        }
    };
    private final Action showOnlineHelpBeat = new AbstractAction(I18.get("lib_help_online")) {
        public void actionPerformed(ActionEvent e) {
            openURL("http://www.yass-along.com/beats");
        }
    };
    private final Action showOnlineHelpDuet = new AbstractAction(I18.get("lib_help_online")) {
        public void actionPerformed(ActionEvent e) {
            openURL("http://www.yass-along.com/duets");
        }
    };
    private final Action editLyrics = new AbstractAction(I18.get("edit_lyrics_edit")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            editLyrics();
        }
    };

    private final Action setVideoGapHere = new AbstractAction(
            I18.get("tool_video_start_here")) {
        public void actionPerformed(ActionEvent e) {
            int n = sheet.getPlayerPosition();
            long t = sheet.fromTimeline(n);
            setVideoGap((int) (vmark - t));

            setVideoMark(0);
            vmarkButton.setSelected(false);

            long time = sheet.fromTimeline(sheet.getPlayerPosition());
            video.setTime((int) time);
            video.refreshPlayer();
        }
    };
    /**
     * Unfortunately, most Java runtime environments fire multiple KEY_PRESSED
     * and in some cases multiple KEY_RELEASED events when the user has only
     * pressed the key once. Additionally, in some environments you may never
     * receive the KEY_RELEASED events for an upstroke. This is because the
     * behavior of KEY_PRESSED and KEY_RELEASED is system dependent. The
     * behavior occurs through an interaction with the operating system's
     * handling of key repeats that occur when you hold down a key for a period
     * of time. On Windows, Java will produce multiple KEY_PRESSED events as the
     * key is held down and only one KEY_RELEASED when the key is actually
     * released. For example, holding down the 'A' key will generate these
     * events: PRESSED 'A' PRESSED 'A' ... RELEASED 'A' On Unix, multiple pairs
     * of KEY_PRESSED and KEY_RELEASED are received as the key is held down:
     * PRESSED 'A' RELEASED 'A' PRESSED 'A' RELEASED 'A' ... PRESSED 'A'
     * RELEASED 'A'
     */
    Action playSelection = new AbstractAction(I18.get("edit_play_selection")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }

            playSelection(0);
        }
    };
    private final Action playSelectionWithMIDI = new AbstractAction(
            I18.get("edit_play_selection_midi")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }

            playSelection(1);
        }
    };
    private final Action playSelectionWithMIDIAudio = new AbstractAction(
            I18.get("edit_play_selection_midi_audio")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }

            playSelection(2);
        }
    };
    private final Action playPage = new AbstractAction(I18.get("edit_play_page")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            playPageOrFrozen(0, false);
        }
    };
    private final Action playPageWithMIDI = new AbstractAction(I18.get("edit_play_page_midi")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            playPageOrFrozen(1, false);
        }
    };
    private final Action playPageWithMIDIAudio = new AbstractAction(I18.get("edit_play_page_midi_audio")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            playPageOrFrozen(2, false);
        }
    };
    private final Action playFrozen = new AbstractAction(I18.get("edit_copy_play")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            playPageOrFrozen(0, true);
        }
    };
    private final Action playBefore = new AbstractAction(I18.get("edit_play_before")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            playSelectionBefore(0);
        }
    };

    private final Action playNext = new AbstractAction(I18.get("edit_play_next")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            playSelectionNext(0);
        }
    };

    private final Action playFrozenWithMIDI = new AbstractAction(I18.get("edit_copy_play_midi")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            playPageOrFrozen(1, true);
        }
    };

    private final Action playFrozenWithMIDIAudio = new AbstractAction(I18.get("edit_copy_play_midi_audio")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            playPageOrFrozen(2, true);
        }
    };

    private final Action recordSelection = new AbstractAction(I18.get("edit_record")) {
        final long[] inout = new long[2];

        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            int i = table.getSelectionModel().getMinSelectionIndex();
            if (i < 0) {
                JOptionPane.showMessageDialog(getTab(), "<html>" + I18.get("edit_record_error_msg"), I18.get("edit_record_error_title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            int j = table.getSelectionModel().getMaxSelectionIndex();
            long[][] clicks = table.getSelection(i, j, inout, null, false);
            inout[0] = Math.max(0, inout[0] - 2000000);
            inout[1] = -1;// inout[1] + 2000000;

            if (clicks != null) {
                recordLength = clicks.length;

                String defbase = prop.getProperty("record-timebase");
                int def = 1;
                if (defbase != null) {
                    def = Integer.parseInt(defbase) - 1;
                }

                Object[] speed = {"100%", "50%", "33%", "25%"};
                String s = (String) JOptionPane.showInputDialog(
                        tab, "<html>"
                                + MessageFormat.format(
                                I18.get("edit_record_msg"), recordLength), I18.get("edit_record_title"), JOptionPane.INFORMATION_MESSAGE, null, speed, speed[def]);

                if (s == null || s.length() < 1) {
                    return;
                }

                int timebase = 1;
                if (s.equals(speed[1])) {
                    timebase = 2;
                }
                if (s.equals(speed[2])) {
                    timebase = 3;
                }
                if (s.equals(speed[3])) {
                    timebase = 4;
                }
                prop.setProperty("record-timebase", String.valueOf(timebase));
                prop.store();

                // int ok = JOptionPane.showConfirmDialog(tab, "<html>" +
                // MessageFormat.format(I18.get("edit_record_msg"), // recordLength), I18.get("edit_record_title"), // JOptionPane.OK_CANCEL_OPTION);

                startRecording();
                mp3.playSelection(inout[0], inout[1], null, timebase);
            }
        }
    };

    private final Action setPlayTimebase = new AbstractAction(I18.get("edit_speed")) {
        public void actionPerformed(ActionEvent e) {
            playSlower.actionPerformed(e);
        }
    };

    private final Action playSlower = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (playTimebase == 1) {
                playTimebase = 2;
                speedButton.setSelectedIcon(getIcon("speedtwo24Icon"));
            } else if (playTimebase == 2) {
                playTimebase = 3;
                speedButton.setSelectedIcon(getIcon("speedthree24Icon"));
            } else if (playTimebase == 3) {
                playTimebase = 4;
                speedButton.setSelectedIcon(getIcon("speedfour24Icon"));
            } else {
                playTimebase = 1;
            }

            speedButton.setSelected(playTimebase != 1);
        }
    };

    private final Action playAll = new AbstractAction(I18.get("edit_play_all")) {
        public void actionPerformed(ActionEvent e) {
            previewEdit(true);
            playAll(true);
        }
    };
    private final Action playAllFromHere = new AbstractAction(I18.get("edit_play_remainder")) {
        public void actionPerformed(ActionEvent e) {
            previewEdit(true);
            playAll(false);
        }
    };
    private final Action fullscreen = new AbstractAction(I18.get("lib_fullscreen")) {
        public void actionPerformed(ActionEvent e) {
            fullscreen();
        }
    };

    private final Action recordAll = new AbstractAction(I18.get("edit_record_all")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }

            table.clearSelection();
            startRecording();
            mp3.playAll(null);
        }
    };
    private final Action pasteRows = new AbstractAction(I18.get("edit_paste")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            table.pasteRows();

            snapshotButton.setSelected(false);
            if (showCopyCBI != null) showCopyCBI.setState(false);
            sheet.showSnapshot(false);
            sheet.repaint();
        }
    };
    private final Action pasteNotes = new AbstractAction(I18.get("edit_paste_notes")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            table.insertNotesHere();

            snapshotButton.setSelected(false);
            if (showCopyCBI != null) showCopyCBI.setState(false);
            sheet.showSnapshot(false);
            sheet.repaint();
        }
    };
    private final Action insertNote = new AbstractAction(I18.get("edit_add")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            table.insertNote();
        }
    };
    private final Action removeRows = new AbstractAction(I18.get("edit_remove")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            table.removeRows();
        }
    };
    private final Action removeRowsWithLyrics = new AbstractAction(I18.get("edit_remove_with_lyrics")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            table.removeRowsWithLyrics();
        }
    };
    private final Action enableVideoPreview = new AbstractAction(I18.get("edit_playallvideo_toggle")) {
        public void actionPerformed(ActionEvent e) {
            // playAllVideoCBI
        }
    };
    private final Action enableVideoAudio = new AbstractAction(I18.get("tool_video_audio_toggle")) {
        public void actionPerformed(ActionEvent e) {
            video.muteVideo(!videoAudioButton.isSelected());
        }
    };
    private final Action joinRows = new AbstractAction(I18.get("edit_join")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            table.joinRows();
        }
    };
    private final Action splitRows = new AbstractAction(I18.get("edit_split")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            table.splitRows();
        }
    };
    private final Action rollLeft = new AbstractAction(I18.get("edit_roll_left")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            table.rollLeft();
        }
    };
    private final Action rollRight = new AbstractAction(I18.get("edit_roll_right")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            table.rollRight();
        }
    };
    private final Action togglePageBreak = new AbstractAction(I18.get("edit_break")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            table.togglePageBreak();
        }
    };
    private final Action removePageBreak = new AbstractAction(I18.get("edit_break_remove")) {
        public void actionPerformed(ActionEvent e) {
            removePageBreak();
        }
    };

    public void removePageBreak() {
        int row = table.getSelectionModel().getMinSelectionIndex() - 1;
        if (row < 1) {
            return;
        }
        table.removePageBreak(true);
        table.setRowSelectionInterval(row, row);
    }

    private final Action copyRows = new AbstractAction(I18.get("edit_copy")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            copyRows();

            snapshotButton.setSelected(true);
            if (showCopyCBI != null) showCopyCBI.setState(true);
            sheet.showSnapshot(true);
            sheet.repaint();
        }
    };
    private final Action prevPage = new AbstractAction(I18.get("edit_page_prev")) {
        public void actionPerformed(ActionEvent e) {
            stopPlaying();
            table.gotoPage(-1);
        }
    };
    private final Action nextPage = new AbstractAction(I18.get("edit_page_next")) {
        public void actionPerformed(ActionEvent e) {
            stopPlaying();
            table.gotoPage(1);
        }
    };
    private final Action lessPages = new AbstractAction(I18.get("edit_page_less")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            int k = table.getMultiSize();
            if (k > 2) {
                setRelative(false);
                YassTable.setZoomMode(YassTable.ZOOM_MULTI);
            } else {
                setRelative(true);
                YassTable.setZoomMode(YassTable.ZOOM_ONE);
            }
            table.addMultiSize(-1);
            table.zoomPage();
            lyrics.repaintLineNumbers();
        }
    };
    private final Action morePages = new AbstractAction(I18.get("edit_page_more")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            setRelative(false);
            YassTable.setZoomMode(YassTable.ZOOM_MULTI);
            table.addMultiSize(1);
            table.zoomPage();
            lyrics.repaintLineNumbers();
        }
    };
    private final Action onePage = new AbstractAction(I18.get("edit_page_one")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            setRelative(true);
            YassTable.setZoomMode(YassTable.ZOOM_ONE);
            table.setMultiSize(1);
            table.zoomPage();
            lyrics.repaintLineNumbers();
        }
    };
    private final Action allRemainingPages = new AbstractAction(I18.get("edit_page_all_from_here")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            setRelative(false);
            YassTable.setZoomMode(YassTable.ZOOM_MULTI);
            table.addMultiSize(table.getRowCount());
            table.zoomPage();
            lyrics.repaintLineNumbers();
        }
    };
    private final Action shiftLeft = new AbstractAction(I18.get("edit_shift_left")) {
        public void actionPerformed(ActionEvent e) {
            table.shiftBeat(-1);
        }
    };
    private final Action shiftRight = new AbstractAction(I18.get("edit_shift_right")) {
        public void actionPerformed(ActionEvent e) {
            table.shiftBeat(+1);
        }
    };
    private final Action shiftLeftRemainder = new AbstractAction(I18.get("edit_shift_left_remainder")) {
        public void actionPerformed(ActionEvent e) {
            table.shiftRemainder(-1);
        }
    };
    private final Action shiftRightRemainder = new AbstractAction(I18.get("edit_shift_right_remainder")) {
        public void actionPerformed(ActionEvent e) {
            table.shiftRemainder(+1);
        }
    };
    private final Action home = new AbstractAction(I18.get("edit_home")) {
        public void actionPerformed(ActionEvent e) {
            table.home();
        }
    };
    private final Action end = new AbstractAction(I18.get("edit_end")) {
        public void actionPerformed(ActionEvent e) {
            table.end();
        }
    };
    private final Action first = new AbstractAction(I18.get("edit_first")) {
        public void actionPerformed(ActionEvent e) {
            table.firstNote();
        }
    };
    private final Action last = new AbstractAction(I18.get("edit_last")) {
        public void actionPerformed(ActionEvent e) {
            table.lastNote();
        }
    };
    private final Action activatePrevTrack = new AbstractAction(I18.get("edit_tracks_prev")) {
        public void actionPerformed(ActionEvent e) {
            activateNextTrack(-1);
        }
    };
    private final Action activateNextTrack = new AbstractAction(I18.get("edit_tracks_next")) {
        public void actionPerformed(ActionEvent e) {
            activateNextTrack(1);
        }
    };
    private final Action saveAll = new AbstractAction(I18.get("edit_save_all")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing() || isFilterEditing())
                return;
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(tab, I18.get("edit_save_all_msg"), I18.get("edit_save_all"), JOptionPane.OK_CANCEL_OPTION))
                return;
            save(openTables);
        }
    };
    private final Action saveTrack = new AbstractAction(I18.get("edit_save_track")) {
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing() || isFilterEditing())
                return;
            if (table.getDuetTrackCount() > 0) {
                if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(tab,
                        MessageFormat.format(I18.get("edit_save_duet_msg"), table.getDuetTrackCount()),
                        I18.get("edit_save_track"), JOptionPane.OK_CANCEL_OPTION))
                    return;
            } else {
                if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(tab,
                        I18.get("edit_save_track_msg"),
                        I18.get("edit_save_track"), JOptionPane.OK_CANCEL_OPTION))
                    return;
            }
            Vector<YassTable> v = new Vector<>();
            v.add(table);
            save(v);
        }
    };
    private final Action mergeTracks = new AbstractAction(I18.get("edit_tracks_merge")) {
        public void actionPerformed(ActionEvent e) {
            boolean bContinue = true;
            boolean sameGap = YassTable.sameGap(openTables);
            boolean sameBPM = YassTable.sameBPM(openTables);
            if (! sameBPM) {
                bContinue = JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(tab, I18.get("edit_merge_bpm_text"), I18.get("edit_merge_bpm_title"), JOptionPane.OK_CANCEL_OPTION);
            }
            else if (! sameGap) {
                bContinue = JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(tab, I18.get("edit_merge_gap_text"), I18.get("edit_merge_gap_title"), JOptionPane.OK_CANCEL_OPTION);
            }
            if (bContinue) {
                String filename = askFilename(I18.get("lib_edit_file_msg"), FileDialog.SAVE);
                if (filename != null) {
                    YassTable mt = YassTable.mergeTables(openTables, prop);
                    if (!mt.storeFile(filename)) // todo warn
                        return;
                    openFiles(filename, false);
                }
            }
        }
    };
    private final Action exchangeTracks = new AbstractAction(I18.get("edit_tracks_exchange")) {
        public void actionPerformed(ActionEvent e) {
            if (table.getDuetTrackCount() == 2) {
                exchangeTracks(new int[]{1,0});
                main.repaint();
            } else if (table.getDuetTrackCount() > 2) {
                YassTable[] tracks = getOpenTables(table).toArray(new YassTable[0]);
                JPanel grid = new JPanel(new GridLayout(tracks.length, 2));
                final JComboBox[] nameCombo = new JComboBox[tracks.length];
                final String[] prev = new String[1];
                for (int i = 0; i < tracks.length; i++)
                {
                    String[] names = new String[tracks.length];
                    for (int j=0; j<tracks.length; j++) {
                        names[j] = (i+1) + ": " + tracks[j].getDuetTrackName();
                    }
                    nameCombo[i] = new JComboBox(names);
                    nameCombo[i].setName("cb"+i);
                    nameCombo[i].setSelectedIndex(i);
                    nameCombo[i].addItemListener(e1 -> {
                        if (e1.getStateChange() == ItemEvent.DESELECTED) {
                            prev[0] = e1.getItem().toString().substring(3);
                        }
                        else {
                            String sel = e1.getItem().toString().substring(3);
                            String cbName = ((JComboBox) e1.getSource()).getName();
                            for (int j = 0; j < tracks.length; j++) {
                                if (cbName.equals("cb" + j))
                                    continue;
                                String sel2 = nameCombo[j].getSelectedItem().toString().substring(3);
                                if (sel.equals(sel2)) {
                                    nameCombo[j].setSelectedItem((j+1) + ": " + prev[0]);
                                }
                            }
                        }
                    });
                    grid.add(nameCombo[i]);
                }
                JDialog dia = new JDialog(new OwnerFrame());
                dia.setTitle(I18.get("edit_tracks_exchange"));
                dia.setAlwaysOnTop(true);
                dia.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        e.getWindow().dispose();
                    }
                });
                dia.add("Center", grid);
                JPanel buttons = new JPanel();
                JButton b;
                buttons.add(b = new JButton("Ok"));
                b.addActionListener(e2 -> {
                    dia.dispose();
                    int[] order = new int[tracks.length];
                    for (int j = 0; j < tracks.length; j++)
                        order[j] = nameCombo[j].getSelectedIndex();
                    exchangeTracks(order);
                });
                buttons.add(b = new JButton("Cancel"));
                b.addActionListener(e2 -> dia.dispose());
                dia.add("South", buttons);
                dia.pack();
                int w = 200;
                dia.setSize(w, dia.getHeight());
                dia.setLocationRelativeTo(main);
                dia.setVisible(true);
            }
        }
    };

    private void exchangeTracks(int[] order) {
        YassTable[] tracks = getOpenTables(table).toArray(new YassTable[0]);
        YassTable[] t = new YassTable[tracks.length];
        for (int i=0; i< tracks.length; i++) {
            t[i] = new YassTable();
            t[i].loadTable(tracks[i], true);
        }
        for (int i=0; i < tracks.length; i++) {
            tracks[i].loadTable(t[order[i]], true);
            tracks[i].setSaved(false);
            tracks[i].setDuetTrack(t[i].getDuetTrack(), t[order[i]].getDuetTrackName());
        }
        for (YassTable track : tracks) {
            getAutoCorrect().checkData(track, true, true);
            track.addUndo();
        }
        sheet.init();
        updateActions();
        main.repaint();
    }

    private final Action closeAll = new AbstractAction(I18.get("edit_close_all")) {
        public void actionPerformed(ActionEvent e) {
            closeAll();
        }
    };
    private final Action reloadAll = new AbstractAction(I18.get("edit_reload")) {
        public void actionPerformed(ActionEvent e) {
            if (cancelOpen())
                return;
            String recent = prop.getProperty("recent-files");
            if (recent == null)
                return;

            int sel = table.getSelectionModel().getMinSelectionIndex();
            if (sel < 0)
                sel = sheet.nextElement();
            final int selectRow = sel;

            openFiles(recent, false);
            openEditor(selectRow >= 0);

            SwingUtilities.invokeLater(() -> {
                lyrics.setPreventFireUpdate(true);
                if (selectRow >= 0 && selectRow < table.getRowCount()) {
                    table.setRowSelectionInterval(selectRow, selectRow);
                    table.zoomPage();
                    table.updatePlayerPosition();
                }
                lyrics.setPreventFireUpdate(false);
            });
        }
    };
    private final Action closeLibrary = new AbstractAction(I18.get("lib_close")) {
        public void actionPerformed(ActionEvent e) {
            closeAllTables();
            setLibraryLoaded(false);

            String filename = prop.getProperty("songlist-cache");
            File f = new File(filename);
            f.delete();
            filename = prop.getProperty("playlist-cache");
            f = new File(filename);
            f.delete();

            prop.remove("song-directory");
            prop.remove("playlist-directory");
            prop.remove("cover-directory");
            prop.remove("recent-files");

            songInfo.setSong(null);
            songInfoToggle.setSelected(false);
            songInfo.clear();
            playList.clear();
            updatePlayListBox();
            songList.load();
        }
    };
    private final Action openFile = new AbstractAction(I18.get("edit_file_open")) {
        public void actionPerformed(ActionEvent e) {
            openFile();
        }
    };
    private final Action openTrack = new AbstractAction(I18.get("edit_tracks_open")) {
        public void actionPerformed(ActionEvent e) {
            openTrack();
        }
    };
    private final Action openFolder = new AbstractAction(I18.get("edit_tracks_open_folder")) {
        public void actionPerformed(ActionEvent e) {
            openFolder();
        }
    };
    private final Action openFolderFromLibrary = new AbstractAction(I18.get("edit_tracks_open_folder")) {
        public void actionPerformed(ActionEvent e) {
            openFolderFromLibrary();
        }
    };
    private final Action closeTrack = new AbstractAction(I18.get("edit_tracks_close")) {
        public void actionPerformed(ActionEvent e) {
            if (!cancelTrack())
                closeTrack();
        }
    };
    private final Action saveTrackAs = new AbstractAction(I18.get("edit_tracks_save")) {
        public void actionPerformed(ActionEvent e) {
            saveTrackAs();
        }
    };
    private final Action saveDuetAs = new AbstractAction(I18.get("edit_tracks_save_duet")) {
        public void actionPerformed(ActionEvent e) {
            saveDuetAs();
        }
    };
    private final Action renameTrack = new AbstractAction(I18.get("edit_tracks_rename")) {
        public void actionPerformed(ActionEvent e) {
            renameTrack();
        }
    };
    private final Action deleteTrack = new AbstractAction(I18.get("edit_tracks_delete")) {
        public void actionPerformed(ActionEvent e) {
            deleteTrack();
        }
    };
    private final Action newFile = new AbstractAction(I18.get("lib_new")) {
        public void actionPerformed(ActionEvent e) {
            createNewSong(null);
        }
    };
    private final Action removeSong = new AbstractAction(I18.get("lib_remove")) {
        public void actionPerformed(ActionEvent e) {
            songList.removeSelectedSongs();
        }
    };
    private final Action filterLibrary = new AbstractAction(I18.get("lib_find")) {
        public void actionPerformed(ActionEvent e) {
            startFilterLibrary();
        }
    };
    /*
     * Action importFiles = new AbstractAction(I18.get("lib_import")) { public
     * void actionPerformed(ActionEvent e) { String songdir =
     * YassUtils.getSongDir(table, prop); if (songdir == null) { return; }
     * JFileChooser fileChooser = new JFileChooser(".");
     * fileChooser.setMultiSelectionEnabled(true);
     * fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
     * fileChooser.setDialogTitle(I18.get("lib_import_title")); JPanel pan =
     * new JPanel(new BorderLayout()); JLabel label = new
     * JLabel("<html><center><font color=gray>" + I18.get("lib_import_msg") +
     * "</html>"); pan.add("Center", label); fileChooser.setAccessory(pan); int
     * status = fileChooser.showOpenDialog(tab); if (status ==
     * JFileChooser.APPROVE_OPTION) { File selectedFiles[] =
     * fileChooser.getSelectedFiles(); YassImport.importFiles(songList, prop, * selectedFiles); } } };
     */
    private final Action importMetadata = new AbstractAction(I18.get("lib_import_meta")) {
        public void actionPerformed(ActionEvent e) {
            songList.importMetadata();
        }
    };
    private final Action exportMetadata = new AbstractAction(I18.get("lib_export_meta")) {
        public void actionPerformed(ActionEvent e) {
            songList.exportMetadata();
        }
    };
    private final Action exit = new AbstractAction(I18.get("lib_exit")) {
        public void actionPerformed(ActionEvent e) {
            exit();
        }
    };
    private final Action interruptPlay = new AbstractAction(I18.get("lib_stop_song")) {
        public void actionPerformed(ActionEvent e) {
            mp3.interruptMP3();
            // lyrics.finishEditing();
        }
    };
    private final Action incHeight = new AbstractAction(I18.get("edit_height_inc")) {
        public void actionPerformed(ActionEvent e) {
            table.shiftHeight(+1);
        }
    };
    private final Action incHeightOctave = new AbstractAction(I18.get("edit_height_inc_octave")) {
        public void actionPerformed(ActionEvent e) {
            table.shiftHeight(+12);
        }
    };
    private final Action decHeight = new AbstractAction(I18.get("edit_height_dec")) {
        public void actionPerformed(ActionEvent e) {
            table.shiftHeight(-1);
        }
    };
    private final Action decHeightOctave = new AbstractAction(I18.get("edit_height_dec_octave")) {
        public void actionPerformed(ActionEvent e) {
            table.shiftHeight(-12);
        }
    };
    private final Action incLeft = new AbstractAction(I18.get("edit_length_left_inc")) {
        public void actionPerformed(ActionEvent e) {
            table.shiftLeftEndian(+1);
        }
    };
    private final Action decLeft = new AbstractAction(I18.get("edit_length_left_dec")) {
        public void actionPerformed(ActionEvent e) {
            table.shiftLeftEndian(-1);
        }
    };
    private final Action incRight = new AbstractAction(I18.get("edit_length_right_inc")) {
        public void actionPerformed(ActionEvent e) {
            table.shiftRightEndian(+1);
        }
    };
    private final Action decRight = new AbstractAction(I18.get("edit_length_right_dec")) {
        public void actionPerformed(ActionEvent e) {
            table.shiftRightEndian(-1);
        }
    };
    private final Action prevBeat = new AbstractAction(I18.get("edit_prev")) {
        public void actionPerformed(ActionEvent e) {
            table.prevBeat();
        }
    };
    private final Action nextBeat = new AbstractAction(I18.get("edit_next")) {
        public void actionPerformed(ActionEvent e) {
            table.nextBeat();
        }
    };
    private final Action selectPrevBeat = new AbstractAction(I18.get("edit_select_left")) {
        public void actionPerformed(ActionEvent e) {
            table.selectPrevBeat();
        }
    };
    private final Action selectNextBeat = new AbstractAction(I18.get("edit_select_right")) {
        public void actionPerformed(ActionEvent e) {
            table.selectNextBeat();
        }
    };
    private final Action autoCorrectPageBreaks = new AbstractAction(
            I18.get("edit_correct_breaks")) {
        public void actionPerformed(ActionEvent e) {
            trimPageBreaks();
        }
    };
    private final Action golden = new AbstractAction(I18.get("edit_golden")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            table.setType("*");
        }
    };
    private final Action freestyle = new AbstractAction(I18.get("edit_freestyle")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            table.setType("F");
        }
    };
    private final Action rap = new AbstractAction(I18.get("edit_rap")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            table.setType("R");
        }
    };
    private final Action rapgolden = new AbstractAction(I18.get("edit_rapgolden")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            table.setType("G");
        }
    };
    private final Action darkmode = new AbstractAction(I18.get("edit_darkmode")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            sheet.setDarkMode(!sheet.darkMode);
            sheet.firePropsChanged();
        }
    };
    private final Action detailLibrary = new AbstractAction(I18.get("lib_details_toggle")) {
        public void actionPerformed(ActionEvent e) {
            updateSongInfo(detailLibrary);
        }
    };
    private final Action standard = new AbstractAction(I18.get("edit_normal")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }
            if (table.getRowAt(table.getSelectedRow()).getType().equals(":")) {
                playSelectionNext(0);
            } else {
                table.setType(":");
            }
        }
    };
    private final Action addEndian = new AbstractAction(I18.get("edit_length_right_inc")) {
        public void actionPerformed(ActionEvent e) {
            table.shiftRightEndian(+1);
        }
    };

    private final Action minus = new AbstractAction(I18.get("edit_minus")) {
        public void actionPerformed(ActionEvent e) {
            lyrics.enter(YassRow.HYPHEN + "");
        }
    };
    private final Action space = new AbstractAction(I18.get("edit_space")) {
        public void actionPerformed(ActionEvent e) {
            lyrics.enter(YassRow.SPACE + "");
        }
    };
    private final Action incGap = new AbstractAction(I18.get("edit_gap_add_10")) {
        public void actionPerformed(ActionEvent e) {
            if (gapDialog != null && gapDialog.isShowing()) {
                table.addGap(+10);
                updateGap();
            }
        }
    };
    private final Action decGap = new AbstractAction(I18.get("edit_gap_sub_10")) {
        public void actionPerformed(ActionEvent e) {
            if (gapDialog != null && gapDialog.isShowing()) {
                table.addGap(-10);
                updateGap();
            }
        }
    };
    private final Action incGap2 = new AbstractAction(I18.get("edit_gap_add_1000")) {
        public void actionPerformed(ActionEvent e) {
            if (gapDialog != null && gapDialog.isShowing()) {
                table.addGap(+1000);
                updateGap();
            }
        }
    };
    private final Action decGap2 = new AbstractAction(I18.get("edit_gap_sub_1000")) {
        public void actionPerformed(ActionEvent e) {
            if (gapDialog != null && gapDialog.isShowing()) {
                table.addGap(-1000);
                updateGap();
            }
        }
    };
    Action selectLine = new AbstractAction(I18.get("edit_select_line")) {
        public void actionPerformed(ActionEvent e) {
            table.selectLine();
        }
    };
    Action viewAll = new AbstractAction(I18.get("edit_view_all")) {
        public void actionPerformed(ActionEvent e) {
            table.viewAll();
        }
    };
    Action selectAll = new AbstractAction(I18.get("edit_select_all")) {
        public void actionPerformed(ActionEvent e) {
            table.selectAll();
        }
    };
    private final Action homeSong = new AbstractAction(I18.get("lib_home")) {
        public void actionPerformed(ActionEvent e) {
            songList.gotoSong(table);
        }
    };
    private final Action showTable = new AbstractAction(I18.get("edit_source")) {
        public void actionPerformed(ActionEvent e) {
            if (table == null)
                return;
            if (srcDialog != null) {
                srcDialog.dispose();
                srcDialog = null;
            }
            JDialog dia = srcDialog = new JDialog(new OwnerFrame());
            dia.setTitle(I18.get("edit_source_title"));
            dia.setAlwaysOnTop(true);
            dia.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    e.getWindow().dispose();
                }
            });
            dia.add("Center", new JScrollPane(table));
            dia.pack();
            int w = 240;
            int h = 400;
            dia.setSize(w, h);
            // dia.setIconImage(new
            // ImageIcon(YassActions.this.getClass().getResource("/yass/yass-icon-16.png")).getImage());
            dia.setVisible(true);
        }
    };
    private final Action undo = new AbstractAction(I18.get("edit_undo")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            if (lyrics.isEditable()) {
                lyrics.finishEditing();
            }
            table.undoRows();
            checkData(table, false, true);
            updateGap();
        }
    };
    private final Action redo = new AbstractAction(I18.get("edit_redo")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            if (lyrics.isEditable()) {
                lyrics.finishEditing();
            }
            table.redoRows();
            checkData(table, false, true);
            updateGap();
        }
    };
    private final Action openFileFromLibrary = new AbstractAction(I18.get("lib_edit_file")) {
        public void actionPerformed(ActionEvent e) {
            if (currentView == VIEW_EDIT) {
                if (cancelOpen()) {
                    return;
                }
            }
            String filename = e.getActionCommand();
            if (filename == null || !new File(filename).exists())
                filename = askFilename(I18.get("lib_edit_file_msg"), FileDialog.LOAD);
            if (filename == null)
                return;
            openFiles(filename, false);
        }
    };
    private final Action gotoLibrary = new AbstractAction(I18.get("edit_lib")) {
        public void actionPerformed(ActionEvent e) {
            if (currentView == VIEW_LIBRARY) {
                return;
            }

            if (cancelOpen()) {
                return;
            }
            YassTable lastTable = table;
            closeAllTables();
            video.closeVideo();
            setView(VIEW_LIBRARY);
            if (!songList.isLoaded()) {
                songList.load();
                songList.gotoSong(lastTable);
            } else {
                YassSong s = songList.gotoSong(lastTable);
                if (s != null) {
                    songList.loadSongDetails(s, new YassTable());
                }
                songList.repaint();
            }
        }
    };
    Action autoCorrect = new AbstractAction(I18.get("edit_correct_all")) {
        public void actionPerformed(ActionEvent e) {
            auto.autoCorrectAllSafe(table);
        }
    };
    private final Action autoCorrectSpacing = new AbstractAction(
            I18.get("edit_correct_space")) {
        public void actionPerformed(ActionEvent e) {
            boolean changed = auto.autoCorrectSpacing(table);
            if (changed) {
                table.addUndo();
                ((YassTableModel) table.getModel()).fireTableDataChanged();
            }
            checkData(table, false, true);
        }
    };
    private final Action autoCorrectTransposed = new AbstractAction(
            I18.get("edit_correct_heights")) {
        public void actionPerformed(ActionEvent e) {
            boolean changed = auto.autoCorrectTransposed(table);
            if (changed) {
                table.addUndo();
                ((YassTableModel) table.getModel()).fireTableDataChanged();
            }
            checkData(table, false, true);
        }
    };
    private final Action showCopiedRows = new AbstractAction(I18.get("edit_copy_show")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            if (sheet != null) {
                boolean onoff = !sheet.isSnapshotShown();
                if (e.getSource() != snapshotButton) {
                    snapshotButton.setSelected(onoff);
                }
                if (showCopyCBI != null) showCopyCBI.setState(onoff);
                sheet.showSnapshot(onoff);
                sheet.repaint();
            }
        }
    };
    private final Action removeCopy = new AbstractAction(I18.get("edit_copy_remove")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            if (sheet != null) {
                sheet.removeSnapshot();
                sheet.repaint();
            }
        }
    };
    private final Action showSongInfo = new AbstractAction(I18.get("lib_info_toggle")) {
        public void actionPerformed(ActionEvent e) {
            updateSongInfo(showSongInfo);
        }
    };
    private final Action showSongInfoBackground = new AbstractAction(
            I18.get("lib_background_toggle")) {
        public void actionPerformed(ActionEvent e) {
            updateSongInfo(showSongInfoBackground);
        }
    };
    Action showErrors = new AbstractAction(I18.get("edit_errors")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();
            if (errDialog != null) {
                if (errDialog.isShowing()) {
                    //errDialog.setVisible(false);
                    return;
                }
                errDialog.pack();
                errDialog.setVisible(true);
                return;
            }

            JDialog dia = errDialog = new JDialog(new OwnerFrame());
            dia.setTitle(I18.get("edit_errors_title"));
            dia.setAlwaysOnTop(true);
            dia.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    e.getWindow().dispose();
                }
            });
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.add("Center", errors);
            errorPanel.setSize(440, 400);
            errors.getMessagePanel().setPreferredSize(new Dimension(440, 180));
            errorPanel.setPreferredSize(new Dimension(440, 400));

            dia.add("Center", errorPanel);
            dia.pack();
            // dia.setIconImage(new
            // ImageIcon(YassActions.this.getClass().getResource("/yass/yass-icon-16.png")).getImage());
            dia.setVisible(true);
        }
    };
    private final Action showPlaylist = new AbstractAction(I18.get("lib_playlist_toggle")) {
        public void actionPerformed(ActionEvent e) {
            updateSongInfo(showPlaylist);
        }
    };
    private final Action showOptions = new AbstractAction(I18.get("lib_prefs_title")) {
        public void actionPerformed(ActionEvent e) {
            String oldDir = prop.getProperty("song-directory");
            String oldListDir = prop.getProperty("playlist-directory");
            boolean oldSpacing = prop.isUncommonSpacingAfter();
            if (oldDir == null) {
                oldDir = "";
            }
            if (oldListDir == null) {
                oldListDir = "";
            }

            boolean moveArticles = prop.get("use-articles").equals("true");

            //
            new yass.options.YassOptions(YassActions.this);
            //

            loadColors();
            loadKeys();
            loadLayout();
            try {
                setBeforeNextMs(Integer.parseInt(prop.getProperty("before_next_ms")));
            } catch (Exception ignored) {
                setBeforeNextMs(300);
            }

            auto.init(prop);

            String layout = prop.getProperty("editor-layout");
            if (layout == null) {
                layout = "East";
            }
            sheet.setLyricsLayout(layout);

            String hLangs = prop.getProperty("note-naming-h").toLowerCase();
            sheet.setHNoteEnabled(hLangs.contains(I18.getLanguage()));

            lyrics.init(prop);

            String seekInOffset = prop.getProperty("seek-in-offset");
            String seekOutOffset = prop.getProperty("seek-out-offset");
            try {
                mp3.setSeekInOffset(Integer.parseInt(seekInOffset));
                mp3.setSeekOutOffset(Integer.parseInt(seekOutOffset));
            } catch (Exception ignored) {
            }
            String seekInOffsetMs = prop.getProperty("seek-in-offset-ms");
            String seekOutOffsetMs = prop.getProperty("seek-out-offset-ms");
            try {
                mp3.setSeekInOffsetMs(Integer.parseInt(seekInOffsetMs));
                mp3.setSeekOutOffsetMs(Integer.parseInt(seekOutOffsetMs));
            } catch (Exception ignored) {
            }

            String newDir = prop.getProperty("song-directory");
            String newpDir = prop.getProperty("playlist-directory");

            String importDir = prop.getProperty("import-directory");
            if (newDir != null && !importDir.startsWith(newDir)) {
                prop.setProperty("import-directory", newDir);
                prop.store();
                JOptionPane.showMessageDialog(tab, I18.get("lib_prefs_import_error"), I18.get("lib_prefs_title"), JOptionPane.WARNING_MESSAGE);
            }

            boolean newMoveArticles = prop.get("use-articles").equals("true");
            songList.moveArticles(newMoveArticles);

            boolean useWav = prop.get("use-sample").equals("true");
            mp3.useWav(useWav);

            boolean debugWaveform = prop.getProperty("debug-waveform").equals(
                    "true");
            mp3.createWaveform(debugWaveform);

            updateSheetProperties();
            boolean autoTrim = prop.getProperty("auto-trim").equals("true");
            setAutoTrim(autoTrim);

            boolean needSongDirUpdate = newDir != null && !newDir.equals(oldDir);
            if (newpDir != null && !newpDir.equals(oldListDir)) {
                needSongDirUpdate = true;
            }
            if (moveArticles != newMoveArticles) {
                needSongDirUpdate = true;
            }
            if (oldSpacing != prop.isUncommonSpacingAfter()) {
                for (YassTable table : openTables) {
                    table.setPreventUndo(true);
                    ((YassTableModel) table.getModel()).fireTableDataChanged();
                    table.setPreventUndo(false);
                }
            }

            if (needSongDirUpdate) {
                setLibraryLoaded(false);
                playList.clear();
                updatePlayListBox();
                songList.clear();
                songList.load();
            }
        }
    };
    private final Action showPlaylistMenu = new AbstractAction(
            I18.get("lib_playlist_toggle")) {
        public void actionPerformed(ActionEvent e) {
            playlistToggle.setSelected(!playlistToggle.isSelected());
            updateSongInfo(showPlaylist);
        }
    };
    Action setLibrary = new AbstractAction(I18.get("lib_set_library")) {
        public void actionPerformed(ActionEvent e) {
            new YassLibOptions(prop, YassActions.this, songList, mp3);
        }
    };
    private final Action enableAudio = new AbstractAction(I18.get("edit_audio_toggle")) {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() != mp3Button) {
                mp3Button.setSelected(!mp3Button.isSelected());
            } else {
                mp3.setAudioEnabled(mp3Button.isSelected());
            }
            if (audioCBI != null) {
                audioCBI.setState(mp3Button.isSelected());
            }
        }
    };
    private JCheckBoxMenuItem clicksCBI = null;
    private final Action enableClicks = new AbstractAction(I18.get("edit_clicks_toggle")) {
        public void actionPerformed(ActionEvent e) {
            mp3.setClicksEnabled(!mp3.isClicksEnabled());
            if (clicksCBI != null) {
                clicksCBI.setState(mp3.isClicksEnabled());
            }
        }
    };
    private JCheckBoxMenuItem micCBI = null;
    private final Action enableMic = new AbstractAction(I18.get("edit_mic_toggle")) {
        public void actionPerformed(ActionEvent e) {
            mp3.setCapture(!mp3.isCapture());
            if (micCBI != null) {
                micCBI.setState(mp3.isCapture());
            }
        }
    };
    private JCheckBoxMenuItem midiCBI = null;
    private final Action enableMidi = new AbstractAction(I18.get("edit_midi_toggle")) {
        public void actionPerformed(ActionEvent e) {
            interruptPlay();

            if (e.getSource() != midiButton) {
                midiButton.setSelected(!midiButton.isSelected());
            } else {
                mp3.setMIDIEnabled(midiButton.isSelected());
            }
            if (midiCBI != null) {
                midiCBI.setState(midiButton.isSelected());
            }
            sheet.setPaintHeights(midiButton.isSelected());
            table.zoomPage();
            updatePlayerPosition();
            sheet.repaint();
        }
    };
    Action absolute = new AbstractAction(I18.get("edit_align")) {
        // Align
        public void actionPerformed(ActionEvent e) {
            if (lyrics.isEditable() || songList.isEditing()
                    || isFilterEditing()) {
                return;
            }

            boolean state = sheet.isPanEnabled();

            if (table.getMultiSize() == 1) {
                state = !state;
                sheet.enablePan(state);

                sheet.update();
                revalidateLyricsArea();
                sheet.repaint();
            }
            if (alignCBI != null) {
                alignCBI.setState(state);
            }
        }
    };

    public YassActions(YassSheet s, YassPlayer m, YassLyrics lyr) {
        sheet = s;
        mp3 = m;
        lyrics = lyr;

        configureOptionPane();

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("");
        progressBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // String dir = prop.getProperty("song-directory");
                // if (dir != null && dir.length() > 0 && new
                // File(dir).exists()) {
                // return;
                // }
                SwingUtilities.invokeLater(() -> {
                    progressBar.repaint();
                    new YassLibOptions(prop, YassActions.this, songList, mp3);
                });
            }
        });

        mp3.addPlayerListener(new YassPlayerListener() {
            public void playerStarted() {
            }

            public void playerStopped() {
                if (currentView == VIEW_EDIT) {
                    stopPlaying();
                    stopRecording();
                    YassTapNotes.evaluateTaps(table, sheet.getTemporaryNotes());
                } else if (currentView == VIEW_LIBRARY) {
                    if (!soonStarting) {
                        stopPlaySong();
                    }
                }
                // int i = sheet.nextElement();
                // table.setRowSelectionInterval(i, i);
                // updatePlayerPosition();
                // sheet.repaint();
            }
        });

        // setDropTarget(sheet);
        registerEditorActions(sheet);
        PropertyChangeListener plis = e -> {
            String p = e.getPropertyName();
            if (p.equals("play")) {
                String s1 = (String) e.getNewValue();
                Integer mode = (Integer) e.getOldValue();
                int m1 = mode == null ? 0 : mode.intValue();
                if (s1.equals("start")) {
                    playSelection(m1);
                }
                if (s1.equals("page")) {
                    playPageOrFrozen(m1, false);
                }
                if (s1.equals("before")) {
                    playSelectionBefore(m1);
                }
                if (s1.equals("next")) {
                    playSelectionNext(m1);
                }
                if (s1.equals("stop")) {
                    interruptPlay();
                }
            } else if (p.equals("relHeight")) {
                int h = ((Integer) e.getNewValue()).intValue();
                table.shiftHeight(h);
            } else if (p.equals("midi")) {
                int h = ((Integer) e.getNewValue()).intValue();
                mp3.playMIDI(h);
            } else if (p.equals("relBeat")) {
                table.shiftBeat(((Integer) e.getNewValue()).intValue());
            } else if (p.equals("relBeatRemainder")) {
                table.shiftRemainder(((Integer) e.getNewValue()).intValue());
            } else if (p.equals("relBeatLine")) {
                table.shiftLine(((Integer) e.getNewValue()).intValue());
            } else if (p.equals("relLeft")) {
                table.shiftLeftEndian(((Integer) e.getNewValue())
                        .intValue());
            } else if (p.equals("relRight")) {
                table.shiftRightEndian(((Integer) e.getNewValue())
                        .intValue());
            } else if (p.equals("page")) {
                int i = ((Integer) e.getNewValue()).intValue();
                table.gotoPage(i);
            } else if (p.equals("one")) {
                onePage.actionPerformed(null);
            } else if (p.equals("split")) {
                table.split(((Double) e.getNewValue()).doubleValue());
            } else if (p.equals("joinLeft")) {
                table.joinLeft();
            } else if (p.equals("joinRight")) {
                table.joinRight();
            } else if (p.equals("join")) {
                table.joinRows();
            } else if (p.equals("rollLeft")) {
                table.rollLeft();
            } else if (p.equals("rollRight")) {
                Character c = (Character) e.getOldValue();
                Integer i = (Integer) e.getNewValue();
                if (c != null && i != null) {
                    table.rollRight(c.charValue(), i.intValue());
                } else {
                    table.rollRight();
                }
            } else if (p.equals("removePageBreak")) {
                table.removePageBreak(true);
            } else if (p.equals("addPageBreak")) {
                table.insertPageBreak(true);
            } else if (p.equals("editLyrics")) {
                editLyrics();
            } else if (p.equals("allRemainingPages")) {
                allRemainingPages.actionPerformed(null);
            } else if (p.equals("start")) {
                Integer i = (Integer) e.getNewValue();
                setStart(i.intValue());
            } else if (p.equals("end")) {
                Integer i = (Integer) e.getNewValue();
                setEnd(i.intValue());
            } else if (p.equals("gap")) {
                Integer i = (Integer) e.getNewValue();
                setGap(i.intValue());
            } else if (p.equals("videogap")) {
                Integer i = (Integer) e.getNewValue();
                setVideoGap(i.intValue());
            }
        };
        sheet.addPropertyChangeListener(plis);
        lyr.addPropertyChangeListener(plis);
    }

    public static void openURL(String url) {
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                java.lang.reflect.Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
                openURL.invoke(null, url);
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else {
                String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape", "chrome"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++) {
                    if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                }
                if (browser == null) {
                    throw new Exception("Could not find web browser");
                } else {
                    Runtime.getRuntime().exec(new String[]{browser, url});
                }
            }
        } catch (Exception ignored) {}
    }

    public static void openURLFile(String fn) {
        Vector<String> v = new Vector<>();
        v.addElement(fn);
        openURLFiles(v);
    }

    public static void openURLFiles(Vector<String> fn) {
        if (fn == null || fn.size() < 1 || fn.size() > 10) {
            return;
        }
        try {
            for (Enumeration<String> en = fn.elements(); en.hasMoreElements(); ) {
                String filename = en.nextElement();

                try {
                    // Java 1.6
                    Class<?> c = Class.forName("java.awt.Desktop");
                    Method m = c.getMethod("getDesktop", (Class[]) null);
                    Object desktop = m.invoke(null, (Object[]) null);

                    Class[] par = new Class[1];
                    par[0] = File.class;
                    m = c.getMethod("open", par);
                    m.invoke(desktop, new File(filename));
                    // System.out.println("desktop open " + filename);
                } catch (Throwable t) {
                    try {
                        String os = System.getProperty("os.name");
                        if (os.startsWith("Windows")) {
                            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + filename);
                        } else if (System.getProperty("os.name").startsWith(
                                "MacOS")) {
                            Class<?> fileMgr = Class
                                    .forName("com.apple.eio.FileManager");
                            java.lang.reflect.Method openURL = fileMgr
                                    .getDeclaredMethod("openURL", String.class);
                            openURL.invoke(null, filename);
                        } else if (System.getProperty("os.name").startsWith("Linux")) {
                            String[] apps = {"gnome-open", "kfmclient", "xdg-open", "gvfs-open", "firefox", "opera", "chrome"};
                            String app = null;
                            for (int i = 0; i < apps.length && app == null; i++) {
                                if (Runtime
                                        .getRuntime()
                                        .exec(new String[]{"which", apps[i]})
                                        .waitFor() == 0) {
                                    app = apps[i];
                                }
                            }
                            if (app != null) {
                                Runtime.getRuntime().exec(new String[]{app, filename});
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // BUG Workaround for JOptionPane: add keyboard support
    private static void configureOptionPane() {
        if (UIManager.getLookAndFeelDefaults().get("OptionPane.actionMap") == null) {
            UIManager.put("OptionPane.windowBindings", new Object[]{"ESCAPE", "close", "LEFT", "left", "KP_LEFT", "left", "RIGHT", "right", "KP_RIGHT", "right"});
            ActionMap map = new javax.swing.plaf.ActionMapUIResource();
            map.put("close", new OptionPaneCloseAction());
            map.put("left", new OptionPaneArrowAction(false));
            map.put("right", new OptionPaneArrowAction(true));
            UIManager.getLookAndFeelDefaults().put("OptionPane.actionMap", map);
        }
    }

    private void loadColors() {
        for (int i = 0; i < colors.length; i++) {
            String c = prop.getProperty("color-" + i);
            Color col = Color.decode(c);
            colors[i] = new Color(col.getRed(), col.getGreen(), col.getBlue(), 255);
        }

        Color[] colorSet = new Color[YassSheet.COLORSET_COUNT];
        for (int i = 0; i < colorSet.length; i++) {
            String c = prop.getProperty("note-color-" + i);
            Color color = Color.decode(c);
            colorSet[i] = new Color(color.getRed(), color.getGreen(), color.getBlue(), 221);
        }
        sheet.setColors(colorSet);
        lyrics.setColors(colorSet);
        YassTableRenderer.setColors(colorSet);
        errBackground = colorSet[YassSheet.COLOR_ERROR];
        minorerrBackground = colorSet[YassSheet.COLOR_WARNING];

        boolean shade = prop.get("shade-notes").equals("true");
        sheet.shadeNotes(shade);
        boolean darkMode = prop.containsKey("dark-mode") && prop.get("dark-mode").equals("true");
        sheet.setDarkMode(darkMode);
        sheet.firePropsChanged();
        sheet.repaint();
    }

    private void loadKeys() {
        int[] keycodes = sheet.getKeyCodes();

        for (int i = 0; i < keycodes.length; i++) {
            String str = prop.getProperty("key-" + i);
            try {
                int code = AWTKeyStroke.getAWTKeyStroke(str).getKeyCode();
                if (code != -1) {
                    keycodes[i] = code;
                }
            } catch (Exception ignored) { }

        }
    }

    private void loadLayout() {
        String lyricsWidthString = prop.getProperty("lyrics-width");
        int lyricsWidth = Integer.parseInt(lyricsWidthString);
        sheet.setLyricsWidth(lyricsWidth);

        boolean debugMemory = prop.getProperty("debug-memory").equals("true");
        sheet.setDebugMemory(debugMemory);
    }

    public void storeColors() {
        for (int i = 0; i < colors.length; i++) {
            String c = "color-" + i;

            String rgb = Integer.toHexString(colors[i].getRGB());
            prop.put(c, rgb);
        }

        Color[] col = sheet.getColors();
        for (int i = 0; i < col.length; i++) {
            String rgb = Integer.toHexString(col[i].getRGB());
            prop.put("note-color-" + i, rgb);
        }
    }

    public Color getTableColor(int i) {
        if (i < colors.length)
            return colors[i];
        else
            return colors[(i + 2) % colors.length].darker();
    }

    public JComponent getTab() {
        return tab;
    }

    public void setTab(JComponent c) {
        tab = c;
    }

    public YassTable getTable() {
        return table;
    }

    public void setActiveTable(YassTable t) {
        if (table != null)
            table.removePropertyChangeListener(propertyListener);
        table = t;
        if (table != null)
            table.addPropertyChangeListener(propertyListener);
    }

    public YassProperties getProperties() {
        return prop;
    }

    public void closeAllTables() {
        if (srcDialog != null) {
            srcDialog.dispose();
            srcDialog = null;
        }
        openTables.clear();
        sheet.removeAll();
        setActiveTable(null);
        lyrics.setTable(null);
        sheet.setActiveTable(null);
        songList.closeOpened();
        mp3.disposeMediaPlayer();

        isUpdating = true;
        updateLyrics();
        errors.setTable(null);
        // updateMP3Info(table);
        // updateRaw();
        // updateCover();
        // updateBackground();
        // updateVideo();
        isUpdating = false;
        updateTrackComponent();
        updateActions();
    }

    /**
     * Creates table and adds it to sheet.
     * @return never null
     */
    private YassTable createNextTable() {
        YassTable t = new YassTable();
        t.init(prop);
        t.setTableColor(getTableColor(openTables.size()));
        sheet.addTable(t);
        openTables.addElement(t);
        t.setSheet(sheet);
        t.setAutoCorrect(auto);
        t.setActions(this);
        registerEditorActions(t);
        // setDropTarget(t);
        t.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                showMessage(0);
            }
        });
        t.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                showMessage(0);
            }
        });
        t.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                showMessage(0);
            }
        });
        return t;
    }

    public void showMessage(int id) {
        errors.showMessage(id);
    }

    private JFrame getFrame(Component c) {
        Window root = SwingUtilities.getWindowAncestor(c);
        if (root instanceof JFrame) {
            return (JFrame) root;
        }
        return null;
    }

    public boolean autoTrim() {
        return autoTrim;
    }

    public void setAutoTrim(boolean onoff) {
        autoTrim = onoff;
        if (sheet != null) {
            sheet.setAutoTrim(onoff);
        }
    }

    public void checkData(YassTable t, boolean checkAll, boolean checkExtensive) {
        auto.checkData(t, checkAll, checkExtensive);

        errors.setTable(table);

        if (currentView == VIEW_EDIT && autoTrim()
                && (t.hasMinorPageBreakMessages() || t.hasPageBreakMessages())) {
            boolean oldUndo = table.getPreventUndo();
            table.setPreventUndo(true);
            trimPageBreaks();
            table.setPreventUndo(oldUndo);
        }

        if (t.hasMinorPageBreakMessages()) {
            correctPageBreakButton.setBackground(minorerrBackground);
            correctPageBreakButton.setEnabled(true);
        } else if (t.hasPageBreakMessages()) {
            correctPageBreakButton.setBackground(errBackground);
            correctPageBreakButton.setEnabled(true);
        } else {
            correctPageBreakButton.setBackground(stdBackground);
            correctPageBreakButton.setEnabled(false);
        }

        boolean hasSpacing = t.hasSpacingMessages();
        correctSpacingButton.setBackground(hasSpacing ? errBackground : stdBackground);
        correctSpacingButton.setEnabled(hasSpacing);

        boolean hasTrans = t.hasTransposedMessages();
        correctTransposedButton.setBackground(hasTrans ? errBackground : stdBackground);
        correctTransposedButton.setEnabled(hasTrans);
    }

    public YassTable firstTable() {
        if (openTables.size() < 1)
            return null;
        return openTables.elementAt(0);
    }

    private void updateTrackComponent() {
        int n = openTables.size();
        for (int i = 0; i < n; i++)
            openTables.elementAt(i).setTableColor(getTableColor(i));

        Component[] tcs = trackComponent.getComponents();
        if (tcs.length != n) {
            Vector<YassSheetInfo> toRemove = new Vector<>(n);
            for (Component tc : tcs) {
                if (tc instanceof YassSheetInfo) {
                    YassSheetInfo info = (YassSheetInfo) tc;
                    info.removeListener();
                    toRemove.add(info);
                }
            }
            for (YassSheetInfo info: toRemove)
                trackComponent.remove(info);
            trackComponent.setLayout(new GridLayout(n, 1));
            for (int i = 0; i < n; i++)
                trackComponent.add(new YassSheetInfo(sheet, i));
            main.revalidate();
            main.repaint();
        }
    }

    private void activateNextTrack(int b) {
        int i = openTables.indexOf(table);
        int n = openTables.size();
        i += b;
        if (i < 0) {
            i = n - 1;
        }
        if (i > n - 1) {
            i = 0;
        }
        activateTrack(i);
    }

    public int getActiveTrack() {
        return openTables.indexOf(table);
    }

    /**
     * Activate track (0 = track #1).
     * @param i 0-7
     */
    public void activateTrack(int i) {
        int n = openTables.size();
        if (i < 0 || i > n - 1)
            return;

        int rowIndex = sheet.firstVisibleNote(i);
        int page = openTables.elementAt(i).getPageNumber(rowIndex);

        YassTable t = openTables.elementAt(i);
        setActiveTable(t);
        updateActions();

        sheet.setActiveTable(table);
        String vd = table.getVideo();
        if (video != null && vd != null) {
            video.setVideo(table.getDir() + File.separator + vd);
        }
        String bg = table.getBackgroundTag();
        if (bg != null) {
            File file = new File(table.getDir() + File.separator + bg);
            BufferedImage img = null;
            if (file.exists()) {
                try {
                    img = YassUtils.readImage(file);
                } catch (Exception ignored) {}
            }
            sheet.setBackgroundImage(img);
            mp3.setBackgroundImage(img);
        }
        updateTitle();
        lyrics.setTable(table);
        isUpdating = true;
        updateLyrics(); // bug: startAutoSpellCheck triggers table update
        // updateMP3Info(table);
        // updateRaw();
        // updateCover();
        // updateBackground();
        // updateVideo();
        isUpdating = false;
        SwingUtilities.invokeLater(() -> {
            if (page >= 0)
                table.gotoPageNumber(page);
        });
    }

    public ImageIcon getIcon(String s) {
        return icons.get(s);
    }

    public YassAutoCorrect getAutoCorrect() {
        return auto;
    }

    public YassPlayer getMP3() {
        return mp3;
    }

    public YassVideo getVideo() {
        return video;
    }

    public void setVideo(YassVideo v) {
        video = v;
        // video.setStoreAction(saveVideo);
    }

    public void setSongInfo(YassSongInfo c) {
        songInfo = c;
        songInfo.setStoreAction(saveLibrary);
        songInfo.setCopyAction(copySongInfo);
        songInfo.setCopyAction(copyCoverSongInfo, copyBackgroundSongInfo, copyVideoSongInfo, copyLyricsSongInfo);
        songInfo.setPasteAction(pasteSongInfo);
        songInfo.setReloadAction(undoLibraryChanges, undoAllLibraryChanges);
        copySongInfo.setEnabled(false);
        pasteSongInfo.setEnabled(false);
    }

    public void setGroups(YassGroups g) {
        groups = g;

        groups.addPropertyChangeListener(p -> {
            String pn = p.getPropertyName();
            if (pn.equals("group") || pn.equals("rule")) {
                clearFilter();
            }
        });
    }

    public YassErrors getErrors() {
        return errors;
    }

    public void setErrors(YassErrors i) {
        errors = i;
        errors.setAutoCorrect(auto);
    }

    public void setPlayList(YassPlayList p) {
        playList = p;
        playList.setStoreAction(savePlayList, savePlayListAs);
        playList.setMoveAction(movePlayListUp, movePlayListDown);
        savePlayList.setEnabled(false);
        savePlayListAs.setEnabled(false);
        removePlayList.setEnabled(false);
        refreshPlayList.setEnabled(false);
    }

    public YassSongList getSongList() {
        return songList;
    }

    public void setSongList(YassSongList s) {
        songList = s;
        songList.setOpenAction(openSongFromLibrary);
        songList.setDirAction(setLibrary);
        songList.setPrintAction(printLibrary);
        songList.setAutoAction(correctLibrary);
        songList.setStoreAction(saveLibrary);
        songList.setUndoAllAction(undoAllLibraryChanges);
        setLibraryLoaded(false);
        saveLibrary.setEnabled(false);
        undoAllLibraryChanges.setEnabled(false);

        songList.addSongListListener(e -> {
            int state = e.getState();
            if (state == YassSongListEvent.LOADED) {
                groups.refreshCounters();
            }
        });
        // setDropTarget(songList);
    }

    public void init(YassProperties p) {
        prop = p;

        loadColors();
        loadKeys();
        loadLayout();
        try {
            setBeforeNextMs(Integer.parseInt(prop.getProperty("before_next_ms")));
        } catch (Exception ignored) {
            setBeforeNextMs(300);
        }

        String layout = prop.getProperty("editor-layout");
        if (layout == null) {
            layout = "East";
        }
        sheet.setLyricsLayout(layout);

        String hLanguages = prop.getProperty("note-naming-h").toLowerCase();
        sheet.setHNoteEnabled(hLanguages.contains(I18.getLanguage()));

        boolean autoTrim = prop.getProperty("auto-trim").equals("true");
        setAutoTrim(autoTrim);

        boolean useWav = prop.getProperty("use-sample").equals("true");
        mp3.useWav(useWav);

        auto = new YassAutoCorrect();
        auto.init(prop);
        lyrics.setAutoCorrect(auto);

        String seekInOffset = prop.getProperty("seek-in-offset");
        if (seekInOffset != null) {
            try {
                int off = Integer.parseInt(seekInOffset);
                mp3.setSeekInOffset(off);
            } catch (Exception ignored) {
            }
        }
        String seekOutOffset = prop.getProperty("seek-out-offset");
        if (seekOutOffset != null) {
            try {
                int off = Integer.parseInt(seekOutOffset);
                mp3.setSeekOutOffset(off);
            } catch (Exception ignored) {
            }
        }

        String seekInOffsetMs = prop.getProperty("seek-in-offset-ms");
        if (seekInOffsetMs != null) {
            try {
                int off = Integer.parseInt(seekInOffsetMs);
                mp3.setSeekInOffsetMs(off);
            } catch (Exception ignored) {
            }
        }
        String seekOutOffsetMs = prop.getProperty("seek-out-offset-ms");
        if (seekOutOffsetMs != null) {
            try {
                int off = Integer.parseInt(seekOutOffsetMs);
                mp3.setSeekOutOffsetMs(off);
            } catch (Exception ignored) {
            }
        }

        boolean debugWaveform = prop.getProperty("debug-waveform").equals("true");
        mp3.createWaveform(debugWaveform);

        icons.put("new16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/New16.gif")));
        icons.put("new24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/New24.gif")));
        icons.put("open16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Open16.gif")));
        icons.put("open24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Open24.gif")));
        icons.put("save16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Save16.gif")));
        icons.put("save24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Save24.gif")));
        icons.put("saveas16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/SaveAs16.gif")));
        icons.put("saveas24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/SaveAs24.gif")));
        icons.put("list16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/List16.gif")));
        icons.put("list24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/List24.gif")));
        icons.put("playlist16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Playlist16.gif")));
        icons.put("playlist24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Playlist24.gif")));
        icons.put("add16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Add16.gif")));
        icons.put("add24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Add24.gif")));
        icons.put("import16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Import16.gif")));
        icons.put("import24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Import24.gif")));
        icons.put("edit16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Edit16.gif")));
        icons.put("edit24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Edit24.gif")));
        icons.put("editany24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/EditAny24.gif")));
        // icons.put("hyphen16Icon", new ImageIcon(getClass().getResource("/yass/Hyphenate16.gif")));
        icons.put("hyphenate24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Hyphenate24.gif")));
        // icons.put("check16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Properties16.gif")));
        // icons.put("check24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Properties24.gif")));
        icons.put("spell24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/SpellCheck24.gif")));
        icons.put("refresh16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Refresh16.gif")));
        icons.put("refresh24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Refresh24.gif")));
        icons.put("find16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Find16.gif")));
        icons.put("find24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Find24.gif")));
        icons.put("clearfind24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/ClearFind24.gif")));
        icons.put("pagebreak16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/PageBreak16.gif")));
        icons.put("pagebreak24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/PageBreak24.gif")));
        icons.put("insertnote16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/InsertNote16.gif")));
        icons.put("insertnote24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/InsertNote24.gif")));
        insertNote.putValue(AbstractAction.SMALL_ICON, getIcon("insertnote16Icon"));
        icons.put("correctpagebreak24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/CorrectPageBreak24.gif")));
        icons.put("correctfilenames24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/CorrectFileNames24.gif")));
        icons.put("correcttags24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/CorrectTags24.gif")));
        icons.put("correcttext24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/CorrectText24.gif")));
        icons.put("correcttransposed24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/CorrectTransposed24.gif")));
        icons.put("correctlength24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/CorrectLength24.gif")));
        icons.put("correctalbum24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/CorrectAlbum24.gif")));
        icons.put("correctyear24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/CorrectYear24.gif")));
        icons.put("print16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Print16.gif")));
        icons.put("print24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Print24.gif")));
        icons.put("movie16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/media/Movie16.gif")));
        icons.put("movie24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/media/Movie24.gif")));
        icons.put("fastforward16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/media/FastForward16.gif")));
        icons.put("fastforward24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/media/FastForward24.gif")));
        icons.put("rewind16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/media/Rewind16.gif")));
        icons.put("rewind24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/media/Rewind24.gif")));
        icons.put("empty16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Empty16.gif")));
        icons.put("empty24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Empty24.gif")));
        icons.put("tiles24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Tiles24.gif")));
        icons.put("notiles16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/NoTiles16.gif")));
        icons.put("notiles24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/NoTiles24.gif")));
        icons.put("lyrics16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Lyrics16.gif")));
        icons.put("lyrics24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Lyrics24.gif")));
        icons.put("noerr24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/NoError24.gif")));
        icons.put("err24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Error24.gif")));
        icons.put("grayOpen16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/GrayOpen16.gif")));
        icons.put("grayOpen24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/GrayOpen24.gif")));
        icons.put("just16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/text/AlignJustify16.gif")));
        icons.put("just24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/text/AlignJustify24.gif")));
        icons.put("freestyle16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Freestyle16.gif")));
        icons.put("freestyle24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Freestyle24.gif")));
        icons.put("golden16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Golden16.gif")));
        icons.put("golden24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Golden24.gif")));
        icons.put("rap16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Rap16.gif")));
        icons.put("rap24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Rap24.gif")));
        icons.put("rapgolden16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/RapGolden16.gif")));
        icons.put("rapgolden24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/RapGolden24.gif")));
        icons.put("space16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Space16.gif")));
        icons.put("space24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Space24.gif")));
        icons.put("minus16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Minus16.gif")));
        icons.put("minus24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Minus24.gif")));
        icons.put("lockon24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Lock24.gif")));
        icons.put("lockoff24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Unlock24.gif")));
        golden.putValue(AbstractAction.SMALL_ICON, getIcon("golden16Icon"));
        rap.putValue(AbstractAction.SMALL_ICON, getIcon("rap16Icon"));
        rapgolden.putValue(AbstractAction.SMALL_ICON, getIcon("rapgolden16Icon"));
        freestyle.putValue(AbstractAction.SMALL_ICON, getIcon("freestyle16Icon"));
        togglePageBreak.putValue(AbstractAction.SMALL_ICON, getIcon("pagebreak16Icon"));
        icons.put("home16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/navigation/Home16.gif")));
        icons.put("home24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/navigation/Home24.gif")));
        icons.put("err24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Error24.gif")));
        icons.put("setTitle24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/SetTitle24.gif")));
        icons.put("setArtist24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/SetArtist24.gif")));
        icons.put("setGenre24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/SetGenre24.gif")));
        icons.put("setEdition24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/SetEdition24.gif")));
        icons.put("setLanguage24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/SetLanguage24.gif")));
        icons.put("setYear24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/SetYear24.gif")));
        icons.put("newEdition24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/NewEdition24.gif")));
        icons.put("newFolder24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/NewFolder24.gif")));
        icons.put("renameFolder24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/RenameFolder24.gif")));
        icons.put("play16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/media/Play16.gif")));
        icons.put("play24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/media/Play24.gif")));
        icons.put("stop16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/media/Stop16.gif")));
        icons.put("stop24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/media/Stop24.gif")));
        playSong.putValue(AbstractAction.SMALL_ICON, getIcon("play16Icon"));
        stopPlaySong.putValue(AbstractAction.SMALL_ICON, getIcon("stop16Icon"));
        icons.put("stepf16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/navigation/Down16.gif")));
        icons.put("stepf24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/navigation/Down24.gif")));
        icons.put("stepb16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/navigation/Up16.gif")));
        icons.put("stepb24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/navigation/Up24.gif")));
        playSelection.putValue(AbstractAction.SMALL_ICON, getIcon("play16Icon"));
        interruptPlay.putValue(AbstractAction.SMALL_ICON, getIcon("stop16Icon"));
        icons.put("zoomstd16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/ZoomPage16.gif")));
        icons.put("zoomstd24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/ZoomPage24.gif")));
        icons.put("zoomin16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/ZoomIn16.gif")));
        icons.put("zoomin24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/ZoomIn24.gif")));
        icons.put("zoomout16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/ZoomOut16.gif")));
        icons.put("zoomout24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/ZoomOut24.gif")));
        icons.put("zoomall16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/ZoomAll16.gif")));
        icons.put("zoomall24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/ZoomAll24.gif")));
        onePage.putValue(AbstractAction.SMALL_ICON, getIcon("zoomstd16Icon"));
        allRemainingPages.putValue(AbstractAction.SMALL_ICON, getIcon("zoomall16Icon"));
        lessPages.putValue(AbstractAction.SMALL_ICON, getIcon("zoomout16Icon"));
        morePages.putValue(AbstractAction.SMALL_ICON, getIcon("zoomin16Icon"));
        icons.put("stepl16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/navigation/Back16.gif")));
        icons.put("stepl24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/navigation/Back24.gif")));
        icons.put("stepr16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/navigation/Forward16.gif")));
        icons.put("stepr24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/navigation/Forward24.gif")));
        addToPlayList.putValue(AbstractAction.SMALL_ICON, getIcon("stepr16Icon"));
        removeFromPlayList.putValue(AbstractAction.SMALL_ICON, getIcon("stepl16Icon"));
        icons.put("setdir24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/SetDir24.gif")));
        icons.put("setdir24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Preferences24.gif")));
        icons.put("copy16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Copy16.gif")));
        icons.put("copy24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Copy24.gif")));
        icons.put("pasteMelody16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Paste16.gif")));
        icons.put("pasteMelody24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Paste24.gif")));
        icons.put("paste16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/PasteNew16.gif")));
        icons.put("paste24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/PasteNew24.gif")));
        icons.put("insertb16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/table/RowInsertBefore16.gif")));
        icons.put("insertb24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/table/RowInsertBefore24.gif")));
        icons.put("inserta16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/table/RowInsertAfter16.gif")));
        icons.put("inserta24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/table/RowInsertAfter24.gif")));
        icons.put("split16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Split16.gif")));
        icons.put("split24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Split24.gif")));
        icons.put("join16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Join16.gif")));
        icons.put("join24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Join24.gif")));
        icons.put("delete16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Delete16.gif")));
        icons.put("delete24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Delete24.gif")));
        icons.put("removeSyllable16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/RemoveSyllable16.gif")));
        icons.put("removeSyllable24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/RemoveSyllable24.gif")));
        icons.put("bookmarks24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Bookmarks24.gif")));
        // icons.put("selectall16Icon", new ImageIcon(getClass().getResource("/yass/SelectAll16.gif")));
        // icons.put("selectall24Icon", new ImageIcon(getClass().getResource("/yass/SelectAll24.gif")));
        icons.put("undo16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Undo16.gif")));
        icons.put("undo24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Undo24.gif")));
        icons.put("redo16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Redo16.gif")));
        icons.put("redo24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Redo24.gif")));
        icons.put("snapshot24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Snapshot24.gif")));
        icons.put("snapshot16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Snapshot16.gif")));
        icons.put("gap16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Gap16.gif")));
        icons.put("gap24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Gap24.gif")));
        icons.put("help16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Help16.gif")));
        icons.put("help24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Help24.gif")));
        copyRows.putValue(AbstractAction.SMALL_ICON, getIcon("copy16Icon"));
        pasteRows.putValue(AbstractAction.SMALL_ICON, getIcon("pasteMelody16Icon"));
        pasteNotes.putValue(AbstractAction.SMALL_ICON, getIcon("paste16Icon"));
        joinRows.putValue(AbstractAction.SMALL_ICON, getIcon("join16Icon"));
        splitRows.putValue(AbstractAction.SMALL_ICON, getIcon("split16Icon"));
        removeRows.putValue(AbstractAction.SMALL_ICON, getIcon("removeSyllable16Icon"));
        showLyricsStart.putValue(AbstractAction.SMALL_ICON, getIcon("gap16Icon"));
        undo.putValue(AbstractAction.SMALL_ICON, getIcon("undo16Icon"));
        redo.putValue(AbstractAction.SMALL_ICON, getIcon("redo16Icon"));
        openSongFolder.putValue(AbstractAction.SMALL_ICON, getIcon("open16Icon"));
        refreshLibrary.putValue(AbstractAction.SMALL_ICON, getIcon("refresh16Icon"));
        saveLibrary.putValue(AbstractAction.SMALL_ICON, getIcon("save16Icon"));
        newFile.putValue(AbstractAction.SMALL_ICON, getIcon("new16Icon"));
        // importFiles.putValue(AbstractAction.SMALL_ICON, getIcon("import16Icon"));
        printLibrary.putValue(AbstractAction.SMALL_ICON, getIcon("print16Icon"));
        openSongFromLibrary.putValue(AbstractAction.SMALL_ICON, getIcon("edit16Icon"));
        removeSong.putValue(AbstractAction.SMALL_ICON, getIcon("delete16Icon"));
        undoAllLibraryChanges.putValue(AbstractAction.SMALL_ICON, getIcon("undo16Icon"));
        showCopiedRows.putValue(AbstractAction.SMALL_ICON, getIcon("snapshot16Icon"));
        // selectLine.putValue(AbstractAction.SMALL_ICON, getIcon("selectall16Icon"));
        // filterLibrary.putValue(AbstractAction.SMALL_ICON, getIcon("find16Icon"));
        icons.put("auto16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/development/Application16.gif")));
        icons.put("auto24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/development/Application24.gif")));
        icons.put("pref16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Preferences16.gif")));
        icons.put("pref24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Preferences24.gif")));
        showOptions.putValue(AbstractAction.SMALL_ICON, getIcon("pref16Icon"));
        icons.put("playvis16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/PlayVisible16.gif")));
        icons.put("playvis24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/PlayVisible24.gif")));
        icons.put("playfrozen16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/PlayFrozen16.gif")));
        icons.put("playfrozen24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/PlayFrozen24.gif")));
        icons.put("playpage16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/PlayPage16.gif")));
        playPage.putValue(AbstractAction.SMALL_ICON, getIcon("playpage16Icon"));
        icons.put("playbefore16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/PlayBefore16.gif")));
        playBefore.putValue(AbstractAction.SMALL_ICON, getIcon("playbefore16Icon"));
        icons.put("playnext16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/PlayNext16.gif")));
        playNext.putValue(AbstractAction.SMALL_ICON, getIcon("playnext16Icon"));
        icons.put("record16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Record16.gif")));
        icons.put("record24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Record24.gif")));
        recordSelection.putValue(AbstractAction.SMALL_ICON, getIcon("record16Icon"));
        icons.put("correct24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Correct24.gif")));
        icons.put("correct16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Correct16.gif")));
        showErrors.putValue(AbstractAction.SMALL_ICON, getIcon("correct16Icon"));
        icons.put("nextBreak24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/NextBreak24.gif")));
        icons.put("prevBreak24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/PrevBreak24.gif")));
        icons.put("rollLeft24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/RollLeft24.gif")));
        icons.put("rollRight24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/RollRight24.gif")));
        icons.put("rollLeft16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/RollLeft16.gif")));
        icons.put("rollRight16Icon", new ImageIcon(getClass().getResource("/yass/resources/img/RollRight16.gif")));
        rollLeft.putValue(AbstractAction.SMALL_ICON, getIcon("rollLeft16Icon"));
        rollRight.putValue(AbstractAction.SMALL_ICON, getIcon("rollRight16Icon"));
        editLyrics.putValue(AbstractAction.SMALL_ICON, getIcon("edit16Icon"));
        playFrozen.putValue(AbstractAction.SMALL_ICON, getIcon("playfrozen16Icon"));
        removeRowsWithLyrics.putValue(AbstractAction.SMALL_ICON, getIcon("delete16Icon"));
        icons.put("noalign24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/AlignBottom24.gif")));
        icons.put("align24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/AlignCenter24.gif")));
        icons.put("alignleft16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/text/AlignLeft16.gif")));
        icons.put("alignleft24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/text/AlignLeft24.gif")));
        icons.put("midi24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Midi24.gif")));
        icons.put("nomidi24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/NoMidi24.gif")));
        icons.put("mute24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/Mute24.gif")));
        icons.put("nomute24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/NoMute24.gif")));
        icons.put("speedone24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/SpeedOne24.gif")));
        icons.put("speedtwo24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/SpeedTwo24.gif")));
        icons.put("speedthree24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/SpeedThree24.gif")));
        icons.put("speedfour24Icon", new ImageIcon(getClass().getResource("/yass/resources/img/SpeedFour24.gif")));
        icons.put("info16Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Information16.gif")));
        icons.put("info24Icon", new ImageIcon(getClass().getResource("/yass/resources/toolbarButtonGraphics/general/Information24.gif")));
        enableLyrics.putValue(AbstractAction.SMALL_ICON, getIcon("lyrics16Icon"));
        showPlaylistMenu.putValue(AbstractAction.SMALL_ICON, getIcon("playlist16Icon"));
        showSongInfo.putValue(AbstractAction.SMALL_ICON, getIcon("info16Icon"));
        detailLibrary.putValue(AbstractAction.SMALL_ICON, getIcon("notiles16Icon"));
        showSongInfoBackground.putValue(AbstractAction.SMALL_ICON, getIcon("empty16Icon"));

        updateActions();
    }


    public JMenuBar createEditMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu(I18.get("edit_file"));
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);
        menu.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                interruptPlay();
            }
        });

        menu.add(openFile);
        menu.add(openFolder);
        menu.add(saveAll);
        menu.add(reloadAll);
        menu.addSeparator();
        menu.add(openTrack);
        menu.add(renameTrack);
        menu.add(exchangeTracks);
        menu.add(saveTrack);
        menu.add(saveTrackAs);
        menu.add(saveDuetAs);
        menu.add(mergeTracks);
        menu.add(deleteTrack);
        menu.add(closeTrack);
        menu.add(closeAll);
        menu.addSeparator();
        menu.add(gotoLibrary);
        menu.addSeparator();
        menu.add(exit);

        menu = new JMenu(I18.get("edit_edit"));
        menu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(menu);
        menu.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                interruptPlay();
            }
        });
        menu.add(undo);
        menu.add(redo);
        menu.addSeparator();
        menu.add(selectLine);
        menu.add(selectAll);
        menu.add(selectNextBeat);
        menu.add(selectPrevBeat);
        menu.addSeparator();
        menu.add(decLeft);
        menu.add(incLeft);
        menu.add(decRight);
        menu.add(incRight);
        menu.add(shiftLeft);
        menu.add(shiftRight);
        menu.add(shiftLeftRemainder);
        menu.add(shiftRightRemainder);
        menu.add(incHeight);
        menu.add(incHeightOctave);
        menu.add(decHeight);
        menu.add(decHeightOctave);
        menu.addSeparator();
        menu.add(copyRows);
        menu.add(pasteRows);
        menu.add(pasteNotes);
        menu.add(showCopyCBI = new JCheckBoxMenuItem(showCopiedRows));
        showCopyCBI.setState(false);
        menu.addSeparator();
        menu.add(removeRowsWithLyrics);
        menu.addSeparator();
        menu.add(showStartEnd);

        startSpinner = new TimeSpinner(I18.get("mpop_audio_start"), 0, 10000);
        startSpinner.getSpinner().setFocusable(false);
        startSpinner.getSpinner().addChangeListener(e -> {
            if (!isUpdating) {
                setStart(startSpinner.getTime());
            }
        });

        endSpinner = new TimeSpinner(I18.get("mpop_audio_end"), 10000, 10000);
        endSpinner.getSpinner().setFocusable(false);
        endSpinner.getSpinner().addChangeListener(e -> {
            if (!isUpdating) {
                setEnd(endSpinner.getTime());
            }
        });

        menu.add(showLyricsStart);

        gapSpinner = new TimeSpinner(I18.get("mpop_gap"), 0, 10000);
        gapSpinner.getSpinner().setFocusable(false);
        gapSpinner.getSpinner().addChangeListener(e -> {
            if (!isUpdating) {
                setGap(gapSpinner.getTime());
            }
        });

        menu.add(showVideoGap);

        menu = new JMenu(I18.get("edit_play"));
        menu.setMnemonic(KeyEvent.VK_P);
        menuBar.add(menu);
        menu.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                interruptPlay();
            }
        });
        menu.add(playSelection);
        menu.add(playBefore);
        menu.add(playNext);
        menu.add(playPage);
        menu.add(playFrozen);
        menu.add(interruptPlay);
        if (YassVideoUtils.useFOBS) {
            menu.addSeparator();
            menu.add(playAll);
            menu.add(playAllFromHere);
            menu.add(playAllVideoCBI = new JCheckBoxMenuItem(enableVideoPreview));
        }
        menu.addSeparator();
        menu.add(midiCBI = new JCheckBoxMenuItem(enableMidi));
        menu.add(audioCBI = new JCheckBoxMenuItem(enableAudio));
        menu.add(clicksCBI = new JCheckBoxMenuItem(enableClicks));
        menu.add(micCBI = new JCheckBoxMenuItem(enableMic));
        audioCBI.setState(true);
        clicksCBI.setState(true);
        micCBI.setState(true);

        menu = new JMenu(I18.get("edit_view"));
        menu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(menu);
        menu.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                interruptPlay();
            }
        });
        menu.add(prevPage);
        menu.add(nextPage);
        menu.addSeparator();
        menu.add(onePage);
        menu.add(lessPages);
        menu.add(morePages);
        menu.add(allRemainingPages);
        menu.add(viewAll);
        menu.add(alignCBI = new JCheckBoxMenuItem(absolute));
        alignCBI.setState(true);

        ButtonGroup group = new ButtonGroup();
        showNothingToggle = new JRadioButtonMenuItem(showNothing);
        showBackgroundToggle = new JRadioButtonMenuItem(showBackground);
        showVideoToggle = new JRadioButtonMenuItem(showVideo);
        // menu.addSeparator();
        // menu.add(showNothingToggle = new JRadioButtonMenuItem(showNothing));
        // menu.add(showBackgroundToggle = new
        // JRadioButtonMenuItem(showBackground));
        // menu.add(showVideoToggle = new JRadioButtonMenuItem(showVideo));
        group.add(showNothingToggle);
        group.add(showBackgroundToggle);
        group.add(showVideoToggle);
        showNothingToggle.setSelected(true);

        menu.addSeparator();
        //menu.add(showErrors);
        menu.add(showTable);
        menu.addSeparator();
        menu.add(darkmode);
        menu.add(fullscreen);

        menu = new JMenu(I18.get("edit_lyrics"));
        menu.setMnemonic(KeyEvent.VK_L);
        menuBar.add(menu);
        menu.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                interruptPlay();
            }
        });
        menu.add(editLyrics);
        menu.addSeparator();
        menu.add(insertNote);
        menu.add(splitRows);
        menu.add(joinRows);
        menu.add(removeRows);
        menu.addSeparator();
        menu.add(rollLeft);
        menu.add(rollRight);
        menu.addSeparator();
        menu.add(togglePageBreak);
        menu.addSeparator();
        menu.add(golden);
        menu.add(rapgolden);
        menu.add(rap);
        menu.add(freestyle);
        menu.addSeparator();
        menu.add(minus);
        menu.add(space);
        menu.addSeparator();

        menu.add(reHyphenate);
        menu.add(findLyrics);
        menu.add(spellLyrics);

        menu = new JMenu(I18.get("edit_extras"));
        menu.setMnemonic(KeyEvent.VK_X);
        menuBar.add(menu);
        menu.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                interruptPlay();
            }
        });
        menu.add(recordSelection);
        menu.addSeparator();
        menu.add(autoCorrectPageBreaks);
        menu.add(autoCorrectSpacing);
        menu.add(autoCorrectTransposed);

        menu.addSeparator();
        menu.add(showOptions);

        menu = new JMenu(I18.get("edit_help"));
        menu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menu);
        menu.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                interruptPlay();
            }
        });
        menu.add(showHelp);
        menu.add(showOnlineHelp);
        menu.add(showAbout);
        return menuBar;
    }

    public JMenuBar createLibraryMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu(I18.get("lib_file"));
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);
        menu.add(openSongFromLibrary);
        menu.add(openFileFromLibrary);
        menu.add(openFolderFromLibrary);
        menu.add(editRecent);
        menu.addSeparator();
        menu.add(newFile);
        // menu.add(importFiles);
        menu.add(printLibrary);
        menu.add(exportMetadata);
        menu.add(importMetadata);
        // menu.add(importText);
        menu.addSeparator();
        menu.add(setLibrary);
        menu.add(refreshLibrary);
        menu.add(thumbnailLibrary);
        menu.add(closeLibrary);
        menu.addSeparator();
        menu.add(exit);

        menu = new JMenu(I18.get("lib_edit"));
        menu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(menu);
        menu.add(filterLibrary);
        menu.addSeparator();
        menu.add(selectAllSongs);
        menu.addSeparator();

        JMenu menu2 = new JMenu(I18.get("lib_set"));
        menu2.add(setArtist);
        menu2.add(setTitle);
        menu2.add(songList.getGenreMenu());
        menu2.add(songList.getEditionMenu());
        menu2.add(songList.getLanguageMenu());
        menu2.add(setYear);
        menu2.add(setAlbum);
        menu2.add(setLength);
        menu2.add(setID);
        menu.add(menu2);
        menu.add(setPreviewStart);
        menu.add(setMedleyStartEnd);

        encMenu = new JMenu(I18.get("lib_encoding"));
        encMenu.add(setEncodingUTF8);
        encMenu.add(setEncodingANSI);
        menu.add(encMenu);

        menu.addSeparator();
        menu2 = new JMenu(I18.get("lib_copy"));
        menu2.add(copyLyricsSongInfo);
        menu2.add(copyCoverSongInfo);
        menu2.add(copyBackgroundSongInfo);
        menu2.add(copyVideoSongInfo);
        menu.add(menu2);
        menu.add(pasteSongInfo);
        menu.addSeparator();
        menu.add(saveLibrary);
        menu.add(undoAllLibraryChanges);
        menu.addSeparator();
        menu.add(removeSong);

        menu = new JMenu(I18.get("lib_view"));
        menu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(menu);
        menu.add(showPlaylistMenu);
        menu.add(enableLyrics);
        menu.add(showSongInfo);
        menu.add(detailLibrary);
        menu.add(showSongInfoBackground);
        menu.add(songList.getSortByMenu());
        menu.addSeparator();
        menu.add(openSongFolder);
        menu.add(showLibTable);
        menu.addSeparator();
        menu.add(fullscreen);

        menu = new JMenu(I18.get("lib_play"));
        menu.setMnemonic(KeyEvent.VK_S);
        menuBar.add(menu);
        menu.add(playSong);
        menu.add(stopPlaySong);
        menu.addSeparator();
        menu.add(addToPlayList);
        menu.add(removeFromPlayList);
        menu.addSeparator();
        menu.add(savePlayList);
        menu.add(savePlayListAs);
        menu.add(refreshPlayList);
        menu.add(removePlayList);

        menu = new JMenu(I18.get("lib_extras"));
        menu.setMnemonic(KeyEvent.VK_X);
        menu.add(testMic);
        menu.addSeparator();
        menu.add(showOptions);
        menuBar.add(menu);

        menu = new JMenu(I18.get("lib_help"));
        menu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menu);
        menu.add(showHelp);
        menu.add(showOnlineHelp);
        menu.add(showAbout);
        return menuBar;
    }


    public JComponent createFileEditToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_edit"));
        editTools = t;

        boolean floatable = prop.getProperty("floatable").equals("true");
        t.setFloatable(floatable);

        JButton b;
        t.add(b = new JButton());
        b.setAction(gotoLibrary);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("list24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.add(b = new JButton());
        b.setAction(saveAll);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("save24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.addSeparator();

        t.add(midiButton = new JToggleButton());
        midiButton.setAction(enableMidi);
        midiButton.setToolTipText(midiButton.getText());
        midiButton.setText("");
        midiButton.setIcon(getIcon("nomidi24Icon"));
        midiButton.setSelectedIcon(getIcon("midi24Icon"));
        midiButton.setFocusable(false);
        midiButton.setOpaque(false);

        t.add(speedButton = new JToggleButton());
        speedButton.setAction(setPlayTimebase);
        speedButton.setToolTipText(speedButton.getText());
        speedButton.setText("");
        speedButton.setIcon(getIcon("speedone24Icon"));
        speedButton.setSelected(playTimebase != 1);
        speedButton.setFocusable(false);
        speedButton.setOpaque(false);

        mp3Button = new JToggleButton();
        // t.add(...);
        mp3Button.setAction(enableAudio);
        mp3Button.setToolTipText(mp3Button.getText());
        mp3Button.setText("");
        mp3Button.setIcon(getIcon("mute24Icon"));
        mp3Button.setSelectedIcon(getIcon("nomute24Icon"));
        mp3Button.setFocusable(false);
        mp3Button.setSelected(true);
        mp3Button.setOpaque(false);

        videoButton = new JToggleButton();
        // t.add(..);
        videoButton.setAction(toggleVideo);
        videoButton.setToolTipText(videoButton.getText());
        videoButton.setText("");
        videoButton.setIcon(getIcon("movie24Icon"));
        // videoButton.setSelectedIcon(getIcon("movie24Icon"));
        videoButton.setFocusable(false);
        videoButton.setOpaque(false);

        t.addSeparator();

        t.add(b = new JButton());
        b.setAction(undo);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("undo24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.add(b = new JButton());
        b.setAction(redo);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("redo24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.addSeparator();

        /*
         * t.add(b = new JButton()); b.setAction(selectLine);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("selectall24Icon")); b.setFocusable(false);
         */
        t.add(b = new JButton());
        b.setAction(copyRows);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("copy24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.add(b = new JButton());
        b.setAction(pasteRows);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("pasteMelody24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.add(snapshotButton = new JToggleButton());
        snapshotButton.setAction(showCopiedRows);
        snapshotButton.setToolTipText(snapshotButton.getText());
        snapshotButton.setText("");
        snapshotButton.setIcon(getIcon("snapshot24Icon"));
        snapshotButton.setFocusable(false);
        snapshotButton.setOpaque(false);

        t.add(b = new JButton());
        b.setAction(playFrozen);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("playfrozen24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.addSeparator();

        t.add(b = new JButton());
        b.setAction(insertNote);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("insertnote24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.add(b = new JButton());
        b.setAction(splitRows);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("split24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.add(b = new JButton());
        b.setAction(joinRows);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("join24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.add(b = new JButton());
        b.setAction(removeRows);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("removeSyllable24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.addSeparator();

        t.add(b = new JButton());
        b.setAction(rollLeft);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("rollLeft24Icon"));
        b.setFocusable(false);
        t.add(b = new JButton());
        b.setAction(rollRight);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("rollRight24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.addSeparator();

        t.add(b = new JButton());
        b.setAction(togglePageBreak);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("pagebreak24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        /*
         * t.add(b = new JButton()); b.setAction(space);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("space24Icon")); b.setFocusable(false); t.add(b =
         * new JButton()); b.setAction(minus); b.setToolTipText(b.getText());
         * b.setText(""); b.setIcon(getIcon("minus24Icon"));
         * b.setFocusable(false);
         */
        t.addSeparator();

        t.add(b = new JButton());
        b.setAction(golden);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("golden24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.add(b = new JButton());
        b.setAction(rapgolden);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("rapgolden24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.add(b = new JButton());
        b.setAction(rap);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("rap24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.add(b = new JButton());
        b.setAction(freestyle);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("freestyle24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.addSeparator();

        t.add(b = new JButton());
        b.setAction(editLyrics);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("edit24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.add(b = new JButton());
        b.setAction(onePage);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("zoomstd24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        t.add(b = new JButton());
        b.setAction(morePages);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("zoomin24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);

        return t;
    }


    public JComponent createPlayListToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_playlist"));
        t.setFloatable(false);

        JButton b;

        t.add(b = new JButton());
        b.setAction(savePlayList);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("save24Icon"));
        b.setFocusable(false);

        /*
         * t.add(b = new JButton()); b.setAction(savePlayListAs);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("saveas24Icon")); b.setFocusable(false); t.add(b =
         * new JButton()); b.setAction(refreshPlayList);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("refresh24Icon")); b.setFocusable(false);
         * t.addSeparator(); t.add(b = new JButton());
         * b.setAction(removePlayList); b.setToolTipText(b.getText());
         * b.setText(""); b.setIcon(getIcon("delete24Icon"));
         * b.setFocusable(false); t.addSeparator();
         */
        t.add(createPlayListBox());
        return t;
    }


    public JComboBox<String> createPlayListBox() {
        plBox = new JComboBox<>();
        plBox.setToolTipText(I18.get("tool_playlist_box"));
        plBoxTips = new Vector<>();

        plBox.addActionListener(plBoxListener = new PlayListBoxListener());
        plBox.addItemListener(plBoxListener);
        plBox.setRenderer(new PlayListBoxRenderer());
        updatePlayListBox();
        return plBox;
    }

    /**
     * Gets the playListBox attribute of the YassActions object
     *
     * @return The playListBox value
     */
    public JComboBox<String> getPlayListBox() {
        return plBox;
    }


    public void updatePlayListBox() {
        plBox.removeActionListener(plBoxListener);
        plBox.removeItemListener(plBoxListener);

        plBox.removeAllItems();
        plBoxTips.clear();

        String pdir = prop.getProperty("playlist-directory");
        plBoxTips.addElement(pdir);
        plBox.addItem(I18.get("tool_playlist_new"));

        Vector<?> v = playList.getPlayLists();
        if (v == null) {
            return;
        }

        String rec = prop.getProperty("recent-playlist");
        if (rec == null) {
            rec = "";
        }

        int k = -1;

        int i = 0;
        for (Enumeration<?> en = v.elements(); en.hasMoreElements(); ) {
            YassPlayListModel pl = (YassPlayListModel) en.nextElement();
            String plt = pl.getFileName();
            if (plt == null) {
                plt = pdir;
            }
            plBoxTips.addElement(plt);
            plBox.addItem(pl.getName());
            if (pl.getName().equals(rec)) {
                k = i;
            }
            i++;
        }

        plBox.setSelectedIndex(k + 1);
        plBox.addActionListener(plBoxListener);
        plBox.addItemListener(plBoxListener);
        plBox.validate();

        removePlayList.setEnabled(k >= 0);
        refreshPlayList.setEnabled(k >= 0);
        savePlayList.setEnabled(k >= 0);
        savePlayListAs.setEnabled(k >= 0);

        storePlayListCache();
        refreshGroups();
        updatePlayListCursor();
    }


    public synchronized void storePlayListCache() {
        Vector<?> lists = playList.getPlayLists();
        if (lists != null) {
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

            PrintWriter outputStream = null;
            FileWriter fw = null;
            try {
                outputStream = new PrintWriter(fw = new FileWriter(plcache));
                for (Enumeration<?> en = lists.elements(); en
                        .hasMoreElements(); ) {
                    YassPlayListModel pl = (YassPlayListModel) en.nextElement();
                    String plt = pl.getFileName();
                    outputStream.println(plt);
                }
            } catch (Exception e) {
                System.out.println("Playlist Cache Write Error:" + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (fw != null) {
                        fw.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updatePlayListCursor() {
        int n = playList.getList().getRowCount();
        removeFromPlayList.setEnabled(n > 0);

        movePlayListUp.setEnabled(n > 1);
        movePlayListDown.setEnabled(n > 1);
    }

    private void clearFilter() {
        filterEditor.setText(I18.get("tool_lib_find_empty"));
        filterEditor.setForeground(Color.gray);
        songInfo.setBold(null);
    }

    public void createFilterBox() {
        filter = new JComboBox<>();
        filter.setToolTipText(I18.get("tool_lib_find_box"));
        filter.setEditable(true);
        filter.setEditable(true);
        filter.addItem(I18.get("tool_lib_find_all"));
        filter.addItem("[SC]");
        filter.addItem(I18.get("tool_lib_find_not") + " [SC]");
        filter.addItem("[VIDEO]");
        filter.addItem(I18.get("tool_lib_find_not") + " [VIDEO]");

        filterEditor = new JTextField(10);
        filterEditor.setText(I18.get("tool_lib_find_empty"));
        filterEditor.setForeground(Color.gray);

        filterAll = new JToggleButton();
        filterAll.setAction(enableLyrics);
        filterAll.setToolTipText(filterAll.getText());
        filterAll.setText("");
        filterAll.setIcon(getIcon("lyrics24Icon"));
        filterAll.setFocusable(false);
        filterAll.setSelected(true);

        filterEditor.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                String s = filterEditor.getText();
                if (s.equals(I18.get("tool_lib_find_empty"))) {
                    filterEditor.setText("");
                }
                filterEditor.setForeground(Color.black);
                filterEditor.selectAll();
            }

            public void focusLost(FocusEvent e) {
                String s = filterEditor.getText();
                if (s.equals("")) {
                    filterEditor.setText(I18.get("tool_lib_find_empty"));
                }
                filterEditor.setForeground(Color.gray);
            }
        });
        filterEditor.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String s = filterEditor.getText();
                if (s.equals(I18.get("tool_lib_find_empty"))) {
                    filterEditor.setText("");
                }
            }
        });
        filterEditor.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                keyTyped(e);
            }

            public void keyReleased(KeyEvent e) {
                keyTyped(e);
            }

            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String s = filterEditor.getText();
                    if (s.equals(I18.get("tool_lib_find_empty"))) {
                        s = "";
                    }
                    filterEditor.setForeground(Color.gray);
                    songList.filterLyrics(filterAll.isSelected());
                    songInfo.setBold(s);
                    songList.filter(s);
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    clearFilter();
                    filterLibrary();
                    return;
                }
                String s = filterEditor.getText();
                if (s.equals(I18.get("tool_lib_find_empty"))) {
                    s = "";
                    filterEditor.setForeground(Color.gray);
                } else {
                    filterEditor.setForeground(Color.black);
                }

                if (s.length() > 2) {
                    songList.filterLyrics(filterAll.isSelected());
                    songInfo.setBold(s);
                    songList.filter(s);
                }
            }
        });
        filter.addActionListener(e -> {
            String s = filterEditor.getText();
            if (s.equals(I18.get("tool_lib_find_empty"))) {
                s = "";
            }
            songList.filterLyrics(filterAll.isSelected());
            songInfo.setBold(s);
            songList.filter(s);
        });
    }

    private boolean isFilterEditing() {
        // JTextComponent c = (JTextComponent)
        // filter.getEditor().getEditorComponent();
        return filterEditor.hasFocus();
        // return c.hasFocus();
    }

    JComboBox<String> createGroupsBox() {
        groupsBox = new JComboBox<>(yass.filter.YassFilter.getAllLabels());
        groupsBox
                .setMaximumRowCount(Math.min(20, groupsBox.getItemCount() + 1));
        groupsBox.setEditable(false);
        groupsBox.setRenderer(new AlignedListCellRenderer());
        groupsBox.setFont(groupsFont);
        groupsBox.setForeground(Color.gray);
        return groupsBox;
    }

    JComponent createTitleToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_title"));
        t.setFloatable(false);

        /*
         * t.add(b = new JButton()); b.setAction(setTitle);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("setTitle24Icon")); b.setFocusable(false); t.add(b
         * = new JButton()); b.setAction(setArtist);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("setArtist24Icon")); b.setFocusable(false);
         */
        return t;
    }

    JComponent createArtistToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_artist"));
        t.setFloatable(false);

        /*
         * t.add(b = new JButton()); b.setAction(setTitle);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("setTitle24Icon")); b.setFocusable(false); t.add(b
         * = new JButton()); b.setAction(setArtist);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("setArtist24Icon")); b.setFocusable(false);
         */
        return t;
    }

    JComponent createGenreToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_genre"));
        t.setFloatable(false);
        /*
         * t.add(b = new JButton()); b.setAction(setTitle);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("setTitle24Icon")); b.setFocusable(false); t.add(b
         * = new JButton()); b.setAction(setArtist);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("setArtist24Icon")); b.setFocusable(false);
         * t.add(songList.getGenreMenu()); t.add(Box.createHorizontalGlue());
         */
        return t;
    }

    JComponent createEditionToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_edition"));
        t.setFloatable(false);
        JButton b;
        /*
         * t.add(b = new JButton()); b.setAction(setTitle);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("setTitle24Icon")); b.setFocusable(false); t.add(b
         * = new JButton()); b.setAction(setArtist);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("setArtist24Icon")); b.setFocusable(false);
         */
        t.add(b = new JButton());
        b.setAction(newEdition);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("newEdition24Icon"));
        b.setFocusable(false);

        // t.add(songList.getEditionMenu());
        // t.add(Box.createHorizontalGlue());

        return t;
    }

    JComponent createYearToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_year"));
        t.setFloatable(false);

        JButton b;
        t.add(b = new JButton());
        b.setAction(correctYear);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("correctyear24Icon"));
        b.setFocusable(false);
        return t;
    }

    JComponent createLengthToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_length"));
        t.setFloatable(false);

        JButton b;
        t.add(b = new JButton());
        b.setAction(correctLength);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("correctlength24Icon"));
        b.setFocusable(false);
        return t;
    }


    JComponent createAlbumToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_album"));
        t.setFloatable(false);

        JButton b;
        t.add(b = new JButton());
        b.setAction(correctAlbum);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("correctalbum24Icon"));
        b.setFocusable(false);
        return t;
    }

    JComponent createPlaylistToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_playlist"));
        t.setFloatable(false);

        JButton b;
        t.add(b = new JButton());
        b.setAction(savePlayList);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("save24Icon"));
        b.setFocusable(false);

        t.add(b = new JButton());
        b.setAction(savePlayListAs);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("saveas24Icon"));
        b.setFocusable(false);

        t.add(b = new JButton());
        b.setAction(refreshPlayList);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("refresh24Icon"));
        b.setFocusable(false);

        t.add(b = new JButton());
        b.setAction(removePlayList);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("delete24Icon"));
        b.setFocusable(false);

        return t;
    }

    JComponent createDuetsToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_duets"));
        t.setFloatable(false);
        return t;
    }

    JComponent createLanguageToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_language"));
        t.setFloatable(false);

        /*
         * t.add(b = new JButton()); b.setAction(setTitle);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("setTitle24Icon")); b.setFocusable(false); t.add(b
         * = new JButton()); b.setAction(setArtist);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("setArtist24Icon")); b.setFocusable(false);
         * t.add(songList.getLanguageMenu()); t.add(Box.createHorizontalGlue());
         */
        return t;
    }

    JComponent createFolderToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_folder"));
        t.setFloatable(false);

        JButton b;
        /*
         * t.add(b = new JButton()); b.setAction(setTitle);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("setTitle24Icon")); b.setFocusable(false); t.add(b
         * = new JButton()); b.setAction(setArtist);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("setArtist24Icon")); b.setFocusable(false);
         */
        t.add(b = new JButton());
        b.setAction(newFolder);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("newFolder24Icon"));
        b.setFocusable(false);

        t.add(b = new JButton());
        b.setAction(renameFolder);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("renameFolder24Icon"));
        b.setFocusable(false);

        return t;
    }

    JComponent createErrorToolbar() {
        JToolBar t = new JToolBar(I18.get("mpop_errors"));
        t.setFloatable(false);
        JButton b;
        t.add(b = new JButton() {
            @Override
            protected void paintComponent(Graphics g) { // quickfix
                g.clearRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        });
        b.setAction(autoCorrectTransposed);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("correcttransposed24Icon"));
        b.setFocusable(false);
        b.setOpaque(true);
        correctTransposedButton = b;

        t.add(b = new JButton() {
            @Override
            protected void paintComponent(Graphics g) { // quickfix
                g.clearRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        });
        b.setAction(autoCorrectPageBreaks);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("correctpagebreak24Icon"));
        b.setFocusable(false);
        b.setOpaque(true);
        correctPageBreakButton = b;

        t.add(b = new JButton() {
            @Override
            protected void paintComponent(Graphics g) { // quickfix
                g.clearRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        });
        b.setAction(autoCorrectSpacing);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("correcttext24Icon"));
        b.setFocusable(false);
        b.setOpaque(true);
        correctSpacingButton = b;
        correctSpacingButton.setRolloverEnabled(true);

        t.add(b = new JButton());
        b.setAction(showOnlineHelpErrors);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("help24Icon"));
        b.setFocusable(false);
        b.setOpaque(false);
        return t;
    }

    JComponent createSongListToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_lib"));
        t.setFloatable(false);
        JButton b;
        t.add(b = new JButton());
        b.setAction(saveLibrary);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("save24Icon"));
        b.setFocusable(false);

        t.add(b = new JButton());
        b.setAction(undoAllLibraryChanges);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("undo24Icon"));
        b.setFocusable(false);

        t.addSeparator();

        t.add(playToggle = new JButton());
        playToggle.setAction(togglePlaySong);
        playToggle.setToolTipText(playToggle.getText());
        playToggle.setText("");
        playToggle.setIcon(getIcon("play24Icon"));
        b.setFocusable(false);

        t.add(b = new JButton());
        b.setAction(openSongFromLibrary);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("edit24Icon"));
        b.setFocusable(false);

        t.add(b = new JButton());
        b.setAction(openSongFolder);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("open24Icon"));
        b.setFocusable(false);

        t.add(b = new JButton());
        b.setAction(removeSong);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("delete24Icon"));
        b.setFocusable(false);

        t.addSeparator();

        t.add(progressBar);

        t.addSeparator();

        t.add(b = new JButton());
        b.setAction(refreshLibrary);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("refresh24Icon"));
        b.setFocusable(false);

        t.addSeparator();

        /*
         * t.add(b = new JButton()); b.setAction(newFile);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("new24Icon")); b.setFocusable(false); t.add(b = new
         * JButton()); b.setAction(importFiles); b.setToolTipText(b.getText());
         * b.setText(""); b.setIcon(getIcon("import24Icon"));
         * b.setFocusable(false); t.add(b = new JButton());
         * b.setAction(printLibrary); b.setToolTipText(b.getText());
         * b.setText(""); b.setIcon(getIcon("print24Icon"));
         * b.setFocusable(false);
         */
        t.addSeparator();

        t.add(playlistToggle = new JToggleButton());
        playlistToggle.setAction(showPlaylist);
        playlistToggle.setToolTipText(playlistToggle.getText());
        playlistToggle.setText("");
        playlistToggle.setIcon(getIcon("playlist24Icon"));
        // detailToggle.setSelectedIcon(getIcon("tiles24Icon"));
        playlistToggle.setFocusable(false);

        t.addSeparator();

        t.add(filterAll);

        t.add(songInfoToggle = new JToggleButton());
        songInfoToggle.setAction(showSongInfo);
        songInfoToggle.setToolTipText(songInfoToggle.getText());
        songInfoToggle.setText("");
        songInfoToggle.setIcon(getIcon("info24Icon"));
        songInfoToggle.setFocusable(false);

        t.add(detailToggle = new JToggleButton());
        detailToggle.setAction(detailLibrary);
        detailToggle.setToolTipText(detailToggle.getText());
        detailToggle.setText("");
        detailToggle.setIcon(getIcon("notiles24Icon"));
        // detailToggle.setSelectedIcon(getIcon("tiles24Icon"));
        detailToggle.setFocusable(false);

        t.add(bgToggle = new JToggleButton());
        bgToggle.setAction(showSongInfoBackground);
        bgToggle.setToolTipText(bgToggle.getText());
        bgToggle.setText("");
        bgToggle.setIcon(getIcon("empty24Icon"));
        // detailToggle.setSelectedIcon(getIcon("tiles24Icon"));
        bgToggle.setFocusable(false);
        return t;
    }

    private final Action enableLyrics = new AbstractAction(I18.get("lib_lyrics_toggle")) {
        public void actionPerformed(ActionEvent e) {
            clearFilter();
            updateSongInfo(enableLyrics);

            songList.requestFocus();
        }
    };


    JComponent createFilterToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_lib"));
        t.setFloatable(false);

        JButton b;
        t.add(Box.createHorizontalGlue());

        t.add(filterEditor);
        t.add(b = new JButton());
        b.setAction(clearFilter);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("clearfind24Icon"));
        b.setFocusable(false);
        /*
         * t.add(b = new JButton()); b.setAction(setLibrary);
         * b.setToolTipText(b.getText()); b.setText("");
         * b.setIcon(getIcon("setdir24Icon")); b.setFocusable(false);
         */
        return t;
    }

    JComponent createErrorsToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_errors"));
        t.setFloatable(false);

        JButton b;
        t.add(b = new JButton());
        b.setAction(correctTagsLibrary);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("correcttags24Icon"));
        b.setFocusable(false);

        t.add(b = new JButton());
        b.setAction(correctFilesLibrary);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("correctfilenames24Icon"));
        b.setFocusable(false);

        t.add(b = new JButton());
        b.setAction(correctPageBreakLibrary);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("correctpagebreak24Icon"));
        b.setFocusable(false);

        t.add(b = new JButton());
        b.setAction(correctTextLibrary);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("correcttext24Icon"));
        b.setFocusable(false);

        t.addSeparator();

        t.add(b = new JButton());
        b.setAction(correctLibrary);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("err24Icon"));
        b.setFocusable(false);

        t.add(b = new JButton());
        b.setAction(showOnlineHelpErrors);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("help24Icon"));
        b.setFocusable(false);
        return t;
    }

    JComponent createFilesToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_files"));
        t.setFloatable(false);

        JButton b;
        t.add(b = new JButton());
        b.setAction(renameFilesLibrary);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("correctfilenames24Icon"));
        b.setFocusable(false);
        return t;
    }
    /*
    public JComponent createSongInfoToolbar() {
        JToolBar t = new JToolBar(I18.get("tool_lib_info"));
        t.setFloatable(false);
        t.setOrientation(SwingConstants.VERTICAL);

        JButton b = null;

        t.add(b = new JButton());
        b.setAction(copySongInfo);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("copy24Icon"));
        b.setFocusable(false);

        t.add(b = new JButton());
        b.setAction(pasteSongInfo);
        b.setToolTipText(b.getText());
        b.setText("");
        b.setIcon(getIcon("paste24Icon"));
        b.setFocusable(false);

        t.add(Box.createVerticalGlue());

        return t;
    }
    */

    public void setProgress(String s) {
        progressBar.setString(s);
        progressBar.setValue(0);

        progressBar.setToolTipText("");
        SwingUtilities.invokeLater(progressBar::repaint);
    }

    public void setProgress(String s, int n) {
        progressBar.setString(s);
        progressBar.setValue(0);
        progressBar.setMaximum(n);

        progressBar.setToolTipText("");
        SwingUtilities.invokeLater(progressBar::repaint);
    }

    public void setProgress(String s, String t) {
        progressBar.setString(s);
        progressBar.setToolTipText(t);
        SwingUtilities.invokeLater(progressBar::repaint);
    }

    public void setProgress(int n) {
        progressBar.setValue(n);
        SwingUtilities.invokeLater(progressBar::repaint);
    }

    public void refreshLibrary() {
        clearFilter();
        playList.refreshWhenLoaded();
        songList.refresh();
        refreshGroups();
        updatePlayListBox();
    }

    public void setPreviewStart() {
        int[] inout = songInfo.getInOut();
        songList.setPreviewStart(inout[0]);
    }

    public void setMedleyStartEnd() {
        int[] inout = songInfo.getInOut();
        songList.setMedleyStartEnd(inout[0], inout[1]);
    }

    private void togglePlaySong() {
        long currentTime = System.currentTimeMillis();
        if (currentTime < lastTogglePressed + 500) {
            return;
        }
        lastTogglePressed = currentTime;

        if (isLibraryPlaying) {
            songList.stopPlaying();
            playToggle.setIcon(getIcon("play24Icon"));
            isLibraryPlaying = false;
        } else {
            int[] inout = songInfo.getInOut();
            songList.startPlaying(inout[0], inout[1]);
            playToggle.setIcon(getIcon("stop24Icon"));
            isLibraryPlaying = true;
        }
    }

    public void playSong() {
        soonStarting = true;
        int[] inout = songInfo.getInOut();
        songList.startPlaying(inout[0], inout[1]);
        playToggle.setIcon(getIcon("stop24Icon"));
        isLibraryPlaying = true;
        soonStarting = false;
    }

    public void stopPlaySong() {
        songList.stopPlaying();
        playToggle.setIcon(getIcon("play24Icon"));
        isLibraryPlaying = false;
    }

    public void setPanels(JFrame f, JPanel main, JComponent libComponent, JComponent songInfoComponent, JComponent songComponent, JComponent playlistComponent, JComponent editComponent, JComponent trackComponent) {
        this.menuHolder = f;
        this.main = main;
        this.libComponent = libComponent;
        this.songComponent = songComponent;
        this.songInfoComponent = songInfoComponent;
        this.playlistComponent = playlistComponent;
        this.editComponent = editComponent;
        this.trackComponent = trackComponent;
    }

    public void updateDetails() {
        boolean onoff = detailToggle.isSelected();
        if (onoff) {
            songBounds = songComponent.getBounds();
            songComponent.setBounds(songBounds.x, songBounds.y, songInfoComponent.getWidth() - 10 - songBounds.x, songInfoComponent.getHeight() - 20);
            songInfoComponent.revalidate();

            songList.setOptions(YassSongList.DETAILS);
        } else {
            if (songBounds != null) {
                int hhlow = songInfoComponent.getHeight() - 20;
                int min = 200;
                int max = 500;
                int sw = songInfoComponent.getWidth() / 4;
                sw = Math.max(min, Math.min(sw, max));
                songComponent.setBounds(songBounds.x, songBounds.y, sw, hhlow);
            }
            songInfoComponent.revalidate();

            songList.setOptions(YassSongList.TILE);
        }
        songInfo.revalidate();
    }

    private void updateSheetProperties() {
        boolean mouseover = prop.getProperty("mouseover").equals("true");
        sheet.setMouseOver(mouseover);

        String s = prop.getProperty("sketching");
        boolean useSketching = s != null && s.equals("true");
        s = prop.getProperty("sketching-playback");
        boolean useSketchingPlayback = s != null && s.equals("true");
        sheet.enableSketching(useSketching, useSketchingPlayback);

        s = prop.getProperty("show-note-length");
        boolean showLength = s != null && s.equals("true");
        sheet.setNoteLengthVisible(showLength);

        s = prop.getProperty("show-note-beat");
        boolean showBeat = s != null && s.equals("true");
        sheet.setNoteBeatVisible(showBeat);

        s = prop.getProperty("show-note-scale");
        boolean showScale = s != null && s.equals("true");
        sheet.setNoteScaleVisible(showScale);

        s = prop.getProperty("show-note-height");
        boolean showHeight = s != null && s.equals("true");
        sheet.setNoteHeightVisible(showHeight);

        s = prop.getProperty("show-note-heightnum");
        boolean showHeightNum = s != null && s.equals("true");
        sheet.setNoteHeightNumVisible(showHeightNum);

        s = prop.getProperty("playback-buttons");
        boolean showPlayerButtons = s != null && s.equals("true");
        sheet.showPlayerButtons(showPlayerButtons);

        s = prop.getProperty("piano-volume");
        try {
            mp3.setPianoVolume(Integer.parseInt(s));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getView() {
        return currentView;
    }

    public void setView(int n) {
        if (n == VIEW_EDIT) {
            songList.stopPlaying();
            playToggle.setIcon(getIcon("play24Icon"));
            isLibraryPlaying = false;
            main.removeAll();
            main.add("Center", editComponent);

            mp3.setPlaybackRenderer(sheet);

            if (editMenu == null) {
                editMenu = createEditMenu();

                sheet.addYassSheetListener(new YassSheetListener() {
                    @Override
                    public void posChanged(YassSheet source, double posMs) {
                    }

                    @Override
                    public void rangeChanged(YassSheet source, int minHeight, int maxHeight, int minBeat, int maxBeat) {
                    }

                    @Override
                    public void propsChanged(YassSheet source) {
                        editMenu.setBackground(sheet.darkMode ? sheet.HI_GRAY_2_DARK_MODE : sheet.HI_GRAY_2);
                        for (Component c : editMenu.getComponents()) {
                            c.setForeground(sheet.darkMode ? sheet.blackDarkMode : sheet.black);
                        }
                    }
                });
                editMenu.setBackground(sheet.darkMode ? sheet.HI_GRAY_2_DARK_MODE : sheet.HI_GRAY_2);
                for (Component c : editMenu.getComponents()) {
                    c.setForeground(sheet.darkMode ? sheet.blackDarkMode : sheet.black);
                }

            }
            menuHolder.setJMenuBar(editMenu);
            currentView = VIEW_EDIT;
        } else if (n == VIEW_LIBRARY) {
            main.removeAll();
            // System.out.println("init view 1");
            main.add("Center", libComponent);

            mp3.setPlaybackRenderer(sheet);

            // System.out.println("init view 2");
            if (libMenu == null) {
                libMenu = createLibraryMenu();
            }
            Window root = SwingUtilities.getWindowAncestor(main);
            if (root instanceof JFrame) {
                ((Frame) root).setTitle(I18.get("yass_title"));
            }
            menuHolder.setJMenuBar(libMenu);
            currentView = VIEW_LIBRARY;
        }

        String recent = prop.getProperty("recent-files");
        editRecent.setEnabled(recent != null);
        menuHolder.validate();

        if (n == VIEW_EDIT) {
            openEditor(false);
            updateSheetProperties();
        } else if (n == VIEW_LIBRARY) {
            YassSong s = songList.getFirstSelectedSong();
            songInfo.setSong(s);
            updateSongInfo(null);
            songList.requestFocus();
        }
        menuHolder.repaint();
        }

    /**
     * Ask user 'cancel' and not lose changes.
     * @return true if should cancel (if not saved or user responded with 'cancel')
     */
    boolean cancelOpen() {
        if (!saveAll.isEnabled())
            return false;
        return JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(tab,
                I18.get("edit_exit_cancel"), I18.get("edit_exit_title"), JOptionPane.OK_CANCEL_OPTION);
    }

    /**
     * Ask user 'cancel' and not lose changes.
     * @return true if should cancel (if not saved or user responded with 'cancel')
     */
    boolean cancelTrack() {
        if (table.isSaved())
            return false;
        return JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(tab,
                I18.get("edit_tracks_exit_cancel"), I18.get("edit_exit_title"), JOptionPane.OK_CANCEL_OPTION);
    }

    /**
     * Ask user 'cancel' and not lose changes.
     * @return true if should cancel (if not saved or user responded with 'cancel')
     */
    int unsavedPlaylist() {
        if (playList == null || !playList.isChanged())
            return JOptionPane.NO_OPTION;
        return JOptionPane.showConfirmDialog(tab, I18.get("playlist_unsaved"), I18.get("edit_exit_title"), JOptionPane.YES_NO_CANCEL_OPTION);
    }
    public boolean isLibraryLoaded() {
        return groups.isEnabled();
    }

    public void setLibraryLoaded(boolean b) {
        groups.setEnabled(b);
        groupsBox.setEnabled(b);
        filterEditor.setEnabled(b);
        filterAll.setEnabled(b);

        showPlaylistMenu.setEnabled(b);
        enableLyrics.setEnabled(b);
        showSongInfo.setEnabled(b);
        detailLibrary.setEnabled(b);
        showSongInfoBackground.setEnabled(b);
        songList.getSortByMenu().setEnabled(b);
        refreshLibrary.setEnabled(true);
        if (playToggle != null) {
            playToggle.setEnabled(b);
        }

        setPreviewStart.setEnabled(b);
        setMedleyStartEnd.setEnabled(b);
        if (encMenu != null) {
            encMenu.setEnabled(b);
            setEncodingUTF8.setEnabled(b);
            setEncodingANSI.setEnabled(b);
        }

        filterLibrary.setEnabled(b);
        setTitle.setEnabled(b);
        setArtist.setEnabled(b);
        setAlbum.setEnabled(b);
        setLength.setEnabled(b);
        setID.setEnabled(b);
        songList.getLanguageMenu().setEnabled(b);
        songList.getEditionMenu().setEnabled(b);
        songList.getGenreMenu().setEnabled(b);
        setYear.setEnabled(b);
        if (songInfoToggle != null) {
            songInfoToggle.setEnabled(b);
        }
        copySongInfo.setEnabled(b);
        copyLyricsSongInfo.setEnabled(b);
        copyCoverSongInfo.setEnabled(b);
        copyBackgroundSongInfo.setEnabled(b);
        copyVideoSongInfo.setEnabled(b);
        pasteSongInfo.setEnabled(b);

        exportMetadata.setEnabled(b);
        importMetadata.setEnabled(b);
        clearFilter.setEnabled(b);

        playSong.setEnabled(b);
        stopPlaySong.setEnabled(b);
        detailLibrary.setEnabled(b);

        showLibTable.setEnabled(b);
        selectAllSongs.setEnabled(b);

        correctLength.setEnabled(b);
        correctAlbum.setEnabled(b);
        correctYear.setEnabled(b);

        openSongFromLibrary.setEnabled(b);
        printLibrary.setEnabled(b);
        thumbnailLibrary.setEnabled(b);
        newFile.setEnabled(b);
        // importFiles.setEnabled(onoff);
        closeLibrary.setEnabled(b);
        openSongFolder.setEnabled(b);
        removeSong.setEnabled(b);
        addToPlayList.setEnabled(b);
        removeFromPlayList.setEnabled(b);
        newEdition.setEnabled(b);
        if (bgToggle != null) {
            bgToggle.setEnabled(b);
        }
        if (detailToggle != null) {
            detailToggle.setEnabled(b);
            detailToggle
                    .setSelected(songList.getOptions() == YassSongList.DETAILS);
        }
        if (playlistToggle != null) {
            playlistToggle.setEnabled(b);
        }
        correctLibrary.setEnabled(b);

        if (plBox != null) {
            plBox.setEnabled(b);
        }
        if (filter != null) {
            filter.setEnabled(b);
        }
    }

    public void updateActions() {
        boolean isOpened = openTables.size() > 0;
        boolean isMoreThanOne = openTables.size() > 1;
        boolean isDuetTrack = table != null && table.getDuetTrack() > 0;
        boolean isTrackSaved = table == null || table.isSaved();
        boolean isAllSaved = true;
        if (isOpened) {
            for (YassTable open : openTables)
                if (!open.isSaved()) {
                    isAllSaved = false;
                    break;
                }
        }
        boolean canUndo = table != null && table.canUndo();
        boolean canRedo = table != null && table.canRedo();

        saveTrack.setEnabled(!isTrackSaved);
        saveAll.setEnabled(!isAllSaved);
        openTrack.setEnabled(isOpened);
        closeTrack.setEnabled(isOpened);
        closeAll.setEnabled(isOpened);
        reloadAll.setEnabled(isOpened);

        undo.setEnabled(canUndo);
        redo.setEnabled(canRedo);

        renameTrack.setEnabled(isDuetTrack);
        exchangeTracks.setEnabled(isDuetTrack);
        mergeTracks.setEnabled(isMoreThanOne);
        saveTrackAs.setEnabled(isOpened);
        saveDuetAs.setEnabled(isDuetTrack);
        deleteTrack.setEnabled(isOpened);

        interruptPlay.setEnabled(isOpened);
        rollLeft.setEnabled(isOpened);
        rollRight.setEnabled(isOpened);
        golden.setEnabled(isOpened);
        freestyle.setEnabled(isOpened);
        rap.setEnabled(isOpened);
        rapgolden.setEnabled(isOpened);
        space.setEnabled(isOpened);
        minus.setEnabled(isOpened);
        // enableHyphenKeys.setEnabled(onoff);
        editLyrics.setEnabled(isOpened);
        homeSong.setEnabled(isOpened);
        findLyrics.setEnabled(isOpened);
        spellLyrics.setEnabled(isOpened);
        nextPage.setEnabled(isOpened);
        prevPage.setEnabled(isOpened);
        morePages.setEnabled(isOpened);
        onePage.setEnabled(isOpened);
        allRemainingPages.setEnabled(isOpened);
        viewAll.setEnabled(isOpened);
        selectAll.setEnabled(isOpened);
        lessPages.setEnabled(isOpened);
        playAll.setEnabled(isOpened);
        playAllFromHere.setEnabled(isOpened);
        playSelection.setEnabled(isOpened);
        playPage.setEnabled(isOpened);
        playFrozen.setEnabled(isOpened);
        playBefore.setEnabled(isOpened);
        playNext.setEnabled(isOpened);
        copyRows.setEnabled(isOpened);
        showCopiedRows.setEnabled(isOpened);
        removeCopy.setEnabled(isOpened);
        pasteRows.setEnabled(isOpened);
        pasteNotes.setEnabled(isOpened);
        splitRows.setEnabled(isOpened);
        joinRows.setEnabled(isOpened);
        togglePageBreak.setEnabled(isOpened);
        insertNote.setEnabled(isOpened);
        removeRows.setEnabled(isOpened);
        removeRowsWithLyrics.setEnabled(isOpened);
        absolute.setEnabled(isOpened);
        showTable.setEnabled(isOpened);
        showBackground.setEnabled(isOpened);
        showVideo.setEnabled(isOpened);
        showNothing.setEnabled(isOpened);
        enableClicks.setEnabled(isOpened);
        enableMic.setEnabled(isOpened);

        if (playAllVideoCBI != null) {
            playAllVideoCBI.setEnabled(isOpened);
        }
        enableMidi.setEnabled(isOpened);
        enableAudio.setEnabled(isOpened);

        toggleVideo.setEnabled(isOpened);

        enableVideoAudio.setEnabled(isOpened);
        recordSelection.setEnabled(isOpened);
        recordAll.setEnabled(isOpened);
        selectLine.setEnabled(isOpened);
        selectAll.setEnabled(isOpened);
        multiply.setEnabled(isOpened);
        divide.setEnabled(isOpened);


        if (speedButton != null) {
            speedButton.setEnabled(isOpened);
            speedButton.setSelected(playTimebase != 1);
        }

        autoCorrect.setEnabled(isOpened);
        autoCorrectPageBreaks.setEnabled(isOpened);
        autoCorrectTransposed.setEnabled(isOpened);
        autoCorrectSpacing.setEnabled(isOpened);

        if (gapSpinner != null) {
            gapSpinner.setEnabled(isOpened);
        }
        if (vgapSpinner != null) {
            vgapSpinner.setEnabled(isOpened);
        }
        if (startSpinner != null) {
            startSpinner.setEnabled(isOpened);
        }
        if (endSpinner != null) {
            endSpinner.setEnabled(isOpened);
        }
        if (bpmField != null) {
            bpmField.setEnabled(isOpened);
        }

        setGapHere.setEnabled(isOpened);
        setStartHere.setEnabled(isOpened);
        setEndHere.setEnabled(isOpened);
        setVideoGapHere.setEnabled(isOpened);
        setVideoMarkHere.setEnabled(isOpened);
        removeStart.setEnabled(isOpened);
        removeEnd.setEnabled(isOpened);
        removeVideoGap.setEnabled(isOpened);

        showStartEnd.setEnabled(isOpened);
        showLyricsStart.setEnabled(isOpened);
        showVideoGap.setEnabled(isOpened);
    }

    public void openSongFolder() {
        songList.openSongFolder();
    }

    public void refreshGroups() {
        int row = groups.getSelectedRow();
        String rule = row < 0 ? "" : (String) groups.getValueAt(row, 0);

        groups.setIgnoreChange(true);
        groups.refresh();

        int n = groups.getRowCount();
        for (int i = 0; i < n; i++) {
            String ir = (String) groups.getValueAt(i, 0);
            if (ir.equals(rule)) {
                groups.setRowSelectionInterval(i, i);
                break;
            }
        }
        groups.setIgnoreChange(false);
    }


    void toggleSongInfo() {
        boolean onoff = songInfoToggle.isSelected();
        if (onoff) {
            updateSongInfo(enableLyrics);
        } else {
            updateSongInfo(showSongInfo);
        }
    }

    private void updateSongInfo(Action a) {
        songInfoToggle.setSelected(false);
        filterAll.setSelected(false);
        detailToggle.setSelected(false);
        bgToggle.setSelected(false);

        boolean info = a == showSongInfo;
        boolean lyrics = a == enableLyrics;
        boolean playlist = playlistToggle.isSelected();
        boolean detail = a == detailLibrary;
        boolean bgonly = a == showSongInfoBackground;

        if (!(info || lyrics || detail || bgonly)) {
            // songInfo.show(songInfo.SHOW_NONE);
            lyrics = true;
        }

        songInfoToggle.setSelected(info);
        filterAll.setSelected(lyrics);
        detailToggle.setSelected(detail);
        bgToggle.setSelected(bgonly);

        if (info) {
            songInfo.show(songInfo.SHOW_FILES);
        }

        if (lyrics) {
            songInfo.clear();
            songList.showLyrics(true);
            YassSong s = songList.getFirstSelectedSong();
            if (s != null) {
                songList.loadSongDetails(s, new YassTable());
            }
            songInfo.setSong(s);

            songInfo.show(songInfo.SHOW_LYRICS);
        } else {
            songList.showLyrics(false);
        }

        if (bgonly) {
            songInfo.showChildren(false);
            playlistComponent.setVisible(false);

            songInfoToggle.setSelected(false);
            filterAll.setSelected(false);
            detailToggle.setSelected(false);
        } else {
            songInfo.showChildren(true);
        }

        if (playlist && !bgonly) {
            playlistComponent.setVisible(true);
        } else {
            playlistComponent.setVisible(false);
        }

        if (detail) {
            playlistComponent.setVisible(false);
            updateDetails();
            songInfo.show(songInfo.SHOW_NONE);

            songInfoToggle.setSelected(false);
            filterAll.setSelected(false);
            bgToggle.setSelected(false);
        } else {
            updateDetails();
        }

        boolean plon = playlistComponent.isVisible();
        playlistToggle.setSelected(plon);
        if (playlistCBI != null) {
            playlistCBI.setState(plon);
        }

        songInfo.repaint();
    }


    public void stopPlayers() {
        songInfo.stopPlayer();
    }


    private JComponent createVideoToolbar() {
        if (videoToolbar != null) {
            return videoToolbar;
        }

        JToolBar t = new JToolBar(I18.get("tool_video"));
        videoToolbar = t;
        t.setFloatable(false);
        AbstractButton b;

        if (YassVideoUtils.useFOBS) {
            t.add(videoAudioButton = new JToggleButton());
            videoAudioButton.setAction(enableVideoAudio);
            videoAudioButton.setToolTipText(videoAudioButton.getText());
            videoAudioButton.setText("");
            videoAudioButton.setIcon(getIcon("mute24Icon"));
            videoAudioButton.setSelectedIcon(getIcon("nomute24Icon"));
            videoAudioButton.setFocusable(false);
            videoAudioButton.setSelected(true);
            t.addSeparator();
        }
        vgapSpinner = new TimeSpinner(I18.get("tool_video_gap"), 0, 10000, TimeSpinner.NEGATIVE);
        t.add(vgapSpinner);
        vgapSpinner.getSpinner().setFocusable(false);
        vgapSpinner.getSpinner().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (!isUpdating) {
                    setVideoGap(vgapSpinner.getTime());
                }
            }
        });
        Dimension d = vgapSpinner.getPreferredSize();
        d.height = t.getPreferredSize().height;
        vgapSpinner.setMaximumSize(d);

        if (YassVideoUtils.useFOBS) {
            t.add(Box.createHorizontalGlue());

            t.add(vmarkButton = new JToggleButton());
            vmarkButton.setAction(setVideoMarkHere);
            vmarkButton.setToolTipText(vmarkButton.getText());
            vmarkButton.setText("");
            vmarkButton.setIcon(getIcon("copy24Icon"));
            vmarkButton.setFocusable(false);
            t.add(b = new JButton());
            b.setAction(setVideoGapHere);
            b.setToolTipText(b.getText());
            b.setText("");
            b.setIcon(getIcon("paste24Icon"));
            b.setFocusable(false);
            t.add(b = new JButton());
            b.setAction(removeVideoGap);
            b.setToolTipText(b.getText());
            b.setText("");
            b.setIcon(getIcon("delete24Icon"));
            b.setFocusable(false);
        }
        return t;
    }


    public void updatePlayerPosition() {
        if (sheet == null) {
            return;
        }
        int i = table.getSelectionModel().getMinSelectionIndex();
        if (i >= 0) {
            YassRow r = table.getRowAt(i);
            if (r.isNoteOrPageBreak()) {
                sheet.setPlayerPosition(sheet.beatToTimeline(r.getBeatInt()));
            } else if (r.isGap()) {
                sheet.setPlayerPosition(sheet.beatToTimeline(0));
            } else if (r.isEnd()) {
                sheet.setPlayerPosition(sheet.toTimeline(sheet.getDuration()));
            } else if (r.isComment()) {
                sheet.setPlayerPosition(sheet.toTimeline(table.getStart()));
            } else {
                sheet.setPlayerPosition(-1);
            }

        } else {
            sheet.setPlayerPosition(-1);
        }

        if (video != null && sheet.showVideo()) {
            if (vmark != 0) {
                video.setTime(vmark);
                return;
            }
            long time;
            time = sheet.fromTimeline(sheet.getPlayerPosition());
            if (!sheet.isLive()) {
                video.setTime((int) time);
            }
        }
    }

    public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
        dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    }

    public void dragExit(DropTargetEvent dropTargetEvent) {
    }

    public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
    }

    public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
    }

    public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
        boolean enableImport = false;
        /*
        try {
            Transferable tr = dropTargetDropEvent.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dropTargetDropEvent
                        .acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

                String songdir = prop.getProperty("song-directory");
                if (songdir == null || !new File(songdir).exists()) {
                    dropTargetDropEvent.getDropTargetContext().dropComplete(
                            true);
                    return;
                }
                String canonicSongDir = new File(songdir).getCanonicalPath();
                boolean insideSongDir = false;

                Vector<File> fileVector = new Vector<>();
                java.util.List fileList = (java.util.List) tr.getTransferData(DataFlavor.javaFileListFlavor);
                for (Object o : fileList) {
                    File file = (File) o;
                    insideSongDir = file.getCanonicalPath().startsWith(canonicSongDir);
                    if (file.isDirectory()) {
                        if (insideSongDir) {
                            File[] files = file.listFiles();
                            file = null;
                            for (File file1 : files) {
                                String str = file1.getAbsolutePath();
                                if (str.toLowerCase().endsWith(".txt")) {
                                    file = file1;
                                    // load STANDARD version, if possible
                                    if (!file.getName().contains("[")) {
                                        break;
                                    }
                                }
                            }
                            if (file != null) {
                                openFiles(file.getAbsolutePath());
                                break;
                            }
                        } else {
                            fileVector.addElement(file);
                        }
                    } else {
                        String fn = file.getName().toLowerCase();
                        if (insideSongDir) {
                            if (fn.endsWith(".txt")) {
                                openFiles(file.getAbsolutePath());
                                break;
                            }
                        } else {
                            if (fn.endsWith(".txt") || fn.endsWith(".mp3")
                                    || fn.endsWith(".mpg")
                                    || fn.endsWith(".mpeg")
                                    || fn.endsWith(".jpg")
                                    || fn.endsWith(".jpeg")) {
                                fileVector.addElement(file);
                            }
                            if (fn.endsWith(".kar") || fn.endsWith(".mid")) {
                                dropTargetDropEvent.getDropTargetContext()
                                        .dropComplete(true);
                                createNewSong(file.getAbsolutePath());
                                return;
                            }
                        }
                    }
                }
                dropTargetDropEvent.getDropTargetContext().dropComplete(true);
                File[] ff = new File[fileVector.size()];
                fileVector.copyInto(ff);
                if (enableImport) {
                    YassImport.importFiles(songList, prop, ff);
                }
                return;
            }

            if (enableImport
                    && dropTargetDropEvent
                    .isDataFlavorSupported(DataFlavor.stringFlavor)) {
                dropTargetDropEvent
                        .acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                Object td = tr.getTransferData(DataFlavor.stringFlavor);
                if (td instanceof String) {
                    // TODO IMPORT
                    String tds = (String) td;
                    boolean ok = isValidKaraokeString(tds);
                    if (ok) {
                        StringTokenizer st = new StringTokenizer(tds, "\n\r");
                        dropTargetDropEvent.getDropTargetContext()
                                .dropComplete(true);
                        YassTable t = new YassTable();
                        t.init(prop);
                        while (st.hasMoreTokens()) {
                            String s = st.nextToken();
                            if (!t.addRow(s)) {
                                break;
                            }
                        }
                        String fn = YassImport.importTable(songList, prop, t);
                        if (fn != null) {
                            openFiles(fn);
                        }
                    }
                } else {
                    dropTargetDropEvent.getDropTargetContext().dropComplete(
                            true);
                }
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        dropTargetDropEvent.rejectDrop();
    }

    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Yet Another Songeditor by Saruta\nVersion: " + VERSION + " (" + DATE + ")\nmail@yass-along.com\n\n");
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        BufferCapabilities bufCap = gc.getBufferCapabilities();
        boolean page = bufCap.isPageFlipping();
        sb.append("Maximum Heap: ").append(Runtime.getRuntime().maxMemory() / 1024 / 1024).append("MB\n");
        sb.append("Occupied Heap: ").append(Runtime.getRuntime().totalMemory() / 1024 / 1024).append("MB\n");
        sb.append("Accelerated Memory: ").append(sheet.getAvailableAcceleratedMemory() / 1024 / 1024).append("MB\n");
        sb.append("Page Flipping: ").append(page ? "supported" : "no").append("\n");
        return sb.toString();
    }

    void hideErrors() {
        errDialog.setVisible(false);
    }

    private void showNothing() {
        sheet.showVideo(false);
        sheet.showBackground(false);
        showNothingToggle.setSelected(true);
        videoButton.setSelected(false);
        sheet.repaint();
    }

    private void editLyrics() {
        int[] rows = table.getSelectedRows();
        if (rows.length == 1) {
            YassRow r = table.getRowAt(rows[0]);
            if (r.isGap()) {
                String input = JOptionPane.showInputDialog(tab, I18.get("edit_lyrics_edit_gap_msg"), (int) (table.getGap()) + "");
                try {
                    int gap = Integer.parseInt(input);
                    table.setGap(gap);
                    sheet.update();
                    table.firstNote();
                    table.repaint();
                    table.addUndo();
                } catch (Exception ignored) {
                }
                return;
            }
            if (r.isComment() && r.getCommentTag().equals("START:")) {
                String input = JOptionPane.showInputDialog(tab, I18.get("edit_lyrics_edit_start_msg"), (int) (table.getStart()) + "");
                try {
                    int val = Integer.parseInt(input);
                    setStart(val);
                } catch (Exception ignored) {}
                return;
            }
            if (r.isEnd()
                    || (r.isComment() && r.getCommentTag().equals("END:"))) {
                String input = JOptionPane.showInputDialog(tab, I18.get("edit_lyrics_edit_end_msg"), (int) (table.getEnd()) + "");
                try {
                    int val = Integer.parseInt(input);
                    table.setEnd(val);
                    sheet.update();
                    table.lastNote();
                    table.repaint();
                    table.addUndo();
                } catch (Exception ignored) {}
                return;
            }
        }

        if (lyrics.isEditable()) {
            lyrics.finishEditing();
        } else {
            lyrics.editLyrics();
        }
    }

    public void setStart(int ms) {
        for (YassTable t: getOpenTables(table))
            t.setStart(ms);
        updateStartEnd();
    }

    public void setEnd(int ms) {
        if (ms == (int) (mp3.getDuration() / 1000))
            ms = -1;
        for (YassTable t: getOpenTables(table))
            t.setEnd(ms);
        updateStartEnd();
    }

    private void updateStartEnd() {
        int start = (int) table.getStart();
        startSpinner.setTime(start);
        int end = (int) table.getEnd();
        int dur = (int) (mp3.getDuration() / 1000);
        if (end < 0) {
            end = dur;
        }
        endSpinner.setTime(end);

        startSpinner.setDuration(dur);
        endSpinner.setDuration(dur);
    }

    public void setGap(int ms) {
        for (YassTable t: getOpenTables(table))
            t.setGap(ms);
        sheet.setPlayerPosition(sheet.toTimeline(table.getGap()));
        updateGap();
    }

    private void updateGap() {
        int gap = (int) table.getGap();
        if (gapSpinner != null) {
            gapSpinner.setTime(gap);
            int dur = (int) (mp3.getDuration() / 1000);
            gapSpinner.setDuration(dur);
        }
        double bpm = table.getBPM();
        if (bpmField != null) {
            bpmField.setText(bpm + "");
        }
    }

    public void setVideoGap(int ms) {
        for (YassTable t: getOpenTables(table))
            t.setVideoGap(ms / 1000.0);
        updateVideoGap();
    }

    private void setVideoMark(int ms) {
        vmark = ms;
    }

    private void updateVideoGap() {
        double vgap = table.getVideoGap();
        int ms = (int) (vgap * 1000);

        if (vgapSpinner != null) {
            vgapSpinner.setTime(ms);
            int dur = (int) (mp3.getDuration() / 1000);
            vgapSpinner.setDuration(dur);
        }
        if (video != null && sheet.showVideo())
            video.setVideoGap(ms);
    }

    private void playSelection(int mode) {
        long[] inout = new long[2];

        int i = table.getSelectionModel().getMinSelectionIndex();
        int j = table.getSelectionModel().getMaxSelectionIndex();
        long pos = -1;
        boolean fromHere = i < 0;

        if (!fromHere) {
            YassRow r = table.getRowAt(i);
            if (!r.isNote()) {
                fromHere = true;
            }
        }
        if (fromHere) {
            pos = sheet.fromTimeline(sheet.getPlayerPosition()) * 1000;
            i = sheet.nextElement();
            j = -1;
        }
        startPlaying();
        long[][] clicks = table.getSelection(i, j, inout, null, false);
        boolean midiEnabled = midiButton.isSelected();
        boolean mp3Enabled = mp3Button.isSelected();
        if (mode == 0) {
            mp3.setMIDIEnabled(midiEnabled);
            mp3.setAudioEnabled(mp3Enabled);
        } else if (mode == 1) {
            mp3.setMIDIEnabled(true);
            mp3.setAudioEnabled(false);
        } else if (mode == 2) {
            mp3.setMIDIEnabled(true);
            mp3.setAudioEnabled(true);
        }
        mp3.playSelection(pos < 0 ? inout[0] : pos, inout[1], clicks, playTimebase);
    }

    private void playSelectionBefore(int mode) {
        int i = table.getSelectionModel().getMinSelectionIndex();
        if (i < 0)
            return;

        YassRow r = table.getRowAt(i);
        if (!r.isNote())
            return;
        int beat = r.getBeatInt();
        long end = (long) table.beatToMs(beat);

        long pos;
        YassRow r0 = table.getRowAt(i - 1);
        if (mode == 1 || !r0.isNoteOrPageBreak()) {
            pos = end - beforeNextMs;
            if (pos < 0) pos = 0;
        } else if (r0.isNote()) {
            pos = (long) table.beatToMs(r0.getBeatInt() + r0.getLengthInt());
        }
        else { // if (r0.isPageBreak())
            pos = (long) sheet.getLeftMs();
        }
        startPlaying();

        mp3.setMIDIEnabled(false);
        mp3.setAudioEnabled(true);
        mp3.playSelection(pos * 1000, end * 1000, null, playTimebase);
    }

    private void setBeforeNextMs(int n) {
        beforeNextMs = n;
    }

    private void playSelectionNext(int mode) {
        int i = table.getSelectionModel().getMaxSelectionIndex();
        if (i < 0)
            return;

        YassRow r = table.getRowAt(i);
        if (!r.isNote())
            return;

        int beat = r.getBeatInt() + r.getLengthInt();
        long pos = (long) table.beatToMs(beat);

        long end;
        YassRow r1 = table.getRowAt(i + 1);
        if (mode == 1 || !r1.isNoteOrPageBreak())
            end = pos + beforeNextMs;
        else if (r1.isNote())
            end = (long) table.beatToMs(r1.getBeatInt());
        else // if (r1.isPageBreak()
            end = (long) sheet.getMaxVisibleMs();

        startPlaying();

        mp3.setMIDIEnabled(false);
        mp3.setAudioEnabled(true);
        mp3.playSelection(pos * 1000, end * 1000, null, playTimebase);
    }

    private void startRecording() {
        sheet.getTemporaryNotes().clear();

        awt.reset();
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(awt, AWTEvent.MOUSE_EVENT_MASK);
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(awt, AWTEvent.MOUSE_MOTION_EVENT_MASK);
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(awt, AWTEvent.KEY_EVENT_MASK);
    }

    private void stopRecording() {
        mp3.interruptMP3();
        java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(awt);
        java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(awt);
        java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(awt);
    }

    public void startPlaying() {
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(awt2, AWTEvent.MOUSE_EVENT_MASK);
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(awt2, AWTEvent.MOUSE_MOTION_EVENT_MASK);
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(awt2, AWTEvent.KEY_EVENT_MASK);
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(awt2, AWTEvent.MOUSE_WHEEL_EVENT_MASK);
    }

    public void stopPlaying() {
        java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(awt2);
        java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(awt2);
        java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(awt2);
        mp3.interruptMP3();
    }

    private void playPageOrFrozen(int mode, boolean frozen) {
        int i;
        int j;
        long[] inout = new long[2];
        long[][] clicks;
        if (frozen) {
            long in = sheet.getInSnapshot();
            long out = sheet.getOutSnapshot();
            if (in == out || in < 0 || out < 0) {
                return;
            }

            i = sheet.nextElement(sheet.toTimeline(Math.min(in, out)));
            j = sheet.nextElement(sheet.toTimeline(Math.max(in, out)));
            inout[0] = Math.min(in, out) * 1000L;
            inout[1] = Math.max(in, out) * 1000L;
            clicks = table.getSelection(i, j, new long[2], null, false);
        } else {
            i = table.getSelectionModel().getMinSelectionIndex();
            j = table.getSelectionModel().getMaxSelectionIndex();
            if (i < 0) {
                i = j = sheet.nextElement();
            }
            int[] ij = table.enlargeToPages(i, j);
            if (ij == null) {
                return;
            }
            i = ij[0];
            j = ij[1];
            clicks = table.getSelection(i, j, inout, null, false);
        }
        startPlaying();

        boolean midiEnabled = midiButton.isSelected();
        boolean mp3Enabled = mp3Button.isSelected();
        if (mode == 0) {
            mp3.setMIDIEnabled(midiEnabled);
            mp3.setAudioEnabled(mp3Enabled);
        } else if (mode == 1) {
            mp3.setMIDIEnabled(true);
            mp3.setAudioEnabled(false);
        } else if (mode == 2) {
            mp3.setMIDIEnabled(true);
            mp3.setAudioEnabled(true);
        }
        mp3.playSelection(inout[0], inout[1], clicks, playTimebase);
    }

    public void previewEdit(boolean onoff) {
        if (onoff) {
            if (editMenu != null) {
                editMenu.setVisible(false);
            }
            if (editTools != null) {
                editTools.setVisible(false);
            }
        } else {
            if (editMenu != null) {
                editMenu.setVisible(true);
            }
            if (editTools != null) {
                editTools.setVisible(true);
            }
        }
    }

    private void fullscreen() {
        Component c = SwingUtilities.getRoot(main);
        if (!(c instanceof JFrame)) {
            return;
        }
        JFrame f = (JFrame) c;
        if (f.getExtendedState() == Frame.NORMAL) {
            enterFullscreen();
        } else {
            leaveFullscreen();
        }
        if (currentView == VIEW_EDIT) {
            SwingUtilities.invokeLater(() -> {
                revalidateLyricsArea();
                sheet.repaint();
            });
        }
    }

    private void leaveFullscreen() {
        Component c = SwingUtilities.getRoot(main);
        if (!(c instanceof JFrame)) {
            return;
        }

        JFrame f = (JFrame) c;
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        String w = prop.getProperty("frame-width");
        if (w == null) {
            w = dim.width >= 1000 ? "1000" : String.valueOf(dim.width);
        } else if (Integer.parseInt(w) > dim.width) {
            w = String.valueOf(dim.width);
        }
        String h = prop.getProperty("frame-height");
        if (h == null) {
            h = dim.height >= 600 ? "600" : String.valueOf(dim.height);
        } else if (Integer.parseInt(h) > dim.height) {
            w = String.valueOf(dim.height);
        }
        Point p = null;
        int x = prop.getIntProperty("frame-x");
        int y = prop.getIntProperty("frame-y");
        if (x > 0 && y > 0) {
            p = new Point(x, y);
        }
        f.dispose();
        f.setUndecorated(false);

        f.setSize(new Dimension(Integer.valueOf(w), Integer.valueOf(h)));
        if (p != null) {
            f.setLocation(p);
        } else {
            f.setLocationRelativeTo(null);
        }

        f.setExtendedState(Frame.NORMAL);
        f.validate();
        f.setVisible(true);
    }


    private void enterFullscreen() {
        Component c = SwingUtilities.getRoot(main);
        if (!(c instanceof JFrame)) {
            return;
        }

        JFrame f = (JFrame) c;
        prop.setProperty("frame-width", f.getSize().width + "");
        prop.setProperty("frame-height", f.getSize().height + "");
        prop.setProperty("frame-x", f.getLocation().x + "");
        prop.setProperty("frame-y", f.getLocation().y + "");

        f.setVisible(false);
        f.dispose();
        f.setUndecorated(true);
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle bounds = env.getMaximumWindowBounds();
        f.setMaximizedBounds(bounds);
        f.setExtendedState(Frame.MAXIMIZED_BOTH);
        f.validate();
        f.setVisible(true);
    }

    private void playAll(boolean fromStart) {
        onePage();
        if (fromStart) {
            table.firstNote();
        } else {
            table.home();
        }
        sheet.setLyricsVisible(false);
        sheet.showBackground(true);
        if (playAllVideoCBI != null && playAllVideoCBI.isSelected()) {
            sheet.showVideo(true);
        }
        mp3.setLive(true);
        sheet.setLive(true);
        sheet.refreshImage();

        int i = table.getSelectionModel().getMinSelectionIndex();
        if (i < 0) {
            return;
        }

        long[] inout = new long[2];
        playAllClicks = table.getSelection(i, -1, inout, null, false);
        playAllStart = Math.max(0, inout[0] - 4000000);

        SwingUtilities.invokeLater(() -> {
            startPlaying();

            boolean mp3Enabled = mp3Button.isSelected();
            boolean midiEnabled = midiButton.isSelected();
            mp3.setMIDIEnabled(midiEnabled);
            mp3.setAudioEnabled(mp3Enabled);
            mp3.playSelection(playAllStart, -1, playAllClicks, playTimebase);
        });
    }


    public void revalidateLyricsArea() {
        if (sheet.isLive()) {
            return;
        }
        int newh = sheet.getTopLine() - 30;

        // LYRICS POSITION
        String layout = prop.getProperty("editor-layout");
        if (layout == null) {
            layout = "East";
        }

        String lyricsWidthString = prop.getProperty("lyrics-width");
        String lyricsHeightString = prop.getProperty("lyrics-min-height");
        int lyricsWidth = Integer.parseInt(lyricsWidthString);
        int lyricsMinHeight = Integer.parseInt(lyricsHeightString);
        if (newh < lyricsMinHeight) {
            newh = lyricsMinHeight;
        }

        int h = lyrics.getBounds().height;
        if (h != newh) {
            if (layout.equals("East")) {
                lyrics.setBounds(500, 30, lyricsWidth, newh);
            } else if (layout.equals("West")) {
                lyrics.setBounds(0, 30, lyricsWidth, newh);
            }
            sheet.revalidate();
        }
    }

    public boolean isRelative() {
        return !sheet.isPanEnabled();
    }

    public void setRelative(boolean onoff) {
        sheet.enablePan(onoff);
        if (alignCBI != null) {
            alignCBI.setState(onoff);
        }
        revalidateLyricsArea();
    }


    public void copyRows() {
        table.copyRows();
        if (sheet != null) {
            sheet.makeSnapshot();
            sheet.repaint();
        }
    }

    private void onePage() {
        int row = table.getSelectionModel().getMinSelectionIndex();
        if (row < 0) {
            row = sheet.nextElement();
        }
        if (row < 0) {
            return;
        }

        YassRow r = table.getRowAt(row);
        while (r.isPageBreak() && row > 0) {
            r = table.getRowAt(--row);
        }
        if (row < 0) {
            return;
        }
        table.setRowSelectionInterval(row, row);
        setRelative(true);
        YassTable.setZoomMode(YassTable.ZOOM_ONE);
        table.setMultiSize(1);
        table.zoomPage();
        lyrics.repaintLineNumbers();
    }

    boolean openFiles(String filenames, boolean append) {
        if (filenames == null) {
            return false;
        }
        Vector<String> v = new Vector<>();
        StringTokenizer st = new StringTokenizer(filenames, ";");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            v.addElement(s);
        }
        return openFiles(v, append);
    }

    boolean openFiles(Vector<String> files, boolean append) {
        int n = files.size();
        if (n > 8 || n < 1)
            return false;

        // collect all files
        Vector<String> all = new Vector<>();
        for (String f : files) { // directories
            Vector<String> list = YassUtils.getKaraokeFiles(f, prop.getProperty("song-filetype"));
            if (list != null)
                all.addAll(list);
        }
        for (String f : files) { // files
            if (YassUtils.isKaraokeFile(f))
                all.add(f);
        }
        // collect all tracks
        Vector<YassTable> multis = new Vector<>();
        Vector<YassTable> singles = new Vector<>();
        for (String f : all) {
            YassTable t = new YassTable();
            t.init(prop);
            t.loadFile(f);
            int multi = t.getMaxP();
            if (multi > 1) {
                Vector<YassTable> tracks = t.splitTable();
                if (tracks != null)
                    multis.addAll(tracks);
            } else {
                singles.add(t);
            }
        }

        // open editor, load all tracks
        if (!append) {
            closeAllTables();
        }

        // add all (but only if not opened yet)
        Vector<YassTable> tables = new Vector<>();
        for (YassTable t: multis) {
            if (getOpenTables(t).size() == 0)
                tables.add(t);
        }
        for (YassTable t: singles) {
            if (getOpenTables(t).size() == 0)
                tables.add(t);
        }
        if (tables.size() < 1)
            return false;

        for (YassTable t2 : tables) {
            YassTable t = createNextTable();
            t.setPreventUndo(true);
            t.removeAllRows();
            t.loadTable(t2, true);
            t.setPreventUndo(false);
        }
        setActiveTable(firstTable());
        setView(VIEW_EDIT);
        return true;
    }

    private void openEditor(boolean reload) {
        if (table == null)
            setActiveTable(firstTable());
        if (table == null || table.getArtist() == null || table.getTitle() == null)
            return;
        updateTrackComponent();
        storeRecentFiles();
        mp3.reinitSynth();
        mp3.openMP3(table.getDirMP3());
        updateTitle();

        sheet.setDuration(mp3.getDuration() / 1000.0);
        sheet.setActiveTable(table);
        String vd = table.getVideo();
        if (video != null && vd != null) {
            video.setVideo(table.getDir() + File.separator + vd);
        }
        String bg = table.getBackgroundTag();
        if (bg != null) {
            File file = new File(table.getDir() + File.separator + bg);
            BufferedImage img = null;
            if (file.exists()) {
                try {
                    img = YassUtils.readImage(file);
                } catch (Exception ignored) {
                }
            }
            sheet.setBackgroundImage(img);
            mp3.setBackgroundImage(img);
        }

        sheet.revalidate();
        main.revalidate();
        SwingUtilities.invokeLater(() -> {
            // sheet.init();
            sheet.update();
            sheet.requestFocus();
            setRelative(true);
            if (!reload) {
                YassTable.setZoomMode(YassTable.ZOOM_ONE);
                table.setMultiSize(1);

                table.firstNote();
                updatePlayerPosition();
                sheet.repaint();
            }
        });

        lyrics.setTable(table);
        lyrics.repaintLineNumbers();

        for (YassTable t: openTables) {
            t.getColumnModel().getColumn(0).setPreferredWidth(10);
            t.getColumnModel().getColumn(0).setMaxWidth(10);
            t.getColumnModel().getColumn(1).setPreferredWidth(50);
            t.getColumnModel().getColumn(2).setPreferredWidth(50);
            t.getColumnModel().getColumn(3).setPreferredWidth(50);
            t.resetUndo();
            t.addUndo();
            boolean oldTrim = autoTrim();
            setAutoTrim(false);
            checkData(t, false, true);
            setAutoTrim(oldTrim);
        }
        updateActions();

        // prevent unsetting saved icon
        isUpdating = true;
        updateLyrics();
        // updateMP3Info(table);
        // updateRaw();
        // updateCover();
        // updateBackground();
        // updateVideo();
        updateVideo(); // todo: really?
        isUpdating = false;

        for (YassTable t: openTables)
            songList.addOpened(t);

        System.out.println("Searching for USB mic...");
        String device = prop.getProperty("control-mic");
        String[] devices = YassCaptureAudio.getDeviceNames();
        boolean found = false;
        for (String device1 : devices) {
            if (device1.equals(device)) {
                found = true;
                break;
            }
        }
        if (found) {
            YassSession session = table.createSession();
            session.addTrack();
            session.getTrack(0).setActive(true);
            mp3.setDemo(false);
            mp3.setCapture(0, device, 0);
            mp3.setCapture(true);
            sheet.init(session);
        } else
            sheet.init(null);
    }

    /**
     * Stores opened files in .yass file.
     * */
    public void storeRecentFiles() {
        String recentFiles = null;
        for (YassTable yt: openTables) {
            String fn = yt.getDirFilename();
            if (recentFiles == null)
                recentFiles = fn;
            else if (!recentFiles.contains(fn))
                recentFiles = recentFiles + ";" + fn;
        }
        if (recentFiles == null && table != null)
            recentFiles = table.getDirFilename();
        if (recentFiles != null) {
            prop.setProperty("recent-files", recentFiles);
            prop.store();
        }
    }

    private void updateTitle() {
        Window root = SwingUtilities.getWindowAncestor(tab);
        if (root instanceof JFrame) {
            if (table == null)
                ((Frame) root).setTitle(I18.get("yass_title"));
            else {
                String t = table.getTitle();
                String a = table.getArtist();
                if (t == null || a == null) {
                    ((Frame) root).setTitle(I18.get("yass_title"));
                } else {
                    ((Frame) root).setTitle(a + " - " + t + " "
                            + I18.get("yass_title_appendix"));
                }
            }
        }
    }

    public YassSong selectSong(String artist, String title) {
        groups.setRowSelectionInterval(0, 0);
        groups.refresh();
        songList.setPreFilter(null);
        songList.filter(null);
        Vector<?> v = songList.findSong(artist, title);
        if (v == null || v.size() < 1) {
            songList.focusFirstVisible();
            return null;
        }
        YassSong song = (YassSong) v.firstElement();
        int i = songList.indexOf(song);
        if (i >= 0) {
            songList.changeSelection(i, 0, false, false);
        } else {
            songList.focusFirstVisible();
        }
        return song;
    }

    private void updateLyrics() {
        if (table == null)
            return;
        String lang = table.getLanguage();
        if (lang == null) {
            lang = "EN_US";
        } else {
            lang = mapLanguage(lang);
        }
        lyrics.setLanguage(lang);
        updateGap();
    }

    private String mapLanguage(String lang) {
        String dictMap = prop.getProperty("dict-map");
        StringTokenizer st = new StringTokenizer(dictMap, "|");
        while (st.hasMoreTokens()) {
            String language = st.nextToken();
            if (language.equals(lang)) {
                return st.nextToken();
            }
            st.nextToken();
        }
        return "EN-US";
    }


    private void undoLibraryChanges() {
        Vector<?> sel = songList.getSelectedSongs();
        if (sel.size() < 1) {
            return;
        }
        YassSong s = (YassSong) sel.firstElement();

        songList.undoSelection();
        songInfo.setSong(s);

        for (Enumeration<?> en = sel.elements(); en.hasMoreElements(); ) {
            s = (YassSong) en.nextElement();
            songInfo.removeBackup(s);
        }
    }

    private void undoAllLibraryChanges() {
        Vector<?> sel = songList.getSelectedSongs();
        if (sel.size() < 1) {
            return;
        }
        YassSong s = (YassSong) sel.firstElement();

        songList.undoAll();
        songInfo.setSong(s);

        File td = new File(prop.getProperty("temp-dir"));
        if (td.exists()) {
            YassUtils.deleteDir(td);
        }
        td.mkdirs();
    }

    private void updateVideo() {
        String vd = null;
        if (table != null) {
            String d = table.getDir();

            YassTableModel tm = (YassTableModel) table.getModel();
            YassRow r = tm.getCommentRow("VIDEO:");
            String v = r != null ? r.getComment() : null;
            if (v != null) {
                vd = d + File.separator + v;
            }
        }
        video.setVideo(vd);

        updateVideoGap();
    }

    private void save(Vector<YassTable> tables) {
        Vector<YassTable> stored = new Vector<>();
        for (YassTable t : tables) {
            if (t.isSaved() || stored.contains(t))
                continue;
            if (t.getDuetTrack() > 0) {
                Vector<YassTable> tracks = getOpenTables(t);
                YassTable mt = YassTable.mergeTables(tracks, prop);
                mt.storeFile(t.getDirFilename());
                stored.addAll(tracks);
            } else {
                t.storeFile(t.getDirFilename());
                stored.add(t);
            }
        }
        for (YassTable t : stored)
            t.setSaved(true);
        saveAll.setEnabled(false);
        main.repaint();
    }

    private void saveTrackAs() {
        String filename = askFilename(I18.get("lib_edit_file_msg"), FileDialog.SAVE);
        if (filename != null) {
            if (!table.storeFile(filename)) // todo warn
                return;
            openFiles(filename, true);
        }
    }

    private String askFolderName() {
        String defDir = table != null ? table.getDir() : null;
        if (defDir == null)
            defDir = prop.getProperty("song-directory");
        if (defDir != null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(defDir));
            chooser.setDialogTitle(I18.get("edit_tracks_open_folder"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.getActionMap().get("New Folder").setEnabled(false);
            chooser.setFileFilter(new javax.swing.filechooser.FileFilter(){
                public boolean accept(File f) { return f.isDirectory(); }
                public String getDescription() { return I18.get("edit_tracks_folder"); }

            });
            if (JFileChooser.APPROVE_OPTION == chooser.showDialog(tab, null))
                return chooser.getSelectedFile().getAbsolutePath();

        }
        return null;
    }

    /**
     * File selection dialog.
     *
     * @param msg  message
     * @param type FileDialog.LOAD or FileDialog.SAVE
     * @return null if canceled
     */
    private String askFilename(String msg, int type) {
        String filename = null;
        FileDialog fd = new FileDialog((JFrame) SwingUtilities.getWindowAncestor(tab), msg, type);
        fd.setMultipleMode(true);
        fd.setFile("*.txt");
        String defDir = table != null ? table.getDir() : null;
        if (defDir == null)
            defDir = prop.getProperty("song-directory");
        if (defDir != null)
            fd.setDirectory(defDir);
        fd.setVisible(true);
        if (fd.getFile() != null)
            filename = fd.getDirectory() + File.separator + fd.getFile();
        fd.dispose();
        return filename;
    }

    private void saveDuetAs() {
        String filename = askFilename(I18.get("lib_edit_file_msg"), FileDialog.SAVE);
        if (filename != null) {
            Vector<YassTable> tracks = getOpenTables(table);
            YassTable mt = YassTable.mergeTables(tracks, prop);
            if (!mt.storeFile(filename)) // todo warn
                return;
            openFiles(filename, false);
        }
    }

    private void renameTrack() {
        int p = table.getDuetTrack();
        if (p < 0)
            return;
        String old = table.getDuetTrackName();
        if (old == null)
            old = "";
        String name = JOptionPane.showInputDialog(tab, I18.get("edit_tracks_rename_title"), old);
        if (name != null) {
            name = name.trim();
            if (name.length() > 1 && !name.equals(old)) {
                table.setDuetTrack(p, name);
                table.setSaved(false);
                table.addUndo();
                main.repaint();
            }
        }
    }

    private void openFolder() {
        if (cancelOpen())
            return;
        String folderName = askFolderName();
        if (folderName != null)
            openFiles(folderName, false);
    }

    private void openFolderFromLibrary() {
        String folderName = askFolderName();
        if (folderName != null)
            openFiles(folderName, false);
    }

    private void openFile() {
        if (cancelOpen())
            return;
        String filename = askFilename(I18.get("lib_edit_file_msg"), FileDialog.LOAD);
        if (filename != null)
            openFiles(filename, false);
    }

    private void openTrack() {
        if (cancelOpen())
            return;
        String filename = askFilename(I18.get("lib_edit_file_msg"), FileDialog.LOAD);
        if (filename != null)
            openFiles(filename, true);
    }

    private void deleteTrack() {
        if (table.getDuetTrack() > 0) {
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(tab,
                    MessageFormat.format(I18.get("tool_tracks_delete_duet_msg"), table.getFilename(), table.getDuetTrackCount()),
                    I18.get("tool_tracks_delete_title"), JOptionPane.OK_CANCEL_OPTION))
                return;
        } else {
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(tab,
                    MessageFormat.format(I18.get("tool_tracks_delete_track_msg"), table.getFilename()),
                    I18.get("tool_tracks_delete_title"), JOptionPane.OK_CANCEL_OPTION))
                return;
        }
        if (!new File(table.getDirFilename()).delete()) {
            // todo show error
            return;
        }
        closeTrack();
    }

    private void closeTrack() {
        for (YassTable t : getOpenTables(table)) {
            sheet.removeTable(t);
            openTables.removeElement(t);
        }
        setActiveTable(firstTable());
        storeRecentFiles();
        updateTrackComponent();
        sheet.setActiveTable(table);
        lyrics.setTable(table);
        updateActions();
        updateTitle();
        if (table != null) { // todo: unused
            String vd = table.getVideo();
            if (video != null && vd != null) {
                video.setVideo(table.getDir() + File.separator + vd);
            }
            String bg = table.getBackgroundTag();
            if (bg != null) {
                File file = new File(table.getDir() + File.separator + bg);
                BufferedImage img = null;
                if (file.exists()) {
                    try {
                        img = YassUtils.readImage(file);
                    } catch (Exception ignored) {
                    }
                }
                sheet.setBackgroundImage(img);
                mp3.setBackgroundImage(img);
            }
        }
        sheet.repaint();
        sheet.requestFocus();
    }

    private boolean closeAll() {
        if (cancelOpen())
            return false;
        closeAllTables();
        prop.remove("recent-files");
        editRecent.setEnabled(false);
        saveAll.setEnabled(false);
        undo.setEnabled(false);
        redo.setEnabled(false);
        return true;
    }

    /**
     * Get all open tables belonging to the given table's filename
     * @param table
     * @return
     */
    private Vector<YassTable> getOpenTables(YassTable table) {
        Vector<YassTable> tables = new Vector<>();
        for (YassTable open : openTables) {
            if (open.getDir().equals(table.getDir()) && open.getFilename().equals(table.getFilename()))
                tables.add(open);
        }
        return tables;
    }

    public void filterLibrary() {
        String s = filterEditor.getText();
        if (s.equals(I18.get("tool_lib_find_empty"))) {
            s = "";
        }
        filterEditor.setForeground(Color.gray);
        songList.filterLyrics(filterAll.isSelected());
        songInfo.setBold(s);
        songList.filter(s);
    }


    public void startFilterLibrary() {
        if (currentView != VIEW_LIBRARY) {
            return;
        }
        SwingUtilities.invokeLater(() -> filterEditor.requestFocus());
    }

    private void createNewSong(String karname) {
        createNewSong(karname, false);

    }

    String createNewSong(String karname, boolean standAlone) {
        String songdir = null;
        if (standAlone) {
            if (karname != null) {
                File kar = new File(karname);
                songdir = kar.getParent();
            }
        } else {
            songdir = YassUtils.getSongDir(table, prop);
        }
        if (songdir == null) {
            songdir = "";
        }

        CreateSongWizard wiz = new CreateSongWizard(tab);
        wiz.setValue("songdir", songdir);
        wiz.setValue("folder", "");
        wiz.setValue("filename", "");
        wiz.setValue("melody", karname);
        wiz.setValue("creator", prop.getProperty("creator"));
        wiz.setValue("genre", "Other");
        wiz.setValue("edition", "");
        wiz.setValue("language", "Other");
        wiz.setValue("bpm", "300");
        wiz.setValue("lyrics", "");
        wiz.setValue("starteditor", "true");
        wiz.setValue("encoding", "");

        wiz.setValue("languages", prop.getProperty("language-tag"));
        wiz.setValue("languages-more", prop.getProperty("language-more-tag"));
        wiz.setValue("genres", prop.getProperty("genre-tag"));
        wiz.setValue("genres-more", prop.getProperty("genre-more-tag"));
        wiz.setValue("editions", prop.getProperty("edition-tag"));
        wiz.setValue("hyphenations", prop.getProperty("hyphenations"));
        wiz.show();

        if (wiz.getReturnCode() != Wizard.FINISH_RETURN_CODE) {
            return null;
        }

        Hashtable<?, ?> hash = wiz.getValues();
        String file = YassUtils.createSong(tab, hash, prop);

        boolean starteditor = hash.get("starteditor").equals("true");
        if (!standAlone) {
            if (file != null) {
                int k = file.lastIndexOf(File.separator);
                String filedir = file.substring(0, k);
                String folder = (String) hash.get("folder");
                String name = file.substring(k + 1);
                songList.addSong(filedir, folder, name);
                if (starteditor) {
                    songList.gotoSong(table);
                    openFiles(file, false);
                }
            }
            return file;
        } else {
            if (file != null && starteditor) {
                songList.gotoSong(table);
                openFiles(file, false);
                return file;
            }
        }
        return null;
    }


    public void exit() {
        Window w = SwingUtilities.getWindowAncestor(getTab());
        if (w != null) {
            w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
        }
    }

    void interruptPlay() {
        mp3.interruptMP3();
    }

    private void trimPageBreaks() {
        if (preventTrim) {
            return;
        }

        String s = prop.getProperty("correct-uncommon-pagebreaks");
        boolean withMinors = s != null && s.equals("true");
        boolean ask = s == null || s.equals("unknown");

        if (ask) {
            JPanel panel = new JPanel(new BorderLayout());
            JCheckBox box;
            panel.add("Center", new JLabel("<html>" + I18.get("edit_correct_breaks_msg")));
            panel.add("South", box = new JCheckBox("<html>" + I18.get("edit_correct_breaks_msg_hide")));

            int ok = JOptionPane.showConfirmDialog(tab, panel, I18.get("edit_correct_breaks_title"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                withMinors = true;
                if (box.isSelected()) {
                    prop.setProperty("correct-uncommon-pagebreaks", "true");
                    prop.store();
                }
            } else if (ok == JOptionPane.NO_OPTION) {
                withMinors = false;
                if (box.isSelected()) {
                    prop.setProperty("correct-uncommon-pagebreaks", "false");
                    prop.store();
                }
            } else {
                return;
            }
        }

        int[] rows = table.getSelectedRows();

        boolean changed = auto.autoCorrectAllPageBreaks(table, withMinors);
        if (changed) {
            preventTrim = true;
            ((YassTableModel) table.getModel()).fireTableDataChanged();

            if (rows != null) {
                table.clearSelection();
                for (int row : rows) {
                    table.addRowSelectionInterval(row, row);
                }
            }
            checkData(table, false, true);
            preventTrim = false;
            sheet.repaint();
        }
    }

    public void registerLibraryActions(JComponent c) {
        InputMap im = c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = c.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "showHelp");
        am.put("showHelp", showHelp);
        showHelp.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "setTitle");
        am.put("setTitle", setTitle);
        setTitle.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "setArtist");
        am.put("setArtist", setArtist);
        setArtist.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "setAlbum");
        am.put("setAlbum", setAlbum);
        setAlbum.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), "setID");
        am.put("setID", setID);
        setID.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refreshLibrary");
        am.put("refreshLibrary", refreshLibrary);
        refreshLibrary.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "setYear");
        am.put("setYear", setYear);
        setYear.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK), "newFile");
        am.put("newFile", newFile);
        newFile.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK), "openSongFromLibrary");
        am.put("openSongFromLibrary", openSongFromLibrary);
        openSongFromLibrary.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), "openFile");
        am.put("openFile", openFileFromLibrary);
        openFileFromLibrary.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "playSong");
        am.put("playSong", playSong);
        playSong.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "stopPlaySong");
        am.put("stopPlaySong", stopPlaySong);
        stopPlaySong.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK), "openSongFolder");
        am.put("openSongFolder", openSongFolder);
        openSongFolder.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), "saveLibrary");
        am.put("saveLibrary", saveLibrary);
        saveLibrary.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK), "editRecent");
        am.put("editRecent", editRecent);
        editRecent.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK), "undoAllLibraryChanges");
        am.put("undoAllLibraryChanges", undoAllLibraryChanges);
        undoAllLibraryChanges.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK), "filterLibrary");
        am.put("filterLibrary", filterLibrary);
        filterLibrary.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK), "printLibrary");
        am.put("printLibrary", printLibrary);
        printLibrary.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK), "addToPlayList");
        am.put("addToPlayList", addToPlayList);
        addToPlayList.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK), "removeFromPlayList");
        am.put("removeFromPlayList", removeFromPlayList);
        removeFromPlayList.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK), "movePlayListUp");
        am.put("movePlayListUp", movePlayListUp);
        movePlayListUp.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK), "movePlayListDown");
        am.put("movePlayListDown", movePlayListDown);
        movePlayListDown.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK), "removeSong");
        am.put("removeSong", removeSong);
        removeSong.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK), "findLyrics");
        am.put("findLyrics", findLyrics);
        findLyrics.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK), "reHyphenate");
        am.put("reHyphenate", reHyphenate);
        reHyphenate.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK), "selectAllSongs");
        am.put("selectAllSongs", selectAllSongs);
        selectAllSongs.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK), "fullscreen");
        c.getActionMap().put("fullscreen", fullscreen);
        fullscreen.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK));
    }

    public void registerEditorActions(JComponent c) {
        InputMap im = c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = c.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "showHelp");
        am.put("showHelp", showHelp);
        showHelp.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "editLyrics");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "editLyrics");
        editLyrics.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        am.put("editLyrics", editLyrics);

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), "saveTrack");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "saveTrack");
        saveTrack.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        am.put("saveTrack", saveTrack);

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), "saveAll");
        saveAll.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        am.put("saveAll", saveAll);

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "reloadAll");
        reloadAll.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        am.put("reloadAll", reloadAll);

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK), "openFile");
        am.put("openFile", openFile);
        openFile.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK | InputEvent.ALT_MASK), "openFolder");
        am.put("openFolder", openFolder);
        openFolder.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK | InputEvent.ALT_MASK), "openFolder");
        am.put("openFolder", openFolder);
        openFolderFromLibrary.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK), "gotoLibrary");
        am.put("gotoLibrary", gotoLibrary);
        gotoLibrary.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK | InputEvent.ALT_MASK), "shiftLeftRemainder");
        am.put("shiftLeftRemainder", shiftLeftRemainder);
        shiftLeftRemainder.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK | InputEvent.ALT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK | InputEvent.ALT_MASK), "shiftRightRemainder");
        am.put("shiftRightRemainder", shiftRightRemainder);
        shiftRightRemainder.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK | InputEvent.ALT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK | InputEvent.ALT_MASK), "shiftLeft");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK), "shiftLeft");
        am.put("shiftLeft", shiftLeft);
        shiftLeft.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK | InputEvent.ALT_MASK), "shiftRight");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK), "shiftRight");
        am.put("shiftRight", shiftRight);
        shiftRight.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK), "decHeight");
        am.put("decHeight", decHeight);
        decHeight.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK ), "decHeightOctave");

        am.put("decHeightOctave", decHeightOctave);
        decHeightOctave.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK ));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK), "incHeight");
        am.put("incHeight", incHeight);
        incHeight.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), "incHeightOctave");
        am.put("incHeightOctave", incHeightOctave);
        incHeightOctave.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK), "decLeft");
        am.put("decLeft", decLeft);
        decLeft.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK), "incLeft");
        am.put("incLeft", incLeft);
        incLeft.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_MASK), "decRight");
        am.put("decRight", decRight);
        decRight.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_MASK), "incRight");
        am.put("incRight", incRight);
        incRight.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "prevPage");
        am.put("prevPage", prevPage);
        prevPage.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "prevBeat");
        am.put("prevBeat", prevBeat);

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "nextBeat");
        am.put("nextBeat", nextBeat);

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "nextPage");
        am.put("nextPage", nextPage);
        nextPage.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_MASK), "selectPrevBeat");
        am.put("selectPrevBeat", selectPrevBeat);
        selectPrevBeat.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_MASK), "selectNextBeat");
        am.put("selectNextBeat", selectNextBeat);
        selectNextBeat.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_MASK), "onePage");
        am.put("onePage", onePage);
        onePage.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_MASK));
        onePage.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "lessPages");
        am.put("lessPages", lessPages);
        lessPages.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "morePages");
        am.put("morePages", morePages);
        morePages.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_MASK), "allRemainingPages");
        am.put("allRemainingPages", allRemainingPages);
        allRemainingPages.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), "viewAll");
        am.put("viewAll", viewAll);
        viewAll.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK), "pasteRows");
        am.put("pasteRows", pasteRows);
        pasteRows.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK | InputEvent.SHIFT_DOWN_MASK), "pasteNotes");
        am.put("pasteNotes", pasteNotes);
        pasteNotes.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), "copyRows");
        am.put("copyRows", copyRows);
        copyRows.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK), "removeCopy");
        am.put("removeCopy", removeCopy);
        removeCopy.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "togglePageBreak");
        am.put("togglePageBreak", togglePageBreak);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "removePageBreak");
        am.put("removePageBreak", removePageBreak);

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "removeRows");
        am.put("removeRows", removeRows);
        removeRows.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK), "removeRowsWithLyrics");
        am.put("removeRowsWithLyrics", removeRowsWithLyrics);
        removeRowsWithLyrics.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0), "absolute");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.SHIFT_MASK), "absolute");
        am.put("absolute", absolute);
        absolute.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK), "fullscreen");
        am.put("fullscreen", fullscreen);
        fullscreen.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK));

        // im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, // InputEvent.SHIFT_MASK), // "playAll");
        // am.put("playAll", playAll);
        // playAll.putValue(AbstractAction.ACCELERATOR_KEY, // KeyStroke.getKeyStroke(KeyEvent.VK_F12, InputEvent.SHIFT_MASK));
        //
        // im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), // "playAllFromHere");
        // am.put("playAllFromHere", playAllFromHere);
        // playAllFromHere.putValue(AbstractAction.ACCELERATOR_KEY, // KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));

        // am.put("recordAll", recordAll);

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK), "recordSelection");
        am.put("recordSelection", recordSelection);
        recordSelection.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK), "playSlower");
        am.put("playSlower", playSlower);
        playSlower.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "playSelection");
        am.put("playSelection", playSelection);
        playSelection.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.SHIFT_MASK), "playSelectionWithMIDI");
        am.put("playSelectionWithMIDI", playSelectionWithMIDI);
        playSelectionWithMIDI.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.SHIFT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK), "playSelectionWithMIDIAudio");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK), "playSelectionWithMIDIAudio");
        am.put("playSelectionWithMIDIAudio", playSelectionWithMIDIAudio);
        playSelectionWithMIDIAudio.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "playPage");
        am.put("playPage", playPage);
        playPage.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_MASK), "playPageWithMIDI");
        am.put("playPageWithMIDI", playPageWithMIDI);
        playPageWithMIDI.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK), "playPageWithMIDIAudio");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK), "playPageWithMIDIAudio");
        am.put("playPageWithMIDIAudio", playPageWithMIDIAudio);
        playPageWithMIDIAudio.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), "playBefore");
        am.put("playNext", playBefore);
        playBefore.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), "playNext");
        am.put("playNext", playNext);
        playNext.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), "showCopiedRows");
        am.put("showCopiedRows", showCopiedRows);
        showCopiedRows.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "playFrozen");
        am.put("playFrozen", playFrozen);
        playFrozen.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_MASK), "playFrozenWithMIDI");
        am.put("playFrozenWithMIDI", playFrozenWithMIDI);
        playFrozenWithMIDI.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK), "playFrozenWithMIDIAudio");
        am.put("playFrozenWithMIDIAudio", playFrozenWithMIDIAudio);
        playFrozenWithMIDIAudio.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK), "enableMidi");
        am.put("enableMidi", enableMidi);
        enableMidi.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK), "enableAudio");
        am.put("enableAudio", enableAudio);
        enableAudio.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK), "enableClicks");
        am.put("enableClicks", enableClicks);
        enableClicks.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "interruptPlay");
        am.put("interruptPlay", interruptPlay);
        interruptPlay.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK), "undo");
        am.put("undo", undo);
        undo.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK), "redo");
        am.put("redo", redo);
        redo.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "splitRows");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "joinRows");
        am.put("splitRows", splitRows);
        am.put("joinRows", joinRows);
        splitRows.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0));
        joinRows.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_MASK), "rollLeft");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "rollRight");
        am.put("rollLeft", rollLeft);
        am.put("rollRight", rollRight);
        rollLeft.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_MASK));
        rollRight.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "home");
        am.put("home", home);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "end");
        am.put("end", end);

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_MASK), "first");
        am.put("first", first);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_MASK), "last");
        am.put("last", last);

        /*
         * im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L , InputEvent.CTRL_MASK), * "lock"); am.put("lock", enableHyphenKeys);
         * enableHyphenKeys.putValue(AbstractAction.ACCELERATOR_KEY, * KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
         */
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), "golden");
        am.put("golden", golden);
        golden.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, 0));
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), "freestyle");
        am.put("freestyle", freestyle);
        freestyle.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.SHIFT_MASK), "rapgolden");
        am.put("rapgolden", rapgolden);
        rapgolden.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.SHIFT_MASK));
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.SHIFT_MASK), "rap");
        am.put("rap", rap);
        rap.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.SHIFT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), "standard");
        am.put("standard", standard);
        standard.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DEAD_TILDE, 0), "addEndian");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DEAD_CIRCUMFLEX, 0), "addEndian");
        im.put(KeyStroke.getKeyStroke("~"), "addEndian");
        am.put("addEndian", addEndian);
        addEndian.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DEAD_TILDE, 0));

        // umlaute not working
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK), "decGap");
        am.put("decGap", decGap);
        decGap.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), "incGap");
        am.put("incGap", incGap);
        incGap.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK | InputEvent.ALT_MASK), "decGap2");
        am.put("decGap2", decGap2);
        decGap2.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK | InputEvent.ALT_MASK | InputEvent.SHIFT_MASK), "incGap2");
        am.put("incGap2", incGap2);
        incGap2.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK | InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK), "selectLine");
        am.put("selectLine", selectLine);
        selectLine.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), "selectAll");
        am.put("selectAll", selectAll);
        selectAll.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "togglePageBreak");
        am.put("togglePageBreak", togglePageBreak);
        togglePageBreak.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK), "autoCorrectPageBreaks");
        am.put("autoCorrectPageBreaks", autoCorrectPageBreaks);
        autoCorrectPageBreaks.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK), "insertNote");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK), "insertNote");
        am.put("insertNote", insertNote);
        insertNote.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK));
        insertNote.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_MASK), "prevTrack");
        am.put("prevTrack", activatePrevTrack);
        activatePrevTrack.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK), "nextTrack");
        am.put("nextTrack", activateNextTrack);
        activateNextTrack.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK), "track1");
        am.put("track1", activateTrack1);
        activateTrack1.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_MASK), "track2");
        am.put("track2", activateTrack2);
        activateTrack2.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK), "track3");
        am.put("track3", activateTrack3);
        activateTrack3.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_MASK), "track4");
        am.put("track4", activateTrack4);
        activateTrack4.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_MASK), "track5");
        am.put("track5", activateTrack5);
        activateTrack5.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_MASK), "track6");
        am.put("track6", activateTrack6);
        activateTrack6.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_MASK), "track7");
        am.put("track7", activateTrack7);
        activateTrack7.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_MASK), "track8");
        am.put("track8", activateTrack8);
        activateTrack8.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_MASK));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_MASK), "darkmode");
        am.put("darkmode", darkmode);
        darkmode.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_MASK));
    }


    public JFrame createOwnerFrame() {
        return new OwnerFrame();
    }

    private static class OptionPaneCloseAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            JOptionPane optionPane = (JOptionPane) e.getSource();
            optionPane.setValue(JOptionPane.CLOSED_OPTION);
        }
    }

    private static class OptionPaneArrowAction extends AbstractAction {
        private final boolean myMoveRight;

        OptionPaneArrowAction(boolean moveRight) {
            myMoveRight = moveRight;
        }

        public void actionPerformed(ActionEvent e) {
            JOptionPane optionPane = (JOptionPane) e.getSource();
            EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
            eq.postEvent(new KeyEvent(optionPane, KeyEvent.KEY_PRESSED, e.getWhen(), (myMoveRight) ? 0 : InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_TAB, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_UNKNOWN));
        }
    }

    class PlayListBoxRenderer extends BasicComboBoxRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                if (-1 < index) {
                    list.setToolTipText(plBoxTips.elementAt(index));
                }
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(list.getFont());
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class PlayListBoxListener implements ItemListener, ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (playList == null) {
                return;
            }
            int savePlaylist = unsavedPlaylist();
            if (savePlaylist == JOptionPane.YES_OPTION) {
                playList.storePlayList();
            } else if (savePlaylist == JOptionPane.CANCEL_OPTION) {
                for (int i = 0; i < plBox.getItemCount(); i++) {
                    if (plBox.getItemAt(i).equals(playList.getPreviousItem())) {
                        plBox.setSelectedIndex(i);
                        break;
                    }
                }
                return;
            }
            int n = plBox.getSelectedIndex();
            if (n == 0) {
                playList.setName(I18.get("tool_playlist_new"));
                playList.removeAllSongs();
            } else {
                playList.setPlayList(n - 1);
            }
            removePlayList.setEnabled(n != 0);
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                playList.setPreviousItem((String)e.getItem());
            }
        }
    }

    class AlignedListCellRenderer extends JLabel implements
            ListCellRenderer<Object> {
        Dimension dim = new Dimension(100, 20);
        Color selcol = UIManager.getColor("Table.selectionBackground");
        Color bgcol = UIManager.getColor("Label.background");

        AlignedListCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
        }

        public Dimension getPreferredSize() {
            return dim;
        }

        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String txt = (String) value;
            setForeground(Color.gray);
            setBackground(isSelected ? selcol : bgcol);
            setFont(groupsFont);
            setText(txt);
            return this;
        }
    }

    class RecordEventListener implements AWTEventListener {
        private boolean lastWasPressed = false;

        public void reset() {
            lastWasPressed = false;
        }

        public void eventDispatched(AWTEvent e) {
            boolean added = false;
            if (e instanceof KeyEvent) {
                KeyEvent k = (KeyEvent) e;
                if (k.getID() == KeyEvent.KEY_TYPED) {
                    k.consume();
                    return;
                }
                if (k.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    stopRecording();
                    k.consume();
                    return;
                }
                boolean pressed = k.getID() == KeyEvent.KEY_PRESSED;
                if (pressed && !lastWasPressed) {
                    sheet.getTemporaryNotes().addElement(mp3.getPosition());
                }
                if (!pressed && lastWasPressed) {
                    sheet.getTemporaryNotes().addElement(mp3.getPosition());
                    added = true;
                }
                lastWasPressed = pressed;
                k.consume();
            } else if (e instanceof MouseEvent) {
                MouseEvent k = (MouseEvent) e;

                if (k.getID() == MouseEvent.MOUSE_MOVED
                        || k.getID() == MouseEvent.MOUSE_EXITED
                        || k.getID() == MouseEvent.MOUSE_ENTERED
                        || k.getID() == MouseEvent.MOUSE_CLICKED) {
                    k.consume();
                    return;
                }
                boolean pressed = k.getID() == MouseEvent.MOUSE_PRESSED;
                if (pressed && !lastWasPressed) {
                    sheet.getTemporaryNotes().addElement(mp3.getPosition());
                }
                if (!pressed && lastWasPressed) {
                    sheet.getTemporaryNotes().addElement(mp3.getPosition());
                    added = true;
                }
                lastWasPressed = pressed;
                k.consume();
            }
            if (added) {
                if (sheet.getTemporaryNotes().size() >= 2 * recordLength) {
                    stopRecording();
                }
            }
        }
    }

    private class OwnerFrame extends JFrame {
        OwnerFrame() {
            setIconImage(new ImageIcon(YassActions.this.getClass().getResource("/yass/resources/img/yass-icon-16.png")).getImage());
        }

        // This frame can never be shown.
        @SuppressWarnings("deprecation")
        public void show() {
        }
    }
    // http://java.sun.com/products/jfc/tsc/special_report/kestrel/keybindings.html
    // http://java.sun.com/docs/books/tutorial/uiswing/misc/keybinding.html
}
