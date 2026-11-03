package com.alchemist.hangover.gameplay.chapter;

import com.alchemist.hangover.model.GameSaveSnapshot;
import com.alchemist.hangover.model.GameState;
import java.util.Objects;

/**
 * Regole di progressione dei capitoli, senza finestre Swing o accessi H2.
 * SALVA prepara solo una fotografia; CONTINUA avanza la partita attiva.
 */
public final class ChapterProgression {
    private ChapterProgression() { }

    /**
     * Prepara lo stato da salvare all’inizio del capitolo successivo senza spostare la partita
     * attiva. All’ingresso nel capitolo 3 assegna un seme persistente al rituale.
     */
    public static GameSaveSnapshot saveAfterCompletion(GameState state, int completedChapter) {
        GameSaveSnapshot s = GameSaveSnapshot.capture(Objects.requireNonNull(state, "state"));
        return switch (completedChapter) {
            case 1 -> new GameSaveSnapshot(
                    2, 1, s.maxHealth(), s.maxHealth(),
                    s.givenMapToMartino(), s.dragonKey(), s.dragonUnlocked(),
                    false, s.ymirPacified(), s.ymirRuneSeed(),
                    s.gunnarWisdomLearned(), s.gunnarIntroductionHeard(),
                    s.ymirSteleRead(), s.ymirOldFashionedCreated(),
                    s.ragnarokFlambeCreated(), s.inventory());
            case 2 -> new GameSaveSnapshot(
                    3, 1, s.maxHealth(), s.maxHealth(),
                    s.givenMapToMartino(), s.dragonKey(), s.dragonUnlocked(),
                    s.bossDefeated(), s.ymirPacified(), s.ymirRuneSeed(),
                    s.gunnarWisdomLearned(), s.gunnarIntroductionHeard(),
                    s.ymirSteleRead(), s.ymirOldFashionedCreated(),
                    s.ragnarokFlambeCreated(), s.inventory()).withPersistentRuneSeed();
            case 3 -> new GameSaveSnapshot(4, 1, s.maxHealth(), s.maxHealth(),
                    s.givenMapToMartino(), s.dragonKey(), s.dragonUnlocked(),
                    false, s.ymirPacified(), s.ymirRuneSeed(), s.gunnarWisdomLearned(),
                    s.gunnarIntroductionHeard(), s.ymirSteleRead(),
                    s.ymirOldFashionedCreated(), false, s.inventory());
            case 4 -> s;
            default -> throw new IllegalArgumentException("Capitolo non previsto: " + completedChapter);
        };
    }

    /**
     * Avanza realmente la partita: imposta capitolo e stanza iniziale e ripristina salute e
     * condizioni di ripresa previste.
     */
    public static void advanceToNextChapter(GameState state, int completedChapter) {
        Objects.requireNonNull(state, "state");
        switch (completedChapter) {
            case 1 -> {
                state.setChapter(2);
                state.setCurrentRoom(1);
                state.setBossDefeated(false);
                state.setBossRetryAvailable(false);
                state.resetHealth();
            }
            case 2 -> {
                state.setChapter(3);
                state.setCurrentRoom(1);
                state.setBossRetryAvailable(false);
                state.ensureYmirRuneSeed();
                state.resetHealth();
            }
            case 3 -> {
                state.setChapter(4);
                state.setCurrentRoom(1);
                state.setBossDefeated(false);
                state.setBossRetryAvailable(false);
                state.resetHealth();
            }
            case 4 -> { /* Ultimo capitolo terminato. */ }
            default -> throw new IllegalArgumentException("Capitolo non previsto: " + completedChapter);
        }
    }
}
