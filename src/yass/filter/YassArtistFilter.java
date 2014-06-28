package yass.filter;

import yass.YassSong;

import java.util.*;

/**
 * Description of the Class
 *
 * @author Saruta
 * @created 4. M�rz 2008
 */
public class YassArtistFilter extends YassFilter {
    private static boolean moveArticles = true;
    private static Hashtable<String, Vector<String>> lang_articles;
    private static String[] langArray, langID;
    Vector<String> artists = new Vector<>();

    /**
     * Gets the iD attribute of the YassArtistFilter object
     *
     * @return The iD value
     */
    public String getID() {
        return "artist";
    }

    /**
     * Gets the genericRules attribute of the YassArtistFilter object
     *
     * @param data Description of the Parameter
     * @return The genericRules value
     */
    public String[] getGenericRules(Vector<YassSong> data) {
        int min = new Integer(getProperties().getProperty("group-min")).intValue();

        moveArticles = getProperties().get("use-articles").equals("true");
        if (moveArticles)
            loadArticles();

        artists.clear();
        Hashtable<String, Integer> count = new Hashtable<>();
        for (Enumeration<?> e = data.elements(); e.hasMoreElements(); ) {
            YassSong s = (YassSong) e.nextElement();
            String artist = s.getArtist();
            if (artist == null || artist.length() < 1) {
                continue;
            }

            String language = s.getLanguage();
            if (moveArticles) {
                artist = getSortedArtist(artist, language);
            }

            if (!artists.contains(artist)) {
                Integer i = count.get(artist);
                if (i == null) {
                    i = new Integer(1);
                }
                count.put(artist, new Integer(i.intValue() + 1));
                if (i.intValue() >= min) {
                    artists.addElement(artist);
                }
            }
        }
        Collections.sort(artists);

        return artists.toArray(new String[]{});
    }

    /**
     * Description of the Method
     */
    private void loadArticles() {
        Vector<String> langVector = new Vector<>();
        Vector<String> langIDVector = new Vector<>();

        StringTokenizer languages = new StringTokenizer(getProperties().getProperty("language-tag"), "|");
        while (languages.hasMoreTokens()) {
            String s = languages.nextToken();
            String sid = languages.nextToken();
            langVector.add(s);
            langIDVector.add(sid);
        }
        languages = new StringTokenizer(getProperties().getProperty("language-more-tag"), "|");
        while (languages.hasMoreTokens()) {
            String s = languages.nextToken();
            String sid = languages.nextToken();
            langVector.add(s);
            langIDVector.add(sid);
        }
        langArray = new String[langVector.size()];
        langID = new String[langVector.size()];
        langVector.toArray(langArray);
        langIDVector.toArray(langID);

        //---

        lang_articles = new Hashtable<>();
        String s = getProperties().getProperty("articles");
        StringTokenizer st = new StringTokenizer(s, ":");
        while (st.hasMoreTokens()) {
            String lang = st.nextToken();

            Vector<String> v = lang_articles.get(lang);
            if (v == null) {
                v = new Vector<>();
                lang_articles.put(lang, v);
            } else {
                v.clear();
            }
            String articles = st.nextToken();
            StringTokenizer sta = new StringTokenizer(articles, "|");
            while (sta.hasMoreTokens()) {
                String article = sta.nextToken();
                article = article.toLowerCase();
                v.addElement(article);
            }
        }
    }

    public String getSortedArtist(String a, String lang) {
        if (langArray == null)
            loadArticles();

        if (lang == null)
            return a;

        for (int i = 0; i < langArray.length; i++) {
            if (langArray[i].equals(lang)) {
                lang = langID[i];
                break;
            }
        }

        Vector<?> v = lang_articles.get(lang);
        if (v == null)
            return a;

        String artist = a.toLowerCase();
        for (Enumeration<?> en = v.elements(); en.hasMoreElements(); ) {
            String article = (String) en.nextElement();
            if (artist.startsWith(article)) {
                int len = article.length();
                a = a.substring(len) + ", " + a.substring(0, len);
                return a;
            }
        }
        return a;
    }

    /**
     * Description of the Method
     *
     * @param rule Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean allowCoverDrop(String rule) {
        if (rule.equals("all")) {
            return false;
        }
        if (rule.equals("others")) {
            return false;
        }
        return true;
    }


    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean accept(YassSong s) {
        String t = s.getArtist();
        boolean hit = false;

        if (moveArticles)
            t = getSortedArtist(t, s.getLanguage());

        if (rule.equals("all")) {
            hit = true;
        } else if (rule.equals("others")) {
            hit = artists.contains(t);
        } else {
            hit = t.equals(rule);
        }

        return hit;
    }
}

