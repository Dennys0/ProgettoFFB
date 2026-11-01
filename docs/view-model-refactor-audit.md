# Revisione completa dei package view e model

> **Aggiornamento 1 novembre 2026:** ulteriore refactoring su `GameState`,
> `ProloguePanel`, `Chapter1Panel`, `Chapter2Panel` e sui layout di tutte le
> stanze. Il vecchio `Chapter3World` è stato eliminato a favore di
> `ChapterLayouts`, comune a C1/C2/C3. La scheda che segue resta una
> fotografia dell'analisi storica: le modifiche nuove sono nel
> [rapporto per la revisione universitaria](university-code-cleanup.md).
>

> **Nota di aggiornamento:** questo rapporto fotografa l'analisi iniziale dei 41 file
> prima dei refactoring successivi. Nella versione attuale del branch
> `refactor/view-model-cleanup`, i componenti specifici
> `YmirCauldronPanel`, `YmirCauldronRecipe`, `YmirStoryPrompt`,
> `ItemPickupOverlay`, `StyledEventDialog`,
> `ChapterTransitionPanel`, `Chapter3PreviewPanel`,
> `Chapter3CompletePanel` e `TavernIntroPanel` sono stati
> sostituiti da componenti comuni; vedere
> [architettura finale](chapter-completions-unified.md).
>

**Base verificata**: commit `d26c71927094eee0a6d357e405e81c74e80db1f5` del branch `feat/chapter3`.
**Branch di lavoro**: `refactor/view-model-cleanup`.
**Ambito**: 37 classi view + 4 model = **41 file Java**. La verifica di riferimenti e conseguenze
coinvolge anche `SaveGameDAO`, `ItemDAO`, `SceneManager` e test esistenti senza riscriverli.

## Risultati: model (4 file)

| File | Stato | Risultato della revisione |
|---|---|---|
| `GameState.java` | Conservare per ora | Singleton; salute osservabile; flag capitoli e inventario persistiti in H2. Rischio: getInventory() espone lista mutabile; setInventory() conserva alias. |
| `Item.java` | Pulito | Quattro campi ora final; ID/nome/descrizione/immagine restano invariati nel DAO e nei salvataggi. |
| `YmirCauldronRecipe.java` | Conservare | Valida 3 ingredienti e due esche; logica separata dalla UI correttamente. |
| `YmirRuneRitual.java` | Conservare | Ordine delle rune e zone deterministico da seed di partita; non fondere con boss, stele o stato. |

## Risultati: view (37 file)

| File | Stato | Risultato della revisione |
|---|---|---|
| `AbstractChapterPanel.java` | Rifattorizzato | Registra NPC di ogni capitolo; contiene gia' mappa, frecce, oggetti e atmosfera comuni. |
| `BossFightOverlay.java` | Pulito; possibile fase 2 | Rimossi campo HUD non utilizzato e wrapper; specializzazione WASD da preservare. Ricompensa/sconfitta ripetute. |
| `CauldronOverlay.java` | Pulito; da valutare | Ricette C1 e C2 in una finestra; contiene sia UI sia condizioni di reset/game over. Estrarre regole solo con test delle conseguenze. |
| `Chapter1Panel.java` | Rifattorizzato | Hotspot NPC e loader ritratti comuni; tasti, Martino, boss e aree di scena restano specifici. |
| `Chapter2Panel.java` | Rifattorizzato | Hotspot NPC e ritratti comuni; rimossa variabile journalHotspot inutilizzata; boss Rán distinto. |
| `Chapter3CompletePanel.java` | Mantenere | Epilogo e opzioni salvataggio/rientro. Somiglianza grafica con transizioni, ma logica specifica. |
| `Chapter3Panel.java` | Rifattorizzato; fase 2 | Riusa registry NPC; coordina Gunnar, NPC, stele, forno, boss. Doppio controller dialoghi da consolidare dopo verifica finale. |
| `Chapter3PreviewPanel.java` | Mantenere | Transizione C2 -> C3 con inizializzazione capitolo, reset salute e seed rune. |
| `Chapter3World.java` | Mantenere | Dati delle 7 stanze e percorsi, testati in autonomia; corretto separarli dalla UI. |
| `ChapterTransitionPanel.java` | Pulito | Titolo/testo/pulsante ora locali; transizione C1 -> C2 con reset del flag boss condiviso. |
| `DialogueChoiceOverlay.java` | Mantenere per ora | Donazione Mappa del Cielo + Chiave del Drago; mutazione del GameState in view: candidata a servizio dedicato. |
| `EarthBreathEffect.java` | Mantenere | Animazione temporanea attivata dalla crepa, diversa dal pulviscolo ambientale continuo. |
| `GameJournalDialog.java` | Mantenere | Contenuti derivati da GameState senza campi nel DB; dipende dagli indizi del terzo capitolo. |
| `GameOverPanel.java` | Mantenere; fase 2 | Retry differenziato tra boss e calderone; smistamento capitoli duplicato in SaveLoadDialog. |
| `GameSettingsDialog.java` | Mantenere | Finestra minima per impostazioni audio/transizioni. |
| `HUDPanel.java` | Pulito | Bottone inventario ora variabile locale; HealthDisplayPanel necessario per aggiornamenti live. |
| `HealthDisplayPanel.java` | Mantenere | Listener registrato/rimosso con ciclo di vita Swing; evitare fusioni con HUD. |
| `HotspotButton.java` | Rifattorizzato nei chiamanti | Configura gia' cursore, trasparenza e hover; eliminate 60 righe duplicate tra chiamanti. |
| `InventoryOverlay.java` | Mantenere | Inventario verticale e origine oggetti; mantiene cache immagini e dettagli senza nuove colonne nel DB. |
| `ItemOriginCatalog.java` | Mantenere | Mappa centralizzata degli scenari di ritrovamento. Spostare package richiederebbe aggiornare test e consumer. |
| `ItemPickupOverlay.java` | Mantenere per ora | Ancora chiamato dalle ricette dei Capitoli I e II: non e' codice morto. |
| `MainFrame.java` | Mantenere | Registrazione scene e scorciatoie F2/F3/F4, usate per test rapidi; non rimuovere. |
| `MainMenuPanel.java` | Pulito | Rimosso import java.io.File; tasti e azioni ancora separati. |
| `PauseMenuDialog.java` | Mantenere | ESC e azioni pausa/salva/diario/impostazioni; non equivale a MainMenuPanel. |
| `RanStrategyBossOverlay.java` | Pulito; fase 2 | Costanti di boss ora static; prova narrativa a scelta distinta da WASD e rune. |
| `RoomAtmosphereEffect.java` | Mantenere | Particelle click-through comuni, timer disattivato quando il pannello e' nascosto. |
| `RuneSteleDialog.java` | Mantenere | Legge sequenza persistita e segna stele come letta; non e' una semplice finestra statica. |
| `SaveLoadDialog.java` | Pulito; fase 2 | Rimossa reference inutilizzata al frame; tocca dati H2, dispatch capitoli e slot: richiede regressioni dedicate. |
| `SharedCauldronArtworkPanel.java` | Mantenere | Artwork comune a CauldronOverlay e YmirCauldronPanel; separazione grafica gia' corretta. |
| `SharedDialogueBox.java` | Rifattorizzato | Cornice, etichette e nuovo loader unico dei ritratti; futuro host per un controller comune. |
| `StoryPromptDialog.java` | Mantenere | Stile grafico unico per richieste e messaggi C1 e C2. |
| `StyledEventDialog.java` | Da valutare | info/confirm delegano al prompt base, ma reward aggiunge immagine. Una fusione richiede aggiornare consumer e test. |
| `TavernIntroPanel.java` | Rifattorizzato | Caricamento portrait condiviso, etichette locali; SKIP intro differisce da mostra frase. |
| `ToastNotification.java` | Mantenere | Notifica non modale; non sostituisce i due popup di ricetta presenti in ItemPickupOverlay. |
| `YmirBossOverlay.java` | Mantenere | Puzzle posizionamento+rune: non equivale ai due boss precedenti. |
| `YmirCauldronPanel.java` | Pulito | Calderone embedded nel fondale, mantiene la ricetta in YmirCauldronRecipe. |
| `YmirStoryPrompt.java` | Da valutare | Usa PNG con testo/bottoni incorporati e crop. Non fondere con StoryPromptDialog senza ridisegno asset. |

## Cambiamenti a basso rischio applicati

1. `Item`: i quattro attributi sono final; nessun cambiamento all'API o allo schema H2.
2. Rimozione di campi non necessari: `journalHotspot` (C2), `hudPanel` (boss C1),
   `inventoryButton` come campo (HUD), `parentFrame` (SaveLoadDialog), campi
   title/story/continue (ChapterTransitionPanel), etichette create una sola volta nei dialoghi.
3. Cancellazione di import inutili e riuso di costanti statiche nei boss.
4. La registrazione e visibilita' degli hotspot NPC dei tre capitoli e' ora
   condivisa in `AbstractChapterPanel`, non ci sono piu' tre mappe e tre loop duplicati.
5. Il caricamento dei ritratti (altezza 520/550) e' centralizzato in `SharedDialogueBox`.
6. `HotspotButton` configura gia' trasparenza, bordo, focus e cursore;
   cancellate **60 righe ridondanti** nei chiamanti, senza cambiare i bounds.
7. Inseriti test automatici del registro NPC e dei portrait, oltre ai 52 test preesistenti.

## Proposte da discutere prima della seconda fase

### A. Estrarre controller dialoghi comune per C1/C2/C3

**Pro**: ridurre gran parte di `initDialogueUI`, `startSceneDialogue`, callback
e gestione ritratto ripetuti; maggiore uniformita' nella navigazione.
**Rischio**: Gunnar sblocca la saggezza solo completando tutta la conversazione;
Martino apre la donazione; due boss iniziano solo dopo la loro intro;
interrompere NPC ordinari non deve attivare boss o cambiare lo stato.
Richiede nuovi test per skip/interruzione/cambio stanza. **Decisione richiesta**.

### B. Estrarre un servizio condiviso di risultato boss

**Pro**: unificare takeDamage/checkpoint/GameOver/ricompense e relativi dialoghi.
**Rischio**: C1 usa sequenza WASD, C2 risposte, C3 rune + drop raccoglibile;
la lacrima NON e' assegnata direttamente alla vittoria, a differenza C1/C2.
Non unire `BossFightOverlay`, `RanStrategyBossOverlay`, `YmirBossOverlay` in un'unica
classe monolitica. **Decisione richiesta**.

### C. Separare logica delle ricette da CauldronOverlay (C1/C2)

**Pro**: rendere testabili mix, ingredienti esca e condizioni di vittoria.
**Rischio**: le ricette C1/C2 condividono l'inventario e alcune scelte
lo svuotano, resettano il capitolo o tornano al menu. Necessario stabilire
quali conseguenze debbano restare identiche. `YmirCauldronRecipe` resta autonoma.
**Decisione richiesta**.

### D. Riordinare GameState e i flag dei boss

**Pro**: flag per capitolo e inventario protetto da mutazioni accidentali.
**Rischio alto**: `bossDefeated` e' riutilizzato tra C1 e C2, reset durante
la transizione; `getInventory()` restituisce lista mutabile e le ricette
chiamano `clear()` direttamente; ogni modifica potrebbe toccare `SaveGameDAO`,
gli slot H2 e i checkpoint di sessione. **Non applicato; decisione richiesta**.

### E. Consolidare StoryPromptDialog / StyledEventDialog / YmirStoryPrompt

**Pro**: meno classe/i e meno ripetizione grafica.
**Rischio**: Ymir usa immagini con scritte e pulsanti incorporati: forzare
una fusione senza asset nuovi cambierebbe layout/crop. `ItemPickupOverlay`
e' ancora usato dalle ricette C1/C2. **Da valutare sul risultato visivo**.

## Interventi non consigliati

- Non fondere `Item` e `GameState`: entita' e stato della partita hanno responsabilita' distinte.
- Non fondere `YmirRuneRitual` e `YmirCauldronRecipe`: sono logiche indipendenti e testate.
- Non eliminare `ItemPickupOverlay` prima di aver migrato i due punti di chiamata.
- Non spostare `ItemOriginCatalog` senza coordinare inventario e test.
- Non rimuovere F2/F3/F4: restano strumenti richiesti per provare i capitoli.
- Non eliminare le scene di transizione o cambiare il comportamento di salvataggio H2.

## Verifica consigliata in locale

Con il gioco chiuso e dopo aver controllato `git status --short`:

```powershell
git fetch origin
git switch refactor/view-model-cleanup
mvn clean test
mvn exec:java
```

Se il checkout e' bloccato dai file tracciati `saves/` o `target/`, non
forzarlo: fare prima backup dei salvataggi e chiedere un piano di protezione.

I commit di questo branch **non modificano `saves/`, `target/`, immagini o file DAO**.
