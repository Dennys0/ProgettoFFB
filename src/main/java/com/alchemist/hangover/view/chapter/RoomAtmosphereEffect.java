package com.alchemist.hangover.view.chapter;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import javax.swing.*;

/**
 * Atmosfera animata condivisa dai quattro capitoli.
 *
 * Il fondale resta il PNG originale: qui vengono disegnate soltanto
 * poche particelle traslucide. Il componente e' click-through e il Timer
 * si arresta quando il capitolo non e' visibile.
 */
public final class RoomAtmosphereEffect extends JComponent {
    /** Famiglie di particelle scelte in base al fondale. */
    public enum Style { SKY, SEA, EMBERS, FIREFLIES, EARTH }

    private final Timer timer;
    private Style style = Style.EARTH;
    private int tick;

    public RoomAtmosphereEffect() {
        setOpaque(false);
        setFocusable(false);
        timer = new Timer(100, e -> { tick++; repaint(); });
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0)
                refreshAnimationState();
        });
    }

    /**
     * Associa il fondale a una famiglia di particelle senza modificare l’immagine della stanza.
     */
    static Style styleFor(String background) {
        if (background == null) return Style.EARTH;
        if (background.contains("/chapter1/")) {
            return background.contains("room4_lab") ? Style.EMBERS : Style.SKY;
        }
        if (background.contains("/chapter2/")) return Style.SEA;
        if (background.contains("/chapter4/")) return Style.EMBERS;
        if (background.contains("/chapter3/")) {
            if (background.contains("room3_forge")) return Style.EMBERS;
            if (background.contains("room4_forest")
                    || background.contains("room2_rune")) return Style.FIREFLIES;
        }
        return Style.EARTH;
    }

    /**
     * Sceglie l’atmosfera dal percorso del fondale e riallinea lo stato dell’animazione.
     */
    public void configure(String background) {
        style = styleFor(background);
        tick = 0;
        refreshAnimationState();
        repaint();
    }

    /**
     * Fa lavorare il timer solo quando l’effetto deve essere visibile.
     */
    private void refreshAnimationState() {
        if (isShowing() && getWidth() > 0 && getHeight() > 0) {
            if (!timer.isRunning()) timer.start();
        } else {
            timer.stop();
        }
    }

    /** Un velo d'atmosfera non deve mai impedire click e rollover. */
    @Override
    public boolean contains(int x, int y) {
        return false;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (getWidth() <= 0 || getHeight() <= 0) return;

        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            // Posizioni deterministiche: non serve creare un Random o PNG ad ogni frame.
            for (int i = 0; i < 17; i++) {
                int seed = i * 173 + 41;
                int radius = 2 + (i % 4);
                float pulse = (float) (0.55 + 0.45
                        * Math.sin(tick * 0.12 + i * 1.83));
                int alpha = Math.round((style == Style.EMBERS ? 61 : 37) * pulse);
                int x = Math.floorMod(seed * 7 + tick *
                        (style == Style.SKY ? 4 : (i % 3)), w);
                int y = Math.floorMod(seed * 13 +
                        (style == Style.EMBERS || style == Style.FIREFLIES
                                ? -tick * (2 + i % 3)
                                : tick * (1 + i % 2)), h);

                Color tint = switch (style) {
                    case SKY -> new Color(230, 215, 182, alpha);
                    case SEA -> new Color(150, 230, 249, alpha);
                    case EMBERS -> new Color(255, 169, 85, alpha);
                    case FIREFLIES -> new Color(87, 229, 255, alpha);
                    case EARTH -> new Color(225, 195, 147, alpha);
                };
                g.setColor(tint);
                if (style == Style.SKY) {
                    g.setStroke(new BasicStroke(1f));
                    g.drawLine(x, y, Math.min(w, x + 26 + (i % 3) * 12), y - 5);
                } else if (style == Style.SEA) {
                    g.setStroke(new BasicStroke(1f));
                    g.drawOval(x, y, radius * 2, radius * 2);
                } else {
                    g.fillOval(x, y, radius, radius);
                }
            }
        } finally {
            g.dispose();
        }
    }
}
