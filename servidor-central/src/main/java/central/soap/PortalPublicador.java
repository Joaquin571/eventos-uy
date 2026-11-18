package central.soap;
import central.seguridad.SesionesRemotas;
import contratos.ConversorDatos;
import contratos.dto.*;
import datatypes.Actor;
import interfaces.ISistema;
import jakarta.annotation.Resource;
import jakarta.jws.*;
import jakarta.xml.ws.WebServiceContext;
import java.util.*;

/** Fachada de transporte. Sistema conserva autorización, proyecciones y transacciones. */
@WebService(name="Portal",serviceName="PortalService",portName="PortalPort",targetNamespace="https://eventos.uy/soap/portal")
public class PortalPublicador {
    private final ISistema sistema;
    private final SesionesRemotas sesiones;
    @Resource private WebServiceContext contexto;
    public PortalPublicador() { throw new IllegalStateException("Inyectar sistema y registro P03 compartido."); }
    public PortalPublicador(ISistema sistema,SesionesRemotas sesiones) {
        this.sistema=Objects.requireNonNull(sistema);this.sesiones=Objects.requireNonNull(sesiones);
    }
    @FunctionalInterface private interface Accion<T> { T ejecutar() throws Exception; }
    private <T> T invocar(Accion<T> accion) throws OperacionSoapException {
        try { return accion.ejecutar(); }
        catch(OperacionSoapException e) { throw e; }
        catch(excepciones.NoEncontradoException e) { throw new OperacionSoapException("NO_ENCONTRADO",e.getMessage()); }
        catch(excepciones.AccesoDenegadoException e) { throw new OperacionSoapException("ACCESO_DENEGADO",e.getMessage()); }
        catch(IllegalArgumentException e) { throw new OperacionSoapException("DATOS_INVALIDOS",e.getMessage()); }
        catch(Exception e) {
            if(e.getClass()==Exception.class) throw new OperacionSoapException("DATOS_INVALIDOS",e.getMessage());
            System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR,"Fallo técnico del servicio Portal.");
            throw new OperacionSoapException("SERVICIO_NO_DISPONIBLE","No se pudo completar la operación.");
        }
    }
    private Actor actor(boolean requerido) throws OperacionSoapException {
        try {
            String credencial=CredencialesSoap.leerOpcional(contexto.getMessageContext());
            if(credencial==null && !requerido) return null;
            return sesiones.requerirActor(credencial);
        } catch(SesionSoapException | IllegalArgumentException e) {
            throw new OperacionSoapException("SESION_INVALIDA","La sesión remota no es válida.");
        }
    }
    private <T> T salida(Object origen,Class<T> tipo) { return ConversorDatos.convertir(origen,tipo); }
    private <T> List<T> lista(Collection<?> origen,Class<T> tipo) {
        return origen.stream().map(o->salida(o,tipo)).toList();
    }
    private boolean altaUsuarioInterna(EntradaUsuario entrada) {
        datatypes.DtUsuario usuario=MapeadorPortal.usuario(entrada);
        datatypes.DtImagen imagen=MapeadorPortal.imagen(entrada.getImagen());
        if(usuario instanceof datatypes.DtAsistente a) return sistema.altaAsistente(a,imagen);
        return sistema.altaOrganizador((datatypes.DtOrganizador)usuario,imagen);
    }
    private DtUsuario modificarUsuarioInterno(EntradaUsuario entrada) throws OperacionSoapException {
        Actor actor=actor(true);
        datatypes.DtUsuario usuario=MapeadorPortal.usuario(entrada);
        datatypes.DtImagen imagen=MapeadorPortal.imagen(entrada.getImagen());
        if(usuario instanceof datatypes.DtAsistente a) sistema.modificarAsistente(a,imagen,actor);
        else sistema.modificarOrganizador((datatypes.DtOrganizador)usuario,imagen,actor);
        return salida(sistema.consultarUsuario(usuario.getNickname()),DtUsuario.class);
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.ListarEventosPorCategoria")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.ListarEventosPorCategoriaResponse")
    public List<DtEvento> listarEventosPorCategoria(@WebParam(name="categoria") String categoria) throws OperacionSoapException {
        return invocar(() -> lista(sistema.listarEventosPorCategoria(categoria), DtEvento.class));
    }

    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.RegistrarVisitaEvento")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.RegistrarVisitaEventoResponse")
    public void registrarVisitaEvento(@WebParam(name="evento") String evento) throws OperacionSoapException {
        invocar(() -> { sistema.registrarVisitaEvento(evento, actor(false)); return null; });
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.ListarEventos")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.ListarEventosResponse")
    public List<DtEvento> listarEventos() throws OperacionSoapException {
        return invocar(() -> lista(sistema.listarEventosPublicos(), DtEvento.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.ListarCategorias")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.ListarCategoriasResponse")
    public List<DtCategoria> listarCategorias() throws OperacionSoapException {
        return invocar(() -> lista(sistema.listarCategorias(), DtCategoria.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.ListarInstituciones")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.ListarInstitucionesResponse")
    public List<DtInstitucion> listarInstituciones() throws OperacionSoapException {
        return invocar(() -> lista(sistema.listarInstituciones(), DtInstitucion.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.ConsultarInstitucion")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.ConsultarInstitucionResponse")
    public DtInstitucion consultarInstitucion(@WebParam(name="nombre") String nombre) throws OperacionSoapException {
        return invocar(() -> salida(sistema.consultarInstitucion(nombre), DtInstitucion.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.ListarUsuarios")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.ListarUsuariosResponse")
    public List<DtUsuario> listarUsuarios() throws OperacionSoapException {
        return invocar(() -> lista(sistema.listarUsuarios(), DtUsuario.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.UsuarioActual")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.UsuarioActualResponse")
    public DtUsuario usuarioActual() throws OperacionSoapException {
        return invocar(() -> salida(sistema.consultarUsuario(actor(true).nickname()), DtUsuario.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.ConsultarPerfil")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.ConsultarPerfilResponse")
    public DtPerfilUsuario consultarPerfil(@WebParam(name="nickname") String nickname) throws OperacionSoapException {
        return invocar(() -> salida(sistema.consultarPerfil(nickname, actor(false)), DtPerfilUsuario.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.DisponibleNickname")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.DisponibleNicknameResponse")
    public boolean disponibleNickname(@WebParam(name="valor") String valor) throws OperacionSoapException {
        return invocar(() -> sistema.disponibleNickname(valor));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.DisponibleCorreo")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.DisponibleCorreoResponse")
    public boolean disponibleCorreo(@WebParam(name="valor") String valor) throws OperacionSoapException {
        return invocar(() -> sistema.disponibleCorreo(valor));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.AltaInstitucion")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.AltaInstitucionResponse")
    public boolean altaInstitucion(@WebParam(name="datos") DtInstitucion datos, @WebParam(name="imagen") DtImagen imagen) throws OperacionSoapException {
        return invocar(() -> sistema.altaInstitucion(MapeadorPortal.institucion(datos), MapeadorPortal.imagen(imagen), actor(true)));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.AltaEvento")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.AltaEventoResponse")
    public boolean altaEvento(@WebParam(name="datos") DtEvento datos, @WebParam(name="imagen") DtImagen imagen) throws OperacionSoapException {
        return invocar(() -> sistema.altaEvento(MapeadorPortal.evento(datos), MapeadorPortal.imagen(imagen), actor(true)));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.AltaEdicion")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.AltaEdicionResponse")
    public boolean altaEdicion(@WebParam(name="datos") DtEdicion datos, @WebParam(name="evento") String evento, @WebParam(name="imagen") DtImagen imagen) throws OperacionSoapException {
        return invocar(() -> sistema.altaEdicion(MapeadorPortal.edicion(datos), evento, MapeadorPortal.imagen(imagen), actor(true)));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.BuscarEventosYEdiciones")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.BuscarEventosYEdicionesResponse")
    public List<DtResultadoBusqueda> buscarEventosYEdiciones(@WebParam(name="texto") String texto,
            @WebParam(name="orden") OrdenBusqueda orden) throws OperacionSoapException {
        return invocar(() -> lista(sistema.buscarEventosYEdiciones(texto,
                orden == null ? null : datatypes.OrdenBusqueda.valueOf(orden.name())), DtResultadoBusqueda.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.FinalizarEvento")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.FinalizarEventoResponse")
    public void finalizarEvento(@WebParam(name="evento") String evento) throws OperacionSoapException {
        invocar(() -> { sistema.finalizarEvento(evento, actor(true)); return true; });
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.AltaTipoRegistro")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.AltaTipoRegistroResponse")
    public boolean altaTipoRegistro(@WebParam(name="datos") DtTipoRegistro datos, @WebParam(name="edicion") String edicion) throws OperacionSoapException {
        return invocar(() -> sistema.altaTipoRegistro(MapeadorPortal.tipo(datos), edicion, actor(true)));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.AltaPatrocinio")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.AltaPatrocinioResponse")
    public boolean altaPatrocinio(@WebParam(name="datos") DtPatrocinio datos, @WebParam(name="evento") String evento) throws OperacionSoapException {
        return invocar(() -> sistema.altaPatrocinio(MapeadorPortal.patrocinio(datos), evento, actor(true)));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.ObtenerOpcionesAltaPatrocinio")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.ObtenerOpcionesAltaPatrocinioResponse")
    public DtOpcionesAltaPatrocinio obtenerOpcionesAltaPatrocinio(@WebParam(name="evento") String evento, @WebParam(name="edicion") String edicion) throws OperacionSoapException {
        return invocar(() -> salida(sistema.obtenerOpcionesAltaPatrocinio(evento, edicion, actor(true)), DtOpcionesAltaPatrocinio.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.ConsultarPatrocinios")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.ConsultarPatrociniosResponse")
    public DtConsultaPatrocinios consultarPatrocinios(@WebParam(name="evento") String evento, @WebParam(name="edicion") String edicion, @WebParam(name="institucion") String institucion) throws OperacionSoapException {
        return invocar(() -> salida(sistema.consultarPatrocinios(evento, edicion, institucion, actor(false)), DtConsultaPatrocinios.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.ConsultarEdiciones")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.ConsultarEdicionesResponse")
    public DtSeleccionEdicion consultarEdiciones(@WebParam(name="evento") String evento, @WebParam(name="edicion") String edicion) throws OperacionSoapException {
        return invocar(() -> salida(sistema.consultarEdiciones(evento, edicion, actor(false)), DtSeleccionEdicion.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.ConsultarDetalleEdicion")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.ConsultarDetalleEdicionResponse")
    public DtDetalleEdicion consultarDetalleEdicion(@WebParam(name="edicion") String edicion) throws OperacionSoapException {
        return invocar(() -> salida(sistema.consultarDetalleEdicion(edicion, actor(false)), DtDetalleEdicion.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.ConsultarTipoRegistro")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.ConsultarTipoRegistroResponse")
    public DtTipoRegistro consultarTipoRegistro(@WebParam(name="edicion") String edicion, @WebParam(name="tipo") String tipo) throws OperacionSoapException {
        return invocar(() -> salida(sistema.consultarTipoRegistroVisible(edicion, tipo, actor(false)), DtTipoRegistro.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.ConsultarRegistros")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.ConsultarRegistrosResponse")
    public DtConsultaRegistros consultarRegistros(@WebParam(name="edicion") String edicion, @WebParam(name="asistente") String asistente) throws OperacionSoapException {
        return invocar(() -> salida(sistema.consultarRegistros(edicion, asistente, actor(true)), DtConsultaRegistros.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.FormularioRegistro")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.FormularioRegistroResponse")
    public DtFormularioRegistro formularioRegistro(@WebParam(name="evento") String evento, @WebParam(name="edicion") String edicion, @WebParam(name="tipo") String tipo) throws OperacionSoapException {
        return invocar(() -> salida(sistema.formularioRegistro(evento, edicion, tipo, actor(true)), DtFormularioRegistro.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.Registrar")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.RegistrarResponse")
    public boolean registrar(@WebParam(name="edicion") String edicion, @WebParam(name="tipo") String tipo, @WebParam(name="modalidad") String modalidad, @WebParam(name="codigo") String codigo) throws OperacionSoapException {
        return invocar(() -> sistema.registrarAutorizado(edicion, tipo, modalidad, codigo, actor(true)));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.ObtenerImagen")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.ObtenerImagenResponse")
    public DtImagen obtenerImagen(@WebParam(name="tipo") TipoEntidadImagen tipo, @WebParam(name="clave") String clave) throws OperacionSoapException {
        return invocar(() -> salida(sistema.obtenerImagenVisible(tipo == null ? null : datatypes.TipoEntidadImagen.valueOf(tipo.name()), clave, actor(false)), DtImagen.class));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.AltaUsuario")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.AltaUsuarioResponse")
    public boolean altaUsuario(@WebParam(name="entrada") EntradaUsuario entrada) throws OperacionSoapException {
        return invocar(() -> altaUsuarioInterna(entrada));
    }
    @WebMethod
    @jakarta.xml.ws.RequestWrapper(className="central.soap.portal.jaxws.ModificarUsuario")
    @jakarta.xml.ws.ResponseWrapper(className="central.soap.portal.jaxws.ModificarUsuarioResponse")
    public DtUsuario modificarUsuario(@WebParam(name="entrada") EntradaUsuario entrada) throws OperacionSoapException {
        return invocar(() -> modificarUsuarioInterno(entrada));
    }
}
