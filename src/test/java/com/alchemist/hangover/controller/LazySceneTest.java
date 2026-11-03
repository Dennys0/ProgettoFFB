package com.alchemist.hangover.controller;

import com.alchemist.hangover.model.GameState;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class LazySceneTest {
    @Test void createsOnceAndPreservesLoadedStateAndRetryCheckpoint() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            GameState state = GameState.getInstance();
            state.resetState(); state.setChapter(4); state.setCurrentRoom(6); state.setBossRetryAvailable(true);
            SceneManager manager = SceneManager.getInstance();
            AtomicInteger calls = new AtomicInteger();
            manager.registerScene("TEST_LAZY", () -> {
                calls.incrementAndGet(); state.setCurrentRoom(1); return new JPanel();
            });
            assertEquals(0, calls.get());
            JPanel first = manager.getPanel("TEST_LAZY");
            assertSame(first, manager.getPanel("TEST_LAZY"));
            assertEquals(1, calls.get());
            assertEquals(6, state.getCurrentRoom());
            assertEquals(4, state.getChapter());
            assertTrue(state.isBossRetryAvailable());
            state.resetState();
        });
    }
}
