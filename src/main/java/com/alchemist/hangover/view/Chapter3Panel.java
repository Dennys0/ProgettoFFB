package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.controller.DialogueSystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private JButton steleHotspot;
    private JButton forgeHotspot;
    // La cornice originale e lo sprite si aprono NELLA stanza,
    // come avviene nei primi due capitoli.
    private SharedDialogueBox gunnarDialogueBox;
    private JLabel gunnarDialogueSprite;
    private DialogueSystem gunnarDialogueSystem;
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
    private final Map<String, ImageIcon> npcPortraits = new HashMap<>();
    private SharedDialogueBox npcDialogueBox;
    private JLabel npcDialogueSprite;
    private DialogueSystem npcDialogueSystem;
    private boolean npcConversationActive;

    public Chapter3Panel(JFrame mainFrame) {
        initializeChapterBase(mainFrame,
                Chapter3World.backgrounds(),
                Chapter3World.exits());
        // Stanza 4: Foresta Pietrificata.
        addItemWorldButton("MUSCHIO_MIDGARD", "/images/items/item_moss.png",
                350, 445, 132, 98, 4, "Muschio di Midgard",
                "Un lichene brillante che emana un profumo amarognolo di terra pulita.");
        addItemWorldButton("RADICE_NIDHOGG", "/images/items/item_nidhogg.png",
                858, 460, 135, 110, 4, "Radice Nera di Nidhogg",
                "Una radice nerastra e pulsante, attraversata da vene cremisi.");

        // Stanza 5: Miniere Basaltiche.
        addItemWorldButton("FRAMMENTO_YGGDRASIL", "/images/items/item_fragment_yggdrasil.png",
                370, 485, 125, 90, 5, "Frammento di Yggdrasil",
                "Una scheggia di legno millenario che emette una debole luce dorata.");
        addItemWorldButton("POLVERE_OSSA_GIGANTE", "/images/items/item_bone.png",
                830, 465, 140, 100, 5, "Polvere di Ossa di Gigante",
                "Una polvere biancastra di ossa antiche, straordinariamente pesante.");

        // La Lacrima di Ymir e' disponibile solo dopo il rituale del boss.
        // Non viene aggiunta automaticamente dall'anteprima dello scontro.
        addItemWorldButton("LACRIMA_YMIR", "/images/items/item_ymircry.png",
                575, 480, 125, 108, 7, "Lacrima di Ymir",
                "Una lacrima cristallina del gigante primordiale, intrisa della memoria della terra.");

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
        gunnarDialogueSprite = new JLabel("", SwingConstants.CENTER);
        gunnarDialogueSprite.setBounds(340, 30, 600, 620);
        gunnarDialogueSprite.setOpaque(false);
        java.net.URL spriteUrl = getClass().getResource("/images/characters/gunnar_realms.png");
        if (spriteUrl != null) {
            Image sprite = new ImageIcon(spriteUrl).getImage()
                    .getScaledInstance(-1, 520, Image.SCALE_SMOOTH);
            gunnarDialogueSprite.setIcon(new ImageIcon(sprite));
        } else {
            gunnarDialogueSprite.setText("GUNNAR");
            gunnarDialogueSprite.setFont(new Font("Georgia", Font.BOLD, 32));
            gunnarDialogueSprite.setForeground(new Color(255, 215, 160));
        }
        add(gunnarDialogueSprite);
        gunnarDialogueSprite.setVisible(false);

        gunnarDialogueBox = new SharedDialogueBox();
        gunnarDialogueBox.setBounds(40, 470, 1200, 200);
        gunnarDialogueBox.setVisible(false);
        add(gunnarDialogueBox);
        // Zero significa davanti in Swing; il testo copre la parte
        // inferiore dello sprite, come nei capitoli precedenti.
        setComponentZOrder(gunnarDialogueBox, 0);
        setComponentZOrder(gunnarDialogueSprite, 1);

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
        gunnarDialogueBox.add(interrupt);

        gunnarDialogueSystem = new DialogueSystem(
                gunnarDialogueBox.getSpeakerLabel(),
                gunnarDialogueBox.getTextLabel(),
                null,
                () -> closeGunnarDialogue(true)
        );
        gunnarDialogueSystem.setWrapWidth(995);

        DialogueManager.connect(gunnarDialogueBox, gunnarDialogueSprite,
                gunnarDialogueSystem, () -> gunnarConversationActive);

        InputMap inputs = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = getActionMap();
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "continueGunnar");
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "continueGunnar");
        actions.put("continueGunnar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (gunnarConversationActive) gunnarDialogueSystem.nextLine();
                else if (npcConversationActive) npcDialogueSystem.nextLine();
            }
        });
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "interruptGunnar");
        actions.put("interruptGunnar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (gunnarConversationActive) closeGunnarDialogue(false);
                else if (npcConversationActive) closeNpcDialogue();
                else new PauseMenuDialog(mainFrameRef).setVisible(true);
            }
        });
    }

    /** Stage choice stays independent from visual rendering and can be tested headlessly. */
    static String gunnarDialoguePath(GameState state) {
        if (state.hasLearnedGunnarWisdom()) return "/dialogues/ch3_gunnar_repeat.txt";
        return state.hasHeardGunnarIntroduction()
                ? "/dialogues/ch3_gunnar_reward.txt" : "/dialogues/ch3_gunnar.txt";
    }

    /** Invoked only after the final line, never on ESC or a room change. */
    static boolean completeGunnarStage(GameState state, boolean rewardConversation) {
        if (state.hasLearnedGunnarWisdom()) return false;
        if (!state.hasHeardGunnarIntroduction()) {
            state.setGunnarIntroductionHeard(true);
            return false;
        }
        if (rewardConversation) {
            state.setGunnarWisdomLearned(true);
            return true;
        }
        return false;
    }

    private void startGunnarDialogue() {
        if (gunnarConversationActive || npcConversationActive || currentRoom != 2) return;
        gunnarConversationActive = true;
        setArrowsVisible(false);
        gunnarHotspot.setVisible(false);
        gunnarDialogueSprite.setVisible(true);
        gunnarDialogueBox.setVisible(true);
        setComponentZOrder(gunnarDialogueBox, 0);
        setComponentZOrder(gunnarDialogueSprite, 1);
        GameState state = GameState.getInstance();
        gunnarRewardConversation = !state.hasLearnedGunnarWisdom()
                && state.hasHeardGunnarIntroduction();
        String resource = gunnarDialoguePath(state);
        gunnarDialogueSystem.loadDialogue(resource);
        gunnarDialogueSystem.startDialogue();
        repaint();
    }

    private void closeGunnarDialogue(boolean completed) {
        if (!gunnarConversationActive) return;
        gunnarConversationActive = false;
        gunnarDialogueSystem.stop();
        gunnarDialogueBox.setVisible(false);
        gunnarDialogueSprite.setVisible(false);
        if (completed) {
            GameState state = GameState.getInstance();
            boolean firstMeeting = !state.hasHeardGunnarIntroduction()
                    && !state.hasLearnedGunnarWisdom();
            boolean rewarded = completeGunnarStage(state, gunnarRewardConversation);
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
     * Condivisione della cornice cinematografica con Gunnar.
     * Gli NPC non attivano il boss e non modificano gli indizi di Gunnar.
     */
    private void initNpcDialogues() {
        npcPortraits.put("SGHERRO", loadNpcPortrait("/images/characters/diavoletto_realms.png"));
        npcPortraits.put("VANIR", loadNpcPortrait("/images/chapter3/npc/visir_or_vanir.png"));
        npcPortraits.put("BILLINGR", loadNpcPortrait("/images/chapter3/npc/billingr.png"));
        npcPortraits.put("NIDHOGGR", loadNpcPortrait("/images/chapter3/npc/nidhoggr.png"));

        npcDialogueSprite = new JLabel("", SwingConstants.CENTER);
        npcDialogueSprite.setOpaque(false);
        npcDialogueSprite.setBounds(220, 18, 840, 486);
        add(npcDialogueSprite);
        npcDialogueSprite.setVisible(false);

        npcDialogueBox = new SharedDialogueBox();
        npcDialogueBox.setBounds(40, 470, 1200, 200);
        npcDialogueBox.setVisible(false);
        add(npcDialogueBox);
        setComponentZOrder(npcDialogueBox, 0);
        setComponentZOrder(npcDialogueSprite, 1);

        npcDialogueSystem = new DialogueSystem(
                npcDialogueBox.getSpeakerLabel(),
                npcDialogueBox.getTextLabel(),
                this::updateNpcPortrait,
                this::closeNpcDialogue);
        npcDialogueSystem.setWrapWidth(995);
        DialogueManager.connect(npcDialogueBox, npcDialogueSprite,
                npcDialogueSystem, () -> npcConversationActive);
    }

    private static ImageIcon loadNpcPortrait(String resource) {
        java.net.URL url = Chapter3Panel.class.getResource(resource);
        return url == null ? null : new ImageIcon(url);
    }

    /**
     * Mantiene le proporzioni e non occupa mai l'HUD. Lo sprite emerge
     * soltanto quando il personaggio sta parlando.
     */
    static Rectangle portraitBoundsFor(String speaker) {
        if ("SGHERRO".equals(speaker)) return new Rectangle(15, 27, 570, 465);
        if ("NIDHOGGR".equals(speaker)) return new Rectangle(170, 28, 940, 457);
        return new Rectangle(265, 25, 750, 463);
    }

    private void updateNpcPortrait() {
        DialogueSystem.DialogueLine line = npcDialogueSystem.getCurrentLine();
        String speaker = line == null ? "" : line.getSpeaker();
        ImageIcon original = npcPortraits.get(speaker);
        npcDialogueSprite.setVisible(npcConversationActive && original != null);
        if (original == null) {
            npcDialogueSprite.setIcon(null);
            return;
        }
        Rectangle bounds = portraitBoundsFor(speaker);
        npcDialogueSprite.setBounds(bounds);
        double scale = Math.min((double) bounds.width / Math.max(1, original.getIconWidth()),
                (double) bounds.height / Math.max(1, original.getIconHeight()));
        int w = Math.max(1, (int) Math.round(original.getIconWidth() * scale));
        int h = Math.max(1, (int) Math.round(original.getIconHeight() * scale));
        npcDialogueSprite.setIcon(new ImageIcon(original.getImage()
                .getScaledInstance(w, h, Image.SCALE_SMOOTH)));
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
        npcDialogueBox.setVisible(true);
        setComponentZOrder(npcDialogueBox, 0);
        setComponentZOrder(npcDialogueSprite, 1);
        npcDialogueSystem.loadDialogue(npc.dialogue());
        npcDialogueSystem.startDialogue();
        com.alchemist.hangover.controller.AudioManager.getInstance()
                .playEffect(com.alchemist.hangover.controller.AudioManager.Effect.CLICK);
        repaint();
    }

    private void closeNpcDialogue() {
        if (!npcConversationActive) return;
        npcConversationActive = false;
        npcDialogueSystem.stop();
        npcDialogueBox.setVisible(false);
        npcDialogueSprite.setVisible(false);
        npcDialogueSprite.setIcon(null);
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
        forgeHotspot = new HotspotButton();
        forgeHotspot.setBounds(548, 282, 235, 260);
        forgeHotspot.setToolTipText("Interagisci con il calderone");
        forgeHotspot.getAccessibleContext().setAccessibleName("Apri il boccale di pietra");
        forgeHotspot.addActionListener(e -> openForge());
        add(forgeHotspot);
        forgeHotspot.setVisible(false);
    }

    /** I tre capitoli aprono esattamente lo stesso calderone, con la ricetta
     * scelta da RecipeManager. Il dialogo modale impedisce click sul mondo. */
    private void openForge() {
        if (npcConversationActive || gunnarConversationActive) return;
        if (!CauldronController.allowOpening(mainFrameRef)) return;
        new CauldronOverlay(mainFrameRef).setVisible(true);
    }

    /** Checkpoint: non consuma oggetti, rientra nella stanza e riapre la ricetta. */
    public void retryCauldronAfterFailure() {
        loadRoom(3);
        openForge();
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

    /** Indica esattamente gli indizi mancanti, senza testi incorporati nei PNG. */
    static String missingYmirClues(boolean hasGunnarWisdom, boolean hasStele) {
        StringBuilder message = new StringBuilder(
                "Il gigante si muove sotto la pietra. Per placarlo devi ancora:");
        if (!hasGunnarWisdom) message.append(
                "\n• Comprendere la saggezza di Gunnar nel Cerchio delle Rune.");
        if (!hasStele) message.append(
                "\n• Leggere la stele della Rupe dell'Eco.");
        return message.toString();
    }

    private void triggerYmirEncounter(boolean askConfirmation) {
        GameState state = GameState.getInstance();
        if (state.isYmirPacified()) return;

        // Gli avvisi e la conferma usano la stessa finestra fantasy
        // dei boss dei capitoli precedenti, con tema Terra.
        if (!state.hasLearnedGunnarWisdom() || !state.hasReadYmirStele()) {
            StoryPromptDialog.message(mainFrameRef, StoryPromptDialog.Theme.EARTH,
                    "IL SONNO DI YMIR",
                    missingYmirClues(state.hasLearnedGunnarWisdom(),
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
            loadRoom(7); // Mostra la Lacrima dopo la vittoria.
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
    protected boolean shouldShowWorldItem(String itemId, GameState state) {
        return !"LACRIMA_YMIR".equals(itemId) || state.isYmirPacified();
    }

    @Override
    protected boolean canCollectWorldItem(String itemId, GameState state) {
        return !"LACRIMA_YMIR".equals(itemId) || state.isYmirPacified();
    }

    @Override
    protected void onRoomChanged() {
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
