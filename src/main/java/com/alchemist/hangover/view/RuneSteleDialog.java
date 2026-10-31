package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.YmirRuneRitual;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Stele con la sequenza specifica della partita, da rileggere nel diario. */
public final class RuneSteleDialog extends JDialog {
    public RuneSteleDialog(Frame owner) {
        super(owner, "Stele della Rupe dell'Eco", true);
        setSize(760, 550);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        GameState state = GameState.getInstance();
        String inscription = YmirRuneRitual.steleInscription(YmirRuneRitual.current(state));
        state.setYmirSteleRead(true);

        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(new Color(26, 25, 34));
        root.setBorder(new EmptyBorder(23, 30, 20, 30));
        setContentPane(root);
        JLabel heading = new JLabel("LE PAROLE SCOLPITE NELLA ROCCIA", SwingConstants.CENTER);
        heading.setFont(new Font("Georgia", Font.BOLD, 23));
        heading.setForeground(new Color(255, 202, 108));
        root.add(heading, BorderLayout.NORTH);

        JTextArea message = new JTextArea(inscription);
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setFont(new Font("Georgia", Font.PLAIN, 18));
        message.setBackground(new Color(42, 37, 45));
        message.setForeground(new Color(244, 228, 196));
        message.setMargin(new Insets(18, 21, 16, 21));
        message.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(message);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(170, 124, 65)));
        root.add(scroll, BorderLayout.CENTER);

        JButton close = new JButton("ANNOTATO NEL DIARIO DI ARTHUR");
        close.setFont(new Font("Georgia", Font.BOLD, 14));
        close.setBackground(new Color(84, 61, 47));
        close.setForeground(new Color(255, 220, 163));
        close.setFocusPainted(false);
        close.setBorder(new EmptyBorder(14, 15, 14, 15));
        close.addActionListener(e -> dispose());
        root.add(close, BorderLayout.SOUTH);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
