package com.gamelibrary.memento;

import com.gamelibrary.model.Game;
import com.gamelibrary.model.Game.GameMemento;
import com.gamelibrary.model.Genere;
import com.gamelibrary.model.Piattaforma;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Test unitari per {@link CaretakerStorico} — verifica il Memento Pattern
 * con salvataggio/ripristino stato, limite stack e casi limite.
 */
@DisplayName("CaretakerStorico — Memento Pattern")
class MementoTest {

    private CaretakerStorico storico;
    private Game gioco;

    @BeforeEach
    void setUp() {
        storico = new CaretakerStorico(3); // limite massimo 3
        gioco = new Game.Builder("Dark Souls")
                .piattaforma(Piattaforma.PLAYSTATION).genere(Genere.RPG).anno(2011).voto(10).build();
    }

    // Test salvataggio stato

    @Test
    @DisplayName("salvaStato — incrementa la dimensione dello storico")
    void salvaStatoIncrementaDimensione() {
        storico.salvaStato(gioco);

        assertThat(storico.dimensione()).isEqualTo(1);
        assertThat(storico.isVuoto()).isFalse();
    }

    @Test
    @DisplayName("salvaStato con gioco null — lancia IllegalArgumentException")
    void salvaStatoConGiocoNull() {
        assertThatThrownBy(() -> storico.salvaStato(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("salvaStato multipli — tutti salvati nello stack")
    void salvaStatoMultipli() {
        storico.salvaStato(gioco);
        gioco.setVoto(9);
        storico.salvaStato(gioco);

        assertThat(storico.dimensione()).isEqualTo(2);
    }

    // Test ripristino stato

    @Test
    @DisplayName("annullaUltimaModifica — riporta il gioco allo stato precedente")
    void ripristinaStatoPrecedente() {
        storico.salvaStato(gioco); // salva con voto=10
        gioco.setVoto(5);          // modifica il voto

        Optional<GameMemento> result = storico.annullaUltimaModifica(gioco);

        assertThat(result).isPresent();
        assertThat(gioco.getVoto()).isEqualTo(10); // ripristinato
    }

    @Test
    @DisplayName("annullaUltimaModifica su storico vuoto — restituisce Optional vuoto")
    void annullaConStoricoVuoto() {
        Optional<GameMemento> result = storico.annullaUltimaModifica(gioco);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("annullaUltimaModifica con gioco null — lancia IllegalArgumentException")
    void annullaConGiocoNull() {
        assertThatThrownBy(() -> storico.annullaUltimaModifica(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("annullaUltimaModifica per gioco diverso — memento rimesso nello stack")
    void annullaPerGiocoDiverso() {
        storico.salvaStato(gioco);

        Game altroGioco = new Game.Builder("Elden Ring")
                .piattaforma(Piattaforma.PLAYSTATION).genere(Genere.RPG).anno(2022).voto(9).build();

        Optional<GameMemento> result = storico.annullaUltimaModifica(altroGioco);

        assertThat(result).isEmpty();
        assertThat(storico.dimensione()).isEqualTo(1); // memento rimesso nello stack
    }

    // Test limite massimo dello stack

    @Test
    @DisplayName("Superamento limite massimo — il memento più vecchio viene scartato")
    void superamentoLimiteMaxScartaIlPiuVecchio() {
        // storico ha limite 3
        storico.salvaStato(gioco);
        gioco.setVoto(9);
        storico.salvaStato(gioco);
        gioco.setVoto(8);
        storico.salvaStato(gioco);
        gioco.setVoto(7);

        // Quarto salvataggio: il più vecchio viene rimosso
        storico.salvaStato(gioco);

        assertThat(storico.dimensione()).isEqualTo(3); // non supera il limite
    }

    // Test ultimoStato (peek)

    @Test
    @DisplayName("ultimoStato — peek senza rimuovere dal stack")
    void ultimoStatoPeekSenzaRimuovere() {
        storico.salvaStato(gioco);

        Optional<GameMemento> peek = storico.ultimoStato();

        assertThat(peek).isPresent();
        assertThat(peek.get().getTitolo()).isEqualTo("Dark Souls");
        assertThat(storico.dimensione()).isEqualTo(1); // non rimosso
    }

    @Test
    @DisplayName("ultimoStato su storico vuoto — restituisce Optional vuoto")
    void ultimoStatoStoricoVuoto() {
        assertThat(storico.ultimoStato()).isEmpty();
    }

    // Test metodi di utilità

    @Test
    @DisplayName("svuota — rimuove tutti i memento dallo storico")
    void svuotaRimuoveTuttiIMemento() {
        storico.salvaStato(gioco);
        storico.salvaStato(gioco);

        storico.svuota();

        assertThat(storico.isVuoto()).isTrue();
        assertThat(storico.dimensione()).isZero();
    }

    @Test
    @DisplayName("getLimiteMax — restituisce il limite configurato")
    void getLimiteMaxRestituisceLimite() {
        assertThat(storico.getLimiteMax()).isEqualTo(3);
    }

    @Test
    @DisplayName("Costruttore default — limite a LIMITE_DEFAULT (10)")
    void costruttoreDefaultLimiteDieci() {
        CaretakerStorico defaultStorico = new CaretakerStorico();
        assertThat(defaultStorico.getLimiteMax()).isEqualTo(CaretakerStorico.LIMITE_DEFAULT);
    }

    @Test
    @DisplayName("Costruttore con limite zero — lancia IllegalArgumentException")
    void costruttoreConLimiteZero() {
        assertThatThrownBy(() -> new CaretakerStorico(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Costruttore con limite negativo — lancia IllegalArgumentException")
    void costruttoreConLimiteNegativo() {
        assertThatThrownBy(() -> new CaretakerStorico(-5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Ripristino multiplo — annulla più modifiche in sequenza LIFO")
    void ripristinoMultiploInSequenzaLifo() {
        storico.salvaStato(gioco); // voto=10
        gioco.setVoto(8);
        storico.salvaStato(gioco); // voto=8
        gioco.setVoto(5);

        // Primo annullamento: ripristina a voto=8
        Optional<GameMemento> primo = storico.annullaUltimaModifica(gioco);
        assertThat(primo).isPresent();
        assertThat(gioco.getVoto()).isEqualTo(8);

        // Secondo annullamento: ripristina a voto=10
        Optional<GameMemento> secondo = storico.annullaUltimaModifica(gioco);
        assertThat(secondo).isPresent();
        assertThat(gioco.getVoto()).isEqualTo(10);

        // Storico ora vuoto
        assertThat(storico.isVuoto()).isTrue();
    }
}
