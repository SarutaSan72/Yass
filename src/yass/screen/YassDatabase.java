package yass.screen;

import java.io.File;
import java.sql.*;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassDatabase {
    private final static String DB_NAME = "yass_db";
    private final static String DB_USER = "yass_along";
    private final static String DB_PASS = "yass_along";

    private Connection connection = null;


    /**
     * Constructor for the YassDatabase object
     */
    public YassDatabase() {
        if (yass.YassMain.NO_GAME) {
            return;
        }

        //derby home
        String homeDir = new File(System.getProperty("user.home") + File.separator + ".yass").getPath();
        System.setProperty("derby.system.home", homeDir);

        //load driver
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        } catch (Exception e) {
            System.out.println("ERROR: Cannot load database driver.");
            e.printStackTrace();
        }
    }


    /**
     * Description of the Method
     */
    public void open() {
        if (yass.YassMain.NO_GAME) {
            return;
        }
        boolean db_exists = new File(System.getProperty("user.home") + File.separator + ".yass" + File.separator + DB_NAME).exists();

        try {
            if (db_exists) {
                connection = DriverManager.getConnection("jdbc:derby:" + DB_NAME + ";user=" + DB_USER + ";password=" + DB_PASS);
            } else {
                connection = DriverManager.getConnection("jdbc:derby:" + DB_NAME + ";user=" + DB_USER + ";password=" + DB_PASS + ";create=true");
                createTables();
            }
        } catch (Exception e) {
            System.out.println("ERROR: Cannot connect database.");
            e.printStackTrace();
        }
    }


    /**
     * Description of the Method
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (Exception e) {
            System.out.println("ERROR: Cannot close database.");
            e.printStackTrace();
        }
    }


    private boolean createTables() {
        boolean ok = false;
        Statement st = null;

        try {
            st = connection.createStatement();
            st.execute("CREATE table SCORE ( ARTIST VARCHAR(100), TITLE VARCHAR(100), PLAYER VARCHAR(30), SCORE INTEGER, DIFFICULTY INTEGER )");
            ok = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ok;
    }


    /**
     * Description of the Method
     *
     * @param artist     Description of the Parameter
     * @param title      Description of the Parameter
     * @param user       Description of the Parameter
     * @param score      Description of the Parameter
     * @param difficulty Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean insertScore(String artist, String title, String user, int score, int difficulty) {
        boolean ok = false;
        PreparedStatement st = null;

        try {
            st = connection.prepareStatement("insert into score values ( ?, ?, ?, ?, ?)");
            st.setString(1, artist);
            st.setString(2, title);
            st.setString(3, user);
            st.setInt(4, score);
            st.setInt(5, difficulty);
            st.executeUpdate();
            ok = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ok;
    }


    /**
     * Description of the Method
     */
    public void showScore() {
        Statement st = null;
        ResultSet res = null;

        try {
            st = connection.createStatement();
            res = st.executeQuery("select * from SCORE");
            while (res.next()) {
                String artist = res.getString(1);
                String title = res.getString(2);
                String player = res.getString(3);
                int score = res.getInt(4);
                int difficulty = res.getInt(5);
                System.out.println(artist + " - " + title);
                System.out.println("   " + player + ": " + score + " (" + difficulty + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
                if (st != null) {
                    st.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Gets the score attribute of the YassDatabase object
     *
     * @param artist Description of the Parameter
     * @param title  Description of the Parameter
     * @return The score value
     */
    public Vector<YassScore> getScore(String artist, String title) {
        PreparedStatement st = null;
        ResultSet res = null;

        Vector<YassScore> all = new Vector<>();
        try {
            //st = connection.createStatement();
            st = connection.prepareStatement("select * from SCORE where artist = ? and title = ? ORDER BY score DESC");
            st.setString(1, artist);
            st.setString(2, title);
            res = st.executeQuery();
            while (res.next()) {
                artist = res.getString(1);
                title = res.getString(2);
                String player = res.getString(3);
                int score = res.getInt(4);
                int difficulty = res.getInt(5);
                all.addElement(new YassScore(artist, title, player, score, difficulty));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
                if (st != null) {
                    st.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return all;
    }


    /**
     * Gets the score attribute of the YassDatabase object
     *
     * @param artist Description of the Parameter
     * @param title  Description of the Parameter
     * @param player Description of the Parameter
     * @return The score value
     */
    public Vector<YassScore> getScore(String artist, String title, String player) {
        PreparedStatement st = null;
        ResultSet res = null;

        Vector<YassScore> all = new Vector<>();
        try {
            //st = connection.createStatement();
            st = connection.prepareStatement("select * from SCORE where artist = ? and title = ? and player = ? ORDER BY score DESC");
            st.setString(1, artist);
            st.setString(2, title);
            st.setString(3, player);
            res = st.executeQuery();
            while (res.next()) {
                artist = res.getString(1);
                title = res.getString(2);
                player = res.getString(3);
                int score = res.getInt(4);
                int difficulty = res.getInt(5);
                all.addElement(new YassScore(artist, title, player, score, difficulty));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
                if (st != null) {
                    st.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return all;
    }


    /**
     * Description of the Method
     *
     * @param oldPlayer Description of the Parameter
     * @param newPlayer Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean renamePlayer(String oldPlayer, String newPlayer) {
        PreparedStatement st = null;

        try {
            st = connection.prepareStatement("update player = ? from SCORE where player = ?");
            st.setString(1, newPlayer);
            st.setString(2, oldPlayer);
            st.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }


    /**
     * Gets the score attribute of the YassDatabase object
     *
     * @param artist     Description of the Parameter
     * @param title      Description of the Parameter
     * @param difficulty Description of the Parameter
     * @return The score value
     */
    public Vector<YassScore> getScore(String artist, String title, int difficulty) {
        PreparedStatement st = null;
        ResultSet res = null;

        Vector<YassScore> all = new Vector<>();
        try {
            //st = connection.createStatement();
            st = connection.prepareStatement("select * from SCORE where artist = ? and title = ? and difficulty = ? ORDER BY score DESC");
            st.setString(1, artist);
            st.setString(2, title);
            st.setInt(3, difficulty);
            res = st.executeQuery();
            while (res.next()) {
                artist = res.getString(1);
                title = res.getString(2);
                String player = res.getString(3);
                int score = res.getInt(4);
                difficulty = res.getInt(5);
                all.addElement(new YassScore(artist, title, player, score, difficulty));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
                if (st != null) {
                    st.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return all;
    }


    /**
     * Gets the top attribute of the YassDatabase object
     *
     * @param top        Description of the Parameter
     * @param difficulty Description of the Parameter
     * @return The top value
     */
    public Vector<YassScore> getTop(int top, int difficulty) {
        PreparedStatement st = null;
        ResultSet res = null;

        Vector<YassScore> all = new Vector<>();
        try {
            //st = connection.createStatement();
            st = connection.prepareStatement("select * from SCORE where difficulty = ? ORDER BY score DESC");
            st.setInt(1, difficulty);
            res = st.executeQuery();
            while (res.next() && top-- > 0) {
                String artist = res.getString(1);
                String title = res.getString(2);
                String player = res.getString(3);
                int score = res.getInt(4);
                difficulty = res.getInt(5);
                all.addElement(new YassScore(artist, title, player, score, difficulty));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
                if (st != null) {
                    st.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return all;
    }
}

