package com.gamelibrary.iterator;

import com.gamelibrary.model.Game;
import com.gamelibrary.model.Genere;
import com.gamelibrary.model.Piattaforma;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

/**
 * Test unitari per {@link GameIterator} e {@link GameIteratorFiltrato} —
 * verifica il pattern Iterator custom con iterazione, reset, filtri e casi limite.
 */
@DisplayName("GameIterator e GameIteratorFiltrato — pattern Iterator")
class GameIteratorTest {

    private List<Game> giochi;
    private Game gioco1;
    private Game gioco2;
    private Game gioco3;

    @BeforeEach
    void setUp() {
        gioco1 = new Game.Builder("Dark Souls")
                .piattaforma(Piattaforma.PLAYSTATION).genere(Genere.RPG).anno(2011).voto(10).build();
        gioco2 = new Game.Builder("Hollow Knight")
                .piattaforma(Piattaforma.STEAM).genere(Genere.PLATFORM).anno(2017).voto(9).build();
        gioco3 = new Game.Builder("Resident Evil")
                .piattaforma(Piattaforma.XBOX).genere(Genere.HORROR).anno(2005).voto(8).build();
        giochi = new ArrayList<>(List.of(gioco1, gioco2, gioco3));
    }

    // Test GameIterator

    @Test
    @DisplayName("GameIterator — iterazione completa su lista con 3 giochi")
    void iterazioneCompletaSuTreGiochi() {
        GameIterator it = new GameIterator(giochi);

        assertThat(it.hasProssimo()).isTrue();
        assertThat(it.prossimo()).isEqualTo(gioco1);
        assertThat(it.prossimo()).isEqualTo(gioco2);
        assertThat(it.prossimo()).isEqualTo(gioco3);
        assertThat(it.hasProssimo()).isFalse();
    }

    @Test
    @DisplayName("GameIterator — reset riporta il cursore all'inizio")
    void resetRiportaCursoreAllinizio() {
        GameIterator it = new GameIterator(giochi);
        it.prossimo();
        it.prossimo();

        it.reset();

        assertThat(it.hasProssimo()).isTrue();
        assertThat(it.prossimo()).isEqualTo(gioco1);
    }

    @Test
    @DisplayName("GameIterator — lista vuota non ha elementi da iterare")
    void listaVuotaNonHaElementi() {
        GameIterator it = new GameIterator(new ArrayList<>());

        assertThat(it.hasProssimo()).isFalse();
        assertThat(it.dimensione()).isZero();
    }

    @Test
    @DisplayName("GameIterator — prossimo() su lista esaurita lancia NoSuchElementException")
    void prossimoSuListaEsauritaLanciaEccezione() {
        GameIterator it = new GameIterator(new ArrayList<>());

        assertThatThrownBy(it::prossimo)
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("GameIterator — lista null lancia IllegalArgumentException")
    void listaNullLanciaEccezione() {
        assertThatThrownBy(() -> new GameIterator(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("GameIterator — dimensione() restituisce il numero corretto di elementi")
    void dimensioneCorretta() {
        GameIterator it = new GameIterator(giochi);
        assertThat(it.dimensione()).isEqualTo(3);
    }

    @Test
    @DisplayName("GameIterator — copia difensiva: modifiche alla lista originale non influenzano l'iteratore")
    void copiaDifensivaListaOriginale() {
        GameIterator it = new GameIterator(giochi);
        giochi.clear(); // modifica la lista originale

        assertThat(it.dimensione()).isEqualTo(3);
        assertThat(it.hasProssimo()).isTrue();
    }

    // Test GameIteratorFiltrato

    @Test
    @DisplayName("GameIteratorFiltrato — filtra solo giochi PlayStation")
    void filtraPerPiattaformaPlayStation() {
        GameIteratorFiltrato it = new GameIteratorFiltrato(giochi,
                g -> g.getPiattaforma() == Piattaforma.PLAYSTATION);

        assertThat(it.hasProssimo()).isTrue();
        assertThat(it.prossimo()).isEqualTo(gioco1);
        assertThat(it.hasProssimo()).isFalse();
        assertThat(it.dimensione()).isEqualTo(1);
    }

    @Test
    @DisplayName("GameIteratorFiltrato — filtra giochi con voto >= 9")
    void filtraPerVotoMinimo() {
        GameIteratorFiltrato it = new GameIteratorFiltrato(giochi,
                g -> g.getVoto() >= 9);

        assertThat(it.dimensione()).isEqualTo(2);
        assertThat(it.prossimo()).isEqualTo(gioco1);
        assertThat(it.prossimo()).isEqualTo(gioco2);
    }

    @Test
    @DisplayName("GameIteratorFiltrato — filtro che non trova risultati restituisce iteratore vuoto")
    void filtroSenzaRisultati() {
        GameIteratorFiltrato it = new GameIteratorFiltrato(giochi,
                g -> g.getVoto() > 10);

        assertThat(it.hasProssimo()).isFalse();
        assertThat(it.isVuoto()).isTrue();
        assertThat(it.dimensione()).isZero();
    }

    @Test
    @DisplayName("GameIteratorFiltrato — reset dopo iterazione parziale riporta all'inizio")
    void resetFiltratoDopoIterazioneParziale() {
        GameIteratorFiltrato it = new GameIteratorFiltrato(giochi,
                g -> g.getVoto() >= 9);

        it.prossimo(); // consuma gioco1
        it.reset();

        assertThat(it.hasProssimo()).isTrue();
        assertThat(it.prossimo()).isEqualTo(gioco1);
    }

    @Test
    @DisplayName("GameIteratorFiltrato — prossimo() oltre il limite lancia NoSuchElementException")
    void prossimoOltreLimiteFiltrato() {
        GameIteratorFiltrato it = new GameIteratorFiltrato(giochi,
                g -> g.getVoto() == 10);
        it.prossimo(); // consuma l'unico risultato

        assertThatThrownBy(it::prossimo)
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("GameIteratorFiltrato — predicato null lancia IllegalArgumentException")
    void predicatoNullLanciaEccezione() {
        assertThatThrownBy(() -> new GameIteratorFiltrato(giochi, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("GameIteratorFiltrato — lista null lancia IllegalArgumentException")
    void listaNullPerFiltratoLanciaEccezione() {
        assertThatThrownBy(() -> new GameIteratorFiltrato(null, g -> true))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
