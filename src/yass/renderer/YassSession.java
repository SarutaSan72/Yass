package yass.renderer;

/**
 *  Description of the Interface
 *
 * @author     Saruta
 * @created    22. M�rz 2010
 */
public class YassSession {
	private String artist = null;
	private String title = null;

	private YassTrack[] tracks = null;
	private long start = 0;
	private long end = -1;

	private int maxScore = 0;
	private int maxGoldenScore = 0;
	private int maxLineScore = 0;

	/**
	 *  Description of the Field
	 */
	public final static int RATING_PERFECT = 8;
	/**
	 *  Description of the Field
	 */
	public final static int RATING_AWESOME = 7;
	/**
	 *  Description of the Field
	 */
	public final static int RATING_GREAT = 6;
	/**
	 *  Description of the Field
	 */
	public final static int RATING_GOOD = 5;
	/**
	 *  Description of the Field
	 */
	public final static int RATING_NOTBAD = 4;
	/**
	 *  Description of the Field
	 */
	public final static int RATING_BAD = 3;
	/**
	 *  Description of the Field
	 */
	public final static int RATING_POOR = 2;
	/**
	 *  Description of the Field
	 */
	public final static int RATING_AWFUL = 1;

	/**
	 *  Description of the Field
	 */
	public final static int RATING_UNKNOWN = 0;

	private String[] ratings = null;


	/**
	 *  Gets the ratingText attribute of the YassSession object
	 *
	 * @param  i  Description of the Parameter
	 * @return    The ratingText value
	 */
	public String getRatingText(int i) {
		return ratings[i];
	}


	/**
	 *  Constructor for the YassSession object
	 *
	 * @param  tracks       Description of the Parameter
	 * @param  startMillis  Description of the Parameter
	 * @param  endMillis    Description of the Parameter
	 * @param  ratingText   Description of the Parameter
	 * @param  artist       Description of the Parameter
	 * @param  title        Description of the Parameter
	 */
	public YassSession(String artist, String title, YassTrack[] tracks, long startMillis, long endMillis, String ratingText[]) {
		this.artist = artist;
		this.title = title;
		this.tracks = tracks;
		start = startMillis;
		end = endMillis;

		for (int i = 0; i < tracks.length; i++) {
			tracks[i].session = this;
		}
		ratings = ratingText;
	}


	/**
	 *  Adds a feature to the Track attribute of the YassSession object
	 */
	public void addTrack() {
		YassTrack[] tracks2 = new YassTrack[tracks.length + 1];
		for (int i = 0; i < tracks.length; i++) {
			tracks2[i] = tracks[i];
		}
		tracks2[tracks.length] = (YassTrack) tracks[0].clone();
		tracks = tracks2;
	}


	/**
	 *  Gets the notes attribute of the YassTrack object
	 *
	 * @return    The notes value
	 */
	public String getArtist() {
		return artist;
	}


	/**
	 *  Gets the title attribute of the YassSession object
	 *
	 * @return    The title value
	 */
	public String getTitle() {
		return title;
	}


	/**
	 *  Gets the track attribute of the YassSession object
	 *
	 * @param  i  Description of the Parameter
	 * @return    The track value
	 */
	public YassTrack getTrack(int i) {
		return tracks[i];
	}


	/**
	 *  Gets the trackCount attribute of the YassSession object
	 *
	 * @return    The trackCount value
	 */
	public int getTrackCount() {
		return tracks.length;
	}


	/**
	 *  Gets the pages attribute of the YassTrack object
	 *
	 * @return    The pages value
	 */
	public long getStartMillis() {
		return start;
	}


	/**
	 *  Gets the endMillis attribute of the YassSession object
	 *
	 * @return    The endMillis value
	 */
	public long getEndMillis() {
		return end;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  currentMillis  Description of the Parameter
	 */
	public void updateSession(long currentMillis) {
		int trackCount = getTrackCount();
		for (int t = 0; t < trackCount; t++) {
			getTrack(t).updateTrack(currentMillis);
		}
	}


	/**
	 *  Gets the activeTracks attribute of the YassSession object
	 *
	 * @return    The activeTracks value
	 */
	public int getActiveTracks() {
		int n = 0;
		int trackCount = getTrackCount();
		for (int t = 0; t < trackCount; t++) {
			if (getTrack(t).isActive()) {
				n++;
			}
		}
		return n;
	}


	/**
	 *  Gets the maxScore attribute of the YassLine object
	 *
	 * @return    The maxScore value
	 */
	public int getMaxLineScore() {
		return maxLineScore;
	}


	/**
	 *  Gets the maxScore attribute of the YassLine object
	 *
	 * @return    The maxScore value
	 */
	public int getMaxGoldenScore() {
		return maxGoldenScore;
	}


	/**
	 *  Gets the maxScore attribute of the YassLine object
	 *
	 * @return    The maxScore value
	 */
	public int getMaxScore() {
		return maxScore;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  max     Description of the Parameter
	 * @param  golden  Description of the Parameter
	 * @param  line    Description of the Parameter
	 */
	public void initScore(int max, int golden, int line) {
		maxScore = max;
		maxGoldenScore = golden;
		maxLineScore = line;

		for (int k = 0; k < tracks.length; k++) {
			YassTrack t = tracks[k];
			int lineCount = t.getLineCount();
			int noteCount = t.getNoteCount();

			int scoreNoteCount = 0;
			int scoreGoldenCount = 0;
			int totalNoteLength = 0;
			for (int i = 0; i < noteCount; i++) {
				YassNote note = t.getNote(i);
				if (t.getNote(i).getType() == YassNote.GOLDEN) {
					scoreGoldenCount++;
				}
				if (note.getType() != YassNote.FREESTYLE) {
					scoreNoteCount++;
					totalNoteLength += note.getLength();
				}
			}
			for (int i = 0; i < noteCount; i++) {
				YassNote note = t.getNote(i);
				if (note.getType() == YassNote.GOLDEN) {
					note.setMaxGoldenScore((int) (maxGoldenScore / (double) scoreGoldenCount));
				}
				if (note.getType() != YassNote.FREESTYLE) {
					// same amount to all notes
					//t.getNote(i).setMaxNoteScore((int) (maxScore / (double) scoreNoteCount));
					note.setMaxNoteScore((int) (maxScore * note.getLength() / (double) totalNoteLength));
				}
			}

			int scoreLineCount = 0;
			for (int i = 0; i < lineCount; i++) {
				if (!t.getLine(i).isFreestyle()) {
					scoreLineCount++;
				}
			}
			for (int i = 0; i < lineCount; i++) {
				if (!t.getLine(i).isFreestyle()) {
					t.getLine(i).setMaxLineScore((int) (maxLineScore / (double) scoreLineCount));
				}
			}
		}
	}
}

