package datatypes;

import java.util.Collection;
import java.util.List;

/** Datos autorizados del formulario; no contiene entidades ni códigos de patrocinios. */
public record DtOpcionesAltaPatrocinio(String evento, Collection<DtEvento> eventos,
                                     Collection<DtEdicion> ediciones, Collection<DtTipoRegistro> tipos,
                                     Collection<DtInstitucion> instituciones) {
    public DtOpcionesAltaPatrocinio {
        eventos = List.copyOf(eventos);
        ediciones = List.copyOf(ediciones);
        tipos = List.copyOf(tipos);
        instituciones = List.copyOf(instituciones);
    }
}
