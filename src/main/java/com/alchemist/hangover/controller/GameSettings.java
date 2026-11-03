package com.alchemist.hangover.controller;

import java.util.prefs.Preferences;

/** Preferenze locali indipendenti dal database H2 delle partite. */
public final class GameSettings {
    private static final GameSettings INSTANCE = new GameSettings();
    private final Preferences preferences;
    private volatile boolean typingEnabled;
    private volatile boolean ambienceEnabled = true;
    private volatile boolean soundEnabled = true;
    private volatile boolean transitionsEnabled = true;

    private GameSettings() {
        Preferences store = null;
        try {
            store = Preferences.userNodeForPackage(GameSettings.class);
            typingEnabled = store.getBoolean("audio.typing", false);
            ambienceEnabled = store.getBoolean("audio.ambience", true);
            soundEnabled = store.getBoolean("audio.enabled", true);
            transitionsEnabled = store.getBoolean("transitions.enabled", true);
        } catch (SecurityException ignored) {
            // In ambienti con permessi limitati restano valide le impostazioni di sessione.
        }
        preferences = store;
    }

    public static GameSettings getInstance() { return INSTANCE; }
    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isTransitionsEnabled() { return transitionsEnabled; }

    public boolean isTypingEnabled() { return typingEnabled; }

    public void setTypingEnabled(boolean enabled) {
        typingEnabled = enabled;
        store("audio.typing", enabled);
    }

    public boolean isAmbienceEnabled() { return ambienceEnabled; }

    /**
     * Salva la preferenza del sottofondo e aggiorna la riproduzione della scena.
     */
    public void setAmbienceEnabled(boolean enabled) {
        ambienceEnabled = enabled;
        store("audio.ambience", enabled);
        AudioManager.getInstance().refreshSettings();
    }

    /**
     * Aggiorna il comando generale dell’audio, salva la preferenza e riapplica le impostazioni
     * alla scena.
     */
    public void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
        store("audio.enabled", enabled);
        AudioManager.getInstance().refreshSettings();
    }

    public void setTransitionsEnabled(boolean enabled) {
        transitionsEnabled = enabled;
        store("transitions.enabled", enabled);
    }

    /**
     * Memorizza la preferenza sul dispositivo. Se l’accesso è negato, resta valida la modifica
     * in memoria per questa sessione.
     */
    private void store(String key, boolean value) {
        if (preferences != null) {
            try {
                preferences.putBoolean(key, value);
            } catch (SecurityException ignored) {
                // Permesso non disponibile: l'impostazione continua a valere per la sessione.
            }
        }
    }
}
