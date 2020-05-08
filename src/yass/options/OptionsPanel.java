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
import yass.YassProperties;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class OptionsPanel extends JPanel {
    private static final long serialVersionUID = -5593710558109233649L;
    private static Hashtable<String, String> myprop = null;
    private static YassProperties prop = null;
    private JLabel title = null;
    private JPanel left = null, right = null;
    private JButton resetButton = null;
    private int labelWidth = 80;
    private Vector<String> panelProps = new Vector<>();
    private JPanel contentPanel = null;


    /**
     * Constructor for the OptionsPanel object
     */
    public OptionsPanel() {
    }

    /**
     * Sets the properties attribute of the OptionsPanel object
     *
     * @param p The new properties value
     */
    public static void loadProperties(YassProperties p) {
        prop = p;
        myprop = new Hashtable<>(p.size());
        for (Enumeration<Object> en = prop.keys(); en.hasMoreElements(); ) {
            String key = (String) en.nextElement();
            String val = prop.getProperty(key);
            myprop.put(key, val);
        }
    }

    public void resetPanelProperties() {
        for (Enumeration<String> en = panelProps.elements(); en.hasMoreElements(); ) {
            String key = en.nextElement();
            String val = prop.getDefaultProperty(key);
            if (myprop.containsKey(key)) myprop.replace(key, val);
        }
        BorderLayout layout = (BorderLayout) contentPanel.getLayout();
        contentPanel.remove(layout.getLayoutComponent(BorderLayout.CENTER));
        contentPanel.add("Center", getBody());
        contentPanel.revalidate();
    }


    /**
     * Description of the Method
     */
    public static void storeProperties() {
        for (Enumeration<String> en = myprop.keys(); en.hasMoreElements(); ) {
            String key = en.nextElement();
            String val = myprop.get(key);
            prop.put(key, val);
        }
        prop.store();
    }

    /**
     * Description of the Method
     *
     * @param title Description of the Parameter
     */
    public void init(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setLayout(new BorderLayout());
        panel.add("North", getHeader());
        panel.add("Center", getBody());
        panel.add("South", getFooter());
        contentPanel = panel;

        setLayout(new BorderLayout());
        add("North", panel);

        add("Center", new JLabel());
        setHeader(title);
    }

    /**
     * Gets the property attribute of the OptionsPanel object
     *
     * @param key Description of the Parameter
     * @return The property value
     */
    public String getProperty(String key) {
        if (! panelProps.contains(key)) panelProps.add(key);
        return myprop.get(key);
    }

    /**
     * Gets the properties attribute of the OptionsPanel object
     *
     * @return The properties value
     */
    public YassProperties getProperties() {
        return prop;
    }

    /**
     * Sets the property attribute of the OptionsPanel object
     *
     * @param key The new property value
     * @param val The new property value
     */
    public void setProperty(String key, String val) {
        myprop.put(key, val);
    }

    /**
     * Description of the Method
     *
     * @param key Description of the Parameter
     * @return Description of the Return Value
     */
    public String resetProperty(String key) {
        String val = prop.getProperty(key);
        myprop.put(key, val);
        return val;
    }

    /**
     * Gets the header attribute of the OptionsPanel object
     *
     * @return The header value
     */
    public JComponent getHeader() {
        title = new JLabel();
        Font f = title.getFont();
        float size = f.getSize();
        f = f.deriveFont(size + 2);
        title.setFont(f);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        return title;
    }

    /**
     * Gets the footer attribute of the OptionsPanel object
     *
     * @return The footer value
     */
    public JComponent getFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        p.add("Center", new JPanel());
        p.add("East", resetButton = new JButton(I18.get("options_reset")));
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int res = JOptionPane.showConfirmDialog(OptionsPanel.this, I18.get("options_reset_msg"), resetButton.getText(), JOptionPane.OK_CANCEL_OPTION);
                if (res == JOptionPane.OK_OPTION)
                    resetPanelProperties();
            }
        });
        return p;
    }

    /**
     * Sets the header attribute of the OptionsPanel object
     *
     * @param s The new header value
     */
    public void setHeader(String s) {
        title.setText(s);
    }

    /**
     * Gets the body attribute of the OptionsPanel object
     *
     * @return The body value
     */
    public JComponent getBody() {
        //right.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        left = null;
        right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        addRows();

        JPanel p = new JPanel(new BorderLayout());
        p.add("Center", right);
        return p;
    }

    /**
     * Adds a feature to the Rows attribute of the OptionsPanel object
     */
    public void addRows() {
    }

    /**
     * Sets the labelWidth attribute of the OptionsPanel object
     *
     * @param w The new labelWidth value
     */
    public void setLabelWidth(int w) {
        labelWidth = w;
    }


    /**
     * Adds a feature to the Comment attribute of the OptionsPanel object
     *
     * @param s The feature to be added to the Comment attribute
     */
    public void addComment(String s) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        JLabel la = new JLabel("<html><font color=gray>" + s);
        la.setVerticalAlignment(JLabel.TOP);
        la.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel spacer = new JLabel("");
        //spacer.setSize(new Dimension(120, 10));
        spacer.setPreferredSize(new Dimension(labelWidth, 20));
        //spacer.setMaximumSize(new Dimension(200, 20));
        row.add(spacer);
        row.add(la);
        right.add(row);
    }


    /**
     * Adds a feature to the Separator attribute of the OptionsPanel object
     */
    public void addSeparator() {
        addComment("");
    }


    /**
     * Adds a feature to the Separator attribute of the OptionsPanel object
     *
     * @param s The feature to be added to the Separator attribute
     */
    public void addSeparator(String s) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.add(new JLabel("<html><u>" + s));
        row.add(Box.createHorizontalGlue());
        right.add(row);
    }


    /**
     * Adds a feature to the Color attribute of the OptionsPanel object
     *
     * @param label The feature to be added to the Color attribute
     * @param key   The feature to be added to the Color attribute
     */
    public void addColor(String label, String key) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        JLabel lab = new JLabel(label);
        lab.setVerticalAlignment(JLabel.CENTER);
        lab.setHorizontalAlignment(JLabel.LEFT);
        //lab.setSize(new Dimension(120, 10));
        lab.setPreferredSize(new Dimension(labelWidth, 20));
        //lab.setMaximumSize(new Dimension(200, 20));

        String s = getProperty(key);
        Color col = Color.decode(s);

        JLabel la = new JLabel(s.toUpperCase(), JLabel.CENTER);
        la.setOpaque(true);
        la.setBackground(col);
        la.addMouseListener(new EditColorListener(la, col, key));

        JPanel colPanel = new JPanel(new BorderLayout());
        colPanel.add("Center", la);

        row.add(lab);
        row.add(colPanel);
        right.add(row);
        lab.setAlignmentY(Component.TOP_ALIGNMENT);
        colPanel.setAlignmentY(Component.TOP_ALIGNMENT);
    }


    /**
     * Adds a feature to the ColorSet attribute of the OptionsPanel object
     *
     * @param label The feature to be added to the ColorSet attribute
     * @param n     The feature to be added to the ColorSet attribute
     * @param okey  The feature to be added to the ColorSet attribute
     */
    public void addColorSet(String label, String okey, int n, String[] names) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        JLabel lab = new JLabel(label);
        lab.setVerticalAlignment(JLabel.CENTER);
        lab.setHorizontalAlignment(JLabel.LEFT);
        //lab.setSize(new Dimension(120, 10));
        lab.setPreferredSize(new Dimension(labelWidth, 20));
        //lab.setMaximumSize(new Dimension(200, 20));

        JPanel grid = new JPanel(new GridLayout(1, 0));
        for (int i = 0; i < n; i++) {
            String key = okey + "-" + i;
            String s = getProperty(key);
            Color col = Color.decode(s);

            String lbl = i+"";
            if (names != null && i < names.length) lbl = names[i];
            JLabel la = new JLabel(lbl, JLabel.CENTER);
            la.addMouseListener(new EditColorListener(la, col, key));
            la.setToolTipText("#" + Integer.toHexString(col.getRGB() & 0x00ffffff));
            la.setOpaque(true);
            la.setBackground(col);
            la.setFont(la.getFont().deriveFont(((float)(la.getFont().getSize() - 3))));
            grid.add(la);
        }
        JPanel colPanel = new JPanel(new BorderLayout());
        colPanel.add("Center", grid);

        row.add(lab);
        row.add(colPanel);
        right.add(row);
        lab.setAlignmentY(Component.TOP_ALIGNMENT);
        colPanel.setAlignmentY(Component.TOP_ALIGNMENT);
    }


    /**
     * Adds a feature to the Text attribute of the OptionsPanel object
     *
     * @param label The feature to be added to the Text attribute
     * @param key   The feature to be added to the Text attribute
     */
    public void addText(String label, String key) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        JLabel lab = new JLabel(label);
        lab.setVerticalAlignment(JLabel.CENTER);
        lab.setHorizontalAlignment(JLabel.LEFT);
        //lab.setSize(new Dimension(120, 10));
        lab.setPreferredSize(new Dimension(labelWidth, 20));
        //lab.setMaximumSize(new Dimension(200, 20));

        JTextField txtField = new JTextField(getProperty(key));
        txtField.getDocument().addDocumentListener(new MyDocumentListener(key));

        row.add(lab);
        row.add(txtField);
        right.add(row);
    }


    /**
     * Adds a feature to the Button attribute of the OptionsPanel object
     *
     * @param label The feature to be added to the Button attribute
     * @param s     The feature to be added to the Button attribute
     * @param txt   The feature to be added to the Button attribute
     * @return Description of the Return Value
     */
    public JButton addButton(String label, String txt, String s) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        JLabel lab = new JLabel(label);
        lab.setVerticalAlignment(JLabel.CENTER);
        lab.setHorizontalAlignment(JLabel.LEFT);
        //lab.setSize(new Dimension(120, 10));
        lab.setPreferredSize(new Dimension(labelWidth, 20));
        //lab.setMaximumSize(new Dimension(200, 20));

        JPanel p = new JPanel(new BorderLayout());
        JButton b = new JButton(s);
        if (txt != null) p.add("Center", new JLabel(txt, JLabel.RIGHT));
        p.add("East", b);

        row.add(lab);
        row.add(b);
        right.add(row);
        lab.setAlignmentY(Component.TOP_ALIGNMENT);
        b.setAlignmentY(Component.TOP_ALIGNMENT);

        return b;
    }


    /**
     * Adds a feature to the Boolean attribute of the OptionsPanel object
     *
     * @param label The feature to be added to the Boolean attribute
     * @param key   The feature to be added to the Boolean attribute
     * @param val   The feature to be added to the Boolean attribute
     */
    public void addBoolean(String label, String key, String val) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        JLabel lab = new JLabel(label);
        lab.setVerticalAlignment(JLabel.CENTER);
        lab.setHorizontalAlignment(JLabel.LEFT);
        //lab.setSize(new Dimension(120, 10));
        lab.setPreferredSize(new Dimension(labelWidth, 20));
        //lab.setMaximumSize(new Dimension(200, 20));

        JCheckBox box = new JCheckBox(val);

        String p = getProperty(key);
        boolean checked = p != null && p.equals("true");
        box.setSelected(checked);
        box.addItemListener(new MyItemListener(key));
        box.setVerticalAlignment(SwingConstants.TOP);

        row.add(lab);
        row.add(box);
        row.add(Box.createHorizontalGlue());
        right.add(row);
        lab.setAlignmentY(Component.TOP_ALIGNMENT);
        box.setAlignmentY(Component.TOP_ALIGNMENT);
    }


    /**
     * Adds a feature to the Radio attribute of the OptionsPanel object
     *
     * @param label The feature to be added to the Radio attribute
     * @param key   The feature to be added to the Radio attribute
     * @param val   The feature to be added to the Radio attribute
     * @param txt   The feature to be added to the Radio attribute
     */
    public void addRadio(String label, String key, String val, String txt) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        JLabel lab = new JLabel(label);
        lab.setVerticalAlignment(JLabel.TOP);
        lab.setHorizontalAlignment(JLabel.LEFT);
        //lab.setSize(new Dimension(120, 10));
        lab.setPreferredSize(new Dimension(labelWidth, 20));
        //lab.setMaximumSize(new Dimension(200, 20));

        StringTokenizer st1 = new StringTokenizer(val, "|");
        StringTokenizer st2 = new StringTokenizer(txt, "|");
        int n = st1.countTokens();

        String keyval = getProperty(key);
        if (keyval == null) {
            keyval = "unknown";
        }

        ButtonGroup g = new ButtonGroup();

        JPanel bp = new JPanel(new GridLayout(0, 1));
        for (int i = 0; i < n; i++) {
            val = st1.nextToken();
            txt = st2.nextToken();
            JRadioButton b = new JRadioButton(txt);
            if (val.equals(keyval)) {
                b.setSelected(true);
            }
            g.add(b);

            b.addActionListener(new MyActionListener(key, val));
            bp.add(b);
        }

        row.add(lab);
        row.add(bp);
        lab.setAlignmentY(Component.TOP_ALIGNMENT);
        bp.setAlignmentY(Component.TOP_ALIGNMENT);
        right.add(row);
    }


    /**
     * Adds a feature to the Choice attribute of the OptionsPanel object
     *
     * @param label          The feature to be added to the Choice attribute
     * @param choices_key    The feature to be added to the Choice attribute
     * @param select_key     The feature to be added to the Choice attribute
     * @param choices_labels The feature to be added to the Choice attribute
     */
    public void addChoice(String label, String choices_labels, String choices_key, String select_key) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        JLabel lab = new JLabel(label);
        lab.setVerticalAlignment(JLabel.CENTER);
        lab.setHorizontalAlignment(JLabel.LEFT);
        //lab.setSize(new Dimension(120, 10));
        lab.setPreferredSize(new Dimension(labelWidth, 20));
        //lab.setMaximumSize(new Dimension(200, 20));

        String ch = getProperty(choices_key);
        StringTokenizer st = new StringTokenizer(ch, "|");
        StringTokenizer st2 = new StringTokenizer(choices_labels, "|");
        Vector<String> labels = new Vector<>();
        Vector<String> keys = new Vector<>();
        while (st.hasMoreTokens()) {
            keys.addElement(st.nextToken().trim());
            labels.addElement(st2.nextToken().trim());
        }
        JComboBox<String> choiceBox = new JComboBox<>(labels);
        String key = getProperty(select_key);
        int i = keys.indexOf(key);
        choiceBox.setSelectedIndex(i);
        choiceBox.addActionListener(new ChoiceListener(keys, select_key));

        row.add(lab);
        row.add(choiceBox);
        lab.setAlignmentY(Component.TOP_ALIGNMENT);
        choiceBox.setAlignmentY(Component.TOP_ALIGNMENT);
        right.add(row);
    }

    /**
     * Adds a feature to the TextArea attribute of the OptionsPanel object
     *
     * @param label The feature to be added to the TextArea attribute
     * @param col   The feature to be added to the TextArea attribute
     * @param key   The feature to be added to the TextArea attribute
     */
    public void addTextArea(String label, String key, int col) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        JLabel lab = new JLabel(label);
        lab.setVerticalAlignment(JLabel.TOP);
        lab.setHorizontalAlignment(JLabel.LEFT);
        //lab.setSize(new Dimension(120, 10));
        lab.setPreferredSize(new Dimension(labelWidth, 20));
        //lab.setMaximumSize(new Dimension(200, 20));

        JTextArea txtArea = new JTextArea(getProperty(key), col, 10);
        txtArea.getDocument().addDocumentListener(new MyDocumentListener(key));
        JScrollPane scroll = new JScrollPane(txtArea);
        txtArea.setLineWrap(true);

        row.add(lab);
        row.add(scroll);
        lab.setAlignmentY(Component.TOP_ALIGNMENT);
        scroll.setAlignmentY(Component.TOP_ALIGNMENT);
        right.add(row);
    }

    /**
     * Adds a feature to the Directory attribute of the OptionsPanel object
     *
     * @param label The feature to be added to the Directory attribute
     * @param key   The feature to be added to the Directory attribute
     */
    public void addDirectory(String label, String key) {
        addFileOrDirectory(label, key, true);
    }

    /**
     * Adds a feature to the File attribute of the OptionsPanel object
     *
     * @param label The feature to be added to the File attribute
     * @param key   The feature to be added to the File attribute
     */
    public void addFile(String label, String key) {
        addFileOrDirectory(label, key, false);
    }

    /**
     * Adds a feature to the File attribute of the OptionsPanel object
     *
     * @param label The feature to be added to the File attribute
     * @param dir   The feature to be added to the FileOrDirectory attribute
     * @param key   The feature to be added to the FileOrDirectory attribute
     */
    public void addFileOrDirectory(String label, String key, boolean dir) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        JLabel lab = new JLabel(label);
        lab.setVerticalAlignment(JLabel.CENTER);
        lab.setHorizontalAlignment(JLabel.LEFT);
        //lab.setSize(new Dimension(120, 10));
        lab.setPreferredSize(new Dimension(labelWidth, 20));
        //lab.setMaximumSize(new Dimension(200, 20));

        String s = getProperty(key);
        if (s == null) {
            s = "";
        } else {
            File fs = new File(s);
            if (s == null || (dir && !fs.isDirectory()) || (!dir && fs.isDirectory())) {
                s = "";
            }
        }
        JTextField textField = new JTextField(s);
        textField.setCaretPosition(s.length());
        textField.getDocument().addDocumentListener(new MyDocumentListener(key));
        textField.setColumns(20);
        JButton browseButton = new JButton(I18.get("options_browse"));
        browseButton.addActionListener(new BrowseListener(textField, dir, key));

        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.add("Center", textField);
        filePanel.add("East", browseButton);

        row.add(lab);
        row.add(filePanel);
        lab.setAlignmentY(Component.TOP_ALIGNMENT);
        filePanel.setAlignmentY(Component.TOP_ALIGNMENT);
        right.add(row);
    }

    class ChoiceListener implements ActionListener {
        private final Vector<String> keys;
        private final String select_key;


        /**
         * Constructor for the ChoiceListener object
         *
         * @param keys       Description of the Parameter
         * @param select_key Description of the Parameter
         */
        public ChoiceListener(Vector<String> keys, String select_key) {
            this.keys = keys;
            this.select_key = select_key;
        }


        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            JComboBox<?> c = (JComboBox<?>) e.getSource();
            int i = c.getSelectedIndex();
            String key = keys.elementAt(i);
            setProperty(select_key, key);
            // System.out.println("Setting " + select_key + " to " + key);
        }
    }

    /**
     * Description of the Class
     *
     * @author Saruta
     */
    class BrowseListener implements ActionListener {
        JTextField textField = null;
        boolean mode;
        String key = null;


        /**
         * Constructor for the BrowseListener object
         *
         * @param t   Description of the Parameter
         * @param dir Description of the Parameter
         * @param s   Description of the Parameter
         */
        public BrowseListener(JTextField t, boolean dir, String s) {
            textField = t;
            mode = dir;
            key = s;
        }


        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            File d = null;
            String s = textField.getText();
            if (s != null) {
                d = new File(s);
            }
            if (!d.exists()) {
                d = new java.io.File(".");
            }

            chooser.setCurrentDirectory(d);
            chooser.setDialogTitle(mode ? I18.get("options_dir") : I18.get("options_file"));
            chooser.setFileSelectionMode(mode ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);

            if (chooser.showOpenDialog(OptionsPanel.this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            s = chooser.getSelectedFile().getAbsolutePath();
            textField.setText(s);
            textField.setCaretPosition(s.length());
            setProperty(key, s);
        }
    }


    /**
     * Description of the Class
     *
     * @author Saruta
     */
    class EditColorListener extends MouseAdapter {
        JLabel label = null;
        Color color = null;
        String key = null;


        /**
         * Constructor for the BrowseListener object
         *
         * @param l   Description of the Parameter
         * @param col Description of the Parameter
         * @param s   Description of the Parameter
         */
        public EditColorListener(JLabel l, Color col, String s) {
            label = l;
            color = col;
            key = s;
        }


        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        public void mouseReleased(MouseEvent e) {

            Color newColor = JColorChooser.showDialog(OptionsPanel.this,
                    I18.get("options_color"),
                    color);

            if (newColor == null) {
                return;
            }

            label.setBackground(newColor);

            String hexred = Integer.toHexString(newColor.getRed());
            if (hexred.length() == 1) {
                hexred = "0" + hexred;
            }
            String hexgreen = Integer.toHexString(newColor.getGreen());
            if (hexgreen.length() == 1) {
                hexgreen = "0" + hexgreen;
            }
            String hexblue = Integer.toHexString(newColor.getBlue());
            if (hexblue.length() == 1) {
                hexblue = "0" + hexblue;
            }
            String hex = "#" + hexred + hexgreen + hexblue;
            setProperty(key, hex);
        }
    }


    /**
     * Description of the Class
     *
     * @author Saruta
     */
    class MyItemListener implements ItemListener {
        String key = null;


        /**
         * Constructor for the MyItemListener object
         *
         * @param s Description of the Parameter
         */
        public MyItemListener(String s) {
            key = s;
        }


        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        public void itemStateChanged(ItemEvent e) {
            JCheckBox src = (JCheckBox) e.getSource();
            boolean checked = src.isSelected();
            setProperty(key, checked ? "true" : "false");
        }
    }


    /**
     * Description of the Class
     *
     * @author Saruta
     */
    class MyActionListener implements ActionListener {
        String key = null, val = null;


        /**
         * Constructor for the MyItemListener object
         *
         * @param s Description of the Parameter
         * @param v Description of the Parameter
         */
        public MyActionListener(String s, String v) {
            key = s;
            val = v;
        }


        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            setProperty(key, val);
        }
    }


    /**
     * Description of the Class
     *
     * @author Saruta
     */
    class MyDocumentListener implements DocumentListener {
        String key = null;


        /**
         * Constructor for the MyDocumentListener object
         *
         * @param s Description of the Parameter
         */
        public MyDocumentListener(String s) {
            key = s;
        }


        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        public void changedUpdate(DocumentEvent e) {
            //style change
        }


        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        public void insertUpdate(DocumentEvent e) {
            removeUpdate(e);
        }


        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        public void removeUpdate(DocumentEvent e) {
            String txt = "";
            try {
                txt = e.getDocument().getText(0, e.getDocument().getLength());
            } catch (Exception ex) {
            }
            setProperty(key, txt);
        }
    }
}

