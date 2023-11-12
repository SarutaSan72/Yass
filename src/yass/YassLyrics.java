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

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.swing.JTextComponentSpellChecker;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassLyrics extends JPanel implements TabChangeListener, YassSheetListener {
	private static final long serialVersionUID = -2873881715263100606L;
	private JTextPane lyricsArea = null;
	private JScrollPane lyricsScrollPane;
	private YassAutoCorrect auto;
	private YassProperties prop;
	private YassTable table;
	private YassSheet sheet;

	private Style notLongStyle, longStyle, notSelectStyle, selectStyle,
			goldenStyle, freeStyle, rapStyle, rapgoldenStyle, notGoldenOrFreeStyle;

	private Color red = new Color(255, 240, 240);
	private Color redDarkMode = new Color(255, 240, 240);


	Color blue = Color.blue;

	private Color selection = new Color(180, 200, 230);
	private Color selectionDarkMode = new Color(80, 100, 130);

	public static final  Color whitetrans = new Color(225,225,225,210);
	public static final  Color whitetransDarkMode = new Color(70,70,70, 200);


	private Color nofontBG = new Color(1f, 1f, 1f, 0f); // transparent

	private Color[] colorSet = new Color[YassSheet.COLORSET_COUNT];
	private Color errBackground, minorerrBackground;
	private Font lineNumberFont = new Font("SansSerif", Font.PLAIN, 10);
	private LineNumbers lineNumbers;
	private int fontSize = 16;

	private boolean preventFireUpdate = false;
	private boolean preventHyphenKeys = true;

	private Hashtable<Object, Object> spellCheckers;

	private JTextComponentSpellChecker spellCheckerComp = null;
	private FindReplace frDialog;

	private long lastTime = -1;
	private String lastTimeString = "";
	private Font big = new Font("SansSerif", Font.BOLD, 22);

	private static String userdir = System.getProperty("user.home")
			+ File.separator + ".yass" + File.separator + "spell";

	private int mismatch = 0;

	/**
	 * Description of the Method
	 *
	 * @param onoff
	 *            Description of the Parameter
	 */
	public void tabChanged(boolean onoff) {
	}

	public void setColors(Color[] c) {
		System.arraycopy(c, 0, colorSet, 0, colorSet.length);
		errBackground = colorSet[YassSheet.COLOR_ERROR];
		minorerrBackground = colorSet[YassSheet.COLOR_WARNING];
	}

	/**
	 * Description of the Method
	 *
	 * @param onoff
	 *            Description of the Parameter
	 */
	public void enableHyphenKeys(boolean onoff) {
		preventHyphenKeys = !onoff;
	}

	/**
	 * Description of the Method
	 *
	 * @return Description of the Return Value
	 */
	public boolean hyphenKeysEnabled() {
		return !preventHyphenKeys;
	}

	/**
	 * Description of the Method
	 *
	 * @param prop
	 *            Description of the Parameter
	 */
	public void init(YassProperties prop) {
		String fontSizeString = prop.getProperty("lyrics-font-size");
		try {
			int i = Integer.parseInt(fontSizeString);
			if (i > 0) {
				fontSize = i;
			}
		} catch (Exception e) {
		}
		StyleConstants.setFontSize(notLongStyle, fontSize);
	}

	public void posChanged(YassSheet source, double posMs) {
	}
	public void rangeChanged(YassSheet source, int minH, int maxH, int minB, int maxB) {
	}
	public void colorsChanged() {
		if (sheet != null) {
			if (isEditable()) {
				StyleConstants.setForeground(notLongStyle, sheet.darkMode ? YassSheet.blackDarkMode : YassSheet.dkGray);
				StyleConstants.setForeground(notSelectStyle, sheet.darkMode ? YassSheet.blackDarkMode : YassSheet.dkGray);
			}
			else {
				StyleConstants.setForeground(notLongStyle, sheet.darkMode ? YassSheet.dkGrayDarkMode : YassSheet.dkGray);
				StyleConstants.setForeground(notSelectStyle, sheet.darkMode ? YassSheet.dkGrayDarkMode : YassSheet.dkGray);
			}
			StyleConstants.setBackground(notSelectStyle, nofontBG); // transparent
			lyricsArea.setSelectionColor(sheet.darkMode ? selectionDarkMode : selection);
			lyricsArea.setSelectedTextColor(sheet.darkMode ? YassSheet.blackDarkMode : YassSheet.black);
			StyleConstants.setForeground(selectStyle, sheet.darkMode ? YassSheet.blackDarkMode : YassSheet.black);
			StyleConstants.setBackground(selectStyle, sheet.darkMode ? selectionDarkMode : selection);
		}
	}
	public void propsChanged(YassSheet source) {
		colorsChanged();

		lyricsScrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
			protected JButton createZeroButton() {
				JButton button = new JButton("");
				Dimension zeroDim = new Dimension(0,0);
				button.setPreferredSize(zeroDim);
				button.setMinimumSize(zeroDim);
				button.setMaximumSize(zeroDim);
				return button;
			}
			@Override
			protected JButton createDecreaseButton(int orientation) {
				JButton b = createZeroButton();
				b.setBackground(sheet.darkMode? YassSheet.hiGrayDarkMode : YassSheet.hiGray);
				b.setForeground(sheet.darkMode? YassSheet.HI_GRAY_2_DARK_MODE : YassSheet.HI_GRAY_2);
				return b;
			}
			protected JButton createIncreaseButton(int orientation) {
				JButton b = createZeroButton();
				b.setBackground(sheet.darkMode? YassSheet.hiGrayDarkMode : YassSheet.hiGray);
				return b;
			}
			@Override
			protected void configureScrollBarColors() {
				this.thumbColor = sheet.darkMode? YassSheet.hiGray : YassSheet.hiGray;
				this.thumbDarkShadowColor = sheet.darkMode? YassSheet.dkGray : YassSheet.dkGray;
				this.trackColor = sheet.darkMode? YassSheet.HI_GRAY_2_DARK_MODE : YassSheet.HI_GRAY_2;
			}
		});
		repaint();
	}

	/**
	 * Constructor for the YassLyrics object
	 *
	 * @param p
	 *            Description of the Parameter
	 */
	public YassLyrics(YassProperties p) {
		prop = p;

		spellCheckers = new Hashtable<Object,Object>();
		StringTokenizer st = new StringTokenizer(prop.getProperty("dicts"), "|");
		while (st.hasMoreTokens()) {
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), "()-_");
			String language = st2.nextToken();
			String country = st2.hasMoreTokens() ? st2.nextToken() : "";
			String lc = country.length() > 0 ? language + "_" + country
					: language;
			spellCheckers.put(lc, lc);
		}

		StyleContext sc = new StyleContext();
		DefaultStyledDocument doc = new DefaultStyledDocument(sc);

		// Ok, line not too long

		notLongStyle = sc.addStyle(null, null);
		StyleConstants.setLeftIndent(notLongStyle, 5);
		StyleConstants.setRightIndent(notLongStyle, 10);
		StyleConstants.setFontFamily(notLongStyle, "SansSerif");
		StyleConstants.setFontSize(notLongStyle, fontSize);
		StyleConstants.setSpaceAbove(notLongStyle, 0);
		StyleConstants.setSpaceBelow(notLongStyle, 0);
		// StyleConstants.setForeground(notLongStyle, dkGray); --> see propsChanged
		StyleConstants.setStrikeThrough(notLongStyle, false);

		// Line too long
		longStyle = sc.addStyle(null, null);
		StyleConstants.setStrikeThrough(longStyle, true);
		// not selected
		notSelectStyle = sc.addStyle(null, null);
		//StyleConstants.setForeground(notSelectStyle, dkGray); --> see propsChanged
		//StyleConstants.setBackground(notSelectStyle, nofontBG); --> see propsChanged
		// selected
		selectStyle = sc.addStyle(null, null);
		// StyleConstants.setForeground(selectStyle, black); --> see propsChanged
		// StyleConstants.setBackground(selectStyle, lyricsArea.getSelectionColor()); --> see propsChanged
		// golden
		goldenStyle = sc.addStyle(null, null);
		StyleConstants.setBold(goldenStyle, true);
		freeStyle = sc.addStyle(null, null);
		StyleConstants.setItalic(freeStyle, true);
		rapStyle = sc.addStyle(null, null);
		StyleConstants.setFontFamily(rapStyle, "Monospaced");
		rapgoldenStyle = sc.addStyle(null, null);
		StyleConstants.setFontFamily(rapgoldenStyle, "Monospaced");
		StyleConstants.setBold(rapgoldenStyle, true);
		notGoldenOrFreeStyle = sc.addStyle(null, null);
		StyleConstants.setBold(notGoldenOrFreeStyle, false);
		StyleConstants.setItalic(notGoldenOrFreeStyle, false);

		lineNumbers = new LineNumbers();
		lyricsArea = new JTextPane(doc) {
			private static final long serialVersionUID = -639942626000500351L;

			public void paint(Graphics g) {
				super.paint(g);
			}

			public void paintComponent(Graphics g) {

				if (mismatch != 0)
					g.setColor(sheet.darkMode ? redDarkMode : red);
				else {
					g.setColor(lyricsArea.isEditable()
							? (sheet.darkMode ? YassSheet.hiGrayDarkMode : YassSheet.white)
							: (sheet.darkMode ? new Color(73,73,73,210) : whitetrans));
				}
				Rectangle r = ((JViewport) getParent()).getViewRect();
				g.fillRect(r.x, r.y, r.width, r.height);

				try {
					super.paintComponent(g);
				} catch (Exception ignored) {
				}

				long currentTime = System.currentTimeMillis();
				if (currentTime > lastTime + 700) {
					return;
				}

				Graphics2D g2 = (Graphics2D) g;
				String str = lastTimeString.toUpperCase();
				FontMetrics metrics = g2.getFontMetrics();
				int strh = metrics.getHeight();

				Point p = ((JViewport) lyricsArea.getParent())
						.getViewPosition();
				Dimension d = lyricsArea.getSize();
				int x = d.width - 60;
				int y = p.y + strh + 6;
				g2.setFont(big);
				g.setColor(blue);
				g.drawString(str, x, y);
			}
		};
		lyricsArea.setLogicalStyle(notLongStyle);

		addKeymapBindings();

		init(prop);

		// compound editing instead of character-based:

		// http://forum.java.sun.com/thread.jspa?forumID=57&threadID=637225

		lyricsArea.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				if (preventFireUpdate) {
					return;
				}
				YassLyrics.this.firePropertyChange("play", null, "stop");

				hyphenationChanged = true;
				checkLength();
				applyLyrics();
			}

			public void removeUpdate(DocumentEvent e) {
				insertUpdate(e);
			}

			public void changedUpdate(DocumentEvent e) {
				// insertUpdate(e);
			}
		});

		// prevent flicker when updating text via setText
		final class QuietCaret extends DefaultCaret {
			private static final long serialVersionUID = 5435239421917917780L;

			/**
			 * Description of the Method
			 *
			 * @param r
			 *            Description of the Parameter
			 */
			protected void adjustVisibility(Rectangle r) {
			}
		}
		lyricsArea.setCaret(new QuietCaret());

		lyricsArea.addCaretListener(caretListener);
		lyricsArea.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				// System.out.println("lyrics focus gained");
			}

			public void focusLost(FocusEvent e) {
				// System.out.println("lyrics focus lost");
				if (spellCheckerComp != null) {
					if (spellCheckerComp.getHandler().getPopup() != null
							&& spellCheckerComp.getHandler().getPopup()
									.isVisible()) {
						return;
					}
				}

				if (frDialog == null || !frDialog.isVisible()) {
					finishEditing();
				}
			}
		});
		/*
		 * addMouseMotionListener( new MouseMotionListener() { public void
		 * mouseMoved(MouseEvent e) { Component p = getParent();
		 * p.dispatchEvent(e); } public void mouseDragged(MouseEvent e) { } });
		 * lyricsArea.addMouseMotionListener( new MouseMotionListener() { public
		 * void mouseMoved(MouseEvent e) { Component p = getParent();
		 * p.dispatchEvent(e); } public void mouseDragged(MouseEvent e) { } });
		 * lineNumbers.addMouseMotionListener( new MouseMotionListener() {
		 * public void mouseMoved(MouseEvent e) { Component p = getParent();
		 * p.dispatchEvent(e); } public void mouseDragged(MouseEvent e) { } });
		 */
		lyricsArea.addMouseListener(new MouseAdapter() {

			public void mouseExited(MouseEvent e) {
				// System.out.println("lyrics exited");
			}

			public void mouseClicked(MouseEvent e) {
				YassLyrics.this.firePropertyChange("play", null, "stop");
				if (e.getClickCount() > 1) {
					table.selectLine();
				}
			}

			public void mousePressed(MouseEvent e) {
				YassLyrics.this.firePropertyChange("play", null, "stop");
				if (SwingUtilities.isLeftMouseButton(e)) {
					Runnable later = new Runnable() {
						public synchronized void run() {
							try {
								lyricsArea.getStyledDocument()
										.setCharacterAttributes(
												0,
												lyricsArea.getStyledDocument()
														.getLength(),
												notSelectStyle, false);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					};
					SwingUtilities.invokeLater(later);
				} else {
					int pos = lyricsArea.viewToModel(e.getPoint());
					lyricsArea.getCaret().setDot(pos);
					lyricsArea.getCaret().moveDot(pos);
					editLyrics();
				}
			}

			public void mouseReleased(MouseEvent e) {
			}
		});
		lyricsArea.getInputMap().put(KeyStroke.getKeyStroke(' '), "nop");
		lyricsArea.getActionMap().put("nop", new AbstractAction("nop") {
			private static final long serialVersionUID = -6571486093583233962L;

			public void actionPerformed(ActionEvent e) {
			}
		});
		lyricsArea.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "nop");
		lyricsArea.getInputMap().put(KeyStroke.getKeyStroke('-'), "nop");
		lyricsArea.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "nop");
		lyricsArea.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "nop");
		lyricsArea.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "nop");

		lyricsArea.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (!lyricsArea.isEditable() && sheet != null) {
					char c = e.getKeyChar();

					if (e.isControlDown() && e.isAltDown()
							&& c == KeyEvent.CHAR_UNDEFINED) {
						sheet.dispatchEvent(e);
						return;
					} else if (e.isControlDown()
							&& c == KeyEvent.CHAR_UNDEFINED) {
						sheet.dispatchEvent(e);
						return;
					} else if (e.isAltDown() && c == KeyEvent.CHAR_UNDEFINED) {
						sheet.dispatchEvent(e);
						return;
					} else if (e.isShiftDown() && c == KeyEvent.CHAR_UNDEFINED) {
						sheet.dispatchEvent(e);
						return;
					}
				}

				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_Z && e.isControlDown()) {
					if (isEditable()) {
						finishEditing();
						table.undoRows();
						editLyrics();
						e.consume();
					}
					return;
				} else if (keyCode == KeyEvent.VK_Y && e.isControlDown()) {
					if (isEditable()) {
						finishEditing();
						table.redoRows();
						editLyrics();
						e.consume();
					}
					return;
				} else if (keyCode == KeyEvent.VK_F4) {
					editLyrics();
					e.consume();
					return;
				} else if (keyCode == KeyEvent.VK_DELETE && e.isControlDown()) {
					finishEditing();
					table.removeRowsWithLyrics();
					e.consume();
					return;
				} else if (keyCode == KeyEvent.VK_ESCAPE) {
					finishEditing();
					updateSelection();
					e.consume();
					return;
				} else if (keyCode == KeyEvent.VK_ENTER) {
					boolean state = lyricsArea.isEditable();
					int dot = lyricsArea.getCaret().getDot();
					finishEditing();
					table.dispatchEvent(e);
					if (state) {
						editLyrics();
						lyricsArea.getCaret().setDot(dot);
					}
					e.consume();
					return;
				} else if (keyCode == KeyEvent.VK_SPACE
						|| keyCode == KeyEvent.VK_MINUS) {
					char key = keyCode == KeyEvent.VK_SPACE ? ' ' : '-';

					if (!lyricsArea.isEditable()) {
						table.dispatchEvent(e);
						e.consume();
						return;
					}
					String txt = lyricsArea.getText();
					int txtlen = txt.length();
					int dot = lyricsArea.getCaret().getDot();
					int mark = lyricsArea.getCaret().getMark();
					if (dot != mark || dot >= txtlen) {
						e.consume();
						return;
					}
					int dot0 = dot - 1;
					int dot1 = dot + 1;
					char c = txt.charAt(dot);
					char c0 = dot0 >= 0 ? txt.charAt(dot0) : '0';

					if (c == '\n' || c0 == '\n' || (c == ' ' && key == ' ')
							|| (c == '-' && key == '-') || c0 == ' '
							|| c0 == '-') {
						lyricsArea.getCaret().setDot(dot1);
						lyricsArea.getCaret().moveDot(dot1);
						e.consume();
						return;
					}
					if ((key == ' ' && c == '-') || (key == '-' && c == ' ')) {
						lyricsArea.getCaret().setDot(dot);
						lyricsArea.getCaret().moveDot(dot1);
						lyricsArea.replaceSelection(key + "");
						lyricsArea.getCaret().setDot(dot1);

						lyricsArea.getCaret().setDot(dot1);
						lyricsArea.getCaret().moveDot(dot1);
						e.consume();
						return;
					}

					lyricsArea.getCaret().setDot(dot1);
					lyricsArea.getCaret().setDot(dot);

					int pos = dot;
					while (pos > 0 && c != ' ' && c != '-' && c != '\n') {
						c = txt.charAt(--pos);
					}
					if (c == '-' || c == '\n') {
						pos++;
					}
					pos = dot - pos;

					finishEditing();
					preventFireUpdate = true;
					table.getModel().removeTableModelListener(tableListener);
					table.getSelectionModel().removeListSelectionListener(
							tableSelectionListener);
					lyricsArea.removeCaretListener(caretListener);
					table.rollRight(key, pos);
					lyricsArea.setText(table.getText());
					lyricsArea.addCaretListener(caretListener);
					table.getSelectionModel().addListSelectionListener(
							tableSelectionListener);
					table.getModel().addTableModelListener(tableListener);
					tableListener.tableChanged(null);
					preventFireUpdate = false;

					editLyrics();
					lyricsArea.getCaret().setDot(dot1);
					lyricsArea.getCaret().moveDot(dot1);
					e.consume();

					return;
				} else if (keyCode == KeyEvent.VK_DELETE
						|| keyCode == KeyEvent.VK_BACK_SPACE) {
					// check on valid action
					if (!lyricsArea.isEditable()) {
						if (keyCode == KeyEvent.VK_BACK_SPACE) {
							table.getActions().removePageBreak();
						}
						return;
					}
					String txt = lyricsArea.getText();
					int txtlen = txt.length();
					int dot = lyricsArea.getCaret().getDot();
					int mark = lyricsArea.getCaret().getMark();
					if (dot != mark) {
						e.consume();
						return;
					}

					boolean isDelete = keyCode == KeyEvent.VK_DELETE;
					boolean isBackspace = !isDelete;

					if (isDelete && dot == txtlen) {
						return;
					}
					if (isBackspace && dot == 0) {
						return;
					}

					if (isBackspace) {
						dot--;
						lyricsArea.getCaret().setDot(dot);
					}

					// set c=current cc=next character
					int dot1 = dot + 1;

					char c = txt.charAt(dot);
					char c1 = dot1 < txtlen - 1 ? txt.charAt(dot1) : '0';

					if (c == '\n') {
						lyricsArea.getCaret().setDot(dot1);
						finishEditing();
						table.togglePageBreak();
						editLyrics();
						lyricsArea.getCaret().setDot(dot1);
						e.consume();
						return;
					}

					if (c == ' ' || c == '-' || (c == YassRow.HYPHEN && c1 == '\n')) {
						// workaround
						lyricsArea.getCaret().setDot(dot1);
						lyricsArea.getCaret().setDot(dot);
						finishEditing();
						preventFireUpdate = false;
						table.getModel()
								.removeTableModelListener(tableListener);
						table.getSelectionModel().removeListSelectionListener(
								tableSelectionListener);
						lyricsArea.removeCaretListener(caretListener);
						if (prop.isUncommonSpacingAfter()) {
							table.removeEndSpace();
						} else {
							table.rollLeft();
						}
						lyricsArea.setText(table.getText());
						lyricsArea.addCaretListener(caretListener);
						table.getSelectionModel().addListSelectionListener(
								tableSelectionListener);
						table.getModel().addTableModelListener(tableListener);
						tableListener.tableChanged(null);
						preventFireUpdate = false;

						editLyrics();
						lyricsArea.getCaret().setDot(dot);
						lyricsArea.getCaret().moveDot(dot1);

						// lyricsArea.replaceSelection("");
						lyricsArea.getCaret().setDot(dot1);
						lyricsArea.getCaret().setDot(dot);

						e.consume();
						return;
					}

					lyricsArea.getCaret().setDot(dot);
					lyricsArea.getCaret().moveDot(dot1);
					lyricsArea.replaceSelection("");
					lyricsArea.getCaret().setDot(dot);
					e.consume();
					return;
				}

				// non-control key
				if (!isEditable()) {
					char c = e.getKeyChar();

					if (Character.isDigit(c) && !e.isControlDown()) {
						String cstr = c + "";
						long currentTime = System.currentTimeMillis();
						if (currentTime < lastTime + 700) {
							if (lastTimeString.length() < 3) {
								cstr = lastTimeString + cstr;
							}
							lastTimeString = cstr;
							try {
								int n = Integer.parseInt(cstr);
								table.gotoPageNumber(n);
							} catch (Exception ignored) {
							}
						} else {
							lastTimeString = cstr;
							try {
								int n = Integer.parseInt(cstr);
								table.gotoPageNumber(n);
							} catch (Exception ignored) {
							}
						}
						lastTime = currentTime;
						lyricsArea.repaint();
						e.consume();
					} else if (e.isControlDown() && keyCode == KeyEvent.VK_H) {
						table.rehyphenate();
					} else {
						table.dispatchEvent(e);
					}
				} else {
					int dot = lyricsArea.getCaret().getDot();
					int mark = lyricsArea.getCaret().getMark();
					if (dot != mark) {
						int min = Math.min(dot, mark);
						int max = Math.max(dot, mark);

						char c = 'x';
						String txt = lyricsArea.getText();
						int k = min;
						while (k < max && c != ' ' && c != '-' && c != '\n') {
							c = txt.charAt(k++);
						}
						if (c == '-' || c == '\n' || c == ' ') {
							e.consume();
						}
					}
					// do not consume()
				}
			}

			public void keyTyped(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_SPACE
						|| keyCode == KeyEvent.VK_DELETE
						|| keyCode == KeyEvent.VK_BACK_SPACE
						|| keyCode == KeyEvent.VK_MINUS) {
					e.consume();
                }
			}

			public void keyReleased(KeyEvent e) {
				if (!lyricsArea.isEditable() && sheet != null) {
					if (!e.isControlDown() && !e.isAltDown()
							&& !e.isShiftDown()) {
						sheet.dispatchEvent(e);
						return;
					} else if (!e.isControlDown() && e.isAltDown()) {
						sheet.dispatchEvent(e);
						return;
					} else if (e.isControlDown() && !e.isAltDown()) {
						sheet.dispatchEvent(e);
						return;
					} else if (e.isShiftDown()) {
						sheet.dispatchEvent(e);
						return;
					}
				}

				e.consume();
			}
		});

		ActionMap am = lyricsArea.getActionMap();
		am.put(DefaultEditorKit.selectWordAction, new TextAction(
				DefaultEditorKit.selectWordAction) {
			private static final long serialVersionUID = 2463563308976339545L;

			public void actionPerformed(ActionEvent e) {
			}
		});
		am.put(DefaultEditorKit.selectLineAction, new TextAction(
				DefaultEditorKit.selectLineAction) {
			private static final long serialVersionUID = -4742750389608437521L;

			public void actionPerformed(ActionEvent e) {
			}
		});
		am.put(DefaultEditorKit.insertBreakAction, new TextAction(
				DefaultEditorKit.selectLineAction) {
			private static final long serialVersionUID = -3927177417337723716L;

			public void actionPerformed(ActionEvent e) {
			}
		});

		// lyricsArea.requestFocus();
		// lyricsArea.setBackground(Color.lightGray);
		finishEditing();

		setLanguage("EN_US");

		lyricsScrollPane = new JScrollPane(lyricsArea);
		// lyricsArea.setBackground(YassMain.combinedLyrics ? fontBG :
		// lyricsArea.getBackground());
		lyricsArea.setOpaque(false);
		lyricsScrollPane.setOpaque(false);
		lyricsScrollPane.getViewport().setOpaque(false);
		lyricsScrollPane.getViewport().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				lineNumbers.repaint();
			}
		});
		lyricsScrollPane.setBorder(null);

		setLayout(new BorderLayout());
		add("West", lineNumbers);
		add("Center", lyricsScrollPane);
		// sp.setPreferredSize(new Dimension(600,400));

        setOpaque(false);
	}

	/**
	 * Description of the Method
	 *
	 * @param g
	 *            Description of the Parameter
	 */
	public void paintComponent2(Graphics g) {
		paintComponent(g);
		paintChildren(g);
	}

	/**
	 * Gets the editable attribute of the YassLyrics object
	 *
	 * @return The editable value
	 */
	public boolean isEditable() {
		return lyricsArea.isEditable();
	}

	/**
	 * Description of the Method
	 *
	 * @param s
	 *            Description of the Parameter
	 */
	public void enter(String s) {
		if (!isEditable()) {
			editLyrics();
		}
		int dot = lyricsArea.getCaret().getDot();
		int mark = lyricsArea.getCaret().getMark();
		if (dot != mark) {
			lyricsArea.getCaret().setDot(Math.max(dot, mark));
		}

		lyricsArea.replaceSelection(s);
	}

	/**
	 * Sets the language attribute of the YassLyrics object
	 *
	 * @param lang
	 *            The new language value
	 */
	public void setLanguage(String lang) {
		Thread loader = new LanguageLoader(lang);
		loader.setPriority(Thread.NORM_PRIORITY - 1);
		SwingUtilities.invokeLater(loader);
	}

	/**
	 * Description of the Class
	 *
	 * @author Saruta
	 */
	class LanguageLoader extends Thread {
		private String lang = null;

		/**
		 * Constructor for the LanguageLoader object
		 *
		 * @param language
		 *            Description of the Parameter
		 */
		public LanguageLoader(String language) {
			lang = language;
		}

		/**
		 * Main processing method for the LanguageLoader object
		 */
		public void run() {
			StringTokenizer st = new StringTokenizer(lang, "()-_");
			String language = st.nextToken();
			String country = st.hasMoreTokens() ? st.nextToken() : "";
			String lc = country.length() > 0 ? language + "_" + country : language;

			// System.out.println("# remove spell check");
			if (spellCheckerComp != null) {
				spellCheckerComp.stopAutoSpellCheck(lyricsArea);
			}
			spellCheckerComp = null;
			Object sc = spellCheckers.get(lc);
			if (sc == null) {
				sc = spellCheckers.get(language);
			}
			if (sc != null) {
				if (sc instanceof String) {
					try {
						InputStream is;
						File f = new File(userdir + File.separator + sc
								+ ".dic");
						if (f.exists()) {
							is = Files.newInputStream(f.toPath());
						} else {
							is = getClass().getResourceAsStream(
									"/yass/resources/spell/" + sc + ".dic");
						}
						// System.out.println("/spell/"+sc+".dic");
						SpellDictionaryHashMap dict = new SpellDictionaryHashMap(new InputStreamReader(is));
						String user = prop.getProperty("user-dicts")
								+ File.separator + "user_" + sc + ".dic";
						File userfile = new File(user);
						// userfile.createNewFile() throws ioexception
						new File(prop.getProperty("user-dicts")).mkdirs();
						try {
							BufferedWriter out = new BufferedWriter(
									new FileWriter(user));
							out.write("yass\n");
							out.close();
						} catch (IOException e) {
						}
						SpellDictionaryHashMap dict_user = new SpellDictionaryHashMap(userfile);
						// System.out.println("# add spell check");
						spellCheckerComp = new JTextComponentSpellChecker(dict,
								dict_user, I18.get("tool_spellcheck"));
						spellCheckers.put(sc, spellCheckerComp);
					} catch (Exception e) {
						// e.printStackTrace();
					}
				} else {
					spellCheckerComp = (JTextComponentSpellChecker) sc;
				}
			}

			// System.out.println("# start spell check");
			try {
				if (spellCheckerComp != null) {
					isSpellChecking = true;

					preventFireUpdate = true;
					table.getModel().removeTableModelListener(tableListener);
					table.getSelectionModel().removeListSelectionListener(
							tableSelectionListener);
					lyricsArea.removeCaretListener(caretListener);

					// bug: triggers table update
					spellCheckerComp.startAutoSpellCheck(lyricsArea);
					spellCheckerComp.getHandler().markupSpelling(lyricsArea);

					lyricsArea.addCaretListener(caretListener);
					table.getSelectionModel().addListSelectionListener(
							tableSelectionListener);
					table.getModel().addTableModelListener(tableListener);
					preventFireUpdate = false;

					isSpellChecking = false;
					tableListener.tableChanged(null);
				}
			} catch (Exception e) {
				// System.out.println("startup bug spellchecker during setLanguage");
			}
		}
	}

	/**
	 * Description of the Method
	 */
	public void disconnectTable() {
		table.getModel().removeTableModelListener(tableListener);
		table.getSelectionModel().removeListSelectionListener(
				tableSelectionListener);
		lyricsArea.removeCaretListener(caretListener);
	}

	/**
	 * Description of the Method
	 */
	public void connectTable() {
		lyricsArea.addCaretListener(caretListener);
		table.getSelectionModel().addListSelectionListener(
				tableSelectionListener);
		table.getModel().addTableModelListener(tableListener);
	}

	private boolean isSpellChecking = false;

	/**
	 * Description of the Method
	 */
	public void spellLyrics() {
		isSpellChecking = true;
		spellCheckerComp.spellCheck(lyricsArea);
		spellCheckerComp.getHandler().markupSpelling(lyricsArea);
		isSpellChecking = false;
	}

	int initialBlinkRate = 500;

	/**
	 * Description of the Method
	 */
	public void editLyrics() {
		if (lyricsArea.isEditable()) {
			return;
		}

		lyricsArea.requestFocus();
		lyricsArea.setEditable(true);
		colorsChanged();
		lyricsArea.getCaret().setVisible(true);
		lyricsArea.setCaretColor(Color.red);
		lyricsArea.getCaret().setBlinkRate(initialBlinkRate);
		lyricsArea.getStyledDocument().setCharacterAttributes(0,
				lyricsArea.getStyledDocument().getLength(), notSelectStyle,
				false);
		tableSelectionListener.valueChanged(null);
	}

	/**
	 * Description of the Method
	 */
	public void finishEditing() {
		lyricsArea.setEditable(false);
		colorsChanged();
		lyricsArea.setCaretColor(null);
		lyricsArea.getCaret().setVisible(false);
		lyricsArea.getCaret().setBlinkRate(0);
		lyricsArea.getStyledDocument().setCharacterAttributes(0,
				lyricsArea.getStyledDocument().getLength(), notSelectStyle,
				false);

		// if (spellCheckerComp != null) {
		// spellCheckerComp.getHandler().markupSpelling(lyricsArea);
		// }
	}

	/**
	 * Sets the autoCorrect attribute of the YassLyrics object
	 *
	 * @param a
	 *            The new autoCorrect value
	 */
	public void setAutoCorrect(YassAutoCorrect a) {
		auto = a;
	}

	/**
	 * Sets the sheet attribute of the YassLyrics object
	 *
	 * @param s
	 *            The new sheet value
	 */
	public void setSheet(YassSheet s) {
		if (sheet != null)
			sheet.removeYassSheetListener(this);
		sheet = s;
		sheet.addYassSheetListener(this);
		propsChanged(sheet);
	}

	/**
	 * Sets the table attribute of the YassLyrics object
	 *
	 * @param t
	 *            The new table value
	 */
	public void setTable(YassTable t) {
		if (table != null) {
			table.getSelectionModel().removeListSelectionListener(
					tableSelectionListener);
			table.getModel().removeTableModelListener(tableListener);
		}
		table = t;
		if (table != null) {
			table.getSelectionModel().addListSelectionListener(
					tableSelectionListener);
			table.getModel().addTableModelListener(tableListener);
		}
		if (table == null) {
			return;
		}

		preventFireUpdate = true;

		lyricsArea.setText(table.getText());

		try {
			if (spellCheckerComp != null && lyricsArea.isVisible()) {
				spellCheckerComp.getHandler().markupSpelling(lyricsArea);
			}
		} catch (Exception e) {
			// System.out.println("startup bug spellchecker during setTable");
		}
		// startup problems

		checkLength();
		preventFireUpdate = false;

		tableListener.tableChanged(null);
	}

	private TableModelListener tableListener = new TableModelListener() {
		public void tableChanged(TableModelEvent e) {
			// if (e.getType() == e.UPDATE) return;
			if (!table.lyricsChanged()) {
				return;
			}
			if (lyricsArea.isEditable()) {
				return;
			}
			if (isSpellChecking) {
				return;
			}

			preventFireUpdate = true;
			table.getModel().removeTableModelListener(tableListener);
			table.getSelectionModel().removeListSelectionListener(
					tableSelectionListener);
			lyricsArea.removeCaretListener(caretListener);

			Document d = lyricsArea.getDocument();
			try {
				d.remove(0, d.getLength());
				d.insertString(0, table.getText(), notLongStyle);
			} catch (BadLocationException ignored) {
			}

			updateSelection();
			try {
				if (spellCheckerComp != null && lyricsArea.isVisible()) {
					spellCheckerComp.getHandler().markupSpelling(lyricsArea);
				}
			} catch (Exception ex) {
				// System.out.println("startup bug spellchecker during tableUpdate");
			}
			lyricsArea.addCaretListener(caretListener);
			table.getSelectionModel().addListSelectionListener(
					tableSelectionListener);
			table.getModel().addTableModelListener(tableListener);
			preventFireUpdate = false;

			try {
				String txt = lyricsArea.getText();
				lyricsArea.getStyledDocument().setCharacterAttributes(0,
						txt.length() - 1, notGoldenOrFreeStyle, false);
				int ij[] = nextSyllable(0);
				int k = 0;
				int n = table.getRowCount();
				while (ij != null) {
					YassRow r = table.getRowAt(k);
					while (!r.isNote() && k < n) {
						r = table.getRowAt(++k);
					}
					if (k >= n) {
						return;
					}

					if (r.isGolden()) {
						lyricsArea.getStyledDocument().setCharacterAttributes(
								ij[0], ij[1] - ij[0], goldenStyle, false);
					} else if (r.isFreeStyle()) {
						lyricsArea.getStyledDocument().setCharacterAttributes(
								ij[0], ij[1] - ij[0], freeStyle, false);
					} else if (r.isRap()) {
						lyricsArea.getStyledDocument().setCharacterAttributes(
								ij[0], ij[1] - ij[0], rapStyle, false);
					} else if (r.isRapGolden()) {
						lyricsArea.getStyledDocument().setCharacterAttributes(
								ij[0], ij[1] - ij[0], rapgoldenStyle, false);
					}
					ij = nextSyllable(ij[1] + 1);
					k++;
				}
				errLines.clear();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};
	private ListSelectionListener tableSelectionListener = new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			updateSelection();
		}
	};

	/**
	 * Description of the Method
	 */
	public void updateSelection() {
		int i = table.getSelectionModel().getMinSelectionIndex();
		int j = table.getSelectionModel().getMaxSelectionIndex();
		if (i < 0) {
			Runnable later = new Runnable() {
				public synchronized void run() {
					try {
						lyricsArea.getStyledDocument().setCharacterAttributes(
								0, lyricsArea.getStyledDocument().getLength(),
								notSelectStyle, false);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			};
			SwingUtilities.invokeLater(later);
			return;
		}

		int n = table.getRowCount();

		int min = -1;

		int max = -1;

		int syll = 0;
		for (int k = 0; k < n; ++k) {
			YassRow r = table.getRowAt(k);
			if (r.isNote()) {
				syll++;
			}
			if (k == i) {
				min = syll;
			}
			if (k == j) {
				max = syll;
			}
		}
		if (min < 0) {
			return;
		}
		if (max < min) {
			max = min;
		}

		selectSyllablesAt(min, max);
	}

	Vector<Integer> errLines = new Vector<>();

	/**
	 * Description of the Method
	 */
	public synchronized void checkLength() {
		errLines.clear();
		Document doc = lyricsArea.getDocument();
		Element root = doc.getDefaultRootElement();
		for (int i = 0; i < root.getElementCount(); i++) {
			Element e = root.getElement(i);
			int si = e.getStartOffset();
			int ei = e.getEndOffset();
			try {
				String line = lyricsArea.getText(si, ei - si);
				line = line.replaceAll("-", "");
				line = line.replace(YassRow.HYPHEN, '-');
				if (auto.getPageSpace(line) < 0) {
					errLines.addElement(si);
					errLines.addElement(ei);
				}
			} catch (BadLocationException ignored) {
			}
		}

		Runnable later = new Runnable() {
			public synchronized void run() {
				try {
					lyricsArea.getStyledDocument().setCharacterAttributes(0,
							lyricsArea.getStyledDocument().getLength(),
							notLongStyle, false);
					for (Enumeration<Integer> en = errLines.elements(); en
							.hasMoreElements();) {
						int si = en.nextElement().intValue();
						int ei = en.nextElement().intValue();
						lyricsArea.getStyledDocument().setCharacterAttributes(
								si, ei - si, longStyle, false);
					}
					errLines.clear();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		SwingUtilities.invokeLater(later);
	}

	long lastCompoundChange = 0;
	YassUndoElement myUndo = null;
	boolean canUndo = false;

	/**
	 * Description of the Method
	 */
	public void applyLyrics() {
		YassTableModel tm = (YassTableModel) table.getModel();
		int i = 0;
		int notes = 0;
		int n = table.getRowCount() - 1;
		while (i <= n) {
			YassRow r = table.getRowAt(i);
			if (r.isNote() || r.isPageBreak()) {
				notes++;
			}
			i++;
		}

		Vector<String> hyph = getSyllables();
		int syllables = hyph.size();

		mismatch = syllables - notes;

		// spread syllables
		String txt;
		int k = 0;
		boolean changed = false;
		for (i = 0; i < n; i++) {
			YassRow r = table.getRowAt(i);
			if (r.isNote()) {
				if (k < syllables) {
					txt = hyph.elementAt(k++);
				} else {
					txt = "";
				}
				while (k < syllables && txt.equals("\n")) {
					txt = hyph.elementAt(k++);
				}
				if (txt.equals("\n")) {
					txt = "";
				}
				txt = txt.replace(YassRow.HYPHEN, '-');

				if (!r.getText().equals(txt)) {
					r.setText(txt);
					changed = true;
				}
			}
		}
		if (changed) {
			canUndo = myUndo != null && (table.currentUndo() == myUndo);
			myUndo = table.addUndo();
			long currentTime = System.currentTimeMillis();
			if (canUndo && (currentTime < lastCompoundChange + 1500)) {
				table.removeLastUndo();
			}
			lastCompoundChange = currentTime;

			tm.fireTableDataChanged();
			table.repaint();
		}
	}

	/**
	 * Description of the Method
	 */
	public void find() {
		editLyrics();
		if (frDialog == null) {
			frDialog = new FindReplace();
		}
		frDialog.locateAndShow();
	}

	Vector<String> h = new Vector<>();
	private boolean hyphenationChanged = true;

	/**
	 * Gets the syllables attribute of the YassLyrics object
	 *
	 * @return The syllables value
	 */
	public Vector<String> getSyllables() {
		if (!hyphenationChanged) {
			return h;
		}
		h.clear();
		String txt = lyricsArea.getText();
		StringTokenizer st = new StringTokenizer(txt, "\n");
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(line, " ");
			boolean first = true;
			while (st2.hasMoreTokens()) {
				String word = st2.nextToken();
				if (prop.isUncommonSpacingAfter()) {
					word = word + YassRow.SPACE;
				} else {
					if (first) {
						first = false;
					} else {
						word = YassRow.SPACE + word;
					}
				}
				StringTokenizer st3 = new StringTokenizer(word, "-", true);
				boolean last;
				boolean delim = false;
				while (st3.hasMoreTokens()) {
					String syll = st3.nextToken();
					last = delim;
					delim = syll.equals("-");
					if (delim && last) {
						h.addElement("");
					} else if (!delim) {
						h.addElement(syll);
					}
				}
			}
			if (st.hasMoreTokens()) {
				h.addElement("\n");
			}
		}
		return h;
	}

	/**
	 * Description of the Method
	 *
	 * @param i
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public int[] nextSyllable(int i) {
		String txt = lyricsArea.getText();
		int n = txt.length();
		if (n < 1) {
			return null;
		}

		int ij[] = new int[2];
		while (i < n) {
			char c = txt.charAt(i);
			if (c != '-' && c != ' ' && c != '\n') {
				ij[0] = i;
				if (i < n - 1) {
					while (i < n - 1 && c != '-' && c != ' ' && c != '\n') {
						c = txt.charAt(++i);
					}
				}
				if (i == n - 1) {
					i = n;
				}
				ij[1] = i;
				return ij;
			}
			i++;
		}
		return null;
	}

	/**
	 * Description of the Method
	 *
	 * @param min
	 *            Description of the Parameter
	 * @param max
	 *            Description of the Parameter
	 */
	public void selectSyllablesAt(int min, int max) {
		String txt = lyricsArea.getText();
		int n = txt.length();
		if (n < 1) {
			return;
		}

		int in;

		int i = 0;

		int syllable = 0;

		int caretin = 0;
		while (i < n) {
			char c = txt.charAt(i);
			if (c != '-' && c != ' ' && c != '\n') {
				syllable++;
				in = i;
				if (i < n - 1) {
					while (i < n - 1 && c != '-' && c != ' ' && c != '\n') {
						c = txt.charAt(++i);
					}
				}
				if (i == n - 1) {
					i = n;
				}

				if (syllable == min) {
					caretin = in;
				}
				if (syllable == max) {
					SwingUtilities.invokeLater(new Selector(caretin, i));
					return;
				}
			}
			i++;
		}

	}

	/**
	 * Description of the Class
	 *
	 * @author Saruta
	 */
	class Selector implements Runnable {
		int in, out;

		/**
		 * Constructor for the Selector object
		 *
		 * @param i
		 *            Description of the Parameter
		 * @param j
		 *            Description of the Parameter
		 */
		public Selector(int i, int j) {
			in = i;
			out = j;
		}

		/**
		 * Main processing method for the Selector object
		 */
		public synchronized void run() {
			try {
				lyricsArea.getStyledDocument().setCharacterAttributes(0,
						lyricsArea.getStyledDocument().getLength(),
						notSelectStyle, false);
				lyricsArea.getStyledDocument().setCharacterAttributes(in,
						out - in, selectStyle, false);
				Rectangle r = lyricsArea.modelToView(in);
				r.add(lyricsArea.modelToView(out));
				lyricsArea.scrollRectToVisible(r);

				if (!lyricsArea.isEditable()) {
					preventFireUpdate = true;
					lyricsArea.getCaret().setDot(in);
					lyricsArea.getCaret().moveDot(out);
					preventFireUpdate = false;
				}
			} catch (Exception ignored) {
			}
		}
	}

	public void setPreventFireUpdate(boolean b) {
		preventFireUpdate = b;
	}
	/**
	 * Description of the Method
	 *
	 * @param dot
	 *            Description of the Parameter
	 * @param mark
	 *            Description of the Parameter
	 */
	public void selectSyllables(int dot, int mark) {
		String txt = lyricsArea.getText();
		int txtlen = txt.length();
		int min = Math.min(dot, mark);
		int max = Math.max(dot, mark);

		if (!lyricsArea.isEditable()) {
			if (min > 0) {
				char c = txt.charAt(min - 1);
				while (min > 0 && c != '-' && c != ' ' && c != '\n') {
					min--;
					if (min > 0) {
						c = txt.charAt(min - 1);
					}
				}
			}
			if (max < txtlen - 1) {
				char c = txt.charAt(max);
				if (c != '\n') {
					c = txt.charAt(max + 1);
					while (max < txtlen - 1 && c != '-' && c != ' '
							&& c != '\n') {
						max++;
						if (max < txtlen - 1) {
							c = txt.charAt(max + 1);
						}
					}
				}
			}
			lyricsArea.getCaret().setDot(Math.min(max + 1, txtlen));
			lyricsArea.getCaret().moveDot(min);
		}

		boolean hit = false;
		int i;
		int syll = 0;
		for (i = 0; i < min; i++) {
			if (txt.charAt(i) == '-' || txt.charAt(i) == ' '
					|| txt.charAt(i) == '\n') {
				if (!hit) {
					syll++;
				}
				hit = true;
			} else {
				hit = false;
			}
		}
		syll = syll + 1;

		hit = false;
		int syll2 = 0;
		for (; i < max; i++) {
			if (txt.charAt(i) == '-' || txt.charAt(i) == ' '
					|| txt.charAt(i) == '\n') {
				if (!hit) {
					syll2++;
				}
				hit = true;
			} else {
				hit = false;
			}
		}
		syll2 = syll + syll2 + 1;

		int notes = 0;

		int n = table.getRowCount() - 1;
		i = 0;
		while (i <= n && notes < syll) {
			YassRow r = table.getRowAt(i);
			if (r.isNote()) {
				notes++;
			}
			if (notes == syll) {
				break;
			}
			i++;
		}

		int j = i;
		while (j <= n && notes < syll2) {
			YassRow r = table.getRowAt(j);
			if (r.isNote()) {
				notes++;
			}
			if (notes == syll2) {
				break;
			}
			j++;
		}
		if (j > n) {
			j = i;
		}

		if (i <= n) {
			table.setRowSelectionInterval(i, j);
			Rectangle rr = table.getCellRect(i, 0, true);
			rr.add(table.getCellRect(j, 4, true));
			table.scrollRectToVisible(rr);

			table.adjustMultiSize();
		}
	}

	/**
	 * Description of the Method
	 */
	public void repaintLineNumbers() {
		lineNumbers.repaint();
	}

	/**
	 * Description of the Class
	 *
	 * @author Saruta
	 */
	public class LineNumbers extends JPanel {
		private static final long serialVersionUID = -4666558192446912742L;
		int inDrag = -1, outDrag = -1;
		boolean dragMultiBar = false;
		int multiBarWidth = 23;
		private BasicStroke stdStroke = new BasicStroke(1f);

		/**
		 * Constructor for the LineNumbers object
		 */
		public LineNumbers() {
			super();
			setOpaque(false);
			setMinimumSize(new Dimension(multiBarWidth, 0));
			setPreferredSize(new Dimension(multiBarWidth, 0));
			setMaximumSize(new Dimension(multiBarWidth, 0));

			addMouseWheelListener(new MouseWheelListener() {
				public void mouseWheelMoved(MouseWheelEvent e) {
					lyricsArea.dispatchEvent(e);
				}
			});
			addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}

				public void mouseClicked(MouseEvent ev) {
					YassLyrics.this.firePropertyChange("play", null, "stop");
					if (ev.getClickCount() > 1) {
						table.selectAll();
						return;
					}

					int ey = ev.getY();

					try {
						Point p = lyricsScrollPane.getViewport()
								.getViewPosition();
						int start = lyricsArea.viewToModel(p);
						int end = lyricsArea.viewToModel(new Point(
								lyricsScrollPane.getViewport()
										.getViewPosition().x
										+ lyricsArea.getWidth(),
								lyricsScrollPane.getViewport()
										.getViewPosition().y
										+ lyricsArea.getHeight()));

						Document doc = lyricsArea.getDocument();
						int startline = doc.getDefaultRootElement()
								.getElementIndex(start);
						int endline = doc.getDefaultRootElement()
								.getElementIndex(end);

						for (int n = startline; n <= endline; n++) {
							Element e = doc.getDefaultRootElement().getElement(
									n);
							Rectangle r = lyricsArea.modelToView(e
									.getStartOffset());
							int y = r.y + r.height - p.y;

							if (ey > y - r.height && ey < y) {
								lyricsArea.requestFocus();
								lyricsArea.getCaret()
										.setDot(e.getStartOffset());
								lyricsArea.getCaret().moveDot(
										e.getEndOffset() - 1);
								// goto START tag
								if (ev.getX() < 10) {
									if (n == 0)
										table.gotoPage(-1);
									else if (n == table.getPageCount() - 1)
										table.gotoPage(+1);
								}
								break;
							}
						}
					} catch (Exception ignored) {
					}

				}

				public void mouseReleased(MouseEvent ev) {
					if (dragMultiBar) {
						updateMultiBar();
					}
				}

				public void mousePressed(MouseEvent ev) {
					YassLyrics.this.firePropertyChange("play", null, "stop");

					int ey = ev.getY();

					try {
						Point p = lyricsScrollPane.getViewport()
								.getViewPosition();
						int start = lyricsArea.viewToModel(p);
						int end = lyricsArea.viewToModel(new Point(
								lyricsScrollPane.getViewport()
										.getViewPosition().x
										+ lyricsArea.getWidth(),
								lyricsScrollPane.getViewport()
										.getViewPosition().y
										+ lyricsArea.getHeight()));

						Document doc = lyricsArea.getDocument();
						int startline = doc.getDefaultRootElement()
								.getElementIndex(start);
						int endline = doc.getDefaultRootElement()
								.getElementIndex(end);

						for (int n = startline; n <= endline; n++) {
							Element e = doc.getDefaultRootElement().getElement(
									n);
							Rectangle r = lyricsArea.modelToView(e
									.getStartOffset());
							int y = r.y + r.height - p.y;

							if (ey > y - r.height && ey < y) {
								inDrag = outDrag = n;
								dragMultiBar = true;

								lyricsArea.requestFocus();
								int dot = lyricsArea.getCaret().getDot();
								if (dot < e.getStartOffset()
										|| dot > e.getEndOffset()) {
									lyricsArea.getCaret().setDot(
											e.getStartOffset());
								}
								break;
							}
						}
					} catch (Exception ignored) {
					}
				}
			});
			addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent ev) {
					int ey = ev.getY();

					try {
						Point p = lyricsScrollPane.getViewport()
								.getViewPosition();
						int start = lyricsArea.viewToModel(p);
						int end = lyricsArea.viewToModel(new Point(
								lyricsScrollPane.getViewport()
										.getViewPosition().x
										+ lyricsArea.getWidth(),
								lyricsScrollPane.getViewport()
										.getViewPosition().y
										+ lyricsArea.getHeight()));

						Document doc = lyricsArea.getDocument();
						int startline = doc.getDefaultRootElement()
								.getElementIndex(start);
						int endline = doc.getDefaultRootElement()
								.getElementIndex(end);

						for (int n = startline; n <= endline; n++) {
							Element e = doc.getDefaultRootElement().getElement(
									n);
							Rectangle r = lyricsArea.modelToView(e
									.getStartOffset());
							int y = r.y + r.height - p.y;

							if (ey > y - r.height && ey < y) {
								if (n != outDrag) {
									outDrag = n;
									if (dragMultiBar) {
										updateMultiBar();
									}
									if (outDrag < inDrag) {
										lyricsArea.requestFocus();
										lyricsArea.getCaret().setDot(
												e.getStartOffset());
									}
								}
								break;
							}
						}
					} catch (Exception ignored) {
					}
				}
			});

		}

		/**
		 * Description of the Method
		 */
		public void updateMultiBar() {
			int m = Math.abs(inDrag - outDrag) + 1;
			table.getActions().setRelative(m == 1);
			YassTable.setZoomMode(m == 1 ? YassTable.ZOOM_ONE
					: YassTable.ZOOM_MULTI);
			if (m > 1) {
				m++;
			}
			table.setMultiSize(m);
			table.zoomPage();
			repaint();
		}

		/**
		 * Description of the Method
		 *
		 * @param g
		 *            Description of the Parameter
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;

			try {
				Point p = lyricsScrollPane.getViewport().getViewPosition();
				int start = lyricsArea.viewToModel(p);
				int end = lyricsArea.viewToModel(new Point(lyricsScrollPane
						.getViewport().getViewPosition().x
						+ lyricsArea.getWidth(), lyricsScrollPane.getViewport()
						.getViewPosition().y + lyricsArea.getHeight()));

				Document doc = lyricsArea.getDocument();
				int startline = doc.getDefaultRootElement().getElementIndex(
						start);
				int endline = doc.getDefaultRootElement().getElementIndex(end);

				g.setColor(sheet.darkMode ? YassSheet.blackDarkMode : YassSheet.dkGray);

				g.setFont(lineNumberFont);
				FontMetrics metrics = g.getFontMetrics();
				int rows = doc.getDefaultRootElement().getElementCount();
				for (int n = startline; n <= endline; n++) {
					Element e = doc.getDefaultRootElement().getElement(n);
					Rectangle r = lyricsArea.modelToView(e.getStartOffset());
					int y = r.y + r.height - p.y;

					String msg = table.getPageMessage(n+1);
					if (msg != null) {
						if (YassAutoCorrect.isAutoCorrectionMinorPageBreak(msg))
							g.setColor(minorerrBackground);
						else g.setColor(errBackground);

						g2.fillRect(0, r.y - p.y, getWidth()-1, r.height);
						g.setColor(sheet.darkMode ? YassSheet.blackDarkMode : YassSheet.dkGray);
					}

					String s = (n + 1) + "";
					int w = metrics.stringWidth(s);
					g.drawString(s, multiBarWidth - w - 3, y - 6);

					if (n == 0) {
						g.drawString("【", 0, y - 6);
					}if (n == rows-1) {
						g.drawString("】", 0, y - 6);
					}
				}

				int curPage = table.getFirstVisiblePageNumber() - 1;
				int multiSize = table.getMultiSize();
				Element e = doc.getDefaultRootElement().getElement(
						Math.max(curPage, 0));
				Rectangle r = lyricsArea.modelToView(e.getStartOffset());
				int h = r.height;
				if (multiSize > 2) {
					Element e2 = doc.getDefaultRootElement().getElement(
							Math.min(curPage + multiSize - 2, rows - 1));
					Rectangle r2 = lyricsArea.modelToView(e2.getStartOffset());
					h = r2.y - r.y + r2.height;
				}

				if (multiSize > 1) {
					g2.setStroke(stdStroke);
					g2.drawRect(0, r.y - p.y, multiBarWidth, h);
				}
			} catch (Exception ignored) {
			}
		}
	}

	// Add some emacs key bindings to the key map for navigation
	/**
	 * Adds a feature to the KeymapBindings attribute of the YassLyrics object
	 */
	protected void addKeymapBindings() {
		lyricsArea.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK),
				"find");
		lyricsArea.getActionMap().put("find", find);
		find.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		erase.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK));

		/*
		 * lyricsArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
		 * 0), "noop");
		 * lyricsArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent
		 * .VK_MINUS, 0), "noop"); lyricsArea.getActionMap().put("noop", noop);
		 */
	}

	Action find = new AbstractAction("Find") {
		private static final long serialVersionUID = 6123378559204943308L;

		public void actionPerformed(ActionEvent e) {
			find();
		}
	};

	Action erase = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			table.removeRowsWithLyrics();
		}
	};

	private CaretListener caretListener = new CaretListener() {
		public void caretUpdate(CaretEvent e) {
			if (preventFireUpdate) {
				return;
			}
			if (isSpellChecking) {
				return;
			}
			if (table == null) {
				return;
			}

			table.getSelectionModel().removeListSelectionListener(
					tableSelectionListener);
			lyricsArea.removeCaretListener(caretListener);
			selectSyllables(e.getDot(), e.getMark());
			lyricsArea.addCaretListener(caretListener);
			table.getSelectionModel().addListSelectionListener(
					tableSelectionListener);
			tableSelectionListener.valueChanged(null);
			table.updatePlayerPosition();
		}
	};

	/**
	 * Description of the Class
	 *
	 * @author Saruta
	 */
	class FindReplace extends JDialog {
		private static final long serialVersionUID = 2979657194837453134L;
		private JTextArea findInput, replaceInput;
		private JButton find, rfind, rall, close;

		/**
		 * Constructor for the FindReplace object
		 */
		public FindReplace() {
			super(JOptionPane.getFrameForComponent(lyricsArea));

			JPanel panel = new JPanel(new GridLayout(2, 1));
			JPanel spanel = new JPanel(new BorderLayout());
			spanel.add("North", new JLabel(I18.get("tool_lyrics_find_search")));
			spanel.add("Center", new JScrollPane(findInput = new JTextArea()));
			panel.add(spanel);

			JPanel rpanel = new JPanel(new BorderLayout());
			rpanel.add("North", new JLabel(I18.get("tool_lyrics_find_replace")));
			rpanel.add("Center",
					new JScrollPane(replaceInput = new JTextArea()));
			panel.add(rpanel);

			JPanel buttonPanel = new JPanel(new BorderLayout());
			JPanel buttons = new JPanel(new GridLayout(0, 1));
			buttonPanel.add("North", buttons);
			buttonPanel.add("Center", new JLabel());

			buttons.add(find = new JButton(I18.get("tool_lyrics_find_ok")));
			find.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					search();
				}
			});
			buttons.add(rfind = new JButton(I18
					.get("tool_lyrics_find_replace_find")));
			rfind.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					replace();
				}
			});
			buttons.add(rall = new JButton(I18
					.get("tool_lyrics_find_replace_all")));
			rall.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					replaceAll();
				}
			});
			buttons.add(close = new JButton(I18.get("tool_lyrics_find_close")));
			close.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					hide();
				}
			});
			JLabel help;
			buttons.add(help = new JLabel("<html><p style='margin:5px'>"
					+ I18.get("tool_lyrics_find_info") + "</html>"));
			help.setForeground(Color.gray);
			JPanel panel2 = new JPanel(new BorderLayout());
			panel2.add("Center", panel);
			panel2.add("East", buttonPanel);

			setContentPane(panel2);

			setModal(false);
			setAlwaysOnTop(true);
			setTitle(I18.get("tool_lyrics_find_title"));
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					hide();
				}
			});
			pack();
			setSize(400, 300);

			// setLocation(dim.width/2-200, dim.height/2-150);
		}

		/**
		 * Description of the Method
		 */
		public void locateAndShow() {
			Point p = new Point();
			SwingUtilities.convertPointToScreen(p, lyricsArea);
			// setSize(300, 200);
			// setLocation(p.x + lyricsArea.getSize().width - 300, p.y + 200);
			findInput.setText(lyricsArea.getSelectedText());
			replaceInput.setText("");
			replaceInput.requestFocus();
			setVisible(true);
		}

		/**
		 * Description of the Method
		 */
		@SuppressWarnings("deprecation")
		public void hide() {
			super.hide();
			editLyrics();
		}

		Pattern pat = null;
		Matcher m = null;
		String findText = null;

		/**
		 * Description of the Method
		 */
		public void search() {
			int n = lyricsArea.getCaret().getDot();

			// if (ignoreCase) flags |= Pattern.CASE_INSENSITIVE;
			if (!findInput.getText().equals(findText) || pat == null) {
				findText = findInput.getText();
				pat = Pattern.compile(findText, Pattern.MULTILINE);
				m = pat.matcher(lyricsArea.getText());
			}
			if (!m.find(n)) {
				// rewind
				m = pat.matcher(lyricsArea.getText());
				if (!m.find(0)) {
					return;
				}
			}

			int in = m.start();

			int out = m.end();
			table.getSelectionModel().removeListSelectionListener(
					tableSelectionListener);
			lyricsArea.removeCaretListener(caretListener);
			lyricsArea.getCaret().setDot(in);
			lyricsArea.getCaret().moveDot(out);
			lyricsArea.getStyledDocument().setCharacterAttributes(0,
					lyricsArea.getStyledDocument().getLength(), notSelectStyle,
					false);
			lyricsArea.getStyledDocument().setCharacterAttributes(in, out - in,
					selectStyle, false);
			try {
				Rectangle r = lyricsArea.modelToView(in);
				r.add(lyricsArea.modelToView(out));
				lyricsArea.scrollRectToVisible(r);
			} catch (Exception ignored) {
			}

			selectSyllables(in, out);
			lyricsArea.addCaretListener(caretListener);
			table.getSelectionModel().addListSelectionListener(
					tableSelectionListener);
		}

		/**
		 * Description of the Method
		 */
		public void replace() {
			if (m == null) {
				search();
			}

			StringBuffer sb = new StringBuffer();
			String rep = replaceInput.getText();
			int in;
			int out;
			try {
				in = m.start();
				out = in + rep.length();
				m.appendReplacement(sb, rep);
				m.appendTail(sb);
			} catch (Exception e) {
				return;
			}

			table.getSelectionModel().removeListSelectionListener(
					tableSelectionListener);
			lyricsArea.removeCaretListener(caretListener);
			lyricsArea.setText(sb.toString());
			lyricsArea.getCaret().setDot(in);
			lyricsArea.getCaret().moveDot(out);
			lyricsArea.getStyledDocument().setCharacterAttributes(0,
					lyricsArea.getStyledDocument().getLength(), notSelectStyle,
					false);
			lyricsArea.getStyledDocument().setCharacterAttributes(in, out - in,
					selectStyle, false);
			try {
				Rectangle r = lyricsArea.modelToView(in);
				r.add(lyricsArea.modelToView(out));
				lyricsArea.scrollRectToVisible(r);
			} catch (Exception ignored) {
			}

			selectSyllables(in, out);
			lyricsArea.addCaretListener(caretListener);
			table.getSelectionModel().addListSelectionListener(
					tableSelectionListener);

			m = pat.matcher(lyricsArea.getText());
			search();
		}

		/**
		 * Description of the Method
		 */
		public void replaceAll() {
			if (m == null) {
				search();
			}

			String txt = m.replaceAll(replaceInput.getText());

			table.getSelectionModel().removeListSelectionListener(
					tableSelectionListener);
			lyricsArea.removeCaretListener(caretListener);

			lyricsArea.setText(txt);

			lyricsArea.addCaretListener(caretListener);
			table.getSelectionModel().addListSelectionListener(
					tableSelectionListener);
		}
	}
}
