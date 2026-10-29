package com.alchemist.hangover.view;

import com.alchemist.hangover.dao.ItemDAO;
import com.alchemist.hangover.model.Item;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ItemPickupOverlay extends JDialog {

    private Image bgImage;
    private Item item;
    private Image itemIconImage;

    public ItemPickupOverlay(Frame parent, Item item) {
        super(parent, "Oggetto Trovato", true);
        this.item = item;
        initOverlay(parent);
    }

    public ItemPickupOverlay(Frame parent, String itemName) {
        super(parent, "Oggetto Trovato", true);
        Item resolved = ItemDAO.getItemById(itemName);
        if (resolved != null) {
            this.item = resolved;
        } else {
            this.item = new Item(itemName, itemName, "", null);
        }
        initOverlay(parent);
    }

    private void initOverlay(Frame parent) {
        setSize(750, 520);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        // Caricamento sfondo popup
        try {
            bgImage = new ImageIcon(getClass().getResource("/images/ui/item_pickup_bg.png")).getImage();
        } catch (Exception e) {
            System.err.println("Errore caricamento item_pickup_bg.png: " + e.getMessage());
        }

        // Caricamento icona dell'oggetto (senza glow, rendering pulito)
        if (item != null && item.getIconPath() != null) {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(item.getIconPath()));
                itemIconImage = icon.getImage().getScaledInstance(96, 96, Image.SCALE_SMOOTH);
            } catch (Exception e) {
                System.err.println("Icona oggetto non trovata: " + item.getIconPath());
            }
        }

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // 1. Sfondo Popup (ha già l'altare, il banner e il tasto OK disegnati)
                if (bgImage != null) {
                    g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2.setColor(new Color(25, 20, 30, 240));
                    g2.fillRoundRect(80, 50, getWidth() - 160, getHeight() - 100, 25, 25);
                }

                int centerX = getWidth() / 2;

                // 2. Icona Oggetto (senza alone o background giallo dietro)
                if (itemIconImage != null) {
                    int imgX = centerX - (itemIconImage.getWidth(null) / 2);
                    int imgY = 142;
                    g2.drawImage(itemIconImage, imgX, imgY, this);
                }

                // 3. Solo il Nome dell'Oggetto (senza descrizione)
                String nameText = (item != null ? item.getName() : "OGGETTO").toUpperCase();
                int nameFontSize = nameText.length() > 25 ? 16 : 20;
                Font nameFont = new Font("Georgia", Font.BOLD, nameFontSize);
                g2.setFont(nameFont);
                FontMetrics fmName = g2.getFontMetrics();
                int nameX = centerX - (fmName.stringWidth(nameText) / 2);
                int nameY = 290;

                // Ombra di contrasto e testo oro brillante
                g2.setColor(new Color(20, 15, 10, 220));
                g2.drawString(nameText, nameX - 1, nameY);
                g2.drawString(nameText, nameX + 1, nameY);
                g2.drawString(nameText, nameX, nameY - 1);
                g2.drawString(nameText, nameX, nameY + 1);

                g2.setColor(new Color(255, 230, 130));
                g2.drawString(nameText, nameX, nameY);
            }
        };
        contentPanel.setLayout(null);
        contentPanel.setOpaque(false);
        setContentPane(contentPanel);

        // Tasto OK sovrapposto esattamente alla targhetta 'OK' disegnata nell'immagine
        // Nessun testo scritto sopra: funziona cliccando sul tasto OK dell'immagine
        JButton btnOk = new JButton();
        btnOk.setBounds(275, 335, 200, 55);
        btnOk.setContentAreaFilled(false);
        btnOk.setBorderPainted(false);
        btnOk.setFocusPainted(false);
        btnOk.setFocusable(false);
        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOk.addActionListener(e -> dispose());
        contentPanel.add(btnOk);

        // Tasto X in alto a destra
        JButton btnClose = new JButton();
        btnClose.setBounds(620, 35, 45, 45);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setFocusable(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        contentPanel.add(btnClose);

        // Scorciatoie tastiera (ESC, INVIO o SPAZIO chiudono il popup)
        contentPanel.setFocusable(true);
        contentPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE ||
                    e.getKeyCode() == KeyEvent.VK_ENTER ||
                    e.getKeyCode() == KeyEvent.VK_SPACE) {
                    dispose();
                }
            }
        });

        // Cliccando fuori dal riquadro centrale si chiude
        contentPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getX() < 120 || e.getX() > 630 || e.getY() < 60 || e.getY() > 440) {
                    dispose();
                }
            }
        });
    }
}
