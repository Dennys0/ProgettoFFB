package com.alchemist.hangover.view.menu;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.view.story.ProloguePanel;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class MainMenuPanel extends JPanel {

    private final Image backgroundImage = new ImageIcon(getClass().getResource("/images/intro/menu_bg.png")).getImage();
    private final JLabel hint = new JLabel("", SwingConstants.CENTER) {
        @Override protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }
    };
    private final JPanel entries = new JPanel(new GridLayout(6, 1, 0, 4));
    private JButton selected;

    public MainMenuPanel() {
        setLayout(null);
        entries.setOpaque(false);
        add(entries);
        hint.setOpaque(false);
        hint.setBackground(new Color(13, 13, 13, 208));
        hint.setForeground(new Color(238, 226, 205));
        hint.setFont(new Font(Font.SERIF, Font.PLAIN, 17));
        add(hint);
        String[][] actions = {
                {"Nuova partita", "Comincia la storia di Arthur."},
                {"Carica partita", "Riprendi il viaggio da un salvataggio."},
                {"Come giocare", "Consulta i comandi di esplorazione, dialogo e alchimia."},
                {"Opzioni", "Regola l’audio e le dissolvenze."},
                {"Crediti", "Sfoglia i crediti di The Alchemist’s Hangover."},
                {"Esci", "Chiudi il gioco."}
        };
        for (String[] action : actions) entries.add(createCustomButton(action[0], action[1]));
        select((JButton) entries.getComponent(0));
        bind("UP", () -> step(-1));
        bind("DOWN", () -> step(1));
        bind("ENTER", () -> selected.doClick());
    }

    private void bind(String key, Runnable action) {
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(key), key);
        getActionMap().put(key, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { action.run(); }
        });
    }

    @Override public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(() -> { if (isShowing()) selected.requestFocusInWindow(); });
    }

    private void step(int delta) {
        int i = (entries.getComponentZOrder(selected) + delta + entries.getComponentCount())
                % entries.getComponentCount();
        ((JButton) entries.getComponent(i)).requestFocusInWindow();
    }

    private void select(JButton button) {
        if (selected != null) selected.repaint();
        selected = button;
        selected.repaint();
        hint.setText(button.getAccessibleContext().getAccessibleDescription());
    }

    @Override public void doLayout() {
        int w = getWidth(), h = getHeight();
        entries.setBounds(w * 68 / 100, h * 26 / 100, w * 27 / 100, h * 48 / 100);
        hint.setBounds(0, h * 84 / 100, w, h * 8 / 100);
    }

    private JButton createCustomButton(String text, String description) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                boolean active = this == selected;
                g.setPaint(new GradientPaint(0, 0, active ? new Color(112, 55, 16, 240)
                        : new Color(8, 10, 11, 225), getWidth(), 0,
                        active ? new Color(56, 29, 12, 224) : new Color(17, 18, 18, 178)));
                g.fillRect(0, 0, getWidth(), getHeight());
                if (active) {
                    g.setColor(new Color(219, 161, 72));
                    g.drawRect(0, 0, getWidth()-1, getHeight()-1);
                    g.fillRect(0, 0, 4, getHeight());
                }
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 8));
        button.setForeground(new Color(249, 237, 210));
        button.setFont(new Font(Font.SERIF, Font.PLAIN, 22));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.getAccessibleContext().setAccessibleDescription(description);
        button.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { select(button); }
        });
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                button.requestFocusInWindow();
                select(button);
            }
        });

        // Azioni dei pulsanti
        button.addActionListener(e -> {
            switch (text) {
                case "Nuova partita":
                    com.alchemist.hangover.model.GameState.getInstance().resetState();
                    ProloguePanel intro = (ProloguePanel)
                            SceneManager.getInstance().getPanel("INTRO");
                    SceneManager.getInstance().showScene("INTRO", () -> {
                        if (intro != null) intro.restartDialogue();
                    });
                    break;
                case "Carica partita":
                    JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                    SaveLoadDialog loadDialog = new SaveLoadDialog(topFrame, SaveLoadDialog.Mode.LOAD);
                    loadDialog.setVisible(true);
                    break;
                case "Come giocare":
                    openPage(MenuBookDialog.Page.GUIDE);
                    break;
                case "Opzioni":
                    openPage(MenuBookDialog.Page.OPTIONS);
                    break;
                case "Crediti":
                    openPage(MenuBookDialog.Page.CREDITS);
                    break;
                case "Esci":
                    System.exit(0);
                    break;
                default:
                    System.out.println("Cliccato: " + text);
                    break;
            }
        });

        return button;
    }

    private void openPage(MenuBookDialog.Page page) {
        new MenuBookDialog((Frame) SwingUtilities.getWindowAncestor(this), page).setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            double scale = Math.max((double) getWidth() / backgroundImage.getWidth(this),
                    (double) getHeight() / backgroundImage.getHeight(this));
            int w = (int) Math.ceil(backgroundImage.getWidth(this) * scale);
            int h = (int) Math.ceil(backgroundImage.getHeight(this) * scale);
            g.drawImage(backgroundImage, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, this);
        }
    }
}