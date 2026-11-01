package com.alchemist.hangover.view;

import com.alchemist.hangover.model.Item;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

/**
 * Reusable adventure-style choice and warning dialog for older chapters.
 * Unlike the Chapter III Ymir prompt, Chapters I and II do not yet have
 * dedicated illustration assets, so this is painted by Swing at runtime.
 *
 * Keeping the story UI separate lets upcoming ship NPCs reuse it.
 */
public final class StoryPromptDialog {
    public enum Theme {
        SKY("IL CIELO DI ASGARD", new Color(210, 159, 84), new Color(41, 42, 54)),
        OCEAN("LE PROFONDITA' DI RAN", new Color(103, 200, 215), new Color(24, 50, 61)),
        EARTH("IL DOMINIO DI YMIR", new Color(214, 153, 89), new Color(55, 39, 38));

        final String subtitle;
        final Color accent;
        final Color upperBackground;

        Theme(String subtitle, Color accent, Color upperBackground) {
            this.subtitle = subtitle;
            this.accent = accent;
            this.upperBackground = upperBackground;
        }
    }

    private StoryPromptDialog() { }

    /** One full-screen-style reward panel, including the item's real sprite. */
    public static void reward(Frame owner, StoryPromptDialog.Theme theme,
                              String title, String narrative, Item item) {
        JDialog dialog = new JDialog(owner, title, true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel panel = buildRewardContent(theme, title, narrative, item, dialog::dispose);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    /**
     * Non-window factory for layout tests; the actual action button remains
     * a Swing button with the same styling as the existing story choices.
     */
    static JPanel buildRewardContent(StoryPromptDialog.Theme theme, String title,
                                     String narrative, Item item, Runnable onContinue) {
        JPanel panel = StoryPromptDialog.buildContent(
                theme, title, narrative, "CONTINUA", null, accepted -> onContinue.run());
        panel.setName("styledRewardPanel");
        panel.setPreferredSize(new Dimension(750, 445));

        for (Component component : panel.getComponents()) {
            if ("storyPromptText".equals(component.getName())) {
                panel.remove(component);
                break;
            }
        }

        JPanel main = new JPanel(new BorderLayout(22, 0));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(8, 6, 5, 6));

        JPanel picture = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(new Color(theme.accent.getRed(),
                            theme.accent.getGreen(), theme.accent.getBlue(), 34));
                    g.fillRoundRect(7, 9, getWidth() - 14, getHeight() - 18, 20, 20);
                    g.setColor(new Color(theme.accent.getRed(),
                            theme.accent.getGreen(), theme.accent.getBlue(), 148));
                    g.setStroke(new BasicStroke(2));
                    g.drawRoundRect(7, 9, getWidth() - 14, getHeight() - 18, 20, 20);
                } finally {
                    g.dispose();
                }
            }
        };
        picture.setOpaque(false);
        picture.setPreferredSize(new Dimension(190, 190));

        JLabel icon = new JLabel("", SwingConstants.CENTER);
        icon.setName("bossRewardIcon");
        icon.setToolTipText(item == null ? null : item.getName());
        if (item != null && item.getIconPath() != null) {
            URL sprite = StoryPromptDialog.class.getResource(item.getIconPath());
            if (sprite != null) {
                ImageIcon sourceIcon = new ImageIcon(sprite);
                int sourceWidth = Math.max(1, sourceIcon.getIconWidth());
                int sourceHeight = Math.max(1, sourceIcon.getIconHeight());
                double scale = Math.min(150.0 / sourceWidth, 150.0 / sourceHeight);
                int width = Math.max(1, (int) Math.round(sourceWidth * scale));
                int height = Math.max(1, (int) Math.round(sourceHeight * scale));
                Image scaledImage = sourceIcon.getImage()
                        .getScaledInstance(width, height, Image.SCALE_SMOOTH);
                icon.setIcon(new ImageIcon(scaledImage));
            }
        }
        if (icon.getIcon() == null) {
            icon.setText("✦");
            icon.setFont(new Font("Georgia", Font.BOLD, 72));
            icon.setForeground(theme.accent);
        }
        picture.add(icon, BorderLayout.CENTER);
        main.add(picture, BorderLayout.WEST);

        JPanel description = new JPanel(new BorderLayout(0, 9));
        description.setOpaque(false);

        JLabel obtained = new JLabel("INGREDIENTE OTTENUTO");
        obtained.setFont(new Font("Georgia", Font.BOLD, 14));
        obtained.setForeground(theme.accent);
        description.add(obtained, BorderLayout.NORTH);

        String itemName = item == null ? "Ricompensa" : item.getName();
        String details = narrative + "\n\n" + itemName
                + (item != null && item.getDescription() != null
                    ? "\n" + item.getDescription() : "");
        JTextArea text = new JTextArea(details);
        text.setName("bossRewardDescription");
        text.setEditable(false);
        text.setFocusable(false);
        text.setOpaque(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(new Font("Georgia", Font.PLAIN, 18));
        text.setForeground(new Color(244, 228, 205));
        description.add(text, BorderLayout.CENTER);
        main.add(description, BorderLayout.CENTER);
        panel.add(main, BorderLayout.CENTER);
        panel.revalidate();
        return panel;
    }

    public static boolean confirm(Frame owner, Theme theme, String title,
                                  String message, String accept, String decline) {
        JDialog dialog = new JDialog(owner, title, true);
        final boolean[] accepted = {false};
        JPanel content = buildContent(theme, title, message, accept, decline, choice -> {
            accepted[0] = choice;
            dialog.dispose();
        });
        show(owner, dialog, content);
        return accepted[0];
    }

    public static void message(Frame owner, Theme theme, String title, String text) {
        JDialog dialog = new JDialog(owner, title, true);
        JPanel content = buildContent(theme, title, text, "HO CAPITO", null,
                choice -> dialog.dispose());
        show(owner, dialog, content);
    }

    /**
     * Package-private for headless UI tests; all actions are real Swing controls.
     * A null secondary label represents an informational prompt.
     */
    static JPanel buildContent(Theme theme, String title, String message,
                               String accept, String decline,
                               Consumer<Boolean> onChoice) {
        JPanel panel = new JPanel(new BorderLayout(0, 17)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setPaint(new GradientPaint(0, 0, theme.upperBackground,
                            getWidth(), getHeight(), new Color(17, 19, 30)));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                    g.setColor(theme.accent);
                    g.setStroke(new BasicStroke(2));
                    g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 24, 24);
                    g.setColor(new Color(theme.accent.getRed(), theme.accent.getGreen(),
                            theme.accent.getBlue(), 85));
                    g.drawLine(43, 101, getWidth() - 43, 101);
                } finally {
                    g.dispose();
                }
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(24, 40, 24, 40));
        panel.setPreferredSize(new Dimension(750, 348));

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 1));
        heading.setOpaque(false);
        JLabel caption = new JLabel(theme.subtitle, SwingConstants.CENTER);
        caption.setFont(new Font("Georgia", Font.BOLD, 13));
        caption.setForeground(theme.accent);
        heading.add(caption);
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 27));
        titleLabel.setForeground(new Color(255, 229, 184));
        heading.add(titleLabel);
        panel.add(heading, BorderLayout.NORTH);

        JTextArea body = new JTextArea(message);
        body.setName("storyPromptText");
        body.setEditable(false);
        body.setFocusable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setOpaque(false);
        body.setFont(new Font("Georgia", Font.PLAIN, 19));
        body.setForeground(new Color(242, 229, 207));
        body.setBorder(new EmptyBorder(12, 12, 6, 12));
        panel.add(body, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actions.setOpaque(false);

        JButton primary = button(accept, theme.accent, true);
        primary.addActionListener(e -> onChoice.accept(true));
        actions.add(primary);

        if (decline != null) {
            JButton secondary = button(decline, theme.accent, false);
            secondary.addActionListener(e -> onChoice.accept(false));
            actions.add(secondary);
            bind(panel, "ESCAPE", "declinePrompt", () -> secondary.doClick());
        } else {
            bind(panel, "ESCAPE", "dismissPrompt", () -> primary.doClick());
        }
        bind(panel, "ENTER", "acceptPrompt", () -> primary.doClick());
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private static JButton button(String label, Color accent, boolean primary) {
        JButton button = new JButton(label) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean glow = getModel().isRollover();
                    int alpha = glow ? 225 : primary ? 174 : 88;
                    g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(),
                            alpha));
                    g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 13, 13);
                    g.setColor(new Color(255, 232, 190, glow ? 245 : 115));
                    g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 13, 13);
                } finally {
                    g.dispose();
                }
                super.paintComponent(graphics);
            }
        };
        button.setName(primary ? "storyPromptAccept" : "storyPromptDecline");
        button.setFont(new Font("Georgia", Font.BOLD, 15));
        button.setForeground(new Color(21, 22, 30));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(244, 51));
        return button;
    }

    private static void bind(JComponent content, String key, String id, Runnable action) {
        content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(key), id);
        content.getActionMap().put(id, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private static void show(Frame owner, JDialog dialog, JPanel content) {
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}
