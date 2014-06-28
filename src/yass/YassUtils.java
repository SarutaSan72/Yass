package yass;

import org.tritonus.share.sampled.file.TAudioFileFormat;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 22. August 2007
 */
public class YassUtils {

    /**
     * Description of the Field
     */
    public final static int NORTH = 0;
    /**
     * Description of the Field
     */
    public final static int SOUTH = 1;
    /**
     * Description of the Field
     */
    public final static int EAST = 2;
    /**
     * Description of the Field
     */
    public final static int WEST = 3;
    private static String msg = null;

    /**
     * Gets the songDir attribute of the YassUtils class
     *
     * @param parent Description of the Parameter
     * @param prop   Description of the Parameter
     * @return The songDir value
     */
    public static String getSongDir(Component parent, YassProperties prop) {
        return getSongDir(parent, prop, false);
    }

    /**
     * Gets the songDir attribute of the YassUtils class
     *
     * @param parent Description of the Parameter
     * @param prop   Description of the Parameter
     * @param force  Description of the Parameter
     * @return The songDir value
     */
    public static String getSongDir(Component parent, YassProperties prop, boolean force) {
        String songdir = prop.getProperty("song-directory");
        if (songdir == null || !new File(songdir).exists() || force) {
            JFileChooser chooser = new JFileChooser();
            File d = null;
            if (songdir != null) {
                d = new File(songdir);
            }
            if (d == null || !d.exists()) {
                d = new java.io.File(".");
            }
            chooser.setCurrentDirectory(d);
            chooser.setDialogTitle(I18.get("utils_songdir"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
                return null;
            }
            songdir = chooser.getSelectedFile().getAbsolutePath();
            prop.setProperty("song-directory", songdir);
            prop.store();
        }
        return songdir;
    }

    /**
     * Description of the Method
     *
     * @param songdir Description of the Parameter
     * @param title   Description of the Parameter
     * @param artist  Description of the Parameter
     * @return Description of the Return Value
     */
    public static String findSong(String songdir, String title, String artist) {
        // @deprecated      Use YassSongList.findSong instead
        artist = artist.toLowerCase().trim();
        title = title.toLowerCase().trim();

        File dFiles[] = new File(songdir).listFiles();
        for (int i = 0; i < dFiles.length; i++) {
            if (dFiles[i].isDirectory()) {
                String fd = dFiles[i].getName().toLowerCase();
                if (fd.startsWith(artist)) {
                    if (fd.indexOf(title, artist.length()) > 0) {
                        return dFiles[i].getAbsolutePath();
                    }
                }
                String dir = findSong(dFiles[i].getAbsolutePath(), title, artist);
                if (dir != null) {
                    return dir;
                }
            }
        }
        return null;
    }

    /**
     * Gets the data attribute of the YassUtils class
     *
     * @param s Description of the Parameter
     * @return The data value
     */
    public static String[] getData(String s) {
        if (s == null) {
            return null;
        }
        File f = new File(s);
        if (!f.exists()) {
            return null;
        }
        try {
            AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(f);
            if (baseFileFormat instanceof TAudioFileFormat) {
                Map<?, ?> properties = baseFileFormat.properties();
                String artist = (String) properties.get("author");
                String title = (String) properties.get("title");

                String genre = "";
                s = f.getName();
                int i = s.indexOf(" - ");
                if (i >= 0) {
                    if (artist == null || artist.trim().length() < 1) {
                        artist = s.substring(0, i).trim();
                    }
                    if (title == null || title.trim().length() < 1) {
                        title = s.substring(i + 3).trim();
                        i = title.indexOf("[");
                        if (i > 0) {
                            title = title.substring(0, i).trim();
                        }
                        i = title.lastIndexOf(".");
                        if (i > 0) {
                            title = title.substring(0, i).trim();
                        }
                    }
                }

                genre = (String) properties.get("mp3.id3tag.genre");
                if (genre == null) {
                    genre = "Unknown";
                }

                String data[] = new String[3];
                data[0] = title;
                data[1] = artist;
                data[2] = genre;
                return data;
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Description of the Method
     *
     * @param parent Description of the Parameter
     * @param vals   Description of the Parameter
     * @return Description of the Return Value
     */
    public static String createSong(JComponent parent, Hashtable<?, ?> vals, YassProperties prop) {
        String artist = (String) vals.get("artist");
        String title = (String) vals.get("title");
        String mp3filename = (String) vals.get("filename");
        String folder = (String) vals.get("folder");
        String songdir = (String) vals.get("songdir");
        String tabletxt = (String) vals.get("melodytable");
        String encoding = (String) vals.get("encoding");
        if (encoding != null && encoding.trim().length() < 1) {
            encoding = null;
        }

        if (artist == null || artist.trim().length() < 1) {
            artist = "UnknownArtist";
        }
        if (title == null || title.trim().length() < 1) {
            title = "UnknownTitle";
        }

        String at = YassSong.toFilename(artist + " - " + title);

        if (songdir == null || songdir.trim().length() < 1 || !new File(songdir).exists()) {
            JFileChooser chooser = new JFileChooser();
            File d = new java.io.File(".");
            chooser.setCurrentDirectory(d);
            chooser.setDialogTitle(I18.get("tool_prefs_songs_spec"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
                return null;
            }
            songdir = chooser.getSelectedFile().getAbsolutePath();
        }

        File dir = null;
        if (folder != null) {
            dir = new File(songdir + File.separator + folder);
            if (!dir.exists()) {
                dir.mkdir();
            }
        } else {
            dir = new File(songdir);
        }

        //new song dir; move file & create empty txt
        File newdir = new File(dir, at);
        if (newdir.exists()) {
            int ok = JOptionPane.showConfirmDialog(parent, "<html>" + MessageFormat.format(I18.get("create_error_msg_1"), newdir.getAbsolutePath()) + "</html>", I18.get("create_error_title"), JOptionPane.OK_CANCEL_OPTION);
            if (ok == JOptionPane.OK_OPTION) {
                deleteDir(newdir);
            } else {
                return null;
            }
        }

        newdir.mkdir();

        if (mp3filename != null && mp3filename.trim().length() > 0) {
            File newmp3 = new File(newdir, at + ".mp3");
            File mp3 = new File(mp3filename);
            if (!copyFile(mp3, newmp3)) {
                JOptionPane.showMessageDialog(parent, "<html>" + MessageFormat.format(I18.get("create_error_msg_1"), newdir.getAbsolutePath()) + "</html>", I18.get("create_error_title"), JOptionPane.ERROR_MESSAGE);
            }
        }

        File newtxt = new File(newdir, at + ".txt");
        YassTable table = new YassTable();
        table.init(prop);
        table.removeAllRows();
        table.setText(tabletxt);
        table.setEncoding(encoding);
        table.storeFile(newtxt.getAbsolutePath());
        return newtxt.getAbsolutePath();
    }

    /**
     * Description of the Method
     *
     * @param in  Description of the Parameter
     * @param out Description of the Parameter
     * @return Description of the Return Value
     */
    public static boolean copyFile(File in, File out) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        boolean success = false;
        try {
            fis = new FileInputStream(in);
            fos = new FileOutputStream(out);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
            success = true;
        } catch (Exception e) {
            System.out.println("Cannot copy file: " + in.getName());
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                }
            }
            success = false;
        }
        return success;
    }

    /**
     * Description of the Method
     *
     * @param parent Description of the Parameter
     * @param prop   Description of the Parameter
     * @param t      Description of the Parameter
     * @return Description of the Return Value
     */
    public static String createVersion(Component parent, YassProperties prop, YassTable t) {
        YassTableModel tm = (YassTableModel) t.getModel();
        YassRow r = tm.getCommentRow("TITLE:");
        if (r == null) {
            return null;
        }
        String title = YassSong.toFilename(r.getComment());
        String version = r.getVersion();
        String newVersion = r.hasVersion() ? version : "UPDATE";
        String artist = YassSong.toFilename(tm.getCommentRow("ARTIST:").getComment());

        String newVersion2 = newVersion;
        String absFilename = t.getDir() + File.separator + artist + " - " + title + " [" + newVersion2 + "].txt";
        File f = new File(absFilename);
        int k = 1;
        while (f.exists()) {
            newVersion2 = newVersion + k;
            absFilename = t.getDir() + File.separator + artist + " - " + title + " [" + newVersion2 + "].txt";
            f = new File(absFilename);
            k++;
        }
        r.setVersion(newVersion2);
        t.storeFile(f.getAbsolutePath());
        r.setVersion(version);
        return f.getAbsolutePath();
    }

    /**
     * Description of the Method
     *
     * @param parent Description of the Parameter
     * @param prop   Description of the Parameter
     * @param t      Description of the Parameter
     * @return Description of the Return Value
     */
    public static boolean removeVersion(Component parent, YassProperties prop, YassTable t) {
        String absFilename = t.getDir() + File.separator + t.getFilename();
        File f = new File(absFilename);

        YassTableModel tm = (YassTableModel) t.getModel();
        YassRow r = tm.getCommentRow("TITLE:");
        if (r == null) {
            return false;
        }

        if (!r.hasVersion()) {
            JOptionPane.showMessageDialog(parent, "<html>" + I18.get("tool_versions_remove_error") + "</html>", I18.get("tool_versions_remove_title"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String version = r.getVersion();
        int ok = JOptionPane.showConfirmDialog(parent, MessageFormat.format(I18.get("tool_versions_remove_msg"), version), I18.get("tool_versions_remove_title"), JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            f.delete();
            return true;
        }
        return false;
    }

    /**
     * Sets the asStandard attribute of the YassUtils class
     *
     * @param parent The new asStandard value
     * @param prop   The new asStandard value
     * @param t      The new asStandard value
     * @return Description of the Return Value
     */
    public static String setAsStandard(Component parent, YassProperties prop, YassTable t) {
        int ok = JOptionPane.showConfirmDialog(parent, "<html>" + I18.get("tool_versions_remove_all_msg"), I18.get("tool_versions_remove_all_title"), JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) {
            return null;
        }

        YassTableModel tm = (YassTableModel) t.getModel();
        YassRow r = tm.getCommentRow("TITLE:");
        YassRow r2 = tm.getCommentRow("ARTIST:");
        if (r == null || r2 == null) {
            return null;
        }
        String title = r.getComment();
        String artist = r2.getComment();

        if (r.hasVersion()) {
            r.setVersion("");
            t.setFilename(artist.trim() + " - " + title.trim() + ".txt");
            String absFilename = t.getDir() + File.separator + t.getFilename();
            t.storeFile(absFilename);
        }
        String absFilename = t.getDir() + File.separator + t.getFilename();

        File dFiles[] = new File(t.getDir()).listFiles();
        for (int i = 0; i < dFiles.length; i++) {
            if (dFiles[i].getName().toLowerCase().endsWith(".txt")) {
                YassTable tt = new YassTable();
                tt.loadFile(dFiles[i].getAbsolutePath());
                YassTableModel ttm = (YassTableModel) tt.getModel();
                YassRow tr = ttm.getCommentRow("TITLE:");
                if (tr.hasVersion()) {
                    dFiles[i].delete();
                }
            }
        }
        return absFilename;
    }

    /**
     * Gets the wildcard attribute of the YassAutoCorrect object
     *
     * @param s  Description of the Parameter
     * @param id Description of the Parameter
     * @return The wildcard value
     */
    public static String getWildcard(String s, String id) {
        s = s.toLowerCase();
        id = id.toLowerCase();

        int k = id.indexOf("*");
        if (k < 0) {
            return null;
        }

        String id2 = id.substring(k + 1);
        String id1 = id.substring(0, k);

        int n1 = s.lastIndexOf(id1);
        if (n1 < 0) {
            return null;
        }
        int n2 = s.indexOf(id2, n1 + 1);
        if (n2 < 0) {
            return null;
        }
        String res = s.substring(n1 + id1.length(), n2);
        return res;
    }

    /**
     * Gets the fileWithExtension attribute of the YassUtils object
     *
     * @param dir Description of the Parameter
     * @param id  Description of the Parameter
     * @param ext Description of the Parameter
     * @return The fileWithExtension value
     */
    public static File getFileWithExtension(String dir, String id, String ext[]) {
        // ext = ext.toLowerCase();

        String id2 = null;
        if (id != null) {
            int k = id.indexOf("*");
            if (k > 0) {
                id2 = id.substring(k + 1);
                id = id.substring(0, k);
            }
        }

        File files[] = new File(dir).listFiles();
        if (files == null) {
            return null;
        }

        for (int i = 0; i < files.length; i++) {
            String filename = files[i].getName().toLowerCase();

            for (int j = 0; j < ext.length; j++) {
                if (filename.endsWith(ext[j])) {
                    if (id == null) {
                        return files[i];
                    }

                    int idn = filename.lastIndexOf(id);
                    if (id2 != null) {
                        if (idn > 0 && filename.indexOf(id2, idn + 1) > 0) {
                            return files[i];
                        }
                    }
                    if (idn > 0) {
                        return files[i];
                    }
                }
            }
        }
        return null;
    }

    /**
     * Description of the Method
     *
     * @param t            Description of the Parameter
     * @param coverID      Description of the Parameter
     * @param backgroundID Description of the Parameter
     * @param videoID      Description of the Parameter
     * @param videodirID   Description of the Parameter
     * @return Description of the Return Value
     */
    public static boolean renameFiles(YassTable t, String coverID, String backgroundID, String videoID, String videodirID) {
        String artist = YassSong.toFilename(t.getArtist());
        String title = YassSong.toFilename(t.getTitle());

        String dir = t.getDir();
        String text = t.getFilename();
        String audio = t.getMP3();
        String version = t.getVersion();
        String cover = t.getCover();
        String background = t.getBackgroundTag();
        String video = t.getVideo();
        String videogap = new Double(t.getVideoGap()).toString().replace('.', ',');
        if (videogap.endsWith(",0")) {
            videogap = videogap.substring(0, videogap.length() - 2);
        }

        String at = artist + " - " + title;

        File dirfile = new File(dir);
        if (!dirfile.exists() || !dirfile.isDirectory()) {
            System.out.println("Error: Cannot find folder " + dir);
            return false;
        }
        String newdir = dirfile.getParent() + File.separator + at;
        if (video != null) {
            newdir = newdir + " " + videodirID;
        }

        if (!newdir.equals(dir)) {
            File newdirfile = new File(newdir);
            if (dirfile.renameTo(newdirfile)) {
                t.setDir(newdir);
                dir = newdir;
            } else {
                System.out.println("Error: Cannot rename folder " + dir);
            }
        }

        String extension = null;
        int i = 0;
        String filename = dir + File.separator + text;
        File file = new File(filename);
        if (file.exists()) {
            i = text.lastIndexOf(".");
            extension = text.substring(i);
            extension = extension.toLowerCase();

            if (version == null || version.length() < 1) {
                text = at + extension;
            } else {
                text = at + " [" + version + "]" + extension;
            }

            filename = dir + File.separator + text;
            File newfile = new File(filename);
            if (!newfile.equals(file)) {
                if (file.renameTo(newfile)) {
                    t.setFilename(text);
                } else {
                    System.out.println("Error: Cannot rename karaoke " + filename);
                }
            }
        }

        filename = dir + File.separator + cover;
        file = new File(filename);
        if (file.exists()) {
            i = cover.lastIndexOf(".");
            extension = cover.substring(i).toLowerCase();

            cover = at + " " + coverID + extension;
            filename = dir + File.separator + cover;
            File newfile = new File(filename);
            if (!newfile.equals(file)) {
                if (file.renameTo(newfile)) {
                    t.setCover(cover);
                } else {
                    System.out.println("Error: Cannot rename cover " + filename);
                }
            }
        }

        filename = dir + File.separator + background;
        file = new File(filename);
        if (file.exists()) {
            i = background.lastIndexOf(".");
            extension = background.substring(i).toLowerCase();

            background = at + " " + backgroundID + extension;
            filename = dir + File.separator + background;
            File newfile = new File(filename);
            if (!newfile.equals(file)) {
                if (file.renameTo(newfile)) {
                    t.setBackground(background);
                } else {
                    System.out.println("Error: Cannot rename background " + filename);
                }
            }
        }

        filename = dir + File.separator + audio;
        file = new File(filename);
        if (file.exists()) {
            i = audio.lastIndexOf(".");
            extension = audio.substring(i).toLowerCase();

            audio = at + extension;
            filename = dir + File.separator + audio;
            File newfile = new File(filename);
            if (!newfile.equals(file)) {
                if (file.renameTo(newfile)) {
                    t.setMP3(audio);
                } else {
                    System.out.println("Error: Cannot rename audio " + filename);
                }
            }
        }

        int n = videoID.indexOf("*");
        String videoID1 = videoID.substring(0, n);
        String videoID2 = videoID.substring(n + 1);

        filename = dir + File.separator + video;
        file = new File(filename);
        if (file.exists()) {
            i = video.lastIndexOf(".");
            extension = video.substring(i).toLowerCase();

            video = at + " " + videoID1 + videogap + videoID2 + extension;
            filename = dir + File.separator + video;
            File newfile = new File(filename);
            if (!newfile.equals(file)) {
                if (file.renameTo(newfile)) {
                    t.setVideo(video);
                } else {
                    System.out.println("Error: Cannot rename video " + filename);
                }
            }
        }
        return true;
    }

    /**
     * Gets the message attribute of the YassUtils class
     *
     * @return The message value
     */
    public static String getMessage() {
        return msg;
    }

    /**
     * Description of the Method
     *
     * @param dir Description of the Parameter
     * @return Description of the Return Value
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = false;
                int trials = 4;
                while (!success && trials-- > 0) {
                    success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        //System.out.println("cannot delete " + children[i]);
                        try {
                            Thread.currentThread();
                            Thread.sleep(100);
                        } catch (Exception e) {
                        }
                    }
                }
                if (!success) {
                    msg = MessageFormat.format(I18.get("utils_msg_remove_error"), children[i]);
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     * @throws ClassNotFoundException Description of the Exception
     */
    public static Class<?> forName(String s)
            throws ClassNotFoundException {
        Class<?> classDefinition = null;
        try {
            classDefinition = Class.forName(s, true, ClassLoader.getSystemClassLoader());
        } catch (Throwable cls) {
            //System.out.println("Security manager hides SystemClassLoader.");
            //System.out.println("Falling back to ContextClassLoader.");
            try {
                classDefinition = Class.forName(s, true, Thread.currentThread().getContextClassLoader());
            } catch (Throwable cls2) {
                //    System.out.println("Security manager hides current thread's ContextClassLoader.");
                try {
                    classDefinition = Class.forName(s);
                } catch (Throwable cls3) {
                    throw new ClassNotFoundException("Unknown class: " + s);
                }
                //    System.out.println("Falling back to Class.forName("+s+")");
            }
        }
        return classDefinition;
    }

    /**
     * Description of the Method
     *
     * @param source Description of the Parameter
     * @return Description of the Return Value
     * @throws IOException Description of the Exception
     */
    public static BufferedImage readImage(Object source) throws IOException {
        ImageInputStream stream = ImageIO.createImageInputStream(source);
        if (stream == null) {
            if (source instanceof java.net.URL) {
                return ImageIO.read((java.net.URL) source);
            } else {
                return null;
            }
        }
        Iterator<?> it = ImageIO.getImageReaders(stream);
        if (!it.hasNext()) {
            // bug with firefox 2
            BufferedImage buf = null;
            if (source instanceof File) {
                buf = ImageIO.read((File) source);
            }
            return buf;
        }
        ImageReader reader = (ImageReader) it.next();
        reader.setInput(stream);
        ImageReadParam param = reader.getDefaultReadParam();

        ImageTypeSpecifier typeToUse = null;
        boolean looking = true;
        for (Iterator<?> i = reader.getImageTypes(0); i.hasNext() && looking; ) {
            ImageTypeSpecifier type = (ImageTypeSpecifier) i.next();
            if (type.getColorModel().getColorSpace().getNumComponents() == 1) {
                typeToUse = type;
                looking = false;
            } else if (type.getColorModel().getColorSpace().isCS_sRGB()) {
                typeToUse = type;
                looking = false;
            }
        }
        if (typeToUse != null) {
            param.setDestinationType(typeToUse);
        }

        BufferedImage b = null;
        try {
            b = reader.read(0, param);
        } catch (Exception e) {
            e.printStackTrace();
        }

        reader.dispose();
        stream.close();
        return b;
    }

    /**
     * Description of the Method
     *
     * @param bufferedImage Description of the Parameter
     */
    public static void waitForImage(BufferedImage bufferedImage) {
        final ImageLoadStatus imageLoadStatus = new ImageLoadStatus();
        bufferedImage.getHeight(
                new ImageObserver() {
                    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                        if (infoflags == ALLBITS) {
                            imageLoadStatus.heightDone = true;
                            return true;
                        }
                        return false;
                    }
                });
        bufferedImage.getWidth(
                new ImageObserver() {
                    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                        if (infoflags == ALLBITS) {
                            imageLoadStatus.widthDone = true;
                            return true;
                        }
                        return false;
                    }
                });
        while (!imageLoadStatus.widthDone && !imageLoadStatus.heightDone) {
            try {
                System.out.println("wait ");
                Thread.sleep(50);
            } catch (InterruptedException e) {

            }
        }
    }

    /**
     * Gets the scaledInstance attribute of the YassUtils object
     *
     * @param img          Description of the Parameter
     * @param targetWidth  Description of the Parameter
     * @param targetHeight Description of the Parameter
     * @return The scaledInstance value
     */
    public static BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight) {
        boolean higherQuality = true;
        Object hint = RenderingHints.VALUE_INTERPOLATION_BICUBIC;

        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        int w;
        int h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

    /**
     * Description of the Method
     *
     * @param str     Description of the Parameter
     * @param pattern Description of the Parameter
     * @param replace Description of the Parameter
     * @return Description of the Return Value
     */
    public static String replace(String str, String pattern, String replace) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();

        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e + pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }

    /**
     * Description of the Method
     *
     * @param str Description of the Parameter
     * @param t   Description of the Parameter
     * @return Description of the Return Value
     */
    public static String replace(String str, Hashtable<?, ?> t) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();

        while ((e = str.indexOf('$', s)) >= 0) {
            int e2 = e + 1;
            char c = str.charAt(e2);
            while (c != ' ' && c != ';') {
                c = str.charAt(++e2);
            }
            String key = str.substring(e + 1, e2);
            result.append(str.substring(s, e));
            String replace = (String) t.get(key.toLowerCase());
            result.append(replace);
            s = e2 + 1;
        }
        result.append(str.substring(s));
        return result.toString();
    }

    /**
     * Description of the Method
     *
     * @param ms Description of the Parameter
     * @return Description of the Return Value
     */
    public static String commaTime(long ms) {
        String sec = "0";
        String msec = ms + "";
        int j = msec.length() - 3;
        if (j > 0) {
            sec = msec.substring(0, j);
            msec = msec.substring(j);
        }
        while (msec.length() < 3) {
            msec = "0" + msec;
        }
        return sec + "," + msec;
    }

    /**
     * Description of the Method
     *
     * @param g          Description of the Parameter
     * @param x          Description of the Parameter
     * @param y          Description of the Parameter
     * @param size       Description of the Parameter
     * @param direction  Description of the Parameter
     * @param isEnabled  Description of the Parameter
     * @param darkShadow Description of the Parameter
     * @param shadow     Description of the Parameter
     * @param highlight  Description of the Parameter
     */
    public static void paintTriangle(Graphics g, int x, int y, int size, int direction, boolean isEnabled, Color darkShadow, Color shadow, Color highlight) {
        Color oldColor = g.getColor();
        int mid;
        int i;
        int j;

        j = 0;
        size = Math.max(size, 2);
        mid = (size / 2) - 1;

        g.translate(x, y);
        if (isEnabled) {
            g.setColor(shadow);
        } else {
            g.setColor(darkShadow);
        }

        switch (direction) {
            case NORTH:
                for (i = 0; i < size; i++) {
                    g.drawLine(mid - i, i, mid + i, i);
                }
                break;
            case SOUTH:
                j = 0;
                for (i = size - 1; i >= 0; i--) {
                    g.drawLine(mid - i, j, mid + i, j);
                    j++;
                }
                break;
            case WEST:
                for (i = 0; i < size; i++) {
                    g.drawLine(i, mid - i, i, mid + i);
                }
                break;
            case EAST:
                j = 0;
                for (i = size - 1; i >= 0; i--) {
                    g.drawLine(j, mid - i, j, mid + i);
                    j++;
                }
                break;
        }
        g.translate(-x, -y);
        g.setColor(oldColor);
    }

    /**
     * Gets the titleVersion attribute of the YassUtils object
     *
     * @param s Description of the Parameter
     * @return The titleVersion value
     */
    public String[] getTitleVersion(String s) {
        String version = null;
        int ti = s.indexOf("[");
        if (ti > 0) {
            int tii = s.indexOf("]", ti);
            if (tii < 0) {
                version = " " + s.substring(ti);
            } else {
                version = " " + s.substring(ti, tii + 1);
            }
        }
        if (ti > 0 && s.charAt(ti - 1) == ' ') {
            ti--;
        }
        if (ti > 0) {
            s = s.substring(0, ti);
        }

        return new String[]{s, version};
    }

    static class ImageLoadStatus {
        public boolean widthDone = false;
        public boolean heightDone = false;
    }
}

