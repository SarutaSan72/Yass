package yass.screen;

import yass.YassSong;
import yass.YassTable;
import yass.stats.YassStats;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 4. September 2006
 */
public class YassSelectSongScreen extends YassScreen {
    private static final long serialVersionUID = -5077683864700080314L;
    YassScreenGroup group = null;
    private int itemCount = 0;
    private int selectedItem = 0;
    private int topItem = 0;
    private boolean randomActive = false;
    private BufferedImage img = null;
    private String currentSong = null;
    private YassSong song = new YassSong("", "", "", "", "");
    private String speedLabel, melodicLabel, bumpyLabel, leapsLabel, holdsLabel;
    private PlaybackThread playbackThread = null;

    /**
     * Gets the iD attribute of the YassScoreScreen object
     *
     * @return The iD value
     */
    public String getID() {
        return "selectsong";
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public String nextScreen() {
        return "playsong";
    }

    /**
     * Description of the Method
     */
    public void init() {
        speedLabel = getString("", "stats_diagram_speed");
        melodicLabel = getString("", "stats_diagram_melodic");
        bumpyLabel = getString("", "stats_diagram_bumpy");
        leapsLabel = getString("", "stats_diagram_leaps");
        holdsLabel = getString("", "stats_diagram_holds");
    }

    /**
     * Description of the Method
     */
    public void show() {
        getTheme().interruptAll();

        group = getGroupAt(getSelectedGroup());
        selectedItem = 0;
        itemCount = getGroupAt(getSelectedGroup()).getSongs().size() + 1;
        updateSelection();
        startTimer(60);
    }

    /**
     * Description of the Method
     */
    public void hide() {
        getTheme().unloadSample("file:" + currentSong);
        stopTimer();
    }

    /**
     * Description of the Method
     */
    public void updateSelection() {
        if (selectedItem < itemCount - 1) {
            YassSongData sd = getSongDataAt(group.getSongAt(selectedItem));
            File imgfile = sd.getCover();
            if (imgfile.exists()) {
                try {
                    img = javax.imageio.ImageIO.read(imgfile);
                } catch (Exception e) {
                }
            } else {
                img = null;
            }
            loadStats(sd);
            playSelectedSong();
            repaint();
        }
    }

    /**
     * Description of the Method
     *
     * @param key Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean keyPressed(int key) {
        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (key == UP[t] || key == LEFT[t]) {
                selectedItem--;
                if (selectedItem < 0) {
                    selectedItem = itemCount - 1;
                }
                getTheme().playSample("songs_navigation.wav", false);
                repaint();
                return true;
            }
            if (key == DOWN[t] || key == RIGHT[t]) {
                selectedItem++;
                if (selectedItem > itemCount - 1) {
                    selectedItem = 0;
                }
                getTheme().playSample("songs_navigation.wav", false);
                repaint();
                return true;
            }
            if (key == SELECT[t]) {
                getTheme().unloadSample("file:" + currentSong);

                if (selectedItem == itemCount - 1) {
                    randomActive = true;
                    new Thread() {
                        public void run() {
                            random();
                        }
                    }.start();
                    return true;
                }

                setSelectedSong(selectedItem);
                getTheme().playSample("menu_selection.wav", false);
                gotoScreen("playsong");
                return true;
            }
        }
        return false;
    }

    /**
     * Description of the Method
     *
     * @param key Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean keyReleased(int key) {
        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (key == UP[t] || key == LEFT[t] || key == DOWN[t] || key == RIGHT[t]) {
                updateSelection();
            }
        }
        return true;
    }

    private void loadStats(YassSongData sd) {
        Vector<YassStats> stats = yass.stats.YassStats.getAllStats();
        YassTable table = new YassTable();
        song.clearStats();
        table.loadFile(sd.getKaraokeData().getPath());
        for (Enumeration<YassStats> en = stats.elements(); en.hasMoreElements(); ) {
            YassStats st = en.nextElement();
            st.calcStats(song, table);
        }
    }

    /**
     * Description of the Method
     */
    public void random() {
        selectedItem = (int) ((itemCount - 1) * Math.random());

        int steps = 50;
        selectedItem -= 50;
        while (selectedItem < 0) {
            selectedItem += itemCount;
        }
        selectedItem = selectedItem % itemCount;

        while (steps > 0) {
            selectedItem++;
            if (selectedItem > itemCount - 1) {
                selectedItem = 0;
            }

            getTheme().playSample("songs_navigation.wav", false);
            repaint();

            try {
                if (steps > 10) {
                    Thread.currentThread();
                    Thread.sleep(50);
                } else if (steps > 5) {
                    Thread.currentThread();
                    Thread.sleep(100);
                } else if (steps > 3) {
                    Thread.currentThread();
                    Thread.sleep(200);
                } else if (steps > 1) {
                    Thread.currentThread();
                    Thread.sleep(300);
                } else {
                    Thread.currentThread();
                    Thread.sleep(800);
                }
            } catch (Exception e) {
            }
            steps--;
        }
        randomActive = false;
        keyPressed(SELECT[0]);
    }

    /**
     * Description of the Method
     */
    public void playSelectedSong() {
        getTheme().unloadSample("file:" + currentSong);

        if (selectedItem < itemCount - 1) {
            YassScreenGroup group = getGroupAt(getSelectedGroup());
            YassSongData sd = getSongDataAt(group.getSongAt(selectedItem));
            File f = sd.getAudio();

            int startMillis = sd.getPreviewStart();
            if (startMillis <= 0) {
                startMillis = sd.getMedleyStart();
            }
            if (startMillis <= 0) {
                startMillis = sd.getStart();
            }
            if (startMillis <= 0) {
                startMillis = sd.getGap();
            }
            if (startMillis <= 0) {
                startMillis = 0;
            }

            currentSong = f.getPath();

            if (playbackThread != null) {
                playbackThread.interrupt = true;
            }
            playbackThread = new PlaybackThread(currentSong, startMillis);
            playbackThread.start();
        }
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        int margin = getMargin();
        int w = getSize().width;
        int h = getSize().height;

        int wsize = 2 * w / 5;
        int hsize = 60;
        int h2 = h / 2;

        int x = Math.max(w - wsize, margin + 60 + h2 - 10 - margin);
        int y = margin + 60;

        int i = selectedItem;
        y = h2;

        g2.setColor(getTheme().getColor(3));
        g2.setStroke(getThickStroke());
        g2.drawRect(x - 10, y - 30, wsize + 10, hsize);

        while (y < h - 50) {
            i = i % itemCount;
            g2.setColor(getTheme().getColor(selectedItem == i ? 3 : 2));
            g2.setFont(getTextFont());
            if (i == itemCount - 1) {
                g2.drawString(getString("random"), x, y + 10);
            } else {
                YassSongData sd = getSongDataAt(group.getSongAt(i));
                g2.drawString(sd.getTitle(), x, y);
                g2.setFont(getSubTextFont());
                g2.drawString(sd.getArtist(), x, y + 20);
            }
            i++;
            y += hsize;
        }
        i = selectedItem - 1;
        y = h2 - hsize;
        while (y > margin) {
            i = i % itemCount;
            if (i < 0) {
                i += itemCount;
            }
            g2.setColor(getTheme().getColor(selectedItem == i ? 3 : 2));
            g2.setFont(getTextFont());
            if (i == itemCount - 1) {
                g2.drawString(getString("random"), x, y + 10);
            } else {
                YassSongData sd = getSongDataAt(group.getSongAt(i));
                g2.drawString(sd.getTitle(), x, y);
                g2.setFont(getSubTextFont());
                g2.drawString(sd.getArtist(), x, y + 20);
            }
            i--;
            y -= hsize;
        }

        x = margin + 40;

        int imgsize = h2 - 10 - margin;
        if (img != null && imgsize > 20) {
            g2.drawImage(img, x, h2 - 10 - imgsize, imgsize, imgsize, null);
            g2.setColor(Color.white);
            g2.setStroke(getThickStroke());
            g2.drawRect(x, h2 - 10 - imgsize, imgsize, imgsize);
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int statSize = imgsize;
        int offx = 0;
        int offy = 30;

        int mx = x + statSize;
        h -= 30;

        Color blue = new Color(getTheme().getColor(3).getRed(), getTheme().getColor(3).getGreen(), getTheme().getColor(3).getBlue(), 128);

        g2.setColor(blue);
        g2.setStroke(getStandardStroke());
        g2.drawOval(mx - statSize - offx, h - statSize - offy, statSize, statSize);

        int statSize8 = statSize / 8;
        g2.setColor(Color.white);
        g2.fillOval(mx - statSize / 2 - offx - statSize8, h - statSize / 2 - offy - statSize8, statSize8 * 2, statSize8 * 2);

        g2.setColor(getTheme().getColor(1));
        g2.setStroke(new BasicStroke(statSize8));
        g2.drawOval(mx - statSize / 2 - offx - 2 * statSize8 - statSize8 / 2, h - statSize / 2 - offy - 2 * statSize8 - statSize8 / 2, 5 * statSize8, 5 * statSize8);

        int speedlenIndex = yass.stats.YassStats.indexOf("speedlen");
        float speedlen = song.getStatsAt(speedlenIndex);
        int speeddistIndex = yass.stats.YassStats.indexOf("speeddist");
        float speeddist = song.getStatsAt(speeddistIndex);
        float speed = Math.min(1, 0.5f * speeddist / 7f + 0.5f * speedlen / 8f);

        int pitchrangepageIndex = yass.stats.YassStats.indexOf("pitchrangepage");
        float melodic = song.getStatsAt(pitchrangepageIndex);
        melodic = Math.min(1, melodic / 8f);

        int pitchdistanceIndex = yass.stats.YassStats.indexOf("pitchdistance");
        float bumpy = song.getStatsAt(pitchdistanceIndex);
        bumpy = Math.min(1, bumpy / 3f);

        int pitchleaps3Index = yass.stats.YassStats.indexOf("pitchleaps3");
        float pitchleaps3 = song.getStatsAt(pitchleaps3Index);
        int pitchleaps6Index = yass.stats.YassStats.indexOf("pitchleaps6");
        float pitchleaps6 = song.getStatsAt(pitchleaps6Index);
        float leaps = Math.min(1, .5f * pitchleaps3 / 25f + .5f * pitchleaps6 / 10f);

        int holds1secIndex = yass.stats.YassStats.indexOf("holds1sec");
        float holds1sec = song.getStatsAt(holds1secIndex);
        int holds3secIndex = yass.stats.YassStats.indexOf("holds3sec");
        float holds3sec = song.getStatsAt(holds3secIndex);
        float holds = Math.min(1, .5f * holds1secIndex / 15f + .5f * holds3sec / 2f);

        g2.setStroke(getThickStroke());
        Polygon p = new Polygon();
        Polygon p2 = new Polygon();

        mx = mx - statSize / 2 - offx;
        int my = h - statSize / 2 - offy;
        int r = statSize / 2;
        int num = 5;
        double sec = 2 * Math.PI / (float) num;

        int fontsize = 12;

        int polx = (int) (mx + leaps * r * Math.cos(sec));
        int poly = (int) (my + leaps * r * Math.sin(sec));
        int maxx = (int) (mx + r * Math.cos(sec));
        int maxy = (int) (my + r * Math.sin(sec));
        int maxx2 = (int) (mx + 1.1 * r * Math.cos(sec));
        int maxy2 = (int) (my + 1.1 * r * Math.sin(sec));
        g2.setColor(getTheme().getColor(3));
        g2.drawLine(mx, my, maxx, maxy);
        g2.setFont(getSubTextFont());
        FontMetrics metrics = g2.getFontMetrics();
        String label = leapsLabel;
        int llen = metrics.stringWidth(label);
        g2.setColor(getTheme().getColor(3));
        g2.drawString(label, maxx2, maxy2 + 6);
        p.addPoint(polx, poly);
        p2.addPoint(maxx, maxy);

        polx = (int) (mx + bumpy * r * Math.cos(2 * sec));
        poly = (int) (my + bumpy * r * Math.sin(2 * sec));
        maxx = (int) (mx + r * Math.cos(2 * sec));
        maxy = (int) (my + r * Math.sin(2 * sec));
        maxx2 = (int) (mx + 1.1 * r * Math.cos(2 * sec));
        maxy2 = (int) (my + 1.1 * r * Math.sin(2 * sec));
        g2.drawLine(mx, my, maxx, maxy);
        label = bumpyLabel;
        llen = metrics.stringWidth(label);
        g2.setColor(getTheme().getColor(3));
        g2.drawString(label, maxx2 - llen, maxy2 + 6);
        p.addPoint(polx, poly);
        p2.addPoint(maxx, maxy);

        polx = (int) (mx + melodic * r * Math.cos(3 * sec));
        poly = (int) (my + melodic * r * Math.sin(3 * sec));
        maxx = (int) (mx + r * Math.cos(3 * sec));
        maxy = (int) (my + r * Math.sin(3 * sec));
        maxx2 = (int) (mx + 1.1 * r * Math.cos(3 * sec));
        maxy2 = (int) (my + 1.1 * r * Math.sin(3 * sec));
        g2.drawLine(mx, my, maxx, maxy);
        label = melodicLabel;
        llen = metrics.stringWidth(label);
        g2.setColor(getTheme().getColor(3));
        g2.drawString(label, maxx2 - llen, maxy2 + 6);
        p.addPoint(polx, poly);
        p2.addPoint(maxx, maxy);

        polx = (int) (mx + speed * r * Math.cos(4 * sec));
        poly = (int) (my + speed * r * Math.sin(4 * sec));
        maxx = (int) (mx + r * Math.cos(4 * sec));
        maxy = (int) (my + r * Math.sin(4 * sec));
        maxx2 = (int) (mx + 1.1 * r * Math.cos(4 * sec));
        maxy2 = (int) (my + 1.1 * r * Math.sin(4 * sec));
        g2.drawLine(mx, my, maxx, maxy);
        label = speedLabel;
        llen = metrics.stringWidth(label);
        g2.setColor(getTheme().getColor(3));
        g2.drawString(label, maxx2, maxy2 + 6);
        p.addPoint(polx, poly);
        p2.addPoint(maxx, maxy);

        polx = (int) (mx + holds * r * Math.cos(5 * sec));
        poly = (int) (my + holds * r * Math.sin(5 * sec));
        maxx = (int) (mx + r * Math.cos(5 * sec));
        maxy = (int) (my + r * Math.sin(5 * sec));
        maxx2 = (int) (mx + 1.1 * r * Math.cos(5 * sec));
        maxy2 = (int) (my + 1.1 * r * Math.sin(5 * sec));
        g2.drawLine(mx, my, maxx, maxy);
        label = holdsLabel;
        llen = metrics.stringWidth(label);
        g2.setColor(getTheme().getColor(3));
        g2.drawString(label, maxx2, maxy2 + 6);
        p.addPoint(polx, poly);
        p2.addPoint(maxx, maxy);

        g2.drawPolygon(p2);

        if (speedlen >= 0) {
            g2.setColor(blue);
            g2.fillPolygon(p);
            g2.setColor(getTheme().getColor(3));
            g2.drawPolygon(p);
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);

    }

    class PlaybackThread extends Thread {
        public String song = null;
        public int startMillis = 0;
        public boolean interrupt = false;


        /**
         * Constructor for the PlaybackThread object
         *
         * @param song        Description of the Parameter
         * @param startMillis Description of the Parameter
         */
        public PlaybackThread(String song, int startMillis) {
            this.song = song;
            this.startMillis = startMillis;
        }


        public void run() {
            try {
                sleep(30);
            } catch (Exception e) {
            }
            if (song != currentSong) {
                return;
            }
            getTheme().loadSample("file:" + song);

            if (song != currentSong) {
                getTheme().unloadSample("file:" + song);
                return;
            }

            getTheme().playSample("file:" + song, true, startMillis);

            while (!interrupt) {
                try {
                    sleep(30);
                } catch (Exception e) {
                }
            }
            getTheme().unloadSample("file:" + song);
        }
    }
}

