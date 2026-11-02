package com.alchemist.hangover.view.boss;

import com.alchemist.hangover.controller.CombatManager;
import com.alchemist.hangover.gameplay.boss.BossRewardCatalog;
import com.alchemist.hangover.gameplay.boss.SurturDuel;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.view.HealthDisplayPanel;
import com.alchemist.hangover.view.StoryPromptDialog;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/** Surtur: schivate segnalate e inganno finale della spada. */
public final class SurturBossOverlay extends JDialog {
    private final SurturDuel duel = new SurturDuel(new Random());
    private final JLabel phaseLabel = label(24, Font.BOLD);
    private final JLabel warning = label(19, Font.BOLD);
    private final JLabel feedback = label(17, Font.PLAIN);
    private final JButton[] lanes = new JButton[3];
    private final HealthDisplayPanel hearts = new HealthDisplayPanel(35);

    public SurturBossOverlay(Frame parent) {
        super(parent, "SURTUR - LA FORNACE DEL RAGNAROK", true);
        setSize(1030, 650);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        java.net.URL imageUrl = getClass().getResource("/images/chapter4/room_boss.png");
        Image art = imageUrl == null ? null : new ImageIcon(imageUrl).getImage();
        JPanel stage = new JPanel(new BorderLayout(0, 15)) {
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    if (art != null) g.drawImage(art, 0, 0, getWidth(), getHeight(), this);
                    else {
                        g.setPaint(new GradientPaint(0, 0, new Color(115, 29, 12),
                                0, getHeight(), new Color(27, 16, 19)));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                    g.setColor(new Color(18, 9, 11, 195));
                    g.fillRect(0, 0, getWidth(), getHeight());
                } finally { g.dispose(); }
            }
        };
        stage.setBorder(BorderFactory.createEmptyBorder(18, 35, 24, 35));
        setContentPane(stage);

        JPanel north = new JPanel(new GridLayout(4, 1, 0, 8));
        north.setOpaque(false);
        JLabel title = label(31, Font.BOLD);
        title.setText("SURTUR - LA DANZA DELLA SPADA INFERNALE");
        north.add(title);
        north.add(phaseLabel);
        north.add(warning);
        north.add(feedback);
        stage.add(north, BorderLayout.NORTH);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
        center.setOpaque(false);
        for (int i = 0; i < lanes.length; i++) {
            final int index = i;
            JButton button = new JButton(new String[]{"A  SINISTRA","S  CENTRO","D  DESTRA"}[i]);
            button.setPreferredSize(new Dimension(265, 85));
            button.setFont(new Font("Georgia", Font.BOLD, 19));
            button.setFocusPainted(false);
            button.addActionListener(e -> choose(SurturDuel.Lane.values()[index]));
            lanes[i] = button;
            center.add(button);
        }
        stage.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(hearts, BorderLayout.WEST);
        JButton leave = new JButton("ABBANDONA LO SCONTRO (ESC)");
        leave.setFocusable(false);
        leave.addActionListener(e -> dispose());
        bottom.add(leave, BorderLayout.EAST);
        stage.add(bottom, BorderLayout.SOUTH);

        for (int i = 0; i < 3; i++) {
            String key = new String[]{"A","S","D"}[i];
            final int index = i;
            stage.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(key), "lane" + i);
            stage.getActionMap().put("lane" + i, new AbstractAction() {
                @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                    choose(SurturDuel.Lane.values()[index]);
                }
            });
        }
        stage.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "retreat");
        stage.getActionMap().put("retreat", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) { dispose(); }
        });
        refresh();
    }

    private static JLabel label(int size, int style) {
        JLabel text = new JLabel("", SwingConstants.CENTER);
        text.setFont(new Font("Georgia", style, size));
        text.setForeground(new Color(255, 222, 168));
        return text;
    }

    private void refresh() {
        if (duel.finalTrap()) {
            phaseLabel.setText("FASE FINALE - INGANNA IL GIGANTE");
            warning.setText(duel.lured()
                    ? "La lama precipita verso il centro: SCHIVA A SINISTRA O DESTRA!"
                    : "La spada va attirata sulla fornace: SCEGLI IL CENTRO!");
        } else {
            phaseLabel.setText("FASE " + (duel.round() < 2 ? "I" : "II")
                    + " - SCHIVATA " + (duel.round() + 1) + "/4");
            warning.setText(duel.round() < 2
                    ? "Una corsia sta per essere colpita: scegli una via sicura!"
                    : "Surtur colpisce DUE corsie: resta soltanto un passaggio!");
        }
        for (int i = 0; i < lanes.length; i++) {
            SurturDuel.Lane lane = SurturDuel.Lane.values()[i];
            boolean danger = !duel.finalTrap() && duel.danger().contains(lane);
            lanes[i].setBackground(danger ? new Color(196, 53, 39) : new Color(224, 183, 110));
            lanes[i].setForeground(Color.BLACK);
            lanes[i].setToolTipText(danger ? "Attenzione: il fendente colpira' qui" : "Clicca per scegliere la corsia");
        }
        if (duel.finalTrap()) {
            lanes[1].setBackground(duel.lured() ? new Color(196, 53, 39) : new Color(247, 199, 71));
        }
    }

    private void choose(SurturDuel.Lane lane) {
        switch (duel.choose(lane)) {
            case DODGED -> feedback.setText("Schivata perfetta: Surtur si infuria!");
            case LURED -> feedback.setText("Surtur abbocca! La lama cala sulla fornace...");
            case HIT -> {
                feedback.setText("Sei stato colpito! Scegli piu' attentamente.");
                if (CombatManager.takeHit("Surtur ti ha colpito con la sua spada ardente.")) {
                    dispose();
                    CombatManager.finishDefeat((Frame) getOwner(), StoryPromptDialog.Theme.FIRE,
                            "LA SPADA INFERNALE", "Arthur crolla tra le fiamme di Muspelheim.");
                    return;
                }
            }
            case VICTORY -> {
                CombatManager.victory(4);
                dispose();
                StoryPromptDialog.reward((Frame) getOwner(), StoryPromptDialog.Theme.FIRE,
                        "SURTUR E' STATO INGANNATO",
                        "La spada cade nella fornace: la Brace di Muspelheim e' tua. "
                        + "Portala al calderone e usala per ultima!",
                        BossRewardCatalog.immediateReward(4).orElseThrow());
                return;
            }
        }
        refresh();
    }
}
