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

public class Chapter1Panel extends AbstractChapterPanel {

    private JButton room7ScreenClickHotspot;

    // Elementi UI per il sistema di dialogo in-game
    private JPanel dialogueBoxOverlay;
    private JLabel dialogueNameLabel;
    private JLabel dialogueTextLabel;
    private JLabel dialogueSkipLabel;
    private JLabel characterSpriteLabel;
    private Image dialogueBoxImage;
    private DialogueSystem dialogueSystem;
    private Map<String, ImageIcon> characterSprites;

    // Hotspots
    private JButton martinoHotspot;
    private JButton cauldronHotspot;

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
        try {
            dialogueBoxImage = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/images/ui/dialogue_box.png"));
        } catch (Exception e) {
            System.err.println("Errore caricamento dialogue_box.png in Chapter1: " + e.getMessage());
        }

        characterSprites = new HashMap<>();
        loadSprite("MARTINO", "/images/characters/martino_realms.png");
        loadSprite("HELA", "/images/characters/hela_realms.png");
        loadSprite("SIEGFRIED", "/images/characters/siegfried_realms.png");
        loadSprite("GUNNAR", "/images/characters/gunnar_realms.png");

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
        room7ScreenClickHotspot = new JButton();
        room7ScreenClickHotspot.setBounds(150, 100, 980, 500); // Copre l'area centrale della Stanza 7
        room7ScreenClickHotspot.setContentAreaFilled(false);
        room7ScreenClickHotspot.setBorderPainted(false);
        room7ScreenClickHotspot.setFocusPainted(false);
        room7ScreenClickHotspot.setFocusable(false);
        room7ScreenClickHotspot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        room7ScreenClickHotspot.addActionListener(e -> triggerBossEncounter());
        add(room7ScreenClickHotspot);

// Carica lo sprite di HrÃ¦svelgr
        loadSprite("HRAESVELGR", "/images/characters/hraesvelgr.png");
        // Hotspot Martino al Timone (Stanza 2)
        martinoHotspot = new JButton();
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
        cauldronHotspot = new JButton();
        cauldronHotspot.setBounds(500, 350, 280, 250);
        cauldronHotspot.setContentAreaFilled(false);
        cauldronHotspot.setBorderPainted(false);
        cauldronHotspot.setFocusPainted(false);
        cauldronHotspot.setFocusable(false);
        cauldronHotspot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cauldronHotspot.setToolTipText("Usa il Calderone d'Ottone");
        cauldronHotspot.addActionListener(e -> interactWithCauldron());
        add(cauldronHotspot);

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

    private void interactWithCauldron() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Vuoi interagire con il Calderone d'Ottone e iniziare a mixare gli ingredienti?",
                "Calderone Alchemico", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
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

        dialogueBoxOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                if (dialogueBoxImage != null) {
                    g2d.drawImage(dialogueBoxImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2d.setColor(new Color(20, 20, 20, 230));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        dialogueBoxOverlay.setLayout(null);
        dialogueBoxOverlay.setOpaque(false);
        dialogueBoxOverlay.setBounds(40, 470, 1200, 200);
        dialogueBoxOverlay.setVisible(false);

        dialogueNameLabel = new JLabel("");
        dialogueNameLabel.setFont(new Font("Georgia", Font.BOLD, 24));
        dialogueNameLabel.setForeground(new Color(255, 215, 0));
        dialogueNameLabel.setBounds(65, 25, 500, 30);
        dialogueBoxOverlay.add(dialogueNameLabel);

        dialogueTextLabel = new JLabel("");
        dialogueTextLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        dialogueTextLabel.setForeground(Color.WHITE);
        dialogueTextLabel.setBounds(65, 60, 1070, 95);
        dialogueTextLabel.setVerticalAlignment(SwingConstants.TOP);
        dialogueBoxOverlay.add(dialogueTextLabel);

        dialogueSkipLabel = new JLabel("MOSTRA FRASE", SwingConstants.RIGHT);
        dialogueSkipLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        dialogueSkipLabel.setForeground(new Color(255, 218, 145));
        dialogueSkipLabel.setBounds(950, 155, 180, 25);
        dialogueBoxOverlay.add(dialogueSkipLabel);

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
        martinoHotspot.setVisible(currentRoom == 2);
        cauldronHotspot.setVisible(currentRoom == 4);
        room7ScreenClickHotspot.setVisible(currentRoom == 7);
    }

    @Override
    protected boolean canCollectWorldItem(String itemId, GameState state) {
        if ("RUGIADA_ASGARD".equals(itemId) && !state.hasDragonKey()) {
            JOptionPane.showMessageDialog(this,
                    "La polena a drago è bloccata da un meccanismo d'ottone. Serve la Chiave del Drago!",
                    "Serbatoio Bloccato", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected boolean shouldShowWorldItem(String itemId, GameState state) {
        return !("MAPPA_CIELO".equals(itemId) && state.hasGivenMapToMartino());
    }

    private void startMartinoDialogue() {
        setArrowsVisible(false);
        martinoHotspot.setVisible(false);
        dialogueBoxOverlay.setVisible(true);

        dialogueSystem.loadDialogue("/dialogues/martino_dialogue.txt");
        dialogueSystem.startDialogue();
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
    dialogueBoxOverlay.setVisible(false);
    characterSpriteLabel.setVisible(false);
    updateArrowVisibility();

    if (currentRoom == 2) {
        DialogueChoiceOverlay overlay = new DialogueChoiceOverlay(mainFrameRef, gaveMap -> {
            if (gaveMap) {
                System.out.println("Mappa consegnata! Ottenuta la Chiave del Drago.");
            }
            martinoHotspot.setVisible(true);
            hudPanel.updateHUD();
        });
        overlay.setVisible(true);
    } else if (currentRoom == 7 && !GameState.getInstance().isBossDefeated()) {
        // Avvia la Boss Fight subito dopo il dialogo!
        BossFightOverlay bossFight = new BossFightOverlay(mainFrameRef);
        bossFight.setVisible(true);
        
        // Al termine della boss fight, ripristina la visibilitÃ 
        room7ScreenClickHotspot.setVisible(true);
        hudPanel.updateHUD();
    }
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
        JOptionPane.showMessageDialog(this, "La tempesta si Ã¨ placata. HrÃ¦svelgr osserva il vascello in silenzio.", "Boss GiÃ  Placato", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    // Passa allo sfondo room7_boss2.png
    try {
        currentBgImage = new ImageIcon(getClass().getResource("/images/chapter1/room7_boss2.png")).getImage();
    } catch (Exception e) {
        System.err.println("Sfondo room7_boss2.png non trovato!");
    }
    repaint();

    // Avvia dialogo introduttivo del Boss
    setArrowsVisible(false);
    room7ScreenClickHotspot.setVisible(false);
    dialogueBoxOverlay.setVisible(true);

    dialogueSystem.loadDialogue("/dialogues/boss_intro_dialogue.txt");
    dialogueSystem.startDialogue();
}
}
