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

public class Chapter1Panel extends JPanel {

    private int currentRoom = 1;
    private HUDPanel hudPanel;
    private Image currentBgImage;
    private JButton room7ScreenClickHotspot;
    // Frecce UI
    private JButton btnUp, btnDown, btnLeft, btnRight;
    private Map<Integer, String> roomBgPaths;

    // Mappa per i pulsanti degli oggetti nel mondo di gioco
    private Map<String, JButton> itemButtons = new HashMap<>();

    // Elementi UI per il sistema di dialogo in-game
    private JPanel dialogueBoxOverlay;
    private JLabel dialogueNameLabel;
    private JLabel dialogueTextLabel;
    private JLabel dialogueSkipLabel;
    private JLabel characterSpriteLabel;
    private Image dialogueBoxImage;
    private DialogueSystem dialogueSystem;
    private Map<String, ImageIcon> characterSprites;
    private JFrame mainFrameRef;

    // Hotspots
    private JButton martinoHotspot;
    private JButton cauldronHotspot;

    public Chapter1Panel(JFrame mainFrame) {
        this.mainFrameRef = mainFrame;
        setLayout(null);
        setPreferredSize(new Dimension(1280, 720));

        // 1. Mappatura Sfondi
        roomBgPaths = new HashMap<>();
        roomBgPaths.put(1, "/images/chapter1/room1_deck.png");
        roomBgPaths.put(2, "/images/chapter1/room2_helm.png");
        roomBgPaths.put(3, "/images/chapter1/room3_prow.png");
        roomBgPaths.put(4, "/images/chapter1/room4_lab.png");
        roomBgPaths.put(5, "/images/chapter1/room5_hold.png");
        roomBgPaths.put(6, "/images/chapter1/room6_crowsnest.png");
        roomBgPaths.put(7, "/images/chapter1/room7_boss.png");

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

        // 3. HUD (Cuori + Inventario)
        hudPanel = new HUDPanel(mainFrame);
        hudPanel.setBounds(0, 0, 1280, 80);
        add(hudPanel);

        // 4. Inizializzazione Hotspots & Oggetti
        initHotspots();

        // 5. Inizializzazione UI Dialoghi
        initDialogueUI();

        // 6. Frecce di Navigazione
        initArrowButtons();

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
        martinoHotspot.setToolTipText("Parla con Martino (Capitano MÃ¦lstrom)");
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

    private void addItemWorldButton(String itemId, String iconPath, int x, int y, int w, int h, int roomId, String name, String desc) {
        JButton btn = new JButton();
        btn.setBounds(x, y, w, h);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(name);

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            btn.setText(name);
            btn.setForeground(Color.YELLOW);
        }

        btn.addActionListener(e -> {
            GameState gs = GameState.getInstance();

            if (itemId.equals("RUGIADA_ASGARD") && !gs.hasDragonKey()) {
                JOptionPane.showMessageDialog(this,
                        "La polena a drago Ã¨ bloccata da un meccanismo d'ottone. Serve la Chiave del Drago!",
                        "Serbatoio Bloccato", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Item item = new Item(itemId, name, desc, iconPath);
            gs.addItem(item);
            btn.setVisible(false);
            hudPanel.updateHUD();

            ItemPickupOverlay pickup = new ItemPickupOverlay(mainFrameRef, item);
            pickup.setVisible(true);
        });

        itemButtons.put(itemId, btn);
        add(btn);
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

        dialogueSkipLabel = new JLabel("");
        dialogueBoxOverlay.add(dialogueSkipLabel);

        add(dialogueBoxOverlay);

        dialogueSystem = new DialogueSystem(
                dialogueNameLabel,
                dialogueTextLabel,
                this::updateDialogueSprite,
                this::onDialogueFinished
        );

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
    private void initArrowButtons() {
        btnUp = createArrowButton("/images/ui/arrow_up.png", "â–²");
        btnDown = createArrowButton("/images/ui/arrow_down.png", "â–¼");
        btnLeft = createArrowButton("/images/ui/arrow_left.png", "â—„");
        btnRight = createArrowButton("/images/ui/arrow_right.png", "â–º");

        btnUp.setBounds(600, 90, 80, 80);
        btnDown.setBounds(600, 610, 80, 80);
        btnLeft.setBounds(30, 320, 80, 80);
        btnRight.setBounds(1170, 320, 80, 80);

        btnUp.addActionListener(e -> navigate("UP"));
        btnDown.addActionListener(e -> navigate("DOWN"));
        btnLeft.addActionListener(e -> navigate("LEFT"));
        btnRight.addActionListener(e -> navigate("RIGHT"));

        add(btnUp);
        add(btnDown);
        add(btnLeft);
        add(btnRight);
    }

    private JButton createArrowButton(String iconPath, String fallbackText) {
        JButton btn = new JButton();
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            btn.setText(fallbackText);
            btn.setFont(new Font("SansSerif", Font.BOLD, 36));
            btn.setForeground(new Color(255, 215, 0));
        }
        return btn;
    }

    public void refreshFromGameState() {
        loadRoom(GameState.getInstance().getCurrentRoom());
        hudPanel.updateHUD();
    }

    public void loadRoom(int roomId) {
        this.currentRoom = roomId;
        GameState.getInstance().setCurrentRoom(roomId);

        try {
            currentBgImage = new ImageIcon(getClass().getResource(roomBgPaths.get(roomId))).getImage();
        } catch (Exception e) {
            currentBgImage = null;
        }

        GameState gs = GameState.getInstance();

        martinoHotspot.setVisible(currentRoom == 2);
        cauldronHotspot.setVisible(currentRoom == 4);
        room7ScreenClickHotspot.setVisible(currentRoom == 7);
        updateWorldItemsVisibility(gs);
        updateArrowVisibility();
        hudPanel.updateHUD();
        repaint();
    }

    private void updateWorldItemsVisibility(GameState gs) {
        if (itemButtons.containsKey("MAPPA_CIELO")) {
            itemButtons.get("MAPPA_CIELO").setVisible(currentRoom == 5 && !gs.hasItem("MAPPA_CIELO") && !gs.hasGivenMapToMartino());
        }

        if (itemButtons.containsKey("NEBBIA_NIFLHEIM")) {
            itemButtons.get("NEBBIA_NIFLHEIM").setVisible(currentRoom == 5 && !gs.hasItem("NEBBIA_NIFLHEIM"));
        }

        if (itemButtons.containsKey("PIUMA_FOLGORE")) {
            itemButtons.get("PIUMA_FOLGORE").setVisible(currentRoom == 6 && !gs.hasItem("PIUMA_FOLGORE"));
        }

        if (itemButtons.containsKey("POLLINE_YGGDRASIL")) {
            itemButtons.get("POLLINE_YGGDRASIL").setVisible(currentRoom == 3 && !gs.hasItem("POLLINE_YGGDRASIL"));
        }

        if (itemButtons.containsKey("RUGIADA_ASGARD")) {
            itemButtons.get("RUGIADA_ASGARD").setVisible(currentRoom == 3 && !gs.hasItem("RUGIADA_ASGARD"));
        }
    }

    private void setArrowsVisible(boolean visible) {
        btnUp.setVisible(visible);
        btnDown.setVisible(visible);
        btnLeft.setVisible(visible);
        btnRight.setVisible(visible);
    }

    private void updateArrowVisibility() {
        setArrowsVisible(false);

        switch (currentRoom) {
            case 1:
                btnLeft.setVisible(true);
                btnRight.setVisible(true);
                btnDown.setVisible(true);
                break;
            case 2:
                btnLeft.setVisible(true);
                btnUp.setVisible(true);
                break;
            case 3:
                btnRight.setVisible(true);
                btnLeft.setVisible(true);
                break;
            case 4:
                btnRight.setVisible(true);
                break;
            case 5:
                btnUp.setVisible(true);
                btnLeft.setVisible(true);
                break;
            case 6:
                btnDown.setVisible(true);
                break;
            case 7:
                btnRight.setVisible(true);
                break;
        }
    }

    private void navigate(String direction) {
        switch (currentRoom) {
            case 1:
                if (direction.equals("LEFT")) {
                    loadRoom(3);
                } else if (direction.equals("RIGHT")) {
                    loadRoom(2);
                } else if (direction.equals("DOWN")) {
                    loadRoom(5);
                }
                break;
            case 2:
                if (direction.equals("LEFT")) {
                    loadRoom(1);
                } else if (direction.equals("UP")) {
                    loadRoom(6);
                }
                break;
            case 3:
                if (direction.equals("RIGHT")) {
                    loadRoom(1);
                } else if (direction.equals("LEFT")) {
                    loadRoom(7);
                }
                break;
            case 4:
                if (direction.equals("RIGHT")) {
                    loadRoom(5);
                }
                break;
            case 5:
                if (direction.equals("UP")) {
                    loadRoom(1);
                } else if (direction.equals("LEFT")) {
                    loadRoom(4);
                }
                break;
            case 6:
                if (direction.equals("DOWN")) {
                    loadRoom(2);
                }
                break;
            case 7:
                if (direction.equals("RIGHT")) {
                    loadRoom(3);
                }
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentBgImage != null) {
            g.drawImage(currentBgImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(25, 25, 35));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Georgia", Font.BOLD, 28));
            g.drawString("STANZA " + currentRoom, 580, 360);
        }
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
