package com.alchemist.hangover.dao;

import com.alchemist.hangover.database.DatabaseManager;
import com.alchemist.hangover.model.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ItemDAO {

    /**
     * Recupera un oggetto dal database dato il suo identificatore univoco.
     */
    public static Item getItemById(String itemId) {
        String sql = "SELECT id, name, description, icon_path FROM items WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Item(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("icon_path")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[ItemDAO] Errore lettura item " + itemId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Recupera tutti gli oggetti presenti nel catalogo.
     */
    public static Map<String, Item> getAllItems() {
        Map<String, Item> map = new HashMap<>();
        String sql = "SELECT id, name, description, icon_path FROM items";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Item item = new Item(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("icon_path")
                );
                map.put(item.getId(), item);
            }
        } catch (SQLException e) {
            System.err.println("[ItemDAO] Errore lettura items: " + e.getMessage());
        }
        return map;
    }
}
