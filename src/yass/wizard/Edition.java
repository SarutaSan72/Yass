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

package yass.wizard;

import com.nexes.wizard.Wizard;
import yass.I18;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.io.File;
import java.net.URL;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class Edition extends JPanel {
    /**
     * Description of the Field
     */
    public final static String ID = "edition";
    private static final long serialVersionUID = -2933153105162906931L;
    private JComboBox<String> fc;
    private JTextField eField;


    /**
     * Constructor for the Edition object
     *
     * @param w Description of the Parameter
     */
    public Edition(Wizard w) {
        JLabel iconLabel = new JLabel();
        setLayout(new BorderLayout());
        iconLabel.setIcon(new ImageIcon(this.getClass().getResource("clouds.jpg")));
        add("West", iconLabel);
        add("Center", getContentPanel());
    }


    /**
     * Sets the songDir attribute of the Edition object
     *
     * @param songdir The new songDir value
     */
    public void setSongDir(String songdir) {
        fc.removeAllItems();
        fc.addItem("");
        if (songdir == null) {
            return;
        }
        File f = new File(songdir);
        if (!f.exists() || !f.isDirectory()) {
            return;
        }
        File folders[] = f.listFiles();
        if (folders == null) {
            return;
        }
        for (File folder : folders) {
            if (folder.isDirectory()) {
                fc.addItem(folder.getName());
            }
        }
    }


    /**
     * Gets the folder attribute of the Edition object
     *
     * @return The folder value
     */
    public String getFolder() {
        return (String) fc.getSelectedItem();
    }


    /**
     * Sets the folder attribute of the Edition object
     *
     * @param s The new folder value
     */
    public void setFolder(String s) {
        fc.getEditor().setItem(s);
    }


    /**
     * Gets the edition attribute of the Edition object
     *
     * @return The edition value
     */
    public String getEdition() {
        return eField.getText();
    }


    /**
     * Sets the edition attribute of the Edition object
     *
     * @param s The new edition value
     */
    public void setEdition(String s) {
        eField.setText(s);
    }


    /**
     * Gets the contentPanel attribute of the Edition object
     *
     * @return The contentPanel value
     */
    private JPanel getContentPanel() {
        JPanel content = new JPanel(new BorderLayout());
        JTextPane txt = new JTextPane();
        HTMLDocument doc = (HTMLDocument) txt.getEditorKitForContentType("text/html").createDefaultDocument();
        doc.setAsynchronousLoadPriority(-1);
        txt.setDocument(doc);
        txt.setEditable(false);
        URL url = I18.getResource("create_edition.html");
        try {
            txt.setPage(url);
        } catch (Exception ignored) {
        }
        content.add("Center", new JScrollPane(txt));

        fc = new JComboBox<>();
        fc.setEditable(true);

        JPanel fPanel = new JPanel(new BorderLayout());
        fPanel.add("West", new JLabel(I18.get("create_edition_folder")));
        fPanel.add("Center", fc);
        JPanel ePanel = new JPanel(new BorderLayout());
        ePanel.add("West", new JLabel(I18.get("create_edition_edition")));
        ePanel.add("Center", eField = new JTextField());

        JPanel all = new JPanel(new GridLayout(2, 1));
        all.add(fPanel);
        all.add(ePanel);
        content.add("South", all);
        return content;
    }
}

