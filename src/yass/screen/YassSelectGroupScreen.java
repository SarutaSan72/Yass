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

package yass.screen;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassSelectGroupScreen extends YassScreen {
    private static final long serialVersionUID = 8767183574660292415L;
    private int itemCount = 0;
    private int selectedItem = 0;
    private int topItem = 0;
    private String selectedGroup = null;
    private boolean randomActive = false;
    private BufferedImage img = null;

    private Color borderColor = new Color(0, 124, 197);


    /**
     * Gets the iD attribute of the YassScoreScreen object
     *
     * @return The iD value
     */
    public String getID() {
        return "selectgroup";
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public String nextScreen() {
        return "selectsong";
    }


    /**
     * Description of the Method
     */
    public void init() {
        loadBackgroundImage("plain_background.jpg");
    }


    /**
     * Description of the Method
     */
    public void show() {
        loadBackgroundImage("plain_background.jpg");
        selectedItem = 0;

        itemCount = getGroupsBySorting().size() + 1;
        startTimer(60);
        setContinue(true);
    }


    /**
     * Description of the Method
     */
    public void hide() {
        stopTimer();
    }


    /**
     * Description of the Method
     *
     * @param key Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean keyPressed(int key) {
        if (randomActive) {
            return true;
        }

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
                if (selectedItem == itemCount - 1) {
                    randomActive = true;
                    new Thread() {
                        public void run() {
                            random();
                        }
                    }.start();
                    return true;
                }

                setSelectedGroup(selectedItem);
                getTheme().playSample("menu_selection.wav", false);
                gotoScreen("selectsong");
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
                if (selectedItem < itemCount - 1) {
                    YassScreenGroup sg = getGroupAt(selectedItem);
                    String s = getString("group_" + sg.getTitle() + "_", sg.getFilter());
                    img = getCover(s);
                }
                repaint();
            }
        }
        return true;
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
     *
     * @param g Description of the Parameter
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (itemCount < 1) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;

        int margin = getMargin();
        int w = getSize().width;
        int h = getSize().height;

        int wsize = Math.min(w, Math.max(500, w / 3));
        int hsize = 60;

        int x = w - wsize;
        int y = margin + 60;

        int h2 = h / 2;
        int hsongs = h2;

        int i = selectedItem;
        y = h2 / 2;

        g2.setColor(getTheme().getColor(3));
        g2.setStroke(getThickStroke());
        g2.drawRect(x - 10, y - 40, wsize + 10, hsize);

        g2.setFont(getTextFont());
        while (y < h2) {
            i = i % itemCount;
            if (i == itemCount - 1) {
                g2.drawString(getString("random"), x, y);
            } else {
                YassScreenGroup sg = getGroupAt(i);
                g2.setColor(getTheme().getColor(selectedItem == i ? 3 : 2));
                String s = getString("group_" + sg.getTitle() + "_", sg.getFilter());
                g2.drawString(s, x, y);

            }
            i++;
            y += hsize;
        }
        i = selectedItem - 1;
        y = h2 / 2 - hsize;
        while (y > margin) {
            i = i % itemCount;
            if (i < 0) {
                i += itemCount;
            }
            if (i == itemCount - 1) {
                g2.drawString(getString("random"), x, y);
            } else {
                YassScreenGroup sg = getGroupAt(i);
                g2.setColor(getTheme().getColor(selectedItem == i ? 3 : 2));
                g2.drawString(getString("group_" + sg.getTitle() + "_", sg.getFilter()), x, y);
            }
            i--;
            y -= hsize;
        }

        int imgsize = h2 - 10 - margin;
        if (img != null && imgsize > 20) {
            g2.drawImage(img, margin, h2 - 10 - imgsize, imgsize, imgsize, null);
            g2.setColor(Color.white);
            g2.setStroke(getThickStroke());
            g2.drawRect(margin, h2 - 10 - imgsize, imgsize, imgsize);
        }

        y = h2 + 10;

        g2.setColor(borderColor);
        g2.fillRect(0, y, w, 10);

        if (selectedItem == itemCount - 1) {
            return;
        }
        int colWidth = 240;
        int colHeight = 20;

        x = 10;
        y = h2 + 40;

        g2.setColor(getTheme().getColor(2));
        g2.setFont(getSubTextFont());
        YassScreenGroup sg = getGroupAt(selectedItem);
        Rectangle clip = g2.getClipBounds();
        int cx = clip.x;
        int cw = clip.width;
        int n = sg.getSongs().size();
        int colSize = (h - y) / colHeight;
        for (int k = 0; k < n; k++) {
            clip.x = x - 10;
            clip.width = k < n - colSize ? colWidth - 16 : w;
            g2.setClip(clip);
            g2.drawString(getSongDataAt(sg.getSongAt(k)).getTitle(), x, y);
            y += colHeight;
            if (x + 2 * colWidth >= w && y > h - colHeight * 2 - 10) {
                g2.drawString((n - k) + " more...", x, y);
                break;
            }
            if (y > h - colHeight - 10) {
                y = h2 + 40;
                x += colWidth;
            }
        }
        clip.x = cx;
        clip.width = cw;
        g2.setClip(clip);
    }
}

