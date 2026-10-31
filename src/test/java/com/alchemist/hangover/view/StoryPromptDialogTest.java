package com.alchemist.hangover.view;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class StoryPromptDialogTest {
    @Test
    void skyAndOceanPromptsExposeAccessibleRealActions() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (StoryPromptDialog.Theme theme : StoryPromptDialog.Theme.values()) {
                AtomicReference<Boolean> response = new AtomicReference<>();
                JPanel panel = StoryPromptDialog.buildContent(theme, "Boss",
                        "Vuoi affrontare il boss?", "AFFRONTA", "NON ANCORA",
                        response::set);
                panel.setSize(750, 348);
                assertEquals(new Dimension(750, 348), panel.getPreferredSize());
                JButton confirm = find(panel, "storyPromptAccept");
                JButton decline = find(panel, "storyPromptDecline");
                assertNotNull(confirm);
                assertNotNull(decline);
                decline.doClick();
                assertEquals(Boolean.FALSE, response.get());
                confirm.doClick();
                assertEquals(Boolean.TRUE, response.get());

                BufferedImage image = new BufferedImage(750, 348, BufferedImage.TYPE_INT_ARGB);
                Graphics graphics = image.createGraphics();
                try { assertDoesNotThrow(() -> panel.paint(graphics)); }
                finally { graphics.dispose(); }
            }
        });
    }

    @Test
    void messagePromptHasOnlyOneAcknowledgmentButton() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel message = StoryPromptDialog.buildContent(
                    StoryPromptDialog.Theme.SKY, "Serbatoio bloccato",
                    "Serve la Chiave del Drago", "HO CAPITO", null, ignored -> {});
            assertNotNull(find(message, "storyPromptAccept"));
            assertNull(find(message, "storyPromptDecline"));
        });
    }

    private static JButton find(Container root, String name) {
        for (Component child : root.getComponents()) {
            if (child instanceof JButton button && name.equals(button.getName())) {
                return button;
            }
            if (child instanceof Container container) {
                JButton result = find(container, name);
                if (result != null) return result;
            }
        }
        return null;
    }
}
