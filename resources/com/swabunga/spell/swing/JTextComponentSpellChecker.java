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
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.
    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
package com.swabunga.spell.swing;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.SpellDictionaryCachedDichoDisk;
import com.swabunga.spell.event.DocumentWordTokenizer;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.swing.autospell.AutoSpellEditorKit;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import com.swabunga.spell.swing.autospell.*;

/**
 * This class spellchecks a JTextComponent throwing up a Dialog everytime
 *  it encounters a misspelled word.
 *
 * @author     Robert Gustavsson (robert@lindesign.se)
 * @created    12. September 2007
 */

public class JTextComponentSpellChecker implements SpellCheckListener {

//    private static final String COMPLETED="COMPLETED";
	private String dialogTitle = null;

	private SpellChecker spellCheck = null;
	private JSpellDialog dlg = null;
	private JTextComponent textComp = null;
	private ResourceBundle messages;
	private SpellDictionary mainDict = null;
	private AutoSpellCheckHandler markHandler;


	// Constructor
	/**
	 *Constructor for the JTextComponentSpellChecker object
	 *
	 * @param  dict  Description of the Parameter
	 */
	public JTextComponentSpellChecker(SpellDictionary dict) {
		this(dict, null, null);
	}


	// Convinient Constructors, for those lazy guys.
	/**
	 *Constructor for the JTextComponentSpellChecker object
	 *
	 * @param  dictFile         Description of the Parameter
	 * @exception  IOException  Description of the Exception
	 */
	public JTextComponentSpellChecker(String dictFile)
		throws IOException {
		this(dictFile, null);
	}


	/**
	 *Constructor for the JTextComponentSpellChecker object
	 *
	 * @param  dictFile         Description of the Parameter
	 * @param  title            Description of the Parameter
	 * @exception  IOException  Description of the Exception
	 */
	public JTextComponentSpellChecker(String dictFile, String title)
		throws IOException {
		this(new SpellDictionaryHashMap(new File(dictFile)), null, title);
	}


	/**
	 *Constructor for the JTextComponentSpellChecker object
	 *
	 * @param  dictFile         Description of the Parameter
	 * @param  phoneticFile     Description of the Parameter
	 * @param  title            Description of the Parameter
	 * @exception  IOException  Description of the Exception
	 */
	public JTextComponentSpellChecker(String dictFile, String phoneticFile, String title)
		throws IOException {
		this(new SpellDictionaryHashMap(new File(dictFile), new File(phoneticFile)), null, title);
	}


	/**
	 *Constructor for the JTextComponentSpellChecker object
	 *
	 * @param  dict      Description of the Parameter
	 * @param  userDict  Description of the Parameter
	 * @param  title     Description of the Parameter
	 */
	public JTextComponentSpellChecker(SpellDictionary dict, SpellDictionary userDict, String title) {
		spellCheck = new SpellChecker(dict);
		mainDict = dict;
		spellCheck.setCache();
		if (userDict != null)
			spellCheck.setUserDictionary(userDict);
		spellCheck.addSpellCheckListener(this);
		dialogTitle = title;
		messages = ResourceBundle.getBundle("com.swabunga.spell.swing.messages", Locale.getDefault());
		markHandler = new AutoSpellCheckHandler(spellCheck, messages);
	}


	// added by Saruta
	/**
	 *  Gets the handler attribute of the JTextComponentSpellChecker object
	 *
	 * @return    The handler value
	 */
	public AutoSpellCheckHandler getHandler() {
		return markHandler;
	}


	// MEMBER METHODS

	/**
	 * Set user dictionary (used when a word is added)
	 *
	 * @param  dictionary  The new userDictionary value
	 */
	public void setUserDictionary(SpellDictionary dictionary) {
		if (spellCheck != null)
			spellCheck.setUserDictionary(dictionary);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  textComp  Description of the Parameter
	 */
	private void setupDialog(JTextComponent textComp) {

		Component comp = SwingUtilities.getRoot(textComp);

		// Probably the most common situation efter the first time.
		if (dlg != null && dlg.getOwner() == comp)
			return;

		if (comp != null && comp instanceof Window) {
			if (comp instanceof Frame)
				dlg = new JSpellDialog((Frame) comp, dialogTitle, true);
			if (comp instanceof Dialog)
				dlg = new JSpellDialog((Dialog) comp, dialogTitle, true);
			// Put the dialog in the middle of it's parent.
			if (dlg != null) {
				Window win = (Window) comp;
				int x = (int) (win.getLocation().getX() + win.getWidth() / 2 - dlg.getWidth() / 2);
				int y = (int) (win.getLocation().getY() + win.getHeight() / 2 - dlg.getHeight() / 2);
				dlg.setLocation(x, y);
			}
		}
		else {
			dlg = new JSpellDialog((Frame) null, dialogTitle, true);
		}
	}


	/**
	 * This method is called to check the spelling of a JTextComponent.
	 *
	 * @param  textComp  The JTextComponent to spellcheck.
	 * @return           Either SpellChecker.SPELLCHECK_OK,  SpellChecker.SPELLCHECK_CANCEL or the number of errors found. The number of errors are those that
	 * are found BEFORE any corrections are made.
	 */
	public synchronized int spellCheck(JTextComponent textComp) {
		setupDialog(textComp);
		this.textComp = textComp;

		DocumentWordTokenizer tokenizer = new DocumentWordTokenizer(textComp.getDocument());
		int exitStatus = spellCheck.checkSpelling(tokenizer);

		textComp.requestFocus();
		textComp.setCaretPosition(0);
		this.textComp = null;
		try {
			if (mainDict instanceof SpellDictionaryCachedDichoDisk)
				((SpellDictionaryCachedDichoDisk) mainDict).saveCache();
		}
		catch (IOException ex) {
			System.err.println(ex.getMessage());
		}
		return exitStatus;
	}


	/**
	 * @param  pane
	 */
	public void startAutoSpellCheck(JEditorPane pane) {
		Document doc = pane.getDocument();
		pane.setEditorKit(new AutoSpellEditorKit((StyledEditorKit) pane.getEditorKit()));
		pane.setDocument(doc);
		markHandler.addJEditorPane(pane);
	}


	/**
	 * @param  pane
	 */
	public void stopAutoSpellCheck(JEditorPane pane) {
		EditorKit kit;
		Document doc;
		if (pane.getEditorKit() instanceof com.swabunga.spell.swing.autospell.AutoSpellEditorKit) {
			doc = pane.getDocument();
			kit = ((com.swabunga.spell.swing.autospell.AutoSpellEditorKit) pane.getEditorKit()).getStyledEditorKit();
			pane.setEditorKit(kit);
			pane.setDocument(doc);
		}
		markHandler.removeJEditorPane(pane);
	}


	/**
	 * @param  event  Description of the Parameter
	 */
	public void spellingError(SpellCheckEvent event) {

//        java.util.List suggestions = event.getSuggestions();
		event.getSuggestions();
		int start = event.getWordContextPosition();
		int end = start + event.getInvalidWord().length();

		// Mark the invalid word in TextComponent
		// added by Saruta
		CaretListener cl[] = textComp.getCaretListeners();
		for (int i = 0; i < cl.length; i++)
			textComp.removeCaretListener(cl[i]);

		textComp.requestFocus();
		textComp.setCaretPosition(0);
		textComp.setCaretPosition(start);
		textComp.moveCaretPosition(end);

		for (int i = 0; i < cl.length; i++)
			textComp.addCaretListener(cl[i]);

		try {
			Rectangle r = textComp.modelToView(start);
			r.add(textComp.modelToView(end));
			textComp.scrollRectToVisible(r);
		}
		catch (Exception ex) {}
		dlg.show(event);
	}


	/**
	 *  Gets the dialog attribute of the JTextComponentSpellChecker object
	 *
	 * @return    The dialog value
	 */
	public JSpellDialog getDialog() {
		return dlg;
	}

}

