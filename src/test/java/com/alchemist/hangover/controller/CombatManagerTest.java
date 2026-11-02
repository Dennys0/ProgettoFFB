package com.alchemist.hangover.controller;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.gameplay.boss.BossRewardCatalog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CombatManagerTest {
    @AfterEach void reset(){ GameState.getInstance().resetState(); }

    @Test void exactlyThreeHitsCauseGameOverWithBossCheckpoint() {
        GameState state=GameState.getInstance();
        state.resetState();
        assertFalse(CombatManager.takeHit("Prima ferita"));
        assertEquals(2,state.getCurrentHealth());
        assertFalse(CombatManager.takeHit("Seconda ferita"));
        assertEquals(1,state.getCurrentHealth());
        assertTrue(CombatManager.takeHit("Terza ferita"));
        assertEquals(0,state.getCurrentHealth());
        assertTrue(state.isBossRetryAvailable());
    }

    @Test void victoryMarksChapterSpecificBossAndClearsRetry() {
        GameState state=GameState.getInstance();
        for(int chapter=1; chapter<=4;chapter++){
            state.resetState();
            state.setBossRetryAvailable(true);
            CombatManager.victory(chapter);
            assertFalse(state.isBossRetryAvailable());
            if(chapter==3){
                assertTrue(state.isYmirPacified());
                assertFalse(state.isBossDefeated());
                assertTrue(BossRewardCatalog.immediateReward(3).isEmpty());
            }else{
                assertTrue(state.isBossDefeated());
                assertFalse(state.isYmirPacified());
                String reward = BossRewardCatalog.immediateReward(chapter)
                        .orElseThrow().getId();
                assertTrue(state.hasItem(reward), "La ricompensa viene assegnata dal manager");
                CombatManager.victory(chapter);
                assertEquals(1, state.getInventory().size(), "Premio non duplicato");
            }
        }
        assertThrows(IllegalArgumentException.class,()->CombatManager.victory(5));
    }
}
