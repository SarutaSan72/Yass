package yass;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    22. August 2007
 */
public class YassImport {
	static JTextPane staticPane;
	static File[] staticTargets;
	static File staticSourceFile;


	/**
	 *  Description of the Method
	 *
	 * @param  prop      Description of the Parameter
	 * @param  dFiles    Description of the Parameter
	 * @param  songList  Description of the Parameter
	 */
	public static void importFiles(YassSongList songList, YassProperties prop, File[] dFiles) {
		String songdir = prop.getProperty("song-directory");
		if (songdir == null || !new File(songdir).exists()) {
			JOptionPane.showMessageDialog(songList, I18.get("create_songdir_error"), "Import", JOptionPane.ERROR_MESSAGE);
			return;
		}
		YassSong targetSong = null;
		if ((dFiles.length == 1) && !dFiles[0].isDirectory()) {
			Vector<?> sel = songList.getSelectedSongs();
			if (sel != null || sel.size() == 1) {
				targetSong = (YassSong) sel.firstElement();
			}
		}

		String videoID = prop.getProperty("video-id");

		String importfolder = prop.getProperty("import-directory");

		final JFrame fh = new JFrame(I18.get("create_dialog_title"));
		fh.addWindowListener(
			new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					e.getWindow().dispose();
				}
			});
		final JTextPane pane = new JTextPane();
		pane.setContentType("text/html");
		pane.setEditable(false);
		JScrollPane scroll = new JScrollPane(pane);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		fh.add("Center", scroll);
		fh.pack();
		fh.setSize(800, 300);
		fh.setVisible(true);
		JPanel bPanel = new JPanel();
		final JButton b = new JButton(I18.get("create_dialog_cancel"));
		JButton b2 = new JButton("create_dialog_execute");
		bPanel.add(b2);
		bPanel.add(b);

		im = new ImportThread(fh, pane, songdir, importfolder, songList, targetSong, dFiles);
		im.videoID = videoID;

		b.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					im.interrupted = true;
					fh.dispose();
				}
			});
		b2.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					while (!im.finished) {
						try {
							Thread.currentThread();
							Thread.sleep(500);
						}
						catch (Exception ex) {}
					}

					im = new ImportThread(im);
					im.start();
					((JButton) e.getSource()).setEnabled(false);
					b.setText(I18.get("create_dialog_close"));
				}
			});
		fh.add("South", bPanel);

		im.simulate = true;
		im.start();
	}


	private static ImportThread im = null;


	/**
	 *  Description of the Class
	 *
	 * @author     Saruta
	 * @created    22. August 2007
	 */
	private static class ImportThread extends Thread {
		/**
		 *  Description of the Field
		 */
		public boolean interrupted = false, finished = false, simulate = false;
		StringBuffer buf = new StringBuffer();
		/**
		 *  Description of the Field
		 */
		Component parent;
		JTextPane pane = null;
		String songdir, importfolder;
		File[] iFiles;
		YassSong targetSong = null;
		YassSongList songList = null;
		String videoID = "";


		/**
		 *  Constructor for the ImportThread object
		 *
		 * @param  par  Description of the Parameter
		 * @param  p    Description of the Parameter
		 * @param  d    Description of the Parameter
		 * @param  id   Description of the Parameter
		 * @param  f    Description of the Parameter
		 * @param  ts   Description of the Parameter
		 * @param  sl   Description of the Parameter
		 */
		public ImportThread(Component par, JTextPane p, String d, String id, YassSongList sl, YassSong ts, File[] f) {
			parent = par;
			pane = p;
			songdir = d;
			importfolder = id;
			iFiles = f;
			targetSong = ts;
			songList = sl;
		}


		/**
		 *  Constructor for the ImportThread object
		 *
		 * @param  i  Description of the Parameter
		 */
		public ImportThread(ImportThread i) {
			buf = i.buf;
			parent = i.parent;
			pane = i.pane;
			songdir = i.songdir;
			importfolder = i.importfolder;
			iFiles = i.iFiles;
			songList = i.songList;
			targetSong = i.targetSong;
		}


		/**
		 *  Description of the Method
		 *
		 * @param  s  Description of the Parameter
		 */
		public void append(String s) {
			buf.append(s);
			SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						pane.setText("<html>" + buf.toString() + "</html>");
						pane.setCaretPosition(pane.getDocument().getLength());
					}
				});
		}


		/**
		 *  Main processing method for the ImportThread object
		 */
		public void run() {

			outer :
			for (int fi = 0; fi < iFiles.length && !interrupted; fi++) {
				File file = iFiles[fi];

				String absFilename = file.getAbsolutePath();

				String filename = file.getName();
				append(filename + "<br>");

				if (absFilename.startsWith(songdir)) {
					append("&nbsp;&nbsp;&nbsp;<font color=gray>Inside song library.</font><br>");
					continue;
				}

				String title = null;
				String artist = null;
				int i = filename.indexOf(" - ");
				if (i >= 0) {
					artist = filename.substring(0, i).trim();
					title = filename.substring(i + 3).trim();
					i = title.indexOf("[");
					if (i > 0) {
						title = title.substring(0, i).trim();
					}
					i = title.lastIndexOf(".");
					if (i > 0) {
						title = title.substring(0, i).trim();
					}
				}
				boolean takeTargetSong = false;
				if (title == null || artist == null) {
					if (targetSong != null) {
						append("&nbsp;&nbsp;&nbsp;<font color=gray>No Artist - Title. Import to selected song." + "</font><br>");
						title = targetSong.getTitle();
						artist = targetSong.getArtist();
						takeTargetSong = true;
					} else {
						append("&nbsp;&nbsp;&nbsp;<font color=red>No Artist - Title.</font><br>");
						continue;
					}
				}

				if (file.isDirectory()) {
					boolean hasMP3 = false;
					boolean hasTXT = false;
					File checkFiles[] = file.listFiles();
					for (i = 0; i < checkFiles.length; i++) {
						File chFile = checkFiles[i];
						String chAbsName = chFile.getAbsolutePath();
						String chAbsNameLow = chAbsName.toLowerCase();
						if (!hasMP3) {
							hasMP3 = chAbsNameLow.endsWith(".mp3");
						}
						if (!hasTXT) {
							hasTXT = chAbsNameLow.endsWith(".txt");
						}
					}
					if (!hasTXT) {
						append("&nbsp;&nbsp;&nbsp;<font color=red>No song data.</font><br>");
						continue outer;
					}
					if (!hasMP3) {
						append("&nbsp;&nbsp;&nbsp;<font color=red>No MP3 file.</font><br>");
						continue outer;
					}

					String imDirName = songdir;

					if (importfolder != null && importfolder.trim().length() > 0) {
						imDirName = importfolder;
					}
					File imDir = new File(imDirName);
					if (imDir.exists() && !imDir.isDirectory()) {
						append("&nbsp;&nbsp;&nbsp;<font color=red>Invalid import folder: " + imDirName + ".</font><br>");
						continue outer;
					} else if (!imDir.exists()) {
						imDir.mkdir();
					}
					File ddir = new File(imDir, filename);
					String ddirShortName = ddir.getAbsolutePath().substring(songdir.length() + 1);
					append("&nbsp;&nbsp;&nbsp;\u2192 " + "[import folder]" + "<br>");

					Vector<YassSong> targets = songList.findSong(artist, title, null);
					if (targets != null) {
						int ntarget = targets.size();
						int m = 0;
						String[] oldDirName = new String[ntarget];
						String[] oldDirShortName = new String[ntarget];
						File[] oldDir = new File[ntarget];
						for (Enumeration<YassSong> en = targets.elements(); en.hasMoreElements(); ) {
							YassSong song = (YassSong) en.nextElement();
							oldDirName[m] = song.getDirectory();
							oldDirShortName[m] = oldDirName[m].substring(songdir.length() + 1);
							oldDir[m] = new File(oldDirName[m]);
							append("&nbsp;&nbsp;&nbsp;\u2192 " + oldDirName[m] + "<br>");
							m++;
						}

						Object[] options = {"Replace", "Import", "Cancel"};
						if (simulate) {
							append("&nbsp;&nbsp;&nbsp;<font color=gray>\u219d Replace or import?</font><br>");
						} else {
							staticPane = new JTextPane();
							staticTargets = oldDir;
							staticSourceFile = file;
							staticPane.setEditable(false);
							staticPane.setBackground(new JLabel().getBackground());
							staticPane.setContentType("text/html");
							updateMessage(staticPane, staticTargets[0], staticSourceFile);

							JComboBox<?> selectCombo = new JComboBox<Object>(oldDirShortName);
							selectCombo.addActionListener(
								new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										JComboBox<?> selectCombo = (JComboBox<?>) e.getSource();
										int m = selectCombo.getSelectedIndex();
										if (m < 0) {
											return;
										}
										updateMessage(staticPane, staticTargets[m], staticSourceFile);
									}
								});

							JPanel selectMPanel = new JPanel(new BorderLayout());
							selectMPanel.add("West", new JLabel(m + " matching songs: "));
							selectMPanel.add("Center", selectCombo);

							JPanel selectPanel = new JPanel(new BorderLayout());
							selectPanel.add("North", selectMPanel);
							selectPanel.add("Center", staticPane);

							int ok = JOptionPane.showOptionDialog(parent, selectPanel, "Import", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
							m = selectCombo.getSelectedIndex();

							if (ok == JOptionPane.YES_OPTION) {
								// replace
								File oldParent = oldDir[m].getParentFile();
								YassUtils.deleteDir(oldDir[m]);
								ddir = new File(oldParent, filename);
								if (ddir.exists()) {
									YassUtils.deleteDir(ddir);
								}
								boolean success = file.renameTo(ddir);
								if (!success) {
									append("&nbsp;&nbsp;&nbsp;<font color=red>Fatal error: Cannot move " + absFilename + "<br>&nbsp;&nbsp;&nbsp;to <br>" + ddir.getAbsolutePath() + ".</font><br>");
									continue outer;
								}
								String dshortName = ddir.getAbsolutePath().substring(songdir.length() + 1);
								append("&nbsp;&nbsp;&nbsp;<font color=gray>Replaced as " + dshortName + "</font><br>");
								continue outer;
							} else if (ok == JOptionPane.NO_OPTION) {
								// import
								// continue below
							} else {
								append("&nbsp;&nbsp;&nbsp;<font color=red>Canceled.</font><br>");
								continue outer;
							}
						}
					}
					if (ddir.exists()) {
						if (simulate) {
							append("&nbsp;&nbsp;&nbsp;\u2192 " + ddirShortName + "<br>");
							append("&nbsp;&nbsp;&nbsp;\u219d Replace?<br>");
						} else {
							int ok = JOptionPane.showConfirmDialog(parent, "<html><i>" + filename + "</i><br>&nbsp;&nbsp;&nbsp;\u2192 " + ddirShortName + "<br><br>You already have that song.<br><br>Replace?</html>", "Import", JOptionPane.YES_NO_OPTION);
							if (ok != JOptionPane.OK_OPTION) {
								append("&nbsp;&nbsp;&nbsp;<font color=red>Canceled.</font><br>");
								continue outer;
							}
							boolean success = YassUtils.deleteDir(ddir);
							if (!success) {
								append("&nbsp;&nbsp;&nbsp;<font color=red>Fatal error: Cannot replace " + ddir.getName() + "</font><br>");
								continue outer;
							}
						}
					}
					boolean success = true;
					if (!simulate) {
						success = file.renameTo(ddir);
						if (!success) {
							append("&nbsp;&nbsp;&nbsp;<font color=red>Fatal error: Cannot create" + ddir.getName() + "</font><br>");
							continue outer;
						} else {
							append("&nbsp;&nbsp;&nbsp;<font color=gray>Moved to " + ddirShortName + "</font><br>");
						}
					}
					continue outer;
					// end of directory import
				}

				String dDirName = null;
				if (takeTargetSong) {
					dDirName = targetSong.getDirectory();
				} else {
					Vector<?> targets = songList.findSong(artist, title, null);
					if (targets != null) {
						int ntarget = targets.size();
						int m = 0;
						String[] oldDirName = new String[ntarget];
						String[] oldDirShortName = new String[ntarget];
						File[] oldDir = new File[ntarget];
						for (Enumeration<?> en = targets.elements(); en.hasMoreElements(); ) {
							YassSong song = (YassSong) en.nextElement();
							oldDirName[m] = song.getDirectory();
							oldDirShortName[m] = oldDirName[m].substring(songdir.length() + 1);
							oldDir[m] = new File(oldDirName[m]);
							if (ntarget > 1) {
								append("&nbsp;&nbsp;&nbsp;\u2192 " + oldDirShortName[m] + "<br>");
							}
							m++;
						}
						dDirName = oldDirName[0];

						if (simulate) {
							if (ntarget > 1) {
								append("&nbsp;&nbsp;&nbsp;<font color=gray>\u219d Select?</font><br>");
								continue;
							}
						} else {
							if (ntarget > 1) {
								staticTargets = oldDir;
								staticSourceFile = file;
								staticPane.setEditable(false);
								staticPane.setBackground(new JLabel().getBackground());
								staticPane.setContentType("text/html");
								updateMessageFile(staticPane, staticTargets[0], staticSourceFile);

								JComboBox<?> selectCombo = new JComboBox<Object>(oldDirShortName);
								selectCombo.addActionListener(
									new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											JComboBox<?> selectCombo = (JComboBox<?>) e.getSource();
											int m = selectCombo.getSelectedIndex();
											if (m < 0) {
												return;
											}
											updateMessageFile(staticPane, staticTargets[m], staticSourceFile);
										}
									});

								JPanel selectMPanel = new JPanel(new BorderLayout());
								selectMPanel.add("West", new JLabel(m + " matching songs: "));
								selectMPanel.add("Center", selectCombo);

								JPanel selectPanel = new JPanel(new BorderLayout());
								selectPanel.add("North", selectMPanel);
								selectPanel.add("Center", staticPane);

								int ok = JOptionPane.showConfirmDialog(parent, selectPanel, "Import", JOptionPane.OK_CANCEL_OPTION);
								if (ok == JOptionPane.OK_OPTION) {
									// replace
									m = selectCombo.getSelectedIndex();
									dDirName = oldDirName[m];
								} else {
									append("&nbsp;&nbsp;&nbsp;<font color=red>Canceled.</font><br>");
									continue outer;
								}
							}
						}
					} else {
						append("&nbsp;&nbsp;&nbsp;<font color=red>You don't have " + artist + " - " + title + ".</font><br>");
						continue outer;
					}
				}

				File dDir = new File(dDirName);
				File dFile = new File(dDir, filename);
				String dDirShortName = dDirName.substring(songdir.length() + 1);
				append("&nbsp;&nbsp;&nbsp;\u2192 " + dDirShortName + "<br>");

				String filenameLow = filename.toLowerCase();
				boolean isCO = filenameLow.indexOf("[co]") > 0 && (filenameLow.endsWith(".jpg") || filenameLow.endsWith(".jpeg"));
				boolean isBG = filenameLow.indexOf("[bg]") > 0 && (filenameLow.endsWith(".jpg") || filenameLow.endsWith(".jpeg"));
				boolean isVD = filenameLow.endsWith(".mpg") || filenameLow.endsWith(".mpeg") || filenameLow.endsWith(".avi");
				boolean isTXT = filenameLow.endsWith(".txt");

				if (!isCO && !isBG && (filenameLow.endsWith(".jpg") || filenameLow.endsWith(".jpeg"))) {
					int ow = 0;
					int oh = 0;
					try {
						BufferedImage img = YassUtils.readImage(file);
						ow = img.getWidth();
						oh = img.getHeight();
					}
					catch (Exception e) {}
					if (ow > 10 && Math.abs(ow - oh) < 50) {
						isCO = true;
					} else if (ow > oh + 50) {
						isBG = true;
					}
					if (isCO) {
						append("&nbsp;&nbsp;&nbsp;<font color=gray>Square dimension. Import as cover.</font><br>");
						dFile = new File(dDir, artist + " - " + title + " [CO].jpg");
					}
					if (isBG) {
						append("&nbsp;&nbsp;&nbsp;<font color=gray>Rectangular dimension. Import as background.</font><br>");
						dFile = new File(dDir, artist + " - " + title + " [BG].jpg");
					}
				}

				boolean hasCO = false;
				boolean hasBG = false;
				boolean hasVD = false;
				boolean hasTXT = false;
				Vector<String> haveVersions = new Vector<String>();
				String txtName = "";
				File coFile = null;
				File bgFile = null;
				File vdFile = null;
				File txtFile = null;
				boolean dtxtFound = false;
				YassTable table = null;
				YassTable dTable = new YassTable();
				if (isTXT) {
					table = new YassTable();
					table.removeAllRows();
					table.loadFile(absFilename);
				}
				File dFiles[] = dDir.listFiles();
				for (i = 0; i < dFiles.length; i++) {
					File ddFile = dFiles[i];
					String dAbsName = ddFile.getAbsolutePath();
					String dAbsNameLow = dAbsName.toLowerCase();
					hasCO = dAbsNameLow.indexOf("[co]") > 0 && (dAbsNameLow.endsWith(".jpg") || dAbsNameLow.endsWith(".jpeg"));
					if (hasCO && isCO) {
						coFile = ddFile;
					}
					hasBG = dAbsNameLow.indexOf("[bg]") > 0 && (dAbsNameLow.endsWith(".jpg") || dAbsNameLow.endsWith(".jpeg"));
					if (hasBG && isBG) {
						bgFile = ddFile;
					}
					hasVD = dAbsNameLow.endsWith(".mpg") || dAbsNameLow.endsWith(".mpeg") || dAbsNameLow.endsWith(".avi");
					if (hasVD && isVD) {
						vdFile = ddFile;
					}
					hasTXT = dAbsNameLow.endsWith(".txt");
					if (hasTXT && !dtxtFound) {
						// get non-version txt file
						dtxtFound = true;
						dTable.removeAllRows();
						dTable.loadFile(dAbsName);
						YassRow row = dTable.getCommentRow("TITLE:");
						if (row != null) {
							if (row.hasVersion()) {
								haveVersions.addElement(row.getVersion());
								if (txtFile == null) {
									txtName = dAbsName;
									txtFile = ddFile;
								}
							} else {
								txtName = dAbsName;
								txtFile = ddFile;
							}
						}
					}
					if (isTXT && table.equalsData(dTable)) {
						append("&nbsp;&nbsp;&nbsp;<font color=gray>Identical song data.</font><br>");
						continue outer;
					}
				}
				hasCO = coFile != null;
				hasBG = bgFile != null;
				hasVD = vdFile != null;
				hasTXT = txtFile != null;

				if (isCO) {
					if (dFile.exists()) {
						hasCO = true;
					}
					if (simulate) {
						if (hasCO) {
							append("&nbsp;&nbsp;&nbsp;<font color=gray>\u219d Replace?</font><br>");
						}
						if (hasTXT) {
							append("&nbsp;&nbsp;&nbsp;<font color=gray>Update COVER tag.</font><br>");
						}
						continue outer;
					} else {
						if (hasCO) {
							int ow = 0;
							int oh = 0;
							int nw = 0;
							int nh = 0;

							try {
								BufferedImage img = YassUtils.readImage(coFile);
								ow = img.getWidth();
								oh = img.getHeight();
								img = YassUtils.readImage(new File(absFilename));
								nw = img.getWidth();
								nh = img.getHeight();
							}
							catch (Exception e) {
							}

							int ok = JOptionPane.showConfirmDialog(parent, "<html><i>" + filename + "</i><br>&nbsp;&nbsp;&nbsp;\u2192 " + dDirShortName + "<br><br><table><tr><td>Current cover<td>New cover</tr><tr><td><img width=200 height=200 src=\"file:/" + coFile + "\"><td><img width=200 height=200 src=\"file:/" + absFilename + "\"></tr><tr><td>Dimension: " + ow + " x " + oh + "<td>" + nw + " x " + nh + "</tr></table><br><br>Replace?</html>", "Import", JOptionPane.YES_NO_OPTION);
							if (ok != JOptionPane.OK_OPTION) {
								append("&nbsp;&nbsp;&nbsp;<font color=red>Canceled.</font><br>");
								continue outer;
							}
						}
						if (hasCO) {
							coFile.delete();
						}
						if (dFile.exists()) {
							dFile.delete();
						}
						boolean success = file.renameTo(dFile);
						if (!success) {
							YassUtils.copyFile(file, dFile);
						}
						if (hasTXT) {
							YassTable txtTable = new YassTable();
							txtTable.init(songList.getActions().getProperties());
							txtTable.loadFile(txtName);
							YassAutoCorrect.insertCover(txtTable, dFile);
							YassAutoCorrect.sortComments(txtTable);
							txtTable.storeFile(txtName);
							append("&nbsp;&nbsp;&nbsp;<font color=gray>Updated COVER tag.</font><br>");
						}

						try {
							BufferedImage img = YassUtils.readImage(dFile);
							int w = img.getWidth();
							int h = img.getHeight();
							if (w > 800 || h > 800) {
								BufferedImage bufferedImage = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);
								Graphics2D g2d = bufferedImage.createGraphics();
								g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
								g2d.drawImage(img, 0, 0, 800, 800, null);
								g2d.dispose();
								img.flush();
								javax.imageio.ImageIO.write(bufferedImage, "jpg", dFile);
								append("&nbsp;&nbsp;&nbsp;<font color=gray>Downscaled image to 800x800.</font><br>");
							}
						}
						catch (Exception e) {}

						continue outer;
					}
				} else if (isBG) {
					if (dFile.exists()) {
						hasBG = true;
					}
					if (simulate) {
						if (hasBG) {
							append("&nbsp;&nbsp;&nbsp;<font color=gray>\u219d Replace?</font><br>");
						}
						if (hasTXT) {
							append("&nbsp;&nbsp;&nbsp;<font color=gray>Update BACKGROUND tag.</font><br>");
						}
						continue outer;
					} else {
						if (hasBG) {
							int ow = 0;
							int oh = 0;
							int nw = 0;
							int nh = 0;
							try {
								BufferedImage img = YassUtils.readImage(bgFile);
								ow = img.getWidth();
								oh = img.getHeight();
								img = YassUtils.readImage(new File(absFilename));
								nw = img.getWidth();
								nh = img.getHeight();
							}
							catch (Exception e) {}

							int ok = JOptionPane.showConfirmDialog(parent, "<html><i>" + filename + "</i><br>&nbsp;&nbsp;&nbsp;\u2192 " + dDirShortName + ".<br><br><table><tr><td>Current background<td>New background</tr><tr><td><img width=200 height=150 src=\"file:/" + bgFile + "\"><td><img width=200 height=150 src=\"file:/" + absFilename + "\"></tr><tr><td>Dimension: " + ow + " x " + oh + "<td>" + nw + " x " + nh + "</tr></table><br><br>Replace?</html>", "Import", JOptionPane.YES_NO_OPTION);
							if (ok != JOptionPane.OK_OPTION) {
								append("&nbsp;&nbsp;&nbsp;<font color=red>Canceled.</font><br>");
								continue outer;
							}
						}
						if (hasBG) {
							bgFile.delete();
						}
						if (dFile.exists()) {
							dFile.delete();
						}
						boolean success = file.renameTo(dFile);
						if (!success) {
							YassUtils.copyFile(file, dFile);
						}

						if (hasTXT) {
							YassTable txtTable = new YassTable();
							txtTable.loadFile(txtName);
							YassAutoCorrect.insertBackground(txtTable, dFile);
							YassAutoCorrect.sortComments(txtTable);
							txtTable.storeFile(txtName);
							append("&nbsp;&nbsp;&nbsp;<font color=gray>Updated BACKGROUND tag.</font><br>");
						}

						try {
							BufferedImage img = YassUtils.readImage(dFile);
							int w = img.getWidth();
							int h = img.getHeight();
							if (w > 1024 || h > 768) {
								w = (int) (768 * w / (double) h);
								h = 768;
								BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
								Graphics2D g2d = bufferedImage.createGraphics();
								g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
								g2d.drawImage(img, 0, 0, w, h, null);
								g2d.dispose();
								img.flush();
								javax.imageio.ImageIO.write(bufferedImage, "jpg", dFile);
								append("&nbsp;&nbsp;&nbsp;<font color=gray>Downscaled image to " + w + "x" + h + ".</font><br>");
							}
						}
						catch (Exception e) {}

						continue outer;
					}
				} else if (isVD) {
					if (dFile.exists()) {
						hasVD = true;
					}
					if (simulate) {
						if (hasVD) {
							append("&nbsp;&nbsp;&nbsp;<font color=gray>\u219d Replace?</font><br>");
						}
						if (hasTXT) {
							append("&nbsp;&nbsp;&nbsp;<font color=gray>Update VIDEO/VIDEOGAP tags.</font><br>");
						}
						continue outer;
					} else {
						if (hasVD) {
							int ok = JOptionPane.showConfirmDialog(parent, "<html><i>" + filename + "</i><br>&nbsp;&nbsp;&nbsp;\u2192 " + dDirShortName + ".<br><br>Replace?</html>", "Import", JOptionPane.YES_NO_OPTION);
							if (ok != JOptionPane.OK_OPTION) {
								append("&nbsp;&nbsp;&nbsp;<font color=red>Canceled.</font><br>");
								continue outer;
							}
						}
						if (hasVD) {
							vdFile.delete();
						}
						if (dFile.exists()) {
							dFile.delete();
						}
						boolean success = file.renameTo(dFile);
						if (!success) {
							YassUtils.copyFile(file, dFile);
						}

						if (hasTXT) {
							YassTable txtTable = new YassTable();
							txtTable.loadFile(txtName);
							txtTable.setVideo(filename);
							String vg = YassUtils.getWildcard(filename, videoID);
							if (vg == null) {
								vg = "0";
							}
							txtTable.setVideoGap(vg);
							txtTable.storeFile(txtName);
							append("&nbsp;&nbsp;&nbsp;<font color=gray>Updated VIDEO/VIDEOGAP tags.</font><br>");
						}
						continue outer;
					}
				} else if (!isTXT && dFile.exists()) {
					if (simulate) {
						append("&nbsp;&nbsp;&nbsp;<font color=gray>\u219d Replace?</font><br>");
						continue outer;
					} else {
						int ok = JOptionPane.showConfirmDialog(parent, "<html><i>" + filename + "</i><br>&nbsp;&nbsp;&nbsp;\u2192 " + dDirShortName + "<br><br>Replace existing file?</html>", "Import", JOptionPane.YES_NO_OPTION);
						if (ok != JOptionPane.OK_OPTION) {
							append("&nbsp;&nbsp;&nbsp;<font color=red>Canceled.</font><br>");
							continue outer;
						}
						dFile.delete();
						boolean success = file.renameTo(dFile);
						if (!success) {
							YassUtils.copyFile(file, dFile);
						}
						continue outer;
					}
				} else if (isTXT && hasTXT) {
					String v = "UPDATE";
					if (haveVersions.contains(v)) {
						int k = 1;
						String v2 = v + k;
						while (haveVersions.contains(v2)) {
							k++;
							v2 = v + k;
						}
						v = v2;
					}

					if (simulate) {
						append("&nbsp;&nbsp;&nbsp;<font color=gray>\u219d Replace or add version?</font><br>");
						continue outer;
					} else {
						int ok = 0;
						Object[] options = {"Replace", "Add Version", "Cancel"};
						if (haveVersions.size() == 0) {
							ok = JOptionPane.showOptionDialog(parent, "<html><i>" + filename + "</i><br><br>You already have a data file in " + dDirShortName + "<br><br>Replace it or add as " + v + " version?</html>", "Import", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
						} else {
							ok = JOptionPane.showOptionDialog(parent, "<html><i>" + filename + "</i><br><br>You already have a data file in " + dDirShortName + "<br>with versions " + haveVersions.toString() + "<br><br>Replace it or add as " + v + " version?</html>", "Import", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
						}
						if (ok == JOptionPane.NO_OPTION) {
							// add version
							File fv = new File(dDir, artist + " - " + title + " [" + v + "].txt");
							YassRow r = table.getCommentRow("TITLE:");
							r.setVersion(v);
							table.storeFile(fv.getAbsolutePath());
							boolean success = fv.exists();
							if (!success) {
								append("&nbsp;&nbsp;&nbsp;<font color=red>Fatal error: Cannot create " + fv.getAbsolutePath() + "</font><br>");
								continue outer;
							}
							file.delete();
							continue outer;
						} else if (ok == JOptionPane.YES_OPTION) {
							// replace
							if (!dFile.delete()) {
								append("&nbsp;&nbsp;&nbsp;<font color=red>Fatal error: Cannot replace " + filename + "</font><br>");
								continue outer;
							}
							boolean success = file.renameTo(dFile);
							if (!success) {
								YassUtils.copyFile(file, dFile);
							}
							continue outer;
						} else {
							// cancel
							append("&nbsp;&nbsp;&nbsp;<font color=red>Canceled.</font><br>");
							continue outer;
						}
					}
				} else {
					if (simulate) {
					} else {
						boolean success = file.renameTo(dFile);
						if (!success) {
							append("&nbsp;&nbsp;&nbsp;<font color=red>Fatal error: Cannot move to <br>" + dDirShortName + "</font><br>");
							continue outer;
						}
						append("&nbsp;&nbsp;&nbsp;<font color=gray>Moved to " + dDir.getAbsolutePath() + "</font><br>");
					}
				}
			}
			if (simulate) {
				append("<b>Simulation finished. Execute?</b><br><br>");
			} else {
				append("<b>Import finished.</b><br><br>");
			}
			finished = true;
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  doc      Description of the Parameter
	 * @param  oldFile  Description of the Parameter
	 * @param  newFile  Description of the Parameter
	 */
	public static void updateMessage(JTextPane doc, File oldFile, File newFile) {
		File[] oldFiles = oldFile.listFiles();
		File[] newFiles = newFile.listFiles();

		int k;

		int imgc = 0;
		StringBuffer oldBuffer = new StringBuffer();
		for (k = 0; k < Math.min(10, oldFiles.length); k++) {
			String s = oldFiles[k].getName();
			oldBuffer.append(s + "<br>");
			s = s.toLowerCase();
			if (s.endsWith(".mp3") || s.endsWith(".mpg") || s.endsWith(".mpeg") || s.endsWith(".avi")) {
				String filesize = ((int) (10 * oldFiles[k].length() / 1024.0) / 10.0) + "";
				oldBuffer.append("&nbsp;&nbsp;&nbsp;Size: " + filesize + " kB<br>");
			}
			if (s.endsWith(".jpg") || s.endsWith(".jpeg")) {
				imgc++;
				if (imgc < 4) {
					try {
						BufferedImage img = YassUtils.readImage(oldFiles[k]);
						int ow = img.getWidth();
						int oh = img.getHeight();
						int w = (int) (80 * ow / (double) oh);
						int h = 80;
						oldBuffer.append("&nbsp;&nbsp;&nbsp;<img src=\"file:/" + oldFiles[k].getAbsolutePath() + "\" width=" + w + " height=" + h + "><br>&nbsp;&nbsp;&nbsp;Dimension: " + ow + " x " + oh + "<br><br>");
					}
					catch (Exception e) {
					}
				}
			}
		}
		if (k < oldFiles.length) {
			oldBuffer.append("[more files]");
		}

		imgc = 0;
		StringBuffer newBuffer = new StringBuffer();
		for (k = 0; k < Math.min(10, newFiles.length); k++) {
			String s = newFiles[k].getName();
			newBuffer.append(s + "<br>");
			s = s.toLowerCase();
			if (s.endsWith(".mp3") || s.endsWith(".mpg") || s.endsWith(".mpeg")) {
				String filesize = ((int) (10 * newFiles[k].length() / 1024.0) / 10.0) + "";
				newBuffer.append("&nbsp;&nbsp;&nbsp;Size: " + filesize + " kB<br>");
			}
			if (s.endsWith(".jpg") || s.endsWith(".jpeg")) {
				imgc++;
				if (imgc < 4) {
					try {
						BufferedImage img = YassUtils.readImage(newFiles[k]);
						int ow = img.getWidth();
						int oh = img.getHeight();
						int w = (int) (80 * ow / (double) oh);
						int h = 80;
						newBuffer.append("&nbsp;&nbsp;&nbsp;<img src=\"file:/" + newFiles[k].getAbsolutePath() + "\" width=" + w + " height=" + h + "><br>&nbsp;&nbsp;&nbsp;Dimension: " + ow + " x " + oh + "<br><br>");
					}
					catch (Exception e) {}
				}
			}
		}
		if (k < newFiles.length) {
			newBuffer.append("[more files]");
		}
		doc.setText("<table><tr><td>Current song<td>Replace with:</tr><tr><td>" + oldBuffer.toString() + "<td>" + newBuffer.toString() + "</tr></table><br><br>Replace or move to import folder?");
	}


	/**
	 *  Description of the Method
	 *
	 * @param  doc      Description of the Parameter
	 * @param  oldFile  Description of the Parameter
	 * @param  newFile  Description of the Parameter
	 */
	public static void updateMessageFile(JTextPane doc, File oldFile, File newFile) {
		File[] oldFiles = oldFile.listFiles();

		int k;

		int imgc = 0;
		StringBuffer oldBuffer = new StringBuffer();
		for (k = 0; k < Math.min(10, oldFiles.length); k++) {
			String s = oldFiles[k].getName();
			oldBuffer.append(s + "<br>");
			s = s.toLowerCase();
			if (s.endsWith(".mp3") || s.endsWith(".mpg") || s.endsWith(".mpeg") || s.endsWith(".avi")) {
				String filesize = ((int) (10 * oldFiles[k].length() / 1024.0) / 10.0) + "";
				oldBuffer.append("&nbsp;&nbsp;&nbsp;Size: " + filesize + " kB<br>");
			}
			if (s.endsWith(".jpg") || s.endsWith(".jpeg")) {
				imgc++;
				if (imgc < 4) {
					try {
						BufferedImage img = YassUtils.readImage(oldFiles[k]);
						int ow = img.getWidth();
						int oh = img.getHeight();
						int w = (int) (80 * ow / (double) oh);
						int h = 80;
						oldBuffer.append("&nbsp;&nbsp;&nbsp;<img src=\"file:/" + oldFiles[k].getAbsolutePath() + "\" width=" + w + " height=" + h + "><br>&nbsp;&nbsp;&nbsp;Dimension: " + ow + " x " + oh + "<br><br>");
					}
					catch (Exception e) {
					}
				}
			}
		}
		if (k < oldFiles.length) {
			oldBuffer.append("[more files]");
		}

		StringBuffer newBuffer = new StringBuffer();
		String s = newFile.getName();
		newBuffer.append(s + "<br>");
		s = s.toLowerCase();
		if (s.endsWith(".mp3") || s.endsWith(".mpg") || s.endsWith(".mpeg")) {
			String filesize = ((int) (10 * newFile.length() / 1024.0) / 10.0) + "";
			newBuffer.append("&nbsp;&nbsp;&nbsp;Size: " + filesize + " kB<br>");
		}
		if (s.endsWith(".jpg") || s.endsWith(".jpeg")) {
			try {
				BufferedImage img = YassUtils.readImage(newFile);
				int ow = img.getWidth();
				int oh = img.getHeight();
				int w = (int) (80 * ow / (double) oh);
				int h = 80;
				newBuffer.append("&nbsp;&nbsp;&nbsp;<img src=\"file:/" + newFile.getAbsolutePath() + "\" width=" + w + " height=" + h + "><br>&nbsp;&nbsp;&nbsp;Dimension: " + ow + " x " + oh + "<br><br>");
			}
			catch (Exception e) {}
		}

		doc.setText("<table><tr><td>Current song<td>New File:</tr><tr><td>" + oldBuffer.toString() + "<td>" + newBuffer.toString() + "</tr></table><br><br>Import to this song?");
	}


	/**
	 *  Description of the Method
	 *
	 * @param  prop      Description of the Parameter
	 * @param  t         Description of the Parameter
	 * @param  songList  Description of the Parameter
	 * @return           Description of the Return Value
	 */
	public static String importTable(YassSongList songList, YassProperties prop, YassTable t) {
		String title = t.getTitle();
		String artist = t.getArtist();
		String version = t.getVersion();
		if (title == null || artist == null) {
			JOptionPane.showMessageDialog(songList, "<html>Text must specify song title and artist.</html>", "Import Text", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		String dir = prop.getProperty("temp-dir");
		String at = dir + File.separator + artist + " - " + title + ".txt";
		if (version != null) {
			at = dir + File.separator + artist + " - " + title + " [" + version + "].txt";
		}
		File f = new File(at);
		if (f.exists()) {
			f.delete();
		}
		t.storeFile(at);

		importFiles(songList, prop, new File[]{new File(at)});
		return null;
	}

}

