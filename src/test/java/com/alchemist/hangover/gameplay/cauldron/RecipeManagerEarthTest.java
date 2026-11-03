package com.alchemist.hangover.gameplay.cauldron;

import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Verifica la ricetta di Ymir secondo la regola comune dei tre ingredienti. */
class RecipeManagerEarthTest {
    @Test void onlyTheCorrectEarthTrioWins() {
        RecipeManager.Recipe recipe=RecipeManager.forChapter(3);
        assertEquals(RecipeManager.Result.SUCCESS,
                RecipeManager.evaluate(3,recipe.correct()));
        assertEquals(RecipeManager.Result.INCOMPLETE,
                RecipeManager.evaluate(3,Set.of("MUSCHIO_MIDGARD")));
        assertEquals(RecipeManager.Result.WRONG_INGREDIENT,
                RecipeManager.evaluate(3,Set.of(
                        "MUSCHIO_MIDGARD","LACRIMA_YMIR","ACQUA_LETE")));
    }
    @Test void bothDecoysNowFollowTheSameGameOverResult() {
        for(String decoy:Set.of("RADICE_NIDHOGG","POLVERE_OSSA_GIGANTE")) {
            assertTrue(RecipeManager.forChapter(3).available().contains(decoy));
            assertEquals(RecipeManager.Result.WRONG_INGREDIENT,
                    RecipeManager.evaluate(3,Set.of(
                            "MUSCHIO_MIDGARD","LACRIMA_YMIR",decoy)));
        }
    }
}
