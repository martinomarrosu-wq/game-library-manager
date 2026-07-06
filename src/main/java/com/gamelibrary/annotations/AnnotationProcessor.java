package com.gamelibrary.annotations;

import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Utility per l'analisi del bytecode a runtime (Reflection).
 * Esegue lo scanning di una classe target al fine di individuare e tracciare
 * i metodi contrassegnati con l'annotazione custom @Operazione.
 *
 * @author Martino Marrosu
 */
public class AnnotationProcessor {

    private static final Logger LOGGER = Logger.getLogger(AnnotationProcessor.class.getName());

    /**
     * Ispeziona la struttura di una classe a runtime estraendone i metodi dichiarati.
     * In presenza dell'annotazione preposta, provvede al log delle informazioni correlate.
     *
     * @param classeTarget il reference alla classe (Class type) da sottoporre ad ispezione
     */
    public static void analizzaOperazioni(Class<?> classeTarget) {
        if (classeTarget == null) {
            LOGGER.warning("Classe target nulla passata al processore di annotazioni.");
            return;
        }

        LOGGER.info(() -> String.format("--- Avvio analisi Reflection su %s ---", classeTarget.getSimpleName()));
        
        int conteggio = 0;
        
        try {
            // Recupero dell'array dei metodi tramite Reflection API
            Method[] metodi = classeTarget.getDeclaredMethods();
            
            for (Method metodo : metodi) {
                // Controllo della presenza dell'annotazione target
                if (metodo.isAnnotationPresent(Operazione.class)) {
                    Operazione annotazione = metodo.getAnnotation(Operazione.class);
                    String descrizione = annotazione.descrizione();
                    
                    LOGGER.info(() -> String.format(
                            "Trovata operazione tracciata: %s() -> '%s'",
                            metodo.getName(), descrizione));
                            
                    conteggio++;
                }
            }
            
            LOGGER.info(String.format(
                    "--- Analisi completata. Trovate %d operazioni in %s ---",
                    conteggio, classeTarget.getSimpleName()));
                    
        } catch (SecurityException e) {
            LOGGER.severe("Errore di sicurezza durante l'analisi Reflection: " + e.getMessage());
        }
    }
}
