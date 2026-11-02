package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainMenuPanel extends JPanel {

    private Image backgroundImage;

    public MainMenuPanel() {
        setLayout(null); // Layout assoluto per posizionare i pulsanti sulla destra

        // Carica lo sfondo senza tasti inserito nella cartella risorse
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/images/intro/menu_bg.png")).getImage();
        } catch (Exception e) {
            System.err.println("Immagine di sfondo non trovata in /images/intro/menu_bg.jpg");
        }

        // Creazione del pannello contenitore dei pulsanti in colonna a destra
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 1, 0, 12));
        buttonPanel.setOpaque(false);
        JPanel menu = new JPanel(new BorderLayout(0, 16));
        menu.setBackground(new Color(24, 28, 29));
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(114, 92, 60)),
                BorderFactory.createEmptyBorder(18, 23, 18, 23)));
        menu.setBounds(896, 142, 298, 420);
        JLabel heading = new JLabel("TACCUINO   /   MENU PRINCIPALE");
        heading.setForeground(new Color(218, 175, 105));
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        menu.add(heading, BorderLayout.NORTH);
        menu.add(buttonPanel, BorderLayout.CENTER);

        // Definizione delle voci del menu
        String[] menuItems = {"Nuova partita", "Carica partita", "Come giocare", "Opzioni", "Crediti", "Esci"};

        for (String item : menuItems) {
            JButton btn = createCustomButton(item);
            buttonPanel.add(btn);
        }

        add(menu);
    }

    private JButton createCustomButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SERIF, Font.BOLD, 21));
        button.setForeground(new Color(200, 200, 200)); // Colore di base testo
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);

        // Gestione dell'effetto Hover illuminato al passaggio del mouse
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(new Color(255, 220, 100)); // Testo oro brillante
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(new Color(200, 200, 200)); // Ritorna al colore originale
            }
        });

        // Azioni dei pulsanti
        button.addActionListener(e -> {
            switch (text) {
                case "Nuova partita":
                    com.alchemist.hangover.model.GameState.getInstance().resetState();
                    ProloguePanel intro = (ProloguePanel)
                            SceneManager.getInstance().getPanel("INTRO");
                    if (intro != null) intro.restartDialogue();
                    SceneManager.getInstance().showScene("INTRO");
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
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}