package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Logica grafica e interattiva condivisa da tutti i capitoli. */
public abstract class AbstractChapterPanel extends JPanel {
    protected int currentRoom = 1;
    protected HUDPanel hudPanel;
    protected Image currentBgImage;
    protected JButton btnUp, btnDown, btnLeft, btnRight;
    protected JFrame mainFrameRef;

    private Map<Integer, String> roomBgPaths = Map.of();
    private Map<Integer, Map<String, Integer>> roomExits = Map.of();
    protected final Map<String, JButton> itemButtons = new LinkedHashMap<>();
    private final Map<String, WorldItemDefinition> worldItems = new LinkedHashMap<>();

    protected void initializeChapterBase(JFrame frame,
            Map<Integer, String> backgrounds,
            Map<Integer, Map<String, Integer>> exits) {
        mainFrameRef = frame;
        roomBgPaths = Map.copyOf(backgrounds);
        roomExits = Map.copyOf(exits);
        setLayout(null);
        setPreferredSize(new Dimension(1280, 720));
        hudPanel = new HUDPanel(frame);
        hudPanel.setBounds(0, 0, 1280, 80);
        add(hudPanel);
        createArrowButtons();
    }

    private void createArrowButtons() {
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
        add(btnUp); add(btnDown); add(btnLeft); add(btnRight);
    }

    private JButton createArrowButton(String path, String fallback) {
        JButton button = new JButton();
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        java.net.URL url = getClass().getResource(path);
        if (url != null) {
            Image img = new ImageIcon(url).getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(img));
        } else {
            button.setText(fallback);
            button.setFont(new Font("SansSerif", Font.BOLD, 36));
            button.setForeground(new Color(255, 215, 0));
            System.err.println("[AbstractChapterPanel] Risorsa non trovata: " + path);
        }
        return button;
    }

    /** Crea un hotspot grafico e registra i dati necessari all'oggetto. */
    protected void addItemWorldButton(String id, String iconPath, int x, int y,
            int width, int height, int roomId, String name, String description) {
        JButton button = new JButton();
        button.setBounds(x, y, width, height);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(name);
        java.net.URL url = getClass().getResource(iconPath);
        if (url != null) {
            Image img = new ImageIcon(url).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(img));
        } else {
            button.setText(name);
            button.setForeground(Color.YELLOW);
            System.err.println("[AbstractChapterPanel] Immagine oggetto non trovata: " + iconPath);
        }
        WorldItemDefinition definition = new WorldItemDefinition(id, iconPath, roomId, name, description);
        worldItems.put(id, definition);
        itemButtons.put(id, button);
        button.addActionListener(e -> collectWorldItem(definition, button));
        add(button);
    }

    private void collectWorldItem(WorldItemDefinition definition, JButton button) {
        GameState state = GameState.getInstance();
        if (state.hasItem(definition.id)) {
            button.setVisible(false);
            return;
        }
        if (!canCollectWorldItem(definition.id, state)) return;
        Item item = new Item(definition.id, definition.name, definition.description, definition.iconPath);
        state.addItem(item);
        button.setVisible(false);
        hudPanel.updateHUD();
        new ItemPickupOverlay(mainFrameRef, item).setVisible(true);
    }

    protected boolean canCollectWorldItem(String itemId, GameState state) { return true; }
    protected boolean shouldShowWorldItem(String itemId, GameState state) { return true; }

    private void updateWorldItemsVisibility(GameState state) {
        for (WorldItemDefinition item : worldItems.values()) {
            JButton button = itemButtons.get(item.id);
            boolean visible = currentRoom == item.roomId
                    && !state.hasItem(item.id)
                    && shouldShowWorldItem(item.id, state);
            button.setVisible(visible);
            if (visible) setComponentZOrder(button, 0);
        }
    }

    public void refreshFromGameState() {
        loadRoom(GameState.getInstance().getCurrentRoom());
    }

    public void loadRoom(int roomId) {
        if (!roomBgPaths.containsKey(roomId)) {
            System.err.println("[AbstractChapterPanel] Stanza non configurata: " + roomId);
            return;
        }
        currentRoom = roomId;
        GameState state = GameState.getInstance();
        state.setCurrentRoom(roomId);
        java.net.URL url = getClass().getResource(roomBgPaths.get(roomId));
        currentBgImage = url == null ? null : new ImageIcon(url).getImage();
        updateWorldItemsVisibility(state);
        updateArrowVisibility();
        onRoomChanged();
        hudPanel.updateHUD();
        revalidate();
        repaint();
    }

    protected void onRoomChanged() { }
    protected void setArrowsVisible(boolean visible) {
        btnUp.setVisible(visible); btnDown.setVisible(visible);
        btnLeft.setVisible(visible); btnRight.setVisible(visible);
    }

    protected void updateArrowVisibility() {
        Map<String, Integer> exits = roomExits.getOrDefault(currentRoom, Map.of());
        btnUp.setVisible(exits.containsKey("UP"));
        btnDown.setVisible(exits.containsKey("DOWN"));
        btnLeft.setVisible(exits.containsKey("LEFT"));
        btnRight.setVisible(exits.containsKey("RIGHT"));
    }

    protected void navigate(String direction) {
        Integer destination = roomExits.getOrDefault(currentRoom, Map.of()).get(direction);
        if (destination != null) loadRoom(destination);
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

    private static final class WorldItemDefinition {
        private final String id, iconPath, name, description;
        private final int roomId;
        private WorldItemDefinition(String id, String iconPath, int roomId, String name, String description) {
            this.id = id; this.iconPath = iconPath; this.roomId = roomId;
            this.name = name; this.description = description;
        }
    }
}
