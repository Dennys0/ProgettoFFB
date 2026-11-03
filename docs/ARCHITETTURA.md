# Architettura di Alchemist Hangover

Il punto di ingresso è `com.alchemist.hangover.Main`. Inizializza H2 e apre la finestra Swing `MainFrame`, che registra le scene e i comandi globali.

## Mappa dei package

| Package | Responsabilità |
| --- | --- |
| `model` | Stato della partita, oggetti e snapshot usato per i salvataggi |
| `database` | Connessione e schema H2, catalogo degli oggetti, lettura e scrittura degli slot |
| `controller` | Cambio scena, audio, impostazioni, avanzamento del testo e coordinamento dei combattimenti |
| `gameplay.chapter` | Progressione fra capitoli e condizioni narrative della Terra |
| `gameplay.boss` | Danni, ricompense, rituale di Ymir e regole del duello con Surtur |
| `gameplay.cauldron` | Ingredienti, ordine di preparazione ed esito delle ricette |
| `view.chapter` | Stanze, uscite, hotspot, effetti ambientali e pannelli dei quattro capitoli |
| `view.boss` | Quattro scontri interattivi e indicatore dei cuori |
| `view.cauldron` | Selezione degli ingredienti e presentazione dell’esito |
| `view.dialogue` | Finestra delle battute, ritratti, scelte e messaggi narrativi |
| `view.inventory` | Barra di esplorazione, inventario, diario e provenienza degli oggetti |
| `view.menu` | Menu iniziale, opzioni, pausa e scelta dello slot |
| `view.story` | Prologo, fine capitolo, game over ed epilogo |
| `view` | Finestra principale e notifiche condivise |

## Dove intervenire

- **Una stanza o un NPC.** I collegamenti fra le stanze sono in `ChapterLayouts`. La posizione dell’NPC e gli eventi del colloquio sono nel relativo `Chapter1Panel`–`Chapter4Panel`. Le battute si trovano in `src/main/resources/dialogues`.
- **Un combattimento.** La finestra porta il nome del boss, ad esempio `HraesvelgrBossOverlay`. Per cambiare il premio assegnato alla vittoria si modifica `BossRules.rewardForChapter`.
- **Una ricetta.** `RecipeManager` definisce gli ingredienti validi. `CauldronGameService` applica l’esito alla partita. `CauldronOverlay` presenta la selezione al giocatore.
- **Un salvataggio.** `GameSaveSnapshot` contiene i dati da conservare. `SaveGameDAO` legge slot e inventario prima di applicarli a `GameState`. Eventuali nuovi campi persistenti richiedono anche una migrazione compatibile in `src/main/resources/db/schema.sql`.

## Verifica

Dalla cartella che contiene `pom.xml`, eseguire `mvn clean test` con JDK 25. I test dei salvataggi usano H2 in memoria. Per verificare le modifiche del branch, usare `git diff --stat origin/fix/ch4-gameplay-polish...HEAD`.
