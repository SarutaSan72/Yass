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
import yass.YassSong;
import yass.YassTable;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class Tap extends JPanel {
    /**
     * Description of the Field
     */
    public final static String ID = "tap";
    private static final long serialVersionUID = -7998365588082763662L;
    private Wizard wizard;
    private YassTable table = null;
    private JScrollPane scroll = null;
    private JCheckBox check = null;


    /**
     * Constructor for the Tap object
     *
     * @param w Description of the Parameter
     */
    public Tap(Wizard w) {
        wizard = w;
        JLabel iconLabel = new JLabel();
        setLayout(new BorderLayout());
        iconLabel.setIcon(new ImageIcon(this.getClass().getResource("clouds.jpg")));
        add("West", iconLabel);
        add("Center", getContentPanel());
    }


    /**
     * Gets the contentPanel attribute of the Tap object
     *
     * @return The contentPanel value
     */
    private JPanel getContentPanel() {
        JPanel content = new JPanel(new BorderLayout());
        JTextPane txt = new JTextPane();
        HTMLDocument doc = (HTMLDocument) txt.getEditorKitForContentType("text/html").createDefaultDocument();
        doc.setAsynchronousLoadPriority(-1);
        txt.setDocument(doc);
        URL url = I18.getResource("create_tap.html");
        try {
            txt.setPage(url);
        } catch (Exception ignored) {
        }
        txt.setEditable(false);
        content.add("North", new JScrollPane(txt));

        table = new YassTable();
        table.setEnabled(false);
        content.add("Center", scroll = new JScrollPane(table));
        content.add("South", check = new JCheckBox(I18.get("create_tap_edit")));
        check.setSelected(wizard.getValue("starteditor").equals("true"));
        check.addItemListener(
                new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if (check.isSelected()) {
                            wizard.setValue("starteditor", "true");
                        } else {
                            wizard.setValue("starteditor", "false");
                        }
                    }
                });
        scroll.setPreferredSize(new Dimension(100, 100));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return content;
    }


    /**
     * Description of the Method
     */
    public void updateTable() {
        table.setEnabled(true);
        table.removeAllRows();
        table.setText(wizard.getValue("melodytable"));
        table.setTitle(wizard.getValue("title"));
        table.setArtist(wizard.getValue("artist"));
        table.setMP3(YassSong.toFilename(wizard.getValue("artist") + " - " + wizard.getValue("title") + ".mp3"));
        table.setBPM(wizard.getValue("bpm"));
        table.getCommentRow("EDITION:").setComment(wizard.getValue("edition"));
        table.getCommentRow("GENRE:").setComment(wizard.getValue("genre"));
        table.getCommentRow("LANGUAGE:").setComment(wizard.getValue("language"));
        wizard.setValue("melodytable", table.getPlainText());
        //System.out.println(wizard.getValue("melodytable"));
        table.getColumnModel().getColumn(0).setPreferredWidth(10);
        table.getColumnModel().getColumn(0).setMaxWidth(10);
        table.setEnabled(false);
        table.revalidate();
        scroll.revalidate();
    }
}

