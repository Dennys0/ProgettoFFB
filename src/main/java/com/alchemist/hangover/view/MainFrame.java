package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import javax.swing.JFrame;

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
    }
}