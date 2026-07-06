package com.gamelibrary.util;

import java.util.Optional;

/**
 * Wrapper generico che incapsula l'esito di un'operazione di business, distinguendo
 * fra risultato positivo (con payload) e fallimento (con messaggio diagnostico).
 * Sopperisce ai limiti espressivi di Optional, il quale non consente di veicolare
 * informazioni contestuali in caso di assenza del valore atteso.
 * Dimostra inoltre l'applicazione pratica dei Generics di Java.
 *
 * @param <T> il tipo parametrico del valore restituito in caso di successo
 * @author Martino Marrosu
 */
public class Risultato<T> {

    private final T valore;
    private final String messaggioErrore;
    private final boolean successo;

    /**
     * Costruttore privato. L'instanziazione è riservata ai factory method statici.
     */
    private Risultato(T valore, String messaggioErrore, boolean successo) {
        this.valore = valore;
        this.messaggioErrore = messaggioErrore;
        this.successo = successo;
    }

    /**
     * Factory method per la costruzione di un esito positivo.
     *
     * @param valore il payload prodotto dall'operazione
     * @param <T>    il tipo parametrico
     * @return un Risultato in stato di successo
     */
    public static <T> Risultato<T> successo(T valore) {
        return new Risultato<>(valore, null, true);
    }

    /**
     * Factory method per la costruzione di un esito negativo.
     *
     * @param messaggioErrore la descrizione formale dell'anomalia riscontrata
     * @param <T>             il tipo parametrico
     * @return un Risultato in stato di fallimento
     */
    public static <T> Risultato<T> fallimento(String messaggioErrore) {
        return new Risultato<>(null, messaggioErrore, false);
    }

    /**
     * Indica se l'operazione si è conclusa con esito positivo.
     *
     * @return true in caso di successo
     */
    public boolean isSuccesso() {
        return successo;
    }

    /**
     * Indica se l'operazione si è conclusa con esito negativo.
     *
     * @return true in caso di fallimento
     */
    public boolean isFallimento() {
        return !successo;
    }

    /**
     * Restituisce il payload associato all'operazione riuscita.
     *
     * @return l'oggetto prodotto, oppure null in caso di fallimento
     */
    public T getValore() {
        return valore;
    }

    /**
     * Converte il risultato in un Optional, agevolando la composizione con le Stream API.
     *
     * @return un Optional contenente il valore, o empty in assenza
     */
    public Optional<T> toOptional() {
        return Optional.ofNullable(valore);
    }

    /**
     * Espone il messaggio diagnostico associato al fallimento.
     *
     * @return la stringa esplicativa, oppure null in caso di successo
     */
    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    @Override
    public String toString() {
        if (successo) {
            return String.format("Risultato[successo: %s]", valore);
        }
        return String.format("Risultato[fallimento: %s]", messaggioErrore);
    }
}
