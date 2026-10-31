# Capitolo III — Terra: Il Dominio di Ymir

Branch: \`feat/chapter3\`

## Risorse

Gli scenari sono in \`src/main/resources/images/chapter3/room1_hub.png\` ...
\`room7_boss.png\`; gli oggetti sono in \`src/main/resources/images/items/\`,
lo sprite del boss in \`src/main/resources/images/characters/boss_ymir.png\`
e Gunnar in \`/images/characters/gunnar_realms.png\`.

## Mappa

| Stanza | Nome | Uscite |
|---|---|---|
| 1 | Radura delle Radici | UP 2, DOWN 5, LEFT 3, RIGHT 4 |
| 2 | Cerchio delle Rune | UP 6, DOWN 1 |
| 3 | Forgia di Caverna | RIGHT 1 |
| 4 | Foresta Pietrificata | LEFT 1 |
| 5 | Miniere Basaltiche | UP 1, DOWN 7 |
| 6 | Rupe dell'Eco | DOWN 2 |
| 7 | Cuore di Roccia | UP 5 |

## Storia, enigmi e boss

- Nella **stanza 2**, clicca Gunnar e completa tutti i dialoghi.
  **INTERROMPI** chiude la conversazione senza concedere la conoscenza delle
  rune. **MOSTRA FRASE** rivela solo la frase corrente, senza saltare
  l'intera conversazione.
- Nella **stanza 6**, clicca la Stele Runica. I tre versi e le piattaforme
  sicure sono generati casualmente una sola volta per partita.
- Nel **Diario di Arthur (J)** sono conservati gli indizi già scoperti,
  compresa la sequenza dei versi della specifica partita.
- Nella **stanza 7**, clicca Ymir per aprire lo scontro, disponibile solo
  dopo aver ascoltato Gunnar e letto la stele. In ogni fase scegli una
  piattaforma e una runa. Errore: un cuore. Tre successi: Ymir dorme.
  Puoi rileggere i versi durante lo scontro. Dopo il terzo successo,
  torna alla stanza e raccogli la Lacrima di Ymir.
- Nella **stanza 3**, clicca direttamente il boccale visibile
  nell'immagine: la postazione di preparazione appare SOPRA lo
  scenario. Scegli tre ingredienti:
    - Muschio di Midgard + Frammento di Yggdrasil + Lacrima di Ymir
      -> Ymir's Old Fashioned e finale Capitolo III;
    - Radice Nera di Nidhogg -> il boccale si scioglie (Game Over);
    - Polvere di Ossa di Gigante -> il drink diventa pietra (Game Over).
  In entrambi i fallimenti il giocatore può riprovare senza perdere
  le partite già salvate sul database.

## Prova in locale

1. Assicurati che la copia locale sia sul branch \`feat/chapter3\`.
2. \`mvn clean test\`
3. \`mvn exec:java\`
4. **F3** avvia una NUOVA anteprima del capitolo 3, resettando solo
   lo stato in memoria: salva prima di usarlo.
5. **F4** apre la scelta rapida delle stanze, senza toccare l'inventario.
6. Verifica la consistenza della sequenza della stele: salvala su uno
   slot, caricala, rileggila e poi riprova il boss.
7. Salva dal pulsante nell'HUD dopo il finale, se vuoi conservare
   lo stato \`ymir_old_fashioned_created\`.

### Sicurezza dei salvataggi

- L'H2 su disco resta in \`saves/\`.
- Le nuove colonne vengono aggiunte con migrazioni
  \`ADD COLUMN IF NOT EXISTS\`: nessuna cancellazione delle partite.
- I test usano esclusivamente \`jdbc:h2:mem:\` e non accedono al
  database del giocatore.
- Sono esclusi dai commit i file \`saves/\` e \`target/\`.
