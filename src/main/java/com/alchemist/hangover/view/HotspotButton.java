package com.alchemist.hangover.view;

import javax.swing.*;
import java.awt.*;

/**
 * Area interattiva per oggetti e personaggi.
 *
 * NPC, calderoni e dettagli dello scenario sono invisibili anche al passaggio
 * del mouse: cambiano soltanto cursore e suggerimento contestuale.
 * Il bagliore rettangolare e' disponibile solo se abilitato esplicitamente
 * per gli oggetti raccoglibili.
 */
public final class HotspotButton extends JButton {
    private boolean hoverHighlightEnabled;

    public HotspotButton() {
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(false);
        setRequestFocusEnabled(false);
        setMargin(new Insets(0, 0, 0, 0));
        setBorder(null);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setRolloverEnabled(true);
    }

    public void setHoverHighlightEnabled(boolean enabled) {
        hoverHighlightEnabled = enabled;
        repaint();
    }

    public boolean isHoverHighlightEnabled() {
        return hoverHighlightEnabled;
    }

    @Override
    public JToolTip createToolTip() {
        // Un'etichetta scura discreta, non il classico riquadro giallo.
        JToolTip tooltip = super.createToolTip();
        tooltip.setOpaque(true);
        tooltip.setBackground(new Color(31, 27, 38));
        tooltip.setForeground(new Color(245, 225, 189));
        tooltip.setFont(new Font("Georgia", Font.PLAIN, 14));
        tooltip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 85, 70)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return tooltip;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (!hoverHighlightEnabled || !isEnabled() || !getModel().isRollover()) return;
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
