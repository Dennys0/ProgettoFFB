package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

class ChapterCompletionPanelTest {
    @Test void oneScreenProvidesThreeChapterCompletionsAndOptionalArtwork() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (int chapter=1;chapter<=3;chapter++) {
                ChapterCompletionPanel.Details details = ChapterCompletionPanel.detailsFor(chapter);
                assertEquals(chapter,details.chapter());
                assertTrue(details.title().contains("COMPLETATO"));
                assertTrue(details.imagePath().endsWith("cocktail_"+chapter+".png"));
                ChapterCompletionPanel panel=new ChapterCompletionPanel(null,chapter);
                assertEquals("chapterCompletionPanel",panel.getName());
                JLabel drink=(JLabel) find(panel,"chapterCompletionDrink");
                assertNotNull(drink);
                assertTrue(drink.getIcon()!=null || !drink.getText().isBlank(),
                        "L'immagine può mancare ma non il segnaposto");
                assertNotNull(find(panel,"chapterCompletionSave"));
                assertNotNull(find(panel,"chapterCompletionNext"));
                assertNotNull(find(panel,"chapterCompletionMenu"));
                panel.setSize(1280,720);
                BufferedImage buffer=new BufferedImage(1280,720,BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics=buffer.createGraphics();
                try { assertDoesNotThrow(()->panel.paint(graphics)); }
                finally { graphics.dispose(); }
            }
        });
    }

    @Test void savedCompletionsResumeInNextChapterWhileThirdRemainsCompleted() {
        GameState state=GameState.getInstance();
        try {
            state.resetState();
            state.setChapter(1);
            state.setCurrentRoom(4);
            state.setBossDefeated(true);
            ChapterCompletionPanel.prepareNextChapterState(1,state);
            assertEquals(2,state.getChapter());
            assertEquals(1,state.getCurrentRoom());
            assertFalse(state.isBossDefeated());
            ChapterCompletionPanel.prepareNextChapterState(2,state);
            assertEquals(3,state.getChapter());
            assertEquals(1,state.getCurrentRoom());
            assertTrue(state.getYmirRuneSeed()>0);
            state.setCurrentRoom(3);
            state.setYmirOldFashionedCreated(true);
            ChapterCompletionPanel.prepareNextChapterState(3,state);
            assertEquals(3,state.getChapter());
            assertEquals(3,state.getCurrentRoom());
            assertTrue(state.isYmirOldFashionedCreated());
        } finally { state.resetState(); }
    }

    private static Component find(Container panel,String name) {
        for(Component child:panel.getComponents()) {
            if(name.equals(child.getName())) return child;
            if(child instanceof Container nested) {
                Component found=find(nested,name);
                if(found!=null) return found;
            }
        }
        return null;
    }
}
