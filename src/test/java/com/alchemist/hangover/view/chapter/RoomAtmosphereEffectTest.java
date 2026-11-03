package com.alchemist.hangover.view.chapter;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica stili ambientali, disegno senza finestra e passaggio dei clic attraverso le
 * particelle.
 */
class RoomAtmosphereEffectTest {
  @Test void distinctiveEffectsAcrossThreeChapters() {
    assertEquals(RoomAtmosphereEffect.Style.SKY, RoomAtmosphereEffect.styleFor("/images/chapter1/room1_deck.png"));
    assertEquals(RoomAtmosphereEffect.Style.EMBERS, RoomAtmosphereEffect.styleFor("/images/chapter1/room4_lab.png"));
    assertEquals(RoomAtmosphereEffect.Style.SEA, RoomAtmosphereEffect.styleFor("/images/chapter2/room4_gardens.png"));
    assertEquals(RoomAtmosphereEffect.Style.FIREFLIES, RoomAtmosphereEffect.styleFor("/images/chapter3/room2_rune.png"));
    assertEquals(RoomAtmosphereEffect.Style.EMBERS, RoomAtmosphereEffect.styleFor("/images/chapter3/room3_forge.png"));
    assertEquals(RoomAtmosphereEffect.Style.FIREFLIES, RoomAtmosphereEffect.styleFor("/images/chapter3/room4_forest.png"));
    assertEquals(RoomAtmosphereEffect.Style.EARTH, RoomAtmosphereEffect.styleFor("/images/chapter3/room5_miners.png"));
  }
  @Test void effectNeverBlocksClicksAndPaintsWithoutWindow() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      RoomAtmosphereEffect effect = new RoomAtmosphereEffect();
      effect.setSize(1280, 530);
      assertFalse(effect.contains(100, 100));
      BufferedImage image = new BufferedImage(1280, 530, BufferedImage.TYPE_INT_ARGB);
      Graphics graphics = image.getGraphics();
      try {
        for (int room=1; room<=7; room++) {
          effect.configure(ChapterLayouts.forChapter(3).backgrounds().get(room));
          assertDoesNotThrow(() -> effect.paint(graphics));
        }
      } finally { graphics.dispose(); }
    });
  }
}
