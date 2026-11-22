package web.remoto;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Configuración de destinos HTTPS; la JVM conserva su validación TLS normal. */
public final class ConfiguracionWeb {
    private final URI portal,sesiones;
    public ConfiguracionWeb(URI portal,URI sesiones){this.portal=validar(portal);this.sesiones=validar(sesiones);}
    public URI portal(){return portal;}
    public URI sesiones(){return sesiones;}
    public static URI validar(URI uri){
        if(uri==null || !"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost()==null
                || uri.getUserInfo()!=null || uri.getQuery()!=null || uri.getFragment()!=null)
            throw new IllegalArgumentException("El destino Central debe utilizar HTTPS.");
        return uri;
    }
    public static Path archivoConfiguracion(){
        return System.getProperty("eventos.web.config")==null
                ?Path.of(System.getProperty("user.home"),".eventosUy","web.properties")
                :Path.of(System.getProperty("eventos.web.config"));
    }
    public static ConfiguracionWeb cargar(){
        Path archivo=archivoConfiguracion();
        try(var lector=Files.newBufferedReader(archivo)){
            Properties p=new Properties();p.load(lector);String host=p.getProperty("central.host");int puerto=Integer.parseInt(p.getProperty("central.puerto"));
            if(host==null || host.isBlank() || puerto<1 || puerto>65535)throw new IllegalArgumentException();
            URI portal=new URI("https",null,host,puerto,p.getProperty("central.ruta.portal","/portal"),null,null);
            URI sesiones=new URI("https",null,host,puerto,p.getProperty("central.ruta.sesiones","/sesiones"),null,null);
            return new ConfiguracionWeb(portal,sesiones);
        }catch(Exception e){throw new ErrorCentral("SERVICIO_NO_DISPONIBLE","Revisá la configuración HTTPS de Central.");}
    }
}
