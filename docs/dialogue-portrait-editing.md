# Ritratti e dialoghi: revisione narrativa

Queste modifiche intervengono sul Capitolo 2 senza cambiare
hotspot, percorsi delle stanze o regole del boss Rán.

## Posizione dei ritratti durante il dialogo

Il pannello del ritratto usa coordinate logiche **1280×720**:

- **Marmæl**: x=45, y=30, larghezza=550, altezza=620 (sinistra)
- **Sgherro**: x=55, y=30, larghezza=540, altezza=620 (sinistra)
- **Jörmungandr**: x=200, y=30, larghezza=880, altezza=620 (centrato)
- Altri personaggi: x=340, y=30, larghezza=600, altezza=620

Il metodo \`Chapter2Panel.portraitBoundsFor(speaker)\` determina
la posizione del ritratto per il parlante corrente; NON sposta gli
hotspot trasparenti che identificano gli NPC nello scenario.

L'immagine del ritratto resta sullo sfondo della cornice dialogo
\`SharedDialogueBox\`, che rimane al suo posto.

## Pulizia dei dialoghi

Eliminate tutte le 13 intestazioni in forma
\`SCENA <numero> — <titolo>.\` dalle battute del NARRATOR
nei file di dialogo dei capitoli e dell'introduzione.
Le descrizioni degli ambienti e delle azioni restano intatte.

Sono state ammorbidite **35 espressioni di Arthur**, principalmente
nei sei nuovi dialoghi del Capitolo 2, e alcune nei dialoghi del
Capitolo 1 per continuita' di tono.

Il protagonista resta ironico, sarcastico e insofferente; non sono
cambiate le conseguenze narrative, il significato delle battute,
il numero delle linee o le parole degli altri personaggi.

Due test di regressione garantiscono che la numerazione delle scene
non venga recitata e che le conversazioni del Capitolo 2 conservino
il numero originale di battute.

## Verifica

\`\`\`powershell
git fetch origin
git merge --no-edit origin/feat/chapter3
mvn clean test
mvn exec:java
\`\`\`

Per il Capitolo 2: ascolta Marmæl nella stanza 1, lo sgherro
nella stanza 3 e Jörmungandr nella stanza 5.
Controlla che il ritratto occupi la posizione desiderata
e che la prima battuta del NARRATOR inizi direttamente con
la descrizione dell'ambiente.
