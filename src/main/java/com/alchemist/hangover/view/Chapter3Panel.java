package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.controller.SceneManager;
import javax.swing.*;
import java.awt.*;

/**
 * Capitolo III - Terra: Il Dominio di Ymir.
 *
 * Navigazione tra sette stanze e raccolta degli ingredienti della Terra.
 * Gunnar, l'enigma di Ymir e il calderone arriveranno nelle tappe successive.
 */
public final class Chapter3Panel extends AbstractChapterPanel {
    private JButton ymirHotspot;
    private JButton gunnarHotspot;
    private JButton steleHotspot;
    private JButton forgeHotspot;
    private YmirCauldronPanel brewingPanel;

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

        createGunnarHotspot();
        createSteleHotspot();
        createForgeHotspot();
        createYmirHotspot();
        loadRoom(1);
    }

    private void createGunnarHotspot() {
        // Gunnar e' gia' raffigurato seduto a sinistra in room2_rune.png.
        // L'area di clic e' trasparente: il ritratto va mostrato solo
        // nella finestra di dialogo GunnarDialogueDialog.
        // Coordinate in base alla risoluzione di gioco 1280x720.
        gunnarHotspot = new HotspotButton();
        gunnarHotspot.setBounds(143, 268, 139, 174);
        gunnarHotspot.setToolTipText("Parla con Gunnar, l'eremita seduto sulla roccia");
        gunnarHotspot.getAccessibleContext().setAccessibleName("Parla con Gunnar");
        gunnarHotspot.addActionListener(e -> {
            boolean alreadyLearned = GameState.getInstance().hasLearnedGunnarWisdom();
            new GunnarDialogueDialog(mainFrameRef).setVisible(true);
            if (!alreadyLearned && GameState.getInstance().hasLearnedGunnarWisdom()) {
                ToastNotification.show(this, "Il linguaggio delle rune e' stato annotato nel Diario.");
            }
        });
        add(gunnarHotspot);
        gunnarHotspot.setVisible(false);
    }

    private void createSteleHotspot() {
        steleHotspot = new HotspotButton();
        steleHotspot.setBounds(140, 285, 200, 178);
        steleHotspot.setForeground(new Color(255, 190, 83));
        steleHotspot.setFont(new Font("Georgia", Font.BOLD, 18));
        steleHotspot.setText("<html><center>STELE<br>RUNICA</center></html>");
        steleHotspot.setToolTipText("Leggi i tre versi della Rupe dell'Eco");
        steleHotspot.addActionListener(e ->
                new RuneSteleDialog(mainFrameRef).setVisible(true));
        add(steleHotspot);
        steleHotspot.setVisible(false);
    }

    private void createForgeHotspot() {
        forgeHotspot = new HotspotButton();
        forgeHotspot.setBounds(548, 282, 235, 260);
        forgeHotspot.setToolTipText("Prepara l'Ymir's Old Fashioned nel boccale di pietra");
        forgeHotspot.getAccessibleContext().setAccessibleName("Apri il boccale di pietra");
        forgeHotspot.addActionListener(e -> openForge());
        add(forgeHotspot);
        forgeHotspot.setVisible(false);

        brewingPanel = new YmirCauldronPanel(this::closeForge,
                this::finishYmirDrink, this::onBrewingDisaster);
        brewingPanel.setBounds(270, 108, 740, 536);
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
        if (gameOver != null) gameOver.showReason(message);
        SceneManager.getInstance().showScene("GAMEOVER");
    }

    private void finishYmirDrink() {
        closeForge();
        GameState.getInstance().setYmirOldFashionedCreated(true);
        com.alchemist.hangover.controller.AudioManager.getInstance()
                .playEffect(com.alchemist.hangover.controller.AudioManager.Effect.SUCCESS);
        SceneManager.getInstance().showScene("CHAPTER3_COMPLETE");
    }

    private void createYmirHotspot() {
        ymirHotspot = new JButton();
        ymirHotspot.setBounds(308, 165, 664, 443);
        ymirHotspot.setContentAreaFilled(false);
        ymirHotspot.setBorderPainted(false);
        ymirHotspot.setFocusPainted(false);
        ymirHotspot.setFocusable(false);
        ymirHotspot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ymirHotspot.setToolTipText("Ymir - il gigante primordiale");
        ymirHotspot.getAccessibleContext().setAccessibleName("Esamina Ymir");

        java.net.URL image = getClass().getResource("/images/characters/boss_ymir.png");
        if (image != null) {
            Image sprite = new ImageIcon(image).getImage().getScaledInstance(664, 443, Image.SCALE_SMOOTH);
            ymirHotspot.setIcon(new ImageIcon(sprite));
        } else {
            ymirHotspot.setText("Ymir");
            ymirHotspot.setForeground(new Color(255, 216, 137));
            ymirHotspot.setFont(new Font("Georgia", Font.BOLD, 40));
            System.err.println("[Chapter3Panel] Sprite Ymir non trovato.");
        }
        ymirHotspot.addActionListener(e -> triggerYmirEncounter());
        add(ymirHotspot);
        ymirHotspot.setVisible(false);
    }

    private void triggerYmirEncounter() {
        GameState state = GameState.getInstance();
        if (state.isYmirPacified()) return;
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
        triggerYmirEncounter();
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
