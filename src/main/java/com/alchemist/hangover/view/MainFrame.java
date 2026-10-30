package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("The Alchemist's Hangover");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);

        SceneManager manager = SceneManager.getInstance();
        manager.addPanel("MENU", new MainMenuPanel());
        manager.addPanel("INTRO", new TavernIntroPanel());
        manager.addPanel("CHAPTER1", new Chapter1Panel(this));
        manager.addPanel("TRANSITION_CH1_CH2", new ChapterTransitionPanel());
        manager.addPanel("CHAPTER2", new Chapter2Panel(this));
        manager.addPanel("TRANSITION_CH2_CH3", new Chapter3PreviewPanel());
        manager.addPanel("GAMEOVER", new GameOverPanel());

        add(manager.getMainPanel());
        manager.showScene("MENU");

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "pauseGame");
        getRootPane().getActionMap().put("pauseGame", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (isPlayingChapter()) new PauseMenuDialog(MainFrame.this).setVisible(true);
            }
        });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_J, 0), "openJournal");
        getRootPane().getActionMap().put("openJournal", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (isPlayingChapter()) new GameJournalDialog(MainFrame.this).setVisible(true);
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "skipToCh2");
        getRootPane().getActionMap().put("skipToCh2", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                GameState state = GameState.getInstance();
                state.resetState();
                state.setChapter(2);
                state.setCurrentRoom(1);
                Chapter2Panel chapter2 = (Chapter2Panel) manager.getPanel("CHAPTER2");
                if (chapter2 != null) chapter2.refreshFromGameState();
                manager.showScene("CHAPTER2");
                System.out.println("[DEBUG] Saltato direttamente al Capitolo 2!");
            }
        });
    }

    private boolean isPlayingChapter() {
        String scene = SceneManager.getInstance().getCurrentSceneName();
        return "CHAPTER1".equals(scene) || "CHAPTER2".equals(scene);
    }
}
