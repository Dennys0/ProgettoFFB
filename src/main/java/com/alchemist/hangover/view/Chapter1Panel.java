package com.alchemist.hangover.view;

import com.alchemist.hangover.view.cauldron.CauldronController;
import com.alchemist.hangover.view.cauldron.CauldronOverlay;

import com.alchemist.hangover.view.boss.BossFightOverlay;

import com.alchemist.hangover.model.Item;
import com.alchemist.hangover.model.GameState;

import javax.swing.*;
import java.awt.*;

public class Chapter1Panel extends AbstractChapterPanel {

    private JButton room7ScreenClickHotspot;
    private HotspotButton chest;
    private static final Rectangle CHEST_AREA = new Rectangle(438, 550, 84, 72);

    // Elementi UI per il sistema di dialogo in-game
    private final DialogueManager.SceneSession dialogue;

    // Hotspots
    private JButton martinoHotspot;
    private JButton cauldronHotspot;

    /** Ogni conversazione ha una conseguenza propria: gli NPC non aprono il boss. */
    enum DialogueKind { NONE, MARTINO, NPC, BOSS_INTRO }
    private DialogueKind activeDialogueKind = DialogueKind.NONE;

    static boolean opensBossAfterDialogue(DialogueKind kind, int room, boolean bossDefeated) {
        return kind == DialogueKind.BOSS_INTRO && room == 7 && !bossDefeated;
    }

    public Chapter1Panel(JFrame mainFrame) {
        initializeChapterBase(mainFrame, ChapterLayouts.forChapter(1));
        // 2. Caricamento Risorse Grafiche Dialoghi

        dialogue = new DialogueManager.SceneSession(this,
                speaker -> "HRAESVELGR".equals(speaker) ? new Rectangle(100, 12, 1080, 450)
                        : new Rectangle(340, 30, 600, 620),
                this::onDialogueFinished);
        dialogue.addPortrait("MARTINO", "/images/chapter1/npc/martino_realms.png");
        dialogue.addPortrait("HELA", "/images/shared/npc/hela_realms.png");
        dialogue.addPortrait("SIEGFRIED", "/images/shared/npc/siegfried_realms.png");
        dialogue.addPortrait("GUNNAR", "/images/shared/npc/gunnar_realms.png");
        dialogue.addPortrait("MOZZO", "/images/chapter1/npc/mozzo_realms.png");
        dialogue.addPortrait("DIAVOLETTO", "/images/shared/npc/diavoletto_realms.png");
        dialogue.addPortrait("GRIFONE", "/images/chapter1/npc/grifone_realms.png");

        // 4. Inizializzazione Hotspots & Oggetti
        initHotspots();

        // 5. Inizializzazione UI Dialoghi

        loadRoom(1);
    }

    private void initHotspots() {
        room7ScreenClickHotspot = registerActionHotspot(
                new Rectangle(150, 100, 980, 500),
                "Esamina il guardiano delle tempeste", this::triggerBossEncounter);

        dialogue.addFittedPortrait("HRAESVELGR", "/images/chapter1/npc/hraesvelgr.png");
        // Hotspot Martino al Timone (Stanza 2)
        martinoHotspot = registerActionHotspot(
                new Rectangle(500, 200, 280, 400),
                "Parla con Martino", this::startMartinoDialogue);

        // Hotspot invisibile per il Calderone nella Cabina Alchimista (Stanza 4)
        cauldronHotspot = registerActionHotspot(
                new Rectangle(500, 350, 280, 250),
                "Interagisci con il calderone d'ottone", this::interactWithCauldron);

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
        addItemWorldButton("MAPPA_CIELO", "/images/chapter1/items/map.png", 968, 348, 78, 44, 5,
                "Mappa del Cielo", "Una pergamena cosmica trovata nella stiva.");

        addItemWorldButton("NEBBIA_NIFLHEIM", "/images/chapter1/items/fog.png", 322, 529, 55, 55, 5,
                "Nebbia di Niflheim", "Una fiala sigillata contenente fumo denso e gelido.");

        chest = registerActionHotspot(CHEST_AREA, "Esamina la serratura d’ottone", this::openChest);
    }

    private void addNpcHotspot(int room, Rectangle bounds,
                               String label, String dialogueResource) {
        registerNpcHotspot(room, bounds, label,
                () -> startSceneDialogue(dialogueResource, DialogueKind.NPC));
    }

    /**
     * Un unico punto di ingresso per Martino, gli NPC e il boss.
     * La callback finale usera' il tipo di dialogo, non la stanza, per
     * determinare se aprire la donazione, il combattimento o nulla.
     */
    private void startSceneDialogue(String resource, DialogueKind kind) {
        if (dialogue.isVisible()) return;
        activeDialogueKind = kind;
        setArrowsVisible(false);
        setRegisteredNpcHotspotsVisible(false);
        martinoHotspot.setVisible(false);
        cauldronHotspot.setVisible(false);
        room7ScreenClickHotspot.setVisible(false);
        chest.setVisible(false);
        dialogue.start(resource);
        repaint();
    }

    private void interactWithCauldron() {
        if (!CauldronController.allowOpening(mainFrameRef)) return;
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

    @Override
    protected void onRoomChanged() {
        // Un cambio stanza (anche tramite F4 o caricamento) chiude un
        // dialogo in corso senza attivarne la callback narrativa.
        if (dialogue.isVisible()) {
            dialogue.stop();
            activeDialogueKind = DialogueKind.NONE;
        }
        showRoomInteractionHotspots();
    }

    private void showRoomInteractionHotspots() {
        boolean idle = !dialogue.isVisible();
        chest.setVisible(idle && currentRoom == 3);
        if (chest.isVisible()) setComponentZOrder(chest, 0);
        martinoHotspot.setVisible(idle && currentRoom == 2);
        cauldronHotspot.setVisible(idle && currentRoom == 4);
        room7ScreenClickHotspot.setVisible(idle && currentRoom == 7);
        if (idle && currentRoom == 7) setComponentZOrder(room7ScreenClickHotspot, 0);
        // I nuovi NPC devono essere sopra le aree generiche della stanza 7.
        setRegisteredNpcHotspotsVisible(idle);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        if (chest != null) chest.setBounds(CHEST_AREA.x * getWidth() / 1280,
                CHEST_AREA.y * getHeight() / 720, CHEST_AREA.width * getWidth() / 1280,
                CHEST_AREA.height * getHeight() / 720);
    }

    private void openChest() {
        GameState state = GameState.getInstance();
        if (state.hasItem("RUGIADA_ASGARD")) {
            ToastNotification.show(this, "La cassa è vuota.");
        } else if (!state.hasDragonKey()) {
            StoryPromptDialog.message(mainFrameRef, StoryPromptDialog.Theme.SKY,
                    "CASSA BLOCCATA", "La serratura d’ottone porta il marchio del drago. Forse il timoniere ha la chiave.");
        } else {
            state.setDragonUnlocked(true);
            receiveItem(new Item("RUGIADA_ASGARD", "Rugiada delle Nuvole di Asgard",
                    "Una fiala custodita nella cassa di prua.", "/images/chapter1/items/dew.png"));
        }
    }

    @Override
    protected boolean shouldShowWorldItem(String itemId, GameState state) {
        return !("MAPPA_CIELO".equals(itemId) && state.hasGivenMapToMartino());
    }

    private void startMartinoDialogue() {
        startSceneDialogue("/dialogues/martino_dialogue.txt", DialogueKind.MARTINO);
    }

    private void onDialogueFinished() {
        DialogueKind completed = activeDialogueKind;
        activeDialogueKind = DialogueKind.NONE;
        dialogue.stop();
        updateArrowVisibility();
        showRoomInteractionHotspots();

        if (completed == DialogueKind.NPC && currentRoom == 6) {
            receiveItem(new Item("PIUMA_FOLGORE", "Piuma di Folgore", "La piuma donata da Huginn vibra di elettricità.",
                    "/images/chapter1/items/feather.png"));
        } else if (completed == DialogueKind.NPC && currentRoom == 1) {
            receiveItem(new Item("POLLINE_YGGDRASIL", "Polline di Yggdrasil", "Il polline dorato raccolto dal mozzo sul ponte.",
                    "/images/chapter1/items/pollen.png"));
        } else if (completed == DialogueKind.MARTINO && currentRoom == 2) {
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
