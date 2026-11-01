package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.RecipeManager;

import java.awt.Frame;

/** Entry/exit rules common to the three cauldrons; no save-file mutations. */
public final class CauldronController {
    private CauldronController() { }

    public static boolean allowOpening(Frame frame) {
        GameState state = GameState.getInstance();
        if (RecipeManager.canOpen(state.getChapter(), state.getInventory())) return true;
        StoryPromptDialog.Theme theme = state.getChapter() == 2
                ? StoryPromptDialog.Theme.OCEAN : StoryPromptDialog.Theme.SKY;
        StoryPromptDialog.message(frame, theme, "INGREDIENTI INSUFFICIENTI",
                "Prima di usare il calderone raccogli almeno tre ingredienti "
                + "di questo capitolo. Gli oggetti degli altri regni non contano.");
        return false;
    }

    public static void fail(String reason) {
        GameState state = GameState.getInstance();
        state.setCurrentHealth(0);
        state.setBossRetryAvailable(false);
        GameOverPanel panel = (GameOverPanel) SceneManager.getInstance().getPanel("GAMEOVER");
        if (panel != null) panel.showCauldronFailure(reason);
        SceneManager.getInstance().showScene("GAMEOVER");
    }
}
