package com.gamelibrary.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Implementazione del nodo composito per il design pattern Composite.
 * Rappresenta un contenitore logico (simile a una directory o cartella) che
 * può annidare al proprio interno sia foglie (singoli giochi) che altri nodi (collezioni).
 * Garantisce un trattamento uniforme per l'intera gerarchia, supportando 
 * operazioni ricorsive come il conteggio dei figli.
 *
 * @author Martino Marrosu
 */
public class CollezioneGiochi implements ComponenteLibreria {

    private static final Logger LOGGER = Logger.getLogger(CollezioneGiochi.class.getName());

    private final String nome;
    private final List<ComponenteLibreria> componenti;

    /**
     * Inizializza un'istanza vuota associandovi un identificativo nominativo.
     *
     * @param nome la stringa alfanumerica rappresentante il nome
     * @throws IllegalArgumentException in caso di parametro nullo o puramente composto da whitespaces
     */
    public CollezioneGiochi(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Il nome della collezione non può essere nullo o vuoto.");
        }
        this.nome = nome.trim();
        this.componenti = new ArrayList<>();
    }

    /**
     * Recupera il nome identificativo assegnato alla collezione.
     *
     * @return il nome della collezione
     */
    @Override
    public String getNome() {
        return nome;
    }

    /**
     * Produce una rappresentazione testuale formattata della collezione corrente
     * e, ricorsivamente, del suo intero albero di discendenti, applicando un'indentazione
     * basata sulla profondità gerarchica.
     *
     * @param livello il livello di profondità corrente (utilizzato per il fattore di indentazione)
     */
    @Override
    public void mostra(int livello) {
        String indentazione = "  ".repeat(livello);
        int totaleGiochi = contaGiochi();
        String suffisso = totaleGiochi == 1 ? "gioco" : "giochi";

        System.out.printf("%s📁 %s (%d %s)%n", indentazione, nome, totaleGiochi, suffisso);

        // Chiamata ricorsiva su ogni figlio — questo è il cuore del Composite Pattern
        for (ComponenteLibreria componente : componenti) {
            componente.mostra(livello + 1);
        }
    }

    /**
     * Accorpa un nuovo componente (nodo o foglia) come figlio diretto.
     *
     * @param componente l'entità da aggiungere alla collezione
     * @throws IllegalArgumentException in caso si passi un reference nullo
     */
    @Override
    public void aggiungi(ComponenteLibreria componente) {
        if (componente == null) {
            throw new IllegalArgumentException("Il componente da aggiungere non può essere nullo.");
        }
        componenti.add(componente);
        LOGGER.info(() -> String.format("Aggiunto '%s' alla collezione '%s'.",
                componente.getNome(), this.nome));
    }

    /**
     * Dissocia e rimuove un componente specifico dall'elenco dei figli diretti.
     *
     * @param componente l'entità bersaglio da epurare
     * @throws IllegalArgumentException in caso si passi un reference nullo
     */
    @Override
    public void rimuovi(ComponenteLibreria componente) {
        if (componente == null) {
            throw new IllegalArgumentException("Il componente da rimuovere non può essere nullo.");
        }
        boolean rimosso = componenti.remove(componente);
        if (rimosso) {
            LOGGER.info(() -> String.format("Rimosso '%s' dalla collezione '%s'.",
                    componente.getNome(), this.nome));
        } else {
            LOGGER.warning(() -> String.format("Componente '%s' non trovato nella collezione '%s'.",
                    componente.getNome(), this.nome));
        }
    }

    /**
     * Computa in modo ricorsivo il quantitativo totale di foglie (giochi) in questo nodo e nei sottonodi.
     *
     * @return la sommatoria matematica degli elementi terminali presenti
     */
    @Override
    public int contaGiochi() {
        int conteggio = 0;
        for (ComponenteLibreria componente : componenti) {
            conteggio += componente.contaGiochi();
        }
        return conteggio;
    }

    /**
     * Restituisce i riferimenti ai nodi figli immediati. Si avvale di una copia 
     * difensiva della lista nativa per tutelarne l'incapsulamento originario.
     *
     * @return la lista clonata dei componenti discendenti
     */
    public List<ComponenteLibreria> getComponenti() {
        return new ArrayList<>(componenti);
    }

    /**
     * Verifica l'assenza di discendenti primari (lista componenti vuota).
     *
     * @return true in assenza di figli, false contrariamente
     */
    public boolean isVuota() {
        return componenti.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollezioneGiochi that = (CollezioneGiochi) o;
        return Objects.equals(nome, that.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome);
    }

    @Override
    public String toString() {
        return String.format("CollezioneGiochi[%s, %d componenti]", nome, componenti.size());
    }
}
