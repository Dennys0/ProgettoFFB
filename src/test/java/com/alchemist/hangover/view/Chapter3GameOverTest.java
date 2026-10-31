package com.alchemist.hangover.view;

import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

/** Il Game Over deve distinguere un boss dalla miscela distrutta. */
class Chapter3GameOverTest {
    @Test
    void cauldronFailureHasARecipeRetryAndBossFailureRestoresTheOriginalLabel()
            throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            GameOverPanel panel = new GameOverPanel();
            panel.showCauldronFailure("La Radice Nera ha sciolto il boccale");
            assertTrue(panel.isCauldronFailure());
            assertEquals("RIPROVA LA RICETTA", retryButton(panel).getText());

            panel.showReason("Ymir ha travolto Arthur");
            assertFalse(panel.isCauldronFailure());
            assertEquals("RIPROVA LO SCONTRO", retryButton(panel).getText());
        });
    }

    private static JButton retryButton(GameOverPanel panel) {
        for (java.awt.Component component : panel.getComponents()) {
            if (component instanceof JButton button
                    && button.getText().startsWith("RIPROVA")) return button;
        }
        fail("Pulsante RIPROVA mancante nel Game Over");
        return null;
    }
}
