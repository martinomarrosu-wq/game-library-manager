package com.gamelibrary.exceptions;

/**
 * Eccezione di tipo Unchecked progettata per la notifica di un input strutturalmente invalido.
 *
 * Aderendo alle convenzioni del Defensive Programming, questa anomalia funge da barriera
 * estrema (Fail-Fast) nell'eventualità che parametri difettosi scavalchino le
 * fasi di validazione del layer di Boundary. Il ricorso a una sottoclasse
 * di RuntimeException consente di mantenere pulita la firma dei metodi esposti (assenza di throws clause).
 *
 * @author Martino Marrosu
 */
public class InputNonValidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Instanzia l'anomalia associandovi una descrizione esplicita della violazione riscontrata.
     *
     * @param messaggio dettaglio relativo all'invalidità dell'input
     */
    public InputNonValidoException(String messaggio) {
        super(messaggio);
    }

    /**
     * Genera un'anomalia con tecnica di Exception Chaining. Utile qualora l'input
     * malformato causi eccezioni di basso livello (es. NumberFormatException).
     *
     * @param messaggio il testo informativo sull'irregolarità formale
     * @param causa l'anomalia di sistema sollevata in origine
     */
    public InputNonValidoException(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }
}
