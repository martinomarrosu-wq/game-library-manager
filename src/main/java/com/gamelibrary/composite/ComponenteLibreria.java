package com.gamelibrary.composite;

/**
 * Interfaccia di astrazione per il Component designato dal pattern architetturale Composite.
 * Espone un protocollo uniforme sia per le strutture aggregate (nodi complessi) 
 * sia per i singoli elementi (foglie scalari).
 *
 * @author Martino Marrosu
 */
public interface ComponenteLibreria {

    /**
     * Espone in sola lettura il nome identificativo del componente logico.
     *
     * @return stringa rappresentante il nome
     */
    String getNome();

    /**
     * Delegato alla formattazione e visualizzazione dell'intera struttura ad albero,
     * supportando l'astrazione su livelli multipli di profondità.
     *
     * @param livello fattore intero utile a calcolare il distanziamento indentativo
     */
    void mostra(int livello);

    /**
     * Operazione di associazione di un nuovo nodo/foglia in subordine all'entità corrente.
     *
     * @param componente l'entità target dell'associazione
     */
    void aggiungi(ComponenteLibreria componente);

    /**
     * Operazione di dissociazione gerarchica per un elemento attualmente incluso nell'albero.
     *
     * @param componente il riferimento da evincere
     */
    void rimuovi(ComponenteLibreria componente);

    /**
     * Computa il quantitativo esatto di istanze atomiche presenti al di sotto del ramo corrente.
     *
     * @return l'accumulatore intero dei nodi di tipo foglia
     */
    int contaGiochi();
}
