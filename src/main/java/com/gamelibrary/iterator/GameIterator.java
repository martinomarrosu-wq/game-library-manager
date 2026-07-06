package com.gamelibrary.iterator;

import com.gamelibrary.model.Game;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implementazione concreta dell'IteratoreLibreria per iterare su oggetti di tipo Game.
 * Per prevenire eccezioni del tipo ConcurrentModificationException durante l'iterazione,
 * la classe opera su una copia difensiva della lista originale passata a costruttore.
 *
 * @author Martino Marrosu
 */
public class GameIterator implements IteratoreLibreria<Game> {

    private final List<Game> giochi;
    private int posizioneCorrente;

    /**
     * Inizializza l'iteratore creando una copia difensiva della collezione di partenza.
     *
     * @param giochi la lista di giochi su cui iterare
     * @throws IllegalArgumentException qualora la referenza alla lista sia nulla
     */
    public GameIterator(List<Game> giochi) {
        if (giochi == null) {
            throw new IllegalArgumentException("La lista di giochi non può essere nulla.");
        }
        // Copia difensiva: l'iteratore non è influenzato da modifiche esterne alla lista
        this.giochi = List.copyOf(giochi);
        this.posizioneCorrente = 0;
    }

    /**
     * Verifica se il cursore ha raggiunto la fine della collezione.
     *
     * @return true qualora vi siano altri elementi da iterare
     */
    @Override
    public boolean hasProssimo() {
        return posizioneCorrente < giochi.size();
    }

    /**
     * Restituisce il gioco alla posizione corrente del cursore e ne incrementa l'indice.
     *
     * @return il prossimo gioco nella collezione
     * @throws NoSuchElementException se la collezione è stata esaurita
     */
    @Override
    public Game prossimo() {
        if (!hasProssimo()) {
            throw new NoSuchElementException("Non ci sono più giochi da iterare.");
        }
        return giochi.get(posizioneCorrente++);
    }

    /**
     * Resetta l'indice del cursore a 0 per consentire una nuova iterazione completa.
     */
    @Override
    public void reset() {
        posizioneCorrente = 0;
    }

    /**
     * Restituisce la cardinalità della collezione iterata.
     *
     * @return la quantità totale degli elementi
     */
    public int dimensione() {
        return giochi.size();
    }
}
