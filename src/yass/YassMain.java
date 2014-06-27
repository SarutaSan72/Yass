package yass;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    21. August 2007
 */
public class YassMain extends JApplet {

	private static final long serialVersionUID = 777825370133814283L;

	public final static boolean COMBINED_LYRICS = true;
	public final static boolean COMBINED_LIBRARY = true;

	public final static boolean NO_GAME = true;
	private static final boolean PRELOAD_FOBS = false;
	
	private boolean redirectOutput = false;

	private YassProperties prop = null;
	
	private YassSheet sheet = null;
	private YassActions actions = null;
	private YassPlayer mp3 = null;
	private YassErrors errors = null;
	private YassVideo video = null;
	private YassLyrics lyrics = null;
	private YassSongList songList = null;
	private YassSongInfo songInfo = null;
	private YassPlayList playList = null;
	private YassGroups groups = null;

	private JPanel toolPanel = null;

	private JPanel groupsPanel, songPanel, playlistPanel;

	private static boolean convert = false, lib = true, edit = false, correct = false, play = false;
	private static String midiFile = null, txtFile = null;


	/**
	 *  Description of the Method
	 */
	public void onShow() {
		JViewport v = (JViewport) songList.getTableHeader().getParent();
		if (v != null) {
			v.setVisible(false);
		}
	}


	/**
	 *  Gets the yassActions attribute of the YassMain object
	 *
	 * @return    The yassActions value
	 */
	public YassActions getYassActions() {
		return actions;
	}


	/**
	 *  Sets the defaultLocation attribute of the YassMain object
	 *
	 * @param  p  The new defaultLocation value
	 */
	public void setDefaultLocation(Point p) {
		prop.setProperty("frame-x", p.x + "");
		prop.setProperty("frame-y", p.y + "");
	}


	/**
	 *  Sets the defaultSize attribute of the YassMain object
	 *
	 * @param  d  The new defaultSize value
	 */
	public void setDefaultSize(Dimension d) {
		prop.setProperty("frame-width", d.width + "");
		prop.setProperty("frame-height", d.height + "");

		actions.interruptPlay();

		sheet.updateHeight();
		if (COMBINED_LYRICS) {
			actions.revalidateLyricsArea();
		}
		sheet.update();
		sheet.repaint();
	}


	/**
	 *  Gets the defaultLocation attribute of the YassMain object
	 *
	 * @return    The defaultLocation value
	 */
	public Point getDefaultLocation() {
		String x = prop.getProperty("frame-x");
		if (x == null) {
			return null;
		}
		String y = prop.getProperty("frame-y");
		if (y == null) {
			return null;
		}
		return new Point(new Integer(x), new Integer(y));
	}


	/**
	 *  Gets the defaultSize attribute of the YassMain object
	 *
	 * @return    The defaultSize value
	 */
	public Dimension getDefaultSize() {
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
		return new Dimension(new Integer(w), new Integer(h));
	}


	/**
	 *  Description of the Method
	 */
	public void init() {
		final StringBuffer buf = new StringBuffer(3000);

		try {
			if (redirectOutput) {
				PrintStream stream = new PrintStream(
					new OutputStream() {
						public void write(int b) {
							buf.append((char) b);
						}
					}, true, "UTF-8");
				System.setOut(stream);
				System.setErr(stream);
			} else {
				PrintStream utf8 = new PrintStream(System.out, true, "UTF-8");
				System.setOut(utf8);
				System.setErr(utf8);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		prop = new YassProperties();
		prop.load();

		String lang = prop.getProperty("yass-language");
		if (lang == null || lang.equals("default")) {
			lang = Locale.getDefault().getLanguage();
		}
		System.out.println("Setting Language: "+lang);
		I18.setLanguage(lang);

		if (prop.checkVersion()) {
			String dir = prop.getUserDir();
			int ok = JOptionPane.showConfirmDialog(null, "<html>" + I18.get("incompatible_version") + "<br>" + dir + "<br><br>" + I18.get("remove_version"), I18.get("incompatible_version") + " - Yass", JOptionPane.OK_CANCEL_OPTION);
			if (ok == JOptionPane.OK_OPTION) {
				boolean verify = dir.indexOf(".yass") < 0;
				if (verify || !(new File(dir).exists()) || !(new File(dir).isDirectory())) {
					JOptionPane.showMessageDialog(null, I18.get("remove_version_error"), I18.get("incompatible_version"), JOptionPane.WARNING_MESSAGE);
				} else {
					YassUtils.deleteDir(new File(dir));
				}
				prop.load();
			}
		}

		String fobs = prop.getProperty("use-fobs");
		YassVideoUtils.TRY_TO_USE_FOBS = fobs!=null && fobs.equals("true");

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		sheet = new YassSheet();
		mp3 = new YassPlayer(sheet);

		File td = new File(prop.getProperty("temp-dir"));
		if (td.exists()) {
			YassUtils.deleteDir(td);
		}
		td.mkdirs();

		lyrics = new YassLyrics(prop);
		lyrics.setSheet(sheet);
		actions = new YassActions(sheet, mp3, lyrics);
		actions.init(prop);
		actions.setTab(mainPanel);
		actions.setOutBuffer(buf);
		sheet.setActions(actions);

		if (! NO_GAME) {
		yass.screen.YassScreen.loadPlugins(prop);
		yass.screen.YassScreen.addScreenChangeListener(
			new yass.screen.ScreenChangeListener() {
				public void screenChanged(yass.screen.ScreenChangeEvent e) {
					actions.gotoScreen(e.getNewScreen());
				}
			});
		}
		
		ToolTipManager.sharedInstance().setInitialDelay(200);
		ToolTipManager.sharedInstance().setReshowDelay(0);

		yass.stats.YassStats.setProperties(prop);
		yass.stats.YassStats.init();

		JPanel songListPanel = createSongListPanel();

		JPanel lyricsPanel = COMBINED_LYRICS ? (new JPanel()) : createLyricsPanel();

		final JPanel sheetPanel = createSheetPanel();

		video = new YassVideo(prop, sheet);
		actions.setVideo(video);
		mp3.setVideo(video);

		JPanel infoPanel = new JPanel(new BorderLayout());

		String layout = prop.getProperty("editor-layout");
		if (layout == null) {
			layout = "East";
		}

		if (COMBINED_LYRICS) {
			String lyricsWidthString = prop.getProperty("lyrics-width");
			String lyricsHeightString = prop.getProperty("lyrics-min-height");
			int lyricsWidth = new Integer(lyricsWidthString).intValue();
			int lyricsMinHeight = new Integer(lyricsHeightString).intValue();

			// LYRICS POSITION
			if (layout.equals("East")) {
				lyrics.setBounds(500, 30, lyricsWidth, lyricsMinHeight);
			} else if (layout.equals("West")) {
				lyrics.setBounds(0, 30, lyricsWidth, lyricsMinHeight);
			}

			lyrics.setOpaque(false);
			sheet.setLayout(null);
			sheet.add(lyrics);
		} else {
			infoPanel.add("Center", lyricsPanel);
		}

		errors = new YassErrors(actions, prop, actions.createErrorToolbar());
		actions.setErrors(errors);
		//actions.setRaw(raw);
		//actions.setMP3Info(mp3info);
		// actions.setVideo(video);
		//actions.setCover(cover);
		//actions.setBackground(background);

		JPanel p = new JPanel(new BorderLayout());
		//p.add("East", errorPanel);
		//p.add("Center", infoPanel);

		String dv = prop.getProperty("split-vertical");
		if (dv == null) {
			dv = "300";
		}

		if (!COMBINED_LYRICS) {
			final JSplitPane splitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sheetPanel, p);
			splitPaneV.setOneTouchExpandable(true);
			splitPaneV.setDividerLocation(new Integer(dv).intValue());
			splitPaneV.setResizeWeight(0);
			splitPaneV.setContinuousLayout(true);
			splitPaneV.addPropertyChangeListener(
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName() == JSplitPane.DIVIDER_LOCATION_PROPERTY) {
							int dl = splitPaneV.getDividerLocation();
							if (dl > 0 && dl < splitPaneV.getMaximumDividerLocation()) {
								prop.setProperty("split-vertical", dl + "");
							}
							splitPaneV.validate();
							sheet.revalidate();
							sheet.init();
							sheet.update();
							sheet.repaint();
						}
					}
				});
			actions.setPanels(this, mainPanel, songListPanel, songInfo, groupsPanel, songPanel, playlistPanel, splitPaneV);
		} else {
			actions.setPanels(this, mainPanel, songListPanel, songInfo, groupsPanel, songPanel, playlistPanel, sheetPanel);
		}

		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add("Center", mainPanel);
	}


	/**
	 *  Sets the commandLine attribute of the YassMain object
	 *
	 * @param  argv  The new commandLine value
	 */
	public void setCommandLine(String argv[]) {
		if (argv == null) {
			return;
		}
		for (int i = 0; i < argv.length; i++) {
			String arg = argv[i];
			String low = arg.toLowerCase();
			if (low.equals("-convert")) {
				convert = true;
			}
			if (low.equals("-lib")) {
				lib = true;
			}
			if (low.equals("-play")) {
				play = true;
			}
			if (low.equals("-edit")) {
				edit = true;
			}
			if (low.equals("-correct")) {
				correct = true;
			}
			if (low.endsWith(".mid") || low.endsWith(".midi") || low.endsWith(".kar")) {
				midiFile = arg;
			}
			if (low.endsWith(".txt")) {
				edit = true;
				txtFile = arg;
			}
			File f = new File(arg);
			if (f.exists() && f.isDirectory()) {
				edit = true;
				txtFile = arg;
			}
		}
	}


	/**
	 *  Description of the Method
	 */
	public void load() {
		String s = prop.getProperty("welcome");
		if (s != null && s.equals("true")) {
			new YassLibOptions(prop, actions, songList, mp3);
			prop.setProperty("welcome", "false");
			prop.store();
		}

		if (edit) {
			if (txtFile != null) {
				actions.openFiles(txtFile);
			} else {
				actions.closeAllTables();
			}
			actions.setView(YassActions.VIEW_EDIT);
			sheet.requestFocusInWindow();
			return;
		}

		if (play && !NO_GAME) {
			songList.addSongListListener(
				new YassSongListListener() {
					public void stateChanged(YassSongListEvent e) {
						int state = e.getState();
						if (state == YassSongListEvent.STARTING) {
							yass.screen.YassScreen.setLoading(true);
							yass.screen.YassScreen.getCurrentScreen().stopJukebox();
						}
						if (state == YassSongListEvent.LOADED) {
							groups.refreshCounters();
							yass.screen.YassScreen.setSongData(songList.getSongData());
							yass.screen.YassScreen.setGroupData(groups.toScreenGroups());
							yass.screen.YassScreen.setLoading(false);
							yass.screen.YassScreen.getCurrentScreen().startJukebox();
						}
					}
				});
			yass.screen.YassScreen.setCurrentScreen("start");
			actions.setView(YassActions.VIEW_SCREEN);
			yass.screen.YassScreen.getCurrentScreen().stopJukebox();
			yass.screen.YassScreen.getCurrentScreen().requestFocusInWindow();
			yass.screen.YassScreen.setLoading(true);
			SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						String songdir = prop.getProperty("song-directory");
						if (songdir == null || !new File(songdir).exists()) {
							yass.screen.YassScreen.getCurrentScreen().loadSongs();
						} else {
							songList.load();
							songList.filter(null);
						}
					}
				});
			return;
		}

		//System.out.println("init library view");
		actions.setView(YassActions.VIEW_LIBRARY);
		//System.out.println("library view ready");

		//System.out.println("load");
		songList.load();
		//System.out.println("filter");
		songList.filter(null);
		//System.out.println("focus");
		songList.focusFirstVisible();
		//System.out.println("done");
	}


	/**
	 *  Description of the Method
	 */
	public void stop() {
		askStop();
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public boolean askStop() {
		if (actions.cancelOpen()) {
			return false;
		}

		mp3.closeMP3();
		YassTable t = actions.firstTable();
		if (t != null) {
			prop.setProperty("recent-file", t.getDir() + File.separator + t.getFilename());
		} else {
			prop.setProperty("recent-file", "");
		}
		songList.setDefaults();
		prop.store();

		songList.interrupt();
		return true;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public JPanel createSheetPanel() {
		JPanel sheetPanel = new JPanel(new BorderLayout());
		JScrollPane sheetPane = new JScrollPane(sheet);

		if (COMBINED_LYRICS) {
			sheetPane.getHorizontalScrollBar().addMouseListener(
				new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						YassTable t = sheet.getActiveTable();
						if (t != null && t.getMultiSize() == 1) {
							YassTable.setZoomMode(YassTable.ZOOM_MULTI);
							sheet.enablePan(false);
							actions.revalidateLyricsArea();
							sheet.repaint();
						}
					}
				});
			sheetPane.getViewport().addChangeListener(
				new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						JViewport v = (JViewport) e.getSource();
						Point p = v.getViewPosition();
						Dimension r = v.getExtentSize();

						// LYRICS POSITION
						String layout = prop.getProperty("editor-layout");
						if (layout == null) {
							layout = "East";
						}

						String lyricsWidthString = prop.getProperty("lyrics-width");
						int lyricsWidth = new Integer(lyricsWidthString).intValue();

						int newx = (int) p.getX() + r.width - lyricsWidth;
						if (layout.equals("East")) {
							newx = (int) p.getX() + r.width - lyricsWidth;
						} else if (layout.equals("West")) {
							newx = (int) p.getX();
						}
						int newy = (int) p.getY() + 20;
						Point p2 = lyrics.getLocation();
						if (p2.x != newx || p2.y != newy) {
							lyrics.setLocation(newx, newy);
							sheet.revalidate();
							sheet.update();
						}
					}
				});
			sheetPane.getViewport().addComponentListener(
				new ComponentAdapter() {
					public void componentResized(ComponentEvent e) {
						actions.stopPlaying();
						
						JViewport v = (JViewport) e.getSource();
						Point p = v.getViewPosition();
						Dimension r = v.getExtentSize();

						//System.out.println("RESIZE " + v.getExtentSize());

						// LYRICS POSITION
						String layout = prop.getProperty("editor-layout");
						if (layout == null) {
							layout = "East";
						}

						String lyricsWidthString = prop.getProperty("lyrics-width");
						int lyricsWidth = new Integer(lyricsWidthString).intValue();

						int newx = (int) p.getX() + r.width - lyricsWidth;
						if (layout.equals("East")) {
							newx = (int) p.getX() + r.width - lyricsWidth;
						} else if (layout.equals("West")) {
							newx = (int) p.getX();
						}
						int newy = (int) p.getY() + 20;
						Point p2 = lyrics.getLocation();
						if (p2.x != newx || p2.y != newy) {
							lyrics.setLocation(newx, newy);
							sheet.revalidate();
							sheet.update();
						} else {
							sheet.updateHeight();
							actions.revalidateLyricsArea();
						}
					}
				});
		} else {
			sheetPane.getViewport().addChangeListener(
				new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						if (!sheet.isTemporaryStop()) {
							actions.stopPlaying();
						}
					}
				});
		}

		sheetPane.setWheelScrollingEnabled(false);
		sheetPane.addMouseWheelListener(
			new MouseWheelListener() {
				public void mouseWheelMoved(MouseWheelEvent e) {
					if (sheet.isPlaying() || sheet.isTemporaryStop()) {
						return;
					}
					int notches = e.getWheelRotation();
					if (notches == 0) {
						return;
					}
					actions.getTable().gotoPage(notches < 0 ? -1 : 1);
				}
			});

		// boolean expert = prop.getProperty("expert").equals("true");

		sheetPanel.add("North", actions.createFileEditToolbar());
		//sheetPanel.add("South", actions.createPlaybackToolbar());
		sheetPanel.add("Center", sheetPane);

		sheetPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//sheetPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		sheetPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		sheetPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

		return sheetPanel;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public JPanel createLyricsPanel() {
		JPanel lyricsPanel = new JPanel(new BorderLayout());
		lyricsPanel.add("Center", lyrics);
		lyricsPanel.add("West", actions.createLyricsToolbar());
		return lyricsPanel;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public JPanel createSongListPanel() {
		JScrollPane songScroll = new JScrollPane(songList = new YassSongList(actions));
		songScroll.setOpaque(false);
		songScroll.getViewport().setOpaque(false);
		songScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		JScrollPane groupsScroll = new JScrollPane(groups = new YassGroups(songList));
		groupsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		final JComboBox<String> groupsBox = actions.createGroupsBox();
		groupsBox.setPreferredSize(new Dimension(120, 40));

		actions.createFilterBox();
		
		actions.setGroups(groups);
		actions.setSongList(songList);

		JPanel toolPanel3 = new JPanel(new BorderLayout());
		JPanel toolPanel2 = new JPanel(new BorderLayout());
		toolPanel3.add("West", actions.createSongListToolbar());
		toolPanel3.add("East", actions.createFilterToolbar());
		toolPanel3.add("Center", toolPanel2);
		toolPanel = new JPanel(new BorderLayout());

		toolPanel2.add("Center", toolPanel);
		groups.setToolBar("title", (JToolBar) actions.createTitleToolbar());
		groups.setToolBar("artist", (JToolBar) actions.createArtistToolbar());
		groups.setToolBar("genre", (JToolBar) actions.createGenreToolbar());
		groups.setToolBar("edition", (JToolBar) actions.createEditionToolbar());
		groups.setToolBar("language", (JToolBar) actions.createLanguageToolbar());
		groups.setToolBar("year", (JToolBar) actions.createYearToolbar());
		groups.setToolBar("length", (JToolBar) actions.createLengthToolbar());
		groups.setToolBar("album", (JToolBar) actions.createAlbumToolbar());
		groups.setToolBar("playlist", (JToolBar) actions.createPlaylistToolbar());
		groups.setToolBar("duets", (JToolBar) actions.createDuetsToolbar());
		groups.setToolBar("files", (JToolBar) actions.createFilesToolbar());
		groups.setToolBar("folder", (JToolBar) actions.createFolderToolbar());
		groups.setToolBar("errors", (JToolBar) actions.createErrorsToolbar());
		toolPanel.add("Center", groups.getToolBar(0));

		groupsPanel = new JPanel(new BorderLayout());
		groupsPanel.add("Center", groupsScroll);

		groupsBox.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(
						new Runnable() {
							public void run() {
								int i = groupsBox.getSelectedIndex();
								groups.setGroups(i);
								toolPanel.removeAll();
								toolPanel.add("Center", groups.getToolBar(i));
								toolPanel.validate();
								toolPanel.repaint();
							}
						});
				}
			});
		groups.addPropertyChangeListener(
			new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent p) {
					if (!p.getPropertyName().equals("group")) {
						return;
					}

					String o = (String) p.getOldValue();
					String n = (String) p.getNewValue();

					if (o.equals("errors")) {
						songList.showErrors(false);
					}
					if (o.equals("stats")) {
						songList.showStats(false);
					}
					if (n.equals("errors")) {
						songList.showStats(false);
						songList.showErrors(true);
					}
					if (n.equals("stats")) {
						songList.showErrors(false);
						songList.showStats(true);
					}
				}
			});
		groupsPanel.add("North", groupsBox);

		songInfo = new YassSongInfo(prop, actions);
		actions.setSongInfo(songInfo);

		playList = new YassPlayList(actions, prop, null, songList);
		playList.setOpaque(false);

		playlistPanel = new JPanel(new BorderLayout());
		playlistPanel.add("Center", playList);
		playlistPanel.setOpaque(false);
		//playlistPanel.add("West", actions.createListTransferToolbar());
		playlistPanel.add("North", actions.createPlayListToolbar());

		songPanel = new JPanel(new BorderLayout());
		songPanel.add("Center", songScroll);
		//songPanel.add("South", actions.createSongSearchToolbar());
		songPanel.setOpaque(false);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add("North", toolPanel3);
		panel.add("Center", songInfo);
		actions.setLibTools(toolPanel3);

		songList.addKeyListener(
			new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					int c = e.getKeyCode();
					if (c == KeyEvent.VK_RIGHT && e.getModifiers() == 0 && playList.getList().getRowCount() > 0) {
						playList.getList().requestFocus();
						YassSong s = playList.getList().getFirstSelectedSong();
						if (s == null) {
							playList.getList().selectSong(0);
						} else {
							songInfo.setSong(s);
							songInfo.repaint();
						}
						songList.repaint();
						playList.repaint();
					}
					if (c == KeyEvent.VK_LEFT && e.getModifiers() == 0) {
						groups.requestFocus();
						songList.repaint();
						groups.repaint();
					}
				}
			});
		playList.getList().addKeyListener(
			new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					int c = e.getKeyCode();
					if (c == KeyEvent.VK_LEFT && e.getModifiers() == 0) {
						if (songList.getRowCount() > 0) {
							songList.requestFocus();
							YassSong s = songList.getFirstSelectedSong();
							if (s == null) {
								songList.selectSong(0);
							} else {
								songInfo.setSong(s);
								songInfo.repaint();
							}
							songList.repaint();
							playList.repaint();
						} else {
							groups.requestFocus();
							playList.repaint();
							groups.repaint();
						}
					}
					if (c == KeyEvent.VK_RIGHT && e.getModifiers() == 0) {
						actions.getPlayListBox().requestFocus();
						playList.repaint();
						actions.getPlayListBox().repaint();
					}
				}
			});

		groups.addKeyListener(
			new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					int c = e.getKeyCode();
					if (c == KeyEvent.VK_RIGHT && e.getModifiers() == 0) {
						if (songList.getRowCount() > 0) {
							songList.requestFocus();
							YassSong s = songList.getFirstSelectedSong();
							if (s == null) {
								songList.selectSong(0);
							} else {
								songInfo.setSong(s);
								songInfo.repaint();
							}
						} else if (playList.getList().getRowCount() > 0) {
							playList.getList().requestFocus();
							YassSong s = playList.getList().getFirstSelectedSong();
							if (s == null) {
								playList.getList().selectSong(0);
							} else {
								songInfo.setSong(s);
								songInfo.repaint();
							}
						}
						songList.repaint();
						playList.repaint();
					}
					if (c == KeyEvent.VK_LEFT && e.getModifiers() == 0) {
						groupsBox.requestFocus();
					}
				}
			});
		groupsBox.addKeyListener(
			new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					int c = e.getKeyCode();
					if (c == KeyEvent.VK_RIGHT && e.getModifiers() == 0) {
						groups.requestFocus();
						groups.repaint();
					}
					if (c == KeyEvent.VK_LEFT && e.getModifiers() == 0) {
						groups.requestFocus();
						groups.repaint();
					}
				}
			});
		actions.getPlayListBox().addKeyListener(
			new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					int c = e.getKeyCode();
					if ((c == KeyEvent.VK_RIGHT || c == KeyEvent.VK_LEFT) && e.getModifiers() == 0) {
						if (playList.getList().getRowCount() > 0) {
							playList.getList().requestFocus();
							YassSong s = playList.getList().getFirstSelectedSong();
							if (s == null) {
								playList.getList().selectSong(0);
							} else {
								songInfo.setSong(s);
								songInfo.repaint();
							}
							playList.repaint();
						}
					}
				}
			});

		songList.getSelectionModel().addListSelectionListener(
			new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					YassSong s = songList.getFirstSelectedSong();
					boolean isEmpty = songList.getRowCount() < 1;

					if (s != null) {
						playList.repaint();
					}
					if (s != null && songList.getShowLyrics() && s.getLyrics() == null) {
						songList.loadSongDetails(s, new YassTable());
					}

					if (s != null || isEmpty) {
						songInfo.setSong(s);
						songInfo.repaint();
					}
				}
			});
		playList.getList().getSelectionModel().addListSelectionListener(
			new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					YassSong s = playList.getList().getFirstSelectedSong();
					boolean isEmpty = playList.getList().getRowCount() < 1;

					if (s == null) {
						return;
					}

					if (s != null) {
						songList.repaint();
					}
					if (s != null && songList.getShowLyrics() && s.getLyrics() == null) {
						songList.loadSongDetails(s, new YassTable());
					}
					if (s != null || isEmpty) {
						songInfo.setSong(s);
						songInfo.repaint();
					}
				}
			});

		if (COMBINED_LIBRARY) {
			songInfo.add(playlistPanel, 0);
			songInfo.add(songPanel, 0);
			songInfo.add(groupsPanel, 0);
			songInfo.validate();

			songInfo.addComponentListener(
				new ComponentAdapter() {
					public void componentResized(ComponentEvent e) {
						int ww = songInfo.getWidth();
						int hh = songInfo.getHeight();

						int bb = 10;
						int w2 = groupsPanel.getPreferredSize().width;

						groupsPanel.setBounds(bb, bb, w2, hh - 20);

						int min = 200;
						int max = 500;
						int sw = ww / 4;
						sw = Math.max(min, Math.min(sw, max));
						songPanel.setBounds(bb + w2 + bb, bb, sw, hh - 20);

						int w3 = ww - bb - 250;
						songInfo.updateTextBounds(w3, bb + 250 + bb + 32, 250, hh - 250 - 3 * bb - 32);

						int w4 = bb + w2 + bb + sw + bb;
						playlistPanel.setBounds(w4, bb, 200, hh - 20);

						actions.updateDetails();
					}
				});
		} else {
			//libMainPanel.setLayout(new BorderLayout());
			//libMainPanel.add("Center", songPanel);
			//libMainPanel.add("West", groupsPanel);

			//JPanel libinfo = new JPanel(new BorderLayout());
			//libinfo.add("North", songInfo);
			//libinfo.add("Center", flow ? tab : playlistPanel);

			//libinfoPanel = new JPanel(new BorderLayout());
			//libinfoPanel.add("Center", libinfo);
			// panel.add("East", libinfoPanel);
		}

		actions.registerLibraryActions(panel);
		return panel;
	}

	// APPLICATION-SPECIFIC
	private static String[] app_argv = null;


	/**
	 *  Description of the Method
	 *
	 * @param  argv  Description of the Parameter
	 */
	public static void main(String argv[]) {
		app_argv = argv;

		try {
			if (javax.sound.midi.MidiSystem.getSequencer() == null) {
				System.out.println("MidiSystem sequencer unavailable.");
			} else if (javax.sound.sampled.AudioSystem.getMixer(null) == null) {
				System.out.println("AudioSystem unavailable.");
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("AudioSystem and MidiSystem Sequencer found.");


		if (PRELOAD_FOBS) {
			System.out.println("Library path: "+System.getProperty("java.library.path"));
			System.loadLibrary("fobs4jmf");
		}
		
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {

					final YassMain y = new YassMain();
					y.setCommandLine(YassMain.app_argv);

					System.out.println("Init...");
					y.init();
					System.out.println("Inited.");

					if (convert || midiFile != null) {
						String newf = y.getYassActions().createNewSong(midiFile, true);
						if (newf == null) {
							System.exit(0);
						}
						y.setCommandLine(new String[]{"-edit", newf});
					}
					
					if (correct) {
						if (txtFile != null) {
							y.getYassActions().openFiles(txtFile);
						} else {
							// ok
						}
						// finished
						System.exit(0);
					}

					final JFrame mainFrame = new JFrame(I18.get("yass_title"));
					mainFrame.setIconImage(new ImageIcon(mainFrame.getClass().getResource("/yass/yass-icon-16.png")).getImage());
					mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					mainFrame.addWindowListener(
						new WindowAdapter() {
							public void windowClosing(WindowEvent e) {
								if (!y.askStop()) {
									return;
								}

								y.setDefaultSize(((Component) e.getSource()).getSize());
								y.setDefaultLocation(((Component) e.getSource()).getLocation());

								//System.out.println("closing");
								e.getWindow().setVisible(false);
								e.getWindow().dispose();
								System.exit(0);
							}
						});
					mainFrame.add("Center", y);
					mainFrame.pack();

					y.onShow();

					mainFrame.setSize(y.getDefaultSize());

					y.setDefaultSize(y.getDefaultSize());

					Point p = y.getDefaultLocation();
					if (p != null) {
						mainFrame.setLocation(p);
					} else {
						mainFrame.setLocationRelativeTo(null);
					}

					System.out.println("Starting...");
					y.start();
					System.out.println("Loading...");
					y.load();

					mainFrame.setVisible(true);
					System.out.println("Ready. Let's go.");
				}
			});
	}
}

