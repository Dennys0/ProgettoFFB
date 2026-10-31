package com.alchemist.hangover.controller;

import javax.sound.sampled.*;
import java.util.Random;

/**
 * Audio originale generato proceduralmente: non servono WAV esterni.
 * Se il computer non offre un'uscita audio compatibile, il gioco rimane utilizzabile.
 */
public final class AudioManager {
    public enum Effect { CLICK, PICKUP, DAMAGE, SUCCESS, EARTH_BREATH }

    private static final AudioManager INSTANCE = new AudioManager();
    private static final int SAMPLE_RATE = 8000;
    private static final AudioFormat FORMAT = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, 16, 1, 2,
            SAMPLE_RATE, false);

    private Clip ambience;
    private String activeScene;

    private AudioManager() { }
    public static AudioManager getInstance() { return INSTANCE; }

    public synchronized void playScene(String scene) {
        if (scene == null) return;
        if (scene.equals(activeScene) && ambience != null && ambience.isRunning()) return;
        stopAmbience();
        activeScene = scene;
        if (!GameSettings.getInstance().isSoundEnabled()) return;
        if (!("INTRO".equals(scene) || "CHAPTER1".equals(scene)
                || "CHAPTER2".equals(scene) || "CHAPTER3".equals(scene)
                || "MENU".equals(scene))) return;
        try {
            byte[] samples = makeAmbience(scene);
            ambience = AudioSystem.getClip();
            ambience.open(FORMAT, samples, 0, samples.length);
            setLevel(ambience, -27f);
            ambience.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException ex) {
            // Nessun dispositivo sonoro: disattiviamo solo l'audio.
            stopAmbience();
        }
    }

    public synchronized void refreshSettings() {
        if (!GameSettings.getInstance().isSoundEnabled()) {
            stopAmbience();
        } else {
            String scene = activeScene;
            activeScene = null;
            playScene(scene);
        }
    }

    private void stopAmbience() {
        if (ambience != null) {
            ambience.stop();
            ambience.close();
            ambience = null;
        }
    }

    public void playEffect(Effect effect) {
        if (!GameSettings.getInstance().isSoundEnabled()) return;
        try {
            byte[] samples = makeEffect(effect);
            Clip sound = AudioSystem.getClip();
            sound.open(FORMAT, samples, 0, samples.length);
            setLevel(sound, -16f);
            sound.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    sound.close();
                }
            });
            sound.start();
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException ignored) {
            // Gli effetti sono facoltativi, mai bloccare l'esplorazione.
        }
    }

    private static void setLevel(Clip clip, float decibels) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), decibels)));
        }
    }

    private static byte[] makeAmbience(String scene) {
        int length = SAMPLE_RATE * 6;
        byte[] pcm = new byte[length * 2];
        Random random = new Random(scene.hashCode());
        boolean water = "CHAPTER2".equals(scene);
        boolean menu = "MENU".equals(scene);
        boolean earth = "CHAPTER3".equals(scene);
        double[] notes = water ? new double[]{110.0, 146.8, 164.8, 146.8}
                : earth ? new double[]{65.4, 82.4, 73.4, 82.4}
                : menu ? new double[]{130.8, 164.8, 196.0, 164.8}
                : new double[]{146.8, 174.6, 220.0, 174.6};
        double filteredNoise = 0;
        for (int i = 0; i < length; i++) {
            double t = (double) i / SAMPLE_RATE;
            int noteIndex = Math.min(3, (int) (t / 1.5));
            double phase = (t % 1.5) / 1.5;
            double envelope = Math.pow(Math.sin(Math.PI * phase), 2);
            double note = Math.sin(2 * Math.PI * notes[noteIndex] * t)
                    + 0.35 * Math.sin(2 * Math.PI * notes[noteIndex] * 0.5 * t);
            double wind = random.nextDouble() * 2 - 1;
            filteredNoise = 0.985 * filteredNoise + 0.015 * wind;
            double swell = 0.5 + 0.5 * Math.sin(2 * Math.PI * t / 5.0);
            double sample = 0.12 * envelope * note
                    + (water ? 0.14 : 0.09) * swell * filteredNoise;
            // Dissolvenza ai confini del loop, per evitare scatti.
            double edge = Math.min(1.0, Math.min(i, length - i - 1) / 450.0);
            writeSample(pcm, i, sample * edge);
        }
        return pcm;
    }

    private static byte[] makeEffect(Effect effect) {
        if (effect == Effect.EARTH_BREATH) return makeEarthBreath();
        double seconds = effect == Effect.DAMAGE ? 0.38 : 0.27;
        int length = (int) (SAMPLE_RATE * seconds);
        byte[] pcm = new byte[length * 2];
        for (int i = 0; i < length; i++) {
            double t = (double) i / SAMPLE_RATE;
            double progress = t / seconds;
            double frequency = switch (effect) {
                case PICKUP -> 650 + 440 * progress;
                case DAMAGE -> 250 - 160 * progress;
                case SUCCESS -> 420 + 600 * progress;
                case CLICK -> 550;
                case EARTH_BREATH -> 55; // gestito separatamente sopra
            };
            double envelope = Math.pow(1 - progress, 2) * Math.min(1, progress * 35);
            writeSample(pcm, i, 0.24 * envelope
                    * Math.sin(2 * Math.PI * frequency * t));
        }
        return pcm;
    }

    /** Due battiti profondi e un respiro lento, generati senza file WAV. */
    private static byte[] makeEarthBreath() {
        double duration = 1.8;
        int length = (int) (SAMPLE_RATE * duration);
        byte[] pcm = new byte[length * 2];
        for (int i = 0; i < length; i++) {
            double t = (double) i / SAMPLE_RATE;
            double firstBeat = Math.exp(-Math.pow((t - 0.20) / 0.095, 2));
            double secondBeat = Math.exp(-Math.pow((t - 0.57) / 0.13, 2));
            double breath = Math.sin(Math.PI * t / duration);
            double fade = Math.min(1.0, Math.min(t / 0.05, (duration - t) / 0.26));
            double bass = Math.sin(2 * Math.PI * 54 * t)
                    + 0.28 * Math.sin(2 * Math.PI * 35 * t);
            double rumble = 0.035 * breath * Math.sin(2 * Math.PI * 73 * t);
            writeSample(pcm, i, (0.24 * (firstBeat + secondBeat) * bass + rumble)
                    * Math.max(0, fade));
        }
        return pcm;
    }

    private static void writeSample(byte[] target, int index, double normalized) {
        short pcm = (short) (Math.max(-1, Math.min(1, normalized)) * 32767);
        target[index * 2] = (byte) (pcm & 0xff);
        target[index * 2 + 1] = (byte) ((pcm >>> 8) & 0xff);
    }
}
