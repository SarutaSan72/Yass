/*
 * Yass - Karaoke Editor
 * Copyright (C) 2014 Saruta
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

package yass.screen;

import yass.YassInput;

import java.awt.*;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassSelectControllerScreen extends YassScreen {
    private static final long serialVersionUID = 4744949154347328403L;
    AnimateThread anim = null;
    private int components[][] = null;
    private String playerdevicetype[][] = new String[MAX_PLAYERS][19];
    private String playerdevicename[][] = new String[MAX_PLAYERS][19];
    private String playercomponent[][] = new String[MAX_PLAYERS][19];
    private int activeRow = 0;
    private int activeCol = -1;
    private int eventCol = -1, eventRow = -1;
    private long eventMillis = -1;

    /**
     * Gets the iD attribute of the YassScoreScreen object
     *
     * @return The iD value
     */
    public String getID() {
        return "selectcontrol";
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public String nextScreen() {
        return "selectdevice";
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
        setContinue(false);

        YassInput input = getInput();

        int dCount = input.getControllerCount();
        components = new int[dCount][];
        for (int d = 0; d < dCount; d++) {
            int cCount = input.getComponentCount(d);
            components[d] = new int[cCount];
            for (int c = 0; c < cCount; c++) {
                components[d][c] = 0;
            }
        }

        String pd = getProperties().getProperty("player" + 0 + "_dev_" + 0);
        boolean reset = pd == null;
        loadControllers(reset);

        activeRow = 0;
        activeCol = -1;

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
        getInput().startPoll();
    }


    /**
     * Gets the controllers attribute of the YassSelectControllerScreen object
     *
     * @param reset Description of the Parameter
     */
    public void loadControllers(boolean reset) {
        YassInput input = getInput();
        for (int t = 0; t < MAX_PLAYERS; t++) {
            for (int k = 0; k < 17; k++) {
                playerdevicetype[t][k] = "-";
                playerdevicename[t][k] = "-";
                playercomponent[t][k] = "-";

                if (!reset) {
                    String pd = getProperties().getProperty("player" + t + "_dev_" + k);
                    if (pd != null) {
                        int d = input.getControllerIndex(pd);
                        if (d >= 0) {
                            playerdevicename[t][k] = input.getControllerName(d);
                            playerdevicetype[t][k] = input.getControllerType(d);
                            String pc = getProperties().getProperty("player" + t + "_comp_" + k);
                            if (pc != null) {
                                int c = input.getComponentIndex(d, pc);
                                int cCount = input.getComponentCount(d);
                                if (c >= 0 && c < cCount) {
                                    playercomponent[t][k] = input.getComponentName(d, c);

                                }
                            }
                        }
                    }
                }
            }
        }

        if (reset) {
            int controllerCount = 0;
            for (int t = 0; t < MAX_PLAYERS; t++) {
                boolean missing = false;
                for (int k = 0; k < 5; k++) {
                    missing = playerdevicetype[t][k].equals("-") || playercomponent[t][k].equals("-");
                    if (missing) {
                        break;
                    }
                }
                if (missing) {
                    int d = YassInput.getButtonController(controllerCount);
                    controllerCount++;
                    if (d >= 0) {
                        for (int k = 0; k < 5; k++) {
                            int c = YassInput.getButton(d, k);
                            if (c >= 0) {
                                playerdevicename[t][k] = input.getControllerName(d);
                                playerdevicetype[t][k] = input.getControllerType(d);
                                playercomponent[t][k] = input.getComponentName(d, c);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Sets the difficulty attribute of the YassSelectDifficultyScreen object
     */
    public void storeControllers() {
        anim.interrupt = true;
        for (int t = 0; t < MAX_PLAYERS; t++) {
            for (int k = 0; k < 17; k++) {
                getProperties().setProperty("player" + t + "_dev_" + k, playerdevicename[t][k]);
                getProperties().setProperty("player" + t + "_comp_" + k, playercomponent[t][k]);
            }
        }
        getProperties().store();
    }


    /**
     * Description of the Method
     */
    public void map() {
        YassInput input = getInput();

        input.removeMaps();
        for (int t = 0; t < MAX_PLAYERS; t++) {
            for (int k = 0; k < 17; k++) {
                int n = 2 + t * 17 + k;
                input.mapTo(this, playerdevicename[t][k], playercomponent[t][k], t, 0, getKeyCode(n), getKeyLocation(n));
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param key Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean keyReleased(int key) {
        if (activeCol < 0) {
            eventCol = -1;
            eventRow = -1;
            repaint();
        }
        return true;
    }

    /**
     * Description of the Method
     *
     * @param key Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean keyPressed(int key) {
        if (key == ESCAPE) {
            getTheme().playSample("menu_selection.wav", false);
            repaint();

            gotoScreen("start");
            return true;
        }

        if (activeCol == -3) {
            if (eventCol < 0) {
                eventMillis = System.currentTimeMillis();
            }
            boolean exit = System.currentTimeMillis() - eventMillis > 1000;
            if (exit) {
                activeCol = -1;
                getTheme().playSample("menu_selection.wav", false);
                repaint();
                return true;
            }
            if (eventCol >= 0) {
                return true;
            }
            for (int t = 0; t < MAX_PLAYERS; t++) {
                if (key == LEFT[t]) {
                    eventCol = t;
                    eventRow = 3;
                }
                if (key == RIGHT[t]) {
                    eventCol = t;
                    eventRow = 1;
                }
                if (key == UP[t]) {
                    eventCol = t;
                    eventRow = 0;
                }
                if (key == DOWN[t]) {
                    eventCol = t;
                    eventRow = 2;
                }
                if (key == SELECT[t]) {
                    eventCol = t;
                    eventRow = 4;
                }
            }
            getTheme().playSample("menu_navigation.wav", false);
            repaint();
            return true;
        }
        if (eventCol >= 0) {
            return true;
        }

        YassInput input = getInput();
        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (key == LEFT[t]) {
                activeCol--;
                activeRow = 0;
                if (activeCol < -3) {
                    activeCol = 2 * MAX_PLAYERS - 1;
                }
                if (activeCol >= 0) {
                    activeRow = activeCol % 2 == 0 ? 0 : 5;
                }
                if (activeCol == -1) {
                    map();
                    input.startPoll();
                }

                getTheme().playSample("menu_navigation.wav", false);
                repaint();
                return true;
            }
            if (key == RIGHT[t]) {
                activeCol++;
                activeRow = 0;
                if (activeCol > 2 * MAX_PLAYERS - 1) {
                    activeCol = 0;
                }
                if (activeCol >= 0) {
                    activeRow = activeCol % 2 == 0 ? 0 : 5;
                }
                if (activeCol >= 0) {
                    input.stopPoll();
                }
                getTheme().playSample("menu_navigation.wav", false);
                repaint();
                return true;
            }
            if (key == UP[t]) {
                activeRow--;
                if (activeCol % 2 == 0) {
                    if (activeRow < 0) {
                        activeRow = 4;
                    }
                } else {
                    if (activeRow < 5) {
                        activeRow = 16;
                    }
                }

                getTheme().playSample("menu_navigation.wav", false);
                repaint();
                return true;
            }
            if (key == DOWN[t]) {
                activeRow++;
                if (activeCol % 2 == 0) {
                    if (activeRow > 4) {
                        activeRow = 0;
                    }
                } else {
                    if (activeRow > 16) {
                        activeRow = 5;
                    }
                }

                getTheme().playSample("menu_navigation.wav", false);
                repaint();
                return true;
            }
            if (key == SELECT[t]) {
                if (activeCol == -2) {
                    loadControllers(true);
                    map();
                    getTheme().playSample("menu_selection.wav", false);
                    repaint();
                    return true;
                } else if (activeCol == -1) {
                    storeControllers();
                    map();
                    getTheme().playSample("menu_selection.wav", false);
                    repaint();
                    gotoScreen("selectdevice");
                    return true;
                } else {
                    activeCol = -1;
                    getTheme().playSample("menu_navigation.wav", false);
                    repaint();
                    return true;
                }
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

        Font f = getSubTextFont();
        g2.setFont(f);
        g2.setStroke(getThinStroke());
        FontMetrics m = g2.getFontMetrics();

        int x = getMargin();
        int y = getMargin();
        int d = 28;

        int colWidth = 160;

        g2.setColor(getTheme().getColor(2));
        String s = getString("test");
        int sw = m.stringWidth(s);
        g2.drawString(s, x - 30 - sw / 2, y);
        s = getString("reset");
        sw = m.stringWidth(s);
        g2.drawString(s, x + 55 - sw / 2, y);
        s = getString("ok");
        sw = m.stringWidth(s);
        g2.drawString(s, x + 140 - sw / 2, y);

        g2.setColor(getTheme().getColor(2));

        for (int k = 0; k < 17; k++) {
            y += d;
            if (k == 5) {
                y += d;
            }
            s = getString("key_" + (k + 2));
            g2.drawString(s, x, y);
        }

        for (int t = 0; t < MAX_PLAYERS; t++) {
            g2.drawString(getTheme().getPlayerSymbol(t, true), getMargin() + 200 + colWidth / 3 + t * colWidth, getMargin());

            x = getMargin() + 200 + t * colWidth;
            y = getMargin();
            for (int k = 0; k < 17; k++) {
                y += d;
                if (k == 5) {
                    y += d;
                }
                String pd = playercomponent[t][k] + " | " + playerdevicetype[t][k].charAt(0);
                g2.drawString(pd, x, y);
            }
        }

        g2.setStroke(getThickStroke());
        g2.setColor(getTheme().getColor(1));

        x = getMargin() - 30;
        y = getMargin() + 5;
        g2.setColor(getTheme().getColor(activeCol == -3 ? 3 : 1));
        g2.drawRect(x - 30, y - d, 60, d);

        x = getMargin() + 55;
        g2.setColor(getTheme().getColor(activeCol == -2 ? 3 : 1));
        g2.drawRect(x - 45, y - d, 90, d);

        x = getMargin() + 140;
        g2.setColor(getTheme().getColor(activeCol == -1 ? 3 : 1));
        g2.drawRect(x - 30, y - d, 60, d);

        if (eventCol >= 0 && eventRow >= 0) {
            g2.setColor(getTheme().getColor(3));
            x = getMargin() + 200 + eventCol / 2 * colWidth - 10;
            y = getMargin() + eventRow * d + 5;
            g2.fillRect(x, y, colWidth - 10, d);
        }

        if (activeCol >= 0) {
            if (activeCol % 2 == 0) {
                x = getMargin() + 200 + activeCol / 2 * colWidth - 10;
                y = getMargin() + 5;
                g2.drawRect(x, y, colWidth - 10, d * 5);
            } else {
                x = getMargin() + 200 + activeCol / 2 * colWidth - 10;
                y = getMargin() + d * 6 + 5;
                g2.drawRect(x, y, colWidth - 10, d * 12);
            }
            int row = activeRow;
            if (row > 4) {
                row++;
            }
            y = getMargin() + row * d + 5;
            g2.setColor(getTheme().getColor(3));
            g2.drawRect(x, y, colWidth - 10, d);

            if (activeRow >= 0) {
                g2.setColor(getTheme().getColor(3));
                String type = playerdevicetype[activeCol / 2][activeRow];
                if (!type.equals("-")) {
                    g2.drawString(getString(type) + playerdevicename[activeCol / 2][activeRow], getMargin() + 400, getMargin() - 40);
                }
            }
        } else if (activeCol == -3) {
            g2.setColor(getTheme().getColor(3));
            g2.drawString("Hold any key to exit", getMargin() + 400, getMargin() - 40);
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
            YassInput input = getInput();

            while (!interrupt) {
                try {
                    sleep(30);
                } catch (Exception e) {
                }

                if (activeCol < 0) {
                    continue;
                }

                input.pollAllButtons();

                int dCount = input.getControllerCount();
                for (int d = 0; d < dCount && !interrupt; d++) {
                    int cCount = input.getComponentCount(d);
                    for (int c = 0; c < cCount && !interrupt; c++) {
                        int val = input.getButtonValue(d, c);
                        if (val != components[d][c]) {
                            if (val > components[d][c]) {
                                playerdevicetype[activeCol / 2][activeRow] = input.getControllerType(d);
                                playerdevicename[activeCol / 2][activeRow] = input.getControllerName(d);
                                playercomponent[activeCol / 2][activeRow] = input.getComponentName(d, c);

                                activeRow++;
                                if (activeCol % 2 == 0) {
                                    if (activeRow > 4) {
                                        activeRow = 0;
                                    }
                                } else {
                                    if (activeRow > 16) {
                                        activeRow = 5;
                                    }
                                }
                            }
                            components[d][c] = val;
                        }
                    }
                }
                repaint();
            }
        }
    }
}

