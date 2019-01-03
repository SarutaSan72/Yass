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

package yass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.StringTokenizer;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassLibOptions extends JDialog {
    private static final long serialVersionUID = -7342966934700458895L;
    private JTextField songdirInput, playlistdirInput, coverdirInput;
    private JComboBox<String> defaults = null;
    private YassProperties prop = null;
    private YassSongList songList = null;
    private YassPlayer mp3 = null;
    private String songdir = null;
    private String playlistdir = null;
    private String coverdir = null;
    private JLabel songdirlabel;
    private JLabel playlistdirlabel;
    private JLabel coverdirlabel;
    private Color fgColor = new JLabel().getForeground();

    private YassActions actions = null;


    /**
     * Constructor for the YassOptions object
     *
     * @param p        Description of the Parameter
     * @param songList Description of the Parameter
     * @param mp3      Description of the Parameter
     * @param a        Description of the Parameter
     */
    public YassLibOptions(YassProperties p, YassActions a, YassSongList songList, YassPlayer mp3) {
        super(a.createOwnerFrame());
        this.actions = a;
        this.prop = p;
        this.songList = songList;
        this.mp3 = mp3;

        songdir = prop.getProperty("song-directory");

        JPanel panel = new JPanel(new BorderLayout());
        JPanel left = new JPanel(new GridLayout(0, 1));
        JPanel right = new JPanel(new GridLayout(0, 1));
        panel.add("North", new JLabel(I18.get("tool_prefs_where")));
        panel.add("West", left);
        panel.add("Center", right);

        left.add(new JLabel(""));
        right.add(new JLabel(""));
        left.add(new JLabel(I18.get("tool_prefs_programs") + " "));
        defaults = new JComboBox<>();
        defaults.setEditable(false);
        defaults.addItem("");
        String def = p.getProperty("default-programs");
        def = def.replaceAll("[/\\\\]+", "\\" + File.separator);
        //System.out.println(def);
        int selIndex = -1;
        StringTokenizer st = new StringTokenizer(def, "|");
        int i = 0;
        while (st.hasMoreTokens()) {
            def = st.nextToken();
            if (new File(def).exists()) {
                defaults.addItem(def);
                def = def + File.separator + "Songs";
                if (def.equals(songdir)) {
                    selIndex = i;
                }
                i++;
            }
        }
        if (selIndex >= 0) {
            defaults.setSelectedIndex(selIndex);
        }

        defaults.getEditor().getEditorComponent().addFocusListener(
                new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        defaults.setEditable(false);
                    }
                });
        defaults.addKeyListener(
                new KeyAdapter() {
                    public void keyReleased(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                            if (defaults.getItemCount() > 1) {
                                String item = (String) defaults.getSelectedItem();
                                defaults.removeItemAt(defaults.getSelectedIndex());
                                removeDefaults(item);
                            }
                        }
                    }
                });

        ActionListener[] listeners = defaults.getActionListeners();
        for (ActionListener listener : listeners) {
            defaults.removeActionListener(listener);
        }
        defaults.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox<String> cb = (JComboBox<String>) e.getSource();
                        String s = (String) cb.getSelectedItem();

                        if (cb.isEditable()) {
                            cb.setEditable(false);
                        }

                        s = s.replaceAll("[/\\\\]+", "\\" + File.separator);
                        if (!new File(s).exists()) {
                            cb.setSelectedIndex(0);
                            return;
                        }

                        boolean exists = false;
                        int ic = cb.getItemCount();
                        for (int i = 0; i < ic; i++) {
                            String def = cb.getItemAt(i);
                            if (def.equals(s)) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            cb.addItem(s);
                            addDefaults(s);
                        }

                        String d = s + File.separator + "Songs";
                        String d2 = s + File.separator + "songs";
                        if (new File(d).exists()) {
                            songdirInput.setText(d);
                        } else if (new File(d2).exists()) {
                            songdirInput.setText(d2);
                        } else {
                            songdirInput.setText(s);
                        }
                        d = s + File.separator + "Playlists";
                        d2 = s + File.separator + "playlists";
                        if (new File(d).exists()) {
                            playlistdirInput.setText(d);
                        } else if (new File(d2).exists()) {
                            playlistdirInput.setText(d2);
                        } else {
                            playlistdirInput.setText(s);
                        }
                        d = s + File.separator + "Covers";
                        d2 = s + File.separator + "covers";
                        if (new File(d).exists()) {
                            coverdirInput.setText(d);
                        } else if (new File(d2).exists()) {
                            coverdirInput.setText(d2);
                        } else {
                            coverdirInput.setText(s);
                        }

                    }
                });

        JButton add = new JButton(I18.get("tool_prefs_programs_add"));
        add.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser();
                        File d = null;
                        if (songdir != null) {
                            d = new File(songdir);
                        }
                        if (d == null || !d.exists()) {
                            d = new java.io.File(".");
                        }

                        chooser.setCurrentDirectory(d);
                        chooser.setDialogTitle(I18.get("tool_prefs_songs_spec"));
                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                        if (chooser.showOpenDialog(YassLibOptions.this) != JFileChooser.APPROVE_OPTION) {
                            return;
                        }
                        String s = chooser.getSelectedFile().getAbsolutePath();
                        File f = new File(s);

                        // user has chosen songs folder instead of parent
                        if (s.toLowerCase().endsWith("songs")) {
                            String par = f.getParent();
                            String d2 = par + File.separator + "Songs";
                            String d3 = par + File.separator + "songs";
                            if (new File(d2).exists() || new File(d3).exists()) {
                                f = new File(par);
                            }
                        }
                        s = f.getAbsolutePath();
                        s = s.replaceAll("[/\\\\]+", "\\" + File.separator);
                        defaults.addItem(s);
                        defaults.setSelectedItem(s);

                        String def = p.getProperty("default-programs");
                        if (! def.endsWith("|")) def += "|";
                        def += s;
                        p.setProperty("default-programs", def);
                        p.store();
                    }
                });
        JPanel defaultsBox = new JPanel(new BorderLayout());
        defaultsBox.add("Center", defaults);
        defaultsBox.add("East", add);

        JPanel help = new JPanel(new GridLayout(1, 3));
        help.add(new JLabel(""));
        help.add(new JLabel(""));
        JLabel helpLabel;
        help.add(helpLabel = new JLabel(I18.get("tool_prefs_need_help"), JLabel.RIGHT));
        helpLabel.setForeground(Color.BLUE);
        helpLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        helpLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try {
                    YassActions.openURL("http://www.yass-along.com/installation");
                } catch (Exception e) {
                    // ok
                }
            }
        });

        right.add(defaultsBox);
        left.add(new JLabel(""));
        right.add(help);

        left.add(songdirlabel = new JLabel(I18.get("tool_prefs_songs")));

        songdirInput = new JTextField(songdir != null ? songdir : "");
        JPanel sdbPanel = new JPanel(new GridLayout(1, 0));
        JButton songdirBrowse;
        sdbPanel.add(songdirBrowse = new JButton(I18.get("tool_prefs_browse")));

        JPanel sdPanel = new JPanel(new BorderLayout());
        sdPanel.add("Center", songdirInput);
        sdPanel.add("East", sdbPanel);
        right.add(sdPanel);
        songdirBrowse.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser();
                        File d = null;
                        if (songdir != null) {
                            d = new File(songdir);
                        }
                        if (d == null || !d.exists()) {
                            d = new java.io.File(".");
                        }

                        chooser.setCurrentDirectory(d);
                        chooser.setDialogTitle(I18.get("tool_prefs_songs_spec"));
                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                        if (chooser.showOpenDialog(YassLibOptions.this) != JFileChooser.APPROVE_OPTION) {
                            return;
                        }
                        String s = chooser.getSelectedFile().getAbsolutePath();
                        File f = new File(s);

                        // saruta, Jan 2019: user has chosen parent instead of song folder
                        if (! s.toLowerCase().endsWith("songs"))
                        {
                            String d2 = s + File.separator + "Songs";
                            String d3 = s + File.separator + "songs";
                            if (new File(d2).exists()) {
                                f = new File(d2);
                            } else if (new File(d3).exists()) {
                                f = new File(d3);
                            }
                        }

                        File p = f;
                        File c = f;
                        File par = f.getParentFile();
                        if (par != null) {
                            File pf = new File(par, "Playlists");
                            if (pf.exists()) {
                                p = pf;
                            }
                            File cf = new File(par, "Covers");
                            if (cf.exists()) {
                                c = cf;
                            }
                        }
                        songdirInput.setText(f.getAbsolutePath());
                        playlistdirInput.setText(p.getAbsolutePath());
                        coverdirInput.setText(c.getAbsolutePath());
                    }
                });

        playlistdir = prop.getProperty("playlist-directory");
        left.add(playlistdirlabel = new JLabel(I18.get("tool_prefs_playlists")));

        playlistdirInput = new JTextField(playlistdir != null ? playlistdir : "");
        JButton playlistdirBrowse = new JButton(I18.get("tool_prefs_browse"));
        JPanel pldPanel = new JPanel(new BorderLayout());
        pldPanel.add("Center", playlistdirInput);
        pldPanel.add("East", playlistdirBrowse);
        right.add(pldPanel);
        playlistdirBrowse.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser();
                        File d = null;
                        if (playlistdir != null) {
                            d = new File(playlistdir);
                        }
                        if (d == null || !d.exists()) {
                            d = new java.io.File(".");
                        }

                        chooser.setCurrentDirectory(d);
                        chooser.setDialogTitle(I18.get("tool_prefs_playlists_spec"));
                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                        if (chooser.showOpenDialog(YassLibOptions.this) != JFileChooser.APPROVE_OPTION) {
                            return;
                        }
                        String s = chooser.getSelectedFile().getAbsolutePath();
                        playlistdirInput.setText(s);
                    }
                });

        coverdir = prop.getProperty("cover-directory");
        left.add(coverdirlabel = new JLabel(I18.get("tool_prefs_covers")));

        coverdirInput = new JTextField(coverdir != null ? coverdir : "");
        JButton coverdirBrowse = new JButton(I18.get("tool_prefs_browse"));
        JPanel cdPanel = new JPanel(new BorderLayout());
        cdPanel.add("Center", coverdirInput);
        cdPanel.add("East", coverdirBrowse);
        right.add(cdPanel);
        coverdirBrowse.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser();
                        File d = null;
                        if (coverdir != null) {
                            d = new File(coverdir);
                        }
                        if (d == null || !d.exists()) {
                            d = new java.io.File(".");
                        }

                        chooser.setCurrentDirectory(d);
                        chooser.setDialogTitle(I18.get("tool_prefs_covers_spec"));
                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                        if (chooser.showOpenDialog(YassLibOptions.this) != JFileChooser.APPROVE_OPTION) {
                            return;
                        }
                        String s = chooser.getSelectedFile().getAbsolutePath();
                        coverdirInput.setText(s);
                    }
                });

        playlistdirBrowse.setForeground(Color.GRAY);
        coverdirBrowse.setForeground(Color.GRAY);
        playlistdirInput.setBackground(helpLabel.getBackground());
        coverdirInput.setBackground(helpLabel.getBackground());

        JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        setContentPane(optionPane);
        optionPane.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        if (!e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
                            return;
                        }
                        if (defaults.isEditable()) {
                            return;
                        }

                        JOptionPane optionPane = (JOptionPane) e.getSource();
                        Object val = optionPane.getValue();
                        if (val == null || val == JOptionPane.UNINITIALIZED_VALUE) {
                            return;
                        }

                        int value = ((Integer) val).intValue();
                        if (value == JOptionPane.OK_OPTION) {
                            songdir = songdirInput.getText();
                            File f = new File(songdir);
                            songdir = f.getAbsolutePath();
                            songdirlabel.setForeground(f.exists() ? fgColor : Color.red);

                            playlistdir = playlistdirInput.getText();
                            f = new File(playlistdir);
                            playlistdir = f.getAbsolutePath();
                            playlistdirlabel.setForeground(f.exists() ? fgColor : Color.red);

                            coverdir = coverdirInput.getText();
                            f = new File(coverdir);
                            coverdir = f.getAbsolutePath();
                            coverdirlabel.setForeground(f.exists() ? fgColor : Color.red);

                            String oldDir = prop.getProperty("song-directory");
                            String oldpDir = prop.getProperty("playlist-directory");
                            String oldcDir = prop.getProperty("cover-directory");
                            if (oldDir == null) {
                                oldDir = "";
                            }
                            if (oldpDir == null) {
                                oldpDir = "";
                            }
                            if (oldcDir == null) {
                                oldcDir = "";
                            }
                            if (songdir != null && !songdir.equals(oldDir)) {
                                prop.setProperty("song-directory", songdir);
                            }
                            if (playlistdir != null && !playlistdir.equals(oldpDir)) {
                                prop.setProperty("playlist-directory", playlistdir);
                            }
                            if (coverdir != null && !coverdir.equals(oldcDir)) {
                                prop.setProperty("cover-directory", coverdir);
                            }

                            prop.setProperty("import-directory", songdir);

                            prop.store();
                            dispose();

                            actions.refreshLibrary();
                            //System.out.println("load lib");
                        } else if (value == JOptionPane.CANCEL_OPTION) {
                            dispose();
                        }
                    }
                });

        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        Dimension dim = this.getToolkit().getScreenSize();
        setSize(500, 300);
        setLocation(dim.width / 2 - 250, dim.height / 2 - 150);
        //setIconImage(new ImageIcon(YassLibOptions.this.getClass().getResource("/yass/yass-icon-16.png")).getImage());
        setTitle(I18.get("tool_prefs_title"));
        setVisible(true);
    }

    /**
     * Gets the songList attribute of the YassOptions object
     *
     * @return The songList value
     */
    public YassSongList getSongList() {
        return songList;
    }

    /**
     * Gets the mP3 attribute of the YassOptions object
     *
     * @return The mP3 value
     */
    public YassPlayer getMP3() {
        return mp3;
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     */
    public void removeDefaults(String s) {
        String def = prop.getProperty("default-programs");
        def = def.replaceAll("[/\\\\]+", "\\" + File.separator);

        String newString = "";
        StringTokenizer st = new StringTokenizer(def, "|");
        boolean changed = false;
        while (st.hasMoreTokens()) {
            def = st.nextToken();
            if (!def.equals(s)) {
                newString = newString + "|" + def;
                changed = true;
            }
        }

        if (changed) {
            prop.setProperty("default-programs", newString);
            prop.store();
        }
    }


    /**
     * Adds a feature to the Defaults attribute of the YassLibOptions object
     *
     * @param s The feature to be added to the Defaults attribute
     */
    public void addDefaults(String s) {
        String def = prop.getProperty("default-programs");
        def = def + "|" + s;
        prop.setProperty("default-programs", def);
        prop.store();

    }
}

