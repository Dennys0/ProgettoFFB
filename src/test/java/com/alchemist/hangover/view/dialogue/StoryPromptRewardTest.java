package com.alchemist.hangover.view.dialogue;

import com.alchemist.hangover.model.Item;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica immagine della ricompensa e funzionamento dell’azione di continuazione.
 */
class StoryPromptRewardTest {
    @Test
    void bothChapterThemesDisplayRealBossLootWithOneWorkingContinueButton() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            String[] art = {"/images/chapter1/items/breath.png", "/images/chapter2/items/foam.png"};
            StoryPromptDialog.Theme[] themes = {
                    StoryPromptDialog.Theme.SKY, StoryPromptDialog.Theme.OCEAN
            };
            for (int i = 0; i < themes.length; i++) {
                Item item = new Item("ITEM_" + i, "Premio boss " + i,
                        "Descrizione ricompensa", art[i]);
                AtomicInteger clicks = new AtomicInteger();
                JPanel content = StoryPromptDialog.buildRewardContent(
                        themes[i], "VITTORIA", "Hai sconfitto il guardiano.", item,
                        clicks::incrementAndGet);
                content.setSize(750, 445);
                JLabel icon = find(content, "bossRewardIcon", JLabel.class);
                assertNotNull(icon);
                assertNotNull(icon.getIcon(), "Usare lo sprite reale dell'ingrediente.");
                ImageIcon original = new ImageIcon(StoryPromptRewardTest.class.getResource(art[i]));
                assertTrue(icon.getIcon().getIconWidth() <= 150);
                assertTrue(icon.getIcon().getIconHeight() <= 150);
                assertEquals(original.getIconWidth() / (double) original.getIconHeight(),
                        icon.getIcon().getIconWidth() / (double) icon.getIcon().getIconHeight(),
                        0.02, "Non deformare le immagini degli ingredienti.");
                JTextArea body = find(content, "bossRewardDescription", JTextArea.class);
                assertNotNull(body);
                assertTrue(body.getText().contains(item.getName()));
                assertTrue(body.getText().contains(item.getDescription()));
                JButton continueButton = find(content, "storyPromptAccept", JButton.class);
                assertNotNull(continueButton);
                assertEquals("CONTINUA", continueButton.getText());
                continueButton.doClick();
                assertEquals(1, clicks.get());
                assertNull(find(content, "storyPromptDecline", JButton.class));

                BufferedImage image = new BufferedImage(750, 445, BufferedImage.TYPE_INT_ARGB);
                Graphics graphics = image.createGraphics();
                try { assertDoesNotThrow(() -> content.paint(graphics)); }
                finally { graphics.dispose(); }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Component> T find(Container root, String name, Class<T> type) {
        for (Component component : root.getComponents()) {
            if (name.equals(component.getName()) && type.isInstance(component))
                return (T) component;
            if (component instanceof Container child) {
                T found = find(child, name, type);
                if (found != null) return found;
            }
        }
        return null;
    }
}
