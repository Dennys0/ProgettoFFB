package com.alchemist.hangover.gameplay.boss;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/** Minigioco finale: prevedi i fendenti, poi attira la spada sulla fornace. */
public final class SurturDuel {
    public enum Lane { LEFT, CENTER, RIGHT }
    public enum Result { DODGED, HIT, LURED, VICTORY }
    private final Random random;
    private int round;
    private Set<Lane> danger;
    private boolean lured;

    public SurturDuel(Random random) {
        this.random = Objects.requireNonNull(random);
        reroll();
    }
    public int round() { return round; }
    public boolean finalTrap() { return round >= 4; }
    public boolean lured() { return lured; }
    public Set<Lane> danger() { return Set.copyOf(danger); }

    public Result choose(Lane lane) {
        Objects.requireNonNull(lane);
        if (finalTrap()) {
            if (!lured) {
                if (lane != Lane.CENTER) return Result.HIT;
                lured = true;
                return Result.LURED;
            }
            if (lane == Lane.CENTER) return Result.HIT;
            return Result.VICTORY;
        }
        if (danger.contains(lane)) {
            reroll();
            return Result.HIT;
        }
        round++;
        if (!finalTrap()) reroll();
        return Result.DODGED;
    }

    private void reroll() {
        Lane[] lanes = Lane.values();
        danger = EnumSet.noneOf(Lane.class);
        if (round < 2) {
            danger.add(lanes[random.nextInt(lanes.length)]);
        } else {
            // Nella seconda fase due corsie sono pericolose, una e' sicura.
            Lane safe = lanes[random.nextInt(lanes.length)];
            for (Lane lane : lanes) if (lane != safe) danger.add(lane);
        }
    }
}
