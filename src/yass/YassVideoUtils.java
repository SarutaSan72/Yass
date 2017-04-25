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

import com.sun.media.util.Registry;

import javax.media.PackageManager;
import javax.media.PlugInManager;
import javax.media.format.AudioFormat;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassVideoUtils {

    public static boolean TRY_TO_USE_FOBS = true;

    public static boolean useFOBS = false;


    /**
     * Gets the videoInited attribute of the YassVideoUtils class
     *
     * @param rendList Description of the Parameter
     * @return The videoInited value
     */
    public static boolean isVideoInited(Vector<Object> rendList) {
        String rname;
        for (int i = 0; i < rendList.size(); i++) {
            rname = (String) (rendList.elementAt(i));
            if (rname.equals("org.llama.jmf.ByteBufferRenderer")) {
                return true;
            }
        }
        return false;
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public static boolean initVideo() {
        if (!TRY_TO_USE_FOBS)
            return false;

        Vector plugInList = PlugInManager.getPlugInList(null, null, PlugInManager.RENDERER);
        if (isVideoInited(plugInList)) {
            return true;
        }

        com.sun.media.Log.isEnabled = false;
        Registry.set("secure.allowLogging", Boolean.FALSE);

        System.out.println("Looking for FOBS...");
        try {
            javax.media.Format[] ffmpegformat = new VideoFormat[]{new VideoFormat("FFMPEG_VIDEO")};
            javax.media.Format[] ffmpegdesc = new javax.media.protocol.ContentDescriptor[]{new javax.media.protocol.ContentDescriptor("video.ffmpeg")};
            javax.media.Format[] ffmpegaudio = new javax.media.Format[]{new AudioFormat("FFMPEG_AUDIO")};
            javax.media.Format[] supportedInputFormats = new VideoFormat[]{
                    new VideoFormat("iv31"),
                    //CODEC_ID_INDEO3
                    new VideoFormat("iv32"),
                    new VideoFormat("msvc"),
                    //CODEC_ID_MSVIDEO1
                    new VideoFormat("cram"),
                    new VideoFormat("wham"),
                    new VideoFormat("wmv1"),
                    //CODEC_ID_WMV1

                    new VideoFormat("wmv2"),
                    //CODEC_ID_WMV2

                    new VideoFormat("mpeg"),
                    //CODEC_ID_MPEG1VIDEO
                    new VideoFormat("mpg1"),
                    new VideoFormat("mpg2"),
                    new VideoFormat("pim1"),
                    new VideoFormat("vcr2"),
                    new VideoFormat("mjpa"),
                    //CODEC_ID_MJPEG
                    new VideoFormat("mjpb"),
                    new VideoFormat("mjpg"),
                    new VideoFormat("ljpg"),
                    new VideoFormat("jpgl"),
                    new VideoFormat("avdj"),
                    new VideoFormat("svq1"),
                    //CODEC_ID_SVQ1
                    new VideoFormat("svqi"),
                    new VideoFormat("svq3"),
                    //CODEC_ID_SVQ3

                    new VideoFormat("mp4v"),
                    //CODEC_ID_MPEG4
                    new VideoFormat("divx"),
                    new VideoFormat("dx50"),
                    new VideoFormat("xvid"),
                    new VideoFormat("mp4s"),
                    new VideoFormat("m4s2"),
                    new VideoFormat("div1"),
                    new VideoFormat("blz0"),
                    new VideoFormat("ump4"),
                    new VideoFormat("h264"),
                    //CODEC_ID_H264

                    new VideoFormat("h263"),
                    //CODEC_ID_H263

                    new VideoFormat("u263"),
                    //CODEC_ID_H263P
                    new VideoFormat("viv1"),
                    new VideoFormat("i263"),
                    //CODEC_ID_i263

                    new VideoFormat("dvc"),
                    //CODEC_ID_DVVIDEO
                    new VideoFormat("dvcp"),
                    new VideoFormat("dvsd"),
                    new VideoFormat("dvhs"),
                    new VideoFormat("dvs1"),
                    new VideoFormat("dv25"),
                    new VideoFormat("vp31"),
                    //CODEC_ID_VP3

                    new VideoFormat("rpza"),
                    //CODEC_ID_RPZA

                    new VideoFormat("cvid"),
                    //CODEC_ID_CINEPAK

                    new VideoFormat("smc"),
                    //CODEC_ID_SMC

                    new VideoFormat("mp42"),
                    // CODEC_ID_MSMPEG4V2
                    new VideoFormat("div2"),
                    new VideoFormat("mpg4"),
                    // CODEC_ID_MSMPEG4V1

                    new VideoFormat("div3"),
                    // CODEC_ID_MSMPEG4V3
                    new VideoFormat("mp43"),
                    new VideoFormat("mpg3"),
                    new VideoFormat("div5"),
                    new VideoFormat("div6"),
                    new VideoFormat("div4"),
                    new VideoFormat("ap41"),
                    new VideoFormat("col1"),
                    new VideoFormat("col0")
            };
            javax.media.Format[] frgb = new VideoFormat[]{new RGBFormat()};
            javax.media.Format[] alinear = new AudioFormat[]{new AudioFormat("LINEAR")};

            PlugInManager.addPlugIn("org.llama.jmf.ByteBufferRenderer", frgb, null, PlugInManager.RENDERER);
            Vector plist = PlugInManager.getPlugInList(null, null, PlugInManager.RENDERER);
            //move the plugin to the top of the list
            Object last = plist.lastElement();
            plist.insertElementAt(last, 0);
            plist.remove(plist.lastIndexOf(last));
            PlugInManager.setPlugInList(plist, PlugInManager.RENDERER);
            // try{PlugInManager.commit();}catch(Exception e){e.printStackTrace();};
            //System.out.println("RENDERER\n" + plist.toString());

			/*
             *  PlugInManager.addPlugIn("com.omnividea.media.parser.video.Parser",
			 *  ffmpegformat,
			 *  null,
			 *  PlugInManager.DEMULTIPLEXER);
			 */
            javax.media.Format[] In;
            javax.media.Format[] Out;
            In = ffmpegdesc;
            Out = null;

            PlugInManager.addPlugIn("com.omnividea.media.parser.video.Parser",
                    In,
                    Out,
                    PlugInManager.DEMULTIPLEXER);
            plist = PlugInManager.getPlugInList(null, null, PlugInManager.DEMULTIPLEXER);
            //System.out.println("DEMULTIPLEXER\n" + plist.toString());

            In = ffmpegformat;
            Out = frgb;
            PlugInManager.addPlugIn("com.omnividea.media.codec.video.NativeDecoder",
                    In,
                    Out,
                    PlugInManager.CODEC);

            In = ffmpegaudio;
            Out = alinear;

            PlugInManager.addPlugIn("com.omnividea.media.codec.audio.NativeDecoder",
                    In,
                    Out,
                    PlugInManager.CODEC);

            //PlugInManager.addPlugIn("com.omnividea.media.codec.video.JavaDecoder",supportedInputFormats,defaultOutputFormats,PlugInManager.CODEC);
            In = supportedInputFormats;
            Out = frgb;
            PlugInManager.addPlugIn("com.omnividea.media.codec.video.JavaDecoder",
                    In,
                    Out,
                    PlugInManager.CODEC);
            plist = PlugInManager.getPlugInList(null, null, PlugInManager.CODEC);
            //System.out.println("CODECS\n" + plist.toString());

            //registre le package
            Vector packagePrefix = PackageManager.getProtocolPrefixList();
            String myPackagePrefix = new String("com.omnividea");
            packagePrefix.add(0, myPackagePrefix);
            PackageManager.setProtocolPrefixList(packagePrefix);

            System.out.println("FOBS found. Video functions activated.");
            useFOBS = true;
        } catch (Error e) {
            useFOBS = false;
            System.out.println("FOBS error. Video functions deactivated.");
            return false;
        } catch (Throwable e) {
            useFOBS = false;
            System.out.println("FOBS error. Video functions deactivated.");
            return false;
        }

        //System.out.println(PackageManager.getProtocolPrefixList().toString());

        return true;
    }
}

