package yass.screen;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 4. September 2006
 */
public class YassSelectDifficultyScreen extends YassScreen {
    private static final long serialVersionUID = 6577689808374808248L;
    private int player[] = new int[MAX_PLAYERS];
    private boolean selected[] = new boolean[MAX_PLAYERS];


    /**
     * Gets the iD attribute of the YassScoreScreen object
     *
     * @return The iD value
     */
    public String getID() {
        return "selectdifficulty";
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public String nextScreen() {
        return "selectsorting";
    }


    /**
     * Description of the Method
     */
    public void show() {
        loadDifficulty();

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
    public void loadDifficulty() {
        for (int t = 0; t < MAX_PLAYERS; t++) {
            selected[t] = false;
            String s = getProperties().getProperty("player" + t + "_difficulty");
            if (s == null) {
                s = "1";
            }
            int level = new Integer(s).intValue();
            if (level == 2) {
                player[t] = 0;
            } else if (level == 0) {
                player[t] = 2;
            } else {
                player[t] = 1;
            }
        }
    }


    /**
     * Sets the difficulty attribute of the YassSelectDifficultyScreen object
     */
    public void storeDifficulty() {
        for (int t = 0; t < MAX_PLAYERS; t++) {
            int level = player[t];
            if (level == 0) {
                level = 2;
            } else if (level == 2) {
                level = 0;
            } else {
                level = 1;
            }
            getProperties().put("player1_difficulty", level + "");
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
            if (selected[t] && (key == UP[t] || key == LEFT[t] || key == DOWN[t] || key == RIGHT[t] || key == SELECT[t])) {
                return true;
            }

            if (key == UP[t] || key == LEFT[t]) {
                player[t]--;
                if (player[t] < 0) {
                    player[t] = 0;
                }
                getTheme().playSample("menu_navigation.wav", false);
                repaint();
                return true;
            }
            if (key == DOWN[t] || key == RIGHT[t]) {
                player[t]++;
                if (player[t] > 2) {
                    player[t] = 2;
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

                storeDifficulty();
                gotoScreen(nextScreen());
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

        int x = w / 4;
        int y = h / 2;

        String s = getString("beginner");
        boolean sel = false;
        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (isPlayerActive(t) && player[t] == 0) {
                sel = true;
                break;
            }
        }
        if (sel) {
            int sw = m.stringWidth(s);
            g2.setColor(getTheme().getColor(3));
            g2.drawString(s, x - sw / 2, y);
        } else {
            TextLayout txtLayout = new TextLayout(s, f, g2.getFontRenderContext());
            float sw = (float) txtLayout.getBounds().getWidth();
            AffineTransform transform = new AffineTransform();
            transform.setToTranslation(x - sw / 2, y);
            g2.setColor(getTheme().getColor(2));
            g2.draw(txtLayout.getOutline(transform));
        }
        int d = 0;
        int dc = 0;
        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (isPlayerActive(t) && player[t] == 0) {
                dc++;
            }
        }
        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (isPlayerActive(t) && player[t] == 0) {
                g.drawString(getTheme().getPlayerSymbol(t, selected[t]), x - dc * 15 + (d++) * 30, y + 30);
            }
        }

        x = w / 2;
        s = getString("standard");
        sel = false;
        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (isPlayerActive(t) && player[t] == 1) {
                sel = true;
                break;
            }
        }
        if (sel) {
            int sw = m.stringWidth(s);
            g2.setColor(getTheme().getColor(3));
            g2.drawString(s, x - sw / 2, y);
        } else {
            TextLayout txtLayout = new TextLayout(s, f, g2.getFontRenderContext());
            float sw = (float) txtLayout.getBounds().getWidth();
            AffineTransform transform = new AffineTransform();
            transform.setToTranslation(x - sw / 2, y);
            g2.setColor(getTheme().getColor(2));
            g2.draw(txtLayout.getOutline(transform));
        }
        d = 0;
        dc = 0;
        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (isPlayerActive(t) && player[t] == 1) {
                dc++;
            }
        }
        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (isPlayerActive(t) && player[t] == 1) {
                g.drawString(getTheme().getPlayerSymbol(t, selected[t]), x - dc * 15 + (d++) * 30, y + 30);
            }
        }

        x = w * 3 / 4;
        s = getString("expert");
        sel = false;
        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (isPlayerActive(t) && player[t] == 2) {
                sel = true;
                break;
            }
        }
        if (sel) {
            int sw = m.stringWidth(s);
            g2.setColor(getTheme().getColor(3));
            g2.drawString(s, x - sw / 2, y);
        } else {
            TextLayout txtLayout = new TextLayout(s, f, g2.getFontRenderContext());
            float sw = (float) txtLayout.getBounds().getWidth();
            AffineTransform transform = new AffineTransform();
            transform.setToTranslation(x - sw / 2, y);
            g2.setColor(getTheme().getColor(2));
            g2.draw(txtLayout.getOutline(transform));
        }
        d = 0;
        dc = 0;
        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (isPlayerActive(t) && player[t] == 2) {
                dc++;
            }
        }
        for (int t = 0; t < MAX_PLAYERS; t++) {
            if (isPlayerActive(t) && player[t] == 2) {
                g.drawString(getTheme().getPlayerSymbol(t, selected[t]), x - dc * 15 + (d++) * 30, y + 30);
            }
        }
    }
}

