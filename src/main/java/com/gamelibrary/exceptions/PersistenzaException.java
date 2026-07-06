package com.gamelibrary.exceptions;

/**
 * Eccezione di dominio Checked, delegata alla notifica di criticità insorte
 * durante le interazioni in I/O (caricamento, serializzazione, problemi di file system).
 *
 * Implementa la best practice dell'Exception Shielding: l'architettura
 * intercetta eccezioni inerenti al livello fisico del Data Access Object
 * (come IOException, o difetti in fase di parsing Gson) oscurandole dietro a questa
 * classe custom per minimizzare l'accoppiamento verso la UI.
 *
 * @author Martino Marrosu
 */
public class PersistenzaException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Istanzia un'anomalia di persistenza corredata unicamente di una spiegazione formale.
     *
     * @param messaggio il feedback diagnostico descrivente il fallimento in Storage
     */
    public PersistenzaException(String messaggio) {
        super(messaggio);
    }

    /**
     * Associa al fallimento di Data Access System anche l'errore tecnico root.
     * Il chaining favorisce il tracciamento lato backend (Logging Layer) preservando
     * la pulizia dell'output utente lato frontend.
     *
     * @param messaggio il testo ad alto livello sul malfunzionamento logico
     * @param causa l'eccezione fisica di origine intercettata nello strato sottostante
     */
    public PersistenzaException(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }
}
