package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

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
        buttonPanel.setBounds(850, 230, 300, 350); // Coordinate X, Y, Larghezza, Altezza

        // Definizione delle voci del menu
        String[] menuItems = {"START GAME", "LOAD GAME", "HOW TO PLAY", "OPTIONS", "CREDITS", "EXIT GAME"};

        for (String item : menuItems) {
            JButton btn = createCustomButton(item);
            buttonPanel.add(btn);
        }

        add(buttonPanel);
    }

    private JButton createCustomButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 22));
        button.setForeground(new Color(200, 200, 200)); // Colore di base testo
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.RIGHT);

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
                case "START GAME":
                    com.alchemist.hangover.model.GameState.getInstance().resetState();
                    SceneManager.getInstance().showScene("INTRO");
                    break;
                case "LOAD GAME":
                    JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                    SaveLoadDialog loadDialog = new SaveLoadDialog(topFrame, SaveLoadDialog.Mode.LOAD);
                    loadDialog.setVisible(true);
                    break;
                case "HOW TO PLAY":
                    JOptionPane.showMessageDialog(this,
                            "ESPLORAZIONE: clicca gli oggetti e usa le frecce.\n"
                            + "DIALOGHI: click, spazio o invio per avanzare.\n"
                            + "DIARIO: J. PAUSA: ESC.\n"
                            + "Usa il calderone per creare le ricette con gli ingredienti.",
                            "Come giocare", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case "OPTIONS":
                    new GameSettingsDialog((JFrame) SwingUtilities.getWindowAncestor(this)).setVisible(true);
                    break;
                case "EXIT GAME":
                    System.exit(0);
                    break;
                default:
                    System.out.println("Cliccato: " + text);
                    break;
            }
        });

        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}