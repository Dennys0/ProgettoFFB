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

        // L'immagine contiene gia' il pulsante dorato "DONA".
        // Questo JButton e' soltanto un hotspot trasparente sopra il disegno.
        JButton btnDonate = new HotspotButton();
        btnDonate.setBounds(235, 323, 218, 65);
        btnDonate.setToolTipText(hasMap ? "Consegna la Mappa del Cielo a Martino" : "Prima devi trovare la Mappa del Cielo");
        btnDonate.getAccessibleContext().setAccessibleName("Consegna la Mappa del Cielo");
        btnDonate.setContentAreaFilled(false);
        btnDonate.setBorderPainted(false);
        btnDonate.setFocusPainted(false);
        btnDonate.setFocusable(false);
        btnDonate.setEnabled(hasMap);
        btnDonate.setCursor(hasMap ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));

        btnDonate.addActionListener(e -> {
            GameState gs = GameState.getInstance();
            Item mapItem = gs.getItemById("MAPPA_CIELO");
            if (mapItem == null || gs.hasGivenMapToMartino()) return;
            gs.removeItem(mapItem);
            gs.addItem(new Item("CHIAVE_DRAGO", "Chiave del Drago", "Una pesante chiave d'ottone con incisioni a forma di drago celeste.", "/images/items/item_key.png"));
            gs.setGivenMapToMartino(true);
            gs.setHasDragonKey(true);

            dispose();
            if (callback != null) callback.onChoiceMade(true);
        });
        contentPanel.add(btnDonate);

        // Bottone Congedati / Esci
        JButton btnExit = new HotspotButton();
        btnExit.setToolTipText("Saluta Martino e torna a esplorare");
        btnExit.setBounds(235, 397, 218, 55);
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
        JButton btnClose = new HotspotButton();
        btnClose.setToolTipText("Chiudi la conversazione");
        btnClose.setBounds(584, 57, 65, 70);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setFocusable(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> {
            dispose();
            if (callback != null) callback.onChoiceMade(false);
        });
        contentPanel.add(btnClose);
    }
}