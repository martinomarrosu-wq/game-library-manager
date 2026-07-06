package com.gamelibrary.ui;


import com.gamelibrary.model.Game;
import com.gamelibrary.model.Genere;
import com.gamelibrary.model.Piattaforma;
import com.gamelibrary.service.GameLibraryService;
import com.gamelibrary.util.Risultato;

import java.util.List;
import java.util.Scanner;

/**
 * Modulo di interfaccia utente (Boundary) delegato alle operazioni di interrogazione.
 * Espone le funzionalità di ricerca e filtraggio supportate dal layer di servizio.
 *
 * @author Martino Marrosu
 */
public class MenuRicerca {

    private final GameLibraryService service;
    private final Scanner scanner;

    public MenuRicerca(GameLibraryService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    /**
     * Avvia il ciclo di interazione per la selezione e l'applicazione dei criteri di ricerca.
     */
    public void esegui() {
        boolean esci = false;
        do {
            System.out.println("\n--- 🔍 RICERCA E FILTRI ---");
            System.out.println("1. Mostra tutti i giochi");
            System.out.println("2. Filtra per Genere");
            System.out.println("3. Filtra per Piattaforma");
            System.out.println("4. Cerca per ID");
            System.out.println("0. Torna al menu principale");
            System.out.print("Scegli un'opzione: ");

            String scelta = scanner.nextLine().trim();

            try {
                switch (scelta) {
                    case "1":
                        mostraLista(service.getTuttiIGiochi());
                        break;
                    case "2":
                        System.out.println("Scegli il Genere:");
                        for (Genere g : Genere.values()) {
                            System.out.println("- " + g.name() + " (" + g.getNomeVisualizzazione() + ")");
                        }
                        System.out.print("Inserisci il Genere: ");
                        Genere genere = Genere.valueOf(scanner.nextLine().trim().toUpperCase());
                        mostraLista(service.filtraPerGenere(genere));
                        break;
                    case "3":
                        System.out.println("Scegli la Piattaforma:");
                        for (Piattaforma p : Piattaforma.values()) {
                            System.out.println("- " + p.name() + " (" + p.getNomeVisualizzazione() + ")");
                        }
                        System.out.print("Inserisci la Piattaforma: ");
                        Piattaforma piattaforma = Piattaforma.valueOf(scanner.nextLine().trim().toUpperCase());
                        mostraLista(service.filtraPerPiattaforma(piattaforma));
                        break;
                    case "4":
                        System.out.print("Inserisci l'ID del gioco: ");
                        String id = scanner.nextLine().trim();
                        Risultato<Game> risultato = service.cercaPerId(id);
                        if (risultato.isSuccesso()) {
                            System.out.println("✅ Gioco trovato: " + risultato.getValore().getTitolo());
                            System.out.println("Dettagli: " + risultato.getValore());
                        } else {
                            System.out.println("❌ " + risultato.getMessaggioErrore());
                        }
                        break;
                    case "0":
                        esci = true;
                        break;
                    default:
                        System.out.println("❌ Opzione non valida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Errore: Valore inserito non valido.");
            } catch (Exception e) {
                System.out.println("❌ Si è verificato un errore durante la ricerca.");
            }
        } while (!esci);
    }

    private void mostraLista(List<Game> giochi) {
        if (giochi == null || giochi.isEmpty()) {
            System.out.println("Nessun gioco trovato.");
        } else {
            System.out.println("\nLista Giochi Trovati (" + giochi.size() + "):");
            for (Game g : giochi) {
                System.out.println("- " + g.getTitolo() + " (" + g.getPiattaforma().getNomeVisualizzazione() + 
                                   " | " + g.getGenere().getNomeVisualizzazione() + ") [Voto: " + g.getVoto() + "]");
            }
        }
    }
}
