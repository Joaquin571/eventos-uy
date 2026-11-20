package central.seguridad;

import datatypes.*;
import excepciones.AccesoDenegadoException;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.security.SecureRandom;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class SesionesMovilesTest {
    private final DtAsistente asistente=new DtAsistente("asis","Asistente","as@test.uy","Apellido",null,null);
    private final DtOrganizador organizador=new DtOrganizador("org","Organizador","org@test.uy","Descripcion","");
    private final Reloj reloj=new Reloj();
    private SesionesRemotas registro() {
        var sistema=(ISistema)Proxy.newProxyInstance(ISistema.class.getClassLoader(),new Class<?>[]{ISistema.class},(p,m,a)->{
            if(!"clave".equals(a[1]))throw new IllegalArgumentException("invalida");
            if("org".equals(a[0])) {
                if(m.getName().equals("autenticarMovil"))throw new AccesoDenegadoException("rol");
                return organizador;
            }
            return asistente;
        });
        return new SesionesRemotas(sistema,new SecureRandom(),reloj,Duration.ofMinutes(5));
    }
    @Test void movilReutilizaRevocacionYVencimientoP03() {
        var r=registro();var s=r.iniciarMovil("asis","clave");
        assertEquals("asis",r.requerirActor(s.credencial()).nickname());
        r.revocar(s.credencial());assertThrows(IllegalArgumentException.class,()->r.requerirActor(s.credencial()));
        var otra=r.iniciarMovil("asis","clave");reloj.ahora=reloj.ahora.plus(Duration.ofMinutes(5));
        assertThrows(IllegalArgumentException.class,()->r.requerirActor(otra.credencial()));
    }
    @Test void organizadorSigueIngresandoPorWebPeroNoPorMovil() {
        var r=registro();assertTrue(r.requerirActor(r.iniciar("org","clave").credencial()).esOrganizador());
        assertThrows(AccesoDenegadoException.class,()->r.iniciarMovil("org","clave"));
        assertThrows(IllegalArgumentException.class,()->r.iniciarMovil("asis","mala"));
    }
    private static class Reloj extends Clock {
        Instant ahora=Instant.parse("2026-01-01T00:00:00Z");
        public ZoneId getZone(){return ZoneOffset.UTC;}
        public Clock withZone(ZoneId zona){return this;}
        public Instant instant(){return ahora;}
    }
}
