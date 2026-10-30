package com.alchemist.hangover.controller;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/** Gestisce gli scenari e le transizioni, senza ricreare i pannelli. */
public final class SceneManager {
    private static SceneManager instance;
    private final JPanel mainPanel;
    private final CardLayout cardLayout;
    private final Map<String, JPanel> scenes;

    private float alpha = 0f;
    private boolean isFading = false;
    private String currentSceneName = "MENU";
    private JGlassPane glassPane;
    private Runnable whenVisible;

    private SceneManager() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        scenes = new HashMap<>();
    }

    public static SceneManager getInstance() {
        if (instance == null) instance = new SceneManager();
        return instance;
    }

    public JPanel getMainPanel() { return mainPanel; }
    public JPanel getPanel(String name) { return scenes.get(name); }
    public String getCurrentSceneName() { return currentSceneName; }

    public void addPanel(String name, JPanel panel) {
        scenes.put(name, panel);
        mainPanel.add(panel, name);
    }

    public void showScene(String name) {
        showScene(name, null);
    }

    /** Callback richiamata solo dopo il completamento della dissolvenza. */
    public void showScene(String name, Runnable onShown) {
        if (!scenes.containsKey(name) || isFading) return;
        if (!GameSettings.getInstance().isTransitionsEnabled()) {
            activateScene(name);
            if (onShown != null) SwingUtilities.invokeLater(onShown);
            return;
        }

        isFading = true;
        whenVisible = onShown;
        alpha = 0f;
        JGlassPane glass = getGlassPane();
        if (glass != null) {
            glass.setVisible(true);
            glass.repaint();
        }

        Timer fadeOut = new Timer(25, null);
        fadeOut.addActionListener(e -> {
            alpha = Math.min(1f, alpha + 0.085f);
            if (glass != null) glass.repaint();
            if (alpha >= 1f) {
                fadeOut.stop();
                activateScene(name);
                fadeIn(glass);
            }
        });
        fadeOut.start();
    }

    private void fadeIn(JGlassPane glass) {
        Timer fadeIn = new Timer(25, null);
        fadeIn.addActionListener(e -> {
            alpha = Math.max(0f, alpha - 0.085f);
            if (glass != null) glass.repaint();
            if (alpha <= 0f) {
                fadeIn.stop();
                isFading = false;
                if (glass != null) glass.setVisible(false);
                Runnable callback = whenVisible;
                whenVisible = null;
                if (callback != null) SwingUtilities.invokeLater(callback);
            }
        });
        fadeIn.start();
    }

    private void activateScene(String name) {
        cardLayout.show(mainPanel, name);
        currentSceneName = name;
        AudioManager.getInstance().playScene(name);
        scenes.get(name).requestFocusInWindow();
    }

    private JGlassPane getGlassPane() {
        Window owner = SwingUtilities.getWindowAncestor(mainPanel);
        if (!(owner instanceof JFrame frame)) return null;
        if (!(frame.getGlassPane() instanceof JGlassPane)) {
            glassPane = new JGlassPane();
            frame.setGlassPane(glassPane);
        } else {
            glassPane = (JGlassPane) frame.getGlassPane();
        }
        return glassPane;
    }

    private final class JGlassPane extends JPanel {
        private JGlassPane() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (alpha <= 0f) return;
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setComposite(AlphaComposite.SrcOver.derive(alpha));
                g.setPaint(new GradientPaint(0, 0, new Color(10, 9, 22),
                        0, getHeight(), new Color(26, 17, 35)));
                g.fillRect(0, 0, getWidth(), getHeight());
            } finally {
                g.dispose();
            }
        }
    }
}
