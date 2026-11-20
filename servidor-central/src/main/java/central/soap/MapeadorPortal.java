package central.soap;

import contratos.dto.*;

/** Entrada SOAP a DTO de lógica; las validaciones funcionales siguen en Sistema. */
final class MapeadorPortal {
    private MapeadorPortal() { }
    static datatypes.DtImagen imagen(DtImagen d) {
        return d==null?null:new datatypes.DtImagen(d.getTipoContenido(),d.getContenido()==null?null:d.getContenido().clone());
    }
    static datatypes.DtUsuario usuario(EntradaUsuario entrada) {
        if(entrada==null || entrada.getUsuario()==null) throw new IllegalArgumentException("Los datos del usuario son obligatorios.");
        DtUsuario d=entrada.getUsuario();datatypes.DtUsuario resultado;
        if(d instanceof DtAsistente a) resultado=new datatypes.DtAsistente(a.getNickname(),a.getNombre(),a.getCorreoElectronico(),a.getApellido(),a.getFechaNacimiento(),a.getNombreInstitucion());
        else if(d instanceof DtOrganizador o) resultado=new datatypes.DtOrganizador(o.getNickname(),o.getNombre(),o.getCorreoElectronico(),o.getDescripcion(),o.getSitioWeb());
        else throw new IllegalArgumentException("Seleccioná un tipo de usuario válido.");
        resultado.setContrasenia(entrada.getContrasenia());resultado.setConfirmacionContrasenia(entrada.getConfirmacion());
        return resultado;
    }
    static datatypes.DtInstitucion institucion(DtInstitucion d) {
        return d==null?null:new datatypes.DtInstitucion(d.getNombre(),d.getDescripcion(),d.getSitioWeb());
    }
    static datatypes.DtEvento evento(DtEvento d) {
        return d==null?null:new datatypes.DtEvento(d.getNombre(),d.getSigla(),d.getDescripcion(),d.getFechaAlta(),d.getCategorias());
    }
    static datatypes.DtEdicion edicion(DtEdicion d) {
        return d==null?null:new datatypes.DtEdicion(d.getIdNombre(),d.getSigla(),d.getFechaInicio(),d.getFechaFin(),d.getFechaAlta(),d.getCiudad(),d.getPais(),d.getNombreOrganizador());
    }
    static datatypes.DtTipoRegistro tipo(DtTipoRegistro d) {
        return d==null?null:new datatypes.DtTipoRegistro(d.getIdNombre(),d.getDescripcion(),d.getCosto(),d.getCupo());
    }
    static datatypes.DtPatrocinio patrocinio(DtPatrocinio d) {
        return d==null?null:new datatypes.DtPatrocinio(d.getFecha(),d.getMontoAporte(),d.getCantRegistrosGrat(),d.getCodigoPatrocinio(),
                d.getNivel()==null?null:clases.NivelPatrocinio.valueOf(d.getNivel().name()),d.getNombreInstituto(),d.getNombreEdicion(),d.getNombreTipoRegistro());
    }
}
