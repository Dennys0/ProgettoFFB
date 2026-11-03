# Identita' grafica condivisa - The Alchemist's Hangover

## Dialoghi

La grafica ufficiale per tutti i capitoli e' \`/images/ui/dialogue_box.png\`.

- \`SharedDialogueBox\` si occupa della cornice, del nome del personaggio,
  del testo e del collegamento visuale "MOSTRA FRASE".
- \`DialogueSystem\` controlla le battute, l'effetto macchina da scrivere e
  le callback narrative. Non dipende da un capitolo.
- \`TavernIntroPanel\`, \`Chapter1Panel\`, \`Chapter2Panel\` e il dialogo di
  Gunnar in \`Chapter3Panel\` riusano lo stesso elemento visuale.
- **Gunnar** resta disegnato nello sfondo della stanza 2; cliccando sulla sua
  figura seduta compare il ritratto \`gunnar_realms.png\` sopra lo scenario,
  insieme alla cornice cinematografica. Il pulsante "INTERROMPI" nega
  l'indizio; completare il dialogo lo registra nel Diario.
- Il contenuto e' diverso da una scena all'altra, ma la grafica resta uguale.

## Calderoni

La grafica ufficiale e' \`/images/ui/cauldron_bg.png\`.

- \`SharedCauldronArtworkPanel\` ne disegna la stessa illustrazione in
  tutte le postazioni; non contiene la logica delle ricette.
- \`CauldronOverlay\` la riusa per i capitoli 1 e 2.
- \`YmirCauldronPanel\` riusa lo stesso disegno, ma resta un componente
  **dentro la stanza 3** e mantiene i risultati speciali della Terra.
- Le regole dell'Old Fashioned restano isolate in
  \`YmirCauldronRecipe\`.

## Collaudo

1. \`mvn clean test\`
2. \`mvn exec:java\`
3. Controlla la cornice in INTRO, in un dialogo del capitolo 1 e nel 2.
4. Premi F3, vai con F4 alla stanza 2 e clicca Gunnar: compare il ritratto,
   la **stessa cornice illustrata** degli altri capitoli e il dialogo
   interagibile sopra lo sfondo.
5. Prova il calderone dei capitoli 1, 2 e 3. Nel capitolo 3 il click
   avviene direttamente sul boccale, sopra lo scenario \`room3_forge.png\`.

I refactor non modificano il database H2 e non toccano \`saves/\` o \`target/\`.
