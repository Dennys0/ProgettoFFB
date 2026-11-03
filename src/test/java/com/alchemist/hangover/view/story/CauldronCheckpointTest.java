package com.alchemist.hangover.view.story;

import com.alchemist.hangover.gameplay.cauldron.RecipeManager;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;
import java.util.*;
import javax.swing.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica che riprovare la ricetta conservi ingredienti, ripristini salute e torni al
 * calderone.
 */
class CauldronCheckpointTest {
    @AfterEach void cleanup() { GameState.getInstance().resetState(); }

    @Test void retryKeepsIngredientsRestoresAllHeartsAndReturnsToCauldronRoom() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            GameState state=GameState.getInstance();
            try {
                for(int chapter=1;chapter<=3;chapter++) {
                    state.resetState();
                    state.setChapter(chapter);
                    state.setCurrentRoom(7);
                    for(String id:RecipeManager.forChapter(chapter).correct())
                        state.addItem(new Item(id,id,"Desc","/images/chapter1/items/map.png"));
                    Set<String> snapshot=new HashSet<>();
                    state.getInventory().forEach(item->snapshot.add(item.getId()));
                    GameOverPanel panel=new GameOverPanel();
                    panel.showCauldronFailure("Ingrediente sbagliato");
                    assertTrue(panel.isCauldronFailure());
                    state.setCurrentHealth(0);
                    JButton retry=Arrays.stream(panel.getComponents())
                            .filter(JButton.class::isInstance).map(JButton.class::cast)
                            .filter(btn->btn.getText().contains("RIPROVA")).findFirst().orElseThrow();
                    retry.doClick();
                    assertEquals(3,state.getCurrentHealth());
                    assertEquals(RecipeManager.forChapter(chapter).cauldronRoom(),state.getCurrentRoom());
                    Set<String> after=new HashSet<>();
                    state.getInventory().forEach(item->after.add(item.getId()));
                    assertEquals(snapshot,after,"Il checkpoint non consuma ingredienti");
                }
            } finally { state.resetState(); }
        });
    }
}
