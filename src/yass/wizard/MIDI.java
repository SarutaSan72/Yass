package yass.wizard;

import com.nexes.wizard.Wizard;
import yass.I18;
import yass.YassMIDIConverter;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class MIDI extends JPanel {
    /**
     * Description of the Field
     */
    public final static String ID = "midi";
    private static final long serialVersionUID = -7574656534689143176L;
    private YassMIDIConverter convert = null;
    private JPanel content = null;
    private JComponent gui = null;


    /**
     * Constructor for the MIDI object
     *
     * @param w Description of the Parameter
     */
    public MIDI(Wizard w) {
        JLabel iconLabel = new JLabel();
        setLayout(new BorderLayout());
        iconLabel.setIcon(new ImageIcon(this.getClass().getResource("clouds.jpg")));
        add("West", iconLabel);
        add("Center", getContentPanel());
    }


    private JPanel getContentPanel() {
        content = new JPanel(new BorderLayout());
        JTextPane txt = new JTextPane();
        HTMLDocument doc = (HTMLDocument) txt.getEditorKitForContentType("text/html").createDefaultDocument();
        doc.setAsynchronousLoadPriority(-1);
        txt.setDocument(doc);
        URL url = I18.getResource("create_midi.html");
        try {
            txt.setPage(url);
        } catch (Exception ignored) {
        }
        txt.setEditable(false);
        content.add("North", new JScrollPane(txt));

        convert = new YassMIDIConverter();
        convert.setStandAlone(false);
        content.add("Center", gui = convert.getGUI());
        return content;
    }


    /**
     * Sets the filename attribute of the MIDI object
     *
     * @param filename The new filename value
     */
    public void setFilename(String filename) {
        content.remove(gui);

        convert.setFilename(filename);
        int voiceTrack = convert.init();
        if (voiceTrack < 0 || true) {
            gui = convert.getGUI();
        } else {
            gui = new JTextPane();
            ((JTextPane) gui).setText(convert.createTable(voiceTrack));
            gui.setEnabled(false);
        }

        JRootPane root = SwingUtilities.getRootPane(content);
        root.addComponentListener(
                new ComponentAdapter() {
                    public void componentResized(ComponentEvent e) {
                        if (e.getID() == ComponentEvent.COMPONENT_RESIZED) {
                            convert.updateSheet();
                        }
                    }
                });

        content.add("Center", gui);
        content.validate();
    }


    /**
     * Description of the Method
     */
    public void startRendering() {
        convert.startGUI();
    }


    /**
     * Description of the Method
     */
    public void stopRendering() {
        convert.close();
    }


    /**
     * Gets the text attribute of the MIDI object
     *
     * @return The text value
     */
    public String getText() {
        return convert.getText();
    }


    /**
     * Gets the maxBPM attribute of the MIDI object
     *
     * @return The maxBPM value
     */
    public String getMaxBPM() {
        return convert.getMaxBPM() + "";
    }
}

