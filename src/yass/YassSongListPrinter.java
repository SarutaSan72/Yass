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

import yass.print.PrintPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassSongListPrinter {
    YassProperties prop = null;
    PrintDialog pd = null;


    /**
     * Constructor for the YassSongListPrinter object
     *
     * @param prop Description of the Parameter
     */
    public YassSongListPrinter(YassProperties prop) {
        this.prop = prop;
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public Hashtable<String, Object> showDialog() {
        if (pd == null) {
            pd = new PrintDialog();
            //pd.setPluginDir(prop.getProperty("yass-plugins"));
            StringTokenizer st = new StringTokenizer(prop.getProperty("print-plugins"), "|");
            while (st.hasMoreTokens()) {
                pd.addPlugin(st.nextToken());
            }
            pd.selectPlugin(0);
        }
        pd.setVisible(true);
        return pd.getValues();
    }

    /**
     * Description of the Method
     *
     * @param songList Description of the Parameter
     * @param filename Description of the Parameter
     * @param hash     Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean print(Vector<YassSong> songList, String filename, Hashtable<String, Object> hash) {
        return pd.getSelectedPlugin().print(songList, filename, hash);
    }

    class StyleRenderer extends JLabel implements ListCellRenderer<Object> {
        private static final long serialVersionUID = -8363240580183018758L;
        Vector<ImageIcon> images = new Vector<>();
        Vector<String> titles = new Vector<>();
        Vector<String> tooltips = new Vector<>();


        /**
         * Constructor for the StyleRenderer object
         */
        public StyleRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
        }


        public void addStyle(ImageIcon icon, String title, String tip) {
            images.addElement(icon);
            titles.addElement(title);
            tooltips.addElement(tip);
        }


        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                return this;
            }

            int selectedIndex = ((Integer) value).intValue();
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            if (selectedIndex >= titles.size()) {
                return this;
            }
            if (titles.size() < 1) {
                return this;
            }

            setIcon(images.elementAt(selectedIndex));
            setToolTipText(titles.elementAt(selectedIndex));
            setText("");
            setFont(list.getFont());
            return this;
        }
    }

    class PrintDialog extends JDialog {
        private static final long serialVersionUID = -2346994183127232532L;
        private String plugindir = null;
        private Hashtable<String, Object> hash = null;
        private JComboBox<Integer> detList;
        private Vector<PrintPlugin> plugins = new Vector<>();
        private JPanel description = null, details = null;
        private JPanel pluginControls = null;
        private StyleRenderer renderer = null;
        private JLabel dLabel = null;


        /**
         * Constructor for the PrintDialog object
         */
        public PrintDialog() {
            JPanel panel = new JPanel(new BorderLayout());

            description = new JPanel(new BorderLayout());
            description.add("Center", dLabel = new JLabel(""));
            panel.add("Center", description);

            details = new JPanel(new BorderLayout());
            details.setBorder(BorderFactory.createTitledBorder(I18.get("print_style")));
            detList = new JComboBox<>();
            renderer = new StyleRenderer();
            renderer.setPreferredSize(new Dimension(100, 100));
            detList.setRenderer(renderer);
            detList.setMaximumRowCount(3);
            details.add("West", detList);

            if (getSelectedPlugin() != null) {
                pluginControls = getSelectedPlugin().getControl();
                details.add("Center", pluginControls);
            } else {
                pluginControls = new JPanel();
                details.add("Center", pluginControls);
            }
            panel.add("North", details);

            detList.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            PrintPlugin p = getSelectedPlugin();
                            if (p != null) {
                                details.remove(pluginControls);
                                pluginControls = p.getControl();
                                details.add("Center", pluginControls);
                                description.setBorder(BorderFactory.createTitledBorder(p.getTitle()));
                                dLabel.setText("<html>" + p.getDescription());
                            }
                            details.validate();
                        }
                    });

            JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            setContentPane(optionPane);
            optionPane.addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent e) {
                            if (!e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
                                return;
                            }

                            JOptionPane optionPane = (JOptionPane) e.getSource();
                            Object val = optionPane.getValue();
                            if (val == null || val == JOptionPane.UNINITIALIZED_VALUE) {
                                return;
                            }
                            int value = ((Integer) val).intValue();
                            if (value == JOptionPane.OK_OPTION) {
                                hash = new Hashtable<>();
                                hash.put("covercache", prop.getProperty("songlist-imagecache"));
                                PrintPlugin p = getSelectedPlugin();
                                if (p != null) {
                                    p.commit(hash);
                                }
                            } else {
                                hash = null;
                            }
                            dispose();
                        }
                    });

            setModal(true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            pack();
            Dimension dim = this.getToolkit().getScreenSize();
            setSize(500, 400);
            setLocation(dim.width / 2 - 200, dim.height / 2 - 150);
            setTitle(I18.get("print_title"));
        }

        public PrintPlugin getSelectedPlugin() {
            if (plugins.size() < 1) {
                return null;
            }
            return plugins.elementAt(detList.getSelectedIndex());
        }

        public void addPlugin(String s) {
            int n = (plugins == null) ? 0 : plugins.size();
            PrintPlugin p;
            String fullname = (plugindir != null) ? plugindir + "." + s : s;
            try {
                Class<?> c = YassUtils.forName(fullname);
                p = (PrintPlugin) c.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            String fullpath = fullname.replace('.', '/');
            URL u = getClass().getResource("/" + fullpath + ".gif");
            ImageIcon img = u != null ? new ImageIcon(u) : null;
            renderer.addStyle(img, p.getTitle(), p.getDescription());
            detList.addItem(new Integer(n));
            plugins.addElement(p);
        }

        public void selectPlugin(int i) {
            detList.setSelectedIndex(i);
        }

        public Hashtable<String, Object> getValues() {
            return hash;
        }
    }
}

