# Capitolo IV — collaudo interattivo: NPC e boss Surtur V2

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

## 2. Surtur V2 — controllo completo

Il vecchio sistema di scelte istantanee è stato sostituito da una vera
sequenza a timer. La finestra mostra lo sfondo della fornace,
la posizione di Arthur, le corsie rosse pericolose e i cuori.

- **A**: vai a sinistra.
- **S**: vai al centro.
- **D**: vai a destra.
- **E**: provoca Surtur *soltanto* al centro nella fase finale.
- **ESC**: abbandona e torna alla stanza.
- Puoi anche **cliccare** sulla corsia per spostare Arthur.

**Fase I:** due fendenti singoli, un solo lato colpito.
**Fase II:** due fendenti doppi, resta una sola corsia sicura.
**Fase III:** due spazzate a doppia ondata: bisogna spostarsi
sulla corsia sicura della prima ondata e poi cambiare lato.
**Finale:** vai al centro, premi E, scappa a sinistra/destra
prima che il timer finisca. La spada di Surtur cade nella fornace
e libera la Brace di Muspelheim.

L'impatto avviene **quando il timer raggiunge zero**: cliccare
una corsia sicura non fa avanzare immediatamente la fase.
Un fendente subito fa perdere un cuore. Dopo la terza ferita
il vecchio Game Over permette il retry dal boss.
La ricompensa non deve duplicarsi.

## 3. Verifiche essenziali prima della consegna

- [ ] F6 mostra ogni NPC nella corretta posizione, senza coprire le frecce.
- [ ] Tutti i dialoghi si aprono e si chiudono correttamente.
- [ ] I tasti A/S/D spostano Arthur; il timer continua a scorrere.
- [ ] Le corsie rosse producono un danno al momento dell'impatto.
- [ ] Le due spazzate obbligano a spostarsi fra due ondate.
- [ ] E non produce la vittoria prima della fase finale.
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
