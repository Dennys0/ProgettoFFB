package com.alchemist.hangover.view.chapter;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.*;

/**
 * Pulsante trasparente per le interazioni nei quattro capitoli.
 * Al passaggio del mouse mostra un bagliore e la descrizione dell’azione.
 * I contorni delle aree cliccabili compaiono soltanto in modalità sviluppatore.
 */
public final class HotspotButton extends JButton {
    // Modalita' di sviluppo: inattiva di default, quindi mai visibile al giocatore.
    private static boolean showDebugBounds;

    public static boolean debugBoundsVisible() { return showDebugBounds; }
    public static void setDebugBoundsVisible(boolean enabled) { showDebugBounds = enabled; }
    private boolean collectibleEmphasis;
    private int pointerX = -1;
    private int pointerY = -1;

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
        ToolTipManager.sharedInstance().setInitialDelay(200);
        ToolTipManager.sharedInstance().setDismissDelay(4200);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                pointerX = event.getX();
                pointerY = event.getY();
                repaint();
            }
        });
    }

    /** Gli oggetti raccoglibili possono mostrare un bagliore più evidente dello scenario. */
    public void setHoverHighlightEnabled(boolean enabled) {
        collectibleEmphasis = enabled;
        repaint();
    }

    public boolean isHoverHighlightEnabled() {
        return collectibleEmphasis;
    }

    /**
     * Applica lo stile comune alla descrizione mostrata dal sistema di suggerimenti Swing.
     */
    @Override
    public JToolTip createToolTip() {
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
        if (showDebugBounds && isVisible()) {
            Graphics2D debug = (Graphics2D) graphics.create();
            try {
                debug.setColor(new Color(0, 215, 158, 48));
                debug.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
                debug.setColor(new Color(68, 255, 189, 230));
                debug.setStroke(new BasicStroke(2f));
                debug.drawRect(1, 1, Math.max(0, getWidth() - 3), Math.max(0, getHeight() - 3));
                debug.setFont(new Font("SansSerif", Font.BOLD, 11));
                String label = getToolTipText();
                if (label == null || label.isBlank()) label = "Hotspot";
                String info = getX() + "," + getY() + "  " + getWidth() + "x" + getHeight();
                int width = Math.min(getWidth() - 4, Math.max(0,
                        debug.getFontMetrics().stringWidth(info) + 6));
                if (width > 0 && getHeight() >= 16) {
                    debug.setColor(new Color(14, 39, 34, 225));
                    debug.fillRoundRect(2, 2, width, 17, 5, 5);
                    debug.setColor(Color.WHITE);
                    debug.drawString(info, 5, 14);
                }
                if (getHeight() >= 42) {
                    debug.setColor(new Color(14, 39, 34, 220));
                    debug.fillRoundRect(2, 22, Math.max(0, getWidth() - 4), 20, 5, 5);
                    debug.setColor(Color.WHITE);
                    debug.setClip(4, 22, Math.max(0, getWidth() - 8), 19);
                    debug.drawString(label, 6, 36);
                }
            } finally { debug.dispose(); }
        }
        if (!isEnabled() || !getModel().isRollover()) return;

        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int x = pointerX < 0 ? getWidth() / 2 : pointerX;
            int y = pointerY < 0 ? getHeight() / 2 : pointerY;
            int radius = collectibleEmphasis ? 43 : 34;

            // Luce circolare localizzata, senza un bordo rettangolare nell’interfaccia normale.
            Paint light = new RadialGradientPaint(new Point(x, y), radius,
                    new float[]{0f, 0.34f, 1f},
                    new Color[]{
                        new Color(255, 214, 129, collectibleEmphasis ? 132 : 102),
                        new Color(248, 182, 92, 52),
                        new Color(248, 182, 92, 0)
                    });
            g.setPaint(light);
            g.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        } finally {
            g.dispose();
        }
    }

}
