package yass.wizard;

import com.nexes.wizard.Wizard;
import com.nexes.wizard.WizardPanelDescriptor;
import yass.I18;
import yass.YassTable;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 25. August 2007
 */
public class CreateSongWizard extends Wizard {
    MP3 mp3 = null;
    Header header = null;
    Edition edition = null;
    Melody melody = null;
    Lyrics lyrics = null;
    LyricsForMIDI lyricsformidi = null;
    MIDI midi = null;
    Tap tap = null;


    /**
     * Constructor for the CreateSongWizard object
     *
     * @param parent Description of the Parameter
     */
    public CreateSongWizard(Component parent) {
        super(JOptionPane.getFrameForComponent(parent));
        getDialog().setTitle(I18.get("create_title"));

        registerWizardPanel(Melody.ID,
                new WizardPanelDescriptor(Melody.ID, melody = new Melody(this)) {
                    public Object getNextPanelDescriptor() {
                        if (melody.getFilename() != null && new File(melody.getFilename()).exists()) {
                            return MIDI.ID;
                        }
                        return Lyrics.ID;
                    }


                    public Object getBackPanelDescriptor() {
                        return null;
                    }


                    public void aboutToDisplayPanel() {
                        melody.setFilename(getValue("melody"));
                    }
                });
        registerWizardPanel(Lyrics.ID,
                new WizardPanelDescriptor(Lyrics.ID, lyrics = new Lyrics(this)) {
                    public Object getNextPanelDescriptor() {
                        return MP3.ID;
                    }


                    public Object getBackPanelDescriptor() {
                        return Melody.ID;
                    }


                    public void aboutToDisplayPanel() {
                        setValue("melodytable", "");
                        lyrics.setText(getValue("lyrics"));
                    }


                    public void aboutToHidePanel() {
                        setValue("lyrics", lyrics.getText());
                        setValue("melodytable", lyrics.getTable());
                        setValue("bpm", "300");
                    }
                });
        registerWizardPanel(MIDI.ID,
                new WizardPanelDescriptor(MIDI.ID, midi = new MIDI(this)) {
                    public Object getNextPanelDescriptor() {
                        if (melody.getFilename() != null && new File(melody.getFilename()).exists()) {
                            String txt = midi.getText();
                            if (txt != null) {
                                YassTable t = new YassTable();
                                t.setText(txt);
                                if (t.hasLyrics()) {
                                    setValue("haslyrics", "yes");
                                    return MP3.ID;
                                }
                            }
                        }
                        setValue("haslyrics", "no");
                        return LyricsForMIDI.ID;
                    }


                    public Object getBackPanelDescriptor() {
                        return Melody.ID;
                    }


                    public void aboutToDisplayPanel() {
                        midi.setFilename(getValue("melody"));
                        setValue("melodytable", "");
                        midi.startRendering();
                    }


                    public void aboutToHidePanel() {
                        if (melody.getFilename() != null && new File(melody.getFilename()).exists()) {
                            setValue("melodytable", midi.getText());
                            setValue("bpm", midi.getMaxBPM());
                        } else {
                            setValue("melodytable", "");
                        }
                        midi.stopRendering();
                    }
                });
        registerWizardPanel(LyricsForMIDI.ID,
                new WizardPanelDescriptor(LyricsForMIDI.ID, lyricsformidi = new LyricsForMIDI(this)) {
                    public Object getNextPanelDescriptor() {
                        return MP3.ID;
                    }


                    public Object getBackPanelDescriptor() {
                        return MIDI.ID;
                    }


                    public void aboutToDisplayPanel() {
                        lyricsformidi.setHyphenations(getValue("hyphenations"));
                        lyricsformidi.setTable(getValue("melodytable"));
                        lyricsformidi.setText(getValue("lyrics"));
                        lyricsformidi.requestFocus();
                    }


                    public void aboutToHidePanel() {
                        setValue("lyrics", lyricsformidi.getText());
                        setValue("melodytable", lyricsformidi.getTable());
                    }
                });
        registerWizardPanel(MP3.ID,
                new WizardPanelDescriptor(MP3.ID, mp3 = new MP3(this)) {
                    public Object getNextPanelDescriptor() {
                        return Header.ID;
                    }


                    public Object getBackPanelDescriptor() {
                        if (melody.getFilename() != null && new File(melody.getFilename()).exists()) {
                            String hasLyrics = getValue("haslyrics");
                            if (hasLyrics != null && hasLyrics.equals("yes")) {
                                return MIDI.ID;
                            }
                            return LyricsForMIDI.ID;
                        }
                        return Lyrics.ID;
                    }


                    public void aboutToDisplayPanel() {
                        mp3.setFilename(getValue("filename"));
                    }
                });
        registerWizardPanel(Header.ID,
                new WizardPanelDescriptor(Header.ID, header = new Header(this)) {
                    public Object getNextPanelDescriptor() {
                        return Edition.ID;
                    }


                    public Object getBackPanelDescriptor() {
                        return MP3.ID;
                    }


                    public void aboutToDisplayPanel() {
                        header.setGenres(getValue("genres"), getValue("genres-more"));
                        header.setLanguages(getValue("languages"), getValue("languages-more"));
                        header.setTitle(getValue("title"));
                        header.setArtist(getValue("artist"));
                        header.setBPM(getValue("bpm"));
                        header.setGenre(getValue("genre"));
                    }


                    public void aboutToHidePanel() {
                        setValue("title", header.getTitle());
                        setValue("artist", header.getArtist());
                        setValue("genre", header.getGenre());
                        setValue("language", header.getLanguage());
                        setValue("bpm", header.getBPM());
                    }
                });
        registerWizardPanel(Edition.ID,
                new WizardPanelDescriptor(Edition.ID, edition = new Edition(this)) {
                    public Object getNextPanelDescriptor() {
                        return Tap.ID;
                    }


                    public Object getBackPanelDescriptor() {
                        return Header.ID;
                    }


                    public void aboutToDisplayPanel() {
                        edition.setSongDir(getValue("songdir"));
                        edition.setFolder(getValue("folder"));
                    }


                    public void aboutToHidePanel() {
                        setValue("folder", edition.getFolder());
                        setValue("edition", edition.getEdition());
                    }
                });
        registerWizardPanel(Tap.ID,
                new WizardPanelDescriptor(Tap.ID, tap = new Tap(this)) {
                    public Object getNextPanelDescriptor() {
                        return FINISH;
                    }


                    public Object getBackPanelDescriptor() {
                        return Header.ID;
                    }


                    public void aboutToDisplayPanel() {
                        tap.updateTable();
                    }
                });
    }


    /**
     * Description of the Method
     */
    public void show() {
        String s = getValue("melody");
        if (s != null && s.trim().length() > 0) {
            setCurrentPanel(MIDI.ID);
            melody.setFilename(s);
            midi.setFilename(s);
            setValue("melodytable", "");
            midi.startRendering();
        } else {
            setCurrentPanel(Melody.ID);
        }
        setModal(true);
        getDialog().pack();
        getDialog().setSize(new Dimension(600, 480));
        getDialog().setVisible(true);
    }


    /**
     * Description of the Method
     */
    public void hide() {
        getDialog().setVisible(false);
    }
}

