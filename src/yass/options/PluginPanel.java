package yass.options;


/**
 * Description of the Class
 *
 * @author Saruta
 */
public class PluginPanel extends OptionsPanel {

    private static final long serialVersionUID = -5827257245107763965L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        addText("Package:", "plugins");
        addComment("Location of custom plugins.<br>(Requires restart for changes to take effect.)");
    }
}

