package com.alchemist.hangover.view.chapter;

import com.alchemist.hangover.gameplay.cauldron.RecipeManager;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
import com.alchemist.hangover.view.ToastNotification;
import com.alchemist.hangover.view.cauldron.CauldronController;
import com.alchemist.hangover.view.cauldron.CauldronOverlay;
import com.alchemist.hangover.view.inventory.ExplorationHud;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

/** Logica grafica e interattiva condivisa da tutti i capitoli. */
public abstract class AbstractChapterPanel extends JPanel {
    protected int currentRoom = 1;
    protected ExplorationHud hudPanel;
    protected Image currentBgImage;
    protected JButton btnUp, btnDown, btnLeft, btnRight;
    protected JFrame mainFrameRef;
    private RoomAtmosphereEffect roomAtmosphere;

    private Map<Integer, String> roomBgPaths = Map.of();
    private Map<Integer, Map<String, Integer>> roomExits = Map.of();
    /** Registro condiviso: nessuna sottoclasse duplica la gestione degli NPC. */
    private final Map<HotspotButton, Rectangle> sceneBounds = new LinkedHashMap<>();
    private final Map<Integer, List<HotspotButton>> npcHotspots = new HashMap<>();
    protected final Map<String, JButton> itemButtons = new LinkedHashMap<>();
    private final Map<String, WorldItemDefinition> worldItems = new LinkedHashMap<>();

    /** Evita di ripetere la configurazione delle stanze nei singoli capitoli. */
    protected void initializeChapterBase(JFrame frame, ChapterLayouts.Layout layout) {
        initializeChapterBase(frame, layout.backgrounds(), layout.exits());
    }

    protected void initializeChapterBase(JFrame frame,
            Map<Integer, String> backgrounds,
            Map<Integer, Map<String, Integer>> exits) {
        mainFrameRef = frame;
        roomBgPaths = Map.copyOf(backgrounds);
        roomExits = Map.copyOf(exits);
        setLayout(null);
        setPreferredSize(new Dimension(1280, 720));
        hudPanel = new ExplorationHud(frame);
        hudPanel.setBounds(0, 0, 1280, 80);
        add(hudPanel);
        createArrowButtons();
        roomAtmosphere = new RoomAtmosphereEffect();
        // Solo zona di gioco, non l'HUD. Sempre dietro gli hotspot.
        roomAtmosphere.setBounds(0, 80, 1280, 530);
        add(roomAtmosphere);
        setComponentZOrder(roomAtmosphere, getComponentCount() - 1);
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
        btnUp.setToolTipText("Vai in alto");
        btnDown.setToolTipText("Vai in basso");
        btnLeft.setToolTipText("Vai a sinistra");
        btnRight.setToolTipText("Vai a destra");
        btnUp.addActionListener(e -> navigate("UP"));
        btnDown.addActionListener(e -> navigate("DOWN"));
        btnLeft.addActionListener(e -> navigate("LEFT"));
        btnRight.addActionListener(e -> navigate("RIGHT"));
        add(btnUp); add(btnDown); add(btnLeft); add(btnRight);
    }

    private JButton createArrowButton(String path, String fallback) {
        JButton button = new HotspotButton();
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

    /** Una sola creazione per gli hotspot di NPC, boss, indizi e calderoni. */
    protected final HotspotButton registerActionHotspot(
            Rectangle bounds, String label, Runnable action) {
        HotspotButton hotspot = new HotspotButton();
        sceneBounds.put(hotspot, new Rectangle(bounds));
        hotspot.setBounds(bounds);
        hotspot.setToolTipText(label);
        hotspot.getAccessibleContext().setAccessibleName(label);
        hotspot.addActionListener(e -> action.run());
        hotspot.setVisible(false);
        add(hotspot);
        return hotspot;
    }

    /** Registra un NPC gia' visibile nel PNG della stanza, senza sovrapporre sprite. */
    @Override public void doLayout() {
        super.doLayout();
        sceneBounds.forEach((button, r) -> button.setBounds(r.x * getWidth() / 1280,
                r.y * getHeight() / 720, r.width * getWidth() / 1280, r.height * getHeight() / 720));
    }

    protected final HotspotButton registerNpcHotspot(int room, Rectangle bounds,
            String label, Runnable action) {
        HotspotButton hotspot = registerActionHotspot(bounds, label, action);
        npcHotspots.computeIfAbsent(room, ignored -> new ArrayList<>()).add(hotspot);
        return hotspot;
    }

    /**
     * La visibilita' dipende dalla stanza e dallo stato di interazione.
     * Gli hotspot attivi vengono portati davanti alle zone generiche.
     */
    protected final void setRegisteredNpcHotspotsVisible(boolean enabled) {
        for (Map.Entry<Integer, List<HotspotButton>> entry : npcHotspots.entrySet()) {
            for (HotspotButton hotspot : entry.getValue()) {
                boolean show = enabled && entry.getKey() == currentRoom;
                hotspot.setVisible(show);
                if (show) setComponentZOrder(hotspot, 0);
            }
        }
        bringArrowsToFront();
    }

    protected final void bringArrowsToFront() {
        for (JButton arrow : new JButton[] {btnUp, btnDown, btnLeft, btnRight})
            if (arrow != null && arrow.isVisible()) setComponentZOrder(arrow, 0);
    }

    /** Crea un hotspot grafico e registra i dati necessari all'oggetto. */
    protected void addItemWorldButton(String id, String iconPath, int x, int y,
            int width, int height, int roomId, String name, String description) {
        HotspotButton button = new HotspotButton();
        // Gli oggetti raccolti hanno un bagliore circolare leggermente piu' evidente.
        button.setHoverHighlightEnabled(true);
        button.setBounds(x, y, width, height);
        button.setToolTipText("Raccogli: " + name);
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

    /** Attenua solo l'icona nella stanza; l'inventario mantiene l'originale. */
    protected final void muteWorldItem(String id, float opacity) {
        JButton button = itemButtons.get(id);
        Icon source = button.getIcon();
        if (source == null) return;
        java.awt.image.BufferedImage muted = new java.awt.image.BufferedImage(
                source.getIconWidth(), source.getIconHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = muted.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.SrcOver.derive(opacity));
            source.paintIcon(button, graphics, 0, 0);
        } finally {
            graphics.dispose();
        }
        button.setIcon(new ImageIcon(muted));
    }

    private void collectWorldItem(WorldItemDefinition definition, JButton button) {
        GameState state = GameState.getInstance();
        if (state.hasItem(definition.id)) {
            button.setVisible(false);
            return;
        }
        if (!canCollectWorldItem(definition.id, state)) return;
        Item item = new Item(definition.id, definition.name, definition.description, definition.iconPath);
        button.setVisible(false);
        receiveItem(item);
    }

    protected final void receiveItem(Item item) {
        GameState state = GameState.getInstance();
        if (state.hasItem(item.getId())) return;
        state.addItem(item);
        hudPanel.updateHUD();
        com.alchemist.hangover.controller.AudioManager.getInstance()
                .playEffect(com.alchemist.hangover.controller.AudioManager.Effect.PICKUP);
        ToastNotification.show(this, "Hai ricevuto: " + item.getName(), item.getIconPath());
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

    /** Checkpoint della ricetta: comune ai tre capitoli, senza consumare oggetti. */
    public void retryBossEncounter() {
        throw new UnsupportedOperationException("Boss non configurato per questo capitolo");
    }

    public final void retryCauldronAfterFailure() {
        int chapter = GameState.getInstance().getChapter();
        loadRoom(RecipeManager.forChapter(chapter).cauldronRoom());
        showCauldron();
    }

    protected final void showCauldron() {
        if (CauldronController.allowOpening(mainFrameRef)) {
            new CauldronOverlay(mainFrameRef).setVisible(true);
        }
    }

    public void refreshFromGameState() {
        loadRoom(GameState.getInstance().getCurrentRoom());
    }

    public void loadRoom(int roomId) {
        long started = System.nanoTime();
        if (!roomBgPaths.containsKey(roomId)) {
            System.err.println("[AbstractChapterPanel] Stanza non configurata: " + roomId);
            return;
        }
        boolean changed = roomId != currentRoom;
        currentRoom = roomId;
        GameState state = GameState.getInstance();
        state.setCurrentRoom(roomId);
        java.net.URL url = getClass().getResource(roomBgPaths.get(roomId));
        currentBgImage = url == null ? null : new ImageIcon(url).getImage();
        roomAtmosphere.configure(roomBgPaths.get(roomId));
        // Un accento breve, mai un loop aggiuntivo, solo entrando in una nuova stanza.
        if (changed && isShowing()) {
            com.alchemist.hangover.controller.AudioManager.getInstance()
                    .playRoomCue(roomBgPaths.get(roomId));
        }
        updateWorldItemsVisibility(state);
        updateArrowVisibility();
        onRoomChanged();
        bringArrowsToFront();
        hudPanel.updateHUD();
        revalidate();
        repaint();
        com.alchemist.hangover.controller.PerformanceLog.finished("Stanza " + roomId, started);
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
        if (destination != null) {
            loadRoom(destination);
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

    private static final class WorldItemDefinition {
        private final String id, iconPath, name, description;
        private final int roomId;
        private WorldItemDefinition(String id, String iconPath, int roomId, String name, String description) {
            this.id = id; this.iconPath = iconPath; this.roomId = roomId;
            this.name = name; this.description = description;
        }
    }
}
