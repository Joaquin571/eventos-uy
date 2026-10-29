import clases.NivelPatrocinio;
import jakarta.servlet.http.HttpServlet;
import datatypes.*;
import implementacion.Fabrica;
import interfaces.ISistema;
import persistencia.BaseDeDatos;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.jasper.servlet.JasperInitializer;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

// Prueba explícita con el Tomcat instalado: no agrega dependencias al proyecto.
public class VerificarPatrociniosWeb {
    static String base;
    static int verificaciones;
    static String sufijo = String.valueOf(System.nanoTime());
    static String evento = "Evento integración " + sufijo;
    static String edicion = "Edición & música " + sufijo;
    static String oculta = "Ingresada privada " + sufijo;
    static String rechazada = "Rechazada privada " + sufijo;
    static String institucion = "Institución & UTEC " + sufijo;
    static String institucionNueva = "Nueva <UTEC> " + sufijo;
    static String codigo = "SECRETO-" + sufijo;

    public static void main(String[] args) throws Exception {
        confirmarBase();
        ISistema sistema = Fabrica.getInstance().getISistema();
        String duenio = "orgWeb" + sufijo, ajeno = "ajenoWeb" + sufijo, asistente = "asisWeb" + sufijo;
        for (String nick : List.of(duenio, ajeno)) {
            DtOrganizador d = new DtOrganizador(nick, nick, nick + "@test.uy", "Descripción", "");
            d.setContrasenia("clave");d.setConfirmacionContrasenia("clave");sistema.altaOrganizador(d);
        }
        DtAsistente a = new DtAsistente(asistente, "Asistente", asistente + "@test.uy", "Apellido", LocalDate.of(2000,1,1), null);
        a.setContrasenia("clave");a.setConfirmacionContrasenia("clave");sistema.altaAsistente(a);
        sistema.altaCategoria("catWeb" + sufijo, null);
        sistema.altaEvento(new DtEvento(evento,"EV","Descripción",LocalDate.now(),List.of("catWeb"+sufijo)));
        for (String nombre : List.of(edicion, oculta, rechazada)) {
            LocalDate h = LocalDate.now();
            sistema.altaEdicion(new DtEdicion(nombre,"ED",h,h,h,"Ciudad","País",duenio),evento);
            sistema.altaTipoRegistro(new DtTipoRegistro("General","General",100,50),nombre);
        }
        sistema.aceptarRechazarEdicion(edicion,true);sistema.aceptarRechazarEdicion(rechazada,false);
        sistema.altaInstitucion(new DtInstitucion(institucion,"Descripción","https://test.uy"));
        sistema.altaInstitucion(new DtInstitucion(institucionNueva,"Descripción","https://test.uy"));
        for (String nombre : List.of(edicion,oculta,rechazada)) {
            sistema.altaPatrocinio(new DtPatrocinio(LocalDate.of(1999,1,1),1000,1,codigo+nombre,
                    NivelPatrocinio.ORO,institucion,nombre,"General"));
        }
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(Path.of("webapp/target/tomcat-patrocinios-integracion").toAbsolutePath().toString());
        tomcat.setPort(0);tomcat.getConnector().setProperty("address","127.0.0.1");
        Context ctx = tomcat.addContext("/pruebas",Path.of("webapp/src/main/webapp").toAbsolutePath().toString());
        ctx.setParentClassLoader(VerificarPatrociniosWeb.class.getClassLoader());
        Tomcat.initWebappDefaults(ctx);ctx.addServletContainerInitializer(new JasperInitializer(),Set.of());
        registrar(ctx,"login",new web.LoginServlet());
        registrar(ctx,"alta-patrocinio",new web.AltaPatrocinioServlet());
        registrar(ctx,"consulta-patrocinio",new web.ConsultaPatrocinioServlet());
        registrar(ctx,"edicion",new web.ConsultaEdicionServlet());
        FilterDef filtro = new FilterDef();filtro.setFilterName("acceso");filtro.setFilter(new web.FiltroAcceso());ctx.addFilterDef(filtro);
        FilterMap mapa = new FilterMap();mapa.setFilterName("acceso");mapa.addURLPattern("/*");ctx.addFilterMap(mapa);
        try {
            tomcat.start();ctx.getServletContext().setAttribute("sistema",sistema);
            base="http://127.0.0.1:"+tomcat.getConnector().getLocalPort()+"/pruebas";
            HttpClient publico=cliente(), propietario=cliente(), otro=cliente(), asist=cliente();
            login(propietario,duenio);login(otro,ajeno);login(asist,asistente);
            var r=get(propietario,"/alta-patrocinio?evento="+enc(evento));
            comprobar(r.statusCode()==200 && r.body().contains(encHtml(oculta)) && r.body().contains(encHtml(rechazada)),"Alta ofrece estados propios");
            r=get(propietario,"/alta-patrocinio?edicion="+enc(edicion));
            comprobar(r.statusCode()==200 && r.body().contains("data-costo=\"100.0\"") && r.body().contains("name=\"codigo\""),"Alta carga tipos e instituciones");
            Map<String,String> datos=new LinkedHashMap<>(Map.of("evento",evento,"edicion",edicion,"institucion",institucionNueva,
                    "nivel","ORO","tipoRegistro","General","aporte","1000","registrograt","2","codigo","NUEVO-"+sufijo));
            r=post(propietario,"/alta-patrocinio",datos);
            comprobar(r.statusCode()==302 && r.headers().firstValue("location").orElse("").contains("%26"),"Alta real y redirección codificada");
            String destino=r.headers().firstValue("location").orElseThrow();
            r=get(propietario,destino.substring("/pruebas".length()));
            comprobar(r.statusCode()==200 && r.body().contains("NUEVO-"+sufijo)
                    && r.body().contains(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    && r.body().contains("Nueva &lt;UTEC&gt;"),"Detalle persistido, fecha y escape HTML");
            comprobar(sistema.obtenerPatrociniosEdicion(edicion).size()==2,"Persistencia sin duplicar al consultar");
            datos.put("codigo","OTRO-"+sufijo);
            r=post(propietario,"/alta-patrocinio",datos);
            comprobar(r.statusCode()==200 && r.body().contains("ya posee") && r.body().contains("value=\"OTRO-"+sufijo+"\"")
                    && r.body().contains("value=\"1000\""),"Duplicado conserva campos y mensaje");
            datos.put("nivel","DIAMANTE");datos.put("aporte","NaN");
            r=post(propietario,"/alta-patrocinio",datos);
            comprobar(r.statusCode()==200 && r.body().contains("value=\"NaN\"") && r.body().contains("DIAMANTE"),"Formulario inválido conserva valores sin romper JSP");
            datos.put("aporte","1000");
            r=post(propietario,"/alta-patrocinio",datos);
            comprobar(r.statusCode()==200 && r.body().contains("nivel de patrocinio válido"),"Nivel inválido tiene mensaje español");
            r=post(otro,"/alta-patrocinio",datos);comprobar(r.statusCode()==403,"POST ajeno sigue protegido");
            r=get(otro,"/alta-patrocinio?edicion="+enc(edicion));comprobar(r.statusCode()==403,"GET de tipos ajenos protegido");
            r=get(publico,"/consulta-patrocinio");comprobar(r.statusCode()==200 && r.body().contains("name=\"evento\""),"Inicio de consulta público");
            r=get(publico,"/consulta-patrocinio?evento="+enc(evento));
            comprobar(r.statusCode()==200 && r.body().contains(encHtml(edicion)) && !r.body().contains(oculta) && !r.body().contains(rechazada),"Selector público no expone estados ocultos");
            r=get(publico,"/consulta-patrocinio?edicion="+enc(edicion));
            comprobar(r.statusCode()==200 && r.body().contains(encHtml(institucion)) && !r.body().contains(codigo),"Selector de patrocinios no expone códigos");
            for (String nombre:List.of(edicion,oculta,rechazada)) {
                String url="/consulta-patrocinio?edicion="+enc(nombre)+"&institucion="+enc(institucion);
                for (HttpClient c:List.of(publico,asist,otro,propietario)) {
                    r=get(c,url);boolean propio=c==propietario;boolean visible=nombre.equals(edicion)||propio;
                    comprobar(r.statusCode()==(visible?200:404) && r.body().contains(codigo)==propio,
                            "Privacidad HTTP "+(nombre.equals(edicion)?"aceptada":nombre.equals(oculta)?"ingresada":"rechazada")+" / "+(propio?"dueño":c==publico?"visitante":c==asist?"asistente":"ajeno"));
                }
            }
            r=get(publico,"/edicion?nombre="+enc(edicion));
            comprobar(r.statusCode()==200 && r.body().contains("/pruebas/consulta-patrocinio?") && !r.body().contains(codigo),"Edición enlaza patrocinios sin códigos");
            System.out.println("RESULTADO: "+verificaciones+" verificaciones HTTP/JSP/PostgreSQL aprobadas.");
        } finally { tomcat.stop();tomcat.destroy();BaseDeDatos.cerrar(); }
    }
    static void confirmarBase() throws Exception {
        if (!"eventosuy_test".equals(System.getenv("EVENTOS_DB_NAME"))) throw new IllegalStateException("Elegí eventosuy_test.");
        var p=BaseDeDatos.propiedadesJpa();String url=p.get("jakarta.persistence.jdbc.url");
        if(!url.endsWith("/eventosuy_test"))throw new IllegalStateException("URL insegura.");
        Class.forName("org.postgresql.Driver");
        try(var c=DriverManager.getConnection(url,p.get("jakarta.persistence.jdbc.user"),p.get("jakarta.persistence.jdbc.password"))) {
            c.setReadOnly(true);
            try(var s=c.createStatement();var r=s.executeQuery("SELECT current_database()")) {
                if(!r.next()||!"eventosuy_test".equals(r.getString(1)))throw new IllegalStateException("Base real incorrecta.");
                System.out.println("BASE CONFIRMADA ANTES DE ESCRIBIR: "+r.getString(1));
            }
        }
    }
    static void registrar(Context c,String ruta,HttpServlet servlet) {
        Tomcat.addServlet(c,ruta,servlet);c.addServletMappingDecoded("/"+ruta,ruta);
    }
    static HttpClient cliente() {return HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();}
    static void login(HttpClient c,String nick)throws Exception {comprobar(post(c,"/login",Map.of("identificador",nick,"contrasenia","clave")).statusCode()==302,"Login real "+nick);}
    static HttpResponse<String> get(HttpClient c,String ruta)throws Exception {return c.send(HttpRequest.newBuilder(URI.create(base+ruta)).GET().build(),HttpResponse.BodyHandlers.ofString());}
    static HttpResponse<String> post(HttpClient c,String ruta,Map<String,String> datos)throws Exception {
        String body=datos.entrySet().stream().map(e->enc(e.getKey())+"="+enc(e.getValue())).reduce((a,b)->a+"&"+b).orElse("");
        return c.send(HttpRequest.newBuilder(URI.create(base+ruta)).header("Content-Type","application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    static String enc(String s){return URLEncoder.encode(s,StandardCharsets.UTF_8);}
    static String encHtml(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    static void comprobar(boolean ok,String mensaje){if(!ok)throw new AssertionError(mensaje);verificaciones++;System.out.println("OK: "+mensaje);}
}
