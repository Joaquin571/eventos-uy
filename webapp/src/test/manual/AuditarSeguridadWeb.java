import org.apache.catalina.startup.Tomcat;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;

/** Pruebas HTTP reales. Los defectos se acumulan y producen AssertionError al finalizar. */
public class AuditarSeguridadWeb {
    static int correctas,fallidas;
    static Object sistema;
    static final String sufijo=Long.toString(System.nanoTime());

    public static void main(String[] args)throws Exception {
        Class.forName("soporte.ProteccionBasePruebas").getMethod("verificar").invoke(null);
        Tomcat t=new Tomcat();Path tmp=Files.createTempDirectory("eventosuy-auditoria-");
        Files.createDirectories(tmp.resolve("webapps"));t.setBaseDir(tmp.toString());t.setPort(0);
        t.getConnector().setProperty("address","127.0.0.1");
        var ctx=t.addWebapp("",Path.of("webapp/target/webapp-1.0-SNAPSHOT.war").toAbsolutePath().toString());
        ctx.setParentClassLoader(AuditarSeguridadWeb.class.getClassLoader());
        try {
            t.start();if(!ctx.getState().isAvailable())throw new AssertionError("WAR no disponible");
            VerificarCsrfPrgWeb.base="http://127.0.0.1:"+t.getConnector().getLocalPort();
            sistema=ctx.getServletContext().getAttribute("sistema");
            HttpClient anon=cliente(),a=cliente(),b=cliente(),as=cliente(),otro=cliente();
            String orgA="audOrgA"+sufijo,orgB="audOrgB"+sufijo,asA="audAsA"+sufijo,asB="audAsB"+sufijo;
            alta(a,orgA,"Org A","organizador",orgA+"@test.uy","clave");
            alta(b,orgB,"Org B","organizador",orgB+"@test.uy","clave");
            alta(as,asA,"As A","asistente",asA+"@test.uy","clave");
            alta(otro,asB,"As B","asistente",asB+"@test.uy","clave");
            login(a,orgA,"clave");login(b,orgB,"clave");login(as,asA,"clave");login(otro,asB,"clave");
            String cat="audCat"+sufijo,ev="audEv"+sufijo,ed="audEd"+sufijo,oculta="audOculta"+sufijo;
            invocar("altaCategoria",new Class[]{String.class,String.class},cat,null);
            exigir(VerificarCsrfPrgWeb.multipart(a,"/alta-evento",Map.of("nombre",ev,"sigla","EV","descripcion","Auditoría","categorias",cat,
                    "_csrf",token(a,"/alta-evento"))).statusCode()==302,"Preparación de evento");
            for(var par:List.of(Map.entry(a,ed),Map.entry(b,oculta))) {
                var datos=new HashMap<String,String>(Map.of("evento",ev,"nombre",par.getValue(),"sigla","ED","ciudad","Ciudad","pais","País",
                        "fechaInicio","2026-11-03","fechaFin","2026-11-04","organizador",orgB,"_csrf",token(par.getKey(),"/alta-edicion")));
                exigir(VerificarCsrfPrgWeb.multipart(par.getKey(),"/alta-edicion",datos).statusCode()==302,"Preparación de edición");
            }
            invocar("aceptarRechazarEdicion",new Class[]{String.class,boolean.class},ed,true);
            String tipo="TipoAud"+sufijo;
            exigir(post(a,"/alta-tipo-registro",Map.of("edicion",ed,"nombre",tipo,"descripcion","Tipo","costo","100","cupo","3",
                    "_csrf",token(a,"/alta-tipo-registro"))).statusCode()==302,"Preparación de tipo");
            verificar(invocar("obtenerEdicionesOrganizador",new Class[]{String.class},orgA).toString().contains(ed),"Propiedad sale de sesión, no del parámetro organizador");
            verificar(VerificarCsrfPrgWeb.get(anon,"/modificar-usuario").statusCode()==302,"Sin sesión no se abre modificación");
            verificar(post(as,"/alta-evento",Map.of("_csrf",token(as,"/usuario?nick="+asA))).statusCode()==403,"Token válido no elude el rol");
            verificar(VerificarCsrfPrgWeb.get(b,"/consulta-registro?edicion="+ed).statusCode()==403,"Organizador ajeno no consulta registros");
            verificar(VerificarCsrfPrgWeb.get(a,"/alta-patrocinio?edicion="+oculta).statusCode()==403,"GET de patrocinio ajeno bloqueado");
            verificar(post(a,"/alta-patrocinio",Map.of("edicion",oculta,"_csrf",token(a,"/alta-patrocinio"))).statusCode()==403,"POST de patrocinio ajeno bloqueado");
            post(a,"/alta-tipo-registro",Map.of("edicion",oculta,"nombre","NoCrear","descripcion","X","costo","1","cupo","1",
                    "_csrf",token(a,"/alta-tipo-registro")));
            verificar(((Collection<?>)invocar("obtenerTiposRegistroEdicion",new Class[]{String.class},oculta)).isEmpty(),"No se crea tipo en edición ajena");
            for(HttpClient c:List.of(anon,a,as)) {
                verificar(VerificarCsrfPrgWeb.get(c,"/edicion?nombre="+oculta).statusCode()==404,"Edición oculta no se accede por URL directa");
                verificar(VerificarCsrfPrgWeb.get(c,"/imagen?tipo=edicion&id="+oculta).statusCode()==404,"Imagen oculta no se accede por identificador");
            }
            verificar(VerificarCsrfPrgWeb.get(b,"/edicion?nombre="+oculta).statusCode()==200,"Dueño sí puede abrir edición oculta");
            var registro=post(as,"/registro-edicion",Map.of("edicion",ed,"tipo",tipo,"modalidad","general","asistente",asB,"nickname",asB,
                    "costo","0","_csrf",token(as,"/registro-edicion?edicion="+ed)));
            verificar(registro.statusCode()==302,"Registro legítimo aceptado");
            var propios=(Collection<?>)invocar("obtenerRegistrosAsistente",new Class[]{String.class},asA);
            var ajenos=(Collection<?>)invocar("obtenerRegistrosAsistente",new Class[]{String.class},asB);
            verificar(propios.size()==1 && ajenos.isEmpty(),"Manipular asistente no registra a otra persona");
            verificar(((Number)propios.iterator().next().getClass().getMethod("getCosto").invoke(propios.iterator().next())).floatValue()==100,"Manipular costo no vuelve gratuito el registro");
            verificar(!VerificarCsrfPrgWeb.get(otro,"/consulta-registro?edicion="+ed+"&asistente="+asA).body().contains(tipo),"Asistente no consulta registro ajeno cambiando parámetros");
            verificar(!VerificarCsrfPrgWeb.get(otro,"/usuario?nick="+asA).body().contains(tipo),"Perfil ajeno no publica sus registros");
            var datosAs=new HashMap<String,String>(Map.of("nickname",asB,"correo",asB+"@test.uy","tipo","organizador","nombre","Propio modificado",
                    "apellido","Apellido","fechaNac","2000-01-01","institucion","","_csrf",token(as,"/modificar-usuario")));
            exigir(VerificarCsrfPrgWeb.multipart(as,"/modificar-usuario",datosAs).statusCode()==302,"Modificación legítima");
            verificar(nombre(asA).equals("Propio modificado") && nombre(asB).equals("As B"),"Perfil y rol no se cambian a otra persona por parámetros");
            String marca="<script>alert('auditoria')</script>";
            exigir(VerificarCsrfPrgWeb.multipart(b,"/modificar-usuario",Map.of("nombre",marca,"descripcion","Auditoría","sitioWeb","javascript:alert(1)",
                    "_csrf",token(b,"/modificar-usuario"))).statusCode()==302,"Preparación de texto no confiable");
            String html=VerificarCsrfPrgWeb.get(anon,"/usuario?nick="+orgB).body();
            verificar(!html.contains(marca) && html.contains("&lt;script&gt;"),"Texto persistido se escapa, no se interpreta como script");
            verificar(!html.contains("href=\"javascript:"),"Sitio de organizador se muestra como texto, sin enlace ejecutable");
            for(String ruta:List.of("/WEB-INF/views/usuario.jsp","/web-inf/views/usuario.jsp"))
                verificar(VerificarCsrfPrgWeb.get(anon,ruta).statusCode()==404,"JSP privada no se abre directamente");
            auditarLoginAmbiguo();auditarErrorInterno();auditarLimites(a,cat,ev,ed);
            // Inyección de fallo exclusivamente en el contexto del WAR de prueba.
            Object real=sistema;
            Object proxy=java.lang.reflect.Proxy.newProxyInstance(real.getClass().getClassLoader(),real.getClass().getInterfaces(),(obj,m,argsMetodo)->{
                if(m.getName().equals("asociarImagen"))throw new IllegalArgumentException("Imagen temporalmente no disponible.");
                try{return m.invoke(real,argsMetodo);}catch(java.lang.reflect.InvocationTargetException e){throw e.getCause();}
            });
            try {
                ctx.getServletContext().setAttribute("sistema",proxy);
                var r=VerificarCsrfPrgWeb.multipart(a,"/modificar-usuario",Map.of("nombre","Perfil tras fallo","descripcion","Datos nuevos","sitioWeb","",
                        "contrasenia","nueva","confirmacion","nueva","_csrf",token(a,"/modificar-usuario")));
                boolean cambio=nombre(orgA).equals("Perfil tras fallo");
                boolean claveNueva=invocar("autenticar",new Class[]{String.class,String.class},orgA,"nueva")!=null;
                boolean error=r.statusCode()==200 && r.body().contains("Imagen temporalmente no disponible.");
                System.out.println("OBSERVADO perfil/imagen: formularioConError="+error+", datosPersistidos="+cambio+", claveNuevaActiva="+claveNueva);
                verificar(r.statusCode()==302 && cambio && claveNueva,"Fallo de imagen conserva perfil/clave y utiliza PRG");
                var destino=VerificarCsrfPrgWeb.get(a,r.headers().firstValue("Location").orElseThrow().replace(VerificarCsrfPrgWeb.base, ""));
                verificar(destino.body().contains("La imagen no se pudo actualizar") && destino.body().contains("Los datos del perfil"),
                        "Advertencia explícita de éxito parcial en destino");
                verificar(VerificarCsrfPrgWeb.get(a,"/").body().contains("Perfil tras fallo"),"Cabezal usa identidad de sesión actualizada");
                verificar(!VerificarCsrfPrgWeb.get(a,"/usuario?nick="+orgA).body().contains("La imagen no se pudo actualizar"),
                        "Advertencia flash consumida una sola vez");
            } finally {ctx.getServletContext().setAttribute("sistema",real);}
            auditarErrorInesperado(ctx);
            verificar(VerificarCsrfPrgWeb.get(a,"/logout").statusCode()==405,"GET logout no modifica estado");
            String viejo=token(a,"/modificar-usuario");exigir(post(a,"/logout",Map.of("_csrf",viejo)).statusCode()==302,"Logout protegido");
            verificar(post(a,"/modificar-usuario",Map.of("_csrf",viejo)).statusCode()==403,"Sesión invalidada rechaza token anterior");
        } finally {t.stop();t.destroy();}
        System.out.println("AUDITORIA: "+correctas+" comprobaciones correctas; "+fallidas+" expectativas fallidas.");
        if(fallidas>0)throw new AssertionError("Regresiones de seguridad: "+fallidas);
    }
    static void auditarLoginAmbiguo()throws Exception {
        String identificador="colision"+sufijo+"@test.uy";
        HttpClient x=cliente(),y=cliente();
        alta(x,identificador,"Colisión A","organizador","propio"+sufijo+"@test.uy","claveA");
        alta(y,"colisionB"+sufijo,"Colisión B","organizador",identificador,"claveB");
        int a=post(x,"/login",Map.of("identificador",identificador,"contrasenia","claveA","_csrf",token(x,"/login"))).statusCode();
        int b=post(y,"/login",Map.of("identificador",identificador,"contrasenia","claveB","_csrf",token(y,"/login"))).statusCode();
        System.out.println("OBSERVADO login cruzado: nickname/claveA="+a+", correo/claveB="+b);
        verificar(a==302 && b==302,"Usuarios válidos deben poder autenticarse por nickname/correo aun al coincidir columnas distintas");
        HttpClient z=cliente();
        var mala=post(z,"/login",Map.of("identificador",identificador,"contrasenia","incorrecta","_csrf",token(z,"/login")));
        verificar(mala.statusCode()==200 && mala.body().contains("no son correctos"),"Credenciales incorrectas dan mensaje genérico");
    }
    static void auditarErrorInterno()throws Exception {
        HttpClient c=cliente();String nick="longitud"+sufijo;
        var r=VerificarCsrfPrgWeb.multipart(c,"/alta-usuario",datosAlta(c,nick,"N".repeat(256),"organizador",nick+"@test.uy","clave"));
        boolean traza=r.body().contains("org.hibernate") || r.body().contains("jakarta.persistence") || r.body().contains("SQLException");
        System.out.println("OBSERVADO campo excesivo: HTTP="+r.statusCode()+", detallesInternos="+traza);
        verificar(r.statusCode()!=500 && !traza,"Un campo excesivo debe rechazarse sin 500 ni trazas internas");
        verificar(invocar("consultarUsuario",new Class[]{String.class},nick)==null,"Fallo de alta revierte la inserción");
    }

    static void auditarLimites(HttpClient c,String categoria,String evento,String edicion)throws Exception {
        var formularios=Map.of(
            "/alta-evento",Map.of("nombre","limEv"+sufijo,"sigla","EV","descripcion","Descripción","categorias",categoria),
            "/alta-edicion",Map.of("evento",evento,"nombre","limEd"+sufijo,"sigla","ED","ciudad","Ciudad","pais","País","fechaInicio","2026-11-03","fechaFin","2026-11-04"),
            "/alta-institucion",Map.of("nombre","limInst"+sufijo,"descripcion","Descripción","sitioWeb","https://ejemplo.test"),
            "/alta-tipo-registro",Map.of("edicion",edicion,"nombre","limTipo"+sufijo,"descripcion","Descripción","costo","0","cupo","1"));
        var campos=Map.of("/alta-evento",List.of("nombre","sigla","descripcion"),"/alta-edicion",List.of("nombre","sigla","ciudad","pais"),
                "/alta-institucion",List.of("nombre","descripcion","sitioWeb"),"/alta-tipo-registro",List.of("nombre","descripcion"));
        for(var f:formularios.entrySet())for(String campo:campos.get(f.getKey())) {
            var datos=new HashMap<>(f.getValue()); datos.put(campo,campo.equals("sitioWeb")?"https://"+"a".repeat(250)+".test":"X".repeat(256));
            datos.put("_csrf",token(c,f.getKey()));
            var r=f.getKey().equals("/alta-tipo-registro") ? post(c,f.getKey(),datos) : VerificarCsrfPrgWeb.multipart(c,f.getKey(),datos);
            verificar(r.statusCode()==200 && r.body().contains("255 caracteres") && !r.body().contains("org.hibernate"),
                    "Límite HTTP sin persistencia: "+f.getKey()+" / "+campo);
        }
        HttpClient anon=cliente();
        String nickValido="exacto255"+sufijo;
        var exacto=VerificarCsrfPrgWeb.multipart(anon,"/alta-usuario",datosAlta(anon,nickValido,"N".repeat(255),"organizador",nickValido+"@test.uy","clave"));
        verificar(exacto.statusCode()==302 && nombre(nickValido).length()==255,"Límite exacto HTTP de 255 se guarda");
        for(String valor:List.of("a\0b","Normal")) {
            String nick="formato"+System.nanoTime();
            var datos=datosAlta(anon,nick,valor,"organizador",valor.equals("Normal")?"correo-sin-arroba":nick+"@test.uy","clave");
            var r=VerificarCsrfPrgWeb.multipart(anon,"/alta-usuario",datos);
            verificar(r.statusCode()==200 && !r.body().contains("org.hibernate") && invocar("consultarUsuario",new Class[]{String.class},nick)==null,
                    "Formato inválido se rechaza sin insertar ni exponer trazas");
        }
        for(String costo:List.of("NaN","Infinity","-1")) {
            var datos=new HashMap<>(formularios.get("/alta-tipo-registro"));datos.put("costo",costo);datos.put("_csrf",token(c,"/alta-tipo-registro"));
            var r=post(c,"/alta-tipo-registro",datos);
            verificar(r.statusCode()==200 && !r.body().contains("org.hibernate"),"Costo inválido se conserva como error de formulario: "+costo);
        }
        for(String campo:List.of("nickname","nombre","correo","descripcion","sitioWeb")) {
            String nick="limUsuario"+campo+sufijo;
            var datos=datosAlta(anon,nick,"Nombre","organizador",nick+"@test.uy","clave");
            datos.put(campo,campo.equals("correo")?"x".repeat(250)+"@test.uy":"X".repeat(256));
            var r=VerificarCsrfPrgWeb.multipart(anon,"/alta-usuario",datos);
            verificar(r.statusCode()==200 && r.body().contains("255 caracteres"),"Límite HTTP de usuario: "+campo);
            verificar(invocar("consultarUsuario",new Class[]{String.class},nick)==null,"Alta excesiva no deja usuario: "+campo);
        }
    }
    static void auditarErrorInesperado(org.apache.catalina.Context contexto)throws Exception {
        Object real=sistema;
        Object proxy=java.lang.reflect.Proxy.newProxyInstance(real.getClass().getClassLoader(),real.getClass().getInterfaces(),(obj,m,args)->{
            if(m.getName().equals("listarUsuarios"))throw new IllegalStateException("org.hibernate SQLException DETALLE_INTERNO");
            try{return m.invoke(real,args);}catch(java.lang.reflect.InvocationTargetException e){throw e.getCause();}
        });
        try {
            contexto.getServletContext().setAttribute("sistema",proxy);
            var r=VerificarCsrfPrgWeb.get(cliente(),"/usuarios");
            verificar(r.statusCode()==500 && r.body().contains("error inesperado") && !r.body().contains("org.hibernate")
                    && !r.body().contains("DETALLE_INTERNO"),"Error inesperado devuelve página genérica sin trazas");
        } finally {contexto.getServletContext().setAttribute("sistema",real);}
    }
    static Map<String,String> datosAlta(HttpClient c,String nick,String nombre,String tipo,String correo,String clave)throws Exception {
        var d=new HashMap<String,String>(Map.of("tipo",tipo,"nickname",nick,"nombre",nombre,"correo",correo,"descripcion","Auditoría","sitioWeb","",
                "contrasenia",clave,"confirmacion",clave,"_csrf",token(c,"/alta-usuario")));
        if(tipo.equals("asistente")){d.put("apellido","Apellido");d.put("fechaNac","2000-01-01");}return d;
    }
    static void alta(HttpClient c,String n,String nombre,String tipo,String correo,String clave)throws Exception {
        exigir(VerificarCsrfPrgWeb.multipart(c,"/alta-usuario",datosAlta(c,n,nombre,tipo,correo,clave)).statusCode()==302,"Alta de fixture propia");
    }
    static void login(HttpClient c,String n,String clave)throws Exception {
        exigir(post(c,"/login",Map.of("identificador",n,"contrasenia",clave,"_csrf",token(c,"/login"))).statusCode()==302,"Login de fixture");
    }
    static Object invocar(String m,Class<?>[] tipos,Object...args)throws Exception {
        try{return sistema.getClass().getMethod(m,tipos).invoke(sistema,args);}
        catch(java.lang.reflect.InvocationTargetException e){if(e.getCause() instanceof Exception x)throw x;throw e;}
    }
    static String nombre(String n)throws Exception {Object u=invocar("consultarUsuario",new Class[]{String.class},n);return (String)u.getClass().getMethod("getNombre").invoke(u);}
    static HttpClient cliente(){return VerificarCsrfPrgWeb.cliente();}
    static String token(HttpClient c,String r)throws Exception{return VerificarCsrfPrgWeb.token(c,r);}
    static HttpResponse<String> post(HttpClient c,String r,Map<String,String>d)throws Exception{return VerificarCsrfPrgWeb.post(c,r,d);}
    static void exigir(boolean ok,String m){if(!ok)throw new AssertionError("Fixture no válida: "+m);}
    static void verificar(boolean ok,String m){if(ok){correctas++;System.out.println("OK AUD: "+m);}else{fallidas++;System.out.println("FALLA AUD: "+m);}}
}
