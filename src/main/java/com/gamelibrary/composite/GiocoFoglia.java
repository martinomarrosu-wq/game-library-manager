package com.gamelibrary.composite;

import com.gamelibrary.model.Game;

import java.util.Objects;

/**
 * Implementazione della classe Foglia (Leaf) all'interno dell'architettura Composite.
 * Rappresenta l'elemento atomico e terminale della gerarchia (un singolo videogioco), 
 * il quale per definizione strutturale non può possedere a sua volta dei nodi figli.
 *
 * @author Martino Marrosu
 */
public class GiocoFoglia implements ComponenteLibreria {

    private final Game gioco;

    /**
     * Inizializza il componente associandovi un'istanza concreta di tipo Game.
     *
     * @param gioco il riferimento al gioco da incapsulare
     * @throws IllegalArgumentException qualora il parametro fornito sia nullo
     */
    public GiocoFoglia(Game gioco) {
        if (gioco == null) {
            throw new IllegalArgumentException("Il gioco non può essere nullo.");
        }
        this.gioco = gioco;
    }

    /**
     * Espone l'oggetto Game sottostante incapsulato dalla foglia.
     *
     * @return l'istanza del gioco
     */
    public Game getGioco() {
        return gioco;
    }

    /**
     * Restituisce l'identificativo nominativo del componente, derivandolo direttamente dal titolo del gioco.
     *
     * @return il titolo del videogioco
     */
    @Override
    public String getNome() {
        return gioco.getTitolo();
    }

    /**
     * Produce una formattazione testuale dell'elemento per l'output, 
     * applicando una spaziatura calcolata in base alla profondità gerarchica.
     *
     * @param livello il grado di annidamento utilizzato per calcolare l'indentazione
     */
    @Override
    public void mostra(int livello) {
        String indentazione = "  ".repeat(livello);
        System.out.printf("%s🎮 %s%n", indentazione, gioco.toString());
    }

    /**
     * Operazione non supportata. L'architettura vieta logicamente l'inserimento di figli a un nodo terminale.
     *
     * @param componente l'elemento da non aggiungere
     * @throws UnsupportedOperationException sistematicamente al fine di preservare l'integrità strutturale
     */
    @Override
    public void aggiungi(ComponenteLibreria componente) {
        throw new UnsupportedOperationException(
                "Impossibile aggiungere componenti a una foglia (gioco singolo).");
    }

    /**
     * Operazione non supportata, in quanto una foglia è priva di discendenti per definizione.
     *
     * @param componente l'elemento da non rimuovere
     * @throws UnsupportedOperationException in ottemperanza ai vincoli del design pattern
     */
    @Override
    public void rimuovi(ComponenteLibreria componente) {
        throw new UnsupportedOperationException(
                "Impossibile rimuovere componenti da una foglia (gioco singolo).");
    }

    /**
     * Restituisce il conteggio degli elementi terminali, il quale corrisponde costantemente all'unità.
     *
     * @return 1, essendo un singolo nodo di tipo Game
     */
    @Override
    public int contaGiochi() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GiocoFoglia that = (GiocoFoglia) o;
        return Objects.equals(gioco, that.gioco);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gioco);
    }

    @Override
    public String toString() {
        return String.format("GiocoFoglia[%s]", gioco.getTitolo());
    }
}
