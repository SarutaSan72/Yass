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

import javazoom.spi.vorbis.sampled.file.VorbisFileFormatType;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import javax.media.Controller;
import javax.media.MediaLocator;
import javax.media.protocol.DataSource;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassSongInfo extends JPanel implements DropTargetListener {
    private static final long serialVersionUID = -9041213163254101396L;
    /**
     * Description of the Field
     */
    public final int NONE = 0;
    /**
     * Description of the Field
     */
    public final int BACKGROUND = 1;
    /**
     * Description of the Field
     */
    public final int COVER = 2;
    /**
     * Description of the Field
     */
    public final int VIDEO = 3;
    /**
     * Description of the Field
     */
    public final int LYRICS = 4;
    /**
     * Description of the Field
     */
    public final int SHOW_NONE = 0;
    private int infoMode = SHOW_NONE;
    /**
     * Description of the Field
     */
    public final int SHOW_FILES = 1;
    /**
     * Description of the Field
     */
    public final int SHOW_LYRICS = 2;
    /**
     * Description of the Field
     */
    public final int SHOW_ERRORS = 3;
    protected boolean hiliteTitleRect = false;
    YassProperties prop;
    YassSong song = null;
    YassActions actions = null;
    BufferedImage origco = null, origbg = null, imgco = null, imgbg = null;
    JEditorPane txt = null;
    String template = "";
    JScrollPane scroll = null;
    Image fadebg = null, curbg = null;
    Action copyCoverSongInfo = new AbstractAction(I18.get("lib_copy_cover")) {
        private static final long serialVersionUID = 6596469459427208563L;

        public void actionPerformed(ActionEvent e) {
            copyCover();
        }
    };
    Action copyLyricsSongInfo = new AbstractAction(I18.get("lib_copy_lyrics")) {
        private static final long serialVersionUID = -4907138676752964328L;

        public void actionPerformed(ActionEvent e) {
            copyLyrics();
        }
    };
    Action copyBackgroundSongInfo = new AbstractAction(
            I18.get("lib_copy_background")) {
        private static final long serialVersionUID = 2404221301071286281L;

        public void actionPerformed(ActionEvent e) {
            copyBackground();
        }
    };
    Action copyVideoSongInfo = new AbstractAction(I18.get("lib_copy_video")) {
        private static final long serialVersionUID = -8957550925496316614L;

        public void actionPerformed(ActionEvent e) {
            copyVideo();
        }
    };
    Action pasteSongInfo = new AbstractAction(I18.get("lib_paste_data")) {
        private static final long serialVersionUID = -6023118973603374571L;

        public void actionPerformed(ActionEvent e) {
            paste();
        }
    };
    Action editPreviewStart = new AbstractAction(I18.get("lib_previewstart")) {
        private static final long serialVersionUID = 6118683730246393909L;

        public void actionPerformed(ActionEvent e) {
            actions.setPreviewStart();
        }
    };
    Action startPlaying = new AbstractAction(I18.get("lib_play_song")) {
        private static final long serialVersionUID = -5901642092940901086L;

        public void actionPerformed(ActionEvent e) {
            actions.playSong();
        }
    };
    Action editMedleyStartEnd = new AbstractAction(
            I18.get("lib_medleystartend")) {
        private static final long serialVersionUID = 2177037933845333920L;

        public void actionPerformed(ActionEvent e) {
            actions.setMedleyStartEnd();
        }
    };
    Action openSongFolder = new AbstractAction(I18.get("lib_open_folder")) {
        private static final long serialVersionUID = -4154556762419745007L;

        public void actionPerformed(ActionEvent e) {
            actions.openSongFolder();
        }
    };
    Font smallfont = new Font("SansSerif", Font.BOLD, 10);
    Font font = new Font("SansSerif", Font.PLAIN, 11);
    Font bigfont = new Font("SansSerif", Font.BOLD, 12);
    Rectangle corect = new Rectangle(), bgrect = new Rectangle(),
            vdrect = new Rectangle();
    Rectangle titlerect = new Rectangle();
    LoaderThread loader = null;
    javax.media.Player mediaPlayer = null;
    Component video = null;
    boolean closed = true;
    JDialog updateDialog = null;
    Vector<JCheckBox> checks = null;
    YassTable check1 = null, check2 = null;
    private Image standardbg = null;
    private String coString = null;
    private String bgString = null;
    private long duration = -1;
    private boolean hasvideo = false;
    private Color selColor = UIManager.getColor("Table.selectionBackground");
    private Color bgColor = new JLabel().getBackground();
    private Color blue = new Color(.3f, .3f, .6f);
    private Color hiBlue = new Color(.4f, .4f, .7f);
    private Color color1 = new Color(255, 255, 255, 150);
    private Color color2 = new Color(255, 255, 255, 200);
    private Properties info = new Properties();
    private Image videoIcon = null;
    private String copiedVideoGap = null;
    private float alpha = 0;
    private boolean showChildren = true;
    private String nobgString = null;
    private int inout[] = new int[2];
    private String speedLabel, melodicLabel, bumpyLabel, leapsLabel,
            holdsLabel;

	/*
	 * public void openCover() { FileDialog fd = new FileDialog((JFrame)
	 * SwingUtilities.getWindowAncestor(this),
	 * "Please choose your cover image:", FileDialog.LOAD); String defDir =
	 * prop.getProperty("song-directory"); if (defDir != null) {
	 * fd.setDirectory(defDir); } fd.setVisible(true); if (fd.getFile() != null) {
	 * setCover(fd.getDirectory() + File.separator + fd.getFile());
	 * storeAction.setEnabled(true); } fd.dispose(); }
	 */
    private JPopupMenu copopup, bgpopup, vdpopup, txtpopup, filepopup;
    private boolean layoutIsLyrics = false;
    private Action storeAction = null, copyAction = null, pasteAction = null,
            reloadAction = null, reloadAllAction = null,
            copyLyricsAction = null, copyCoverAction = null,
            copyBackgroundAction = null, copyVideoAction = null;
    private boolean preventLoad = false;
    private String bold = null;
    private JComponent dropTarget = null;

    /**
     * Constructor for the YassSongInfo object
     *
     * @param p Description of the Parameter
     * @param a Description of the Parameter
     */
    public YassSongInfo(YassProperties p, YassActions a) {
        super(true);

        prop = p;
        actions = a;

        setLayout(null);
        setOpaque(true);

        // JUKEBOX
        // standardbg = new
        // ImageIcon(getClass().getResource("/yass-bg/83606453.jpg")).getImage();
        // MACHINE
        // standardbg = new
        // ImageIcon(getClass().getResource("/yass-bg/812396190.jpg")).getImage();

        txt = new JEditorPane();
        add(scroll = new JScrollPane(txt));
        scroll.setBounds(700, 10, 300, 400);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        txt.setOpaque(false);
        txt.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        txt.setEditable(false);
        txt.setContentType("text/html");
        txt.setOpaque(false);

        inout[0] = inout[1] = -1;
        txt.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                int in = txt.getSelectionStart();
                int out = txt.getSelectionEnd();
                if (in >= out) {
                    inout[0] = inout[1] = -1;
                    return;
                }

                String s = "";
                try {
                    s = txt.getDocument().getText(0,
                            txt.getDocument().getLength());
                } catch (Exception ignored) {
                }

                int count = 0;
                for (int i = -1; (i = s.substring(0, in).indexOf("\n", i + 1)) != -1; count++) {
                }
                inout[0] = count;

                count = 0;
                for (int i = -1; (i = s.substring(in, out).indexOf("\n", i + 1)) != -1; count++) {
                }
                inout[1] = inout[0] + count;
            }

        });

        DefaultCaret caret = (DefaultCaret) txt.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        txt.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    URL url = e.getURL();
                    String s = url.toExternalForm();
                    s = s.substring(7);
                    switch (s) {
                        case "txt":
                            s = getProperty("full-txt-path");
                            break;
                        case "co":
                            s = getProperty("full-co-path");
                            break;
                        case "bg":
                            s = getProperty("full-bg-path");
                            break;
                        case "mp3":
                            s = getProperty("full-mp3-path");
                            break;
                        case "vd":
                            s = getProperty("full-vd-path");
                            break;
                        case "dir":
                            s = getProperty("full-dir-path");
                            break;
                    }
                    // System.out.println(s);
                    YassActions.openURLFile(s);
                }
            }
        });

        videoIcon = new ImageIcon(getClass().getResource("/yass/resources/img/Movie32.gif"))
                .getImage();

        InputStreamReader isr = null;
        BufferedReader reader = null;
        try {
            URL url = I18.getResource("SongInfoTemplate.html");
            reader = new BufferedReader(isr = new InputStreamReader(
                    url.openStream()));
            StringBuilder sb = new StringBuilder();
            String nextLine = reader.readLine();
            while (nextLine != null) {
                sb.append(nextLine);
                nextLine = reader.readLine();
            }
            template = sb.toString();
        } catch (Exception ignored) {
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception ignored) {
            }
        }

        copopup = new JPopupMenu();
        copopup.add(new JMenuItem(copyCoverSongInfo));
        copopup.add(new JMenuItem(pasteSongInfo));

        bgpopup = new JPopupMenu();
        bgpopup.add(new JMenuItem(copyBackgroundSongInfo));
        bgpopup.add(new JMenuItem(pasteSongInfo));
        vdpopup = new JPopupMenu();
        vdpopup.add(new JMenuItem(copyVideoSongInfo));
        vdpopup.add(new JMenuItem(pasteSongInfo));
        txtpopup = new JPopupMenu();
        txtpopup.add(new JMenuItem(startPlaying));
        txtpopup.add(new JMenuItem(editPreviewStart));
        txtpopup.add(new JMenuItem(editMedleyStartEnd));
        txtpopup.addSeparator();
        txtpopup.add(new JMenuItem(copyLyricsSongInfo));
        txtpopup.add(new JMenuItem(pasteSongInfo));

        // txtpopup.add(m = new JMenuItem(copyCoverAction));
        filepopup = new JPopupMenu();
        filepopup.add(new JMenuItem(openSongFolder));
        filepopup.add(new JMenuItem(pasteSongInfo));
        // filepopup.add(m = new JMenuItem(copyCoverAction));

        addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {

            }

            public void focusLost(FocusEvent e) {

            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();

                boolean isPopup = e.isPopupTrigger()
                        || (e.getModifiers() & InputEvent.BUTTON1_MASK) == 0;
                if (isPopup && actions.isLibraryLoaded()) {
                    if (corect.contains(p)) {
                        copopup.show(e.getComponent(), e.getX(), e.getY());
                    } else if (hasvideo && vdrect.contains(p)) {
                        vdpopup.show(e.getComponent(), e.getX(), e.getY());
                    } else if (bgrect.contains(p)) {
                        bgpopup.show(e.getComponent(), e.getX(), e.getY());
                    }
                    return;
                }

                if (showChildren && titlerect.contains(p)) {
                    actions.toggleSongInfo();
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();

                if (showChildren) {
                    boolean newHiliteTitleRect = titlerect.contains(p);
                    boolean changed = newHiliteTitleRect != hiliteTitleRect;
                    if (changed) {
                        hiliteTitleRect = newHiliteTitleRect;
                        repaint();
                    }
                }
            }
        });
        txt.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                if (showChildren) {
                    boolean changed = hiliteTitleRect;
                    if (changed) {
                        hiliteTitleRect = false;
                        repaint();
                    }
                }
            }
        });
        txt.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                boolean isPopup = e.isPopupTrigger()
                        || (e.getModifiers() & InputEvent.BUTTON1_MASK) == 0;
                if (isPopup && actions.isLibraryLoaded()) {
                    if (infoMode == SHOW_LYRICS) {
                        txtpopup.show(e.getComponent(), e.getX(), e.getY());
                    } else if (infoMode == SHOW_FILES) {
                        filepopup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        /*
		 * addMouseMotionListener( new MouseMotionAdapter() { public void
		 * mouseExited(MouseEvent e) { titlerectActive = false;
		 * setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); }
		 * public void mouseMoved(MouseEvent e) { Point p = e.getPoint(); if
		 * (titlerect.contains(p) && !titlerectActive) { titlerectActive = true;
		 * setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); } if
		 * (!titlerect.contains(p) && titlerectActive) { titlerectActive =
		 * false; setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		 * } } });
		 */
        setDropTarget(txt);
        setDropTarget(this);

        speedLabel = I18.get("stats_diagram_speed");
        melodicLabel = I18.get("stats_diagram_melodic");
        bumpyLabel = I18.get("stats_diagram_bumpy");
        leapsLabel = I18.get("stats_diagram_leaps");
        holdsLabel = I18.get("stats_diagram_holds");
    }

    /**
     * Description of the Method
     *
     * @param s   Description of the Parameter
     * @param txt Description of the Parameter
     * @param f   Description of the Parameter
     * @return Description of the Return Value
     */
    public static String trim(String s, JComponent txt, Font f) {
        Graphics2D g2 = (Graphics2D) txt.getGraphics();
        if (g2 == null) {
            return s;
        }
        Font of = g2.getFont();
        g2.setFont(f);
        FontMetrics fm = g2.getFontMetrics();

        Dimension d = txt.getSize();
        Insets in = txt.getInsets();
        int w = d.width - in.left - in.right;

        if (fm.stringWidth(s) < w) {
            return s;
        }

        int at = s.indexOf("[");
        if (at < 0) {
            at = s.lastIndexOf(".");
        }
        if (at < 0) {
            at = s.length();
        }

        String trim = s.substring(0, at);
        String ext = s.substring(at);
        while (trim.length() > 1 && fm.stringWidth(trim + "~" + ext) > w) {
            trim = trim.substring(0, trim.length() - 1);
        }
        g2.setFont(of);
        return trim + "~" + ext;
    }

    /**
     * Gets the property attribute of the YassSongInfo object
     *
     * @param key Description of the Parameter
     * @return The property value
     */
    public String getProperty(String key) {
        String p = (String) info.get(key);
        if (p == null) {
            return "";
        }
        return p;
    }

    /**
     * Sets the property attribute of the YassSongInfo object
     *
     * @param key The new property value
     * @param val The new property value
     */
    public void setProperty(String key, String val) {
        if (val == null) {
            val = "";
        }
        info.put(key, val);
    }

    /**
     * Description of the Method
     *
     * @param mode Description of the Parameter
     */
    public void show(int mode) {
        infoMode = mode;
        replaceInfo();
        updateLayout();
        repaint();
    }

    /**
     * Description of the Method
     */
    public void updateLayout() {
        if (infoMode == SHOW_LYRICS) {
            if (!layoutIsLyrics) {
                scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                // validate();
                layoutIsLyrics = true;
            }
        } else {
            if (layoutIsLyrics) {
                scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                // validate();

                layoutIsLyrics = false;
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param h Description of the Parameter
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     * @param w Description of the Parameter
     */
    public void updateTextBounds(int x, int y, int w, int h) {
        scroll.setBounds(x, y, w, h);
    }

    /**
     * Gets the inOut attribute of the YassSongInfo object
     *
     * @return The inOut value
     */
    public int[] getInOut() {
        return inout;
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public void paintComponent(Graphics g) {
        fade(g);
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public void fade(Graphics g) {
        paintImage(g);
        if (infoMode != SHOW_NONE) {
            setOpaque(false);
            super.paintComponent(g);
            setOpaque(true);
        }

        boolean onoff;
        try {
            onoff = Toolkit.getDefaultToolkit().getSystemClipboard()
                    .getContents(null) != null;
        } catch (IllegalStateException ex) {
            onoff = true;// used by another app
        }

        if (onoff && (song != null) && !pasteAction.isEnabled()) {
            pasteAction.setEnabled(true);
        }
        if ((song == null || !onoff) && pasteAction.isEnabled()) {
            pasteAction.setEnabled(false);
        }
    }

    /**
     * Description of the Method
     *
     * @param onoff The new childrenVisible value
     */
    public void showChildren(boolean onoff) {
        showChildren = onoff;

        Component[] comps = getComponents();
        for (Component comp : comps) {
            comp.setVisible(onoff);
        }
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public synchronized void paintImage(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        Dimension d = getSize();

        Dimension td = scroll.getSize();
        int tx = scroll.getX();
        int ty = scroll.getY();

        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, d.width, d.height);

        int dw = getWidth();
        int dh = getHeight();

        int h = (int) (dw * 3 / 4.0);

        // correct zoom would be:
		/*
		 * if (dw > (int) (dh * 4 / 3.0)) { w = (int) dw; h = (int) (w * 3 /
		 * 4.0); } else { h = dh; w = (int) (h * 4 / 3.0); }
		 */
        int xx = 0;
        int yy = dh / 2 - h / 2;

        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, dw, dh);

        if (song == null) {
            curbg = imgbg = null;
        }

        if (alpha >= 1 && curbg != null) {
            g2d.drawImage(curbg, xx, yy, dw, h, null);
        } else if (alpha > 0 && alpha < 1 && imgbg != null) {
            Composite oldComposite = g2d.getComposite();

            if (fadebg != null) {
                g2d.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 1));
                g2d.drawImage(fadebg, xx, yy, dw, h, null);
            }

            g2d.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, Math.max(Math.min(alpha, 1), 0)));
            g2d.drawImage(curbg = imgbg, xx, yy, dw, h, null);
            g2d.setComposite(oldComposite);
        } else {
            curbg = null;
            if (standardbg != null) {
                g2d.drawImage(curbg = standardbg, xx, yy, dw, h, null);
            }
            g2d.translate(dw / 2, dh / 2);
            g2d.rotate(-Math.PI / 6);

            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.setFont(new Font("SansSerif", Font.BOLD, dw / 12));
            if (nobgString == null) {
                nobgString = I18.get("lib_nobg");
            }
            FontMetrics metrics = g2d.getFontMetrics();
            int strw = metrics.stringWidth(nobgString);
            int strh = metrics.getHeight();
            g2d.drawString(nobgString, -strw / 2, strh / 2);

            g2d.rotate(Math.PI / 6);
            g2d.translate(-dw / 2, -dh / 2);
        }

        if (song == null) {
            return;
        }

        bgrect.setBounds(0, yy, dw, h);

        g2d.setColor(Color.black);
        g2d.setFont(font);

        int cow = 250;

        int ix = d.width - cow - 10;
        if (imgco != null && infoMode != NONE) {
            corect.setBounds(ix, 10, cow, cow);
            if (showChildren) {
                g2d.setStroke(new BasicStroke(2f));
                g2d.setColor(Color.white);
                g2d.drawImage(imgco, corect.x, corect.y, null);
                g2d.draw(corect);
            }
        }

        String s;

        if (infoMode == SHOW_FILES && scroll.isVisible()) {
            GradientPaint gp = new GradientPaint(tx, ty, color1, td.width,
                    td.height, color2);
            g2d.setPaint(gp);
            g2d.fillRect(tx + 2, ty, td.width - 3, td.height);

            g2d.setColor(selColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.fillRect(tx + 2, ty, td.width - 3, 24);
            g2d.setColor(blue);
            g2d.drawRect(tx + 2, ty, td.width - 3, td.height);
        } else if (infoMode == SHOW_LYRICS && scroll.isVisible()) {
            GradientPaint gp = new GradientPaint(tx, ty, color1, td.width,
                    td.height, color2);
            g2d.setPaint(gp);
            g2d.fillRect(tx + 2, ty, td.width - 3, td.height);

            g2d.setColor(blue);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(tx + 2, ty, td.width - 3, td.height);
        } else if (infoMode == SHOW_ERRORS) {
            g2d.setFont(font);

            g2d.setColor(new Color(1, 1, 1, 0.8f));
            g2d.fillRect(tx + 10, ty + 10, ix - 20, td.height - 70);

            g2d.setColor(new Color(.3f, .3f, .6f, 0.8f));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(tx + 11, ty + 11, ix - 22, td.height - 72);
            g2d.drawImage(YassSongList.err_major_icon, tx + 20, ty + 40, null);
            g2d.drawImage(YassSongList.err_tags_icon, tx + 20, ty + 60, null);
            g2d.drawImage(YassSongList.err_file_icon, tx + 20, ty + 80, null);
            g2d.drawImage(YassSongList.err_minorpage_icon, tx + 20, ty + 100,
                    null);
            g2d.drawImage(YassSongList.err_text_icon, tx + 20, ty + 120, null);
        }

        if (!showChildren) {
            return;
        }

        d = getSize();

        titlerect.setBounds(corect.x, corect.y + corect.height + 10, cow - 1,
                33);

        g2d.setColor(hiliteTitleRect ? hiBlue : blue);
        g2d.fillRect(titlerect.x, titlerect.y, titlerect.width,
                titlerect.height);

        // g2d.translate(titlerect.x + titlerect.width, titlerect.y +
        // titlerect.height / 2);
        // g2d.rotate(-Math.PI / 2);

        g2d.setColor(Color.white);
        g2d.setFont(bigfont);
        FontMetrics metrics;
        s = song.getTitle();
        int strw;
        g2d.drawString(s, titlerect.x + 5, titlerect.y + titlerect.height - 18);

        g2d.setFont(font);
        metrics = g2d.getFontMetrics();
        s = song.getArtist();
        g2d.drawString(s, titlerect.x + 5, titlerect.y + titlerect.height - 4);

        // g2d.rotate(Math.PI / 2);
        // g2d.translate(-(titlerect.x + titlerect.width), -(titlerect.y +
        // titlerect.height / 2));

        vdrect.setBounds(titlerect.x + titlerect.width - 32, titlerect.y, 32,
                32);
        if (hasvideo && videoIcon != null) {
            g2d.drawImage(videoIcon, vdrect.x, vdrect.y, null);
        }
        if (duration > 0) {
            int sec = (int) Math.round(duration / 1000000.0);
            int min = sec / 60;
            sec = sec - min * 60;
            String dString = (sec < 10) ? min + ":0" + sec : min + ":" + sec;
            strw = metrics.stringWidth(dString);
            int dx = titlerect.x + titlerect.width - 5 - strw;
            if (hasvideo && videoIcon != null) {
                dx -= 32;
            }
            g.drawString(dString, dx, titlerect.y + titlerect.height - 4);
        }

        int statSize = 200;
        int offx = 100;
        int offy = 30;

        int maxleftx = 0;
        Component comp[] = getComponents();
        for (Component aComp : comp) {
            Rectangle bc = aComp.getBounds();
            if (bc.x < corect.x) {
                maxleftx = Math.max(maxleftx, bc.x + bc.width);
            }
        }
        statSize = Math.min(statSize, corect.x - maxleftx);

        if (statSize > 50) {
            g2d.setColor(selColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(corect.x - statSize - offx,
                    d.height - statSize - offy, statSize, statSize);

            int statSize8 = statSize / 8;
            g2d.setColor(color1);
            g2d.fillOval(corect.x - statSize / 2 - offx - statSize8, d.height
                            - statSize / 2 - offy - statSize8, statSize8 * 2,
                    statSize8 * 2);

            g2d.setColor(color1);
            g2d.setStroke(new BasicStroke(statSize8));
            g2d.drawOval(corect.x - statSize / 2 - offx - 2 * statSize8
                    - statSize8 / 2, d.height - statSize / 2 - offy - 2
                    * statSize8 - statSize8 / 2, 5 * statSize8, 5 * statSize8);

            int speedlenIndex = yass.stats.YassStats.indexOf("speedlen");
            float speedlen = song.getStatsAt(speedlenIndex);
            int speeddistIndex = yass.stats.YassStats.indexOf("speeddist");
            float speeddist = song.getStatsAt(speeddistIndex);
            float speed = Math.min(1, 0.5f * speeddist / 7f + 0.5f * speedlen
                    / 8f);

            int pitchrangepageIndex = yass.stats.YassStats
                    .indexOf("pitchrangepage");
            float melodic = song.getStatsAt(pitchrangepageIndex);
            melodic = Math.min(1, melodic / 8f);

            int pitchdistanceIndex = yass.stats.YassStats
                    .indexOf("pitchdistance");
            float bumpy = song.getStatsAt(pitchdistanceIndex);
            bumpy = Math.min(1, bumpy / 3f);

            int pitchleaps3Index = yass.stats.YassStats.indexOf("pitchleaps3");
            float pitchleaps3 = song.getStatsAt(pitchleaps3Index);
            int pitchleaps6Index = yass.stats.YassStats.indexOf("pitchleaps6");
            float pitchleaps6 = song.getStatsAt(pitchleaps6Index);
            float leaps = Math.min(1, .5f * pitchleaps3 / 25f + .5f
                    * pitchleaps6 / 10f);

            int holds1secIndex = yass.stats.YassStats.indexOf("holds1sec");
            //float holds1sec = song.getStatsAt(holds1secIndex);
            int holds3secIndex = yass.stats.YassStats.indexOf("holds3sec");
            float holds3sec = song.getStatsAt(holds3secIndex);
            float holds = Math.min(1, .5f * holds1secIndex / 15f + .5f
                    * holds3sec / 2f);

            g2d.setStroke(new BasicStroke(2));
            Polygon p = new Polygon();
            Polygon p2 = new Polygon();

            int mx = corect.x - statSize / 2 - offx;
            int my = d.height - statSize / 2 - offy;
            int r = statSize / 2;
            int num = 5;
            double sec = 2 * Math.PI / (float) num;

            int polx = (int) (mx + leaps * r * Math.cos(sec));
            int poly = (int) (my + leaps * r * Math.sin(sec));
            int maxx = (int) (mx + r * Math.cos(sec));
            int maxy = (int) (my + r * Math.sin(sec));
            int maxx2 = (int) (mx + 1.1 * r * Math.cos(sec));
            int maxy2 = (int) (my + 1.1 * r * Math.sin(sec));
            g2d.setColor(blue);
            g2d.drawLine(mx, my, maxx, maxy);
            g2d.setFont(bigfont);
            metrics = g2d.getFontMetrics();
            String label = leapsLabel;
            int llen = metrics.stringWidth(label);
            g2d.setColor(color1);
            g2d.fillRoundRect(maxx2 - 2, maxy2 - 6, llen + 3, 14, 4, 4);
            g2d.setColor(blue);
            g2d.drawString(label, maxx2, maxy2 + 6);
            p.addPoint(polx, poly);
            p2.addPoint(maxx, maxy);

            polx = (int) (mx + bumpy * r * Math.cos(2 * sec));
            poly = (int) (my + bumpy * r * Math.sin(2 * sec));
            maxx = (int) (mx + r * Math.cos(2 * sec));
            maxy = (int) (my + r * Math.sin(2 * sec));
            maxx2 = (int) (mx + 1.1 * r * Math.cos(2 * sec));
            maxy2 = (int) (my + 1.1 * r * Math.sin(2 * sec));
            g2d.drawLine(mx, my, maxx, maxy);
            label = bumpyLabel;
            llen = metrics.stringWidth(label);
            g2d.setColor(color1);
            g2d.fillRoundRect(maxx2 - llen - 2, maxy2 - 6, llen + 3, 14, 4, 4);
            g2d.setColor(blue);
            g2d.drawString(label, maxx2 - llen, maxy2 + 6);
            p.addPoint(polx, poly);
            p2.addPoint(maxx, maxy);

            polx = (int) (mx + melodic * r * Math.cos(3 * sec));
            poly = (int) (my + melodic * r * Math.sin(3 * sec));
            maxx = (int) (mx + r * Math.cos(3 * sec));
            maxy = (int) (my + r * Math.sin(3 * sec));
            maxx2 = (int) (mx + 1.1 * r * Math.cos(3 * sec));
            maxy2 = (int) (my + 1.1 * r * Math.sin(3 * sec));
            g2d.drawLine(mx, my, maxx, maxy);
            label = melodicLabel;
            llen = metrics.stringWidth(label);
            g2d.setColor(color1);
            g2d.fillRoundRect(maxx2 - 2 - llen, maxy2 - 6, llen + 3, 14, 4, 4);
            g2d.setColor(blue);
            g2d.drawString(label, maxx2 - llen, maxy2 + 6);
            p.addPoint(polx, poly);
            p2.addPoint(maxx, maxy);

            polx = (int) (mx + speed * r * Math.cos(4 * sec));
            poly = (int) (my + speed * r * Math.sin(4 * sec));
            maxx = (int) (mx + r * Math.cos(4 * sec));
            maxy = (int) (my + r * Math.sin(4 * sec));
            maxx2 = (int) (mx + 1.1 * r * Math.cos(4 * sec));
            maxy2 = (int) (my + 1.1 * r * Math.sin(4 * sec));
            g2d.drawLine(mx, my, maxx, maxy);
            label = speedLabel;
            llen = metrics.stringWidth(label);
            g2d.setColor(color1);
            g2d.fillRoundRect(maxx2 - 2, maxy2 - 6, llen + 3, 14, 4, 4);
            g2d.setColor(blue);
            g2d.drawString(label, maxx2, maxy2 + 6);
            p.addPoint(polx, poly);
            p2.addPoint(maxx, maxy);

            polx = (int) (mx + holds * r * Math.cos(5 * sec));
            poly = (int) (my + holds * r * Math.sin(5 * sec));
            maxx = (int) (mx + r * Math.cos(5 * sec));
            maxy = (int) (my + r * Math.sin(5 * sec));
            maxx2 = (int) (mx + 1.1 * r * Math.cos(5 * sec));
            maxy2 = (int) (my + 1.1 * r * Math.sin(5 * sec));
            g2d.drawLine(mx, my, maxx, maxy);
            label = holdsLabel;
            llen = metrics.stringWidth(label);
            g2d.setColor(color1);
            g2d.fillRoundRect(maxx2 - 2, maxy2 - 6, llen + 3, 14, 4, 4);
            g2d.setColor(blue);
            g2d.drawString(label, maxx2, maxy2 + 6);
            p.addPoint(polx, poly);
            p2.addPoint(maxx, maxy);

            g2d.drawPolygon(p2);

            if (speedlen >= 0) {
                g2d.setColor(new Color(.3f, .3f, .6f, .5f));
                g2d.fillPolygon(p);
                g2d.setColor(blue);
                g2d.drawPolygon(p);
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param local Description of the Parameter
     */
    public void backupCover(File local) {
        String tmp = prop.getProperty("temp-dir");
        File tmpfile = new File(tmp);
        if (!tmpfile.exists()) {
            tmpfile.mkdirs();
        }

        String title = YassSong.toFilename(song.getTitle());
        String artist = YassSong.toFilename(song.getArtist());
        String version = YassSong.toFilename(song.getVersion());
        String folder = song.getFolder();
        String filename = tmp + File.separator + artist + " - " + title + " ["
                + version + "] [CO] @ " + folder + ".jpg";

        File file = new File(filename);
        if (local == null) {
            try {
                javax.imageio.ImageIO.write(origco, "jpg", file);
            } catch (Exception ignored) {
            }
        } else {
            YassUtils.copyFile(local, file);
        }
    }

    /**
     * Gets the coverBackupFile attribute of the YassSongInfo object
     *
     * @return The coverBackupFile value
     */
    public File getCoverBackupFile() {
        String tmp = prop.getProperty("temp-dir");
        File tmpfile = new File(tmp);
        if (!tmpfile.exists()) {
            tmpfile.mkdirs();
        }

        String title = YassSong.toFilename(song.getTitle());
        String artist = YassSong.toFilename(song.getArtist());
        String version = YassSong.toFilename(song.getVersion());
        String folder = song.getFolder();
        String filename = tmp + File.separator + artist + " - " + title + " ["
                + version + "] [CO] @ " + folder + ".jpg";

        return new File(filename);
    }

    /**
     * Gets the coverBackup attribute of the YassSongInfo object
     *
     * @return The coverBackup value
     */
    public Image getCoverBackup() {
        File file = getCoverBackupFile();
        if (file.exists()) {
            BufferedImage img = null;
            try {
                img = YassUtils.readImage(file);
            } catch (Exception ignored) {
            }
            return img;
        }
        return null;
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     */
    public void removeBackup(YassSong s) {
        String tmp = prop.getProperty("temp-dir");
        File tmpfile = new File(tmp);
        if (!tmpfile.exists()) {
            tmpfile.mkdirs();
        }

        String title = YassSong.toFilename(s.getTitle());
        String artist = YassSong.toFilename(s.getArtist());
        String version = YassSong.toFilename(s.getVersion());
        String folder = s.getFolder();

        String filename = tmp + File.separator + artist + " - " + title + " ["
                + version + "] [BG] @ " + folder + ".jpg";
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }

        filename = tmp + File.separator + artist + " - " + title + " ["
                + version + "] [CO] @ " + folder + ".jpg";
        file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Gets the backgroundBackupFile attribute of the YassSongInfo object
     *
     * @return The backgroundBackupFile value
     */
    public File getBackgroundBackupFile() {
        String tmp = prop.getProperty("temp-dir");
        File tmpfile = new File(tmp);
        if (!tmpfile.exists()) {
            tmpfile.mkdirs();
        }

        String title = YassSong.toFilename(song.getTitle());
        String artist = YassSong.toFilename(song.getArtist());
        String version = YassSong.toFilename(song.getVersion());
        String folder = song.getFolder();
        String filename = tmp + File.separator + artist + " - " + title + " ["
                + version + "] [BG] @ " + folder + ".jpg";

        return new File(filename);
    }

    /**
     * Gets the videoBackupFile attribute of the YassSongInfo object
     *
     * @return The videoBackupFile value
     */
    public File getVideoBackupFile() {
        String tmp = prop.getProperty("temp-dir");
        File tmpfile = new File(tmp);
        if (!tmpfile.exists()) {
            tmpfile.mkdirs();
        }

        String title = YassSong.toFilename(song.getTitle());
        String artist = YassSong.toFilename(song.getArtist());
        String version = YassSong.toFilename(song.getVersion());
        String folder = song.getFolder();

        String at = artist + " - " + title + " [" + version + "] [VD#";
        File files[] = tmpfile.listFiles();
        for (File file : files) {
            String name = file.getName();
            if (name.startsWith(at)) {
                if (name.indexOf("@ " + folder + ".") > 0) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * Gets the backgroundBackup attribute of the YassSongInfo object
     *
     * @return The backgroundBackup value
     */
    public Image getBackgroundBackup() {
        File file = getBackgroundBackupFile();
        if (file.exists()) {
            BufferedImage img = null;
            try {
                img = YassUtils.readImage(file);
            } catch (Exception ignored) {
            }
            return img;
        }
        return null;
    }

    /**
     * Description of the Method
     *
     * @param local Description of the Parameter
     */
    public void backupBackground(File local) {
        String tmp = prop.getProperty("temp-dir");
        File tmpfile = new File(tmp);
        if (!tmpfile.exists()) {
            tmpfile.mkdirs();
        }

        String title = YassSong.toFilename(song.getTitle());
        String artist = YassSong.toFilename(song.getArtist());
        String v = song.getVersion();
        String version = YassSong.toFilename(v);
        String folder = song.getFolder();
        String filename = tmp + File.separator + artist + " - " + title + " ["
                + version + "] [BG] @ " + folder + ".jpg";

        File file = new File(filename);
        if (local == null) {
            try {
                javax.imageio.ImageIO.write(origbg, "jpg", file);
            } catch (Exception ignored) {
            }
        } else {
            YassUtils.copyFile(local, file);
        }
    }

    /**
     * Description of the Method
     *
     * @param local Description of the Parameter
     * @param vg    Description of the Parameter
     */
    public void backupVideo(File local, String vg) {
        String tmp = prop.getProperty("temp-dir");
        File tmpfile = new File(tmp);
        if (!tmpfile.exists()) {
            tmpfile.mkdirs();
        }

        String title = YassSong.toFilename(song.getTitle());
        String artist = YassSong.toFilename(song.getArtist());
        String v = song.getVersion();
        String version = YassSong.toFilename(v);
        String folder = song.getFolder();

        String name = local.getName();
        int i = name.lastIndexOf(".");
        String ext = name.substring(i);

        if (vg == null) {
            String videoID = prop.getProperty("video-id");
            vg = YassUtils.getWildcard(name, videoID.toLowerCase());
            if (vg == null) {
                vg = "0";
            }
        }

        String filename = tmp + File.separator + artist + " - " + title + " ["
                + version + "] [VD#" + vg + "] @ " + folder + ext;
        File file = new File(filename);
        YassUtils.copyFile(local, file);
    }

    /**
     * Sets the storeAction attribute of the YassSongInfo object
     *
     * @param a The new storeAction value
     */
    public void setStoreAction(Action a) {
        storeAction = a;
    }

    /**
     * Sets the reloadAction attribute of the YassSongInfo object
     *
     * @param r   The new reloadAction value
     * @param all The new reloadAction value
     */
    public void setReloadAction(Action r, Action all) {
        reloadAction = r;
        reloadAllAction = all;
    }

    /**
     * Sets the copyAction attribute of the YassSongInfo object
     *
     * @param a The new copyAction value
     */
    public void setCopyAction(Action a) {
        copyAction = a;
    }

    /**
     * Sets the copyAction attribute of the YassSongInfo object
     *
     * @param co  The new copyAction value
     * @param bg  The new copyAction value
     * @param vd  The new copyAction value
     * @param lyr The new copyAction value
     */
    public void setCopyAction(Action co, Action bg, Action vd, Action lyr) {
        copyCoverAction = co;
        copyBackgroundAction = bg;
        copyVideoAction = vd;
        copyLyricsAction = lyr;
    }

    /**
     * Sets the pasteAction attribute of the YassSongInfo object
     *
     * @param a The new pasteAction value
     */
    public void setPasteAction(Action a) {
        pasteAction = a;
    }

    /**
     * Description of the Method
     */
    public void copyCover() {
        copy(COVER);
    }

    /**
     * Description of the Method
     */
    public void copyLyrics() {
        copy(LYRICS);
    }

    /**
     * Description of the Method
     */
    public void copyBackground() {
        copy(BACKGROUND);
    }

    /**
     * Description of the Method
     */
    public void copyVideo() {
        copy(VIDEO);
    }

    /**
     * Description of the Method
     *
     * @param sel Description of the Parameter
     */
    public void copy(int sel) {
        if (sel == LYRICS) {
            String s = txt.getSelectedText();
            if (s == null) {
                s = song.getLyrics();
            }
            if (s != null && s.length() > 0) {
                StringSelection data = new StringSelection(s);
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(data, data);
                pasteAction.setEnabled(true);
            }
        } else if (sel == COVER) {
            File f;
            if (!song.isSaved()) {
                f = getCoverBackupFile();
                if (f == null) {
                    f = new File(song.getDirectory() + File.separator
                            + song.getCover());
                }
            } else {
                f = new File(song.getDirectory() + File.separator
                        + song.getCover());
            }
            if (f.exists()) {
                FileTransferable.copyToClipboard(f);
                pasteAction.setEnabled(true);
                return;
            }
            if (origco == null) {
                return;
            }
            ImageSelection imgSel = new ImageSelection(origco);
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(imgSel, null);
            pasteAction.setEnabled(true);
        } else if (sel == BACKGROUND) {
            File f;
            if (!song.isSaved()) {
                f = getBackgroundBackupFile();
                if (f == null) {
                    f = new File(song.getDirectory() + File.separator
                            + song.getBackground());
                }
            } else {
                f = new File(song.getDirectory() + File.separator
                        + song.getBackground());
            }
            if (f.exists()) {
                FileTransferable.copyToClipboard(f);
                pasteAction.setEnabled(true);
                return;
            }
            if (origbg == null) {
                return;
            }
            ImageSelection imgSel = new ImageSelection(origbg);
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(imgSel, null);
            pasteAction.setEnabled(true);
        } else if (sel == VIDEO) {
            if (!hasvideo || song == null) {
                return;
            }

            File f = null;
            if (!song.isSaved()) {
                f = getVideoBackupFile();
                if (f == null) {
                    String vd = song.getVideo();
                    if (vd != null && vd.length() > 0) {
                        f = new File(song.getDirectory() + File.separator + vd);
                    }
                }
            } else {
                String vd = song.getVideo();
                if (vd != null && vd.length() > 0) {
                    f = new File(song.getDirectory() + File.separator + vd);
                }
            }
            if (f.exists()) {
                String dir = song.getDirectory();
                String txtname = song.getFilename();
                YassTable t = new YassTable();
                if (new File(dir + File.separator + txtname).exists()) {
                    t.loadFile(dir + File.separator + txtname);
                    String vgap = t.getVideoGap() + "";
                    if (vgap.endsWith(".0")) {
                        vgap = vgap.substring(0, vgap.length() - 2);
                    }
                    copiedVideoGap = vgap;
                } else {
                    copiedVideoGap = null;
                }

                FileTransferable.copyToClipboard(f);
                pasteAction.setEnabled(true);
            }
        }
    }

    /**
     * Description of the Method
     */
    public void paste() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard()
                .getContents(null);

        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                Image im = (Image) t.getTransferData(DataFlavor.imageFlavor);

                int ow = im.getWidth(this);
                int oh = im.getHeight(this);

                boolean isCO = false;

                boolean isBG = false;
                if (ow > 10 && Math.abs(ow - oh) < 50) {
                    isCO = true;
                } else if (ow > oh + 50) {
                    isBG = true;
                }
                if (isCO) {
                    setCover(im);
                    backupCover(null);
                }
                if (isBG) {
                    setBackground(im);
                    backupBackground(null);
                }
                song.setSaved(false);
                reloadAction.setEnabled(true);
                reloadAllAction.setEnabled(true);
                storeAction.setEnabled(true);
            }
        } catch (UnsupportedFlavorException ignored) {
        } catch (IOException ignored) {
        }
        try {
            if (t != null
                    && t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                java.util.List fileList = (java.util.List) t
                        .getTransferData(DataFlavor.javaFileListFlavor);
                if (fileList.isEmpty()) {
                    return;
                }
                Iterator<?> it = fileList.iterator();
                File file = (File) it.next();
                if (!file.exists()) {
                    return;
                }

                String filename = file.getName();
                String filenameLow = filename.toLowerCase();

                boolean isCO = filenameLow.indexOf("[co]") > 0
                        && (filenameLow.endsWith(".jpg") || filenameLow
                        .endsWith(".jpeg"));
                boolean isBG = filenameLow.indexOf("[bg]") > 0
                        && (filenameLow.endsWith(".jpg") || filenameLow
                        .endsWith(".jpeg"));
                boolean isVD = filenameLow.endsWith(".mpg")
                        || filenameLow.endsWith(".mpeg")
                        || filenameLow.endsWith(".avi");
                boolean isTXT = filenameLow.endsWith(".txt");

                BufferedImage img = null;
                if (!isCO
                        && !isBG
                        && (filenameLow.endsWith(".jpg") || filenameLow
                        .endsWith(".jpeg"))) {
                    int ow = 0;
                    int oh = 0;
                    try {
                        img = YassUtils.readImage(file);
                        ow = img.getWidth();
                        oh = img.getHeight();
                    } catch (Exception ignored) {
                    }
                    if (ow > 10 && Math.abs(ow - oh) < 50) {
                        isCO = true;
                    } else if (ow > oh + 50) {
                        isBG = true;
                    }
                } else if (isCO || isBG) {
                    img = YassUtils.readImage(file);
                }

                if (isCO) {
                    setCover(img);
                    backupCover(file);
                }
                if (isBG) {
                    setBackground(img);
                    backupBackground(file);
                }
                if (isTXT) {
                    StringBuffer sb = new StringBuffer();
                    String encoding = null;
                    unicode.UnicodeReader r = null;
                    BufferedReader inputStream = null;
                    FileInputStream fis = null;
                    try {
                        r = new unicode.UnicodeReader(
                                fis = new FileInputStream(file), null);
                        inputStream = new BufferedReader(r);

                        String l;
                        while ((l = inputStream.readLine()) != null) {
                            sb.append(l);
                            sb.append("\n");
                        }
                        encoding = r.getEncoding();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (r != null) {
                            r.close();
                        }
                        if (fis != null) {
                            fis.close();
                        }
                    }

                    String s = sb.toString();
                    if (YassUtils.isValidKaraokeString(s)) {
                        YassTable t1 = new YassTable();
                        t1.setText(s);
                        String newTitle = t1.getTitle().trim();
                        String newArtist = t1.getArtist().trim();
                        String newVersion = t1.getVersion().trim();
                        YassSong newSong = actions.selectSong(newArtist,
                                newTitle, newVersion);
                        if (newSong != null) {
                            song = newSong;
                            updateText(s, encoding);
                        }
                    }
                }

                if (isVD) {
                    backupVideo(file, copiedVideoGap);
                    setVideo(getVideoBackupFile().getPath());
                }

                if (isCO || isBG || isVD) {
                    song.setSaved(false);
                    reloadAction.setEnabled(true);
                    reloadAllAction.setEnabled(true);
                    storeAction.setEnabled(true);
                }
            }
            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                Object td = t.getTransferData(DataFlavor.stringFlavor);
                if (!(td instanceof String)) {
                    repaint();
                    return;
                }

                String tds = (String) td;
                boolean ok = YassUtils.isValidKaraokeString(tds);
                if (!ok) {
                    repaint();
                    return;
                }

                StringTokenizer st = new StringTokenizer(tds, "\n\r");
                YassTable t1 = new YassTable();
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    if (!t1.addRow(s)) {
                        break;
                    }
                }

                String newTitle = t1.getTitle().trim();
                String newArtist = t1.getArtist().trim();
                String newVersion = t1.getVersion().trim();

                YassSong newSong = actions.selectSong(newArtist, newTitle,
                        newVersion);
                if (newSong != null) {
                    song = newSong;
                    updateText(tds, null);
                }
                repaint();
                return;
            }
        } catch (UnsupportedFlavorException ignored) {
        } catch (IOException ignored) {
        }
        setSong(song);
    }

    /**
     * Description of the Method
     */
    public synchronized void clear() {
        if (loader != null) {
            loader.isInterrupted = true;
            int n = 10;
            while (!loader.isFinished && --n > 0) {
                try {
                    Thread.currentThread().wait(100);
                } catch (Exception ignored) {
                }
            }
        }

        song = null;
        txt.setText("");
        inout[0] = inout[1] = -1;
        repaint();
    }

    /**
     * Sets the song attribute of the YassSongInfo object
     *
     * @param s The new song value
     */
    public synchronized void setSong(YassSong s) {
        clear();

        song = s;

        copyAction.setEnabled(false);
        reloadAction.setEnabled(s == null ? false : !s.isSaved());
        storeAction.setEnabled(s == null ? false : !s.isSaved());

        copyCoverAction.setEnabled(false);
        copyLyricsAction.setEnabled(false);
        copyBackgroundAction.setEnabled(false);
        copyVideoAction.setEnabled(false);

        if (s == null) {
            txt.setText("");
            inout[0] = inout[1] = -1;
        }
        if (!isVisible()) {
            return;
        }

        if (preventLoad) {
            return;
        }
        loader = new LoaderThread();
        loader.start();
    }

    /**
     * Description of the Method
     *
     * @param onoff Description of the Parameter
     */
    public void preventLoad(boolean onoff) {
        if (loader != null) {
            loader.isInterrupted = true;
            int n = 10;
            while (!loader.isFinished && --n > 0) {
                try {
                    Thread.currentThread().wait(100);
                } catch (Exception ignored) {
                }
            }
        }
        preventLoad = onoff;
    }

    /**
     * Sets the bold attribute of the YassSongInfo object
     *
     * @param s The new bold value
     */
    public void setBold(String s) {
        bold = s;
        if (bold != null) {
            bold = bold.toLowerCase();
        }
    }

    /**
     * Description of the Method
     */
    public synchronized void replaceInfo() {
        if (infoMode == SHOW_LYRICS) {

            double rowBeat = 0;
            YassTable table = null;
            Vector<YassPage> pages = null;
            if (song != null) {
                table = new YassTable();
                table.loadFile(song.getDirectory() + File.separator
                        + song.getFilename());
                pages = table.getPages();
                if (pages.size() > 0) {
                    Vector<YassRow> rows = pages.get(0).getRows();
                    if (rows.size() > 0) {
                        rowBeat = rows.firstElement().getBeatInt();
                    }
                }
            }

            String s = song != null ? song.getLyrics() : null;
            if (s == null) {
                s = "";
            }

            String psl = getProperty("txt-previewstart");
            if (psl == null) {
                psl = "0";
            }

            String msl = getProperty("txt-medleystartline");
            String mel = getProperty("txt-medleyendline");
            if (msl == null) {
                msl = "0";
            }
            if (mel == null) {
                mel = "0";
            }
            int ms = -1;
            int me = -1;
            double ps;
            int psBeat = 0;
            try {
                ps = Double.parseDouble(psl);
                if (table != null)
                    psBeat = (int) ((ps - table.getGap() / 1000) * 4 * table.getBPM() / 60);
            } catch (Exception ignored) {
            }
            try {
                ms = Integer.parseInt(msl);
                me = Integer.parseInt(mel);
            } catch (Exception ignored) {
            }
            boolean psBeatFound = false;
            String medleyStyle = "<p style='margin-top:0;margin-bottom:0;color:black;background:white;'>";
            String notMedleyStyle = "<p style='margin-top:0;margin-bottom:0;color:black;'>";
            if (rowBeat >= psBeat) {
                s = "\u27A4" + s;
                psBeatFound = true;
            }
            if (ms == 1) {
                s = medleyStyle + s;
            } else {
                s = notMedleyStyle + s;
            }

            int k = s.indexOf('\n');
            int pg = 2;
            while (k > 0) {
                if (pages != null && pg - 1 < pages.size() && !psBeatFound) {
                    Vector<YassRow> rows = pages.get(pg - 1).getRows();
                    if (rows.size() > 0) {
                        rowBeat = rows.firstElement().getBeatInt();
                    }
                }
                String pre = "";
                if (rowBeat >= psBeat && !psBeatFound) {
                    pre = "\u27A4";
                    psBeatFound = true;
                }
                if (pg >= ms && pg <= me) {
                    s = s.substring(0, k) + medleyStyle + pre
                            + s.substring(k + 1);
                } else {
                    s = s.substring(0, k) + notMedleyStyle + pre
                            + s.substring(k + 1);
                }
                k = s.indexOf('\n', k + 10);
                pg++;
            }

            if (bold != null && bold.length() > 0) {
                String low = s.toLowerCase();
                k = 0;
                int blen = bold.length();
                while ((k = low.indexOf(bold, k)) >= 0) {
                    s = s.substring(0, k) + "<b>" + bold + "</b>"
                            + s.substring(k + blen);
                    low = low.substring(0, k) + "<b>" + bold + "</b>"
                            + low.substring(k + blen);
                    k += 3 + blen + 4;
                }
            }

            // s = s.replaceAll("[\n]",
            // "<p style='margin-top:0;margin-bottom:0'>");
            // s = YassUtils.replace(s, "\n", "<br>");

            txt.setText(s);
        } else if (infoMode == SHOW_FILES) {
            String s = template;
            s = YassUtils.replace(s, info);
            txt.setText(s);
        } else {
            txt.setText("");
        }
        inout[0] = inout[1] = -1;
    }

    /**
     * Description of the Method
     */
    public void resetSong() {
        setSong(song);
    }

    /**
     * Description of the Method
     */
    public void stopPlayer() {
        if (mediaPlayer != null) {
            synchronized (mediaPlayer) {
                mediaPlayer.stop();
                mediaPlayer.close();
            }
            int n = 10;
            while (!closed && --n > 0) {
                try {
                    System.out.println("waiting until mediaplayer closed");
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (Exception ignored) {
                }
            }
            closed = true;
        }
    }

    /**
     * Description of the Method
     *
     * @param vdname Description of the Parameter
     */
    public void setVideo(String vdname) {
        if (!YassVideoUtils.useFOBS) {
            setProperty("vd-duration", "");
            setProperty("vd-width", "");
            setProperty("vd-cross", "");
            setProperty("vd-height", "");
            return;
        }
        setProperty("vd-cross", "x");

        vdname = vdname.replace('\\', '/');
        javax.media.Manager.setHint(javax.media.Manager.LIGHTWEIGHT_RENDERER,
                true);
        javax.media.Manager.setHint(javax.media.Manager.PLUGIN_PLAYER, true);
        try {
            // System.out.println("create player: " + vdname);

            if (mediaPlayer != null) {
                synchronized (mediaPlayer) {
                    if (!closed) {
                        mediaPlayer.stop();
                        mediaPlayer.close();
                    }
                    closed = true;
                    MediaLocator ml = new MediaLocator("file:" + vdname);
                    DataSource ds = javax.media.Manager.createDataSource(ml);
                    try {
                        mediaPlayer = javax.media.Manager.createPlayer(ds);
                    } catch (Exception e) {
                        mediaPlayer = null;
                        ds.disconnect();
                    }
                }
            } else {
                MediaLocator ml = new MediaLocator("file:" + vdname);
                DataSource ds = javax.media.Manager.createDataSource(ml);
                try {
                    mediaPlayer = javax.media.Manager.createPlayer(ds);
                } catch (Exception e) {
                    mediaPlayer = null;
                    ds.disconnect();
                }
            }
            if (mediaPlayer == null) {
                return;
            }

            // System.out.println("player created");

            mediaPlayer
                    .addControllerListener(new javax.media.ControllerAdapter() {
                        public void controllerUpdate(
                                javax.media.ControllerEvent event) {
                            if (event instanceof javax.media.ControllerErrorEvent) {
                                if (mediaPlayer != null) {
                                    synchronized (mediaPlayer) {
                                        mediaPlayer.stop();
                                        mediaPlayer.close();
                                    }
                                }
                                closed = true;
                                // System.out.println("ERROR: Video could be realized.");
                            }
                            if (event instanceof javax.media.RealizeCompleteEvent) {
                                closed = false;

                                try {
                                    if (mediaPlayer != null) {
                                        if (mediaPlayer.getState() == Controller.Realized) {
                                            video = mediaPlayer
                                                    .getVisualComponent();
                                        }
                                    }
                                } catch (Exception e) {
                                    video = null;
                                    e.printStackTrace();
                                }
                                // fg =
                                // (javax.media.control.FrameGrabbingControl)
                                // mediaPlayer.getControl("javax.media.control.FrameGrabbingControl");
                                long dur = mediaPlayer.getDuration()
                                        .getNanoseconds();
                                if (dur < 0) {
                                    dur = 0;
                                }
                                int sec = (int) Math.round(dur
                                        / (1000.0 * 1000.0 * 1000.0));
                                int min = sec / 60;
                                sec = sec - min * 60;
                                String dString = (sec < 10) ? min + ":0" + sec
                                        : min + ":" + sec;
                                setProperty("vd-duration", dString);

                                if (video != null) {
                                    int vw = video.getPreferredSize().width;
                                    int vh = video.getPreferredSize().height;
                                    setProperty("vd-width", vw + "");
                                    setProperty("vd-height", vh + "");
                                }

                                replaceInfo();
                                repaint();
                                if (mediaPlayer != null) {
                                    synchronized (mediaPlayer) {
                                        mediaPlayer.stop();
                                        mediaPlayer.close();
                                    }
                                }
                            } else if (event instanceof javax.media.EndOfMediaEvent) {
                                mediaPlayer
                                        .setMediaTime(new javax.media.Time(0));
                            } else if (event instanceof javax.media.ControllerClosedEvent) {
                                closed = true;
                            }
                        }
                    });
            synchronized (mediaPlayer) {
                mediaPlayer.realize();
            }
        } catch (Exception e) {
            video = null;
            closed = true;
            e.printStackTrace();
        }
    }

    /**
     * Sets the cover attribute of the YassSongInfo object
     *
     * @param co The new cover value
     */
    public void setCover(String co) {
        coString = co;

        try {
            File coverFile = new File(coString);
            origco = YassUtils.readImage(coverFile);

            // imgco = YassUtils.getScaledInstance(origco, 300, 300);

            imgco = new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = imgco.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.drawImage(origco, 0, 0, 250, 250, null);
            g2d.dispose();

            setProperty("co-filename", trim(coverFile.getName()));
            setProperty("co-filesize",
                    ((int) (10 * coverFile.length() / 1024.0) / 10.0) + "");
            setProperty("co-width", origco == null ? "" : origco.getWidth()
                    + "");
            setProperty("co-height", origco == null ? "" : origco.getHeight()
                    + "");
        } catch (Exception e) {
            origco = null;
            imgco = null;
            setProperty("co-filename", "");
            setProperty("co-filesize", "");
            setProperty("co-width", "");
            setProperty("co-height", "");
        }
    }

    /**
     * Sets the background attribute of the YassSongInfo object
     *
     * @param bg The new background value
     */
    public void setBackground(String bg) {
        bgString = bg;

        try {
            File bgFile = new File(bgString);
            origbg = YassUtils.readImage(bgFile);

            imgbg = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = imgbg.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(origbg, 0, 0, 800, 600, null);
            g2d.dispose();

            setProperty("bg-filename", trim(bgFile.getName()));
            setProperty("bg-filesize",
                    ((int) (10 * bgFile.length() / 1024.0) / 10.0) + "");
            setProperty("bg-width", origbg == null ? "" : origbg.getWidth()
                    + "");
            setProperty("bg-height", origbg == null ? "" : origbg.getHeight()
                    + "");
        } catch (Exception e) {
            origbg = null;
            imgbg = null;
            setProperty("bg-filename", "");
            setProperty("bg-filesize", "");
            setProperty("bg-width", "");
            setProperty("bg-height", "");
        }
    }

    /**
     * Sets the cover attribute of the YassSongInfo object
     *
     * @param im The new cover value
     */
    public void setCover(Image im) {
        coString = null;

        origco = new BufferedImage(im.getWidth(null), im.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics g = origco.getGraphics();
        g.drawImage(im, 0, 0, null);
        g.dispose();

        imgco = new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imgco.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(origco, 0, 0, 250, 250, null);
        g2d.dispose();

        setProperty("co-filename", "");
        setProperty("co-filesize", "");
        setProperty("co-width", origco == null ? "" : origco.getWidth() + "");
        setProperty("co-height", origco == null ? "" : origco.getHeight() + "");

        repaint();
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public String trim(String s) {
        return trim(s, txt, smallfont);
    }

    /**
     * Sets the background attribute of the YassSongInfo object
     *
     * @param im The new background value
     */
    public void setBackground(Image im) {
        bgString = null;

        origbg = new BufferedImage(im.getWidth(null), im.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics g = origbg.getGraphics();
        g.drawImage(im, 0, 0, null);
        g.dispose();

        imgbg = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imgbg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(origbg, 0, 0, 800, 600, null);
        g2d.dispose();

        setProperty("bg-filename", "");
        setProperty("bg-filesize", "");
        setProperty("bg-width", origbg == null ? "" : origbg.getWidth() + "");
        setProperty("bg-height", origbg == null ? "" : origbg.getHeight() + "");

        repaint();
    }

    /**
     * Description of the Method
     *
     * @param txt      Description of the Parameter
     * @param encoding Description of the Parameter
     */
    public void updateText(String txt, String encoding) {
        // update all metadata except artist/title:
        // co/bg/vd/vdgap,gap,start/end/...

        JDialog dia = updateDialog = new JDialog(actions.createOwnerFrame());
        dia.setTitle(I18.get("mpop_update_title"));
        dia.setAlwaysOnTop(true);
        dia.setResizable(true);
        dia.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
            }
        });

        JPanel cmp = new JPanel(new BorderLayout());
        YassTable table = new YassTable();
        table.loadFile(song.getDirectory() + File.separator
                + song.getFilename());

        YassTable table2 = new YassTable();
        table2.setText(txt);

        YassTable t = check1 = new YassTable();
        YassTable t2 = check2 = new YassTable();
        YassTableModel tm = (YassTableModel) t.getModel();
        YassTableModel tm2 = (YassTableModel) t2.getModel();
        if (encoding != null) {
            t.setEncoding(encoding);
        }

        int n = table.getRowCount();
        int n2 = table2.getRowCount();

        int i = 0;
        while (i < n) {
            YassRow r = table.getRowAt(i++);
            if (r.isComment()) {
                tm.addRow(r);
                String tag = r.getCommentTag();
                YassRow r2 = table2.getCommentRow(tag);
                if (r2 != null) {
                    tm2.addRow(r2);
                } else {
                    tm2.addRow("Y", "hide", "", "", "", "");
                }
            } else {
                break;
            }
        }
        int i2 = 0;
        while (i2 < n2) {
            YassRow r = table2.getRowAt(i2++);
            if (r.isComment()) {
                String tag = r.getCommentTag();
                YassRow r2 = table.getCommentRow(tag);
                if (r2 == null) {
                    tm2.addRow(r);
                    tm.addRow("Y", "hide", "", "", "", "");
                }
            } else {
                break;
            }
        }

        i = 0;
        boolean isHeader = true;
        int count = 0;
        int count2 = 0;
        while (i < n) {
            YassRow r = table.getRowAt(i++);
            if (r.isComment() && isHeader) {
                continue;
            } else {
                isHeader = false;
                tm.addRow(r);
                count++;
            }
        }
        i2 = 0;
        isHeader = true;
        while (i2 < n2) {
            YassRow r = table2.getRowAt(i2++);
            if (r.isComment() && isHeader) {
                continue;
            } else {
                isHeader = false;
                tm2.addRow(r);
                count2++;
            }
        }

        while (count < count2) {
            tm.addRow("Y", "hide", "", "", "");
            count++;
        }
        while (count2 < count) {
            tm2.addRow("Y", "hide", "", "", "");
            count2++;
        }

        tm.fireTableDataChanged();
        tm2.fireTableDataChanged();

        checks = new Vector<>();
        int h = t.getRowHeight();
        JPanel updatePanel = new JPanel(new GridLayout(t.getRowCount() + 1, 1));
        JLabel label;
        updatePanel.add(label = new JLabel(I18.get("mpop_update_label")));
        label.setPreferredSize(new Dimension(140, h));
        i = 0;
        isHeader = true;
        JCheckBox notes;
        boolean allNotesSame = true;
        n = t.getRowCount();
        int changedTags = 0;
        while (i < n) {
            YassRow r = tm.getRowAt(i);
            YassRow r2 = t2.getRowAt(i++);
            if (r.isComment() && isHeader) {
                String tag = r.isHidden() ? r2.getCommentTag() : r
                        .getCommentTag();
                tag = tag.substring(0, tag.length() - 1);
                tag = tag.toUpperCase();
                boolean same = r2.getComment().trim()
                        .equals(r.getComment().trim());
                if (same || tag.equals("TITLE") || tag.equals("ARTIST")) {
                    updatePanel.add(label = new JLabel(""));
                    label.setPreferredSize(new Dimension(140, h));
                    checks.addElement(new JCheckBox("placeholder"));
                    continue;
                }
                if (r2.isHidden()) {
                    tag = I18.get("mpop_update_remove");
                } else {
                    tag = r2.getComment();
                }

                JCheckBox check = new JCheckBox(tag);
                check.setPreferredSize(new Dimension(140, h));
                updatePanel.add(check);
                checks.addElement(check);
                changedTags++;
            } else if (r.isHidden() && isHeader) {
                String tag = r2.getComment();
                JCheckBox check = new JCheckBox(tag);
                check.setPreferredSize(new Dimension(140, h));
                updatePanel.add(check);
                checks.addElement(check);
                changedTags++;
            } else {
                if (isHeader) {
                    isHeader = false;
                } else {
                    if (!r.equals(r2)) {
                        allNotesSame = false;
                        break;
                    }
                }
            }
        }

        if (!allNotesSame) {
            notes = new JCheckBox(I18.get("mpop_update_notes"));
            notes.setPreferredSize(new Dimension(140, h));
            updatePanel.add(notes);
            checks.add(notes);
            changedTags++;
        } else {
            updatePanel.add(label = new JLabel(""));
            label.setPreferredSize(new Dimension(140, h));
        }

        updatePanel.add(label = new JLabel(""));
        label.setPreferredSize(new Dimension(140, h));

        if (checks.size() > 1) {
            JCheckBox all = new JCheckBox(I18.get("mpop_update_all"));
            all.setPreferredSize(new Dimension(140, h));
            updatePanel.add(all);
            all.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    boolean onoff = ((JCheckBox) (e.getSource())).isSelected();
                    for (Enumeration<JCheckBox> en = checks.elements(); en
                            .hasMoreElements(); ) {
                        JCheckBox ch = en.nextElement();
                        ch.setSelected(onoff);
                    }
                }
            });
        } else {
            updatePanel.add(label = new JLabel(I18.get("mpop_update_nodiff")));
            label.setPreferredSize(new Dimension(140, h));
        }
        boolean needsOk = !allNotesSame || changedTags > 0;

        JScrollPane scroll;
        JScrollPane scroll2;
        JScrollPane scroll3;
        cmp.add("West", scroll = new JScrollPane(t));
        cmp.add("Center", scroll2 = new JScrollPane(t2));
        cmp.add("East", scroll3 = new JScrollPane(updatePanel));
        scroll.setPreferredSize(new Dimension(240, 400));
        scroll2.setPreferredSize(new Dimension(240, 400));
        scroll3.setPreferredSize(new Dimension(145, 400));
        scroll2.getVerticalScrollBar().setModel(
                scroll.getVerticalScrollBar().getModel());// sync
        scroll3.getVerticalScrollBar().setModel(
                scroll.getVerticalScrollBar().getModel());// sync
        scroll2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll3.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        JOptionPane optionPane = new JOptionPane(cmp,
                JOptionPane.PLAIN_MESSAGE,
                needsOk ? JOptionPane.OK_CANCEL_OPTION
                        : JOptionPane.DEFAULT_OPTION);
        dia.setContentPane(optionPane);
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (!e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
                    return;
                }

                JOptionPane optionPane = (JOptionPane) e.getSource();
                Object val = optionPane.getValue();
                if (val == null || val == JOptionPane.UNINITIALIZED_VALUE) {
                    return;
                }
                int value = ((Integer) val).intValue();
                if (value == JOptionPane.OK_OPTION) {
                    int i = 0;
                    boolean isHeader = true;
                    YassTable t = check1;
                    YassTable t2 = check2;
                    YassTable t3 = new YassTable();
                    t3.init(prop);
                    int n = t.getRowCount();
                    YassTableModel tm3 = (YassTableModel) t3.getModel();
                    boolean notesChecked = checks
                            .elementAt(checks.size() - 1).isSelected();

                    while (i < n) {
                        YassRow r = t.getRowAt(i);
                        YassRow r2 = t2.getRowAt(i++);
                        if (r.isComment() && isHeader) {
                            String tag = r.isHidden() ? r2.getCommentTag() : r
                                    .getCommentTag();
                            tag = tag.substring(0, tag.length() - 1);
                            tag = tag.toUpperCase();
                            boolean same = r2.getComment().trim()
                                    .equals(r.getComment().trim());
                            if (same || tag.equals("TITLE")
                                    || tag.equals("ARTIST")) {
                                tm3.addRow(r);
                                continue;
                            }
                            JCheckBox check = checks
                                    .elementAt(i - 1);
                            boolean checked = check.isSelected();
                            if (r2.isHidden()) {
                                if (!checked) {
                                    tm3.addRow(r);
                                }
                                continue;
                            } else {
                                if (checked) {
                                    tm3.addRow(r2);
                                } else {
                                    tm3.addRow(r);
                                }
                            }
                        } else {
                            isHeader = false;
                            if (notesChecked) {
                                if (!r2.isHidden()) {
                                    tm3.addRow(r2);
                                }
                            } else {
                                if (!r2.isHidden()) {
                                    tm3.addRow(r2);
                                }
                            }
                        }
                    }

                    t3.setEncoding(t.getEncoding());
                    if (!t3.storeFile(song.getDirectory() + File.separator
                            + song.getFilename())) {
                        System.out.println("Cannot update file: "
                                + song.getDirectory() + File.separator
                                + song.getFilename());
                    }
                }

                updateDialog.dispose();
            }
        });
        dia.setModal(true);
        dia.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dia.pack();
        dia.setVisible(true);
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

        try {
            Transferable tr = dropTargetDragEvent.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                java.util.List fileList = (java.util.List) tr
                        .getTransferData(DataFlavor.javaFileListFlavor);
                if (fileList.isEmpty()) {
                    return;
                }
                Iterator<?> it = fileList.iterator();
                File file = (File) it.next();
                if (!file.exists()) {
                    return;
                }

                String filename = file.getName();
                String filenameLow = filename.toLowerCase();
                boolean isTXT = filenameLow.endsWith(".txt");

                if (isTXT) {
                    StringBuilder sb = new StringBuilder();
                    unicode.UnicodeReader r;
                    BufferedReader inputStream;
                    FileInputStream fis;
                    try {
                        r = new unicode.UnicodeReader(
                                fis = new FileInputStream(file), null);
                        inputStream = new BufferedReader(r);

                        String l;
                        while ((l = inputStream.readLine()) != null) {
                            sb.append(l);
                            sb.append("\n");
                        }
                        //encoding = r.getEncoding();

                        inputStream.close();
                        r.close();
                        fis.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String s = sb.toString();
                    YassTable t = new YassTable();
                    t.setText(s);
                    String newTitle = t.getTitle().trim();
                    String newArtist = t.getArtist().trim();
                    String newVersion = t.getVersion().trim();
                    actions.selectSong(newArtist, newTitle, newVersion);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Description of the Method
     *
     * @param dropTargetEvent Description of the Parameter
     */
    public void dragExit(DropTargetEvent dropTargetEvent) {
    }

    /**
     * Description of the Method
     *
     * @param dropTargetDragEvent Description of the Parameter
     */
    public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
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
        try {
            Transferable tr = dropTargetDropEvent.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dropTargetDropEvent
                        .acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

                java.util.List fileList = (java.util.List) tr
                        .getTransferData(DataFlavor.javaFileListFlavor);
                if (fileList.isEmpty()) {
                    dropTargetDropEvent.getDropTargetContext().dropComplete(
                            true);
                    return;
                }
                Iterator<?> it = fileList.iterator();
                File file = (File) it.next();
                if (!file.exists()) {
                    dropTargetDropEvent.getDropTargetContext().dropComplete(
                            true);
                    return;
                }

                String filename = file.getName();
                String filenameLow = filename.toLowerCase();
                boolean isCO = filenameLow.indexOf("[co]") > 0
                        && (filenameLow.endsWith(".jpg") || filenameLow
                        .endsWith(".jpeg"));
                boolean isBG = filenameLow.indexOf("[bg]") > 0
                        && (filenameLow.endsWith(".jpg") || filenameLow
                        .endsWith(".jpeg"));
                boolean isVD = filenameLow.endsWith(".mpg")
                        || filenameLow.endsWith(".mpeg")
                        || filenameLow.endsWith(".avi");
                boolean isTXT = filenameLow.endsWith(".txt");

                BufferedImage img = null;
                if (!isCO
                        && !isBG
                        && (filenameLow.endsWith(".jpg") || filenameLow
                        .endsWith(".jpeg"))) {
                    int ow = 0;
                    int oh = 0;
                    try {
                        img = YassUtils.readImage(file);
                        ow = img.getWidth();
                        oh = img.getHeight();
                    } catch (Exception ignored) {
                    }
                    if (ow > 10 && Math.abs(ow - oh) < 50) {
                        isCO = true;
                    } else if (ow > oh + 50) {
                        isBG = true;
                    }
                } else if (isCO || isBG) {
                    img = YassUtils.readImage(file);
                }

                if (isTXT) {
                    StringBuffer sb = new StringBuffer();
                    String encoding = null;
                    unicode.UnicodeReader r;
                    BufferedReader inputStream;
                    FileInputStream fis;
                    try {
                        r = new unicode.UnicodeReader(
                                fis = new FileInputStream(file), null);
                        inputStream = new BufferedReader(r);

                        String l;
                        while ((l = inputStream.readLine()) != null) {
                            sb.append(l);
                            sb.append("\n");
                        }
                        encoding = r.getEncoding();

                        inputStream.close();
                        r.close();
                        fis.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String s = sb.toString();
                    if (YassUtils.isValidKaraokeString(s)) {
                        YassTable t = new YassTable();
                        t.setText(s);
                        String newTitle = t.getTitle().trim();
                        String newArtist = t.getArtist().trim();
                        String newVersion = t.getVersion().trim();
                        String oldTitle = song.getTitle().trim();
                        String oldArtist = song.getArtist().trim();
                        String oldVersion = song.getVersion().trim();
                        if (!oldTitle.equals(newTitle)
                                || !oldArtist.equals(newArtist)
                                || !oldVersion.equals(newVersion)) {
                            dropTargetDropEvent.getDropTargetContext()
                                    .dropComplete(true);
                            repaint();
                            return;
                        }
                        updateText(s, encoding);
                    }
                }
                if (isCO) {
                    setCover(img);
                    backupCover(file);
                }
                if (isBG) {
                    setBackground(img);
                    backupBackground(file);
                    curbg = imgbg;
                }
                if (isVD) {
                    setVideo(file.getPath());
                    backupVideo(file, null);
                }
                if (isCO || isBG || isVD) {
                    song.setSaved(false);
                    reloadAction.setEnabled(true);
                    reloadAllAction.setEnabled(true);
                    storeAction.setEnabled(true);
                }
                dropTargetDropEvent.getDropTargetContext().dropComplete(true);

                repaint();
                return;
            }
            if (dropTargetDropEvent
                    .isDataFlavorSupported(DataFlavor.imageFlavor)) {
                dropTargetDropEvent
                        .acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                Object td = tr.getTransferData(DataFlavor.imageFlavor);
                if (td instanceof Image) {
                    Image im = (Image) td;
                    dropTargetDropEvent.getDropTargetContext().dropComplete(
                            true);

                    int ow = im.getWidth(this);
                    int oh = im.getHeight(this);

                    boolean isCO = false;

                    boolean isBG = false;
                    if (ow > 10 && Math.abs(ow - oh) < 50) {
                        isCO = true;
                    } else if (ow > oh + 50) {
                        isBG = true;
                    }
                    if (isCO) {
                        setCover(im);
                        backupCover(null);
                    }
                    if (isBG) {
                        setBackground(im);
                        backupBackground(null);
                        curbg = imgbg;
                    }
                    if (isCO || isBG) {
                        song.setSaved(false);
                        reloadAction.setEnabled(true);
                        reloadAllAction.setEnabled(true);
                        storeAction.setEnabled(true);
                    }
                } else {
                    dropTargetDropEvent.getDropTargetContext().dropComplete(
                            true);
                }
                repaint();
                return;
            }
            if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                dropTargetDropEvent
                        .acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

                Object td = tr.getTransferData(DataFlavor.stringFlavor);
                if (!(td instanceof String)) {
                    dropTargetDropEvent.getDropTargetContext().dropComplete(
                            true);
                    repaint();
                    return;
                }

                String tds = (String) td;
                boolean ok = YassUtils.isValidKaraokeString(tds);
                if (!ok) {
                    dropTargetDropEvent.getDropTargetContext().dropComplete(
                            true);
                    repaint();
                    return;
                }

                StringTokenizer st = new StringTokenizer(tds, "\n\r");
                YassTable t = new YassTable();
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    if (!t.addRow(s)) {
                        break;
                    }
                }

                String newTitle = t.getTitle().trim();
                String newArtist = t.getArtist().trim();
                String newVersion = t.getVersion().trim();
                String oldTitle = song.getTitle().trim();
                String oldArtist = song.getArtist().trim();
                String oldVersion = song.getVersion().trim();
                if (!oldTitle.equals(newTitle) || !oldArtist.equals(newArtist)
                        || !oldVersion.equals(newVersion)) {
                    dropTargetDropEvent.getDropTargetContext().dropComplete(
                            true);
                    repaint();
                    return;
                }

                dropTargetDropEvent.getDropTargetContext().dropComplete(true);

                updateText(tds, null);

                repaint();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dropTargetDropEvent.rejectDrop();
    }

    /**
     * Description of the Class
     *
     * @author Saruta
     */
    public static class ImageSelection implements Transferable {
        private Image image;

        /**
         * Constructor for the ImageSelection object
         *
         * @param image Description of the Parameter
         */
        public ImageSelection(Image image) {
            this.image = image;
        }

        /**
         * Gets the transferDataFlavors attribute of the ImageSelection object
         *
         * @return The transferDataFlavors value
         */
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        /**
         * Gets the dataFlavorSupported attribute of the ImageSelection object
         *
         * @param flavor Description of the Parameter
         * @return The dataFlavorSupported value
         */
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        /**
         * Gets the transferData attribute of the ImageSelection object
         *
         * @param flavor Description of the Parameter
         * @return The transferData value
         * @throws UnsupportedFlavorException Description of the Exception
         * @throws IOException                Description of the Exception
         */
        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }

    }

    /**
     * Description of the Class
     *
     * @author Saruta
     */
    class LoaderThread extends Thread {
        boolean isInterrupted = false;
        boolean isFinished = false;

        /**
         * Main processing method for the LoaderThread object
         */
        public void run() {
            YassSong song2 = song;
            if (song2 == null || txt.getGraphics() == null) {
                isFinished = true;
                return;
            }

            String dir = song2.getDirectory();
            String co = song2.getCover();
            String bg = song2.getBackground();
            String mp3 = song2.getMP3();
            String vd = song2.getVideo();
            String txtname = song2.getFilename();
            String folder = song2.getFolder();

            String rgb = Integer.toHexString(blue.getRGB());
            rgb = rgb.substring(2, rgb.length());
            setProperty("link-color", rgb);

            setProperty("co-path", "http://co");
            setProperty("bg-path", "http://bg");
            setProperty("mp3-path", "http://mp3");
            setProperty("vd-path", "http://vd");
            setProperty("txt-path", "http://txt");
            setProperty("dir-path", "http://dir");

            setProperty("full-co-path", co == null ? "" : dir + File.separator
                    + co);
            setProperty("full-bg-path", bg == null ? "" : dir + File.separator
                    + bg);
            setProperty("full-mp3-path", mp3 == null ? "" : dir
                    + File.separator + mp3);
            setProperty("full-vd-path", vd == null ? "" : dir + File.separator
                    + vd);
            setProperty("full-txt-path", txtname == null ? "" : dir
                    + File.separator + txtname);
            setProperty("full-dir-path", dir);

            String songdir = prop.getProperty("song-directory");
            File sd = new File(songdir);
            setProperty("folder", "");
            setProperty("path", "");
            File f = new File(dir);
            if (f.equals(sd)) {
                setProperty("path", "/");
                setProperty("folder", "");
            } else if (f.exists()) {
                String path = f.getName();
                f = f.getParentFile();
                while (!f.equals(sd)) {
                    path = f.getName() + " / " + path;
                    f = f.getParentFile();
                }
                setProperty("path", trim(path));
                setProperty("folder", folder);
            }

            if (isInterrupted) {
                isFinished = true;
                return;
            }

            if (song2.isSaved()) {
                setCover(dir + File.separator + co);
            } else {
                Image img = getCoverBackup();
                if (img == null) {
                    setCover(dir + File.separator + co);
                } else {
                    setCover(img);
                }
            }
            if (isInterrupted) {
                isFinished = true;
                return;
            }

            copyCoverAction.setEnabled(imgco != null);

            alpha = 1;
            fadebg = curbg;

            if (song2.isSaved()) {
                setBackground(dir + File.separator + bg);
            } else {
                Image img = getBackgroundBackup();
                if (img == null) {
                    setBackground(dir + File.separator + bg);
                } else {
                    setBackground(img);
                }
            }

            if (isInterrupted) {
                isFinished = true;
                return;
            }

            copyBackgroundAction.setEnabled(imgbg != null);

            duration = -1;

            File file = new File(dir + File.separator + mp3);
            if (mp3 != null && mp3.trim().length() > 0 && file.exists()) {
                setProperty("mp3-filesize",
                        ((int) (10 * file.length() / 1024.0 / 1024.0) / 10.0)
                                + "");

                boolean ogg = false;
                AudioInputStream in = null;
                try {
                    AudioFileFormat aff = AudioSystem.getAudioFileFormat(file);
                    if (aff.getType() == VorbisFileFormatType.OGG) {
                        ogg = true;
                    }
                    // System.out.println("Audio Type : " + aff.getType());

                    in = AudioSystem.getAudioInputStream(file);
                    //if (in != null) {
                    //AudioFormat baseFormat = in.getFormat();
                    // System.out.println("Source Format : " +
                    // baseFormat.toString());
                    //}
                } catch (Exception ignored) {
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception ignored) {
                        }
                    }
                }

                try {
                    AudioFileFormat baseFileFormat = AudioSystem
                            .getAudioFileFormat(file);
                    if (baseFileFormat instanceof TAudioFileFormat) {
                        Map<?, ?> properties = baseFileFormat
                                .properties();
                        setProperty("mp3-author",
                                (String) properties.get("author"));
                        setProperty("mp3-title",
                                (String) properties.get("title"));
                        setProperty("mp3-album",
                                (String) properties.get("album"));
                        setProperty("mp3-date", (String) properties.get("date"));

                        Long dur = (Long) properties.get("duration");
                        if (dur != null) {
                            duration = dur.longValue();
                            int sec = (int) Math
                                    .round(dur.longValue() / 1000000.0);
                            int min = sec / 60;
                            sec = sec - min * 60;
                            String dString = (sec < 10) ? min + ":0" + sec
                                    : min + ":" + sec;
                            setProperty("mp3-duration", dString);
                        }

                        String genre = (String) properties
                                .get("mp3.id3tag.genre");
                        if (genre != null) {
                            setProperty("mp3-genre", genre);
                        } else {
                            setProperty("mp3-genre",
                                    (String) properties
                                            .get("ogg.comment.genre"));
                        }

                        Boolean vbr = (Boolean) properties.get("mp3.vbr");
                        if (vbr == null) {
                            vbr = (Boolean) properties.get("vbr");
                        }

                        if (vbr != null) {
                            setProperty("mp3-vbr", vbr.booleanValue() ? "VBR"
                                    : "CBR");
                        } else {
                            setProperty("mp3-vbr", "CBR");
                        }

                        Integer val = (Integer) properties
                                .get("mp3.bitrate.nominal.bps");
                        if (val == null) {
                            val = (Integer) properties
                                    .get("ogg.bitrate.nominal.bps");
                        }

                        String s = ((int) (10 * val.intValue() / 1000.0))
                                / 10.0 + "";
                        if (s.endsWith(".0")) {
                            s = s.substring(0, s.length() - 2);
                        }
                        setProperty("mp3-bitrate", val == null ? "" : s);

                        val = (Integer) properties.get("mp3.frequency.hz");
                        if (val == null) {
                            val = (Integer) properties.get("ogg.frequency.hz");
                        }
                        s = ((int) (10 * val.intValue() / 1000.0)) / 10.0 + "";
                        if (s.endsWith(".0")) {
                            s = s.substring(0, s.length() - 2);
                        }
                        setProperty("mp3-frequency", val == null ? "" : s);
                    }
                } catch (Exception e) {
                    if (!ogg) {
                        System.out.println("Unknown Audio Format: " + mp3);
                        e.printStackTrace();
                    }
                }
            }

            setProperty("mp3-filename", trim(mp3));

            if (isInterrupted) {
                isFinished = true;
                return;
            }

            file = new File(dir + File.separator + txtname);
            if (file.exists()) {
                YassTable t = new YassTable();
                t.loadFile(dir + File.separator + txtname);

                copyLyricsAction.setEnabled(true);

                setProperty("txt-notes", t.getNoteCount() + "");
                setProperty("txt-pages", t.getPageCount() + "");

                double gap = t.getGap();
                int sec = (int) Math.round(gap / 1000.0);
                int min = sec / 60;
                sec = sec - min * 60;
                String gapString = (sec < 10) ? min + ":0" + sec : min + ":"
                        + sec;
                setProperty("txt-gap", gapString);

                double start = t.getStart();
                String startString = start + "";
                if (startString.endsWith(".0")) {
                    startString = startString.substring(0,
                            startString.length() - 2);
                }
                setProperty("txt-start", startString);

                double end = t.getEnd();
                String endString = end + "";
                if (endString.endsWith(".0")) {
                    endString = endString.substring(0, endString.length() - 2);
                }
                setProperty("txt-end", endString);

                String edition = t.getEdition();
                setProperty("txt-edition", edition == null ? "-" : edition);
                String genre = t.getGenre();
                setProperty("txt-genre", genre == null ? "-" : genre);
                String year = t.getYear();
                setProperty("txt-year", year == null ? "-" : year);
                String album = t.getAlbum();
                setProperty("txt-album", album == null ? "-" : album);
                String id = t.getID();
                setProperty("txt-id", id == null ? "-" : id);
                String len = t.getLength();
                setProperty("txt-length", len == null ? "-" : len);
                String lang = t.getLanguage();
                setProperty("txt-language", lang == null ? "-" : lang);

                double bpm = t.getBPM();
                String bpmString = bpm + "";
                if (bpmString.endsWith(".0")) {
                    bpmString = bpmString.substring(0, bpmString.length() - 2);
                }
                setProperty("txt-bpm", bpmString);

                double previewstart = t.getPreviewStart();
                setProperty("txt-previewstart", previewstart < 0 ? "-"
                        : previewstart + "");
                int msb = t.getMedleyStartBeat();
                setProperty("txt-medleystartbeat", msb < 0 ? "" : msb + "");
                int meb = t.getMedleyEndBeat();
                setProperty("txt-medleyendbeat", meb < 0 ? "" : meb + "");

                setProperty("txt-encoding", t.getEncoding());

                int msl = -1;
                int mel = -1;
                if (msb >= 0 && meb >= 0) {
                    int pg = 1;
                    for (Enumeration<YassRow> en = t.getRows(); en.hasMoreElements(); ) {
                        YassRow r = en.nextElement();
                        if (r.isPageBreak()) {
                            pg++;
                            continue;
                        }
                        if (!r.isNote()) {
                            continue;
                        }
                        int beat = r.getBeatInt();
                        int beat2 = beat + r.getLengthInt();
                        if (beat >= msb && msl < 0) {
                            msl = pg;
                        }
                        if (beat2 >= meb && mel < 0) {
                            mel = pg;
                            break;
                        }
                    }
                }
                setProperty("txt-medleystartline", msl + "");
                setProperty("txt-medleyendline", mel + "");

                String vgap = t.getVideoGap() + "";
                if (vgap.endsWith(".0")) {
                    vgap = vgap.substring(0, vgap.length() - 2);
                }
                setProperty("vd-gap", vgap);

                if (song != null) {
                    song.clearStats();
                    Vector<?> stats = yass.stats.YassStats.getAllStats();
                    for (Enumeration<?> en = stats.elements(); en.hasMoreElements(); ) {
                        yass.stats.YassStats st = (yass.stats.YassStats) en.nextElement();
                        st.calcStats(song, t);
                    }
                }
            } else {
                setProperty("txt-notes", "");
            }
            setProperty("txt-filename", trim(txtname));

            if (isInterrupted) {
                isFinished = true;
                return;
            }
            repaint();

            setProperty("vd-filename", trim(vd));

            setProperty("vd-filesize", "0");
            setProperty("vd-duration", "");
            setProperty("vd-width", "");
            setProperty("vd-height", "");

            hasvideo = false;
            file = null;
            if (!song2.isSaved()) {
                file = getVideoBackupFile();
                if (file == null && vd != null && vd.length() > 0) {
                    file = new File(dir + File.separator + vd);
                } else {
                    String videoID = prop.getProperty("video-id");
                    String vg = null;
                    if (file != null) {
                        vg = YassUtils.getWildcard(file.getName(),
                                videoID.toLowerCase());
                    }
                    if (vg == null) {
                        vg = "0";
                    }
                    // setProperty("vd-gap", vg);

                    String name = null;
                    if (vd != null && vd.length() > 0) {
                        String oldfilename = dir + File.separator + vd;
                        File oldfile = new File(oldfilename);
                        if (oldfile.exists()) {
                            name = oldfile.getName();
                        }
                    }
                    if (name == null && file != null) {
                        String vdname = file.getName();
                        int i = vdname.lastIndexOf(".");
                        String ext = vdname.substring(i);
                        name = song.getArtist() + " - " + song.getTitle()
                                + " [VD#" + vg + "]" + ext;
                        setProperty("vd-filename", name);
                    } else {
                        setProperty("vd-filename", "");
                    }

                }
            } else if (vd != null && vd.length() > 0) {
                file = new File(dir + File.separator + vd);
            }
            if (file != null && file.exists()) {
                setProperty("vd-filesize",
                        ((int) (10 * file.length() / 1024.0 / 1024.0) / 10.0)
                                + "");
                hasvideo = true;

                // System.out.println("Loading Video:" + vd);

                YassVideoUtils.initVideo();
                setVideo(file.getPath());
                // System.out.println("Loading Video DONE");

                copyVideoAction.setEnabled(true);
            }

            try {
                Thread.currentThread();
                Thread.sleep(200);
            } catch (Exception ignored) {
            }
            if (isInterrupted) {
                isFinished = true;
                return;
            }

            if (imgbg != null) {

                alpha = 0;
                while (alpha < .95) {
                    alpha += .1f;
                    repaint();
                    try {
                        Thread.currentThread();
                        Thread.sleep(20);
                    } catch (Exception ignored) {
                    }
                    if (isInterrupted) {
                        alpha = 1;
                        isFinished = true;
                        return;
                    }
                }
            } else {
                alpha = 1;
                curbg = null;
            }

            replaceInfo();
            repaint();
        }
    }
}
