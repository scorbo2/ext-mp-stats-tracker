package ca.corbett.musicplayer.extensions.statstracker;

import ca.corbett.musicplayer.Version;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wraps the database file provided by sqlite and provides service methods
 * for querying and updating track statistics.
 *
 * @since 2025-06-01
 * @author scorbo2
 */
public class StatsDb {

    private static final Logger logger = Logger.getLogger(StatsDb.class.getName());
    private Connection conn;
    private boolean dbAvailable;
    private final File APP_HOME;
    private File dbFile;

    public StatsDb() {
        this(Version.SETTINGS_DIR);
    }

    /**
     * This constructor provided for test purposes - this allows
     * you to override where the stats db lives, instead of assuming
     * it's going to live in the app's home dir.
     *
     * @param appHomeDir The parent directory for the stats db file.
     */
    public StatsDb(File appHomeDir) {
        this.APP_HOME = appHomeDir;
        initialize();
    }

    /**
     * Closes db connections if open.
     */
    public void close() {
        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Problem closing db connections.", sqe);
        }
    }

    /**
     * Attempts to open the database file if it exists, or attempts to create
     * it if it does not yet exist. It's possible that the sqlite jdbc driver
     * can't be located, in which case database access isn't possible and this
     * class effectively becomes one big no-op.
     */
    public void initialize() {
        close();
        dbAvailable = true;

        // Make sure our classpath contains what we need:
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException ignored) {
            logger.severe("SQLite not available - stats tracking disabled.");
            dbAvailable = false;
        }

        dbFile = new File(APP_HOME, "stats_tracker.db");
        boolean dbFileExists = dbFile.exists();
        conn = getConnection(dbFile);
        if (! dbFileExists) {
            createDb();
        }
    }

    /**
     * Returns the play count of the given track, or zero if
     * the given track has never been seen by this StatsDb.
     *
     * @param trackFile The track in question.
     * @return The play count on record for that track.
     */
    public int getPlayCount(File trackFile) {
        if (! dbAvailable || conn == null) {
            return 0;
        }
        try {
            String sql = "select playcount from statstracker where track = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, trackFile.getAbsolutePath());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("playcount");
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Problem querying stats db: "+sqe.getMessage(), sqe);
        }

        return 0;
    }

    /**
     * Used internally to determine if any stats have been tracked for the
     * given track file.
     *
     * @param trackFile The track in question.
     * @return True if there is a play count on record for that track.
     */
    protected boolean hasPlayCount(File trackFile) {
        if (! dbAvailable || conn == null) {
            return false;
        }

        boolean result = false;
        try {
            String sql = "select playcount from statstracker where track = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, trackFile.getAbsolutePath());
            ResultSet rs = ps.executeQuery();
            result = rs.next();
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Problem querying stats db: "+sqe.getMessage(), sqe);
        }

        return result;
    }

    /**
     * Increments the play count, whatever it currently is, for the
     * given track file. Will silently create an entry for the given
     * track with a play count of 1 if it has never been seen before.
     *
     * @param trackFile The track in question.
     */
    public void incrementPlayCount(File trackFile) {
        if (! dbAvailable || conn == null) {
            return;
        }

        try {
            if (hasPlayCount(trackFile)) {
                int count = getPlayCount(trackFile) + 1;
                String sql = "update statstracker set playcount = ? where track = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, count);
                ps.setString(2, trackFile.getAbsolutePath());
                ps.executeUpdate();
                ps.close();
            }
            else {
                String sql = "insert into statstracker (track, playcount) values (?,?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, trackFile.getAbsolutePath());
                ps.setInt(2, 1);
                ps.executeUpdate();
                ps.close();
            }
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Problem updating stats db: "+sqe.getMessage(), sqe);
        }
    }

    public List<Entry> getTop10() {
        List<Entry> list = new ArrayList<>(10);
        if (! dbAvailable || conn == null) {
            return list;
        }

        try {
            String sql = "select track,playcount from statstracker order by playcount desc limit 10";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                File trackFile = new File(rs.getString(1));
                int playCount = rs.getInt(2);
                list.add(new Entry(trackFile, playCount));
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Problem querying stats db: "+sqe.getMessage(), sqe);
        }

        return list;
    }

    /**
     * Removes all stats for all tracks.
     */
    public void resetStats() {
        if (! dbAvailable || conn == null) {
            return;
        }

        try {
            String sql = "delete from statstracker";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.executeUpdate();
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Problem updating stats db: "+sqe.getMessage(), sqe);
        }
    }

    /**
     * Invoked internally to open a sqlite connection to the given
     * File, which is assumed to be an existing sqlite database.
     *
     * @param file The sqlite db file.
     * @return A Connection object, or null if something goes wrong (details in log).
     */
    private Connection getConnection(File file) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Problem getting connection to " + file.getName(), sqe);
        }

        return conn;
    }

    /**
     * Invoked internally to create the schema for a new tracking database.
     */
    private void createDb() {
        if (! dbAvailable) {
            return;
        }

        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate("create table statstracker (id integer, track text, playcount integer)");
            statement.close();
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Unable to create stats tracker db.", sqe);
        }
    }

    public static class Entry {
        public final File trackFile;
        public final int playCount;

        public Entry(File trackFile, int playCount) {
            this.trackFile = trackFile;
            this.playCount = playCount;
        }
    }
}
