package com.alchemist.hangover.view.cauldron;

import com.alchemist.hangover.gameplay.cauldron.RecipeManager;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
import com.alchemist.hangover.view.chapter.HotspotButton;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Finestra di preparazione dei cocktail: mostra gli ingredienti utilizzabili e conserva l’ordine
 * delle tre scelte. RecipeManager valuta la miscela; CauldronController applica l’esito e cambia
 * scena.
 */
public class CauldronOverlay extends JDialog {

    private final List<Item> selectedItems = new ArrayList<>();
    private final JPanel slotsPanel;
    private final JLabel statusLabel;

    public CauldronOverlay(Frame parent) {
        super(parent, "CALDERONE ALCHEMICO", true);
        setSize(980, 680); // Risoluzione per la cornice
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        // Un unico asset visivo per tutte le postazioni alchemiche.
        // I risultati e le ricette rimangono specifici dei capitoli.
        JPanel contentPanel = new CauldronArtworkPanel();
        setContentPane(contentPanel);

        // 1. I 3 Slot Rettangolari per gli ingredienti scelti (Posizionati sui 3 riquadri luminosi in alto)
        slotsPanel = new JPanel(new GridLayout(1, 3, 35, 0));
        slotsPanel.setOpaque(false);
        slotsPanel.setBounds(145, 110, 690, 140);
        contentPanel.add(slotsPanel);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Georgia", Font.BOLD, 14));
        statusLabel.setForeground(new Color(255, 230, 170));
        statusLabel.setBounds(145, 455, 690, 38);
        contentPanel.add(statusLabel);

        // 2. Griglia degli ingredienti disponibili dall'inventario (Spazio centrale)
        JPanel inventoryGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        inventoryGrid.setOpaque(false);
        inventoryGrid.setBounds(120, 290, 740, 160);
        contentPanel.add(inventoryGrid);

        List<Item> inventory = RecipeManager.usableItems(GameState.getInstance().getChapter(),
                GameState.getInstance().getInventory());
        for (Item item : inventory) {
            JButton btnItem = new JButton();
            btnItem.setPreferredSize(new Dimension(130, 45));
            btnItem.setFocusPainted(false);
            btnItem.setFocusable(false);
            btnItem.setContentAreaFilled(false);
            btnItem.setBorder(BorderFactory.createLineBorder(new Color(200, 160, 80), 1));
            btnItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnItem.setToolTipText("Seleziona: " + item.getName());

            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(item.getIconPath()));
                Image img = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                btnItem.setIcon(new ImageIcon(img));
            } catch (Exception ignored) {
            }

            btnItem.setText("<html><center><font color='white'><b>" + item.getName() + "</b></font></center></html>");

            btnItem.addActionListener(e -> {
                if (selectedItems.size() < 3 && !selectedItems.contains(item)) {
                    selectedItems.add(item);
                    refreshSlots();
                }
            });
            inventoryGrid.add(btnItem);
        }

        // 4. Bottone Trasparente sovrapposto a "MIX INGREDIENTI..."
        JButton btnMix = new HotspotButton();
        btnMix.setToolTipText("Mescola i tre ingredienti");
        btnMix.setBounds(210, 528, 282, 60);
        btnMix.addActionListener(e -> processMixing());
        contentPanel.add(btnMix);

        // 5. Bottone Trasparente sovrapposto a "RESET SLOT"
        JButton btnReset = new HotspotButton();
        btnReset.setToolTipText("Svuota gli slot del calderone");
        btnReset.setBounds(520, 528, 252, 60);
        btnReset.addActionListener(e -> {
            selectedItems.clear();
            refreshSlots();
        });
        contentPanel.add(btnReset);

        // 6. Bottone Trasparente sovrapposto a "CHIUDI"
        JButton btnClose = new HotspotButton();
        btnClose.setToolTipText("Chiudi il calderone");
        btnClose.setBounds(440, 620, 100, 30);
        btnClose.addActionListener(e -> dispose());
        contentPanel.add(btnClose);

        refreshSlots();
    }

    /**
     * Aggiorna le tre posizioni visive secondo l’ordine degli ingredienti selezionati.
     */
    private void refreshSlots() {
        slotsPanel.removeAll();
        for (int i = 0; i < 3; i++) {
            JPanel slot = new JPanel(new BorderLayout());
            slot.setOpaque(false); // Lasciamo trasparente per vedere i riquadri dello sfondo

            if (i < selectedItems.size()) {
                Item item = selectedItems.get(i);
                JLabel lbl = new JLabel("<html><center><font color='#FFD700'><b>" + item.getName() + "</b></font></center></html>", SwingConstants.CENTER);
                lbl.setFont(new Font("Georgia", Font.BOLD, 13));

                try {
                    ImageIcon icon = new ImageIcon(getClass().getResource(item.getIconPath()));
                    Image img = icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
                    lbl.setIcon(new ImageIcon(img));
                    lbl.setVerticalTextPosition(SwingConstants.BOTTOM);
                    lbl.setHorizontalTextPosition(SwingConstants.CENTER);
                } catch (Exception ignored) {
                }

                slot.add(lbl, BorderLayout.CENTER);
            }
            slotsPanel.add(slot);
        }
        slotsPanel.revalidate();
        slotsPanel.repaint();
    }
    /**
     * Valuta la selezione e distingue miscela incompleta, successo ed errore prima di delegarne
     * le conseguenze.
     */
    private void processMixing() {
        int chapter = GameState.getInstance().getChapter();
        java.util.List<String> selectedIds = selectedItems.stream()
                .map(Item::getId).toList();
        switch (RecipeManager.evaluate(chapter, selectedIds)) {
            case INCOMPLETE -> statusLabel.setText("Devi selezionare esattamente 3 ingredienti!");
            case WRONG_INGREDIENT -> {
                String message = RecipeManager.failureText(chapter, selectedIds,
                        GameState.getInstance().getInventory());
                dispose();
                CauldronController.fail(message);
            }
            case SUCCESS -> {
                dispose();
                CauldronController.complete(chapter);
            }
        }
    }
}
