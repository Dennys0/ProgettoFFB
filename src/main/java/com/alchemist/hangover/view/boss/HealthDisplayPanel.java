package com.alchemist.hangover.view.boss;

import com.alchemist.hangover.model.GameState;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.net.URL;
import javax.swing.*;

/**
 * Indicatore dei cuori utilizzato durante i combattimenti.
 * Si iscrive agli eventi di salute solo mentre appartiene alla gerarchia Swing.
 */
public class HealthDisplayPanel extends JPanel {
    private final JLabel[] hearts;
    private final ImageIcon fullIcon;
    private final ImageIcon emptyIcon;
    private final PropertyChangeListener healthListener = event -> refresh();

    public HealthDisplayPanel(int iconSize) {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        fullIcon = loadIcon("/images/ui/heart_full.png", iconSize);
        emptyIcon = loadIcon("/images/ui/heart_empty.png", iconSize);

        int count = Math.max(3, GameState.getInstance().getMaxHealth());
        hearts = new JLabel[count];
        for (int i = 0; i < hearts.length; i++) {
            JLabel heart = new JLabel();
            heart.setPreferredSize(new Dimension(iconSize, iconSize));
            heart.setHorizontalAlignment(SwingConstants.CENTER);
            hearts[i] = heart;
            add(heart);
        }
        refresh();
    }

    private ImageIcon loadIcon(String path, int size) {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            System.err.println("[HealthDisplayPanel] Immagine mancante: " + path);
            return null;
        }
        Image image = new ImageIcon(resource).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    /**
     * Collega l’indicatore agli eventi di salute quando viene inserito nella gerarchia grafica.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        GameState.getInstance().addHealthChangeListener(healthListener);
        refresh();
    }

    /**
     * Rimuove l’ascoltatore quando il componente viene scollegato, evitando aggiornamenti a
     * pannelli non più utilizzati.
     */
    @Override
    public void removeNotify() {
        GameState.getInstance().removeHealthChangeListener(healthListener);
        super.removeNotify();
    }

    /**
     * Ridisegna cuori pieni e vuoti in base alla salute corrente.
     */
    public void refresh() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refresh);
            return;
        }
        int health = GameState.getInstance().getCurrentHealth();
        for (int i = 0; i < hearts.length; i++) {
            boolean filled = i < health;
            ImageIcon icon = filled ? fullIcon : emptyIcon;
            hearts[i].setIcon(icon);
            hearts[i].setText(icon == null ? (filled ? "\u2665" : "\u2661") : "");
            hearts[i].setForeground(filled ? Color.RED : Color.LIGHT_GRAY);
        }
        repaint();
    }
}
