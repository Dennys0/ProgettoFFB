package com.alchemist.hangover.controller;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AudioManagerRoomCueTest {
 @Test void dedicatedRoomSoundCuesAreCorrect() {
   assertEquals(AudioManager.Effect.WIND_GUST, AudioManager.cueForRoom("/images/chapter1/room1_deck.png"));
   assertEquals(AudioManager.Effect.FORGE_SPARK, AudioManager.cueForRoom("/images/chapter1/room4_lab.png"));
   assertEquals(AudioManager.Effect.WATER_DROP, AudioManager.cueForRoom("/images/chapter2/room4_gardens.png"));
   assertEquals(AudioManager.Effect.STONE_RUMBLE, AudioManager.cueForRoom("/images/chapter2/room5_pit.png"));
   assertEquals(AudioManager.Effect.RUNE_CHIME, AudioManager.cueForRoom("/images/chapter3/room2_rune.png"));
   assertEquals(AudioManager.Effect.FORGE_SPARK, AudioManager.cueForRoom("/images/chapter3/room3_forge.png"));
   assertEquals(AudioManager.Effect.STONE_RUMBLE, AudioManager.cueForRoom("/images/chapter3/room5_miners.png"));
   assertEquals(AudioManager.Effect.WIND_GUST, AudioManager.cueForRoom("/images/chapter3/room6_eco.png"));
   assertNull(AudioManager.cueForRoom("/images/chapter1/room2_stern.png"));
   assertNull(AudioManager.cueForRoom(null));
 }
}
