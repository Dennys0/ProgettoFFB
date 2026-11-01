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
 * The PNGs and NPC dialogues are delivered as a separate artist asset pack.
 * Use -Dalchemist.require.chapter2.assets=true after importing the pack:
 * it makes the classpath image and dialogue checks mandatory.
 */
class Chapter2NpcIntegrationTest {
    private static final Map<Integer, String> SPEAKERS = Map.of(
            1, "MARMÆL",
            3, "SGHERRO",
            4, "SIRENA",
            5, "JORMUNGANDR",
            6, "CAPITANO",
            7, "HRÖNN"
    );

    private static final Map<Integer, String> PORTRAITS = Map.of(
            1, "ch2_marmael.png",
            3, "diavoletto_realms.png",
            4, "ch2_sirena.png",
            5, "ch2_jormungandr.png",
            6, "ch2_captain.png",
            7, "ch2_hronn.png"
    );

    @Test
    void chapterTwoDialoguePortraitsUseSpeakerSpecificPositions() {
        Rectangle normal = Chapter2Panel.portraitBoundsFor("SIEGFRIED");
        Rectangle marm = Chapter2Panel.portraitBoundsFor("MARMÆL");
        Rectangle imp = Chapter2Panel.portraitBoundsFor("SGHERRO");
        Rectangle serpent = Chapter2Panel.portraitBoundsFor("JORMUNGANDR");
        assertTrue(marm.x < normal.x, "Marmæl deve comparire a sinistra.");
        assertTrue(imp.x < normal.x, "Lo sgherro deve comparire a sinistra.");
        assertEquals(640, serpent.getCenterX(), 1,
                "Jörmungandr deve essere centrato nella finestra 1280x720.");
        assertTrue(serpent.width > normal.width,
                "Il serpente necessita di spazio per la testa e le squame.");
        assertEquals(normal, Chapter2Panel.portraitBoundsFor("HRÖNN"));
    }

    @Test
    void sixNpcsHaveDistinctRoomAndDialogueBindings() {
        List<Chapter2Panel.NpcDefinition> npcs = Chapter2Panel.NPC_DEFINITIONS;
        assertEquals(6, npcs.size());

        Set<Integer> rooms = npcs.stream()
                .map(Chapter2Panel.NpcDefinition::room).collect(Collectors.toSet());
        assertEquals(SPEAKERS.keySet(), rooms);

        Set<String> dialogues = new HashSet<>();
        for (Chapter2Panel.NpcDefinition npc : npcs) {
            assertTrue(dialogues.add(npc.dialogue()), "Duplicated dialogue binding");
            assertTrue(npc.dialogue().startsWith("/dialogues/ch2_"));
            assertTrue(npc.dialogue().endsWith("_dialogue.txt"));
            assertFalse(npc.tooltip().isBlank());
            Rectangle rect = npc.bounds();
            assertTrue(rect.x >= 0 && rect.y >= 80);
            assertTrue(rect.width >= 100 && rect.height >= 100);
            assertTrue(rect.x + rect.width <= 1280);
            assertTrue(rect.y + rect.height <= 720);
        }

        // Hrönn on the right must not be hidden beneath the boss whirlpool.
        Rectangle hronn = npcs.stream().filter(n -> n.room() == 7)
                .findFirst().orElseThrow().bounds();
        assertFalse(hronn.intersects(new Rectangle(400, 300, 470, 265)));
    }

    @Test
    void talkingToHronnCannotLaunchRanBoss() {
        assertTrue(Chapter2Panel.opensRanAfterDialogue(
                Chapter2Panel.DialogueKind.BOSS_INTRO, 7, false));
        assertFalse(Chapter2Panel.opensRanAfterDialogue(
                Chapter2Panel.DialogueKind.NPC, 7, false));
        assertFalse(Chapter2Panel.opensRanAfterDialogue(
                Chapter2Panel.DialogueKind.SIEGFRIED, 7, false));
        assertFalse(Chapter2Panel.opensRanAfterDialogue(
                Chapter2Panel.DialogueKind.BOSS_INTRO, 7, true));
        assertFalse(Chapter2Panel.opensRanAfterDialogue(
                Chapter2Panel.DialogueKind.BOSS_INTRO, 5, false));
    }

    @Test
    void importedDialoguesHaveValidFormatAndMatchingSpeaker() throws Exception {
        boolean strict = Boolean.getBoolean("alchemist.require.chapter2.assets");
        for (Chapter2Panel.NpcDefinition npc : Chapter2Panel.NPC_DEFINITIONS) {
            InputStream resource = getClass().getResourceAsStream(npc.dialogue());
            if (resource == null) {
                assertFalse(strict, "Import the asset package first: " + npc.dialogue());
                continue;
            }
            boolean foundSpeaker = false;
            int lines = 0;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || line.stripLeading().startsWith("#")) continue;
                    String[] parts = line.trim().split("\\|", 2);
                    assertEquals(2, parts.length, "Invalid PARLANTE|TESTO in " + npc.dialogue());
                    assertFalse(parts[0].isBlank());
                    assertFalse(parts[1].isBlank());
                    foundSpeaker |= SPEAKERS.get(npc.room()).equals(parts[0]);
                    lines++;
                }
            }
            assertTrue(foundSpeaker, "The expected NPC never speaks: " + npc.dialogue());
            assertTrue(lines >= 6, "Too few dialogue lines: " + npc.dialogue());
        }
    }

    @Test
    void characterSpritesAreAvailableAfterPackageImport() {
        boolean strict = Boolean.getBoolean("alchemist.require.chapter2.assets");
        for (String portrait : PORTRAITS.values()) {
            String path = "/images/characters/" + portrait;
            if (strict) {
                assertNotNull(getClass().getResource(path),
                        "Missing sprite after package import: " + path);
            }
        }

        for (int room = 1; room <= 7; room++) {
            String[] backgrounds = {
                    "room1_hall.png", "room2_bar.png", "room3_sacristy.png",
                    "room4_gardens.png", "room5_pit.png",
                    "room6_balcony.png", "room7_boss.png"
            };
            assertNotNull(getClass().getResource("/images/chapter2/" + backgrounds[room - 1]),
                    "Missing chapter 2 background");
        }
    }
}
