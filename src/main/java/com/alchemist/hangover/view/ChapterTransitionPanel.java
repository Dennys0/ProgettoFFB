package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;

import javax.swing.*;
import java.awt.*;

public class ChapterTransitionPanel extends JPanel {

    private JLabel titleLabel;
    private JLabel storyLabel;
    private JButton continueButton;

    public ChapterTransitionPanel() {
        setLayout(null);
        setBackground(new Color(15, 12, 20));

        // Titolo del Capitolo
        titleLabel = new JLabel("CAPITOLO I COMPLETATO", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 38));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setBounds(140, 100, 1000, 50);
        add(titleLabel);

        // Testo di Intermezzo Narrativo
        String storyText = "<html><center>"
                + "L'<b>Asgardian Aviation</b> brilla nel calderone d'ottone di una luce azzurra e pulsante.<br>"
                + "Bevendo il primo sorso, il sapore del vento e del tuono ti invade i sensi, dissolvendo le nebbie della mente.<br><br>"
                + "Il veliero <i>Skíðblaðnir</i> fende l'Orizzonte della Tempesta e comincia la sua discesa verso i mari inferiori.<br>"
                + "All'orizzonte, le acque ribollono e all'improvviso una figura imponente emerge dalle onde tenendo in mano un corno dorato...<br><br>"
                + "<b>IL PROSSIMO REGNO TI ATTENDE.</b>"
                + "</center></html>";

        storyLabel = new JLabel(storyText, SwingConstants.CENTER);
        storyLabel.setFont(new Font("Georgia", Font.PLAIN, 18));
        storyLabel.setForeground(new Color(220, 220, 230));
        storyLabel.setBounds(190, 180, 900, 280);
        add(storyLabel);

        // Pulsante di Avanzamento al Capitolo 2
        continueButton = new JButton("ENTRA NEL CAPITOLO II -> ");
        continueButton.setFont(new Font("Georgia", Font.BOLD, 18));
        continueButton.setForeground(Color.BLACK);
        continueButton.setBackground(new Color(255, 215, 0));
        continueButton.setFocusPainted(false);
        continueButton.setFocusable(false);
        continueButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        continueButton.setBounds(440, 500, 400, 55);

        continueButton.addActionListener(e -> {
            // Avanza lo stato di gioco al Capitolo 2
            GameState.getInstance().setChapter(2);
            // Mostreremo la scena del Capitolo 2
            SceneManager.getInstance().showScene("CHAPTER2");
        });
        add(continueButton);
    }
}