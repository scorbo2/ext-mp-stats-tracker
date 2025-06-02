package ca.corbett.musicplayer.extensions.statstracker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class StatsDbTest {

    StatsDb statsDb;

    @BeforeEach
    public void setup() throws Exception {
        statsDb = new StatsDb(Files.createTempDirectory("statsDb").toFile());
    }

    @AfterEach
    public void tearDown() {
        statsDb.close();
    }

    @Test
    public void testHasPlayCount_empty_shouldReturnFalse() {
        // GIVEN a new setup with no data:

        // WHEN we query for literally anything:
        boolean actual = statsDb.hasPlayCount(new File("hello"));

        // THEN we should get false:
        assertFalse(actual);
    }

    @Test
    public void testGetPlayCount_notEmpty_shouldReturnCount() {
        // GIVEN a setup with some data:
        statsDb.incrementPlayCount(new File("test"));
        statsDb.incrementPlayCount(new File("test"));

        // WHEN we query for it:
        int playCount = statsDb.getPlayCount(new File("test"));

        // THEN we should get good results:
        assertTrue(statsDb.hasPlayCount(new File("test")));
        assertEquals(2, playCount);
    }
}