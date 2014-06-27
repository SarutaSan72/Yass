package yass.wizard;

import java.awt.BorderLayout;
import java.net.URL;
import java.util.StringTokenizer;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.TableCellEditor;
import javax.swing.text.html.HTMLDocument;

import yass.I18;

import com.nexes.wizard.Wizard;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    25. August 2007
 */
public class Header extends JPanel {
	private static final long serialVersionUID = 2851283708411100220L;
	/**
	 *  Description of the Field
	 */
	public final static String ID = "header";
	private String mp3 = null;

	private JTable fileTable;


	/**
	 *  Constructor for the Header object
	 *
	 * @param  w  Description of the Parameter
	 */
	public Header(Wizard w) {
		JLabel iconLabel = new JLabel();
		setLayout(new BorderLayout());
		iconLabel.setIcon(new ImageIcon(this.getClass().getResource("clouds.jpg")));
		add("West", iconLabel);
		add("Center", getContentPanel());
	}


	/**
	 *  Gets the filename attribute of the Header object
	 *
	 * @return    The filename value
	 */
	public String getFilename() {
		return mp3;
	}


	/**
	 *  Sets the filename attribute of the Header object
	 *
	 * @param  s  The new filename value
	 */
	public void setFilename(String s) {
		mp3 = s;
	}


	/**
	 *  Gets the title attribute of the Header object
	 *
	 * @return    The title value
	 */
	public String getTitle() {
		return (String) fileTable.getValueAt(0, 1);
	}


	/**
	 *  Sets the title attribute of the Header object
	 *
	 * @param  s  The new title value
	 */
	public void setTitle(String s) {
		fileTable.setValueAt(s, 0, 1);
	}


	/**
	 *  Gets the artist attribute of the Header object
	 *
	 * @return    The artist value
	 */
	public String getArtist() {
		return (String) fileTable.getValueAt(1, 1);
	}


	/**
	 *  Sets the artist attribute of the Header object
	 *
	 * @param  s  The new artist value
	 */
	public void setArtist(String s) {
		fileTable.setValueAt(s, 1, 1);
	}


	/**
	 *  Gets the genre attribute of the Header object
	 *
	 * @return    The genre value
	 */
	public String getGenre() {
		return (String) genreEditor.getCellEditorValue();
		//return (String) fileTable.getValueAt(2, 1);
	}


	/**
	 *  Sets the genre attribute of the Header object
	 *
	 * @param  s  The new genre value
	 */
	public void setGenre(String s) {
		if (s.startsWith("(") && s.endsWith(")")) {
			s = s.substring(1, s.length() - 1);
			try {
				int n = Integer.parseInt(s);
				s = genres[n];
			}
			catch (Exception e) {}
		}
		fileTable.setValueAt(s, 2, 1);
		genreBox.setSelectedItem(s);
	}


	/**
	 *  Gets the language attribute of the Header object
	 *
	 * @return    The language value
	 */
	public String getLanguage() {
		return (String) langEditor.getCellEditorValue();
		//return (String) fileTable.getValueAt(3, 1);
	}


	/**
	 *  Sets the langage attribute of the Header object
	 *
	 * @param  s  The new langage value
	 */
	public void setLangage(String s) {
		fileTable.setValueAt(s, 3, 1);
	}


	/**
	 *  Gets the bPM attribute of the Header object
	 *
	 * @return    The bPM value
	 */
	public String getBPM() {
		return (String) fileTable.getValueAt(4, 1);
	}


	/**
	 *  Sets the bPM attribute of the Header object
	 *
	 * @param  s  The new bPM value
	 */
	public void setBPM(String s) {
		s = s.replace('.', ',');
		int i = s.indexOf(',');
		if (i >= 0 && i + 3 < s.length()) {
			s = s.substring(0, i + 3);
		}
		if (s.endsWith(",00")) {
			s = s.substring(0, s.length() - 3);
		}
		if (s.endsWith(",0")) {
			s = s.substring(0, s.length() - 2);
		}
		fileTable.setValueAt(s, 4, 1);
	}


	/**
	 *  Sets the genres attribute of the Header object
	 *
	 * @param  g  The new genres value
	 * @param  m  The new genres value
	 */
	public void setGenres(String g, String m) {
		genreBox = new JComboBox<>();
		genreBox.setEditable(true);
		StringTokenizer genres = new StringTokenizer(g, "|");
		while (genres.hasMoreTokens()) {
			genreBox.addItem(genres.nextToken());
		}
		genres = new StringTokenizer(m, "|");
		while (genres.hasMoreTokens()) {
			genreBox.addItem(genres.nextToken());
		}
		genreEditor = new DefaultCellEditor(genreBox);
	}


	JComboBox<String> genreBox = null;
	DefaultCellEditor genreEditor = null;


	/**
	 *  Sets the languages attribute of the Header object
	 *
	 * @param  l  The new languages value
	 * @param  m  The new languages value
	 */
	public void setLanguages(String l, String m) {
		JComboBox<String> langBox = new JComboBox<>();
		langBox.setEditable(true);
		StringTokenizer lang = new StringTokenizer(l, "|");
		while (lang.hasMoreTokens()) {
			langBox.addItem(lang.nextToken());
			if (lang.hasMoreTokens()) {
				lang.nextToken();
			}
		}
		lang = new StringTokenizer(m, "|");
		while (lang.hasMoreTokens()) {
			langBox.addItem(lang.nextToken());
			if (lang.hasMoreTokens()) {
				lang.nextToken();
			}
		}
		langEditor = new DefaultCellEditor(langBox);
	}


	DefaultCellEditor langEditor = null;


	/**
	 *  Gets the contentPanel attribute of the Header object
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
		URL url = I18.getResource("create_header.html");
		try {
			txt.setPage(url);
		}
		catch (Exception ex) {}
		content.add("Center", new JScrollPane(txt));

		fileTable =
			new JTable(5, 2) {
				private static final long serialVersionUID = -6838778960413155202L;

				public TableCellEditor getCellEditor(int row, int column) {
					if (row == 2 && column == 1 && genreEditor != null) {
						return (TableCellEditor) genreEditor;
					} else if (row == 3 && column == 1 && langEditor != null) {
						return (TableCellEditor) langEditor;
					} else if (column == 1) {
						return super.getCellEditor(row, column);
					} else {
						return null;
					}
				}
			};
		;
		fileTable.setValueAt(I18.get("create_header_title"), 0, 0);
		fileTable.setValueAt(I18.get("create_header_artist"), 1, 0);
		fileTable.setValueAt(I18.get("create_header_genre"), 2, 0);
		fileTable.setValueAt(I18.get("create_header_language"), 3, 0);
		fileTable.setValueAt(I18.get("create_header_bpm"), 4, 0);
		content.add("South", fileTable);
		return content;
	}


	/**
	 *  Description of the Field
	 */
	public final static String genres[] = {"Blues", "Classic Rock", "Country",
		"Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age",
		"Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial",
		"Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack", "Euro-Techno",
		"Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical",
		"Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise",
		"AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop",
		"Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial",
		"Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy",
		"Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle",
		"Native American", "Cabaret", "New Wave", "Psychadelic", "Rave", "Showtunes",
		"Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro",
		"Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", "National Folk",
		"Swing", "Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass",
		"Avantgarde", "Gothic Rock", "Progressive Rock", "Psychedelic Rock",
		"Symphonic Rock", "Slow Rock", "Big Band", "Chorus", "Easy Listening",
		"Acoustic", "Humour", "Speech", "Chanson", "Opera", "Chamber Music", "Sonata",
		"Symphony", "Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club",
		"Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul",
		"Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House",
		"Dance Hall", "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror",
		"Indie", "BritPop", "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta",
		"Heavy Metal", "Black Metal", "Crossover", "Contemporary C", "Christian Rock",
		"Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "SynthPop"};
}

