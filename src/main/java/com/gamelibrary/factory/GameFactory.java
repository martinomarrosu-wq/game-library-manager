package com.gamelibrary.factory;

import com.gamelibrary.exceptions.GameCreationException;
import com.gamelibrary.model.Game;
import com.gamelibrary.model.Genere;
import com.gamelibrary.model.Piattaforma;

import java.util.logging.Logger;

/**
 * Implementazione del Factory Method Pattern finalizzata alla creazione sicura di istanze Game.
 * Centralizza l'instanziazione incapsulando il Builder sottostante. Inoltre, funge da layer di
 * traslazione architetturale per le eccezioni, effettuando un wrapping in eccezioni di dominio custom.
 *
 * @author Martino Marrosu
 */
public final class GameFactory {

    private static final Logger LOGGER = Logger.getLogger(GameFactory.class.getName());

    /**
     * Occulta il costruttore pubblico per inibire l'instanziazione della classe utility.
     */
    private GameFactory() {
        throw new UnsupportedOperationException("Classe factory — non istanziabile.");
    }

    /**
     * Coordina il flusso di instanziazione e validazione di un nuovo Game.
     * Delega l'assegnazione automatica dell'identificativo al sotto-costruttore.
     *
     * @param titolo la denominazione dell'opera
     * @param piattaforma il sistema hardware di riferimento
     * @param genere la classificazione tipologica
     * @param anno la finestra temporale di commercializzazione
     * @param voto la valutazione espressa in scala decimale
     * @return la nuova istanza solidificata
     * @throws GameCreationException qualora l'operazione violi le regole di business validation
     */
    public static Game crea(String titolo, Piattaforma piattaforma, Genere genere,
                            int anno, int voto) throws GameCreationException {
        LOGGER.info(() -> String.format("Tentativo di creazione gioco: titolo='%s', piattaforma=%s, "
                + "genere=%s, anno=%d, voto=%d", titolo, piattaforma, genere, anno, voto));

        try {
            Game gioco = new Game.Builder(titolo)
                    .piattaforma(piattaforma)
                    .genere(genere)
                    .anno(anno)
                    .voto(voto)
                    .build();

            LOGGER.info(() -> String.format("Gioco creato con successo: %s (ID: %s)",
                    gioco.getTitolo(), gioco.getId()));

            return gioco;

        } catch (IllegalArgumentException e) {
            // wrappa l'eccezione in una di dominio
            LOGGER.warning(() -> String.format("Creazione gioco fallita per '%s': %s",
                    titolo, e.getMessage()));
            throw new GameCreationException(
                    String.format("Impossibile creare il gioco '%s': %s", titolo, e.getMessage()), e);
        }
    }
}
