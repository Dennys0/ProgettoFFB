package com.alchemist.hangover.model;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    private static GameState instance;

    private int maxHealth = 3;
    private int currentHealth = 3;
    private int currentRoom = 1;
    private List<Item> inventory;

    // Flag di avanzamento del Capitolo 1
    private boolean hasGivenMapToMartino = false;
    private boolean hasDragonKey = false;
    private boolean isDragonUnlocked = false;
    private boolean isBossDefeated = false;

    private GameState() {
        inventory = new ArrayList<>();
    }

    public static GameState getInstance() {
        if (instance == null) {
            instance = new GameState();
        }
        return instance;
    }

    // Gestione Salute
    public int getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(int currentHealth) { this.currentHealth = currentHealth; }

    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }

    public void takeDamage() {
        if (currentHealth > 0) {
            currentHealth--;
        }
    }

    public void resetHealth() {
        currentHealth = maxHealth;
    }

    // Gestione Stanza
    public int getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(int currentRoom) { this.currentRoom = currentRoom; }

    // Gestione Inventario
    public List<Item> getInventory() { return inventory; }

    public void addItem(Item item) {
        if (item != null && !hasItem(item.getId())) {
            inventory.add(item);
        }
    }

    public void removeItem(Item item) {
        inventory.remove(item);
    }

    public boolean hasItem(String itemId) {
        return inventory.stream().anyMatch(i -> i.getId().equalsIgnoreCase(itemId));
    }

    public Item getItemById(String itemId) {
        return inventory.stream()
                .filter(i -> i.getId().equalsIgnoreCase(itemId))
                .findFirst()
                .orElse(null);
    }

    // Gestione Flag
    public boolean hasGivenMapToMartino() { return hasGivenMapToMartino; }
    public void setGivenMapToMartino(boolean val) { this.hasGivenMapToMartino = val; }

    public boolean hasDragonKey() { return hasDragonKey; }
    public void setHasDragonKey(boolean val) { this.hasDragonKey = val; }

    public boolean isDragonUnlocked() { return isDragonUnlocked; }
    public void setDragonUnlocked(boolean val) { this.isDragonUnlocked = val; }

    public boolean isBossDefeated() { return isBossDefeated; }
    public void setBossDefeated(boolean val) { this.isBossDefeated = val; }

    public void resetState() {
        currentHealth = maxHealth;
        currentRoom = 1;
        inventory.clear();
        hasGivenMapToMartino = false;
        hasDragonKey = false;
        isDragonUnlocked = false;
        isBossDefeated = false;
    }
}
