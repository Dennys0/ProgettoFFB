package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
import com.alchemist.hangover.model.YmirCauldronRecipe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Postazione alchemica SOVRAPPOSTA alla stanza 3: non usa un altro sfondo.
 * Nel mondo restano visibili i contorni e il boccale di room3_forge.png.
 */
public final class YmirCauldronPanel extends JPanel {
    private static final Color GOLD = new Color(242, 192, 116);
    private final Set<String> selected = new LinkedHashSet<>();
    private final JPanel cards = new JPanel(new GridLayout(2, 3, 10, 10));
    private final JLabel selectionLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
    private final Runnable onClose;
    private final Runnable onSuccess;
    private final Consumer<String> onDisaster;

    public YmirCauldronPanel(Runnable onClose, Runnable onSuccess, Consumer<String> onDisaster) {
        this.onClose = onClose;
        this.onSuccess = onSuccess;
        this.onDisaster = onDisaster;
        setOpaque(false);
        setFocusable(true);
        setLayout(new BorderLayout(14, 12));
        setBorder(new EmptyBorder(24, 29, 22, 29));

        JLabel heading = new JLabel("IL BOCCALE DI PIETRA — YMIR'S OLD FASHIONED", SwingConstants.CENTER);
        heading.setFont(new Font("Georgia", Font.BOLD, 23));
        heading.setForeground(GOLD);
        add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        JLabel note = new JLabel("Scegli tre ingredienti per la miscela della Terra", SwingConstants.CENTER);
        note.setFont(new Font("Georgia", Font.ITALIC, 16));
        note.setForeground(new Color(231, 223, 202));
        center.add(note, BorderLayout.NORTH);

        cards.setOpaque(false);
        center.add(cards, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 9));
        bottom.setOpaque(false);
        selectionLabel.setForeground(new Color(246, 218, 174));
        selectionLabel.setFont(new Font("Georgia", Font.PLAIN, 14));
        bottom.add(selectionLabel, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(1, 3, 10, 0));
        actions.setOpaque(false);
        JButton mix = makeButton("MESCOLA");
        mix.addActionListener(e -> mix());
        JButton reset = makeButton("AZZERA");
        reset.addActionListener(e -> { selected.clear(); refreshIngredients(); });
        JButton exit = makeButton("CHIUDI");
        exit.addActionListener(e -> onClose.run());
        actions.add(mix);
        actions.add(reset);
        actions.add(exit);
        bottom.add(actions, BorderLayout.CENTER);

        statusLabel.setForeground(new Color(239, 206, 146));
        statusLabel.setFont(new Font("Georgia", Font.PLAIN, 13));
        bottom.add(statusLabel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "closeBrewing");
        getActionMap().put("closeBrewing", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (isVisible()) onClose.run();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(20, 18, 28, 240));
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 25, 25);
        } finally {
            g.dispose();
        }
    }

    public void refreshIngredients() {
        cards.removeAll();
        Set<String> available = new LinkedHashSet<>();
        for (Item item : GameState.getInstance().getInventory()) {
            if (!YmirCauldronRecipe.ALL.contains(item.getId())) continue;
            available.add(item.getId());
            JButton button = makeButton("<html><center>" + html(item.getName()) + "</center></html>");
            button.setToolTipText(item.getDescription());
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.BOTTOM);
            URL image = getClass().getResource(item.getIconPath());
            if (image != null) {
                Image icon = new ImageIcon(image).getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(icon));
            }
            button.setBackground(selected.contains(item.getId())
                    ? new Color(112, 80, 48) : new Color(53, 48, 58));
            button.addActionListener(e -> {
                if (selected.contains(item.getId())) {
                    selected.remove(item.getId());
                } else if (selected.size() < 3) {
                    selected.add(item.getId());
                } else {
                    statusLabel.setText("Tre ingredienti al massimo. Deselezionane uno.");
                }
                refreshIngredients();
            });
            cards.add(button);
        }
        selected.retainAll(available);
        int owned = available.size();
        for (int i = owned; i < 6; i++) {
            JLabel placeholder = new JLabel(i == 0 ? "Nessun ingrediente" : "—", SwingConstants.CENTER);
            placeholder.setForeground(new Color(137, 127, 124));
            cards.add(placeholder);
        }
        selectionLabel.setText("Ingredienti nel boccale: " + selected.size() + " / 3");
        cards.revalidate();
        cards.repaint();
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

    private static String html(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static JButton makeButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(53, 48, 58));
        button.setForeground(new Color(251, 225, 176));
        button.setFont(new Font("Georgia", Font.BOLD, 13));
        button.setBorder(BorderFactory.createLineBorder(new Color(181, 134, 75), 2));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}
