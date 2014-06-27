package yass;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import javax.sound.midi.SysexMessage;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import javazoom.spi.vorbis.sampled.file.VorbisFileFormatType;

import org.tritonus.share.sampled.file.TAudioFileFormat;

import yass.renderer.YassNote;
import yass.renderer.YassPlaybackRenderer;
import yass.renderer.YassPlayerNote;
import yass.renderer.YassSession;
import yass.screen.YassScreen;

/**
 * Description of the Class
 * 
 * @author Saruta
 * @created 4. September 2006
 */
public class YassPlayer {
	public static final boolean DEBUG = false;

	private YassPlaybackRenderer playbackRenderer;
	private YassMIDI midi;
	private YassVideo video = null;
	// private Clip click;
	private byte midis[][];
	private long duration = 0, position = -1, seekInOffset = 0,
			seekOutOffset = 0;
	private boolean isPlaying = false;
	private String filename = null;

	private YassCaptureAudio capture = null;
	private boolean useCapture = false;

	private AdvancedPlayer advancedPlayer = null;
	private PlayThread player = null;
	private String cachedMP3 = "";

	private float fps = 0;

	byte[] memcache = null;
	byte[] clickmemcache = null;

	private boolean useWav = true;
	private boolean ogg = false;

	private boolean createWaveform = false;

	private boolean demo = false;

	private Vector<String> devices = new Vector<>(YassScreen.MAX_PLAYERS);
	private int[] playerdevice = new int[YassScreen.MAX_PLAYERS];
	private int[] playerchannel = new int[YassScreen.MAX_PLAYERS];
	private YassPlayerNote[] playernote = new YassPlayerNote[YassScreen.MAX_PLAYERS * 2];

	/**
	 * Sets the capture attribute of the YassPlayer object
	 * 
	 * @param device
	 *            The new capture value
	 * @param channel
	 *            The new capture value
	 * @param t
	 *            The new capture value
	 */
	public void setCapture(int t, String device, int channel) {
		System.out.println("player " + t + " " + device + " (" + channel + ")");
		if (device == null) {
			playerdevice[t] = -1;
			playerchannel[t] = channel;
			return;
		}
		if (!devices.contains(device)) {
			devices.addElement(device);
		}
		int index = devices.indexOf(device);
		playerdevice[t] = index;
		playerchannel[t] = channel;
	}

	/**
	 * Sets the video attribute of the YassPlayer object
	 * 
	 * @param v
	 *            The new video value
	 */
	public void setVideo(YassVideo v) {
		video = v;
	}

	/**
	 * Sets the demo attribute of the YassPlayer object
	 * 
	 * @param onoff
	 *            The new demo value
	 */
	public void setDemo(boolean onoff) {
		demo = onoff;
	}

	/**
	 * Sets the backgroundImage attribute of the YassPlayer object
	 * 
	 * @param img
	 *            The new backgroundImage value
	 */
	public void setBackgroundImage(BufferedImage img) {
		bgImage = img;
	}

	private BufferedImage bgImage = null;

	/**
	 * Description of the Method
	 * 
	 * @param onoff
	 *            Description of the Parameter
	 */
	public void createWaveform(boolean onoff) {
		createWaveform = onoff;
		if (createWaveform != onoff) {
			openMP3(filename);
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public boolean createWaveform() {
		return createWaveform;
	}

	/**
	 * Sets the playbackRenderer attribute of the YassPlayer object
	 * 
	 * @param s
	 *            The new playbackRenderer value
	 */
	public void setPlaybackRenderer(YassPlaybackRenderer s) {
		playbackRenderer = s;
	}

	/**
	 * Gets the playbackRenderer attribute of the YassPlayer object
	 * 
	 * @return The playbackRenderer value
	 */
	public YassPlaybackRenderer getPlaybackRenderer() {
		return playbackRenderer;
	}

	/**
	 * Constructor for the YassPlayer object
	 * 
	 * @param s
	 *            Description of the Parameter
	 */
	public YassPlayer(YassPlaybackRenderer s) {
		playbackRenderer = s;

		// Mixer defaultMixer = AudioSystem.getMixer(null);
		// Mixer.Info mixerInfo = defaultMixer.getMixerInfo();
		// System.out.println("Default Mixer: \"" + mixerInfo.getName() + "\"");
		// System.out.println("    Description: " + mixerInfo.getDescription());
		// System.out.println("    SourceLineInfo (e.g., speakers):");
		// printLineInfo("        ", defaultMixer.getSourceLineInfo());
		// System.exit(0);

		// supported:
		// PCM_SIGNED unknown sample rate, 16 bit, mono, 2 bytes/frame,
		// little-endian
		// PCM_SIGNED unknown sample rate, 16 bit, mono, 2 bytes/frame,
		// big-endian
		// PCM_SIGNED unknown sample rate, 16 bit, stereo, 4 bytes/frame,
		// little-endian
		// PCM_SIGNED unknown sample rate, 16 bit, stereo, 4 bytes/frame,
		// big-endian

		// printMixers();

		midi = new YassMIDI();

		Thread synth = new Thread() {
			public void run() {
				midis = new byte[128][];
				for (int i = 0; i < 128; i++) {
					// byte[] data = YassSynth.createRect(2);
					midis[i] = YassSynth.create(i, 15, YassSynth.SINE);
				}

				YassSynth.loadWav();
			}
		};

		// click = null;
		// if (clickEnabled) {
		// click = loadClip("/samples/406__TicTacShutUp__click_1_d_long.wav");
		// }

		// if (false) {
		// try {
		// FileInputStream clickfi = new
		// FileInputStream("/samples/406__TicTacShutUp__click_1_d_orig.wav");
		// BufferedInputStream clickbfi = new BufferedInputStream(clickfi,
		// 4096);
		// ByteArrayOutputStream bout = new ByteArrayOutputStream();
		// int readP;
		// byte[] bufferP = new byte[1024];
		// while ((readP = clickbfi.read(bufferP)) > -1) {
		// bout.write(bufferP, 0, readP);
		// }
		// clickmemcache = bout.toByteArray();
		// clickbfi.close();
		// bout.close();
		//
		// AudioInputStream stream =
		// AudioSystem.getAudioInputStream(this.getClass().getResource("/samples/406__TicTacShutUp__click_1_d_orig.wav"));
		// AudioFormat af = stream.getFormat();
		//
		// InputStream bis = new ByteArrayInputStream(clickmemcache);
		// AudioInputStream din = new AudioInputStream(bis, af,
		// clickmemcache.length / af.getFrameSize());
		// DataLine.Info info = new DataLine.Info(Clip.class, af, ((int)
		// din.getFrameLength() * af.getFrameSize()));
		// click = (Clip) AudioSystem.getLine(info);
		//
		// click.open(din);
		// }
		// catch (Exception e) {
		// e.printStackTrace();
		// }
		// }

		initCapture();

		/*
		 * ByteArrayInputStream clickbin = new
		 * ByteArrayInputStream(clickmemcache); clickplayer = new
		 * AdvancedPlayer(clickbin); clickplayer.setVolume(1); } catch
		 * (Exception e) { e.printStackTrace(); }
		 */
		synth.start();
	}

	/**
	 * Description of the Method
	 */
	public void initCapture() {
		capture = new YassCaptureAudio();
	}

	/**
	 * Sets the capture attribute of the YassPlayer object
	 * 
	 * @param onoff
	 *            The new capture value
	 */
	public void setCapture(boolean onoff) {
		useCapture = onoff;
	}

	public boolean isCapture() {
		return useCapture;
	}

	/**
	 * Description of the Method
	 */
	public void printMixers() {
		Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		for (int i = 0; i < mixerInfo.length; i++) {
			// Mixer mixer = AudioSystem.getMixer(mixerInfo[i]);

			System.out.println("Mixer[" + i + "]: \"" + mixerInfo[i].getName()
					+ "\"");
			// System.out.println("    Description: " +
			// mixerInfo[i].getDescription());
			// System.out.println("    SourceLineInfo (e.g., speakers):");
			// printLineInfo("        ", mixer.getSourceLineInfo());

			// System.out.println("    TargetLineInfo (e.g., microphones):");
			// printLineInfo("        ", mixer.getTargetLineInfo());
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @param indent
	 *            Description of the Parameter
	 * @param lineInfo
	 *            Description of the Parameter
	 */
	public void printLineInfo(String indent, Line.Info[] lineInfo) {
		int numDumped = 0;

		if (lineInfo != null) {
			for (int i = 0; i < lineInfo.length; i++) {
				if (lineInfo[i] instanceof DataLine.Info) {
					AudioFormat[] formats = ((DataLine.Info) lineInfo[i])
							.getFormats();
					for (int j = 0; j < formats.length; j++) {
						System.out.println(indent + formats[j]);
					}
					numDumped++;
				} else if (lineInfo[i] instanceof Port.Info) {
					System.out.println(indent + lineInfo[i]);
					numDumped++;
				}
			}
		}
		if (numDumped == 0) {
			System.out.println(indent + "none");
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public boolean useWav() {
		return useWav;
	}

	/**
	 * Description of the Method
	 * 
	 * @param onoff
	 *            Description of the Parameter
	 */
	public void useWav(boolean onoff) {
		useWav = onoff;
	}

	AdvancedPlayer clickplayer = null;

	/**
	 * Sets the seekOffset attribute of the YassPlayer object
	 * 
	 * @param in
	 *            The new seekOffset value
	 * @param out
	 *            The new seekOffset value
	 */
	public void setSeekOffset(long in, long out) {
		seekInOffset = in;
		seekOutOffset = out;
	}

	/**
	 * Sets the seekInOffset attribute of the YassPlayer object
	 * 
	 * @param in
	 *            The new seekInOffset value
	 */
	public void setSeekInOffset(long in) {
		seekInOffset = in;
	}

	/**
	 * Sets the seekOutOffset attribute of the YassPlayer object
	 * 
	 * @param out
	 *            The new seekOutOffset value
	 */
	public void setSeekOutOffset(long out) {
		seekOutOffset = out;
	}

	/**
	 * Gets the seekOffset attribute of the YassPlayer object
	 * 
	 * @return The seekOffset value
	 */
	public long getSeekInOffset() {
		return seekInOffset;
	}

	/**
	 * Gets the seekOutOffset attribute of the YassPlayer object
	 * 
	 * @return The seekOutOffset value
	 */
	public long getSeekOutOffset() {
		return seekOutOffset;
	}

	/**
	 * Sets the volume attribute of the YassPlayer object
	 * 
	 * @param c
	 *            The new volume value
	 * @param val
	 *            The new volume value
	 */
	// private void setVolume(DataLine c, float val) {
	// FloatControl gainControl = (FloatControl)
	// c.getControl(FloatControl.Type.MASTER_GAIN);
	// if (gainControl != null) {
	// float min = gainControl.getMinimum();
	// float max = gainControl.getMaximum();
	// double minGainDB = min;
	// double ampGainDB = ((10.0f / 20.0f) * max) - min;
	// double cste = Math.log(10.0) / 20;
	// double valueDB = minGainDB + (1 / cste) * Math.log(1 + (Math.exp(cste *
	// ampGainDB) - 1) * val);
	// gainControl.setValue((float) valueDB);
	// }
	// }

	/**
	 * Description of the Method
	 * 
	 * @param fnm
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	// private Clip loadClip(String fnm) {
	// Clip clip = null;
	// try {
	//
	// AudioInputStream stream =
	// AudioSystem.getAudioInputStream(this.getClass().getResource(fnm));
	// AudioFormat format = stream.getFormat();
	//
	// DataLine.Info info = new DataLine.Info(Clip.class, format);
	// if (!AudioSystem.isLineSupported(info)) {
	// System.out.println("Unsupported Clip File: " + fnm);
	// }
	//
	// clip = (Clip) AudioSystem.getLine(info);
	// clip.addLineListener(
	// new LineListener() {
	// public void update(LineEvent lineEvent) {
	// if (lineEvent.getType() == LineEvent.Type.STOP) {
	// //((Clip)(lineEvent.getLine())).stop();
	// //((Clip)(lineEvent.getLine())).setFramePosition(0);
	// }
	// }
	// });
	// clip.open(stream);
	// stream.close();
	// }
	// catch (Exception e) {
	// System.out.println("Unknown Audio Format: " + fnm);
	// playbackRenderer.setErrorMessage(I18.get("sheet_msg_audio_format"));
	// e.printStackTrace();
	// }
	// return clip;
	// }

	/**
	 * Description of the Method
	 * 
	 * @param filename
	 *            Description of the Parameter
	 */
	public void openMP3(String filename) {
		this.filename = filename;
		if (filename == null) {
			return;
		}

		File file = new File(filename);
		if (!file.exists()) {
			playbackRenderer
					.setErrorMessage(I18.get("sheet_msg_audio_missing"));
			return;
		}

		fps = -1;
		AudioInputStream in = null;
		ogg = false;
		try {

			AudioFileFormat aff = AudioSystem.getAudioFileFormat(file);
			if (aff.getType() == VorbisFileFormatType.OGG) {
				System.err.println("Audio Type OGG is not supported");
				ogg = true;
				playbackRenderer
						.setErrorMessage(I18.get("sheet_msg_audio_ogg"));
			} else {
				playbackRenderer.setErrorMessage(null);
			}
			// System.out.println("Audio Type : " + aff.getType());

			in = AudioSystem.getAudioInputStream(file);
			if (in != null) {
				AudioFormat baseFormat = in.getFormat();
				// System.out.println("Source Format : " +
				// baseFormat.toString());

				if ((baseFormat.getEncoding() == AudioFormat.Encoding.ULAW)
						|| (baseFormat.getEncoding() == AudioFormat.Encoding.ALAW)) {
					// System.out.println("Format: ULAW/ALAW");
					/*
					 * AudioFormat tmp = new AudioFormat(
					 * AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(),
					 * format.getSampleSizeInBits() * 2, format.getChannels(),
					 * format.getFrameSize() * 2, format.getFrameRate(), true);
					 * stream = AudioSystem.getAudioInputStream(tmp, stream);
					 * format = tmp;
					 */
				} else {
					// System.out.println("Format: Not ULAW/ALAW, fine!");
				}

				fps = baseFormat.getFrameRate();
				// System.out.println("fps: " + fps);
			}
		} catch (Exception e) {
			String s = e.getMessage();
			if (s == null || !s.equals("Resetting to invalid mark")) {
				e.printStackTrace();
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
		try {
			AudioFileFormat baseFileFormat = AudioSystem
					.getAudioFileFormat(file);
			if (baseFileFormat instanceof TAudioFileFormat) {
				Map<?, ?> properties = ((TAudioFileFormat) baseFileFormat)
						.properties();
				String key = "author";
				// String val = (String) properties.get(key);
				// System.out.println(key + ": " + val);

				key = "title";
				// val = (String) properties.get(key);
				// System.out.println(key + ": " + val);

				// key = "mp3.id3tag.genre";
				// val = (String) properties.get(key);
				// System.out.println(key + ": " + val);

				// key = "mp3.vbr";
				// val = ((Boolean) properties.get(key)).toString();
				// System.out.println(key + ": " + val);

				key = "duration";
				duration = ((Long) properties.get(key)).longValue();
				// System.out.println(key + ": " + duration);

				// key = "mp3.id3tag.v2";
				// InputStream tag = (InputStream) properties.get(key);
				// key = "mp3.header.pos";
				// headerpos = ((Integer) properties.get(key)).intValue();

				// if (ogg) {
				// Integer bpsi = (Integer)
				// properties.get("ogg.bitrate.nominal.bps");
				// if (bpsi != null) {
				// bps = bpsi.intValue();
				// }
				// //System.out.println("BPS: " + bps);
				// }
				if (!ogg && fps < 0) {
					Float fpsf = (Float) properties.get("mp3.framerate.fps");
					if (fpsf != null) {
						fps = fpsf.floatValue();
					}
					// System.out.println("id3 fps: " + fps);
				}

				// if (!ogg) {
				// Integer leni = (Integer) properties.get("mp3.length.frames");
				// if (leni != null) {
				// lengthInFrames = leni.intValue();
				// }
				// Integer fsi = (Integer)
				// properties.get("mp3.framesize.bytes");
				// if (fsi != null) {
				// frameSizeInBytes = fsi.intValue();
				// }
				// }
				// System.out.println("FPS: " + fps);
			}
			/*
			 * try { MpegAudioFileReader mpegAudioFileReader = new
			 * MpegAudioFileReader(); in =
			 * mpegAudioFileReader.getAudioInputStream(new
			 * FileInputStream(file)); baseFormat = in.getFormat();
			 * baseFileFormat = mpegAudioFileReader.getAudioFileFormat(in); if
			 * (baseFileFormat instanceof MpegAudioFileFormat) { Map props =
			 * ((TAudioFileFormat) baseFileFormat).properties(); Object[] obj =
			 * props.keySet().toArray(); bps =
			 * Integer.parseInt(props.get("mp3.bitrate.nominal.bps"
			 * ).toString()); fps =
			 * Float.parseFloat(props.get("mp3.framerate.fps").toString()); } }
			 * catch (Exception e) {
			 * playbackRenderer.setErrorMessage("Cannot read MPEG Audio";
			 * e.printStackTrace(); }
			 */
			/*
			 * try to prevents initial delay FileInputStream fi=new
			 * FileInputStream(new File(filename)); advancedPlayer = new
			 * AdvancedPlayer2(fi); while (advancedPlayer.skipFrame());
			 * fi.close();
			 */
		} catch (Exception e) {
			playbackRenderer.setErrorMessage(I18.get("sheet_msg_audio_format"));
			e.printStackTrace();
		}

		cacheMP3();

		if (createWaveform) {
			createWaveForm(in);
		}
		/*
		 * } catch (Exception e) { if (e instanceof IOException &&
		 * e.getMessage().equals("Resetting to invalid mark"))
		 * playbackRenderer.setErrorMessage
		 * ("Oversized MP3 ID3v2 Header (please remove)"); else
		 * playbackRenderer.
		 * setErrorMessage("Audio Read Error: "+e.getMessage()); }
		 */
	}

	/**
	 * Description of the Method
	 */
	public void cacheMP3() {
		if (!filename.equals(cachedMP3)) {
			FileInputStream fi = null;
			BufferedInputStream bfi = null;
			ByteArrayOutputStream bout = null;
			try {
				fi = new FileInputStream(new File(filename));
				bfi = new BufferedInputStream(fi, 4096);

				bout = new ByteArrayOutputStream();
				// Fast buffer implementation
				// BufferInputStream and OutputStream is much slower
				int readP;
				byte[] bufferP = new byte[1024];
				while ((readP = bfi.read(bufferP)) > -1) {
					bout.write(bufferP, 0, readP);
				}
				memcache = bout.toByteArray();
				cachedMP3 = filename;
				System.out.println("MP3 cached.");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (bout != null) {
					try {
						bout.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (bfi != null) {
					try {
						bfi.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (fi != null) {
					try {
						fi.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/*
	 * class AdvancedPlayer2 extends AdvancedPlayer { public
	 * AdvancedPlayer2(InputStream is) throws JavaLayerException { super(is); }
	 * public boolean skipFrame() throws JavaLayerException { return
	 * super.skipFrame(); } }
	 */
	/**
	 * Gets the duration attribute of the YassPlayer object
	 * 
	 * @return The duration value
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Description of the Method
	 * 
	 * @param in
	 *            Description of the Parameter
	 * @param out
	 *            Description of the Parameter
	 * @param clicks
	 *            Description of the Parameter
	 */
	public void playSelection(long in, long out, long clicks[][]) {
		playSelection(in, out, clicks, 1);
	}

	/**
	 * Description of the Method
	 * 
	 * @param clicks
	 *            Description of the Parameter
	 */
	public void playAll(long clicks[][]) {
		playSelection(0, -1, clicks);
	}

	/**
	 * Description of the Method
	 * 
	 * @param clicks
	 *            Description of the Parameter
	 * @param timebase
	 *            Description of the Parameter
	 */
	public void playAll(long clicks[][], int timebase) {
		playSelection(0, -1, clicks, timebase);
	}

	/**
	 * Description of the Method
	 * 
	 * @param in
	 *            Description of the Parameter
	 * @param out
	 *            Description of the Parameter
	 * @param clicks
	 *            Description of the Parameter
	 * @param timebase
	 *            Description of the Parameter
	 */
	public void playSelection(long in, long out, long clicks[][], int timebase) {
		if (filename == null) {
			return;
		}

		interruptMP3();

		player = new PlayThread(in, out, clicks, timebase);
		player.start();
	}

	/**
	 * Description of the Method
	 */
	public void interruptMP3() {

//		if (player != null) {
//			System.out.println("interrupt started:" + player.started
//					+ " finished:" + player.finished);
//		} else {
//			System.out.println("interrupt player==null");
//		}
		
		// new Exception("interrupt").printStackTrace();

		if (player != null && player.started) {
			player.notInterrupted = false;
			if (hasPlaybackRenderer) {
				playbackRenderer.setPlaybackInterrupted(true);
			}
		}
		while (player != null && player.started && !player.finished) {
			try {
				player.notInterrupted = false;
				if (hasPlaybackRenderer) {
					playbackRenderer.setPlaybackInterrupted(true);
				}
			} catch (Exception e) {
			}

			if (!player.finished) {
				try {
					Thread.currentThread();
					Thread.sleep(10);
				} catch (Exception e) {
				}
				// System.out.println("waiting for finished " + player.started +
				// " " + player.finished);
			}
		}
	}

	boolean midiEnabled = false, midisEnabled = false;

	/**
	 * Sets the mIDIEnabled attribute of the YassPlayer object
	 * 
	 * @param onoff
	 *            The new mIDIEnabled value
	 */
	public void setMIDIEnabled(boolean onoff) {
		midiEnabled = onoff;
		midisEnabled = !onoff;

		// ENABLE CLICK SAMPLE INSTEAD GENERATED SINES
		// midisEnabled = false;
		// clickEnabled = !onoff;
	}

	boolean playAudio = true;
	boolean playClicks = true;

	/**
	 * Sets the audioEnabled attribute of the YassPlayer object
	 * 
	 * @param onoff
	 *            The new audioEnabled value
	 */
	public void setAudioEnabled(boolean onoff) {
		playAudio = onoff;
	}

	/**
	 * Sets the audioEnabled attribute of the YassPlayer object
	 * 
	 * @param onoff
	 *            The new audioEnabled value
	 */
	public void setClicksEnabled(boolean onoff) {
		playClicks = onoff;
	}

	/**
	 * Description of the Class
	 * 
	 * @author Saruta
	 * @created 26. August 2007
	 */
	class Play2Thread extends Thread {
		AdvancedPlayer ap;
		int skip1, skip2;

		/**
		 * Constructor for the Play2Thread object
		 * 
		 * @param ap
		 *            Description of the Parameter
		 * @param skip1
		 *            Description of the Parameter
		 * @param skip2
		 *            Description of the Parameter
		 */
		public Play2Thread(AdvancedPlayer ap, int skip1, int skip2) {
			this.ap = ap;
			this.skip1 = skip1;
			this.skip2 = skip2;
		}

		/**
		 * Main processing method for the Play2Thread object
		 */
		public void run() {
			try {
				ap.play(skip1, skip2);
			} catch (Exception e) {
				System.out.println("Playback Error");
				e.printStackTrace();
			}
		}
	}

	boolean hasPlaybackRenderer = true;

	/**
	 * Sets the hasPlaybackRenderer attribute of the YassPlayer object
	 * 
	 * @param onoff
	 *            The new hasPlaybackRenderer value
	 */
	public void setHasPlaybackRenderer(boolean onoff) {
		hasPlaybackRenderer = onoff;
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public boolean hasPlaybackRenderer() {
		return hasPlaybackRenderer;
	}

	boolean live = false;

	/**
	 * Sets the live attribute of the YassPlayer object
	 * 
	 * @param onoff
	 *            The new live value
	 */
	public void setLive(boolean onoff) {
		live = onoff;
	}

	/**
	 * Gets the live attribute of the YassPlayer object
	 * 
	 * @return The live value
	 */
	public boolean isLive() {
		return live;
	}

	/**
	 * Description of the Class
	 * 
	 * @author Saruta
	 * @created 26. August 2007
	 */
	class PlayThread extends Thread {
		/**
		 * Description of the Field
		 */
		public boolean notInterrupted = true, finished = false,
				started = false;
		long in, out, clicks[][];
		int timebase = 1;

		/**
		 * Constructor for the PlayThread object
		 * 
		 * @param in
		 *            Description of the Parameter
		 * @param out
		 *            Description of the Parameter
		 * @param clicks
		 *            Description of the Parameter
		 * @param timebase
		 *            Description of the Parameter
		 */
		public PlayThread(long in, long out, long[][] clicks, int timebase) {
			this.in = in;
			this.out = out;
			this.clicks = clicks;
			this.timebase = timebase;
		}

		/**
		 * Main processing method for the PlayThread object
		 */
		public void run() {
			playMP3(in, out, clicks, timebase);
		}

		/**
		 * Description of the Method
		 * 
		 * @param inpoint
		 *            Description of the Parameter
		 * @param outpoint
		 *            Description of the Parameter
		 * @param clicks
		 *            Description of the Parameter
		 * @param timebase
		 *            Description of the Parameter
		 */
		private void playMP3(long inpoint, long outpoint, long[][] clicks,
				int timebase) {
			finished = false;
			started = true;

			File mp3File = new File(filename);
			if (!mp3File.exists()) {
				finished = true;
				return;
			}

			if (DEBUG) {
				System.out.println("in: " + inpoint);
				System.out.println("out: " + outpoint);
				for (int i = 0; i < clicks.length; i++) {
					long duration = clicks[i][2] - clicks[i][0];
					System.out
							.println("click[" + i + "]=" + clicks[i][1]
									+ " (at:" + clicks[i][0] + " len:"
									+ duration + ")");
				}
			}

			long duration = getDuration();
			if (outpoint < 0 || outpoint > duration) {
				outpoint = duration;
			}

			long off;

			long maxClickOffset = 0;
			int midiPitch = 0;
			int clicksPos = 0;
			int n = clicks != null ? clicks.length : 0;
			long nextClick = clicks == null ? -1 : clicks[clicksPos][0];
			long nextClickEnd = clicks == null ? -1 : clicks[clicksPos][2];

			if (DEBUG)
				System.out.println("playAudio:" + playAudio + "  ogg:" + ogg);
			if (playAudio && !ogg) {
				cacheMP3();
				ByteArrayInputStream bin = new ByteArrayInputStream(memcache);

				try {
					// System.out.println("Creating JavaZoom AdvancedPlayer...");
					advancedPlayer = new AdvancedPlayer(bin);
					advancedPlayer.setVolume(0.3);
					advancedPlayer.setTimeBase(timebase);
					advancedPlayer.setPlayBackListener(new PlaybackListener() {
						public void playbackStarted(PlaybackEvent evt) {
							isPlaying = true;
							// System.out.println("playback started");
						}

						public void playbackFinished(PlaybackEvent evt) {
							isPlaying = false;
							// System.out.println("playback finished");
						}
					});
					if (DEBUG)
						System.out.println("JavaZoom AdvancedPlayer created.");
				} catch (Exception e) {
					notInterrupted = false;
					if (DEBUG)
						System.out
								.println("Cannot create JavaZoom AdvancedPlayer.");
					e.printStackTrace();
				}
			}

			if (hasPlaybackRenderer) {
				playbackRenderer.setErrorMessage(null);

				if (useCapture) {
					int trackCount = playbackRenderer.getSession()
							.getTrackCount();
					for (int t = 0; t < trackCount; t++) {
						playbackRenderer.getSession().getTrack(t)
								.getPlayerNotes().removeAllElements();
					}
					for (Enumeration<String> devEnum = devices.elements(); devEnum
							.hasMoreElements();) {
						String device = (String) devEnum.nextElement();
						capture.startQuery(device);
					}
				}

				if (!playbackRenderer.preparePlayback(inpoint / 1000,
						outpoint / 1000)) {
					finished = true;
					return;
				}
				playbackRenderer.setPlaybackInterrupted(false);

				if (video != null && playbackRenderer.showVideo()) {
					video.setTime((int) (inpoint / 1000));
				}
				if (bgImage != null && playbackRenderer.showBackground()) {
					playbackRenderer.setBackgroundImage(bgImage);
				}
			}

			// Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 1);

			if (midisEnabled && playClicks) {
				if (useWav) {
					YassSynth.openWavLine();
				} else {
					YassSynth.openLine();
				}
			}

			firePlayerStarted();
			if (hasPlaybackRenderer) {
				playbackRenderer.setPause(false);
				playbackRenderer.startPlayback();
				if (video != null && playbackRenderer.showVideo()) {
					video.playVideo();
				}
			}
			if (playAudio && !ogg) {
				try {
					isPlaying = false;
					int skip1 = (int) Math.floor(fps * (inpoint / 1000000.0))
							+ (int) seekInOffset;
					int skip2 = (int) Math.floor(fps * (outpoint / 1000000.0))
							+ (int) seekOutOffset;
					if (skip1 < 0) {
						skip1 = 0;
					}
					if (skip2 < 0) {
						skip2 = 0;
					}
					new Play2Thread(advancedPlayer, skip1, skip2).start();
				} catch (Exception e) {
					e.printStackTrace();
				}
				int nn = 200;
				try {
					while (!isPlaying && nn-- > 0) {
						Thread.sleep(0, 100);
					}
				} catch (InterruptedException e) {
				}
				if (nn <= 0) {
					if (DEBUG)
						System.out.println("Cannot start playback.");
					notInterrupted = false;
				}
				if (DEBUG) System.out.println("Waited "+nn);
			}

			long nanoStart = System.nanoTime() / 1000L;
			position = inpoint;

			long lastms = System.nanoTime();

			if (!notInterrupted) {
				System.out.println("Playback interrupted.");
			}

			while (notInterrupted) {
				position = (System.nanoTime() / 1000L - nanoStart) / timebase
						+ inpoint;
				if (position >= outpoint) {
					position = outpoint;
					notInterrupted = false;
					if (DEBUG) System.out.println("Playback stopped.");
					break;
				}
				if (clicks != null && clicksPos < n) {
					if (position >= nextClickEnd) {
						if (midiEnabled) {
							midi.stopPlay();
						}
					}
					if (position >= nextClick) {
						off = Math.abs(position - nextClick);
						if (DEBUG)
							System.out.println(off + " us offset  at line "
									+ clicksPos);
						maxClickOffset = Math.max(maxClickOffset, off);
						midiPitch = (int) clicks[clicksPos][1];
						midiPitch += 60;
						if (midiPitch > 127) {
							midiPitch = 127;
						}

						if (playClicks && midisEnabled && n > 1) {
							int midiPitch2 = midiPitch + 12;
							if (midiPitch2 > 127) {
								midiPitch2 = 127;
							}
							if (useWav) {
								YassSynth.playWav();
							} else {
								YassSynth.play(midis[midiPitch2]);
							}
						}

						if (midiEnabled) {
							midi.stopPlay();
							midi.startPlay(midiPitch);
						}

						// if (clickEnabled) {
						// click.stop();
						// click.setFramePosition(0);
						// click.start();
						// }

						if (++clicksPos < n) {
							nextClick = clicks[clicksPos][0];
							nextClickEnd = clicks[clicksPos][2];
						}
					}
				}

				if (hasPlaybackRenderer) {
					long currentMillis = (position / 1000);
					YassSession session = playbackRenderer.getSession();
					if (session != null) {
						session.updateSession(currentMillis);

						if (demo) {
							YassPlayerNote note = new YassPlayerNote(
									YassPlayerNote.NOISE, 1, currentMillis);

							int trackCount = session.getTrackCount();
							for (int t = 0; t < trackCount; t++) {
								int currentNoteIndex = session.getTrack(t)
										.getCurrentNote();
								YassNote currentTrackNote = session.getTrack(t)
										.getNote(currentNoteIndex);
								if (currentTrackNote.getStartMillis() <= currentMillis
										&& currentMillis <= currentTrackNote
												.getEndMillis()) {
									if (currentMillis < currentTrackNote
											.getStartMillis() + 10) {
										note.setStartMillis(currentTrackNote
												.getStartMillis());
									}
									if (currentMillis > currentTrackNote
											.getEndMillis() - 10) {
										note.setEndMillis(currentTrackNote
												.getEndMillis());
									}

									int h = currentTrackNote.getHeight();
									note.setHeight(h);
								}
								session.getTrack(t).addPlayerNote(
										new YassPlayerNote(note));

							}
						} else if (useCapture) {
							int d = 0;
							for (Enumeration<String> devEnum = devices
									.elements(); devEnum.hasMoreElements();) {
								String device = (String) devEnum.nextElement();
								YassPlayerNote[] note = capture.query(device);
								playernote[d++] = note != null ? note[0] : null;
								playernote[d++] = note != null ? note[1] : null;
							}

							int trackCount = session.getTrackCount();
							for (int t = 0; t < trackCount; t++) {
								if (playerdevice[t] < 0) {
									continue;
								}
								YassPlayerNote note = playernote[playerdevice[t]
										+ playerchannel[t]];
								if (note == null) {
									continue;
								}
								note.setStartMillis(currentMillis);

								int currentNoteIndex = session.getTrack(t)
										.getCurrentNote();
								YassNote currentTrackNote = session.getTrack(t)
										.getNote(currentNoteIndex);
								if (currentTrackNote.getStartMillis() <= currentMillis
										&& currentMillis <= currentTrackNote
												.getEndMillis()) {
									if (currentMillis < currentTrackNote
											.getStartMillis() + 10) {
										note.setStartMillis(currentTrackNote
												.getStartMillis());
									}
									if (currentMillis > currentTrackNote
											.getEndMillis() - 10) {
										note.setEndMillis(currentTrackNote
												.getEndMillis());
									}
								} else {
									note.setHeight(YassPlayerNote.NOISE);
								}
								session.getTrack(t).addPlayerNote(
										new YassPlayerNote(note));
							}
						}
					}
					if (video != null && playbackRenderer.showVideo()) {
						playbackRenderer.setVideoFrame(video.getFrame());
					}
					playbackRenderer.updatePlayback(currentMillis);
				}

				try {
					long curms = System.nanoTime();
					long diff = curms - lastms;
					lastms = curms;
					int diffms = (int) (diff / 1000L);
					if (diffms < 1000) {
						Thread.currentThread();
						Thread.sleep(0, 1000 - diffms);
						// System.out.println("   wait " + (1000 - diffms));
					}
				} catch (InterruptedException e) {
					if (DEBUG)
						System.out.println("Playback renderer: interrupt.");
					notInterrupted = false;
				}
			}

			if (midiEnabled) {
				midi.stopPlay();
			}
			notInterrupted = false;

			if (playAudio && !ogg) {
				if (advancedPlayer != null && isPlaying) {
					try {
						Thread.currentThread();
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					try {
						advancedPlayer.stop();
						// System.out.println("player stop()");
					} catch (Throwable t) {
						// t.printStackTrace();
					}
				}
			}

			if (playClicks && midisEnabled) {
				if (useWav) {
					YassSynth.closeWavLine();
				} else {
					YassSynth.closeLine();
				}
			}

			if (hasPlaybackRenderer) {
				if (useCapture) {
					for (Enumeration<String> devEnum = devices.elements(); devEnum
							.hasMoreElements();) {
						String device = (String) devEnum.nextElement();
						capture.stopQuery(device);
					}
				}
				playbackRenderer.setPlaybackInterrupted(false);
				if (video != null && playbackRenderer.showVideo()) {
					video.stopVideo();
				}
				playbackRenderer.finishPlayback();

				if (clicks != null && maxClickOffset / 1000.0 > 10) {
					// greater 10 ms
					playbackRenderer.setErrorMessage(latency
							.format(new Object[] { Math
									.round(maxClickOffset / 1000.0) + "" }));
				}
			}
			live = false;

			// Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
			finished = true;
			firePlayerStopped();
		}
	}

	MessageFormat latency = new MessageFormat(I18.get("sheet_msg_latency"));
	String bufferlost = I18.get("sheet_msg_buffer_lost");

	/**
	 * Gets the playing attribute of the YassPlayer object
	 * 
	 * @return The playing value
	 */
	public boolean isPlaying() {
		return isPlaying;
	}

	/**
	 * Description of the Method
	 * 
	 * @param midiPitch
	 *            Description of the Parameter
	 */
	public void playMIDI(int midiPitch) {
		midiPitch += 60;
		if (midiPitch > 127) {
			midiPitch = 127;
		}
		midi.stopPlay();
		midi.startPlay(midiPitch);
	}

	/**
	 * Gets the position attribute of the YassPlayer object
	 * 
	 * @return The position value
	 */
	public long getPosition() {
		return position;
	}

	/**
	 * Description of the Method
	 */
	public void closeMP3() {
	}

	private Vector<YassPlayerListener> listeners = null;

	/**
	 * Adds a feature to the PlayerListener attribute of the YassPlayer object
	 * 
	 * @param p
	 *            The feature to be added to the PlayerListener attribute
	 */
	public void addPlayerListener(YassPlayerListener p) {
		if (listeners == null) {
			listeners = new Vector<>();
		}
		listeners.addElement(p);
	}

	/**
	 * Description of the Method
	 * 
	 * @param p
	 *            Description of the Parameter
	 */
	public void removePlayerListener(YassPlayerListener p) {
		if (listeners == null) {
			return;
		}
		listeners.removeElement(p);
	}

	/**
	 * Description of the Method
	 */
	public void firePlayerStarted() {
		if (listeners == null) {
			return;
		}
		for (Enumeration<YassPlayerListener> en = listeners.elements(); en
				.hasMoreElements();) {
			((YassPlayerListener) en.nextElement()).playerStarted();
		}
	}

	/**
	 * Description of the Method
	 */
	public void firePlayerStopped() {
		if (listeners == null) {
			return;
		}
		for (Enumeration<YassPlayerListener> en = listeners.elements(); en
				.hasMoreElements();) {
			((YassPlayerListener) en.nextElement()).playerStopped();
		}
	}

	/**
	 * Description of the Method
	 */
	private byte[] audioBytes;
	private AudioFormat audioBytesFormat = null;
	private int audioBytesChannels = 2;
	private float audioBytesSampleRate = 44100;
	private int audioBytesSampleSize = 2;

	/**
	 * Description of the Method
	 * 
	 * @param audioInputStream
	 *            Description of the Parameter
	 */
	public void createWaveForm(AudioInputStream audioInputStream) {
		audioBytesFormat = audioInputStream.getFormat();
		audioBytesChannels = audioBytesFormat.getChannels();
		audioBytesSampleRate = audioBytesFormat.getSampleRate();

		AudioFormat decodedFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED, audioBytesSampleRate,
				audioBytesSampleSize * 8, audioBytesChannels,
				audioBytesChannels * audioBytesSampleSize, // framesize
				audioBytesFormat.getSampleRate(), false);
		// System.out.println(decodedFormat);

		AudioInputStream decodedStream = AudioSystem.getAudioInputStream(
				decodedFormat, audioInputStream);

		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			int readP;
			byte[] bufferP = new byte[1024];
			while ((readP = decodedStream.read(bufferP)) > -1) {
				bout.write(bufferP, 0, readP);
			}
			audioBytes = bout.toByteArray();
			// System.out.println("len " + audioBytes.length);
			decodedStream.close();
			bout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the waveFormAtMillis attribute of the YassPlayer object
	 * 
	 * @param ms
	 *            Description of the Parameter
	 * @return The waveFormAtMillis value
	 */
	public int getWaveFormAtMillis(double ms) {
		// sampleSize == 16, bigEndian == false

		int i = (int) (ms * audioBytesSampleRate / 1000.0);

		if (2 * i + 1 >= audioBytes.length) {
			i = (audioBytes.length - 1) / 2;
		}
		if (i < 0) {
			i = 0;
		}

		byte low = audioBytes[2 * i];
		byte big = audioBytes[2 * i + 1];

		int data = (big << 8) | (low & 255);

		return (int) (128 * data / 32768.0);
	}

	/**
	 * Gets the waveFormAtMillis attribute of the YassPlayer object
	 * 
	 * @param ms1
	 *            Description of the Parameter
	 * @param ms2
	 *            Description of the Parameter
	 * @return The waveFormAtMillis value
	 */
	public int getWaveFormAtMillis(double ms1, double ms2) {
		// sampleSize == 16, bigEndian == false

		int i1 = (int) (ms1 * audioBytesSampleRate / 1000.0 * 2);
		int i2 = (int) (ms2 * audioBytesSampleRate / 1000.0 * 2);

		if (i1 >= audioBytes.length) {
			i1 = audioBytes.length - 2;
		}
		if (i2 >= audioBytes.length) {
			i2 = audioBytes.length - 2;
		}
		if (i1 < 0) {
			i1 = 0;
		}
		if (i2 < 0) {
			i2 = 0;
		}

		double sum = 0;
		int n = (i2 - i1) / 2;
		if (n < 1) {
			n = 1;
		}

		for (int i = i1; i <= i2; i += 2) {
			int LSB = (int) audioBytes[i];
			int MSB = (int) audioBytes[i + 1];
			int data = (MSB << 8) | (255 & LSB);
			sum += Math.abs(data / n);
		}
		return (int) (128 * sum / 32768.0);
	}

	public boolean isClicksEnabled() {
		return playClicks;
	}

	public YassCaptureAudio getCapture() {
		return capture;
	}
}
