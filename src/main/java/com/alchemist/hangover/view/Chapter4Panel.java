package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.view.boss.SurturBossOverlay;
import com.alchemist.hangover.view.cauldron.CauldronController;
import com.alchemist.hangover.view.cauldron.CauldronOverlay;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Capitolo IV: Muspelheim. Riutilizza navigazione, inventario e dialoghi
 * dei primi tre capitoli; aggiunge soltanto le interazioni del regno di fuoco.
 */
public final class Chapter4Panel extends AbstractChapterPanel {

    record Npc(int room, Rectangle bounds, String label, String dialogue) { }

    static final List<Npc> NPCS = List.of(
            new Npc(1, new Rectangle(455, 255, 285, 310),
                    "Ascolta il Muspelsonar ferito", "/dialogues/ch4_bersercker_dialogue.txt"),
            new Npc(2, new Rectangle(465, 300, 280, 260),
                    "Parla con Sigrún, la Valchiria", "/dialogues/ch4_valkyrie_dialogue.txt"),
            new Npc(3, new Rectangle(920, 505, 160, 145),
                    "Esamina i resti del diavoletto", "/dialogues/ch4_diavoletto_dialogue.txt"),
            new Npc(4, new Rectangle(738, 330, 190, 265),
                    "Parla con il vagabondo", "/dialogues/ch4_oldman_dialogue.txt"),
            new Npc(5, new Rectangle(465, 305, 230, 245),
                    "Parla con il Figlio di Ivaldi", "/dialogues/ch4_blacksmith_dialogue.txt")
    );

    private final DialogueManager.SceneSession dialogues;
    private final HotspotButton cauldron;
    private final HotspotButton stele;
    private final HotspotButton shield;
    private final HotspotButton surtur;
    private final HotspotButton pulse;

    public Chapter4Panel(JFrame owner) {
        initializeChapterBase(owner, ChapterLayouts.forChapter(4));
        dialogues = new DialogueManager.SceneSession(this,
                speaker -> new Rectangle(245, 20, 790, 465), this::endDialogue);
        dialogues.addFittedPortrait("MUSPELSONAR", "/images/characters/ch4_berserker.png");
        dialogues.addFittedPortrait("VALCHIRIA", "/images/characters/ch4_valkyrie.png");
        dialogues.addFittedPortrait("SIGRUN", "/images/characters/ch4_valkyrie.png");
        dialogues.addFittedPortrait("VAGABONDO", "/images/characters/ch4_oldman.png");
        dialogues.addFittedPortrait("FIGLIO_IVALDI", "/images/characters/ch4_blacksmith.png");

        for (Npc npc : NPCS) {
            registerNpcHotspot(npc.room(), npc.bounds(), npc.label(),
                    () -> startDialogue(npc.dialogue()));
        }

        addItemWorldButton("CENERE_FENRIR", "/images/items/item_fenrir_ash.png",
                295, 490, 150, 105, 4, "Cenere di Fenrir",
                "Polvere nera di lupo che assorbe la magia.");
        addItemWorldButton("ZOLFO_LOKI", "/images/items/item_sulfur.png",
                970, 490, 145, 100, 4, "Zolfo Ingannatore di Loki",
                "Una polvere dorata e luminosa, insidiosamente invitante.");
        addItemWorldButton("MAGMA_SVARTALFAHEIMR", "/images/items/item_magma.png",
                930, 475, 140, 113, 5, "Magma Filtrato di Svartálfaheimr",
                "La base liquida incandescente del Ragnarök Flambé.");
        addItemWorldButton("LACRIMA_HEL", "/images/items/item_ice_tear.png",
                215, 480, 135, 110, 5, "Lacrima di Ghiaccio di Hel",
                "Un cristallo gelido che non va mai mischiato al magma.");

        pulse = registerActionHotspot(new Rectangle(690, 490, 195, 112),
                "Ascolta il cuore di Muspelheim", () ->
                ToastNotification.show(this,
                        "Arthur: Il pavimento batte come un cuore. Non è rassicurante."));
        shield = registerActionHotspot(new Rectangle(230, 300, 185, 190),
                "Esamina lo scudo della Valchiria", () ->
                StoryPromptDialog.message(mainFrameRef, StoryPromptDialog.Theme.FIRE,
                        "INCISIONI SULLO SCUDO",
                        "Il ghiaccio di Hel e il magma non devono mai essere mescolati."));
        cauldron = registerActionHotspot(new Rectangle(455, 272, 330, 285),
                "Usa il Calderone d'Ossidiana", this::openCauldron);
        stele = registerActionHotspot(new Rectangle(500, 240, 280, 300),
                "Leggi la stele delle fiamme", () ->
                StoryPromptDialog.message(mainFrameRef, StoryPromptDialog.Theme.FIRE,
                        "LA STELE DEI VENTI FOCOSI",
                        "Non colpire Surtur. Schiva i fendenti, poi attiralo al centro "
                        + "della fornace: la sua stessa spada libererà la Brace."));
        surtur = registerActionHotspot(new Rectangle(380, 175, 485, 390),
                "Affronta Surtur", () -> startSurtur(true));
        loadRoom(1);
    }

    private void startDialogue(String resource) {
        if (dialogues.isVisible()) return;
        setArrowsVisible(false);
        setRegisteredNpcHotspotsVisible(false);
        setExtrasVisible(false);
        dialogues.start(resource);
    }

    private void endDialogue() {
        dialogues.stop();
        updateArrowVisibility();
        onRoomChanged();
    }

    private void openCauldron() {
        GameState state = GameState.getInstance();
        if (state.isRagnarokFlambeCreated()) {
            EpiloguePanel finale = (EpiloguePanel) SceneManager.getInstance().getPanel("EPILOGUE");
            if (finale != null) finale.restart();
            SceneManager.getInstance().showScene("EPILOGUE");
            return;
        }
        if (!CauldronController.allowOpening(mainFrameRef)) return;
        new CauldronOverlay(mainFrameRef).setVisible(true);
    }

    private void startSurtur(boolean confirm) {
        GameState state = GameState.getInstance();
        if (state.isBossDefeated()) {
            ToastNotification.show(this, "Surtur è già stato sconfitto. Porta la Brace al calderone!");
            return;
        }
        if (confirm && !StoryPromptDialog.confirm(mainFrameRef, StoryPromptDialog.Theme.FIRE,
                "LA FORNACE DI SURTUR",
                "La spada del gigante può essere la chiave per ottenere la Brace. "
                + "Vuoi affrontarlo?",
                "AFFRONTA SURTUR", "NON ANCORA")) return;
        new SurturBossOverlay(mainFrameRef).setVisible(true);
        loadRoom(7);
    }

    @Override public void retryBossEncounter() {
        loadRoom(7);
        startSurtur(false);
    }

    private void setExtrasVisible(boolean visible) {
        pulse.setVisible(visible && currentRoom == 1);
        shield.setVisible(visible && currentRoom == 2);
        cauldron.setVisible(visible && currentRoom == 3);
        stele.setVisible(visible && currentRoom == 6);
        surtur.setVisible(visible && currentRoom == 7);
        if (visible) {
            for (JButton button : new JButton[]{pulse, shield, cauldron, stele, surtur})
                if (button.isVisible()) setComponentZOrder(button, 0);
        }
    }

    @Override protected void onRoomChanged() {
        if (dialogues == null) return;
        if (dialogues.isVisible()) dialogues.stop();
        setRegisteredNpcHotspotsVisible(true);
        setExtrasVisible(true);
    }
}
