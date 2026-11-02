package com.alchemist.hangover.gameplay.boss;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Motore indipendente da Swing: Arthur sceglie una corsia, il timer della
 * view decide quando il fendente impatta. Lo schermo non conosce la sequenza
 * degli attacchi, ma ne mostra le zone di pericolo prima della collisione.
 */
public final class SurturDuel {
    public enum Lane { LEFT, CENTER, RIGHT }
    public enum Attack { SINGLE, DOUBLE, SWEEP, FINAL }
    public enum Result { DODGED, HIT, VICTORY }

    private final Random random;
    private Lane position = Lane.CENTER;
    private EnumSet<Lane> danger = EnumSet.noneOf(Lane.class);
    private int completedAttacks;
    private int sweepWave;
    private boolean reverseSweep;
    private boolean lurePrepared;

    public SurturDuel(Random random) {
        this.random = Objects.requireNonNull(random, "random");
        prepareAttack();
    }

    /** Gli attacchi richiedono movimenti attivi, non una risposta a scelta. */
    public void move(Lane lane) {
        position = Objects.requireNonNull(lane, "lane");
    }

    public Lane position() { return position; }
    public int completedAttacks() { return completedAttacks; }
    public int sweepWave() { return sweepWave; }
    public boolean finalTrap() { return completedAttacks >= 6; }
    public boolean lured() { return lurePrepared; }
    public Set<Lane> danger() { return Set.copyOf(danger); }

    public Attack attack() {
        if (finalTrap()) return Attack.FINAL;
        if (completedAttacks < 2) return Attack.SINGLE;
        if (completedAttacks < 4) return Attack.DOUBLE;
        return Attack.SWEEP;
    }

    /** Solo dalla corsia centrale si puo' provocare il gigante con E. */
    public boolean taunt() {
        if (!finalTrap() || position != Lane.CENTER || lurePrepared) return false;
        lurePrepared = true;
        danger = EnumSet.of(Lane.CENTER);
        return true;
    }

    /** Chiamato dal timer allo scadere del preavviso, non dal clic del giocatore. */
    public Result impact() {
        if (finalTrap()) {
            if (!lurePrepared || position == Lane.CENTER) {
                lurePrepared = false;  // Permette di riprovare la trappola.
                danger = EnumSet.noneOf(Lane.class);
                return Result.HIT;
            }
            return Result.VICTORY;
        }

        boolean hit = danger.contains(position);
        if (attack() == Attack.SWEEP && sweepWave == 0) {
            // La spazzata richiede due schivate consecutive in direzioni opposte.
            sweepWave = 1;
            setSweepDanger();
        } else {
            completedAttacks++;
            sweepWave = 0;
            prepareAttack();
        }
        return hit ? Result.HIT : Result.DODGED;
    }

    private void prepareAttack() {
        danger = EnumSet.noneOf(Lane.class);
        Lane[] lanes = Lane.values();
        switch (attack()) {
            case SINGLE -> danger.add(lanes[random.nextInt(lanes.length)]);
            case DOUBLE -> {
                Lane safe = lanes[random.nextInt(lanes.length)];
                for (Lane lane : lanes) if (lane != safe) danger.add(lane);
            }
            case SWEEP -> {
                reverseSweep = random.nextBoolean();
                setSweepDanger();
            }
            case FINAL -> { /* Si attende la provocazione con E. */ }
        }
    }

    private void setSweepDanger() {
        danger = reverseSweep == (sweepWave == 0)
                ? EnumSet.of(Lane.CENTER, Lane.RIGHT)
                : EnumSet.of(Lane.LEFT, Lane.CENTER);
    }
}
