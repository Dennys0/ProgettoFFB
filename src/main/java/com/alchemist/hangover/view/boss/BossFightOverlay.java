package com.alchemist.hangover.view.boss;

import com.alchemist.hangover.view.HealthDisplayPanel;
import com.alchemist.hangover.view.StoryPromptDialog;

import com.alchemist.hangover.controller.CombatManager;
import com.alchemist.hangover.controller.AudioManager;
import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.gameplay.boss.BossRewardCatalog;
import com.alchemist.hangover.model.Item;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BossFightOverlay extends JDialog {

    private int currentStep = 1;
    private static final int MAX_STEPS = 7;
    
    private List<String> currentSequence;
    private int userIndex = 0;

    private JLabel stepLabel;
    private JLabel sequenceDisplayLabel;
    private final JProgressBar timeBar = new JProgressBar(0, 1000);
    private JLabel timerLabel;
    private JLabel statusLabel;

    private Timer countdownTimer;
    private long deadline;
    private Timer nextStepTimer;
    private boolean battleMusic;
    private static final String[] DIRECTIONS = {"W", "A", "S", "D"};

    private final Frame parentFrame;

    public BossFightOverlay(Frame parent) {
        super(parent, "Hræsvelgr — Il fronte della tempesta", true);
        this.parentFrame = parent;
        setSize(800, 560);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        Image storm = new ImageIcon(getClass().getResource("/images/chapter1/room7_boss2.png")).getImage();
        Image eagle = new ImageIcon(getClass().getResource("/images/chapter1/npc/hraesvelgr.png")).getImage();
        JPanel contentPanel = new JPanel() {
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.drawImage(storm, 0, 0, getWidth(), getHeight(), this);
                g.setColor(new Color(7, 18, 32, 178));
                g.fillRect(0, 0, getWidth(), getHeight());
                double scale = Math.min(710.0 / eagle.getWidth(this), 190.0 / eagle.getHeight(this));
                int w = (int) (eagle.getWidth(this) * scale);
                g.drawImage(eagle, (getWidth() - w) / 2, 53, w, (int) (eagle.getHeight(this) * scale), this);
                g.setColor(new Color(8, 18, 30, 204));
                g.fillRoundRect(45, 246, 710, 290, 12, 12);
                g.dispose();
            }
        };
        contentPanel.setLayout(null);
        contentPanel.setOpaque(false);
        setContentPane(contentPanel);

        // Titolo
        JLabel titleLabel = new JLabel("HRÆSVELGR   /   IL FRONTE DELLA TEMPESTA", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 24));
        titleLabel.setForeground(new Color(242, 227, 189));
        titleLabel.setBounds(20, 20, 760, 35);
        contentPanel.add(titleLabel);

        // Indicatori Step e Timer
        stepLabel = new JLabel("STEP 1 / 7", SwingConstants.CENTER);
        stepLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        stepLabel.setForeground(new Color(255, 215, 0));
        stepLabel.setBounds(50, 255, 700, 30);
        contentPanel.add(stepLabel);

        // Display della Sequenza di Tasti
        sequenceDisplayLabel = new JLabel("", SwingConstants.CENTER);
        sequenceDisplayLabel.setFont(new Font("Monospaced", Font.BOLD, 36));
        sequenceDisplayLabel.setForeground(Color.CYAN);
        sequenceDisplayLabel.setBounds(50, 285, 700, 80);
        sequenceDisplayLabel.setBorder(BorderFactory.createLineBorder(new Color(102, 146, 167), 2));
        contentPanel.add(sequenceDisplayLabel);

        // Timer
        timerLabel = new JLabel("TEMPO: 10s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setBounds(50, 370, 700, 28);
        contentPanel.add(timerLabel);
        timeBar.setBounds(120, 397, 560, 7);
        timeBar.setForeground(new Color(222, 183, 103));
        contentPanel.add(timeBar);

        // Messaggi di Stato
        statusLabel = new JLabel("Premi i tasti WASD corretti prima che scada il tempo!", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 15));
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setBounds(50, 407, 700, 30);
        contentPanel.add(statusLabel);

        // La salute rimane visibile e si aggiorna ad ogni colpo anche nel dialogo modale.
        JLabel healthTitle = new JLabel("VITA", SwingConstants.RIGHT);
        healthTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        healthTitle.setForeground(Color.WHITE);
        healthTitle.setBounds(270, 450, 75, 36);
        contentPanel.add(healthTitle);

        HealthDisplayPanel bossHearts = new HealthDisplayPanel(34);
        bossHearts.setBounds(355, 451, 150, 36);
        contentPanel.add(bossHearts);

        // Istruzioni Input
        JLabel infoInput = new JLabel("Usa i tasti [W] [A] [S] [D] oppure le Frecce Direzionali", SwingConstants.CENTER);
        infoInput.setFont(new Font("SansSerif", Font.PLAIN, 13));
        infoInput.setForeground(new Color(180, 180, 180));
        infoInput.setBounds(50, 500, 700, 25);
        contentPanel.add(infoInput);

        // Key Listener per catturare WASD e Frecce
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        setFocusable(true);
    }

    @Override public void setVisible(boolean visible) {
        if (visible && !battleMusic) {
            battleMusic = true;
            AudioManager.getInstance().playScene("STORM_DUEL");
            currentRoomStep(1);
        }
        super.setVisible(visible);
    }

    @Override public void dispose() {
        if (countdownTimer != null) countdownTimer.stop();
        if (nextStepTimer != null) nextStepTimer.stop();
        if (battleMusic) {
            battleMusic = false;
            AudioManager.getInstance().playScene(SceneManager.getInstance().getCurrentSceneName());
        }
        super.dispose();
    }

    private void currentRoomStep(int step) {
        this.currentStep = step;
        this.userIndex = 0;
        stepLabel.setText("RAFFICA " + currentStep + " / " + MAX_STEPS);

        // Genera sequenza casuale (lunghezza = numero dello step)
        currentSequence = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < currentStep; i++) {
            currentSequence.add(DIRECTIONS[rand.nextInt(DIRECTIONS.length)]);
        }

        updateSequenceDisplay();

        int durationMs = 2200 + (step - 1) * 250;
        timeBar.setValue(1000);
        deadline = System.nanoTime() + durationMs * 1_000_000L;
        timerLabel.setText(String.format("%.1f s", durationMs / 1000.0));
        AudioManager.getInstance().playEffect(AudioManager.Effect.WIND_GUST);
        if (countdownTimer != null) countdownTimer.stop();
        countdownTimer = new Timer(50, e -> {
            long remaining = deadline - System.nanoTime();
            timeBar.setValue((int) (Math.max(0, remaining) / (durationMs * 1000L)));
            timerLabel.setText(String.format("%.1f s", Math.max(0, remaining) / 1e9));
            if (remaining <= 0) {
                countdownTimer.stop();
                failStep("Tempo scaduto! Hræsvelgr sbatte le ali!");
            }
        });
        countdownTimer.start();
    }

    private void updateSequenceDisplay() {
        StringBuilder sb = new StringBuilder("<html>");
        for (int i = 0; i < currentSequence.size(); i++) {
            if (i < userIndex) {
                sb.append("<font color='#00FF00'>[").append(currentSequence.get(i)).append("]</font> "); // Tasto corretto (Verde)
            } else if (i == userIndex) {
                sb.append("<font color='#FFFF00'>[").append(currentSequence.get(i)).append("]</font> "); // Tasto attuale (Giallo)
            } else {
                sb.append("<font color='#888888'>[").append(currentSequence.get(i)).append("]</font> "); // Da premere (Grigio)
            }
        }
        sb.append("</html>");
        sequenceDisplayLabel.setText(sb.toString());
    }

    private void handleKeyPress(KeyEvent e) {
        if (!isEnabled() || currentSequence == null || userIndex >= currentSequence.size()) return;

        String pressed = null;
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) pressed = "W";
        else if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) pressed = "A";
        else if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) pressed = "S";
        else if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) pressed = "D";

        if (pressed == null) return;

        String expected = currentSequence.get(userIndex);

        if (pressed.equals(expected)) {
            userIndex++;
            updateSequenceDisplay();

            if (userIndex >= currentSequence.size()) {
                // Step completato
                countdownTimer.stop();
                if (currentStep < MAX_STEPS) {
                    statusLabel.setText("Step " + currentStep + " superato! Preparati per il prossimo...");
                    nextStepTimer = new Timer(500, ev -> currentRoomStep(currentStep + 1));
                    nextStepTimer.setRepeats(false);
                    nextStepTimer.start();
                } else {
                    winBossFight();
                }
            }
        } else {
            countdownTimer.stop();
            failStep("Tasto errato! Sequenza interrotta.");
        }
    }

    private void failStep(String reason) {
        boolean defeated = CombatManager.takeHit(this,
                "Le ali di Hræsvelgr travolgono il vascello.");
        int hp = GameState.getInstance().getCurrentHealth();
        if (defeated) {
            CombatManager.finishDefeat(this, StoryPromptDialog.Theme.SKY,
                    "LA TEMPESTA HA VINTO",
                    "L'aquila gigante spezza la tua difesa e la nave trema sotto il vento.\n"
                    + "Puoi tornare allo scontro dal checkpoint.");
        } else {
            // Non interrompiamo il ritmo con un JOptionPane modale.
            currentRoomStep(1);
            statusLabel.setText(reason + " Cuori rimasti: " + hp);
        }
    }

    private void winBossFight() {
        dispose();
        CombatManager.victory(1);
        Item breath = BossRewardCatalog.immediateReward(1).orElseThrow();

        StoryPromptDialog.message(parentFrame, StoryPromptDialog.Theme.SKY,
                "LA TEMPESTA SI PLACA",
                "Hai superato le prove di Hræsvelgr!\n"
                + "Il cielo si schiarisce e il guardiano riconosce il tuo coraggio.");
        StoryPromptDialog.reward(parentFrame, StoryPromptDialog.Theme.SKY,
                "SOFFIO DI HRÆSVELGR",
                "Il guardiano ti dona un frammento del suo vento celestiale.",
                breath);
    }
}