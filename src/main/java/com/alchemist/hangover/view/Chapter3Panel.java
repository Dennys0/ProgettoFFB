package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.controller.DialogueSystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;

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
    private YmirCauldronPanel brewingPanel;
    // La cornice originale e lo sprite si aprono NELLA stanza,
    // come avviene nei primi due capitoli.
    private SharedDialogueBox gunnarDialogueBox;
    private JLabel gunnarDialogueSprite;
    private DialogueSystem gunnarDialogueSystem;
    private boolean gunnarConversationActive;

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

        gunnarDialogueBox.getRevealLabel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gunnarConversationActive) gunnarDialogueSystem.revealCurrentLine();
            }
        });

        MouseAdapter advance = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gunnarConversationActive) gunnarDialogueSystem.nextLine();
            }
        };
        gunnarDialogueSprite.addMouseListener(advance);
        gunnarDialogueBox.addMouseListener(advance);
        gunnarDialogueBox.getSpeakerLabel().addMouseListener(advance);
        gunnarDialogueBox.getTextLabel().addMouseListener(advance);

        InputMap inputs = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = getActionMap();
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "continueGunnar");
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "continueGunnar");
        actions.put("continueGunnar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (gunnarConversationActive) gunnarDialogueSystem.nextLine();
            }
        });
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "interruptGunnar");
        actions.put("interruptGunnar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (gunnarConversationActive) closeGunnarDialogue(false);
                else new PauseMenuDialog(mainFrameRef).setVisible(true);
            }
        });
    }

    private void startGunnarDialogue() {
        if (gunnarConversationActive || currentRoom != 2) return;
        gunnarConversationActive = true;
        setArrowsVisible(false);
        gunnarHotspot.setVisible(false);
        gunnarDialogueSprite.setVisible(true);
        gunnarDialogueBox.setVisible(true);
        setComponentZOrder(gunnarDialogueBox, 0);
        setComponentZOrder(gunnarDialogueSprite, 1);
        gunnarDialogueSystem.loadDialogue("/dialogues/ch3_gunnar.txt");
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
            boolean firstTime = !GameState.getInstance().hasLearnedGunnarWisdom();
            GameState.getInstance().setGunnarWisdomLearned(true);
            if (firstTime) ToastNotification.show(this,
                    "Hai compreso il linguaggio delle rune: indizi nel Diario.");
        }
        gunnarHotspot.setVisible(currentRoom == 2);
        updateArrowVisibility();
        hudPanel.updateHUD();
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

        brewingPanel = new YmirCauldronPanel(this::closeForge,
                this::finishYmirDrink, this::onBrewingDisaster);
        brewingPanel.setBounds(220, 90, YmirCauldronPanel.WIDTH, YmirCauldronPanel.HEIGHT);
        add(brewingPanel);
        brewingPanel.setVisible(false);
    }

    private void openForge() {
        brewingPanel.refreshIngredients();
        brewingPanel.setVisible(true);
        forgeHotspot.setVisible(false);
        setArrowsVisible(false);
        setComponentZOrder(brewingPanel, 0);
        brewingPanel.requestFocusInWindow();
        repaint();
    }

    private void closeForge() {
        brewingPanel.setVisible(false);
        updateArrowVisibility();
        forgeHotspot.setVisible(currentRoom == 3);
        repaint();
    }

    private void onBrewingDisaster(String message) {
        closeForge();
        GameState state = GameState.getInstance();
        state.setCurrentHealth(0);
        state.setBossRetryAvailable(false);
        GameOverPanel gameOver = (GameOverPanel) SceneManager.getInstance().getPanel("GAMEOVER");
        if (gameOver != null) gameOver.showCauldronFailure(message);
        SceneManager.getInstance().showScene("GAMEOVER");
    }

    /** Riprende esattamente dal boccale, senza ripetere il boss o perdere gli ingredienti. */
    public void retryCauldronAfterFailure() {
        loadRoom(3);
        openForge();
    }

    private void finishYmirDrink() {
        closeForge();
        GameState.getInstance().setYmirOldFashionedCreated(true);
        com.alchemist.hangover.controller.AudioManager.getInstance()
                .playEffect(com.alchemist.hangover.controller.AudioManager.Effect.SUCCESS);
        SceneManager.getInstance().showScene("CHAPTER3_COMPLETE");
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

        if (askConfirmation) {
            String[] options = {"AFFRONTA YMIR", "NON ANCORA"};
            int choice = JOptionPane.showOptionDialog(mainFrameRef,
                    "Un'ombra gigantesca siede immobile tra le rocce.\n"
                    + "Un solo movimento potrebbe risvegliare il mondo intero.\n\n"
                    + "Vuoi affrontare Ymir e tentare il rituale del sonno?",
                    "Il Cuore di Roccia — Ymir",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[1]);
            if (choice != 0) return;
        }
        if (!state.hasLearnedGunnarWisdom() || !state.hasReadYmirStele()) {
            JOptionPane.showMessageDialog(mainFrameRef,
                    "Il gigante si muove e la roccia trema sotto i tuoi piedi.\n\n"
                    + (!state.hasLearnedGunnarWisdom()
                        ? "Devi ascoltare Gunnar fino alla fine nel Cerchio delle Rune.\n" : "")
                    + (!state.hasReadYmirStele()
                        ? "Devi leggere la stele della Rupe dell'Eco.\n" : "")
                    + "\nRaccogli gli indizi prima di iniziare il rituale.",
                    "Il sonno di Ymir non puo' essere spezzato",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
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
        if (brewingPanel != null) brewingPanel.setVisible(false);
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
    }
}
