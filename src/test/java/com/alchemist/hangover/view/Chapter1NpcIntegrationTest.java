package com.alchemist.hangover.view;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regressioni narrative: ogni NPC deve avere il proprio dialogo e
 * conversare con il falco NON deve scatenare subito il boss.
 */
class Chapter1NpcIntegrationTest {
    private static final Map<String, String> DIALOGUES = Map.of(
            "mozzo", "MOZZO",
            "grifone", "GRIFONE",
            "diavoletto", "DIAVOLETTO",
            "huginn", "HUGINN",
            "vedrfolnir", "VEDRFOLNIR"
    );

    @Test
    void allFiveNpcsHaveReadableDialogueWithTheExpectedSpeaker() throws Exception {
        for (Map.Entry<String, String> npc : DIALOGUES.entrySet()) {
            String resource = "/dialogues/" + npc.getKey() + "_dialogue.txt";
            InputStream file = getClass().getResourceAsStream(resource);
            assertNotNull(file, () -> "Dialogo mancante: " + resource);
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.strip();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    assertTrue(line.contains("|"), () -> "Riga non valida in " + resource);
                    String[] parts = line.split("\\|", 2);
                    assertEquals(2, parts.length, "Il dialogo deve usare PARLANTE|TESTO");
                    assertFalse(parts[0].isBlank());
                    assertFalse(parts[1].isBlank());
                    lines.add(line);
                }
            }
            assertTrue(lines.size() >= 5, () -> "Dialogo troppo corto: " + resource);
            assertTrue(lines.stream().anyMatch(line ->
                    line.startsWith(npc.getValue() + "|")), () ->
                    "Non c'e' una battuta di " + npc.getValue() + " in " + resource);
        }
    }

    @Test
    void newCharacterArtAndFiveBackgroundsExist() {
        for (String name : new String[]{"mozzo", "grifone", "diavoletto"}) {
            assertNotNull(getClass().getResource(
                    "/images/characters/" + name + "_realms.png"),
                    "Manca lo sprite di " + name);
        }
        for (String background : new String[]{"room1_deck.png", "room3_prow.png",
                "room4_lab.png", "room6_crowsnest.png", "room7_boss.png"}) {
            assertNotNull(getClass().getResource("/images/chapter1/" + background),
                    "Manca lo scenario " + background);
        }
    }

    @Test
    void onlyBossIntroductionCanOpenBossFight() {
        assertTrue(Chapter1Panel.opensBossAfterDialogue(
                Chapter1Panel.DialogueKind.BOSS_INTRO, 7, false));
        assertFalse(Chapter1Panel.opensBossAfterDialogue(
                Chapter1Panel.DialogueKind.NPC, 7, false),
                "Veðrfölnir non deve avviare il boss al termine del dialogo");
        assertFalse(Chapter1Panel.opensBossAfterDialogue(
                Chapter1Panel.DialogueKind.MARTINO, 7, false));
        assertFalse(Chapter1Panel.opensBossAfterDialogue(
                Chapter1Panel.DialogueKind.BOSS_INTRO, 7, true));
        assertFalse(Chapter1Panel.opensBossAfterDialogue(
                Chapter1Panel.DialogueKind.BOSS_INTRO, 6, false));
    }
}
