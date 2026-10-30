# The Alchemist's Hangover

Avventura grafica Java Swing con scene PNG, oggetti interattivi, dialoghi, inventario, ricette e salvataggi H2.

## Avvio

Richiede **JDK 25** e Maven.

```bash
mvn clean test
mvn exec:java
```

## Comandi

- **Mouse:** interagisci con oggetti e hotspot.
- **Frecce disegnate sullo schermo:** naviga tra le stanze.
- **Click / Spazio / Invio:** durante un dialogo mostra la frase completa oppure passa alla successiva.
- **MOSTRA FRASE:** visualizza subito tutto il testo corrente senza saltarlo.
- **SKIP:** durante l'introduzione salta direttamente al capitolo 1.
- **ESC:** menu pausa durante i capitoli; ESC chiude anche il diario e le impostazioni.
- **J:** apri il diario di Arthur.
- **F2:** scorciatoia *solo di sviluppo* per accedere al capitolo 2.

## Rifiniture pre-capitolo 3

- Evidenziazione degli oggetti interattivi, tooltips e notifiche di raccolta senza dialoghi bloccanti.
- Dialoghi con avanzamento e rivelazione distinti, punteggiatura scandita e sfondo dello SKIP opaco.
- Cuori aggiornati dal modello e visibili durante i due boss.
- Retry del boss dopo il game-over senza ripetere il dialogo introduttivo.
- Diario contestuale che deriva le annotazioni dallo stato già salvato.
- Menu pausa e impostazioni audio/transizioni locali.
- Colonna sonora ambientale e segnali sonori **originali, generati proceduralmente**, silenziati automaticamente se manca un dispositivo audio.
- Schermata provvisoria di fine Capitolo II, in attesa degli asset del Capitolo III.

## Salvataggi e test

I salvataggi reali restano in `saves/alchemist.mv.db`. **Non cancellare questa cartella.**

I test JUnit usano esclusivamente `jdbc:h2:mem:alchemist_test`. Non scrivono, sovrascrivono o cancellano slot del database reale. La build su GitHub Actions esegue `mvn clean test` con Java 25.

La directory `target/` contiene file generati: non includerli nei commit. Nel repository precedente alcuni file di `target/` e `saves/` risultano già tracciati: modificarne la gestione Git richiede attenzione per non perdere dati locali.
