package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 22. August 2007
 */
public class CachePanel extends OptionsPanel {

    private static final long serialVersionUID = -7453496938869803003L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        addFile(I18.get("options_cache_songs"), "songlist-cache");
        addComment(I18.get("options_cache_songs_comment"));
        addFile(I18.get("options_cache_playlists"), "playlist-cache");
        addComment(I18.get("options_cache_playlists_comment"));

        addDirectory(I18.get("options_cache_covers"), "songlist-imagecache");
        addComment(I18.get("options_cache_covers_comment"));

        addComment(I18.get("options_cache_comment"));
    }
}

