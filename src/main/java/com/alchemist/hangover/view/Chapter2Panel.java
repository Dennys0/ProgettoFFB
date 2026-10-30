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

public class Chapter2Panel extends JPanel {

    private int currentRoom = 1;
    private HUDPanel hudPanel;
    private Image currentBgImage;

    // Navigation
    private JButton btnUp, btnDown, btnLeft, btnRight;
    private Map<Integer, String> roomBgPaths;

    // Elementi Mondo di Gioco
    private Map<String, JButton> itemButtons = new HashMap<>();
    private JButton siegfriedHotspot;
    private JButton cauldronHotspot;
    private JButton ranBossHotspot;
    private JButton journalHotspot;

    // UI Dialoghi
    private JPanel dialogueBoxOverlay;
    private JLabel dialogueNameLabel;
    private JLabel dialogueTextLabel;
    private JLabel dialogueSkipLabel;
    private JLabel characterSpriteLabel;
    private Image dialogueBoxImage;
    private DialogueSystem dialogueSystem;
    private Map<String, ImageIcon> characterSprites;
    private JFrame mainFrameRef;

    public Chapter2Panel(JFrame mainFrame) {
        this.mainFrameRef = mainFrame;
        setLayout(null);
        setPreferredSize(new Dimension(1280, 720));

        GameState.getInstance().setBossDefeated(false);

        // 1. Mappatura Sfondi Capitolo 2
        roomBgPaths = new HashMap<>();
        roomBgPaths.put(1, "/images/chapter2/room1_hall.png");
        roomBgPaths.put(2, "/images/chapter2/room2_bar.png");
        roomBgPaths.put(3, "/images/chapter2/room3_sacristy.png");
        roomBgPaths.put(4, "/images/chapter2/room4_gardens.png");
        roomBgPaths.put(5, "/images/chapter2/room5_pit.png");
        roomBgPaths.put(6, "/images/chapter2/room6_balcony.png");
        roomBgPaths.put(7, "/images/chapter2/room7_boss.png");

        try {
            dialogueBoxImage = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/images/ui/dialogue_box.png"));
        } catch (Exception e) {
            System.err.println("Errore caricamento dialogue_box.png in Chapter2: " + e.getMessage());
        }

        characterSprites = new HashMap<>();
        loadSprite("SIEGFRIED", "/images/characters/siegfried_realms.png");
        loadSprite("RÁN", "/images/characters/ran_boss.png");

        hudPanel = new HUDPanel(mainFrame);
        hudPanel.setBounds(0, 0, 1280, 80);
        add(hudPanel);

        initHotspots();
        initDialogueUI();
        initArrowButtons();

        loadRoom(1);
    }

    private void loadSprite(String name, String path) {
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(path));
            Image img = icon.getImage().getScaledInstance(-1, 520, Image.SCALE_SMOOTH);
            characterSprites.put(name, new ImageIcon(img));
        } catch (Exception e) {
            System.err.println("Sprite non trovato per " + name + " in " + path);
        }
    }

    private void initHotspots() {
        // NPC Siegfried al Bancone (Stanza 2)
        siegfriedHotspot = new JButton();
        siegfriedHotspot.setBounds(500, 200, 280, 400);
        siegfriedHotspot.setContentAreaFilled(false);
        siegfriedHotspot.setBorderPainted(false);
        siegfriedHotspot.setFocusPainted(false);
        siegfriedHotspot.setFocusable(false);
        siegfriedHotspot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        siegfriedHotspot.addActionListener(e -> startSiegfriedDialogue());
        add(siegfriedHotspot);

        // Calderone nella Sacrestia Corallina (Stanza 3)
        cauldronHotspot = new JButton();
        cauldronHotspot.setBounds(500, 350, 280, 250);
        cauldronHotspot.setContentAreaFilled(false);
        cauldronHotspot.setBorderPainted(false);
        cauldronHotspot.setFocusPainted(false);
        cauldronHotspot.setFocusable(false);
        cauldronHotspot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cauldronHotspot.addActionListener(e -> interactWithCauldron());
        add(cauldronHotspot);

        // Boss Rán nel Santuario (Stanza 7 - Area ridotta per non coprire le frecce)
        ranBossHotspot = new JButton();
        ranBossHotspot.setBounds(250, 200, 780, 380);
        ranBossHotspot.setContentAreaFilled(false);
        ranBossHotspot.setBorderPainted(false);
        ranBossHotspot.setFocusPainted(false);
        ranBossHotspot.setFocusable(false);
        ranBossHotspot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ranBossHotspot.addActionListener(e -> triggerRanBossEncounter());
        add(ranBossHotspot);

        // Oggetto Diario del Capitano (Stanza 6 - Balconata dei Relitti)
        addItemWorldButton("DIARIO_CAPITANO", "/images/items/item_journal.png", 580, 420, 70, 70, 6,
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

    private void addItemWorldButton(String itemId, String iconPath, int x, int y, int w, int h, int roomId, String name, String desc) {
        JButton btn = new JButton();
        btn.setBounds(x, y, w, h);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            btn.setText(name);
            btn.setForeground(Color.CYAN);
        }

        btn.addActionListener(e -> {
            GameState gs = GameState.getInstance();
            gs.addItem(new Item(itemId, name, desc, iconPath));
            btn.setVisible(false);
            hudPanel.updateHUD();

            //ItemPickupOverlay overlay = new ItemPickupOverlay(mainFrameRef, name, iconPath);
            // Nuova chiamata usando l'oggetto Item creato:
            Item item = new Item(itemId, name, desc, iconPath);
            GameState.getInstance().addItem(item);
            btn.setVisible(false);
            hudPanel.updateHUD();

            ItemPickupOverlay overlay = new ItemPickupOverlay(mainFrameRef, item);
            overlay.setVisible(true);
        });

        itemButtons.put(itemId, btn);
        add(btn);
    }

    private void interactWithCauldron() {
        CauldronOverlay cauldron = new CauldronOverlay(mainFrameRef);
        cauldron.setVisible(true);
    }

    private void startSiegfriedDialogue() {
        setArrowsVisible(false);
        siegfriedHotspot.setVisible(false);
        dialogueBoxOverlay.setVisible(true);

        boolean hasJournal = GameState.getInstance().hasItem("DIARIO_CAPITANO");
        String dialoguePath = hasJournal ? "/dialogues/siegfried_dialogue_2.txt" : "/dialogues/siegfried_dialogue_1.txt";

        dialogueSystem.loadDialogue(dialoguePath);
        dialogueSystem.startDialogue();
    }

    private void triggerRanBossEncounter() {
        GameState gs = GameState.getInstance();
        if (gs.isBossDefeated()) {
            JOptionPane.showMessageDialog(this, "Le reti di Rán sono spezzate. Il santuario sommerso è calmo.", "Boss Già Sconfitto", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        setArrowsVisible(false);
        ranBossHotspot.setVisible(false);
        dialogueBoxOverlay.setVisible(true);

        dialogueSystem.loadDialogue("/dialogues/ran_boss_dialogue.txt");
        dialogueSystem.startDialogue();
    }

    private void initDialogueUI() {
        characterSpriteLabel = new JLabel();
        characterSpriteLabel.setBounds(340, 30, 600, 620);
        characterSpriteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        characterSpriteLabel.setVerticalAlignment(SwingConstants.CENTER);
        characterSpriteLabel.setVisible(false);
        add(characterSpriteLabel);

        dialogueBoxOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
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

        dialogueSkipLabel = new JLabel("[ SPAZIO / CLICK ▶ ]");
        dialogueSkipLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        dialogueSkipLabel.setForeground(new Color(200, 200, 200, 180));
        dialogueSkipLabel.setBounds(950, 155, 180, 25);
        dialogueSkipLabel.setHorizontalAlignment(SwingConstants.RIGHT);
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
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "advanceDialogue2");
        actionMap.put("advanceDialogue2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dialogueBoxOverlay.isVisible()) {
                    dialogueSystem.nextLine();
                }
            }
        });
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
            siegfriedHotspot.setVisible(true);
        } else if (currentRoom == 7 && !GameState.getInstance().isBossDefeated()) {
            RanStrategyBossOverlay bossFight = new RanStrategyBossOverlay(mainFrameRef);
            bossFight.setVisible(true);
            ranBossHotspot.setVisible(true);
            hudPanel.updateHUD();
            updateArrowVisibility();
        }
    }

    private void initArrowButtons() {
        btnUp = createArrowButton("/images/ui/arrow_up.png", "▲");
        btnDown = createArrowButton("/images/ui/arrow_down.png", "▼");
        btnLeft = createArrowButton("/images/ui/arrow_left.png", "◄");
        btnRight = createArrowButton("/images/ui/arrow_right.png", "►");

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

    public void loadRoom(int roomId) {
        this.currentRoom = roomId;
        GameState.getInstance().setCurrentRoom(roomId);

        try {
            currentBgImage = new ImageIcon(getClass().getResource(roomBgPaths.get(roomId))).getImage();
        } catch (Exception e) {
            currentBgImage = null;
        }

        GameState gs = GameState.getInstance();

        siegfriedHotspot.setVisible(currentRoom == 2);
        cauldronHotspot.setVisible(currentRoom == 3);

        // Rán visibile solo in stanza 7 e SOLO se NON ancora sconfitta
        ranBossHotspot.setVisible(currentRoom == 7 && !gs.isBossDefeated());

        updateWorldItemsVisibility(gs);
        updateArrowVisibility();
        hudPanel.updateHUD();
        revalidate();
        repaint();
    }

    private void updateWorldItemsVisibility(GameState gs) {
        // 1. Corallo (Stanza 4)
        if (itemButtons.containsKey("CORALLO_RAN")) {
            boolean show = (currentRoom == 4 && !gs.hasItem("CORALLO_RAN"));
            JButton btn = itemButtons.get("CORALLO_RAN");
            btn.setVisible(show);
            if (show) {
                setComponentZOrder(btn, 0);
            }
        }

        // 2. Inchiostro di Kraken (Stanza 4)
        if (itemButtons.containsKey("INCHIOSTRO_KRAKEN")) {
            boolean show = (currentRoom == 4 && !gs.hasItem("INCHIOSTRO_KRAKEN"));
            JButton btn = itemButtons.get("INCHIOSTRO_KRAKEN");
            btn.setVisible(show);
            if (show) {
                setComponentZOrder(btn, 0);
            }
        }

        // 3. Sale di Jörmungandr (Stanza 5)
        if (itemButtons.containsKey("SALE_JORMUNGANDR")) {
            boolean show = (currentRoom == 5 && !gs.hasItem("SALE_JORMUNGANDR"));
            JButton btn = itemButtons.get("SALE_JORMUNGANDR");
            btn.setVisible(show);
            if (show) {
                setComponentZOrder(btn, 0);
            }
        }

        // 4. Acqua del Lete (Stanza 5)
        if (itemButtons.containsKey("ACQUA_LETE")) {
            boolean show = (currentRoom == 5 && !gs.hasItem("ACQUA_LETE"));
            JButton btn = itemButtons.get("ACQUA_LETE");
            btn.setVisible(show);
            if (show) {
                setComponentZOrder(btn, 0);
            }
        }

        // 5. Diario del Capitano (RIGOROSAMENTE SOLO STANZA 6)
        if (itemButtons.containsKey("DIARIO_CAPITANO")) {
            boolean show = (currentRoom == 6 && !gs.hasItem("DIARIO_CAPITANO"));
            JButton btn = itemButtons.get("DIARIO_CAPITANO");
            btn.setVisible(show);
            if (show) {
                setComponentZOrder(btn, 0);
            }
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
            case 1: // Salone Banchetti (Hub)
                btnUp.setVisible(true);
                btnLeft.setVisible(true);
                btnRight.setVisible(true);
                btnDown.setVisible(true);
                break;
            case 2: // Bancone di Cristallo
                btnDown.setVisible(true);
                btnUp.setVisible(true);
                break;
            case 3: // Sacrestia Corallina (Calderone)
                btnRight.setVisible(true);
                break;
            case 4: // Giardini Corallo Scarlatto
                btnLeft.setVisible(true);
                break;
            case 5: // Fossa Salmastra
                btnUp.setVisible(true);
                btnDown.setVisible(true);
                break;
            case 6: // Balconata dei Relitti
                btnDown.setVisible(true);
                break;
            case 7: // Santuario della Rete (Boss Rán)
                btnUp.setVisible(true);
                break;
        }
    }

    private void navigate(String direction) {
        switch (currentRoom) {
            case 1:
                if (direction.equals("UP")) {
                    loadRoom(2);
                } else if (direction.equals("LEFT")) {
                    loadRoom(3);
                } else if (direction.equals("RIGHT")) {
                    loadRoom(4);
                } else if (direction.equals("DOWN")) {
                    loadRoom(5);
                }
                break;
            case 2:
                if (direction.equals("DOWN")) {
                    loadRoom(1);
                } else if (direction.equals("UP")) {
                    loadRoom(6);
                }
                break;
            case 3:
                if (direction.equals("RIGHT")) {
                    loadRoom(1);
                }
                break;
            case 4:
                if (direction.equals("LEFT")) {
                    loadRoom(1);
                }
                break;
            case 5:
                if (direction.equals("UP")) {
                    loadRoom(1);
                } else if (direction.equals("DOWN")) {
                    loadRoom(7);
                }
                break;
            case 6:
                if (direction.equals("DOWN")) {
                    loadRoom(2);
                }
                break;
            case 7:
                if (direction.equals("UP")) {
                    loadRoom(5);
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
            g.setColor(new Color(10, 30, 45));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.CYAN);
            g.setFont(new Font("Georgia", Font.BOLD, 28));
            g.drawString("SALE DI ÆGIR — STANZA " + currentRoom, 480, 360);
        }
    }
}
