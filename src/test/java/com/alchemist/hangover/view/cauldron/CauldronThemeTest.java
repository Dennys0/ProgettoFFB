package com.alchemist.hangover.view.cauldron;

import com.alchemist.hangover.view.dialogue.StoryPromptDialog;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica l’associazione fra capitolo e tema della finestra del calderone.
 */
class CauldronThemeTest {
    @Test void chapterThemeMatchesEachWorld() {
        assertEquals(StoryPromptDialog.Theme.SKY,CauldronController.themeForChapter(1));
        assertEquals(StoryPromptDialog.Theme.OCEAN,CauldronController.themeForChapter(2));
        assertEquals(StoryPromptDialog.Theme.EARTH,CauldronController.themeForChapter(3));
    }
}
