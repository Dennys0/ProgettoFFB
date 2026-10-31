package com.alchemist.hangover.view;

import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

class EarthBreathEffectTest {
    @Test
    void pulseIsNonInteractiveAndCanStopWithoutCoveringNavigation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            EarthBreathEffect effect = new EarthBreathEffect();
            effect.setSize(320, 140);
            assertFalse(effect.contains(160, 70));
            assertFalse(effect.isActive());
            effect.startEffect();
            assertTrue(effect.isActive());
            BufferedImage frame = new BufferedImage(320, 140, BufferedImage.TYPE_INT_ARGB);
            Graphics g = frame.createGraphics();
            try { assertDoesNotThrow(() -> effect.paint(g)); }
            finally { g.dispose(); }
            effect.stopEffect();
            assertFalse(effect.isActive());
            assertFalse(effect.isVisible());
        });
    }
}
