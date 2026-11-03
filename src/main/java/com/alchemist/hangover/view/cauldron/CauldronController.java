package com.alchemist.hangover.view.cauldron;

import com.alchemist.hangover.controller.AudioManager;
import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.gameplay.cauldron.CauldronGameService;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.view.dialogue.StoryPromptDialog;
import com.alchemist.hangover.view.story.EpiloguePanel;
import com.alchemist.hangover.view.story.GameOverPanel;
import java.awt.Frame;
import java.awt.Window;
import javax.swing.SwingUtilities;

/** Coordina apertura ed esito dei quattro calderoni, senza scrivere nei salvataggi. */
public final class CauldronController {
    private CauldronController() { }

    static StoryPromptDialog.Theme themeForChapter(int chapter) {
        return switch (chapter) {
            case 2 -> StoryPromptDialog.Theme.OCEAN;
            case 3 -> StoryPromptDialog.Theme.EARTH;
            case 4 -> StoryPromptDialog.Theme.FIRE;
            default -> StoryPromptDialog.Theme.SKY;
        };
    }

    /**
     * Controlla gli ingredienti e mostra l’avviso con il tema del capitolo quando mancano.
     */
    public static boolean allowOpening(Frame frame) {
        GameState state = GameState.getInstance();
        if (CauldronGameService.canOpen(state)) return true;
        StoryPromptDialog.Theme theme = themeForChapter(state.getChapter());
        StoryPromptDialog.message(frame, theme, "INGREDIENTI INSUFFICIENTI",
                "Prima di usare il calderone raccogli almeno tre ingredienti "
                + "di questo capitolo. Gli oggetti degli altri regni non contano.");
        return false;
    }

    /** Applica il successo e apre la scena conclusiva del capitolo.
     * La rappresentazione del cocktail è affidata alla scena di destinazione. */
    public static String completionScene(int chapter) {
        return CauldronGameService.completionScene(chapter);
    }

    public static void complete(int chapter) {
        String scene = completionScene(chapter);
        CauldronGameService.registerSuccess(GameState.getInstance(), chapter);
        AudioManager.getInstance().playEffect(AudioManager.Effect.RECIPE_READY);
        if (chapter == 4) {
            EpiloguePanel finale = (EpiloguePanel) SceneManager.getInstance().getPanel("EPILOGUE");
            if (finale != null) finale.restart();
        }
        SceneManager.getInstance().showScene(scene);
    }

    /**
     * Registra il fallimento e prepara la schermata di sconfitta con ripresa della ricetta.
     */
    public static void fail(String reason) {
        GameState state = GameState.getInstance();
        CauldronGameService.registerFailure(state);
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
