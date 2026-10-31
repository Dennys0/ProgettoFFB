package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;
import javax.swing.*;
import java.awt.*;

/**
 * Intermezzo narrativo dopo la ricetta del Capitolo II:
 * conduce al Capitolo III senza azzerare la partita o gli slot H2.
 */
public final class Chapter3PreviewPanel extends JPanel {
    public Chapter3PreviewPanel() {
        setLayout(null);
        JLabel heading = new JLabel("FINE DEL CAPITOLO II", SwingConstants.CENTER);
        heading.setForeground(new Color(255, 220, 150));
        heading.setFont(new Font("Georgia", Font.BOLD, 39));
        heading.setBounds(100, 190, 1080, 75);
        add(heading);

        JLabel message = new JLabel("<html><center>"
                + "Il Maelstrom Martini è finalmente pronto.<br>"
                + "Il viaggio di Arthur proseguirà nel Capitolo III...<br>"
                + "Il ponte si incrina: sotto i piedi di Arthur batte la pelle stessa di Ymir.<br>"
                + "L'antico mondo di Terra attende il suo prossimo intruso."
                + "</center></html>", SwingConstants.CENTER);
        message.setFont(new Font("Georgia", Font.ITALIC, 21));
        message.setForeground(new Color(225, 222, 226));
        message.setBounds(200, 275, 880, 135);
        add(message);

        JButton back = new JButton("TORNA A ESPLORARE");
        back.setBounds(175, 475, 270, 52);
        back.addActionListener(e -> SceneManager.getInstance().showScene("CHAPTER2"));
        add(back);

        JButton next = new JButton("ENTRA NEL CAPITOLO III");
        next.setBounds(460, 475, 355, 52);
        next.addActionListener(e -> {
            GameState state = GameState.getInstance();
            state.setChapter(3);
            state.setCurrentRoom(1);
            state.resetHealth();
            state.setBossRetryAvailable(false);
            state.ensureYmirRuneSeed();
            Chapter3Panel chapter = (Chapter3Panel) SceneManager.getInstance().getPanel("CHAPTER3");
            if (chapter != null) chapter.refreshFromGameState();
            SceneManager.getInstance().showScene("CHAPTER3");
        });
        add(next);

        JButton menu = new JButton("MENU PRINCIPALE");
        menu.setBounds(830, 475, 270, 52);
        menu.addActionListener(e -> SceneManager.getInstance().showScene("MENU"));
        add(menu);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setPaint(new GradientPaint(0, 0, new Color(18, 30, 49),
                    0, getHeight(), new Color(12, 8, 24)));
            g.fillRect(0, 0, getWidth(), getHeight());
        } finally {
            g.dispose();
        }
    }
}
