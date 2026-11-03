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

    private final Map<String, java.util.function.Supplier<JPanel>> factories = new HashMap<>();

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
    /**
     * Restituisce il pannello esistente o lo crea al primo accesso, conservando stato caricato e
     * punto di ripresa durante l’inizializzazione.
     */
    public JPanel getPanel(String name) {
        JPanel panel = scenes.get(name);
        if (panel != null || !factories.containsKey(name)) return panel;
        // I costruttori dei capitoli inizializzano una stanza: preserva lo stato caricato.
        var state = com.alchemist.hangover.model.GameState.getInstance();
        var snapshot = com.alchemist.hangover.model.GameSaveSnapshot.capture(state);
        boolean retry = state.isBossRetryAvailable();
        long started = System.nanoTime();
        try {
            panel = factories.get(name).get();
            addPanel(name, panel);
            return panel;
        } finally {
            state.restoreFrom(snapshot);
            state.setBossRetryAvailable(retry);
            PerformanceLog.finished("Prima creazione " + name, started);
        }
    }

    /**
     * Registra una funzione di costruzione senza creare subito la scena e le sue risorse.
     */
    public void registerScene(String name, java.util.function.Supplier<JPanel> factory) {
        factories.put(name, factory);
    }
    public String getCurrentSceneName() { return currentSceneName; }

    /**
     * Inserisce il pannello nel contenitore CardLayout e lo associa al nome della scena.
     */
    public void addPanel(String name, JPanel panel) {
        scenes.put(name, panel);
        mainPanel.add(panel, name);
    }

    public void showScene(String name) {
        showScene(name, null);
    }

    /** Avvia la transizione e richiama onShown quando la scena è visibile.
     * Senza dissolvenze, la funzione viene accodata subito sul thread Swing. */
    public void showScene(String name, Runnable onShown) {
        if ((!scenes.containsKey(name) && !factories.containsKey(name)) || isFading) return;
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

    /**
     * Riduce la copertura della nuova scena e richiama la funzione prevista quando diventa
     * visibile.
     */
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

    /**
     * Rende visibile il pannello, aggiorna il nome della scena e richiede ambiente sonoro e
     * attenzione della tastiera.
     */
    private void activateScene(String name) {
        if (getPanel(name) == null) return;
        cardLayout.show(mainPanel, name);
        currentSceneName = name;
        AudioManager.getInstance().playScene(name);
        scenes.get(name).requestFocusInWindow();
    }

    /**
     * Recupera o crea lo strato usato per disegnare le dissolvenze sopra la finestra.
     */
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

    /** Strato grafico della dissolvenza, dipendente dall’opacità gestita dal coordinatore delle scene. */
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
