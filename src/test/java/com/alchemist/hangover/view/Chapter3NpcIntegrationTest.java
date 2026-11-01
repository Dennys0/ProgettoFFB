package com.alchemist.hangover.view;

import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regressioni delle quattro nuove interazioni del Dominio di Ymir.
 * Le risorse PNG/TXT sono realmente pubblicate e non sono segnaposto.
 */
class Chapter3NpcIntegrationTest {
    private static final Map<Integer, String> SPEAKERS = Map.of(
            3, "SGHERRO", 4, "VANIR", 5, "BILLINGR", 6, "NIDHOGGR");

    @Test
    void everyNpcHasUniqueRoomAndClickableArea() {
        List<Chapter3Panel.NpcDefinition> npcs = Chapter3Panel.NPC_DEFINITIONS;
        assertEquals(4, npcs.size());
        assertEquals(SPEAKERS.keySet(),
                npcs.stream().map(Chapter3Panel.NpcDefinition::room).collect(Collectors.toSet()));
        Set<String> paths = new HashSet<>();
        for (Chapter3Panel.NpcDefinition npc : npcs) {
            assertTrue(paths.add(npc.dialogue()), "Non condividere il dialogo tra NPC");
            assertTrue(npc.action().startsWith("Parla con "));
            Rectangle b = npc.bounds();
            assertTrue(b.x >= 0 && b.y >= 80, "L'hotspot deve essere sotto l'HUD");
            assertTrue(b.width >= 140 && b.height >= 150);
            assertTrue(b.x + b.width <= 1280 && b.y + b.height <= 720);
        }
        Rectangle cauldron = new Rectangle(548, 282, 235, 260);
        Rectangle imp = npcs.stream().filter(n -> n.room() == 3).findFirst()
                .orElseThrow().bounds();
        assertFalse(cauldron.intersects(imp),
                "Cliccare sullo sgherro non deve aprire il calderone.");
        Rectangle stele = new Rectangle(150, 238, 175, 235);
        Rectangle dragon = npcs.stream().filter(n -> n.room() == 6).findFirst()
                .orElseThrow().bounds();
        assertFalse(stele.intersects(dragon),
                "Il drago non deve nascondere la stele delle rune.");
    }

    @Test
    void allFourDialoguesContainExpectedSpeakersAndReadableStory() throws Exception {
        for (Chapter3Panel.NpcDefinition npc : Chapter3Panel.NPC_DEFINITIONS) {
            String resource = npc.dialogue();
            assertTrue(resource.startsWith("/dialogues/ch3_"));
            InputStream stream = getClass().getResourceAsStream(resource);
            assertNotNull(stream, () -> "Dialogo non trovato: " + resource);
            boolean hasNarrator = false, hasArthur = false, hasNpc = false;
            int lines = 0;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isBlank() || line.startsWith("#")) continue;
                    String[] parts = line.split("\\|", 2);
                    assertEquals(2, parts.length, "Usare PARLANTE|TESTO: " + line);
                    assertFalse(parts[1].isBlank());
                    hasNarrator |= parts[0].equals("NARRATOR");
                    hasArthur |= parts[0].equals("ARTHUR");
                    hasNpc |= parts[0].equals(SPEAKERS.get(npc.room()));
                    if (parts[0].equals("NARRATOR")) {
                        assertFalse(parts[1].matches("(?i)^SCENA\\s+\\d+.*"),
                                "La numerazione delle scene non va mostrata nel gioco");
                    }
                    lines++;
                }
            }
            assertTrue(lines >= 15, "Dialogo incompleto: " + resource);
            assertTrue(hasNarrator && hasArthur && hasNpc,
                    "Servono narratore, Arthur e il protagonista della scena: " + resource);
        }
    }

    @Test
    void allPortraitsAreAvailableWithoutModifyingSceneryArt() {
        for (String path : List.of(
                "/images/characters/diavoletto_realms.png",
                "/images/chapter3/npc/visir_or_vanir.png",
                "/images/chapter3/npc/billingr.png",
                "/images/chapter3/npc/nidhoggr.png")) {
            assertNotNull(getClass().getResource(path), "Ritratto mancante: " + path);
        }
        for (int room = 3; room <= 6; room++) {
            assertNotNull(getClass().getResource(ChapterLayouts.forChapter(3).backgrounds().get(room)));
        }
        for (String speaker : SPEAKERS.values()) {
            Rectangle box = Chapter3Panel.portraitBoundsFor(speaker);
            assertTrue(box.y >= 0 && box.y + box.height <= 505);
            assertTrue(box.x >= 0 && box.x + box.width <= 1280);
        }
    }
}
