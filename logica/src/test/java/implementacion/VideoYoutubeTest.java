package implementacion;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VideoYoutubeTest {
    @Test void aceptaEnlacesYGeneraEmbedSeguro() {
        String esperado = "https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ";
        assertEquals(esperado, VideoYoutube.normalizar("https://youtu.be/dQw4w9WgXcQ"));
        assertEquals(esperado, VideoYoutube.normalizar("https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=20"));
        assertEquals(esperado, VideoYoutube.normalizar("https://www.youtube.com/embed/dQw4w9WgXcQ"));
    }
    @Test void admiteEdicionesSinVideo() {
        assertNull(VideoYoutube.normalizar(null));
        assertNull(VideoYoutube.normalizar("   "));
    }
    @Test void rechazaHostsYEsquemasInseguros() {
        for (String enlace : new String[] {
                "http://youtube.com/watch?v=dQw4w9WgXcQ",
                "https://youtube.com.evil.org/watch?v=dQw4w9WgXcQ",
                "https://evil.org/embed/dQw4w9WgXcQ",
                "javascript:alert(1)",
                "https://user:pass@youtube.com/watch?v=dQw4w9WgXcQ",
                "https://youtube.com/watch?v=<script>"}) {
            assertThrows(IllegalArgumentException.class, () -> VideoYoutube.normalizar(enlace), enlace);
        }
    }
}
