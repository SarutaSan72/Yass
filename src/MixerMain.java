import javax.sound.sampled.*;

public class MixerMain {
    public static void main(String[] argv) {
        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        if (mixerInfo.length == 0)
            System.out.println(" Error: No mixers available");
        else
        for (Mixer.Info mi : mixerInfo) {
            Mixer mixer = AudioSystem.getMixer(mi);
            // e.g. com.sun.media.sound.DirectAudioDevice
            System.out.println("Mixer: " + mixer.getClass().getName());
            Line.Info[] lis = mixer.getSourceLineInfo();
            for (Line.Info li : lis) {
                System.out.println("    Source line: " + li.toString());
                showFormats(li);
            }
            lis = mixer.getTargetLineInfo();
            for (Line.Info li : lis) {
                System.out.println("    Target line: " + li.toString());
                showFormats(li);
            }
            Control[] cs = mixer.getControls();
            for (Control c : cs) {
                System.out.println("    Control: " + c.toString());
            }
        }
    }
    private static void showFormats(Line.Info li) {
        if (li instanceof DataLine.Info) {
            AudioFormat[] afs = ((DataLine.Info) li).getFormats();
            for (AudioFormat af : afs) {
                System.out.println("        " + af.toString());
            }
        }
    }
}