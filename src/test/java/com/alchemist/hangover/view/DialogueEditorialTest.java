package com.alchemist.hangover.view;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** Ensures scene headings stay out of spoken narration and editorial
 * edits keep every line of the six new underwater conversations.
 */
class DialogueEditorialTest {
    private static final List<String> ALL_DIALOGUES = List.of(
            "boss_intro_dialogue.txt",
            "ch2_captain_dialogue.txt",
            "ch2_hronn_dialogue.txt",
            "ch2_jormungandr_dialogue.txt",
            "ch2_marmael_dialogue.txt",
            "ch2_sgherro_dialogue.txt",
            "ch2_sirena_dialogue.txt",
            "ch3_gunnar.txt",
            "diavoletto_dialogue.txt",
            "grifone_dialogue.txt",
            "huginn_dialogue.txt",
            "intro_dialogue.txt",
            "martino_dialogue.txt",
            "mozzo_dialogue.txt",
            "ran_boss_dialogue.txt",
            "siegfried_dialogue_1.txt",
            "siegfried_dialogue_2.txt",
            "vedrfolnir_dialogue.txt"
    );

    private static final Pattern HARSH_WORDS = Pattern.compile(
            "\\b(cazzo|puttana|troia|merda|stronzo|stronzata|bastardo|vaffanculo)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    @Test
    void dialogueFilesNoLongerSpeakSceneNumbersOrHarshArthurSwearWords() throws Exception {
        for (String name : ALL_DIALOGUES) {
            for (String line : load(name)) {
                assertFalse(line.matches("NARRATOR\\|SCENA\\s+\\d+.*"),
                        "L'intestazione della scena non deve apparire nel gioco: " + name);
                if (line.startsWith("ARTHUR|")) {
                    assertFalse(HARSH_WORDS.matcher(line).find(),
                            "Il linguaggio di Arthur deve restare ironico ma piu' moderato: "
                                    + name + " -> " + line);
                }
            }
        }
    }

    @Test
    void sixChapterTwoDialoguesKeepOriginalLineCountsAndNarrativeOpenings()
            throws Exception {
        Map<String, Integer> expected = Map.of(
                "ch2_captain_dialogue.txt", 27,
                "ch2_hronn_dialogue.txt", 23,
                "ch2_jormungandr_dialogue.txt", 22,
                "ch2_marmael_dialogue.txt", 31,
                "ch2_sgherro_dialogue.txt", 19,
                "ch2_sirena_dialogue.txt", 22
        );
        for (Map.Entry<String, Integer> entry : expected.entrySet()) {
            List<String> lines = load(entry.getKey());
            assertEquals(entry.getValue(), lines.size(),
                    "Non cambiare il numero di battute e le parti narrative");
            assertTrue(lines.get(0).startsWith("NARRATOR|"),
                    "La descrizione introduttiva deve rimanere: " + entry.getKey());
            assertTrue(lines.get(0).length() > 80,
                    "Conservare la descrizione, eliminando solo SCENA e titolo");
        }
    }

    private static List<String> load(String filename) throws Exception {
        try (InputStream resource = DialogueEditorialTest.class.getResourceAsStream(
                "/dialogues/" + filename)) {
            assertNotNull(resource, "Dialogo mancante: " + filename);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                return reader.lines().filter(line -> !line.isBlank()
                        && !line.stripLeading().startsWith("#")).toList();
            }
        }
    }
}
