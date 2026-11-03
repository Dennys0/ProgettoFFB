package com.alchemist.hangover.view.chapter;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Verifica che i quattro mondi condividano lo stesso formato di configurazione. */
class ChapterLayoutsTest {
    private static final Map<String, String> OPPOSITES = Map.of(
            "UP", "DOWN", "DOWN", "UP", "LEFT", "RIGHT", "RIGHT", "LEFT");

    @Test void sevenBackgroundsAndExitsAreDefinedForEachChapter() {
        for (int chapter = 1; chapter <= 4; chapter++) {
            var layout = ChapterLayouts.forChapter(chapter);
            assertEquals(7, layout.backgrounds().size());
            assertEquals(7, layout.exits().size());
            for (int room = 1; room <= 7; room++) {
                String image = layout.backgrounds().get(room);
                assertNotNull(image);
                assertTrue(image.startsWith("/images/chapter" + chapter + "/"));
                assertNotNull(getClass().getResource(image),
                        "Scenario non trovato: " + image);
            }
        }
        assertThrows(IllegalArgumentException.class, () -> ChapterLayouts.forChapter(5));
    }

    @Test void everyDirectionHasAReversibleConnection() {
        for (int chapter = 1; chapter <= 4; chapter++) {
            var exits = ChapterLayouts.forChapter(chapter).exits();
            for (var room : exits.entrySet()) {
                for (var direction : room.getValue().entrySet()) {
                    assertTrue(OPPOSITES.containsKey(direction.getKey()));
                    assertEquals(room.getKey(),
                            exits.get(direction.getValue()).get(OPPOSITES.get(direction.getKey())),
                            "Percorso non reversibile nel capitolo " + chapter);
                }
            }
        }
    }

    @Test void domainOfYmirKeepsItsHubAndBossPaths() {
        var exits = ChapterLayouts.forChapter(3).exits();
        assertEquals(Map.of("UP", 2, "DOWN", 5, "LEFT", 3, "RIGHT", 4), exits.get(1));
        assertEquals(Map.of("UP", 5), exits.get(7));
        assertThrows(UnsupportedOperationException.class,
                () -> ChapterLayouts.forChapter(1).backgrounds().clear());
    }
}
