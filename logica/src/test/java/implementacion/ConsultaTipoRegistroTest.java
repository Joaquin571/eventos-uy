package implementacion;

import datatypes.*;
import interfaces.ISistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistencia.BaseDeDatos;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConsultaTipoRegistroTest {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = String.valueOf(System.nanoTime());
    private final String primera = "Primera consulta " + sufijo, segunda = "Segunda consulta " + sufijo;

    @BeforeEach
    void prepararSoloEnBaseDePruebas() throws Exception {
        assertEquals("eventosuy_test", System.getenv("EVENTOS_DB_NAME"));
        var p = BaseDeDatos.propiedadesJpa();
        assertTrue(p.get("jakarta.persistence.jdbc.url").endsWith("/eventosuy_test"));
        Class.forName("org.postgresql.Driver");
        try (var c = DriverManager.getConnection(p.get("jakarta.persistence.jdbc.url"),
                p.get("jakarta.persistence.jdbc.user"), p.get("jakarta.persistence.jdbc.password"))) {
            c.setReadOnly(true);
            try (var s = c.createStatement(); var r = s.executeQuery("SELECT current_database()")) {
                assertTrue(r.next());assertEquals("eventosuy_test", r.getString(1));
            }
        }
        String nick = "orgTipo" + sufijo;
        DtOrganizador d = new DtOrganizador(nick,"Organizador",nick+"@test.uy","Descripción","");
        d.setContrasenia("clave");d.setConfirmacionContrasenia("clave");assertTrue(sistema.altaOrganizador(d));
        String cat = "catTipo"+sufijo, evento="Evento tipos "+sufijo;
        sistema.altaCategoria(cat,null);LocalDate hoy=LocalDate.now();
        assertTrue(sistema.altaEvento(new DtEvento(evento,"EV","Descripción",hoy,List.of(cat))));
        for (String nombre : List.of(primera,segunda)) {
            assertTrue(sistema.altaEdicion(new DtEdicion(nombre,"ED",hoy,hoy,hoy,"Ciudad","País",nick),evento));
        }
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General","Primera descripción",100,25),primera));
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General","Segunda descripción",200,50),segunda));
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("Exclusivo","Solo en segunda",0,5),segunda));
    }

    @Test
    void mismoNombreEnDistintasEdicionesConservaSusDatos() {
        DtTipoRegistro a=sistema.consultarTipoRegistro(primera,"General"),b=sistema.consultarTipoRegistro(segunda,"General");
        assertEquals("General",a.getIdNombre());assertEquals("Primera descripción",a.getDescripcion());
        assertEquals(100f,a.getCosto());assertEquals(25,a.getCupo());assertEquals(200f,b.getCosto());assertEquals(50,b.getCupo());
    }

    @Test
    void tiposAjenosOInexistentesDevuelvenNull() {
        assertNull(sistema.consultarTipoRegistro(primera,"Exclusivo"));
        assertEquals("General", sistema.consultarTipoRegistro(primera,"general").getIdNombre());
        assertNull(sistema.consultarTipoRegistro("NoExiste"+sufijo,"General"));
    }

    @Test
    void listadoSoloIncluyeLosTiposDeLaEdicion() {
        assertEquals(List.of("General"),sistema.obtenerTiposRegistroEdicion(primera).stream().map(DtTipoRegistro::getIdNombre).toList());
        assertEquals(2,sistema.obtenerTiposRegistroEdicion(segunda).size());
    }
}
