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

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassEnterScoreScreen extends YassScreen {
    private static final long serialVersionUID = -3014456581653940630L;
    private char c[][] = new char[MAX_PLAYERS][4];
    private int ystep = 0;
    private int selectedItem[] = null;
    private AnimateThread anim = null;
    private boolean reverse[] = null;
    private int dirChanged[] = null;
    private boolean selectPressed[] = null;
    private boolean newEntry[] = null;
    private int trackCount = 0;


    /**
     * Gets the iD attribute of the YassScoreScreen object
     *
     * @return The iD value
     */
    public String getID() {
        return "enterscore";
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public String nextScreen() {
        return "highscore";
    }


    /**
     * Description of the Method
     */
    public void show() {
        getDatabase().open();

        loadBackgroundImage("plain_background.jpg");
        getTheme().loadSample("score.mp3");
        getTheme().playSample("score.mp3", true);

        trackCount = getSession().getTrackCount();
        selectedItem = new int[trackCount];
        newEntry = new boolean[trackCount];
        reverse = new boolean[trackCount];
        dirChanged = new int[trackCount];
        selectPressed = new boolean[trackCount];
        for (int t = 0; t < trackCount; t++) {
            selectedItem[t] = 0;
            reverse[t] = false;
            newEntry[t] = false;
            dirChanged[t] = 0;
            selectPressed[t] = false;
            c[t][0] = 'W';
            c[t][1] = 'X';
            c[t][2] = 'Y';
            c[t][3] = 'Z';
        }

        for (int t = 0; t < trackCount; t++) {
            Vector scores = getDatabase().getScore(getSession().getArtist(), getSession().getTitle(), "000" + t);
            newEntry[t] = scores != null && !scores.isEmpty();
        }
        startTimer(30);

        anim = new AnimateThread();
        anim.start();
    }


    /**
     * Description of the Method
     */
    public void hide() {
        getDatabase().close();
        getTheme().unloadSample("score.mp3");
        anim.interrupt = true;
        stopTimer();
    }


    /**
     * Description of the Method
     *
     * @param t Description of the Parameter
     */
    public void renameScore(int t) {
        String player = "" + c[0] + c[1] + c[2] + c[3];
        getDatabase().renamePlayer("000" + t, player);
    }


    /**
     * Description of the Method
     *
     * @param key Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean keyPressed(int key) {
        for (int t = 0; t < trackCount; t++) {
            if (!newEntry[t]) {
                continue;
            }
            if (key == UP[t] || key == LEFT[t]) {
                dirChanged[t] = 2;
                return true;
            }
            if (key == DOWN[t] || key == RIGHT[t]) {
                dirChanged[t] = 1;
                return true;
            }
            if (key == SELECT[t]) {
                getTheme().playSample("menu_selection.wav", false);

                if (!selectPressed[t] && selectedItem[t] >= 4) {
                    for (int k = 0; k < trackCount; k++) {
                        if (isPlayerActive(k) && newEntry[k] && selectedItem[k] < 4) {
                            return true;
                        }
                    }
                    timerFinished();
                } else {
                    selectPressed[t] = true;
                }
                return true;
            }
        }
        return false;
    }


    /**
     * Description of the Method
     */
    public void timerFinished() {
        for (int t = 0; t < trackCount; t++) {
            if (isPlayerActive(t) && newEntry[t] && selectedItem[t] < 4) {
                renameScore(t);
            }
        }
        gotoScreen("highscore");
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
        int margin = getMargin();

        Font f = getBigFont();
        g2.setFont(f);
        g2.setStroke(getStandardStroke());
        FontMetrics m = g2.getFontMetrics();

        int charWidth = 60;
        int charHeight = 69;
        int cellHeight = 2 * charHeight + m.getAscent() + 10;
        int y = margin + 30;

        Rectangle clip = g2.getClipBounds();
        int cx = clip.x;
        int cy = clip.y;
        int cw = clip.width;
        int ch = clip.height;

        int x = 0;
        int xspace = Math.max(margin, (w - (2 * margin) - 4 * charWidth) / 2);
        int yspace = margin + cellHeight / 2;

        int n = 0;
        for (int t = 0; t < trackCount; t++) {
            if (!newEntry[t] || !isPlayerActive(t)) {
                continue;
            }

            x = xspace;
            y = yspace + n * (cellHeight + 20);
            n++;

            clip.x = x;
            clip.y = y - charHeight - m.getAscent() + 20;
            clip.width = 6 * charWidth;
            clip.height = cellHeight - 40;
            g2.setClip(clip);

            g2.drawString(getTheme().getPlayerSymbol(t, false), x, y);

            Color col = getTheme().getColor(3);
            col = new Color(col.getRed(), col.getGreen(), col.getBlue(), 0);
            GradientPaint gradientBackground = new GradientPaint(0, clip.y, col, 0, clip.y + clip.height / 2, Color.white, true);
            TextLayout tl = new TextLayout("W", f, g2.getFontRenderContext());
            int sww = (int) tl.getBounds().getWidth();

            for (int i = 0; i < 4; i++) {
                x = xspace + (i + 2) * charWidth;

                if (selectedItem[t] == i) {
                    g2.setPaint(gradientBackground);
                    //g2.setStroke(getThinStroke());
                    //g2.drawRect(clip.x + 1, clip.y + charHeight + 1, clip.width - 2, charHeight - 2);
                    g2.fillRect(x - sww / 2 - 2, clip.y, sww + 8, clip.height - 1);
                    g2.setColor(getTheme().getColor(0));
                    g2.setStroke(getThickStroke());
                    g2.drawRect(x - sww / 2 - 2, clip.y - 5, sww + 8, clip.height + 10);
                    g2.setStroke(getStandardStroke());

                    int step = reverse[t] ? (-ystep) : ystep;

                    char prev2 = c[t][i];
                    if (--prev2 < 65) {
                        prev2 = 90;
                    }
                    if (--prev2 < 65) {
                        prev2 = 90;
                    }
                    String s = prev2 + "";
                    TextLayout txtLayout = new TextLayout(s, f, g2.getFontRenderContext());
                    float sw = (float) txtLayout.getBounds().getWidth();
                    AffineTransform transform = new AffineTransform();
                    transform.setToTranslation(x - sw / 2, y - charHeight - charHeight + step);
                    g2.setColor(getTheme().getColor(2));
                    g2.draw(txtLayout.getOutline(transform));

                    char prev = c[t][i];
                    if (--prev < 65) {
                        prev = 90;
                    }
                    s = prev + "";
                    txtLayout = new TextLayout(s, f, g2.getFontRenderContext());
                    sw = (float) txtLayout.getBounds().getWidth();
                    transform.setToTranslation(x - sw / 2, y - charHeight + step);
                    g2.setColor(getTheme().getColor(2));
                    g2.draw(txtLayout.getOutline(transform));

                    s = c[t][i] + "";
                    txtLayout = new TextLayout(s, f, g2.getFontRenderContext());
                    sw = (float) txtLayout.getBounds().getWidth();
                    transform.setToTranslation(x - sw / 2, y + step);
                    g2.setColor(getTheme().getColor(2));
                    g2.draw(txtLayout.getOutline(transform));

                    char next = c[t][i];
                    if (++next > 90) {
                        next = 65;
                    }
                    s = next + "";
                    txtLayout = new TextLayout(s, f, g2.getFontRenderContext());
                    sw = (float) txtLayout.getBounds().getWidth();
                    transform.setToTranslation(x - sw / 2, y + charHeight + step);
                    g2.setColor(getTheme().getColor(2));
                    g2.draw(txtLayout.getOutline(transform));

                    if (++next > 90) {
                        next = 65;
                    }
                    s = next + "";
                    txtLayout = new TextLayout(s, f, g2.getFontRenderContext());
                    sw = (float) txtLayout.getBounds().getWidth();
                    transform.setToTranslation(x - sw / 2, y + charHeight + charHeight + step);
                    g2.draw(txtLayout.getOutline(transform));

                    if (++next > 90) {
                        next = 65;
                    }
                    s = next + "";
                    txtLayout = new TextLayout(s, f, g2.getFontRenderContext());
                    sw = (float) txtLayout.getBounds().getWidth();
                    transform.setToTranslation(x - sw / 2, y + charHeight + charHeight + charHeight + step);
                    g2.draw(txtLayout.getOutline(transform));
                } else {
                    String s = c[t][i] + "";
                    int sw = m.stringWidth(s);
                    g2.setColor(getTheme().getColor(2));
                    g2.drawString(s, x - sw / 2, y);
                }
            }
        }

        clip.x = cx;
        clip.y = cy;
        clip.width = cw;
        clip.height = ch;
        g2.setClip(clip);
    }

    class AnimateThread extends Thread {
        public boolean interrupt = false;


        /**
         * Constructor for the AnimateThread object
         */
        public AnimateThread() {
        }


        public void run() {
            while (!interrupt) {
                try {
                    sleep(100);
                } catch (Exception e) {
                }
                getTheme().playSample("menu_navigation.wav", false);

                for (int t = 0; t < trackCount; t++) {
                    if (selectPressed[t]) {
                        selectPressed[t] = false;
                        if (selectedItem[t] < 4) {
                            selectedItem[t]++;
                        }
                    }
                }

                boolean finished = true;
                for (int t = 0; t < trackCount; t++) {
                    if (!newEntry[t]) {
                        continue;
                    }
                    if (isPlayerActive(t) && selectedItem[t] < 4) {
                        finished = false;
                        break;
                    }
                }
                if (finished) {
                    SwingUtilities.invokeLater(
                            new Runnable() {
                                public void run() {
                                    repaint();
                                }
                            });
                    break;
                }

                for (int t = 0; t < trackCount; t++) {
                    if (!newEntry[t]) {
                        continue;
                    }
                    if (!isPlayerActive(t) || selectedItem[t] >= 4) {
                        continue;
                    }
                    if (dirChanged[t] != 0) {
                        reverse[t] = dirChanged[t] == 1;
                        dirChanged[t] = 0;
                    }
                    if (c[t][selectedItem[t]] == 90 && !reverse[t]) {
                        c[t][selectedItem[t]] = 65;
                    } else if (c[t][selectedItem[t]] == 65 && reverse[t]) {
                        c[t][selectedItem[t]] = 90;
                    } else if (!reverse[t]) {
                        c[t][selectedItem[t]]++;
                    } else {
                        c[t][selectedItem[t]]--;
                    }
                }

                for (ystep = 69; ystep >= 0; ystep -= 3) {
                    SwingUtilities.invokeLater(
                            new Runnable() {
                                public void run() {
                                    repaint();
                                }
                            });
                    try {
                        sleep(20);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }
}

