package datatypes;
import java.util.List;
public record DtFormularioRegistro(String evento, List<DtEdicion> ediciones, List<DtTipoRegistro> tipos, DtEdicion edicion, DtTipoRegistro tipo, List<DtEvento> eventos) { }
