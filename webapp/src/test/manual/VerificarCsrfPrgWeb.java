import org.apache.catalina.startup.Tomcat;
import java.io.ByteArrayOutputStream;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

/** WAR real: filtros, sesiones, JSP y multipart. Solo datos propios en eventosuy_test. */
public class VerificarCsrfPrgWeb {
    static String base;
    static int verificaciones;
    static final String sufijo = Long.toString(System.nanoTime());
    static final byte[] PNG = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII=");
    static final Pattern TOKEN = Pattern.compile("name=\"_csrf\" value=\"([A-Za-z0-9_-]{43})\"");

    public static void main(String[] args) throws Exception {
        // Falla cerrado si falta H02 en el classpath; ocurre antes del arranque JPA del WAR.
        Class.forName("soporte.ProteccionBasePruebas").getMethod("verificar").invoke(null);
        Tomcat t = new Tomcat();
        Path temporal = Files.createTempDirectory("eventosuy-csrf-prg-");
        Files.createDirectories(temporal.resolve("webapps")); t.setBaseDir(temporal.toString());
        t.setPort(0); t.getConnector().setProperty("address", "127.0.0.1");
        var ctx = t.addWebapp("", Path.of("webapp/target/webapp-1.0-SNAPSHOT.war").toAbsolutePath().toString());
        ctx.setParentClassLoader(VerificarCsrfPrgWeb.class.getClassLoader());
        try {
            t.start(); comprobar(ctx.getState().isAvailable(), "WAR real iniciado");
            base = "http://127.0.0.1:" + t.getConnector().getLocalPort();
            Object sistema = ctx.getServletContext().getAttribute("sistema");
            String cat = "Categoría CSRF " + sufijo, evento = "Evento CSRF " + sufijo;
            String ed = "Edición CSRF " + sufijo, org = "orgCsrf" + sufijo, as = "asCsrf" + sufijo;
            sistema.getClass().getMethod("altaCategoria", String.class, String.class).invoke(sistema, cat, null);
            HttpClient anonimo = cliente(), duenio = cliente();
            verificarFormulario(anonimo,"/login","formLogin");
            verificarFormulario(anonimo,"/alta-usuario","formAltaUsuario");
            String tokenAnonimo = token(anonimo, "/login");
            for (String ruta : List.of("/login", "/logout", "/alta-usuario", "/modificar-usuario", "/alta-evento",
                    "/alta-edicion", "/alta-tipo-registro", "/alta-institucion", "/alta-patrocinio", "/registro-edicion")) {
                comprobar(post(anonimo, ruta, Map.of()).statusCode() == 403, "POST sin token bloqueado: " + ruta);
                comprobar(post(anonimo, ruta, Map.of("_csrf", "incorrecto")).statusCode() == 403, "POST con token incorrecto bloqueado: " + ruta);
            }
            comprobar(post(cliente(), "/login", Map.of("_csrf", tokenAnonimo)).statusCode() == 403, "Token de otra sesión no autoriza");
            Map<String,String> usuario = new LinkedHashMap<>(Map.of("tipo","organizador","nickname",org,"nombre","Organización CSRF",
                    "correo",org+"@test.uy","descripcion","Descripción","sitioWeb","","contrasenia","clave","confirmacion","clave"));
            usuario.put("_csrf", token(duenio, "/alta-usuario"));
            comprobar(multipart(duenio,"/alta-usuario",usuario).statusCode() == 302, "Alta usuario multipart protegida");
            comprobar(Arrays.equals(getBytes(duenio,"/imagen?tipo=usuario&id="+enc(org)),PNG), "Part de imagen conservado después del filtro");
            String tokenAntes = token(duenio,"/login");
            comprobar(post(duenio,"/login",Map.of("identificador",org,"contrasenia","clave","_csrf",tokenAntes)).statusCode()==302,"Login con token válido");
            String actual = token(duenio,"/alta-edicion");
            verificarFormulariosValidacion(duenio);
            verificarFormulario(duenio,"/modificar-usuario","formModificarUsuario");
            verificarFormulario(duenio,"/alta-evento","formAltaEvento");
            verificarFormulario(duenio,"/alta-edicion","formAltaEdicion");
            comprobar(!tokenAntes.equals(actual),"Login renueva sesión y token");
            comprobar(post(duenio,"/alta-tipo-registro",Map.of("_csrf",tokenAntes)).statusCode()==403,"Token previo al login queda inválido");

            Map<String,String> ev = new LinkedHashMap<>(Map.of("nombre",evento,"sigla","EV","descripcion","Descripción","categorias",cat,
                    "_csrf",token(duenio,"/alta-evento")));
            comprobar(multipart(duenio,"/alta-evento",ev).statusCode()==302,"Alta evento multipart protegida");
            Map<String,String> edicion = new LinkedHashMap<>(Map.of("evento",evento,"nombre",ed,"sigla","ED","ciudad","Ciudad","pais","País",
                    "fechaInicio","2026-11-02","fechaFin","2026-11-03","_csrf",token(duenio,"/alta-edicion")));
            var r = multipart(duenio,"/alta-edicion",edicion);
            comprobar(r.statusCode()==302 && r.headers().firstValue("location").orElse("").equals("/alta-edicion"),"Edición usa PRG");
            comprobar(get(duenio,"/alta-edicion").body().contains("Edición creada correctamente"),"Mensaje de éxito tras redirect de edición");
            comprobar(!get(duenio,"/alta-edicion").body().contains("Edición creada correctamente"),"Mensaje edición de una sola lectura");
            comprobar(cantidad(sistema,"obtenerEdicionesEvento",evento)==1,"Refrescar GET no duplica edición");
            comprobar(Arrays.equals(getBytes(duenio,"/imagen?tipo=edicion&id="+enc(ed)),PNG),"Imagen de edición multipart persistida");
            edicion.put("fechaFin","2026-11-01"); edicion.put("nombre","Texto <preservado> "+sufijo);
            edicion.put("_csrf",token(duenio,"/alta-edicion"));
            r=multipart(duenio,"/alta-edicion",edicion);
            comprobar(r.statusCode()==200 && r.body().contains("Texto &lt;preservado&gt;")
                    && r.body().contains("value=\"2026-11-01\"") && r.body().contains("selected"),"Error edición preserva datos escapados y selección");

            Map<String,String> tipo = new LinkedHashMap<>(Map.of("edicion",ed,"nombre","General","descripcion","Descripción","costo","100","cupo","10",
                    "_csrf",token(duenio,"/alta-tipo-registro")));
            r=post(duenio,"/alta-tipo-registro",tipo);
            comprobar(r.statusCode()==302 && r.headers().firstValue("location").orElse("").equals("/alta-tipo-registro"),"Tipo usa PRG");
            comprobar(get(duenio,"/alta-tipo-registro").body().contains("Tipo de registro creado correctamente"),"Mensaje tipo tras redirect");
            comprobar(!get(duenio,"/alta-tipo-registro").body().contains("Tipo de registro creado correctamente"),"Mensaje tipo no se repite");
            comprobar(cantidad(sistema,"obtenerTiposRegistroEdicion",ed)==1,"Refrescar GET no duplica tipo");
            for(String costoInvalido:List.of("-1","NaN","Infinity")) {
                var invalido=new LinkedHashMap<>(tipo);
                invalido.put("nombre","No crear "+costoInvalido);invalido.put("costo",costoInvalido);
                invalido.put("_csrf",token(duenio,"/alta-tipo-registro"));
                comprobar(post(duenio,"/alta-tipo-registro",invalido).statusCode()==200,"Servidor rechaza costo sin validacion JS: "+costoInvalido);
                comprobar(cantidad(sistema,"obtenerTiposRegistroEdicion",ed)==1,"Costo invalido no crea tipo");
            }
            tipo.put("costo","invalido"); tipo.put("descripcion","Texto <preservado>"); tipo.put("_csrf",token(duenio,"/alta-tipo-registro"));
            r=post(duenio,"/alta-tipo-registro",tipo);
            comprobar(r.statusCode()==200 && r.body().contains("value=\"invalido\"") && r.body().contains("Texto &lt;preservado&gt;"),"Error tipo preserva datos escapados");

            String institucion="Institución CSRF "+sufijo;
            comprobar(multipart(duenio,"/alta-institucion",Map.of("nombre",institucion,"descripcion","Descripcion",
                    "sitioWeb","ftp://test.uy","_csrf",token(duenio,"/alta-institucion"))).statusCode()==200,
                    "Servidor rechaza URL invalida aunque no se ejecute JavaScript");
            comprobar(!(boolean)sistema.getClass().getMethod("existeInstitucion",String.class).invoke(sistema,institucion),
                    "URL invalida no persiste institucion");
            comprobar(multipart(duenio,"/alta-institucion",Map.of("nombre",institucion,"descripcion","Descripción","sitioWeb","https://test.uy",
                    "_csrf",token(duenio,"/alta-institucion"))).statusCode()==302,"Alta institución multipart protegida");
            verificarFormulario(duenio,"/alta-patrocinio?evento="+enc(evento)+"&edicion="+enc(ed),"formAltaPatrocinio");
            comprobar(multipart(duenio,"/modificar-usuario",Map.of("nombre","Nombre actualizado","descripcion","Descripción actualizada","sitioWeb","",
                    "_csrf",token(duenio,"/modificar-usuario"))).statusCode()==302,"Modificación perfil multipart protegida");
            comprobar(get(duenio,"/usuario?nick="+enc(org)).body().contains("Nombre actualizado"),"Perfil actualizado por multipart");
            verificarImagenes(duenio, org);
            comprobar(post(duenio,"/alta-patrocinio",Map.of("evento",evento,"edicion",ed,"institucion",institucion,"nivel","ORO","tipoRegistro","General",
                    "aporte","1000","registrograt","1","codigo","CSRF-"+sufijo,"_csrf",token(duenio,"/alta-patrocinio?edicion="+enc(ed)))).statusCode()==302,"Patrocinio POST protegido");

            HttpClient asistente=cliente();
            comprobar(multipart(asistente,"/alta-usuario",Map.of("tipo","asistente","nickname",as,"nombre","Ana","correo",as+"@test.uy","apellido","Pérez",
                    "fechaNac","2000-01-01","contrasenia","clave","confirmacion","clave","_csrf",token(asistente,"/alta-usuario"))).statusCode()==302,"Alta asistente protegida");
            comprobar(post(asistente,"/login",Map.of("identificador",as,"contrasenia","clave","_csrf",token(asistente,"/login"))).statusCode()==302,"Login asistente protegido");
            String tokenAs=token(asistente,"/usuario?nick="+enc(as));
            comprobar(post(asistente,"/alta-tipo-registro",Map.of("_csrf",tokenAs)).statusCode()==403,"Token válido no evita control de rol");
            sistema.getClass().getMethod("aceptarRechazarEdicion",String.class,boolean.class).invoke(sistema,ed,true);
            verificarFormulario(asistente,"/registro-edicion?edicion="+enc(ed),"formRegistro");
            comprobar(post(asistente,"/registro-edicion",Map.of("evento",evento,"edicion",ed,"tipo","General","modalidad","general",
                    "_csrf",token(asistente,"/registro-edicion?edicion="+enc(ed)))).statusCode()==302,"Registro POST protegido");

            comprobar(get(duenio,"/logout").statusCode()==405,"GET logout no modifica sesión");
            comprobar(get(duenio,"/modificar-usuario").statusCode()==200,"Sesión sigue activa después de GET logout");
            actual=token(duenio,"/modificar-usuario");
            comprobar(post(duenio,"/logout",Map.of("_csrf",actual)).statusCode()==302,"POST logout válido invalida sesión");
            comprobar(post(duenio,"/alta-tipo-registro",Map.of("_csrf",actual)).statusCode()==403,"Sesión invalidada bloquea POST");
            System.out.println("RESULTADO CSRF/PRG: "+verificaciones+" verificaciones HTTP/JSP/Part/PostgreSQL aprobadas.");
        } finally { t.stop(); t.destroy(); }
    }

    static int cantidad(Object sistema,String metodo,String clave)throws Exception {
        return ((Collection<?>)sistema.getClass().getMethod(metodo,String.class).invoke(sistema,clave)).size();
    }
    static void verificarFormulariosValidacion(HttpClient c)throws Exception {
        for(String ruta:List.of("/alta-institucion","/alta-tipo-registro")) {
            var r=get(c,ruta);
            comprobar(r.statusCode()==200 && r.body().contains("/js/validacion-altas.js"),"JSP sirve validacion propia: "+ruta);
            comprobar(r.body().contains("aria-describedby=") && r.body().contains("aria-live=\"polite\""),
                    "JSP contiene mensajes accesibles por campo: "+ruta);
            comprobar(r.body().contains("name=\"_csrf\""),"Validacion conserva token CSRF: "+ruta);
        }
        var js=get(c,"/js/validacion-altas.js");
        comprobar(js.statusCode()==200 && js.body().contains("formAltaInstitucion") && js.body().contains("formAltaTipoRegistro"),
                "Tomcat sirve script local para ambos formularios");
        var general=get(c,"/js/validacion-formularios.js");
        comprobar(general.statusCode()==200 && general.body().contains("aria-live") && general.body().contains("form.noValidate"),
                "Tomcat sirve validacion general con mensajes por campo");
    }
    static void verificarFormulario(HttpClient c,String ruta,String id)throws Exception {
        var r=get(c,ruta);
        comprobar(r.statusCode()==200 && r.body().contains("id=\""+id+"\""),"Formulario disponible: "+id);
        comprobar(r.body().contains("/js/validacion-formularios.js"),"Formulario incluye validacion general: "+id);
        comprobar(r.body().contains("name=\"_csrf\""),"Formulario conserva CSRF: "+id);
    }
    static HttpClient cliente(){return HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();}
    static HttpResponse<String> get(HttpClient c,String ruta)throws Exception {
        return c.send(HttpRequest.newBuilder(URI.create(base+ruta)).GET().build(),HttpResponse.BodyHandlers.ofString());
    }
    static byte[] getBytes(HttpClient c,String ruta)throws Exception {
        var r=c.send(HttpRequest.newBuilder(URI.create(base+ruta)).GET().build(),HttpResponse.BodyHandlers.ofByteArray());
        comprobar(r.statusCode()==200,"Imagen accesible según permisos");return r.body();
    }
    static String token(HttpClient c,String ruta)throws Exception {
        var r=get(c,ruta); if(r.statusCode()!=200)throw new AssertionError("Formulario no disponible: "+ruta);
        var m=TOKEN.matcher(r.body());if(!m.find())throw new AssertionError("Token ausente en formulario: "+ruta);
        return m.group(1);
    }
    static HttpResponse<String> post(HttpClient c,String ruta,Map<String,String> datos)throws Exception {
        String body=datos.entrySet().stream().map(e->enc(e.getKey())+"="+enc(e.getValue())).reduce((a,b)->a+"&"+b).orElse("");
        return c.send(HttpRequest.newBuilder(URI.create(base+ruta)).header("Content-Type","application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    static HttpResponse<String> multipart(HttpClient c,String ruta,Map<String,String> datos)throws Exception {
        return multipart(c,ruta,datos,PNG,"image/png","prueba.png");
    }
    static HttpResponse<String> multipart(HttpClient c,String ruta,Map<String,String> datos,byte[] imagen,String mime,String nombre)throws Exception {
        String limite="----eventosuy"+sufijo;var out=new ByteArrayOutputStream();
        out.write(("--"+limite+"\r\nContent-Disposition: form-data; name=\"imagen\"; filename=\""+nombre+"\"\r\nContent-Type: "+mime+"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(imagen);out.write("\r\n".getBytes(StandardCharsets.UTF_8));
        for(var e:datos.entrySet())out.write(("--"+limite+"\r\nContent-Disposition: form-data; name=\""+e.getKey()+"\"\r\n\r\n"+e.getValue()+"\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("--"+limite+"--\r\n").getBytes(StandardCharsets.UTF_8));
        return c.send(HttpRequest.newBuilder(URI.create(base+ruta)).header("Content-Type","multipart/form-data; boundary="+limite)
                .POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());
    }
    static void verificarImagenes(HttpClient c,String nick)throws Exception {
        byte[][] imagenes={PNG,soporte.ImagenesPrueba.imagen("jpeg"),soporte.ImagenesPrueba.gifAnimado(2),
                soporte.ImagenesPrueba.webp(),soporte.ImagenesPrueba.webpSinPerdida(),soporte.ImagenesPrueba.webpAnimado(2)};
        String[] formatos={"png","jpeg","gif","webp","webp","webp"};
        var datos=new LinkedHashMap<>(Map.of("nombre","Nombre actualizado","descripcion","Imagen real","sitioWeb",""));
        for(int i=0;i<imagenes.length;i++) {
            datos.put("_csrf",token(c,"/modificar-usuario"));
            var r=multipart(c,"/modificar-usuario",datos,imagenes[i],"image/"+formatos[i],"real."+formatos[i]);
            comprobar(r.statusCode()==302,"Imagen real multipart con CSRF: "+formatos[i]+" caso "+i);
            comprobar(Arrays.equals(getBytes(c,"/imagen?tipo=usuario&id="+enc(nick)),imagenes[i]),"Bytes originales sin conversiones: caso "+i);
        }
        byte[] anterior=imagenes[imagenes.length-1];
        byte[][] invalidas={Arrays.copyOf(PNG,PNG.length-1),new byte[]{(byte)137,80,78,71},
                soporte.ImagenesPrueba.gifAnimado(101),new byte[2*1024*1024+1]};
        for(int i=0;i<invalidas.length;i++) {
            datos.put("_csrf",token(c,"/modificar-usuario"));
            var r=multipart(c,"/modificar-usuario",datos,invalidas[i],i==2?"image/gif":"image/png",i==2?"invalida.gif":"invalida.png");
            comprobar(r.statusCode()==(i==3?413:200),"Rechazo de imagen multipart invalida: caso "+i);
            comprobar(Arrays.equals(getBytes(c,"/imagen?tipo=usuario&id="+enc(nick)),anterior),"Rechazo conserva imagen anterior: caso "+i);
        }
    }
    static String enc(String s){return URLEncoder.encode(s,StandardCharsets.UTF_8);}
    static void comprobar(boolean ok,String mensaje){if(!ok)throw new AssertionError(mensaje);verificaciones++;System.out.println("OK: "+mensaje);}
}
