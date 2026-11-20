package web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Comunica una visita válida al Servidor Central. La implementación de producción llama a la operación
 * SOAP registrarVisitaEvento(String evento) mediante ClienteCentral; el Web no accede a la base de datos.
 */
@FunctionalInterface
interface InformanteVisitas {
    void registrar(HttpServletRequest req, String evento) throws Exception;
}
