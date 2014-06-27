package yass.wizard;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;

import yass.I18;

import com.nexes.wizard.Wizard;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    26. August 2007
 */
public class Lyrics extends JPanel {
	private static final long serialVersionUID = 2887575440389998645L;
	/**
	 *  Description of the Field
	 */
	public final static String ID = "lyrics";
	private Wizard wizard;
	private JTextArea lyricsArea = null;
	private JCheckBox utf8 = null;


	/**
	 *  Constructor for the Lyrics object
	 *
	 * @param  w  Description of the Parameter
	 */
	public Lyrics(Wizard w) {
		wizard = w;
		JLabel iconLabel = new JLabel();
		setLayout(new BorderLayout());
		iconLabel.setIcon(new ImageIcon(this.getClass().getResource("clouds.jpg")));
		add("West", iconLabel);
		add("Center", getContentPanel());
	}


	/**
	 *  Gets the contentPanel attribute of the Lyrics object
	 *
	 * @return    The contentPanel value
	 */
	private JPanel getContentPanel() {
		JPanel content = new JPanel(new BorderLayout());
		JTextPane txt = new JTextPane();
		HTMLDocument doc = (HTMLDocument) txt.getEditorKitForContentType("text/html").createDefaultDocument();
		doc.setAsynchronousLoadPriority(-1);
		txt.setDocument(doc);
		URL url = I18.getResource("create_lyrics.html");
		try {
			txt.setPage(url);
		}
		catch (Exception ex) {}
		txt.setEditable(false);
		content.add("Center", new JScrollPane(txt));

		JPanel buttons = new JPanel(new GridLayout(1, 3));
		utf8 = new JCheckBox("UTF-8");
		utf8.setSelected(wizard.getValue("encoding").equals("utf8"));
		utf8.addItemListener(
			new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (utf8.isSelected()) {
						wizard.setValue("encoding", "utf8");
					} else {
						wizard.setValue("encoding", "");
					}
				}
			});
		buttons.add(utf8);
		buttons.add(new JLabel(""));
		buttons.add(new JLabel(""));

		lyricsArea = new JTextArea(10, 20);
		JPanel pan = new JPanel(new BorderLayout());
		pan.add("Center", new JScrollPane(lyricsArea));
		pan.add("South", buttons);
		content.add("South", pan);
		return content;
	}


	/**
	 *  Gets the text attribute of the Lyrics object
	 *
	 * @return    The text value
	 */
	public String getText() {
		return lyricsArea.getText();
	}


	/**
	 *  Sets the text attribute of the Lyrics object
	 *
	 * @param  s  The new text value
	 */
	public void setText(String s) {
		if (s == null) {
			s = "";
		}
		lyricsArea.setText(s);
	}


	/**
	 *  Gets the table attribute of the Lyrics object
	 *
	 * @return    The table value
	 */
	public String getTable() {
		StringWriter buffer = new StringWriter();
		PrintWriter outputStream = new PrintWriter(buffer);

		outputStream.println("#TITLE:Unknown");
		outputStream.println("#ARTIST:Unknown");
		outputStream.println("#LANGUAGE:Unknown");
		outputStream.println("#EDITION:Unknown");
		outputStream.println("#GENRE:Unknown");
		outputStream.println("#CREATOR:Unknown");
		outputStream.println("#MP3:Unknown");
		outputStream.println("#BPM:300");
		outputStream.println("#GAP:0");
		StringTokenizer st = new StringTokenizer(getText(), "\n");
		int b = 0;
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(line, " ");
			boolean isFirst = true;
			while (st2.hasMoreTokens()) {
				outputStream.println(": " + b + " 2 6 " + (isFirst ? "" : " ") + st2.nextToken());
				isFirst = false;
				b += 4;
			}
			if (st.hasMoreTokens()) {
				outputStream.println("- " + b);
			}
		}
		outputStream.println("E");
		return buffer.toString();
	}
}

