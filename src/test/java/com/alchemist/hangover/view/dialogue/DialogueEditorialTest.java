package com.alchemist.hangover.view.dialogue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Verifica che le intestazioni non siano recitate e che gli indizi descrivano le ricette giocabili. */
class DialogueEditorialTest {
    private static final List<String> ALL_DIALOGUES = List.of(
            "boss_intro_dialogue.txt",
            "ch2_captain_dialogue.txt",
            "ch2_hronn_dialogue.txt",
            "ch2_jormungandr_dialogue.txt",
            "ch2_marmael_dialogue.txt",
            "ch2_sgherro_dialogue.txt",
            "ch2_sirena_dialogue.txt",
            "ch3_caldron_revised.txt",
            "ch3_gunnar.txt",
            "ch3_gunnar_repeat.txt",
            "ch3_gunnar_reward.txt",
            "ch3_mining_revised.txt",
            "ch3_nighodd_revised.txt",
            "ch3_vanir_revised.txt",
            "ch4_bersercker_dialogue.txt",
            "ch4_blacksmith_dialogue.txt",
            "ch4_diavoletto_dialogue.txt",
            "ch4_hela.txt",
            "ch4_oldman_dialogue.txt",
            "ch4_valkyrie_dialogue.txt",
            "diavoletto_dialogue.txt",
            "grifone_dialogue.txt",
            "huginn_dialogue.txt",
            "intro_dialogue.txt",
            "martino_cassa.txt",
            "martino_chiave.txt",
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
    void eachGuideNamesTheThreeIngredientsUsedAtTheCauldron() throws Exception {
        Map<String, List<String>> guides = Map.of(
                "martino_dialogue.txt", List.of("Soffio di Hræsvelgr", "Piuma di Folgore", "Rugiada delle Nuvole di Asgard"),
                "siegfried_dialogue_2.txt", List.of("Schiuma di Ægir", "Corallo di Rán", "Sale di Jörmungandr"),
                "ch3_gunnar_reward.txt", List.of("Muschio di Midgard", "Frammento di Yggdrasil", "Lacrima di Ymir"),
                "ch4_hela.txt", List.of("Cenere di Fenrir", "magma filtrato di Svartálfaheimr", "Brace di Muspelheim va per ultima"));
        for (var guide : guides.entrySet()) {
            String script = String.join(" ", load(guide.getKey()));
            for (String ingredient : guide.getValue())
                assertTrue(script.contains(ingredient), guide.getKey() + " — " + ingredient);
        }
    }

    @Test
    void underwaterEncountersOpenWithSceneNarration() throws Exception {
        for (String name : ALL_DIALOGUES) {
            if (name.startsWith("ch2_")) assertTrue(load(name).get(0).startsWith("NARRATOR|"), name);
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
