package com.alchemist.hangover.view.dialogue;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Il menu delle scelte con Martino segue la stessa UI cinematografica
 * dei prompt boss e delle ricompense: fondo blu notte, bordo oro,
 * illustrazione dell'oggetto, pulsanti reali e testo accessibile.
 *
 * L'immagine legacy dialogue_choice_bg.png non serve piu': conteneva
 * gia' scritte e bottoni, causando click e proporzioni fragili.
 */
public class MartinoMapDialog extends JDialog {
    public interface ChoiceCallback {
        void onChoiceMade(boolean gaveMap);
    }

    private static final String MAP_ID = "MAPPA_CIELO";
    private static final String KEY_ID = "CHIAVE_DRAGO";
    private final ChoiceCallback callback;
    private boolean completed;

    public MartinoMapDialog(Frame parent, ChoiceCallback callback) {
        super(parent, "La scelta di Arthur", true);
        this.callback = callback;
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JPanel content = buildContent(GameState.getInstance(), this::complete);
        setContentPane(content);
        pack();
        setLocationRelativeTo(parent);

        // Anche Alt+F4 e il tasto di chiusura del sistema equivalgono a Esci.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                complete(false);
            }
        });
    }

    static boolean canDonate(GameState state) {
        return state != null && state.hasItem(MAP_ID)
                && !state.hasGivenMapToMartino();
    }

    /**
     * Mutazione atomica a livello di flusso UI. Chiave e flag vengono
     * aggiornati soltanto quando la mappa e' davvero nell'inventario.
     */
    static Item donateMap(GameState state) {
        if (!canDonate(state)) return null;
        Item map = state.getItemById(MAP_ID);
        if (map == null) return null;

        state.removeItem(map);
        Item key = new Item(KEY_ID, "Chiave del Drago",
                "Una pesante chiave d'ottone con incisioni a forma di drago celeste.",
                "/images/chapter1/items/key.png");
        state.addItem(key);
        state.setGivenMapToMartino(true);
        state.setHasDragonKey(true);
        return key;
    }

    private void complete(boolean wantsToDonate) {
        if (completed) return;
        Item reward = wantsToDonate ? donateMap(GameState.getInstance()) : null;
        if (wantsToDonate && reward == null) return;

        completed = true;
        dispose();
        if (callback != null) callback.onChoiceMade(reward != null);
        if (reward != null) {
            StoryPromptDialog.reward((Frame) getOwner(), StoryPromptDialog.Theme.SKY,
                    "CHIAVE DEL DRAGO",
                    "Martino riceve la Mappa del Cielo e ti consegna "
                    + "una chiave incisa con un drago celeste.",
                    reward);
        }
    }

    /**
     * Il pannello si puo' verificare con i test anche senza aprire un JDialog.
     * Riutilizza la cornice, i colori e i due pulsanti di StoryPromptDialog.
     */
    static JPanel buildContent(GameState state, Consumer<Boolean> onChoice) {
        boolean hasMap = canDonate(state);
        boolean alreadyDonated = state.hasGivenMapToMartino();

        JPanel panel = StoryPromptDialog.buildContent(
                StoryPromptDialog.Theme.SKY,
                "COSA VUOI FARE?",
                "",
                "DONA",
                "CONGEDATI / ESCI",
                onChoice
        );
        panel.setName("martinoChoicePanel");
        panel.setPreferredSize(new Dimension(750, 430));

        for (Component child : panel.getComponents()) {
            if ("storyPromptText".equals(child.getName())) {
                panel.remove(child);
                break;
            }
        }

        JPanel content = new JPanel(new BorderLayout(24, 0));
        content.setName("martinoChoiceBody");
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(5, 8, 2, 8));

        JPanel artworkPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    Color gold = StoryPromptDialog.Theme.SKY.accent;
                    g.setColor(new Color(gold.getRed(), gold.getGreen(), gold.getBlue(), 32));
                    g.fillRoundRect(7, 7, getWidth() - 14, getHeight() - 14, 22, 22);
                    g.setColor(new Color(gold.getRed(), gold.getGreen(), gold.getBlue(), 145));
                    g.setStroke(new BasicStroke(2));
                    g.drawRoundRect(7, 7, getWidth() - 14, getHeight() - 14, 22, 22);
                } finally {
                    g.dispose();
                }
            }
        };
        artworkPanel.setOpaque(false);
        artworkPanel.setPreferredSize(new Dimension(194, 204));

        JLabel itemIcon = new JLabel("", SwingConstants.CENTER);
        itemIcon.setName("martinoChoiceItemIcon");
        String imageResource = hasMap ? "/images/chapter1/items/map.png"
                : alreadyDonated ? "/images/chapter1/items/key.png" : null;
        if (imageResource != null) {
            URL imageUrl = MartinoMapDialog.class.getResource(imageResource);
            if (imageUrl != null) {
                ImageIcon original = new ImageIcon(imageUrl);
                int sourceWidth = Math.max(1, original.getIconWidth());
                int sourceHeight = Math.max(1, original.getIconHeight());
                double ratio = Math.min(150d / sourceWidth, 150d / sourceHeight);
                int width = Math.max(1, (int) Math.round(sourceWidth * ratio));
                int height = Math.max(1, (int) Math.round(sourceHeight * ratio));
                itemIcon.setIcon(new ImageIcon(original.getImage()
                        .getScaledInstance(width, height, Image.SCALE_SMOOTH)));
            }
        }
        if (itemIcon.getIcon() == null) {
            itemIcon.setText("✦");
            itemIcon.setForeground(StoryPromptDialog.Theme.SKY.accent);
            itemIcon.setFont(new Font("Georgia", Font.BOLD, 68));
        }
        artworkPanel.add(itemIcon, BorderLayout.CENTER);
        content.add(artworkPanel, BorderLayout.WEST);

        JPanel explanation = new JPanel(new BorderLayout(0, 12));
        explanation.setOpaque(false);
        JLabel caption = new JLabel(hasMap ? "LA MAPPA DEL CIELO"
                : alreadyDonated ? "DONAZIONE COMPLETATA" : "LA MAPPA DEL CIELO");
        caption.setFont(new Font("Georgia", Font.BOLD, 15));
        caption.setForeground(StoryPromptDialog.Theme.SKY.accent);
        explanation.add(caption, BorderLayout.NORTH);

        String message = hasMap
                ? "Martino osserva la mappa che hai trovato.\n\n"
                + "Vuoi consegnargliela in cambio della Chiave del Drago?"
                : alreadyDonated
                ? "Hai gia' donato la Mappa del Cielo a Martino.\n\n"
                + "La Chiave del Drago e' stata consegnata."
                : "Non possiedi ancora la Mappa del Cielo.\n\n"
                + "Continua a esplorare il vascello per trovarla.";
        JTextArea description = new JTextArea(message);
        description.setName("martinoChoiceDescription");
        description.setEditable(false);
        description.setFocusable(false);
        description.setOpaque(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setFont(new Font("Georgia", Font.PLAIN, 19));
        description.setForeground(new Color(244, 228, 205));
        explanation.add(description, BorderLayout.CENTER);
        content.add(explanation, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);

        JButton donate = findButton(panel, "storyPromptAccept");
        JButton leave = findButton(panel, "storyPromptDecline");
        if (donate != null) {
            donate.setName("martinoDonateButton");
            donate.setEnabled(hasMap);
            donate.getAccessibleContext().setAccessibleName("Dona la Mappa del Cielo");
            donate.setToolTipText(hasMap
                    ? "Consegna la Mappa del Cielo a Martino"
                    : "Devi possedere la Mappa del Cielo per donarla");
        }
        if (leave != null) {
            leave.setName("martinoExitButton");
            leave.getAccessibleContext().setAccessibleName("Saluta Martino e torna a esplorare");
        }
        panel.revalidate();
        return panel;
    }

    private static JButton findButton(Container container, String name) {
        for (Component child : container.getComponents()) {
            if (child instanceof JButton button && name.equals(button.getName())) {
                return button;
            }
            if (child instanceof Container nested) {
                JButton found = findButton(nested, name);
                if (found != null) return found;
            }
        }
        return null;
    }
}
