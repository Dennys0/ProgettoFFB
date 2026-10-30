package com.alchemist.hangover.controller;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {

    private static SceneManager instance;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private Map<String, JPanel> scenes;

    // Gestione Dissolvenza (Fade Effect)
    private float alpha = 0.0f;
    private Timer fadeTimer;
    private boolean isFading = false;
    private String pendingSceneName = null;

    private SceneManager() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        scenes = new HashMap<>();
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void addPanel(String name, JPanel panel) {
        scenes.put(name, panel);
        mainPanel.add(panel, name);
    }

    public JPanel getPanel(String name) {
        return scenes.get(name);
    }

    // Passaggio di scena con effetto Fade Out -> Fade In
    public void showScene(String name) {
        if (isFading || !scenes.containsKey(name)) return;

        isFading = true;
        pendingSceneName = name;
        alpha = 0.0f;

        JGlassPane glassPane = getGlassPane();
        if (glassPane != null) {
            glassPane.setVisible(true);
        }

        fadeTimer = new Timer(25, e -> {
            alpha += 0.08f;
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                fadeTimer.stop();

                // Cambia la scena effettiva
                cardLayout.show(mainPanel, pendingSceneName);
                scenes.get(pendingSceneName).requestFocusInWindow();

                // Avvia Fade In (da nero a trasparente)
                fadeOutEffect();
            }
            if (glassPane != null) glassPane.repaint();
        });
        fadeTimer.start();
    }

    private void fadeOutEffect() {
        fadeTimer = new Timer(25, e -> {
            alpha -= 0.08f;
            if (alpha <= 0.0f) {
                alpha = 0.0f;
                fadeTimer.stop();
                isFading = false;

                JGlassPane glassPane = getGlassPane();
                if (glassPane != null) {
                    glassPane.setVisible(false);
                }
            }
            JGlassPane glassPane = getGlassPane();
            if (glassPane != null) glassPane.repaint();
        });
        fadeTimer.start();
    }

    private JGlassPane getGlassPane() {
        Window window = SwingUtilities.getWindowAncestor(mainPanel);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            if (!(frame.getGlassPane() instanceof JGlassPane)) {
                JGlassPane gp = new JGlassPane();
                frame.setGlassPane(gp);
            }
            return (JGlassPane) frame.getGlassPane();
        }
        return null;
    }

    // Componente personalizzato per il rendering del nero in trasparenza
    private class JGlassPane extends JPanel {
        public JGlassPane() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (alpha > 0.0f) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(0, 0, 0, Math.min(255, (int) (alpha * 255))));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }
}
