package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.RecipeManager;

import javax.swing.*;
import java.awt.*;

public class GameOverPanel extends JPanel {

    private JLabel reasonLabel;
    private JButton btnRetry;
    private boolean cauldronFailure;

    /** Mostra la causa di una sconfitta ordinaria (boss). */
    public void showReason(String reason) {
        cauldronFailure = false;
        if (reasonLabel != null) reasonLabel.setText(reason);
        if (btnRetry != null) btnRetry.setText("RIPROVA LO SCONTRO");
    }

    /** Il calderone non e' uno scontro: il retry deve riaprire la ricetta. */
    public void showCauldronFailure(String reason) {
        showReason(reason);
        cauldronFailure = true;
        if (btnRetry != null) btnRetry.setText("RIPROVA LA RICETTA");
    }

    boolean isCauldronFailure() { return cauldronFailure; }

    public GameOverPanel() {
        setLayout(null);
        setBackground(new Color(15, 10, 15));

        JLabel titleLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 54));
        titleLabel.setForeground(new Color(220, 40, 40));
        titleLabel.setBounds(240, 180, 800, 80);
        add(titleLabel);

        JLabel subLabel = new JLabel("La tempesta ha avuto la meglio sul tuo spirito...", SwingConstants.CENTER);
        reasonLabel = subLabel;
        subLabel.setFont(new Font("Georgia", Font.ITALIC, 20));
        subLabel.setForeground(new Color(200, 200, 200));
        subLabel.setBounds(240, 260, 800, 40);
        add(subLabel);

        // Bottone 1: RIPROVA
        btnRetry = new JButton("RIPROVA LO SCONTRO");
        btnRetry.setFont(new Font("Georgia", Font.BOLD, 18));
        btnRetry.setForeground(Color.BLACK);
        btnRetry.setBackground(new Color(255, 215, 0));
        btnRetry.setFocusPainted(false);
        btnRetry.setFocusable(false);
        btnRetry.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRetry.setBounds(440, 360, 400, 50);

        btnRetry.addActionListener(e -> {
            GameState state = GameState.getInstance();
            boolean retryBoss = state.isBossRetryAvailable();
            boolean retryForge = cauldronFailure;
            state.resetHealth();
            state.setBossRetryAvailable(false);
            if (retryForge) state.setCurrentRoom(
                    RecipeManager.forChapter(state.getChapter()).cauldronRoom());
            SceneManager manager = SceneManager.getInstance();
            String chapterScene = "CHAPTER" + state.getChapter();
            AbstractChapterPanel chapter =
                    (AbstractChapterPanel) manager.getPanel(chapterScene);
            if (chapter != null) chapter.refreshFromGameState();
            Runnable afterTransition = chapter == null ? null
                    : retryForge ? chapter::retryCauldronAfterFailure
                    : retryBoss ? chapter::retryBossEncounter : null;
            manager.showScene(chapterScene, afterTransition);
            cauldronFailure = false;
            btnRetry.setText("RIPROVA LO SCONTRO");
        });
        add(btnRetry);

        // Bottone 2: TORNA AL MENU
        JButton btnMenu = new JButton("TORNA AL MENU");
        btnMenu.setFont(new Font("Georgia", Font.BOLD, 16));
        btnMenu.setForeground(Color.WHITE);
        btnMenu.setBackground(new Color(80, 30, 30));
        btnMenu.setFocusPainted(false);
        btnMenu.setFocusable(false);
        btnMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMenu.setBounds(440, 430, 400, 50);

        btnMenu.addActionListener(e -> {
            cauldronFailure = false;
            btnRetry.setText("RIPROVA LO SCONTRO");
            GameState.getInstance().resetState();
            SceneManager.getInstance().showScene("MENU");
        });
        add(btnMenu);
    }
}