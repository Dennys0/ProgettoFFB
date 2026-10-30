package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;

import javax.swing.*;
import java.awt.*;

public class HUDPanel extends JPanel {

    private JLabel[] heartLabels;
    private JButton inventoryButton;
    private Image heartFullImg;
    private Image heartEmptyImg;

    public HUDPanel(JFrame mainFrame) {
        setLayout(null);
        setOpaque(false);
        setBounds(0, 0, 1280, 80);

        // Caricamento immagini cuori
        try {
            heartFullImg = new ImageIcon(getClass().getResource("/images/ui/heart_full.png")).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            heartEmptyImg = new ImageIcon(getClass().getResource("/images/ui/heart_empty.png")).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            System.err.println("Errore caricamento cuori HUD: " + e.getMessage());
        }

        // 1. Cuori in Alto a Sinistra
        heartLabels = new JLabel[3];
        for (int i = 0; i < 3; i++) {
            heartLabels[i] = new JLabel();
            heartLabels[i].setBounds(30 + (i * 45), 20, 40, 40);
            if (heartFullImg != null) {
                heartLabels[i].setIcon(new ImageIcon(heartFullImg));
            } else {
                heartLabels[i].setText("â¤ï¸");
            }
            add(heartLabels[i]);
        }

        // 2. Icona Inventario in Alto a Destra
        inventoryButton = new JButton();
        inventoryButton.setBounds(1180, 15, 50, 50);
        inventoryButton.setContentAreaFilled(false);
        inventoryButton.setBorderPainted(false);
        inventoryButton.setFocusPainted(false);
        inventoryButton.setFocusable(false); // <-- AGGIUNGI QUESTA RIGA FONDAMENTALE!
        inventoryButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        try {
            ImageIcon invIcon = new ImageIcon(getClass().getResource("/images/ui/inventory_icon.png"));
            Image img = invIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            inventoryButton.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            inventoryButton.setText("ðŸŽ’");
            inventoryButton.setForeground(Color.WHITE);
        }

        inventoryButton.addActionListener(e -> {
            InventoryOverlay dialog = new InventoryOverlay(mainFrame);
            dialog.setVisible(true);
        });

        add(inventoryButton);

        // 3. Icona Salva Partita in Alto a Destra (accanto all'inventario)
        JButton saveButton = new JButton("💾");
        saveButton.setBounds(1115, 15, 50, 50);
        saveButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        saveButton.setForeground(new Color(255, 215, 0));
        saveButton.setBackground(new Color(30, 30, 45, 200));
        saveButton.setBorder(BorderFactory.createLineBorder(new Color(212, 175, 55), 1));
        saveButton.setFocusPainted(false);
        saveButton.setFocusable(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        saveButton.addActionListener(e -> {
            SaveLoadDialog saveDialog = new SaveLoadDialog(mainFrame, SaveLoadDialog.Mode.SAVE);
            saveDialog.setVisible(true);
        });

        add(saveButton);
    }

    public void updateHUD() {
        int hp = GameState.getInstance().getCurrentHealth();
        for (int i = 0; i < 3; i++) {
            if (i < hp) {
                if (heartFullImg != null) heartLabels[i].setIcon(new ImageIcon(heartFullImg));
                else heartLabels[i].setText("â¤ï¸");
            } else {
                if (heartEmptyImg != null) heartLabels[i].setIcon(new ImageIcon(heartEmptyImg));
                else heartLabels[i].setText("ðŸ–¤");
            }
        }
    }
}