package yass.screen;

import yass.I18;
import yass.YassInput;
import yass.YassProperties;
import yass.YassUtils;
import yass.renderer.YassPlaybackRenderer;
import yass.renderer.YassSession;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Description of the Interface
 *
 * @author Saruta
 * @created 22. März 2010
 */
public class YassScreen extends JPanel {
    /**
     * Description of the Field
     */
    public final static int MAX_PLAYERS = 3;
    private static boolean activePlayers[] = new boolean[MAX_PLAYERS];
    /**
     * Description of the Field
     */
    public final static int JUKEBOX_TIME = 7;
    /**
     * Description of the Field
     */
    public final static int[] UP = new int[]{3, 20, 37};
    /**
     * Description of the Field
     */
    public final static int[] RIGHT = new int[]{4, 21, 38};
    /**
     * Description of the Field
     */
    public final static int[] DOWN = new int[]{5, 22, 39};
    /**
     * Description of the Field
     */
    public final static int[] LEFT = new int[]{6, 23, 40};
    /**
     * Description of the Field
     */
    public final static int[] SELECT = new int[]{7, 24, 41};
    /**
     * Description of the Field
     */
    public final static int ESCAPE = 0;
    /**
     * Description of the Field
     */
    public final static int PAUSE = 1;
    /**
     * Description of the Field
     */
    public final static int LOAD = 2;
    /**
     * Description of the Field
     */
    public final static int KEY1 = 13;
    /**
     * Description of the Field
     */
    public final static int KEY2 = 14;
    /**
     * Description of the Field
     */
    public final static int KEY3 = 15;
    /**
     * Description of the Field
     */
    public final static int KEY4 = 16;
    /**
     * Description of the Field
     */
    public final static int KEY5 = 17;
    /**
     * Description of the Field
     */
    public final static int KEY6 = 18;
    /**
     * Description of the Field
     */
    public final static int KEY7 = 18;
    /**
     * Description of the Field
     */
    public final static int KEY8 = 19;
    /**
     * Description of the Field
     */
    public final static int KEY9 = 20;
    /**
     * Description of the Field
     */
    public final static int KEY10 = 21;
    /**
     * Description of the Field
     */
    public final static int KEY11 = 22;
    /**
     * Description of the Field
     */
    public final static int KEY12 = 23;
    /**
     * Description of the Field
     */
    public final static int FOUR_TO_THREE = 0;
    private static int ratio = FOUR_TO_THREE;
    /**
     * Description of the Field
     */
    public final static int SIXTEEN_TO_NINE = 1;
    private static final long serialVersionUID = 7267844722897446857L;
    private static boolean continueSession = false;
    private static boolean loading = false;
    private static String loadingString = null;
    private static YassSession session = null;
    private static YassProperties prop = null;
    private static YassDatabase db = null;
    private static YassInput input = null;
    private static KeyListener keyListener = null;
    private static int keycodes[] = new int[54];
    private static int keylocations[] = new int[54];
    private static Font bigFont = new Font("SansSerif", Font.BOLD, 60);
    private static Font titleFont = new Font("SansSerif", Font.BOLD, 32);
    private static Font textFont = new Font("SansSerif", Font.BOLD, 32);
    private static Font subtextFont = new Font("SansSerif", Font.BOLD, 20);
    private static Hashtable<String, YassScreen> screens = new Hashtable<>();
    private static Hashtable<String, String> strings = new Hashtable<>();
    private static Vector<YassSongData> songData = null;
    private static Vector<?> groupData = null;
    private static YassScreen currentScreen = null;
    private static YassScreen startScreen = null;
    private static String currentID = null;
    private static Dimension paintdim = new Dimension();
    private static GradientPaint gradientBackground = null;
    private static int margin = 80;
    private static ScreenChangeListener listener = null;
    private static Stroke thinStroke = new BasicStroke(1);
    private static Stroke stdStroke = new BasicStroke(2);
    private static Stroke thickStroke = new BasicStroke(4);
    private static int selectedGroup = 0;
    private static int selectedSorting = 0;
    private static int selectedSong = 0;
    private static float curtain = -1;
    private static YassTheme theme = null;
    private static String bgImage = null;
    private static Hashtable<String, BufferedImage> backgroundImages = new Hashtable<>();
    private static YassPlaybackRenderer renderer = null;
    private static String[] jukeboxPages = null;
    private static boolean jukebox = false;
    private static String screenParam = null;
    private static int jukeboxPageIndex = 0;
    private static int jukeboxSong = -1;
    private static JukeboxTimer jukeboxTimer = null;
    private static boolean inited = false;
    private static String nextID = null;
    private TimerThread timer = null;
    private boolean isTitleShown = true;
    private String title = null;


    /**
     * Constructor for the YassScreen object
     */
    public YassScreen() {
        // enable double buffer;
        super(true);
        setFocusable(true);

        if (yass.YassMain.NO_GAME) {
            return;
        }

        if (!inited) {
            currentScreen = this;
            if (getID().equals("start")) {
                startScreen = this;
            }
            for (int i = 0; i < activePlayers.length; i++) {
                activePlayers[i] = false;
            }
            renderer = new yass.renderer.YassBasicRenderer();
            db = new YassDatabase();

            String s = prop.getProperty("jukebox");
            jukeboxPages = s.split("\\|");
            for (int i = 0; i < jukeboxPages.length; i++) {
                jukeboxPages[i] = jukeboxPages[i].trim();
            }

            keyListener =
                    new KeyAdapter() {
                        public void keyPressed(KeyEvent e) {
                            if (e.isControlDown() || e.isAltDown()) {
                                return;
                            }

                            if (isLoading() || curtain > -1) {
                                e.consume();
                                return;
                            }

                            int code = e.getKeyCode();
                            int location = e.getKeyLocation();

                            if (isJukeboxEnabled()) {
                                for (int t = 0; t < MAX_PLAYERS; t++) {
                                    if ((code == keycodes[UP[t]] && location == keylocations[UP[t]]) || (code == keycodes[LEFT[t]] && location == keylocations[LEFT[t]])) {
                                        getTheme().playSample("menu_selection.wav", false);
                                        prevJukebox();
                                        if (getCurrentJukebox().startsWith("jukebox")) {
                                            getTheme().interruptAll();
                                        } else if (!getTheme().isPlaying("credits.mp3")) {
                                            getTheme().playSample("credits.mp3", true);
                                        }
                                        e.consume();
                                        return;
                                    }
                                    if ((code == keycodes[DOWN[t]] && location == keylocations[DOWN[t]]) || (code == keycodes[RIGHT[t]] && location == keylocations[RIGHT[t]])) {
                                        getTheme().playSample("menu_selection.wav", false);
                                        nextJukebox();
                                        if (getCurrentJukebox().startsWith("jukebox")) {
                                            getTheme().interruptAll();
                                        } else if (!getTheme().isPlaying("credits.mp3")) {
                                            getTheme().playSample("credits.mp3", true);
                                        }
                                        e.consume();
                                        return;
                                    }
                                }

                                enableJukebox(false);
                                gotoScreen("start");
                                e.consume();
                                return;
                            }

                            for (int i = 0; i < MAX_PLAYERS; i++) {
                                if (!activePlayers[i] && code == keycodes[SELECT[i]] && location == keylocations[SELECT[i]]) {
                                    getTheme().playSample("menu_selection.wav", false);
                                    activatePlayer(i);
                                    e.consume();
                                    repaint();
                                    return;
                                }
                                if (!activePlayers[i] && ((code == keycodes[UP[i]] && location == keylocations[UP[i]]) || (code == keycodes[DOWN[i]] && location == keylocations[DOWN[i]]) || (code == keycodes[LEFT[i]] && location == keylocations[LEFT[i]]) || (code == keycodes[RIGHT[i]] && location == keylocations[RIGHT[i]]))) {
                                    startJukebox();
                                    jukeboxTimer.time = 0;
                                    e.consume();
                                    return;
                                }
                            }

                            for (int i = 0; i < keycodes.length; i++) {
                                if (code == keycodes[i] && location == keylocations[i]) {
                                    if (currentScreen.keyPressed(i)) {
                                        e.consume();
                                    }
                                }
                            }
                        }


                        public void keyReleased(KeyEvent e) {
                            if (e.isControlDown() || e.isAltDown()) {
                                return;
                            }

                            if (isLoading() || curtain > -1) {
                                e.consume();
                                return;
                            }

                            int code = e.getKeyCode();
                            int location = e.getKeyLocation();

                            for (int i = 0; i < keycodes.length; i++) {
                                if (code == keycodes[i] && location == keylocations[i]) {
                                    if (currentScreen.keyReleased(i)) {
                                        e.consume();
                                    }
                                }
                            }
                        }
                    };

            input = new YassInput();
            YassInput.init();
            input.startPoll();
            input.addKeyListener(keyListener);

            loadInputMaps();
            inited = true;
        }

        addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        boolean twice = e.getClickCount() > 1;
                        if (twice) {
                            resetRatio();
                        }
                    }
                });
        addKeyListener(keyListener);
    }

    /**
     * Gets the loading attribute of the YassScreen object
     *
     * @return The loading value
     */
    public static boolean isLoading() {
        return loading;
    }

    /**
     * Sets the loading attribute of the YassScreen object
     *
     * @param onoff The new loading value
     */
    public static void setLoading(boolean onoff) {
        loading = onoff;
        if (currentScreen != null) {
            if (!loading) {
                currentScreen.startJukebox();
            }
            currentScreen.repaint();
        }
    }

    /**
     * Sets the loadingMessage attribute of the YassScreen class
     *
     * @param s The new loadingMessage value
     */
    public static void setLoadingMessage(String s) {
        loadingString = s;
        currentScreen.repaint();
    }

    /**
     * Description of the Method
     */
    public static void loadInputMaps() {
        YassInput input = getInput();

        input.removeMaps();
        for (int t = 0; t < MAX_PLAYERS; t++) {
            for (int k = 0; k < 17; k++) {
                int n = 2 + t * 17 + k;

                String pd = getProperties().getProperty("player" + t + "_dev_" + k);
                if (pd != null) {
                    int d = input.getControllerIndex(pd);
                    if (d >= 0) {
                        String controller = input.getControllerName(d);
                        String pc = getProperties().getProperty("player" + t + "_comp_" + k);
                        if (pc != null) {
                            int c = input.getComponentIndex(d, pc);
                            int cCount = input.getComponentCount(d);
                            if (c >= 0 && c < cCount) {
                                String component = input.getComponentName(d, c);
                                input.mapTo(currentScreen, controller, component, t, 0, getKeyCode(n), getKeyLocation(n));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param p Description of the Parameter
     */
    public static void loadPlugins(YassProperties p) {
        prop = p;

        theme = new YassTheme();
        screens = new Hashtable<>();
        StringTokenizer st = new StringTokenizer(prop.getProperty("screen-plugins"), "|");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            YassScreen screen = addPlugin(s);
            screens.put(screen.getID(), screen);
        }
    }

    /**
     * Gets the theme attribute of the YassScreen class
     *
     * @return The theme value
     */
    public static YassTheme getTheme() {
        return theme;
    }

    /**
     * Gets the properties attribute of the YassScreen class
     *
     * @return The properties value
     */
    public static YassProperties getProperties() {
        return prop;
    }

    private static YassScreen addPlugin(String name) {
        YassScreen s = null;
        try {
            Class<?> c = YassUtils.forName(name);
            s = (YassScreen) c.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        s.init();
        return s;
    }

    /**
     * Gets the input attribute of the YassScreen class
     *
     * @return The input value
     */
    public static YassInput getInput() {
        return input;
    }

    /**
     * Gets the continue attribute of the YassScreen class
     *
     * @return The continue value
     */
    public static boolean isContinue() {
        return continueSession;
    }

    /**
     * Sets the continue attribute of the YassScreen class
     *
     * @param onoff The new continue value
     */
    public static void setContinue(boolean onoff) {
        continueSession = onoff;
    }

    /**
     * Gets the playbackRenderer attribute of the YassScreen object
     *
     * @return The playbackRenderer value
     */
    public static YassPlaybackRenderer getPlaybackRenderer() {
        return renderer;
    }

    /**
     * Sets the playbackRenderer attribute of the YassScreen class
     *
     * @param r The new playbackRenderer value
     */
    public static void setPlaybackRenderer(YassPlaybackRenderer r) {
        renderer = r;
    }

    /**
     * Gets the session attribute of the YassScreen class
     *
     * @return The session value
     */
    public static YassSession getSession() {
        return session;
    }

    /**
     * Description of the Method
     *
     * @param s The new session value
     */
    public static void setSession(YassSession s) {
        session = s;
    }

    /**
     * Gets the database attribute of the YassScreen class
     *
     * @return The database value
     */
    public static YassDatabase getDatabase() {
        return db;
    }

    /**
     * Gets the currentSong attribute of the YassScreen class
     *
     * @return The currentSong value
     */
    public static YassSongData getSelectedSong() {
        return songData.elementAt(selectedSong);
    }

    /**
     * Sets the currentSong attribute of the YassScreen class
     *
     * @param i The new currentSong value
     */
    public static void setSelectedSong(int i) {
        selectedSong = i;
    }

    /**
     * Gets the jukeboxSong attribute of the YassScreen class
     *
     * @return The jukeboxSong value
     */
    public static YassSongData getJukeboxSong() {
        return songData.elementAt(jukeboxSong);
    }

    /**
     * Sets the jukeboxSong attribute of the YassScreen class
     *
     * @param i The new jukeboxSong value
     */
    public static void setJukeboxSong(int i) {
        jukeboxSong = i;
    }

    /**
     * Gets the songs attribute of the YassScreen class
     *
     * @return The songs value
     */
    public static Vector<YassSongData> getSongData() {
        return songData;
    }

    /**
     * Sets the songs attribute of the YassScreen class
     *
     * @param s The new songs value
     */
    public static void setSongData(Vector<YassSongData> s) {
        songData = s;
    }

    /**
     * Gets the groups attribute of the YassScreen class
     *
     * @return The groups value
     */
    public static Vector<?> getGroupsData() {
        return groupData;
    }

    /**
     * Gets the groupAt attribute of the YassScreen class
     *
     * @param i Description of the Parameter
     * @return The groupAt value
     */
    public static YassScreenGroup getGroupAt(int i) {
        Vector<?> groups = (Vector<?>) groupData.elementAt(selectedSorting);
        return (YassScreenGroup) groups.elementAt(i);
    }

    /**
     * Gets the sortingAt attribute of the YassScreen class
     *
     * @param i Description of the Parameter
     * @return The sortingAt value
     */
    public static String getSortingAt(int i) {
        Vector<?> groups = (Vector<?>) groupData.elementAt(i);
        return ((YassScreenGroup) groups.elementAt(0)).getTitle();
    }

    /**
     * Gets the songAt attribute of the YassScreen class
     *
     * @param i Description of the Parameter
     * @return The songAt value
     */
    public static YassSongData getSongDataAt(int i) {
        return songData.elementAt(i);
    }

    /**
     * Sets the groups attribute of the YassScreen class
     *
     * @param g The new groups value
     */
    public static void setGroupData(Vector<?> g) {
        groupData = g;
    }

    /**
     * Adds a feature to the PropertyChangeListener attribute of the YassScreen
     * class
     *
     * @param p The feature to be added to the PropertyChangeListener attribute
     */
    public static void addScreenChangeListener(ScreenChangeListener p) {
        listener = p;
    }

    /**
     * Description of the Method
     */
    public static void finishGotoScreen() {
        curtain = -1;
    }

    /**
     * Gets the textFont attribute of the YassScreen object
     *
     * @return The textFont value
     */
    public static Font getTitleFont() {
        return titleFont;
    }

    /**
     * Gets the bigFont attribute of the YassScreen class
     *
     * @return The bigFont value
     */
    public static Font getBigFont() {
        return bigFont;
    }

    /**
     * Gets the textFont attribute of the YassScreen class
     *
     * @return The textFont value
     */
    public static Font getTextFont() {
        return textFont;
    }

    /**
     * Gets the standardStroke attribute of the YassScreen class
     *
     * @return The standardStroke value
     */
    public static Stroke getStandardStroke() {
        return stdStroke;
    }

    /**
     * Gets the thinStroke attribute of the YassScreen class
     *
     * @return The thinStroke value
     */
    public static Stroke getThinStroke() {
        return thinStroke;
    }

    /**
     * Gets the thickStroke attribute of the YassScreen class
     *
     * @return The thickStroke value
     */
    public static Stroke getThickStroke() {
        return thickStroke;
    }

    /**
     * Gets the subTextFont attribute of the YassScreen class
     *
     * @return The subTextFont value
     */
    public static Font getSubTextFont() {
        return subtextFont;
    }

    /**
     * Gets the keyCodes attribute of the YassSheet object
     *
     * @param i Description of the Parameter
     * @return The keyCodes value
     */
    public static int getKeyCode(int i) {
        return keycodes[i];
    }

    /**
     * Gets the keyLocation attribute of the YassScreen class
     *
     * @param i Description of the Parameter
     * @return The keyLocation value
     */
    public static int getKeyLocation(int i) {
        return keylocations[i];
    }

    /**
     * Gets the keyCodes attribute of the YassScreen class
     *
     * @return The keyCodes value
     */
    public static int[] getKeyCodes() {
        return keycodes;
    }

    /**
     * Gets the keyLocations attribute of the YassScreen class
     *
     * @return The keyLocations value
     */
    public static int[] getKeyLocations() {
        return keylocations;
    }

    /**
     * Sets the ratio attribute of the YassBasicRenderer object
     *
     * @param r The new ratio value
     */
    public static void setRatio(int r) {
        ratio = r;
        resetRatio();
    }

    /**
     * Description of the Method
     */
    public static void resetRatio() {
        Window root = SwingUtilities.getWindowAncestor(getCurrentScreen());
        if (root instanceof JFrame) {
            JFrame f = (JFrame) root;
            int w = getCurrentScreen().getSize().width;
            int h = (int) (w * 3 / 4.0);
            getCurrentScreen().setPreferredSize(new Dimension(w, h));
            f.pack();
        }
    }

    /**
     * Gets the component attribute of the YassBasicRenderer object
     *
     * @return The component value
     */
    public static YassScreen getCurrentScreen() {
        return currentScreen;
    }

    /**
     * Description of the Method
     *
     * @param id Description of the Parameter
     * @return Description of the Return Value
     */
    public static YassScreen setCurrentScreen(String id) {
        currentID = id;
        currentScreen = screens.get(id);
        return currentScreen;
    }

    /**
     * Gets the margin attribute of the YassScreen class
     *
     * @return The margin value
     */
    public static int getMargin() {
        return margin;
    }

    /**
     * Sets the backgroundImage attribute of the YassScreen object
     *
     * @param s Description of the Parameter
     */
    public static void loadBackgroundImage(String s) {
        BufferedImage img = backgroundImages.get(s);
        if (img == null) {
            img = getTheme().getImage(s);
            backgroundImages.put(s, img);
        }
        bgImage = s;
    }

    /**
     * Sets the backgroundImage attribute of the YassScreen class
     *
     * @param s Description of the Parameter
     */
    public static void unloadBackgroundImage(String s) {
        backgroundImages.remove(s);
    }

    /**
     * Sets the jukebox attribute of the YassStartScreen object
     *
     * @param onoff The new jukebox value
     */
    public static void enableJukebox(boolean onoff) {
        jukebox = onoff;
        jukeboxPageIndex = -1;
    }

    /**
     * Gets the jukebox attribute of the YassStartScreen object
     *
     * @return The jukebox value
     */
    public static boolean isJukeboxEnabled() {
        return jukebox;
    }

    /**
     * Gets the screenParam attribute of the YassStartScreen object
     *
     * @return The screenParam value
     */
    public static String getScreenParam() {
        return screenParam;
    }

    /**
     * Sets the screenParam attribute of the YassScreen class
     *
     * @param s The new screenParam value
     */
    public static void setScreenParam(String s) {
        screenParam = s;
    }

    /**
     * Gets the jukeboxPageCount attribute of the YassScreen class
     *
     * @return The jukeboxPageCount value
     */
    public static int getJukeboxPageCount() {
        return jukeboxPages.length;
    }

    /**
     * Description of the Method
     */
    public void loadSongs() {
        setLoading(true);
        stopTimer();
        stopJukebox();
        listener.screenChanged(new ScreenChangeEvent(currentScreen, currentID, "loading"));
    }

    /**
     * Description of the Method
     *
     * @param i Description of the Parameter
     */
    public void activatePlayer(int i) {
        if (i < 0) {
            for (int t = 0; t < MAX_PLAYERS; t++) {
                activePlayers[t] = false;
            }
            startJukebox();
        } else {
            activePlayers[i] = true;
            stopJukebox();
        }
    }

    /**
     * Gets the cover attribute of the YassScreen object
     *
     * @param s Description of the Parameter
     * @return The cover value
     */
    public BufferedImage getCover(String s) {
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

        try {
            File file = new File(coverDir + File.separator + s + ".jpg");
            BufferedImage img = null;
            if (file.exists()) {
                img = javax.imageio.ImageIO.read(file);
            } else {
                java.net.URL is = I18.getResource(s + ".jpg");
                img = YassUtils.readImage(is);
            }
            return img;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Description of the Method
     *
     * @param t Description of the Parameter
     */
    public void startTimer(int t) {
        if (timer != null) {
            timer.interrupt = true;
        }
        timer = new TimerThread(t);
        timer.start();
    }

    /**
     * Description of the Method
     */
    public void stopTimer() {
        if (timer != null) {
            timer.interrupt = true;
        }
    }

    /**
     * Description of the Method
     */
    public void timerFinished() {
    }

    /**
     * Description of the Method
     */
    public void startJukebox() {
        if (loading || songData == null || songData.size() < 1) {
            return;
        }
        if (jukeboxTimer != null && !jukeboxTimer.interrupt) {
            jukeboxTimer.time = JUKEBOX_TIME;
            return;
        }
        jukeboxTimer = new JukeboxTimer(JUKEBOX_TIME);
        jukeboxTimer.start();
    }

    /**
     * Description of the Method
     */
    public void stopJukebox() {
        if (jukeboxTimer != null) {
            jukeboxTimer.interrupt = true;
        }
    }

    /**
     * Gets the selectedSorting attribute of the YassScreen object
     *
     * @return The selectedSorting value
     */
    public int getSelectedSorting() {
        return selectedSorting;
    }

    /**
     * Sets the selectedSorting attribute of the YassScreen object
     *
     * @param i The new selectedSorting value
     */
    public void setSelectedSorting(int i) {
        selectedSorting = i;
    }

    /**
     * Gets the selectedGroup attribute of the YassScreen object
     *
     * @return The selectedGroup value
     */
    public int getSelectedGroup() {
        return selectedGroup;
    }

    /**
     * Sets the selectedGroup attribute of the YassScreen object
     *
     * @param i The new selectedGroup value
     */
    public void setSelectedGroup(int i) {
        selectedGroup = i;
    }

    /**
     * Gets the groupBySorting attribute of the YassScreen object
     *
     * @return The groupBySorting value
     */
    public Vector<?> getGroupsBySorting() {
        return (Vector<?>) groupData.elementAt(selectedSorting);
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public String nextScreen() {
        return "library";
    }

    /**
     * Description of the Method
     *
     * @param i Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean keyPressed(int i) {
        return false;
    }

    /**
     * Description of the Method
     *
     * @param i Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean keyReleased(int i) {
        return false;
    }

    /**
     * Description of the Method
     */
    public void init() {
    }

    /**
     * Description of the Method
     */
    public void show() {
    }

    /**
     * Description of the Method
     */
    public void hide() {
    }

    /**
     * Description of the Method
     *
     * @param id Description of the Parameter
     */
    public void gotoScreen(String id) {
        nextID = id;

        if (id.equals("exit") || id.equals("library")) {
            getTheme().interruptAll();
            stopJukebox();
        }
        Thread t =
                new Thread() {
                    public void run() {
                        int w = currentScreen.getSize().width;
                        int h = currentScreen.getSize().height;
                        for (curtain = 1; curtain > 0; curtain -= .05f) {
                            currentScreen.paintImmediately(0, 0, w, h);
                            //currentScreen.repaint();
                            try {
                                sleep(10);
                            } catch (Exception e) {
                            }
                        }
                        curtain = 0;
                        listener.screenChanged(new ScreenChangeEvent(currentScreen, currentID, nextID));
                    }
                };
        t.start();
    }

    /**
     * Gets the iD attribute of the YassScreen object
     *
     * @return The iD value
     */
    public String getID() {
        return "core";
    }

    /**
     * Gets the label attribute of the YassFilter object
     *
     * @return The label value
     */
    public String getTitle() {
        if (title == null) {
            title = I18.get("screen_" + getID() + "_title");
        }
        return title;
    }

    /**
     * Gets the string attribute of the YassScreen object
     *
     * @param key Description of the Parameter
     * @return The string value
     */
    public String getString(String key) {
        String s = strings.get(key);
        if (s == null) {
            try {
                s = I18.get("screen_" + getID() + "_" + key);
                strings.put(key, s);
            } catch (Exception e) {
            }
        }
        if (s == null) {
            s = I18.get("screen_core_" + key);
            strings.put(key, s);
        }
        return s;
    }

    /**
     * Gets the string attribute of the YassScreen object
     *
     * @param key Description of the Parameter
     * @param pre Description of the Parameter
     * @return The string value
     */
    public String getString(String pre, String key) {
        String s = strings.get(pre + key);
        if (s == null) {
            try {
                s = I18.get(pre + key);
                if (s == null) {
                    s = key;
                }
            } catch (java.util.MissingResourceException ex) {
                s = key;
            }
            strings.put(pre + key, s);
        }
        return s;
    }

    /**
     * Gets the playerActive attribute of the YassScreen object
     *
     * @param i Description of the Parameter
     * @return The playerActive value
     */
    public boolean isPlayerActive(int i) {
        return activePlayers[i];
    }

    /**
     * Gets the jukeboxPage attribute of the YassStartScreen object
     */
    public void nextJukebox() {
        jukeboxPageIndex++;
        if (jukeboxPageIndex >= jukeboxPages.length) {
            jukeboxPageIndex = 0;
        }

        if (jukeboxTimer != null && !jukeboxTimer.interrupt) {
            jukeboxTimer.time = JUKEBOX_TIME;
        }

        String page = jukeboxPages[jukeboxPageIndex];
        int i = page.indexOf(":");
        setScreenParam(i > 0 ? page.substring(i + 1) : null);
        if (i > 0) {
            page = page.substring(0, i);
        }
        gotoScreen(page);
    }

    /**
     * Gets the currentJukebox attribute of the YassScreen object
     *
     * @return The currentJukebox value
     */
    public String getCurrentJukebox() {
        return jukeboxPages[jukeboxPageIndex];
    }

    /**
     * Description of the Method
     */
    public void prevJukebox() {
        jukeboxPageIndex--;
        if (jukeboxPageIndex < 0) {
            jukeboxPageIndex = jukeboxPages.length - 1;
        }

        if (jukeboxTimer != null && !jukeboxTimer.interrupt) {
            jukeboxTimer.time = JUKEBOX_TIME;
        }

        String page = jukeboxPages[jukeboxPageIndex];
        int i = page.indexOf(":");
        setScreenParam(i > 0 ? page.substring(i + 1) : null);
        if (i > 0) {
            page = page.substring(0, i);
        }
        gotoScreen(page);
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public int countActivePlayers() {
        int n = 0;
        for (int i = 0; i < MAX_PLAYERS; i++) {
            if (activePlayers[i]) {
                n++;
            }
        }
        return n;
    }

    /**
     * Gets the titleShown attribute of the YassScreen object
     *
     * @return The titleShown value
     */
    public boolean isTitleShown() {
        return isTitleShown;
    }

    /**
     * Sets the titleShown attribute of the YassScreen object
     *
     * @param onoff The new titleShown value
     */
    public void setTitleShown(boolean onoff) {
        isTitleShown = onoff;
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (curtain > 0) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        Dimension dim = getSize();
        int w = dim.width;
        int h = dim.height;

        BufferedImage img = null;
        if (bgImage != null) {
            img = backgroundImages.get(bgImage);
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
            g2.drawImage(img, left, top, width, height, null);
        } else {
            if (!dim.equals(paintdim)) {
                gradientBackground = new GradientPaint(0, 0, Color.white, dim.width, dim.height, getTheme().getColor(3), true);
                paintdim.setSize(dim);
            }

            g2.setPaint(Color.white);
            g2.fillRect(0, 0, dim.width, dim.height);
            g2.setPaint(gradientBackground);
            g2.fillRect(0, 0, dim.width, dim.height);

        }

        int x = margin;
        int y = margin;

        g2.setColor(getTheme().getColor(2));
        g2.fillRect(0, h - 24, w, 24);

        g2.setColor(getTheme().getColor(1));
        g2.setFont(getSubTextFont());

        if (loading) {
            String s = loadingString != null ? loadingString : getString("wait");
            int sw = g2.getFontMetrics().stringWidth(s);
            g2.drawString(s, w - sw - 10, h - 4);
        } else {
            String s = getString("active");
            boolean empty = true;
            for (int t = 0; t < MAX_PLAYERS; t++) {
                if (isPlayerActive(t)) {
                    s += " " + getTheme().getPlayerSymbol(t, true);
                    empty = false;
                }
            }
            if (!empty) {
                int sw = g2.getFontMetrics().stringWidth(s);
                g2.drawString(s, 10, h - 4);
            }

            s = getString("inactive");
            empty = true;
            for (int t = 0; t < MAX_PLAYERS; t++) {
                if (!isPlayerActive(t)) {
                    s += " " + getTheme().getPlayerSymbol(t, false);
                    empty = false;
                }
            }
            if (!empty) {
                int sw = g2.getFontMetrics().stringWidth(s);
                g2.drawString(s, w - sw - 10, h - 4);
            }
        }

        //GradientPaint gp = new GradientPaint(0, 100, Color.white, 0, 0, getTheme().getColor(3));
        //g2.setPaint(gp);

        float alpha = 1 - curtain;
        if (nextID != null && nextID.equals("playsong") && alpha >= 0 && alpha < 1) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(Color.black);
            g2.fillRect(0, 0, w, h);
        }

        if (curtain > 0) {
            g2.translate(-(1 - curtain) * w * 2, 0);
        }

        if (isTitleShown()) {
            g2.setFont(getTitleFont());
            g2.setColor(Color.white);
            g2.drawString(getTitle(), x, 40);
        }

        g2.setColor(getTheme().getColor(2));
        if (timer != null && !timer.interrupt && timer.time > 0) {
            g2.drawString(timer.time + "", w - 80, 40);
        }
    }

    class TimerThread extends Thread {
        public boolean interrupt = false;
        public int time = 0;


        /**
         * Constructor for the AnimateThread object
         *
         * @param t Description of the Parameter
         */
        public TimerThread(int t) {
            time = t;
        }


        public void run() {
            while (!interrupt) {
                try {
                    sleep(1000);
                } catch (Exception e) {
                }
                if (interrupt) {
                    break;
                }
                time--;
                if (time < 10) {
                    getTheme().playSample("bell.wav", false);
                }
                if (time < 1) {
                    if (currentScreen.getID().equals("start")) {
                        setContinue(false);
                        activatePlayer(-1);
                        SwingUtilities.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        repaint();
                                    }
                                });
                        break;
                    } else {
                        for (int t = 0; t < MAX_PLAYERS; t++) {
                            if (activePlayers[t]) {
                                keyPressed(SELECT[t]);
                            }
                        }
                        timerFinished();
                        break;
                    }
                }
                SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run() {
                                repaint();
                            }
                        });
            }
        }
    }

    class JukeboxTimer extends Thread {
        public boolean interrupt = false;
        public int time = 0;


        /**
         * Constructor for the AnimateThread object
         *
         * @param t Description of the Parameter
         */
        public JukeboxTimer(int t) {
            time = t;
        }


        public void run() {
            while (!interrupt) {
                try {
                    sleep(1000);
                } catch (Exception e) {
                }
                //System.out.println("juke " + time);
                if (interrupt) {
                    break;
                }
                time--;
                if (time < 1) {
                    if (!isJukeboxEnabled()) {
                        getTheme().interruptAll();
                        getTheme().playSample("credits.mp3", true);
                        enableJukebox(true);
                        nextJukebox();
                        time = JUKEBOX_TIME;
                    } else if (getCurrentJukebox().startsWith("jukebox")) {
                        time = JUKEBOX_TIME;
                    } else {
                        nextJukebox();
                        if (getCurrentJukebox().startsWith("jukebox")) {
                            getTheme().interruptAll();
                        } else if (!getTheme().isPlaying("credits.mp3")) {
                            getTheme().playSample("credits.mp3", true);
                        }
                        time = JUKEBOX_TIME;
                    }
                }
            }
        }
    }
}

