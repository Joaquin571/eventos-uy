package web;

import implementacion.Fabrica;
import interfaces.ISistema;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;
import persistencia.BaseDeDatos;

@WebListener
public class ContextoListener implements ServletContextListener {

    private static final String ATRIBUTO_SISTEMA = "sistema";

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        BaseDeDatos.inicializar();
        evento.getServletContext().setAttribute(
                ATRIBUTO_SISTEMA, Fabrica.getInstance().getISistema());
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        BaseDeDatos.cerrar();
    }

    // Los servlets obtienen la lógica con ContextoListener.sistema(request)
    public static ISistema sistema(HttpServletRequest request) {
        return (ISistema) request.getServletContext().getAttribute(ATRIBUTO_SISTEMA);
    }
}