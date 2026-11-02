# Capitolo IV — collaudo interattivo: NPC e boss Surtur

Branch: `fix/ch4-gameplay-polish` (non fare merge su `master` ora).

## 1. Controllare le hitbox

1. Avvia il gioco con `mvn exec:java`.
2. Premi **F5** per entrare nel Capitolo 4. **Attenzione:** F5 azzera
   la partita attiva; usa la scorciatoia soltanto per test.
3. Premi **F6**: compaiono i rettangoli verdi degli hotspot, con **x, y,
   larghezza, altezza** e nome dell'interazione.
4. Premi **F7** per selezionare qualunque stanza (1–7) senza ripercorrere
   la mappa.
5. Verifica a schermo ogni area cliccabile. Gli NPC sono in stanza 1
   (Muspelsonar), 2 (Sigrún), 3 (diavoletto), 4 (vagabondo) e 5
   (figlio di Ivaldi). Controlla anche ingredienti, calderone e stele.
6. Se una hitbox non combacia con la persona/oggetto, annota **stanza,
   personaggio, x, y, larghezza e altezza desiderati** e uno screenshot
   con F6 attivo. Potremo correggere le coordinate senza indovinarle.
7. Premi nuovamente F6 per nascondere i rettangoli prima di giocare.

**Nota:** la modalità F6 serve per la calibrazione e rimane disattivata
all'avvio; non altera i salvataggi e non cambia i file PNG.

## 2. Il duello nella fornace

Muovi Arthur con **A/S/D** o cliccando la corsia. **Esc** chiude il duello.
Le fratture colpiscono allo scadere del timer. Superata l’ondata, Surtur
inizia a seguire Arthur con la spada. Posizionati sul segno dorato. Quando
la mira si blocca, cambia corsia prima dell’impatto. Se schivi in anticipo,
la lama segue Arthur e manca il sigillo. Se rimani fermo, perdi un cuore.

Spezza entrambi i sigilli laterali e ripeti la manovra sul calderone centrale.
Ogni sigillo infranto aggiunge un’ondata e riduce il tempo di schivata.

## 3. Verifiche essenziali prima della consegna

- [ ] F6 mostra ogni NPC nella corretta posizione, senza coprire le frecce.
- [ ] Tutti i dialoghi si aprono e si chiudono correttamente.
- [ ] I tasti A/S/D spostano Arthur; il timer continua a scorrere.
- [ ] Le corsie rosse producono un danno al momento dell'impatto.
- [ ] La mira segue Arthur fino al blocco della traiettoria.
- [ ] Un colpo a vuoto lascia intatto il sigillo.
- [ ] Sconfiggendo Surtur viene assegnata una sola Brace.
- [ ] Tre colpi fanno apparire Game Over; RIPROVA riapre il boss.
- [ ] Il calderone accetta Magma, Cenere e Brace per ultima.
- [ ] Il Ragnarök Flambé apre il risveglio e i ringraziamenti.
- [ ] Chiudi e riavvia il programma, poi carica il salvataggio finale.
- [ ] `mvn clean test` è verde prima di integrare nel branch successivo.

## Git — nuovo branch

```powershell
git status
git fetch origin
git switch --track origin/fix/ch4-gameplay-polish
mvn clean test
mvn exec:java

git diff origin/feat/chapter4-muspelheim...HEAD
git log --oneline origin/feat/chapter4-muspelheim..HEAD
git show HEAD
```

Se il branch esiste già in locale:
`git switch fix/ch4-gameplay-polish`
e `git pull --ff-only origin fix/ch4-gameplay-polish`.
Prima di cambiare branch, salva eventuali modifiche locali.
