package com.gamelibrary.exceptions;

/**
 * Rappresenta un'anomalia occorsa in fase di instanziazione di un'entità Game.
 *
 * Trattandosi di un'eccezione Checked, delega in modo esplicito la gestione
 * (Error Recovery) al chiamante, consentendo un flusso applicativo prevedibile.
 * Viene sollevata dalla GameFactory avvalendosi del pattern Exception Shielding, 
 * ovvero intercettando eccezioni tecniche (es. IllegalArgumentException emessa
 * dal Builder) e mascherandole con un'anomalia legata al contesto di Dominio.
 *
 * @author Martino Marrosu
 */
public class GameCreationException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Dichiara l'errore in fase di Factory Method, fornendo unicamente un feedback testuale.
     *
     * @param messaggio descrizione formale del vincolo di dominio violato
     */
    public GameCreationException(String messaggio) {
        super(messaggio);
    }

    /**
     * Dichiara l'errore in fase di instanziazione mediante Exception Chaining.
     * Ciò permette al Service Layer di esporre all'interfaccia un messaggio
     * pulito, seppur mantenendo l'errore di root (Throwable causa) persistito nel log.
     *
     * @param messaggio il feedback testuale sul fallimento dell'azione
     * @param causa il problema di instanziazione sorgente sollevato dai validatori interni
     */
    public GameCreationException(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }
}
