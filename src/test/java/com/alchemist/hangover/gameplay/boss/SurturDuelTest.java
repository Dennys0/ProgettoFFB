package com.alchemist.hangover.gameplay.boss;

import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SurturDuelTest {

    private static SurturDuel.Lane safe(SurturDuel duel) {
        return java.util.Arrays.stream(SurturDuel.Lane.values())
                .filter(l -> !duel.danger().contains(l)).findFirst().orElseThrow();
    }

    @Test void attacksBecomeMoreDifficultButAlwaysHaveASafeLane() {
        SurturDuel duel = new SurturDuel(new Random(7));
        assertEquals(SurturDuel.Lane.CENTER, duel.position());
        for (int n = 0; n < 6; n++) {
            SurturDuel.Attack expected = n < 2 ? SurturDuel.Attack.SINGLE
                    : n < 4 ? SurturDuel.Attack.DOUBLE : SurturDuel.Attack.SWEEP;
            assertEquals(expected, duel.attack());
            assertEquals(n < 2 ? 1 : 2, duel.danger().size());
            duel.move(safe(duel));
            assertEquals(SurturDuel.Result.DODGED, duel.impact());
            if (expected == SurturDuel.Attack.SWEEP) {
                assertEquals(n, duel.completedAttacks(), "La spazzata ha due onde");
                assertEquals(1, duel.sweepWave());
                duel.move(safe(duel));
                assertEquals(SurturDuel.Result.DODGED, duel.impact());
            }
            assertEquals(n + 1, duel.completedAttacks());
        }
        assertTrue(duel.finalTrap());
        assertEquals(SurturDuel.Attack.FINAL, duel.attack());
        assertFalse(duel.taunt()); // Arthur e' rimasto su una corsia laterale.
        duel.move(SurturDuel.Lane.CENTER);
        assertTrue(duel.taunt());
        assertEquals(java.util.Set.of(SurturDuel.Lane.CENTER), duel.danger());
        duel.move(SurturDuel.Lane.LEFT);
        assertEquals(SurturDuel.Result.VICTORY, duel.impact());
    }

    @Test void gettingHitDoesNotProgressToVictoryAndFinalTrapCanRetry() {
        SurturDuel duel = new SurturDuel(new Random(13));
        var hitLane = duel.danger().iterator().next();
        duel.move(hitLane);
        assertEquals(SurturDuel.Result.HIT, duel.impact());
        assertEquals(1, duel.completedAttacks());
        while (!duel.finalTrap()) {
            duel.move(safe(duel));
            duel.impact();
        }
        duel.move(SurturDuel.Lane.CENTER);
        assertTrue(duel.taunt());
        assertEquals(SurturDuel.Result.HIT, duel.impact());
        assertFalse(duel.lured());
        assertTrue(duel.taunt());
        duel.move(SurturDuel.Lane.RIGHT);
        assertEquals(SurturDuel.Result.VICTORY, duel.impact());
    }

    @Test void tauntIsOnlyPossibleAtCenterInTheFinalRound() {
        SurturDuel duel = new SurturDuel(new Random(7));
        assertFalse(duel.taunt());
        assertEquals(1, duel.danger().size());
    }
}
