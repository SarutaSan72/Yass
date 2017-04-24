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

import yass.renderer.YassSession;

import java.awt.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassHighScoreScreen extends YassScreen {
    private static final long serialVersionUID = -1638253187882261371L;
    private Vector<YassScore> scores = null;
    private Vector<YassScore> scores2 = null;
    private Vector<YassScore> scores3 = null;
    private String scoreFormat = String.format("%%0%dd", 5);
    private String title = null;
    private String beginner = null, standard = null, expert = null;
    private boolean current = false;


    /**
     * Gets the iD attribute of the YassScoreScreen object
     *
     * @return The iD value
     */
    public String getID() {
        return "highscore";
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public String nextScreen() {
        return "start";
    }


    /**
     * Description of the Method
     */
    public void show() {
        String param = getScreenParam();
        if (param == null || param.trim().length() < 1) {
            param = "current";
        }

        current = false;
        if (param.equals("current")) {
            current = true;
            YassSession s = getSession();
            if (s != null) {
                String artist = s.getArtist();
                String title = s.getTitle();
                if (artist != null && title != null) {
                    getDatabase().open();
                    scores = getDatabase().getScore(artist, title, 0);
                    scores2 = getDatabase().getScore(artist, title, 1);
                    scores3 = getDatabase().getScore(artist, title, 2);
                    getDatabase().close();
                }
            }
            title = "";

            scores = new Vector<>();
            scores2 = new Vector<>();
            scores3 = new Vector<>();
            scores.add(new YassScore("The Sprites", "Where The Fuck Is Denise?", "DUDE", 1000, 0));
            scores2.add(new YassScore("The Ataris", "Pet Cemetary", "DUDE", 2000, 0));
            scores3.add(new YassScore("David Joiner", "Faery Tales", "DUDE", 3000, 0));
            scores.add(new YassScore("The Sprites", "Where The Fuck Is Denise?", "DUDE", 1000, 1));
            scores2.add(new YassScore("The Ataris", "Pet Cemetary", "DUDE", 4000, 1));
            scores3.add(new YassScore("David Joiner", "Faery Tales", "DUDE", 2000, 1));
            scores.add(new YassScore("The Sprites", "Where The Fuck Is Denise?", "DUDE", 1000, 2));
            scores2.add(new YassScore("The Ataris", "Pet Cemetary", "DUDE", 3000, 2));
            scores3.add(new YassScore("David Joiner", "Faery Tales", "DUDE", 2000, 2));

            beginner = getString("beginner");
            standard = getString("standard");
            expert = getString("expert");
        } else if (param.equals("topten-beginner")) {
            title = getString("beginner");
            getDatabase().open();
            scores = getDatabase().getTop(10, 0);
            scores2 = scores3 = null;
            getDatabase().close();
        } else if (param.equals("topten-standard")) {
            title = getString("standard");
            getDatabase().open();
            scores = getDatabase().getTop(10, 1);
            scores2 = scores3 = null;
            getDatabase().close();
        } else if (param.equals("topten-expert")) {
            title = getString("expert");
            getDatabase().open();
            scores = getDatabase().getTop(10, 2);
            scores2 = scores3 = null;
            getDatabase().close();
        }

        if (!param.equals("current")) {
            scores.add(new YassScore("The Sprites", "Where The Fuck Is Denise?", "DUDE", 1000, 0));
            scores.add(new YassScore("The Ataris", "Pet Cemetary", "DUDE", 2000, 0));
            scores.add(new YassScore("David Joiner", "Faery Tales", "DUDE", 3000, 0));
            scores.add(new YassScore("L'n'K", "Computer Revolution", "DUDE", 3500, 0));
            scores.add(new YassScore("Florence", "If Thou Canst Not Dream", "DUDE", 4000, 0));
            scores.add(new YassScore("The Jaggies", "Nyquist Symphonies", "DUDE", 4500, 0));
            scores.add(new YassScore("Spice McCloud", "Soda Maker Song", "DUDE", 5000, 0));
            scores.add(new YassScore("Giana Sisters", "Great Christmas", "DUDE", 6000, 0));
            scores.add(new YassScore("The Teddies", "Magic Xanadu", "DUDE", 6500, 0));
            scores.add(new YassScore("Ma Galways", "Wizball", "DUDE", 7000, 0));
            Collections.sort(scores);
        } else {
            startTimer(20);
        }
    }


    /**
     * Description of the Method
     */
    public void hide() {
        scores = null;
    }


    /**
     * Description of the Method
     *
     * @param key Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean keyPressed(int key) {
        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (key == SELECT[t]) {
                getTheme().playSample("menu_selection.wav", false);
                gotoScreen("start");
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

        if (title == null || scores == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;

        int w = getSize().width;
        int h = getSize().height;
        int margin = getMargin();
        int x = margin;
        int y = margin;

        if (!current) {
            g2.setColor(Color.white);
            g2.setFont(getTitleFont());
            g2.drawString(title, 400, y - 40);
        }

        int w2 = w / 2;

        int dy = 35;

        Font f = getTextFont();
        g2.setFont(f);

        FontMetrics fm = g2.getFontMetrics();
        int vspace = 10 * dy;

        if (vspace < h - margin - 20) {
            y = (h - vspace) / 3;
        } else {
            float size = (h - margin - 10) / (9 + 5f);
            f = g2.getFont().deriveFont(Font.BOLD, size);
            g2.setFont(f);
            fm = g2.getFontMetrics();
            dy = (int) (size + 5);
            vspace = 10 * dy;
            if (vspace < h) {
                y = (h - vspace) / 3;
            } else {
                y = 10;
            }
        }

        g2.setColor(getTheme().getColor(2));
        if (!current) {
            int col1 = w2 - fm.stringWidth("MMMMM   MMMMM   XXXX");
            int col2 = w2 - fm.stringWidth("MMMMM   XXXX");
            int col3 = w2 - fm.stringWidth("XXXX");
            if (col1 < 10) {
                col2 = col2 - col1 + 10;
                col3 = col3 - col1 + 10;
                col1 = 10;
            }

            Font small = f.deriveFont(Font.BOLD, f.getSize() / 2);

            int i = 0;
            for (Enumeration<YassScore> en = scores.elements(); en.hasMoreElements() && i < 10; i++) {
                YassScore s = en.nextElement();

                if (i == 5) {
                    y += dy;
                }
                y += dy;

                g2.setColor(getTheme().getColor(i < 5 ? 3 : 2));
                g2.setFont(f);
                g2.drawString(s.getPlayer(), col1, y);
                g2.drawString(String.format(scoreFormat, s.getScore()), col2, y);
                g2.drawString(s.getTitle(), col3, y);
                int sw = fm.stringWidth(s.getTitle());
                g2.setFont(small);
                g2.drawString(s.getArtist(), col3 + 10 + sw, y);
            }
        } else {
            g2.setFont(f);

            int col1 = w2 - fm.stringWidth("SCORE XXXX   |   SCORE ");
            int col2 = w2 - fm.stringWidth("XXXX   |   SCORE ");
            int col3 = w2 - fm.stringWidth("SCORE ");
            int col4 = w2;
            int col5 = w2 + fm.stringWidth(" XXXX   |   ");
            int col6 = w2 + fm.stringWidth(" XXXX   |   SCORE ");

            int col12 = w2 - fm.stringWidth(" XXXX   |   SCORE ");
            int col34 = w2 - fm.stringWidth(" ");
            int col56 = w2 + fm.stringWidth("XXXX   |   SCORE");

            if (col1 < 10) {
                col2 = col2 - col1 + 10;
                col3 = col3 - col1 + 10;
                col4 = col4 - col1 + 10;
                col5 = col5 - col1 + 10;
                col6 = col6 - col1 + 10;
                col1 = 10;
            }

            y += dy;
            y += dy;
            g2.setColor(Color.white);
            g2.drawString(beginner, col12 - g2.getFontMetrics().stringWidth(beginner) / 2, y);
            g2.drawString(standard, col34 - g2.getFontMetrics().stringWidth(standard) / 2, y);
            g2.drawString(expert, col56 - g2.getFontMetrics().stringWidth(expert) / 2, y);

            int yy = y;
            int i = 0;
            for (Enumeration<YassScore> en = scores.elements(); en.hasMoreElements() && i < 10; i++) {
                YassScore s = en.nextElement();

                if (i == 5) {
                    yy += dy;
                }
                yy += dy;

                g2.setColor(getTheme().getColor(i < 5 ? 3 : 2));
                g2.drawString(s.getPlayer(), col1, yy);
                g2.drawString(String.format(scoreFormat, s.getScore()), col2, yy);
            }

            yy = y;
            i = 0;
            for (Enumeration<YassScore> en = scores2.elements(); en.hasMoreElements() && i < 10; i++) {
                YassScore s = en.nextElement();

                if (i == 5) {
                    yy += dy;
                }
                yy += dy;

                g2.setColor(getTheme().getColor(i < 5 ? 3 : 2));
                g2.drawString(s.getPlayer(), col3, yy);
                g2.drawString(String.format(scoreFormat, s.getScore()), col4, yy);
            }

            yy = y;
            i = 0;
            for (Enumeration<YassScore> en = scores3.elements(); en.hasMoreElements() && i < 10; i++) {
                YassScore s = en.nextElement();

                if (i == 5) {
                    yy += dy;
                }
                yy += dy;

                g2.setColor(getTheme().getColor(i < 5 ? 3 : 2));
                g2.drawString(s.getPlayer(), col5, yy);
                g2.drawString(String.format(scoreFormat, s.getScore()), col6, yy);
            }
        }
    }
}

