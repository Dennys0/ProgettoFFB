package com.alchemist.hangover.controller;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.BossRewardCatalog;
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
        state.takeDamage();
        AudioManager.getInstance().playEffect(AudioManager.Effect.DAMAGE);
        if (state.getCurrentHealth() > 0) return false;
        state.setBossRetryAvailable(true);
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
        GameState state = GameState.getInstance();
        switch (chapter) {
            case 1, 2 -> state.setBossDefeated(true);
            case 3 -> state.setYmirPacified(true);
            default -> throw new IllegalArgumentException("Unknown boss chapter " + chapter);
        }
        state.setBossRetryAvailable(false);
        BossRewardCatalog.immediateReward(chapter).ifPresent(state::addItem);
        AudioManager.getInstance().playEffect(AudioManager.Effect.SUCCESS);
    }
}
