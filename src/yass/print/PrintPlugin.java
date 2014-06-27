package yass.print;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JPanel;

import yass.YassSong;

/**
 *  Description of the Interface
 *
 * @author     Saruta
 * @created    16. Februar 2009
 */
public interface PrintPlugin {
	/**
	 *  Gets the title attribute of the PrintPlugin object
	 *
	 * @return    The title value
	 */
	public String getTitle();


	/**
	 *  Gets the description attribute of the PrintPlugin object
	 *
	 * @return    The description value
	 */
	public String getDescription();


	/**
	 *  Gets the control attribute of the PrintPlugin object
	 *
	 * @return    The control value
	 */
	public JPanel getControl();


	/**
	 *  Description of the Method
	 *
	 * @param  songList  Description of the Parameter
	 * @param  filename  Description of the Parameter
	 * @param  options   Description of the Parameter
	 * @return           Description of the Return Value
	 */
	public boolean print(Vector<YassSong> songList, String filename, Hashtable<String,Object> options);


	/**
	 *  Description of the Method
	 *
	 * @param  options  Description of the Parameter
	 */
	public void commit(Hashtable<String,Object> options);
}

