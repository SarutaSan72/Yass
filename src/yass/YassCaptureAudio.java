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

import yass.renderer.YassPlayerNote;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassCaptureAudio {

    private final static float MAX_8_BITS_SIGNED = Byte.MAX_VALUE;
    private final static float MAX_8_BITS_UNSIGNED = 0xff;
    private final static float MAX_16_BITS_SIGNED = Short.MAX_VALUE;
    private final static float MAX_16_BITS_UNSIGNED = 0xffff;
    private final static int BUFFER_SIZE = 256;
    private byte[] buffer = new byte[BUFFER_SIZE];
    private static Hashtable<String, Integer> channelsHash = new Hashtable<>();
    // "0.05", "0.1", "0.15", "0.2"
    double minlevel = Double.parseDouble("0.5");
    // "1", "2", "4", "8"
    int micboost = Integer.parseInt("64");
    Hashtable<String, TargetDataLine> linesHash = new Hashtable<>();
    private Color leftColor = new Color(51, 153, 255);
    private Color rightColor = new Color(255, 51, 51);
    private double maxpitchprob = 1;
    private Vector<YassPlayerNote> notesLeft = null;
    private Vector<YassPlayerNote> notesRight = null;
    private YassAudioMonitor monitor = null;
    private boolean stopCapture = false;
    private AudioFormat audioFormat = new AudioFormat(8000, 8, 2, true, false);
    private TargetDataLine line;
    private int currentPitch = YassPlayerNote.NOISE;
    private double currentLevel = 0;
    private long currentMillis = 0;
    private int LEFT = 0;
    private int RIGHT = 1;
    private double pitchprob[] = new double[12];

    /**
     * Constructor for the YassCaptureAudio object
     */
    public YassCaptureAudio() {
    }

    /**
     * Description of the Method
     *
     * @param args Description of the Parameter
     */
    public static void main(String args[]) {
        YassCaptureAudio cap = new YassCaptureAudio();
        cap.createGUI();
        cap.startCapture(null);
    }

    /**
     * Gets the deviceNames attribute of the YassCaptureAudio object
     *
     * @return The deviceNames value
     */
    public static String[] getDeviceNames() {
        Vector<String> m = new Vector<>();
        Line.Info targetLineInfo = new Line.Info(TargetDataLine.class);
        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        for (Mixer.Info aMixerInfo : mixerInfo) {
            Mixer mixer = AudioSystem.getMixer(aMixerInfo);
            if (mixer.isLineSupported(targetLineInfo)) {
                String name = aMixerInfo.getName();
                Line.Info[] lineInfo = mixer.getTargetLineInfo();
                for (Line.Info aLineInfo : lineInfo) {
                    if (!(aLineInfo instanceof DataLine.Info)) continue;

                    AudioFormat[] formats = ((DataLine.Info) aLineInfo)
                            .getFormats();
                    for (AudioFormat format : formats) {
                        int channels = format.getChannels();
                        int sampleSizeInBits = format.getSampleSizeInBits();
                        int frameSize = format.getFrameSize();
                        boolean pcmSigned = format.getEncoding().equals(
                                AudioFormat.Encoding.PCM_SIGNED);
                        if (sampleSizeInBits == 8 && frameSize == 2
                                && pcmSigned) {
                            m.addElement(name);
                            channelsHash.put(name, new Integer(channels));
                            break;
                        }
                    }
                }
            }
        }
        return m.toArray(new String[]{});
    }

    /**
     * Gets the currentPitch attribute of the YassCaptureAudio object
     *
     * @param channel Description of the Parameter
     * @return The currentPitch value
     */
    public YassPlayerNote getCurrentNote(int channel) {
        int available = line.available();
        if (available < buffer.length) {
            return new YassPlayerNote(YassPlayerNote.NOISE, 0, 0);
        }

        line.read(buffer, 0, buffer.length);

        // "0.05", "0.1", "0.15", "0.2"
        float level = calculateLevel(channel);
        boolean noise = level < minlevel;
        int pitch = noise ? YassPlayerNote.NOISE : calculatePitch(channel);

        currentPitch = pitch;
        currentLevel = level;
        currentMillis = System.currentTimeMillis();
        return new YassPlayerNote(currentPitch, currentLevel, currentMillis);
    }

    /**
     * Gets the currentNote attribute of the YassCaptureAudio object
     *
     * @param line    Description of the Parameter
     * @param channel Description of the Parameter
     * @return The currentNote value
     */
    public YassPlayerNote getCurrentNote(TargetDataLine line, int channel) {
        int available = line.available();
        if (available < buffer.length) {
            return new YassPlayerNote(YassPlayerNote.NOISE, 0, 0);
        }

        line.read(buffer, 0, buffer.length);

        // "0.05", "0.1", "0.15", "0.2"
        float level = calculateLevel(channel);
        boolean noise = level < minlevel;
        int pitch = noise ? YassPlayerNote.NOISE : calculatePitch(channel);

        currentPitch = pitch;
        currentLevel = level;
        currentMillis = System.currentTimeMillis();
        return new YassPlayerNote(currentPitch, currentLevel, currentMillis);
    }

    /**
     * Description of the Method
     *
     * @param channel Description of the Parameter
     * @return Description of the Return Value
     */
    public int calculatePitch(int channel) {
        for (int p = 0; p < 12; p++) {
            pitchprob[p] = 0;
        }

        int maxp = -1;
        maxpitchprob = 0;
        double f = 130.81;// middle-C
        int basepitch = 0;
        double cor = 0;
        for (int p = 0; p < 36; p++) {
            // for (int p = 0; p < 12; p++) {
            basepitch = p % 12;
            // 1.05946309436 = 12th root of 2 = pitch difference between two
            // half-tones
            // multiply a note (frequency) by 1.0546309436 to get the next note

            f = 130.81 * Math.pow(1.05946309436, p) / 2;
            cor = autocorrelate(f, channel);
            pitchprob[basepitch] = Math.max(cor, pitchprob[basepitch]);
            if (cor > maxpitchprob) {
                maxpitchprob = cor;
                maxp = basepitch;
            }
            // f *= 1.05946309436;// /2.0
        }
        return maxp;
    }

    /**
     * Description of the Method
     *
     * @param f       Description of the Parameter
     * @param channel Description of the Parameter
     * @return Description of the Return Value
     */
    public double autocorrelate(double f, int channel) {
        int buffersize = buffer.length;

        double n = 0;
        int i = 0;
        int src = channel == LEFT ? 0 : 1;
        int move = (int) Math.round(8000 / f);
        if (move % 2 == 1) {
            move++;
        }

        for (int dst = src + move; dst < buffersize; i++) {
            n += Math.abs(buffer[src] - buffer[dst]) / 10000.0;
            src += 2;
            dst += 2;
        }

        return 1 - micboost * (n / (double) i);
    }

    /**
     * Description of the Method
     *
     * @param channel Description of the Parameter
     * @return Description of the Return Value
     */
    public float calculateLevel(int channel) {
        float level = 0;

        int max = 0;
        boolean use16Bit = (audioFormat.getSampleSizeInBits() == 16);
        boolean signed = (audioFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED);
        boolean bigEndian = (audioFormat.isBigEndian());
        if (use16Bit) {
            for (int i = 0; i < buffer.length; i += 2) {
                int value = 0;
                // deal with endianness
                int hiByte = (bigEndian ? buffer[i] : buffer[i + 1]);
                int loByte = (bigEndian ? buffer[i + 1] : buffer[i]);
                if (signed) {
                    short shortVal = (short) hiByte;
                    shortVal = (short) ((shortVal << 8) | (byte) loByte);
                    value = shortVal;
                } else {
                    value = (hiByte << 8) | loByte;
                }
                max = Math.max(max, value);
            }
        } else {
            // 8 bit - no endianness issues, just sign
            if (channel == LEFT) {
                for (int i = 0; i < buffer.length; i += 2) {
                    int value = 0;
                    if (signed) {
                        value = buffer[i];
                    } else {
                        short shortVal = 0;
                        shortVal = (short) (shortVal | buffer[i]);
                        value = shortVal;
                    }
                    max = Math.max(max, value);
                }
            } else {
                for (int i = 1; i < buffer.length; i += 2) {
                    int value = 0;
                    if (signed) {
                        value = buffer[i];
                    } else {
                        short shortVal = 0;
                        shortVal = (short) (shortVal | buffer[i]);
                        value = shortVal;
                    }
                    max = Math.max(max, value);
                }
            }
        }
        // 8 bit
        // express max as float of 0.0 to 1.0 of max value
        // of 8 or 16 bits (signed or unsigned)

        // max = (int) (max * Math.pow(max / MAX_8_BITS_SIGNED, 2.2));

        if (signed) {
            if (use16Bit) {
                level = (float) max / MAX_16_BITS_SIGNED;
            } else {
                level = (float) max / MAX_8_BITS_SIGNED;
            }
        } else {
            if (use16Bit) {
                level = (float) max / MAX_16_BITS_UNSIGNED;
            } else {
                level = (float) max / MAX_8_BITS_UNSIGNED;
            }
        }
        return level;
    }

    /**
     * Description of the Method
     */
    public void createGUI() {
        if (monitor == null) {
            monitor = new YassAudioMonitor();
        }

        JFrame f = new JFrame();
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add("Center", monitor);

        I18.setLanguage(null);
        f.setTitle(I18.get("mlib_mic"));
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                stopCapture();
                e.getWindow().setVisible(false);
                e.getWindow().dispose();
            }
        });

        Dimension dim = f.getToolkit().getScreenSize();
        f.setSize(600, 400);
        f.setLocation(dim.width / 2 - 300, dim.height / 2 - 200);
        f.setIconImage(new ImageIcon(YassCaptureAudio.this.getClass()
                .getResource("/yass/yass-icon-16.png")).getImage());
        f.setVisible(true);
    }

    /**
     * Gets the channels attribute of the YassCaptureAudio class
     *
     * @param name Description of the Parameter
     * @return The channels value
     */
    public int getChannels(String name) {
        return channelsHash.get(name).intValue();
    }

    /**
     * Gets the channelActive attribute of the YassCaptureAudio object
     *
     * @param name Description of the Parameter
     */
    public void startQuery(String name) {
        if (linesHash.get(name) != null) {
            return;
        }

        int micIndex = -1;
        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        for (int i = 0; i < mixerInfo.length; i++) {
            if (mixerInfo[i].getName().equals(name)) {
                micIndex = i;
                break;
            }
        }
        if (micIndex < 0) {
            return;
        }
        try {
            Mixer mixer = AudioSystem.getMixer(mixerInfo[micIndex]);
            TargetDataLine line = (TargetDataLine) mixer
                    .getLine(new DataLine.Info(TargetDataLine.class,
                            audioFormat));
            line.open(audioFormat);
            line.start();
            linesHash.put(name, line);
        } catch (Exception e) {
        }
    }

    /**
     * Description of the Method
     *
     * @param name Description of the Parameter
     */
    public void stopQuery(String name) {
        TargetDataLine line = linesHash.get(name);
        if (line == null) {
            return;
        }
        try {
            line.close();
        } catch (Exception e) {
        }
        linesHash.remove(name);
    }

    /**
     * Description of the Method
     *
     * @param name Description of the Parameter
     * @return Description of the Return Value
     */
    public YassPlayerNote[] query(String name) {
        YassPlayerNote left = new YassPlayerNote(YassPlayerNote.NOISE, 0, 0);
        YassPlayerNote right = new YassPlayerNote(YassPlayerNote.NOISE, 0, 0);

        TargetDataLine line = linesHash.get(name);
        try {
            if (line.available() < buffer.length) {
                // try {
                // Thread.currentThread().sleep(10);
                // }
                // catch (Exception e) {}
                return null;
            }
            left = getCurrentNote(line, LEFT);
            right = getCurrentNote(line, RIGHT);
        } catch (Exception e) {
        }
        return new YassPlayerNote[]{left, right};
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean openLine(String name) {
        try {
            int micIndex = -1;

            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            for (int i = 0; i < mixerInfo.length; i++) {
                if (name == null
                        && mixerInfo[i].getName().indexOf("USBMIC") >= 0) {
                    micIndex = i;
                    break;
                }
                if (mixerInfo[i].getName().equals(name)) {
                    micIndex = i;
                    break;
                }
            }
            if (micIndex < 0) {
                System.err.println("Capture device not found: " + name);
                return false;
            }
            // System.out.println(audioFormat);

            Mixer mixer = AudioSystem.getMixer(mixerInfo[micIndex]);
            line = (TargetDataLine) mixer.getLine(new DataLine.Info(
                    TargetDataLine.class, audioFormat));
            line.open(audioFormat);
            line.start();
        } catch (Exception e) {
            System.err.println("Capture failed.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean startCapture(String device) {
        boolean ok = openLine(device);
        if (!ok) {
            return false;
        }

        stopCapture = false;
        Thread captureThread = new CaptureThread();
        captureThread.start();
        return true;
    }

    /**
     * Description of the Method
     */
    public void stopCapture() {
        stopCapture = true;
        if (line != null)
            line.close();
    }

    class YassAudioMonitor extends JPanel {
        private static final long serialVersionUID = -793354580252286174L;

        /**
         * Constructor for the YassAudioMonitor object
         */
        public YassAudioMonitor() {
            super(true);
        }

        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;

            if (notesLeft == null || notesLeft.size() < 1) {
                return;
            }

            YassPlayerNote currentLeft = notesLeft
                    .lastElement();
            YassPlayerNote currentRight = notesRight
                    .lastElement();

            double levelLeft = currentLeft.getLevel();
            double levelRight = currentRight.getLevel();

            boolean noiseLeft = levelLeft < minlevel;
            boolean noiseRight = levelRight < minlevel;

            int w = getSize().width;
            int h = getSize().height;

            while (notesLeft.size() * 4 > w - 40) {
                notesLeft.remove(0);
            }
            while (notesRight.size() * 4 > w - 40) {
                notesRight.remove(0);
            }

            g.setColor(Color.white);
            g2.setStroke(new BasicStroke(1));
            g.fillRect(0, 0, w, h);

            g.setColor(Color.black);
            int hh = (int) (minlevel * h);
            g.fillRect(2, h - hh - 1, 18, hh);
            g.setColor(leftColor);
            hh = (int) (levelLeft * h);
            g.fillRect(2, h - hh, 8, hh);
            g.setColor(rightColor);
            hh = (int) (levelRight * h);
            g.fillRect(12, h - hh, 8, hh);

            g.setColor(Color.black);
            for (int i = 0; i < 12; i++) {
                int y = (int) ((i + 1) / 12.0 * (h - 10));
                g.fillRect(20, h - y - 1, w - 20, 2);
            }

            if (!noiseLeft || !noiseRight) {
                g.setColor(Color.lightGray);
                g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                if (pitchprob != null) {
                    g.setColor(Color.gray);
                    int lastx = -1;
                    int lasty = -1;
                    for (int i = 0; i < 12; i++) {
                        int y = (int) ((i + 1) / 12.0 * (h - 10));
                        int x = (int) (pitchprob[i] / maxpitchprob * (w - 30));
                        y = h - y;
                        if (i == 0) {
                            lastx = x;
                            lasty = y;
                        }
                        g.drawLine(lastx, lasty, x, y);
                        lastx = x;
                        lasty = y;
                    }
                }
            }

            int x = 20;
            int last = YassPlayerNote.NOISE;

            g.setColor(leftColor);
            int lastpy = -10;
            for (Enumeration<YassPlayerNote> en = notesLeft.elements(); en
                    .hasMoreElements(); ) {
                YassPlayerNote n = en.nextElement();
                if (n.getHeight() == YassPlayerNote.NOISE) {
                    last = YassPlayerNote.NOISE;
                    x += 4;
                    lastpy = -10;
                    continue;
                }

                int py = n.getHeight() + 1;
                int y = (int) (py / 12.0 * (h - 10));
                if (last == YassPlayerNote.NOISE || Math.abs(py - lastpy) > 4) {
                    last = y;
                }
                lastpy = py;

                int strokeWidth = Math.min(15,
                        (int) (15 * (n.getLevel() - minlevel)));
                g2.setStroke(new BasicStroke(strokeWidth,
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(x - 4, h - last, x, h - y);
                x += 4;
                last = y;
            }
            x = 20;
            last = YassPlayerNote.NOISE;
            lastpy = -10;
            g.setColor(rightColor);
            for (Enumeration<YassPlayerNote> en = notesRight.elements(); en
                    .hasMoreElements(); ) {
                YassPlayerNote n = en.nextElement();
                if (n.getHeight() == YassPlayerNote.NOISE) {
                    last = YassPlayerNote.NOISE;
                    x += 4;
                    lastpy = -10;
                    continue;
                }

                int py = n.getHeight() + 1;
                int y = (int) (py / 12.0 * (h - 10));
                if (last == YassPlayerNote.NOISE || Math.abs(py - lastpy) > 4) {
                    last = y;
                }
                lastpy = py;

                int strokeWidth = Math.min(15,
                        (int) (15 * (n.getLevel() - minlevel)));
                g2.setStroke(new BasicStroke(strokeWidth,
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(x - 4, h - last, x, h - y);
                x += 4;
                last = y;
            }
        }
    }

    class CaptureThread extends Thread {
        public void run() {
            if (monitor != null) {
                if (notesLeft == null) {
                    notesLeft = new Vector<>(4096);
                    notesRight = new Vector<>(4096);
                }
                if (notesLeft.size() < 1) {
                    for (int i = 0; i < 100; i++) {
                        notesLeft.addElement(new YassPlayerNote(
                                YassPlayerNote.NOISE, 0, 0));
                        notesRight.addElement(new YassPlayerNote(
                                YassPlayerNote.NOISE, 0, 0));
                    }
                }
            }

            try {
                while (!stopCapture) {
                    if (line.available() < buffer.length) {
                        try {
                            Thread.currentThread();
                            Thread.sleep(10);
                        } catch (Exception e) {
                        }
                        continue;
                    }
                    YassPlayerNote pnoteLeft = getCurrentNote(LEFT);
                    YassPlayerNote pnoteRight = getCurrentNote(RIGHT);
                    // System.out.println(pnoteLeft.getLevel()+"/"+pnoteRight.getLevel());

                    if (monitor != null) {
                        notesLeft.addElement(pnoteLeft);
                        notesRight.addElement(pnoteRight);
                        monitor.repaint();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
