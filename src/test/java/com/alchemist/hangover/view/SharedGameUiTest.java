package com.alchemist.hangover.view;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/** Verifica l'identita' visiva comune a dialoghi e calderoni. */
class SharedGameUiTest {
    @Test
    void bothClassicIllustrationsAreAvailable() {
        assertNotNull(getClass().getResource("/images/ui/dialogue_box.png"));
        assertNotNull(getClass().getResource("/images/ui/cauldron_bg.png"));
    }

    @Test
    void dialogueFrameExposesStablePositionsAndCanBePaintedHeadlessly() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            SharedDialogueBox box = new SharedDialogueBox();
            box.setSize(1200, 200);
            assertEquals(new Rectangle(65, 25, 500, 30),
                    box.getSpeakerLabel().getBounds());
            assertEquals(new Rectangle(65, 60, 1070, 95),
                    box.getTextLabel().getBounds());
            assertEquals("MOSTRA FRASE", box.getRevealLabel().getText());
            BufferedImage buffer = new BufferedImage(1200, 200, BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = buffer.createGraphics();
            try {
                assertDoesNotThrow(() -> box.paint(graphics));
            } finally {
                graphics.dispose();
            }
        });
    }

    @Test
    void thirdChapterUsesTheSameCauldronArtworkWithoutANewWindow() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            YmirCauldronPanel panel = new YmirCauldronPanel(
                    () -> {}, () -> {}, message -> {});
            assertInstanceOf(SharedCauldronArtworkPanel.class, panel);
            assertEquals(840, panel.getPreferredSize().width);
            assertEquals(583, panel.getPreferredSize().height);
            assertDoesNotThrow(panel::refreshIngredients);
        });
    }
}
