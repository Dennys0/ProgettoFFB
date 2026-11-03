# Capitolo IV — checklist di collaudo (2–4 novembre 2026)

## Verifica rapida

1. Aggiorna il branch `feat/chapter4-muspelheim` e avvia `mvn clean test`.
2. Avvia con `mvn exec:java`, quindi premi **F5** per entrare subito in Muspelheim.
3. Controlla tutte le **7 stanze** e il percorso 1 → 5 → 7.
4. Completa i dialoghi di Muspelsonar (1), Sigrún (2), resti del diavoletto (3), vagabondo (4), figlio di Ivaldi (5) e Hela (6).
5. Clicca lo scudo in stanza 2 e la stele sotto Hela nella stanza 6.
6. Raccogli la Cenere di Fenrir in stanza 4. Verifica i doni del vagabondo (Zolfo), del nano (Magma) e di Hela (Lacrima di Hel).
7. Affronta Surtur sul trono nella stanza 7. Attira la lama sui due sigilli e sul calderone.
8. Controlla che venga assegnata la **Brace di Muspelheim** una sola volta.
9. Nella stanza 3 prova il calderone con Zolfo e Ghiaccio (fallimento + retry);
   poi usa Magma/Cenere/Brace in ordine, con la Brace per ultima.
10. Verifica risveglio di Arthur, ringraziamenti, salvataggio e ritorno al menu.
11. Ricarica il salvataggio finale: deve riaprire l'epilogo.
12. Percorri anche la transizione ordinaria **Capitolo 3 → Capitolo 4**,
    non soltanto il salto di debug F5.

## Risorse da rifinire

- [ ] Illustrazione finale `cocktail_4.png` del Ragnarök Flambé (non ancora presente).
- [ ] Crediti nominativi e attribuzioni degli asset, con nomi verificati.
- [ ] Verifica visiva delle coordinate degli hotspot: i PNG sono stati
      referenziati correttamente, ma le aree di clic vanno provate a schermo.
- [ ] Sound design nel branch dedicato, senza compromettere le build.

## Git

```powershell
git status
git fetch origin
git switch --track origin/feat/chapter4-muspelheim
mvn clean test
mvn exec:java

# Modifiche rispetto al refactoring precedente
git diff origin/refactor/chapter4-foundation...HEAD

# Cronologia del Capitolo IV
git log --oneline origin/refactor/chapter4-foundation..HEAD
```

Se il branch esiste gia' localmente, usa `git switch feat/chapter4-muspelheim`
seguito da `git pull --ff-only origin feat/chapter4-muspelheim`.
Conserva ogni modifica locale prima di cambiare branch.
