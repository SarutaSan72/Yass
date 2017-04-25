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
public class ImagesErrorPanel extends OptionsPanel {

    private static final long serialVersionUID = -7160783083146787083L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        setLabelWidth(140);
        addSeparator(I18.get("options_images_cover_size"));
        addText(I18.get("options_images_cover_size_max"), "cover-max-size");
        addText(I18.get("options_images_cover_size_minwidth"), "cover-min-width");
        addText(I18.get("options_images_cover_size_maxwidth"), "cover-max-width");
        addText(I18.get("options_images_cover_size_ratio"), "cover-ratio");
        addBoolean("", "use-cover-ratio", I18.get("options_images_cover_size_use_ratio"));
        //addComment("Defines group: Uncommon Cover Size.");
        addSeparator(I18.get("options_images_background_size"));
        addText(I18.get("options_images_background_size_max"), "background-max-size");
        addText(I18.get("options_images_background_size_minwidth"), "background-min-width");
        addText(I18.get("options_images_background_size_maxwidth"), "background-max-width");
        addText(I18.get("options_images_background_size_ratio"), "background-ratio");
        addBoolean("", "use-background-ratio", I18.get("options_images_background_size_use_ratio"));
        //addComment("Defines group: Uncommon Background Size.");
    }
}

