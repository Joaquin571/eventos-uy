package web.remoto;

import com.sun.xml.ws.developer.JAXWSProperties;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.MessageContext;
import web.soap.generado.sesiones.*;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;
import java.util.*;

/** P03: credencial solo en encabezados HTTP, fija y nunca en el cuerpo SOAP. */
public final class ClienteSesionesCentral {
    private final URI destino;
    private final SSLSocketFactory sockets;
    public ClienteSesionesCentral(URI destino){this(destino,null);}
    public ClienteSesionesCentral(URI destino,SSLSocketFactory sockets){this.destino=ConfiguracionWeb.validar(destino);this.sockets=sockets;}
    public record Inicio(String nickname,String nombre,String rol,String credencial){
        @Override public String toString(){return "Inicio[credencial omitida]";}
    }
    private Sesiones puerto(){
        var servicio=new SesionesService();var puerto=servicio.getSesionesPort();
        var contexto=((BindingProvider)puerto).getRequestContext();
        contexto.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,destino.toString());
        contexto.put("com.sun.xml.ws.connect.timeout",4000);contexto.put("com.sun.xml.ws.request.timeout",12000);
        if(sockets!=null)contexto.put(JAXWSProperties.SSL_SOCKET_FACTORY,sockets);
        return puerto;
    }
    public Inicio iniciar(String identificador,String contrasenia){
        var puerto=puerto();
        try{
            var usuario=puerto.iniciarSesion(identificador,contrasenia);
            Object valor=((BindingProvider)puerto).getResponseContext().get(MessageContext.HTTP_RESPONSE_HEADERS);
            String credencial=null;
            if(valor instanceof Map<?,?> encabezados)for(var entrada:encabezados.entrySet()){
                if(entrada.getKey() instanceof String nombre && nombre.equalsIgnoreCase("X-EventosUy-Session")){
                    if(credencial!=null || !(entrada.getValue() instanceof List<?> lista) || lista.size()!=1
                            || !(lista.getFirst() instanceof String texto) || !texto.matches("[A-Za-z0-9_-]{43}"))throw new WebServiceException();
                    credencial=texto;
                }
            }
            if(credencial==null || usuario==null || usuario.getRol()==null)throw new WebServiceException();
            return new Inicio(usuario.getNickname(),usuario.getNombre(),usuario.getRol().value(),credencial);
        }catch(SesionSoapException e){
            if("CREDENCIALES_INVALIDAS".equals(e.getFaultInfo().getCodigo()) || "SOLICITUD_MALFORMADA".equals(e.getFaultInfo().getCodigo()))
                throw new IllegalArgumentException("El nickname, el correo o la contrase?a no son correctos.");
            throw new ErrorCentral("SERVICIO_NO_DISPONIBLE","No se pudo iniciar la sesión.");
        }catch(WebServiceException e){throw new ErrorCentral("SERVICIO_NO_DISPONIBLE","El servidor Central no está disponible.");}
    }
    public void cerrar(String credencial){
        if(credencial==null)return;
        var puerto=puerto();((BindingProvider)puerto).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS,
                Map.of("Authorization",List.of("Bearer "+credencial)));
        try{puerto.cerrarSesion();}
        catch(SesionSoapException | WebServiceException e){throw new ErrorCentral("SERVICIO_NO_DISPONIBLE","No se pudo notificar el cierre de sesión.");}
    }
}
