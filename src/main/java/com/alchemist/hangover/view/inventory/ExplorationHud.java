package com.alchemist.hangover.view.inventory;

import com.alchemist.hangover.view.chapter.ChapterLayouts;
import com.alchemist.hangover.view.menu.SaveLoadDialog;
import java.awt.*;
import javax.swing.*;

/**
 * Barra dell’esplorazione con capitolo, stanza, diario, inventario e salvataggi. I cuori sono
 * mostrati nelle finestre dei combattimenti.
 */
public class ExplorationHud extends JPanel {

    private final JLabel location = new JLabel();
    public ExplorationHud(JFrame mainFrame) {
        setLayout(null);
        setOpaque(false);
        setBounds(0, 0, 1280, 80);

        location.setOpaque(true);
        location.setBackground(new Color(30, 27, 31));
        location.setForeground(new Color(245, 222, 171));
        location.setFont(new Font(Font.SERIF, Font.BOLD, 16));
        location.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        add(location);
        updateHUD();
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) updateHUD();
        });

        // 2. Icona Inventario in Alto a Destra
        JButton inventoryButton = new JButton();
        inventoryButton.setBounds(1180, 15, 50, 50);
        inventoryButton.setContentAreaFilled(false);
        inventoryButton.setBorderPainted(false);
        inventoryButton.setFocusPainted(false);
        inventoryButton.setFocusable(false);
        inventoryButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        try {
            ImageIcon invIcon = new ImageIcon(getClass().getResource("/images/ui/inventory_icon.png"));
            Image img = invIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            inventoryButton.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            inventoryButton.setText("Inv.");
            inventoryButton.setForeground(Color.WHITE);
        }

        inventoryButton.addActionListener(e -> {
            InventoryOverlay dialog = new InventoryOverlay(mainFrame);
            dialog.setVisible(true);
        });

        add(inventoryButton);

        // Diario di Arthur: sempre disponibile durante l'esplorazione.
        JButton journalButton = new JButton("📜");
        journalButton.setBounds(1045, 15, 55, 50);
        journalButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        journalButton.setForeground(new Color(255, 215, 0));
        journalButton.setBackground(new Color(30, 30, 45));
        journalButton.setBorder(BorderFactory.createLineBorder(new Color(212, 175, 55), 1));
        journalButton.setToolTipText("Diario di Arthur (J)");
        journalButton.setFocusPainted(false);
        journalButton.setFocusable(false);
        journalButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        journalButton.addActionListener(e -> new GameJournalDialog(mainFrame).setVisible(true));
        add(journalButton);

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

    /**
     * Aggiorna capitolo e nome della stanza, adattando la larghezza dell’etichetta al testo.
     */
    public void updateHUD() {
        var state = com.alchemist.hangover.model.GameState.getInstance();
        String room = ChapterLayouts.roomName(state.getChapter(), state.getCurrentRoom());
        location.setText("Capitolo " + state.getChapter() + "   ·   " + room);
        location.setBounds(25, 20, location.getPreferredSize().width, 36);
    }
}