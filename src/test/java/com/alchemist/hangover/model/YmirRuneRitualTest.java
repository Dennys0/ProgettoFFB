package com.alchemist.hangover.model;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class YmirRuneRitualTest {
    @Test
    void sameSeedAlwaysProducesTheSameThreeSteps() {
        List<YmirRuneRitual.Step> first = YmirRuneRitual.fromSeed(39281);
        assertEquals(first, YmirRuneRitual.fromSeed(39281));
        assertEquals(3, first.size());
        assertEquals(3, first.stream().map(YmirRuneRitual.Step::rune).distinct().count());
        assertEquals(3, first.stream().map(YmirRuneRitual.Step::safeZone).distinct().count());
    }

    @Test
    void differentSeedsCanCreateDifferentRituals() {
        Set<List<YmirRuneRitual.Step>> variants = new HashSet<>();
        for (int seed = 1; seed <= 100; seed++) variants.add(YmirRuneRitual.fromSeed(seed));
        assertTrue(variants.size() >= 10, "La combinazione deve variare tra partite");
    }

    @Test
    void inscriptionReflectsTheExactSequenceForThatSave() {
        List<YmirRuneRitual.Step> steps = YmirRuneRitual.fromSeed(781);
        String inscription = YmirRuneRitual.steleInscription(steps);
        for (int i = 0; i < steps.size(); i++) {
            assertTrue(inscription.contains(steps.get(i).rune().verse()));
            assertTrue(inscription.contains(steps.get(i).safeZone().label().toUpperCase()));
        }
        assertThrows(IllegalArgumentException.class, () -> YmirRuneRitual.fromSeed(0));
    }
}
