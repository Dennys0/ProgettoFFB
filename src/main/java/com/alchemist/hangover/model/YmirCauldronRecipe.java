package com.alchemist.hangover.model;

import java.util.Set;

/** Valida soltanto i 5 ingredienti del mondo di Terra. */
public final class YmirCauldronRecipe {
    public enum Result { INCOMPLETE, NIDHOGG_ACID, GIANT_BONE_STONE, SUCCESS, WRONG_MIX }

    public static final Set<String> CORRECT = Set.of(
            "MUSCHIO_MIDGARD", "FRAMMENTO_YGGDRASIL", "LACRIMA_YMIR");
    public static final Set<String> ALL = Set.of(
            "MUSCHIO_MIDGARD", "FRAMMENTO_YGGDRASIL", "LACRIMA_YMIR",
            "RADICE_NIDHOGG", "POLVERE_OSSA_GIGANTE");

    private YmirCauldronRecipe() { }

    public static Result evaluate(Set<String> ids) {
        if (ids == null || ids.size() != 3) return Result.INCOMPLETE;
        if (ids.contains("RADICE_NIDHOGG")) return Result.NIDHOGG_ACID;
        if (ids.contains("POLVERE_OSSA_GIGANTE")) return Result.GIANT_BONE_STONE;
        if (ids.equals(CORRECT)) return Result.SUCCESS;
        return Result.WRONG_MIX;
    }
}
