package central;

import central.soap.ConsultaException;
import central.soap.EventosPublicador;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DisponibilidadSoapTest {
    private ISistema sistema(List<String> llamadas, boolean falla) {
        return (ISistema) Proxy.newProxyInstance(ISistema.class.getClassLoader(),
                new Class<?>[]{ISistema.class}, (obj, metodo, args) -> {
                    if (!metodo.getName().equals("existeUsuario") &&
                            !metodo.getName().equals("existeCorreoElectronico"))
                        throw new AssertionError("Operación inesperada: " + metodo.getName());
                    if (falla) throw new IllegalStateException("Detalle interno de base de datos");
                    llamadas.add(metodo.getName() + ":" + args[0]);
                    return "ocupado".equals(args[0]) || "usado@ejemplo.com".equals(args[0]);
                });
    }

    @Test void consultaLogicaConIdentificadoresNormalizados() throws Exception {
        List<String> llamadas = new ArrayList<>();
        var servicio = new EventosPublicador(sistema(llamadas, false));
        assertFalse(servicio.nicknameDisponible(" ocupado "));
        assertTrue(servicio.nicknameDisponible(" nuevo "));
        assertFalse(servicio.correoDisponible(" usado@ejemplo.com "));
        assertTrue(servicio.correoDisponible(" nuevo@ejemplo.com "));
        assertEquals(List.of("existeUsuario:ocupado", "existeUsuario:nuevo",
                "existeCorreoElectronico:usado@ejemplo.com",
                "existeCorreoElectronico:nuevo@ejemplo.com"), llamadas);
    }

    @Test void entradasVaciasNoConsultanLogica() throws Exception {
        List<String> llamadas = new ArrayList<>();
        var servicio = new EventosPublicador(sistema(llamadas, false));
        assertFalse(servicio.nicknameDisponible("  "));
        assertFalse(servicio.nicknameDisponible(null));
        assertFalse(servicio.correoDisponible(""));
        assertFalse(servicio.correoDisponible(null));
        assertTrue(llamadas.isEmpty());
    }

    @Test void erroresInternosSeConviertenEnFaultSinFiltrarDetalles() {
        var servicio = new EventosPublicador(sistema(new ArrayList<>(), true));
        ConsultaException error = assertThrows(ConsultaException.class,
                () -> servicio.nicknameDisponible("alguien"));
        assertFalse(error.getMessage().contains("base de datos"));
        assertThrows(ConsultaException.class, () -> servicio.correoDisponible("a@b.com"));
    }
}
