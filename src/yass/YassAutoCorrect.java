/*
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

package yass;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassAutoCorrect {
    private static int FIXED_PAGE_BREAK = 0;

    int[] fontWidth = null;
    int fontSize = 14, charSpacing = 2;
    private YassProperties prop = null;
    private String[] audioExtensions, imageExtensions, videoExtensions;
    private String coverID;
    private String backgroundID;
    private String videoID;

    private Map<String, YassAutoCorrector> autoCorrectorMap;
    private YassLanguageUtils languageUtils;

    private Map<String, YassAutoCorrector> initAutoCorrectors() {
        Map<String, YassAutoCorrector> tempAutoCorrectors = new HashMap<>();
        tempAutoCorrectors.put(YassRow.UNCOMMON_SPACING, new YassAutoCorrectUncommonSpacing(prop));
        tempAutoCorrectors.put(YassRow.LOWERCASE_ROWSTART, new YassAutoCorrectLineCapitalization(prop));
        tempAutoCorrectors.put(YassRow.BORING_APOSTROPHE, new YassAutoCorrectApostrophes(prop));
        return tempAutoCorrectors;
    }

    /**
     * Constructor for the YassAutoCorrect object
     */
    public YassAutoCorrect() {
    }

    /**
     * Gets the autoCorrectionPageBreak attribute of the YassAutoCorrect object
     *
     * @param msg Description of the Parameter
     * @return The autoCorrectionPageBreak value
     */
    public static boolean isAutoCorrectionPageBreak(String msg) {
        return msg.equals(YassRow.PAGE_OVERLAP)
                || msg.equals(YassRow.SHORT_PAGE_BREAK)
                || msg.equals(YassRow.EARLY_PAGE_BREAK)
                || msg.equals(YassRow.LATE_PAGE_BREAK);
    }

    /**
     * Gets the autoCorrectionMinorPageBreak attribute of the YassAutoCorrect
     * class
     *
     * @param msg Description of the Parameter
     * @return The autoCorrectionMinorPageBreak value
     */
    public static boolean isAutoCorrectionMinorPageBreak(String msg) {
        return msg.equals(YassRow.UNCOMMON_PAGE_BREAK);
    }

    /**
     * Gets the autoCorrectionFileNames attribute of the YassAutoCorrect object
     *
     * @param msg Description of the Parameter
     * @return The autoCorrectionFileNames value
     */
    public static boolean isAutoCorrectionFileName(String msg) {
        return msg.equals(YassRow.FILE_FOUND)
                || msg.equals(YassRow.NO_COVER_LABEL)
                || msg.equals(YassRow.NO_BACKGROUND_LABEL)
                || msg.equals(YassRow.NO_VIDEO_LABEL)
                || msg.equals(YassRow.WRONG_VIDEOGAP);
    }

    /**
     * Gets the autoCorrectionTags attribute of the YassAutoCorrect class
     *
     * @param msg Description of the Parameter
     * @return The autoCorrectionTags value
     */
    public static boolean isAutoCorrectionTags(String msg) {
        return msg.equals(YassRow.MISSING_TAG)
                || msg.equals(YassRow.UNSORTED_COMMENTS)
                || msg.equals(YassRow.WRONG_MEDLEY_START_BEAT)
                || msg.equals(YassRow.WRONG_MEDLEY_END_BEAT);
    }

    /**
     * Gets the autoCorrectionSpacing attribute of the YassAutoCorrect class
     *
     * @param msg Description of the Parameter
     * @return The autoCorrectionSpacing value
     */
    public static boolean isAutoCorrectionSpacing(String msg) {
        return msg.equals(YassRow.TOO_MUCH_SPACES)
                || msg.equals(YassRow.UNCOMMON_SPACING);
    }

    /**
     * Gets the golden attribute of the YassAutoCorrect class
     *
     * @param msg Description of the Parameter
     * @return The golden value
     */
    public static boolean isGolden(String msg) {
        return msg.equals(YassRow.UNCOMMON_GOLDEN);
    }

    /**
     * Gets the unhandledError attribute of the YassAutoCorrect class
     *
     * @param msg Description of the Parameter
     * @return The unhandledError value
     */
    public static boolean isUnhandledError(String msg) {
        if (isAutoCorrectionMinorPageBreak(msg) || isAutoCorrectionPageBreak(msg) || isAutoCorrectionSpacing(msg) || isGolden(msg))
            return false;
        return true;
    }

    /**
     * Gets the pause attribute of the YassAutoCorrect class
     *
     * @param in  Description of the Parameter
     * @param out Description of the Parameter
     * @param bpm Description of the Parameter
     * @return The pause value
     */
    public static double getPause(int in, int out, double bpm) {
        return Math.abs(out - in) * 60 / (4 * bpm);
    }

    /**
     * Gets the commonPageBreak attribute of the YassAutoCorrect class
     *
     * @param inout   Description of the Parameter
     * @param bpm     Description of the Parameter
     * @param inoutms Description of the Parameter
     * @return The commonPageBreak value
     */
    public static int getCommonPageBreak(int inout[], double bpm, double inoutms[]) {
        int pause = inout[1] - inout[0];
        if (pause < 0) {
            return -1;
        }

        double psec = pause * 60 / (4 * bpm);

        // FIXED_PAGE_BREAK is assured to be <= 10
        int f = 0;
        if (FIXED_PAGE_BREAK > 0) {
            f = inout[0] + FIXED_PAGE_BREAK;
            if (f > inout[1] - FIXED_PAGE_BREAK)
                f = inout[1] - FIXED_PAGE_BREAK;
            if (f < inout[0])
                f = inout[0];
        }
        if (psec >= 4) {
            if (FIXED_PAGE_BREAK > 0) {
                inout[0] = inout[1] = f;
            } else {
                int gap = (int) (2 * 4 * bpm / 60);
                inout[0] = inout[0] + gap;
                inout[1] = inout[0];
                if (inoutms != null) {
                    inoutms[0] = 2;
                    inoutms[1] = psec - 2;
                }
            }
            return 4;
        } else if (psec >= 2) {
            if (FIXED_PAGE_BREAK > 0) {
                inout[0] = inout[1] = f;
            } else {
                int gap = (int) (4 * bpm / 60);
                inout[0] = inout[0] + gap;
                inout[1] = inout[0];
                if (inoutms != null) {
                    inoutms[0] = 1;
                    inoutms[1] = psec - 1;
                }
            }
            return 2;
        }
        if (pause == 0 || pause == 1) {
            inout[1] = inout[0];
            if (inoutms != null) {
                inoutms[1] = pause * 60 / (4 * bpm);
                inoutms[0] = 0;
            }
            return 0;
        } else if (pause >= 2 && pause <= 8) {
            if (FIXED_PAGE_BREAK > 0) {
                inout[0] = inout[1] = f;
            } else {
                inout[0] = inout[1] - 2;
                inout[1] = inout[0];

                if (inoutms != null) {
                    inoutms[1] = 2 * 60 / (4 * bpm);
                    inoutms[0] = psec - inoutms[1];
                }
            }
            return 8;
        } else if (pause >= 9 && pause <= 12) {
            if (FIXED_PAGE_BREAK > 0) {
                inout[0] = inout[1] = f;
            } else {
                inout[0] = inout[1] - 3;
                inout[1] = inout[0];

                if (inoutms != null) {
                    double psec1 = (pause - 3) * 60 / (4 * bpm);
                    double psec2 = 3 * 60 / (4 * bpm);
                    inoutms[0] = psec1;
                    inoutms[1] = psec2;
                }
            }
            return 12;
        } else if (pause >= 13 && pause <= 16) {
            if (FIXED_PAGE_BREAK > 0) {
                inout[0] = inout[1] = f;
            } else {
                inout[0] = inout[1] - 4;
                inout[1] = inout[0];

                if (inoutms != null) {
                    double psec1 = 4 * 60 / (4 * bpm);
                    double psec2 = (pause - 4) * 60 / (4 * bpm);
                    inoutms[0] = psec1;
                    inoutms[1] = psec2;
                }
            }
            return 16;
        }
        // else if (pause>17) {
        if (FIXED_PAGE_BREAK > 0) {
            inout[0] = inout[1] = f;
        } else {
            inout[0] = inout[0] + 10;
            inout[1] = inout[0];

            if (inoutms != null) {
                double psec1 = 10 * 60 / (4 * bpm);
                double psec2 = (pause - 10) * 60 / (4 * bpm);
                inoutms[0] = psec1;
                inoutms[1] = psec2;
            }
        }
        return 18;
        // }
    }

    /**
     * Description of the Method
     *
     * @param t  Description of the Parameter
     * @param co Description of the Parameter
     */
    public static void insertCover(YassTable t, File co) {
        YassRow r2 = t.getCommentRow("COVER:");
        if (r2 != null) {
            r2.setComment(co.getName());
        } else {
            YassRow r3;
            YassTableModel tm = ((YassTableModel) t.getModel());
            Vector<YassRow> data = tm.getData();
            int j = 0;
            while ((r3 = tm.getRowAt(j)) != null && r3.isComment()) {
                j++;
                if (r3.getCommentTag().equals("MP3:")) {
                    break;
                }
            }
            data.insertElementAt(new YassRow("#", "COVER:", co.getName(), "", ""), j);
        }
    }

    /**
     * Description of the Method
     *
     * @param t  Description of the Parameter
     * @param bg Description of the Parameter
     */
    public static void insertBackground(YassTable t, File bg) {
        YassRow r2 = t.getCommentRow("BACKGROUND:");
        if (r2 != null) {
            r2.setComment(bg.getName());
        } else {
            YassRow r3;
            YassTableModel tm = ((YassTableModel) t.getModel());
            Vector<YassRow> data = tm.getData();
            int j = 0;
            int k = 0;
            while ((r3 = tm.getRowAt(j)) != null && r3.isComment()) {
                j++;
                if (r3.getCommentTag().equals("MP3:")) {
                    k = j;
                }
                if (r3.getCommentTag().equals("COVER:")) {
                    k = j;
                    break;
                }
            }
            if (k == 0) {
                k = j;
            }
            data.insertElementAt(new YassRow("#", "BACKGROUND:", bg.getName(), "", ""), k);
        }
    }

    /**
     * Description of the Method
     *
     * @param t  Description of the Parameter
     * @param vd Description of the Parameter
     */
    public static void insertVideo(YassTable t, File vd) {
        YassRow r2 = t.getCommentRow("VIDEO:");
        if (r2 != null) {
            r2.setComment(vd.getName());
        } else {
            YassRow r3;
            YassTableModel tm = ((YassTableModel) t.getModel());
            Vector<YassRow> data = tm.getData();
            int coverIndex = -1;
            int j = 0;
            while ((r3 = tm.getRowAt(j)) != null && r3.isComment()) {
                j++;
                if (r3.getCommentTag().equals("COVER:")) {
                    coverIndex = j;
                }
                if (r3.getCommentTag().equals("BACKGROUND:")) {
                    break;
                }
            }
            if (r3 != null && !r3.isComment()) {
                j = coverIndex + 1;
            }
            data.insertElementAt(new YassRow("#", "VIDEO:", vd.getName(), "", ""), j);
        }
    }

    /**
     * Description of the Method
     *
     * @param table Description of the Parameter
     */
    public static void sortComments(YassTable table) {
        int kk = 0;
        int n = table.getRowCount();
        YassRow rr = table.getRowAt(kk);
        while (kk < n && rr.isComment()) {
            kk++;
            rr = table.getRowAt(kk);
        }
        Vector<YassRow> rrv = new Vector<>(kk);
        for (int j = 0; j < kk; j++) {
            rrv.addElement(table.getRowAt(j).clone());
        }
        Collections.sort(rrv);
        for (int j = 0; j < kk; j++) {
            table.getRowAt(j).setRow(rrv.elementAt(j));
        }
    }

    /**
     * Description of the Method
     *  @param p Description of the Parameter
     *
     */
    public void init(YassProperties p) {
        prop = p;
        autoCorrectorMap = initAutoCorrectors();
        languageUtils = new YassLanguageUtils();
        YassRow.setValidTags(prop.getProperty("valid-tags"));
        YassRow.setValidLines(prop.getProperty("valid-lines"));
        loadFont();

        String ext = prop.getProperty("audio-files");
        StringTokenizer st = new StringTokenizer(ext, "|");
        int n = st.countTokens();
        audioExtensions = new String[n];
        for (int i = 0; i < n; i++) {
            audioExtensions[i] = st.nextToken().toLowerCase();
        }

        ext = prop.getProperty("image-files");
        st = new StringTokenizer(ext, "|");
        n = st.countTokens();
        imageExtensions = new String[n];
        for (int i = 0; i < n; i++) {
            imageExtensions[i] = st.nextToken().toLowerCase();
        }

        ext = prop.getProperty("video-files");
        st = new StringTokenizer(ext, "|");
        n = st.countTokens();
        videoExtensions = new String[n];
        for (int i = 0; i < n; i++) {
            videoExtensions[i] = st.nextToken().toLowerCase();
        }

        coverID = prop.getProperty("cover-id").toLowerCase();
        backgroundID = prop.getProperty("background-id").toLowerCase();
        videoID = prop.getProperty("video-id").toLowerCase();
    }

    /**
     * Description of the Method
     *
     * @param msg Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean autoCorrectionSupported(String msg) {
        return msg.equals(YassRow.EMPTY_LINE)
                || msg.equals(YassRow.UNCOMMON_SPACING)
                || msg.equals(YassRow.TOO_MUCH_SPACES)
                || msg.equals(YassRow.OUT_OF_ORDER)
                || msg.equals(YassRow.PAGE_OVERLAP)
                || msg.equals(YassRow.EARLY_PAGE_BREAK)
                || msg.equals(YassRow.LATE_PAGE_BREAK)
                || msg.equals(YassRow.SHORT_PAGE_BREAK)
                || msg.equals(YassRow.UNCOMMON_PAGE_BREAK)
                || msg.equals(YassRow.UNSORTED_COMMENTS)
                || msg.equals(YassRow.WRONG_MEDLEY_START_BEAT)
                || msg.equals(YassRow.WRONG_MEDLEY_END_BEAT)
                || msg.equals(YassRow.FILE_FOUND)
                || msg.equals(YassRow.NO_COVER_LABEL)
                || msg.equals(YassRow.NO_BACKGROUND_LABEL)
                || msg.equals(YassRow.NO_VIDEO_LABEL)
                || msg.equals(YassRow.WRONG_VIDEOGAP)
                || msg.equals(YassRow.TRANSPOSED_NOTES)
                || msg.equals(YassRow.INVALID_NOTE_LENGTH)
                || msg.equals(YassRow.MISSING_TAG)
                || msg.equals(YassRow.NOTES_TOUCHING)
                || msg.equals(YassRow.NONZERO_FIRST_BEAT)
                || msg.equals(YassRow.LOWERCASE_ROWSTART)
                || msg.equals(YassRow.BORING_APOSTROPHE);
    }

    // should return true if messages were added;
    // for now, only returns false if table is relative

    /**
     * Gets the autoCorrectionSafe attribute of the YassAutoCorrect object
     *
     * @param msg Description of the Parameter
     * @return The autoCorrectionSafe value
     */
    public boolean isAutoCorrectionSafe(String msg) {
        return msg.equals(YassRow.EMPTY_LINE)
                || msg.equals(YassRow.UNCOMMON_SPACING)
                || msg.equals(YassRow.TOO_MUCH_SPACES)
                || msg.equals(YassRow.UNSORTED_COMMENTS)
                || msg.equals(YassRow.WRONG_MEDLEY_START_BEAT)
                || msg.equals(YassRow.WRONG_MEDLEY_END_BEAT)
                || msg.equals(YassRow.PAGE_OVERLAP)
                || msg.equals(YassRow.EARLY_PAGE_BREAK)
                || msg.equals(YassRow.LATE_PAGE_BREAK)
                || msg.equals(YassRow.UNCOMMON_PAGE_BREAK)
                || msg.equals(YassRow.FILE_FOUND)
                || msg.equals(YassRow.NO_COVER_LABEL)
                || msg.equals(YassRow.NO_BACKGROUND_LABEL)
                || msg.equals(YassRow.NO_VIDEO_LABEL)
                || msg.equals(YassRow.WRONG_VIDEOGAP)
                || msg.equals(YassRow.TRANSPOSED_NOTES)
                || msg.equals(YassRow.INVALID_NOTE_LENGTH)
                || msg.equals(YassRow.MISSING_TAG)
                || msg.equals(YassRow.NOTES_TOUCHING)
                || msg.equals(YassRow.NONZERO_FIRST_BEAT)
                || msg.equals(YassRow.LOWERCASE_ROWSTART)
                || msg.equals(YassRow.BORING_APOSTROPHE);
    }

    /**
     * Description of the Method
     *
     * @param table Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean autoCorrectAllSafe(YassTable table) {
        int n = 0;
        boolean changed = true;
        boolean changedAny = false;
        while (changed && n++ < 20) {
            if (!checkData(table, true, true)) {
                return changedAny;
            }

            changed = false;
            for (int i = 0; i < YassRow.ALL_MESSAGES.length; i++) {
                if (isAutoCorrectionSafe(YassRow.ALL_MESSAGES[i])) {
                    if (autoCorrect(table, true, YassRow.ALL_MESSAGES[i])) {
                        changed = true;
                        changedAny = true;
                    }
                }
            }
        }
        return changedAny;
    }

    /**
     * Description of the Method
     *
     * @param table      Description of the Parameter
     * @param withMinors Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean autoCorrectAllPageBreaks(YassTable table, boolean withMinors) {
        // quick hack; should loop until no change is made
        if (!checkData(table, false, true)) {
            return false;
        }

        boolean match;
        int n = 1;
        boolean changed = true;
        boolean changedAny = true;
        while (changed && n++ < 20) {
            for (int i = 0; i < YassRow.ALL_MESSAGES.length; i++) {

                match = isAutoCorrectionPageBreak(YassRow.ALL_MESSAGES[i]);
                if (!match) {
                    match = withMinors
                            && isAutoCorrectionMinorPageBreak(YassRow.ALL_MESSAGES[i]);
                }

                if (match) {
                    changed = false;
                    if (autoCorrect(table, true, YassRow.ALL_MESSAGES[i])) {
                        changed = true;
                        changedAny = true;
                    }
                }
            }
        }
        return changedAny;
    }

    /**
     * Description of the Method
     *
     * @param table Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean autoCorrectTransposed(YassTable table) {
        if (!checkData(table, false, true)) {
            return false;
        }

        boolean changed = autoCorrect(table, true, YassRow.TRANSPOSED_NOTES);
        if (changed) {
            table.addUndo();
            ((YassTableModel) table.getModel()).fireTableDataChanged();
            return true;
        }

        return false;
    }

    /**
     * Description of the Method
     *
     * @param table Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean autoCorrectSpacing(YassTable table) {
        // quick hack; should loop until no change is made
        int n = 1;
        boolean changed = true;
        boolean changedAny = false;
        while (changed && n++ < 10) {
            changed = false;
            for (int i = 0; i < YassRow.ALL_MESSAGES.length; i++) {
                if (isAutoCorrectionSpacing(YassRow.ALL_MESSAGES[i])) {
                    if (!checkData(table, false, true)) {
                        return changedAny;
                    }
                    if (autoCorrect(table, true, YassRow.ALL_MESSAGES[i])) {
                        changed = true;
                        changedAny = true;
                    }
                }
            }
        }
        return changedAny;
    }

    /**
     * Description of the Method
     *
     * @param table Description of the Parameter
     * @param msg   Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean autoCorrectAllSafe(YassTable table, Vector<?> msg) {
        int n = 0;
        boolean changed = true;
        boolean changedAny = false;
        while (changed && n++ < 20) {
            if (!checkData(table, true, true)) {
                return changedAny;
            }

            changed = false;
            for (Enumeration<?> e = msg.elements(); e.hasMoreElements(); ) {
                String m = (String) e.nextElement();
                if (isAutoCorrectionSafe(m)) {
                    if (autoCorrect(table, true, m)) {
                        changed = true;
                        changedAny = true;
                    }
                }
            }
        }
        return changedAny;
    }

    /**
     * Description of the Method
     *
     * @param table              Description of the Parameter
     * @param checkHeaderAndText Description of the Parameter
     * @param checkExtensive     Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean checkData(YassTable table, boolean checkHeaderAndText, boolean checkExtensive) {
        table.resetMessages();

        String fixString = prop.getProperty("correct-uncommon-pagebreaks-fix");
        FIXED_PAGE_BREAK = fixString != null ? Integer.parseInt(fixString) : 0;

        YassRow currentRow = null;
        YassRow nextRow = null;
        try {
            YassTableModel tm = (YassTableModel) table.getModel();
            Vector<?> data = tm.getData();
            int n = data.size();
            String dir = table.getDir();
            boolean inHeader = true;
            int lastTagPos = -1;
            int tagPos;

            int durationNormal = 0;
            int durationGolden = 0;

            YassRow firstnormal = null;
            YassRow firstnote = null;
            YassRow firstgolden = null;
            YassRow lastnote = null;
            boolean end = false;
            boolean firstonpage = true;

            String freestyleCountsString = (String) prop
                    .get("freestyle-counts");
            boolean freestyleCounts = freestyleCountsString != null
                    && freestyleCountsString.equals("true");
            boolean touchingSyllables = isTouchingSyllables();
            for (int i = 0; i < n; i++) {
                currentRow = table.getRowAt(i);
                if (i + 1 < n) {
                    nextRow = table.getRowAt(i + 1);
                } else {
                    nextRow = null;
                }
                // @bug: shouldn't remove YassTable.addRow()-Messages (will
                // recheck them anyway)
                currentRow.removeAllMessages();
                String type = currentRow.getType();
                if (!YassRow.getValidLines().contains(type)) {
                    currentRow.addMessage(YassRow.INVALID_LINE);
                    table.addMessage(YassRow.INVALID_LINE);
                }

                if (currentRow.isEnd() && currentRow.getComment().length() > 0) {
                    currentRow.addMessage(YassRow.COMMENT_AFTER_END);
                    table.addMessage(YassRow.COMMENT_AFTER_END);
                }

                boolean isComment = currentRow.isComment();
                if (inHeader && !isComment) {
                    inHeader = false;
                }

                if (isComment) {
                    if (!checkHeaderAndText) {
                        continue;
                    }

                    String tag = currentRow.getCommentTag();
                    int tagLength = tag.length();
                    int commentLength = currentRow.getComment().length();

                    if (tagLength < 1 && commentLength < 1) {
                        currentRow.addMessage(YassRow.EMPTY_LINE);
                        table.addMessage(YassRow.EMPTY_LINE);
                        continue;
                    }
                    tagPos = YassRow.getValidTags().indexOf(" " + tag.substring(0, tagLength - 1) + " ");
                    if (commentLength > 0 && tagPos < 0) {
                        currentRow.addMessage(YassRow.INVALID_TAG);
                        table.addMessage(YassRow.INVALID_TAG);
                    } else if (!inHeader) {
                        currentRow.addMessage(YassRow.OUT_OF_ORDER_COMMENT);
                        table.addMessage(YassRow.OUT_OF_ORDER_COMMENT);
                    } else if (tag.equals("TITLE:")) {
                        checkTitleRelevantErros(table, currentRow, tm, dir);
                    } else if (tag.equals("MP3:")) {
                        String filename = currentRow.getComment();
                        if (!new File(dir + File.separator + filename).exists()) {
                            File mp3 = YassUtils.getFileWithExtension(dir, null, audioExtensions);
                            if (mp3 != null) {
                                currentRow.addMessage(YassRow.FILE_FOUND, MessageFormat
                                        .format(I18.get("correct_file_not_found"), filename, mp3));
                                table.addMessage(YassRow.FILE_FOUND);
                            } else {
                                currentRow.addMessage(YassRow.FILE_NOT_FOUND, filename);
                                table.addMessage(YassRow.FILE_NOT_FOUND);
                            }
                        }
                    } else if (tag.equals("COVER:")) {
                        String filename = currentRow.getComment();
                        if (!new File(dir + File.separator + filename).exists()) {
                            File co = YassUtils.getFileWithExtension(dir, coverID, imageExtensions);
                            if (co != null) {
                                currentRow.addMessage(YassRow.FILE_FOUND, MessageFormat
                                        .format(I18.get("correct_file_not_found"), filename, co));
                                table.addMessage(YassRow.FILE_FOUND);
                            } else {
                                currentRow.addMessage(YassRow.FILE_NOT_FOUND, filename);
                                table.addMessage(YassRow.FILE_NOT_FOUND);
                            }
                        }
                    } else if (tag.equals("BACKGROUND:")) {
                        String filename = currentRow.getComment();
                        if (!new File(dir + File.separator + filename).exists()) {
                            File bg = YassUtils.getFileWithExtension(dir, backgroundID, imageExtensions);
                            if (bg != null) {
                                currentRow.addMessage(YassRow.FILE_FOUND, MessageFormat
                                        .format(I18.get("correct_file_not_found"), filename, bg));
                                table.addMessage(YassRow.FILE_FOUND);
                            } else {
                                currentRow.addMessage(YassRow.FILE_NOT_FOUND, filename);
                                table.addMessage(YassRow.FILE_NOT_FOUND);
                            }
                        }
                    } else if (tag.equals("VIDEO:")) {
                        String filename = currentRow.getComment();
                        if (!new File(dir + File.separator + filename).exists()) {
                            File vd = YassUtils.getFileWithExtension(dir, videoID, videoExtensions);
                            if (vd != null) {
                                currentRow.addMessage(YassRow.FILE_FOUND, MessageFormat
                                        .format(I18.get("correct_file_not_found"), filename, vd));
                                table.addMessage(YassRow.FILE_FOUND);
                            } else {
                                currentRow.addMessage(YassRow.FILE_NOT_FOUND, filename);
                                table.addMessage(YassRow.FILE_NOT_FOUND);
                            }
                        } else {
                            String vg = YassUtils.getWildcard(filename, videoID);
                            double oldvgap = table.getVideoGap();
                            if (vg != null) {
                                vg = vg.replace(',', '.');
                                double vgap = Double.parseDouble(vg);

                                if (tm.getCommentRow("VIDEOGAP:") == null) {
                                    currentRow.addMessage(
                                            YassRow.WRONG_VIDEOGAP, MessageFormat.format(I18.get("correct_wrong_videogap_1"), vgap + ""));
                                    table.addMessage(YassRow.WRONG_VIDEOGAP);
                                } else if (vgap != oldvgap) {
                                    currentRow.addMessage(
                                            YassRow.WRONG_VIDEOGAP, MessageFormat.format(I18.get("correct_wrong_videogap_2"), vgap + "", oldvgap + ""));
                                    table.addMessage(YassRow.WRONG_VIDEOGAP);
                                }
                            }
                        }
                    } else if (tag.equals("MEDLEYSTARTBEAT:")) {
                        String medleyStartString = currentRow.getComment();
                        int medleyStart = -1;
                        try {
                            medleyStart = Integer.parseInt(medleyStartString);
                        } catch(Exception e) {}
                        if (medleyStart >= 0)
                        {
                           if (table.getNoteAtBeat(medleyStart) == null) {
                               YassRow r2 = table.getNoteBeforeBeat(medleyStart);
                               if (r2 != null) {
                                   currentRow.addMessage(YassRow.WRONG_MEDLEY_START_BEAT, MessageFormat.format(I18.get("correct_wrong_medley_start"), medleyStartString, r2.getBeatInt()+""));
                                   table.addMessage(YassRow.WRONG_MEDLEY_START_BEAT);
                               }
                           }
                        }
                    } else if (tag.equals("MEDLEYENDBEAT:")) {
                        String medleyEndString = currentRow.getComment();
                        int medleyEnd = -1;
                        try {
                            medleyEnd = Integer.parseInt(medleyEndString);
                        } catch(Exception e) {}
                        if (medleyEnd > 0)
                        {
                            if (table.getNoteEndingAtBeat(medleyEnd) == null) {
                                YassRow r2 = table.getNoteEndingBeforeBeat(medleyEnd);
                                if (r2 != null) {
                                    currentRow.addMessage(YassRow.WRONG_MEDLEY_END_BEAT, MessageFormat.format(I18.get("correct_wrong_medley_end"), medleyEndString, (r2.getBeatInt() + r2.getLengthInt())+""));
                                    table.addMessage(YassRow.WRONG_MEDLEY_END_BEAT);
                                }
                            }
                        }
                    }
                    if (tagPos < lastTagPos) {
                        currentRow.addMessage(YassRow.UNSORTED_COMMENTS);
                        table.addMessage(YassRow.UNSORTED_COMMENTS);
                    }
                    lastTagPos = tagPos;
                } else if (currentRow.isNote()) {
                    lastnote = currentRow;

                    if (firstnote == null) {
                        firstnote = currentRow;
                        int beat = currentRow.getBeatInt();
                        double gap = table.getGap();
                        double bpm = table.getBPM();
                        if ((beat != 0 && table.getDuetTrack() < 2) || (beat < 0 && table.getDuetTrack() > 1)) {
                            double ms = beat * (60 * 1000) / (4 * bpm);
                            double newgap = gap + ms;
                            newgap = ((int) (newgap * 100)) / 100.0;
                            currentRow.addMessage(YassRow.NONZERO_FIRST_BEAT, MessageFormat.format(I18.get("correct_nonzero_first_beat"), beat + "", gap + "", newgap + ""));
                            table.addMessage(YassRow.NONZERO_FIRST_BEAT);
                        }
                    }

                    if (currentRow.isGolden() || currentRow.isRapGolden()) {
                        if (firstgolden == null) {
                            firstgolden = currentRow;
                        }
                        durationGolden += currentRow.getLengthInt();
                    } else {
                        if (firstnormal == null) {
                            firstnormal = currentRow;
                        }
                        if (!currentRow.isFreeStyle() || freestyleCounts) {
                            durationNormal += currentRow.getLengthInt();
                        }
                    }

                    if (currentRow.getBeat().length() < 1 || currentRow.getLength().length() < 1
                            || currentRow.getText().length() < 1) {
                        currentRow.addMessage(YassRow.LINE_CUT);
                        table.addMessage(YassRow.LINE_CUT);
                        continue;
                    }
                    if (currentRow.getLengthInt() < 1) {
                        currentRow.addMessage(YassRow.INVALID_NOTE_LENGTH);
                        table.addMessage(YassRow.INVALID_NOTE_LENGTH);
                        continue;
                    }
                    String txt = currentRow.getText();
                    boolean startswithspace = txt
                            .startsWith(YassRow.SPACE + "");
                    if (txt.contains(YassRow.SPACE + "" + YassRow.SPACE)) {
                        currentRow.addMessage(YassRow.TOO_MUCH_SPACES);
                        table.addMessage(YassRow.TOO_MUCH_SPACES);
                    } else if (isUncommonSpacing(currentRow, nextRow)) {
                        currentRow.addMessage(YassRow.UNCOMMON_SPACING);
                        table.addMessage(YassRow.UNCOMMON_SPACING);
                    } else if (firstonpage || startswithspace) {
                        if (i > 0) {
                            YassRow r2 = table.getRowAt(i - 1);
                            if (r2.isPageBreak() && i > 1) {
                                r2 = table.getRowAt(i - 2);
                            }
                            if (r2.isNote()) {
                                String txt2 = r2.getText();
                                if (startswithspace && txt2.endsWith(YassRow.SPACE + "")) {
                                    currentRow.addMessage(YassRow.TOO_MUCH_SPACES);
                                    table.addMessage(YassRow.TOO_MUCH_SPACES);
                                }

                                int beat = currentRow.getBeatInt();
                                int beat2 = r2.getBeatInt() + r2.getLengthInt();
                                if (beat == beat2 && !touchingSyllables
                                        && r2.getLengthInt() > 1) {
                                    r2.addMessage(YassRow.NOTES_TOUCHING);
                                    table.addMessage(YassRow.NOTES_TOUCHING);
                                }
                            } else if (startswithspace) {
                                currentRow.addMessage(YassRow.TOO_MUCH_SPACES);
                                table.addMessage(YassRow.TOO_MUCH_SPACES);
                            }
                            checkUppercase(currentRow, table);
                        }
                    }
                    if (touchingSyllables) {
                        if (i > 0) {
                            YassRow r2 = table.getRowAt(i - 1);
                            if (r2.isPageBreak() && i > 1) {
                                r2 = table.getRowAt(i - 2);
                            }
                            if (r2.isNote()) {
                                int beat = currentRow.getBeatInt();
                                int beat2 = r2.getBeatInt() + r2.getLengthInt();
                                if (beat == beat2 && r2.getLengthInt() > 1) {
                                    r2.addMessage(YassRow.NOTES_TOUCHING);
                                    table.addMessage(YassRow.NOTES_TOUCHING);
                                }
                            }
                        }
                    }
                    if (checkExtensive) {
                        YassRow r2 = null;
                        if (i > 0) {
                            r2 = table.getRowAt(i - 1);
                        }
                        if (i > 0 && r2.isComment()) {
                            int minH = 128;
                            int maxH = 0;
                            YassRow r3;
                            for (int j = 0; j < n; j++) {
                                r3 = table.getRowAt(j);
                                if (r3.isNote()) {
                                    int height = r3.getHeightInt();
                                    minH = Math.min(minH, height);
                                    maxH = Math.max(maxH, height);
                                }
                            }
                            int range = maxH - minH;
                            if (minH >= 12
                                    || (range <= 48 && (minH < -12 || maxH > 36))) {
                                int minHd = minH / 12 * 12;
                                int bias = minH - minHd;
                                int newMax = maxH - minH + bias;
                                currentRow.addMessage(YassRow.TRANSPOSED_NOTES, MessageFormat.format(I18.get("correct_transposed"), minH, maxH, bias, newMax));
                                table.addMessage(YassRow.TRANSPOSED_NOTES);
                            }
                        }
                        if (i > 0 && (!(r2.isNote()))) {
                            int ij[];
                            ij = table.enlargeToPages(i, i);
                            StringBuilder sb = new StringBuilder();
                            while (ij[0] <= ij[1]) {
                                r2 = table.getRowAt(ij[0]);
                                if (r2.isNote()) {
                                    sb.append(r2.getText().replace(
                                            YassRow.SPACE, ' '));
                                }
                                ij[0]++;
                            }
                            String s = sb.toString();
                            double percentFree = getPageSpace(s);
                            if (percentFree < 0) {
                                String font = prop.getProperty("font-file-custom").trim() != ""
                                        ? prop.getProperty("font-file-custom")
                                        : prop.getProperty("font-file");

                                // System.out.println(percentFree + " outside");
                                int pf = -(int) (percentFree * 100);
                                if (pf == 0) {
                                    currentRow.addMessage(
                                            YassRow.TOO_MUCH_TEXT, MessageFormat.format(I18.get("correct_too_much_text_1"), font));
                                } else {
                                    currentRow.addMessage(
                                            YassRow.TOO_MUCH_TEXT, MessageFormat.format(I18.get("correct_too_much_text_2"), font, pf));
                                }
                                table.addMessage(YassRow.TOO_MUCH_TEXT);
                            }
                        }
                    }
                    checkNiceApostrophes(currentRow, table);

                    int beat = currentRow.getBeatInt();
                    if (i > 0) {
                        YassRow r2 = getPreviousNote(table, i);
                        if (r2.isNote()) {
                            int beat2 = r2.getBeatInt();
                            int dur2 = r2.getLengthInt();
                            if (beat2 > beat) {
                                currentRow.addMessage(YassRow.OUT_OF_ORDER);
                                table.addMessage(YassRow.OUT_OF_ORDER);
                            } else if (beat2 + dur2 > beat) {
                                currentRow.addMessage(YassRow.NOTES_OVERLAP);
                                table.addMessage(YassRow.NOTES_OVERLAP);
                            }
                        }
                    }
                    firstonpage = false;
                } else if (currentRow.isPageBreak()) {
                    // check & autocorrect EARLY, LATE, UNCOMMON, OVERLAPPING page breaks
                    int beat = currentRow.getBeatInt();

                    YassRow r2 = (i > 0) ? table.getRowAt(i - 1) : null;
                    YassRow r3 = (i < n - 1) ? table.getRowAt(i + 1) : null;

                    if (r2 != null && r3 != null) {
                        int beat2 = currentRow.getSecondBeatInt();
                        int comm[] = new int[]{0, 0};
                        if (r2.isNote()) {
                            comm[0] = r2.getBeatInt() + r2.getLengthInt();
                        }
                        if (r3.isNote()) {
                            comm[1] = r3.getBeatInt();
                            firstonpage = true;
                        }
                        if (comm[0] != 0 && comm[1] != 0) {
                            if (beat < comm[0] || beat2 > comm[1]) {
                                currentRow.addMessage(YassRow.PAGE_OVERLAP);
                                table.addMessage(YassRow.PAGE_OVERLAP);
                            }
                            boolean early = getPause(comm[0], beat, table.getBPM()) < .05;
                            boolean late = getPause(beat2, comm[1], table.getBPM()) < .05;

                            double ms[] = new double[2];
                            int ptype = getCommonPageBreak(comm, table.getBPM(), ms);
                            boolean canchange = (ptype >= 0)
                                    && (beat != comm[0] || beat2 != comm[1]);

                            String key = FIXED_PAGE_BREAK > 0 ? "correct_pause_fix_"
                                    + ptype
                                    : "correct_pause_" + ptype;
                            String details = ptype < 0 ? "" : MessageFormat
                                    .format(I18.get(key),
                                            ((int) (ms[0] * 100)) / 100.0,
                                            (((int) (ms[1] * 100)) / 100.0), FIXED_PAGE_BREAK);

                            if (early) {
                                if (canchange) {
                                    currentRow.addMessage(YassRow.EARLY_PAGE_BREAK, details);
                                    table.addMessage(YassRow.EARLY_PAGE_BREAK);
                                }
                                else if (r2.isNote() && r2.getLengthInt() > 1) {
                                    r2.addMessage(YassRow.SHORT_PAGE_BREAK, details);
                                    table.addMessage(YassRow.SHORT_PAGE_BREAK);
                                }
                            } else if (late) {
                                if (canchange) {
                                    currentRow.addMessage(YassRow.LATE_PAGE_BREAK, details);
                                    table.addMessage(YassRow.LATE_PAGE_BREAK);
                                }
                                else if (r2.isNote() && r2.getLengthInt() > 1){
                                    r2.addMessage(YassRow.SHORT_PAGE_BREAK, details);
                                    table.addMessage(YassRow.SHORT_PAGE_BREAK);
                                }
                            } else if (canchange) {
                                currentRow.addMessage(YassRow.UNCOMMON_PAGE_BREAK, details);
                                table.addMessage(YassRow.UNCOMMON_PAGE_BREAK);
                            }
                        }
                    }
                } else if (currentRow.isEnd()) {
                    end = true;
                }
            }

            if (!end) {
                table.addMessage(YassRow.MISSING_END);
                if (lastnote != null) {
                    lastnote.addMessage(YassRow.MISSING_END);
                }
            }

            int idealGoldenPoints = Integer.parseInt(prop.getProperty("max-golden"));
            int maxPoints = Integer.parseInt(prop.getProperty("max-points"));
            String goldenVarianceString = prop.getProperty("golden-allowed-variance");
            int goldenVariance = goldenVarianceString != null ? Integer.parseInt(goldenVarianceString)
                    : 0;
            maxPoints += idealGoldenPoints;

            // The maximum score is 10.000. Maximum phrase bonus is 1.000. Notes span one or more beats, points are given per beat.
            // Golden notes give double points. Normally you want the maximum golden notes to be near about 1.000.
            // That defines this formula:
            // Golden / MaxScore = 2*SumOfGoldenBeats / (SumOfNormalBeats + 2*SumOfGoldenBeats)
            // Example: Golden=1000, MaxScore=8000, TotalBeats=100 --> SumOfGoldenBeats=6

            int idealGoldenBeats = Math.round(idealGoldenPoints * durationNormal / (2 * maxPoints - 2 * idealGoldenPoints));

            int goldenPoints = durationNormal + 2 * durationGolden > 0 ? Math.round(maxPoints * 2 * durationGolden / (durationNormal + 2 * durationGolden)) : 0;

            String diff = idealGoldenBeats > durationGolden ? "+"
                    + (idealGoldenBeats - durationGolden) : ""
                    + (idealGoldenBeats - durationGolden);

            if (Math.abs(goldenPoints - idealGoldenPoints) > goldenVariance) {
                String key = "correct_golden";
                String details = MessageFormat.format(I18.get(key), "" + idealGoldenPoints, ""
                        + goldenPoints, "" + idealGoldenBeats, "" + durationGolden, diff);

                if (firstgolden == null) {
                    firstgolden = firstnormal;
                }
                if (firstgolden != null) {
                    firstgolden.addMessage(YassRow.UNCOMMON_GOLDEN, details);
                    table.addMessage(YassRow.UNCOMMON_GOLDEN);
                }
            }
            table.setGoldenPoints(goldenPoints, idealGoldenPoints, goldenVariance, durationGolden, idealGoldenBeats, diff);

        } catch (Throwable th) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            th.printStackTrace(pw);
            JOptionPane.showMessageDialog(
                    JOptionPane.getFrameForComponent(table), "<html>"
                            + MessageFormat.format(I18.get("correct_parse_error_msg"), table.getDir(), th.getMessage(), currentRow.toString(), sw.toString()), I18.get("correct_parse_error_title"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void checkNiceApostrophes(YassRow currentRow, YassTable table) {
        if (!niceApostrophes()) {
            return;
        }
        String txt = currentRow.isComment() ? currentRow.getComment() : currentRow.getText();
        boolean containsBoringApostrophe = YassAutoCorrectApostrophes.BORING_APOSTROPHES.stream()
                                                                                        .anyMatch(txt::contains);
        if (containsBoringApostrophe) {
            currentRow.addMessage(YassRow.BORING_APOSTROPHE);
            table.addMessage(YassRow.BORING_APOSTROPHE);
        }
    }

    private void checkUppercase(YassRow currentRow, YassTable table) {
        if (!linesStartUppercase() || StringUtils.isEmpty(currentRow.getText()) || currentRow.getText().length() < 1) {
            return;
        }
        String text = currentRow.getText();
        String first = text.substring(0, 1);
        if (YassUtils.isPunctuation(first) && text.length() > 1) {
            first = text.substring(1, 2);
        }
        if (StringUtils.isAllLowerCase(first)) {
            currentRow.addMessage(YassRow.LOWERCASE_ROWSTART);
            table.addMessage(YassRow.LOWERCASE_ROWSTART);
        }
    }

    private void checkTitleRelevantErros(YassTable table, YassRow currentRow, YassTableModel tm, String dir) {
        if (currentRow.getCommentTag().equals("TITLE:")) {
            checkNiceApostrophes(currentRow, table);
        }
        YassRow r2 = tm.getCommentRow("MP3:");
        if (r2 == null) {
            File mp3 = YassUtils.getFileWithExtension(dir, null, audioExtensions);
            if (mp3 != null) {
                currentRow.addMessage(YassRow.FILE_FOUND, I18.get("correct_add_tag") + " " + mp3);
                table.addMessage(YassRow.FILE_FOUND);
            }
        }
        r2 = tm.getCommentRow("COVER:");
        if (r2 == null) {
            File co = YassUtils.getFileWithExtension(dir, coverID, imageExtensions);
            if (co != null) {
                currentRow.addMessage(YassRow.FILE_FOUND, I18.get("correct_add_tag") + " " + co);
                table.addMessage(YassRow.FILE_FOUND);
            }
        }
        r2 = tm.getCommentRow("BACKGROUND:");
        if (r2 == null) {
            File bg = YassUtils.getFileWithExtension(dir, backgroundID, imageExtensions);
            if (bg != null) {
                currentRow.addMessage(YassRow.FILE_FOUND, I18.get("correct_add_tag") + " " + bg);
                table.addMessage(YassRow.FILE_FOUND);
            }
        }
        r2 = tm.getCommentRow("VIDEO:");
        if (r2 == null) {
            File vd = YassUtils.getFileWithExtension(dir, videoID, videoExtensions);
            if (vd != null) {
                currentRow.addMessage(YassRow.FILE_FOUND, I18.get("correct_add_tag") + " " + vd);
                table.addMessage(YassRow.FILE_FOUND);
            }
        }
        r2 = tm.getCommentRow("LANGUAGE:");
        if (r2 == null) {
            currentRow.addMessage(YassRow.MISSING_TAG, I18.get("correct_add_language"));
            table.addMessage(YassRow.MISSING_TAG);
        }
        r2 = tm.getCommentRow("GENRE:");
        if (r2 == null) {
            currentRow.addMessage(YassRow.MISSING_TAG, I18.get("correct_add_genre"));
            table.addMessage(YassRow.MISSING_TAG);
        }
        r2 = tm.getCommentRow("ARTIST:");
        if (r2 != null) {
            checkNiceApostrophes(r2, table);
        }
    }

    private YassRow getPreviousNote(YassTable table, int i) {
        YassRow previous = table.getRowAt(i - 1);
        if (previous.isNote()) {
            return previous;
        }
        if (i > 2) {
            return table.getRowAt(i - 2);
        }
        return previous;
    }

    private boolean isUncommonSpacing(YassRow currentRow, YassRow nextRow) {
        boolean isUncommonSpacingAfter = prop.isUncommonSpacingAfter();
        if (isUncommonSpacingAfter) {
            if (currentRow.startsWithSpace()) {
                return true;
            }
            if (!currentRow.endsWithSpace()) {
                return (nextRow != null && nextRow.startsWithSpace()) || nextRow == null || !nextRow.isNote();
            }
        } else {
            return currentRow.endsWithSpace();
        }
        return false;
    }

    /**
     * Auto-Corrects based on the message of the found issue.
     *
     * @param table          Description of the Parameter
     * @param all            Description of the Parameter
     * @param currentMessage Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean autoCorrect(YassTable table, boolean all, String currentMessage) {
        // correct messages until data is inserted/removed

        String dir = table.getDir();

        int rows[];
        int n;

        if (all) {
            n = table.getRowCount();
            rows = new int[n];
            for (int i = 0; i < n; i++) {
                rows[i] = i;
            }
        } else {
            rows = table.getSelectedRows();
            n = rows.length;
            Arrays.sort(rows);
        }

        boolean changed = false;
        YassTableModel tm = (YassTableModel) table.getModel();
        Vector<?> data = tm.getData();
        YassAutoCorrector autoCorrector = autoCorrectorMap.get(currentMessage);
        for (int k = 0; k < n; k++) {
            int i = rows[k];
            YassRow r = table.getRowAt(i);
            Vector<?> msg = r.getMessages();
            if (msg == null || msg.size() < 1) {
                continue;
            }
            boolean found = false;
            for (Enumeration<?> en = msg.elements(); en.hasMoreElements()
                    && !found; ) {
                String[] m = (String[]) en.nextElement();
                if (currentMessage.equals(m[0])) {
                    found = true;
                }
            }
            if (!found) {
                continue;
            }
            if (autoCorrector != null) {
                changed = autoCorrector.autoCorrect(table, i, n) || changed;
                continue;
            }
            if (currentMessage.equals(YassRow.UNSORTED_COMMENTS)) {
                sortComments(table);
                return true;
            }
            if (currentMessage.equals(YassRow.WRONG_MEDLEY_START_BEAT)) {
                int medleyStart = Integer.parseInt(r.getComment());
                YassRow r2 = table.getNoteBeforeBeat(medleyStart);
                table.setMedleyStartBeat(r2.getBeatInt());
                return true;
            }
            if (currentMessage.equals(YassRow.WRONG_MEDLEY_END_BEAT)) {
                int medleyEnd = Integer.parseInt(r.getComment());
                YassRow r2 = table.getNoteEndingBeforeBeat(medleyEnd);
                table.setMedleyEndBeat(r2.getBeatInt() + r2.getLengthInt());
                return true;
            }
            if (currentMessage.equals(YassRow.TRANSPOSED_NOTES)) {
                int minH = 128;
                int maxH = 0;
                YassRow r3;
                n = table.getRowCount();
                for (int j = 0; j < n; j++) {
                    r3 = table.getRowAt(j);
                    if (r3.isNote()) {
                        int height = r3.getHeightInt();
                        minH = Math.min(minH, height);
                        maxH = Math.max(maxH, height);
                    }
                }
                int minHd = minH / 12 * 12;
                int bias = minH - minHd;
                for (int j = 0; j < n; j++) {
                    r3 = table.getRowAt(j);
                    if (r3.isNote()) {
                        int height = r3.getHeightInt();
                        height = height - minH + bias;
                        r3.setHeight(height);
                    }
                }
                return true;
            }
            if (currentMessage.equals(YassRow.NONZERO_FIRST_BEAT)) {
                int beat = r.getBeatInt();
                table.setGapByBeat(beat);
                return true;
            }
            switch (currentMessage) {
                case YassRow.EMPTY_LINE:
                    tm.getData().remove(r);
                    return true;
                case YassRow.FILE_FOUND:
                    String tag = r.getCommentTag();

                    boolean isTitle = tag.equals("TITLE:");
                    if (isTitle || tag.equals("MP3:")) {
                        File f = YassUtils.getFileWithExtension(dir, null, audioExtensions);
                        if (f != null) {
                            table.setMP3(f.getName());
                            changed = true;
                        }
                    }
                    if (isTitle || tag.equals("COVER:")) {
                        File f = YassUtils.getFileWithExtension(dir, coverID, imageExtensions);
                        if (f != null) {
                            table.setCover(f.getName());
                            changed = true;
                        }
                    }
                    if (isTitle || tag.equals("BACKGROUND:")) {
                        File f = YassUtils.getFileWithExtension(dir, backgroundID, imageExtensions);
                        if (f != null) {
                            table.setBackground(f.getName());
                            changed = true;
                        }
                    }
                    if (isTitle || tag.equals("VIDEO:")) {
                        File f = YassUtils.getFileWithExtension(dir, videoID, imageExtensions);
                        if (f != null) {
                            table.setVideo(f.getName());
                            changed = true;
                        }
                    }
                    if (isTitle) {
                        n = data.size();
                    }
                    return true;
                case YassRow.MISSING_TAG:
                    YassRow r2 = tm.
                            getCommentRow("LANGUAGE:");
                    if (r2 == null) {
                        table.setLanguage(languageUtils.detectLanguage(table.getText()));
                        changed = true;
                    }
                    r2 = tm.getCommentRow("GENRE:");
                    if (r2 == null) {
                        table.setGenre("Other");
                        changed = true;
                    }
                    if (changed) {
                        return true;
                    }
                    break;
                case YassRow.WRONG_VIDEOGAP:
                    // msg set on video tag, so comment is filename
                    String filename = r.getComment();
                    String vg = YassUtils.getWildcard(filename, videoID);
                    if (vg != null) {
                        table.setVideoGap(vg);
                    }
                    return true;
                case YassRow.INVALID_NOTE_LENGTH:
                    if (r.isNote()) {
                        r.setLength(1);
                        changed = true;
                    }
                    break;
                case YassRow.NOTES_TOUCHING:
                    if (r.isNote()) {
                        int len = r.getLengthInt();
                        if (len > 1) {
                            r.setLength(len - 1);
                            changed = true;
                        }
                    }
                    break;
                case YassRow.TOO_MUCH_SPACES:
                    if (r.isNote()) {
                        String txt = r.getText();
                        int j = 0;
                        while ((j = txt.indexOf(YassRow.SPACE + "" + YassRow.SPACE)) > 0) {
                            txt = txt.substring(0, j) + txt.substring(j + 1);
                        }
                        while (txt.startsWith(YassRow.SPACE + "" + YassRow.SPACE)) {
                            txt = txt.substring(1);
                        }
                        if (txt.startsWith(YassRow.SPACE + "") && i > 0) {
                            YassRow r3 = table.getRowAt(i - 1);
                            if (!(r3.isNote())) {
                                txt = txt.substring(1);
                            }
                        }
                        r.setText(txt);
                        changed = true;
                    }
                    break;
                case YassRow.OUT_OF_ORDER:
                    int beat = r.getBeatInt();
                    int j = i - 1;
                    int beat2 = table.getRowAt(j).getBeatInt();
                    while (beat > beat2) {
                        beat2 = table.getRowAt(--j).getBeatInt();
                    }
                    r.setBeat(beat2 + table.getRowAt(j).getLengthInt());
                    changed = true;
                    break;
                case YassRow.PAGE_OVERLAP:
                case YassRow.EARLY_PAGE_BREAK:
                case YassRow.LATE_PAGE_BREAK:
                case YassRow.UNCOMMON_PAGE_BREAK: {
                    int comm[] = new int[2];
                    comm[0] = table.getRowAt(i - 1).getBeatInt()
                            + table.getRowAt(i - 1).getLengthInt();
                    comm[1] = table.getRowAt(i + 1).getBeatInt();
                    int pause = getCommonPageBreak(comm, table.getBPM(), null);
                    if (pause >= 0) {
                        r.setBeat(comm[0]);
                        r.setSecondBeat(comm[1]);
                        changed = true;
                    }
                    break;
                }
                case YassRow.SHORT_PAGE_BREAK: {
                    int comm[] = new int[2];
                    comm[0] = r.getBeatInt()
                            + r.getLengthInt() - 1;
                    comm[1] = table.getRowAt(i + 2).getBeatInt();
                    int pause = getCommonPageBreak(comm, table.getBPM(), null);
                    if (pause >= 0) {
                        // reduce note length, then auto-correct page break
                        r.setLength((r.getLengthInt() - 1));
                        YassRow r1 = table.getRowAt(i + 1);
                        r1.setBeat(comm[0]);
                        r1.setSecondBeat(comm[1]);
                        changed = true;
                    }
                    break;
                }
            }
        }
        return changed;
    }

    /**
     * Gets the pageSpace attribute of the YassAutoCorrect object
     *
     * @param s Description of the Parameter
     * @return The pageSpace value
     */
    public double getPageSpace(String s) {
        return (800 - getStringWidth(s)) / 800.0;
        // Render width is 800
        // Screen width might be 600, 800, 1024
    }

    /**
     * Gets the stringWidth attribute of the YassAutoCorrect object
     *
     * @param s Description of the Parameter
     * @return The stringWidth value
     */
    public int getStringWidth(String s) {
        double stringWidth = 0;
        for (char aC : s.toCharArray()) {
            int ascii = (int) (aC);
            if (ascii > 255) {
                // replace left/right/low single quotation marks: ‘’‚
                if (ascii == 8216 || ascii == 8218)
                    ascii = (int) '\'';
                // replace right/left/low double quotation marks: “”„
                else if (ascii >= 8220 && ascii <= 8222)
                    ascii = (int) '\'';
                // replace horizontal ellipsis '...'
                else if (ascii == 8230)
                    ascii = (int) 'w';
                // replace any other non-ascii char
                else
                    ascii = (int) 'W';
            }
            stringWidth += fontWidth[ascii] * fontSize / 256.0 + charSpacing;
        }
        //System.out.println(s.substring(0,5) + " = " + (int)stringWidth + " px" + "  " + (int)(100*(stringWidth)/800.0));
        return (int) stringWidth;
    }


    /**
     * Description of the Method
     *
     */
    public void loadFont() {
        fontSize=28;
        try {
            fontSize = Integer.parseInt(prop.getProperty("font-size"));
        } catch(Exception e) { e.printStackTrace(); }
        charSpacing = 0;
        try {
            charSpacing = Integer.parseInt(prop.getProperty("char-spacing"));
        } catch(Exception e) { e.printStackTrace(); }
        String font = prop.getProperty("font-file-custom").trim().length() > 0
                ? prop.getProperty("font-file-custom")
                : prop.getProperty("font-file");

        if (! new File(font).exists())
            loadFontWidths(font);
        else
        {
            loadTTFontWidths(font);
        }
    }

    public void loadFontWidths(String font) {
        String s = null;
        try {
            InputStream is = getClass().getResourceAsStream("/yass/resources/fonts/"+ font + ".txt");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1)
                os.write(buffer, 0, length);
            s = os.toString("UTF-8");
            is.close();
        } catch (Exception e) {
            System.out.println("Font file not found: " + font);
            e.printStackTrace();
        }

        fontWidth = new int[256];
        String[] stokens = s.split(" ");
        int i = 32;
        for (String st: stokens)
        {
            if (! st.startsWith("["))
            {
                //System.out.println((i) + " = " + st);
                fontWidth[i] = Integer.parseInt(st);
                i++;
                if (i>255) break;
            }
        }
    }

    private void loadTTFontWidths(String font)
    {
        fontWidth = new int[256];
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(font));
            Font f = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont((float) 256);

            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics g = img.getGraphics();
            g.setFont(f);
            int[] fw2 = g.getFontMetrics().getWidths();
            String s = "";
            for (int i = 0; i < 256; i++) {
                fontWidth[i] = fw2[i];
                String c = ""+(char) i;
                if (i==32) c = "space";
                if (i==160) c= "nbsp";
                if (i==173) c= "shy";
                if (i>=32) s += " ["+i+"]["+c+"] "+ fw2[i];
            }
            // System.out.println(font + " = " + s);
            g.dispose();
        } catch (Exception e) {
            System.out.println("Font file not found: " + font);
            e.printStackTrace();
        }
    }

    /**
     * If true, syllable should not touch each other
     * @return true if syllable should not touch each other
     */
    public boolean isTouchingSyllables() {
        return prop.getBooleanProperty("touching-syllables");
    }

    public boolean niceApostrophes() {
        return prop.getBooleanProperty("typographic-apostrophes");
    }
    public boolean linesStartUppercase() {
        return prop.getBooleanProperty("capitalize-rows");
    }
}
