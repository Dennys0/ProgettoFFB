package com.alchemist.hangover.view;

import javax.swing.*;
import java.awt.*;

/** Pulsante per oggetti di scena: iconografia originale, lieve alone al passaggio. */
public final class HotspotButton extends JButton {
    public HotspotButton() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setRolloverEnabled(true);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (!isEnabled() || !getModel().isRollover()) return;
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(255, 220, 110, 35));
            g.fillRoundRect(2, 2, Math.max(0, getWidth() - 4),
                    Math.max(0, getHeight() - 4), 18, 18);
            g.setColor(new Color(255, 215, 100, 195));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(2, 2, Math.max(0, getWidth() - 5),
                    Math.max(0, getHeight() - 5), 18, 18);
        } finally {
            g.dispose();
        }
    }
}
