# Capitolo I — nuovi NPC dello Skíðblaðnir

Queste cinque interazioni sono sul branch \`feat/chapter3\` finche' i nuovi
scenari non sono stati provati nel gioco; i salvataggi H2 restano invariati.

## Hotspot per gli NPC

Gli scenari incorporano il personaggio. In Java, l'hotspot e' trasparente e
mostra il consueto feedback al passaggio del mouse; non viene aggiunto uno
sprite fisso sopra al PNG della stanza.

Le coordinate seguenti sono nella finestra di gioco **1280 x 720** e
sono un primo allineamento sulle immagini ricevute: possono essere
perfezionate tramite uno screenshot del gioco, se necessario.

| Room | NPC | Hotspot (x, y, larghezza, altezza) | Dialogo |
|---|---|---|---|
| 1 | Mozzo sulla scopa | 710, 130, 150, 180 | mozzo_dialogue.txt |
| 3 | Grifone tra le nubi | 855, 192, 245, 205 | grifone_dialogue.txt |
| 4 | Diavoletto alla finestra | 120, 218, 195, 220 | diavoletto_dialogue.txt |
| 6 | Huginn sulla balconata | 278, 300, 195, 190 | huginn_dialogue.txt |
| 7 | Veðrfölnir nel cielo | 135, 178, 420, 290 | vedrfolnir_dialogue.txt |

Le aree vengono configurate in \`Chapter1Panel.initHotspots()\` tramite
\`addNpcHotspot()\`; se verranno aggiunti altri NPC, basta una nuova
registrazione con stanza, rettangolo, tooltip e percorso del dialogo.

## Dialoghi e comportamenti

- Tutte le conversazioni utilizzano \`SharedDialogueBox\` e
  \`DialogueSystem\`, compresi click, barra spaziatrice, INVIO e
  "MOSTRA FRASE".
- Mozzo, Grifone e Diavoletto usano i ritratti
  \`/images/characters/*_realms.png\` quando parlano.
- Huginn e Veðrfölnir sono visibili nello sfondo; lo sprite separato
  e' facoltativo per il futuro.
- Il grifone ha un dialogo breve e volutamente enigmatico dedicato.
- Parlare con il falco NON avvia lo scontro. Il giocatore deve scegliere
  separatamente di affrontare Hræsvelgr, e il combattimento parte solo
  dopo il vero \`boss_intro_dialogue.txt\`.
- Martino conserva l'overlay di donazione attuale, da rifinire in seguito.
- Chiudere una stanza mentre e' attivo un dialogo interrompe il testo,
  senza erogare conseguenze narrative.

## Prove consigliate

\`mvn clean test\` e \`mvn exec:java\`.

1. Dal Capitolo I, visita le stanze 1, 3, 4, 6 e 7.
2. Passa il mouse sul personaggio per verificare il suggerimento.
3. Clicca e completa il dialogo, poi verifica che la navigazione
   riprenda normalmente.
4. Nella stanza 4 verifica che il nuovo hotspot del diavoletto
   non nasconda il calderone.
5. Nella stanza 7 parla con Veðrfölnir, poi scegli se avviare
   Hræsvelgr tramite la distinta interazione del boss.
6. Verifica che Martino continui a gestire la donazione come prima.

Nessun file \`saves/\`, \`target/\`, o immagine originale viene cambiato
dall'implementazione Java degli NPC.
