package com.alchemist.hangover.view;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;
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
          effect.configure(Chapter3World.backgrounds().get(room));
          assertDoesNotThrow(() -> effect.paint(graphics));
        }
      } finally { graphics.dispose(); }
    });
  }
}
