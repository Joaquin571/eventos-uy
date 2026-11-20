package central.soap;

import jakarta.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Única lectura de identidad remota; nunca parámetros, DTO ni roles del formulario. */
final class CredencialesSoap {
    private static final Pattern BEARER=Pattern.compile("Bearer +([A-Za-z0-9_-]{43})",Pattern.CASE_INSENSITIVE);
    private CredencialesSoap() { }
    static String leerOpcional(MessageContext contexto) throws SesionSoapException {
        Object propiedad=contexto.get(MessageContext.HTTP_REQUEST_HEADERS);String valor=null;
        if(propiedad instanceof Map<?,?> encabezados) for(var entrada:encabezados.entrySet()) {
            if(!(entrada.getKey() instanceof String nombre) || !"Authorization".equalsIgnoreCase(nombre)) continue;
            if(valor!=null || !(entrada.getValue() instanceof List<?> valores) || valores.size()!=1
                    || !(valores.getFirst() instanceof String texto)) throw invalido();
            valor=texto;
        }
        if(valor==null) return null;
        var matcher=BEARER.matcher(valor);if(!matcher.matches()) throw invalido();
        return matcher.group(1);
    }
    static String leerRequerida(MessageContext contexto) throws SesionSoapException {
        String valor=leerOpcional(contexto);if(valor==null) throw invalido();return valor;
    }
    private static SesionSoapException invalido(){return new SesionSoapException("SOLICITUD_MALFORMADA","Se requiere un encabezado Authorization Bearer válido.");}
}
