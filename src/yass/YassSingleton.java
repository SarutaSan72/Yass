package yass;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassSingleton {
    private static YassSingleton instance = new YassSingleton();


    /**
     * Constructor for the YassSingleton object
     */
    private YassSingleton() {
    }


    /**
     * Gets the instance attribute of the YassSingleton class
     *
     * @return The instance value
     */
    public static YassSingleton getInstance() {
        return instance;
    }
}

