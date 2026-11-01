package com.alchemist.hangover.dao;

import com.alchemist.hangover.database.DatabaseManager;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.GameSaveSnapshot;
import com.alchemist.hangover.model.Item;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SaveGameDAO {

    public static class SaveSlotInfo {
        private int slotId;
        private String slotName;
        private int currentChapter;
        private int currentRoom;
        private int currentHealth;
        private int maxHealth;
        private boolean hasGivenMap;
        private boolean hasDragonKey;
        private boolean isDragonUnlocked;
        private boolean isBossDefeated;
        private Timestamp lastSaved;

        public SaveSlotInfo(int slotId, String slotName, int currentChapter, int currentRoom, int currentHealth,
                            int maxHealth, boolean hasGivenMap, boolean hasDragonKey,
                            boolean isDragonUnlocked, boolean isBossDefeated, Timestamp lastSaved) {
            this.slotId = slotId;
            this.slotName = slotName;
            this.currentChapter = currentChapter;
            this.currentRoom = currentRoom;
            this.currentHealth = currentHealth;
            this.maxHealth = maxHealth;
            this.hasGivenMap = hasGivenMap;
            this.hasDragonKey = hasDragonKey;
            this.isDragonUnlocked = isDragonUnlocked;
            this.isBossDefeated = isBossDefeated;
            this.lastSaved = lastSaved;
        }

        public int getSlotId() { return slotId; }
        public String getSlotName() { return slotName; }
        public int getCurrentChapter() { return currentChapter; }
        public int getCurrentRoom() { return currentRoom; }
        public int getCurrentHealth() { return currentHealth; }
        public int getMaxHealth() { return maxHealth; }
        public boolean hasGivenMap() { return hasGivenMap; }
        public boolean hasDragonKey() { return hasDragonKey; }
        public boolean isDragonUnlocked() { return isDragonUnlocked; }
        public boolean isBossDefeated() { return isBossDefeated; }
        public Timestamp getLastSaved() { return lastSaved; }

        public String getFormattedDate() {
            if (lastSaved == null) return "-";
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return sdf.format(lastSaved);
        }
    }

    /**
     * Recupera le informazioni di uno slot specifico. Restituisce null se lo slot è vuoto.
     */
    public static SaveSlotInfo getSlot(int slotId) {
        String sql = "SELECT * FROM save_slots WHERE slot_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSlotInfo(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[SaveGameDAO] Errore lettura slot " + slotId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Recupera tutti i salvataggi esistenti ordinati per slot.
     */
    public static List<SaveSlotInfo> getAllSlots() {
        List<SaveSlotInfo> list = new ArrayList<>();
        String sql = "SELECT * FROM save_slots ORDER BY slot_id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToSlotInfo(rs));
            }
        } catch (SQLException e) {
            System.err.println("[SaveGameDAO] Errore lettura lista salvataggi: " + e.getMessage());
        }
        return list;
    }

    /**
     * Salva lo stato attuale del gioco (GameState + Inventario) nello slot specificato.
     */
    public static boolean saveGame(int slotId, String slotName) {
        GameState state = GameState.getInstance();
        if (state.getChapter() == 3) state.ensureYmirRuneSeed();
        return saveGame(slotId, slotName, GameSaveSnapshot.capture(state));
    }

    /** Persiste uno snapshot, senza modificare la partita attiva. */
    public static boolean saveGame(int slotId, String slotName, GameSaveSnapshot snapshot) {
        GameSaveSnapshot state = java.util.Objects.requireNonNull(snapshot, "snapshot")
                .withPersistentRuneSeed();
        String upsertSql = "MERGE INTO save_slots (slot_id, slot_name, current_chapter, current_room, current_health, "
                + "max_health, has_given_map, has_dragon_key, is_dragon_unlocked, is_boss_defeated, "
                + "is_ymir_pacified, ymir_rune_seed, gunnar_wisdom, gunnar_intro_heard, ymir_stele_read, "
                + "ymir_old_fashioned_created, last_saved) "
                + "KEY(slot_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Inizia transazione

            // 1. Salva lo slot principale
            try (PreparedStatement ps = conn.prepareStatement(upsertSql)) {
                ps.setInt(1, slotId);
                ps.setString(2, (slotName == null || slotName.trim().isEmpty()) ? "Partita " + slotId : slotName.trim());
                ps.setInt(3, state.chapter());
                ps.setInt(4, state.room());
                ps.setInt(5, state.health());
                ps.setInt(6, state.maxHealth());
                ps.setBoolean(7, state.givenMapToMartino());
                ps.setBoolean(8, state.dragonKey());
                ps.setBoolean(9, state.dragonUnlocked());
                ps.setBoolean(10, state.bossDefeated());
                ps.setBoolean(11, state.ymirPacified());
                ps.setInt(12, state.ymirRuneSeed());
                ps.setBoolean(13, state.gunnarWisdomLearned());
                ps.setBoolean(14, state.gunnarIntroductionHeard());
                ps.setBoolean(15, state.ymirSteleRead());
                ps.setBoolean(16, state.ymirOldFashionedCreated());
                ps.executeUpdate();
            }

            // 2. Cancella i precedenti oggetti di questo slot nell'inventario
            try (PreparedStatement delPs = conn.prepareStatement("DELETE FROM save_inventory WHERE slot_id = ?")) {
                delPs.setInt(1, slotId);
                delPs.executeUpdate();
            }

            // 3. Inserisci gli oggetti attuali
            try (PreparedStatement insPs = conn.prepareStatement("INSERT INTO save_inventory (slot_id, item_id) VALUES (?, ?)")) {
                for (Item item : state.inventory()) {
                    insPs.setInt(1, slotId);
                    insPs.setString(2, item.getId());
                    insPs.executeUpdate();
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("[SaveGameDAO] Partita salvata con successo nello slot " + slotId);
            return true;
        } catch (SQLException e) {
            System.err.println("[SaveGameDAO] Errore salvataggio slot " + slotId + ": " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) { }
            }
        }
    }

    /**
     * Carica i dati salvati nello slot e aggiorna l'istanza di GameState.
     */
    public static boolean loadGame(int slotId) {
        String sql = "SELECT * FROM save_slots WHERE slot_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Preleva interamente slot e inventario prima di mutare la partita.
                    int chapter = rs.getInt("current_chapter");
                    int room = rs.getInt("current_room");
                    int health = rs.getInt("current_health");
                    int maxHealth = rs.getInt("max_health");
                    boolean givenMap = rs.getBoolean("has_given_map");
                    boolean dragonKey = rs.getBoolean("has_dragon_key");
                    boolean dragonUnlocked = rs.getBoolean("is_dragon_unlocked");
                    boolean bossDefeated = rs.getBoolean("is_boss_defeated");
                    boolean ymirPacified = rs.getBoolean("is_ymir_pacified");
                    int runeSeed = rs.getInt("ymir_rune_seed");
                    boolean gunnarWisdom = rs.getBoolean("gunnar_wisdom");
                    boolean gunnarIntro = rs.getBoolean("gunnar_intro_heard");
                    boolean steleRead = rs.getBoolean("ymir_stele_read");
                    boolean cocktail = rs.getBoolean("ymir_old_fashioned_created");
                    List<Item> inventory = readInventoryForSlot(conn, slotId);
                    GameSaveSnapshot snapshot = new GameSaveSnapshot(chapter, room,
                            health, maxHealth, givenMap, dragonKey, dragonUnlocked,
                            bossDefeated, ymirPacified, runeSeed, gunnarWisdom,
                            gunnarIntro, steleRead, cocktail, inventory);
                    GameState.getInstance().restoreFrom(snapshot);
                    System.out.println("[SaveGameDAO] Partita caricata dallo slot "
                            + slotId + ". Stanza: " + room);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[SaveGameDAO] Errore caricamento slot " + slotId + ": " + e.getMessage());
        }
        return false;
    }

    /** Legge l'inventario per intero prima di applicare il caricamento. */
    private static List<Item> readInventoryForSlot(Connection conn, int slotId) throws SQLException {
        List<Item> inventory = new ArrayList<>();
        String invSql = "SELECT i.id, i.name, i.description, i.icon_path FROM save_inventory si "
                + "JOIN items i ON si.item_id = i.id WHERE si.slot_id = ?";
        try (PreparedStatement invPs = conn.prepareStatement(invSql)) {
            invPs.setInt(1, slotId);
            try (ResultSet rs = invPs.executeQuery()) {
                while (rs.next()) {
                    inventory.add(new Item(rs.getString("id"), rs.getString("name"),
                            rs.getString("description"), rs.getString("icon_path")));
                }
            }
        }
        return inventory;
    }

    /**
     * Elimina un salvataggio dal database.
     */
    public static boolean deleteSave(int slotId) {
        String sql = "DELETE FROM save_slots WHERE slot_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("[SaveGameDAO] Errore cancellazione slot " + slotId + ": " + e.getMessage());
            return false;
        }
    }

    private static SaveSlotInfo mapRowToSlotInfo(ResultSet rs) throws SQLException {
        return new SaveSlotInfo(
                rs.getInt("slot_id"),
                rs.getString("slot_name"),
                rs.getInt("current_chapter"),
                rs.getInt("current_room"),
                rs.getInt("current_health"),
                rs.getInt("max_health"),
                rs.getBoolean("has_given_map"),
                rs.getBoolean("has_dragon_key"),
                rs.getBoolean("is_dragon_unlocked"),
                rs.getBoolean("is_boss_defeated"),
                rs.getTimestamp("last_saved")
        );
    }
}
