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

package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class FiletypePanel extends OptionsPanel {

    private static final long serialVersionUID = 5688869646532944757L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        setLabelWidth(140);
        addText(I18.get("options_filetypes_songs"), "song-filetype");
        addComment(I18.get("options_filetypes_songs_comment"));
        addText(I18.get("options_filetypes_playlists"), "playlist-filetype");
        addComment(I18.get("options_filetypes_playlists_comment"));
        addSeparator();
        addText(I18.get("options_filetypes_audio"), "audio-files");
        addText(I18.get("options_filetypes_images"), "image-files");
        addText(I18.get("options_filetypes_videos"), "video-files");
        addComment(I18.get("options_filetypes_comment"));
        addSeparator();
        addBoolean(I18.get("options_filetypes_utf8_bom"), "utf8-without-bom", I18.get("options_filetypes_utf8_bom_enable"));
        addBoolean("", "utf8-always", I18.get("options_filetypes_utf8_always"));
        //addSeparator();
        //addBoolean(I18.get("options_filetypes_duet_seq"), "duet-sequential", I18.get("options_filetypes_duet_seq_enable"));
        //addComment(I18.get("options_filetypes_duet_seq_comment"));
    }
}

