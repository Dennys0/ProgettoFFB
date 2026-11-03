package com.alchemist.hangover.gameplay.cauldron;

import com.alchemist.hangover.model.Item;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecipeManagerTest {
    private static Item item(String id) { return new Item(id, id, "Item", "/images/chapter1/items/map.png"); }

    @Test void validatesOnlyExactThreeIngredientsForEachChapter() {
        for (int chapter=1; chapter<=4; chapter++) {
            RecipeManager.Recipe recipe=RecipeManager.forChapter(chapter);
            assertEquals(3,recipe.correct().size());
            assertEquals(RecipeManager.Result.SUCCESS,
                    RecipeManager.evaluate(chapter, chapter == 4
                            ? List.of("MAGMA_SVARTALFAHEIMR", "CENERE_FENRIR", "BRACE_MUSPELHEIM")
                            : recipe.correct()));
            assertEquals(RecipeManager.Result.INCOMPLETE,
                    RecipeManager.evaluate(chapter,List.of(recipe.correct().iterator().next())));
            assertEquals(RecipeManager.Result.INCOMPLETE,
                    RecipeManager.evaluate(chapter,List.of("A","A","B")));
            assertEquals(RecipeManager.Result.WRONG_INGREDIENT,
                    RecipeManager.evaluate(chapter,List.of("WRONG","OTHER","THIRD")));
        }
        assertThrows(IllegalArgumentException.class,()->RecipeManager.forChapter(5));
    }

    @Test void inventoryGateCountsOnlyCurrentChapterIngredientsAndRetainsDecoys() {
        for(int chapter=1;chapter<=4;chapter++){
            RecipeManager.Recipe recipe=RecipeManager.forChapter(chapter);
            List<Item> inventory=new ArrayList<>();
            inventory.add(item("UNKNOWN"));
            assertFalse(RecipeManager.canOpen(chapter,inventory));
            List<String> all=new ArrayList<>(recipe.available());
            inventory.add(item(all.get(0)));
            inventory.add(item(all.get(1)));
            assertFalse(RecipeManager.canOpen(chapter,inventory));
            inventory.add(item(all.get(2)));
            assertTrue(RecipeManager.canOpen(chapter,inventory));
            assertEquals(3,RecipeManager.usableItems(chapter,inventory).size());
            assertFalse(RecipeManager.canOpen(chapter,
                    List.of(item(all.get(0)),item(all.get(0)),item(all.get(1)))));
        }
        assertFalse(RecipeManager.canOpen(1, null));
        assertEquals(4,RecipeManager.forChapter(1).cauldronRoom());
        assertEquals(3,RecipeManager.forChapter(2).cauldronRoom());
        assertEquals(3,RecipeManager.forChapter(3).cauldronRoom());
    }

    @Test void wrongIngredientProducesMeaningfulTextWithoutMutatingInventory() {
        var recipe=RecipeManager.forChapter(3);
        List<String> mixed=new ArrayList<>(recipe.correct());
        mixed.set(0,"RADICE_NIDHOGG");
        List<Item> inventory=mixed.stream().map(RecipeManagerTest::item).toList();
        assertEquals(RecipeManager.Result.WRONG_INGREDIENT,
                RecipeManager.evaluate(3,mixed));
        String reason=RecipeManager.failureText(3,mixed,inventory);
        assertTrue(reason.contains("RADICE_NIDHOGG"));
        assertEquals(3,inventory.size());
    }
}
