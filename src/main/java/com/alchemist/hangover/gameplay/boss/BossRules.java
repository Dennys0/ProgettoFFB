package com.alchemist.hangover.gameplay.boss;

import com.alchemist.hangover.model.GameState;
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
        var reward = BossRewardCatalog.immediateReward(chapter);
        if (chapter == 3) state.setYmirPacified(true);
        else state.setBossDefeated(true);
        state.setBossRetryAvailable(false);
        reward.ifPresent(state::addItem);
    }
}
