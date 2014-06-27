// rkippen  (h8ttp://forum.java.sun.com/profile.jspa?userID=341349)
// Here's some code I'll put into the public domain:

// modified by saruta, 8/07
// removed cancel button fix
// removed escape key
// added directory key

package com.graphbuilder.desktop;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *  <p>
 *
 *  Attempt to improve the behaviour of the JFileChooser. Anyone who has used
 *  the the JFileChooser will probably remember that it lacks several useful
 *  features. The following features have been added:
 *  <ul>
 *    <li> Double click to choose a file</li>
 *    <li> Enter key to choose a file after typing the filename</li>
 *    <li> Enter key to change to a different directory after typing the
 *    filename</li>
 *    <li> Automatic rescanning of directories</li>
 *    <li> A getSelectedFiles method that returns the correct selected files
 *    </li>
 *    <li> Escape key cancels the dialog</li>
 *    <li> Access to common GUI components, such as the OK and Cancel buttons
 *    </li>
 *    <li> Removal of the useless Update and Help buttons in Motif L&F</li>
 *
 *  </ul>
 *  <p>
 *
 *  There are a lot more features that could be added to make the JFileChooser
 *  more user friendly. For example, a drop-down combo-box as the user is typing
 *  the name of the file, a list of currently visited directories, user
 *  specified file filtering, etc. <p>
 *
 *  The look and feels supported are Metal, Window and Motif. Each look and feel
 *  puts the OK and Cancel buttons in different locations and unfortunately the
 *  JFileChooser doesn't provide direct access to them. Thus, for each
 *  look-and-feel the buttons must be found. <p>
 *
 *  The following are known issues: Rescanning doesn't work when in Motif L&F.
 *  Some L&Fs have components that don't become available until the user clicks
 *  a button. For example, the Metal L&F has a JTable but only when viewing in
 *  details mode. The double click to choose a file does not work in details
 *  mode. There are probably more unknown issues, but the changes made so far
 *  should make the JFileChooser easier to use.
 *
 * @author     Saruta
 * @created    22. August 2007
 */
public class FileChooserFixer implements KeyListener, MouseListener, Runnable {

	/*
	 *  Had to make new buttons because when the original buttons are clicked
	 *  they revert back to the original label text.  I.e. some programmer decided
	 *  it would be a good idea to set the button text during an actionPerformed
	 *  method.
	 */
	private JFileChooser fileChooser = null;
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");
	private JList<?> fileList = null;
	private JTextField filenameTextField = null;
	private long rescanTime = 20000;


	/**
	 *  Constructor for the FileChooserFixer object
	 *
	 * @param  fc  Description of the Parameter
	 * @param  a   Description of the Parameter
	 */
	public FileChooserFixer(JFileChooser fc, ActionListener a) {
		fileChooser = fc;

		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		okButton.setMnemonic('O');
		cancelButton.setMnemonic('C');

		JTextField[] textField = getTextFields(fc);
		JButton[] button = getButtons(fc);
		JList[] list = getLists(fc);

		String laf = javax.swing.UIManager.getLookAndFeel().getClass().getName();

		if (laf.equals("javax.swing.plaf.metal.MetalLookAndFeel")) {
			filenameTextField = textField[0];
			fileList = list[0];
		} else if (laf.equals("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")) {
			filenameTextField = textField[0];
			fileList = list[0];
		} else if (laf.equals("com.sun.java.swing.plaf.motif.MotifLookAndFeel")) {
			button[1].setVisible(false);
			// hides the do-nothing 'Update' button
			button[3].setVisible(false);
			// hides the disabled 'Help' button
			filenameTextField = textField[1];
			fileList = list[0];
		}

		fileList.addMouseListener(this);
		addKeyListeners(fileChooser);

		new Thread(this).start();
		// note: rescanning in Motif feel doesn't work
	}


	/**
	 *  Main processing method for the FileChooserFixer object
	 */
	public void run() {
		try {
			while (true) {
				Thread.sleep(rescanTime);
				Window w = SwingUtilities.windowForComponent(fileChooser);
				if (w != null && w.isVisible()) {
					fileChooser.rescanCurrentDirectory();
				}
			}
		}
		catch (Throwable err) {}
	}


	/**
	 *  Gets the rescanTime attribute of the FileChooserFixer object
	 *
	 * @return    The rescanTime value
	 */
	public long getRescanTime() {
		return rescanTime;
	}


	/**
	 *  Sets the rescanTime attribute of the FileChooserFixer object
	 *
	 * @param  t  The new rescanTime value
	 */
	public void setRescanTime(long t) {
		if (t < 200) {
			throw new IllegalArgumentException("Rescan time >= 200 required.");
		}

		rescanTime = t;
	}


	/**
	 *  Adds a feature to the KeyListeners attribute of the FileChooserFixer object
	 *
	 * @param  c  The feature to be added to the KeyListeners attribute
	 */
	private void addKeyListeners(Container c) {
		for (int i = 0; i < c.getComponentCount(); i++) {
			Component d = c.getComponent(i);
			if (d instanceof Container) {
				addKeyListeners((Container) d);
			}
			d.addKeyListener(this);
		}
	}

	/**
	 *  Gets the oKButton attribute of the FileChooserFixer object
	 *
	 * @return    The oKButton value
	 */
	public JButton getOKButton() {
		return okButton;
	}


	/**
	 *  Gets the cancelButton attribute of the FileChooserFixer object
	 *
	 * @return    The cancelButton value
	 */
	public JButton getCancelButton() {
		return cancelButton;
	}


	/**
	 *  Gets the fileList attribute of the FileChooserFixer object
	 *
	 * @return    The fileList value
	 */
	public JList<?> getFileList() {
		return fileList;
	}


	/**
	 *  Gets the filenameTextField attribute of the FileChooserFixer object
	 *
	 * @return    The filenameTextField value
	 */
	public JTextField getFilenameTextField() {
		return filenameTextField;
	}


	/**
	 *  Gets the fileChooser attribute of the FileChooserFixer object
	 *
	 * @return    The fileChooser value
	 */
	public JFileChooser getFileChooser() {
		return fileChooser;
	}


	/**
	 *  Gets the buttons attribute of the FileChooserFixer object
	 *
	 * @param  fc  Description of the Parameter
	 * @return     The buttons value
	 */
	protected JButton[] getButtons(JFileChooser fc) {
		Vector<Component> v = new Vector<Component>();
		Stack<Component> s = new Stack<Component>();
		s.push(fc);
		while (!s.isEmpty()) {
			Component c = (Component) s.pop();

			if (c instanceof Container) {
				Container d = (Container) c;
				for (int i = 0; i < d.getComponentCount(); i++) {
					if (d.getComponent(i) instanceof JButton) {
						v.add(d.getComponent(i));
					} else {
						s.push(d.getComponent(i));
					}
				}
			}
		}

		JButton[] arr = new JButton[v.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = (JButton) v.get(i);
		}

		return arr;
	}


	/**
	 *  Gets the textFields attribute of the FileChooserFixer object
	 *
	 * @param  fc  Description of the Parameter
	 * @return     The textFields value
	 */
	protected JTextField[] getTextFields(JFileChooser fc) {
		Vector<Component> v = new Vector<Component>();
		Stack<Component> s = new Stack<Component>();
		s.push(fc);
		while (!s.isEmpty()) {
			Component c = (Component) s.pop();

			if (c instanceof Container) {
				Container d = (Container) c;
				for (int i = 0; i < d.getComponentCount(); i++) {
					if (d.getComponent(i) instanceof JTextField) {
						v.add(d.getComponent(i));
					} else {
						s.push(d.getComponent(i));
					}
				}
			}
		}

		JTextField[] arr = new JTextField[v.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = (JTextField) v.get(i);
		}

		return arr;
	}


	/**
	 *  Gets the lists attribute of the FileChooserFixer object
	 *
	 * @param  fc  Description of the Parameter
	 * @return     The lists value
	 */
	protected JList[] getLists(JFileChooser fc) {
		Vector<Component> v = new Vector<Component>();
		Stack<Component> s = new Stack<Component>();
		s.push(fc);
		while (!s.isEmpty()) {
			Component c = (Component) s.pop();

			if (c instanceof Container) {
				Container d = (Container) c;
				for (int i = 0; i < d.getComponentCount(); i++) {
					if (d.getComponent(i) instanceof JList) {
						v.add(d.getComponent(i));
					} else {
						s.push(d.getComponent(i));
					}
				}
			}
		}

		JList[] arr = new JList[v.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = (JList<?>) v.get(i);
		}

		return arr;
	}


	/**
	 *  Gets the selectedFiles attribute of the FileChooserFixer object
	 *
	 * @return    The selectedFiles value
	 */
	public File[] getSelectedFiles() {
		File[] f = fileChooser.getSelectedFiles();
		if (f.length == 0) {
			File file = fileChooser.getSelectedFile();
			if (file != null) {
				f = new File[]{file};
			}
		}
		return f;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  evt  Description of the Parameter
	 */
	public void mousePressed(MouseEvent evt) {
		Object src = evt.getSource();

		if (src == fileList) {
			if (evt.getModifiers() != InputEvent.BUTTON1_MASK) {
				return;
			}

			int index = fileList.locationToIndex(evt.getPoint());
			if (index < 0) {
				return;
			}
			fileList.setSelectedIndex(index);
			File[] arr = getSelectedFiles();

			if (evt.getClickCount() == 1 && arr.length == 1 && arr[0].isDirectory()) {
				fileChooser.setCurrentDirectory(arr[0]);
				filenameTextField.setText("");
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  evt  Description of the Parameter
	 */
	public void mouseReleased(MouseEvent evt) { }


	/**
	 *  Description of the Method
	 *
	 * @param  evt  Description of the Parameter
	 */
	public void mouseClicked(MouseEvent evt) {
		Object src = evt.getSource();

		if (src == fileList) {
			if (evt.getModifiers() != InputEvent.BUTTON1_MASK) {
				return;
			}

			int index = fileList.locationToIndex(evt.getPoint());
			if (index < 0) {
				return;
			}
			fileList.setSelectedIndex(index);
			File[] arr = getSelectedFiles();

			//changed by saruta
			//if (evt.getClickCount() == 2 && arr.length == 1 && arr[0].isFile())
			//	actionPerformed(new ActionEvent(okButton, 0, okButton.getActionCommand()));
			if (evt.getClickCount() == 2 && arr.length == 1 && arr[0].isDirectory()) {
				fileChooser.setCurrentDirectory(arr[0]);
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  evt  Description of the Parameter
	 */
	public void mouseEntered(MouseEvent evt) { }


	/**
	 *  Description of the Method
	 *
	 * @param  evt  Description of the Parameter
	 */
	public void mouseExited(MouseEvent evt) { }


	/**
	 *  Description of the Method
	 *
	 * @param  evt  Description of the Parameter
	 */
	public void keyPressed(KeyEvent evt) {
		Object src = evt.getSource();
		int code = evt.getKeyCode();

		//changed by saruta
		// if (code == KeyEvent.VK_ESCAPE)
		//	actionPerformed(new ActionEvent(cancelButton, 0, cancelButton.getActionCommand()));

		if (src == fileList) {
			if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
				File[] arr = getSelectedFiles();
				if (arr.length == 1 && arr[0].isDirectory()) {
					fileChooser.setCurrentDirectory(arr[0]);
				}
				evt.consume();
				//filenameTextField.setText(arr[0].getPath());
			}
			//if (code == KeyEvent.VK_ENTER) {
			//	fileList.getSelectionModel().clearSelection();
			//	actionPerformed(new ActionEvent(okButton, 0, "enter"));
			//}
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  evt  Description of the Parameter
	 */
	public void keyReleased(KeyEvent evt) { }


	/**
	 *  Description of the Method
	 *
	 * @param  evt  Description of the Parameter
	 */
	public void keyTyped(KeyEvent evt) { }
}

