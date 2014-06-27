package yass.options;

import yass.I18;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    22. August 2007
 */
public class ImagesErrorPanel extends OptionsPanel {

	private static final long serialVersionUID = -7160783083146787083L;

	/**
	 *  Gets the body attribute of the DirPanel object
	 */
	public void addRows() {
		setLabelWidth(140);		
		addSeparator(I18.get("options_images_cover_size"));
		addText(I18.get("options_images_cover_size_max"), "cover-max-size");
		addText(I18.get("options_images_cover_size_minwidth"), "cover-min-width");
		addText(I18.get("options_images_cover_size_maxwidth"), "cover-max-width");
		addText(I18.get("options_images_cover_size_ratio"), "cover-ratio");
		addBoolean("", "use-cover-ratio", I18.get("options_images_cover_size_use_ratio"));
		//addComment("Defines group: Uncommon Cover Size.");
		addSeparator(I18.get("options_images_background_size"));
		addText(I18.get("options_images_background_size_max"), "background-max-size");
		addText(I18.get("options_images_background_size_minwidth"), "background-min-width");
		addText(I18.get("options_images_background_size_maxwidth"), "background-max-width");
		addText(I18.get("options_images_background_size_ratio"), "background-ratio");
		addBoolean("", "use-background-ratio", I18.get("options_images_background_size_use_ratio"));
		//addComment("Defines group: Uncommon Background Size.");
	}
}

