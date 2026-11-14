package soap;

import datatypes.*;
import implementacion.Fabrica;
import interfaces.IUsuariosWeb;
import usuarios.ErrorAccesoUsuarios;
import jakarta.jws.*;
import java.util.List;

@WebService(name = "UsuariosPort", serviceName = "UsuariosService", portName = "UsuariosPort",
        targetNamespace = "urn:eventosuy:usuarios:v1")
public class UsuariosEndpoint {
    private final IUsuariosWeb controlador = Fabrica.getInstance().getIUsuariosWeb();
    @FunctionalInterface
    private interface Operacion<T> { T ejecutar(); }

    private <T> T invocar(Operacion<T> operacion) throws ErrorUsuarios {
        try { return operacion.ejecutar(); }
        catch (ErrorAccesoUsuarios e) { throw fallo(e.getCodigo(), e.getMessage()); }
        catch (IllegalArgumentException e) { throw fallo("DATOS", e.getMessage()); }
        catch (RuntimeException e) {
            System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Error en usuarios SOAP", e);
            throw fallo("SERVIDOR", "No se pudo completar la operación. Intentá nuevamente más tarde.");
        }
    }
    private ErrorUsuarios fallo(String codigo, String mensaje) {
        return new ErrorUsuarios(mensaje, new FalloUsuarios(codigo, mensaje));
    }

    @WebMethod
    public DtSesion iniciarSesion(@WebParam(name = "identificador") String identificador, @WebParam(name = "contrasenia") String contrasenia, @WebParam(name = "movil") boolean movil) throws ErrorUsuarios {
        return invocar(() -> controlador.iniciarSesion(identificador, contrasenia, movil));
    }

    @WebMethod
    public void cerrarSesion(@WebParam(name = "token") String token) throws ErrorUsuarios {
        invocar(() -> { controlador.cerrarSesion(token); return null; });
    }

    @WebMethod
    public DtUsuario usuarioActual(@WebParam(name = "token") String token) throws ErrorUsuarios {
        return invocar(() -> controlador.usuarioActual(token));
    }

    @WebMethod
    public List<DtUsuario> listarUsuarios() throws ErrorUsuarios {
        return invocar(() -> controlador.listarUsuarios());
    }

    @WebMethod
    public DtPerfilUsuario consultarPerfil(@WebParam(name = "token") String token, @WebParam(name = "nickname") String nickname) throws ErrorUsuarios {
        return invocar(() -> controlador.consultarPerfil(token, nickname));
    }

    @WebMethod
    public boolean disponibleNickname(@WebParam(name = "valor") String valor) throws ErrorUsuarios {
        return invocar(() -> controlador.disponibleNickname(valor));
    }

    @WebMethod
    public boolean disponibleCorreo(@WebParam(name = "valor") String valor) throws ErrorUsuarios {
        return invocar(() -> controlador.disponibleCorreo(valor));
    }

    @WebMethod
    public void altaUsuario(@WebParam(name = "usuario") DtUsuario usuario, @WebParam(name = "imagen") DtImagen imagen) throws ErrorUsuarios {
        invocar(() -> { controlador.altaUsuario(usuario, imagen); return null; });
    }

    @WebMethod
    public DtUsuario modificarUsuario(@WebParam(name = "token") String token, @WebParam(name = "usuario") DtUsuario usuario, @WebParam(name = "imagen") DtImagen imagen) throws ErrorUsuarios {
        return invocar(() -> controlador.modificarUsuario(token, usuario, imagen));
    }

    @WebMethod
    public List<DtInstitucion> listarInstituciones() throws ErrorUsuarios {
        return invocar(() -> controlador.listarInstituciones());
    }

    @WebMethod
    public DtInstitucion consultarInstitucion(@WebParam(name = "nombre") String nombre) throws ErrorUsuarios {
        return invocar(() -> controlador.consultarInstitucion(nombre));
    }

    @WebMethod
    public void altaInstitucion(@WebParam(name = "token") String token, @WebParam(name = "institucion") DtInstitucion institucion, @WebParam(name = "imagen") DtImagen imagen) throws ErrorUsuarios {
        invocar(() -> { controlador.altaInstitucion(token, institucion, imagen); return null; });
    }

    @WebMethod
    public DtFormularioPatrocinio formularioPatrocinio(@WebParam(name = "token") String token, @WebParam(name = "evento") String evento, @WebParam(name = "edicion") String edicion) throws ErrorUsuarios {
        return invocar(() -> controlador.formularioPatrocinio(token, evento, edicion));
    }

    @WebMethod
    public void altaPatrocinio(@WebParam(name = "token") String token, @WebParam(name = "evento") String evento, @WebParam(name = "patrocinio") DtPatrocinio patrocinio) throws ErrorUsuarios {
        invocar(() -> { controlador.altaPatrocinio(token, evento, patrocinio); return null; });
    }

    @WebMethod
    public DtConsultaPatrocinio consultarPatrocinio(@WebParam(name = "token") String token, @WebParam(name = "evento") String evento, @WebParam(name = "edicion") String edicion, @WebParam(name = "institucion") String institucion) throws ErrorUsuarios {
        return invocar(() -> controlador.consultarPatrocinio(token, evento, edicion, institucion));
    }

    @WebMethod
    public DtImagen imagen(@WebParam(name = "tipo") String tipo, @WebParam(name = "clave") String clave) throws ErrorUsuarios {
        return invocar(() -> controlador.imagen(tipo, clave));
    }

    @WebMethod
    public List<DtCategoria> listarCategorias() throws ErrorUsuarios {
        return invocar(() -> controlador.listarCategorias());
    }
}