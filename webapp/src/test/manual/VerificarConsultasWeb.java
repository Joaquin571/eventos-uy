import clases.NivelPatrocinio;
import datatypes.*;
import interfaces.ISistema;
import implementacion.Fabrica;
import persistencia.BaseDeDatos;
import jakarta.servlet.http.HttpServlet;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.jasper.servlet.JasperInitializer;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.*;

public class VerificarConsultasWeb {
    static String base;
    static int verificaciones;
    static final String sufijo=String.valueOf(System.nanoTime());
    static final String evento="000 Consultas "+sufijo,otroEvento="000 Otro "+sufijo;
    static final String aceptada="Aceptada & música "+sufijo,ingresada="Ingresada privada "+sufijo,rechazada="Rechazada privada "+sufijo;
    static final String otra="Otra aceptada "+sufijo,vacia="Sin tipos "+sufijo;
    static final String org="orgConsulta"+sufijo,ajeno="ajenoConsulta"+sufijo;
    static final String ana="anaConsulta"+sufijo,bea="beaConsulta"+sufijo,libre="libreConsulta"+sufijo;
    static final String institucion="Institución "+sufijo,codigo="NO-PUBLICAR-"+sufijo;
    static final byte[] png=Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jH3sAAAAASUVORK5CYII=");

    public static void main(String[] args)throws Exception {
        confirmarBase();
        ISistema sistema=Fabrica.getInstance().getISistema();
        preparar(sistema);
        Tomcat t=new Tomcat();t.setBaseDir(Path.of("webapp/target/tomcat-consultas-integracion").toAbsolutePath().toString());
        t.setPort(0);t.getConnector().setProperty("address","127.0.0.1");
        Context ctx=t.addContext("/pruebas",Path.of("webapp/src/main/webapp").toAbsolutePath().toString());
        ctx.setParentClassLoader(VerificarConsultasWeb.class.getClassLoader());Tomcat.initWebappDefaults(ctx);
        ctx.addServletContainerInitializer(new JasperInitializer(),Set.of());
        registrar(ctx,"login",new web.LoginServlet());registrar(ctx,"edicion",new web.ConsultaEdicionServlet());
        registrar(ctx,"tipo-registro",new web.ConsultaTipoRegistroServlet());registrar(ctx,"imagen",new web.ImagenServlet());
        registrar(ctx,"usuario",new web.UsuarioServlet());registrar(ctx,"consulta-registro",new web.ConsultaRegistroServlet());
        registrar(ctx,"evento",new web.EventoServlet());registrar(ctx,"registro-edicion",new web.RegistroEdicionServlet());
        registrar(ctx,"consulta-patrocinio",new web.ConsultaPatrocinioServlet());
        FilterDef f=new FilterDef();f.setFilterName("acceso");f.setFilter(new web.FiltroAcceso());ctx.addFilterDef(f);
        FilterMap m=new FilterMap();m.setFilterName("acceso");m.addURLPattern("/*");ctx.addFilterMap(m);
        try {
            t.start();ctx.getServletContext().setAttribute("sistema",sistema);
            base="http://127.0.0.1:"+t.getConnector().getLocalPort()+"/pruebas";
            HttpClient visitante=cliente(),duenio=cliente(),otro=cliente(),general=cliente(),gratuito=cliente(),sinRegistro=cliente();
            login(duenio,org);login(otro,ajeno);login(general,ana);login(gratuito,bea);login(sinRegistro,libre);
            for(HttpClient c:List.of(visitante,duenio,otro,general)) {
                var r=get(c,"/edicion?evento="+enc(evento));
                comprobar(r.statusCode()==200&&!r.body().contains(ingresada)&&!r.body().contains(rechazada),"Listado general solo aceptadas");
            }
            var r=get(duenio,"/usuario?nick="+enc(org));
            comprobar(r.statusCode()==200&&r.body().contains(ingresada)&&r.body().contains(rechazada),"Perfil propio conserva acceso a estados privados");
            r=get(visitante,"/usuario?nick="+enc(org));
            comprobar(r.statusCode()==200&&!r.body().contains(ingresada)&&!r.body().contains(rechazada),"Perfil ajeno no lista estados privados");
            r=get(visitante,"/evento?nombre="+enc(evento));
            comprobar(r.statusCode()==200&&r.body().contains("/pruebas/edicion?nombre="),"Evento enlaza edición");
            r=get(visitante,"/edicion?nombre="+enc(aceptada));
            comprobar(r.statusCode()==200&&r.body().contains("07/03/2026")&&r.body().contains("09/03/2026")&&r.body().contains("/pruebas/imagen?"),"Detalle con fechas e imagen");
            comprobar(r.body().contains("/pruebas/usuario?nick=")&&r.body().contains("/pruebas/tipo-registro?")&&r.body().contains("/pruebas/consulta-patrocinio?"),"Enlaces a organizador, tipos y patrocinios");
            comprobar(!r.body().contains("Asistentes registrados")&&!r.body().contains(ana)&&!r.body().contains(bea)&&!r.body().contains(codigo),"Detalle público sin registros ni códigos");
            r=get(duenio,"/edicion?nombre="+enc(aceptada));
            comprobar(r.body().contains("Asistentes registrados")&&r.body().contains(ana)&&r.body().contains(bea)&&r.body().contains("/pruebas/consulta-registro?"),"Dueño ve listado y enlaces de registros");
            r=get(general,"/edicion?nombre="+enc(aceptada)+"&asistente="+enc(bea)+"&usuario="+enc(org));
            comprobar(r.body().contains("Mi registro")&&r.body().contains("$100")&&!r.body().contains(bea)&&!r.body().contains("Asistentes registrados"),"Registro general propio e identidad de sesión");
            r=get(gratuito,"/edicion?nombre="+enc(aceptada));
            comprobar(r.body().contains("Mi registro")&&r.body().contains("$0")&&r.body().contains("Sí")&&!r.body().contains(ana)&&!r.body().contains(codigo),"Registro con código sin publicar el código");
            r=get(sinRegistro,"/edicion?nombre="+enc(aceptada));
            comprobar(r.body().contains(">Registrarme</a>")&&!r.body().contains("Mi registro"),"Asistente sin registro ve botón");
            r=get(sinRegistro,"/registro-edicion?edicion="+enc(aceptada));
            comprobar(r.statusCode()==200&&r.body().contains("General"),"Enlace de inscripción funciona");
            r=get(general,"/consulta-registro?edicion="+enc(aceptada));
            comprobar(r.statusCode()==200&&r.body().contains("/pruebas/tipo-registro?"),"Consulta de registro enlaza tipo");
            r=get(visitante,"/tipo-registro");
            comprobar(r.statusCode()==200&&r.body().contains("method=\"get\"")&&!r.body().contains("datos.js")&&!r.body().contains("tipoRegistro.js"),"Consulta dinámica independiente del prototipo");
            r=get(visitante,"/tipo-registro?evento="+enc(evento));
            comprobar(r.statusCode()==200&&!r.body().contains(ingresada)&&!r.body().contains(rechazada),"Selección de edición para tipos sin estados ocultos");
            r=get(visitante,"/tipo-registro?evento="+enc(evento)+"&edicion="+enc(aceptada));
            comprobar(r.statusCode()==200&&r.body().contains("name=\"nombre\"")&&r.body().contains("General"),"Selección de tipo por GET sin JavaScript");
            r=get(visitante,"/tipo-registro?edicion="+enc(aceptada)+"&nombre=General");
            comprobar(r.statusCode()==200&&r.body().contains("Acceso &lt;general&gt;")&&r.body().contains("Costo: $100")&&r.body().contains("Cupo: 50"),"Detalle completo con texto escapado");
            r=get(visitante,"/tipo-registro?edicion="+enc(otra)+"&nombre=General");
            comprobar(r.statusCode()==200&&r.body().contains("Costo: $200"),"Nombre de tipo repetido se resuelve por edición");
            for(String privada:List.of(ingresada,rechazada)) for(HttpClient c:List.of(visitante,otro,general,duenio)) {
                boolean propietario=c==duenio;
                r=get(c,"/edicion?nombre="+enc(privada));
                comprobar(r.statusCode()==(propietario?200:404),"Visibilidad de detalle privado");
                r=get(c,"/tipo-registro?edicion="+enc(privada)+"&nombre=General");
                comprobar(r.statusCode()==(propietario?200:404),"Visibilidad de tipo privado");
                var bytes=getBytes(c,"/imagen?tipo=edicion&id="+enc(privada));
                comprobar(bytes.statusCode()==(propietario?200:404)&&(!propietario||Arrays.equals(png,bytes.body())),"Imagen privada protegida");
            }
            var imagenPublica=getBytes(visitante,"/imagen?tipo=edicion&id="+enc(aceptada));
            comprobar(imagenPublica.statusCode()==200&&Arrays.equals(png,imagenPublica.body()),"Imagen pública devuelve bytes correctos");
            r=get(visitante,"/imagen?tipo=edicion&id="+enc(vacia));
            comprobar(r.statusCode()==302&&r.headers().firstValue("location").orElse("").endsWith("/img/default-avatar.png"),"Edición sin imagen mantiene alternativa");
            r=get(visitante,"/edicion?nombre="+enc(vacia));
            comprobar(r.statusCode()==200&&r.body().contains("no tiene tipos de registro")&&r.body().contains("no tiene patrocinios"),"Estados vacíos de edición");
            for(String url:List.of("/edicion?nombre=NoExiste","/edicion?evento="+enc(otroEvento)+"&nombre="+enc(aceptada),
                    "/tipo-registro?edicion="+enc(aceptada)+"&nombre=SoloOtra","/tipo-registro?nombre=General")) {
                comprobar(get(visitante,url).statusCode()==404,"Selección inexistente o incompatible devuelve 404");
            }
            r=get(visitante,"/consulta-patrocinio?edicion="+enc(aceptada)+"&institucion="+enc(institucion));
            comprobar(r.statusCode()==200&&!r.body().contains(codigo),"Consulta de patrocinio conserva privacidad");
            System.out.println("RESULTADO: "+verificaciones+" verificaciones HTTP/JSP/PostgreSQL de consultas aprobadas.");
        } finally {t.stop();t.destroy();BaseDeDatos.cerrar();}
    }

    static void preparar(ISistema s)throws Exception {
        for(String nick:List.of(org,ajeno)) {DtOrganizador d=new DtOrganizador(nick,nick,nick+"@test.uy","Descripción","");d.setContrasenia("clave");d.setConfirmacionContrasenia("clave");s.altaOrganizador(d);}
        s.altaInstitucion(new DtInstitucion(institucion,"Descripción","https://test.uy"));
        for(String nick:List.of(ana,bea,libre)) {DtAsistente d=new DtAsistente(nick,nick,nick+"@test.uy","Apellido",LocalDate.of(2000,1,1),nick.equals(bea)?institucion:null);d.setContrasenia("clave");d.setConfirmacionContrasenia("clave");s.altaAsistente(d);}
        String cat="000 Cat "+sufijo;s.altaCategoria(cat,null);
        for(String ev:List.of(evento,otroEvento))s.altaEvento(new DtEvento(ev,"EV","Descripción",LocalDate.now(),List.of(cat)));
        for(String nombre:List.of(aceptada,ingresada,rechazada,otra,vacia)) {
            s.altaEdicion(new DtEdicion(nombre,"ED",LocalDate.of(2026,3,7),LocalDate.of(2026,3,9),LocalDate.now(),"Montevideo","Uruguay",org),nombre.equals(otra)?otroEvento:evento);
            if(!nombre.equals(vacia))s.altaTipoRegistro(new DtTipoRegistro("General","Acceso <general>",nombre.equals(otra)?200:100,50),nombre);
            if(!nombre.equals(ingresada)&&!nombre.equals(rechazada))s.aceptarRechazarEdicion(nombre,true);
            if(!nombre.equals(vacia))s.asociarImagen(TipoEntidadImagen.EDICION,nombre,new DtImagen("image/png",png));
        }
        s.aceptarRechazarEdicion(rechazada,false);
        s.altaTipoRegistro(new DtTipoRegistro("SoloOtra","Exclusivo",0,5),otra);
        s.altaPatrocinio(new DtPatrocinio(LocalDate.now(),1000,1,codigo,NivelPatrocinio.ORO,institucion,aceptada,"General"));
        s.registroAEdicion(ana,aceptada,"General",new DtRegistro(LocalDate.now(),100));
        s.registroAEdicion(bea,aceptada,"General",new DtRegistro(LocalDate.now(),0),codigo);
    }
    static void confirmarBase()throws Exception {
        if(!"eventosuy_test".equals(System.getenv("EVENTOS_DB_NAME")))throw new IllegalStateException("No se seleccionó eventosuy_test.");
        var p=BaseDeDatos.propiedadesJpa();String url=p.get("jakarta.persistence.jdbc.url");
        if(!url.endsWith("/eventosuy_test"))throw new IllegalStateException("URL insegura.");
        Class.forName("org.postgresql.Driver");
        try(var c=DriverManager.getConnection(url,p.get("jakarta.persistence.jdbc.user"),p.get("jakarta.persistence.jdbc.password"))) {
            c.setReadOnly(true);try(var stmt=c.createStatement();var r=stmt.executeQuery("SELECT current_database()")) {
                if(!r.next()||!"eventosuy_test".equals(r.getString(1)))throw new IllegalStateException("Base real incorrecta.");
                System.out.println("BASE CONFIRMADA ANTES DE ESCRIBIR: "+r.getString(1));
            }
        }
    }
    static void registrar(Context c,String ruta,HttpServlet s){Tomcat.addServlet(c,ruta,s);c.addServletMappingDecoded("/"+ruta,ruta);}
    static HttpClient cliente(){return HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();}
    static void login(HttpClient c,String nick)throws Exception{comprobar(post(c,"/login",Map.of("identificador",nick,"contrasenia","clave")).statusCode()==302,"Login real");}
    static HttpResponse<String> get(HttpClient c,String ruta)throws Exception{return c.send(HttpRequest.newBuilder(URI.create(base+ruta)).GET().build(),HttpResponse.BodyHandlers.ofString());}
    static HttpResponse<byte[]> getBytes(HttpClient c,String ruta)throws Exception{return c.send(HttpRequest.newBuilder(URI.create(base+ruta)).GET().build(),HttpResponse.BodyHandlers.ofByteArray());}
    static HttpResponse<String> post(HttpClient c,String ruta,Map<String,String> datos)throws Exception {
        String body=datos.entrySet().stream().map(e->enc(e.getKey())+"="+enc(e.getValue())).reduce((a,b)->a+"&"+b).orElse("");
        return c.send(HttpRequest.newBuilder(URI.create(base+ruta)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    static String enc(String s){return URLEncoder.encode(s,StandardCharsets.UTF_8);}
    static void comprobar(boolean ok,String mensaje){if(!ok)throw new AssertionError(mensaje);verificaciones++;System.out.println("OK: "+mensaje);}
}
