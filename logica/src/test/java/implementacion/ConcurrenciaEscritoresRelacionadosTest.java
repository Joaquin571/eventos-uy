package implementacion;

import datatypes.*;
import clases.NivelPatrocinio;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;
import persistencia.BaseDeDatos;
import soporte.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ConcurrenciaEscritoresRelacionadosTest extends PruebaConBaseSegura {
    private final ISistema sistema=Fabrica.getInstance().getISistema();
    private final String sufijo=Long.toString(System.nanoTime());
    private final String edicion="ceEd"+sufijo,organizador="ceOrg"+sufijo;
    private void preparar(boolean aceptar)throws Exception {
        var org=new DtOrganizador(organizador,"Organizador",organizador+"@test.uy","Descripción","");
        org.setContrasenia("clave");org.setConfirmacionContrasenia("clave");sistema.altaOrganizador(org);
        String cat="ceCat"+sufijo,ev="ceEv"+sufijo;sistema.altaCategoria(cat,null);
        sistema.altaEvento(new DtEvento(ev,"EV","Evento",null,List.of(cat)));
        sistema.altaEdicion(new DtEdicion(edicion,"ED",LocalDate.now(),LocalDate.now(),null,"Ciudad","País",organizador),ev);
        sistema.altaTipoRegistro(new DtTipoRegistro("General","Entrada",100,10),edicion,Actor.administradorLocal());
        if(aceptar)sistema.aceptarRechazarEdicion(edicion,true);
    }
    private void esperar(jakarta.persistence.EntityManager em,String consulta,Future<?> tarea)throws Exception {
        long fin=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
        while(System.nanoTime()<fin){
            em.createNativeQuery("SELECT pg_stat_clear_snapshot()").getSingleResult();
            Number n=(Number)em.createNativeQuery("SELECT COUNT(*) FROM pg_stat_activity WHERE datname=current_database() AND wait_event_type='Lock' AND query ILIKE :consulta")
                    .setParameter("consulta",consulta).getSingleResult();
            if(n.intValue()>0)return;if(tarea.isDone())tarea.get();Thread.sleep(20);
        }
        fail("No se alcanzó la intercalación retenida: "+consulta);
    }
    @Test void cambioDeEstadoNoPierdeImagenConfirmadaMientrasEspera()throws Exception {
        preparar(false);var foto=new DtImagen("image/png",ImagenesPrueba.imagen("png"));
        var pool=Executors.newFixedThreadPool(2);
        try(var em=BaseDeDatos.getEntityManager()){
            var tx=em.getTransaction();tx.begin();
            try{
                em.createNativeQuery("LOCK TABLE imagen IN SHARE MODE").executeUpdate();
                var imagen=pool.submit(()->sistema.asociarImagen(TipoEntidadImagen.EDICION,edicion,foto));
                esperar(em,"%insert into imagen%",imagen);
                var estado=pool.submit(()->sistema.aceptarRechazarEdicion(edicion,true));
                assertThrows(TimeoutException.class,()->estado.get(300,TimeUnit.MILLISECONDS));
                tx.rollback();imagen.get(15,TimeUnit.SECONDS);estado.get(15,TimeUnit.SECONDS);
            }finally{if(tx.isActive())tx.rollback();}
        }finally{pool.shutdownNow();}
        var actual=sistema.consultarDetalleEdicion(edicion,Actor.administradorLocal());assertEquals(EstadoEdicion.ACEPTADA,actual.edicion().getEstado());
        var imagen=sistema.obtenerImagen(TipoEntidadImagen.EDICION,edicion);assertNotNull(imagen,"El cambio de estado no puede restaurar imagen_id obsoleto.");assertArrayEquals(foto.getContenido(),imagen.getContenido());
    }
    @Test void dosDecisionesRetenidasNoPuedenAceptarYRechazarLaMismaEdicion()throws Exception {
        preparar(false);var pool=Executors.newFixedThreadPool(2);
        try(var em=BaseDeDatos.getEntityManager()){
            var tx=em.getTransaction();tx.begin();
            try{
                em.createNativeQuery("LOCK TABLE edicion IN SHARE MODE").executeUpdate();
                var primera=pool.submit(()->decidir(true));esperar(em,"%update edicion%",primera);
                var segunda=pool.submit(()->decidir(false));assertThrows(TimeoutException.class,()->segunda.get(300,TimeUnit.MILLISECONDS));
                tx.rollback();assertTrue(primera.get(15,TimeUnit.SECONDS));assertFalse(segunda.get(15,TimeUnit.SECONDS));
            }finally{if(tx.isActive())tx.rollback();}
        }finally{pool.shutdownNow();}
        assertEquals(EstadoEdicion.ACEPTADA,sistema.consultarDetalleEdicion(edicion,Actor.administradorLocal()).edicion().getEstado());
    }
    private boolean decidir(boolean aceptar){try{sistema.aceptarRechazarEdicion(edicion,aceptar);return true;}catch(IllegalArgumentException | IllegalStateException e){return false;}}
    @Test void registroNoUsaInstitucionObsoletaTrasModificacionConfirmadaDelPerfil()throws Exception {
        preparar(true);String anterior="ceAntes"+sufijo,nueva="ceNueva"+sufijo,nick="ceAs"+sufijo,codigo="CE-COD-"+sufijo;
        for(String n:List.of(anterior,nueva))sistema.altaInstitucion(new DtInstitucion(n,"Institución",""),Actor.administradorLocal());
        var usuario=new DtAsistente(nick,"Anterior",nick+"@test.uy","Apellido",LocalDate.of(2000,1,1),anterior);
        usuario.setContrasenia("anterior");usuario.setConfirmacionContrasenia("anterior");sistema.altaAsistente(usuario);
        var actor=Actor.usuarioAutenticado(usuario);
        sistema.altaPatrocinio(new DtPatrocinio(LocalDate.now(),1000,1,codigo,NivelPatrocinio.ORO,anterior,edicion,"General"),"",Actor.administradorLocal());
        var pool=Executors.newFixedThreadPool(2);
        try(var em=BaseDeDatos.getEntityManager()){
            var tx=em.getTransaction();tx.begin();
            try{
                em.createNativeQuery("LOCK TABLE usuario IN SHARE MODE").executeUpdate();
                var perfil=pool.submit(()->{
                    var dto=new DtAsistente(nick,"Actualizado",nick+"@test.uy","Apellido",LocalDate.of(2000,1,1),nueva);
                    dto.setContrasenia("nueva");dto.setConfirmacionContrasenia("nueva");sistema.modificarAsistente(dto,null,actor);
                });
                esperar(em,"%update usuario%",perfil);
                var registro=pool.submit(()->{try{return sistema.registrarAutorizado(edicion,"General","codigo",codigo,actor);}catch(IllegalArgumentException e){return false;}});
                assertThrows(TimeoutException.class,()->registro.get(300,TimeUnit.MILLISECONDS));
                tx.rollback();perfil.get(15,TimeUnit.SECONDS);assertFalse(registro.get(15,TimeUnit.SECONDS),"El beneficio exige la institución actual, no la leída antes del bloqueo.");
            }finally{if(tx.isActive())tx.rollback();}
        }finally{pool.shutdownNow();}
        assertEquals(nueva,((DtAsistente)sistema.consultarUsuario(nick)).getNombreInstitucion());
        assertTrue(sistema.obtenerRegistrosAsistente(nick).isEmpty());assertNotNull(sistema.autenticar(nick,"nueva"));
        assertThrows(IllegalArgumentException.class,()->sistema.autenticar(nick,"anterior"));
    }
}
