package com.gamelibrary;

import com.gamelibrary.service.GameLibraryService;
import com.gamelibrary.ui.MenuPrincipale;


import java.util.logging.Logger;

/**
 * Entry point primario dell'infrastruttura software.
 * Svolge compiti di bootstrap, configurazione dei subsistemi (es. Logging) 
 * e instanziazione del contesto applicativo (Dependency Injection rudimentale),
 * per poi cedere il controllo all'interfaccia utente.
 *
 * @author Martino Marrosu
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Inizializzazione preliminare del sottosistema di logging
        configurazionLogging();

        LOGGER.info("Avvio Game Library Manager...");

        try {
            // Instanziazione del contesto e risoluzione delle dipendenze (Service Layer)
            AppContext context = new AppContext();
            GameLibraryService service = context.getGameLibraryService();

            // Allocazione e avvio del modulo di interfaccia utente CLI
            MenuPrincipale menu = new MenuPrincipale(service);
            menu.avvia();
            
        } catch (Exception e) {
            LOGGER.severe("Errore critico durante l'esecuzione: " + e.getMessage());
            System.out.println("❌ Si è verificato un errore critico che ha causato l'arresto dell'applicazione.");
        }

        LOGGER.info("Game Library Manager chiuso.");
    }

    /**
     * Esegue il caricamento delle policy di logging prelevandole da file esterno.
     * In caso di indisponibilità della risorsa, si appoggia ai parametri di default.
     */
    private static void configurazionLogging() {
        try {
            var configFile = new java.io.File("logging.properties");
            if (configFile.exists()) {
                try (var fis = new java.io.FileInputStream(configFile)) {
                    java.util.logging.LogManager.getLogManager().readConfiguration(fis);
                }
            }
        } catch (Exception e) {
            // Tolleranza d'errore (Fallback): si procede con la configurazione di default del JRE
            System.err.println("Attenzione: impossibile caricare logging.properties, uso configurazione predefinita.");
        }
    }
}
