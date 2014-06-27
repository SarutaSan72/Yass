package yass;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    19. September 2007
 */
public class YassTableRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = -8540061131858129154L;
	private Color red = new Color(1f, .8f, .8f);
	private Color darkRed = new Color(.8f, .5f, .5f);


	/**
	 *  Gets the tableCellRendererComponent attribute of the YassTableRenderer
	 *  object
	 *
	 * @param  table       Description of the Parameter
	 * @param  value       Description of the Parameter
	 * @param  isSelected  Description of the Parameter
	 * @param  hasFocus    Description of the Parameter
	 * @param  row         Description of the Parameter
	 * @param  column      Description of the Parameter
	 * @return             The tableCellRendererComponent value
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
	                                               boolean isSelected, boolean hasFocus, int row, int column) {
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if ((column == 4) || table.getValueAt(row, 0).equals("#")) {
			setHorizontalAlignment(LEFT);
		} else {
			setHorizontalAlignment(RIGHT);
		}

		YassTable t = ((YassTable) table);
		boolean showMessages = t.showMessages();

		YassRow r = t.getRowAt(row);
		boolean hidden = r.isHidden();
		if (hidden) {
			setText("");
		}

		if ((!(r.isNote())) && !isSelected) {
			if (r.hasMessage() && showMessages && !hidden) {
				cell.setBackground(darkRed);
			} else {
				cell.setBackground(Color.lightGray);
			}
		} else if (!isSelected) {
			if (r.hasMessage() && showMessages && !hidden) {
				cell.setBackground(red);
			} else {
				cell.setBackground(Color.white);
			}

		}
		return this;
	}
}

