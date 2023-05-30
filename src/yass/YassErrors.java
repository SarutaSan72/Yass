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

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.StringJoiner;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassErrors extends JPanel {
    private static final long serialVersionUID = 2825245752115131894L;
    YassProperties prop;
    JPanel errorPanel = null;
    JTable errTable;
    JPanel msgPanel = null, msgButtonPanel = null;
    DefaultTableModel tm = null;
    private YassActions actions = null;
    private JLabel popupLabel = null;

    private JToolBar buttons = null;

    private YassAutoCorrect auto = null;

    private boolean preventTableUpdate = false;
    private YassTable table = null;


    /**
     * Constructor for the YassGeneral object
     *
     * @param p    Description of the Parameter
     * @param a    Description of the Parameter
     * @param toolbar Description of the Parameter
     */
    public YassErrors(YassActions a, YassProperties p, JComponent toolbar) {
        actions = a;
        prop = p;
        setLayout(new BorderLayout());
        add("Center", errorPanel = new JPanel(new BorderLayout()));

        popupLabel = new JLabel();
        popupLabel.setVerticalAlignment(SwingConstants.TOP);

        String[] columns = {I18.get("tool_errors_col_0"), I18.get("tool_errors_col_1"), I18.get("tool_errors_col_2"), I18.get("tool_errors_col_3"), I18.get("tool_errors_col_4"), "#"};
        tm =
                new DefaultTableModel(null, columns) {
                    private static final long serialVersionUID = 4298268313228698022L;

                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

        errTable = new JTable(tm);
        errTable.setBackground(new JLabel().getBackground());
        errTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        errTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent event) {
                        updateMessage();
                    }
                });

        errTable.getTableHeader().setReorderingAllowed(false);
        errTable.createDefaultColumnsFromModel();

        errTable.getColumnModel().getColumn(0).setMinWidth(240);
        errTable.getColumnModel().getColumn(0).setMaxWidth(240);
        errTable.getColumnModel().getColumn(0).setPreferredWidth(240);
        errTable.getColumnModel().getColumn(0).setResizable(true);

        errTable.getColumnModel().getColumn(1).setMinWidth(48);
        errTable.getColumnModel().getColumn(1).setMaxWidth(48);
        errTable.getColumnModel().getColumn(1).setPreferredWidth(48);
        errTable.getColumnModel().getColumn(1).setResizable(false);

        errTable.getColumnModel().getColumn(2).setMinWidth(48);
        errTable.getColumnModel().getColumn(2).setMaxWidth(48);
        errTable.getColumnModel().getColumn(2).setPreferredWidth(48);
        errTable.getColumnModel().getColumn(2).setResizable(false);

        errTable.getColumnModel().getColumn(3).setMinWidth(0);
        errTable.getColumnModel().getColumn(3).setMaxWidth(0);
        errTable.getColumnModel().getColumn(3).setPreferredWidth(0);
        errTable.getColumnModel().getColumn(3).setResizable(false);

        errTable.getColumnModel().getColumn(4).setMinWidth(64);
        errTable.getColumnModel().getColumn(4).setPreferredWidth(64);
        errTable.getColumnModel().getColumn(4).setResizable(true);

        errTable.getColumnModel().getColumn(5).setMinWidth(0);
        errTable.getColumnModel().getColumn(5).setMaxWidth(0);
        errTable.getColumnModel().getColumn(5).setPreferredWidth(0);
        errTable.getColumnModel().getColumn(5).setResizable(false);

        errorPanel.add("Center", new JScrollPane(errTable));
        errorPanel.add("South", msgPanel = new JPanel(new BorderLayout()));
        msgPanel.add("Center", popupLabel);
        msgPanel.add("South", msgButtonPanel = new JPanel(new BorderLayout()));
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        msgButtonPanel.add("Center", tb);
        msgButtonPanel.add("East", toolbar);

        buttons = new JToolBar(I18.get("tool_correct"));
        buttons.setFloatable(false);
        JButton okButton;
        buttons.add(okButton = new JButton(I18.get("tool_correct_ok")));
        JButton allButton;
        buttons.add(allButton = new JButton(I18.get("tool_correct_all")));
        JButton cancelButton;
        buttons.add(cancelButton = new JButton(I18.get("tool_correct_cancel")));

        okButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (table != null) {
                            int viewRow = errTable.getSelectedRow();
                            if (viewRow < 0) {
                                return;
                            }
                            Vector<?> data = tm.getDataVector();
                            if (data.size() <= viewRow) {
                                return;
                            }

                            Vector<?> v = (Vector<?>) data.elementAt(viewRow);
                            int i = ((Integer) (v.elementAt(3))).intValue();
                            int id = ((Integer) (v.elementAt(5))).intValue();
                            YassRow r = table.getRowAt(i);
                            String m = r.getMessage(id);

                            boolean changed = auto.autoCorrect(table, false, m);
                            if (changed) {
                                table.addUndo();
                                ((YassTableModel) table.getModel()).fireTableDataChanged();
                            }

                            auto.checkData(table, false, true);

                            SwingUtilities.invokeLater(
                                    new Runnable() {
                                        public void run() {
                                            updateMessage();
                                        }
                                    });
                        }
                    }
                });
        allButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (table != null) {
                            int viewRow = errTable.getSelectedRow();
                            if (viewRow < 0) {
                                return;
                            }
                            Vector<?> data = tm.getDataVector();
                            if (data.size() <= viewRow) {
                                return;
                            }

                            Vector<?> v = (Vector<?>) data.elementAt(viewRow);
                            int i = ((Integer) (v.elementAt(3))).intValue();
                            int id = ((Integer) (v.elementAt(5))).intValue();
                            YassRow r = table.getRowAt(i);
                            String m = r.getMessage(id);

                            boolean changed = auto.autoCorrect(table, true, m);
                            if (changed) {
                                table.addUndo();
                                ((YassTableModel) table.getModel()).fireTableDataChanged();
                            }
                            auto.checkData(table, false, true);

                            SwingUtilities.invokeLater(
                                    new Runnable() {
                                        public void run() {
                                            updateMessage();
                                        }
                                    });
                        }
                    }
                });
        cancelButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        SwingUtilities.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        actions.hideErrors();
                                    }
                                });
                    }
                });
    }

    /**
     * Sets the auto attribute of the YassMain object
     *
     * @param a The new auto value
     */
    public void setAutoCorrect(YassAutoCorrect a) {
        auto = a;
    }

    /**
     * Description of the Method
     *
     * @param onoff Description of the Parameter
     */
    public void preventTableUpdate(boolean onoff) {
        preventTableUpdate = onoff;
    }


	/*
     *  public boolean hasFocus() {
	 *  return super.hasFocus() || okButton.hasFocus() || allButton.hasFocus();
	 *  }
	 */

    /**
     * Description of the Method
     */
    public void updateMessage() {
        if (table == null) {
            return;
        }

        int rows[] = errTable.getSelectedRows();
        if (rows == null || rows.length != 1) {
            hideMessage();
            return;
        }
        int viewRow = rows[0];

        Vector<?> data = tm.getDataVector();
        if (data.size() <= viewRow) {
            hideMessage();
            return;
        }

        Vector<?> v = (Vector<?>) data.elementAt(viewRow);
        int i = ((Integer) (v.elementAt(3))).intValue();
        int id = ((Integer) (v.elementAt(5))).intValue();

        if (!preventTableUpdate) {
            table.setRowSelectionInterval(i, i);
            table.updatePlayerPosition();
            table.zoomPage();
        }

        YassRow r = table.getRowAt(i);

        String msg = getMessage(r, id);
        popupLabel.setText(msg);

        String mess = r.getMessage(id);
        msgButtonPanel.remove(buttons);
        if (auto.autoCorrectionSupported(mess)) {
            msgButtonPanel.add("West", buttons);
        }
        repaint();
    }

    /**
     * Description of the Method
     */
    public void hideMessage() {
        popupLabel.setText("");
        msgButtonPanel.remove(buttons);
        repaint();
    }

    /**
     * Description of the Method
     *
     * @param id Description of the Parameter
     */
    public void showMessage(int id) {
        int rows[] = table.getSelectedRows();
        if (rows == null || rows.length != 1) {
            hideMessage();
            return;
        }
        int i = rows[0];

        YassRow r = table.getRowAt(i);
        if (!r.hasMessage()) {
            hideMessage();
            return;
        }

        String mess = r.getMessage(id);
        if (mess == null) {
            hideMessage();
            return;
        }

        String msg = getMessage(r, id);
        popupLabel.setText(msg);
        //popupLabel.repaint();

        msgButtonPanel.remove(buttons);
        if (auto.autoCorrectionSupported(mess)) {
            msgButtonPanel.add("West", buttons);
        }

        Vector<?> data = tm.getDataVector();
        int k = 0;
        for (Enumeration<?> en = data.elements(); en.hasMoreElements(); k++) {
            Vector<?> v = (Vector<?>) en.nextElement();
            int ii = ((Integer) (v.elementAt(3))).intValue();
            if (i == ii) {
                preventTableUpdate(true);
                errTable.setRowSelectionInterval(k, k);
                Rectangle rr = errTable.getCellRect(k, 0, true);
                errTable.scrollRectToVisible(rr);
                preventTableUpdate(false);
                break;
            }
        }
        repaint();
    }

    /**
     * Gets the messagePanel attribute of the YassErrors object
     *
     * @return The messagePanel value
     */
    public JPanel getMessagePanel() {
        return msgPanel;
    }

    /**
     * Gets the message attribute of the YassAutoCorrect object
     *
     * @param r Description of the Parameter
     * @param i Description of the Parameter
     * @return The message value
     */
    public String getMessage(YassRow r, int i) {
        if (!r.hasMessage()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><font size=+1 color=red>");
        String mess[] = r.getMessageWithDetail(i);
        sb.append(I18.get(mess[0]));
        sb.append("</font><br><font color=black>");
        String msg = I18.get(getErrorMessageKey(mess[0]));
        if (msg != null) {
            sb.append(msg);
        }
        if (mess.length > 1 && mess[1] != null) {
            sb.append("</font><br><font size=-2 color=red>");
            sb.append(mess[1]);
        }
        if (!auto.isAutoCorrectionSafe(r.getMessage()) && auto.autoCorrectionSupported(r.getMessage())) {
            sb.append("</font><br><font size=-2 color=red>").append(I18.get("tool_correct_unsafe"));
        }
        sb.append("</font></html>");
        return sb.toString();
    }

    private String getErrorMessageKey(String errorKey) {
        StringJoiner messageKey = new StringJoiner("_");
        messageKey.add(errorKey);
        if (YassRow.UNCOMMON_SPACING.equals(errorKey)) {
            String spacingMode = StringUtils.defaultString(
                    prop.getProperty("correct-uncommon-spacing"), "after");
            messageKey.add(spacingMode);
        }
        messageKey.add("msg");
        return messageKey.toString();
    }

    /**
     * Gets the empty attribute of the YassErrors object
     *
     * @return The empty value
     */
    public boolean isEmpty() {
        return errTable.getRowCount() < 1;
    }


    /**
     * Sets the general attribute of the YassGeneral object
     *
     * @param t The new general value
     */
    public void setTable(YassTable t) {

        table = t;
        tm.getDataVector().clear();

        if (table != null) {
            YassTableModel tablemodel = (YassTableModel) t.getModel();

            int rows = tablemodel.getRowCount();
            int line = 1;
            int note = 0;
            for (int i = 0; i < rows; i++) {
                YassRow r = tablemodel.getRowAt(i);
                if (r.isNote()) {
                    note++;
                }
                if (r.isPageBreak()) {
                    note = 0;
                    line++;
                }
                String txt = r.getText();
                if (txt == null) {
                    txt = "";
                }

                Vector<String[]> msg = r.getMessages();
                if (msg != null) {
                    int id = 0;
                    for (Enumeration<String[]> en = msg.elements(); en.hasMoreElements(); id++) {
                        String[] m = en.nextElement();
                        String m18 = I18.get(m[0]);
                        tm.addRow(new Object[]{m18, line, note, new Integer(i), txt, new Integer(id)});
                    }
                }
            }
        }
        tm.fireTableDataChanged();
        //errTable.repaint();
    }
}


