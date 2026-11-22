package web.soap;

import org.junit.jupiter.api.Test;
import java.net.ServerSocket;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/** Fallo real de conexión: no detiene PostgreSQL ni modifica la configuración habitual. */
class CentralSinBaseIT {
    @Test void jarSinPostgresqlFallaAntesDePublicarSoapSinExponerClaves() throws Exception {
        Path jar=Path.of("../servidor-central/target/servidor.jar").toAbsolutePath();
        assertTrue(Files.isRegularFile(jar),"Construir servidor.jar antes de ejecutar esta IT.");
        Path base=Files.createTempDirectory(Path.of("target"),"central-sin-base-").toAbsolutePath();
        Path configuracion=Files.createDirectories(base.resolve(".eventosUy"));
        Path almacen=base.resolve("central.p12");
        char[] clave=UUID.randomUUID().toString().toCharArray();
        SoporteTls.generar(almacen,clave);
        String secretoDb="secreto-prueba-"+UUID.randomUUID();
        int puertoCerrado;
        try(var puerto=new ServerSocket(0)){puertoCerrado=puerto.getLocalPort();}
        Properties p=new Properties();
        p.setProperty("central.host","127.0.0.1");p.setProperty("central.escucha","127.0.0.1");p.setProperty("central.puerto","0");
        p.setProperty("central.ruta","/eventos");p.setProperty("central.ruta.sesiones","/sesiones");p.setProperty("central.ruta.portal","/portal");
        p.setProperty("sesiones.duracion.segundos","300");
        p.setProperty("tls.almacen",almacen.toString());p.setProperty("tls.almacen.password",new String(clave));p.setProperty("tls.clave.password",new String(clave));
        p.setProperty("db.url","jdbc:postgresql://127.0.0.1:"+puertoCerrado+"/eventosuy_test");
        p.setProperty("db.usuario","prueba_sin_base");p.setProperty("db.password",secretoDb);
        try(var w=Files.newBufferedWriter(configuracion.resolve("central.properties"))){p.store(w,"Configuración sintética de fallo");}
        String secretoTls=new String(clave);Arrays.fill(clave,'\0');
        Path log=base.resolve("fallo.log");
        var builder=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin/java.exe").toString(),
                "-Djava.awt.headless=true","-Dstdout.encoding=UTF-8","-Dstderr.encoding=UTF-8","-Duser.home="+base,"-jar",jar.toString());
        builder.environment().keySet().removeIf(k->k.startsWith("EVENTOS_DB_"));
        Process proceso=builder.directory(base.toFile()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try{
            assertTrue(proceso.waitFor(45,TimeUnit.SECONDS),"Central debe finalizar si PostgreSQL no está disponible.");
            assertNotEquals(0,proceso.exitValue());
            String salida=Files.readString(log);
            assertFalse(salida.contains("WSDL del portal:"),"No publicar un sistema sin persistencia disponible.");
            assertTrue(salida.contains("PSQLException"),"El fallo debe ser la conexión real con PostgreSQL.");
            assertFalse(salida.contains(secretoDb));assertFalse(salida.contains(secretoTls));
            System.out.println("[IT] PostgreSQL inaccesible: JAR falla antes de publicar SOAP; sin claves en el log.");
        }finally{
            if(proceso.isAlive()){proceso.destroy();if(!proceso.waitFor(10,TimeUnit.SECONDS))proceso.destroyForcibly();}
        }
    }
}
