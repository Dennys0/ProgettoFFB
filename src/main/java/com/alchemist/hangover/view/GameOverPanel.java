package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;

import javax.swing.*;
import java.awt.*;

public class GameOverPanel extends JPanel {

    private JLabel reasonLabel;

    public void showReason(String reason) {
        if (reasonLabel != null) reasonLabel.setText(reason);
    }

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
        JButton btnRetry = new JButton("RIPROVA LO SCONTRO");
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
            state.resetHealth();
            state.setBossRetryAvailable(false);
            SceneManager manager = SceneManager.getInstance();
            if (state.getChapter() == 3) {
                Chapter3Panel chapter3 = (Chapter3Panel) manager.getPanel("CHAPTER3");
                if (chapter3 != null) chapter3.refreshFromGameState();
                manager.showScene("CHAPTER3",
                        retryBoss && chapter3 != null ? chapter3::retryBossEncounter : null);
            } else if (state.getChapter() == 2) {
                Chapter2Panel chapter2 = (Chapter2Panel) manager.getPanel("CHAPTER2");
                if (chapter2 != null) chapter2.refreshFromGameState();
                manager.showScene("CHAPTER2",
                        retryBoss && chapter2 != null ? chapter2::retryBossEncounter : null);
            } else {
                Chapter1Panel chapter1 = (Chapter1Panel) manager.getPanel("CHAPTER1");
                if (chapter1 != null) chapter1.refreshFromGameState();
                manager.showScene("CHAPTER1",
                        retryBoss && chapter1 != null ? chapter1::retryBossEncounter : null);
            }
        });
        add(btnRetry);

        // Bottone 2: TORNA AL MENU
        JButton btnMenu = new JButton("TORNA AL MENU ðŸ ");
        btnMenu.setFont(new Font("Georgia", Font.BOLD, 16));
        btnMenu.setForeground(Color.WHITE);
        btnMenu.setBackground(new Color(80, 30, 30));
        btnMenu.setFocusPainted(false);
        btnMenu.setFocusable(false);
        btnMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMenu.setBounds(440, 430, 400, 50);

        btnMenu.addActionListener(e -> {
            GameState.getInstance().resetState();
            SceneManager.getInstance().showScene("MENU");
        });
        add(btnMenu);
    }
}