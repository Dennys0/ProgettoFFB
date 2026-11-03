package com.alchemist.hangover.view.dialogue;

import javax.swing.ImageIcon;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Intro e capitoli condividono lo stesso ridimensionamento dei ritratti. */
class SharedPortraitLoadingTest {
    @Test void portraitsKeepRequestedHeightAndMissingImagesRemainOptional() {
        ImageIcon chapterPortrait = DialogueBox.scaledPortrait(
                "/images/chapter1/npc/martino_realms.png", 520);
        assertNotNull(chapterPortrait);
        assertEquals(520, chapterPortrait.getIconHeight());
        assertTrue(chapterPortrait.getIconWidth() > 0);

        ImageIcon introPortrait = DialogueBox.scaledPortrait(
                "/images/chapter1/npc/martino_realms.png", 550);
        assertNotNull(introPortrait);
        assertEquals(550, introPortrait.getIconHeight());

        assertNull(DialogueBox.scaledPortrait("/images/characters/not-real.png", 520));
    }
}
