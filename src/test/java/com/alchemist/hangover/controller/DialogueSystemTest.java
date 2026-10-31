package com.alchemist.hangover.controller;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DialogueSystemTest {
    @Test
    void wrappedGunnarDialogueFitsInItsTextPanel() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JLabel name = new JLabel();
            JLabel content = new JLabel();
            DialogueSystem dialogue = new DialogueSystem(name, content, null, null);
            dialogue.setWrapWidth(510);
            dialogue.loadDialogue("/dialogues/ch3_gunnar.txt");
            dialogue.startDialogue();
            assertNotNull(dialogue.getCurrentLine());
            dialogue.revealCurrentLine();
            assertTrue(content.getText().contains("width='510'"));
            assertTrue(content.getText().contains("Gunnar") || !content.getText().isBlank());
            dialogue.stop();
        });
    }

    @Test
    void skipCompletesOnceWithoutReplayingTheCallback() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            JLabel name = new JLabel();
            JLabel content = new JLabel();
            DialogueSystem dialogue = new DialogueSystem(name, content, () -> {}, calls::incrementAndGet);
            dialogue.loadDialogue("/dialogues/intro_dialogue.txt");
            dialogue.startDialogue();
            assertNotNull(dialogue.getCurrentLine());
            dialogue.revealCurrentLine();
            assertFalse(dialogue.isTyping());
            assertTrue(content.getText().startsWith("<html>"));
            dialogue.skipDialogue();
            dialogue.skipDialogue();
            dialogue.nextLine();
            assertEquals(1, calls.get());
        });
    }
}
