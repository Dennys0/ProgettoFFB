package com.alchemist.hangover.view;

import com.alchemist.hangover.view.chapter.AbstractChapterPanel;
import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.view.chapter.Chapter1Panel;
import com.alchemist.hangover.view.chapter.Chapter2Panel;
import com.alchemist.hangover.view.chapter.Chapter3Panel;
import com.alchemist.hangover.view.chapter.Chapter4Panel;
import com.alchemist.hangover.view.chapter.ChapterLayouts;
import com.alchemist.hangover.view.chapter.HotspotButton;
import com.alchemist.hangover.view.inventory.GameJournalDialog;
import com.alchemist.hangover.view.menu.MainMenuPanel;
import com.alchemist.hangover.view.menu.PauseMenuDialog;
import com.alchemist.hangover.view.story.ChapterCompletionPanel;
import com.alchemist.hangover.view.story.EpiloguePanel;
import com.alchemist.hangover.view.story.GameOverPanel;
import com.alchemist.hangover.view.story.ProloguePanel;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("The Alchemist's Hangover");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);

        SceneManager manager = SceneManager.getInstance();
        manager.addPanel("MENU", new MainMenuPanel());
        manager.addPanel("INTRO", new ProloguePanel());
        manager.addPanel("CHAPTER1", new Chapter1Panel(this));
        manager.addPanel("TRANSITION_CH1_CH2", new ChapterCompletionPanel(this, 1));
        manager.addPanel("CHAPTER2", new Chapter2Panel(this));
        manager.addPanel("CHAPTER3", new Chapter3Panel(this));
        manager.addPanel("CHAPTER4", new Chapter4Panel(this));
        manager.addPanel("EPILOGUE", new EpiloguePanel(this));
        manager.addPanel("CHAPTER3_COMPLETE", new ChapterCompletionPanel(this, 3));
        manager.addPanel("TRANSITION_CH2_CH3", new ChapterCompletionPanel(this, 2));
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

        // F5: anteprima diretta delle sette stanze di Muspelheim.
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "previewChapter4");
        getRootPane().getActionMap().put("previewChapter4", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                GameState state = GameState.getInstance();
                state.resetState();
                state.setChapter(4);
                state.setCurrentRoom(1);
                Chapter4Panel chapter = (Chapter4Panel) manager.getPanel("CHAPTER4");
                if (chapter != null) chapter.refreshFromGameState();
                manager.showScene("CHAPTER4");
            }
        });

        // F6: contorni e coordinate delle hitbox per riallinearle ai PNG (solo debug).
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "showHitboxes");
        getRootPane().getActionMap().put("showHitboxes", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!isPlayingChapter()) return;
                HotspotButton.setDebugBoundsVisible(!HotspotButton.debugBoundsVisible());
                manager.getMainPanel().repaint();
                System.out.println("[DEBUG] Hitbox: "
                        + (HotspotButton.debugBoundsVisible() ? "VISIBILI" : "NASCOSTE"));
            }
        });

        bindRoomPicker(KeyEvent.VK_F4, 3);
        bindRoomPicker(KeyEvent.VK_F7, 4);
    }

    private void bindRoomPicker(int key, int chapter) {
        String action = "chapter" + chapter + "RoomPicker";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(key, 0), action);
        getRootPane().getActionMap().put(action, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                SceneManager manager = SceneManager.getInstance();
                String scene = "CHAPTER" + chapter;
                if (!scene.equals(manager.getCurrentSceneName())) return;
                String[] rooms = java.util.stream.IntStream.rangeClosed(1, 7)
                        .mapToObj(n -> n + " - " + ChapterLayouts.roomName(chapter, n))
                        .toArray(String[]::new);
                Object choice = JOptionPane.showInputDialog(MainFrame.this,
                        "Scegli la stanza da controllare (F6 mostra le hitbox)",
                        "Debug capitolo " + chapter, JOptionPane.QUESTION_MESSAGE,
                        null, rooms, rooms[GameState.getInstance().getCurrentRoom() - 1]);
                if (choice != null && manager.getPanel(scene) instanceof AbstractChapterPanel panel)
                    panel.loadRoom(java.util.Arrays.asList(rooms).indexOf(choice) + 1);
            }
        });
    }

    private boolean isPlayingChapter() {
        String scene = SceneManager.getInstance().getCurrentSceneName();
        return "CHAPTER1".equals(scene) || "CHAPTER2".equals(scene)
                || "CHAPTER3".equals(scene) || "CHAPTER4".equals(scene);
    }
}
