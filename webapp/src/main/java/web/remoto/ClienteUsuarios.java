package web.remoto;

import datatypes.*;
import interfaces.IUsuariosWeb;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import web.remoto.generado.UsuariosPort;
import web.remoto.generado.UsuariosService;
import web.remoto.generado.ErrorUsuarios;
import java.util.*;
import javax.xml.namespace.QName;

/** Cada invocación usa su propio puerto; los proxies JAX-WS no se comparten entre hilos. */
public final class ClienteUsuarios implements IUsuariosWeb {
    private final String direccion;
    private static final class Instancia {
        private static final ClienteUsuarios CLIENTE = new ClienteUsuarios();
    }
    private ClienteUsuarios() { direccion = ConfiguracionUsuariosCliente.direccion(); }
    public static ClienteUsuarios get() { return Instancia.CLIENTE; }
    public static String token(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        return sesion == null ? null : (String) sesion.getAttribute("tokenUsuarios");
    }
    private UsuariosPort puerto() {
        var wsdl = ClienteUsuarios.class.getResource("/wsdl/UsuariosService.wsdl");
        if (wsdl == null) throw new IllegalStateException("Falta el WSDL de usuarios. Compilá desde la raíz con Maven.");
        UsuariosPort puerto = new UsuariosService(wsdl, new QName("urn:eventosuy:usuarios:v1", "UsuariosService")).getUsuariosPort();
        Map<String, Object> contexto = ((BindingProvider) puerto).getRequestContext();
        contexto.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, direccion);
        contexto.put("com.sun.xml.ws.connect.timeout", 4000);
        contexto.put("com.sun.xml.ws.request.timeout", 12000);
        return puerto;
    }
    @FunctionalInterface
    private interface Operacion<T> { T ejecutar(UsuariosPort puerto) throws ErrorUsuarios; }
    private <T> T invocar(Operacion<T> operacion) {
        try { return operacion.ejecutar(puerto()); }
        catch (ErrorUsuarios e) {
            throw new ErrorRemotoUsuarios(e.getFaultInfo().getCodigo(), e.getFaultInfo().getMensaje());
        } catch (WebServiceException e) {
            throw new ErrorRemotoUsuarios("SERVIDOR", "El servidor central no está disponible. Intentá nuevamente más tarde.");
        }
    }
    @SuppressWarnings("unchecked")
    private <T> T convertir(Object valor, Class<T> tipo) { return (T) ConversorUsuarios.convertir(valor, tipo); }

    public DtSesion iniciarSesion(String identificador, String contrasenia, boolean movil) {
        return convertir(invocar(p -> p.iniciarSesion(identificador, contrasenia, movil)), DtSesion.class);
    }

    public void cerrarSesion(String token) {
        invocar(p -> { p.cerrarSesion(token); return null; });
    }

    public DtUsuario usuarioActual(String token) {
        return convertir(invocar(p -> p.usuarioActual(token)), DtUsuario.class);
    }

    public List<DtUsuario> listarUsuarios() {
        List<DtUsuario> resultado = new ArrayList<>();
        for (var item : invocar(p -> p.listarUsuarios())) resultado.add(convertir(item, DtUsuario.class));
        return resultado;
    }

    public DtPerfilUsuario consultarPerfil(String token, String nickname) {
        return convertir(invocar(p -> p.consultarPerfil(token, nickname)), DtPerfilUsuario.class);
    }

    public boolean disponibleNickname(String valor) {
        return invocar(p -> p.disponibleNickname(valor));
    }

    public boolean disponibleCorreo(String valor) {
        return invocar(p -> p.disponibleCorreo(valor));
    }

    public void altaUsuario(DtUsuario usuario, DtImagen imagen) {
        invocar(p -> { p.altaUsuario(convertir(usuario, web.remoto.generado.DtUsuario.class), convertir(imagen, web.remoto.generado.DtImagen.class)); return null; });
    }

    public DtUsuario modificarUsuario(String token, DtUsuario usuario, DtImagen imagen) {
        return convertir(invocar(p -> p.modificarUsuario(token, convertir(usuario, web.remoto.generado.DtUsuario.class), convertir(imagen, web.remoto.generado.DtImagen.class))), DtUsuario.class);
    }

    public List<DtInstitucion> listarInstituciones() {
        List<DtInstitucion> resultado = new ArrayList<>();
        for (var item : invocar(p -> p.listarInstituciones())) resultado.add(convertir(item, DtInstitucion.class));
        return resultado;
    }

    public DtInstitucion consultarInstitucion(String nombre) {
        return convertir(invocar(p -> p.consultarInstitucion(nombre)), DtInstitucion.class);
    }

    public void altaInstitucion(String token, DtInstitucion institucion, DtImagen imagen) {
        invocar(p -> { p.altaInstitucion(token, convertir(institucion, web.remoto.generado.DtInstitucion.class), convertir(imagen, web.remoto.generado.DtImagen.class)); return null; });
    }

    public DtFormularioPatrocinio formularioPatrocinio(String token, String evento, String edicion) {
        return convertir(invocar(p -> p.formularioPatrocinio(token, evento, edicion)), DtFormularioPatrocinio.class);
    }

    public void altaPatrocinio(String token, String evento, DtPatrocinio patrocinio) {
        invocar(p -> { p.altaPatrocinio(token, evento, convertir(patrocinio, web.remoto.generado.DtPatrocinio.class)); return null; });
    }

    public DtConsultaPatrocinio consultarPatrocinio(String token, String evento, String edicion, String institucion) {
        return convertir(invocar(p -> p.consultarPatrocinio(token, evento, edicion, institucion)), DtConsultaPatrocinio.class);
    }

    public DtImagen imagen(String tipo, String clave) {
        return convertir(invocar(p -> p.imagen(tipo, clave)), DtImagen.class);
    }

    public List<DtCategoria> listarCategorias() {
        List<DtCategoria> resultado = new ArrayList<>();
        for (var item : invocar(p -> p.listarCategorias())) resultado.add(convertir(item, DtCategoria.class));
        return resultado;
    }
}