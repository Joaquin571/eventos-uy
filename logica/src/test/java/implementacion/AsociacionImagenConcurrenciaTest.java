package implementacion;

import datatypes.*;
import implementacion.Fabrica;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;
import persistencia.BaseDeDatos;
import soporte.ImagenesPrueba;
import soporte.PruebaConBaseSegura;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Regresión del hash restaurado por una asociación de imagen con entidad obsoleta. */
class AsociacionImagenConcurrenciaTest extends PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private DtUsuario crear(boolean org) {
        String nick = "imagenCi" + UUID.randomUUID().toString().replace("-", "");
        DtUsuario dt = org ? new DtOrganizador(nick, "Original", nick+"@test.uy", "Descripción", "")
                : new DtAsistente(nick,"Original",nick+"@test.uy","Apellido",LocalDate.of(2000,1,1),null);
        dt.setContrasenia("anterior"); dt.setConfirmacionContrasenia("anterior");
        assertTrue(org ? sistema.altaOrganizador((DtOrganizador)dt) : sistema.altaAsistente((DtAsistente)dt));
        return dt;
    }
    private DtImagen imagen() { return new DtImagen("image/png", ImagenesPrueba.imagen("png")); }
    private void comprobar(boolean org, boolean perfil) throws Exception {
        DtUsuario dt=crear(org); Actor actor=Actor.usuarioAutenticado(dt);
        var pool=Executors.newFixedThreadPool(2);
        try(var em=BaseDeDatos.getEntityManager()) {
            var tx=em.getTransaction(); tx.begin();
            try {
                em.createNativeQuery("LOCK TABLE imagen IN SHARE MODE").executeUpdate();
                Future<?> foto=pool.submit(()->sistema.asociarImagen(TipoEntidadImagen.USUARIO,dt.getNickname(),imagen(), datatypes.Actor.administradorLocal()));
                boolean esperando=false; long limite=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
                while(System.nanoTime()<limite) {
                    em.createNativeQuery("SELECT pg_stat_clear_snapshot()").getSingleResult();
                    Number n=(Number)em.createNativeQuery("SELECT COUNT(*) FROM pg_stat_activity WHERE datname=current_database() AND wait_event_type='Lock' AND query ILIKE '%insert into imagen%'").getSingleResult();
                    if(n.intValue()>0) { esperando=true; break; }
                    if(foto.isDone()) foto.get();
                    Thread.sleep(20);
                }
                assertTrue(esperando,"La imagen debe estar retenida después de adquirir el bloqueo del usuario.");
                Future<?> cambio=pool.submit(()->{
                    if(perfil) {
                        dt.setNombre("Actualizado");dt.setContrasenia("nueva");dt.setConfirmacionContrasenia("nueva");
                        if(dt instanceof DtAsistente a) sistema.modificarAsistente(a,null,actor);
                        else sistema.modificarOrganizador((DtOrganizador)dt,null,actor);
                    } else sistema.cambiarContrasenia(dt.getNickname(),"nueva","nueva",actor);
                });
                assertThrows(TimeoutException.class,()->cambio.get(300,TimeUnit.MILLISECONDS));
                tx.rollback(); foto.get(15,TimeUnit.SECONDS); cambio.get(15,TimeUnit.SECONDS);
            } finally { if(tx.isActive()) tx.rollback(); }
        } finally { pool.shutdownNow(); }
        assertEquals(dt.getNickname(),sistema.autenticar(dt.getNickname(),"nueva").getNickname());
        assertThrows(IllegalArgumentException.class,()->sistema.autenticar(dt.getNickname(),"anterior"));
        assertEquals(perfil?"Actualizado":"Original",sistema.consultarUsuario(dt.getNickname()).getNombre());
        assertArrayEquals(imagen().getContenido(),sistema.obtenerImagen(TipoEntidadImagen.USUARIO,dt.getNickname()).getContenido());
    }
    @Test void imagenNoRestauraClaveDeAsistente() throws Exception { comprobar(false,false); }
    @Test void imagenNoRestauraClaveDeOrganizador() throws Exception { comprobar(true,false); }
    @Test void imagenNoRestauraPerfilDeAsistente() throws Exception { comprobar(false,true); }
    @Test void imagenNoRestauraPerfilDeOrganizador() throws Exception { comprobar(true,true); }
}
