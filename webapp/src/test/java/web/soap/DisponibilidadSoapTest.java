package web.soap;

import central.seguridad.SesionesRemotas;
import central.soap.PortalPublicador;
import com.sun.net.httpserver.*;
import implementacion.Sistema;
import jakarta.xml.ws.Endpoint;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import web.remoto.*;
import java.net.*;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import javax.net.ssl.SSLSocketFactory;
import static org.junit.jupiter.api.Assertions.*;

/** Tres escenarios de Emmanuel (2911ab1), adaptados a Portal, Sistema y HTTPS.
 * Las consultas de existencia son dobles; no se inicializa persistencia. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DisponibilidadSoapTest {
    private final List<String> llamadas=new ArrayList<>();
    private boolean falla;
    private HttpsServer servidor;
    private Endpoint endpoint;
    private ExecutorService hilos;
    private ClienteCentral cliente;
    @BeforeAll void publicar(@TempDir Path temporal)throws Exception {
        var sistema=new Sistema(){
            @Override public boolean existeUsuario(String valor){return existe("nickname",valor);}
            @Override public boolean existeCorreoElectronico(String valor){return existe("correo",valor);}
        };
        var archivo=temporal.resolve("disponibilidad.p12");char[] clave=UUID.randomUUID().toString().toCharArray();
        SSLSocketFactory confianza;
        try{
            SoporteTls.generar(archivo,clave);confianza=SoporteTls.confianza(archivo,clave);
            servidor=HttpsServer.create(new InetSocketAddress("127.0.0.1",0),0);
            servidor.setHttpsConfigurator(new HttpsConfigurator(SoporteTls.contexto(archivo,clave)));
        }finally{Arrays.fill(clave,'\0');}
        hilos=Executors.newVirtualThreadPerTaskExecutor();servidor.setExecutor(hilos);
        endpoint=Endpoint.create(new PortalPublicador(sistema,new SesionesRemotas(sistema,new java.security.SecureRandom(),Clock.systemUTC(),Duration.ofMinutes(5))));
        endpoint.publish(servidor.createContext("/portal"));servidor.start();
        cliente=new ClienteCentral(URI.create("https://127.0.0.1:"+servidor.getAddress().getPort()+"/portal"),null,confianza);
    }
    private boolean existe(String campo,String valor){
        if(falla)throw new IllegalStateException("Detalle interno de base de datos");
        llamadas.add(campo+":"+valor);return valor.equals("ocupado")||valor.equals("usado@ejemplo.com");
    }
    @BeforeEach void limpiar(){llamadas.clear();falla=false;}
    @Test void consultaLogicaConIdentificadoresNormalizados(){
        assertFalse(cliente.disponibleNickname(" ocupado "));assertTrue(cliente.disponibleNickname(" nuevo "));
        assertFalse(cliente.disponibleCorreo(" usado@ejemplo.com "));assertTrue(cliente.disponibleCorreo(" nuevo@ejemplo.com "));
        assertEquals(List.of("nickname:ocupado","nickname:nuevo","correo:usado@ejemplo.com","correo:nuevo@ejemplo.com"),llamadas);
    }
    @Test void entradasVaciasOInvalidasNoConsultanPersistencia(){
        assertFalse(cliente.disponibleNickname("  "));assertFalse(cliente.disponibleNickname(null));
        assertFalse(cliente.disponibleCorreo(""));assertFalse(cliente.disponibleCorreo(null));
        assertFalse(cliente.disponibleCorreo("invalido"));assertFalse(cliente.disponibleNickname("x".repeat(256)));
        assertTrue(llamadas.isEmpty());
    }
    @Test void erroresInternosSeConviertenEnFaultSinFiltrarDetalles(){
        falla=true;
        for(Runnable llamada:List.<Runnable>of(()->cliente.disponibleNickname("alguien"),()->cliente.disponibleCorreo("a@b.com"))){
            var error=assertThrows(ErrorCentral.class,llamada::run);
            assertEquals("SERVICIO_NO_DISPONIBLE",error.getCodigo());assertFalse(error.getMessage().contains("base de datos"));
        }
    }
    @AfterAll void cerrar(){try{if(endpoint!=null)endpoint.stop();}finally{if(servidor!=null)servidor.stop(0);if(hilos!=null)hilos.shutdownNow();}}
}
