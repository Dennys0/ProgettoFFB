package com.alchemist.hangover.view;

import org.junit.jupiter.api.Test;
import javax.swing.ImageIcon;
import static org.junit.jupiter.api.Assertions.*;

/** Intro e capitoli condividono lo stesso ridimensionamento dei ritratti. */
class SharedPortraitLoadingTest {
    @Test void portraitsKeepRequestedHeightAndMissingImagesRemainOptional() {
        ImageIcon chapterPortrait = SharedDialogueBox.scaledPortrait(
                "/images/characters/martino_realms.png", 520);
        assertNotNull(chapterPortrait);
        assertEquals(520, chapterPortrait.getIconHeight());
        assertTrue(chapterPortrait.getIconWidth() > 0);

        ImageIcon introPortrait = SharedDialogueBox.scaledPortrait(
                "/images/characters/martino_realms.png", 550);
        assertNotNull(introPortrait);
        assertEquals(550, introPortrait.getIconHeight());

        assertNull(SharedDialogueBox.scaledPortrait("/images/characters/not-real.png", 520));
    }
}
