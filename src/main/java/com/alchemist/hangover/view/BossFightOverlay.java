package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;
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
    private final int MAX_STEPS = 7;
    
    private List<String> currentSequence;
    private int userIndex = 0;

    private JLabel stepLabel;
    private JLabel sequenceDisplayLabel;
    private JLabel timerLabel;
    private JLabel statusLabel;
    private HUDPanel hudPanel;

    private Timer countdownTimer;
    private int timeLeft;
    private final String[] DIRECTIONS = {"W", "A", "S", "D"};

    private Frame parentFrame;

    public BossFightOverlay(Frame parent) {
        super(parent, "BOSS FIGHT: HRÃ†SVELGR", true);
        this.parentFrame = parent;
        setSize(800, 500);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(15, 10, 25, 245));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2d.setColor(new Color(140, 80, 220)); // Bordo viola temporale
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 30, 30);
            }
        };
        contentPanel.setLayout(null);
        contentPanel.setOpaque(false);
        setContentPane(contentPanel);

        // Titolo
        JLabel titleLabel = new JLabel("âš¡ LA PROVA DI HRÃ†SVELGR âš¡", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 24));
        titleLabel.setForeground(new Color(220, 180, 255));
        titleLabel.setBounds(20, 20, 760, 35);
        contentPanel.add(titleLabel);

        // Indicatori Step e Timer
        stepLabel = new JLabel("STEP 1 / 7", SwingConstants.CENTER);
        stepLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        stepLabel.setForeground(new Color(255, 215, 0));
        stepLabel.setBounds(50, 70, 700, 30);
        contentPanel.add(stepLabel);

        // Display della Sequenza di Tasti
        sequenceDisplayLabel = new JLabel("", SwingConstants.CENTER);
        sequenceDisplayLabel.setFont(new Font("Monospaced", Font.BOLD, 36));
        sequenceDisplayLabel.setForeground(Color.CYAN);
        sequenceDisplayLabel.setBounds(50, 140, 700, 80);
        sequenceDisplayLabel.setBorder(BorderFactory.createLineBorder(new Color(100, 60, 180), 2));
        contentPanel.add(sequenceDisplayLabel);

        // Timer
        timerLabel = new JLabel("TEMPO: 10s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setBounds(50, 250, 700, 35);
        contentPanel.add(timerLabel);

        // Messaggi di Stato
        statusLabel = new JLabel("Premi i tasti WASD corretti prima che scada il tempo!", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 15));
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setBounds(50, 310, 700, 30);
        contentPanel.add(statusLabel);

        // Istruzioni Input
        JLabel infoInput = new JLabel("Usa i tasti [W] [A] [S] [D] oppure le Frecce Direzionali", SwingConstants.CENTER);
        infoInput.setFont(new Font("SansSerif", Font.PLAIN, 13));
        infoInput.setForeground(new Color(180, 180, 180));
        infoInput.setBounds(50, 420, 700, 25);
        contentPanel.add(infoInput);

        // Key Listener per catturare WASD e Frecce
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        setFocusable(true);
        startStep(1);
    }

    private void startStep(int step) {
        this.currentRoomStep(step);
    }

    private void currentRoomStep(int step) {
        this.currentStep = step;
        this.userIndex = 0;
        stepLabel.setText("STEP " + currentStep + " / " + MAX_STEPS);

        // Genera sequenza casuale (lunghezza = numero dello step)
        currentSequence = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < currentStep; i++) {
            currentSequence.add(DIRECTIONS[rand.nextInt(DIRECTIONS.length)]);
        }

        updateSequenceDisplay();

        // Calcolo Tempo: da 10 secondi scende gradualmente fino a 4 secondi
        timeLeft = Math.max(4, 11 - currentStep);
        timerLabel.setText("TEMPO: " + timeLeft + "s");

        if (countdownTimer != null) countdownTimer.stop();

        countdownTimer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("TEMPO: " + timeLeft + "s");
            if (timeLeft <= 0) {
                countdownTimer.stop();
                failStep("Tempo scaduto! HrÃ¦svelgr sbatte le ali!");
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
        if (currentSequence == null || userIndex >= currentSequence.size()) return;

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
                    Timer nextStepTimer = new Timer(800, ev -> currentRoomStep(currentStep + 1));
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
        GameState.getInstance().takeDamage();
        int hp = GameState.getInstance().getCurrentHealth();

        if (hp <= 0) {
            dispose();
            JOptionPane.showMessageDialog(null,
                reason + "\nHai perso tutti i cuori di vita!",
                "Sconfitto dalla Tempesta", JOptionPane.ERROR_MESSAGE);
            SceneManager.getInstance().showScene("GAMEOVER");
        } else {
            JOptionPane.showMessageDialog(this, 
                reason + "\nHai perso 1 cuore di vita! Cuori rimasti: " + hp, 
                "Colpo Subito", JOptionPane.WARNING_MESSAGE);
            // Ricomincia dallo step 1
            currentRoomStep(1);
        }
    }

    private void winBossFight() {
        dispose();
        GameState gs = GameState.getInstance();
        gs.setBossDefeated(true);
        Item breath = new Item("SOFFIO_HRAESVELGR", "Soffio di Hræsvelgr",
                "L'ampolla contenente il vento celestiale puro donato dall'aquila gigante.",
                "/images/items/item_breath.png");
        gs.addItem(breath);

        ItemPickupOverlay popup = new ItemPickupOverlay(parentFrame, breath);
        popup.setVisible(true);
    }
}