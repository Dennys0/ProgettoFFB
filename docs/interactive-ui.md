# Gameplay feedback & illustrated Ymir prompts

## Hover universale (capitoli 1, 2 e 3)

La classe \`HotspotButton\` e' ora il componente unico per interazioni
nel mondo: NPC, oggetti raccoglibili, calderoni, dettagli e frecce di
navigazione.

- A riposo gli hotspot restano invisibili, salvo le immagini vere degli oggetti.
- Al passaggio del mouse: cursore a mano, piccolo bagliore circolare attorno
  al puntatore ed etichetta scura con l'azione (es. **Parla con Gunnar**).
- Niente riquadri gialli: il bagliore NON circonda lo sprite.
- Suggerimento completo dopo circa un secondo, utile quando il testo
  della piccola etichetta deve essere abbreviato.
- \`setHoverHighlightEnabled(true)\` aumenta leggermente la luce per gli
  oggetti raccoglibili, senza tornare alla vecchia cornice.

\`AbstractChapterPanel\` usa lo stesso componente anche per le frecce.
Le interazioni di capitolo 1 e 2 sono state migrate ai nuovi hotspot.
\`YmirCauldronPanel\` lo riutilizza nei pulsanti sovrapposti ai disegni.

## Ymir: schermate illustrate interattive

\`YmirStoryPrompt\` mostra \`/images/ui/ymir_fight.png\` al clic sulla
sagoma della stanza 7, usando la cornice centrale dell'immagine come
interfaccia del gioco.

- **AFFRONTA YMIR** / **NON ANCORA**, entrambi cliccabili.
- **ENTER** conferma, **ESC** annulla; la croce chiude.
- Se mancano entrambi gli indizi: usa \`ymir_warning.png\` e il pulsante
  **OK**.
- Se manca un solo indizio: mostra un messaggio nello stesso stile,
  ma **dinamico**, per non attribuire a Gunnar o alla stele un progresso
  gia' completato.
- Le immagini vengono lette dalle risorse e ritagliate in Java: i PNG
  non vengono modificati. Lo scontro, i cuori e la sequenza delle rune
  restano invariati.

## Collaudo grafico

1. Esegui \`mvn clean test\` e \`mvn exec:java\`.
2. Nel Capitolo 1 verifica Martino, calderone, oggetti e frecce.
3. Nel Capitolo 2 verifica Siegfried, diario, Rán e calderone.
4. Con F3 e F4 entra in Capitolo 3:
   - stanza 1: crepa e battito;
   - stanza 2: Gunnar;
   - stanza 3: boccale;
   - stanze 4-5: quattro ingredienti;
   - stanza 6: stele;
   - stanza 7: sagoma nera di Ymir, scelta e avviso.
5. Verifica che il testo emerga al passaggio del mouse senza un bordo
   rettangolare attorno all'oggetto o al personaggio.
6. Prova il salvataggio, riavvio e caricamento prima del boss.

## Igiene Git (IMPORTANTISSIMO)

I file \`saves/\` e \`target/\` erano gia' tracciati da Git prima che
venissero esclusi in \`.gitignore\`. Per questo Source Control li mostra
come modificati. Non usare \`reset --hard\` o \`clean -fd\` per risolvere.

La rimozione dall'indice Git va fatta **solo dopo aver chiuso il gioco,
copiato la cartella saves in un backup verificato** e pianificato il
passaggio sugli altri computer che usano il repository. La rimozione
dell'indice eseguita in un commit remoto puo' altrimenti cancellare
file tracciati durante un successivo checkout/pull in copie pulite.

Il refactoring UI non ha toccato nessun file di salvataggio o di build.
