package com.gamelibrary;

import com.gamelibrary.annotations.AnnotationProcessor;
import com.gamelibrary.memento.CaretakerStorico;
import com.gamelibrary.observer.GestoreNotifiche;
import com.gamelibrary.persistence.JsonPersistenza;
import com.gamelibrary.persistence.Persistenza;
import com.gamelibrary.service.GameLibraryService;

import java.util.logging.Logger;

/**
 * Componente responsabile dell'Inversion of Control (IoC) e del wiring delle
 * dipendenze (Dependency Injection).
 * Centralizza l'instanziazione e il collegamento dei moduli applicativi
 * (Service, Persistence, Memento Caretaker),
 * promuovendo il disaccoppiamento e alleggerendo l'entry point (Main).
 *
 * @author Martino Marrosu
 */
public class AppContext {

    private static final Logger LOGGER = Logger.getLogger(AppContext.class.getName());

    private final GameLibraryService gameLibraryService;

    /**
     * Costruttore di default. Si occupa di inizializzare l'intero grafo delle
     * dipendenze.
     */
    public AppContext() {
        LOGGER.info("Inizializzazione del contesto dell'applicazione (IoC)...");

        // Instanziazione dei layer infrastrutturali
        Persistenza persistenza = new JsonPersistenza(resolvePersistenzaPath("library.json"));
        CaretakerStorico storico = new CaretakerStorico();

        // Iniezione delle dipendenze nel Core Service
        this.gameLibraryService = new GameLibraryService(persistenza, storico);

        // Registrazione pattern Observer
        GestoreNotifiche gestoreNotifiche = new GestoreNotifiche();
        this.gameLibraryService.registraOsservatore(gestoreNotifiche);

        try {
            this.gameLibraryService.carica();
            LOGGER.info("Libreria caricata automaticamente dal supporto persistente.");
        } catch (Exception e) {
            LOGGER.warning(() -> String.format("Impossibile caricare la libreria al bootstrap: %s", e.getMessage()));
        }

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

    private String resolvePersistenzaPath(String percorso) {
        if (percorso == null || percorso.isBlank()) {
            return "library.json";
        }

        java.nio.file.Path path = java.nio.file.Path.of(percorso);
        if (path.isAbsolute()) {
            return path.toString();
        }

        java.nio.file.Path workingDir = java.nio.file.Path.of(System.getProperty("user.dir", ".")).toAbsolutePath()
                .normalize();
        java.nio.file.Path resolved = workingDir.resolve(path).normalize();

        if (java.nio.file.Files.exists(resolved)) {
            return resolved.toString();
        }

        java.nio.file.Path workspaceRoot = workingDir.getParent();
        if (workspaceRoot != null) {
            java.nio.file.Path fallback = workspaceRoot.resolve(path).normalize();
            if (java.nio.file.Files.exists(fallback)) {
                return fallback.toString();
            }
        }

        return resolved.toString();
    }
}
