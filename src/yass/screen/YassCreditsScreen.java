package yass.screen;

import java.awt.*;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 4. September 2006
 */
public class YassCreditsScreen extends YassScreen {
    private static final long serialVersionUID = -1719073108885658488L;
    private String[] lines = null;
    private String param = null;


    /**
     * Gets the iD attribute of the YassScoreScreen object
     *
     * @return The iD value
     */
    public String getID() {
        return "credits";
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
        param = getScreenParam();

        if (param.equals("yass")) {
            lines = getString("yass").split("\\|");
        } else if (param.equals("thirdparty")) {
            lines = getString("thirdparty").split("\\|");
        } else if (param.equals("thanks")) {
            lines = getString("thanks").split("\\|");
        }
        setTitleShown(false);
    }


    /**
     * Description of the Method
     */
    public void hide() {
    }


    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        boolean thirdparty = param.equals("thirdparty");

        int w = getSize().width;
        int h = getSize().height;
        int x = getMargin();
        int y = thirdparty ? getMargin() - 30 : getMargin();

        int dy = thirdparty ? 25 : 40;

        g2.setFont(thirdparty ? getSubTextFont() : getTextFont());
        FontMetrics fm = g2.getFontMetrics();
        int vspace = (lines.length - 1) * dy;

        if (vspace < h - 20) {
            y = (h - vspace) / 2;
        } else {
            float size = h / (lines.length + 5f);
            Font f = g2.getFont().deriveFont(size);
            g2.setFont(f);
            fm = g2.getFontMetrics();
            dy = (int) (size + 5);
            vspace = lines.length * dy;
            if (vspace < h) {
                y = (h - vspace) / 2;
            } else {
                y = 10;
            }
        }

        g2.setColor(getTheme().getColor(2));
        if (lines != null) {
            for (int i = 0; i < lines.length; i++) {
                String s = lines[i];
                int sw = fm.stringWidth(s);
                g2.drawString(s, w / 2 - sw / 2, y);
                y += dy;
            }
        }
    }
}

