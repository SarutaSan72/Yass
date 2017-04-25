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
public class DirPanel extends OptionsPanel {

    private static final long serialVersionUID = -1467886089536238190L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        setLabelWidth(140);
        addTextArea(I18.get("options_dir_programs"), "default-programs", 4);
        addComment(I18.get("options_dir_programs_comment"));

        addDirectory(I18.get("options_dir_songs"), "song-directory");
        addComment(I18.get("options_dir_songs_comment"));
        addDirectory(I18.get("options_dir_playlists"), "playlist-directory");
        addComment(I18.get("options_dir_playlists_comment"));
        addDirectory(I18.get("options_dir_covers"), "cover-directory");
        addComment(I18.get("options_dir_covers_comment"));
        addDirectory(I18.get("options_dir_imports"), "import-directory");
        addComment(I18.get("options_dir_imports_comment"));
        addSeparator();
        addChoice(I18.get("options_yass_languages_title"), I18.get("options_yass_languages"), "yass-languages", "yass-language");
        addComment(I18.get("options_yass_languages_comment"));

		/*
         *  addSeparator();
		 *  JButton b = addButton("", "Factory Settings: ", "Restore All");
		 *  b.addActionListener(
		 *  new ActionListener() {
		 *  public void actionPerformed(ActionEvent e) {
		 *  int ok = JOptionPane.showConfirmDialog(null, "<html>All user settings will be lost.<br><br>Continue?", "Restore Factory Settings - Yass", JOptionPane.OK_CANCEL_OPTION);
		 *  if (ok == JOptionPane.OK_OPTION) {
		 *  getProperties().reset();
		 *  getProperties().store();
		 *  }
		 *  OptionsPanel.loadProperties(getProperties());
		 *  }
		 *  });
		 *  / todo:
		 *  / (1) reload all panels
		 *  / (2) query directories
		 */
        //addDirectory("Temp:", "temp-dir");
        //addComment("Directory for temporary internal data.");
    }
}

