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
        manager.addPanel("CHAPTER3", new Chapter3Panel(this));
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

        // Accesso provvisorio al capitolo 3 per verificare le sette stanze,
        // senza passare dal boss o dalla ricetta del capitolo 2.
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "previewChapter3");
        getRootPane().getActionMap().put("previewChapter3", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                GameState state = GameState.getInstance();
                state.resetState();
                state.setChapter(3);
                state.setCurrentRoom(1);
                Chapter3Panel chapter3 = (Chapter3Panel) manager.getPanel("CHAPTER3");
                if (chapter3 != null) chapter3.refreshFromGameState();
                manager.showScene("CHAPTER3");
            }
        });

        // F4: scelta rapida della stanza di Ymir, senza azzerare inventario.
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "chapter3RoomPicker");
        getRootPane().getActionMap().put("chapter3RoomPicker", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!"CHAPTER3".equals(manager.getCurrentSceneName())) return;
                Chapter3Panel chapter3 = (Chapter3Panel) manager.getPanel("CHAPTER3");
                String[] rooms = java.util.stream.IntStream.rangeClosed(1, 7)
                        .mapToObj(n -> n + " - " + Chapter3World.roomName(n))
                        .toArray(String[]::new);
                Object selection = javax.swing.JOptionPane.showInputDialog(
                        MainFrame.this, "Scegli una stanza da provare:",
                        "Debug Capitolo III", javax.swing.JOptionPane.QUESTION_MESSAGE,
                        null, rooms, rooms[GameState.getInstance().getCurrentRoom() - 1]);
                if (selection != null && chapter3 != null) {
                    int room = Integer.parseInt(selection.toString().split(" - ", 2)[0]);
                    chapter3.loadRoom(room);
                }
            }
        });
    }

    private boolean isPlayingChapter() {
        String scene = SceneManager.getInstance().getCurrentSceneName();
        return "CHAPTER1".equals(scene) || "CHAPTER2".equals(scene)
                || "CHAPTER3".equals(scene);
    }
}
