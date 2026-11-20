package web;
import contratos.ServicioPortal;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;
import web.remoto.*;
/** Tomcat solamente crea clientes: no inicia persistencia ni una instancia de Sistema. */
@WebListener
public class ContextoListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent evento) {
        evento.getServletContext().setAttribute("configuracionCentral", ConfiguracionWeb.cargar());
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
