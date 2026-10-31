package com.alchemist.hangover.view;

import java.util.Map;

/**
 * Configuration of Ymir's Domain. Kept separate from the panel so that
 * navigation can be verified without creating a Swing window.
 */
public final class Chapter3World {
    private static final Map<Integer, String> BACKGROUNDS = Map.of(
            1, "/images/chapter3/room1_hub.png",
            2, "/images/chapter3/room2_rune.png",
            3, "/images/chapter3/room3_forge.png",
            4, "/images/chapter3/room4_forest.png",
            5, "/images/chapter3/room5_miners.png",
            6, "/images/chapter3/room6_eco.png",
            7, "/images/chapter3/room7_boss.png"
    );

    // In the existing chapter engine UP/DOWN/LEFT/RIGHT identify UI arrows.
    // "Avanti" from the basalt mines is DOWN, to keep the route reversible.
    private static final Map<Integer, Map<String, Integer>> EXITS = Map.of(
            1, Map.of("UP", 2, "LEFT", 3, "RIGHT", 4, "DOWN", 5),
            2, Map.of("DOWN", 1, "UP", 6),
            3, Map.of("RIGHT", 1),
            4, Map.of("LEFT", 1),
            5, Map.of("UP", 1, "DOWN", 7),
            6, Map.of("DOWN", 2),
            7, Map.of("UP", 5)
    );

    private static final Map<Integer, String> NAMES = Map.of(
            1, "La Radura delle Radici",
            2, "Il Cerchio delle Rune",
            3, "La Forgia di Caverna",
            4, "La Foresta Pietrificata",
            5, "Le Miniere Basaltiche",
            6, "La Rupe dell'Eco",
            7, "Il Cuore di Roccia"
    );

    private Chapter3World() { }

    public static Map<Integer, String> backgrounds() {
        return BACKGROUNDS;
    }

    public static Map<Integer, Map<String, Integer>> exits() {
        return EXITS;
    }

    public static String roomName(int id) {
        return NAMES.getOrDefault(id, "Stanza sconosciuta");
    }
}
