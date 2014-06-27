package yass.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import yass.YassActions;
import yass.YassPlayListModel;
import yass.YassSong;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    4. März 2008
 */
public class YassPlaylistFilter extends YassFilter {

	static Hashtable<String, Vector<String>> songs = null;
	static Hashtable<String, YassPlayListModel> playlists = null;


	/**
	 *  Gets the iD attribute of the YassAlbumFilter object
	 *
	 * @return    The iD value
	 */
	public String getID() {
		return "playlist";
	}


	/**
	 *  Gets the genericRules attribute of the YassAlbumFilter object
	 *
	 * @param  data  Description of the Parameter
	 * @return       The genericRules value
	 */
	public String[] getGenericRules(Vector<YassSong> data) {
		Vector<String> pl = new Vector<String>();
		if (songs == null) {
			songs = new Hashtable<String, Vector<String>>(1000);
			playlists = new Hashtable<String, YassPlayListModel>(10);
		} else {
			playlists.clear();
		}

		String plcacheName = getProperties().getProperty("playlist-cache");
		if (plcacheName == null) {
			return (String[]) pl.toArray(new String[]{});
		}
		File plcache = new File(plcacheName);
		if (plcache.exists()) {
			try {
				BufferedReader inputStream = new BufferedReader(new FileReader(plcache));
				String l;
				while ((l = inputStream.readLine()) != null) {
					YassPlayListModel plf = getPlayListFile(l);
					pl.addElement(plf.getName());
					playlists.put(plf.getName(), plf);
				}
				inputStream.close();
			}
			catch (Exception e) {
			}
		}

		return (String[]) pl.toArray(new String[]{});
	}

	// copied from yassplaylist

	/**
	 *  Gets the playListFile attribute of the YassPlaylistFilter object
	 *
	 * @param  txt  Description of the Parameter
	 * @return      The playListFile value
	 */
	public YassPlayListModel getPlayListFile(String txt) {
		try {
			File pFile = new File(txt);
			FileReader fr = new FileReader(pFile);

			StringWriter sw = new StringWriter();
			char[] buffer = new char[1024];
			int readCount = 0;
			while ((readCount = fr.read(buffer)) > 0) {
				sw.write(buffer, 0, readCount);
			}
			fr.close();

			return getPlayList(sw.toString());
		}
		catch (Exception e) {
		}
		return null;
	}


	/**
	 *  Gets the playList attribute of the YassPlaylistFilter object
	 *
	 * @param  txt  Description of the Parameter
	 * @return      The playList value
	 */
	public YassPlayListModel getPlayList(String txt) {
		YassPlayListModel pl = new YassPlayListModel();
		try {
			BufferedReader inputStream = new BufferedReader(new StringReader(txt));
			String l;

			while ((l = inputStream.readLine()) != null) {
				StringTokenizer ll = new StringTokenizer(l, ":");
				String artist = null;
				String title = null;

				if (ll.hasMoreTokens()) {
					artist = ll.nextToken().trim();
					if (artist.startsWith("#")) {
						if (artist.startsWith("#Playlist")) {
							int k = artist.indexOf('\"');
							if (k > 0) {
								int kk = artist.indexOf('\"', k + 1);
								if (kk > 0) {
									pl.setName(artist.substring(k + 1, kk));
								}
							}
						}
						if (artist.startsWith("#Name:")) {
							int k = artist.indexOf(':');
							pl.setName(artist.substring(k + 1).trim());
						}
						artist = null;
						continue;
					}
				}
				if (ll.hasMoreTokens()) {
					title = ll.nextToken().trim();

					String version = "";
					int k = title.indexOf("[");
					if (k > 0) {
						version = title.substring(k + 1, title.indexOf("]", k));
						title = title.substring(0, k);
					}
					String key = artist + " : " + title + " [" + version + "]";
					Vector<String> v = (Vector<String>) songs.get(key);
					if (v == null) {
						v = new Vector<String>(3);
						songs.put(key, v);
					}
					v.addElement(pl.getName());
					pl.addElement(key);
				}
			}
			inputStream.close();

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return pl;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  rule  Description of the Parameter
	 * @return       Description of the Return Value
	 */
	public boolean allowDrop(String rule) {
		return false;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  rule  Description of the Parameter
	 * @return       Description of the Return Value
	 */
	public boolean allowCoverDrop(String rule) {
		if (rule.equals("all")) {
			return false;
		}
		if (rule.equals("unspecified")) {
			return false;
		}
		return true;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  rule  Description of the Parameter
	 * @param  s     Description of the Parameter
	 */
	public void drop(String rule, YassSong s) {
		if (false) {
			return;
		}

		// abandoned
		// false cognitive model; using songlist for displaying playlist irritates user

		YassPlayListModel pl = (YassPlayListModel) playlists.get(rule);
		if (pl == null) {
			return;
		}

		if (rule.equals("unspecified")) {
			// todo remove song from all playlists
			return;
		}

		String artist = s.getArtist();
		String title = s.getTitle();
		String version = s.getVersion();
		String key = artist + " : " + title + " [" + version + "]";

		if (pl.contains(key)) {
			return;
		}
		pl.addElement(key);

		// todo: append song to playlist, ask, write
		storePlayList(pl);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  pl  Description of the Parameter
	 * @return     Description of the Return Value
	 */
	public boolean storePlayList(YassPlayListModel pl) {
		String listTitle = pl.getName();

		String defDir = getProperties().getProperty("playlist-directory");
		String filename = defDir + File.separator + listTitle + ".upl";

		String listFilename = pl.getFileName();
		if (listFilename != null) {
			filename = listFilename;
		}

		int n = pl.size();
		PrintWriter outputStream = null;
		FileWriter fw = null;
		try {
			outputStream = new PrintWriter(fw = new FileWriter(filename));

			outputStream.println("######################################");
			outputStream.println("#Ultrastar Deluxe Playlist Format v1.0");
			outputStream.println("#Playlist \"" + listTitle + "\" with " + n + " Songs.");
			outputStream.println("#Created with Yass " + YassActions.VERSION + ".");
			outputStream.println("######################################");
			outputStream.println("#Name: " + listTitle);
			outputStream.println("#Songs:");

			for (Enumeration<?> en = pl.elements(); en.hasMoreElements(); ) {
				String line = (String) en.nextElement();
				int i = line.lastIndexOf(" [");
				if (i > 0) {
					line = line.substring(0, i);
				}
				outputStream.println(line);
			}
			outputStream.close();
		}
		catch (Exception e) {
			return false;
		}
		finally {
			try {
				if (fw != null) {
					fw.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			}
			catch (Exception e) {}
		}
		return true;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public boolean count() {
		return false;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public boolean accept(YassSong s) {
		boolean hit = false;

		if (rule.equals("all")) {
			hit = true;
		} else if (rule.equals("unspecified")) {
			String artist = s.getArtist();
			String title = s.getTitle();
			String version = s.getVersion();
			if (version == null) {
				version = "";
			}
			String key = artist + " - " + title + " [" + version + "]";
			Vector<?> v = (Vector<?>) songs.get(key);
			if (v == null) {
				hit = true;
			}
		} else {
			String artist = s.getArtist();
			String title = s.getTitle();
			String version = s.getVersion();
			if (version == null) {
				version = "";
			}
			String key = artist + " : " + title + " [" + version + "]";
			Vector<?> v = (Vector<?>) songs.get(key);
			if (v != null) {
				for (Enumeration<?> en = v.elements(); en.hasMoreElements(); ) {
					String pl = (String) en.nextElement();
					if (pl.equals(rule)) {
						hit = true;
					}
				}
			}
		}

		return hit;
	}
}

