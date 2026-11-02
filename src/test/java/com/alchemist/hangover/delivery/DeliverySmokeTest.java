package com.alchemist.hangover.delivery;

import com.alchemist.hangover.gameplay.boss.BossRules;
import com.alchemist.hangover.gameplay.cauldron.CauldronGameService;
import com.alchemist.hangover.gameplay.chapter.ChapterProgression;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
import com.alchemist.hangover.model.RecipeManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Collaudo rapido del percorso obbligatorio: tre boss, tre ricette,
 * due transizioni e il cocktail finale. Non avvia finestre Swing.
 */
class DeliverySmokeTest {

    @AfterEach
    void cleanup() {
        GameState.getInstance().resetState();
    }

    private static void addItem(GameState state, String id) {
        state.addItem(new Item(id, id, "Oggetto di prova", "/images/items/item_map.png"));
    }

    private static void addMissingIngredients(GameState state, int chapter) {
        for (String id : RecipeManager.forChapter(chapter).correct()) {
            if (!state.hasItem(id)) addItem(state, id);
        }
    }

    private static void assertRecipeReady(GameState state, int chapter) {
        assertTrue(CauldronGameService.canOpen(state),
                "Il calderone deve aprirsi quando gli ingredienti sono disponibili");
        assertEquals(RecipeManager.Result.SUCCESS,
                RecipeManager.evaluate(chapter, RecipeManager.forChapter(chapter).correct()));
    }

    @Test
    void completeThreeChapterStoryWithoutLosingProgress() {
        GameState state = GameState.getInstance();
        state.resetState();

        // Cielo: boss -> ingredienti -> cocktail -> Mare.
        addItem(state, "MAPPA_CIELO");
        BossRules.victory(state, 1);
        assertTrue(state.hasItem("SOFFIO_HRAESVELGR"));
        addMissingIngredients(state, 1);
        assertRecipeReady(state, 1);
        assertEquals("TRANSITION_CH1_CH2", CauldronGameService.completionScene(1));
        CauldronGameService.registerSuccess(state, 1);
        assertTrue(state.hasItem("MAPPA_CIELO"),
                "Gli oggetti narrativi non sono ingredienti della ricetta");
        assertFalse(state.hasItem("PIUMA_FOLGORE"));

        var chapter2Save = ChapterProgression.saveAfterCompletion(state, 1);
        assertEquals(2, chapter2Save.chapter());
        assertEquals(1, state.getChapter(), "Salvare non deve avanzare la partita");
        ChapterProgression.advanceToNextChapter(state, 1);
        assertEquals(2, state.getChapter());
        assertFalse(state.isBossDefeated());
        assertEquals(3, state.getCurrentHealth());

        // Mare: la ricompensa di Ran entra nella ricetta.
        BossRules.victory(state, 2);
        assertTrue(state.hasItem("SCHIUMA_AEGIR"));
        addMissingIngredients(state, 2);
        assertRecipeReady(state, 2);
        assertEquals("TRANSITION_CH2_CH3", CauldronGameService.completionScene(2));
        CauldronGameService.registerSuccess(state, 2);
        var chapter3Save = ChapterProgression.saveAfterCompletion(state, 2);
        assertEquals(3, chapter3Save.chapter());
        assertTrue(chapter3Save.ymirRuneSeed() > 0);
        assertEquals(2, state.getChapter());
        ChapterProgression.advanceToNextChapter(state, 2);
        assertEquals(3, state.getChapter());
        assertTrue(state.getYmirRuneSeed() > 0);

        // Terra: Ymir non assegna una ricompensa immediata;
        // la lacrima viene raccolta nella stanza del boss.
        BossRules.victory(state, 3);
        assertTrue(state.isYmirPacified());
        assertFalse(state.hasItem("LACRIMA_YMIR"));
        addMissingIngredients(state, 3);
        assertRecipeReady(state, 3);
        assertEquals("CHAPTER3_COMPLETE", CauldronGameService.completionScene(3));
        CauldronGameService.registerSuccess(state, 3);
        assertTrue(state.isYmirOldFashionedCreated());
        assertEquals(3, ChapterProgression.saveAfterCompletion(state, 3).chapter());
    }

    @Test
    void requiredVisualResourcesAreBundled() {
        for (String resource : new String[] {
                "/images/intro/menu_bg.png",
                "/images/ui/dialogue_box.png",
                "/images/ui/cauldron_bg.png",
                "/images/items/item_breath.png",
                "/images/items/item_foam.png",
                "/images/items/item_ymircry.png",
                "/images/items/cocktail_3.png"}) {
            assertNotNull(getClass().getResource(resource), "Risorsa mancante: " + resource);
        }
    }
}
