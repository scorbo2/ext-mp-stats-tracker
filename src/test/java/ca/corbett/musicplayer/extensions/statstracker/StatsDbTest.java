package ca.corbett.musicplayer.extensions.statstracker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

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

    @Test
    public void testResetStats_notEmpty_shouldDeleteAll() {
        // GIVEN a setup with some test data:
        File test1 = new File("test1");
        File test2 = new File("test2");
        statsDb.incrementPlayCount(test1);
        statsDb.incrementPlayCount(test2);

        // WHEN we reset it:
        statsDb.resetStats();

        // THEN the data should be gone:
        assertFalse(statsDb.hasPlayCount(test1));
        assertFalse(statsDb.hasPlayCount(test2));
    }

    @Test
    public void testTop10_empty_shouldReturnEmptyList() {
        // GIVEN a setup with zero data:

        // WHEN we query it:
        List<StatsDb.Entry> top10 = statsDb.getTop10();

        // THEN it should be empty:
        assertEquals(0, top10.size());
    }

    @Test
    public void testTop10_withSmallDataSet_shouldReturnList() {
        // GIVEN a setup with some data:
        for (int i = 0; i < 5; i++) {
            statsDb.incrementPlayCount(new File("test"+i));
        }

        // WHEN we query it:
        List<StatsDb.Entry> top10 = statsDb.getTop10();

        // THEN we should see a list with the expected size:
        assertEquals(5, top10.size());
    }

    @Test
    public void testTop10_withLargeDataSet_shouldReturnList() {
        // GIVEN a setup with lots of data:
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j <= i; j++) {
                statsDb.incrementPlayCount(new File("test"+i));
            }
        }

        // WHEN we query it:
        List<StatsDb.Entry> top10 = statsDb.getTop10();

        // THEN we should see just the top 10 entries:
        assertEquals(10, top10.size());
        assertEquals("test99", top10.get(0).trackFile.getName());
        assertEquals(100, top10.get(0).playCount);
    }
}