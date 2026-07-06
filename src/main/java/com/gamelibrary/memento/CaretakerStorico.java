package com.gamelibrary.memento;

import com.gamelibrary.model.Game;
import com.gamelibrary.model.Game.GameMemento;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementazione del ruolo di Caretaker secondo il pattern Memento.
 * Gestisce uno stack degli stati pregressi per erogare la funzionalità di ripristino (Undo).
 * Prevede un tetto massimo di salvataggi in memoria al fine di mitigarne il consumo (Memory Footprint),
 * adottando una politica di evizione di tipo FIFO al raggiungimento della soglia.
 *
 * @author Martino Marrosu
 */
public class CaretakerStorico {

    private static final Logger LOGGER = Logger.getLogger(CaretakerStorico.class.getName());

    /** Limite massimo di default per lo storico */
    public static final int LIMITE_DEFAULT = 10;

    private final Deque<GameMemento> storico;
    private final int limiteMax;

    /**
     * Inizializza il Caretaker adottando il limite capacitivo di default (10).
     */
    public CaretakerStorico() {
        this(LIMITE_DEFAULT);
    }

    /**
     * Instanzia il Caretaker vincolandone esplicitamente la capacità massima ammissibile.
     *
     * @param limiteMax il numero massimo di stati storicizzabili
     * @throws IllegalArgumentException qualora la soglia sia definita con un valore inferiore all'unità
     */
    public CaretakerStorico(int limiteMax) {
        if (limiteMax < 1) {
            throw new IllegalArgumentException(
                    String.format("Il limite massimo deve essere almeno 1, ricevuto: %d", limiteMax));
        }
        this.limiteMax = limiteMax;
        // ArrayDeque è più performante di LinkedList per uso come stack (no overhead nodi)
        this.storico = new ArrayDeque<>(limiteMax);
        LOGGER.info(() -> String.format("CaretakerStorico inizializzato con limite massimo: %d", limiteMax));
    }

    /**
     * Genera e archivia un'istantanea formale dello stato del gioco all'interno dello storico.
     * Provvede in modo autonomo all'esclusione (eviction) del salvataggio più obsoleto se il limite risulta superato.
     *
     * @param gioco il riferimento all'Originator da campionare
     * @throws IllegalArgumentException qualora venga passato un reference nullo
     */
    public void salvaStato(Game gioco) {
        if (gioco == null) {
            throw new IllegalArgumentException("Il gioco non può essere nullo.");
        }

        // Se lo stack è pieno, rimuovi il memento più vecchio (dal fondo)
        if (storico.size() >= limiteMax) {
            GameMemento rimosso = storico.removeLast();
            LOGGER.info(() -> String.format("Rimosso memento più vecchio dallo storico: %s (limite %d raggiunto)",
                    rimosso, limiteMax));
        }

        GameMemento memento = gioco.salvaStato();
        storico.push(memento);

        LOGGER.info(() -> String.format("Stato salvato per gioco '%s' (ID: %s). Storico: %d/%d",
                gioco.getTitolo(), gioco.getId(), storico.size(), limiteMax));
    }

    /**
     * Esegue il recupero in modalità sicura dell'ultimo stato valido, operando una validazione 
     * di congruità tramite verifica dell'identificativo (ID) tra l'Originator corrente e il Memento.
     *
     * @param gioco l'istanza su cui attuare il rollback
     * @return un Optional contenente il Memento estratto se il ripristino va a buon fine, altrimenti empty
     * @throws IllegalArgumentException nel caso si tenti la manovra su un oggetto nullo
     */
    public Optional<GameMemento> annullaUltimaModifica(Game gioco) {
        if (gioco == null) {
            throw new IllegalArgumentException("Il gioco non può essere nullo.");
        }

        if (storico.isEmpty()) {
            LOGGER.info("Tentativo di annullamento con storico vuoto.");
            return Optional.empty();
        }

        GameMemento memento = storico.pop();

        // Verifica che il memento appartenga al gioco corretto
        if (!memento.getId().equals(gioco.getId())) {
            // Rimetti il memento nello stack — non è per questo gioco
            storico.push(memento);
            LOGGER.warning(() -> String.format(
                    "Memento in cima allo stack appartiene al gioco '%s' (ID: %s), "
                    + "non al gioco richiesto '%s' (ID: %s). Annullamento non eseguito.",
                    memento.getTitolo(), memento.getId(),
                    gioco.getTitolo(), gioco.getId()));
            return Optional.empty();
        }

        gioco.ripristinaStato(memento);

        LOGGER.info(() -> String.format("Stato ripristinato per gioco '%s' (ID: %s). Storico rimanente: %d/%d",
                gioco.getTitolo(), gioco.getId(), storico.size(), limiteMax));

        return Optional.of(memento);
    }

    /**
     * Consente l'ispezione in sola lettura dell'ultimo Memento depositato in cima alla pila (operazione peek).
     *
     * @return l'istanza storicizzata incapsulata, o empty se la pila risulta esaurita
     */
    public Optional<GameMemento> ultimoStato() {
        return Optional.ofNullable(storico.peek());
    }

    /**
     * Restituisce la consistenza numerica dello storico corrente.
     *
     * @return l'ammontare degli elementi attualmente impilati
     */
    public int dimensione() {
        return storico.size();
    }

    /**
     * Verifica l'assenza totale di elementi all'interno del registro di salvataggio.
     *
     * @return true qualora non vi siano storicizzazioni attive
     */
    public boolean isVuoto() {
        return storico.isEmpty();
    }

    /**
     * Espone la soglia massima prefissata di mantenimento in memoria.
     *
     * @return il limite numerico configurato
     */
    public int getLimiteMax() {
        return limiteMax;
    }

    /**
     * Effettua lo svuotamento de-allocando forzatamente l'intero storico in memoria. 
     * Utile nei flussi di caricamento ex novo dell'applicazione.
     */
    public void svuota() {
        int dimensionePrecedente = storico.size();
        storico.clear();
        LOGGER.info(() -> String.format("Storico svuotato: rimossi %d memento.", dimensionePrecedente));
    }
}
