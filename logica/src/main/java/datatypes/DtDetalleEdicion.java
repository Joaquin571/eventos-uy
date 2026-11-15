package datatypes;
import java.util.List;
public record DtDetalleEdicion(DtEdicion edicion, DtUsuario organizador, boolean tieneImagen, List<DtTipoRegistro> tipos, List<String> patrocinadoras, List<DtRegistro> registros, DtRegistro registroPropio, boolean propietario, boolean puedeRegistrarse) { }
