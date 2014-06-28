package yass.renderer;

import yass.YassProperties;
import yass.screen.YassTheme;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 4. September 2006
 */
public class YassBasicRenderer extends JPanel implements yass.renderer.YassPlaybackRenderer {

    private static final long serialVersionUID = 775652994428561995L;
    private final static int fs = 24;
    private Font big[] = new Font[]{
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
            new Font("SansSerif", Font.BOLD, fs + 16)
    };
    private static Hashtable<String, String> strings = new Hashtable<>();
    BufferedImage noteStar[] = null;
    private YassSession session = null;
    private YassProperties prop = null;
    private YassTheme theme = null;
    private Rectangle clip = null;
    private BufferedImage bgImage = null;
    private BufferedImage videoFrame = null;
    private boolean showVideo = true, showBackground = true;
    private VolatileImage buffer = null;
    private Graphics2D bufferGraphics = null;
    private boolean playbackInterrupted = false;
    private boolean pause = false;
    private boolean debugScore = false;
    private Color colors[] = null;
    private Color pcolors[] = null;
    private BasicStroke stdStroke = new BasicStroke(1f), medStroke = new BasicStroke(1.5f), thickStroke = new BasicStroke(2f);
    private int ratio = FOUR_TO_THREE;
    private Font bigFont = new Font("SansSerif", Font.BOLD, 60);

    private int TOP_NOTES_BORDER = 20;
    private int LYRICS_HEIGHT = 60;
    private int NORM_HEIGHT = 20;
    private int PLAYER_SPACE = 60;

    private double hSize = -1;

    private String message = "";

    private long currentMillis = -1;
    private long fpsMillis = -1;
    private int fpsCount = 0;
    private int fps = 0;
    private long songStartMillis = -1;
    private long songEndMillis = -1;
    private int currentLine[] = null;
    private int timelinePos[] = null;
    private Vector<?> noteShapes[] = null;
    private long lineLastNoteMillis[] = null;
    private long playerLineTextStartMillis[] = null;
    private Rectangle bounds[] = null;

    private Vector<StarInfo> flyingStars[] = null;
    private int lineExplosion[] = null;
    private double randoms[] = null;

    private String scoreFormat = String.format("%%0%dd", 5);

    private long lastFlyingMillis[] = null;
    private long lastExplosionMillis[] = null;
    private long introMillis = -1;

    private String halftime = null;


    /**
     * Constructor for the YassSheet object
     */
    public YassBasicRenderer() {
        // disable double buffer; done manually
        super(false);
        setFocusable(true);
        setIgnoreRepaint(true);

        // prevent menu focus for alt key
        Action keepfocus =
                new AbstractAction("Keep Focus") {
                    private static final long serialVersionUID = 6702876922885062532L;

                    public void actionPerformed(ActionEvent e) {
                    }
                };
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, 0, true), "keepfocus");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(0, Event.ALT_MASK, true), "keepfocus");
        getActionMap().put("keepfocus", keepfocus);

        addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        boolean twice = e.getClickCount() > 1;

                        JComponent c = getComponent();
                        if (twice) {
                            resetRatio();
                        }
                    }
                });

        noteStar = loadSprite("star");

        halftime = getString("halftime");
    }

    /**
     * Gets the iD attribute of the YassBasicRenderer object
     *
     * @return The iD value
     */
    public String getID() {
        return "basic";
    }

    /**
     * Gets the string attribute of the YassPlaybackRenderer object
     *
     * @param key Description of the Parameter
     * @return The string value
     */
    public String getString(String key) {
        String s = strings.get(key);
        if (s == null) {
            try {
                s = yass.I18.get("renderer_" + getID() + "_" + key);
                strings.put(key, s);
            } catch (Exception e) {
            }
        }
        if (s == null) {
            s = yass.I18.get("renderer_core_" + key);
            strings.put(key, s);
        }
        return s;
    }

    /**
     * Gets the session attribute of the YassBasicRenderer object
     *
     * @return The session value
     */
    public YassSession getSession() {
        return session;
    }

    /**
     * Sets the backgroundImage attribute of the YassBasicRenderer object
     *
     * @param img The new backgroundImage value
     */
    public void setBackgroundImage(BufferedImage img) {
        bgImage = img;
    }

    /**
     * Sets the videoFrame attribute of the YassBasicRenderer object
     *
     * @param img The new videoFrame value
     */
    public void setVideoFrame(BufferedImage img) {
        videoFrame = img;
    }

    /**
     * Sets the ratio attribute of the YassBasicRenderer object
     *
     * @param r The new ratio value
     */
    public void setRatio(int r) {
        ratio = r;
        resetRatio();
    }

    /**
     * Description of the Method
     */
    public void resetRatio() {
        Window root = SwingUtilities.getWindowAncestor(getComponent());
        if (root instanceof JFrame) {
            JFrame f = (JFrame) root;
            int w = clip.width;
            int h = (int) (w * 3 / 4.0);
            getComponent().setPreferredSize(new Dimension(w, h));
            f.pack();

            Graphics2D g = (Graphics2D) getGraphics();
            if (g != null) {
                validateBuffer(g);
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public void paintComponent(Graphics g) {
        if (buffer == null) {
            return;
        }
        if (pause) {
            updatePlayback(currentMillis);
            g.drawImage(buffer, clip.x, clip.y, null);
        }
    }

    /**
     * Sets the pause attribute of the YassBasicRenderer object
     *
     * @param onoff The new pause value
     */
    public void setPause(boolean onoff) {
        pause = onoff;
        setIgnoreRepaint(!onoff);
        repaint();
    }

    /**
     * Gets the buffer attribute of the YassSheet object
     *
     * @return The buffer value
     */
    public VolatileImage getBackBuffer() {
        return buffer;
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean validateBuffer(Graphics2D g) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return false;
        }

        clip = new Rectangle(0, 0, w, h);
        if (buffer == null || buffer.getWidth() != clip.width || buffer.getHeight() != clip.height) {
            buffer = g.getDeviceConfiguration().createCompatibleVolatileImage(clip.width, clip.height, Transparency.OPAQUE);
            init();
            return true;
        }
        if (buffer.validate(getGraphicsConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE) {
            buffer.flush();
            buffer = g.getDeviceConfiguration().createCompatibleVolatileImage(clip.width, clip.height, Transparency.OPAQUE);
            init();
            return true;
        }
        return false;
    }

    /**
     * Description of the Method
     *
     * @param g          Description of the Parameter
     * @param waitMillis Description of the Parameter
     * @param t          Description of the Parameter
     */
    public void paintWait(Graphics2D g, long waitMillis, int t) {
        int sec = (int) (waitMillis / 1000);

        if (waitMillis <= 4000) {
            int width = (int) (60 * waitMillis / 4000.0);
            g.setColor(colors[0]);
            g.fillRect(0, clip.height - LYRICS_HEIGHT, width, 32);
        } else {
            int width = 60;
            g.setColor(colors[0]);
            g.fillRect(0, clip.height - LYRICS_HEIGHT, width, 32);
        }
        if (waitMillis > 3000) {
            String s = sec + "";
            g.setFont(big[0]);
            g.setColor(colors[7]);
            FontMetrics metrics = g.getFontMetrics();
            int strw = metrics.stringWidth(s);
            float sx = 30 - strw / 2;
            float sh = clip.height - LYRICS_HEIGHT + 24;
            g.drawString(s, sx, sh);
        }
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     * @param t Description of the Parameter
     */
    public void paintText(Graphics2D g, int t) {
        YassTrack track = session.getTrack(t);
        int j = track.getLine(currentLine[t]).getFirstNote();
        int k = track.getLine(currentLine[t]).getLastNote();

        if (showVideo() || showBackground()) {
            g.setColor(colors[6]);
            g.fillRect(0, clip.height - LYRICS_HEIGHT, clip.width, LYRICS_HEIGHT);
        }

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int strwidth = 0;
        g.setFont(big[0]);
        FontMetrics metrics = g.getFontMetrics();
        for (int i = j; i <= k; i++) {
            String str = track.getNote(i).getText();
            int strw = metrics.stringWidth(str);
            strwidth += strw;
        }

        int currentNote = track.getCurrentNote();
        double currentPercent = track.getCurrentPercent();
        int strpos = 0;
        float sh = clip.height - LYRICS_HEIGHT + 24;
        for (int i = j; i <= k; i++) {
            String str = track.getNote(i).getText();

            g.setFont(big[0]);
            g.setColor(colors[2]);
            metrics = g.getFontMetrics();
            int strw = metrics.stringWidth(str);
            int strh = metrics.getHeight();

            int offx = 0;
            int offy = 0;
            if (i == currentNote && currentPercent >= 0) {
                int shade = (int) ((1 - currentPercent) * (big.length - 1));
                g.setColor(colors[3]);
                g.setFont(big[shade]);
                metrics = g.getFontMetrics();
                int strw2 = metrics.stringWidth(str);
                int strh2 = metrics.getHeight();
                offx = -(strw2 - strw) / 2;
                offy = (strh2 - strh) / 4;
            }

            float sx = clip.width / 2 - strwidth / 2 + strpos;
            strpos += strw;

            g.drawString(str, sx + offx, sh + offy);
        }

        // next line
        if (currentLine[t] + 1 >= track.getLineCount()) {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            return;
        }
        j = track.getLine(currentLine[t] + 1).getFirstNote();
        k = track.getLine(currentLine[t] + 1).getLastNote();

        StringBuffer sb = new StringBuffer();
        for (int i = j; i <= k; i++) {
            String str = track.getNote(i).getText();
            sb.append(str);
        }
        String str = sb.toString();

        sh = clip.height - LYRICS_HEIGHT + 52;
        g.setFont(big[0]);
        g.setColor(colors[2]);
        metrics = g.getFontMetrics();
        strwidth = metrics.stringWidth(str);
        int strh = metrics.getHeight();
        float sx = clip.width / 2 - strwidth / 2;
        g.drawString(str, sx, sh);

        g.setFont(big[0]);
        g.setColor(colors[1]);

        long midMillis = (songEndMillis - songStartMillis) / 2;
        if ((currentMillis > midMillis - 3000) && (currentMillis < midMillis + 3000)) {
            String s = halftime;
            int sw = metrics.stringWidth(s);
            double zoom = (currentMillis - midMillis) / 100.0;
            int x = clip.width - 20 - sw - 12;
            int y = clip.height - LYRICS_HEIGHT / 2 - metrics.getAscent() / 2;
            boolean nozoom = true;
            if ((zoom > -30 && zoom < -29) || (zoom > 29 && zoom < 30)) {
                zoom = 30 - Math.abs(zoom);
                x = (int) (clip.width - 20 - sw / 2 - 12 - sw * zoom / 2);
                sw *= zoom;
                nozoom = false;
            }
            g.setColor(colors[1]);
            g.fillRoundRect(x, y, sw + 12, 30, 10, 10);
            g.setColor(colors[3]);
            g.drawRoundRect(x, y, sw + 12, 30, 10, 10);

            if (nozoom) {
                g.setColor(colors[3]);
                g.drawString(s, x + 6, y + 24);
            }
        }
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    /**
     * Sets the errorMessage attribute of the YassBasicRenderer object
     *
     * @param s The new errorMessage value
     */
    public void setErrorMessage(String s) {
    }

    /**
     * Gets the messageInfo attribute of the YassSheet object
     *
     * @return The messageInfo value
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the messageInfo attribute of the YassSheet object
     *
     * @param s The new messageInfo value
     */
    public void setMessage(String s) {
        message = s;
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public void paintMessage(Graphics2D g) {
        if (message == null || message.length() < 1) {
            return;
        }

        int x = getSize().width / 2;
        int y = getSize().height / 2;

        g.setFont(bigFont);

        Font f = g.getFont();
        TextLayout txtLayout = new TextLayout(message, f, g.getFontRenderContext());
        float sw = (float) txtLayout.getBounds().getWidth();
        AffineTransform transform = new AffineTransform();
        transform.setToTranslation(x - sw / 2, y);

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setStroke(thickStroke);
        g.setColor(colors[0]);
        g.draw(txtLayout.getOutline(transform));

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    /**
     * Description of the Method
     *
     * @param s  Description of the Parameter
     * @param p  Description of the Parameter
     * @param th Description of the Parameter
     */
    public void init(YassSession s, YassTheme th, YassProperties p) {
        session = s;
        theme = th;
        prop = p;

        colors = YassTheme.getRendererColors();
        pcolors = YassTheme.getPlayerColors();

        int trackCount = s.getTrackCount();
        timelinePos = new int[trackCount];
        currentLine = new int[trackCount];
        lineLastNoteMillis = new long[trackCount];
        bounds = new Rectangle[trackCount];
        flyingStars = new Vector[trackCount];
        lineExplosion = new int[trackCount];
        lastExplosionMillis = new long[trackCount];
        lastFlyingMillis = new long[trackCount];

        playerLineTextStartMillis = new long[trackCount];
        noteShapes = new Vector[trackCount];
        for (int t = 0; t < trackCount; t++) {
            noteShapes[t] = new Vector<Double>(32);
            flyingStars[t] = new Vector<>(64);
        }
        init();
    }

    /**
     * Description of the Method
     *
     * @param t Description of the Parameter
     */
    public void initLine(int t) {
        // adjust height
        hSize = bounds[t].height / (double) (NORM_HEIGHT - 2);
        if (hSize > 16) {
            hSize = 16;
        }

        YassTrack track = session.getTrack(t);
        int lineCount = track.getLineCount();

        YassLine line = track.getLine(currentLine[t]);
        int firstNote = line.getFirstNote();
        int lastNote = line.getLastNote();
        long lineStartMillis = track.getNote(firstNote).getStartMillis();
        long lineEndMillis = track.getNote(lastNote).getEndMillis();
        int lineMinHeight = line.getMinHeight();
        double lineMillis = lineEndMillis - lineStartMillis;

        //thread-safe
        Vector<Double> notes = new Vector<>(lastNote - firstNote + 1);
        for (int noteIndex = firstNote; noteIndex <= lastNote; noteIndex++) {
            YassNote note = track.getNote(noteIndex);
            long startMillis = note.getStartMillis();
            long endMillis = note.getEndMillis();
            int height = note.getHeight();

            RoundRectangle2D.Double r = new RoundRectangle2D.Double(0, 0, 0, 0, 10, 10);
            r.x = 20 + (startMillis - lineStartMillis) / lineMillis * (bounds[t].width - 40) + 3;
            r.y = bounds[t].y + bounds[t].height - (height - lineMinHeight + 4) * hSize - hSize + 1;
            r.width = 20 + (endMillis - lineStartMillis) / lineMillis * (bounds[t].width - 40) - 3 - r.x;
            r.height = 2 * hSize - 2;
            notes.addElement(r);
        }
        noteShapes[t] = notes;
        lineLastNoteMillis[t] = lineEndMillis;
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public void paintBackground(Graphics2D g) {
        float introSeconds = Math.max(0, ((currentMillis - introMillis) / 1000f));

        int w = clip.width;
        int h = clip.height;
        if (introSeconds < 2) {
            float alpha = introSeconds / 2f;
            g.setColor(colors[8]);
            g.fillRect(0, 0, w, h);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
        BufferedImage img = null;
        if (showVideo() && videoFrame != null) {
            img = videoFrame;
        } else if (showBackground() && bgImage != null) {
            img = bgImage;
        }
        if (img != null) {

            boolean wide = w >= h * 4 / 3.0;

            int left = 0;
            int top = 0;
            int width = w;
            int height = h;
            if (wide) {
                height = (int) (w * 3 / 4.0);
                top = h / 2 - height / 2;
            } else {
                width = (int) (h * 4 / 3.0);
                left = w / 2 - width / 2;
            }
            g.drawImage(img, left, top, width, height, null);
        }
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     * @param t Description of the Parameter
     */
    public void paintHeights(Graphics2D g, int t) {
        Line2D.Double line = new Line2D.Double(0, 0, bounds[t].width, 0);
        g.setStroke(stdStroke);
        g.setColor(colors[5]);
        for (int h = 0; h < NORM_HEIGHT; h += 2) {
            line.y1 = line.y2 = bounds[t].y + bounds[t].height - h * hSize;
            g.draw(line);
        }
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     * @param t Description of the Parameter
     */
    public void paintLine(Graphics2D g, int t) {
        YassTrack track = session.getTrack(t);
        YassLine line = track.getLine(currentLine[t]);
        int firstNote = line.getFirstNote();
        int lastNote = line.getLastNote();

        g.setStroke(medStroke);
        Enumeration<?> en = noteShapes[t].elements();

        for (int noteIndex = firstNote; noteIndex <= lastNote && en.hasMoreElements(); noteIndex++) {
            RoundRectangle2D.Double r = (RoundRectangle2D.Double) en.nextElement();
            YassNote note = track.getNote(noteIndex);
            if (note.getType() == YassNote.FREESTYLE) {
                continue;
            }

            int a = ((int) (currentMillis / 10)) % 300;
            if (a > 150) {
                a = 300 - a;
            }
            a += 50;
            g.setColor(new Color(pcolors[t].getRed(), pcolors[t].getGreen(), pcolors[t].getBlue(), a));
            r.x -= 4;
            r.width += 8;
            r.y -= 4;
            r.height += 8;
            g.fill(r);

            Color hiliteFill = note.getType() == YassNote.GOLDEN ? colors[4] : colors[1];
            g.setPaint(new GradientPaint((float) r.x, (float) r.y + 2, hiliteFill, (float) (r.x), (float) (r.y + r.height), colors[2]));
            r.x += 4;
            r.width -= 8;
            r.y += 4;
            r.height -= 8;
            g.fill(r);
            g.setColor(colors[2]);
            g.draw(r);

            if (debugScore) {
                int maxNoteScore = note.getMaxNoteScore();
                int maxGoldenScore = note.getMaxGoldenScore();
                int noteScore = (int) Math.round(note.getPlayerNoteScore());
                int goldenScore = (int) Math.round(note.getPlayerGoldenScore());
                g.setColor(colors[7]);
                g.drawString(noteScore + "/" + maxNoteScore, (int) r.x, (int) r.y - 2);
                if (note.getType() == YassNote.GOLDEN) {
                    g.drawString(goldenScore + "/" + maxGoldenScore, (int) r.x, (int) r.y - 14);
                }
            }
        }
        g.setStroke(stdStroke);

        if (debugScore) {
            int maxLineScore = line.getMaxLineScore();
            int lineScore = (int) Math.round(line.getPlayerLineScore());
            g.setColor(colors[7]);
            g.drawString(lineScore + "/" + maxLineScore, 10, 20);
        }
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     * @param t Description of the Parameter
     */
    public void paintPlayerNotes(Graphics2D g, int t) {
        YassTrack track = session.getTrack(t);
        Vector<?> playerNotes = track.getPlayerNotes();
        int lastPlayerNote = playerNotes.size() - 1;
        if (lastPlayerNote < 0) {
            return;
        }

        YassLine line = track.getLine(currentLine[t]);
        int firstNote = line.getFirstNote();
        int lastNote = line.getLastNote();

        YassNote current = track.getNote(track.getCurrentNote());
        boolean currentIsGolden = current.getType() == YassNote.GOLDEN;
        long currentStartMillis = current.getStartMillis();
        long currentEndMillis = current.getEndMillis();

        long lineStartMillis = track.getNote(firstNote).getStartMillis();
        long lineEndMillis = track.getNote(lastNote).getEndMillis();
        int lineMinHeight = line.getMinHeight();
        double lineMillis = lineEndMillis - lineStartMillis;

        g.setStroke(medStroke);

        RoundRectangle2D.Double r = new RoundRectangle2D.Double(0, 0, 0, 0, 10, 10);

        for (int noteIndex = lastPlayerNote; noteIndex >= 0; noteIndex--) {
            YassPlayerNote playerNote = (YassPlayerNote) playerNotes.elementAt(noteIndex);
            if (playerNote.isNoise()) {
                continue;
            }
            long startMillis = playerNote.getStartMillis();
            long endMillis = playerNote.getEndMillis();
            if (endMillis < lineStartMillis) {
                break;
            }
            int height = playerNote.getHeight();
            double level = playerNote.getLevel();
            double levelHeight = 2 * hSize * level;

            r.x = 20 + (startMillis - lineStartMillis) / lineMillis * (bounds[t].width - 40) + 1;
            r.y = bounds[t].y + bounds[t].height - (height - lineMinHeight + 4) * hSize - levelHeight / 2 + 1;
            r.width = 20 + (endMillis - lineStartMillis) / lineMillis * (bounds[t].width - 40) - 1 - r.x;
            r.height = levelHeight - 2;

            Color hiliteFill = colors[1];
            if (t == 0) {
                hiliteFill = colors[0];
            }
            g.setPaint(new GradientPaint((float) r.x, (float) r.y + 2, hiliteFill, (float) (r.x), (float) (r.y + r.height), pcolors[t]));

            g.fill(r);
            g.setColor(colors[2]);
            g.draw(r);

            if (current.getHeight() != playerNote.getHeight()) {
                continue;
            }

            if (noteIndex == lastPlayerNote && currentMillis < currentEndMillis) {
                long durationHit = endMillis - startMillis;
                long lastFly = currentMillis - lastFlyingMillis[t];
                boolean fly = false;
                if (!currentIsGolden) {
                    fly = (durationHit > 500 && lastFly > 100) || (durationHit > 800 && lastFly > 10);
                } else {
                    fly = (durationHit > 200 && lastFly > 50) || (durationHit > 400 && lastFly > 25);
                }

                if (fly) {
                    lastFlyingMillis[t] = currentMillis;

                    int x = (int) (r.x + r.width);
                    int anim = (int) (((currentMillis + 100 * x) % 1000) / 1000.0 * 20);
                    if (anim > 10) {
                        anim = 20 - anim;
                    }
                    int size = (int) (10 * randoms[(int) ((currentMillis + x) % randoms.length)] + anim);
                    int y = (int) (r.y);
                    double dx = 6 + Math.random() * 7.0;
                    double dy = 0.2 + Math.random() / 3.0;

                    y += (int) (Math.random() * r.height / 4);
                    if (currentIsGolden) {
                        size *= 1.5;
                        dx /= 2;
                        dy /= 2;
                    }
                    flyingStars[t].addElement(new StarInfo(x, y, dx, dy, size, size % noteStar.length));
                }
            }
        }
        g.setStroke(stdStroke);
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     * @param t Description of the Parameter
     */
    public void paintPlayerScore(Graphics2D g, int t) {
        YassTrack track = session.getTrack(t);

        int score = 123;

        String s = String.format(scoreFormat, (int) track.getPlayerScore());
        int sw = 110;

        int x = bounds[t].width - 20 - sw;
        int y = (int) (bounds[t].y + bounds[t].height - (NORM_HEIGHT - 1) * hSize);

        g.setColor(pcolors[t]);
        g.fillRoundRect(x, y, sw, 30, 10, 10);

        g.setColor(colors[1]);
        g.setFont(big[0]);
        g.drawString(theme.getPlayerSymbol(t, true) + " " + s, x + 6, y + 24);

        if (currentMillis > lineLastNoteMillis[t]) {
            playerLineTextStartMillis[t] = currentMillis;
        }
        if (currentMillis < playerLineTextStartMillis[t] + 1000 && playerLineTextStartMillis[t] > 0) {
            int y2 = (int) (bounds[t].y + bounds[t].height - (NORM_HEIGHT - 1) * hSize + 34);
            int y3 = (int) (y2 + hSize * 3);
            double alpha = (currentMillis - playerLineTextStartMillis[t]) / 1000.0;
            int y4 = (int) ((1 - alpha) * y3 + alpha * y2);

            g.setStroke(medStroke);

            s = track.getPlayerLineText();
            FontMetrics metrics = g.getFontMetrics();
            sw = metrics.stringWidth(s);
            x = bounds[t].width - 20 - sw - 12;

            double zoom = (currentMillis - playerLineTextStartMillis[t]) / 100.0;
            if (zoom < 1) {
                x = (int) (bounds[t].width - 20 - sw / 2 - 12 - sw * zoom / 2);
                sw *= zoom;
            }

            g.setColor(colors[1]);
            g.fillRoundRect(x, y4, sw + 12, 30, 10, 10);
            g.setColor(pcolors[t]);
            g.drawRoundRect(x, y4, sw + 12, 30, 10, 10);

            if (zoom >= 1) {
                g.setColor(pcolors[t]);
                g.drawString(s, x + 6, y4 + 24);
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     * @param t Description of the Parameter
     */
    public void paintStars(Graphics2D g, int t) {
        if (randoms == null) {
            randoms = new double[200];
            for (int i = 0; i < randoms.length; i++) {
                randoms[i] = Math.random();
            }
        }

        YassTrack track = session.getTrack(t);
        double currentPercent = track.getCurrentPercent();
        YassLine line = track.getLine(currentLine[t]);
        int firstNote = line.getFirstNote();
        int lastNote = line.getLastNote();

        Enumeration<?> en = noteShapes[t].elements();
        for (int noteIndex = firstNote; noteIndex <= lastNote; noteIndex++) {
            RoundRectangle2D.Double r = (RoundRectangle2D.Double) en.nextElement();
            YassNote note = track.getNote(noteIndex);
            if (note.getType() == YassNote.FREESTYLE) {
                continue;
            }

            if (note.getType() == YassNote.NORMAL && (note.getPlayerNoteScore() / (double) note.getMaxNoteScore() > 0.9)) {
                int spriteIndex = (int) Math.round(((currentMillis + r.x * 100) % 1000) / 1000.0 * (noteStar.length - 1));
                g.drawImage(noteStar[spriteIndex], (int) (r.x + r.width - noteStar[spriteIndex].getWidth() / 2), (int) r.y - noteStar[spriteIndex].getHeight() / 2, null);
            }
            if (note.getType() == YassNote.GOLDEN) {
                int spriteIndex = 0;
                for (int x = (int) r.x + 10; x < r.x + r.width - 5; x += 15, spriteIndex++) {
                    int anim = (int) (((currentMillis + 100 * x) % 1000) / 1000.0 * 20);
                    if (anim > 10) {
                        anim = 20 - anim;
                    }
                    int size = (int) (10 * randoms[(spriteIndex + 10) % randoms.length] + anim);
                    int y = (int) (r.y - 2 + randoms[x % randoms.length] * (r.height + 2));
                    g.drawImage(noteStar[spriteIndex % noteStar.length], x - size / 2, y - size / 2, size, size, null);
                }
            }
        }

        if (lineExplosion[t] != currentLine[t] && (line.getPlayerLineScore() / (double) line.getMaxLineScore() > 0.9)) {
            lineExplosion[t] = currentLine[t];
            int spriteIndex = 0;
            flyingStars[t].removeAllElements();
            for (int x = 20; x < bounds[t].width - 20; x += 15, spriteIndex++) {
                int anim = (int) (((currentMillis + 100 * x) % 1000) / 1000.0 * 20);
                if (anim > 10) {
                    anim = 20 - anim;
                }
                int size = (int) (10 * randoms[(int) ((currentMillis + x) % randoms.length)] + anim);
                int y = (int) (bounds[t].y + bounds[t].height - randoms[(int) ((currentMillis + x) % randoms.length)] * NORM_HEIGHT * (hSize - 2));
                flyingStars[t].addElement(new StarInfo(x, y, 0, 0.1 + Math.random() / 5.0, size, spriteIndex % noteStar.length));
            }
        }

        if (!flyingStars[t].isEmpty()) {
            boolean drawn = false;
            boolean move = false;
            if (currentMillis - lastExplosionMillis[t] > 10) {
                lastExplosionMillis[t] = currentMillis;
                move = true;
            }
            for (en = flyingStars[t].elements(); en.hasMoreElements(); ) {
                StarInfo si = (StarInfo) en.nextElement();
                if (move) {
                    si.vy += si.dy;
                    si.x += si.dx;
                    si.y += si.vy;
                }
                if (si.size > 0 && si.y < bounds[t].y + bounds[t].height) {
                    g.drawImage(noteStar[si.starIndex], si.x - si.size / 2, si.y - si.size / 2, si.size, si.size, null);
                } else {
                    flyingStars[t].removeElement(si);
                }
            }
        }
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean showVideo() {
        return showVideo;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean showBackground() {
        return showBackground;
    }

    /**
     * Description of the Method
     *
     * @param onoff Description of the Parameter
     */
    public void showBackground(boolean onoff) {
        showBackground = onoff;
    }

    /**
     * Description of the Method
     *
     * @param onoff Description of the Parameter
     */
    public void showVideo(boolean onoff) {
        showVideo = onoff;
    }

    /**
     * Gets the playbackInterrupted attribute of the PlaybackRenderer object
     *
     * @return The playbackInterrupted value
     */
    public boolean isPlaybackInterrupted() {
        return playbackInterrupted;
    }

    /**
     * Sets the playbackInterrupted attribute of the YassSheet object
     *
     * @param onoff The new playbackInterrupted value
     */
    public void setPlaybackInterrupted(boolean onoff) {
        playbackInterrupted = onoff;
    }

    /**
     * Description of the Method
     *
     * @param currentMillis Description of the Parameter
     * @param endMillis     Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean preparePlayback(long currentMillis, long endMillis) {
        this.songStartMillis = currentMillis;
        this.currentMillis = currentMillis;
        this.songEndMillis = endMillis;
        introMillis = currentMillis;

        colors = YassTheme.getRendererColors();
        pcolors = YassTheme.getPlayerColors();

        Graphics2D g = (Graphics2D) getGraphics();
        if (g == null) {
            return false;
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (!validateBuffer(g)) {
            init();
        }

        debugScore = prop.getProperty("debug-score").equals("true");
        int trackCount = session.getTrackCount();
        for (int t = 0; t < trackCount; t++) {
            timelinePos[t] = 0;
            currentLine[t] = 0;
            lineLastNoteMillis[t] = -1;

            if (session.getTrack(t).isActive()) {
                initLine(t);
            }
        }
        return true;
    }

    /**
     * Description of the Method
     */
    public void init() {
        int activeCount = session.getActiveTracks();
        if (activeCount == 0) {
            // demo mode
            activeCount = 1;
        }

        Dimension dim = getSize();
        int trackCount = session.getTrackCount();

        int top = TOP_NOTES_BORDER;
        int space = PLAYER_SPACE;

        int trackh = (dim.height - LYRICS_HEIGHT - top - space / 2 - (activeCount - 1) * space) / activeCount;

        if (trackh < 120) {
            space /= 2;
            trackh = (dim.height - LYRICS_HEIGHT - top - space / 2 - (activeCount - 1) * space) / activeCount;
        }
        if (trackh < 120) {
            space /= 2;
            trackh = (dim.height - LYRICS_HEIGHT - top - space / 2 - (activeCount - 1) * space) / activeCount;
        }

        int n = 1;
        for (int t = 0; t < trackCount; t++) {
            YassTrack track = session.getTrack(t);
            bounds[t] = new Rectangle(0, dim.height - LYRICS_HEIGHT - space / 2 - n * trackh - (n - 1) * space, dim.width, trackh);
            if (track.isActive()) {
                n++;
            }

            currentLine[t] = track.getCurrentLine();
            initLine(t);
        }
    }

    /**
     * Description of the Method
     */
    public void startPlayback() {
    }

    /**
     * Description of the Method
     *
     * @param currentMillis Description of the Parameter
     */
    public void updatePlayback(long currentMillis) {
        this.currentMillis = currentMillis;

        Graphics2D g = (Graphics2D) getGraphics();
        if (g == null || getBackBuffer() == null || bounds == null || bounds[0] == null) {
            return;
        }
        validateBuffer(g);

        bufferGraphics = getBackBuffer().createGraphics();
        paintBackground(bufferGraphics);

        int trackCount = session.getTrackCount();
        for (int t = 0; t < trackCount; t++) {
            YassTrack track = session.getTrack(t);
            if (!track.isActive()) {
                continue;
            }

            if (currentLine[t] != track.getCurrentLine()) {
                currentLine[t] = track.getCurrentLine();
                initLine(t);
            }

            if (!pause && isPlaybackInterrupted()) {
                return;
            }

            YassLine line = track.getLine(currentLine[t]);
            long startMillis = track.getNote(line.getFirstNote()).getStartMillis();
            long endMillis = track.getNote(line.getLastNote()).getEndMillis();
            double lineMillis = endMillis - startMillis;
            timelinePos[t] = (int) (20 + (currentMillis - startMillis) / lineMillis * (clip.width - 40) + 1);

            paintHeights(bufferGraphics, t);
            paintLine(bufferGraphics, t);
            paintPlayerNotes(bufferGraphics, t);
            paintPlayerScore(bufferGraphics, t);
            paintText(bufferGraphics, t);
            if (currentMillis < startMillis) {
                paintWait(bufferGraphics, startMillis - currentMillis, t);
            }
            paintStars(bufferGraphics, t);
        }

        fpsCount++;
        if (currentMillis > fpsMillis + 1000) {
            fpsMillis = currentMillis;
            fps = fpsCount;
            fpsCount = 0;
        }
        bufferGraphics.setColor(colors[8]);
        bufferGraphics.drawString(fps + " fps", clip.width - 110, clip.height - 10);

        paintMessage(bufferGraphics);

        g.drawImage(buffer, clip.x, clip.y, null);
    }

    /**
     * Description of the Method
     */
    public void finishPlayback() {
        if (!pause) {
            int trackCount = session.getTrackCount();
            for (int t = 0; t < trackCount; t++) {
                timelinePos[t] = 0;
                currentLine[t] = 0;
            }
        }
    }

    /**
     * Gets the component attribute of the YassBasicRenderer object
     *
     * @return The component value
     */
    public JComponent getComponent() {
        return this;
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public BufferedImage[] loadSprite(String s) {
        Vector<BufferedImage> spriteVector = new Vector<>();
        String name = "/yass/renderer/sprites/" + s + "_0.png";
        BufferedImage img = null;
        try {
            img = ImageIO.read(this.getClass().getResource(name));
        } catch (Exception e) {
            System.err.println("Sprite not found: " + name);
            img = null;
        }
        int i = 0;
        while (img != null) {
            spriteVector.addElement(img);
            i++;
            name = "/yass/renderer/sprites/" + s + "_" + i + ".png";
            try {
                img = ImageIO.read(this.getClass().getResource(name));
            } catch (Exception e) {
                img = null;
            }
        }
        BufferedImage imgArray[] = spriteVector.toArray(new BufferedImage[]{});
        return imgArray;
    }

    class StarInfo {
        public int x = 0;
        public int y = 0;
        public double vx = 0;
        public double vy = -2;
        public double dx = 0;
        public double dy = 0;
        public int size = 0;
        public int starIndex = 0;
        /**
         * Constructor for the StarInfo object
         *
         * @param x         Description of the Parameter
         * @param y         Description of the Parameter
         * @param size      Description of the Parameter
         * @param starIndex Description of the Parameter
         * @param dx        Description of the Parameter
         * @param dy        Description of the Parameter
         */
        public StarInfo(int x, int y, double dx, double dy, int size, int starIndex) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.size = size;
            this.starIndex = starIndex;
        }
    }
}

