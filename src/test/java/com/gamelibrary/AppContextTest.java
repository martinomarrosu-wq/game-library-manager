package com.gamelibrary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AppContextTest {

    @TempDir
    Path tempDir;

    @Test
    void inizializzazioneCaricaIGiochiSalvatiDalJson() throws Exception {
        Path root = tempDir.resolve("workspace");
        Files.createDirectories(root);
        Files.writeString(
                root.resolve("library.json"),
                "[{\"id\":\"abc\",\"titolo\":\"Zelda\",\"piattaforma\":\"PLAYSTATION\",\"genere\":\"ACTION\",\"anno\":2023,\"voto\":8}]",
                StandardCharsets.UTF_8);

        System.setProperty("user.dir", root.toString());

        AppContext context = new AppContext();

        assertThat(context.getGameLibraryService().numeroDiGiochi()).isEqualTo(1);
    }
}
