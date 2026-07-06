package com.gamelibrary.exceptions;

/**
 * Eccezione checked (recuperabile) sollevata a seguito del fallimento 
 * di un'operazione di interrogazione o alterazione su un'istanza non presente in libreria.
 *
 * Impone strutturalmente al livello chiamante la gestione dell'anomalia,
 * conformandosi al principio di Robustness per input imprevisti dell'utente.
 *
 * @author Martino Marrosu
 */
public class GameNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Instanzia l'eccezione corredandola di un messaggio esplicativo contestuale.
     *
     * @param messaggio descrizione del dettaglio di fallimento (es. ID mancante)
     */
    public GameNotFoundException(String messaggio) {
        super(messaggio);
    }

    /**
     * Instanzia l'eccezione garantendo il wrapping di una causa preesistente (Exception Chaining).
     *
     * @param messaggio descrizione del dettaglio di fallimento
     * @param causa     l'eccezione originaria catturata
     */
    public GameNotFoundException(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }
}
