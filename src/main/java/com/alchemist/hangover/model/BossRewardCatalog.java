package com.alchemist.hangover.model;

import java.util.Optional;

/** Ricompense immediate dei boss; Ymir lascia invece la Lacrima nella stanza. */
public final class BossRewardCatalog {
    private BossRewardCatalog() { }

    public static Optional<Item> immediateReward(int chapter) {
        return switch (chapter) {
            case 1 -> Optional.of(new Item(
                    "SOFFIO_HRAESVELGR", "Soffio di Hræsvelgr",
                    "L'ampolla contenente il vento celestiale puro donato dall'aquila gigante.",
                    "/images/items/item_breath.png"));
            case 2 -> Optional.of(new Item(
                    "SCHIUMA_AEGIR", "Schiuma di Ægir",
                    "La birra sottomarina lucente estratta dalle reti della dea Rán.",
                    "/images/items/item_foam.png"));
            case 3 -> Optional.empty();
            default -> throw new IllegalArgumentException("Capitolo senza boss: " + chapter);
        };
    }
}
