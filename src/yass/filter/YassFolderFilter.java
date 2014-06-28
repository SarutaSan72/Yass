package yass.filter;

import yass.I18;
import yass.YassSong;
import yass.YassSongList;

import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 4. M�rz 2008
 */
public class YassFolderFilter extends YassFilter {
    static String songdir = null;


    /**
     * Gets the iD attribute of the YassFolderFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "folder";
    }


    /**
     * Gets the genericRules attribute of the YassFolderFilter object
     *
     * @param data Description of the Parameter
     * @return The genericRules value
     */
    public String[] getGenericRules(Vector<YassSong> data) {
        songdir = getProperties().getProperty("song-directory");

        Vector<String> folders = new Vector<>();
        for (Enumeration<YassSong> e = data.elements(); e.hasMoreElements(); ) {
            YassSong s = e.nextElement();
            String folder = s.getFolder();
            if (folder == null || folder.length() < 1) {
                continue;
            }
            if (!folders.contains(folder)) {
                folders.addElement(folder);
            }
        }
        Collections.sort(folders);

        return folders.toArray(new String[]{});
    }


    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean accept(YassSong s) {
        String t = s.getFolder();
        boolean hit = false;

        if (rule.equals("all")) {
            hit = true;
        } else {
            hit = t.equals(rule);
        }

        return hit;
    }


    /**
     * Description of the Method
     *
     * @param rule Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean allowDrop(String rule) {
        if (songdir == null) {
            return false;
        }
        if (rule.equals("all")) {
            return false;
        }
        return true;
    }


    /**
     * Description of the Method
     *
     * @param rule Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean allowCoverDrop(String rule) {
        if (songdir == null) {
            return false;
        }
        if (rule.equals("all")) {
            return false;
        }
        return true;
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean confirm() {
        return true;
    }


    /**
     * Gets the confirmString attribute of the YassFolderFilter object
     *
     * @param rule Description of the Parameter
     * @return The confirmString value
     */
    public String getConfirmString(String rule) {
        return java.text.MessageFormat.format(I18.get("group_folder_confirm"), rule);
    }


    /**
     * Gets the extraInfo attribute of the YassFolderFilter object
     *
     * @return The extraInfo value
     */
    public int getExtraInfo() {
        return YassSongList.FOLDER_COLUMN;
    }


    /**
     * Description of the Method
     *
     * @param rule Description of the Parameter
     * @param s    Description of the Parameter
     */
    public void drop(String rule, YassSong s) {
        String dir = s.getDirectory();
        File f = new File(dir);
        File oldp = f.getParentFile();
        String name = f.getName();

        File p = new File(songdir, rule);
        File f2 = new File(p, name);
        String newdir = f2.getAbsolutePath();

        boolean ok2 = f.renameTo(f2);
        if (ok2) {
            s.setDirectory(newdir);
            s.setFolder(rule);

            if (oldp.listFiles() == null || oldp.listFiles().length == 0) {
                oldp.delete();
            }
        }
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean refreshCounters() {
        return true;
    }
}

