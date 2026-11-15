package datatypes;
import java.util.List;
public record DtSeleccionEdicion(String evento, DtEdicion edicion, List<DtEdicion> ediciones, List<DtEvento> eventos) { }
