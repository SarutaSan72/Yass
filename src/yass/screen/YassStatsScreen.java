package yass.screen;

import yass.renderer.YassSession;

import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 4. September 2006
 */
public class YassStatsScreen extends YassScreen {
    private static final long serialVersionUID = 7539603712150896010L;
    private Vector<YassScore> scores = null;
    private String scoreFormat = String.format("%%0%dd", 5);


    /**
     * Gets the iD attribute of the YassScoreScreen object
     *
     * @return The iD value
     */
    public String getID() {
        return "stats";
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
    public void init() {
    }


    /**
     * Description of the Method
     */
    public void show() {
        YassSession s = getSession();
        getDatabase().open();
        scores = getDatabase().getScore(s.getArtist(), s.getTitle());
        getDatabase().close();
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
        if (key == SELECT[0]) {
            getTheme().playSample("menu_selection.wav", false);
            gotoScreen("start");
            return true;
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

        int x = getMargin();
        int y = getMargin();

        g2.setFont(getTextFont());
        g2.setColor(getTheme().getColor(2));
        g2.drawString(getSession().getTitle(), x, y += 30);
        g2.drawString(getSession().getArtist(), x, y += 30);

        y += 30;

        int i = 0;
        if (scores != null) {
            g2.setFont(getTextFont());
            for (Enumeration<YassScore> en = scores.elements(); en.hasMoreElements() && i < 5; i++) {
                YassScore s = (YassScore) en.nextElement();

                y += 30;
                g2.drawString(s.getPlayer(), x, y);
                g2.drawString(String.format(scoreFormat, s.getScore()), 300, y);
                g2.drawString("(" + s.getDifficulty() + ")", 500, y);
            }
        }
    }
}

