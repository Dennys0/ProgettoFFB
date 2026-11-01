package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.DialogueSystem;
import com.alchemist.hangover.model.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chapter2Panel extends AbstractChapterPanel {

    // Elementi Mondo di Gioco
    private JButton siegfriedHotspot;
    private JButton cauldronHotspot;
    private JButton ranBossHotspot;

    // UI Dialoghi
    private JPanel dialogueBoxOverlay;
    private JLabel characterSpriteLabel;
    private DialogueSystem dialogueSystem;
    private Map<String, ImageIcon> characterSprites;

    /** The meaning of a conversation must not depend on its room number. */
    enum DialogueKind { NONE, SIEGFRIED, NPC, BOSS_INTRO }
    private DialogueKind activeDialogueKind = DialogueKind.NONE;

    /** 1280 x 720 logical coordinates, aligned to the provided illustrations. */
    record NpcDefinition(int room, Rectangle bounds, String tooltip, String dialogue) { }

    static final List<NpcDefinition> NPC_DEFINITIONS = List.of(
            new NpcDefinition(1, new Rectangle(765, 287, 285, 274),
                    "Parla con Marmæl", "/dialogues/ch2_marmael_dialogue.txt"),
            new NpcDefinition(3, new Rectangle(1002, 102, 265, 225),
                    "Parla con lo sgherro", "/dialogues/ch2_sgherro_dialogue.txt"),
            new NpcDefinition(4, new Rectangle(710, 110, 215, 240),
                    "Parla con la sirena", "/dialogues/ch2_sirena_dialogue.txt"),
            new NpcDefinition(5, new Rectangle(365, 135, 540, 345),
                    "Parla con Jörmungandr", "/dialogues/ch2_jormungandr_dialogue.txt"),
            new NpcDefinition(6, new Rectangle(140, 165, 300, 285),
                    "Parla con il Capitano", "/dialogues/ch2_captain_dialogue.txt"),
            new NpcDefinition(7, new Rectangle(940, 175, 280, 285),
                    "Parla con Hrönn", "/dialogues/ch2_hronn_dialogue.txt")
    );

    static boolean opensRanAfterDialogue(DialogueKind kind, int room, boolean bossDefeated) {
        return kind == DialogueKind.BOSS_INTRO && room == 7 && !bossDefeated;
    }

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


        characterSprites = new HashMap<>();
        loadSprite("SIEGFRIED", "/images/characters/siegfried_realms.png");
        loadSprite("RÁN", "/images/characters/ran_boss.png");
        loadSprite("RAN", "/images/characters/ran_boss.png");
        loadSprite("MARMÆL", "/images/characters/ch2_marmael.png");
        loadSprite("SGHERRO", "/images/characters/diavoletto_realms.png");
        loadSprite("SIRENA", "/images/characters/ch2_sirena.png");
        loadSprite("JORMUNGANDR", "/images/characters/ch2_jormungandr.png");
        loadSprite("CAPITANO", "/images/characters/ch2_captain.png");
        loadSprite("HRÖNN", "/images/characters/ch2_hronn.png");

        initHotspots();
        initDialogueUI();
        loadRoom(1);
    }

    private void loadSprite(String name, String path) {
        ImageIcon icon = SharedDialogueBox.scaledPortrait(path, 520);
        if (icon != null) characterSprites.put(name, icon);
        else System.err.println("Sprite non trovato per " + name + " in " + path);
    }

    private void initHotspots() {
        // NPC Siegfried al Bancone (Stanza 2)
        siegfriedHotspot = new HotspotButton();
        siegfriedHotspot.setBounds(790, 175, 235, 385);
        siegfriedHotspot.setToolTipText("Parla con Siegfried");
        siegfriedHotspot.addActionListener(e -> startSiegfriedDialogue());
        add(siegfriedHotspot);

        // Calderone nella Sacrestia Corallina (Stanza 3)
        cauldronHotspot = new HotspotButton();
        cauldronHotspot.setBounds(500, 350, 280, 250);
        cauldronHotspot.setToolTipText("Interagisci con il calderone");
        cauldronHotspot.addActionListener(e -> interactWithCauldron());
        add(cauldronHotspot);

        // Boss Rán nel Santuario (Stanza 7 - Area ridotta per non coprire le frecce)
        ranBossHotspot = new HotspotButton();
        // The whirlpool is in the middle; Hrönn sits to its right.
        // Separate clickable areas allow talking to her without fighting.
        ranBossHotspot.setBounds(400, 300, 470, 265);
        ranBossHotspot.setToolTipText("Affronta Rán");
        ranBossHotspot.addActionListener(e -> triggerRanBossEncounter());
        add(ranBossHotspot);

        // NPCs are baked into the seven scenes: only transparent hotzones.
        for (NpcDefinition npc : NPC_DEFINITIONS) {
            addNpcHotspot(npc);
        }

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

    private void addNpcHotspot(NpcDefinition npc) {
        registerNpcHotspot(npc.room(), npc.bounds(), npc.tooltip(),
                () -> startSceneDialogue(npc.dialogue(), DialogueKind.NPC));
    }

    @Override
    protected void onRoomChanged() {
        // Scene changes made by debug navigation or loading must not
        // accidentally complete the previous room's story or start Rán.
        if (dialogueBoxOverlay != null && dialogueBoxOverlay.isVisible()) {
            dialogueSystem.stop();
            dialogueBoxOverlay.setVisible(false);
            characterSpriteLabel.setVisible(false);
            activeDialogueKind = DialogueKind.NONE;
        }
        showRoomHotspots();
    }

    private void showRoomHotspots() {
        boolean idle = dialogueBoxOverlay == null || !dialogueBoxOverlay.isVisible();
        siegfriedHotspot.setVisible(idle && currentRoom == 2);
        cauldronHotspot.setVisible(idle && currentRoom == 3);
        ranBossHotspot.setVisible(idle && currentRoom == 7
                && !GameState.getInstance().isBossDefeated());
        setRegisteredNpcHotspotsVisible(idle);
    }

    /** One shared entry point for Siegfried, room NPCs and Rán. */
    private void startSceneDialogue(String resource, DialogueKind kind) {
        if (dialogueBoxOverlay.isVisible()) return;
        activeDialogueKind = kind;
        setArrowsVisible(false);
        siegfriedHotspot.setVisible(false);
        cauldronHotspot.setVisible(false);
        ranBossHotspot.setVisible(false);
        setRegisteredNpcHotspotsVisible(false);
        dialogueBoxOverlay.setVisible(true);
        setComponentZOrder(dialogueBoxOverlay, 0);
        setComponentZOrder(characterSpriteLabel, 1);
        dialogueSystem.loadDialogue(resource);
        dialogueSystem.startDialogue();
    }

    private void startSiegfriedDialogue() {
        boolean hasJournal = GameState.getInstance().hasItem("DIARIO_CAPITANO");
        String dialoguePath = hasJournal
                ? "/dialogues/siegfried_dialogue_2.txt"
                : "/dialogues/siegfried_dialogue_1.txt";
        startSceneDialogue(dialoguePath, DialogueKind.SIEGFRIED);
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
            StoryPromptDialog.message(mainFrameRef, StoryPromptDialog.Theme.OCEAN,
                    "RAN E' STATA PLACATA",
                    "Le reti di Ran sono spezzate. Il santuario sommerso e' calmo.");
            return;
        }

        if (!StoryPromptDialog.confirm(mainFrameRef,
                StoryPromptDialog.Theme.OCEAN, "IL SANTUARIO DI RAN",
                "La dea attende nelle profondita', circondata dalle sue reti.\n"
                + "Potresti sfidarla con le parole del Diario del Capitano.\n\n"
                + "Vuoi iniziare lo scontro?",
                "AFFRONTA RAN", "NON ANCORA")) return;

        // Only this explicit boss-introduction dialogue starts the fight.
        // Talking to Hrönn alone must never trigger the boss.
        startSceneDialogue("/dialogues/ran_boss_dialogue.txt", DialogueKind.BOSS_INTRO);
    }

    /**
     * Posizione del ritratto durante la conversazione (NON dell'hotspot
     * nel fondale). Gli altri NPC mantengono l'impaginazione precedente.
     */
    static Rectangle portraitBoundsFor(String speaker) {
        if (speaker == null) return new Rectangle(340, 30, 600, 620);
        return switch (speaker) {
            case "SGHERRO" -> new Rectangle(55, 30, 540, 620);
            case "MARMÆL" -> new Rectangle(45, 30, 550, 620);
            case "JORMUNGANDR" -> new Rectangle(200, 30, 880, 620);
            default -> new Rectangle(340, 30, 600, 620);
        };
    }

    private void initDialogueUI() {
        characterSpriteLabel = new JLabel();
        characterSpriteLabel.setBounds(portraitBoundsFor(null));
        characterSpriteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        characterSpriteLabel.setVerticalAlignment(SwingConstants.CENTER);
        characterSpriteLabel.setVisible(false);
        add(characterSpriteLabel);

        SharedDialogueBox sharedDialogueBox = new SharedDialogueBox();
        dialogueBoxOverlay = sharedDialogueBox;
        dialogueBoxOverlay.setBounds(40, 470, 1200, 200);
        dialogueBoxOverlay.setVisible(false);
        JLabel dialogueNameLabel = sharedDialogueBox.getSpeakerLabel();
        JLabel dialogueTextLabel = sharedDialogueBox.getTextLabel();
        JLabel dialogueSkipLabel = sharedDialogueBox.getRevealLabel();

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
        // Longer NPC stories must wrap within the illustrated dialogue box.
        dialogueSystem.setWrapWidth(1000);

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
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "advanceDialogue2");
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
                characterSpriteLabel.setBounds(portraitBoundsFor(speaker));
                characterSpriteLabel.setIcon(characterSprites.get(speaker));
                characterSpriteLabel.setVisible(true);
            } else {
                characterSpriteLabel.setVisible(false);
            }
        }
    }

    private void onDialogueFinished() {
        DialogueKind completed = activeDialogueKind;
        activeDialogueKind = DialogueKind.NONE;
        dialogueBoxOverlay.setVisible(false);
        characterSpriteLabel.setVisible(false);
        characterSpriteLabel.setIcon(null);
        updateArrowVisibility();
        showRoomHotspots();

        if (opensRanAfterDialogue(completed, currentRoom,
                GameState.getInstance().isBossDefeated())) {
            ranBossHotspot.setVisible(false);
            new RanStrategyBossOverlay(mainFrameRef).setVisible(true);
            showRoomHotspots();
            updateArrowVisibility();
            hudPanel.updateHUD();
        }
        repaint();
    }
}
