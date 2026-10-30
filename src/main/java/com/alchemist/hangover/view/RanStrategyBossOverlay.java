package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.Item;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class RanStrategyBossOverlay extends JDialog {

    private final String[] RIDDLE_PHRASES = {
        "Tra le correnti spezzo la rete",
        "Invoco la forza della marea",
        "Placo l'ira della dea",
        "Proteggo la rotta del veliero",
        "Rivelo il segreto dell'abisso",
        "Padroneggio il vortice finale"
    };

    private int currentTurn = 1;
    private final int TOTAL_TURNS = 3;
    private int targetNumber; // Numero pronunciato da Rán (1..6)

    private JLabel turnLabel;
    private JLabel bossDialogueLabel;
    private JPanel choicesPanel;
    private Frame parentFrame;

    public RanStrategyBossOverlay(Frame parent) {
        super(parent, "Scontro Strategico con Rán", true);
        this.parentFrame = parent;
        setSize(850, 550);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(15, 25, 40, 240));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(0, 200, 255));
                g.drawRect(5, 5, getWidth() - 10, getHeight() - 10);
            }
        };
        contentPanel.setLayout(null);
        setContentPane(contentPanel);

        // Titolo / Turno
        turnLabel = new JLabel("SCONTRO STRATEGICO — TURNO " + currentTurn + "/" + TOTAL_TURNS, SwingConstants.CENTER);
        turnLabel.setFont(new Font("Georgia", Font.BOLD, 22));
        turnLabel.setForeground(new Color(255, 215, 0));
        turnLabel.setBounds(50, 30, 750, 40);
        contentPanel.add(turnLabel);

        // Box del Parlato di Rán
        bossDialogueLabel = new JLabel("", SwingConstants.CENTER);
        bossDialogueLabel.setFont(new Font("Georgia", Font.BOLD, 20));
        bossDialogueLabel.setForeground(Color.CYAN);
        bossDialogueLabel.setBounds(50, 90, 750, 60);
        contentPanel.add(bossDialogueLabel);

        // Pannello per i 3 Pulsanti di Scelta
        choicesPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        choicesPanel.setOpaque(false);
        choicesPanel.setBounds(100, 180, 650, 280);
        contentPanel.add(choicesPanel);

        // I cuori cambiano immediatamente quando si sbaglia una risposta.
        JLabel healthTitle = new JLabel("VITA", SwingConstants.RIGHT);
        healthTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        healthTitle.setForeground(Color.WHITE);
        healthTitle.setBounds(280, 475, 85, 36);
        contentPanel.add(healthTitle);

        HealthDisplayPanel bossHearts = new HealthDisplayPanel(34);
        bossHearts.setBounds(375, 475, 150, 36);
        contentPanel.add(bossHearts);

        startNewTurn();
    }

    private void startNewTurn() {
        turnLabel.setText("SCONTRO STRATEGICO — TURNO " + currentTurn + "/" + TOTAL_TURNS);
        choicesPanel.removeAll();

        // 1. Rán pronuncia un numero casuale da 1 a 6
        targetNumber = new Random().nextInt(6) + 1; // 1..6
        bossDialogueLabel.setText("<html><center>Rán grida nell'abisso:<br><b>« " + targetNumber + " ! »</b></center></html>");

        // 2. Prepara la risposta corretta + 2 distrattori
        String correctPhrase = RIDDLE_PHRASES[targetNumber - 1];
        List<String> options = new ArrayList<>();
        options.add(correctPhrase);

        List<String> pool = new ArrayList<>(Arrays.asList(RIDDLE_PHRASES));
        pool.remove(correctPhrase);
        Collections.shuffle(pool);
        options.add(pool.get(0));
        options.add(pool.get(1));

        Collections.shuffle(options); // Mescola i 3 tasti

        // 3. Genera i 3 bottoni cliccabili
        for (String optionText : options) {
            JButton btnOption = new JButton(optionText);
            btnOption.setFont(new Font("SansSerif", Font.BOLD, 15));
            btnOption.setForeground(Color.WHITE);
            btnOption.setBackground(new Color(30, 60, 90));
            btnOption.setFocusPainted(false);
            btnOption.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnOption.addActionListener(e -> processChoice(optionText.equals(correctPhrase)));
            choicesPanel.add(btnOption);
        }

        choicesPanel.revalidate();
        choicesPanel.repaint();
    }

    private void processChoice(boolean correct) {
        if (correct) {
            if (currentTurn < TOTAL_TURNS) {
                currentTurn++;
                startNewTurn();
            } else {
                // Vittoria al 3° Turno!
                dispose();
                winBossFight();
            }
        } else {
            // Errore
            GameState gs = GameState.getInstance();
            gs.takeDamage();
            if (gs.getCurrentHealth() <= 0) {
                dispose();
                JOptionPane.showMessageDialog(null,
                        "La furia di Rán ti travolge! Hai esaurito i cuori.",
                        "Sconfitta", JOptionPane.ERROR_MESSAGE);
                SceneManager.getInstance().showScene("GAMEOVER");
            } else {
                JOptionPane.showMessageDialog(this, "Risposta errata! Rán ti colpisce con la sua rete. Perdi 1 Cuore!", "Errore!", JOptionPane.WARNING_MESSAGE);
                currentTurn = 1;
                startNewTurn();
            }
        }
    }

    private void winBossFight() {
        GameState gs = GameState.getInstance();
        gs.setBossDefeated(true);

        Item foam = new Item("SCHIUMA_AEGIR", "Schiuma di Ægir",
                "La birra sottomarina lucente estratta dalle reti della dea Rán.",
                "/images/items/item_foam.png");
        gs.addItem(foam);

        JOptionPane.showMessageDialog(null, "Hai decifrato la mente di Rán e spezzato le sue reti!\nHai ottenuto: Schiuma di Ægir!", "Vittoria!", JOptionPane.INFORMATION_MESSAGE);
    }
}