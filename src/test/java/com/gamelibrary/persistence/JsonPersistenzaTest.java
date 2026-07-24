package com.gamelibrary.persistence;

import com.gamelibrary.model.Game;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonPersistenzaTest {

    @TempDir
    Path tempDir;

    private String originalUserDir;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
    }

    @AfterEach
    void tearDown() {
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void carica_risolvePercorsoRelativoRispettoAllaRootDelProgetto() throws Exception {
        Path progetto = tempDir.resolve("progetto");
        Path sottocartella = progetto.resolve("src");
        Files.createDirectories(sottocartella);

        Path fileJson = progetto.resolve("library.json");
        Files.writeString(fileJson,
                "[{\"id\":\"abc\",\"titolo\":\"Zelda\",\"piattaforma\":\"PLAYSTATION\",\"genere\":\"ACTION\",\"anno\":2023,\"voto\":8}]",
                StandardCharsets.UTF_8);

        System.setProperty("user.dir", sottocartella.toString());

        JsonPersistenza persistenza = new JsonPersistenza("library.json");
        List<Game> giochi = persistenza.carica();

        assertThat(giochi)
                .hasSize(1)
                .extracting(Game::getTitolo)
                .containsExactly("Zelda");
    }
}
