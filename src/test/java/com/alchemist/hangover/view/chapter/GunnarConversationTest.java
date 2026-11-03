package com.alchemist.hangover.view.chapter;

import com.alchemist.hangover.gameplay.chapter.EarthChapterRules;
import com.alchemist.hangover.model.GameState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Controlla le due fasi del dialogo di Gunnar: interruzione e caricamento non devono assegnare
 * progressi impropri.
 */
class GunnarConversationTest {
    @AfterEach void reset() { GameState.getInstance().resetState(); }

    @Test void firstFullConversationDoesNotGrantWisdomSecondDoes() {
        GameState state=GameState.getInstance();
        state.resetState();
        String first=EarthChapterRules.gunnarDialoguePath(state);
        assertEquals("/dialogues/ch3_gunnar.txt",first);
        assertNotNull(getClass().getResource(first));
        assertFalse(EarthChapterRules.completeGunnarStage(state,false));
        assertTrue(state.hasHeardGunnarIntroduction());
        assertFalse(state.hasLearnedGunnarWisdom());

        String second=EarthChapterRules.gunnarDialoguePath(state);
        assertEquals("/dialogues/ch3_gunnar_reward.txt",second);
        assertNotNull(getClass().getResource(second));
        assertTrue(EarthChapterRules.completeGunnarStage(state,true));
        assertTrue(state.hasLearnedGunnarWisdom());
        assertEquals("/dialogues/ch3_gunnar_repeat.txt",
                EarthChapterRules.gunnarDialoguePath(state));
        assertNotNull(getClass().getResource(EarthChapterRules.gunnarDialoguePath(state)));
        assertFalse(EarthChapterRules.completeGunnarStage(state,false));
    }

    @Test void interruptNeverGrantsWisdomAndLoadedRewardIsPreserved() {
        GameState state=GameState.getInstance();
        state.resetState();
        assertFalse(state.hasHeardGunnarIntroduction());
        // L’annullamento non esegue la funzione di completamento.
        assertEquals("/dialogues/ch3_gunnar.txt",EarthChapterRules.gunnarDialoguePath(state));
        state.setGunnarWisdomLearned(true);
        assertEquals("/dialogues/ch3_gunnar_repeat.txt",EarthChapterRules.gunnarDialoguePath(state));
        assertFalse(EarthChapterRules.completeGunnarStage(state,false));
        assertFalse(state.hasHeardGunnarIntroduction());
    }
}
