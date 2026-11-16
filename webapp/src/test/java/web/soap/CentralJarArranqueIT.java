package web.soap;

import org.junit.jupiter.api.Test;
import persistencia.BaseDeDatos;
import web.remoto.ClienteCentral;
import java.net.URI;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/** Ejecuta el artefacto real fuera de Maven, sin fuentes y sin variables DB en el hijo. */
class CentralJarArranqueIT {
    @Test void jarPublicaHttpsConConfiguracionExternaYPostgresqlDePrueba()throws Exception {
        PortalPersistenciaHttpsTest.verificarBase();
        Path jar=Path.of("../servidor-central/target/servidor.jar").toAbsolutePath();assertTrue(Files.isRegularFile(jar),"Ejecutar package antes de esta IT.");
        Path base=Files.createTempDirectory(Path.of("target"),"central-jar-").toAbsolutePath();
        Path configuracion=base.resolve(".eventosUy");Files.createDirectories(configuracion);
        Path almacen=base.resolve("central.p12");char[] clave=UUID.randomUUID().toString().toCharArray();
        SoporteTls.generar(almacen,clave);var confianza=SoporteTls.confianza(almacen,clave);
        Properties p=new Properties();p.setProperty("central.host","127.0.0.1");p.setProperty("central.escucha","127.0.0.1");p.setProperty("central.puerto","0");
        p.setProperty("central.ruta","/eventos");p.setProperty("central.ruta.sesiones","/sesiones");p.setProperty("central.ruta.portal","/portal");p.setProperty("sesiones.duracion.segundos","300");
        p.setProperty("tls.almacen",almacen.toString());p.setProperty("tls.almacen.password",new String(clave));p.setProperty("tls.clave.password",new String(clave));Arrays.fill(clave,'\0');
        var db=BaseDeDatos.propiedadesJpa();p.setProperty("db.url",db.get("jakarta.persistence.jdbc.url"));p.setProperty("db.usuario",db.get("jakarta.persistence.jdbc.user"));p.setProperty("db.password",db.get("jakarta.persistence.jdbc.password"));
        try(var w=Files.newBufferedWriter(configuracion.resolve("central.properties"))){p.store(w,"Configuracion temporal exclusiva de pruebas");}
        Path log=base.resolve("central.log");
        var builder=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin/java.exe").toString(),"-Dstdout.encoding=UTF-8","-Dstderr.encoding=UTF-8","-Duser.home="+base,"-jar",jar.toString(),"--sin-swing");
        builder.environment().keySet().removeIf(k->k.startsWith("EVENTOS_DB_"));
        Process proceso=builder.directory(base.toFile()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try{
            URI portal=null;long fin=System.nanoTime()+Duration.ofSeconds(50).toNanos();
            while(System.nanoTime()<fin){
                assertTrue(proceso.isAlive(),"El JAR terminó antes de publicar. Ver evidencia temporal: "+log);
                if(Files.isRegularFile(log))for(String linea:Files.readAllLines(log))if(linea.startsWith("WSDL del portal: "))portal=URI.create(linea.substring("WSDL del portal: ".length()).replace("?wsdl",""));
                if(portal!=null)break;Thread.sleep(100);
            }
            assertNotNull(portal,"El JAR no publicó el Portal HTTPS.");assertEquals("https",portal.getScheme());
            assertNotNull(new ClienteCentral(portal,null,confianza).listarEventos());
            assertTrue(proceso.isAlive());System.out.println("[IT] servidor.jar: arranque HTTPS/PostgreSQL externo aprobado, sin fuentes ni variables DB en el hijo.");
        }finally{
            proceso.destroy();if(!proceso.waitFor(15,TimeUnit.SECONDS))proceso.destroyForcibly();
        }
    }
}
