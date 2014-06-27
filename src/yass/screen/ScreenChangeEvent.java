package yass.screen;


/**
 *  Description of the Interface
 *
 * @author     Saruta
 * @created    8. April 2010
 */
public class ScreenChangeEvent {
	private String oldScreen = null;
	private String newScreen = null;
	private YassScreen screen = null;


	/**
	 *  Constructor for the ScreenChangeEvent object
	 *
	 * @param  source     Description of the Parameter
	 * @param  oldScreen  Description of the Parameter
	 * @param  newScreen  Description of the Parameter
	 */
	public ScreenChangeEvent(YassScreen source, String oldScreen, String newScreen) {
		screen = source;
		this.oldScreen = oldScreen;
		this.newScreen = newScreen;
	}


	/**
	 *  Gets the source attribute of the ScreenChangeEvent object
	 *
	 * @return    The source value
	 */
	public YassScreen getSource() {
		return screen;
	}


	/**
	 *  Gets the oldScreen attribute of the ScreenChangeEvent object
	 *
	 * @return    The oldScreen value
	 */
	public String getOldScreen() {
		return oldScreen;
	}


	/**
	 *  Gets the newScreen attribute of the ScreenChangeEvent object
	 *
	 * @return    The newScreen value
	 */
	public String getNewScreen() {
		return newScreen;
	}
}

