package yass.screen;


/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    4. September 2006
 */
public class YassPlaySongScreen extends YassScreen {
	private static final long serialVersionUID = 4243370261445350556L;


	/**
	 *  Gets the iD attribute of the YassScoreScreen object
	 *
	 * @return    The iD value
	 */
	public String getID() {
		return "playsong";
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public String nextScreen() {
		return "enterscore";
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
		unloadBackgroundImage("start_background.jpg");
		unloadBackgroundImage("plain_background.jpg");
	}


	/**
	 *  Description of the Method
	 */
	public void hide() {
	}
}

