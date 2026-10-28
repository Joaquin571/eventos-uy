package implementacion;

import clases.NivelPatrocinio;
import datatypes.DtPatrocinio;
import interfaces.ISistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class PatrocinioTest {

    private ISistema sistema;

    @BeforeEach
    void setUp() {
        sistema = Fabrica.getInstance().getISistema();
    }

    @Test
    void testAltaPatrocinioExcedeVentePorCiento() {
        DtPatrocinio dt = new DtPatrocinio(
                LocalDate.now(),
                1000.0f,
                5,
                "COD_TEST_20",
                NivelPatrocinio.ORO,
                "UTEC",
                "Edicion2026",
                "General"
        );

        assertThrows(IllegalArgumentException.class, () -> {
            sistema.altaPatrocinio(dt);
        });
    }

    @Test
    void testAltaPatrocinioCodigoDuplicado() {
        DtPatrocinio dt1 = new DtPatrocinio(
                LocalDate.now(),
                5000.0f,
                1,
                "COD_REPETIDO",
                NivelPatrocinio.PLATINO,
                "ANTEL",
                "Edicion2026",
                "General"
        );

        if (sistema.consultarPatrocinio("COD_REPETIDO") != null) {
            assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(dt1));
        }
    }
}