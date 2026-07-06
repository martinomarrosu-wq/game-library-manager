package com.gamelibrary.observer;

import com.gamelibrary.model.Game;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Implementazione concreta del pattern Observer incaricata della gestione dell'output informativo (Logging/CLI).
 * Intercetta passivamente i cambiamenti di stato notificati dal subject (Service) 
 * ed elabora un resoconto testuale formattato comprendente timestamp e indicatori semantici.
 *
 * @author Martino Marrosu
 */
public class GestoreNotifiche implements OsservatoreLibreria {

    private static final Logger LOGGER = Logger.getLogger(GestoreNotifiche.class.getName());

    /** Definisce la configurazione standard per la formattazione oraria (time-only) */
    private static final DateTimeFormatter FORMATO_ORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Variabile di appoggio preposta al tracciamento cumulativo delle notifiche intercettate */
    private int contatorNotifiche;

    /**
     * Costruttore di default. Si occupa di azzerare la metrica di monitoraggio interna.
     */
    public GestoreNotifiche() {
        this.contatorNotifiche = 0;
        LOGGER.info("GestoreNotifiche inizializzato.");
    }

    /**
     * Riceve asincronamente la notifica di un cambiamento di stato dal Subject.
     * Computa il log terminale corredando i dati con marca temporale e classificazione iconografica.
     *
     * @param evento la categorizzazione della modifica indotta
     * @param gioco l'istanza su cui si è registrato il mutamento
     */
    @Override
    public void aggiorna(EventoLibreria evento, Game gioco) {
        if (evento == null || gioco == null) {
            LOGGER.warning("Notifica ricevuta con evento o gioco nullo — ignorata.");
            return;
        }

        contatorNotifiche++;

        String timestamp = LocalDateTime.now().format(FORMATO_ORA);
        String icona = ottieniIcona(evento);

        String notifica = String.format("%s [%s] %s: \"%s\" (%s — %s)",
                icona,
                timestamp,
                evento.getDescrizione(),
                gioco.getTitolo(),
                gioco.getPiattaforma().getNomeVisualizzazione(),
                gioco.getGenere().getNomeVisualizzazione());

        System.out.println(notifica);

        LOGGER.info(() -> String.format("Notifica #%d inviata: %s per gioco '%s' (ID: %s)",
                contatorNotifiche, evento.name(), gioco.getTitolo(), gioco.getId()));
    }

    /**
     * Algoritmo decisionale per l'associazione tra tipo di evento e l'indicatore semantico visuale (Unicode Emoji).
     *
     * @param evento la tipologia di mutamento
     * @return la stringa rappresentante il glifo predefinito
     */
    private String ottieniIcona(EventoLibreria evento) {
        return switch (evento) {
            case GIOCO_AGGIUNTO -> "✅";
            case GIOCO_MODIFICATO -> "✏️";
            case GIOCO_RIMOSSO -> "🗑️";
        };
    }

    /**
     * Espone la lettura inerente alla totalità di messaggi distribuiti lungo l'intero ciclo vitale.
     *
     * @return il computo totale cumulato
     */
    public int getContatorNotifiche() {
        return contatorNotifiche;
    }
}
