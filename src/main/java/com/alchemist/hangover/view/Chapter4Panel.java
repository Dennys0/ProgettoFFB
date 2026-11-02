package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.controller.AudioManager;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
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
            new Npc(1, new Rectangle(546, 301, 185, 128),
                    "Ascolta il Muspelsonar ferito", "/dialogues/ch4_bersercker_dialogue.txt"),
            new Npc(2, new Rectangle(386, 417, 486, 205),
                    "Parla con Sigrún, la Valchiria", "/dialogues/ch4_valkyrie_dialogue.txt"),
            new Npc(3, new Rectangle(822, 512, 149, 116),
                    "Esamina i resti del diavoletto", "/dialogues/ch4_diavoletto_dialogue.txt"),
            new Npc(4, new Rectangle(784, 280, 78, 99),
                    "Parla con il vagabondo", "/dialogues/ch4_oldman_dialogue.txt"),
            new Npc(5, new Rectangle(858, 380, 137, 125),
                    "Parla con il Figlio di Ivaldi", "/dialogues/ch4_blacksmith_dialogue.txt"),
            new Npc(6, new Rectangle(577, 99, 110, 150),
                    "Parla con Hela", "/dialogues/ch4_hela.txt")
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
        dialogues.addFittedPortrait("MUSPELSONAR", "/images/chapter4/npc/berserker.png");
        dialogues.addFittedPortrait("VALCHIRIA", "/images/chapter4/npc/valkyrie.png");
        dialogues.addFittedPortrait("SIGRUN", "/images/chapter4/npc/valkyrie.png");
        dialogues.addFittedPortrait("VAGABONDO", "/images/chapter4/npc/oldman.png");
        dialogues.addFittedPortrait("FIGLIO_IVALDI", "/images/chapter4/npc/blacksmith.png");

        dialogues.addFittedPortrait("HELA", "/images/shared/npc/hela_realms.png");

        for (Npc npc : NPCS) {
            addNpcArea(npc, npc.bounds());
            if (npc.room() == 3) addNpcArea(npc, new Rectangle(118, 520, 328, 137));
        }

        addItemWorldButton("CENERE_FENRIR", "/images/chapter4/items/fenrir_ash.png",
                218, 496, 74, 49, 4, "Cenere di Fenrir",
                "Polvere nera di lupo che assorbe la magia.");
        pulse = registerActionHotspot(new Rectangle(690, 490, 195, 112),
                "Ascolta il cuore di Muspelheim", () -> {
                    AudioManager.getInstance().playEffect(AudioManager.Effect.MAGMA_HEART);
                    ToastNotification.show(this, "Due battiti sotto il basalto. Al secondo, la roccia si incrina.");
                });
        shield = registerActionHotspot(new Rectangle(901, 564, 167, 91),
                "Esamina lo scudo della Valchiria", () ->
                StoryPromptDialog.message(mainFrameRef, StoryPromptDialog.Theme.FIRE,
                        "INCISIONI SULLO SCUDO",
                        "Il ghiaccio di Hel e il magma non devono mai essere mescolati."));
        cauldron = registerActionHotspot(new Rectangle(455, 272, 330, 285),
                "Usa il Calderone d'Ossidiana", this::openCauldron);
        stele = registerActionHotspot(new Rectangle(597, 375, 114, 231),
                "Leggi la stele delle fiamme", () ->
                StoryPromptDialog.message(mainFrameRef, StoryPromptDialog.Theme.FIRE,
                        "LA STELE DEI VENTI FOCOSI",
                        "Non colpire Surtur. Attira la lama sui due sigilli, poi al centro "
                        + "della fornace: la sua stessa spada libererà la Brace."));
        surtur = registerActionHotspot(new Rectangle(821, 267, 126, 115),
                "Affronta Surtur", () -> startSurtur(true));
        loadRoom(1);
    }

    private void addNpcArea(Npc npc, Rectangle bounds) {
        registerNpcHotspot(npc.room(), bounds, npc.label(),
                () -> startDialogue(npc.dialogue()));
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
        switch (currentRoom) {
            case 1 -> {
                if (StoryPromptDialog.confirm(mainFrameRef, StoryPromptDialog.Theme.FIRE,
                        "LA LEZIONE DEL BERSERKER", "Vuoi imparare come Muspelsonar attirava i colpi di Surtur?",
                        "ASCOLTA IL VETERANO", "PIÙ TARDI")) {
                    AudioManager.getInstance().playEffect(AudioManager.Effect.MAGMA_HEART);
                    StoryPromptDialog.message(mainFrameRef, StoryPromptDialog.Theme.FIRE, "IL PIEDE DEL GIGANTE",
                            "«Gli offrivo un bersaglio. Finché la punta mi seguiva, restavo sul sigillo. "
                            + "Quando piantava il piede, schivavo. Allora la sua lama rompeva la pietra al posto mio.»");
                }
            }
            case 4 -> receiveItem(new Item("ZOLFO_LOKI", "Zolfo Ingannatore di Loki",
                    "Un pizzico di zolfo dorato avvolto in un lembo di stoffa.", "/images/chapter4/items/sulfur.png"));
            case 5 -> receiveItem(new Item("MAGMA_SVARTALFAHEIMR", "Magma Filtrato di Svartálfaheimr",
                    "Magma passato attraverso il filtro del fabbro.", "/images/chapter4/items/magma.png"));
            case 6 -> receiveItem(new Item("LACRIMA_HEL", "Lacrima di Ghiaccio di Hel",
                    "Una goccia di gelo che il fuoco non riesce a sciogliere.", "/images/chapter4/items/ice_tear.png"));
        }
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
