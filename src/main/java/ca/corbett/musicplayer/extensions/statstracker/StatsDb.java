package ca.corbett.musicplayer.extensions.statstracker;

import ca.corbett.musicplayer.Version;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatsDb {

    private static final Logger logger = Logger.getLogger(StatsDb.class.getName());
    private Connection conn;
    private boolean dbAvailable;
    private final File APP_HOME;
    private File dbFile;

    public StatsDb() {
        this(Version.APP_HOME);
    }

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

    public void initialize() {
        close();
        dbAvailable = true;

        // Make sure our classpath contains what we need:
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException ignored) {
            logger.severe("SQLite not available");
            dbAvailable = false;
        }

        dbFile = new File(APP_HOME, "stats_tracker.db");
        boolean dbFileExists = dbFile.exists();
        conn = getConnection(dbFile);
        if (! dbFileExists) {
            createDb();
        }
    }

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
}
