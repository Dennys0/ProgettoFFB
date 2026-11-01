package com.alchemist.hangover.view;

import com.alchemist.hangover.view.cauldron.CauldronController;
import com.alchemist.hangover.view.cauldron.CauldronOverlay;

import com.alchemist.hangover.view.boss.RanStrategyBossOverlay;

import com.alchemist.hangover.controller.DialogueSystem;
import com.alchemist.hangover.model.GameState;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Chapter2Panel extends AbstractChapterPanel {

    // Elementi Mondo di Gioco
    private JButton siegfriedHotspot;
    private JButton cauldronHotspot;
    private JButton ranBossHotspot;

    // UI Dialoghi
    private final DialogueManager.SceneSession dialogue;

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
        initializeChapterBase(mainFrame, ChapterLayouts.forChapter(2));

        dialogue = new DialogueManager.SceneSession(this,
                Chapter2Panel::portraitBoundsFor,
                this::onDialogueFinished);
        dialogue.addPortrait("SIEGFRIED", "/images/characters/siegfried_realms.png");
        dialogue.addPortrait("RÁN", "/images/characters/ran_boss.png");
        dialogue.addPortrait("RAN", "/images/characters/ran_boss.png");
        dialogue.addPortrait("MARMÆL", "/images/characters/ch2_marmael.png");
        dialogue.addPortrait("SGHERRO", "/images/characters/diavoletto_realms.png");
        dialogue.addPortrait("SIRENA", "/images/characters/ch2_sirena.png");
        dialogue.addPortrait("JORMUNGANDR", "/images/characters/ch2_jormungandr.png");
        dialogue.addPortrait("CAPITANO", "/images/characters/ch2_captain.png");
        dialogue.addPortrait("HRÖNN", "/images/characters/ch2_hronn.png");

        initHotspots();
        loadRoom(1);
    }

    private void initHotspots() {
        // NPC Siegfried al Bancone (Stanza 2)
        siegfriedHotspot = registerActionHotspot(
                new Rectangle(790, 175, 235, 385),
                "Parla con Siegfried", this::startSiegfriedDialogue);

        // Calderone nella Sacrestia Corallina (Stanza 3)
        cauldronHotspot = registerActionHotspot(
                new Rectangle(500, 350, 280, 250),
                "Interagisci con il calderone", this::interactWithCauldron);

        // Boss Rán nel Santuario (Stanza 7 - Area ridotta per non coprire le frecce)
        // Il vortice e' al centro; il pulsante non deve coprire Hrönn.
        ranBossHotspot = registerActionHotspot(
                new Rectangle(400, 300, 470, 265),
                "Affronta Rán", this::triggerRanBossEncounter);

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
        if (!CauldronController.allowOpening(mainFrameRef)) return;
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
        if (dialogue.isVisible()) {
            dialogue.stop();
            activeDialogueKind = DialogueKind.NONE;
        }
        showRoomHotspots();
    }

    private void showRoomHotspots() {
        boolean idle = !dialogue.isVisible();
        siegfriedHotspot.setVisible(idle && currentRoom == 2);
        cauldronHotspot.setVisible(idle && currentRoom == 3);
        ranBossHotspot.setVisible(idle && currentRoom == 7
                && !GameState.getInstance().isBossDefeated());
        setRegisteredNpcHotspotsVisible(idle);
    }

    /** One shared entry point for Siegfried, room NPCs and Rán. */
    private void startSceneDialogue(String resource, DialogueKind kind) {
        if (dialogue.isVisible()) return;
        activeDialogueKind = kind;
        setArrowsVisible(false);
        siegfriedHotspot.setVisible(false);
        cauldronHotspot.setVisible(false);
        ranBossHotspot.setVisible(false);
        setRegisteredNpcHotspotsVisible(false);
        dialogue.start(resource);
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

    private void onDialogueFinished() {
        DialogueKind completed = activeDialogueKind;
        activeDialogueKind = DialogueKind.NONE;
        dialogue.stop();
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
