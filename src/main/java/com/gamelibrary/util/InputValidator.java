package com.gamelibrary.util;

import com.gamelibrary.exceptions.InputNonValidoException;
import com.gamelibrary.model.Game;
import com.gamelibrary.model.Genere;
import com.gamelibrary.model.Piattaforma;

import java.time.Year;
import java.util.logging.Logger;

/**
 * Modulo di utility delegato alla validazione formale e semantica dell'input proveniente dalla Boundary (UI).
 * Raccoglie la logica di controllo (Validation Logic) evitando la proliferazione 
 * di blocchi condizionali (if-statements) disordinati all'interno delle classi client.
 * Trasla i difetti formali in apposite eccezioni custom (InputNonValidoException).
 *
 * @author Martino Marrosu
 */
public final class InputValidator {

    private static final Logger LOGGER = Logger.getLogger(InputValidator.class.getName());

    /**
     * Scongiura l'instanziazione della classe utility mascherandone il costruttore di default.
     */
    private InputValidator() {
        throw new UnsupportedOperationException("Classe utility — non istanziabile.");
    }

    /**
     * Valida l'immissione di una stringa, verificandone la presenza effettiva (not null / not empty).
     *
     * @param valore la stringa in ingresso sottomessa dall'utente
     * @param nomeCampo identificativo nominale del parametro (per chiarezza nel log)
     * @return la stringa soggetta a operazione di trim
     * @throws InputNonValidoException qualora l'ingresso consti in spaziature vuote o sia nullo
     */
    public static String validaStringa(String valore, String nomeCampo) {
        if (valore == null || valore.isBlank()) {
            LOGGER.warning(() -> String.format("Validazione fallita per '%s': valore nullo o vuoto.", nomeCampo));
            throw new InputNonValidoException(
                    String.format("Il campo '%s' non può essere vuoto.", nomeCampo));
        }
        return valore.trim();
    }

    /**
     * Sottopone a parsificazione ed ispezione temporale l'anno digitato.
     *
     * @param input il frammento testuale sottomesso
     * @return il valore intero rappresentante l'anno
     * @throws InputNonValidoException se si inseriscono formati illegali o valori fuori dai vincoli storici stabiliti
     */
    public static int validaAnno(String input) {
        int anno;
        try {
            anno = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            LOGGER.warning(() -> String.format("Validazione anno fallita: '%s' non è un numero.", input));
            throw new InputNonValidoException(
                    String.format("'%s' non è un anno valido. Inserisci un numero intero.", input), e);
        }

        int annoCorrente = Year.now().getValue();
        if (anno < Game.ANNO_MINIMO || anno > annoCorrente) {
            LOGGER.warning(() -> String.format("Anno fuori range: %d (atteso %d-%d).",
                    anno, Game.ANNO_MINIMO, annoCorrente));
            throw new InputNonValidoException(
                    String.format("L'anno deve essere compreso tra %d e %d.",
                            Game.ANNO_MINIMO, annoCorrente));
        }
        return anno;
    }

    /**
     * Parsifica e valida la scala decimale assegnata come valutazione (Range ristretto 1-10).
     *
     * @param input il frammento testuale conenente la votazione
     * @return la valutazione intera convertita
     * @throws InputNonValidoException in caso di formati stringa invalidi o score fuori soglia
     */
    public static int validaVoto(String input) {
        int voto;
        try {
            voto = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            LOGGER.warning(() -> String.format("Validazione voto fallita: '%s' non è un numero.", input));
            throw new InputNonValidoException(
                    String.format("'%s' non è un voto valido. Inserisci un numero intero.", input), e);
        }

        if (voto < Game.VOTO_MINIMO || voto > Game.VOTO_MASSIMO) {
            LOGGER.warning(() -> String.format("Voto fuori range: %d (atteso %d-%d).",
                    voto, Game.VOTO_MINIMO, Game.VOTO_MASSIMO));
            throw new InputNonValidoException(
                    String.format("Il voto deve essere compreso tra %d e %d.",
                            Game.VOTO_MINIMO, Game.VOTO_MASSIMO));
        }
        return voto;
    }

    /**
     * Traduce l'indice inserito tramite interfaccia a riga di comando nella corrispondente costante Enumerata Piattaforma.
     *
     * @param input la cifra inserita dall'operatore
     * @return l'instanza tipizzata di Piattaforma associata
     * @throws InputNonValidoException se l'indice esula dalle opzioni supportate o non è numerico
     */
    public static Piattaforma validaPiattaforma(String input) {
        int scelta;
        try {
            scelta = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            LOGGER.warning(() -> String.format("Validazione piattaforma fallita: '%s' non è un numero.", input));
            throw new InputNonValidoException(
                    String.format("'%s' non è una scelta valida. Inserisci un numero.", input), e);
        }

        Piattaforma[] valori = Piattaforma.values();
        if (scelta < 1 || scelta > valori.length) {
            throw new InputNonValidoException(
                    String.format("Scelta non valida. Inserisci un numero tra 1 e %d.", valori.length));
        }
        return valori[scelta - 1];
    }

    /**
     * Traduce l'indice inserito tramite interfaccia a riga di comando nella corrispondente costante Enumerata Genere.
     *
     * @param input la cifra inserita dall'operatore
     * @return l'istanza tipizzata di Genere associata
     * @throws InputNonValidoException se l'indice esula dalle opzioni supportate o non è numerico
     */
    public static Genere validaGenere(String input) {
        int scelta;
        try {
            scelta = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            throw new InputNonValidoException(
                    String.format("'%s' non è una scelta valida. Inserisci un numero.", input), e);
        }

        Genere[] valori = Genere.values();
        if (scelta < 1 || scelta > valori.length) {
            throw new InputNonValidoException(
                    String.format("Scelta non valida. Inserisci un numero tra 1 e %d.", valori.length));
        }
        return valori[scelta - 1];
    }

    /**
     * Applica un vincolo generalizzato e parametrizzabile su range numerici di selezione menù.
     *
     * @param input l'ingresso numerico espresso in stringa
     * @param min il vincolo inferiore della selezione (incluso)
     * @param max il vincolo superiore della selezione (incluso)
     * @return il valore intero conforme ai canoni imposti
     * @throws InputNonValidoException se si attesta violazione dell'intervallo [min, max] o parse fallito
     */
    public static int validaSceltaMenu(String input, int min, int max) {
        int scelta;
        try {
            scelta = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            throw new InputNonValidoException(
                    String.format("'%s' non è una scelta valida. Inserisci un numero tra %d e %d.",
                            input, min, max),
                    e);
        }

        if (scelta < min || scelta > max) {
            throw new InputNonValidoException(
                    String.format("Scelta non valida. Inserisci un numero tra %d e %d.", min, max));
        }
        return scelta;
    }
}
