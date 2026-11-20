package manejadores;

import clases.*;
import datatypes.*;
import implementacion.Fabrica;
import org.junit.jupiter.api.Test;
import persistencia.BaseDeDatos;
import soporte.PruebaConBaseSegura;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Fallo SQL durante el cascade de imagen: la transacción no deja un alta parcial. */
class AltaEventoEdicionRollbackImagenTest extends PruebaConBaseSegura {
    private final String sufijo=Long.toString(System.nanoTime());
    private long imagenes(){try(var em=BaseDeDatos.getEntityManager()){return em.createQuery("SELECT COUNT(i) FROM Imagen i",Long.class).getSingleResult();}}
    private Imagen imagenQueFallaEnPersistencia(){return new Imagen("x".repeat(256),new byte[]{1});}
    @Test void falloDeImagenRevierteTodoElEvento(){
        var m=ManejadorEventos.getInstance();String nombre="rollbackEv"+sufijo;
        var e=new Evento(nombre,"EV","Descripción",LocalDate.now());e.setImagen(imagenQueFallaEnPersistencia());long antes=imagenes();
        assertThrows(RuntimeException.class,()->m.addEvento(e));assertFalse(m.existeEvento(nombre));assertEquals(antes,imagenes());
    }
    @Test void falloDeImagenRevierteTodaLaEdicion()throws Exception {
        var s=Fabrica.getInstance().getISistema();var admin=Actor.administradorLocal();String nick="rollbackOrg"+sufijo,cat="rollbackCat"+sufijo,ev="rollbackPadre"+sufijo,nombre="rollbackEd"+sufijo;
        var org=new DtOrganizador(nick,"Org",nick+"@test.uy","Descripción","");org.setContrasenia("clave");org.setConfirmacionContrasenia("clave");s.altaOrganizador(org);
        s.altaCategoria(cat,null,admin);s.altaEvento(new DtEvento(ev,"EV","Desc",null,List.of(cat)),admin);
        var e=new Edicion(nombre,"ED",LocalDate.now(),LocalDate.now(),LocalDate.now(),"Ciudad","País",ManejadorUsuarios.getInstance().obtenerOrganizador(nick));
        e.setEvento(ManejadorEventos.getInstance().obtenerEvento(ev));e.setImagen(imagenQueFallaEnPersistencia());long antes=imagenes();
        assertThrows(RuntimeException.class,()->ManejadorEventos.getInstance().addEdicion(e));assertFalse(ManejadorEventos.getInstance().existeEdicion(nombre));assertEquals(antes,imagenes());
        assertTrue(ManejadorEventos.getInstance().existeEvento(ev));assertNotNull(s.consultarUsuario(nick));
    }
}
