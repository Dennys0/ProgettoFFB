package com.alchemist.hangover.view;

import com.alchemist.hangover.model.Item;
import javax.swing.*;
import java.awt.*;

public class ItemPickupOverlay extends JDialog {

    private Image bgImage;
    private Image itemIcon;
    private String itemName;

    // Costruttore 1: tramite oggetto Item
    public ItemPickupOverlay(Frame parent, Item item) {
        this(parent, item != null ? item.getName() : "", item != null ? item.getIconPath() : null);
    }

    // Costruttore 2: tramite nome dell'oggetto
    public ItemPickupOverlay(Frame parent, String itemName) {
        this(parent, itemName, null);
    }

    // Costruttore 3: tramite nome dell'oggetto e percorso icona (Aggiunto)
    public ItemPickupOverlay(Frame parent, String itemName, String iconPath) {
        super(parent, "Oggetto Trovato", true);
        this.itemName = itemName;
        setSize(750, 520);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        try {
            bgImage = new ImageIcon(getClass().getResource("/images/ui/item_pickup_bg.png")).getImage();
        } catch (Exception e) {
            System.err.println("Errore caricamento item_pickup_bg.png: " + e.getMessage());
        }

        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                itemIcon = new ImageIcon(getClass().getResource(iconPath)).getImage();
            } catch (Exception e) {
                System.err.println("Errore caricamento icona oggetto: " + e.getMessage());
            }
        }

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (bgImage != null) {
                    g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }

                if (itemIcon != null) {
                    g2.drawImage(itemIcon, (getWidth() - 80) / 2, 175, 80, 80, this);
                }

                if (ItemPickupOverlay.this.itemName != null) {
                    String text = ItemPickupOverlay.this.itemName.toUpperCase();
                    Font font = new Font("Georgia", Font.BOLD, 20);
                    g2.setFont(font);
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = 290;

                    g2.setColor(new Color(212, 175, 55));
                    g2.drawString(text, x - 1, y);
                    g2.drawString(text, x + 1, y);
                    g2.drawString(text, x, y - 1);
                    g2.drawString(text, x, y + 1);

                    g2.setColor(Color.WHITE);
                    g2.drawString(text, x, y);
                }
            }
        };
        contentPanel.setLayout(null);
        contentPanel.setOpaque(false);
        setContentPane(contentPanel);

        JButton btnOk = new JButton();
        btnOk.setBounds(275, 335, 200, 55);
        btnOk.setContentAreaFilled(false);
        btnOk.setBorderPainted(false);
        btnOk.setFocusPainted(false);
        btnOk.setFocusable(false);
        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOk.addActionListener(e -> dispose());
        contentPanel.add(btnOk);

        JButton btnClose = new JButton();
        btnClose.setBounds(620, 35, 45, 45);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setFocusable(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        contentPanel.add(btnClose);
    }
}