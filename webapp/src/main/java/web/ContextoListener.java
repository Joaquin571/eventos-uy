package web;
import contratos.ServicioPortal;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;
import web.remoto.*;
import java.util.logging.Logger;
/** Tomcat solamente crea clientes: no inicia persistencia ni una instancia de Sistema. */
@WebListener
public class ContextoListener implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(ContextoListener.class.getName());
    public void contextInitialized(ServletContextEvent evento) {
        ConfiguracionWeb configuracion = ConfiguracionWeb.cargar();
        evento.getServletContext().setAttribute("configuracionCentral", configuracion);
        LOG.info(() -> "Configuración SOAP: archivo=" + ConfiguracionWeb.archivoConfiguracion().toAbsolutePath().normalize()
                + "; portal=" + configuracion.portal() + "; sesiones=" + configuracion.sesiones());
    }
    static ConfiguracionWeb configuracion(HttpServletRequest req) {
        return (ConfiguracionWeb)req.getServletContext().getAttribute("configuracionCentral");
    }
    public static ServicioPortal central(HttpServletRequest req) {
        Object doble=req.getServletContext().getAttribute("central");
        if(doble instanceof ServicioPortal remoto)return remoto;
        return new ClienteCentral(configuracion(req).portal(), SesionWeb.credencial(req));
    }
    static ServicioSesiones sesiones(HttpServletRequest req) {
        Object doble=req.getServletContext().getAttribute("sesionesCentral");
        if(doble instanceof ServicioSesiones remoto)return remoto;
        return new ClienteSesionesCentral(configuracion(req).sesiones());
    }
}
