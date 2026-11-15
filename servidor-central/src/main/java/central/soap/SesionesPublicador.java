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
import java.util.regex.Pattern;

/** Adaptador SOAP de sesiones. Publicar únicamente en un contexto HTTPS. */
@WebService(name = "Sesiones", serviceName = "SesionesService", portName = "SesionesPort",
        targetNamespace = SesionesPublicador.NAMESPACE)
public class SesionesPublicador {
    static final String NAMESPACE = "https://eventos.uy/soap/sesiones";
    private static final Pattern BEARER = Pattern.compile("Bearer +([A-Za-z0-9_-]{43})",
            Pattern.CASE_INSENSITIVE);
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
        if (identificador == null || identificador.isBlank() || contrasenia == null) {
            throw new SesionSoapException("SOLICITUD_MALFORMADA", "Ingresá identificador y contraseña.");
        }
        SesionesRemotas.SesionEmitida emitida;
        try {
            emitida = sesiones.iniciar(identificador, contrasenia);
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
        Object propiedad = mensaje.get(MessageContext.HTTP_REQUEST_HEADERS);
        String valor = null;
        if (propiedad instanceof Map<?, ?> encabezados) {
            for (var entrada : encabezados.entrySet()) {
                if (!(entrada.getKey() instanceof String nombre) || !"Authorization".equalsIgnoreCase(nombre)) {
                    continue;
                }
                if (valor != null || !(entrada.getValue() instanceof List<?> valores) || valores.size() != 1
                        || !(valores.getFirst() instanceof String texto)) {
                    throw encabezadoInvalido();
                }
                valor = texto;
            }
        }
        if (valor == null) throw encabezadoInvalido();
        var coincidencia = BEARER.matcher(valor);
        if (!coincidencia.matches()) throw encabezadoInvalido();
        return coincidencia.group(1);
    }

    private static SesionSoapException encabezadoInvalido() {
        return new SesionSoapException("SOLICITUD_MALFORMADA", "Se requiere un encabezado Authorization Bearer válido.");
    }
}
