/*
 * Yass - Karaoke Editor
 * Copyright (C) 2009 Saruta
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package yass.renderer;

/**
 * Description of the Interface
 *
 * @author Saruta
 */
public class YassSession {
    /**
     * Description of the Field
     */
    public final static int RATING_PERFECT = 8;
    /**
     * Description of the Field
     */
    public final static int RATING_AWESOME = 7;
    /**
     * Description of the Field
     */
    public final static int RATING_GREAT = 6;
    /**
     * Description of the Field
     */
    public final static int RATING_GOOD = 5;
    /**
     * Description of the Field
     */
    public final static int RATING_NOTBAD = 4;
    /**
     * Description of the Field
     */
    public final static int RATING_BAD = 3;
    /**
     * Description of the Field
     */
    public final static int RATING_POOR = 2;
    /**
     * Description of the Field
     */
    public final static int RATING_AWFUL = 1;
    /**
     * Description of the Field
     */
    public final static int RATING_UNKNOWN = 0;
    private String artist = null;
    private String title = null;
    private YassTrack[] tracks = null;
    private long start = 0;
    private long end = -1;
    private int maxScore = 0;
    private int maxGoldenScore = 0;
    private int maxLineScore = 0;
    private String[] ratings = null;


    /**
     * Constructor for the YassSession object
     *
     * @param tracks      Description of the Parameter
     * @param startMillis Description of the Parameter
     * @param endMillis   Description of the Parameter
     * @param ratingText  Description of the Parameter
     * @param artist      Description of the Parameter
     * @param title       Description of the Parameter
     */
    public YassSession(String artist, String title, YassTrack[] tracks, long startMillis, long endMillis, String ratingText[]) {
        this.artist = artist;
        this.title = title;
        this.tracks = tracks;
        start = startMillis;
        end = endMillis;

        for (YassTrack track : tracks) {
            track.session = this;
        }
        ratings = ratingText;
    }

    /**
     * Gets the ratingText attribute of the YassSession object
     *
     * @param i Description of the Parameter
     * @return The ratingText value
     */
    public String getRatingText(int i) {
        return ratings[i];
    }

    /**
     * Adds a feature to the Track attribute of the YassSession object
     */
    public void addTrack() {
        YassTrack[] tracks2 = new YassTrack[tracks.length + 1];
        System.arraycopy(tracks, 0, tracks2, 0, tracks.length);
        tracks2[tracks.length] = (YassTrack) tracks[0].clone();
        tracks = tracks2;
    }


    /**
     * Gets the notes attribute of the YassTrack object
     *
     * @return The notes value
     */
    public String getArtist() {
        return artist;
    }


    /**
     * Gets the title attribute of the YassSession object
     *
     * @return The title value
     */
    public String getTitle() {
        return title;
    }


    /**
     * Gets the track attribute of the YassSession object
     *
     * @param i Description of the Parameter
     * @return The track value
     */
    public YassTrack getTrack(int i) {
        return tracks[i];
    }


    /**
     * Gets the trackCount attribute of the YassSession object
     *
     * @return The trackCount value
     */
    public int getTrackCount() {
        return tracks.length;
    }


    /**
     * Gets the pages attribute of the YassTrack object
     *
     * @return The pages value
     */
    public long getStartMillis() {
        return start;
    }


    /**
     * Gets the endMillis attribute of the YassSession object
     *
     * @return The endMillis value
     */
    public long getEndMillis() {
        return end;
    }


    /**
     * Description of the Method
     *
     * @param currentMillis Description of the Parameter
     */
    public void updateSession(long currentMillis) {
        int trackCount = getTrackCount();
        for (int t = 0; t < trackCount; t++) {
            getTrack(t).updateTrack(currentMillis);
        }
    }


    /**
     * Gets the activeTracks attribute of the YassSession object
     *
     * @return The activeTracks value
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
     * Gets the maxScore attribute of the YassLine object
     *
     * @return The maxScore value
     */
    public int getMaxLineScore() {
        return maxLineScore;
    }


    /**
     * Gets the maxScore attribute of the YassLine object
     *
     * @return The maxScore value
     */
    public int getMaxGoldenScore() {
        return maxGoldenScore;
    }


    /**
     * Gets the maxScore attribute of the YassLine object
     *
     * @return The maxScore value
     */
    public int getMaxScore() {
        return maxScore;
    }


    /**
     * Description of the Method
     *
     * @param max    Description of the Parameter
     * @param golden Description of the Parameter
     * @param line   Description of the Parameter
     */
    public void initScore(int max, int golden, int line) {
        maxScore = max;
        maxGoldenScore = golden;
        maxLineScore = line;

        for (YassTrack t : tracks) {
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

