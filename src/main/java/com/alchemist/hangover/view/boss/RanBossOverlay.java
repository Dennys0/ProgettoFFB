package com.alchemist.hangover.view.boss;

import com.alchemist.hangover.controller.CombatManager;
import com.alchemist.hangover.gameplay.boss.BossRules;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
import com.alchemist.hangover.view.ToastNotification;
import com.alchemist.hangover.view.dialogue.StoryPromptDialog;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

/**
 * Combattimento del capitolo 2 basato sul codice del capitano. Estrae segnali e risposte esca
 * senza ripetizioni finché le rispettive riserve non sono esaurite e impone una scadenza a ogni
 * prova.
 */
public class RanBossOverlay extends JDialog {

    private static final String[] RIDDLE_PHRASES = {
        "Tra le correnti spezzo la rete",
        "Invoco la forza della marea",
        "Placo l'ira della dea",
        "Proteggo la rotta del veliero",
        "Rivelo il segreto dell'abisso",
        "Padroneggio il vortice finale"
    };

    private static final List<String> FALSE_ORDERS = List.of(
            "Spezzo la corrente con la prua", "Affido il timone alla marea",
            "Invoco il silenzio degli scogli", "Placo le acque con la rete",
            "Proteggo le vele dal vortice", "Rivelo il nome del naufrago",
            "Recido le funi dell’ancora", "Richiamo la nave dagli abissi",
            "Porto la rete oltre la rotta", "Sollevo il remo contro la dea",
            "Affondo il ferro nella schiuma", "Sciolgo il nodo della risacca");
    private static final int TOTAL_TURNS = 3;

    /**
     * Compone la corrispondenza fra numeri e ordini usata anche come indizio nel diario del
     * capitano.
     */
    public static String journalCode() {
        StringBuilder code = new StringBuilder("CODICE DEL CAPITANO");
        for (int i = 0; i < RIDDLE_PHRASES.length; i++)
            code.append("\n").append(i + 1).append(": ").append(RIDDLE_PHRASES[i]);
        return code.toString();
    }

    private final List<Integer> signals = new ArrayList<>();
    private final List<String> decoys = new ArrayList<>();
    private final Frame parentFrame;
    private final JLabel turnLabel = new JLabel();
    private final JLabel signal = new JLabel();
    private final JLabel clock = new JLabel();
    private final JProgressBar tide = new JProgressBar();
    private final JPanel choices = new JPanel(new GridLayout(3, 1, 0, 8));
    private final Timer timer = new Timer(40, e -> tick());
    private int turn = 1;
    private long deadline;
    private int duration;
    private boolean accepting;

    public RanBossOverlay(Frame parent) {
        super(parent, "Rán — Il codice delle maree", true);
        parentFrame = parent;
        setSize(870, 540);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Image backdrop = art("/images/chapter2/room7_boss.png");
        Image goddess = art("/images/chapter2/npc/ran_boss.png");
        JPanel arena = new JPanel(new BorderLayout(20, 16)) {
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.drawImage(backdrop, 0, 0, getWidth(), getHeight(), this);
                g.setColor(new Color(4, 21, 32, 176));
                g.fillRect(0, 0, getWidth(), getHeight());
                double scale = Math.min(getWidth() * 0.35 / goddess.getWidth(this),
                        (getHeight() - 105.0) / goddess.getHeight(this));
                int w = (int) (goddess.getWidth(this) * scale);
                int h = (int) (goddess.getHeight(this) * scale);
                g.drawImage(goddess, 14, (getHeight() - h) / 2, w, h, this);
                g.dispose();
            }
        };
        arena.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 28));
        JLabel title = new JLabel("RÁN   /   IL CODICE DELLE MAREE");
        style(title, 25);
        arena.add(title, BorderLayout.NORTH);
        JPanel orders = new JPanel(new GridLayout(0, 1, 0, 4));
        orders.setOpaque(false);
        for (JLabel label : List.of(turnLabel, signal, clock)) {
            style(label, label == signal ? 24 : 16);
            orders.add(label);
        }
        tide.setForeground(new Color(86, 193, 183));
        orders.add(tide);
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(482, 365));
        card.setBackground(new Color(11, 39, 51));
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 14, 22));
        card.add(orders, BorderLayout.NORTH);
        choices.setOpaque(false);
        card.add(choices, BorderLayout.CENTER);
        card.add(new HealthDisplayPanel(30), BorderLayout.SOUTH);
        arena.add(card, BorderLayout.EAST);
        setContentPane(arena);
    }

    private static Image art(String path) {
        return new ImageIcon(RanBossOverlay.class.getResource(path)).getImage();
    }

    private static void style(JLabel label, int size) {
        label.setFont(new Font(Font.SERIF, Font.BOLD, size));
        label.setForeground(new Color(234, 223, 191));
    }

    @Override public void setVisible(boolean visible) {
        if (visible && !accepting) startTurn();
        super.setVisible(visible);
    }

    @Override public void dispose() {
        accepting = false;
        timer.stop();
        super.dispose();
    }

    /**
     * Estrae un segnale e due risposte esca dalle riserve mescolate, poi avvia la scadenza della
     * prova.
     */
    private void startTurn() {
        if (signals.isEmpty()) {
            for (int i = 1; i <= RIDDLE_PHRASES.length; i++) signals.add(i);
            Collections.shuffle(signals);
        }
        if (decoys.size() < 2) {
            decoys.clear();
            decoys.addAll(FALSE_ORDERS);
            Collections.shuffle(decoys);
        }
        int number = signals.remove(signals.size() - 1);
        String answer = RIDDLE_PHRASES[number - 1];
        List<String> options = new ArrayList<>(List.of(answer,
                decoys.remove(decoys.size() - 1), decoys.remove(decoys.size() - 1)));
        Collections.shuffle(options);
        turnLabel.setText("SIGILLI SPEZZATI   " + (turn - 1) + " / " + TOTAL_TURNS);
        signal.setText("Segnale " + number + "   ·   Rispondi a Rán");
        choices.removeAll();
        for (String option : options) {
            JButton button = new JButton(option);
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(27, 76, 86));
            button.setFont(new Font(Font.SERIF, Font.BOLD, 17));
            button.addActionListener(e -> {
                if (button.getParent() == choices) resolve(option.equals(answer) && System.nanoTime() < deadline);
            });
            choices.add(button);
        }
        choices.revalidate();
        choices.repaint();
        duration = 13000 - turn * 2000;
        tide.setMaximum(duration);
        deadline = System.nanoTime() + duration * 1_000_000L;
        accepting = true;
        tick();
        timer.start();
    }

    /**
     * Aggiorna barra e secondi rimanenti rispetto alla scadenza monotona; allo zero risolve la
     * prova come errore.
     */
    private void tick() {
        if (!accepting) return;
        long remaining = Math.max(0, deadline - System.nanoTime());
        tide.setValue((int) (remaining / 1_000_000L));
        clock.setText(String.format("La rete si chiude   %.1f s", remaining / 1e9));
        if (remaining == 0) resolve(false);
    }

    /**
     * Disabilita ulteriori risposte alla stessa prova e applica avanzamento, danno o vittoria.
     */
    private void resolve(boolean correct) {
        if (!accepting) return;
        accepting = false;
        timer.stop();
        if (correct && ++turn > TOTAL_TURNS) {
            dispose();
            winBossFight();
            return;
        }
        if (!correct) {
            if (CombatManager.takeHit(this, "Le reti di Rán ti trascinano nel vortice.")) {
                CombatManager.finishDefeat(this, StoryPromptDialog.Theme.OCEAN,
                        "LE PROFONDITÀ PREVALGONO", "Le reti di Rán si stringono. Puoi riprovare dal checkpoint.");
                return;
            }
            ToastNotification.show(this, "La rete ti colpisce! Cuori rimasti: " + GameState.getInstance().getCurrentHealth());
        }
        startTurn();
    }

    /**
     * Registra la vittoria di Rán e mostra la Schiuma di Ægir già assegnata dalle regole.
     */
    private void winBossFight() {
        CombatManager.victory(2);
        Item foam = BossRules.rewardForChapter(2);

        StoryPromptDialog.message(parentFrame, StoryPromptDialog.Theme.OCEAN,
                "LE RETI DI RÁN SI SPEZZANO",
                "Hai decifrato la mente di Rán e superato le sue prove.\n"
                + "Le correnti rallentano e l'abisso finalmente si quieta.");
        StoryPromptDialog.reward(parentFrame, StoryPromptDialog.Theme.OCEAN,
                "SCHIUMA DI ÆGIR",
                "Tra le reti ormai spezzate emerge la ricompensa della dea.",
                foam);
    }
}