
package web;

import datatypes.*;
import interfaces.ISistema;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Collection;

@WebServlet("/alta-tipo-registro")
public class AltaTipoRegistroServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        DtOrganizador organizador = obtenerOrganizador(req, resp);
        if (organizador == null) {
            return;
        }

        ISistema sistema = ContextoListener.sistema(req);

        Collection<DtEdicion> ediciones =
                sistema.obtenerEdicionesOrganizador(
                        organizador.getNickname()
                );

        req.setAttribute("ediciones", ediciones);

        req.getRequestDispatcher(
                "/WEB-INF/views/alta-tipo-registro.jsp"
        ).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        DtOrganizador organizador = obtenerOrganizador(req, resp);
        if (organizador == null) {
            return;
        }

        ISistema sistema = ContextoListener.sistema(req);

        try {
            String edicion = requerido(req, "edicion");

            boolean propia = sistema
                    .obtenerEdicionesOrganizador(
                            organizador.getNickname()
                    )
                    .stream()
                    .anyMatch(e ->
                            e.getIdNombre().equals(edicion)
                    );

            if (!propia) {
                throw new IllegalArgumentException(
                        "La edición no pertenece al organizador."
                );
            }

            String nombre = requerido(req, "nombre");
            String descripcion = requerido(req, "descripcion");

            float costo = Float.parseFloat(
                    requerido(req, "costo")
            );

            int cupo = Integer.parseInt(
                    requerido(req, "cupo")
            );

            if (!Float.isFinite(costo) || costo < 0) {
                throw new IllegalArgumentException(
                        "El costo debe ser un número no negativo."
                );
            }

            if (cupo < 1) {
                throw new IllegalArgumentException(
                        "El cupo debe ser mayor que cero."
                );
            }

            DtTipoRegistro tipo = new DtTipoRegistro(
                    nombre,
                    descripcion,
                    costo,
                    cupo
            );

            boolean creado = sistema.altaTipoRegistro(
                    tipo,
                    edicion
            );

            if (!creado) {
                throw new IllegalArgumentException(
                        "No se pudo crear el tipo de registro. " +
                                "Verificá que no exista otro con el mismo nombre."
                );
            }

            req.setAttribute(
                    "exito",
                    "Tipo de registro creado correctamente."
            );

        } catch (NumberFormatException e) {
            req.setAttribute(
                    "error",
                    "El costo y el cupo deben ser números válidos."
            );

        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
        }

        doGet(req, resp);
    }

    private DtOrganizador obtenerOrganizador(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        HttpSession sesion = req.getSession(false);

        if (sesion == null) {
            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Debés iniciar sesión como organizador."
            );
            return null;
        }

        Object usuario = sesion.getAttribute("usuario");

        if (!(usuario instanceof DtOrganizador)) {
            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Esta operación requiere un organizador."
            );
            return null;
        }

        return (DtOrganizador) usuario;
    }

    private String requerido(
            HttpServletRequest req,
            String campo
    ) {
        String valor = req.getParameter(campo);

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "El campo " + campo + " es obligatorio."
            );
        }

        return valor.trim();
    }
}
