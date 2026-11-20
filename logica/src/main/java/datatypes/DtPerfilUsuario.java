package datatypes;
import java.util.List;
public record DtPerfilUsuario(DtUsuario usuario, boolean propio, List<DtRegistro> registros, List<DtEdicion> ediciones) { }
