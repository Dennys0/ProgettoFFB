package com.alchemist.hangover.gameplay.boss;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

/** Surtur segue Arthur durante la mira. Una volta piantato il piede, la lama
 * cade sul punto fissato anche se Arthur si è già spostato. */
public final class SurturDuel {
    public enum Lane { LEFT, CENTER, RIGHT }
    public enum Attack { SURGE, AIM, STRIKE }
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

    public Lane seal() {
        if (seals == 0) return firstSeal;
        if (seals == 1) return firstSeal == Lane.LEFT ? Lane.RIGHT : Lane.LEFT;
        return Lane.CENTER;
    }

    public int durationMs() {
        return switch (attack) {
            case SURGE -> 1500 - seals * 200;
            case AIM -> 1800 - seals * 230;
            case STRIKE -> 840 - seals * 95;
        };
    }

    private void startSurge() {
        attack = Attack.SURGE;
        danger = EnumSet.of(position);
        if (seals > 0) {
            int offset = random.nextBoolean() ? 1 : 2;
            danger.add(Lane.values()[(position.ordinal() + offset) % 3]);
        }
    }

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
        boolean broken = !hit && danger.contains(seal());
        if (broken && ++seals == 3) return Result.VICTORY;
        waves = 0;
        startSurge();
        if (hit) return Result.HIT;
        return broken ? Result.BROKEN : Result.MISS;
    }
}
