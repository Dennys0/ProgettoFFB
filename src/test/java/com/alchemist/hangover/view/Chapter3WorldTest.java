package com.alchemist.hangover.view;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Chapter3WorldTest {
    private static final Map<String, String> OPPOSITES = Map.of(
            "UP", "DOWN", "DOWN", "UP",
            "LEFT", "RIGHT", "RIGHT", "LEFT"
    );

    @Test
    void sevenRoomsAreMappedWithTheExpectedResources() {
        assertEquals(7, Chapter3World.backgrounds().size());
        assertEquals(7, Chapter3World.exits().size());
        for (int room = 1; room <= 7; room++) {
            assertTrue(Chapter3World.backgrounds().containsKey(room));
            assertFalse(Chapter3World.roomName(room).isBlank());
            assertTrue(Chapter3World.backgrounds().get(room).startsWith("/images/chapter3/"));
        }
    }

    @Test
    void allConnectionsLeadToExistingRoomsAndCanBeReversed() {
        for (Map.Entry<Integer, Map<String, Integer>> room : Chapter3World.exits().entrySet()) {
            int from = room.getKey();
            for (Map.Entry<String, Integer> exit : room.getValue().entrySet()) {
                assertTrue(OPPOSITES.containsKey(exit.getKey()));
                assertTrue(Chapter3World.exits().containsKey(exit.getValue()));
                assertEquals(from, Chapter3World.exits().get(exit.getValue())
                                .get(OPPOSITES.get(exit.getKey())),
                        "Percorso senza ritorno dalla stanza " + from
                                + " in direzione " + exit.getKey());
            }
        }
    }

    @Test
    void hubAndFinalBossHaveTheRequiredConnections() {
        assertEquals(Map.of("UP", 2, "DOWN", 5, "LEFT", 3, "RIGHT", 4),
                Chapter3World.exits().get(1));
        assertEquals(Map.of("UP", 5), Chapter3World.exits().get(7));
    }
}
