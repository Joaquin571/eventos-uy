package web;

import contratos.dto.*;
import contratos.ServicioPortal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/alta-edicion")
@MultipartConfig(maxFileSize = 2L * 1024 * 1024, maxRequestSize = 3L * 1024 * 1024)
public class AltaEdicionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {


        MensajesFlash.mostrar(req, "/alta-edicion");

        req.setAttribute(
                "eventos",
                ContextoListener.central(req).listarEventos()
        );

        req.getRequestDispatcher("/WEB-INF/views/alta-edicion.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {


        ServicioPortal sistema = ContextoListener.central(req);

        Map<String, String> form = new HashMap<>();
        for (String campo : new String[]{"evento", "nombre", "sigla", "ciudad", "pais", "fechaInicio", "fechaFin", "videoYoutube"}) {
            String valor = req.getParameter(campo);
            form.put(campo, valor == null ? "" : valor);
        }
        req.setAttribute("form", form);

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

            DtImagen imagen = Subidas.leerImagen(req, "imagen");

            DtEdicion edicion = new DtEdicion(
                    nombre,
                    sigla,
                    inicio,
                    fin,
                    LocalDate.now(),
                    ciudad,
                    pais,
                    SesionWeb.usuario(req) == null ? "" : SesionWeb.usuario(req).getNickname()
            );

            edicion.setVideoYoutube(req.getParameter("videoYoutube"));
            boolean creada = sistema.altaEdicion(edicion, evento, imagen);

            if (!creada) {
                throw new IllegalArgumentException(
                        "No se pudo crear la edición. " +
                                "Verificá el evento, el organizador y el nombre único."
                );
            }

            MensajesFlash.guardar(req, "/alta-edicion",
                    "Edición creada correctamente. Estado: Ingresada.", null);
            resp.sendRedirect(req.getContextPath() + "/alta-edicion");
            return;

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

    private String obligatorio(
            HttpServletRequest req,
            String campo
    ) {
        String valor = req.getParameter(campo);

        return valor == null ? "" : valor.trim();
    }
}
