package com.alchemist.hangover.view.story;

import com.alchemist.hangover.controller.DialogueSystem;
import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.view.dialogue.DialogueBox;
import com.alchemist.hangover.view.dialogue.DialogueManager;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/** Prologo: sfondo della taverna, ritratti e una sola conversazione skippabile. */
public final class ProloguePanel extends JPanel {
    private static final String INTRO_DIALOGUE = "/dialogues/intro_dialogue.txt";
    private static final String TAVERN_BACKGROUND = "/images/intro/tavern_bg.png";
    private static final Map<String, String> PORTRAIT_PATHS = Map.of(
            "ARTHUR", "/images/shared/npc/arthur_silhouette.png",
            "MARTINO", "/images/intro/npc/martino.png",
            "HELA", "/images/intro/npc/hela.png",
            "SIEGFRIED", "/images/intro/npc/siegfried.png",
            "GUNNAR", "/images/intro/npc/gunnar.png"
    );

    private final Image backgroundImage;
    private final JLabel portrait = new JLabel("", SwingConstants.CENTER);
    private final Map<String, ImageIcon> portraits = new HashMap<>();
    private final DialogueSystem dialogue;

    public ProloguePanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 720));
        URL tavern = getClass().getResource(TAVERN_BACKGROUND);
        backgroundImage = tavern == null ? null : new ImageIcon(tavern).getImage();

        PORTRAIT_PATHS.forEach((speaker, path) -> {
            ImageIcon icon = DialogueBox.scaledPortrait(path, 550);
            if (icon != null) portraits.put(speaker, icon);
            else System.err.println("[ProloguePanel] Ritratto mancante: " + path);
        });

        JLayeredPane layers = new JLayeredPane();
        layers.setLayout(null);
        layers.setBounds(0, 0, 1280, 720);
        add(layers, BorderLayout.CENTER);

        portrait.setBounds(390, 40, 500, 600);
        layers.add(portrait, Integer.valueOf(1));

        DialogueBox frame = new DialogueBox();
        frame.setBounds(40, 470, 1200, 200);
        frame.getRevealLabel().setText(""); // Il comando di rivelazione e' in alto.
        layers.add(frame, Integer.valueOf(2));

        dialogue = new DialogueSystem(frame.getSpeakerLabel(), frame.getTextLabel(),
                this::updatePortrait, this::finishPrologue);
        DialogueManager.connect(frame, portrait, dialogue, () -> true);
        DialogueManager.bindAdvance(this, "advanceDialogue", () -> true, dialogue);

        layers.add(controlButton("MOSTRA FRASE", 985, 145, 12, false,
                () -> dialogue.revealCurrentLine()), Integer.valueOf(3));
        layers.add(controlButton("SKIP", 1140, 100, 14, true,
                this::finishPrologue), Integer.valueOf(3));

        restartDialogue();
    }

    /** I pannelli sono riutilizzati: Nuova partita ricomincia il testo da capo. */
    public void restartDialogue() {
        dialogue.stop();
        dialogue.loadDialogue(INTRO_DIALOGUE);
        dialogue.startDialogue();
    }

    private void finishPrologue() {
        dialogue.stop();
        SceneManager.getInstance().showScene("CHAPTER1");
    }

    private void updatePortrait() {
        DialogueSystem.DialogueLine line = dialogue.getCurrentLine();
        ImageIcon icon = line == null ? null : portraits.get(line.getSpeaker());
        portrait.setIcon(icon);
        portrait.setVisible(icon != null);
    }

    private static JButton controlButton(String text, int x, int width,
                                         int fontSize, boolean outlined, Runnable action) {
        JButton button = new JButton(text);
        button.setBounds(x, 20, width, 35);
        button.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        button.setForeground(new Color(255, outlined ? 215 : 225, outlined ? 0 : 160));
        button.setBackground(new Color(30, 30, 30));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusable(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (outlined) {
            button.setBorder(BorderFactory.createLineBorder(new Color(200, 160, 80)));
        }
        button.addActionListener(e -> action.run());
        return button;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (backgroundImage != null) {
            graphics.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
