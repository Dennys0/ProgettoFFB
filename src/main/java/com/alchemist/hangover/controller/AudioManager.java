package com.alchemist.hangover.controller;

import javax.sound.sampled.*;
import java.util.Random;

/**
 * Audio originale generato proceduralmente: non servono WAV esterni.
 * Se il computer non offre un'uscita audio compatibile, il gioco rimane utilizzabile.
 */
public final class AudioManager {
    /** Identificatori degli effetti sintetizzati dal servizio audio. */
    public enum Effect {
        CLICK, PICKUP, DAMAGE, SERPENT_CALL, MAGMA_BUBBLE, MAGMA_HEART, SUCCESS, RECIPE_READY, FOOTSTEP, EARTH_BREATH,
        WIND_GUST, WATER_DROP, FORGE_SPARK, STONE_RUMBLE, RUNE_CHIME
    }

    private static final AudioManager INSTANCE = new AudioManager();
    private static final int SAMPLE_RATE = 8000;
    private static final AudioFormat FORMAT = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, 16, 1, 2,
            SAMPLE_RATE, false);

    // Sintesi e apertura delle tracce sono serializzate per non bloccare il thread Swing.
    private final java.util.concurrent.ExecutorService worker =
            java.util.concurrent.Executors.newSingleThreadExecutor(task -> {
                Thread thread = new Thread(task, "alchemist-audio");
                thread.setDaemon(true);
                return thread;
            });
    private final java.util.concurrent.atomic.AtomicLong sceneVersion = new java.util.concurrent.atomic.AtomicLong();
    private final java.util.concurrent.atomic.AtomicBoolean typingPending = new java.util.concurrent.atomic.AtomicBoolean();
    private final java.util.concurrent.atomic.AtomicInteger pendingEffects = new java.util.concurrent.atomic.AtomicInteger();
    private final java.util.Map<Effect, byte[]> effectSamples = new java.util.EnumMap<>(Effect.class);
    private final java.util.Map<String, byte[]> sceneSamples = new java.util.HashMap<>();
    private final java.util.Set<Clip> effects = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private volatile boolean closed;

    /**
     * Accoda l’ambiente della nuova scena. Il contatore di versione scarta le richieste superate
     * da un cambio scena più recente.
     */
    public void playScene(String scene) {
        long version = sceneVersion.incrementAndGet();
        submit(() -> { if (version == sceneVersion.get()) playSceneNow(scene); });
    }

    /**
     * Riapplica le preferenze alla scena attiva sull’esecutore audio.
     */
    public void refreshSettings() {
        long version = sceneVersion.incrementAndGet();
        submit(() -> {
            if (version != sceneVersion.get()) return;
            String scene = activeScene;
            activeScene = null;
            playSceneNow(scene);
        });
    }

    /**
     * Accoda un solo tic di scrittura alla volta, evitando accumuli durante l’animazione.
     */
    public void playTypingTick() {
        if (!typingPending.compareAndSet(false, true)) return;
        if (!submit(() -> { try { playTypingTickNow(); } finally { typingPending.set(false); } }))
            typingPending.set(false);
    }

    /**
     * Accoda un effetto breve rispettando il limite degli effetti in attesa.
     */
    public void playEffect(Effect effect) {
        if (effect == null || pendingEffects.incrementAndGet() > 8) {
            if (effect != null) pendingEffects.decrementAndGet();
            return;
        }
        if (!submit(() -> { try { playEffectNow(effect); } finally { pendingEffects.decrementAndGet(); } }))
            pendingEffects.decrementAndGet();
    }

    /**
     * Invia il lavoro all’esecutore audio; rifiuta le richieste dopo la chiusura o in un
     * ambiente senza schermo.
     */
    private boolean submit(Runnable task) {
        if (closed || java.awt.GraphicsEnvironment.isHeadless()) return false;
        try { worker.execute(task); return true; }
        catch (java.util.concurrent.RejectedExecutionException ex) { return false; }
    }

    /** La chiusura è ordinata dopo le operazioni già accodate, fuori dall'EDT. */
    public void close() {
        closed = true;
        try {
            worker.execute(() -> {
                stopAmbience();
                if (typingClip != null) typingClip.close();
                effects.forEach(Clip::close);
                effects.clear();
            });
        } catch (java.util.concurrent.RejectedExecutionException ignored) { }
        worker.shutdown();
        try { worker.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS); }
        catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
    }

    private Clip ambience;
    private Clip typingClip;
    private boolean typingUnavailable;
    private String activeScene;

    private AudioManager() { }
    public static AudioManager getInstance() { return INSTANCE; }

    /**
     * Cambia l’ambiente sull’esecutore audio, riutilizzando i campioni già generati.
     */
    private void playSceneNow(String scene) {
        if (scene == null) return;
        if (scene.equals(activeScene) && ambience != null && ambience.isRunning()) return;
        stopAmbience();
        activeScene = scene;
        boolean battleMusic = "STORM_DUEL".equals(scene);
        if (!GameSettings.getInstance().isSoundEnabled()
                || (!battleMusic && !GameSettings.getInstance().isAmbienceEnabled())) return;
        if (!(battleMusic || "INTRO".equals(scene) || "CHAPTER1".equals(scene)
                || "CHAPTER2".equals(scene) || "CHAPTER3".equals(scene)
                || "CHAPTER4".equals(scene) || "EPILOGUE".equals(scene) || "MENU".equals(scene))) return;
        try {
            byte[] samples = sceneSamples.computeIfAbsent(scene, key -> battleMusic ? makeStormMusic() : makeAmbience(key));
            ambience = AudioSystem.getClip();
            ambience.open(FORMAT, samples, 0, samples.length);
            setLevel(ambience, -12f);
            ambience.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException ex) {
            // Nessun dispositivo sonoro: disattiviamo solo l'audio.
            stopAmbience();
        }
    }

    /**
     * Ferma e libera la traccia ambientale attualmente aperta.
     */
    private void stopAmbience() {
        if (ambience != null) {
            ambience.stop();
            ambience.close();
            ambience = null;
        }
    }

    /**
     * Piccoli effetti d'ambiente per una selezione di stanze importanti,
     * senza introdurre file WAV o sovrapporre loop alla musica di capitolo.
     */
    static Effect cueForRoom(String resourcePath) {
        if (resourcePath == null) return null;
        if (resourcePath.contains("/chapter1/")) {
            if (resourcePath.contains("room4_lab")) return Effect.FORGE_SPARK;
            if (resourcePath.contains("room1_deck")
                    || resourcePath.contains("room3_prow")
                    || resourcePath.contains("room7_boss")) return Effect.WIND_GUST;
        } else if (resourcePath.contains("/chapter2/")) {
            if (resourcePath.contains("room5_pit")) return Effect.STONE_RUMBLE;
            if (resourcePath.contains("room1_hall")
                    || resourcePath.contains("room4_gardens")
                    || resourcePath.contains("room7_boss")) return Effect.WATER_DROP;
        } else if (resourcePath.contains("/chapter3/")) {
            if (resourcePath.contains("room2_rune")) return Effect.RUNE_CHIME;
            if (resourcePath.contains("room3_forge")) return Effect.FORGE_SPARK;
            if (resourcePath.contains("room5_miners")
                    || resourcePath.contains("room7_boss")) return Effect.STONE_RUMBLE;
            if (resourcePath.contains("room6_eco")) return Effect.WIND_GUST;
        } else if (resourcePath.contains("/chapter4/")) {
            if (resourcePath.endsWith("room_1.png") || resourcePath.endsWith("room_3.png")
                    || resourcePath.endsWith("room_5.png")) return Effect.MAGMA_BUBBLE;
            if (resourcePath.endsWith("room_6.png")) return Effect.WIND_GUST;
            if (resourcePath.endsWith("room_boss.png")) return Effect.STONE_RUMBLE;
        }
        return null;
    }

    /**
     * Associa il fondale a un breve accento sonoro di ingresso nella stanza.
     */
    public void playRoomCue(String resourcePath) {
        Effect cue = cueForRoom(resourcePath);
        playEffect(cue == null ? Effect.FOOTSTEP : cue);
    }

    private void playTypingTickNow() {
        GameSettings settings = GameSettings.getInstance();
        if (!settings.isSoundEnabled() || !settings.isTypingEnabled() || typingUnavailable) return;
        try {
            if (typingClip == null) {
                byte[] tick = new byte[320];
                for (int i = 0; i < tick.length / 2; i++)
                    writeSample(tick, i, 0.11 * Math.sin(i * 0.85) * Math.exp(-i / 22.0));
                typingClip = AudioSystem.getClip();
                typingClip.open(FORMAT, tick, 0, tick.length);
                setLevel(typingClip, -17f);
            }
            if (!typingClip.isRunning()) {
                typingClip.setFramePosition(0);
                typingClip.start();
            }
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException ex) {
            if (typingClip != null) typingClip.close();
            typingClip = null;
            typingUnavailable = true;
        }
    }

    /**
     * Apre un Clip e ne libera le risorse al termine, limitando le riproduzioni sovrapposte.
     */
    private void playEffectNow(Effect effect) {
        if (!GameSettings.getInstance().isSoundEnabled()) return;
        try {
            if (effects.size() >= 8) return;
            byte[] samples = effectSamples.computeIfAbsent(effect, AudioManager::makeEffect);
            Clip sound = AudioSystem.getClip();
            sound.open(FORMAT, samples, 0, samples.length);
            setLevel(sound, effect == Effect.DAMAGE ? -7f : -16f);
            sound.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    sound.close();
                    effects.remove(sound);
                }
            });
            effects.add(sound);
            sound.start();
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException ignored) {
            // Gli effetti sono facoltativi, mai bloccare l'esplorazione.
        }
    }

    /**
     * Regola il guadagno soltanto se il dispositivo espone il controllo del volume.
     */
    private static void setLevel(Clip clip, float decibels) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), decibels)));
        }
    }

    /**
     * Sintetizza il fondo sonoro della scena in campioni PCM riutilizzabili dalla cache.
     */
    private static byte[] makeAmbience(String scene) {
        int length = SAMPLE_RATE * 12;
        byte[] pcm = new byte[length * 2];
        Random random = new Random(scene.hashCode());
        boolean water = "CHAPTER2".equals(scene);
        boolean menu = "MENU".equals(scene) || "EPILOGUE".equals(scene);
        boolean fire = "CHAPTER4".equals(scene);
        boolean earth = "CHAPTER3".equals(scene);
        double[] notes = water ? new double[]{110.0, 146.8, 164.8, 146.8}
                : earth ? new double[]{65.4, 82.4, 73.4, 82.4}
                : menu ? new double[]{130.8, 164.8, 196.0, 164.8}
                : new double[]{146.8, 174.6, 220.0, 174.6};
        double filteredNoise = 0;
        double ember = 0;
        for (int i = 0; i < length; i++) {
            double t = (double) i / SAMPLE_RATE;
            int noteIndex = Math.min(3, (int) ((t % 6) / 1.5));
            double phase = (t % 1.5) / 1.5;
            double envelope = Math.pow(Math.sin(Math.PI * phase), 2);
            double note = Math.sin(2 * Math.PI * notes[noteIndex] * t)
                    + 0.35 * Math.sin(2 * Math.PI * notes[noteIndex] * 0.5 * t);
            double wind = random.nextDouble() * 2 - 1;
            filteredNoise = 0.994 * filteredNoise + 0.006 * wind;
            double swell = 0.5 + 0.5 * Math.sin(2 * Math.PI * t / 5.0);
            double sample;
            if (fire) {
                ember = random.nextDouble() < 0.0006 ? 0.14 : ember * 0.91;
                sample = (0.5 + swell) * filteredNoise + ember * wind
                        + 0.07 * Math.sin(2 * Math.PI * 52 * t) * swell;
            } else if (earth) {
                double heartbeat = Math.pow(Math.max(0, Math.cos(Math.PI * t)), 12);
                sample = (0.04 + 0.19 * heartbeat) * Math.sin(2 * Math.PI * 58 * t)
                        + 0.5 * filteredNoise;
            } else if (water) {
                sample = (0.35 + 1.4 * swell) * filteredNoise
                        + 0.07 * envelope * Math.sin(2 * Math.PI * 91 * t);
            } else if (menu) {
                sample = 0.12 * envelope * note + 0.22 * filteredNoise;
            } else {
                sample = (0.5 + 1.4 * swell) * filteredNoise
                        + 0.06 * envelope * Math.sin(2 * Math.PI * (117 * t + swell));
            }
            // Dissolvenza ai confini del loop, per evitare scatti.
            double edge = Math.min(1.0, Math.min(i, length - i - 1) / 450.0);
            writeSample(pcm, i, 0.27 * sample * edge);
        }
        return pcm;
    }

    /**
     * Costruisce il motivo dello scontro con Hræsvelgr combinando note e pulsazioni ritmiche.
     */
    private static byte[] makeStormMusic() {
        double[] bass = {110, 110, 130.81, 98, 110, 146.83, 130.81, 98};
        int beat = SAMPLE_RATE / 2;
        byte[] pcm = new byte[beat * 16 * 2];
        for (int i = 0; i < pcm.length / 2; i++) {
            int pulse = i / beat;
            double t = (i % beat) / (double) SAMPLE_RATE;
            double note = bass[(pulse / 2) % bass.length];
            double drum = Math.exp(-15 * t) * Math.sin(2 * Math.PI * 62 * t);
            double strings = Math.sin(2 * Math.PI * note * t)
                    + 0.38 * Math.sin(2 * Math.PI * note * 1.5 * t);
            double edge = Math.min(1, Math.min(t * 110, (0.5 - t) * 70));
            writeSample(pcm, i, edge * (0.20 * drum + 0.12 * Math.exp(-5 * t) * strings));
        }
        return pcm;
    }

    /**
     * Genera il suono del danno con un attacco breve e un decadimento marcato.
     */
    private static byte[] makeImpact() {
        byte[] pcm = new byte[SAMPLE_RATE]; // Mezzo secondo di PCM a 16 bit.
        Random noise = new Random(71);
        double rumble = 0;
        for (int i = 0; i < pcm.length / 2; i++) {
            double t = i / (double) SAMPLE_RATE;
            rumble = 0.7 * rumble + 0.3 * (noise.nextDouble() * 2 - 1);
            double thud = Math.sin(2 * Math.PI * (94 * t - 53 * t * t));
            double crack = Math.exp(-25 * t) * rumble;
            double envelope = Math.min(1, t * 300) * Math.min(1, (0.5 - t) * 65);
            writeSample(pcm, i, envelope * (0.65 * thud * Math.exp(-8 * t) + 0.45 * crack));
        }
        return pcm;
    }

    /**
     * Genera il ribollire della fornace o la variante a battito, in base al parametro.
     */
    private static byte[] makeMagma(boolean heartbeat) {
        int frames = heartbeat ? SAMPLE_RATE * 3 / 2 : SAMPLE_RATE;
        byte[] pcm = new byte[frames * 2];
        double[] pulses = heartbeat ? new double[]{0.08, 0.58} : new double[]{0.04, 0.22, 0.53, 0.70};
        for (int i = 0; i < frames; i++) {
            double t = i / (double) SAMPLE_RATE;
            double sample = 0;
            for (double onset : pulses) {
                double age = t - onset;
                if (age < 0) continue;
                double phase = 2 * Math.PI * (heartbeat ? 54 * age :
                        90 * age + 0.65 * (1 - Math.exp(-26 * age)));
                sample += Math.sin(phase) * Math.exp(-(heartbeat ? 7 : 16) * age)
                        * Math.min(1, age * 80);
            }
            writeSample(pcm, i, 0.40 * sample * Math.min(1, (frames - 1 - i) / 100.0));
        }
        return pcm;
    }

    /**
     * Seleziona la sintesi dell’effetto richiesto e restituisce i campioni da riprodurre.
     */
    private static byte[] makeEffect(Effect effect) {
        if (effect == Effect.MAGMA_BUBBLE || effect == Effect.MAGMA_HEART)
            return makeMagma(effect == Effect.MAGMA_HEART);
        if (effect == Effect.SERPENT_CALL) {
            byte[] pcm = new byte[SAMPLE_RATE * 2];
            for (int i = 0; i < SAMPLE_RATE; i++) {
                double t = (double) i / SAMPLE_RATE;
                double phase = 2 * Math.PI * (86 * t - 16 * t * t);
                double voice = Math.sin(phase) + 0.4 * Math.sin(2 * phase + Math.sin(18 * t));
                writeSample(pcm, i, 0.36 * voice * Math.pow(Math.sin(Math.PI * t), 2));
            }
            return pcm;
        }
        if (effect == Effect.DAMAGE) return makeImpact();
        if (effect == Effect.RECIPE_READY) return makeRecipeChord();
        if (effect == Effect.EARTH_BREATH) return makeEarthBreath();
        if (effect == Effect.FOOTSTEP || effect == Effect.WIND_GUST || effect == Effect.WATER_DROP
                || effect == Effect.FORGE_SPARK || effect == Effect.STONE_RUMBLE
                || effect == Effect.RUNE_CHIME) return makeRoomEffect(effect);
        double seconds = 0.27;
        int length = (int) (SAMPLE_RATE * seconds);
        byte[] pcm = new byte[length * 2];
        for (int i = 0; i < length; i++) {
            double t = (double) i / SAMPLE_RATE;
            double progress = t / seconds;
            double frequency = switch (effect) {
                case PICKUP -> 650 + 440 * progress;
                case SERPENT_CALL, MAGMA_HEART, MAGMA_BUBBLE, DAMAGE -> throw new AssertionError(effect);
                case SUCCESS -> 420 + 600 * progress;
                case CLICK -> 550;
                case EARTH_BREATH, RECIPE_READY, FOOTSTEP -> 55; // gestito separatamente sopra
                case WIND_GUST, WATER_DROP, FORGE_SPARK, STONE_RUMBLE, RUNE_CHIME -> 220;
            };
            double envelope = Math.pow(1 - progress, 2) * Math.min(1, progress * 35);
            writeSample(pcm, i, 0.24 * envelope
                    * Math.sin(2 * Math.PI * frequency * t));
        }
        return pcm;
    }


    /** Brevi campioni originali: aria, gocce, scintille, pietra e rune. */
    private static byte[] makeRoomEffect(Effect effect) {
        double seconds = effect == Effect.STONE_RUMBLE ? 0.66 : 0.46;
        int count = (int) (SAMPLE_RATE * seconds);
        byte[] data = new byte[count * 2];
        Random noise = new Random(effect.ordinal() * 1741L);
        double filtered = 0;
        for (int i = 0; i < count; i++) {
            double t = (double) i / SAMPLE_RATE;
            double p = t / seconds;
            double fade = Math.sin(Math.PI * p);
            double sample;
            switch (effect) {
                case FOOTSTEP -> {
                    filtered = 0.83 * filtered + 0.17 * (noise.nextDouble() * 2 - 1);
                    sample = (0.08 * Math.sin(2 * Math.PI * 94 * t) + filtered * 0.3)
                            * Math.exp(-11 * p) * Math.min(1, p * 28);
                }
                case WIND_GUST -> {
                    filtered = 0.94 * filtered + 0.06 * (noise.nextDouble() * 2 - 1);
                    sample = 0.26 * filtered * fade * fade;
                }
                case WATER_DROP -> {
                    double frequency = 520.0 - 330.0 * p;
                    sample = 0.21 * Math.sin(2 * Math.PI * frequency * t)
                            * Math.exp(-7 * p) * Math.min(1, p * 35);
                }
                case FORGE_SPARK -> {
                    double frequency = 620 + 980 * p;
                    sample = 0.16 * Math.sin(2 * Math.PI * frequency * t)
                            * Math.exp(-9 * p);
                }
                case STONE_RUMBLE -> {
                    filtered = 0.96 * filtered + 0.04 * (noise.nextDouble() * 2 - 1);
                    sample = 0.20 * (Math.sin(2 * Math.PI * 60 * t)
                            + filtered * 0.3) * fade;
                }
                case RUNE_CHIME -> sample = 0.14 *
                        (Math.sin(2 * Math.PI * 392 * t)
                        + 0.45 * Math.sin(2 * Math.PI * 588 * t))
                        * Math.exp(-5 * p) * Math.min(1, p * 25);
                default -> sample = 0;
            }
            writeSample(data, i, sample * fade * 0.6);
        }
        return data;
    }

    /** Tre attacchi di vetro sovrapposti, con decadimento naturale. */
    private static byte[] makeRecipeChord() {
        double[] notes = {392, 493.88, 587.33};
        int length = SAMPLE_RATE * 3 / 2;
        byte[] pcm = new byte[length * 2];
        for (int i = 0; i < length; i++) {
            double t = (double) i / SAMPLE_RATE;
            double sample = 0;
            for (int n = 0; n < notes.length; n++) {
                double age = t - n * 0.16;
                if (age < 0) continue;
                double phase = 2 * Math.PI * notes[n] * age;
                double ring = Math.sin(phase) + 0.2 * Math.sin(2.01 * phase);
                sample += 0.18 * ring * Math.min(1, age / 0.015) * Math.exp(-4 * age);
            }
            writeSample(pcm, i, sample * Math.min(1, (length - 1 - i) / 320.0));
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

    /**
     * Limita l’ampiezza del campione e la converte in due byte PCM a 16 bit.
     */
    private static void writeSample(byte[] target, int index, double normalized) {
        short pcm = (short) (Math.max(-1, Math.min(1, normalized)) * 32767);
        target[index * 2] = (byte) (pcm & 0xff);
        target[index * 2 + 1] = (byte) ((pcm >>> 8) & 0xff);
    }
}
