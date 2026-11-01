# Interfacce boss e hotspot — Capitoli 1 e 2

Le nuove finestre dei due capitoli seguono lo stile **IL GUARDIANO DELLE
TEMPESTE** gia' presente in `StoryPromptDialog`.

## Hotspot Capitolo 2 (1280 x 720)

| Stanza | NPC | Prima | Dopo |
|---|---|---|---|
| 1 | Marmæl | (815, 320, 175, 200) | (765, 287, 285, 274) |
| 3 | Sgherro | (1090, 116, 170, 160) | (1002, 102, 265, 225) |
| 5 | Jörmungandr | (700, 128, 320, 242) | (365, 135, 540, 345) |

Le dimensioni sono un primo riallineamento basato sulle scene e sullo
screenshot di gioco: si possono perfezionare se il ritaglio grafico
sul monitor reale non coincide ancora.

`HotspotButton` dipinge un solo suggerimento centrato orizzontalmente
nell'area cliccabile. Il tooltip Swing nativo e' stato disabilitato
per evitare la duplicazione di «Parla con ...». La normale API
`getToolTipText()` conserva il testo accessibile.

## Boss: vittoria, sconfitta, ricompensa

`StyledEventDialog` offre un'interfaccia comune per i tre casi:
`confirm`, `info` e `reward`. I primi due riutilizzano
`StoryPromptDialog` direttamente, mentre `reward` aggiunge
lo sprite e la descrizione dell'ingrediente sotto la stessa cornice
tematica. Le immagini mantengono le proporzioni originali.

- **Hræsvelgr (Capitolo 1):** avviso di vittoria, poi ricompensa
  con `/images/items/item_breath.png` e nome Soffio di Hræsvelgr.
- **Rán (Capitolo 2):** avviso di vittoria, poi ricompensa
  con `/images/items/item_foam.png` e nome Schiuma di Ægir.
- Alla sconfitta di Arthur contro uno di questi boss appare
  un avviso nel colore del capitolo, seguito dal Game Over
  che offre il checkpoint con la causa corretta della sconfitta.

I premi vengono assegnati al `GameState` esattamente una volta
(prima che compaia la schermata), rispettando il precedente flusso
di inventario e salvataggi.

## Collaudo

1. Chiudi il gioco, poi esegui `git pull --ff-only origin feat/chapter3`.
2. Esegui `mvn clean test`, quindi `mvn exec:java`.
3. Capitolo 2, stanze 1, 3 e 5: passa il mouse sopra i tre NPC.
   Deve comparire un solo suggerimento senza rettangolo giallo.
4. Completa lo scontro con Hræsvelgr e osserva vittoria + ricompensa.
5. Completa lo scontro con Rán e osserva vittoria + ricompensa.
6. Riprova entrambi gli scontri perdendo tutti i cuori:
   verifica avviso, schermata Game Over e retry.
7. Controlla le icone e i nuovi ingredienti nell'inventario.

Non sono stati modificati asset PNG, database H2 o file nella cartella
`target/`.
