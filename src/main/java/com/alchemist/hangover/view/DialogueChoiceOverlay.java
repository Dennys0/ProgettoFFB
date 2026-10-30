package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DialogueChoiceOverlay extends JDialog {

    private Image bgImage;

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
        } catch (Exception e) {
            System.err.println("Errore caricamento dialogue_choice_bg.png: " + e.getMessage());
        }

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        contentPanel.setLayout(null);
        contentPanel.setOpaque(false);
        setContentPane(contentPanel);

        boolean hasMap = GameState.getInstance().hasItem("MAPPA_CIELO");

        // 1. Tasto Consegna Mappa (senza immagine della mappa, solo testo pulito sulla targhetta centrale)
        JButton btnGiveMap = new JButton(hasMap ? "CONSEGNA MAPPA DEL CIELO" : "MAPPA NON TROVATA");
        btnGiveMap.setFont(new Font("Georgia", Font.BOLD, 14));
        btnGiveMap.setForeground(hasMap ? new Color(255, 215, 0) : Color.GRAY);
        btnGiveMap.setBounds(240, 318, 270, 52);
        btnGiveMap.setContentAreaFilled(false);
        btnGiveMap.setBorderPainted(false);
        btnGiveMap.setFocusPainted(false);
        btnGiveMap.setFocusable(false);
        btnGiveMap.setEnabled(hasMap);
        btnGiveMap.setCursor(hasMap ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));

        btnGiveMap.addActionListener(e -> {
            GameState gs = GameState.getInstance();
            Item mapItem = gs.getItemById("MAPPA_CIELO");
            if (mapItem != null) {
                gs.removeItem(mapItem);
            }

            Item dragonKey = new Item("CHIAVE_DRAGO", "Chiave del Drago",
                    "Una pesante chiave d'ottone con incisioni a forma di drago celeste.",
                    "/images/items/item_key.png");
            gs.addItem(dragonKey);
            gs.setGivenMapToMartino(true);
            gs.setHasDragonKey(true);

            dispose();
            if (callback != null) callback.onChoiceMade(true);

            // Mostra popup grafico della Chiave del Drago appena ottenuta
            ItemPickupOverlay pickup = new ItemPickupOverlay(parent, dragonKey);
            pickup.setVisible(true);
        });
        contentPanel.add(btnGiveMap);

        // 2. Tasto Congedati / Esci (senza scritta sovrapposta: opera direttamente sul tasto disegnato nell'immagine)
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

        // 3. Tasto X in alto a destra
        JButton btnClose = new JButton();
        btnClose.setBounds(620, 35, 45, 45);
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

        // Chiusura tramite ESC
        contentPanel.setFocusable(true);
        contentPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                    if (callback != null) callback.onChoiceMade(false);
                }
            }
        });

        // Chiusura al clic esterno
        contentPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getX() < 120 || e.getX() > 630 || e.getY() < 60 || e.getY() > 440) {
                    dispose();
                    if (callback != null) callback.onChoiceMade(false);
                }
            }
        });
    }
}
