package yass.screen;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassScore implements Comparable<Object> {
    private String artist = null;
    private String title = null;
    private String user = null;
    private int score = 0;
    private int difficulty = 0;


    /**
     * Constructor for the YassScore object
     *
     * @param artist     Description of the Parameter
     * @param title      Description of the Parameter
     * @param user       Description of the Parameter
     * @param score      Description of the Parameter
     * @param difficulty Description of the Parameter
     */
    public YassScore(String artist, String title, String user, int score, int difficulty) {
        this.artist = artist;
        this.title = title;
        this.user = user;
        this.score = score;
        this.difficulty = difficulty;
    }


    /**
     * Gets the artist attribute of the YassScore object
     *
     * @return The artist value
     */
    public String getArtist() {
        return artist;
    }


    /**
     * Gets the title attribute of the YassScore object
     *
     * @return The title value
     */
    public String getTitle() {
        return title;
    }


    /**
     * Gets the player attribute of the YassScore object
     *
     * @return The player value
     */
    public String getPlayer() {
        return user;
    }


    /**
     * Gets the score attribute of the YassScore object
     *
     * @return The score value
     */
    public int getScore() {
        return score;
    }


    /**
     * Gets the difficulty attribute of the YassScore object
     *
     * @return The difficulty value
     */
    public int getDifficulty() {
        return difficulty;
    }


    /**
     * Description of the Method
     *
     * @param o Description of the Parameter
     * @return Description of the Return Value
     */
    public int compareTo(Object o) {
        YassScore s = (YassScore) o;
        if (score < s.score) {
            return 1;
        }
        if (score > s.score) {
            return -1;
        }
        return 0;
    }
}

