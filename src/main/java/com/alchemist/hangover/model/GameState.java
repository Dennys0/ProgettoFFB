package com.alchemist.hangover.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class GameState {

    private static GameState instance;

    private final PropertyChangeSupport healthChanges = new PropertyChangeSupport(this);
    private int currentHealth = 3;
    private int maxHealth = 3;
    private int currentChapter = 1;
    private int currentRoom = 1; // Traccia della stanza attuale per i salvataggi

    private List<Item> inventory;
    private boolean givenMapToMartino = false;
    private boolean hasDragonKey = false;
    private boolean dragonUnlocked = false; // Flag per sblocco polena/drago
    private boolean bossDefeated = false;

    private GameState() {
        inventory = new ArrayList<>();
    }

    public static GameState getInstance() {
        if (instance == null) {
            instance = new GameState();
        }
        return instance;
    }

    // --- Gestione Stanza & Capitolo ---
    public int getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(int room) {
        this.currentRoom = room;
    }

    public int getChapter() {
        return currentChapter;
    }

    public void setChapter(int chapter) {
        this.currentChapter = chapter;
    }

    // --- Gestione Salute ---
    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int health) {
        int newHealth = Math.max(0, health);
        if (currentHealth != newHealth) {
            currentHealth = newHealth;
            notifyHealthChanged();
        }
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        if (this.maxHealth != maxHealth) {
            this.maxHealth = maxHealth;
            notifyHealthChanged();
        }
    }

    public void takeDamage() {
        if (currentHealth > 0) {
            setCurrentHealth(currentHealth - 1);
        }
    }

    public void resetHealth() {
        setCurrentHealth(maxHealth);
    }

    // Gli indicatori grafici si aggiornano anche mentre un JDialog modale e' aperto.
    public void addHealthChangeListener(PropertyChangeListener listener) {
        healthChanges.addPropertyChangeListener("health", listener);
    }

    public void removeHealthChangeListener(PropertyChangeListener listener) {
        healthChanges.removePropertyChangeListener("health", listener);
    }

    private void notifyHealthChanged() {
        healthChanges.firePropertyChange("health", null, new int[]{currentHealth, maxHealth});
    }

    // --- Gestione Inventario ---
    public List<Item> getInventory() {
        return inventory;
    }

    public void setInventory(List<Item> items) {
        this.inventory = items;
    }

    public void addItem(Item item) {
        if (!hasItem(item.getId())) {
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

    // --- Flag di Gioco ---
    public boolean hasGivenMapToMartino() { return givenMapToMartino; }
    public void setGivenMapToMartino(boolean val) { this.givenMapToMartino = val; }

    public boolean hasDragonKey() { return hasDragonKey; }
    public void setHasDragonKey(boolean val) { this.hasDragonKey = val; }

    public boolean isDragonUnlocked() { return dragonUnlocked; }
    public void setDragonUnlocked(boolean val) { this.dragonUnlocked = val; }

    public boolean isBossDefeated() { return bossDefeated; }
    public void setBossDefeated(boolean val) { this.bossDefeated = val; }

    // --- Reset Stato Generale ---
    public void resetState() {
        int oldHealth = currentHealth;
        int oldMaxHealth = maxHealth;
        this.currentHealth = 3;
        this.maxHealth = 3;
        this.currentChapter = 1;
        this.currentRoom = 1;
        this.inventory.clear();
        this.givenMapToMartino = false;
        this.hasDragonKey = false;
        this.dragonUnlocked = false;
        this.bossDefeated = false;
        if (oldHealth != currentHealth || oldMaxHealth != maxHealth) {
            notifyHealthChanged();
        }
    }
}