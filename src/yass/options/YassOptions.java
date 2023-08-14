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
import yass.YassActions;
import yass.YassProperties;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassOptions extends JDialog {
    private static final long serialVersionUID = -3946878493552010967L;
    private final JTree tree;
    private final Hashtable<String, OptionsPanel> panels;
    private final JPanel main;
    private final JScrollPane scrollMain;
    private final YassActions actions;


    /**
     * Constructor for the YassOptions object
     *
     * @param a Description of the Parameter
     */
    public YassOptions(YassActions a) {
        super(new OwnerFrame());

        actions = a;
        YassProperties prop = actions.getProperties();
        panels = new Hashtable<>();

        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Yass");
        DefaultMutableTreeNode library = new DefaultMutableTreeNode(I18.get("options_library"));
        DefaultMutableTreeNode meta = new DefaultMutableTreeNode(I18.get("options_metadata"));
        DefaultMutableTreeNode checker = new DefaultMutableTreeNode(I18.get("options_errors"));
        DefaultMutableTreeNode editor = new DefaultMutableTreeNode(I18.get("options_editor"));
        DefaultMutableTreeNode advanced = new DefaultMutableTreeNode(I18.get("options_advanced"));
        top.add(library);
        top.add(meta);
        top.add(checker);
        top.add(editor);
        top.add(advanced);

        OptionsPanel.loadProperties(prop);

        addPanel(library, I18.get("options_directories"), new DirPanel());
        addPanel(library, I18.get("options_groups_1"), new GroupPanel());
        addPanel(library, I18.get("options_groups_2"), new Group2Panel());
        addPanel(library, I18.get("options_sorting"), new SortPanel());
        addPanel(library, I18.get("options_printer"), new PrintPanel());
        addPanel(library, I18.get("options_cache"), new CachePanel());
        addPanel(library, I18.get("options_filetypes"), new FiletypePanel());

        addPanel(meta, I18.get("options_languages"), new LanguagePanel());
        addPanel(meta, I18.get("options_editions"), new EditionPanel());
        addPanel(meta, I18.get("options_genres"), new GenrePanel());

        addPanel(checker, I18.get("options_tags"), new TagPanel());
        addPanel(checker, I18.get("options_images"), new ImagesErrorPanel());
        addPanel(checker, I18.get("options_errors_fonts"), new FontErrorPanel());
        addPanel(checker, I18.get("options_errors_breaks"), new ErrorPanel());
        addPanel(checker, I18.get("options_errors_score"), new BonusErrorPanel());

        addPanel(editor, I18.get("options_design"), new ColorPanel());
        addPanel(editor, I18.get("options_control"), new SketchPanel());
        addPanel(editor, I18.get("options_keyboard"), new KeyboardPanel());
        addPanel(editor, I18.get("options_spelling"), new DictionaryPanel());

        addPanel(advanced, I18.get("options_advanced_audio"), new AudioPanel());
        addPanel(advanced, I18.get("options_advanced_debug"), new DebugPanel());

        // addPanel(advanced, "Labels", new LabelPanel());
        //addPanel(advanced, "Plugins", new PluginPanel());

        JPanel panel = new JPanel(new BorderLayout());
        main = new JPanel(new BorderLayout());
        //left
        main.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        panel.add("West", new JScrollPane(tree = new JTree(top)));
        panel.add("Center", scrollMain = new JScrollPane(main));

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        expandAll(tree);

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(
                new TreeSelectionListener() {
                    public void valueChanged(TreeSelectionEvent e) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                        if (node == null) {
                            return;
                        }
                        if (!node.isLeaf()) {
                            return;
                        }
                        String id = (String) node.getUserObject();
                        showPanel(id);
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

                            actions.storeColors();
                            OptionsPanel.storeProperties();
                            //System.out.println("store");
                        }

                        dispose();
                    }
                });

        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        Dimension dim = this.getToolkit().getScreenSize();
        setSize(800, 620);
        setLocation(dim.width / 2 - 400, dim.height / 2 - 250);
        setTitle(I18.get("options_title"));
        showPanel(I18.get("options_directories"));
        setVisible(true);
    }

    /**
     * Adds a feature to the Node attribute of the YassOptions object
     *
     * @param node  The feature to be added to the Node attribute
     * @param title The feature to be added to the Node attribute
     * @param p     The feature to be added to the Node attribute
     */
    public void addPanel(DefaultMutableTreeNode node, String title, OptionsPanel p) {
        DefaultMutableTreeNode n = new DefaultMutableTreeNode(title);
        node.add(n);
        panels.put(title, p);
        p.init(title);
    }

    /**
     * Gets the panel attribute of the YassOptions object
     *
     * @param id Description of the Parameter
     * @return The panel value
     */
    public OptionsPanel getPanel(String id) {
        return panels.get(id);
    }

    /**
     * Description of the Method
     *
     * @param id Description of the Parameter
     */
    public void showPanel(String id) {
        main.removeAll();
        main.add("Center", getPanel(id));
        getPanel(id).validate();
        scrollMain.validate();
        scrollMain.repaint();
    }

    /**
     * Description of the Method
     *
     * @param tree Description of the Parameter
     */
    public void expandAll(JTree tree) {
        int row = 0;
        while (row < tree.getRowCount()) {
            tree.expandRow(row);
            row++;
        }
    }


    private static class OwnerFrame extends JFrame {
        private static final long serialVersionUID = -5555522703593740252L;

        OwnerFrame() {
            URL icon16 = this.getClass().getResource("/yass/resources/img/yass-icon-16.png");
            URL icon32 = this.getClass().getResource("/yass/resources/img/yass-icon-32.png");
            URL icon48 = this.getClass().getResource("/yass/resources/img/yass-icon-48.png");
            ArrayList<Image> icons = new ArrayList<Image>();
            icons.add(new ImageIcon(icon48).getImage());
            icons.add(new ImageIcon(icon32).getImage());
            icons.add(new ImageIcon(icon16).getImage());
            setIconImages(icons);
        }
        // This frame can never be shown.

        @SuppressWarnings("deprecation")
        public void show() {
        }
    }
}

