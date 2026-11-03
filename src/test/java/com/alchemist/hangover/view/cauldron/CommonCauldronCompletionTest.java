package com.alchemist.hangover.view.cauldron;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommonCauldronCompletionTest {
    @Test void eachChapterHasExactlyOneCompletionScene() {
        assertEquals("TRANSITION_CH1_CH2",CauldronController.completionScene(1));
        assertEquals("TRANSITION_CH2_CH3",CauldronController.completionScene(2));
        assertEquals("CHAPTER3_COMPLETE",CauldronController.completionScene(3));
        assertEquals("EPILOGUE", CauldronController.completionScene(4));
        assertThrows(IllegalArgumentException.class,()->CauldronController.completionScene(5));
    }
}
