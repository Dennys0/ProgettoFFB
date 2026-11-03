package com.alchemist.hangover.gameplay.boss;

import java.util.Random;
import org.junit.jupiter.api.Test;
import static com.alchemist.hangover.gameplay.boss.SurturDuel.*;
import static org.junit.jupiter.api.Assertions.*;

class SurturDuelTest {
    private void dodge(SurturDuel duel) {
        for (Lane lane : Lane.values()) {
            if (!duel.danger().contains(lane)) {
                duel.move(lane);
                return;
            }
        }
        fail("Nessuna via di fuga");
    }

    private void reachAim(SurturDuel duel) {
        for (int i = 0; i <= duel.seals(); i++) {
            assertEquals(Attack.SURGE, duel.attack());
            dodge(duel);
            assertEquals(Result.DODGED, duel.impact());
        }
        assertEquals(Attack.AIM, duel.attack());
    }

    @Test void swordMustBreakBothSealsBeforeReachingTheCauldron() {
        for (int seed = 0; seed < 12; seed++) {
            var duel = new SurturDuel(new Random(seed));
            var first = duel.seal();
            for (int i = 0; i < 3; i++) {
                reachAim(duel);
                if (i == 1) assertNotEquals(first, duel.seal());
                if (i == 2) assertEquals(Lane.CENTER, duel.seal());
                duel.move(duel.seal());
                assertEquals(Result.LOCKED, duel.impact());
                dodge(duel);
                assertEquals(i == 2 ? Result.VICTORY : Result.BROKEN, duel.impact());
            }
        }
    }

    @Test void prematureDodgeMissesAndLateDodgeCostsHealth() {
        var duel = new SurturDuel(new Random(9));
        reachAim(duel);
        duel.move(Lane.CENTER);
        duel.impact();
        dodge(duel);
        assertEquals(Result.MISS, duel.impact());
        reachAim(duel);
        duel.move(duel.seal());
        duel.impact();
        assertEquals(Result.HIT, duel.impact());
        assertEquals(0, duel.seals());
    }
}
