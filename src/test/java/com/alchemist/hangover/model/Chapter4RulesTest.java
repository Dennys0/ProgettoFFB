package com.alchemist.hangover.model;

import com.alchemist.hangover.gameplay.boss.BossRules;
import com.alchemist.hangover.gameplay.cauldron.CauldronGameService;
import com.alchemist.hangover.gameplay.cauldron.RecipeManager;
import com.alchemist.hangover.gameplay.chapter.ChapterProgression;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Chapter4RulesTest {
    @Test void correctFlambeRequiresEmberAsLastIngredient() {
        assertEquals(RecipeManager.Result.SUCCESS,
                RecipeManager.evaluate(4, List.of("MAGMA_SVARTALFAHEIMR", "CENERE_FENRIR", "BRACE_MUSPELHEIM")));
        assertEquals(RecipeManager.Result.SUCCESS,
                RecipeManager.evaluate(4, List.of("CENERE_FENRIR", "MAGMA_SVARTALFAHEIMR", "BRACE_MUSPELHEIM")));
        assertEquals(RecipeManager.Result.WRONG_INGREDIENT,
                RecipeManager.evaluate(4, List.of("BRACE_MUSPELHEIM", "MAGMA_SVARTALFAHEIMR", "CENERE_FENRIR")));
        assertTrue(RecipeManager.failureText(4, List.of("LACRIMA_HEL", "CENERE_FENRIR", "BRACE_MUSPELHEIM"), List.of()).contains("shock termico"));
    }

    @Test void chapterThreeProgressesIntoFourAndTheFinalDrinkPersistsInSnapshot() {
        GameState state = GameState.getInstance();
        state.resetState();
        try {
            state.setChapter(3);
            state.setYmirOldFashionedCreated(true);
            var afterThird = ChapterProgression.saveAfterCompletion(state, 3);
            assertEquals(4, afterThird.chapter());
            assertEquals(3, state.getChapter());
            ChapterProgression.advanceToNextChapter(state, 3);
            assertEquals(4, state.getChapter());
            assertFalse(state.isBossDefeated());
            BossRules.victory(state, 4);
            assertTrue(state.hasItem("BRACE_MUSPELHEIM"));
            CauldronGameService.registerSuccess(state, 4);
            assertTrue(state.isRagnarokFlambeCreated());
            assertTrue(GameSaveSnapshot.capture(state).ragnarokFlambeCreated());
            assertEquals("EPILOGUE", CauldronGameService.completionScene(4));
        } finally { state.resetState(); }
    }
}
