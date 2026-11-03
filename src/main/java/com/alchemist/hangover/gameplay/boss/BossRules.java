package com.alchemist.hangover.gameplay.boss;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
import java.util.Objects;

/** Regole dei combattimenti, indipendenti da Swing, audio e scene. */
public final class BossRules {
    private BossRules() { }

    /** @return true se il colpo causa la sconfitta e abilita il checkpoint. */
    public static boolean takeHit(GameState state) {
        Objects.requireNonNull(state, "state");
        state.takeDamage();
        if (state.getCurrentHealth() > 0) return false;
        state.setBossRetryAvailable(true);
        return true;
    }

    /** Riconosce la vittoria e assegna una sola ricompensa, se prevista. */
    public static void victory(GameState state, int chapter) {
        Objects.requireNonNull(state, "state");
        Item reward = rewardForChapter(chapter);
        if (chapter == 3) state.setYmirPacified(true);
        else state.setBossDefeated(true);
        state.setBossRetryAvailable(false);
        state.addItem(reward);
    }

    /**
     * Costruisce la ricompensa del boss richiesto senza aggiungerla direttamente all’inventario.
     */
    public static Item rewardForChapter(int chapter) {
        return switch (chapter) {
            case 1 -> new Item(
                    "SOFFIO_HRAESVELGR", "Soffio di Hræsvelgr",
                    "L'ampolla contenente il vento celestiale puro donato dall'aquila gigante.",
                    "/images/chapter1/items/breath.png");
            case 2 -> new Item(
                    "SCHIUMA_AEGIR", "Schiuma di Ægir",
                    "La birra sottomarina lucente estratta dalle reti della dea Rán.",
                    "/images/chapter2/items/foam.png");
            case 3 -> new Item("LACRIMA_YMIR", "Lacrima di Ymir",
                    "Una lacrima cristallina donata dal gigante nel sonno.", "/images/chapter3/items/ymircry.png");
            case 4 -> new Item(
                    "BRACE_MUSPELHEIM", "Brace di Muspelheim",
                    "Il cuore di fuoco inestinguibile liberato dalla spada di Surtur.",
                    "/images/chapter4/items/ember.png");
            default -> throw new IllegalArgumentException("Capitolo senza boss: " + chapter);
        };
    }
}
