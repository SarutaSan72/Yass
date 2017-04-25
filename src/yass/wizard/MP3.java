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
import yass.YassUtils;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class MP3 extends JPanel {
    /**
     * Description of the Field
     */
    public final static String ID = "mp3";
    private static final long serialVersionUID = -35786632482964988L;
    private JTextField txtField;
    private JCheckBox skipCheck = null;
    private JButton browse;

    private Wizard wizard;


    /**
     * Constructor for the MP3 object
     *
     * @param w Description of the Parameter
     */
    public MP3(Wizard w) {
        wizard = w;
        JLabel iconLabel = new JLabel();
        setLayout(new BorderLayout());
        iconLabel.setIcon(new ImageIcon(this.getClass().getResource("clouds.jpg")));
        add("West", iconLabel);
        add("Center", getContentPanel());
    }


    /**
     * Gets the filename attribute of the MP3 object
     *
     * @return The filename value
     */
    public String getFilename() {
        return txtField.getText();
    }


    /**
     * Sets the filename attribute of the MP3 object
     *
     * @param s The new filename value
     */
    public void setFilename(String s) {
        if (s == null) {
            s = "";
        }
        txtField.setText(s);

        boolean onoff = skipCheck.isSelected();
        if (!onoff) {
            String data[] = YassUtils.getData(s);
            if (data == null) {
                wizard.setNextFinishButtonEnabled(false);
                return;
            }
            wizard.setValue("filename", getFilename());
            if (data[0] == null || data[0].trim().length() < 1) {
                data[0] = "UnknownTitle";
            }
            wizard.setValue("title", data[0]);
            if (data[1] == null || data[1].trim().length() < 1) {
                data[1] = "UnknownArtist";
            }
            wizard.setValue("artist", data[1]);
            wizard.setValue("genre", data[2]);
        } else {
            wizard.setValue("title", "UnknownTitle");
            wizard.setValue("artist", "UnknownArtist");
        }
        wizard.setNextFinishButtonEnabled(true);

		/*
         *  AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(file);
		 *  if (baseFileFormat instanceof TAudioFileFormat) {
		 *  Map properties = ((TAudioFileFormat) baseFileFormat).properties();
		 *  Boolean vbr = (Boolean) properties.get("mp3.vbr");
		 *  if (vbr == null) {
		 *  vbr = (Boolean) properties.get("vbr");
		 *  }
		 *  if (vbr != null) {
		 *  setProperty("mp3-vbr", vbr.booleanValue() ? "VBR" : "CBR");
		 *  } else {
		 *  setProperty("mp3-vbr", "CBR");
		 *  }
		 */
    }


    /**
     * Gets the contentPanel attribute of the MP3 object
     *
     * @return The contentPanel value
     */
    private JPanel getContentPanel() {
        JPanel content = new JPanel(new BorderLayout());
        JTextPane txt = new JTextPane();
        HTMLDocument doc = (HTMLDocument) txt.getEditorKitForContentType("text/html").createDefaultDocument();
        doc.setAsynchronousLoadPriority(-1);
        txt.setDocument(doc);
        URL url = I18.getResource("create_mp3.html");
        try {
            txt.setPage(url);
        } catch (Exception ignored) {
        }
        txt.setEditable(false);
        content.add("Center", new JScrollPane(txt));
        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.add("Center", txtField = new JTextField());
        filePanel.add("South", skipCheck = new JCheckBox(I18.get("create_mp3_skip")));
        txtField.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        setFilename(txtField.getText());
                    }
                });
        skipCheck.addItemListener(
                new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        boolean onoff = skipCheck.isSelected();
                        if (onoff) {
                            setFilename("");
                        } else {
                            setFilename(getFilename());
                        }
                        txtField.setEnabled(!onoff);
                        browse.setEnabled(!onoff);
                    }
                });

        browse = new JButton(I18.get("create_mp3_browse"));
        browse.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Frame f = new Frame();
                        FileDialog fd = new FileDialog(f, I18.get("create_mp3_title"), FileDialog.LOAD);
                        fd.setFilenameFilter(
                                new FilenameFilter() {
                                    // won't work on Windows - thx to Sun
                                    public boolean accept(File dir, String name) {
                                        return name.toLowerCase().endsWith(".mp3");
                                    }
                                });
                        fd.setFile("*.mp3");
                        fd.setVisible(true);
                        if (fd.getFile() != null) {
                            setFilename(fd.getDirectory() + File.separator + fd.getFile());
                        }
                        fd.dispose();
                        f.dispose();
                    }
                });
        filePanel.add("East", browse);
        content.add("South", filePanel);
        return content;
    }
}

