package com.alchemist.hangover.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * Shared world interaction feedback for Chapters I, II and III.
 *
 * Every hotspot remains visually transparent at rest. Hovering shows a
 * small localized light and an elegant dark interaction label close to
 * the pointer. No rectangles around characters, cauldrons or scenery.
 */
public final class HotspotButton extends JButton {
    private static final Font HINT_FONT = new Font("Georgia", Font.BOLD, 12);
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
        ToolTipManager.sharedInstance().setInitialDelay(220);
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

    /** Collectibles have a warmer, slightly stronger glint than scenery. */
    public void setHoverHighlightEnabled(boolean enabled) {
        collectibleEmphasis = enabled;
        repaint();
    }

    public boolean isHoverHighlightEnabled() {
        return collectibleEmphasis;
    }

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
        if (!isEnabled() || !getModel().isRollover()) return;

        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int x = pointerX < 0 ? getWidth() / 2 : pointerX;
            int y = pointerY < 0 ? getHeight() / 2 : pointerY;
            int radius = collectibleEmphasis ? 43 : 34;

            // A localized circular light, never a rectangular outline.
            Paint light = new RadialGradientPaint(new Point(x, y), radius,
                    new float[]{0f, 0.34f, 1f},
                    new Color[]{
                        new Color(255, 214, 129, collectibleEmphasis ? 132 : 102),
                        new Color(248, 182, 92, 52),
                        new Color(248, 182, 92, 0)
                    });
            g.setPaint(light);
            g.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            String label = getToolTipText();
            if (label == null || label.isBlank() || getWidth() < 45 || getHeight() < 22) return;

            g.setFont(HINT_FONT);
            FontMetrics fm = g.getFontMetrics();
            int allowed = Math.max(15, getWidth() - 20);
            String visible = fitText(label, fm, allowed);
            int width = Math.min(getWidth() - 8, fm.stringWidth(visible) + 17);
            int height = Math.min(25, getHeight() - 3);
            int left = Math.max(4, Math.min(getWidth() - width - 4,
                    x - width / 2));
            int top = Math.max(2, Math.min(getHeight() - height - 2,
                    y - radius - height / 2));
            g.setColor(new Color(21, 21, 28, 218));
            g.fillRoundRect(left, top, width, height, 13, 13);
            g.setColor(new Color(245, 218, 170));
            g.drawString(visible, left + 8,
                    top + (height - fm.getHeight()) / 2 + fm.getAscent());
        } finally {
            g.dispose();
        }
    }

    private static String fitText(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) return text;
        String result = text;
        while (!result.isEmpty() && fm.stringWidth(result + "...") > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
        return result.isEmpty() ? "..." : result.stripTrailing() + "...";
    }
}
