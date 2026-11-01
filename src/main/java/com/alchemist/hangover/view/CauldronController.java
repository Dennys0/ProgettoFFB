package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.RecipeManager;
import com.alchemist.hangover.controller.AudioManager;

import java.awt.Frame;
import java.awt.Window;
import javax.swing.SwingUtilities;

/** Entry/exit rules common to the three cauldrons; no save-file mutations. */
public final class CauldronController {
    private CauldronController() { }

    static StoryPromptDialog.Theme themeForChapter(int chapter) {
        return switch (chapter) {
            case 2 -> StoryPromptDialog.Theme.OCEAN;
            case 3 -> StoryPromptDialog.Theme.EARTH;
            default -> StoryPromptDialog.Theme.SKY;
        };
    }

    public static boolean allowOpening(Frame frame) {
        GameState state = GameState.getInstance();
        if (RecipeManager.canOpen(state.getChapter(), state.getInventory())) return true;
        StoryPromptDialog.Theme theme = themeForChapter(state.getChapter());
        StoryPromptDialog.message(frame, theme, "INGREDIENTI INSUFFICIENTI",
                "Prima di usare il calderone raccogli almeno tre ingredienti "
                + "di questo capitolo. Gli oggetti degli altri regni non contano.");
        return false;
    }

    /** A common outcome for all three cauldrons; no pickup popup is shown.
     * Completion scenes are responsible for the illustrated drink and actions. */
    static String completionScene(int chapter) {
        return switch (chapter) {
            case 1 -> "TRANSITION_CH1_CH2";
            case 2 -> "TRANSITION_CH2_CH3";
            case 3 -> "CHAPTER3_COMPLETE";
            default -> throw new IllegalArgumentException("Capitolo senza calderone: " + chapter);
        };
    }

    public static void complete(int chapter) {
        String scene = completionScene(chapter);
        GameState state = GameState.getInstance();
        if (chapter == 3) {
            state.setYmirOldFashionedCreated(true);
        } else if (chapter == 1) {
            // Preserve the original chapter-one transition behavior.
            state.clearInventory();
        }
        state.resetHealth();
        AudioManager.getInstance().playEffect(AudioManager.Effect.SUCCESS);
        SceneManager.getInstance().showScene(scene);
    }

    public static void fail(String reason) {
        GameState state = GameState.getInstance();
        state.setCurrentHealth(0);
        state.setBossRetryAvailable(false);
        GameOverPanel panel = (GameOverPanel) SceneManager.getInstance().getPanel("GAMEOVER");
        if (panel != null) panel.showCauldronFailure(reason);
        // Prima la notifica fantasy, poi la schermata Game Over.
        // In un test headless senza finestra non viene aperto alcun JDialog.
        Window window = SwingUtilities.getWindowAncestor(
                SceneManager.getInstance().getMainPanel());
        if (window instanceof Frame owner && owner.isShowing()) {
            StoryPromptDialog.message(owner, themeForChapter(state.getChapter()),
                    "MISCELA FALLITA",
                    reason + "\n\nIl calderone trabocca: Arthur perde i tre cuori. "
                    + "Potrai riprovare senza perdere gli ingredienti.");
        }
        SceneManager.getInstance().showScene("GAMEOVER");
    }
}
