package com.alchemist.hangover.gameplay.boss;

import com.alchemist.hangover.model.GameState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BossRulesTest {
    @AfterEach void reset() { GameState.getInstance().resetState(); }

    @Test void damageAndCheckpointNeedNoGraphics() {
        GameState state = GameState.getInstance();
        state.resetState();
        assertFalse(BossRules.takeHit(state));
        assertFalse(BossRules.takeHit(state));
        assertTrue(BossRules.takeHit(state));
        assertEquals(0, state.getCurrentHealth());
        assertTrue(state.isBossRetryAvailable());
    }

    @Test void everyBossGrantsExactlyOneReward() {
        GameState state = GameState.getInstance();
        for (int chapter=1; chapter<=4; chapter++) {
            state.resetState();
            BossRules.victory(state, chapter);
            BossRules.victory(state, chapter);
            assertEquals(1, state.getInventory().size());
            assertEquals(chapter == 3, state.isYmirPacified());
        }
        assertThrows(IllegalArgumentException.class, () -> BossRules.victory(state, 5));
    }
}
