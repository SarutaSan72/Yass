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
import org.apache.commons.lang3.math.NumberUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class YassProperties extends Properties {
    private static final long serialVersionUID = -8189893110989853544L;
    private final String userDir = System.getProperty("user.home");
    private final String yassDir = ".yass";
    private final String userProps = "user.xml";
    private Hashtable<Object, Object> defaultProperties = null;

    public YassProperties() {
        super();
        load();
    }

    public boolean checkVersion() {
        String v = getProperty("yass-version");
        boolean old = v == null;
        if (!old) {
            old = v.startsWith("0.");
        }
        if (!old) {
            old = v.startsWith("1.");
        }
        if (!old) {
            old = v.startsWith("2.0");
        }
        if (!old) {
            old = v.startsWith("2.1.0");
        }
        if (!old) {
            old = v.startsWith("2.1.1");
        }
        return old;
    }

    public String getUserDir() {
        return userDir + File.separator + yassDir;
    }

    public String getDefaultProperty(String key) {
        if (defaultProperties == null) {
            defaultProperties = new Hashtable<>();
            setDefaultProperties(defaultProperties);
        }
        return (String)defaultProperties.get(key);
    }

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

            if (getProperty("key-17") == null)
                setProperty("key-17", "B");
            if (getProperty("key-18") == null)
                setProperty("key-18", "N");
            if (getProperty("before_next_ms") == null)
                setProperty("before_next_ms", "300");
            setupHyphenationDictionaries();
            return;
        } catch (Exception e) {
            // not exists
        }
        // user props not found; fall back to defaults
        setDefaultProperties(this);
        loadDevices();
        setupHyphenationDictionaries();
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
                if (m.contains("USBMIC")) {
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

    public void setDefaultProperties(Hashtable<Object,Object> p) {

        p.put("default-programs", "C:/Program Files/Ultrastar Deluxe|C:/Program Files (x86)/UltraStar Deluxe|C:/Program Files/Ultrastar|C:/Programme/Ultrastar Deluxe|C:/Ultrastar Deluxe|C:/Programme/Ultrastar|C:/Ultrastar|D:/Ultrastar|E:/Ultrastar|F:/Ultrastar|/home/.ultrastardx|/home/.ultrastar|C:/Program Files (x86)/UltraStar Deluxe WorldParty|C:/Program Files/UltraStar Deluxe WorldParty|D:/UltraStar Deluxe WorldParty|C:/Program Files (x86)/Vocaluxe|C:/Program Files/Vocaluxe|D:/Vocaluxe|C:/Program Files (x86)/Performous|C:/Program Files/Performous");
        // dirs
        p.put("song-directory", userDir + File.separator + "Songs");
        p.put("playlist-directory", userDir + File.separator + "Playlists");
        p.put("cover-directory", userDir + File.separator + "Covers");
        p.put("import-directory", "");

        p.put("yass-version", YassActions.VERSION);

        p.put("yass-language", "default");
        p.put("yass-languages", "default|en|de|hu|pl|es");

        //filetype association
        p.put("song-filetype", ".txt");
        p.put("playlist-filetype", ".upl");

        p.put("audio-files", ".mp3");
        p.put("image-files", ".jpg|.jpeg");
        p.put("video-files", ".mpg|.mpeg|.avi|.divx");
        p.put("cover-id", "[CO]");
        p.put("background-id", "[BG]");
        p.put("video-id", "[VD#*]");
        p.put("videodir-id", "[VIDEO]");

        p.put("correct-uncommon-pagebreaks", "unknown");
        p.put("correct-uncommon-spacing", "");

        p.put("articles", "EN:the |a |an :DE:der |die |das |ein |eine :FR:le |la |les |l'|un |une |des :ES:el |la |los |las:HU:a |az ");
        p.put("use-articles", "true");

        // print
        p.put("songlist-pdf", userDir + File.separator + yassDir + File.separator + "songlist.pdf");

        // cache
        p.put("songlist-cache", userDir + File.separator + yassDir + File.separator + "songlist.txt");
        p.put("playlist-cache", userDir + File.separator + yassDir + File.separator + "playlists.txt");
        p.put("songlist-imagecache", userDir + File.separator + yassDir + File.separator + "covers-cache");
        p.put("temp-dir", userDir + File.separator + yassDir + File.separator + "temp");
        p.put("lyrics-cache", userDir + File.separator + yassDir + File.separator + "lyrics.txt");

        // metadata
        p.put("language-tag", "English|EN|German|DE|Spanish|ES|French|FR|Other|NN");
        p.put("language-more-tag", "Chinese|CH|Croatian|HR|Danish|DA|Hungarian|HU|Italian|IT|Japanese|JA|Korean|KR|Polish|PL|Russian|RU|Swedish|SV|Turkish|TR");
        // ISO 639-2 http|//www.loc.gov/standards/iso639-2/php/English_list.php
        p.put("genre-tag", "Blues|Darkwave|Musical|Metal|Oldies|Pop|Punk|Reggae|Rock|Other");
        p.put("genre-more-tag", "Acid|Alternative|Anime|Classical|Country|Dance|Death Metal|Disco|Electronic|Folk|Funk|Game|Gangsta|Gospel|Gothic|Grunge|Hip-Hop|House|Industrial|Jazz|JPop|MORE|New Age|Noise|R&B|Rave|Rap|Retro|Rock & Roll|Showtunes|Ska|Soundtrack|Soul|Techno|Trance|Tribal|Vocal");
        // http|//de.wikipedia.org/wiki/Liste_der_ID3-Genres
        p.put("edition-tag", "Birthday Party|Child's Play|Christmas|Greatest Hits|Halloween|Hits of the 60s|Kuschelrock|New Year's Eve|Summer|Sodamakers");
        // http|//de.wikipedia.org/wiki/Compilation
        p.put("note-naming-h", "DE RU PL NO FI SE");
        p.put("duetsinger-tag", "P");

        // errors
        p.put("valid-tags", "TITLE ARTIST LANGUAGE EDITION <br> GENRE ALBUM YEAR CREATOR AUTHOR ID <br> MP3 COVER BACKGROUND VIDEO VIDEOGAP START END DUETSINGERP1 DUETSINGERP2 DUETSINGERP3 DUETSINGERP4 P1 P2 P3 P4 RELATIVE BPM GAP LENGTH PREVIEWSTART MEDLEYSTARTBEAT MEDLEYENDBEAT ENCODING COMMENT ");
        p.put("valid-lines", "#:*FRG-EP");
        p.put("max-points", "7500");
        p.put("max-golden", "1250");
        p.put("max-linebonus", "1000");
        p.put("golden-allowed-variance", "250");
        p.put("freestyle-counts", "false");
        p.put("touching-syllables", "false");
        p.put("correct-uncommon-pagebreaks-fix", "0");

        p.put("font-file", "Candara Bold");
        p.put("font-files", "Arial Bold|Candara Bold|Roboto Regular");
        p.put("font-file-custom", "");
        p.put("font-size", "28");
        p.put("char-spacing", "0");
        p.put("text-max-width", "800");

        p.put("cover-max-size", "128");
        p.put("cover-min-width", "200");
        p.put("cover-max-width", "800");
        p.put("cover-ratio", "100");
        p.put("use-cover-ratio", "true");

        p.put("background-max-size", "1024");
        p.put("background-min-width", "800");
        p.put("background-max-width", "1024");
        p.put("background-ratio", "133");
        p.put("use-background-ratio", "true");

        //mic
        p.put("control-mic", "");
        p.put("control-mics", "");

        //editor
        p.put("editor-layout", "East");
        p.put("editor-layouts", "East|West");
        p.put("lyrics-width", "450");
        p.put("lyrics-min-height", "120");
        p.put("lyrics-font-size", "14");

        p.put("note-color-8", "#dd9966"); // warning
        p.put("note-color-7", "#dd6666"); // error
        p.put("note-color-6", "#ffccff"); // freestyle
        p.put("note-color-5", "#66aa66"); // rap
        p.put("note-color-4", "#bbffbb"); // golden rap
        p.put("note-color-3", "#f0f066"); // golden
        p.put("note-color-2", "#4488cc"); // active
        p.put("note-color-1", "#777777"); // shade
        p.put("note-color-0", "#dddddd"); // note
        p.put("shade-notes", "true");
        p.put("dark-mode", "false");

        p.put("color-7", "#cccccc");
        p.put("color-6", "#444444");
        p.put("color-5", "#88cccc");
        p.put("color-4", "#cc88cc");
        p.put("color-3", "#f09944");
        p.put("color-2", "#f0f044");
        p.put("color-1", "#88cc44");
        p.put("color-0", "#4488cc");

        p.put("hi-color-7", "#669966");
        p.put("hi-color-6", "#666699");
        p.put("hi-color-5", "#994d99");
        p.put("hi-color-4", "#99994d");
        p.put("hi-color-3", "#4d9999");
        p.put("hi-color-2", "#4d4d99");
        p.put("hi-color-1", "#4d994d");
        p.put("hi-color-0", "#4d4d4d");

        p.put("group-title", "all|#|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z");
        p.put("group-artist", "all|generic|others");
        p.put("group-genre", "all|unspecified|generic");
        p.put("group-language", "all|unspecified|English;German|generic");
        p.put("group-edition", "all|unspecified|generic");
        p.put("group-album", "all|unspecified|generic");
        p.put("group-playlist", "all|generic|unspecified");
        p.put("group-year", "all|unspecified|1930-1939|1940-1949|1950-1959|1960-1969|1970-1979|1980-1989|1990-1999|2000-2009|2010-2019|2020-2029|generic");
        p.put("group-folder", "all|generic");
        p.put("group-length", "all|unspecified|0 - 1:30|1:31 - 3:00|3:01 - 4:30|4:31 - 6:00|6:01 -");
        p.put("group-files", "all|duplicates|video|no_video|no_video_background|no_background|no_cover|uncommon_filenames|uncommon_cover_size|uncommon_background_size");
        p.put("group-format", "all|encoding_utf8|encoding_ansi|encoding_other|audio_vbr|audio_ogg");
        p.put("group-tags", "all|previewstart|medleystartbeat|start|end|relative|gap<5|gap>30|gap>60|bpm<200|bpm>400");
        p.put("group-errors", "all|all_errors|critical_errors|major_errors|tag_errors|file_errors|page_errors|text_errors");
        p.put("group-duets", "all|solos|duets|trios|quartets|choirs");
        p.put("group-stats", "all|pages_common|pages_pages_0-30|pages_pages_31-80|pages_pages_81-|golden_golden_3-20|rap_rap_1-|freestyle_freestyle_0-30|notesperpage_notesperpage_0-5|notesperpage_notesperpage_5.1-|speeddist_slow|speeddist_averagespeed|speeddist_fast|pitchrange_monoton|pitchrange_melodic|pitchrange_smooth|pitchrange_bumpy|speeddist_longbreath");
        p.put("group-instrument", "all|white|black|12-white|8-white|25-keys|13-keys");

        p.put("group-min", "3");

        p.put("hyphenations", "EN|DE|ES|FR|IT|PL|PT|RU|TR|ZH");
        p.put("dicts", "EN|DE");
        p.put("dict-map", "English|EN|German|DE|French|EN|Croatian|EN|Hungarian|EN|Italian|EN|Japanese|EN|Polish|EN|Russian|EN|Spanish|EN|Swedish|EN|Turkish|EN");
        p.put("user-dicts", userDir + File.separator + yassDir);

        p.put("utf8-without-bom", "true");
        p.put("utf8-always", "true");
        //p.put("duet-sequential", "true");

        p.put("floatable", "false");

        p.put("mouseover", "false");
        p.put("sketching", "false");
        p.put("sketching-playback", "false");
        p.put("show-note-heightnum", "false");
        p.put("show-note-height", "true");
        p.put("show-note-length", "true");
        p.put("show-note-beat", "false");
        p.put("show-note-scale", "false");
        p.put("auto-trim", "false");
        p.put("playback-buttons", "true");
        p.put("record-timebase", "2");

        p.put("use-sample", "true");
        p.put("typographic-apostrophes", "false");
        p.put("capitalize-rows", "false");

        // 0=next_note, 1=prev_note, 2=page_down, 3=page_up, 4=init, 5=init_next, 6=right, 7=left, 8=up, 9=down, 10=lengthen, 11=shorten, 12=play, 13=play_page, 14=scroll_left, 15=scroll_right, 16=one_page
        p.put("key-0", "NUMPAD6");
        p.put("key-1", "NUMPAD4");
        p.put("key-2", "NUMPAD2");
        p.put("key-3", "NUMPAD8");

        p.put("key-4", "NUMPAD0");
        p.put("key-5", "DECIMAL");

        p.put("key-6", "NUMPAD3");
        p.put("key-7", "NUMPAD1");
        p.put("key-8", "SUBTRACT");
        p.put("key-9", "ADD");
        p.put("key-10", "NUMPAD9");
        p.put("key-11", "NUMPAD7");

        p.put("key-12", "NUMPAD5");
        p.put("key-13", "P");
        p.put("key-14", "H");
        p.put("key-15", "J");
        p.put("key-16", "K");
        p.put("key-17", "B");
        p.put("key-18", "N");

        p.put("screenkey-0", "ESCAPE");
        p.put("screenkey-1", "PAUSE");
        p.put("screenkey-2", "F5");

        p.put("screenkey-3", "UP");
        p.put("screenkey-4", "RIGHT");
        p.put("screenkey-5", "DOWN");
        p.put("screenkey-6", "LEFT");
        p.put("screenkey-7", "ENTER");

        p.put("screenkey-8", "A");
        p.put("screenkey-9", "S");
        p.put("screenkey-10", "D");
        p.put("screenkey-11", "F");
        p.put("screenkey-12", "G");
        p.put("screenkey-13", "H");
        p.put("screenkey-14", "J");
        p.put("screenkey-15", "K");
        p.put("screenkey-16", "L");
        p.put("screenkey-17", "OEM_3");
        p.put("screenkey-18", "OEM_7");
        p.put("screenkey-19", "OEM_2");

        p.put("screenkey-20", "UP|NUMPAD");
        p.put("screenkey-21", "RIGHT|NUMPAD");
        p.put("screenkey-22", "DOWN|NUMPAD");
        p.put("screenkey-23", "LEFT|NUMPAD");
        p.put("screenkey-24", "ENTER|NUMPAD");

        p.put("screenkey-25", "---");
        p.put("screenkey-26", "---");
        p.put("screenkey-27", "---");
        p.put("screenkey-28", "---");
        p.put("screenkey-29", "---");
        p.put("screenkey-30", "---");
        p.put("screenkey-31", "---");
        p.put("screenkey-32", "---");
        p.put("screenkey-33", "---");
        p.put("screenkey-34", "---");
        p.put("screenkey-35", "---");
        p.put("screenkey-36", "---");

        p.put("screenkey-37", "W");
        p.put("screenkey-38", "D");
        p.put("screenkey-39", "S");
        p.put("screenkey-40", "A");
        p.put("screenkey-41", "SPACE");

        p.put("screenkey-42", "---");
        p.put("screenkey-43", "---");
        p.put("screenkey-44", "---");
        p.put("screenkey-45", "---");
        p.put("screenkey-46", "---");
        p.put("screenkey-47", "---");
        p.put("screenkey-48", "---");
        p.put("screenkey-49", "---");
        p.put("screenkey-50", "---");
        p.put("screenkey-51", "---");
        p.put("screenkey-52", "---");
        p.put("screenkey-53", "---");

        p.put("player1_difficulty", "1");
        p.put("player2_difficulty", "1");
        p.put("player3_difficulty", "1");
        p.put("game_sorting", "edition");
        p.put("game_group", "all");
        p.put("game_mode", "lines");

        p.put("screen-groups", "title|artist|genre|language|edition|folder|playlist|year");

        // advanced
        p.put("before_next_ms", "300");
        p.put("seek-in-offset", "0");
        p.put("seek-out-offset", "0");
        p.put("seek-in-offset-ms", "0");
        p.put("seek-out-offset-ms", "0");
        p.put("print-plugins", "yass.print.PrintBlocks|yass.print.PrintPlain|yass.print.PrintPlainLandscape|yass.print.PrintDetails");
        p.put("filter-plugins", "yass.filter.YassTitleFilter|yass.filter.YassArtistFilter|yass.filter.YassLanguageFilter|yass.filter.YassEditionFilter|yass.filter.YassGenreFilter|yass.filter.YassAlbumFilter|yass.filter.YassPlaylistFilter|yass.filter.YassYearFilter|yass.filter.YassLengthFilter|yass.filter.YassFolderFilter|yass.filter.YassFilesFilter|yass.filter.YassFormatFilter|yass.filter.YassTagsFilter|yass.filter.YassMultiPlayerFilter|yass.filter.YassInstrumentFilter|yass.filter.YassErrorsFilter|yass.filter.YassStatsFilter");
        p.put("stats-plugins", "yass.stats.YassBasicStats|yass.stats.YassTimeStats|yass.stats.YassPitchStats");
        p.put("screen-plugins", "yass.screen.YassStartScreen|yass.screen.YassSelectGameScreen|yass.screen.YassSelectControllerScreen|yass.screen.YassSelectDeviceScreen|yass.screen.YassSelectDifficultyScreen|yass.screen.YassSelectSortingScreen|yass.screen.YassSelectGroupScreen|yass.screen.YassSelectSongScreen|yass.screen.YassPlaySongScreen|yass.screen.YassViewScoreScreen|yass.screen.YassHighScoreScreen|yass.screen.YassEnterScoreScreen|yass.screen.YassJukeboxScreen|yass.screen.YassCreditsScreen|yass.screen.YassStatsScreen");
        p.put("jukebox", "highscore:topten-beginner|highscore:topten-standard|highscore:topten-expert|jukebox:topten|credits:yass|credits:thirdparty|credits:thanks|jukebox:random");

        p.put("debug-memory", "false");
        p.put("debug-score", "false");
        p.put("debug-waveform", "false");

        //piano
        p.put("piano-volume", "100");

        //non-editable
        p.put("welcome", "true");
        p.put("recent-files", "");
    }

    public String getProperty(String key, String[] var, String[] val) {
        String s = getProperty(key);
        for (int i = 0; i < var.length; i++) {
            s = YassUtils.replace(s, var[i], val[i]);
        }
        return s;
    }

    public String getProperty(String key, String var, String val) {
        String s = getProperty(key);
        s = YassUtils.replace(s, var, val);
        return s;
    }

    public String getProperty(String key, String var1, String val1, String var2, String val2) {
        String s = getProperty(key);
        s = YassUtils.replace(s, var1, val1);
        s = YassUtils.replace(s, var2, val2);
        return s;
    }

    public String getProperty(String key, String var1, String val1, String var2, String val2, String var3, String val3) {
        String s = getProperty(key);
        s = YassUtils.replace(s, var1, val1);
        s = YassUtils.replace(s, var2, val2);
        s = YassUtils.replace(s, var3, val3);
        return s;
    }

    public String getProperty(String key, String var1, String val1, String var2, String val2, String var3, String val3, String var4, String val4) {
        String s = getProperty(key);
        s = YassUtils.replace(s, var1, val1);
        s = YassUtils.replace(s, var2, val2);
        s = YassUtils.replace(s, var3, val3);
        s = YassUtils.replace(s, var4, val4);
        return s;
    }

    public String getProperty(String key, String var1, String val1, String var2, String val2, String var3, String val3, String var4, String val4, String var5, String val5) {
        String s = getProperty(key);
        s = YassUtils.replace(s, var1, val1);
        s = YassUtils.replace(s, var2, val2);
        s = YassUtils.replace(s, var3, val3);
        s = YassUtils.replace(s, var4, val4);
        s = YassUtils.replace(s, var5, val5);
        return s;
    }

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
            fos.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error in storing properties to " + propFile, "Store properties", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean getBooleanProperty(String s) {
        String p = getProperty(s);
        return p != null && p.equals("true");
    }

    public int getIntProperty(String key) {
        String strValue = getProperty(key);
        return NumberUtils.toInt(strValue, 0);
    }

    public boolean isUncommonSpacingAfter() {
        return "after".equals(getProperty("correct-uncommon-spacing"));
    }

    public boolean isLegacyDuet() {
        return "DUETSINGERP".equals(getProperty("duetsinger-tag", "P"));
    }

    public void setupHyphenationDictionaries() {
        String hyphenationLanguages = getProperty("hyphenations");
        if (StringUtils.isEmpty(hyphenationLanguages)) {
            System.out.println("No hyphenation languages have been setup, skipping this now...");
            return;
        }
        Map<String, String> languageMap = initLanguageMap();
        Set<Path> paths = findPath(List.of("UltraStar-Creator"));
        if (paths == null || paths.isEmpty()) {
            System.out.println("No valid program paths were found, skipping this now...");
            return;
        }
        String[] languages = hyphenationLanguages.split("\\|");
        boolean changes = false;
        for (String language : languages) {
            String prop = getProperty("hyphenations_" + language);
            if (StringUtils.isEmpty(prop)) {
                for (Path path : paths) {
                    if (languageMap.get(language) == null) {
                        System.out.println("Language " + language + " is not supported, skipping this now...");
                        continue;
                    }
                    Path dictionary = Path.of(path.toString(), languageMap.get(language));
                    if (Files.exists(dictionary)) {
                        changes = true;
                        setProperty("hyphenations_" + language, dictionary.toString());
                        continue;
                    }
                    System.out.println("Dictionary for " + language + " was not supported, skipping this now...");
                }
            }
        }
        if (changes) {
            store();
        }
    }

    private Map<String, String> initLanguageMap() {
        Map<String, String> languageMap = new HashMap<>();
        languageMap.put("EN", "English.txt");
        languageMap.put("FR", "French.txt");
        languageMap.put("DE", "German.txt");
        languageMap.put("IT", "Italian.txt");
        languageMap.put("PL", "Polish.txt");
        languageMap.put("PT", "Portuguese.txt");
        languageMap.put("ES", "Spanish.txt");
        languageMap.put("SE", "Swedish.txt");
        return languageMap;
    }

    private Set<Path> findPath(List<String> additionalPrograms) {
        String defaultPaths = getProperty("default-programs");
        if (StringUtils.isEmpty(defaultPaths)) {
            return null;
        }
        Set<Path> validPaths = new HashSet<>();
        Set<String> parentPaths = new HashSet<>();
        String[] paths = defaultPaths.split("\\|");
        for (String path : paths) {
            Path tempPath = Path.of(path);
            if (!Files.exists(tempPath)) {
                continue;
            }
            parentPaths.add(tempPath.toString());
            parentPaths.add(tempPath.getParent().toString());
            validPaths.add(tempPath);
        }
        if (validPaths.isEmpty()) {
            return null;
        }
        for (String additionalProgram : additionalPrograms) {
             for (String parentPath : parentPaths) {
                 Path tempPath = Path.of(parentPath, additionalProgram);
                 if (!Files.exists(tempPath)) {
                     continue;
                 }
                 validPaths.add(tempPath);
             }
        }
        return validPaths;
    }
}

