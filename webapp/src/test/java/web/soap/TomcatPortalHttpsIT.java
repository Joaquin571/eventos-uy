package web.soap;

import central.*;
import datatypes.*;
import implementacion.Fabrica;
import interfaces.ISistema;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import persistencia.BaseDeDatos;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;
import static org.junit.jupiter.api.Assertions.*;

/** Ejecutar explícitamente después de package, con TOMCAT_HOME y eventosuy_test.
 * Inicia un CATALINA_BASE temporal: nunca despliega sobre la instalación del usuario. */
@EnabledIfEnvironmentVariable(named="TOMCAT_HOME",matches=".+")
class TomcatPortalHttpsIT {
    private static final String CLAVE="Clave-sintetica-42";
    private static final byte[] PNG=Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII=");
    private ServidorHttpsCentral central;
    private Process tomcat;
    private URI web;
    private String sufijo,org,evento,edicion,institucion,categoria;
    private ISistema sistema;
    private int solicitudes;

    @Test void navegadorTomcatSoapHttpsCentralYPersistenciaSinJpaEnWar()throws Exception {
        Path temporal=Files.createTempDirectory(Path.of("target"),"tomcat-portal-").toAbsolutePath();
        System.out.println("[IT] Evidencia Tomcat: "+temporal);
        PortalPersistenciaHttpsTest.verificarBase();
        Path war=Path.of(System.getProperty("eventos.prueba.war","target/web.war")).toAbsolutePath();assertTrue(Files.isRegularFile(war),"Ejecutar package antes de esta IT.");
        try(var zip=new ZipFile(war.toFile())){
            var prohibidos=zip.stream().map(e->e.getName().toLowerCase(Locale.ROOT)).filter(n->n.startsWith("web-inf/lib/")
                    && List.of("logica-","hibernate","postgresql","jakarta.persistence","servidor-central","estacion-trabajo").stream().anyMatch(n::contains)).toList();
            assertTrue(prohibidos.isEmpty(),"El WAR contiene persistencia: "+prohibidos);
        }
        BaseDeDatos.inicializar();sistema=Fabrica.getInstance().getISistema();prepararDatos();
        Path almacen=temporal.resolve("central.p12");char[] clave=UUID.randomUUID().toString().toCharArray();
        SoporteTls.generar(almacen,clave);
        Properties p=new Properties();p.setProperty("central.host","127.0.0.1");p.setProperty("central.escucha","127.0.0.1");p.setProperty("central.puerto","0");
        p.setProperty("central.ruta","/eventos");p.setProperty("central.ruta.sesiones","/sesiones");p.setProperty("central.ruta.portal","/portal");
        p.setProperty("sesiones.duracion.segundos","300");p.setProperty("tls.almacen",almacen.toString());
        p.setProperty("tls.almacen.password",new String(clave));p.setProperty("tls.clave.password",new String(clave));
        Path configuracion=temporal.resolve("central.properties");try(var w=Files.newBufferedWriter(configuracion)){p.store(w,"Solo prueba TLS");}
        central=new ServidorHttpsCentral(new ConfiguracionCentral(configuracion));central.iniciar(sistema);
        iniciarTomcat(temporal,almacen,new String(clave),war);Arrays.fill(clave,'\0');
        var navegador=browser();esperar(navegador);
        assertEquals(200,get(navegador,"/").statusCode());
        assertEquals(404,get(navegador,"/evento").statusCode());
        assertTrue(get(navegador,"/usuarios").body().contains(org));
        assertTrue(get(navegador,"/disponibilidad-usuario?campo=nickname&valor=libre"+sufijo).body().contains("true"));
        assertEquals(404,get(navegador,"/edicion?nombre=privada"+sufijo).statusCode());
        assertEquals(404,get(navegador,"/imagen?tipo=edicion&id=privada"+sufijo).statusCode());

        login(navegador,org,CLAVE);
        var perfil=get(navegador,"/modificar-usuario");assertEquals(200,perfil.statusCode());
        var cambio=multipart(navegador,"/modificar-usuario",Map.of("_csrf",csrf(perfil),"nickname","ajeno","rol","administrador",
                "nombre","Organización actualizada","descripcion","Descripción","sitioWeb","","contrasenia","Otra-clave-43","confirmacion","Otra-clave-43"),false);
        assertEquals(302,cambio.statusCode());assertTrue(get(navegador,"/usuario?nick="+org).body().contains("Organización actualizada"));
        assertEquals(403,post(navegador,"/alta-tipo-registro",Map.of("nombre","Sin CSRF")).statusCode());
        var inst=get(navegador,"/alta-institucion");String nuevaInstitucion="httpInst"+sufijo;
        assertEquals(302,multipart(navegador,"/alta-institucion",Map.of("_csrf",csrf(inst),"nombre",nuevaInstitucion,"descripcion","Institución HTTP","sitioWeb","https://test.uy"),true).statusCode());
        assertTrue(get(navegador,"/consulta-institucion?nombre="+nuevaInstitucion).body().contains(nuevaInstitucion));
        assertEquals(200,get(navegador,"/imagen?tipo=institucion&id="+nuevaInstitucion).statusCode());
        var ev=get(navegador,"/alta-evento");String nuevoEvento="httpEvento"+sufijo;
        assertEquals(302,multipart(navegador,"/alta-evento",Map.of("_csrf",csrf(ev),"nombre",nuevoEvento,"sigla","EV","descripcion","Evento HTTP","categorias",categoria),true).statusCode());
        var ed=get(navegador,"/alta-edicion");String nuevaEdicion="httpEdicion"+sufijo;
        assertEquals(302,multipart(navegador,"/alta-edicion",Map.of("_csrf",csrf(ed),"evento",nuevoEvento,"nombre",nuevaEdicion,"sigla","ED","ciudad","Ciudad","pais","País","fechaInicio",LocalDate.now().toString(),"fechaFin",LocalDate.now().toString()),true).statusCode());
        var tipo=get(navegador,"/alta-tipo-registro");
        assertEquals(302,post(navegador,"/alta-tipo-registro",Map.of("_csrf",csrf(tipo),"edicion",nuevaEdicion,"nombre","General","descripcion","Entrada","costo","100","cupo","10")).statusCode());
        assertEquals(1,sistema.obtenerTiposRegistroEdicion(nuevaEdicion).size());
        var pat=get(navegador,"/alta-patrocinio?edicion="+edicion);String codigo="HTTP-COD-"+sufijo;
        assertEquals(302,post(navegador,"/alta-patrocinio",Map.of("_csrf",csrf(pat),"evento",evento,"edicion",edicion,"institucion",institucion,"nivel","ORO","tipoRegistro","General","aporte","1000","registrograt","1","codigo",codigo)).statusCode());
        assertTrue(get(navegador,"/consulta-patrocinio?edicion="+edicion+"&institucion="+institucion).body().contains(codigo));
        assertFalse(get(browser(),"/consulta-patrocinio?edicion="+edicion+"&institucion="+institucion).body().contains(codigo));

        var asistente=browser();var alta=get(asistente,"/alta-usuario");String nick="httpAs"+sufijo;
        var campos=new HashMap<String,String>(Map.of("_csrf",csrf(alta),"tipo","asistente","nickname",nick,"nombre","Ana","correo",nick+"@test.uy","apellido","Prueba","fechaNac","2000-01-01","institucion",institucion,"contrasenia",CLAVE,"confirmacion",CLAVE));
        assertEquals(302,multipart(asistente,"/alta-usuario",campos,true).statusCode());login(asistente,nick,CLAVE);
        assertEquals(403,get(asistente,"/alta-institucion").statusCode());
        var registro=get(asistente,"/registro-edicion?edicion="+edicion+"&tipo=General");assertEquals(200,registro.statusCode());
        assertEquals(302,post(asistente,"/registro-edicion",Map.of("_csrf",csrf(registro),"evento",evento,"edicion",edicion,"tipo","General","modalidad","codigo","codigo",codigo,"nickname",org)).statusCode());
        assertTrue(get(asistente,"/consulta-registro?edicion="+edicion).body().contains(edicion));
        assertEquals(0,sistema.obtenerRegistrosAsistente(nick).iterator().next().getCosto());
        assertTrue(sistema.obtenerRegistrosAsistente(org).isEmpty());
        assertEquals(405,get(asistente,"/logout").statusCode());
        var pagina=get(asistente,"/usuario?nick="+nick);
        assertEquals(302,post(asistente,"/logout",Map.of("_csrf",csrf(pagina))).statusCode());
        assertEquals(302,get(asistente,"/modificar-usuario").statusCode());
        var cierre=get(navegador,"/usuario?nick="+org);post(navegador,"/logout",Map.of("_csrf",csrf(cierre)));
        var login=get(navegador,"/login");assertEquals(200,post(navegador,"/login",Map.of("_csrf",csrf(login),"identificador",org,"contrasenia",CLAVE)).statusCode());
        login(navegador,org,"Otra-clave-43");
        central.close();central=null;
        assertEquals(503,get(browser(),"/").statusCode(),"Sin Central no puede haber respaldo local en Tomcat.");
        System.out.println("[IT] Solicitudes HTTP verificadas: "+solicitudes+"; WAR sin lógica/JPA/JDBC; Central HTTPS real.");
    }
    private void prepararDatos()throws Exception {
        sufijo=Long.toString(System.nanoTime());org="httpOrg"+sufijo;evento="httpEv"+sufijo;edicion="httpEd"+sufijo;institucion="httpIn"+sufijo;categoria="httpCat"+sufijo;
        var usuario=new DtOrganizador(org,"Organización",org+"@test.uy","Descripción","");usuario.setContrasenia(CLAVE);usuario.setConfirmacionContrasenia(CLAVE);sistema.altaOrganizador(usuario);
        var actor=Actor.usuarioAutenticado(sistema.autenticar(org,CLAVE));sistema.altaCategoria(categoria,null, datatypes.Actor.administradorLocal());
        sistema.altaEvento(new DtEvento(evento,"EV","Evento",null,List.of(categoria)),null,actor);
        for(String nombre:List.of(edicion,"privada"+sufijo)){
            sistema.altaEdicion(new DtEdicion(nombre,"ED",LocalDate.now(),LocalDate.now(),null,"Ciudad","País",org),evento,null,actor);
            sistema.altaTipoRegistro(new DtTipoRegistro("General","Entrada",100,10),nombre,actor);
        }
        sistema.aceptarRechazarEdicion(edicion,true, datatypes.Actor.administradorLocal());sistema.altaInstitucion(new DtInstitucion(institucion,"Institución","https://test.uy"),actor);
    }
    private void iniciarTomcat(Path base,Path almacen,String clave,Path war)throws Exception {
        Path home=Path.of(System.getenv("TOMCAT_HOME")).toAbsolutePath();assertTrue(Files.isRegularFile(home.resolve("bin/bootstrap.jar")));
        for(String n:List.of("conf","logs","temp","work","webapps"))Files.createDirectories(base.resolve(n));
        for(String n:List.of("web.xml","catalina.properties","logging.properties"))Files.copy(home.resolve("conf/"+n),base.resolve("conf/"+n));
        int puerto;try(var socket=new java.net.ServerSocket(0)){puerto=socket.getLocalPort();}
        String xml="<Server port='-1'><Service name='Catalina'><Connector port='"+puerto+"' address='127.0.0.1' protocol='HTTP/1.1'/><Engine name='Catalina' defaultHost='localhost'><Host name='localhost' appBase='webapps' unpackWARs='true' autoDeploy='false'/></Engine></Service></Server>";
        Files.writeString(base.resolve("conf/server.xml"),xml);Files.copy(war,base.resolve("webapps/web.war"));
        URI url=URI.create(central.urlPortal());Path config=base.resolve("web.properties");Files.writeString(config,"central.host=127.0.0.1\ncentral.puerto="+url.getPort()+"\ncentral.ruta.portal=/portal\ncentral.ruta.sesiones=/sesiones\n");
        var builder=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin/java.exe").toString(),
                "-Dcatalina.home="+home,"-Dcatalina.base="+base,"-Deventos.web.config="+config,
                "-Djavax.net.ssl.trustStore="+almacen,"-Djavax.net.ssl.trustStorePassword="+clave,"-Djavax.net.ssl.trustStoreType=PKCS12",
                "-Djava.util.logging.config.file="+base.resolve("conf/logging.properties"),"-cp",home.resolve("bin/bootstrap.jar")+File.pathSeparator+home.resolve("bin/tomcat-juli.jar"),"org.apache.catalina.startup.Bootstrap","start");
        builder.command().add(1,"-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager");
        builder.environment().keySet().removeIf(k->k.startsWith("EVENTOS_DB_"));
        tomcat=builder.redirectErrorStream(true).redirectOutput(base.resolve("tomcat.log").toFile()).start();
        web=URI.create("http://127.0.0.1:"+puerto+"/web");
    }
    private HttpClient browser(){return HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).connectTimeout(Duration.ofSeconds(3)).build();}
    private HttpResponse<String> get(HttpClient c,String ruta)throws Exception {solicitudes++;return c.send(HttpRequest.newBuilder(URI.create(web+ruta)).timeout(Duration.ofSeconds(25)).GET().build(),HttpResponse.BodyHandlers.ofString());}
    private HttpResponse<String> post(HttpClient c,String ruta,Map<String,String> datos)throws Exception {
        solicitudes++;
        String cuerpo=datos.entrySet().stream().map(e->URLEncoder.encode(e.getKey(),StandardCharsets.UTF_8)+"="+URLEncoder.encode(e.getValue(),StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));
        return c.send(HttpRequest.newBuilder(URI.create(web+ruta)).timeout(Duration.ofSeconds(25)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(cuerpo)).build(),HttpResponse.BodyHandlers.ofString());
    }
    private HttpResponse<String> multipart(HttpClient c,String ruta,Map<String,String> datos,boolean imagen)throws Exception {
        solicitudes++;
        String limite="prueba"+UUID.randomUUID();var bytes=new ByteArrayOutputStream();
        for(var e:datos.entrySet())bytes.write(("--"+limite+"\r\nContent-Disposition: form-data; name=\""+e.getKey()+"\"\r\n\r\n"+e.getValue()+"\r\n").getBytes(StandardCharsets.UTF_8));
        if(imagen){bytes.write(("--"+limite+"\r\nContent-Disposition: form-data; name=\"imagen\"; filename=\"prueba.png\"\r\nContent-Type: image/png\r\n\r\n").getBytes(StandardCharsets.UTF_8));bytes.write(PNG);bytes.write("\r\n".getBytes(StandardCharsets.UTF_8));}
        bytes.write(("--"+limite+"--\r\n").getBytes(StandardCharsets.UTF_8));
        return c.send(HttpRequest.newBuilder(URI.create(web+ruta)).timeout(Duration.ofSeconds(25)).header("Content-Type","multipart/form-data; boundary="+limite).POST(HttpRequest.BodyPublishers.ofByteArray(bytes.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());
    }
    private String csrf(HttpResponse<String> r){var m=Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"").matcher(r.body());assertTrue(m.find(),"Formulario sin CSRF; HTTP "+r.statusCode());return m.group(1);}
    private void login(HttpClient c,String nick,String clave)throws Exception {
        var formulario=get(c,"/login");assertEquals(200,formulario.statusCode());
        CookieManager cookies=(CookieManager)c.cookieHandler().orElseThrow();
        String anterior=cookies.getCookieStore().getCookies().stream().filter(k->k.getName().equals("JSESSIONID")).findFirst().orElseThrow().getValue();
        var respuesta=post(c,"/login",Map.of("_csrf",csrf(formulario),"identificador",nick,"contrasenia",clave));assertEquals(302,respuesta.statusCode());
        String nueva=cookies.getCookieStore().getCookies().stream().filter(k->k.getName().equals("JSESSIONID")).findFirst().orElseThrow().getValue();
        assertNotEquals(anterior,nueva,"Renovar sesión para impedir fijación.");
        assertTrue(respuesta.headers().allValues("Set-Cookie").stream().anyMatch(v->v.toLowerCase(Locale.ROOT).contains("httponly")));
        assertTrue(respuesta.headers().firstValue("X-EventosUy-Session").isEmpty(),"La credencial P03 no puede llegar al navegador.");
    }
    private void esperar(HttpClient c)throws Exception {
        long fin=System.nanoTime()+Duration.ofSeconds(50).toNanos();
        while(System.nanoTime()<fin){assertTrue(tomcat.isAlive(),"Tomcat terminó antes de publicar el WAR.");try{if(get(c,"/").statusCode()==200)return;}catch(IOException e){/* Arranque en curso. */}Thread.sleep(250);}
        fail("Tomcat no publicó el WAR dentro del plazo.");
    }
    @AfterEach void cerrar()throws Exception {
        if(tomcat!=null){tomcat.destroy();if(!tomcat.waitFor(15,java.util.concurrent.TimeUnit.SECONDS))tomcat.destroyForcibly();}
        if(central!=null)central.close();BaseDeDatos.cerrar();
    }
}
