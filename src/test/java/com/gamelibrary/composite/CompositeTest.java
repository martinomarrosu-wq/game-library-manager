package com.gamelibrary.composite;

import com.gamelibrary.model.Game;
import com.gamelibrary.model.Genere;
import com.gamelibrary.model.Piattaforma;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Qui testo CollezioneGiochi e GiocoFoglia, cioe il Composite Pattern.
 * In pratica controllo che l'albero funzioni bene, che il conteggio ricorsivo
 * sia giusto e che mostra() stampi tutto come deve.
 *
 * @author Martino
 */
@DisplayName("Composite Pattern — CollezioneGiochi e GiocoFoglia")
class CompositeTest {

    private Game gioco1;
    private Game gioco2;
    private Game gioco3;

    @BeforeEach
    void setUp() {
        gioco1 = new Game.Builder("Dark Souls")
                .piattaforma(Piattaforma.PLAYSTATION).genere(Genere.RPG).anno(2011).voto(10).build();
        gioco2 = new Game.Builder("Elden Ring")
                .piattaforma(Piattaforma.PLAYSTATION).genere(Genere.RPG).anno(2022).voto(9).build();
        gioco3 = new Game.Builder("Hollow Knight")
                .piattaforma(Piattaforma.STEAM).genere(Genere.PLATFORM).anno(2017).voto(9).build();
    }

    // Test CollezioneGiochi

    @Test
    @DisplayName("CollezioneGiochi — creazione con nome valido")
    void creazioneCollezioneConNomeValido() {
        CollezioneGiochi collezione = new CollezioneGiochi("Libreria");

        assertThat(collezione.getNome()).isEqualTo("Libreria");
        assertThat(collezione.isVuota()).isTrue();
        assertThat(collezione.contaGiochi()).isZero();
    }

    @Test
    @DisplayName("CollezioneGiochi — nome null lancia IllegalArgumentException")
    void creazioneConNomeNull() {
        assertThatThrownBy(() -> new CollezioneGiochi(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("CollezioneGiochi — nome vuoto lancia IllegalArgumentException")
    void creazioneConNomeVuoto() {
        assertThatThrownBy(() -> new CollezioneGiochi("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("CollezioneGiochi — aggiunta foglie incrementa il conteggio")
    void aggiuntaFoglieIncrementaConteggio() {
        CollezioneGiochi collezione = new CollezioneGiochi("RPG");
        collezione.aggiungi(new GiocoFoglia(gioco1));
        collezione.aggiungi(new GiocoFoglia(gioco2));

        assertThat(collezione.contaGiochi()).isEqualTo(2);
        assertThat(collezione.isVuota()).isFalse();
        assertThat(collezione.getComponenti()).hasSize(2);
    }

    @Test
    @DisplayName("CollezioneGiochi — aggiunta componente null lancia IllegalArgumentException")
    void aggiuntaComponenteNull() {
        CollezioneGiochi collezione = new CollezioneGiochi("Test");

        assertThatThrownBy(() -> collezione.aggiungi(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("CollezioneGiochi — rimozione componente esistente riduce il conteggio")
    void rimozioneComponenteEsistente() {
        CollezioneGiochi collezione = new CollezioneGiochi("Test");
        GiocoFoglia foglia = new GiocoFoglia(gioco1);
        collezione.aggiungi(foglia);

        collezione.rimuovi(foglia);

        assertThat(collezione.contaGiochi()).isZero();
        assertThat(collezione.isVuota()).isTrue();
    }

    @Test
    @DisplayName("CollezioneGiochi — rimozione componente null lancia IllegalArgumentException")
    void rimozioneComponenteNull() {
        CollezioneGiochi collezione = new CollezioneGiochi("Test");

        assertThatThrownBy(() -> collezione.rimuovi(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("CollezioneGiochi — conteggio ricorsivo con sotto-collezioni annidate")
    void conteggioRicorsivoConSottoCollezioni() {
        CollezioneGiochi libreria = new CollezioneGiochi("Libreria");
        CollezioneGiochi ps = new CollezioneGiochi("PlayStation");
        CollezioneGiochi steam = new CollezioneGiochi("Steam");

        ps.aggiungi(new GiocoFoglia(gioco1));
        ps.aggiungi(new GiocoFoglia(gioco2));
        steam.aggiungi(new GiocoFoglia(gioco3));

        libreria.aggiungi(ps);
        libreria.aggiungi(steam);

        assertThat(libreria.contaGiochi()).isEqualTo(3);
        assertThat(ps.contaGiochi()).isEqualTo(2);
        assertThat(steam.contaGiochi()).isEqualTo(1);
    }

    @Test
    @DisplayName("CollezioneGiochi — mostra() stampa la struttura ad albero con indentazione")
    void mostraStampaStrutturaAdAlbero() {
        CollezioneGiochi libreria = new CollezioneGiochi("Libreria");
        libreria.aggiungi(new GiocoFoglia(gioco1));

        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        try {
            libreria.mostra(0);
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertThat(output).contains("Libreria");
        assertThat(output).contains("Dark Souls");
    }

    @Test
    @DisplayName("CollezioneGiochi — getComponenti restituisce copia difensiva")
    void getComponentiRestituisceCopiaDifensiva() {
        CollezioneGiochi collezione = new CollezioneGiochi("Test");
        collezione.aggiungi(new GiocoFoglia(gioco1));

        var componenti = collezione.getComponenti();
        componenti.clear(); // modifica la copia

        assertThat(collezione.contaGiochi()).isEqualTo(1); // originale invariato
    }

    @Test
    @DisplayName("CollezioneGiochi — collezione vuota ha conteggio zero")
    void collezioneVuotaHaConteggioZero() {
        CollezioneGiochi collezione = new CollezioneGiochi("Vuota");

        assertThat(collezione.contaGiochi()).isZero();
        assertThat(collezione.isVuota()).isTrue();
    }

    // Test GiocoFoglia

    @Test
    @DisplayName("GiocoFoglia — getNome restituisce il titolo del gioco wrappato")
    void fogliaGetNomeRestituisceTitolo() {
        GiocoFoglia foglia = new GiocoFoglia(gioco1);
        assertThat(foglia.getNome()).isEqualTo("Dark Souls");
    }

    @Test
    @DisplayName("GiocoFoglia — contaGiochi restituisce sempre 1")
    void fogliaContaGiochiRestituisceUno() {
        GiocoFoglia foglia = new GiocoFoglia(gioco1);
        assertThat(foglia.contaGiochi()).isEqualTo(1);
    }

    @Test
    @DisplayName("GiocoFoglia — getGioco restituisce il gioco wrappato")
    void fogliaGetGiocoRestituisceIlGioco() {
        GiocoFoglia foglia = new GiocoFoglia(gioco1);
        assertThat(foglia.getGioco()).isEqualTo(gioco1);
    }

    @Test
    @DisplayName("GiocoFoglia — gioco null lancia IllegalArgumentException")
    void fogliaGiocoNullLanciaEccezione() {
        assertThatThrownBy(() -> new GiocoFoglia(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("GiocoFoglia — aggiungi() lancia UnsupportedOperationException")
    void fogliaAggiungiLanciaUnsupported() {
        GiocoFoglia foglia = new GiocoFoglia(gioco1);

        assertThatThrownBy(() -> foglia.aggiungi(new GiocoFoglia(gioco2)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("GiocoFoglia — rimuovi() lancia UnsupportedOperationException")
    void fogliaRimuoviLanciaUnsupported() {
        GiocoFoglia foglia = new GiocoFoglia(gioco1);

        assertThatThrownBy(() -> foglia.rimuovi(new GiocoFoglia(gioco2)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("GiocoFoglia — mostra() stampa le informazioni del gioco")
    void fogliaMostraStampaInfoGioco() {
        GiocoFoglia foglia = new GiocoFoglia(gioco1);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        try {
            foglia.mostra(0);
        } finally {
            System.setOut(originalOut);
        }

        assertThat(outContent.toString()).contains("Dark Souls");
    }
}
