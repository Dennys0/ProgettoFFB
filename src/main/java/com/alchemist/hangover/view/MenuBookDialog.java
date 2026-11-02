package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.GameSettings;
import java.awt.*;
import javax.swing.*;

/** Tre pagine dello stesso taccuino, accessibili anche dalla pausa. */
final class MenuBookDialog extends JDialog {
    enum Page {
        GUIDE("01   /   GUIDA", "Come giocare"),
        OPTIONS("02   /   PREFERENZE", "Opzioni"),
        CREDITS("03   /   PRODUZIONE", "Crediti");
        final String section;
        final String title;
        Page(String section, String title) {
            this.section = section;
            this.title = title;
        }
    }

    private static final Color INK = new Color(27, 31, 32);
    private static final Color GOLD = new Color(220, 179, 111);
    private static final Color PAPER = new Color(244, 233, 213);
    private static final Color MUTED = new Color(194, 195, 185);

    MenuBookDialog(Frame owner, Page page) {
        super(owner, page.title, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(pagePanel(page, this::dispose));
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    static JPanel pagePanel(Page page, Runnable close) {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(26, 32, 24, 32));
        panel.setBackground(INK);
        JPanel heading = column();
        heading.add(label("THE ALCHEMIST’S HANGOVER     /     " + page.section, 12, GOLD));
        heading.add(Box.createVerticalStrut(6));
        JLabel title = label(page.title, 32, PAPER);
        title.setFont(new Font(Font.SERIF, Font.BOLD, 32));
        heading.add(title);
        panel.add(heading, BorderLayout.NORTH);

        JPanel body = column();
        switch (page) {
            case GUIDE -> {
                row(body, "01    Esplora", "Usa le frecce sul fondale. Clicca sui reperti per raccoglierli.");
                row(body, "02    Ascolta", "Parla con chi incontri. Avanza con clic, spazio o invio.");
                row(body, "03    Miscela", "Gli indizi dei dialoghi ti aiutano a scegliere cosa versare nel calderone.");
                row(body, "A portata di mano", "J apre il diario. Esc apre il menu di pausa.");
            }
            case CREDITS -> {
                row(body, "Gioco MAP", "The Alchemist’s Hangover  ·  Avventura grafica in quattro capitoli");
                row(body, "Realizzazione tecnica", "Interfaccia in Java Swing e salvataggi su database H2.");
                row(body, "Paesaggio sonoro", "Ambienti, passi e accordi del calderone sono sintetizzati dal gioco.");
            }
            case OPTIONS -> {
                GameSettings settings = GameSettings.getInstance();
                JCheckBox sound = option(body, "Audio di gioco", settings.isSoundEnabled(),
                        "Attiva gli effetti e i suoni del mondo.");
                JCheckBox ambience = option(body, "Fondo ambientale", settings.isAmbienceEnabled(),
                        "Vento, mare, caverna e fornace durante l’esplorazione.");
                ambience.setEnabled(sound.isSelected());
                sound.addActionListener(e -> {
                    settings.setSoundEnabled(sound.isSelected());
                    ambience.setEnabled(sound.isSelected());
                });
                ambience.addActionListener(e -> settings.setAmbienceEnabled(ambience.isSelected()));
                JCheckBox fade = option(body, "Dissolvenze", settings.isTransitionsEnabled(),
                        "Accompagna i cambi di scena con una transizione.");
                fade.addActionListener(e -> settings.setTransitionsEnabled(fade.isSelected()));
            }
        }
        panel.add(body, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(24, 0));
        footer.setOpaque(false);
        footer.add(label(page == Page.OPTIONS ? "Preferenze salvate su questo dispositivo." :
                "Taccuino di Arthur", 13, MUTED), BorderLayout.WEST);
        JButton back = new JButton("Chiudi   ·   Esc");
        back.setBackground(GOLD);
        back.setForeground(INK);
        back.addActionListener(e -> close.run());
        footer.add(back, BorderLayout.EAST);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private static JPanel column() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        return panel;
    }

    private static JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, size));
        return label;
    }

    private static void row(JPanel body, String title, String description) {
        body.add(label(title, 18, GOLD));
        body.add(Box.createVerticalStrut(3));
        body.add(label(description, 15, PAPER));
        body.add(Box.createVerticalStrut(13));
    }

    private static JCheckBox option(JPanel body, String title, boolean selected, String detail) {
        JCheckBox box = new JCheckBox(title, selected);
        box.setOpaque(false);
        box.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        box.setForeground(GOLD);
        body.add(box);
        body.add(label(detail, 15, PAPER));
        body.add(Box.createVerticalStrut(12));
        return box;
    }
}
