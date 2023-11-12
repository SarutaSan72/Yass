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
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassHyphenator {
    private static String userdir = System.getProperty("user.home") + File.separator + ".yass" + File.separator + "hyphen";
    private Hyphenator hyphenator = null;
    private Hashtable<String, Object> hyphenators = null;
    private String currentLanguage;
    private YassProperties yassProperties;

    private List<String> fallbackHyphenations = null;

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
        this.currentLanguage = lang;
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
        if (hyphenator != null) {
            word = hyphenator.hyphenate(word, 2, 2);
            if (!word.contains("\u00AD")) {
                word = fallbackHyphenation(word);
            }
            if (!word.contains("\u00AD")) {
                // Still couldn't hyphenate. Checking, if it's a word like Checkin'
                word = hyphenateWithApostrophe(word);
            }
            if (!word.contains("\u00AD") && word.length() == 2 && StringUtils.isAllUpperCase(word)) {
                word = word.charAt(0) + "\u00AD" + word.charAt(1);
            }
        }
        return word;
    }

    private String hyphenateWithApostrophe(String word) {
        if (!word.contains("\u00AD") && (word.endsWith("in'") || word.endsWith("in’"))) {
            String temp = hyphenateWord(word.substring(0, word.length() - 1) + "g");
            String apostrophe = word.substring(word.length() - 1);
            if (temp.contains("\u00AD")) {
                word = temp.substring(0, temp.length() - 1) + apostrophe;
            }
        }
        return word;
    }

    private List<String> splitAtApostrophe(String word) {
        int pos;
        List<String> result = new ArrayList<>();
        String apostrophe = yassProperties.getBooleanProperty("typographic-apostrophes") ? "’" : "'";
        pos = word.indexOf(apostrophe);
        if (pos >= 0) {
            result.add(word.substring(0, pos));
            result.add(word.substring(pos));
        }
        return result;
    }

    public List<String> rehyphenate(List<String> original) {
        if (hyphenator == null) {
            return original;
        }
        String word = String.join("", original);
        if (original.size() == 2 && original.get(1).equals("~")) {
            List<String> apostropheTilde = splitAtApostrophe(original.get(0));
            if (apostropheTilde.size() == 2) {
                return apostropheTilde;
            }
        }
        boolean triedFallback = false;
        String apostrophe = yassProperties.getBooleanProperty("typographic-apostrophes") ? "’" : "'";
        boolean shortened = word.endsWith("in" + apostrophe);
        if (shortened) {
            word = word.substring(0, word.length() - 1) + "g";
        }
        String hyphenated = hyphenator.hyphenate(word, 2, 2);
        String[] newSyllables = hyphenated.split("\u00AD");
        if (newSyllables.length < 2) {
            triedFallback = true;
            hyphenated = fallbackHyphenation(word);
            newSyllables = hyphenated.split("\u00AD");
        }
        if (!triedFallback && original.get(0).equals(newSyllables[0])) {
            hyphenated = fallbackHyphenation(word);
            newSyllables = hyphenated.split("\u00AD");
        }

        if (newSyllables.length != original.size() && word.endsWith("~")) {
            hyphenated = fallbackHyphenation(word.substring(0, word.indexOf("~")));
            newSyllables = hyphenated.split("\u00AD");
        }
        if (newSyllables.length != original.size() && checkAbbreviatedWord(original)) {
            hyphenated = hyphenateAbbreviation(original.get(0));
            newSyllables = hyphenated.split("\u00AD");
        }
        if (newSyllables.length != original.size()) {
            return original;
        }
        if (shortened) {
            newSyllables[newSyllables.length - 1] = newSyllables[newSyllables.length - 1].replace("ing", "in" + apostrophe);
        }
        return Arrays.stream(newSyllables).toList();
    }

    private String hyphenateAbbreviation(String input) {
        StringJoiner output = new StringJoiner("\u00AD");
        for (int i = 0; i < input.length(); i++) {
            output.add(String.valueOf(input.charAt(i)));
        }
        return output.toString();
    }

    /**
     * Check if passed syllables are in the following format:
     * First item in the list contains an abbreviated all-caps word like "TV" or "ATM", the following items in the
     * list are all tildes, e. g. ["TV", "~"] or ["ATM", "~", "~"]
     * @param original
     * @return true, if the passed syllables are an abbreviation
     */
    private boolean checkAbbreviatedWord(List<String> original) {
        String firstWord = original.get(0);
        if (original.size() == 1 || (firstWord.length() != original.size() && !StringUtils.isAllUpperCase(firstWord))) {
            return false;
        }
        for (int i = 1; i < original.size(); i++) {
            if (!original.get(i).trim().equals("~")) {
                return false;
            }
        }
        return true;
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

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public YassProperties getYassProperties() {
        return yassProperties;
    }

    public void setYassProperties(YassProperties yassProperties) {
        this.yassProperties = yassProperties;
    }

    public String fallbackHyphenation(String word) {
        if (fallbackHyphenations == null) {
            String fallbackDictionary = getYassProperties().getProperty("hyphenations_" + getCurrentLanguage());
            if (StringUtils.isEmpty(fallbackDictionary)) {
                fallbackHyphenations = null;
            }
            try {
                fallbackHyphenations = Files.readAllLines(Paths.get(fallbackDictionary));
            } catch (IOException e) {
                fallbackHyphenations = null;
            }
        }
        if (fallbackHyphenations != null) {
            for (String line : fallbackHyphenations) {
                if (line.replace("•", "").equals(word.toLowerCase())) {
                    String temp = line.replace("•", "\u00AD");
                    if (StringUtils.isAllUpperCase(word.substring(0, 1))) {
                        temp = StringUtils.capitalize(temp);
                    }
                    return temp;
                }
                if (Character.getNumericValue(line.charAt(0)) > Character.getNumericValue(
                        word.toLowerCase().charAt(0))) {
                    break;
                }
            }
        }
        return word;
    }
}


