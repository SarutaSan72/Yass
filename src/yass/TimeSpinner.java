package yass;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    25. September 2007
 */
public class TimeSpinner extends JPanel {
	private static final long serialVersionUID = -1220107624676188602L;
	private int duration;
	private String ms;
	private JSpinner msSpinner = null;
	private SpinnerNumberModel msModel = null;
	private JLabel lab1 = null, lab2 = null;


	/**
	 *  Sets the enabled attribute of the TimeSpinner object
	 *
	 * @param  onoff  The new enabled value
	 */
	public void setEnabled(boolean onoff) {
		msSpinner.setEnabled(onoff);
		if (lab1 != null) {
			lab1.setEnabled(onoff);
		}
		if (lab2 != null) {
			lab2.setEnabled(onoff);
		}
	}


	/**
	 *  Constructor for the TimeSpinner object
	 *
	 * @param  label  Description of the Parameter
	 * @param  init   Description of the Parameter
	 * @param  dur    Description of the Parameter
	 */
	public TimeSpinner(String label, int init, int dur) {
		this(label, init, dur, null);
	}


	/**
	 *  Constructor for the TimeSpinner object
	 *
	 * @param  label  Description of the Parameter
	 * @param  init   Description of the Parameter
	 * @param  dur    Description of the Parameter
	 * @param  type   Description of the Parameter
	 */
	public TimeSpinner(String label, int init, int dur, int type) {
		this(label, init, dur, null, type);
	}


	/**
	 *  Description of the Field
	 */
	public final static int POSITIVE = 1, NEGATIVE = 2;


	/**
	 *  Constructor for the TimeSpinner object
	 *
	 * @param  dur    Description of the Parameter
	 * @param  init   Description of the Parameter
	 * @param  label  Description of the Parameter
	 * @param  mss    Description of the Parameter
	 */
	public TimeSpinner(String label, int init, int dur, String mss) {
		this(label, init, dur, mss, POSITIVE);
	}


	/**
	 *  Constructor for the TimeSpinner object
	 *
	 * @param  label  Description of the Parameter
	 * @param  init   Description of the Parameter
	 * @param  dur    Description of the Parameter
	 * @param  mss    Description of the Parameter
	 * @param  type   Description of the Parameter
	 */
	public TimeSpinner(String label, int init, int dur, String mss, int type) {
		duration = dur;
		ms = mss;

		msModel = new SpinnerNumberModel(init, (type == POSITIVE ? 0 : -duration), duration, 10);
		msSpinner = new JSpinner(msModel);

		JTextField tf = ((JSpinner.DefaultEditor) msSpinner.getEditor()).getTextField();
		tf.setColumns(new String(duration + "").length());
		tf.setHorizontalAlignment(JTextField.RIGHT);
		tf.addKeyListener(
			new KeyAdapter() {
				public void keyTyped(KeyEvent e) {
					char c = e.getKeyChar();
					if (!((Character.isDigit(c) || c == ',' || c == '.'))) {
						e.consume();
					}
				}
			});

		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.LINE_AXIS));
		boxPanel.setOpaque(false);

		if (label != null) {
			lab1 = new JLabel(label, JLabel.LEFT);
			lab1.setOpaque(false);
			boxPanel.add(lab1);
			boxPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		}
		boxPanel.add(Box.createRigidArea(new Dimension(5, 0)));

		boxPanel.add(msSpinner);
		msSpinner.setToolTipText(I18.get("time_spinner_tip"));

		if (ms != null) {
			lab2 = new JLabel(ms, JLabel.LEFT);
			lab2.setOpaque(false);
			boxPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			boxPanel.add(lab2);
			boxPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		}

		setLayout(new BorderLayout());
		setOpaque(false);
		add("Center", boxPanel);
	}


	/**
	 *  Sets the toolTipText attribute of the TimeSpinner object
	 *
	 * @return    The spinner value
	 */
	public JSpinner getSpinner() {
		return msSpinner;
	}


	/**
	 *  Gets the time attribute of the TimeSpinner object
	 *
	 * @return    The time value
	 */
	public int getTime() {
		return ((Integer) (msSpinner.getValue())).intValue();
	}


	/**
	 *  Sets the duration attribute of the TimeSpinner object
	 *
	 * @param  d  The new duration value
	 */
	public void setDuration(int d) {
		if (d == duration) {
			return;
		}
		duration = d;
		msModel.setMaximum(new Integer(duration));
	}


	/**
	 *  Sets the time attribute of the TimeSpinner object
	 *
	 * @param  t  The new time value
	 */
	public void setTime(int t) {
		Integer ms = (Integer) msSpinner.getValue();
		if (t == ms.intValue()) {
			return;
		}
		msSpinner.setValue(new Integer(t));
	}
}


