package datatypes;
import java.util.List;
public record DtConsultaRegistros(boolean organizador, List<DtEdicion> ediciones, List<DtRegistro> registros, DtRegistro detalle, List<DtUsuario> asistentes) { }
