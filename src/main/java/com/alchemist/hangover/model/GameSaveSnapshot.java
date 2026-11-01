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
        List<Item> inventory) {

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
                state.getInventory());
    }

    /** La partita in memoria resta alla schermata di completamento fino a PROSEGUI. */
    public static GameSaveSnapshot afterCompletedChapter(GameState state, int completedChapter) {
        GameSaveSnapshot s = capture(state);
        return switch (completedChapter) {
            case 1 -> new GameSaveSnapshot(2, 1, s.maxHealth(), s.maxHealth(),
                    s.givenMapToMartino(), s.dragonKey(), s.dragonUnlocked(),
                    false, s.ymirPacified(), s.ymirRuneSeed(),
                    s.gunnarWisdomLearned(), s.gunnarIntroductionHeard(),
                    s.ymirSteleRead(), s.ymirOldFashionedCreated(), s.inventory());
            case 2 -> new GameSaveSnapshot(3, 1, s.maxHealth(), s.maxHealth(),
                    s.givenMapToMartino(), s.dragonKey(), s.dragonUnlocked(),
                    s.bossDefeated(), s.ymirPacified(),
                    s.ymirRuneSeed() == 0 ? nextRuneSeed() : s.ymirRuneSeed(),
                    s.gunnarWisdomLearned(), s.gunnarIntroductionHeard(),
                    s.ymirSteleRead(), s.ymirOldFashionedCreated(), s.inventory());
            case 3 -> s;
            default -> throw new IllegalArgumentException("Capitolo non previsto: " + completedChapter);
        };
    }

    /** Non scrivere in H2 un capitolo 3 privo di seed, senza mutare GameState. */
    public GameSaveSnapshot withPersistentRuneSeed() {
        if (chapter != 3 || ymirRuneSeed != 0) return this;
        return new GameSaveSnapshot(chapter, room, health, maxHealth,
                givenMapToMartino, dragonKey, dragonUnlocked, bossDefeated,
                ymirPacified, nextRuneSeed(), gunnarWisdomLearned,
                gunnarIntroductionHeard, ymirSteleRead, ymirOldFashionedCreated, inventory);
    }

    private static int nextRuneSeed() {
        return ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
    }
}
