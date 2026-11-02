package com.alchemist.hangover.view;

import com.alchemist.hangover.view.cauldron.CauldronController;
import com.alchemist.hangover.view.cauldron.CauldronOverlay;

import com.alchemist.hangover.view.boss.RanStrategyBossOverlay;

import com.alchemist.hangover.controller.AudioManager;
import com.alchemist.hangover.model.Item;
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
            new NpcDefinition(1, new Rectangle(757, 390, 116, 181),
                    "Parla con Marmæl", "/dialogues/ch2_marmael_dialogue.txt"),
            new NpcDefinition(3, new Rectangle(1027, 105, 91, 79),
                    "Parla con lo sgherro", "/dialogues/ch2_sgherro_dialogue.txt"),
            new NpcDefinition(4, new Rectangle(576, 141, 277, 131),
                    "Parla con la sirena", "/dialogues/ch2_sirena_dialogue.txt"),
            new NpcDefinition(5, new Rectangle(627, 143, 121, 91),
                    "Parla con Jörmungandr", "/dialogues/ch2_jormungandr_dialogue.txt"),
            new NpcDefinition(6, new Rectangle(120, 133, 290, 269),
                    "Parla con il Capitano", "/dialogues/ch2_captain_dialogue.txt"),
            new NpcDefinition(7, new Rectangle(908, 218, 111, 145),
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
        dialogue.addPortrait("SIEGFRIED", "/images/shared/npc/siegfried_realms.png");
        dialogue.addPortrait("RÁN", "/images/chapter2/npc/ran_boss.png");
        dialogue.addPortrait("RAN", "/images/chapter2/npc/ran_boss.png");
        dialogue.addPortrait("MARMÆL", "/images/chapter2/npc/marmael.png");
        dialogue.addPortrait("SGHERRO", "/images/shared/npc/diavoletto_realms.png");
        dialogue.addPortrait("SIRENA", "/images/chapter2/npc/sirena.png");
        dialogue.addPortrait("JORMUNGANDR", "/images/chapter2/npc/jormungandr.png");
        dialogue.addPortrait("CAPITANO", "/images/chapter2/npc/captain.png");
        dialogue.addPortrait("HRÖNN", "/images/chapter2/npc/hronn.png");

        initHotspots();
        loadRoom(1);
    }

    private void initHotspots() {
        // NPC Siegfried al Bancone (Stanza 2)
        siegfriedHotspot = registerActionHotspot(
                new Rectangle(775, 264, 249, 418),
                "Parla con Siegfried", this::startSiegfriedDialogue);

        // Calderone nella Sacrestia Corallina (Stanza 3)
        cauldronHotspot = registerActionHotspot(
                new Rectangle(545, 322, 159, 124),
                "Interagisci con il calderone", this::interactWithCauldron);

        // Boss Rán nel Santuario (Stanza 7 - Area ridotta per non coprire le frecce)
        // Il vortice e' al centro; il pulsante non deve coprire Hrönn.
        ranBossHotspot = registerActionHotspot(
                new Rectangle(414, 365, 607, 237),
                "Affronta Rán", this::triggerRanBossEncounter);

        // NPCs are baked into the seven scenes: only transparent hotzones.
        for (NpcDefinition npc : NPC_DEFINITIONS) {
            addNpcHotspot(npc);
            if (npc.room() == 5) {
                // Spire in primo piano: seguono il corpo senza coprire tutta la fossa.
                for (Rectangle coil : List.of(new Rectangle(8, 230, 235, 121),
                        new Rectangle(190, 327, 212, 118), new Rectangle(366, 417, 195, 116),
                        new Rectangle(534, 506, 216, 122), new Rectangle(735, 446, 122, 148))) {
                    registerNpcHotspot(5, coil, npc.tooltip(), () -> startSceneDialogue(npc.dialogue(), DialogueKind.NPC));
                }
            }
        }

        // Oggetti Raccoglibili (Stanza 4 e Stanza 5)
        addItemWorldButton("CORALLO_RAN", "/images/chapter2/items/coral.png", 211, 510, 52, 52, 4,
                "Corallo di Rán", "Un ramo di corallo aspro rosso sangue incastrato nelle rocce.");

        addItemWorldButton("INCHIOSTRO_KRAKEN", "/images/chapter2/items/ink.png", 1022, 491, 85, 62, 4,
                "Inchiostro di Kraken", "Una boccetta di liquido buio e denso depositata nell'anfratto.");

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
        if (kind == DialogueKind.NPC && currentRoom == 5)
            AudioManager.getInstance().playEffect(AudioManager.Effect.SERPENT_CALL);
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

    private void captainOrders() {
        if (GameState.getInstance().hasItem("DIARIO_CAPITANO")) return;
        String[] orders = {
                "La rete sale a babordo. Al mio segnale molli la cima. Ricevuto?",
                "Anche se senti il tuo nome tra le onde, tieni gli occhi sulla prua. Intesi?",
                "Se cado io, porti tu il codice al timoniere. Ho la tua parola?"
        };
        for (String order : orders) {
            if (!StoryPromptDialog.confirm(mainFrameRef, StoryPromptDialog.Theme.OCEAN,
                    "GLI ORDINI DI BARBANERA", order, "Sì, capitano!", "Aspetti un momento…")) {
                ToastNotification.show(this, "Barbanera richiude il diario. Torna quando sarai pronto.");
                return;
            }
        }
        receiveItem(new Item("DIARIO_CAPITANO", "Diario del Capitano",
                RanStrategyBossOverlay.journalCode(), "/images/chapter2/items/captain_journal.png"));
    }

    private void onDialogueFinished() {
        DialogueKind completed = activeDialogueKind;
        activeDialogueKind = DialogueKind.NONE;
        dialogue.stop();
        updateArrowVisibility();
        showRoomHotspots();

        if (completed == DialogueKind.NPC) {
            switch (currentRoom) {
                case 1 -> receiveItem(new Item("ACQUA_LETE", "Acqua del Lete",
                        "L’acqua immobile affidata ad Arthur da Marmæl.", "/images/chapter2/items/lete.png"));
                case 5 -> receiveItem(new Item("SALE_JORMUNGANDR", "Sale di Jörmungandr",
                        "Cristalli staccati dalle scaglie del Serpente del Mondo.", "/images/chapter2/items/salt.png"));
                case 6 -> captainOrders();
            }
        }
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
