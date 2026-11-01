# Pulizia del codice per la revisione universitaria

**Branch:** `refactor/view-model-cleanup` · **base:** `b586dfce`.
Questo documento descrive il refactoring successivo alla prima analisi.

## Scelte da spiegare al professore

### GameState
Non esiste un comando Java sicuro per impostare automaticamente tutti
i booleani di un oggetto a false senza conoscere i loro significati.
La riflessione sarebbe fragile e aggirerebbe le regole di incapsulamento.

Il reset è ora organizzato in `resetExploration`, `resetStoryProgress` e
`resetYmirProgress`. Resta **esplicito**, per non azzerare accidentalmente
nuovi flag. Gli osservatori della salute rimangono registrati e ricevono
un solo evento sul reset.

`getInventory()` restituisce una copia non modificabile;
`setInventory()` effettua una copia difensiva, scartando doppioni per ID;
`clearInventory()` è l'operazione intenzionale di svuotamento.
In particolare, il calderone del capitolo I chiama `clearInventory()`,
non modifica più direttamente la lista interna del modello.

### RecipeManager
`private RecipeManager() { }` **non è codice morto**: il costruttore
privato impedisce di istanziare una classe di soli metodi statici.
`forChapter(int)` è usato per selezionare ricetta, ingredienti
disponibili e stanza del checkpoint. Non va eliminato.

### Prologo
`ProloguePanel` ora ha un unico metodo per terminare il prologo
(SKIP oppure fine conversazione), una sola configurazione dei cinque
sprite e una routine condivisa per disegnare i pulsanti.
Il layout, i nomi dei personaggi, i PNG e i tasti sono rimasti invariati.
Il codice è sceso da 154 a 113 righe.

### Capitoli I e II
`DialogueManager.SceneSession` crea e coordina una sola volta:
- cornice `SharedDialogueBox` e sprite;
- visualizzazione dei ritratti con bounds personalizzati (C2);
- click/INVIO/SPAZIO e text wrapping;
- apertura, interruzione e chiusura della conversazione.

Le decisioni narrative restano nelle rispettive classi:
Martino apre la donazione; Hræsvelgr/Rán si attivano solo al termine
della rispettiva introduzione. I clic su NPC ordinari non avviano boss.
`AbstractChapterPanel` centralizza la fabbrica degli hotspot
e il checkpoint del calderone per tutti e tre i capitoli.

### Stanze
`Chapter3World` è stato **eliminato**.
`ChapterLayouts.forChapter(id)` contiene i sette fondali e le uscite
di **tutti e tre** i capitoli, con la stessa interfaccia. Il
`Chapter3Panel` non ripete più i percorsi dei PNG al suo interno.
Sono preservati tutti i collegamenti, le coordinate degli hotspot
e i nomi delle stanze nel selettore rapido F4.

Non è stato necessario rinominare le immagini:
sarebbe stato un cambiamento di risorse senza alcun vantaggio
per la progettazione delle classi.

## Perché non eliminare altre classi
La separazione `Item` / `GameState` / `RecipeManager` /
`DialogueManager` / `ChapterLayouts` rispetta responsabilità
diverse. Unirle creerebbe dipendenze e codice monolitico.
Un minor numero di file non coincide automaticamente con un
progetto meglio progettato.

## Prove da fare dopo il checkout
1. Nuova partita, SKIP del prologo, clic e invio durante dialoghi C1/C2.
2. Verifica delle posizioni speciali dei ritratti (Marmæl,
   sgherro e Jörmungandr).
3. Navigazione delle sette stanze di ciascun capitolo; F4 in C3.
4. Calderoni: ricetta errata, Game Over, retry, ingredienti conservati.
5. Salvataggio e caricamento H2: inventario e flag devono restare corretti.

La suite JUnit comprende test per il reset, l'inventario protetto,
la mappa di tutti e tre i capitoli e il nuovo componente dialoghi.
**Nessun file H2, PNG o artefatto Maven in `target/` viene modificato
da questo refactoring.**
