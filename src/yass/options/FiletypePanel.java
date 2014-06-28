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
    }
}

