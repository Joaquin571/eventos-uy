package web;

import contratos.ServicioPortal;
import contratos.dto.*;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;
import web.remoto.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SesionMovilWebTest {
    @Test void loginMovilRenuevaSesionYConservaSoloIdentidadMinima() {
        var e = new Escenario();
        e.datos.put(SesionWeb.CREDENCIAL, "anterior");
        e.datos.put("datoAnterior", "privado");
        SesionWeb.iniciarMovil(e.req, "asistente", "clave");
        assertEquals(List.of("anterior"), e.revocadas);
        assertEquals(1, e.invalidaciones);
        assertFalse(e.datos.containsKey("datoAnterior"));
        assertEquals("nueva", e.datos.get(SesionWeb.CREDENCIAL));
        var usuario = assertInstanceOf(DtAsistente.class, e.datos.get("usuario"));
        assertEquals("asistente", usuario.getNickname());
        assertNull(usuario.getCorreoElectronico());
        assertNull(usuario.getContrasenia());
        assertEquals("asistente", SesionWeb.usuarioMovilActual(e.req).getNickname());
        SesionWeb.cerrar(e.req);
        assertEquals(List.of("anterior", "nueva"), e.revocadas);
        assertTrue(e.datos.isEmpty());
    }

    @Test void credencialesInvalidasNoReemplazanSesionExistente() {
        var e = new Escenario(); e.falloLogin = true;
        e.datos.put(SesionWeb.CREDENCIAL, "anterior");
        assertThrows(IllegalArgumentException.class, () -> SesionWeb.iniciarMovil(e.req, "asistente", "mala"));
        assertEquals("anterior", e.datos.get(SesionWeb.CREDENCIAL));
        assertTrue(e.revocadas.isEmpty());
        assertEquals(0, e.invalidaciones);
    }

    @Test void adaptadorConRolIncorrectoNoCreaSesionMovil() {
        var e = new Escenario(); e.rol = "ORGANIZADOR";
        assertEquals("ACCESO_DENEGADO", assertThrows(ErrorCentral.class,
                () -> SesionWeb.iniciarMovil(e.req, "org", "clave")).getCodigo());
        assertEquals(List.of("nueva"), e.revocadas);
        assertFalse(e.datos.containsKey(SesionWeb.CREDENCIAL));
    }

    @Test void falloRevocandoSesionPreviaTambienRevocaLaNueva() {
        var e = new Escenario(); e.falloCierre = true;
        e.datos.put(SesionWeb.CREDENCIAL, "anterior");
        assertThrows(ErrorCentral.class, () -> SesionWeb.iniciarMovil(e.req, "asistente", "clave"));
        assertEquals(List.of("anterior", "nueva"), e.revocadas);
        assertTrue(e.datos.isEmpty());
    }

    @Test void consultaMovilPropagaSesionVencidaDesdeCentral() {
        var e = new Escenario(); e.vencida = true;
        assertEquals("SESION_INVALIDA", assertThrows(ErrorCentral.class,
                () -> SesionWeb.usuarioMovilActual(e.req)).getCodigo());
    }

    private static class Escenario {
        final Map<String,Object> datos = new HashMap<>();
        final List<String> revocadas = new ArrayList<>();
        int invalidaciones;
        boolean falloLogin, falloCierre, vencida;
        String rol = "ASISTENTE";
        final ServicioSesiones sesiones = new ServicioSesiones() {
            public ClienteSesionesCentral.Inicio iniciar(String id, String clave) { throw new AssertionError("Usar login movil"); }
            public ClienteSesionesCentral.Inicio iniciarMovil(String id, String clave) {
                if (falloLogin) throw new IllegalArgumentException("Credenciales invalidas");
                return new ClienteSesionesCentral.Inicio("asistente", "Nombre", rol, "nueva");
            }
            public void cerrar(String token) {
                revocadas.add(token);
                if (falloCierre && "anterior".equals(token)) throw new ErrorCentral("SERVICIO_NO_DISPONIBLE", "Central no disponible");
            }
        };
        final ServicioPortal portal = CsrfTest.doble(ServicioPortal.class, (p,m,a) -> {
            assertEquals("usuarioMovilActual", m.getName());
            if (vencida) throw new ErrorCentral("SESION_INVALIDA", "Sesion vencida");
            var usuario = new DtAsistente(); usuario.setNickname("asistente"); return usuario;
        });
        final ServletContext contexto = CsrfTest.doble(ServletContext.class,
                (p,m,a) -> "sesionesCentral".equals(a[0]) ? sesiones : portal);
        final HttpSession session = CsrfTest.doble(HttpSession.class, (p,m,a) -> switch(m.getName()) {
            case "getAttribute" -> datos.get(a[0]);
            case "setAttribute" -> { datos.put((String)a[0],a[1]); yield null; }
            case "invalidate" -> { invalidaciones++; datos.clear(); yield null; }
            default -> throw new AssertionError(m.getName());
        });
        final HttpServletRequest req = CsrfTest.doble(HttpServletRequest.class, (p,m,a) -> switch(m.getName()) {
            case "getSession" -> session;
            case "getServletContext" -> contexto;
            default -> throw new AssertionError(m.getName());
        });
    }
}
