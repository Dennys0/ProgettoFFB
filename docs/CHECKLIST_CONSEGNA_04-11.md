# Consegna Alchemist Hangover — 4 novembre 2026

## Regola fino alla consegna
Questo e' un **branch di stabilizzazione**, non di nuove funzionalita'.
Non iniziare Chapter 4 o un altro refactoring strutturale prima del collaudo.
Non unire in `master` senza una build verde e una prova manuale.

## Oggi 2 novembre — test da fare in VS Code / Windows

**Preparazione (circa 20 minuti)**
- [ ] Verifica `java -version` (JDK 25) e `mvn -version`.
- [ ] `git status`: conserva prima eventuali modifiche locali.
- [ ] `git fetch origin`, poi passa al branch `release/consegna-2026-11-04`.
- [ ] Esegui `mvn clean test`; poi `mvn exec:java` o `run_game.bat`.
- [ ] Non cancellare `saves/`: contiene le partite dell'utente.

**Percorso giocabile (priorita' assoluta)**
- [ ] Menu -> START GAME -> prologo -> Capitolo 1.
- [ ] Esplora le 7 stanze del Capitolo 1: frecce, PNG, NPC, inventario, dialoghi.
- [ ] Sconfiggi Hraesvelgr: ricevi SOFFIO_HRAESVELGR e prepari il primo cocktail.
- [ ] Salva dalla schermata di completamento; annulla un secondo salvataggio
      e verifica che la partita non cambi capitolo per errore.
- [ ] CONTINUA -> Capitolo 2: dialoghi, scontro con Ran, ingredienti e secondo cocktail.
- [ ] CONTINUA -> Capitolo 3: Gunnar, stele, Ymir, Lacrima e terzo cocktail.
- [ ] Verifica la schermata conclusiva e il ritorno alla forgia.
- [ ] Dal menu carica uno slot H2: inventario, stanza e progressi devono coincidere.

**Errori e interfaccia**
- [ ] Perdi tre cuori contro un boss e premi RIPROVA: niente soft-lock.
- [ ] Sbaglia una ricetta e premi RIPROVA LA RICETTA: ingredienti conservati.
- [ ] Verifica ESC, J, F2/F3/F4 (solo debug), audio e impostazioni.
- [ ] Riapri una partita dopo aver chiuso e riavviato il programma.
- [ ] Controlla crediti e pulsante EXIT GAME.

## Materiali ancora da controllare
- [ ] **cocktail_1.png e cocktail_2.png**: non risultano nelle risorse del branch
      base. Le schermate attuali hanno fallback testuale: per rifinirle servono
      illustrazioni corrette, non semplicemente copiare immagini di altri cocktail.
- [ ] **Credits**: l'interfaccia contiene ancora un testo provvisorio.
      Verificare autori, licenze delle immagini ed eventuali attribuzioni.
- [ ] Una descrizione breve per la presentazione: obiettivo, tre mondi,
      inventario, boss, calderoni, architettura MVC, salvataggi H2.
- [ ] Acquisire 5–8 screenshot del gioco funzionante.

## Domani 3 novembre
- [ ] Risolvere soltanto bug riproducibili e difetti che impediscono il percorso.
- [ ] Controllare tutti i file PNG/TXT e i crediti.
- [ ] Rieseguire il percorso essenziale e `mvn clean package`.
- [ ] Preparare ZIP pulito del repository con sorgenti e risorse
      (escludendo `target/` e le partite personali in `saves/`).
- [ ] Verificare il funzionamento dello ZIP in una cartella nuova.

## 4 novembre — consegna
- [ ] Non fare refactoring dell'ultimo minuto.
- [ ] Presentare il branch stabile, eventuale JAR/istruzioni, README e screenshot.
- [ ] Conservare separato il database delle partite personali.

## Comandi Git utili
```powershell
git status
git fetch origin
git switch --track origin/release/consegna-2026-11-04
mvn clean test
mvn exec:java

git diff origin/refactor/state-and-architecture...HEAD
git log --oneline origin/refactor/state-and-architecture..HEAD
git show HEAD
```

Se il branch esiste gia' in locale: `git switch release/consegna-2026-11-04`
e `git pull --ff-only origin release/consegna-2026-11-04`.
