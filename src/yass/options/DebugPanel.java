package yass.options;

import yass.I18;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    22. August 2007
 */
public class DebugPanel extends OptionsPanel {

	private static final long serialVersionUID = 2888327974296685430L;

	/**
	 *  Gets the body attribute of the DirPanel object
	 */
	public void addRows() {
		addSeparator(I18.get("options_debug_editor"));
		addBoolean(I18.get("options_debug_memory_title"), "debug-memory", I18.get("options_debug_memory"));
		addBoolean(I18.get("options_debug_accuracy_title"), "debug-waveform", I18.get("options_debug_waveform"));
		addSeparator(I18.get("options_debug_player"));
		addBoolean(I18.get("options_debug_score_title"), "debug-score", I18.get("options_debug_score"));
	}
}

