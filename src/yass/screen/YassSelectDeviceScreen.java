package yass.screen;

import yass.YassCaptureAudio;
import yass.renderer.YassPlayerNote;

import java.awt.*;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 4. September 2006
 */
public class YassSelectDeviceScreen extends YassScreen {
    private static final long serialVersionUID = -2240042534249987502L;
    private YassCaptureAudio capture = null;
    private String devices[] = null;
    private int channels[] = null;
    private int channelActive[] = null;

    private int playerdevice[] = new int[MAX_PLAYERS];
    private int playerchannel[] = new int[MAX_PLAYERS];
    private boolean selected[] = new boolean[MAX_PLAYERS];

    private AnimateThread anim = null;


    /**
     * Gets the iD attribute of the YassScoreScreen object
     *
     * @return The iD value
     */
    public String getID() {
        return "selectdevice";
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public String nextScreen() {
        return "selectdifficulty";
    }


    /**
     * Description of the Method
     */
    public void init() {
        loadBackgroundImage("plain_background.jpg");
        capture = new YassCaptureAudio();
    }


    /**
     * Description of the Method
     */
    public void show() {
        loadBackgroundImage("plain_background.jpg");

        loadDevices();

        anim = new AnimateThread();
        anim.start();

    }


    /**
     * Description of the Method
     */
    public void hide() {
        if (anim != null) {
            anim.interrupt = true;
        }
    }


    /**
     * Description of the Method
     */
    public void loadDevices() {
        devices = capture.getDeviceNames();
        channels = new int[devices.length];
        channelActive = new int[devices.length * 2];
        for (int i = 0; i < devices.length; i++) {
            channelActive[2 * i] = -1000;
            channelActive[2 * i + 1] = -1000;
        }

        for (int t = 0; t < MAX_PLAYERS; t++) {
            playerdevice[t] = -1;
            playerchannel[t] = 0;
            selected[t] = false;

            String name = getProperties().getProperty("player" + t + "_device");
            String channel = getProperties().getProperty("player" + t + "_channel");
            if (channel != null) {
                int c = Integer.parseInt(channel);
                if (name != null) {
                    for (int i = 0; i < devices.length; i++) {
                        if (name.equals(devices[i])) {
                            playerdevice[t] = i;
                            playerchannel[t] = Math.min(c, capture.getChannels(devices[i]) - 1);
                            break;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < devices.length; i++) {
            channels[i] = capture.getChannels(devices[i]);
            if (devices[i].indexOf("USBMIC Serial#") >= 0) {
                for (int t = 0; t < MAX_PLAYERS; t++) {
                    if (isPlayerActive(t) && playerdevice[t] == -1) {
                        playerdevice[t] = i;
                        playerchannel[t] = 0;
                        break;
                    }
                }
                for (int t = 0; t < MAX_PLAYERS; t++) {
                    if (isPlayerActive(t) && playerdevice[t] == -1) {
                        playerdevice[t] = i;
                        playerchannel[t] = 1;
                        break;
                    }
                }
            }
        }

        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (playerdevice[t] == -1) {
                playerdevice[t] = 0;
                playerchannel[t] = t % 2;
            }
        }
    }


    /**
     * Sets the difficulty attribute of the YassSelectDifficultyScreen object
     */
    public void storeDevices() {
        for (int t = 0; t < MAX_PLAYERS; t++) {
            getProperties().put("player" + t + "_device", devices[playerdevice[t]]);
            getProperties().put("player" + t + "_channel", playerchannel[t] + "");
        }
        getProperties().store();
    }

    /**
     * Description of the Method
     *
     * @param key Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean keyPressed(int key) {
        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (selected[t]) {
                continue;
            }
            if (key == UP[t] || key == LEFT[t]) {
                if (playerchannel[t] > 0) {
                    playerchannel[t]--;
                } else {
                    playerdevice[t]--;
                    if (playerdevice[t] < 0) {
                        playerdevice[t] = devices.length - 1;
                    }
                    playerchannel[t] = channels[playerdevice[t]] - 1;
                }
                getTheme().playSample("menu_navigation.wav", false);
                repaint();
                return true;
            }
            if (key == DOWN[t] || key == RIGHT[t]) {
                if (playerchannel[t] < channels[playerdevice[t]] - 1) {
                    playerchannel[t]++;
                } else {
                    playerdevice[t]++;
                    if (playerdevice[t] > devices.length - 1) {
                        playerdevice[t] = 0;
                    }
                    playerchannel[t] = 0;
                }
                getTheme().playSample("menu_navigation.wav", false);
                repaint();
                return true;
            }
            if (key == SELECT[t]) {
                selected[t] = true;
                getTheme().playSample("menu_selection.wav", false);
                repaint();

                for (int t2 = 0; t2 < MAX_PLAYERS; t2++) {
                    if (isPlayerActive(t2) && !selected[t2]) {
                        return true;
                    }
                }
                storeDevices();
                gotoScreen("selectdifficulty");
                return true;
            }
        }
        return false;
    }

    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        int w = getSize().width;
        int h = getSize().height;

        Font f = getTextFont();
        g2.setFont(f);
        g2.setStroke(getThinStroke());
        FontMetrics m = g2.getFontMetrics();

        int devw = (w - 40) / devices.length;
        int x = getMargin() + 80;
        int y = getMargin();
        int d = 20;

        g2.setColor(getTheme().getColor(2));
        for (int i = 0; i < devices.length; i++) {
            y += 40;
            String s = devices[i];
            g2.drawString(s, x, y);

            String lr = "\u25CB";
            String lr2 = "\u25EF";
            if (channels[i] == 1) {
                int val = channelActive[i * 2];
                if (val >= 0) {
                    g2.setColor(new Color(51, 153, 255));
                    g2.drawString(lr2, x - 70 - 2, y);
                }
                g2.setColor(getTheme().getColor(2));
                g2.drawString(lr, x - 70, y);

            } else {
                int val = channelActive[i * 2];
                if (val >= 0) {
                    g2.setColor(new Color(51, 153, 255));
                    g2.drawString(lr2, x - 70 - d - 2, y);
                }
                g2.setColor(getTheme().getColor(2));
                g2.drawString(lr, x - 70 - d, y);

                val = channelActive[i * 2 + 1];
                if (val >= 0) {
                    g2.setColor(new Color(255, 153, 153));
                    g2.drawString(lr2, x - 70 + d - 2, y);
                }
                g2.setColor(getTheme().getColor(2));
                g2.drawString(lr, x - 70 + d, y);

            }
        }

        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (isPlayerActive(t)) {
                x = getMargin() + 80 - 70 - d + playerchannel[t] * d * 2;
                y = getMargin() + (playerdevice[t] + 1) * 40;
                g2.drawString(getTheme().getPlayerSymbol(t, selected[t]), x, y);
            }
        }
    }

    class AnimateThread extends Thread {
        public boolean interrupt = false;


        /**
         * Constructor for the AnimateThread object
         */
        public AnimateThread() {
        }


        public void run() {
            for (int i = 0; i < devices.length; i++) {
                capture.startQuery(devices[i]);
            }
            while (!interrupt) {
                try {
                    sleep(10);
                } catch (Exception e) {
                }

                for (int i = 0; i < devices.length; i++) {
                    YassPlayerNote note[] = capture.query(devices[i]);
                    if (note == null) {
                        continue;
                    }
                    channelActive[2 * i] = note[0].isNoise() ? -1000 : note[0].getHeight();
                    channelActive[2 * i + 1] = note[1].isNoise() ? -1000 : note[1].getHeight();
                }
                repaint();
            }
            for (int i = 0; i < devices.length; i++) {
                capture.stopQuery(devices[i]);
            }
        }
    }
}

