package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.Rectangle;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** La gestione condivisa degli NPC non altera i click ne' la stanza corrente. */
class NpcHotspotRegistryTest {
    @AfterEach void reset() { GameState.getInstance().resetState(); }

    private static final class ExampleChapter extends AbstractChapterPanel {
        final AtomicInteger interactions = new AtomicInteger();
        final HotspotButton first;
        final HotspotButton second;

        ExampleChapter() {
            initializeChapterBase(null, Map.of(
                    1, "/images/chapter3/room1_hub.png",
                    2, "/images/chapter3/room2_rune.png"
            ), Map.of(1, Map.of("UP", 2), 2, Map.of("DOWN", 1)));
            first = registerNpcHotspot(1, new Rectangle(200, 200, 125, 180),
                    "Parla con il primo", interactions::incrementAndGet);
            second = registerNpcHotspot(2, new Rectangle(470, 230, 145, 175),
                    "Parla con il secondo", interactions::incrementAndGet);
            loadRoom(1);
        }

        @Override protected void onRoomChanged() {
            setRegisteredNpcHotspotsVisible(true);
        }
    }

    @Test void registryFiltersHotspotsByRoomAndPreservesActions() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ExampleChapter chapter = new ExampleChapter();
            assertTrue(chapter.first.isVisible());
            assertFalse(chapter.second.isVisible());
            assertEquals("Parla con il primo", chapter.first.getToolTipText());
            assertEquals("Parla con il secondo",
                    chapter.second.getAccessibleContext().getAccessibleName());
            chapter.first.doClick();
            assertEquals(1, chapter.interactions.get());
            chapter.loadRoom(2);
            assertFalse(chapter.first.isVisible());
            assertTrue(chapter.second.isVisible());
            chapter.second.doClick();
            assertEquals(2, chapter.interactions.get());
            chapter.setRegisteredNpcHotspotsVisible(false);
            assertFalse(chapter.first.isVisible());
            assertFalse(chapter.second.isVisible());
        });
    }
}
