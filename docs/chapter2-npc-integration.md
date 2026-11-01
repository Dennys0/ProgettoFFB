# Capitolo 2 — sette scenari, sei nuovi personaggi

Questa modifica conserva la mappa e i collegamenti del Capitolo II.
Sono cambiati le immagini degli scenari e gli hotspot dei personaggi.

## Pacchetto asset

Il pacchetto \`Capitolo2_NPC_scenari_dialoghi_pronti.zip\` (fornito in chat)
contiene una cartella \`src/\` da copiare accanto al \`pom.xml\`.

Lo ZIP comprende **7 scenari** in
\`src/main/resources/images/chapter2/\`, **5 sprite**
in \`src/main/resources/images/characters/\` e **6 file di dialogo**
in \`src/main/resources/dialogues/\`.

| Stanza | File scena | NPC/azione | File testo |
|---|---|---|---|
| 1 | room1_hall.png | Marmæl, il profeta | ch2_marmael_dialogue.txt |
| 2 | room2_bar.png | Siegfried al bancone | *siegfried_dialogue_1/2.txt* (gia' esistenti) |
| 3 | room3_sacristy.png | Sgherro, calderone | ch2_sgherro_dialogue.txt |
| 4 | room4_gardens.png | Sirena rossa | ch2_sirena_dialogue.txt |
| 5 | room5_pit.png | Jörmungandr | ch2_jormungandr_dialogue.txt |
| 6 | room6_balcony.png | Capitano scheletro, diario | ch2_captain_dialogue.txt |
| 7 | room7_boss.png | Hrönn e Rán (azioni distinte) | ch2_hronn_dialogue.txt |

In stanza 7 **parlare con Hrönn non avvia lo scontro**. Cliccare sul
vortice e accettare la conferma inizia il dialogo di introduzione
gia' presente \`ran_boss_dialogue.txt\`, poi il boss Rán.

Gli sprite usano le chiavi testuali: MARMÆL, SGHERRO, SIRENA,
JORMUNGANDR, CAPITANO, HRÖNN. Lo Sgherro riutilizza lo sprite del
diavoletto del Capitolo 1; Siegfried e Rán conservano gli sprite precedenti.

## Interfaccia e dialoghi

Ogni NPC e' gia' parte dell'immagine della propria stanza. Un
\`HotspotButton\` trasparente rende il personaggio cliccabile e
mostra bagliore circolare e nome dell'azione al passaggio del mouse.
Gli NPC usano \`SharedDialogueBox\`, l'effetto macchina da scrivere,
il ritratto del parlante e i comandi barra spaziatrice / INVIO.

Le coordinate sono in **1280 x 720**, nel campo
\`Chapter2Panel.NPC_DEFINITIONS\`. Sono valori iniziali da
confermare con screenshot ottenuti nel gioco; non sono ancora
coordinate calibrate in runtime.

Il Capitano menziona un tesoro ma non gli e' stata aggiunta una
ricompensa: per ora e' un elemento narrativo, NON un oggetto inventario.
Il refuso "Dareì" e' stato corretto in "Darei" nel testo dello ZIP.

## Test e importazione

Prima di estrarre, chiudi il gioco e fai una copia degli sfondi che
vuoi conservare. Estrarre lo ZIP sovrascrive i sette PNG dei fondali,
ma non modifica i salvataggi.

Per verificare i sei file di dialogo e i cinque sprite nuovi dopo
l'estrazione:

\`\`\`powershell
mvn -Dalchemist.require.chapter2.assets=true clean test
mvn exec:java
\`\`\`

Prima dell'estrazione, i test CI controllano struttura e regole del
codice ma **non possono ancora verificare i PNG/TXT non committati**.

Per pubblicare su GitHub:

\`\`\`powershell
git add -- src/main/resources/images/chapter2/
git add -- "src/main/resources/images/characters/ch2_*.png"
git add -- "src/main/resources/dialogues/ch2_*.txt"
git diff --cached --stat
git commit -m "assets(chapter2): add underwater NPC scenes sprites and dialogues"
git fetch origin
git merge --no-edit origin/feat/chapter3
git push origin feat/chapter3
\`\`\`

Non eseguire \`git add .\`: i salvataggi H2 e i file compilati sono ancora
tracciati da Git in copie esistenti e non devono essere committati.
Non utilizzare \`reset --hard\` o \`clean -fd\` per sbloccare merge.
