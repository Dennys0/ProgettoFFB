package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;
import javax.swing.*;
import java.awt.*;

/**
 * Epilogo del Capitolo III: il giocatore puo' salvare lo stato COMPLETATO
 * senza tornare obbligatoriamente allo scenario della Forgia.
 */
public final class Chapter3CompletePanel extends JPanel {
    public Chapter3CompletePanel(JFrame mainFrame) {
        setLayout(null);

        JLabel title = new JLabel("CAPITOLO III COMPLETATO", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 43));
        title.setForeground(new Color(249, 213, 131));
        title.setBounds(140, 68, 1000, 80);
        add(title);

        // Illustrazione della bevanda completata; il pannello rimane
        // utilizzabile anche prima che cocktail_3.png sia pubblicato.
        JLabel finishedDrink = new JLabel("Ymir's Old Fashioned", SwingConstants.CENTER);
        finishedDrink.setFont(new Font("Georgia", Font.ITALIC, 28));
        finishedDrink.setForeground(new Color(236, 191, 119));
        finishedDrink.setBounds(430, 151, 420, 270);
        java.net.URL drinkUrl = getClass().getResource("/images/items/cocktail_3.png");
        if (drinkUrl != null) {
            Image img = new ImageIcon(drinkUrl).getImage()
                    .getScaledInstance(300, 270, Image.SCALE_SMOOTH);
            finishedDrink.setIcon(new ImageIcon(img));
            finishedDrink.setText("");
        }
        add(finishedDrink);

        JLabel ending = new JLabel("<html><center>"
                + "Il Muschio di Midgard, il Frammento di Yggdrasil e la Lacrima di Ymir<br>"
                + "si fondono in un drink fumante di energia primordiale.<br>"
                + "Ymir riposa, la terra e' finalmente quieta.<br>"
                + "Arthur: «Alla prossima catastrofe. Possibilmente dopo un aperitivo.»"
                + "</center></html>", SwingConstants.CENTER);
        ending.setFont(new Font("Georgia", Font.PLAIN, 20));
        ending.setForeground(new Color(239, 230, 216));
        ending.setBounds(95, 420, 1090, 94);
        add(ending);

        JLabel reminder = new JLabel(
                "Puoi salvare il capitolo completato direttamente da qui.", SwingConstants.CENTER);
        reminder.setFont(new Font("Georgia", Font.ITALIC, 16));
        reminder.setForeground(new Color(214, 188, 151));
        reminder.setBounds(220, 524, 840, 30);
        add(reminder);

        JButton saveButton = new JButton("SALVA PARTITA");
        saveButton.setBounds(128, 582, 308, 55);
        saveButton.setToolTipText("Salva l'Ymir's Old Fashioned completato in uno slot H2");
        saveButton.addActionListener(e ->
                new SaveLoadDialog(mainFrame, SaveLoadDialog.Mode.SAVE).setVisible(true));
        add(saveButton);

        JButton returnButton = new JButton("TORNA ALLA FORGIA");
        returnButton.setBounds(486, 582, 308, 55);
        returnButton.addActionListener(e -> {
            GameState state = GameState.getInstance();
            state.setCurrentRoom(3);
            Chapter3Panel chapter = (Chapter3Panel)
                    SceneManager.getInstance().getPanel("CHAPTER3");
            if (chapter != null) chapter.refreshFromGameState();
            SceneManager.getInstance().showScene("CHAPTER3");
        });
        add(returnButton);

        JButton menuButton = new JButton("MENU PRINCIPALE");
        menuButton.setBounds(844, 582, 308, 55);
        menuButton.addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(this,
                    "Hai salvato il capitolo completato? I progressi non salvati andranno persi.",
                    "Torna al menu", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                SceneManager.getInstance().showScene("MENU");
            }
        });
        add(menuButton);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setPaint(new GradientPaint(0, 0, new Color(31, 24, 26),
                    0, getHeight(), new Color(12, 20, 35)));
            g.fillRect(0, 0, getWidth(), getHeight());
        } finally {
            g.dispose();
        }
    }
}
