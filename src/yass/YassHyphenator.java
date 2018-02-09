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

import net.davidashen.text.Hyphenator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassHyphenator {
    private static String userdir = System.getProperty("user.home") + File.separator + ".yass" + File.separator + "hyphen";
    private Hyphenator hyphenator = null;
    private Hashtable<String, Object> hyphenators = null;


    /**
     * Constructor for the YassHyphenator object
     *
     * @param hyphenations Description of the Parameter
     */
    public YassHyphenator(String hyphenations) {
        hyphenators = new Hashtable<>();
        setHyphenations(hyphenations);
    }


    /**
     * Constructor for the setHyphenators object
     *
     * @param hyphenations Description of the Parameter
     */
    public void setHyphenations(String hyphenations) {
        hyphenators.clear();
        if (hyphenations == null) {
            return;
        }
        StringTokenizer st = new StringTokenizer(hyphenations, "|");
        while (st.hasMoreTokens()) {
            String language = st.nextToken();
            hyphenators.put(language, language);
        }
    }


    /**
     * Gets the languages attribute of the YassHyphenator object
     *
     * @return The languages value
     */
    public Enumeration<String> getLanguages() {
        return hyphenators.keys();
    }


    /**
     * Sets the language attribute of the YassHyphenator object
     *
     * @param lang The new language value
     * @return Description of the Return Value
     */
    public boolean setLanguage(String lang) {
        StringTokenizer st = new StringTokenizer(lang, "()-_");
        String language = st.nextToken();
        String country = st.hasMoreTokens() ? st.nextToken() : "";
        String lc = country.length() > 0 ? language + "_" + country : language;

        hyphenator = null;
        Object hy = hyphenators.get(lc);
        if (hy == null) {
            hy = hyphenators.get(language);
        }
        if (hy != null) {
            if (hy instanceof String) {
                try {
                    InputStream is;
                    File f = new File(userdir + File.separator + hy + ".tex");
                    if (f.exists()) {
                        is = new FileInputStream(f);
                    } else {
                        is = getClass().getResourceAsStream("/yass/resources/hyphen/" + hy + ".tex");
                    }

                    //System.out.println("/hyphen/" + hy + ".tex");
                    hyphenator = new Hyphenator();
                    hyphenator.loadTable(new BufferedInputStream(is));
                    hyphenators.put((String) hy, hyphenator);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                hyphenator = (Hyphenator) hy;
            }
        }
        return hyphenator != null;
    }


    /**
     * Description of the Method
     *
     * @param word Description of the Parameter
     * @return Description of the Return Value
     */
    public String hyphenateWord(String word) {
        word = hyphenator.hyphenate(word, 2, 2);
        return word;
    }


    /**
     * Description of the Method
     *
     * @param txt Description of the Parameter
     * @return Description of the Return Value
     */
    public String hyphenateText(String txt) {
        StringTokenizer st = new StringTokenizer(txt, "\n");
        StringBuilder sb = new StringBuilder();
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            StringTokenizer st2 = new StringTokenizer(line, " ");
            boolean first = true;
            while (st2.hasMoreTokens()) {
                String word = st2.nextToken();
                word = word.replaceAll("-", "");
                word = word.replace(YassRow.SPACE, ' ');
                word = word.replace(YassRow.HYPHEN, '-');

                //System.out.print("word "+word+" ");
                word = hyphenator.hyphenate(word, 2, 2);
                //System.out.println(word);
                word = word.replace('-', YassRow.HYPHEN);
                word = word.replace(' ', YassRow.SPACE);
                word = word.replace('\u00ad', '-');
                word = word.replaceAll("\u200b", "");

                if (first) {
                    first = false;
                } else {
                    sb.append(" ");
                }
                sb.append(word);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}


