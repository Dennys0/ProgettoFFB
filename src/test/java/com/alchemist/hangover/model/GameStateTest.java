package com.alchemist.hangover.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;

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
    void ymirRewardStateIsIndependentFromEarlierBosses() {
        GameState state = GameState.getInstance();
        state.resetState();
        state.setBossDefeated(true);
        assertFalse(state.isYmirPacified(),
                "La vittoria del boss precedente non deve sbloccare Ymir");
        state.setYmirPacified(true);
        assertTrue(state.isYmirPacified());
        state.resetState();
        assertFalse(state.isYmirPacified());
    }

    @Test
    void ritualSeedDoesNotChangeWhilePlayingButResetsOnNewGame() {
        GameState state = GameState.getInstance();
        state.resetState();
        int seed = state.ensureYmirRuneSeed();
        assertTrue(seed > 0);
        assertEquals(seed, state.ensureYmirRuneSeed());
        state.setGunnarWisdomLearned(true);
        state.setYmirSteleRead(true);
        state.setYmirOldFashionedCreated(true);
        state.resetState();
        assertEquals(0, state.getYmirRuneSeed());
        assertFalse(state.hasLearnedGunnarWisdom());
        assertFalse(state.hasReadYmirStele());
        assertFalse(state.isYmirOldFashionedCreated());
    }

    @Test
    void bossCheckpointClearsOnReset() {
        GameState state = GameState.getInstance();
        state.setBossRetryAvailable(true);
        assertTrue(state.isBossRetryAvailable());
        state.resetState();
        assertFalse(state.isBossRetryAvailable());
    }
    @Test
    void inventoryIsReadOnlyAndSetInventoryCopiesTheInput() {
        GameState state = GameState.getInstance();
        state.resetState();
        Item first = new Item("TEST_1", "Prima", "Descrizione", "/test.png");
        Item second = new Item("TEST_2", "Seconda", "Descrizione", "/test.png");
        List<Item> source = new ArrayList<>(List.of(first));
        state.setInventory(source);
        source.clear();
        assertEquals(1, state.getInventory().size());
        assertThrows(UnsupportedOperationException.class,
                () -> state.getInventory().clear());
        state.addItem(first); // duplicate ID must still be rejected
        state.addItem(second);
        assertEquals(2, state.getInventory().size());
        state.clearInventory();
        assertTrue(state.getInventory().isEmpty());
    }

    @Test
    void resetCleansEveryFlagAndKeepsHealthObservers() {
        GameState state = GameState.getInstance();
        state.resetState();
        state.setChapter(3);
        state.setCurrentRoom(7);
        state.setGivenMapToMartino(true);
        state.setHasDragonKey(true);
        state.setDragonUnlocked(true);
        state.setBossDefeated(true);
        state.setYmirPacified(true);
        state.setGunnarIntroductionHeard(true);
        state.setGunnarWisdomLearned(true);
        state.setYmirSteleRead(true);
        state.setYmirOldFashionedCreated(true);
        state.setBossRetryAvailable(true);
        state.ensureYmirRuneSeed();
        state.addItem(new Item("TEST", "Test", "Test", "/test.png"));
        state.setCurrentHealth(1);
        AtomicInteger changed = new AtomicInteger();
        java.beans.PropertyChangeListener listener = event -> changed.incrementAndGet();
        state.addHealthChangeListener(listener);
        try {
            state.resetState();
            assertEquals(1, state.getChapter());
            assertEquals(1, state.getCurrentRoom());
            assertEquals(3, state.getCurrentHealth());
            assertEquals(3, state.getMaxHealth());
            assertEquals(1, changed.get());
            assertTrue(state.getInventory().isEmpty());
            assertFalse(state.hasGivenMapToMartino());
            assertFalse(state.hasDragonKey());
            assertFalse(state.isDragonUnlocked());
            assertFalse(state.isBossDefeated());
            assertFalse(state.isYmirPacified());
            assertFalse(state.hasHeardGunnarIntroduction());
            assertFalse(state.hasLearnedGunnarWisdom());
            assertFalse(state.hasReadYmirStele());
            assertFalse(state.isYmirOldFashionedCreated());
            assertFalse(state.isBossRetryAvailable());
            assertEquals(0, state.getYmirRuneSeed());
        } finally {
            state.removeHealthChangeListener(listener);
        }
    }

}
