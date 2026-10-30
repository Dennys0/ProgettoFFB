package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Inventario a lista verticale: mostra solo gli oggetti raccolti.
 * La scheda a destra mostra l'icona, il luogo d'origine e una foto della stanza.
 * Non altera il modello Item o i salvataggi H2.
 */
public final class InventoryOverlay extends JDialog {
    private static final Color GOLD = new Color(224, 185, 103);
    private static final Color TEXT = new Color(236, 226, 205);
    private static final Color MUTED = new Color(180, 172, 160);
    private static final Color PANEL = new Color(38, 32, 47);
    private static final Color LIST_BACKGROUND = new Color(27, 25, 36);

    private final DefaultListModel<Item> inventoryModel = new DefaultListModel<>();
    private final Map<String, ImageIcon> iconCache = new HashMap<>();
    private final JList<Item> itemList;
    private final JLabel itemCountLabel = new JLabel();
    private final JLabel itemNameLabel = new JLabel();
    private final JLabel itemImageLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel itemLocationLabel = new JLabel();
    private final JLabel locationImageLabel = new JLabel("", SwingConstants.CENTER);
    private final JTextArea descriptionArea = new JTextArea();

    public InventoryOverlay(Frame parent) {
        super(parent, "Inventario di Arthur", true);
        setUndecorated(true);
        setSize(1030, 650);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 18)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setPaint(new GradientPaint(0, 0, new Color(37, 29, 43),
                            getWidth(), getHeight(), new Color(18, 22, 35)));
                    g.fillRect(0, 0, getWidth(), getHeight());
                } finally {
                    g.dispose();
                }
            }
        };
        root.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(142, 103, 52), 3),
                new EmptyBorder(22, 27, 19, 27)));
        setContentPane(root);

        JPanel heading = new JPanel(new BorderLayout(12, 0));
        heading.setOpaque(false);
        JLabel title = new JLabel("INVENTARIO DI ARTHUR");
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        title.setForeground(GOLD);
        heading.add(title, BorderLayout.WEST);
        itemCountLabel.setFont(new Font("Georgia", Font.ITALIC, 14));
        itemCountLabel.setForeground(MUTED);
        heading.add(itemCountLabel, BorderLayout.EAST);
        root.add(heading, BorderLayout.NORTH);

        JPanel columns = new JPanel(new BorderLayout(18, 0));
        columns.setOpaque(false);
        root.add(columns, BorderLayout.CENTER);

        JPanel listColumn = new JPanel(new BorderLayout(0, 12));
        listColumn.setOpaque(false);
        listColumn.setPreferredSize(new Dimension(368, 0));
        JLabel listHeading = sectionHeading("OGGETTI RACCOLTI");
        listColumn.add(listHeading, BorderLayout.NORTH);

        itemList = new JList<>(inventoryModel) {
            @Override
            public String getToolTipText(MouseEvent event) {
                int index = locationToIndex(event.getPoint());
                if (index < 0 || !getCellBounds(index, index).contains(event.getPoint())) {
                    return null;
                }
                return getModel().getElementAt(index).getName();
            }
        };
        itemList.setCellRenderer(new ItemRowRenderer());
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.setFixedCellHeight(78);
        itemList.setBackground(LIST_BACKGROUND);
        itemList.setForeground(TEXT);
        itemList.setBorder(new EmptyBorder(4, 4, 4, 4));
        itemList.setFocusable(true);
        itemList.setToolTipText("Seleziona un oggetto per esaminarlo");
        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showItemDetails(itemList.getSelectedValue());
        });

        JScrollPane listScroll = new JScrollPane(itemList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listScroll.getViewport().setBackground(LIST_BACKGROUND);
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(102, 78, 58), 1));
        listScroll.getVerticalScrollBar().setUnitIncrement(18);
        listColumn.add(listScroll, BorderLayout.CENTER);
        columns.add(listColumn, BorderLayout.WEST);

        JPanel details = new JPanel(new BorderLayout(0, 16));
        details.setBackground(PANEL);
        details.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(116, 91, 69)),
                new EmptyBorder(19, 24, 18, 24)));
        columns.add(details, BorderLayout.CENTER);

        JPanel detailsHeading = new JPanel(new BorderLayout(0, 5));
        detailsHeading.setOpaque(false);
        JLabel detailsLabel = sectionHeading("SCHEDA DELL'OGGETTO");
        detailsHeading.add(detailsLabel, BorderLayout.NORTH);
        itemNameLabel.setFont(new Font("Georgia", Font.BOLD, 22));
        itemNameLabel.setForeground(TEXT);
        detailsHeading.add(itemNameLabel, BorderLayout.CENTER);
        details.add(detailsHeading, BorderLayout.NORTH);

        JPanel preview = new JPanel(new BorderLayout(18, 0));
        preview.setOpaque(false);
        preview.setPreferredSize(new Dimension(0, 197));

        itemImageLabel.setOpaque(true);
        itemImageLabel.setBackground(new Color(23, 22, 33));
        itemImageLabel.setForeground(GOLD);
        itemImageLabel.setFont(new Font("Georgia", Font.BOLD, 45));
        itemImageLabel.setBorder(BorderFactory.createLineBorder(new Color(105, 82, 58)));
        itemImageLabel.setPreferredSize(new Dimension(193, 190));
        preview.add(itemImageLabel, BorderLayout.WEST);

        JPanel origin = new JPanel(new BorderLayout(0, 8));
        origin.setOpaque(false);
        origin.add(sectionHeading("LUOGO DEL RITROVAMENTO"), BorderLayout.NORTH);
        itemLocationLabel.setFont(new Font("Georgia", Font.PLAIN, 15));
        itemLocationLabel.setForeground(TEXT);
        origin.add(itemLocationLabel, BorderLayout.CENTER);
        locationImageLabel.setOpaque(true);
        locationImageLabel.setBackground(new Color(23, 22, 33));
        locationImageLabel.setForeground(MUTED);
        locationImageLabel.setFont(new Font("Georgia", Font.ITALIC, 12));
        locationImageLabel.setPreferredSize(new Dimension(205, 116));
        locationImageLabel.setBorder(BorderFactory.createLineBorder(new Color(105, 82, 58)));
        origin.add(locationImageLabel, BorderLayout.SOUTH);
        preview.add(origin, BorderLayout.CENTER);

        JPanel information = new JPanel(new BorderLayout(0, 10));
        information.setOpaque(false);
        information.add(preview, BorderLayout.NORTH);
        information.add(sectionHeading("DESCRIZIONE"), BorderLayout.CENTER);

        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Georgia", Font.PLAIN, 17));
        descriptionArea.setForeground(TEXT);
        descriptionArea.setBackground(new Color(29, 27, 39));
        descriptionArea.setMargin(new Insets(14, 16, 14, 16));
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descriptionScroll.setBorder(BorderFactory.createLineBorder(new Color(89, 72, 60)));
        descriptionScroll.getViewport().setBackground(new Color(29, 27, 39));
        descriptionScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(information, BorderLayout.NORTH);
        body.add(descriptionScroll, BorderLayout.CENTER);
        details.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(14, 0));
        footer.setOpaque(false);
        JLabel hint = new JLabel("Seleziona un oggetto dall'elenco  •  ESC / I per chiudere");
        hint.setForeground(MUTED);
        hint.setFont(new Font("Georgia", Font.ITALIC, 13));
        footer.add(hint, BorderLayout.WEST);
        JButton close = new JButton("CHIUDI");
        close.setFont(new Font("Georgia", Font.BOLD, 15));
        close.setForeground(new Color(255, 229, 171));
        close.setBackground(new Color(74, 51, 49));
        close.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD),
                new EmptyBorder(8, 30, 8, 30)));
        close.setFocusPainted(false);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addActionListener(e -> dispose());
        footer.add(close, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke('I'),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        refreshInventory();
    }

    /** Ricostruisce la lista mantenendo, se possibile, l'oggetto selezionato. */
    public void refreshInventory() {
        Item selected = itemList.getSelectedValue();
        String previousId = selected == null ? null : selected.getId();
        inventoryModel.clear();
        List<Item> items = GameState.getInstance().getInventory();
        for (Item item : items) inventoryModel.addElement(item);
        int count = inventoryModel.getSize();
        itemCountLabel.setText(count == 1 ? "1 oggetto raccolto" : count + " oggetti raccolti");

        if (count == 0) {
            showItemDetails(null);
            return;
        }
        int selection = 0;
        if (previousId != null) {
            for (int i = 0; i < count; i++) {
                if (previousId.equalsIgnoreCase(inventoryModel.get(i).getId())) {
                    selection = i;
                    break;
                }
            }
        }
        itemList.setSelectedIndex(selection);
        itemList.ensureIndexIsVisible(selection);
    }

    private void showItemDetails(Item item) {
        if (item == null) {
            itemNameLabel.setText("Nessun oggetto raccolto");
            itemImageLabel.setIcon(null);
            itemImageLabel.setText("?");
            itemLocationLabel.setText("Non ancora scoperto");
            locationImageLabel.setIcon(null);
            locationImageLabel.setText("Nessuna stanza");
            descriptionArea.setText("Esplora gli scenari e raccogli oggetti per scoprire la loro storia.");
            descriptionArea.setCaretPosition(0);
            return;
        }
        itemNameLabel.setText(item.getName());
        ImageIcon icon = loadIcon(item.getIconPath(), 155, 155);
        itemImageLabel.setIcon(icon);
        itemImageLabel.setText(icon == null ? "?" : "");

        ItemOriginCatalog.Origin origin = ItemOriginCatalog.find(item.getId());
        itemLocationLabel.setText("<html><b>" + escapeHtml(origin.chapterLabel())
                + "</b><br>" + escapeHtml(origin.place()) + "</html>");
        ImageIcon scene = loadIcon(origin.scenePath(), 200, 112);
        locationImageLabel.setIcon(scene);
        locationImageLabel.setText(scene == null ? "Anteprima non disponibile" : "");
        descriptionArea.setText(item.getDescription() == null || item.getDescription().isBlank()
                ? "Nessuna descrizione disponibile."
                : item.getDescription());
        descriptionArea.setCaretPosition(0);
    }

    private JLabel sectionHeading(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(GOLD);
        return label;
    }

    private ImageIcon loadIcon(String path, int width, int height) {
        if (path == null || path.isBlank()) return null;
        String key = path + "#" + width + "x" + height;
        if (iconCache.containsKey(key)) return iconCache.get(key);
        URL resource = getClass().getResource(path);
        ImageIcon icon = resource == null ? null : new ImageIcon(
                new ImageIcon(resource).getImage().getScaledInstance(
                        width, height, Image.SCALE_SMOOTH));
        iconCache.put(key, icon);
        return icon;
    }

    private static String escapeHtml(String value) {
        return value == null ? "" : value.replace("&", "&amp;")
                .replace("<", "&lt;").replace(">", "&gt;");
    }

    private final class ItemRowRenderer extends JPanel implements ListCellRenderer<Item> {
        private final JLabel image = new JLabel();
        private final JLabel name = new JLabel();
        private final JLabel place = new JLabel();

        private ItemRowRenderer() {
            super(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(9, 12, 9, 12));
            image.setPreferredSize(new Dimension(49, 49));
            image.setHorizontalAlignment(SwingConstants.CENTER);
            add(image, BorderLayout.WEST);
            JPanel texts = new JPanel(new GridLayout(2, 1, 0, 2));
            texts.setOpaque(false);
            name.setFont(new Font("Georgia", Font.BOLD, 15));
            place.setFont(new Font("SansSerif", Font.PLAIN, 12));
            texts.add(name);
            texts.add(place);
            add(texts, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Item> list, Item item,
                int index, boolean selected, boolean focus) {
            setOpaque(true);
            setBackground(selected ? new Color(83, 64, 51) : LIST_BACKGROUND);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(selected ? GOLD : new Color(54, 49, 59),
                            selected ? 2 : 1),
                    new EmptyBorder(selected ? 7 : 8, 11, selected ? 7 : 8, 11)));
            image.setIcon(loadIcon(item.getIconPath(), 48, 48));
            name.setText(item.getName());
            name.setForeground(selected ? new Color(255, 226, 159) : TEXT);
            ItemOriginCatalog.Origin origin = ItemOriginCatalog.find(item.getId());
            place.setText(origin.shortLabel());
            place.setForeground(selected ? new Color(231, 198, 143) : MUTED);
            return this;
        }
    }
}
