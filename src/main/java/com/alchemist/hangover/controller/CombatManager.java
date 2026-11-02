package com.alchemist.hangover.controller;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.gameplay.boss.BossRules;
import com.alchemist.hangover.view.GameOverPanel;
import com.alchemist.hangover.view.StoryPromptDialog;

import java.awt.Frame;
import java.awt.Point;
import javax.swing.JDialog;
import javax.swing.Timer;

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

    public static boolean takeHit(JDialog battle, String reason) {
        boolean defeated = takeHit(reason);
        if (battle.getRootPane().getClientProperty("hitShake") == null) {
            Point origin = battle.getLocation();
            int[] offsets = {4, -4, 3, -2, 1, 0};
            Timer shake = new Timer(24, null);
            shake.addActionListener(new java.awt.event.ActionListener() {
                private int frame;
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (battle.isShowing()) battle.setLocation(origin.x + offsets[frame], origin.y);
                    if (++frame == offsets.length || !battle.isShowing()) {
                        shake.stop();
                        battle.getRootPane().putClientProperty("hitShake", null);
                    }
                }
            });
            battle.getRootPane().putClientProperty("hitShake", shake);
            shake.start();
        }
        return defeated;
    }

    public static void finishDefeat(JDialog battle, StoryPromptDialog.Theme theme,
                                    String title, String message) {
        battle.setEnabled(false);
        Timer afterImpact = new Timer(180, event -> {
            Frame owner = (Frame) battle.getOwner();
            battle.dispose();
            StoryPromptDialog.message(owner, theme, title, message);
            SceneManager.getInstance().showScene("GAMEOVER");
        });
        afterImpact.setRepeats(false);
        afterImpact.start();
    }

    public static void victory(int chapter) {
        BossRules.victory(GameState.getInstance(), chapter);
        AudioManager.getInstance().playEffect(AudioManager.Effect.SUCCESS);
    }
}
