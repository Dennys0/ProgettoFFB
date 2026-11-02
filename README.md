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
- **F3:** anteprima del capitolo 3 (*solo sviluppo*).
- **F4:** selezione di una stanza nel capitolo 3 (*solo sviluppo*).

## Architettura e capitoli

Il gioco comprende tre mondi (Cielo, Mare e Terra) con scene PNG, dialoghi,
boss distinti, calderoni condivisi, inventario e diario.

Il codice e' organizzato in **model / gameplay / controller / view / dao**.
Le nuove regole si aggiungono ai package dedicati per funzionalita';
le schermate grafiche rimangono separate dalle regole, cosi' sono testabili
anche senza una finestra Swing.

Leggi [la guida dettagliata ai package](docs/ARCHITETTURA.md) prima di creare nuove classi.

## Salvataggi e progressione

La schermata di fine capitolo crea una fotografia immutabile della partita,
senza avanzare realmente il capitolo fino al comando **CONTINUA**.
I salvataggi del Capitolo III conservano il seed delle rune e le tappe narrative
di Gunnar, compresa la sua prima conversazione.

`target/` e' output di Maven e non va pubblicata nel repository.

## Salvataggi e test

I salvataggi reali restano in `saves/alchemist.mv.db`. **Non cancellare questa cartella.**

I test JUnit usano esclusivamente `jdbc:h2:mem:alchemist_test`. Non scrivono, sovrascrivono o cancellano slot del database reale. La build su GitHub Actions esegue `mvn clean test` con Java 25.

La directory `target/` contiene file generati ed e' ignorata da Git. Non inserirla nei commit. Tieni sempre separati i salvataggi locali contenuti in `saves/`.

## Capitolo IV — Muspelheim

Le sette scene sono in `src/main/resources/images/chapter4/`.
La navigazione (frecce, inventario, dialoghi) riusa `AbstractChapterPanel`;
il boss Surtur segue Arthur con la lama. Usa **A/S/D** o clicca una corsia.
Schiva le fratture, portati sul sigillo dorato e aspetta che Surtur fissi
la traiettoria prima di spostarti. Dopo i due sigilli laterali, attira la lama
sul calderone centrale per liberare la Brace.

Il **Ragnarök Flambé** richiede Magma Filtrato e Cenere di Fenrir nei primi due slot
e **Brace di Muspelheim tassativamente nel terzo**.
Dopo il drink viene mostrato l'epilogo con il risveglio di Arthur e i ringraziamenti.
Il pulsante Salva il finale conserva il progresso in H2.

Premi **F5** durante lo sviluppo per aprire direttamente il Capitolo IV.
La versione finale dell'immagine del cocktail e i crediti nominativi
sono ancora da completare.

Leggi la [checklist di collaudo](docs/CAPITOLO4_COLLAUDO.md) prima della consegna.
