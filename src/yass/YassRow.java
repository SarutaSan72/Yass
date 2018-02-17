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

import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassRow implements Cloneable, Comparable<Object> {
    /**
     * Description of the Field
     */
    public final static char SPACE = '\u00B7';
    /**
     * Description of the Field
     */
    public final static char HYPHEN = '\u2043';
    /**
     * Description of the Field
     */
    public final static String EMPTY_LINE = "err_empty_line";
    // MIDDLE DOT used to show spaces in table
    /**
     * Description of the Field
     */
    public final static String INVALID_LINE = "err_invalid_line";
    // SMALL DASH used to show non-hyphens in lyrics
    /**
     * Description of the Field
     */
    public final static String LINE_CUT = "err_line_cut";
    /**
     * Description of the Field
     */
    public final static String OUT_OF_ORDER_COMMENT = "err_out_of_order_comment";
    /**
     * Description of the Field
     */
    public final static String COMMENT_AFTER_END = "err_comment_after_end";
    /**
     * Description of the Field
     */
    public final static String UNSORTED_COMMENTS = "err_unsorted_comments";
    /**
     * Description of the Field
     */
    public final static String INVALID_TAG = "err_invalid_tag";
    /**
     * Description of the Field
     */
    public final static String MISSING_END = "err_missing_end";
    /**
     * Description of the Field
     */
    public final static String MISSING_TAG = "err_missing_tag";
    /**
     * Description of the Field
     */
    public final static String OUT_OF_ORDER = "err_out_of_order";
    /**
     * Description of the Field
     */
    public final static String NOTES_OVERLAP = "err_notes_overlap";
    /**
     * Description of the Field
     */
    public final static String NOTES_TOUCHING = "err_notes_touching";
    /**
     * Description of the Field
     */
    public final static String PAGE_OVERLAP = "err_page_overlap";
    /**
     * Description of the Field
     */
    public final static String EARLY_PAGE_BREAK = "err_early_page_break";
    /**
     * Description of the Field
     */
    public final static String LATE_PAGE_BREAK = "err_late_page_break";
    /**
     * Description of the Field
     */
    public final static String SHORT_PAGE_BREAK = "err_short_page_break";
    /**
     * Description of the Field
     */
    public final static String UNCOMMON_PAGE_BREAK = "err_uncommon_page_break";
    /**
     * Description of the Field
     */
    public final static String TOO_MUCH_TEXT = "err_too_much_text";
    /**
     * Description of the Field
     */
    public final static String TRANSPOSED_NOTES = "err_transposed_notes";
    /**
     * Description of the Field
     */
    public final static String NONZERO_FIRST_BEAT = "err_nonzero_first_beat";
    /**
     * Description of the Field
     */
    public final static String INVALID_NOTE_LENGTH = "err_invalid_note_length";
    /**
     * Description of the Field
     */
    public final static String UNCOMMON_SPACING = "err_uncommon_spacing";
    /**
     * Description of the Field
     */
    public final static String TOO_MUCH_SPACES = "err_too_much_spaces";
    /**
     * Description of the Field
     */
    public final static String MISSING_SPACES = "err_missing_spaces";
    /**
     * Description of the Field
     */
    public final static String WRONG_SPELLING = "err_wrong_spelling";
    /**
     * Description of the Field
     */
    public final static String WRONG_LENGTH = "err_wrong_length";
    /**
     * Description of the Field
     */
    public final static String WRONG_HEIGHT = "err_wrong_height";
    /**
     * Description of the Field
     */
    public final static String WRONG_TEXT = "err_wrong_text";
    /**
     * Description of the Field
     */
    public final static String MISUSED_HYPHENATION = "err_misused_hyphenation";
    /**
     * Description of the Field
     */
    public final static String UNCOMMON_GOLDEN = "err_uncommon_golden";
    /**
     * Description of the Field
     */
    public final static String FILE_FOUND = "err_file_found";
    /**
     * Description of the Field
     */
    public final static String FILE_NOT_FOUND = "err_file_not_found";
    /**
     * Description of the Field
     */
    public final static String NO_COVER_LABEL = "err_no_cover_label";
    /**
     * Description of the Field
     */
    public final static String NO_BACKGROUND_LABEL = "err_no_background_label";
    /**
     * Description of the Field
     */
    public final static String NO_VIDEO_LABEL = "err_no_video_label";
    /**
     * Description of the Field
     */
    public final static String WRONG_VIDEOGAP = "err_wrong_videogap";
    /**
     * Description of the Field
     */
    public final static String[] ALL_MESSAGES = {
            MISSING_TAG, UNSORTED_COMMENTS,
            OUT_OF_ORDER_COMMENT, COMMENT_AFTER_END, INVALID_TAG, MISSING_END,
            FILE_FOUND, WRONG_VIDEOGAP, FILE_NOT_FOUND,
            EARLY_PAGE_BREAK, LATE_PAGE_BREAK, PAGE_OVERLAP,
            UNCOMMON_PAGE_BREAK,
            TOO_MUCH_SPACES, UNCOMMON_SPACING,
            EMPTY_LINE, TRANSPOSED_NOTES, NONZERO_FIRST_BEAT, INVALID_NOTE_LENGTH, NOTES_TOUCHING,
            INVALID_LINE, LINE_CUT,
            OUT_OF_ORDER, NOTES_OVERLAP, TOO_MUCH_TEXT, UNCOMMON_GOLDEN
            // MISSING_SPACES, WRONG_SPELLING,
            //WRONG_LENGTH, WRONG_HEIGHT, WRONG_TEXT, MISUSED_HYPHENATION
    };
    /**
     * Description of the Field
     */
    public final static String NO_VIDEOGAP = "err_no_videogap";
    /**
     * Description of the Field
     */
    public final static String DIRECTORY_WITHOUT_VIDEO = "err_directory_without_label";
    /**
     * Description of the Field
     */
    public final static String WRONG_FILENAME = "err_wrong_filename";
    private static int[] tagsMessages = null, minorPageBreaks = null, majorMessages = null, fileMessages = null, textMessages = null, criticalMessages = null;
    private static String validTags = "";
    private static String validLines = "";
    private String s[] = new String[5];
    private Vector<String[]> messages = null;


    /**
     * Constructor for the YassRow object
     *
     * @param t   Description of the Parameter
     * @param b   Description of the Parameter
     * @param d   Description of the Parameter
     * @param h   Description of the Parameter
     * @param txt Description of the Parameter
     */
    public YassRow(String t, String b, String d, String h, String txt) {
        s[0] = t;
        s[1] = b;
        s[2] = d;
        s[3] = h;
        setText(txt);
    }


    /**
     * Constructor for the YassRow object
     *
     * @param t   Description of the Parameter
     * @param b   Description of the Parameter
     * @param d   Description of the Parameter
     * @param h   Description of the Parameter
     * @param txt Description of the Parameter
     * @param msg Description of the Parameter
     */
    public YassRow(String t, String b, String d, String h, String txt, String msg) {
        s[0] = t;
        s[1] = b;
        s[2] = d;
        s[3] = h;
        setText(txt);
        addMessage(msg);
    }


    /**
     * Constructor for the YassRow object
     *
     * @param t      Description of the Parameter
     * @param b      Description of the Parameter
     * @param d      Description of the Parameter
     * @param h      Description of the Parameter
     * @param txt    Description of the Parameter
     * @param msg    Description of the Parameter
     * @param detail Description of the Parameter
     */
    public YassRow(String t, String b, String d, String h, String txt, String msg, String detail) {
        s[0] = t;
        s[1] = b;
        s[2] = d;
        s[3] = h;
        setText(txt);
        addMessage(msg, detail);
    }


    /**
     * Sets the row attribute of the YassRow object
     *
     * @param r Description of the Parameter
     */
    public YassRow(YassRow r) {
        s[0] = r.s[0];
        s[1] = r.s[1];
        s[2] = r.s[2];
        s[3] = r.s[3];
        s[4] = r.s[4];
    }

    /**
     * Gets the minorPageBreakMessages attribute of the YassRow class
     *
     * @return The minorPageBreakMessages value
     */
    public static int[] getMinorPageBreakMessages() {
        if (minorPageBreaks == null) {
            minorPageBreaks = createMessageArray(new String[]{EARLY_PAGE_BREAK, LATE_PAGE_BREAK, PAGE_OVERLAP});
        }
        return minorPageBreaks;
    }

    /**
     * Gets the textMessages attribute of the YassRow class
     *
     * @return The textMessages value
     */
    public static int[] getTextMessages() {
        if (textMessages == null) {
            textMessages = createMessageArray(new String[]{TOO_MUCH_SPACES, UNCOMMON_SPACING});
        }
        return textMessages;
    }

    /**
     * Gets the tagsMessages attribute of the YassRow class
     *
     * @return The tagsMessages value
     */
    public static int[] getTagsMessages() {
        if (tagsMessages == null) {
            tagsMessages = createMessageArray(new String[]{MISSING_TAG, UNSORTED_COMMENTS});
        }
        return tagsMessages;
    }

    /**
     * Gets the message attribute of the YassRow class
     *
     * @param msg Description of the Parameter
     * @param all Description of the Parameter
     * @return The message value
     */
    public static boolean isMessage(int msg, int[] all) {
        for (int m : all) {
            if (msg == m) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the majorMessages attribute of the YassRow class
     *
     * @return The majorMessages value
     */
    public static int[] getMajorMessages() {
        if (majorMessages == null) {
            majorMessages = createMessageArray(new String[]{OUT_OF_ORDER, NOTES_OVERLAP, TOO_MUCH_TEXT});
        }
        return majorMessages;
    }

    /**
     * Gets the criticalMessages attribute of the YassRow class
     *
     * @return The criticalMessages value
     */
    public static int[] getCriticalMessages() {
        if (criticalMessages == null) {
            criticalMessages = createMessageArray(new String[]{MISSING_END});
        }
        return criticalMessages;
    }

    /**
     * Gets the fileMessages attribute of the YassRow class
     *
     * @return The fileMessages value
     */
    public static int[] getFileMessages() {
        if (fileMessages == null) {
            fileMessages = createMessageArray(new String[]{FILE_FOUND,
                    // NO_COVER_LABEL, NO_BACKGROUND_LABEL, NO_VIDEO_LABEL,
                    NO_VIDEOGAP, WRONG_VIDEOGAP});
            // deprecated DIRECTORY_WITHOUT_VIDEO, WRONG_FILENAME
        }
        return fileMessages;
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public static int[] createMessageArray(String s[]) {
        int a[] = new int[s.length];
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

    /**
     * Gets the validTags attribute of the YassRow class
     *
     * @return The validTags value
     */
    public static String getValidTags() {
        return validTags;
    }

    /**
     * Sets the validTags attribute of the YassRow class
     *
     * @param s The new validTags value
     */
    public static void setValidTags(String s) {
        validTags = s;
        if (!validTags.startsWith(" ")) {
            validTags = " " + validTags;
        }
        if (!validTags.endsWith(" ")) {
            validTags = validTags + " ";
        }
    }

    /**
     * Gets the validLines attribute of the YassRow class
     *
     * @return The validLines value
     */
    public static String getValidLines() {
        return validLines;
    }

    /**
     * Sets the validLines attribute of the YassRow class
     *
     * @param s The new validLines value
     */
    public static void setValidLines(String s) {
        validLines = s;
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
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

    /**
     * Sets the row attribute of the YassRow object
     *
     * @param t   The new row value
     * @param b   The new row value
     * @param d   The new row value
     * @param h   The new row value
     * @param txt The new row value
     */
    public void setRow(String t, String b, String d, String h, String txt) {
        s[0] = t;
        s[1] = b;
        s[2] = d;
        s[3] = h;
        setText(txt);
    }

    /**
     * Sets the row attribute of the YassRow object
     *
     * @param r The new row value
     */
    public void setRow(YassRow r) {
        s[0] = r.s[0];
        s[1] = r.s[1];
        s[2] = r.s[2];
        s[3] = r.s[3];
        s[4] = r.s[4];
    }

    /**
     * Sets the elementAt attribute of the YassRow object
     *
     * @param val The new elementAt value
     * @param i   The new elementAt value
     */
    public void setElementAt(String val, int i) {
        s[i] = val;
    }

    /**
     * Sets the beat attribute of the YassRow object
     *
     * @param val The new beat value
     */
    public void setBeat(String val) {
        s[1] = val;
    }

    /**
     * Sets the secondBeat attribute of the YassRow object
     *
     * @param val The new secondBeat value
     */
    public void setSecondBeat(String val) {
        if (val.equals(s[1])) {
            s[2] = "";
        } else {
            s[2] = val;
        }
    }

    /**
     * Sets the length attribute of the YassRow object
     *
     * @param val The new length value
     */
    public void setLength(String val) {
        s[2] = val;
    }

    /**
     * Sets the height attribute of the YassRow object
     *
     * @param val The new height value
     */
    public void setHeight(String val) {
        s[3] = val;
    }

    /**
     * Description of the Method
     *
     * @param i Description of the Parameter
     * @return Description of the Return Value
     */
    public String elementAt(int i) {
        return s[i];
    }

    /**
     * Gets the type attribute of the YassRow object
     *
     * @return The type value
     */
    public String getType() {
        return s[0];
    }

    /**
     * Sets the type attribute of the YassRow object
     *
     * @param val The new type value
     */
    public void setType(String val) {
        s[0] = val;
    }

    /**
     * Gets the beat attribute of the YassRow object
     *
     * @return The beat value
     */
    public String getBeat() {
        return s[1];
    }


    /**
     * Gets the commentTag attribute of the YassRow object
     *
     * @return The commentTag value
     */
    public String getCommentTag() {
        return s[1];
    }


    /**
     * Gets the comment attribute of the YassRow object
     *
     * @return The comment value
     */
    public String getComment() {
        return s[2];
    }


    /**
     * Gets the length attribute of the YassRow object
     *
     * @return The length value
     */
    public String getLength() {
        return s[2];
    }

    /**
     * Sets the length attribute of the YassRow object
     *
     * @param val The new length value
     */
    public void setLength(int val) {
        s[2] = val + "";
    }

    /**
     * Gets the secondBeat attribute of the YassRow object
     *
     * @return The secondBeat value
     */
    public String getSecondBeat() {
        return s[2];
    }

    /**
     * Sets the secondBeat attribute of the YassRow object
     *
     * @param val The new secondBeat value
     */
    public void setSecondBeat(int val) {
        if (val == getBeatInt()) {
            s[2] = "";
        } else {
            s[2] = val + "";
        }
    }

    /**
     * Gets the height attribute of the YassRow object
     *
     * @return The height value
     */
    public String getHeight() {
        return s[3];
    }

    /**
     * Sets the height attribute of the YassRow object
     *
     * @param val The new height value
     */
    public void setHeight(int val) {
        s[3] = val + "";
    }


    // todo: could be optimized by adding fields

    /**
     * Gets the version attribute of the YassRow object
     *
     * @return The version value
     */
    public String getVersion() {
        if (s[3].length() < 3) {
            return "";
        }
        return s[3].substring(2, s[3].length() - 1);
    }

    /**
     * Sets the version attribute of the YassRow object
     *
     * @param val The new version value
     */
    public void setVersion(String val) {
        // speeds up toString()
        s[3] = val.length() > 0 ? " [" + val + "]" : "";
    }

    /**
     * Gets the text attribute of the YassRow object
     *
     * @return The text value
     */
    public String getText() {
        return s[4];
    }

    /**
     * Sets the text attribute of the YassRow object
     *
     * @param val The new text value
     */
    public void setText(String val) {
        s[4] = val;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean hasVersion() {
        return s[3].length() > 0;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean hasSecondBeat() {
        return s[2].length() > 0;
    }

    /**
     * Gets the beatInt attribute of the YassRow object
     *
     * @return The beatInt value
     */
    public int getBeatInt() {
        if (s[1].length() < 1) {
            return 0;
        }
        return Integer.parseInt(s[1]);
    }

    /**
     * Gets the secondBeatInt attribute of the YassRow object
     *
     * @return The secondBeatInt value
     */
    public int getSecondBeatInt() {
        if (!hasSecondBeat()) {
            return getBeatInt();
        }
        return Integer.parseInt(getSecondBeat());
    }

    /**
     * Gets the lengthInt attribute of the YassRow object
     *
     * @return The lengthInt value
     */
    public int getLengthInt() {
        if (s[2].length() < 1) {
            return 0;
        }
        return Integer.parseInt(s[2]);
    }

    /**
     * Gets the heightInt attribute of the YassRow object
     *
     * @return The heightInt value
     */
    public int getHeightInt() {
        if (s[3].length() < 1) {
            return 0;
        }
        return Integer.parseInt(s[3]);
    }

    /**
     * Gets the end attribute of the YassRow object
     *
     * @return The end value
     */
    public boolean isEnd() {
        return s[0].equals("E");
    }

    /**
     * Gets the hidden attribute of the YassRow object
     *
     * @return The hidden value
     */
    public boolean isHidden() {
        return s[0].equals("Y") && s[1].equals("hide");
    }

    /**
     * Gets the comment attribute of the YassRow object
     *
     * @return The comment value
     */
    public boolean isComment() {
        return s[0].equals("#");
    }

    /**
     * Sets the comment attribute of the YassRow object
     *
     * @param val The new comment value
     */
    public void setComment(String val) {
        s[2] = val;
    }

    /**
     * Gets the pageBreak attribute of the YassRow object
     *
     * @return The pageBreak value
     */
    public boolean isPageBreak() {
        return s[0].equals("-");
    }

    /**
     * Gets the beat attribute of the YassRow object
     *
     * @return The beat value
     */
    public boolean isBeat() {
        return s[0].equals(":");
    }

    /**
     * Sets the beat attribute of the YassRow object
     *
     * @param val The new beat value
     */
    public void setBeat(int val) {
        s[1] = val + "";
    }

    /**
     * Gets the golden attribute of the YassRow object
     *
     * @return The golden value
     */
    public boolean isGolden() {
        return s[0].equals("*");
    }

    /**
     * Gets the freeStyle attribute of the YassRow object
     *
     * @return The freeStyle value
     */
    public boolean isFreeStyle() {
        return s[0].equals("F");
    }

    /**
     * Gets the multiplayer attribute of the YassRow object
     *
     * @return The multiplayer value
     */
    public boolean isMultiplayer() {
        return s[0].equals("P");
    }

    /**
     * Gets the note attribute of the YassRow object
     *
     * @return The note value
     */
    public boolean isNote() {
        return isBeat() || isGolden() || isFreeStyle();
    }

    /**
     * Gets the noteOrPageBreak attribute of the YassRow object
     *
     * @return The noteOrPageBreak value
     */
    public boolean isNoteOrPageBreak() {
        return isNote() || isPageBreak();
    }

    /**
     * Gets the gap attribute of the YassRow object
     *
     * @return The gap value
     */
    public boolean isGap() {
        return s[0].equals("#") && s[1].toUpperCase().equals("GAP:");
    }

    /**
     * Description of the Method
     */
    public void removeAllMessages() {
        messages = null;
    }

    /**
     * Gets the messages attribute of the YassRow object
     *
     * @return The messages value
     */
    public Vector<String[]> getMessages() {
        return messages;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean hasMessage() {
        return messages != null;
    }

    /**
     * Gets the message attribute of the YassRow object
     *
     * @return The message value
     */
    public String getMessage() {
        String[] msg = messages.firstElement();
        return msg[0];
    }

    /**
     * Gets the message attribute of the YassRow object
     *
     * @param i Description of the Parameter
     * @return The message value
     */
    public String getMessage(int i) {
        if (i >= messages.size()) {
            return null;
        }
        String[] msg = messages.elementAt(i);
        return msg[0];
    }

    /**
     * Gets the detail attribute of the YassRow object
     *
     * @param i Description of the Parameter
     * @return The detail value
     */
    public String getDetail(int i) {
        if (i >= messages.size()) {
            return null;
        }
        String[] msg = messages.elementAt(i);
        return msg[1];
    }

    /**
     * Gets the messageWithDetail attribute of the YassRow object
     *
     * @param i Description of the Parameter
     * @return The messageWithDetail value
     */
    public String[] getMessageWithDetail(int i) {
        if (i >= messages.size()) {
            return null;
        }
        return messages.elementAt(i);
    }

    /**
     * Adds a feature to the Message attribute of the YassRow object
     *
     * @param s The feature to be added to the Message attribute
     */
    public void addMessage(String s) {
        if (messages == null) {
            messages = new Vector<>();
        }
        messages.addElement(new String[]{s});
    }

    /**
     * Adds a feature to the Message attribute of the YassRow object
     *
     * @param s The feature to be added to the Message attribute
     * @param d The feature to be added to the Message attribute
     */
    public void addMessage(String s, String d) {
        if (messages == null) {
            messages = new Vector<>();
        }
        messages.addElement(new String[]{s, d});
    }

    /**
     * Gets the detail attribute of the YassRow object
     *
     * @return The detail value
     */
    public String getDetail() {
        String[] msg = messages.firstElement();
        return msg[1];
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean hasDetail() {
        String[] msg = messages.firstElement();
        return msg.length > 1;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
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
        } else if (isMultiplayer()) {
            return s[0] + " " + s[1];
        }
        return s[0] + s[1] + s[2] + s[3] + s[4];
    }

    /**
     * Description of the Method
     *
     * @param relative Description of the Parameter
     * @return Description of the Return Value
     */
    public String toString(int relative) {
        if (isNote()) {
            int time = 0;
            try {
                time = Integer.parseInt(s[1]);
            } catch (Exception e) {
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
        return s[0] + s[1] + s[2] + s[3] + s[4];
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public YassRow clone() {
        return new YassRow(s[0], s[1], s[2], s[3], s[4]);
    }

    /**
     * Description of the Method
     *
     * @param o Description of the Parameter
     * @return Description of the Return Value
     */
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


    // extended String.trim(...)

    /**
     * Description of the Method
     *
     * @param o Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean equals(Object o) {
        YassRow r = (YassRow) o;
        if (!s[0].equals(r.s[0])) {
            return false;
        }
        if (!s[1].equals(r.s[1])) {
            return false;
        }

        // quick hack; version should become standard tag
        if (s[1].equals("TITLE:")) {
            String ts = s[2];
            String rs = r.s[2];
            int i = ts.indexOf("[");
            int ri = rs.indexOf("[");
            if (i > 0) {
                ts = ts.substring(0, i).trim();
            }
            if (ri > 0) {
                rs = rs.substring(0, ri).trim();
            }
            return ts.equals(rs);
        }

        if (!s[2].equals(r.s[2])) {
            return false;
        }
        if (!s[3].equals(r.s[3])) {
            return false;
        }
        if (!s[4].equals(r.s[4])) {
            return false;
        }
        return true;
    }
}

