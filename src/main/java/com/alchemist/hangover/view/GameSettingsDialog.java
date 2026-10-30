package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.GameSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Preferenze grafiche e audio, accessibili dal menu principale e dalla pausa. */
public final class GameSettingsDialog extends JDialog {
    public GameSettingsDialog(Frame owner) {
        super(owner, "Impostazioni", true);
        setSize(510, 320);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 12));
        panel.setBorder(new EmptyBorder(25, 35, 25, 35));
        panel.setBackground(new Color(30, 26, 40));

        GameSettings settings = GameSettings.getInstance();
        JCheckBox sound = new JCheckBox("Audio ed effetti sonori", settings.isSoundEnabled());
        JCheckBox fade = new JCheckBox("Transizioni cinematografiche", settings.isTransitionsEnabled());
        for (JCheckBox option : new JCheckBox[]{sound, fade}) {
            option.setOpaque(false);
            option.setForeground(new Color(245, 222, 172));
            option.setFont(new Font("Georgia", Font.PLAIN, 17));
            option.setFocusPainted(false);
            panel.add(option);
        }

        sound.addActionListener(e -> settings.setSoundEnabled(sound.isSelected()));
        fade.addActionListener(e -> settings.setTransitionsEnabled(fade.isSelected()));

        JLabel info = new JLabel("Le preferenze vengono conservate su questo computer.");
        info.setForeground(Color.LIGHT_GRAY);
        panel.add(info);
        JButton close = new JButton("Chiudi");
        close.addActionListener(e -> dispose());
        panel.add(close);
        setContentPane(panel);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
