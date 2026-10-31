package com.alchemist.hangover.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * Pulsazione breve e semitrasparente sulla crepa della Radura.
 * L'effetto e' solo disegnato; il fondale PNG non viene modificato.
 */
public final class EarthBreathEffect extends JComponent {
    private static final int FRAME_MS = 60;
    private static final int LAST_FRAME = 48;
    private Timer timer;
    private int frame;

    public EarthBreathEffect() {
        setOpaque(false);
        setFocusable(false);
        setVisible(false);
    }

    public void startEffect() {
        stopEffect();
        frame = 0;
        setVisible(true);
        timer = new Timer(FRAME_MS, e -> {
            frame++;
            if (frame >= LAST_FRAME) stopEffect();
            else repaint();
        });
        timer.start();
        repaint();
    }

    public void stopEffect() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        setVisible(false);
    }

    public boolean isActive() {
        return timer != null && timer.isRunning() && isVisible();
    }

    @Override
    public boolean contains(int x, int y) {
        // L'overlay non deve intercettare frecce, HUD o clic.
        return false;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (!isVisible()) return;

        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            double t = frame / (double) LAST_FRAME;
            double heartbeat = Math.pow(Math.sin(Math.PI * Math.min(1.0, t * 2)), 2);
            double fade = Math.min(1.0, Math.min(t * 6 + 0.1, (1 - t) * 3));
            float intensity = (float) Math.max(0, heartbeat * fade);
            if (intensity <= 0) return;

            Path2D path = new Path2D.Float();
            path.moveTo(getWidth() * .07, getHeight() * .70);
            path.lineTo(getWidth() * .30, getHeight() * .57);
            path.lineTo(getWidth() * .42, getHeight() * .69);
            path.lineTo(getWidth() * .56, getHeight() * .49);
            path.lineTo(getWidth() * .75, getHeight() * .63);
            path.lineTo(getWidth() * .95, getHeight() * .44);

            g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(75, 193, 247, Math.min(255, (int) (53 * intensity))));
            g.draw(path);
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(163, 229, 255, Math.min(255, (int) (180 * intensity))));
            g.draw(path);

            g.setColor(new Color(128, 219, 255, Math.min(255, (int) (70 * intensity))));
            float growth = 1 + (float) (t * .36);
            double width = getWidth() * .66 * growth;
            double height = getHeight() * .50 * growth;
            double centerX = getWidth() * .55;
            double centerY = getHeight() * .58;
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new Ellipse2D.Double(centerX - width / 2,
                    centerY - height / 2, width, height));
        } finally {
            g.dispose();
        }
    }
}
