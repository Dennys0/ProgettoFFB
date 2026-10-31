package com.alchemist.hangover.model;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class YmirCauldronRecipeTest {
    @Test
    void onlyThreeCorrectEarthIngredientsWin() {
        assertEquals(YmirCauldronRecipe.Result.SUCCESS,
                YmirCauldronRecipe.evaluate(YmirCauldronRecipe.CORRECT));
        assertEquals(YmirCauldronRecipe.Result.INCOMPLETE,
                YmirCauldronRecipe.evaluate(Set.of("MUSCHIO_MIDGARD")));
        assertEquals(YmirCauldronRecipe.Result.WRONG_MIX,
                YmirCauldronRecipe.evaluate(Set.of(
                        "MUSCHIO_MIDGARD", "LACRIMA_YMIR", "ACQUA_LETE")));
    }

    @Test
    void baitsHaveDistinctFailures() {
        assertEquals(YmirCauldronRecipe.Result.NIDHOGG_ACID,
                YmirCauldronRecipe.evaluate(Set.of(
                        "MUSCHIO_MIDGARD", "LACRIMA_YMIR", "RADICE_NIDHOGG")));
        assertEquals(YmirCauldronRecipe.Result.GIANT_BONE_STONE,
                YmirCauldronRecipe.evaluate(Set.of(
                        "MUSCHIO_MIDGARD", "LACRIMA_YMIR", "POLVERE_OSSA_GIGANTE")));
    }
}
