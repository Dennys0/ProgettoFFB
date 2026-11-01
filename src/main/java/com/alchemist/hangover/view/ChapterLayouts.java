package com.alchemist.hangover.view;

import java.util.Map;

/**
 * Dati dei mondi (PNG e frecce), separati dai pannelli Swing.
 * Un solo formato di configurazione per tutti i capitoli.
 */
final class ChapterLayouts {
    record Layout(Map<Integer, String> backgrounds,
                  Map<Integer, Map<String, Integer>> exits) {
        Layout {
            backgrounds = Map.copyOf(backgrounds);
            exits = Map.copyOf(exits);
            if (!backgrounds.keySet().equals(exits.keySet())) {
                throw new IllegalArgumentException("Le stanze e le uscite non coincidono");
            }
            for (Map<String, Integer> routes : exits.values()) {
                for (Integer destination : routes.values()) {
                    if (!backgrounds.containsKey(destination)) {
                        throw new IllegalArgumentException("Uscita verso stanza inesistente: "
                                + destination);
                    }
                }
            }
        }
    }

    private static final Map<Integer, Layout> CHAPTERS = Map.of(
            1, new Layout(
                    Map.of(1, "/images/chapter1/room1_deck.png",
                           2, "/images/chapter1/room2_helm.png",
                           3, "/images/chapter1/room3_prow.png",
                           4, "/images/chapter1/room4_lab.png",
                           5, "/images/chapter1/room5_hold.png",
                           6, "/images/chapter1/room6_crowsnest.png",
                           7, "/images/chapter1/room7_boss.png"),
                    Map.of(1, Map.of("LEFT", 3, "RIGHT", 2, "DOWN", 5),
                           2, Map.of("LEFT", 1, "UP", 6),
                           3, Map.of("RIGHT", 1, "LEFT", 7),
                           4, Map.of("RIGHT", 5),
                           5, Map.of("UP", 1, "LEFT", 4),
                           6, Map.of("DOWN", 2),
                           7, Map.of("RIGHT", 3))),
            2, new Layout(
                    Map.of(1, "/images/chapter2/room1_hall.png",
                           2, "/images/chapter2/room2_bar.png",
                           3, "/images/chapter2/room3_sacristy.png",
                           4, "/images/chapter2/room4_gardens.png",
                           5, "/images/chapter2/room5_pit.png",
                           6, "/images/chapter2/room6_balcony.png",
                           7, "/images/chapter2/room7_boss.png"),
                    Map.of(1, Map.of("UP", 2, "LEFT", 3, "RIGHT", 4, "DOWN", 5),
                           2, Map.of("DOWN", 1, "UP", 6),
                           3, Map.of("RIGHT", 1),
                           4, Map.of("LEFT", 1),
                           5, Map.of("UP", 1, "DOWN", 7),
                           6, Map.of("DOWN", 2),
                           7, Map.of("UP", 5))),
            3, new Layout(
                    Map.of(1, "/images/chapter3/room1_hub.png",
                           2, "/images/chapter3/room2_rune.png",
                           3, "/images/chapter3/room3_forge.png",
                           4, "/images/chapter3/room4_forest.png",
                           5, "/images/chapter3/room5_miners.png",
                           6, "/images/chapter3/room6_eco.png",
                           7, "/images/chapter3/room7_boss.png"),
                    Map.of(1, Map.of("UP", 2, "LEFT", 3, "RIGHT", 4, "DOWN", 5),
                           2, Map.of("DOWN", 1, "UP", 6),
                           3, Map.of("RIGHT", 1),
                           4, Map.of("LEFT", 1),
                           5, Map.of("UP", 1, "DOWN", 7),
                           6, Map.of("DOWN", 2),
                           7, Map.of("UP", 5)))
    );

    private ChapterLayouts() { }

    static Layout forChapter(int chapter) {
        Layout layout = CHAPTERS.get(chapter);
        if (layout == null) {
            throw new IllegalArgumentException("Capitolo non configurato: " + chapter);
        }
        return layout;
    }
}
