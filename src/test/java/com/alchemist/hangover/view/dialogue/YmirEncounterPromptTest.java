package com.alchemist.hangover.view.dialogue;

import com.alchemist.hangover.gameplay.chapter.EarthChapterRules;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** The Ymir encounter uses the same accessible fantasy prompt as other bosses. */
class YmirEncounterPromptTest {
    @Test void displaysCorrectMissingRequirementsWithoutBakedText() {
        String both = EarthChapterRules.missingYmirClues(false, false);
        assertTrue(both.contains("Gunnar"));
        assertTrue(both.contains("stele"));
        String one = EarthChapterRules.missingYmirClues(true, false);
        assertFalse(one.contains("Gunnar"));
        assertTrue(one.contains("stele"));
        String other = EarthChapterRules.missingYmirClues(false, true);
        assertTrue(other.contains("Gunnar"));
        assertFalse(other.contains("stele"));
    }

    @Test void earthBossUsesSharedPanelAndTwoAccessibleChoices() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = StoryPromptDialog.buildContent(
                    StoryPromptDialog.Theme.EARTH, "IL CUORE DI ROCCIA — YMIR",
                    "Vuoi tentare il rituale del sonno?",
                    "AFFRONTA YMIR", "NON ANCORA", accepted -> {});
            assertEquals(new Dimension(750, 348), panel.getPreferredSize());
            JButton fight = findButton(panel, "storyPromptAccept");
            JButton retreat = findButton(panel, "storyPromptDecline");
            assertNotNull(fight);
            assertNotNull(retreat);
            assertEquals("AFFRONTA YMIR", fight.getText());
            assertEquals("NON ANCORA", retreat.getText());
            panel.setSize(750,348);
            BufferedImage img = new BufferedImage(750,348,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=img.createGraphics();
            try { assertDoesNotThrow(() -> panel.paint(g)); }
            finally { g.dispose(); }
        });
    }

    private static JButton findButton(Container panel, String name) {
        for(Component child:panel.getComponents()) {
            if(child instanceof JButton button && name.equals(button.getName())) return button;
            if(child instanceof Container parent) {
                JButton result=findButton(parent,name);
                if(result!=null) return result;
            }
        }
        return null;
    }
}
