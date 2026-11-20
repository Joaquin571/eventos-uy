package central.soap;

import central.seguridad.SesionesRemotas;
import jakarta.annotation.Resource;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Adaptador SOAP de sesiones. Publicar únicamente en un contexto HTTPS. */
@WebService(name = "Sesiones", serviceName = "SesionesService", portName = "SesionesPort",
        targetNamespace = SesionesPublicador.NAMESPACE)
public class SesionesPublicador {
    static final String NAMESPACE = "https://eventos.uy/soap/sesiones";
    private final SesionesRemotas sesiones;

    @Resource
    private WebServiceContext contexto;

    /** Requerido por wsgen. La publicación real debe inyectar el registro compartido. */
    public SesionesPublicador() {
        throw new IllegalStateException("Publicar SesionesPublicador con el registro compartido.");
    }

    public SesionesPublicador(SesionesRemotas sesiones) {
        this.sesiones = Objects.requireNonNull(sesiones);
    }

    @WebMethod
    public UsuarioSesionSoap iniciarSesion(@WebParam(name = "identificador") String identificador,
                                          @WebParam(name = "contrasenia") String contrasenia)
            throws SesionSoapException {
        return iniciar(identificador, contrasenia, false);
    }

    @WebMethod
    public UsuarioSesionSoap iniciarSesionMovil(@WebParam(name = "identificador") String identificador,
                                               @WebParam(name = "contrasenia") String contrasenia)
            throws SesionSoapException {
        return iniciar(identificador, contrasenia, true);
    }

    private UsuarioSesionSoap iniciar(String identificador, String contrasenia, boolean movil)
            throws SesionSoapException {
        if (identificador == null || identificador.isBlank() || contrasenia == null) {
            throw new SesionSoapException("SOLICITUD_MALFORMADA", "Ingresá identificador y contraseña.");
        }
        SesionesRemotas.SesionEmitida emitida;
        try {
            emitida = movil ? sesiones.iniciarMovil(identificador, contrasenia)
                    : sesiones.iniciar(identificador, contrasenia);
        } catch (excepciones.AccesoDenegadoException e) {
            throw new SesionSoapException("ACCESO_DENEGADO", "El acceso movil requiere un asistente.");
        } catch (IllegalArgumentException e) {
            throw new SesionSoapException("CREDENCIALES_INVALIDAS",
                    "El nickname, el correo o la contraseña no son correctos.");
        } catch (IllegalStateException e) {
            throw new SesionSoapException("SERVICIO_NO_DISPONIBLE", "No se pudo completar el inicio de sesión.");
        }

        contexto.getMessageContext().put(MessageContext.HTTP_RESPONSE_HEADERS, Map.of(
                "X-EventosUy-Session", List.of(emitida.credencial()),
                "Cache-Control", List.of("no-store")));
        var rol = emitida.actor().esOrganizador() ? UsuarioSesionSoap.Rol.ORGANIZADOR : UsuarioSesionSoap.Rol.ASISTENTE;
        return new UsuarioSesionSoap(emitida.actor().nickname(), emitida.nombre(), rol);
    }

    @WebMethod
    public void cerrarSesion() throws SesionSoapException {
        String credencial = leerBearer(contexto.getMessageContext());
        sesiones.revocar(credencial);
    }

    private static String leerBearer(MessageContext mensaje) throws SesionSoapException {
        return CredencialesSoap.leerRequerida(mensaje);
    }
}
