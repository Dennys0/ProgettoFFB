# Prestazioni e collaudo

## Metodo e limiti

Rilevazioni del 3 novembre 2026 nello stesso ambiente Linux headless, JDK 17.
Baseline: `59c1290`. Il progetto resta configurato per JDK 25 e la CI deve
verificare quella versione. I valori sono campioni esplorativi, non un benchmark
statistico o misure del PC Windows di consegna.

| Operazione | Prima | Dopo |
| --- | ---: | ---: |
| Costruzione Chapter1Panel senza JFrame | 4194,56 ms | 3156,28 ms |
| Primo giro di 7 stanze cap 1 | 1198,57 ms | 1186,29 ms |
| Secondo/terzo giro | 4,95 / 1,40 ms | 2,22 / 4,53 ms |
| 20 richieste della stessa icona a 155 × 155, prima serie | 4426,26 ms | 537,74 ms |
| Stesse richieste, serie successive | 4021,83 / 4748,74 ms | 1,73 / 0,25 ms |
| Primo salvataggio H2 in memoria | 25,81 ms | 25,30 ms |
| Salvataggi successivi | 1,92 / 3,28 ms | 5,43 / 2,56 ms |

Il risultato più netto è l'eliminazione del ridimensionamento ripetuto. Per le
stanze non emerge un miglioramento significativo. Il salvataggio asincrono
migliora la disponibilità dell'EDT, non necessariamente il tempo della query.
Non sono stati misurati avvio completo con JFrame, apertura visibile
 dell'inventario, FPS, audio su dispositivo o salvataggio su disco Windows.

La creazione differita sposta una parte del costo al primo ingresso nella scena;
non elimina quel costo. I successivi accessi riusano il pannello.

## Ripetere le misure

```powershell
mvn test-compile exec:java "-Dexec.mainClass=com.alchemist.hangover.PerformanceProbe" "-Dexec.classpathScope=test" "-Djava.awt.headless=true"
mvn exec:java "-Dalchemist.profile=true"
```

Il probe usa H2 in memoria, costruisce il capitolo 1 senza JFrame e misura un
caso sintetico di icone ripetute. Non rappresenta l'intero inventario.
Conservare almeno tre esecuzioni a freddo e tre a caldo, annotando macchina,
JDK, commit e impostazioni audio. Non confrontare tempi di dispositivi diversi
come se fossero un'accelerazione del programma.

## Verifiche automatiche

Test aggiunti: lettura di snapshot senza modificare la partita corrente;
creazione di una scena una sola volta preservando stanza e checkpoint;
servizio HTTP con invio valido, input invalido, metodo errato e payload eccessivo.
La suite precedente copre ricette, progressione, boss, risorse e salvataggi.

## Collaudo da completare prima di main

- Avvio normale: nessuna scorciatoia debug attiva; audio e menu corretti.
- Avvio debug: F2/F3/F5 cambiano capitolo, F4/F7 selezionano stanze e F6 mostra hitbox.
- Partita completa senza scorciatoie: NPC, ricompense e quattro cocktail.
- Quattro boss: vittoria, sconfitta, riprova e uscita senza timer residui.
- Salva/carica/elimina: feedback visibile, niente doppi comandi; controllare inventario,
  stanza, seed delle rune e finale anche dopo la prima creazione differita del capitolo.
- Inventario e dialoghi: immagini proporzionate e leggibili dopo il nuovo ridimensionamento.
- Audio: cambi rapidi di scena e opzioni senza ritardi, sovrapposizioni o blocchi.
- REST: avviare il servizio, vincere/perdere, leggere il registro; poi spegnere il
  servizio e verificare che il gioco resti utilizzabile.
- Epilogo e nuova partita: nessun riuso dei progressi precedenti.

Il collaudo grafico e sonoro non è sostituito dai test headless. Non eseguire il
merge in main fino alla conclusione di questi controlli.
