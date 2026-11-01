# Architettura di Alchemist Hangover

Il progetto usa Java Swing e un'architettura MVC pragmatica:
le **regole** devono poter essere testate senza immagini o finestre;
la **view** disegna componenti, scene e animazioni; il **controller**
coordina input, audio e navigazione; il **DAO** salva e rilegge H2.

## Mappa dei package

```text
com.alchemist.hangover
├── Main.java                       Avvio Java/Swing
├── model/                          Dati e regole di base
│   ├── GameState.java              Stato corrente della partita
│   ├── GameSaveSnapshot.java       Copia immutabile dei dati salvabili
│   ├── Item.java                   Oggetto dell'inventario
│   ├── RecipeManager.java          Ricette dei capitoli
│   └── YmirRuneRitual.java         Sequenza e regole delle rune
├── gameplay/                       Logica del mondo senza Swing
│   ├── boss/
│   │   ├── BossRules.java          Danno, vittoria, checkpoint
│   │   └── BossRewardCatalog.java  Premi dei boss
│   ├── cauldron/
│   │   └── CauldronGameService.java Verifica e conseguenze delle ricette
│   └── chapter/
│       ├── ChapterProgression.java Transizioni e save di fine capitolo
│       └── EarthChapterRules.java  Indizi, dialoghi e progresso di Gunnar
├── controller/                     Coordinamento delle azioni
│   ├── CombatManager.java          Regole boss + scena/audio di risposta
│   ├── DialogueSystem.java         Lettura e avanzamento dialoghi
│   ├── SceneManager.java           Cambio schermata
│   └── AudioManager.java           Effetti e atmosfera
├── view/                           Swing: componenti condivisi e mondi
│   ├── AbstractChapterPanel.java   Navigazione e interazioni condivise
│   ├── Chapter1Panel.java          Vascello
│   ├── Chapter2Panel.java          Mare
│   ├── Chapter3Panel.java          Terra
│   ├── ChapterLayouts.java         Percorsi PNG e stanze dei tre mondi
│   ├── DialogueManager.java        Sessione dialoghi condivisa
│   ├── boss/                       Tre minigiochi grafici distinti
│   │   ├── BossFightOverlay.java
│   │   ├── RanStrategyBossOverlay.java
│   │   └── YmirBossOverlay.java
│   └── cauldron/                   Finestra e presentazione del calderone
│       ├── CauldronController.java
│       ├── CauldronOverlay.java
│       └── SharedCauldronArtworkPanel.java
├── dao/                            Accesso ai dati
│   ├── SaveGameDAO.java            Slot e inventario
│   └── ItemDAO.java                Catalogo oggetti
└── database/                       Configurazione H2
    └── DatabaseManager.java
```

Le schermate dei capitoli rimangono nel package `view` perche' condividono
molti componenti Swing. La logica di progressione e' gia' separata
nel package `gameplay.chapter`: non spostare file soltanto per creare
cartelle aggiuntive.

## Dove inserire una nuova classe

- **Nuovo boss:** meccanica grafica in `view.boss`;
  regole condivise in `gameplay.boss`; ricompensa nel catalogo.
- **Nuovo calderone:** conserva `view.cauldron.CauldronOverlay`,
  aggiungi ricette a `RecipeManager`, regole comuni in `gameplay.cauldron`.
- **Nuovo capitolo:** descrivi prima stanze e sfondi in `ChapterLayouts`;
  crea un pannello che estende `AbstractChapterPanel`;
  inserisci solo la logica specifica nel modulo `gameplay.chapter`.
- **Nuova conversazione:** usa `DialogueManager.SceneSession`;
  i contenuti narrativi rimangono nei file TXT di `resources/dialogues`.
- **Nuovo campo persistente:** aggiungilo al modello e allo snapshot,
  crea una migrazione `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` in
  `src/main/resources/db/schema.sql`, aggiorna `SaveGameDAO`
  e aggiungi un test H2 in memoria.

## Salvataggi: regole importanti

1. `GameSaveSnapshot` e' immutabile e non modifica `GameState`.
2. `ChapterProgression.saveAfterCompletion` prepara il **prossimo**
   capitolo senza avanzare quello giocato: annullare SALVA non cambia nulla.
3. `ChapterProgression.advanceToNextChapter` viene richiamato solo
   quando il giocatore decide di continuare.
4. `SaveGameDAO.loadGame` legge prima i dati dello slot e tutto
   l'inventario, poi applica `GameState.restoreFrom`.
5. Le migrazioni H2 devono conservare gli slot reali. I test usano sempre
   `jdbc:h2:mem:` e non accedono ai file `saves/alchemist.mv.db`.

## Come verificare il refactoring

```powershell
git fetch origin
git switch refactor/state-and-architecture
git pull --ff-only origin refactor/state-and-architecture

mvn clean test

# Solo modifiche di questo ciclo
git diff origin/refactor/gameplay-logic-cleanup...HEAD

# Tutto il refactoring rispetto alla versione principale
git diff origin/master...HEAD

# Ultimi commit, dal piu' recente
git log --oneline --graph -12
```

Non integrare automaticamente in `master`: prima verificare i test
GitHub Actions e giocare manualmente i tre capitoli (dialoghi, boss,
retry, calderoni, salvataggio e caricamento).
