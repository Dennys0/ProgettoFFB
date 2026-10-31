package com.alchemist.hangover.view;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Two illustrated, fully interactive in-game Ymir prompts.
 *
 * The shipped PNGs contain the entire calligraphic layout and the text. We crop
 * the illustrated stone panels out of those images at runtime and place real
 * Swing buttons over the baked buttons. The game never changes the PNG files.
 *
 * When Arthur has already learned one clue, a dynamic dialog shows ONLY the
 * remaining clue (the baked warning lists both and would be misleading).
 */
public final class YmirStoryPrompt {
    private static final String FIGHT = "/images/ui/ymir_fight.png";
    private static final String WARNING = "/images/ui/ymir_warning.png";
    private static final int ART_WIDTH = 960;
    private static final int ART_HEIGHT = 480;

    private static final Rectangle FIGHT_CROP = new Rectangle(125, 100, 1418, 705);
    private static final Rectangle WARNING_CROP = new Rectangle(150, 144, 1345, 670);

    private YmirStoryPrompt() { }

    public static boolean confirmFight(Frame owner) {
        JDialog dialog = new JDialog(owner, "Il Cuore di Roccia — Ymir", true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        final boolean[] accepted = {false};

        IllustratedPanel panel = new IllustratedPanel(FIGHT, FIGHT_CROP);
        JButton fight = actionButton("AFFRONTA YMIR",
                hotspot(330, 611, 543, 116, FIGHT_CROP));
        JButton retreat = actionButton("NON ANCORA",
                hotspot(887, 611, 468, 116, FIGHT_CROP));
        JButton dismiss = actionButton("CHIUDI",
                hotspot(1417, 150, 108, 100, FIGHT_CROP));

        fight.addActionListener(e -> {
            accepted[0] = true;
            dialog.dispose();
        });
        retreat.addActionListener(e -> dialog.dispose());
        dismiss.addActionListener(e -> dialog.dispose());
        panel.add(fight);
        panel.add(retreat);
        panel.add(dismiss);

        bind(panel, "ENTER", "fightYmir", () -> fight.doClick());
        bind(panel, "ESCAPE", "retreatYmir", () -> retreat.doClick());
        showDialog(owner, dialog, panel);
        return accepted[0];
    }

    public static void showMissingClues(Frame owner, boolean needGunnar, boolean needStele) {
        if (!needGunnar && !needStele) return;
        if (needGunnar && needStele) {
            JDialog dialog = new JDialog(owner,
                    "Il sonno di Ymir non puo' essere spezzato", true);
            dialog.setUndecorated(true);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            IllustratedPanel panel = new IllustratedPanel(WARNING, WARNING_CROP);
            JButton acknowledge = actionButton("OK",
                    hotspot(650, 655, 384, 115, WARNING_CROP));
            acknowledge.addActionListener(e -> dialog.dispose());
            panel.add(acknowledge);
            bind(panel, "ENTER", "acknowledge", () -> acknowledge.doClick());
            bind(panel, "ESCAPE", "cancel", () -> dialog.dispose());
            showDialog(owner, dialog, panel);
            return;
        }
        // Specific story progress must not show a contradictory baked PNG:
        // render matching colors and frame with the actual missing objective.
        showDynamicWarning(owner, needGunnar
                ? "Devi ascoltare Gunnar fino alla fine nel Cerchio delle Rune."
                : "Devi leggere la stele della Rupe dell'Eco.");
    }

    private static void showDynamicWarning(Frame owner, String missingClue) {
        JDialog dialog = new JDialog(owner, "Il sonno di Ymir", true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(14, 20)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setPaint(new GradientPaint(0, 0, new Color(55, 39, 33),
                            getWidth(), getHeight(), new Color(20, 19, 25)));
                    g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 18, 18);
                    g.setColor(new Color(213, 134, 65));
                    g.setStroke(new BasicStroke(3));
                    g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 18, 18);
                } finally {
                    g.dispose();
                }
            }
        };
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(31, 43, 25, 43));
        JLabel title = new JLabel("IL SONNO DI YMIR NON PUO' ESSERE SPEZZATO",
                SwingConstants.CENTER);
        title.setForeground(new Color(255, 205, 138));
        title.setFont(new Font("Georgia", Font.BOLD, 23));
        content.add(title, BorderLayout.NORTH);

        JLabel clue = new JLabel("<html><center>"
                + "Il gigante si muove e la roccia trema sotto i tuoi piedi.<br><br>"
                + escapeHtml(missingClue) + "<br><br>"
                + "Raccogli l'indizio prima di iniziare il rituale."
                + "</center></html>", SwingConstants.CENTER);
        clue.setForeground(new Color(244, 223, 193));
        clue.setFont(new Font("Georgia", Font.PLAIN, 20));
        content.add(clue, BorderLayout.CENTER);

        JButton close = new JButton("HO CAPITO");
        close.setFont(new Font("Georgia", Font.BOLD, 18));
        close.setForeground(new Color(255, 224, 172));
        close.setBackground(new Color(104, 64, 44));
        close.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 141, 70), 2),
                new EmptyBorder(12, 22, 12, 22)));
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.add(close);
        content.add(footer, BorderLayout.SOUTH);
        bind(content, "ENTER", "close", () -> close.doClick());
        bind(content, "ESCAPE", "cancel", () -> dialog.dispose());

        content.setPreferredSize(new Dimension(860, 360));
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    static Rectangle hotspot(int sourceX, int sourceY, int sourceWidth, int sourceHeight,
                             Rectangle crop) {
        return new Rectangle(
                (int) Math.round((sourceX - crop.x) * (ART_WIDTH / (double) crop.width)),
                (int) Math.round((sourceY - crop.y) * (ART_HEIGHT / (double) crop.height)),
                (int) Math.round(sourceWidth * ART_WIDTH / (double) crop.width),
                (int) Math.round(sourceHeight * ART_HEIGHT / (double) crop.height));
    }

    static Rectangle fightCrop() { return new Rectangle(FIGHT_CROP); }
    static Rectangle warningCrop() { return new Rectangle(WARNING_CROP); }

    static boolean hasArtwork(String path) {
        return YmirStoryPrompt.class.getResource(path) != null;
    }

    private static JButton actionButton(String accessibleName, Rectangle bounds) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics graphics) {
                // The button and label are already painted in the illustration.
                // Only a subtle glow is drawn on hover, without a yellow frame.
                if (getModel().isRollover()) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    try {
                        g.setColor(new Color(255, 205, 113, 43));
                        g.fillRoundRect(5, 5, Math.max(0, getWidth() - 10),
                                Math.max(0, getHeight() - 10), 18, 18);
                    } finally {
                        g.dispose();
                    }
                }
            }
        };
        button.setBounds(bounds);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText(accessibleName);
        button.getAccessibleContext().setAccessibleName(accessibleName);
        return button;
    }

    private static void bind(JComponent panel, String keystroke, String action,
                             Runnable callback) {
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keystroke), action);
        panel.getActionMap().put(action, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { callback.run(); }
        });
    }

    private static void showDialog(Frame owner, JDialog dialog, IllustratedPanel panel) {
        panel.setPreferredSize(new Dimension(ART_WIDTH, ART_HEIGHT));
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private static String escapeHtml(String raw) {
        return raw.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static final class IllustratedPanel extends JPanel {
        private final BufferedImage artwork;
        private final Rectangle crop;

        IllustratedPanel(String resource, Rectangle crop) {
            setLayout(null);
            setOpaque(false);
            this.crop = new Rectangle(crop);
            this.artwork = read(resource);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                if (artwork != null) {
                    double x = artwork.getWidth() / 1672.0;
                    double y = artwork.getHeight() / 941.0;
                    g.drawImage(artwork, 0, 0, getWidth(), getHeight(),
                            (int) (crop.x * x), (int) (crop.y * y),
                            (int) ((crop.x + crop.width) * x),
                            (int) ((crop.y + crop.height) * y), this);
                } else {
                    g.setPaint(new GradientPaint(0, 0, new Color(53, 39, 33),
                            getWidth(), getHeight(), new Color(23, 20, 27)));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(new Color(213, 139, 70));
                    g.setStroke(new BasicStroke(4));
                    g.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 18, 18);
                }
            } finally {
                g.dispose();
            }
        }

        private static BufferedImage read(String path) {
            URL url = YmirStoryPrompt.class.getResource(path);
            if (url == null) {
                System.err.println("[YmirStoryPrompt] Missing image: " + path);
                return null;
            }
            try {
                return ImageIO.read(url);
            } catch (IOException ex) {
                System.err.println("[YmirStoryPrompt] Unable to load " + path + ": " + ex);
                return null;
            }
        }
    }
}
