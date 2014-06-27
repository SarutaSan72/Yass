package yass.stats;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import yass.I18;
import yass.YassSong;
import yass.YassTable;
import yass.YassUtils;

/**
 *  Description of the Class
 *
 * @author     Saruta
 * @created    4. M�rz 2008
 */
public class YassStats implements Cloneable {
	/**
	 *  Description of the Field
	 */
	private String label = null;

	private static Hashtable<String, Integer> hash = null;
	private static Vector<YassStats> plugins = null;
	private static int index = -1;

	/**
	 *  Description of the Field
	 */
	private static Properties prop = null;

	/**
	 *  Description of the Field
	 */
	public static int length = -1;

	/**
	 *  Description of the Field
	 */
	public static String allids[] = null;
	/**
	 *  Description of the Field
	 */
	public static String alllabels[] = null;
	/**
	 *  Description of the Field
	 */
	public static String alldesc[] = null;
	/**
	 *  Description of the Field
	 */
	public static YassStats allstats[] = null;



	/**
	 *  Constructor for the YassFilterPlugin object
	 */
	public YassStats() { }


	/**
	 *  Gets the iD attribute of the YassStats object
	 *
	 * @return    The iD value
	 */
	public String[] getIDs() {
		return new String[]{"unspecified"};
	}


	/**
	 *  Gets the iDAt attribute of the YassStats class
	 *
	 * @param  i  Description of the Parameter
	 * @return    The iDAt value
	 */
	public static String getIDAt(int i) {
		return allids[i];
	}


	/**
	 *  Gets the labelAt attribute of the YassStats class
	 *
	 * @param  i  Description of the Parameter
	 * @return    The labelAt value
	 */
	public static String getLabelAt(int i) {
		return alllabels[i];
	}


	/**
	 *  Gets the descriptionAt attribute of the YassStats class
	 *
	 * @param  i  Description of the Parameter
	 * @return    The descriptionAt value
	 */
	public static String getDescriptionAt(int i) {
		return alldesc[i];
	}


	/**
	 *  Gets the count attribute of the YassStats class
	 *
	 * @return    The count value
	 */
	public int getIDCount() {
		return getIDs().length;
	}



	/**
	 *  Description of the Method
	 */
	public static void init() {
		hash = new Hashtable<>();
		plugins = new Vector<>();
		StringTokenizer st = new StringTokenizer(prop.getProperty("stats-plugins"), "|");
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			addPlugin(s);
		}

		length = 0;
		for (Enumeration<YassStats> en = plugins.elements(); en.hasMoreElements(); ) {
			YassStats stats = (YassStats) en.nextElement();
			length += stats.getIDCount();
		}

		allids = new String[length];
		alllabels = new String[length];
		alldesc = new String[length];
		allstats = new YassStats[length];

		int k = 0;
		for (Enumeration<YassStats> en = plugins.elements(); en.hasMoreElements(); ) {
			YassStats stats = (YassStats) en.nextElement();
			String s[] = stats.getIDs();
			for (int i = 0; i < s.length; i++) {
				allstats[k] = stats;
				allids[k] = s[i];
				alllabels[k] = I18.get("stats_" + s[i] + "_title");
				alldesc[k] = I18.get("stats_" + s[i] + "_msg");
				hash.put(s[i], new Integer(k));
				k++;
			}
		}
	}


	private static void addPlugin(String classname) {
		YassStats stats = null;
		try {
			Class<?> c = YassUtils.forName(classname);
			stats = (YassStats) c.newInstance();
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		plugins.addElement(stats);
	}


	/**
	 *  Gets the allStats attribute of the YassStats class
	 *
	 * @param  i  Description of the Parameter
	 * @return    The allStats value
	 */
	public static YassStats getStatsAt(int i) {
		return allstats[i];
	}


	/**
	 *  Description of the Method
	 *
	 * @param  id  Description of the Parameter
	 * @return     Description of the Return Value
	 */
	public static int indexOf(String id) {
		return ((Integer) (hash.get(id))).intValue();
	}


	/**
	 *  Gets the allStats attribute of the YassStats class
	 *
	 * @return    The allStats value
	 */
	public static Vector<YassStats> getAllStats() {
		return plugins;
	}


	/**
	 *  Sets the properties attribute of the YassStats class
	 *
	 * @param  p  The new properties value
	 */
	public static void setProperties(Properties p) {
		prop = p;
	}



	/**
	 *  Gets the properties attribute of the YassStats class
	 *
	 * @return    The properties value
	 */
	public static Properties getProperties() {
		return prop;
	}


	/**
	 *  Gets the stats attribute of the YassStats object
	 *
	 * @param  t  Description of the Parameter
	 * @param  s  Description of the Parameter
	 */
	public void calcStats(YassSong s, YassTable t) {
	}


	/**
	 *  Constructor for the accept object
	 *
	 * @param  s      Description of the Parameter
	 * @param  rule   Description of the Parameter
	 * @param  start  Description of the Parameter
	 * @param  end    Description of the Parameter
	 * @return        Description of the Return Value
	 */
	public boolean accept(YassSong s, String rule, float start, float end) {
		return false;
	}
}


