package com.alchemist.hangover.gameplay.boss;

import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SurturDuelTest {
    @Test void fourDodgesThenLureAndEscape() {
        SurturDuel duel = new SurturDuel(new Random(7));
        while (!duel.finalTrap()) {
            var safe = java.util.Arrays.stream(SurturDuel.Lane.values())
                    .filter(l -> !duel.danger().contains(l)).findFirst().orElseThrow();
            assertEquals(SurturDuel.Result.DODGED, duel.choose(safe));
        }
        assertEquals(4, duel.round());
        assertEquals(SurturDuel.Result.HIT, duel.choose(SurturDuel.Lane.LEFT));
        assertEquals(SurturDuel.Result.LURED, duel.choose(SurturDuel.Lane.CENTER));
        assertEquals(SurturDuel.Result.HIT, duel.choose(SurturDuel.Lane.CENTER));
        assertEquals(SurturDuel.Result.VICTORY, duel.choose(SurturDuel.Lane.RIGHT));
    }

    @Test void attackLanesAreVisibleAndNeverAllDangerous() {
        SurturDuel duel = new SurturDuel(new Random(42));
        assertEquals(1, duel.danger().size());
        var struck = duel.danger().iterator().next();
        assertEquals(SurturDuel.Result.HIT, duel.choose(struck));
        assertEquals(0, duel.round());
    }
}
