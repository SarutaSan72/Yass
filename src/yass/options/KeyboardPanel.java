package yass.options;

import yass.I18;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 22. August 2007
 */
public class KeyboardPanel extends OptionsPanel {

    private static final long serialVersionUID = 1765234333293781794L;

    /**
     * Gets the body attribute of the DirPanel object
     */
    public void addRows() {
        // 0=next_note, 1=prev_note, 2=page_down, 3=page_up, 4=init, 5=init_next, 6=right, 7=left, 8=up, 9=down,
        // 10=lengthen, 11=shorten, 12=play, 13=play_page, 14=scroll_left, 15=scroll_right, 16=one_page

        setLabelWidth(150);
        addText(I18.get("options_key_next_note"), "key-0");
        addText(I18.get("options_key_prev_note"), "key-1");
        addText(I18.get("options_key_page_down"), "key-2");
        addText(I18.get("options_key_page_up"), "key-3");
        addText(I18.get("options_key_init"), "key-4");
        addText(I18.get("options_key_init_next"), "key-5");
        addText(I18.get("options_key_note_right"), "key-6");
        addText(I18.get("options_key_note_left"), "key-7");
        addText(I18.get("options_key_note_up"), "key-8");
        addText(I18.get("options_key_note_down"), "key-9");
        addText(I18.get("options_key_note_lengthen"), "key-10");
        addText(I18.get("options_key_note_shorten"), "key-11");
        addText(I18.get("options_key_play"), "key-12");
        addText(I18.get("options_key_play_page"), "key-13");
        addText(I18.get("options_key_scroll_left"), "key-14");
        addText(I18.get("options_key_scroll_right"), "key-15");
        addText(I18.get("options_key_one_page"), "key-16");
        addComment(I18.get("options_key_comment"));
    }
}

