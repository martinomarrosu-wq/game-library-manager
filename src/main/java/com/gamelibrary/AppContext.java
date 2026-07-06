package com.gamelibrary;

import com.gamelibrary.annotations.AnnotationProcessor;
import com.gamelibrary.memento.CaretakerStorico;
import com.gamelibrary.observer.GestoreNotifiche;
import com.gamelibrary.persistence.JsonPersistenza;
import com.gamelibrary.persistence.Persistenza;
import com.gamelibrary.service.GameLibraryService;

import java.util.logging.Logger;

/**
 * Componente responsabile dell'Inversion of Control (IoC) e del wiring delle dipendenze (Dependency Injection).
 * Centralizza l'instanziazione e il collegamento dei moduli applicativi (Service, Persistence, Memento Caretaker),
 * promuovendo il disaccoppiamento e alleggerendo l'entry point (Main).
 *
 * @author Martino Marrosu
 */
public class AppContext {

    private static final Logger LOGGER = Logger.getLogger(AppContext.class.getName());

    private final GameLibraryService gameLibraryService;

    /**
     * Costruttore di default. Si occupa di inizializzare l'intero grafo delle dipendenze.
     */
    public AppContext() {
        LOGGER.info("Inizializzazione del contesto dell'applicazione (IoC)...");
        
        // Instanziazione dei layer infrastrutturali
        Persistenza persistenza = new JsonPersistenza("library.json");
        CaretakerStorico storico = new CaretakerStorico();
        
        // Iniezione delle dipendenze nel Core Service
        this.gameLibraryService = new GameLibraryService(persistenza, storico);
        
        // Registrazione pattern Observer
        GestoreNotifiche gestoreNotifiche = new GestoreNotifiche();
        this.gameLibraryService.registraOsservatore(gestoreNotifiche);
        
        // Risoluzione a runtime via Reflection delle annotazioni applicative
        AnnotationProcessor.analizzaOperazioni(GameLibraryService.class);
        
        LOGGER.info("Contesto dell'applicazione inizializzato con successo.");
    }

    /**
     * Espone l'istanza del Service Layer correttamente configurata.
     *
     * @return riferimento al GameLibraryService
     */
    public GameLibraryService getGameLibraryService() {
        return gameLibraryService;
    }
}
