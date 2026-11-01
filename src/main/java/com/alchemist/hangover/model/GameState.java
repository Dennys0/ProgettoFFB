package com.alchemist.hangover.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class GameState {

    private static GameState instance;

    private final PropertyChangeSupport healthChanges = new PropertyChangeSupport(this);
    private int currentHealth = 3;
    private int maxHealth = 3;
    private int currentChapter = 1;
    private int currentRoom = 1; // Traccia della stanza attuale per i salvataggi

    private final List<Item> inventory = new ArrayList<>();
    private boolean givenMapToMartino = false;
    private boolean hasDragonKey = false;
    private boolean dragonUnlocked = false; // Flag per sblocco polena/drago
    private boolean bossDefeated = false;
    // Stato separato: Rán e Hræsvelgr non devono sbloccare la ricompensa di Ymir.
    private boolean ymirPacified = false;
    private int ymirRuneSeed = 0;
    private boolean gunnarWisdomLearned = false;
    // Tappa intermedia della conversazione: ora persiste nei salvataggi H2.
    private boolean gunnarFirstDialogueHeard = false;
    private boolean ymirSteleRead = false;
    private boolean ymirOldFashionedCreated = false;
    // Checkpoint di sessione: non viene serializzato nei salvataggi H2.
    private boolean bossRetryAvailable = false;

    private GameState() { }

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
    /**
     * Restituisce una fotografia in sola lettura dell'inventario.
     * I chiamanti non possono piu' cancellare oggetti senza passare dal modello.
     */
    public List<Item> getInventory() {
        return List.copyOf(inventory);
    }

    /** Copia difensiva: modifiche alla lista esterna non alterano la partita. */
    public void setInventory(List<Item> items) {
        List<Item> snapshot = List.copyOf(Objects.requireNonNull(items, "items"));
        inventory.clear();
        snapshot.forEach(this::addItem);
    }

    public void clearInventory() {
        inventory.clear();
    }

    public void addItem(Item item) {
        Item valid = Objects.requireNonNull(item, "item");
        if (!hasItem(valid.getId())) {
            inventory.add(valid);
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

    /**
     * The random seed is created once per Chapter III adventure and is persisted
     * in H2, so Gunnar, the stele and the boss share the exact same ritual.
     */
    public int ensureYmirRuneSeed() {
        if (ymirRuneSeed == 0) {
            ymirRuneSeed = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        }
        return ymirRuneSeed;
    }

    public int getYmirRuneSeed() { return ymirRuneSeed; }
    public void setYmirRuneSeed(int seed) { this.ymirRuneSeed = seed; }

    public boolean hasHeardGunnarIntroduction() { return gunnarFirstDialogueHeard; }
    public void setGunnarIntroductionHeard(boolean heard) { gunnarFirstDialogueHeard = heard; }

    public boolean hasLearnedGunnarWisdom() { return gunnarWisdomLearned; }
    public void setGunnarWisdomLearned(boolean value) { gunnarWisdomLearned = value; }

    public boolean hasReadYmirStele() { return ymirSteleRead; }
    public void setYmirSteleRead(boolean value) { ymirSteleRead = value; }

    public boolean isYmirOldFashionedCreated() { return ymirOldFashionedCreated; }
    public void setYmirOldFashionedCreated(boolean value) { ymirOldFashionedCreated = value; }

    public boolean isYmirPacified() { return ymirPacified; }
    public void setYmirPacified(boolean value) { this.ymirPacified = value; }

    public boolean isBossDefeated() { return bossDefeated; }
    public void setBossDefeated(boolean val) { this.bossDefeated = val; }

    public boolean isBossRetryAvailable() { return bossRetryAvailable; }
    public void setBossRetryAvailable(boolean val) { this.bossRetryAvailable = val; }

    // --- Reset Stato Generale ---
    /** Ripristina una nuova partita senza sostituire l'oggetto GameState singleton. */
    public void resetState() {
        int previousHealth = currentHealth;
        int previousMaxHealth = maxHealth;
        resetExploration();
        resetStoryProgress();
        resetYmirProgress();
        maxHealth = 3;
        currentHealth = 3;
        // Un solo evento: l'HUD non viene aggiornato su uno stato intermedio.
        if (previousHealth != currentHealth || previousMaxHealth != maxHealth) {
            notifyHealthChanged();
        }
    }

    private void resetExploration() {
        currentChapter = 1;
        currentRoom = 1;
        clearInventory();
    }

    private void resetStoryProgress() {
        givenMapToMartino = false;
        hasDragonKey = false;
        dragonUnlocked = false;
        bossDefeated = false;
        bossRetryAvailable = false;
    }

    private void resetYmirProgress() {
        ymirPacified = false;
        ymirRuneSeed = 0;
        gunnarWisdomLearned = false;
        gunnarFirstDialogueHeard = false;
        ymirSteleRead = false;
        ymirOldFashionedCreated = false;
    }
}