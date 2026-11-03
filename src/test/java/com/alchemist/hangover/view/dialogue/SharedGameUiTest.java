package com.alchemist.hangover.view.dialogue;

import com.alchemist.hangover.view.cauldron.CauldronArtworkPanel;
import com.alchemist.hangover.view.cauldron.CauldronController;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import org.junit.jupiter.api.Test;
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
            DialogueBox box = new DialogueBox();
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
    void everyChapterUsesOneSharedCauldronArtwork() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            CauldronArtworkPanel panel = new CauldronArtworkPanel();
            panel.setSize(980, 680);
            BufferedImage image = new BufferedImage(980, 680,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = image.createGraphics();
            try {
                assertDoesNotThrow(() -> panel.paint(graphics));
            } finally {
                graphics.dispose();
            }
            for (int chapter = 1; chapter <= 3; chapter++) {
                assertNotNull(com.alchemist.hangover.gameplay.cauldron.RecipeManager.forChapter(chapter));
                assertNotNull(CauldronController.completionScene(chapter));
            }
        });
    }
}
