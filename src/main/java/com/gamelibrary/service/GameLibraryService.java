package com.gamelibrary.service;

import com.gamelibrary.annotations.Operazione;
import com.gamelibrary.exceptions.GameCreationException;
import com.gamelibrary.exceptions.GameNotFoundException;
import com.gamelibrary.exceptions.PersistenzaException;
import com.gamelibrary.factory.GameFactory;
import com.gamelibrary.memento.CaretakerStorico;
import com.gamelibrary.model.Game;
import com.gamelibrary.model.Game.GameMemento;
import com.gamelibrary.model.Genere;
import com.gamelibrary.model.Piattaforma;
import com.gamelibrary.observer.EventoLibreria;
import com.gamelibrary.observer.OsservatoreLibreria;
import com.gamelibrary.persistence.Persistenza;
import com.gamelibrary.util.Risultato;
import com.gamelibrary.util.StatisticheUtil;

import java.util.*;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Classe principale che espone il Service Layer dell'applicazione per la gestione della libreria di giochi.
 * Centralizza la logica di business: le operazioni richieste dall'utente (inserimento, modifica, ricerca) 
 * vengono delegate a questa classe, la quale funge da intermediario tra l'interfaccia utente (UI) e il livello di persistenza dei dati.
 * Implementa l'Observer Pattern per disaccoppiare la logica dalla UI, e il Memento Pattern 
 * per consentire funzionalità di ripristino dello stato (Undo).
 *
 * @author Martino Marrosu
 */
public class GameLibraryService {

    private static final Logger LOGGER = Logger.getLogger(GameLibraryService.class.getName());

    // Lista sincronizzata per garantire thread‑safety durante le operazioni
    // concorrenti
    private final List<Game> giochi;
    private final Persistenza persistenza;
    private final CaretakerStorico storico;
    private final List<OsservatoreLibreria> osservatori;
    // Executor a thread singolo per gestire il salvataggio asincrono su disco
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Inizializza il servizio iniettando le dipendenze necessarie.
     * Questa iniezione dal costruttore facilita la testabilità (es. uso di mock per i test unitari).
     *
     * @param persistenza il gestore per il salvataggio e caricamento dei dati
     * @param storico il gestore dello storico per l'annullamento delle modifiche
     */
    public GameLibraryService(Persistenza persistenza, CaretakerStorico storico) {
        this.persistenza = Objects.requireNonNull(persistenza, "La persistenza non può essere nulla.");
        this.storico = Objects.requireNonNull(storico, "Lo storico non può essere nullo.");
        this.giochi = Collections.synchronizedList(new ArrayList<>());
        this.osservatori = new ArrayList<>();
        LOGGER.info("GameLibraryService inizializzato.");
    }

    // Gestione osservatori (Observer Pattern)

    /**
     * Registra un nuovo osservatore per gli eventi della libreria.
     *
     * @param osservatore l'istanza che si pone in ascolto delle modifiche
     */
    public void registraOsservatore(OsservatoreLibreria osservatore) {
        if (osservatore == null) {
            LOGGER.warning("Tentativo di registrare un osservatore nullo — ignorato.");
            return;
        }
        osservatori.add(osservatore);
        LOGGER.info("Osservatore registrato. Totale osservatori: " + osservatori.size());
    }

    /**
     * Rimuove un osservatore precedentemente registrato.
     *
     * @param osservatore l'istanza da rimuovere
     */
    public void rimuoviOsservatore(OsservatoreLibreria osservatore) {
        osservatori.remove(osservatore);
    }

    /**
     * Notifica a tutti gli osservatori registrati l'avvenimento di uno specifico evento.
     *
     * @param evento la tipologia di evento scatenato
     * @param gioco l'istanza del gioco oggetto dell'evento
     */
    private void notificaOsservatori(EventoLibreria evento, Game gioco) {
        for (OsservatoreLibreria osservatore : osservatori) {
            try {
                osservatore.aggiorna(evento, gioco);
            } catch (Exception e) {
                // Un osservatore difettoso non deve bloccare il servizio
                LOGGER.warning(() -> String.format(
                        "Errore nell'osservatore durante evento %s: %s", evento, e.getMessage()));
            }
        }
    }

    // Operazioni principali di business

    /**
     * Crea e inserisce un nuovo gioco nella libreria, delegando la validazione formale alla Factory.
     *
     * @param titolo il titolo del gioco
     * @param piattaforma la piattaforma di destinazione
     * @param genere il genere videoludico
     * @param anno l'anno di rilascio
     * @param voto il voto assegnato dall'utente
     * @return un oggetto Risultato contenente l'esito dell'operazione
     */
    @Operazione(descrizione = "Creazione e inserimento di un nuovo gioco nella libreria")
    public Risultato<Game> aggiungiGioco(String titolo, Piattaforma piattaforma,
            Genere genere, int anno, int voto) {
        try {
            Game gioco = GameFactory.crea(titolo, piattaforma, genere, anno, voto);
            giochi.add(gioco);
            notificaOsservatori(EventoLibreria.GIOCO_AGGIUNTO, gioco);

            LOGGER.info(() -> String.format("Gioco aggiunto alla libreria: '%s' (ID: %s)",
                    gioco.getTitolo(), gioco.getId()));
            // Salvataggio asincrono
            salvaAsync();

            return Risultato.successo(gioco);

        } catch (GameCreationException e) {
            LOGGER.warning(() -> String.format("Creazione gioco fallita: %s", e.getMessage()));
            return Risultato.fallimento(e.getMessage());
        }
    }

    /**
     * Modifica il valore di un attributo specifico per un gioco esistente.
     * Preserva lo stato antecedente la modifica per permettere un successivo annullamento (pattern Memento).
     *
     * @param id l'identificativo univoco del gioco
     * @param campo l'attributo da alterare (es. "titolo", "voto")
     * @param valore il nuovo valore da assegnare
     * @return l'esito dell'operazione incapsulato in un Risultato
     * @throws GameNotFoundException qualora l'ID fornito non corrisponda a nessun gioco in libreria
     */
    @Operazione(descrizione = "Modifica dei campi di un gioco esistente")
    public Risultato<Game> modificaGioco(String id, String campo, String valore)
            throws GameNotFoundException {

        Game gioco = cercaPerIdInterno(id);

        // Salva lo stato PRIMA della modifica (Memento)
        storico.salvaStato(gioco);

        try {
            switch (campo.toLowerCase()) {
                case "titolo" -> gioco.setTitolo(valore);
                case "piattaforma" -> {
                    try {
                        gioco.setPiattaforma(Piattaforma.valueOf(valore.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        LOGGER.warning(() -> String.format("Piattaforma non valida: '%s'", valore));
                        return Risultato.fallimento(
                                String.format("Piattaforma '%s' non riconosciuta. Piattaforme valide: %s",
                                        valore, java.util.Arrays.toString(Piattaforma.values())));
                    }
                }
                case "genere" -> {
                    try {
                        gioco.setGenere(Genere.valueOf(valore.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        LOGGER.warning(() -> String.format("Genere non valido: '%s'", valore));
                        return Risultato.fallimento(
                                String.format("Genere '%s' non riconosciuto. Generi validi: %s",
                                        valore, java.util.Arrays.toString(Genere.values())));
                    }
                }
                case "anno" -> gioco.setAnno(Integer.parseInt(valore));
                case "voto" -> gioco.setVoto(Integer.parseInt(valore));
                default -> {
                    return Risultato.fallimento(
                            String.format(
                                    "Campo '%s' non riconosciuto. Campi validi: titolo, piattaforma, genere, anno, voto.",
                                    campo));
                }
            }

            notificaOsservatori(EventoLibreria.GIOCO_MODIFICATO, gioco);

            LOGGER.info(() -> String.format("Gioco modificato: '%s' — campo '%s' → '%s'",
                    gioco.getTitolo(), campo, valore));
            // Salvataggio asincrono dopo modifica
            salvaAsync();

            return Risultato.successo(gioco);

        } catch (IllegalArgumentException e) {
            // Ripristina lo stato precedente se la modifica fallisce
            storico.annullaUltimaModifica(gioco);
            return Risultato.fallimento(
                    String.format("Valore non valido per '%s': %s", campo, e.getMessage()));
        }
    }

    /**
     * Rimuove definitivamente un gioco dalla collezione corrente, ricercandolo per ID.
     *
     * @param id l'identificativo del gioco da espungere
     * @return l'esito dell'operazione
     * @throws GameNotFoundException qualora l'ID fornito non corrisponda ad alcuna voce
     */
    @Operazione(descrizione = "Rimozione di un gioco dalla libreria")
    public Risultato<Game> rimuoviGioco(String id) throws GameNotFoundException {
        Game gioco = cercaPerIdInterno(id);
        giochi.remove(gioco);
        notificaOsservatori(EventoLibreria.GIOCO_RIMOSSO, gioco);

        LOGGER.info(() -> String.format("Gioco rimosso dalla libreria: '%s' (ID: %s)",
                gioco.getTitolo(), gioco.getId()));
        // Salvataggio asincrono dopo rimozione
        salvaAsync();

        return Risultato.successo(gioco);
    }

    // Ricerca e interrogazione

    /**
     * Ricerca puntuale di un gioco all'interno della libreria mediante l'identificativo univoco.
     *
     * @param id l'ID del gioco da individuare
     * @return il Risultato contenente il gioco, o un errore qualora la ricerca fallisca
     */
    public Risultato<Game> cercaPerId(String id) {
        return giochi.stream()
                .filter(g -> g.getId().equals(id))
                .findFirst()
                .map(Risultato::successo)
                .orElse(Risultato.fallimento(
                        String.format("Nessun gioco trovato con ID: %s", id)));
    }

    /**
     * Ricerca tramite sottostringa di testo applicata al titolo dei giochi.
     *
     * @param titolo la stringa parziale da ricercare
     * @return una lista contenente le occorrenze individuate
     */
    public List<Game> cercaPerTitolo(String titolo) {
        return StatisticheUtil.cercaPerTitolo(giochi, titolo);
    }

    /**
     * Applica un filtro sulla collezione per estrarre esclusivamente i titoli compatibili con una determinata piattaforma.
     *
     * @param piattaforma la piattaforma da utilizzare come filtro
     * @return la collezione filtrata
     */
    public List<Game> filtraPerPiattaforma(Piattaforma piattaforma) {
        return StatisticheUtil.filtraPerPiattaforma(giochi, piattaforma);
    }

    /**
     * Applica un filtro sulla collezione per estrarre esclusivamente i titoli di un determinato genere videoludico.
     *
     * @param genere il genere da utilizzare come filtro
     * @return la collezione filtrata
     */
    public List<Game> filtraPerGenere(Genere genere) {
        return StatisticheUtil.filtraPerGenere(giochi, genere);
    }

    /**
     * Applica un filtro per isolare i titoli pubblicati in un anno specifico.
     *
     * @param anno l'anno di pubblicazione target
     * @return la collezione filtrata
     */
    public List<Game> filtraPerAnno(int anno) {
        return StatisticheUtil.filtraPerAnno(giochi, anno);
    }

    /**
     * Filtra la libreria per restituire unicamente i giochi che possiedono una valutazione utente pari o superiore alla soglia definita.
     *
     * @param votoMinimo la soglia minima di valutazione
     * @return la collezione filtrata
     */
    public List<Game> filtraPerVotoMinimo(int votoMinimo) {
        return StatisticheUtil.filtraPerVotoMinimo(giochi, votoMinimo);
    }

    // Ordinamento della collezione

    /**
     * Esegue l'ordinamento lessicografico della collezione in base al titolo.
     *
     * @return la lista ordinata dei giochi
     */
    public List<Game> ordinaPerTitolo() {
        return StatisticheUtil.ordinaPerTitolo(giochi);
    }

    /**
     * Esegue l'ordinamento cronologico decrescente (dal più recente al meno recente).
     *
     * @return la lista ordinata dei giochi
     */
    public List<Game> ordinaPerAnno() {
        return StatisticheUtil.ordinaPerAnno(giochi);
    }

    /**
     * Esegue l'ordinamento decrescente in base alla valutazione (voto) assegnata.
     *
     * @return la lista ordinata dei giochi
     */
    public List<Game> ordinaPerVoto() {
        return StatisticheUtil.ordinaPerVoto(giochi);
    }

    // Aggregazione e statistiche

    /**
     * Aggrega gli elementi della collezione suddividendoli per categoria di piattaforma.
     *
     * @return una mappa contenente come chiavi le piattaforme e come valori le relative liste di giochi
     */
    public Map<Piattaforma, List<Game>> raggruppaPerPiattaforma() {
        return StatisticheUtil.raggruppaPeRPiattaforma(giochi);
    }

    /**
     * Genera una distribuzione di frequenza computando il numero di giochi disponibili per ogni singolo genere.
     *
     * @return una mappa associativa genere/conteggio
     */
    public Map<Genere, Long> contaPerGenere() {
        return StatisticheUtil.contaPerGenere(giochi);
    }

    /**
     * Elabora un resoconto testuale comprensivo delle statistiche descrittive generali della collezione.
     *
     * @return la stringa formattata rappresentante il report
     */
    public String generaStatistiche() {
        return StatisticheUtil.generaReport(giochi);
    }

    // Ripristino stato (Memento / Undo)

    /**
     * Esegue l'azione di ripristino (Undo) annullando l'ultima operazione di modifica effettuata, 
     * avvalendosi dello stato salvato nel Memento tramite lo storico.
     *
     * @return l'esito dell'operazione di ripristino
     */
    public Risultato<Game> annullaUltimaModifica() {
        if (storico.isVuoto()) {
            return Risultato.fallimento("Non ci sono modifiche da annullare.");
        }

        // Peek al memento per trovare il gioco corrispondente
        Optional<GameMemento> mementoOpt = storico.ultimoStato();
        if (mementoOpt.isEmpty()) {
            return Risultato.fallimento("Non ci sono modifiche da annullare.");
        }

        GameMemento memento = mementoOpt.get();
        String idGioco = memento.getId();

        // Cerca il gioco nella libreria
        Optional<Game> giocoOpt = giochi.stream()
                .filter(g -> g.getId().equals(idGioco))
                .findFirst();

        if (giocoOpt.isEmpty()) {
            // Il gioco potrebbe essere stato rimosso — rimuovi il memento inutile
            storico.annullaUltimaModifica(new Game.Builder("temp")
                    .id("non-esistente")
                    .piattaforma(Piattaforma.STEAM)
                    .genere(Genere.ACTION)
                    .anno(2000)
                    .voto(5)
                    .build());
            return Risultato.fallimento(
                    String.format("Il gioco '%s' non è più nella libreria.", memento.getTitolo()));
        }

        Game gioco = giocoOpt.get();
        Optional<GameMemento> risultato = storico.annullaUltimaModifica(gioco);

        if (risultato.isPresent()) {
            notificaOsservatori(EventoLibreria.GIOCO_MODIFICATO, gioco);
            LOGGER.info(() -> String.format("Annullata modifica per '%s' (ID: %s)",
                    gioco.getTitolo(), gioco.getId()));
            return Risultato.successo(gioco);
        }

        return Risultato.fallimento("Impossibile annullare la modifica.");
    }

    // Persistenza su file system

    /**
     * Scatena un salvataggio sincrono dello stato attuale della collezione sul supporto persistente (JSON).
     * Viene utilizzato in prevalenza negli ambienti di test per imporre una scrittura bloccante.
     *
     * @throws PersistenzaException qualora sorgano errori I/O durante la serializzazione
     */
    public void salva() throws PersistenzaException {
        persistenza.salva(new ArrayList<>(giochi));
        LOGGER.info(() -> String.format("Libreria salvata (sync): %d giochi.", giochi.size()));
    }

    /**
     * Innesca un'operazione di salvataggio delegandola a un thread concorrente.
     * L'approccio asincrono permette di non ostacolare il thread principale e l'interazione con l'utente.
     */
    private void salvaAsync() {
        LOGGER.info("Salvataggio in corso ...");
        executor.submit(() -> {
            try {
                persistenza.salva(new ArrayList<>(giochi));
                LOGGER.info(() -> String.format("Libreria salvata (async): %d giochi.", giochi.size()));
            } catch (PersistenzaException e) {
                LOGGER.severe(() -> String.format("Errore di salvataggio asincrono: %s", e.getMessage()));
                // Rilancia come RuntimeException per permettere al Future di catturarla
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    /**
     * Deserializza i dati dal file JSON riversandoli nella collezione in memoria centrale,
     * sovrascrivendo l'eventuale stato preesistente in modo thread-safe.
     *
     * @throws PersistenzaException nel caso vi siano criticità in fase di lettura o validazione JSON
     */
    public void carica() throws PersistenzaException {
        List<Game> giochiCaricati = persistenza.carica();
        // Sostituiamo la lista in modo thread‑safe
        synchronized (giochi) {
            giochi.clear();
            giochi.addAll(giochiCaricati);
        }
        LOGGER.info(() -> String.format("Libreria caricata: %d giochi.", giochi.size()));
    }

    // Metodi di utilità e accesso

    /**
     * Fornisce l'accesso alla collezione completa in modalità protetta, 
     * restituendo un clone della lista per evitare alterazioni esterne all'incapsulamento della classe.
     *
     * @return una copia difensiva dell'elenco dei giochi
     */
    public List<Game> getTuttiIGiochi() {
        synchronized (giochi) {
            return new ArrayList<>(giochi);
        }
    }

    /**
     * Computa la cardinalità dell'insieme dei giochi gestiti.
     *
     * @return il quantitativo totale di istanze registrate
     */
    public int numeroDiGiochi() {
        synchronized (giochi) {
            return giochi.size();
        }
    }

    /**
     * Verifica l'assenza di elementi all'interno della collezione.
     *
     * @return {@code true} qualora non sussistano elementi in memoria
     */
    public boolean isVuota() {
        synchronized (giochi) {
            return giochi.isEmpty();
        }
    }

    /**
     * Espone in sola lettura il modulo incaricato alla tracciatura e salvataggio dello storico.
     *
     * @return il gestore Caretaker
     */
    public CaretakerStorico getStorico() {
        return storico;
    }

    /**
     * Esegue lo spegnimento controllato del pool di thread (ExecutorService).
     * Procedura critica da chiamare in chiusura dell'applicazione per permettere al processo di terminare correttamente.
     */
    public void shutdown() {
        try {
            executor.shutdown();
            LOGGER.info("ExecutorService chiuso.");
        } catch (Exception e) {
            LOGGER.warning(() -> "Errore durante la chiusura dell'executor: " + e.getMessage());
        }
    }

    /**
     * Funzione ausiliaria per la ricerca stringente per identificativo. Provvede al lancio esplicito 
     * di eccezione di business se la verifica dovesse fallire.
     *
     * @param id l'identificativo bersaglio
     * @return l'istanza individuata
     * @throws GameNotFoundException nel caso manchi la corrispondenza in libreria
     */
    private Game cercaPerIdInterno(String id) throws GameNotFoundException {
        synchronized (giochi) {
            return giochi.stream()
                    .filter(g -> g.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new GameNotFoundException(
                            String.format("Nessun gioco trovato con ID: %s", id)));
        }
    }
}
