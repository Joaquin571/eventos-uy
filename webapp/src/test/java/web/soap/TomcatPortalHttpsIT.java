package web.soap;

import central.*;
import datatypes.*;
import implementacion.Fabrica;
import interfaces.ISistema;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
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
        var camposEdicion=new HashMap<String,String>(Map.of("_csrf",csrf(ed),"evento",nuevoEvento,"nombre",nuevaEdicion,"sigla","ED","ciudad","Ciudad","pais","País","fechaInicio",LocalDate.now().toString(),"fechaFin",LocalDate.now().toString(),"videoYoutube","https://www.youtube.com/watch?v=dQw4w9WgXcQ","descripcion","Descripción HTTP buscable "+sufijo));
        assertEquals(302,multipart(navegador,"/alta-edicion",camposEdicion,true).statusCode());
        assertEquals("https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ",sistema.obtenerEdicionesEvento(nuevoEvento).iterator().next().getVideoYoutube());
        camposEdicion.put("nombre","videoInvalido"+sufijo);camposEdicion.put("videoYoutube","https://example.org/video");
        assertEquals(200,multipart(navegador,"/alta-edicion",camposEdicion,false).statusCode());
        assertTrue(sistema.obtenerEdicionesEvento(nuevoEvento).stream().noneMatch(e->e.getIdNombre().equals("videoInvalido"+sufijo)));
        var tipo=get(navegador,"/alta-tipo-registro");
        assertEquals(302,post(navegador,"/alta-tipo-registro",Map.of("_csrf",csrf(tipo),"edicion",nuevaEdicion,"nombre","General","descripcion","Entrada","costo","100","cupo","10")).statusCode());
        assertEquals(1,sistema.obtenerTiposRegistroEdicion(nuevaEdicion).size());
        var pat=get(navegador,"/alta-patrocinio?edicion="+edicion);String codigo="HTTP-COD-"+sufijo;
        assertEquals(302,post(navegador,"/alta-patrocinio",Map.of("_csrf",csrf(pat),"evento",evento,"edicion",edicion,"institucion",institucion,"nivel","ORO","tipoRegistro","General","aporte","1000","registrograt","1","codigo",codigo)).statusCode());
        assertTrue(get(navegador,"/consulta-patrocinio?edicion="+edicion+"&institucion="+institucion).body().contains(codigo));
        assertFalse(get(browser(),"/consulta-patrocinio?edicion="+edicion+"&institucion="+institucion).body().contains(codigo));

        var asistente=browser();var alta=get(asistente,"/alta-usuario");String nick="httpAs"+sufijo;
        var campos=new HashMap<String,String>(Map.of("_csrf",csrf(alta),"tipo","asistente","nickname",nick,"nombre","Łucía","correo",nick+"@test.uy","apellido","Pérez","fechaNac","2000-01-01","institucion",institucion,"contrasenia",CLAVE,"confirmacion",CLAVE));
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
        verificarMovilYPdf(nick);
        verificarSeguimientoBusquedaArchivoYFin(navegador,nick);
        central.close();central=null;
        assertEquals(503,get(browser(),"/").statusCode(),"Sin Central no puede haber respaldo local en Tomcat.");
        System.out.println("[IT] Solicitudes HTTP verificadas: "+solicitudes+"; WAR sin lógica/JPA/JDBC; Central HTTPS real.");
    }
    private void prepararDatos()throws Exception {
        // El selector legado recorre eventos ordenados; esta IT de transporte no mide su escalabilidad.
        // Evitar que miles de fixtures históricos agoten el timeout SOAP antes de probar el flujo.
        sufijo=Long.toString(System.nanoTime());org="httpOrg"+sufijo;evento="000_httpEv"+sufijo;edicion="httpEd"+sufijo;institucion="httpIn"+sufijo;categoria="httpCat"+sufijo;
        var usuario=new DtOrganizador(org,"Organización",org+"@test.uy","Descripción","");usuario.setContrasenia(CLAVE);usuario.setConfirmacionContrasenia(CLAVE);sistema.altaOrganizador(usuario);
        var actor=Actor.usuarioAutenticado(sistema.autenticar(org,CLAVE));sistema.altaCategoria(categoria,null, datatypes.Actor.administradorLocal());
        sistema.altaEvento(new DtEvento(evento,"EV","Evento",null,List.of(categoria)),null,actor);
        for(String nombre:List.of(edicion,"privada"+sufijo)){
            var datos=new DtEdicion(nombre,"ED",LocalDate.now(),LocalDate.now(),null,"Ciudad","País",org);
            if(nombre.equals(edicion))datos.setVideoYoutube("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
            sistema.altaEdicion(datos,evento,null,actor);
            sistema.altaTipoRegistro(new DtTipoRegistro("General","Entrada",100,10),nombre,actor);
        }
        sistema.aceptarRechazarEdicion(edicion,true, datatypes.Actor.administradorLocal());sistema.altaInstitucion(new DtInstitucion(institucion,"Institución","https://test.uy"),actor);
    }
    private void verificarMovilYPdf(String nick)throws Exception {
        var movil=browser();
        var formulario=get(movil,"/movil/login");assertEquals(200,formulario.statusCode());
        assertEquals(200,post(movil,"/movil/login",Map.of("_csrf",csrf(formulario),"identificador",nick,"contrasenia","incorrecta")).statusCode());
        formulario=get(movil,"/movil/login");
        assertEquals(302,post(movil,"/movil/login",Map.of("_csrf",csrf(formulario),"identificador",nick+"@test.uy","contrasenia",CLAVE)).statusCode());
        var eventos=get(movil,"/movil/eventos");assertEquals(200,eventos.statusCode());
        assertTrue(eventos.body().contains("action=\"/web/buscar\""));
        var detalle=get(movil,"/movil/edicion?nombre="+edicion);assertEquals(200,detalle.statusCode());
        assertTrue(detalle.body().contains("https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ"));
        var registro=get(movil,"/movil/registros?edicion="+edicion);assertEquals(200,registro.statusCode());
        assertTrue(registro.body().contains("action=\"/web/movil/asistencia\""));
        assertEquals(400,get(movil,"/constancia-asistencia?edicion="+edicion).statusCode(),"Sin asistencia no se emite PDF.");
        assertEquals(302,post(movil,"/movil/asistencia",Map.of("_csrf",csrf(registro),"edicion",edicion)).statusCode());
        registro=get(movil,"/movil/registros?edicion="+edicion);assertEquals(200,registro.statusCode());
        assertTrue(registro.body().contains("Asistió"));
        assertFalse(registro.body().contains("action=\"/web/movil/asistencia\""));
        solicitudes++;
        var pdf=movil.send(HttpRequest.newBuilder(URI.create(web+"/constancia-asistencia?edicion="+edicion))
                .timeout(Duration.ofSeconds(60)).GET().build(),HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200,pdf.statusCode());
        assertEquals("application/pdf",pdf.headers().firstValue("Content-Type").orElseThrow());
        try(var documento=Loader.loadPDF(pdf.body())){
            String texto=new PDFTextStripper().getText(documento);
            assertTrue(texto.contains("Łucía Pérez"));assertTrue(texto.contains(evento));assertTrue(texto.contains(edicion));
        }
        assertEquals(403,get(movil,"/constancia-asistencia?edicion=privada"+sufijo).statusCode());
        assertEquals(302,post(movil,"/movil/asistencia",Map.of("_csrf",csrf(registro),"edicion",edicion)).statusCode());
        assertEquals(302,post(movil,"/movil/logout",Map.of("_csrf",csrf(registro))).statusCode());
        assertEquals(302,get(movil,"/movil/registros").statusCode());
        var organizadorMovil=browser();var login=get(organizadorMovil,"/movil/login");
        var rechazo=post(organizadorMovil,"/movil/login",Map.of("_csrf",csrf(login),"identificador",org,"contrasenia","Otra-clave-43"));
        assertEquals(200,rechazo.statusCode());assertTrue(rechazo.body().contains("solo pueden iniciar sesión los asistentes"));
    }
    private void verificarSeguimientoBusquedaArchivoYFin(HttpClient organizador,String nick)throws Exception {
        var asistente=browser();login(asistente,nick,CLAVE);
        var perfil=get(asistente,"/usuario?nick="+org);
        assertEquals(302,post(asistente,"/usuario",Map.of("_csrf",csrf(perfil),"nick",org,"accion","seguir")).statusCode());
        assertTrue(sistema.consultarRelaciones(org,Actor.administradorLocal()).seguidores().contains(nick));
        assertEquals(302,post(asistente,"/usuario",Map.of("_csrf",csrf(perfil),"nick",org,"accion","seguir")).statusCode());
        assertEquals(1,sistema.consultarRelaciones(org,Actor.administradorLocal()).seguidores().stream().filter(nick::equals).count(),"Repetir la solicitud no duplica seguidores.");
        assertEquals(302,post(asistente,"/usuario",Map.of("_csrf",csrf(perfil),"nick",org,"accion","dejar-de-seguir")).statusCode());
        assertFalse(sistema.consultarRelaciones(org,Actor.administradorLocal()).seguidores().contains(nick));
        assertEquals(400,post(asistente,"/usuario",Map.of("_csrf",csrf(perfil),"nick",nick,"accion","seguir")).statusCode());
        var otroPerfil=get(organizador,"/usuario?nick="+nick);
        assertEquals(302,post(organizador,"/usuario",Map.of("_csrf",csrf(otroPerfil),"nick",nick,"accion","seguir")).statusCode());
        assertTrue(sistema.consultarRelaciones(nick,Actor.administradorLocal()).seguidores().contains(org));

        for(String orden:List.of("FECHA_DESC","NOMBRE_ASC","NOMBRE_DESC")){
            var busqueda=get(browser(),"/buscar?texto="+sufijo+"&orden="+orden);assertEquals(200,busqueda.statusCode());
            assertTrue(busqueda.body().contains("name=\"viewport\""));
            assertTrue(busqueda.body().contains("/vendor/bootstrap/css/bootstrap.min.css"));
            assertTrue(busqueda.body().contains("/vendor/bootstrap/js/bootstrap.bundle.min.js"));
            assertTrue(busqueda.body().contains(edicion));assertFalse(busqueda.body().contains("privada"+sufijo));
            assertTrue(busqueda.body().contains("/web/edicion?"));
            var nombres=new ArrayList<String>();
            var enlaces=Pattern.compile("<li class=\"list-group-item text-break\">\\s*<a[^>]*>([^<]+)</a>",Pattern.DOTALL).matcher(busqueda.body());
            while(enlaces.find())nombres.add(enlaces.group(1));
            assertTrue(nombres.size()>=3,"Los resultados deben conservar sus enlaces.");
            if(!orden.equals("FECHA_DESC")){
                var esperados=new ArrayList<>(nombres);esperados.sort(orden.equals("NOMBRE_ASC")?String.CASE_INSENSITIVE_ORDER:String.CASE_INSENSITIVE_ORDER.reversed());
                assertEquals(esperados,nombres,"Orden alfabético de los resultados HTTP.");
            }
        }
        assertEquals(200,get(browser(),"/buscar?texto=").statusCode());
        assertTrue(get(browser(),"/buscar?texto="+edicion.toUpperCase(Locale.ROOT)).body().contains(edicion));
        long visitas=manejadores.ManejadorEventos.getInstance().obtenerEvento(evento).getVisitas();
        assertEquals(200,get(browser(),"/evento?nombre="+evento).statusCode());
        assertEquals(200,get(asistente,"/movil/eventos?evento="+evento).statusCode());
        long limite=System.nanoTime()+Duration.ofSeconds(5).toNanos();
        while(manejadores.ManejadorEventos.getInstance().obtenerEvento(evento).getVisitas()<visitas+2 && System.nanoTime()<limite)Thread.sleep(50);
        assertEquals(visitas+2,manejadores.ManejadorEventos.getInstance().obtenerEvento(evento).getVisitas());

        var actor=Actor.usuarioAutenticado(sistema.autenticar(org,"Otra-clave-43"));
        String pasada="pasada"+sufijo;
        sistema.altaEdicion(new DtEdicion(pasada,"PA",LocalDate.now().minusDays(3),LocalDate.now().minusDays(1),null,"Ciudad","País",org),evento,null,actor);
        sistema.aceptarRechazarEdicion(pasada,true,Actor.administradorLocal());
        sistema.altaTipoRegistro(new DtTipoRegistro("General","Entrada",100,10),pasada,actor);
        var registro=get(asistente,"/registro-edicion?edicion="+pasada+"&tipo=General");
        assertEquals(302,post(asistente,"/registro-edicion",Map.of("_csrf",csrf(registro),"evento",evento,"edicion",pasada,"tipo","General","modalidad","general")).statusCode());
        var detalle=get(organizador,"/edicion?nombre="+pasada);assertEquals(200,detalle.statusCode());
        assertEquals(403,post(asistente,"/archivar-edicion",Map.of("_csrf",csrf(registro),"edicion",pasada)).statusCode());
        assertEquals(302,post(organizador,"/archivar-edicion",Map.of("_csrf",csrf(detalle),"edicion",edicion)).statusCode());
        assertEquals(EstadoEdicion.ACEPTADA,manejadores.ManejadorEventos.getInstance().obtenerEdicion(edicion).getEstado(),"No archivar una edición que termina hoy.");
        assertEquals(302,post(organizador,"/archivar-edicion",Map.of("_csrf",csrf(detalle),"edicion",pasada)).statusCode());
        assertEquals(EstadoEdicion.ARCHIVADA,manejadores.ManejadorEventos.getInstance().obtenerEdicion(pasada).getEstado());
        assertEquals(404,get(browser(),"/edicion?nombre="+pasada).statusCode());
        assertTrue(get(browser(),"/usuario?nick="+org).body().contains(pasada));
        assertFalse(get(browser(),"/buscar?texto="+pasada).body().contains("nombre="+pasada));
        var propio=get(asistente,"/consulta-registro?edicion="+pasada);assertEquals(200,propio.statusCode());assertTrue(propio.body().contains(pasada));

        var fin=get(organizador,"/finalizar-evento");assertEquals(200,fin.statusCode());
        assertEquals(403,post(asistente,"/finalizar-evento",Map.of("_csrf",csrf(propio),"evento",evento)).statusCode());
        assertEquals(302,post(organizador,"/finalizar-evento",Map.of("_csrf",csrf(fin),"evento",evento)).statusCode());
        assertEquals(404,get(browser(),"/evento?nombre="+evento).statusCode());
        assertFalse(get(browser(),"/buscar?texto="+evento).body().contains("nombre="+evento));
        assertTrue(sistema.listarEventos().stream().anyMatch(e->evento.equals(e.getNombre())&&e.isFinalizado()));
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
    private HttpResponse<String> get(HttpClient c,String ruta)throws Exception {solicitudes++;return c.send(HttpRequest.newBuilder(URI.create(web+ruta)).timeout(Duration.ofSeconds(60)).GET().build(),HttpResponse.BodyHandlers.ofString());}
    private HttpResponse<String> post(HttpClient c,String ruta,Map<String,String> datos)throws Exception {
        solicitudes++;
        String cuerpo=datos.entrySet().stream().map(e->URLEncoder.encode(e.getKey(),StandardCharsets.UTF_8)+"="+URLEncoder.encode(e.getValue(),StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));
        return c.send(HttpRequest.newBuilder(URI.create(web+ruta)).timeout(Duration.ofSeconds(60)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(cuerpo)).build(),HttpResponse.BodyHandlers.ofString());
    }
    private HttpResponse<String> multipart(HttpClient c,String ruta,Map<String,String> datos,boolean imagen)throws Exception {
        solicitudes++;
        String limite="prueba"+UUID.randomUUID();var bytes=new ByteArrayOutputStream();
        for(var e:datos.entrySet())bytes.write(("--"+limite+"\r\nContent-Disposition: form-data; name=\""+e.getKey()+"\"\r\n\r\n"+e.getValue()+"\r\n").getBytes(StandardCharsets.UTF_8));
        if(imagen){bytes.write(("--"+limite+"\r\nContent-Disposition: form-data; name=\"imagen\"; filename=\"prueba.png\"\r\nContent-Type: image/png\r\n\r\n").getBytes(StandardCharsets.UTF_8));bytes.write(PNG);bytes.write("\r\n".getBytes(StandardCharsets.UTF_8));}
        bytes.write(("--"+limite+"--\r\n").getBytes(StandardCharsets.UTF_8));
        return c.send(HttpRequest.newBuilder(URI.create(web+ruta)).timeout(Duration.ofSeconds(60)).header("Content-Type","multipart/form-data; boundary="+limite).POST(HttpRequest.BodyPublishers.ofByteArray(bytes.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());
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
        long fin=System.nanoTime()+Duration.ofSeconds(150).toNanos();
        while(System.nanoTime()<fin){assertTrue(tomcat.isAlive(),"Tomcat terminó antes de publicar el WAR.");try{if(get(c,"/").statusCode()==200)return;}catch(IOException e){/* Arranque en curso. */}Thread.sleep(250);}
        fail("Tomcat no publicó el WAR dentro del plazo.");
    }
    @AfterEach void cerrar()throws Exception {
        if(tomcat!=null){tomcat.destroy();if(!tomcat.waitFor(15,java.util.concurrent.TimeUnit.SECONDS))tomcat.destroyForcibly();}
        if(central!=null)central.close();BaseDeDatos.cerrar();
    }
}
