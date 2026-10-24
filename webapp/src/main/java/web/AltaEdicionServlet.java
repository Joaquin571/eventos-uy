
package web;

import datatypes.*;
import interfaces.ISistema;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet("/alta-edicion")
@MultipartConfig
public class AltaEdicionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        DtOrganizador organizador = obtenerOrganizador(req, resp);
        if (organizador == null) {
            return;
        }

        req.setAttribute(
                "eventos",
                ContextoListener.sistema(req).listarEventos()
        );

        req.getRequestDispatcher("/WEB-INF/views/alta-edicion.jsp")
                .forward(req, resp);
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
            String evento = obligatorio(req, "evento");
            String nombre = obligatorio(req, "nombre");
            String sigla = obligatorio(req, "sigla");
            String ciudad = obligatorio(req, "ciudad");
            String pais = obligatorio(req, "pais");

            LocalDate inicio = LocalDate.parse(
                    obligatorio(req, "fechaInicio")
            );

            LocalDate fin = LocalDate.parse(
                    obligatorio(req, "fechaFin")
            );

            if (fin.isBefore(inicio)) {
                throw new IllegalArgumentException(
                        "La fecha de fin no puede ser anterior al inicio."
                );
            }

            DtImagen imagen = Subidas.leerImagen(req, "imagen");

            if (imagen != null) {
                sistema.validarImagen(imagen);
            }

            DtEdicion edicion = new DtEdicion(
                    nombre,
                    sigla,
                    inicio,
                    fin,
                    LocalDate.now(),
                    ciudad,
                    pais,
                    organizador.getNickname()
            );

            boolean creada = sistema.altaEdicion(edicion, evento);

            if (!creada) {
                throw new IllegalArgumentException(
                        "No se pudo crear la edición. " +
                                "Verificá el evento, el organizador y el nombre único."
                );
            }

            if (imagen != null) {
                try {
                    sistema.asociarImagen(
                            TipoEntidadImagen.EDICION,
                            nombre,
                            imagen
                    );
                } catch (IllegalArgumentException e) {
                    req.setAttribute(
                            "advertencia",
                            "La edición se creó, pero no se pudo guardar su imagen."
                    );
                }
            }

            req.setAttribute(
                    "exito",
                    "Edición creada correctamente. Estado: Ingresada."
            );

        } catch (DateTimeParseException e) {
            req.setAttribute(
                    "error",
                    "Las fechas ingresadas no son válidas."
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

    private String obligatorio(
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
