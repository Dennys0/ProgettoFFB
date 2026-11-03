package com.alchemist.hangover.view.cauldron;

import java.awt.*;
import java.net.URL;
import javax.swing.*;

/**
 * Cornice illustrata comune dei calderoni, usando l'immagine del Capitolo I.
 * Non contiene alcuna regola di gioco: ogni capitolo può preparare una
 * ricetta diversa conservando la stessa identità visiva.
 */
public class CauldronArtworkPanel extends JPanel {
    private static final String IMAGE_PATH = "/images/ui/cauldron_bg.png";
    private static final Image IMAGE = loadImage();

    public CauldronArtworkPanel() {
        setLayout(null);
        setOpaque(false);
    }

    /**
     * Adatta la cornice illustrata alle dimensioni del pannello senza applicare regole di
     * miscela.
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            if (IMAGE != null) {
                g.drawImage(IMAGE, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setPaint(new GradientPaint(0, 0, new Color(51, 35, 39),
                        getWidth(), getHeight(), new Color(24, 26, 40)));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g.setColor(new Color(205, 161, 97));
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
            }
        } finally {
            g.dispose();
        }
    }

    /**
     * Carica l’illustrazione condivisa dalle finestre del calderone.
     */
    private static Image loadImage() {
        URL resource = CauldronArtworkPanel.class.getResource(IMAGE_PATH);
        return resource == null ? null : new ImageIcon(resource).getImage();
    }
}
