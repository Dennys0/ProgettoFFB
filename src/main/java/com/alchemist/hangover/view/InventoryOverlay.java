package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class InventoryOverlay extends JDialog {

    private Image backgroundPattern;
    private JPanel gridPanel;
    private JLabel descLabel;
    private JButton closeButton;
    private Item selectedItem;
    private JPanel selectedSlotPanel;

    public InventoryOverlay(Frame parent) {
        super(parent, "INVENTARIO", true);
        setSize(980, 680);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        // Caricamento immagine di sfondo
        try {
            backgroundPattern = new ImageIcon(getClass().getResource("/images/ui/inventory_bg.png")).getImage();
        } catch (Exception e) {
            System.err.println("Errore nel caricamento di inventory_bg.png: " + e.getMessage());
        }

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                if (backgroundPattern != null) {
                    g2.drawImage(backgroundPattern, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2.setColor(new Color(25, 22, 35, 245));
                    g2.fillRoundRect(80, 40, getWidth() - 160, getHeight() - 80, 30, 30);
                }
            }
        };
        contentPanel.setLayout(null);
        contentPanel.setOpaque(false);
        setContentPane(contentPanel);

        // 1. Pulsante di Chiusura 'X' (Sulla gemma rossa in alto a destra)
        closeButton = new JButton();
        closeButton.setBounds(810, 35, 90, 90);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setFocusable(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());
        contentPanel.add(closeButton);

        // 2. Griglia degli Oggetti (4 Righe x 8 Colonne, centrata sui riquadri dello sfondo)
        gridPanel = new JPanel(new GridLayout(4, 8, 12, 12));
        gridPanel.setOpaque(false);
        gridPanel.setBounds(205, 195, 570, 300);
        contentPanel.add(gridPanel);

        // 3. Area Descrizioni (Targa in basso)
        descLabel = new JLabel("", SwingConstants.CENTER);
        descLabel.setFont(new Font("Georgia", Font.PLAIN, 14));
        descLabel.setForeground(new Color(240, 230, 200));
        descLabel.setBounds(260, 560, 460, 60);
        contentPanel.add(descLabel);

        refreshInventory();

        // Supporto scorciatoie da tastiera (ESC o I per chiudere)
        contentPanel.setFocusable(true);
        contentPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_I) {
                    dispose();
                }
            }
        });

        // Chiusura al clic al di fuori dell'area dell'inventario
        contentPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getX() < 120 || e.getX() > 860 || e.getY() < 30 || e.getY() > 640) {
                    dispose();
                }
            }
        });
    }

    public void refreshInventory() {
        gridPanel.removeAll();
        List<Item> items = GameState.getInstance().getInventory();

        if (items.isEmpty()) {
            descLabel.setText("<html><center><font color='#D4AF37'><b>INVENTARIO VUOTO</b></font><br>"
                    + "<font size='3' color='#C0B8A0'>Esplora le stanze del vascello per raccogliere oggetti e ingredienti.</font></center></html>");
        } else {
            selectItem(items.get(0), null);
        }

        // 32 Caselle totali (4x8)
        for (int i = 0; i < 32; i++) {
            final boolean hasItem = (i < items.size());
            final boolean[] isHovered = new boolean[]{false};
            final int slotIndex = i;

            // Slot con bordo rotondo e bianco al passaggio mouse / selezione
            JPanel slot = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    boolean isSelected = (this == selectedSlotPanel);
                    if (hasItem && (isSelected || isHovered[0])) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Color.WHITE);
                        // Bordo bianco rotondo
                        g2.setStroke(new BasicStroke(isSelected ? 2.5f : 1.5f));
                        int pad = 4;
                        int size = Math.min(getWidth(), getHeight()) - (pad * 2);
                        int cx = (getWidth() - size) / 2;
                        int cy = (getHeight() - size) / 2;
                        g2.drawOval(cx, cy, size, size);
                        g2.dispose();
                    }
                }
            };
            slot.setOpaque(false);

            if (hasItem) {
                final Item item = items.get(i);
                final JPanel currentSlot = slot;

                JButton btn = new JButton();
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setFocusable(false);
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                // Icona dell'oggetto centrata (50x50)
                try {
                    ImageIcon icon = new ImageIcon(getClass().getResource(item.getIconPath()));
                    Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                    btn.setIcon(new ImageIcon(img));
                } catch (Exception e) {
                    btn.setText(item.getName().substring(0, Math.min(3, item.getName().length())));
                    btn.setFont(new Font("SansSerif", Font.BOLD, 12));
                    btn.setForeground(Color.YELLOW);
                }

                // Hover ed effetto rotondo bianco
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHovered[0] = true;
                        currentSlot.repaint();
                        showItemDetails(item);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered[0] = false;
                        currentSlot.repaint();
                        if (selectedItem != null) {
                            showItemDetails(selectedItem);
                        }
                    }
                });

                btn.addActionListener(e -> selectItem(item, currentSlot));
                slot.add(btn, BorderLayout.CENTER);

                if (i == 0) {
                    selectedSlotPanel = slot;
                }
            }

            gridPanel.add(slot);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void selectItem(Item item, JPanel slotPanel) {
        JPanel prev = selectedSlotPanel;
        this.selectedItem = item;
        this.selectedSlotPanel = slotPanel;
        if (prev != null) prev.repaint();
        if (slotPanel != null) slotPanel.repaint();
        showItemDetails(item);
    }

    private void showItemDetails(Item item) {
        if (item != null) {
            descLabel.setText("<html><center><font color='#FFD700' size='4'><b>" + item.getName().toUpperCase() + "</b></font><br>"
                    + "<font color='#EDE0C8' size='3'>" + item.getDescription() + "</font></center></html>");
        }
    }
}
