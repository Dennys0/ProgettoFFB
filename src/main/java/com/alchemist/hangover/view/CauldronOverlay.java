package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CauldronOverlay extends JDialog {

    private List<Item> selectedItems;
    private JPanel slotsPanel;
    private JLabel statusLabel;

    public CauldronOverlay(Frame parent) {
        super(parent, "CALDERONE ALCHEMICO", true);
        setSize(980, 680); // Risoluzione per la cornice
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        selectedItems = new ArrayList<>();

        // Un unico asset visivo per tutte le postazioni alchemiche.
        // I risultati e le ricette rimangono specifici dei capitoli.
        JPanel contentPanel = new SharedCauldronArtworkPanel();
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

        List<Item> inventory = GameState.getInstance().getInventory();
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
        btnMix.setContentAreaFilled(false);
        btnMix.setBorderPainted(false);
        btnMix.setFocusPainted(false);
        btnMix.setFocusable(false);
        btnMix.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMix.addActionListener(e -> processMixing());
        contentPanel.add(btnMix);

        // 5. Bottone Trasparente sovrapposto a "RESET SLOT"
        JButton btnReset = new HotspotButton();
        btnReset.setToolTipText("Svuota gli slot del calderone");
        btnReset.setBounds(520, 528, 252, 60);
        btnReset.setContentAreaFilled(false);
        btnReset.setBorderPainted(false);
        btnReset.setFocusPainted(false);
        btnReset.setFocusable(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> {
            selectedItems.clear();
            refreshSlots();
        });
        contentPanel.add(btnReset);

        // 6. Bottone Trasparente sovrapposto a "CHIUDI"
        JButton btnClose = new HotspotButton();
        btnClose.setToolTipText("Chiudi il calderone");
        btnClose.setBounds(440, 620, 100, 30);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setFocusable(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        contentPanel.add(btnClose);

        refreshSlots();
    }

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
    private void processMixing() {
        if (selectedItems.size() != 3) {
            statusLabel.setText("Devi selezionare esattamente 3 ingredienti!");
            return;
        }
        java.util.Set<String> ids = selectedItems.stream()
                .map(Item::getId)
                .collect(java.util.stream.Collectors.toSet());

        if (ids.contains("NEBBIA_NIFLHEIM")) {
            dispose();
            JOptionPane.showMessageDialog(null,
                    "La Nebbia di Niflheim congela all'istante il calderone spaccandolo in due!\nGAME OVER!",
                    "Game Over!", JOptionPane.ERROR_MESSAGE);
            GameState.getInstance().resetState();
            SceneManager.getInstance().showScene("MENU");
            return;
        }
        if (ids.contains("POLLINE_YGGDRASIL")) {
            dispose();
            JOptionPane.showMessageDialog(null,
                    "Il Polline di Yggdrasil provoca un'allucinazione nell'allucinazione!\nTi risvegli smarrito sul ponte...",
                    "Reset Livello!", JOptionPane.WARNING_MESSAGE);
            SceneManager.getInstance().showScene("CHAPTER1");
            return;
        }
        if (ids.contains("ACQUA_LETE")) {
            dispose();
            JOptionPane.showMessageDialog(null,
                    "L'Acqua del Lete ti fa dimenticare ogni ricetta ed enigma!\nL'inventario si svuota e il livello si resetta!",
                    "Amnesia Alchemica", JOptionPane.ERROR_MESSAGE);
            GameState.getInstance().getInventory().clear();
            SceneManager.getInstance().showScene("CHAPTER2");
            return;
        }
        if (ids.contains("INCHIOSTRO_KRAKEN")) {
            dispose();
            JOptionPane.showMessageDialog(null,
                    "L'Inchiostro di Kraken crea un buco nero liquido che risucchia il calderone stesso!\nGAME OVER!",
                    "Game Over!", JOptionPane.ERROR_MESSAGE);
            GameState.getInstance().resetState();
            SceneManager.getInstance().showScene("MENU");
            return;
        }

        boolean chapter1Recipe = ids.contains("SOFFIO_HRAESVELGR")
                && ids.contains("PIUMA_FOLGORE")
                && ids.contains("RUGIADA_ASGARD");
        if (chapter1Recipe) {
            dispose();
            ItemPickupOverlay pickup = new ItemPickupOverlay(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "ASGARDIAN AVIATION CREATO!"
            );
            pickup.setVisible(true);
            GameState.getInstance().getInventory().clear();
            GameState.getInstance().resetHealth();
            SceneManager.getInstance().showScene("TRANSITION_CH1_CH2");
            return;
        }

        boolean chapter2Recipe = ids.contains("SCHIUMA_AEGIR")
                && ids.contains("CORALLO_RAN")
                && ids.contains("SALE_JORMUNGANDR");
        if (chapter2Recipe) {
            dispose();
            ItemPickupOverlay pickup = new ItemPickupOverlay(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "MAELSTROM MARTINI CREATO!"
            );
            pickup.setVisible(true);
            GameState.getInstance().resetHealth();
            SceneManager.getInstance().showScene("TRANSITION_CH2_CH3");
            return;
        }

        statusLabel.setText("La miscela svanisce... Non è la combinazione giusta. Riprova!");
    }
}

