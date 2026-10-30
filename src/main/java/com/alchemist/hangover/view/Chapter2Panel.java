package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.DialogueSystem;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class Chapter2Panel extends AbstractChapterPanel {

    // Elementi Mondo di Gioco
    private JButton siegfriedHotspot;
    private JButton cauldronHotspot;
    private JButton ranBossHotspot;
    private JButton journalHotspot;

    // UI Dialoghi
    private JPanel dialogueBoxOverlay;
    private JLabel dialogueNameLabel;
    private JLabel dialogueTextLabel;
    private JLabel dialogueSkipLabel;
    private JLabel characterSpriteLabel;
    private Image dialogueBoxImage;
    private DialogueSystem dialogueSystem;
    private Map<String, ImageIcon> characterSprites;

    public Chapter2Panel(JFrame mainFrame) {
        initializeChapterBase(
                mainFrame,
                Map.of(
                        1, "/images/chapter2/room1_hall.png",
                        2, "/images/chapter2/room2_bar.png",
                        3, "/images/chapter2/room3_sacristy.png",
                        4, "/images/chapter2/room4_gardens.png",
                        5, "/images/chapter2/room5_pit.png",
                        6, "/images/chapter2/room6_balcony.png",
                        7, "/images/chapter2/room7_boss.png"
                ),
                Map.of(
                        1, Map.of("UP", 2, "LEFT", 3, "RIGHT", 4, "DOWN", 5),
                        2, Map.of("DOWN", 1, "UP", 6),
                        3, Map.of("RIGHT", 1),
                        4, Map.of("LEFT", 1),
                        5, Map.of("UP", 1, "DOWN", 7),
                        6, Map.of("DOWN", 2),
                        7, Map.of("UP", 5)
                )
        );

        try {
            dialogueBoxImage = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/images/ui/dialogue_box.png"));
        } catch (Exception e) {
            System.err.println("Errore caricamento dialogue_box.png in Chapter2: " + e.getMessage());
        }

        characterSprites = new HashMap<>();
        loadSprite("SIEGFRIED", "/images/characters/siegfried_realms.png");
        loadSprite("RÁN", "/images/characters/ran_boss.png");

        initHotspots();
        initDialogueUI();
        loadRoom(1);
    }

    private void loadSprite(String name, String path) {
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(path));
            Image img = icon.getImage().getScaledInstance(-1, 520, Image.SCALE_SMOOTH);
            characterSprites.put(name, new ImageIcon(img));
        } catch (Exception e) {
            System.err.println("Sprite non trovato per " + name + " in " + path);
        }
    }

    private void initHotspots() {
        // NPC Siegfried al Bancone (Stanza 2)
        siegfriedHotspot = new JButton();
        siegfriedHotspot.setBounds(500, 200, 280, 400);
        siegfriedHotspot.setContentAreaFilled(false);
        siegfriedHotspot.setBorderPainted(false);
        siegfriedHotspot.setFocusPainted(false);
        siegfriedHotspot.setFocusable(false);
        siegfriedHotspot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        siegfriedHotspot.setToolTipText("Parla con Siegfried");
        siegfriedHotspot.addActionListener(e -> startSiegfriedDialogue());
        add(siegfriedHotspot);

        // Calderone nella Sacrestia Corallina (Stanza 3)
        cauldronHotspot = new JButton();
        cauldronHotspot.setBounds(500, 350, 280, 250);
        cauldronHotspot.setContentAreaFilled(false);
        cauldronHotspot.setBorderPainted(false);
        cauldronHotspot.setFocusPainted(false);
        cauldronHotspot.setFocusable(false);
        cauldronHotspot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cauldronHotspot.setToolTipText("Usa il calderone");
        cauldronHotspot.addActionListener(e -> interactWithCauldron());
        add(cauldronHotspot);

        // Boss Rán nel Santuario (Stanza 7 - Area ridotta per non coprire le frecce)
        ranBossHotspot = new JButton();
        ranBossHotspot.setBounds(250, 200, 780, 380);
        ranBossHotspot.setContentAreaFilled(false);
        ranBossHotspot.setBorderPainted(false);
        ranBossHotspot.setFocusPainted(false);
        ranBossHotspot.setFocusable(false);
        ranBossHotspot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ranBossHotspot.setToolTipText("Affronta Rán");
        ranBossHotspot.addActionListener(e -> triggerRanBossEncounter());
        add(ranBossHotspot);

        // Oggetto Diario del Capitano (Stanza 6 - Balconata dei Relitti)
        addItemWorldButton("DIARIO_CAPITANO", "/images/items/item_journl.png", 580, 420, 70, 70, 6,
                "Diario del Capitano",
                "CODICE DEL CAPITANO:\n1: Tra le correnti spezzo la rete\n2: Invoco la forza della marea\n3: Placo l'ira della dea\n4: Proteggo la rotta del veliero\n5: Rivelo il segreto dell'abisso\n6: Padroneggio il vortice finale");

        // Oggetti Raccoglibili (Stanza 4 e Stanza 5)
        addItemWorldButton("CORALLO_RAN", "/images/items/item_coral.png", 450, 380, 80, 80, 4,
                "Corallo di Rán", "Un ramo di corallo aspro rosso sangue incastrato nelle rocce.");

        addItemWorldButton("INCHIOSTRO_KRAKEN", "/images/items/item_ink.png", 800, 450, 70, 70, 4,
                "Inchiostro di Kraken", "Una boccetta di liquido buio e denso depositata nell'anfratto.");

        addItemWorldButton("SALE_JORMUNGANDR", "/images/items/item_salt.png", 400, 420, 80, 80, 5,
                "Sale di Jörmungandr", "Cristalli lucenti di squama salmastra staccati dalle pareti.");

        addItemWorldButton("ACQUA_LETE", "/images/items/item_lete.png", 850, 480, 70, 70, 5,
                "Acqua del Lete", "Una linfa trasparente e stranamente immobile.");
    }

    private void interactWithCauldron() {
        CauldronOverlay cauldron = new CauldronOverlay(mainFrameRef);
        cauldron.setVisible(true);
    }

    @Override
    protected void onRoomChanged() {
        siegfriedHotspot.setVisible(currentRoom == 2);
        cauldronHotspot.setVisible(currentRoom == 3);
        ranBossHotspot.setVisible(currentRoom == 7
                && !GameState.getInstance().isBossDefeated());
    }

    private void startSiegfriedDialogue() {
        setArrowsVisible(false);
        siegfriedHotspot.setVisible(false);
        dialogueBoxOverlay.setVisible(true);

        boolean hasJournal = GameState.getInstance().hasItem("DIARIO_CAPITANO");
        String dialoguePath = hasJournal ? "/dialogues/siegfried_dialogue_2.txt" : "/dialogues/siegfried_dialogue_1.txt";

        dialogueSystem.loadDialogue(dialoguePath);
        dialogueSystem.startDialogue();
    }

    /** Checkpoint del boss: salta il dialogo introduttivo gia' visto. */
    public void retryBossEncounter() {
        if (GameState.getInstance().isBossDefeated()) return;
        loadRoom(7);
        setArrowsVisible(false);
        ranBossHotspot.setVisible(false);
        new RanStrategyBossOverlay(mainFrameRef).setVisible(true);
        ranBossHotspot.setVisible(!GameState.getInstance().isBossDefeated());
        updateArrowVisibility();
        hudPanel.updateHUD();
    }

    private void triggerRanBossEncounter() {
        GameState gs = GameState.getInstance();
        if (gs.isBossDefeated()) {
            JOptionPane.showMessageDialog(this, "Le reti di Rán sono spezzate. Il santuario sommerso è calmo.", "Boss Già Sconfitto", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        setArrowsVisible(false);
        ranBossHotspot.setVisible(false);
        dialogueBoxOverlay.setVisible(true);

        dialogueSystem.loadDialogue("/dialogues/ran_boss_dialogue.txt");
        dialogueSystem.startDialogue();
    }

    private void initDialogueUI() {
        characterSpriteLabel = new JLabel();
        characterSpriteLabel.setBounds(340, 30, 600, 620);
        characterSpriteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        characterSpriteLabel.setVerticalAlignment(SwingConstants.CENTER);
        characterSpriteLabel.setVisible(false);
        add(characterSpriteLabel);

        dialogueBoxOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                if (dialogueBoxImage != null) {
                    g2d.drawImage(dialogueBoxImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2d.setColor(new Color(20, 20, 20, 230));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        dialogueBoxOverlay.setLayout(null);
        dialogueBoxOverlay.setOpaque(false);
        dialogueBoxOverlay.setBounds(40, 470, 1200, 200);
        dialogueBoxOverlay.setVisible(false);

        dialogueNameLabel = new JLabel("");
        dialogueNameLabel.setFont(new Font("Georgia", Font.BOLD, 24));
        dialogueNameLabel.setForeground(new Color(255, 215, 0));
        dialogueNameLabel.setBounds(65, 25, 500, 30);
        dialogueBoxOverlay.add(dialogueNameLabel);

        dialogueTextLabel = new JLabel("");
        dialogueTextLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        dialogueTextLabel.setForeground(Color.WHITE);
        dialogueTextLabel.setBounds(65, 60, 1070, 95);
        dialogueTextLabel.setVerticalAlignment(SwingConstants.TOP);
        dialogueBoxOverlay.add(dialogueTextLabel);

        dialogueSkipLabel = new JLabel("MOSTRA FRASE");
        dialogueSkipLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        dialogueSkipLabel.setForeground(new Color(200, 200, 200, 180));
        dialogueSkipLabel.setBounds(950, 155, 180, 25);
        dialogueSkipLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        dialogueBoxOverlay.add(dialogueSkipLabel);

        add(dialogueBoxOverlay);
        // In Swing l'indice zero e' davanti: la cornice deve coprire
        // la parte inferiore dello sprite, non essere coperta dal personaggio.
        setComponentZOrder(dialogueBoxOverlay, 0);
        setComponentZOrder(characterSpriteLabel, 1);

        dialogueSystem = new DialogueSystem(
                dialogueNameLabel,
                dialogueTextLabel,
                this::updateDialogueSprite,
                this::onDialogueFinished
        );

        dialogueSkipLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dialogueSkipLabel.setToolTipText("Mostra subito il testo completo");
        dialogueSkipLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dialogueSystem.revealCurrentLine();
            }
        });

        MouseAdapter advanceOnClick = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (dialogueBoxOverlay.isVisible()) dialogueSystem.nextLine();
            }
        };
        dialogueBoxOverlay.addMouseListener(advanceOnClick);
        dialogueNameLabel.addMouseListener(advanceOnClick);
        dialogueTextLabel.addMouseListener(advanceOnClick);
        characterSpriteLabel.addMouseListener(advanceOnClick);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (dialogueBoxOverlay.isVisible()) {
                    dialogueSystem.nextLine();
                }
            }
        });

        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "advanceDialogue2");
        actionMap.put("advanceDialogue2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dialogueBoxOverlay.isVisible()) {
                    dialogueSystem.nextLine();
                }
            }
        });
    }

    private void updateDialogueSprite() {
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

    private void onDialogueFinished() {
        dialogueBoxOverlay.setVisible(false);
        characterSpriteLabel.setVisible(false);
        updateArrowVisibility();

        if (currentRoom == 2) {
            siegfriedHotspot.setVisible(true);
        } else if (currentRoom == 7 && !GameState.getInstance().isBossDefeated()) {
            RanStrategyBossOverlay bossFight = new RanStrategyBossOverlay(mainFrameRef);
            bossFight.setVisible(true);
            ranBossHotspot.setVisible(true);
            hudPanel.updateHUD();
            updateArrowVisibility();
        }
    }
}
