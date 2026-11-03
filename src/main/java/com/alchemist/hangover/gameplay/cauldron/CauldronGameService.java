package com.alchemist.hangover.gameplay.cauldron;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
import java.util.Objects;

/**
 * Regole del calderone senza componenti Swing, suoni o navigazione tra scene.
 * La view decide come mostrare il risultato; il modello conserva il progresso.
 */
public final class CauldronGameService {
    private CauldronGameService() { }

    /**
     * Verifica la disponibilità di tre ingredienti ammessi per il capitolo corrente, incluse le
     * esche.
     */
    public static boolean canOpen(GameState state) {
        Objects.requireNonNull(state, "state");
        return RecipeManager.canOpen(state.getChapter(), state.getInventory());
    }

    /**
     * Associa ogni capitolo alla destinazione prevista dopo la miscela riuscita.
     */
    public static String completionScene(int chapter) {
        return switch (chapter) {
            case 1 -> "TRANSITION_CH1_CH2";
            case 2 -> "TRANSITION_CH2_CH3";
            case 3 -> "CHAPTER3_COMPLETE";
            case 4 -> "EPILOGUE";
            default -> throw new IllegalArgumentException("Capitolo senza calderone: " + chapter);
        };
    }

    /** Solo gli ingredienti del primo drink vengono consumati in capitolo 1.
     * Mappa, chiavi e oggetti non appartenenti alla ricetta non vanno cancellati.
     * Gli altri capitoli mantengono la precedente politica di inventario. */
    public static void registerSuccess(GameState state, int chapter) {
        Objects.requireNonNull(state, "state");
        completionScene(chapter); // Valida il capitolo prima di modificare la partita.
        if (chapter == 1) {
            var ingredients = RecipeManager.forChapter(chapter).correct();
            for (Item item : state.getInventory()) {
                if (ingredients.contains(item.getId())) state.removeItem(item);
            }
        } else if (chapter == 3) {
            state.setYmirOldFashionedCreated(true);
        } else if (chapter == 4) {
            state.setRagnarokFlambeCreated(true);
        }
        state.resetHealth();
    }

    /**
     * Porta la salute a zero e disabilita la ripresa del boss: la sconfitta proviene dalla
     * ricetta.
     */
    public static void registerFailure(GameState state) {
        Objects.requireNonNull(state, "state");
        state.setCurrentHealth(0);
        state.setBossRetryAvailable(false);
    }
}
