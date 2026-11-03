# The Alchemist's Hangover

Avventura grafica Java Swing in quattro capitoli: Aria, Acqua, Terra e Fuoco.
Arthur esplora stanze illustrate, parla con gli NPC e prepara quattro cocktail.
I boss hanno meccaniche differenti; inventario, diario e salvataggi H2 conservano il percorso.

## Avvio normale

Richiede **JDK 25** e Maven. Aprire la cartella che contiene `pom.xml`.
Chiudere il gioco prima di `clean`, soprattutto su Windows. Eseguire i comandi
uno alla volta e avviare solo dopo `BUILD SUCCESS`.

```powershell
mvn -version
mvn clean test
mvn exec:java
```

## Comandi

- Mouse e frecce grafiche: interazioni e navigazione.
- Click, Spazio o Invio: completa la battuta o passa alla successiva.
- MOSTRA FRASE: completa il testo corrente; SKIP salta il prologo.
- ESC: pausa durante i capitoli; J: diario.
- I boss mostrano i propri comandi. Surtur: A/S/D o clic sulle corsie.

I cuori sono mostrati durante i combattimenti. Audio, atmosfera, typing e
transizioni sono regolabili nelle opzioni. I quattro cocktail dispongono di immagini.

## Modalità di sviluppo

Le scorciatoie sono **disabilitate nell'avvio normale**. Per la demo:

```powershell
mvn exec:java "-Dalchemist.debug=true"
```

| Tasto | Azione |
| --- | --- |
| F2 | Nuova sessione al capitolo 2 |
| F3 | Nuova sessione al capitolo 3 |
| F4 | Sceglie una stanza del capitolo 3 |
| F5 | Nuova sessione al capitolo 4 |
| F6 | Contorni e coordinate delle hitbox |
| F7 | Sceglie una stanza del capitolo 4 |

F1 non è assegnato. F2/F3/F5 azzerano lo stato **in memoria**: usare una sessione
di prova. Non eliminano gli slot, ma salvare successivamente può sovrascriverli.

## Registro REST delle sfide

Funzione facoltativa, separata dai salvataggi. Il servizio ascolta solo su
`127.0.0.1`; non richiede account né collegamenti a servizi Internet.

Primo terminale, dalla cartella del progetto:

```powershell
mvn compile exec:java "-Dexec.mainClass=com.alchemist.hangover.network.ChallengeServer"
```

Secondo terminale:

```powershell
mvn exec:java "-Dalchemist.registry=true"
```

Compare la voce **Registro sfide** nel menu. Vittorie e sconfitte contro i boss
vengono inviate in background. Il registro conta i risultati dall'avvio del
servizio: **si azzera al riavvio del server**. Non è una classifica certificata.
Se il servizio è assente, il gioco prosegue offline; gli invii falliti non vengono
ritentati né recuperati successivamente. CTRL+C arresta il servizio.

La porta predefinita è 8765. Per cambiarla, aggiungere
`"-Dalchemist.registry.port=8766"` a entrambi i comandi.

- `POST /results`: corpo form `chapter=1&outcome=win` oppure `loss`; risposta 201.
- `GET /stats`: statistiche in testo UTF-8; risposta 200.
- Errori: 400 per valori non ammessi, 405 per metodo errato, 413 per corpo oltre 256 byte.
- Client: timeout di connessione 1 s e richiesta 2 s; coda di invio limitata.

## Prestazioni e architettura

Le operazioni H2 della finestra salvataggi usano `SwingWorker`: lo snapshot viene
catturato sull'EDT, letto/scritto in background e applicato sull'EDT.
L'audio usa un esecutore singolo, campioni riutilizzati e un limite agli effetti
contemporanei. Le scene sono create al primo utilizzo e poi conservate; i ritratti
NPC sono caricati alla prima battuta. Una cache LRU di 32 MiB condivide immagini
ridimensionate tra inventari e dialoghi. Non conserva tutte le scene del gioco.

```powershell
mvn exec:java "-Dalchemist.profile=true"
```

Il terminale stampa tempi di costruzione della finestra, delle scene,
dell'inventario e delle operazioni H2. Sono durate delle operazioni, non FPS o
latenza fino al primo pixel sullo schermo.

Il codice è organizzato in `model`, `database`, `controller`, `gameplay`, `view`
e `network`. Leggere [ARCHITETTURA](docs/ARCHITETTURA.md) e
[misurazioni e collaudo](docs/PRESTAZIONI_E_COLLAUDO.md).

## Dati e test

I salvataggi reali sono in `saves/alchemist.mv.db`: non cancellare la cartella.
I test usano esclusivamente H2 in memoria; `target/` contiene solo output generato.
Le preferenze audio sono memorizzate con Java Preferences, separatamente dagli slot.
La CI verifica il branch `refactor/organizzazione-gioco` con Java 25 e Maven.

Prima del passaggio a `main` servono build verde e collaudo manuale completo:
quattro capitoli, boss, salvataggi, epilogo e nuova partita. Verificare anche i
crediti nominativi, la provenienza degli asset e i dati anagrafici della relazione.
