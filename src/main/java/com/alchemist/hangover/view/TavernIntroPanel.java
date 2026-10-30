package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.DialogueSystem;
import com.alchemist.hangover.controller.SceneManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class TavernIntroPanel extends JPanel {

    private Image backgroundImage;
    private Image dialogueBoxImage;
    private JLabel characterSpriteLabel;
    private JLabel nameLabel;
    private JLabel dialogueTextLabel;
    private JLabel skipPromptLabel;
    private DialogueSystem dialogueSystem;

    private Map<String, ImageIcon> characterSprites;

    public TavernIntroPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 720));

        // 1. Sfondo Taverna
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/images/intro/tavern_bg.png")).getImage();
        } catch (Exception e) {
            System.err.println("Errore caricamento sfondo taverna: " + e.getMessage());
        }

        // 2. Texture Dialogue Box Custom
        try {
            dialogueBoxImage = new ImageIcon(getClass().getResource("/images/ui/dialogue_box.png")).getImage();
        } catch (Exception e) {
            System.err.println("Errore caricamento immagine dialogue_box: " + e.getMessage());
        }

        // 3. Sprite Personaggi
        characterSprites = new HashMap<>();
        loadSprite("ARTHUR", "/images/characters/arthur_silhouette.png");
        loadSprite("MARTINO", "/images/characters/martino.png");
        loadSprite("HELA", "/images/characters/hela.png");
        loadSprite("SIEGFRIED", "/images/characters/siegfried.png");
        loadSprite("GUNNAR", "/images/characters/gunnar.png");

        // JLayeredPane con layout NULLO per rispettare le coordinate assolute
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        layeredPane.setBounds(0, 0, 1280, 720);

        // LIVELLO 1: Sprite Personaggio
        characterSpriteLabel = new JLabel();
        characterSpriteLabel.setBounds(390, 40, 500, 600);
        characterSpriteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        layeredPane.add(characterSpriteLabel, Integer.valueOf(1));

        // 2. Caricamento Texture Dialogue Box con ImageIO
        try {
            dialogueBoxImage = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/images/ui/dialogue_box.png"));
        } catch (Exception e) {
            System.err.println("ERRORE: Impossibile caricare dialogue_box.png: " + e.getMessage());
        }

// LIVELLO 2: Box dei Dialoghi
        JPanel dialogueBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                // Migliora la qualitÃ  dello scaling visivo
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                if (dialogueBoxImage != null) {
                    // Forza il disegno dell'immagine su tutta la larghezza e altezza del pannello (1200x200)
                    g2d.drawImage(dialogueBoxImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2d.setColor(new Color(20, 20, 20, 230));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        dialogueBox.setLayout(null);
        dialogueBox.setOpaque(false);
// Imposta posizione e dimensione fissa lungo tutta la fascia inferiore (1200x200)
        dialogueBox.setBounds(40, 470, 1200, 200);

// Nome Personaggio (Posizionato dentro la cornice)
        nameLabel = new JLabel("");
        nameLabel.setFont(new Font("Georgia", Font.BOLD, 24));
        nameLabel.setForeground(new Color(255, 215, 0));
        nameLabel.setBounds(65, 25, 500, 30);
        dialogueBox.add(nameLabel);

// Testo del Dialogo
        dialogueTextLabel = new JLabel("");
        dialogueTextLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        dialogueTextLabel.setForeground(Color.WHITE);
        dialogueTextLabel.setBounds(65, 60, 1070, 95);
        dialogueTextLabel.setVerticalAlignment(SwingConstants.TOP);
        dialogueBox.add(dialogueTextLabel);

// Prompt di avanzamento (disabilitato per non mostrare scritte sovrapposte)
        skipPromptLabel = new JLabel("");
        dialogueBox.add(skipPromptLabel);

        layeredPane.add(dialogueBox, Integer.valueOf(2));

        add(layeredPane, BorderLayout.CENTER);

        // Inizializzazione DialogueSystem
        dialogueSystem = new DialogueSystem(
                nameLabel,
                dialogueTextLabel,
                this::updateCharacterSprite,
                () -> {
                    System.out.println("Introduzione completata! Passaggio al Capitolo 1...");
                    SceneManager.getInstance().showScene("CHAPTER1");
                }
        );

        dialogueSystem.loadDialogue("/dialogues/intro_dialogue.txt");
        dialogueSystem.startDialogue();

        // Mouse Listener per advance
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dialogueSystem.nextLine();
            }
        });

        // KeyBindings per SPAZIO e INVIO
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "advanceDialogue");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "advanceDialogue");

        actionMap.put("advanceDialogue", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialogueSystem.nextLine();
            }
        });

        JButton skipButton = new JButton("SKIP");
        skipButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        skipButton.setForeground(new Color(255, 215, 0));
        // Sfondo pienamente opaco: evita artefatti del testo della dialogue box
        // durante il repaint e il passaggio del mouse sul pulsante.
        skipButton.setBackground(new Color(30, 30, 30));
        skipButton.setOpaque(true);
        skipButton.setContentAreaFilled(true);
        skipButton.setFocusPainted(false);
        skipButton.setFocusable(false); // Impedisce al focus di evidenziare o mostrare testo di background
        skipButton.setBorder(BorderFactory.createLineBorder(new Color(200, 160, 80), 1));
        skipButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        skipButton.setBounds(1140, 20, 100, 35);

        skipButton.addActionListener(e -> {
            if (dialogueSystem != null) {
                dialogueSystem.stop(); // Interrompe il timer del testo
            }
            SceneManager.getInstance().showScene("CHAPTER1");
        });

// Livello dedicato ai controlli, sopra sprite e dialogue box.
        layeredPane.add(skipButton, Integer.valueOf(3));

    }

    private void loadSprite(String name, String path) {
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(path));
            Image img = icon.getImage().getScaledInstance(-1, 550, Image.SCALE_SMOOTH);
            characterSprites.put(name, new ImageIcon(img));
        } catch (Exception e) {
            System.err.println("Sprite non trovato per " + name + " in " + path);
        }
    }

    private void updateCharacterSprite() {
        DialogueSystem.DialogueLine current = dialogueSystem.getCurrentLine();
        if (current != null) {
            String speaker = current.getSpeaker();
            if (characterSprites.containsKey(speaker)) {
                characterSpriteLabel.setIcon(characterSprites.get(speaker));
                characterSpriteLabel.setVisible(true);
            } else {
                characterSpriteLabel.setVisible(false);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
