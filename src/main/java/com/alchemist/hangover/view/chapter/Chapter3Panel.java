package com.alchemist.hangover.view.chapter;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.gameplay.chapter.EarthChapterRules;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
import com.alchemist.hangover.view.ToastNotification;
import com.alchemist.hangover.view.boss.YmirBossOverlay;
import com.alchemist.hangover.view.dialogue.DialogueManager;
import com.alchemist.hangover.view.dialogue.StoryPromptDialog;
import com.alchemist.hangover.view.menu.PauseMenuDialog;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;

/**
 * Capitolo III - Terra: Il Dominio di Ymir.
 *
 * Navigazione, dialoghi cinematografici, rituale delle rune e alchimia.
 * Gunnar usa la stessa cornice visuale dei capitoli precedenti.
 */
public final class Chapter3Panel extends AbstractChapterPanel {
    private JButton ymirHotspot;
    private JButton gunnarHotspot;
    private JButton breathingCrackHotspot;
    private EarthBreathEffect breathingEffect;
    private JComponent crackArtwork;
    private int crackInspections;
    private boolean heardQuietGlade;
    private JButton steleHotspot;
    private JButton forgeHotspot;
    // Gunnar condivide la stessa sessione grafica degli altri dialoghi.
    private DialogueManager.SceneSession gunnarDialogue;
    private boolean gunnarConversationActive;
    private boolean gunnarRewardConversation;

    /** Hotspot invisibili: gli NPC sono gia' raffigurati nei PNG delle stanze. */
    record NpcDefinition(int room, Rectangle bounds, String action, String dialogue) { }
    static final List<NpcDefinition> NPC_DEFINITIONS = List.of(
            new NpcDefinition(3, new Rectangle(987, 363, 270, 205),
                    "Parla con lo sgherro", "/dialogues/ch3_caldron_revised.txt"),
            new NpcDefinition(4, new Rectangle(737, 293, 225, 175),
                    "Parla con il Vanir", "/dialogues/ch3_vanir_revised.txt"),
            new NpcDefinition(5, new Rectangle(389, 289, 185, 182),
                    "Parla con Billingr", "/dialogues/ch3_mining_revised.txt"),
            new NpcDefinition(6, new Rectangle(713, 373, 365, 178),
                    "Parla con Níðhöggr", "/dialogues/ch3_nighodd_revised.txt")
    );
    private DialogueManager.SceneSession npcDialogue;
    private boolean npcConversationActive;

    public Chapter3Panel(JFrame mainFrame) {
        initializeChapterBase(mainFrame, ChapterLayouts.forChapter(3));
        // Stanza 4: Foresta Pietrificata.
        addItemWorldButton("MUSCHIO_MIDGARD", "/images/chapter3/items/moss.png",
                184, 480, 66, 47, 4, "Muschio di Midgard",
                "Un lichene brillante che emana un profumo amarognolo di terra pulita.");
        // Stanza 5: Miniere Basaltiche.
        addItemWorldButton("FRAMMENTO_YGGDRASIL", "/images/chapter3/items/fragment_yggdrasil.png",
                303, 481, 62, 44, 5, "Frammento di Yggdrasil",
                "Una scheggia di legno millenario che emette una debole luce dorata.");
        // Il frammento sul terreno riflette meno luce rispetto all’icona nell’inventario.
        JButton fragment = itemButtons.get("FRAMMENTO_YGGDRASIL");
        ImageIcon source = (ImageIcon) fragment.getIcon();
        if (source != null) {
            java.awt.image.BufferedImage muted = new java.awt.image.BufferedImage(
                    source.getIconWidth(), source.getIconHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = muted.createGraphics();
            g.setComposite(AlphaComposite.SrcOver.derive(0.55f));
            source.paintIcon(fragment, g, 0, 0);
            g.dispose();
            fragment.setIcon(new ImageIcon(muted));
        }
        createBreathingCrackHotspot();
        createGunnarHotspot();
        initGunnarDialogue();
        initNpcDialogues();
        for (NpcDefinition npc : NPC_DEFINITIONS) createNpcHotspot(npc);
        createSteleHotspot();
        createForgeHotspot();
        createYmirHotspot();
        loadRoom(1);
    }

    private void createBreathingCrackHotspot() {
        // La crepa e' gia' disegnata nel terreno in room1_hub.png.
        breathingCrackHotspot = new HotspotButton();
        breathingCrackHotspot.setBounds(690, 522, 255, 102);
        breathingCrackHotspot.setToolTipText("Interagisci con la crepa");
        breathingCrackHotspot.getAccessibleContext().setAccessibleName(
                "Ascolta la crepa che respira nella Radura delle Radici");
        breathingCrackHotspot.addActionListener(e -> inspectBreathingCrack());
        add(breathingCrackHotspot);
        breathingCrackHotspot.setVisible(false);

        breathingEffect = new EarthBreathEffect();
        breathingEffect.setBounds(655, 506, 320, 140);
        add(breathingEffect);
        breathingEffect.setVisible(false);

        // Overlay artistico opzionale: quando crepa.png verra' pubblicato
        // sara' disegnato sulla roccia con opacita' tenue.
        java.net.URL crackResource = getClass().getResource("/images/ui/crepa.png");
        if (crackResource != null) {
            Image crack = new ImageIcon(crackResource).getImage();
            crackArtwork = new JComponent() {
                @Override
                public boolean contains(int x, int y) {
                    return false; // il click raggiunge sempre l'hotspot sottostante
                }
                @Override
                protected void paintComponent(Graphics graphics) {
                    super.paintComponent(graphics);
                    Graphics2D g = (Graphics2D) graphics.create();
                    try {
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g.setComposite(AlphaComposite.SrcOver.derive(0.40f));
                        g.drawImage(crack, 0, 0, getWidth(), getHeight(), this);
                    } finally {
                        g.dispose();
                    }
                }
            };
            crackArtwork.setBounds(655, 473, 320, 210);
            add(crackArtwork);
            crackArtwork.setVisible(false);
        }
    }

    private void inspectBreathingCrack() {
        if (currentRoom != 1) return;
        if (GameState.getInstance().isYmirPacified()) {
            ToastNotification.show(this, "Arthur appoggia il palmo alla crepa. Un battito lento, poi il silenzio.");
            return;
        }
        breathingEffect.startEffect();
        com.alchemist.hangover.controller.AudioManager.getInstance()
                .playEffect(com.alchemist.hangover.controller.AudioManager.Effect.EARTH_BREATH);
        String[] thoughts = {
                "Arthur: Il terreno sta respirando. Spero non sia allergico a me.",
                "Arthur: Questo non e' un terremoto... e' un battito cardiaco!",
                "Arthur: Se il mondo si gira nel sonno, finisco sotto al cuscino."
        };
        ToastNotification.show(this, thoughts[crackInspections % thoughts.length]);
        crackInspections++;
    }

    private void createGunnarHotspot() {
        // Gunnar e' gia' raffigurato seduto a sinistra in room2_rune.png.
        // L'area di clic e' trasparente: il ritratto va mostrato solo
        // nella finestra di dialogo GunnarDialogueDialog.
        // Coordinate in base alla risoluzione di gioco 1280x720.
        gunnarHotspot = new HotspotButton();
        gunnarHotspot.setBounds(143, 268, 139, 174);
        gunnarHotspot.setToolTipText("Parla con Gunnar");
        gunnarHotspot.getAccessibleContext().setAccessibleName("Parla con Gunnar");
        gunnarHotspot.addActionListener(e -> startGunnarDialogue());
        add(gunnarHotspot);
        gunnarHotspot.setVisible(false);
    }

    private void initGunnarDialogue() {
        gunnarDialogue = new DialogueManager.SceneSession(
                this, speaker -> new Rectangle(340, 30, 600, 620),
                () -> closeGunnarDialogue(true), false);
        gunnarDialogue.addPortrait("GUNNAR", "/images/shared/npc/gunnar_realms.png");
        // Come prima, Gunnar resta in primo piano durante l'intera conversazione.
        gunnarDialogue.setFixedPortrait("GUNNAR");
        gunnarDialogue.setWrapWidth(995);

        JButton interrupt = new JButton("INTERROMPI");
        interrupt.setBounds(980, 18, 160, 34);
        interrupt.setFont(new Font("SansSerif", Font.BOLD, 12));
        interrupt.setForeground(new Color(255, 225, 165));
        interrupt.setBackground(new Color(44, 35, 40));
        interrupt.setBorder(BorderFactory.createLineBorder(new Color(177, 135, 80)));
        interrupt.setFocusable(false);
        interrupt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        interrupt.setToolTipText("Interrompe Gunnar senza concedere gli indizi");
        interrupt.addActionListener(e -> closeGunnarDialogue(false));
        gunnarDialogue.dialogueBox().add(interrupt);

        InputMap inputs = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = getActionMap();
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "continueGunnar");
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "continueGunnar");
        actions.put("continueGunnar", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (gunnarConversationActive) gunnarDialogue.advance();
                else if (npcConversationActive) npcDialogue.advance();
            }
        });
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "interruptGunnar");
        actions.put("interruptGunnar", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (gunnarConversationActive) closeGunnarDialogue(false);
                else if (npcConversationActive) closeNpcDialogue();
                else new PauseMenuDialog(mainFrameRef).setVisible(true);
            }
        });
    }

    private void startGunnarDialogue() {
        if (gunnarConversationActive || npcConversationActive || currentRoom != 2) return;
        gunnarConversationActive = true;
        setArrowsVisible(false);
        gunnarHotspot.setVisible(false);
        GameState state = GameState.getInstance();
        gunnarRewardConversation = !state.hasLearnedGunnarWisdom()
                && state.hasHeardGunnarIntroduction();
        String resource = EarthChapterRules.gunnarDialoguePath(state);
        gunnarDialogue.start(resource);
        repaint();
    }

    private void closeGunnarDialogue(boolean completed) {
        if (!gunnarConversationActive) return;
        gunnarConversationActive = false;
        gunnarDialogue.stop();
        if (completed) {
            GameState state = GameState.getInstance();
            boolean firstMeeting = !state.hasHeardGunnarIntroduction()
                    && !state.hasLearnedGunnarWisdom();
            boolean rewarded = EarthChapterRules.completeGunnarStage(state, gunnarRewardConversation);
            if (firstMeeting) ToastNotification.show(this,
                    "Gunnar ha terminato il racconto. Torna a parlargli per la sua saggezza.");
            else if (rewarded) ToastNotification.show(this,
                    "Saggezza di Gunnar ottenuta: gli indizi sono nel Diario.");
        }
        gunnarRewardConversation = false;
        gunnarHotspot.setVisible(currentRoom == 2);
        updateArrowVisibility();
        hudPanel.updateHUD();
        repaint();
    }


    /**
     * Gli NPC del terzo capitolo usano lo stesso motore visuale di C1/C2.
     * Mantengono ritratti senza tagli e limiti di posizione sotto l'HUD.
     */
    private void initNpcDialogues() {
        // La gestione di SPAZIO/INVIO e' gia' condivisa con Gunnar qui sopra.
        npcDialogue = new DialogueManager.SceneSession(
                this, Chapter3Panel::portraitBoundsFor, this::finishNpcDialogue, false);
        npcDialogue.addFittedPortrait("SGHERRO", "/images/shared/npc/diavoletto_realms.png");
        npcDialogue.addFittedPortrait("VANIR", "/images/chapter3/npc/visir_or_vanir.png");
        npcDialogue.addFittedPortrait("BILLINGR", "/images/chapter3/npc/billingr.png");
        npcDialogue.addFittedPortrait("NIDHOGGR", "/images/chapter3/npc/nidhoggr.png");
    }

    /** Coordinate necessarie a lasciare sempre l'HUD libero. */
    static Rectangle portraitBoundsFor(String speaker) {
        if ("SGHERRO".equals(speaker)) return new Rectangle(15, 27, 570, 465);
        if ("NIDHOGGR".equals(speaker)) return new Rectangle(170, 28, 940, 457);
        return new Rectangle(265, 25, 750, 463);
    }

    private void createNpcHotspot(NpcDefinition npc) {
        registerNpcHotspot(npc.room(), npc.bounds(), npc.action(),
                () -> startNpcDialogue(npc));
    }

    private void updateNpcHotspots() {
        setRegisteredNpcHotspotsVisible(
                !gunnarConversationActive && !npcConversationActive
                );
    }

    private void startNpcDialogue(NpcDefinition npc) {
        if (gunnarConversationActive || npcConversationActive
                || currentRoom != npc.room()) return;
        npcConversationActive = true;
        setArrowsVisible(false);
        updateNpcHotspots();
        if (forgeHotspot != null) forgeHotspot.setVisible(false);
        if (steleHotspot != null) steleHotspot.setVisible(false);
        npcDialogue.start(npc.dialogue());
        com.alchemist.hangover.controller.AudioManager.getInstance()
                .playEffect(com.alchemist.hangover.controller.AudioManager.Effect.CLICK);
        repaint();
    }

    private void finishNpcDialogue() {
        if (npcConversationActive && currentRoom == 4) {
            receiveItem(new Item("RADICE_NIDHOGG", "Radice Nera di Nidhogg",
                    "Un pezzo di radice corrotta reciso dal Vanir.", "/images/chapter3/items/nidhogg.png"));
        }
        if (npcConversationActive && currentRoom == 5) {
            receiveItem(new Item("POLVERE_OSSA_GIGANTE",
                    "Polvere di Ossa di Gigante", "La polvere che Billingr usa per far presa sulle pietre.",
                    "/images/chapter3/items/bone.png"));
        }
        closeNpcDialogue();
    }

    private void closeNpcDialogue() {
        if (!npcConversationActive) return;
        npcConversationActive = false;
        npcDialogue.stop();
        updateArrowVisibility();
        if (forgeHotspot != null) forgeHotspot.setVisible(currentRoom == 3);
        if (steleHotspot != null) steleHotspot.setVisible(currentRoom == 6);
        updateNpcHotspots();
        repaint();
    }

    private void createSteleHotspot() {
        steleHotspot = new HotspotButton();
        // La pietra iscritta a sinistra diventa una zona cliccabile.
        steleHotspot.setBounds(150, 238, 175, 235);
        steleHotspot.setToolTipText("Leggi la stele runica");
        steleHotspot.getAccessibleContext().setAccessibleName(
                "Leggi la stele dei tre versi sulla Rupe dell'Eco");
        steleHotspot.addActionListener(e ->
                new RuneSteleDialog(mainFrameRef).setVisible(true));
        add(steleHotspot);
        steleHotspot.setVisible(false);
    }

    private void createForgeHotspot() {
        forgeHotspot = registerActionHotspot(new Rectangle(721, 384, 184, 132),
                "Apri il calderone della Forgia", this::openForge);
    }

    /** I tre capitoli aprono esattamente lo stesso calderone, con la ricetta
     * scelta da RecipeManager. Il dialogo modale impedisce click sul mondo. */
    private void openForge() {
        if (npcConversationActive || gunnarConversationActive) return;
        showCauldron();
    }

    private void createYmirHotspot() {
        // Nella nuova room7_boss.png la sagoma scura e' gia' parte
        // del fondale: NON sovrapporre il PNG del boss a questa stanza.
        // Il sprite completo boss_ymir.png resta nel combattimento.
        ymirHotspot = new HotspotButton();
        // Area provvisoria al centro della scena: verra' affinata
        // controllando il fondale aggiornato su GitHub.
        ymirHotspot.setBounds(445, 175, 390, 415);
        ymirHotspot.setToolTipText("Parla con Ymir");
        ymirHotspot.getAccessibleContext().setAccessibleName(
                "Parla con Ymir: scegli se iniziare il combattimento");
        ymirHotspot.addActionListener(e -> triggerYmirEncounter(true));
        add(ymirHotspot);
        ymirHotspot.setVisible(false);
    }

    private void triggerYmirEncounter(boolean askConfirmation) {
        GameState state = GameState.getInstance();
        if (state.isYmirPacified()) return;

        // Gli avvisi e la conferma usano la stessa finestra fantasy
        // dei boss dei capitoli precedenti, con tema Terra.
        if (!state.hasLearnedGunnarWisdom() || !state.hasReadYmirStele()) {
            StoryPromptDialog.message(mainFrameRef, StoryPromptDialog.Theme.EARTH,
                    "IL SONNO DI YMIR",
                    EarthChapterRules.missingYmirClues(state.hasLearnedGunnarWisdom(),
                            state.hasReadYmirStele()));
            return;
        }
        if (askConfirmation && !StoryPromptDialog.confirm(mainFrameRef,
                StoryPromptDialog.Theme.EARTH, "IL CUORE DI ROCCIA — YMIR",
                "Il gigante dorme nel cuore della montagna. "
                + "Vuoi tentare il rituale del sonno prima che si risvegli?",
                "AFFRONTA YMIR", "NON ANCORA")) return;
        setArrowsVisible(false);
        ymirHotspot.setVisible(false);
        new YmirBossOverlay(mainFrameRef).setVisible(true);
        if ("CHAPTER3".equals(SceneManager.getInstance().getCurrentSceneName())) {
            loadRoom(7);
        }
    }

    /** Il checkpoint ricomincia il rituale, senza rigenerare i versi della stele. */
    public void retryBossEncounter() {
        if (GameState.getInstance().isYmirPacified()) return;
        loadRoom(7);
        // Il retry e' gia' una scelta esplicita: non richiede altra conferma.
        triggerYmirEncounter(false);
    }

    @Override
    protected void onRoomChanged() {
        if (!GameState.getInstance().isYmirPacified()) heardQuietGlade = false;
        else if (currentRoom == 1 && !heardQuietGlade) {
            heardQuietGlade = true;
            ToastNotification.show(this, "Arthur: Adesso riesco a sentire i miei passi.");
        }
        if (gunnarConversationActive) closeGunnarDialogue(false);
        if (npcConversationActive) closeNpcDialogue();
        if (breathingEffect != null && currentRoom != 1) breathingEffect.stopEffect();
        if (crackArtwork != null) {
            crackArtwork.setVisible(currentRoom == 1);
            if (currentRoom == 1) setComponentZOrder(crackArtwork, 0);
        }
        if (breathingCrackHotspot != null) {
            breathingCrackHotspot.setVisible(currentRoom == 1);
            if (currentRoom == 1) setComponentZOrder(breathingCrackHotspot, 0);
        }
        if (gunnarHotspot != null) {
            gunnarHotspot.setVisible(currentRoom == 2);
            if (currentRoom == 2) setComponentZOrder(gunnarHotspot, 0);
        }
        if (steleHotspot != null) {
            steleHotspot.setVisible(currentRoom == 6);
            if (currentRoom == 6) setComponentZOrder(steleHotspot, 0);
        }
        if (forgeHotspot != null) {
            forgeHotspot.setVisible(currentRoom == 3);
            if (currentRoom == 3) setComponentZOrder(forgeHotspot, 0);
        }
        // Dopo la vittoria il gigante si placa e appare la sua lacrima.
        // Lo sprite compare solo nella stanza 7, senza coprire l'HUD.
        if (ymirHotspot != null) {
            boolean visible = currentRoom == 7 && !GameState.getInstance().isYmirPacified();
            ymirHotspot.setVisible(visible);
            if (visible) setComponentZOrder(ymirHotspot, 0);
        }
        updateNpcHotspots();
    }
}
