package com.alchemist.hangover.controller;

import java.util.prefs.Preferences;

/** Preferenze locali indipendenti dal database H2 delle partite. */
public final class GameSettings {
    private static final GameSettings INSTANCE = new GameSettings();
    private final Preferences preferences;
    private boolean soundEnabled = true;
    private boolean transitionsEnabled = true;

    private GameSettings() {
        Preferences store = null;
        try {
            store = Preferences.userNodeForPackage(GameSettings.class);
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

    public void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
        store("audio.enabled", enabled);
    }

    public void setTransitionsEnabled(boolean enabled) {
        transitionsEnabled = enabled;
        store("transitions.enabled", enabled);
    }

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
