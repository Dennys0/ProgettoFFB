package com.alchemist.hangover.controller;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CauldronGameServiceTest {
    @AfterEach void resetState() { GameState.getInstance().resetState(); }

    private static Item item(String id) {
        return new Item(id, id, "", "/images/items/item_map.png");
    }

    @Test void successfulChapterOneConsumesOnlyRecipeIngredients() {
        GameState state = GameState.getInstance();
        state.resetState();
        state.setChapter(1);
        state.setCurrentHealth(1);
        state.addItem(item("MAPPA_CIELO"));
        state.addItem(item("NEBBIA_NIFLHEIM"));
        for (String id : com.alchemist.hangover.model.RecipeManager
                .forChapter(1).correct()) state.addItem(item(id));

        assertTrue(CauldronGameService.canOpen(state));
        CauldronGameService.registerSuccess(state, 1);

        assertTrue(state.hasItem("MAPPA_CIELO"), "Un oggetto narrativo non e' un ingrediente");
        assertTrue(state.hasItem("NEBBIA_NIFLHEIM"), "Le esche non sono state usate");
        for (String id : com.alchemist.hangover.model.RecipeManager
                .forChapter(1).correct()) assertFalse(state.hasItem(id));
        assertEquals(3, state.getCurrentHealth());
    }

    @Test void completionAndFailureHaveNoSwingDependencies() {
        GameState state = GameState.getInstance();
        state.resetState();
        state.setChapter(3);
        state.setBossRetryAvailable(true);
        state.setCurrentHealth(2);
        CauldronGameService.registerSuccess(state, 3);
        assertTrue(state.isYmirOldFashionedCreated());
        CauldronGameService.registerFailure(state);
        assertEquals(0, state.getCurrentHealth());
        assertFalse(state.isBossRetryAvailable());
        assertThrows(IllegalArgumentException.class,
                () -> CauldronGameService.registerSuccess(state, 4));
    }
}
