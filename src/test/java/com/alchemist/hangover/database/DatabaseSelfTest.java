package com.alchemist.hangover.database;

import com.alchemist.hangover.dao.ItemDAO;
import com.alchemist.hangover.dao.SaveGameDAO;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test isolati: NON aprono, modificano o cancellano mai saves/alchemist.mv.db.
 * Anche lo slot 1 esiste esclusivamente nel database jdbc:h2:mem.
 */
public class DatabaseSelfTest {
    private static final int TEST_SLOT = 1;

    @BeforeAll
    static void setupDatabase() {
        System.setProperty(DatabaseManager.TEST_DATABASE_PROPERTY,
                "jdbc:h2:mem:alchemist_test;DB_CLOSE_DELAY=-1");
        DatabaseManager.initDatabase();
    }

    @AfterAll
    static void shutdownDatabase() {
        DatabaseManager.close();
        System.clearProperty(DatabaseManager.TEST_DATABASE_PROPERTY);
    }

    @BeforeEach
    void resetGame() {
        GameState.getInstance().resetState();
    }

    @AfterEach
    void cleanupTestSlot() {
        SaveGameDAO.deleteSave(TEST_SLOT);
        GameState.getInstance().resetState();
    }

    @Test
    void catalogContainsItemsFromBothChapters() {
        Item map = ItemDAO.getItemById("MAPPA_CIELO");
        assertNotNull(map);
        assertEquals("Mappa del Cielo", map.getName());
        assertNotNull(ItemDAO.getItemById("DIARIO_CAPITANO"));
        assertNotNull(ItemDAO.getItemById("SCHIUMA_AEGIR"));
        for (String itemId : java.util.List.of(
                "MUSCHIO_MIDGARD", "RADICE_NIDHOGG",
                "FRAMMENTO_YGGDRASIL", "POLVERE_OSSA_GIGANTE", "LACRIMA_YMIR")) {
            Item item = ItemDAO.getItemById(itemId);
            assertNotNull(item, "Oggetto del capitolo 3 mancante: " + itemId);
            assertNotNull(getClass().getResource(item.getIconPath()),
                    "Icona assente: " + item.getIconPath());
        }
    }

    @Test
    void savesAndRestoresChapterHealthAndInventory() {
        GameState state = GameState.getInstance();
        state.setChapter(2);
        state.setCurrentRoom(4);
        state.takeDamage();
        state.setGivenMapToMartino(true);
        state.addItem(ItemDAO.getItemById("MAPPA_CIELO"));

        assertTrue(SaveGameDAO.saveGame(TEST_SLOT, "Test in memoria"));
        assertEquals(2, SaveGameDAO.getSlot(TEST_SLOT).getCurrentChapter());

        state.resetState();
        assertTrue(SaveGameDAO.loadGame(TEST_SLOT));
        assertEquals(2, state.getChapter());
        assertEquals(4, state.getCurrentRoom());
        assertEquals(2, state.getCurrentHealth());
        assertTrue(state.hasGivenMapToMartino());
        assertTrue(state.hasItem("MAPPA_CIELO"));
    }

    @Test
    void chapter3ItemsCanBeSavedAndLoaded() {
        GameState state = GameState.getInstance();
        state.setChapter(3);
        state.setCurrentRoom(5);
        state.addItem(ItemDAO.getItemById("MUSCHIO_MIDGARD"));
        state.addItem(ItemDAO.getItemById("RADICE_NIDHOGG"));
        state.addItem(ItemDAO.getItemById("FRAMMENTO_YGGDRASIL"));
        state.addItem(ItemDAO.getItemById("POLVERE_OSSA_GIGANTE"));
        state.setYmirPacified(true);
        state.setYmirRuneSeed(9345);
        state.setGunnarWisdomLearned(true);
        state.setYmirSteleRead(true);
        state.setYmirOldFashionedCreated(true);

        assertTrue(SaveGameDAO.saveGame(TEST_SLOT, "Terra: test"));
        state.resetState();
        assertTrue(SaveGameDAO.loadGame(TEST_SLOT));
        assertEquals(3, state.getChapter());
        assertEquals(5, state.getCurrentRoom());
        assertTrue(state.isYmirPacified(), "Vittoria di Ymir non ripristinata");
        assertEquals(9345, state.getYmirRuneSeed());
        assertTrue(state.hasLearnedGunnarWisdom());
        assertTrue(state.hasReadYmirStele());
        assertTrue(state.isYmirOldFashionedCreated());
        for (String id : java.util.List.of("MUSCHIO_MIDGARD",
                "RADICE_NIDHOGG", "FRAMMENTO_YGGDRASIL", "POLVERE_OSSA_GIGANTE")) {
            assertTrue(state.hasItem(id), "Oggetto non ripristinato: " + id);
        }
    }

    @Test
    void deletesOnlyTheTestSlot() {
        assertTrue(SaveGameDAO.saveGame(TEST_SLOT, "Eliminazione di prova"));
        assertTrue(SaveGameDAO.deleteSave(TEST_SLOT));
        assertNull(SaveGameDAO.getSlot(TEST_SLOT));
    }
}
