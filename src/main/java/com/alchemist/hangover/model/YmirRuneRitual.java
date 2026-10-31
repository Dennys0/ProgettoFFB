package com.alchemist.hangover.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Rituale deterministico generato da un seed di partita, non da ogni apertura del boss. */
public final class YmirRuneRitual {
    public enum Rune {
        RADICE("Radice", "la radice trattiene il mondo"),
        BATTITO("Battito", "il cuore pulsa nel granito"),
        RESPIRO("Respiro", "il vento torna al silenzio");

        private final String label;
        private final String verse;
        Rune(String label, String verse) {
            this.label = label;
            this.verse = verse;
        }
        public String label() { return label; }
        public String verse() { return verse; }
    }

    public enum Zone {
        SINISTRA("Sinistra"), CENTRO("Centro"), DESTRA("Destra");
        private final String label;
        Zone(String label) { this.label = label; }
        public String label() { return label; }
    }

    public record Step(Rune rune, Zone safeZone) { }

    private YmirRuneRitual() { }

    public static List<Step> fromSeed(int seed) {
        if (seed <= 0) throw new IllegalArgumentException("Seed del rituale non inizializzato");
        Random random = new Random(seed);
        List<Rune> runes = new ArrayList<>(List.of(Rune.values()));
        List<Zone> zones = new ArrayList<>(List.of(Zone.values()));
        Collections.shuffle(runes, random);
        Collections.shuffle(zones, random);
        List<Step> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) result.add(new Step(runes.get(i), zones.get(i)));
        return List.copyOf(result);
    }

    public static List<Step> current(GameState state) {
        return fromSeed(state.ensureYmirRuneSeed());
    }

    /** La stele usa immagini poetiche; Gunnar insegna ad associarle alle rune. */
    public static String steleInscription(List<Step> steps) {
        StringBuilder message = new StringBuilder(
                "LA STELE DEL SONNO PRIMORDIALE\n\n"
                + "Tre versi sigillano il sonno del gigante.\n"
                + "L'ordine muta a ogni nuovo viaggio, mai durante lo stesso.\n\n");
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            message.append(i + 1).append(". Quando ")
                    .append(step.rune().verse())
                    .append(", raggiungi la pietra a ")
                    .append(step.safeZone().label().toUpperCase(java.util.Locale.ROOT))
                    .append(".\n\n");
        }
        message.append("Ascolta il custode: solo lui riconosce i tre segni.");
        return message.toString();
    }
}
