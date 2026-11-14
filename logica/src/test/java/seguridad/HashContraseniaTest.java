package seguridad;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Rechazo seguro de credenciales almacenadas inválidas; no usa PostgreSQL. */
class HashContraseniaTest {
    @Test
    void rechazaHashOContraseniaNulosSinLanzarExcepcion() {
        String valido = HashContrasenia.hashear("clave");
        assertFalse(HashContrasenia.coincide("clave", null));
        assertFalse(HashContrasenia.coincide(null, valido));
        assertFalse(HashContrasenia.coincide(null, null));
        assertTrue(HashContrasenia.coincide("clave", valido));
    }

    @Test
    void rechazaFormatoIncompletoSinAceptarLaClaveEnTextoPlano() {
        for (String almacenado : new String[]{"", "clave", "120000", "120000:c2Fs", "120000:c2Fs:", "1:a:b:c"}) {
            assertFalse(HashContrasenia.coincide("clave", almacenado), "Formato inválido: " + almacenado);
        }
    }

    @Test
    void rechazaBase64IncorrectoEnSalOHash() {
        for (String almacenado : new String[]{"120000:@@@:aGFzaA==", "120000:c2Fs:@@@"}) {
            assertFalse(HashContrasenia.coincide("clave", almacenado));
        }
    }

    @Test
    void rechazaIteracionesInvalidasYConservaElFuncionamientoConUnHashValido() {
        for (String almacenado : new String[]{"texto:c2Fs:aGFzaA==", "0:c2Fs:aGFzaA==", "-1:c2Fs:aGFzaA=="}) {
            assertFalse(HashContrasenia.coincide("clave", almacenado));
        }
        String valido = HashContrasenia.hashear("clave");
        assertTrue(HashContrasenia.coincide("clave", valido));
        assertFalse(HashContrasenia.coincide("otra", valido));
    }
}
