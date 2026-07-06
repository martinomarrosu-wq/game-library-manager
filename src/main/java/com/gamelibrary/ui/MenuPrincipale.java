package com.gamelibrary.ui;

import com.gamelibrary.service.GameLibraryService;
import com.gamelibrary.util.ErrorHandler;
import com.gamelibrary.exceptions.InputNonValidoException;
import com.gamelibrary.exceptions.GameNotFoundException;

import java.util.Scanner;

/**
 * Punto di ingresso principale per l'interazione utente via Command Line
 * Interface (CLI).
 * Implementa un loop di controllo in ascolto delle direttive utente, delegando
 * l'effettiva logica di business e persistenza al modulo di servizio
 * (Facade/Service Layer).
 *
 * @author Martino Marrosu
 */
public class MenuPrincipale {

    private final GameLibraryService service;
    private final Scanner scanner;

    public MenuPrincipale(GameLibraryService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    public void avvia() {
        int scelta = -1;
        try {
            do {
                stampaMenu();
                try {
                    String input = scanner.nextLine();
                    scelta = Integer.parseInt(input.trim());
                    switch (scelta) {
                        case 1 -> new MenuAggiungi(service, scanner).esegui();
                        case 2 -> modificaGioco();
                        case 3 -> rimuoviGioco();
                        case 4 -> new MenuRicerca(service, scanner).esegui();
                        case 5 -> new MenuStatistiche(service, scanner).esegui();
                        case 0 -> System.out.println("Uscita in corso...");
                        default -> System.out.println("Scelta non valida. Riprova.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Inserisci un numero intero per la scelta.");
                } catch (InputNonValidoException e) {
                    ErrorHandler.gestisci(e, "gestione input menu");
                } catch (Exception e) {
                    // Qualsiasi eccezione inattesa
                    ErrorHandler.gestisci(e, "esecuzione menu principale");
                }
            } while (scelta != 0);
        } finally {
            // Chiudi risorse — garantito anche se si verifica un'eccezione
            service.shutdown();
            scanner.close();
        }
    }

    private void stampaMenu() {
        System.out.println();
        System.out.println("╔═════════════════════════════╗");
        System.out.println("║      MENU PRINCIPALE        ║");
        System.out.println("╠═════════════════════════════╣");
        System.out.println("║ 1) Aggiungi gioco           ║");
        System.out.println("║ 2) Modifica gioco           ║");
        System.out.println("║ 3) Rimuovi gioco            ║");
        System.out.println("║ 4) Ricerca / Filtri         ║");
        System.out.println("║ 5) Statistiche e report     ║");
        System.out.println("║ 0) Esci                     ║");
        System.out.println("╚═════════════════════════════╝");
        System.out.print("Scelta: ");
    }

    private void modificaGioco() {
        try {
            System.out.print("Inserisci l'ID del gioco da modificare: ");
            String id = scanner.nextLine().trim();
            System.out.print("Campo da modificare (titolo, piattaforma, genere, anno, voto): ");
            String campo = scanner.nextLine().trim();
            System.out.print("Nuovo valore: ");
            String valore = scanner.nextLine().trim();
            var risultato = service.modificaGioco(id, campo, valore);
            if (risultato.isSuccesso()) {
                System.out.println("✅ Gioco modificato con successo.");
            } else {
                System.out.println(risultato.getMessaggioErrore());
            }
        } catch (GameNotFoundException e) {
            ErrorHandler.gestisci(e, "modifica gioco");
        } catch (InputNonValidoException e) {
            ErrorHandler.gestisci(e, "input modifica gioco");
        }
    }

    private void rimuoviGioco() {
        try {
            System.out.print("Inserisci l'ID del gioco da rimuovere: ");
            String id = scanner.nextLine().trim();
            var risultato = service.rimuoviGioco(id);
            if (risultato.isSuccesso()) {
                System.out.println("🗑️ Gioco rimosso.");
            } else {
                System.out.println(risultato.getMessaggioErrore());
            }
        } catch (GameNotFoundException e) {
            ErrorHandler.gestisci(e, "rimozione gioco");
        } catch (InputNonValidoException e) {
            ErrorHandler.gestisci(e, "input rimozione gioco");
        }
    }
}
