package datatypes;

import java.util.Collection;
import java.util.List;

/** Selección y datos ya autorizados para consultar patrocinios, sin entidades JPA. */
public record DtConsultaPatrocinios(String evento, Collection<DtEvento> eventos,
                                   Collection<DtEdicion> ediciones, Collection<String> patrocinadoras,
                                   DtPatrocinio patrocinio, boolean puedeVerCodigo) {
    public DtConsultaPatrocinios {
        eventos = List.copyOf(eventos);
        ediciones = List.copyOf(ediciones);
        patrocinadoras = List.copyOf(patrocinadoras);
    }
}
