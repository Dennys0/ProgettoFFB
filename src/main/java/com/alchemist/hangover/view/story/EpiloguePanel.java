package com.alchemist.hangover.view.story;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.view.menu.SaveLoadDialog;
import java.awt.*;
import java.net.URL;
import javax.swing.*;

/**
 * Epilogo cinematografico dopo il Ragnarök Flambé.
 * Non altera i progressi: il salvataggio resta disponibile prima del menu.
 */
public final class EpiloguePanel extends JPanel {
    private final JFrame owner;
    private final JLabel heading = new JLabel("", SwingConstants.CENTER);
    private final JLabel narration = new JLabel("", SwingConstants.CENTER);
    private final JButton next = new JButton("CONTINUA");
    private final JLabel cocktail = new JLabel();
    private int page;
    private final Image backdrop;

    private static final String[] TITLES = {
        "L'ULTIMO SORSO", "IL RISVEGLIO", "UNA NOTTE IMPOSSIBILE",
        "GRAZIE PER AVER GIOCATO"
    };

    private static final String[] STORY = {
        "<html><center>Arthur avvicina il Ragnarök Flambé alle labbra.<br>"
        + "Il fuoco della Brace attraversa la Cenere di Fenrir e il Magma filtrato.<br>"
        + "Una vampata bianca, un boato... e il mondo si dissolve.</center></html>",
        "<html><center>Il respiro di Surtur gratta ancora. Poi Martino, addormentato sul tavolo, cambia fianco.<br>"
        + "Siegfried posa il cucchiaio. Gunnar ferma il tavolo con due dita.<br>"
        + "Hela tiene ancora il bicchiere d’acqua.</center></html>",
        "<html><center>«Stavolta la prendo subito.» Arthur beve. Nessuno gli chiede dove sia stato.<br>"
        + "Mette una sedia in più vicino alla finestra. «Aspetti qualcuno?» chiede Hela.<br>"
        + "«Uno che teneva il conto meglio di me.» Sul davanzale crepita una scintilla.</center></html>",
        "<html><center>THE ALCHEMIST'S HANGOVER<br><br>"
        + "Avventura realizzata come progetto universitario in Java Swing.<br>"
        + "Salvataggi H2, dialoghi, inventario, boss e alchimia.<br><br>"
        + "Grazie per aver accompagnato Arthur tra i Nove Mondi.</center></html>"
    };

    public EpiloguePanel(JFrame frame) {
        owner = frame;
        setLayout(null);
        setPreferredSize(new Dimension(1280, 720));
        URL url = getClass().getResource("/images/intro/menu_bg.png");
        backdrop = url == null ? null : new ImageIcon(url).getImage();

        URL drinkUrl = getClass().getResource("/images/chapter4/items/ragnarok_flambe.png");
        if (drinkUrl != null) {
            ImageIcon source = new ImageIcon(drinkUrl);
            double scale = Math.min(300.0 / source.getIconWidth(), 300.0 / source.getIconHeight());
            cocktail.setIcon(new ImageIcon(source.getImage().getScaledInstance(
                    (int) (source.getIconWidth() * scale),
                    (int) (source.getIconHeight() * scale), Image.SCALE_SMOOTH)));
        }
        cocktail.setBounds(115, 200, 300, 300);
        cocktail.setHorizontalAlignment(SwingConstants.CENTER);
        add(cocktail);

        heading.setBounds(110, 95, 1060, 85);
        heading.setFont(new Font("Georgia", Font.BOLD, 47));
        heading.setForeground(new Color(247, 197, 119));
        add(heading);

        narration.setBounds(110, 208, 1060, 280);
        narration.setFont(new Font("Georgia", Font.PLAIN, 25));
        narration.setForeground(new Color(242, 227, 202));
        add(narration);

        next.setBounds(475, 535, 330, 56);
        next.setFont(new Font("Georgia", Font.BOLD, 18));
        next.setBackground(new Color(217, 162, 91));
        next.setForeground(new Color(30, 20, 22));
        next.setFocusable(false);
        next.addActionListener(e -> {
            if (++page < STORY.length) refresh();
            else SceneManager.getInstance().showScene("MENU");
        });
        add(next);

        JButton save = new JButton("SALVA IL FINALE");
        save.setBounds(475, 605, 330, 40);
        save.setFont(new Font("Georgia", Font.BOLD, 15));
        save.setBackground(new Color(68, 54, 50));
        save.setForeground(new Color(240, 204, 151));
        save.setFocusable(false);
        save.addActionListener(e -> new SaveLoadDialog(owner, SaveLoadDialog.Mode.SAVE)
                .setVisible(true));
        add(save);
        refresh();
    }

    /** Rigiocando il finale si parte sempre dalla prima pagina. */
    public void restart() {
        page = 0;
        refresh();
    }

    /**
     * Aggiorna il contenuto della pagina corrente dell’epilogo.
     */
    private void refresh() {
        cocktail.setVisible(page == 0);
        narration.setBounds(page == 0 ? 440 : 110, 208, page == 0 ? 710 : 1060, 280);
        heading.setText(TITLES[page]);
        narration.setText(STORY[page]);
        next.setText(page == STORY.length - 1 ? "TORNA AL MENU" : "CONTINUA");
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            if (backdrop != null)
                g.drawImage(backdrop, 0, 0, getWidth(), getHeight(), this);
            else {
                g.setColor(new Color(28, 18, 19));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.setColor(new Color(8, 7, 11, 214));
            g.fillRect(0, 0, getWidth(), getHeight());
        } finally { g.dispose(); }
    }
}
