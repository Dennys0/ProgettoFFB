package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import javax.swing.*;
import java.awt.*;

/** Epilogo del terzo capitolo dopo la creazione dell'Ymir's Old Fashioned. */
public final class Chapter3CompletePanel extends JPanel {
    public Chapter3CompletePanel() {
        setLayout(null);
        JLabel title = new JLabel("CAPITOLO III COMPLETATO", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 43));
        title.setForeground(new Color(249, 213, 131));
        title.setBounds(140, 145, 1000, 80);
        add(title);

        JLabel ending = new JLabel("<html><center>"
                + "Il boccale di pietra custodisce finalmente l'<b>Ymir's Old Fashioned</b>.<br><br>"
                + "Il Muschio di Midgard libera il profumo della terra pulita; il Frammento di Yggdrasil<br>"
                + "illumina il liquido con una luce millenaria; la Lacrima di Ymir lo rende immortale.<br><br>"
                + "Il gigante primordiale si addormenta. Il mondo smette di tremare.<br>"
                + "Arthur solleva il boccale: «Alla prossima catastrofe. Possibilmente dopo un aperitivo.»"
                + "</center></html>", SwingConstants.CENTER);
        ending.setFont(new Font("Georgia", Font.PLAIN, 21));
        ending.setForeground(new Color(239, 230, 216));
        ending.setBounds(100, 250, 1080, 230);
        add(ending);

        JButton returnButton = new JButton("TORNA ALLA FORGIA (PER SALVARE)");
        returnButton.setBounds(260, 535, 410, 55);
        returnButton.addActionListener(e -> SceneManager.getInstance().showScene("CHAPTER3"));
        add(returnButton);

        JButton menuButton = new JButton("MENU PRINCIPALE");
        menuButton.setBounds(710, 535, 285, 55);
        menuButton.addActionListener(e -> SceneManager.getInstance().showScene("MENU"));
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
