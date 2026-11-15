package implementacion;

import clases.Imagen;
import clases.Institucion;
import datatypes.*;
import excepciones.AccesoDenegadoException;
import interfaces.ISistema;
import jakarta.persistence.PersistenceException;
import manejadores.ManejadorInstituciones;
import org.junit.jupiter.api.Test;
import persistencia.BaseDeDatos;
import soporte.ImagenesPrueba;
import soporte.PruebaConBaseSegura;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class InstitucionConImagenTest extends PruebaConBaseSegura {
    private final ISistema s=Fabrica.getInstance().getISistema();
    private DtInstitucion datos(){return new DtInstitucion("instImg"+UUID.randomUUID(),"Descripción","https://test.uy");}
    private DtImagen imagen(){return new DtImagen("image/png",ImagenesPrueba.imagen("png"));}
    private long imagenes(){try(var em=BaseDeDatos.getEntityManager()){return em.createQuery("SELECT COUNT(i) FROM Imagen i",Long.class).getSingleResult();}}
    @Test void organizadorYAdministradorCreanInstitucionEImagenJuntos(){
        Actor org=Actor.usuarioAutenticado(new DtOrganizador("org","Org","org@test.uy","Desc",""));
        for(Actor actor:new Actor[]{org,Actor.administradorLocal()}){
            var dt=datos();long antes=imagenes();assertTrue(s.altaInstitucion(dt,imagen(),actor));
            assertTrue(s.existeInstitucion(dt.getNombre()));assertEquals(antes+1,imagenes());
            assertArrayEquals(imagen().getContenido(),s.obtenerImagen(TipoEntidadImagen.INSTITUCION,dt.getNombre()).getContenido());
        }
    }
    @Test void imagenOpcionalYCompatibilidadConActorExplicito(){
        var dt=datos();long antes=imagenes();assertTrue(s.altaInstitucion(dt,Actor.administradorLocal()));
        assertNull(s.obtenerImagen(TipoEntidadImagen.INSTITUCION,dt.getNombre()));assertEquals(antes,imagenes());
    }
    @Test void ausenciaDeActorOAsistenteNoEscriben(){
        Actor asistente=Actor.usuarioAutenticado(new DtAsistente("as","As","as@test.uy","Apellido",LocalDate.of(2000,1,1),null));
        for(Actor actor:new Actor[]{null,asistente}){var dt=datos();long antes=imagenes();
            assertThrows(AccesoDenegadoException.class,()->s.altaInstitucion(dt,imagen(),actor));
            assertFalse(s.existeInstitucion(dt.getNombre()));assertEquals(antes,imagenes());}
    }
    @Test void imagenInvalidaNoCreaInstitucion(){var dt=datos();long antes=imagenes();
        assertThrows(IllegalArgumentException.class,()->s.altaInstitucion(dt,new DtImagen("image/png",new byte[]{1}),Actor.administradorLocal()));
        assertFalse(s.existeInstitucion(dt.getNombre()));assertEquals(antes,imagenes());}
    @Test void duplicadosNoDejanImagenHuerfana(){var dt=datos();assertTrue(s.altaInstitucion(dt,imagen(),Actor.administradorLocal()));
        long antes=imagenes();assertFalse(s.altaInstitucion(dt,imagen(),Actor.administradorLocal()));assertEquals(antes,imagenes());}
    @Test void falloSqlRevierteInstitucionEImagen(){var dt=datos();long antes=imagenes();var entidad=new Institucion(dt.getNombre(),dt.getDescripcion(),dt.getSitioWeb());
        entidad.setImagen(new Imagen("x".repeat(256),imagen().getContenido()));
        Throwable error=assertThrows(PersistenceException.class,()->ManejadorInstituciones.getInstance().addInstitucion(entidad));
        boolean sql=false;for(Throwable c=error;c!=null;c=c.getCause())if(c instanceof SQLException e && e.getSQLState().equals("22001"))sql=true;
        assertTrue(sql);assertFalse(s.existeInstitucion(dt.getNombre()));assertEquals(antes,imagenes());}
}
