package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class ErrorPanel extends OptionsPanel {

    private static final long serialVersionUID = -7477393618838320424L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        setLabelWidth(140);
        addSeparator(I18.get("options_errors_display"));
        addFile(I18.get("options_errors_font"), "font-file");
        addComment(I18.get("options_errors_font_comment"));
        addBoolean(I18.get("options_errors_touching"), "touching-syllables", I18.get("options_errors_touching_syllables"));
        addRadio(I18.get("options_errors_pages"), "correct-uncommon-pagebreaks", "true|false|unknown", I18.get("options_errors_pages_true") + "|" + I18.get("options_errors_pages_false") + "|" + I18.get("options_errors_pages_unknown"));
        addComment(I18.get("options_errors_pages_comment"));
        addText(I18.get("options_errors_pages_fix"), "correct-uncommon-pagebreaks-fix");
        addComment(I18.get("options_errors_pages_fix_comment"));
        addSeparator(I18.get("options_errors_score"));
        addText(I18.get("options_errors_score_golden"), "max-golden");
        addText(I18.get("options_errors_score_linebonus"), "max-linebonus");
        addText(I18.get("options_errors_score_points"), "max-points");
        addComment(I18.get("options_errors_score_comment"));
        addText(I18.get("options_errors_score_golden_variance"), "golden-allowed-variance");
        addComment(I18.get("options_errors_score_golden_variance_comment"));
        addBoolean("", "freestyle-counts", I18.get("options_errors_score_freestyle"));
    }
}


