package yass.wizard;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
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
public class Edition extends JPanel {
	private static final long serialVersionUID = -2933153105162906931L;
	/**
	 *  Description of the Field
	 */
	public final static String ID = "edition";
	private JComboBox<String> fc;
	private JTextField eField;
	private String d = "";


	/**
	 *  Constructor for the Edition object
	 *
	 * @param  w  Description of the Parameter
	 */
	public Edition(Wizard w) {
		JLabel iconLabel = new JLabel();
		setLayout(new BorderLayout());
		iconLabel.setIcon(new ImageIcon(this.getClass().getResource("clouds.jpg")));
		add("West", iconLabel);
		add("Center", getContentPanel());
	}


	/**
	 *  Sets the songDir attribute of the Edition object
	 *
	 * @param  songdir  The new songDir value
	 */
	public void setSongDir(String songdir) {
		fc.removeAllItems();
		fc.addItem("");
		if (songdir == null) {
			return;
		}
		File f = new File(songdir);
		if (!f.exists() || !f.isDirectory()) {
			return;
		}
		File folders[] = f.listFiles();
		if (folders == null) {
			return;
		}
		for (int i = 0; i < folders.length; i++) {
			if (folders[i].isDirectory()) {
				fc.addItem(folders[i].getName());
			}
		}
	}


	/**
	 *  Gets the folder attribute of the Edition object
	 *
	 * @return    The folder value
	 */
	public String getFolder() {
		return (String) fc.getSelectedItem();
	}


	/**
	 *  Sets the folder attribute of the Edition object
	 *
	 * @param  s  The new folder value
	 */
	public void setFolder(String s) {
		d = s;
		fc.getEditor().setItem(d);
	}


	/**
	 *  Gets the edition attribute of the Edition object
	 *
	 * @return    The edition value
	 */
	public String getEdition() {
		return eField.getText();
	}


	/**
	 *  Sets the edition attribute of the Edition object
	 *
	 * @param  s  The new edition value
	 */
	public void setEdition(String s) {
		eField.setText(s);
	}


	/**
	 *  Gets the contentPanel attribute of the Edition object
	 *
	 * @return    The contentPanel value
	 */
	private JPanel getContentPanel() {
		JPanel content = new JPanel(new BorderLayout());
		JTextPane txt = new JTextPane();
		HTMLDocument doc = (HTMLDocument) txt.getEditorKitForContentType("text/html").createDefaultDocument();
		doc.setAsynchronousLoadPriority(-1);
		txt.setDocument(doc);
		txt.setEditable(false);
		URL url = I18.getResource("create_edition.html");
		try {
			txt.setPage(url);
		}
		catch (Exception ex) {}
		content.add("Center", new JScrollPane(txt));

		fc = new JComboBox<String>();
		fc.setEditable(true);

		JPanel fPanel = new JPanel(new BorderLayout());
		fPanel.add("West", new JLabel(I18.get("create_edition_folder")));
		fPanel.add("Center", fc);
		JPanel ePanel = new JPanel(new BorderLayout());
		ePanel.add("West", new JLabel(I18.get("create_edition_edition")));
		ePanel.add("Center", eField = new JTextField());

		JPanel all = new JPanel(new GridLayout(2, 1));
		all.add(fPanel);
		all.add(ePanel);
		content.add("South", all);
		return content;
	}
}

