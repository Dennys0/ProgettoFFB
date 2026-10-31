package com.alchemist.hangover.view;

import org.junit.jupiter.api.Test;
import java.awt.Rectangle;
import static org.junit.jupiter.api.Assertions.*;

class YmirStoryPromptTest {
    @Test
    void deliveredArtworkIsAvailableAndClickableAreasFitInsidePopup() {
        assertTrue(YmirStoryPrompt.hasArtwork("/images/ui/ymir_warning.png"));
        assertTrue(YmirStoryPrompt.hasArtwork("/images/ui/ymir_fight.png"));

        for (Rectangle rect : new Rectangle[]{
                YmirStoryPrompt.hotspot(330, 611, 543, 116, YmirStoryPrompt.fightCrop()),
                YmirStoryPrompt.hotspot(887, 611, 468, 116, YmirStoryPrompt.fightCrop()),
                YmirStoryPrompt.hotspot(1417, 150, 108, 100, YmirStoryPrompt.fightCrop()),
                YmirStoryPrompt.hotspot(650, 655, 384, 115, YmirStoryPrompt.warningCrop())}) {
            assertTrue(rect.width > 40 && rect.height > 25);
            assertTrue(rect.x >= 0 && rect.y >= 0);
            assertTrue(rect.x + rect.width <= 960);
            assertTrue(rect.y + rect.height <= 480);
        }
    }
}
