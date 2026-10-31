# Capitolo 3 — Terra: Il Dominio di Ymir

## Asset consegnati (7 scenari)

I PNG devono essere copiati nella cartella
`src/main/resources/images/chapter3/`, con **questi nomi esatti**:

| Stanza | Scenario | PNG |
|---|---|---|
| 1 | Radura delle Radici | room1_hub.png |
| 2 | Cerchio delle Rune | room2_rune.png |
| 3 | Forgia di Caverna | room3_forge.png |
| 4 | Foresta Pietrificata | room4_forest.png |
| 5 | Miniere Basaltiche | room5_miners.png |
| 6 | Rupe dell'Eco | room6_eco.png |
| 7 | Cuore di Roccia | room7_boss.png |

Tutti gli scenari ricevuti sono PNG 1672 x 941 (16:9). Il motore li
ridimensiona a 1280 x 720, senza richiedere un'ulteriore conversione.
I sette PNG non fanno parte del commit del codice: devono essere copiati
dall'archivio degli asset nell'albero di progetto.

## Collegamenti già implementati

| Da | UP | DOWN | LEFT | RIGHT |
|---|---|---|---|---|
| 1 | 2 | 5 | 3 | 4 |
| 2 | 6 | 1 | - | - |
| 3 | - | - | - | 1 |
| 4 | - | - | 1 | - |
| 5 | 1 | 7 | - | - |
| 6 | - | 2 | - | - |
| 7 | 5 | - | - | - |

Le parole *avanti* e *indietro* della sceneggiatura sono state rese
mediante frecce cardinali per evitare ambiguità nel motore.

## Elementi in attesa di sprite e posizionamento

- Stanza 1: hotspot sulla crepa che emette il respiro di Ymir.
- Stanza 2: Gunnar, monologo sulla pazienza e indizio sulla radice velenosa.
- Stanza 3: boccale di pietra e ricetta dell'**Ymir's Old Fashioned**.
- Stanza 4: Muschio di Midgard e Radice Nera di Nidhogg.
- Stanza 5: Frammento di Yggdrasil e Polvere di Ossa di Gigante.
- Stanza 6: stele con la combinazione dei versi per Ymir.
- Stanza 7: enigma di posizionamento e rune; drop Lacrima di Ymir.

Non sono ancora presenti gli hotspot, gli sprite, la logica del boss
o i dialoghi. Saranno implementati con gli asset dei personaggi e
delle creature, dopo aver scelto insieme le coordinate delle interazioni.

## Prova del livello (anteprima)

1. Passa al branch `feat/chapter3`.
2. Copia le sette immagini in `src/main/resources/images/chapter3/`.
3. Avvia `mvn clean test` e `mvn exec:java`.
4. Nel gioco premi **F3** per aprire il capitolo 3 in modalità anteprima.
5. Esplora le sette stanze con le quattro frecce.

**Attenzione:** F3 è una scorciatoia da sviluppo; azzera lo stato
corrente in memoria, quindi salva la partita prima di usarla.
Non cancella il database H2 né gli slot su disco.

I salvataggi con capitolo 3 possono già riaprire la stanza corretta.
L'accesso narrativo dal finale del capitolo 2 rimane in modalità
anteprima fino a quando il terzo capitolo sarà completato.
