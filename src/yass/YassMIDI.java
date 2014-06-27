package yass;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

/**
 * Description of the Class
 * 
 * @author Saruta
 * @created 28. April 2009
 */
public class YassMIDI {
	Synthesizer synth = null;
	MidiChannel[] mc = null;

	/**
	 * Constructor for the YassMIDI object
	 */
	public YassMIDI() {
		try {
			System.out.println("Loading Soundbank...");
			Soundbank s = MidiSystem.getSoundbank(getClass().getResource(
					"/yass/AJH_Piano.sf2"));

			// Soundbank s = synth.getDefaultSoundbank();
			Instrument[] instr = s.getInstruments();

			/*
			 * System.out.println(instr.length+" instruments"); for (int i=1;
			 * i<instr.length; i++) {
			 * System.out.print(i+" "+instr[i].getName().trim()+" ");
			 * synth.loadInstrument(instr[i]); mc[4].programChange(i);
			 * mc[4].setMute(false); mc[4].noteOn(65,100); try {
			 * Thread.currentThread().sleep(500); } catch (InterruptedException
			 * e) {} mc[4].setMute(true); try {
			 * Thread.currentThread().sleep(1000); } catch (InterruptedException
			 * e) {} }
			 */

			int n = 0;// default jdk soundbank.gm 1 piano 56 trumpet

			synth = MidiSystem.getSynthesizer();
			synth.open();

			synth.loadInstrument(instr[n]);

			/*
			 * Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
			 * System.out.println("Available mixers:"); for(int cnt = 0; cnt <
			 * mixerInfo.length; cnt++){
			 * System.out.println(mixerInfo[cnt].getName());
			 * /mixer.isSynchronizationSupported(new Line[] {clip, clip}, true);
			 * }
			 */
			mc = synth.getChannels();

			mc[4].programChange(n);
			mc[4].controlChange(7, 127);
			System.out.println("Soundbank ready.");

//			for (int i = 0; i < 127; i++) {
//				startPlay(i);
//				System.out.println(i);
//				try {
//					Thread.sleep(600);
//				} catch (InterruptedException e) {
//				}
//				stopPlay();
//			}

		} catch (Exception e) {
			System.err.println("Error: Missing Soundbank.");
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Gets the latency attribute of the YassMIDI object
	 * 
	 * @return The latency value
	 */
	public long getLatency() {
		return synth.getLatency();
	}

	/**
	 * Description of the Method
	 * 
	 * @param n
	 *            Description of the Parameter
	 */
	public synchronized void startPlay(int n) {
		mc[4].setMute(false);
		mc[4].noteOn(n, 127);
	}

	/**
	 * Description of the Method
	 */
	public synchronized void stopPlay() {
		mc[4].setMute(true);
	}

	/**
	 * Description of the Method
	 */
	public void close() {
		if (synth == null) {
			return;
		}
		synth.close();
	}

	/**
	 * Description of the Method
	 * 
	 * @param argv
	 *            Description of the Parameter
	 */
	public static void main(String argv[]) {
		YassMIDI m = new YassMIDI();
	}
}
