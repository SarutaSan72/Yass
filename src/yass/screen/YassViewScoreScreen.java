package yass.screen;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Enumeration;
import java.util.Vector;

import yass.renderer.YassSession;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    4. September 2006
 */
public class YassViewScoreScreen extends YassScreen {
	private static final long serialVersionUID = -6608705150473202127L;

	private String scoreFormat = String.format("%%0%dd", 5);

	private double angle[] = null;
	private int playerRank[] = null;
	private float progress = 0;


	/**
	 *  Gets the iD attribute of the YassScoreScreen object
	 *
	 * @return    The iD value
	 */
	public String getID() {
		return "viewscore";
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public String nextScreen() {
		return "highscore";
	}


	/**
	 *  Description of the Method
	 */
	public void init() {
	}


	/**
	 *  Description of the Method
	 */
	public void show() {
		loadBackgroundImage("plain_background.jpg");

		YassSession session = getSession();
		int trackCount = session.getTrackCount();

		getDatabase().open();
		angle = new double[trackCount];
		playerRank = new int[trackCount];
		for (int t = 0; t < trackCount; t++) {
			if (!isPlayerActive(t)) {
				continue;
			}

			int playerScore = session.getTrack(t).getPlayerScore();
			String artist = session.getArtist();
			String title = session.getTitle();
			int difficulty = session.getTrack(t).getDifficulty();

			// debug
			playerScore = 5555 + t;

			if (playerScore >= 1000) {
				getDatabase().insertScore(artist, title, "000" + t, playerScore, difficulty);
			}
			playerRank[t] = 0;
			angle[t] = -1000;
		}

		int i = 1;
		Vector<YassScore> scores = getDatabase().getScore(session.getArtist(), session.getTitle());
		if (scores != null) {
			for (Enumeration<YassScore> en = scores.elements(); en.hasMoreElements() && i <= 5; i++) {
				YassScore s = (YassScore) en.nextElement();
				int score = s.getScore();
				String name = s.getPlayer();
				System.out.println(name + "/" + score);
				if (name.startsWith("000")) {
					int t = new Integer(name.substring(3, 4)).intValue();
					System.out.println("index " + t);
					playerRank[t] = i;
				}
			}
		}

		animate();
		startTimer(25);
	}


	/**
	 *  Description of the Method
	 */
	public void hide() {
		getDatabase().close();
		stopTimer();
		getTheme().unloadSample("applause_1.mp3");
		getTheme().unloadSample("applause_2.mp3");
		getTheme().unloadSample("applause_3.mp3");
	}


	/**
	 *  Description of the Method
	 *
	 * @param  key  Description of the Parameter
	 * @return      Description of the Return Value
	 */
	public boolean keyPressed(int key) {
		int trackCount = getSession().getTrackCount();
		for (int t = 0; t < trackCount; t++) {
			if (key == SELECT[t]) {
				getTheme().playSample("menu_selection.wav", false);
				for (int k = 0; k < trackCount; k++) {
					if (playerRank[k] > 0 && playerRank[k] < 6) {
						gotoScreen("enterscore");
						return true;
					}
				}
				gotoScreen("highscore");
				return true;
			}
		}
		return false;
	}


	/**
	 *  Description of the Method
	 */
	public void animate() {
		System.out.println("animate");
		new AnimateThread().start();
	}


	class AnimateThread extends Thread {
		/**
		 *  Constructor for the AnimateThread object
		 */
		public AnimateThread() { }


		public void run() {
			int maxScoreSeconds = -1;
			int trackCount = getSession().getTrackCount();
			for (int t = 0; t < trackCount; t++) {
				int trackScore[] = getSession().getTrack(t).getScore();
				if (trackScore != null) {
					maxScoreSeconds = Math.max(maxScoreSeconds, trackScore.length);
				}
			}

			int applause = 3;
			for (int t = 0; t < trackCount; t++) {
				angle[t] = 220;

				if (!isPlayerActive(t)) {
					continue;
				}
				if (playerRank[t] > 0 && playerRank[t] < 6) {
					applause = 1;
				}
				if (applause == 3 && getSession().getTrack(t).getPlayerScore() > 5000) {
					applause = 2;
				}
			}
			getTheme().loadSample("applause_" + applause + ".mp3");
			getTheme().playSample("applause_" + applause + ".mp3", false);

			progress = 0;
			int MAXSTEP = 500;
			for (int step = 0; step < MAXSTEP; step++) {
				progress = step / (float) MAXSTEP;
				int scoreSeconds = (int) (progress * maxScoreSeconds);
				for (int t = 0; t < trackCount; t++) {
					if (!isPlayerActive(t)) {
						continue;
					}
					int score = getSession().getTrack(t).getScoreAt(scoreSeconds);
					float alpha = score / (float) 10000;
					alpha = progress;
					angle[t] = 220 * (1 - alpha) + alpha * (-40);
				}
				repaint();
				try {
					sleep(20);
				}
				catch (Exception e) {
				}
			}
			progress = 1;
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  g  Description of the Parameter
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;

		YassSession session = getSession();
		String artist = session.getArtist();
		String title = session.getTitle();

		int colw1 = 360;
		int colw2 = 140;
		int colh = 40;

		int w = getSize().width;
		int h = getSize().height;
		int margin = getMargin();
		int x = margin;
		int yspace = h - margin - 5 * colh;
		int y = margin;

		int activePlayers = countActivePlayers();

		int clocksize = Math.min(h / 4, Math.max(100, (w - 80 - x - colw1 - (activePlayers - 1) * colw2) / 2));
		int xclock = w - 30 - clocksize;
		int yclock = 30 + clocksize;

		g2.setColor(getTheme().getColor(2));
		g2.setFont(getTextFont());
		g2.drawString(title, x, y += colh);
		g2.setFont(getSubTextFont());
		g2.drawString(artist, x, y += colh);

		y = Math.max(margin + 3 * colh, margin + yspace / 2);

		g2.setColor(getTheme().getColor(3));
		g2.fillRect(x, y + 4 * colh - 15, colw1 + (activePlayers - 1) * colw2, 10);

		y += colh;
		g2.setColor(getTheme().getColor(2));
		g2.setFont(getTextFont());
		g2.drawString(getString("note"), x, y);
		y += colh;
		g2.drawString(getString("golden"), x, y);
		y += colh;
		g2.drawString(getString("line"), x, y);

		int xoff = g2.getFontMetrics().stringWidth("00000") / 2;
		int off = g2.getFontMetrics().stringWidth("\u25CF") / 2 - 1;

		g2.setStroke(getThickStroke());
		for (int a = 205; a >= -25; a -= 10) {
			double cos = Math.cos(Math.PI * a / 180.0);
			double sin = Math.sin(Math.PI * a / 180.0);
			g2.drawLine(xclock + (int) (cos * (clocksize - 20)), yclock - (int) (sin * (clocksize - 20)), xclock + (int) (cos * clocksize), yclock - (int) (sin * clocksize));
		}

		int n = 0;
		int trackCount = session.getTrackCount();
		for (int t = 0; t < trackCount; t++) {
			if (!isPlayerActive(t)) {
				continue;
			}

			int noteScore = (int) session.getTrack(t).getPlayerNoteScore();
			int goldenScore = (int) session.getTrack(t).getPlayerGoldenScore();
			int lineScore = (int) session.getTrack(t).getPlayerLineScore();
			int playerScore = (int) session.getTrack(t).getPlayerScore();

			x = 20 + colw1 + n * colw2;
			y = Math.max(margin + 3 * colh, margin + yspace / 2);
			n++;

			g2.setColor(getTheme().getColor(2));
			String ps = getTheme().getPlayerSymbol(t, true);
			g2.drawString(ps, x - g2.getFontMetrics().stringWidth(ps) / 2, y);
			y += colh;
			g2.drawString(String.format(scoreFormat, noteScore), x - xoff, y);
			y += colh;
			g2.drawString(String.format(scoreFormat, goldenScore), x - xoff, y);
			y += colh;
			g2.drawString(String.format(scoreFormat, lineScore), x - xoff, y);
			y += colh;
			y += colh;

			g2.drawString(String.format(scoreFormat, playerScore), x - xoff, y);
			y += colh;

			g2.setColor(getTheme().getColor(2));
			if (angle[t] > -1000) {
				double rad = Math.PI * angle[t] / 180.0;
				double cos = Math.cos(rad);
				double sin = Math.sin(rad);

				g2.fillOval(xclock - 10, yclock - 10, 20, 20);
				g2.setColor(getTheme().getColor(5));
				g2.drawLine(xclock - (int) (cos * 15), yclock + (int) (sin * 15), xclock + (int) (cos * (clocksize - 10)), yclock - (int) (sin * (clocksize - 10)));

				g2.setColor(getTheme().getColor(0));
				g2.drawString("\u25CF", xclock - off + (int) (cos * (clocksize - 50 - t * 30)), yclock + off - (int) (sin * (clocksize - 50 - t * 30)));
				g2.setColor(getTheme().getColor(2));
				g2.drawString(getTheme().getPlayerSymbol(t, true), xclock - off + (int) (cos * (clocksize - 50 - t * 30)), yclock + off - (int) (sin * (clocksize - 50 - t * 30)));
			}

			if (progress > 0.9) {
				String s = null;
				if (playerRank[t] > 0 && playerRank[t] < 6) {
					s = getString("rank_" + playerRank[t]);
				} else if (playerRank[t] > 0) {
					s = getString("rank_6") + " " + playerRank[t];
				}
				if (s != null) {
					int sw = g2.getFontMetrics().stringWidth(s);
					Color c = getTheme().getColor(3);
					Color col = new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.min(255, (int) ((progress - 0.9) / 0.1 * 255)));
					g2.setColor(col);
					g2.drawString(s, x - sw / 2, y);
				}
			}

			g2.setStroke(getStandardStroke());
		}
	}
}

