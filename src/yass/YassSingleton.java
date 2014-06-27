package yass;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    26. August 2007
 */
public class YassSingleton {
	private static YassSingleton instance = new YassSingleton();


	/**
	 *Constructor for the YassSingleton object
	 */
	private YassSingleton() { }


	/**
	 *  Gets the instance attribute of the YassSingleton class
	 *
	 * @return    The instance value
	 */
	public static YassSingleton getInstance() {
		return instance;
	}
}

