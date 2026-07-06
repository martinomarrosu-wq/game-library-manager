package com.gamelibrary.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotazione marker custom (Runtime Retention) destinata ai metodi di business primari.
 * Consente la rilevazione e l'elaborazione dei meta-dati associati da parte del processore reflection.
 *
 * @author Martino Marrosu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Operazione {
    /**
     * Fornisce una descrizione semantica sintetica dell'operazione contrassegnata.
     *
     * @return stringa esplicativa del metodo
     */
    String descrizione() default "Operazione di sistema";
}
