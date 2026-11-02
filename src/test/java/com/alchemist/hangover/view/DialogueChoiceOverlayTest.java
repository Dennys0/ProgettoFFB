package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DialogueChoiceOverlayTest {

    @Test
    void cinematicMartinoMenuHandlesUnavailableMapAndExit() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            GameState state = GameState.getInstance();
            state.resetState();
            try {
                AtomicReference<Boolean> choice = new AtomicReference<>();
                JPanel panel = DialogueChoiceOverlay.buildContent(state, choice::set);
                assertEquals(new Dimension(750, 430), panel.getPreferredSize());
                JButton donate = find(panel, "martinoDonateButton", JButton.class);
                JButton exit = find(panel, "martinoExitButton", JButton.class);
                assertNotNull(donate);
                assertNotNull(exit);
                assertFalse(donate.isEnabled());
                assertTrue(exit.isEnabled());
                JTextArea body = find(panel, "martinoChoiceDescription", JTextArea.class);
                assertTrue(body.getText().contains("Non possiedi"));
                exit.doClick();
                assertEquals(Boolean.FALSE, choice.get());
                assertFalse(state.hasDragonKey());
                assertFalse(state.hasGivenMapToMartino());
                assertNull(DialogueChoiceOverlay.donateMap(state));

                panel.setSize(750, 430);
                BufferedImage image = new BufferedImage(750, 430, BufferedImage.TYPE_INT_ARGB);
                Graphics graphics = image.createGraphics();
                try { assertDoesNotThrow(() -> panel.paint(graphics)); }
                finally { graphics.dispose(); }
            } finally {
                state.resetState();
            }
        });
    }

    @Test
    void donatingMapGrantsOnlyOneDragonKeyAndShowsIllustratedChoice() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            GameState state = GameState.getInstance();
            state.resetState();
            try {
                Item map = new Item("MAPPA_CIELO", "Mappa del Cielo",
                        "Una mappa incisa con stelle.", "/images/chapter1/items/map.png");
                state.addItem(map);
                AtomicReference<Boolean> choice = new AtomicReference<>();
                JPanel panel = DialogueChoiceOverlay.buildContent(state, choice::set);
                JButton donate = find(panel, "martinoDonateButton", JButton.class);
                assertNotNull(donate);
                assertTrue(donate.isEnabled());
                JLabel icon = find(panel, "martinoChoiceItemIcon", JLabel.class);
                assertNotNull(icon);
                assertNotNull(icon.getIcon(), "L'icona della mappa deve essere visibile");
                donate.doClick();
                assertEquals(Boolean.TRUE, choice.get());

                Item reward = DialogueChoiceOverlay.donateMap(state);
                assertNotNull(reward);
                assertTrue(state.hasGivenMapToMartino());
                assertTrue(state.hasDragonKey());
                assertFalse(state.hasItem("MAPPA_CIELO"));
                assertTrue(state.hasItem("CHIAVE_DRAGO"));
                assertNull(DialogueChoiceOverlay.donateMap(state),
                        "Non si deve poter ricevere un'altra chiave per la stessa mappa.");
                assertEquals(1, state.getInventory().stream()
                        .filter(item -> item.getId().equals("CHIAVE_DRAGO")).count());
            } finally {
                state.resetState();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Component> T find(Container root, String name, Class<T> type) {
        for (Component component : root.getComponents()) {
            if (name.equals(component.getName()) && type.isInstance(component)) {
                return (T) component;
            }
            if (component instanceof Container child) {
                T found = find(child, name, type);
                if (found != null) return found;
            }
        }
        return null;
    }
}
