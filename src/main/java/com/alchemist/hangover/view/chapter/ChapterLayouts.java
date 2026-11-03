package com.alchemist.hangover.view.chapter;

import java.util.Map;

/**
 * Dati dei mondi (PNG e frecce), separati dai pannelli Swing.
 * Un solo formato di configurazione per tutti i capitoli.
 */
public final class ChapterLayouts {
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
                           7, Map.of("UP", 5))),
            4, new Layout(
                    Map.of(1, "/images/chapter4/room_1.png",
                           2, "/images/chapter4/room_2.png",
                           3, "/images/chapter4/room_3.png",
                           4, "/images/chapter4/room_4.png",
                           5, "/images/chapter4/room_5.png",
                           6, "/images/chapter4/room_6.png",
                           7, "/images/chapter4/room_boss.png"),
                    Map.of(1, Map.of("UP", 2, "LEFT", 3, "RIGHT", 4, "DOWN", 5),
                           2, Map.of("DOWN", 1, "UP", 6),
                           3, Map.of("RIGHT", 1),
                           4, Map.of("LEFT", 1),
                           5, Map.of("UP", 1, "DOWN", 7),
                           6, Map.of("DOWN", 2),
                           7, Map.of("UP", 5)))
    );

    /** Nomi delle stanze usati nei menu di sviluppo, separati dai percorsi. */
    private static final Map<Integer, Map<Integer, String>> ROOM_NAMES = Map.of(
            1, Map.of(1, "Ponte di coperta", 2, "Timone", 3, "Prua", 4, "Cabina dell’alchimista",
                    5, "Stiva", 6, "Coffa", 7, "Il fronte della tempesta"),
            2, Map.of(1, "Sala sommersa", 2, "Taverna abissale", 3, "Sacrestia corallina",
                    4, "Giardini sommersi", 5, "Fossa degli abissi", 6, "Balconata dei relitti", 7, "Santuario di Rán"),
            3, Map.of(
                    1, "La Radura delle Radici",
                    2, "Il Cerchio delle Rune",
                    3, "La Forgia di Caverna",
                    4, "La Foresta Pietrificata",
                    5, "Le Miniere Basaltiche",
                    6, "La Rupe dell'Eco",
                    7, "Il Cuore di Roccia"),
            4, Map.of(
                    1, "Il Crogiolo di Magma",
                    2, "L'Avamposto della Valchiria",
                    3, "La Fossa di Vulcano",
                    4, "La Cenere dei Mondi",
                    5, "I Fiumi di Svartálfaheimr",
                    6, "Il Poggio dei Venti Focosi",
                    7, "La Fornace di Surtur")
    );

    private ChapterLayouts() { }

    public static String roomName(int chapter, int room) {
        return ROOM_NAMES.getOrDefault(chapter, Map.of())
                .getOrDefault(room, "Stanza " + room);
    }

    static Layout forChapter(int chapter) {
        Layout layout = CHAPTERS.get(chapter);
        if (layout == null) {
            throw new IllegalArgumentException("Capitolo non configurato: " + chapter);
        }
        return layout;
    }
}
