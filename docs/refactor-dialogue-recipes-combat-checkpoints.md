# Refactoring completo: dialoghi, ricette, boss, finestre e prologo

Branch di lavoro: `refactor/view-model-cleanup`. Il branch giocabile
`feat/chapter3` e' lasciato invariato finche' non sara' approvata l'integrazione.

## 1. Dialoghi
- `DialogueManager.connect` gestisce mouse/rivelazione/avanzamento con
  `SharedDialogueBox` in tutti e tre i capitoli e nel prologo.
- `DialogueManager.bindAdvance` centralizza SPAZIO e INVIO in C1, C2 e prologo.
  Il Capitolo 3 usa lo stesso `connect`, ma mantiene un unico dispatch
  per Gunnar o NPC per evitare sovrapposizioni.
- Gunnar: primo TXT completo, secondo TXT (`ch3_gunnar_reward.txt`)
  concede la conoscenza, interazioni successive (`ch3_gunnar_repeat.txt`).
  Le interruzioni (ESC/cambio stanza) non assegnano nulla.
- Il flag `gunnarWisdomLearned` resta persistito in H2.
  Il primo ascolto (`gunnarFirstDialogueHeard`) e' solo di sessione:
  se si ricarica la partita dopo il primo ascolto ma prima del secondo,
  va riascoltato il primo discorso. Nessuna migrazione DB viene eseguita.

## 2. Calderone
- `RecipeManager` raccoglie le TRE ricette corrette per capitolo e i cinque
  ingredienti selezionabili di ogni regno, comprese le esche.
- **Tutti e tre i capitoli** aprono la stessa finestra modale
  `CauldronOverlay`. `YmirCauldronPanel` e `YmirCauldronRecipe`
  sono stati rimossi; `RecipeManager` definisce ora anche gli ingredienti
  corretti e le esche di Ymir. La Forgia resta lo scenario sottostante.
- Il calderone non si apre senza 3 ingredienti del capitolo.
- Mix incompleto = messaggio; mix esatto = successivo capitolo;
  qualsiasi ingrediente errato in un mix completo = Game Over.
- `CauldronController` e `GameOverPanel` condividono la regola del retry.
  Dopo RIPROVA LA RICETTA il gioco ripristina tutti e 3 i cuori,
  conserva l'inventario, torna alla stanza del calderone e lo riapre.
  Le ricompense non vengono assegnate per un mix errato.
- Nessuno di questi passi riscrive gli slot H2 o il modello Item.

## 3. Combattimenti
- `CombatManager.takeHit`: danno, audio, condizione a 0 cuori,
  motivo del game over e checkpoint del boss.
- `CombatManager.victory(chapter)`: capitoli 1 e 2 segnano
  bossDefeated, capitolo 3 segna ymirPacified, tutti azzerano retry.
- Le tre UI di combattimento conservano meccaniche distinte:
  sequenze WASD, scelta delle frasi di Rán, rune/piattaforme di Ymir.
- I premi narrativi restano distinti: la Lacrima di Ymir va raccolta
  dopo la vittoria, gli ingredienti dei primi due boss vengono assegnati subito.

## 4. Finestre
- `StyledEventDialog` e' stato incorporato in `StoryPromptDialog` e rimosso.
  Lo stesso pannello disegna messaggi, conferme e ricompense illustrate.
- `StoryPromptDialog.Theme.EARTH` completa SKY e OCEAN.
- Le grandi schermate illustrate di `YmirStoryPrompt` sono state
  sostituite con `StoryPromptDialog` tema EARTH: anche conferma pre-boss
  e requisiti mancanti usano la cornice compatta degli altri capitoli.
- L'animazione della crepa resta in `EarthBreathEffect` e non e'
  associata ai vecchi popup illustrati.

## 5. Pulizia
- Prologo rinominato `ProloguePanel`, senza aggiungere un `chapter=0`
  ai salvataggi. START GAME avvia ancora il dialogo del prologo;
  LOAD GAME continua ad aprire il capitolo salvato.
- `TavernIntroPanel` eliminato dopo aggiornamento dei consumer.
- Test per ricette, salute, checkpoint del calderone, Gunnar,
  caricamento portrait e hotspot; nessun cambio dello schema H2.


## 6. Completamento condiviso dei tre capitoli
- `ChapterCompletionPanel` sostituisce `ChapterTransitionPanel`,
  `Chapter3PreviewPanel` e `Chapter3CompletePanel`.
- I Capitoli I, II e III usano la stessa schermata scura/oro, titolo,
  illustrazione del drink, testo e tre pulsanti SALVA / PROSEGUI / MENU.
- Le immagini opzionali sono `/images/items/cocktail_1.png`,
  `cocktail_2.png` e `cocktail_3.png`. Per i primi due
  viene mostrato il nome del cocktail fino all'arrivo dei PNG.
- La ricetta esatta porta direttamente alla schermata di completamento;
  il vecchio popup illustrato `ItemPickupOverlay` e' stato eliminato.
- Salvando sulla schermata del Capitolo I/II si registra il prossimo
  capitolo con stanza iniziale 1, cosi' il caricamento non riapre
  un calderone privo di oggetti.
- Il Capitolo III conserva TORNA ALLA FORGIA e il flag di cocktail creato.
- I checkpoint di fallimento e le ricompense dei boss non cambiano.

## Controlli manuali dopo i test
1. Da START GAME (nuova partita), il prologo deve essere skippabile.
2. Capitolo 3, Gunnar: primo dialogo senza ricompensa, secondo con
   ricompensa; nessuna ricompensa se premi ESC.
3. I calderoni si aprono soltanto con almeno 3 oggetti del capitolo.
4. Una miscela con almeno 1 ingrediente errato da' Game Over.
   RIPROVA LA RICETTA deve riaprire il calderone con gli stessi ingredienti.
5. Sconfiggi e perdi contro i tre boss: 3 cuori -> Game Over; retry boss;
   ingredienti e flag non devono sbloccarsi prematuramente.
6. L'ingresso nello scontro di Ymir usa ora il prompt compatto Terra,
   mentre la crepa del mondo continua a usare EarthBreathEffect.
7. I tre cocktail corretti aprono le schermate di completamento comuni;
   SALVA memorizza il capitolo successivo in C1/C2.
8. L'assenza di cocktail_1.png o cocktail_2.png mostra un nome leggibile,
   senza crash o immagine inventata.

### Salvataggi locali
Non eseguire `git reset --hard`, `git clean` o `git add .`.
I file tracciati `saves/*.db` e `target/` possono essere modificati
localmente da VS Code/Maven. Il branch non li tocca.
