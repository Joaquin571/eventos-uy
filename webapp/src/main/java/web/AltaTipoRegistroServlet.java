package web;

import contratos.dto.*;
import contratos.ServicioPortal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/alta-tipo-registro")
public class AltaTipoRegistroServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        DtOrganizador organizador = obtenerOrganizador(req, resp);
        if (organizador == null) {
            return;
        }

        MensajesFlash.mostrar(req, "/alta-tipo-registro");

        ServicioPortal sistema = ContextoListener.central(req);

        Collection<DtEdicion> ediciones =
                sistema.consultarPerfil(organizador.getNickname()).getEdiciones();

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

        ServicioPortal sistema = ContextoListener.central(req);

        Map<String, String> form = new HashMap<>();
        for (String campo : new String[]{"edicion", "nombre", "descripcion", "costo", "cupo"}) {
            String valor = req.getParameter(campo);
            form.put(campo, valor == null ? "" : valor);
        }
        req.setAttribute("form", form);

        try {
            String edicion = form.get("edicion").trim();
            String nombre = form.get("nombre").trim();
            String descripcion = form.get("descripcion").trim();
            float costo = Float.parseFloat(campoNumericoRequerido(form, "costo"));
            int cupo = Integer.parseInt(campoNumericoRequerido(form, "cupo"));

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

            MensajesFlash.guardar(req, "/alta-tipo-registro", "Tipo de registro creado correctamente.", null);
            resp.sendRedirect(req.getContextPath() + "/alta-tipo-registro");
            return;

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

    private String campoNumericoRequerido(Map<String, String> form, String campo) {
        String valor = form.get(campo);
        if (valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        return valor.trim();
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
}
