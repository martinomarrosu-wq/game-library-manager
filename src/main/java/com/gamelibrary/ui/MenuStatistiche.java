package com.gamelibrary.ui;

import com.gamelibrary.model.Game;
import com.gamelibrary.model.Genere;
import com.gamelibrary.model.Piattaforma;
import com.gamelibrary.service.GameLibraryService;
import com.gamelibrary.util.StatisticheUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Modulo di interfaccia utente orientato alla presentazione di metriche e reportistica.
 * Si interfaccia con le classi di utilità e di servizio per estrapolare e formattare i dati statistici.
 *
 * @author Martino Marrosu
 */
public class MenuStatistiche {

    private final GameLibraryService service;
    private final Scanner scanner;

    public MenuStatistiche(GameLibraryService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    /**
     * Avvia il ciclo operativo dedicato all'interrogazione e visualizzazione delle metriche.
     */
    public void esegui() {
        boolean esci = false;
        do {
            System.out.println("\n--- 📊 STATISTICHE E REPORT ---");
            System.out.println("1. Statistiche generali");
            System.out.println("2. Giochi raggruppati per Piattaforma");
            System.out.println("3. Giochi raggruppati per Genere");
            System.out.println("0. Torna al menu principale");
            System.out.print("Scegli un'opzione: ");

            String scelta = scanner.nextLine().trim();

            try {
                switch (scelta) {
                    case "1":
                        List<Game> tutti = service.getTuttiIGiochi();
                        if (tutti.isEmpty()) {
                            System.out.println("La libreria è vuota. Nessuna statistica disponibile.");
                        } else {
                            System.out.println("\n--- Statistiche Generali ---");
                            System.out.println("Totale giochi: " + tutti.size());
                            System.out.printf("Voto medio: %.2f\n", StatisticheUtil.mediaVotiGenerale(tutti));
                            
                            Optional<Game> migliore = StatisticheUtil.giocoConVotoPiuAlto(tutti);
                            migliore.ifPresent(g -> System.out.println("Gioco con voto più alto: " + g.getTitolo() + " (" + g.getVoto() + ")"));
                        }
                        break;
                    case "2":
                        Map<Piattaforma, List<Game>> perPiattaforma = service.raggruppaPerPiattaforma();
                        if (perPiattaforma.isEmpty()) {
                            System.out.println("Nessun gioco presente.");
                        } else {
                            System.out.println("\n--- Giochi per Piattaforma ---");
                            for (Map.Entry<Piattaforma, List<Game>> entry : perPiattaforma.entrySet()) {
                                System.out.println(entry.getKey().getNomeVisualizzazione() + ": " + entry.getValue().size() + " giochi");
                            }
                        }
                        break;
                    case "3":
                        Map<Genere, Long> perGenere = service.contaPerGenere();
                        if (perGenere.isEmpty()) {
                            System.out.println("Nessun gioco presente.");
                        } else {
                            System.out.println("\n--- Giochi per Genere ---");
                            for (Map.Entry<Genere, Long> entry : perGenere.entrySet()) {
                                System.out.println(entry.getKey().getNomeVisualizzazione() + ": " + entry.getValue() + " giochi");
                            }
                        }
                        break;
                    case "0":
                        esci = true;
                        break;
                    default:
                        System.out.println("❌ Opzione non valida.");
                }
            } catch (Exception e) {
                System.out.println("❌ Si è verificato un errore durante l'elaborazione delle statistiche.");
            }
        } while (!esci);
    }
}
