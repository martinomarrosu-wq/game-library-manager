package com.gamelibrary.persistence;

import com.gamelibrary.exceptions.PersistenzaException;
import com.gamelibrary.model.Game;

import java.util.List;

/**
 * Contratto logico che astrae le operazioni afferenti al layer di Data Access Object (DAO).
 * L'impiego dell'interfaccia favorisce il principio Open/Closed, abilitando l'innesto
 * di implementazioni future (es. Relational Database, NoSQL) mediante Dependency Injection.
 *
 * @author Martino Marrosu
 */
public interface Persistenza {

    /**
     * Consolida il dataset in memoria trasferendolo sul supporto di memorizzazione sottostante.
     *
     * @param giochi il grafo di oggetti da serializzare
     * @throws PersistenzaException qualora intercorrano anomalie a livello di I/O o di rete
     */
    void salva(List<Game> giochi) throws PersistenzaException;

    /**
     * Ricostruisce lo stato applicativo prelevando e deserializzando i dati dal layer persistente.
     *
     * @return l'insieme degli oggetti materializzati (allocando una lista vuota in caso di assenza dati)
     * @throws PersistenzaException qualora la lettura fallisca per questioni strutturali o di accesso
     */
    List<Game> carica() throws PersistenzaException;
}
