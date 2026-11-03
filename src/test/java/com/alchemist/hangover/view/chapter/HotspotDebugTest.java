package com.alchemist.hangover.view.chapter;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica che i contorni siano disattivati normalmente e attivabili per il collaudo.
 */
class HotspotDebugTest {
    @Test void outlinesStayDisabledByDefaultAndCanBeEnabledTemporarily() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            HotspotButton button = new HotspotButton();
            button.setBounds(120, 210, 180, 120);
            button.setToolTipText("NPC Sigrun");
            button.setVisible(true);
            BufferedImage image = new BufferedImage(180, 120, BufferedImage.TYPE_INT_ARGB);
            Graphics2D painter = image.createGraphics();
            try {
                HotspotButton.setDebugBoundsVisible(false);
                assertFalse(HotspotButton.debugBoundsVisible());
                button.paint(painter);
                assertEquals(0, image.getRGB(1, 1) >>> 24);
                HotspotButton.setDebugBoundsVisible(true);
                button.paint(painter);
                assertTrue((image.getRGB(1, 1) >>> 24) > 0);
            } finally {
                HotspotButton.setDebugBoundsVisible(false);
                painter.dispose();
            }
        });
    }
}
