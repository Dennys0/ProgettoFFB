package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

public class MainFrame extends JFrame {

    public MainFrame() {

        setTitle("The Alchemist's Hangover");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);

        SceneManager sceneManager = SceneManager.getInstance();

        // Aggiunta delle scene
        sceneManager.addPanel("MENU", new MainMenuPanel());
        sceneManager.addPanel("INTRO", new TavernIntroPanel());
        sceneManager.addPanel("CHAPTER1", new Chapter1Panel(this));

        // Imposta il pannello principale gestito dal SceneManager
        add(sceneManager.getMainPanel());

        // Mostra il menu iniziale
        sceneManager.showScene("MENU");
        sceneManager.addPanel("GAMEOVER", new GameOverPanel());

        // Dentro il costruttore di MainFrame.java:
        sceneManager.addPanel("MENU", new MainMenuPanel());
        sceneManager.addPanel("INTRO", new TavernIntroPanel());
        sceneManager.addPanel("CHAPTER1", new Chapter1Panel(this));
        sceneManager.addPanel("TRANSITION_CH1_CH2", new ChapterTransitionPanel()); // <-- NUOVA RIGA
        sceneManager.addPanel("GAMEOVER", new GameOverPanel());

// (Aggiungeremo CHAPTER2 appena creiamo la classe Chapter2Panel)
        sceneManager.addPanel("CHAPTER2", new Chapter2Panel(this));

        
        
        
        ////////////////PULSANTE PER SALTARE AL CAP 2
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0), "skipToCh2");

        getRootPane().getActionMap().put("skipToCh2", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                GameState gs = GameState.getInstance();
                gs.resetState();
                gs.setChapter(2);
                gs.setCurrentRoom(1);

                // Carica direttamente la scena del Capitolo 2
                SceneManager.getInstance().showScene("CHAPTER2");
                System.out.println("[DEBUG] Saltato direttamente al Capitolo 2!");
            }
        });
    }
}
