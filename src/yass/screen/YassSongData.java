package yass.screen;

import java.io.File;

/**
 *  Description of the Interface
 *
 * @author     Saruta
 * @created    22. M�rz 2010
 */
public class YassSongData {
	private String artist = null;
	private String title = null;
	private String genre = null;
	private String edition = null;
	private String language = null;
	private String folder = null;
	private String album = null;
	private int year = 0;
	private int length = 0;
	private int medleyStart = 0;
	private int medleyEnd = 0;
	private int previewStart = 0;
	private int start = 0;
	private int end = -1;
	private double bpm = 0;
	private int gap = 0;
	private int multiplayer = 1;

	private File txt = null;
	private File thumbnail = null;
	private File cover = null;
	private File background = null;
	private File audio = null;
	private File video = null;


	/**
	 *  Constructor for the YassSongData object
	 *
	 * @param  artist        Description of the Parameter
	 * @param  title         Description of the Parameter
	 * @param  genre         Description of the Parameter
	 * @param  edition       Description of the Parameter
	 * @param  language      Description of the Parameter
	 * @param  folder        Description of the Parameter
	 * @param  album         Description of the Parameter
	 * @param  year          Description of the Parameter
	 * @param  length        Description of the Parameter
	 * @param  start         Description of the Parameter
	 * @param  end           Description of the Parameter
	 * @param  bpm           Description of the Parameter
	 * @param  gap           Description of the Parameter
	 * @param  medleyStart   Description of the Parameter
	 * @param  medleyEnd     Description of the Parameter
	 * @param  previewStart  Description of the Parameter
	 * @param  multiplayer   Description of the Parameter
	 * @param  txt           Description of the Parameter
	 * @param  thumbnail     Description of the Parameter
	 * @param  cover         Description of the Parameter
	 * @param  background    Description of the Parameter
	 * @param  audio         Description of the Parameter
	 * @param  video         Description of the Parameter
	 */
	public YassSongData(String artist, String title, String genre, String edition, String language, String folder, String album, int year, int length, int start, int end, double bpm, int gap, int medleyStart, int medleyEnd, int previewStart, int multiplayer, File txt, File thumbnail, File cover, File background, File audio, File video) {
		this.artist = artist;
		this.title = title;
		this.genre = genre;
		this.edition = edition;
		this.language = language;
		this.folder = folder;
		this.album = album;
		this.year = year;
		this.length = length;
		this.start = start;
		this.end = end;
		this.bpm = bpm;
		this.gap = gap;
		this.medleyStart = medleyStart;
		this.medleyEnd = medleyEnd;
		this.previewStart = previewStart;
		this.multiplayer = multiplayer;

		this.txt = txt;
		this.thumbnail = thumbnail;
		this.cover = cover;
		this.background = background;
		this.audio = audio;
		this.video = video;
	}


	/**
	 *  Gets the artist attribute of the YassSongData object
	 *
	 * @return    The artist value
	 */
	public String getArtist() {
		return artist;
	}


	/**
	 *  Gets the title attribute of the YassSongData object
	 *
	 * @return    The title value
	 */
	public String getTitle() {
		return title;
	}


	/**
	 *  Gets the genre attribute of the YassSongData object
	 *
	 * @return    The genre value
	 */
	public String getGenre() {
		return genre;
	}


	/**
	 *  Gets the edition attribute of the YassSongData object
	 *
	 * @return    The edition value
	 */
	public String getEdition() {
		return edition;
	}


	/**
	 *  Gets the language attribute of the YassSongData object
	 *
	 * @return    The language value
	 */
	public String getLanguage() {
		return language;
	}


	/**
	 *  Gets the folder attribute of the YassSongData object
	 *
	 * @return    The folder value
	 */
	public String getFolder() {
		return folder;
	}


	/**
	 *  Gets the album attribute of the YassSongData object
	 *
	 * @return    The album value
	 */
	public String getAlbum() {
		return album;
	}


	/**
	 *  Gets the year attribute of the YassSongData object
	 *
	 * @return    The year value
	 */
	public int getYear() {
		return year;
	}


	/**
	 *  Gets the length attribute of the YassSongData object
	 *
	 * @return    The length value
	 */
	public int getLength() {
		return length;
	}


	/**
	 *  Gets the start attribute of the YassSongData object
	 *
	 * @return    The start value
	 */
	public int getStart() {
		return start;
	}


	/**
	 *  Gets the end attribute of the YassSongData object
	 *
	 * @return    The end value
	 */
	public int getEnd() {
		return end;
	}


	/**
	 *  Gets the bPM attribute of the YassSongData object
	 *
	 * @return    The bPM value
	 */
	public double getBPM() {
		return bpm;
	}


	/**
	 *  Gets the gap attribute of the YassSongData object
	 *
	 * @return    The gap value
	 */
	public int getGap() {
		return gap;
	}


	/**
	 *  Gets the medleyStart attribute of the YassSongData object
	 *
	 * @return    The medleyStart value
	 */
	public int getMedleyStart() {
		return medleyStart;
	}


	/**
	 *  Gets the medleyEnd attribute of the YassSongData object
	 *
	 * @return    The medleyEnd value
	 */
	public int getMedleyEnd() {
		return medleyEnd;
	}


	/**
	 *  Gets the previewStart attribute of the YassSongData object
	 *
	 * @return    The previewStart value
	 */
	public int getPreviewStart() {
		return previewStart;
	}


	/**
	 *  Gets the multiplayer attribute of the YassSongData object
	 *
	 * @return    The multiplayer value
	 */
	public int getMultiplayer() {
		return multiplayer;
	}


	/**
	 *  Gets the karaokeData attribute of the YassSongData object
	 *
	 * @return    The karaokeData value
	 */
	public File getKaraokeData() {
		return txt;
	}


	/**
	 *  Gets the thumbnail attribute of the YassSongData object
	 *
	 * @return    The thumbnail value
	 */
	public File getThumbnail() {
		return thumbnail;
	}


	/**
	 *  Gets the cover attribute of the YassSongData object
	 *
	 * @return    The cover value
	 */
	public File getCover() {
		return cover;
	}


	/**
	 *  Gets the background attribute of the YassSongData object
	 *
	 * @return    The background value
	 */
	public File getBackground() {
		return background;
	}


	/**
	 *  Gets the audio attribute of the YassSongData object
	 *
	 * @return    The audio value
	 */
	public File getAudio() {
		return audio;
	}


	/**
	 *  Gets the video attribute of the YassSongData object
	 *
	 * @return    The video value
	 */
	public File getVideo() {
		return video;
	}
}

