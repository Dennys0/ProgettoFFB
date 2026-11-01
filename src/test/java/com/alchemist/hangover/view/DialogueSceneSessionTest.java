package com.alchemist.hangover.view;

import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;
import static org.junit.jupiter.api.Assertions.*;

class DialogueSceneSessionTest {
    @Test void sharedSessionStartsAndStopsWithoutChangingSceneOrGameProgress() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel parent = new JPanel(null);
            final int[] completions = {0};
            DialogueManager.SceneSession session = new DialogueManager.SceneSession(
                    parent, speaker -> new Rectangle(340, 30, 600, 620),
                    () -> completions[0]++);
            session.addPortrait("MOZZO", "/images/characters/mozzo_realms.png");
            assertFalse(session.isVisible());
            assertEquals(2, parent.getComponentCount());
            assertInstanceOf(SharedDialogueBox.class, parent.getComponent(0));
            session.start("/dialogues/mozzo_dialogue.txt");
            assertTrue(session.isVisible());
            session.stop();
            assertFalse(session.isVisible());
            assertEquals(0, completions[0], "Changing rooms must not complete the dialogue.");
        });
    }
}
