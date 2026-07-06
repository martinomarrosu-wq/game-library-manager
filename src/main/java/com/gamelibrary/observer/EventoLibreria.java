package com.gamelibrary.observer;

/**
 * Tipo enumerato preposto alla classificazione tipologica degli eventi scatenati
 * all'interno del perimetro applicativo. Promuove l'impiego della Type Safety a
 * compilazione limitando le hardcoded strings durante lo scambio di messaggi tra Subject e Observer.
 *
 * @author Martino Marrosu
 */
public enum EventoLibreria {

    /** Rappresenta l'operazione di prima iscrizione di una risorsa in libreria */
    GIOCO_AGGIUNTO("Gioco aggiunto"),

    /** Rappresenta l'operazione di epurazione logica e fisica di una risorsa */
    GIOCO_RIMOSSO("Gioco rimosso"),

    /** Rappresenta l'operazione di persistenza a seguito di un'alterazione dello stato di una risorsa */
    GIOCO_MODIFICATO("Gioco modificato");

    private final String descrizione;

    /**
     * Instanzia il record enumerato associandovi la stringa di supporto per la reportistica UI/CLI.
     *
     * @param descrizione il nome identificativo formato human-readable
     */
    EventoLibreria(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * Espone l'etichetta associata al fine di utilizzarla in contesti applicativi e di log.
     *
     * @return il testo in forma esplicita
     */
    public String getDescrizione() {
        return descrizione;
    }

    @Override
    public String toString() {
        return descrizione;
    }
}
