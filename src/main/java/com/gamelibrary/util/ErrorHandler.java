package com.gamelibrary.util;

import com.gamelibrary.exceptions.GameCreationException;
import com.gamelibrary.exceptions.GameNotFoundException;
import com.gamelibrary.exceptions.InputNonValidoException;
import com.gamelibrary.exceptions.PersistenzaException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Componente centralizzato per la gestione uniforme delle eccezioni applicative.
 * Opera una traduzione semantica degli errori tecnici (stack trace, eccezioni di basso livello)
 * in messaggi comprensibili destinati all'utente finale, preservando al contempo
 * la tracciabilità diagnostica completa nel sottosistema di logging.
 *
 * @author Martino Marrosu
 */
public final class ErrorHandler {

    private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());

    /** Prefisso standard anteposto ai messaggi di errore per evidenziazione visiva */
    private static final String PREFISSO_ERRORE = "⚠ Errore";

    /**
     * Inibisce l'instanziazione della classe utility mascherandone il costruttore di default.
     */
    private ErrorHandler() {
        throw new UnsupportedOperationException("Classe utility — non istanziabile.");
    }

    /**
     * Intercetta l'eccezione, ne determina il livello di severità, persiste lo stack trace
     * nel sottosistema di logging e produce un messaggio user-friendly per l'output a console.
     *
     * @param eccezione      l'anomalia intercettata dal chiamante
     * @param contestoUtente descrizione dell'operazione in corso al momento del fallimento
     */
    public static void gestisci(Exception eccezione, String contestoUtente) {
        if (eccezione == null) {
            LOGGER.warning("ErrorHandler.gestisci() chiamato con eccezione null.");
            return;
        }

        String messaggioUtente = generaMessaggioUtente(eccezione, contestoUtente);
        Level livelloLog = determinaLivelloLog(eccezione);

        // Log interno con stack trace completo (solo nel file di log, MAI in console)
        LOGGER.log(livelloLog,
                String.format("Errore durante %s: %s", contestoUtente, eccezione.getMessage()),
                eccezione);

        // Messaggio user-friendly all'utente (senza stack trace)
        System.out.println(messaggioUtente);
    }

    /**
     * Applica un mapping tra tipologia di eccezione e relativo messaggio localizzato,
     * isolando l'utente dai dettagli implementativi interni.
     *
     * @param eccezione      l'anomalia da tradurre
     * @param contestoUtente l'operazione di riferimento
     * @return la stringa formattata destinata all'output standard
     */
    private static String generaMessaggioUtente(Exception eccezione, String contestoUtente) {
        return switch (eccezione) {
            case InputNonValidoException e -> String.format("%s: input non valido durante %s. %s",
                    PREFISSO_ERRORE, contestoUtente, e.getMessage());
            case GameCreationException e -> String.format("%s nella %s: %s",
                    PREFISSO_ERRORE, contestoUtente, e.getMessage());
            case GameNotFoundException e -> String.format("%s: gioco non trovato durante %s. %s",
                    PREFISSO_ERRORE, contestoUtente, e.getMessage());
            case PersistenzaException ignored -> String.format(
                    "%s di sistema durante %s. I dati potrebbero non essere stati salvati. "
                            + "Riprova o controlla il file di configurazione.",
                    PREFISSO_ERRORE, contestoUtente);
            // Eccezione imprevista — messaggio generico per non esporre dettagli tecnici
            default -> String.format("%s imprevisto durante %s. "
                    + "Se il problema persiste, contatta il supporto.",
                    PREFISSO_ERRORE, contestoUtente);
        };
    }

    /**
     * Classifica la severità dell'anomalia distinguendo tra errori di validazione (WARNING),
     * condizioni applicative prevedibili (INFO) e guasti di sistema critici (SEVERE).
     *
     * @param eccezione l'anomalia da classificare
     * @return il livello di gravità corrispondente per il framework di logging
     */
    private static Level determinaLivelloLog(Exception eccezione) {
        return switch (eccezione) {
            case GameNotFoundException e ->
                // Non trovare un gioco è un caso d'uso normale, non un errore
                Level.INFO;
            case InputNonValidoException e ->
                // Dati non validi — errore dell'utente, non del sistema
                Level.WARNING;
            case GameCreationException e ->
                // Dati non validi — errore dell'utente, non del sistema
                Level.WARNING;
            case PersistenzaException e ->
                // Errore I/O — critico, potrebbe causare perdita di dati
                Level.SEVERE;
            default ->
                // Eccezione imprevista — potenziale bug, sempre SEVERE
                Level.SEVERE;
        };
    }
}
