package web;

import contratos.ServicioPortal;
import contratos.dto.*;
import datatypes.Actor;
import java.lang.reflect.*;
import java.util.*;

/** Doble remoto para conservar los escenarios HTTP anteriores. Nunca entra en el WAR.
 * Sus proyecciones son datos de fixture, no sustituyen las pruebas reales de Sistema y HTTPS. */
final class PortalDoble {
    @FunctionalInterface interface Accion { void ejecutar() throws Exception; }
    static void http(Accion accion,jakarta.servlet.http.HttpServletRequest req,jakarta.servlet.http.HttpServletResponse resp)throws Exception {
        try{accion.ejecutar();}catch(web.remoto.ErrorCentral e){FiltroAcceso.responderError(req,resp,e);}
    }
    interface Datos {
        List<DtEvento> listarEventos();
        List<DtEdicion> obtenerEdicionesEvento(String evento);
        DtUsuario consultarUsuario(String nick);
        DtImagen obtenerImagen(TipoEntidadImagen tipo,String clave);
        List<DtTipoRegistro> obtenerTiposRegistroEdicion(String edicion);
        DtTipoRegistro consultarTipoRegistro(String edicion,String tipo);
        List<String> obtenerInstitucionesPatrocinadoras(String edicion,Actor actor);
        List<DtRegistro> obtenerRegistrosEdicion(String edicion);
        List<DtRegistro> obtenerRegistrosAsistente(String nick);
        DtOpcionesAltaPatrocinio obtenerOpcionesAltaPatrocinio(String evento,String edicion,Actor actor);
        DtConsultaPatrocinios consultarPatrocinios(String evento,String edicion,String institucion,Actor actor);
        boolean altaPatrocinio(DtPatrocinio datos,String evento,Actor actor);
    }
    static ServicioPortal crear(Datos datos,Map<String,Object> sesion) {
        return (ServicioPortal)Proxy.newProxyInstance(ServicioPortal.class.getClassLoader(),new Class<?>[]{ServicioPortal.class},(p,m,a)->{
            DtUsuario usuario=(DtUsuario)sesion.get("usuario");
            Actor actor=usuario==null?null:Actor.usuarioAutenticado(usuario instanceof DtOrganizador
                    ?new datatypes.DtOrganizador(usuario.getNickname(),"","","","")
                    :new datatypes.DtAsistente(usuario.getNickname(),"","","",null,null));
            return switch(m.getName()) {
                case "consultarRelaciones" -> new DtRelacionesUsuario();
                case "listarEventos" -> datos.listarEventos();
                case "consultarEdiciones" -> seleccionar(datos,(String)a[0],(String)a[1],usuario);
                case "consultarDetalleEdicion" -> {
                    var edicion=seleccionar(datos,"",(String)a[0],usuario).getEdicion();
                    var resultado=new DtDetalleEdicion();resultado.setEdicion(edicion);
                    resultado.setOrganizador(datos.consultarUsuario(edicion.getNombreOrganizador()));
                    resultado.setTieneImagen(datos.obtenerImagen(TipoEntidadImagen.EDICION,edicion.getIdNombre())!=null);
                    resultado.setTipos(datos.obtenerTiposRegistroEdicion(edicion.getIdNombre()));
                    resultado.setPatrocinadoras(datos.obtenerInstitucionesPatrocinadoras(edicion.getIdNombre(),actor));
                    boolean propio=propio(usuario,edicion);resultado.setPropietario(propio);
                    if(propio)resultado.setRegistros(datos.obtenerRegistrosEdicion(edicion.getIdNombre()));
                    else if(usuario instanceof DtAsistente){
                        var registro=datos.obtenerRegistrosAsistente(usuario.getNickname()).stream().filter(r->r.getNombreEdicion().equals(edicion.getIdNombre())).findFirst().orElse(null);
                        resultado.setRegistroPropio(registro);resultado.setPuedeRegistrarse(registro==null&&edicion.getEstado()==EstadoEdicion.ACEPTADA);
                    }
                    yield resultado;
                }
                case "consultarTipoRegistro" -> {
                    if(a[0]==null || ((String)a[0]).isBlank())throw new web.remoto.ErrorCentral("NO_ENCONTRADO","No encontrado");
                    seleccionar(datos,"",(String)a[0],usuario);
                    var tipo=datos.consultarTipoRegistro((String)a[0],(String)a[1]);
                    if(tipo==null||!tipo.getIdNombre().equals(a[1]))throw new web.remoto.ErrorCentral("NO_ENCONTRADO","No encontrado");
                    yield tipo;
                }
                case "obtenerImagen" -> {
                    if(a[0]==TipoEntidadImagen.EDICION&&(a[1]==null||((String)a[1]).isBlank()))throw new web.remoto.ErrorCentral("NO_ENCONTRADO","No encontrado");
                    if(a[0]==TipoEntidadImagen.EDICION)seleccionar(datos,"",(String)a[1],usuario);
                    yield datos.obtenerImagen((TipoEntidadImagen)a[0],(String)a[1]);
                }
                case "obtenerOpcionesAltaPatrocinio" -> {
                    if(actor==null||!actor.esOrganizador())throw new web.remoto.ErrorCentral("ACCESO_DENEGADO","Se requiere organizador");
                    try{yield datos.obtenerOpcionesAltaPatrocinio((String)a[0],(String)a[1],actor);}
                    catch(excepciones.AccesoDenegadoException e){throw new web.remoto.ErrorCentral("ACCESO_DENEGADO",e.getMessage());}
                }
                case "altaPatrocinio" -> {
                    if(actor==null||!actor.esOrganizador())throw new web.remoto.ErrorCentral("ACCESO_DENEGADO","Se requiere organizador");
                    try{yield datos.altaPatrocinio((DtPatrocinio)a[0],(String)a[1],actor);}
                    catch(excepciones.AccesoDenegadoException e){throw new web.remoto.ErrorCentral("ACCESO_DENEGADO",e.getMessage());}
                }
                case "consultarPatrocinios" -> datos.consultarPatrocinios((String)a[0],(String)a[1],(String)a[2],actor);
                default -> throw new AssertionError("Operación remota inesperada: "+m.getName());
            };
        });
    }
    private static boolean propio(DtUsuario u,DtEdicion e){return u instanceof DtOrganizador&&u.getNickname().equals(e.getNombreOrganizador());}
    static DtOpcionesAltaPatrocinio opciones(String evento,Collection<DtEvento> eventos,Collection<DtEdicion> ediciones,Collection<DtTipoRegistro> tipos,Collection<DtInstitucion> instituciones){
        var r=new DtOpcionesAltaPatrocinio();r.setEvento(evento);r.setEventos(new ArrayList<>(eventos));r.setEdiciones(new ArrayList<>(ediciones));r.setTipos(new ArrayList<>(tipos));r.setInstituciones(new ArrayList<>(instituciones));return r;
    }
    static DtConsultaPatrocinios consulta(String evento,Collection<DtEvento> eventos,Collection<DtEdicion> ediciones,Collection<String> patrocinadoras,DtPatrocinio patrocinio,boolean codigo){
        var r=new DtConsultaPatrocinios();r.setEvento(evento);r.setEventos(new ArrayList<>(eventos));r.setEdiciones(new ArrayList<>(ediciones));r.setPatrocinadoras(new ArrayList<>(patrocinadoras));r.setPatrocinio(patrocinio);r.setPuedeVerCodigo(codigo);return r;
    }
    private static DtSeleccionEdicion seleccionar(Datos d,String evento,String nombre,DtUsuario usuario){
        var resultado=new DtSeleccionEdicion();var eventos=d.listarEventos();resultado.setEventos(eventos);
        DtEdicion elegida=null;
        if(evento==null)evento="";if(nombre==null)nombre="";
        if(!evento.isEmpty()&&eventos.stream().noneMatch(e->e.getNombre().equals(resultado.getEvento()))) {
            boolean existe=false;for(var e:eventos)if(e.getNombre().equals(evento))existe=true;
            if(!existe)throw new web.remoto.ErrorCentral("NO_ENCONTRADO","No encontrado");
        }
        if(!nombre.isEmpty()){
            for(var e:eventos)if(evento.isEmpty()||evento.equals(e.getNombre()))for(var ed:d.obtenerEdicionesEvento(e.getNombre()))if(nombre.equals(ed.getIdNombre())){
                if(ed.getEstado()!=EstadoEdicion.ACEPTADA&&!propio(usuario,ed))throw new web.remoto.ErrorCentral("NO_ENCONTRADO","No encontrado");
                elegida=ed;evento=e.getNombre();break;
            }
            if(elegida==null)throw new web.remoto.ErrorCentral("NO_ENCONTRADO","No encontrado");
        }
        resultado.setEvento(evento);resultado.setEdicion(elegida);
        resultado.setEdiciones(evento.isEmpty()?List.of():d.obtenerEdicionesEvento(evento).stream().filter(e->e.getEstado()==EstadoEdicion.ACEPTADA).toList());
        return resultado;
    }
}
