package yass;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassProperties extends Properties {
    private static final long serialVersionUID = -8189893110989853544L;
    private String userDir = System.getProperty("user.home");
    private String yassDir = ".yass";
    private String userProps = "user.xml";


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean checkVersion() {
        String v = getProperty("yass-version");
        boolean old = v == null;
        if (!old) {
            old = v.startsWith("0.8");
        }
        if (!old) {
            old = v.startsWith("0.9.5");
        }
        if (!old) {
            old = v.startsWith("0.9.6");
        }
        if (!old) {
            old = v.startsWith("0.9.7");
        }
        if (!old) {
            old = v.startsWith("0.9.8");
        }
        if (!old) {
            old = v.startsWith("0.9.9");
        }
        if (!old) {
            old = v.startsWith("1.0.1");
        }
        if (!old) {
            old = v.startsWith("1.1");
        }
        if (!old) {
            old = v.startsWith("1.2");
        }
        return old;
    }


    /**
     * Gets the userDir attribute of the YassProperties object
     *
     * @return The userDir value
     */
    public String getUserDir() {
        return userDir + File.separator + yassDir;
    }


    /**
     * Description of the Method
     */
    public void load() {
        String propFile = userDir + File.separator + yassDir + File.separator + userProps;
        System.out.println("Loading properties: " + propFile);

        try {
            FileInputStream fis = new FileInputStream(propFile);
            loadFromXML(fis);
            fis.close();
            loadDevices();

            if (getProperty("note-naming-h") == null)
                setProperty("note-naming-h", "DE RU PL NO FI SE");

            return;
        } catch (Exception e) {
            // not exists
        }

        // user props not found; fall back to defaults

        reset();
        loadDevices();
    }


    private void loadDevices() {
        String[] mics = YassCaptureAudio.getDeviceNames();
        StringBuffer sb = new StringBuffer();
        int i = 0;
        int n = mics.length;
        for (String m : mics) {
            sb.append(m);
            if (i != n - 1) sb.append("|");
            i++;
        }
        String newMics = sb.toString();
        String oldMics = getProperty("control-mics");

        String mic = getProperty("control-mic");
        if (mic == null || mic.trim().length() < 1) {
            for (String m : mics) {
                if (m.indexOf("USBMIC") >= 0) {
                    mic = m;
                    put("control-mic", mic);
                    break;
                }
            }
        }
        if (!oldMics.equals(newMics)) {
            put("control-mics", newMics);
            store();
        }
        System.out.println("Mics: " + newMics);
        System.out.println("Selecting Mic: " + mic);
    }


    /**
     * Description of the Method
     */
    public void reset() {

        setProperty("default-programs", "C:/Program Files/Ultrastar Deluxe|C:/Program Files/Ultrastar|C:/Programme/Ultrastar Deluxe|C:/Ultrastar Deluxe|C:/Programme/Ultrastar|C:/Ultrastar|D:/Ultrastar|E:/Ultrastar|F:/Ultrastar|/home/.ultrastardx|/home/.ultrastar");
        // dirs
        setProperty("song-directory", userDir + File.separator + "Songs");
        setProperty("playlist-directory", userDir + File.separator + "Playlists");
        setProperty("cover-directory", userDir + File.separator + "Covers");
        setProperty("import-directory", "");

        setProperty("yass-version", YassActions.VERSION);

        setProperty("yass-language", "default");
        setProperty("yass-languages", "default|en|de|pl|es");

        //filetype association
        setProperty("song-filetype", ".txt");
        setProperty("playlist-filetype", ".upl");

        setProperty("audio-files", ".mp3");
        setProperty("image-files", ".jpg|.jpeg");
        setProperty("video-files", ".mpg|.mpeg|.avi|.divx");
        setProperty("cover-id", "[CO]");
        setProperty("background-id", "[BG]");
        setProperty("video-id", "[VD#*]");
        setProperty("videodir-id", "[VIDEO]");
        setProperty("use-fobs", "false");

        setProperty("correct-uncommon-pagebreaks", "unknown");

        setProperty("articles", "EN:the |a |an :DE:der |die |das |ein |eine :FR:le |la |les |l'|un |une |des :ES:el |la |los |las ");
        setProperty("use-articles", "true");

        // print
        setProperty("songlist-pdf", userDir + File.separator + yassDir + File.separator + "songlist.pdf");

        // cache
        setProperty("songlist-cache", userDir + File.separator + yassDir + File.separator + "songlist.txt");
        setProperty("playlist-cache", userDir + File.separator + yassDir + File.separator + "playlists.txt");
        setProperty("songlist-imagecache", userDir + File.separator + yassDir + File.separator + "covers-cache");
        setProperty("temp-dir", userDir + File.separator + yassDir + File.separator + "temp");
        setProperty("lyrics-cache", userDir + File.separator + yassDir + File.separator + "lyrics.txt");

        // metadata
        setProperty("language-tag", "English|EN|German|DE|Spanish|ES|French|FR|Other|NN");
        setProperty("language-more-tag", "Chinese|CH|Croatian|HR|Danish|DA|Italian|IT|Japanese|JA|Korean|KR|Polish|PL|Russian|RU|Swedish|SV|Turkish|TR");
        // ISO 639-2 http|//www.loc.gov/standards/iso639-2/php/English_list.php
        setProperty("genre-tag", "Blues|Darkwave|Musical|Metal|Oldies|Pop|Punk|Reggae|Rock|Other");
        setProperty("genre-more-tag", "Acid|Alternative|Anime|Classical|Country|Dance|Death Metal|Disco|Electronic|Folk|Funk|Game|Gangsta|Gospel|Gothic|Grunge|Hip-Hop|House|Industrial|Jazz|JPop|MORE|New Age|Noise|R&B|Rave|Rap|Retro|Rock & Roll|Showtunes|Ska|Soundtrack|Soul|Techno|Trance|Tribal|Vocal");
        // http|//de.wikipedia.org/wiki/Liste_der_ID3-Genres
        setProperty("edition-tag", "Birthday Party|Child's Play|Christmas|Greatest Hits|Halloween|Hits of the 60s|Kuschelrock|New Year's Eve|Summer|Sodamakers");
        // http|//de.wikipedia.org/wiki/Compilation
        setProperty("note-naming-h", "DE RU PL NO FI SE");

        // errors
        setProperty("valid-tags", "TITLE ARTIST LANGUAGE EDITION <br> GENRE ALBUM YEAR CREATOR ID <br> MP3 COVER BACKGROUND VIDEO <br> VIDEOGAP START END PLAYERS <br> RELATIVE BPM GAP <br> LENGTH PREVIEWSTART ");
        setProperty("valid-lines", "#:*F-EP");
        setProperty("font-file", "/fonts/Font1.dat");
        setProperty("max-points", "8000");
        setProperty("max-golden", "1000");
        setProperty("max-linebonus", "1000");
        setProperty("golden-allowed-variance", "200");
        setProperty("freestyle-counts", "false");
        setProperty("touching-syllables", "false");
        setProperty("correct-uncommon-pagebreaks-fix", "0");

        setProperty("cover-max-size", "128");
        setProperty("cover-min-width", "200");
        setProperty("cover-max-width", "800");
        setProperty("cover-ratio", "100");
        setProperty("use-cover-ratio", "true");

        setProperty("background-max-size", "1024");
        setProperty("background-min-width", "800");
        setProperty("background-max-width", "1024");
        setProperty("background-ratio", "133");
        setProperty("use-background-ratio", "true");

        //mic
        setProperty("control-mic", "");
        setProperty("control-mics", "");

        //editor
        setProperty("editor-layout", "East");
        setProperty("editor-layouts", "East|West");
        setProperty("lyrics-width", "400");
        setProperty("lyrics-min-height", "120");
        setProperty("lyrics-font-size", "14");

        setProperty("note-color-5", "#ff6666");
        setProperty("note-color-4", "#ffccff");
        setProperty("note-color-3", "#f0f066");
        setProperty("note-color-2", "#4488cc");
        setProperty("note-color-1", "#777777");
        setProperty("note-color-0", "#dddddd");
        setProperty("shade-notes", "true");

        setProperty("color-7", "#cccccc");
        setProperty("color-6", "#444444");
        setProperty("color-5", "#88cccc");
        setProperty("color-4", "#cc88cc");
        setProperty("color-3", "#f09944");
        setProperty("color-2", "#f0f044");
        setProperty("color-1", "#88cc44");
        setProperty("color-0", "#4488cc");

        setProperty("hi-color-7", "#669966");
        setProperty("hi-color-6", "#666699");
        setProperty("hi-color-5", "#994d99");
        setProperty("hi-color-4", "#99994d");
        setProperty("hi-color-3", "#4d9999");
        setProperty("hi-color-2", "#4d4d99");
        setProperty("hi-color-1", "#4d994d");
        setProperty("hi-color-0", "#4d4d4d");

        setProperty("group-title", "all|#|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z");
        setProperty("group-artist", "all|generic|others");
        setProperty("group-genre", "all|unspecified|generic");
        setProperty("group-language", "all|unspecified|English;German|generic");
        setProperty("group-edition", "all|unspecified|generic");
        setProperty("group-album", "all|unspecified|generic");
        setProperty("group-playlist", "all|generic|unspecified");
        setProperty("group-year", "all|unspecified|1930-1939|1940-1949|1950-1959|1960-1969|1970-1979|1980-1989|1990-1999|2000-2009|generic");
        setProperty("group-folder", "all|generic");
        setProperty("group-length", "all|unspecified|0 - 1:30|1:31 - 3:00|3:01 - 4:30|4:31 - 6:00|6:01 -");
        setProperty("group-files", "all|duplicates|video|no_video|no_video_background|no_background|no_cover|uncommon_filenames|uncommon_cover_size|uncommon_background_size");
        setProperty("group-format", "all|encoding_utf8|encoding_ansi|encoding_other|audio_vbr|audio_ogg");
        setProperty("group-tags", "all|previewstart|medleystartbeat|start|end|relative|gap<5|gap>30|gap>60|bpm<200|bpm>400");
        setProperty("group-errors", "all|all_errors|critical_errors|major_errors|tag_errors|file_errors|page_errors|text_errors");
        setProperty("group-duets", "all|solos|duets|trios|quartets|choirs");
        setProperty("group-stats", "all|pages_common|pages_pages_0-30|pages_pages_31-80|pages_pages_81-|golden_golden_3-20|freestyle_freestyle_0-30|notesperpage_notesperpage_0-5|notesperpage_notesperpage_5.1-|speeddist_slow|speeddist_averagespeed|speeddist_fast|pitchrange_monoton|pitchrange_melodic|pitchrange_smooth|pitchrange_bumpy|speeddist_longbreath");
        setProperty("group-instrument", "all|white|black|12-white|8-white|25-keys|13-keys");

        setProperty("group-min", "3");

        setProperty("hyphenations", "EN|DE|ES|PL|RU");
        setProperty("dicts", "EN|DE");
        setProperty("dict-map", "English|EN|German|DE|French|EN|Croatian|EN|Italian|EN|Japanese|EN|Polish|EN|Russian|EN|Spanish|EN|Swedish|EN|Turkish|EN");
        setProperty("user-dicts", userDir + File.separator + yassDir);

        setProperty("utf8-without-bom", "false");
        setProperty("utf8-always", "false");

        setProperty("floatable", "false");

        setProperty("mouseover", "false");
        setProperty("sketching", "false");
        setProperty("sketching-playback", "false");
        setProperty("show-note-height", "true");
        setProperty("show-note-length", "true");
        setProperty("auto-trim", "false");
        setProperty("playback-buttons", "true");
        setProperty("record-timebase", "2");

        setProperty("use-sample", "true");

        // 0=next_note, 1=prev_note, 2=page_down, 3=page_up, 4=init, 5=init_next, 6=right, 7=left, 8=up, 9=down, 10=lengthen, 11=shorten, 12=play, 13=play_page, 14=scroll_left, 15=scroll_right, 16=one_page
        setProperty("key-0", "NUMPAD6");
        setProperty("key-1", "NUMPAD4");
        setProperty("key-2", "NUMPAD2");
        setProperty("key-3", "NUMPAD8");

        setProperty("key-4", "NUMPAD0");
        setProperty("key-5", "DECIMAL");

        setProperty("key-6", "NUMPAD3");
        setProperty("key-7", "NUMPAD1");
        setProperty("key-8", "SUBTRACT");
        setProperty("key-9", "ADD");
        setProperty("key-10", "NUMPAD9");
        setProperty("key-11", "NUMPAD7");

        setProperty("key-12", "NUMPAD5");
        setProperty("key-13", "P");
        setProperty("key-14", "H");
        setProperty("key-15", "J");
        setProperty("key-16", "K");

        setProperty("screenkey-0", "ESCAPE");
        setProperty("screenkey-1", "PAUSE");
        setProperty("screenkey-2", "F5");

        setProperty("screenkey-3", "UP");
        setProperty("screenkey-4", "RIGHT");
        setProperty("screenkey-5", "DOWN");
        setProperty("screenkey-6", "LEFT");
        setProperty("screenkey-7", "ENTER");

        setProperty("screenkey-8", "A");
        setProperty("screenkey-9", "S");
        setProperty("screenkey-10", "D");
        setProperty("screenkey-11", "F");
        setProperty("screenkey-12", "G");
        setProperty("screenkey-13", "H");
        setProperty("screenkey-14", "J");
        setProperty("screenkey-15", "K");
        setProperty("screenkey-16", "L");
        setProperty("screenkey-17", "OEM_3");
        setProperty("screenkey-18", "OEM_7");
        setProperty("screenkey-19", "OEM_2");

        setProperty("screenkey-20", "UP|NUMPAD");
        setProperty("screenkey-21", "RIGHT|NUMPAD");
        setProperty("screenkey-22", "DOWN|NUMPAD");
        setProperty("screenkey-23", "LEFT|NUMPAD");
        setProperty("screenkey-24", "ENTER|NUMPAD");

        setProperty("screenkey-25", "---");
        setProperty("screenkey-26", "---");
        setProperty("screenkey-27", "---");
        setProperty("screenkey-28", "---");
        setProperty("screenkey-29", "---");
        setProperty("screenkey-30", "---");
        setProperty("screenkey-31", "---");
        setProperty("screenkey-32", "---");
        setProperty("screenkey-33", "---");
        setProperty("screenkey-34", "---");
        setProperty("screenkey-35", "---");
        setProperty("screenkey-36", "---");

        setProperty("screenkey-37", "W");
        setProperty("screenkey-38", "D");
        setProperty("screenkey-39", "S");
        setProperty("screenkey-40", "A");
        setProperty("screenkey-41", "SPACE");

        setProperty("screenkey-42", "---");
        setProperty("screenkey-43", "---");
        setProperty("screenkey-44", "---");
        setProperty("screenkey-45", "---");
        setProperty("screenkey-46", "---");
        setProperty("screenkey-47", "---");
        setProperty("screenkey-48", "---");
        setProperty("screenkey-49", "---");
        setProperty("screenkey-50", "---");
        setProperty("screenkey-51", "---");
        setProperty("screenkey-52", "---");
        setProperty("screenkey-53", "---");

        setProperty("player1_difficulty", "1");
        setProperty("player2_difficulty", "1");
        setProperty("player3_difficulty", "1");
        setProperty("game_sorting", "edition");
        setProperty("game_group", "all");
        setProperty("game_mode", "lines");

        setProperty("screen-groups", "title|artist|genre|language|edition|folder|playlist|year");

        // advanced
        setProperty("seek-in-offset", "0");
        setProperty("seek-out-offset", "0");
        setProperty("print-plugins", "yass.print.PrintBlocks|yass.print.PrintPlain|yass.print.PrintPlainLandscape|yass.print.PrintDetails");
        setProperty("filter-plugins", "yass.filter.YassTitleFilter|yass.filter.YassArtistFilter|yass.filter.YassLanguageFilter|yass.filter.YassEditionFilter|yass.filter.YassGenreFilter|yass.filter.YassAlbumFilter|yass.filter.YassPlaylistFilter|yass.filter.YassYearFilter|yass.filter.YassLengthFilter|yass.filter.YassFolderFilter|yass.filter.YassFilesFilter|yass.filter.YassFormatFilter|yass.filter.YassTagsFilter|yass.filter.YassMultiPlayerFilter|yass.filter.YassInstrumentFilter|yass.filter.YassErrorsFilter|yass.filter.YassStatsFilter");
        setProperty("stats-plugins", "yass.stats.YassBasicStats|yass.stats.YassTimeStats|yass.stats.YassPitchStats");
        setProperty("screen-plugins", "yass.screen.YassStartScreen|yass.screen.YassSelectGameScreen|yass.screen.YassSelectControllerScreen|yass.screen.YassSelectDeviceScreen|yass.screen.YassSelectDifficultyScreen|yass.screen.YassSelectSortingScreen|yass.screen.YassSelectGroupScreen|yass.screen.YassSelectSongScreen|yass.screen.YassPlaySongScreen|yass.screen.YassViewScoreScreen|yass.screen.YassHighScoreScreen|yass.screen.YassEnterScoreScreen|yass.screen.YassJukeboxScreen|yass.screen.YassCreditsScreen|yass.screen.YassStatsScreen");
        setProperty("jukebox", "highscore:topten-beginner|highscore:topten-standard|highscore:topten-expert|jukebox:topten|credits:yass|credits:thirdparty|credits:thanks|jukebox:random");

        setProperty("debug-memory", "false");
        setProperty("debug-score", "false");
        setProperty("debug-waveform", "false");

        //non-editable
        setProperty("welcome", "true");
        setProperty("recent-file", "");
    }


    /**
     * Gets the property attribute of the YassProperties object
     *
     * @param key Description of the Parameter
     * @param var Description of the Parameter
     * @param val Description of the Parameter
     * @return The property value
     */
    public String getProperty(String key, String var[], String val[]) {
        String s = getProperty(key);
        for (int i = 0; i < var.length; i++) {
            s = YassUtils.replace(s, var[i], val[i]);
        }
        return s;
    }


    /**
     * Gets the property attribute of the YassProperties object
     *
     * @param key Description of the Parameter
     * @param var Description of the Parameter
     * @param val Description of the Parameter
     * @return The property value
     */
    public String getProperty(String key, String var, String val) {
        String s = getProperty(key);
        s = YassUtils.replace(s, var, val);
        return s;
    }


    /**
     * Gets the property attribute of the YassProperties object
     *
     * @param key  Description of the Parameter
     * @param var1 Description of the Parameter
     * @param val1 Description of the Parameter
     * @param var2 Description of the Parameter
     * @param val2 Description of the Parameter
     * @return The property value
     */
    public String getProperty(String key, String var1, String val1, String var2, String val2) {
        String s = getProperty(key);
        s = YassUtils.replace(s, var1, val1);
        s = YassUtils.replace(s, var2, val2);
        return s;
    }


    /**
     * Gets the property attribute of the YassProperties object
     *
     * @param key  Description of the Parameter
     * @param var1 Description of the Parameter
     * @param val1 Description of the Parameter
     * @param var2 Description of the Parameter
     * @param val2 Description of the Parameter
     * @param var3 Description of the Parameter
     * @param val3 Description of the Parameter
     * @return The property value
     */
    public String getProperty(String key, String var1, String val1, String var2, String val2, String var3, String val3) {
        String s = getProperty(key);
        s = YassUtils.replace(s, var1, val1);
        s = YassUtils.replace(s, var2, val2);
        s = YassUtils.replace(s, var3, val3);
        return s;
    }


    /**
     * Gets the property attribute of the YassProperties object
     *
     * @param key  Description of the Parameter
     * @param var1 Description of the Parameter
     * @param val1 Description of the Parameter
     * @param var2 Description of the Parameter
     * @param val2 Description of the Parameter
     * @param var3 Description of the Parameter
     * @param val3 Description of the Parameter
     * @param var4 Description of the Parameter
     * @param val4 Description of the Parameter
     * @return The property value
     */
    public String getProperty(String key, String var1, String val1, String var2, String val2, String var3, String val3, String var4, String val4) {
        String s = getProperty(key);
        s = YassUtils.replace(s, var1, val1);
        s = YassUtils.replace(s, var2, val2);
        s = YassUtils.replace(s, var3, val3);
        s = YassUtils.replace(s, var4, val4);
        return s;
    }


    /**
     * Gets the property attribute of the YassProperties object
     *
     * @param key  Description of the Parameter
     * @param var1 Description of the Parameter
     * @param val1 Description of the Parameter
     * @param var2 Description of the Parameter
     * @param val2 Description of the Parameter
     * @param var3 Description of the Parameter
     * @param val3 Description of the Parameter
     * @param var4 Description of the Parameter
     * @param val4 Description of the Parameter
     * @param var5 Description of the Parameter
     * @param val5 Description of the Parameter
     * @return The property value
     */
    public String getProperty(String key, String var1, String val1, String var2, String val2, String var3, String val3, String var4, String val4, String var5, String val5) {
        String s = getProperty(key);
        s = YassUtils.replace(s, var1, val1);
        s = YassUtils.replace(s, var2, val2);
        s = YassUtils.replace(s, var3, val3);
        s = YassUtils.replace(s, var4, val4);
        s = YassUtils.replace(s, var5, val5);
        return s;
    }


    /**
     * Description of the Method
     */
    public void store() {
        String propDir = userDir + File.separator + yassDir;
        File propDirFile = new File(propDir);
        if (!propDirFile.exists()) {
            boolean ok = propDirFile.mkdir();
            if (!ok) {
                JOptionPane.showMessageDialog(null, "Cannot write properties to " + propDir, "Store properties", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        String propFile = propDir + File.separator + userProps;

        try {
            FileOutputStream fos = new FileOutputStream(propFile);
            storeToXML(fos, null);
            fos.flush();
            // Evtl. unn�tig?!
            fos.close();
            // Evtl. unn�tig?!
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error in storing properties to " + propFile, "Store properties", JOptionPane.ERROR_MESSAGE);
        }
    }
}

