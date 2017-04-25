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
import java.util.Enumeration;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassSelectSortingScreen extends YassScreen {
    private static final long serialVersionUID = 7698221934739876675L;
    private int itemCount = 0;
    private int selectedItem = 0;


    /**
     * Gets the iD attribute of the YassScoreScreen object
     *
     * @return The iD value
     */
    public String getID() {
        return "selectsorting";
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public String nextScreen() {
        return "selectgroup";
    }


    /**
     * Description of the Method
     */
    public void show() {
        itemCount = getGroupsData().size();
        loadSorting();
        startTimer(20);
    }


    /**
     * Description of the Method
     */
    public void hide() {
        stopTimer();
    }


    /**
     * Description of the Method
     */
    public void loadSorting() {
        String s = getProperties().getProperty("game_sorting");
        if (s == null) {
            return;
        }

        int i = 0;
        selectedItem = 0;
        for (Enumeration<Vector<YassScreenGroup>> en = (Enumeration<Vector<YassScreenGroup>>) getGroupsData().elements(); en.hasMoreElements(); ) {
            Vector<YassScreenGroup> groups = en.nextElement();
            String title = groups.elementAt(0).getTitle();
            if (title.equals(s)) {
                selectedItem = i;
                break;
            }
        }
        setSelectedSorting(selectedItem);
    }


    /**
     * Description of the Method
     */
    public void storeSorting() {
        setSelectedSorting(selectedItem);
        String s = getSortingAt(selectedItem);
        getProperties().setProperty("game_sorting", s);
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
                repaint();
                getTheme().playSample("songs_navigation.wav", false);
                return true;
            }
            if (key == SELECT[t]) {
                storeSorting();
                getTheme().playSample("menu_selection.wav", false);
                gotoScreen("selectgroup");
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

        if (itemCount < 1) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;

        int margin = getMargin();
        int w = getSize().width;
        int h = getSize().height;

        int wsize = (int) (w * 1 / 3.0);
        int hsize = 60;

        int x = w - wsize;
        int y = margin + 60;

        int h2 = h / 2;

        int i = selectedItem;
        y = h2 / 2;

        g2.setColor(getTheme().getColor(3));
        g2.setStroke(getThickStroke());
        g2.drawRect(x - 10, y - 40, wsize + 10, hsize);

        while (y < h2) {
            i = i % itemCount;
            g2.setColor(getTheme().getColor(selectedItem == i ? 3 : 2));
            g2.setFont(getTextFont());
            g2.drawString(getString("", "group_" + getSortingAt(i) + "_title"), x, y);
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
            g2.setColor(getTheme().getColor(selectedItem == i ? 3 : 2));
            g2.setFont(getTextFont());
            g2.drawString(getString("", "group_" + getSortingAt(i) + "_title"), x, y);
            i--;
            y -= hsize;
        }
    }
}

