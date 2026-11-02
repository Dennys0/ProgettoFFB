package com.alchemist.hangover.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Only one canonical recipe per chapter. UI and penalties are kept separate. */
public final class RecipeManager {
    public enum Result { INCOMPLETE, SUCCESS, WRONG_INGREDIENT }

    public record Recipe(int chapter, int cauldronRoom, String drinkName,
                         Set<String> correct, Set<String> available) {
        public Recipe {
            correct = Set.copyOf(correct);
            available = Set.copyOf(available);
            if (correct.size() != 3 || !available.containsAll(correct))
                throw new IllegalArgumentException("A recipe needs three valid ingredients");
        }
    }

    private static final Map<Integer, Recipe> RECIPES = Map.of(
            1, new Recipe(1, 4, "Asgardian Aviation",
                    Set.of("SOFFIO_HRAESVELGR", "PIUMA_FOLGORE", "RUGIADA_ASGARD"),
                    Set.of("SOFFIO_HRAESVELGR", "PIUMA_FOLGORE", "RUGIADA_ASGARD",
                            "NEBBIA_NIFLHEIM", "POLLINE_YGGDRASIL")),
            2, new Recipe(2, 3, "Maelstrom Martini",
                    Set.of("SCHIUMA_AEGIR", "CORALLO_RAN", "SALE_JORMUNGANDR"),
                    Set.of("SCHIUMA_AEGIR", "CORALLO_RAN", "SALE_JORMUNGANDR",
                            "ACQUA_LETE", "INCHIOSTRO_KRAKEN")),
            3, new Recipe(3, 3, "Ymir's Old Fashioned",
                    Set.of("MUSCHIO_MIDGARD", "FRAMMENTO_YGGDRASIL", "LACRIMA_YMIR"),
                    Set.of("MUSCHIO_MIDGARD", "FRAMMENTO_YGGDRASIL", "LACRIMA_YMIR",
                            "RADICE_NIDHOGG", "POLVERE_OSSA_GIGANTE")),
            4, new Recipe(4, 3, "Ragnarök Flambé",
                    Set.of("BRACE_MUSPELHEIM", "CENERE_FENRIR", "MAGMA_SVARTALFAHEIMR"),
                    Set.of("BRACE_MUSPELHEIM", "CENERE_FENRIR", "MAGMA_SVARTALFAHEIMR",
                            "ZOLFO_LOKI", "LACRIMA_HEL"))
    );

    private RecipeManager() { }

    public static Recipe forChapter(int chapter) {
        Recipe recipe = RECIPES.get(chapter);
        if (recipe == null) throw new IllegalArgumentException(
                "Il capitolo " + chapter + " non ha ancora una ricetta");
        return recipe;
    }

    /** Decoys count toward the THREE available ingredients, but not toward success. */
    public static boolean canOpen(int chapter, Collection<Item> inventory) {
        if (inventory == null) return false;
        Set<String> available = forChapter(chapter).available();
        return inventory.stream().filter(Objects::nonNull).map(Item::getId)
                .filter(available::contains).distinct().limit(3).count() >= 3;
    }

    public static List<Item> usableItems(int chapter, Collection<Item> inventory) {
        Set<String> available = forChapter(chapter).available();
        if (inventory == null) return List.of();
        return inventory.stream().filter(Objects::nonNull)
                .filter(item -> available.contains(item.getId())).toList();
    }

    public static Result evaluate(int chapter, Collection<String> ids) {
        if (ids == null || ids.size() != 3 || ids.stream().anyMatch(Objects::isNull)
                || ids.stream().distinct().count() != 3) return Result.INCOMPLETE;
        if (!Set.copyOf(ids).equals(forChapter(chapter).correct()))
            return Result.WRONG_INGREDIENT;
        // Nel capitolo IV la Brace deve essere aggiunta per ultima.
        if (chapter == 4 && !"BRACE_MUSPELHEIM".equals(
                ids.stream().toList().get(2))) return Result.WRONG_INGREDIENT;
        return Result.SUCCESS;
    }

    /** Gives the player a meaningful failure message without destroying items. */
    public static String failureText(int chapter, Collection<String> selection,
                                     Collection<Item> inventory) {
        Set<String> required = forChapter(chapter).correct();
        if (chapter == 4 && selection != null && selection.size() == 3
                && Set.copyOf(selection).equals(required)
                && !"BRACE_MUSPELHEIM".equals(selection.stream().toList().get(2)))
            return "La Brace deve essere aggiunta per ultima: la miscela divampa!";
        if (chapter == 4 && selection != null) {
            if (selection.contains("LACRIMA_HEL"))
                return "Il ghiaccio di Hel causa uno shock termico: il calderone esplode!";
            if (selection.contains("ZOLFO_LOKI"))
                return "Lo zolfo di Loki evapora in una nube pungente: miscela fallita!";
        }
        String wrongId = selection.stream().filter(id -> !required.contains(id))
                .findFirst().orElse(null);
        String name = wrongId;
        if (wrongId != null && inventory != null)
            for (Item item : inventory)
                if (item.getId().equals(wrongId)) { name = item.getName(); break; }
        return wrongId == null
                ? "Gli ingredienti non formano la ricetta del capitolo. Il calderone trabocca."
                : "Ingrediente errato: " + name + ". La miscela si destabilizza.";
    }
}
