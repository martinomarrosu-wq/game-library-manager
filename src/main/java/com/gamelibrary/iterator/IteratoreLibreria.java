package com.gamelibrary.iterator;

/**
 * Interfaccia customizzata per l'implementazione del design pattern Iterator.
 * Sebbene si potesse adottare l'interfaccia nativa java.util.Iterator, si è 
 * scelto di definirne una proprietaria per implementare la funzionalità estesa 
 * di reset() del cursore e mantenere la nomenclatura in lingua italiana, in accordo 
 * con il resto del dominio applicativo.
 * L'uso dei Generics (<T>) garantisce il riutilizzo dell'interfaccia.
 *
 * @param <T> il tipo di elemento su cui si itera
 * @author Martino Marrosu
 */
public interface IteratoreLibreria<T> {

    /**
     * Verifica la presenza di un ulteriore elemento non ancora iterato.
     *
     * @return true se è presente un elemento successivo, false altrimenti
     */
    boolean hasProssimo();

    /**
     * Restituisce l'elemento corrente e avanza il cursore dell'iteratore.
     *
     * @return l'elemento successivo della collezione
     * @throws java.util.NoSuchElementException qualora si tenti un accesso oltre i limiti della collezione
     */
    T prossimo();

    /**
     * Reimposta lo stato dell'iteratore al primo elemento della collezione.
     */
    void reset();
}
