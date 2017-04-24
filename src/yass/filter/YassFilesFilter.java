/*
 * Yass - Karaoke Editor
 * Copyright (C) 2014 Saruta
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

package yass.filter;

import yass.YassSong;
import yass.YassSongList;
import yass.YassTable;
import yass.YassUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassFilesFilter extends YassFilter {

    Vector<YassSong> songs = null;
    Hashtable<String, YassSong> hash = null;

    YassTable t = null;

    String coverID = null, backgroundID, videoID, videodirID;

    int maxCoverSize, minCoverWidth, maxCoverWidth, coverRatio,
            maxBackgroundSize, minBackgroundWidth, maxBackgroundWidth, backgroundRatio;
    boolean checkCoverRatio = true, checkBackgroundRatio = true;


    /**
     * Gets the iD attribute of the YassFilesFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "files";
    }


    /**
     * Description of the Method
     *
     * @param allsongs Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean start(Vector<?> allsongs) {
        if (rule.equals("uncommon_cover_size") || rule.equals("uncommon_background_size")) {
            Properties p = getProperties();
            try {
                maxCoverSize = Integer.parseInt((String) p.get("cover-max-size"));
                minCoverWidth = Integer.parseInt((String) p.get("cover-min-width"));
                maxCoverWidth = Integer.parseInt((String) p.get("cover-max-width"));
                coverRatio = Integer.parseInt((String) p.get("cover-ratio"));
                checkCoverRatio = p.get("use-cover-ratio").equals("true");

                maxBackgroundSize = Integer.parseInt((String) p.get("background-max-size"));
                minBackgroundWidth = Integer.parseInt((String) p.get("background-min-width"));
                maxBackgroundWidth = Integer.parseInt((String) p.get("background-max-width"));
                backgroundRatio = Integer.parseInt((String) p.get("background-ratio"));
                checkBackgroundRatio = p.get("use-background-ratio").equals("true");
            } catch (Exception e) {
                System.out.println("Files Errors: Cannot parse properties.");
                //e.printStackTrace();
            }
        }

        if (rule.equals("duplicates")) {
            songs = new Vector<>();
            hash = new Hashtable<>();

            for (Enumeration<?> e = allsongs.elements(); e.hasMoreElements(); ) {
                YassSong s = (YassSong) e.nextElement();
                String title = s.getTitle();
                String artist = s.getArtist();
                String at = artist + " - " + title;

                YassSong s2 = hash.get(at);
                if (s2 != null) {
                    if (!songs.contains(s2)) {
                        songs.addElement(s2);
                    }
                    songs.addElement(s);
                } else {
                    hash.put(at, s);
                }

                if (isInterrupted) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Description of the Method
     */
    public void stop() {
        if (songs != null) {
            songs.clear();
        }
        if (hash != null) {
            hash.clear();
        }
    }


    /**
     * Gets the extraInfo attribute of the YassFilesFilter object
     *
     * @return The extraInfo value
     */
    public int getExtraInfo() {
        return YassSongList.FOLDER_COLUMN;
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean count() {
        if (rule.equals("uncommon_cover_size")) {
            return false;
        }
        if (rule.equals("uncommon_background_size")) {
            return false;
        }
        if (rule.equals("uncommon_filenames")) {
            return false;
        }
        if (rule.equals("no_cover")) {
            return false;
        }
        if (rule.equals("no_background")) {
            return false;
        }
        return true;
    }


    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean accept(YassSong s) {
        boolean hit = false;

        if (rule.equals("all")) {
            hit = true;
        } else if (rule.equals("video")) {
            hit = s.getComplete().equals("V");
        } else if (rule.equals("no_video")) {
            hit = !s.getComplete().equals("V");
        } else if (rule.equals("no_video_background")) {
            hit = s.getComplete().equals("b");
        } else if (rule.equals("no_background")) {
            String dir = s.getDirectory();
            String bg = s.getBackground();
            if (bg != null) {
                File f = new File(dir + File.separator + bg);
                if (!f.exists()) {
                    hit = true;
                }
            } else {
                hit = true;
            }
        } else if (rule.equals("no_cover")) {
            String dir = s.getDirectory();
            String co = s.getCover();
            if (co != null) {
                File f = new File(dir + File.separator + co);
                if (!f.exists()) {
                    hit = true;
                }
            } else {
                hit = true;
            }
        } else if (rule.equals("duplicates")) {
            hit = songs.contains(s);
        } else if (rule.equals("uncommon_cover_size")) {
            String dir = s.getDirectory();
            String cover = s.getCover();
            if (cover != null) {
                File f = new File(dir + File.separator + cover);
                BufferedImage img = null;
                try {
                    img = YassUtils.readImage(f);
                } catch (Exception e) {
                }
                if (img != null) {
                    double size = (int) (10 * f.length() / 1024.0) / 10.0;
                    int width = img.getWidth();
                    int height = img.getHeight();
                    double actratio = width / (double) height;
                    int intratio = (int) Math.round(actratio * 100);
                    hit = size > maxCoverSize || width > maxCoverWidth || width < minCoverWidth || (checkCoverRatio && intratio != coverRatio);
                }
            }
        } else if (rule.equals("uncommon_background_size")) {
            String dir = s.getDirectory();
            String background = s.getBackground();
            if (background != null) {
                File f = new File(dir + File.separator + background);
                BufferedImage img = null;
                try {
                    img = YassUtils.readImage(f);
                } catch (Exception e) {
                }
                if (img != null) {
                    double size = (int) (10 * f.length() / 1024.0) / 10.0;
                    int width = img.getWidth();
                    int height = img.getHeight();
                    double actratio = width / (double) height;
                    int intratio = (int) Math.round(actratio * 100);

                    hit = size > maxBackgroundSize || width > maxBackgroundWidth || width < minBackgroundWidth || (checkBackgroundRatio && intratio != backgroundRatio);
                }
            }
        } else if (rule.equals("uncommon_filenames")) {
            if (t == null) {
                t = new YassTable();
            }

            if (coverID == null) {
                Properties prop = getProperties();
                coverID = prop.getProperty("cover-id");
                backgroundID = prop.getProperty("background-id");
                videoID = prop.getProperty("video-id");
                videodirID = prop.getProperty("videodir-id");
            }

            String filename = s.getDirectory() + File.separator + s.getFilename();
            t.removeAllRows();
            if (!t.loadFile(filename)) {
                return false;
            }

            String artist = YassSong.toFilename(t.getArtist());
            String title = YassSong.toFilename(t.getTitle());

            String text = t.getFilename();
            String audio = t.getMP3();
            String version = t.getVersion();
            String cover = t.getCover();
            String background = t.getBackgroundTag();
            String video = t.getVideo();
            String videogap = new Double(t.getVideoGap()).toString().replace('.', ',');
            if (videogap.endsWith(",0")) {
                videogap = videogap.substring(0, videogap.length() - 2);
            }

            String at = artist + " - " + title;

            String name = s.getDirectory();
            int i = name.lastIndexOf(File.separator);
            name = name.substring(i + 1);
            String std = (video != null) ? at + " " + videodirID : at;
            if (!name.equals(std)) {
                // System.out.println("Uncommon foldername: " + name);
                // System.out.println("Standard foldername: " + std);
                return true;
            }

            String extension = null;
            i = text.lastIndexOf(".");
            extension = text.substring(i);
            extension = extension.toLowerCase();

            if (version == null || version.length() < 1) {
                std = at + extension;
            } else {
                std = at + " [" + version + "]" + extension;
            }

            if (!text.equals(std)) {
                // System.out.println("Uncommon karaoke filename: " + text);
                return true;
            }

            if (cover != null) {
                i = cover.lastIndexOf(".");
                extension = cover.substring(i);
                extension = extension.toLowerCase();
                std = at + " [CO]" + extension;
                if (!cover.equals(std)) {
                    // System.out.println("Uncommon cover filename: " + cover);
                    return true;
                }
            }
            if (background != null) {
                i = background.lastIndexOf(".");
                extension = background.substring(i);
                extension = extension.toLowerCase();
                std = at + " [BG]" + extension;
                if (!background.equals(std)) {
                    // System.out.println("Uncommon background filename: " + background);
                    return false;
                }
            }
            if (audio != null) {
                i = audio.lastIndexOf(".");
                extension = audio.substring(i);
                extension = extension.toLowerCase();
                std = at + extension;
                if (!audio.equals(std)) {
                    // System.out.println("Uncommon audio filename: " + audio);
                    return true;
                }
            }
            if (video != null) {
                i = video.lastIndexOf(".");
                extension = video.substring(i);
                extension = extension.toLowerCase();
                std = at + " [VD#" + videogap + "]" + extension;
                if (!video.equals(std)) {
                    // System.out.println("Uncommon video filename: " + video);
                    return true;
                }
            }
        }
        return hit;
    }

    public boolean showTitle() {
        return false;
    }
}

