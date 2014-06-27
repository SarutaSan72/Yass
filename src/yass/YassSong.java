package yass;

import java.io.File;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    6. September 2007
 */
public class YassSong implements Cloneable, Comparable<Object> {
	private String s[] = new String[31];
	private String[] messages = null;
	private float[] stats = null;
	private long timestamp = 0;

	private ImageIcon icon = null;
	private boolean saved = true, opened = false, locked = false;
	private YassTable openedTable = null;

	private String lyrics = null, sortedArtist = null;

	private boolean hasVersions = false;


	/**
	 *  Sets the icon attribute of the YassSong object
	 *
	 * @param  ii  The new icon value
	 */
	public void setIcon(ImageIcon ii) {
		icon = ii;
	}


	/**
	 *  Gets the lyrics attribute of the YassSong object
	 *
	 * @return    The lyrics value
	 */
	public String getLyrics() {
		return lyrics;
	}


	/**
	 *  Sets the lyrics attribute of the YassSong object
	 *
	 * @param  s  The new lyrics value
	 */
	public void setLyrics(String s) {
		lyrics = s;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public boolean hasVersions() {
		return hasVersions;
	}


	/**
	 *  Sets the hasVersions attribute of the YassSong object
	 *
	 * @param  onoff  The new hasVersions value
	 */
	public void setHasVersions(boolean onoff) {
		hasVersions = onoff;
	}


	/**
	 *  Sets the table attribute of the YassSong object
	 *
	 * @param  t  The new table value
	 */
	public void setTable(YassTable t) {
		openedTable = t;
	}


	/**
	 *  Gets the table attribute of the YassSong object
	 *
	 * @return    The table value
	 */
	public YassTable getTable() {
		return openedTable;
	}


	/**
	 *  Sets the saved attribute of the YassSong object
	 *
	 * @param  onoff  The new saved value
	 */
	public void setSaved(boolean onoff) {
		saved = onoff;
	}


	/**
	 *  Sets the locked attribute of the YassSong object
	 *
	 * @param  onoff  The new locked value
	 */
	public void setLocked(boolean onoff) {
		locked = onoff;
	}


	/**
	 *  Sets the opened attribute of the YassSong object
	 *
	 * @param  onoff  The new opened value
	 */
	public void setOpened(boolean onoff) {
		opened = onoff;
	}


	/**
	 *  Gets the saved attribute of the YassSong object
	 *
	 * @return    The saved value
	 */
	public boolean isSaved() {
		return saved;
	}


	/**
	 *  Gets the locked attribute of the YassSong object
	 *
	 * @return    The locked value
	 */
	public boolean isLocked() {
		return locked;
	}


	/**
	 *  Gets the opened attribute of the YassSong object
	 *
	 * @return    The opened value
	 */
	public boolean isOpened() {
		return opened;
	}


	/**
	 *  Gets the icon attribute of the YassSong object
	 *
	 * @return    The icon value
	 */
	public ImageIcon getIcon() {
		return icon;
	}


	/**
	 *  Description of the Field
	 */
	public final static int SORT_BY_ARTIST = 2;
	/**
	 *  Description of the Field
	 */
	public final static int SORT_BY_TITLE = 4;
	/**
	 *  Description of the Field
	 */
	public final static int SORT_BY_VERSION = 8;
	/**
	 *  Description of the Field
	 */
	public final static int SORT_BY_EDITION = 16;
	/**
	 *  Description of the Field
	 */
	public final static int SORT_BY_GENRE = 32;
	/**
	 *  Description of the Field
	 */
	public final static int SORT_BY_LANGUAGE = 64;
	/**
	 *  Description of the Field
	 */
	public final static int SORT_BY_YEAR = 128;

	/**
	 *  Description of the Field
	 */
	public final static int SORT_BY_FOLDER = 256;
	/**
	 *  Description of the Field
	 */
	public final static int SORT_BY_COMPLETE = 512;

	/**
	 *  Description of the Field
	 */
	public final static int SORT_BY_ALBUM = 1024;

	/**
	 *  Description of the Field
	 */
	public final static int SORT_BY_LENGTH = 2048;

	/**
	 *  Description of the Field
	 */
	public final static int SORT_BY_ID = 4096;

	/**
	 *  Description of the Field
	 */
	public final static int SORT_BY_MESSAGE = 8192;

	/**
	 *  Description of the Field
	 */
	public final static int SORT_BY_STATS = 16384;
	/**
	 *  Description of the Field
	 */
	public static int msgindex = -1;
	/**
	 *  Description of the Field
	 */
	public static int statsindex = -1;

	/**
	 *  Description of the Field
	 */
	public final static int SORT_INVERSE = 1;

	/**
	 *  Description of the Field
	 */
	public static int ordering = SORT_BY_ARTIST;



	/**
	 *  Constructor for the YassSong object
	 *
	 * @param  state       Description of the Parameter
	 * @param  dir         Description of the Parameter
	 * @param  folder      Description of the Parameter
	 * @param  filename    Description of the Parameter
	 * @param  artist      Description of the Parameter
	 * @param  title       Description of the Parameter
	 * @param  edition     Description of the Parameter
	 * @param  genre       Description of the Parameter
	 * @param  language    Description of the Parameter
	 * @param  mp3         Description of the Parameter
	 * @param  cover       Description of the Parameter
	 * @param  video       Description of the Parameter
	 * @param  videogap    Description of the Parameter
	 * @param  start       Description of the Parameter
	 * @param  end         Description of the Parameter
	 * @param  relative    Description of the Parameter
	 * @param  bpm         Description of the Parameter
	 * @param  gap         Description of the Parameter
	 * @param  background  Description of the Parameter
	 * @param  year        Description of the Parameter
	 */
	public YassSong(String state, String dir, String folder, String filename, String artist, String title, String edition, String genre, String language, String year, String mp3, String cover, String background, String video, String videogap, String start, String end, String relative, String bpm, String gap) {
		s[0] = state;
		s[1] = dir;
		s[2] = folder;
		s[3] = filename;
		s[4] = artist;
		s[5] = title;
		s[6] = edition;
		s[7] = genre;
		s[8] = language;
		s[9] = year;
		s[10] = mp3;
		s[11] = cover;
		s[12] = background;
		s[13] = video;
		s[14] = videogap;
		s[15] = start;
		s[16] = end;
		s[17] = relative;
		s[18] = bpm;
		s[19] = gap;
		for (int i = 20; i < s.length; i++) {
			s[i] = "";
		}
		updateComplete();
	}


	/**
	 *  Constructor for the YassSong object
	 *
	 * @param  dir       Description of the Parameter
	 * @param  folder    Description of the Parameter
	 * @param  filename  Description of the Parameter
	 * @param  artist    Description of the Parameter
	 * @param  title     Description of the Parameter
	 */
	public YassSong(String dir, String folder, String filename, String artist, String title) {
		this(" ", dir, folder, filename, artist, title, "", "", "", "", "", "", "", "", "", "", "", "", "", "");
	}


	/**
	 *  Constructor for the YassSong object
	 *
	 * @param  r  Description of the Parameter
	 */
	public YassSong(YassSong r) {
		setSong(r);
	}


	/**
	 *  Constructor for the YassSong object
	 *
	 * @param  str  Description of the Parameter
	 */
	public YassSong(String str) {
		setSong(str);
	}


	/**
	 *  Sets the song attribute of the YassSong object
	 *
	 * @param  dir       The new song value
	 * @param  folder    The new song value
	 * @param  filename  The new song value
	 * @param  artist    The new song value
	 * @param  title     The new song value
	 * @param  edition   The new song value
	 * @param  genre     The new song value
	 * @param  language  The new song value
	 * @param  year      The new song value
	 */
	public void setSong(String dir, String folder, String filename, String artist, String title, String edition, String genre, String language, String year) {
		s[0] = "";
		s[1] = dir;
		s[2] = folder;
		s[3] = filename;
		s[4] = artist;
		s[5] = title;
		s[6] = edition;
		s[7] = genre;
		s[8] = language;
		s[9] = year;
		for (int i = 10; i < s.length; i++) {
			s[i] = "";
		}
		updateComplete();
	}


	/**
	 *  Sets the song attribute of the YassSong object
	 *
	 * @param  r  The new song value
	 */
	public void setSong(YassSong r) {
		for (int i = 0; i < s.length; i++) {
			s[i] = r.s[i];
		}
		if (r.messages == null) {
			messages = null;
		} else {
			for (int i = 0; i < messages.length; i++) {
				messages[i] = r.messages[i];
			}
		}
		if (r.stats == null) {
			stats = null;
		} else {
			for (int i = 0; i < stats.length; i++) {
				stats[i] = r.stats[i];
			}
		}
		icon = r.icon;
		saved = r.saved;
		opened = r.opened;
		locked = r.locked;
		openedTable = r.openedTable;

		timestamp = r.timestamp;
	}


	/**
	 *  Sets the song attribute of the YassSong object
	 *
	 * @param  str  The new song value
	 */
	public void setSong(String str) {
		StringTokenizer st = new StringTokenizer(str, "\t");
		int i = 0;
		while (st.hasMoreTokens() && i < s.length) {
			s[i] = st.nextToken();
			if (s[i].equals("-")) {
				s[i] = "";
			}
			i++;
		}
		if (st.hasMoreTokens()) {
			setTimestamp(st.nextToken());
		}
		/*
		 *  if (st.hasMoreTokens()) {
		 *  i = 0;
		 *  if (messages == null) {
		 *  messages = new String[YassRow.ALL_MESSAGES.length];
		 *  }
		 *  while (st.hasMoreTokens() && i < messages.length) {
		 *  messages[i] = st.nextToken();
		 *  if (messages[i].equals("-")) {
		 *  messages[i] = null;
		 *  }
		 *  i++;
		 *  }
		 *  }
		 */
	}


	/**
	 *  Sets the elementAt attribute of the YassSong object
	 *
	 * @param  val  The new elementAt value
	 * @param  i    The new elementAt value
	 */
	public void setElementAt(String val, int i) {
		s[i] = val;
	}


	/**
	 *  Sets the state attribute of the YassSong object
	 *
	 * @param  val  The new state value
	 */
	public void setState(String val) {
		s[0] = val;
	}


	/**
	 *  Sets the directory attribute of the YassSong object
	 *
	 * @param  val  The new directory value
	 */
	public void setDirectory(String val) {
		s[1] = val;
	}


	/**
	 *  Sets the folder attribute of the YassSong object
	 *
	 * @param  val  The new folder value
	 */
	public void setFolder(String val) {
		s[2] = val;
	}


	/**
	 *  Sets the filename attribute of the YassSong object
	 *
	 * @param  val  The new filename value
	 */
	public void setFilename(String val) {
		s[3] = val;
	}


	/**
	 *  Sets the cover attribute of the YassSong object
	 *
	 * @param  val  The new cover value
	 */
	public void setCover(String val) {
		s[11] = val;
	}


	/**
	 *  Sets the background attribute of the YassSong object
	 *
	 * @param  val  The new background value
	 */
	public void setBackground(String val) {
		s[12] = val;
		updateComplete();
	}


	/**
	 *  Sets the video attribute of the YassSong object
	 *
	 * @param  val  The new video value
	 */
	public void setVideo(String val) {
		s[13] = val;
		updateComplete();
	}


	/**
	 *  Sets the artist attribute of the YassSong object
	 *
	 * @param  val  The new artist value
	 */
	public void setArtist(String val) {
		s[4] = val;
	}


	/**
	 *  Sets the sortedArtist attribute of the YassSong object
	 *
	 * @param  val  The new sortedArtist value
	 */
	public void setSortedArtist(String val) {
		sortedArtist = val;
	}


	/**
	 *  Gets the sortedArtist attribute of the YassSong object
	 *
	 * @return    The sortedArtist value
	 */
	public String getSortedArtist() {
		return sortedArtist;
	}


	/**
	 *  Sets the title attribute of the YassSong object
	 *
	 * @param  val  The new title value
	 */
	public void setTitle(String val) {
		s[5] = val;
	}


	/**
	 *  Sets the edition attribute of the YassSong object
	 *
	 * @param  val  The new edition value
	 */
	public void setEdition(String val) {
		s[6] = val;
	}


	/**
	 *  Sets the genre attribute of the YassSong object
	 *
	 * @param  val  The new genre value
	 */
	public void setGenre(String val) {
		s[7] = val;
	}


	/**
	 *  Sets the language attribute of the YassSong object
	 *
	 * @param  val  The new language value
	 */
	public void setLanguage(String val) {
		s[8] = val;
	}


	/**
	 *  Sets the year attribute of the YassSong object
	 *
	 * @param  val  The new year value
	 */
	public void setYear(String val) {
		s[9] = val;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  i  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public String elementAt(int i) {
		return s[i];
	}


	/**
	 *  Gets the state attribute of the YassSong object
	 *
	 * @return    The state value
	 */
	public String getState() {
		return s[0];
	}


	/**
	 *  Gets the directory attribute of the YassSong object
	 *
	 * @return    The directory value
	 */
	public String getDirectory() {
		return s[1];
	}


	/**
	 *  Gets the folder attribute of the YassSong object
	 *
	 * @return    The folder value
	 */
	public String getFolder() {
		return s[2];
	}


	/**
	 *  Gets the filename attribute of the YassSong object
	 *
	 * @return    The filename value
	 */
	public String getFilename() {
		return s[3];
	}


	/**
	 *  Gets the cover attribute of the YassSong object
	 *
	 * @return    The cover value
	 */
	public String getCover() {
		return s[11];
	}


	/**
	 *  Gets the background attribute of the YassSong object
	 *
	 * @return    The background value
	 */
	public String getBackground() {
		return s[12];
	}


	/**
	 *  Gets the video attribute of the YassSong object
	 *
	 * @return    The video value
	 */
	public String getVideo() {
		return s[13];
	}


	/**
	 *  Gets the artist attribute of the YassSong object
	 *
	 * @return    The artist value
	 */
	public String getArtist() {
		return s[4];
	}


	/**
	 *  Gets the title attribute of the YassSong object
	 *
	 * @return    The title value
	 */
	public String getTitle() {
		return s[5];
	}


	/**
	 *  Gets the edition attribute of the YassSong object
	 *
	 * @return    The edition value
	 */
	public String getEdition() {
		return s[6];
	}


	/**
	 *  Gets the genre attribute of the YassSong object
	 *
	 * @return    The genre value
	 */
	public String getGenre() {
		return s[7];
	}


	/**
	 *  Gets the language attribute of the YassSong object
	 *
	 * @return    The language value
	 */
	public String getLanguage() {
		return s[8];
	}


	/**
	 *  Gets the year attribute of the YassSong object
	 *
	 * @return    The year value
	 */
	public String getYear() {
		return s[9];
	}


	/**
	 *  Gets the album attribute of the YassSong object
	 *
	 * @return    The album value
	 */
	public String getAlbum() {
		return s[23];
	}


	/**
	 *  Sets the album attribute of the YassSong object
	 *
	 * @param  val  The new album value
	 */
	public void setAlbum(String val) {
		s[23] = val;
	}


	/**
	 *  Gets the length attribute of the YassSong object
	 *
	 * @return    The length value
	 */
	public String getLength() {
		return s[24];
	}


	/**
	 *  Sets the length attribute of the YassSong object
	 *
	 * @param  val  The new length value
	 */
	public void setLength(String val) {
		s[24] = val;
	}


	/**
	 *  Gets the iD attribute of the YassSong object
	 *
	 * @return    The iD value
	 */
	public String getID() {
		return s[25];
	}


	/**
	 *  Sets the iD attribute of the YassSong object
	 *
	 * @param  val  The new iD value
	 */
	public void setID(String val) {
		s[25] = val;
	}


	/**
	 *  Gets the version attribute of the YassSong object
	 *
	 * @return    The version value
	 */
	public String getVersionID() {
		return s[26];
	}


	/**
	 *  Sets the version attribute of the YassSong object
	 *
	 * @param  val  The new version value
	 */
	public void setVersionID(String val) {
		s[26] = val;
	}


	/**
	 *  Gets the previewStart attribute of the YassSong object
	 *
	 * @return    The previewStart value
	 */
	public String getPreviewStart() {
		return s[27];
	}


	/**
	 *  Sets the previewStart attribute of the YassSong object
	 *
	 * @param  val  The new previewStart value
	 */
	public void setPreviewStart(String val) {
		s[27] = val;
	}


	/**
	 *  Gets the encoding attribute of the YassSong object
	 *
	 * @return    The encoding value
	 */
	public String getEncoding() {
		return s[30];
	}


	/**
	 *  Gets the medleyStartBeat attribute of the YassSong object
	 *
	 * @return    The medleyStartBeat value
	 */
	public String getMedleyStartBeat() {
		return s[28];
	}


	/**
	 *  Sets the previewStart attribute of the YassSong object
	 *
	 * @param  val  The new previewStart value
	 */
	public void setMedleyStartBeat(String val) {
		s[28] = val;
	}


	/**
	 *  Gets the medleyEndBeat attribute of the YassSong object
	 *
	 * @return    The medleyEndBeat value
	 */
	public String getMedleyEndBeat() {
		return s[29];
	}


	/**
	 *  Sets the previewStart attribute of the YassSong object
	 *
	 * @param  val  The new previewStart value
	 */
	public void setMedleyEndBeat(String val) {
		s[29] = val;
	}


	/**
	 *  Sets the previewStart attribute of the YassSong object
	 *
	 * @param  val  The new previewStart value
	 */
	public void setEncoding(String val) {
		s[30] = val;
	}


	/**
	 *  Sets the complete attribute of the YassSong object
	 *
	 * @param  val  The new complete value
	 */
	public void setComplete(String val) {
		s[20] = val;
	}


	/**
	 *  Gets the complete attribute of the YassSong object
	 *
	 * @return    The complete value
	 */
	public String getComplete() {
		return s[20];
	}


	/**
	 *  Gets the mP3 attribute of the YassSong object
	 *
	 * @return    The mP3 value
	 */
	public String getMP3() {
		return s[10];
	}


	/**
	 *  Sets the mP3 attribute of the YassSong object
	 *
	 * @param  val  The new mP3 value
	 */
	public void setMP3(String val) {
		s[10] = val;
	}


	/**
	 *  Gets the videoGap attribute of the YassSong object
	 *
	 * @return    The videoGap value
	 */
	public String getVideoGap() {
		return s[14];
	}


	/**
	 *  Sets the videoGap attribute of the YassSong object
	 *
	 * @param  val  The new videoGap value
	 */
	public void setVideoGap(String val) {
		s[14] = val;
	}


	/**
	 *  Gets the start attribute of the YassSong object
	 *
	 * @return    The start value
	 */
	public String getStart() {
		return s[15];
	}


	/**
	 *  Sets the start attribute of the YassSong object
	 *
	 * @param  val  The new start value
	 */
	public void setStart(String val) {
		s[15] = val;
	}


	/**
	 *  Gets the end attribute of the YassSong object
	 *
	 * @return    The end value
	 */
	public String getEnd() {
		return s[16];
	}


	/**
	 *  Sets the end attribute of the YassSong object
	 *
	 * @param  val  The new end value
	 */
	public void setEnd(String val) {
		s[16] = val;
	}


	/**
	 *  Gets the relative attribute of the YassSong object
	 *
	 * @return    The relative value
	 */
	public String getRelative() {
		return s[17];
	}


	/**
	 *  Sets the relative attribute of the YassSong object
	 *
	 * @param  val  The new relative value
	 */
	public void setRelative(String val) {
		s[17] = val;
	}


	/**
	 *  Gets the bPM attribute of the YassSong object
	 *
	 * @return    The bPM value
	 */
	public String getBPM() {
		return s[18];
	}


	/**
	 *  Sets the bPM attribute of the YassSong object
	 *
	 * @param  val  The new bPM value
	 */
	public void setBPM(String val) {
		s[18] = val;
	}


	/**
	 *  Gets the gap attribute of the YassSong object
	 *
	 * @return    The gap value
	 */
	public String getGap() {
		return s[19];
	}


	/**
	 *  Sets the gap attribute of the YassSong object
	 *
	 * @param  val  The new gap value
	 */
	public void setGap(String val) {
		s[19] = val;
	}


	/**
	 *  Gets the multiplayer attribute of the YassSong object
	 *
	 * @return    The multiplayer value
	 */
	public String getMultiplayer() {
		return s[22];
	}


	/**
	 *  Sets the multiplayer attribute of the YassSong object
	 *
	 * @param  m  The new multiplayer value
	 */
	public void setMultiplayer(String m) {
		s[22] = m;
	}


	/**
	 *  Description of the Method
	 */
	public void updateComplete() {
		String dir = getDirectory();
		String bg = getBackground();
		String vd = getVideo();
		boolean noBG = bg == null || bg.length() < 1 || !(new File(dir + File.separator + bg).exists());
		boolean noVD = vd == null || vd.length() < 1 || !(new File(dir + File.separator + vd).exists());
		if (!noVD) {
			setComplete("V");
		} else if (noBG) {
			setComplete("b");
		} else {
			setComplete("");
		}
	}


	/**
	 *  Sets the version attribute of the YassSong object
	 *
	 * @param  val  The new version value
	 */
	public void setVersion(String val) {
		s[21] = val;
	}


	/**
	 *  Gets the version attribute of the YassSong object
	 *
	 * @return    The version value
	 */
	public String getVersion() {
		return s[21];
	}


	/**
	 *  Sets the timestamp attribute of the YassSong object
	 *
	 * @param  t  The new timestamp value
	 */
	public void setTimestamp(long t) {
		timestamp = t;
	}


	/**
	 *  Sets the timestamp attribute of the YassSong object
	 *
	 * @param  s  The new timestamp value
	 */
	public void setTimestamp(String s) {
		try {
			timestamp = Long.parseLong(s);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 *  Gets the timestamp attribute of the YassSong object
	 *
	 * @return    The timestamp value
	 */
	public long getTimestamp() {
		return timestamp;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length; i++) {
			sb.append(s[i] == null || s[i].trim().length() < 1 ? "-" : s[i]);
			sb.append("\t");
		}
		sb.append(timestamp + "");

		/*
		 *  if (hasMessages()) {
		 *  sb.append("\t");
		 *  for (int i = 0; i < messages.length; i++) {
		 *  sb.append(messages[i] == null || messages[i].trim().length() < 1 ? "-" : messages[i]);
		 *  if (i < messages.length - 1) {
		 *  sb.append("\t");
		 *  }
		 *  }
		 *  }
		 */
		return sb.toString();
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public boolean hasMessages() {
		return messages != null;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public boolean hasStats() {
		return stats != null;
	}


	/**
	 *  Description of the Method
	 */
	public void clearMessages() {
		messages = null;
	}


	/**
	 *  Description of the Method
	 */
	public void clearStats() {
		stats = null;
	}


	/**
	 *  Gets the message attribute of the YassSong object
	 *
	 * @param  i  Description of the Parameter
	 * @return    The message value
	 */
	public String getMessage(int i) {
		return messages[i];
	}


	/**
	 *  Gets the stats attribute of the YassSong object
	 *
	 * @param  i  Description of the Parameter
	 * @return    The stats value
	 */
	public float getStatsAt(int i) {
		if (stats == null) {
			return -1;
		}
		return stats[i];
	}


	/**
	 *  Description of the Method
	 *
	 * @param  i  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public boolean hasMessage(int i) {
		return (messages != null) && (messages[i] != null);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  m  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public boolean hasMessage(int m[]) {
		if (!hasMessages()) {
			return false;
		}
		for (int i = 0; i < m.length; i++) {
			if (hasMessage(m[i])) {
				return true;
			}
		}
		return false;
	}


	/**
	 *  Sets the messageAt attribute of the YassSong object
	 *
	 * @param  i    The new messageAt value
	 * @param  msg  The new messageAt value
	 */
	public void setMessageAt(int i, String msg) {
		if (messages == null) {
			messages = new String[YassRow.ALL_MESSAGES.length];
		}
		messages[i] = msg;
	}


	/**
	 *  Sets the messageAt attribute of the YassSong object
	 *
	 * @param  i  The new messageAt value
	 * @param  s  The new statsAt value
	 */
	public void setStatsAt(int i, float s) {
		if (stats == null) {
			stats = new float[yass.stats.YassStats.length];
		}
		stats[i] = s;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public Object clone() {
		return new YassSong(this);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  o  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public int compareTo(Object o) {
		YassSong r = (YassSong) o;

		int f = ((ordering & SORT_INVERSE) != 0) ? -1 : 1;

		java.text.Collator col = java.text.Collator.getInstance();

		if ((ordering & SORT_BY_ARTIST) != 0) {
			String a = getSortedArtist();
			String ra = r.getSortedArtist();

			if (a == null) {
				a = getArtist();
			}
			if (ra == null) {
				ra = r.getArtist();
			}

			if (a == null) {
				a = "";
			}
			if (ra == null) {
				ra = "";
			}
			int res = col.compare(a.toLowerCase(), ra.toLowerCase());
			if (res != 0) {
				return f * res;
			}

			String t = getTitle();

			String rt = r.getTitle();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			res = col.compare(t.toLowerCase(), rt.toLowerCase());
			if (res != 0) {
				return res;
			}

			String v = getVersion();

			String rv = r.getVersion();
			if (v == null) {
				v = "";
			}
			if (rv == null) {
				rv = "";
			}
			return col.compare(v.toLowerCase(), rv.toLowerCase());
		}
		if ((ordering & SORT_BY_TITLE) != 0) {
			String t = getTitle();
			String rt = r.getTitle();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			int res = col.compare(t.toLowerCase(), rt.toLowerCase());
			if (res != 0) {
				return f * res;
			}

			String v = getVersion();

			String rv = r.getVersion();
			if (v == null) {
				v = "";
			}
			if (rv == null) {
				rv = "";
			}
			return col.compare(v.toLowerCase(), rv.toLowerCase());
		}
		if ((ordering & SORT_BY_EDITION) != 0) {
			String t = getEdition();
			String rt = r.getEdition();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			int res = col.compare(t.toLowerCase(), rt.toLowerCase());
			if (res != 0) {
				return f * res;
			}

			t = getTitle();
			rt = r.getTitle();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			return col.compare(t.toLowerCase(), rt.toLowerCase());
		}
		if ((ordering & SORT_BY_ALBUM) != 0) {
			String t = getAlbum();
			String rt = r.getAlbum();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			int res = col.compare(t.toLowerCase(), rt.toLowerCase());
			if (res != 0) {
				return f * res;
			}

			t = getTitle();
			rt = r.getTitle();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			return col.compare(t.toLowerCase(), rt.toLowerCase());
		}
		if ((ordering & SORT_BY_VERSION) != 0) {
			String v = getVersion();
			String rv = r.getVersion();
			if (v == null) {
				v = "";
			}
			if (rv == null) {
				rv = "";
			}
			int res = col.compare(v.toLowerCase(), rv.toLowerCase());
			if (res != 0) {
				return f * res;
			}

			String t = getTitle();

			String rt = r.getTitle();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			return col.compare(t.toLowerCase(), rt.toLowerCase());
		}
		if ((ordering & SORT_BY_GENRE) != 0) {
			String t = getGenre();
			String rt = r.getGenre();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			int res = col.compare(t.toLowerCase(), rt.toLowerCase());
			if (res != 0) {
				return f * res;
			}

			t = getTitle();
			rt = r.getTitle();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			return col.compare(t.toLowerCase(), rt.toLowerCase());
		}
		if ((ordering & SORT_BY_LANGUAGE) != 0) {
			String t = getLanguage();
			String rt = r.getLanguage();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			int res = col.compare(t.toLowerCase(), rt.toLowerCase());
			if (res != 0) {
				return f * res;
			}

			t = getTitle();
			rt = r.getTitle();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			return col.compare(t.toLowerCase(), rt.toLowerCase());
		}
		if ((ordering & SORT_BY_ID) != 0) {
			String t = getID();
			String rt = r.getID();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			int res = col.compare(t.toLowerCase(), rt.toLowerCase());
			if (res != 0) {
				return f * res;
			}

			t = getTitle();
			rt = r.getTitle();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			return col.compare(t.toLowerCase(), rt.toLowerCase());
		}
		if ((ordering & SORT_BY_YEAR) != 0) {
			String t = getYear();
			String rt = r.getYear();
			if (t == null || t.trim().length() < 1) {
				t = "99999";
			}
			if (rt == null || rt.trim().length() < 1) {
				rt = "99999";
			}

			Integer ti = new Integer(99999);
			try {
				ti = new Integer(t);
			}
			catch (Exception e) {
			}
			Integer rti = new Integer(99999);
			try {
				rti = new Integer(rt);
			}
			catch (Exception e) {
			}

			int res = ti.compareTo(rti);
			if (res != 0) {
				return f * res;
			}

			t = getTitle();
			rt = r.getTitle();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			return col.compare(t.toLowerCase(), rt.toLowerCase());
		}
		if ((ordering & SORT_BY_LENGTH) != 0) {
			String t = getLength();
			String rt = r.getLength();
			if (t == null || t.trim().length() < 1) {
				t = "999999";
			}
			if (rt == null || rt.trim().length() < 1) {
				rt = "999999";
			}

			Integer ti = new Integer(999999);
			try {
				ti = new Integer(t);
			}
			catch (Exception e) {
			}
			Integer rti = new Integer(999999);
			try {
				rti = new Integer(rt);
			}
			catch (Exception e) {
			}

			int res = ti.compareTo(rti);
			if (res != 0) {
				return f * res;
			}

			t = getTitle();
			rt = r.getTitle();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			return col.compare(t.toLowerCase(), rt.toLowerCase());
		}
		if ((ordering & SORT_BY_FOLDER) != 0) {
			String t = getFolder();
			String rt = r.getFolder();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			int res = col.compare(t.toLowerCase(), rt.toLowerCase());
			if (res != 0) {
				return f * res;
			}

			t = getTitle();
			rt = r.getTitle();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			return col.compare(t.toLowerCase(), rt.toLowerCase());
		}
		if ((ordering & SORT_BY_COMPLETE) != 0) {
			String t = getComplete();
			String rt = r.getComplete();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			int res = col.compare(t.toLowerCase(), rt.toLowerCase());
			if (res != 0) {
				return f * res;
			}

			t = getTitle();
			rt = r.getTitle();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			return col.compare(t.toLowerCase(), rt.toLowerCase());
		}
		if ((ordering & SORT_BY_MESSAGE) != 0) {
			if (msgindex < 0) {
				return 0;
			}
			if (messages == null && r.messages == null) {
				return 0;
			}
			if (messages == null && r.messages != null) {
				return 1;
			}
			if (messages != null && r.messages == null) {
				return -1;
			}

			String m = messages[msgindex];
			String rm = r.messages[msgindex];

			if (m == null || m.length() < 1) {
				m = "0";
			}
			if (rm == null || rm.length() < 1) {
				rm = "0";
			}
			Double d = new Double(m);
			Double rd = new Double(rm);

			int res = d.compareTo(rd);
			if (res != 0) {
				return f * res;
			}

			String t = getTitle();
			String rt = r.getTitle();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			return col.compare(t.toLowerCase(), rt.toLowerCase());
		}
		if ((ordering & SORT_BY_STATS) != 0) {
			if (statsindex < 0) {
				return 0;
			}
			if (stats == null && r.stats == null) {
				return 0;
			}
			if (stats == null && r.stats != null) {
				return 1;
			}
			if (stats != null && r.stats == null) {
				return -1;
			}

			float s = stats[statsindex];
			float rs = r.stats[statsindex];

			int res = Float.compare(s, rs);
			if (res != 0) {
				return f * res;
			}

			String t = getTitle();
			String rt = r.getTitle();
			if (t == null) {
				t = "";
			}
			if (rt == null) {
				rt = "";
			}
			return col.compare(t.toLowerCase(), rt.toLowerCase());
		}

		return 0;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  o  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public boolean equals(Object o) {
		YassSong r = (YassSong) o;
		for (int i = 0; i < s.length; i++) {
			if (s[i] == null && r.s[i] == null) {
				continue;
			}
			if (s[i] == null && r.s[i] != null) {
				return false;
			}
			if (s[i] != null && r.s[i] == null) {
				return false;
			}
			if (!s[i].equals(r.s[i])) {
				return false;
			}
		}
		return true;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public static String toFilename(String s) {
		if (s == null || s.length() < 1) {
			return "";
		}

		StringBuffer sb = new StringBuffer();
		int i = 0;
		int n = s.length();
		String c = "/\\?*:<>";
		char ch;
		while (i < n) {
			if (c.indexOf(ch = s.charAt(i++)) < 0) {
				sb.append(ch);
			}
		}
		while (sb.charAt(sb.length() - 1) == '.') {
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public yass.screen.YassSongData toSongData() {
		String artist = getArtist();
		String title = getTitle();
		String genre = getGenre();
		String edition = getEdition();
		String language = getLanguage();
		String folder = getFolder();
		String album = getAlbum();
		int year = -1;
		try {
			year = Integer.parseInt(getYear());
		}
		catch (Exception e) {}
		int length = -1;
		try {
			length = Integer.parseInt(getLength().replace(',', '.'));
		}
		catch (Exception e) {}
		int start = -1;
		try {
			start = Integer.parseInt(getStart().replace(',', '.'));
		}
		catch (Exception e) {}
		int end = -1;
		try {
			end = Integer.parseInt(getEnd().replace(',', '.'));
		}
		catch (Exception e) {}
		double bpm = -1;
		try {
			bpm = Double.parseDouble(getBPM().replace(',', '.'));
		}
		catch (Exception e) {}
		int gap = -1;
		try {
			gap = (int) Double.parseDouble(getGap().replace(',', '.'));
		}
		catch (Exception e) {}
		int medleyStartBeat = -1;
		try {
			medleyStartBeat = Integer.parseInt(getMedleyStartBeat());
		}
		catch (Exception e) {}
		int medleyEndBeat = -1;
		try {
			medleyEndBeat = Integer.parseInt(getMedleyEndBeat());
		}
		catch (Exception e) {}
		int previewStart = -1;
		try {
			previewStart = Integer.parseInt(getPreviewStart().replace(',', '.'));
		}
		catch (Exception e) {}
		int multiplayer = -1;
		try {
			multiplayer = Integer.parseInt(getMultiplayer());
		}
		catch (Exception e) {}

		String dir = getDirectory();
		File txt = new File(dir + File.separator + getFilename());
		if (!txt.exists()) {
			txt = null;
		}
		File thumbnail = new File(dir + File.separator + getCover());
		if (!thumbnail.exists()) {
			thumbnail = null;
		}
		File cover = new File(dir + File.separator + getCover());
		if (!cover.exists()) {
			cover = null;
		}
		File background = new File(dir + File.separator + getBackground());
		if (!background.exists()) {
			background = null;
		}
		File audio = new File(dir + File.separator + getMP3());
		if (!audio.exists()) {
			audio = null;
		}
		File video = new File(dir + File.separator + getVideo());
		if (!video.exists()) {
			video = null;
		}

		int medleyStartMillis = -1;
		int medleyEndMillis = -1;
		if (medleyStartBeat > 0) {
			double in = Math.max(0, 60 * medleyStartBeat / (4.0 * bpm) + gap / 1000.0);
			medleyStartMillis = (int) (1000 * in);
		}
		if (medleyEndBeat > 0) {
			double out = Math.max(0, 60 * medleyEndBeat / (4.0 * bpm) + gap / 1000.0);
			medleyEndMillis = (int) (1000 * out);
		}
		int previewStartMillis = -1;
		if (previewStart > 0) {
			previewStartMillis = (int) (1000 * previewStart);
		}

		return new yass.screen.YassSongData(artist, title, genre, edition, language, folder, album, year, length, start, end, bpm, gap, medleyStartMillis, medleyEndMillis, previewStartMillis, multiplayer, txt, thumbnail, cover, background, audio, video);
	}
}

