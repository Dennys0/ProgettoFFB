package com.alchemist.hangover.database;

import com.alchemist.hangover.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SnapshotReadTest {
    @Test void backgroundReadDoesNotMutateActiveGame() throws Exception {
        String previous = System.getProperty(DatabaseManager.TEST_DATABASE_PROPERTY);
        System.setProperty(DatabaseManager.TEST_DATABASE_PROPERTY, "jdbc:h2:mem:snapshot_read;DB_CLOSE_DELAY=-1");
        try {
            DatabaseManager.initDatabase();
            GameState state = GameState.getInstance();
            state.resetState(); state.setChapter(4); state.setCurrentRoom(6);
            assertTrue(SaveGameDAO.saveGame(1, "Test", GameSaveSnapshot.capture(state)));
            state.resetState();
            GameSaveSnapshot loaded = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                try { return SaveGameDAO.readSnapshot(1); }
                catch (java.sql.SQLException ex) { throw new RuntimeException(ex); }
            }).get();
            assertEquals(4, loaded.chapter()); assertEquals(6, loaded.room());
            assertEquals(1, state.getChapter()); assertEquals(1, state.getCurrentRoom());
            assertNull(SaveGameDAO.readSnapshot(99));
        } finally {
            DatabaseManager.close();
            if (previous == null) System.clearProperty(DatabaseManager.TEST_DATABASE_PROPERTY);
            else System.setProperty(DatabaseManager.TEST_DATABASE_PROPERTY, previous);
            GameState.getInstance().resetState();
        }
    }
}
