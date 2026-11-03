package com.alchemist.hangover.view.dialogue;

import com.alchemist.hangover.controller.DialogueSystem;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.swing.*;

/**
 * Collega clic, tastiera e ritratti al motore dei dialoghi di prologo e capitoli.
 * DialogueSystem gestisce testo e avanzamento; il capitolo decide gli esiti narrativi.
 */
public final class DialogueManager {
    private DialogueManager() { }

    /**
     * Collega clic sulla cornice e sul ritratto all’avanzamento, rispettando la condizione di
     * dialogo attivo.
     */
    public static void connect(DialogueBox box, JLabel portrait,
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

    /**
     * Associa spazio e invio all’avanzamento solo quando la conversazione è attiva.
     */
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
     * Riunisce ritratto, cornice e comandi di una conversazione nella scena.
     * La funzione di completamento resta definita dal capitolo che crea la sessione.
     */
    public static final class SceneSession {
        private final JPanel chapter;
        private final JLabel portrait = new JLabel("", SwingConstants.CENTER);
        private final DialogueBox box = new DialogueBox();
        private final DialogueSystem dialogue;
        private final Map<String, ImageIcon> portraits = new HashMap<>();
        private final Map<String, String> portraitPaths = new HashMap<>();
        private final Set<String> fittedPortraits = new HashSet<>();
        private final Function<String, Rectangle> portraitBounds;
        private String fixedPortraitSpeaker;

        public SceneSession(JPanel chapter, Function<String, Rectangle> portraitBounds,
                            Runnable onComplete) {
            this(chapter, portraitBounds, onComplete, true);
        }

        /** bindKeys=false quando un capitolo ha gia' una gestione condivisa dei tasti. */
        public SceneSession(JPanel chapter, Function<String, Rectangle> portraitBounds,
                            Runnable onComplete, boolean bindKeys) {
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
            if (bindKeys) bindAdvance(chapter, "advanceDialogue", this::isVisible, dialogue);
        }

        /**
         * Registra il percorso del ritratto senza decodificare subito l’immagine.
         */
        public void addPortrait(String speaker, String path) {
            portraitPaths.put(speaker, path);
        }

        /** Registra la risorsa; viene caricata solo alla prima battuta del personaggio. */
        public void addFittedPortrait(String speaker, String path) {
            portraitPaths.put(speaker, path);
            fittedPortraits.add(speaker);
        }

        /** Mantiene lo stesso ritratto anche quando nel TXT parla Arthur. */
        public void setFixedPortrait(String speaker) {
            fixedPortraitSpeaker = speaker;
        }

        public DialogueBox dialogueBox() {
            return box;
        }

        public void setWrapWidth(int width) {
            dialogue.setWrapWidth(width);
        }

        public void advance() {
            if (isVisible()) dialogue.nextLine();
        }

        public boolean isVisible() {
            return box.isVisible();
        }

        /**
         * Porta cornice e ritratto in primo piano e avvia la risorsa narrativa, evitando avvii
         * sovrapposti.
         */
        public void start(String resource) {
            if (isVisible()) return;
            box.setVisible(true);
            chapter.setComponentZOrder(box, 0);
            chapter.setComponentZOrder(portrait, 1);
            dialogue.loadDialogue(resource);
            dialogue.startDialogue();
            if (fixedPortraitSpeaker != null) updatePortrait();
        }

        /** Interrompe la conversazione senza eseguirne le conseguenze narrative. */
        public void stop() {
            dialogue.stop();
            box.setVisible(false);
            portrait.setIcon(null);
            portrait.setVisible(false);
        }

        /**
         * Sceglie il ritratto, lo carica al primo utilizzo e applica posizione e proporzioni.
         */
        private void updatePortrait() {
            DialogueSystem.DialogueLine line = dialogue.getCurrentLine();
            String speaker = fixedPortraitSpeaker != null ? fixedPortraitSpeaker
                    : line == null ? null : line.getSpeaker();
            ImageIcon icon = portraits.get(speaker);
            if (icon == null && portraitPaths.containsKey(speaker)) {
                String path = portraitPaths.get(speaker);
                if (fittedPortraits.contains(speaker)) {
                    java.net.URL resource = chapter.getClass().getResource(path);
                    icon = resource == null ? null : new ImageIcon(resource);
                } else icon = DialogueBox.scaledPortrait(path, 520);
                if (icon != null) portraits.put(speaker, icon);
            }
            portrait.setVisible(icon != null);
            if (icon == null) {
                portrait.setIcon(null);
                return;
            }
            Rectangle bounds = portraitBounds.apply(speaker);
            portrait.setBounds(bounds);
            if (fittedPortraits.contains(speaker)) {
                double scale = Math.min((double) bounds.width / Math.max(1, icon.getIconWidth()),
                        (double) bounds.height / Math.max(1, icon.getIconHeight()));
                int width = Math.max(1, (int) Math.round(icon.getIconWidth() * scale));
                int height = Math.max(1, (int) Math.round(icon.getIconHeight() * scale));
                portrait.setIcon(com.alchemist.hangover.view.ImageAssets.scaled(
                        portraitPaths.get(speaker), width, height));
            } else {
                portrait.setIcon(icon);
            }
        }
    }

}
