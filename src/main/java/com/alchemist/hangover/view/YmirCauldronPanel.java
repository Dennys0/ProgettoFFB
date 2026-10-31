package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
import com.alchemist.hangover.model.YmirCauldronRecipe;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Postazione sopra room3_forge.png, senza aprire una finestra separata.
 * Riusa i tre slot, le posizioni dei pulsanti e il disegno del calderone
 * originale dei Capitoli I e II, ma mantiene la ricetta di Ymir.
 */
public final class YmirCauldronPanel extends SharedCauldronArtworkPanel {
    public static final int WIDTH = 840;
    public static final int HEIGHT = 583;

    private final Set<String> selected = new LinkedHashSet<>();
    private final JPanel slotsPanel = new JPanel(new GridLayout(1, 3, 30, 0));
    private final JPanel inventoryGrid = new JPanel(new GridLayout(2, 3, 8, 7));
    private final JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
    private final Runnable onClose;
    private final Runnable onSuccess;
    private final Consumer<String> onDisaster;

    public YmirCauldronPanel(Runnable onClose, Runnable onSuccess,
                            Consumer<String> onDisaster) {
        this.onClose = onClose;
        this.onSuccess = onSuccess;
        this.onDisaster = onDisaster;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);

        // La grafica e' ridimensionata da 980x680 ai 840x583 del pannello,
        // con le stesse coordinate proporzionali della UI originale.
        slotsPanel.setOpaque(false);
        slotsPanel.setBounds(125, 94, 590, 120);
        add(slotsPanel);

        inventoryGrid.setOpaque(false);
        inventoryGrid.setBounds(103, 249, 634, 137);
        add(inventoryGrid);

        statusLabel.setForeground(new Color(255, 228, 174));
        statusLabel.setFont(new Font("Georgia", Font.BOLD, 13));
        statusLabel.setBounds(124, 390, 592, 34);
        add(statusLabel);

        JButton mix = invisibleAction("MESCOLA", 180, 453, 242, 51);
        mix.addActionListener(e -> mix());
        add(mix);

        JButton reset = invisibleAction("RESET", 446, 453, 216, 51);
        reset.addActionListener(e -> {
            selected.clear();
            refreshIngredients();
        });
        add(reset);

        JButton close = invisibleAction("CHIUDI", 377, 529, 86, 31);
        close.addActionListener(e -> onClose.run());
        add(close);

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "closeEarthCauldron");
        getActionMap().put("closeEarthCauldron", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (isVisible()) onClose.run();
            }
        });
    }

    private static JButton invisibleAction(String label, int x, int y, int w, int h) {
        JButton button = new JButton();
        button.setBounds(x, y, w, h);
        button.setToolTipText(label);
        button.getAccessibleContext().setAccessibleName(label);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    /** La selezione si rigenera a ogni apertura della postazione. */
    public void refreshIngredients() {
        selected.clear();
        populateIngredients();
    }

    private void populateIngredients() {
        inventoryGrid.removeAll();
        List<Item> available = new ArrayList<>();
        for (Item item : GameState.getInstance().getInventory()) {
            if (YmirCauldronRecipe.ALL.contains(item.getId())) available.add(item);
        }

        for (Item item : available) {
            JButton button = new JButton();
            button.setForeground(new Color(255, 223, 175));
            button.setFont(new Font("Georgia", Font.BOLD, 12));
            button.setBackground(new Color(60, 43, 44));
            button.setContentAreaFilled(false);
            button.setBorder(BorderFactory.createLineBorder(
                    selected.contains(item.getId())
                            ? new Color(255, 225, 121)
                            : new Color(166, 129, 69),
                    selected.contains(item.getId()) ? 2 : 1));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setFocusPainted(false);
            button.setToolTipText(item.getName() + ": " + item.getDescription());
            button.setText("<html><center>" + escapeHtml(item.getName()) + "</center></html>");
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.BOTTOM);
            URL icon = getClass().getResource(item.getIconPath());
            if (icon != null) {
                button.setIcon(new ImageIcon(new ImageIcon(icon).getImage()
                        .getScaledInstance(42, 42, Image.SCALE_SMOOTH)));
            }
            button.addActionListener(e -> {
                if (selected.contains(item.getId())) {
                    selected.remove(item.getId());
                } else if (selected.size() < 3) {
                    selected.add(item.getId());
                } else {
                    statusLabel.setText("Massimo tre ingredienti. Deselezionane uno.");
                    return;
                }
                populateIngredients();
            });
            inventoryGrid.add(button);
        }

        for (int i = available.size(); i < 6; i++) {
            JLabel empty = new JLabel(i == 0 ? "Nessun ingrediente" : "", SwingConstants.CENTER);
            empty.setForeground(new Color(213, 200, 173));
            inventoryGrid.add(empty);
        }

        slotsPanel.removeAll();
        List<String> ids = new ArrayList<>(selected);
        for (int i = 0; i < 3; i++) {
            JPanel slot = new JPanel(new BorderLayout(0, 2));
            slot.setOpaque(false);
            if (i < ids.size()) {
                String id = ids.get(i);
                for (Item item : available) {
                    if (!id.equals(item.getId())) continue;
                    JLabel label = new JLabel(
                            "<html><center>" + escapeHtml(item.getName()) + "</center></html>",
                            SwingConstants.CENTER);
                    label.setForeground(new Color(255, 229, 143));
                    label.setFont(new Font("Georgia", Font.BOLD, 12));
                    URL resource = getClass().getResource(item.getIconPath());
                    if (resource != null) {
                        label.setIcon(new ImageIcon(new ImageIcon(resource).getImage()
                                .getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
                    }
                    label.setHorizontalTextPosition(SwingConstants.CENTER);
                    label.setVerticalTextPosition(SwingConstants.BOTTOM);
                    slot.add(label, BorderLayout.CENTER);
                    break;
                }
            }
            slotsPanel.add(slot);
        }

        slotsPanel.revalidate();
        inventoryGrid.revalidate();
        slotsPanel.repaint();
        inventoryGrid.repaint();
        if (selected.isEmpty()) statusLabel.setText("Seleziona tre ingredienti dall'inventario.");
        else statusLabel.setText(selected.size() + " / 3 ingredienti nel boccale.");
    }

    private void mix() {
        switch (YmirCauldronRecipe.evaluate(selected)) {
            case INCOMPLETE -> statusLabel.setText("Servono esattamente tre ingredienti.");
            case WRONG_MIX -> statusLabel.setText("La miscela non reagisce. Cambia combinazione.");
            case NIDHOGG_ACID -> onDisaster.accept(
                    "La Radice di Nidhogg scioglie il boccale con acido draconico.");
            case GIANT_BONE_STONE -> onDisaster.accept(
                    "La Polvere d'Ossa trasforma il drink in un blocco di pietra.");
            case SUCCESS -> onSuccess.run();
        }
    }

    private static String escapeHtml(String text) {
        return text == null ? "" : text.replace("&", "&amp;")
                .replace("<", "&lt;").replace(">", "&gt;");
    }
}
