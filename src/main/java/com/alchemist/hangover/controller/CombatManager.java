package com.alchemist.hangover.controller;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.gameplay.boss.BossRules;
import com.alchemist.hangover.view.GameOverPanel;
import com.alchemist.hangover.view.StoryPromptDialog;

import java.awt.Frame;

/**
 * Common health, checkpoint and victory handling for all three chapter bosses.
 * Each boss keeps its own mechanic (WASD, Rán riddles, Ymir runes).
 */
public final class CombatManager {
    private CombatManager() { }

    /** @return true when Arthur has lost all available hearts */
    public static boolean takeHit(String defeatReason) {
        GameState state = GameState.getInstance();
        boolean defeated = BossRules.takeHit(state);
        AudioManager.getInstance().playEffect(AudioManager.Effect.DAMAGE);
        if (!defeated) return false;
        GameOverPanel panel = (GameOverPanel) SceneManager.getInstance().getPanel("GAMEOVER");
        if (panel != null) panel.showReason(defeatReason);
        return true;
    }

    public static void finishDefeat(Frame owner, StoryPromptDialog.Theme theme,
                                    String title, String message) {
        StoryPromptDialog.message(owner, theme, title, message);
        SceneManager.getInstance().showScene("GAMEOVER");
    }

    public static void victory(int chapter) {
        BossRules.victory(GameState.getInstance(), chapter);
        AudioManager.getInstance().playEffect(AudioManager.Effect.SUCCESS);
    }
}
