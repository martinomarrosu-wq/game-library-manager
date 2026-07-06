package com.gamelibrary.iterator;

import com.gamelibrary.model.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Implementazione specializzata dell'Iterator Pattern preposta all'attraversamento condizionato.
 * Sfrutta un approccio di valutazione anticipata (Eager Evaluation) applicando il predicato 
 * in fase di instanziazione. Tale scelta architetturale favorisce la predicibilità delle performance
 * in fase di iterazione, a fronte di un moderato trade-off sull'occupazione di memoria (Memory Footprint).
 *
 * @author Martino Marrosu
 */
public class GameIteratorFiltrato implements IteratoreLibreria<Game> {

    private final List<Game> giochiFiltrati;
    private int posizioneCorrente;

    /**
     * Inizializza l'iteratore operando un filtraggio preventivo sul dataset fornito.
     *
     * @param giochi la collezione sorgente da elaborare
     * @param predicato la funzione di test impiegata come criterio di inclusione
     * @throws IllegalArgumentException qualora i parametri risultino nulli
     */
    public GameIteratorFiltrato(List<Game> giochi, Predicate<Game> predicato) {
        if (giochi == null) {
            throw new IllegalArgumentException("La lista di giochi non può essere nulla.");
        }
        if (predicato == null) {
            throw new IllegalArgumentException("Il predicato di filtraggio non può essere nullo.");
        }

        // Pre-computa la lista filtrata — eager evaluation
        this.giochiFiltrati = new ArrayList<>();
        for (Game gioco : giochi) {
            if (predicato.test(gioco)) {
                giochiFiltrati.add(gioco);
            }
        }
        this.posizioneCorrente = 0;
    }

    /**
     * Verifica l'eventuale presenza di ulteriori elementi nel buffer filtrato.
     *
     * @return true qualora sussistano nodi da esaminare
     */
    @Override
    public boolean hasProssimo() {
        return posizioneCorrente < giochiFiltrati.size();
    }

    /**
     * Esegue il fetch del successivo elemento utile e fa avanzare il cursore interno.
     *
     * @return l'istanza iterata al passo corrente
     * @throws NoSuchElementException in caso di superamento del limite superiore dell'indice
     */
    @Override
    public Game prossimo() {
        if (!hasProssimo()) {
            throw new NoSuchElementException("Non ci sono più giochi filtrati da iterare.");
        }
        return giochiFiltrati.get(posizioneCorrente++);
    }

    /**
     * Reinizializza il puntatore portandolo alla posizione logica iniziale (indice zero).
     */
    @Override
    public void reset() {
        posizioneCorrente = 0;
    }

    /**
     * Restituisce la consistenza numerica del sottoinsieme generato dal predicato.
     *
     * @return il computo totale degli elementi validi
     */
    public int dimensione() {
        return giochiFiltrati.size();
    }

    /**
     * Verifica se l'operazione di classificazione abbia prodotto un insieme vuoto.
     *
     * @return true qualora nessun elemento soddisfi la condizione di filtraggio
     */
    public boolean isVuoto() {
        return giochiFiltrati.isEmpty();
    }
}
