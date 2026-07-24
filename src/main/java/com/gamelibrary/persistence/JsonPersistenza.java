package com.gamelibrary.persistence;

import com.gamelibrary.exceptions.PersistenzaException;
import com.gamelibrary.model.Game;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Implementazione della persistenza su file system tramite formato JSON
 * (libreria Gson).
 * Si è optato per Gson in quanto offre un mapping automatico con gli enumerati
 * e richiede minor
 * configurazione rispetto ad alternative come Jackson.
 * Il path del file di destinazione viene risolto dinamicamente leggendo un file
 * di properties,
 * evitando l'hardcoding. L'output viene inoltre formattato (pretty printing)
 * per favorirne
 * l'ispezionabilità manuale.
 *
 * @author Martino Marrosu
 */
public class JsonPersistenza implements Persistenza {

    private static final Logger LOGGER = Logger.getLogger(JsonPersistenza.class.getName());

    /** Chiave nel file config.properties per il percorso del file JSON */
    private static final String CONFIG_KEY_FILE_PATH = "gamelibrary.file.path";

    /** Percorso di fallback se config.properties non è disponibile */
    private static final String PERCORSO_DEFAULT = "data/gamelibrary.json";

    private final String percorsoFile;
    private final Gson gson;

    /**
     * Risolve il problema della Type Erasure di Java a runtime. Tramite TypeToken
     * viene preservata l'informazione del tipo generico (List di Game), essenziale
     * per il corretto mapping durante la deserializzazione da parte di Gson.
     */
    private static final Type TIPO_LISTA_GAME = new TypeToken<List<Game>>() {
    }.getType();

    /**
     * Costruttore di default. Si occupa di inizializzare l'istanza tentando il
     * caricamento
     * del path target dal file di configurazione `config.properties`, applicando un
     * percorso
     * di fallback in caso di assenza.
     */
    public JsonPersistenza() {
        this.percorsoFile = caricaPercorsoDaConfig();
        this.gson = creaGson();
        LOGGER.info(() -> String.format("JsonPersistenza inizializzata con percorso: %s", percorsoFile));
    }

    /**
     * Costruttore esplicito per l'iniezione del path di destinazione.
     * Introdotto per favorire il testing isolato, evitando di inquinare il file di
     * persistenza dell'applicazione.
     *
     * @param percorsoFile il percorso su cui scrivere/leggere il JSON
     */
    public JsonPersistenza(String percorsoFile) {
        if (percorsoFile == null || percorsoFile.isBlank()) {
            throw new IllegalArgumentException("Il percorso del file non può essere nullo o vuoto.");
        }
        this.percorsoFile = risolviPercorso(percorsoFile).toString();
        this.gson = creaGson();
        LOGGER.info(() -> String.format("JsonPersistenza inizializzata con percorso esplicito: %s", percorsoFile));
    }

    /**
     * Esegue la serializzazione della collezione in memoria sovrascrivendo il file
     * JSON.
     * Verifica l'esistenza della directory padre e procede alla sua eventuale
     * creazione.
     *
     * @param giochi la collezione di istanze da persistere
     * @throws PersistenzaException qualora intercorrano errori di I/O
     */
    @Override
    public void salva(List<Game> giochi) throws PersistenzaException {
        if (giochi == null) {
            throw new PersistenzaException("La lista di giochi da salvare non può essere nulla.");
        }

        Path path = Path.of(percorsoFile);

        try {
            // Crea le directory padre se non esistono (es. "data/")
            java.util.Optional.ofNullable(path.getParent())
                    .ifPresent(parent -> {
                        try {
                            Files.createDirectories(parent);
                        } catch (IOException e) {
                            throw new java.io.UncheckedIOException(e);
                        }
                    });

            // Serializzazione con BufferedWriter per efficienza
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(percorsoFile, StandardCharsets.UTF_8))) {
                gson.toJson(giochi, writer);
            }

            LOGGER.info(() -> String.format("Salvati %d giochi su %s", giochi.size(), percorsoFile));

        } catch (IOException e) {
            // Exception Shielding: wrappa IOException in PersistenzaException
            LOGGER.severe(() -> String.format("Errore durante il salvataggio su %s: %s",
                    percorsoFile, e.getMessage()));
            throw new PersistenzaException(
                    String.format("Impossibile salvare la libreria sul file '%s'.", percorsoFile), e);
        }
    }

    /**
     * Provvede alla lettura e deserializzazione del file JSON, ricostituendo gli
     * oggetti Game in memoria.
     * Prevede la gestione formale del "primo avvio", creando un file vuoto se non
     * preesistente.
     * Ogni oggetto deserializzato viene processato attraverso il Builder nativo per
     * assicurare l'integrità
     * delle regole di business prima della sua ammissione in memoria.
     *
     * @return l'elenco dei giochi correttamente processati e validati
     * @throws PersistenzaException in presenza di JSON malformati o errori di
     *                              accesso al file system
     */
    @Override
    public List<Game> carica() throws PersistenzaException {
        Path path = risolviPercorso(percorsoFile);

        // Primo avvio: file non esiste — restituisci lista vuota
        if (!Files.exists(path)) {
            LOGGER.info(() -> String.format("File %s non trovato — primo avvio, creazione libreria vuota.",
                    path));
            // Crea il file con un array vuoto per le esecuzioni successive
            try {
                salva(new ArrayList<>());
            } catch (PersistenzaException e) {
                LOGGER.warning(() -> String.format("Impossibile creare il file iniziale: %s", e.getMessage()));
                // Non critico: la libreria funziona comunque in memoria
            }
            return new ArrayList<>();
        }

        try (BufferedReader reader = new BufferedReader(
                new FileReader(percorsoFile, StandardCharsets.UTF_8))) {

            List<Game> giochi = gson.fromJson(reader, TIPO_LISTA_GAME);

            // Gson restituisce null se il file è vuoto o contiene solo whitespace
            if (giochi == null) {
                LOGGER.info("File JSON vuoto — restituita lista vuota.");
                return new ArrayList<>();
            }

            // Ricostruisci i giochi tramite Builder per garantire la validazione
            List<Game> giochiValidati = new ArrayList<>();
            for (Game gioco : giochi) {
                try {
                    Game validato = new Game.Builder(gioco.getTitolo())
                            .id(gioco.getId())
                            .piattaforma(gioco.getPiattaforma())
                            .genere(gioco.getGenere())
                            .anno(gioco.getAnno())
                            .voto(gioco.getVoto())
                            .build();
                    giochiValidati.add(validato);
                } catch (IllegalArgumentException e) {
                    LOGGER.warning(() -> String.format(
                            "Gioco saltato durante il caricamento (dati non validi): %s — %s",
                            gioco.getTitolo(), e.getMessage()));
                    // Il gioco corrotto viene saltato, non blocca il caricamento
                }
            }

            LOGGER.info(() -> String.format("Caricati %d giochi da %s (%d validi su %d totali)",
                    giochiValidati.size(), percorsoFile, giochiValidati.size(), giochi.size()));

            return giochiValidati;

        } catch (JsonSyntaxException e) {
            LOGGER.severe(() -> String.format("File JSON malformato in %s: %s",
                    percorsoFile, e.getMessage()));
            throw new PersistenzaException(
                    String.format("Il file '%s' contiene dati JSON non validi.", percorsoFile), e);

        } catch (IOException e) {
            LOGGER.severe(() -> String.format("Errore durante il caricamento da %s: %s",
                    percorsoFile, e.getMessage()));
            throw new PersistenzaException(
                    String.format("Impossibile leggere il file '%s'.", percorsoFile), e);
        }
    }

    /**
     * Accessor per recuperare il percorso del file attualmente in uso.
     *
     * @return stringa rappresentante il path
     */
    public String getPercorsoFile() {
        return percorsoFile;
    }

    // Metodi privati di supporto

    /**
     * Tenta il parsing del file di properties per dedurre il parametro relativo al
     * file di salvataggio.
     * Applica un meccanismo di fallback in caso di eccezione per garantire la
     * tolleranza ai guasti (Fault Tolerance).
     *
     * @return il percorso determinato
     */
    private String caricaPercorsoDaConfig() {
        File configFile = new File("config.properties");
        if (!configFile.exists()) {
            LOGGER.warning("config.properties non trovato — uso percorso default: " + PERCORSO_DEFAULT);
            return PERCORSO_DEFAULT;
        }

        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
            String percorso = properties.getProperty(CONFIG_KEY_FILE_PATH, PERCORSO_DEFAULT);
            if (percorso.isBlank()) {
                LOGGER.warning("Percorso file vuoto in config.properties — uso default.");
                return PERCORSO_DEFAULT;
            }
            return risolviPercorso(percorso.trim()).toString();

        } catch (IOException e) {
            LOGGER.warning(() -> String.format("Errore lettura config.properties: %s — uso default.",
                    e.getMessage()));
            return PERCORSO_DEFAULT;
        }
    }

    private Path risolviPercorso(String percorso) {
        if (percorso == null || percorso.isBlank()) {
            return Path.of(PERCORSO_DEFAULT);
        }

        Path path = Path.of(percorso);
        if (path.isAbsolute()) {
            return path.normalize();
        }

        Path workingDir = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path resolved = workingDir.resolve(path).normalize();

        if (Files.exists(resolved)) {
            return resolved;
        }

        Path parent = workingDir.getParent();
        if (parent != null) {
            Path fallback = parent.resolve(path).normalize();
            if (Files.exists(fallback)) {
                return fallback;
            }
        }

        return resolved;
    }

    /**
     * Configura e istanzia il motore di serializzazione Gson secondo i requisiti
     * del progetto.
     *
     * @return l'istanza di Gson configurata
     */
    private Gson creaGson() {
        return new GsonBuilder()
                .setPrettyPrinting() // Output leggibile (indentato)
                .disableHtmlEscaping() // Non escapare caratteri come <, >, &
                .create();
    }
}
