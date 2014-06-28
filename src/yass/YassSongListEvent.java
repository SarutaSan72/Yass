package yass;

import java.util.EventObject;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassSongListEvent extends EventObject {
    /**
     * Description of the Field
     */
    public final static int STARTING = 0;
    /**
     * Description of the Field
     */
    public final static int SEARCHING = 1;
    /**
     * Description of the Field
     */
    public final static int UPDATING = 2;
    /**
     * Description of the Field
     */
    public final static int LOADING = 3;
    /**
     * Description of the Field
     */
    public final static int LOADED = 4;
    /**
     * Description of the Field
     */
    public final static int THUMBNAILING = 5;
    /**
     * Description of the Field
     */
    public final static int FINISHED = 6;
    private static final long serialVersionUID = -7232684562486258377L;
    int state = 0;


    /**
     * Constructor for the YassSongListEvent object
     *
     * @param src   Description of the Parameter
     * @param state Description of the Parameter
     */
    public YassSongListEvent(Object src, int state) {
        super(src);
        this.state = state;
    }


    /**
     * Gets the state attribute of the YassSongListEvent object
     *
     * @return The state value
     */
    public int getState() {
        return state;
    }
}

