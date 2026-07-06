package com.gamelibrary.observer;

import com.gamelibrary.model.Game;

/**
 * Contratto funzionale (Observer) da esporre ai client che necessitano di intercettare 
 * le variazioni di stato del sistema centrale. Promuove l'Inversion of Control: il
 * subject principale comunicherà l'avvenimento di un evento restando completamente disaccoppiato
 * dalle conseguenze derivate (es. aggiornamento DB, logging, interfacciamento UI).
 *
 * @author Martino Marrosu
 */
@FunctionalInterface
public interface OsservatoreLibreria {

    /**
     * Callback invocato attivamente all'insorgere di uno shift comportamentale o di stato.
     *
     * @param evento il tipo enumerato che classifica l'azione
     * @param gioco il riferimento all'oggetto coinvolto nel transitorio
     */
    void aggiorna(EventoLibreria evento, Game gioco);
}
