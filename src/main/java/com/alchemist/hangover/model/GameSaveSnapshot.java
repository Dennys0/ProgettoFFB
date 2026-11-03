package com.alchemist.hangover.model;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/** Fotografia immutabile dei dati persistiti in H2, indipendente dall'interfaccia. */
public record GameSaveSnapshot(
        int chapter, int room, int health, int maxHealth,
        boolean givenMapToMartino, boolean dragonKey, boolean dragonUnlocked,
        boolean bossDefeated, boolean ymirPacified, int ymirRuneSeed,
        boolean gunnarWisdomLearned, boolean gunnarIntroductionHeard,
        boolean ymirSteleRead, boolean ymirOldFashionedCreated,
        boolean ragnarokFlambeCreated, List<Item> inventory) {

    public GameSaveSnapshot {
        inventory = List.copyOf(Objects.requireNonNull(inventory, "inventory"));
    }

    /** Cattura lo stato senza modificarlo, neppure quando manca il seed di Ymir. */
    public static GameSaveSnapshot capture(GameState state) {
        Objects.requireNonNull(state, "state");
        return new GameSaveSnapshot(
                state.getChapter(), state.getCurrentRoom(),
                state.getCurrentHealth(), state.getMaxHealth(),
                state.hasGivenMapToMartino(), state.hasDragonKey(),
                state.isDragonUnlocked(), state.isBossDefeated(),
                state.isYmirPacified(), state.getYmirRuneSeed(),
                state.hasLearnedGunnarWisdom(), state.hasHeardGunnarIntroduction(),
                state.hasReadYmirStele(), state.isYmirOldFashionedCreated(),
                state.isRagnarokFlambeCreated(), state.getInventory());
    }

    /** Non scrivere in H2 un capitolo 3 privo di seed, senza mutare GameState. */
    public GameSaveSnapshot withPersistentRuneSeed() {
        if (chapter != 3 || ymirRuneSeed != 0) return this;
        return new GameSaveSnapshot(chapter, room, health, maxHealth,
                givenMapToMartino, dragonKey, dragonUnlocked, bossDefeated,
                ymirPacified, nextRuneSeed(), gunnarWisdomLearned,
                gunnarIntroductionHeard, ymirSteleRead, ymirOldFashionedCreated,
                ragnarokFlambeCreated, inventory);
    }

    /**
     * Genera un seme positivo da conservare nella fotografia della partita.
     */
    private static int nextRuneSeed() {
        return ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
    }
}
