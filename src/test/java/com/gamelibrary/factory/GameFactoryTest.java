package com.gamelibrary.factory;

import com.gamelibrary.exceptions.GameCreationException;
import com.gamelibrary.model.Game;
import com.gamelibrary.model.Genere;
import com.gamelibrary.model.Piattaforma;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Test unitari per {@link GameFactory} — verifica la creazione di giochi
 * tramite il Factory Pattern con validazione dei parametri.
 */
@DisplayName("GameFactory — creazione giochi")
class GameFactoryTest {

    @Test
    @DisplayName("Creazione gioco con parametri validi — successo")
    void creaGiocoConParametriValidi() throws GameCreationException {
        Game gioco = GameFactory.crea("Dark Souls", Piattaforma.PLAYSTATION, Genere.RPG, 2011, 10);

        assertThat(gioco).isNotNull();
        assertThat(gioco.getTitolo()).isEqualTo("Dark Souls");
        assertThat(gioco.getPiattaforma()).isEqualTo(Piattaforma.PLAYSTATION);
        assertThat(gioco.getGenere()).isEqualTo(Genere.RPG);
        assertThat(gioco.getAnno()).isEqualTo(2011);
        assertThat(gioco.getVoto()).isEqualTo(10);
        assertThat(gioco.getId()).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("Creazione gioco con titolo null — lancia GameCreationException")
    void creaGiocoConTitoloNull() {
        assertThatThrownBy(() ->
                GameFactory.crea(null, Piattaforma.STEAM, Genere.ACTION, 2020, 8))
                .isInstanceOf(GameCreationException.class);
    }

    @Test
    @DisplayName("Creazione gioco con titolo vuoto — lancia GameCreationException")
    void creaGiocoConTitoloVuoto() {
        assertThatThrownBy(() ->
                GameFactory.crea("   ", Piattaforma.XBOX, Genere.HORROR, 2019, 7))
                .isInstanceOf(GameCreationException.class);
    }

    @Test
    @DisplayName("Creazione gioco con anno troppo basso — lancia GameCreationException")
    void creaGiocoConAnnoTroppoBasso() {
        assertThatThrownBy(() ->
                GameFactory.crea("Test Game", Piattaforma.NINTENDO, Genere.PUZZLE, 1960, 5))
                .isInstanceOf(GameCreationException.class);
    }

    @Test
    @DisplayName("Creazione gioco con anno nel futuro — lancia GameCreationException")
    void creaGiocoConAnnoFuturo() {
        assertThatThrownBy(() ->
                GameFactory.crea("Test Game", Piattaforma.STEAM, Genere.RPG, 9999, 7))
                .isInstanceOf(GameCreationException.class);
    }

    @Test
    @DisplayName("Creazione gioco con voto troppo alto — lancia GameCreationException")
    void creaGiocoConVotoTroppoAlto() {
        assertThatThrownBy(() ->
                GameFactory.crea("Test Game", Piattaforma.STEAM, Genere.SPORT, 2020, 15))
                .isInstanceOf(GameCreationException.class);
    }

    @Test
    @DisplayName("Creazione gioco con voto troppo basso — lancia GameCreationException")
    void creaGiocoConVotoTroppoBasso() {
        assertThatThrownBy(() ->
                GameFactory.crea("Test Game", Piattaforma.STEAM, Genere.RPG, 2020, 0))
                .isInstanceOf(GameCreationException.class);
    }

    @Test
    @DisplayName("Creazione gioco con piattaforma null — lancia GameCreationException")
    void creaGiocoConPiattaformaNull() {
        assertThatThrownBy(() ->
                GameFactory.crea("Test Game", null, Genere.ACTION, 2020, 7))
                .isInstanceOf(GameCreationException.class);
    }

    @Test
    @DisplayName("Creazione gioco con genere null — lancia GameCreationException")
    void creaGiocoConGenereNull() {
        assertThatThrownBy(() ->
                GameFactory.crea("Test Game", Piattaforma.XBOX, null, 2020, 7))
                .isInstanceOf(GameCreationException.class);
    }

    @Test
    @DisplayName("Due giochi creati hanno ID univoci diversi")
    void dueGiochiConIdDiversi() throws GameCreationException {
        Game g1 = GameFactory.crea("Gioco 1", Piattaforma.STEAM, Genere.ACTION, 2020, 7);
        Game g2 = GameFactory.crea("Gioco 2", Piattaforma.XBOX, Genere.RPG, 2021, 8);

        assertThat(g1.getId()).isNotEqualTo(g2.getId());
    }
}
