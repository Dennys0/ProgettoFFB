package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.DialogueSystem;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BooleanSupplier;

/**
 * Shared interaction wiring for dialogue panels in every chapter and prologue.
 * The underlying DialogueSystem still owns typing, reading and completion;
 * narrative callbacks remain in the chapter (Gunnar, boss, Martino, NPC).
 */
public final class DialogueManager {
    private DialogueManager() { }

    public static void connect(SharedDialogueBox box, JLabel portrait,
                               DialogueSystem dialogue, BooleanSupplier active) {
        box.getRevealLabel().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (active.getAsBoolean()) dialogue.revealCurrentLine();
            }
        });
        MouseAdapter advance = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (active.getAsBoolean()) dialogue.nextLine();
            }
        };
        box.addMouseListener(advance);
        box.getSpeakerLabel().addMouseListener(advance);
        box.getTextLabel().addMouseListener(advance);
        if (portrait != null) portrait.addMouseListener(advance);
    }

    public static void bindAdvance(JComponent owner, String actionName,
                                   BooleanSupplier active, DialogueSystem dialogue) {
        InputMap inputs = owner.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = owner.getActionMap();
        inputs.put(KeyStroke.getKeyStroke("SPACE"), actionName);
        inputs.put(KeyStroke.getKeyStroke("ENTER"), actionName);
        actions.put(actionName, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                if (active.getAsBoolean()) dialogue.nextLine();
            }
        });
    }
}
