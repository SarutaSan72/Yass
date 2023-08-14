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

import yass.renderer.YassNote;
import yass.renderer.YassPlayerNote;
import yass.renderer.YassSession;
import yass.renderer.YassTrack;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

public class YassSheet extends JPanel implements yass.renderer.YassPlaybackRenderer {

    public final static int NORM_HEIGHT = 20;

    // gray, blue, golden, freestyle, red
    public static final int COLORSET_COUNT = 9;
    public static final int COLOR_NORMAL = 0;
    public static final int COLOR_SHADE = 1;
    public static final int COLOR_ACTIVE = 2;
    public static final int COLOR_GOLDEN = 3;
    public static final int COLOR_RAPGOLDEN = 4;
    public static final int COLOR_RAP = 5;
    public static final int COLOR_FREESTYLE = 6;
    public static final int COLOR_ERROR = 7;
    public static final int COLOR_WARNING = 8;
    private final Color[] colorSet = new Color[COLORSET_COUNT];

    public static final  Color black = new Color(0,0,0);
    public static final Color dkGray = new Color(102,102,102);
    public static final  Color hiGray = new Color(153,153,153);
    public static final Color HI_GRAY_2 = new Color(230,230,230);
    public static final  Color white = new Color(255,255,255);

    public static final  Color blackDarkMode = new Color(200,200,200);
    public static final Color dkGrayDarkMode = new Color(142,142,142);
    public static final Color hiGrayDarkMode = new Color(100,100,100);
    public static final Color HI_GRAY_2_DARK_MODE = new Color(70,70,70);
    public static final  Color whiteDarkMode = new Color(50,50,50);

    private static final Color arrow = new Color(238, 238, 238, 160);
    private static final Color arrowDarkMode = new Color(200, 200, 200, 160);

    private static final Color playertextBG = new Color(1f, 1f, 1f, .9f); // used once; deprecated
    private static final  Color playBlueHi = new Color(1f, 1f, 1f, 1f);  // used once
    private static final  Color playBlue = new Color(.4f, .6f, .8f, 1f); // used once
    public static final  Color BLUE = new Color(.4f, .6f, .8f, .7f);
    public static final  Color blueDrag = new Color(.8f, .9f, 1f, .5f);
    public static final  Color blueDragDarkMode = new Color(.4f, .6f, .8f, .5f);
    private static final  Color dkRed = new Color(.8f, .4f, .4f, .7f);
    public static final  Color playerColor = new Color(1f, .1f, .1f, .5f);
    private static final  Color playerColor2 = new Color(1f, .1f, .1f, .3f);
    private static final  Color playerColor3 = new Color(1f, .1f, .1f, .1f);
    private static final  Color inoutColor = new Color(.9f, .9f, 1f, .5f);
    private static final  Color inoutSnapshotBarColor = new Color(.3f, .3f, .5f, .7f);
    private static final  Color inoutBarColor = new Color(.5f, .5f, .7f, .7f);

    public static final BasicStroke thinStroke = new BasicStroke(0.5f),
            stdStroke = new BasicStroke(1f), medStroke = new BasicStroke(1.5f),
            thickStroke = new BasicStroke(2f);

    @Serial
    private static final long serialVersionUID = 3284920111520989009L;
    private final static int ACTION_CONTROL = 1, ACTION_ALT = 2,
            ACTION_CONTROL_ALT = 4, ACTION_NONE = 0;

    private final static int SKETCH_LENGTH = 30;
    private final static int SKETCH_UP = 1, SKETCH_DOWN = 2, SKETCH_LEFT = 3,
            SKETCH_RIGHT = 4, SKETCH_NONE = 0;
    private final static int SKETCH_HORIZONTAL = -1, SKETCH_VERTICAL = -2;
    private final static int[] GESTURE_UP = new int[]{SKETCH_VERTICAL,
            SKETCH_UP};
    private final static int[] GESTURE_UP_DOWN = new int[]{SKETCH_VERTICAL,
            SKETCH_UP, SKETCH_DOWN};
    private final static int[] GESTURE_DOWN_UP = new int[]{SKETCH_VERTICAL,
            SKETCH_DOWN, SKETCH_UP};
    private final static int[] GESTURE_UP_DOWN_UP = new int[]{
            SKETCH_VERTICAL, SKETCH_UP, SKETCH_DOWN, SKETCH_UP};
    private final static int[] GESTURE_DOWN_UP_DOWN = new int[]{
            SKETCH_VERTICAL, SKETCH_DOWN, SKETCH_UP, SKETCH_DOWN};
    private final static int[] GESTURE_LEFT = new int[]{SKETCH_HORIZONTAL,
            SKETCH_LEFT};
    private final static int[] GESTURE_RIGHT = new int[]{SKETCH_HORIZONTAL,
            SKETCH_RIGHT};
    private final static int[] GESTURE_LEFT_RIGHT = new int[]{
            SKETCH_HORIZONTAL, SKETCH_LEFT, SKETCH_RIGHT};
    private final static int[] GESTURE_RIGHT_LEFT = new int[]{
            SKETCH_HORIZONTAL, SKETCH_RIGHT, SKETCH_LEFT};
    private final static int[] GESTURE_LEFT_RIGHT_LEFT = new int[]{
            SKETCH_HORIZONTAL, SKETCH_LEFT, SKETCH_RIGHT, SKETCH_LEFT};
    private final static int[] GESTURE_RIGHT_LEFT_RIGHT = new int[]{
            SKETCH_HORIZONTAL, SKETCH_RIGHT, SKETCH_LEFT, SKETCH_RIGHT};
    private final static int[] GESTURE_DOWN = new int[]{SKETCH_VERTICAL,
            SKETCH_DOWN};
    private final static int fs = 14;

    private final Font font = new Font("SansSerif", Font.BOLD, fs);
    private final Font fontv = new Font("SansSerif", Font.PLAIN, fs);
    private final Font fonti = new Font("SansSerif", Font.ITALIC, fs);
    private final Font fontb = new Font("SansSerif", Font.BOLD, fs + 2);
    private final Font fontt = new Font("MonoSpaced", Font.PLAIN, fs);
    private final Font fonttb = new Font("MonoSpaced", Font.BOLD, fs + 2);
    private final Font[] big = new Font[]{new Font("SansSerif", Font.BOLD, fs - 8),
            new Font("SansSerif", Font.BOLD, fs - 8),
            new Font("SansSerif", Font.BOLD, fs - 8),
            new Font("SansSerif", Font.BOLD, fs - 8),
            new Font("SansSerif", Font.BOLD, fs - 8),
            new Font("SansSerif", Font.BOLD, fs - 8),
            new Font("SansSerif", Font.BOLD, fs - 8),
            new Font("SansSerif", Font.BOLD, fs - 8),
            new Font("SansSerif", Font.BOLD, fs - 8),
            new Font("SansSerif", Font.BOLD, fs - 8),
            new Font("SansSerif", Font.BOLD, fs - 8),
            new Font("SansSerif", Font.BOLD, fs - 7),
            new Font("SansSerif", Font.BOLD, fs - 6),
            new Font("SansSerif", Font.BOLD, fs - 5),
            new Font("SansSerif", Font.BOLD, fs - 4),
            new Font("SansSerif", Font.BOLD, fs - 3),
            new Font("SansSerif", Font.BOLD, fs - 2),
            new Font("SansSerif", Font.BOLD, fs - 1),
            new Font("SansSerif", Font.BOLD, fs),
            new Font("SansSerif", Font.BOLD, fs + 1),
            new Font("SansSerif", Font.BOLD, fs + 2),
            new Font("SansSerif", Font.BOLD, fs + 3),
            new Font("SansSerif", Font.BOLD, fs + 4),
            new Font("SansSerif", Font.BOLD, fs + 5),
            new Font("SansSerif", Font.BOLD, fs + 6),
            new Font("SansSerif", Font.BOLD, fs + 7),
            new Font("SansSerif", Font.BOLD, fs + 8),
            new Font("SansSerif", Font.BOLD, fs + 9),
            new Font("SansSerif", Font.BOLD, fs + 10),
            new Font("SansSerif", Font.BOLD, fs + 11),
            new Font("SansSerif", Font.BOLD, fs + 12),
            new Font("SansSerif", Font.BOLD, fs + 13),
            new Font("SansSerif", Font.BOLD, fs + 14),
            new Font("SansSerif", Font.BOLD, fs + 15),
            new Font("SansSerif", Font.BOLD, fs + 16),
            new Font("SansSerif", Font.BOLD, fs + 17),
            new Font("SansSerif", Font.BOLD, fs + 18),
            new Font("SansSerif", Font.BOLD, fs + 19),
            new Font("SansSerif", Font.BOLD, fs + 20)};
    private final static int UNDEFINED = 0;
    private int dragDir = UNDEFINED;
    private int hiliteCue = UNDEFINED, dragMode = UNDEFINED;

    private final static int VERTICAL = 1;
    private final static int HORIZONTAL = 2;
    private final static int LEFT = 1;
    private final static int RIGHT = 2;
    private final static int CENTER = 3;
    private final static int CUT = 4;
    private final static int JOIN_LEFT = 5;
    private final static int JOIN_RIGHT = 6;
    private final static int SNAPSHOT = 7;
    private final static int MOVE_REMAINDER = 8;
    private final static int PREV_PAGE = 9;
    private final static int NEXT_PAGE = 10;
    private final static int PREV_PAGE_PRESSED = 11;
    private final static int NEXT_PAGE_PRESSED = 12;
    private final static int SLIDE = 14;
    private final static int PLAY_NOTE = 15;
    private final static int PLAY_PAGE = 16;
    private final static int PLAY_NOTE_PRESSED = 18;
    private final static int PLAY_PAGE_PRESSED = 19;
    private final static int PLAY_BEFORE_PRESSED = 20;
    private final static int PLAY_BEFORE = 21;
    private final static int PLAY_NEXT = 22;
    private final static int PLAY_NEXT_PRESSED = 23;
    private final static int PREV_SLIDE_PRESSED = 24;
    private final static int NEXT_SLIDE_PRESSED = 25;
    private final static int PREV_SLIDE = 26;
    private final static int NEXT_SLIDE = 27;
    boolean useSketching = false, useSketchingPlayback = false;
    AffineTransform identity = new AffineTransform();
    String bufferlost = I18.get("sheet_msg_buffer_lost");
    VolatileImage backVolImage = null, plainVolImage = null;
    String[] hNoteTable = new String[]{"C", "C#", "D", "D#", "E", "F", "F#",
            "G", "G#", "A", "B", "H"};
    String[] bNoteTable = new String[]{"C", "C#", "D", "D#", "E", "F", "F#",
            "G", "G#", "A", "A#", "B"};
    String[] actualNoteTable = bNoteTable;
    boolean paintHeights = false;
    boolean live = false;
    String toomuchtext = I18.get("sheet_msg_too_much_text");
    private YassTable table = null;
    private final Vector<YassTable> tables = new Vector<>();
    private final Vector<Vector<YassRectangle>> rects = new Vector<>();
    private Vector<YassRectangle> rect = null;
    private Vector<Cloneable> snapshot = null, snapshotRect = null;
    private YassActions actions = null;
    private boolean noshade = false;
    boolean darkMode = false;
    private boolean autoTrim = false;
    private boolean temporaryZoomOff = false;
    private long lastMidiTime = -1;
    private long lastDragTime = -1;
    private int lyricsWidth = 400;
    private boolean lyricsVisible = true;
    private boolean messageMemory = false;
    private final int[] keycodes = new int[19];
    private long equalsKeyMillis = 0;
    private String layout = "East";

    private Paint tex, bgtex;
    private BufferedImage bgImage = null;
    private boolean showVideo = false, showBackground = false;
    private boolean mouseover = true;
    private boolean paintSnapshot = false, showArrows = true,
            showPlayerButtons = true, showText = true;
    private int hiliteAction = 0;

    /*
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    private long lastTime = -1;
    private String lastTimeString = "";
    private final int
            LEFT_BORDER = 36,
            RIGHT_BORDER = 36,
            TOP_BORDER = 30,
            PLAY_PAGE_X = -76,
            PLAY_PAGE_W = 36,
            PLAY_BEFORE_X = -36,
            PLAY_BEFORE_W = 36,
            PLAY_NOTE_X = 2,
            PLAY_NOTE_W = 48,
            PLAY_NEXT_X = 49,
            PLAY_NEXT_W = 36;
    private int BOTTOM_BORDER = 56,
            TOP_LINE,
            TOP_PLAYER_BUTTONS;
    private Point[] sketch = null;
    private int sketchPos = 0, dirPos = 0;
    private long sketchStartTime = 0;
    private int[] sketchDirs = null;
    private boolean sketchStarted = false;
    private final Font smallFont = new Font("SansSerif", Font.PLAIN, 10);
    private int minHeight = 0, maxHeight = 18;
    private int minBeat = 0, maxBeat = 1000;
    private int hit = -1, hilite = -1, hiliteHeight = 1000, hhPageMin = 0;
    private final int heightBoxWidth = 74;
    private final Rectangle2D.Double select = new Rectangle2D.Double(0, 0, 0, 0);
    private double selectX, selectY;
    private double wSize = 30, hSize = -1;
    private int dragOffsetX = 0, dragOffsetY = 0, slideX = 0;
    private double dragOffsetXRatio = 0;
    private boolean pan = false, isPlaying = false, isTemporaryStop = false;
    private double bpm = -1, gap = 0, beatgap = 0, duration = -1;
    private int outgap = 0;
    private double cutPercent = .5;
    private BufferedImage image;
    private boolean imageChanged = true;
    private int imageX = -1;
    private int playerPos = -1, inPoint = -1, outPoint = -1;
    private String message = "";
    private long inSelect = -1, outSelect = -1;
    private long inSnapshot = -1, outSnapshot = -1;
    private final Cursor cutCursor;
    private boolean showNoteLength = false;
    private boolean showNoteBeat = false;

    private boolean showNoteScale = false;
    private boolean showNoteHeight = true;

    private boolean showNoteHeightNum = false;
    private Rectangle clip = new Rectangle();
    private boolean refreshing = false;
    private String equalsDigits = "";
    private boolean versionTextPainted = true;
    private final Vector<Long> tmpNotes = new Vector<>(1024);
    private final Dimension dim = new Dimension(1000, 100);
    private Graphics2D pgb = null;
    private int ppos = 0;
    private Point psheetpos = null;
    private boolean pisinterrupted = false;
    private BufferedImage videoFrame = null;
    private YassSession session = null;
    private boolean isMousePressed = false;

    public YassSheet() {
        super(false);
        setFocusable(true);
        Image image = new ImageIcon(this.getClass().getResource("/yass/resources/img/cut.gif")).getImage();
        cutCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 10), "cut");
        removeAll();
        setDarkMode(false); // creates TexturePaint

        addKeyListener(new KeyListener() {
            private long lastDigitMillis;
            private int lastDigit;

            public void keyTyped(KeyEvent e) {
                if (equalsKeyMillis > 0) {
                    e.consume();
                    return;
                }
                dispatch();
            }

            public void keyPressed(KeyEvent e) {
                if (table == null)
                    return;
                char c = e.getKeyChar();
                int code = e.getKeyCode();

                if (equalsKeyMillis > 0) {
                    if (code == KeyEvent.VK_BACK_SPACE) {
                        equalsDigits = equalsDigits.substring(0,
                                equalsDigits.length() - 1);
                        repaint();
                    } else if (code == KeyEvent.VK_ESCAPE) {
                        equalsDigits = "";
                        equalsKeyMillis = 0;
                        repaint();
                    } else if (code == KeyEvent.VK_ENTER) {
                        if (equalsDigits.length() > 0)
                            setCurrentLineTo(Integer.valueOf(equalsDigits)
                                    .intValue());
                        equalsDigits = "";
                        equalsKeyMillis = 0;
                        repaint();
                    } else if (c >= '0' && c <= '9') {
                        equalsDigits = equalsDigits + c;
                        repaint();
                    }
                    e.consume();
                    return;
                }

                if (e.isControlDown() && e.isAltDown()
                        && c == KeyEvent.CHAR_UNDEFINED) {
                    hiliteAction = ACTION_CONTROL_ALT;
                    repaint();
                } else if (e.isControlDown() && c == KeyEvent.CHAR_UNDEFINED) {
                    hiliteAction = ACTION_CONTROL;
                    repaint();
                } else if (e.isAltDown() && c == KeyEvent.CHAR_UNDEFINED) {
                    hiliteAction = ACTION_ALT;
                    repaint();
                } else if (e.isShiftDown() && c == KeyEvent.CHAR_UNDEFINED) {
                    hiliteAction = ACTION_CONTROL_ALT;
                    repaint();
                }

                // 0=next_note, 1=prev_note, 2=page_down, 3=page_up
                if (code == keycodes[0] && !e.isControlDown() && !e.isAltDown()) {
                    table.nextBeat(false);
                    e.consume();
                    return;
                }
                if (code == keycodes[1] && !e.isControlDown() && !e.isAltDown()) {
                    table.prevBeat(false);
                    e.consume();
                    return;
                }
                if (code == keycodes[2] && !e.isControlDown() && !e.isAltDown()) {
                    table.gotoPage(1);
                    e.consume();
                    return;
                }
                if (code == keycodes[3] && !e.isControlDown() && !e.isAltDown()) {
                    table.gotoPage(-1);
                    e.consume();
                    return;
                }
                // 12=play, 13=play_page
                if (code == keycodes[12] && !e.isControlDown()
                        && !e.isAltDown()) {
                    if (e.isShiftDown()) {
                        firePropertyChange("play", null, "page");
                    } else {
                        firePropertyChange("play", null, "start");
                    }
                    e.consume();
                    return;
                }
                if (code == keycodes[13] && !e.isControlDown()
                        && !e.isAltDown()) {
                    Integer mode = e.isShiftDown() ? 1 : 0;
                    firePropertyChange("play", mode, "page");
                    e.consume();
                    return;
                }

                // 17=play_before, 18=play_next
                if (code == keycodes[17] && !e.isControlDown() && !e.isAltDown()) {
                    Integer mode = e.isShiftDown() ? 1 : 0;
                    firePropertyChange("play", mode, "before");
                    e.consume();
                    return;
                }
                if (code == keycodes[18] && !e.isControlDown() && !e.isAltDown()) {
                    Integer mode = e.isShiftDown() ? 1 : 0;
                    firePropertyChange("play", mode, "next");
                    e.consume();
                    return;
                }

                // 4=init, 5=init_next, 6=right, 7=left, 8=up, 9=down,
                // 10=lengthen, 11=shorten, 12=play, 13=play_page,
                // 14=scroll_left, 15=scroll_right, 16=one_page
                if (code == keycodes[14] && !e.isControlDown()
                        && !e.isAltDown()) {
                    slideLeft(e.isShiftDown() ? 50 : 10);
                    e.consume();
                    return;
                }
                if (code == keycodes[15] && !e.isControlDown()
                        && !e.isAltDown()) {
                    slideRight(e.isShiftDown() ? 50 : 10);
                    e.consume();
                    return;
                }

                if (code == keycodes[16] && !e.isControlDown()
                        && !e.isAltDown()) {
                    firePropertyChange("one", null, null);
                    e.consume();
                    return;
                }

                // 4=init, 5=init_next, 6=right, 7=left, 8=up, 9=down,
                // 10=lengthen, 11=shorten
                if ((code == keycodes[10] || code == keycodes[11])
                        && !e.isControlDown() && !e.isAltDown()) {
                    boolean lengthen = code == keycodes[10];
                    boolean changed = false;
                    int[] rows = table.getSelectedRows();
                    for (int next : rows) {
                        YassRow row = table.getRowAt(next);
                        if (!row.isNote()) {
                            continue;
                        }

                        int length = row.getLengthInt();
                        row.setLength(lengthen ? length + 1 : Math.max(1,
                                length - 1));
                        changed = true;
                    }
                    table.zoomPage();
                    table.updatePlayerPosition();
                    if (changed) {
                        table.addUndo();
                    }

                    e.consume();
                    SwingUtilities.invokeLater(() -> {
                        update();
                        repaint();
                        firePropertyChange("play", null, "start");
                    });
                    return;
                }

                if ((code == keycodes[6] || code == keycodes[7])
                        && !e.isControlDown() && !e.isAltDown()) {
                    boolean right = code == keycodes[6];
                    boolean changed = false;
                    int[] rows = table.getSelectedRows();
                    for (int next : rows) {
                        YassRow row = table.getRowAt(next);
                        if (!row.isNote()) {
                            continue;
                        }

                        int beat = row.getBeatInt();
                        row.setBeat(right ? beat + 1 : beat - 1);
                        changed = true;
                    }
                    table.updatePlayerPosition();
                    if (changed) {
                        table.addUndo();
                    }

                    e.consume();
                    SwingUtilities.invokeLater(() -> {
                        update();
                        repaint();
                        firePropertyChange("play", null, "start");
                    });
                    return;
                }

                // 4=init, 5=init_next, 6=right, 7=left, 8=up, 9=down,
                // 10=lengthen, 11=shorten
                if ((code == keycodes[8] || code == keycodes[9])
                        && !e.isControlDown() && !e.isAltDown()) {
                    boolean up = code == keycodes[8];
                    boolean changed = false;
                    int[] rows = table.getSelectedRows();
                    for (int next : rows) {
                        YassRow row = table.getRowAt(next);
                        if (!row.isNote()) {
                            continue;
                        }

                        int height = row.getHeightInt();
                        row.setHeight(up ? height + 1 : height - 1);
                        changed = true;
                    }
                    table.updatePlayerPosition();
                    if (changed) {
                        table.addUndo();
                    }

                    e.consume();
                    SwingUtilities.invokeLater(new Thread(() -> {
                        update();
                        repaint();
                        firePropertyChange("play", 2, "page");
                    }));
                    return;
                }

                if (Character.isDigit(c) && e.isAltDown() && !e.isControlDown()) {
                    String cstr = Character.toString(c);
                    long currentTime = System.currentTimeMillis();
                    if (currentTime < lastTime + 700) {
                        if (lastTimeString.length() < 3) {
                            cstr = lastTimeString + cstr;
                        }
                        lastTimeString = cstr;
                        try {
                            int n = Integer.parseInt(cstr);
                            table.gotoPageNumber(n);
                        } catch (Exception ignored) {
                        }
                    } else {
                        lastTimeString = cstr;
                        try {
                            int n = Integer.parseInt(cstr);
                            table.gotoPageNumber(n);
                        } catch (Exception ignored) {
                        }
                    }
                    lastTime = currentTime;
                    e.consume();
                    return;
                }

                // 4=init, 5=init_next, 6=right, 7=left, 8=up, 9=down,
                // 10=lengthen, 11=shorten, 12=play, 13=play_page,
                // 14=scroll_left, 15=scroll_right, 16=one_page
                if ((code == keycodes[4] || code == keycodes[5])
                        && !e.isControlDown() && !e.isAltDown()) {
                    boolean initCurrent = code == keycodes[4];
                    boolean changed = false;

                    if (initCurrent) {
                        int[] rows = table.getSelectedRows();
                        for (int next : rows) {
                            YassRow row = table.getRowAt(next);
                            if (!row.isNote()) {
                                continue;
                            }

                            YassRow row2 = table.getRowAt(next - 1);
                            if (!row2.isNote()) {
                                continue;
                            }
                            int beat = row2.getBeatInt();
                            int len = row2.getLengthInt();
                            row.setBeat(beat + len + 1);
                            row.setLength(1);
                            changed = true;
                        }
                        table.updatePlayerPosition();
                    } else {
                        int next = table.getSelectionModel()
                                .getMaxSelectionIndex();
                        YassRow row = table.getRowAt(next);
                        if (!row.isNote()) {
                            e.consume();
                            return;
                        }

                        int beat = row.getBeatInt();
                        int len = row.getLengthInt();
                        if (next + 1 >= table.getRowCount()) {
                            e.consume();
                            return;
                        }
                        YassRow row2 = table.getRowAt(next + 1);
                        if (!row2.isNote()) {
                            e.consume();
                            return;
                        }
                        row2.setBeat(beat + len + 1);
                        row2.setLength(1);
                        table.setRowSelectionInterval(next + 1, next + 1);
                        changed = true;
                        table.updatePlayerPosition();
                    }

                    if (changed) {
                        table.addUndo();
                    }

                    e.consume();
                    SwingUtilities.invokeLater(() -> {
                        update();
                        repaint();
                        firePropertyChange("play", null, "start");
                    });
                    return;
                }

                if (Character.isDigit(c) && !e.isControlDown()) {
                    boolean changed = false;
                    String cstr = Character.toString(c);
                    int n = -1;
                    try {
                        n = Integer.parseInt(cstr);
                    } catch (Exception ignored) {
                    }

                    long currentMillis = System.currentTimeMillis();
                    if (currentMillis - lastDigitMillis < 500)
                        n = lastDigit * 10 + n;
                    lastDigitMillis = System.currentTimeMillis();
                    lastDigit = n;

                    int[] rows = table.getSelectedRows();
                    for (int next : rows) {
                        YassRow row = table.getRowAt(next);
                        if (!row.isNote()) {
                            continue;
                        }
                        row.setLength(n);
                        changed = true;
                    }

                    table.updatePlayerPosition();
                    if (changed) {
                        table.addUndo();
                    }

                    e.consume();
                    SwingUtilities.invokeLater(() -> {
                        update();
                        repaint();
                        firePropertyChange("play", null, "start");
                    });
                    return;
                }
                dispatch();
            }

            public void keyReleased(KeyEvent e) {
                if (table == null)
                    return;
                if (!e.isControlDown() && !e.isAltDown() && !e.isShiftDown()) {
                    hiliteAction = ACTION_NONE;
                    repaint();
                } else if (!e.isControlDown() && e.isAltDown()) {
                    hiliteAction = ACTION_ALT;
                    repaint();
                } else if (e.isControlDown() && !e.isAltDown()) {
                    hiliteAction = ACTION_CONTROL;
                    repaint();
                } else if (e.isShiftDown()) {
                    hiliteAction = ACTION_CONTROL_ALT;
                    repaint();
                }

                if (!isPlaying) {
                    table.setPreventAutoCheck(false);
                    if (actions != null) {
                        actions.checkData(table, false, true);
                        actions.showMessage(0);
                    }
                }
                dispatch();
            }

            private void dispatch() {
                // if (table != null)
                // table.dispatchEvent(e);
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                hiliteAction = ACTION_NONE;
                if (! isMousePressed)
                    return;
                isMousePressed = false;

                if (table == null) {
                    return;
                }

                if (hiliteCue == PREV_SLIDE_PRESSED || hiliteCue == NEXT_SLIDE_PRESSED) {
                    stopSlide();
                    hiliteCue = UNDEFINED;
                    return;
                }

                if (temporaryZoomOff) {
                    temporaryZoomOff = false;
                    YassTable.setZoomMode(YassTable.ZOOM_ONE);
                    table.zoomPage();
                }

                if (table.getPreventUndo()) {
                    table.setPreventUndo(false);
                    table.setSaved(false);
                    table.addUndo();
                    actions.updateActions();
                }
                if (actions != null) {
                    actions.showMessage(0);
                }

				/*
                 * if (table.getPreventZoom()) { table.setPreventZoom(false);
				 * table.zoomPage(); }
				 */
                if (hiliteCue == MOVE_REMAINDER) {
                    hiliteCue = UNDEFINED;
                }

                if (hiliteCue == PREV_PAGE_PRESSED) {
                    SwingUtilities.invokeLater(() -> firePropertyChange("page", null, -1));
                    hiliteCue = UNDEFINED;
                }
                if (hiliteCue == NEXT_PAGE_PRESSED) {
                    SwingUtilities.invokeLater(() -> firePropertyChange("page", null, +1));
                    hiliteCue = UNDEFINED;
                }
                if (hiliteCue == PLAY_NOTE_PRESSED) {
                    SwingUtilities.invokeLater(() -> firePropertyChange("play", null, "start"));
                    hiliteCue = UNDEFINED;
                }
                if (hiliteCue == PLAY_PAGE_PRESSED) {
                    SwingUtilities.invokeLater(() -> firePropertyChange("play", null, "page"));
                    hiliteCue = UNDEFINED;
                }
                if (hiliteCue == PLAY_BEFORE_PRESSED) {
                    SwingUtilities.invokeLater(() -> firePropertyChange("play", null, "before"));
                    hiliteCue = UNDEFINED;
                }
                if (hiliteCue == PLAY_NEXT_PRESSED) {
                    SwingUtilities.invokeLater(() -> firePropertyChange("play", null, "next"));
                    hiliteCue = UNDEFINED;
                }

                if (sketchStarted()) {
                    // firePropertyChange("play", null, "stop");
                    SwingUtilities.invokeLater(() -> {
                        int ok = executeSketch();
                        cancelSketch();
                        if (useSketchingPlayback) {
                            if (ok == 2) {
                                firePropertyChange("play", null, "start");
                            } else if (ok == 3) {
                                int i = table.getSelectionModel()
                                        .getMinSelectionIndex();
                                if (i >= 0) {
                                    YassRow r = table.getRowAt(i);
                                    if (r.isNote()) {
                                        int h = r.getHeightInt();
                                        firePropertyChange("midi", null, h);
                                        firePropertyChange("play", null,
                                                "start");
                                    }
                                }
                            }
                        }
                        repaint();
                    });

                } else {
                    repaint();
                }
                selectX = selectY = -1;
                select.x = select.y = select.width = select.height = 0;
                inPoint = outPoint = -1;
            }

            public void mouseClicked(MouseEvent e) {
                if (isPlaying() || isTemporaryStop()) {
                    firePropertyChange("play", null, "stop");
                    e.consume();
                    return;
                }

                int x = e.getX();
                int y = e.getY();

                if (x > playerPos + PLAY_PAGE_X && x < playerPos + PLAY_NEXT_X + PLAY_NEXT_W
                        && y > TOP_PLAYER_BUTTONS
                        && y < TOP_PLAYER_BUTTONS + 64) {
                    // PLAY_NOTE_PRESSED or PLAY_PAGE_PRESSED or PLAY_BEFORE/NEXT_PRESSED
                    return;
                }

                boolean left = SwingUtilities.isLeftMouseButton(e);
                boolean twice = e.getClickCount() > 1;
                boolean one = e.getClickCount() == 1;

                // LYRICS POSITION
                boolean notInLyrics = true;
                if (layout.equals("East")) {
                    notInLyrics = (x - getViewPosition().x) < clip.width - lyricsWidth;
                } else if (layout.equals("West")) {
                    notInLyrics = (x - getViewPosition().x) > lyricsWidth;
                }

                if (y > clip.height - BOTTOM_BORDER + 20
                        || (y > 20 && y < TOP_LINE - 10 && notInLyrics)) {
                    if (!left || twice || one) {
                        //firePropertyChange("one", null, null);
                        return;
                    }
                }

                if (!twice) {
                    if (!paintHeights) {
                        return;
                    }
                    if (e.getX() > clip.x + heightBoxWidth) {
                        return;
                    }
                    if (hiliteHeight > 200) {
                        return;
                    }
                    firePropertyChange("midi", null, pan ? (hiliteHeight - 2) : hiliteHeight);
                    return;
                }
                table.selectLine();
            }

            public void mousePressed(MouseEvent e) {
                if (equalsKeyMillis > 0) {
                    equalsKeyMillis = 0;
                    equalsDigits = "";
                }
                boolean left = SwingUtilities.isLeftMouseButton(e);
                if (table == null)
                    return;
                if (! hasFocus()) {
                    requestFocusInWindow();
                    requestFocus();
                }
                if (isPlaying() || isTemporaryStop()) {
                    firePropertyChange("play", null, "stop");
                    e.consume();
                    return;
                }
                isMousePressed = true; // not while playing
                int x = e.getX();
                int y = e.getY();
                if (x > clip.x + LEFT_BORDER && x < clip.x + LEFT_BORDER + LEFT_BORDER && y > dim.height - BOTTOM_BORDER) {
                    hiliteCue = PREV_SLIDE_PRESSED;
                    startSlide(-10);
                    repaint();
                    return;
                }
                if (x > clip.x + clip.width - RIGHT_BORDER - RIGHT_BORDER && x < clip.x + clip.width - RIGHT_BORDER && y > dim.height - BOTTOM_BORDER) {
                    hiliteCue = NEXT_SLIDE_PRESSED;
                    startSlide(+10);
                    repaint();
                    return;
                }
                if (YassTable.getZoomMode() == YassTable.ZOOM_ONE && dragMode != SLIDE) {
                    temporaryZoomOff = true;
                    YassTable.setZoomMode(YassTable.ZOOM_MULTI);
                }
                setErrorMessage("");
                if (paintHeights) {
                    if (x < clip.x + heightBoxWidth && y > TOP_LINE - 10
                            && (y < clip.height - BOTTOM_BORDER)) {
                        if (y < 0) {
                            y = 0;
                        }
                        if (y > dim.height) {
                            y = dim.height;
                        }

                        int dy;
                        if (pan) {
                            dy = (int) Math.round(hhPageMin
                                    + (dim.height - y - BOTTOM_BORDER) / hSize);
                        } else {
                            dy = (int) Math.round(minHeight
                                    + (dim.height - y - BOTTOM_BORDER) / hSize);
                        }
                        hiliteHeight = dy;
                        repaint();
                        return;
                    }
                }
                if (x < clip.x + LEFT_BORDER && y > dim.height - BOTTOM_BORDER) {
                    hiliteCue = PREV_PAGE_PRESSED;
                    repaint();
                    return;
                }
                if (x > clip.x + clip.width - RIGHT_BORDER && y > dim.height - BOTTOM_BORDER) {
                    hiliteCue = NEXT_PAGE_PRESSED;
                    repaint();
                    return;
                }
                if (x > playerPos + PLAY_PAGE_X && x < playerPos + PLAY_PAGE_X + PLAY_PAGE_W
                        && y > TOP_PLAYER_BUTTONS
                        && y < TOP_PLAYER_BUTTONS + 64) {
                    hiliteCue = PLAY_PAGE_PRESSED;
                    repaint();
                    return;
                }
                if (x > playerPos + PLAY_BEFORE_X && x < playerPos + PLAY_BEFORE_X + PLAY_BEFORE_W
                        && y > TOP_PLAYER_BUTTONS
                        && y < TOP_PLAYER_BUTTONS + 64) {
                    hiliteCue = PLAY_BEFORE_PRESSED;
                    repaint();
                    return;
                }
                if (x > playerPos + PLAY_NOTE_X && x < playerPos + PLAY_NOTE_X + PLAY_NOTE_W
                        && y > TOP_PLAYER_BUTTONS
                        && y < TOP_PLAYER_BUTTONS + 64) {
                    hiliteCue = PLAY_NOTE_PRESSED;
                    repaint();
                    return;
                }
                if (x > playerPos + PLAY_NEXT_X && x < playerPos + PLAY_NEXT_X + PLAY_NEXT_W
                        && y > TOP_PLAYER_BUTTONS
                        && y < TOP_PLAYER_BUTTONS + 64) {
                    hiliteCue = PLAY_NEXT_PRESSED;
                    repaint();
                    return;
                }
                YassRectangle r;
                if (hiliteCue == CUT) {
                    r = rect.elementAt(hilite);
                    table.clearSelection();
                    table.addRowSelectionInterval(hilite, hilite);
                    firePropertyChange("split", null, (e.getX() - r.x) / r.width);
                    hiliteCue = UNDEFINED;
                } else if (hiliteCue == JOIN_LEFT) {
                    r = rect.elementAt(hilite);
                    table.clearSelection();
                    table.addRowSelectionInterval(hilite, hilite);
                    firePropertyChange("joinLeft", null, (e.getX() - r.x));
                    hiliteCue = UNDEFINED;
                } else if (hiliteCue == JOIN_RIGHT) {
                    r = rect.elementAt(hilite);
                    table.clearSelection();
                    table.addRowSelectionInterval(hilite, hilite);
                    firePropertyChange("joinRight", null, (int) (e.getX() - r.x));
                    hiliteCue = UNDEFINED;
                } else if (hiliteCue == SNAPSHOT) {
                    hiliteCue = UNDEFINED;
                    createSnapshot();
                    repaint();
                    return;
                } else if (hiliteCue == MOVE_REMAINDER) {
                    hit = nextElement();
                    if (hit < 0)
                        return;
                    table.setRowSelectionInterval(hit, hit);
                    table.updatePlayerPosition();

                    dragMode = CENTER;
                    r = rect.elementAt(hit);
                    dragOffsetX = (int) (e.getX() - r.x);
                    dragOffsetY = (int) (e.getY() - r.y);
                    dragOffsetXRatio = dragOffsetX / wSize;
                    return;
                } else if (hiliteCue == SLIDE && left) {
                    YassTable t = getActiveTable();
                    if (t != null && t.getMultiSize() == 1) {
                        YassTable.setZoomMode(YassTable.ZOOM_MULTI);
                        enablePan(false);
                        actions.revalidateLyricsArea();
                        update();
                        repaint();
                    }
                    dragMode = SLIDE;
                    slideX = e.getX();
                    return;
                }
                YassRectangle next = null;
                hit = -1;
                selectX = selectY = -1;
                dragDir = UNDEFINED;
                dragOffsetX = dragOffsetY = 0;
                int i = 0;
                for (Enumeration<?> en = rect.elements(); en.hasMoreElements(); i++) {
                    if (next != null) {
                        r = next;
                        next = (YassRectangle) en.nextElement();
                    } else {
                        r = (YassRectangle) en.nextElement();
                    }
                    if (next == null) {
                        next = en.hasMoreElements() ? (YassRectangle) en.nextElement() : null;
                    }
                    if (r == null)
                        break;
                    if (r.isPageBreak()) {
                        if (x > r.x - 5 && x < r.x + 5) {
                            hit = i;
                            dragOffsetX = (int) (e.getX() - r.x);
                            dragOffsetY = (int) (e.getY() - r.y);
                            dragOffsetXRatio = dragOffsetX / wSize;
                            dragMode = hiliteCue;
                            if (!table.isRowSelected(i)) {
                                if (e.isControlDown()) {
                                    table.addRowSelectionInterval(i, i);
                                } else {
                                    table.setRowSelectionInterval(i, i);
                                }
                            }
                            table.scrollRectToVisible(table.getCellRect(i, 0, true));
                            repaint();
                            break;
                        }
                    } else if (r.contains(e.getPoint())) {
                        // hiliteAction = ACTION_CONTROL_ALT;
                        hit = i;
                        dragOffsetX = (int) (e.getX() - r.x);
                        dragOffsetY = (int) (e.getY() - r.y);
                        dragOffsetXRatio = dragOffsetX / wSize;
                        dragMode = hiliteCue;
                        if (!table.isRowSelected(i)) {
                            if (e.isShiftDown() || e.isControlDown()) {
                                table.addRowSelectionInterval(i, i);
                            } else {
                                table.setRowSelectionInterval(i, i);
                            }
                        }
                        table.scrollRectToVisible(table.getCellRect(i, 0, true));
                        table.updatePlayerPosition();

                        inPoint = outPoint = playerPos;
                        inSelect = fromTimeline(inPoint);
                        outSelect = fromTimeline(outPoint);
                        if (r.hasType(YassRectangle.GAP)) {
                            temporaryZoomOff = false;
                        }
                        repaint();
                        break;
                    } else if (table.getMultiSize() > 1
                            && r.x < x
                            && (((next == null || next.isPageBreak() || next.hasType(YassRectangle.END)) && x < r.x + r.width) ||
                            (next != null && (!next.isPageBreak() && !next.hasType(YassRectangle.END)) && x < next.x))
                            && y > clip.height - BOTTOM_BORDER
                            && y < clip.height - BOTTOM_BORDER + 16) {
                        hiliteAction = ACTION_CONTROL_ALT;

                        hit = i;
                        dragOffsetX = (int) (e.getX() - r.x);
                        dragOffsetY = (int) (e.getY() - r.y);
                        dragOffsetXRatio = dragOffsetX / wSize;
                        dragMode = hiliteCue;

                        table.setRowSelectionInterval(i, i);
                        table.selectLine();
                        table.updatePlayerPosition();

                        inPoint = outPoint = playerPos;
                        inSelect = fromTimeline(inPoint);
                        outSelect = fromTimeline(outPoint);

                        repaint();
                        break;
                    }
                }
                if (hit < 0) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        inPoint = outPoint = e.getX();
                        inSelect = fromTimeline(inPoint);
                        outSelect = fromTimeline(outPoint);

                        if (useSketching) {
                            startSketch();
                            addSketch(e.getX(), e.getY());
                            repaint();
                            return;
                        }

                        boolean any = false;
                        int k = 0;
                        for (Enumeration<?> en = rect.elements(); en.hasMoreElements(); k++) {
                            r = (YassRectangle) en.nextElement();
                            if (r.x <= e.getX() && e.getX() <= r.x + r.width) {
                                if (!any) {
                                    if (!(e.isShiftDown() || e.isControlDown())) {
                                        table.clearSelection();
                                    }
                                }
                                if (!table.isRowSelected(k)) {
                                    table.addRowSelectionInterval(k, k);
                                }
                                any = true;
                            }
                        }
                        if (any) {
                            table.updatePlayerPosition();
                        } else {
                            table.clearSelection();
                            playerPos = Math.min(inPoint, outPoint);
                            table.updatePlayerPosition();
                        }
                    } else {
                        inPoint = outPoint = -1;
                        inSelect = outSelect = -1;

                        Point p = (Point) e.getPoint().clone();
                        SwingUtilities.convertPointToScreen(p, YassSheet.this);
                        selectX = p.getX();
                        selectY = p.getY();
                        select.x = select.y = select.width = select.height = 0;
                    }
                    repaint();
                }
            }

            public void mouseEntered(MouseEvent e) {
                // System.out.println("sheet entered");
            }

            public void mouseExited(MouseEvent e) {
                hilite = -1;
                hiliteHeight = 1000;
                repaint();
            }

        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                if (equalsKeyMillis > 0) {
                    equalsKeyMillis = 0;
                    equalsDigits = "";
                }
                if (table == null || rect == null || isPlaying())
                    return;

                int x = e.getX();
                int y = e.getY();
                if (hilite >= 0) {
                    hilite = -2;
                } else {
                    hilite = -1;
                }
                if (hiliteCue != UNDEFINED) {
                    hilite = -2;
                }
                hiliteCue = UNDEFINED;
                hiliteAction = ACTION_NONE;

                boolean shouldRepaint = false;

                if (paintHeights) {
                    if (x < clip.x + heightBoxWidth && y > TOP_LINE - 10 && (y < clip.height - BOTTOM_BORDER)) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        if (hiliteHeight < 1000) {
                            if (y < 0)
                                y = 0;
                            if (y > dim.height)
                                y = dim.height;
                            int dy;
                            if (pan) {
                                dy = (int) Math.round(hhPageMin + (dim.height - y - BOTTOM_BORDER) / hSize);
                            } else {
                                dy = (int) Math.round(minHeight + (dim.height - y - BOTTOM_BORDER) / hSize);
                            }

                            if (hiliteHeight != dy) {
                                hiliteHeight = dy;
                                repaint();
                            }
                        }
                        return;
                    } else {
                        if (hiliteHeight != 1000) {
                            shouldRepaint = true;
                            hiliteHeight = 1000;
                        }
                    }
                }

                if (x < clip.x + LEFT_BORDER && y > clip.height - BOTTOM_BORDER) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    hiliteCue = PREV_PAGE;
                    repaint();
                    return;
                }
                int right = clip.x + clip.width - RIGHT_BORDER;
                if (x > right && y > clip.height - BOTTOM_BORDER) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    hiliteCue = NEXT_PAGE;
                    repaint();
                    return;
                }
                if (x > clip.x + LEFT_BORDER && x < clip.x + LEFT_BORDER + LEFT_BORDER && y > clip.height - BOTTOM_BORDER) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    hiliteCue = PREV_SLIDE;
                    repaint();
                    return;
                }
                if (x > clip.x + clip.width - RIGHT_BORDER - RIGHT_BORDER && x < clip.x + clip.width - RIGHT_BORDER && y > clip.height - BOTTOM_BORDER) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    hiliteCue = NEXT_SLIDE;
                    repaint();
                    return;
                }
                if (x > playerPos + PLAY_PAGE_X && x < playerPos + PLAY_PAGE_X + PLAY_PAGE_W
                        && y > TOP_PLAYER_BUTTONS
                        && y < TOP_PLAYER_BUTTONS + 64) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    hiliteCue = PLAY_PAGE;
                    repaint();
                    return;
                }
                if (x > playerPos + PLAY_BEFORE_X && x < playerPos + PLAY_BEFORE_X + PLAY_BEFORE_W
                        && y > TOP_PLAYER_BUTTONS
                        && y < TOP_PLAYER_BUTTONS + 64) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    hiliteCue = PLAY_BEFORE;
                    repaint();
                    return;
                }
                if (x > playerPos + PLAY_NOTE_X && x < playerPos + PLAY_NOTE_X + PLAY_NOTE_W
                        && y > TOP_PLAYER_BUTTONS
                        && y < TOP_PLAYER_BUTTONS + 64) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    hiliteCue = PLAY_NOTE;
                    repaint();
                    return;
                }
                if (x > playerPos + PLAY_NEXT_X && x < playerPos + PLAY_NEXT_X + PLAY_NEXT_W
                        && y > TOP_PLAYER_BUTTONS
                        && y < TOP_PLAYER_BUTTONS + 64) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    hiliteCue = PLAY_NEXT;
                    repaint();
                    return;
                }
                if (inSelect >= 0 && inSelect != outSelect && y < TOP_BORDER
                        && x >= toTimeline(Math.min(inSelect, outSelect))
                        && x <= toTimeline(Math.max(inSelect, outSelect))) {
                    hiliteCue = SNAPSHOT;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    repaint();
                    return;
                }
                // LYRICS POSITION
                boolean notInLyrics = true;
                if (layout.equals("East")) {
                    notInLyrics = (x - getViewPosition().x) < clip.width - lyricsWidth;
                } else if (layout.equals("West")) {
                    notInLyrics = (x - getViewPosition().x) > lyricsWidth;
                }
                if (!notInLyrics && getComponentCount() > 0 && lyricsVisible) {
                    // dirty bugfix for lost bounds
                    YassLyrics lyrics = (YassLyrics) getComponent(0);
                    Point p2 = lyrics.getLocation();
                    if ((layout.equals("East") && (x - p2.x > 500))
                            || (layout.equals("West") && x < lyricsWidth)) {
                        Point p = ((JViewport) getParent()).getViewPosition();
                        Dimension vr = ((JViewport) getParent()).getExtentSize();

                        int newx = (int) p.getX() + vr.width - lyricsWidth;
                        if (layout.equals("East")) {
                            newx = (int) p.getX() + vr.width - lyricsWidth;
                        } else if (layout.equals("West")) {
                            newx = (int) p.getX();
                        }

                        int newy = (int) p.getY() + 50;
                        if (p2.x != newx || p2.y != newy) {
                            lyrics.setLocation(newx, newy);
                            revalidate();
                            update();
                        }
                        repaint();
                    }
                }
                YassRectangle next = null;
                YassRectangle r;
                int i = 0;
                for (Enumeration<?> en = rect.elements(); en.hasMoreElements(); i++) {
                    if (next != null) {
                        r = next;
                        next = (YassRectangle) en.nextElement();
                    } else {
                        r = (YassRectangle) en.nextElement();
                    }
                    if (next == null) {
                        next = en.hasMoreElements() ? (YassRectangle) en.nextElement() : null;
                    }
                    if (r != null) {
                        boolean isNote = !r.isType(YassRectangle.GAP) && !r.isType(YassRectangle.START) && !r.isType(YassRectangle.END);
                        if (r.isPageBreak()) {
                            if (x > r.x - 5 && x < r.x + 5 && !autoTrim) {
                                hiliteCue = CENTER;
                                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                                repaint();
                                return;
                            }
                        } else if (r.contains(x, y)) {
                            hilite = i;
                            if (mouseover) {
                                if (!table.isRowSelected(i)) {
                                    if (!(e.isShiftDown() || e.isControlDown()))
                                        table.clearSelection();
                                    table.addRowSelectionInterval(i, i);
                                    table.updatePlayerPosition();
                                }
                            }
                            int dragw = r.width > Math.max(wSize, 32) * 3 ? (int) Math.max(wSize, 32) : (r.width > 72 ? 24 : (r.width > 48 ? 16 : 5));
                            if (Math.abs(r.x - x) < dragw && r.width > 20) {
                                hiliteCue = LEFT;
                                hiliteAction = ACTION_CONTROL;
                            } else if (Math.abs(r.x + r.width - x) < dragw && r.width > 20) {
                                hiliteCue = RIGHT;
                                hiliteAction = ACTION_ALT;
                            } else {
                                hiliteCue = CENTER;
                                hiliteAction = ACTION_CONTROL_ALT;
                            }

                            if (hiliteCue == CENTER) {
                                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                            } else {
                                setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                            }
                            repaint();
                            return;
                        } else if (table.getMultiSize() > 1
                                && r.x < x
                                && (((next == null || next.isPageBreak() || next.hasType(YassRectangle.END)) && x < r.x + r.width)
                                || (next != null && (!next.isPageBreak() && !next.hasType(YassRectangle.END)) && x < next.x))
                                && y > clip.height - BOTTOM_BORDER
                                && y < clip.height - BOTTOM_BORDER + 16) {
                            hiliteCue = CENTER;
                            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                            repaint();
                            return;
                        } else if (isNote && r.x + wSize / 2 < x
                                && x < r.x + r.width - wSize / 2
                                && Math.abs(r.y - y) < hSize && r.width > 5) {
                            hilite = i;
                            hiliteCue = CUT;
                            cutPercent = (x - r.x) / r.width;
                            setCursor(cutCursor);
                            repaint();
                            return;
                        } else if (isNote && r.x < x && x < r.x + wSize / 2
                                && r.width > 5) {
                            if (Math.abs(r.y - y) < hSize) {
                                hilite = i;
                                hiliteCue = JOIN_LEFT;
                                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                                repaint();
                                return;
                            }
                        } else if (isNote && r.x + r.width - wSize / 2 < x
                                && x < r.x + r.width && r.width > 5) {
                            if (Math.abs(r.y - y) < hSize) {
                                hilite = i;
                                hiliteCue = JOIN_RIGHT;
                                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                                repaint();
                                return;
                            }
                        }
                    }
                }
                if (y > clip.height - BOTTOM_BORDER + 20 || (y > 20 && y < TOP_LINE - 10 && notInLyrics)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    hiliteCue = SLIDE;
                    repaint();
                    return;
                }
                if (x > playerPos - 10 && x < playerPos && y > TOP_LINE && y < dim.height - BOTTOM_BORDER) {
                    hiliteCue = MOVE_REMAINDER;
                    setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                    repaint();
                    return;
                }
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                if (hilite == -2) {
                    hilite = -1;
                    repaint();
                    return;
                }
                if (shouldRepaint)
                    repaint();
            }

            public void mouseDragged(MouseEvent e) {
                if (rect == null)
                    return;
                if (! isMousePressed)
                    return;
                boolean left = SwingUtilities.isLeftMouseButton(e);
                Point p = e.getPoint();
                int px = Math.max(clip.x, Math.min(p.x, clip.x + clip.width));
                int py = p.y;
                if (hiliteCue == PREV_PAGE_PRESSED && !(px < clip.x + LEFT_BORDER && py > dim.height - BOTTOM_BORDER)) {
                    hiliteCue = PREV_PAGE;
                    repaint();
                    return;
                }
                if (hiliteCue == PREV_SLIDE_PRESSED
                        && !(px > clip.x + LEFT_BORDER && px < clip.x + LEFT_BORDER+ LEFT_BORDER && py > dim.height - BOTTOM_BORDER)) {
                    hiliteCue = PREV_SLIDE;
                    repaint();
                    return;
                }
                if (hiliteCue == PREV_PAGE
                        && (px < clip.x + LEFT_BORDER && py > dim.height - BOTTOM_BORDER)) {
                    hiliteCue = PREV_PAGE_PRESSED;
                    repaint();
                    return;
                }
                if (hiliteCue == PREV_SLIDE
                        && (px > clip.x + LEFT_BORDER && px < clip.x + LEFT_BORDER + LEFT_BORDER && py > dim.height - BOTTOM_BORDER)) {
                    hiliteCue = PREV_SLIDE_PRESSED;
                    repaint();
                    return;
                }
                if (hiliteCue == NEXT_PAGE_PRESSED
                        && !(px > clip.x + clip.width - RIGHT_BORDER && py > dim.height - BOTTOM_BORDER)) {
                    hiliteCue = NEXT_PAGE;
                    repaint();
                    return;
                }
                if (hiliteCue == NEXT_SLIDE_PRESSED
                        && !(px > clip.x + clip.width - RIGHT_BORDER - RIGHT_BORDER && px < clip.x + clip.width - RIGHT_BORDER && py > dim.height - BOTTOM_BORDER)) {
                    hiliteCue = NEXT_SLIDE;
                    repaint();
                    return;
                }
                if (hiliteCue == NEXT_PAGE
                        && (px > clip.x + clip.width - RIGHT_BORDER && py > dim.height - BOTTOM_BORDER)) {
                    hiliteCue = NEXT_PAGE_PRESSED;
                    repaint();
                    return;
                }
                if (hiliteCue == NEXT_SLIDE
                        && (px > clip.x + clip.width - RIGHT_BORDER - RIGHT_BORDER && px < clip.x + clip.width - RIGHT_BORDER
                        && py > dim.height - BOTTOM_BORDER)) {
                    hiliteCue = NEXT_PAGE_PRESSED;
                    repaint();
                    return;
                }
                if (hiliteCue == PLAY_PAGE_PRESSED
                        && !(px > playerPos + PLAY_PAGE_X && px < playerPos + PLAY_PAGE_W
                        && py > TOP_PLAYER_BUTTONS && py < TOP_PLAYER_BUTTONS + 64)) {
                    hiliteCue = PLAY_PAGE;
                    repaint();
                    return;
                }
                if (hiliteCue == PLAY_PAGE
                        && (px > playerPos + PLAY_PAGE_X && px < playerPos + PLAY_PAGE_W
                        && py > TOP_PLAYER_BUTTONS && py < TOP_PLAYER_BUTTONS + 64)) {
                    hiliteCue = PLAY_PAGE_PRESSED;
                    repaint();
                    return;
                }
                if (hiliteCue == PLAY_BEFORE_PRESSED
                        && !(px > playerPos + PLAY_BEFORE_X && px < playerPos + PLAY_BEFORE_W
                        && py > TOP_PLAYER_BUTTONS && py < TOP_PLAYER_BUTTONS + 64)) {
                    hiliteCue = PLAY_BEFORE;
                    repaint();
                    return;
                }
                if (hiliteCue == PLAY_BEFORE
                        && (px > playerPos + PLAY_BEFORE_X && px < playerPos + PLAY_BEFORE_W
                        && py > TOP_PLAYER_BUTTONS && py < TOP_PLAYER_BUTTONS + 64)) {
                    hiliteCue = PLAY_BEFORE_PRESSED;
                    repaint();
                    return;
                }
                if (hiliteCue == PLAY_NOTE_PRESSED
                        && !(px >= playerPos + PLAY_NOTE_X && px < playerPos + PLAY_NOTE_W
                        && py > TOP_PLAYER_BUTTONS && py < TOP_PLAYER_BUTTONS + 64)) {
                    hiliteCue = PLAY_NOTE;
                    repaint();
                    return;
                }
                if (hiliteCue == PLAY_NOTE
                        && (px >= playerPos && px < playerPos + 48
                        && py > TOP_PLAYER_BUTTONS && py < TOP_PLAYER_BUTTONS + 64)) {
                    hiliteCue = PLAY_NOTE_PRESSED;
                    repaint();
                    return;
                }

                if (hiliteCue == PLAY_NEXT_PRESSED
                        && !(px > playerPos + PLAY_NEXT_X && px < playerPos + PLAY_NEXT_W
                        && py > TOP_PLAYER_BUTTONS && py < TOP_PLAYER_BUTTONS + 64)) {
                    hiliteCue = PLAY_NEXT;
                    repaint();
                    return;
                }
                if (hiliteCue == PLAY_NEXT
                        && (px > playerPos + PLAY_NEXT_X && px < playerPos + PLAY_NEXT_W
                        && py > TOP_PLAYER_BUTTONS && py < TOP_PLAYER_BUTTONS + 64)) {
                    hiliteCue = PLAY_NEXT_PRESSED;
                    repaint();
                    return;
                }

                if (hiliteCue == NEXT_PAGE || hiliteCue == PREV_PAGE || hiliteCue == NEXT_SLIDE || hiliteCue == PREV_SLIDE
                        || hiliteCue == PLAY_NOTE
                        || hiliteCue == PLAY_PAGE
                        || hiliteCue == PLAY_BEFORE
                        || hiliteCue == PLAY_NEXT
                        || hiliteCue == NEXT_PAGE_PRESSED
                        || hiliteCue == PREV_PAGE_PRESSED
                        || hiliteCue == NEXT_SLIDE_PRESSED
                        || hiliteCue == PREV_SLIDE_PRESSED
                        || hiliteCue == PLAY_NOTE_PRESSED
                        || hiliteCue == PLAY_BEFORE_PRESSED
                        || hiliteCue == PLAY_NEXT_PRESSED
                        || hiliteCue == PLAY_PAGE_PRESSED) {
                    return;
                }

                if (paintHeights) {
                    if (px < clip.x + heightBoxWidth && py > TOP_LINE - 10 && (py < clip.height - BOTTOM_BORDER)) {
                        if (py < 0)
                            py = 0;
                        if (py > dim.height)
                            py = dim.height;
                        int dy;
                        if (pan) {
                            dy = (int) Math.round(hhPageMin + (dim.height - py - BOTTOM_BORDER) / hSize);
                        } else {
                            dy = (int) Math.round(minHeight + (dim.height - py - BOTTOM_BORDER) / hSize);
                        }
                        hiliteHeight = dy;
                        repaint();
                        if (hiliteHeight > 200)
                            return;
                        long time = System.currentTimeMillis();
                        if (time - lastMidiTime > 100) {
                            firePropertyChange("midi", null, pan ? (hiliteHeight - 2) : hiliteHeight);
                            lastMidiTime = time;
                        }
                        return;
                    }
                }

                if (hiliteCue == SLIDE && left) {
                    if (slideX == px)
                        return;
                    Point vp = getViewPosition();
                    int off = px - slideX;
                    int oldpoff = px - vp.x;
                    vp.x = vp.x - off;
                    if (vp.x < 0)
                        vp.x = 0;
                    setViewPosition(vp);
                    slideX = vp.x + oldpoff;
                    if (playerPos < vp.x || playerPos > vp.x + clip.width) {
                        int next = nextElement(vp.x);
                        if (next >= 0) {
                            YassRow row = table.getRowAt(next);
                            if (!row.isNote() && next + 1 < table.getRowCount()) {
                                next = next + 1;
                                row = table.getRowAt(next);
                            }
                            if (row.isNote()) {
                                table.setRowSelectionInterval(next, next);
                                table.updatePlayerPosition();
                            }
                        }
                    }
                    setPlayerPosition(-1);
                    return;
                }
                int shiftRemainder = InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK;
                if ((e.getModifiersEx() & shiftRemainder) == shiftRemainder)
                    shiftRemainder = 1;
                else
                    shiftRemainder = 0;
                if (hiliteCue == MOVE_REMAINDER)
                    shiftRemainder = 1;

                if (useSketching) {
                    if (sketchStarted()) {
                        addSketch(px, py);
                        if (!detectSketch()) {
                            cancelSketch();
                        } else {
                            repaint();
                            return;
                        }
                    }
                }

                if (hit < 0) {
                    if (left) {
                        // playerPos = px;
                        outPoint = px;
                        outSelect = fromTimeline(outPoint);

                        select.x = Math.min(inPoint, outPoint);
                        select.y = 0;
                        select.width = Math.abs(outPoint - inPoint);
                        select.height = clip.height;
                    } else {

                        Point p2 = new Point((int) selectX, (int) selectY);
                        SwingUtilities.convertPointFromScreen(p2,
                                YassSheet.this);

                        select.x = Math.min(p2.getX(), px);
                        select.y = Math.min(p2.getY(), py);
                        select.width = Math.abs(p2.getX() - (double) px);
                        select.height = Math.abs(p2.getY() - (double) py);
                    }

                    int n = rect.size();
                    YassRectangle r;
                    boolean any = false;
                    for (int i = 0; i < n; i++) {
                        r = rect.elementAt(i);
                        if (r.intersects(select)) {
                            if (!any) {
                                if (!(e.isShiftDown() || e.isControlDown())) {
                                    table.clearSelection();
                                }
                            }
                            if (!table.isRowSelected(i)) {
                                table.addRowSelectionInterval(i, i);
                            }
                            any = true;
                        }
                    }
                    if (any) {
                        table.updatePlayerPosition();
                    } else {
                        table.clearSelection();
                        playerPos = Math.min(inPoint, outPoint);
                        table.updatePlayerPosition();
                    }

                    repaint();
                    return;
                }

                long time = System.currentTimeMillis();
                if (time - lastDragTime < 60)
                    return;
                lastDragTime = time;
                table.setPreventUndo(true);
                YassRectangle rr = rect.elementAt(hit);
                table.getRowAt(hit);
                int pageMin = rr.getPageMin();
                int x;
                int dx;
                int y = py - dragOffsetY;
                if (y < 0)
                    y = 0;
                if (y > dim.height)
                    y = dim.height;
                if (y < hSize)
                    y = (int) -hSize;
                int dy;
                if (pan) {
                    dy = (int) Math.round(pageMin + (dim.height - y - hSize - BOTTOM_BORDER + 1) / hSize) - 2;
                } else {
                    dy = (int) Math.round(minHeight + (dim.height - y - hSize - BOTTOM_BORDER + 1) / hSize);
                }

                YassRow r = table.getRowAt(hit);
                if (rr.isType(YassRectangle.GAP)) {
                    x = (int) ((px - dragOffsetXRatio * wSize));
                    if (paintHeights)
                        x -= heightBoxWidth;
                    double gapres = x / wSize;
                    double gap2 = gapres * 60 * 1000 / (4 * bpm);
                    gap2 = Math.round(gap2 / 10) * 10;
                    firePropertyChange("gap", null, (int) gap2);
                    return;
                }
                if (rr.isType(YassRectangle.START)) {
                    x = (int) ((px - dragOffsetXRatio * wSize));
                    if (paintHeights)
                        x -= heightBoxWidth;
                    double valres = x / wSize;
                    double val = valres * 60 * 1000 / (4 * bpm);
                    val = Math.round(val / 10) * 10;
                    firePropertyChange("start", null, (int) val);
                    return;
                }
                if (rr.isType(YassRectangle.END)) {
                    x = (int) ((px - dragOffsetXRatio * wSize));
                    if (paintHeights)
                        x -= heightBoxWidth;
                    double valres = x / wSize;
                    double val = valres * 60 * 1000 / (4 * bpm);
                    val = Math.round(val / 10) * 10;
                    table.clearSelection();
                    // quick hack
                    firePropertyChange("end", null, (int) val);
                    table.clearSelection();
                    return;
                }

                boolean isPageBreak = r.isPageBreak();
                if (!isPageBreak) {
                    int oldy = r.getHeightInt();
                    if (oldy != dy) {
                        if (dragDir != HORIZONTAL) {
                            dragDir = VERTICAL;
                            firePropertyChange("relHeight", null, (dy - oldy));
                            return;
                        }
                    }
                }
                boolean isPageBreakMin = false;
                if (isPageBreak)
                    isPageBreakMin = r.getBeatInt() == r.getSecondBeatInt();
                if (!isPageBreakMin && dragMode == RIGHT) {
                    x = (int) (px - beatgap * wSize - 2 + wSize / 2);
                    if (paintHeights)
                        x -= heightBoxWidth;
                    dx = (int) Math.round(x / wSize);
                } else {
                    x = (int) (px - beatgap * wSize - 2 - dragOffsetXRatio * wSize);
                    if (paintHeights)
                        x -= heightBoxWidth;
                    dx = (int) Math.round(x / wSize);
                }
                if (isPageBreakMin || dragMode == CENTER) {
                    int oldx = r.getBeatInt();
                    if (oldx != dx) {
                        if (dragDir != VERTICAL) {
                            dragDir = HORIZONTAL;
                            if (shiftRemainder != 0) {
                                firePropertyChange("relBeatRemainder", null, (dx - oldx));
                            } else {
                                firePropertyChange("relBeat", null, (dx - oldx));
                            }
                        }
                    }
                } else if (dragMode == LEFT) {
                    int oldx = r.getBeatInt();
                    if (oldx != dx) {
                        if (dragDir != VERTICAL) {
                            dragDir = HORIZONTAL;
                            firePropertyChange("relLeft", null, (dx - oldx));
                        }
                    }
                } else {
                    // dragMode==RIGHT
                    int oldx = 0;
                    if (r.isNote())
                        oldx = r.getBeatInt() + r.getLengthInt();
                    else if (r.isPageBreak())
                        oldx = r.getSecondBeatInt();
                    if (oldx != dx) {
                        if (dragDir != VERTICAL) {
                            dragDir = HORIZONTAL;
                            firePropertyChange("relRight", null, (dx - oldx));
                        }
                    }
                }
            }
        });
    }

    public int[] getKeyCodes() {
        return keycodes;
    }

    public void setLyricsWidth(int w) {
        lyricsWidth = w;
    }

    public void setLyricsVisible(boolean onoff) {
        lyricsVisible = onoff;
    }

    public void setDebugMemory(boolean onoff) {
        messageMemory = onoff;
    }

    public void setColors(Color[] c) {
        System.arraycopy(c, 0, colorSet, 0, colorSet.length);
    }

    public Color[] getColors() {
        return colorSet;
    }

    public void shadeNotes(boolean onoff) {
        noshade = !onoff;
    }

    public void setDarkMode(boolean onoff) {
        darkMode = onoff;
        BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        Graphics2D big = bi.createGraphics();
        big.setColor(darkMode ? dkGrayDarkMode : dkGray);
        big.fillRect(0, 0, 4, 2);
        big.setColor(darkMode ? HI_GRAY_2_DARK_MODE : HI_GRAY_2);
        big.fillRect(0, 2, 4, 2);
        Rectangle rec = new Rectangle(0, 0, 4, 4);
        tex = new TexturePaint(bi, rec);

        int w = 16;
        int w2 = w / 2;
        BufferedImage im = new BufferedImage(w, w, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics();
        g.setColor(darkMode ? whiteDarkMode : white);
        g.fillRect(0, 0, w, w);
        g.setColor(darkMode ? HI_GRAY_2_DARK_MODE : HI_GRAY_2);
        g.fillRect(0, 0, w2, w2);
        g.fillRect(w2, w2, w2, w2);
        bgtex = new TexturePaint(im, new Rectangle(w, w));
    }

    public void setAutoTrim(boolean onoff) {
        autoTrim = onoff;
    }

    public void setLyricsLayout(String s) {
        layout = s;
    }

    public int getTopLine() {
        return TOP_LINE;
    }

    public void showArrows(boolean onoff) {
        showArrows = onoff;
    }

    public void showPlayerButtons(boolean onoff) {
        showPlayerButtons = onoff;
    }

    public void showText(boolean onoff) {
        BOTTOM_BORDER = onoff ? 56 : 10;
        showText = onoff;
    }

    public boolean showVideo() {
        return showVideo;
    }

    public boolean showBackground() {
        return showBackground;
    }

    public void showBackground(boolean onoff) {
        showBackground = onoff;
    }

    public void showVideo(boolean onoff) {
        showVideo = onoff;
    }

    private void addSketch(int x, int y) {
        if (sketch == null) {
            sketch = new Point[SKETCH_LENGTH];
        }
        if (sketch[sketchPos] == null) {
            sketch[sketchPos] = new Point();
        }

        sketch[sketchPos].setLocation(x, y);
        if (sketchPos < sketch.length - 1) {
            sketchPos++;
        }
    }

    private void startSketch() {
        sketchPos = dirPos = 0;
        sketchStartTime = System.currentTimeMillis();
        sketchStarted = true;
    }

    private void cancelSketch() {
        sketchStarted = false;
    }

    private boolean sketchStarted() {
        return sketchStarted;
    }

    public void enableSketching(boolean onoff, boolean playonoff) {
        useSketching = onoff;
        useSketchingPlayback = playonoff;
    }

    private boolean detectSketch() {
        if (sketchPos < 3) {
            return true;
        }

        long sketchEndTime = System.currentTimeMillis();

        long ms = sketchEndTime - sketchStartTime;
        boolean intime = ms < 300;
        if (!intime) {
            return false;
        }

        Rectangle r = new Rectangle(sketch[0]);
        for (int i = 1; i < sketchPos; i++) {
            r.add(sketch[i]);
        }

        boolean vertical = r.height < 36 && r.height > r.width && r.height > 2
                && r.width < 10;
        boolean horizontal = r.width < 36 && r.width > r.height && r.width > 2
                && r.height < 10;

        if (!horizontal && !vertical) {
            return false;
        }

        if (sketchDirs == null) {
            sketchDirs = new int[SKETCH_LENGTH];
        }
        sketchDirs[0] = vertical ? SKETCH_VERTICAL : SKETCH_HORIZONTAL;
        dirPos = 1;
        Point s = sketch[1];
        for (int i = 2; i < sketchPos; i++) {
            Point s1 = s;
            s = sketch[i];

            int dx = s.x - s1.x;

            int dy = s.y - s1.y;

            if (horizontal && Math.abs(dx) > Math.abs(dy)) {
                sketchDirs[dirPos] = dx > 0 ? SKETCH_RIGHT : SKETCH_LEFT;
                if (sketchDirs[dirPos] != sketchDirs[dirPos - 1]) {
                    dirPos++;
                }
            } else if (vertical && Math.abs(dy) > Math.abs(dx)) {
                sketchDirs[dirPos] = dy > 0 ? SKETCH_DOWN : SKETCH_UP;
                if (sketchDirs[dirPos] != sketchDirs[dirPos - 1]) {
                    dirPos++;
                }
            }
        }
        sketchDirs[dirPos] = SKETCH_NONE;

		/*
         * System.out.print("p.x"); for (int i = 0; i < sketchPos; i++) {
		 * System.out.print(" " + sketch[i].x); } System.out.println();
		 * System.out.print("p.y"); for (int i = 0; i < sketchPos; i++) {
		 * System.out.print(" " + sketch[i].y); } System.out.println();
		 * System.out.print("dir"); for (int i = 1; i < dirPos; i++) {
		 * System.out.print(" " + sketchDirs[i]); } System.out.println();
		 */
        return true;
    }

    public boolean compareWithGesture(int[] g1, int[] g2) {
        if (g1.length < g2.length) {
            return false;
        }

        int n = g2.length;
        for (int i = 0; i < n; i++) {
            if (g1[i] != g2[i]) {
                return false;
            }
        }
        return true;
    }

    private int executeSketch() {
        // System.out.println("execute");

        if (sketchDirs == null) {
            return 0;
        }
        if (dirPos < 2) {
            return 0;
        }
        if (table.getSelectedRows().length < 1) {
            return 0;
        }

        if (compareWithGesture(sketchDirs, GESTURE_RIGHT_LEFT_RIGHT)) {
            firePropertyChange("rollRight", null, 1);
            // System.out.println("gesture right-left-right");
            return 1;
        } else if (compareWithGesture(sketchDirs, GESTURE_LEFT_RIGHT_LEFT)) {
            firePropertyChange("rollLeft", null, -1);
            // System.out.println("gesture left-right-left");
            return 1;
        } else if (compareWithGesture(sketchDirs, GESTURE_UP_DOWN_UP)) {
            firePropertyChange("removePageBreak", null, 1);
            // System.out.println("gesture up-down-up");
            return 1;
        } else if (compareWithGesture(sketchDirs, GESTURE_DOWN_UP_DOWN)) {
            firePropertyChange("addPageBreak", null, 1);
            // System.out.println("gesture up-down-up");
            return 1;
        } else if (compareWithGesture(sketchDirs, GESTURE_RIGHT_LEFT)) {
            firePropertyChange("relRight", null, 1);
            // System.out.println("gesture right-left");
            return 2;
        } else if (compareWithGesture(sketchDirs, GESTURE_LEFT_RIGHT)) {
            firePropertyChange("relRight", null, -1);
            // System.out.println("gesture left-right");
            return 2;
        } else if (compareWithGesture(sketchDirs, GESTURE_UP_DOWN)) {
            firePropertyChange("join", null, 0.5d);
            // System.out.println("gesture up-down");
            return 1;
        } else if (compareWithGesture(sketchDirs, GESTURE_DOWN_UP)) {
            firePropertyChange("split", null, 0.5d);
            // System.out.println("gesture down-up");
            return 1;
        }

        if (compareWithGesture(sketchDirs, GESTURE_LEFT)) {
            firePropertyChange("relBeat", null, -1);
            table.updatePlayerPosition();
            // System.out.println("gesture left");
            return 2;
        } else if (compareWithGesture(sketchDirs, GESTURE_RIGHT)) {
            firePropertyChange("relBeat", null, 1);
            table.updatePlayerPosition();
            // System.out.println("gesture right");
            return 2;
        } else if (compareWithGesture(sketchDirs, GESTURE_UP)) {
            firePropertyChange("relHeight", null, 1);
            // System.out.println("gesture up");
            return 3;
        } else if (compareWithGesture(sketchDirs, GESTURE_DOWN)) {
            firePropertyChange("relHeight", null, -1);
            // System.out.println("gesture down");
            return 3;
        }
        return 0;
    }

    public void setMouseOver(boolean onoff) {
        mouseover = onoff;
    }

    public void setActions(YassActions a) {
        actions = a;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean onoff) {
        isPlaying = onoff;
    }

    public boolean isTemporaryStop() {
        return isTemporaryStop;
    }

    public void setTemporaryStop(boolean onoff) {
        isTemporaryStop = onoff;
    }

    protected void setCurrentLineTo(int line) {
        table.setCurrentLineTo(line);
    }

    public boolean isSnapshotShown() {
        return paintSnapshot;
    }

    public void showSnapshot(boolean onoff) {
        paintSnapshot = onoff;
    }

    public void removeSnapshot() {
        snapshot = null;
    }

    public void makeSnapshot() {
        int i = table.getSelectionModel().getMinSelectionIndex();
        int j = table.getSelectionModel().getMaxSelectionIndex();
        if (i >= 0) {
            YassRow r = table.getRowAt(i);
            while (!r.isNote() && i <= j) {
                r = table.getRowAt(++i);
            }
            inSelect = fromTimeline(beatToTimeline(r.getBeatInt()));

            r = table.getRowAt(j);
            while (!r.isNote() && j > i) {
                r = table.getRowAt(--j);
            }
            outSelect = fromTimeline(beatToTimeline(r.getBeatInt()
                    + r.getLengthInt()));
            createSnapshot();
        } else {
            createSnapshot();
        }
    }

    public void createSnapshot() {
        inSnapshot = inSelect;
        outSnapshot = outSelect;

        int i = table.getSelectionModel().getMinSelectionIndex();
        int j = table.getSelectionModel().getMaxSelectionIndex();
        if (i < 0) {
            return;
        }

        int n = j - i + 1;
        snapshot = new Vector<>(n);
        snapshotRect = new Vector<>(n);
        int startx = -1;
        for (int k = i; k <= j; k++) {
            YassRow row = table.getRowAt(k);
            snapshot.addElement(row);

            YassRectangle r = rect.elementAt(k);
            r = (YassRectangle) r.clone();
            if (startx < 0) {
                startx = (int) r.x;
            }
            r.x -= startx;
            snapshotRect.addElement(r);
        }
    }

    public void addTable(YassTable t) {
        tables.addElement(t);
        rects.addElement(new Vector<>(3000, 1000));
    }

    public void removeTable(YassTable t) {
        int i = tables.indexOf(t);
        if (i >= 0) {
            tables.removeElementAt(i);
            rects.removeElementAt(i);
        }
    }

    public void setActiveTable(int i) {
        YassTable t = tables.elementAt(i);
        setActiveTable(t);
    }

    public BufferedImage getBackgroundImage() {
        return bgImage;
    }

    public void setBackgroundImage(BufferedImage i) {
        bgImage = i;
    }

    public YassTable getActiveTable() {
        return table;
    }

    public YassTable getTable(int track) {
        if (track < 0 || track >= tables.size())
            return null;
        return tables.elementAt(track);
    }

    public int getTableCount() {
        return tables.size();
    }

    public void setActiveTable(YassTable t) {
        table = t;
        if (table != null) {
            int k = tables.indexOf(table);
            rect = rects.elementAt(k);
        }
        init();
    }

    public void removeAll() {
        tables.clear();
        rects.clear();
        snapshot = null;
        rect = null;
        table = null;
        gap = 0;
        beatgap = 0;
        outgap = 0;
        bpm = 120;
        setDuration(-1);
        init();
        setZoom(80 * 60 / bpm);
    }

    public void setNoteLengthVisible(boolean onoff) {
        showNoteLength = onoff;
    }

    public void setNoteScaleVisible(boolean onoff) {
        showNoteScale = onoff;
    }

    public void setNoteBeatVisible(boolean onoff) {
        showNoteBeat = onoff;
    }

    public void setNoteHeightVisible(boolean onoff) {
        showNoteHeight = onoff;
    }

    public void setNoteHeightNumVisible(boolean onoff) {
        showNoteHeightNum = onoff;
    }

    public boolean isVisible(int i) {
        YassRectangle r = rect.elementAt(i);
        if (r == null || r.y < 0)
            return false;
        return r.x >= getLeftX() && r.x + r.width <= clip.x + clip.width - RIGHT_BORDER;
    }

    public void scrollRectToVisible(int i, int j) {
        int minx = Integer.MAX_VALUE;
        for (int k = i; k <= j; k++) {
            if (k >= table.getRowCount()) {
                return;
            }
            YassRectangle r = rect.elementAt(k);

            double x = r.x;
            if (r.isType(YassRectangle.HEADER) || r.isType(YassRectangle.START)) {
                x = paintHeights ? heightBoxWidth : 0;
            } else if (r.isType(YassRectangle.END)) {
                x = beatToTimeline(outgap);
            } else if (r.isType(YassRectangle.UNDEFINED)) {
                continue;
            } else if (r.y < 0) {
                continue;
            }

            minx = (int) Math.min(x, minx);
        }
        setLeftX(minx);
    }

    public Point getViewPosition() {
        return ((JViewport) getParent()).getViewPosition();
    }

    public void setViewPosition(Point p) {
        ((JViewport) getParent()).setViewPosition(p);
        clip = getClipBounds();
    }

    public int getLeftX() {
        int x = getViewPosition().x;
        if (paintHeights) {
            x += heightBoxWidth;
        }

        x += LEFT_BORDER;
        return x;
    }

    public void setLeftX(int x) {
        if (paintHeights) {
            x -= heightBoxWidth;
        }

        x -= LEFT_BORDER;

        setViewPosition(new Point(x, 0));
    }

    /**
     * Gets the clipBounds attribute of the YassSheet object
     *
     * @return The clipBounds value
     */
    public Rectangle getClipBounds() {
        return ((JViewport) getParent()).getViewRect();
    }

    /**
     * Gets the validateRoot attribute of the YassSheet object
     *
     * @return The validateRoot value
     */
    public boolean isValidateRoot() {
        return true;
    }

    /**
     * Description of the Method
     */
    public void revalidate() {
        super.revalidate();
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public void paint(Graphics g) {
        super.paint(g);
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public void paintChildren(Graphics g) {
        if (table == null || table.getRowCount() < 1) {
            Graphics2D g2d = (Graphics2D) g;
            int dw = getWidth();
            int dh = getHeight();

            g.setColor(darkMode ? hiGrayDarkMode : hiGray);
            g2d.fillRect(0, 0, dw, dh);
        }
    }

    /**
     * Description of the Method
     */
    public void repaint() {
        if (rect == null || rect.size() < 1) {
            return;
        }
        if (isPlaying()) {
            return;
        }
        if (isLive()) {
            return;
        }
        super.repaint();
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public void paintComponent(Graphics g) {
        if (table == null || rect == null || rect.size() < 1) {
            return;
        }
        if (isPlaying()) {
            return;
        }

        if (hSize < 0 || (beatgap == 0 && gap != 0)) {
            update();
        }

        Graphics2D g2 = (Graphics2D) g;
        if (isPlaying()) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        clip = getClipBounds();
        if (image == null || image.getWidth() != clip.width
                || image.getHeight() != clip.height) {
            // do not use INT_RGB for width>2000 (sun bug_id=5005969)
            // image = new BufferedImage(clip.width, clip.height,
            // BufferedImage.TYPE_INT_ARGB);

            image = g2.getDeviceConfiguration().createCompatibleImage(
                    clip.width, clip.height, Transparency.TRANSLUCENT);
            backVolImage = g2.getDeviceConfiguration()
                    .createCompatibleVolatileImage(clip.width, clip.height,
                            Transparency.OPAQUE);
            plainVolImage = g2.getDeviceConfiguration()
                    .createCompatibleVolatileImage(clip.width, clip.height,
                            Transparency.OPAQUE);
            imageChanged = true;
        }

        refreshImage();

        // http://weblogs.java.net/blog/chet/archive/2005/05/graphics_accele.html
        // http://weblogs.java.net/blog/chet/archive/2004/08/toolkitbuffered.html
        // http://today.java.net/pub/a/today/2004/11/12/graphics2d.html

        Graphics2D gb = getBackBuffer().createGraphics();
        gb.drawImage(getPlainBuffer(), 0, 0, null);
        if (getPlainBuffer().contentsLost()) {
            // setErrorMessage(bufferlost);
            revalidate();
        }

        paintText(gb);
        if (showText) {
            paintPlayerText(gb);
        }
        paintPlayerPosition(gb);
        if (showPlayerButtons && !live) {
            paintPlayerButtons(gb);
        }
        if (!live) {
            paintSketch(gb);
        }
        gb.dispose();

        paintBackBuffer(g2);

        if (!live) {
            paintMessage(g2);
        }
    }

    /**
     * Gets the backBuffer attribute of the YassSheet object
     *
     * @return The backBuffer value
     */
    public VolatileImage getBackBuffer() {
        return backVolImage;
    }

    /**
     * Gets the plainBuffer attribute of the YassSheet object
     *
     * @return The plainBuffer value
     */
    public VolatileImage getPlainBuffer() {
        return plainVolImage;
    }

    /**
     * Gets the refreshing attribute of the YassSheet object
     *
     * @return The refreshing value
     */
    public boolean isRefreshing() {
        return refreshing;
    }

    /**
     * Description of the Method
     */
    public void refreshImage() {
        refreshing = true;

        Graphics2D db = image.createGraphics();
        db.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        db.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        db.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        clip = getClipBounds();
        db.setTransform(identity);
        db.setClip(0, 0, clip.width, clip.height);
        db.translate(-clip.x, -clip.y);
        if (!imageChanged) {
            imageChanged = clip.x != imageX;
        }

        paintEmptySheet(db);

        YassPlayer mp3 = actions != null ? actions.getMP3() : null;
        if (mp3 != null && mp3.createWaveform()) {
            paintWaveform(db);
        }

        if (!showVideo() && !showBackground()) {
            paintBeatLines(db);
        }
        paintLines(db);
        if (!live) {
            paintBeats(db);
            paintInOut(db);
            if (paintSnapshot) {
                paintSnapshot(db);
            }
        }

        paintRectangles(db);
        if (paintHeights) {
            paintHeightBox(db);
        }

        paintVersionsText(db);
        if (showArrows && !live) {
            paintArrows(db);
            paintSlideArrows(db);
        }

        if (messageMemory && !live) {
            db.setFont(font);
            int maxHeap = (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024);
            int occHeap = (int) (Runtime.getRuntime().totalMemory() / 1024 / 1024);
            int freeHeap = (int) (Runtime.getRuntime().freeMemory() / 1024 / 1024);
            int usedHeap = occHeap - freeHeap;
            String info = usedHeap + " of " + maxHeap + "Mb in use" + ", "
                    + occHeap + "Mb reserved.";
            if (layout.equals("East")) {
                db.drawString(info, clip.x + 10, 40);
            } else if (layout.equals("West")) {
                db.drawString(info, clip.x + 10 + lyricsWidth, 40);
            }
        }

        // message:
        // LYRICS POSITION
        if (getComponentCount() > 0
                && lyricsVisible) {
            YassLyrics c = (YassLyrics) getComponent(0);
            Rectangle cr = c.getBounds();

            if (layout.equals("East")) {
                db.translate(
                        clip.x + clip.width - cr.width + cr.getX()
                                - c.getX(), cr.getY() - c.getY() + 20);
            } else if (layout.equals("West")) {
                db.translate(clip.x, cr.getY() - c.getY() + 20);
            }

            // System.out.println("refresh print");
            c.print(db);
            // System.out.println("refresh print done");
        }
        // paintText(db);

        imageChanged = false;
        imageX = clip.x;

        db.dispose();

        Graphics2D gc = backVolImage.createGraphics();
        gc.drawImage(image, 0, 0, null);
        gc.dispose();

        gc = plainVolImage.createGraphics();
        gc.drawImage(image, 0, 0, null);
        gc.dispose();

        refreshing = false;
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public synchronized void paintBackBuffer(Graphics2D g) {
        final int MAX_TRIES = 5;
        for (int i = 0; i < MAX_TRIES; i++) {

            // switch (backVolImage.validate(g.getDeviceConfiguration())) {
            switch (backVolImage.validate(getGraphicsConfiguration())) {
                case VolatileImage.IMAGE_INCOMPATIBLE:
                    backVolImage.flush();
                    backVolImage = null;
                    image.flush();
                    image = null;
                    plainVolImage.flush();
                    plainVolImage = null;
                    image = g.getDeviceConfiguration().createCompatibleImage(
                            clip.width, clip.height, Transparency.TRANSLUCENT);
                    backVolImage = g.getDeviceConfiguration()
                            .createCompatibleVolatileImage(clip.width, clip.height,
                                    Transparency.OPAQUE);
                    plainVolImage = g.getDeviceConfiguration()
                            .createCompatibleVolatileImage(clip.width, clip.height,
                                    Transparency.OPAQUE);
                    // backVolImage = createVolatileImage(clip.width, clip.height);
                case VolatileImage.IMAGE_RESTORED:
                    Graphics2D gc = backVolImage.createGraphics();
                    gc.drawImage(image, 0, 0, white, null);
                    gc.dispose();
                    break;
            }

            g.drawImage(backVolImage, clip.x, clip.y, this);
            if (!backVolImage.contentsLost()) {
                return;
            }
            System.out.println("contents lost (" + i + ")");
        }
        g.drawImage(image, clip.x, clip.y, clip.x + clip.width, clip.y
                + clip.height, 0, 0, clip.width, clip.height, white, this);
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     * @param w Description of the Parameter
     * @param h Description of the Parameter
     */
    public synchronized void paintBackBuffer(Graphics2D g, int x, int y, int w,
                                             int h) {
        final int MAX_TRIES = 5;
        for (int i = 0; i < MAX_TRIES; i++) {

            // switch (backVolImage.validate(g.getDeviceConfiguration())) {
            switch (backVolImage.validate(getGraphicsConfiguration())) {
                case VolatileImage.IMAGE_INCOMPATIBLE:
                    backVolImage.flush();
                    backVolImage = null;
                    image.flush();
                    image = null;
                    plainVolImage.flush();
                    plainVolImage = null;
                    image = g.getDeviceConfiguration().createCompatibleImage(
                            clip.width, clip.height, Transparency.TRANSLUCENT);
                    backVolImage = g.getDeviceConfiguration()
                            .createCompatibleVolatileImage(clip.width, clip.height,
                                    Transparency.OPAQUE);
                    plainVolImage = g.getDeviceConfiguration()
                            .createCompatibleVolatileImage(clip.width, clip.height,
                                    Transparency.OPAQUE);
                    // backVolImage = createVolatileImage(clip.width, clip.height);
                case VolatileImage.IMAGE_RESTORED:
                    Graphics2D gc = backVolImage.createGraphics();
                    gc.drawImage(image, 0, 0, white, null);
                    gc.dispose();
                    break;
            }

            g.drawImage(backVolImage, clip.x + x, clip.y + y, clip.x + x + w,
                    clip.y + y + h, x, y, x + w, y + h, this);
            if (!backVolImage.contentsLost()) {
                return;
            }
            System.out.println("contents lost (" + i + ")");
        }
        g.drawImage(image, clip.x, clip.y, clip.x + clip.width, clip.y
                + clip.height, 0, 0, clip.width, clip.height, white, this);
    }

    /**
     * Description of the Method
     *
     * @param onoff Description of the Parameter
     */
    public void previewEdit(boolean onoff) {
        actions.previewEdit(onoff);
    }

    /**
     * Description of the Method
     *
     * @param g2 Description of the Parameter
     */
    public void paintEmptySheet(Graphics2D g2) {
        BufferedImage img = null;
        if (videoFrame != null && showVideo) {
            img = videoFrame;
        }
        if (img == null && showBackground) {
            img = bgImage;
        }

        if (img != null) {
            int w = clip.width;
            int h = (int) (w * 3 / 4.0);
            int yy = clip.height / 2 - h / 2;

            g2.setColor(darkMode ? whiteDarkMode : white);
            g2.fillRect(clip.x, 0, clip.width, yy);
            g2.fillRect(clip.x, yy, clip.width, clip.height);

            g2.drawImage(img, clip.x, clip.y + yy, w, h, null);
        } else {
            g2.setColor(darkMode ? whiteDarkMode : white);
            g2.fillRect(clip.x, clip.y, clip.width, clip.height);

            g2.setPaint(bgtex);

            // LYRICS POSITION

            if (lyricsVisible) {
                if (layout.equals("East")) {
                    g2.fillRect(clip.x, TOP_BORDER, clip.width - lyricsWidth,
                            TOP_LINE - 10 - TOP_BORDER - 1);
                } else if (layout.equals("West")) {
                    g2.fillRect(clip.x + lyricsWidth, TOP_BORDER, clip.width
                            - lyricsWidth, TOP_LINE - 10 - TOP_BORDER - 1);
                }
                if (live) {
                    g2.fillRect(clip.x, dim.height - BOTTOM_BORDER + 16,
                            clip.width, BOTTOM_BORDER + 16);
                } else {
                    g2.fillRect(clip.x + LEFT_BORDER, dim.height
                            - BOTTOM_BORDER + 16, clip.width - LEFT_BORDER
                            - RIGHT_BORDER, BOTTOM_BORDER + 16);
                }
            } else {
                g2.fillRect(clip.x, TOP_BORDER, clip.width, TOP_LINE - 10
                        - TOP_BORDER - 1);
                if (live) {
                    g2.fillRect(clip.x, dim.height - BOTTOM_BORDER + 16,
                            clip.width, BOTTOM_BORDER + 16);
                } else {
                    g2.fillRect(clip.x + LEFT_BORDER, dim.height
                            - BOTTOM_BORDER + 16, clip.width - LEFT_BORDER
                            - RIGHT_BORDER, BOTTOM_BORDER + 16);
                }
            }
            g2.setColor(darkMode ? whiteDarkMode : white);
        }
    }

    /**
     * Description of the Method
     *
     * @param g2 Description of the Parameter
     */
    public void paintWaveform(Graphics2D g2) {
        g2.setColor(Color.green);

        int h = TOP_LINE - 10 + 128;

        YassPlayer mp3 = actions.getMP3();

        int lasty = 0;
        for (int x = clip.x + 1; x < clip.x + clip.width; x++) {
            double ms = fromTimelineExact(x);
            int y = mp3.getWaveFormAtMillis(ms);
            g2.drawLine(x - 1, h - lasty, x, h - y);
            lasty = y;
        }

        g2.setColor(darkMode ? whiteDarkMode : white);
    }

    /**
     * Description of the Method
     *
     * @param g2 Description of the Parameter
     */
    public void paintArrows(Graphics2D g2) {

        int x = clip.x;// + (paintHeights ? heightBoxWidth : 0);
        int y = dim.height - BOTTOM_BORDER + 16;
        int w = LEFT_BORDER;
        int h = BOTTOM_BORDER - 16;

        Color fg = darkMode ? hiGrayDarkMode : hiGray;
        Color sh = darkMode ? HI_GRAY_2_DARK_MODE : HI_GRAY_2;
        Color wt = darkMode ? whiteDarkMode : white;
        Color arr = darkMode ? arrowDarkMode : arrow;

        boolean isPressed = hiliteCue == PREV_PAGE_PRESSED;
        g2.setColor(isPressed ? fg : arr);
        g2.fillRect(x, y, w, h);

        if (isPressed) {
            g2.setColor(sh);
            g2.drawRect(x, y, w - 1, h - 1);
        } else {
            g2.setColor(fg);
            g2.drawLine(x, y, x, y + h - 1);
            g2.drawLine(x + 1, y, x + w - 2, y);

            g2.setColor(wt);
            g2.drawLine(x + 1, y + 1, x + 1, y + h - 3);
            g2.drawLine(x + 2, y + 1, x + w - 3, y + 1);

            g2.setColor(sh);
            g2.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
            g2.drawLine(x + w - 2, y + 1, x + w - 2, y + h - 3);

            g2.setColor(fg);
            g2.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
            g2.drawLine(x + w - 1, y + h - 1, x + w - 1, y);
        }

        boolean isEnabled = hiliteCue == PREV_PAGE
                || hiliteCue == PREV_PAGE_PRESSED;
        YassUtils.paintTriangle(g2, x + 10, y + 14, w / 3, YassUtils.NORTH,
                isEnabled, fg, sh, wt);

        x = clip.x + clip.width - RIGHT_BORDER;
        y = dim.height - BOTTOM_BORDER + 16;
        w = RIGHT_BORDER;
        h = BOTTOM_BORDER - 16;

        isPressed = hiliteCue == NEXT_PAGE_PRESSED;
        g2.setColor(isPressed ? fg : arr);
        g2.fillRect(x, y, w, h);
        if (isPressed) {
            g2.setColor(sh);
            g2.drawRect(x, y, w - 1, h - 1);
        } else {
            g2.setColor(fg);
            g2.drawLine(x, y, x, y + h - 1);
            g2.drawLine(x + 1, y, x + w - 2, y);

            g2.setColor(wt);
            g2.drawLine(x + 1, y + 1, x + 1, y + h - 3);
            g2.drawLine(x + 2, y + 1, x + w - 3, y + 1);

            g2.setColor(sh);
            g2.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
            g2.drawLine(x + w - 2, y + 1, x + w - 2, y + h - 3);

            g2.setColor(fg);
            g2.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
            g2.drawLine(x + w - 1, y + h - 1, x + w - 1, y);
        }

        isEnabled = hiliteCue == NEXT_PAGE || hiliteCue == NEXT_PAGE_PRESSED;
        YassUtils.paintTriangle(g2, x + 10, y + 14, w / 3, YassUtils.SOUTH,
                isEnabled, fg, sh, wt);
    }
    /**
     * Description of the Method
     *
     * @param g2 Description of the Parameter
     */
    public void paintSlideArrows(Graphics2D g2) {

        int x = clip.x + LEFT_BORDER;
        int y = dim.height - BOTTOM_BORDER + 16;
        int w = LEFT_BORDER;
        int h = BOTTOM_BORDER - 16;

        Color fg = darkMode ? hiGrayDarkMode : hiGray;
        Color sh = darkMode ? HI_GRAY_2_DARK_MODE : HI_GRAY_2;
        Color wt = darkMode ? whiteDarkMode : white;
        Color arr = darkMode ? arrowDarkMode : arrow;

        boolean isPressed = hiliteCue == PREV_SLIDE_PRESSED;
        g2.setColor(isPressed ? fg : arr);
        g2.fillRect(x, y, w, h);

        if (isPressed) {
            g2.setColor(sh);
            g2.drawRect(x, y, w - 1, h - 1);
        } else {
            g2.setColor(fg);
            g2.drawLine(x, y, x, y + h - 1);
            g2.drawLine(x + 1, y, x + w - 2, y);

            g2.setColor(wt);
            g2.drawLine(x + 1, y + 1, x + 1, y + h - 3);
            g2.drawLine(x + 2, y + 1, x + w - 3, y + 1);

            g2.setColor(sh);
            g2.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
            g2.drawLine(x + w - 2, y + 1, x + w - 2, y + h - 3);

            g2.setColor(fg);
            g2.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
            g2.drawLine(x + w - 1, y + h - 1, x + w - 1, y);
        }

        boolean isEnabled = hiliteCue == PREV_SLIDE
                || hiliteCue == PREV_SLIDE_PRESSED;
        YassUtils.paintTriangle(g2, x + 10, y + 14, w / 3, YassUtils.WEST,
                isEnabled, fg, sh, wt);

        x = clip.x + clip.width - RIGHT_BORDER - RIGHT_BORDER;
        y = dim.height - BOTTOM_BORDER + 16;
        w = RIGHT_BORDER;
        h = BOTTOM_BORDER - 16;

        isPressed = hiliteCue == NEXT_SLIDE_PRESSED;
        g2.setColor(isPressed ? fg : arr);
        g2.fillRect(x, y, w, h);
        if (isPressed) {
            g2.setColor(sh);
            g2.drawRect(x, y, w - 1, h - 1);
        } else {
            g2.setColor(fg);
            g2.drawLine(x, y, x, y + h - 1);
            g2.drawLine(x + 1, y, x + w - 2, y);

            g2.setColor(wt);
            g2.drawLine(x + 1, y + 1, x + 1, y + h - 3);
            g2.drawLine(x + 2, y + 1, x + w - 3, y + 1);

            g2.setColor(sh);
            g2.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
            g2.drawLine(x + w - 2, y + 1, x + w - 2, y + h - 3);

            g2.setColor(fg);
            g2.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
            g2.drawLine(x + w - 1, y + h - 1, x + w - 1, y);
        }

        isEnabled = hiliteCue == NEXT_SLIDE || hiliteCue == NEXT_SLIDE_PRESSED;
        YassUtils.paintTriangle(g2, x + 10, y + 14, w / 3, YassUtils.EAST,
                isEnabled, fg, sh, wt);
    }

    /**
     * Description of the Method
     *
     * @param g2 Description of the Parameter
     */
    public void paintPlayerButtons(Graphics2D g2) {
        // if (!paintArrows) return;

        TOP_PLAYER_BUTTONS = dim.height - BOTTOM_BORDER - 64;

        int next = nextElementStarting(playerPos);
        if (next >= 0) {
            YassRectangle rec = rect.elementAt(next);
            if (rec.hasType(YassRectangle.GAP) && next + 1 < rect.size()) {
                rec = rect.elementAt(next + 1);
            }
            if (rec.y + rec.height > TOP_PLAYER_BUTTONS) {
                TOP_PLAYER_BUTTONS = TOP_LINE - 10;
            }
        }

        // play current note

        int x = playerPos - clip.x + PLAY_NOTE_X;
        int y = TOP_PLAYER_BUTTONS;
        int w = PLAY_NOTE_W;
        int h = 64;

        Color fg = darkMode ? hiGrayDarkMode : hiGray;
        Color sh = darkMode ? HI_GRAY_2_DARK_MODE : HI_GRAY_2;
        Color wt = darkMode ? whiteDarkMode : white;
        Color arr = darkMode ? arrowDarkMode : arrow;

        boolean isPressed = hiliteCue == PLAY_NOTE_PRESSED;
        g2.setColor(isPressed ? fg : arr);
        g2.fillRect(x, y, w, h);

        if (isPressed) {
            g2.setColor(sh);
            g2.drawRect(x, y, w - 1, h - 1);
        } else {
            g2.setColor(fg);
            g2.drawLine(x, y, x, y + h - 1);
            g2.drawLine(x + 1, y, x + w - 2, y);

            g2.setColor(wt);
            g2.drawLine(x + 1, y + 1, x + 1, y + h - 3);
            g2.drawLine(x + 2, y + 1, x + w - 3, y + 1);

            g2.setColor(sh);
            g2.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
            g2.drawLine(x + w - 2, y + 1, x + w - 2, y + h - 3);

            g2.setColor(fg);
            g2.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
            g2.drawLine(x + w - 1, y + h - 1, x + w - 1, y);
        }
        boolean isEnabled = hiliteCue == PLAY_NOTE_PRESSED || hiliteCue == PLAY_NOTE;
        YassUtils.paintTriangle(g2, x + 16, y + 24, 18, YassUtils.EAST,
                isEnabled, fg, sh, wt);


        // play note before

        x = playerPos - clip.x + PLAY_BEFORE_X;
        y = TOP_PLAYER_BUTTONS;
        w = PLAY_BEFORE_W;
        h = 64;

        isPressed = hiliteCue == PLAY_BEFORE_PRESSED;
        g2.setColor(isPressed ? fg : arr);
        g2.fillRect(x, y, w, h);

        if (isPressed) {
            g2.setColor(sh);
            g2.drawRect(x, y, w - 1, h - 1);
        } else {
            g2.setColor(fg);
            g2.drawLine(x, y, x, y + h - 1);
            g2.drawLine(x + 1, y, x + w - 2, y);

            g2.setColor(wt);
            g2.drawLine(x + 1, y + 1, x + 1, y + h - 3);
            g2.drawLine(x + 2, y + 1, x + w - 3, y + 1);

            g2.setColor(sh);
            g2.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
            g2.drawLine(x + w - 2, y + 1, x + w - 2, y + h - 3);

            g2.setColor(fg);
            g2.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
            g2.drawLine(x + w - 1, y + h - 1, x + w - 1, y);
        }
        isEnabled = hiliteCue == PLAY_BEFORE || hiliteCue == PLAY_BEFORE_PRESSED;
        g2.setColor(isEnabled ? sh : fg);
        g2.fillRect(x + 22, y + 21, 3, 23);
        YassUtils.paintTriangle(g2, x + 10, y + 27, 12, YassUtils.EAST,
                isEnabled, fg, sh, wt);

        // play note next

        x = playerPos - clip.x + PLAY_NEXT_X;
        y = TOP_PLAYER_BUTTONS;
        w = PLAY_NEXT_W;
        h = 64;

        isPressed = hiliteCue == PLAY_NEXT_PRESSED;
        g2.setColor(isPressed ? fg : arr);
        g2.fillRect(x, y, w, h);

        if (isPressed) {
            g2.setColor(sh);
            g2.drawRect(x, y, w - 1, h - 1);
        } else {
            g2.setColor(fg);
            g2.drawLine(x, y, x, y + h - 1);
            g2.drawLine(x + 1, y, x + w - 2, y);

            g2.setColor(wt);
            g2.drawLine(x + 1, y + 1, x + 1, y + h - 3);
            g2.drawLine(x + 2, y + 1, x + w - 3, y + 1);

            g2.setColor(sh);
            g2.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
            g2.drawLine(x + w - 2, y + 1, x + w - 2, y + h - 3);

            g2.setColor(fg);
            g2.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
            g2.drawLine(x + w - 1, y + h - 1, x + w - 1, y);
        }
        isEnabled = hiliteCue == PLAY_NEXT || hiliteCue == PLAY_NEXT_PRESSED;
        g2.setColor(isEnabled ? sh : fg);
        g2.fillRect(x + 10, y + 21, 3, 23);
        YassUtils.paintTriangle(g2, x + 15, y + 27, 12, YassUtils.EAST,
                isEnabled, fg, sh, wt);

        // play page

        x = playerPos - clip.x + PLAY_PAGE_X;
        y = TOP_PLAYER_BUTTONS;
        w = PLAY_PAGE_W;
        h = 64;

        isPressed = hiliteCue == PLAY_PAGE_PRESSED;
        g2.setColor(isPressed ? fg : arr);
        g2.fillRect(x, y, w, h);

        if (isPressed) {
            g2.setColor(sh);
            g2.drawRect(x, y, w - 1, h - 1);
        } else {
            g2.setColor(fg);
            g2.drawLine(x, y, x, y + h - 1);
            g2.drawLine(x + 1, y, x + w - 2, y);

            g2.setColor(wt);
            g2.drawLine(x + 1, y + 1, x + 1, y + h - 3);
            g2.drawLine(x + 2, y + 1, x + w - 3, y + 1);

            g2.setColor(sh);
            g2.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
            g2.drawLine(x + w - 2, y + 1, x + w - 2, y + h - 3);

            g2.setColor(fg);
            g2.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
            g2.drawLine(x + w - 1, y + h - 1, x + w - 1, y);
        }
        isEnabled = hiliteCue == PLAY_PAGE || hiliteCue == PLAY_PAGE_PRESSED;
        g2.setColor(isEnabled ? sh : fg);
        g2.fillRect(x + 10, y + 30, 14, 3);
        YassUtils.paintTriangle(g2, x + 4, y + 28, 8, YassUtils.WEST,
                isEnabled, fg, sh, wt);
        YassUtils.paintTriangle(g2, x + 24, y + 28, 8, YassUtils.EAST,
                isEnabled, fg, sh, wt);

    }

    /**
     * Description of the Method
     *
     * @param g2 Description of the Parameter
     */
    public void paintBeats(Graphics2D g2) {
        int off = 0;
        if (paintHeights) {
            off += heightBoxWidth;
        }
        g2.setColor(darkMode ? HI_GRAY_2_DARK_MODE : HI_GRAY_2);
        g2.fillRect((int) (beatgap * wSize + off), 0, dim.width, TOP_BORDER);
        g2.setFont(smallFont);
        FontMetrics metrics = g2.getFontMetrics();

        int multiplier = 1;
        double wwSize = wSize;
        while (wwSize < 10) {
            wwSize *= 4.0;
            multiplier *= 4;
        }
        String str;
        long ms;
        Line2D.Double line = new Line2D.Double(0, 20, 0, 28);
        double leftx = clip.x;
        double rightx = clip.x + clip.width;
        g2.setColor(darkMode ? hiGrayDarkMode : hiGray);
        g2.setStroke(thinStroke);
        int i = 0, j, strw;
        while (true) {
            line.x1 = line.x2 = (beatgap + i) * wSize + off;
            if (line.x1 < leftx) {
                i++;
                continue;
            }
            if (line.x1 > rightx) {
                break;
            }
            j = i / multiplier;
            if (multiplier == 1 || i % multiplier == 0) {
                g2.setStroke(j % 4 == 0 ? stdStroke : thinStroke);
                g2.setColor(j % 4 == 0 ? (darkMode ? dkGrayDarkMode : dkGray) : (darkMode ? hiGrayDarkMode : hiGray));
                g2.draw(line);
                if (j % 4 == 0) {
                    g2.setColor(darkMode ? hiGrayDarkMode : hiGray);
                    str = Integer.toString(i);
                    strw = metrics.stringWidth(str);
                    g2.drawString(str, (float) (line.x1 - strw / 2), 8f);

                    ms = (long) table.beatToMs(i);
                    str = YassUtils.commaTime(ms) + "s";
                    strw = metrics.stringWidth(str);
                    g2.drawString(str, (float) (line.x1 - strw / 2), 18f);
                }
            }
            i++;
        }
    }

    /**
     * Description of the Method
     *
     * @param g2 Description of the Parameter
     */
    public void paintBeatLines(Graphics2D g2) {
        if (minHeight > maxHeight) {
            return;
        }

        g2.setStroke(thinStroke);
        g2.setColor(darkMode ? hiGrayDarkMode : hiGray);
        double miny = dim.height - BOTTOM_BORDER;
        double maxy;
        if (pan) {
            maxy = dim.height - BOTTOM_BORDER - ((double) (2 * (NORM_HEIGHT - 1)) / 2) * hSize + 1;
        } else {
            maxy = dim.height - BOTTOM_BORDER - ((double) (2 * (maxHeight - 1)) / 2 - minHeight) * hSize + 1;
        }

        int multiplier = 1;
        double wwSize = wSize;
        while (wwSize < 10) {
            wwSize *= 4.0;
            multiplier *= 4;
        }

        float firstB = -1;
        double leftx = getLeftX() - LEFT_BORDER;
        double rightx = clip.getX() + clip.getWidth();
        Line2D.Double line = new Line2D.Double(0, maxy, 0, miny);
        int off = 0;
        if (paintHeights) {
            off += heightBoxWidth;
        }
        int i = 0;
        int j;
        while (true) {
            line.x1 = line.x2 = (beatgap + i) * wSize + off;
            if (line.x1 < leftx) {
                i++;
                continue;
            }
            if (line.x1 > rightx) {
                break;
            }
            j = i / multiplier;
            if (multiplier == 1 || i % multiplier == 0) {
                if (firstB < 0 && j % 4 == 0) {
                    firstB = (float) line.x1;
                }
                g2.setStroke(j % 4 == 0 ? (showBackground || showVideo) ? thickStroke
                        : stdStroke
                        : thinStroke);
                g2.setColor(j % 4 == 0 ? (darkMode ? hiGrayDarkMode : hiGray) : (darkMode ? HI_GRAY_2_DARK_MODE : HI_GRAY_2));
                g2.draw(line);
            }
            i++;
        }

        if (!live) {
            if (firstB < 0) {
                return;
            }
            line.x1 = firstB;
            line.x2 = line.x1 + 4 * multiplier * wSize;
            line.y1 = line.y2 = TOP_LINE - 10;
            g2.setStroke(thickStroke);
            g2.setColor(darkMode ? dkGrayDarkMode : dkGray);
            g2.draw(line);
            String bstr = "B";
            if (multiplier > 1) {
                bstr = bstr + "/" + multiplier;
            }
            g2.drawString(bstr, (float) (line.x1 + line.x2) / 2f, (float) (line.y2 - 2));
            line.x1 = line.x2;
            line.y1 -= 2;
            line.y2 += 2;
            g2.draw(line);
            line.x1 = line.x2 = firstB;
            g2.draw(line);
        }
    }

    /**
     * Description of the Method
     *
     * @param g2 Description of the Parameter
     */
    public void paintLines(Graphics2D g2) {
        Line2D.Double line = new Line2D.Double(getLeftX() - LEFT_BORDER, 0,
                clip.x + clip.width, 0);
        if (pan) {
            g2.setColor(darkMode ? HI_GRAY_2_DARK_MODE : HI_GRAY_2);
            g2.setStroke(stdStroke);
            for (int h = 0; h < NORM_HEIGHT; h += 2) {
                line.y1 = line.y2 = dim.height - BOTTOM_BORDER - h * hSize;
                g2.draw(line);
            }
        } else {
            // scale with alternating background
            g2.setColor(new Color(0, 0, 0, 10));
            for (int h = minHeight; h < maxHeight; h++) {
                if (h % 2 != 0) {
                    continue;
                }
                double y = dim.height - BOTTOM_BORDER - (h - minHeight) * hSize;
                if ((h+12) % 24 == 0) {
                    g2.fillRect((int) line.x1, (int) (y - 12 * hSize), (int) (line.x2 - line.x1), (int) (12 * hSize));
                }
            }
            // lowest scale might be visible partly
            int mh = minHeight;
            if ((mh+12) % 24 != 0) {
                while (mh % 12 != 0) mh++;
                if (mh % 24 == 0) {
                    double y = dim.height - BOTTOM_BORDER - (mh - minHeight) * hSize;
                    g2.fillRect((int) line.x1, (int) y, (int) (line.x2 - line.x1), (int) ((mh - minHeight) * hSize));
                }
            }
            // lines
            for (int h = minHeight; h < maxHeight; h++) {
                if (h % 2 != 0) {
                    continue;
                }
                line.y1 = line.y2 = dim.height - BOTTOM_BORDER - (h - minHeight) * hSize;
                g2.setStroke(h % 12 == 0 ? stdStroke : thinStroke);
                g2.setColor(h % 12 == 0 ? (darkMode ? hiGrayDarkMode : hiGray) : (darkMode ? HI_GRAY_2_DARK_MODE : HI_GRAY_2));
                g2.draw(line);
            }

            // scale numbers
            if (paintHeights) {
                g2.setColor(darkMode ? hiGrayDarkMode : hiGray);
                int fs = (int) Math.min(big.length * hSize / 8, big.length - 1);
                g2.setFont(big[fs]);
                int fh = g2.getFontMetrics().getAscent();
                for (int h = minHeight; h < maxHeight; h++) {
                    if (h % 12 == 0) {
                        double y = dim.height - BOTTOM_BORDER - (h - minHeight) * hSize;
                        String s = "" + (h / 12 + 4);
                        int sw = g2.getFontMetrics().stringWidth(s);
                        g2.drawString(s, (int) line.x1 + 5, (int) (y - 6 * hSize + fh / 2));
                        g2.drawString(s, (int) line.x2 - 5 - sw, (int) (y - 6 * hSize + fh / 2));
                    }
                }
                double y = dim.height - BOTTOM_BORDER - (mh - minHeight) * hSize;
                if (y + 12 * hSize - (double) fh / 2 < dim.height - BOTTOM_BORDER) {
                    String s = "" + (mh / 12 + 4 - 1);
                    int sw = g2.getFontMetrics().stringWidth(s);
                    g2.drawString(s, (int) line.x1 + 5, (int) (y + 12 * hSize - fh / 2));
                    g2.drawString(s, (int) line.x2 - 5 - sw, (int) (y + 12 * hSize - fh / 2));
                }
            }
        }
    }

    /**
     * Gets the noteName attribute of the YassSheet object
     *
     * @param n Description of the Parameter
     * @return The noteName value
     */
    public String getNoteName(int n) {
        while (n < 0) {
            n += 12;
        }
        return actualNoteTable[n % 12];
    }

    /**
     * Gets the whiteNote attribute of the YassSheet object
     *
     * @param n Description of the Parameter
     * @return The whiteNote value
     */
    public boolean isWhiteNote(int n) {
        n = n % 12;
        return n == 0 || n == 2 || n == 4 || n == 5 || n == 7 || n == 9 || n == 11;
    }

    /**
     * Description of the Method
     *
     * @param g2 Description of the Parameter
     */
    public void paintHeightBox(Graphics2D g2) {
        int x = clip.x;
        int y = TOP_LINE - 10;
        int w = heightBoxWidth - 1;
        int hh = clip.height - TOP_LINE + 10 - BOTTOM_BORDER - 1;

        g2.setStroke(thinStroke);
        g2.setColor(darkMode ? HI_GRAY_2_DARK_MODE : HI_GRAY_2);
        g2.fillRect(x, y, w, hh);

        g2.setColor(Color.gray);
        g2.drawRect(x, y, w, hh);

        int blackw = 24;
        int whitew = 40;

        Line2D.Double line = new Line2D.Double(clip.x + heightBoxWidth,
                TOP_LINE - 10, clip.x + clip.width, clip.height - TOP_LINE + 10
                - BOTTOM_BORDER);
        g2.setFont(smallFont);
        FontMetrics metrics = g2.getFontMetrics();
        String str;
        if (pan) {
            for (int h = 1; h < NORM_HEIGHT - 2; h++) {
                line.y1 = line.y2 = dim.height - BOTTOM_BORDER - h * hSize + 4;
                str = getNoteName(hhPageMin + h - 2 + 60);

                boolean isWhite = isWhiteNote(hhPageMin + h - 2 + 60);
                if (isWhite) {
                    g2.setColor(Color.white);
                    g2.fillRect(x + 1, (int) line.y1 - (int) hSize / 2 - 2,
                            whitew, (int) hSize - 2);
                } else {
                    g2.setColor(Color.black);
                    g2.fillRect(x + 1, (int) line.y1 - (int) hSize / 3 - 2,
                            blackw, (int) (2 * hSize / 3) - 3);

                    // paint black over full width
                    // g2.fillRect(clip.x+heightBoxWidth +1, (int) line.y1 -
                    // (int) hSize / 3 - 2,
                    // clip.width-heightBoxWidth-10, (int) (2 * hSize / 3) - 3);
                }
                if (hhPageMin + h == hiliteHeight) {
                    g2.setColor(Color.black);
                } else {
                    g2.setColor(Color.gray);
                }

                if (isWhite) {
                    metrics.stringWidth(str);
                    g2.drawString(str, (float) (clip.x + 3), (float) (line.y1));
                }

                str = "" + (hhPageMin + h - 2);
                int strw = metrics.stringWidth(str);
                g2.drawString(str,
                        (float) (clip.x + heightBoxWidth - strw - 3),
                        (float) (line.y1));
            }
            if (hiliteHeight < 200) {
                g2.setColor(blueDrag);
                g2.fillRect(clip.x + heightBoxWidth, (int) (dim.height
                        - BOTTOM_BORDER - (hiliteHeight - hhPageMin + 1)
                        * hSize), clip.width, (int) (2 * hSize));
                g2.setColor(BLUE);
                g2.fillRect(clip.x,
                        (int) (dim.height - BOTTOM_BORDER - (hiliteHeight
                                - hhPageMin + 1)
                                * hSize), heightBoxWidth, (int) (2 * hSize));
            }
        } else {
            for (int h = minHeight + 1; h < maxHeight - 2; h++) {
                line.y1 = line.y2 = dim.height - BOTTOM_BORDER
                        - (h - minHeight) * hSize + 4;
                str = getNoteName(h + 60);

                boolean isWhite = isWhiteNote(h + 60);
                if (isWhite) {
                    g2.setColor(Color.white);
                    g2.fillRect(x + 1, (int) line.y1 - 9, whitew, 10);
                } else {
                    g2.setColor(Color.black);
                    g2.fillRect(x + 1, (int) line.y1 - 7, blackw, 6);
                }

                if (h == hiliteHeight) {
                    g2.setColor(Color.black);
                } else {
                    g2.setColor(Color.gray);
                }

                if (isWhite) {
                    metrics.stringWidth(str);
                    g2.drawString(str, (float) (clip.x + 3), (float) (line.y1));
                }
                str = "" + h;
                int strw = metrics.stringWidth(str);
                g2.drawString(str, (float) (x + w - strw - 5),
                        (float) (line.y1));
            }
            if (hiliteHeight < 200) {
                g2.setColor(hiGray);
                g2.fillRect(clip.x + heightBoxWidth, (int) (dim.height
                        - BOTTOM_BORDER - (hiliteHeight - minHeight + 1)
                        * hSize), clip.width, (int) (2 * hSize));
                g2.setColor(BLUE);
                g2.fillRect(clip.x,
                        (int) (dim.height - BOTTOM_BORDER - (hiliteHeight
                                - minHeight + 1)
                                * hSize), heightBoxWidth, (int) (2 * hSize));
            }
        }
    }

    public void paintInOut(Graphics2D g2) {
        if (sketchStarted())
            return;

        if (select.y > 0) {
            g2.setColor(inoutColor);
            g2.fill(select);
        }

        if (inSnapshot >= 0) {
            if (outSnapshot < 0) {
                outSnapshot = inSnapshot;
            }
            g2.setColor(inoutSnapshotBarColor);
            int inf = toTimeline(inSnapshot);
            int outf = toTimeline(outSnapshot);
            int x = Math.min(inf, outf);
            int xw = Math.abs(outf - inf);
            if (xw > 0) {
                g2.fillRect(x, 0, xw, 10);
            }
        }

        if (inSelect >= 0) {
            if (outSelect < 0) {
                outSelect = inSelect;
            }
            g2.setColor(hiliteCue == SNAPSHOT ? inoutSnapshotBarColor : inoutBarColor);
            g2.setFont(smallFont);
            int inf = toTimeline(inSelect);
            int outf = toTimeline(outSelect);
            int x = Math.min(inf, outf);
            int xw = Math.abs(outf - inf);
            if (xw > 0) {
                g2.fillRect(x, 0, xw, 10);
                long d = paintHeights ? fromTimeline(xw + heightBoxWidth) : fromTimeline(xw);
                String s = YassUtils.commaTime(d) + "s";
                int sw = g2.getFontMetrics().stringWidth(s);

                g2.setColor(darkMode ? hiGrayDarkMode : hiGray);
                g2.fillRect(x + xw / 2 - sw / 2 - 5, 11, sw + 10, 9);

                g2.setColor(darkMode ? dkGrayDarkMode : dkGray);
                g2.drawString(s, x + xw / 2 - sw / 2, 18);
            }
        }

        if (inPoint < 0)
            return;
        if (outPoint < 0)
            outPoint = inPoint;

        g2.setColor(inoutColor);
        g2.fillRect(Math.min(inPoint, outPoint), TOP_LINE - 10,
                Math.abs(outPoint - inPoint), clip.height - TOP_LINE + 10
                        - BOTTOM_BORDER);
    }

    public void paintPlayerPosition(Graphics2D g2, boolean active) {
        int left = paintHeights ? heightBoxWidth : 0;
        if (playerPos < left) {
            return;
        }

        int y = TOP_LINE - 10;
        int h = dim.height - TOP_LINE + 10 - BOTTOM_BORDER;
        if (!active) {
            if (hiliteCue == MOVE_REMAINDER) {
                g2.setColor(BLUE);
            } else {
                g2.setColor(playerColor);
            }
            g2.fillRect(playerPos - clip.x - 1, y, 3, h);
            if (!live) {
                g2.fillRect(playerPos - clip.x - 1, TOP_BORDER, 3, 8);
            }
        } else {
            g2.setColor(playerColor3);
            g2.fillRect(playerPos - clip.x - 1 - 2, y, 1, h);
            g2.setColor(playerColor2);
            g2.fillRect(playerPos - clip.x - 1 - 1, y, 1, h);
            g2.setColor(playerColor);
            g2.fillRect(playerPos - clip.x - 1, y, 3, h);
        }
    }

    public void paintPlayerPosition(Graphics2D g2) {
        paintPlayerPosition(g2, false);
    }

    public void paintSketch(Graphics2D g2) {
        if (!sketchStarted())
            return;
        if (sketchPos < 1)
            return;

        g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g2.setColor(BLUE);
        Point p1 = sketch[0];
        for (int k = 1; k < sketchPos; k++) {
            Point p2 = sketch[k];
            g2.drawLine(-clip.x + p1.x, p1.y, -clip.x + p2.x, p2.y);
            p1 = p2;
        }
    }

    public void paintRectangles(Graphics2D g2) {
        Enumeration<YassTable> ts = tables.elements();
        for (Enumeration<Vector<YassRectangle>> e = rects.elements(); e
                .hasMoreElements(); ) {
            Vector<YassRectangle> r = e.nextElement();
            YassTable t = ts.nextElement();
            if (r == rect) {
                continue;
            }
            paintRectangles(g2, r, t.getTableColor(), false);
        }
        if (rect != null) {
            paintRectangles(g2, rect, table.getTableColor(), true);
        }
    }

    public void paintRectangles(Graphics2D g2, Vector<?> rect, Color col, boolean onoff) {
        YassRectangle r = null;
        int i = 0;
        new Line2D.Double(0, 0, 0, 0);
        RoundRectangle2D.Double mouseRect = new RoundRectangle2D.Double(0, 0,0, 0, 0, 0);
        RoundRectangle2D.Double cutRect = new RoundRectangle2D.Double(0, 0, 0,0, 10, 10);
        YassRectangle prev;
        YassRectangle next = null;

        int[] rows = table != null ? table.getSelectedRows() : null;
        int selnum = rows != null ? rows.length : 1;
        int selfirst = table != null ? table.getSelectionModel()
                .getMinSelectionIndex() : -1;
        int sellast = table != null ? table.getSelectionModel()
                .getMaxSelectionIndex() : -1;

        int pn = 1;

        int cx = clip.x;
        int cw = clip.width;
        int ch = clip.height;
        Line2D.Double smallRect = new Line2D.Double(0, 0, 0, 0);
        Rectangle clip2 = new Rectangle(cx, 0, cw, ch);

        Paint oldPaint = g2.getPaint();
        Stroke oldStroke = g2.getStroke();

        for (Enumeration<?> e = rect.elements(); e.hasMoreElements() || next != null; i++) {
            prev = r;
            if (next != null) {
                r = next;
                next = e.hasMoreElements() ? (YassRectangle) e.nextElement() : null;
            } else {
                r = (YassRectangle) e.nextElement();
            }
            if (next == null) {
                next = e.hasMoreElements() ? (YassRectangle) e.nextElement() : null;
            }
            if (r.isPageBreak()) {
                pn = r.getPageNumber();
            }

            Color borderCol = col;
            if (r.x < clip.x + clip.width && r.x + r.width > clip.x) {
                if (!r.isPageBreak())
                    hhPageMin = r.getPageMin();
                boolean isSelected = table != null && table.isRowSelected(i);
                if (onoff) {
                    if (!r.isPageBreak() && table.getMultiSize() > 1) {
                        Color bg = pn % 2 == 1
                                ? (darkMode ? hiGrayDarkMode : hiGray)
                                : (darkMode ? HI_GRAY_2_DARK_MODE : HI_GRAY_2);
                        int w = (next == null || next.isPageBreak() || next.hasType(YassRectangle.END))
                                ? (int) r.width
                                : (int) (next.x - r.x + 1);
                        g2.setColor(bg);
                        g2.fillRect((int) r.x, clip.height - BOTTOM_BORDER, w,16);
                    }
                    Color shadeCol = table.isRowSelected(i) ? colorSet[YassSheet.COLOR_ACTIVE]
                            : colorSet[YassSheet.COLOR_SHADE];
                    Color hiliteFill = colorSet[YassSheet.COLOR_NORMAL];
                    if (r.isType(YassRectangle.GOLDEN)) {
                        hiliteFill = colorSet[YassSheet.COLOR_GOLDEN];
                    } else if (r.isType(YassRectangle.FREESTYLE)) {
                        hiliteFill = colorSet[YassSheet.COLOR_FREESTYLE];
                    } else if (r.isType(YassRectangle.RAP)) {
                        hiliteFill = colorSet[YassSheet.COLOR_RAP];
                    } else if (r.isType(YassRectangle.RAPGOLDEN)) {
                        hiliteFill = colorSet[YassSheet.COLOR_RAPGOLDEN];
                    } else if (r.isType(YassRectangle.WRONG)) {
                        hiliteFill = colorSet[YassSheet.COLOR_ERROR];
                    }
                    if (noshade) {
                        g2.setPaint(table.isRowSelected(i) ? colorSet[YassSheet.COLOR_ACTIVE] : hiliteFill);
                    } else {
                        g2.setPaint(new GradientPaint(
                                (float) r.x, (float) (r.y + 2), hiliteFill,
                                (float) r.x, (float) (r.y + r.height), shadeCol));
                    }
                }

                if (r.isPageBreak()) {
                    Line2D.Double dashLine = new Line2D.Double(0, 0, 0, 0);
                    float[] dash1 = {8f, 2f};
                    float dashWidth = .5f;
                    BasicStroke dashed = new BasicStroke(dashWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,10.0f, dash1, 0.0f);
                    g2.setStroke(dashed);
                    if (r.isType(YassRectangle.WRONG)) {
                        g2.setColor(colorSet[YassSheet.COLOR_WARNING]);
                    }
                    dashLine.y1 = TOP_LINE - 10;
                    dashLine.y2 = clip.height - BOTTOM_BORDER;
                    if (wSize > 5) {
                        dashLine.x1 = dashLine.x2 = r.x - 3;
                        g2.draw(dashLine);
                    }
                    dashLine.x1 = dashLine.x2 = r.x - 1;
                    g2.draw(dashLine);
                    if (r.width >= 2 * wSize) {
                        dashLine.x1 = dashLine.x2 = r.x + r.width;
                        g2.draw(dashLine);
                    }
                } else {
                    if (r.isType(YassRectangle.GAP)) {
                        if (!live) {
                            g2.setColor(col);
                            g2.drawString("【", (float)r.x, (float)r.y + 8);
                        }
                        continue;
                    }
                    if (r.isType(YassRectangle.START)) {
                        if (!live) {
                            g2.setColor(darkMode ? dkGrayDarkMode : dkGray);
                            Rectangle2D.Double rec = new Rectangle2D.Double(
                                    r.x, r.y, 2, r.height);
                            g2.fill(rec);
                            rec.width = 4;
                            rec.height = 2;
                            g2.fill(rec);
                            rec.y = r.y + r.height - 2;
                            g2.fill(rec);
                        }
                        continue;
                    }
                    if (r.isType(YassRectangle.END)) {
                        if (!live) {
                            g2.setColor(darkMode ? dkGrayDarkMode : dkGray);
                            Rectangle2D.Double rec = new Rectangle2D.Double(
                                    r.x, r.y, 2, r.height);
                            g2.fill(rec);
                            rec.x = r.x - 4;
                            rec.width = 4;
                            rec.height = 2;
                            g2.fill(rec);
                            rec.y = r.y + r.height - 2;
                            g2.fill(rec);
                        }
                        continue;
                    }

                    if (onoff && r.width > 2.4) {
                        g2.fill(r);

                        if (wSize > 8) {
                            YassRow row = table.getRowAt(i);
                            if (table.isRowSelected(i))
                            {
                                if (!isPlaying && !live) {
                                    g2.setFont(smallFont);
                                    g2.setColor(darkMode ? blackDarkMode : black);
                                    FontMetrics fm = g2.getFontMetrics();

                                    int beat = row.getBeatInt();
                                    int x = beatToTimeline(beat);
                                    String s = beat+"";
                                    int sw = fm.stringWidth(s);
                                    g2.setColor(darkMode ? HI_GRAY_2_DARK_MODE : HI_GRAY_2);
                                    g2.fillRect(x - sw / 2, 0, sw, 10);
                                    g2.setColor(darkMode ? blackDarkMode : black);
                                    g2.drawString(s, x - sw / 2, 8f);

                                    long ms = (long)table.beatToMs(beat);
                                    s = YassUtils.commaTime(ms) + "s";
                                    sw = fm.stringWidth(s);
                                    g2.setColor(darkMode ? HI_GRAY_2_DARK_MODE : HI_GRAY_2);
                                    g2.fillRect(x - sw / 2, 10, sw, 10);
                                    g2.setColor(darkMode ? blackDarkMode : black);
                                    g2.drawString(s, x - sw / 2, 18);
                                }
                            }
                            if (showNoteBeat) {
                                String beatstr = row.getBeat();
                                int yoff = 4;

                                Font oldFont = g2.getFont();
                                g2.setColor(darkMode ? dkGrayDarkMode : dkGray);
                                g2.setFont(big[15]);
                                FontMetrics metrics = g2.getFontMetrics();
                                int strw = metrics.stringWidth(beatstr);
                                int midx = (int) (r.x + r.width / 2);
                                int hx = (int) (r.x + 3);
                                int hy = (int) (r.y - yoff);
                                if (hx + strw > midx - 4)
                                    hx = midx - strw - 2;
                                g2.drawString(beatstr, hx, hy);
                                g2.setFont(oldFont);
                            }
                            if (showNoteLength) {
                                String lenstr = row.getLength();
                                int yoff = 4;

                                int midx = (int) (r.x + r.width / 2);
                                Font oldFont = g2.getFont();
                                g2.setColor(darkMode ? dkGrayDarkMode : dkGray);
                                g2.setFont(big[16]);
                                FontMetrics metrics = g2.getFontMetrics();
                                int strw = metrics.stringWidth(lenstr);
                                int lenx = (int) (r.x + r.width - 2 - strw);
                                int leny = (int) (r.y + r.height - yoff + 16);
                                if (lenx < midx + 2)
                                    lenx = midx;
                                if (r.width < 24) {
                                    lenx = (int) (midx - strw / 2 + 0.5);
                                }
                                g2.drawString(lenstr, lenx, leny);
                                g2.setFont(oldFont);

                            }
                            if (showNoteHeight || showNoteHeightNum) {
                                int pitch = row.getHeightInt();
                                String hstr = "";
                                if (showNoteHeightNum)
                                    hstr = pitch + "";
                                else if (showNoteHeight) {
                                    hstr = getNoteName(pitch + 60);
                                    if (showNoteScale || paintHeights) {
                                        int scale = pitch >= 0 ? (pitch / 12 + 4) : (3 + (pitch + 1) / 12); // negative pitch requires special handling
                                        hstr += "" + scale;
                                    }
                                }
                                int yoff = 4;
                                int midx = (int) (r.x + r.width / 2);
                                Font oldFont = g2.getFont();
                                g2.setColor(darkMode ? dkGrayDarkMode : dkGray);
                                g2.setFont(big[16]);
                                FontMetrics metrics = g2.getFontMetrics();
                                int strw = metrics.stringWidth(hstr);
                                int hx = (int) (r.x + 3);
                                int hy = (int) (r.y + r.height - yoff + 16);
                                if (hx + strw > midx - 2)
                                    hx = midx - strw;
                                if (r.width < 24) {
                                    hx = (int) (midx - strw / 2 + 0.5);
                                    if (showNoteLength) hy += 10;
                                }
                                g2.drawString(hstr, hx, hy);
                                g2.setFont(oldFont);
                            }
                        }
                    }

                    if (!live) {
                        boolean paintLeft = false;
                        boolean paintRight = false;
                        if (mouseover) {
                            if (isSelected && hiliteAction != ACTION_NONE) {
                                if (selnum == 1) {
                                    if (hiliteAction == ACTION_CONTROL_ALT) {
                                        paintLeft = paintRight = true;
                                    }
                                    if (hiliteAction == ACTION_CONTROL) {
                                        paintLeft = true;
                                    }
                                    if (hiliteAction == ACTION_ALT) {
                                        paintRight = true;
                                    }
                                } else if (selnum >= 2) {
                                    if (hiliteAction == ACTION_CONTROL_ALT) {
                                        if (selfirst == i) {
                                            paintLeft = true;
                                        }
                                        if (sellast == i) {
                                            paintRight = true;
                                        }
                                    }
                                    if (hiliteAction == ACTION_CONTROL) {
                                        paintLeft = true;
                                    }
                                    if (hiliteAction == ACTION_ALT) {
                                        paintRight = true;
                                    }
                                }
                            }
                        } else {
                            if ((selnum == 1 && isSelected)
                                    || (hilite == i && !isSelected)) {
                                if (hilite < 0 || hilite == i) {
                                    if (hiliteAction == ACTION_CONTROL_ALT) {
                                        paintLeft = paintRight = true;
                                    }
                                    if (hiliteAction == ACTION_CONTROL) {
                                        paintLeft = true;
                                    }
                                    if (hiliteAction == ACTION_ALT) {
                                        paintRight = true;
                                    }
                                }
                            } else if (selnum >= 2 && isSelected) {
                                if (hilite < 0 || table.isRowSelected(hilite)) {
                                    if (hiliteAction == ACTION_CONTROL_ALT) {
                                        if (selfirst == i) {
                                            paintLeft = true;
                                        }
                                        if (sellast == i) {
                                            paintRight = true;
                                        }
                                    }
                                }
                                if (hiliteAction == ACTION_CONTROL) {
                                    paintLeft = true;
                                }
                                if (hiliteAction == ACTION_ALT) {
                                    paintRight = true;
                                }
                            }
                        }
                        int dragw = r.width > Math.max(wSize, 32) * 3 ? (int) Math
                                .max(wSize, 32) : (r.width > 72 ? 24
                                : (r.width > 48 ? 16 : 5));
                        if (paintLeft) {
                            g2.setColor(blueDrag);
                            clip.width = (int) r.x - clip.x + dragw;
                            g2.setClip(clip);
                            g2.fill(r);
                        }
                        if (paintRight) {
                            g2.setColor(blueDrag);
                            clip.x = (int) (r.x + r.width - dragw + 1);
                            g2.setClip(clip);
                            g2.fill(r);
                        }
                        if (paintLeft || paintRight) {
                            clip.x = cx;
                            clip.width = cw;
                            g2.setClip(clip);
                        }
                    }

                    borderCol = hilite == i ? colorSet[YassSheet.COLOR_ACTIVE] : borderCol;

                    g2.setColor(borderCol);

                    if (wSize < 10) {
                        g2.setStroke(stdStroke);
                    } else {
                        g2.setStroke(medStroke);
                    }

                    if (hilite == i && !r.isType(YassRectangle.GAP)
                            && !r.isType(YassRectangle.START)
                            && !r.isType(YassRectangle.END)) {
                        if (hiliteCue == CUT || hiliteCue == JOIN_LEFT
                                || hiliteCue == JOIN_RIGHT) {
                            if (r.width > 5) {
                                mouseRect.x = r.x;
                                mouseRect.y = r.y - hSize;
                                mouseRect.width = r.width;
                                mouseRect.height = hSize - 1;

                                g2.setColor(blueDrag);
                                g2.fill(mouseRect);

                                g2.setColor(borderCol);
                                clip2.x = (int) (r.x);
                                clip2.width = (int) (wSize / 2);
                                g2.setClip(clip2);
                                g2.fill(mouseRect);

                                clip2.x = (int) (r.x + r.width - wSize / 2);
                                clip2.width = cw;
                                g2.setClip(clip2);
                                g2.fill(mouseRect);

                                g2.setClip(clip);

                                if (wSize / 2 > 5) {
                                    g2.setColor(white);
                                    int hh = (int) (r.y - hSize / 2);
                                    g2.drawLine((int) (r.x + 3), hh, (int) (r.x
                                            + wSize / 2 - 4), hh);
                                    g2.drawLine((int) (r.x + 5), hh - 1,
                                            (int) (r.x + 5), hh + 1);
                                    g2.drawLine((int) (r.x + 6), hh - 2,
                                            (int) (r.x + 6), hh + 2);
                                    g2.drawLine((int) (r.x + r.width - wSize
                                                    / 2 + 3), hh,
                                            (int) (r.x + r.width - 4), hh);
                                    g2.drawLine((int) (r.x + r.width - 6),
                                            hh - 1, (int) (r.x + r.width - 6),
                                            hh + 1);
                                    g2.drawLine((int) (r.x + r.width - 7),
                                            hh - 2, (int) (r.x + r.width - 7),
                                            hh + 2);
                                }

                                // int cutx = (int) (r.x - 2 + wSize / 2);
                                // g2.drawLine(cutx, (int) (r.y - hSize), cutx,
                                // (int) (r.y - 3));
                                // cutx = (int) (r.x + r.width - 2 - wSize / 2);
                                // g2.drawLine(cutx, (int) (r.y - hSize), cutx,
                                // (int) (r.y - 3));
                                // for (cutx = (int) (r.x + wSize); cutx < (int)
                                // (r.x + r.width - wSize / 2); cutx += wSize) {
                                // g2.drawLine(cutx, (int) (r.y - hSize), cutx,
                                // (int) (r.y - 3));
                                // }

                                g2.setColor(borderCol);
                            }
                        }

                        if (hiliteCue == CUT) {
                            cutRect.x = r.x;
                            cutRect.y = r.y;
                            cutRect.width = wSize
                                    * Math.round(cutPercent * r.width / wSize);
                            cutRect.height = r.height;
                            g2.draw(cutRect);
                            cutRect.x = r.x + cutRect.width;
                            cutRect.y = r.y;
                            cutRect.width = r.width - cutRect.width;
                            cutRect.height = r.height;
                            g2.draw(cutRect);
                        } else if (hiliteCue == JOIN_LEFT && prev != null) {
                            cutRect.x = prev.x;
                            cutRect.y = r.y;
                            cutRect.width = r.x - prev.x + r.width;
                            cutRect.height = r.height;
                            g2.draw(cutRect);
                        } else if (hiliteCue == JOIN_RIGHT && next != null) {
                            cutRect.x = r.x;
                            cutRect.y = r.y;
                            cutRect.width = next.x - r.x + next.width;
                            cutRect.height = r.height;
                            g2.draw(cutRect);
                        }
                    }

                    if (hilite != i
                            || (hiliteCue != CUT && hiliteCue != JOIN_RIGHT && hiliteCue != JOIN_LEFT)) {
                        if (r.width > 2.4) {
                            g2.draw(r);
                        } else if (r.width > 1.4) {
                            Color c = table.isRowSelected(i) ? colorSet[YassSheet.COLOR_ACTIVE]
                                    : (darkMode ? dkGrayDarkMode : dkGray);
                            g2.setColor(c);

                            smallRect.x1 = r.x;
                            smallRect.y1 = r.y;
                            smallRect.x2 = r.x;
                            smallRect.y2 = r.y + r.height;
                            g2.draw(smallRect);
                            smallRect.x1++;
                            smallRect.x2++;
                            g2.draw(smallRect);
                        } else {
                            Color c = table.isRowSelected(i) ? colorSet[YassSheet.COLOR_ACTIVE]
                                    : (darkMode ? dkGrayDarkMode : dkGray);
                            g2.setColor(c);

                            smallRect.x1 = r.x;
                            smallRect.y1 = r.y;
                            smallRect.x2 = r.x;
                            smallRect.y2 = r.y + r.height;
                            g2.draw(smallRect);
                        }
                    }
                }
            }
        }
        g2.setPaint(oldPaint);
        g2.setStroke(oldStroke);
    }

    /**
     * Description of the Method
     *
     * @param g2 Description of the Parameter
     */
    public void paintPlainRectangles(Graphics2D g2) {
        YassRectangle r;
        new YassRectangle();
        Color borderCol = darkMode ? dkGrayDarkMode : dkGray;

        int cx = clip.x;
        int cw = clip.width;
        int ch = clip.height;

        Paint oldPaint = g2.getPaint();

        Rectangle clip2 = new Rectangle(cx, 0, cw, ch);

        for (Enumeration<?> e = rect.elements(); e.hasMoreElements(); ) {
            r = (YassRectangle) e.nextElement();
            if (r.isPageBreak() || r.isType(YassRectangle.START)
                    || r.isType(YassRectangle.GAP)
                    || r.isType(YassRectangle.END)) {
                continue;
            }

            if (!(r.x < clip.x + clip.width && r.x + r.width > clip.x)) {
                continue;
            }

            // r.x = r.x - clip.x;

            Color shadeCol = colorSet[YassSheet.COLOR_SHADE];
            Color hiliteFill = colorSet[YassSheet.COLOR_NORMAL];
            if (r.isType(YassRectangle.GOLDEN)) {
                hiliteFill = colorSet[YassSheet.COLOR_GOLDEN];
            } else if (r.isType(YassRectangle.FREESTYLE)) {
                hiliteFill = colorSet[YassSheet.COLOR_FREESTYLE];
            } else if (r.isType(YassRectangle.RAP)) {
                hiliteFill = colorSet[YassSheet.COLOR_RAP];
            } else if (r.isType(YassRectangle.RAPGOLDEN)) {
                hiliteFill = colorSet[YassSheet.COLOR_RAPGOLDEN];
            } else if (r.isType(YassRectangle.WRONG)) {
                hiliteFill = colorSet[YassSheet.COLOR_ERROR];
            }

            g2.setPaint(new GradientPaint((float) r.x, (float) r.y + 2,
                    hiliteFill, (float) (r.x), (float) (r.y + r.height),
                    shadeCol));

            clip2.width = cw;
            g2.setClip(clip2);
            g2.fill(r);

            g2.setPaint(new GradientPaint((float) r.x, (float) r.y - 4,
                    playBlueHi, (float) (r.x), (float) (r.y + r.height),
                    playBlue));

            clip2.width = playerPos - cx;
            g2.setClip(clip2);
            g2.fill(r);

            clip2.width = cw;
            g2.setClip(clip2);

            g2.setColor(borderCol);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(r);

            // r.x = r.x + clip.x;
        }
        g2.setPaint(oldPaint);
    }

    /**
     * Description of the Method
     *
     * @param g2 Description of the Parameter
     */
    public void paintSnapshot(Graphics2D g2) {
        if (snapshot == null) {
            return;
        }

        Paint oldPaint = g2.getPaint();

        int next = nextElement(playerPos);
        if (next < 0) {
            return;
        }
        if (!table.isRowSelected(next)) {
            return;
        }
        YassRectangle rec = rect.elementAt(next);
        int pageMin = rec.getPageMin();

        double timelineGap = table.getGap() * 4 / (60 * 1000 / table.getBPM());

        YassRectangle r;
        YassRow row;
        int startx = -1;
        for (Enumeration<Cloneable> e = snapshotRect.elements(), te = snapshot
                .elements(); e.hasMoreElements() && te.hasMoreElements(); ) {
            r = (YassRectangle) e.nextElement();
            row = (YassRow) te.nextElement();

            // copied from update
            int beat = row.getBeatInt();
            int length = row.getLengthInt();
            int height = row.getHeightInt();
            r.x = (timelineGap + beat) * wSize + 1;
            if (paintHeights) {
                r.x += heightBoxWidth;
            }
            if (pan) {
                r.y = dim.height - BOTTOM_BORDER - (height - pageMin + 2)
                        * hSize - hSize + 1;
            } else {
                r.y = dim.height - BOTTOM_BORDER - (height - minHeight) * hSize
                        - hSize + 1;
            }
            r.width = length * wSize - 2;
            r.height = 2 * hSize - 2;

            if (startx < 0) {
                startx = (int) r.x;
            }

            r.x = r.x - startx + playerPos;

            g2.setPaint(tex);
            g2.fill(r);

            if (r.height > 3 * hSize) {
                g2.fill(r);
            } else {
                g2.fill(r);
            }
        }
        g2.setPaint(oldPaint);
    }

    /**
     * Sets the versionTextPainted attribute of the YassSheet object
     *
     * @param onoff The new versionTextPainted value
     */
    public void setVersionTextPainted(boolean onoff) {
        versionTextPainted = onoff;
    }

    /**
     * Description of the Method
     *
     * @param g2 Description of the Parameter
     */
    public void paintVersionsText(Graphics2D g2) {
        if (!versionTextPainted) {
            return;
        }

        int off = 1;
        Enumeration<Vector<YassRectangle>> er = rects.elements();
        for (Enumeration<YassTable> e = tables.elements(); e.hasMoreElements()
                && er.hasMoreElements(); ) {
            YassTable t = e.nextElement();
            Vector<?> r = er.nextElement();
            if (t == table) {
                continue;
            }
            Color c = t.getTableColor();
            paintTableText(g2, t, r, c.darker(), c, off, 0, false);
            off++;
        }
    }

    /**
     * Description of the Method
     *
     * @param g2 Description of the Parameter
     */
    public void paintText(Graphics2D g2) {
        int i = tables.indexOf(table);
        if (i < 0) {
            return;
        }
        Color c = table.getTableColor();
        if (c==null) c = Color.black;
        paintTableText(g2, table, rect, c.darker(), c, 0, -clip.x, true);
    }

    /**
     * Description of the Method
     *
     * @param g2   Description of the Parameter
     * @param t    Description of the Parameter
     * @param re   Description of the Parameter
     * @param c1   Description of the Parameter
     * @param c2   Description of the Parameter
     * @param off  Description of the Parameter
     * @param offx Description of the Parameter
     * @param info Description of the Parameter
     */
    public void paintTableText(Graphics2D g2, YassTable t, Vector<?> re,
                               Color c1, Color c2, int off, int offx, boolean info) {
        String str;
        String ostr;
        int strw;
        int strh;
        YassRectangle r;
        YassRectangle next = null;
        FontMetrics metrics;

        int pn = 1;
        float sx;
        float sh;

        Rectangle2D.Double lastStringBounds = null;
        Rectangle2D.Double strBounds = new Rectangle2D.Double(0, 0, 0, 0);

        Enumeration<?> en = ((YassTableModel) t.getModel()).getData()
                .elements();
        for (Enumeration<?> ren = re.elements(); ren.hasMoreElements()
                && en.hasMoreElements(); ) {
            if (next != null) {
                r = next;
                next = (YassRectangle) ren.nextElement();
            } else {
                r = (YassRectangle) ren.nextElement();
            }
            if (next == null) {
                next = ren.hasMoreElements() ? (YassRectangle) ren.nextElement() : null;
            }

            str = ((YassRow) en.nextElement()).getText();

            if (r.isType(YassRectangle.GAP)) {
                continue;
            }
            if (r.isType(YassRectangle.START)) {
                continue;
            }
            if (r.isType(YassRectangle.END)) {
                continue;
            }
            if (r.isPageBreak()) {
                pn = r.getPageNumber();
            }

            boolean isVisible = r.x < clip.x + clip.width
                    && r.x + r.width > getLeftX();
            if (!isVisible) {
                continue;
            }

            if (info && r.isType(YassRectangle.FIRST)) {
                String s = Integer.toString(pn);
                g2.setColor(darkMode ? dkGrayDarkMode : dkGray);
                g2.setFont(big[18]);
                // g2.drawString(s, (float) (r.x + offx + 5), 18);
                g2.drawString(s, (float) (r.x + offx + 5), clip.height
                        - BOTTOM_BORDER + 14);
            }

            if (str.length() < 1) {
                continue;
            }
            ostr = str = str.replace(YassRow.SPACE, ' ');

            if (off == 0 && playerPos >= r.x && playerPos < r.x + r.width) {
                g2.setColor(c1);
                // if (isPlaying) {
                // int shade = (int) ((big.length - 1 - 6) - (big.length - 18 -
                // 6) * (playerPos - r.x) / r.width);
                // g2.setFont(big[shade]);
                // } else {
                g2.setFont(big[18]);
                // }
            } else {
                g2.setColor(c2);
                g2.setFont(big[18]);

                if (r.isType(YassRectangle.FREESTYLE)) {
                    g2.setFont(fonti);
                } else if (r.isType(YassRectangle.GOLDEN)) {
                    g2.setFont(fontb);
                } else if (r.isType(YassRectangle.RAP)) {
                    g2.setFont(fontt);
                } else if (r.isType(YassRectangle.RAPGOLDEN)) {
                    g2.setFont(fonttb);
                } else if (table != t) {
                    g2.setFont(fontv);
                } else {
                    g2.setFont(font);
                }
            }

            metrics = g2.getFontMetrics();
            strw = metrics.stringWidth(str);

            if (off == 0 && strw > r.width) {
                g2.setFont(big[15]);
                metrics = g2.getFontMetrics();
                strw = metrics.stringWidth(str);
            }
            if (off == 0 && strw > r.width) {
                g2.setFont(big[13]);
                metrics = g2.getFontMetrics();
                strw = metrics.stringWidth(str);
            }

            sx = (float) Math.round(r.x + r.width / 2f - strw / 2f + offx + 1);
            strh = metrics.getAscent();
            if (off == 0) {
                sh = (float) (r.y + r.height / 2 + strh / 2f);
            } else {
                sh = (float) (r.y + r.height + 2 + strh);
            }

            if (strw <= r.width) {
                if (off == 0) {
                    g2.setColor(darkMode ? whiteDarkMode : white);
                }
                g2.drawString(str, sx, sh);
            } else {
                g2.setFont(big[18]);
                metrics = g2.getFontMetrics();
                strw = metrics.stringWidth(ostr);
                strh = metrics.getAscent();
                if (strh > r.width + 4) {
                    g2.setFont(big[15]);
                    metrics = g2.getFontMetrics();
                    strh = metrics.getAscent();
                }
                if (strh > r.width + 4) {
                    g2.setFont(big[13]);
                    metrics = g2.getFontMetrics();
                    strh = metrics.getAscent();
                }
                sx = (float) (r.x + r.width / 2 + strh / 2f + offx - 1);
                sh = (float) (r.y - 5);
                strBounds.x = r.x + r.width / 2 - strh / 2f + offx - 1;
                strBounds.y = r.y - 5 - strw;
                strBounds.width = strh;
                strBounds.height = strw;
                if (strh <= r.width + 4 || lastStringBounds == null
                        || !lastStringBounds.intersects(strBounds)) {
                    if (lastStringBounds == null) {
                        lastStringBounds = new Rectangle2D.Double(0, 0, 0, 0);
                    }
                    lastStringBounds.setRect(strBounds);

                    g2.translate(sx, sh);
                    g2.rotate(-Math.PI / 2);

                    g2.setColor(darkMode ? dkGrayDarkMode : dkGray);
                    g2.drawString(ostr, 0, 0);

                    g2.rotate(Math.PI / 2);
                    g2.translate(-sx, -sh);
                }
            }
        }
    }

    /**
     * Gets the live attribute of the YassSheet object
     *
     * @return The live value
     */
    public boolean isLive() {
        return live;
    }

    /**
     * Sets the live attribute of the YassSheet object
     *
     * @param onoff The new live value
     */
    public void setLive(boolean onoff) {
        getComponent(0).setVisible(!onoff);
        live = onoff;
    }

    /**
     * Description of the Method
     *
     * @param g2     Description of the Parameter
     * @param waitms Description of the Parameter
     */
    public void paintWait(Graphics2D g2, int waitms) {
        int sec = waitms / 1000;

        int leftx = live ? 0 : LEFT_BORDER;

        if (waitms <= 4000) {
            int width = (int) (60 * waitms / 4000.0);
            g2.setColor(BLUE);
            g2.fillRect(leftx, clip.height - BOTTOM_BORDER + 16, width,
                    BOTTOM_BORDER - 16);
        } else {
            int width = 60;
            g2.setColor(BLUE);
            g2.fillRect(leftx, clip.height - BOTTOM_BORDER + 16, width,BOTTOM_BORDER - 16);
        }
        if (waitms > 3000) {
            String s = Integer.toString(sec);
            g2.setFont(big[24]);
            g2.setColor(white);
            FontMetrics metrics = g2.getFontMetrics();
            int strw = metrics.stringWidth(s);
            float sx = leftx + 30 - strw / 2;
            float sh = clip.height - 12;
            g2.drawString(s, sx, sh);
        }
    }

    public void paintPlayerText(Graphics2D g2) {
        String str;
        int strw;
        int strh;
        YassRectangle r = null;
        YassRow row = null;
        FontMetrics metrics = null;

        int strpos = 0;

        if (table == null) {
            return;
        }

        int i = table.getSelectionModel().getMinSelectionIndex();
        int j = table.getSelectionModel().getMaxSelectionIndex();

        if (i < 0) {
            i = nextElement();
            if (i < 0)
                i = rect.size() - 2;
        }
        if (i < 0)
            return;
        if (j < 0)
            j = i;
        int[] ij = table.enlargeToPages(i, j);
        i = ij[0];
        j = ij[1];
        if (showVideo() || showBackground()) {
            int leftx = 0;
            g2.setColor(playertextBG);
            if (live)
                g2.fillRect(leftx, clip.height - BOTTOM_BORDER + 16, clip.width, BOTTOM_BORDER - 16);
            else
                g2.fillRect(leftx + LEFT_BORDER, clip.height - BOTTOM_BORDER + 16, clip.width - LEFT_BORDER - RIGHT_BORDER, BOTTOM_BORDER - 16);
        }
        float sh = clip.height - 12;

        int k = 0;
        Enumeration<?> en = ((YassTableModel) table.getModel()).getData().elements();
        Enumeration<?> ren = rect.elements();
        for (; ren.hasMoreElements() && en.hasMoreElements(); k++) {
            r = (YassRectangle) ren.nextElement();
            row = (YassRow) en.nextElement();
            if (k == i)
                break;
        }
        if (k != i || row == null)
            return;

        boolean first = true;
        int strwidth = 0;
        while (ren.hasMoreElements() && en.hasMoreElements() && k <= j) {
            if (!first) {
                r = (YassRectangle) ren.nextElement();
                row = (YassRow) en.nextElement();
                k++;
            } else {
                first = false;
            }

            str = row.getText();
            if (r.isType(YassRectangle.GAP))
                continue;
            if (r.isType(YassRectangle.START))
                continue;
            if (r.isType(YassRectangle.END))
                continue;
            if (r.isPageBreak()) {
                if (strwidth == 0)
                    continue;
                str = " / ";
            }
            if (str.length() < 1)
                continue;
            str = str.replace(YassRow.SPACE, ' ');

            g2.setFont(big[24]);
            g2.setColor(colorSet[YassSheet.COLOR_SHADE]);
            metrics = g2.getFontMetrics();
            strw = metrics.stringWidth(str);
            strwidth += strw;
        }
        if (strwidth > clip.width) {
            str = toomuchtext;
            strwidth = metrics.stringWidth(str);

            g2.setFont(big[24]);
            g2.setColor(colorSet[YassSheet.COLOR_SHADE]);

            float sx = clip.width / 2 - strwidth / 2;
            g2.drawString(str, sx, sh);
            return;
        }

        k = 0;
        en = ((YassTableModel) table.getModel()).getData().elements();
        for (ren = rect.elements(); ren.hasMoreElements()
                && en.hasMoreElements() && k <= j; k++) {
            r = (YassRectangle) ren.nextElement();
            row = (YassRow) en.nextElement();

            if (k < i) {
                continue;
            }

            str = row.getText();
            if (r.isType(YassRectangle.GAP)) {
                continue;
            }
            if (r.isType(YassRectangle.START)) {
                continue;
            }
            if (r.isType(YassRectangle.END)) {
                continue;
            }
            if (r.isPageBreak()) {
                if (strwidth == 0) {
                    continue;
                }
                str = " / ";
            }
            if (str.length() < 1) {
                continue;
            }
            str = str.replace(YassRow.SPACE, ' ');

            g2.setFont(big[24]);
            g2.setColor(darkMode ? colorSet[YassSheet.COLOR_NORMAL] : colorSet[YassSheet.COLOR_SHADE]);
            metrics = g2.getFontMetrics();
            strw = metrics.stringWidth(str);
            strh = metrics.getHeight();

            int offx = 0;

            int offy = 0;
            if (isPlaying) {
                if (playerPos >= r.x && playerPos < r.x + r.width) {
                    int shade = (int) ((big.length - 1) - (big.length - 24)
                            * (playerPos - r.x) / r.width);
                    g2.setColor(darkMode ? colorSet[YassSheet.COLOR_NORMAL] : colorSet[YassSheet.COLOR_SHADE]);
                    g2.setFont(big[shade]);
                    metrics = g2.getFontMetrics();
                    int strw2 = metrics.stringWidth(str);
                    int strh2 = metrics.getHeight();
                    offx = -(strw2 - strw) / 2;
                    offy = (strh2 - strh) / 4;
                }
            } else {
                if (playerPos >= r.x - 2 && playerPos < r.x + r.width) {
                    g2.setColor(darkMode ? colorSet[YassSheet.COLOR_ACTIVE] : colorSet[YassSheet.COLOR_ACTIVE]);
                }
            }
            float sx = clip.width / 2 - strwidth / 2 + strpos;
            strpos += strw;
            g2.drawString(str, sx + offx, sh + offy);
        }
    }

    public Vector<Long> getTemporaryNotes() {
        return tmpNotes;
    }

    public void paintTemporaryNotes() {
        Graphics2D g2 = backVolImage.createGraphics();
        g2.setColor(dkRed);
        Enumeration<Long> e = tmpNotes.elements();
        int i;
        int o;
        int x1;
        int x2;
        double ms;
        double ms2;
        while (e.hasMoreElements()) {
            Long in = e.nextElement();
            i = (int) in.longValue();
            ms = i / 1000.0;
            x1 = toTimeline(ms);
            if (e.hasMoreElements()) {
                Long out = e.nextElement();
                o = (int) out.longValue();
                ms2 = o / 1000.0;
                x2 = toTimeline(ms2);
            } else {
                x2 = playerPos;
            }
            x1 = x1 - clip.x;
            x2 = x2 - clip.x;
            if (x1 < 0)
                x1 = 0;
            if (x2 >= clip.width)
                x2 = clip.width - 1;
            g2.fillRect(x1, getTopLine() - 10, x2 - x1, (int) hSize);
        }
        g2.dispose();
    }

    public void paintRecordedNotes() {
        if (session == null)
            return;
        Graphics2D g2 = backVolImage.createGraphics();
        YassTrack track = session.getTrack(0);
        Vector<YassPlayerNote> playerNotes = track.getPlayerNotes();
        int lastPlayerNote = playerNotes.size() - 1;
        if (lastPlayerNote < 0)
            return;
        g2.setStroke(medStroke);
        for (int playerNoteIndex = lastPlayerNote; playerNoteIndex >= 0; playerNoteIndex--) {
            YassPlayerNote playerNote = playerNotes.elementAt(playerNoteIndex);
            if (playerNote.isNoise())
                continue;
            long startMillis = playerNote.getStartMillis();
            long endMillis = playerNote.getEndMillis();
            int playerHeight = playerNote.getHeight();
            int currentNote = track.getCurrentNote();
            YassNote note = track.getNote(currentNote);
            while (note.getStartMillis() >= endMillis && currentNote > 0)
                note = track.getNote(--currentNote);
            int noteHeight = note.getHeight();
            if (playerHeight < noteHeight) {
                while (Math.abs(playerHeight - noteHeight) > 6)
                    playerHeight += 12;
            } else {
                while (Math.abs(playerHeight - noteHeight) > 6)
                    playerHeight -= 12;
            }

            int x1 = toTimeline(startMillis);
            int x2 = toTimeline(endMillis);
            x1 = x1 - clip.x;
            x2 = x2 - clip.x;
            if (x1 < 0)
                x1 = 0;
            if (x2 >= clip.width)
                x2 = clip.width - 1;
            int h = pan ? (playerHeight - hhPageMin + 3) : playerHeight - minHeight + 1;
            if (h <= 0)
                h += 12;
            g2.setColor(new Color(0, 120, 0, 100));
            g2.fillRoundRect(x1 + 1, (int) (dim.height - BOTTOM_BORDER - h * hSize) + 1, x2 - x1 - 3, (int) (2 * hSize - 2), 10, 10);
            g2.setColor(new Color(160, 200, 160));
            g2.drawRoundRect(x1 + 1, (int) (dim.height - BOTTOM_BORDER - h * hSize) + 1, x2 - x1 - 3, (int) (2 * hSize - 2), 10, 10);
        }
        g2.dispose();
    }

    public void setMessage(String s) {
    }

    public void setErrorMessage(String s) {
        message = s;
    }

    public void paintMessage(Graphics2D g2) {
        if (message == null || message.length() < 1)
            return;
        g2.setFont(big[19]);
        FontMetrics metrics = g2.getFontMetrics();
        metrics.stringWidth(message);
        metrics.getHeight();
        g2.setColor(Color.blue);
        g2.drawString(message, clip.x + 4, 2 + metrics.getAscent());
    }

    private void updateFromRow(YassTable t, int i, YassRow prev, YassRow r, YassRectangle rr) {
        double timelineGap = t.getGap() * 4 / (60 * 1000 / t.getBPM());
        if (r.isNote()) {
            int pageMin = r.getHeightInt();
            if (pan) {
                int j = i - 1;
                YassRow p = t.getRowAt(j);
                while (p.isNote()) {
                    pageMin = Math.min(pageMin, p.getHeightInt());
                    p = t.getRowAt(--j);
                }
                j = i + 1;
                p = t.getRowAt(j);
                while (p != null && p.isNote()) {
                    pageMin = Math.min(pageMin, p.getHeightInt());
                    p = t.getRowAt(++j);
                }
            }
            int beat = r.getBeatInt();
            int length = r.getLengthInt();
            int height = r.getHeightInt();
            rr.x = (timelineGap + beat) * wSize + 1;
            if (paintHeights)
                rr.x += heightBoxWidth;
            if (pan) {
                rr.y = dim.height - (height - pageMin + 2) * hSize - hSize
                        - BOTTOM_BORDER + 1;
            } else {
                rr.y = dim.height - (height - minHeight) * hSize - hSize
                        - BOTTOM_BORDER + 1;
            }
            rr.width = length * wSize - 2;
            if (rr.width < 1)
                rr.width = 1;
            rr.height = 2 * hSize - 2;
            rr.setPageMin(pan ? pageMin : minHeight);
            if (r.hasMessage())
                rr.setType(YassRectangle.WRONG);
            else if (r.isGolden())
                rr.setType(YassRectangle.GOLDEN);
            else if (r.isFreeStyle())
                rr.setType(YassRectangle.FREESTYLE);
            else if (r.isRap())
                rr.setType(YassRectangle.RAP);
            else if (r.isRapGolden())
                rr.setType(YassRectangle.RAPGOLDEN);
            else
                rr.resetType();
            if (prev != null && prev.isPageBreak())
                rr.addType(YassRectangle.FIRST);
        } else if (r.isPageBreak()) {
            int beat = r.getBeatInt();
            int beat2 = r.getSecondBeatInt();
            int length = beat2 - beat;
            rr.x = (timelineGap + beat) * wSize + 2;
            if (paintHeights)
                rr.x += heightBoxWidth;
            rr.width = (length == 0) ? wSize / 4.0 : length * wSize - 2;
            if (pan)
                rr.height = ((double) (2 * NORM_HEIGHT) / 2 - 1) * hSize;
            else
                rr.height = ((double) (2 * maxHeight) / 2 - 1 - minHeight) * hSize;
            rr.y = dim.height - BOTTOM_BORDER - rr.height;
            if (r.hasMessage())
                rr.setType(YassRectangle.WRONG);
            else
                rr.resetType();
            rr.setPageNumber(0);
        } else if (r.isComment() && r.getCommentTag().equals("GAP:")) {
            // choose correct index i
            rr.x = timelineGap * wSize - 10;
            if (paintHeights)
                rr.x += heightBoxWidth;
            rr.width = 20;
            rr.height = 9;
            rr.y = 0;
            rr.setType(YassRectangle.GAP);
        } else if (r.isComment()
                && (r.getCommentTag().equals("START:") || r.getCommentTag().equals("TITLE:"))) {
            double start = t.getStart() * 4 / (60 * 1000 / t.getBPM());
            // choose correct index i
            rr.x = start * wSize;
            if (paintHeights)
                rr.x += heightBoxWidth;
            rr.width = 10;
            rr.height = 18;
            rr.y = 21;
            rr.setType(YassRectangle.START);
        } else if (r.isEnd()) {
            double end = t.getEnd();
            if (end < 0 || end > duration)
                end = duration;
            end = end * 4 / (60 * 1000 / t.getBPM());
            // choose correct index i
            rr.x = end * wSize - 5;
            if (paintHeights)
                rr.x += heightBoxWidth;
            rr.width = 10;
            rr.height = 18;
            rr.y = 21;
            rr.setType(YassRectangle.END);
        } else if (r.isComment()) {
            rr.setType(YassRectangle.HEADER);
        }
    }

    public void enablePan(boolean onoff) {
        pan = onoff;
        updateHeight();
        revalidate();
    }

    public boolean isPanEnabled() {
        return pan;
    }

    /**
     * Calculates min/max bounds for all tables (heights and beats).
     * Table gaps are added to beats (rounded to next beat).
     * @return
     */
    public int[] getHeightRange() {
        int minH = 128;
        int maxH = -128;
        int minB = 100000;
        int maxB = 0;
        for (YassTable t: tables) {
            for (YassRow r: t.getModelData()) {
                if (r.isNote()) {
                    int height = r.getHeightInt();
                    minH = Math.min(minH, height);
                    maxH = Math.max(maxH, height);
                    minB = Math.min(minB, r.getBeatInt());
                    maxB = Math.max(maxB, r.getBeatInt() + r.getLengthInt());
                }
            }
        }
        if (minH == 128)
            minH = 0;
        maxH = maxH + 3;
        minH = minH - 1;
        if (maxH - minH < 19)
            maxH = minH + 19;
        return new int[]{minH, maxH, minB, maxB};
    }

    /**
     * Description of the Method
     */
    public void init() {
        firePropertyChange("play", null, "stop");
        int maxWait = 10;
        while (isRefreshing() && maxWait-- > 0) {
            try {
                Thread.sleep(100);
            } catch (Exception ignored) {}
        }
        int[] range = getHeightRange();
        minHeight = range[0];
        maxHeight = range[1];
        minBeat = range[2];
        maxBeat = range[3];
        fireRangeChanged(minHeight, maxHeight, minBeat, maxBeat);

        Enumeration<YassTable> et = tables.elements();
        for (Enumeration<Vector<YassRectangle>> e = rects.elements(); e.hasMoreElements() && et.hasMoreElements(); ) {
            Vector<YassRectangle> r = e.nextElement();
            YassTable t = et.nextElement();
            r.removeAllElements();
            int n = t.getRowCount();
            for (int i = 0; i < n; i++)
                r.addElement(new YassRectangle());
        }
        if (isValid()) {
            updateHeight();
            update();
            repaint();
        }
    }

    public void updateHeight() {
        if (dim == null || getParent() == null)
            return;
        dim.setSize(dim.width, getParent().getSize().height);
        if (pan)
            hSize = (dim.height - BOTTOM_BORDER - 30) / (double) (NORM_HEIGHT - 2);
        else
            hSize = (dim.height - BOTTOM_BORDER - 30) / (double) (maxHeight - minHeight - 2);
        if (hSize > 16)
            hSize = 16;
        if (pan)
            TOP_LINE = dim.height - BOTTOM_BORDER + 10 - (int) (hSize * (NORM_HEIGHT - 2));
        else
            TOP_LINE = dim.height - BOTTOM_BORDER + 10 - (int) (hSize * (maxHeight - minHeight - 2));
    }

    public void update() {
        updateHeight();
        if (table != null) {
            gap = table.getGap();
            bpm = table.getBPM();
            beatgap = gap * 4 / (60 * 1000 / bpm);
        }
        outgap = 0;

        // beat/height range
        int minH = 128;
        int maxH = -128;
        int minB = 100000;
        int maxB = 0;

        Enumeration<YassTable> et = tables.elements();
        for (Enumeration<Vector<YassRectangle>> e = rects.elements(); e.hasMoreElements() && et.hasMoreElements(); ) {
            Vector<YassRectangle> r = e.nextElement();
            YassTable t = et.nextElement();
            int i = 0;
            int pn = 1;
            Enumeration<?> ren = r.elements();
            Enumeration<?> ten = ((YassTableModel) t.getModel()).getData().elements();
            YassRow row = null;
            YassRow prev;
            YassRow next = null;
            while (ren.hasMoreElements()) {
                prev = row;
                if (next != null) {
                    row = next;
                    next = ten.hasMoreElements() ? (YassRow) ten.nextElement() : null;
                } else
                    row = (YassRow) ten.nextElement();
                if (next == null)
                    next = ten.hasMoreElements() ? (YassRow) ten.nextElement() : null;
                if (row.isNote())
                    outgap = Math.max(outgap, row.getBeatInt() + row.getLengthInt());
                else if (row.isPageBreak())
                    outgap = Math.max(outgap, row.getSecondBeatInt());
                YassRectangle rr = (YassRectangle) ren.nextElement();
                updateFromRow(t, i++, prev, row, rr);
                if (rr.isPageBreak()) {
                    rr.setPageNumber(++pn);
                    // should better add PAGE_BREAK type
                    rr.removeType(YassRectangle.DEFAULT);
                }
                // beat/height range
                if (row.isNote()) {
                    int height = row.getHeightInt();
                    minH = Math.min(minH, height);
                    maxH = Math.max(maxH, height);
                    minB = Math.min(minB, row.getBeatInt());
                    maxB = Math.max(maxB, row.getBeatInt() + row.getLengthInt());
                }
            }
        }

        // beat/height range
        if (minH == 128)
            minH = 0;
        maxH = maxH + 3;
        minH = minH - 1;
        if (maxH - minH < 19)
            maxH = minH + 19;
        boolean changed = false;
        if (minHeight != minH) { minHeight = minH; changed = true; }
        if (maxHeight != maxH) { maxHeight = maxH; changed = true; }
        if (minBeat   != minB) { minBeat   = minB; changed = true; }
        if (maxBeat   != maxB) { maxBeat   = maxB; changed = true; }
        if (changed)
            fireRangeChanged(minHeight, maxHeight, minBeat, maxBeat);
    }

    public void setHNoteEnabled(boolean b) {
        actualNoteTable = b ? hNoteTable : bNoteTable;
    }

    public void updateActiveTable() {
        if (table == null)
            return;
        gap = table.getGap();
        bpm = table.getBPM();
        beatgap = gap * 4 / (60 * 1000 / bpm);
        int i = 0;
        int pn = 1;
        Enumeration<?> ren = rect.elements();
        Enumeration<?> ten = ((YassTableModel) table.getModel()).getData().elements();
        YassRow row = null;
        YassRow prev;
        YassRow next = null;
        while (ren.hasMoreElements() && ten.hasMoreElements()) {
            prev = row;
            if (next != null) {
                row = next;
                next = (YassRow) ten.nextElement();
            } else
                row = (YassRow) ten.nextElement();
            if (next == null)
                next = ten.hasMoreElements() ? (YassRow) ten.nextElement() : null;
            if (row.isNote())
                outgap = Math.max(outgap, row.getBeatInt() + row.getLengthInt());
            else if (row.isPageBreak())
                outgap = Math.max(outgap, row.getSecondBeatInt());
            YassRectangle rr = (YassRectangle) ren.nextElement();
            updateFromRow(table, i++, prev, row, rr);
            if (rr.isPageBreak()) {
                rr.setPageNumber(++pn);
            }
        }
    }

    public int getPlayerPosition() {
        return playerPos;
    }

    public void setPlayerPosition(int x) {
        if (x>=0) playerPos = x;
		firePosChanged();
    }

    public long getInSnapshot() {
        return inSnapshot;
    }

    public long getOutSnapshot() {
        return outSnapshot;
    }

    public void setPaintHeights(boolean onoff) {
        paintHeights = onoff;
    }

    public int toTimeline(double ms) {
        int x = (int) (4 * bpm * ms / (60 * 1000) * wSize + .5);
        if (paintHeights)
            x += heightBoxWidth;
        return x;
    }

    public long fromTimeline(double x) {
        if (paintHeights)
            x -= heightBoxWidth;
        return (long) (x * 60 * 1000L / (4.0 * bpm * wSize) + .5);
    }
    public long fromTimeline(int track, double x) {
        if (paintHeights)
            x -= heightBoxWidth;
        return (long) (x * 60 * 1000L / (4.0 * getTable(track).getBPM() * wSize) + .5);
    }

    public double fromTimelineExact(double x) {
        if (paintHeights)
            x -= heightBoxWidth;
        return x * 60 * 1000L / (4.0 * bpm * wSize);
    }

    public int beatToTimeline(int beat) {
        int x = (int) ((beatgap + beat) * wSize + .5);
        if (paintHeights)
            x += heightBoxWidth;
        return x;
    }

    public int timelineToBeat(int x) {
        if (paintHeights)
            x -= heightBoxWidth;
        return (int) (x/wSize - beatgap);
    }

    public double getMinGapInBeats()
    {
        int n = tables.size();
        double b = 10000;
        for (int i=0; i<n; i++)
            b = Math.min(b, getTable(i).getGapInBeats());
        return b;
    }

    /**
     * Finds first element that starts or ends after current position (or directly at)
     * @return index, -1 if not found
     */
    public int nextElement() {
        return nextElement(playerPos);
    }

    /**
     * Finds first element that starts or ends after given position (or directly at)
     * @param pos
     * @return index, -1 if not found
     */
    public int nextElement(int pos) {
        YassRectangle r;
        int i = 0;
        for (Enumeration<?> e = rect.elements(); e.hasMoreElements(); i++) {
            r = (YassRectangle) e.nextElement();
            if (r.x - 1 >= pos || r.x + r.width >= pos) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds first element that starts after given position (or nearly at)
     * @param pos
     * @return index, -1 if not found
     */
    public int nextElementStarting(int pos) {
        YassRectangle r;
        int i = 0;
        for (Enumeration<?> e = rect.elements(); e.hasMoreElements(); i++) {
            r = (YassRectangle) e.nextElement();
            if (r.x >= pos - 2) {
                return i;
            }
        }
        return -1;
    }

    /* not used
    public int nextElement(int track, int pos) {
        YassRectangle r;
        int i = 0;
        for (Enumeration<?> e = rects.elementAt(track).elements(); e.hasMoreElements(); i++) {
            r = (YassRectangle) e.nextElement();
            if (r.x - 1 >= pos || r.x + r.width >= pos) {
                return i;
            }
        }
        return -1;
    }*/

    public int firstVisibleNote() {
        int x = clip.x + LEFT_BORDER;
        if (paintHeights) {
            x += heightBoxWidth;
        }
        return nextNote(x);
    }
    public int nextNote(int pos) {
        YassRectangle r;
        int i = 0;
        for (Enumeration<?> e = rect.elements(); e.hasMoreElements(); i++) {
            r = (YassRectangle) e.nextElement();
            if (! r.isPageBreak() && !r.isType(YassRectangle.GAP) && !r.isType(YassRectangle.START)&& !r.isType(YassRectangle.END)) {
                if (r.x - 1 >= pos || r.x + r.width >= pos)
                    return i;
            }
        }
        return -1;
    }
    public int firstVisibleNote(int track) {
        int x = clip.x + LEFT_BORDER;
        if (paintHeights) {
            x += heightBoxWidth;
        }
        return nextNote(track, x);
    }
    public int nextNote(int track, int pos) {
        YassRectangle r;
        int i = 0;
        if (track < 0 || track >= rects.size())
            return -1;
        for (Enumeration<?> e = rects.elementAt(track).elements(); e.hasMoreElements(); i++) {
            r = (YassRectangle) e.nextElement();
            if (! r.isPageBreak() && !r.isType(YassRectangle.GAP) && !r.isType(YassRectangle.START)&& !r.isType(YassRectangle.END))
                if (r.x - 1 >= pos || r.x + r.width >= pos)
                    return i;
        }
        return -1;
    }

    public double getMinVisibleMs() {
        int x = clip.x;
        if (paintHeights) {
            x += heightBoxWidth;
        }
        return fromTimelineExact(x);
    }
    public double getMaxVisibleMs() {
        int x = clip.x + clip.width - 1;
        return fromTimelineExact(x);
    }

    public boolean isVisibleMs(double ms) {
        return getMinVisibleMs() < ms && ms < getMaxVisibleMs();
    }

    public double getLeftMs() {
        int x = clip.x;
        if (paintHeights)
            x += heightBoxWidth;
        return fromTimeline(x);
    }

    public void setViewToNextPage() {
        table.gotoPage(1);
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double ms) {
        if (ms <= 0)
            ms = 10000L;
        duration = ms;
        dim.setSize(toTimeline(duration), dim.height);
        setSize(dim);
    }

    public double getBeatSize() {
        return wSize;
    }

    public void setBeatSize(double w) {
        wSize = (int)w;
        update();
    }

    public void setZoom(double w) {
        wSize = (int)w;
        dim.setSize(toTimeline(duration), dim.height);
        setSize(dim);
        update();
        if (table != null) {
            int i = table.getSelectionModel().getMinSelectionIndex();
            int j = table.getSelectionModel().getMaxSelectionIndex();
            if (i >= 0)
                scrollRectToVisible(i, j);
        }
        repaint();
    }

    public void setZoom(int i, int j, boolean force) {
        if (table == null)
            return;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int beat = min;
        int end = max;
        for (int k = i; k <= j; k++) {
            YassRow r = table.getRowAt(k);
            if (r.isNote()) {
                beat = r.getBeatInt();
                end = beat + r.getLengthInt();
            } else if (r.isPageBreak()) {
                end = r.getSecondBeatInt();
            } else if (r.isComment() && !r.getCommentTag().equals("END:")) {
                beat = table.msToBeat(0);
                if (r.getCommentTag().equals("GAP:"))
                    end = 0;
            } else if (r.isEnd()) {
                beat = Math.max(outgap - 1, 0);
                double b = table.getEnd();
                if (b < 0)
                    b = duration;
                end = table.msToBeat(b);
            }
            min = Math.min(min, beat);
            max = Math.max(max, end);
        }
        if (min == Integer.MAX_VALUE)
            return;

        // quick hack to get actual size on screen
        int d = ((JViewport) getParent()).getExtentSize().width - 2;
        if (d < 0) {
            System.out.println("warning: invalid sheet width");
        }
        d -= LEFT_BORDER + RIGHT_BORDER;
        if (paintHeights)
            d -= heightBoxWidth;
        double val = min == max ? d : d / (double) (max - min);
        if (force || val < wSize) {
            // adjust wSize
            wSize = (int)val;
            if (wSize == 0) wSize = val;
            dim.setSize(toTimeline(duration), dim.height);
            setSize(dim);
            if (isVisible()) {
                validate();
                update();
            }
        }
        scrollRectToVisible(i, j);
    }

    private class SlideThread extends Thread {
        private int off;
        private int ticks = 0;
        public boolean quit = false;

        public SlideThread(int off) {
            this.off=off;
        }
        public void run() {
            while (! quit && hiliteCue == PREV_SLIDE_PRESSED || hiliteCue == NEXT_SLIDE_PRESSED) {
                if (off < 0)
                    slideLeft(-off);
                else slideRight(off);
                ticks++;
                if (ticks == 50)
                    off *= 5;
                try { Thread.sleep(40); } catch (Exception e) {}
            }
        }
    }
    private SlideThread slideThread = null;

    private void startSlide (int off) {
        stopSlide();
        slideThread = new SlideThread(off);
        slideThread.start();
    }
    private void stopSlide() {
        if (slideThread != null) {
            slideThread.quit = true;
            slideThread = null;
        }
    }

    public void slideLeft(int off) {
        if (YassTable.getZoomMode() == YassTable.ZOOM_ONE) {
            YassTable.setZoomMode(YassTable.ZOOM_MULTI);
            enablePan(false);
            actions.revalidateLyricsArea();
            update();
        }
        Point vp = getViewPosition();
        vp.x = vp.x - off;
        if (vp.x < 0) {
            vp.x = 0;
        }
        setViewPosition(vp);
        if (playerPos < vp.x || playerPos > vp.x + clip.width) {
            int next = nextElement(vp.x);
            if (next >= 0) {
                YassRow row = table.getRowAt(next);
                if (!row.isNote() && next + 1 < table.getRowCount()) {
                    next = next + 1;
                    row = table.getRowAt(next);
                }
                if (row.isNote()) {
                    table.setRowSelectionInterval(next, next);
                    table.updatePlayerPosition();
                }
            }
        }
    }
    public void slideRight(int off) {
        if (YassTable.getZoomMode() == YassTable.ZOOM_ONE) {
            YassTable.setZoomMode(YassTable.ZOOM_MULTI);
            enablePan(false);
            actions.revalidateLyricsArea();
            update();
        }
        Point vp = getViewPosition();
        vp.x = vp.x + off;
        if (vp.x < 0) {
            vp.x = 0;
        }
        setViewPosition(vp);
        if (playerPos < vp.x || playerPos > vp.x + clip.width) {
            int next = nextElement(vp.x);
            if (next >= 0) {
                YassRow row = table.getRowAt(next);
                if (!row.isNote() && next + 1 < table.getRowCount()) {
                    next = next + 1;
                    row = table.getRowAt(next);
                }
                if (row.isNote()) {
                    table.setRowSelectionInterval(next, next);
                    table.updatePlayerPosition();
                }
            }
        }
    }

    // //////////////////////// PLAYBACK RENDERER
    public Dimension getPreferredSize() {
        return dim;
    }
    public int getAvailableAcceleratedMemory() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            GraphicsDevice[] gs = ge.getScreenDevices();
            // Get current amount of available memory in bytes for each screen
            for (GraphicsDevice g : gs) {
                // Workaround; see description
                VolatileImage im = g.getDefaultConfiguration().createCompatibleVolatileImage(1, 1);
                // Retrieve available free accelerated image memory
                int bytes = g.getAvailableAcceleratedMemory();
                // Release the temporary volatile image
                im.flush();

                return bytes;
            }
        } catch (HeadlessException e) {
            // Is thrown if there are no screen devices
        }
        return 0;
    }
    public void init(yass.renderer.YassSession s) {
        session = s;
    }
    public yass.renderer.YassSession getSession() {
        return session;
    }
    public void setVideoFrame(BufferedImage img) {
        videoFrame = img;
    }
    public boolean isPlaybackInterrupted() {
        return pisinterrupted;
    }
    public void setPlaybackInterrupted(boolean onoff) {
        pisinterrupted = onoff;
    }
    public boolean preparePlayback(long inpoint_ms, long endpoint_ms) {
        Graphics2D pg2 = (Graphics2D) getGraphics();
        if (pg2 == null) {
            return false;
        }
        pg2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        // g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        // RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        // g2.setRenderingHint(RenderingHints.KEY_RENDERING,
        // RenderingHints.VALUE_RENDER_SPEED);

        pgb = getBackBuffer().createGraphics();
        // gb.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        // RenderingHints.VALUE_ANTIALIAS_OFF);
        // gb.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        // RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        // gb.setRenderingHint(RenderingHints.KEY_RENDERING,
        // RenderingHints.VALUE_RENDER_SPEED);

        ppos = playerPos;
        playerPos = -1;
        setPlaying(true);

        psheetpos = getViewPosition();

        int maxwait = 10;
        while (isRefreshing() && maxwait-- > 0) {
            try {
                Thread.sleep(10);
            } catch (Exception ignored) { }
        }
        // stalls sometimes in c.print:
        // sheet.refreshImage();
        return true;
    }

    public void startPlayback() {
    }

    public void updatePlayback(long pos_ms) {
        int newPlayerPos = toTimeline(pos_ms);

        if (newPlayerPos <= playerPos) {
            return;
        }
        playerPos = newPlayerPos;
        if (playerPos > clip.x + clip.width) {
            setTemporaryStop(true);
            setPlaying(false);
            if (live) {
                setViewToNextPage();
                playerPos = toTimeline(pos_ms);
            } else {
                Point p = getViewPosition();
                p.x += clip.width;
                setViewPosition(p);
            }
            paintComponent(pgb);
            setPlaying(true);
            setTemporaryStop(false);
        }
        if (isPlaybackInterrupted())
            return;
        if (!isRefreshing()) {
            VolatileImage plain = getPlainBuffer();
            if (showVideo()) {
                BufferedImage img = videoFrame;
                if (img != null) {
                    int w = plain.getWidth();
                    int h = plain.getHeight();
                    int hh = (int) (w * 3 / 4.0);
                    int yy = h / 2 - hh / 2;
                    pgb.setColor(white);
                    pgb.fillRect(0, 0, w, yy);
                    pgb.fillRect(0, yy, w, h);
                    pgb.drawImage(img, 0, yy, w, hh, null);
                } else {
                    pgb.drawImage(plain, 0, 0, null);
                }
                pgb.translate(-clip.x, 0);
                paintLines(pgb);
                paintPlainRectangles(pgb);
                pgb.translate(clip.x, 0);
            } else if (showBackground()) {
                BufferedImage img = getBackgroundImage();
                if (img != null) {
                    int w = plain.getWidth();
                    int h = plain.getHeight();
                    int hh = (int) (w * 3 / 4.0);
                    int yy = h / 2 - hh / 2;
                    pgb.setColor(white);
                    pgb.fillRect(0, 0, w, yy);
                    pgb.fillRect(0, yy, w, h);
                    pgb.drawImage(img, 0, yy, w, hh, null);
                } else {
                    pgb.drawImage(plain, 0, 0, null);
                }
                pgb.translate(-clip.x, 0);
                paintLines(pgb);
                paintPlainRectangles(pgb);
                pgb.translate(clip.x, 0);
            } else {
                    int top = getTopLine() - 10;
                    int w = plain.getWidth();
                    int h = plain.getHeight() - top;
                    pgb.drawImage(plain, 0, top, w, top + h, 0, top, w,top + h, null);
            }

            if (getPlainBuffer().contentsLost())
                setErrorMessage(bufferlost);
            if (isPlaybackInterrupted())
                return;
            paintText(pgb);
            paintPlayerText(pgb);
            paintPlayerPosition(pgb, true);
            if (playerPos < clip.x)
                paintWait(pgb, (int) fromTimeline(clip.x - playerPos));
            paintTemporaryNotes();
            paintRecordedNotes();

            Graphics2D pg2 = (Graphics2D) getGraphics();
            if (!showVideo()) {
                int top = getTopLine() - 10;
                int w = plain.getWidth();
                int h = plain.getHeight() - top;
                paintBackBuffer(pg2, 0, top, w, top + h);
            } else {
                paintBackBuffer(pg2);
            }
        }
    }

    public void finishPlayback() {
        Graphics2D pg2 = (Graphics2D) getGraphics();
        pg2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isLive()) {
            previewEdit(false);
            showVideo(false);
        }
        showBackground(false);
        setLyricsVisible(true);
        if (!isLive()) {
            setViewPosition(psheetpos);
            setPlayerPosition(ppos);
        }
        setLive(false);
        setPlaying(false);
        repaint();
    }

    public JComponent getComponent() {
        return this;
    }
    public void setPause(boolean onoff) {
    }

    private ArrayList<YassSheetListener> listeners = new ArrayList<>();
    public void addYassSheetListener(YassSheetListener listener) {
        listeners.add(listener);
    }
    public void removeYassSheetListener(YassSheetListener listener) {
        listeners.remove(listener);
    }
    public void firePosChanged() {
        double posMs = fromTimeline(playerPos);
        for (YassSheetListener listener : listeners) listener.posChanged(this, posMs);
    }
    public void fireRangeChanged(int minH, int maxH, int minB, int maxB) {
        for (YassSheetListener listener : listeners) listener.rangeChanged(this, minH, maxH, minB, maxB);
    }
    public void firePropsChanged() {
        for (YassSheetListener listener : listeners) listener.propsChanged(this);
    }

    public void stopPlaying() {
        firePropertyChange("play", null, "stop");
    }
}
