package implementacion;

import datos.CargarDatosLab3;
import datatypes.*;
import org.junit.jupiter.api.Test;
import persistencia.BaseDeDatos;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DatosLab3Test extends soporte.PruebaConBaseSegura {
    @Test void preparaTodosLosEscenariosSinReemplazarDatosYConservaElConjuntoTrasReiniciar() throws Exception {
        var s = Fabrica.getInstance().getISistema();
        String prefijo = "seed_"+UUID.randomUUID().toString().replace("-", "");
        var datos = CargarDatosLab3.cargar(s,prefijo,UUID.randomUUID().toString());
        assertThrows(IllegalArgumentException.class, () -> CargarDatosLab3.cargar(s,prefijo,"otra-clave"));
        BaseDeDatos.cerrar(); BaseDeDatos.inicializar();
        var actorOrg = Actor.usuarioAutenticado(s.consultarUsuario(datos.organizador()));
        var actorAs = Actor.usuarioAutenticado(s.consultarUsuario(datos.asistente()));
        assertTrue(s.consultarRelaciones(datos.organizador(),actorAs).siguiendo());
        assertTrue(s.consultarRelaciones(datos.asistente(),actorOrg).siguiendo());
        assertTrue(s.listarEventosPublicos().stream().noneMatch(e -> datos.eventoFinalizado().equals(e.getNombre())));
        assertFalse(s.consultarEdiciones("",datos.conVideo(),actorAs).edicion().getVideoYoutube().isBlank());
        assertNull(s.consultarEdiciones("",datos.sinVideo(),actorAs).edicion().getVideoYoutube());
        assertEquals(EstadoEdicion.ARCHIVADA,s.consultarPerfil(datos.organizador(),actorOrg).ediciones().stream()
                .filter(e -> datos.archivada().equals(e.getIdNombre())).findFirst().orElseThrow().getEstado());
        assertEquals(EstadoEdicion.INGRESADA,s.consultarEdiciones("",datos.ingresada(),actorOrg).edicion().getEstado());
        assertEquals(EstadoEdicion.RECHAZADA,s.consultarEdiciones("",datos.rechazada(),actorOrg).edicion().getEstado());
        assertEquals(datos.archivada(),s.obtenerDatosConstancia(datos.archivada(),actorAs).getNombreEdicion());
        assertFalse(s.obtenerRegistrosAsistente(datos.otroAsistente()).stream().anyMatch(DtRegistro::isAsistenciaConfirmada));
        assertEquals(2,sistemaVisitas(s,datos.evento()));
        s.archivarEdicion(datos.archivable(),actorOrg);
    }

    private long sistemaVisitas(interfaces.ISistema s,String nombre) {
        try(var em=BaseDeDatos.getEntityManager()) { return em.find(clases.Evento.class,nombre).getVisitas(); }
    }
}
