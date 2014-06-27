package yass;

import java.util.EventListener;
/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    10. Oktober 2007
 */
public interface YassSongListListener extends EventListener {

	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void stateChanged(YassSongListEvent e);

}

