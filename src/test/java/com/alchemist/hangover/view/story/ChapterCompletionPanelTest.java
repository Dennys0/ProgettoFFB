package com.alchemist.hangover.view.story;

import com.alchemist.hangover.gameplay.chapter.ChapterProgression;
import com.alchemist.hangover.model.GameState;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Controlla schermate condivise e salvataggi di passaggio senza modificare la partita attiva.
 */
class ChapterCompletionPanelTest {
    @Test void oneScreenProvidesThreeChapterCompletionsAndOptionalArtwork() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (int chapter=1;chapter<=3;chapter++) {
                ChapterCompletionPanel.Details details = ChapterCompletionPanel.detailsFor(chapter);
                assertEquals(chapter,details.chapter());
                assertTrue(details.title().contains("COMPLETATO"));
                assertTrue(details.imagePath().startsWith("/images/chapter"+chapter+"/items/"));
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

    @Test void savedCompletionsResumeInNextChapterIncludingTheFourth() {
        GameState state=GameState.getInstance();
        try {
            state.resetState();
            state.setChapter(1);
            state.setCurrentRoom(4);
            state.setBossDefeated(true);
            ChapterProgression.advanceToNextChapter(state,1);
            assertEquals(2,state.getChapter());
            assertEquals(1,state.getCurrentRoom());
            assertFalse(state.isBossDefeated());
            ChapterProgression.advanceToNextChapter(state,2);
            assertEquals(3,state.getChapter());
            assertEquals(1,state.getCurrentRoom());
            assertTrue(state.getYmirRuneSeed()>0);
            state.setCurrentRoom(3);
            state.setYmirOldFashionedCreated(true);
            ChapterProgression.advanceToNextChapter(state,3);
            assertEquals(4,state.getChapter());
            assertEquals(1,state.getCurrentRoom());
            assertTrue(state.isYmirOldFashionedCreated());
        } finally { state.resetState(); }
    }

    @Test void preparingCompletionSaveNeverChangesTheCurrentGameState() {
        GameState state = GameState.getInstance();
        try {
            state.resetState();
            state.setChapter(1);
            state.setCurrentRoom(4);
            state.setCurrentHealth(1);
            state.setBossDefeated(true);
            var snapshot = ChapterProgression
                    .saveAfterCompletion(state, 1);
            assertEquals(1, state.getChapter());
            assertEquals(4, state.getCurrentRoom());
            assertEquals(1, state.getCurrentHealth());
            assertTrue(state.isBossDefeated());
            assertEquals(2, snapshot.chapter());
            assertEquals(1, snapshot.room());
            assertEquals(3, snapshot.health());
            assertFalse(snapshot.bossDefeated());

            state.setChapter(2);
            state.setCurrentRoom(3);
            state.setYmirRuneSeed(0);
            var next = ChapterProgression
                    .saveAfterCompletion(state, 2);
            assertEquals(2, state.getChapter());
            assertEquals(3, state.getCurrentRoom());
            assertEquals(0, state.getYmirRuneSeed());
            assertEquals(3, next.chapter());
            assertTrue(next.ymirRuneSeed() > 0);
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
