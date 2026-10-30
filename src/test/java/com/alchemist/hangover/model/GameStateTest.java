package com.alchemist.hangover.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {
    @AfterEach
    void restoreState() {
        GameState.getInstance().resetState();
    }

    @Test
    void damageNeverCreatesNegativeHealthAndEmitsChanges() {
        GameState state = GameState.getInstance();
        state.resetState();
        AtomicInteger changed = new AtomicInteger();
        java.beans.PropertyChangeListener listener = e -> changed.incrementAndGet();
        state.addHealthChangeListener(listener);
        try {
            state.takeDamage();
            state.takeDamage();
            state.takeDamage();
            state.takeDamage();
            assertEquals(0, state.getCurrentHealth());
            assertEquals(3, changed.get());
            state.resetHealth();
            assertEquals(3, state.getCurrentHealth());
            assertEquals(4, changed.get());
        } finally {
            state.removeHealthChangeListener(listener);
        }
    }

    @Test
    void bossCheckpointClearsOnReset() {
        GameState state = GameState.getInstance();
        state.setBossRetryAvailable(true);
        assertTrue(state.isBossRetryAvailable());
        state.resetState();
        assertFalse(state.isBossRetryAvailable());
    }
}
