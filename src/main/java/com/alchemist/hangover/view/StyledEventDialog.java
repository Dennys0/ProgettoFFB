package com.alchemist.hangover.view;

import com.alchemist.hangover.model.Item;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Shared cinematic event dialogs for the first two chapters.
 * Reuses the exact visual language of StoryPromptDialog (the golden
 * "Guardian of the Storms" panel) for choices, boss results and loot.
 */
public final class StyledEventDialog {
    public enum Mode { CONFIRM, REWARD, INFO }

    private StyledEventDialog() { }

    public static boolean confirm(Frame owner, StoryPromptDialog.Theme theme, String title,
                                  String message, String accept, String cancel) {
        return StoryPromptDialog.confirm(owner, theme, title, message, accept, cancel);
    }

    /** Single-button notice styled like the cinematic boss invitation. */
    public static void info(Frame owner, StoryPromptDialog.Theme theme,
                            String title, String message) {
        StoryPromptDialog.message(owner, theme, title, message);
    }

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
            URL sprite = StyledEventDialog.class.getResource(item.getIconPath());
            if (sprite != null) {
                Image image = new ImageIcon(sprite).getImage()
                        .getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                icon.setIcon(new ImageIcon(image));
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
}
