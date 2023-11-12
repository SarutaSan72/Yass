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
import org.apache.commons.math3.util.Precision;

import java.util.Vector;

public class YassRow implements Cloneable, Comparable<Object> {
    public static final char SPACE = '\u00B7';
    public static final char HYPHEN = '\u2043';
    public static final String EMPTY_LINE = "err_empty_line";
    public static final String INVALID_LINE = "err_invalid_line";
    public static final String LINE_CUT = "err_line_cut";
    public static final String OUT_OF_ORDER_COMMENT = "err_out_of_order_comment";
    public static final String COMMENT_AFTER_END = "err_comment_after_end";
    public static final String UNSORTED_COMMENTS = "err_unsorted_comments";
    public static final String INVALID_TAG = "err_invalid_tag";
    public static final String MISSING_END = "err_missing_end";
    public static final String MISSING_TAG = "err_missing_tag";
    public static final String OUT_OF_ORDER = "err_out_of_order";
    public static final String NOTES_OVERLAP = "err_notes_overlap";
    public static final String NOTES_TOUCHING = "err_notes_touching";
    public static final String PAGE_OVERLAP = "err_page_overlap";
    public static final String EARLY_PAGE_BREAK = "err_early_page_break";
    public static final String LATE_PAGE_BREAK = "err_late_page_break";
    public static final String SHORT_PAGE_BREAK = "err_short_page_break";
    public static final String UNCOMMON_PAGE_BREAK = "err_uncommon_page_break";
    public static final String TOO_MUCH_TEXT = "err_too_much_text";
    public static final String TRANSPOSED_NOTES = "err_transposed_notes";
    public static final String NONZERO_FIRST_BEAT = "err_nonzero_first_beat";
    public static final String INVALID_NOTE_LENGTH = "err_invalid_note_length";
    public static final String UNCOMMON_SPACING = "err_uncommon_spacing";
    public static final String TOO_MUCH_SPACES = "err_too_much_spaces";
    public static final String MISSING_SPACES = "err_missing_spaces";
    public static final String WRONG_SPELLING = "err_wrong_spelling";
    public static final String WRONG_LENGTH = "err_wrong_length";
    public static final String WRONG_HEIGHT = "err_wrong_height";
    public static final String WRONG_TEXT = "err_wrong_text";
    public static final String MISUSED_HYPHENATION = "err_misused_hyphenation";
    public static final String UNCOMMON_GOLDEN = "err_uncommon_golden";
    public static final String FILE_FOUND = "err_file_found";
    public static final String FILE_NOT_FOUND = "err_file_not_found";
    public static final String NO_COVER_LABEL = "err_no_cover_label";
    public static final String NO_BACKGROUND_LABEL = "err_no_background_label";
    public static final String NO_VIDEO_LABEL = "err_no_video_label";
    public static final String WRONG_VIDEOGAP = "err_wrong_videogap";
    public static final String WRONG_MEDLEY_START_BEAT = "err_wrong_medley_start_beat";
    public static final String WRONG_MEDLEY_END_BEAT = "err_wrong_medley_end_beat";
    public static final String LOWERCASE_ROWSTART = "err_line_starts_with_lowercase";
    public static final String BORING_APOSTROPHE = "err_contains_boring_apostrophe";
    public final static String[] ALL_MESSAGES = {
            MISSING_TAG, UNSORTED_COMMENTS,
            WRONG_MEDLEY_START_BEAT, WRONG_MEDLEY_END_BEAT,
            OUT_OF_ORDER_COMMENT, COMMENT_AFTER_END, INVALID_TAG, MISSING_END,
            FILE_FOUND, WRONG_VIDEOGAP, FILE_NOT_FOUND,
            EARLY_PAGE_BREAK, LATE_PAGE_BREAK, PAGE_OVERLAP,
            UNCOMMON_PAGE_BREAK,
            TOO_MUCH_SPACES, UNCOMMON_SPACING,
            EMPTY_LINE, TRANSPOSED_NOTES, NONZERO_FIRST_BEAT, INVALID_NOTE_LENGTH, NOTES_TOUCHING,
            INVALID_LINE, LINE_CUT,
            OUT_OF_ORDER, NOTES_OVERLAP, TOO_MUCH_TEXT, UNCOMMON_GOLDEN, LOWERCASE_ROWSTART, BORING_APOSTROPHE
            // MISSING_SPACES, WRONG_SPELLING,
            //WRONG_LENGTH, WRONG_HEIGHT, WRONG_TEXT, MISUSED_HYPHENATION
    };

    public final static String NO_VIDEOGAP = "err_no_videogap";
    public final static String DIRECTORY_WITHOUT_VIDEO = "err_directory_without_label";
    public final static String WRONG_FILENAME = "err_wrong_filename";
    private static int[] tagsMessages = null, minorPageBreaks = null, majorMessages = null, fileMessages = null, textMessages = null, criticalMessages = null;
    private static String validTags = "";
    private static String validLines = "";
    private final String[] s = new String[5];
    private Vector<String[]> messages = null;

    public YassRow(String t, String b, String d, String h, String txt) {
        s[0] = t;
        s[1] = b;
        s[2] = d;
        s[3] = h;
        setText(txt);
    }

    public YassRow(String t, String b, String d, String h, String txt, String msg) {
        s[0] = t;
        s[1] = b;
        s[2] = d;
        s[3] = h;
        setText(txt);
        addMessage(msg);
    }

    public YassRow(String t, String b, String d, String h, String txt, String msg, String detail) {
        s[0] = t;
        s[1] = b;
        s[2] = d;
        s[3] = h;
        setText(txt);
        addMessage(msg, detail);
    }

    public YassRow(YassRow r) {
        s[0] = r.s[0];
        s[1] = r.s[1];
        s[2] = r.s[2];
        s[3] = r.s[3];
        s[4] = r.s[4];
    }

    public YassRow(String line) {
        String[] rows = line.split("\t");
        for (int i = 0; i < rows.length; i++) {
            s[i] = rows[i];
        }
        if (rows.length >= 1 && rows.length < 4) {
            for (int i = rows.length; i < 5; i++) {
                s[i] = "";
            }
        }
    }

    public static int[] getMinorPageBreakMessages() {
        if (minorPageBreaks == null) {
            minorPageBreaks = createMessageArray(new String[]{EARLY_PAGE_BREAK, LATE_PAGE_BREAK, PAGE_OVERLAP});
        }
        return minorPageBreaks;
    }

    public static int[] getTextMessages() {
        if (textMessages == null) {
            textMessages = createMessageArray(new String[]{TOO_MUCH_SPACES, UNCOMMON_SPACING});
        }
        return textMessages;
    }

    public static int[] getTagsMessages() {
        if (tagsMessages == null) {
            tagsMessages = createMessageArray(new String[]{MISSING_TAG, UNSORTED_COMMENTS, WRONG_MEDLEY_START_BEAT, WRONG_MEDLEY_END_BEAT});
        }
        return tagsMessages;
    }

    public static boolean isMessage(int msg, int[] all) {
        for (int m : all) {
            if (msg == m) {
                return true;
            }
        }
        return false;
    }

    public static int[] getMajorMessages() {
        if (majorMessages == null) {
            majorMessages = createMessageArray(new String[]{OUT_OF_ORDER, NOTES_OVERLAP, TOO_MUCH_TEXT});
        }
        return majorMessages;
    }

    public static int[] getCriticalMessages() {
        if (criticalMessages == null) {
            criticalMessages = createMessageArray(new String[]{MISSING_END});
        }
        return criticalMessages;
    }

    public static int[] getFileMessages() {
        if (fileMessages == null) {
            fileMessages = createMessageArray(new String[]{FILE_FOUND,
                    // NO_COVER_LABEL, NO_BACKGROUND_LABEL, NO_VIDEO_LABEL,
                    NO_VIDEOGAP, WRONG_VIDEOGAP});
            // deprecated DIRECTORY_WITHOUT_VIDEO, WRONG_FILENAME
        }
        return fileMessages;
    }

    public static int[] createMessageArray(String[] s) {
        int[] a = new int[s.length];
        for (int i = 0; i < s.length; i++) {
            String msg = s[i];
            for (int k = 0; k < ALL_MESSAGES.length; k++) {
                if (ALL_MESSAGES[k].equals(msg)) {
                    a[i] = k;
                    break;
                }
            }
        }
        return a;
    }

    public static String getValidTags() {
        return validTags;
    }

    public static void setValidTags(String s) {
        validTags = s;
        if (!validTags.startsWith(" ")) {
            validTags = " " + validTags;
        }
        if (!validTags.endsWith(" ")) {
            validTags = validTags + " ";
        }
    }

    public static String getValidLines() {
        return validLines;
    }

    public static void setValidLines(String s) {
        validLines = s;
    }

    public static String trim(String s) {
        int count = s.length();
        int st = 0;
        int len = count;
        char[] val = s.toCharArray();
        while ((st < len) && (val[st] <= ' ' || val[st] == SPACE)) {
            st++;
        }
        while ((st < len) && (val[len - 1] <= ' ' || val[st] == SPACE)) {
            len--;
        }
        return ((st > 0) || (len < count)) ? s.substring(st, len) : s;
    }

    public void setRow(String t, String b, String d, String h, String txt) {
        s[0] = t;
        s[1] = b;
        s[2] = d;
        s[3] = h;
        setText(txt);
    }

    public void setRow(YassRow r) {
        s[0] = r.s[0];
        s[1] = r.s[1];
        s[2] = r.s[2];
        s[3] = r.s[3];
        s[4] = r.s[4];
    }

    public void setElementAt(String val, int i) {
        s[i] = val;
    }

    public void setBeat(String val) {
        s[1] = val;
    }

    public void setSecondBeat(String val) {
        if (val.equals(s[1])) {
            s[2] = "";
        } else {
            s[2] = val;
        }
    }

    public void setLength(String val) {
        s[2] = val;
    }

    public void setHeight(String val) {
        s[3] = val;
    }

    public String elementAt(int i) {
        return s[i];
    }

    public String getType() {
        return s[0];
    }

    public void setType(String val) {
        s[0] = val;
    }

    public String getBeat() {
        return s[1];
    }

    public String getCommentTag() {
        return s[1].toUpperCase();
    }

    public String getComment() {
        return s[2];
    }

    public String getLength() {
        return s[2];
    }

    public void setLength(int val) {
        s[2] = val + "";
    }

    public void setLength(double val) {
        setLength((int)Precision.round(val, 0));
    }

    public String getSecondBeat() {
        return s[2];
    }

    public void setSecondBeat(int val) {
        if (val == getBeatInt()) {
            s[2] = "";
        } else {
            s[2] = val + "";
        }
    }

    public void setSecondBeat(double val) {
        setSecondBeat((int)Math.round(val));
    }

    public String getHeight() {
        return s[3];
    }

    public void setHeight(int val) {
        s[3] = val + "";
    }

    public String getText() {
        return s[4];
    }

    public void setText(String val) {
        s[4] = val;
    }

    public boolean hasSecondBeat() {
        return s[2].length() > 0;
    }

    public int getBeatInt() {
        if (s[1].length() < 1) {
            return 0;
        }
        return Integer.parseInt(s[1]);
    }

    public int getSecondBeatInt() {
        if (!hasSecondBeat())
            return getBeatInt();
        return Integer.parseInt(getSecondBeat());
    }

    public int getLengthInt() {
        if (s[2].length() < 1)
            return 0;
        return Integer.parseInt(s[2]);
    }

    public int getHeightInt() {
        if (s[3].length() < 1)
            return 0;
        return Integer.parseInt(s[3]);
    }

    public boolean isEnd() {
        return s[0].equals("E");
    }

    public boolean isHidden() {
        return s[0].equals("Y") && s[1].equals("hide");
    }

    public boolean isComment() {
        return s[0].equals("#");
    }

    public void setComment(String val) {
        s[2] = val;
    }

    public boolean isPageBreak() {
        return s[0].equals("-");
    }

    public boolean isBeat() {
        return s[0].equals(":");
    }

    public void setBeat(int val) {
        s[1] = val + "";
    }

    public void setBeat(double val) {
        setBeat((int) Precision.round(val, 0));
    }

    public boolean isGolden() {
        return s[0].equals("*");
    }

    public boolean isFreeStyle() {
        return s[0].equals("F");
    }

    public boolean isRap() {
        return s[0].equals("R");
    }

    public boolean isRapGolden() {
        return s[0].equals("G");
    }

    /**
     * Checks if row is "P [n]"
     * @return
     */
    public boolean isP() {
        return s[0] != "" && s[0].charAt(0) == 'P';
    }

    public boolean isNote() {
        return isBeat() || isGolden() || isFreeStyle() || isRap() || isRapGolden();
    }

    public boolean isNoteOrPageBreak() {
        return isNote() || isPageBreak();
    }

    public boolean isGap() {
        return s[0].equals("#") && s[1].toUpperCase().equals("GAP:");
    }

    public void removeAllMessages() {
        messages = null;
    }

    public Vector<String[]> getMessages() {
        return messages;
    }

    public boolean hasMessage() {
        return messages != null;
    }

    public String getMessage() {
        String[] msg = messages.firstElement();
        return msg[0];
    }

    public String getMessage(int i) {
        if (i >= messages.size())
            return null;
        String[] msg = messages.elementAt(i);
        return msg[0];
    }

    public String getDetail(int i) {
        if (i >= messages.size())
            return null;
        String[] msg = messages.elementAt(i);
        return msg[1];
    }

    public String[] getMessageWithDetail(int i) {
        if (i >= messages.size())
            return null;
        return messages.elementAt(i);
    }

    public void addMessage(String s) {
        if (messages == null)
            messages = new Vector<>();
        messages.addElement(new String[]{s});
    }

    public void addMessage(String s, String d) {
        if (messages == null)
            messages = new Vector<>();
        messages.addElement(new String[]{s, d});
    }

    public String getDetail() {
        String[] msg = messages.firstElement();
        return msg[1];
    }

    public boolean hasDetail() {
        String[] msg = messages.firstElement();
        return msg.length > 1;
    }

    public String toString() {
        if (isNote()) {
            String t = s[4].replace(SPACE, ' ');
            return s[0] + " " + s[1] + " " + s[2] + " " + s[3] + " " + t;
        } else if (isPageBreak()) {
            String ss = s[0] + " " + s[1];
            if (s[2].length() > 0) {
                ss = ss + " " + s[2];
            }
            if (s[3].length() > 0) {
                ss = ss + " " + s[3];
            }
            if (s[4].length() > 0) {
                ss = ss + " " + s[4];
            }
            return ss;
        } else if (isP() && StringUtils.isNotEmpty(s[1])) {
            return s[0] + " " + s[1];
        } else if (isP()) {
            return s[0];
        }
        return s[0] + s[1] + s[2] + s[3] + s[4];
    }

    public String toString(int relative) {
        if (isNote()) {
            int time = 0;
            try {
                time = Integer.parseInt(s[1]);
            } catch (Exception ignored) {
            }
            time -= relative;
            String t = s[4].replace(SPACE, ' ');
            return s[0] + " " + time + " " + s[2] + " " + s[3] + " " + t;
        }
        if (isPageBreak()) {
            int time = 0;
            int time2 = 0;
            try {
                time = Integer.parseInt(s[1]);
                time2 = s[2].length() > 0 ? Integer.parseInt(s[2]) : time;
            } catch (Exception e) {
                time2 = time;
            }

            time -= relative;
            time2 -= relative;
            String ss = s[0] + " " + time + " " + time2;
            if (s[3].length() > 0) {
                ss = ss + " " + s[3];
            }
            if (s[4].length() > 0) {
                ss = ss + " " + s[4];
            }
            return ss;
        }
        if (isP() && StringUtils.isNotEmpty(s[1])) {
            return s[0] + " " + s[1];
        } else if (isP()) {
            return s[0];
        }
        return s[0] + s[1] + s[2] + s[3] + s[4];
    }

    public YassRow clone() {
        return new YassRow(s[0], s[1], s[2], s[3], s[4]);
    }

    public int compareTo(Object o) {
        YassRow r = (YassRow) o;
        if (isEnd()) {
            return 1;
        }
        if (r.isEnd()) {
            return -1;
        }
        if (r.isComment()) {
            if (!isComment()) {
                return 1;
            }
            String tag = getCommentTag().trim();
            if (tag.length() < 1) {
                return 1;
            }
            int tagPos = validTags.indexOf(" " + tag.substring(0, tag.length() - 1) + " ");
            String tag2 = r.getCommentTag().trim();
            if (tag2.length() < 1) {
                return -1;
            }
            int tagPos2 = validTags.indexOf(" " + tag2.substring(0, tag2.length() - 1) + " ");
            return tagPos < tagPos2 ? -1 : 1;
        }
        if (isNoteOrPageBreak() && r.isNoteOrPageBreak()) {
            int beat = getBeatInt();
            int beat2 = r.getBeatInt();
            return beat < beat2 ? -1 : 1;
        }
        return -1;
    }

    public boolean equals(Object o) {
        YassRow r = (YassRow) o;
        if (!s[0].equals(r.s[0]))
            return false;
        if (!s[1].equals(r.s[1]))
            return false;
        if (!s[2].equals(r.s[2]))
            return false;
        if (!s[3].equals(r.s[3]))
            return false;
        if (!s[4].equals(r.s[4]))
            return false;
        return true;
    }

    public boolean startsWithSpace() {
        return isNote() && getText().startsWith(YassRow.SPACE + "");
    }

    public boolean endsWithSpace() {
        return isNote() && getText().endsWith(YassRow.SPACE + "");
    }

    public boolean isTilde() {
        return isNote() && getText().startsWith("~");
    }

    public String getTrimmedText() {
        if (getText() == null) {
            return "";
        }
        String tempText = getText().replace(YassRow.SPACE, ' ');
        return tempText.trim();
    }
}
