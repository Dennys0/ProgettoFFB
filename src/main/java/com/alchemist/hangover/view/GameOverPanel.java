package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;

import javax.swing.*;
import java.awt.*;

public class GameOverPanel extends JPanel {

    public GameOverPanel() {
        setLayout(null);
        setBackground(new Color(15, 10, 15));

        JLabel titleLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 54));
        titleLabel.setForeground(new Color(220, 40, 40));
        titleLabel.setBounds(240, 180, 800, 80);
        add(titleLabel);

        JLabel subLabel = new JLabel("La tempesta ha avuto la meglio sul tuo spirito...", SwingConstants.CENTER);
        subLabel.setFont(new Font("Georgia", Font.ITALIC, 20));
        subLabel.setForeground(new Color(200, 200, 200));
        subLabel.setBounds(240, 260, 800, 40);
        add(subLabel);

        // Bottone 1: RIPROVA
        JButton btnRetry = new JButton("RIPROVA ðŸ”„");
        btnRetry.setFont(new Font("Georgia", Font.BOLD, 18));
        btnRetry.setForeground(Color.BLACK);
        btnRetry.setBackground(new Color(255, 215, 0));
        btnRetry.setFocusPainted(false);
        btnRetry.setFocusable(false);
        btnRetry.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRetry.setBounds(440, 360, 400, 50);

        btnRetry.addActionListener(e -> {
            GameState.getInstance().resetHealth();
            Chapter1Panel ch1 = (Chapter1Panel) SceneManager.getInstance().getPanel("CHAPTER1");
            if (ch1 != null) {
                ch1.refreshFromGameState();
            }
            SceneManager.getInstance().showScene("CHAPTER1");
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