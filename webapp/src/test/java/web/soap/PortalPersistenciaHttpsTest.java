package web.soap;

import central.seguridad.SesionesRemotas;
import central.soap.*;
import com.sun.net.httpserver.*;
import contratos.dto.*;
import implementacion.Fabrica;
import interfaces.ISistema;
import jakarta.xml.ws.Endpoint;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import persistencia.BaseDeDatos;
import web.remoto.*;
import javax.net.ssl.SSLSocketFactory;
import java.net.*;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Cliente wsimport -> HTTPS -> P03 -> Sistema -> PostgreSQL real de pruebas. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PortalPersistenciaHttpsTest {
    private HttpsServer servidor;
    private ExecutorService hilos;
    private final List<Endpoint> endpoints=new ArrayList<>();
    private SSLSocketFactory confianza;
    private URI portal,sesiones;
    private ISistema sistema;
    private ClienteCentral org,ajeno;
    private String evento,edicion,privada,institucion,categoria,orgNick;
    private final String sufijo=Long.toString(System.nanoTime());
    private static final String CLAVE="Clave-sintetica-42";
    private static final byte[] PNG=Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII=");

    @BeforeAll void iniciar(@TempDir Path temporal)throws Exception {
        verificarBase(); // Antes de inicializar JPA o construir la fachada.
        BaseDeDatos.inicializar();sistema=Fabrica.getInstance().getISistema();
        var registro=new SesionesRemotas(sistema,new java.security.SecureRandom(),Clock.systemUTC(),Duration.ofMinutes(5));
        Path almacen=temporal.resolve("portal-real.p12");char[] clave=UUID.randomUUID().toString().toCharArray();
        try{
            SoporteTls.generar(almacen,clave);confianza=SoporteTls.confianza(almacen,clave);
            servidor=HttpsServer.create(new InetSocketAddress("127.0.0.1",0),0);
            servidor.setHttpsConfigurator(new HttpsConfigurator(SoporteTls.contexto(almacen,clave)));
        }finally{Arrays.fill(clave,'\0');}
        hilos=Executors.newVirtualThreadPerTaskExecutor();servidor.setExecutor(hilos);
        publicar("/portal",new PortalPublicador(sistema,registro));publicar("/sesiones",new SesionesPublicador(registro));servidor.start();
        String base="https://127.0.0.1:"+servidor.getAddress().getPort();portal=URI.create(base+"/portal");sesiones=URI.create(base+"/sesiones");
        orgNick="tlsOrg"+sufijo;org=organizador(orgNick);ajeno=organizador("tlsAj"+sufijo);
        categoria="tlsCat"+sufijo;sistema.altaCategoria(categoria,null);
        evento="tlsEv"+sufijo;org.altaEvento(new DtEvento(evento,"EV","Descripción",null,List.of(categoria)),imagen());
        edicion="tlsEd"+sufijo;privada="tlsPr"+sufijo;
        for(String nombre:List.of(edicion,privada)){
            org.altaEdicion(new DtEdicion(nombre,"ED",LocalDate.now(),LocalDate.now(),null,"Ciudad","País",orgNick),evento,imagen());
            org.altaTipoRegistro(new DtTipoRegistro("General","Entrada",100,20),nombre);
        }
        sistema.aceptarRechazarEdicion(edicion,true);
        institucion="tlsInst"+sufijo;org.altaInstitucion(new DtInstitucion(institucion,"Institución","https://test.uy"),imagen());
    }
    static void verificarBase()throws Exception {
        if(!"eventosuy_test".equals(System.getenv("EVENTOS_DB_NAME")))throw new IllegalStateException("Definir EVENTOS_DB_NAME=eventosuy_test.");
        var p=BaseDeDatos.propiedadesJpa();
        if(!p.get("jakarta.persistence.jdbc.url").endsWith("/eventosuy_test"))throw new IllegalStateException("Destino JDBC no permitido.");
        try(var c=DriverManager.getConnection(p.get("jakarta.persistence.jdbc.url"),p.get("jakarta.persistence.jdbc.user"),p.get("jakarta.persistence.jdbc.password"))){
            c.setReadOnly(true);try(var q=c.createStatement()){q.setQueryTimeout(5);try(var r=q.executeQuery("SELECT current_database()")){
                if(!r.next()||!"eventosuy_test".equals(r.getString(1)))throw new IllegalStateException("Base JDBC incorrecta.");
            }}
        }
        System.out.println("[PRUEBAS HTTPS] Base efectiva confirmada por JDBC: eventosuy_test.");
    }
    private void publicar(String ruta,Object objeto){var e=Endpoint.create(objeto);endpoints.add(e);e.publish(servidor.createContext(ruta));}
    private ClienteCentral publico(){return new ClienteCentral(portal,null,confianza);}
    private ClienteCentral login(String nick,String clave){return new ClienteCentral(portal,new ClienteSesionesCentral(sesiones,confianza).iniciar(nick,clave).credencial(),confianza);}
    private EntradaUsuario entrada(DtUsuario usuario,String clave,DtImagen imagen){var e=new EntradaUsuario();e.setUsuario(usuario);e.setContrasenia(clave);e.setConfirmacion(clave);e.setImagen(imagen);return e;}
    private ClienteCentral organizador(String nick){publico().altaUsuario(entrada(new DtOrganizador(nick,"Organización",nick+"@test.uy","Descripción",""),CLAVE,imagen()));return login(nick,CLAVE);}
    private ClienteCentral asistente(String prefijo){String nick=prefijo+sufijo;publico().altaUsuario(entrada(new DtAsistente(nick,"Asistente",nick+"@test.uy","Apellido",LocalDate.of(2000,1,1),institucion),CLAVE,imagen()));return login(nick,CLAVE);}
    private DtImagen imagen(){return new DtImagen("image/png",PNG.clone());}

    @Test void consultasPublicasYDisponibilidadSeResuelvenEnCentral(){
        assertTrue(publico().listarEventosPorCategoria(categoria).stream().anyMatch(e->evento.equals(e.getNombre())));
        assertTrue(publico().listarEventosPorCategoria("no-existe"+sufijo).isEmpty());
        assertFalse(publico().disponibleNickname(orgNick));assertFalse(publico().disponibleCorreo((orgNick+"@test.uy").toUpperCase()));
        assertEquals(institucion,publico().consultarInstitucion(institucion).getNombre());
        assertArrayEquals(PNG,publico().obtenerImagen(TipoEntidadImagen.INSTITUCION,institucion).getContenido());
    }
    @Test void altaConImagenInvalidaODuplicadosNoDejaUsuarioParcial(){
        String nick="tlsFallido"+sufijo;var dt=new DtOrganizador(nick,"Org",nick+"@test.uy","Desc","");
        assertThrows(IllegalArgumentException.class,()->publico().altaUsuario(entrada(dt,CLAVE,new DtImagen("image/png",new byte[]{1}))));
        assertTrue(publico().disponibleNickname(nick));
        assertThrows(IllegalArgumentException.class,()->publico().altaUsuario(entrada(new DtOrganizador(orgNick,"Otro","otro"+sufijo+"@test.uy","Desc",""),CLAVE,imagen())));
        assertEquals("Organización",org.usuarioActual().getNombre());
    }
    @Test void perfilImagenYClaveSeModificanJuntosYClaveViejaNoAutentica(){
        var cliente=asistente("tlsPerfil");var actual=(DtAsistente)cliente.usuarioActual();
        actual.setNombre("Nombre actualizado");String nueva="Otra-clave-segura-43";
        cliente.modificarUsuario(entrada(actual,nueva,null));
        assertThrows(IllegalArgumentException.class,()->login(actual.getNickname(),CLAVE));
        var actualizado=login(actual.getNickname(),nueva);assertEquals("Nombre actualizado",actualizado.usuarioActual().getNombre());
        assertArrayEquals(PNG,actualizado.obtenerImagen(TipoEntidadImagen.USUARIO,actual.getNickname()).getContenido());
        actual.setNombre("No persistir");
        assertThrows(IllegalArgumentException.class,()->actualizado.modificarUsuario(entrada(actual,"otra",new DtImagen("image/png",new byte[]{1}))));
        assertEquals("Nombre actualizado",login(actual.getNickname(),nueva).usuarioActual().getNombre());
    }
    @Test void identidadDeSesionImpideModificarPerfilAjenoYEstadoPrivado(){
        var victima=asistente("tlsVictima").usuarioActual();victima.setNombre("Suplantado");
        assertEquals("ACCESO_DENEGADO",assertThrows(ErrorCentral.class,()->ajeno.modificarUsuario(entrada(victima,null,null))).getCodigo());
        assertNotEquals("Suplantado",publico().consultarPerfil(victima.getNickname()).getUsuario().getNombre());
        for(var c:List.of(publico(),ajeno)){
            assertEquals("NO_ENCONTRADO",assertThrows(ErrorCentral.class,()->c.consultarDetalleEdicion(privada)).getCodigo());
            assertEquals("NO_ENCONTRADO",assertThrows(ErrorCentral.class,()->c.obtenerImagen(TipoEntidadImagen.EDICION,privada)).getCodigo());
        }
        assertTrue(org.consultarDetalleEdicion(privada).isPropietario());
    }
    @Test void patrocinioOcultaCodigoYRegistroUsaIdentidadCostoYCupoCentrales(){
        String codigo="TLS-COD-"+sufijo;
        org.altaPatrocinio(new DtPatrocinio(LocalDate.now(),1000,1,codigo,NivelPatrocinio.ORO,institucion,edicion,"General"),evento);
        assertNull(publico().consultarPatrocinios(evento,edicion,institucion).getPatrocinio().getCodigoPatrocinio());
        assertEquals(codigo,org.consultarPatrocinios(evento,edicion,institucion).getPatrocinio().getCodigoPatrocinio());
        var asistente=asistente("tlsCodigo");assertTrue(asistente.registrar(edicion,"General","codigo",codigo));
        var r=asistente.consultarDetalleEdicion(edicion).getRegistroPropio();assertEquals(0,r.getCosto());assertTrue(r.isUsoCodigo());
        assertEquals(asistente.usuarioActual().getNickname(),r.getNicknameAsistente());
        assertEquals("ACCESO_DENEGADO",assertThrows(ErrorCentral.class,()->ajeno.consultarRegistros(edicion,"")).getCodigo());
        var segundo=asistente("tlsSinCupo");assertThrows(IllegalArgumentException.class,()->segundo.registrar(edicion,"General","codigo",codigo));
        assertTrue(segundo.consultarRegistros("","").getRegistros().isEmpty());
    }
    @Test void cierreP03RevocaTokenEnTodasLasOperacionesProtegidas(){
        var sesionesCliente=new ClienteSesionesCentral(sesiones,confianza);var inicio=sesionesCliente.iniciar(orgNick,CLAVE);
        var c=new ClienteCentral(portal,inicio.credencial(),confianza);assertEquals(orgNick,c.usuarioActual().getNickname());
        sesionesCliente.cerrar(inicio.credencial());assertEquals("SESION_INVALIDA",assertThrows(ErrorCentral.class,c::usuarioActual).getCodigo());
        assertEquals("SESION_INVALIDA",assertThrows(ErrorCentral.class,()->c.altaInstitucion(new DtInstitucion("no","desc",""),null)).getCodigo());
    }
    @AfterAll void cerrar(){try{endpoints.forEach(Endpoint::stop);}finally{if(servidor!=null)servidor.stop(0);if(hilos!=null)hilos.shutdownNow();BaseDeDatos.cerrar();}}
}
