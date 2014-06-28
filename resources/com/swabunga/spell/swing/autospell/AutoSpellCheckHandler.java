/*
    Jazzy - a Java library for Spell Checking
    Copyright (C) 2001 Mindaugas Idzelis
    Full text of license can be found in LICENSE.txt
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.
    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNE105SS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.
    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
package com.swabunga.spell.swing.autospell;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import com.swabunga.spell.event.*;
import com.swabunga.spell.engine.*;

/**
 * This class handles the actual autospelling by implementing some listeners
 * on the spellchecked JEditorPane and Document.
 *
 * @author     Robert Gustavsson (robert@lindesign.se)
 * @created    11. September 2007
 */
public class AutoSpellCheckHandler extends MouseAdapter implements DocumentListener,
		AutoSpellConstants {

	private SpellChecker sCheck = null;
	private Configuration config = Configuration.getConfiguration();
	private ResourceBundle messages = null;


	/**
	 *Constructor for the AutoSpellCheckHandler object
	 *
	 * @param  sc  Description of the Parameter
	 */
	public AutoSpellCheckHandler(SpellChecker sc) {
		sCheck = sc;
	}


	/**
	 *Constructor for the AutoSpellCheckHandler object
	 *
	 * @param  sc  Description of the Parameter
	 * @param  rs  Description of the Parameter
	 */
	public AutoSpellCheckHandler(SpellChecker sc, ResourceBundle rs) {
		this(sc);
		messages = rs;
	}


	/**
	 *  Adds a feature to the JEditorPane attribute of the AutoSpellCheckHandler object
	 *
	 * @param  pane  The feature to be added to the JEditorPane attribute
	 */
	public void addJEditorPane(JEditorPane pane) {
		StyledDocument doc = (StyledDocument) pane.getDocument();
		markupSpelling(doc, 0, doc.getLength() - 1);
		doc.addDocumentListener(this);
		pane.addMouseListener(this);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  pane  Description of the Parameter
	 */
	public void removeJEditorPane(JEditorPane pane) {
		Document doc = pane.getDocument();
		pane.removeMouseListener(this);
		doc.removeDocumentListener(this);
	}


	// added by Saruta
	/**
	 *  Description of the Method
	 *
	 * @param  pane  Description of the Parameter
	 */
	public void markupSpelling(JEditorPane pane) {
		StyledDocument doc = (StyledDocument) pane.getDocument();
		markupSpelling(doc, 0, doc.getLength());
	}


	/**
	 *  Description of the Method
	 *
	 * @param  doc    Description of the Parameter
	 * @param  start  Description of the Parameter
	 * @param  end    Description of the Parameter
	 */
	private void markupSpelling(StyledDocument doc, int start, int end) {
		if (doc.getLength() < 1)
			return;

		int wordStart = -1;
		int wordEnd = -1;
		String word;
		DocumentWordTokenizer docTok;
		Segment seg = new Segment();

		docTok = new DocumentWordTokenizer(doc);
		if (start > 0) {
			docTok.posStartFullWordFrom(start);
		}

		while (docTok.hasMoreWords() && docTok.getCurrentWordPosition() <= end) {
			word = docTok.nextWord();
			if (word == null)
				break;
			wordStart = docTok.getCurrentWordPosition();

			// Mark non word parts (spaces) as correct
			if (wordEnd != -1) {
				//System.out.println("Space:"+wordEnd+","+wordStart);
				markAsCorrect(doc, wordEnd, wordStart);
			}
			wordEnd = docTok.getCurrentWordEnd();

			if (wordEnd > doc.getLength())
				wordEnd = doc.getLength() - 1;
			if (wordStart >= wordEnd)
				continue;
			//System.out.println("Word:"+wordStart+","+wordEnd);

			// changed by Saruta
			word = word.replaceAll("-", "");
			word = word.replaceAll("~", "");
			//System.out.println("Word:"+word);

			if (sCheck.isCorrect(word) || sCheck.isIgnored(word)) {
				markAsCorrect(doc, wordStart, wordEnd);
			}
			else {
				markAsMisspelled(doc, wordStart, wordEnd);
			}
		}
		// Mark the rest (if any) as correct.
		if (wordEnd < end && wordEnd != -1) {
			//System.out.println("End:"+wordEnd+","+end);
			markAsCorrect(doc, wordEnd, end);
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  doc    Description of the Parameter
	 * @param  start  Description of the Parameter
	 * @param  end    Description of the Parameter
	 */
	private void markAsMisspelled(StyledDocument doc, int start, int end) {
		SimpleAttributeSet attr;
		attr = new SimpleAttributeSet();
		attr.addAttribute(wordMisspelled, wordMisspelledTrue);
		doc.setCharacterAttributes(start, end - start, attr, false);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  doc    Description of the Parameter
	 * @param  start  Description of the Parameter
	 * @param  end    Description of the Parameter
	 */
	private void markAsCorrect(StyledDocument doc, int start, int end) {
		SimpleAttributeSet attr;
		attr = new SimpleAttributeSet(doc.getCharacterElement((start + end) / 2).getAttributes());
		attr.removeAttribute(wordMisspelled);
		if (end >= start)
			doc.setCharacterAttributes(start, end - start, attr, true);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  evt  Description of the Parameter
	 */
	private void handleDocumentChange(DocumentEvent evt) {
		Element curElem;
		Element
				parElem;
		StyledDocument doc;
		int start;
		int
				end;

		if (evt.getDocument() instanceof StyledDocument) {
			doc = (StyledDocument) evt.getDocument();
			curElem = doc.getCharacterElement(evt.getOffset());
			parElem = curElem.getParentElement();
			if (parElem != null) {
				start = parElem.getStartOffset();
				end = parElem.getEndOffset();
			}
			else {
				start = curElem.getStartOffset();
				end = curElem.getEndOffset();
			}
			//System.out.println("curElem: "+curElem.getStartOffset()+", "+curElem.getEndOffset());
			//System.out.println("parElem: "+parElem.getStartOffset()+", "+parElem.getEndOffset());
			//System.out.println("change: "+start+", "+end);
			markupSpelling(doc, start, end);
		}
	}


	/**
	 *  Gets the popup attribute of the AutoSpellCheckHandler object
	 *
	 * @return    The popup value
	 */
	public JPopupMenu getPopup() {
		return popup;
	}


	private JPopupMenu popup;


	/**
	 *  Description of the Method
	 *
	 * @param  pane  Description of the Parameter
	 * @param  p     Description of the Parameter
	 */
	private void showSuggestionPopup(JEditorPane pane, Point p) {
		StyledDocument doc;
		JMenuItem item;
		AttributeSet attr;
		int pos = pane.viewToModel(p);
		DocumentWordTokenizer docTok;
		String word;
		java.util.List suggestions;
		ReplaceListener repList;

		if (pos >= 0) {
			doc = (StyledDocument) pane.getDocument();
			attr = doc.getCharacterElement(pos).getAttributes();
			if (attr.containsAttribute(wordMisspelled, wordMisspelledTrue)) {
				docTok = new DocumentWordTokenizer(doc);
				docTok.posStartFullWordFrom(pos);
				word = docTok.nextWord();
				//added by Saruta
				int hcount = 0;
				char[] cw = word.toCharArray();
				for (int c = 0; c < cw.length; c++) {
					if (cw[c] == '-')
						hcount++;
				}
				suggestions = sCheck.getSuggestions(word, config.getInteger(Configuration.SPELL_THRESHOLD));

				popup = new JPopupMenu();
				repList = new ReplaceListener(docTok);
				for (int i = 0; i < suggestions.size(); i++) {
					com.swabunga.spell.engine.Word w = (com.swabunga.spell.engine.Word) suggestions.get(i);

					// added by Saruta
					String s = w.toString();
					for (int k = 0; k < hcount; k++)
						s = s + "-~";

					item = new JMenuItem(s);
					item.setActionCommand(s);
					item.addActionListener(repList);
					popup.add(item);
				}
				popup.addSeparator();
				item = new JMenuItem();
				if (messages != null)
					item.setText(messages.getString("IGNOREALL"));
				else
					item.setText("Ignore All");
				item.setActionCommand(word);
				item.addActionListener(new IgnoreAllListener(doc));
				popup.add(item);
				item = new JMenuItem();
				if (messages != null)
					item.setText(messages.getString("ADD"));
				else
					item.setText("Add word to wordlist");
				item.setActionCommand(word);
				item.addActionListener(new AddToDictListener(doc));
				popup.add(item);
				popup.show(pane, p.x, p.y);
			}
		}
	}


	// DocumentListener implementation
	// ------------------------------------------------------------------
	/**
	 *  Description of the Method
	 *
	 * @param  evt  Description of the Parameter
	 */
	public void changedUpdate(DocumentEvent evt) {
	}


	/**
	 *  Description of the Method
	 *
	 * @param  evt  Description of the Parameter
	 */
	public void insertUpdate(DocumentEvent evt) {
		Runnable r = new SpellCheckChange(evt);
		SwingUtilities.invokeLater(r);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  evt  Description of the Parameter
	 */
	public void removeUpdate(DocumentEvent evt) {
		Runnable r = new SpellCheckChange(evt);
		SwingUtilities.invokeLater(r);
	}


	// MouseListener implementation
	// ------------------------------------------------------------------
	/**
	 *  Description of the Method
	 *
	 * @param  evt  Description of the Parameter
	 */
	public void mouseReleased(MouseEvent evt) {
		JEditorPane pane;
		if (!(evt.getComponent() instanceof JEditorPane))
			return;

		if (evt.isPopupTrigger()) {
			pane = (JEditorPane) evt.getComponent();
			if (pane.isEditable())
				showSuggestionPopup(pane, new Point(evt.getX(), evt.getY()));
		}
	}


	// INNER CLASSES
	// ------------------------------------------------------------------
	/**
	 *  Description of the Class
	 *
	 * @author     Saruta
	 */
	private class SpellCheckChange implements Runnable {

		private DocumentEvent evt;


		/**
		 *Constructor for the SpellCheckChange object
		 *
		 * @param  evt  Description of the Parameter
		 */
		public SpellCheckChange(DocumentEvent evt) {
			this.evt = evt;
		}


		/**
		 *  Main processing method for the SpellCheckChange object
		 */
		public void run() {
			handleDocumentChange(evt);
		}

	}


	/**
	 *  Description of the Class
	 *
	 * @author     Saruta
	 */
	private class ReplaceListener implements ActionListener {

		DocumentWordTokenizer tok;


		/**
		 *Constructor for the ReplaceListener object
		 *
		 * @param  tok  Description of the Parameter
		 */
		public ReplaceListener(DocumentWordTokenizer tok) {
			this.tok = tok;
		}


		/**
		 *  Description of the Method
		 *
		 * @param  evt  Description of the Parameter
		 */
		public void actionPerformed(ActionEvent evt) {
			tok.replaceWord(evt.getActionCommand());
		}
	}


	/**
	 *  Description of the Class
	 *
	 * @author     Saruta
	 */
	private class AddToDictListener implements ActionListener {

		private StyledDocument doc;


		/**
		 *Constructor for the AddToDictListener object
		 *
		 * @param  doc  Description of the Parameter
		 */
		public AddToDictListener(StyledDocument doc) {
			this.doc = doc;
		}


		/**
		 *  Description of the Method
		 *
		 * @param  evt  Description of the Parameter
		 */
		public void actionPerformed(ActionEvent evt) {
			String word = evt.getActionCommand();
			// changed by Saruta
			word = word.replaceAll("-", "");
			word = word.replaceAll("~", "");
			sCheck.addToDictionary(word);
			Runnable r = new MarkUpSpellingAll(doc);
			SwingUtilities.invokeLater(r);
		}
	}


	/**
	 *  Description of the Class
	 *
	 * @author     Saruta
	 */
	private class IgnoreAllListener implements ActionListener {

		private StyledDocument doc;


		/**
		 *Constructor for the IgnoreAllListener object
		 *
		 * @param  doc  Description of the Parameter
		 */
		public IgnoreAllListener(StyledDocument doc) {
			this.doc = doc;
		}


		/**
		 *  Description of the Method
		 *
		 * @param  evt  Description of the Parameter
		 */
		public void actionPerformed(ActionEvent evt) {
			sCheck.ignoreAll(evt.getActionCommand());
			Runnable r = new MarkUpSpellingAll(doc);
			SwingUtilities.invokeLater(r);
		}
	}


	/**
	 *  Description of the Class
	 *
	 * @author     Saruta
	 */
	private class MarkUpSpellingAll implements Runnable {

		private StyledDocument doc;


		/**
		 *Constructor for the MarkUpSpellingAll object
		 *
		 * @param  doc  Description of the Parameter
		 */
		public MarkUpSpellingAll(StyledDocument doc) {
			this.doc = doc;
		}


		/**
		 *  Main processing method for the MarkUpSpellingAll object
		 */
		public void run() {
			markupSpelling(doc, 0, doc.getLength());
		}
	}

}
