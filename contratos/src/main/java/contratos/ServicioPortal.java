package contratos;
import contratos.dto.*;
import java.util.List;
/** API del consumidor Web, independiente de lógica y persistencia. */
public interface ServicioPortal {
    List<DtEvento> listarEventos();
    List<DtEvento> listarEventosPorCategoria(String categoria);
    List<DtCategoria> listarCategorias();
    List<DtInstitucion> listarInstituciones();
    DtInstitucion consultarInstitucion(String nombre);
    List<DtUsuario> listarUsuarios();
    DtUsuario usuarioActual();
    DtPerfilUsuario consultarPerfil(String nickname);
    boolean disponibleNickname(String valor);
    boolean disponibleCorreo(String valor);
    boolean altaInstitucion(DtInstitucion datos, DtImagen imagen);
    boolean altaEvento(DtEvento datos, DtImagen imagen);
    boolean altaEdicion(DtEdicion datos, String evento, DtImagen imagen);
    boolean altaTipoRegistro(DtTipoRegistro datos, String edicion);
    boolean altaPatrocinio(DtPatrocinio datos, String evento);
    DtOpcionesAltaPatrocinio obtenerOpcionesAltaPatrocinio(String evento, String edicion);
    DtConsultaPatrocinios consultarPatrocinios(String evento, String edicion, String institucion);
    DtSeleccionEdicion consultarEdiciones(String evento, String edicion);
    DtDetalleEdicion consultarDetalleEdicion(String edicion);
    DtTipoRegistro consultarTipoRegistro(String edicion, String tipo);
    DtConsultaRegistros consultarRegistros(String edicion, String asistente);
    DtFormularioRegistro formularioRegistro(String evento, String edicion, String tipo);
    boolean registrar(String edicion, String tipo, String modalidad, String codigo);
    DtImagen obtenerImagen(TipoEntidadImagen tipo, String clave);
    boolean altaUsuario(EntradaUsuario entrada);
    DtUsuario modificarUsuario(EntradaUsuario entrada);
}
