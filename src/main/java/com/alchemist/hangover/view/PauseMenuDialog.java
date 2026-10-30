package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Menu accessibile con ESC, senza modificare i salvataggi. */
public final class PauseMenuDialog extends JDialog {
    public PauseMenuDialog(JFrame owner) {
        super(owner, "Pausa", true);
        setSize(470, 480);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(new Color(24, 22, 35));
        root.setBorder(new EmptyBorder(25, 40, 24, 40));

        JLabel heading = new JLabel("PAUSA", SwingConstants.CENTER);
        heading.setFont(new Font("Georgia", Font.BOLD, 30));
        heading.setForeground(new Color(240, 206, 120));
        root.add(heading, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(6, 1, 0, 12));
        actions.setOpaque(false);
        actions.add(button("Riprendi", () -> dispose()));
        actions.add(button("Salva partita", () -> new SaveLoadDialog(owner, SaveLoadDialog.Mode.SAVE).setVisible(true)));
        actions.add(button("Diario di Arthur", () -> new GameJournalDialog(owner).setVisible(true)));
        actions.add(button("Impostazioni", () -> new GameSettingsDialog(owner).setVisible(true)));
        actions.add(button("Comandi", () -> JOptionPane.showMessageDialog(this,
                "Frecce e hotspot: esplora le stanze\n"
                + "Click / SPAZIO / INVIO: avanza dialogo\n"
                + "J: diario di Arthur\nESC: pausa o chiudi",
                "Comandi", JOptionPane.INFORMATION_MESSAGE)));
        actions.add(button("Torna al menu", () -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "I progressi non salvati andranno persi. Tornare al menu?",
                    "Conferma", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                GameState.getInstance().resetState();
                SceneManager.getInstance().showScene("MENU");
            }
        }));
        root.add(actions, BorderLayout.CENTER);
        setContentPane(root);

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Georgia", Font.BOLD, 16));
        button.setForeground(new Color(250, 227, 174));
        button.setBackground(new Color(49, 42, 58));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> action.run());
        return button;
    }
}
