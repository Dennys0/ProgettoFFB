package com.alchemist.hangover.controller;

import com.alchemist.hangover.gameplay.boss.BossRules;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.view.dialogue.StoryPromptDialog;
import com.alchemist.hangover.view.story.GameOverPanel;
import java.awt.Frame;
import java.awt.Point;
import javax.swing.JDialog;
import javax.swing.Timer;

/** Coordina salute, ripresa e vittoria dei quattro boss.
 * Le meccaniche specifiche restano nelle regole e nelle finestre dei combattimenti. */
public final class CombatManager {
    private CombatManager() { }

    /** @return true se Arthur ha esaurito tutti i cuori disponibili */
    public static boolean takeHit(String defeatReason) {
        GameState state = GameState.getInstance();
        boolean defeated = BossRules.takeHit(state);
        AudioManager.getInstance().playEffect(AudioManager.Effect.DAMAGE);
        if (!defeated) return false;
        com.alchemist.hangover.network.ChallengeClient.report(state.getChapter(), false);
        GameOverPanel panel = (GameOverPanel) SceneManager.getInstance().getPanel("GAMEOVER");
        if (panel != null) panel.showReason(defeatReason);
        return true;
    }

    /**
     * Applica il danno comune e, quando è presente la finestra, aggiunge una breve scossa visiva
     * senza sovrapporne altre.
     */
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

    /**
     * Lascia percepire l’impatto, poi chiude il combattimento e mostra avviso e schermata di
     * sconfitta.
     */
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

    /**
     * Applica la ricompensa, segnala l’esito al registro facoltativo e riproduce il suono di
     * successo.
     */
    public static void victory(int chapter) {
        BossRules.victory(GameState.getInstance(), chapter);
        com.alchemist.hangover.network.ChallengeClient.report(chapter, true);
        AudioManager.getInstance().playEffect(AudioManager.Effect.SUCCESS);
    }
}
