package yass;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 9. Juli 2009
 */
public class YassAudioSample {
    private static int channels = 1;
    private static int bytesPerSample = 1;
    private static int sampleRate = 44100;

    private static AudioFormat audioFormat = null;
    private static AudioFormat sampleAudioFormat = null;
    private static SourceDataLine dataLine = null;

    private static Vector<Byte> buffer = null;
    private static int BUFFER_SIZE = 128000;
    /**
     * Description of the Method
     */
    private static byte[] tempBuff = new byte[BUFFER_SIZE];

    /**
     * Description of the Method
     *
     * @param args Description of the Parameter
     */
    public static void main(String args[]) {
        loadSample();

        openLine();
        playSample();
        closeLine();
    }

    /**
     * Description of the Method
     */
    public static void openLine() {
        try {
            if (audioFormat == null) {
                audioFormat = new AudioFormat(sampleRate, bytesPerSample * 8, channels, true, true);
            }
            if (sampleAudioFormat == null) {
                sampleAudioFormat = new AudioFormat(sampleRate, 2 * 8, 2, true, true);
            }
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            dataLine = (SourceDataLine) AudioSystem.getLine(info);
            dataLine.open(sampleAudioFormat);
            dataLine.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Description of the Method
     */
    public static void loadSample() {

        if (audioFormat == null) {
            audioFormat = new AudioFormat(sampleRate, bytesPerSample * 8, channels, true, true);
        }
        if (sampleAudioFormat == null) {
            sampleAudioFormat = new AudioFormat(sampleRate, 2 * 8, 2, true, true);
        }

        AudioInputStream ostream = null;
        AudioInputStream stream = null;
        try {
            ostream = AudioSystem.getAudioInputStream(new BufferedInputStream(YassSynth.class.getClass().getResource("/samples/click.wav").openStream()));
            stream = AudioSystem.getAudioInputStream(sampleAudioFormat, ostream);
            long len = stream.getFrameLength() * sampleAudioFormat.getFrameSize();

            buffer = new Vector<>((int) len);
            int bytesRead = 0;
            byte[] bufferData = new byte[BUFFER_SIZE];
            while (bytesRead != -1) {
                try {
                    bytesRead = stream.read(bufferData, 0, bufferData.length - 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (bytesRead >= 0) {
                    for (int i = 1; i <= bytesRead; i++) {
                        buffer.add(new Byte(bufferData[i]));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
                ostream.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Description of the Method
     */
    public static void closeLine() {
        try {
            dataLine.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the frame attribute of the YassSynth class
     *
     * @param frameNum Description of the Parameter
     * @return The frame value
     */
    public static byte[] getFrame(int frameNum) {
        int frameSize = sampleAudioFormat.getFrameSize();

        int framePos = (frameNum - 1) * frameSize;
        byte[] frame = new byte[frameSize];
        Byte sample = null;
        for (int i = 0; i < frameSize; i++) {
            sample = buffer.get(framePos + i + 1);
            frame[i] = sample.byteValue();
        }
        return frame;
    }

    /**
     * Gets the lengthInFrames attribute of the YassSynth class
     *
     * @return The lengthInFrames value
     */
    public static int getLengthInFrames() {
        int frameLength = audioFormat.getFrameSize();
        return buffer.size() / frameLength - 1;
    }

    /**
     * Description of the Method
     */
    public static void playSample() {
        double soundFramePos = 1;
        int frameLength = sampleAudioFormat.getFrameSize();
        int lengthInFrames = buffer.size() / frameLength - 1;

        byte[] sample = null;

        dataLine.flush();

        while (Math.floor(soundFramePos) <= lengthInFrames) {
            int bufferPos = 0;
            while ((bufferPos < BUFFER_SIZE) & (Math.floor(soundFramePos) <= lengthInFrames)) {
                sample = getFrame((int) Math.floor(soundFramePos));
                for (int i = 0; i < frameLength; i++) {
                    tempBuff[bufferPos++] = sample[i];
                }
                soundFramePos++;
            }

            if (bufferPos > 0) {
                dataLine.write(tempBuff, 0, bufferPos);
            }
        }
    }
}

