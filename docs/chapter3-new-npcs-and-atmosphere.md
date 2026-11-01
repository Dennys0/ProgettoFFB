# Capitolo III: NPC, dialoghi ed effetti ambientali

Le quattro nuove scene mantengono i PNG originali; i personaggi non sono disegnati due volte.
Questi sono rettangoli logici da calibrare dopo una prova nel gioco a 1280x720:

| Stanza | NPC | Area cliccabile (x,y,w,h) | Risorsa dialogo |
|---|---|---|---|
| 3 Forgia | Sgherro | 987,363,270,205 | ch3_caldron_revised.txt |
| 4 Foresta | Vanir | 737,293,225,175 | ch3_vanir_revised.txt |
| 5 Miniere | Billingr | 389,289,185,182 | ch3_mining_revised.txt |
| 6 Rupe | Níðhöggr | 713,373,365,178 | ch3_nighodd_revised.txt |

Gli NPC sono visibili negli scenari e si aprono come ritratti soltanto durante il dialogo;
si utilizza SharedDialogueBox con mouse, SPAZIO, INVIO e MOSTRA FRASE.
Il dialogo di Gunnar mantiene la regola della pazienza, mentre il calderone,
la stele e il boss Ymir rimangono interazioni indipendenti.

RoomAtmosphereEffect aggiunge particelle semi-trasparenti non cliccabili.
Il timer viene fermato quando il capitolo e' nascosto. Il fondale non e' modificato.
C1: vento sul ponte e braci nel laboratorio.
C2: minuscole bolle e riflessi negli abissi.
C3: pulviscolo, scintille di fucina e lucciole bioluminescenti nelle rune/foresta.

AudioManager.playRoomCue crea suoni originali di pochi decimi di secondo
solo per stanze selezionate: vento (C1 ponte, prua, boss; C3 rupe),
gocce (C2), scintille (C1 laboratorio, C3 fucina),
rune (C3 cerchio runico) e roccia (C2 fossa; C3 miniere e boss).
Rispettano l'opzione audio nelle impostazioni.

Per verificare: git fetch origin; git merge --ff-only origin/feat/chapter3;
mvn clean test; mvn exec:java.
Non usare git add . e non cancellare saves/ o target/ perche' contengono
file personali/tracciati. Nessuno dei commit ha modificato i salvataggi.

Verificare in particolare che gli hotspot NPC nella Foresta e nelle Miniere
non interferiscano con gli ingredienti raccoglibili.