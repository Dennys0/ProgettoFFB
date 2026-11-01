package com.alchemist.hangover.gameplay.chapter;

import com.alchemist.hangover.model.GameState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChapterProgressionTest {
    @AfterEach void clear() { GameState.getInstance().resetState(); }

    @Test void preparingSaveNeverAdvancesCurrentPlaythrough() {
        GameState s = GameState.getInstance();
        s.resetState();
        s.setChapter(1);
        s.setCurrentRoom(4);
        s.setCurrentHealth(1);
        s.setBossDefeated(true);
        var next = ChapterProgression.saveAfterCompletion(s, 1);
        assertEquals(2, next.chapter());
        assertEquals(1, next.room());
        assertEquals(3, next.health());
        assertFalse(next.bossDefeated());
        assertEquals(1, s.getChapter());
        assertEquals(4, s.getCurrentRoom());
        assertEquals(1, s.getCurrentHealth());
        assertTrue(s.isBossDefeated());
    }

    @Test void nextChapterThreeHasStableRuneSeedOnlyWhenContinuing() {
        GameState s = GameState.getInstance();
        s.resetState();
        s.setChapter(2);
        var snapshot = ChapterProgression.saveAfterCompletion(s, 2);
        assertEquals(3, snapshot.chapter());
        assertTrue(snapshot.ymirRuneSeed() > 0);
        assertEquals(0, s.getYmirRuneSeed());
        ChapterProgression.advanceToNextChapter(s, 2);
        assertEquals(3, s.getChapter());
        assertTrue(s.getYmirRuneSeed() > 0);
        assertEquals(s.getYmirRuneSeed(), s.ensureYmirRuneSeed());
        assertThrows(IllegalArgumentException.class,
                () -> ChapterProgression.advanceToNextChapter(s, 7));
    }
}
