package com.gamelibrary.model;

/**
 * Tipo enumerato che raccoglie le diverse categorie (generi videoludici) supportate dal dominio.
 * Integra una rappresentazione verbale formattata in lingua italiana per esigenze di interfaccia utente.
 *
 * @author Martino Marrosu
 */
public enum Genere {

    ACTION("Azione"),
    RPG("RPG"),
    ADVENTURE("Avventura"),
    PUZZLE("Puzzle"),
    SPORT("Sport"),
    HORROR("Horror"),
    STRATEGY("Strategia"),
    SIMULATION("Simulazione"),
    PLATFORM("Platform"),
    FIGHTING("Picchiaduro");

    private final String nomeVisualizzazione;

    /**
     * Associa al valore enumerato una stringa di testo utile alla presentazione.
     *
     * @param nomeVisualizzazione stringa descrittiva
     */
    Genere(String nomeVisualizzazione) {
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
