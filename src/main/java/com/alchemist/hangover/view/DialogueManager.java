package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.DialogueSystem;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
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
    /**
     * C1/C2 share the same portrait, dialogue frame and input lifecycle.
     * The chapter keeps ownership of the narrative onComplete callback.
     */
    public static final class SceneSession {
        private final JPanel chapter;
        private final JLabel portrait = new JLabel("", SwingConstants.CENTER);
        private final SharedDialogueBox box = new SharedDialogueBox();
        private final DialogueSystem dialogue;
        private final Map<String, ImageIcon> portraits = new HashMap<>();
        private final Function<String, Rectangle> portraitBounds;

        public SceneSession(JPanel chapter, Function<String, Rectangle> portraitBounds,
                            Runnable onComplete) {
            this.chapter = chapter;
            this.portraitBounds = portraitBounds;
            portrait.setBounds(portraitBounds.apply(null));
            portrait.setVerticalAlignment(SwingConstants.CENTER);
            portrait.setVisible(false);
            chapter.add(portrait);

            box.setBounds(40, 470, 1200, 200);
            box.setVisible(false);
            chapter.add(box);
            chapter.setComponentZOrder(box, 0);
            chapter.setComponentZOrder(portrait, 1);

            dialogue = new DialogueSystem(box.getSpeakerLabel(), box.getTextLabel(),
                    this::updatePortrait, onComplete);
            dialogue.setWrapWidth(1000);
            connect(box, portrait, dialogue, this::isVisible);
            chapter.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent event) {
                    if (isVisible()) dialogue.nextLine();
                }
            });
            bindAdvance(chapter, "advanceDialogue", this::isVisible, dialogue);
        }

        public void addPortrait(String speaker, String path) {
            ImageIcon icon = SharedDialogueBox.scaledPortrait(path, 520);
            if (icon != null) portraits.put(speaker, icon);
            else System.err.println("Sprite non trovato per " + speaker + " in " + path);
        }

        public boolean isVisible() {
            return box.isVisible();
        }

        public void start(String resource) {
            if (isVisible()) return;
            box.setVisible(true);
            chapter.setComponentZOrder(box, 0);
            chapter.setComponentZOrder(portrait, 1);
            dialogue.loadDialogue(resource);
            dialogue.startDialogue();
        }

        /** Closing by navigation must never call a narrative completion callback. */
        public void stop() {
            dialogue.stop();
            box.setVisible(false);
            portrait.setIcon(null);
            portrait.setVisible(false);
        }

        private void updatePortrait() {
            DialogueSystem.DialogueLine line = dialogue.getCurrentLine();
            ImageIcon icon = line == null ? null : portraits.get(line.getSpeaker());
            portrait.setVisible(icon != null);
            portrait.setIcon(icon);
            if (icon != null) portrait.setBounds(portraitBounds.apply(line.getSpeaker()));
        }
    }

}
