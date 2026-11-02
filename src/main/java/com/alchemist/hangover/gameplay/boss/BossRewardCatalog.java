package com.alchemist.hangover.gameplay.boss;

import com.alchemist.hangover.model.Item;

import java.util.Optional;

/** Ricompense immediate dei boss dei quattro capitoli. */
public final class BossRewardCatalog {
    private BossRewardCatalog() { }

    public static Optional<Item> immediateReward(int chapter) {
        return switch (chapter) {
            case 1 -> Optional.of(new Item(
                    "SOFFIO_HRAESVELGR", "Soffio di Hræsvelgr",
                    "L'ampolla contenente il vento celestiale puro donato dall'aquila gigante.",
                    "/images/chapter1/items/breath.png"));
            case 2 -> Optional.of(new Item(
                    "SCHIUMA_AEGIR", "Schiuma di Ægir",
                    "La birra sottomarina lucente estratta dalle reti della dea Rán.",
                    "/images/chapter2/items/foam.png"));
            case 3 -> Optional.of(new Item("LACRIMA_YMIR", "Lacrima di Ymir",
                    "Una lacrima cristallina donata dal gigante nel sonno.", "/images/chapter3/items/ymircry.png"));
            case 4 -> Optional.of(new Item(
                    "BRACE_MUSPELHEIM", "Brace di Muspelheim",
                    "Il cuore di fuoco inestinguibile liberato dalla spada di Surtur.",
                    "/images/chapter4/items/ember.png"));
            default -> throw new IllegalArgumentException("Capitolo senza boss: " + chapter);
        };
    }
}
