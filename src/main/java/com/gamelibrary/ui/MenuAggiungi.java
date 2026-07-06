package com.gamelibrary.ui;


import com.gamelibrary.model.Game;
import com.gamelibrary.model.Genere;
import com.gamelibrary.model.Piattaforma;
import com.gamelibrary.service.GameLibraryService;
import com.gamelibrary.util.Risultato;

import java.util.Scanner;

/**
 * Modulo di interfaccia (Boundary) delegato all'acquisizione dei dati in ingresso.
 * Gestisce l'interazione per il flusso di creazione e inserimento di una nuova istanza videoludica.
 *
 * @author Martino Marrosu
 */
public class MenuAggiungi {

    private final GameLibraryService service;
    private final Scanner scanner;

    public MenuAggiungi(GameLibraryService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    /**
     * Avvia e gestisce il flusso procedurale di acquisizione campi e comunicazione con il layer di servizio.
     */
    public void esegui() {
        System.out.println("\n--- ➕ AGGIUNGI UN NUOVO GIOCO ---");
        
        try {
            System.out.print("Titolo del gioco: ");
            String titolo = scanner.nextLine().trim();

            System.out.println("Scegli la Piattaforma:");
            for (Piattaforma p : Piattaforma.values()) {
                System.out.println("- " + p.name() + " (" + p.getNomeVisualizzazione() + ")");
            }
            System.out.print("Inserisci il nome della Piattaforma: ");
            String piattaformaStr = scanner.nextLine().trim().toUpperCase();
            Piattaforma piattaforma = Piattaforma.valueOf(piattaformaStr);

            System.out.println("Scegli il Genere:");
            for (Genere g : Genere.values()) {
                System.out.println("- " + g.name() + " (" + g.getNomeVisualizzazione() + ")");
            }
            System.out.print("Inserisci il nome del Genere: ");
            String genereStr = scanner.nextLine().trim().toUpperCase();
            Genere genere = Genere.valueOf(genereStr);

            System.out.print("Anno di uscita (es. 2023): ");
            int anno = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Voto personale (1-10): ");
            int voto = Integer.parseInt(scanner.nextLine().trim());

            Risultato<Game> risultato = service.aggiungiGioco(titolo, piattaforma, genere, anno, voto);
            
            if (risultato.isSuccesso()) {
                System.out.println("✅ Gioco aggiunto con successo!");
                System.out.println("Titolo: " + risultato.getValore().getTitolo());
                System.out.println("ID Generato: " + risultato.getValore().getId());
            } else {
                System.out.println("❌ Errore durante l'aggiunta: " + risultato.getMessaggioErrore());
            }
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Errore: Inserisci un numero valido per l'anno e per il voto.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Errore: Piattaforma o Genere non validi.");
        } catch (Exception e) {
            System.out.println("❌ Si è verificato un errore imprevisto.");
        }
    }
}
