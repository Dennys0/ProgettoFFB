package com.alchemist.hangover.controller;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DialogueSystem {

    public static class DialogueLine {
        private String speaker;
        private String text;

        public DialogueLine(String speaker, String text) {
            this.speaker = speaker;
            this.text = text;
        }

        public String getSpeaker() { return speaker; }
        public String getText() { return text; }
    }

    private JLabel speakerLabel;
    private JLabel textLabel;
    private Runnable onSpeakerChanged;
    private Runnable onDialogueComplete;

    private List<DialogueLine> dialogueLines;
    private int currentIndex = -1;

    // Gestione Macchina da Scrivere (Typewriter Effect)
    private Timer typewriterTimer;
    private String currentFullText = "";
    private int charIndex = 0;
    private boolean isTyping = false;

    public DialogueSystem(JLabel speakerLabel, JLabel textLabel, Runnable onSpeakerChanged, Runnable onDialogueComplete) {
        this.speakerLabel = speakerLabel;
        this.textLabel = textLabel;
        this.onSpeakerChanged = onSpeakerChanged;
        this.onDialogueComplete = onDialogueComplete;
        this.dialogueLines = new ArrayList<>();
    }

    public void loadDialogue(String resourcePath) {
        stop();
        dialogueLines.clear();
        currentIndex = -1;

        java.io.InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is == null) {
            System.err.println("File di dialogo non trovato: " + resourcePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|", 2);
                if (parts.length == 2) {
                    dialogueLines.add(new DialogueLine(parts[0].trim(), parts[1].trim()));
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante il caricamento del dialogo (" + resourcePath + "): " + e.getMessage());
        }
    }

    public void startDialogue() {
        if (!dialogueLines.isEmpty()) {
            currentIndex = 0;
            displayCurrentLine();
        } else if (onDialogueComplete != null) {
            onDialogueComplete.run();
        }
    }

    public void nextLine() {
        if (isTyping) {
            // Se sta ancora scrivendo, il click mostra subito il testo completo
            completeTypewriting();
            return;
        }

        currentIndex++;
        if (currentIndex < dialogueLines.size()) {
            displayCurrentLine();
        } else {
            stop();
            if (onDialogueComplete != null) {
                onDialogueComplete.run();
            }
        }
    }

    private void displayCurrentLine() {
        DialogueLine line = getCurrentLine();
        if (line == null) return;

        if (speakerLabel != null) {
            speakerLabel.setText(line.getSpeaker().equals("NARRATOR") ? "" : line.getSpeaker());
        }

        if (onSpeakerChanged != null) {
            onSpeakerChanged.run();
        }

        currentFullText = line.getText();
        charIndex = 0;
        isTyping = true;
        textLabel.setText("");

        stop(); // Ferma eventuali timer precedenti

        // Effetto macchina da scrivere (15ms a carattere)
        typewriterTimer = new Timer(15, e -> {
            if (charIndex < currentFullText.length()) {
                textLabel.setText("<html>" + currentFullText.substring(0, charIndex + 1) + "</html>");
                charIndex++;
            } else {
                completeTypewriting();
            }
        });
        typewriterTimer.start();
    }

    private void completeTypewriting() {
        if (typewriterTimer != null && typewriterTimer.isRunning()) {
            typewriterTimer.stop();
        }
        textLabel.setText("<html>" + currentFullText + "</html>");
        isTyping = false;
    }

    // Metodo per fermare il dialogo (usato da SKIP)
    public void stop() {
        if (typewriterTimer != null && typewriterTimer.isRunning()) {
            typewriterTimer.stop();
        }
        isTyping = false;
    }

    public DialogueLine getCurrentLine() {
        if (currentIndex >= 0 && currentIndex < dialogueLines.size()) {
            return dialogueLines.get(currentIndex);
        }
        return null;
    }

    public boolean isTyping() {
        return isTyping;
    }
}