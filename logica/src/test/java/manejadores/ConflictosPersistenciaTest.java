package manejadores;

import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class ConflictosPersistenciaTest {
    @Test void unicidadSeTraduceSinDependerDelTextoDelDriver() {
        var original=new RuntimeException(new RuntimeException(new SQLException("detalle técnico","23505")));
        var resultado=ConflictosPersistencia.traducir(original,"Dato repetido.");
        assertInstanceOf(IllegalArgumentException.class,resultado);
        assertEquals("Dato repetido.",resultado.getMessage());assertSame(original,resultado.getCause());
    }
    @Test void otrosEstadosSqlNoSeDisfrazanDeDuplicados() {
        var original=new RuntimeException(new SQLException("dato demasiado largo","22001"));
        assertSame(original,ConflictosPersistencia.traducir(original,"Dato repetido."));
    }
    @Test void erroresDeNegocioSeConservan() {
        var original=new IllegalArgumentException("Validación de negocio.");
        assertSame(original,ConflictosPersistencia.traducir(original,"Dato repetido."));
    }
}
