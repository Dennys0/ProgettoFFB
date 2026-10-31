package com.alchemist.hangover.view;

import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class HotspotButtonTest {
    @Test
    void narrativeHotspotsShowOnlyLocalizedFeedbackWithoutBorders() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            HotspotButton button = new HotspotButton();
            button.setSize(180, 100);
            button.setToolTipText("Parla con Gunnar");
            button.getModel().setRollover(true);
            assertFalse(button.isHoverHighlightEnabled());
            assertEquals(Cursor.HAND_CURSOR, button.getCursor().getType());
            assertFalse(button.isOpaque());
            assertFalse(button.isBorderPainted());
            BufferedImage buffer = new BufferedImage(180, 100, BufferedImage.TYPE_INT_ARGB);
            Graphics g = buffer.createGraphics();
            try { button.paint(g); }
            finally { g.dispose(); }
            assertEquals(0, buffer.getRGB(1, 1) >>> 24,
                    "Nessun bordo intorno all'hotspot narrativo.");
            assertTrue((buffer.getRGB(90, 50) >>> 24) > 0,
                    "Un piccolo bagliore indica l'interazione.");
            assertEquals("Parla con Gunnar", button.getToolTipText());
            assertEquals(new Color(31, 27, 38), button.createToolTip().getBackground());
        });
    }

    @Test
    void collectibleItemsCanRetainTheirOptionalGlow() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            HotspotButton button = new HotspotButton();
            button.setSize(180, 100);
            button.setHoverHighlightEnabled(true);
            button.getModel().setRollover(true);
            assertTrue(button.isHoverHighlightEnabled());
            BufferedImage buffer = new BufferedImage(180, 100, BufferedImage.TYPE_INT_ARGB);
            Graphics g = buffer.createGraphics();
            try { button.paint(g); }
            finally { g.dispose(); }
            assertTrue((buffer.getRGB(90, 50) >>> 24) > 0,
                    "Gli oggetti possono ancora evidenziarsi al passaggio del mouse.");
        });
    }
}
