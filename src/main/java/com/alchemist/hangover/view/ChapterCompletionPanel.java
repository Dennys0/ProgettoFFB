package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * One illustrated completion screen for every chapter.
 * Replaces three formerly unrelated panels while retaining their scene IDs.
 * Cocktail images are optional: missing assets show a readable placeholder.
 */
public final class ChapterCompletionPanel extends JPanel {
    record Details(int chapter, String title, String drink, String imagePath,
                   String narrative, String nextLabel) { }

    static Details detailsFor(int chapter) {
        return switch (chapter) {
            case 1 -> new Details(1, "CAPITOLO I COMPLETATO", "Asgardian Aviation",
                    "/images/items/cocktail_1.png",
                    "<html><center>"
                    + "Il primo sorso dell'Asgardian Aviation sprigiona vento e fulmini.<br>"
                    + "Skíðblaðnir inizia la discesa verso i mari inferiori.<br>"
                    + "Arthur: «Certo che questo non era nel menu del bar!»"
                    + "</center></html>", "ENTRA NEL CAPITOLO II");
            case 2 -> new Details(2, "CAPITOLO II COMPLETATO", "Maelstrom Martini",
                    "/images/items/cocktail_2.png",
                    "<html><center>"
                    + "La Schiuma di Ægir, il Corallo di Rán e il Sale di Jörmungandr<br>"
                    + "si uniscono in un cocktail luminoso, come le profondità marine.<br>"
                    + "La terra di Ymir ora attende Arthur."
                    + "</center></html>", "ENTRA NEL CAPITOLO III");
            case 3 -> new Details(3, "CAPITOLO III COMPLETATO", "Ymir's Old Fashioned",
                    "/images/items/cocktail_3.png",
                    "<html><center>"
                    + "Il Muschio di Midgard, il Frammento di Yggdrasil e la Lacrima di Ymir<br>"
                    + "si fondono in un drink fumante di energia primordiale.<br>"
                    + "Ymir riposa, la terra è finalmente quieta.<br>"
                    + "Arthur: «Alla prossima catastrofe. Possibilmente dopo un aperitivo.»"
                    + "</center></html>", "TORNA ALLA FORGIA");
            default -> throw new IllegalArgumentException(
                    "Nessuna schermata di completamento per il capitolo " + chapter);
        };
    }

    private final JFrame owner;
    private final Details details;

    public ChapterCompletionPanel(JFrame owner, int chapter) {
        this.owner = owner;
        this.details = detailsFor(chapter);
        setLayout(null);
        setName("chapterCompletionPanel");
        setBackground(new Color(23, 21, 29));

        JLabel title = new JLabel(details.title(), SwingConstants.CENTER);
        title.setName("chapterCompletionTitle");
        title.setFont(new Font("Georgia", Font.BOLD, 43));
        title.setForeground(new Color(249, 213, 131));
        title.setBounds(90, 43, 1100, 82);
        add(title);

        JLabel drink = new JLabel(details.drink(), SwingConstants.CENTER);
        drink.setName("chapterCompletionDrink");
        drink.setFont(new Font("Georgia", Font.ITALIC, 27));
        drink.setForeground(new Color(236, 191, 119));
        drink.setBounds(430, 138, 420, 260);
        URL art = getClass().getResource(details.imagePath());
        if (art != null) {
            ImageIcon source = new ImageIcon(art);
            int origWidth = Math.max(1, source.getIconWidth());
            int origHeight = Math.max(1, source.getIconHeight());
            double scale = Math.min(320.0 / origWidth, 260.0 / origHeight);
            int width = Math.max(1, (int) Math.round(origWidth * scale));
            int height = Math.max(1, (int) Math.round(origHeight * scale));
            drink.setIcon(new ImageIcon(source.getImage()
                    .getScaledInstance(width, height, Image.SCALE_SMOOTH)));
            drink.setText("");
        }
        add(drink);

        JLabel narration = new JLabel(details.narrative(), SwingConstants.CENTER);
        narration.setName("chapterCompletionNarrative");
        narration.setFont(new Font("Georgia", Font.PLAIN, 20));
        narration.setForeground(new Color(239, 230, 216));
        narration.setBounds(80, 405, 1120, 112);
        add(narration);

        JLabel reminder = new JLabel(
                "Puoi salvare i progressi di questo capitolo direttamente da qui.",
                SwingConstants.CENTER);
        reminder.setFont(new Font("Georgia", Font.ITALIC, 16));
        reminder.setForeground(new Color(214, 188, 151));
        reminder.setBounds(160, 522, 960, 30);
        add(reminder);

        JButton save = button("SALVA PARTITA", "chapterCompletionSave", 128);
        save.addActionListener(e -> {
            // A completed C1/C2 save must load directly into the NEXT chapter,
            // not into an emptied cauldron inventory of the finished chapter.
            prepareNextChapterState(details.chapter(), GameState.getInstance());
            new SaveLoadDialog(owner, SaveLoadDialog.Mode.SAVE).setVisible(true);
        });
        add(save);

        JButton next = button(details.nextLabel(), "chapterCompletionNext", 486);
        next.addActionListener(e -> advance());
        add(next);

        JButton menu = button("MENU PRINCIPALE", "chapterCompletionMenu", 844);
        menu.addActionListener(e -> {
            if (StoryPromptDialog.confirm(owner, themeFor(details.chapter()),
                    "TORNARE AL MENU?",
                    "I progressi non salvati andranno persi. Vuoi tornare al menu?",
                    "TORNA AL MENU", "RESTA QUI")) {
                SceneManager.getInstance().showScene("MENU");
            }
        });
        add(menu);
    }

    private static StoryPromptDialog.Theme themeFor(int chapter) {
        return switch (chapter) {
            case 1 -> StoryPromptDialog.Theme.SKY;
            case 2 -> StoryPromptDialog.Theme.OCEAN;
            case 3 -> StoryPromptDialog.Theme.EARTH;
            default -> throw new IllegalArgumentException("Capitolo non previsto");
        };
    }

    private static JButton button(String text, String name, int x) {
        JButton button = new JButton(text);
        button.setName(name);
        button.setBounds(x, 582, 308, 55);
        button.setFont(new Font("Georgia", Font.BOLD, 15));
        button.setForeground(new Color(53, 37, 30));
        button.setBackground(new Color(238, 211, 161));
        button.setBorder(BorderFactory.createLineBorder(new Color(165, 113, 55), 2));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    /** Used by SAVE and CONTINUE; never mutates DB directly. */
    static void prepareNextChapterState(int completedChapter, GameState state) {
        switch (completedChapter) {
            case 1 -> {
                state.setChapter(2);
                state.setCurrentRoom(1);
                state.setBossDefeated(false);
                state.setBossRetryAvailable(false);
                state.resetHealth();
            }
            case 2 -> {
                state.setChapter(3);
                state.setCurrentRoom(1);
                state.setBossRetryAvailable(false);
                state.ensureYmirRuneSeed();
                state.resetHealth();
            }
            case 3 -> { /* Chapter 3 remains completed for saving. */ }
            default -> throw new IllegalArgumentException("Capitolo non previsto");
        }
    }

    private void advance() {
        GameState state = GameState.getInstance();
        SceneManager manager = SceneManager.getInstance();
        int chapter = details.chapter();
        if (chapter == 3) {
            state.setCurrentRoom(3);
            Chapter3Panel earth = (Chapter3Panel) manager.getPanel("CHAPTER3");
            if (earth != null) earth.refreshFromGameState();
            manager.showScene("CHAPTER3");
            return;
        }
        prepareNextChapterState(chapter, state);
        String nextScene = chapter == 1 ? "CHAPTER2" : "CHAPTER3";
        AbstractChapterPanel nextPanel = (AbstractChapterPanel) manager.getPanel(nextScene);
        if (nextPanel != null) nextPanel.refreshFromGameState();
        manager.showScene(nextScene);
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            Color bottom = switch (details.chapter()) {
                case 1 -> new Color(15, 25, 44);
                case 2 -> new Color(10, 29, 40);
                default -> new Color(12, 20, 35);
            };
            g.setPaint(new GradientPaint(0, 0, new Color(31, 24, 26),
                    0, getHeight(), bottom));
            g.fillRect(0, 0, getWidth(), getHeight());
        } finally { g.dispose(); }
    }
}
