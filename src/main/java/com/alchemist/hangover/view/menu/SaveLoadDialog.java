package com.alchemist.hangover.view.menu;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.database.SaveGameDAO;
import com.alchemist.hangover.database.SaveGameDAO.SaveSlotInfo;
import com.alchemist.hangover.model.GameSaveSnapshot;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.view.chapter.Chapter1Panel;
import com.alchemist.hangover.view.chapter.Chapter2Panel;
import com.alchemist.hangover.view.chapter.Chapter3Panel;
import com.alchemist.hangover.view.chapter.Chapter4Panel;
import com.alchemist.hangover.view.story.EpiloguePanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class SaveLoadDialog extends JDialog {

    public enum Mode {
        SAVE,
        LOAD
    }

    private final Mode mode;
    private final GameSaveSnapshot saveSnapshot;
    private JPanel slotsContainer;
    private boolean busy;
    private final JLabel operationStatus = new JLabel(" ");

    public SaveLoadDialog(JFrame parent, Mode mode) {
        this(parent, mode, null);
    }

    public SaveLoadDialog(JFrame parent, Mode mode, GameSaveSnapshot snapshot) {
        super(parent, mode == Mode.SAVE ? "SALVA PARTITA" : "CARICA PARTITA", true);
        if (snapshot != null && mode != Mode.SAVE)
            throw new IllegalArgumentException("Snapshot disponibile solo per SAVE");
        this.mode = mode;
        this.saveSnapshot = snapshot;

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
        bottomPanel.add(operationStatus);
        bottomPanel.add(btnClose);
        rootPanel.add(bottomPanel, BorderLayout.SOUTH);

        refreshSlots();
    }

    private void refreshSlots() {
        runDatabaseTask("Lettura salvataggi…", () -> SaveGameDAO.getAllSlots().stream()
                .collect(Collectors.toMap(SaveSlotInfo::getSlotId, slot -> slot)), slotsMap -> {
            slotsContainer.removeAll();
            for (int i = 1; i <= 3; i++) {
                final int slotId = i;
                SaveSlotInfo slotInfo = slotsMap.get(slotId);
                slotsContainer.add(createSlotPanel(slotId, slotInfo));
            }

            slotsContainer.revalidate();
            slotsContainer.repaint();
        });
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
            GameState state = GameState.getInstance();
            if (saveSnapshot == null && state.getChapter() == 3) state.ensureYmirRuneSeed();
            GameSaveSnapshot snapshot = saveSnapshot == null ? GameSaveSnapshot.capture(state) : saveSnapshot;
            runDatabaseTask("Salvataggio…", () -> SaveGameDAO.saveGame(slotId, name.trim(), snapshot), ok -> {
            if (ok) {
                JOptionPane.showMessageDialog(this, "Partita salvata con successo!", "Salvataggio", JOptionPane.INFORMATION_MESSAGE);
                refreshSlots();
            } else {
                JOptionPane.showMessageDialog(this, "Errore durante il salvataggio nel database.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
            });
        }
    }

    private void onLoadClicked(int slotId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Vuoi caricare lo Slot " + slotId + "?", "Conferma Caricamento", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            runDatabaseTask("Caricamento…", () -> SaveGameDAO.readSnapshot(slotId), snapshot -> {
            if (snapshot != null) {
                GameState.getInstance().restoreFrom(snapshot);
                dispose();

                GameState state = GameState.getInstance();
                if (state.getChapter() == 4) {
                    Chapter4Panel chapter4 =
                            (Chapter4Panel) SceneManager.getInstance().getPanel("CHAPTER4");
                    if (chapter4 != null) chapter4.refreshFromGameState();
                    if (state.isRagnarokFlambeCreated()) {
                        EpiloguePanel finale =
                                (EpiloguePanel) SceneManager.getInstance().getPanel("EPILOGUE");
                        if (finale != null) finale.restart();
                    }
                    SceneManager.getInstance().showScene(
                            state.isRagnarokFlambeCreated() ? "EPILOGUE" : "CHAPTER4");
                } else if (state.getChapter() == 3) {
                    Chapter3Panel chapter3 =
                            (Chapter3Panel) SceneManager.getInstance().getPanel("CHAPTER3");
                    if (chapter3 != null) chapter3.refreshFromGameState();
                    // Dopo aver completato l'Old Fashioned il giocatore deve
                    // ritrovare l'epilogo, non una partita che sembra incompleta.
                    SceneManager.getInstance().showScene(
                            state.isYmirOldFashionedCreated() ? "CHAPTER3_COMPLETE" : "CHAPTER3");
                } else if (state.getChapter() == 2) {
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
            });
        }
    }

    private void onDeleteClicked(int slotId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Sei sicuro di voler eliminare lo Slot " + slotId + "?", "Elimina Salvataggio", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            runDatabaseTask("Eliminazione…", () -> SaveGameDAO.deleteSave(slotId), ok -> {
                if (ok) refreshSlots();
                else JOptionPane.showMessageDialog(this, "Eliminazione non riuscita.");
            });
        }
    }

    /** Un solo task alla volta; stato e componenti vengono aggiornati esclusivamente sull'EDT. */
    private <T> void runDatabaseTask(String label, java.util.concurrent.Callable<T> work,
                                     java.util.function.Consumer<T> success) {
        if (busy) return;
        busy = true;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        java.util.Map<java.awt.Component, Boolean> enabled = new java.util.IdentityHashMap<>();
        rememberAndDisable(getContentPane(), enabled);
        operationStatus.setText(label);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception {
                long started = System.nanoTime();
                try { return work.call(); }
                finally { com.alchemist.hangover.controller.PerformanceLog.finished(label, started); }
            }
            @Override protected void done() {
                busy = false;
                enabled.forEach(java.awt.Component::setEnabled);
                setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                operationStatus.setText(" ");
                try { success.accept(get()); }
                catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                catch (java.util.concurrent.ExecutionException ex) {
                    JOptionPane.showMessageDialog(SaveLoadDialog.this,
                            "Operazione non riuscita. La partita corrente è stata conservata.",
                            "Salvataggi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static void rememberAndDisable(java.awt.Container root,
                                           java.util.Map<java.awt.Component, Boolean> enabled) {
        for (java.awt.Component child : root.getComponents()) {
            enabled.put(child, child.isEnabled());
            child.setEnabled(false);
            if (child instanceof java.awt.Container container) rememberAndDisable(container, enabled);
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
