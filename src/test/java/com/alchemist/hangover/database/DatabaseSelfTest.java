package com.alchemist.hangover.database;

import com.alchemist.hangover.dao.ItemDAO;
import com.alchemist.hangover.dao.SaveGameDAO;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;

public class DatabaseSelfTest {
    public static void main(String[] args) {
        System.out.println("--- Inizio Test H2 Database ---");
        DatabaseManager.initDatabase();

        // Test 1: Catalogo Oggetti
        Item mapItem = ItemDAO.getItemById("MAPPA_CIELO");
        if (mapItem == null || !"Mappa del Cielo".equals(mapItem.getName())) {
            throw new RuntimeException("Test 1 Fallito: Mappa del cielo non trovata!");
        }
        System.out.println("Test 1 PASSATO: Catalogo oggetti OK (" + mapItem.getName() + ")");

        // Test 2: Salvataggio GameState
        GameState state = GameState.getInstance();
        state.resetState();
        state.setCurrentRoom(4);
        state.takeDamage(); // 2 cuori
        state.setGivenMapToMartino(true);
        state.addItem(mapItem);

        boolean saved = SaveGameDAO.saveGame(1, "Partita Test Alchimista");
        if (!saved) throw new RuntimeException("Test 2 Fallito: Errore nel salvataggio!");
        System.out.println("Test 2 PASSATO: Salvataggio effettuato!");

        // Test 3: Reset e Caricamento
        state.resetState();
        if (state.getCurrentRoom() != 1 || state.getInventory().size() != 0) {
            throw new RuntimeException("Reset fallito");
        }

        boolean loaded = SaveGameDAO.loadGame(1);
        if (!loaded) throw new RuntimeException("Test 3 Fallito: Errore caricamento!");
        if (state.getCurrentRoom() != 4 || state.getCurrentHealth() != 2 || !state.hasGivenMapToMartino()) {
            throw new RuntimeException("Test 3 Fallito: Dati di stato non corrispondenti!");
        }
        if (!state.hasItem("MAPPA_CIELO")) {
            throw new RuntimeException("Test 3 Fallito: Inventario non ripristinato!");
        }
        System.out.println("Test 3 PASSATO: Caricamento ripristinato con successo!");

        // Test 4: Eliminazione
        SaveGameDAO.deleteSave(1);
        if (SaveGameDAO.getSlot(1) != null) {
            throw new RuntimeException("Test 4 Fallito: Cancellazione slot non riuscita!");
        }
        System.out.println("Test 4 PASSATO: Eliminazione slot OK!");

        DatabaseManager.close();
        System.out.println("--- TUTTI I TEST H2 COMPLETATI CON SUCCESSO! ---");
    }
}
