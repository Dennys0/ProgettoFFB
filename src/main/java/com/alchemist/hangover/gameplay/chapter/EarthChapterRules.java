package com.alchemist.hangover.gameplay.chapter;

import com.alchemist.hangover.model.GameState;

/** Regole narrative del Capitolo III: nessuna dipendenza Swing. */
public final class EarthChapterRules {
    private EarthChapterRules() { }

    /** Sceglie la fase del dialogo dai progressi, indipendentemente dalla grafica. */
    public static String gunnarDialoguePath(GameState state) {
        if (state.hasLearnedGunnarWisdom()) return "/dialogues/ch3_gunnar_repeat.txt";
        return state.hasHeardGunnarIntroduction()
                ? "/dialogues/ch3_gunnar_reward.txt" : "/dialogues/ch3_gunnar.txt";
    }

    /** Da chiamare alla fine del dialogo, mai su ESC o dopo un cambio stanza. */
    public static boolean completeGunnarStage(GameState state, boolean rewardConversation) {
        if (state.hasLearnedGunnarWisdom()) return false;
        if (!state.hasHeardGunnarIntroduction()) {
            state.setGunnarIntroductionHeard(true);
            return false;
        }
        if (rewardConversation) {
            state.setGunnarWisdomLearned(true);
            return true;
        }
        return false;
    }

    /** Indica esattamente gli indizi mancanti, senza testi incorporati nei PNG. */
    public static String missingYmirClues(boolean hasGunnarWisdom, boolean hasStele) {
        StringBuilder message = new StringBuilder(
                "Il gigante si muove sotto la pietra. Per placarlo devi ancora:");
        if (!hasGunnarWisdom) message.append(
                "\n• Comprendere la saggezza di Gunnar nel Cerchio delle Rune.");
        if (!hasStele) message.append(
                "\n• Leggere la stele della Rupe dell'Eco.");
        return message.toString();
    }

}
