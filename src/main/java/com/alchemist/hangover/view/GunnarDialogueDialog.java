package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.DialogueSystem;
import com.alchemist.hangover.model.GameState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Gunnar insegna a riconoscere Radice, Battito e Respiro.
 * Interrompere la conversazione NON concede l'indizio; il giocatore puo'
 * riprovare in qualsiasi momento. Rivelare una singola frase non e' skip.
 */
public final class GunnarDialogueDialog extends JDialog {
    private final DialogueSystem dialogue;
    private boolean completedNormally;

    public GunnarDialogueDialog(Frame owner) {
        super(owner, "Gunnar — L'Eremita delle Rune", true);
        setSize(970, 570);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel body = new JPanel(new BorderLayout(20, 15));
        body.setBackground(new Color(24, 24, 33));
        body.setBorder(new EmptyBorder(22, 22, 18, 22));
        setContentPane(body);

        JLabel title = new JLabel("IL CERCHIO DELLE RUNE — LA LEZIONE DI GUNNAR");
        title.setFont(new Font("Georgia", Font.BOLD, 21));
        title.setForeground(new Color(239, 199, 115));
        body.add(title, BorderLayout.NORTH);

        JLabel spriteLabel = new JLabel("GUNNAR", SwingConstants.CENTER);
        spriteLabel.setForeground(new Color(230, 196, 132));
        spriteLabel.setPreferredSize(new Dimension(305, 395));
        URL sprite = getClass().getResource("/images/characters/gunnar_realms.png");
        if (sprite != null) {
            spriteLabel.setIcon(new ImageIcon(new ImageIcon(sprite).getImage()
                    .getScaledInstance(305, 395, Image.SCALE_SMOOTH)));
            spriteLabel.setText("");
        }
        body.add(spriteLabel, BorderLayout.WEST);

        JPanel conversation = new JPanel(new BorderLayout(0, 15));
        conversation.setOpaque(false);
        conversation.setBorder(new EmptyBorder(14, 8, 14, 8));
        JLabel speaker = new JLabel();
        speaker.setFont(new Font("Georgia", Font.BOLD, 23));
        speaker.setForeground(new Color(255, 213, 130));
        conversation.add(speaker, BorderLayout.NORTH);

        JLabel text = new JLabel();
        text.setVerticalAlignment(SwingConstants.TOP);
        text.setFont(new Font("Georgia", Font.PLAIN, 19));
        text.setForeground(new Color(245, 234, 211));
        conversation.add(text, BorderLayout.CENTER);

        dialogue = new DialogueSystem(speaker, text, null, () -> {
            completedNormally = true;
            GameState.getInstance().setGunnarWisdomLearned(true);
            dispose();
        });
        dialogue.loadDialogue("/dialogues/ch3_gunnar.txt");

        JPanel buttons = new JPanel(new GridLayout(1, 3, 10, 0));
        buttons.setOpaque(false);
        JButton reveal = button("MOSTRA FRASE");
        reveal.setToolTipText("Mostra il testo completo della frase corrente");
        reveal.addActionListener(e -> dialogue.revealCurrentLine());
        JButton next = button("ASCOLTA / CONTINUA");
        next.addActionListener(e -> dialogue.nextLine());
        JButton interrupt = button("INTERROMPI");
        interrupt.addActionListener(e -> interrupt());
        buttons.add(reveal);
        buttons.add(next);
        buttons.add(interrupt);
        conversation.add(buttons, BorderLayout.SOUTH);
        body.add(conversation, BorderLayout.CENTER);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "continueGunnar");
        getRootPane().getActionMap().put("continueGunnar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { dialogue.nextLine(); }
        });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "interruptGunnar");
        getRootPane().getActionMap().put("interruptGunnar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { interrupt(); }
        });
        dialogue.startDialogue();
    }

    private static JButton button(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("Georgia", Font.BOLD, 12));
        button.setForeground(new Color(255, 225, 178));
        button.setBackground(new Color(65, 47, 55));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(188, 146, 77)),
                new EmptyBorder(12, 5, 12, 5)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        return button;
    }

    private void interrupt() {
        dialogue.stop();
        if (!completedNormally) {
            // Non modificare GameState; una visita successiva riparte da capo.
            System.out.println("[Gunnar] Arthur interrompe il monologo: nessun indizio ottenuto.");
        }
        dispose();
    }

    @Override
    public void dispose() {
        if (dialogue != null) dialogue.stop();
        super.dispose();
    }
}
