package com.gamelibrary.util;

import com.gamelibrary.model.Game;
import com.gamelibrary.model.Genere;
import com.gamelibrary.model.Piattaforma;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe di utilità preposta all'elaborazione statistica e all'aggregazione dei dati 
 * relativi alla libreria di giochi (medie, raggruppamenti, filtri).
 * L'implementazione si basa estensivamente sulle Stream API (introdotte in Java 8),
 * preferendo un approccio dichiarativo per ottimizzare la leggibilità ed evitare
 * costrutti iterativi espliciti complessi.
 *
 * @author Martino Marrosu
 */
public final class StatisticheUtil {

        /**
         * Costruttore privato per prevenire l'istanziazione, trattandosi di una classe puramente utility.
         */
        private StatisticheUtil() {
                throw new UnsupportedOperationException("Non posso creare oggetti di questa classe.");
        }

        // Raggruppamento (Collectors.groupingBy)

        /**
         * Partiziona la collezione in base alla piattaforma hardware di appartenenza.
         *
         * @param giochi la collezione di riferimento
         * @return una mappa strutturata per piattaforma
         */
        public static Map<Piattaforma, List<Game>> raggruppaPeRPiattaforma(List<Game> giochi) {
                return giochi.stream()
                                .collect(Collectors.groupingBy(Game::getPiattaforma));
        }

        /**
         * Computa le frequenze assolute raggruppando gli elementi per piattaforma.
         *
         * @param giochi la collezione in input
         * @return una mappa associativa con le relative occorrenze
         */
        public static Map<Piattaforma, Long> contaPerPiattaforma(List<Game> giochi) {
                return giochi.stream()
                                .collect(Collectors.groupingBy(Game::getPiattaforma, Collectors.counting()));
        }

        /**
         * Computa le frequenze assolute raggruppando gli elementi in base al genere.
         *
         * @param giochi la collezione di riferimento
         * @return mappa delle frequenze per genere videoludico
         */
        public static Map<Genere, Long> contaPerGenere(List<Game> giochi) {
                return giochi.stream()
                                .collect(Collectors.groupingBy(Game::getGenere, Collectors.counting()));
        }

        // Calcolo delle medie (Collectors.averagingInt)

        /**
         * Calcola la media aritmetica globale delle valutazioni.
         *
         * @param giochi il set di dati
         * @return la media calcolata, o 0.0 nel caso di collezione vuota
         */
        public static double mediaVotiGenerale(List<Game> giochi) {
                return giochi.stream()
                                .mapToInt(Game::getVoto)
                                .average()
                                .orElse(0.0);
        }

        /**
         * Aggrega e computa la media delle valutazioni isolandole per genere specifico.
         *
         * @param giochi la collezione dati
         * @return una mappa delle medie calcolate
         */
        public static Map<Genere, Double> mediaVotiPerGenere(List<Game> giochi) {
                return giochi.stream()
                                .collect(Collectors.groupingBy(
                                                Game::getGenere,
                                                Collectors.averagingInt(Game::getVoto)));
        }

        /**
         * Aggrega e computa la media delle valutazioni isolandole per singola piattaforma.
         *
         * @param giochi la collezione di riferimento
         * @return la mappatura delle medie calcolate
         */
        public static Map<Piattaforma, Double> mediaVotiPerPiattaforma(List<Game> giochi) {
                return giochi.stream()
                                .collect(Collectors.groupingBy(
                                                Game::getPiattaforma,
                                                Collectors.averagingInt(Game::getVoto)));
        }

        // Ricerca estremi (Comparator con max/min)

        /**
         * Ricerca l'elemento che possiede la massima valutazione utente.
         *
         * @param giochi la collezione sorgente
         * @return un Optional contenente il risultato (se presente)
         */
        public static Optional<Game> giocoConVotoPiuAlto(List<Game> giochi) {
                return giochi.stream()
                                .max(Comparator.comparingInt(Game::getVoto));
        }

        /**
         * Ricerca l'elemento che presenta la valutazione utente più modesta.
         *
         * @param giochi la collezione sorgente
         * @return un Optional contenente il risultato (se presente)
         */
        public static Optional<Game> giocoConVotoPiuBasso(List<Game> giochi) {
                return giochi.stream()
                                .min(Comparator.comparingInt(Game::getVoto));
        }

        // Filtraggio (Stream.filter con predicati)

        /**
         * Estrae gli elementi filtrandoli tramite un confronto restrittivo di piattaforma.
         *
         * @param giochi la collezione di input
         * @param piattaforma la discriminante di ricerca
         * @return la collezione filtrata
         */
        public static List<Game> filtraPerPiattaforma(List<Game> giochi, Piattaforma piattaforma) {
                return giochi.stream()
                                .filter(g -> g.getPiattaforma() == piattaforma)
                                .collect(Collectors.toList());
        }

        /**
         * Estrae gli elementi che soddisfano l'appartenenza a un particolare genere videoludico.
         *
         * @param giochi la collezione di input
         * @param genere il genere richiesto
         * @return la collezione isolata
         */
        public static List<Game> filtraPerGenere(List<Game> giochi, Genere genere) {
                return giochi.stream()
                                .filter(g -> g.getGenere() == genere)
                                .collect(Collectors.toList());
        }

        /**
         * Isola il sottoinsieme di giochi rilasciati nell'anno solare specificato.
         *
         * @param giochi la base dati
         * @param anno parametro temporale di filtraggio
         * @return gli elementi corrispondenti
         */
        public static List<Game> filtraPerAnno(List<Game> giochi, int anno) {
                return giochi.stream()
                                .filter(g -> g.getAnno() == anno)
                                .collect(Collectors.toList());
        }

        /**
         * Restituisce gli elementi conformi a una soglia prestazionale/valutativa minima (inclusiva).
         *
         * @param giochi dati sorgente
         * @param votoMinimo limite inferiore di valutazione
         * @return la collezione post-filtraggio
         */
        public static List<Game> filtraPerVotoMinimo(List<Game> giochi, int votoMinimo) {
                return giochi.stream()
                                .filter(g -> g.getVoto() >= votoMinimo)
                                .collect(Collectors.toList());
        }

        /**
         * Implementa una ricerca tramite substring case-insensitive in relazione al campo titolo.
         *
         * @param giochi la lista bersaglio
         * @param testoCercato pattern parziale da identificare
         * @return la sottolista dei match positivi
         */
        public static List<Game> cercaPerTitolo(List<Game> giochi, String testoCercato) {
                String testoLower = testoCercato.toLowerCase().trim();
                return giochi.stream()
                                .filter(g -> g.getTitolo().toLowerCase().contains(testoLower))
                                .collect(Collectors.toList());
        }

        // Ordinamento (Stream.sorted con Comparator)

        /**
         * Ordina il set di elementi per titolo sfruttando l'ordine lessicografico.
         *
         * @param giochi gli elementi in questione
         * @return la vista ordinata
         */
        public static List<Game> ordinaPerTitolo(List<Game> giochi) {
                return giochi.stream()
                                .sorted(Comparator.comparing(Game::getTitolo, String.CASE_INSENSITIVE_ORDER))
                                .collect(Collectors.toList());
        }

        /**
         * Riorganizza temporalmente gli elementi applicando un ordine discendente (dal prodotto più recente).
         *
         * @param giochi gli elementi
         * @return la vista ordinata
         */
        public static List<Game> ordinaPerAnno(List<Game> giochi) {
                return giochi.stream()
                                .sorted(Comparator.comparingInt(Game::getAnno).reversed())
                                .collect(Collectors.toList());
        }

        /**
         * Riorganizza gli elementi in modo decrescente sulla base del riscontro utente (voto).
         *
         * @param giochi gli elementi da elaborare
         * @return la vista ordinata
         */
        public static List<Game> ordinaPerVoto(List<Game> giochi) {
                return giochi.stream()
                                .sorted(Comparator.comparingInt(Game::getVoto).reversed())
                                .collect(Collectors.toList());
        }

        // Distribuzione temporale (raggruppamento per anno)

        /**
         * Genera un'istogramma tabellare indicizzante l'occorrenza delle release nel tempo, strutturato su base annua.
         *
         * @param giochi il dominio dati
         * @return mappa ordinata degli anni intercettati
         */
        public static Map<Integer, Long> distribuzionePerAnno(List<Game> giochi) {
                return giochi.stream()
                                .collect(Collectors.groupingBy(
                                                Game::getAnno,
                                                TreeMap::new,
                                                Collectors.counting()));
        }

        // Formattazione del report per la CLI

        /**
         * Raccoglie ed elabora una stringa riepilogativa consolidando molteplici misurazioni descrittive.
         * Utilizzato a scopo di visualizzazione all'interno della Command Line Interface (CLI).
         *
         * @param giochi la base dati su cui computare le metriche
         * @return la sequenza testuale formattata per l'output standard
         */
        public static String generaReport(List<Game> giochi) {
                if (giochi.isEmpty()) {
                        return "La libreria è vuota. Aggiungi qualche gioco per vedere le statistiche!";
                }

                StringBuilder report = new StringBuilder();
                report.append("\n╔══════════════════════════════════╗\n");
                report.append("║         📊 STATISTICHE           ║\n");
                report.append("╚══════════════════════════════════╝\n\n");

                // Totale giochi
                report.append(String.format("📚 Totale giochi: %d%n%n", giochi.size()));

                // Media voti generale
                report.append(String.format("⭐ Media voti generale: %.1f/10%n%n", mediaVotiGenerale(giochi)));

                // Gioco con voto più alto
                giocoConVotoPiuAlto(giochi).ifPresent(
                                g -> report.append(String.format("🏆 Gioco con voto più alto: %s (Voto: %d/10)%n%n",
                                                g.getTitolo(), g.getVoto())));

                // Gioco con voto più basso
                giocoConVotoPiuBasso(giochi).ifPresent(
                                g -> report.append(String.format("📉 Gioco con voto più basso: %s (Voto: %d/10)%n%n",
                                                g.getTitolo(), g.getVoto())));

                // Conteggio per piattaforma
                report.append("🎮 Giochi per piattaforma:\n");
                contaPerPiattaforma(giochi).entrySet().stream()
                                .sorted(Map.Entry.<Piattaforma, Long>comparingByValue().reversed())
                                .forEach(e -> report.append(String.format("   %-15s %d%n",
                                                e.getKey().getNomeVisualizzazione(), e.getValue())));
                report.append("\n");

                // Media voti per genere
                report.append("📊 Media voti per genere:\n");
                mediaVotiPerGenere(giochi).entrySet().stream()
                                .sorted(Map.Entry.<Genere, Double>comparingByValue().reversed())
                                .forEach(e -> report.append(String.format("   %-15s %.1f/10%n",
                                                e.getKey().getNomeVisualizzazione(), e.getValue())));
                report.append("\n");

                // Distribuzione per anno
                report.append("📅 Distribuzione per anno:\n");
                distribuzionePerAnno(giochi)
                                .forEach((anno, count) -> report.append(String.format("   %-6d %s (%d)%n",
                                                anno, "█".repeat(Math.min(count.intValue(), 20)), count)));

                return report.toString();
        }
}
