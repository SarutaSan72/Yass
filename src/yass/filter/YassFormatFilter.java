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

package yass.filter;

import javazoom.spi.vorbis.sampled.file.VorbisFileFormatType;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import yass.YassSong;
import yass.YassSongList;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.util.Map;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassFormatFilter extends YassFilter {

    /**
     * Gets the iD attribute of the YassFilesFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "format";
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
        if (rule.equals("audio_vbr")) {
            return false;
        }
        if (rule.equals("audio_ogg")) {
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
        } else if (rule.equals("encoding_utf8")) {
            String enc = s.getEncoding();
            hit = enc.equals("UTF-8");
        } else if (rule.equals("encoding_ansi")) {
            String enc = s.getEncoding();
            hit = enc != null && enc.equals("Cp1252");
        } else if (rule.equals("encoding_other")) {
            String enc = s.getEncoding();
            hit = enc == null || !(enc.equals("Cp1252") || enc.equals("UTF-8") || enc.equals("UTF-16"));
        } else if (rule.equals("audio_vbr")) {
            String dir = s.getDirectory();
            String mp3 = s.getMP3();
            File file = new File(dir + File.separator + mp3);
            boolean isVBR = false;
            if (file.exists()) {
                try {
                    AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(file);
                    if (baseFileFormat instanceof TAudioFileFormat) {
                        Map properties = baseFileFormat.properties();
                        Boolean vbr = (Boolean) properties.get("mp3.vbr");
                        if (vbr == null) {
                            vbr = (Boolean) properties.get("vbr");
                        }
                        if (vbr != null) {
                            isVBR = vbr.booleanValue();
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            hit = isVBR;
        } else if (rule.equals("audio_ogg")) {
            String dir = s.getDirectory();
            String mp3 = s.getMP3();
            File file = new File(dir + File.separator + mp3);
            boolean ogg = false;
            try {
                AudioFileFormat aff = AudioSystem.getAudioFileFormat(file);
                if (aff.getType() == VorbisFileFormatType.OGG) {
                    ogg = true;
                }
            } catch (Exception ignored) {
            }
            hit = ogg;
        }
        return hit;
    }
}

