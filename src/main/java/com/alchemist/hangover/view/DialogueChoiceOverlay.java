package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;

import javax.swing.*;
import java.awt.*;

public class DialogueChoiceOverlay extends JDialog {

    private Image bgImage;
    private Image mapIcon;

    public interface ChoiceCallback {
        void onChoiceMade(boolean gaveMap);
    }

    public DialogueChoiceOverlay(Frame parent, ChoiceCallback callback) {
        super(parent, "Cosa Vuoi Fare?", true);
        setSize(750, 520);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        try {
            bgImage = new ImageIcon(getClass().getResource("/images/ui/dialogue_choice_bg.png")).getImage();
            mapIcon = new ImageIcon(getClass().getResource("/images/items/item_map.png")).getImage();
        } catch (Exception e) {
            System.err.println("Errore caricamento immagini in DialogueChoiceOverlay: " + e.getMessage());
        }

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }

                // Disegno dell'icona della Mappa del Cielo
                if (mapIcon != null && GameState.getInstance().hasItem("MAPPA_CIELO")) {
                    g.drawImage(mapIcon, (getWidth() - 90) / 2, 200, 90, 90, this);
                }
            }
        };
        contentPanel.setLayout(null);
        contentPanel.setOpaque(false);
        setContentPane(contentPanel);

        boolean hasMap = GameState.getInstance().hasItem("MAPPA_CIELO");

        // Bottone DONA (Posizionato sotto la mappa)
        JButton btnDonate = new JButton(hasMap ? "DONA" : "MAPPA NON TROVATA");
        btnDonate.setFont(new Font("Georgia", Font.BOLD, 18));
        btnDonate.setForeground(hasMap ? new Color(255, 215, 0) : Color.GRAY);
        btnDonate.setBounds(275, 320, 200, 48);
        btnDonate.setContentAreaFilled(false);
        btnDonate.setBorderPainted(false);
        btnDonate.setFocusPainted(false);
        btnDonate.setFocusable(false);
        btnDonate.setEnabled(hasMap);
        btnDonate.setCursor(hasMap ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));

        btnDonate.addActionListener(e -> {
            GameState gs = GameState.getInstance();
            Item mapItem = gs.getItemById("MAPPA_CIELO");
            if (mapItem != null) {
                gs.removeItem(mapItem);
            }
            gs.addItem(new Item("CHIAVE_DRAGO", "Chiave del Drago", "Una pesante chiave d'ottone con incisioni a forma di drago celeste.", "/images/items/item_key.png"));
            gs.setGivenMapToMartino(true);
            gs.setHasDragonKey(true);

            dispose();
            if (callback != null) callback.onChoiceMade(true);
        });
        contentPanel.add(btnDonate);

        // Bottone Congedati / Esci
        JButton btnExit = new JButton();
        btnExit.setBounds(275, 382, 200, 48);
        btnExit.setContentAreaFilled(false);
        btnExit.setBorderPainted(false);
        btnExit.setFocusPainted(false);
        btnExit.setFocusable(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnExit.addActionListener(e -> {
            dispose();
            if (callback != null) callback.onChoiceMade(false);
        });
        contentPanel.add(btnExit);

        // Tasto X in alto a destra
        JButton btnClose = new JButton();
        btnClose.setBounds(620, 35, 45, 45);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setFocusable(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        contentPanel.add(btnClose);
    }
}