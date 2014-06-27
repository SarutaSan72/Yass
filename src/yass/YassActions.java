package yass;

import com.nexes.wizard.Wizard;
import yass.renderer.YassBasicRenderer;
import yass.renderer.YassPlaybackRenderer;
import yass.renderer.YassSession;
import yass.screen.YassScreen;
import yass.screen.YassSongData;
import yass.screen.YassTheme;
import yass.wizard.CreateSongWizard;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

/**
 * Description of the Class
 * 
 * @author Saruta
 * @created 21. August 2007
 */
public class YassActions implements DropTargetListener {

	/**
	 * Description of the Field
	 */
	public final static String VERSION = "1.5.1";
	/**
	 * Description of the Field
	 */
	public final static String DATE = "5/2014";

	private YassTable table = null;
	private YassSheet sheet = null;
	private YassLyrics lyrics = null;
	private YassPlayer mp3 = null;
	private YassPlayList playList = null;
	private YassSongInfo songInfo = null;
	private YassVideo video = null;
	private YassErrors errors = null;
	private YassProperties prop = null;
	private YassAutoCorrect auto = null;
	private YassSongList songList = null;
	private YassGroups groups = null;

	private YassScreen screen = null;

	private JComboBox<String> groupsBox = null;

	private Hashtable<String, ImageIcon> icons = new Hashtable<>();
	private JToggleButton mp3Button, midiButton, vmarkButton, detailToggle,
			songInfoToggle, bgToggle, playlistToggle, snapshotButton,
			videoButton, videoAudioButton;
	private JToggleButton errToggle = null;
	private JCheckBoxMenuItem playlistCBI = null;
	private JCheckBoxMenuItem audioCBI = null;
	private JCheckBoxMenuItem clicksCBI = null;
	private JCheckBoxMenuItem micCBI = null;
	private JCheckBoxMenuItem midiCBI = null;
	private JCheckBoxMenuItem playAllVideoCBI = null;
	private JCheckBoxMenuItem alignCBI = null;
	private JToggleButton filterAll = null;
	private JButton playToggle;
	private boolean isLibraryPlaying = false;

	private JRadioButtonMenuItem showNothingToggle, showBackgroundToggle,
			showVideoToggle = null;

	private boolean isUpdating = false;

	private JButton correctPageBreakButton, correctTransposedButton,
			correctSpacingButton, hasErrorsButton;
	// , correctFileNamesButton, correctTagsButton;
	JProgressBar progressBar = null;

	JComponent tab = null;

	private boolean playerDemoMode = false;

	private Color stdBackground = new JButton().getBackground(),
			errBackground = new Color(.8f, .5f, .5f),
			minorerrBackground = new Color(.8f, .8f, .5f);

	private Color[] hicolors = new Color[] { new Color(.3f, .3f, 0.3f, .3f),
			new Color(.3f, .6f, 0.3f, .3f), new Color(.3f, .3f, 0.6f, .3f),
			new Color(.3f, .6f, 0.6f, .3f), new Color(.6f, .6f, 0.3f, .3f),
			new Color(.6f, .3f, 0.6f, .3f), new Color(.4f, .4f, 0.6f, .3f),
			new Color(.4f, .6f, 0.4f, .3f), };
	private Color[] colors = new Color[] { new Color(.3f, .3f, 0.3f, .7f),
			new Color(.3f, .6f, 0.3f, .7f), new Color(.3f, .3f, 0.6f, .7f),
			new Color(.3f, .6f, 0.6f, .7f), new Color(.6f, .6f, 0.3f, .7f),
			new Color(.6f, .3f, 0.6f, .7f), new Color(.4f, .4f, 0.6f, .7f),
			new Color(.4f, .6f, 0.4f, .7f), };

	/**
	 * Description of the Method
	 */
	public void loadColors() {
		for (int i = 0; i < colors.length; i++) {
			String c = prop.getProperty("color-" + i);
			Color col = Color.decode(c);
			colors[i] = new Color(col.getRed(), col.getGreen(), col.getBlue(),
					255);
		}
		for (int i = 0; i < hicolors.length; i++) {
			Color col = colors[i];
			hicolors[i] = new Color(col.getRed(), col.getGreen(),
					col.getBlue(), 64);
		}

		Color ncol[] = new Color[6];
		for (int i = 0; i < ncol.length; i++) {
			String c = prop.getProperty("note-color-" + i);
			Color col = Color.decode(c);
			ncol[i] = new Color(col.getRed(), col.getGreen(), col.getBlue(),
					221);
		}
		sheet.setColors(ncol);

		boolean shade = prop.get("shade-notes").equals("true");
		sheet.shadeNotes(shade);
		sheet.repaint();
	}

	/**
	 * Description of the Method
	 */
	public void loadKeys() {
		int keycodes[] = sheet.getKeyCodes();

		for (int i = 0; i < keycodes.length; i++) {
			String str = prop.getProperty("key-" + i);
			int code = -1;
			try {
				code = AWTKeyStroke.getAWTKeyStroke(str).getKeyCode();
				if (code != -1) {
					keycodes[i] = code;
				}
			} catch (Exception e) {
			}

		}
	}

	/**
	 * Description of the Method
	 */
	public void loadScreenKeys() {
		int keycodes[] = YassScreen.getKeyCodes();
		int keylocations[] = YassScreen.getKeyLocations();

		for (int i = 0; i < keycodes.length; i++) {
			String str = prop.getProperty("screenkey-" + i);

			StringTokenizer st = new StringTokenizer(str, "|");
			String key = st.nextToken();
			String location = st.hasMoreTokens() ? st.nextToken() : "STANDARD";
			keylocations[i] = KeyEvent.KEY_LOCATION_STANDARD;
			if (location.equals("NUMPAD")) {
				keylocations[i] = KeyEvent.KEY_LOCATION_NUMPAD;
			} else if (location.equals("LEFT")) {
				keylocations[i] = KeyEvent.KEY_LOCATION_LEFT;
			} else if (location.equals("RIGHT")) {
				keylocations[i] = KeyEvent.KEY_LOCATION_RIGHT;
			}
			int code = -1;
			try {
				code = AWTKeyStroke.getAWTKeyStroke(key).getKeyCode();
				if (code != -1) {
					keycodes[i] = code;
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Description of the Method
	 */
	public void loadLayout() {
		String lyricsWidthString = prop.getProperty("lyrics-width");
		int lyricsWidth = new Integer(lyricsWidthString).intValue();
		sheet.setLyricsWidth(lyricsWidth);

		boolean debugMemory = prop.getProperty("debug-memory").equals("true");
		sheet.setDebugMemory(debugMemory);
	}

	/**
	 * Description of the Method
	 */
	public void storeColors() {
		for (int i = 0; i < colors.length; i++) {
			String c = "color-" + i;

			String rgb = Integer.toHexString(colors[i].getRGB());
			prop.put(c, rgb);
		}
		for (int i = 0; i < hicolors.length; i++) {
			String c = "hi-color-" + i;

			String rgb = Integer.toHexString(colors[i].getRGB());
			prop.put(c, rgb);
		}
	}

	/**
	 * Gets the tableColor attribute of the YassActions object
	 * 
	 * @param i
	 *            Description of the Parameter
	 * @return The tableColor value
	 */
	public Color getTableColor(int i) {
		return colors[i];
	}

	/**
	 * Gets the tableHiColor attribute of the YassActions object
	 * 
	 * @param i
	 *            Description of the Parameter
	 * @return The tableHiColor value
	 */
	public Color getTableHiColor(int i) {
		return hicolors[i];
	}

	/**
	 * Sets the tab attribute of the YassActions object
	 * 
	 * @param c
	 *            The new tab value
	 */
	public void setTab(JComponent c) {
		tab = c;
	}

	/**
	 * Gets the tab attribute of the YassActions object
	 * 
	 * @return The tab value
	 */
	public JComponent getTab() {
		return tab;
	}

	/**
	 * Gets the tableColors attribute of the YassActions object
	 * 
	 * @return The tableColors value
	 */
	public Color[] getTableColors() {
		return hicolors;
	}

	/**
	 * Gets the tableHiColors attribute of the YassActions object
	 * 
	 * @return The tableHiColors value
	 */
	public Color[] getTableHiColors() {
		return hicolors;
	}

	/**
	 * Constructor for the YassActions object
	 * 
	 * @param s
	 *            Description of the Parameter
	 * @param m
	 *            Description of the Parameter
	 * @param lyr
	 *            Description of the Parameter
	 */
	public YassActions(YassSheet s, YassPlayer m, YassLyrics lyr) {
		sheet = s;
		mp3 = m;
		lyrics = lyr;

		configureOptionPane();

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setString("");
		progressBar.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// String dir = prop.getProperty("song-directory");
				// if (dir != null && dir.length() > 0 && new
				// File(dir).exists()) {
				// return;
				// }
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						progressBar.repaint();
						new YassLibOptions(prop, YassActions.this, songList,
								mp3);
					}
				});
			}
		});

		mp3.addPlayerListener(new YassPlayerListener() {
			public void playerStarted() {
			}

			public void playerStopped() {
				if (currentView == VIEW_EDIT) {
					stopPlaying();
					stopRecording();
					YassTapNotes.evaluateTaps(tab, table,
							sheet.getTemporaryNotes());
				} else if (currentView == VIEW_LIBRARY) {
					if (!soonStarting) {
						stopPlaySong();
					}
				} else if (currentView == VIEW_PLAY_CURRENT) {
					if (pausePosition < 0) {
						setView(VIEW_LIBRARY);
					}
				} else if (currentView == VIEW_PLAYER) {
					if (pausePosition < 0) {
						screen.gotoScreen("viewscore");
					}
				} else if (currentView == VIEW_JUKEBOX) {
					if (pausePosition == -2) {
						screen.prevJukebox();
						YassScreen.getTheme().playSample("credits.mp3", true);
					} else {
						screen.nextJukebox();
						YassScreen.getTheme().playSample("credits.mp3", true);
					}
				}
				// int i = sheet.nextElement();
				// table.setRowSelectionInterval(i, i);
				// updatePlayerPosition();
				// sheet.repaint();
			}
		});

		// setDropTarget(sheet);
		registerEditorActions(sheet);
		PropertyChangeListener plis = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String p = e.getPropertyName();
				if (p.equals("play")) {
					String s = (String) e.getNewValue();
					Integer mode = (Integer) e.getOldValue();
					int m = mode == null ? 0 : mode.intValue();
					if (s.equals("start")) {
						playSelection(m);
					}
					if (s.equals("page")) {
						playPageOrFrozen(m, false);
					}
					if (s.equals("stop")) {
						interruptPlay();
					}
				} else if (p.equals("relHeight")) {
					int h = ((Integer) e.getNewValue()).intValue();
					table.shiftHeight(h);
				} else if (p.equals("midi")) {
					int h = ((Integer) e.getNewValue()).intValue();
					mp3.playMIDI(h);
				} else if (p.equals("relBeat")) {
					table.shiftBeat(((Integer) e.getNewValue()).intValue());
				} else if (p.equals("relBeatRemainder")) {
					table.shiftRemainder(((Integer) e.getNewValue()).intValue());
				} else if (p.equals("relBeatLine")) {
					table.shiftLine(((Integer) e.getNewValue()).intValue());
				} else if (p.equals("relLeft")) {
					table.shiftLeftEndian(((Integer) e.getNewValue())
							.intValue());
				} else if (p.equals("relRight")) {
					table.shiftRightEndian(((Integer) e.getNewValue())
							.intValue());
				} else if (p.equals("page")) {
					int i = ((Integer) e.getNewValue()).intValue();
					table.gotoPage(i);
				} else if (p.equals("one")) {
					onePage();
				} else if (p.equals("split")) {
					table.split(((Double) e.getNewValue()).doubleValue());
				} else if (p.equals("joinLeft")) {
					table.joinLeft();
				} else if (p.equals("joinRight")) {
					table.joinRight();
				} else if (p.equals("join")) {
					table.joinRows();
				} else if (p.equals("rollLeft")) {
					table.rollLeft();
				} else if (p.equals("rollRight")) {
					Character c = (Character) e.getOldValue();
					Integer i = (Integer) e.getNewValue();
					if (c != null && i != null) {
						table.rollRight(c.charValue(), i.intValue());
					} else {
						table.rollRight();
					}
				} else if (p.equals("removePageBreak")) {
					table.removePageBreak(true);
				} else if (p.equals("addPageBreak")) {
					table.insertPageBreak(true);
				} else if (p.equals("editLyrics")) {
					editLyrics();
				} else if (p.equals("allPages")) {
					allPages();
				} else if (p.equals("start")) {
					Integer i = (Integer) e.getNewValue();
					setStart(i.intValue());
				} else if (p.equals("end")) {
					Integer i = (Integer) e.getNewValue();
					setEnd(i.intValue());
				} else if (p.equals("gap")) {
					Integer i = (Integer) e.getNewValue();
					setGap(i.intValue());
				} else if (p.equals("videogap")) {
					Integer i = (Integer) e.getNewValue();
					setVideoGap(i.intValue());
				}
			}
		};
		sheet.addPropertyChangeListener(plis);
		lyr.addPropertyChangeListener(plis);
	}

	/**
	 * Sets the table attribute of the YassActions object
	 * 
	 * @param t
	 *            The new table value
	 */
	public void setTable(YassTable t) {
		if (table != null) {
			table.removePropertyChangeListener(propertyListener);
		}
		table = t;
		table.addPropertyChangeListener(propertyListener);
	}

	/**
	 * Gets the table attribute of the YassActions object
	 * 
	 * @return The table value
	 */
	public YassTable getTable() {
		return table;
	}

	/**
	 * Gets the properties attribute of the YassActions object
	 * 
	 * @return The properties value
	 */
	public YassProperties getProperties() {
		return prop;
	}

	PropertyChangeListener propertyListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			String p = e.getPropertyName();
			if (p.equals("edit")) {
				String s = (String) e.getNewValue();
				if (s.equals("start")) {
					editLyrics();
				}
				if (s.equals("stop")) {
					lyrics.finishEditing();
				}
			}
		}
	};

	JPopupMenu versionPopup = null;

	/**
	 * Description of the Method
	 * 
	 * @param c
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public JPopupMenu createVersionPopup(JComponent c) {
		versionPopup = new JPopupMenu();
		JMenuItem m = null;
		versionPopup.add(m = new JMenuItem(I18.get("mpop_versions")));
		m.setEnabled(false);
		versionPopup.addSeparator();
		versionPopup.add(createVersion);
		versionPopup.add(renameVersion);
		versionPopup.add(removeVersion);
		versionPopup.add(setAsStandard);
		// versionPopup.addSeparator();
		// versionPopup.add(mergeVersions);
		c.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					versionPopup.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					versionPopup.show(e.getComponent(), e.getX(), e.getY());
				}
			}

		});
		return versionPopup;
	}

	Vector<YassTable> openTables = null;

	/**
	 * Description of the Method
	 */
	public void closeAllTables() {
		setOpened(false);
		closeAll();
		table = createNextTable();

		lyrics.setTable(table);
		sheet.setActiveTable(table);
		songList.closeOpened();

		isUpdating = true;
		updateLyrics();
		errors.setTable(table);
		// updateMP3Info(table);
		// updateRaw();
		// updateCover();
		// updateBackground();
		// updateVideo();

		isUpdating = false;
	}

	/**
	 * Description of the Method
	 */
	public void closeAll() {
		if (openTables == null) {
			openTables = new Vector<>();
		}
		if (openTables.size() > 0) {
			openTables.clear();
			sheet.removeAll();
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public YassTable createNextTable() {
		if (openTables == null) {
			return null;
		}

		openTables.size();
		YassTable t = new YassTable();
		t.init(prop);
		t.setTableColor(getTableColor(0));

		sheet.addTable(t);
		openTables.addElement(t);
		t.setSheet(sheet);
		t.setAutoCorrect(auto);
		t.setActions(this);

		registerEditorActions(t);
		// setDropTarget(t);
		t.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				showMessage(0);
			}
		});
		t.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				showMessage(0);
			}
		});
		t.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				showMessage(0);
			}
		});
		return t;
	}

	/**
	 * Description of the Method
	 */
	public void updateMessage() {
		errors.updateMessage();
	}

	/**
	 * Description of the Method
	 * 
	 * @param id
	 *            Description of the Parameter
	 */
	public void showMessage(int id) {
		errors.showMessage(id);
	}

	/**
	 * Gets the frame attribute of the YassActions object
	 * 
	 * @param c
	 *            Description of the Parameter
	 * @return The frame value
	 */
	public JFrame getFrame(Component c) {
		Window root = SwingUtilities.getWindowAncestor(c);
		if (root instanceof JFrame) {
			return (JFrame) root;
		}
		return null;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public boolean autoTrim() {
		return autoTrim;
	}

	/**
	 * Sets the autoTrim attribute of the YassSheet object
	 * 
	 * @param onoff
	 *            The new autoTrim value
	 */
	public void setAutoTrim(boolean onoff) {
		autoTrim = onoff;
		if (sheet != null) {
			sheet.setAutoTrim(onoff);
		}
	}

	private boolean autoTrim = false;

	/**
	 * Description of the Method
	 * 
	 * @param t
	 *            Description of the Parameter
	 * @param checkAll
	 *            Description of the Parameter
	 * @param checkExtensive
	 *            Description of the Parameter
	 */
	public void checkData(YassTable t, boolean checkAll, boolean checkExtensive) {
		auto.checkData(t, checkAll, checkExtensive);

		errors.setTable(table);

		if (currentView == VIEW_EDIT && autoTrim()
				&& (t.hasMinorPageBreakMessages() || t.hasPageBreakMessages())) {
			trimPageBreaks();
		}

		if (t.hasMinorPageBreakMessages()) {
			correctPageBreakButton.setBackground(minorerrBackground);
			correctPageBreakButton.setEnabled(true);
		} else if (t.hasPageBreakMessages()) {
			correctPageBreakButton.setBackground(errBackground);
			correctPageBreakButton.setEnabled(true);
		} else {
			correctPageBreakButton.setBackground(stdBackground);
			correctPageBreakButton.setEnabled(false);
		}

		boolean hasSpacing = t.hasSpacingMessages();
		correctSpacingButton.setBackground(hasSpacing ? errBackground
				: stdBackground);
		correctSpacingButton.setEnabled(hasSpacing);

		boolean hasTrans = t.hasTransposedMessages();
		correctTransposedButton.setBackground(hasTrans ? errBackground
				: stdBackground);
		correctTransposedButton.setEnabled(hasTrans);

		if (hasSpacing || hasTrans) {
		}

		boolean empty = errors.isEmpty();
		hasErrorsButton.setBackground(empty ? stdBackground : errBackground);
		hasErrorsButton.setEnabled(!empty);
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public YassTable firstTable() {
		if (openTables == null || openTables.size() < 1) {
			return null;
		}
		return (YassTable) openTables.elementAt(0);
	}

	/**
	 * Gets the allTables attribute of the YassActions object
	 * 
	 * @return The allTables value
	 */
	public Vector<YassTable> getOpenTables() {
		return openTables;
	}

	JComboBox<JLabel> vBox = null;

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComboBox<JLabel> createVersionBox() {
		vBox = new JComboBox<>();
		vBox.setEditable(false);
		vBox.setEnabled(false);
		vBox.setPreferredSize(new Dimension(200, 36));
		vBox.setMaximumSize(new Dimension(200, 36));
		createVersionPopup(vBox);

		vBox.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 2262131610310303506L;
			JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);

			public Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if (value == null) {
					return super.getListCellRendererComponent(list, value,
							index, isSelected, cellHasFocus);
				}
				JLabel label = (JLabel) value;
				value = label.getText();
				if (value == null) {
					return super.getListCellRendererComponent(list, value,
							index, isSelected, cellHasFocus);
				}

				if (value.equals("SEPARATOR")) {
					return separator;
				}

				JComponent comp = (JComponent) super
						.getListCellRendererComponent(list, value, index,
								isSelected, cellHasFocus);
				Color fg = label.getForeground();
				comp.setForeground(fg);
				return comp;
			}
		});

		vBox.addActionListener(vBoxListener = new VersionListener());
		vBox.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					removeVersion();
				} else if (e.getKeyCode() == KeyEvent.VK_C) {
					createVersion();
				}
			}
		});

		return vBox;
	}

	VersionListener vBoxListener = null;

	/**
	 * Description of the Class
	 * 
	 * @author Saruta
	 * @created 28. September 2007
	 */
	class VersionListener implements ActionListener {
		/**
		 * Description of the Method
		 * 
		 * @param e
		 *            Description of the Parameter
		 */
		public void actionPerformed(ActionEvent e) {
			int k = vBox.getSelectedIndex();
			gotoVersion(k);
		}
	}

	/**
	 * Description of the Method
	 */
	public void updateVersionBox() {
		String active = null;
		if (table != null) {
			active = table.getDir() + File.separator + table.getFilename();
		}
		int k = 0;

		vBox.removeActionListener(vBoxListener);
		vBox.removeAllItems();
		vBox.setSelectedIndex(-1);

		int i = 0;
		JLabel label = null;
		Font f = new Label().getFont();
		for (Enumeration<YassTable> en = openTables.elements(); en
				.hasMoreElements();) {
			YassTable t = (YassTable) en.nextElement();
			String fn = t.getDir() + File.separator + t.getFilename();
			String title = t.getTitle();
			String version = t.getVersion();
			String s = title;
			title = YassSongInfo.trim(title, vBox, f);

			if (version != null && version.trim().length() > 0) {
				s = title + " [" + version + "]";
			}
			vBox.addItem(label = new JLabel(s));
			label.setForeground(t.getTableColor());

			// bug: not working
			label.setToolTipText(fn);

			if (active != null && active.equals(fn)) {
				k = i;
			}
			i++;
		}
		// vBox.addItem(label = new JLabel("SEPARATOR"));
		// label.setEnabled(false);
		// vBox.addItem(new JLabel("New Version"));

		vBox.setEnabled(vBox.getItemCount() > 1);
		vBox.setSelectedIndex(k);
		vBox.addActionListener(vBoxListener);
		vBox.validate();
	}

	/**
	 * Description of the Method
	 * 
	 * @param b
	 *            Description of the Parameter
	 */
	public void nextVersion(int b) {
		int i = vBox.getSelectedIndex();
		int n = openTables.size();
		i += b;
		if (i < 0) {
			i = n - 1;
		}
		if (i > n - 1) {
			i = 0;
		}
		gotoVersion(i);
	}

	/**
	 * Description of the Method
	 * 
	 * @param b
	 *            Description of the Parameter
	 */
	public void gotoVersion(int b) {
		int n = openTables.size();
		if (b < 0 || b > n - 1) {
			return;
		}
		vBox.setSelectedIndex(b);

		YassTable t = (YassTable) openTables.elementAt(b);
		setTable(t);

		auto.checkData(table, false, true);

		/*
		 * int sheet_pos = sheet.getPlayerPosition(); long ms_pos =
		 * sheet.fromTimeline(sheet_pos); double view_x = ((JViewport)
		 * sheet.getParent()).getViewPosition().x; double off = sheet_pos -
		 * view_x; System.out.println("sheet_pos "+sheet_pos);
		 * System.out.println("ms_pos "+ms_pos);
		 * System.out.println("view_x "+view_x); System.out.println("off "+off);
		 */
		sheet.setActiveTable(table);
		String vd = table.getVideo();
		if (video != null && vd != null) {
			video.setVideo(table.getDir() + File.separator + vd);
		}
		String bg = table.getBackgroundTag();
		if (bg != null) {
			File file = new File(table.getDir() + File.separator + bg);
			BufferedImage img = null;
			if (file.exists()) {
				try {
					img = YassUtils.readImage(file);
				} catch (Exception e) {
					img = null;
				}
			}
			sheet.setBackgroundImage(img);
			mp3.setBackgroundImage(img);
		}

		updateTitle();

		lyrics.setTable(table);

		isUpdating = true;

		// bug: startAutoSpellCheck triggers table update
		updateLyrics();

		// updateMP3Info(table);
		// updateRaw();
		// updateCover();
		// updateBackground();
		// updateVideo();
		isUpdating = false;

		/*
		 * int new_sheet_pos = sheet.toTimeline(ms_pos);
		 * System.out.println("new_sheet_pos "+new_sheet_pos);
		 * sheet.setPlayerPosition(new_sheet_pos); ((JViewport)
		 * sheet.getParent()).setViewPosition(new
		 * Point(new_sheet_pos-(int)off,0)); sheet.repaint();
		 */
		/*
		 * int i = sheet.currentElement(); if (i < 0) { i = sheet.nextElement();
		 * } if (i >= 0) { table.setRowSelectionInterval(i, i);
		 * table.zoomPage(); Rectangle rr = table.getCellRect(i, 0, true);
		 * table.scrollRectToVisible(rr); }
		 */
	}

	Action version1 = new AbstractAction(I18.get("mpop_versions_1")) {
		private static final long serialVersionUID = -5531508932850580037L;

		public void actionPerformed(ActionEvent e) {
			gotoVersion(0);
		}
	};
	Action version2 = new AbstractAction(I18.get("mpop_versions_2")) {
		private static final long serialVersionUID = 3882950916233947823L;

		public void actionPerformed(ActionEvent e) {
			gotoVersion(1);
		}
	};
	Action version3 = new AbstractAction(I18.get("mpop_versions_3")) {
		private static final long serialVersionUID = -2495032022070456291L;

		public void actionPerformed(ActionEvent e) {
			gotoVersion(2);
		}
	};
	Action version4 = new AbstractAction(I18.get("mpop_versions_4")) {
		private static final long serialVersionUID = -4541507168393880976L;

		public void actionPerformed(ActionEvent e) {
			gotoVersion(3);
		}
	};

	private StringBuffer buf = null;

	/**
	 * Sets the outBuffer attribute of the YassActions object
	 * 
	 * @param p
	 *            The new outBuffer value
	 */
	public void setOutBuffer(StringBuffer p) {
		buf = p;
	}

	/**
	 * Gets the outBuffer attribute of the YassActions object
	 * 
	 * @return The outBuffer value
	 */
	public StringBuffer getOutBuffer() {
		return buf;
	}

	/**
	 * Gets the icon attribute of the YassActions object
	 * 
	 * @param s
	 *            Description of the Parameter
	 * @return The icon value
	 */
	public ImageIcon getIcon(String s) {
		return (ImageIcon) icons.get(s);
	}

	/**
	 * Gets the autoCorrect attribute of the YassActions object
	 * 
	 * @return The autoCorrect value
	 */
	public YassAutoCorrect getAutoCorrect() {
		return auto;
	}

	/**
	 * Gets the mP3 attribute of the YassActions object
	 * 
	 * @return The mP3 value
	 */
	public YassPlayer getMP3() {
		return mp3;
	}

	/**
	 * Gets the video attribute of the YassActions object
	 * 
	 * @return The video value
	 */
	public YassVideo getVideo() {
		return video;
	}

	/**
	 * Sets the songInfo attribute of the YassActions object
	 * 
	 * @param c
	 *            The new songInfo value
	 */
	public void setSongInfo(YassSongInfo c) {
		songInfo = c;
		songInfo.setStoreAction(saveLibrary);
		songInfo.setCopyAction(copySongInfo);
		songInfo.setCopyAction(copyCoverSongInfo, copyBackgroundSongInfo,
				copyVideoSongInfo, copyLyricsSongInfo);
		songInfo.setPasteAction(pasteSongInfo);
		songInfo.setReloadAction(undoLibraryChanges, undoAllLibraryChanges);
		copySongInfo.setEnabled(false);
		pasteSongInfo.setEnabled(false);
	}

	/**
	 * Sets the groups attribute of the YassActions object
	 * 
	 * @param g
	 *            The new groups value
	 */
	public void setGroups(YassGroups g) {
		groups = g;

		groups.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent p) {
				String pn = p.getPropertyName();
				if (pn.equals("group") || pn.equals("rule")) {
					clearFilter();
				}
			}
		});
	}

	/**
	 * Sets the generalInfo attribute of the YassActions object
	 * 
	 * @param i
	 *            The new generalInfo value
	 */
	public void setErrors(YassErrors i) {
		errors = i;
		errors.setAutoCorrect(auto);
	}

	/**
	 * Gets the cover attribute of the YassActions object
	 * 
	 * @return The cover value
	 */
	public YassErrors getErrors() {
		return errors;
	}

	/**
	 * Sets the playList attribute of the YassActions object
	 * 
	 * @param p
	 *            The new playList value
	 */
	public void setPlayList(YassPlayList p) {
		playList = p;
		playList.setStoreAction(savePlayList, savePlayListAs);
		playList.setMoveAction(movePlayListUp, movePlayListDown);
		savePlayList.setEnabled(false);
		savePlayListAs.setEnabled(false);
		removePlayList.setEnabled(false);
		refreshPlayList.setEnabled(false);
	}

	/**
	 * Gets the cover attribute of the YassActions object
	 * 
	 * @return The cover value
	 */
	public YassPlayList getPlayList() {
		return playList;
	}

	/**
	 * Sets the video attribute of the YassActions object
	 * 
	 * @param v
	 *            The new video value
	 */
	public void setVideo(YassVideo v) {
		video = v;
		// video.setStoreAction(saveVideo);
	}

	/**
	 * Sets the songList attribute of the YassActions object
	 * 
	 * @param s
	 *            The new songList value
	 */
	public void setSongList(YassSongList s) {
		songList = s;
		songList.setOpenAction(openSongFromLibrary);
		songList.setDirAction(setLibrary);
		songList.setPrintAction(printLibrary);
		songList.setAutoAction(correctLibrary);
		songList.setStoreAction(saveLibrary);
		songList.setUndoAllAction(undoAllLibraryChanges);
		setLibraryLoaded(false);
		saveLibrary.setEnabled(false);
		undoAllLibraryChanges.setEnabled(false);

		songList.addSongListListener(new YassSongListListener() {
			public void stateChanged(YassSongListEvent e) {
				int state = e.getState();
				switch (state) {
				case YassSongListEvent.LOADED:
					groups.refreshCounters();
					break;
				}
			}
		});

		// setDropTarget(songList);
	}

	/**
	 * Gets the songList attribute of the YassActions object
	 * 
	 * @return The songList value
	 */
	public YassSongList getSongList() {
		return songList;
	}

	/**
	 * Description of the Method
	 * 
	 * @param p
	 *            Description of the Parameter
	 */
	public void init(YassProperties p) {
		prop = p;

		// storeColors();
		// prop.store();
		loadColors();
		loadKeys();
		loadScreenKeys();
		loadLayout();

		String layout = prop.getProperty("editor-layout");
		if (layout == null) {
			layout = "East";
		}
		sheet.setLyricsLayout(layout);

		String hLangs = prop.getProperty("note-naming-h").toLowerCase();
		sheet.setHNoteEnabled(hLangs.contains(I18.getLanguage()));

		boolean autoTrim = prop.getProperty("auto-trim").equals("true");
		setAutoTrim(autoTrim);

		boolean useWav = prop.getProperty("use-sample").equals("true");
		mp3.useWav(useWav);

		auto = new YassAutoCorrect();
		auto.init(prop, this);
		lyrics.setAutoCorrect(auto);

		String seekInOffset = prop.getProperty("seek-in-offset");
		if (seekInOffset != null) {
			try {
				int off = Integer.parseInt(seekInOffset);
				mp3.setSeekInOffset(off);
			} catch (Exception ex) {
			}
		}
		String seekOutOffset = prop.getProperty("seek-out-offset");
		if (seekOutOffset != null) {
			try {
				int off = Integer.parseInt(seekOutOffset);
				mp3.setSeekOutOffset(off);
			} catch (Exception ex) {
			}
		}

		boolean debugWaveform = prop.getProperty("debug-waveform").equals(
				"true");
		mp3.createWaveform(debugWaveform);

		icons.put(
				"new16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/New16.gif")));
		icons.put(
				"new24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/New24.gif")));
		icons.put(
				"open16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Open16.gif")));
		icons.put(
				"open24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Open24.gif")));
		icons.put(
				"save16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Save16.gif")));
		icons.put(
				"save24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Save24.gif")));
		icons.put(
				"saveas16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/SaveAs16.gif")));
		icons.put(
				"saveas24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/SaveAs24.gif")));
		icons.put("list16Icon",
				new ImageIcon(getClass().getResource("/yass/List16.gif")));
		icons.put("list24Icon",
				new ImageIcon(getClass().getResource("/yass/List24.gif")));
		icons.put("playlist16Icon",
				new ImageIcon(getClass().getResource("/yass/Playlist16.gif")));
		icons.put("playlist24Icon",
				new ImageIcon(getClass().getResource("/yass/Playlist24.gif")));
		icons.put(
				"add16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Add16.gif")));
		icons.put(
				"add24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Add24.gif")));
		icons.put(
				"import16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Import16.gif")));
		icons.put(
				"import24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Import24.gif")));
		icons.put(
				"edit16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Edit16.gif")));
		icons.put(
				"edit24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Edit24.gif")));
		icons.put("editany24Icon",
				new ImageIcon(getClass().getResource("/yass/EditAny24.gif")));
		// icons.put("hyphen16Icon", new
		// ImageIcon(getClass().getResource("/yass/Hyphenate16.gif")));
		icons.put("hyphenate24Icon",
				new ImageIcon(getClass().getResource("/yass/Hyphenate24.gif")));
		// icons.put("check16Icon", new
		// ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Properties16.gif")));
		// icons.put("check24Icon", new
		// ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Properties24.gif")));
		icons.put("spell24Icon",
				new ImageIcon(getClass().getResource("/yass/SpellCheck24.gif")));
		icons.put(
				"refresh16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Refresh16.gif")));
		icons.put(
				"refresh24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Refresh24.gif")));

		icons.put(
				"find16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Find16.gif")));
		icons.put(
				"find24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Find24.gif")));
		icons.put("clearfind24Icon",
				new ImageIcon(getClass().getResource("/yass/ClearFind24.gif")));

		icons.put("pagebreak16Icon",
				new ImageIcon(getClass().getResource("/yass/PageBreak16.gif")));
		icons.put("pagebreak24Icon",
				new ImageIcon(getClass().getResource("/yass/PageBreak24.gif")));
		icons.put("insertnote16Icon",
				new ImageIcon(getClass().getResource("/yass/InsertNote16.gif")));
		icons.put("insertnote24Icon",
				new ImageIcon(getClass().getResource("/yass/InsertNote24.gif")));

		icons.put("correctpagebreak24Icon", new ImageIcon(getClass()
				.getResource("/yass/CorrectPageBreak24.gif")));
		icons.put("correctfilenames24Icon", new ImageIcon(getClass()
				.getResource("/yass/CorrectFileNames24.gif")));
		icons.put(
				"correcttags24Icon",
				new ImageIcon(getClass().getResource("/yass/CorrectTags24.gif")));
		icons.put(
				"correcttext24Icon",
				new ImageIcon(getClass().getResource("/yass/CorrectText24.gif")));
		icons.put("correcttransposed24Icon", new ImageIcon(getClass()
				.getResource("/yass/CorrectTransposed24.gif")));
		icons.put(
				"correctlength24Icon",
				new ImageIcon(getClass().getResource(
						"/yass/CorrectLength24.gif")));
		icons.put("correctalbum24Icon",
				new ImageIcon(getClass()
						.getResource("/yass/CorrectAlbum24.gif")));
		icons.put(
				"correctyear24Icon",
				new ImageIcon(getClass().getResource("/yass/CorrectYear24.gif")));

		icons.put(
				"print16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Print16.gif")));
		icons.put(
				"print24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Print24.gif")));
		icons.put(
				"movie16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/media/Movie16.gif")));
		icons.put(
				"movie24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/media/Movie24.gif")));
		icons.put(
				"fastforward16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/media/FastForward16.gif")));
		icons.put(
				"fastforward24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/media/FastForward24.gif")));
		icons.put(
				"rewind16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/media/Rewind16.gif")));
		icons.put(
				"rewind24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/media/Rewind24.gif")));

		icons.put("empty16Icon",
				new ImageIcon(getClass().getResource("/yass/Empty16.gif")));
		icons.put("empty24Icon",
				new ImageIcon(getClass().getResource("/yass/Empty24.gif")));

		icons.put("tiles24Icon",
				new ImageIcon(getClass().getResource("/yass/Tiles24.gif")));
		icons.put("notiles16Icon",
				new ImageIcon(getClass().getResource("/yass/NoTiles16.gif")));
		icons.put("notiles24Icon",
				new ImageIcon(getClass().getResource("/yass/NoTiles24.gif")));
		icons.put("lyrics16Icon",
				new ImageIcon(getClass().getResource("/yass/Lyrics16.gif")));
		icons.put("lyrics24Icon",
				new ImageIcon(getClass().getResource("/yass/Lyrics24.gif")));
		icons.put("noerr24Icon",
				new ImageIcon(getClass().getResource("/yass/NoError24.gif")));
		icons.put("err24Icon",
				new ImageIcon(getClass().getResource("/yass/Error24.gif")));

		icons.put("grayOpen16Icon",
				new ImageIcon(getClass().getResource("/yass/GrayOpen16.gif")));
		icons.put("grayOpen24Icon",
				new ImageIcon(getClass().getResource("/yass/GrayOpen24.gif")));

		icons.put(
				"just16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/text/AlignJustify16.gif")));
		icons.put(
				"just24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/text/AlignJustify24.gif")));
		icons.put("freestyle16Icon",
				new ImageIcon(getClass().getResource("/yass/Freestyle16.gif")));
		icons.put("freestyle24Icon",
				new ImageIcon(getClass().getResource("/yass/Freestyle24.gif")));
		icons.put("golden16Icon",
				new ImageIcon(getClass().getResource("/yass/Golden16.gif")));
		icons.put("golden24Icon",
				new ImageIcon(getClass().getResource("/yass/Golden24.gif")));
		icons.put("space16Icon",
				new ImageIcon(getClass().getResource("/yass/Space16.gif")));
		icons.put("space24Icon",
				new ImageIcon(getClass().getResource("/yass/Space24.gif")));
		icons.put("minus16Icon",
				new ImageIcon(getClass().getResource("/yass/Minus16.gif")));
		icons.put("minus24Icon",
				new ImageIcon(getClass().getResource("/yass/Minus24.gif")));
		icons.put("lockon24Icon",
				new ImageIcon(getClass().getResource("/yass/Lock24.gif")));
		icons.put("lockoff24Icon",
				new ImageIcon(getClass().getResource("/yass/Unlock24.gif")));

		icons.put(
				"home16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/navigation/Home16.gif")));
		icons.put(
				"home24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/navigation/Home24.gif")));

		icons.put("err24Icon",
				new ImageIcon(getClass().getResource("/yass/Error24.gif")));

		icons.put("setTitle24Icon",
				new ImageIcon(getClass().getResource("/yass/SetTitle24.gif")));
		icons.put("setArtist24Icon",
				new ImageIcon(getClass().getResource("/yass/SetArtist24.gif")));
		icons.put("setGenre24Icon",
				new ImageIcon(getClass().getResource("/yass/SetGenre24.gif")));
		icons.put("setEdition24Icon",
				new ImageIcon(getClass().getResource("/yass/SetEdition24.gif")));
		icons.put(
				"setLanguage24Icon",
				new ImageIcon(getClass().getResource("/yass/SetLanguage24.gif")));
		icons.put("setYear24Icon",
				new ImageIcon(getClass().getResource("/yass/SetYear24.gif")));
		icons.put("newEdition24Icon",
				new ImageIcon(getClass().getResource("/yass/NewEdition24.gif")));
		icons.put("newFolder24Icon",
				new ImageIcon(getClass().getResource("/yass/NewFolder24.gif")));
		icons.put("renameFolder24Icon",
				new ImageIcon(getClass()
						.getResource("/yass/RenameFolder24.gif")));

		icons.put(
				"play16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/media/Play16.gif")));
		icons.put(
				"play24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/media/Play24.gif")));
		icons.put(
				"stop16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/media/Stop16.gif")));
		icons.put(
				"stop24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/media/Stop24.gif")));
		playSong.putValue(AbstractAction.SMALL_ICON, getIcon("play16Icon"));
		stopPlaySong.putValue(AbstractAction.SMALL_ICON, getIcon("stop16Icon"));

		icons.put(
				"stepf16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/navigation/Down16.gif")));
		icons.put(
				"stepf24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/navigation/Down24.gif")));
		icons.put(
				"stepb16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/navigation/Up16.gif")));
		icons.put(
				"stepb24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/navigation/Up24.gif")));
		playSelection
				.putValue(AbstractAction.SMALL_ICON, getIcon("play16Icon"));
		interruptPlay
				.putValue(AbstractAction.SMALL_ICON, getIcon("stop16Icon"));
		icons.put("zoomstd16Icon",
				new ImageIcon(getClass().getResource("/yass/ZoomPage16.gif")));
		icons.put("zoomstd24Icon",
				new ImageIcon(getClass().getResource("/yass/ZoomPage24.gif")));
		icons.put(
				"zoomin16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/ZoomIn16.gif")));
		icons.put(
				"zoomin24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/ZoomIn24.gif")));
		icons.put(
				"zoomout16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/ZoomOut16.gif")));
		icons.put(
				"zoomout24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/ZoomOut24.gif")));
		icons.put("zoomall16Icon",
				new ImageIcon(getClass().getResource("/yass/ZoomAll16.gif")));
		icons.put("zoomall24Icon",
				new ImageIcon(getClass().getResource("/yass/ZoomAll24.gif")));
		onePage.putValue(AbstractAction.SMALL_ICON, getIcon("zoomstd16Icon"));
		allPages.putValue(AbstractAction.SMALL_ICON, getIcon("zoomall16Icon"));
		lessPages.putValue(AbstractAction.SMALL_ICON, getIcon("zoomout16Icon"));
		morePages.putValue(AbstractAction.SMALL_ICON, getIcon("zoomin16Icon"));

		icons.put(
				"stepl16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/navigation/Back16.gif")));
		icons.put(
				"stepl24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/navigation/Back24.gif")));
		icons.put(
				"stepr16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/navigation/Forward16.gif")));
		icons.put(
				"stepr24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/navigation/Forward24.gif")));
		addToPlayList.putValue(AbstractAction.SMALL_ICON,
				getIcon("stepr16Icon"));
		removeFromPlayList.putValue(AbstractAction.SMALL_ICON,
				getIcon("stepl16Icon"));

		icons.put("setdir24Icon",
				new ImageIcon(getClass().getResource("/yass/SetDir24.gif")));
		icons.put(
				"setdir24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Preferences24.gif")));

		icons.put(
				"copy16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Copy16.gif")));
		icons.put(
				"copy24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Copy24.gif")));
		icons.put(
				"paste16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Paste16.gif")));
		icons.put(
				"paste24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Paste24.gif")));
		icons.put(
				"insertb16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/table/RowInsertBefore16.gif")));
		icons.put(
				"insertb24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/table/RowInsertBefore24.gif")));
		icons.put(
				"inserta16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/table/RowInsertAfter16.gif")));
		icons.put(
				"inserta24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/table/RowInsertAfter24.gif")));
		icons.put("split16Icon",
				new ImageIcon(getClass().getResource("/yass/Split16.gif")));
		icons.put("split24Icon",
				new ImageIcon(getClass().getResource("/yass/Split24.gif")));
		icons.put("join16Icon",
				new ImageIcon(getClass().getResource("/yass/Join16.gif")));
		icons.put("join24Icon",
				new ImageIcon(getClass().getResource("/yass/Join24.gif")));
		icons.put(
				"delete16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Delete16.gif")));
		icons.put(
				"delete24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Delete24.gif")));
		icons.put(
				"bookmarks24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Bookmarks24.gif")));
		// icons.put("selectall16Icon", new
		// ImageIcon(getClass().getResource("/yass/SelectAll16.gif")));
		// icons.put("selectall24Icon", new
		// ImageIcon(getClass().getResource("/yass/SelectAll24.gif")));
		icons.put(
				"undo16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Undo16.gif")));
		icons.put(
				"undo24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Undo24.gif")));
		icons.put(
				"redo16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Redo16.gif")));
		icons.put(
				"redo24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Redo24.gif")));
		icons.put("snapshot24Icon",
				new ImageIcon(getClass().getResource("/yass/Snapshot24.gif")));
		copyRows.putValue(AbstractAction.SMALL_ICON, getIcon("copy16Icon"));
		pasteRows.putValue(AbstractAction.SMALL_ICON, getIcon("paste16Icon"));
		joinRows.putValue(AbstractAction.SMALL_ICON, getIcon("join16Icon"));
		splitRows.putValue(AbstractAction.SMALL_ICON, getIcon("split16Icon"));
		removeRows.putValue(AbstractAction.SMALL_ICON, getIcon("delete16Icon"));
		undo.putValue(AbstractAction.SMALL_ICON, getIcon("undo16Icon"));
		redo.putValue(AbstractAction.SMALL_ICON, getIcon("redo16Icon"));
		openSongFolder.putValue(AbstractAction.SMALL_ICON,
				getIcon("open16Icon"));
		refreshLibrary.putValue(AbstractAction.SMALL_ICON,
				getIcon("refresh16Icon"));
		saveLibrary.putValue(AbstractAction.SMALL_ICON, getIcon("save16Icon"));
		newFile.putValue(AbstractAction.SMALL_ICON, getIcon("new16Icon"));
		// importFiles.putValue(AbstractAction.SMALL_ICON,
		// getIcon("import16Icon"));
		printLibrary
				.putValue(AbstractAction.SMALL_ICON, getIcon("print16Icon"));
		openSongFromLibrary.putValue(AbstractAction.SMALL_ICON,
				getIcon("edit16Icon"));
		removeSong.putValue(AbstractAction.SMALL_ICON, getIcon("delete16Icon"));
		undoAllLibraryChanges.putValue(AbstractAction.SMALL_ICON,
				getIcon("undo16Icon"));
		// selectLine.putValue(AbstractAction.SMALL_ICON,
		// getIcon("selectall16Icon"));

		// filterLibrary.putValue(AbstractAction.SMALL_ICON,
		// getIcon("find16Icon"));

		icons.put(
				"auto16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/development/Application16.gif")));
		icons.put(
				"auto24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/development/Application24.gif")));
		icons.put(
				"pref16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Preferences16.gif")));
		icons.put(
				"pref24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Preferences24.gif")));
		showOptions.putValue(AbstractAction.SMALL_ICON, getIcon("pref16Icon"));

		icons.put(
				"playvis16Icon",
				new ImageIcon(getClass().getResource("/yass/PlayVisible16.gif")));
		icons.put(
				"playvis24Icon",
				new ImageIcon(getClass().getResource("/yass/PlayVisible24.gif")));
		icons.put("playfrozen24Icon",
				new ImageIcon(getClass().getResource("/yass/PlayFrozen24.gif")));
		playPage.putValue(AbstractAction.SMALL_ICON, getIcon("playvis16Icon"));
		icons.put("record16Icon",
				new ImageIcon(getClass().getResource("/yass/Record16.gif")));
		icons.put("record24Icon",
				new ImageIcon(getClass().getResource("/yass/Record24.gif")));
		recordSelection.putValue(AbstractAction.SMALL_ICON,
				getIcon("record16Icon"));
		icons.put("correct24Icon",
				new ImageIcon(getClass().getResource("/yass/Correct24.gif")));
		icons.put("correct16Icon",
				new ImageIcon(getClass().getResource("/yass/Correct16.gif")));
		showErrors
				.putValue(AbstractAction.SMALL_ICON, getIcon("correct16Icon"));

		icons.put("nextBreak24Icon",
				new ImageIcon(getClass().getResource("/yass/NextBreak24.gif")));
		icons.put("prevBreak24Icon",
				new ImageIcon(getClass().getResource("/yass/PrevBreak24.gif")));

		icons.put("rollLeft24Icon",
				new ImageIcon(getClass().getResource("/yass/RollLeft24.gif")));
		icons.put("rollRight24Icon",
				new ImageIcon(getClass().getResource("/yass/RollRight24.gif")));

		icons.put(
				"noalign24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/AlignBottom24.gif")));
		icons.put(
				"align24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/AlignCenter24.gif")));
		icons.put(
				"alignleft16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/text/AlignLeft16.gif")));
		icons.put(
				"alignleft24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/text/AlignLeft24.gif")));

		icons.put("midi24Icon",
				new ImageIcon(getClass().getResource("/yass/Midi24.gif")));
		icons.put("nomidi24Icon",
				new ImageIcon(getClass().getResource("/yass/NoMidi24.gif")));
		icons.put("mute24Icon",
				new ImageIcon(getClass().getResource("/yass/Mute24.gif")));
		icons.put("nomute24Icon",
				new ImageIcon(getClass().getResource("/yass/NoMute24.gif")));

		icons.put(
				"info16Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Information16.gif")));
		icons.put(
				"info24Icon",
				new ImageIcon(getClass().getResource(
						"/toolbarButtonGraphics/general/Information24.gif")));

		enableLyrics.putValue(AbstractAction.SMALL_ICON,
				getIcon("lyrics16Icon"));
		showPlaylistMenu.putValue(AbstractAction.SMALL_ICON,
				getIcon("playlist16Icon"));
		showSongInfo.putValue(AbstractAction.SMALL_ICON, getIcon("info16Icon"));
		detailLibrary.putValue(AbstractAction.SMALL_ICON,
				getIcon("notiles16Icon"));
		showSongInfoBackground.putValue(AbstractAction.SMALL_ICON,
				getIcon("empty16Icon"));

		setOpened(false);
		setSaved(true);
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JMenuBar createEditMenu() {
		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu(I18.get("medit_file"));
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		menu.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				interruptPlay();
			}
		});

		menu.add(openFile);
		menu.add(saveSong);
		menu.add(closeSong);
		menu.add(reloadSong);

		// menu.add(saveAsFile);
		// menu.add(saveAllFile);
		menu.addSeparator();
		JMenu menu2 = new JMenu(I18.get("medit_versions"));
		menu2.setMnemonic(KeyEvent.VK_V);
		menu2.add(nextVersion);
		menu2.add(prevVersion);
		menu2.addSeparator();
		menu2.add(createVersion);
		menu2.add(renameVersion);
		menu2.add(removeVersion);
		menu2.add(setAsStandard);
		// menu2.addSeparator();
		// menu2.add(mergeVersions);
		menu.add(menu2);
		menu.addSeparator();
		menu.add(gotoLibrary);
		menu.addSeparator();
		menu.add(exit);

		menu = new JMenu(I18.get("medit_edit"));
		menu.setMnemonic(KeyEvent.VK_E);
		menuBar.add(menu);
		menu.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				interruptPlay();
			}
		});
		menu.add(undo);
		menu.add(redo);
		menu.addSeparator();
		menu.add(selectLine);
		menu.add(selectAll);
		menu.addSeparator();
		menu.add(copyRows);
		menu.add(pasteRows);
		menu.add(showCopiedRows);
		menu.addSeparator();
		menu.add(togglePageBreak);
		menu.add(insertNote);
		menu.add(removeRows);
		menu.addSeparator();
		menu.add(joinRows);
		menu.add(splitRows);
		menu.addSeparator();
		menu.add(showStartEnd);

		startSpinner = new TimeSpinner(I18.get("mpop_audio_start"), 0, 10000);
		startSpinner.getSpinner().setFocusable(false);
		startSpinner.getSpinner().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!isUpdating) {
					setStart(startSpinner.getTime());
				}
			}
		});

		endSpinner = new TimeSpinner(I18.get("mpop_audio_end"), 10000, 10000);
		endSpinner.getSpinner().setFocusable(false);
		endSpinner.getSpinner().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!isUpdating) {
					setEnd(endSpinner.getTime());
				}
			}
		});

		menu.add(showLyricsStart);

		gapSpinner = new TimeSpinner(I18.get("mpop_gap"), 0, 10000);
		gapSpinner.getSpinner().setFocusable(false);
		gapSpinner.getSpinner().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!isUpdating) {
					setGap(gapSpinner.getTime());
				}
			}
		});

		menu.add(showVideoGap);

		menu = new JMenu(I18.get("medit_play"));
		menu.setMnemonic(KeyEvent.VK_P);
		menuBar.add(menu);
		menu.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				interruptPlay();
			}
		});
		menu.add(playSelection);
		menu.add(playPage);
		menu.add(playFrozen);
		menu.add(interruptPlay);
		if (YassVideoUtils.useFOBS) {
			menu.addSeparator();
			menu.add(playAll);
			menu.add(playAllFromHere);
			menu.add(playAllVideoCBI = new JCheckBoxMenuItem(enableVideoPreview));
		}
		menu.addSeparator();
		menu.add(midiCBI = new JCheckBoxMenuItem(enableMidi));
		menu.add(audioCBI = new JCheckBoxMenuItem(enableAudio));
		menu.add(clicksCBI = new JCheckBoxMenuItem(enableClicks));
		menu.add(micCBI = new JCheckBoxMenuItem(enableMic));
		audioCBI.setState(true);
		clicksCBI.setState(true);
		micCBI.setState(true);

		menu = new JMenu(I18.get("medit_view"));
		menu.setMnemonic(KeyEvent.VK_V);
		menuBar.add(menu);
		menu.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				interruptPlay();
			}
		});
		menu.add(prevPage);
		menu.add(nextPage);
		menu.addSeparator();
		menu.add(onePage);
		menu.add(lessPages);
		menu.add(morePages);
		menu.add(selectAll);
		menu.add(alignCBI = new JCheckBoxMenuItem(absolute));
		alignCBI.setState(true);

		ButtonGroup group = new ButtonGroup();
		showNothingToggle = new JRadioButtonMenuItem(showNothing);
		showBackgroundToggle = new JRadioButtonMenuItem(showBackground);
		showVideoToggle = new JRadioButtonMenuItem(showVideo);
		// menu.addSeparator();
		// menu.add(showNothingToggle = new JRadioButtonMenuItem(showNothing));
		// menu.add(showBackgroundToggle = new
		// JRadioButtonMenuItem(showBackground));
		// menu.add(showVideoToggle = new JRadioButtonMenuItem(showVideo));
		group.add(showNothingToggle);
		group.add(showBackgroundToggle);
		group.add(showVideoToggle);
		showNothingToggle.setSelected(true);

		menu.addSeparator();
		menu.add(showErrors);
		menu.add(showTable);
		menu.addSeparator();
		menu.add(fullscreen);

		menu = new JMenu(I18.get("medit_lyrics"));
		menu.setMnemonic(KeyEvent.VK_L);
		menuBar.add(menu);
		menu.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				interruptPlay();
			}
		});
		menu.add(editLyrics);
		menu.addSeparator();
		menu.add(rollLeft);
		menu.add(rollRight);
		menu.addSeparator();
		menu.add(golden);
		menu.add(freestyle);
		menu.add(minus);
		menu.add(space);

		menu.addSeparator();

		menu.add(findLyrics);
		menu.add(spellLyrics);

		menu = new JMenu(I18.get("medit_extras"));
		menu.setMnemonic(KeyEvent.VK_X);
		menuBar.add(menu);
		menu.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				interruptPlay();
			}
		});
		menu.add(recordSelection);
		menu.addSeparator();
		menu.add(autoCorrectPageBreaks);
		menu.add(autoCorrectSpacing);
		menu.add(autoCorrectTransposed);

		menu.addSeparator();
		menu.add(showOptions);

		menu = new JMenu(I18.get("medit_help"));
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);
		menu.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				interruptPlay();
			}
		});
		menu.add(showHelp);
		menu.add(showOnlineHelp);
		menu.add(showAbout);
		return menuBar;
	}

	JMenu encMenu = null;

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JMenuBar createPlayerMenu() {
		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu(I18.get("mlib_file"));
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		menu.add(pausePlayer);
		menu.add(forwardPlayer);
		menu.add(ratio43);
		menu.addSeparator();
		menu.add(gotoLibraryFromPlayer);
		menu.add(exit);
		menu.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				pausePosition = -1;
				pausePlayer();
			}
		});

		menu = new JMenu(I18.get("mlib_help"));
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);
		menu.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				pausePosition = -1;
				pausePlayer();
			}
		});
		menu.add(showHelp);
		menu.add(showOnlineHelp);
		menu.add(showAbout);

		return menuBar;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JMenuBar createScreenMenu() {
		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu(I18.get("mlib_file"));
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		menu.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (YassScreen.getCurrentScreen().getID().equals("playsong")) {
					pausePosition = -1;
					pausePlayer();
				}
			}
		});

		menu.add(firstScreen);
		menu.add(loadScreenSongs);
		menu.add(gotoLibraryFromScreen);
		menu.addSeparator();
		menu.add(exit);
		menu = new JMenu(I18.get("mlib_help"));
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);
		menu.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (YassScreen.getCurrentScreen().getID().equals("playsong")) {
					pausePosition = -1;
					pausePlayer();
				}
			}
		});
		menu.add(showHelp);
		menu.add(showOnlineHelp);
		menu.add(showAbout);

		return menuBar;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JMenuBar createPlayerScreenMenu() {
		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu(I18.get("mlib_file"));
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		menu.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (YassScreen.getCurrentScreen().getID().equals("playsong")) {
					pausePosition = -1;
					pausePlayer();
				}
			}
		});

		menu.add(pausePlayer);
		menu.add(forwardPlayer);
		menu.add(ratio43);
		menu.addSeparator();
		menu.add(firstScreen);
		menu.add(exit);
		menu = new JMenu(I18.get("mlib_help"));
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);
		menu.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (YassScreen.getCurrentScreen().getID().equals("playsong")) {
					pausePosition = -1;
					pausePlayer();
				}
			}
		});
		menu.add(showHelp);
		menu.add(showOnlineHelp);
		menu.add(showAbout);

		return menuBar;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JMenuBar createLibraryMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu(I18.get("mlib_file"));
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		if (!YassMain.NO_GAME) {
			menu.add(gotoPlayer);
			menu.add(gotoPlayerSelectedSong);
			// menu.add(gotoPlayerDemo);
			// menu.add(gotoScore);
			menu.addSeparator();
		}
		menu.add(openSongFromLibrary);
		menu.add(openFile);
		menu.add(editRecent);
		menu.addSeparator();
		menu.add(newFile);
		// menu.add(importFiles);
		menu.add(printLibrary);
		menu.add(exportMetadata);
		menu.add(importMetadata);
		// menu.add(importText);
		menu.addSeparator();
		menu.add(setLibrary);
		menu.add(refreshLibrary);
		menu.add(thumbnailLibrary);
		menu.add(closeLibrary);
		menu.addSeparator();
		menu.add(exit);

		menu = new JMenu(I18.get("mlib_edit"));
		menu.setMnemonic(KeyEvent.VK_E);
		menuBar.add(menu);
		menu.add(filterLibrary);
		menu.addSeparator();
		menu.add(selectAllSongs);
		menu.addSeparator();

		JMenu menu2 = new JMenu(I18.get("mlib_set"));
		menu2.add(setArtist);
		menu2.add(setTitle);
		menu2.add(songList.getGenreMenu());
		menu2.add(songList.getEditionMenu());
		menu2.add(songList.getLanguageMenu());
		menu2.add(setYear);
		menu2.add(setAlbum);
		menu2.add(setLength);
		menu2.add(setID);
		menu.add(menu2);
		menu.add(setPreviewStart);
		menu.add(setMedleyStartEnd);

		encMenu = new JMenu(I18.get("mlib_encoding"));
		encMenu.add(setEncodingUTF8);
		encMenu.add(setEncodingANSI);
		menu.add(encMenu);

		menu.addSeparator();
		menu2 = new JMenu(I18.get("mlib_copy"));
		menu2.add(copyLyricsSongInfo);
		menu2.add(copyCoverSongInfo);
		menu2.add(copyBackgroundSongInfo);
		menu2.add(copyVideoSongInfo);
		menu.add(menu2);
		menu.add(pasteSongInfo);
		menu.addSeparator();
		menu.add(saveLibrary);
		menu.add(undoAllLibraryChanges);
		menu.addSeparator();
		menu.add(removeSong);

		menu = new JMenu(I18.get("mlib_view"));
		menu.setMnemonic(KeyEvent.VK_V);
		menuBar.add(menu);
		menu.add(showPlaylistMenu);
		menu.add(enableLyrics);
		menu.add(showSongInfo);
		menu.add(detailLibrary);
		menu.add(showSongInfoBackground);
		menu.add(songList.getSortByMenu());
		menu.addSeparator();
		menu.add(openSongFolder);
		menu.add(showLibTable);
		menu.addSeparator();
		menu.add(fullscreen);

		menu = new JMenu(I18.get("mlib_play"));
		menu.setMnemonic(KeyEvent.VK_S);
		menuBar.add(menu);
		menu.add(playSong);
		menu.add(stopPlaySong);
		menu.addSeparator();
		menu.add(addToPlayList);
		menu.add(removeFromPlayList);
		menu.addSeparator();
		menu.add(savePlayList);
		menu.add(savePlayListAs);
		menu.add(refreshPlayList);
		menu.add(removePlayList);

		menu = new JMenu(I18.get("mlib_extras"));
		menu.setMnemonic(KeyEvent.VK_X);
		menu.add(splitSong);
		menu.add(mergeSongs);
		menu.addSeparator();
		menu.add(testMic);
		menu.addSeparator();
		menu.add(showOptions);
		menuBar.add(menu);

		menu = new JMenu(I18.get("mlib_help"));
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);
		menu.add(showHelp);
		menu.add(showOnlineHelp);
		menu.add(showAbout);
		return menuBar;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createPlaybackToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_playback"));
		t.setOrientation(SwingConstants.HORIZONTAL);

		boolean floatable = prop.getProperty("floatable").equals("true");
		t.setFloatable(floatable);

		JButton b = null;

		/*
		 * t.add(b = new JButton()); b.setAction(prevPage);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("stepb24Icon")); b.setFocusable(false);
		 * b.setAlignmentY(b.CENTER_ALIGNMENT);
		 * b.setAlignmentX(b.LEFT_ALIGNMENT); t.add(b = new JButton());
		 * b.setAction(nextPage); b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("stepf24Icon")); b.setFocusable(false);
		 * b.setAlignmentY(b.CENTER_ALIGNMENT);
		 * b.setAlignmentX(b.LEFT_ALIGNMENT); t.addSeparator();
		 */
		/*
		 * t.add(b = new JButton()); b.setAction(playSelection);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("play24Icon")); b.setFocusable(false); t.add(b =
		 * new JButton()); b.setAction(playPage); b.setToolTipText(b.getText());
		 * b.setText(""); b.setIcon(getIcon("playvis24Icon"));
		 * b.setFocusable(false);
		 */
		/*
		 * t.add(b = new JButton()); b.setAction(interruptPlay);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("stop24Icon")); b.setFocusable(false);
		 * 
		 * t.addSeparator(); t.add(b = new JButton());
		 * b.setAction(recordSelection); b.setToolTipText(b.getText());
		 * b.setText(""); b.setIcon(getIcon("record24Icon"));
		 * b.setFocusable(false);
		 */
		t.add(Box.createHorizontalGlue());
		t.add(Box.createVerticalGlue());

		t.add(b = new JButton());
		b.setAction(morePages);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("zoomin24Icon"));
		b.setFocusable(false);

		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createFileEditToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_edit"));
		edittools = t;

		boolean floatable = prop.getProperty("floatable").equals("true");
		t.setFloatable(floatable);

		JButton b;

		t.add(b = new JButton());
		b.setAction(gotoLibrary);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("list24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(saveSong);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("save24Icon"));
		b.setFocusable(false);

		t.addSeparator();

		t.add(midiButton = new JToggleButton());
		midiButton.setAction(enableMidi);
		midiButton.setToolTipText(midiButton.getText());
		midiButton.setText("");
		midiButton.setIcon(getIcon("nomidi24Icon"));
		midiButton.setSelectedIcon(getIcon("midi24Icon"));
		midiButton.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(showErrors);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("correct24Icon"));
		b.setFocusable(false);
		hasErrorsButton = (JButton) b;

		mp3Button = new JToggleButton();
		// t.add(...);
		mp3Button.setAction(enableAudio);
		mp3Button.setToolTipText(mp3Button.getText());
		mp3Button.setText("");
		mp3Button.setIcon(getIcon("mute24Icon"));
		mp3Button.setSelectedIcon(getIcon("nomute24Icon"));
		mp3Button.setFocusable(false);
		mp3Button.setSelected(true);

		videoButton = new JToggleButton();
		// t.add(..);
		videoButton.setAction(toggleVideo);
		videoButton.setToolTipText(videoButton.getText());
		videoButton.setText("");
		videoButton.setIcon(getIcon("movie24Icon"));
		// videoButton.setSelectedIcon(getIcon("movie24Icon"));
		videoButton.setFocusable(false);

		t.addSeparator();

		t.add(b = new JButton());
		b.setAction(undo);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("undo24Icon"));

		b.setFocusable(false);
		t.add(b = new JButton());
		b.setAction(redo);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("redo24Icon"));
		b.setFocusable(false);

		t.addSeparator();

		/*
		 * t.add(b = new JButton()); b.setAction(selectLine);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("selectall24Icon")); b.setFocusable(false);
		 */
		t.add(b = new JButton());
		b.setAction(copyRows);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("copy24Icon"));
		b.setFocusable(false);
		t.add(b = new JButton());
		b.setAction(pasteRows);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("paste24Icon"));
		b.setFocusable(false);

		t.add(snapshotButton = new JToggleButton());
		snapshotButton.setAction(showCopiedRows);
		snapshotButton.setToolTipText(snapshotButton.getText());
		snapshotButton.setText("");
		snapshotButton.setIcon(getIcon("snapshot24Icon"));
		snapshotButton.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(playFrozen);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("playfrozen24Icon"));
		b.setFocusable(false);

		t.addSeparator();

		t.add(b = new JButton());
		b.setAction(insertNote);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("insertnote24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(splitRows);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("split24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(joinRows);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("join24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(removeRows);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("delete24Icon"));
		b.setFocusable(false);

		t.addSeparator();

		t.add(b = new JButton());
		b.setAction(rollLeft);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("rollLeft24Icon"));
		b.setFocusable(false);
		t.add(b = new JButton());
		b.setAction(rollRight);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("rollRight24Icon"));
		b.setFocusable(false);

		t.addSeparator();

		t.add(b = new JButton());
		b.setAction(togglePageBreak);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("pagebreak24Icon"));
		b.setFocusable(false);

		// t.add(Box.createHorizontalGlue());
		// t.add(createVersionBox());
		createVersionBox();

		/*
		 * t.add(b = new JButton()); b.setAction(space);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("space24Icon")); b.setFocusable(false); t.add(b =
		 * new JButton()); b.setAction(minus); b.setToolTipText(b.getText());
		 * b.setText(""); b.setIcon(getIcon("minus24Icon"));
		 * b.setFocusable(false);
		 */
		t.addSeparator();

		t.add(b = new JButton());
		b.setAction(golden);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("golden24Icon"));
		b.setFocusable(false);
		t.add(b = new JButton());
		b.setAction(freestyle);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("freestyle24Icon"));
		b.setFocusable(false);

		t.addSeparator();

		t.add(b = new JButton());
		b.setAction(editLyrics);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("edit24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(onePage);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("zoomstd24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(morePages);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("zoomin24Icon"));
		b.setFocusable(false);

		return t;
	}

	Action homeSong = new AbstractAction(I18.get("mlib_home")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songList.gotoSong(table);
		}
	};

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createListTransferToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_playlist_transfer"));
		t.setFloatable(false);
		t.setOrientation(SwingConstants.VERTICAL);

		t.add(Box.createVerticalGlue());

		JButton b = null;

		t.add(b = new JButton());
		b.setAction(addToPlayList);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("stepr24Icon"));
		b.setFocusable(false);

		t.addSeparator();

		t.add(b = new JButton());
		b.setAction(removeFromPlayList);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("stepl24Icon"));
		b.setFocusable(false);

		t.addSeparator();
		t.addSeparator();

		t.add(b = new JButton());
		b.setAction(movePlayListUp);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("stepb24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(movePlayListDown);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("stepf24Icon"));
		b.setFocusable(false);

		t.add(Box.createVerticalGlue());

		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createPlayListToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_playlist"));
		t.setFloatable(false);

		JButton b = null;

		t.add(b = new JButton());
		b.setAction(savePlayList);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("save24Icon"));
		b.setFocusable(false);

		/*
		 * t.add(b = new JButton()); b.setAction(savePlayListAs);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("saveas24Icon")); b.setFocusable(false); t.add(b =
		 * new JButton()); b.setAction(refreshPlayList);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("refresh24Icon")); b.setFocusable(false);
		 * t.addSeparator(); t.add(b = new JButton());
		 * b.setAction(removePlayList); b.setToolTipText(b.getText());
		 * b.setText(""); b.setIcon(getIcon("delete24Icon"));
		 * b.setFocusable(false); t.addSeparator();
		 */
		t.add(createPlayListBox());
		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComboBox<String> createPlayListBox() {
		plBox = new JComboBox<>();
		plBox.setToolTipText(I18.get("tool_playlist_box"));
		plBoxTips = new Vector<>();

		plBox.addActionListener(plBoxListener = new PlayListBoxListener());
		plBox.setRenderer(new PlayListBoxRenderer());
		updatePlayListBox();
		return plBox;
	}

	/**
	 * Gets the playListBox attribute of the YassActions object
	 * 
	 * @return The playListBox value
	 */
	public JComboBox<String> getPlayListBox() {
		return plBox;
	}

	Vector<String> plBoxTips = null;

	/**
	 * Description of the Class
	 * 
	 * @author Saruta
	 * @created 11. Oktober 2007
	 */
	class PlayListBoxRenderer extends BasicComboBoxRenderer {
		private static final long serialVersionUID = 1L;

		/**
		 * Gets the listCellRendererComponent attribute of the
		 * PlayListBoxRenderer object
		 * 
		 * @param list
		 *            Description of the Parameter
		 * @param value
		 *            Description of the Parameter
		 * @param index
		 *            Description of the Parameter
		 * @param isSelected
		 *            Description of the Parameter
		 * @param cellHasFocus
		 *            Description of the Parameter
		 * @return The listCellRendererComponent value
		 */
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
				if (-1 < index) {
					list.setToolTipText((String) plBoxTips.elementAt(index));
				}
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setHorizontalAlignment(SwingConstants.CENTER);
			setFont(list.getFont());
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}

	PlayListBoxListener plBoxListener = null;

	/**
	 * Description of the Class
	 * 
	 * @author Saruta
	 * @created 28. September 2007
	 */
	class PlayListBoxListener implements ActionListener {
		/**
		 * Description of the Method
		 * 
		 * @param e
		 *            Description of the Parameter
		 */
		public void actionPerformed(ActionEvent e) {
			if (playList == null) {
				return;
			}

			int n = plBox.getSelectedIndex();
			if (n == 0) {
				playList.setName(I18.get("tool_playlist_new"));
				playList.removeAllSongs();
			} else {
				playList.setPlayList(n - 1);
			}
			removePlayList.setEnabled(n != 0);
		}
	}

	/**
	 * Description of the Method
	 */
	public void updatePlayListBox() {
		plBox.removeActionListener(plBoxListener);

		plBox.removeAllItems();
		plBoxTips.clear();

		String pdir = prop.getProperty("playlist-directory");
		plBoxTips.addElement(pdir);
		plBox.addItem(I18.get("tool_playlist_new"));

		Vector<?> v = playList.getPlayLists();
		if (v == null) {
			return;
		}

		String rec = prop.getProperty("recent-playlist");
		if (rec == null) {
			rec = "";
		}

		int k = -1;

		int i = 0;
		for (Enumeration<?> en = v.elements(); en.hasMoreElements();) {
			YassPlayListModel pl = (YassPlayListModel) en.nextElement();
			String plt = pl.getFileName();
			if (plt == null) {
				plt = pdir;
			}
			plBoxTips.addElement(plt);
			plBox.addItem(pl.getName());
			if (pl.getName().equals(rec)) {
				k = i;
			}
			i++;
		}

		plBox.setSelectedIndex(k + 1);
		plBox.addActionListener(plBoxListener);
		plBox.validate();

		removePlayList.setEnabled(k >= 0);
		refreshPlayList.setEnabled(k >= 0);
		savePlayList.setEnabled(k >= 0);
		savePlayListAs.setEnabled(k >= 0);

		storePlayListCache();
		refreshGroups();
		updatePlayListCursor();
	}

	/**
	 * Description of the Method
	 */
	public synchronized void storePlayListCache() {
		Vector<?> pldata = playList.getPlayLists();
		if (pldata != null) {
			String plcacheName = prop.getProperty("playlist-cache");
			File plcache = new File(plcacheName);
			if (plcache.exists()) {
				if (!plcache.delete()) {
					System.out.println("Error: Cannot delete playlist cache.");
				}
			}
			File pp = plcache.getParentFile();
			if (pp != null) {
				pp.mkdirs();
			}

			PrintWriter outputStream = null;
			FileWriter fw = null;
			try {
				outputStream = new PrintWriter(fw = new FileWriter(plcache));
				for (Enumeration<?> en = pldata.elements(); en
						.hasMoreElements();) {
					YassPlayListModel pl = (YassPlayListModel) en.nextElement();
					String plt = pl.getFileName();
					outputStream.println(plt);
				}
			} catch (Exception e) {
				System.out.println("Playlist Cache Write Error:"
						+ e.getMessage());
				e.printStackTrace();
			} finally {
				try {
					if (fw != null) {
						fw.close();
					}
					if (outputStream != null) {
						outputStream.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Description of the Method
	 */
	public void updatePlayListCursor() {
		int n = playList.getList().getRowCount();
		removeFromPlayList.setEnabled(n > 0);

		movePlayListUp.setEnabled(n > 1);
		movePlayListDown.setEnabled(n > 1);
	}

	private JComboBox<String> plBox = null, filter = null;
	private JTextField filterEditor = null;

	/**
	 * Description of the Method
	 */
	public void clearFilter() {
		filterEditor.setText(I18.get("tool_lib_find_empty"));
		filterEditor.setForeground(Color.gray);
		songInfo.setBold(null);
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createFilterBox() {
		JToolBar t = new JToolBar(I18.get("tool_lib_find"));
		t.setFloatable(false);
		t.setOrientation(SwingConstants.VERTICAL);

		filter = new JComboBox<>();
		filter.setToolTipText(I18.get("tool_lib_find_box"));
		filter.setEditable(true);
		filter.setEditable(true);
		filter.addItem(I18.get("tool_lib_find_all"));
		filter.addItem("[SC]");
		filter.addItem(I18.get("tool_lib_find_not") + " [SC]");
		filter.addItem("[VIDEO]");
		filter.addItem(I18.get("tool_lib_find_not") + " [VIDEO]");

		filterEditor = new JTextField(10);
		// filterEditor.setMinimumSize(new Dimension(60, 36));
		// filterEditor.setPreferredSize(new Dimension(60, 36));
		// filterEditor.setMaximumSize(new Dimension(60, 36));
		filterEditor.setText(I18.get("tool_lib_find_empty"));
		filterEditor.setForeground(Color.gray);

		filterAll = new JToggleButton();
		filterAll.setAction(enableLyrics);
		filterAll.setToolTipText(filterAll.getText());
		filterAll.setText("");
		filterAll.setIcon(getIcon("lyrics24Icon"));
		filterAll.setFocusable(false);
		filterAll.setSelected(true);

		// JTextComponent editor = (JTextComponent)
		// filter.getEditor().getEditorComponent();
		filterEditor.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				String s = filterEditor.getText();
				if (s.equals(I18.get("tool_lib_find_empty"))) {
					filterEditor.setText("");
				}
				filterEditor.setForeground(Color.black);
				filterEditor.selectAll();
			}

			public void focusLost(FocusEvent e) {
				String s = filterEditor.getText();
				if (s.equals("")) {
					filterEditor.setText(I18.get("tool_lib_find_empty"));
				}
				filterEditor.setForeground(Color.gray);
			}
		});
		filterEditor.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				keyTyped(e);
			}

			public void keyReleased(KeyEvent e) {
				keyTyped(e);
			}

			public void keyTyped(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String s = filterEditor.getText();
					if (s.equals(I18.get("tool_lib_find_empty"))) {
						s = "";
					}
					filterEditor.setForeground(Color.gray);
					songList.filterLyrics(filterAll.isSelected());
					songInfo.setBold(s);
					songList.filter(s);
					return;
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					clearFilter();
					filterLibrary();
					return;
				}
				String s = filterEditor.getText();
				if (s.equals(I18.get("tool_lib_find_empty"))) {
					s = "";
					filterEditor.setForeground(Color.gray);
				} else {
					filterEditor.setForeground(Color.black);
				}

				if (s != null && s.length() > 2) {
					songList.filterLyrics(filterAll.isSelected());
					songInfo.setBold(s);
					songList.filter(s);
				}
			}
		});
		filter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = filterEditor.getText();
				if (s.equals(I18.get("tool_lib_find_empty"))) {
					s = "";
				}
				songList.filterLyrics(filterAll.isSelected());
				songInfo.setBold(s);
				songList.filter(s);
			}
		});
		return t;
	}

	/**
	 * Gets the filterEditing attribute of the YassSongList object
	 * 
	 * @return The filterEditing value
	 */
	public boolean isFilterEditing() {
		// JTextComponent c = (JTextComponent)
		// filter.getEditor().getEditorComponent();
		return filterEditor.hasFocus();
		// return c.hasFocus();
	}

	Font groupsFont = new Font("SansSerif", Font.BOLD, 14);

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComboBox<String> createGroupsBox() {
		groupsBox = new JComboBox<>(yass.filter.YassFilter.getAllLabels());
		groupsBox
				.setMaximumRowCount(Math.min(20, groupsBox.getItemCount() + 1));
		groupsBox.setEditable(false);
		groupsBox.setRenderer(new AlignedListCellRenderer());
		groupsBox.setFont(groupsFont);
		groupsBox.setForeground(Color.gray);
		return groupsBox;
	}

	class AlignedListCellRenderer extends JLabel implements
			ListCellRenderer<Object> {
		private static final long serialVersionUID = 1L;
		Dimension dim = new Dimension(100, 20);

		/**
		 * Constructor for the ComboBoxRenderer object
		 */
		public AlignedListCellRenderer() {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}

		public Dimension getPreferredSize() {
			return dim;
		}

		Color selcol = UIManager.getColor("Table.selectionBackground");
		Color bgcol = UIManager.getColor("Label.background");

		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			String txt = (String) value;
			setForeground(Color.gray);
			setBackground(isSelected ? selcol : bgcol);
			setFont(groupsFont);
			setText(txt);
			return this;
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createTitleToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_title"));
		t.setFloatable(false);

		/*
		 * t.add(b = new JButton()); b.setAction(setTitle);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("setTitle24Icon")); b.setFocusable(false); t.add(b
		 * = new JButton()); b.setAction(setArtist);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("setArtist24Icon")); b.setFocusable(false);
		 */
		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createArtistToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_artist"));
		t.setFloatable(false);

		/*
		 * t.add(b = new JButton()); b.setAction(setTitle);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("setTitle24Icon")); b.setFocusable(false); t.add(b
		 * = new JButton()); b.setAction(setArtist);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("setArtist24Icon")); b.setFocusable(false);
		 */
		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createGenreToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_genre"));
		t.setFloatable(false);

		/*
		 * t.add(b = new JButton()); b.setAction(setTitle);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("setTitle24Icon")); b.setFocusable(false); t.add(b
		 * = new JButton()); b.setAction(setArtist);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("setArtist24Icon")); b.setFocusable(false);
		 * t.add(songList.getGenreMenu()); t.add(Box.createHorizontalGlue());
		 */
		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createEditionToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_edition"));
		t.setFloatable(false);

		JButton b;

		/*
		 * t.add(b = new JButton()); b.setAction(setTitle);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("setTitle24Icon")); b.setFocusable(false); t.add(b
		 * = new JButton()); b.setAction(setArtist);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("setArtist24Icon")); b.setFocusable(false);
		 */
		t.add(b = new JButton());
		b.setAction(newEdition);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("newEdition24Icon"));
		b.setFocusable(false);

		// t.add(songList.getEditionMenu());
		// t.add(Box.createHorizontalGlue());

		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createYearToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_year"));
		t.setFloatable(false);

		JButton b;
		t.add(b = new JButton());
		b.setAction(correctYear);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("correctyear24Icon"));
		b.setFocusable(false);
		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createLengthToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_length"));
		t.setFloatable(false);

		JButton b;
		t.add(b = new JButton());
		b.setAction(correctLength);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("correctlength24Icon"));
		b.setFocusable(false);
		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createAlbumToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_album"));
		t.setFloatable(false);

		JButton b;
		t.add(b = new JButton());
		b.setAction(correctAlbum);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("correctalbum24Icon"));
		b.setFocusable(false);
		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createPlaylistToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_playlist"));
		t.setFloatable(false);

		JButton b;
		t.add(b = new JButton());
		b.setAction(savePlayList);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("save24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(savePlayListAs);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("saveas24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(refreshPlayList);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("refresh24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(removePlayList);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("delete24Icon"));
		b.setFocusable(false);

		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createDuetsToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_duets"));
		t.setFloatable(false);
		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createLanguageToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_language"));
		t.setFloatable(false);

		/*
		 * t.add(b = new JButton()); b.setAction(setTitle);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("setTitle24Icon")); b.setFocusable(false); t.add(b
		 * = new JButton()); b.setAction(setArtist);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("setArtist24Icon")); b.setFocusable(false);
		 * t.add(songList.getLanguageMenu()); t.add(Box.createHorizontalGlue());
		 */
		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createFolderToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_folder"));
		t.setFloatable(false);

		JButton b;

		/*
		 * t.add(b = new JButton()); b.setAction(setTitle);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("setTitle24Icon")); b.setFocusable(false); t.add(b
		 * = new JButton()); b.setAction(setArtist);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("setArtist24Icon")); b.setFocusable(false);
		 */
		t.add(b = new JButton());
		b.setAction(newFolder);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("newFolder24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(renameFolder);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("renameFolder24Icon"));
		b.setFocusable(false);

		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createErrorToolbar() {
		JToolBar t = new JToolBar(I18.get("mpop_errors"));
		t.setFloatable(false);
		// t.setOrientation(t.VERTICAL);

		JButton b;

		// t.add(Box.createHorizontalGlue());

		t.add(b = new JButton());
		b.setAction(autoCorrectTransposed);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("correcttransposed24Icon"));
		b.setFocusable(false);
		correctTransposedButton = (JButton) b;

		t.add(b = new JButton());
		b.setAction(autoCorrectPageBreaks);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("correctpagebreak24Icon"));
		b.setFocusable(false);
		correctPageBreakButton = (JButton) b;

		t.add(b = new JButton());
		b.setAction(autoCorrectSpacing);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("correcttext24Icon"));
		b.setFocusable(false);
		correctSpacingButton = (JButton) b;

		return t;
	}

	private JComponent libtools = null;
	private JComponent edittools = null;

	/**
	 * Sets the libTools attribute of the YassActions object
	 * 
	 * @param c
	 *            The new libTools value
	 */
	public void setLibTools(JComponent c) {
		libtools = c;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createSongListToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_lib"));
		// boolean floatable = prop.getProperty("floatable").equals("true");
		t.setFloatable(false);

		JButton b;

		t.add(b = new JButton());
		b.setAction(saveLibrary);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("save24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(undoAllLibraryChanges);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("undo24Icon"));
		b.setFocusable(false);

		t.addSeparator();

		t.add(playToggle = new JButton());
		playToggle.setAction(togglePlaySong);
		playToggle.setToolTipText(playToggle.getText());
		playToggle.setText("");
		playToggle.setIcon(getIcon("play24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(openSongFromLibrary);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("edit24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(openSongFolder);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("open24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(removeSong);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("delete24Icon"));
		b.setFocusable(false);

		t.addSeparator();

		t.add(progressBar);

		t.addSeparator();

		t.add(b = new JButton());
		b.setAction(refreshLibrary);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("refresh24Icon"));
		b.setFocusable(false);

		t.addSeparator();

		/*
		 * t.add(b = new JButton()); b.setAction(newFile);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("new24Icon")); b.setFocusable(false); t.add(b = new
		 * JButton()); b.setAction(importFiles); b.setToolTipText(b.getText());
		 * b.setText(""); b.setIcon(getIcon("import24Icon"));
		 * b.setFocusable(false); t.add(b = new JButton());
		 * b.setAction(printLibrary); b.setToolTipText(b.getText());
		 * b.setText(""); b.setIcon(getIcon("print24Icon"));
		 * b.setFocusable(false);
		 */
		t.addSeparator();

		t.add(playlistToggle = new JToggleButton());
		playlistToggle.setAction(showPlaylist);
		playlistToggle.setToolTipText(playlistToggle.getText());
		playlistToggle.setText("");
		playlistToggle.setIcon(getIcon("playlist24Icon"));
		// detailToggle.setSelectedIcon(getIcon("tiles24Icon"));
		playlistToggle.setFocusable(false);

		t.addSeparator();

		t.add(filterAll);

		t.add(songInfoToggle = new JToggleButton());
		songInfoToggle.setAction(showSongInfo);
		songInfoToggle.setToolTipText(songInfoToggle.getText());
		songInfoToggle.setText("");
		songInfoToggle.setIcon(getIcon("info24Icon"));
		songInfoToggle.setFocusable(false);

		t.add(detailToggle = new JToggleButton());
		detailToggle.setAction(detailLibrary);
		detailToggle.setToolTipText(detailToggle.getText());
		detailToggle.setText("");
		detailToggle.setIcon(getIcon("notiles24Icon"));
		// detailToggle.setSelectedIcon(getIcon("tiles24Icon"));
		detailToggle.setFocusable(false);

		t.add(bgToggle = new JToggleButton());
		bgToggle.setAction(showSongInfoBackground);
		bgToggle.setToolTipText(bgToggle.getText());
		bgToggle.setText("");
		bgToggle.setIcon(getIcon("empty24Icon"));
		// detailToggle.setSelectedIcon(getIcon("tiles24Icon"));
		bgToggle.setFocusable(false);

		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createFilterToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_lib"));
		t.setFloatable(false);

		JButton b;
		t.add(Box.createHorizontalGlue());

		t.add(filterEditor);
		t.add(b = new JButton());
		b.setAction(clearFilter);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("clearfind24Icon"));
		b.setFocusable(false);

		/*
		 * t.add(b = new JButton()); b.setAction(setLibrary);
		 * b.setToolTipText(b.getText()); b.setText("");
		 * b.setIcon(getIcon("setdir24Icon")); b.setFocusable(false);
		 */
		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createErrorsToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_errors"));
		t.setFloatable(false);

		JButton b;
		/*
		 * t.add(errToggle = new JToggleButton());
		 * errToggle.setAction(errLibrary);
		 * errToggle.setToolTipText(errToggle.getText()); errToggle.setText("");
		 * errToggle.setIcon(getIcon("noerr24Icon"));
		 * errToggle.setSelectedIcon(getIcon("err24Icon"));
		 * errToggle.setFocusable(false);
		 */
		t.add(b = new JButton());
		b.setAction(correctTagsLibrary);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("correcttags24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(correctFilesLibrary);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("correctfilenames24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(correctPageBreakLibrary);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("correctpagebreak24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(correctTextLibrary);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("correcttext24Icon"));
		b.setFocusable(false);

		t.addSeparator();

		t.add(b = new JButton());
		b.setAction(correctLibrary);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("err24Icon"));
		b.setFocusable(false);

		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createFilesToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_files"));
		t.setFloatable(false);

		JButton b;

		t.add(b = new JButton());
		b.setAction(renameFilesLibrary);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("correctfilenames24Icon"));
		b.setFocusable(false);

		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createSongSearchToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_lib_standard"));
		t.setFloatable(false);

		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createSongInfoToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_lib_info"));
		t.setFloatable(false);
		t.setOrientation(SwingConstants.VERTICAL);

		JButton b = null;

		t.add(b = new JButton());
		b.setAction(copySongInfo);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("copy24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(pasteSongInfo);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("paste24Icon"));
		b.setFocusable(false);

		t.add(Box.createVerticalGlue());

		return t;
	}

	/**
	 * Sets the progress attribute of the YassActions object
	 * 
	 * @param s
	 *            The new progress value
	 */
	public void setProgress(String s) {
		progressBar.setString(s);
		progressBar.setValue(0);

		progressBar.setToolTipText("");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.repaint();
			}
		});
		if (currentView == VIEW_SCREEN) {
			YassScreen.getCurrentScreen();
			YassScreen.setLoadingMessage(progressBar.getString());
		}
	}

	/**
	 * Sets the progress attribute of the YassActions object
	 * 
	 * @param s
	 *            The new progress value
	 * @param n
	 *            The new progress value
	 */
	public void setProgress(String s, int n) {
		progressBar.setString(s);
		progressBar.setValue(0);
		progressBar.setMaximum(n);

		progressBar.setToolTipText("");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.repaint();
			}
		});

		if (currentView == VIEW_SCREEN) {
			YassScreen.getCurrentScreen();
			YassScreen.setLoadingMessage(progressBar.getString());
		}
	}

	/**
	 * Sets the progress attribute of the YassActions object
	 * 
	 * @param s
	 *            The new progress value
	 * @param t
	 *            The new progress value
	 */
	public void setProgress(String s, String t) {
		progressBar.setString(s);
		progressBar.setToolTipText(t);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.repaint();
			}
		});
		if (currentView == VIEW_SCREEN) {
			YassScreen.getCurrentScreen();
			YassScreen.setLoadingMessage(progressBar.getString());
		}
	}

	/**
	 * Sets the progress attribute of the YassActions object
	 * 
	 * @param n
	 *            The new progress value
	 */
	public void setProgress(int n) {
		progressBar.setValue(n);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.repaint();
			}
		});
		if (currentView == VIEW_SCREEN) {
			YassScreen.getCurrentScreen();
			YassScreen.setLoadingMessage(progressBar.getString());
		}
	}

	Action openSongFromLibrary = new AbstractAction(I18.get("mlib_edit_songs")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			openSelectedSongs();
		}
	};
	Action mergeSongs = new AbstractAction(I18.get("mlib_tracks_merge")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			mergeSelectedSongs();
		}
	};
	Action splitSong = new AbstractAction(I18.get("mlib_tracks_split")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			splitSelectedSong();
		}
	};

	Action testMic = new AbstractAction(I18.get("mlib_test_mic")) {
		private static final long serialVersionUID = 623692528158639654L;

		@Override
		public void actionPerformed(ActionEvent e) {
			YassCaptureAudio cap = new YassCaptureAudio();
			cap.createGUI();
			cap.startCapture(getProperties().getProperty("control-mic"));
		}
	};

	/**
	 * Description of the Method
	 */
	public void openSelectedSongs() {
		Vector<String> fn = songList.getSelectedFiles();
		if (fn == null) {
			return;
		}

		for (Enumeration<String> en = fn.elements(); en.hasMoreElements();) {
			String filename = (String) en.nextElement();
			File file = new File(filename);
			if (!file.exists()) {
				continue;
			}
			if (!isKaraokeFile(file)) {
				continue;
			}

			YassTable mt = new YassTable();
			mt.loadFile(file.getAbsolutePath());
			int multi = mt.getMultiplayer();

			if (multi > 1 && fn.size() > 1) {
				JOptionPane.showMessageDialog(getTab(),
						I18.get("no_duets_edit_msg"),
						I18.get("no_duets_edit_title"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		if (openFiles(fn)) {
			setView(VIEW_EDIT);
		}
	}

	/**
	 * Description of the Method
	 */
	public void mergeSelectedSongs() {
		Vector<?> fn = songList.getSelectedFiles();
		if (fn == null) {
			return;
		}
		mergeFiles(fn);
	}

	/**
	 * Description of the Method
	 */
	public void splitSelectedSong() {
		Vector<?> fn = songList.getSelectedFiles();
		if (fn == null) {
			return;
		}
		if (fn.size() > 1) {
			return;
		}

		splitFile((String) fn.firstElement());
	}

	/**
	 * Description of the Method
	 */
	public void refreshLibrary() {
		clearFilter();
		playList.refreshWhenLoaded();
		songList.refresh();
		refreshGroups();
		updatePlayListBox();
	}

	Action refreshLibrary = new AbstractAction(I18.get("mlib_refresh")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			refreshLibrary();
		}
	};
	Action printLibrary = new AbstractAction(I18.get("mlib_print")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songList.printSongs();
		}
	};
	Action setPreviewStart = new AbstractAction(I18.get("mlib_previewstart")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int inout[] = songInfo.getInOut();
			songList.setPreviewStart(inout[0]);
		}
	};
	Action setMedleyStartEnd = new AbstractAction(
			I18.get("mlib_medleystartend")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int inout[] = songInfo.getInOut();
			songList.setMedleyStartEnd(inout[0], inout[1]);
		}
	};

	/**
	 * Sets the previewStart attribute of the YassActions object
	 */
	public void setPreviewStart() {
		int inout[] = songInfo.getInOut();
		songList.setPreviewStart(inout[0]);
	}

	/**
	 * Sets the medleyStartEnd attribute of the YassActions object
	 */
	public void setMedleyStartEnd() {
		int inout[] = songInfo.getInOut();
		songList.setMedleyStartEnd(inout[0], inout[1]);
	}

	long lastTogglePressed = -1;

	Action togglePlaySong = new AbstractAction(I18.get("mlib_play_song")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			togglePlaySong();
		}
	};
	Action playSong = new AbstractAction(I18.get("mlib_play_song")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (isFilterEditing()) {
				return;
			}
			playSong();
		}
	};

	/**
	 * Description of the Method
	 */
	public void togglePlaySong() {
		long currentTime = System.currentTimeMillis();
		if (currentTime < lastTogglePressed + 500) {
			return;
		}
		lastTogglePressed = currentTime;

		if (isLibraryPlaying) {
			songList.stopPlaying();
			playToggle.setIcon(getIcon("play24Icon"));
			isLibraryPlaying = false;
		} else {
			int inout[] = songInfo.getInOut();
			songList.startPlaying(inout[0], inout[1]);
			playToggle.setIcon(getIcon("stop24Icon"));
			isLibraryPlaying = true;
		}
	}

	private boolean soonStarting = false;

	/**
	 * Description of the Method
	 */
	public void playSong() {
		soonStarting = true;
		int inout[] = songInfo.getInOut();
		songList.startPlaying(inout[0], inout[1]);
		playToggle.setIcon(getIcon("stop24Icon"));
		isLibraryPlaying = true;
		soonStarting = false;
	}

	/**
	 * Description of the Method
	 */
	public void stopPlaySong() {
		songList.stopPlaying();
		playToggle.setIcon(getIcon("play24Icon"));
		isLibraryPlaying = false;
	}

	Action stopPlaySong = new AbstractAction(I18.get("mlib_stop_song")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (isFilterEditing()) {
				return;
			}
			stopPlaySong();
		}
	};

	Action thumbnailLibrary = new AbstractAction(I18.get("mlib_thumbs")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songList.startThumbnailer();
		}
	};
	Action setLibrary = new AbstractAction(I18.get("mlib_set_library")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			// songList.setLibrary();
			new YassLibOptions(prop, YassActions.this, songList, mp3);
		}
	};
	Action correctLibrary = new AbstractAction(I18.get("mlib_correct")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (songList.getOptions() != YassSongList.DETAILS
					|| !songList.getShowErrors()) {
				JOptionPane.showMessageDialog(getFrame(tab),
						I18.get("mlib_correct_error"), I18.get("mlib_correct"),
						JOptionPane.PLAIN_MESSAGE);
				return;
			}
			songList.batchCorrect();
		}
	};

	Action correctLength = new AbstractAction(I18.get("tool_correct_length")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songList.updateLength();
		}
	};
	Action correctAlbum = new AbstractAction(I18.get("tool_correct_album")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songList.updateAlbum();
		}
	};
	Action correctYear = new AbstractAction(I18.get("tool_correct_year")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songList.updateYear();
		}
	};
	Action correctPageBreakLibrary = new AbstractAction(
			I18.get("tool_correct_breaks")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int rows[] = songList.getSelectedRows();
			if (rows == null) {
				return;
			}
			int n = rows.length;
			if (n < 1) {
				return;
			}
			int ok = JOptionPane.showConfirmDialog(
					tab,
					"<html>"
							+ MessageFormat.format(
									I18.get("tool_correct_breaks_msg"), n),
					I18.get("tool_correct_breaks_title"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (ok != JOptionPane.OK_OPTION) {
				return;
			}

			Vector<String> msg = new Vector<>();
			// msg.addElement(YassRow.UNCOMMON_PAGE_BREAK);
			msg.addElement(YassRow.EARLY_PAGE_BREAK);
			msg.addElement(YassRow.LATE_PAGE_BREAK);
			msg.addElement(YassRow.PAGE_OVERLAP);
			songList.batchProcess(false, msg);
		}
	};
	Action correctTextLibrary = new AbstractAction(I18.get("tool_correct_text")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int rows[] = songList.getSelectedRows();
			if (rows == null) {
				return;
			}
			int n = rows.length;
			if (n < 1) {
				return;
			}
			int ok = JOptionPane.showConfirmDialog(
					tab,
					"<html>"
							+ MessageFormat.format(
									I18.get("tool_correct_text_msg"), n),
					I18.get("tool_correct_text_title"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (ok != JOptionPane.OK_OPTION) {
				return;
			}

			Vector<String> msg = new Vector<>();
			msg.addElement(YassRow.TOO_MUCH_SPACES);
			msg.addElement(YassRow.UNCOMMON_SPACING);
			songList.batchProcess(false, msg);
		}
	};

	Action renameFilesLibrary = new AbstractAction(
			I18.get("tool_correct_files")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int rows[] = songList.getSelectedRows();
			if (rows == null) {
				return;
			}
			int n = rows.length;
			if (n < 1) {
				return;
			}

			String coverID = prop.getProperty("cover-id");
			String backgroundID = prop.getProperty("background-id");
			String videoID = prop.getProperty("video-id");
			String videodirID = prop.getProperty("videodir-id");

			songInfo.preventLoad(true);

			int ok = JOptionPane
					.showConfirmDialog(
							tab,
							"<html>"
									+ MessageFormat.format(
											I18.get("tool_correct_files_msg"),
											n, coverID, backgroundID, videoID,
											videodirID), I18
									.get("tool_correct_files_title"),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE);
			if (ok != JOptionPane.OK_OPTION) {
				return;
			}

			songList.batchRename(false);

			songInfo.preventLoad(false);
			songInfo.resetSong();
		}
	};

	Action correctFilesLibrary = new AbstractAction(
			I18.get("tool_correct_filetags")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int rows[] = songList.getSelectedRows();
			if (rows == null) {
				return;
			}
			int n = rows.length;
			if (n < 1) {
				return;
			}
			int ok = JOptionPane.showConfirmDialog(
					tab,
					"<html>"
							+ MessageFormat.format(
									I18.get("tool_correct_filetags_msg"), n),
					I18.get("tool_correct_filetags_title"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (ok != JOptionPane.OK_OPTION) {
				return;
			}

			Vector<String> msg = new Vector<>();
			msg.addElement(YassRow.FILE_FOUND);
			// msg.addElement(YassRow.NO_COVER_LABEL);
			// msg.addElement(YassRow.NO_BACKGROUND_LABEL);
			// msg.addElement(YassRow.NO_VIDEO_LABEL);
			// msg.addElement(YassRow.DIRECTORY_WITHOUT_VIDEO);
			msg.addElement(YassRow.WRONG_VIDEOGAP);
			// msg.addElement(YassRow.WRONG_FILENAME);
			songList.batchProcess(false, msg);
		}
	};
	Action correctTagsLibrary = new AbstractAction(I18.get("tool_correct_tags")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int rows[] = songList.getSelectedRows();
			if (rows == null) {
				return;
			}
			int n = rows.length;
			if (n < 1) {
				return;
			}
			int ok = JOptionPane.showConfirmDialog(
					tab,
					"<html>"
							+ MessageFormat.format(
									I18.get("tool_correct_tags_msg"), n),
					I18.get("tool_correct_tags_title"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (ok != JOptionPane.OK_OPTION) {
				return;
			}

			Vector<String> msg = new Vector<>();
			msg.addElement(YassRow.MISSING_TAG);
			msg.addElement(YassRow.UNSORTED_COMMENTS);
			songList.batchProcess(false, msg);
		}
	};

	Action errLibrary = new AbstractAction(I18.get("mlib_errors_toggle")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			boolean onoff = errToggle.isSelected();

			songList.showErrors(onoff);
		}
	};

	Action refreshPlayList = new AbstractAction(
			I18.get("mlib_playlist_refresh")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int n = plBox.getSelectedIndex();
			if (n > 0) {
				playList.setPlayList(n - 1);
			}
		}
	};

	Action selectAllSongs = new AbstractAction(I18.get("mlib_select_all")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView != VIEW_LIBRARY) {
				return;
			}

			songList.selectAll();
		}
	};
	Action setTitle = new AbstractAction(I18.get("mlib_title")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView != VIEW_LIBRARY) {
				return;
			}

			songList.setTitle();
		}
	};
	Action setArtist = new AbstractAction(I18.get("mlib_artist")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView != VIEW_LIBRARY) {
				return;
			}
			songList.setArtist();
		}
	};
	Action setYear = new AbstractAction(I18.get("mlib_year")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView != VIEW_LIBRARY) {
				return;
			}
			songList.setYear();
		}
	};
	Action setAlbum = new AbstractAction(I18.get("mlib_album")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView != VIEW_LIBRARY) {
				return;
			}
			songList.setAlbum();
		}
	};
	Action setLength = new AbstractAction(I18.get("mlib_length")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView != VIEW_LIBRARY) {
				return;
			}
			songList.setLength();
		}
	};
	Action setID = new AbstractAction(I18.get("mlib_id")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView != VIEW_LIBRARY) {
				return;
			}
			songList.setID();
		}
	};
	Action setEncodingUTF8 = new AbstractAction(I18.get("mlib_encoding_utf8")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView != VIEW_LIBRARY) {
				return;
			}
			songList.setEncoding("UTF8");
		}
	};
	Action setEncodingANSI = new AbstractAction(I18.get("mlib_encoding_ansi")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView != VIEW_LIBRARY) {
				return;
			}
			songList.setEncoding(null);
		}
	};
	Action newEdition = new AbstractAction(I18.get("mlib_edition_new")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songList.newEdition();
			refreshGroups();
		}
	};
	Action newFolder = new AbstractAction(I18.get("mlib_folder_new")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songList.newFolder();
			refreshGroups();
		}
	};
	Action renameFolder = new AbstractAction(I18.get("mlib_folder_rename")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			YassSong s = songList.getFirstSelectedSong();
			if (s == null) {
				return;
			}

			if (!songList.renameFolder()) {
				return;
			}

			groups.setIgnoreChange(true);
			groups.refresh();

			String rule = s.getFolder();
			int n = groups.getRowCount();
			for (int i = 0; i < n; i++) {
				String ir = (String) groups.getValueAt(i, 0);
				if (ir.equals(rule)) {
					groups.setRowSelectionInterval(i, i);
					break;
				}
			}
			groups.setIgnoreChange(false);
		}
	};

	/**
	 * Description of the Field
	 */
	public static int VIEW_LIBRARY = 1;
	/**
	 * Description of the Field
	 */
	public static int VIEW_EDIT = 2;

	/**
	 * Description of the Field
	 */
	public static int VIEW_PLAY_CURRENT = 3;

	/**
	 * Description of the Field
	 */
	public static int VIEW_PLAYER = 4;

	/**
	 * Description of the Field
	 */
	public static int VIEW_JUKEBOX = 5;

	/**
	 * Description of the Field
	 */
	public static int VIEW_SCREEN = 6;

	/**
	 * Description of the Field
	 */
	public int currentView = 0;

	/**
	 * Sets the view attribute of the YassActions object
	 * 
	 * @param n
	 *            The new view value
	 */

	JPanel main = null;
	JComponent libComponent = null, groupsComponent = null,
			songComponent = null, songinfoComponent = null,
			playlistComponent = null, editComponent = null;
	JApplet menuHolder = null;

	/**
	 * Sets the splitPanes attribute of the YassActions object
	 * 
	 * @param main
	 *            The new panels value
	 * @param libComponent
	 *            The new panels value
	 * @param editComponent
	 *            The new panels value
	 * @param f
	 *            The new panels value
	 * @param groupsComponent
	 *            The new panels value
	 * @param songComponent
	 *            The new panels value
	 * @param songinfoComponent
	 *            The new panels value
	 * @param playlistComponent
	 *            The new panels value
	 */
	public void setPanels(JApplet f, JPanel main, JComponent libComponent,
			JComponent songinfoComponent, JComponent groupsComponent,
			JComponent songComponent, JComponent playlistComponent,
			JComponent editComponent) {
		this.menuHolder = f;
		this.main = main;
		this.libComponent = libComponent;
		this.groupsComponent = groupsComponent;
		this.songComponent = songComponent;
		this.songinfoComponent = songinfoComponent;
		this.playlistComponent = playlistComponent;
		this.editComponent = editComponent;
	}

	Rectangle songBounds = null;

	Action detailLibrary = new AbstractAction(I18.get("mlib_details_toggle")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			updateSongInfo(detailLibrary);
		}
	};

	/**
	 * Description of the Method
	 */
	public void updateDetails() {
		boolean onoff = detailToggle.isSelected();

		if (onoff) {
			songBounds = songComponent.getBounds();
			songComponent.setBounds(songBounds.x, songBounds.y,
					songinfoComponent.getWidth() - 10 - songBounds.x,
					songinfoComponent.getHeight() - 20);
			songinfoComponent.revalidate();

			songList.setOptions(YassSongList.DETAILS);
		} else {
			if (songBounds != null) {
				int hhlow = songinfoComponent.getHeight() - 20;
				int min = 200;
				int max = 500;
				int sw = songinfoComponent.getWidth() / 4;
				sw = Math.max(min, Math.min(sw, max));
				songComponent.setBounds(songBounds.x, songBounds.y, sw, hhlow);
			}
			songinfoComponent.revalidate();

			songList.setOptions(YassSongList.TILE);
		}
		songInfo.revalidate();
	}

	private JMenuBar editMenu = null, libMenu = null, playerMenu = null,
			screenMenu = null, playerscreenMenu = null;

	/**
	 * Sets the view attribute of the YassActions object
	 * 
	 * @param n
	 *            The new view value
	 */
	public void setView(int n) {
		if (n == VIEW_EDIT) {
			songList.stopPlaying();
			playToggle.setIcon(getIcon("play24Icon"));
			isLibraryPlaying = false;

			// System.out.println("init edit view 1");
			main.removeAll();
			main.add("Center", editComponent);

			mp3.setPlaybackRenderer(sheet);

			// System.out.println("init edit view 2");
			if (editMenu == null) {
				editMenu = createEditMenu();
			}
			menuHolder.setJMenuBar(editMenu);
			currentView = VIEW_EDIT;
		} else if (n == VIEW_LIBRARY) {
			main.removeAll();
			// System.out.println("init view 1");
			main.add("Center", libComponent);

			mp3.setPlaybackRenderer(sheet);

			// System.out.println("init view 2");
			if (libMenu == null) {
				libMenu = createLibraryMenu();
			}
			Window root = SwingUtilities.getWindowAncestor(main);
			if (root instanceof JFrame) {
				((Frame) root).setTitle(I18.get("yass_title"));
			}
			menuHolder.setJMenuBar(libMenu);
			currentView = VIEW_LIBRARY;
		} else if (n == VIEW_PLAY_CURRENT || n == VIEW_PLAYER
				|| n == VIEW_JUKEBOX) {
			songList.stopPlaying();
			playToggle.setIcon(getIcon("play24Icon"));
			isLibraryPlaying = false;

			YassPlaybackRenderer renderer = n == VIEW_PLAY_CURRENT ? new YassBasicRenderer()
					: YassScreen.getPlaybackRenderer();
			mp3.setPlaybackRenderer(renderer);

			JMenuBar menu = menuHolder.getJMenuBar();
			if (n == VIEW_PLAY_CURRENT) {
				if (playerMenu == null) {
					playerMenu = menu = createPlayerMenu();
				}
				menuHolder.setJMenuBar(playerMenu);

			} else if (n == VIEW_PLAYER) {
				if (playerscreenMenu == null) {
					playerscreenMenu = menu = createPlayerScreenMenu();
				}
				menuHolder.setJMenuBar(playerscreenMenu);
			}

			Component c = SwingUtilities.getRoot(main);
			if (c instanceof JFrame) {
				JFrame f = (JFrame) c;
				if (f.getExtendedState() != Frame.NORMAL) {
					menu.setVisible(false);
				}
			}
			currentView = n;
		} else if (n == VIEW_SCREEN) {
			songList.stopPlaying();
			playToggle.setIcon(getIcon("play24Icon"));
			isLibraryPlaying = false;

			main.removeAll();
			screen = YassScreen.getCurrentScreen();
			registerScreenActions(screen);
			main.add("Center", screen);

			if (screenMenu == null) {
				screenMenu = createScreenMenu();
			}
			menuHolder.setJMenuBar(screenMenu);
			Component c = SwingUtilities.getRoot(main);
			if (c instanceof JFrame) {
				JFrame f = (JFrame) c;
				if (f.getExtendedState() != Frame.NORMAL) {
					screenMenu.setVisible(false);
				}
			}
			currentView = VIEW_SCREEN;
		}
		// System.out.println("init view 3");

		String recent = prop.getProperty("recent-files");
		editRecent.setEnabled(recent != null && new File(recent).exists());

		menuHolder.validate();

		// System.out.println("init view 4");

		if (n == VIEW_EDIT) {
			sheet.revalidate();
			// System.out.println("init edit view 5");
			openEditor(false);

			// System.out.println("init edit view 6");
			updateSheetProperties();
		} else if (n == VIEW_LIBRARY) {
			// System.out.println("init view 4");
			// JViewport v = (JViewport) songList.getTableHeader().getParent();
			// if (v != null) {
			// v.setVisible(false);
			// }

			YassSong s = songList.getFirstSelectedSong();
			songInfo.setSong(s);
			updateSongInfo(null);
			songList.requestFocus();
		} else if (n == VIEW_PLAY_CURRENT) {
			openPlayerCurrent();
		} else if (n == VIEW_SCREEN) {
			openScreen();
		} else if (n == VIEW_PLAYER) {
			openPlayer();
		} else if (n == VIEW_JUKEBOX) {
			openJukebox();
		}
		menuHolder.repaint();
	}

	/**
	 * Description of the Method
	 */
	public void updateSheetProperties() {
		boolean mouseover = prop.getProperty("mouseover").equals("true");
		sheet.setMouseOver(mouseover);

		String s = prop.getProperty("sketching");
		boolean useSketching = s != null && s.equals("true");
		s = prop.getProperty("sketching-playback");
		boolean useSketchingPlayback = s != null && s.equals("true");
		sheet.enableSketching(useSketching, useSketchingPlayback);

		s = prop.getProperty("show-note-length");
		boolean showLength = s != null && s.equals("true");
		sheet.setNoteLengthVisible(showLength);

		s = prop.getProperty("show-note-height");
		boolean showHeight = s != null && s.equals("true");
		sheet.setNoteHeightVisible(showHeight);

		s = prop.getProperty("playback-buttons");
		boolean showPlayerButtons = s != null && s.equals("true");
		sheet.showPlayerButtons(showPlayerButtons);
	}

	/**
	 * Gets the view attribute of the YassActions object
	 * 
	 * @return The view value
	 */
	public int getView() {
		return currentView;
	}

	/*
	 * static class OptionsUI extends BasicComboBoxUI { public static
	 * ComponentUI createUI(JComponent c) { return new OptionsUI(); } protected
	 * JButton createArrowButton() { JButton b = new
	 * BasicArrowButton(BasicArrowButton.SOUTH,
	 * UIManager.getColor("ComboBox.buttonBackground"),
	 * UIManager.getColor("ComboBox.buttonShadow"),
	 * UIManager.getColor("ComboBox.buttonDarkShadow"),
	 * UIManager.getColor("ComboBox.buttonHighlight")); b.setFocusable(false);
	 * return b; } protected LayoutManager createLayoutManager() { return new
	 * OptionsLayoutManager(); } public class OptionsLayoutManager extends
	 * ComboBoxLayoutManager { public void layoutContainer(Container parent) {
	 * JComboBox cb = (JComboBox) parent; int width = cb.getWidth(); int height
	 * = cb.getHeight(); Insets insets = getInsets(); int buttonSize = 20;
	 * Rectangle cvb; System.out.println(width); if (arrowButton != null) {
	 * arrowButton.setBounds(width - (insets.right + buttonSize), insets.top,
	 * buttonSize, height - insets.top - insets.bottom); } if (filter != null) {
	 * cvb = rectangleForCurrentValue();
	 * filter.getEditor().getEditorComponent().setBounds(cvb); } } } }
	 */
	/**
	 * Sets the saved attribute of the YassActions object
	 * 
	 * @param onoff
	 *            The new saved value
	 */
	public void setSaved(boolean onoff) {
		saveSong.setEnabled(!onoff);
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public boolean cancelOpen() {
		if (!saveSong.isEnabled()) {
			return false;
		}

		int ok = JOptionPane.showConfirmDialog(tab,
				I18.get("medit_exit_cancel"), I18.get("medit_exit_title"),
				JOptionPane.OK_CANCEL_OPTION);
		if (ok != JOptionPane.OK_OPTION) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the libraryLoaded attribute of the YassActions object
	 * 
	 * @return The libraryLoaded value
	 */
	public boolean isLibraryLoaded() {
		return groups.isEnabled();
	}

	/**
	 * Sets the libraryLoaded attribute of the YassActions object
	 * 
	 * @param onoff
	 *            The new libraryLoaded value
	 */
	public void setLibraryLoaded(boolean onoff) {
		groups.setEnabled(onoff);
		groupsBox.setEnabled(onoff);
		filterEditor.setEnabled(onoff);
		filterAll.setEnabled(onoff);

		showPlaylistMenu.setEnabled(onoff);
		enableLyrics.setEnabled(onoff);
		showSongInfo.setEnabled(onoff);
		detailLibrary.setEnabled(onoff);
		showSongInfoBackground.setEnabled(onoff);
		songList.getSortByMenu().setEnabled(onoff);
		refreshLibrary.setEnabled(true);
		if (playToggle != null) {
			playToggle.setEnabled(onoff);
		}

		setPreviewStart.setEnabled(onoff);
		setMedleyStartEnd.setEnabled(onoff);
		if (encMenu != null) {
			encMenu.setEnabled(onoff);
			setEncodingUTF8.setEnabled(onoff);
			setEncodingANSI.setEnabled(onoff);
		}

		filterLibrary.setEnabled(onoff);
		setTitle.setEnabled(onoff);
		setArtist.setEnabled(onoff);
		setAlbum.setEnabled(onoff);
		setLength.setEnabled(onoff);
		setID.setEnabled(onoff);
		songList.getLanguageMenu().setEnabled(onoff);
		songList.getEditionMenu().setEnabled(onoff);
		songList.getGenreMenu().setEnabled(onoff);
		setYear.setEnabled(onoff);
		if (songInfoToggle != null) {
			songInfoToggle.setEnabled(onoff);
		}
		copySongInfo.setEnabled(onoff);
		copyLyricsSongInfo.setEnabled(onoff);
		copyCoverSongInfo.setEnabled(onoff);
		copyBackgroundSongInfo.setEnabled(onoff);
		copyVideoSongInfo.setEnabled(onoff);
		pasteSongInfo.setEnabled(onoff);

		exportMetadata.setEnabled(onoff);
		importMetadata.setEnabled(onoff);
		clearFilter.setEnabled(onoff);

		playSong.setEnabled(onoff);
		stopPlaySong.setEnabled(onoff);
		detailLibrary.setEnabled(onoff);
		errLibrary.setEnabled(onoff);

		showLibTable.setEnabled(onoff);
		mergeSongs.setEnabled(onoff);
		splitSong.setEnabled(onoff);

		selectAllSongs.setEnabled(onoff);

		correctLength.setEnabled(onoff);
		correctAlbum.setEnabled(onoff);
		correctYear.setEnabled(onoff);

		openSongFromLibrary.setEnabled(onoff);
		printLibrary.setEnabled(onoff);
		thumbnailLibrary.setEnabled(onoff);
		newFile.setEnabled(onoff);
		// importFiles.setEnabled(onoff);
		closeLibrary.setEnabled(onoff);
		openSongFolder.setEnabled(onoff);
		removeSong.setEnabled(onoff);
		addToPlayList.setEnabled(onoff);
		removeFromPlayList.setEnabled(onoff);
		newEdition.setEnabled(onoff);
		if (bgToggle != null) {
			bgToggle.setEnabled(onoff);
		}
		if (detailToggle != null) {
			detailToggle.setEnabled(onoff);
			detailToggle
					.setSelected(songList.getOptions() == YassSongList.DETAILS);
		}
		if (playlistToggle != null) {
			playlistToggle.setEnabled(onoff);
		}
		if (errToggle != null) {
			errToggle.setEnabled(onoff);
			boolean se = songList.getShowErrors() && onoff;
			errToggle.setSelected(se);
			correctPageBreakLibrary.setEnabled(se);
			correctFilesLibrary.setEnabled(se);
			correctTagsLibrary.setEnabled(se);
			correctTextLibrary.setEnabled(se);
		}
		correctLibrary.setEnabled(onoff);

		if (plBox != null) {
			plBox.setEnabled(onoff);
		}
		if (filter != null) {
			filter.setEnabled(onoff);
		}
	}

	/**
	 * Sets the opened attribute of the YassActions object
	 * 
	 * @param onoff
	 *            The new opened value
	 */
	public void setOpened(boolean onoff) {
		createVersion.setEnabled(onoff);
		renameVersion.setEnabled(onoff);
		removeVersion.setEnabled(onoff);
		setAsStandard.setEnabled(onoff);
		// mergeVersions.setEnabled(onoff);
		interruptPlay.setEnabled(onoff);
		rollLeft.setEnabled(onoff);
		rollRight.setEnabled(onoff);
		golden.setEnabled(onoff);
		freestyle.setEnabled(onoff);
		space.setEnabled(onoff);
		minus.setEnabled(onoff);
		// enableHyphenKeys.setEnabled(onoff);
		editLyrics.setEnabled(onoff);
		homeSong.setEnabled(onoff);
		findLyrics.setEnabled(onoff);
		spellLyrics.setEnabled(onoff);
		nextPage.setEnabled(onoff);
		prevPage.setEnabled(onoff);
		morePages.setEnabled(onoff);
		onePage.setEnabled(onoff);
		selectAll.setEnabled(onoff);
		lessPages.setEnabled(onoff);
		playAll.setEnabled(onoff);
		playAllFromHere.setEnabled(onoff);
		playSelection.setEnabled(onoff);
		playPage.setEnabled(onoff);
		playFrozen.setEnabled(onoff);
		copyRows.setEnabled(onoff);
		showCopiedRows.setEnabled(onoff);
		removeCopy.setEnabled(onoff);
		pasteRows.setEnabled(onoff);
		splitRows.setEnabled(onoff);
		joinRows.setEnabled(onoff);
		togglePageBreak.setEnabled(onoff);
		insertNote.setEnabled(onoff);
		removeRows.setEnabled(onoff);
		absolute.setEnabled(onoff);
		showTable.setEnabled(onoff);
		showBackground.setEnabled(onoff);
		showVideo.setEnabled(onoff);
		showNothing.setEnabled(onoff);

		if (playAllVideoCBI != null) {
			playAllVideoCBI.setEnabled(onoff);
		}
		enableMidi.setEnabled(onoff);
		enableAudio.setEnabled(onoff);

		toggleVideo.setEnabled(onoff);

		enableVideoAudio.setEnabled(onoff);
		undo.setEnabled(onoff);
		redo.setEnabled(onoff);
		closeSong.setEnabled(onoff);
		reloadSong.setEnabled(onoff);
		recordSelection.setEnabled(onoff);
		recordAll.setEnabled(onoff);
		selectLine.setEnabled(onoff);
		selectAll.setEnabled(onoff);
		multiply.setEnabled(onoff);
		divide.setEnabled(onoff);

		saveAsFile.setEnabled(onoff);
		// saveAllFile.setEnabled(onoff);
		autoCorrect.setEnabled(onoff);
		autoCorrectPageBreaks.setEnabled(onoff);
		autoCorrectTransposed.setEnabled(onoff);
		autoCorrectSpacing.setEnabled(onoff);

		if (gapSpinner != null) {
			gapSpinner.setEnabled(onoff);
		}
		if (vgapSpinner != null) {
			vgapSpinner.setEnabled(onoff);
		}
		if (startSpinner != null) {
			startSpinner.setEnabled(onoff);
		}
		if (endSpinner != null) {
			endSpinner.setEnabled(onoff);
		}
		if (bpmField != null) {
			bpmField.setEnabled(onoff);
		}

		setGapHere.setEnabled(onoff);
		setStartHere.setEnabled(onoff);
		setEndHere.setEnabled(onoff);
		setVideoGapHere.setEnabled(onoff);
		setVideoMarkHere.setEnabled(onoff);
		removeStart.setEnabled(onoff);
		removeEnd.setEnabled(onoff);
		removeVideoGap.setEnabled(onoff);

		showStartEnd.setEnabled(onoff);
		showLyricsStart.setEnabled(onoff);
		showVideoGap.setEnabled(onoff);
		showErrors.setEnabled(onoff);
		if (!onoff && hasErrorsButton != null) {
			hasErrorsButton.setBackground(stdBackground);
		}
		// openCover.setEnabled(onoff);
		// openBackground.setEnabled(onoff);
		// openMP3.setEnabled(onoff);
		// openVideo.setEnabled(onoff);
		// copyCover.setEnabled(onoff);
		// pasteCover.setEnabled(onoff);
		// copyBackground.setEnabled(onoff);
		// copySongInfo.setEnabled(onoff);
		// pasteBackground.setEnabled(onoff);
		// pasteSongInfo.setEnabled(onoff);
		// saveCover.setEnabled(onoff);
		// saveVideo.setEnabled(onoff);
		// saveBackground.setEnabled(onoff);
		// editCover.setEnabled(onoff);
		// editBackground.setEnabled(onoff);
		// reloadCover.setEnabled(onoff);
		// reloadVideo.setEnabled(onoff);
		// reloadBackground.setEnabled(onoff);

		// editRaw.setEnabled(onoff);
		// editRawParent.setEnabled(onoff);
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createLyricsToolbar() {
		JToolBar t = new JToolBar(I18.get("tool_lyrics"));
		t.setFloatable(false);
		t.setOrientation(SwingConstants.HORIZONTAL);
		AbstractButton b;

		t.add(b = new JButton());
		b.setAction(editLyrics);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("edit24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(space);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("space24Icon"));
		b.setFocusable(false);

		t.add(b = new JButton());
		b.setAction(minus);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("minus24Icon"));
		b.setFocusable(false);

		t.addSeparator();

		t.add(b = new JButton());
		b.setAction(golden);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("golden24Icon"));
		b.setFocusable(false);
		t.add(b = new JButton());
		b.setAction(freestyle);
		b.setToolTipText(b.getText());
		b.setText("");
		b.setIcon(getIcon("freestyle24Icon"));
		b.setFocusable(false);

		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	/*
	 * public JComponent createMP3Toolbar() { JToolBar t = new JToolBar();
	 * t.setFloatable(false); AbstractButton b; t.add(b = new JButton());
	 * b.setAction(openMP3); b.setToolTipText(b.getText()); b.setText("");
	 * b.setIcon(getIcon("grayOpen24Icon")); b.setFocusable(false);
	 * t.addSeparator(); startSpinner = new TimeSpinner("Start:", 0, 10000);
	 * t.add(startSpinner); startSpinner.getSpinner().setFocusable(false);
	 * startSpinner.getSpinner().addChangeListener( new ChangeListener() {
	 * public void stateChanged(ChangeEvent e) { if (!isUpdating) {
	 * setStart(startSpinner.getTime()); } } }); Dimension d =
	 * startSpinner.getPreferredSize(); d.height = t.getPreferredSize().height;
	 * startSpinner.setMaximumSize(d); t.add(b = new JButton());
	 * b.setAction(setStartHere); b.setToolTipText(b.getText()); b.setText("");
	 * b.setIcon(getIcon("paste24Icon")); b.setFocusable(false); t.add(b = new
	 * JButton()); b.setAction(removeStart); b.setToolTipText(b.getText());
	 * b.setText(""); b.setIcon(getIcon("delete24Icon")); b.setFocusable(false);
	 * t.add(Box.createRigidArea(new Dimension(30, 0))); endSpinner = new
	 * TimeSpinner("End:", 10000, 10000); t.add(endSpinner);
	 * endSpinner.getSpinner().setFocusable(false);
	 * endSpinner.getSpinner().addChangeListener( new ChangeListener() { public
	 * void stateChanged(ChangeEvent e) { if (!isUpdating) {
	 * setEnd(endSpinner.getTime()); } } }); d = endSpinner.getPreferredSize();
	 * d.height = t.getPreferredSize().height; endSpinner.setMaximumSize(d);
	 * t.add(b = new JButton()); b.setAction(setEndHere);
	 * b.setToolTipText(b.getText()); b.setText("");
	 * b.setIcon(getIcon("paste24Icon")); b.setFocusable(false); t.add(b = new
	 * JButton()); b.setAction(removeEnd); b.setToolTipText(b.getText());
	 * b.setText(""); b.setIcon(getIcon("delete24Icon")); b.setFocusable(false);
	 * t.add(Box.createHorizontalGlue()); return t; }
	 */
	TimeSpinner gapSpinner = null, startSpinner = null, endSpinner = null,
			vgapSpinner = null;

	Action setGapHere = new AbstractAction(I18.get("tool_lyrics_start_here")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int n = sheet.getPlayerPosition();
			long t = sheet.fromTimeline(n);
			setGap((int) t);
		}
	};

	Action setVideoMarkHere = new AbstractAction(I18.get("tool_video_mark")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (!vmarkButton.isSelected()) {
				setVideoMark(0);
				return;
			}

			setVideoMark(video.getTime());
		}
	};

	Action setVideoGapHere = new AbstractAction(
			I18.get("tool_video_start_here")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int n = sheet.getPlayerPosition();
			long t = sheet.fromTimeline(n);
			setVideoGap((int) (vmark - t));

			setVideoMark(0);
			vmarkButton.setSelected(false);

			long time = sheet.fromTimeline(sheet.getPlayerPosition());
			video.setTime((int) time);
			video.refreshPlayer();
		}
	};

	Action setStartHere = new AbstractAction(I18.get("tool_audio_start_here")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int n = sheet.getPlayerPosition();
			long t = sheet.fromTimeline(n);
			setStart((int) t);
		}
	};
	Action setEndHere = new AbstractAction(I18.get("tool_audio_end_here")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int n = sheet.getPlayerPosition();
			long t = sheet.fromTimeline(n);
			setEnd((int) t);
		}
	};

	Action removeStart = new AbstractAction(I18.get("tool_audio_start_reset")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			setStart(0);
		}
	};
	Action removeEnd = new AbstractAction(I18.get("tool_audio_end_reset")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			setEnd((int) (mp3.getDuration() / 1000));
		}
	};
	Action removeVideoGap = new AbstractAction(I18.get("tool_video_gap_reset")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			setVideoGap(0);
		}
	};

	Action undoLibraryChanges = new AbstractAction("Undo Changes") {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			undoLibraryChanges();
			refreshGroups();
		}
	};
	Action undoAllLibraryChanges = new AbstractAction(I18.get("mlib_undo_all")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			undoAllLibraryChanges();
			refreshGroups();
		}
	};
	Action openSongFolder = new AbstractAction(I18.get("mlib_open_folder")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songList.openSongFolder();
		}
	};

	/**
	 * Description of the Method
	 */
	public void openSongFolder() {
		songList.openSongFolder();
	}

	/**
	 * Description of the Method
	 */
	public void refreshGroups() {
		int row = groups.getSelectedRow();
		String rule = row < 0 ? "" : (String) groups.getValueAt(row, 0);

		groups.setIgnoreChange(true);
		groups.refresh();

		int n = groups.getRowCount();
		for (int i = 0; i < n; i++) {
			String ir = (String) groups.getValueAt(i, 0);
			if (ir.equals(rule)) {
				groups.setRowSelectionInterval(i, i);
				break;
			}
		}
		groups.setIgnoreChange(false);
	}

	Action showSongInfo = new AbstractAction(I18.get("mlib_info_toggle")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			updateSongInfo(showSongInfo);
		}
	};
	Action showSongInfoBackground = new AbstractAction(
			I18.get("mlib_background_toggle")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			updateSongInfo(showSongInfoBackground);
		}
	};
	Action showPlaylist = new AbstractAction(I18.get("mlib_playlist_toggle")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			updateSongInfo(showPlaylist);
		}
	};
	Action showPlaylistMenu = new AbstractAction(
			I18.get("mlib_playlist_toggle")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			playlistToggle.setSelected(!playlistToggle.isSelected());
			updateSongInfo(showPlaylist);
		}
	};

	/**
	 * Description of the Method
	 */
	public void toggleSongInfo() {
		boolean onoff = songInfoToggle.isSelected();
		if (onoff) {
			updateSongInfo(enableLyrics);
		} else {
			updateSongInfo(showSongInfo);
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @param a
	 *            Description of the Parameter
	 */
	public void updateSongInfo(Action a) {
		songInfoToggle.setSelected(false);
		filterAll.setSelected(false);
		detailToggle.setSelected(false);
		bgToggle.setSelected(false);

		boolean info = a == showSongInfo;
		boolean lyrics = a == enableLyrics;
		boolean playlist = playlistToggle.isSelected();
		boolean detail = a == detailLibrary;
		boolean bgonly = a == showSongInfoBackground;

		if (!(info || lyrics || detail || bgonly)) {
			// songInfo.show(songInfo.SHOW_NONE);
			lyrics = true;
		}

		songInfoToggle.setSelected(info);
		filterAll.setSelected(lyrics);
		detailToggle.setSelected(detail);
		bgToggle.setSelected(bgonly);

		if (info) {
			songInfo.show(songInfo.SHOW_FILES);
		}

		if (lyrics) {
			songInfo.clear();
			songList.showLyrics(true);
			YassSong s = songList.getFirstSelectedSong();
			if (s != null) {
				songList.loadSongDetails(s, new YassTable());
			}
			songInfo.setSong(s);

			songInfo.show(songInfo.SHOW_LYRICS);
		} else {
			songList.showLyrics(false);
		}

		if (bgonly) {
			songInfo.showChildren(false);
			playlistComponent.setVisible(false);

			songInfoToggle.setSelected(false);
			filterAll.setSelected(false);
			detailToggle.setSelected(false);
		} else {
			songInfo.showChildren(true);
		}

		if (playlist && !bgonly) {
			playlistComponent.setVisible(true);
		} else {
			playlistComponent.setVisible(false);
		}

		if (detail) {
			playlistComponent.setVisible(false);
			updateDetails();
			songInfo.show(songInfo.SHOW_NONE);

			songInfoToggle.setSelected(false);
			filterAll.setSelected(false);
			bgToggle.setSelected(false);
		} else {
			updateDetails();
		}

		boolean plon = playlistComponent.isVisible();
		playlistToggle.setSelected(plon);
		if (playlistCBI != null) {
			playlistCBI.setState(plon);
		}

		songInfo.repaint();
	}

	Action copySongInfo = new AbstractAction(I18.get("mlib_copy_data")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songInfo.copy(songInfo.NONE);
		}
	};
	Action copyCoverSongInfo = new AbstractAction(I18.get("mlib_copy_cover")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songInfo.copyCover();
		}
	};
	Action copyLyricsSongInfo = new AbstractAction(I18.get("mlib_copy_lyrics")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songInfo.copyLyrics();
		}
	};
	Action copyBackgroundSongInfo = new AbstractAction(
			I18.get("mlib_copy_background")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songInfo.copyBackground();
		}
	};
	Action copyVideoSongInfo = new AbstractAction(I18.get("mlib_copy_video")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songInfo.copyVideo();
		}
	};
	Action pasteSongInfo = new AbstractAction(I18.get("mlib_paste_data")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songInfo.paste();
		}
	};

	/**
	 * Description of the Method
	 */
	public void stopPlayers() {
		songInfo.stopPlayer();
	}

	private JToolBar videoToolbar = null;

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JComponent createVideoToolbar() {
		if (videoToolbar != null) {
			return videoToolbar;
		}

		JToolBar t = new JToolBar(I18.get("tool_video"));
		videoToolbar = t;
		t.setFloatable(false);
		AbstractButton b;

		if (YassVideoUtils.useFOBS) {
			t.add(videoAudioButton = new JToggleButton());
			videoAudioButton.setAction(enableVideoAudio);
			videoAudioButton.setToolTipText(videoAudioButton.getText());
			videoAudioButton.setText("");
			videoAudioButton.setIcon(getIcon("mute24Icon"));
			videoAudioButton.setSelectedIcon(getIcon("nomute24Icon"));
			videoAudioButton.setFocusable(false);
			videoAudioButton.setSelected(true);
			t.addSeparator();
		}
		vgapSpinner = new TimeSpinner(I18.get("tool_video_gap"), 0, 10000,
				TimeSpinner.NEGATIVE);
		t.add(vgapSpinner);
		vgapSpinner.getSpinner().setFocusable(false);
		vgapSpinner.getSpinner().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!isUpdating) {
					setVideoGap(vgapSpinner.getTime());
				}
			}
		});
		Dimension d = vgapSpinner.getPreferredSize();
		d.height = t.getPreferredSize().height;
		vgapSpinner.setMaximumSize(d);

		if (YassVideoUtils.useFOBS) {
			t.add(Box.createHorizontalGlue());

			t.add(vmarkButton = new JToggleButton());
			vmarkButton.setAction(setVideoMarkHere);
			vmarkButton.setToolTipText(vmarkButton.getText());
			vmarkButton.setText("");
			vmarkButton.setIcon(getIcon("copy24Icon"));
			vmarkButton.setFocusable(false);
			t.add(b = new JButton());
			b.setAction(setVideoGapHere);
			b.setToolTipText(b.getText());
			b.setText("");
			b.setIcon(getIcon("paste24Icon"));
			b.setFocusable(false);
			t.add(b = new JButton());
			b.setAction(removeVideoGap);
			b.setToolTipText(b.getText());
			b.setText("");
			b.setIcon(getIcon("delete24Icon"));
			b.setFocusable(false);
		}
		return t;
	}

	/**
	 * Description of the Method
	 */
	public void updatePlayerPosition() {
		if (sheet == null) {
			return;
		}
		int i = table.getSelectionModel().getMinSelectionIndex();
		if (i >= 0) {
			YassRow r = table.getRowAt(i);
			if (r.isNoteOrPageBreak()) {
				sheet.setPlayerPosition(sheet.beatToTimeline(r.getBeatInt()));
			} else if (r.isGap()) {
				sheet.setPlayerPosition(sheet.beatToTimeline(0));
			} else if (r.isEnd()) {
				sheet.setPlayerPosition(sheet.toTimeline(sheet.getDuration()));
			} else if (r.isComment()) {
				sheet.setPlayerPosition(sheet.toTimeline(table.getStart()));
			}
		}

		if (video != null && sheet.showVideo()) {
			if (vmark != 0) {
				video.setTime(vmark);
				return;
			}
			long time = 0;
			time = sheet.fromTimeline(sheet.getPlayerPosition());
			if (!sheet.isLive()) {
				video.setTime((int) time);
			}
		}
	}

	Action addToPlayList = new AbstractAction(I18.get("mlib_playlist_add")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			Vector<?> v = songList.getSelectedSongs();
			playList.addSongs(v);
			songList.repaint();
		}
	};

	/**
	 * Adds a feature to the ToPlayList attribute of the YassActions object
	 * 
	 * @param s
	 *            The feature to be added to the ToPlayList attribute
	 */
	public void addToPlayList(YassSong s) {
		Vector<YassSong> v = new Vector<>();
		v.addElement(s);
		playList.addSongs(v);
		songList.repaint();
	}

	Action removeFromPlayList = new AbstractAction(
			I18.get("mlib_playlist_remove")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			playList.removeSongs();
			songList.repaint();
		}
	};

	/**
	 * Description of the Method
	 * 
	 * @param s
	 *            Description of the Parameter
	 */
	public void removeFromPlayList(YassSong s) {
		int i = ((YassSongListModel) playList.getList().getModel()).getData()
				.indexOf(s);
		if (i < 0) {
			return;
		}
		int rows[] = new int[1];
		rows[0] = i;
		playList.removeSongs(rows);
		songList.repaint();
	}

	Action movePlayListDown = new AbstractAction(I18.get("mlib_playlist_down")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			playList.down();
		}
	};
	Action openPlayList = new AbstractAction(I18.get("mlib_playlist_open")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			playList.openPlayList();
		}
	};
	Action removePlayList = new AbstractAction(
			I18.get("mlib_playlist_remove_disk")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int n = plBox.getSelectedIndex();
			if (n > 0) {
				playList.removePlayList(n - 1);
			}
		}
	};
	Action movePlayListUp = new AbstractAction(I18.get("mlib_playlist_up")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			playList.up();
		}
	};
	Action savePlayList = new AbstractAction(I18.get("mlib_playlist_save")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			playList.storePlayList();

		}
	};

	Action savePlayListAs = new AbstractAction(I18.get("mlib_playlist_save_as")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			playList.storePlayListAs();
		}
	};

	Action saveLibrary = new AbstractAction(I18.get("mlib_save_all")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songList.store();
			saveLibrary.setEnabled(false);
			undoAllLibraryChanges.setEnabled(false);
			refreshLibrary.setEnabled(false);
		}
	};

	Action saveLibrarySelected = new AbstractAction("not used") {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			// not used
		}
	};

	private JComponent dropTarget = null;

	/**
	 * Sets the dropTarget attribute of the YassActions object
	 * 
	 * @param c
	 *            The new dropTarget value
	 */
	public void setDropTarget(JComponent c) {
		if (dropTarget == null) {
			dropTarget = c;
		}
		new DropTarget(c, this);
	}

	/**
	 * Description of the Method
	 * 
	 * @param dropTargetDragEvent
	 *            Description of the Parameter
	 */
	public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
		dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
	}

	/**
	 * Description of the Method
	 * 
	 * @param dropTargetEvent
	 *            Description of the Parameter
	 */
	public void dragExit(DropTargetEvent dropTargetEvent) {
	}

	/**
	 * Description of the Method
	 * 
	 * @param dropTargetDragEvent
	 *            Description of the Parameter
	 */
	public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
	}

	/**
	 * Description of the Method
	 * 
	 * @param dropTargetDragEvent
	 *            Description of the Parameter
	 */
	public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
	}

	/**
	 * Description of the Method
	 * 
	 * @param dropTargetDropEvent
	 *            Description of the Parameter
	 */
	public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
		try {
			boolean enableImport = false;

			Transferable tr = dropTargetDropEvent.getTransferable();
			if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				dropTargetDropEvent
						.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

				String songdir = prop.getProperty("song-directory");
				if (songdir == null || !new File(songdir).exists()) {
					dropTargetDropEvent.getDropTargetContext().dropComplete(
							true);
					return;
				}
				String canonicSongDir = new File(songdir).getCanonicalPath();
				boolean insideSongDir = false;

				Vector<File> fileVector = new Vector<>();
				java.util.List fileList = (java.util.List) tr
						.getTransferData(DataFlavor.javaFileListFlavor);
				Iterator<?> iterator = fileList.iterator();
				while (iterator.hasNext()) {
					File file = (File) iterator.next();

					if (canonicSongDir != null) {
						insideSongDir = file.getCanonicalPath().startsWith(
								canonicSongDir);
					}

					if (file.isDirectory()) {
						if (insideSongDir) {
							File[] files = file.listFiles();
							file = null;
							for (int i = 0; i < files.length; i++) {
								String str = files[i].getAbsolutePath();
								if (str.toLowerCase().endsWith(".txt")) {
									file = files[i];
									// load STANDARD version, if possible
									if (file.getName().indexOf("[") < 0) {
										break;
									}
								}
							}
							if (file != null) {
								openFile(file.getAbsolutePath());
								break;
							}
						} else {
							fileVector.addElement(file);
						}
					} else {
						String fn = file.getName().toLowerCase();
						if (insideSongDir) {
							if (fn.endsWith(".txt")) {
								openFile(file.getAbsolutePath());
								break;
							}
						} else {
							if (fn.endsWith(".txt") || fn.endsWith(".mp3")
									|| fn.endsWith(".mpg")
									|| fn.endsWith(".mpeg")
									|| fn.endsWith(".jpg")
									|| fn.endsWith(".jpeg")) {
								fileVector.addElement(file);
							}
							if (fn.endsWith(".kar") || fn.endsWith(".mid")) {
								dropTargetDropEvent.getDropTargetContext()
										.dropComplete(true);
								createNewSong(file.getAbsolutePath());
								return;
							}
						}
					}
				}
				dropTargetDropEvent.getDropTargetContext().dropComplete(true);
				File[] ff = new File[fileVector.size()];
				fileVector.copyInto(ff);
				if (enableImport) {
					YassImport.importFiles(songList, prop, ff);
				}
				return;
			}

			if (enableImport
					&& dropTargetDropEvent
							.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				dropTargetDropEvent
						.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				Object td = tr.getTransferData(DataFlavor.stringFlavor);
				if (td instanceof String) {
					// TODO IMPORT
					String tds = (String) td;
					boolean ok = isValidKaraokeString(tds);
					if (ok) {
						StringTokenizer st = new StringTokenizer(tds, "\n\r");
						dropTargetDropEvent.getDropTargetContext()
								.dropComplete(true);
						YassTable t = new YassTable();
						t.init(prop);
						while (st.hasMoreTokens()) {
							String s = st.nextToken();
							if (!t.addRow(s)) {
								break;
							}
						}
						String fn = YassImport.importTable(songList, prop, t);
						if (fn != null) {
							openFile(fn);
						}
					}
				} else {
					dropTargetDropEvent.getDropTargetContext().dropComplete(
							true);
				}
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		dropTargetDropEvent.rejectDrop();
	}

	Action showOptions = new AbstractAction(I18.get("mlib_prefs_title")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			String oldDir = prop.getProperty("song-directory");
			String oldpDir = prop.getProperty("playlist-directory");
			if (oldDir == null) {
				oldDir = "";
			}
			if (oldpDir == null) {
				oldpDir = "";
			}

			boolean moveArticles = prop.get("use-articles").equals("true");

			//
			new yass.options.YassOptions(YassActions.this);
			//

			loadColors();
			loadKeys();
			loadScreenKeys();
			loadLayout();
			auto.init(prop, YassActions.this);

			String layout = prop.getProperty("editor-layout");
			if (layout == null) {
				layout = "East";
			}
			sheet.setLyricsLayout(layout);

			String hLangs = prop.getProperty("note-naming-h").toLowerCase();
			sheet.setHNoteEnabled(hLangs.contains(I18.getLanguage()));

			lyrics.init(prop);

			String seekInOffset = prop.getProperty("seek-in-offset");
			String seekOutOffset = prop.getProperty("seek-out-offset");
			try {
				mp3.setSeekInOffset(Integer.parseInt(seekInOffset));
				mp3.setSeekOutOffset(Integer.parseInt(seekOutOffset));
			} catch (Exception ex) {
			}

			String newDir = prop.getProperty("song-directory");
			String newpDir = prop.getProperty("playlist-directory");

			String importDir = prop.getProperty("import-directory");
			if (newDir != null && !importDir.startsWith(newDir)) {
				importDir = newDir;
				prop.setProperty("import-directory", newDir);
				prop.store();
				JOptionPane.showMessageDialog(tab,
						I18.get("mlib_prefs_import_error"),
						I18.get("mlib_prefs_title"),
						JOptionPane.WARNING_MESSAGE);
			}

			boolean newMoveArticles = prop.get("use-articles").equals("true");
			songList.moveArticles(newMoveArticles);

			boolean useWav = prop.get("use-sample").equals("true");
			mp3.useWav(useWav);

			boolean debugWaveform = prop.getProperty("debug-waveform").equals(
					"true");
			mp3.createWaveform(debugWaveform);

			if (sheet != null) {
				updateSheetProperties();
			}
			boolean autoTrim = prop.getProperty("auto-trim").equals("true");
			setAutoTrim(autoTrim);

			boolean needSongDirUpdate = false;
			if (newDir != null && !newDir.equals(oldDir)) {
				needSongDirUpdate = true;
			}
			if (newpDir != null && !newpDir.equals(oldpDir)) {
				needSongDirUpdate = true;
			}
			if (moveArticles != newMoveArticles) {
				needSongDirUpdate = true;
			}

			if (needSongDirUpdate) {
				setLibraryLoaded(false);
				playList.clear();
				updatePlayListBox();
				songList.clear();
				songList.load();
			}
		}
	};

	Action showAbout = new AbstractAction(I18.get("mlib_about")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			URL url = getClass().getResource("/yass/about.gif");
			ImageIcon icon;
			if (url == null) {
				icon = null;
			} else {
				icon = new ImageIcon(url);
			}

			try {
				JEditorPane label = new JEditorPane("text/html",
						I18.getCopyright(VERSION, DATE));
				label.setEditable(false);
				label.setBackground(new JLabel().getBackground());
				label.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
							openURL(e.getURL().toExternalForm());
						}
					}
				});

				JOptionPane.showMessageDialog(null, label,
						I18.get("mlib_about_title"), JOptionPane.PLAIN_MESSAGE,
						icon);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	/**
	 * Description of the Method
	 * 
	 * @param url
	 *            Description of the Parameter
	 */
	public static void openURL(String url) {
		String osName = System.getProperty("os.name");
		try {
			if (osName.startsWith("Mac OS")) {
				Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
				java.lang.reflect.Method openURL = fileMgr.getDeclaredMethod(
						"openURL", new Class[] { String.class });
				openURL.invoke(null, new Object[] { url });
			} else if (osName.startsWith("Windows")) {
				Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " + url);
			} else {
				String[] browsers = { "firefox", "opera", "konqueror",
						"epiphany", "mozilla", "netscape" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++) {
					if (Runtime.getRuntime()
							.exec(new String[] { "which", browsers[count] })
							.waitFor() == 0) {
						browser = browsers[count];
					}
				}
				if (browser == null) {
					throw new Exception("Could not find web browser");
				} else {
					Runtime.getRuntime().exec(new String[] { browser, url });
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @param fn
	 *            Description of the Parameter
	 */
	public static void openURLFile(String fn) {
		Vector<String> v = new Vector<>();
		v.addElement(fn);
		openURLFiles(v);
	}

	/**
	 * Description of the Method
	 * 
	 * @param fn
	 *            Description of the Parameter
	 */
	public static void openURLFiles(Vector<String> fn) {
		if (fn == null || fn.size() < 1 || fn.size() > 10) {
			return;
		}
		try {
			for (Enumeration<String> en = fn.elements(); en.hasMoreElements();) {
				String filename = (String) en.nextElement();

				try {
					// Java 1.6
					Class<?> c = Class.forName("java.awt.Desktop");
					Method m = c.getMethod("getDesktop", (Class[]) null);
					Object desktop = m.invoke(null, (Object[]) null);

					Class[] par = new Class[1];
					par[0] = File.class;
					m = c.getMethod("open", par);
					m.invoke(desktop, new File(filename));
					// System.out.println("desktop open " + filename);
				} catch (Throwable t) {
					try {
						String os = System.getProperty("os.name");
						if (os.startsWith("Windows")) {
							Runtime.getRuntime().exec(
									"rundll32 url.dll,FileProtocolHandler "
											+ filename);
							// System.out.println("runtime open " + filename);
						} else if (System.getProperty("os.name").startsWith(
								"MacOS")) {
							Class<?> fileMgr = Class
									.forName("com.apple.eio.FileManager");
							java.lang.reflect.Method openURL = fileMgr
									.getDeclaredMethod("openURL",
											new Class[] { String.class });
							openURL.invoke(null, new Object[] { filename });
						} else if (System.getProperty("os.name").startsWith(
								"Linux")) {
							String[] apps = { "gnome-open", "kfmclient",
									"xdg-open", "gvfs-open", "firefox", "opera" };
							String app = null;
							for (int i = 0; i < apps.length && app == null; i++) {
								if (Runtime
										.getRuntime()
										.exec(new String[] { "which", apps[i] })
										.waitFor() == 0) {
									app = apps[i];
								}
							}
							if (app != null) {
								Runtime.getRuntime().exec(
										new String[] { app, filename });
							} else {

							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Gets the info attribute of the YassActions object
	 * 
	 * @return The info value
	 */
	public String getInfo() {
		StringBuffer sb = new StringBuffer();
		sb.append("Yet Another Songeditor by Saruta\nVersion: " + VERSION
				+ " (" + DATE + ")\nmail@yass-along.com\n\n");

		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferCapabilities bufCap = gc.getBufferCapabilities();
		boolean page = bufCap.isPageFlipping();
		sb.append("Maximum Heap: "
				+ (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "MB\n");
		sb.append("Occupied Heap: "
				+ (Runtime.getRuntime().totalMemory() / 1024 / 1024) + "MB\n");
		sb.append("Accelerated Memory: "
				+ (sheet.getAvailableAcceleratedMemory() / 1024 / 1024)
				+ "MB\n");
		sb.append("Page Flipping: " + (page ? "supported" : "no") + "\n");
		return sb.toString();
	}

	JDialog seDialog = null;
	Action showStartEnd = new AbstractAction(I18.get("medit_audio_offsets")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (seDialog != null) {
				if (seDialog.isShowing()) {
					seDialog.setVisible(false);
					return;
				}
				seDialog.pack();
				seDialog.setVisible(true);
				return;
			}

			JDialog dia = seDialog = new JDialog(new OwnerFrame());
			dia.setTitle(I18.get("medit_audio_offsets_title"));
			dia.setAlwaysOnTop(true);
			dia.setResizable(false);
			dia.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					e.getWindow().dispose();
				}
			});

			AbstractButton b;
			JToolBar panel = new JToolBar();
			panel.setFloatable(false);
			panel.add(startSpinner);

			panel.add(b = new JButton());
			b.setAction(setStartHere);
			b.setToolTipText(b.getText());
			b.setText("");
			b.setIcon(getIcon("bookmarks24Icon"));
			b.setFocusable(false);

			panel.add(b = new JButton());
			b.setAction(removeStart);
			b.setToolTipText(b.getText());
			b.setText("");
			b.setIcon(getIcon("delete24Icon"));
			b.setFocusable(false);

			panel.addSeparator();
			panel.add(endSpinner);

			panel.add(b = new JButton());
			b.setAction(setEndHere);
			b.setToolTipText(b.getText());
			b.setText("");
			b.setIcon(getIcon("bookmarks24Icon"));
			b.setFocusable(false);

			panel.add(b = new JButton());
			b.setAction(removeEnd);
			b.setToolTipText(b.getText());
			b.setText("");
			b.setIcon(getIcon("delete24Icon"));
			b.setFocusable(false);

			dia.add("Center", panel);
			dia.pack();
			// dia.setIconImage(new
			// ImageIcon(YassActions.this.getClass().getResource("/yass/yass-icon-16.png")).getImage());
			dia.setVisible(true);
		}
	};

	JDialog errDialog = null;
	Action showErrors = new AbstractAction(I18.get("medit_errors")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			if (errDialog != null) {
				if (errDialog.isShowing()) {
					errDialog.setVisible(false);
					return;
				}
				errDialog.pack();
				errDialog.setVisible(true);
				return;
			}

			JDialog dia = errDialog = new JDialog(new OwnerFrame());
			dia.setTitle(I18.get("medit_errors_title"));
			dia.setAlwaysOnTop(true);
			dia.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					e.getWindow().dispose();
				}
			});
			JPanel errorPanel = new JPanel(new BorderLayout());
			errorPanel.add("Center", errors);
			errorPanel.setSize(440, 400);
			errors.getMessagePanel().setPreferredSize(new Dimension(440, 180));
			errorPanel.setPreferredSize(new Dimension(440, 400));

			dia.add("Center", errorPanel);
			dia.pack();
			// dia.setIconImage(new
			// ImageIcon(YassActions.this.getClass().getResource("/yass/yass-icon-16.png")).getImage());
			dia.setVisible(true);
		}
	};

	/**
	 * Description of the Method
	 */
	public void hideErrors() {
		errDialog.setVisible(false);
	}

	JDialog gapDialog = null;
	JTextField bpmField = null;
	Action showLyricsStart = new AbstractAction(I18.get("medit_gap")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			updateGap();

			if (gapDialog != null) {
				if (gapDialog.isShowing()) {
					gapDialog.setVisible(false);
					return;
				}
				gapDialog.pack();
				gapDialog.setVisible(true);
				return;
			}

			JDialog dia = gapDialog = new JDialog(new OwnerFrame());
			dia.setTitle(I18.get("medit_gap_title"));
			dia.setAlwaysOnTop(true);
			dia.setResizable(false);
			dia.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					e.getWindow().dispose();
				}
			});

			AbstractButton b;
			JToolBar panel = new JToolBar();
			panel.setFloatable(false);
			panel.add(gapSpinner);

			panel.add(b = new JButton());
			b.setAction(setGapHere);
			b.setToolTipText(b.getText());
			b.setText("");
			b.setIcon(getIcon("paste24Icon"));
			b.setFocusable(false);

			panel.addSeparator();
			panel.add(new JLabel(I18.get("medit_bpm_title")));

			double bpm = table.getBPM();
			bpmField = new JTextField(bpm + "", 5);
			bpmField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String s = bpmField.getText();
					double bpm = table.getBPM();
					try {
						double val = Double.parseDouble(s);
						bpm = val;
					} catch (Exception ex) {
						bpmField.setText(bpm + "");
					}
					table.setBPM(bpm);
				}
			});
			panel.add(bpmField);

			panel.add(b = new JButton());
			b.setAction(multiply);
			b.setToolTipText(b.getText());
			b.setText("");
			b.setIcon(getIcon("fastforward24Icon"));
			b.setFocusable(false);
			panel.add(b = new JButton());
			b.setAction(divide);
			b.setToolTipText(b.getText());
			b.setText("");
			b.setIcon(getIcon("rewind24Icon"));
			b.setFocusable(false);

			dia.add("Center", panel);
			dia.pack();
			// dia.setIconImage(new
			// ImageIcon(YassActions.this.getClass().getResource("/yass/yass-icon-16.png")).getImage());
			dia.setVisible(true);
		}
	};

	JDialog vgapDialog = null;
	Action showVideoGap = new AbstractAction(I18.get("medit_videogap")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			sheet.showVideo(true);

			if (vgapDialog != null) {
				if (vgapDialog.isShowing()) {
					sheet.showVideo(false);
					vgapDialog.setVisible(false);
					return;
				}
				vgapDialog.pack();
				vgapDialog.setVisible(true);
				return;
			}

			updateGap();

			JDialog fh = vgapDialog = new JDialog(new OwnerFrame());
			fh.setTitle(I18.get("medit_videogap_title"));
			fh.setAlwaysOnTop(true);
			fh.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					sheet.showVideo(false);
					// video.closeVideo();
					e.getWindow().dispose();
				}
			});
			updateVideo();

			fh.add("Center", createVideoToolbar());
			fh.pack();
			// fh.setIconImage(new
			// ImageIcon(YassActions.this.getClass().getResource("/yass/yass-icon-16.png")).getImage());
			fh.setVisible(true);

			long time = 0;
			time = sheet.fromTimeline(sheet.getPlayerPosition());
			video.setTime((int) time);
		}
	};

	JDialog srcDialog = null;
	Action showTable = new AbstractAction(I18.get("medit_source")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (srcDialog != null) {
				if (srcDialog.isShowing()) {
					srcDialog.setVisible(false);
					return;
				}
				srcDialog.pack();
				srcDialog.setVisible(true);
				return;
			}

			JDialog dia = srcDialog = new JDialog(new OwnerFrame());
			dia.setTitle(I18.get("medit_source_title"));
			dia.setAlwaysOnTop(true);
			dia.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					e.getWindow().dispose();
				}
			});
			if (table == null) {
				table = new YassTable();
			}
			dia.add("Center", new JScrollPane(table));
			dia.pack();
			int w = 240;
			int h = 400;
			dia.setSize(w, h);
			// dia.setIconImage(new
			// ImageIcon(YassActions.this.getClass().getResource("/yass/yass-icon-16.png")).getImage());
			dia.setVisible(true);
		}
	};
	Action showLibTable = new AbstractAction(I18.get("mlib_source")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			Vector<?> v = songList.getSelectedFiles();
			if (v.size() < 1 || v.size() > 10) {
				return;
			}

			for (Enumeration<?> en = v.elements(); en.hasMoreElements();) {
				String fn = (String) en.nextElement();

				JDialog fh = new JDialog(new OwnerFrame());
				fh.setTitle(I18.get("mlib_source_title"));
				fh.setAlwaysOnTop(true);
				fh.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						e.getWindow().dispose();
					}
				});

				YassTable t = new YassTable();
				t.loadFile(fn);
				fh.add("Center", new JScrollPane(t));
				fh.pack();
				int w = 240;
				int h = 400;
				fh.setSize(w, h);
				// fh.setIconImage(new
				// ImageIcon(YassActions.this.getClass().getResource("/yass/yass-icon-16.png")).getImage());
				fh.setVisible(true);
			}
		}
	};

	/**
	 * Description of the Method
	 */
	public void showNothing() {
		sheet.showVideo(false);
		sheet.showBackground(false);
		showNothingToggle.setSelected(true);
		videoButton.setSelected(false);
		sheet.repaint();
	}

	Action showNothing = new AbstractAction(I18.get("medit_view_1")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			showNothing();
		}
	};
	Action showBackground = new AbstractAction(I18.get("medit_view_2")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			sheet.showVideo(false);
			sheet.showBackground(true);
			showBackgroundToggle.setSelected(true);
			videoButton.setSelected(false);
			sheet.repaint();
		}
	};
	Action showVideo = new AbstractAction(I18.get("medit_view_3")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (!YassVideoUtils.useFOBS) {
				return;
			}
			sheet.showVideo(true);
			sheet.showBackground(false);
			showVideoToggle.setSelected(true);
			videoButton.setSelected(true);

			long time = sheet.fromTimeline(sheet.getPlayerPosition());
			video.setTime((int) time);
			video.refreshPlayer();
			sheet.repaint();
		}
	};

	Action toggleVideo = new AbstractAction(I18.get("medit_video_toggle")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			boolean onoff = sheet.showVideo() || sheet.showBackground();
			onoff = !onoff;

			if (onoff) {
				video.setTime(0);
				if (video.getFrame() != null) {
					sheet.showVideo(true);
					sheet.showBackground(false);
					showVideoToggle.setSelected(true);
				} else {
					sheet.showVideo(false);
					sheet.showBackground(true);
					showBackgroundToggle.setSelected(true);
				}
			} else {
				sheet.showVideo(false);
				sheet.showBackground(false);
				showNothingToggle.setSelected(true);
			}

			videoButton.setSelected(onoff);

			long time = sheet.fromTimeline(sheet.getPlayerPosition());
			video.setTime((int) time);
			video.refreshPlayer();
			sheet.repaint();
		}
	};

	JTextPane helpPane = null;
	Action showOnlineHelp = new AbstractAction(I18.get("mlib_help_online")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			openURL("http://www.yass-along.com");
		}
	};

	Action showHelp = new AbstractAction(I18.get("mlib_help_offline")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			helpPane = new JTextPane();
			HTMLDocument doc = (HTMLDocument) helpPane
					.getEditorKitForContentType("text/html")
					.createDefaultDocument();
			doc.setAsynchronousLoadPriority(-1);
			helpPane.setDocument(doc);
			URL url = I18.getResource("help.html");
			try {
				helpPane.setPage(url);
			} catch (Exception ex) {
			}

			helpPane.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent event) {
					if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						URL url = I18.getResource(event.getDescription());
						try {
							helpPane.setPage(url);
						} catch (IOException ioe) {
						}
					}
				}
			});
			helpPane.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						SwingUtilities.getWindowAncestor(helpPane).dispose();
					}
					if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
						URL url = I18.getResource("help5.html");
						try {
							helpPane.setPage(url);
						} catch (Exception ex) {
						}

					}
				}
			});
			helpPane.setEditable(false);

			JFrame fh = new JFrame(I18.get("mlib_help_offline_title"));
			fh.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					e.getWindow().dispose();
				}
			});
			fh.add("Center", new JScrollPane(helpPane));
			fh.pack();
			fh.setSize(500, 600);
			fh.setIconImage(new ImageIcon(YassActions.this.getClass()
					.getResource("/yass/yass-icon-16.png")).getImage());
			fh.setVisible(true);
		}
	};

	JDialog bugDialog = null;
	JTextPane bugsPane = null;
	JScrollPane bugsScroll = null;

	Action showBugs = new AbstractAction("Bugs...") {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (bugDialog != null) {
				if (bugDialog.isShowing()) {
					bugDialog.setVisible(false);
					return;
				}
				bugDialog.setVisible(true);
				new UpdateBugs().start();
				return;
			}

			JDialog dia = bugDialog = new JDialog(new OwnerFrame());
			dia.setTitle(I18.get("bugs_title"));
			// dia.setAlwaysOnTop(true);
			dia.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					e.getWindow().dispose();
				}
			});

			bugsPane = new JTextPane();
			bugsPane.setBackground(new JLabel().getBackground());
			bugsPane.getEditorKitForContentType("text/html")
					.createDefaultDocument();
			bugsPane.setMargin(new Insets(5, 5, 5, 5));
			bugsPane.setEditable(false);

			bugsPane.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						SwingUtilities.getWindowAncestor(bugsPane).dispose();
					}
				}
			});

			dia.add("Center", bugsScroll = new JScrollPane(bugsPane));

			dia.pack();
			dia.setSize(800, 400);
			// dia.setIconImage(new
			// ImageIcon(YassActions.this.getClass().getResource("/yass/yass-icon-16.png")).getImage());
			dia.setVisible(true);

			new UpdateBugs().start();
		}
	};

	class UpdateBugs extends Thread {
		int bufferLength = -1;

		public void run() {
			while (bugDialog != null && bugDialog.isShowing()) {
				String s = getOutBuffer().toString();
				int len = s.length();
				if (s == null || len < 1) {
					s = "No errors.";
				}
				if (len != bufferLength) {
					bugsPane.setText(s);
					bufferLength = len;

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							bugsScroll.getVerticalScrollBar().setValue(
									bugsScroll.getVerticalScrollBar()
											.getMaximum());
						}
					});
				}
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}
			}
		}
	}

	Action editLyrics = new AbstractAction(I18.get("medit_lyrics_edit")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			editLyrics();
		}
	};

	/**
	 * Description of the Method
	 */
	public void editLyrics() {
		int rows[] = table.getSelectedRows();
		if (rows.length == 1) {
			YassRow r = table.getRowAt(rows[0]);
			if (r.isGap()) {
				String input = JOptionPane.showInputDialog(tab,
						I18.get("medit_lyrics_edit_gap_msg"),
						(int) (table.getGap()) + "");
				try {
					int gap = new Integer(input).intValue();
					table.setGap(gap);
					sheet.update();
					table.firstNote();
					table.repaint();
					table.addUndo();
				} catch (Exception ex) {
				}
				return;
			}
			if (r.isComment() && r.getCommentTag().equals("START:")) {
				String input = JOptionPane.showInputDialog(tab,
						I18.get("medit_lyrics_edit_start_msg"),
						(int) (table.getStart()) + "");
				try {
					int val = new Integer(input).intValue();
					setStart(val);
				} catch (Exception ex) {
				}
				return;
			}
			if (r.isEnd()
					|| (r.isComment() && r.getCommentTag().equals("END:"))) {
				String input = JOptionPane.showInputDialog(tab,
						I18.get("medit_lyrics_edit_end_msg"),
						(int) (table.getEnd()) + "");
				try {
					int val = new Integer(input).intValue();
					table.setEnd(val);
					sheet.update();
					table.lastNote();
					table.repaint();
					table.addUndo();
				} catch (Exception ex) {
				}
				return;
			}
		}

		if (lyrics.isEditable()) {
			lyrics.finishEditing();
		} else {
			lyrics.editLyrics();
		}
	}

	/**
	 * Sets the start attribute of the YassActions object
	 * 
	 * @param ms
	 *            The new start value
	 */
	public void setStart(int ms) {
		table.setStart(ms);

		updateStartEnd();
	}

	/**
	 * Sets the end attribute of the YassActions object
	 * 
	 * @param ms
	 *            The new end value
	 */
	public void setEnd(int ms) {
		if (ms == (int) (mp3.getDuration() / 1000)) {
			ms = -1;
		}

		table.setEnd(ms);
		updateStartEnd();
	}

	/**
	 * Description of the Method
	 */
	public void updateStartEnd() {
		int start = (int) table.getStart();
		startSpinner.setTime(start);
		int end = (int) table.getEnd();
		int dur = (int) (mp3.getDuration() / 1000);
		if (end < 0) {
			end = dur;
		}
		endSpinner.setTime(end);

		startSpinner.setDuration(dur);
		endSpinner.setDuration(dur);
	}

	/**
	 * Sets the gap attribute of the YassActions object
	 * 
	 * @param ms
	 *            The new gap value
	 */
	public void setGap(int ms) {
		table.setGap(ms);

		((YassTableModel) table.getModel()).getCommentRow("GAP:");
		sheet.setPlayerPosition(sheet.toTimeline(table.getGap()));
		updateGap();
	}

	/**
	 * Description of the Method
	 */
	public void updateGap() {
		int gap = (int) table.getGap();
		if (gapSpinner != null) {
			gapSpinner.setTime(gap);
			int dur = (int) (mp3.getDuration() / 1000);
			gapSpinner.setDuration(dur);
		}
		double bpm = table.getBPM();
		if (bpmField != null) {
			bpmField.setText(bpm + "");
		}
	}

	/**
	 * Sets the videoGap attribute of the YassActions object
	 * 
	 * @param ms
	 *            The new videoGap value
	 */
	public void setVideoGap(int ms) {
		table.setVideoGap(ms / 1000.0);
		updateVideoGap();
	}

	/**
	 * Sets the videoMark attribute of the YassActions object
	 * 
	 * @param ms
	 *            The new videoMark value
	 */
	public void setVideoMark(int ms) {
		vmark = ms;
	}

	int vmark = 0;

	/**
	 * Description of the Method
	 */
	public void updateVideoGap() {
		double vgap = (double) table.getVideoGap();
		int ms = (int) (vgap * 1000);

		if (vgapSpinner != null) {
			vgapSpinner.setTime(ms);
			int dur = (int) (mp3.getDuration() / 1000);
			vgapSpinner.setDuration(dur);
		}

		if (video != null && sheet.showVideo()) {
			video.setVideoGap(ms);
		}
	}

	/**
	 * Description of the Field
	 */
	public Action findLyrics = new AbstractAction(I18.get("medit_lyrics_find")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			lyrics.find();
		}
	};
	/**
	 * Description of the Field
	 */
	public Action spellLyrics = new AbstractAction(I18.get("medit_spellcheck")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			lyrics.spellLyrics();
		}
	};

	/**
	 * Unfortunately, most Java runtime environments fire multiple KEY_PRESSED
	 * and in some cases multiple KEY_RELEASED events when the user has only
	 * pressed the key once. Additionally, in some environments you may never
	 * receive the KEY_RELEASED events for an upstroke. This is because the
	 * behavior of KEY_PRESSED and KEY_RELEASED is system dependent. The
	 * behavior occurs through an interaction with the operating system's
	 * handling of key repeats that occur when you hold down a key for a period
	 * of time. On Windows, Java will produce multiple KEY_PRESSED events as the
	 * key is held down and only one KEY_RELEASED when the key is actually
	 * released. For example, holding down the 'A' key will generate these
	 * events: PRESSED 'A' PRESSED 'A' ... RELEASED 'A' On Unix, multiple pairs
	 * of KEY_PRESSED and KEY_RELEASED are received as the key is held down:
	 * PRESSED 'A' RELEASED 'A' PRESSED 'A' RELEASED 'A' ... PRESSED 'A'
	 * RELEASED 'A'
	 */
	Action playSelection = new AbstractAction(I18.get("medit_play_selection")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}

			playSelection(0);
		}
	};
	Action playSelectionWithMIDI = new AbstractAction(
			I18.get("medit_play_selection_midi")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}

			playSelection(1);
		}
	};
	Action playSelectionWithMIDIAudio = new AbstractAction(
			I18.get("medit_play_selection_midi_audio")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}

			playSelection(2);
		}
	};

	/**
	 * Description of the Method
	 * 
	 * @param mode
	 *            Description of the Parameter
	 */
	private void playSelection(int mode) {
		long inout[] = new long[2];

		int i = table.getSelectionModel().getMinSelectionIndex();
		int j = table.getSelectionModel().getMaxSelectionIndex();
		long pos = -1;
		boolean fromHere = i < 0;

		if (!fromHere) {
			YassRow r = table.getRowAt(i);
			if (!r.isNote()) {
				fromHere = true;
			}
		}

		if (fromHere) {
			pos = sheet.fromTimeline(sheet.getPlayerPosition()) * 1000;
			i = sheet.nextElement();
			j = -1;
		}

		startPlaying();
		long clicks[][] = table.getSelection(i, j, inout, null, false);

		boolean midiEnabled = midiButton.isSelected();
		boolean mp3Enabled = mp3Button.isSelected();
		if (mode == 0) {
			mp3.setMIDIEnabled(midiEnabled);
			mp3.setAudioEnabled(mp3Enabled);
		} else if (mode == 1) {
			mp3.setMIDIEnabled(true);
			mp3.setAudioEnabled(false);
		} else if (mode == 2) {
			mp3.setMIDIEnabled(true);
			mp3.setAudioEnabled(true);
		}

		mp3.playSelection(pos < 0 ? inout[0] : pos, inout[1], clicks);
	}

	private RecordEventListener awt = new RecordEventListener();

	/**
	 * Description of the Class
	 * 
	 * @author Saruta
	 * @created 21. August 2007
	 */
	class RecordEventListener implements AWTEventListener {
		private boolean lastWasPressed = false;

		/**
		 * Description of the Method
		 */
		public void reset() {
			lastWasPressed = false;
		}

		/**
		 * Description of the Method
		 * 
		 * @param e
		 *            Description of the Parameter
		 */
		public void eventDispatched(AWTEvent e) {
			boolean added = false;
			if (e instanceof KeyEvent) {
				KeyEvent k = (KeyEvent) e;
				if (k.getID() == KeyEvent.KEY_TYPED) {
					k.consume();
					return;
				}
				if (k.getKeyCode() == KeyEvent.VK_ESCAPE) {
					stopRecording();
					k.consume();
					return;
				}
				boolean pressed = k.getID() == KeyEvent.KEY_PRESSED;
				if (pressed && !lastWasPressed) {
					sheet.getTemporaryNotes().addElement(
							new Long(mp3.getPosition()));
				}
				if (!pressed && lastWasPressed) {
					sheet.getTemporaryNotes().addElement(
							new Long(mp3.getPosition()));
					added = true;
				}
				lastWasPressed = pressed;
				k.consume();
			} else if (e instanceof MouseEvent) {
				MouseEvent k = (MouseEvent) e;

				if (k.getID() == MouseEvent.MOUSE_MOVED
						|| k.getID() == MouseEvent.MOUSE_EXITED
						|| k.getID() == MouseEvent.MOUSE_ENTERED
						|| k.getID() == MouseEvent.MOUSE_CLICKED) {
					k.consume();
					return;
				}
				boolean pressed = k.getID() == MouseEvent.MOUSE_PRESSED;
				if (pressed && !lastWasPressed) {
					sheet.getTemporaryNotes().addElement(
							new Long(mp3.getPosition()));
				}
				if (!pressed && lastWasPressed) {
					sheet.getTemporaryNotes().addElement(
							new Long(mp3.getPosition()));
					added = true;
				}
				lastWasPressed = pressed;
				k.consume();
			}
			if (added) {
				if (sheet.getTemporaryNotes().size() >= 2 * recordLength) {
					stopRecording();
				}
			}
		}
	}

	/**
	 * Description of the Method
	 */
	public void startRecording() {
		sheet.getTemporaryNotes().clear();

		awt.reset();
		java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(awt,
				AWTEvent.MOUSE_EVENT_MASK);
		java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(awt,
				AWTEvent.MOUSE_MOTION_EVENT_MASK);
		java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(awt,
				AWTEvent.KEY_EVENT_MASK);
	}

	/**
	 * Description of the Method
	 */
	public void stopRecording() {
		mp3.interruptMP3();
		java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(awt);
		java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(awt);
		java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(awt);
	}

	private AWTEventListener awt2 = new AWTEventListener() {
		public void eventDispatched(AWTEvent e) {
			if (e instanceof MouseWheelEvent) {
				// bug: freezes
				// stopPlaying();
			}
			if (e instanceof KeyEvent) {
				KeyEvent k = (KeyEvent) e;
				if (k.getID() == KeyEvent.KEY_RELEASED) {
					return;
				}

				if (k.getID() == KeyEvent.KEY_TYPED) {
					char ch = k.getKeyChar();
					if (ch == 'p' || ch == 'P' || ch == ' ') {
						return;
					}
				}
				if ((k.isControlDown() || k.isAltDown() || k.isShiftDown())
						&& k.getKeyCode() == 0) {
					return;
				}
				stopPlaying();

				int c = k.getKeyCode();
				if (c == KeyEvent.VK_P || c == KeyEvent.VK_ESCAPE
						|| c == KeyEvent.VK_SPACE) {
					k.consume();
				}
			}
		}
	};

	/**
	 * Description of the Method
	 */
	public void startPlaying() {
		java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(awt2,
				AWTEvent.MOUSE_EVENT_MASK);
		java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(awt2,
				AWTEvent.MOUSE_MOTION_EVENT_MASK);
		java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(awt2,
				AWTEvent.KEY_EVENT_MASK);
		// java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(awt2,
		// AWTEvent.MOUSE_WHEEL_EVENT_MASK);
	}

	/**
	 * Description of the Method
	 */
	public void stopPlaying() {
		java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(awt2);
		java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(awt2);
		java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(awt2);
		mp3.interruptMP3();
	}

	Action playPage = new AbstractAction(I18.get("medit_play_page")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}
			playPageOrFrozen(0, false);
		}
	};
	Action playPageWithMIDI = new AbstractAction(
			I18.get("medit_play_page_midi")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}
			playPageOrFrozen(1, false);
		}
	};
	Action playPageWithMIDIAudio = new AbstractAction(
			I18.get("medit_play_page_midi_audio")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}
			playPageOrFrozen(2, false);
		}
	};

	Action playFrozen = new AbstractAction(I18.get("medit_copy_play")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}
			playPageOrFrozen(0, true);
		}
	};
	Action playFrozenWithMIDI = new AbstractAction(
			I18.get("medit_copy_play_midi")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}
			playPageOrFrozen(1, true);
		}
	};
	Action playFrozenWithMIDIAudio = new AbstractAction(
			I18.get("medit_copy_play_midi_audio")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}
			playPageOrFrozen(2, true);
		}
	};

	/**
	 * Description of the Method
	 * 
	 * @param mode
	 *            Description of the Parameter
	 * @param frozen
	 *            Description of the Parameter
	 */
	public void playPageOrFrozen(int mode, boolean frozen) {
		int i;
		int j;
		long inout[] = new long[2];
		long clicks[][] = null;
		if (frozen) {
			long in = sheet.getInSnapshot();
			long out = sheet.getOutSnapshot();
			if (in == out || in < 0 || out < 0) {
				return;
			}

			i = sheet.nextElement(sheet.toTimeline(Math.min(in, out)));
			j = sheet.nextElement(sheet.toTimeline(Math.max(in, out)));
			inout[0] = Math.min(in, out) * 1000L;
			inout[1] = Math.max(in, out) * 1000L;
			clicks = table.getSelection(i, j, new long[2], null, false);
		} else {
			i = table.getSelectionModel().getMinSelectionIndex();
			j = table.getSelectionModel().getMaxSelectionIndex();
			if (i < 0) {
				i = j = sheet.nextElement();
			}
			int ij[] = table.enlargeToPages(i, j);
			if (ij == null) {
				return;
			}
			i = ij[0];
			j = ij[1];
			clicks = table.getSelection(i, j, inout, null, false);
		}
		startPlaying();

		boolean midiEnabled = midiButton.isSelected();
		boolean mp3Enabled = mp3Button.isSelected();
		if (mode == 0) {
			mp3.setMIDIEnabled(midiEnabled);
			mp3.setAudioEnabled(mp3Enabled);
		} else if (mode == 1) {
			mp3.setMIDIEnabled(true);
			mp3.setAudioEnabled(false);
		} else if (mode == 2) {
			mp3.setMIDIEnabled(true);
			mp3.setAudioEnabled(true);
		}

		mp3.playSelection(inout[0], inout[1], clicks);
	}

	int recordLength = -1;
	Action recordSelection = new AbstractAction(I18.get("medit_record")) {
		private static final long serialVersionUID = 1L;
		long inout[] = new long[2];

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}

			int i = table.getSelectionModel().getMinSelectionIndex();
			if (i < 0) {
				JOptionPane.showMessageDialog(getTab(),
						"<html>" + I18.get("medit_record_error_msg"),
						I18.get("medit_record_error_title"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int j = table.getSelectionModel().getMaxSelectionIndex();

			long clicks[][] = table.getSelection(i, j, inout, null, false);
			inout[0] = Math.max(0, inout[0] - 2000000);
			inout[1] = -1;// inout[1] + 2000000;

			if (clicks != null) {
				recordLength = clicks.length;

				String defbase = prop.getProperty("record-timebase");
				int def = 1;
				if (defbase != null) {
					def = new Integer(defbase).intValue() - 1;
				}

				Object[] speed = { "100%", "50%", "33%", "25%" };
				String s = (String) JOptionPane.showInputDialog(
						tab,
						"<html>"
								+ MessageFormat.format(
										I18.get("medit_record_msg"),
										recordLength),
						I18.get("medit_record_title"),
						JOptionPane.INFORMATION_MESSAGE, null, speed,
						speed[def]);

				if (s == null || s.length() < 1) {
					return;
				}

				int timebase = 1;
				if (s.equals(speed[1])) {
					timebase = 2;
				}
				if (s.equals(speed[2])) {
					timebase = 3;
				}
				if (s.equals(speed[3])) {
					timebase = 4;
				}
				prop.setProperty("record-timebase", timebase + "");
				prop.store();

				// int ok = JOptionPane.showConfirmDialog(tab, "<html>" +
				// MessageFormat.format(I18.get("medit_record_msg"),
				// recordLength), I18.get("medit_record_title"),
				// JOptionPane.OK_CANCEL_OPTION);

				startRecording();
				mp3.playSelection(inout[0], inout[1], null, timebase);
			}
		}
	};

	Action undo = new AbstractAction(I18.get("medit_undo")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			if (lyrics.isEditable()) {
				lyrics.finishEditing();
			}
			table.undoRows();
			checkData(table, false, true);

		}
	};
	Action redo = new AbstractAction(I18.get("medit_redo")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			if (lyrics.isEditable()) {
				lyrics.finishEditing();
			}
			table.redoRows();
			checkData(table, false, true);
		}
	};

	/**
	 * Gets the undoAction attribute of the YassActions object
	 * 
	 * @return The undoAction value
	 */
	public Action getUndoAction() {
		return undo;
	}

	/**
	 * Gets the redoAction attribute of the YassActions object
	 * 
	 * @return The redoAction value
	 */
	public Action getRedoAction() {
		return redo;
	}

	long playAllStart = -1;
	long playAllClicks[][] = null;

	Action playAll = new AbstractAction(I18.get("medit_play_all")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			previewEdit(true);
			playAll(true);
		}
	};
	Action playAllFromHere = new AbstractAction(I18.get("medit_play_remainder")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			previewEdit(true);
			playAll(false);
		}
	};

	Action previewLib = new AbstractAction(I18.get("mlib_maximize")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			previewLib();
		}
	};
	Action fullscreen = new AbstractAction(I18.get("mlib_fullscreen")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			fullscreen();
		}
	};
	Action ratio43 = new AbstractAction(I18.get("mplayer_ratio")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			ratio43();
		}
	};
	Action pausePlayer = new AbstractAction(I18.get("mplayer_pause")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			pausePlayer();
		}
	};
	Action forwardPlayer = new AbstractAction(I18.get("mplayer_forward")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			forwardPlayer(20);
		}
	};
	Action endPlayer = new AbstractAction(I18.get("mplayer_forward")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			endPlayer(3);
		}
	};

	/**
	 * Description of the Method
	 */
	public void ratio43() {
		mp3.getPlaybackRenderer().setRatio(YassPlaybackRenderer.FOUR_TO_THREE);
		if (pausePosition < 0) {
			pausePosition = mp3.getPosition();
			mp3.interruptMP3();
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
			mp3.playSelection(pausePosition, -1, null);
			pausePosition = -1;
		}
	}

	private long pausePosition = -1;

	/**
	 * Description of the Method
	 */
	public void pausePlayer() {
		if (pausePosition >= 0) {
			mp3.getPlaybackRenderer().setPause(false);
			mp3.playSelection(pausePosition, -1, null);
			pausePosition = -1;
		} else {
			mp3.getPlaybackRenderer().setPause(true);
			pausePosition = mp3.getPosition();
			mp3.interruptMP3();
		}
	}

	private long lastForwardPressed = -1;

	/**
	 * Description of the Method
	 * 
	 * @param sec
	 *            Description of the Parameter
	 */
	public void forwardPlayer(int sec) {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastForwardPressed < 800) {
			return;
		}
		pausePosition = mp3.getPosition();
		pausePosition += sec * 1000000;
		lastForwardPressed = currentTime;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mp3.playSelection(pausePosition, -1, null);
				pausePosition = -1;
			}
		});
	}

	/**
	 * Description of the Method
	 * 
	 * @param sec
	 *            Description of the Parameter
	 */
	public void endPlayer(int sec) {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastForwardPressed < 800) {
			return;
		}
		pausePosition = mp3.getDuration();
		pausePosition -= sec * 1000000;
		lastForwardPressed = currentTime;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mp3.playSelection(pausePosition, -1, null);
				pausePosition = -1;
			}
		});
	}

	/**
	 * Description of the Method
	 */
	public void previewLib() {
		boolean debug = false;
		if (debug) {
			return;
		}

		boolean onoff = libMenu != null && libMenu.isVisible();
		if (onoff) {
			libMenu.setVisible(false);
			if (libtools != null) {
				libtools.setVisible(false);
			}
		} else if (libMenu != null) {
			libMenu.setVisible(true);
			if (libtools != null) {
				libtools.setVisible(true);
			}
		}
	}

	/**
	 * Description of the Method
	 */
	public void fullscreenPlayer_unused() {
		Component c = SwingUtilities.getRoot(main);
		if (!(c instanceof JFrame)) {
			return;
		}

		JFrame f = (JFrame) c;

		if (f.getExtendedState() == Frame.NORMAL) {
			playerMenu.setVisible(false);
			enterFullscreen();
		} else {
			playerMenu.setVisible(true);
			leaveFullscreen();
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @param onoff
	 *            Description of the Parameter
	 */
	public void previewEdit(boolean onoff) {
		if (onoff) {
			if (editMenu != null) {
				editMenu.setVisible(false);
			}
			if (edittools != null) {
				edittools.setVisible(false);
			}
		} else {
			if (editMenu != null) {
				editMenu.setVisible(true);
			}
			if (edittools != null) {
				edittools.setVisible(true);
			}
		}
	}

	/**
	 * Description of the Method
	 */
	public void fullscreen() {
		Component c = SwingUtilities.getRoot(main);
		if (!(c instanceof JFrame)) {
			return;
		}

		JFrame f = (JFrame) c;

		if (f.getExtendedState() == Frame.NORMAL) {
			enterFullscreen();
		} else {
			leaveFullscreen();
		}
		if (currentView == VIEW_EDIT) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					revalidateLyricsArea();
					sheet.repaint();
				}
			});
		}
		if (currentView == VIEW_SCREEN || currentView == VIEW_JUKEBOX) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Component c = SwingUtilities.getRoot(main);
					if (c instanceof JFrame) {
						JFrame f = (JFrame) c;
						screenMenu.setVisible(f.getExtendedState() == Frame.NORMAL);
						f.validate();
					}
				}
			});
		}
		if (currentView == VIEW_PLAYER) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Component c = SwingUtilities.getRoot(main);
					if (c instanceof JFrame) {
						JFrame f = (JFrame) c;
						playerscreenMenu.setVisible(f.getExtendedState() == Frame.NORMAL);
						f.validate();
					}
				}
			});
		}
	}

	/**
	 * Description of the Method
	 */
	public void leaveFullscreen() {
		Component c = SwingUtilities.getRoot(main);
		if (!(c instanceof JFrame)) {
			return;
		}

		JFrame f = (JFrame) c;
		// GraphicsEnvironment ge =
		// GraphicsEnvironment.getLocalGraphicsEnvironment();
		// GraphicsDevice device = ge.getDefaultScreenDevice();
		// device.setDisplayMode(dispModeOld);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		String w = prop.getProperty("frame-width");
		if (w == null) {
			w = dim.width >= 1000 ? "1000" : dim.width + "";
		} else if (new Integer(w).intValue() > dim.width) {
			w = dim.width + "";
		}
		String h = prop.getProperty("frame-height");
		if (h == null) {
			h = dim.height >= 600 ? "600" : dim.height + "";
		} else if (new Integer(h).intValue() > dim.height) {
			w = dim.height + "";
		}
		Point p = null;
		String x = prop.getProperty("frame-x");
		String y = prop.getProperty("frame-y");
		if (x != null && y != null)
			p = new Point(new Integer(x), new Integer(y));

		f.dispose();
		f.setUndecorated(false);

		f.setSize(new Dimension(new Integer(w), new Integer(h)));
		if (p != null) {
			f.setLocation(p);
		} else {
			f.setLocationRelativeTo(null);
		}

		f.setExtendedState(Frame.NORMAL);
		f.validate();
		f.setVisible(true);

		// GraphicsEnvironment ge =
		// GraphicsEnvironment.getLocalGraphicsEnvironment();
		// GraphicsDevice gd = ge.getDefaultScreenDevice();
		// gd.setFullScreenWindow(null);
	}

	DisplayMode dispModeOld = null;

	/**
	 * Description of the Method
	 */
	public void enterFullscreen() {
		Component c = SwingUtilities.getRoot(main);
		if (!(c instanceof JFrame)) {
			return;
		}

		JFrame f = (JFrame) c;
		// GraphicsEnvironment ge =
		// GraphicsEnvironment.getLocalGraphicsEnvironment();
		// GraphicsDevice device = ge.getDefaultScreenDevice();
		// dispModeOld = device.getDisplayMode();

		prop.setProperty("frame-width", f.getSize().width + "");
		prop.setProperty("frame-height", f.getSize().height + "");
		prop.setProperty("frame-x", f.getLocation().x + "");
		prop.setProperty("frame-y", f.getLocation().y + "");

		f.setVisible(false);
		f.dispose();
		f.setUndecorated(true);

		// device.setFullScreenWindow(this);
		// device.setDisplayMode(dispMode);
		f.setExtendedState(Frame.MAXIMIZED_BOTH);
		f.validate();
		f.setVisible(true);

		/*
		 * GraphicsEnvironment ge =
		 * GraphicsEnvironment.getLocalGraphicsEnvironment(); GraphicsDevice gd
		 * = ge.getDefaultScreenDevice(); boolean fullscreenSupported =
		 * gd.isFullScreenSupported(); if (!fullscreenSupported) { /
		 * frame.setMaximized... / frame.setUndecorated(true); return; }
		 * GraphicsConfiguration gc = gd.getDefaultConfiguration(); fsFrame =
		 * new Frame(gc); fsFrame.setLayout(new BorderLayout());
		 * fsFrame.add("Center", main); fsFrame.setUndecorated(true);
		 * gd.setFullScreenWindow(fsFrame); fsFrame.validate(); if
		 * (gd.isDisplayChangeSupported()) { try { gd.setDisplayMode(new
		 * DisplayMode(800, 600, 16, DisplayMode.REFRESH_RATE_UNKNOWN)); } catch
		 * (Exception e) {
		 * System.out.println("Display mode error for resolution 8000x800x16.");
		 * return; } }
		 */
	}

	/**
	 * Description of the Method
	 * 
	 * @param fromStart
	 *            Description of the Parameter
	 */
	public void playAll(boolean fromStart) {
		onePage();
		if (fromStart) {
			table.firstNote();
		} else {
			table.home();
		}
		sheet.setLyricsVisible(false);
		sheet.showBackground(true);
		if (playAllVideoCBI != null && playAllVideoCBI.isSelected()) {
			sheet.showVideo(true);
		}
		mp3.setLive(true);
		sheet.setLive(true);
		sheet.refreshImage();

		int i = table.getSelectionModel().getMinSelectionIndex();
		if (i < 0) {
			return;
		}

		long inout[] = new long[2];
		playAllClicks = table.getSelection(i, -1, inout, null, false);
		playAllStart = Math.max(0, inout[0] - 4000000);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				startPlaying();

				boolean mp3Enabled = mp3Button.isSelected();
				boolean midiEnabled = midiButton.isSelected();
				mp3.setMIDIEnabled(midiEnabled);
				mp3.setAudioEnabled(mp3Enabled);
				mp3.playSelection(playAllStart, -1, playAllClicks);
			}
		});
	}

	Action recordAll = new AbstractAction(I18.get("medit_record_all")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}

			table.clearSelection();
			startRecording();
			mp3.playAll(null);
		}
	};
	Action pasteRows = new AbstractAction(I18.get("medit_paste")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			table.pasteRows();

			snapshotButton.setSelected(false);
			sheet.showSnapshot(false);
			sheet.repaint();
		}
	};
	Action insertPageBreak = new AbstractAction(I18.get("medit_break")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			table.insertPageBreak(false);
		}
	};
	Action insertNote = new AbstractAction(I18.get("medit_add")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			table.insertNote();
		}
	};
	Action removeRows = new AbstractAction(I18.get("medit_remove")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			table.removeRows();
		}
	};
	Action absolute = new AbstractAction(I18.get("medit_align")) {
		private static final long serialVersionUID = 1L;

		// Align
		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}

			boolean state = sheet.isPanEnabled();

			if (table.getMultiSize() == 1) {
				state = !state;
				sheet.enablePan(state);

				sheet.update();
				revalidateLyricsArea();
				sheet.repaint();
			}
			if (alignCBI != null) {
				alignCBI.setState(state);
			}
		}
	};

	Action enableVideoPreview = new AbstractAction(
			I18.get("medit_playallvideo_toggle")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			// playAllVideoCBI
		}
	};
	Action enableMidi = new AbstractAction(I18.get("medit_midi_toggle")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();

			if (e.getSource() != midiButton) {
				midiButton.setSelected(!midiButton.isSelected());
			} else {
				mp3.setMIDIEnabled(midiButton.isSelected());
			}
			if (midiCBI != null) {
				midiCBI.setState(midiButton.isSelected());
			}
			sheet.setPaintHeights(midiButton.isSelected());
			table.zoomPage();
			updatePlayerPosition();
			sheet.repaint();
		}
	};
	Action enableAudio = new AbstractAction(I18.get("medit_audio_toggle")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() != mp3Button) {
				mp3Button.setSelected(!mp3Button.isSelected());
			} else {
				mp3.setAudioEnabled(mp3Button.isSelected());
			}
			if (audioCBI != null) {
				audioCBI.setState(mp3Button.isSelected());
			}
		}
	};
	Action enableClicks = new AbstractAction(I18.get("medit_clicks_toggle")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			mp3.setClicksEnabled(!mp3.isClicksEnabled());
			if (clicksCBI != null) {
				clicksCBI.setState(mp3.isClicksEnabled());
			}
		}
	};
	Action enableMic = new AbstractAction(I18.get("medit_mic_toggle")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			mp3.setCapture(!mp3.isCapture());
			if (micCBI != null) {
				micCBI.setState(mp3.isCapture());
			}
		}
	};
	Action enableVideoAudio = new AbstractAction(
			I18.get("tool_video_audio_toggle")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			video.muteVideo(!videoAudioButton.isSelected());
		}
	};

	Action enableLyrics = new AbstractAction(I18.get("mlib_lyrics_toggle")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			clearFilter();
			updateSongInfo(enableLyrics);

			songList.requestFocus();
		}
	};

	/**
	 * Description of the Method
	 */
	public void revalidateLyricsArea() {
		if (sheet.isLive()) {
			return;
		}
		int newh = sheet.getTopLine() - 30;

		// LYRICS POSITION
		String layout = prop.getProperty("editor-layout");
		if (layout == null) {
			layout = "East";
		}

		String lyricsWidthString = prop.getProperty("lyrics-width");
		String lyricsHeightString = prop.getProperty("lyrics-min-height");
		int lyricsWidth = new Integer(lyricsWidthString).intValue();
		int lyricsMinHeight = new Integer(lyricsHeightString).intValue();
		if (newh < lyricsMinHeight) {
			newh = lyricsMinHeight;
		}

		int h = lyrics.getBounds().height;
		if (h != newh) {
			if (layout.equals("East")) {
				lyrics.setBounds(500, 30, lyricsWidth, newh);
			} else if (layout.equals("West")) {
				lyrics.setBounds(0, 30, lyricsWidth, newh);
			}
			sheet.revalidate();
		}
	}

	/**
	 * Sets the relative attribute of the YassActions object
	 * 
	 * @param onoff
	 *            The new relative value
	 */
	public void setRelative(boolean onoff) {
		sheet.enablePan(onoff);
		if (alignCBI != null) {
			alignCBI.setState(onoff);
		}
		revalidateLyricsArea();
	}

	/**
	 * Gets the relative attribute of the YassActions object
	 * 
	 * @return The relative value
	 */
	public boolean isRelative() {
		return !sheet.isPanEnabled();
	}

	Action joinRows = new AbstractAction(I18.get("medit_join")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}
			table.joinRows();
		}
	};
	Action splitRows = new AbstractAction(I18.get("medit_split")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}
			table.splitRows();
		}
	};
	Action rollLeft = new AbstractAction(I18.get("medit_roll_left")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}
			table.rollLeft();
		}
	};
	Action rollRight = new AbstractAction(I18.get("medit_roll_right")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}
			table.rollRight();
		}
	};

	Action togglePageBreak = new AbstractAction(I18.get("medit_break")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			table.togglePageBreak();
		}
	};
	Action removePageBreak = new AbstractAction(I18.get("medit_break_remove")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int row = table.getSelectionModel().getMinSelectionIndex() - 1;
			if (row < 1) {
				return;
			}
			table.removePageBreak(true);
			table.setRowSelectionInterval(row, row);
		}
	};

	/**
	 * Description of the Method
	 */
	public void copyRows() {
		table.copyRows();
		if (sheet != null) {
			sheet.makeSnapshot();
			sheet.repaint();
		}
	}

	Action copyRows = new AbstractAction(I18.get("medit_copy")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			copyRows();

			snapshotButton.setSelected(true);
			sheet.showSnapshot(true);
			sheet.repaint();
		}
	};
	Action showCopiedRows = new AbstractAction(I18.get("medit_copy_show")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			if (sheet != null) {
				boolean onoff = !sheet.isSnapshotShown();
				if (e.getSource() != snapshotButton) {
					snapshotButton.setSelected(onoff);
				}
				sheet.showSnapshot(onoff);
				sheet.repaint();
			}
		}
	};
	Action removeCopy = new AbstractAction(I18.get("medit_copy_remove")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			if (sheet != null) {
				sheet.removeSnapshot();
				sheet.repaint();
			}
		}
	};
	Action prevPage = new AbstractAction(I18.get("medit_page_prev")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			stopPlaying();
			table.gotoPage(-1);
		}
	};
	Action nextPage = new AbstractAction(I18.get("medit_page_next")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			stopPlaying();
			table.gotoPage(1);
		}
	};
	Action lessPages = new AbstractAction(I18.get("medit_page_less")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			int k = table.getMultiSize();
			if (k > 2) {
				setRelative(false);
				YassTable.setZoomMode(YassTable.ZOOM_MULTI);
			} else {
				setRelative(true);
				YassTable.setZoomMode(YassTable.ZOOM_ONE);
			}
			table.addMultiSize(-1);
			table.zoomPage();
			lyrics.repaintLineNumbers();
		}
	};
	Action morePages = new AbstractAction(I18.get("medit_page_more")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			setRelative(false);
			YassTable.setZoomMode(YassTable.ZOOM_MULTI);
			table.addMultiSize(1);
			table.zoomPage();
			lyrics.repaintLineNumbers();
		}
	};
	Action onePage = new AbstractAction(I18.get("medit_page_one")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			onePage();
		}
	};

	/**
	 * Description of the Method
	 */
	public void onePage() {
		int row = table.getSelectionModel().getMinSelectionIndex();
		if (row < 0) {
			row = sheet.nextElement();
		}
		if (row < 0) {
			return;
		}

		YassRow r = table.getRowAt(row);
		while (r.isPageBreak() && row > 0) {
			r = table.getRowAt(--row);
		}
		if (row < 0) {
			return;
		}
		table.setRowSelectionInterval(row, row);
		setRelative(true);
		YassTable.setZoomMode(YassTable.ZOOM_ONE);
		table.setMultiSize(1);
		table.zoomPage();
		lyrics.repaintLineNumbers();
	}

	Action allPages = new AbstractAction(I18.get("medit_page_all")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			allPages();
		}
	};

	/**
	 * Description of the Method
	 */
	public void allPages() {
		setRelative(false);
		YassTable.setZoomMode(YassTable.ZOOM_MULTI);
		table.addMultiSize(table.getRowCount());
		table.zoomPage();
		lyrics.repaintLineNumbers();
	}

	Action shiftLeft = new AbstractAction(I18.get("medit_shift_left")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.shiftBeat(-1);
		}
	};
	Action shiftRight = new AbstractAction(I18.get("medit_shift_right")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.shiftBeat(+1);
		}
	};
	Action shiftLeftRemainder = new AbstractAction(
			I18.get("medit_shift_left_remainder")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.shiftRemainder(-1);
		}
	};
	Action shiftRightRemainder = new AbstractAction(
			I18.get("medit_shift_right_remainder")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.shiftRemainder(+1);
		}
	};
	Action home = new AbstractAction(I18.get("medit_home")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.home();
		}
	};
	Action end = new AbstractAction(I18.get("medit_end")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.end();
		}
	};
	Action first = new AbstractAction(I18.get("medit_first")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.firstNote();
		}
	};
	Action last = new AbstractAction(I18.get("medit_last")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.lastNote();
		}
	};
	Action prevVersion = new AbstractAction(I18.get("medit_versions_prev")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			nextVersion(-1);
		}
	};
	Action nextVersion = new AbstractAction(I18.get("medit_versions_next")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			nextVersion(1);
		}
	};
	Action openFile = new AbstractAction(I18.get("mlib_edit_file")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView == VIEW_EDIT) {
				if (cancelOpen()) {
					return;
				}
			}

			String filename = e.getActionCommand();
			if (filename == null || !new File(filename).exists()) {
				FileDialog fd = new FileDialog(
						(JFrame) SwingUtilities.getWindowAncestor(tab),
						I18.get("mlib_edit_file_msg"), FileDialog.LOAD);
				fd.setFile("*.txt");

				String defDir = table != null ? table.getDir() : null;
				if (defDir == null) {
					defDir = prop.getProperty("song-directory");
				}
				if (defDir != null) {
					fd.setDirectory(defDir);
				}

				fd.setVisible(true);
				if (fd.getFile() != null) {
					filename = fd.getDirectory() + File.separator
							+ fd.getFile();
				} else {
					filename = null;
				}
				fd.dispose();
			}
			if (filename == null) {
				return;
			}

			if (currentView == VIEW_EDIT) {
				closeAllTables();
				mp3.closeMP3();
			}

			openFile(filename);
			setView(VIEW_EDIT);
		}
	};
	Action editRecent = new AbstractAction(I18.get("mlib_edit_recent")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView == VIEW_EDIT) {
				if (cancelOpen()) {
					return;
				}
			}

			String filename = prop.getProperty("recent-files");
			if (filename == null || !new File(filename).exists()) {
				return;
			}

			if (currentView == VIEW_EDIT) {
				closeAllTables();
				mp3.closeMP3();
			}

			openFile(filename);
			setView(VIEW_EDIT);
		}
	};

	/**
	 * Description of the Method
	 * 
	 * @param filenames
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public boolean openFiles(String filenames) {
		if (filenames == null) {
			return false;
		}

		Vector<String> v = new Vector<>();
		StringTokenizer st = new StringTokenizer(filenames, ";");
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			v.addElement(s);
		}
		return openFiles(v);
	}

	boolean editorIsInDuetMode = false;
	String duetFile = null;

	/**
	 * Description of the Method
	 * 
	 * @param filenames
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public boolean openFiles(Vector<String> filenames) {
		return openFiles(filenames, false);
	}

	/**
	 * Description of the Method
	 * 
	 * @param filenames
	 *            Description of the Parameter
	 * @param duetMode
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public boolean openFiles(Vector<String> filenames, boolean duetMode) {
		if (filenames.size() > 10) {
			return false;
		}
		if (filenames.size() < 1) {
			return false;
		}

		Vector<String> filenames2 = new Vector<>();
		for (Enumeration<String> en = filenames.elements(); en
				.hasMoreElements();) {
			String filename = (String) en.nextElement();
			File file = new File(filename);
			if (!file.exists()) {
				continue;
			}
			if (file.isDirectory()) {
				FilenameFilter filter = new FilenameFilter() {
					public String songFileType = prop
							.getProperty("song-filetype");

					public boolean accept(File dir, String name) {
						return name.endsWith(songFileType);
					}
				};
				File txts[] = file.listFiles(filter);
				for (int i = 0; i < txts.length; i++) {
					if (isKaraokeFile(txts[i])) {
						filenames2.addElement(txts[i].getAbsolutePath());
					}
				}
			} else if (isKaraokeFile(file)) {
				filenames2.addElement(filename);
			}
		}
		filenames = filenames2;
		closeAll();

		int ncol = 0;
		boolean first = true;

		for (Enumeration<String> en = filenames.elements(); en
				.hasMoreElements();) {
			String filename = (String) en.nextElement();
			File file = new File(filename);

			YassTable mt = new YassTable();
			mt.loadFile(file.getAbsolutePath());
			int multi = mt.getMultiplayer();

			if (multi > 1) {
				JOptionPane.showMessageDialog(getTab(),
						"<html>" + I18.get("no_duet_edit_msg"),
						I18.get("no_duet_edit_title"),
						JOptionPane.ERROR_MESSAGE);
				if (false) {
					editorIsInDuetMode = true;
					duetFile = filename;
					Vector<String> filenames3 = splitFile(filename, false);
					openFiles(filenames3, true);
				}
				return false;
			} else {
				if (!duetMode) {
					editorIsInDuetMode = false;
				}

				int group = openTables.size();
				if (group < 0) {
					group = 0;
				}

				YassTable t = createNextTable();
				if (t == null) {
					continue;
				}
				t.removeAllRows();
				t.loadFile(file.getAbsolutePath());

				if (first) {
					table = t;
					first = false;
				}

				String v = t.getVersion();
				if (v == null || v.length() < 1) {
					t.setTableColor(getTableColor(0));
					openTables.remove(t);
					openTables.insertElementAt(t, group);
					table = t;
				} else {
					t.setTableColor(getTableColor(ncol++));
				}
			}
		}
		return true;
	}

	/**
	 * Description of the Method
	 * 
	 * @param filename
	 *            Description of the Parameter
	 */
	public void openFile(String filename) {
		closeAll();

		File file = new File(filename);
		if (!file.exists()) {
			return;
		}
		if (!isKaraokeFile(file)) {
			return;
		}

		YassTable t = new YassTable();
		t.loadFile(file.getAbsolutePath());
		int multi = t.getMultiplayer();

		if (multi < 2) {
			editorIsInDuetMode = false;

			YassTable ot = createNextTable();
			if (ot == null) {
				return;
			}

			ot.removeAllRows();
			ot.loadFile(file.getAbsolutePath());
			table = ot;
			return;
		}

		JOptionPane.showMessageDialog(getTab(),
				"<html>" + I18.get("no_duet_edit_msg"),
				I18.get("no_duet_edit_title"), JOptionPane.ERROR_MESSAGE);
		if (false) {
			editorIsInDuetMode = true;
			duetFile = filename;

			Vector<String> filenames = splitFile(filename, false);
			openFiles(filenames, false);
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @param filename
	 *            Description of the Parameter
	 */
	public void openFileWithVersions(String filename) {
		closeAll();

		File file = new File(filename);
		if (!file.exists()) {
			return;
		}

		YassTable ot = null;
		int ncol = 1;
		File files[] = file.getParentFile().listFiles();
		for (int i = 0; i < files.length; i++) {
			files[i].getAbsolutePath();
			if (!isKaraokeFile(files[i])) {
				continue;
			}

			YassTable t = createNextTable();
			if (t != null) {
				t.removeAllRows();
				t.loadFile(files[i].getAbsolutePath());

				String v = t.getVersion();
				if (v == null || v.length() < 1) {
					t.setTableColor(getTableColor(0));
					openTables.remove(t);
					openTables.insertElementAt(t, 0);
				} else {
					t.setTableColor(getTableColor(ncol++));
				}
				if (files[i].equals(file)) {
					ot = t;
				}
			}
		}
		table = ot;
	}

	/**
	 * Description of the Method
	 */
	public void openPlayer() {
		YassSongData sd = YassScreen.getSelectedSong();
		YassPlaybackRenderer renderer = YassScreen.getPlaybackRenderer();

		registerPlayerActions(renderer.getComponent());
		mp3.setPlaybackRenderer(renderer);

		String txt = sd.getKaraokeData().getPath();
		YassTable table = new YassTable();
		table.init(prop);
		table.loadFile(txt);
		if (table.getDir() != null && table.getMP3() != null) {
			mp3.openMP3(table.getDir() + File.separator + table.getMP3());
		}
		String vd = table.getVideo();
		if (video != null && vd != null) {
			video.setVideo(table.getDir() + File.separator + vd);
		}
		String bg = table.getBackgroundTag();
		if (bg != null) {
			File file = new File(table.getDir() + File.separator + bg);
			BufferedImage img = null;
			if (file.exists()) {
				try {
					img = YassUtils.readImage(file);
				} catch (Exception e) {
					img = null;
				}
			}
			mp3.setBackgroundImage(img);
		}

		YassSession session = table.createSession();
		while (session.getTrackCount() < YassScreen.MAX_PLAYERS) {
			session.addTrack();
		}
		if (playerDemoMode) {
			session.getTrack(0).setActive(true);
		}

		for (int t = 0; t < YassScreen.MAX_PLAYERS; t++) {
			session.getTrack(t).setActive(screen.isPlayerActive(t));
		}
		YassScreen.setSession(session);
		screen = YassScreen.getCurrentScreen();
		screen.setVisible(true);

		initCaptureDevices();
		mp3.setCapture(true);
		mp3.setDemo(playerDemoMode);

		main.removeAll();
		main.add("Center", renderer.getComponent());
		main.validate();
		renderer.init(session, YassScreen.getTheme(), prop);
		renderer.getComponent().requestFocus();

		mp3.playAll(null);
	}

	/**
	 * Description of the Method
	 */
	public void openJukebox() {
		screen = YassScreen.getCurrentScreen();
		screen.setVisible(true);
		YassSongData sd = YassScreen.getJukeboxSong();
		YassPlaybackRenderer renderer = YassScreen.getPlaybackRenderer();

		registerJukeboxActions(renderer.getComponent());
		mp3.setPlaybackRenderer(renderer);

		String txt = sd.getKaraokeData().getPath();
		YassTable table = new YassTable();
		table.init(prop);
		table.loadFile(txt);
		if (table.getDir() != null && table.getMP3() != null) {
			mp3.openMP3(table.getDir() + File.separator + table.getMP3());
		}
		String vd = table.getVideo();
		if (video != null && vd != null) {
			video.setVideo(table.getDir() + File.separator + vd);
		}
		String bg = table.getBackgroundTag();
		if (bg != null) {
			File file = new File(table.getDir() + File.separator + bg);
			BufferedImage img = null;
			if (file.exists()) {
				try {
					img = YassUtils.readImage(file);
				} catch (Exception e) {
					img = null;
				}
			}
			mp3.setBackgroundImage(img);
		}

		YassSession session = table.createSession();
		while (session.getTrackCount() < YassScreen.MAX_PLAYERS) {
			session.addTrack();
		}
		session.getTrack(0).setActive(true);

		YassScreen.setSession(session);

		mp3.setDemo(true);

		initCaptureDevices();
		mp3.setCapture(true);

		int startMillis = sd.getPreviewStart();
		if (startMillis <= 0) {
			startMillis = sd.getMedleyStart();
		}
		if (startMillis <= 0) {
			startMillis = sd.getStart();
		}
		if (startMillis <= 0) {
			startMillis = sd.getGap();
		}
		if (startMillis <= 0) {
			startMillis = 0;
		}

		int endMillis = sd.getMedleyEnd();
		if (endMillis <= 0) {
			endMillis = startMillis + 20000;
		}

		// endMillis = startMillis + 10000;

		main.removeAll();
		main.add("Center", renderer.getComponent());
		main.validate();
		renderer.init(session, YassScreen.getTheme(), prop);
		renderer.getComponent().requestFocus();
		mp3.playSelection(startMillis * 1000L, endMillis * 1000L, null);
	}

	/**
	 * Description of the Method
	 */
	public void initCaptureDevices() {
		for (int t = 0; t < YassScreen.MAX_PLAYERS; t++) {
			String dev = null;
			int ch = 0;
			try {
				dev = prop.getProperty("player" + t + "_device");
				ch = new Integer(prop.getProperty("player" + t + "_channel"))
						.intValue();
			} catch (Exception e) {
				dev = null;
				ch = 0;
			}
			mp3.setCapture(t, dev, ch);
		}
	}

	/**
	 * Description of the Method
	 */
	public void openPlayerCurrent() {
		Vector<?> v = songList.getSelectedFiles();
		if (v.size() != 1) {
			return;
		}

		String fn = (String) v.elementAt(0);
		table = new YassTable();
		table.init(prop);
		table.loadFile(fn);
		if (table.getDir() != null && table.getMP3() != null) {
			mp3.openMP3(table.getDir() + File.separator + table.getMP3());
		}
		String vd = table.getVideo();
		if (video != null && vd != null) {
			video.setVideo(table.getDir() + File.separator + vd);
		}
		String bg = table.getBackgroundTag();
		if (bg != null) {
			File file = new File(table.getDir() + File.separator + bg);
			BufferedImage img = null;
			if (file.exists()) {
				try {
					img = YassUtils.readImage(file);
				} catch (Exception e) {
					img = null;
				}
			}
			mp3.setBackgroundImage(img);
		}

		YassPlaybackRenderer renderer = mp3.getPlaybackRenderer();
		registerPlayerActions(renderer.getComponent());
		YassSession session = table.createSession();
		while (session.getTrackCount() < YassScreen.MAX_PLAYERS) {
			session.addTrack();
		}
		session.getTrack(0).setActive(true);

		renderer.setMessage(I18.get("player_current_demo"));
		mp3.setDemo(true);
		initCaptureDevices();
		mp3.setCapture(true);

		main.removeAll();
		main.add("Center", renderer.getComponent());
		main.validate();
		renderer.init(session, new YassTheme(), prop);
		renderer.getComponent().requestFocus();
		mp3.playAll(null);
	}

	/**
	 * Description of the Method
	 */
	public void openScreen() {
		screen.setVisible(true);
		screen.requestFocus();
		screen.repaint();
	}

	/**
	 * Description of the Method
	 */
	public void openEditor(boolean reload) {
		if (table == null) {
			table = firstTable();
		}
		if (table == null || table.getArtist() == null
				|| table.getTitle() == null) {
			return;
		}
		// System.out.println("open editor 1");

		updateVersionBox();

		// System.out.println("open editor 2");
		String recentFiles = null;
		for (Enumeration<YassTable> en = openTables.elements(); en
				.hasMoreElements();) {
			YassTable yt = (YassTable) en.nextElement();
			String fn = yt.getDir() + File.separator + yt.getFilename();
			if (recentFiles == null) {
				recentFiles = fn;
			} else {
				recentFiles = recentFiles + ";" + fn;
			}
		}
		if (recentFiles != null) {
			prop.setProperty("recent-files", recentFiles);
			prop.store();
		}
		if (table.getDir() != null && table.getMP3() != null) {
			mp3.openMP3(table.getDir() + File.separator + table.getMP3());
		}
		updateTitle();

		// System.out.println("open editor 3");

		sheet.setDuration(mp3.getDuration() / 1000.0);

		sheet.setActiveTable(table);
		String vd = table.getVideo();
		if (video != null && vd != null) {
			video.setVideo(table.getDir() + File.separator + vd);
		}
		String bg = table.getBackgroundTag();
		if (bg != null) {
			File file = new File(table.getDir() + File.separator + bg);
			BufferedImage img = null;
			if (file.exists()) {
				try {
					img = YassUtils.readImage(file);
				} catch (Exception e) {
					img = null;
				}
			}
			sheet.setBackgroundImage(img);
			mp3.setBackgroundImage(img);
		}

		// sheet.init();
		sheet.update();
		sheet.requestFocus();

		// System.out.println("open editor 4");
		
		setRelative(true);
		
		if (!reload) {
			YassTable.setZoomMode(YassTable.ZOOM_ONE);
			table.setMultiSize(1);

			table.firstNote();
			updatePlayerPosition();
			sheet.repaint();
		}

		// System.out.println("open editor 4.1");

		lyrics.setTable(table);
		lyrics.repaintLineNumbers();

		table.getColumnModel().getColumn(0).setPreferredWidth(10);
		table.getColumnModel().getColumn(0).setMaxWidth(10);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		table.getColumnModel().getColumn(2).setPreferredWidth(50);
		table.getColumnModel().getColumn(3).setPreferredWidth(50);

		if (!reload) table.resetUndo();
		table.addUndo();

		setSaved(true);
		setOpened(true);

		// System.out.println("open editor 5");

		// prevent unsetting saved icon
		isUpdating = true;
		updateLyrics();

		// updateMP3Info(table);
		// updateRaw();
		// updateCover();
		// updateBackground();
		// updateVideo();

		updateVideo();

		isUpdating = false;

		checkData(table, false, true);

		// System.out.println("open editor 6");

		for (Enumeration<YassTable> en = openTables.elements(); en
				.hasMoreElements();) {
			YassTable t = (YassTable) en.nextElement();
			songList.addOpened(t);
		}

		System.out.println("Searching for USB mic...");
		String device = prop.getProperty("control-mic");
		String[] devices = YassCaptureAudio.getDeviceNames();
		boolean found = false;
		for (int i = 0; i < devices.length; i++) {
			if (devices[i].equals(device)) {
				found = true;
				break;
			}
		}
		if (found) {
			YassSession session = table.createSession();
			session.addTrack();
			session.getTrack(0).setActive(true);
			YassScreen.setSession(session);

			mp3.setDemo(false);
			mp3.setCapture(0, device, 0);
			mp3.setCapture(true);
			sheet.init(session, null, null);
		} else
			sheet.init(null, null, null);
	}

	/**
	 * Description of the Method
	 */
	public void updateTitle() {
		Window root = SwingUtilities.getWindowAncestor(tab);
		if (root instanceof JFrame) {
			String t = table.getTitle();
			String a = table.getArtist();
			if (t == null || a == null) {
				((Frame) root).setTitle(I18.get("yass_title"));
			} else {
				((Frame) root).setTitle(a + " - " + t + " "
						+ I18.get("yass_title_appendix"));
			}

		}
	}

	/**
	 * Gets the karaokeFile attribute of the YassActions class
	 * 
	 * @param f
	 *            Description of the Parameter
	 * @return The karaokeFile value
	 */
	public static boolean isKaraokeFile(File f) {
		if (!f.getName().endsWith(".txt")) {
			return false;
		}

		try {
			unicode.UnicodeReader r = new unicode.UnicodeReader(
					new FileInputStream(f), null);
			BufferedReader inputStream = new BufferedReader(r);

			// BufferedReader inputStream = new BufferedReader(new
			// FileReader(f));
			String l;
			while ((l = inputStream.readLine()) != null) {
				int n = l.length();
				if (n < 1) {
					continue;
				}
				if (n > 6) {
					l = l.substring(0, 6).toUpperCase();
					if (l.startsWith("#TITLE")) {
						inputStream.close();
						return true;
					}
				}
				if (l.startsWith("#")) {
					continue;
				}
				inputStream.close();
				return false;
			}
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Description of the Method
	 * 
	 * @param artist
	 *            Description of the Parameter
	 * @param title
	 *            Description of the Parameter
	 * @param version
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public YassSong selectSong(String artist, String title, String version) {
		groups.setRowSelectionInterval(0, 0);
		groups.refresh();
		songList.setPreFilter(null);
		songList.filter(null);
		Vector<?> v = songList.findSong(artist, title, version);
		if (v == null || v.size() < 1) {
			songList.focusFirstVisible();
			return null;
		}
		YassSong song = (YassSong) v.firstElement();
		int i = songList.indexOf(song);
		if (i >= 0) {
			songList.changeSelection(i, 0, false, false);
		} else {
			songList.focusFirstVisible();
		}
		return song;
	}

	/**
	 * Gets the validKaraokeString attribute of the YassActions class
	 * 
	 * @param s
	 *            Description of the Parameter
	 * @return The validKaraokeString value
	 */
	public static boolean isValidKaraokeString(String s) {
		boolean hasTitle = false;
		boolean hasArtist = false;
		try {
			StringReader r = new StringReader(s);
			BufferedReader inputStream = new BufferedReader(r);

			String l;
			while ((l = inputStream.readLine()) != null) {
				if (l.startsWith("#")) {
					if (l.startsWith("#TITLE")) {
						hasTitle = true;
					}
					if (l.startsWith("#ARTIST")) {
						hasArtist = true;
					}
				} else {
					if (!hasTitle || !hasArtist) {
						inputStream.close();
						return false;
					}
					if (l.startsWith("E") && l.trim().equals("E")) {
						inputStream.close();
						return true;
					}
				}
			}
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Description of the Method
	 */
	public void updateLyrics() {
		String lang = table.getLanguage();
		if (lang == null) {
			lang = "EN_US";
		} else {
			lang = mapLanguage(lang);
		}
		lyrics.setLanguage(lang);

		updateGap();
	}

	/**
	 * Description of the Method
	 * 
	 * @param lang
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public String mapLanguage(String lang) {
		String dicts = prop.getProperty("dict-map");
		StringTokenizer st = new StringTokenizer(dicts, "|");
		while (st.hasMoreTokens()) {
			String language = st.nextToken();
			if (language.equals(lang)) {
				return st.nextToken();
			}
			st.nextToken();
		}
		return "EN-US";
	}

	/**
	 * Description of the Method
	 */
	public void undoLibraryChanges() {
		Vector<?> sel = songList.getSelectedSongs();
		if (sel.size() < 1) {
			return;
		}
		YassSong s = (YassSong) sel.firstElement();

		songList.undoSelection();
		songInfo.setSong(s);

		for (Enumeration<?> en = sel.elements(); en.hasMoreElements();) {
			s = (YassSong) en.nextElement();
			songInfo.removeBackup(s);
		}
	}

	/**
	 * Description of the Method
	 */
	public void undoAllLibraryChanges() {
		Vector<?> sel = songList.getSelectedSongs();
		if (sel.size() < 1) {
			return;
		}
		YassSong s = (YassSong) sel.firstElement();

		songList.undoAll();
		songInfo.setSong(s);

		File td = new File(prop.getProperty("temp-dir"));
		if (td.exists()) {
			YassUtils.deleteDir(td);
		}
		td.mkdirs();
	}

	/**
	 * Description of the Method
	 */
	public void updateVideo() {
		String vd = null;
		if (table != null) {
			String d = table.getDir();

			YassTableModel tm = (YassTableModel) table.getModel();
			YassRow r = tm.getCommentRow("VIDEO:");
			String v = r != null ? r.getComment() : null;
			if (v != null) {
				vd = d + File.separator + v;
			}
		}
		video.setVideo(vd);

		updateVideoGap();
	}

	/**
	 * Description of the Method
	 */
	public void reload() {
		for (Enumeration<YassTable> en = openTables.elements(); en
				.hasMoreElements();) {
			YassTable t = (YassTable) en.nextElement();
			String d = t.getDir();
			String f = t.getFilename();
			String m = t.getMP3();
			t.removeAllRows();
			t.loadFile(d + File.separator + f);
			t.setMP3(m);
		}
		updateVersionBox();
		sheet.init();
	}

	Action saveAsFile = new AbstractAction(I18.get("mlib_save_as")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			Frame f = new Frame();
			FileDialog fd = new FileDialog(f, I18.get("mlib_save_as_msg"),
					FileDialog.SAVE);
			fd.setVisible(true);
			if (fd.getFile() != null) {
				table.storeFile(fd.getDirectory() + File.separator
						+ fd.getFile());
			}
			fd.dispose();
			f.dispose();
		}
	};
	Action saveSong = new AbstractAction(I18.get("medit_save")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}

			int ok = JOptionPane.showConfirmDialog(tab,
					I18.get("medit_save_msg"), I18.get("medit_save_title"),
					JOptionPane.OK_CANCEL_OPTION);
			if (ok != JOptionPane.OK_OPTION) {
				return;
			}

			Vector<String> splitFiles = new Vector<>();
			for (Enumeration<YassTable> en = openTables.elements(); en
					.hasMoreElements();) {
				YassTable t = (YassTable) en.nextElement();

				String dir = t.getDir();
				String txt = t.getFilename();
				if (txt == null) {
					continue;
				}
				if (editorIsInDuetMode) {
					splitFiles.addElement(dir + File.separator + txt);
				} else {
					t.storeFile(dir + File.separator + txt);
				}
			}

			if (editorIsInDuetMode) {
				mergeFiles(splitFiles, false, duetFile);
			}
			saveSong.setEnabled(false);
		}
	};
	Action closeSong = new AbstractAction(I18.get("medit_close")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			closeSong();
		}
	};
	Action reloadSong = new AbstractAction(I18.get("medit_reload")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (cancelOpen())
				return;

			String filename = prop.getProperty("recent-files");
			if (filename == null || !new File(filename).exists())
				return;

			int sel = table.getSelectionModel().getMinSelectionIndex();
			if (sel < 0) 
				sel = sheet.nextElement();
			final int selectRow = sel;
			
			closeAllTables();
			mp3.closeMP3();
			openFile(filename);
			openEditor(selectRow < 0 ? false : true);			

			SwingUtilities.invokeLater(new Thread() {
				@Override
				public void run() {
					lyrics.setPreventFireUpdate(true);

					if (selectRow >= 0) {
						table.setRowSelectionInterval(selectRow, selectRow);
						table.zoomPage();
						table.updatePlayerPosition();
					}

					lyrics.setPreventFireUpdate(false);					
				}
			});
		}
	};
	Action closeLibrary = new AbstractAction(I18.get("mlib_close")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			closeAllTables();
			mp3.closeMP3();
			setLibraryLoaded(false);

			String filename = prop.getProperty("songlist-cache");
			File f = new File(filename);
			f.delete();
			filename = prop.getProperty("playlist-cache");
			f = new File(filename);
			f.delete();

			prop.remove("song-directory");
			prop.remove("playlist-directory");
			prop.remove("cover-directory");
			prop.remove("recent-files");

			songInfo.setSong(null);
			songInfoToggle.setSelected(false);
			songInfo.clear();
			playList.clear();
			updatePlayListBox();
			songList.load();
		}
	};

	Action createVersion = new AbstractAction(I18.get("medit_versions_new")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			createVersion();
		}
	};

	/**
	 * Description of the Method
	 */
	public void createVersion() {
		String fn = YassUtils.createVersion(tab, prop, table);
		if (fn != null) {
			openFile(fn);
		}
	}

	public boolean closeSong() {
		if (cancelOpen()) {
			return false;
		}
		closeAllTables();
		mp3.closeMP3();
		prop.remove("recent-files");
		editRecent.setEnabled(false);
		return true;
	}

	Action renameVersion = new AbstractAction(I18.get("medit_versions_rename")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			renameVersion();
		}
	};

	/**
	 * Description of the Method
	 */
	public void renameVersion() {
		String v = table.getVersion();
		if (v == null || v.trim().length() < 1) {
			return;
		}
		String newv = JOptionPane.showInputDialog(tab,
				I18.get("medit_versions_rename_title"), v);
		if (newv == null || newv.trim().length() < 1 || newv.equals(v)) {
			return;
		}

		String dir = table.getDir();
		String filename = table.getFilename();

		String title = YassSong.toFilename(table.getTitle());
		String artist = YassSong.toFilename(table.getArtist());

		String absFilename = dir + File.separator + artist + " - " + title
				+ " [" + newv + "].txt";
		File f = new File(absFilename);
		while (f.exists()) {
			newv = JOptionPane.showInputDialog(tab,
					I18.get("medit_versions_rename_error"), v);
			if (newv == null || newv.trim().length() < 1 || newv.equals(v)) {
				return;
			}
			absFilename = dir + File.separator + artist + " - " + title + " ["
					+ newv + "].txt";
			f = new File(absFilename);

		}
		table.setVersion(newv);
		table.storeFile(f.getAbsolutePath());

		File oldf = new File(dir + File.separator + filename);
		if (oldf.exists()) {
			oldf.delete();
		}

		updateVersionBox();
		songList.setVisible(true);
	}

	Action removeVersion = new AbstractAction(I18.get("medit_versions_remove")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			removeVersion();
		}
	};

	/**
	 * Description of the Method
	 */
	public void removeVersion() {
		if (YassUtils.removeVersion(tab, prop, table)) {
			sheet.removeTable(table);
			openTables.remove(table);
			table = firstTable();
			updateVersionBox();
			if (currentView == VIEW_LIBRARY) {
				songList.setVisible(true);
			} else {
				updateTitle();
				lyrics.setTable(table);
				sheet.setActiveTable(table);
				String vd = table.getVideo();
				if (video != null && vd != null) {
					video.setVideo(table.getDir() + File.separator + vd);
				}
				String bg = table.getBackgroundTag();
				if (bg != null) {
					File file = new File(table.getDir() + File.separator + bg);
					BufferedImage img = null;
					if (file.exists()) {
						try {
							img = YassUtils.readImage(file);
						} catch (Exception e) {
							img = null;
						}
					}
					sheet.setBackgroundImage(img);
					mp3.setBackgroundImage(img);
				}
				sheet.repaint();
				sheet.requestFocus();
			}
		}
	}

	Action setAsStandard = new AbstractAction(
			I18.get("medit_versions_remove_all")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			setAsStandard();
		}
	};

	/**
	 * Sets the asStandard attribute of the YassActions object
	 */
	public void setAsStandard() {
		String fn = YassUtils.setAsStandard(tab, prop, table);
		if (fn != null) {
			openFile(fn);
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @param fn
	 *            Description of the Parameter
	 */
	public void splitFile(String fn) {
		splitFile(fn, true);
	}

	/**
	 * Description of the Method
	 * 
	 * @param fn
	 *            Description of the Parameter
	 * @param gui
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public Vector<String> splitFile(String fn, boolean gui) {
		Vector<String> resVector = null;

		YassTable t = new YassTable();
		t.loadFile(fn);
		YassTableModel tm = (YassTableModel) t.getModel();
		Vector<?> data = tm.getData();

		int multi = t.getMultiplayer();
		if (multi < 2) {
			if (gui) {
				JOptionPane.showMessageDialog(getTab(),
						I18.get("mlib_tracks_split_error_msg"),
						I18.get("mlib_tracks_split_error_title"),
						JOptionPane.ERROR_MESSAGE);
			}
			return resVector;
		}

		// get player names
		String versions = "";
		YassRow pr = tm.getCommentRow("PLAYERS:");
		if (pr != null) {
			versions = pr.getComment();
		} else {
			for (int i = 1; i <= multi; i++) {
				versions = versions + " P" + i;
			}
		}
		if (gui) {
			String msg = I18.get("mlib_tracks_split_names");
			versions = JOptionPane.showInputDialog(getTab(), msg,
					versions.trim());
			if (versions == null) {
				return resVector;
			}
		}

		versions = versions.trim();
		String version[] = versions.split(" ");
		if (!gui) {
			for (int i = 0; i < version.length; i++) {
				version[i] = version[i] + "_TEMP";
			}
		}

		// create tables
		Vector<YassTable> tables = new Vector<>(version.length);
		for (int i = 0; i < version.length; i++) {
			YassTable res = new YassTable();
			tables.addElement(res);
		}

		// copy header
		int p = 0;
		for (Enumeration<YassTable> en = tables.elements(); en
				.hasMoreElements();) {
			YassTable res = (YassTable) en.nextElement();
			YassTableModel resmodel = (YassTableModel) res.getModel();
			Vector<YassRow> resdata = resmodel.getData();

			for (Enumeration<?> ren = data.elements(); ren.hasMoreElements();) {
				YassRow r = (YassRow) ren.nextElement();
				if (!r.isComment()) {
					break;
				}
				if (r.getCommentTag().equals("PLAYERS:")) {
					continue;
				}
				resdata.addElement(new YassRow(r));
			}
			res.setDir(t.getDir());
			res.setFilename(t.getFilename());
			res.setVersion(version[p++]);
			res.setBPM(t.getBPM());
			res.setGap(t.getGap());
		}

		// extract player notes
		Vector<YassRow> notes[] = new Vector[version.length];
		for (int i = 0; i < notes.length; i++) {
			notes[i] = new Vector<>();
		}
		p = 0;
		for (Enumeration<?> en = data.elements(); en.hasMoreElements();) {
			YassRow r = (YassRow) en.nextElement();
			if (r.isComment()) {
				continue;
			}
			if (r.isMultiplayer()) {
				String ps = r.getBeat().trim();
				try {
					p = new Integer(ps).intValue();
				} catch (Exception e) {
					p = 0;
				}
				continue;
			}
			if (r.isNote() && p > 0) {
				notes[p - 1].addElement(new YassRow(r));
			}
			if (r.isNote() && p == 0) {
				for (int i = 0; i < notes.length; i++) {
					notes[i].addElement(new YassRow(r));
				}
			}
			if (r.isPageBreak()) {
				for (int i = 0; i < notes.length; i++) {
					int lastn = notes[i].size() - 1;
					YassRow last = null;
					if (lastn >= 0) {
						last = (YassRow) notes[i].elementAt(lastn);
					}

					if (last != null && last.isNote()) {// skip other-player
														// page breaks
						notes[i].addElement(new YassRow(r));
					}
				}
				p = 0;
			}
			if (r.isEnd()) {
				for (int i = 0; i < notes.length; i++) {
					// skip other-player page breaks
					int lastn = notes[i].size() - 1;
					YassRow last = (YassRow) notes[i].elementAt(lastn);
					while (last.isPageBreak()) {
						notes[i].removeElementAt(lastn--);
						if (lastn == 0) {
							break;
						}
						last = (YassRow) notes[i].elementAt(lastn);
					}
					notes[i].addElement(new YassRow(r));
				}
			}
		}

		// add player notes
		p = 0;
		for (Enumeration<YassTable> en = tables.elements(); en
				.hasMoreElements();) {
			YassTable res = (YassTable) en.nextElement();
			YassTableModel resmodel = (YassTableModel) res.getModel();
			Vector<YassRow> resdata = resmodel.getData();

			Vector<?> pnotes = notes[p++];
			for (Enumeration<?> pen = pnotes.elements(); pen.hasMoreElements();) {
				YassRow r = (YassRow) pen.nextElement();
				resdata.addElement(r);
			}
		}

		// get first player gap
		double gap = t.getGap();

		// move first beat to zero
		for (Enumeration<YassTable> en = tables.elements(); en
				.hasMoreElements();) {
			YassTable res = (YassTable) en.nextElement();
			YassTableModel resmodel = (YassTableModel) res.getModel();
			Vector<?> resdata = resmodel.getData();

			boolean first = true;
			int minbeat = 0;
			for (Enumeration<?> ren = resdata.elements(); ren.hasMoreElements();) {
				YassRow r = (YassRow) ren.nextElement();
				if (r.isNote() && first) {
					minbeat = r.getBeatInt();
					double resgap = gap + minbeat / 4
							* (60 * 1000 / res.getBPM());
					res.setGap(resgap);
					first = false;
				}
				if (r.isNote()) {
					int b = r.getBeatInt();
					b = b - minbeat;
					r.setBeat(b);
				}
				if (r.isPageBreak()) {
					int b = r.getBeatInt();
					b = b - minbeat;
					r.setBeat(b);
					if (r.hasSecondBeat()) {
						b = r.getSecondBeatInt();
						b = b - minbeat;
						r.setSecondBeat(b);
					}
				}
			}
		}

		// store files
		p = 0;
		for (Enumeration<YassTable> en = tables.elements(); en
				.hasMoreElements();) {
			YassTable res = (YassTable) en.nextElement();
			YassTableModel resmodel = (YassTableModel) res.getModel();
			Vector<?> resdata = resmodel.getData();

			String v = res.getVersion();
			String absFilename = res.getDir() + File.separator
					+ res.getArtist() + " - " + res.getTitle() + " [" + v
					+ "].txt";
			File f = new File(absFilename);
			if (f.exists()) {
				if (gui) {
					JOptionPane.showMessageDialog(getTab(), MessageFormat
							.format(I18
									.get("mlib_tracks_split_names_error_msg"),
									v), I18
							.get("mlib_tracks_split_names_error_title"),
							JOptionPane.ERROR_MESSAGE);
					return resVector;
				} else {
					f.delete();
				}
			}
		}

		resVector = new Vector<>(tables.size());
		for (Enumeration<YassTable> en = tables.elements(); en
				.hasMoreElements();) {
			YassTable res = (YassTable) en.nextElement();
			YassTableModel resmodel = (YassTableModel) res.getModel();
			Vector<?> resdata = resmodel.getData();

			String v = res.getVersion();
			String absFilename = res.getDir() + File.separator
					+ res.getArtist() + " - " + res.getTitle() + " [" + v
					+ "].txt";
			File f = new File(absFilename);
			res.setFilename(res.getArtist() + " - " + res.getTitle() + " [" + v
					+ "].txt");
			res.storeFile(f.getAbsolutePath());
			resVector.addElement(f.getAbsolutePath());
		}

		if (gui) {
			// update songlist
			String songdir = t.getDir();
			File folderfile = new File(songdir).getParentFile();

			String root = prop.getProperty("song-directory");
			File rootfile = new File(root);

			String folder = "";
			if (!folderfile.equals(rootfile)) {
				folder = folderfile.getName();
			}

			int rows[] = songList.getSelectedRows();
			YassSongListModel sm = (YassSongListModel) songList.getModel();
			int j = rows[rows.length - 1] + 1;
			for (Enumeration<YassTable> en = tables.elements(); en
					.hasMoreElements();) {
				YassTable res = (YassTable) en.nextElement();
				YassTableModel resmodel = (YassTableModel) res.getModel();
				Vector<?> resdata = resmodel.getData();

				YassSong s = new YassSong(res.getDir(), folder,
						res.getFilename(), "", "");
				songList.loadSongDetails(s, new YassTable());
				sm.getData().insertElementAt(s, j++);
				songList.getUnfilteredData().addElement(s);
			}
			sm.fireTableDataChanged();
			songList.storeCache();
			setProgress(MessageFormat.format(I18.get("lib_msg"), sm.getData()
					.size()), "");

			songList.clearSelection();
			songList.addRowSelectionInterval(j, j);
		}

		return resVector;
	}

	/**
	 * Description of the Method
	 * 
	 * @param fn
	 *            Description of the Parameter
	 */
	public void mergeFiles(Vector<?> fn) {
		mergeFiles(fn, true, null);
	}

	/**
	 * Description of the Method
	 * 
	 * @param filenames
	 *            Description of the Parameter
	 * @param gui
	 *            Description of the Parameter
	 * @param resfilename
	 *            Description of the Parameter
	 */
	public void mergeFiles(Vector<?> filenames, boolean gui, String resfilename) {
		if (filenames.size() < 2) {
			return;
		}

		Vector<YassTable> tables = new Vector<>(filenames.size());
		for (Enumeration<?> en = filenames.elements(); en.hasMoreElements();) {
			String fn = (String) en.nextElement();
			YassTable t = new YassTable();
			t.loadFile(fn);
			tables.addElement(t);
		}

		// remove gap in all tables
		for (Enumeration<YassTable> en = tables.elements(); en
				.hasMoreElements();) {
			YassTable t = (YassTable) en.nextElement();

			double gap = t.getGap();
			double bpm = t.getBPM();
			double beatgap = gap * 4 / (60 * 1000 / bpm);

			YassTableModel tm = (YassTableModel) t.getModel();
			Vector<?> data = tm.getData();
			for (Enumeration<?> ren = data.elements(); ren.hasMoreElements();) {
				YassRow r = (YassRow) ren.nextElement();
				if (r.isNote()) {
					int b = r.getBeatInt();
					b = b + (int) (beatgap + .5);
					r.setBeat(b);
				}
				if (r.isPageBreak()) {
					int b = r.getBeatInt();
					b = b + (int) (beatgap + .5);
					r.setBeat(b);
					if (r.hasSecondBeat()) {
						b = r.getSecondBeatInt();
						b = b + (int) (beatgap + .5);
						r.setSecondBeat(b);
					}
				}
			}
		}

		// get first note
		int minbeat = Integer.MAX_VALUE;
		YassTable mintable = null;
		for (Enumeration<YassTable> en = tables.elements(); en
				.hasMoreElements();) {
			YassTable t = (YassTable) en.nextElement();

			YassRow r = t.getFirstNote();
			int b = r.getBeatInt();
			if (minbeat > b) {
				minbeat = b;
				mintable = t;
			}
		}

		// @todo does not work with different bpm
		double mingap = minbeat * 4 / (60 * 1000 / mintable.getBPM());

		// move first beat to zero
		for (Enumeration<YassTable> en = tables.elements(); en
				.hasMoreElements();) {
			YassTable t = (YassTable) en.nextElement();

			YassTableModel tm = (YassTableModel) t.getModel();
			Vector<?> data = tm.getData();
			for (Enumeration<?> ren = data.elements(); ren.hasMoreElements();) {
				YassRow r = (YassRow) ren.nextElement();
				if (r.isNote()) {
					int b = r.getBeatInt();
					b = b - minbeat;
					r.setBeat(b);
				}
				if (r.isPageBreak()) {
					int b = r.getBeatInt();
					b = b - minbeat;
					r.setBeat(b);
					if (r.hasSecondBeat()) {
						b = r.getSecondBeatInt();
						b = b - minbeat;
						r.setSecondBeat(b);
					}
				}
			}
		}

		// extract pages
		Vector<Vector<YassPage>> tablepages = new Vector<>(
				tables.size());
		for (Enumeration<YassTable> en = tables.elements(); en
				.hasMoreElements();) {
			YassTable t = (YassTable) en.nextElement();
			Vector<YassPage> pages = t.getPages();
			tablepages.addElement(pages);
		}

		// sort in order of appeareance
		Vector<YassPage> orderedHeads = new Vector<>(tablepages.size());
		for (Enumeration<Vector<YassPage>> en = tablepages.elements(); en
				.hasMoreElements();) {
			Vector<YassPage> pages = (Vector<YassPage>) en.nextElement();
			if (pages.isEmpty()) {
				continue;
			}
			orderedHeads.addElement(pages.firstElement());
		}
		Collections.sort(orderedHeads);

		tables.clear();
		tablepages.clear();
		String playerTag = "PLAYERS:";
		String playerNames = "";
		for (Enumeration<?> en = orderedHeads.elements(); en.hasMoreElements();) {
			YassPage p = (YassPage) en.nextElement();
			YassTable t = p.getTable();
			tables.addElement(t);
			Vector<YassPage> pages = t.getPages();
			tablepages.addElement(pages);

			String v = t.getVersion();
			if (!gui) {
				v = v.replaceAll("_TEMP", "");
			}
			playerNames += " " + v;
		}
		YassRow playersRow = new YassRow("#", playerTag, playerNames.trim(),
				"", "");

		// copy header
		YassTable res = new YassTable();
		res.init(prop);
		YassTableModel resmodel = (YassTableModel) res.getModel();
		Vector<YassRow> resdata = resmodel.getData();

		YassTableModel minmodel = (YassTableModel) mintable.getModel();
		Vector<?> mindata = minmodel.getData();
		for (Enumeration<?> en = mindata.elements(); en.hasMoreElements();) {
			YassRow r = (YassRow) en.nextElement();
			if (!r.isComment()) {
				break;
			}
			resdata.addElement(new YassRow(r));
		}
		resdata.addElement(playersRow);
		res.setDir(mintable.getDir());

		try {

			boolean isEmpty = false;
			while (!isEmpty) {

				// get next page
				int minpagebeat = Integer.MAX_VALUE;
				YassPage minpage = null;
				for (Enumeration<Vector<YassPage>> en = tablepages.elements(); en
						.hasMoreElements();) {
					Vector<YassPage> pages = (Vector<YassPage>) en
							.nextElement();
					if (pages.isEmpty()) {
						continue;
					}

					YassPage p = (YassPage) pages.firstElement();
					int min = p.getMinBeat();
					if (minpage == null || minpagebeat > min) {
						minpagebeat = min;
						minpage = p;
					}
				}

				// get intersecting page
				Vector<YassPage> intersecting = new Vector<>();
				for (Enumeration<Vector<YassPage>> en = tablepages.elements(); en
						.hasMoreElements();) {
					Vector<?> pages = (Vector<YassPage>) en.nextElement();
					if (pages.isEmpty()) {
						continue;
					}

					YassPage p = (YassPage) pages.firstElement();
					if (p == minpage) {
						pages.removeElement(p);
						continue;
					}
					if (minpage.intersects(p)) {
						intersecting.addElement(p);
						pages.removeElement(p);
					}
				}

				// combine pages, if equal
				int matching = 0;
				for (Enumeration<YassPage> en = intersecting.elements(); en
						.hasMoreElements();) {
					YassPage p = (YassPage) en.nextElement();
					if (minpage.matches(p)) {
						matching++;
					}
				}
				boolean combineTracks = matching > 0
						&& matching == intersecting.size();

				// add pages
				int player = tables.indexOf(minpage.getTable()) + 1;
				if (!combineTracks) {
					resdata.addElement(new YassRow("P", player + "", "", "", ""));
				} else {
					resdata.addElement(new YassRow("P", "0", "", "", ""));
				}
				YassRow pagebreak = null;
				Vector<?> rows = minpage.getRows();
				for (Enumeration<?> en = rows.elements(); en.hasMoreElements();) {
					YassRow r = (YassRow) en.nextElement();
					if (r.isNote()) {
						resdata.addElement(new YassRow(r));
					}
					if (r.isPageBreak()) {
						pagebreak = r;
					}
				}

				if (!combineTracks) {
					for (Enumeration<YassPage> en = intersecting.elements(); en
							.hasMoreElements();) {
						YassPage p = (YassPage) en.nextElement();
						player = tables.indexOf(p.getTable()) + 1;
						resdata.addElement(new YassRow("P", player + "", "",
								"", ""));
						rows = p.getRows();
						for (Enumeration<?> ren = rows.elements(); ren
								.hasMoreElements();) {
							YassRow r = (YassRow) ren.nextElement();
							if (r.isNote()) {
								resdata.addElement(new YassRow(r));
							}
							if (pagebreak == null && r.isPageBreak()) {
								pagebreak = r;
							}
						}
					}
				}

				isEmpty = true;
				for (Enumeration<Vector<YassPage>> en = tablepages.elements(); en
						.hasMoreElements();) {
					Vector<YassPage> pages = (Vector<YassPage>) en
							.nextElement();
					if (!pages.isEmpty()) {
						isEmpty = false;
						break;
					}
				}

				if (!isEmpty && pagebreak != null) {
					resdata.addElement(new YassRow(pagebreak));
				}
			}

			resdata.addElement(new YassRow("E", "", "", "", ""));

			// store file
			String absFilename = null;
			String version = null;
			if (resfilename != null) {
				absFilename = resfilename;
			} else {
				if (gui) {
					version = "MULTI";
					String msg = I18.get("mlib_tracks_merge_name");
					version = JOptionPane.showInputDialog(getTab(), msg,
							version);
					if (version != null) {
						absFilename = res.getDir() + File.separator
								+ res.getArtist() + " - " + res.getTitle()
								+ " [" + version + "].txt";
					} else {
						absFilename = res.getDir() + File.separator
								+ res.getArtist() + " - " + res.getTitle()
								+ ".txt";
					}
				}
			}
			System.out.println(absFilename);
			if (absFilename == null) {
				return;
			}

			File f = new File(absFilename);
			if (f.exists()) {
				if (gui) {
					JOptionPane.showMessageDialog(getTab(),
							I18.get("mlib_tracks_merge_name_error_msg"),
							I18.get("mlib_tracks_merge_name_error_title"),
							JOptionPane.ERROR_MESSAGE);
					return;
				} else {
					f.delete();
				}
			}
			String fname = f.getName();
			res.setFilename(fname);
			if (version != null) {
				res.setVersion(version);
			}
			res.storeFile(absFilename);

			if (gui) {
				// update songlist
				String songdir = res.getDir();
				File folderfile = new File(songdir).getParentFile();

				String root = prop.getProperty("song-directory");
				File rootfile = new File(root);

				String folder = "";
				if (!folderfile.equals(rootfile)) {
					folder = folderfile.getName();
				}

				YassSong s = new YassSong(res.getDir(), folder,
						res.getFilename(), "", "");
				songList.loadSongDetails(s, new YassTable());

				int rows[] = songList.getSelectedRows();
				YassSongListModel sm = (YassSongListModel) songList.getModel();
				int j = rows[rows.length - 1] + 1;
				sm.getData().insertElementAt(s, j);
				songList.getUnfilteredData().addElement(s);

				sm.fireTableDataChanged();
				songList.storeCache();
				setProgress(MessageFormat.format(I18.get("lib_msg"), sm
						.getData().size()), "");

				songList.clearSelection();
				songList.addRowSelectionInterval(j, j);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	Action importText = new AbstractAction("Import From Clipboard") {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			try {
				Clipboard system = Toolkit.getDefaultToolkit()
						.getSystemClipboard();
				String trstring = (String) (system.getContents(this)
						.getTransferData(DataFlavor.stringFlavor));
				StringTokenizer st = new StringTokenizer(trstring, "\n");
				YassTable t = new YassTable();
				t.init(prop);
				while (st.hasMoreTokens()) {
					String s = st.nextToken();
					if (!t.addRow(s)) {
						break;
					}
				}
				String fn = YassImport.importTable(songList, prop, t);
				if (fn != null) {
					openFile(fn);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	Action newFile = new AbstractAction(I18.get("mlib_new")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			createNewSong(null);
		}
	};

	Action removeSong = new AbstractAction(I18.get("mlib_remove")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songList.removeSelectedSongs();
		}
	};

	/**
	 * Description of the Method
	 */
	public void filterLibrary() {
		String s = filterEditor.getText();
		if (s.equals(I18.get("tool_lib_find_empty"))) {
			s = "";
		}
		filterEditor.setForeground(Color.gray);
		songList.filterLyrics(filterAll.isSelected());
		songInfo.setBold(s);
		songList.filter(s);
	}

	Action clearFilter = new AbstractAction(I18.get("mlib_find_clear")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView != VIEW_LIBRARY) {
				return;
			}
			clearFilter();
			filterLibrary();
		}
	};
	Action filterLibrary = new AbstractAction(I18.get("mlib_find")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			startFilterLibrary();
		}
	};

	/**
	 * Description of the Method
	 */
	public void startFilterLibrary() {
		if (currentView != VIEW_LIBRARY) {
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				filterEditor.requestFocus();
			}
		});
	}

	Action gotoLibrary = new AbstractAction(I18.get("medit_lib")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView == VIEW_LIBRARY) {
				return;
			}

			if (cancelOpen()) {
				return;
			}
			YassTable lastTable = table;
			closeAllTables();
			mp3.closeMP3();
			video.closeVideo();
			setView(VIEW_LIBRARY);
			if (!songList.isLoaded()) {
				songList.load();
				songList.gotoSong(lastTable);
			} else {
				YassSong s = songList.gotoSong(lastTable);
				if (s != null) {
					songList.loadSongDetails(s, new YassTable());
				}
				songList.repaint();
			}
		}
	};
	Action gotoLibraryFromPlayer = new AbstractAction(I18.get("medit_lib")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			pausePosition = -1;
			pausePlayer();

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					YassTable lastTable = table;
					closeAllTables();
					mp3.closeMP3();
					video.closeVideo();

					setView(VIEW_LIBRARY);
					songList.gotoSong(lastTable);
					songList.repaint();
				}
			});
		}
	};
	Action gotoPrevJukeboxFromPlayer = new AbstractAction(I18.get("medit_lib")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			pausePosition = -2;
			mp3.interruptMP3();
		}
	};
	Action gotoNextJukeboxFromPlayer = new AbstractAction(I18.get("medit_lib")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			pausePosition = -1;
			mp3.interruptMP3();
		}
	};
	Action gotoLibraryFromScreen = new AbstractAction(I18.get("medit_lib")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			screen.gotoScreen("library");
		}
	};

	Action activatePlayer1 = new AbstractAction(I18.get("player_start1")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			activatePlayer(0);
		}
	};
	Action activatePlayer2 = new AbstractAction(I18.get("player_start2")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			activatePlayer(1);
		}
	};
	Action activatePlayer3 = new AbstractAction(I18.get("player_start3")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			activatePlayer(2);
		}
	};

	/**
	 * Description of the Method
	 * 
	 * @param i
	 *            Description of the Parameter
	 */
	public void activatePlayer(int i) {
		YassSession session = mp3.getPlaybackRenderer().getSession();
		if (session == null) {
			return;
		}

		mp3.setDemo(false);
		mp3.getPlaybackRenderer().setMessage(null);

		// if (!session.getTrack(i).isActive()) {
		session.getTrack(i).setActive(true);
		System.out.println("track activated: " + i);
		mp3.getPlaybackRenderer().init();
		// }
	}

	Action firstScreen = new AbstractAction(I18.get("screen_first")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			pausePosition = -1;
			pausePlayer();

			if (YassScreen.getCurrentScreen().getID().equals("playsong")) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						mp3.closeMP3();
						video.closeVideo();
					}
				});
			}
			gotoScreen("start");
		}
	};

	private String gotoID = null;

	Action loadScreenSongs = new AbstractAction(I18.get("screen_start_load")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			YassScreen.getCurrentScreen().loadSongs();
		}
	};

	/**
	 * Description of the Method
	 * 
	 * @param id
	 *            Description of the Parameter
	 */
	public void gotoScreen(String id) {
		gotoID = id;

		if (gotoID.equals("loading")) {
			loadJukeboxData(true);
			return;
		}

		screen.hide();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				YassScreen.setCurrentScreen(gotoID);

				if (gotoID.equals("playsong")) {
					setView(VIEW_PLAYER);
				} else if (gotoID.equals("jukebox")) {
					setView(VIEW_JUKEBOX);
				} else if (gotoID.equals("exit")) {
					exit();
				} else if (gotoID.equals("library")) {
					YassTable lastTable = table;
					closeAllTables();

					YassScreen.getTheme().interruptAll();
					YassScreen.setSongData(null);
					YassScreen.setGroupData(null);
					setView(VIEW_LIBRARY);
					songList.requestFocus();
					songList.gotoSong(lastTable);
					songList.repaint();
				} else {
					setView(VIEW_SCREEN);
				}
				YassScreen.finishGotoScreen();
			}
		});
	}

	Action gotoScore = new AbstractAction(I18.get("mlib_score")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			Vector<?> v = songList.getSelectedFiles();
			if (v.size() != 1) {
				return;
			}

			String fn = (String) v.elementAt(0);
			table = new YassTable();
			table.init(prop);
			table.loadFile(fn);
			YassScreen.setSession(table.createSession());
			YassScreen.setCurrentScreen("viewscore");
			setView(VIEW_SCREEN);
		}
	};
	Action gotoPlayer = new AbstractAction(I18.get("mlib_player")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView == VIEW_EDIT) {
				closeAllTables();
				mp3.closeMP3();
				video.closeVideo();
			}

			playerDemoMode = false;
			YassScreen.setCurrentScreen("start");
			YassScreen.setContinue(false);
			setView(VIEW_SCREEN);
			loadJukeboxData(false);
		}
	};

	private static boolean forceJukeboxRefresh = false;

	/**
	 * Description of the Method
	 * 
	 * @param force
	 *            Description of the Parameter
	 */
	public void loadJukeboxData(boolean force) {
		forceJukeboxRefresh = force;
		YassScreen.setLoading(true);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (forceJukeboxRefresh) {
					new YassLibOptions(prop, YassActions.this, songList, mp3);
					prop.store();

					String songdir = prop.getProperty("song-directory");
					if (songdir == null || !new File(songdir).exists()) {
						exit();
					}
					refreshLibrary();
				} else {
					YassScreen.setSongData(songList.getSongData());
					YassScreen.setGroupData(groups.toScreenGroups());
					YassScreen.setLoading(false);
				}
			}
		});
	}

	Action gotoPlayerSelectedSong = new AbstractAction(
			I18.get("mlib_player_current")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView == VIEW_EDIT) {
				closeAllTables();
				mp3.closeMP3();
				video.closeVideo();
			}

			playerDemoMode = false;
			setView(VIEW_PLAY_CURRENT);
		}
	};
	Action gotoPlayerDemo = new AbstractAction(I18.get("mlib_player_demo")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentView == VIEW_EDIT) {
				closeAllTables();
				mp3.closeMP3();
				video.closeVideo();
			}

			playerDemoMode = true;
			setView(VIEW_PLAY_CURRENT);
		}
	};

	/**
	 * Description of the Method
	 * 
	 * @param karname
	 *            Description of the Parameter
	 */
	public void createNewSong(String karname) {
		createNewSong(karname, false);

	}

	/**
	 * Description of the Method
	 * 
	 * @param karname
	 *            Description of the Parameter
	 * @param standAlone
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public String createNewSong(String karname, boolean standAlone) {
		String songdir = null;
		if (standAlone) {
			if (karname != null) {
				File kar = new File(karname);
				songdir = kar.getParent();
			}
		} else {
			songdir = YassUtils.getSongDir(table, prop);
		}
		if (songdir == null) {
			songdir = "";
		}

		CreateSongWizard wiz = new CreateSongWizard(tab);
		wiz.setValue("songdir", songdir);
		wiz.setValue("folder", "");
		wiz.setValue("filename", "");
		wiz.setValue("melody", karname);
		wiz.setValue("creator", prop.getProperty("creator"));
		wiz.setValue("genre", "Other");
		wiz.setValue("edition", "");
		wiz.setValue("language", "Other");
		wiz.setValue("bpm", "300");
		wiz.setValue("lyrics", "");
		wiz.setValue("starteditor", "true");
		wiz.setValue("encoding", "");

		wiz.setValue("languages", prop.getProperty("language-tag"));
		wiz.setValue("languages-more", prop.getProperty("language-more-tag"));
		wiz.setValue("genres", prop.getProperty("genre-tag"));
		wiz.setValue("genres-more", prop.getProperty("genre-more-tag"));
		wiz.setValue("editions", prop.getProperty("edition-tag"));
		wiz.setValue("hyphenations", prop.getProperty("hyphenations"));
		wiz.show();

		if (wiz.getReturnCode() != Wizard.FINISH_RETURN_CODE) {
			return null;
		}

		mp3.closeMP3();

		Hashtable<?, ?> hash = wiz.getValues();
		String file = YassUtils.createSong(tab, hash, prop);

		boolean starteditor = hash.get("starteditor").equals("true");
		if (!standAlone) {
			if (file != null) {
				int k = file.lastIndexOf(File.separator);
				String filedir = file.substring(0, k);
				String folder = (String) hash.get("folder");
				String name = file.substring(k + 1);
				songList.addSong(filedir, folder, name);
				if (starteditor) {
					openFile(file);
					songList.gotoSong(table);
					setView(VIEW_EDIT);
				}
			}
			return file;
		} else {
			if (file != null && starteditor) {
				openFile(file);
				songList.gotoSong(table);
				setView(VIEW_EDIT);
				return file;
			}
		}
		return null;
	}

	/*
	 * Action importFiles = new AbstractAction(I18.get("mlib_import")) { public
	 * void actionPerformed(ActionEvent e) { String songdir =
	 * YassUtils.getSongDir(table, prop); if (songdir == null) { return; }
	 * JFileChooser fileChooser = new JFileChooser(".");
	 * fileChooser.setMultiSelectionEnabled(true);
	 * fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	 * fileChooser.setDialogTitle(I18.get("mlib_import_title")); JPanel pan =
	 * new JPanel(new BorderLayout()); JLabel label = new
	 * JLabel("<html><center><font color=gray>" + I18.get("mlib_import_msg") +
	 * "</html>"); pan.add("Center", label); fileChooser.setAccessory(pan); int
	 * status = fileChooser.showOpenDialog(tab); if (status ==
	 * JFileChooser.APPROVE_OPTION) { File selectedFiles[] =
	 * fileChooser.getSelectedFiles(); YassImport.importFiles(songList, prop,
	 * selectedFiles); } } };
	 */
	Action importMetadata = new AbstractAction(I18.get("mlib_import_meta")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songList.importMetadata();
		}
	};
	Action exportMetadata = new AbstractAction(I18.get("mlib_export_meta")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			songList.exportMetadata();
		}
	};

	Action exit = new AbstractAction(I18.get("mlib_exit")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			exit();
		}
	};

	/**
	 * Description of the Method
	 */
	public void exit() {
		Window w = SwingUtilities.getWindowAncestor(getTab());
		if (w != null) {
			w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
		}
	}

	/**
	 * Description of the Method
	 */
	public void interruptPlay() {
		mp3.interruptMP3();
	}

	Action interruptPlay = new AbstractAction(I18.get("mlib_stop_song")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			mp3.interruptMP3();
			// lyrics.finishEditing();
		}
	};
	Action incHeight = new AbstractAction(I18.get("medit_height_inc")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.shiftHeight(+1);
		}
	};
	Action decHeight = new AbstractAction(I18.get("medit_height_dec")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.shiftHeight(-1);
		}
	};
	Action incLeft = new AbstractAction(I18.get("medit_length_left_inc")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.shiftLeftEndian(+1);
		}
	};
	Action decLeft = new AbstractAction(I18.get("medit_length_left_dec")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.shiftLeftEndian(-1);
		}
	};
	Action incRight = new AbstractAction(I18.get("medit_length_right_inc")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.shiftRightEndian(+1);
		}
	};
	Action decRight = new AbstractAction(I18.get("medit_length_right_dec")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.shiftRightEndian(-1);
		}
	};

	Action prevBeat = new AbstractAction(I18.get("medit_prev")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.prevBeat();
		}
	};
	Action nextBeat = new AbstractAction(I18.get("medit_next")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.nextBeat();
		}
	};

	Action selectPrevBeat = new AbstractAction(I18.get("medit_select_left")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.selectPrevBeat();
		}
	};
	Action selectNextBeat = new AbstractAction(I18.get("medit_select_right")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.selectNextBeat();
		}
	};

	Action autoCorrect = new AbstractAction(I18.get("medit_correct_all")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			auto.autoCorrectAllSafe(table);
		}
	};
	Action autoCorrectPageBreaks = new AbstractAction(
			I18.get("medit_correct_breaks")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			trimPageBreaks();
		}
	};

	private boolean preventTrim = false;

	/**
	 * Description of the Method
	 */
	public void trimPageBreaks() {
		if (preventTrim) {
			return;
		}

		String s = prop.getProperty("correct-uncommon-pagebreaks");
		boolean withMinors = s != null && s.equals("true");
		boolean ask = s == null || s.equals("unknown");

		if (ask) {
			JPanel panel = new JPanel(new BorderLayout());
			JCheckBox box = null;
			panel.add("Center",
					new JLabel("<html>" + I18.get("medit_correct_breaks_msg")));
			panel.add(
					"South",
					box = new JCheckBox("<html>"
							+ I18.get("medit_correct_breaks_msg_hide")));

			int ok = JOptionPane.showConfirmDialog(tab, panel,
					I18.get("medit_correct_breaks_title"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (ok == JOptionPane.YES_OPTION) {
				withMinors = true;
				if (box.isSelected()) {
					prop.setProperty("correct-uncommon-pagebreaks", "true");
					prop.store();
				}
			} else if (ok == JOptionPane.NO_OPTION) {
				withMinors = false;
				if (box.isSelected()) {
					prop.setProperty("correct-uncommon-pagebreaks", "false");
					prop.store();
				}
			} else {
				return;
			}
		}

		int rows[] = table.getSelectedRows();

		boolean changed = auto.autoCorrectAllPageBreaks(table, withMinors);
		if (changed) {
			table.addUndo();
			preventTrim = true;
			((YassTableModel) table.getModel()).fireTableDataChanged();

			if (rows != null) {
				table.clearSelection();
				for (int i = 0; i < rows.length; i++) {
					table.addRowSelectionInterval(rows[i], rows[i]);
				}
			}
			checkData(table, false, true);
			preventTrim = false;
			sheet.repaint();
		}
	}

	Action autoCorrectSpacing = new AbstractAction(
			I18.get("medit_correct_space")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			boolean changed = auto.autoCorrectSpacing(table);
			if (changed) {
				table.addUndo();
				((YassTableModel) table.getModel()).fireTableDataChanged();
			}
			checkData(table, false, true);
		}
	};
	Action autoCorrectTransposed = new AbstractAction(
			I18.get("medit_correct_heights")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			boolean changed = auto.autoCorrectTransposed(table);
			if (changed) {
				table.addUndo();
				((YassTableModel) table.getModel()).fireTableDataChanged();
			}
			checkData(table, false, true);
		}
	};
	Action joinLeft = new AbstractAction(I18.get("medit_join_left")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.joinLeft();
		}
	};
	Action joinRight = new AbstractAction(I18.get("medit_join_right")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.joinRight();
		}
	};

	Action golden = new AbstractAction(I18.get("medit_golden")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}
			table.setType("*");
		}
	};
	Action freestyle = new AbstractAction(I18.get("medit_freestyle")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}
			table.setType("F");
		}
	};
	Action standard = new AbstractAction(I18.get("medit_normal")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			interruptPlay();
			if (lyrics.isEditable() || songList.isEditing()
					|| isFilterEditing()) {
				return;
			}
			table.setType(":");
		}
	};
	Action addEndian = new AbstractAction(I18.get("medit_length_right_inc")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.shiftRightEndian(+1);
		}
	};
	Action minus = new AbstractAction(I18.get("medit_minus")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			lyrics.enter(YassRow.HYPHEN + "");
		}
	};
	Action space = new AbstractAction(I18.get("medit_space")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			lyrics.enter(YassRow.SPACE + "");
		}
	};
	Action enableHyphenKeys = new AbstractAction(I18.get("medit_lock")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			lyrics.enableHyphenKeys(!lyrics.hyphenKeysEnabled());
		}
	};

	Action incGap = new AbstractAction(I18.get("medit_gap_add_10")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.addGap(+10);
		}
	};
	Action decGap = new AbstractAction(I18.get("medit_gap_sub_10")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.addGap(-10);
		}
	};
	Action incGap2 = new AbstractAction(I18.get("medit_gap_add_1000")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.addGap(+1000);
		}
	};
	Action decGap2 = new AbstractAction(I18.get("medit_gap_sub_1000")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.addGap(-1000);
		}
	};
	Action gotoGap = new AbstractAction(I18.get("medit_gap_select")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.gotoGap();
		}
	};
	Action multiply = new AbstractAction(I18.get("medit_bpm_double")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.multiply();
			if (bpmField != null) {
				bpmField.setText(table.getBPM() + "");
			}
		}
	};
	Action divide = new AbstractAction(I18.get("medit_bpm_half")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.divide();
			if (bpmField != null) {
				bpmField.setText(table.getBPM() + "");
			}
		}
	};
	Action selectLine = new AbstractAction(I18.get("medit_select_line")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.selectLine();
		}
	};
	Action selectAll = new AbstractAction(I18.get("medit_select_all")) {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			table.selectAll();
		}
	};

	/**
	 * Description of the Method
	 * 
	 * @param c
	 *            Description of the Parameter
	 */

	public void registerPlayerActions(JComponent c) {
		InputMap im = c
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap am = c.getActionMap();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "showHelp");
		am.put("showHelp", showHelp);
		showHelp.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_MASK),
				"showBugs");
		am.put("showBugs", showBugs);
		showBugs.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK),
				"fullscreen");
		c.getActionMap().put("fullscreen", fullscreen);
		fullscreen.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK),
				"ratio43");
		c.getActionMap().put("ratio43", ratio43);
		ratio43.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke
				.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK),
				"pausePlayer");
		c.getActionMap().put("pausePlayer", pausePlayer);
		pausePlayer.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK),
				"forwardPlayer");
		c.getActionMap().put("forwardPlayer", forwardPlayer);
		forwardPlayer.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK),
				"endPlayer");
		c.getActionMap().put("endPlayer", endPlayer);
		endPlayer.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK),
				"gotoLibraryFromPlayer");
		c.getActionMap().put("gotoLibraryFromPlayer", gotoLibraryFromPlayer);
		gotoLibraryFromPlayer.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));

		for (int i = 0; i < yass.screen.YassScreen.MAX_PLAYERS; i++) {
			im.remove(KeyStroke.getKeyStroke(yass.screen.YassScreen
					.getKeyCode(yass.screen.YassScreen.UP[i]), 0));
			im.remove(KeyStroke.getKeyStroke(yass.screen.YassScreen
					.getKeyCode(yass.screen.YassScreen.DOWN[i]), 0));
			im.remove(KeyStroke.getKeyStroke(yass.screen.YassScreen
					.getKeyCode(yass.screen.YassScreen.LEFT[i]), 0));
			im.remove(KeyStroke.getKeyStroke(yass.screen.YassScreen
					.getKeyCode(yass.screen.YassScreen.RIGHT[i]), 0));
			im.remove(KeyStroke.getKeyStroke(yass.screen.YassScreen
					.getKeyCode(yass.screen.YassScreen.SELECT[i]), 0));
		}
		c.getActionMap().remove("gotoJukeboxFromPlayer");

		im.put(KeyStroke.getKeyStroke(yass.screen.YassScreen
				.getKeyCode(yass.screen.YassScreen.SELECT[0]), 0),
				"activatePlayer1");
		c.getActionMap().put("activatePlayer1", activatePlayer1);
		im.put(KeyStroke.getKeyStroke(yass.screen.YassScreen
				.getKeyCode(yass.screen.YassScreen.SELECT[1]), 0),
				"activatePlayer2");
		c.getActionMap().put("activatePlayer2", activatePlayer2);
		im.put(KeyStroke.getKeyStroke(yass.screen.YassScreen
				.getKeyCode(yass.screen.YassScreen.SELECT[2]), 0),
				"activatePlayer3");
		c.getActionMap().put("activatePlayer3", activatePlayer3);
	}

	/**
	 * Description of the Method
	 * 
	 * @param c
	 *            Description of the Parameter
	 */
	public void registerJukeboxActions(JComponent c) {
		InputMap im = c
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap am = c.getActionMap();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "showHelp");
		am.put("showHelp", showHelp);
		showHelp.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_MASK),
				"showBugs");
		am.put("showBugs", showBugs);
		showBugs.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK),
				"fullscreen");
		c.getActionMap().put("fullscreen", fullscreen);
		fullscreen.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK),
				"ratio43");
		c.getActionMap().put("ratio43", ratio43);
		ratio43.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke
				.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK));

		im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		c.getActionMap().remove("pausePlayer");

		im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
		c.getActionMap().remove("forwardPlayer");

		im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
				InputEvent.SHIFT_MASK));
		c.getActionMap().remove("endPlayer");

		im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		c.getActionMap().remove("gotoLibraryFromPlayer");

		for (int i = 0; i < yass.screen.YassScreen.MAX_PLAYERS; i++) {
			im.put(KeyStroke.getKeyStroke(yass.screen.YassScreen
					.getKeyCode(yass.screen.YassScreen.UP[i]), 0),
					"gotoPrevJukeboxFromPlayer");
			im.put(KeyStroke.getKeyStroke(yass.screen.YassScreen
					.getKeyCode(yass.screen.YassScreen.DOWN[i]), 0),
					"gotoNextJukeboxFromPlayer");
			im.put(KeyStroke.getKeyStroke(yass.screen.YassScreen
					.getKeyCode(yass.screen.YassScreen.LEFT[i]), 0),
					"gotoPrevJukeboxFromPlayer");
			im.put(KeyStroke.getKeyStroke(yass.screen.YassScreen
					.getKeyCode(yass.screen.YassScreen.RIGHT[i]), 0),
					"gotoNextJukeboxFromPlayer");
			im.put(KeyStroke.getKeyStroke(yass.screen.YassScreen
					.getKeyCode(yass.screen.YassScreen.SELECT[i]), 0),
					"gotoNextJukeboxFromPlayer");
		}
		c.getActionMap().put("gotoPrevJukeboxFromPlayer",
				gotoPrevJukeboxFromPlayer);
		c.getActionMap().put("gotoNextJukeboxFromPlayer",
				gotoNextJukeboxFromPlayer);
	}

	/**
	 * Description of the Method
	 * 
	 * @param c
	 *            Description of the Parameter
	 */
	public void registerScreenActions(JComponent c) {
		InputMap im = c
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap am = c.getActionMap();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "showHelp");
		am.put("showHelp", showHelp);
		showHelp.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_MASK),
				"showBugs");
		am.put("showBugs", showBugs);
		showBugs.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK),
				"fullscreen");
		c.getActionMap().put("fullscreen", fullscreen);
		fullscreen.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK),
				"ratio43");
		c.getActionMap().put("ratio43", ratio43);
		ratio43.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke
				.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK),
				"gotoLibraryFromScreen");
		c.getActionMap().put("gotoLibraryFromScreen", gotoLibraryFromScreen);
		gotoLibraryFromScreen.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "firstScreen");
		c.getActionMap().put("firstScreen", firstScreen);
		firstScreen.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
	}

	// http://java.sun.com/products/jfc/tsc/special_report/kestrel/keybindings.html
	// http://java.sun.com/docs/books/tutorial/uiswing/misc/keybinding.html
	/**
	 * Description of the Method
	 * 
	 * @param c
	 *            Description of the Parameter
	 */
	public void registerLibraryActions(JComponent c) {
		/*
		 * / Install input map on the root pane which watches Ctrl key
		 * pressed/released events final InputMap im = new InputMap() { boolean
		 * ctrlDown=false; public Object get(KeyStroke keyStroke) { / For key
		 * pressed "events" this needs to complete in case / automatic key
		 * repeat is switched on in the operating system. if
		 * (keyStroke.getKeyEventType() != KeyEvent.KEY_TYPED) { // key pressed
		 * or released boolean oldCtrlDown = ctrlDown; ctrlDown =
		 * (keyStroke.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0; if
		 * (oldCtrlDown && !ctrlDown) // Ctrl key released filterUpdate(); }
		 * return super.get(keyStroke); } };
		 * im.setParent(c.getInputMap(JComponent
		 * .WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));
		 * c.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, im);
		 */
		InputMap im = c
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		ActionMap am = c.getActionMap();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "showHelp");
		am.put("showHelp", showHelp);
		showHelp.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_MASK),
				"showBugs");
		am.put("showBugs", showBugs);
		showBugs.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "setTitle");
		am.put("setTitle", setTitle);
		setTitle.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "setArtist");
		am.put("setArtist", setArtist);
		setArtist.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "setAlbum");
		am.put("setAlbum", setAlbum);
		setAlbum.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), "setID");
		am.put("setID", setID);
		setID.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refreshLibrary");
		am.put("refreshLibrary", refreshLibrary);
		refreshLibrary.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "setYear");
		am.put("setYear", setYear);
		setYear.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK),
				"newFile");
		am.put("newFile", newFile);
		newFile.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK),
				"openSongFromLibrary");
		am.put("openSongFromLibrary", openSongFromLibrary);
		openSongFromLibrary.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK
				| InputEvent.SHIFT_MASK), "openFile");
		am.put("openFile", openFile);
		openFile.putValue(
				AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK
						| InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "playSong");
		am.put("playSong", playSong);
		playSong.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "stopPlaySong");
		am.put("stopPlaySong", stopPlaySong);
		stopPlaySong.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK),
				"openSongFolder");
		am.put("openSongFolder", openSongFolder);
		openSongFolder
				.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke
						.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK));

		// im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK),
		// "importFiles");
		// am.put("importFile", importFiles);
		// importFiles.putValue(AbstractAction.ACCELERATOR_KEY,
		// KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK),
				"saveLibrary");
		am.put("saveLibrary", saveLibrary);
		saveLibrary.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK),
				"editRecent");
		am.put("editRecent", editRecent);
		editRecent.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK),
				"undoAllLibraryChanges");
		am.put("undoAllLibraryChanges", undoAllLibraryChanges);
		undoAllLibraryChanges.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK),
				"filterLibrary");
		am.put("filterLibrary", filterLibrary);
		filterLibrary.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK),
				"printLibrary");
		am.put("printLibrary", printLibrary);
		printLibrary.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK),
				"addToPlayList");
		am.put("addToPlayList", addToPlayList);
		addToPlayList
				.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke
						.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK),
				"removeFromPlayList");
		am.put("removeFromPlayList", removeFromPlayList);
		removeFromPlayList.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK),
				"movePlayListUp");
		am.put("movePlayListUp", movePlayListUp);
		movePlayListUp.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK),
				"movePlayListDown");
		am.put("movePlayListDown", movePlayListDown);
		movePlayListDown.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK),
				"removeSong");
		am.put("removeSong", removeSong);
		removeSong.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke
				.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK),
				"findLyrics");
		am.put("findLyrics", findLyrics);
		findLyrics.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK),
				"selectAllSongs");
		am.put("selectAllSongs", selectAllSongs);
		selectAllSongs.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK),
				"fullscreen");
		c.getActionMap().put("fullscreen", fullscreen);
		fullscreen.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK
				| InputEvent.SHIFT_MASK), "gotoPlayerSelectedSong");
		c.getActionMap().put("gotoPlayerSelectedSong", gotoPlayerSelectedSong);
		gotoPlayerSelectedSong.putValue(
				AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK
						| InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK),
				"gotoPlayer");
		c.getActionMap().put("gotoPlayer", gotoPlayer);
		gotoPlayer.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
	}

	/**
	 * Description of the Method
	 * 
	 * @param c
	 *            Description of the Parameter
	 */
	public void registerEditorActions(JComponent c) {
		InputMap im = c
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		c.getActionMap();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "showHelp");
		c.getActionMap().put("showHelp", showHelp);
		showHelp.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_MASK),
				"showBugs");
		c.getActionMap().put("showBugs", showBugs);
		showBugs.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "editLyrics");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "editLyrics");
		editLyrics.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		c.getActionMap().put("editLyrics", editLyrics);

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK),
				"saveSong");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "saveSong");
		saveSong.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		c.getActionMap().put("saveSong", saveSong);

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "reloadSong");
		reloadSong.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		c.getActionMap().put("reloadSong", reloadSong);
		
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK
				| InputEvent.SHIFT_MASK), "openFile");
		c.getActionMap().put("openFile", openFile);
		openFile.putValue(
				AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK
						| InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK),
				"gotoLibrary");
		c.getActionMap().put("gotoLibrary", gotoLibrary);
		gotoLibrary.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK
				| InputEvent.CTRL_MASK | InputEvent.ALT_MASK),
				"shiftLeftRemainder");
		c.getActionMap().put("shiftLeftRemainder", shiftLeftRemainder);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK
				| InputEvent.CTRL_MASK | InputEvent.ALT_MASK),
				"shiftRightRemainder");
		c.getActionMap().put("shiftRightRemainder", shiftRightRemainder);

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK
				| InputEvent.ALT_MASK), "shiftLeft");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK),
				"shiftLeft");
		c.getActionMap().put("shiftLeft", shiftLeft);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK
				| InputEvent.ALT_MASK), "shiftRight");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK),
				"shiftRight");
		c.getActionMap().put("shiftRight", shiftRight);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK),
				"decHeight");
		c.getActionMap().put("decHeight", decHeight);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK),
				"incHeight");
		c.getActionMap().put("incHeight", incHeight);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK),
				"decLeft");
		c.getActionMap().put("decLeft", decLeft);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK),
				"incLeft");
		c.getActionMap().put("incLeft", incLeft);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_MASK),
				"decRight");
		c.getActionMap().put("decRight", decRight);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_MASK),
				"incRight");
		c.getActionMap().put("incRight", incRight);

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "prevPage");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "prevBeat");
		c.getActionMap().put("prevBeat", prevBeat);
		c.getActionMap().put("prevPage", prevPage);
		prevPage.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "nextPage");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "nextBeat");
		c.getActionMap().put("nextBeat", nextBeat);
		c.getActionMap().put("nextPage", nextPage);
		nextPage.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_MASK),
				"selectPrevBeat");
		c.getActionMap().put("selectPrevBeat", selectPrevBeat);
		selectPrevBeat.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_MASK));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_MASK),
				"selectNextBeat");
		c.getActionMap().put("selectNextBeat", selectNextBeat);
		selectNextBeat
				.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke
						.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "lessPages");
		c.getActionMap().put("lessPages", lessPages);
		lessPages.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "morePages");
		c.getActionMap().put("morePages", morePages);
		morePages.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));

		im.put(KeyStroke
				.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_MASK),
				"onePage");
		c.getActionMap().put("onePage", onePage);
		onePage.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke
				.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_MASK));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,
				InputEvent.CTRL_MASK), "selectAll");
		c.getActionMap().put("selectAll", selectAll);
		selectAll.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke
				.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK),
				"pasteRows");
		c.getActionMap().put("pasteRows", pasteRows);
		pasteRows.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK),
				"copyRows");
		c.getActionMap().put("copyRows", copyRows);
		copyRows.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK),
				"removeCopy");
		c.getActionMap().put("removeCopy", removeCopy);
		removeCopy.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "togglePageBreak");
		c.getActionMap().put("togglePageBreak", togglePageBreak);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
				"removePageBreak");
		c.getActionMap().put("removePageBreak", removePageBreak);

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "removeRows");
		c.getActionMap().put("removeRows", removeRows);
		removeRows.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0), "absolute");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.SHIFT_MASK),
				"absolute");
		c.getActionMap().put("absolute", absolute);
		absolute.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_L, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK),
				"fullscreen");
		c.getActionMap().put("fullscreen", fullscreen);
		fullscreen.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK));

		// im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12,
		// InputEvent.SHIFT_MASK),
		// "playAll");
		// c.getActionMap().put("playAll", playAll);
		// playAll.putValue(AbstractAction.ACCELERATOR_KEY,
		// KeyStroke.getKeyStroke(KeyEvent.VK_F12, InputEvent.SHIFT_MASK));
		//
		// im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0),
		// "playAllFromHere");
		// c.getActionMap().put("playAllFromHere", playAllFromHere);
		// playAllFromHere.putValue(AbstractAction.ACCELERATOR_KEY,
		// KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));

		// c.getActionMap().put("recordAll", recordAll);

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK),
				"recordSelection");
		c.getActionMap().put("recordSelection", recordSelection);
		recordSelection.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "playSelection");
		c.getActionMap().put("playSelection", playSelection);
		playSelection.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.SHIFT_MASK),
				"playSelectionWithMIDI");
		c.getActionMap().put("playSelectionWithMIDI", playSelectionWithMIDI);
		playSelectionWithMIDI
				.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke
						.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.SHIFT_MASK
				| InputEvent.CTRL_MASK), "playSelectionWithMIDIAudio");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK),
				"playSelectionWithMIDIAudio");
		c.getActionMap().put("playSelectionWithMIDIAudio",
				playSelectionWithMIDIAudio);
		playSelectionWithMIDIAudio
				.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke
						.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "playPage");
		c.getActionMap().put("playPage", playPage);
		playPage.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_MASK),
				"playPageWithMIDI");
		c.getActionMap().put("playPageWithMIDI", playPageWithMIDI);
		playPageWithMIDI.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_MASK
				| InputEvent.CTRL_MASK), "playPageWithMIDIAudio");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK),
				"playPageWithMIDIAudio");
		c.getActionMap().put("playPageWithMIDIAudio", playPageWithMIDIAudio);
		playPageWithMIDIAudio.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), "showCopiedRows");
		c.getActionMap().put("showCopiedRows", showCopiedRows);
		showCopiedRows.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_V, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "playFrozen");
		c.getActionMap().put("playFrozen", playFrozen);
		playFrozen.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_MASK),
				"playFrozenWithMIDI");
		c.getActionMap().put("playFrozenWithMIDI", playFrozenWithMIDI);
		playFrozenWithMIDI.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_MASK
				| InputEvent.CTRL_MASK), "playFrozenWithMIDIAudio");
		c.getActionMap()
				.put("playFrozenWithMIDIAudio", playFrozenWithMIDIAudio);
		playFrozenWithMIDIAudio.putValue(
				AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_MASK
						| InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK),
				"enableMidi");
		c.getActionMap().put("enableMidi", enableMidi);
		enableMidi.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK),
				"enableAudio");
		c.getActionMap().put("enableAudio", enableAudio);
		enableAudio.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK),
				"enableClicks");
		c.getActionMap().put("enableClicks", enableClicks);
		enableClicks.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "interruptPlay");
		c.getActionMap().put("interruptPlay", interruptPlay);
		interruptPlay.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK),
				"undo");
		c.getActionMap().put("undo", undo);
		undo.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK),
				"redo");
		c.getActionMap().put("redo", redo);
		redo.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "splitRows");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "joinRows");
		c.getActionMap().put("splitRows", splitRows);
		c.getActionMap().put("joinRows", joinRows);
		splitRows.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0));
		joinRows.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_MASK),
				"rollLeft");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "rollRight");
		c.getActionMap().put("rollLeft", rollLeft);
		c.getActionMap().put("rollRight", rollRight);
		rollLeft.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_MASK));
		rollRight.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "home");
		c.getActionMap().put("home", home);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "end");
		c.getActionMap().put("end", end);

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_MASK),
				"first");
		c.getActionMap().put("first", first);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_MASK),
				"last");
		c.getActionMap().put("last", last);

		/*
		 * im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L , InputEvent.CTRL_MASK),
		 * "lock"); c.getActionMap().put("lock", enableHyphenKeys);
		 * enableHyphenKeys.putValue(AbstractAction.ACCELERATOR_KEY,
		 * KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
		 */
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), "golden");
		c.getActionMap().put("golden", golden);
		golden.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_G, 0));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), "freestyle");
		c.getActionMap().put("freestyle", freestyle);
		freestyle.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), "standard");
		c.getActionMap().put("standard", standard);
		standard.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DEAD_TILDE, 0), "addEndian");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DEAD_CIRCUMFLEX, 0),
				"addEndian");
		im.put(KeyStroke.getKeyStroke("~"), "addEndian");
		c.getActionMap().put("addEndian", addEndian);
		addEndian.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_DEAD_TILDE, 0));

		// umlaute gehn nicht
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK),
				"decGap");
		c.getActionMap().put("decGap", decGap);
		decGap.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK
				| InputEvent.SHIFT_MASK), "incGap");
		c.getActionMap().put("incGap", incGap);
		incGap.putValue(
				AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK
						| InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK
				| InputEvent.ALT_MASK), "decGap2");
		c.getActionMap().put("decGap2", decGap2);
		decGap2.putValue(
				AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK
						| InputEvent.ALT_MASK));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK
				| InputEvent.ALT_MASK | InputEvent.SHIFT_MASK), "incGap2");
		c.getActionMap().put("incGap2", incGap2);
		incGap2.putValue(
				AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK
						| InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK),
				"multiply");
		c.getActionMap().put("multiply", multiply);
		multiply.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK),
				"divide");
		c.getActionMap().put("divide", divide);
		divide.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK),
				"selectLine");
		c.getActionMap().put("selectLine", selectLine);
		selectLine.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK
				| InputEvent.SHIFT_MASK), "selectAll");
		c.getActionMap().put("selectAll", selectAll);
		// selectAll.putValue(AbstractAction.ACCELERATOR_KEY,
		// KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK |
		// InputEvent.SHIFT_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "togglePageBreak");
		c.getActionMap().put("togglePageBreak", togglePageBreak);
		togglePageBreak.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK),
				"autoCorrectPageBreaks");
		c.getActionMap().put("autoCorrectPageBreaks", autoCorrectPageBreaks);
		autoCorrectPageBreaks.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK),
				"insertNote");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK),
				"insertNote");
		c.getActionMap().put("insertNote", insertNote);
		insertNote.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke
				.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK));
		insertNote
				.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke
						.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_MASK),
				"prevVersion");
		c.getActionMap().put("prevVersion", prevVersion);
		prevVersion.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK),
				"nextVersion");
		c.getActionMap().put("nextVersion", nextVersion);
		nextVersion.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK),
				"version1");
		c.getActionMap().put("version1", version1);
		version1.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_MASK),
				"version2");
		c.getActionMap().put("version2", version2);
		version2.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK),
				"version3");
		c.getActionMap().put("version3", version3);
		version3.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_MASK),
				"version4");
		c.getActionMap().put("version4", version4);
		version4.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_MASK));
	}

	// BUG Workaround for JOptionPane: add keyboard support
	private static void configureOptionPane() {
		if (UIManager.getLookAndFeelDefaults().get("OptionPane.actionMap") == null) {
			UIManager.put("OptionPane.windowBindings", new Object[] { "ESCAPE",
					"close", "LEFT", "left", "KP_LEFT", "left", "RIGHT",
					"right", "KP_RIGHT", "right" });
			ActionMap map = new javax.swing.plaf.ActionMapUIResource();
			map.put("close", new OptionPaneCloseAction());
			map.put("left", new OptionPaneArrowAction(false));
			map.put("right", new OptionPaneArrowAction(true));
			UIManager.getLookAndFeelDefaults().put("OptionPane.actionMap", map);
		}
	}

	/**
	 * Description of the Class
	 * 
	 * @author Saruta
	 * @created 21. August 2007
	 */
	private static class OptionPaneCloseAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		/**
		 * Description of the Method
		 * 
		 * @param e
		 *            Description of the Parameter
		 */
		public void actionPerformed(ActionEvent e) {
			JOptionPane optionPane = (JOptionPane) e.getSource();
			optionPane.setValue(JOptionPane.CLOSED_OPTION);
		}
	}

	/**
	 * Description of the Class
	 * 
	 * @author Saruta
	 * @created 21. August 2007
	 */
	private static class OptionPaneArrowAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private boolean myMoveRight;

		/**
		 * Constructor for the OptionPaneArrowAction object
		 * 
		 * @param moveRight
		 *            Description of the Parameter
		 */
		OptionPaneArrowAction(boolean moveRight) {
			myMoveRight = moveRight;
		}

		/**
		 * Description of the Method
		 * 
		 * @param e
		 *            Description of the Parameter
		 */
		public void actionPerformed(ActionEvent e) {
			JOptionPane optionPane = (JOptionPane) e.getSource();
			EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();

			eq.postEvent(new KeyEvent(optionPane, KeyEvent.KEY_PRESSED, e
					.getWhen(), (myMoveRight) ? 0 : InputEvent.SHIFT_DOWN_MASK,
					KeyEvent.VK_TAB, KeyEvent.CHAR_UNDEFINED,
					KeyEvent.KEY_LOCATION_UNKNOWN));
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public JFrame createOwnerFrame() {
		return new OwnerFrame();
	}

	private class OwnerFrame extends JFrame {
		private static final long serialVersionUID = 1L;

		OwnerFrame() {
			setIconImage(new ImageIcon(YassActions.this.getClass().getResource(
					"/yass/yass-icon-16.png")).getImage());
		}

		// This frame can never be shown.

		public void show() {
		}
	}
}
