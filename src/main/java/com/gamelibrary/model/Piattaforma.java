package com.gamelibrary.model;

/**
 * Tipo enumerato che censisce le piattaforme hardware comunemente associate all'esecuzione dei titoli.
 * È munito di etichetta esplicativa ad uso delle interfacce grafiche/testuali (CLI).
 *
 * @author Martino Marrosu
 */
public enum Piattaforma {

    PLAYSTATION("PlayStation"),
    XBOX("Xbox"),
    NINTENDO("Nintendo"),
    STEAM("Steam");

    private final String nomeVisualizzazione;

    /**
     * Associa al valore enumerato una stringa esplicativa.
     *
     * @param nomeVisualizzazione stringa testuale associata
     */
    Piattaforma(String nomeVisualizzazione) {
        this.nomeVisualizzazione = nomeVisualizzazione;
    }

    public String getNomeVisualizzazione() {
        return nomeVisualizzazione;
    }

    @Override
    public String toString() {
        return nomeVisualizzazione;
    }
}
