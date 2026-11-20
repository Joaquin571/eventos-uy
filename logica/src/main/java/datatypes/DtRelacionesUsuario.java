package datatypes;

import java.util.List;

/** Solo nicknames publicos; no expone datos privados de los perfiles relacionados. */
public record DtRelacionesUsuario(List<String> seguidores, List<String> seguidos, boolean siguiendo) {
    public DtRelacionesUsuario {
        seguidores = List.copyOf(seguidores);
        seguidos = List.copyOf(seguidos);
    }
}
