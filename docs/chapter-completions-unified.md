# Calderoni, Ymir e completamenti — architettura unificata

## Interazione del calderone
`Chapter1Panel`, `Chapter2Panel` e `Chapter3Panel` aprono
**la stessa finestra** `CauldronOverlay` con il medesimo disegno e i
medesimi tre slot. `CauldronController` controlla l'accesso
(almeno 3 ingredienti pertinenti); `RecipeManager` determina la
ricetta in base a `GameState.getChapter()`.

**Esiti:** mix incompleto = avviso; mix errato = notifica fantasy
e Game Over; RIPROVA LA RICETTA = salute piena, stesso inventario
e ritorno al calderone. Mix corretto = `CauldronController.complete`
e schermata di completamento del capitolo. L'illustrazione invadente
`ItemPickupOverlay` non viene piu' utilizzata ed e' stata rimossa.

## Boss Ymir
`Chapter3Panel.triggerYmirEncounter` impiega
`StoryPromptDialog.Theme.EARTH`: prima avviso preciso di indizi mancanti
(Gunnar/stele), poi conferma compatta AFFRONTA YMIR / NON ANCORA.
`YmirStoryPrompt` e' stata eliminata; gli asset immagine originali
rimangono disponibili nelle risorse per futuri riutilizzi.
La **crepa** visibile nella stanza 1 continua a usare
`EarthBreathEffect`, indipendente dai popup.

## Unica schermata per fine capitolo
`ChapterCompletionPanel(JFrame, int chapter)` sostituisce 3 classi.
Le route di `SceneManager` rimangono invariate:

| Capitolo | Scene ID | Cocktail | PNG opzionale | Seconda azione |
|---|---|---|---|---|
| I | `TRANSITION_CH1_CH2` | Asgardian Aviation | `/images/chapter1/items/cocktail_1.png` | ENTRA NEL CAPITOLO II |
| II | `TRANSITION_CH2_CH3` | Maelstrom Martini | `/images/chapter2/items/cocktail_2.png` | ENTRA NEL CAPITOLO III |
| III | `CHAPTER3_COMPLETE` | Ymir's Old Fashioned | `/images/chapter3/items/ymir_old_fashioned.png` | TORNA ALLA FORGIA |

Ogni completamento mostra titolo dorato, immagine/nome del cocktail,
narrazione e pulsanti SALVA PARTITA, PROSEGUI e MENU PRINCIPALE.
Le immagini assenti non producono errori o popup aggiuntivi.

SALVA dal completamento dei Capitoli I/II prepara uno stato di
**inizio capitolo successivo**, compatibile con gli slot H2 esistenti.
Salvando dal Capitolo III si mantiene lo stato finale di Ymir.

## Manutenzione

Rimosse le classi ora duplicate:
- `YmirCauldronPanel`
- `YmirCauldronRecipe`
- `YmirStoryPrompt`
- `ItemPickupOverlay`
- `ChapterTransitionPanel`
- `Chapter3PreviewPanel`
- `Chapter3CompletePanel`

Le immagini PNG e il database H2 non sono stati modificati.

## Prova manuale suggerita

1. Prepara una miscela errata in ciascun capitolo e verifica il checkpoint.
2. Prepara quella corretta e verifica la schermata completa e i tre pulsanti.
3. Salva al termine dei Capitoli I e II; ricarica e controlla il nuovo capitolo.
4. Sconfiggi Ymir e controlla il prompt compatto e il ritorno alla Forgia.
5. Inserisci in seguito `cocktail_1.png` e `cocktail_2.png` nella
   cartella `src/main/resources/images/items/`; saranno mostrati automaticamente.

I test GitHub Actions non sostituiscono la verifica visiva su Windows.
