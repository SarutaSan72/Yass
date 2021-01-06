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
import yass.YassHyphenator;
import yass.YassTable;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class LyricsForMIDI extends JPanel {
    /**
     * Description of the Field
     */
    public final static String ID = "lyrics_for_midi";
    private static final long serialVersionUID = 3903516930032841756L;
    private Wizard wizard;
    private JTextArea lyricsArea = null;
    private YassTable table = null;
    private YassHyphenator hyphenator = null;
    private JComboBox<String> langCombo = null;
    private JCheckBox utf8 = null;


    /**
     * Constructor for the Lyrics object
     *
     * @param w Description of the Parameter
     */
    public LyricsForMIDI(Wizard w) {
        wizard = w;
        JLabel iconLabel = new JLabel();
        setLayout(new BorderLayout());
        iconLabel.setIcon(new ImageIcon(this.getClass().getResource("clouds.jpg")));
        add("West", iconLabel);
        add("Center", getContentPanel());
    }


    /**
     * Gets the contentPanel attribute of the Lyrics object
     *
     * @return The contentPanel value
     */
    private JPanel getContentPanel() {
        JPanel content = new JPanel(new BorderLayout());
        JTextPane txt = new JTextPane();
        HTMLDocument doc = (HTMLDocument) txt.getEditorKitForContentType("text/html").createDefaultDocument();
        doc.setAsynchronousLoadPriority(-1);
        txt.setDocument(doc);
        URL url = I18.getResource("create_lyrics_midi.html");
        try {
            txt.setPage(url);
        } catch (Exception ignored) {
        }
        txt.setEditable(false);
        content.add("North", new JScrollPane(txt));
        lyricsArea = new JTextArea(10, 20);

        hyphenator = new YassHyphenator(null);

        JPanel buttons = new JPanel(new GridLayout(1, 3));
        JPanel hyPanel = new JPanel(new BorderLayout());
        utf8 = new JCheckBox("UTF-8");
        utf8.setSelected(wizard.getValue("encoding").equals("utf8"));
        utf8.addItemListener(
                new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if (utf8.isSelected()) {
                            wizard.setValue("encoding", "utf8");
                        } else {
                            wizard.setValue("encoding", "");
                        }
                    }
                });
        buttons.add(utf8);
        buttons.add(new JLabel(""));
        hyPanel.add("West", langCombo = new JComboBox<>());
        JButton hyphButton = new JButton(I18.get("create_lyrics_midi_hyphenate"));
        hyPanel.add("Center", hyphButton);
        buttons.add(hyPanel);
        hyphButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String lang = (String) langCombo.getSelectedItem();
                        if (lang.toLowerCase().equals("zh")) // // special case: Chinese
                        {
                            String txt = lyricsArea.getText();
                            // bigboss97: In Chinese, each word (one UTF-8 character) has exactly one syllable. Basically you only have to place a hyphen everywhere between two characters.
                            txt = txt.replaceAll(".", "$0 ");
                            txt = txt.replaceAll("[ ]{2,}", " ").trim();
                            lyricsArea.setText(txt);
                        }
                        else {
                            hyphenator.setLanguage(lang);
                            String txt = lyricsArea.getText();
                            txt = hyphenator.hyphenateText(txt);
                            lyricsArea.setText(txt);
                        }
                    }
                });

        JPanel sp = new JPanel(new BorderLayout());
        JScrollPane txtScroll;
        sp.add("West", txtScroll = new JScrollPane(table = new YassTable()));
        txtScroll.setPreferredSize(new Dimension(150, 100));
        txtScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        table.getColumnModel().getColumn(0).setMinWidth(10);
        table.getColumnModel().getColumn(0).setMaxWidth(10);
        table.getColumnModel().getColumn(1).setMinWidth(30);
        table.getColumnModel().getColumn(1).setMaxWidth(30);
        table.getColumnModel().getColumn(2).setMinWidth(20);
        table.getColumnModel().getColumn(2).setMaxWidth(20);
        table.getColumnModel().getColumn(3).setMinWidth(20);
        table.getColumnModel().getColumn(3).setMaxWidth(20);
        table.getColumnModel().getColumn(4).setMinWidth(70);
        table.getColumnModel().getColumn(4).setMaxWidth(70);
        table.setEnabled(false);
        sp.add("Center", new JScrollPane(lyricsArea));
        sp.add("South", buttons);
        content.add("Center", sp);
        return content;
    }


    /**
     * Sets the hyphenations attribute of the LyricsForMIDI object
     *
     * @param hy The new hyphenations value
     */
    public void setHyphenations(String hy) {
        hyphenator.setHyphenations(hy);
        langCombo.removeAllItems();
        StringTokenizer st = new StringTokenizer(hy, "|");
        while (st.hasMoreTokens()) {
            String language = st.nextToken();
            langCombo.addItem(language);
        }
    }


    /**
     * Gets the text attribute of the Lyrics object
     *
     * @return The text value
     */
    public String getText() {
        return lyricsArea.getText();
    }

    /**
     * Sets the text attribute of the Lyrics object
     *
     * @param s The new text value
     */
    public void setText(String s) {
        if (s == null) {
            s = "";
        }
        lyricsArea.setText(s);
        lyricsArea.addKeyListener(
                new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        int missing = table.applyLyrics(lyricsArea.getText());
                        if (missing > 0) {
                            missing = 0;
                        }

                        //int n = table.getNoteOrPageBreakCount();
                        //n = n + missing;
                        //table.note(n + 1);

                        int pos = lyricsArea.getCaretPosition();
                        String txt = lyricsArea.getText().substring(0, pos);
                        Vector<?> syl = table.getSyllables(txt);
                        int n = syl.size();
                        table.note(n);
                    }


                    public void keyReleased(KeyEvent e) {
                        keyPressed(e);
                    }


                    public void keyTyped(KeyEvent e) {
                        keyPressed(e);
                    }
                });
    }

    /**
     * Description of the Method
     */
    public void requestFocus() {
        lyricsArea.requestFocus();
    }

    /**
     * Gets the table attribute of the Lyrics object
     *
     * @return The table value
     */
    public String getTable() {
        return table.getPlainText();
    }

    /**
     * Sets the table attribute of the LyricsForMIDI object
     *
     * @param txt The new table value
     */
    public void setTable(String txt) {
        table.removeAllRows();
        table.getColumnModel().getColumn(0).setMinWidth(10);
        table.getColumnModel().getColumn(0).setMaxWidth(10);
        table.getColumnModel().getColumn(1).setMinWidth(30);
        table.getColumnModel().getColumn(1).setMaxWidth(30);
        table.getColumnModel().getColumn(2).setMinWidth(20);
        table.getColumnModel().getColumn(2).setMaxWidth(20);
        table.getColumnModel().getColumn(3).setMinWidth(20);
        table.getColumnModel().getColumn(3).setMaxWidth(20);
        table.getColumnModel().getColumn(4).setMinWidth(70);
        table.getColumnModel().getColumn(4).setMaxWidth(70);
        table.setText(txt);
        table.home();
    }
}

