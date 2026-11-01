package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GunnarConversationTest {
    @AfterEach void reset() { GameState.getInstance().resetState(); }

    @Test void firstFullConversationDoesNotGrantWisdomSecondDoes() {
        GameState state=GameState.getInstance();
        state.resetState();
        String first=Chapter3Panel.gunnarDialoguePath(state);
        assertEquals("/dialogues/ch3_gunnar.txt",first);
        assertNotNull(getClass().getResource(first));
        assertFalse(Chapter3Panel.completeGunnarStage(state,false));
        assertTrue(state.hasHeardGunnarIntroduction());
        assertFalse(state.hasLearnedGunnarWisdom());

        String second=Chapter3Panel.gunnarDialoguePath(state);
        assertEquals("/dialogues/ch3_gunnar_reward.txt",second);
        assertNotNull(getClass().getResource(second));
        assertTrue(Chapter3Panel.completeGunnarStage(state,true));
        assertTrue(state.hasLearnedGunnarWisdom());
        assertEquals("/dialogues/ch3_gunnar_repeat.txt",
                Chapter3Panel.gunnarDialoguePath(state));
        assertNotNull(getClass().getResource(Chapter3Panel.gunnarDialoguePath(state)));
        assertFalse(Chapter3Panel.completeGunnarStage(state,false));
    }

    @Test void interruptNeverGrantsWisdomAndLoadedRewardIsPreserved() {
        GameState state=GameState.getInstance();
        state.resetState();
        assertFalse(state.hasHeardGunnarIntroduction());
        // Cancelling does not invoke a completion callback.
        assertEquals("/dialogues/ch3_gunnar.txt",Chapter3Panel.gunnarDialoguePath(state));
        state.setGunnarWisdomLearned(true);
        assertEquals("/dialogues/ch3_gunnar_repeat.txt",Chapter3Panel.gunnarDialoguePath(state));
        assertFalse(Chapter3Panel.completeGunnarStage(state,false));
        assertFalse(state.hasHeardGunnarIntroduction());
    }
}
