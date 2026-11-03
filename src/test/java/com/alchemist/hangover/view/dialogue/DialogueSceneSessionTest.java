package com.alchemist.hangover.view.dialogue;

import java.awt.*;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DialogueSceneSessionTest {
    @Test void sharedSessionStartsAndStopsWithoutChangingSceneOrGameProgress() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel parent = new JPanel(null);
            final int[] completions = {0};
            DialogueManager.SceneSession session = new DialogueManager.SceneSession(
                    parent, speaker -> new Rectangle(340, 30, 600, 620),
                    () -> completions[0]++);
            session.addPortrait("MOZZO", "/images/chapter1/npc/mozzo_realms.png");
            assertFalse(session.isVisible());
            assertEquals(2, parent.getComponentCount());
            assertInstanceOf(DialogueBox.class, parent.getComponent(0));
            session.start("/dialogues/mozzo_dialogue.txt");
            assertTrue(session.isVisible());
            session.stop();
            assertFalse(session.isVisible());
            assertEquals(0, completions[0], "Changing rooms must not complete the dialogue.");
        });
    }
}
