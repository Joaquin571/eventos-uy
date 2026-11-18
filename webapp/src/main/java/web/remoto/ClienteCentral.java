package web.remoto;
import contratos.ServicioPortal;
import contratos.ConversorDatos;
import contratos.dto.*;
import web.soap.generado.portal.Portal;
import web.soap.generado.portal.PortalService;
import web.soap.generado.portal.OperacionSoapException;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.MessageContext;
import com.sun.xml.ws.developer.JAXWSProperties;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;
import java.util.*;

/** Proxy generado nuevo por invocación; nunca contiene acceso local a lógica. */
public final class ClienteCentral implements ServicioPortal {
 private final URI destino;private final String credencial;private final SSLSocketFactory sockets;
 public ClienteCentral(URI destino,String credencial){this(destino,credencial,null);}
 public ClienteCentral(URI destino,String credencial,SSLSocketFactory sockets){this.destino=ConfiguracionWeb.validar(destino);this.credencial=credencial;this.sockets=sockets;}
 private Portal puerto(){
  Portal p=new PortalService().getPortalPort();var c=((BindingProvider)p).getRequestContext();
  c.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,destino.toString());c.put("com.sun.xml.ws.connect.timeout",4000);c.put("com.sun.xml.ws.request.timeout",12000);
  if(sockets!=null)c.put(JAXWSProperties.SSL_SOCKET_FACTORY,sockets);
  if(credencial!=null)c.put(MessageContext.HTTP_REQUEST_HEADERS,Map.of("Authorization",List.of("Bearer "+credencial)));
  return p;
 }
 @FunctionalInterface private interface Operacion<T>{T ejecutar(Portal p)throws OperacionSoapException;}
 private <T>T invocar(Operacion<T> operacion){
  try{return operacion.ejecutar(puerto());}
  catch(OperacionSoapException e){
   String codigo=e.getFaultInfo().getCodigo();String mensaje=e.getFaultInfo().getMensaje();
   if("DATOS_INVALIDOS".equals(codigo))throw new IllegalArgumentException(mensaje);
   throw new ErrorCentral(codigo,mensaje);
  }catch(WebServiceException e){throw new ErrorCentral("SERVICIO_NO_DISPONIBLE","El servidor Central no está disponible.");}
 }
 @Override public List<DtEvento> listarEventos(){return invocar(p->p.listarEventos()).stream().map(v->ConversorDatos.convertir(v,DtEvento.class)).toList();}
 @Override public void registrarVisitaEvento(String evento){invocar(p->{p.registrarVisitaEvento(evento);return null;});}
 @Override public List<DtCategoria> listarCategorias(){return invocar(p->p.listarCategorias()).stream().map(v->ConversorDatos.convertir(v,DtCategoria.class)).toList();}
 @Override public List<DtInstitucion> listarInstituciones(){return invocar(p->p.listarInstituciones()).stream().map(v->ConversorDatos.convertir(v,DtInstitucion.class)).toList();}
 @Override public DtInstitucion consultarInstitucion(String nombre){return ConversorDatos.convertir(invocar(p->p.consultarInstitucion(nombre)),DtInstitucion.class);}
 @Override public List<DtUsuario> listarUsuarios(){return invocar(p->p.listarUsuarios()).stream().map(v->ConversorDatos.convertir(v,DtUsuario.class)).toList();}
 @Override public DtUsuario usuarioActual(){return ConversorDatos.convertir(invocar(p->p.usuarioActual()),DtUsuario.class);}
 @Override public DtPerfilUsuario consultarPerfil(String nickname){return ConversorDatos.convertir(invocar(p->p.consultarPerfil(nickname)),DtPerfilUsuario.class);}
 @Override public boolean disponibleNickname(String valor){return invocar(p->p.disponibleNickname(valor));}
 @Override public boolean disponibleCorreo(String valor){return invocar(p->p.disponibleCorreo(valor));}
 @Override public boolean altaInstitucion(DtInstitucion datos, DtImagen imagen){return invocar(p->p.altaInstitucion(ConversorDatos.convertir(datos,web.soap.generado.portal.DtInstitucion.class), ConversorDatos.convertir(imagen,web.soap.generado.portal.DtImagen.class)));}
 @Override public boolean altaEvento(DtEvento datos, DtImagen imagen){return invocar(p->p.altaEvento(ConversorDatos.convertir(datos,web.soap.generado.portal.DtEvento.class), ConversorDatos.convertir(imagen,web.soap.generado.portal.DtImagen.class)));}
 @Override public boolean altaEdicion(DtEdicion datos, String evento, DtImagen imagen){return invocar(p->p.altaEdicion(ConversorDatos.convertir(datos,web.soap.generado.portal.DtEdicion.class), evento, ConversorDatos.convertir(imagen,web.soap.generado.portal.DtImagen.class)));}
 @Override public List<DtResultadoBusqueda> buscarEventosYEdiciones(String texto, OrdenBusqueda orden){
  return invocar(p->p.buscarEventosYEdiciones(texto, orden==null?null:web.soap.generado.portal.OrdenBusqueda.valueOf(orden.name())))
      .stream().map(v->ConversorDatos.convertir(v,DtResultadoBusqueda.class)).toList();
 }
 @Override public void finalizarEvento(String evento){invocar(p->{p.finalizarEvento(evento);return true;});}
 @Override public boolean altaTipoRegistro(DtTipoRegistro datos, String edicion){return invocar(p->p.altaTipoRegistro(ConversorDatos.convertir(datos,web.soap.generado.portal.DtTipoRegistro.class), edicion));}
 @Override public boolean altaPatrocinio(DtPatrocinio datos, String evento){return invocar(p->p.altaPatrocinio(ConversorDatos.convertir(datos,web.soap.generado.portal.DtPatrocinio.class), evento));}
 @Override public DtOpcionesAltaPatrocinio obtenerOpcionesAltaPatrocinio(String evento, String edicion){return ConversorDatos.convertir(invocar(p->p.obtenerOpcionesAltaPatrocinio(evento, edicion)),DtOpcionesAltaPatrocinio.class);}
 @Override public DtConsultaPatrocinios consultarPatrocinios(String evento, String edicion, String institucion){return ConversorDatos.convertir(invocar(p->p.consultarPatrocinios(evento, edicion, institucion)),DtConsultaPatrocinios.class);}
 @Override public DtSeleccionEdicion consultarEdiciones(String evento, String edicion){return ConversorDatos.convertir(invocar(p->p.consultarEdiciones(evento, edicion)),DtSeleccionEdicion.class);}
 @Override public DtDetalleEdicion consultarDetalleEdicion(String edicion){return ConversorDatos.convertir(invocar(p->p.consultarDetalleEdicion(edicion)),DtDetalleEdicion.class);}
 @Override public DtTipoRegistro consultarTipoRegistro(String edicion, String tipo){return ConversorDatos.convertir(invocar(p->p.consultarTipoRegistro(edicion, tipo)),DtTipoRegistro.class);}
 @Override public DtConsultaRegistros consultarRegistros(String edicion, String asistente){return ConversorDatos.convertir(invocar(p->p.consultarRegistros(edicion, asistente)),DtConsultaRegistros.class);}
 @Override public DtFormularioRegistro formularioRegistro(String evento, String edicion, String tipo){return ConversorDatos.convertir(invocar(p->p.formularioRegistro(evento, edicion, tipo)),DtFormularioRegistro.class);}
 @Override public boolean registrar(String edicion, String tipo, String modalidad, String codigo){return invocar(p->p.registrar(edicion, tipo, modalidad, codigo));}
 @Override public DtImagen obtenerImagen(TipoEntidadImagen tipo, String clave){return ConversorDatos.convertir(invocar(p->p.obtenerImagen(ConversorDatos.convertir(tipo,web.soap.generado.portal.TipoEntidadImagen.class), clave)),DtImagen.class);}
 @Override public boolean altaUsuario(EntradaUsuario entrada){return invocar(p->p.altaUsuario(ConversorDatos.convertir(entrada,web.soap.generado.portal.EntradaUsuario.class)));}
 @Override public DtUsuario modificarUsuario(EntradaUsuario entrada){return ConversorDatos.convertir(invocar(p->p.modificarUsuario(ConversorDatos.convertir(entrada,web.soap.generado.portal.EntradaUsuario.class))),DtUsuario.class);}
 @Override public List<DtEvento> listarEventosPorCategoria(String categoria){return invocar(p->p.listarEventosPorCategoria(categoria)).stream().map(v->ConversorDatos.convertir(v,DtEvento.class)).toList();}
}
