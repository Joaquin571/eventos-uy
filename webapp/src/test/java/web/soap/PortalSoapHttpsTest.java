package web.soap;

import central.seguridad.SesionesRemotas;
import central.soap.PortalPublicador;
import central.soap.SesionesPublicador;
import com.sun.net.httpserver.*;
import contratos.dto.*;
import datatypes.Actor;
import interfaces.ISistema;
import jakarta.xml.ws.Endpoint;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import web.remoto.*;
import javax.net.ssl.SSLSocketFactory;
import java.lang.reflect.Proxy;
import java.net.*;
import java.nio.file.Path;
import java.time.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Transporte TLS real y cliente generado; sin PostgreSQL ni simulación del transporte. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PortalSoapHttpsTest {
    private HttpsServer servidor;private ExecutorService hilos;
    private final List<Endpoint> endpoints=new ArrayList<>();
    private SSLSocketFactory confianza;private URI portal,sesiones;
    private volatile Actor recibido;private volatile boolean fallo;
    private SesionesRemotas registro;
    @BeforeAll void publicar(@TempDir Path temporal) throws Exception {
        var sistema=(ISistema)Proxy.newProxyInstance(ISistema.class.getClassLoader(),new Class<?>[]{ISistema.class},(p,m,a)->{
            switch(m.getName()){
                case "autenticar":
                    if(!"clave-sintetica".equals(a[1]))throw new IllegalArgumentException("Credenciales inválidas");
                    return new datatypes.DtOrganizador("org","Organización ñ","org@test.uy","Desc","");
                case "listarUsuarios":
                    var u=new datatypes.DtOrganizador("org","Organización ñ","org@test.uy","Desc","");
                    u.setContrasenia("NO-DEBE-SALIR");return List.of(u);
                case "listarEventos", "listarEventosPublicos":return List.of(new datatypes.DtEvento("Encuentro ñ","EN","Descripción",LocalDate.of(2026,11,5),List.of("Arte")));
                case "altaTipoRegistro":
                    recibido=(Actor)a[2];
                    if(fallo)throw new IllegalStateException("SQL-secreto");
                    return true;
                case "registrarVisitaEvento":
                    recibido=(Actor)a[1];
                    if(fallo)throw new IllegalStateException("SQL-secreto");
                    return null;
                case "obtenerOpcionesAltaPatrocinio":
                    recibido=(Actor)a[2];return new datatypes.DtOpcionesAltaPatrocinio("Evento",List.of(),List.of(),List.of(),List.of());
                case "consultarPerfil":
                    recibido=(Actor)a[1];return new datatypes.DtPerfilUsuario(new datatypes.DtOrganizador("org","Org","org@test.uy","Desc",""),recibido!=null,List.of(),List.of());
                default:throw new AssertionError("Caso no esperado: "+m.getName());
            }
        });
        registro=new SesionesRemotas(sistema,new SecureRandom(),Clock.systemUTC(),Duration.ofMinutes(5));
        Path archivo=temporal.resolve("portal.p12");char[] clave=UUID.randomUUID().toString().toCharArray();
        try{
            EncabezadosSoapHttpsTest.generarCertificado(archivo,clave);
            confianza=EncabezadosSoapHttpsTest.confianzaLocal(archivo,clave);
            servidor=HttpsServer.create(new InetSocketAddress("127.0.0.1",0),0);
            servidor.setHttpsConfigurator(new HttpsConfigurator(PruebaHttpsServidor.contextoTls(archivo,clave)));
        }finally{Arrays.fill(clave,'\0');}
        hilos=Executors.newVirtualThreadPerTaskExecutor();servidor.setExecutor(hilos);
        publicar("/portal",new PortalPublicador(sistema,registro));publicar("/sesiones",new SesionesPublicador(registro));servidor.start();
        String base="https://127.0.0.1:"+servidor.getAddress().getPort();portal=URI.create(base+"/portal");sesiones=URI.create(base+"/sesiones");
    }
    private void publicar(String ruta,Object objeto){var e=Endpoint.create(objeto);endpoints.add(e);e.publish(servidor.createContext(ruta));}
    @AfterAll void cerrar(){try{endpoints.forEach(Endpoint::stop);}finally{if(servidor!=null)servidor.stop(0);if(hilos!=null)hilos.shutdownNow();}}
    private ClienteSesionesCentral login(){return new ClienteSesionesCentral(sesiones,confianza);}
    private ClienteCentral cliente(String credencial){return new ClienteCentral(portal,credencial,confianza);}
    @Test void consultaPublicaConvierteFechasColeccionesYUnicode(){var e=cliente(null).listarEventos().getFirst();assertEquals("Encuentro ñ",e.getNombre());assertEquals(LocalDate.of(2026,11,5),e.getFechaAlta());assertEquals(List.of("Arte"),new ArrayList<>(e.getCategorias()));}
    @Test void visitaPropagaActorOpcionalSinInventarUnAdministrador(){
        recibido=Actor.administradorLocal();cliente(null).registrarVisitaEvento("Evento");assertNull(recibido);
        var credencial=login().iniciar("org","clave-sintetica").credencial();
        cliente(credencial).registrarVisitaEvento("Evento");
        assertEquals("org",recibido.nickname());assertTrue(recibido.esOrganizador());assertFalse(recibido.esAdministradorLocal());
    }
    @Test void errorTecnicoDeVisitasSeSanitiza(){
        fallo=true;
        try{var error=assertThrows(ErrorCentral.class,()->cliente(null).registrarVisitaEvento("Evento"));
            assertEquals("SERVICIO_NO_DISPONIBLE",error.getCodigo());assertFalse(error.getMessage().contains("SQL-secreto"));
        }finally{fallo=false;}
    }
    @Test void salidaDeUsuarioNuncaIncluyeContraseniaDelDtoLogico(){var u=cliente(null).listarUsuarios().getFirst();assertInstanceOf(DtOrganizador.class,u);assertNull(u.getContrasenia());assertNull(u.getConfirmacionContrasenia());}
    @Test void entradaProtegidaSinSesionSeRechaza(){var error=assertThrows(ErrorCentral.class,()->cliente(null).altaTipoRegistro(new DtTipoRegistro("General","Desc",1,1),"Edición"));assertEquals("SESION_INVALIDA",error.getCodigo());}
    @Test void loginP03AutorizaYLogoutRevocaEnElMismoRegistro(){var inicio=login().iniciar("org","clave-sintetica");var c=cliente(inicio.credencial());assertTrue(c.altaTipoRegistro(new DtTipoRegistro("General","Desc",1,1),"Edición"));assertEquals("org",recibido.nickname());assertFalse(recibido.esAdministradorLocal());login().cerrar(inicio.credencial());assertEquals("SESION_INVALIDA",assertThrows(ErrorCentral.class,()->c.altaTipoRegistro(new DtTipoRegistro("General","Desc",1,1),"Edición")).getCodigo());}
    @Test void actorOpcionalDistingueVisitanteDePropietario(){assertFalse(cliente(null).consultarPerfil("org").isPropio());var inicio=login().iniciar("org","clave-sintetica");assertTrue(cliente(inicio.credencial()).consultarPerfil("org").isPropio());}
    @Test void faultTecnicoNoFiltraDetalles(){var inicio=login().iniciar("org","clave-sintetica");fallo=true;try{var error=assertThrows(ErrorCentral.class,()->cliente(inicio.credencial()).altaTipoRegistro(new DtTipoRegistro("General","Desc",1,1),"Edición"));assertEquals("SERVICIO_NO_DISPONIBLE",error.getCodigo());assertFalse(error.getMessage().contains("SQL"));assertFalse(error.getMessage().contains(inicio.credencial()));}finally{fallo=false;}}
    @Test void rechazaDestinosHttp(){assertThrows(IllegalArgumentException.class,()->new ClienteCentral(URI.create("http://localhost/portal"),null));assertThrows(IllegalArgumentException.class,()->new ClienteSesionesCentral(URI.create("http://localhost/sesiones")));}
    @Test void noAceptaCredencialEnParametrosDeNegocio(){for(var m:contratos.ServicioPortal.class.getMethods())for(var p:m.getParameters()){assertNotEquals(Actor.class,p.getType());assertFalse(p.getName().toLowerCase().contains("token"));}}
    @Test void coleccionesVaciasDelFormularioSeConservan(){var inicio=login().iniciar("org","clave-sintetica");var opciones=cliente(inicio.credencial()).obtenerOpcionesAltaPatrocinio("Evento","");assertEquals("Evento",opciones.getEvento());assertTrue(opciones.getTipos().isEmpty());assertTrue(opciones.getInstituciones().isEmpty());}
}
