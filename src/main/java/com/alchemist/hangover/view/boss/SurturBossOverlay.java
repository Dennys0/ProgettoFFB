package com.alchemist.hangover.view.boss;

import com.alchemist.hangover.controller.CombatManager;
import com.alchemist.hangover.gameplay.boss.BossRewardCatalog;
import com.alchemist.hangover.gameplay.boss.SurturDuel;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.view.HealthDisplayPanel;
import com.alchemist.hangover.view.StoryPromptDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

/**
 * Surtur V2: Arthur si muove sulle tre corsie mentre il timer prepara
 * l'impatto. I tasti non risolvono automaticamente l'attacco.
 */
public final class SurturBossOverlay extends JDialog {
    private final SurturDuel duel = new SurturDuel(new Random());
    private final JLabel phaseLabel = label(22, Font.BOLD);
    private final JLabel warning = label(19, Font.BOLD);
    private final JLabel feedback = label(17, Font.PLAIN);
    private final JLabel clockLabel = label(15, Font.BOLD);
    private final JProgressBar timeBar = new JProgressBar(0, 1000);
    private final HealthDisplayPanel hearts = new HealthDisplayPanel(32);
    private final ArenaPanel arena = new ArenaPanel();
    private final javax.swing.Timer animation;

    private int countdownMs;
    private long deadlineNanos;
    private long recoveryUntilNanos;
    private boolean finished;

    public SurturBossOverlay(Frame parent) {
        super(parent, "SURTUR - LA DANZA DEL RAGNAROK", true);
        setSize(1060, 720);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        java.net.URL url = getClass().getResource("/images/chapter4/room_boss.png");
        Image artwork = url == null ? null : new ImageIcon(url).getImage();
        JPanel background = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    if (artwork != null) g.drawImage(artwork, 0, 0, getWidth(), getHeight(), this);
                    else {
                        g.setPaint(new GradientPaint(0, 0, new Color(105, 33, 17),
                                0, getHeight(), new Color(24, 11, 18)));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                    g.setColor(new Color(13, 8, 12, 200));
                    g.fillRect(0, 0, getWidth(), getHeight());
                } finally { g.dispose(); }
            }
        };
        background.setBorder(BorderFactory.createEmptyBorder(15, 28, 18, 28));
        setContentPane(background);

        JPanel top = new JPanel(new BorderLayout(10, 2));
        top.setOpaque(false);
        JLabel title = label(28, Font.BOLD);
        title.setText("SURTUR - LA DANZA DELLA SPADA INFERNALE");
        top.add(title, BorderLayout.NORTH);
        JPanel guidance = new JPanel(new GridLayout(2, 1, 0, 5));
        guidance.setOpaque(false);
        guidance.add(phaseLabel);
        guidance.add(warning);
        top.add(guidance, BorderLayout.CENTER);
        top.add(hearts, BorderLayout.EAST);
        background.add(top, BorderLayout.NORTH);
        background.add(arena, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 7));
        bottom.setOpaque(false);
        feedback.setText("Muoviti con A / S / D. Non basta cliccare: devi essere al sicuro quando il tempo scade.");
        bottom.add(feedback, BorderLayout.NORTH);
        JPanel timerRow = new JPanel(new BorderLayout(12, 0));
        timerRow.setOpaque(false);
        clockLabel.setPreferredSize(new Dimension(100, 23));
        timerRow.add(clockLabel, BorderLayout.WEST);
        timeBar.setValue(1000);
        timeBar.setStringPainted(false);
        timeBar.setBackground(new Color(55, 29, 30));
        timeBar.setForeground(new Color(245, 142, 65));
        timerRow.add(timeBar, BorderLayout.CENTER);
        bottom.add(timerRow, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout(10, 0));
        actions.setOpaque(false);
        JLabel help = label(14, Font.PLAIN);
        help.setText("A = SINISTRA    S = CENTRO    D = DESTRA    E = PROVOCA SURTUR (FINALE)");
        actions.add(help, BorderLayout.CENTER);
        JButton leave = new JButton("ABBANDONA (ESC)");
        leave.setFocusable(false);
        leave.setFont(new Font("Georgia", Font.BOLD, 12));
        leave.addActionListener(e -> dispose());
        actions.add(leave, BorderLayout.EAST);
        bottom.add(actions, BorderLayout.SOUTH);
        background.add(bottom, BorderLayout.SOUTH);

        registerKey(background, KeyEvent.VK_A, "left", () -> move(SurturDuel.Lane.LEFT));
        registerKey(background, KeyEvent.VK_S, "center", () -> move(SurturDuel.Lane.CENTER));
        registerKey(background, KeyEvent.VK_D, "right", () -> move(SurturDuel.Lane.RIGHT));
        registerKey(background, KeyEvent.VK_E, "provoke", this::provoke);
        registerKey(background, KeyEvent.VK_ESCAPE, "leave", this::dispose);

        animation = new javax.swing.Timer(35, event -> tick());
        startCountdown();
        animation.start();
    }

    private static void registerKey(JComponent panel, int key, String name, Runnable action) {
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(key, 0), name);
        panel.getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                action.run();
            }
        });
    }

    private static JLabel label(int size, int style) {
        JLabel text = new JLabel("", SwingConstants.CENTER);
        text.setFont(new Font("Georgia", style, size));
        text.setForeground(new Color(255, 218, 162));
        return text;
    }

    private void move(SurturDuel.Lane lane) {
        if (finished) return;
        duel.move(lane);
        arena.repaint();
    }

    private void provoke() {
        if (finished || recoveryUntilNanos != 0) return;
        if (duel.taunt()) {
            feedback.setText("SURTUR ABBOCCA! Ora hai pochissimo tempo per abbandonare il CENTRO!");
            startCountdown();
        } else if (duel.finalTrap() && !duel.lured()) {
            feedback.setText("Raggiungi prima il centro della fornace, poi premi E!");
        }
    }

    private void startCountdown() {
        countdownMs = switch (duel.attack()) {
            case SINGLE -> 2800;
            case DOUBLE -> 2100;
            case SWEEP -> duel.sweepWave() == 0 ? 1950 : 1400;
            case FINAL -> duel.lured() ? 1350 : 5500;
        };
        deadlineNanos = System.nanoTime() + countdownMs * 1_000_000L;
        refreshInstructions();
        timeBar.setValue(1000);
        clockLabel.setText(String.format("%.1f s", countdownMs / 1000.0));
    }

    private void refreshInstructions() {
        String progress = "  (" + duel.completedAttacks() + "/6)";
        phaseLabel.setText(switch (duel.attack()) {
            case SINGLE -> "FASE I - IMPARA IL RITMO" + progress;
            case DOUBLE -> "FASE II - DOPPI FENDENTI" + progress;
            case SWEEP -> "FASE III - SPAZZATE DI FUOCO" + progress;
            case FINAL -> "TRAPPOLA FINALE - LA SPADA DI SURTUR";
        });
        warning.setText(switch (duel.attack()) {
            case SINGLE -> "Una corsia verra' colpita: raggiungi una zona sicura!";
            case DOUBLE -> "DUE corsie in fiamme: trovane una libera prima dell'impatto!";
            case SWEEP -> duel.sweepWave() == 0
                    ? "SPAZZATA: schiva la prima ondata di fuoco!"
                    : "SECONDA ONDATA! Spostati subito verso il lato opposto!";
            case FINAL -> duel.lured()
                    ? "LA SPADA CADE AL CENTRO: ALLONTANATI DALLA FORNACE!"
                    : "Mettiti al CENTRO e premi E per attirare il colpo finale!";
        });
        arena.repaint();
    }

    private void tick() {
        if (finished) return;
        long now = System.nanoTime();
        if (recoveryUntilNanos != 0) {
            if (now >= recoveryUntilNanos) {
                recoveryUntilNanos = 0;
                startCountdown();
            }
            return;
        }
        long millisRemaining = Math.max(0L, (deadlineNanos - now) / 1_000_000L);
        int fraction = (int) Math.min(1000L, millisRemaining * 1000L / countdownMs);
        timeBar.setValue(fraction);
        clockLabel.setText(String.format("%.1f s", millisRemaining / 1000.0));
        arena.repaint();
        if (millisRemaining == 0) resolveImpact();
    }

    private void resolveImpact() {
        SurturDuel.Result result = duel.impact();
        switch (result) {
            case DODGED -> feedback.setText("SCHIVATA! Surtur ruggisce e prepara il prossimo fendente.");
            case HIT -> {
                feedback.setText("COLPITO! Un cuore in meno. Impara il ritmo della fornace!");
                if (CombatManager.takeHit("Surtur ha travolto Arthur con la spada infernale.")) {
                    finished = true;
                    dispose();
                    CombatManager.finishDefeat((Frame) getOwner(), StoryPromptDialog.Theme.FIRE,
                            "LA FURIA DI MUSPELHEIM",
                            "Arthur cade tra le fiamme. Riprovaci e osserva i segnali del gigante.");
                    return;
                }
            }
            case VICTORY -> {
                finished = true;
                CombatManager.victory(4);
                dispose();
                StoryPromptDialog.reward((Frame) getOwner(), StoryPromptDialog.Theme.FIRE,
                        "LA SPADA SI FRANTUMA NELLA FORNACE",
                        "Arthur si sposta appena in tempo: la lama conficcata nella roccia "
                        + "libera la Brace di Muspelheim. Nel cocktail va aggiunta per ULTIMA!",
                        BossRewardCatalog.immediateReward(4).orElseThrow());
                return;
            }
        }
        // Pausa fra le ondate: Arthur ha modo di leggere il nuovo segnale.
        recoveryUntilNanos = System.nanoTime() + 650_000_000L;
        refreshInstructions();
        timeBar.setValue(1000);
        clockLabel.setText("PRONTO");
    }

    @Override public void dispose() {
        if (animation != null) animation.stop();
        super.dispose();
    }

    private final class ArenaPanel extends JPanel {
        ArenaPanel() {
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent event) {
                    int index = Math.max(0, Math.min(2, event.getX() * 3 / Math.max(1, getWidth())));
                    SurturBossOverlay.this.move(SurturDuel.Lane.values()[index]);
                }
            });
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int gap = 12;
                int margin = 12;
                int laneWidth = Math.max(1, (getWidth() - margin * 2 - gap * 2) / 3);
                int laneHeight = getHeight() - 12;
                float progress = countdownMs <= 0 ? 0
                        : 1f - timeBar.getValue() / 1000f;

                for (int i = 0; i < 3; i++) {
                    int x = margin + i * (laneWidth + gap);
                    SurturDuel.Lane lane = SurturDuel.Lane.values()[i];
                    boolean danger = duel.danger().contains(lane);
                    boolean active = duel.position() == lane;
                    g.setColor(danger ? new Color(135, 27, 21, 155)
                            : new Color(60, 46, 32, 143));
                    g.fillRoundRect(x, 5, laneWidth, laneHeight - 8, 25, 25);
                    g.setColor(danger ? new Color(255, 89, 59)
                            : new Color(215, 166, 97));
                    g.setStroke(new BasicStroke(active ? 4f : 2f));
                    g.drawRoundRect(x + 2, 7, laneWidth - 4, laneHeight - 12, 25, 25);

                    if (danger) {
                        int y = 24 + Math.round(progress * Math.max(8, laneHeight - 158));
                        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g.setColor(new Color(255, 120, 55, 200));
                        g.drawLine(x + 29, y, x + laneWidth - 30, y + 21);
                        g.setStroke(new BasicStroke(3f));
                        g.setColor(new Color(255, 215, 131));
                        g.drawLine(x + 29, y - 6, x + laneWidth - 30, y + 15);
                    }

                    String text = switch (lane) {
                        case LEFT -> "A - SINISTRA";
                        case CENTER -> "S - CENTRO";
                        case RIGHT -> "D - DESTRA";
                    };
                    g.setFont(new Font("Georgia", Font.BOLD, 17));
                    g.setColor(new Color(255, 226, 181));
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(text, x + (laneWidth - fm.stringWidth(text)) / 2, laneHeight - 22);

                    if (active) drawArthur(g, x + laneWidth / 2, laneHeight - 92);
                }
            } finally { g.dispose(); }
        }

        private void drawArthur(Graphics2D g, int cx, int cy) {
            g.setColor(new Color(255, 197, 95, 80));
            g.fillOval(cx - 54, cy - 50, 108, 108);
            g.setColor(new Color(21, 23, 31));
            g.fillOval(cx - 18, cy - 32, 36, 36);
            g.fillRoundRect(cx - 29, cy + 4, 58, 56, 15, 15);
            g.setColor(new Color(247, 215, 167));
            g.setStroke(new BasicStroke(3f));
            g.drawOval(cx - 18, cy - 32, 36, 36);
            g.drawRoundRect(cx - 29, cy + 4, 58, 56, 15, 15);
            g.setFont(new Font("Georgia", Font.BOLD, 15));
            String name = "ARTHUR";
            g.drawString(name, cx - g.getFontMetrics().stringWidth(name) / 2, cy - 44);
        }
    }
}
