package com.alchemist.hangover.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;

/** Piccola notifica non modale: non interrompe l'esplorazione. */
public final class ToastNotification {
    private ToastNotification() { }

    public static void show(Component source, String message, String iconPath) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> show(source, message, iconPath));
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(source);
        if (owner == null || !owner.isShowing()) return;

        JWindow window = new JWindow(owner);
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(new Color(28, 23, 39));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 175, 75), 2),
                new EmptyBorder(12, 18, 12, 18)));

        if (iconPath != null) {
            URL url = ToastNotification.class.getResource(iconPath);
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(44, 44, Image.SCALE_SMOOTH);
                panel.add(new JLabel(new ImageIcon(img)), BorderLayout.WEST);
            }
        }
        JLabel label = new JLabel(message);
        label.setFont(new Font("Georgia", Font.BOLD, 16));
        label.setForeground(new Color(255, 230, 170));
        panel.add(label, BorderLayout.CENTER);

        window.setContentPane(panel);
        window.pack();
        window.setLocation(owner.getX() + (owner.getWidth() - window.getWidth()) / 2,
                owner.getY() + owner.getHeight() - window.getHeight() - 80);
        window.setVisible(true);

        Timer timer = new Timer(2400, e -> window.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    public static void show(Component source, String message) {
        show(source, message, null);
    }
}
