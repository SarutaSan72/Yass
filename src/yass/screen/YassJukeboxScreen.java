package yass.screen;

import java.util.Hashtable;
import java.util.Vector;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    4. September 2006
 */
public class YassJukeboxScreen extends YassScreen {
	private static final long serialVersionUID = -5940297789578629613L;
	private Hashtable<String, String> alreadyPlayed = new Hashtable<>();


	/**
	 *  Gets the iD attribute of the YassScoreScreen object
	 *
	 * @return    The iD value
	 */
	public String getID() {
		return "jukebox";
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public String nextScreen() {
		return "start";
	}


	/**
	 *  Description of the Method
	 *
	 * @param  key  Description of the Parameter
	 * @return      Description of the Return Value
	 */
	public boolean keyPressed(int key) {
		for (int t = 0; t < MAX_PLAYERS; t++) {
			if (key == SELECT[t]) {
				getTheme().playSample("menu_selection.wav", false);
				gotoScreen(nextScreen());
				return true;
			}
		}
		return false;
	}


	/**
	 *  Description of the Method
	 */
	public void show() {
		String param = getScreenParam();

		String artist = null;
		String title = null;
		if (param.equals("topten")) {
			getDatabase().open();
			Vector<YassScore> scores = new Vector<>();
			Vector<YassScore> scores1 = getDatabase().getTop(10, 0);
			Vector<YassScore> scores2 = getDatabase().getTop(10, 1);
			Vector<YassScore> scores3 = getDatabase().getTop(10, 2);
			scores.addAll(scores1);
			scores.addAll(scores2);
			scores.addAll(scores3);
			getDatabase().close();

			int n = scores.size();
			if (n > 0) {
				int i = (int) (Math.random() * n);
				YassScore score = (YassScore) scores.elementAt(i);
				artist = score.getArtist();
				title = score.getTitle();

				int trials = 100;
				while (alreadyPlayed.get(artist + "-" + title) != null && --trials > 0) {
					i = (int) (Math.random() * n);
					score = (YassScore) scores.elementAt(i);
					artist = score.getArtist();
					title = score.getTitle();
				}
				System.out.println("juke " + title);
			}
		}

		Vector<?> songs = getSongData();
		int n = songs.size();

		if ((alreadyPlayed.size() == n) || (n > 100 && alreadyPlayed.size() > n - 10)) {
			alreadyPlayed.clear();
		}

		if (artist == null || title == null) {
			if (n > 0) {
				int i = (int) (Math.random() * n);
				YassSongData sd = (YassSongData) songs.elementAt(i);
				artist = sd.getArtist();
				title = sd.getTitle();

				int trials = 100;
				while (alreadyPlayed.get(artist + "-" + title) != null && --trials > 0) {
					i = (int) (Math.random() * n);
					sd = (YassSongData) songs.elementAt(i);
					artist = sd.getArtist();
					title = sd.getTitle();
				}
				System.out.println("random " + title);
			}
		}

		for (int i = 0; i < n; i++) {
			YassSongData song = (YassSongData) songs.elementAt(i);
			if (song.getTitle().equals(title) && song.getArtist().equals(artist)) {
				setJukeboxSong(i);
				alreadyPlayed.put(artist + "-" + title, "true");
				break;
			}
		}
	}


	/**
	 *  Description of the Method
	 */
	public void hide() {
	}
}

