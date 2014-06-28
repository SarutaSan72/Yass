package yass;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassMIDIConverter implements DropTargetListener {
    String filename = null;

    String trackTitle[];
    Vector<String>[] trackNotes, trackTimes, trackTimesEnd, trackNotesLength;
    Vector<Long> tempos = new Vector<>();
    JFrame frame = null;
    JLabel infoLabel;
    YassTable[] tables;
    String[] info, labels, instrums;
    boolean[] take;
    int[] realnr;
    JButton start = null, load = null, toggle = null, save = null,
            buttons[] = null;
    JComboBox<Integer> tracksBox = null;
    Sequence[] sequences, mySequences;
    int lyricsTrack = -1, voiceTrack = -1;
    double duration = 0, bpm = 120;
    // midi standard if no tempo is set
    long ticksPerQuarterNote = 0;
    Vector<String> syllables = null, syltimes = null;
    YassSheet sheet = null;
    int selected = 0, realSelected = -1, nTracks = -1;
    boolean standAlone = true;
    JPanel tablePanel = null;
    long sequencerStartTime = 0, sequencerOffset = 0;
    RenderThread renderThread;
    Sequencer sequencer = null;
    Action playAction = new AbstractAction(I18.get("mconvert_play")) {
        private static final long serialVersionUID = 6694227799235490012L;

        public void actionPerformed(ActionEvent e) {
            play();
        }
    };
    Action prevBeatAction = new AbstractAction(I18.get("mconvert_prev")) {
        private static final long serialVersionUID = -8820137824599926174L;

        public void actionPerformed(ActionEvent e) {
            sheet.getActiveTable().prevBeat();
        }
    };
    Action nextBeatAction = new AbstractAction(I18.get("mconvert_next")) {
        private static final long serialVersionUID = -3301972383123120281L;

        public void actionPerformed(ActionEvent e) {
            sheet.getActiveTable().nextBeat();
        }
    };

    // http://www.sonicspot.com/guide/midifiles.html
    Action home = new AbstractAction(I18.get("mconvert_first")) {
        private static final long serialVersionUID = -8164514720142819954L;

        public void actionPerformed(ActionEvent e) {
            sheet.getActiveTable().home();
        }
    };
    Action prevTrack = new AbstractAction(I18.get("mconvert_track_prev")) {
        private static final long serialVersionUID = -8596434378403910112L;

        public void actionPerformed(ActionEvent e) {
            int n = tracksBox.getSelectedIndex() - 1;
            if (n < 0) {
                n = tracksBox.getItemCount() - 1;
            }
            tracksBox.setSelectedIndex(n);
        }
    };
    Action nextTrack = new AbstractAction(I18.get("mconvert_track_next")) {
        private static final long serialVersionUID = -430088914903784903L;

        public void actionPerformed(ActionEvent e) {
            tracksBox.setSelectedIndex((tracksBox.getSelectedIndex() + 1)
                    % tracksBox.getItemCount());
        }
    };
    private String[] instrument = new String[128];
    private Color[] colors = new Color[]{new Color(.3f, .3f, 0.3f, .7f),
            new Color(.3f, .6f, 0.3f, .7f), new Color(.3f, .3f, 0.6f, .7f),
            new Color(.3f, .6f, 0.6f, .7f), new Color(.6f, .6f, 0.3f, .7f),
            new Color(.6f, .3f, 0.6f, .7f), new Color(.4f, .4f, 0.6f, .7f),
            new Color(.4f, .6f, 0.4f, .7f), new Color(.4f, .6f, 0.6f, .7f),
            new Color(.6f, .6f, 0.4f, .7f), new Color(.6f, .4f, 0.6f, .7f),};
    private Synthesizer synthesizer;
    private JComponent dropTarget = null;

    /**
     * Constructor for the YassMIDIConverter object
     */
    public YassMIDIConverter() {
        this.filename = null;
        initInstruments();
    }
    /**
     * Constructor for the YassMIDIConverter object
     *
     * @param filename Description of the Parameter
     */
    public YassMIDIConverter(String filename) {
        this.filename = filename;
        initInstruments();
    }

    /**
     * Description of the Method
     *
     * @param filename Description of the Parameter
     * @param s        Description of the Parameter
     */
    public static void save(String filename, String s) {
        int i = filename.lastIndexOf(".");
        String fn = filename.substring(0, i) + ".txt";
        File f = new File(fn);
        if (f.exists()) {
            f.delete();
        }
        PrintWriter outputStream = null;
        try {
            outputStream = new PrintWriter(new FileWriter(fn));
            outputStream.println(s);
            outputStream.close();
        } catch (Exception e) {
            return;
        }
    }

    /**
     * Description of the Method
     *
     * @param argv Description of the Parameter
     */
    public static void main(String argv[]) {
        String ini = "C:/file.mid";
        if (argv != null && argv.length > 0) {
            ini = argv[0];
        }
        I18.setLanguage(null);
        YassMIDIConverter m = new YassMIDIConverter(ini);
        int voiceTrack = m.init();
        if (voiceTrack < 0 || true) {
            m.openGUI();
        } else {
            String s = m.createTable(voiceTrack);
            if (s != null) {
                save(ini, s);
            }
        }
    }

    /**
     * Sets the standAlone attribute of the YassMIDIConverter object
     *
     * @param onoff The new standAlone value
     */
    public void setStandAlone(boolean onoff) {
        standAlone = onoff;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public int init() {
        if (filename == null) {
            return -1;
        }
        File inFile = new File(filename);
        if (!inFile.exists()) {
            return -1;
        }

        // check for MIDI file format 1
        MidiFileFormat mff = null;
        try {
            mff = MidiSystem.getMidiFileFormat(inFile);
        } catch (Exception e) {
            System.out.println(I18.get("convert_err_read"));
            return -1;
        }
        // if (mff.getType() == 0)

        // get tracks
        Sequence sequence = null;
        try {
            sequence = MidiSystem.getSequence(inFile);
        } catch (Exception e) {
            System.out.println(I18.get("convert_err_format"));
            return -1;
        }
        Track[] tracks = sequence.getTracks();
        if (tracks.length < 1) {
            System.out.println(I18.get("convert_err_tracks"));
            return -1;
        }

        float fDivisionType = sequence.getDivisionType();
        int nResolution = sequence.getResolution();

        boolean isTicksPerQuarterNote = fDivisionType == Sequence.PPQ;
        // System.out.println("isTicksPerQuarterNote: "+isTicksPerQuarterNote);
        ticksPerQuarterNote = sequence.getResolution();
        // System.out.println("ticksPerQuarterNote: "+ticksPerQuarterNote);

        trackTitle = new String[tracks.length];
        trackNotes = new Vector[tracks.length];
        trackTimes = new Vector[tracks.length];
        trackTimesEnd = new Vector[tracks.length];
        trackNotesLength = new Vector[tracks.length];
        tables = new YassTable[tracks.length];
        mySequences = new Sequence[tracks.length];
        sequences = new Sequence[tracks.length];
        info = new String[tracks.length];
        labels = new String[tracks.length];
        take = new boolean[tracks.length];
        realnr = new int[tracks.length];
        instrums = new String[tracks.length];
        tempos.clear();

        syllables = new Vector<>(3000);
        syltimes = new Vector<>(3000);

        duration = 0;
        nTracks = tracks.length;

        int nTempoTrack = -1;

        // for each input track, create a new sequence and copy all
        // events of the input track to a new track in the new sequence
        // then save the new sequence as a distinct file
        for (int nTrack = 0; nTrack < tracks.length; nTrack++) {
            Sequence singleTrackSequence = null;
            try {
                singleTrackSequence = new Sequence(fDivisionType, nResolution);
            } catch (InvalidMidiDataException e) {
                System.out.println(I18.get("convert_err_format"));
                return -1;
            }

            sequences[nTrack] = singleTrackSequence;

            if (nTempoTrack >= 0) {
                Track tempoTrack = singleTrackSequence.createTrack();
                for (int i = 0; i < tracks[nTempoTrack].size(); i++) {
                    tempoTrack.add(tracks[nTempoTrack].get(i));
                }
            }
            Track track = singleTrackSequence.createTrack();
            for (int i = 0; i < tracks[nTrack].size(); i++) {
                track.add(tracks[nTrack].get(i));
            }

            @SuppressWarnings("unused")
            boolean isKar = false;
            String title = null;

            trackTitle[nTrack] = null;
            trackNotes[nTrack] = new Vector<>(3000);
            trackTimes[nTrack] = new Vector<>(3000);
            trackTimesEnd[nTrack] = new Vector<>(3000);
            trackNotesLength[nTrack] = new Vector<>(3000);

            boolean hasTitle = false;

            boolean isVoiceTitle = false;
            int n = track.size();
            long microsecondsPerQuarterNote = (long) (60000000L / bpm);

            long lastTempo = -1;

            long lastT = -1;

            // System.out.print("\n---track #"+nTrack+"\n");
            for (int i = 0; i < n; i++) {
                MidiEvent e = track.get(i);
                long t = e.getTick();

                duration = Math.max(t, duration);

                MidiMessage m = e.getMessage();
                int status = m.getStatus() & 0xFF;
                int len = m.getLength();
                byte[] bmsg = m.getMessage();
                int[] msg = new int[len];
                for (int k = 0; k < len; k++) {
                    msg[k] = bmsg[k] & 0xFF;
                }

                int eventType = status / 16;
                int channel = status / 15;

                if (msg[0] == 255) {
                    // special
                    int type = msg[1];

                    if (type == 81) {
                        // tempo
                        nTempoTrack = nTrack;
                        microsecondsPerQuarterNote = (msg[3] << 16)
                                | (msg[4] << 8) | msg[5];

                        if (t > lastT
                                && lastTempo != microsecondsPerQuarterNote) {
                            tempos.addElement(new Long(t));
                            tempos.addElement(new Long(
                                    microsecondsPerQuarterNote));
                            lastT = t;
                            lastTempo = microsecondsPerQuarterNote;
                            bpm = Math.max(bpm,
                                    60000000.0 / microsecondsPerQuarterNote);
                            // System.out.print("tempo track="+nTrack);
                            // System.out.print("tempo change with bpm="+60000000.0
                            // / microsecondsPerQuarterNote);
                            // System.out.println(" at tick "+t);
                        }
                    } else if (type == 1 || type == 5) {
                        // text or lyrics
                        StringBuffer sb = new StringBuffer();
                        for (int k = 3; k < len; k++) {
                            sb.append((char) (msg[k]));
                        }
                        String s = sb.toString();
                        // System.out.print(s);

                        if (s.equals("@KMIDI KARAOKE FILE")) {
                            isKar = true;
                        } else if (s.startsWith("@T")) {
                            if (title != null) {
                                title += "\n";
                            }
                            title += s.substring(2);
                            hasTitle = true;
                        } else if (s.startsWith("\\")) {
                            if (hasTitle) {
                                syltimes.addElement(t + "");
                                syllables.addElement("/" + s.substring(1));
                            }
                        } else if (s.startsWith("/")) {
                            if (hasTitle) {
                                syltimes.addElement(t + "");
                                syllables.addElement("/" + s.substring(1));
                            }
                        } else {
                            if (hasTitle) {
                                syltimes.addElement(t + "");
                                syllables.addElement(s);
                            }
                        }
                        // System.out.print(sb.toString()+"|");
                    } else if (type == 3) {
                        // track title
                        StringBuffer sb = new StringBuffer();
                        for (int k = 3; k < len; k++) {
                            sb.append((char) (msg[k]));
                        }
                        String s = sb.toString();
                        if (trackTitle[nTrack] == null) {
                            trackTitle[nTrack] = nTrack + ": " + s;
                        }
                        // DEBUG if (trackTitle[nTrack]==null)
                        // trackTitle[nTrack] = s;
                        s = s.toLowerCase();
                        if (s.equals("vox") || s.equals("voice")
                                || s.startsWith("vocal")) {
                            isVoiceTitle = true;
                        }
                    } else if (type == 84) {
                        // SMPTE offset
                        int hour = msg[3];
                        int min = msg[4];
                        int sec = msg[5];
                        int frames = msg[6];
                        int subframes = msg[7];
                        // System.out.print("SMPTE offset "+hour+" "+min+" "+sec+" "+frames+" "+subframes);
                        // System.out.println(" at "+t);
                    } else if (type == 88) {
                        // time signature
                        int numer = msg[3];
                        int deno = msg[4];
                        int metro = msg[5];
                        int s32 = msg[6];
                        // System.out.print("time signature "+numer+" "+deno+" "+metro+" "+s32);
                        // System.out.println(" at "+t);
                    } else if (type == 89) {
                        // key signature
                    } else if (type == 47) {
                        // end of track
                    } else {
                        // track title
                        // System.out.println("Unknown Meta Event: "+type);
                    }
                } else if (eventType == 8) {
                    // note off
                    trackTimesEnd[nTrack].addElement(t + "");
                } else if (eventType == 9) {
                    // note on
                    int note = msg[1];
                    int velocity = msg[2];
                    if (velocity < 1) {
                        trackTimesEnd[nTrack].addElement(t + "");
                    } else {
                        trackTimes[nTrack].addElement(t + "");
                        trackNotes[nTrack].addElement(note + "");
                    }
                    // System.out.print(t+"#"+note+"|"+velocity);
                } else if (eventType == 10) {
                    // note aftertouch
                    int note = msg[1];
                    int velocity = msg[2];
                    // System.out.println("aftertouch at "+t);
                    trackTimesEnd[nTrack].addElement(t + "");
                    trackTimes[nTrack].addElement(t + "");
                    trackNotes[nTrack].addElement(trackNotes[nTrack]
                            .lastElement());
                } else if (eventType == 11) {
                    // controller
                    /*
					 * int ty = msg[2]; if (ty==0)
					 * System.out.println("bank select at "+t); else if (ty>=32
					 * && ty<=63) System.out.println("LSB at "+t); else if
					 * (ty==64) { System.out.println("HOLD PEDAL at "+t); } else
					 * System.out.println("controller "+ty+" at "+t);
					 */
                } else if (eventType == 12) {
                    // program change
                    int instr = msg[1];
                    instrums[nTrack] = instrument[instr];
                } else if (eventType == 14) {
                    // pitch bend
                    // System.out.println("bend at "+time);
                } else {
					/*
					 * System.out.print("event type"+eventType); for (int k=2;
					 * k<len; k++) System.out.print(msg[k]+" ");
					 * System.out.println();
					 */
                }
            }
            if (hasTitle) {
                lyricsTrack = nTrack;
            }
            if (isVoiceTitle) {
                voiceTrack = nTrack;
            }
        }
        return voiceTrack;
    }

    /**
     * Description of the Method
     */
    public void openGUI() {
        Point loc = null;
        Dimension size = null;
        if (renderThread != null) {
            renderThread.notInterrupted = false;
        }
        if (frame != null) {
            loc = frame.getLocation();
            size = frame.getSize();
            frame.dispose();
        }

        frame = new JFrame(I18.get("convert_title"));
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        frame.add("Center", getGUI());

        frame.pack();
        if (loc != null) {
            frame.setLocation(loc);
        }
        if (size != null) {
            frame.setSize(size);
        } else {
            frame.setSize(new Dimension(800, 400));
        }
        frame.setVisible(true);

        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (e.getID() == ComponentEvent.COMPONENT_RESIZED) {
                    updateSheet();
                }
            }
        });

        startGUI();
    }

    /**
     * Description of the Method
     */
    public void updateSheet() {
        sheet.update();
        sheet.repaint();
    }

    /**
     * Gets the gUI attribute of the YassMIDIConverter object
     *
     * @return The gUI value
     */
    public JPanel getGUI() {
        JPanel tracksPanel = new JPanel(new GridLayout(1, 0));
        int bb = 0;

        sheet = new YassSheet();
        YassProperties prop = new YassProperties();
        prop.load();
        Color ncol[] = new Color[6];
        for (int i = 0; i < ncol.length; i++) {
            String c = prop.getProperty("note-color-" + i);
            Color col = Color.decode(c);
            ncol[i] = new Color(col.getRed(), col.getGreen(), col.getBlue(),
                    221);
        }
        sheet.setColors(ncol);

        boolean shade = prop.get("shade-notes").equals("true");
        sheet.showArrows(false);
        sheet.showPlayerButtons(false);
        sheet.showText(false);
        sheet.shadeNotes(false);
        sheet.setVersionTextPainted(false);
        sheet.setPaintHeights(false);
        sheet.setPreferredSize(new Dimension(300, 400));
        JScrollPane scroll = new JScrollPane(sheet);
        if (standAlone) {
            scroll.setPreferredSize(new Dimension(300, 400));
        }
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        setDropTarget(sheet);
        registerActions(sheet);

        double dur = tickToTime(duration);
        // dur = dur * 4 * bpm / 60.0;
        sheet.setDuration(dur * 1000.0);
        sheet.init();
        sheet.update();
        sheet.setZoom(20 * 60 / bpm);

        buttons = null;
        if (nTracks > 0) {
            buttons = new JButton[nTracks];
        }
        tracksBox = new JComboBox<>();

        load = new JButton(I18.get("convert_open"));
        load.setBackground(new Color(240, 240, 240));
        load.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                load();
            }
        });
        if (standAlone) {
            tracksPanel.add(load);
        }

        for (int nTrack = 0; nTrack < nTracks; nTrack++) {
            int ns = trackNotes[nTrack].size();
            int min = 256;
            int max = -256;
            for (int i = 0; i < ns; i++) {
                int note = Integer.parseInt(trackNotes[nTrack].elementAt(i)
                        .toString());
                min = Math.min(min, note);
                max = Math.max(max, note);
            }
            int ts = trackTimes[nTrack].size();
            int te = trackTimesEnd[nTrack].size();
            trackNotesLength[nTrack] = new Vector<>(ts);
            int minL = 256;
            int maxL = -256;
            for (int i = 0; i < ts && i < te; i++) {
                double startTick = Double.parseDouble(trackTimes[nTrack].elementAt(i)
                        .toString());
                double endTick = Double.parseDouble(trackTimesEnd[nTrack].elementAt(i)
                        .toString());
                double start = tickToTime(startTick);
                double end = tickToTime(endTick);
                start = start * 4 * bpm / 60.0;
                end = end * 4 * bpm / 60.0;

                int len = (int) Math.round(end - start);
                trackNotesLength[nTrack].addElement(len + "");
                minL = Math.min(minL, len);
                maxL = Math.max(maxL, len);
            }
            String s = trackTitle[nTrack];
            if (s == null || s.trim().length() < 1) {
                s = I18.get("convert_track") + " " + bb;
            }

            String inst = instrums[nTrack] == null ? I18
                    .get("convert_err_instrument") : instrums[nTrack];
            if (nTrack == lyricsTrack) {
                info[bb] = "<html><center>"
                        + MessageFormat.format(I18.get("convert_play_1"), inst,
                        syllables.size());
            }
            if (ts < 1) {
                continue;
            }
            if (ts == te) {
                info[bb] = "<html><center>"
                        + MessageFormat.format(I18.get("convert_play_2"), inst,
                        syllables.size(), ts, (min - 60), (max - 60),
                        minL, maxL);
            } else {
                info[bb] = "<html><center>"
                        + MessageFormat.format(I18.get("convert_play_3"), inst,
                        ts, te);
            }

            int ntemps = tempos.size();
            if (ntemps > 2) {
                String colon = I18.get("convert_bpm_3");
                MessageFormat at = new MessageFormat(I18.get("convert_bpm_2"));
                info[bb] += "<br>"
                        + MessageFormat.format(I18.get("convert_bpm_1"), bpm,
                        ntemps / 2);
                int ntn = 0;
                for (Enumeration<Long> tmap = tempos.elements(); ++ntn < 5
                        && tmap.hasMoreElements(); ) {
                    long tick = tmap.nextElement().longValue();
                    long mpq = tmap.nextElement().longValue();
                    double bpm2 = 60000000.0 / mpq;
                    info[bb] += " "
                            + at.format(new Object[]{new Integer((int) bpm2),
                            new Integer((int) tickToTime(tick))});
                    if (tmap.hasMoreElements()) {
                        info[bb] += colon + " ";
                    }
                }
				/*
				 * for (Enumeration tmap = tempos.elements();
				 * tmap.hasMoreElements(); ) { long tick =
				 * ((Long)(tmap.nextElement())).longValue(); long mpq =
				 * ((Long)(tmap.nextElement())).longValue(); double bpm2 =
				 * (double)60000000.0 / mpq; System.out.print(" "+((int)bpm2) +
				 * " at "+((int)tickToTime(tick))+"s"); }
				 */
                if (ntn >= 5) {
                    info[bb] += I18.get("convert_bpm_4");
                } else {
                    info[bb] += I18.get("convert_bpm_5");
                }
            } else {
                info[bb] += "<br>"
                        + MessageFormat.format(I18.get("convert_bpm_6"), bpm);
            }

            buttons[bb] = new JButton(s);
            tracksBox.addItem(new Integer(bb));
            labels[bb] = s;
            take[bb] = false;
            realnr[bb] = nTrack;
            tracksBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    updateGUI();
                }
            });
            buttons[bb].setForeground(colors[bb % colors.length]);
            buttons[bb].addActionListener(new TrackListener(bb, nTrack));
            String tableString = createTable(nTrack);
            StringTokenizer st = new StringTokenizer(tableString, "\n");
            tables[bb] = new YassTable();
            tables[bb].setTableColor(colors[bb % colors.length]);
            YassTable.setZoomMode(YassTable.ZOOM_TIME);
            while (st.hasMoreTokens()) {
                String str = st.nextToken();
                if (!tables[bb].addRow(str)) {
                    break;
                }
            }
            tables[bb].setSheet(sheet);
            sheet.addTable(tables[bb]);
            registerActions(tables[bb]);
            setDropTarget(tables[bb]);
            mySequences[bb] = sequences[nTrack];
            if (bb == 0) {
                realSelected = nTrack;
            }
            bb++;
        }

        if (nTracks > 0) {
            tracksPanel.add(new JLabel(I18.get("convert_track_select"),
                    JLabel.CENTER));
        } else {
            tracksPanel.add(new JLabel(I18.get("convert_track_no"),
                    JLabel.CENTER));
        }

        tracksBox.setRenderer(new BoxRenderer());
        // if (bb > 4)
        tracksPanel.add(tracksBox);
        // else {
        // for (int i = 0; i < bb; i++)
        // tracksPanel.add(buttons[i]);
        // }

        start = new JButton(I18.get("convert_play"));
        start.setBackground(new Color(240, 240, 240));
        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                play();
            }
        });

        toggle = new JButton(I18.get("convert_add"));
        toggle.setBackground(new Color(240, 240, 240));
        toggle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toggle();
            }
        });

        save = new JButton(I18.get("convert_save"));
        if (standAlone) {
            save.setText(I18.get("convert_save_exit"));
        }
        if (standAlone) {
            tracksPanel.add(save);
        }
        save.setBackground(new Color(240, 240, 240));
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });

        JPanel infoP = new JPanel(new GridLayout(0, 1));
        infoP.add(tracksPanel);
        infoP.add(infoLabel = new JLabel("", JLabel.CENTER));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", infoP);
        panel.add("Center", scroll);

        tablePanel = new JPanel(new BorderLayout());
        JScrollPane txtScroll = new JScrollPane(tablePanel);
        if (standAlone) {
            panel.add("West", txtScroll);
            if (nTracks <= 0) {
                panel.add("South", new JLabel(I18.get("convert_info_open"),
                        JLabel.RIGHT));
            } else {
                panel.add("South", new JLabel(I18.get("convert_info_tracks"),
                        JLabel.RIGHT));
            }
        }

        if (nTracks > 0) {
            tracksPanel.add(new JLabel(""));
            tracksPanel.add(start);
            tracksPanel.add(toggle);
            if (standAlone) {
                tracksPanel.add(save);
            }
            updateGUI();
        }

        txtScroll.setPreferredSize(new Dimension(150, 100));
        txtScroll
                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        setDropTarget(panel);
        setDropTarget(infoP);
        setDropTarget(scroll);

        return panel;
    }

    /**
     * Description of the Method
     */
    public void toggle() {
        int n = tracksBox.getSelectedIndex();
        take[n] = !take[n];
        tracksBox.repaint();
    }

    /**
     * Description of the Method
     */
    public void startGUI() {
        if (nTracks > 0) {
            infoLabel.setText(info[selected]);
            int i = 0;
            while (i < buttons.length && buttons[i] != null) {
                buttons[i].setBackground(start.getBackground());
                i++;
            }
            buttons[selected].setBackground(Color.white);

            sheet.setActiveTable(selected);
            int p = (int) tables[selected].getGap();
            p = sheet.toTimeline(p);
            sheet.setPlayerPosition(p);
            sheet.scrollRectToVisible(new Rectangle(p, 0, p, 100));
        }

        try {
            System.out.println("Midi Converter: Loading Soundbank...");
            Soundbank s = MidiSystem.getSoundbank(getClass().getResource(
                    "/yass/AJH_Piano.sf2"));
            Instrument[] instr = s.getInstruments();

            sequencer = MidiSystem.getSequencer();

            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            synthesizer.loadInstrument(instr[0]);
            MidiChannel[] mc = synthesizer.getChannels();
            for (int i = 0; i < mc.length; i++) {
                if (mc[i] != null) {
                    mc[i].programChange(0);
                    mc[i].controlChange(7, 127);
                }
            }
            sequencer.open();
            sequencer.getTransmitter().setReceiver(
                    synthesizer.getReceiver());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        renderThread = new RenderThread();
        renderThread.start();
    }

    /**
     * Description of the Method
     */
    public void updateGUI() {
        int n = tracksBox.getSelectedIndex();
        int real = realnr[n];

        boolean isPlaying = sequencer != null && sequencer.isRunning();
        if (isPlaying) {
            sequencer.stop();
            try {
                // note off to all channels; quick hack
                Receiver r = sequencer.getReceiver();
                ShortMessage offMessage = new ShortMessage();
                for (int nChannel = 128; nChannel <= 143; nChannel++) {
                    for (int nKey = 0; nChannel <= 127; nKey++) {
                        offMessage.setMessage(ShortMessage.NOTE_OFF, nChannel,
                                nKey, 0);
                        r.send(offMessage, -1);
                    }
                }
            } catch (Exception ex) {
            }
            sheet.setPlaying(false);
        }
        int i = 0;
        while (i < buttons.length && buttons[i] != null) {
            buttons[i].setBackground(start.getBackground());
            i++;
        }
        buttons[n].setBackground(Color.white);
        infoLabel.setText(info[n]);

        double oldGap = tables[selected].getGap();
        double op = sheet.getPlayerPosition();
        double vop = ((JViewport) sheet.getParent()).getViewPosition().x;
        op = sheet.fromTimeline((int) op);
        vop = sheet.fromTimeline((int) vop);

        YassTable table = tables[n];
        table.removeAllRows();
        table.setText(createTable(real));
        table.getColumnModel().getColumn(0).setMinWidth(10);
        table.getColumnModel().getColumn(0).setMaxWidth(10);
        table.getColumnModel().getColumn(1).setMinWidth(30);
        table.getColumnModel().getColumn(1).setMaxWidth(30);
        table.getColumnModel().getColumn(2).setMinWidth(20);
        table.getColumnModel().getColumn(2).setMaxWidth(20);
        table.getColumnModel().getColumn(3).setMinWidth(30);
        table.getColumnModel().getColumn(3).setMaxWidth(30);
        table.setEnabled(false);

        if (tablePanel.getComponentCount() > 0) {
            tablePanel.remove(0);
        }
        tablePanel.add("Center", table);
        tablePanel.validate();

        realSelected = real;
        selected = n;

        sheet.setZoom(20 * 60 / bpm);
        ;
        sheet.setActiveTable(table);

        sheet.setZoom(20 * 60 / bpm);
        // bug: zoom gets lost in removeAllRows

        double newGap = table.getGap();
        int p = (int) Math.max(0, op - oldGap + newGap);
        int vp = (int) Math.max(0, vop - oldGap + newGap);

        vp = sheet.toTimeline(vp);
        p = sheet.toTimeline(p);
        sheet.setPlayerPosition(p);
        ((JViewport) sheet.getParent()).setViewPosition(new Point(vp, 0));
        sheet.repaint();

        if (isPlaying) {
            play();
        }
    }

    /**
     * Description of the Method
     */
    public void play() {
        try {
            if (!synthesizer.isOpen()) {
                synthesizer.open();
            }
            if (!sequencer.isOpen()) {
                sequencer.open();
            }
            if (sequencer.isRunning()) {
                sequencer.stop();
                try {
                    // note off to all channels; quick hack
                    Receiver r = sequencer.getReceiver();
                    ShortMessage offMessage = new ShortMessage();
                    for (int nChannel = 128; nChannel <= 143; nChannel++) {
                        for (int nKey = 0; nChannel <= 127; nKey++) {
                            offMessage.setMessage(ShortMessage.NOTE_OFF,
                                    nChannel, nKey, 0);
                            r.send(offMessage, -1);
                        }
                    }
                } catch (Exception ex) {
                }
                sheet.setPlaying(false);
                start.setText(I18.get("convert_play"));
                return;
            }
            sequencer.setSequence(mySequences[selected]);
            double ms = sheet.fromTimeline(sheet.getPlayerPosition());

            // sequencer.setMicrosecondPosition( (long)(ms*1000));
            sequencer.setTickPosition(timeToTick(ms / 1000.0));

			/*
			 * System.out.println("Time: "+ms/1000); long tick =
			 * timeToTick(ms/1000.0); System.out.println("Time To Tick: "+tick);
			 * double time2 = tickToTime(tick);
			 * System.out.println("Tick To Time: "+time2);
			 */
            sequencer.start();
            sequencer.setTempoInBPM(196);
            sequencerStartTime = System.currentTimeMillis();
            sequencerOffset = (long) ms;

            sheet.repaint();
            sheet.refreshImage();
            sheet.setPlaying(true);
            start.setText(I18.get("convert_stop"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // sequencer.close();
    }

    /**
     * Description of the Method
     */
    public void load() {
        boolean isPlaying = sequencer.isRunning();
        if (isPlaying) {
            sequencer.stop();
        }

        Frame f = new Frame();
        FileDialog fd = new FileDialog(f, I18.get("convert_open_title"),
                FileDialog.LOAD);
        fd.setFilenameFilter(new FilenameFilter() {
            // won't work on Windows - thx to Sun
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".kar")
                        || name.toLowerCase().endsWith(".mid");
            }
        });
        // fd.setFile("*.kar");
        fd.setVisible(true);
        if (fd.getFile() != null) {
            filename = fd.getDirectory() + File.separator + fd.getFile();

            if (sequencer.isOpen()) {
                sequencer.close();
            }
            init();
            openGUI();
        }
        fd.dispose();
        f.dispose();
    }

    /**
     * Gets the text attribute of the YassMIDIConverter object
     *
     * @return The text value
     */
    public String getText() {
        Vector<?> tables = new Vector<>();
        int ntakes = 0;
        for (int i = 0; i < nTracks; i++) {
            if (take[i]) {
                ntakes++;
            }
        }

        if (ntakes == 0) {
            return createTable(realSelected);
        }

        String txt = null;
        for (int i = 0; i < nTracks; i++) {
            if (!take[i]) {
                continue;
            }

            if (txt == null) {
                txt = createTable(realnr[i]);
            } else {
                txt = mergeTables(txt, createTable(realnr[i]));
            }
        }
        return txt;
    }

    /**
     * Description of the Method
     */
    public void save() {
        save(filename, getText());
        close();
    }

    /**
     * Description of the Method
     */
    public void close() {
        if (sequencer.isRunning()) {
            sequencer.stop();
            sequencer.close();
        }
        synthesizer.close();

        if (renderThread != null) {
            renderThread.notInterrupted = false;
        }
        if (frame != null) {
            frame.dispose();
        }
    }

    /**
     * Description of the Method
     *
     * @param voiceTrack Description of the Parameter
     * @return Description of the Return Value
     */
    public String createTable(int voiceTrack) {
        if (trackNotes == null || trackNotes[voiceTrack] == null) {
            return null;
        }

        StringWriter buffer = new StringWriter();
        PrintWriter outputStream = new PrintWriter(buffer);

        int n = syllables == null ? 0 : syllables.size();
        int ns = trackNotes[voiceTrack].size();
        int ts = trackTimes[voiceTrack].size();
        int te = trackTimesEnd[voiceTrack].size();

        int beat = -1;

        int height = 0;

        int len = 0;

        int beatgap = -1;

        int lastbeat = -1;

        int lastheight = -1;
        double gap = -1;
        for (int i = 0; i < ts; i++) {
            lastbeat = beat + len;
            lastheight = height;

            String txt = " -";

            if (i < n) {
                txt = syllables.elementAt(i).toString();
            }
            if (i >= te || i >= ns) {
                outputStream.println(": " + lastbeat + " 1 " + lastheight + " "
                        + txt);
                beat += 2;
                len = 2;
                continue;
            }
            String time = trackTimes[voiceTrack].elementAt(i).toString();
            String note = trackNotes[voiceTrack].elementAt(i).toString();
            String timeEnd = trackTimesEnd[voiceTrack].elementAt(i).toString();

            height = Integer.parseInt(note) - 60;
            double startTick = Double.parseDouble(time);
            double endTick = Double.parseDouble(timeEnd);
            double start = tickToTime(startTick);
            double end = tickToTime(endTick);
            start = start * 4 * bpm / 60.0;
            end = end * 4 * bpm / 60.0;

            beat = (int) Math.round(start);
            len = (int) Math.round(end - start);

            if (txt.startsWith("/")) {
                if (beatgap >= 0) {
                    outputStream.println("- " + (beat - beatgap));
                }
                txt = txt.substring(1);
            }
            if (beatgap < 0) {
                beatgap = beat;
                gap = beat * 60 * 1000 / (4.0 * bpm);
                String g = gap + "";
                g = g.replace('.', ',');
                int gi = g.indexOf(',');
                if (gi >= 0 && gi + 3 < g.length()) {
                    g = g.substring(0, gi + 3);
                }
                if (g.endsWith(",00")) {
                    g = g.substring(0, g.length() - 3);
                }
                if (g.endsWith(",0")) {
                    g = g.substring(0, g.length() - 2);
                }
                String b = bpm + "";
                b = b.replace('.', ',');
                if (b.endsWith(",0")) {
                    b = b.substring(0, b.length() - 2);
                }
                outputStream.println("#TITLE:Unknown");
                outputStream.println("#ARTIST:Unknown");
                outputStream.println("#LANGUAGE:Other");
                outputStream.println("#EDITION:Other");
                outputStream.println("#GENRE:Other");
                outputStream.println("#MP3:Unknown");
                outputStream.println("#BPM: " + b);
                outputStream.println("#GAP: " + g);
            }
            beat = beat - beatgap;

            outputStream.println(": " + beat + " " + len + " " + height + " "
                    + txt);
        }
        outputStream.println("E");
        String txt = buffer.toString();

        // String separator = System.getProperty("line.separator");
        // txt = txt.replaceAll("\n", separator);
        return txt;
    }

    /**
     * Description of the Method
     *
     * @param txt1 Description of the Parameter
     * @param txt2 Description of the Parameter
     * @return Description of the Return Value
     */
    public String mergeTables(String txt1, String txt2) {
        YassProperties prop = new YassProperties();
        prop.load();
        YassRow.setValidTags(prop.getProperty("valid-tags"));
        YassRow.setValidLines(prop.getProperty("valid-lines"));

        YassTable t1 = new YassTable();
        t1.setText(txt1.toString());
        YassTable t2 = new YassTable();
        t2.setText(txt2.toString());

        double gap1 = t1.getGap();
        double gap2 = t2.getGap();

        if (gap2 < gap1) {
            double g = gap1;
            gap1 = gap2;
            gap2 = g;

            YassTable d = t1;
            t1 = t2;
            t2 = d;
        }

        int dbeat = (int) Math.round((gap2 - gap1) * 4 * bpm / (60.0 * 1000.0));

        int n1 = t1.getRowCount();
        YassTableModel tm = (YassTableModel) t1.getModel();
        Vector<YassRow> data = tm.getData();
        data.removeElementAt(n1 - 1);

        int n2 = t2.getRowCount();
        for (int i = 0; i < n2; i++) {
            YassRow r = t2.getRowAt(i);
            if (r.isComment()) {
                continue;
            }
            if (r.isNoteOrPageBreak()) {
                int beat = r.getBeatInt();
                r.setBeat(beat + dbeat);
            }
            data.addElement(r);
        }

        java.util.Collections.sort(data);
        String txt = t1.getPlainText();
        System.out.println(txt);
        return txt;
    }

    /**
     * Sets the filename attribute of the YassMIDIConverter object
     *
     * @param s The new filename value
     */
    public void setFilename(String s) {
        filename = s;
    }

    /**
     * Sets the dropTarget attribute of the YassMIDIConverter object
     *
     * @param c The new dropTarget value
     */
    public void setDropTarget(JComponent c) {
        if (dropTarget == null) {
            dropTarget = c;
        }
        new DropTarget(c, this);
    }

    /**
     * Description of the Method
     *
     * @param dropTargetDragEvent Description of the Parameter
     */
    public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
        dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    }

    /**
     * Description of the Method
     *
     * @param dropTargetEvent Description of the Parameter
     */
    public void dragExit(DropTargetEvent dropTargetEvent) {
    }

    // int debug=0, debugn=10;

    /**
     * Description of the Method
     *
     * @param dropTargetDragEvent Description of the Parameter
     */
    public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
    }

    /**
     * Description of the Method
     *
     * @param dropTargetDragEvent Description of the Parameter
     */
    public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
    }

    /**
     * Description of the Method
     *
     * @param dropTargetDropEvent Description of the Parameter
     */
    public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
        try {
            Transferable tr = dropTargetDropEvent.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dropTargetDropEvent
                        .acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

                java.util.List fileList = (java.util.List) tr
                        .getTransferData(DataFlavor.javaFileListFlavor);
                Iterator<?> iterator = fileList.iterator();
                while (iterator.hasNext()) {
                    File file = (File) iterator.next();
                    String fn = file.getAbsolutePath();

                    if (fn.endsWith(".kar") || fn.endsWith(".mid")) {
                        filename = fn;
                        if (sequencer.isOpen()) {
                            sequencer.close();
                        }
                        init();
                        openGUI();
                        dropTargetDropEvent.getDropTargetContext()
                                .dropComplete(true);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dropTargetDropEvent.dropComplete(true);
    }

    /**
     * Description of the Method
     */
    public void initInstruments() {
        instrument[0] = "Piano";
        instrument[1] = "Bright Piano";
        instrument[2] = "Electric Grand";
        instrument[3] = "Honky Tonk Piano";
        instrument[4] = "Electric Piano 1";
        instrument[5] = "Electric Piano 2";
        instrument[6] = "Harpsichord";
        instrument[7] = "Clavinet";
        instrument[8] = "Celesta";
        instrument[9] = "Glockenspiel";
        instrument[10] = "Music Box";
        instrument[11] = "Vibraphone";
        instrument[12] = "Marimba";
        instrument[13] = "Xylophone";
        instrument[14] = "Tubular Bell";
        instrument[15] = "Dulcimer";
        instrument[16] = "Hammond Organ";
        instrument[17] = "Perc Organ";
        instrument[18] = "Rock Organ";
        instrument[19] = "Church Organ";
        instrument[20] = "Reed Organ";
        instrument[21] = "Accordion";
        instrument[22] = "Harmonica";
        instrument[23] = "Tango Accordion";
        instrument[24] = "Nylon Str Guitar";
        instrument[25] = "Steel String Guitar";
        instrument[26] = "Jazz Electric Gtr";
        instrument[27] = "Clean Guitar";
        instrument[28] = "Muted Guitar";
        instrument[29] = "Overdrive Guitar";
        instrument[30] = "Distortion Guitar";
        instrument[31] = "Guitar Harmonics";
        instrument[32] = "Acoustic Bass";
        instrument[33] = "Fingered Bass";
        instrument[34] = "Picked Bass";
        instrument[35] = "Fretless Bass";
        instrument[36] = "Slap Bass 1";
        instrument[37] = "Slap Bass 2";
        instrument[38] = "Syn Bass 1";
        instrument[39] = "Syn Bass 2";
        instrument[40] = "Violin";
        instrument[41] = "Viola";
        instrument[42] = "Cello";
        instrument[43] = "Contrabass";
        instrument[44] = "Tremolo Strings";
        instrument[45] = "Pizzicato Strings";
        instrument[46] = "Orchestral Harp";
        instrument[47] = "Timpani";
        instrument[48] = "Ensemble Strings";
        instrument[49] = "Slow Strings";
        instrument[50] = "Synth Strings 1";
        instrument[51] = "Synth Strings 2";
        instrument[52] = "Choir Aahs";
        instrument[53] = "Voice Oohs";
        instrument[54] = "Syn Choir";
        instrument[55] = "Orchestra Hit";
        instrument[56] = "Trumpet";
        instrument[57] = "Trombone";
        instrument[58] = "Tuba";
        instrument[59] = "Muted Trumpet";
        instrument[60] = "French Horn";
        instrument[61] = "Brass Ensemble";
        instrument[62] = "Syn Brass 1";
        instrument[63] = "Syn Brass 2";
        instrument[64] = "Soprano Sax";
        instrument[65] = "Alto Sax";
        instrument[66] = "Tenor Sax";
        instrument[67] = "Baritone Sax";
        instrument[68] = "Oboe";
        instrument[69] = "English Horn";
        instrument[70] = "Bassoon";
        instrument[71] = "Clarinet";
        instrument[72] = "Piccolo";
        instrument[73] = "Flute";
        instrument[74] = "Recorder";
        instrument[75] = "Pan Flute";
        instrument[76] = "Bottle Blow";
        instrument[77] = "Shakuhachi";
        instrument[78] = "Whistle";
        instrument[79] = "Ocarina";
        instrument[80] = "Syn Square Wave";
        instrument[81] = "Syn Saw Wave";
        instrument[82] = "Syn Calliope";
        instrument[83] = "Syn Chiff";
        instrument[84] = "Syn Charang";
        instrument[85] = "Syn Voice";
        instrument[86] = "Syn Fifths Saw";
        instrument[87] = "Syn Brass and Lead";
        instrument[88] = "Fantasia";
        instrument[89] = "Warm Pad";
        instrument[90] = "Polysynth";
        instrument[91] = "Space Vox";
        instrument[92] = "Bowed Glass";
        instrument[93] = "Metal Pad";
        instrument[94] = "Halo Pad";
        instrument[95] = "Sweep Pad";
        instrument[96] = "Ice Rain";
        instrument[97] = "Soundtrack";
        instrument[98] = "Crystal";
        instrument[99] = "Atmosphere";
        instrument[100] = "Brightness";
        instrument[101] = "Goblins";
        instrument[102] = "Echo Drops";
        instrument[103] = "Sci Fi";
        instrument[104] = "Sitar";
        instrument[105] = "Banjo";
        instrument[106] = "Shamisen";
        instrument[107] = "Koto";
        instrument[108] = "Kalimba";
        instrument[109] = "Bag Pipe";
        instrument[110] = "Fiddle";
        instrument[111] = "Shanai";
        instrument[112] = "Tinkle Bell";
        instrument[113] = "Agogo";
        instrument[114] = "Steel Drums";
        instrument[115] = "Woodblock";
        instrument[116] = "Taiko Drum";
        instrument[117] = "Melodic Tom";
        instrument[118] = "Syn Drum";
        instrument[119] = "Reverse Cymbal";
        instrument[120] = "Guitar Fret Noise";
        instrument[121] = "Breath Noise";
        instrument[122] = "Seashore";
        instrument[123] = "Bird";
        instrument[124] = "Telephone";
        instrument[125] = "Helicopter";
        instrument[126] = "Applause";
        instrument[127] = "Gunshot";
    }

    /**
     * Description of the Method
     *
     * @param tick Description of the Parameter
     * @return Description of the Return Value
     */
    public double tickToTime(double tick) {
        double time = 0;
        double diffTime = 0;
        long changeTick = 0;
        long lastTick = 0;
        long microsecondsPerQuarterNote = 60000000L / 120;
        // default bpm = 120
        for (Enumeration<Long> tmap = tempos.elements(); tmap.hasMoreElements(); ) {
            lastTick = changeTick;
            changeTick = tmap.nextElement().longValue();
            if (tick < changeTick) {
                break;
            }

            diffTime = (changeTick - lastTick) / (double) ticksPerQuarterNote
                    * microsecondsPerQuarterNote / 1000000.0;
            // if (debug<debugn) System.out.println("  +"+ diffTime
            // +" ("+changeTick+")");
            time += diffTime;

            microsecondsPerQuarterNote = tmap.nextElement()
                    .longValue();
        }
        diffTime = (tick - lastTick) / (double) ticksPerQuarterNote
                * microsecondsPerQuarterNote / 1000000.0;
        // if (debug<debugn) System.out.println("  +"+ diffTime +" ("+tick+")");
        time += diffTime;
        // if (debug<debugn) System.out.println(tick +" --> "+ time);
        // debug++;
        return time;
    }

    /**
     * Description of the Method
     *
     * @param time Description of the Parameter
     * @return Description of the Return Value
     */
    public long timeToTick(double time) {
        double mytime = 0;
        double diffTime = 0;
        long changeTick = 0;
        long lastTick = 0;
        long microsecondsPerQuarterNote = 60000000L / 120;
        // default bpm = 120
        for (Enumeration<Long> tmap = tempos.elements(); tmap.hasMoreElements(); ) {
            lastTick = changeTick;
            changeTick = tmap.nextElement().longValue();

            diffTime = (changeTick - lastTick) / (double) ticksPerQuarterNote
                    * microsecondsPerQuarterNote / 1000000.0;
            if (mytime + diffTime >= time) {
                break;
            }
            mytime += diffTime;
            // System.out.println("  +"+ diffTime +" ("+changeTick+")");
            microsecondsPerQuarterNote = tmap.nextElement()
                    .longValue();
        }
        diffTime = time - mytime;
        long diffTick = (long) (diffTime * ticksPerQuarterNote
                / microsecondsPerQuarterNote * 1000000.0);
        // System.out.println("  +"+ diffTime + " (" + diffTick + ")");
        return lastTick + diffTick;
    }

    /**
     * Gets the maxBPM attribute of the YassMIDIConverter object
     *
     * @return The maxBPM value
     */
    public double getMaxBPM() {
        return bpm;
    }

    /**
     * Description of the Method
     *
     * @param c Description of the Parameter
     */
    public void registerActions(JComponent c) {
        c.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
                "prevBeat");
        c.getActionMap().put("prevBeat", prevBeatAction);
        c.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
                "nextBeat");
        c.getActionMap().put("nextBeat", nextBeatAction);

        c.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
                "prevTrack");
        c.getActionMap().put("prevTrack", prevTrack);
        c.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                "nextTrack");
        c.getActionMap().put("nextTrack", nextTrack);

        c.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
                "play");
        c.getActionMap().put("play", playAction);
        c.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                "play");
        c.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "play");

        c.getInputMap()
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "home");
        c.getActionMap().put("home", home);
        // c.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
        // "end");
        // c.getActionMap().put("end", end);
    }

    /**
     * Description of the Class
     *
     * @author Saruta
     */
    class BoxRenderer extends JLabel implements ListCellRenderer<Object> {
        private static final long serialVersionUID = 1189583736809373071L;

        /**
         * Constructor for the BoxRenderer object
         */
        public BoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        /**
         * Gets the listCellRendererComponent attribute of the BoxRenderer
         * object
         *
         * @param list         Description of the Parameter
         * @param value        Description of the Parameter
         * @param index        Description of the Parameter
         * @param isSelected   Description of the Parameter
         * @param cellHasFocus Description of the Parameter
         * @return The listCellRendererComponent value
         */
        public Component getListCellRendererComponent(JList<?> list,
                                                      Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            int selectedIndex = ((Integer) value).intValue();
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(colors[selectedIndex % colors.length]);
            } else {
                setBackground(list.getBackground());
                setForeground(colors[selectedIndex % colors.length]);
            }
            if (take[selectedIndex]) {
                setText("\u25ba " + labels[selectedIndex]);
            } else {
                setText("  " + labels[selectedIndex]);
            }
            return this;
        }
    }

    /**
     * Description of the Class
     *
     * @author Saruta
     */
    class RenderThread extends Thread {
        /**
         * Description of the Field
         */
        public boolean notInterrupted = true;

        /**
         * Main processing method for the RenderThread object
         */
        public void run() {
            while (notInterrupted) {
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                }
                double oldt = -1;
                if (sequencer.isRunning()) {
                    long pos = sequencer.getTickPosition();
                    long len = sequencer.getTickLength();
                    double lenms = sequencer.getMicrosecondLength() / 1000.0;

                    double bpmtempo = sequencer.getTempoInBPM();
                    float fac = sequencer.getTempoFactor();
                    // System.out.println("Tempo: "+bpmtempo + " * "+fac);

                    long ms = sequencerOffset + System.currentTimeMillis()
                            - sequencerStartTime;
                    // System.out.println("Millesecond Position: "+ms);

                    double t = ms;
                    // pos/(double)len * lenms;
                    if (t <= oldt) {
                        continue;
                    }

                    oldt = t;
                    int p = sheet.toTimeline(t);
                    // sheet.scrollRectToVisible(new Rectangle(p,0,p,100));

                    // /*
                    Graphics2D g2 = (Graphics2D) sheet.getGraphics();
                    sheet.setPlayerPosition(p);

                    Rectangle clip = sheet.getClipBounds();
                    if (p < clip.x || p > clip.x + clip.width) {
                        ((JViewport) sheet.getParent())
                                .setViewPosition(new Point(p, 0));
                        sheet.refreshImage();
                    }

                    Graphics2D gb = sheet.getBackBuffer()
                            .createGraphics();
                    gb.drawImage(sheet.getPlainBuffer(), 0, 0, null);
                    if (sheet.getPlainBuffer().contentsLost()) {
                        sheet.setErrorMessage(I18.get("sheet_msg_buffer_lost"));
                    }
                    sheet.paintText(gb);
                    sheet.paintPlayerPosition(gb);

                    gb.dispose();

                    sheet.paintBackBuffer(g2);
                    // */
                    // sheet.repaint();
                }
            }

        }
    }

    /**
     * Description of the Class
     *
     * @author Saruta
     */
    class TrackListener implements ActionListener {
        int n = 0, real = 0;

        /**
         * Constructor for the TrackListener object
         *
         * @param n Description of the Parameter
         * @param r Description of the Parameter
         */
        public TrackListener(int n, int r) {
            this.n = n;
            real = r;
        }

        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            updateGUI();
        }
    }
}
