package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import javax.swing.*;
import java.awt.*;

/**
 * Schermata provvisoria dopo la ricetta del capitolo II.
 * Non assegna ancora il capitolo 3: il salvataggio resta compatibile.
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
                + "Una nuova destinazione attende lo Skíðblaðnir."
                + "</center></html>", SwingConstants.CENTER);
        message.setFont(new Font("Georgia", Font.ITALIC, 21));
        message.setForeground(new Color(225, 222, 226));
        message.setBounds(200, 275, 880, 135);
        add(message);

        JButton back = new JButton("TORNA A ESPLORARE");
        back.setBounds(355, 475, 275, 52);
        back.addActionListener(e -> SceneManager.getInstance().showScene("CHAPTER2"));
        add(back);

        JButton menu = new JButton("MENU PRINCIPALE");
        menu.setBounds(650, 475, 275, 52);
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
