package com.gamelibrary.model;

import java.time.Year;
import java.util.Objects;
import java.util.UUID;

/**
 * Rappresentazione formale (Entity) dell'entità dominio videogioco.
 * Incapsula le proprietà strutturali (titolo, piattaforma, genere, anno, voto) 
 * e adotta il pattern architetturale Builder per governarne il processo di instanziazione,
 * garantendo così lo stato di consistenza iniziale dei dati.
 *
 * @author Martino Marrosu
 */
public class Game {

    // Soglia inferiore per l'anno di pubblicazione (antecedente all'era videoludica)
    public static final int ANNO_MINIMO = 1970;

    // Estremo inferiore della scala di valutazione
    public static final int VOTO_MINIMO = 1;

    // Estremo superiore della scala di valutazione
    public static final int VOTO_MASSIMO = 10;

    private final String id;
    private String titolo;
    private Piattaforma piattaforma;
    private Genere genere;
    private int anno;
    private int voto;

    /**
     * Inizializzazione protetta riservata unicamente al Builder associato.
     * Il confinamento garantisce il transito obbligato attraverso la logica di validazione pre-costruzione.
     *
     * @param builder l'istanza Builder configurata e validata
     */
    private Game(Builder builder) {
        this.id = builder.id;
        this.titolo = builder.titolo;
        this.piattaforma = builder.piattaforma;
        this.genere = builder.genere;
        this.anno = builder.anno;
        this.voto = builder.voto;
    }

    // getter

    public String getId() {
        return id;
    }

    public String getTitolo() {
        return titolo;
    }

    public Piattaforma getPiattaforma() {
        return piattaforma;
    }

    public Genere getGenere() {
        return genere;
    }

    public int getAnno() {
        return anno;
    }

    public int getVoto() {
        return voto;
    }

    // Mutatori — necessari anche per il ripristino dello stato tramite Memento

    public void setTitolo(String titolo) {
        if (titolo == null || titolo.isBlank()) {
            throw new IllegalArgumentException("Il titolo non può essere nullo o vuoto.");
        }
        this.titolo = titolo.trim();
    }

    public void setPiattaforma(Piattaforma piattaforma) {
        if (piattaforma == null) {
            throw new IllegalArgumentException("La piattaforma non può essere nulla.");
        }
        this.piattaforma = piattaforma;
    }

    public void setGenere(Genere genere) {
        if (genere == null) {
            throw new IllegalArgumentException("Il genere non può essere nullo.");
        }
        this.genere = genere;
    }

    public void setAnno(int anno) {
        int annoCorrente = Year.now().getValue();
        if (anno < ANNO_MINIMO || anno > annoCorrente) {
            throw new IllegalArgumentException(
                    String.format("L'anno deve essere compreso tra %d e %d.", ANNO_MINIMO, annoCorrente));
        }
        this.anno = anno;
    }

    public void setVoto(int voto) {
        if (voto < VOTO_MINIMO || voto > VOTO_MASSIMO) {
            throw new IllegalArgumentException(
                    String.format("Il voto deve essere compreso tra %d e %d.", VOTO_MINIMO, VOTO_MASSIMO));
        }
        this.voto = voto;
    }

    // Supporto al pattern Memento per la funzionalità di Undo

    /**
     * Produce un'istantanea storicizzata dello stato attributivo corrente.
     * Si pone come Originator nel contesto del pattern Memento per le funzionalità di ripristino.
     *
     * @return oggetto Memento contenente i campi serializzati
     */
    public GameMemento salvaStato() {
        return new GameMemento(id, titolo, piattaforma, genere, anno, voto);
    }

    /**
     * Sovrascrive lo stato transazionale ripristinandolo dai dati conservati in un Memento.
     * Mantiene l'integrità sull'identificativo primario (ID) che risulta immutabile.
     *
     * @param memento il contenitore dello stato salvato in precedenza
     * @throws IllegalArgumentException se il parametro è nullo o appartiene a un'istanza divergente
     */
    public void ripristinaStato(GameMemento memento) {
        if (memento == null) {
            throw new IllegalArgumentException("Il memento non può essere nullo.");
        }
        if (!this.id.equals(memento.getId())) {
            throw new IllegalArgumentException("Il memento appartiene a un gioco diverso.");
        }
        this.titolo = memento.getTitolo();
        this.piattaforma = memento.getPiattaforma();
        this.genere = memento.getGenere();
        this.anno = memento.getAnno();
        this.voto = memento.getVoto();
    }

    // equals, hashCode, toString

    /**
     * Implementazione del confronto per uguaglianza logica (Identity equality basata sull'ID).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(id, game.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Produce una rappresentazione sintetica formattata degli attributi, adoperata per output CLI.
     *
     * @return sequenza alfanumerica descrittiva
     */
    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s | %d | Voto: %d/10",
                id.substring(0, 8), // mostra solo i primi 8 caratteri dell'UUID per leggibilità
                titolo,
                piattaforma.getNomeVisualizzazione(),
                genere.getNomeVisualizzazione(),
                anno,
                voto);
    }

    // Inner class statica per il pattern Memento

    /**
     * Entità Memento (Inner Class statica). Il suo scopo è congelare 
     * lo stato puntuale di un oggetto Game per successivi ripristini.
     */
    public static class GameMemento {

        private final String id;
        private final String titolo;
        private final Piattaforma piattaforma;
        private final Genere genere;
        private final int anno;
        private final int voto;

        /**
         * Visibilità package-private per aderire all'incapsulamento richiesto dal Memento Pattern.
         */
        GameMemento(String id, String titolo, Piattaforma piattaforma,
                    Genere genere, int anno, int voto) {
            this.id = id;
            this.titolo = titolo;
            this.piattaforma = piattaforma;
            this.genere = genere;
            this.anno = anno;
            this.voto = voto;
        }

        public String getId() { return id; }
        public String getTitolo() { return titolo; }
        public Piattaforma getPiattaforma() { return piattaforma; }
        public Genere getGenere() { return genere; }
        public int getAnno() { return anno; }
        public int getVoto() { return voto; }

        @Override
        public String toString() {
            return String.format("Memento[%s — %s]", id.substring(0, 8), titolo);
        }
    }

    // Inner class Builder per la costruzione guidata di Game

    /**
     * Sottoclasse Builder per supportare la creazione guidata di un'istanza complessa di Game.
     * Applica un partizionamento logico fra attributi obbligatori e opzionali, centralizzando
     * le policy di verifica in fase di consolidamento (build).
     */
    public static class Builder {

        // campo obbligatorio
        private final String titolo;

        // campi opzionali con default
        private String id;
        private Piattaforma piattaforma;
        private Genere genere;
        private int anno = -1;   // -1 indica "non impostato"
        private int voto = -1;   // -1 indica "non impostato"

        /**
         * Inizializzazione del Builder con i requisiti minimi di esistenza.
         *
         * @param titolo la stringa rappresentante il titolo
         * @throws IllegalArgumentException in caso di assenza del parametro obbligatorio
         */
        public Builder(String titolo) {
            if (titolo == null || titolo.isBlank()) {
                throw new IllegalArgumentException("Il titolo non può essere nullo o vuoto.");
            }
            this.titolo = titolo.trim();
        }

        /**
         * Consente l'overriding dell'identificativo primario in scenari di deserializzazione.
         * Omettendone l'uso, il costruttore autogenererà un UUID univoco.
         *
         * @param id l'ID da propagare
         * @return l'istanza fluente del Builder
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder piattaforma(Piattaforma piattaforma) {
            this.piattaforma = piattaforma;
            return this;
        }

        public Builder genere(Genere genere) {
            this.genere = genere;
            return this;
        }

        public Builder anno(int anno) {
            this.anno = anno;
            return this;
        }

        public Builder voto(int voto) {
            this.voto = voto;
            return this;
        }

        /**
         * Consolida la sequenza di costruzione operando una convalida globale sullo stato.
         *
         * @return l'istanza finale regolarmente verificata
         * @throws IllegalArgumentException qualora emergano discrepanze nel rispetto delle business rule
         */
        public Game build() {
            // Generazione automatica dell'UUID se non fornito esplicitamente
            if (this.id == null || this.id.isBlank()) {
                this.id = UUID.randomUUID().toString();
            }

            // Validazione dei campi obbligatori
            StringBuilder errori = new StringBuilder();

            if (piattaforma == null) {
                errori.append("La piattaforma è obbligatoria. ");
            }
            if (genere == null) {
                errori.append("Il genere è obbligatorio. ");
            }

            int annoCorrente = Year.now().getValue();
            if (anno < ANNO_MINIMO || anno > annoCorrente) {
                errori.append(String.format("L'anno deve essere compreso tra %d e %d. ", ANNO_MINIMO, annoCorrente));
            }
            if (voto < VOTO_MINIMO || voto > VOTO_MASSIMO) {
                errori.append(String.format("Il voto deve essere compreso tra %d e %d. ", VOTO_MINIMO, VOTO_MASSIMO));
            }

            if (!errori.isEmpty()) {
                throw new IllegalArgumentException(errori.toString().trim());
            }

            return new Game(this);
        }
    }
}
