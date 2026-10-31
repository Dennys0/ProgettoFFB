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
import java.util.ArrayList;
import java.util.List;

public class Chapter1Panel extends AbstractChapterPanel {

    private JButton room7ScreenClickHotspot;

    // Elementi UI per il sistema di dialogo in-game
    private JPanel dialogueBoxOverlay;
    private JLabel dialogueNameLabel;
    private JLabel dialogueTextLabel;
    private JLabel dialogueSkipLabel;
    private JLabel characterSpriteLabel;
    private DialogueSystem dialogueSystem;
    private Map<String, ImageIcon> characterSprites;

    // Hotspots
    private JButton martinoHotspot;
    private JButton cauldronHotspot;

    /** Ogni conversazione ha una conseguenza propria: gli NPC non aprono il boss. */
    enum DialogueKind { NONE, MARTINO, NPC, BOSS_INTRO }
    private DialogueKind activeDialogueKind = DialogueKind.NONE;
    private final Map<Integer, List<HotspotButton>> npcHotspots = new HashMap<>();

    static boolean opensBossAfterDialogue(DialogueKind kind, int room, boolean bossDefeated) {
        return kind == DialogueKind.BOSS_INTRO && room == 7 && !bossDefeated;
    }

    public Chapter1Panel(JFrame mainFrame) {
        initializeChapterBase(
                mainFrame,
                Map.of(
                        1, "/images/chapter1/room1_deck.png",
                        2, "/images/chapter1/room2_helm.png",
                        3, "/images/chapter1/room3_prow.png",
                        4, "/images/chapter1/room4_lab.png",
                        5, "/images/chapter1/room5_hold.png",
                        6, "/images/chapter1/room6_crowsnest.png",
                        7, "/images/chapter1/room7_boss.png"
                ),
                Map.of(
                        1, Map.of("LEFT", 3, "RIGHT", 2, "DOWN", 5),
                        2, Map.of("LEFT", 1, "UP", 6),
                        3, Map.of("RIGHT", 1, "LEFT", 7),
                        4, Map.of("RIGHT", 5),
                        5, Map.of("UP", 1, "LEFT", 4),
                        6, Map.of("DOWN", 2),
                        7, Map.of("RIGHT", 3)
                )
        );

        // 2. Caricamento Risorse Grafiche Dialoghi

        characterSprites = new HashMap<>();
        loadSprite("MARTINO", "/images/characters/martino_realms.png");
        loadSprite("HELA", "/images/characters/hela_realms.png");
        loadSprite("SIEGFRIED", "/images/characters/siegfried_realms.png");
        loadSprite("GUNNAR", "/images/characters/gunnar_realms.png");
        loadSprite("MOZZO", "/images/characters/mozzo_realms.png");
        loadSprite("DIAVOLETTO", "/images/characters/diavoletto_realms.png");
        loadSprite("GRIFONE", "/images/characters/grifone_realms.png");

        // 4. Inizializzazione Hotspots & Oggetti
        initHotspots();

        // 5. Inizializzazione UI Dialoghi
        initDialogueUI();

        loadRoom(1);
    }

private void loadSprite(String name, String path) {
    try {
        ImageIcon icon = new ImageIcon(getClass().getResource(path));
        // Se il boss Ã¨ molto largo (come HrÃ¦svelgr), ridimensioniamo proporzionato in base all'altezza
        Image img = icon.getImage().getScaledInstance(-1, 520, Image.SCALE_SMOOTH);
        characterSprites.put(name, new ImageIcon(img));
    } catch (Exception e) {
        System.err.println("Sprite non trovato per " + name + " in " + path);
    }
}

    private void initHotspots() {
        room7ScreenClickHotspot = new HotspotButton();
        room7ScreenClickHotspot.setBounds(150, 100, 980, 500); // Copre l'area centrale della Stanza 7
        room7ScreenClickHotspot.setContentAreaFilled(false);
        room7ScreenClickHotspot.setBorderPainted(false);
        room7ScreenClickHotspot.setFocusPainted(false);
        room7ScreenClickHotspot.setFocusable(false);
        room7ScreenClickHotspot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        room7ScreenClickHotspot.setToolTipText("Esamina il guardiano delle tempeste");
        room7ScreenClickHotspot.addActionListener(e -> triggerBossEncounter());
        add(room7ScreenClickHotspot);

// Carica lo sprite di HrÃ¦svelgr
        loadSprite("HRAESVELGR", "/images/characters/hraesvelgr.png");
        // Hotspot Martino al Timone (Stanza 2)
        martinoHotspot = new HotspotButton();
        martinoHotspot.setBounds(500, 200, 280, 400);
        martinoHotspot.setContentAreaFilled(false);
        martinoHotspot.setBorderPainted(false);
        martinoHotspot.setFocusPainted(false);
        martinoHotspot.setFocusable(false);
        martinoHotspot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        martinoHotspot.setToolTipText("Parla con Martino");
        martinoHotspot.addActionListener(e -> startMartinoDialogue());
        add(martinoHotspot);

        // Hotspot invisibile per il Calderone nella Cabina Alchimista (Stanza 4)
        cauldronHotspot = new HotspotButton();
        cauldronHotspot.setBounds(500, 350, 280, 250);
        cauldronHotspot.setContentAreaFilled(false);
        cauldronHotspot.setBorderPainted(false);
        cauldronHotspot.setFocusPainted(false);
        cauldronHotspot.setFocusable(false);
        cauldronHotspot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cauldronHotspot.setToolTipText("Interagisci con il calderone d'ottone");
        cauldronHotspot.addActionListener(e -> interactWithCauldron());
        add(cauldronHotspot);

        // Gli NPC sono gia' disegnati nelle cinque nuove immagini delle stanze.
        // Usiamo quindi soltanto aree invisibili e lo sprite nei dialoghi.
        addNpcHotspot(1, new Rectangle(710, 130, 150, 180),
                "Parla con il mozzo", "/dialogues/mozzo_dialogue.txt");
        addNpcHotspot(3, new Rectangle(855, 192, 245, 205),
                "Osserva il grifone", "/dialogues/grifone_dialogue.txt");
        addNpcHotspot(4, new Rectangle(120, 218, 195, 220),
                "Parla con il diavoletto", "/dialogues/diavoletto_dialogue.txt");
        addNpcHotspot(6, new Rectangle(278, 300, 195, 190),
                "Parla con Huginn", "/dialogues/huginn_dialogue.txt");
        addNpcHotspot(7, new Rectangle(135, 178, 420, 290),
                "Parla con Veðrfölnir", "/dialogues/vedrfolnir_dialogue.txt");

        // Creazione bottoni visibili per gli oggetti raccolti a schermo
        addItemWorldButton("MAPPA_CIELO", "/images/items/item_map.png", 1080, 280, 100, 120, 5,
                "Mappa del Cielo", "Una pergamena cosmica trovata nella stiva.");

        addItemWorldButton("NEBBIA_NIFLHEIM", "/images/items/item_fog.png", 780, 520, 60, 60, 5,
                "Nebbia di Niflheim", "Una fiala sigillata contenente fumo denso e gelido.");

        addItemWorldButton("PIUMA_FOLGORE", "/images/items/item_feather.png", 740, 520, 80, 80, 6,
                "Piuma di Folgore", "Una piuma dorata carica di elettricitÃ  incastrata tra le carrucole.");

        addItemWorldButton("POLLINE_YGGDRASIL", "/images/items/item_pollen.png", 445, 530, 65, 65, 3,
                "Polline di Yggdrasil", "Polvere dorata fluttuante nell'aria.");

        addItemWorldButton("RUGIADA_ASGARD", "/images/items/item_dew.png", 655, 486, 65, 65, 3,
                "Rugiada delle Nuvole di Asgard", "Acqua fluttuante condensata sulla polena drago.");
    }

    private HotspotButton addNpcHotspot(int room, Rectangle bounds,
                                       String label, String dialogueResource) {
        HotspotButton hotspot = new HotspotButton();
        hotspot.setBounds(bounds);
        hotspot.setToolTipText(label);
        hotspot.getAccessibleContext().setAccessibleName(label);
        hotspot.addActionListener(e -> startSceneDialogue(dialogueResource, DialogueKind.NPC));
        npcHotspots.computeIfAbsent(room, ignored -> new ArrayList<>()).add(hotspot);
        add(hotspot);
        hotspot.setVisible(false);
        return hotspot;
    }

    private void setNpcHotspotsVisible(boolean visible) {
        for (Map.Entry<Integer, List<HotspotButton>> entry : npcHotspots.entrySet()) {
            for (HotspotButton button : entry.getValue()) {
                boolean show = visible && entry.getKey() == currentRoom;
                button.setVisible(show);
                if (show) setComponentZOrder(button, 0);
            }
        }
    }

    /**
     * Un unico punto di ingresso per Martino, gli NPC e il boss.
     * La callback finale usera' il tipo di dialogo, non la stanza, per
     * determinare se aprire la donazione, il combattimento o nulla.
     */
    private void startSceneDialogue(String resource, DialogueKind kind) {
        if (dialogueBoxOverlay.isVisible()) return;
        activeDialogueKind = kind;
        setArrowsVisible(false);
        setNpcHotspotsVisible(false);
        martinoHotspot.setVisible(false);
        cauldronHotspot.setVisible(false);
        room7ScreenClickHotspot.setVisible(false);
        dialogueBoxOverlay.setVisible(true);
        setComponentZOrder(dialogueBoxOverlay, 0);
        setComponentZOrder(characterSpriteLabel, 1);
        dialogueSystem.loadDialogue(resource);
        dialogueSystem.startDialogue();
        repaint();
    }

    private void interactWithCauldron() {
        boolean confirm = StoryPromptDialog.confirm(mainFrameRef,
                StoryPromptDialog.Theme.SKY, "CALDERONE D'OTTONE",
                "Il calderone e' pronto a raccogliere tre ingredienti.\n"
                + "Vuoi iniziare la preparazione dell'Asgardian Aviation?",
                "PREPARA IL DRINK", "NON ANCORA");
        if (confirm) {
            CauldronOverlay cauldron = new CauldronOverlay(mainFrameRef);
            cauldron.setVisible(true);
        }
    }

    private void initDialogueUI() {
        characterSpriteLabel = new JLabel();
characterSpriteLabel.setBounds(340, 30, 600, 620); // PiÃ¹ ampia per contenere le ali del boss
characterSpriteLabel.setHorizontalAlignment(SwingConstants.CENTER);
characterSpriteLabel.setVerticalAlignment(SwingConstants.CENTER);
characterSpriteLabel.setVisible(false);
add(characterSpriteLabel);

        SharedDialogueBox sharedDialogueBox = new SharedDialogueBox();
        dialogueBoxOverlay = sharedDialogueBox;
        dialogueBoxOverlay.setBounds(40, 470, 1200, 200);
        dialogueBoxOverlay.setVisible(false);
        dialogueNameLabel = sharedDialogueBox.getSpeakerLabel();
        dialogueTextLabel = sharedDialogueBox.getTextLabel();
        dialogueSkipLabel = sharedDialogueBox.getRevealLabel();

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
        // I nuovi dialoghi includono frasi lunghe: manteniamo il testo
        // entro la cornice illustrata anche durante l'effetto macchina da scrivere.
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
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "advanceDialogue");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "advanceDialogue");
        actionMap.put("advanceDialogue", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dialogueBoxOverlay.isVisible()) {
                    dialogueSystem.nextLine();
                }
            }
        });
    }

    @Override
    protected void onRoomChanged() {
        // Un cambio stanza (anche tramite F4 o caricamento) chiude un
        // dialogo in corso senza attivarne la callback narrativa.
        if (dialogueBoxOverlay != null && dialogueBoxOverlay.isVisible()) {
            dialogueSystem.stop();
            dialogueBoxOverlay.setVisible(false);
            characterSpriteLabel.setVisible(false);
            activeDialogueKind = DialogueKind.NONE;
        }
        showRoomInteractionHotspots();
    }

    private void showRoomInteractionHotspots() {
        boolean idle = dialogueBoxOverlay == null || !dialogueBoxOverlay.isVisible();
        martinoHotspot.setVisible(idle && currentRoom == 2);
        cauldronHotspot.setVisible(idle && currentRoom == 4);
        room7ScreenClickHotspot.setVisible(idle && currentRoom == 7);
        if (idle && currentRoom == 7) setComponentZOrder(room7ScreenClickHotspot, 0);
        // I nuovi NPC devono essere sopra le aree generiche della stanza 7.
        setNpcHotspotsVisible(idle);
    }

    @Override
    protected boolean canCollectWorldItem(String itemId, GameState state) {
        if ("RUGIADA_ASGARD".equals(itemId) && !state.hasDragonKey()) {
            StoryPromptDialog.message(mainFrameRef, StoryPromptDialog.Theme.SKY,
                    "SERBATOIO BLOCCATO",
                    "La polena a drago e' bloccata da un meccanismo d'ottone.\n"
                    + "Serve la Chiave del Drago!");
            return false;
        }
        return true;
    }

    @Override
    protected boolean shouldShowWorldItem(String itemId, GameState state) {
        return !("MAPPA_CIELO".equals(itemId) && state.hasGivenMapToMartino());
    }

    private void startMartinoDialogue() {
        startSceneDialogue("/dialogues/martino_dialogue.txt", DialogueKind.MARTINO);
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
        DialogueKind completed = activeDialogueKind;
        activeDialogueKind = DialogueKind.NONE;
        dialogueBoxOverlay.setVisible(false);
        characterSpriteLabel.setVisible(false);
        characterSpriteLabel.setIcon(null);
        updateArrowVisibility();
        showRoomInteractionHotspots();

        if (completed == DialogueKind.MARTINO && currentRoom == 2) {
            // Il flusso della mappa resta identico a quello originale.
            DialogueChoiceOverlay overlay = new DialogueChoiceOverlay(mainFrameRef, gaveMap -> {
                if (gaveMap) System.out.println("Mappa consegnata! Ottenuta la Chiave del Drago.");
                martinoHotspot.setVisible(true);
                hudPanel.updateHUD();
            });
            overlay.setVisible(true);
        } else if (opensBossAfterDialogue(completed, currentRoom,
                GameState.getInstance().isBossDefeated())) {
            room7ScreenClickHotspot.setVisible(false);
            new BossFightOverlay(mainFrameRef).setVisible(true);
            showRoomInteractionHotspots();
            hudPanel.updateHUD();
        }
        repaint();
    }

    /** Checkpoint del boss: salta il dialogo introduttivo gia' visto. */
    public void retryBossEncounter() {
        if (GameState.getInstance().isBossDefeated()) return;
        loadRoom(7);
        setArrowsVisible(false);
        room7ScreenClickHotspot.setVisible(false);
        new BossFightOverlay(mainFrameRef).setVisible(true);
        room7ScreenClickHotspot.setVisible(true);
        updateArrowVisibility();
        hudPanel.updateHUD();
    }

    private void triggerBossEncounter() {
    GameState gs = GameState.getInstance();
    if (gs.isBossDefeated()) {
        StoryPromptDialog.message(mainFrameRef, StoryPromptDialog.Theme.SKY,
                "IL GUARDIANO DORME",
                "La tempesta si e' placata. Hraesvelgr osserva il vascello in silenzio.");
        return;
    }

    boolean fight = StoryPromptDialog.confirm(mainFrameRef,
            StoryPromptDialog.Theme.SKY, "IL GUARDIANO DELLE TEMPESTE",
            "Tra le nubi, Hraesvelgr spiega le sue ali sul ponte.\n"
            + "Affrontarlo potrebbe mettere in pericolo l'intero equipaggio.\n\n"
            + "Vuoi iniziare lo scontro?",
            "AFFRONTA IL BOSS", "NON ANCORA");
    if (!fight) return;

    // Passa allo sfondo room7_boss2.png
    try {
        currentBgImage = new ImageIcon(getClass().getResource("/images/chapter1/room7_boss2.png")).getImage();
    } catch (Exception e) {
        System.err.println("Sfondo room7_boss2.png non trovato!");
    }
    repaint();

    // Il combattimento si avvia solo alla fine di QUESTO dialogo, non
    // al termine della conversazione con Veðrfölnir nella stessa stanza.
    startSceneDialogue("/dialogues/boss_intro_dialogue.txt", DialogueKind.BOSS_INTRO);
}
}
