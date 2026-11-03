package com.alchemist.hangover.gameplay.boss;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

/** Surtur segue Arthur durante la mira. Una volta piantato il piede, la lama
 * cade sul punto fissato anche se Arthur si è già spostato. */
public final class SurturDuel {
    /** Corsie disponibili per spostamenti, attacchi e sigilli. */
    public enum Lane { LEFT, CENTER, RIGHT }
    /** Fasi del duello: ondata, mira che segue Arthur e colpo sul bersaglio fissato. */
    public enum Attack { SURGE, AIM, STRIKE }
    /** Esito della fase, interpretato dalla finestra per danni, messaggi e avanzamento. */
    public enum Result { DODGED, LOCKED, HIT, MISS, BROKEN, VICTORY }

    private final Random random;
    private final Lane firstSeal;
    private Lane position = Lane.CENTER;
    private Attack attack;
    private int seals;
    private int waves;
    private EnumSet<Lane> danger;

    public SurturDuel(Random random) {
        this.random = random;
        firstSeal = random.nextBoolean() ? Lane.LEFT : Lane.RIGHT;
        startSurge();
    }

    public void move(Lane lane) { position = lane; }
    public Lane position() { return position; }
    public int seals() { return seals; }
    public Attack attack() { return attack; }
    public Set<Lane> danger() { return Set.copyOf(danger); }

    /**
     * Individua il prossimo sigillo: prima un lato casuale, poi quello opposto e infine il
     * centro.
     */
    public Lane seal() {
        if (seals == 0) return firstSeal;
        if (seals == 1) return firstSeal == Lane.LEFT ? Lane.RIGHT : Lane.LEFT;
        return Lane.CENTER;
    }

    /**
     * Riduce il tempo della fase man mano che vengono spezzati i sigilli.
     */
    public int durationMs() {
        return switch (attack) {
            case SURGE -> 1500 - seals * 200;
            case AIM -> 1800 - seals * 230;
            case STRIKE -> 840 - seals * 95;
        };
    }

    /**
     * Prepara un’ondata sulla posizione corrente e, dopo il primo sigillo, su una seconda
     * corsia.
     */
    private void startSurge() {
        attack = Attack.SURGE;
        danger = EnumSet.of(position);
        if (seals > 0) {
            int offset = random.nextBoolean() ? 1 : 2;
            danger.add(Lane.values()[(position.ordinal() + offset) % 3]);
        }
    }

    /**
     * Risolve la fase: la mira fissa il bersaglio, l’ondata controlla la schivata e la lama
     * rompe il sigillo solo se Arthur si è spostato.
     */
    public Result impact() {
        if (attack == Attack.AIM) {
            danger = EnumSet.of(position);
            attack = Attack.STRIKE;
            return Result.LOCKED;
        }
        boolean hit = danger.contains(position);
        if (attack == Attack.SURGE) {
            if (++waves >= seals + 1) {
                attack = Attack.AIM;
                danger.clear();
            } else {
                startSurge();
            }
            return hit ? Result.HIT : Result.DODGED;
        }
        // Il sigillo si rompe attirando la lama su di esso e lasciando libera la corsia.
        boolean broken = !hit && danger.contains(seal());
        if (broken && ++seals == 3) return Result.VICTORY;
        waves = 0;
        startSurge();
        if (hit) return Result.HIT;
        return broken ? Result.BROKEN : Result.MISS;
    }
}
