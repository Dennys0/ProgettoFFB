package com.alchemist.hangover.view.chapter;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.view.story.ProloguePanel;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

class NavigationRegressionTest {
    @Test
    void serpentCannotCoverTheExit() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            GameState.getInstance().resetState();
            Chapter2Panel panel = new Chapter2Panel(null);
            panel.setSize(1280, 720);
            panel.loadRoom(5);
            panel.doLayout();
            assertTrue(panel.btnUp.isVisible());
            for (java.awt.Component component : panel.getComponents()) {
                if (component != panel.btnUp && component.isVisible()
                        && component.getBounds().intersects(panel.btnUp.getBounds())
                        && component instanceof HotspotButton)
                    assertTrue(panel.getComponentZOrder(panel.btnUp) < panel.getComponentZOrder(component));
            }
            panel.btnUp.doClick();
            assertNotEquals(5, GameState.getInstance().getCurrentRoom());
        });
    }

    @Test
    void dormantPrologueDoesNotTypeAndNewGameRefreshesTheShip() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                GameState state = GameState.getInstance();
                state.resetState();
                Chapter1Panel chapter = new Chapter1Panel(null);
                chapter.loadRoom(4);
                SceneManager.getInstance().addPanel("CHAPTER1", chapter);
                ProloguePanel intro = new ProloguePanel();
                Field field = ProloguePanel.class.getDeclaredField("dialogue");
                field.setAccessible(true);
                var dialogue = (com.alchemist.hangover.controller.DialogueSystem) field.get(intro);
                assertFalse(dialogue.isTyping());
                state.resetState();
                Method finish = ProloguePanel.class.getDeclaredMethod("finishPrologue");
                finish.setAccessible(true);
                finish.invoke(intro);
                assertEquals(1, chapter.currentRoom);
                assertEquals(1, state.getCurrentRoom());
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        });
    }
}
