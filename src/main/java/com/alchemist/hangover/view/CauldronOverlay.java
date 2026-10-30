package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CauldronOverlay extends JDialog {

    private Image bgImage;
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

        // Caricamento dello sfondo custom
        try {
            bgImage = new ImageIcon(getClass().getResource("/images/ui/cauldron_bg.png")).getImage();
        } catch (Exception e) {
            System.err.println("Errore caricamento cauldron_bg.png: " + e.getMessage());
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

        // 1. I 3 Slot Rettangolari per gli ingredienti scelti (Posizionati sui 3 riquadri luminosi in alto)
        slotsPanel = new JPanel(new GridLayout(1, 3, 35, 0));
        slotsPanel.setOpaque(false);
        slotsPanel.setBounds(145, 110, 690, 140);
        contentPanel.add(slotsPanel);

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
        JButton btnMix = new JButton();
        btnMix.setBounds(210, 528, 282, 60);
        btnMix.setContentAreaFilled(false);
        btnMix.setBorderPainted(false);
        btnMix.setFocusPainted(false);
        btnMix.setFocusable(false);
        btnMix.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMix.addActionListener(e -> processMixing());
        contentPanel.add(btnMix);

        // 5. Bottone Trasparente sovrapposto a "RESET SLOT"
        JButton btnReset = new JButton();
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
        JButton btnClose = new JButton();
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
        if (selectedItems.size() < 3) {
            statusLabel.setText("Devi inserire esattamente 3 ingredienti!");
            return;
        }

        boolean hasBreath = selectedItems.stream().anyMatch(i -> i.getId().equals("SOFFIO_HRAESVELGR"));
        boolean hasFeather = selectedItems.stream().anyMatch(i -> i.getId().equals("PIUMA_FOLGORE"));
        boolean hasDew = selectedItems.stream().anyMatch(i -> i.getId().equals("RUGIADA_ASGARD"));
        boolean hasFog = selectedItems.stream().anyMatch(i -> i.getId().equals("NEBBIA_NIFLHEIM"));
        boolean hasPollen = selectedItems.stream().anyMatch(i -> i.getId().equals("POLLINE_YGGDRASIL"));

        if (hasFog) {
            dispose();
            JOptionPane.showMessageDialog(null,
                    "La Nebbia di Niflheim congela all'istante il calderone spaccandolo in due!\nGAME OVER!",
                    "Game Over!", JOptionPane.ERROR_MESSAGE);
            GameState.getInstance().resetState();
            SceneManager.getInstance().showScene("MENU");
            return;
        }

        if (hasPollen) {
            dispose();
            JOptionPane.showMessageDialog(null,
                    "Il Polline di Yggdrasil provoca un'allucinazione nell'allucinazione!\nTi risvegli smarrito sul ponte...",
                    "Reset Livello!", JOptionPane.WARNING_MESSAGE);
            SceneManager.getInstance().showScene("CHAPTER1");
            return;
        }

        if (hasBreath && hasFeather && hasDew) {
            dispose();

            // Mostra prima il pop-up custom per l'Asgardian Aviation
            ItemPickupOverlay pickupOverlay = new ItemPickupOverlay(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "ASGARDIAN AVIATION CREATO!"
            );
            pickupOverlay.setVisible(true);

            // Ripristina la salute e avvia la transizione
            // RESET INVENTARIO PER IL CAPITOLO 2
            GameState.getInstance().getInventory().clear();
            GameState.getInstance().resetHealth();

            // Effettua la transizione verso la schermata di intermezzo
            SceneManager.getInstance().showScene("TRANSITION_CH1_CH2");
            return;
        }

        dispose();
        JOptionPane.showMessageDialog(null,
                "La miscela emette un fumo strano e svanisce... Non era la combinazione giusta!",
                "Tentativo Fallito", JOptionPane.WARNING_MESSAGE);

        // Controlli per gli ingredienti del Capitolo 2
        boolean hasSchiumaAegir = selectedItems.stream().anyMatch(i -> i.getId().equals("SCHIUMA_AEGIR"));
        boolean hasCoralloRan = selectedItems.stream().anyMatch(i -> i.getId().equals("CORALLO_RAN"));
        boolean hasSaleJormungandr = selectedItems.stream().anyMatch(i -> i.getId().equals("SALE_JORMUNGANDR"));
        boolean hasAcquaLete = selectedItems.stream().anyMatch(i -> i.getId().equals("ACQUA_LETE"));
        boolean hasInchiostroKraken = selectedItems.stream().anyMatch(i -> i.getId().equals("INCHIOSTRO_KRAKEN"));

// 1. ACQUA DEL LETE -> RESET MEMORIA & INVENTARIO
        if (hasAcquaLete) {
            dispose();
            JOptionPane.showMessageDialog(null,
                    "L'Acqua del Lete ti fa dimenticare ogni ricetta ed enigma!\nL'inventario si svuota e il livello si resetta!",
                    "Amnesia Alchemica", JOptionPane.ERROR_MESSAGE);
            GameState.getInstance().getInventory().clear();
            SceneManager.getInstance().showScene("CHAPTER2");
            return;
        }

// 2. INCHIOSTRO DI KRAKEN -> GAME OVER (Il calderone viene risucchiato)
        if (hasInchiostroKraken) {
            dispose();
            JOptionPane.showMessageDialog(null,
                    "L'Inchiostro di Kraken crea un buco nero liquido che risucchia il calderone stesso!\nGAME OVER!",
                    "Game Over!", JOptionPane.ERROR_MESSAGE);
            GameState.getInstance().resetState();
            SceneManager.getInstance().showScene("MENU");
            return;
        }

// 3. COMBINAZIONE VINCENTE CAPITOLO 2 (Maelstrom Martini)
        if (hasSchiumaAegir && hasCoralloRan && hasSaleJormungandr) {
            dispose();
            ItemPickupOverlay pickupOverlay = new ItemPickupOverlay(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "MAELSTROM MARTINI CREATO!"
            );
            pickupOverlay.setVisible(true);

            GameState.getInstance().resetHealth();
            SceneManager.getInstance().showScene("TRANSITION_CH2_CH3");
            return;
        }
    }
}
