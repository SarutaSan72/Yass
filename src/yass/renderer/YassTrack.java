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

import java.util.Vector;

/**
 * Description of the Interface
 *
 * @author Saruta
 */
public class YassTrack implements Cloneable {
    /**
     * Description of the Field
     */
    public final static int EASY = 2;
    /**
     * Description of the Field
     */
    public final static int MEDIUM = 1;
    /**
     * Description of the Field
     */
    public final static int EXPERT = 0;
    /**
     * Description of the Field
     */
    public final static int BOTH = 0;
    /**
     * Description of the Field
     */
    public final static int P1 = 1;
    /**
     * Description of the Field
     */
    public final static int P2 = 2;
    protected YassSession session = null;
    private YassNote[] notes = null;
    private YassLine[] lines = null;
    private double bpm = 120;
    private int gap = 0;
    private int player = 0;
    private int playerScore = 0;
    private double playerNoteScore = 0;
    private double playerGoldenScore = 0;
    private double playerLineScore = 0;
    private Vector<YassPlayerNote> playerNotes = null;
    private String playerLineText = "";
    private int currentLine = 0, currentNote = 0;
    private long currentMillis = 0;
    private double currentPercent = 0;
    private int difficulty = 1;
    private boolean currentLineFinished = false;
    private boolean active = false;
    private int logScore[] = null;


    /**
     * Constructor for the YassTrack object
     *
     * @param notes     Description of the Parameter
     * @param lines     Description of the Parameter
     * @param bpm       Description of the Parameter
     * @param gapMillis Description of the Parameter
     */
    public YassTrack(YassNote[] notes, YassLine[] lines, double bpm, int gapMillis) {
        this.notes = notes;
        this.lines = lines;
        this.bpm = bpm;
        gap = gapMillis;
        playerNotes = new Vector<>(4096);
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public Object clone() {
        YassNote[] notes2 = new YassNote[notes.length];
        YassLine[] lines2 = new YassLine[lines.length];
        for (int i = 0; i < notes.length; i++) {
            notes2[i] = (YassNote) notes[i].clone();
        }
        for (int i = 0; i < lines.length; i++) {
            lines2[i] = (YassLine) lines[i].clone();
        }
        YassTrack track2 = new YassTrack(notes2, lines2, bpm, gap);
        track2.currentLine = currentLine;
        track2.currentNote = currentNote;
        track2.currentMillis = currentMillis;
        track2.currentPercent = currentPercent;
        track2.difficulty = difficulty;
        track2.currentLineFinished = currentLineFinished;
        track2.session = session;
        return track2;
    }


    /**
     * Gets the notes attribute of the YassTrack object
     *
     * @param i Description of the Parameter
     * @return The notes value
     */
    public YassNote getNote(int i) {
        return notes[i];
    }


    /**
     * Gets the noteCount attribute of the YassTrack object
     *
     * @return The noteCount value
     */
    public int getNoteCount() {
        return notes.length;
    }


    /**
     * Gets the pages attribute of the YassTrack object
     *
     * @param i Description of the Parameter
     * @return The pages value
     */
    public YassLine getLine(int i) {
        return lines[i];
    }


    /**
     * Gets the lineCount attribute of the YassTrack object
     *
     * @return The lineCount value
     */
    public int getLineCount() {
        return lines.length;
    }


    /**
     * Gets the bPM attribute of the YassSession object
     *
     * @return The bPM value
     */
    public double getBPM() {
        return bpm;
    }


    /**
     * Gets the pages attribute of the YassTrack object
     *
     * @return The pages value
     */
    public int getGapMillis() {
        return gap;
    }


    /**
     * Gets the playerNotes attribute of the YassTrack object
     *
     * @return The playerNotes value
     */
    public Vector<YassPlayerNote> getPlayerNotes() {
        return playerNotes;
    }


    /**
     * Gets the playerScore attribute of the YassTrack object
     *
     * @return The playerScore value
     */
    public int getPlayerScore() {
        return playerScore;
    }


    /**
     * Sets the playerScore attribute of the YassTrack object
     *
     * @param val The new playerScore value
     */
    public void setPlayerScore(int val) {
        playerScore = val;
    }


    /**
     * Gets the playerGoldenScore attribute of the YassTrack object
     *
     * @return The playerGoldenScore value
     */
    public double getPlayerGoldenScore() {
        return playerGoldenScore;
    }

    /**
     * Sets the playerScore attribute of the YassTrack object
     *
     * @param val The new playerScore value
     */
    public void setPlayerGoldenScore(double val) {
        playerGoldenScore = val;
    }

    /**
     * Gets the playerNoteScore attribute of the YassTrack object
     *
     * @return The playerNoteScore value
     */
    public double getPlayerNoteScore() {
        return playerNoteScore;
    }

    /**
     * Sets the playerNoteScore attribute of the YassTrack object
     *
     * @param val The new playerNoteScore value
     */
    public void setPlayerNoteScore(double val) {
        playerNoteScore = val;
    }


    /**
     * Gets the playerLineScore attribute of the YassTrack object
     *
     * @return The playerLineScore value
     */
    public double getPlayerLineScore() {
        return playerLineScore;
    }


    /**
     * Sets the playerScore attribute of the YassTrack object
     *
     * @param val The new playerScore value
     */
    public void setPlayerLineScore(double val) {
        playerLineScore = val;
    }

    /**
     * Gets the playerLineText attribute of the YassTrack object
     *
     * @return The playerLineText value
     */
    public String getPlayerLineText() {
        return playerLineText;
    }

    /**
     * Sets the playerLineText attribute of the YassTrack object
     *
     * @param s The new playerLineText value
     */
    public void setPlayerLineText(String s) {
        playerLineText = s;
    }

    /**
     * Gets the currentLine attribute of the YassSession object
     *
     * @return The currentLine value
     */
    public int getCurrentLine() {
        return currentLine;
    }


    /**
     * Gets the currentNote attribute of the YassSession object
     *
     * @return The currentNote value
     */
    public int getCurrentNote() {
        return currentNote;
    }


    /**
     * Gets the currentPercent attribute of the YassSession object
     *
     * @return The currentPercent value
     */
    public double getCurrentPercent() {
        return currentPercent;
    }


    /**
     * Gets the difficulty attribute of the YassTrack object
     *
     * @return The difficulty value
     */
    public int getDifficulty() {
        return difficulty;
    }


    /**
     * Sets the difficulty attribute of the YassTrack object
     *
     * @param d The new difficulty value
     */
    public void setDifficulty(int d) {
        difficulty = d;
    }


    /**
     * Gets the active attribute of the YassTrack object
     *
     * @return The active value
     */
    public boolean isActive() {
        return active;
    }


    /**
     * Sets the active attribute of the YassTrack object
     *
     * @param onoff The new active value
     */
    public void setActive(boolean onoff) {
        active = onoff;
    }


    /**
     * Description of the Method
     */
    public void initTrack() {
    }


    /**
     * Description of the Method
     *
     * @param currentMillis Description of the Parameter
     */
    public void updateTrack(long currentMillis) {
        this.currentMillis = currentMillis;
        int lineCount = getLineCount();
        if (lineCount < 1) {
            return;
        }
        int noteCount = getNoteCount();
        if (noteCount < 1) {
            return;
        }

        int lastCurrentLine = currentLine;
        currentLine = 0;
        YassLine line = getLine(currentLine);
        while (currentLine + 1 < lineCount && currentMillis > line.getEndMillis()) {
            line = getLine(++currentLine);
        }
        boolean newLine = lastCurrentLine != currentLine;
        if (newLine) {
            currentLineFinished = false;
        }

        currentNote = line.getFirstNote();
        YassNote note = getNote(currentNote);
        while (currentNote + 1 < noteCount && currentMillis > note.getEndMillis()) {
            note = getNote(++currentNote);
        }

        currentPercent = (currentMillis - note.getStartMillis()) / (double) (note.getEndMillis() - note.getStartMillis());
        if (currentPercent < 0) {
            currentPercent = -1;
        }
        if (currentPercent > 1) {
            currentPercent = 1;
        }

        boolean afterLastNote = currentMillis > getNote(line.getLastNote()).getEndMillis();
        if (afterLastNote && !currentLineFinished) {
            currentLineFinished = true;

            double playerLineScore = line.getPlayerLineScore();
            double ratio = playerLineScore / (double) line.getMaxLineScore();
            if (ratio >= .9) {
                setPlayerScore(getPlayerScore() + (int) playerLineScore);
                setPlayerLineScore(getPlayerLineScore() + (int) playerLineScore);
            }

            int r = (int) (7 * ratio + .1) + 1;
            setPlayerLineText(session.getRatingText(r));
        }
        int playerScore = 0;
        playerScore += (int) getPlayerNoteScore();
        playerScore += (int) getPlayerGoldenScore();
        playerScore += (int) getPlayerLineScore();
        setPlayerScore(playerScore);

        logScore(currentMillis, playerScore);
    }


    private void logScore(long currentMillis, int playerScore) {
        if (logScore == null) {
            int n = (int) (getNote(getNoteCount() - 1).getEndMillis() / 1000 + 1);
            logScore = new int[n];
        }

        int i = (int) (currentMillis / 1000);
        if (i > logScore.length - 1) {
            i = logScore.length - 1;
        }
        logScore[i] = playerScore;
    }


    /**
     * Gets the scoreAt attribute of the YassTrack object
     *
     * @param seconds Description of the Parameter
     * @return The scoreAt value
     */
    public int getScoreAt(int seconds) {
        if (logScore == null) {
            return 0;
        }
        return logScore[seconds];
    }


    /**
     * Gets the seconds attribute of the YassTrack object
     *
     * @return The seconds value
     */
    public int[] getScore() {
        return logScore;
    }


    /**
     * Adds a feature to the PlayerNote attribute of the YassTrack object
     *
     * @param playerNote The feature to be added to the PlayerNote attribute
     */
    public void addPlayerNote(YassPlayerNote playerNote) {
        boolean hit = false;

        // adjust pitch
        YassNote current = getNote(currentNote);
        int currentHeight = current.getHeight();
        int height = playerNote.getHeight();

        boolean noise = playerNote.isNoise();

        if (!noise) {
            while (height - currentHeight > 6) {
                height -= 12;
            }
            while (currentHeight - height > 6) {
                height += 12;
            }
            if (Math.abs(height - currentHeight) <= difficulty) {
                height = currentHeight;
                hit = true;
            }
            playerNote.setHeight(height);
        }

        // append or join with previous note (if same pitch)
        int playerNoteCount = playerNotes.size();
        if (playerNoteCount == 0) {
            if (!noise) {
                playerNotes.addElement(playerNote);
            }
            return;
        }

        YassPlayerNote lastNote = playerNotes.lastElement();
        long lastEndMillis = lastNote.getEndMillis();
        lastNote.setEndMillis(currentMillis);
        if (lastNote.getHeight() != height) {
            playerNotes.addElement(playerNote);
        }

        if (hit && current.getType() != YassNote.FREESTYLE) {
            long startMillis = Math.max(current.getStartMillis(), lastEndMillis);
            long endMillis = Math.min(current.getEndMillis(), currentMillis);
            long millis = endMillis - startMillis;

            if (millis > 0) {
                long totalMillis = current.getEndMillis() - current.getStartMillis();
                int maxNoteScore = current.getMaxNoteScore();

                double score = (millis / (double) totalMillis * maxNoteScore);
                current.setPlayerNoteScore(current.getPlayerNoteScore() + score);
                setPlayerNoteScore(getPlayerNoteScore() + score);

                if (current.getType() == YassNote.GOLDEN) {
                    int maxGoldenScore = current.getMaxGoldenScore();
                    double goldenScore = (millis / (double) totalMillis * maxGoldenScore);
                    current.setPlayerGoldenScore(current.getPlayerGoldenScore() + goldenScore);
                    setPlayerGoldenScore(getPlayerGoldenScore() + goldenScore);
                }

                YassLine line = getLine(currentLine);
                int maxLineScore = line.getMaxLineScore();
                long totalLineMillis = line.getLineMillis();
                double lineScore = (millis / (double) totalLineMillis * maxLineScore);
                line.setPlayerLineScore(line.getPlayerLineScore() + lineScore);
            }
        }
    }
}

