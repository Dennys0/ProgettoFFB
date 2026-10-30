package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.dao.SaveGameDAO;
import com.alchemist.hangover.dao.SaveGameDAO.SaveSlotInfo;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.GameState;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.stream.Collectors;

public class SaveLoadDialog extends JDialog {

    public enum Mode {
        SAVE,
        LOAD
    }

    private Mode mode;
    private JPanel slotsContainer;
    private JFrame parentFrame;

    public SaveLoadDialog(JFrame parent, Mode mode) {
        super(parent, mode == Mode.SAVE ? "SALVA PARTITA" : "CARICA PARTITA", true);
        this.parentFrame = parent;
        this.mode = mode;

        setSize(700, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Stile Scuro / Fantasy
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(20, 20, 30));
        rootPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(212, 175, 55), 2),
                new EmptyBorder(20, 25, 20, 25)
        ));
        setContentPane(rootPanel);

        // Intestazione
        JLabel titleLabel = new JLabel(mode == Mode.SAVE ? "Scegli uno Slot per Salvare" : "Seleziona una Partita da Caricare", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 22));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        rootPanel.add(titleLabel, BorderLayout.NORTH);

        // Contenitore Slot
        slotsContainer = new JPanel(new GridLayout(3, 1, 0, 15));
        slotsContainer.setOpaque(false);
        rootPanel.add(slotsContainer, BorderLayout.CENTER);

        // Barra inferiore con pulsante Chiudi
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        JButton btnClose = createFantasyButton("Chiudi", new Color(120, 40, 40));
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnClose);
        rootPanel.add(bottomPanel, BorderLayout.SOUTH);

        refreshSlots();
    }

    private void refreshSlots() {
        slotsContainer.removeAll();

        Map<Integer, SaveSlotInfo> slotsMap = SaveGameDAO.getAllSlots().stream()
                .collect(Collectors.toMap(SaveSlotInfo::getSlotId, s -> s));

        for (int i = 1; i <= 3; i++) {
            final int slotId = i;
            SaveSlotInfo slotInfo = slotsMap.get(slotId);
            slotsContainer.add(createSlotPanel(slotId, slotInfo));
        }

        slotsContainer.revalidate();
        slotsContainer.repaint();
    }

    private JPanel createSlotPanel(int slotId, SaveSlotInfo slotInfo) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(new Color(35, 35, 50));
        panel.setBorder(new CompoundBorder(
                new LineBorder(slotInfo != null ? new Color(212, 175, 55) : new Color(80, 80, 100), 1),
                new EmptyBorder(10, 15, 10, 15)
        ));

        // Testo info slot
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel();
        nameLabel.setFont(new Font("Georgia", Font.BOLD, 16));

        JLabel detailsLabel = new JLabel();
        detailsLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        if (slotInfo != null) {
            nameLabel.setText("Slot " + slotId + ": " + slotInfo.getSlotName());
            nameLabel.setForeground(new Color(255, 230, 130));

            detailsLabel.setText("Capitolo: " + slotInfo.getCurrentChapter()
                    + "  |  Stanza: " + slotInfo.getCurrentRoom()
                    + "  |  Salute: " + slotInfo.getCurrentHealth() + "/" + slotInfo.getMaxHealth() + " ❤️"
                    + "  |  Salvataggio: " + slotInfo.getFormattedDate());
            detailsLabel.setForeground(new Color(190, 190, 200));
        } else {
            nameLabel.setText("Slot " + slotId + "  [VUOTO]");
            nameLabel.setForeground(new Color(140, 140, 150));

            detailsLabel.setText("Nessun dato salvato in questo slot.");
            detailsLabel.setForeground(new Color(110, 110, 120));
        }

        infoPanel.add(nameLabel);
        infoPanel.add(detailsLabel);
        panel.add(infoPanel, BorderLayout.CENTER);

        // Pulsanti azione
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnPanel.setOpaque(false);

        if (mode == Mode.SAVE) {
            JButton btnSave = createFantasyButton(slotInfo == null ? "Salva" : "Sovrascrivi", new Color(30, 110, 60));
            btnSave.addActionListener(e -> onSaveClicked(slotId, slotInfo));
            btnPanel.add(btnSave);
        } else {
            JButton btnLoad = createFantasyButton("Carica", new Color(30, 90, 140));
            btnLoad.setEnabled(slotInfo != null);
            btnLoad.addActionListener(e -> onLoadClicked(slotId));
            btnPanel.add(btnLoad);
        }

        if (slotInfo != null) {
            JButton btnDelete = createFantasyButton("Elimina", new Color(130, 40, 40));
            btnDelete.addActionListener(e -> onDeleteClicked(slotId));
            btnPanel.add(btnDelete);
        }

        panel.add(btnPanel, BorderLayout.EAST);
        return panel;
    }

    private void onSaveClicked(int slotId, SaveSlotInfo current) {
        String defaultName = current != null ? current.getSlotName() : "Partita " + slotId;
        String name = JOptionPane.showInputDialog(this, "Inserisci un nome per questo salvataggio:", defaultName);
        if (name != null && !name.trim().isEmpty()) {
            boolean ok = SaveGameDAO.saveGame(slotId, name.trim());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Partita salvata con successo!", "Salvataggio", JOptionPane.INFORMATION_MESSAGE);
                refreshSlots();
            } else {
                JOptionPane.showMessageDialog(this, "Errore durante il salvataggio nel database.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onLoadClicked(int slotId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Vuoi caricare lo Slot " + slotId + "?", "Conferma Caricamento", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = SaveGameDAO.loadGame(slotId);
            if (ok) {
                dispose();

                GameState state = GameState.getInstance();
                if (state.getChapter() == 2) {
                    Chapter2Panel chapter2 =
                            (Chapter2Panel) SceneManager.getInstance().getPanel("CHAPTER2");
                    if (chapter2 != null) chapter2.refreshFromGameState();
                    SceneManager.getInstance().showScene("CHAPTER2");
                } else {
                    Chapter1Panel chapter1 =
                            (Chapter1Panel) SceneManager.getInstance().getPanel("CHAPTER1");
                    if (chapter1 != null) chapter1.refreshFromGameState();
                    SceneManager.getInstance().showScene("CHAPTER1");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Errore nel caricamento della partita.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDeleteClicked(int slotId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Sei sicuro di voler eliminare lo Slot " + slotId + "?", "Elimina Salvataggio", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            SaveGameDAO.deleteSave(slotId);
            refreshSlots();
        }
    }

    private JButton createFantasyButton(String text, Color baseColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Georgia", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(baseColor);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(212, 175, 55), 1),
                new EmptyBorder(6, 14, 6, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(baseColor.brighter());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(baseColor);
            }
        });
        return btn;
    }
}
