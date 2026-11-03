package com.alchemist.hangover.controller;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Legge dialoghi UTF-8 nel formato PERSONAGGIO|testo e li riproduce nelle etichette Swing.
 * Gestisce avanzamento, scrittura progressiva e completamento; i comandi vanno usati sul thread
 * degli eventi Swing.
 */
public class DialogueSystem {

    /** Una battuta del file: nome del parlante e testo da mostrare. */
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

    // Animazione del testo con effetto macchina da scrivere.
    private Timer typewriterTimer;
    private String currentFullText = "";
    private int charIndex = 0;
    private boolean isTyping = false;
    private boolean finished = false;
    // La larghezza resta opzionale, così i dialoghi dei capitoli precedenti
    // mantengono l'impaginazione originale.
    private int wrapWidth = 0;

    public void setWrapWidth(int width) {
        this.wrapWidth = Math.max(0, width);
    }

    public DialogueSystem(JLabel speakerLabel, JLabel textLabel, Runnable onSpeakerChanged, Runnable onDialogueComplete) {
        this.speakerLabel = speakerLabel;
        this.textLabel = textLabel;
        this.onSpeakerChanged = onSpeakerChanged;
        this.onDialogueComplete = onDialogueComplete;
        this.dialogueLines = new ArrayList<>();
    }

    /**
     * Interrompe la conversazione precedente e legge la risorsa UTF-8. Ignora righe vuote e
     * commenti e divide soltanto al primo separatore verticale.
     */
    public void loadDialogue(String resourcePath) {
        stop();
        dialogueLines.clear();
        currentIndex = -1;
        finished = false;

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

    /**
     * Mostra la prima battuta; se non ci sono righe, completa subito la conversazione.
     */
    public void startDialogue() {
        if (!dialogueLines.isEmpty()) {
            currentIndex = 0;
            displayCurrentLine();
        } else {
            finishDialogue();
        }
    }

    /** Mostra subito tutta la frase senza saltare al dialogo seguente. */
    public void revealCurrentLine() {
        if (isTyping) completeTypewriting();
    }

    /** Salta l'intera conversazione, eseguendone una volta la callback finale. */
    public void skipDialogue() {
        if (currentIndex >= 0 && !finished) finishDialogue();
    }

    /**
     * Il primo comando completa la battuta ancora in scrittura; il successivo passa alla
     * seguente.
     */
    public void nextLine() {
        if (finished || currentIndex < 0) return;
        if (isTyping) {
            // Se sta ancora scrivendo, il click mostra subito il testo completo
            completeTypewriting();
            return;
        }

        currentIndex++;
        if (currentIndex < dialogueLines.size()) {
            displayCurrentLine();
        } else {
            finishDialogue();
        }
    }

    /**
     * Esegue la funzione conclusiva una sola volta, anche se arrivano più comandi di
     * avanzamento.
     */
    private void finishDialogue() {
        if (finished) return;
        finished = true;
        stop();
        if (onDialogueComplete != null) onDialogueComplete.run();
    }

    /**
     * Aggiorna il parlante e avvia il timer Swing della battuta, con pause sulla punteggiatura.
     */
    private void displayCurrentLine() {
        DialogueLine line = getCurrentLine();
        if (line == null) return;

        if (speakerLabel != null) {
            speakerLabel.setText(line.getSpeaker().equals("NARRATOR") ? "" : line.getSpeaker());
        }

        if (onSpeakerChanged != null) {
            onSpeakerChanged.run();
        }

        stop(); // Ferma eventuali timer precedenti.
        currentFullText = line.getText();
        charIndex = 0;
        isTyping = true;
        textLabel.setText("");

        // Effetto macchina da scrivere, con pause naturali alla punteggiatura.
        typewriterTimer = new Timer(15, e -> {
            if (charIndex < currentFullText.length()) {
                char ch = currentFullText.charAt(charIndex);
                if (textLabel.isShowing() && Character.isLetter(ch) && (charIndex == 0 || Character.isWhitespace(currentFullText.charAt(charIndex - 1))))
                    AudioManager.getInstance().playTypingTick();
                charIndex++;
                textLabel.setText(formatLine(currentFullText.substring(0, charIndex)));
                typewriterTimer.setDelay(".,!?;:".indexOf(ch) >= 0 ? 110 : 18);
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
        textLabel.setText(formatLine(currentFullText));
        isTyping = false;
    }

    /**
     * Protegge i caratteri speciali HTML e applica, se configurato, il limite di larghezza del
     * testo.
     */
    private String formatLine(String source) {
        String escaped = source.replace("&", "&amp;")
                .replace("<", "&lt;").replace(">", "&gt;")
                .replace("\n", "<br>");
        if (wrapWidth > 0) {
            // Tabelle HTML Swing rispettano la larghezza anche per righe lunghe.
            return "<html><table width='" + wrapWidth
                    + "' cellspacing='0' cellpadding='0'><tr><td>"
                    + escaped + "</td></tr></table></html>";
        }
        return "<html>" + escaped + "</html>";
    }

    // Ferma il timer quando la scena cambia, senza attivare callback.
    public void stop() {
        if (typewriterTimer != null && typewriterTimer.isRunning()) {
            typewriterTimer.stop();
        }
        isTyping = false;
    }

    /**
     * Restituisce la battuta corrente oppure null quando non è selezionata una riga valida.
     */
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