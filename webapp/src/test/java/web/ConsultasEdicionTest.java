package web;

import clases.NivelPatrocinio;
import datatypes.*;
import interfaces.ISistema;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ConsultasEdicionTest {
    @Test
    void listasGeneralesSoloIncluyenAceptadasInclusoParaElDuenio() throws Exception {
        for (DtUsuario usuario : usuarios()) {
            Escenario e = new Escenario(usuario);e.parametros.put("evento", "Evento");
            new ConsultaEdicionServlet().doGet(e.req,e.resp);
            assertEquals(200,e.estado);
            assertEquals(List.of("Aceptada"), ((List<DtEdicion>)e.atributos.get("ediciones")).stream().map(DtEdicion::getIdNombre).toList());
            assertEquals(0,e.imagenes);assertEquals(0,e.registrosEdicion);assertEquals(0,e.registrosAsistente);
        }
    }

    @Test
    void detallesOcultosSoloSonAccesiblesAlPropietarioSinCargarDatosParaTerceros() throws Exception {
        for (String nombre : List.of("Ingresada","Rechazada")) {
            for (DtUsuario usuario : usuarios()) {
                Escenario e = new Escenario(usuario);e.parametros.put("nombre",nombre);
                new ConsultaEdicionServlet().doGet(e.req,e.resp);
                if (usuario instanceof DtOrganizador && usuario.getNickname().equals("duenio")) {
                    assertEquals(200,e.estado);assertTrue(e.reenviado);assertEquals(1,e.imagenes);
                    assertEquals(List.of("Aceptada"), ((List<DtEdicion>)e.atributos.get("ediciones")).stream().map(DtEdicion::getIdNombre).toList());
                } else {
                    assertEquals(404,e.estado);assertFalse(e.reenviado);
                    assertEquals(0,e.imagenes);assertEquals(0,e.tipos);assertEquals(0,e.patrocinios);
                    assertEquals(0,e.registrosEdicion);assertEquals(0,e.registrosAsistente);
                    assertFalse(e.atributos.containsKey("detalle"));
                }
            }
        }
    }

    @Test
    void detallePublicoMuestraFechasOrganizadorYResumenSinCodigo() throws Exception {
        Escenario e = new Escenario(null);e.parametros.put("nombre","Aceptada");
        new ConsultaEdicionServlet().doGet(e.req,e.resp);
        assertEquals("Evento",e.atributos.get("eventoElegido"));
        assertEquals("07/03/2026",e.atributos.get("fechaInicio"));assertEquals("09/03/2026",e.atributos.get("fechaFin"));
        assertEquals("01/02/2026",e.atributos.get("fechaAlta"));
        assertEquals("duenio",((DtUsuario)e.atributos.get("organizador")).getNickname());
        assertEquals(List.of("UTEC"),e.atributos.get("patrocinadoras"));
        assertFalse(e.atributos.toString().contains("NO-PUBLICAR"));
        assertEquals(0,e.registrosEdicion);assertEquals(0,e.registrosAsistente);
    }

    @Test
    void organizadorAjenoNoConsultaRegistros() throws Exception {
        Escenario e = new Escenario(organizador("ajeno"));e.parametros.put("nombre","Aceptada");
        e.parametros.put("usuario","duenio");e.parametros.put("esPropietario","true");
        new ConsultaEdicionServlet().doGet(e.req,e.resp);
        assertEquals(0,e.registrosEdicion);assertEquals(0,e.registrosAsistente);
        assertFalse(e.atributos.containsKey("registros"));assertFalse(e.atributos.containsKey("registroPropio"));
    }

    @Test
    void propietarioVeListadoSinConsultarRegistrosDeAsistente() throws Exception {
        Escenario e = new Escenario(organizador("duenio"));e.parametros.put("nombre","Aceptada");
        new ConsultaEdicionServlet().doGet(e.req,e.resp);
        assertEquals(true,e.atributos.get("esPropietario"));
        assertEquals(2,((List<?>)e.atributos.get("registros")).size());assertEquals(1,e.registrosEdicion);assertEquals(0,e.registrosAsistente);
        assertFalse(e.atributos.containsKey("puedeRegistrarse"));
    }

    @Test
    void asistenteRegistradoVeSoloSuDetalleAunqueManipuleParametros() throws Exception {
        Escenario e = new Escenario(asistente("ana"));e.parametros.put("nombre","Aceptada");
        e.parametros.put("asistente","bea");e.parametros.put("nickname","bea");
        new ConsultaEdicionServlet().doGet(e.req,e.resp);
        DetalleRegistroVista r=(DetalleRegistroVista)e.atributos.get("registroPropio");
        assertEquals("ana",r.getNickname());assertEquals("Sí",r.getUsoCodigo());assertEquals("0",r.getCosto());
        assertEquals("ana",e.nickConsultado);assertEquals(false,e.atributos.get("puedeRegistrarse"));
        assertEquals(0,e.registrosEdicion);assertFalse(e.atributos.containsKey("registros"));
    }

    @Test
    void asistenteNoRegistradoPuedeInscribirseSinRecibirRegistrosAjenos() throws Exception {
        Escenario e = new Escenario(asistente("nueva"));e.parametros.put("nombre","Aceptada");
        new ConsultaEdicionServlet().doGet(e.req,e.resp);
        assertEquals(true,e.atributos.get("puedeRegistrarse"));assertFalse(e.atributos.containsKey("registroPropio"));assertEquals(0,e.registrosEdicion);
    }

    @Test
    void recursosInexistentesCapitalizacionYEventoIncompatibleDevuelven404() throws Exception {
        for (Map<String,String> p : List.of(Map.of("nombre","NoExiste"),Map.of("nombre","ACEPTADA"),
                Map.of("evento","Otro","nombre","Aceptada"),Map.of("evento","NoExiste"))) {
            Escenario e = new Escenario(organizador("duenio"));e.parametros.putAll(p);
            new ConsultaEdicionServlet().doGet(e.req,e.resp);
            assertEquals(404,e.estado);assertEquals(0,e.imagenes);assertEquals(0,e.registrosEdicion);
        }
    }

    @Test
    void sesionAntiguaNoAutorizaDetalleOculto() throws Exception {
        Escenario e = new Escenario(null);e.sesion.put("usuarioLogueado",organizador("duenio"));
        e.parametros.put("nombre","Ingresada");new ConsultaEdicionServlet().doGet(e.req,e.resp);
        assertEquals(404,e.estado);assertEquals(0,e.imagenes);
    }

    @Test
    void tiposSeConsultanPorEdicionYNombreConDatosCompletos() throws Exception {
        Escenario e = new Escenario(null);e.parametros.putAll(Map.of("edicion","Aceptada","nombre","General"));
        new ConsultaTipoRegistroServlet().doGet(e.req,e.resp);
        DtTipoRegistro t=(DtTipoRegistro)e.atributos.get("detalle");
        assertEquals("Descripción pública",t.getDescripcion());assertEquals(50,t.getCupo());assertEquals("100",e.atributos.get("costo"));
        Escenario otra = new Escenario(null);otra.parametros.putAll(Map.of("edicion","OtraAceptada","nombre","General"));
        new ConsultaTipoRegistroServlet().doGet(otra.req,otra.resp);
        assertEquals("200",otra.atributos.get("costo"));assertEquals("Otro",otra.atributos.get("eventoElegido"));
    }

    @Test
    void recorridoDeTiposTieneListasPublicasYEstadosVacios() throws Exception {
        Escenario e = new Escenario(organizador("duenio"));e.parametros.put("evento","Evento");
        new ConsultaTipoRegistroServlet().doGet(e.req,e.resp);
        assertEquals(1,((List<?>)e.atributos.get("ediciones")).size());assertEquals(0,e.tipos);
        Escenario vacio = new Escenario(null);vacio.parametros.put("edicion","SinTipos");
        new ConsultaTipoRegistroServlet().doGet(vacio.req,vacio.resp);
        assertEquals(List.of(),vacio.atributos.get("tipos"));assertEquals(200,vacio.estado);
    }

    @Test
    void tiposOcultosSoloParaElDuenioSinConsultaPreviaParaTerceros() throws Exception {
        for(String nombre:List.of("Ingresada","Rechazada")) for(DtUsuario usuario:usuarios()) {
            Escenario e = new Escenario(usuario);e.parametros.putAll(Map.of("edicion",nombre,"nombre","General"));
            new ConsultaTipoRegistroServlet().doGet(e.req,e.resp);
            boolean propio=usuario instanceof DtOrganizador && usuario.getNickname().equals("duenio");
            assertEquals(propio?200:404,e.estado);assertEquals(propio?1:0,e.consultasTipo);assertEquals(propio?1:0,e.tipos);
        }
    }

    @Test
    void tipoInexistenteDeOtraEdicionONombreSinEdicionDevuelve404() throws Exception {
        for(Map<String,String> p:List.of(Map.of("edicion","Aceptada","nombre","SoloOtra"),Map.of("nombre","General"),
                Map.of("edicion","Aceptada","nombre","general"),Map.of("evento","Otro","edicion","Aceptada"))) {
            Escenario e = new Escenario(null);e.parametros.putAll(p);
            new ConsultaTipoRegistroServlet().doGet(e.req,e.resp);assertEquals(404,e.estado);assertFalse(e.reenviado);
        }
    }

    @Test
    void imagenOcultaSeRechazaAntesDeLeerBytes() throws Exception {
        for(String nombre:List.of("Ingresada","Rechazada")) for(DtUsuario usuario:usuarios()) {
            Escenario e = new Escenario(usuario);e.parametros.putAll(Map.of("tipo","edicion","id",nombre));
            new ImagenServlet().doGet(e.req,e.resp);
            boolean propio=usuario instanceof DtOrganizador && usuario.getNickname().equals("duenio");
            assertEquals(propio?200:404,e.estado);assertEquals(propio?1:0,e.imagenes);
            assertEquals(propio?3:0,e.bytes.size());
        }
    }

    @Test
    void imagenPublicaYAusenteMantienenSuComportamiento() throws Exception {
        Escenario e = new Escenario(null);e.parametros.putAll(Map.of("tipo","edicion","id","Aceptada"));
        new ImagenServlet().doGet(e.req,e.resp);assertEquals(3,e.bytes.size());assertEquals("private, no-store",e.headers.get("Cache-Control"));
        Escenario sinImagen=new Escenario(null);sinImagen.imagen=null;sinImagen.parametros.putAll(e.parametros);
        new ImagenServlet().doGet(sinImagen.req,sinImagen.resp);assertEquals(302,sinImagen.estado);assertEquals("/pruebas/img/default-avatar.png",sinImagen.redireccion);
    }

    @Test
    void imagenDeEdicionInexistenteOVaciaNoLeeBytes() throws Exception {
        for(String nombre:List.of("NoExiste","")) {
            Escenario e=new Escenario(organizador("duenio"));e.parametros.putAll(Map.of("tipo","edicion","id",nombre));
            new ImagenServlet().doGet(e.req,e.resp);assertEquals(404,e.estado);assertEquals(0,e.imagenes);
        }
    }

    @Test
    void imagenDeUsuarioNoAdquiereRestriccionesDeEdicion() throws Exception {
        Escenario e=new Escenario(null);e.parametros.putAll(Map.of("tipo","usuario","id","ana"));
        new ImagenServlet().doGet(e.req,e.resp);assertEquals(200,e.estado);assertEquals(1,e.imagenes);assertEquals("no-cache",e.headers.get("Cache-Control"));
    }

    static DtOrganizador organizador(String nick) {return new DtOrganizador(nick,nick,nick+"@test.uy","Descripción","");}
    static DtAsistente asistente(String nick) {return new DtAsistente(nick,nick,nick+"@test.uy","Apellido",LocalDate.of(2000,1,1),null);}
    static List<DtUsuario> usuarios() {return Arrays.asList(null,organizador("duenio"),organizador("ajeno"),asistente("ana"),asistente("duenio"));}

    static final class Escenario {
        final Map<String,String> parametros=new HashMap<>();
        final Map<String,Object> atributos=new HashMap<>(),sesion=new HashMap<>();
        final Map<String,String> headers=new HashMap<>();
        final Map<String,List<DtEdicion>> ediciones=new HashMap<>();
        final ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        DtImagen imagen=new DtImagen("image/png",new byte[]{1,2,3});
        int estado=200,imagenes,registrosEdicion,registrosAsistente,tipos,consultasTipo,patrocinios;
        boolean reenviado;String nickConsultado,redireccion;
        final LocalDate fecha=LocalDate.of(2026,3,7);
        final List<DtRegistro> registros=List.of(new DtRegistro(fecha,0,"General","Aceptada","ana",true),new DtRegistro(fecha,100,"General","Aceptada","bea",false));
        final ISistema sistema=doble(ISistema.class,(p,m,a)->switch(m.getName()) {
            case "listarEventos" -> List.of(new DtEvento("Evento","EV","Descripción",fecha,List.of()),new DtEvento("Otro","OT","",fecha,List.of()));
            case "obtenerEdicionesEvento" -> ediciones.getOrDefault(a[0],List.of());
            case "consultarUsuario" -> organizador((String)a[0]);
            case "obtenerImagen" -> {imagenes++;yield imagen;}
            case "obtenerTiposRegistroEdicion" -> {tipos++;yield a[0].equals("SinTipos")?List.of():List.of(tipo((String)a[0],"General"));}
            case "consultarTipoRegistro" -> {consultasTipo++;yield ((String)a[1]).equalsIgnoreCase("General")?tipo((String)a[0],"General"):null;}
            case "obtenerPatrociniosEdicion" -> {patrocinios++;yield List.of(new DtPatrocinio(fecha,1000,1,"NO-PUBLICAR",NivelPatrocinio.ORO,"UTEC",(String)a[0],"General"));}
            case "obtenerRegistrosEdicion" -> {registrosEdicion++;yield a[0].equals("Aceptada")?registros:List.of();}
            case "obtenerRegistrosAsistente" -> {registrosAsistente++;nickConsultado=(String)a[0];yield registros.stream().filter(r->r.getNicknameAsistente().equals(a[0])).toList();}
            default -> throw new AssertionError("Consulta inesperada: "+m.getName());
        });
        final ServletContext contexto=doble(ServletContext.class,(p,m,a)->{if(m.getName().equals("getAttribute")&&a[0].equals("sistema"))return sistema;throw new AssertionError(m.getName());});
        final HttpSession httpSesion=doble(HttpSession.class,(p,m,a)->{if(m.getName().equals("getAttribute"))return sesion.get(a[0]);throw new AssertionError(m.getName());});
        final RequestDispatcher dispatcher=doble(RequestDispatcher.class,(p,m,a)->{if(m.getName().equals("forward")){reenviado=true;return null;}throw new AssertionError(m.getName());});
        final HttpServletRequest req=doble(HttpServletRequest.class,(p,m,a)->switch(m.getName()) {
            case "getSession" -> {if(a==null||a.length!=1||!Boolean.FALSE.equals(a[0]))throw new AssertionError("No crear sesión.");yield sesion.isEmpty()?null:httpSesion;}
            case "getParameter" -> parametros.get(a[0]);
            case "getServletContext" -> contexto;
            case "setAttribute" -> {atributos.put((String)a[0],a[1]);yield null;}
            case "getRequestDispatcher" -> dispatcher;
            case "getContextPath" -> "/pruebas";
            default -> throw new AssertionError(m.getName());
        });
        final ServletOutputStream salida=new ServletOutputStream(){public boolean isReady(){return true;}public void setWriteListener(WriteListener l){}public void write(int b){bytes.write(b);}};
        final HttpServletResponse resp=doble(HttpServletResponse.class,(p,m,a)->switch(m.getName()) {
            case "sendError" -> {estado=(Integer)a[0];yield null;}
            case "sendRedirect" -> {estado=302;redireccion=(String)a[0];yield null;}
            case "setHeader" -> {headers.put((String)a[0],(String)a[1]);yield null;}
            case "setContentType","setContentLength" -> null;
            case "getOutputStream" -> salida;
            default -> throw new AssertionError(m.getName());
        });
        Escenario(DtUsuario usuario) {
            if(usuario!=null)sesion.put("usuario",usuario);
            ediciones.put("Evento",List.of(edicion("Aceptada",EstadoEdicion.ACEPTADA),edicion("Ingresada",EstadoEdicion.INGRESADA),edicion("Rechazada",EstadoEdicion.RECHAZADA)));
            ediciones.put("Otro",List.of(edicion("OtraAceptada",EstadoEdicion.ACEPTADA),edicion("SinTipos",EstadoEdicion.ACEPTADA)));
        }
        DtEdicion edicion(String nombre,EstadoEdicion estado) {DtEdicion d=new DtEdicion(nombre,"ED",fecha,fecha.plusDays(2),LocalDate.of(2026,2,1),"Ciudad","País","duenio");d.setEstado(estado);return d;}
        DtTipoRegistro tipo(String edicion,String nombre) {return new DtTipoRegistro(nombre,"Descripción pública",edicion.equals("OtraAceptada")?200:100,50);}
    }
    static <T>T doble(Class<T> tipo,InvocationHandler h){return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(),new Class<?>[]{tipo},h));}
}
