package com.gamelibrary.service;

import com.gamelibrary.exceptions.GameNotFoundException;
import com.gamelibrary.memento.CaretakerStorico;
import com.gamelibrary.model.Game;
import com.gamelibrary.model.Genere;
import com.gamelibrary.model.Piattaforma;
import com.gamelibrary.persistence.Persistenza;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.gamelibrary.util.Risultato;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitari per {@link GameLibraryService}.
 * In questo test usiamo un mock di Mockito per Persistenza,
 * garantendo che nessun file venga scritto su disco.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GameLibraryService — test (Mock persistenza)")
class GameLibraryServiceTest {

    @Mock
    private Persistenza persistenzaMock;

    private GameLibraryService service;
    private CaretakerStorico storicoFake;

    @BeforeEach
    void setUp() {
        // Configura il mock per ritornare una lista vuota quando si chiama carica()
        try {
            lenient().when(persistenzaMock.carica()).thenReturn(new ArrayList<>());
        } catch (com.gamelibrary.exceptions.PersistenzaException e) {
            // Ignorata nel test
        }

        // Uso un'istanza reale di CaretakerStorico dato che non fa I/O su disco
        storicoFake = new CaretakerStorico();

        service = new GameLibraryService(persistenzaMock, storicoFake);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @Test
    @DisplayName("aggiungiGioco — con parametri validi restituisce successo")
    void aggiungiGiocoValido() {
        Risultato<Game> risultato = service.aggiungiGioco(
                "Dark Souls", Piattaforma.PLAYSTATION, Genere.RPG, 2011, 10);

        assertThat(risultato.isSuccesso()).isTrue();
        assertThat(risultato.getValore()).isNotNull();
        assertThat(risultato.getValore().getTitolo()).isEqualTo("Dark Souls");
    }

    @Test
    @DisplayName("aggiungiGioco — con titolo null restituisce fallimento")
    void aggiungiGiocoTitoloNull() {
        Risultato<Game> risultato = service.aggiungiGioco(
                null, Piattaforma.STEAM, Genere.ACTION, 2020, 8);

        assertThat(risultato.isSuccesso()).isFalse();
        assertThat(risultato.isFallimento()).isTrue();
    }

    @Test
    @DisplayName("rimuoviGioco — con ID valido rimuove il gioco")
    void rimuoviGiocoEsistente() throws GameNotFoundException {
        Risultato<Game> aggiunto = service.aggiungiGioco(
                "Hollow Knight", Piattaforma.STEAM, Genere.PLATFORM, 2017, 9);
        String id = aggiunto.getValore().getId();

        Risultato<Game> risultato = service.rimuoviGioco(id);

        assertThat(risultato.isSuccesso()).isTrue();
        assertThat(service.numeroDiGiochi()).isZero();
    }

    @Test
    @DisplayName("rimuoviGioco — con ID inesistente lancia eccezione")
    void rimuoviGiocoInesistente() {
        assertThatThrownBy(() -> service.rimuoviGioco("id-inesistente"))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    @DisplayName("cercaPerId — con ID esistente restituisce successo")
    void cercaPerIdEsistente() {
        Risultato<Game> aggiunto = service.aggiungiGioco(
                "Elden Ring", Piattaforma.PLAYSTATION, Genere.RPG, 2022, 9);
        String id = aggiunto.getValore().getId();

        Risultato<Game> risultato = service.cercaPerId(id);

        assertThat(risultato.isSuccesso()).isTrue();
        assertThat(risultato.getValore().getTitolo()).isEqualTo("Elden Ring");
    }

    @Test
    @DisplayName("cercaPerId — con ID inesistente restituisce fallimento")
    void cercaPerIdInesistente() {
        Risultato<Game> risultato = service.cercaPerId("id-fake");

        assertThat(risultato.isFallimento()).isTrue();
    }

    @Test
    @DisplayName("annullaUltimaModifica — con storico vuoto restituisce fallimento")
    void annullaUltimaModificaVuoto() {
        // Il CaretakerStorico è appena inizializzato, quindi è vuoto
        Risultato<Game> risultato = service.annullaUltimaModifica();

        assertThat(risultato.isSuccesso()).isFalse();
        assertThat(risultato.getMessaggioErrore()).contains("Non ci sono modifiche");
    }

    @Test
    @DisplayName("carica — popola la libreria con i dati dalla persistenza")
    void carica_popolaLaLibreriaConIDatiDellaPersistenza() throws com.gamelibrary.exceptions.PersistenzaException {
        List<Game> giochiSimulati = new ArrayList<>();
        giochiSimulati.add(new Game.Builder("Zelda").piattaforma(Piattaforma.NINTENDO).genere(Genere.ACTION).anno(1998).voto(10).build());
        
        when(persistenzaMock.carica()).thenReturn(giochiSimulati);

        service.carica();

        assertThat(service.isVuota()).isFalse();
        assertThat(service.numeroDiGiochi()).isEqualTo(1);
        assertThat(service.getTuttiIGiochi().get(0).getTitolo()).isEqualTo("Zelda");
    }

    @Test
    @DisplayName("salvataggio asincrono — verifica che salva() venga chiamato")
    void aggiungiGioco_chiamaSalvaInModoAsincrono() throws com.gamelibrary.exceptions.PersistenzaException {
        service.aggiungiGioco("Mario", Piattaforma.NINTENDO, Genere.PLATFORM, 1985, 10);

        // Usiamo timeout() perché il salvataggio avviene in un thread separato
        verify(persistenzaMock, timeout(1000).times(1)).salva(anyList());
    }

    @Test
    @DisplayName("salvataggio asincrono — eccezione I/O non blocca il flusso principale")
    void aggiungiGioco_conPersistenzaException_nonInterrompeApplicazione() throws com.gamelibrary.exceptions.PersistenzaException {
        doThrow(new com.gamelibrary.exceptions.PersistenzaException("Errore simulato di I/O"))
            .when(persistenzaMock).salva(anyList());

        Risultato<Game> risultato = service.aggiungiGioco("Metroid", Piattaforma.NINTENDO, Genere.ACTION, 1986, 9);

        // Il thread principale riceve comunque successo perché il salvataggio è asincrono
        assertThat(risultato.isSuccesso()).isTrue();
        
        // Verifica che abbia provato a salvare
        verify(persistenzaMock, timeout(1000).times(1)).salva(anyList());
    }
}
