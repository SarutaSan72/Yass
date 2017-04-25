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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.StringTokenizer;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class Lyrics extends JPanel {
    /**
     * Description of the Field
     */
    public final static String ID = "lyrics";
    private static final long serialVersionUID = 2887575440389998645L;
    private Wizard wizard;
    private JTextArea lyricsArea = null;
    private JCheckBox utf8 = null;


    /**
     * Constructor for the Lyrics object
     *
     * @param w Description of the Parameter
     */
    public Lyrics(Wizard w) {
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
        URL url = I18.getResource("create_lyrics.html");
        try {
            txt.setPage(url);
        } catch (Exception ignored) {
        }
        txt.setEditable(false);
        content.add("Center", new JScrollPane(txt));

        JPanel buttons = new JPanel(new GridLayout(1, 3));
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
        buttons.add(new JLabel(""));

        lyricsArea = new JTextArea(10, 20);
        JPanel pan = new JPanel(new BorderLayout());
        pan.add("Center", new JScrollPane(lyricsArea));
        pan.add("South", buttons);
        content.add("South", pan);
        return content;
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
    }


    /**
     * Gets the table attribute of the Lyrics object
     *
     * @return The table value
     */
    public String getTable() {
        StringWriter buffer = new StringWriter();
        PrintWriter outputStream = new PrintWriter(buffer);

        outputStream.println("#TITLE:Unknown");
        outputStream.println("#ARTIST:Unknown");
        outputStream.println("#LANGUAGE:Unknown");
        outputStream.println("#EDITION:Unknown");
        outputStream.println("#GENRE:Unknown");
        outputStream.println("#CREATOR:Unknown");
        outputStream.println("#MP3:Unknown");
        outputStream.println("#BPM:300");
        outputStream.println("#GAP:0");
        StringTokenizer st = new StringTokenizer(getText(), "\n");
        int b = 0;
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            StringTokenizer st2 = new StringTokenizer(line, " ");
            boolean isFirst = true;
            while (st2.hasMoreTokens()) {
                outputStream.println(": " + b + " 2 6 " + (isFirst ? "" : " ") + st2.nextToken());
                isFirst = false;
                b += 4;
            }
            if (st.hasMoreTokens()) {
                outputStream.println("- " + b);
            }
        }
        outputStream.println("E");
        return buffer.toString();
    }
}

